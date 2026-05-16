# Phase 13 — Searching

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/4.searching/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/02-searching/`

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `array-traversal` | ✓ reused | Built in Phase 0. Source has 45 Interactive Diagrams — binary-search animation with lo/hi/mid markers, linear search with a running cursor. Range band is already supported |

Per ADR-0006 (updated 2026-05-16): Phase 13 reuses Phase 0's
`array-traversal` widget; no precursor build needed.

## Stats

| | Count |
|---|---:|
| Chapters | 11 |
| Source lessons | 51 |
| Interactive diagrams | 17 |
| Destination size | 5,036 lines across 11 files |
| Indicative sessions | 6-8 |

This phase reuses the **Array Traversal Stepper** that originally
shipped for Arrays' binary search lesson (the very first widget
instance — Arrays ch 01-binary-search.md). Widget conversion should
be quick.

## Widget readiness

Binary search is the **canonical fit** for `array-traversal` — the
existing `01-binary-search.md` widget is the template. All
predicate-search patterns reuse the same `lo` / `hi` / `mid` marker
arrangement.

For 2D binary search / staircase search, a 2D grid widget would be
ideal but doesn't exist. Defer those specific widgets; do code
alignment now.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 13.1 | Binary Search | 4 | 6 | Classic; reuse Arrays template. |
| 13.2 | Lower Bound | 4 | 6 | First-position-where. |
| 13.3 | Upper Bound | 4 | 5 | First-position-strictly-after. |
| 13.4 | 2D Binary Search | 4 | 6 | Matrix search; **needs grid widget**. |
| 13.5 | Staircase Search | 4 | 6 | Sorted-matrix start-from-corner; **needs grid widget**. |
| 13.6 | Sorted Rotated Array | 4 | 2 | Find min / target in rotated. |
| 13.7 | Pattern: Binary Search | 5 | 2 | Pattern template. |
| 13.8 | Pattern: Lower Bound | 5 | 2 | Pattern template. |
| 13.9 | Pattern: Upper Bound | 5 | 2 | Pattern template. |
| 13.10 | Pattern: Minimum Predicate Search | 6 | 4 | "Find smallest x with P(x)". |
| 13.11 | Pattern: Maximum Predicate Search | 6 | 4 | "Find largest x with P(x)". |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 13.1 Binary Search (full) |
| 2 | Ch 13.2 Lower Bound + Ch 13.3 Upper Bound |
| 3 | Ch 13.4 2D Binary Search + Ch 13.5 Staircase Search (code only; defer grid widgets) |
| 4 | Ch 13.6 Sorted Rotated Array |
| 5 | Ch 13.7 + 13.8 + 13.9 Pattern templates (3 short chapters) |
| 6 | Ch 13.10 Minimum Predicate Search |
| 7 | Ch 13.11 Maximum Predicate Search |
| 8 | Phase verification |

## Session task templates

### Session 1 (Binary Search)

```
Phase 13 — Searching, Session 1.

Ch 13.1 Binary Search. The Arrays book already has a binary-search
chapter with an `array-traversal` widget — use that as the template.

Source's `binary_search` function may use `while (lo <= hi)` with
`mid = (lo + hi) / 2` and conditionals updating lo or hi. Match
source's exact loop condition (`<=` vs `<`) — this is a common
bug.

Convert any frame sequences to `array-traversal` widgets with
`lo` / `mid` / `hi` markers.

One commit per problem.
```

### Sessions 2-4 (Bound variants + 2D + Rotated)

```
Phase 13 — Searching, Session <N>.

Chapter <13.X>. Lower/Upper Bound differ from binary search by one
character — match source's exact comparison (`<` for lower,
`<=` for upper, or vice versa). Test boundary cases.

For 2D / staircase: align code now. Defer grid widget conversion
to a future arc.
```

### Sessions 5-7 (Patterns + Predicate Search)

```
Phase 13 — Searching, Session <N>.

Pattern chapters: source factors the pattern into a `binarySearch
(predicate)` template that takes a function as argument. Java/C++
use a Predicate / function pointer; Python uses a callable. Match
per language.

Convert frame sequences to `array-traversal` with `lo`/`hi`/`mid`
markers + a range band showing the "true region" of the predicate.
```

### Session 8 (closeout)

```
Phase 13 — Searching, Session 8 (closeout).

Phase verification. Note any 2D-grid-widget deferrals for the
future widget arc.
```

## Gotchas

- **`<` vs `<=` in the loop condition** — `<` works when `hi` is
  exclusive (length); `<=` works when `hi` is inclusive (length-1).
  Match source exactly; mixing them is a guaranteed bug.
- **Mid overflow** — `(lo + hi) / 2` overflows in some languages
  for very large arrays; source may use `lo + (hi - lo) / 2`. Match
  source's expression literally.
- **Lower-bound semantics** — source's "lower bound" might mean
  "first position where P is true" OR "last position where P is
  false". The two answers differ by 1. Verify source's convention.
- **Rotated array pivot detection** — source's algorithm has a
  specific "which half is sorted?" check. Match the comparison
  direction exactly.
