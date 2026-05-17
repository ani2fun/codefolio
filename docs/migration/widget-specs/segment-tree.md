# Widget Spec — `segment-tree`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render a segment tree over a small array (4–16 leaves), with every
internal node labelled by its range `[l, r]` and its aggregate value
(sum / min / max / gcd / xor, per payload). Step controls scrub through
either a **point update** (write a new value at a leaf, then walk up
refreshing aggregates) or a **range query** (walk the tree from the
root, descending into children, returning aggregates from
fully-inside subranges and recursing through partial-overlap nodes).
Visited nodes light up by role: green for "fully inside, return
aggregate", amber for "partial overlap, recurse", slate for "no
overlap, skip".

For chapters that introduce lazy propagation, the widget grows a
second overlay: per-node lazy tags rendered as a small badge in the
top-right of each node, with a "push" animation that shows the lazy
value flowing from a parent down into its two children when the parent
is visited and needed to clear its pending delta.

The widget's central pedagogical job is to make the chapter's
"O(log n) per query / per update" claim visible — at every step, the
number of touched nodes per level is at most a small constant, and
the tree depth is `~log₂ n`, so even the most expensive operation
touches a handful of nodes out of ~4n.

The widget covers the orphan chapter
[`03-trees/09-segment-tree/01-introduction-to-segment-trees.md`](../../../content/cortex/data-structures-and-algorithms/03-trees/09-segment-tree/01-introduction-to-segment-trees.md),
specifically "The segment tree shape", "Build, query, update", and
"Lazy propagation: range updates in O(log n)".

## 2. Source-diagram inventory

0 — orphan widget; payloads derived from destination chapter prose.

## 3. Destination chapter usage

Four natural insertion points, mapped to four payloads:

1. **In "The segment tree shape"** — a static instance showing the
   tree for `[1, 2, 3, 4, 5, 6, 7, 8]` with sum aggregates; a single
   step that just renders the tree (no animation). Replaces the
   existing mermaid block with a payload-driven render that the next
   widgets re-use.
2. **In "Build, query, update" — point update** — change `A[5]` from
   `6` to `20`; widget steps from the leaf upward, refreshing
   internal-node sums; the running total of touched nodes per step
   sits in a margin gutter.
3. **In "Build, query, update" — range query** — sum of `A[2..5]`;
   widget walks from the root, descending into partial-overlap
   children, returning aggregates from fully-inside `[2..3]` and
   `[4..5]` nodes. The collected aggregates accumulate in a "running
   answer" badge on the right.
4. **In "Lazy propagation"** — add `10` to every element of
   `A[3..5]`; widget shows lazy tags being placed on the two
   fully-inside subtrees `[3..3]` (a leaf) and `[4..5]` (an internal
   node, not recursed into), with the parent of `[3..3]` and `[4..5]`
   updated; then a subsequent range query for `sum(A[0..7])` that
   traverses and pushes lazy values down as it goes.

## 4. Payload schema sketch

```typescript
{
  title?: string;
  // The backing array. Length should be small enough that the rendered
  // tree fits the chapter column: 4–16 is the sweet spot.
  array: number[];
  // The aggregate function the tree maintains. The widget owns the
  // operation; authors pick a name.
  aggregate: "sum" | "min" | "max" | "gcd" | "xor";
  // The animation script. Each entry is one operation; the widget
  // expands it into a sequence of steps internally (the descent /
  // ascent walk, one node per step).
  operations: Array<
    | { kind: "build"; }                                          // initial render
    | { kind: "point-update"; index: number; newValue: number; }  // walk up from leaf
    | { kind: "range-query"; lo: number; hi: number; }            // walk down, gather, return
    | { kind: "range-update"; lo: number; hi: number; delta: number; }  // lazy
  >;
  // Default false. When true, every internal node renders a lazy badge
  // (initially 0); range-update and range-query both interact with it
  // and the per-step caption explains the push.
  showLazy?: boolean;
  // Default true. The right-margin gutter showing "nodes touched this
  // operation: k / 4n" — drives home the O(log n) cost.
  showCostGutter?: boolean;
}
```

## 5. POC payloads

Initial tree render — sum aggregate:

````markdown
```d3 widget=segment-tree
{
  "title": "Segment tree over [1,2,3,4,5,6,7,8] — sum aggregate",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "aggregate": "sum",
  "operations": [{ "kind": "build" }]
}
```
````

Point update walk:

````markdown
```d3 widget=segment-tree
{
  "title": "Point update — A[5] = 20",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "aggregate": "sum",
  "operations": [
    { "kind": "build" },
    { "kind": "point-update", "index": 5, "newValue": 20 }
  ]
}
```
````

Range query:

````markdown
```d3 widget=segment-tree
{
  "title": "Range query — sum(A[2..5])",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "aggregate": "sum",
  "operations": [
    { "kind": "build" },
    { "kind": "range-query", "lo": 2, "hi": 5 }
  ]
}
```
````

Lazy propagation — range update then range query:

