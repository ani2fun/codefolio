# Widget Spec — `recurrence-tree`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render the recursion tree of a divide-and-conquer recurrence
`T(n) = a · T(n/b) + f(n)`, with each node labelled by the work it does
at that level and each level's *total* work summed in a margin gutter
beside the tree. The widget's central pedagogical job is to make the
**Master theorem cases** visible: in Case 1 the level-totals grow toward
the leaves (leaves dominate); in Case 2 every level-total is identical
(balanced); in Case 3 the level-totals shrink toward the leaves (root
dominates). A reader looking at the gutter sees the *shape* of where the
work lives, which is exactly how the chapter tells them to think about
the three cases.

Step controls advance the visualisation level-by-level: step 0 shows the
root; step 1 expands one level; step `k` expands to depth `k`; the final
step shows the full tree plus the geometric-series summation in a
summary card below ("`Total = log_b(n) · f(n) = Θ(n log n)`"). For Case 1
and Case 3 the summary card uses the geometric-series formula and
highlights which level wins.

The widget covers the orphan chapter
[`01-foundations/02-recurrence-relations-and-master-theorem.md`](../../../content/cortex/data-structures-and-algorithms/01-foundations/02-recurrence-relations-and-master-theorem.md),
specifically "The recursion-tree method" and "The Master Theorem"
sections. It replaces a long mermaid block (which is static) with an
interactive scrub through the tree's level-by-level expansion.

## 2. Source-diagram inventory

0 — orphan widget; payloads derived from destination chapter prose.

## 3. Destination chapter usage

The chapter has four natural insertion points, mapped to four payloads:

1. **In "The recursion-tree method"** — the merge-sort recurrence
   `T(n) = 2T(n/2) + n` (Case 2). The widget walks the tree from root
   `n` work, to two `n/2` nodes, to four `n/4` nodes, etc., with the
   level-sum gutter showing `n, n, n, n, …` and the summary "every
   level does `n` work, `log n` levels, total `n log n`".
2. **In "The Master Theorem" near the three-case mermaid block** — three
   side-by-side payloads, one per case (root-dominated, balanced,
   leaf-dominated). The chapter's "leaves win / balanced / root wins"
   intuition becomes a visual: the gutter's bar chart skews up
   (Case 1), flat (Case 2), or down (Case 3).
3. **In "Worked examples" — Karatsuba** — `T(n) = 3T(n/2) + n` (Case 1,
   leaf-dominated). Shows the branching factor 3 producing leaves at
   depth `log_2 n`, total `n^(log_2 3) ≈ n^1.585`.
4. **In "Worked examples" — `T(n) = T(n/2) + n`** — Case 3 (root-
   dominated), summing as a geometric series `n + n/2 + n/4 + … ≤ 2n`.

## 4. Payload schema sketch

```typescript
{
  title?: string;
  // The recurrence in standard form. The widget computes the tree's
  // structure from these three numbers plus the function string. Authors
  // do not draw the tree by hand.
  recurrence: {
    a: number;             // branching factor (>= 1)
    b: number;             // shrink factor (> 1)
    // f(n) as a polynomial in n: each term is c * n^p. The widget
    // evaluates f(n/b^k) at every depth k for the level-sum gutter.
    f: Array<{ coefficient: number; exponent: number }>;
    fLabel?: string;       // human-readable label, e.g. "n", "n log n", "1"
  };
  // Starting n. Trees are rendered to depth log_b(n) and stop when the
  // subproblem size <= 1. For trees that would be too wide to render
  // sensibly (a > 3 OR depth > 5), the widget collapses internal layers
  // with an ellipsis row and shows root + first two levels + leaves only.
  n: number;
  // Which Master-theorem case to highlight in the summary card. If
  // omitted, the widget computes the case itself by comparing f(n) to
  // n^(log_b a) symbolically (the f decomposition makes this exact).
  expectedCase?: 1 | 2 | 3;
  // Optional human-readable closed-form to show in the summary; the
  // widget always also computes its own answer.
  closedForm?: string;     // e.g. "Θ(n log n)"
  // Step grouping: which expansions the author wants the reader to land
  // on. Defaults to one step per level. Authors with very deep trees
  // can group "depth 0..3", "depth 4..k", "leaves + summary".
  steps?: Array<{
    expandToDepth: number;
    msg?: string;
  }>;
}
```

## 5. POC payloads

Case 2 — merge sort (balanced):

````markdown
```d3 widget=recurrence-tree
{
  "title": "Merge sort: T(n) = 2T(n/2) + n",
  "recurrence": {
    "a": 2,
    "b": 2,
    "f": [{ "coefficient": 1, "exponent": 1 }],
    "fLabel": "n"
  },
  "n": 16,
  "expectedCase": 2,
  "closedForm": "Θ(n log n)"
}
```
````

Case 1 — Karatsuba (leaf-dominated):

