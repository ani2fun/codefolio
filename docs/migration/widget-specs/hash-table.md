# Widget Spec — `hash-table`

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `hash-table` and Arc 3 chapter payload writing.

## 1. Purpose

Animate the bucket-by-bucket life of a hash table across the four
collision-resolution strategies Phase 3 teaches: **separate chaining**
(buckets hold a linked chain), **linear probing** (one slot per index,
linear collision search), **quadratic probing** (same, quadratic step), and
**double hashing** (same, second hash function). One widget covers all four
via a `mode` flag. Each step renders: the bucket array, per-bucket
contents (single slot or chain), the active probe sequence with a moving
cursor, and the per-step explanatory message.

## 2. Source-diagram inventory

Source root: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/4.hash-table/`
(11 chapter dirs, 57 interactive diagrams). Grouped by mode:

### Introductory (mode: any — usually `chaining` for the demo)

- `01-introduction-to-hash-tables/01-understanding-the-problem.md:17`
  — "Searching for the roll number of a student by their name" (6 frames → 4 steps)
- `01-introduction-to-hash-tables/07-overview-of-supported-operations.md:9`
  — "Insert a key value mapping into the hash table" (7 frames → 4 steps)
- `01-introduction-to-hash-tables/07-overview-of-supported-operations.md:29`
  — "Search for key in the hash table" (6 frames → 4 steps)
- `01-introduction-to-hash-tables/07-overview-of-supported-operations.md:47`
  — "Delete a key from the hash table" (7 frames → 4 steps)

### Mode: `chaining`

- `02-separate-chaining/03-implementing-the-hash-table-class.md:282`
  — "Execution of code using an instance of the hash table class" (19 frames → 8 steps)
- `02-separate-chaining/04-search-operation-in-separate-chaining.md:11`
  — "Search for the given key" (9 frames → 5 steps)
- `02-separate-chaining/05-insert-operation-in-separate-chaining.md:13`
  — "Insert where key is present" (11 frames → 6 steps)
- `02-separate-chaining/05-insert-operation-in-separate-chaining.md:49`
  — "Insert where key is not present" (12 frames → 6 steps)
- `02-separate-chaining/06-delete-operation-in-separate-chaining.md:13`
  — "Delete where key is present" (11 frames → 6 steps)
- `02-separate-chaining/06-delete-operation-in-separate-chaining.md:47`
  — "Delete where key is not present" (8 frames → 5 steps)

### Mode: `linear`

- `03-linear-probing/03-implementing-the-hash-table-class.md:348`
  — "Execution of code using an instance" (24 frames → 9 steps)
- `03-linear-probing/04-search-operation-in-linear-probing.md:15`
  — "Search — key present" (10 frames → 5 steps)
- `03-linear-probing/04-search-operation-in-linear-probing.md:47`
  — "Search — not present, table not full" (14 frames → 6 steps)
- `03-linear-probing/04-search-operation-in-linear-probing.md:87`
  — "Search — not present, table full" (18 frames → 7 steps)
- `03-linear-probing/05-insert-operation-in-linear-probing.md:13`
  — "Insert — key present" (13 frames → 6 steps)
- `03-linear-probing/05-insert-operation-in-linear-probing.md:51`
  — "Insert — not present, table not full" (14 frames → 6 steps)
- `03-linear-probing/05-insert-operation-in-linear-probing.md:91`
  — "Insert — not present, table full" (18 frames → 7 steps)
- `03-linear-probing/06-delete-operation-in-linear-probing.md:13`
  — "Delete — key present" (12 frames → 5 steps)
- `03-linear-probing/06-delete-operation-in-linear-probing.md:49`
  — "Delete — not present, table not full" (15 frames → 6 steps)
- `03-linear-probing/06-delete-operation-in-linear-probing.md:91`
  — "Delete — not present, table full" (18 frames → 7 steps)

### Mode: `quadratic`

- `04-quadratic-probing/03-implementing-the-hash-table-class.md:368`
  — "Execution of code using an instance" (20 frames → 8 steps)
- `04-quadratic-probing/04-search-operation-in-quadratic-probing.md:15` —
  "Search — present" (11 frames → 5 steps)
- `04-quadratic-probing/04-search-operation-in-quadratic-probing.md:47` —
  "Search — not present, sequence not full" (12 frames → 6 steps)
- `04-quadratic-probing/04-search-operation-in-quadratic-probing.md:81` —
  "Search — not present, sequence full" (19 frames → 7 steps)
- `04-quadratic-probing/05-insert-operation-in-quadratic-probing.md:13` —
  "Insert — present" (14 frames → 6 steps)
- `04-quadratic-probing/05-insert-operation-in-quadratic-probing.md:53` —
  "Insert — not present, sequence not full" (15 frames → 6 steps)
- `04-quadratic-probing/05-insert-operation-in-quadratic-probing.md:95` —
  "Insert — not present, sequence full" (19 frames → 7 steps)
- `04-quadratic-probing/06-delete-operation-in-quadratic-probing.md:13` —
  "Delete — present" (13 frames → 6 steps)
- `04-quadratic-probing/06-delete-operation-in-quadratic-probing.md:51` —
  "Delete — not present, sequence not full" (13 frames → 6 steps)
- `04-quadratic-probing/06-delete-operation-in-quadratic-probing.md:89` —
  "Delete — not present, sequence full" (19 frames → 7 steps)

### Mode: `double`

- `05-double-hashing/03-implementing-the-hash-table-class.md:365` —
  "Execution of code using an instance" (21 frames → 8 steps)
- `05-double-hashing/04-search-operation-in-double-hashing.md:15` —
  "Search — present" (16 frames → 6 steps)
- `05-double-hashing/04-search-operation-in-double-hashing.md:58` —
  "Search — not present, sequence not full" (17 frames → 7 steps)
- `05-double-hashing/04-search-operation-in-double-hashing.md:103` —
  "Search — not present, sequence full" (28 frames → 9 steps)
- `05-double-hashing/05-insert-operation-in-double-hashing.md:13` —
  "Insert — present" (19 frames → 7 steps)
- `05-double-hashing/05-insert-operation-in-double-hashing.md:64` —
  "Insert — not present, sequence not full" (19 frames → 7 steps)
- `05-double-hashing/05-insert-operation-in-double-hashing.md:117` —
  "Insert — not present, sequence full" (28 frames → 9 steps)
- `05-double-hashing/06-delete-operation-in-double-hashing.md:13` —
  "Delete — present" (18 frames → 7 steps)
- `05-double-hashing/06-delete-operation-in-double-hashing.md:62` —
  "Delete — not present, sequence not full" (18 frames → 7 steps)
- `05-double-hashing/06-delete-operation-in-double-hashing.md:113` —
  "Delete — not present, sequence full" (28 frames → 9 steps)

### Pattern chapters (mode: `chaining`, used as a generic map view)

- `06-pattern-counting/01-understanding-the-counting-pattern.md:15` —
  "Counting technique using a hash map" (12 frames → 6 steps)
- `06-pattern-counting/02-identifying-the-counting-pattern.md:23` —
  "Brute force first non-repeating character" (22 frames → 8 steps)
- `06-pattern-counting/02-identifying-the-counting-pattern.md:253` —
  "Create frequency map using counting technique" (12 frames → 6 steps)
- `06-pattern-counting/02-identifying-the-counting-pattern.md:281` —
  "Use the frequency map to find the first non-repeating character" (5 frames → 4 steps)
- `07-pattern-pattern-generation/01-understanding-the-pattern-generation-pattern.md:29`
  — "Generate a pattern from the input array" (25 frames → 9 steps)
- `07-pattern-pattern-generation/02-identifying-the-pattern-generation-pattern.md:29`
  — "Generate a pattern for string s" (13 frames → 6 steps)
- `07-pattern-pattern-generation/02-identifying-the-pattern-generation-pattern.md:59`
  — "Generate a pattern for string t" (13 frames → 6 steps)
- `08-pattern-fixed-sized-sliding-window/01-understanding-the-fixed-sized-sliding-window-pattern.md:21`
  — "Create a frequency map" (2 frames → 1 step)
- `08-pattern-fixed-sized-sliding-window/01-understanding-the-fixed-sized-sliding-window-pattern.md:35`
  — "Fixed sized sliding window technique" (35 frames → 10 steps)
- `08-pattern-fixed-sized-sliding-window/02-identifying-the-fixed-sized-sliding-window-pattern.md:25`
  — "Find duplicates in any window of size k" (32 frames → 10 steps)
- `08-pattern-fixed-sized-sliding-window/02-identifying-the-fixed-sized-sliding-window-pattern.md:237`
  — same, optimised (22 frames → 8 steps)
- `09-pattern-variable-sized-sliding-window/01-understanding-the-variable-sized-sliding-window-pattern.md:5`
  — "Remember occurrences of items in all windows of an array" (37 frames → 10 steps)
- `09-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md:23`
  — "Find longest substring with unique chars" (33 frames → 10 steps)
- `09-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md:273`
  — same, 4 frames → 3 steps
- `09-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md:289`
  — same, 24 frames → 8 steps
- `10-pattern-prefix-sum/01-understanding-the-prefix-sum-pattern.md:35` —
  "Prefix sum maps prefix sums to indices" (17 frames → 7 steps)
- `10-pattern-prefix-sum/02-identifying-the-prefix-sum-pattern.md:35` —
  "Find the number of zero sum subarrays" (21 frames → 8 steps)

**Compression target**: 57 source diagrams → ~50 widget instances after
1-step compressions of single-frame demos. Average compression ~50% on the
N→steps axis (e.g., 28-frame deletes → 7 steps).

## 3. Destination chapter usage

All under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/07-hash-table/`:

