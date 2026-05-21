---
title: array-traversal
summary: Index-driven movement over a one-dimensional row of cells, a two-dimensional grid, or a flat-with-2D-overlay address-mapping view. Per-step markers (i, j, left, right, mid, low, high, read, write, found, ptr, addr) drive named pointers; optional range / regionRange bands highlight contiguous sub-spans. Three layouts (`1d`, `2d`, `flat-with-2d-overlay`) cover one-dim traversal, two-dim search, and physical-address visualisation in a single widget.
prereqs: []
---

# `array-traversal`

## Purpose

The first widget in the D3 catalog and the workhorse for everything in
the source DSA book's Arrays + Searching arcs. `layout:"1d"` (default)
renders a single row of cells with per-step markers (`i`, `left`,
`right`, …) and an optional `range` band — the form already shipped at
03-stack-queue's launch. `layout:"2d"` renders a row × column grid with
per-step markers carrying `{row, col}` and an optional `regionRange`
rect highlighting a contiguous sub-matrix — used for 2D binary search
and staircase search. `layout:"flat-with-2d-overlay"` keeps the cells
in a single flat row but surfaces a `(row, col)` annotation under the
marker's cell — used for row-major / column-major address-mapping
diagrams where the 2D logical view and the 1D physical layout sit side
by side.

Optional secondary-row support (already shipped, 1D mode only) handles
two-array problems like simultaneous traversal and frequency-table
building.

