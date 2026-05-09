---
title: "Postgres B-Tree and the Write Path"
summary: "A deep tour of `src/backend/access/nbtree/` — the B+-tree that backs every CREATE INDEX in Postgres. From WAL-logged page splits to the B-link concurrency that lets concurrent INSERTs through without locking the whole tree."
prereqs:
  - trees-b-tree-introduction-to-b-trees
  - trees-self-balancing-bst-overview
---

# 1. Postgres B-Tree and the Write Path

## The Hook

You write `CREATE INDEX ON users(email)` in Postgres. From that moment, every `SELECT * FROM users WHERE email = ?` runs in `O(log n)` instead of `O(n)`. Behind the scenes, Postgres has built a B+-tree, page by page, on disk, and the B+-tree is now part of the Write-Ahead Log (WAL) — every modification appears in the WAL before it's applied to the page, so a crash mid-update can roll back cleanly.

This is the chapter where the [B-tree chapter](/cortex/data-structures-and-algorithms/trees-b-tree-introduction-to-b-trees) meets the messiness of real implementation. We'll tour `src/backend/access/nbtree/` — the canonical Postgres B+-tree code — looking at:

- The page layout and why it's 8 KB.
- B-link concurrency: how concurrent INSERTs avoid acquiring an exclusive lock on the whole tree.
- The split logic and the "right-link" that lets readers stumble onto an in-progress split safely.
- The WAL records that make every modification crash-recoverable.
- VACUUM and how Postgres reclaims dead tuples without rewriting the index.

By the end you'll be able to read `nbtinsert.c` and follow the algorithm; recognise the difference between Postgres's B-link variant and the textbook B+-tree; and explain why Postgres's INSERT throughput holds up at high concurrency.

---

## Table of contents

