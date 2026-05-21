---
title: hash-table
summary: Hash table animations across four collision-resolution strategies (chaining, linear probing, quadratic probing, double hashing). One widget, one mode flag; per-step bucket overrides accumulate; per-step probe-sequence cursor + trail for the probing modes; per-step chain cursor for chaining mode; per-bucket highlight overlays in a closed kind catalog.
prereqs: []
---

# `hash-table`

## Purpose

A single widget covers source Phase 3 (Hash table — 57 source diagrams across 11 chapters) plus orphan reuses in `10-concurrency-and-systems/03-concurrent-hash-map.md` and `11-dsa-in-real-systems/03-redis-internal-encodings.md`. Four collision-resolution strategies share one renderer through a `mode` flag:

- `mode: "chaining"`  — each bucket holds a linked chain of entries. Chains render as a horizontal row of `key=value` cells with small arrows between consecutive entries.
- `mode: "linear"`    — open addressing with linear probing; each bucket holds at most one entry. Collisions advance by `(h + 1) mod m`.
- `mode: "quadratic"` — open addressing with quadratic probing; collisions advance by `(h + i²) mod m`.
- `mode: "double"`    — open addressing with double hashing; collisions advance by `(h + i * h2(k)) mod m`. The second hash function renders next to the primary in a per-payload header.

The bucket array renders as a vertical column of rows (one per bucket index, capped at `spec.capacity`). For probing modes a sidecar probe lane to the right of the column animates the probe-sequence cursor as it advances through `probe.indices`; earlier indices stay visible as a fading trail so the "tried 2 (full), tried 3 (full), landed at 5" narrative is one glance. For chaining mode an optional `chainCursor` triangle hovers above a specific position in a chain — used during the search-along-chain walkthrough.

Per-step `bucketsOverride` is **sparse and cumulative**: each step lists only the buckets whose contents change at THAT step, and the effective state at step N is `spec.buckets` plus the union of all overrides from step 0 through N. This mirrors how source insert/delete demos show "step 3: bucket 2 now holds X" without restating buckets 0-1 / 3-4.

Per-step `highlight` paints a bucket-wide tint behind the entries via a closed `kind` catalog — `target` (blue, "where the op will land"), `miss` (gray dashed, "probed, empty"), `match` (green, "key found"), `collision` (red dashed, "probed, full"). The colour palette lives in CSS so two adjacent payloads using `match` look the same in both.

Tombstones (`tombstone: true` on an entry) render the key with a strikethrough + dashed border — the slot keeps occupying the probe path for future searches but a subsequent insert can reuse it.

> **Source spec**: `docs/migration/widget-specs/hash-table.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/HashTable.scala`
>
> **Highlight kinds**: closed-set, validated at parse time. Unknown values fall back to `target` and log a console warning naming the canonical vocabulary.

## Payload schema (reference card)

```ts
{
  title:             string,
  mode:              "chaining" | "linear" | "quadratic" | "double",
  capacity:          number,                       // bucket count; renders capacity rows
  primaryHashLabel?: string,                       // e.g. "h(k) = k mod 7"
  secondHashLabel?:  string,                       // mode="double" only; e.g. "h2(k) = 1 + (k mod 6)"
  buckets: [{                                      // initial bucket state (step 0 baseline)
    index:   number,                               // 0..capacity-1
    entries: [{ key: string, value?: string, tombstone?: boolean }]
  }],
  steps: [{
    op?:              "insert" | "search" | "delete" | "lookup",
    opKey?:           string,
    opValue?:         string,                      // insert only
    // Sparse bucket overrides — only mention buckets whose entries change at
    // this step. Accumulates over all prior steps + this step.
    bucketsOverride?: [{
      index:   number,
      entries: [{ key: string, value?: string, tombstone?: boolean }]
    }],
    // Probe sequence visualisation (probing modes). The list of bucket indices
    // visited so far, in temporal order. Last index = active cursor; earlier
    // indices = trail. Labels show the formula per probe step (e.g. "h+1²=3").
    probe?:           { indices: number[], labels?: string[] },
    // Chain cursor (chaining mode only) — small triangle over a specific
    // position in a bucket's chain. Used during search-along-chain.
    chainCursor?:     { bucket: number, position: number },
    // Bucket-wide tint. Closed catalog — see Highlight kinds below.
    highlight?:       [{ bucket: number, kind: "target" | "miss" | "match" | "collision" }],
    msg:              string
  }]
}
```