````markdown
```d3 widget=segment-tree
{
  "title": "Lazy propagation — add 10 to A[3..5], then sum(A[0..7])",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "aggregate": "sum",
  "showLazy": true,
  "operations": [
    { "kind": "build" },
    { "kind": "range-update", "lo": 3, "hi": 5, "delta": 10 },
    { "kind": "range-query",  "lo": 0, "hi": 7 }
  ]
}
```
````

Min aggregate over a different array:

````markdown
```d3 widget=segment-tree
{
  "title": "Min-segment-tree over [7,2,9,4,1,8,3,6]",
  "array": [7, 2, 9, 4, 1, 8, 3, 6],
  "aggregate": "min",
  "operations": [
    { "kind": "build" },
    { "kind": "range-query", "lo": 1, "hi": 5 }
  ]
}
```
````

## 6. Closest existing widget to mimic

The closest catalog widget is `linked-list` — both have step-driven
discrete renders with structured graphs of nodes and per-step role
highlights. The key shape differences:

- Two-dimensional layout (depth × position-within-depth) via
  `d3.tree()`, similar to the planned `recurrence-tree` widget;
  these two widgets share their layout primitive but differ on what
  the per-node label means and how steps drive highlights.
- Per-step highlights are *role-coloured* (fully-inside green,
  partial-overlap amber, no-overlap slate, lazy-push violet) rather
  than the marker-positioning scheme of `linked-list`.
- Per-node values *change across steps* (point updates, lazy
  propagation), with smooth value transitions.

Reuse the React + D3 boundary from `LinkedList.scala`: React owns the
host `<div>`, step controls, running-answer/lazy/cost-gutter HTML
elements; D3 owns the `<svg>` containing the tree.

## 7. D3 selections plan

Five layers inside the `<svg>`:

- `<g class="segment-tree__edges">` — keyed `${parent.idx}→${child.idx}`;
  drawn once at build, no per-step changes.
- `<g class="segment-tree__nodes">` — keyed by node index (the flat
  `1, 2, 3, …` indexing from the chapter's `4n` array layout). Each
  node is a rounded `<rect>` containing two `<text>` elements
  (range label `[l..r]` and aggregate value). On step change the
  role-colour class swaps via `.attr("class", ...)`; aggregate value
  transitions via a number tween.
- `<g class="segment-tree__lazy">` — per-node lazy badge,
  conditionally rendered when `showLazy: true`. Small `<g>` in the
  node's top-right; shown when `lazy[node] !== 0`, fade-in/out as it
  changes.
- `<g class="segment-tree__push-animation">` — transient overlay drawn
  during a lazy-push step: a small label moves from parent down to
  each child along the edge. Implemented as a `<g>` with `<text>` +
  `<rect>` that animates its `transform`.
- `<g class="segment-tree__query-path">` — overlay showing the
  current operation's traversed path, drawn as thicker strokes on the
  visited edges; cleared at operation boundaries.

The right-margin cost gutter and the running-answer/lazy badges are
plain React-rendered HTML elements alongside the SVG, not part of it.

## 8. Shared abstractions

- **`PayloadDecoder`** — parses the closed-enum `aggregate`, the
  `array`, and the `operations` array (each operation validated against
  its `kind`'s schema).
- **`Stepper.scala`** — reuse. Step count is the sum, across all
  operations, of (visited nodes + 1 for the operation's enter and 1
  for exit).
- **An internal `SegmentTreeSimulator`** inside the widget module: a
  pure function that takes `array, aggregate, operations[]` and
  returns the full per-step state sequence (tree values + lazy values
  + highlighted-node-id + caption) needed to drive the render. Lives
  in the widget module; not promoted until a second tree widget (e.g.
  `fenwick-tree`) reuses meaningful chunks.

## 9. Estimated build session count

**2 build sessions**:

- Session 1: Scala module skeleton with parsing, `d3.tree()` layout,
  the simulator for `build` + `point-update` + `range-query`, the
  three primary payloads working. Skip lazy propagation entirely.
- Session 2: lazy-propagation simulator extensions, the lazy-badge
  overlay, the push-animation `<g>`, polish on the cost gutter and
  running-answer badge, browser smoke pass on all four payloads.

Risk factors: the chapter's `4n` array layout means index 1 is the
root, `2k / 2k+1` are children. The widget's simulator must follow the
same indexing scheme (so authors who use the widget alongside a code
walk-through see consistent node numbers between code and visual).
The lazy-propagation push animation is the trickiest piece — it must
visually distinguish "lazy applied to my aggregate" from "lazy pushed
to children"; the chapter explicitly walks both, so the widget needs
to too. The chapter caps array length at 8 in its example; the widget
allows up to 16 but recommends 8 for chapter use (anything larger
crowds the leaves).

## 10. POC chapter

`content/cortex/dsa-widget-catalog/03-trees/segment-tree.md` — a new
chapter under the demo book exhibiting the five payloads with prose
contextualising each operation. Created as part of the widget build
session; serves as the canonical authoring reference.
