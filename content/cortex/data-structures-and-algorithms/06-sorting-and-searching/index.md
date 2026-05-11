# Sorting and Searching

Two of the oldest problems in computing, and still two of the most heavily used. Most "real" algorithm work in production code is one of these two operations, hidden behind a method call. Knowing which sort or search is running underneath — and why — is the difference between debugging in seconds and debugging for hours.

## Place in the curriculum

- **Prerequisites:** [Foundations](/cortex/data-structures-and-algorithms/foundations-index) (asymptotic analysis), [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) (recursion and divide-and-conquer underpin merge-sort and quick-sort).
- **Followed by:** every later module that takes "sorted" or "searchable" as a given.

## Topics

1. [Sorting](/cortex/data-structures-and-algorithms/sorting-and-searching-sorting-index) — bubble, selection, insertion (the O(n²) family); counting (non-comparative); quick, merge, heap (the O(n log n) family); Dutch-flag and three-way quick for duplicate-heavy input. Plus *stubs* for radix and bucket sort.
2. [Searching](/cortex/data-structures-and-algorithms/sorting-and-searching-searching-index) — binary search and its variants (lower/upper bound, 2D, rotated); pattern taxonomy for predicate-search and min/max-search. Plus *stubs* for exponential and interpolation search.