**Required**: `title`, `mode`, `capacity`, `steps` (non-empty), `steps[].msg`.
**Optional**: `primaryHashLabel`, `secondHashLabel`, `buckets` (defaults to empty), every per-step field except `msg`.

**Bucket-override rule**: `bucketsOverride` is sparse — each step only lists buckets whose entries change THIS step. The effective bucket state at step N = `spec.buckets` + accumulated overrides from step 0 through N. A bucket never mentioned in any override keeps its `spec.buckets` content for the whole animation.

**Probe rule**: each step's `probe.indices` is the FULL probe sequence visible at that step (not just the new probe). When a new bucket is probed, add it to the end of `indices`; the renderer slides the cursor to it and adds the previous cursor to the trail.

**Highlight kind rule**: only `target` / `miss` / `match` / `collision`. Unknown values render as `target` and log a console warning listing the canonical vocabulary. Authors don't need to escape — `kind: "Collision"` (capital C) typos surface immediately.

**Mode-specific decorators**: `secondHashLabel` only renders when `mode == "double"`. `chainCursor` only renders when `mode == "chaining"` (probing modes have at most one entry per bucket, so a position cursor is meaningless). Mixing them in is silently ignored.

## Representative payloads

### Payload 1 — minimum (chaining, single insert into an empty table)

The smallest meaningful payload. Empty 5-bucket table; one insert that lands in bucket 2 with no collision. Exercises the renderer's basic bucket row + entry path in `mode: "chaining"` without arrows or chain cursors — the foundation every other payload composes onto.

```d3 widget=hash-table
{
  "title": "Insert ('Alice', 7) into an empty table — chaining, capacity 5",
  "mode": "chaining",
  "capacity": 5,
  "primaryHashLabel": "h(k) = hash(k) mod 5",
  "buckets": [
    {"index": 0, "entries": []},
    {"index": 1, "entries": []},
    {"index": 2, "entries": []},
    {"index": 3, "entries": []},
    {"index": 4, "entries": []}
  ],
  "steps": [
    {
      "op": "insert", "opKey": "Alice", "opValue": "7",
      "msg": "Start: insert ('Alice', 7). Compute h('Alice') = 2."
    },
    {
      "op": "insert", "opKey": "Alice", "opValue": "7",
      "highlight": [{"bucket": 2, "kind": "target"}],
      "msg": "Bucket 2 is empty → append ('Alice', 7) at head of chain."
    },
    {
      "op": "insert", "opKey": "Alice", "opValue": "7",
      "bucketsOverride": [{"index": 2, "entries": [{"key": "Alice", "value": "7"}]}],
      "highlight": [{"bucket": 2, "kind": "match"}],
      "msg": "Done. Bucket 2 → [Alice=7]."
    }
  ]
}
```

### Payload 2 — typical (chaining, search-along-chain via chainCursor)

Chaining insert into an already-occupied bucket — the canonical "walk the chain looking for the key, append at the tail when absent" walkthrough. Exercises the chain cursor (triangle marker advances Bob → Carol → tail), the bucket-override accumulating a third entry, and the highlight transition from `target` (collision detected) to `match` (insert landed).

