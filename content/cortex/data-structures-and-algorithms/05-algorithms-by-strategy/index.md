# Algorithms by Strategy

A "strategy" is a way of *thinking* about problems — a question you ask before you write a single line of code. Recursion asks "can I solve a smaller version and stitch the answer back together?". Greedy asks "can I make a locally-best choice that's also globally optimal?". DP asks "are subproblems shared across the recursion tree?". The strategies in this module are the menu you flip through when you read a new problem statement.

## Place in the curriculum

- **Prerequisites:** [Foundations](/cortex/data-structures-and-algorithms/foundations-index) (recurrence relations are cited heavily in recursion, divide-and-conquer, and DP), [Linear Structures](/cortex/data-structures-and-algorithms/linear-structures-index), [Trees](/cortex/data-structures-and-algorithms/trees-index).
- **Followed by:** [Sorting and Searching](/cortex/data-structures-and-algorithms/sorting-and-searching-index) (which is divide-and-conquer + greedy in disguise), [Graphs](/cortex/data-structures-and-algorithms/graphs-index) (which is DP and greedy in disguise).

## Topics

1. [Recursion](/cortex/data-structures-and-algorithms/algorithms-by-strategy-recursion-index) — base case + recurrence; the four canonical patterns (head, tail, multiple, multidimensional).
2. [Divide and Conquer](/cortex/data-structures-and-algorithms/algorithms-by-strategy-divide-and-conquer-introduction-to-divide-and-conquer) — split, solve, combine; the Master theorem applied; merge-sort, quick-sort, fast multiplication.
3. [Greedy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-greedy-introduction-to-greedy-algorithms) — activity selection, Huffman, exchange-argument proofs; when locally optimal happens to be globally optimal.
4. [Backtracking](/cortex/data-structures-and-algorithms/algorithms-by-strategy-backtracking-index) — search-tree exploration with pruning; permutations, combinations, N-Queens.
5. [Dynamic Programming](/cortex/data-structures-and-algorithms/algorithms-by-strategy-dynamic-programming-index) — overlapping subproblems + optimal substructure; linear, 2D, interval, plus the bitmask/tree/digit DP variants we'll add in the rebuild.
6. [Randomized Algorithms](/cortex/data-structures-and-algorithms/algorithms-by-strategy-randomized-algorithms-introduction-to-randomized-algorithms) — Monte Carlo vs Las Vegas; randomized quickselect; treaps; the right way to pick a pivot.