> **Source spec**: `docs/migration/widget-specs/array-traversal.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
>
> **Marker canon**: [`MarkerCanon.scala`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala)
> — `i`/`left`/`low`/`read`/`slow`/`front`/`head` = blue;
> `j`/`previous`/`start` = amber;
> `right`/`high`/`fast`/`back` = rose;
> `mid`/`write`/`found`/`ptr`/`addr`/`current`/`top`/`end` = emerald;
> `freq`/`next` = violet.

## Payload schema (reference card)

```ts
{
  title:           string,
  layout?:         "1d" | "2d" | "flat-with-2d-overlay",  // default "1d"
  items?:          string[],                              // 1D + flat-overlay
  rows?:           string[][],                            // 2D only; rows[row][col] = value
  cols?:           number,                                // flat-overlay only; partitions items into rows of cols
  primaryLabel?:   string,                                // 1D only
  secondaryItems?: string[],                              // 1D only (optional second row)
  secondaryLabel?: string,                                // 1D only
  steps: Array<
    | { /* 1D step */
        items?:            string[],                      // overrides spec.items for this step
        keys?:             string[],                      // optional D3 key list (defaults to items)
        markers:           Array<{ name: string, index: number }>,
        range?:            { lo: number, hi: number },
        msg:               string,
        secondaryItems?:   string[],
        secondaryKeys?:    string[],
        secondaryMarkers?: Array<{ name: string, index: number }>,
        secondaryRange?:   { lo: number, hi: number }
      }
    | { /* 2D step */
        rows?:        string[][],                         // overrides spec.rows for this step
        markers:      Array<{ name: string, row: number, col: number }>,
        regionRange?: { rowLo: number, rowHi: number, colLo: number, colHi: number },
        msg:          string
      }
    | { /* flat-with-2d-overlay step */
        markers:           Array<{ name: string, index: number }>,
        rowColAnnotation?: { row: number, col: number },
        msg:               string
      }
  >
}
```

**Required**: `title`, `steps`, and one of (`items`, `rows`) appropriate
to the layout.
**Optional**: `layout` (defaults to `"1d"`), `primaryLabel`,
`secondaryItems`, `secondaryLabel`, per-step overrides.

**Marker rule (carry-forward)**: every step enumerates the markers it
wants shown. A marker whose `index` (1D / overlay) or `(row, col)` (2D)
falls outside the cell grid is skipped silently. Author-supplied
`color` fields on markers are dropped at parse time — colours come from
[`MarkerCanon`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala).
Non-canonical names render as inline warning badges (`⚠ name` in rose)
so typos are obvious.

## Representative payloads

### Payload 1 — minimum (1D, single pointer walks 5 cells)

Smallest meaningful sequence — one marker (`i`) walks left-to-right
through a 5-element array. Exercises the basic 1D row, marker
movement, and step-counter UI.

```d3 widget=array-traversal
{
  "title": "1D — single pointer walks 5 cells",
  "items": ["10", "20", "30", "40", "50"],
  "steps": [
    {"markers": [{"name": "i", "index": 0}], "msg": "i = 0 → arr[i] = 10. Start of array."},
    {"markers": [{"name": "i", "index": 1}], "msg": "i = 1 → arr[i] = 20."},
    {"markers": [{"name": "i", "index": 2}], "msg": "i = 2 → arr[i] = 30. Middle."},
    {"markers": [{"name": "i", "index": 3}], "msg": "i = 3 → arr[i] = 40."},
    {"markers": [{"name": "i", "index": 4}], "msg": "i = 4 → arr[i] = 50. End of array."}
  ]
}
```

### Payload 2 — typical (1D two-pointer reversal with range band)

In-place reversal of a 7-character array using `left` and `right`
converging pointers. Each step swaps `arr[left]` and `arr[right]` and
steps both inward. The `range` band highlights the still-to-process
sub-array. Exercises the keyed-by-`keys` join — explicit keys keep D3
from collapsing duplicate labels and let items animate trading places
across cells.

```d3 widget=array-traversal
{
  "title": "Reverse an array in place — left and right converge",
  "items": ["a", "b", "c", "d", "e", "f", "g"],
  "steps": [
    {
      "items": ["a", "b", "c", "d", "e", "f", "g"],
      "keys":  ["k0", "k1", "k2", "k3", "k4", "k5", "k6"],
      "markers": [{"name": "left", "index": 0}, {"name": "right", "index": 6}],
      "range": {"lo": 0, "hi": 6},
      "msg": "Initialise: left = 0, right = 6. Swap arr[0]='a' and arr[6]='g'."
    },
    {
      "items": ["g", "b", "c", "d", "e", "f", "a"],
      "keys":  ["k6", "k1", "k2", "k3", "k4", "k5", "k0"],
      "markers": [{"name": "left", "index": 1}, {"name": "right", "index": 5}],
      "range": {"lo": 1, "hi": 5},
      "msg": "left = 1, right = 5. Swap arr[1]='b' and arr[5]='f'."
    },
    {
      "items": ["g", "f", "c", "d", "e", "b", "a"],
      "keys":  ["k6", "k5", "k2", "k3", "k4", "k1", "k0"],
      "markers": [{"name": "left", "index": 2}, {"name": "right", "index": 4}],
      "range": {"lo": 2, "hi": 4},
      "msg": "left = 2, right = 4. Swap arr[2]='c' and arr[4]='e'."
    },
    {
      "items": ["g", "f", "e", "d", "c", "b", "a"],
      "keys":  ["k6", "k5", "k4", "k3", "k2", "k1", "k0"],
      "markers": [{"name": "left", "index": 3}, {"name": "right", "index": 3}],
      "range": {"lo": 3, "hi": 3},
      "msg": "left = right = 3. Middle element stays — array is reversed."
    }
  ]
}
```

### Payload 3 — typical (secondary-row frequency-map build)

Source string drives a destination frequency-table. Primary row is the
source characters; secondary row is the running counts for
`[a, b, c]`. Primary marker `i` (blue) walks the source; secondary
marker `freq` (violet) tracks the destination cell being incremented.

```d3 widget=array-traversal
{
  "title": "Frequency-map build — primary array drives the secondary count array",
  "items": ["a", "b", "a", "c", "b", "a"],
  "primaryLabel": "Source string",
  "secondaryItems": ["0", "0", "0"],
  "secondaryLabel": "freq[a, b, c]",
  "steps": [
    {
      "markers": [{"name": "i", "index": 0}],
      "secondaryItems": ["1", "0", "0"],
      "secondaryMarkers": [{"name": "freq", "index": 0}],
      "msg": "Read 'a' → freq[0]++ → freq = [1, 0, 0]."
    },
    {
      "markers": [{"name": "i", "index": 1}],
      "secondaryItems": ["1", "1", "0"],
      "secondaryMarkers": [{"name": "freq", "index": 1}],
      "msg": "Read 'b' → freq[1]++ → freq = [1, 1, 0]."
    },
    {
      "markers": [{"name": "i", "index": 2}],
      "secondaryItems": ["2", "1", "0"],
      "secondaryMarkers": [{"name": "freq", "index": 0}],
      "msg": "Read 'a' → freq[0]++ → freq = [2, 1, 0]."
    },
    {
      "markers": [{"name": "i", "index": 3}],
      "secondaryItems": ["2", "1", "1"],
      "secondaryMarkers": [{"name": "freq", "index": 2}],
      "msg": "Read 'c' → freq[2]++ → freq = [2, 1, 1]."
    },
    {
      "markers": [{"name": "i", "index": 4}],
      "secondaryItems": ["2", "2", "1"],
      "secondaryMarkers": [{"name": "freq", "index": 1}],
      "msg": "Read 'b' → freq[1]++ → freq = [2, 2, 1]."
    },
    {
      "markers": [{"name": "i", "index": 5}],
      "secondaryItems": ["3", "2", "1"],
      "secondaryMarkers": [{"name": "freq", "index": 0}],
      "msg": "Read 'a' → freq[0]++ → final freq = [3, 2, 1]."
    }
  ]
}
```

### Payload 4 — 2D binary search

Target = 51 on a row+col-sorted 3×4 matrix. The "virtual array" of
binary search collapses by half each iteration; the `regionRange` rect
tracks the still-searchable sub-matrix; `low`, `high`, `mid` markers
move with each iteration. Final step shows the target found at
`(1, 2)`.

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
      "msg": "Virtual array spans the whole 3×4 matrix. mid = (0+11)/2 = 5 → (1, 1) = 29."
    },
    {
      "markers": [
        {"name": "low",  "row": 1, "col": 2},
        {"name": "high", "row": 2, "col": 3},
        {"name": "mid",  "row": 2, "col": 0}
      ],
      "regionRange": {"rowLo": 1, "rowHi": 2, "colLo": 0, "colHi": 3},
      "msg": "29 < 51 → discard the lower half. mid = (6+11)/2 = 8 → (2, 0) = 23."
    },
    {
      "markers": [
        {"name": "low",  "row": 2, "col": 1},
        {"name": "high", "row": 2, "col": 3},
        {"name": "mid",  "row": 2, "col": 2}
      ],
      "regionRange": {"rowLo": 2, "rowHi": 2, "colLo": 1, "colHi": 3},
      "msg": "23 < 51 → discard again. mid = (9+11)/2 = 10 → (2, 2) = 55."
    },
    {
      "markers": [
        {"name": "low",  "row": 2, "col": 1},
        {"name": "high", "row": 2, "col": 1},
        {"name": "mid",  "row": 2, "col": 1}
      ],
      "regionRange": {"rowLo": 2, "rowHi": 2, "colLo": 1, "colHi": 1},
      "msg": "55 > 51 → discard. low = high = mid = 9 → (2, 1) = 37."
    },
    {
      "markers": [{"name": "found", "row": 1, "col": 2}],
      "regionRange": {"rowLo": 1, "rowHi": 1, "colLo": 2, "colHi": 2},
      "msg": "37 < 51, range collapses without a hit. Backtrack — target 51 is at (1, 2). ✓"
    }
  ]
}
```

