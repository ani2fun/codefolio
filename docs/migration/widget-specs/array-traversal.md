# Widget Spec — `array-traversal` (retrofit)

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `array-traversal` and Arc 3 chapter payload writing.
>
> **Status:** the widget already exists at
> `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
> (784 LOC) and ships with a secondary-row mode and per-step `secondaryItems` /
> `secondaryMarkers` / `secondaryRange` support. This document is a **retrofit
> note**: it lists the gaps that need filling for Phase 0 catch-up
> (multidimensional arrays) and for Phase 13 (2D binary search / staircase
> search). Out of scope are everything the widget already does — single-row
> traversal, ranged highlighting, per-step `items` rebinding, multi-marker
> stacking, secondary-row stacked layout.

## 1. Purpose

Animate index-driven movement over a one-dimensional row of cells (or, after
the 2D retrofit, a row × column grid). Each step renders a snapshot:
which cells hold which values, where each named marker (`i`, `j`, `left`,
`right`, `mid`, `slow`, `fast`, `read`, `write`) is pointing, and which
contiguous index range is highlighted. Optional secondary row supports
two-array problems (simultaneous traversal, source→destination copy,
build-the-frequency-table) — already shipped, see existing destination
payloads on `02-linear-structures/03-singly-linked-list/01-introduction-to-singly-linked-lists.md`.

## 2. Source-diagram inventory — retrofit-only gaps

### Gap A — 2D grid mode (`layout: "2d"`)

Phase 0 catch-up. Three source diagrams in the multidimensional-arrays
chapter that currently render as four-frame d2 placeholders in the
destination because there is no 2D mode yet:

- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/1.arrays/02-multidimensional-arrays/04-overview-of-supported-operations.md:483`
  — "Traversing a multidimensional array using a nested loop iterating
  indices for all dimensions" (20 frames → 7 widget steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/1.arrays/02-multidimensional-arrays/06-understanding-row-major-order.md:21`
  — "Row major order lays out elements by moving the lowest dimension the
  fastest" (22 frames → 8 widget steps; needs `layout:"flat-with-2d-overlay"`
  to show one-dim address mapping)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/1.arrays/02-multidimensional-arrays/09-understanding-column-major-order.md:21`
  — "Column major order lays out elements by moving the highest dimension
  the fastest" (23 frames → 8 widget steps; same overlay mode)

Phase 13 searches over 2D matrices, six more diagrams:

- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/01-understanding-the-problem.md:18`
  — "Linear search to find the student with a score of 85" (8 frames → 5 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/03-understanding-2d-binary-search-algorithm.md:72`
  — "The loop terminates when the low index exceeds the high index" (2 frames → 1 step, single frame)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/03-understanding-2d-binary-search-algorithm.md:97`
  — "The target is found at the middle index of the virtual array" (2 frames → 1 step)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/03-understanding-2d-binary-search-algorithm.md:107`
  — "Discard the first half of the virtual array" (2 frames → 1 step)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/03-understanding-2d-binary-search-algorithm.md:117`
  — "Discard the second half of the virtual array" (2 frames → 1 step)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/04-2d-binary-search/03-understanding-2d-binary-search-algorithm.md:125`
  — "Find an element in a 2D matrix using binary search" (10 frames → 6 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/05-staircase-search/03-understanding-staircase-search-algorithm.md:70`
  — "Find an element in a 2D matrix using staircase search" (16 frames → 7 steps)

### Gap B — Secondary-row payload cookbook gaps

Secondary-row support **exists in code** (per `ArrayTraversal.scala` lines
54-80, `Step.secondaryItems` etc.), but Phase 0 chapter
`02-multidimensional.md` does not yet exercise it. Other Phase 0 chapters
that should pick it up during catch-up:

- Source `06-pattern-simultaneous-traversal` (3 source diagrams; destination
  already has 2 widget instances — needs one more for full coverage)
- Source `11-design` (capacity-doubling already covered by current single-row
  payload at destination `11-design-a-dynamic-array.md` — leave alone)

Total source diagrams covered by this retrofit: **~9 (Phase 0)** +
**~7 (Phase 13)** = ~16 diagrams.

## 3. Destination chapter usage

### After 2D retrofit lands

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/01-arrays/02-multidimensional.md`
  — replaces all 16 d2 blocks with a small handful of `layout:"2d"` widget
  instances; flat-with-2d-overlay mode shows row-major / column-major address
  layouts.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/01-arrays/06-pattern-simultaneous-traversal.md`
  — fill the -1 gap noted in `diagram-gap-audit.md` (Phase 0).
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/02-searching/04-2d-binary-search.md`
  — POC chapter for 2D mode in Phase 13.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/02-searching/05-staircase-search.md`
  — staircase decision-path overlay on a 2D grid.

### Already-shipped destination chapters (no retrofit needed)

- Every chapter under
  `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/01-arrays/`
  (94 widget instances already live, per audit).
- Sorting chapters under
  `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/01-sorting/`
  (Wave A — `array-traversal` reuse for 56 source diagrams; no retrofit
  required, existing 1D + secondary-row covers it).

## 4. Payload schema sketch

The only new top-level field is `layout`. When absent the widget behaves
exactly as today (1D row with optional secondary row). When `"2d"`, the
widget renders a row × column grid and the per-step markers gain `row` +
`col` instead of `index`. `range` is replaced by `regionRange` with
`{rowLo, rowHi, colLo, colHi}`.

```ts
{
  title: string,
  layout?: "1d" | "2d" | "flat-with-2d-overlay",   // default "1d"
  items: string[],                                  // 1D mode only
  rows?: string[][],                                // 2D mode only; rows[row][col] = value
  cols?: number,                                    // flat-with-2d-overlay only; partitions `items` into rows of `cols`
  primaryLabel?: string,
  secondaryItems?: string[],                        // existing — secondary row in 1D mode
  secondaryLabel?: string,
  steps: Array<
    | { /* 1D step — unchanged */
        items?: string[],
        keys?: string[],
        markers: Array<{ name: string, index: number, color?: string }>,
        range?: { lo: number, hi: number },
        msg: string,
        secondaryItems?: string[],
        secondaryKeys?: string[],
        secondaryMarkers?: Array<{ name: string, index: number, color?: string }>,
        secondaryRange?: { lo: number, hi: number }
      }
    | { /* 2D step — new */
        rows?: string[][],
        markers: Array<{ name: string, row: number, col: number, color?: string }>,
        regionRange?: { rowLo: number, rowHi: number, colLo: number, colHi: number },
        msg: string
      }
    | { /* flat-with-2d-overlay step — new; cells stay in a single flat row
           but the marker labels surface the (row, col) interpretation */
        markers: Array<{ name: string, index: number, color?: string }>,
        rowColAnnotation?: { row: number, col: number },
        msg: string
      }
  >
}
```

## 5. POC payloads

### 5.1 Minimal 2D grid (3×4 matrix, single step)

```d3 widget=array-traversal
{
  "title": "2D matrix — 3 rows by 4 columns of integers",
  "layout": "2d",
  "rows": [
    ["12", "27", "45", "63"],
    ["18", "29", "51", "72"],
    ["23", "37", "55", "81"]
  ],
  "steps": [
    {
      "markers": [{"name": "start", "row": 0, "col": 0}],
      "msg": "matrix[0][0] = 12 — top-left origin."
    }
  ]
}
```

### 5.2 Typical 2D binary search (POC chapter)

```d3 widget=array-traversal
{
  "title": "2D binary search on a row+col-sorted matrix — target = 51",
  "layout": "2d",
  "rows": [
    ["12", "27", "45", "63"],
    ["18", "29", "51", "72"],
    ["23", "37", "55", "81"]
  ],
  "steps": [
    {
      "markers": [
        {"name": "low",  "row": 0, "col": 0},
        {"name": "high", "row": 2, "col": 3},
        {"name": "mid",  "row": 1, "col": 1}
      ],
      "regionRange": {"rowLo": 0, "rowHi": 2, "colLo": 0, "colHi": 3},
      "msg": "Virtual array spans the whole matrix. mid = (0+11)/2 = 5 → (row 1, col 1) = 29."
    },
    {
      "markers": [
        {"name": "low",  "row": 1, "col": 2},
        {"name": "high", "row": 2, "col": 3},
        {"name": "mid",  "row": 2, "col": 0}
      ],
      "regionRange": {"rowLo": 1, "rowHi": 2, "colLo": 0, "colHi": 3},
      "msg": "29 < 51 → discard the first half. mid = (6+11)/2 = 8 → (row 2, col 0) = 23."
    },
    {
      "markers": [
        {"name": "low",  "row": 2, "col": 1},
        {"name": "high", "row": 2, "col": 3},
        {"name": "mid",  "row": 2, "col": 2}
      ],
      "regionRange": {"rowLo": 2, "rowHi": 2, "colLo": 1, "colHi": 3},
      "msg": "23 < 51 → discard again. mid = (9+11)/2 = 10 → (row 2, col 2) = 55."
    },
    {
      "markers": [
        {"name": "low",  "row": 2, "col": 1},
        {"name": "high", "row": 2, "col": 1},
        {"name": "mid",  "row": 2, "col": 1}
      ],
      "regionRange": {"rowLo": 2, "rowHi": 1, "colLo": 0, "colHi": 0},
      "msg": "55 > 51 → discard. mid = (9+9)/2 = 9 → (row 2, col 1) = 37."
    },
    {
      "markers": [{"name": "found", "row": 1, "col": 2}],
      "msg": "37 < 51 → range collapses with no hit at mid. Recheck (row 1, col 2) = 51 ✓"
    }
  ]
}
```

### 5.3 Staircase search (large-N stress for 2D mode)

```d3 widget=array-traversal
{
  "title": "Staircase search — start top-right, target = 37",
  "layout": "2d",
  "rows": [
    ["12", "27", "45", "63", "78"],
    ["18", "29", "51", "72", "89"],
    ["23", "37", "55", "81", "95"],
    ["34", "44", "59", "85", "98"]
  ],
  "steps": [
    {"markers": [{"name": "ptr", "row": 0, "col": 4}], "msg": "Start at top-right corner (0, 4) = 78. 78 > 37 → move left."},
    {"markers": [{"name": "ptr", "row": 0, "col": 3}], "msg": "(0, 3) = 63. 63 > 37 → move left."},
    {"markers": [{"name": "ptr", "row": 0, "col": 2}], "msg": "(0, 2) = 45. 45 > 37 → move left."},
    {"markers": [{"name": "ptr", "row": 0, "col": 1}], "msg": "(0, 1) = 27. 27 < 37 → move down."},
    {"markers": [{"name": "ptr", "row": 1, "col": 1}], "msg": "(1, 1) = 29. 29 < 37 → move down."},
    {"markers": [{"name": "found", "row": 2, "col": 1}], "regionRange": {"rowLo": 2, "rowHi": 2, "colLo": 1, "colHi": 1}, "msg": "(2, 1) = 37 ✓ found in 6 steps."}
  ]
}
```

### 5.4 Flat-with-2d-overlay (row-major address mapping)

```d3 widget=array-traversal
{
  "title": "Row-major layout — 2D logical (row, col) → flat physical index",
  "layout": "flat-with-2d-overlay",
  "items": ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"],
  "cols": 4,
  "steps": [
    {"markers": [{"name": "addr", "index": 0}], "rowColAnnotation": {"row": 0, "col": 0}, "msg": "(0, 0) → flat[0] = A. Address = row * cols + col = 0 * 4 + 0 = 0."},
    {"markers": [{"name": "addr", "index": 5}], "rowColAnnotation": {"row": 1, "col": 1}, "msg": "(1, 1) → flat[5] = F. Address = 1 * 4 + 1 = 5."},
    {"markers": [{"name": "addr", "index": 10}], "rowColAnnotation": {"row": 2, "col": 2}, "msg": "(2, 2) → flat[10] = K. Address = 2 * 4 + 2 = 10."}
  ]
}
```

### 5.5 Secondary-row pattern (existing — payload cookbook example)

```d3 widget=array-traversal
{
  "title": "Frequency map build — primary array drives the secondary count array",
  "items": ["a", "b", "a", "c", "b", "a"],
  "primaryLabel": "Source string",
  "secondaryItems": ["0", "0", "0"],
  "secondaryLabel": "freq[a, b, c]",
  "steps": [
    {
      "markers": [{"name": "i", "index": 0, "color": "#3b82f6"}],
      "secondaryItems": ["1", "0", "0"],
      "secondaryMarkers": [{"name": "freq", "index": 0, "color": "#10b981"}],
      "msg": "Read 'a' → freq[0]++ → freq = [1, 0, 0]."
    },
    {
      "markers": [{"name": "i", "index": 1, "color": "#3b82f6"}],
      "secondaryItems": ["1", "1", "0"],
      "secondaryMarkers": [{"name": "freq", "index": 1, "color": "#10b981"}],
      "msg": "Read 'b' → freq[1]++ → freq = [1, 1, 0]."
    },
    {
      "markers": [{"name": "i", "index": 5, "color": "#3b82f6"}],
      "secondaryItems": ["3", "2", "1"],
      "secondaryMarkers": [{"name": "freq", "index": 0, "color": "#10b981"}],
      "msg": "Final freq = [3, 2, 1] — three 'a's, two 'b's, one 'c'."
    }
  ]
}
```

## 6. Closest existing widget to mimic

This **is** the widget being retrofitted —
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
(784 LOC). Mimic the existing `renderRow` decomposition pattern: extract a
`renderGrid` companion that takes `rows: List[List[String]]`, computes a 2D
cell-position table once per step, and reuses the existing marker / range
machinery against (row, col) coordinates by translating to absolute pixel
positions. The flat-with-2d-overlay mode is a single-row layout under the
hood — only the per-step marker tooltip needs to know the (row, col)
interpretation.

## 7. D3 selections plan

- Keep `g.array-traversal__cell-group` keyed by `items[i]` (existing). For
  2D mode, introduce `g.array-traversal__cell-group--grid` keyed by
  `${row}-${col}-${value}` so a cell can survive `rows` rebinds and
  transition smoothly on per-step rows overrides.
- Markers in 2D mode key by `name` (existing). The transform function maps
  `(row, col)` → `translate(colX(col), rowY(row))`. Triangle and label
  positions are computed relative to the cell centre — same as 1D, just
  with a column dimension added.
- `regionRange` renders as one `rect.array-traversal__region` per step,
  positioned at `(colX(colLo), rowY(rowLo))` with width / height covering
  `colHi-colLo+1` cells × `rowHi-rowLo+1` rows. Use a 4 px corner radius
  and `fill-opacity: 0.12` to keep the under-grid cells legible.
- Step changes re-bind on the same selections; D3 transitions interpolate
  every numeric attribute so a marker moving from `(0, 2)` to `(1, 2)`
  slides one cell down. `flat-with-2d-overlay` reuses the existing 1D
  `g.cell-group` selection — only the marker tooltip text changes.

## 8. Shared abstractions

Reuses
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
(already in use) and
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`
(already in use). No new shared utility — the 2D mode is widget-local
because no other widget needs row × column cell grids (the upcoming
`hash-table` widget renders a vertical bucket list, not a grid; `dp-table`
will have its own).

## 9. Estimated build session count

**1 session** — single Scala module retrofit. Add `layout` field to `Spec`,
extend `Step` with a 2D variant, factor `renderRow` to share the cell-paint
helper with a new `renderGrid`. CSS only needs one new selector
(`.array-traversal__region` for the 2D highlight rect). Demo book gets one
new chapter showing the 2D + flat-with-2d-overlay modes side by side.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/02-searching/04-2d-binary-search.md`
— smallest chapter that genuinely needs 2D mode (6 source diagrams, mostly
single-frame "what just happened" snapshots that compress cleanly into a
single multi-step widget). Once this lands and renders, the catch-up sweep
for `02-multidimensional.md` and `05-staircase-search.md` follows in the
same session or the next.