- `01-introduction-to-hash-tables.md` — 4 instances, mostly `mode:"chaining"` for the conceptual frame.
- `02-separate-chaining.md` — 6 instances of `mode:"chaining"` (POC; see §10).
- `03-linear-probing.md` — 10 instances of `mode:"linear"`.
- `04-quadratic-probing.md` — 10 instances of `mode:"quadratic"`.
- `05-double-hashing.md` — 10 instances of `mode:"double"`.
- `06-pattern-counting.md` — 4 instances of `mode:"chaining"` (used as generic map).
- `07-pattern-pattern-generation.md` — 3 instances.
- `08-pattern-fixed-sized-sliding-window.md` — 4 instances.
- `09-pattern-variable-sized-sliding-window.md` — 4 instances.
- `10-pattern-prefix-sum.md` — 2 instances.
- `11-design.md` — 0 source diagrams (prose-only design exercise).

### Orphan reuse

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/10-concurrency-and-systems/03-concurrent-hash-map.md`
  — uses `mode:"chaining"` with a per-bucket lock annotation; one or two
  instances illustrating segment locking.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/11-dsa-in-real-systems/03-redis-internal-encodings.md`
  — uses `mode:"chaining"` to show Redis dict + rehashing; one or two
  instances.

## 4. Payload schema sketch