```d3 widget=hash-table
{
  "title": "Insert ('David', 23) — chaining, bucket 1 already occupied",
  "mode": "chaining",
  "capacity": 5,
  "primaryHashLabel": "h(k) = hash(k) mod 5",
  "buckets": [
    {"index": 0, "entries": []},
    {"index": 1, "entries": [{"key": "Bob", "value": "12"}, {"key": "Carol", "value": "9"}]},
    {"index": 2, "entries": [{"key": "Alice", "value": "7"}]},
    {"index": 3, "entries": []},
    {"index": 4, "entries": [{"key": "Eve", "value": "33"}]}
  ],
  "steps": [
    {
      "op": "insert", "opKey": "David", "opValue": "23",
      "msg": "Compute h('David') = 1."
    },
    {
      "op": "insert", "opKey": "David", "opValue": "23",
      "highlight": [{"bucket": 1, "kind": "target"}],
      "chainCursor": {"bucket": 1, "position": 0},
      "msg": "Bucket 1 occupied — walk chain looking for 'David'. Position 0 = 'Bob' (no match)."
    },
    {
      "op": "insert", "opKey": "David", "opValue": "23",
      "highlight": [{"bucket": 1, "kind": "target"}],
      "chainCursor": {"bucket": 1, "position": 1},
      "msg": "Position 1 = 'Carol' (no match). End of chain — key absent."
    },
    {
      "op": "insert", "opKey": "David", "opValue": "23",
      "bucketsOverride": [{"index": 1, "entries": [
        {"key": "Bob", "value": "12"},
        {"key": "Carol", "value": "9"},
        {"key": "David", "value": "23"}
      ]}],
      "highlight": [{"bucket": 1, "kind": "match"}],
      "msg": "Append ('David', 23) at the chain tail."
    }
  ]
}
```

### Payload 3 — typical (double hashing, probe sequence finds an empty slot)

The canonical double-hashing walkthrough: insert 'X' into a near-full table. h1 lands on an occupied slot; h2 picks the step size and probing advances through buckets 3 → 4 → 5 until an empty bucket appears. Exercises the **probe lane** (trail dots + active cursor), the `collision` highlight (red dashed) → `target` → `match` transition, and the `secondHashLabel` header (only renders for `mode: "double"`).

```d3 widget=hash-table
{
  "title": "Insert 'X' — double hashing, probe sequence finds empty bucket",
  "mode": "double",
  "capacity": 7,
  "primaryHashLabel": "h1(k) = k mod 7",
  "secondHashLabel": "h2(k) = 1 + (k mod 6)",
  "buckets": [
    {"index": 0, "entries": [{"key": "A", "value": "1"}]},
    {"index": 1, "entries": [{"key": "B", "value": "2"}]},
    {"index": 2, "entries": [{"key": "C", "value": "3"}]},
    {"index": 3, "entries": [{"key": "D", "value": "4"}]},
    {"index": 4, "entries": [{"key": "E", "value": "5"}]},
    {"index": 5, "entries": []},
    {"index": 6, "entries": [{"key": "G", "value": "7"}]}
  ],
  "steps": [
    {
      "op": "insert", "opKey": "X", "opValue": "9",
      "probe": {"indices": [3], "labels": ["h1=3"]},
      "highlight": [{"bucket": 3, "kind": "collision"}],
      "msg": "h1('X')=3. Bucket occupied (D) → probe."
    },
    {
      "op": "insert", "opKey": "X", "opValue": "9",
      "probe": {"indices": [3, 4], "labels": ["h1=3", "+h2=4"]},
      "highlight": [{"bucket": 4, "kind": "collision"}],
      "msg": "step = h2('X')=1. Next = (3+1) mod 7 = 4. Occupied (E) → probe."
    },
    {
      "op": "insert", "opKey": "X", "opValue": "9",
      "probe": {"indices": [3, 4, 5], "labels": ["h1=3", "+h2=4", "+h2=5"]},
      "highlight": [{"bucket": 5, "kind": "target"}],
      "msg": "(4+1) mod 7 = 5. Bucket empty → land here."
    },
    {
      "op": "insert", "opKey": "X", "opValue": "9",
      "probe": {"indices": [3, 4, 5], "labels": ["h1=3", "+h2=4", "+h2=5"]},
      "bucketsOverride": [{"index": 5, "entries": [{"key": "X", "value": "9"}]}],
      "highlight": [{"bucket": 5, "kind": "match"}],
      "msg": "Done. X placed at bucket 5 after 2 collisions."
    }
  ]
}
```

### Payload 4 — edge (linear probing, delete leaves a tombstone)

