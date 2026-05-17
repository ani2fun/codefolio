# Widget Spec — `dp-table`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render a 1D or 2D DP table with row/column labels, animate the cell-fill
order, and overlay either:

- `solver: "bottom-up"` — fill-order arrows + "this cell depends on these
  predecessors" highlights at the moment each cell is computed.
- `solver: "top-down"` — a recursion tree above (or beside) the table
  whose leaves are table cells; memoized hits highlight the cached cell
  rather than recursing.

Either mode produces the same final filled table; the difference is the
**path taken** to fill it, which is the pedagogical point. Every source DP
chapter has *two* lessons (top-down + bottom-up) over the same problem
data, so the widget pivots on `solver` to render both with one schema.

Common decorations:

- `axes` — labelled row and column headers (e.g., `"" "" "a" "b"` for
  string indices, with leading blanks for the 0-prefix used by edit-distance-shaped
  tables).
- `optimalPath` — final-step overlay highlighting the cells that make up
  the recovered answer (e.g., the LCS / edit-distance trace, the knapsack
  back-trace).
- Per-cell `value` ∈ string / int (e.g., `-` for "not yet filled"; `T` /
  `F` for boolean DPs; `(2,'^')` for "value + arrow direction" tuples in
  reconstruction-heavy DPs).

Step semantics: compressed from source's 30-70 frame sequences to 8-12
events. Each event either (a) fills a cell, (b) highlights predecessors,
(c) makes a recursion call (top-down), or (d) returns from a memoized
call.

## 2. Source-diagram inventory