```ts
{
  title: string,
  mode: "chaining" | "linear" | "quadratic" | "double",
  capacity: number,                          // bucket count; renders capacity rows
  primaryHashLabel?: string,                  // e.g. "h(k) = k mod 7"
  secondHashLabel?: string,                   // mode="double" only; e.g. "h2(k) = 1 + (k mod 6)"
  buckets: Array<{
    index: number,                            // 0..capacity-1
    // For mode="chaining": array of entries that form the linked chain.
    // For probing modes: single entry or empty.
    entries: Array<{
      key: string,
      value?: string,                          // optional; entries may be just keys for set/frequency demos
      tombstone?: boolean                      // for delete-then-search demos in probing modes
    }>
  }>,
  steps: Array<{
    // Per-step bucket overrides — same shape as top-level `buckets[i].entries`.
    // Absent means "use the prior step / spec-level state".
    bucketsOverride?: Array<{
      index: number,
      entries: Array<{ key, value?, tombstone? }>
    }>,
    // Active operation banner shown above the table.
    op?: "insert" | "search" | "delete" | "lookup",
    opKey?: string,
    opValue?: string,                          // insert only
    // Probe sequence visualisation. The list of bucket indices the
    // algorithm visited so far, in order. Last index is the active cursor;
    // earlier indices render as a fading trail.
    probe?: {
      indices: number[],
      // Per-probe-step formula label (e.g. "h(k)+1²=4" for quadratic).
      labels?: string[]
    },
    // For chaining mode: which chain node (by 0-indexed position in
    // bucket's entries) is currently inspected. Renders a triangle marker.
    chainCursor?: { bucket: number, position: number },
    // Bucket-level highlight tint. Used for "this is where insert lands"
    // or "deletion target" annotation. Empty list = no tinting.
    highlight?: Array<{ bucket: number, kind: "target" | "miss" | "match" | "collision" }>,
    msg: string
  }>
}
```

