# Widget Spec — `hyperloglog`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise HyperLogLog cardinality estimation. The structure is conceptually
two-layered:

1. **Hash split.** Every incoming item is hashed to a uniform 64-bit
   value. The top `p` bits select a bucket; the remaining bits are
   measured for **leading zeros**. The widget animates this split: an
   input arrow drops into the hash function, the resulting bit string
   appears with a vertical divider between bucket bits and value bits,
   and the value bits' leading-zero count `ρ` is highlighted.
2. **Bucket counter update.** Each bucket maintains the running
   *maximum* `ρ` it has ever seen. The widget shows a horizontal row of
   bucket boxes, each labelled with `R[j]` (its current max). On
   insert, the target bucket's counter rises only if the new `ρ` beats
   the stored max — otherwise the bucket flashes "kept" briefly.

A **harmonic-mean estimator** panel sits below the bucket row. After
every insert step it recomputes
`E = α_m · m² / Σ 2^(-R[j])` and displays the running cardinality
estimate alongside the true count. The widget shows at least **8
buckets** (`p = 3`) for the static demos so the harmonic-mean math is
visible row-by-row; the full `m = 16384` is referenced in prose but not
rendered. The whole point is that you can *see* a few high-`R` buckets
pulling the harmonic mean upward while many zero-buckets pull it down
— and watch the estimate converge as inserts accumulate.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

No source coverage. Payload milestones come from
`09-probabilistic-and-advanced/04-hyperloglog.md`: the leading-zero
intuition in § The leading-zero trick, the bucket-split diagram in
§ Bucketed estimation (`hash(x) = (bucket bits)(leading-zero bits)`),
the HLL algorithm pseudocode in § The HLL algorithm, and the
estimate-vs-truth empirical table in § Implementation (5 rows
`n ∈ {100, 1_000, …, 1_000_000}`).

## 3. Destination chapter usage

- **Primary owner.** `09-probabilistic-and-advanced/04-hyperloglog.md` —
  three instances:
  - § The leading-zero trick: single-bucket pathological-variance demo
    (`p = 0`, just one counter; insert a few items and watch the
    estimate swing wildly with one lucky high-ρ hash).
  - § Bucketed estimation: 8-bucket demo (`p = 3`, `m = 8`) showing the
    hash-split + harmonic-mean reducing variance.
  - § The HLL algorithm: the same 8-bucket demo, but stepped — three
    inserts, watch the running estimate update each step.
