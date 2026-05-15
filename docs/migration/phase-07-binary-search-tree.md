# Phase 7 — Binary Search Tree

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/8.binary-search-tree/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/02-binary-search-tree/`

## Stats

| | Count |
|---|---:|
| Chapters | 13 |
| Source lessons | 77 |
| Interactive diagrams | 29 |
| Destination size | 9,736 lines across 13 files |
| Indicative sessions | 10-12 |

## Widget readiness

Same as Phase 6 — needs a **`tree-traversal` widget** that doesn't
exist yet. Defer widget conversion. Do code/example/problem-statement
alignment now.

The "two-pointer" pattern in BSTs (ch 7.13) uses the inorder-iterator
trick that fits a 2-row `array-traversal` showing the iterator's
current value alongside the target sum — that one widget can be done
with existing infrastructure.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 7.1 | Introduction to Binary Search Trees | 4 | 0 | Concept-only. |
| 7.2 | Height and Balance in BSTs | 7 | 1 | Height computation. |
| 7.3 | Recursive Searching in BSTs | 10 | 7 | Several search variants. |
| 7.4 | Iterative Searching in BSTs | 11 | 7 | **Largest chapter.** |
| 7.5 | Insertion in BSTs | 4 | 2 | Recursive + iterative. |
| 7.6 | Deletion in BSTs | 5 | 6 | Three deletion cases. |
| 7.7 | Constructing a BST | 5 | 4 | Build from preorder / sorted array. |
| 7.8 | Lowest Common Ancestor in BSTs | 2 | 1 | LCA leveraging BST property. |
| 7.9 | Iterators in BSTs | 5 | 8 | **Heavy IDs.** Stack-based inorder iterator. |
| 7.10 | Pattern: Sorted Traversal | 6 | 2 | Inorder = sorted. |
| 7.11 | Pattern: Reversed Sorted Traversal | 6 | 2 | Reverse inorder. |
| 7.12 | Pattern: Range Postorder | 6 | 2 | Bounded postorder. |
| 7.13 | Pattern: Two Pointer | 6 | 3 | BST iterator + reverse iterator. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 7.1 + 7.2 (Intro + Height/Balance) |
| 2 | Ch 7.3 Recursive Searching — first half |
| 3 | Ch 7.3 finish + Ch 7.4 Iterative Searching concept |
| 4 | Ch 7.4 Iterative Searching — first half problems |
| 5 | Ch 7.4 Iterative Searching — second half problems |
| 6 | Ch 7.5 Insertion + Ch 7.6 Deletion |
| 7 | Ch 7.7 Constructing + Ch 7.8 LCA |
| 8 | Ch 7.9 Iterators |
| 9 | Ch 7.10 Sorted Traversal + Ch 7.11 Reversed Sorted |
| 10 | Ch 7.12 Range Postorder |
| 11 | Ch 7.13 Two Pointer (CAN do widget — `array-traversal` 2-row) |
| 12 | Phase verification + status |

## Session task templates

### Session 1

```
Phase 7 — Binary Search Tree, Session 1.

Ch 7.1 Introduction + Ch 7.2 Height and Balance. Align height(),
isBalanced(), and the recursive helpers source uses. Source uses
`max(leftHeight, rightHeight) + 1` — destination must match exactly.

Defer tree-traversal widgets. One commit per problem.
```

### Sessions 2-5 (Searching)

```
Phase 7 — Binary Search Tree, Session <N>.

Chapter <7.3 Recursive / 7.4 Iterative>. Source's helpers usually
have signatures like `searchHelper(node, target)` for recursive and
explicit `while (current != null)` for iterative. Match the
parameter order and return type exactly.

For Ch 7.4 floor / ceiling / predecessor / successor problems: pay
attention to which direction source advances when the value
matches — these are easy to get backwards.
```

### Sessions 6-7 (Mutations + Construction)

```
Phase 7 — Binary Search Tree, Session <N>.

Chapter <7.5 Insertion / 7.6 Deletion / 7.7 Construction / 7.8 LCA>.

For Ch 7.6 Deletion: source's three-case structure
(no children / one child / two children) MUST be mirrored —
destination cannot collapse cases. The "two children" case typically
finds the inorder successor; match source's helper.
```

### Session 8 (Iterators)

```
Phase 7 — Binary Search Tree, Session 8.

Ch 7.9 BST Iterators. Source maintains a stack of nodes;
`hasNext()` / `next()` operations have specific invariants. Pay
attention to the eager-push-left-spine pattern at construction time
and after each `next()`.

Defer widget conversion (would need tree + stack visualisation).
```

### Sessions 9-11 (Pattern chapters)

```
Phase 7 — Binary Search Tree, Session <N>.

Chapter <7.10-7.13>. Full code alignment. For Ch 7.13 Two Pointer:
this is the one chapter where `array-traversal` 2-row widget DOES
fit — represent the forward iterator on top, reverse iterator on
bottom, advancing each based on the sum comparison.
```

### Session 12 (closeout)

```
Phase 7 — Binary Search Tree, Session 12 (closeout).

Phase verification + deferred-widgets status note.
```

## Gotchas

- **Successor / predecessor signs** — easy to flip. `successor`
  finds the next-larger node (right subtree's leftmost OR ancestor
  via right-link parent); `predecessor` is mirror. Match source.
- **In-order = sorted order** — many BST problems exploit this.
  Source's pattern chapters lean heavily on it; don't lose the
  inorder-traversal connection in destination's prose.
- **Iterative search loop direction** — `if (target < node.val)
  node = node.left; else if ...` Source's branch order matters for
  consistency with the recursive variant. Match exactly.
