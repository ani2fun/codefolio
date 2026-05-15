# Phase 12 — Sorting

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/3.sorting/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/06-sorting-and-searching/01-sorting/`

## Stats

| | Count |
|---|---:|
| Chapters | 12 |
| Source lessons | 48 |
| Interactive diagrams | 23 |
| Destination size | 7,366 lines across 12 files |
| Indicative sessions | 8-10 |

Sorting is a high-yield phase for widget conversion — every sort
algorithm is essentially "array transformation step-by-step", which
fits `array-traversal` perfectly.

## Widget readiness

**Excellent fit for `array-traversal`.** Each sort algorithm
visualises as:
- The array being sorted (cells)
- One or more markers (i, j, pivot, left, right)
- A range band for the "sorted prefix" or "current partition"
- Per-step `items` for swaps (uses D3 animation from commit
  `bea8e8c`)

This phase can produce 20+ high-quality widget conversions reusing
existing infrastructure — no new widget types needed.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 12.1 | Introduction to Sorting | 4 | 0 | Concept; no widgets. |
| 12.2 | Bubble Sort | 3 | 3 | Classic; swap-step widget. |
| 12.3 | Selection Sort | 3 | 4 | Find-min-and-swap. |
| 12.4 | Insertion Sort | 3 | 3 | Slide-into-place. |
| 12.5 | Counting Sort | 3 | 4 | Non-comparison; histogram + cumulative. |
| 12.6 | Quicksort | 6 | 13 | **Heaviest chapter** — partition + recursion. |
| 12.7 | Dutch National Flag Sort | 3 | 4 | 3-way partition. |
| 12.8 | Three-Way Quicksort | 3 | 8 | DNF + recursion. |
| 12.9 | Merge Sort | 4 | 4 | Divide + conquer + merge. |
| 12.10 | Heapsort | 3 | 4 | Build-heap + extract loop. |
| 12.11 | Pattern: Quickselect | 6 | 4 | Find kth element. |
| 12.12 | Pattern: Custom Compare | 7 | 5 | Comparator-driven sort. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 12.1 + 12.2 Bubble (Intro + first sort) |
| 2 | Ch 12.3 Selection + Ch 12.4 Insertion |
| 3 | Ch 12.5 Counting Sort |
| 4 | Ch 12.6 Quicksort — first half (partition + basic) |
| 5 | Ch 12.6 finish + Ch 12.7 DNF |
| 6 | Ch 12.8 Three-Way Quicksort |
| 7 | Ch 12.9 Merge Sort |
| 8 | Ch 12.10 Heapsort |
| 9 | Ch 12.11 Quickselect |
| 10 | Ch 12.12 Custom Compare + phase verification |

## Session task templates

### Session 1 (Bubble + Intro)

```
Phase 12 — Sorting, Session 1.

Ch 12.1 Introduction + Ch 12.2 Bubble Sort. Align bubble-sort code
verbatim. Source's swap helper or inline swap — match per language.

Convert the bubble-sort interactive diagram to `array-traversal`
with `i` and `j` markers, per-step items showing swaps. Use the
swap-animation pattern from Arrays' Reverse Segments (commit
4433e12).

One commit per problem.
```

### Sessions 2-3 (Selection, Insertion, Counting)

```
Phase 12 — Sorting, Session <N>.

Chapter <12.X>. Convert sort to `array-traversal` widget showing
the per-step evolution. For Counting Sort: use 2-row widget — input
on top, count/cumulative array on bottom.

One commit per problem.
```

### Sessions 4-5 (Quicksort + DNF)

```
Phase 12 — Sorting, Session <N>.

Ch 12.6 Quicksort <half> / Ch 12.7 DNF. Source's partition helper
(Lomuto or Hoare) is named — destination must extract the same
helper. Match the exact pivot-selection strategy.

DNF: 3 pointers (low, mid, high). Convert to `array-traversal` with
three markers + a range band showing the < pivot region.
```

### Session 7 (Merge Sort)

```
Phase 12 — Sorting, Session 7.

Ch 12.9 Merge Sort. Source's `merge(arr, l, m, r)` helper signature
is canonical — destination must use the same parameter names.
Convert the merge step to a 2-row `array-traversal` widget — input
halves on top, merged result on bottom.
```

### Sessions 8-10 (Heapsort + Patterns)

```
Phase 12 — Sorting, Session <N>.

Chapter <12.10 / 12.11 / 12.12>. Heapsort uses Phase 8's heapify
helpers — reuse the heapifyUp / heapifyDown shape. Quickselect uses
Ch 12.6's partition.

For custom compare (Ch 12.12): source uses Comparator interface in
Java, key= in Python, custom < operator in C++ etc. Match per
language.
```

## Gotchas

- **In-place vs allocating** — Quicksort is in-place; Merge Sort
  allocates. Source preserves this distinction; don't unify.
- **Stability** — Merge Sort is stable; Quicksort isn't. Source's
  algorithm choice for the same problem may reflect this.
- **Partition variants** — Lomuto (pivot at end) vs Hoare (pivot
  at any). Match source's variant exactly.
- **Counting Sort range** — `max(arr) + 1` for the count array
  size; source may use `max - min + 1` for negative-aware. Match
  source.
- **Heapsort uses max-heap** — extracting min from max-heap gives
  ascending sort (extracted elements placed at the end). Easy to
  reverse direction; match source.