1. [The page layout](#the-page-layout)
2. [Search: the read path](#search-the-read-path)
3. [Insert: the write path](#insert-the-write-path)
4. [B-link concurrency](#b-link-concurrency)
5. [WAL: making it crash-safe](#wal-making-it-crash-safe)
6. [VACUUM and dead tuples](#vacuum-and-dead-tuples)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The page layout

Every Postgres index is stored as a sequence of **pages**, each `BLCKSZ` bytes (default 8 KB, configurable at compile time). A B+-tree page contains:

- A **page header** (~24 bytes): LSN, special offsets, opaque-area offset, page version.
- An **item-pointer array** (one slot per item, growing from low addresses up).
- A **free-space gap** in the middle.
- The **items themselves** (growing from high addresses down).
- A **special area** at the very end (16 bytes for B-tree metadata).

The metadata in the special area:

```c
// Definition in src/include/access/nbtree.h
typedef struct BTPageOpaqueData {
    BlockNumber btpo_prev;       // left sibling page (0 for leftmost)
    BlockNumber btpo_next;       // right sibling page = the B-link!
    union {
        uint32 level;            // tree level (0 = leaf)
        TransactionId xact;      // for deleted pages
    } btpo;
    uint16 btpo_flags;           // BTP_LEAF, BTP_ROOT, BTP_DELETED, etc.
    BTCycleId btpo_cycleid;
} BTPageOpaqueData;
```

The `btpo_next` field is the **B-link** — the right-sibling pointer that the B-link variant adds to a B+-tree. Every page knows its right neighbour. This unlocks a subtler concurrency model than the textbook B-tree.

Why 8 KB? The default page size matches the OS page size on most platforms (4 KB on Linux × 2 for lower fragmentation). With 8 KB pages and ~50-byte average tuples, a leaf node holds ~150 keys. Internal nodes hold ~250 routing keys (smaller payloads). Tree height for a 1-billion-row table: 4-5. Five disk seeks per index lookup, in the worst case.

***

# Search: the read path

The standard B+-tree descent: start at the root, find the right child based on key comparison, descend until reaching a leaf. The leaf either has the key (with a TID — Tuple Identifier — pointing to the actual row in the heap) or doesn't.

The wrinkle: the B-link. After descending to a leaf, the search may find that the desired key is actually on the *right neighbour* (the B-link target). This happens when a concurrent split moved the key range. The reader simply follows `btpo_next` and continues searching there.

```c
// Sketch of nbtsearch.c::_bt_search
buf = _bt_getroot(rel, BT_READ);
for (;;) {
    page = BufferGetPage(buf);
    opaque = BTPageGetOpaque(page);
    if (P_ISLEAF(opaque)) break;             // reached a leaf
    offnum = _bt_binsrch(rel, &state, scankey);
    childblkno = ItemIdGetTID(...);
    buf = _bt_relandgetbuf(rel, buf, childblkno, BT_READ);
}
```

`_bt_binsrch` is binary search on the page's items. Page reads are protected by buffer-pool latches, not heavyweight locks — they're cheap and short-lived.

***

# Insert: the write path

`_bt_doinsert` is the entry point. It:

1. Walks down to the leaf where the new key belongs.
2. Acquires an *exclusive* page lock on the leaf.
3. If there's space, inserts the item in sorted order.
4. If not, calls `_bt_split` to split the page.

The descent uses *crabbing* — the algorithm holds a shared lock on the current page, acquires a lock on the child, releases the parent lock, repeats. This avoids holding parent locks during long descents.

```c
// Sketch of nbtinsert.c
state = _bt_search(rel, key, &stack, BT_WRITE, snapshot);
if (page_has_space) {
    _bt_insertonpg(rel, state, item, NULL, false, ...);
} else {
    _bt_split(rel, state, item, ...);
}
```

***

# B-link concurrency

The textbook B-tree uses a "lock the whole path" strategy for inserts. Postgres uses **B-link trees** (Lehman-Yao 1981), which permits concurrent inserts via a different invariant.

The key insight: every page has a *right-link*. After a split, the *original* page's right-link points at the *new* sibling. Readers descending from the parent might reach the original page even though the splitting key is now on the new page — the right-link bridges them.

This means concurrent inserters don't need to lock the parent during the split. The split is a *local* operation on the leaf and its right-link; the parent is updated *afterward*, in a separate step (which can be amortised across many inserts).

Concretely, the post-split sequence is:

1. Allocate a new right-sibling page.
2. Copy the upper half of keys into the new page.
3. Update the right-sibling's right-link (to the original right-sibling, if any).
4. Update the original page's right-link to point at the new page.
5. Mark the original page as "split-not-yet-inserted-into-parent" via a flag.
6. *Later* (during the next descent or a recovery pass), insert the new page into the parent.

Step 6 can happen lazily because readers who descend to the original page can detect the split (via the flag and the right-link) and follow the right-link to find missing keys.

***

# WAL: making it crash-safe

Every page modification has a WAL record. The standard records for B-tree:

- `XLOG_BTREE_INSERT_LEAF` — insert on a leaf.
- `XLOG_BTREE_SPLIT_L` / `XLOG_BTREE_SPLIT_R` — split, original page kept (L) vs split goes onto right (R).
- `XLOG_BTREE_DELETE` — VACUUM-driven deletion of dead tuples.
- `XLOG_BTREE_NEWROOT` — root grew by a level.
- `XLOG_BTREE_REUSE_PAGE` — page reused after VACUUM.

The WAL guarantees: every modification is logged *before* the page is flushed to disk. On crash, replay the WAL from the last checkpoint forward; the index is reconstructed identically.

Reading `nbtxlog.c` shows the WAL replay logic — every WAL record has a corresponding `_bt_redo_*` function that replays it.

***

# VACUUM and dead tuples

Postgres uses MVCC: deleted rows are *not* immediately freed; they're "dead". The B-tree carries TIDs to dead tuples until VACUUM cleans up.

VACUUM walks the index, identifying dead tuples (TIDs whose corresponding heap tuple is dead and not visible to any active transaction). It removes those entries from leaf pages, occasionally shrinks the index by reclaiming pages, and updates the FSM (Free Space Map).

The interesting part: VACUUM doesn't *rebuild* the index. It only deletes dead entries and reclaims freed pages. The B-tree shape stays. Over a long-running database without VACUUM, the index grows monotonically; bloat is real and can dominate disk usage.

***

# Edge cases and pitfalls

- **Hot keys.** A key constantly inserted-and-deleted creates a "hotspot" — a single page repeatedly modified. Postgres's `btreebloat` extension diagnoses this.
- **Very wide keys** (>1/3 page size) can't fit; Postgres errors. The fix: `WHERE` clauses should filter, not store, large strings.
- **Concurrent INSERT into a partial index** can deadlock if the partial-index condition involves complex expressions. Reduce to simpler conditions or add a regular index alongside.
- **Index bloat** without VACUUM. A high-update table can grow its indexes to hundreds of GB. `pg_stat_user_indexes` and `pgstattuple` extensions diagnose.
- **The "dump and reload" myth.** Some users believe rebuilding indexes after large updates is necessary. With autovacuum tuned correctly, it's not.

***

# Cross-links

- **Prerequisites:** [B-Tree](/cortex/data-structures-and-algorithms/trees-b-tree-introduction-to-b-trees), [Self-Balancing BSTs Overview](/cortex/data-structures-and-algorithms/trees-self-balancing-bst-overview-self-balancing-bst-overview).
- **Sibling production deep-dives:** [LSM Trees in RocksDB and Cassandra](/cortex/data-structures-and-algorithms/dsa-in-real-systems-lsm-trees-rocksdb-cassandra) — the write-optimised alternative.
- **Source reference:** [Postgres `nbtree`](https://github.com/postgres/postgres/tree/master/src/backend/access/nbtree).

***

# Final takeaway

Postgres's B+-tree is the canonical production B-tree. Three patterns to internalise:

1. **B-link concurrency lets concurrent INSERTs through.** Right-links permit reader-discoverable splits without locking the whole path.
2. **WAL is what makes it crash-safe.** Every page modification logged first; replay reconstructs the index after crash.
3. **The textbook algorithm and the production code differ.** Real systems handle MVCC, WAL, partial indexes, hot keys, deadlocks, dead-tuple cleanup. Reading the source teaches you everything the textbook can't.
