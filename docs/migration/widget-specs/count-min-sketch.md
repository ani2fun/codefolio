# Widget Spec — `count-min-sketch`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise a Count-Min Sketch — a `d × w` grid of integer counters plus
`d` independent hash functions — and the two operations that define it:

1. **Insert** (`add(x, count=1)`). Hash the item `d` ways; each hash
   picks one column per row. Increment all `d` resulting cells. The
   animation traces one arrow per row from the input value to the
   target column, then increments that cell's counter.
2. **Count query** (`query(x)`). Hash the item `d` ways; read all `d`
   cells. **The query returns the *minimum* of those `d` counter
   values** — never the average, never the sum. The widget renders the
   `d` candidate values stacked on the right, with the smallest visually
   selected (gold ring around it) as the answer. This visual emphasis on
   "min" is the entire pedagogy of the chapter — overestimation only,
   minimum is the tightest upper bound.

A heavy-hitter contrast payload shows that a *hot* item lands inflated
counters in *most* rows yet the minimum still reflects its true count
closely, while a *cold* item that collides with hot items in every
row gets dramatically overestimated.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

No source coverage. Payload milestones come from the chapter
`09-probabilistic-and-advanced/03-count-min-sketch.md`: the inline
sketch grid in § The 2D counter grid (`d=4, w=8`), the `add` / `query`
pseudocode in the same section, the "why min" formal statement in
§ Why "min", and the hot/warm/cold stream simulation in § Implementation.

## 3. Destination chapter usage

- **Primary owner.** `09-probabilistic-and-advanced/03-count-min-sketch.md` —
  three instances:
  - § The 2D counter grid: static empty grid + one insert sequence to
    anchor the layout.
  - § Why "min": animated count-query showing 4 candidate counters
    stacked on the right with the minimum highlighted; contrast a hot
    item (tight estimate) against a cold item (loose estimate).
  - § Heavy hitters extension: optional — a top-K mini-stream where
    insert + query alternate. Defer to follow-up if schema additions
    slow session 1.
