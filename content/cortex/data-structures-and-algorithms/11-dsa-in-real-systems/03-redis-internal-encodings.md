---
title: "Redis Internal Encodings"
summary: "Redis stores Lists, Sets, Hashes, and Sorted Sets in different physical layouts depending on size: ziplist, listpack, intset, skiplist, hashtable. The right encoding for your data is what makes Redis fit so much in so little memory."
prereqs:
  - linear-structures-arrays-introduction
  - probabilistic-and-advanced-skip-list
---

# 3. Redis Internal Encodings

## The Hook

A Redis Hash with 5 fields uses *one* encoding; a Hash with 5000 fields uses a different one. A Sorted Set of 100 entries is stored differently than one with 10,000. Redis transparently switches between encodings as your data grows — and the small-data encoding can be 10× more memory-efficient than the general one.

The reason is the **constants Big-O hides** (covered in [Memory Model and Cache](/cortex/data-structures-and-algorithms/foundations-memory-model-and-cache)). For small `n`, a packed contiguous representation (`ziplist` / `listpack`) wins on both memory and cache locality, even though its asymptotic operations are `O(n)` instead of `O(1)`. For large `n`, the asymptotic difference dominates and Redis switches to a real hash table or skip list.

This chapter is the tour: which encoding Redis uses for each data type, the threshold for switching, and the design rationale (one of the cleanest "memory hierarchy in production" stories in open-source code).

---

## Table of contents