````markdown
```d3 widget=recurrence-tree
{
  "title": "Karatsuba multiplication: T(n) = 3T(n/2) + n",
  "recurrence": {
    "a": 3,
    "b": 2,
    "f": [{ "coefficient": 1, "exponent": 1 }],
    "fLabel": "n"
  },
  "n": 16,
  "expectedCase": 1,
  "closedForm": "Θ(n^log₂ 3) ≈ Θ(n^1.585)"
}
```
````

Case 3 — one-sided halving (root-dominated):

````markdown
```d3 widget=recurrence-tree
{
  "title": "One-sided halving: T(n) = T(n/2) + n",
  "recurrence": {
    "a": 1,
    "b": 2,
    "f": [{ "coefficient": 1, "exponent": 1 }],
    "fLabel": "n"
  },
  "n": 16,
  "expectedCase": 3,
  "closedForm": "Θ(n)"
}
```
````

Strassen (Case 1, larger fan-out for visual contrast):

````markdown
```d3 widget=recurrence-tree
{
  "title": "Strassen: T(n) = 7T(n/2) + n²",
  "recurrence": {
    "a": 7,
    "b": 2,
    "f": [{ "coefficient": 1, "exponent": 2 }],
    "fLabel": "n²"
  },
  "n": 8,
  "expectedCase": 1,
  "closedForm": "Θ(n^log₂ 7) ≈ Θ(n^2.807)"
}
```
````

Binary search (Case 2 with `f(n) = 1`):

````markdown
```d3 widget=recurrence-tree
{
  "title": "Binary search: T(n) = T(n/2) + 1",
  "recurrence": {
    "a": 1,
    "b": 2,
    "f": [{ "coefficient": 1, "exponent": 0 }],
    "fLabel": "1"
  },
  "n": 16,
  "expectedCase": 2,
  "closedForm": "Θ(log n)"
}
```
````

## 6. Closest existing widget to mimic

The closest catalog widget is `linked-list` — both have a step-driven
discrete render with a structured tree/graph of nodes. The key shape
differences:

- Two-dimensional layout (depth × position-within-depth) rather than a
  single row; the widget uses `d3.tree()` for x/y assignment.
- A right-margin gutter showing per-level work as both a number and a
  small horizontal bar (proportional to the level's total work).
- A summary card below the tree (analogous to `linked-list`'s `msg`
  area but persistent across steps once the final step is reached).

Reuse the React + D3 boundary pattern from `LinkedList.scala`: React owns
the host `<div>` + step controls + summary card; D3 owns the `<svg>` for
the tree itself and the gutter.

## 7. D3 selections plan

Five layers inside the `<svg>`:

- `<g class="recurrence-tree__edges">` — keyed `${parent.id}→${child.id}`;
  fade in as new levels expand.
- `<g class="recurrence-tree__nodes">` — keyed by node id (an integer
  index in BFS order); enter with `opacity: 0` and scale-up; update with
  position transitions; exit with fade-out.
- `<g class="recurrence-tree__gutter">` — one row per level showing
  `level k: a^k × f(n/b^k) = <number>` plus a small bar. The gutter is
  recomputed and updated on every step.
- `<g class="recurrence-tree__highlight">` — overlay rect drawn around
  the dominant level once the final step is shown, coloured by case
  (green for Case 1 leaves, amber for Case 2 every-level, rose for
  Case 3 root).
- `<g class="recurrence-tree__summary">` — closed-form label centred
  below the tree on final step.

Layout: `d3.tree().size([width, depth × levelHeight])` for x positions;
levels stack top-down. Branching factor `> 3` triggers a "compact" mode
that draws the first 3 children of each node plus an ellipsis "...
(a − 3 more)" placeholder, keeping the tree under 4× the chapter's text
column width.

## 8. Shared abstractions

- **`PayloadDecoder`** — parses the nested `recurrence` object and
  validates `a >= 1`, `b > 1`, non-empty `f`, `n >= 1`.
- **`Stepper.scala`** — reuse the existing step controller for prev /
  next / play / reset. Default step count is `floor(log_b(n)) + 1`
  (root → leaves → summary).
- **A new tiny `MasterTheoremCaseDetector`** lives inside the module:
  given the symbolic `a, b, f`, returns Case 1 / 2 / 3 plus a one-line
  rationale. Used to validate `expectedCase` against the computed case
  and warn if they disagree (helps authors catch payload mistakes).

## 9. Estimated build session count

**1–2 build sessions**:

- Session 1: Scala module skeleton with payload parsing, the `d3.tree()`
  layout, level-sum gutter, stepper integration, and the three primary
  payloads working in the demo book chapter.
- Session 2 (optional, may fit in 1): compact-mode rendering for high
  branching factors, the case-detection summary card, polish on
  transitions, browser smoke pass.

Risk factors: branching factor > 5 produces visually unintelligible
trees even with compact mode — the widget's authoring docs need to
recommend `a ≤ 4` for chapter use (Karatsuba `a = 3` is the realistic
upper bound). Strassen's `a = 7` is included as a payload but renders in
the "ellipsis after 3 children" compact mode.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/02-foundations/02-recurrence-tree.md` —
a new chapter under the demo book exhibiting the five payloads with
prose contextualising each Master-theorem case. Created as part of the
widget build session; serves as the canonical authoring reference.
