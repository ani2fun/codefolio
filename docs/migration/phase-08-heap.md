# Phase 8 — Heap

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/9.heap/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/03-heap/`

## Stats

| | Count |
|---|---:|
| Chapters | 5 |
| Source lessons | 31 |
| Interactive diagrams | 12 |
| Destination size | 5,092 lines across 5 files |
| Indicative sessions | 5-6 |

Compact phase. The array-backed heap is a great fit for the existing
`array-traversal` widget.

## Widget readiness

Heap is a **complete binary tree laid out as an array** — the array
view fits `array-traversal` directly. Heapify-up / heapify-down
animations can be expressed with parent/child index markers stepping
through the array.

The tree-shape view (showing the implicit binary tree above the
array) ideally needs a tree widget, but the array view alone tells
most of the algorithmic story.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 8.1 | Introduction to Heaps | 6 | 4 | Heap property, complete tree, array layout. |
| 8.2 | Array Implementation of Heaps | 8 | 6 | Insert (heapify-up), extract (heapify-down). |
| 8.3 | Pattern: Top K Elements | 6 | 4 | Min-heap of size K trick. |
| 8.4 | Pattern: Comparator | 8 | 6 | Custom comparator (priority queue with tuple). |
| 8.5 | Design | 3 | 0 | Median finder etc. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 8.1 Introduction (full) |
| 2 | Ch 8.2 Array Implementation — first half (insert + heapify-up) |
| 3 | Ch 8.2 — second half (extract + heapify-down + build-heap) |
| 4 | Ch 8.3 Top K Elements (full) |
| 5 | Ch 8.4 Comparator pattern (full) |
| 6 | Ch 8.5 Design + phase verification |

## Session task templates

### Session 1

```
Phase 8 — Heap, Session 1.

Ch 8.1 Introduction to Heaps. Align heap property definition,
parent/child index helpers (parent(i) = (i-1)/2, left(i) = 2*i+1,
right(i) = 2*i+2). Convert any complete-binary-tree-as-array
interactive diagrams to `array-traversal` widgets.

One commit per concept section.
```

### Sessions 2-3 (Implementation)

```
Phase 8 — Heap, Session <N>.

Ch 8.2 Array Implementation of Heaps — <half>. Align the heapify-up
and heapify-down helpers verbatim. Source typically extracts:
- `heapifyUp(i)` / `siftUp(i)` — used after insert
- `heapifyDown(i)` / `siftDown(i)` — used after extract
- `buildHeap(arr)` — used by heapsort

Destination must extract the same helpers in all 5 langs. Add
`array-traversal` widgets for the heapify-step animations using
parent/child markers.
```

### Sessions 4-5 (Patterns)

```
Phase 8 — Heap, Session <N>.

Ch <8.3 Top K / 8.4 Comparator>. Source uses min-heap-of-size-K
pattern: maintain a heap of size K and evict the min when the
new element is larger. Match source's exact eviction condition.

For Ch 8.4 Comparator: source typically uses a tuple (priority,
value) or a custom Comparator class. Java's PriorityQueue uses a
Comparator; Python uses heapq with (priority, value) tuples
(reversed for max-heap). Match per language.
```

### Session 6 (closeout)

```
Phase 8 — Heap, Session 6 (closeout).

Ch 8.5 Design (Median Finder). Phase verification + status note for
Phase 9.
```

## Gotchas

- **Python's heapq is min-heap only** — for max-heap, source negates
  values on push and re-negates on pop. Destination must do the
  same. Don't use a third-party library.
- **Index 0 vs index 1 root** — source uses index-0 root in most
  places (parent(i) = (i-1)//2). Some heap impls use index-1 root.
  Match source's convention exactly.
- **Heapify-up loop condition** — `while i > 0 && heap[parent(i)] >
  heap[i]` for min-heap. Easy to flip the comparison direction. Test
  with both min- and max-heap inputs.
- **`heappush` / `heappop` order** — Python `heappush(h, v)` first;
  Java `pq.offer(v)`. Match per language's idiom.