## 5. POC payloads

### 5.1 Minimal — empty 5-bucket table with one insert

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
      "op": "insert",
      "opKey": "Alice",
      "opValue": "7",
      "msg": "Start: insert ('Alice', 7). Compute h('Alice') = 2."
    },
    {
      "op": "insert",
      "opKey": "Alice",
      "opValue": "7",
      "highlight": [{"bucket": 2, "kind": "target"}],
      "msg": "Bucket 2 is empty → append ('Alice', 7) at head of chain."
    },
    {
      "op": "insert",
      "opKey": "Alice",
      "opValue": "7",
      "bucketsOverride": [{"index": 2, "entries": [{"key": "Alice", "value": "7"}]}],
      "msg": "Done. Table now: bucket 2 → [Alice=7]."
    }
  ]
}
```

### 5.2 Typical — chaining insert into an occupied bucket (POC chapter)

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
    {"op": "insert", "opKey": "David", "opValue": "23", "msg": "Compute h('David') = 1."},
    {"op": "insert", "opKey": "David", "opValue": "23", "highlight": [{"bucket": 1, "kind": "target"}], "chainCursor": {"bucket": 1, "position": 0}, "msg": "Bucket 1 occupied — walk chain looking for 'David'. Position 0 = 'Bob' (no match)."},
    {"op": "insert", "opKey": "David", "opValue": "23", "highlight": [{"bucket": 1, "kind": "target"}], "chainCursor": {"bucket": 1, "position": 1}, "msg": "Position 1 = 'Carol' (no match). End of chain — key absent."},
    {"op": "insert", "opKey": "David", "opValue": "23", "bucketsOverride": [{"index": 1, "entries": [{"key": "Bob", "value": "12"}, {"key": "Carol", "value": "9"}, {"key": "David", "value": "23"}]}], "highlight": [{"bucket": 1, "kind": "match"}], "msg": "Append ('David', 23) at the chain tail."}
  ]
}
```

### 5.3 Large-N — double hashing with full probe sequence

```d3 widget=hash-table
{
  "title": "Insert 'X' — double hashing, probe sequence exercises the whole table",
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
    {"op": "insert", "opKey": "X", "opValue": "9", "probe": {"indices": [3], "labels": ["h1=3"]}, "highlight": [{"bucket": 3, "kind": "collision"}], "msg": "h1('X')=3. Bucket occupied (D) → probe."},
    {"op": "insert", "opKey": "X", "opValue": "9", "probe": {"indices": [3, 4], "labels": ["h1=3", "+h2=4"]}, "highlight": [{"bucket": 4, "kind": "collision"}], "msg": "step = h2('X')=1. Next = (3+1) mod 7 = 4. Occupied (E) → probe."},
    {"op": "insert", "opKey": "X", "opValue": "9", "probe": {"indices": [3, 4, 5], "labels": ["h1=3", "+h2=4", "+h2=5"]}, "highlight": [{"bucket": 5, "kind": "target"}], "msg": "(4+1) mod 7 = 5. Bucket empty → land here."},
    {"op": "insert", "opKey": "X", "opValue": "9", "bucketsOverride": [{"index": 5, "entries": [{"key": "X", "value": "9"}]}], "highlight": [{"bucket": 5, "kind": "match"}], "msg": "Done. X placed at bucket 5 after 2 collisions."}
  ]
}
```