All sources under
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/5.dynamic-programming/`.
24 interactive diagrams in source Phase 14:

| Source file | Line | Frames | Solver | Table shape |
|---|---:|---:|---|---|
| `02-longest-increasing-subsequence/02-understanding-the-top-down-solution-to-the-longest-increasing-subsequence-problem.md` | 47 | 34 | `top-down` | 1D + recursion tree |
| `02-longest-increasing-subsequence/02-understanding-the-top-down-solution-to-the-longest-increasing-subsequence-problem.md` | 133 | 16 | `top-down` | 1D — full-array driver |
| `02-longest-increasing-subsequence/03-understanding-the-bottom-up-solution-to-the-longest-increasing-subsequence-problem.md` | 45 | 22 | `bottom-up` | 1D |
| `03-longest-common-subsequence/02-understanding-the-top-down-solution-to-the-longest-common-subsequence-problem.md` | 51 | 31 | `top-down` | 2D — `lcs(i, j)` |
| `03-longest-common-subsequence/03-understanding-the-bottom-up-solution-to-the-longest-common-subsequence-problem.md` | 59 | 19 | `bottom-up` | 2D |
| `04-longest-common-substring/02-understanding-the-top-down-solution-to-the-longest-common-substring-problem.md` | 67 | 50 | `top-down` | 2D |
| `04-longest-common-substring/03-understanding-the-bottom-up-solution-to-the-longest-common-substring-problem.md` | 69 | 35 | `bottom-up` | 2D |
| `05-edit-distance/02-understanding-the-top-down-solution-to-the-edit-distance-problem.md` | 57 | 68 | `top-down` | 2D — `ed(i, j)` |
| `05-edit-distance/03-understanding-the-bottom-up-solution-to-the-edit-distance-problem.md` | 73 | 36 | `bottom-up` | 2D — fill-order arrows |
| `06-longest-palindromic-subsequence/02-understanding-the-top-down-solution-to-the-longest-palindromic-subsequence-problem.md` | 59 | 24 | `top-down` | 2D over substring `(i, j)` |
| `06-longest-palindromic-subsequence/03-understanding-the-bottom-up-solution-to-the-longest-palindromic-subsequence-problem.md` | 65 | 46 | `bottom-up` | 2D — length-diagonal fill order |
| `07-longest-palindromic-substring/02-understanding-the-top-down-solution-to-the-longest-palindromic-substring-problem.md` | 57 | 47 | `top-down` | 2D — `isPalindrome(i, j)` |
| `07-longest-palindromic-substring/02-understanding-the-top-down-solution-to-the-longest-palindromic-substring-problem.md` | 199 | 39 | `top-down` | 2D — driver |
| `07-longest-palindromic-substring/03-understanding-the-bottom-up-solution-to-the-longest-palindromic-substring-problem.md` | 65 | 34 | `bottom-up` | 2D |
| `08-palindrome-partitioning/02-understanding-the-top-down-solution-to-the-palindrome-partitioning-problem.md` | 57 | 43 | `top-down` | 2D — isPalindrome table |
| `08-palindrome-partitioning/02-understanding-the-top-down-solution-to-the-palindrome-partitioning-problem.md` | 189 | 42 | `top-down` | 1D — `minCuts(i)` |
| `08-palindrome-partitioning/03-understanding-the-bottom-up-solution-to-the-palindrome-partitioning-problem.md` | 65 | 28 | `bottom-up` | 2D — isPalindrome |
| `08-palindrome-partitioning/03-understanding-the-bottom-up-solution-to-the-palindrome-partitioning-problem.md` | 173 | 33 | `bottom-up` | 1D — minCuts |
| `09-0-1-knapsack/02-understanding-the-top-down-solution-to-the-0-1-knapsack-problem.md` | 63 | 36 | `top-down` | 2D — `knap(i, w)` |
| `09-0-1-knapsack/03-understanding-the-bottom-up-solution-to-the-0-1-knapsack-problem.md` | 69 | 62 | `bottom-up` | 2D |
| `10-unbounded-knapsack/02-understanding-the-top-down-solution-to-the-unbounded-knapsack-problem.md` | 65 | 55 | `top-down` | 2D |
| `10-unbounded-knapsack/03-understanding-the-bottom-up-solution-to-the-unbounded-knapsack-problem.md` | 69 | 62 | `bottom-up` | 2D |
| `11-bounded-knapsack/02-understanding-the-top-down-solution-to-the-bounded-knapsack-problem.md` | 61 | 50 | `top-down` | 2D |
| `11-bounded-knapsack/03-understanding-the-bottom-up-solution-to-the-bounded-knapsack-problem.md` | 71 | 60 | `bottom-up` | 2D |

Plus orphan / pattern chapters (no source diagrams):

- `01-linear-dp.md`, `12-optimal-stratergy.md`, `13-boolean-parenthesization.md`,
  `14-matrix-chain-multiplication.md`, `15-pattern-edit-distance.md`,
  `16-pattern-subset-sum.md`, `17-pattern-2d-grid.md`, `18-pattern-prefix-sum.md`,
  `19-practise.md`. Payloads authored from destination prose.

## 3. Destination chapter usage

All under `05-algorithms-by-strategy/05-dynamic-programming/`:

| Destination chapter | Source diagrams | Solver pair |
|---|---:|---|
| `02-longest-increasing-subsequence.md` | 3 | both — 1D |
| `03-longest-common-subsequence.md` | 2 | both — 2D |
| `04-longest-common-substring.md` | 2 | both — 2D |
| `05-edit-distance.md` | 2 | both — 2D |
| `06-longest-palindromic-subsequence.md` | 2 | both — 2D substring |
| `07-longest-palindromic-substring.md` | 3 | both — 2D substring |
| `08-palindrome-partitioning.md` | 4 | both — 1D + 2D combo |
| `09-knapsack.md` (covers 0/1) | 2 | both — 2D |
| Subsequent knapsack variant chapters | 2 each (unbounded + bounded) | both — 2D |
| `15-pattern-edit-distance.md` ... `18-pattern-prefix-sum.md` | 0 (orphan) | author per pattern |

Roughly half the chapters need both `top-down` and `bottom-up` payloads in
sequence (the source pairs them). The widget renders one per fenced
block; the chapter places them under "Top-down" and "Bottom-up" h2s as
already laid out in destination.

## 4. Payload schema sketch

```jsonc
{
  "title": "LCS of \"ab\" and \"acb\" — bottom-up",
  "solver": "bottom-up",         // "top-down" | "bottom-up"
  "dimensions": 2,               // 1 or 2

  // Axis labels. For 1D, only `colLabels` is used.
  "rowLabels": ["", "a", "b"],
  "colLabels": ["", "a", "c", "b"],

  // Per-cell initial value ("" or "-" for "not yet filled").
  "initial": [
    ["0", "0", "0", "0"],
    ["0", "-", "-", "-"],
    ["0", "-", "-", "-"]
  ],

  // Bottom-up: ordered list of fill events.
  // Top-down: a mix of `call` / `return` / `cache-hit` / `fill` events
  //           that traverse a recursion tree authored under `recursionTree`.
  "events": [
    {
      "kind": "fill",
      "cell": [1, 1],
      "value": "1",
      "depends": [[0, 0]],                  // highlight predecessor cells
      "rule": "match → dp[0][0] + 1 = 1",
      "msg": "lcs(\"a\", \"a\") — characters match"
    },
    {
      "kind": "fill",
      "cell": [1, 2],
      "value": "1",
      "depends": [[0, 2], [1, 1]],
      "rule": "no match → max(dp[0][2], dp[1][1]) = max(0, 1) = 1",
      "msg": "lcs(\"a\", \"ac\")"
    }
  ],

  // Top-down only: recursion tree authored as in `decision-tree`.
  "recursionTree": {
    "nodes": [
      {"id": "r-2-3", "label": "lcs(2, 3)"},
      {"id": "r-1-3", "label": "lcs(1, 3)", "parent": "r-2-3"},
      {"id": "r-2-2", "label": "lcs(2, 2)", "parent": "r-2-3"}
    ],
    // events here reuse the same `call` / `return` / `cache-hit` model as
    // `call-stack` but each leaf returns a cell value that lands in the
    // table.
    "events": [
      {"kind": "call",      "nodeId": "r-2-3", "msg": "lcs(2, 3)"},
      {"kind": "call",      "nodeId": "r-1-3", "msg": "Mismatch — recurse lcs(1, 3)"},
      {"kind": "return",    "nodeId": "r-1-3", "value": "1", "cell": [1, 3], "msg": "lcs(1, 3) = 1, cache it"},
      {"kind": "call",      "nodeId": "r-2-2", "msg": "Recurse lcs(2, 2)"},
      {"kind": "cache-hit", "cell": [1, 2], "msg": "lcs(1, 2) already cached"},
      {"kind": "return",    "nodeId": "r-2-2", "value": "1", "cell": [2, 2]},
      {"kind": "return",    "nodeId": "r-2-3", "value": "2", "cell": [2, 3], "msg": "lcs(2, 3) = 2"}
    ]
  },

  // Optional final-state overlay highlighting the recovered answer path.
  "optimalPath": [[2, 3], [2, 2], [1, 1], [0, 0]],
  "optimalPathLabel": "LCS = \"ab\""
}
```

### Canonical cell statuses (closed catalog)

- `empty` — not yet filled (default `-`).
- `filling` — currently being computed this step.
- `filled` — value finalised.
- `depends-on` — predecessor of the cell being computed this step (transient).
- `cached` — top-down memo hit highlight (transient).
- `on-path` — part of the recovered optimal path overlay.

### Solver-specific behaviour

- `bottom-up` — recursionTree is ignored if present. Steps walk
  `events`; each `fill` event animates the cell value-tween (number scrambles
  up to its final value over 250ms), draws transient arrows from
  `depends` cells to the target, and updates the on-cell rule annotation.
- `top-down` — table starts mostly empty (`-`). Steps walk
  `recursionTree.events`; `call` descends the tree; `return` writes the cell
  value and fades the visited tree-branch; `cache-hit` flashes the cell
  green for 400ms without descending. `events` (the table-only tape) is
  optional and ignored in this mode.

## 5. POC payloads

### 5.1 1D bottom-up — LIS

````d3 widget=dp-table
{
  "title": "Longest Increasing Subsequence — bottom-up",
  "solver": "bottom-up",
  "dimensions": 1,
  "colLabels": ["10", "9", "2", "5", "3", "7"],
  "initial": [["1", "1", "1", "1", "1", "1"]],
  "events": [
    {"kind": "fill", "cell": [0, 0], "value": "1", "depends": [], "rule": "base", "msg": "lis(0) = 1"},
    {"kind": "fill", "cell": [0, 1], "value": "1", "depends": [], "rule": "9 ≤ 10 — no extension", "msg": "lis(1) = 1"},
    {"kind": "fill", "cell": [0, 2], "value": "1", "depends": [], "rule": "2 ≤ 10, 2 ≤ 9", "msg": "lis(2) = 1"},
    {"kind": "fill", "cell": [0, 3], "value": "2", "depends": [[0, 2]], "rule": "5 > 2 → 1 + lis(2)", "msg": "lis(3) = 2"},
    {"kind": "fill", "cell": [0, 4], "value": "2", "depends": [[0, 2]], "rule": "3 > 2 → 1 + lis(2)", "msg": "lis(4) = 2"},
    {"kind": "fill", "cell": [0, 5], "value": "3", "depends": [[0, 3], [0, 4]], "rule": "7 > 5, 7 > 3 → 1 + max(lis(3), lis(4))", "msg": "lis(5) = 3"}
  ],
  "optimalPath": [[0, 2], [0, 3], [0, 5]],
  "optimalPathLabel": "LIS = [2, 5, 7]"
}
````

### 5.2 2D bottom-up — LCS of "ab" and "acb"

````d3 widget=dp-table
{
  "title": "LCS of \"ab\" and \"acb\" — bottom-up",
  "solver": "bottom-up",
  "dimensions": 2,
  "rowLabels": ["", "a", "b"],
  "colLabels": ["", "a", "c", "b"],
  "initial": [
    ["0", "0", "0", "0"],
    ["0", "-", "-", "-"],
    ["0", "-", "-", "-"]
  ],
  "events": [
    {"kind": "fill", "cell": [1, 1], "value": "1", "depends": [[0, 0]], "rule": "match a=a → 1 + dp[0][0]", "msg": "dp[1][1] = 1"},
    {"kind": "fill", "cell": [1, 2], "value": "1", "depends": [[0, 2], [1, 1]], "rule": "a≠c → max(dp[0][2], dp[1][1])", "msg": "dp[1][2] = 1"},
    {"kind": "fill", "cell": [1, 3], "value": "1", "depends": [[0, 3], [1, 2]], "rule": "a≠b → max(0, 1)", "msg": "dp[1][3] = 1"},
    {"kind": "fill", "cell": [2, 1], "value": "1", "depends": [[1, 1], [2, 0]], "rule": "b≠a → max(1, 0)", "msg": "dp[2][1] = 1"},
    {"kind": "fill", "cell": [2, 2], "value": "1", "depends": [[1, 2], [2, 1]], "rule": "b≠c → max(1, 1)", "msg": "dp[2][2] = 1"},
    {"kind": "fill", "cell": [2, 3], "value": "2", "depends": [[1, 2]], "rule": "match b=b → 1 + dp[1][2]", "msg": "dp[2][3] = 2 — answer"}
  ],
  "optimalPath": [[2, 3], [1, 2], [1, 1], [0, 0]],
  "optimalPathLabel": "LCS = \"ab\""
}
````

### 5.3 2D top-down with recursion tree — same LCS

````d3 widget=dp-table
{
  "title": "LCS of \"ab\" and \"acb\" — top-down",
  "solver": "top-down",
  "dimensions": 2,
  "rowLabels": ["", "a", "b"],
  "colLabels": ["", "a", "c", "b"],
  "initial": [
    ["-", "-", "-", "-"],
    ["-", "-", "-", "-"],
    ["-", "-", "-", "-"]
  ],
  "recursionTree": {
    "nodes": [
      {"id": "r-2-3",  "label": "lcs(2,3)"},
      {"id": "r-1-3",  "label": "lcs(1,3)", "parent": "r-2-3"},
      {"id": "r-2-2",  "label": "lcs(2,2)", "parent": "r-2-3"},
      {"id": "r-1-2",  "label": "lcs(1,2)", "parent": "r-1-3"},
      {"id": "r-1-2b", "label": "lcs(1,2)", "parent": "r-2-2"}
    ],
    "events": [
      {"kind": "call",      "nodeId": "r-2-3", "msg": "lcs(2, 3) — last chars b=b → 1 + lcs(1, 2)"},
      {"kind": "call",      "nodeId": "r-1-2", "msg": "Recurse lcs(1, 2)"},
      {"kind": "return",    "nodeId": "r-1-2", "value": "1", "cell": [1, 2], "msg": "lcs(1, 2) = 1, cache"},
      {"kind": "return",    "nodeId": "r-2-3", "value": "2", "cell": [2, 3], "msg": "lcs(2, 3) = 2"}
    ]
  },
  "optimalPath": [[2, 3], [1, 2]]
}
````

### 5.4 2D bottom-up — Edit distance "ab" → "ebf"

````d3 widget=dp-table
{
  "title": "Edit distance \"ab\" → \"ebf\" — bottom-up",
  "solver": "bottom-up",
  "dimensions": 2,
  "rowLabels": ["", "e", "b", "f"],
  "colLabels": ["", "a", "b"],
  "initial": [
    ["0", "1", "2"],
    ["1", "-", "-"],
    ["2", "-", "-"],
    ["3", "-", "-"]
  ],
  "events": [
    {"kind": "fill", "cell": [1, 1], "value": "1", "depends": [[0, 0]], "rule": "e≠a → 1 + min(0, 1, 1)", "msg": "ed(e, a) = 1"},
    {"kind": "fill", "cell": [1, 2], "value": "2", "depends": [[0, 1], [1, 1]], "rule": "e≠b → 1 + min(1, 1, 2)", "msg": "ed(e, ab) = 2"},
    {"kind": "fill", "cell": [2, 1], "value": "2", "depends": [[1, 0], [1, 1]], "rule": "b≠a → 1 + min(1, 1, 2)", "msg": "ed(eb, a) = 2"},
    {"kind": "fill", "cell": [2, 2], "value": "1", "depends": [[1, 1]], "rule": "b=b → ed[1][1]", "msg": "ed(eb, ab) = 1"},
    {"kind": "fill", "cell": [3, 1], "value": "3", "depends": [[2, 0], [2, 1]], "rule": "f≠a → 1 + min(2, 2, 3)", "msg": "ed(ebf, a) = 3"},
    {"kind": "fill", "cell": [3, 2], "value": "2", "depends": [[2, 1], [2, 2]], "rule": "f≠b → 1 + min(2, 1, 3)", "msg": "ed(ebf, ab) = 2 — answer"}
  ],
  "optimalPath": [[3, 2], [2, 2], [1, 1], [0, 0]],
  "optimalPathLabel": "2 edits: insert e, insert f"
}
````

### 5.5 2D bottom-up — 0/1 Knapsack (capacity 4)

````d3 widget=dp-table
{
  "title": "0/1 Knapsack — capacity 4, items [(w=1,v=1),(w=3,v=4),(w=2,v=3)]",
  "solver": "bottom-up",
  "dimensions": 2,
  "rowLabels": ["", "item1", "item2", "item3"],
  "colLabels": ["w=0", "w=1", "w=2", "w=3", "w=4"],
  "initial": [
    ["0", "0", "0", "0", "0"],
    ["0", "-", "-", "-", "-"],
    ["0", "-", "-", "-", "-"],
    ["0", "-", "-", "-", "-"]
  ],
  "events": [
    {"kind": "fill", "cell": [1, 1], "value": "1", "depends": [[0, 0]], "rule": "fit (w=1) → max(0, 1+dp[0][0]) = 1", "msg": "dp[1][1] = 1"},
    {"kind": "fill", "cell": [1, 2], "value": "1", "depends": [[0, 1]], "rule": "carry over", "msg": "dp[1][2] = 1"},
    {"kind": "fill", "cell": [1, 3], "value": "1", "depends": [[0, 2]], "rule": "carry over", "msg": "dp[1][3] = 1"},
    {"kind": "fill", "cell": [1, 4], "value": "1", "depends": [[0, 3]], "rule": "carry over", "msg": "dp[1][4] = 1"},
    {"kind": "fill", "cell": [2, 3], "value": "4", "depends": [[1, 0]], "rule": "fit (w=3) → max(1, 4+0) = 4", "msg": "dp[2][3] = 4"},
    {"kind": "fill", "cell": [2, 4], "value": "5", "depends": [[1, 1]], "rule": "fit (w=3) → max(1, 4+dp[1][1]) = 5", "msg": "dp[2][4] = 5"},
    {"kind": "fill", "cell": [3, 4], "value": "5", "depends": [[2, 4], [2, 2]], "rule": "max(dp[2][4], 3+dp[2][2]) = max(5, 4) = 5", "msg": "dp[3][4] = 5 — answer"}
  ],
  "optimalPath": [[3, 4], [2, 4], [1, 1], [0, 0]],
  "optimalPathLabel": "Take items 1 and 2 (value 5)"
}
````

## 6. Closest existing widget to mimic

`ArrayTraversal` is the structural parent for the table:

- `Spec.items` ↔ row 0 of `initial`.
- Per-step `markers` ↔ the `filling` cell + `depends-on` cells.
- Per-step value transitions on cells ↔ the same item-tween machinery
  `ArrayTraversal` uses for swap problems.

The widget extends to 2D by introducing a row dimension (same logic as
the `array-traversal` 2D-mode retrofit noted in the gap audit). When
`solver: "top-down"`, the widget composes with the `call-stack`
recursion-tree mode for the side panel (same parent-pointer schema).

## 7. D3 selections plan

Two top-level groups: `g.dp-table__grid` for the table, optional
`g.dp-table__tree` for the top-down recursion tree.

Grid rendering:

- **Row + column header labels**: one-shot `selectAll("text.dp-table__row-label")`
  / `dp-table__col-label`. Drawn once at mount; no per-step updates.
- **Cells**: `selectAll("g.dp-table__cell").data(cellGrid, c => `${c.r}-${c.c}`)`
  - Enter: append `<g>` with `<rect>` + `<text>` (value). Initial value
    from `initial[r][c]`.
  - Update: rebind class for current cell status (`empty` / `filling` /
    `filled` / `depends-on` / `cached` / `on-path`); transition `<text>`
    via text-tween when the value changes.
- **Dependency arrows** (transient — one step only):
  `selectAll("path.dp-table__dep-arrow").data(currentEvent.depends, ...)`
  drawn as straight lines (or short Béziers when same-row/same-col) from
  predecessor cell-centres to the filling cell. Fade in on event start,
  fade out on next step.
- **Rule annotation** (transient): `selectAll("text.dp-table__rule").data([currentEvent.rule])`
  rendered below the filling cell or in a fixed bottom-strip per step.
- **Optimal-path overlay** (final step): `selectAll("path.dp-table__opt").data([optimalPath])`
  drawn as a polyline through cell centres with a halo; cells in
  `optimalPath` rebind to status `on-path`.

Tree rendering (top-down mode only):

- Composes `LayeredLayout` for `(x, y)` per recursion-tree node.
- Reuses the bubble-up edge-label tween from the `call-stack` spec for the
  `return` event (value travels from leaf up to parent over 400ms before
  landing in the destination cell).
- `cache-hit` event: flashes the target cell green (no tree descent),
  draws a short dashed arc from the calling tree-node to the cell.

Layout helpers:

- `cellXY(r, c)` → top-left of the cell rect.
- `axisOffset` — leading row/col reserved for headers + blank `0` cell.

## 8. Shared abstractions

Reuse:

- `Stepper.hook` over events tape.
- `PayloadDecoder`.
- (Once extracted, per the `call-stack` + `decision-tree` specs):
  - `LayeredLayout` for the recursion tree in top-down mode.
  - Bubble-up `EdgeLabelTween` for return-value animation.
  - `PanelRenderer` for any future sidecar lists (queue / visited).

This widget is the **third** consumer of `LayeredLayout` and `EdgeLabelTween`
(after `call-stack` and `decision-tree`). When all three exist, lift them
into `widgets/internal/` per the methodology's "extract on second consumer"
guidance.

## 9. Estimated build session count

**2 sessions.** Bottom-up table + arrows + optimal-path overlay in
session 1; top-down + recursion-tree composition + cache-hit visuals in
session 2. Schema is broad and the top-down mode adds a whole second
rendering surface; budget the extra session rather than under-deliver.

## 10. POC chapter

Demo book chapter at
`content/cortex/dsa-widget-catalog/06-dp-table.md` exhibits all five
POC payloads above (1D bottom-up, 2D bottom-up, 2D top-down with tree,
edit-distance, knapsack), plus a tiny `2x2` smoke payload that fires every
event kind once (`fill` / `depends` / `cache-hit` / `optimalPath`).

First real production usage is
`05-algorithms-by-strategy/05-dynamic-programming/02-longest-increasing-subsequence.md`
(POC 5.1 + an authored top-down companion) — simplest table (1D, no
recursion-tree branching), validates the schema without the cross-axis
complexity of LCS/edit-distance. Then jump straight to
`03-longest-common-subsequence.md` to validate 2D + both solvers in one
chapter.