- **Possible reuse in `02-linear-structures/07-hash-table/01-introduction.md`**
  as a "cardinality estimation" sidebar contrasting hash sets against
  HLL. Single static instance.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  p:       number,                // bucket-index bit count; m = 2^p; 3 for didactic demos
  hashBits: number,               // total hash width; usually 16 for didactic, 64 in prose
  // Pre-baked hash mapping. Each entry is an item -> binary string of
  // length `hashBits`. Top `p` bits select bucket; remaining bits
  // measured for leading zeros. Deterministic payloads let the author
  // engineer specific bucket dynamics (one very high ρ, many zero
  // buckets, etc.).
  hashes:  Record<string, string>,   // item -> "0101...0010" (length hashBits)
  buckets?: Array<number>,           // initial R[j]; defaults to all zeros; length = 2^p
  steps: Array<{
    op:   "insert" | "estimate" | "noop",
    item: string,
    msg:  string,
    // For estimate-only ops, no mutation occurs; the widget recomputes
    // and renders the estimator panel. Useful for the "static empty
    // grid" first step that already shows the estimator at zero.
    trueCount?: number,                // optional ground-truth annotation for the estimator panel
    // Optional emphasis for the "bucket-keeps" case — flash green when
    // the new ρ is <= stored max so the reader sees that not every
    // insert mutates state.
    bucketEvent?: "raise" | "keep"
  }>,
  sections?: Array<{ name: string, startIdx: number }>
}
```

Validation:

- `p >= 0 && p < hashBits`.
- Every value in `hashes` must be a binary string of length `hashBits`
  (regex `^[01]+$`, length match).
- `buckets.length` (when supplied) must equal `2^p`.
- The widget computes bucket index `j` and `ρ` from `hashes[item]`
  deterministically; if `op == "insert"`, the widget asserts that any
  declared `bucketEvent` matches the actual outcome (raise iff
  `ρ > buckets[j]`).

## 5. POC payloads

### 5a. Single-bucket pathological variance (`p = 0`)

```d3 widget=hyperloglog
{
  "title": "Single counter — one lucky hash pegs the estimate at 2^ρ",
  "p": 0,
  "hashBits": 16,
  "hashes": {
    "item_a": "0000000010110011",
    "item_b": "0100001010111100",
    "item_c": "0010010111000110"
  },
  "steps": [
    { "op": "insert", "item": "item_a", "msg": "ρ = 8 (leading zeros + 1). R[0] = 8. Estimate = 2^8 = 256.",
      "trueCount": 1, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_b", "msg": "ρ = 2. R[0] stays at 8. Estimate still 256.",
      "trueCount": 2, "bucketEvent": "keep" },
    { "op": "insert", "item": "item_c", "msg": "ρ = 3. R[0] stays at 8. True n = 3, estimate = 256 — wildly off.",
      "trueCount": 3, "bucketEvent": "keep" }
  ]
}
```

### 5b. 8-bucket harmonic mean — first insert

```d3 widget=hyperloglog
{
  "title": "8 buckets (p=3) — first insert chooses bucket and ρ",
  "p": 3,
  "hashBits": 16,
  "hashes": {
    "item_a": "0100000101100110"
  },
  "steps": [
    { "op": "noop",   "item": "item_a", "msg": "All 8 buckets empty; estimate ≈ 0.",
      "trueCount": 0 },
    { "op": "insert", "item": "item_a", "msg": "Top 3 bits = 010 → bucket 2. Remaining bits = 0000101100110, leading zeros = 4 → ρ = 5. R[2] = 5.",
      "trueCount": 1, "bucketEvent": "raise" }
  ]
}
```

### 5c. 8-bucket harmonic mean — eight inserts spread across buckets

```d3 widget=hyperloglog
{
  "title": "8 buckets, 8 distinct items — harmonic mean converges toward true count",
  "p": 3,
  "hashBits": 16,
  "hashes": {
    "item_0": "0001001011110100",
    "item_1": "0011100110010001",
    "item_2": "0100000101100110",
    "item_3": "0110000111010101",
    "item_4": "1000010100110011",
    "item_5": "1010100000111100",
    "item_6": "1100001011000010",
    "item_7": "1110000001011100"
  },
  "steps": [
    { "op": "insert", "item": "item_0", "msg": "bucket 0, ρ = 4. R = [4,0,0,0,0,0,0,0]. Estimate updates.",
      "trueCount": 1, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_1", "msg": "bucket 1, ρ = 3.",
      "trueCount": 2, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_2", "msg": "bucket 2, ρ = 5.",
      "trueCount": 3, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_3", "msg": "bucket 3, ρ = 5.",
      "trueCount": 4, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_4", "msg": "bucket 4, ρ = 5.",
      "trueCount": 5, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_5", "msg": "bucket 5, ρ = 2.",
      "trueCount": 6, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_6", "msg": "bucket 6, ρ = 4.",
      "trueCount": 7, "bucketEvent": "raise" },
    { "op": "insert", "item": "item_7", "msg": "bucket 7, ρ = 6. All buckets non-zero — estimator stabilises.",
      "trueCount": 8, "bucketEvent": "raise" }
  ]
}
```

### 5d. Harmonic mean shielding against a single outlier

```d3 widget=hyperloglog
{
  "title": "One outlier doesn't blow up the estimate — harmonic mean shields it",
  "p": 3,
  "hashBits": 16,
  "hashes": {
    "norm_a": "0001000101100110",
    "norm_b": "0010001011110100",
    "norm_c": "0011100110010001",
    "outlier": "0100000000000001"
  },
  "steps": [
    { "op": "insert", "item": "norm_a", "msg": "Bucket 0, ρ = 4. Normal-magnitude.",
      "trueCount": 1, "bucketEvent": "raise" },
    { "op": "insert", "item": "norm_b", "msg": "Bucket 1, ρ = 3.",
      "trueCount": 2, "bucketEvent": "raise" },
    { "op": "insert", "item": "norm_c", "msg": "Bucket 1, ρ = 3 — kept (no raise).",
      "trueCount": 3, "bucketEvent": "keep" },
    { "op": "insert", "item": "outlier", "msg": "Bucket 2, ρ = 13 (lots of leading zeros). 2^13 = 8192 in that bucket alone — but the harmonic mean across all 8 buckets is not 8192.",
      "trueCount": 4, "bucketEvent": "raise" }
  ]
}
```

### 5e. Estimator panel — reads from the table in § Implementation

```d3 widget=hyperloglog
{
  "title": "Reference estimates for n = 100 to 1,000,000 (p = 14, 12 KB memory)",
  "p": 3,
  "hashBits": 16,
  "hashes": {
    "a": "0001001011110100", "b": "0011100110010001",
    "c": "0100000101100110", "d": "0110000111010101",
    "e": "1000010100110011", "f": "1010100000111100",
    "g": "1100001011000010", "h": "1110000001011100"
  },
  "steps": [
    { "op": "insert", "item": "a", "msg": "1 distinct item." , "trueCount": 1, "bucketEvent": "raise" },
    { "op": "insert", "item": "b", "msg": "2."  , "trueCount": 2, "bucketEvent": "raise" },
    { "op": "insert", "item": "c", "msg": "3."  , "trueCount": 3, "bucketEvent": "raise" },
    { "op": "insert", "item": "d", "msg": "4."  , "trueCount": 4, "bucketEvent": "raise" },
    { "op": "insert", "item": "e", "msg": "5."  , "trueCount": 5, "bucketEvent": "raise" },
    { "op": "insert", "item": "f", "msg": "6."  , "trueCount": 6, "bucketEvent": "raise" },
    { "op": "insert", "item": "g", "msg": "7."  , "trueCount": 7, "bucketEvent": "raise" },
    { "op": "insert", "item": "h", "msg": "8 — harmonic mean stabilises.", "trueCount": 8, "bucketEvent": "raise" },
    { "op": "estimate", "item": "h", "msg": "Estimator panel: hover for the closed-form derivation.", "trueCount": 8 }
  ]
}
```

## 6. Closest existing widget to mimic

- **`ArrayTraversal.scala`** is the structural ancestor for the row of
  bucket boxes — single-row cell grid with text. Reuse the cell-rect
  + text + transition idiom for bucket counter updates.
- **`bloom-filter`** (this batch) provides the closest pattern for the
  hash arrow lane and the input-value icon. If both widgets land in
  the same session pair, factor the input-value renderer into
  `widgets/HashSplitArrow.scala`.
- **`CacheStampedeSimulator.scala`** is loosely related for the
  computed-derived metric panel (the estimator at the bottom is
  conceptually similar to the "ops/s under each strategy" readout),
  but the rendering is different enough that copy-paste is not worth
  it.
- **`LinkedList.scala`** is not a fit.

## 7. D3 selections plan

- Host `<svg>` with four logical regions stacked vertically:
  - **Input + hash split lane** (top, `~80px` high). Input-value icon
    on the left; arrow into a hash-function badge; output is a binary
    string with a vertical divider between bucket bits and value bits.
    The leading-zero run in the value bits is underlined and labelled
    `ρ = N`.
  - **Bucket row** (centre, `m` boxes in a horizontal row at `48×48`
    px). Each box labelled with its `R[j]` value. Box fill colour
    interpolated by `R[j]` magnitude (white at 0, blue at max).
  - **Estimator panel** (bottom-left, ~200×60). Three readouts:
    `n_true` (when supplied), `n_estimate` (computed), `error %`.
    Updates each step.
  - **Caption / msg lane** (very bottom). Reuses the standard step msg
    pattern from `linked-list`.
- **Bucket selection** keyed by bucket index. Transition fill + text
  on `R[j]` change. On a `keep` event, brief green flash with no
  permanent change.
- **Hash split selection** keyed by stepIdx so it appears/disappears
  per step rather than mutating.
- **Estimator readout** plain `<text>` updated each step via D3 join.
- Transitions: 400 ms for bucket counter raise; 300 ms for estimator
  numeric tween (animated number rolling, like a flight-departure board);
  150 ms green flash for keep events.

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset.
- **`PayloadDecoder.scala`** for parsing.
- **Canon outcome colours** (parse-time validated):
  - `bucket-raise` → `#3b82f6` (blue, persistent on bucket box)
  - `bucket-keep`  → `#10b981` (emerald, transient flash)
  - `rho-highlight` → `#f59e0b` (amber, the leading-zero underline)
  - `estimate-fill` → `#a855f7` (violet, the estimator panel background)