### 5.4 Edge case — delete with tombstone (linear probing)

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
    {"op": "delete", "opKey": "B", "probe": {"indices": [2], "labels": ["h=2"]}, "highlight": [{"bucket": 2, "kind": "match"}], "msg": "h('B')=2. Bucket 2 holds B → mark tombstone (not empty) to preserve probe path."},
    {"op": "delete", "opKey": "B", "bucketsOverride": [{"index": 2, "entries": [{"key": "B", "tombstone": true}]}], "highlight": [{"bucket": 2, "kind": "match"}], "msg": "Tombstone left in place. Future search for 'C' (which probed past B) still finds it."},
    {"op": "search", "opKey": "C", "probe": {"indices": [3], "labels": ["h=3"]}, "highlight": [{"bucket": 3, "kind": "match"}], "msg": "Verify: search 'C' → h=3 → hit immediately."}
  ]
}
```

### 5.5 Quadratic probing — full probe sequence exhausted

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
    {"op": "insert", "opKey": "Z", "opValue": "9", "probe": {"indices": [2], "labels": ["h+0²=2"]}, "highlight": [{"bucket": 2, "kind": "collision"}], "msg": "Probe 0: (2+0) mod 5 = 2 → occupied."},
    {"op": "insert", "opKey": "Z", "opValue": "9", "probe": {"indices": [2, 3], "labels": ["h+0²=2", "h+1²=3"]}, "highlight": [{"bucket": 3, "kind": "collision"}], "msg": "Probe 1: (2+1) mod 5 = 3 → occupied."},
    {"op": "insert", "opKey": "Z", "opValue": "9", "probe": {"indices": [2, 3, 1], "labels": ["h+0²=2", "h+1²=3", "h+2²=1"]}, "highlight": [{"bucket": 1, "kind": "collision"}], "msg": "Probe 2: (2+4) mod 5 = 1 → occupied."},
    {"op": "insert", "opKey": "Z", "opValue": "9", "probe": {"indices": [2, 3, 1, 1], "labels": ["h+0²=2", "h+1²=3", "h+2²=1", "h+3²=1"]}, "highlight": [{"bucket": 1, "kind": "collision"}], "msg": "Probe 3: (2+9) mod 5 = 1 → cycle. Sequence exhausts the table."}
  ]
}
```

## 6. Closest existing widget to mimic

Mimic
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala`
(1,552 LOC) for the **chaining mode**: each bucket renders an inline
horizontal chain that visually echoes `LinkedList`'s row layout (smaller
node size — chains usually have 1-3 entries). For the **probing modes**
mimic
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
(784 LOC) — the bucket array is a single vertical column of cells, the
probe cursor moves like the `i`/`j` markers in array traversal.

## 7. D3 selections plan

- `g.hash-table__bucket` keyed by `bucket.index` (1..capacity). Renders the
  bucket index label, the bucket box (always present), and the chain or
  single entry inside.
- Inside each bucket, `g.hash-table__entry` keyed by `entry.key` for
  chaining mode (entries persist across step rebinds, slide to position on
  insertion). Probing modes use a single nested `g.hash-table__slot`.
- `path.hash-table__chain-link` keyed by `${from}->${to}` for the chain
  arrows in chaining mode. Auto-derived from the entries' ordering.
- `g.hash-table__probe-marker` keyed by step (one marker per step, not
  per-probe-index — the marker slides along the probe sequence on play).
  Trail behind the marker renders as `circle.hash-table__probe-trail`
  keyed by `${stepIdx}-${probeIdx}`.
- `rect.hash-table__highlight` keyed by `bucket.index` and updated each
  step. Tint colour driven by `highlight[i].kind`.

## 8. Shared abstractions

- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
  for parsing.
- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`
  for step controls.
- New shared utility candidate: a `ProbeTrail` helper for the trail-fade
  rendering pattern, since `stack-queue` (priority mode) and `heap-tree`
  (extract-min) will both want similar trailing cursor visuals. **Defer
  to Arc 2** — extract once two widgets are written and the duplication is
  visible.

## 9. Estimated build session count

**1 session** — one Scala module, one CSS BEM block, one dispatcher case,
one demo chapter showing all four modes side by side. The chaining +
probing rendering branches share the bucket-array scaffolding; the per-
mode entry layout is the only diverging logic.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/07-hash-table/02-separate-chaining.md`
— smallest mode-specific chapter (6 source diagrams, all in the 8-12
frame range). Lands `mode:"chaining"` first; once it renders cleanly the
probing modes follow against the same widget without further code
changes.
