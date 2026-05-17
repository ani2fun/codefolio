# Widget Spec — `lsm-tree`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise a Log-Structured Merge tree end-to-end. The widget renders
**five spatial regions** simultaneously:

1. **WAL strip** (top-left). Sequential append-only log; each write
   appends an entry. Visual: a horizontal stack of small entries with
   a moving "tail" pointer; entries fade out when their backing
   memtable is flushed.
2. **Memtable + immutable memtables** (top-centre). The active memtable
   is a small skip-list-style box that accepts writes. When it fills,
   it becomes immutable (greyed out, sidelined), and a fresh memtable
   appears.
3. **Leveled SSTables** (centre, the big region). `L0..LN` rendered as
   horizontal rows. Each SSTable is a coloured rectangle proportional
   to its key range and size; each carries a Bloom-filter badge.
4. **Compaction overlay** (animation only). When a compaction step
   runs, the source SSTables visually merge into a target SSTable on
   the next level — old SSTables fade out, new SSTable fades in,
   tombstones drop.
5. **Read-path overlay** (animation only). On a `read` step, the query
   key sweeps through memtable → immutable memtable → L0 SSTables → L1
   SSTables → … with each SSTable's Bloom filter consulted first.
   Bloom-filter hits draw an arrow into the SSTable's index; Bloom
   misses skip the SSTable entirely (grey out). The first data hit
   wins and the rest of the levels are not consulted.

The widget animates the **write path** (memtable → SSTable flush →
compaction) and the **read path** (memtable → L0 → L1 → … with Bloom
filter checks) as two separate operation kinds the author drives via
the steps array.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

No source coverage. Payload milestones come from
`11-dsa-in-real-systems/05-lsm-trees-rocksdb-cassandra.md`:
the three-layer write description in § Memtable + WAL + SSTables, the
leveled-vs-tiered compaction explanation in § Compaction strategies,
the read-path step list in § Reads with multiple SSTables (steps 1–3
exactly map to widget steps), the tombstone discussion in § Edge cases.

## 3. Destination chapter usage

- **Primary owner.** `11-dsa-in-real-systems/05-lsm-trees-rocksdb-cassandra.md`
  — four instances:
  - § Memtable + WAL + SSTables: a 5-step write-path animation showing
    WAL append, memtable insert, threshold trigger, flush to L0 SSTable,
    WAL truncation.
  - § Compaction strategies / leveled: a 3-step leveled compaction
    showing L0 → L1 merge with a target SSTable splitting L1 by key
    range.
  - § Reads with multiple SSTables: a read-path animation showing
    memtable miss → L0 Bloom-filter miss → L0 Bloom-filter hit (with
    index lookup) → return.
  - § Edge cases / Tombstones: a delete + compaction sequence showing
    a tombstone propagating through levels and the moment it drops.
