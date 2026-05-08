# Sorting

Sorting is the silent foundation under most algorithms in this course. Twelve lessons cover the canonical algorithms — comparison sorts, counting-based sorts, divide-and-conquer sorts, three-way partitioning — across six classification axes (comparison vs counting, stable vs unstable, in-place vs out-of-place, adaptive vs not, internal vs external, recursive vs iterative). By the end you'll have ten different sorting algorithms in your kit, the vocabulary to name their trade-offs, and two pattern lessons that generalise sorting beyond "produce sorted output."

The section progresses roughly in order of complexity: the three quadratic sorts first (bubble, selection, insertion), then linear-time counting sort, then the divide-and-conquer family (quicksort, three-way quicksort, merge sort, heapsort), and finally the two pattern lessons (quickselect for top-K, custom compare for arbitrary orderings).

## Guides

Read in order if you're learning sorting for the first time; jump directly to a named algorithm or pattern for revision.

- [Introduction to Sorting](/cortex/data-structures-and-algorithms/algorithms-sorting-introduction-to-sorting) — why sorting matters, the six classification axes, the order-check warm-up.
- [Bubble Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-bubble-sort) — the simplest comparison sort. `O(n²)`, stable, in-place. Adaptive with the swap-flag optimisation.
- [Selection Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-selection-sort) — minimum swaps. `O(n²)` comparisons but only `O(n)` writes. Not stable.
- [Insertion Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-insertion-sort) — the small-input workhorse used by every modern hybrid sort. `O(n²)` worst, `O(n)` best, adaptive.
- [Counting Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-counting-sort) — non-comparison sort, `O(n + k)`. Linear time when the value range is small.
- [Quick Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-quicksort) — divide-and-conquer with random pivots. `O(n log n)` average, `O(n²)` worst. The standard-library default.
- [Dutch National Flag Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-dutch-national-flag-sort) — three-way partition for arrays with three distinct values. `O(n)` time, `O(1)` space, single pass.
- [Three Way Quick Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-three-way-quicksort) — quicksort + Dutch flag. Linear on duplicate-heavy input.
- [Merge Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-merge-sort) — divide-and-conquer with halving. `O(n log n)` worst case, stable, `O(n)` extra space. The engine of external sorting.
- [Heap Sort](/cortex/data-structures-and-algorithms/algorithms-sorting-heapsort) — `O(n log n)` worst case, in-place. The IntroSort fallback when quicksort goes too deep.
- [Pattern: Quickselect](/cortex/data-structures-and-algorithms/algorithms-sorting-pattern-quickselect) — find the k-th element in `O(n)` average. Four problems: kth smallest, median, k closest, k most frequent.
- [Pattern: Custom Compare](/cortex/data-structures-and-algorithms/algorithms-sorting-pattern-custom-compare) — sort by any ordering rule. Four problems: bitwise, by frequency, largest number, by height.

## Reading guide

The 10 sorting algorithms break naturally into three groups:

1. **Quadratic sorts (Bubble, Selection, Insertion)** — all `O(n²)`. Read these to understand the comparison-sort baseline; only insertion sort sees real production use (for small inputs inside hybrid sorts).
2. **Linear / divide-and-conquer (Counting Sort, Quicksort, Dutch National Flag, Three-Way Quicksort, Merge Sort, Heapsort)** — the production-grade algorithms. Each has specific trade-offs that determine when it wins.
3. **Pattern lessons (Quickselect, Custom Compare)** — quickselect generalises the partition step beyond sorting; custom compare generalises any sort to any ordering rule.

If you're already comfortable with the basics, skim the Introduction through Insertion Sort lessons and start at Counting Sort. The Three-Way Quicksort + Dutch Flag pair is a tight package — read both together. The Quickselect lesson reuses quicksort's partition; read the Quicksort lesson first if you haven't.

Each lesson's transfer challenge pre-loads the next lesson, so reading in sequence is the smoothest path.