- **No reuse expected.** CMS is a specialised structure; the chapter is
  its only consumer in the current scope.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  rows:    number,                // d; 4 typical for didactic
  cols:    number,                // w; 8 typical for didactic
  // Pre-baked hash mapping. Each entry is an item -> array of d column
  // indices, one per row. The widget does not actually hash. Deterministic
  // payloads let the author engineer the cold-collision example.
  hashes:  Record<string, Array<number>>,   // item -> d column indices
  cells?:  Array<Array<number>>,            // initial counter state; defaults to all zeros
  steps: Array<{
    op:    "insert" | "query" | "noop",
    item:  string,
    msg:   string,
    count?: number,                         // insert weight; defaults to 1
    // For query ops, the widget computes the candidate counter values
    // and asserts at parse time that `expectedMin` matches the actual
    // minimum (when supplied). Render: candidate values stacked on the
    // right, minimum highlighted gold.
    expectedMin?: number,
    // Optional contributor overlay for cold-collision demos. Each entry
    // names which prior insert is causing inflation in a given row.
    // Renders a thin connector from the inflated cell to a legend entry
    // above the grid.
    inflatedBy?: Array<{
      row:        number,
      colliderItem: string                  // name of the hot prior-insert
    }>
  }>,
  sections?: Array<{ name: string, startIdx: number }>
}
```

Validation:

- Every key in `hashes` must have `.length == rows`.
- Every column index in `hashes[item]` must satisfy `0 <= idx < cols`.
- `expectedMin` (when supplied) must equal the actual minimum after
  applying every prior step's mutations.
- `inflatedBy[].row` must be a valid row index; `colliderItem` must
  appear in some earlier `step.item` with `op == "insert"`.

## 5. POC payloads

### 5a. Static empty grid + single insert

```d3 widget=count-min-sketch
{
  "title": "Count-Min Sketch — insert one item, d=4 rows × w=8 cols",
  "rows": 4,
  "cols": 8,
  "hashes": {
    "apple": [2, 5, 0, 7]
  },
  "steps": [
    { "op": "noop",   "item": "apple", "msg": "Empty sketch — all 32 counters zero." },
    { "op": "insert", "item": "apple", "msg": "h(apple) = (2, 5, 0, 7) — increment one cell per row." }
  ]
}
```

### 5b. Insert + query (true count = 1)

```d3 widget=count-min-sketch
{
  "title": "query(apple) — min over 4 candidate counters",
  "rows": 4,
  "cols": 8,
  "hashes": {
    "apple": [2, 5, 0, 7]
  },
  "steps": [
    { "op": "insert", "item": "apple", "msg": "Insert apple — increment cells (0,2), (1,5), (2,0), (3,7)." },
    { "op": "query",  "item": "apple", "msg": "Read 4 candidates: [1, 1, 1, 1]. Min = 1. Returns 1.",
      "expectedMin": 1 }
  ]
}
```

### 5c. Heavy hitter — hot item queries cleanly

```d3 widget=count-min-sketch
{
  "title": "Heavy hitter — 'hot' inserted 100×, query reports ~100",
  "rows": 4,
  "cols": 8,
  "hashes": {
    "hot":  [2, 5, 0, 7],
    "cold": [2, 1, 3, 7]
  },
  "steps": [
    { "op": "insert", "item": "hot", "count": 100, "msg": "Insert 'hot' 100 times — cells (0,2), (1,5), (2,0), (3,7) all become 100." },
    { "op": "insert", "item": "cold", "msg": "Insert 'cold' once — cells (0,2)+1=101, (1,1)=1, (2,3)=1, (3,7)+1=101." },
    { "op": "query",  "item": "hot", "msg": "Read 4 candidates: [101, 100, 100, 101]. Min = 100. True count = 100 — tight estimate.",
      "expectedMin": 100 }
  ]
}
```

### 5d. Cold-item inflation — minimum still overestimates

```d3 widget=count-min-sketch
{
  "title": "Cold item never inserted — but minimum is 1, not 0 (overestimation)",
  "rows": 4,
  "cols": 8,
  "hashes": {
    "hot":   [2, 5, 0, 7],
    "warm":  [4, 1, 6, 3],
    "ghost": [2, 1, 0, 3]
  },
  "steps": [
    { "op": "insert", "item": "hot",  "msg": "Insert hot — cells (0,2), (1,5), (2,0), (3,7) = 1." },
    { "op": "insert", "item": "warm", "msg": "Insert warm — cells (0,4), (1,1), (2,6), (3,3) = 1." },
    { "op": "query",  "item": "ghost", "msg": "Read candidates: [1, 1, 1, 1]. Min = 1. But ghost was never inserted — overestimation.",
      "expectedMin": 1,
      "inflatedBy": [
        { "row": 0, "colliderItem": "hot" },
        { "row": 1, "colliderItem": "warm" },
        { "row": 2, "colliderItem": "hot" },
        { "row": 3, "colliderItem": "warm" }
      ]
    }
  ]
}
```

### 5e. Why "min" not "avg" — comparative readout

```d3 widget=count-min-sketch
{
  "title": "Why min, not avg — one row collides hard, others are clean",
  "rows": 4,
  "cols": 8,
  "hashes": {
    "x":      [2, 5, 0, 7],
    "noisy":  [2, 4, 1, 3],
    "noisy2": [2, 6, 7, 4],
    "noisy3": [2, 3, 6, 1]
  },
  "steps": [
    { "op": "insert", "item": "x",      "msg": "Insert x — all 4 cells = 1." },
    { "op": "insert", "item": "noisy",  "msg": "Insert noisy — its row-0 column is also 2." },
    { "op": "insert", "item": "noisy2", "msg": "Insert noisy2 — also row-0 column 2." },
    { "op": "insert", "item": "noisy3", "msg": "Insert noisy3 — also row-0 column 2. Cell (0,2) is now 4." },
    { "op": "query",  "item": "x",      "msg": "Candidates: [4, 1, 1, 1]. avg = 1.75, min = 1. min is the tight upper bound; avg leaks the noise.",
      "expectedMin": 1 }
  ]
}
```

## 6. Closest existing widget to mimic

- **`ArrayTraversal.scala`** is the structural ancestor for cell-grid
  rendering. CMS extends it to 2D: row + column rather than just column.
  Reuse the cell-rect-and-text idiom, the keyed-join transition pattern,
  and the marker decoration style for the hash arrows.
- **`LinkedList.scala`** is not a fit — no node-and-arrow chain here.
- **`BTreeWalker.scala`** is not a fit — no slider for CMS (the chapter
  uses fixed `(d, w)` tunings; the math intuition lives in the
  prose-side formula table, not an interactive knob).
- The `inflatedBy` contributor legend mirrors the contributor pattern
  in the `bloom-filter` spec; if both widgets land in the same session
  pair, factor the legend renderer into
  `widgets/sketches/ContributorLegend.scala`.

## 7. D3 selections plan

- Host `<svg>` with three logical regions:
  - **Hash arrows lane** (top, `~60px` high). For an insert step, render
    `d` arrows from a synthetic input-value label down to the `d`
    target cells (one per row). For a query step, arrows are dashed.
  - **Counter grid** (centre, `rows × cols` cells of `36×36` px each).
    `<g>` per row; `<g>` per cell within row. `<rect>` filled by counter
    magnitude (interpolated colour: white at 0, blue at max-in-grid);
    `<text>` shows the count.
  - **Candidate stack** (right, `d` boxes stacked vertically; visible
    only during query steps). One box per row, labelled with the cell's
    value; the smallest box gets a gold ring + "← min" label.
- **Cell selection** keyed by `${row}-${col}`. Transition fill colour
  and text content on increment.
- **Hash arrow selection** keyed by `${stepIdx}-${row}` so arrows
  appear / disappear per step.
- **Contributor legend** (cold-collision demos only) — strip above the
  grid; one swatch per `inflatedBy` entry connecting to its row.
- Transitions: 350 ms for counter increments + colour interpolation;
  200 ms fade-in for arrows; 250 ms for the min ring on query steps.

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset.
- **`PayloadDecoder.scala`** for parsing.
- **Canon outcome colours** (parse-time validated):
  - `increment`     → `#3b82f6` (blue, lit cells)
  - `query-min`     → `#f59e0b` (amber, the selected minimum candidate)
  - `query-other`   → `#94a3b8` (slate, non-min candidates)
  - `inflation`     → `#ef4444` (rose, contributor connectors in cold demos)
- **Cell colour ramp** — D3 `interpolateBlues` between counter 0 and
  the grid's current max. Recompute per step so the visual contrast
  scales as counters grow.
- **`LucideIcons.scala`** for the input-value icon.
- **Contributor legend** — shared with `bloom-filter` if both widgets
  land in the same session pair (see § 6).

## 9. Estimated build session count

**2 sessions.**

- Session 1: schema + parsing + cell-grid render covering payloads
  5a, 5b, 5c. Lift the cell-grid pattern from `ArrayTraversal.scala`
  and extend to 2D.
- Session 2: candidate-stack overlay, contributor legend, hash arrows,
  payloads 5d + 5e, demo book chapter, dispatcher wiring, scalafmt.

Risk: the candidate-stack render and the "min" highlight is the
chapter's single most important visual. Budget time for visual polish in
session 2.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/count-min-sketch.md` — exhibits all
5 POC payloads (empty + single insert, insert + query, heavy hitter,
cold inflation, why-min comparative). Each payload has a 1-sentence
caption naming its destination home.