- **Numeric tween helper** — for the estimator readout. New helper in
  `widgets/NumericTween.scala` if generally useful; otherwise local.
- **`LucideIcons.scala`** for the input-value icon and the hash badge.
- **Closed-form helpers**:
  - `alpha(m)`: bias-correction constant per the chapter's table
    (`m=16 → 0.673`, `m=32 → 0.697`, `m=64 → 0.709`, else
    `0.7213 / (1 + 1.079/m)`).
  - `estimate(R, alpha, m)`: harmonic mean formula. Place in widget
    module (not a candidate for cross-widget reuse).

## 9. Estimated build session count

**2 sessions.**

- Session 1: schema + parsing + bucket-row render covering payloads
  5b and 5c. Lift cell-grid pattern; implement hash split lane;
  implement closed-form estimator helper.
- Session 2: estimator panel with numeric tween, keep-event flash,
  payloads 5a, 5d, 5e. Demo book chapter, dispatcher wiring,
  scalafmt.

Risk: the harmonic-mean visual is subtle. If the estimator panel
doesn't read clearly after session 2, a third session for visual polish
+ optional per-bucket `1/2^R` mini-bar above each bucket may be
warranted.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/hyperloglog.md` — exhibits all 5 POC
payloads (single-bucket variance, first insert, 8-bucket convergence,
outlier shielding, estimator-only). Each payload has a 1-sentence
caption naming its destination home.
