# Widget Spec — `bloom-filter`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise a Bloom filter — a bit array of length `m` plus `k` independent
hash functions — and the three operations that make the structure
pedagogically interesting:

1. **Insert.** Hash the item `k` ways, light the `k` corresponding bits.
   The animation traces each hash arrow from the input value to its
   target bit, then flips the bit to `1`.
2. **Lookup (true positive).** Hash the same item `k` ways, check the `k`
   bits, observe all are `1`, return "probably in".
3. **Lookup (false positive).** Hash an item that was *never inserted*;
   discover that — by coincidence — its `k` hashed bits have all been
   set by *other* prior inserts. Return "probably in" incorrectly. The
   widget highlights *which* prior inserts contributed each colliding
   bit so the reader sees the false-positive mechanism, not just the
   verdict.

A capacity / target-FPR slider lets the reader change `n` (expected
items) and `p` (target false-positive rate); `m` and `k` recompute via
`m = -n·ln(p)/(ln 2)²`, `k = (m/n)·ln 2`. The bit-array length and the
arrow fan-out re-render to match.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

No source coverage. Payload milestones come from the chapter
`09-probabilistic-and-advanced/02-bloom-filter.md`: the inline
`flowchart LR` "Add 'apple'" / "Contains 'apple'?" diagram in § The bit
array and the hashes, the worked false-positive example implicit in the
Python `bf2` empirical-check in § Implementation, and the tuning table
in § Tuning `m` and `k`.

## 3. Destination chapter usage

- **Primary owner.** `09-probabilistic-and-advanced/02-bloom-filter.md` —
  three instances:
  - § The bit array and the hashes: insert + true-positive lookup (one
    instance, two operations in sequence).
  - § Tuning `m` and `k`: capacity / FPR slider demo, showing how the
    bit array grows and how saturation degrades the filter.
  - § Variants: optional — could illustrate the Counting Bloom Filter
    deletion with a small counter per bit instead of a 1-bit flag.
    Defer to follow-up if schema additions slow session 1.
- **Reuse in `11-dsa-in-real-systems/05-lsm-trees-rocksdb-cassandra.md`** —
  one instance in § Reads with multiple SSTables, showing the Bloom
  filter as the "is this key possibly in this SSTable?" gate that
  saves the disk seek.