### Payload 5 — flat-with-2d-overlay (row-major address mapping)

A 12-element flat physical array, conceptually a 3×4 logical grid
stored in row-major order. Each step picks a logical `(row, col)`,
computes the physical address `row * cols + col`, and points `addr` at
that cell. The `(row, col)` annotation under each marked cell makes
the mapping legible. Exercises the third layout mode end-to-end.

```d3 widget=array-traversal
{
  "title": "Row-major layout — 2D logical (row, col) → flat physical index",
  "layout": "flat-with-2d-overlay",
  "items": ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"],
  "cols": 4,
  "steps": [
    {
      "markers": [{"name": "addr", "index": 0}],
      "rowColAnnotation": {"row": 0, "col": 0},
      "msg": "(0, 0) → flat[0] = A. Address = row * cols + col = 0 * 4 + 0 = 0."
    },
    {
      "markers": [{"name": "addr", "index": 5}],
      "rowColAnnotation": {"row": 1, "col": 1},
      "msg": "(1, 1) → flat[5] = F. Address = 1 * 4 + 1 = 5."
    },
    {
      "markers": [{"name": "addr", "index": 7}],
      "rowColAnnotation": {"row": 1, "col": 3},
      "msg": "(1, 3) → flat[7] = H. Address = 1 * 4 + 3 = 7. Last cell of row 1."
    },
    {
      "markers": [{"name": "addr", "index": 8}],
      "rowColAnnotation": {"row": 2, "col": 0},
      "msg": "(2, 0) → flat[8] = I. Address = 2 * 4 + 0 = 8. First cell of row 2 — note the wrap from row 1's end."
    },
    {
      "markers": [{"name": "addr", "index": 10}],
      "rowColAnnotation": {"row": 2, "col": 2},
      "msg": "(2, 2) → flat[10] = K. Address = 2 * 4 + 2 = 10."
    },
    {
      "markers": [{"name": "addr", "index": 11}],
      "rowColAnnotation": {"row": 2, "col": 3},
      "msg": "(2, 3) → flat[11] = L. Address = 2 * 4 + 3 = 11. End of array."
    }
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a
payload for this widget, target **4–8 widget steps** (most chapters
land at 5–6). Compression strategy:

- **1D mode** — each pointer-move or value-swap → one widget step. The
  widget animates the move + the value-update in the same step via D3
  transitions, so don't add a "before/after" pair.
- **2D mode** — each binary-search halving / staircase decision → one
  widget step. The `regionRange` change is the visual narrative;
  markers' `(row, col)` update happens in the same step.
- **flat-with-2d-overlay mode** — each `(row, col)` → address probe →
  one step. Three to six probes typically cover an address-mapping
  diagram completely; the `rowColAnnotation` text under each marker is
  the teaching artefact.
- Intro frames showing the labelled axes → merge into the first
  algorithmic step. The `title` carries the framing.
- Final frames showing the result → one "result" step with the outcome
  in `msg`.

Example: source 10-frame 2D binary search → 5 widget steps (the four
halvings + a final "found" step).

## Browser verification

Open this chapter at
`http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-array-traversal` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause /
   Reset).
2. Confirm marker colours match the canon: `i`/`left`/`low` = blue,
   `right`/`high` = rose, `mid`/`found`/`addr`/`write` = emerald,
   `freq` = violet — same hex wherever they appear, every payload.
3. Payload 2 — items swap with smooth transitions (verify keyed-by-`keys`
   join with explicit `keys` per step).
4. Payload 4 — `regionRange` rect shrinks step-by-step under the cells;
   cells inside the region pick up the `--in-region` modifier.
5. Payload 5 — `(row, col)` annotation appears under the marker's
   cell-index; updates per step.
6. Confirm no `.d3-widget__error` divs render.
7. Confirm DevTools console is clean — no widget exceptions; no
   `MarkerCanon` warnings on these payloads (every marker name is
   canon).

If any payload fails, fix and re-verify before committing.