- **No reuse expected.** LSM is specialised to this chapter; other
  storage chapters use `BTreeWalker`.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  levels: number,            // L0..L(levels-1); 3–5 typical for didactic
  // The static SSTable layout per level at step 0. Each SSTable has a
  // key range, an entry count (drives box height), and an optional
  // Bloom filter list of "keys it contains" (for read-path Bloom
  // hit/miss demos).
  initialSstables: Array<{
    id:    string,
    level: number,            // 0 <= level < levels
    keyRange: [number, number],   // [lo, hi] — drives left-right placement
    entryCount: number,           // drives height
    bloomKeys?: Array<number>     // keys the Bloom filter reports as possibly-present
                                  // omit to skip Bloom-filter rendering
  }>,
  // Memtable initial state. Pure-memory; rendered as a stack of
  // key=value pairs.
  initialMemtable?: Array<{ key: number, value: string, tombstone?: boolean }>,
  memtableThreshold: number,   // size at which memtable becomes immutable; 4–6 for didactic
  steps: Array<{
    op:    "write"
         | "delete"
         | "flush"
         | "compact"
         | "read"
         | "noop",
    msg:   string,
    // For write: insert into memtable + WAL append.
    write?: { key: number, value: string },
    // For delete: insert tombstone into memtable.
    delete?: { key: number },
    // For flush: declare which immutable memtable flushes and the
    // L0 SSTable it produces. The widget transitions the memtable
    // box into an L0 box.
    flush?: {
      memtableSnapshot: Array<{ key: number, value: string, tombstone?: boolean }>,
      newSstableId: string
    },
    // For compact: declare the source SSTables (any level) and the
    // target SSTables (next level). The widget fades sources out and
    // targets in.
    compact?: {
      sourceIds: Array<string>,
      targets:   Array<{
        id: string,
        level: number,
        keyRange: [number, number],
        entryCount: number,
        bloomKeys?: Array<number>
      }>,
      // Tombstones that drop during this compaction. Renders a small
      // "💀 dropped" annotation in the compaction overlay.
      droppedTombstones?: Array<number>
    },
    // For read: query a key. The widget animates the read path through
    // memtable → immutable → L0..LN. Each path step is implicit; the
    // widget computes Bloom-filter hits/misses from each SSTable's
    // bloomKeys field. The author declares which level + sstable id
    // ultimately satisfies the read (or "not_found").
    read?: {
      key: number,
      result: "hit-memtable" | "hit-sstable" | "not-found",
      sstableId?: string             // when result == "hit-sstable"
    }
  }>,
  sections?: Array<{ name: string, startIdx: number }>
}
```

Validation:

- Every SSTable id must be unique across initial + all step `flush.newSstableId` +
  `compact.targets[].id`.
- `compact.sourceIds` must all exist at compaction time (i.e., were
  declared earlier and not already compacted away).
- `read.result == "hit-sstable"` requires `sstableId` to be present at
  read time *and* to contain the key (the widget asserts the key falls
  in the SSTable's keyRange and is in bloomKeys when bloomKeys is set).
- `delete.key` produces a tombstone in the memtable; later reads of
  that key must return `not-found` until a write reinserts it.

## 5. POC payloads

### 5a. Write path — WAL → memtable → L0 SSTable flush

```d3 widget=lsm-tree
{
  "title": "Write path — WAL append, memtable insert, flush to L0",
  "levels": 3,
  "initialSstables": [],
  "memtableThreshold": 4,
  "steps": [
    { "op": "write", "msg": "Write k=10. Append to WAL; insert into memtable.",
      "write": { "key": 10, "value": "a" } },
    { "op": "write", "msg": "Write k=20. Memtable size = 2.",
      "write": { "key": 20, "value": "b" } },
    { "op": "write", "msg": "Write k=30. Memtable size = 3.",
      "write": { "key": 30, "value": "c" } },
    { "op": "write", "msg": "Write k=40. Memtable size = 4 — threshold reached.",
      "write": { "key": 40, "value": "d" } },
    { "op": "flush", "msg": "Memtable becomes immutable; flushes to L0 as SSTable S1; WAL truncated.",
      "flush": {
        "memtableSnapshot": [
          { "key": 10, "value": "a" },
          { "key": 20, "value": "b" },
          { "key": 30, "value": "c" },
          { "key": 40, "value": "d" }
        ],
        "newSstableId": "S1"
      }
    }
  ]
}
```

### 5b. Read path — memtable miss, L0 Bloom miss, L1 Bloom hit + index lookup

```d3 widget=lsm-tree
{
  "title": "Read path — memtable → L0 → L1 with Bloom filter checks",
  "levels": 3,
  "initialSstables": [
    { "id": "S1", "level": 0, "keyRange": [50, 90],  "entryCount": 4, "bloomKeys": [50, 60, 80, 90] },
    { "id": "S2", "level": 1, "keyRange": [10, 45],  "entryCount": 4, "bloomKeys": [10, 20, 30, 40] },
    { "id": "S3", "level": 1, "keyRange": [55, 95],  "entryCount": 4, "bloomKeys": [55, 65, 75, 85] }
  ],
  "initialMemtable": [
    { "key": 100, "value": "z" }
  ],
  "memtableThreshold": 4,
  "steps": [
    { "op": "read", "msg": "read(20). Check memtable — only has 100. Miss.",
      "read": { "key": 20, "result": "hit-sstable", "sstableId": "S2" } },
    { "op": "read", "msg": "Check L0 SSTable S1 — Bloom filter says 'not in {50,60,80,90}'. Skip — no disk read.",
      "read": { "key": 20, "result": "hit-sstable", "sstableId": "S2" } },
    { "op": "read", "msg": "Check L1 SSTable S2 — Bloom filter says 'maybe in {10,20,30,40}'. Index lookup confirms. Return.",
      "read": { "key": 20, "result": "hit-sstable", "sstableId": "S2" } }
  ]
}
```

### 5c. Leveled compaction — L0 SSTable merges into overlapping L1 SSTables

```d3 widget=lsm-tree
{
  "title": "Leveled compaction — S1 (L0) merges with S2 (L1); produces S4 + S5",
  "levels": 3,
  "initialSstables": [
    { "id": "S1", "level": 0, "keyRange": [15, 35],  "entryCount": 4, "bloomKeys": [15, 20, 25, 35] },
    { "id": "S2", "level": 1, "keyRange": [10, 30],  "entryCount": 5, "bloomKeys": [10, 14, 22, 27, 30] }
  ],
  "memtableThreshold": 4,
  "steps": [
    { "op": "noop", "msg": "S1 (L0) overlaps S2 (L1). Compaction merges them into L1." },
    { "op": "compact", "msg": "Stream-merge S1 + S2 by key; produce two L1 SSTables S4 + S5 (split for size).",
      "compact": {
        "sourceIds": ["S1", "S2"],
        "targets": [
          { "id": "S4", "level": 1, "keyRange": [10, 22], "entryCount": 5, "bloomKeys": [10, 14, 15, 20, 22] },
          { "id": "S5", "level": 1, "keyRange": [25, 35], "entryCount": 4, "bloomKeys": [25, 27, 30, 35] }
        ]
      }
    }
  ]
}
```

### 5d. Tombstone propagation through compaction

```d3 widget=lsm-tree
{
  "title": "Delete k=20; tombstone migrates through compactions and is finally dropped",
  "levels": 3,
  "initialSstables": [
    { "id": "S0", "level": 1, "keyRange": [10, 40], "entryCount": 4, "bloomKeys": [10, 20, 30, 40] }
  ],
  "memtableThreshold": 4,
  "steps": [
    { "op": "delete", "msg": "Delete k=20 — writes a tombstone to the memtable.",
      "delete": { "key": 20 } },
    { "op": "write", "msg": "Write fillers to trigger a flush.",
      "write": { "key": 50, "value": "e" } },
    { "op": "write", "msg": "Write k=60.",
      "write": { "key": 60, "value": "f" } },
    { "op": "write", "msg": "Write k=70. Memtable full.",
      "write": { "key": 70, "value": "g" } },
    { "op": "flush", "msg": "Flush — produces L0 SSTable S1 containing the tombstone for k=20 plus filler writes.",
      "flush": {
        "memtableSnapshot": [
          { "key": 20, "tombstone": true, "value": "(deleted)" },
          { "key": 50, "value": "e" },
          { "key": 60, "value": "f" },
          { "key": 70, "value": "g" }
        ],
        "newSstableId": "S1"
      }
    },
    { "op": "compact", "msg": "Compaction merges S1 (L0) + S0 (L1). The tombstone for k=20 cancels the live k=20 in S0 — both drop.",
      "compact": {
        "sourceIds": ["S0", "S1"],
        "targets": [
          { "id": "S2", "level": 1, "keyRange": [10, 70], "entryCount": 6, "bloomKeys": [10, 30, 40, 50, 60, 70] }
        ],
        "droppedTombstones": [20]
      }
    }
  ]
}
```

### 5e. Bloom-filter false positive — wasted disk seek

```d3 widget=lsm-tree
{
  "title": "Bloom false positive — query says 'maybe', index lookup says 'no'",
  "levels": 2,
  "initialSstables": [
    { "id": "S1", "level": 1, "keyRange": [10, 90], "entryCount": 5, "bloomKeys": [10, 20, 30, 50, 99] }
  ],
  "memtableThreshold": 4,
  "steps": [
    { "op": "read", "msg": "read(99). Memtable miss.",
      "read": { "key": 99, "result": "not-found" } },
    { "op": "read", "msg": "S1 Bloom filter has 99 — index lookup proceeds (wasted seek).",
      "read": { "key": 99, "result": "not-found" } },
    { "op": "read", "msg": "Index says 99 outside actual range [10..90]. Return not-found. The Bloom hit was a false positive.",
      "read": { "key": 99, "result": "not-found" } }
  ]
}
```

## 6. Closest existing widget to mimic

- **`BTreeWalker.scala`** is the closest *system-design* widget in
  spirit — both show a hierarchical storage layout with bounded
  fan-out. Reuse: the slider-style SVG-string-injection mount pattern
  is fine for the static layout regions; transitions need D3 keyed
  joins (see § 7).
- **`RaftAnimator.scala`** is the closest *stepped scripted-scenario*
  widget. Reuse: the multi-step caption-driven pattern; the
  message-in-flight arrow analogue maps to the read-path overlay
  arrows here.
- **`ArrayTraversal.scala`**'s row-of-cells pattern is reused for the
  memtable + WAL strip rendering.
- **`linked-list`**'s ADR-0016 canon pattern: the SSTable "kind" badges
  (Bloom present, tombstone count, level number) need canonical
  colours — emerald for Bloom hit, rose for Bloom miss, slate for
  "skipped without check".
- **`CacheStampedeSimulator`** is not a fit — no slider; the LSM
  payload is fully scripted.

## 7. D3 selections plan

- Host `<svg>` with five logical regions; total height
  `~480px`, width `~720px`:
  - **WAL strip** (top-left, `~240×60`). Horizontal stack of entries;
    keyed by entry index. Append-only; entries fade out on flush of
    their backing memtable.
  - **Memtable + immutable strip** (top-centre, `~240×60`). Active
    memtable as a vertical stack of `(key, value)` pairs. On
    threshold-trigger, a 350 ms tween moves the memtable to the
    immutable lane and an empty new memtable appears.
  - **SSTable level rows** (centre, `levels × 60px`). Each level a
    horizontal lane; SSTables are `<g>` keyed by `sstableId`. Each
    SSTable is a `<rect>` with width proportional to its key-range
    span (mapped via D3 linear scale over the union of all key ranges)
    and a `<text>` showing `[lo..hi]`. A small Bloom-filter badge on
    each SSTable; tombstone count when > 0.
  - **Compaction overlay** (animation only; transient). On a `compact`
    step, source SSTables fade out (`opacity 1 → 0.2`); a connecting
    "stream merge" arrow appears; target SSTables fade in. Dropped
    tombstones render as 💀 glyphs flying away.
  - **Read-path overlay** (animation only; transient). On a `read`
    step, render a "query cursor" badge that sweeps through memtable →
    immutable → L0 SSTables → L1 SSTables in order. Each SSTable
    check renders the Bloom-filter result: hit → arrow into index +
    "disk seek" annotation; miss → strikethrough + "skipped" annotation.
- **SSTable selection** keyed by `sstableId` so existing SSTables
  smoothly translate when new ones are added or sources fade.
- **WAL entry selection** keyed by `entryIdx` (monotonic; never
  re-uses).
- **Memtable cell selection** keyed by key (so re-writes flash on the
  same row rather than appending).
- Transitions: 400 ms for SSTable fade-in/out; 300 ms for memtable
  cell pop; 500 ms for the read-path cursor sweep (staggered per
  level).

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset + section dividers.
- **`PayloadDecoder.scala`** for parsing.
- **Canon colours** (parse-time validated):
  - WAL entry → `#94a3b8` (slate)
  - memtable cell → `#3b82f6` (blue)
  - immutable memtable → `#cbd5e1` (slate-light)
  - tombstone → `#ef4444` (rose)
  - L0 SSTable → `#3b82f6` (blue)
  - L1 SSTable → `#10b981` (emerald)
  - L2 SSTable → `#f59e0b` (amber)
  - L3+ SSTable → `#a855f7` (violet)
  - Bloom hit → `#10b981` (emerald arrow)
  - Bloom miss → `#94a3b8` (slate strikethrough)
  - read-path cursor → `#a855f7` (violet)
