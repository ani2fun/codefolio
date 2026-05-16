# Phase 11 — Backtracking

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/2.backtracking/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/05-algorithms-by-strategy/04-backtracking/`

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `decision-tree` | to-build | ADR pending. Source has 13 Interactive Diagrams — backtracking-tree with branch / prune visualisation, decision-state at each level. Likely extends `binary-tree` (built in Phase 6) with an n-ary children list and "pruned" branch styling |

Per ADR-0006 (updated 2026-05-16): widget builds run as a precursor
session at the start of the phase. Phase 11 builds (or extends
from `binary-tree`) the `decision-tree` widget.

## Stats

| | Count |
|---|---:|
| Chapters | 4 |
| Source lessons | 21 |
| Interactive diagrams | 7 |
| Destination size | 5,410 lines across 4 files |
| Indicative sessions | 4-5 |

One of the smallest phases. Should be doable in a long weekend.

## Widget readiness

Backtracking visualises a **decision tree being explored**. Each
diagram shows the recursion fanout, the constraint check, and the
backtrack step. Same situation as recursion — needs a call-stack-
tree widget, defer for now.

For permutation / combination / subset enumeration, source might
visualise the current path as a growing/shrinking array — this
fits `array-traversal` with the path as items and a marker for the
current decision index.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 11.1 | Introduction to Backtracking | 3 | 3 | Concept + the choose-explore-unchoose template. |
| 11.2 | Pattern: Unconditional Enumeration | 6 | 3 | All permutations / subsets / combinations. |
| 11.3 | Pattern: Conditional Enumeration | 6 | 3 | Constrained enumeration (palindromes, k-sums). |
| 11.4 | Pattern: Backtracking Search | 6 | 4 | N-queens, Sudoku, maze solving. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 11.1 Introduction (full) |
| 2 | Ch 11.2 Unconditional Enumeration (full) |
| 3 | Ch 11.3 Conditional Enumeration (full) |
| 4 | Ch 11.4 Backtracking Search — first half (N-queens etc.) |
| 5 | Ch 11.4 finish + phase verification |

## Session task templates

### Session 1

```
Phase 11 — Backtracking, Session 1.

Ch 11.1 Introduction to Backtracking. Align the choose-explore-
unchoose template code verbatim. Source's template typically has
`path.add(choice); backtrack(path); path.remove(path.size()-1);` —
the explicit remove on backtrack is the key teaching moment.
Preserve it.

Skip widgets. One commit per concept section.
```

### Sessions 2-3 (Enumeration patterns)

```
Phase 11 — Backtracking, Session <N>.

Ch <11.2 Unconditional / 11.3 Conditional> Enumeration. Align
permutation / combination / subset enumeration code. Source's
function signature typically takes `(arr, start, current, result)`
or similar — match the parameter order exactly.

For subsets: source uses `for (int i = start; i < n; i++)` to avoid
duplicates. For permutations: source uses a `used[]` array. Match
per problem.
```

### Sessions 4-5 (Search problems)

```
Phase 11 — Backtracking, Session <N>.

Ch 11.4 Backtracking Search. N-Queens / Sudoku / Word Search etc.
Source's solutions have specific constraint-checking helpers:
- `isValid(board, row, col)` for N-queens
- `isSafe(board, row, col, num)` for Sudoku

Destination must extract every helper. Match the validity-check
direction order (e.g., column-down, diagonal-up-left, diagonal-up-
right for N-queens) exactly.
```

### Session 5 (closeout)

```
Phase 11 — Backtracking, Session 5 (closeout).

Finish any pending Ch 11.4 problems + phase verification.
```

## Gotchas

- **`path.add` / `path.remove`** — Java/C++ use mutable structures
  with explicit add/remove. Python sometimes uses `path + [x]`
  (immutable concat) to avoid the unchoose step. **Match source's
  style per language** — don't convert one to the other.
- **Pruning vs not** — some source solutions prune (skip branches);
  others don't. The "pure" choose-explore-unchoose template doesn't
  prune. Match source's variant exactly.
- **Result accumulation** — passed as parameter or held as class
  field. Source's convention determines the function signature.
- **Sudoku 9x9 magic numbers** — source uses `9` literally; don't
  parametrise as `n` if source didn't.