Delete 'B' from a linear-probing chain that includes B at its primary slot. The bucket can't be marked empty without breaking future searches for 'C' (which probed past B during insert), so the slot is left as a **tombstone** — the key + dashed strikethrough render keeps the visual cue that "something used to be here". A subsequent search for 'C' still hits at h=3 because its probe path didn't depend on B's slot in this graph; the third step verifies that.

Exercises the `tombstone: true` flag (strikethrough on the entry text + dashed border on the entry rect) and demonstrates the `op` flip from `delete` to `search` mid-payload.

```d3 widget=hash-table
{
  "title": "Delete 'B' from a linear-probing chain — tombstone preserves probe path",
  "mode": "linear",
  "capacity": 5,
  "primaryHashLabel": "h(k) = k mod 5",
  "buckets": [
    {"index": 0, "entries": []},
    {"index": 1, "entries": [{"key": "A", "value": "1"}]},
    {"index": 2, "entries": [{"key": "B", "value": "2"}]},
    {"index": 3, "entries": [{"key": "C", "value": "3"}]},
    {"index": 4, "entries": []}
  ],
  "steps": [
    {
      "op": "delete", "opKey": "B",
      "probe": {"indices": [2], "labels": ["h=2"]},
      "highlight": [{"bucket": 2, "kind": "match"}],
      "msg": "h('B')=2. Bucket 2 holds B → mark tombstone (not empty) to preserve probe path."
    },
    {
      "op": "delete", "opKey": "B",
      "bucketsOverride": [{"index": 2, "entries": [{"key": "B", "tombstone": true}]}],
      "highlight": [{"bucket": 2, "kind": "match"}],
      "msg": "Tombstone left in place. Future search for 'C' (which probed past B) still finds it."
    },
    {
      "op": "search", "opKey": "C",
      "probe": {"indices": [3], "labels": ["h=3"]},
      "highlight": [{"bucket": 3, "kind": "match"}],
      "msg": "Verify: search 'C' → h=3 → hit immediately."
    }
  ]
}
```

### Payload 5 — large-N (quadratic probing, probe sequence exhausts a full table)

Insert 'Z' into a fully-occupied capacity-5 table; quadratic probing's `(h + i²) mod 5` step sequence revisits bucket 1 on probe 2 and again on probe 3 (the modular squares cycle). Each step's `collision` highlight stacks on the active probed bucket while the trail extends. Final step: cycle detected, sequence exhausts the table — the insert fails.

Exercises the **multi-probe-on-same-bucket** rendering (probe 2 and probe 3 both land on bucket 1; the cursor stagger keeps them visually distinct), the largest probe sequence in the catalog (4 probes), and the `collision` highlight tint stacking across steps.