- **`LucideIcons.scala`** for the tombstone 💀 (or unicode literal),
  the database icon for SSTables, the eye icon for read cursor.
- **D3 scales**: shared key-range linear scale across all level rows
  so SSTable widths are visually comparable across levels. Recompute
  on every step where SSTable set changes (use D3 `extent` over union
  of all current SSTable key ranges).

## 9. Estimated build session count

**3 sessions.**

- Session 1: schema + parsing + static layout render (memtable + WAL
  + level rows) covering payload 5a's initial state. Lift the
  rect-and-text patterns from `BTreeWalker` and `ArrayTraversal`.
- Session 2: write-path animations (write, delete, flush) covering
  payloads 5a + 5d. Implement memtable-to-L0 transition.
- Session 3: compaction overlay + read-path overlay covering payloads
  5b + 5c + 5e. Demo book chapter, dispatcher wiring, scalafmt.

Risk: the read-path animation has the most moving parts (cursor sweep
through multiple levels, per-SSTable Bloom check, index-lookup
annotation). Budget extra time in session 3; defer the Bloom
false-positive payload (5e) polish to a follow-up if needed.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/lsm-tree.md` — exhibits all 5 POC
payloads (write path with flush, read path with Bloom checks, leveled
compaction, tombstone propagation, Bloom false positive). Each payload
has a 1-sentence caption naming its destination home (every payload
backs the LSM chapter; the demo book is the sole exhibition site).