- **Possible reuse in `02-linear-structures/07-hash-table/*`** — as a
  contrast against hash sets, in the introduction chapter only.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  bitArrayLength: number,        // m; for static instances; 16-32 typical for didactic
  hashCount: number,             // k; 3 typical
  // Pre-baked hash mapping. Author supplies the (item, k-bit-positions) tuples
  // explicitly; the widget does not actually hash. This keeps payloads
  // deterministic and lets the author engineer the false-positive collision.
  hashes: Record<string, Array<number>>,   // item -> bit indices, length must = hashCount
  bits?:  Array<0 | 1>,          // initial bit state; defaults to all zeros, length = bitArrayLength
  steps: Array<{
    op:   "insert" | "lookup" | "noop",
    item: string,
    msg:  string,
    // For lookup ops: outcome derived from the bits after applying every
    // prior insert step's hashes. The widget computes this; the author
    // does not assert it — but the widget exposes it in the caption.
    // For false-positive demos, the author can set `expectFalsePositive: true`
    // and the widget asserts at parse time that the item was never inserted
    // earlier yet all k bits are set (else parse-time invalid("…")).
    expectFalsePositive?: boolean,
    // Highlight overlay for false-positive demos. List of (item, bitIdx)
    // tuples naming which prior insert set which colliding bit. Renders as
    // colour-coded arrows from the bit cells back to the original insert
    // labels in a legend strip above the bit array.
    contributors?: Array<{ item: string, bitIdx: number }>
  }>,
  // Optional capacity slider — only one of (steps, slider) per widget instance.
  // When `slider` is present, `steps` is ignored.
  slider?: {
    n:     number,                      // expected items starting value
    p:     number,                      // target FPR starting value
    nRange: [number, number],
    pRange: [number, number]
  },
  sections?: Array<{ name: string, startIdx: number }>
}
```

Validation:

- Every key in `hashes` must have `.length == hashCount`.
- Every bit index in `hashes[item]` must satisfy `0 <= idx < bitArrayLength`.
- `expectFalsePositive: true` ⇒ widget computes `bitsAfterPriorInserts(idx)`
  for every `idx in hashes[item]` and asserts all are `1`, *and* asserts
  `item` does not appear in any earlier `step.item` with `op == "insert"`.
- `slider` and `steps` are mutually exclusive; presence of both is parse error.

## 5. POC payloads

### 5a. Insert + true-positive lookup ("apple")

```d3 widget=bloom-filter
{
  "title": "Bloom filter — insert 'apple', then look it up",
  "bitArrayLength": 16,
  "hashCount": 3,
  "hashes": {
    "apple":  [2, 5, 8],
    "banana": [1, 5, 11],
    "cherry": [3, 8, 13],
    "fig":    [2, 8, 11]
  },
  "steps": [
    { "op": "insert", "item": "apple",  "msg": "h1=2, h2=5, h3=8 — set bits 2, 5, 8." },
    { "op": "lookup", "item": "apple",  "msg": "Check bits 2, 5, 8 — all 1 → probably in." }
  ]
}
```

### 5b. False positive demo ("fig" collides after three real inserts)

```d3 widget=bloom-filter
{
  "title": "False positive — 'fig' was never inserted but all 3 bits are set",
  "bitArrayLength": 16,
  "hashCount": 3,
  "hashes": {
    "apple":  [2, 5, 8],
    "banana": [1, 5, 11],
    "cherry": [3, 8, 13],
    "fig":    [2, 8, 11]
  },
  "steps": [
    { "op": "insert", "item": "apple",  "msg": "h(apple) = {2, 5, 8} — set bits." },
    { "op": "insert", "item": "banana", "msg": "h(banana) = {1, 5, 11} — set bits 1 and 11 (5 already 1)." },
    { "op": "insert", "item": "cherry", "msg": "h(cherry) = {3, 8, 13} — set bits 3 and 13 (8 already 1)." },
    { "op": "lookup", "item": "fig",    "msg": "h(fig) = {2, 8, 11} — all 1. Returns YES — false positive.",
      "expectFalsePositive": true,
      "contributors": [
        { "item": "apple",  "bitIdx": 2 },
        { "item": "cherry", "bitIdx": 8 },
        { "item": "banana", "bitIdx": 11 }
      ]
    }
  ]
}
```

### 5c. Definitely-not lookup (one bit is zero)

```d3 widget=bloom-filter
{
  "title": "Lookup miss — 'grape' has a zero bit, definitely not in set",
  "bitArrayLength": 16,
  "hashCount": 3,
  "hashes": {
    "apple": [2, 5, 8],
    "grape": [4, 7, 14]
  },
  "steps": [
    { "op": "insert", "item": "apple", "msg": "Insert 'apple' — bits {2, 5, 8}." },
    { "op": "lookup", "item": "grape", "msg": "Check {4, 7, 14} — bit 4 is 0. Definitely not in set." }
  ]
}
```

### 5d. Saturation demo — after many inserts, FPR explodes

```d3 widget=bloom-filter
{
  "title": "Saturation — 50% of bits set, every lookup is now a near-coin-flip",
  "bitArrayLength": 16,
  "hashCount": 3,
  "hashes": {
    "a": [0, 5, 11], "b": [1, 6, 12], "c": [2, 7, 13],
    "d": [3, 8, 14], "e": [4, 9, 15], "f": [0, 6, 10],
    "g": [1, 7, 11], "test": [2, 6, 9]
  },
  "steps": [
    { "op": "insert", "item": "a", "msg": "Insert a." },
    { "op": "insert", "item": "b", "msg": "Insert b." },
    { "op": "insert", "item": "c", "msg": "Insert c." },
    { "op": "insert", "item": "d", "msg": "Insert d." },
    { "op": "insert", "item": "e", "msg": "Insert e." },
    { "op": "insert", "item": "f", "msg": "Insert f." },
    { "op": "insert", "item": "g", "msg": "Insert g — 50% of bits now lit." },
    { "op": "lookup", "item": "test", "msg": "test was never inserted; bits {2, 6, 9} are all lit — false positive.",
      "expectFalsePositive": true,
      "contributors": [
        { "item": "c", "bitIdx": 2 },
        { "item": "b", "bitIdx": 6 },
        { "item": "e", "bitIdx": 9 }
      ]
    }
  ]
}
```

### 5e. Capacity / FPR slider (§ Tuning `m` and `k`)

```d3 widget=bloom-filter
{
  "title": "Tune m and k — drag to see the bit-array size and hash count update",
  "bitArrayLength": 32,
  "hashCount": 3,
  "hashes": {},
  "slider": {
    "n": 1000,
    "p": 0.01,
    "nRange": [100, 1000000],
    "pRange": [0.0001, 0.1]
  }
}
```

## 6. Closest existing widget to mimic

- **`CacheStampedeSimulator.scala`** is the closest existing pattern for
  the slider variant — single SVG built as a string, slider state
  driving a derived render. Reuse the slider-handle CSS + the
  string-injection mount (`dangerouslySetInnerHTML` is acceptable for
  this widget; the bit array is a flat row).
- **`ArrayTraversal.scala`** is the structural ancestor for the bit
  array row of cells. Reuse the cell-grid layout, the marker decoration
  pattern (k hash arrows drawn from above), and the keyed-join idiom
  for transitioning bits from `0 → 1`.
- **`BTreeWalker.scala`** does not apply — it's a slider widget over a
  pre-computed tree shape; the Bloom widget needs animated step
  transitions, which `BTreeWalker` lacks.
- Use `LinkedList.scala`'s ADR-0016 canon pattern for the contributor
  legend colours (one canonical colour per *position in contributors
  list*, not per item name — the chapter uses random colour-coding to
  visually link bits back to inserts).

## 7. D3 selections plan

- Host `<svg>` with two stacked sections:
  - **Hash arrows lane** (top, `~60px` high). For an insert step, render
    `k` curved arrows from a synthetic "input value" label down to the
    `k` target bit cells. For a lookup, arrows are dashed and styled by
    outcome (green if bit lit, red if not).
  - **Bit array row** (bottom, single row of `bitArrayLength` cells).
    Each cell is `32×32` px; `<rect>` filled by bit state (`0` = light
    slate, `1` = blue); `<text>` inside shows `0` or `1`.
- **Bit cell selection** keyed by bit index. Transition fill colour on
  `0 → 1`; sticky thereafter.
- **Hash arrow selection** keyed by `${stepIdx}-${hashIdx}` so arrows
  appear / disappear per step rather than mutating.
- **Contributor legend** (false-positive demos only) — strip above the
  hash arrows; one coloured swatch per contributor `(item, bitIdx)`
  with a thin line connecting it to the corresponding bit cell.
- **Slider variant** — separate render path: no animation, single
  layout computed from `(n, p)` slider state via the chapter's
  closed-form formulas; bit array rendered as proportional fill (`50%`
  lit at saturation, etc.) rather than per-bit.
- Transitions: 350 ms for bit fills; 200 ms for arrow fade-in; slider
  rerender debounced at 120 ms.

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset on the `steps` variant.
- **`PayloadDecoder.scala`** for parsing.
- **Slider helper** lifted from `CacheStampedeSimulator.scala` — the
  log-mapped slider for `n` and the linear-log slider for `p`. If
  worth promoting, factor into `widgets/SliderControls.scala` during a
  future hardening pass. For session 1, copy-paste is fine.
- **Canon outcome colours** (parse-time validated, never overridden by
  payload):
  - `inserted-bit` → `#3b82f6` (blue, lit)
  - `lookup-hit`   → `#10b981` (emerald, all bits lit)
  - `lookup-miss`  → `#ef4444` (rose, at least one zero)
  - `false-positive` → `#f59e0b` (amber, contributor highlight strip)
- **`LucideIcons.scala`** for the input-value icon and possibly the
  hash function badge.

## 9. Estimated build session count

**2 sessions.**

- Session 1: schema + parsing + step-based render covering payloads 5a,
  5b, 5c, 5d. Lift the cell-grid pattern from `ArrayTraversal.scala`;
  build the hash-arrow lane and contributor legend.
- Session 2: slider variant (5e), demo book chapter, scalafmt +
  verification gates, dispatcher wiring.

Risk: contributor-legend visual polish for false-positive demos may
slip into a third session; the false-positive mechanism is the chapter's
single most important visual.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/bloom-filter.md` — exhibits all 5 POC
payloads (insert + lookup, false positive, miss, saturation, slider).
Each payload has a 1-sentence caption naming its destination home.