```d3 widget=hash-table
{
  "title": "Insert 'Z' fails — quadratic probe sequence exhausts a capacity-5 table",
  "mode": "quadratic",
  "capacity": 5,
  "primaryHashLabel": "h(k, i) = (k + i²) mod 5",
  "buckets": [
    {"index": 0, "entries": [{"key": "A", "value": "1"}]},
    {"index": 1, "entries": [{"key": "B", "value": "2"}]},
    {"index": 2, "entries": [{"key": "C", "value": "3"}]},
    {"index": 3, "entries": [{"key": "D", "value": "4"}]},
    {"index": 4, "entries": [{"key": "E", "value": "5"}]}
  ],
  "steps": [
    {
      "op": "insert", "opKey": "Z", "opValue": "9",
      "probe": {"indices": [2], "labels": ["h+0²=2"]},
      "highlight": [{"bucket": 2, "kind": "collision"}],
      "msg": "Probe 0: (2+0) mod 5 = 2 → occupied."
    },
    {
      "op": "insert", "opKey": "Z", "opValue": "9",
      "probe": {"indices": [2, 3], "labels": ["h+0²=2", "h+1²=3"]},
      "highlight": [{"bucket": 3, "kind": "collision"}],
      "msg": "Probe 1: (2+1) mod 5 = 3 → occupied."
    },
    {
      "op": "insert", "opKey": "Z", "opValue": "9",
      "probe": {"indices": [2, 3, 1], "labels": ["h+0²=2", "h+1²=3", "h+2²=1"]},
      "highlight": [{"bucket": 1, "kind": "collision"}],
      "msg": "Probe 2: (2+4) mod 5 = 1 → occupied."
    },
    {
      "op": "insert", "opKey": "Z", "opValue": "9",
      "probe": {"indices": [2, 3, 1, 1], "labels": ["h+0²=2", "h+1²=3", "h+2²=1", "h+3²=1"]},
      "highlight": [{"bucket": 1, "kind": "collision"}],
      "msg": "Probe 3: (2+9) mod 5 = 1 → cycle detected. Sequence exhausts the table."
    }
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload for this widget, target **4–9 widget steps** (the source diagrams in the widget spec average 8–28 frames each → 4–9 widget steps). Compression strategy:

- **Setup frames** (problem statement, hash function chosen, initial table fades in) → one step with `op` set but `bucketsOverride` empty, establishing the starting `primaryHashLabel` + `buckets`.
- **Each probe step** (a single `(h + step) mod m` evaluation) → one widget step. Push the new bucket index onto `probe.indices`, set the matching `highlight` kind (collision vs. target vs. match), describe the formula in `msg`.
- **Walk-the-chain frames** (chaining mode search) → one step per chain position visited. Advance `chainCursor.position` by one; keep the bucket `highlight` as `target` throughout the walk.
- **The actual insert / delete commit** → one step with `bucketsOverride` mutating the affected bucket and `highlight` flipping to `match`.
- **Tombstone setup** → one step with `bucketsOverride` setting `tombstone: true` on the matched key. Optionally one verification step that searches another key whose probe path crossed the tombstoned slot.
- **Final 1-2 source frames** ("done", "summary") → fold into the commit step's `msg` rather than a separate widget step.

Example: the source linear-probing delete + verify-search composite (`03-linear-probing/06-delete-operation-in-linear-probing.md:13`, 12 frames) compresses to 3 widget steps — one per `op` semantic boundary (match-on-probe, mark-tombstone, verify-search). Payload 4 above is exactly this compression applied to a small example.

For **chaining mode insert-into-occupied-bucket** (source 11–12 frames), compress to one cursor-walk step per chain position visited plus one commit step. Payload 2 follows this pattern at chain length 2 → 3.

For **probing modes that exhaust the table** (source 18–28 frames for the cycle-detection case), compress to one probe step per `(h + i² | i * h2) mod m` evaluation. Don't try to fold consecutive probes into a single step — the pedagogical climax is each probe revealing a new collision; merging hides the sequence.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-hash-table` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm Payload 1's empty table renders 5 stacked bucket rows with index labels 0..4; on step 2 the bucket-2 highlight tints blue; on step 3 the `Alice=7` entry fades in inside bucket 2 with the highlight flipping to green (`match`).
3. Confirm Payload 2's chain cursor (downward triangle) advances over `Bob` at step 2 and `Carol` at step 3; on step 4 the `David=23` entry fades in to the right of `Carol` and the new arrow appears between `Carol` and `David`.
4. Confirm Payload 3's double-hashing header shows two formula lines (`h1` and `h2`); the probe trail dots accumulate on the right of buckets 3, 4 as the cursor advances to bucket 5; the final commit flips bucket 5's highlight from `target` (blue) to `match` (green) and the `X=9` entry fades in.
5. Confirm Payload 4's step-2 entry renders the `B=2` key with a strikethrough text and a dashed border (the tombstone); step 3's verify-search advances the probe cursor to bucket 3 and the `C=3` entry stays solid (not tombstoned).
6. Confirm Payload 5's probe lane shows the staggered cursor — on the final step, the cursor sits to the right of the prior probe at bucket 1, visually demonstrating the cycle without overlap.
7. Confirm no `.d3-widget__error` divs render in the page.
8. Confirm devtools console is clean: no widget exceptions; no highlight-kind canonical-vocabulary warnings on these payloads. Highlight kinds used: `target` / `miss` / `match` / `collision` — all canonical.

If any payload fails, fix and re-verify before committing.
