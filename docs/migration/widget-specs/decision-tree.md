# Widget Spec — `decision-tree`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise backtracking-style enumeration as a tree of choices:

- Root = the empty partial solution.
- Each child edge = a choice (include/exclude an item, pick a digit, drop a
  character, step into a grid cell).
- A DFS walk over this tree corresponds 1:1 to the call stack of a
  recursive backtracking routine.

The widget animates the walk one step at a time: descent (highlight the
edge taken, push the partial onto a "current path" ribbon), prune
(strike-through the subtree being skipped), undo (pop the path back to the
parent, fade the explored subtree), and accept (mark the leaf as a solution
and append the partial to the solutions list).

Step semantics:

- One `enter` step per child descended.
- One `prune` step per subtree skipped (with reason: dup / dead end /
  constraint violation).
- One `leaf` step per terminal — accepted (added to solutions) or rejected.
- One `backtrack` step per pop.

Compressed from source's 30-60 frame sequences to 8-12 meaningful events.

## 2. Source-diagram inventory

All sources under
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/2.backtracking/`.
13 interactive diagrams in source Phase 11:

| Source file | Line | Frames | What |
|---|---:|---:|---|
| `01-introduction-to-backtracking/01-overview-of-backtracking.md` | 34 | 8 | Build a potential solution by filling some values (intro) |
| `01-introduction-to-backtracking/01-overview-of-backtracking.md` | 64 | 4 | Backtrack and update 0 → 1 (undo example) |
| `01-introduction-to-backtracking/01-overview-of-backtracking.md` | 78 | 57 | Build a solution + backtrack at the end (the big example) |
| `02-pattern-unconditional-enumeration/01-understanding-the-unconditional-enumeration-pattern.md` | 39 | 18 | Initial → all solutions enumeration overview |
| `02-pattern-unconditional-enumeration/01-understanding-the-unconditional-enumeration-pattern.md` | 115 | 49 | Enumerate all solutions (deep walkthrough) |
| `02-pattern-unconditional-enumeration/02-identifying-the-unconditional-enumeration-pattern.md` | 45 | 42 | Find all unique subsets of an array |
| `03-pattern-conditional-enumeration/01-understanding-the-conditional-enumeration-pattern.md` | 41 | 18 | Initial → all solution states overview |
| `03-pattern-conditional-enumeration/01-understanding-the-conditional-enumeration-pattern.md` | 119 | 49 | Enumerate all solutions (deep walkthrough) |
| `03-pattern-conditional-enumeration/02-identifying-the-conditional-enumeration-pattern.md` | 56 | 41 | Find all well-formed parentheses with n=2 pairs |
| `04-pattern-backtracking-search/01-understanding-the-backtracking-search-pattern.md` | 31 | 12 | Search for a solution state (intro) |
| `04-pattern-backtracking-search/01-understanding-the-backtracking-search-pattern.md` | 63 | 17 | Backtrack with `fInverse` / `gInverse` on dead end |
| `04-pattern-backtracking-search/01-understanding-the-backtracking-search-pattern.md` | 145 | 32 | Search for a solution from initial state |
| `04-pattern-backtracking-search/02-identifying-the-backtracking-search-pattern.md` | 71 | 42 | Find path to (2, 2) on a grid |

Plus reuse (no source diagrams):

- `05-algorithms-by-strategy/02-divide-and-conquer.md` — orphan; could
  reuse for "split-recurse-combine" decision tree (probably better in
  `call-stack` recursion-tree mode; revisit).
- Permutation chapter — author-added beyond source.

## 3. Destination chapter usage

| Destination chapter | Diagrams | Notes |
|---|---:|---|
| `05-algorithms-by-strategy/04-backtracking/01-introduction-to-backtracking.md` | 3 | Source's 3 intro diagrams (intuition + undo example + big build) |
| `05-algorithms-by-strategy/04-backtracking/02-pattern-unconditional-enumeration.md` | 3 | Source's 3 — generic enumeration + subsets |
| `05-algorithms-by-strategy/04-backtracking/03-pattern-conditional-enumeration.md` | 3 | Source's 3 — generic enumeration + well-formed parens (the canonical prune-on-constraint example) |
| `05-algorithms-by-strategy/04-backtracking/04-pattern-backtracking-search.md` | 4 | Source's 4 — search intro + backtrack with inverse + full search + grid path |

The grid path diagram (`04-pattern-backtracking-search/02`, source line 71)
is *also* renderable in `graph-explorer` with `layout: "grid"`. The
decision-tree representation shows the **enumeration shape** (which cells
were tried); the graph view shows the **path** on the grid itself. The
backtracking chapter benefits from both, presented side by side.

## 4. Payload schema sketch

The tree is authored as nodes + parent pointers (no nested arrays — keeps
the schema flat and JSON-diffable). The walk is a sequence of events that
reference node ids.

```jsonc
{
  "title": "Subsets of [1, 2, 3]",
  "kind": "enumeration",         // "enumeration" | "search" — affects leaf semantics
  "nodes": [
    {"id": "root",  "label": "[]"},
    {"id": "n1",    "label": "[1]",     "parent": "root", "edgeLabel": "+1"},
    {"id": "n1-2",  "label": "[1,2]",   "parent": "n1",   "edgeLabel": "+2"},
    {"id": "n1-2-3","label": "[1,2,3]", "parent": "n1-2", "edgeLabel": "+3"},
    {"id": "n1-3",  "label": "[1,3]",   "parent": "n1",   "edgeLabel": "+3"},
    {"id": "n2",    "label": "[2]",     "parent": "root", "edgeLabel": "+2"},
    {"id": "n2-3",  "label": "[2,3]",   "parent": "n2",   "edgeLabel": "+3"},
    {"id": "n3",    "label": "[3]",     "parent": "root", "edgeLabel": "+3"}
  ],
  "events": [
    {"kind": "enter",     "nodeId": "root",   "msg": "Start with []"},
    {"kind": "enter",     "nodeId": "n1",     "msg": "Choose 1 → [1]"},
    {"kind": "enter",     "nodeId": "n1-2",   "msg": "Choose 2 → [1, 2]"},
    {"kind": "enter",     "nodeId": "n1-2-3", "msg": "Choose 3 → [1, 2, 3]"},
    {"kind": "leaf",      "nodeId": "n1-2-3", "accept": true, "msg": "Record [1, 2, 3]"},
    {"kind": "backtrack", "nodeId": "n1-2",   "msg": "Unchoose 3"},
    {"kind": "backtrack", "nodeId": "n1",     "msg": "Unchoose 2"},
    {"kind": "enter",     "nodeId": "n1-3",   "msg": "Choose 3 → [1, 3]"},
    {"kind": "leaf",      "nodeId": "n1-3",   "accept": true, "msg": "Record [1, 3]"},
    {"kind": "prune",     "nodeId": "n2",     "reason": "dup",       "msg": "Skip already-tried subtree"},
    {"kind": "enter",     "nodeId": "n3",     "msg": "Choose 3 → [3]"},
    {"kind": "leaf",      "nodeId": "n3",     "accept": true, "msg": "Record [3]"}
  ],
  // Optional sidecars rendered to the right of the tree, updating per step.
  "panels": [
    {"name": "path",      "kind": "list"},   // current partial — pushed/popped per enter/backtrack
    {"name": "solutions", "kind": "list"}    // appended per `leaf` with accept: true
  ]
}
```

### Canonical event kinds (closed catalog)

| Kind | Effect |
|---|---|
| `enter` | Highlight edge from parent → node; mark node `active`; push `node.label` onto the path panel. |
| `leaf` | Mark node `accepted` (green halo + checkmark) when `accept: true`; otherwise `rejected` (grey dim). When accepted, append `node.label` to solutions panel. |
| `prune` | Mark node + its entire subtree `pruned` (strike-through, reduced opacity); render a small badge with `reason` ∈ `dup` / `dead-end` / `constraint`. Do NOT push onto the path. |
| `backtrack` | Pop the path panel; mark the node we're moving up to `active`; previously-visited subtree of the popped child stays drawn but dims to `popped`. |

### Canonical node statuses

`unseen` / `active` / `visited` / `accepted` / `rejected` / `pruned` /
`popped`. The widget derives status per step by replaying events; authors
do not author `nodeStates` directly.

## 5. POC payloads

### 5.1 Subsets of [1, 2, 3] — the canonical unconditional enumeration

````d3 widget=decision-tree
{
  "title": "Subsets of [1, 2, 3]",
  "kind": "enumeration",
  "nodes": [
    {"id": "root",    "label": "[]"},
    {"id": "n1",      "label": "[1]",     "parent": "root",   "edgeLabel": "+1"},
    {"id": "n1-2",    "label": "[1,2]",   "parent": "n1",     "edgeLabel": "+2"},
    {"id": "n1-2-3",  "label": "[1,2,3]", "parent": "n1-2",   "edgeLabel": "+3"},
    {"id": "n1-3",    "label": "[1,3]",   "parent": "n1",     "edgeLabel": "+3"},
    {"id": "n2",      "label": "[2]",     "parent": "root",   "edgeLabel": "+2"},
    {"id": "n2-3",    "label": "[2,3]",   "parent": "n2",     "edgeLabel": "+3"},
    {"id": "n3",      "label": "[3]",     "parent": "root",   "edgeLabel": "+3"}
  ],
  "panels": [
    {"name": "path",      "kind": "list"},
    {"name": "solutions", "kind": "list"}
  ],
  "events": [
    {"kind": "enter",     "nodeId": "root",    "msg": "Start []"},
    {"kind": "leaf",      "nodeId": "root",    "accept": true, "msg": "Record []"},
    {"kind": "enter",     "nodeId": "n1",      "msg": "Choose 1"},
    {"kind": "leaf",      "nodeId": "n1",      "accept": true, "msg": "Record [1]"},
    {"kind": "enter",     "nodeId": "n1-2",    "msg": "Choose 2"},
    {"kind": "leaf",      "nodeId": "n1-2",    "accept": true, "msg": "Record [1, 2]"},
    {"kind": "enter",     "nodeId": "n1-2-3",  "msg": "Choose 3"},
    {"kind": "leaf",      "nodeId": "n1-2-3",  "accept": true, "msg": "Record [1, 2, 3]"},
    {"kind": "backtrack", "nodeId": "n1-2",    "msg": "Unchoose 3"},
    {"kind": "backtrack", "nodeId": "n1",      "msg": "Unchoose 2"},
    {"kind": "enter",     "nodeId": "n1-3",    "msg": "Choose 3"},
    {"kind": "leaf",      "nodeId": "n1-3",    "accept": true, "msg": "Record [1, 3]"},
    {"kind": "backtrack", "nodeId": "n1",      "msg": "Unchoose 3"},
    {"kind": "backtrack", "nodeId": "root",    "msg": "Unchoose 1"},
    {"kind": "enter",     "nodeId": "n2",      "msg": "Choose 2"},
    {"kind": "leaf",      "nodeId": "n2",      "accept": true, "msg": "Record [2]"},
    {"kind": "enter",     "nodeId": "n2-3",    "msg": "Choose 3"},
    {"kind": "leaf",      "nodeId": "n2-3",    "accept": true, "msg": "Record [2, 3]"},
    {"kind": "backtrack", "nodeId": "n2",      "msg": "Unchoose 3"},
    {"kind": "backtrack", "nodeId": "root",    "msg": "Unchoose 2"},
    {"kind": "enter",     "nodeId": "n3",      "msg": "Choose 3"},
    {"kind": "leaf",      "nodeId": "n3",      "accept": true, "msg": "Record [3]"}
  ]
}
````

### 5.2 Well-formed parens with n=2 — pruning on invalid prefix

````d3 widget=decision-tree
{
  "title": "Well-formed parens, n = 2",
  "kind": "enumeration",
  "nodes": [
    {"id": "root",   "label": "\"\""},
    {"id": "lp",     "label": "(",      "parent": "root", "edgeLabel": "("},
    {"id": "lplp",   "label": "((",     "parent": "lp",   "edgeLabel": "("},
    {"id": "lplprp", "label": "(()",    "parent": "lplp", "edgeLabel": ")"},
    {"id": "balanced1", "label": "(())", "parent": "lplprp", "edgeLabel": ")"},
    {"id": "lprp",   "label": "()",     "parent": "lp",   "edgeLabel": ")"},
    {"id": "lprplp", "label": "()(",    "parent": "lprp", "edgeLabel": "("},
    {"id": "balanced2", "label": "()()", "parent": "lprplp", "edgeLabel": ")"},
    {"id": "rp",     "label": ")",      "parent": "root", "edgeLabel": ")"}
  ],
  "panels": [
    {"name": "open",      "kind": "list"},
    {"name": "solutions", "kind": "list"}
  ],
  "events": [
    {"kind": "enter",     "nodeId": "root",      "msg": "Start with empty string"},
    {"kind": "enter",     "nodeId": "lp",        "msg": "Append ( — open=1"},
    {"kind": "enter",     "nodeId": "lplp",      "msg": "Append ( — open=2"},
    {"kind": "enter",     "nodeId": "lplprp",    "msg": "Append ) — open=1"},
    {"kind": "enter",     "nodeId": "balanced1", "msg": "Append ) — open=0"},
    {"kind": "leaf",      "nodeId": "balanced1", "accept": true, "msg": "Record (())"},
    {"kind": "backtrack", "nodeId": "lplprp",    "msg": "Unwind"},
    {"kind": "backtrack", "nodeId": "lplp",      "msg": "Unwind"},
    {"kind": "backtrack", "nodeId": "lp",        "msg": "Unwind to ("},
    {"kind": "enter",     "nodeId": "lprp",      "msg": "Try )"},
    {"kind": "enter",     "nodeId": "lprplp",    "msg": "Append ( — open=1"},
    {"kind": "enter",     "nodeId": "balanced2", "msg": "Append )"},
    {"kind": "leaf",      "nodeId": "balanced2", "accept": true, "msg": "Record ()()"},
    {"kind": "backtrack", "nodeId": "root",      "msg": "All ( branches done"},
    {"kind": "prune",     "nodeId": "rp",        "reason": "constraint", "msg": "Cannot start with ) — close > open"}
  ]
}
````

### 5.3 Permutations of [1, 2] — minimal example for intro chapter

````d3 widget=decision-tree
{
  "title": "Permutations of [1, 2]",
  "kind": "enumeration",
  "nodes": [
    {"id": "root", "label": "[]"},
    {"id": "p1",   "label": "[1]",   "parent": "root", "edgeLabel": "pick 1"},
    {"id": "p12",  "label": "[1,2]", "parent": "p1",   "edgeLabel": "pick 2"},
    {"id": "p2",   "label": "[2]",   "parent": "root", "edgeLabel": "pick 2"},
    {"id": "p21",  "label": "[2,1]", "parent": "p2",   "edgeLabel": "pick 1"}
  ],
  "panels": [
    {"name": "path",      "kind": "list"},
    {"name": "solutions", "kind": "list"}
  ],
  "events": [
    {"kind": "enter",     "nodeId": "root", "msg": "Empty"},
    {"kind": "enter",     "nodeId": "p1",   "msg": "Pick 1"},
    {"kind": "enter",     "nodeId": "p12",  "msg": "Pick 2"},
    {"kind": "leaf",      "nodeId": "p12",  "accept": true, "msg": "Record [1, 2]"},
    {"kind": "backtrack", "nodeId": "p1",   "msg": "Unpick 2"},
    {"kind": "backtrack", "nodeId": "root", "msg": "Unpick 1"},
    {"kind": "enter",     "nodeId": "p2",   "msg": "Pick 2"},
    {"kind": "enter",     "nodeId": "p21",  "msg": "Pick 1"},
    {"kind": "leaf",      "nodeId": "p21",  "accept": true, "msg": "Record [2, 1]"}
  ]
}
````

### 5.4 Search to (2, 2) on a grid — backtracking search kind

````d3 widget=decision-tree
{
  "title": "Find path to (2, 2)",
  "kind": "search",
  "nodes": [
    {"id": "s00",   "label": "(0,0)"},
    {"id": "s01",   "label": "(0,1)", "parent": "s00",  "edgeLabel": "→"},
    {"id": "s02",   "label": "(0,2)", "parent": "s01",  "edgeLabel": "→"},
    {"id": "s12",   "label": "(1,2)", "parent": "s02",  "edgeLabel": "↓"},
    {"id": "s22",   "label": "(2,2)", "parent": "s12",  "edgeLabel": "↓"},
    {"id": "s10",   "label": "(1,0)", "parent": "s00",  "edgeLabel": "↓"},
    {"id": "s11",   "label": "(1,1)", "parent": "s10",  "edgeLabel": "→"}
  ],
  "panels": [{"name": "path", "kind": "list"}],
  "events": [
    {"kind": "enter",     "nodeId": "s00", "msg": "Start (0,0)"},
    {"kind": "enter",     "nodeId": "s01", "msg": "Try → (0,1)"},
    {"kind": "enter",     "nodeId": "s02", "msg": "Try → (0,2)"},
    {"kind": "enter",     "nodeId": "s12", "msg": "Try ↓ (1,2)"},
    {"kind": "enter",     "nodeId": "s22", "msg": "Try ↓ (2,2)"},
    {"kind": "leaf",      "nodeId": "s22", "accept": true, "msg": "Goal reached — return path"},
    {"kind": "prune",     "nodeId": "s10", "reason": "dead-end", "msg": "Already found solution — prune alt branches"}
  ]
}
````

### 5.5 Backtrack-with-inverse — demonstrates undo animation

````d3 widget=decision-tree
{
  "title": "Backtrack via gInverse on dead end",
  "kind": "search",
  "nodes": [
    {"id": "s0",   "label": "init"},
    {"id": "s0a",  "label": "S1",   "parent": "s0",  "edgeLabel": "g"},
    {"id": "s0aa", "label": "S2",   "parent": "s0a", "edgeLabel": "g"},
    {"id": "s0ab", "label": "S3",   "parent": "s0a", "edgeLabel": "g'"}
  ],
  "panels": [{"name": "path", "kind": "list"}],
  "events": [
    {"kind": "enter",     "nodeId": "s0",   "msg": "Initial state"},
    {"kind": "enter",     "nodeId": "s0a",  "msg": "Apply g → S1"},
    {"kind": "enter",     "nodeId": "s0aa", "msg": "Apply g → S2"},
    {"kind": "leaf",      "nodeId": "s0aa", "accept": false, "msg": "Dead end"},
    {"kind": "backtrack", "nodeId": "s0a",  "msg": "Apply gInverse — undo S2"},
    {"kind": "enter",     "nodeId": "s0ab", "msg": "Apply g' → S3"},
    {"kind": "leaf",      "nodeId": "s0ab", "accept": true,  "msg": "Solution found"}
  ]
}
````

## 6. Closest existing widget to mimic

`LinkedList` for the keyed `<g>` node + edge selections with smooth
transforms. The `LinkedList` `cycleTarget` rendering pattern (a curved
Bézier between arbitrary nodes) is the closest analogue for
backtrack-animation arcs.

`Stepper` hook drives the walk over `events`.

Conceptually the closest **future** widget is `call-stack` in
`recursion-tree` mode — same parent-pointer tree, similar bubble-up
visualisations. The two widgets share the candidate `LayeredLayout`
abstraction (see §8); decide during Arc 2 whether they end up sharing more.

## 7. D3 selections plan

Top-level `g.decision-tree__canvas` + (optional) `g.decision-tree__panels`.

Pre-compute per step: `(nodeStatus: Map[id, Status], pathStack: List[id],
solutions: List[String])` by replaying events.

Inside the canvas:

- **Edges** (drawn first): `selectAll("path.decision-tree__edge").data(edges, e => e.childId)`
  - Edge class encodes status of the **child** at the current step
    (`enter` → blue; `prune` → dashed grey strike; `backtrack` → orange
    return-arrow; `popped` → faded).
  - Edge labels (`edgeLabel`): `selectAll("text.decision-tree__edge-label")`
    keyed by the same id.
- **Nodes**: `selectAll("g.decision-tree__node").data(nodes, n => n.id)`
  - Enter: append `<g>` with `<rect>` + `<text>`; transform to `treeLayout(node)`.
  - Update: rebind class for status; transition position only if layout
    changes (it doesn't, in this widget — but the API stays uniform).
  - On `accept`: pulse animation (scale 1 → 1.1 → 1 over 350ms) + green halo.
  - On `prune`: opacity → 0.35, add strike-through `<line>` overlay.
- **Backtrack arc** (transient, lives one step):
  `selectAll("path.decision-tree__backtrack-arc").data([backtrackEdge])` —
  appended on `backtrack` event, fades out by next step. Curved Bézier
  pointing parent-ward.
- **Reason badge** (prune events): small `<g.decision-tree__reason>` near
  the pruned node with `dup` / `dead-end` / `constraint` text.

Panel rendering reuses the same `array` / `list` sidecar contract as
`graph-explorer` (extract into a shared `PanelRenderer` once the second
consumer lands — §8).

Layout: Reingold-Tilford-style tree layout. For ≤ 30 nodes the naive
"x-spread by leaf-count, y by depth" approach is sufficient. Computed once
at mount (tree topology doesn't change between steps).

## 8. Shared abstractions

Reuse:

- `Stepper.hook`, `PayloadDecoder`.

Probable shared (with `call-stack` recursion-tree mode and `dp-table`
top-down mode):

- **LayeredLayout** — parent-pointer tree → `(x, y)` per node.
- **PanelRenderer** — `array` / `list` sidecars, also useful for
  `graph-explorer` and `dp-table`.

Defer extraction until the second consumer lands (don't pre-build shared
modules in Arc 1 — favour duplication, consolidate in Arc 2).

## 9. Estimated build session count

**1 session.** Schema is small; the walk is a straight event replay; the
only non-trivial visual is the transient backtrack arc + the prune
strike-through. No layout recomputation per step.

## 10. POC chapter

Demo book chapter at
`content/cortex/dsa-widget-catalog/05-decision-tree.md` exhibits all
five POC payloads above, plus a tiny 3-node tree showing every single
event kind (`enter` / `leaf accept=true` / `leaf accept=false` /
`prune` / `backtrack`) end-to-end for visual regression sanity.

First real production usage is
`05-algorithms-by-strategy/04-backtracking/02-pattern-unconditional-enumeration.md`
(subsets — POC 5.1). Then the conditional enumeration chapter brings in
the pruning machinery via well-formed parens (POC 5.2).