1. [The encoding-per-size pattern](#the-encoding-per-size-pattern)
2. [`ziplist` and its replacement `listpack`](#ziplist-and-its-replacement-listpack)
3. [`intset`](#intset)
4. [Hash table](#hash-table)
5. [Skip list (sorted sets)](#skip-list-sorted-sets)
6. [`quicklist` (large Lists)](#quicklist-large-lists)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The encoding-per-size pattern

Redis data types and their encodings:

| Type | Small encoding | Threshold | Large encoding |
|---|---|---|---|
| **String** | embstr (embedded) | 44 bytes | raw / int |
| **List** | listpack | `list-max-ziplist-size` (default 128) | quicklist |
| **Set** | intset (if all integers) | `set-max-intset-entries` (default 512) | hashtable |
| **Set** | listpack (if mixed types, ≤ 128) | `set-max-listpack-entries` | hashtable |
| **Hash** | listpack | `hash-max-listpack-entries` (default 128) | hashtable |
| **Sorted Set** | listpack | `zset-max-listpack-entries` (default 128) | skiplist + hashtable |

The thresholds are tunable via the Redis config. Defaults reflect the empirical sweet spot — below 128 entries, the packed layouts win; above, the asymptotic structures win.

***

# `ziplist` and its replacement `listpack`

A **ziplist** (until Redis 7.0) and its replacement **listpack** (Redis 7.0+) are *contiguous byte buffers* that encode a sequence of values. Each entry has a size prefix, the value's bytes, and a *backwards size* (for reverse traversal). The whole thing is one allocation.

For a hash with 10 entries, the listpack is roughly:

```
[total size] [num entries] (entry: field, entry: value)*10 [end byte]
```

Maybe 200 bytes for a 10-entry hash. A real hash table for the same data would have node overhead, bucket array overhead, padding — easily 1 KB or more. **5× memory savings** for the small-hash case.

The cost: every operation is `O(n)`. To find a field, scan from the start. To delete an entry, shift everything after it. For 10-100 elements, the constants on the linear scan beat the constants on a real hash table — both because the data fits in a single cache line or two, and because hash-function calls have non-trivial cost.

Listpack improved on ziplist by removing the "cascade update" problem: in ziplist, modifying one entry could cause a cascade of resizing if the size encoding crossed a boundary, leading to occasional `O(n²)` writes. Listpack's encoding eliminates this. Released in Redis 7.0; ziplist removed entirely in 7.2.

***

# `intset`

A **Set** of *all-integer* members under the threshold uses a packed sorted array of integers, called `intset`. Values are stored as int16, int32, or int64 — picking the smallest that fits all members.

`SADD` keeps the array sorted (binary-search-then-insert, `O(n)` due to the shift). `SISMEMBER` is binary search, `O(log n)`. Above 512 entries, Redis switches to a hash table.

The wins: int-only sets are common (user IDs, post IDs, etc.), and storing them packed beats `hashtable` by 4-8× memory for small `n`.

***

# Hash table

For large hashes (and sets), Redis falls back to a regular hash table. The implementation is in `src/dict.c`:

- Open addressing, no — Redis uses **separate chaining**. Each bucket is a linked list.
- `dictEntry` per element: 16 bytes plus pointers.
- **Incremental rehashing.** When the load factor hits 1.0, Redis allocates a new table double the size and starts moving entries — but only `1` bucket per operation, amortised across many calls. This avoids the `O(n)` stall a single rehash would cause.

The `table[2]` array is the rehashing state: while the rehash is in progress, both tables exist; lookups check both; inserts go to the new one. After all entries migrate, the old table is freed.

This is incremental garbage collection's idea applied to hash tables. The result: no single Redis operation ever takes more than a few microseconds, even on a hash table being rehashed.

***

# Skip list (sorted sets)

For sorted sets, Redis uses two structures in tandem:

- A **skip list** (`zskiplist` in `src/t_zset.c`) keyed by score, for `ZRANGE` and `ZSCORE` lookups.
- A **hash table** mapping member → score, for `ZADD` / `ZRANK` / `ZSCORE` `O(1)` lookup.

Both structures are kept in sync; `ZADD` updates both, `ZREM` deletes from both. Memory cost: ~2× for the dual structure, but every operation is fast: skip list for ranges, hash table for member lookups.

The skip list itself is the cleanest production skip list in open source. ~150 lines, well-commented, covered in detail in the [Skip List chapter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-skip-list).

Why skip list over RB-tree? Because Redis is single-threaded (one I/O event loop) — concurrency advantages of skip list don't apply, but the *simpler code* does. Antirez (Redis's creator) cited code clarity and the ease of supporting `ZRANGE`-style ordered iteration as the deciding factors.

***

# `quicklist` (large Lists)

Lists above the listpack threshold use **quicklist** — a linked list of listpacks. Each node holds a small listpack of items; nodes themselves are linked. This is a hybrid: the *outer* structure is a linked list (good for `O(1)` head/tail push/pop), the *inner* nodes are listpacks (good for memory locality and small overhead).

Compared to a pure linked list (which would have one allocation per item, terrible locality), quicklist is dramatically better. Compared to a single huge listpack, quicklist avoids the `O(n)` cost of insert-at-head when the list grows.

Tunable via `list-max-listpack-size` (default `-2`, meaning 8 KB per node).

***

# Edge cases and pitfalls

- **Encoding upgrade is one-way.** Once a Hash exceeds the threshold and switches to hashtable, it doesn't switch back when items are deleted. To shrink, `DEBUG SLEEP 0` triggers no resize; you'd have to `DUMP` and `RESTORE` (or re-insert into a fresh key).
- **Don't tune thresholds without measurement.** The defaults are empirical sweet spots. Lowering the threshold trades memory for query speed; raising it trades query speed for memory. Test on your workload.
- **`HGETALL` on a million-field hash is `O(n)` regardless of encoding.** The encoding optimises memory, not iteration. For large hashes, prefer `HSCAN` (which yields incrementally).
- **Memory measurement is tricky.** `OBJECT ENCODING <key>` tells you the encoding. `MEMORY USAGE <key>` gives the actual bytes. `OBJECT REFCOUNT` tells you about shared strings (Redis interns small integers).
- **Lazy expiration.** When a key with a TTL is accessed, Redis lazily checks expiration. Memory savings from compact encodings are lost if you have many expired-but-not-yet-collected keys.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Redis's encoding-per-size pattern is the cleanest "cache-aware engineering" story in open-source code; these facts come up in every Redis sizing conversation.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Encoding switch rule for a Redis Hash?</summary>

**A:** Below `hash-max-listpack-entries` (default 128) and `hash-max-listpack-value` (default 64 bytes per field/value): **listpack** (contiguous). Above: **hashtable** (chained).

</details>
<details>
<summary><strong>Q:</strong> Why does small-data listpack beat hashtable in memory?</summary>

**A:** Listpack is one contiguous allocation with size-prefixed entries. Hashtable adds bucket arrays + per-entry node overhead + pointer indirection. 5-10× memory savings for small data.

</details>
<details>
<summary><strong>Q:</strong> What replaces ziplist in Redis 7+?</summary>

**A:** **listpack**. Eliminates ziplist's "cascade update" problem (where a size-encoding change could trigger `O(n²)` rewrites).

</details>
<details>
<summary><strong>Q:</strong> Sorted set internal structure?</summary>

**A:** Two structures in tandem: a **skip list** keyed by score (for `ZRANGE`) plus a **hash table** mapping member → score (for `O(1)` `ZSCORE`/`ZRANK`).

</details>
<details>
<summary><strong>Q:</strong> What is a quicklist?</summary>

**A:** A linked list of listpacks. Outer linked-list for `O(1)` head/tail push/pop; inner listpacks for memory locality. Used by Redis Lists.

</details>
<details>
<summary><strong>Q:</strong> Incremental rehashing — what does it solve?</summary>

**A:** A hash-table grow-by-2 would be `O(n)` and stall the single-threaded server. Incremental rehashing migrates one bucket per operation; no individual op exceeds a few microseconds.

</details>
<details>
<summary><strong>Q:</strong> Encoding upgrade — one-way or reversible?</summary>

**A:** One-way. Once a Hash exceeds the threshold and switches to hashtable, deletes don't bring it back. To shrink, dump and restore.

</details>
<details>
<summary><strong>Q:</strong> Memory measurement commands?</summary>

**A:** `OBJECT ENCODING <key>` (which encoding), `MEMORY USAGE <key>` (actual bytes), `OBJECT REFCOUNT <key>` (shared-string detection).

</details>

## Source pointers

```
src/dict.c              — hash table; incremental rehashing in dictRehashStep
src/dict.h              — struct dict (the two-table design)
src/listpack.c          — listpack encoding (Redis 7+)
src/ziplist.c           — ziplist encoding (legacy, removed in 7.2)
src/quicklist.c         — quicklist (linked list of listpacks)
src/intset.c            — intset (packed sorted integer array)
src/t_zset.c            — sorted set: skip list + hash table
src/t_hash.c            — hash data type
src/t_string.c          — string with embstr / raw / int encodings
src/object.c            — generic object encoding helpers
```

Configuration knobs to know:

```
hash-max-listpack-entries / -value
list-max-listpack-size
set-max-listpack-entries / -intset-entries
zset-max-listpack-entries / -value
```

## Pattern triggers

- **"Why is my Redis using so much memory?"** → check `OBJECT ENCODING`; many small hashes already on hashtable encoding burn 5-10× memory
- **"Slow `HGETALL` on a 100k-field hash"** → that's `O(n)` regardless of encoding; use `HSCAN`
- **"Sorted set internals"** → skip list + hash table in `t_zset.c`
- **"Suspicious latency spikes during inserts"** → incremental rehashing migrating a bucket; usually fine
- **"What encoding for my workload?"** → defaults are empirical sweet spots; benchmark before tuning
- **"Memory budget tight on Redis"** → keep collections under threshold sizes; use `MEMORY USAGE` to verify
- **"Where to read the source?"** → `t_zset.c` is unusually clean; `dict.c` is the canonical incremental-rehash example

***

# Cross-links

- **Prerequisites:** [Hash Table](/cortex/data-structures-and-algorithms/linear-structures-hash-table-introduction-to-hash-tables), [Skip List](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-skip-list), [Memory Model and Cache](/cortex/data-structures-and-algorithms/foundations-memory-model-and-cache).
- **Source reference:** [Redis source](https://github.com/redis/redis): `src/t_zset.c` (sorted sets), `src/dict.c` (hash table), `src/listpack.c`, `src/intset.c`, `src/quicklist.c`.

***

# Final takeaway

Redis's encoding choices are a master class in cache-aware engineering. Three patterns to internalise:

1. **The right encoding depends on size.** Below ~128 entries, packed contiguous wins on memory and cache. Above, asymptotic structures win. Redis transparently switches.
2. **Incremental rehashing avoids stalls.** Dual-table hash table with one-bucket-per-op migration; no operation is ever slower than a few microseconds, even mid-rehash.
3. **Read the source.** `src/t_zset.c`, `src/dict.c`, and `src/listpack.c` are unusually clean, well-commented production code. A weekend reading them teaches you more about pragmatic data-structure engineering than most textbooks.

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->
