# Phase 6 — Binary Tree

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/7.binary-tree/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/01-binary-tree/`

## Stats

| | Count |
|---|---:|
| Chapters | 18 |
| Source lessons | 109 |
| Interactive diagrams | 32 |
| Destination size | 9,993 lines across 18 files |
| Indicative sessions | 12-15 |

Largest phase by chapter count (18). **Most chapters are small (3-7
lessons each)** so individual sessions can cover multiple chapters.

## Widget readiness — IMPORTANT

Tree visualisations need a **`tree-traversal` widget** that doesn't
exist yet. The existing `array-traversal` widget cannot represent:
- Tree structure (parent/child edges)
- Recursive traversal animation (call stack + current node)
- Level-order traversal (queue + visited level)

**Recommendation**: defer all interactive-diagram widget conversion
in this phase to a future widget-build arc. Do code + examples +
problem-statement alignment now. Track deferred widget conversions
in the phase's closeout commit.

For the 32 interactive diagrams in this phase, only a handful might
fit `array-traversal` — specifically the array-implementation chapter
(6.2) where the tree is laid out as a flat array.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 6.1 | Introduction to Binary Trees | 3 | 0 | Concept-only; no widgets to convert. |
| 6.2 | Array Implementation of Binary Trees | 4 | 0 | Index-based parent/child. `array-traversal` could fit. |
| 6.3 | Linked-List Implementation of Binary Trees | 3 | 0 | TreeNode with `left`/`right`. |
| 6.4 | Recursive Traversals (preorder/inorder/postorder) | 7 | 3 | Three recursive traversals. |
| 6.5 | Iterative Traversals | 9 | 12 | **Heavy IDs.** Stack-based iteration. |
| 6.6 | Constructing a Binary Tree | 7 | 2 | Build from preorder+inorder etc. |
| 6.7 | Insertion in Binary Trees | 10 | 4 | Level-order insertion. |
| 6.8 | Pattern: Preorder Traversal (Stateless) | 6 | 2 | Pattern intro. |
| 6.9 | Pattern: Preorder Traversal (Stateful) | 6 | 2 | Carry state down. |
| 6.10 | Pattern: Postorder Traversal (Stateless) | 8 | 2 | Pattern intro. |
| 6.11 | Pattern: Postorder Traversal (Stateful) | 9 | 2 | Carry state up. |
| 6.12 | Pattern: Root-to-Leaf (Stateless) | 6 | 2 | Path enumeration. |
| 6.13 | Pattern: Root-to-Leaf (Stateful) | 6 | 2 | Path + aggregation. |
| 6.14 | Pattern: Level-Order Traversal | 7 | 2 | BFS-on-tree. |
| 6.15 | Pattern: Level-Order Traversal Columns | 4 | 0 | Vertical column view. |
| 6.16 | Pattern: Lowest Common Ancestor | 7 | 2 | LCA in arbitrary tree. |
| 6.17 | Pattern: Simultaneous Traversal | 6 | 2 | Two trees in parallel. |
| 6.18 | Practice: Mix Traversals | 1 | 0 | Compound problem set. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 6.1 + 6.2 + 6.3 (Intro + Implementations) |
| 2 | Ch 6.4 Recursive Traversals (full — 7 lessons, 3 IDs) |
| 3 | Ch 6.5 Iterative Traversals — first half |
| 4 | Ch 6.5 Iterative Traversals — second half |
| 5 | Ch 6.6 Constructing Binary Tree |
| 6 | Ch 6.7 Insertion |
| 7 | Ch 6.8 + 6.9 Preorder patterns (stateless + stateful) |
| 8 | Ch 6.10 + 6.11 Postorder patterns |
| 9 | Ch 6.12 + 6.13 Root-to-Leaf patterns |
| 10 | Ch 6.14 + 6.15 Level-Order patterns |
| 11 | Ch 6.16 LCA |
| 12 | Ch 6.17 Simultaneous Traversal |
| 13 | Ch 6.18 Practice + phase verification |

## Session task templates

### Session 1 (Intro + Implementations)

```
Phase 6 — Binary Tree, Session 1.

Cover Ch 6.1-6.3 (Intro + Array Implementation + LL Implementation).
For Ch 6.2 Array Implementation: align parent/child index helpers
(`parent(i) = (i-1)/2`, `left(i) = 2*i+1`, `right(i) = 2*i+2`)
verbatim to source. Convert any flat-array view interactive diagrams
to `array-traversal` widgets.

For Ch 6.3 LL Implementation: align TreeNode definition shape
across all 5 langs.

One commit per chapter (these are small).
```

### Sessions 2-4 (Traversals)

```
Phase 6 — Binary Tree, Session <N>.

Chapter <6.4 Recursive / 6.5 Iterative>. Source's recursive
traversal helpers are typically named `preorder_helper`,
`inorder_helper`, `postorder_helper` — destination must extract the
same helpers. For iterative: source uses an explicit Stack<TreeNode>
— match the exact push/pop ordering for correctness.

Defer tree-traversal widget conversion until the widget is built.
One commit per problem.
```

### Sessions 7-12 (Pattern chapters)

```
Phase 6 — Binary Tree, Session <N>.

Chapters <6.X>+<6.Y> (Preorder / Postorder / Root-to-Leaf / etc.).
Two small chapters per session — align problem code in all 5 langs.

Pay attention to the "stateless vs stateful" distinction in source:
stateless patterns return a list; stateful carry a running variable
(sum, count, max) through the recursion via parameter or class field.

Defer widget conversion. One commit per problem.
```

### Session 13 (closeout)

```
Phase 6 — Binary Tree, Session 13 (closeout).

Ch 6.18 Practice + phase verification. End with a deferred-widgets
status: list each chapter that has interactive diagrams pending
tree-traversal-widget build, so the future widget-building arc has a
work list.
```

## Gotchas

- **`TreeNode` definition** — destination's runnable blocks must
  define it inline per language. Source comments it out (judge
  platform provides). Match destination's existing TreeNode shape
  but use source's algorithm.
- **Recursive vs iterative naming** — `preorderTraversal` is the
  user-facing function; `preorderHelper` is the recursive helper.
  Match this naming verbatim.
- **`StringBuilder` / `List<Integer>` / `Stack<TreeNode>`** —
  source's data-structure choices in Java/C++ matter; destination
  must use the same types.
- **Pattern chapter "stateless vs stateful"** — these are
  pedagogically distinct patterns; source's classification matters
  for how the helper function signature looks. Match source's
  signature shape exactly.
