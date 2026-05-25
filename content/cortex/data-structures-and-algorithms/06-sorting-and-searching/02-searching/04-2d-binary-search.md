---
title: "2D Binary Search"
summary: "<!-- TODO: summary -->"
---

# 4. 2D Binary Search

A spreadsheet of student scores: rows for students (sorted by ID), columns for assignments. The grader wants to find score `X`. Linear scan: `O(rows × cols)`. We just spent three lessons learning that for sorted *1D* arrays, binary search drops the cost to `O(log n)`. Can we apply binary search to a *2D* matrix and get `O(log(rows × cols))`?

Yes — when the matrix has the right structure. **Row-wise sorted** plus **the first element of each row is greater than the last element of the previous row** = the matrix is conceptually a single sorted list, just laid out in a grid. We can binary-search it as if it were 1D, with one tweak: convert each "1D index" to its `(row, col)` pair using divmod arithmetic.

By the end of this lesson you'll know the index-flattening trick, why it produces a single coherent sorted sequence, and `O(log(N·M))` complexity. The next lesson (the Staircase Search lesson, **staircase search**) tackles the looser case where rows are sorted and columns are sorted *but the row-end-row-start property doesn't hold* — which requires a different algorithm.

## Table of contents

1. [Understanding 2D binary search](#understanding-2d-binary-search)
2. [The index-flattening trick](#the-index-flattening-trick)
3. [Implementation](#implementation)
4. [Complexity analysis](#complexity-analysis)
5. [2D binary search problem](#2d-binary-search-problem)

***

# Understanding 2D Binary Search

The matrix structure required:

1. **Each row is sorted** in non-decreasing order.
2. **The first element of each row is greater than the last element of the previous row.**

Together, these mean the matrix is one sorted sequence broken into rows. Reading the matrix row-by-row, left-to-right, you get a single sorted list.

```
matrix = [[1,  2,  2,  4],
          [5,  5,  5,  5],
          [9, 10, 11, 12]]

flattened = [1, 2, 2, 4, 5, 5, 5, 5, 9, 10, 11, 12]
```

The flattened version is sorted. Binary search would find any target in `O(log(N·M))`. The 2D version does the same — without actually flattening — by treating the matrix as if it were the flattened array and computing the `(row, col)` for any "flattened index" on demand.

---

## A Walkthrough

`matrix = [[1, 2, 2, 4], [5, 5, 5, 5], [9, 10, 11, 12]]`, `target = 11`. Total cells = 12.

```
low = 0, high = 11

Iter 1: mid = 5, (row, col) = (1, 1). matrix[1][1] = 5. 5 < 11 → low = 6
Iter 2: mid = 8, (row, col) = (2, 0). matrix[2][0] = 9. 9 < 11 → low = 9
Iter 3: mid = 10, (row, col) = (2, 2). matrix[2][2] = 11. 11 == 11 → return true
```

Three iterations on a 12-cell matrix (`log₂(12) ≈ 3.6`). Same logarithmic behaviour as 1D binary search.

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **`O(log(N·M))`** | Logarithmic in the *total* cell count. |
| **`O(1)` space** | Only a few index variables. |
| **Reuses 1D binary search** | The algorithm is identical to the Binary Search lesson's; only the index-to-coordinate mapping is added. |

| Limitation | Detail |
|---|---|
| **Strict input requirements** | Both row-sortedness *and* row-end-row-start property are required. |
| **Doesn't apply to "row sorted + col sorted" matrices** | That looser case is handled by staircase search (the Staircase Search lesson). |

---

## Key Takeaway

A matrix that's "globally sorted by row order" can be binary-searched in `O(log(N·M))` by treating it as a flattened sorted array. Now we'll see the index-flattening trick.

***

# The Index-Flattening Trick

The core insight: in a row-major matrix of `N` rows and `M` columns, **the cell at `(row, col)` is at flattened-index `row * M + col`**, and conversely, **the flattened-index `i` maps to `(row, col) = (i / M, i % M)`**.

```d2
direction: down

matrix: "matrix[3][4]" {
  grid-rows: 3
  grid-columns: 4
  grid-gap: 0
  c00: "0,0\n[1]"
  c01: "0,1\n[2]"
  c02: "0,2\n[2]"
  c03: "0,3\n[4]"
  c10: "1,0\n[5]"
  c11: "1,1\n[5]"
  c12: "1,2\n[5]"
  c13: "1,3\n[5]"
  c20: "2,0\n[9]"
  c21: "2,1\n[10]"
  c22: "2,2\n[11]"
  c23: "2,3\n[12]"
}

flattened: "Flattened indices 0..11" {
  grid-rows: 1
  grid-columns: 12
  grid-gap: 0
  i0: "0"
  i1: "1"
  i2: "2"
  i3: "3"
  i4: "4"
  i5: "5"
  i6: "6"
  i7: "7"
  i8: "8"
  i9: "9"
  i10: "10"
  i11: "11"
}
```

<p align="center"><strong>Index <code>i</code> maps to <code>(i / 4, i % 4)</code>: index 5 → (1, 1), index 8 → (2, 0), index 11 → (2, 3). The arithmetic uses the column count <code>M = 4</code>.</strong></p>

---

## The Algorithm

Binary-search the *flattened* index range `[0, N·M - 1]`. Each iteration:
1. Compute `mid` as a flattened index.
2. Convert: `row = mid / cols`, `col = mid % cols`.
3. Compare `matrix[row][col]` with target.
4. Adjust `low` or `high` as in 1D binary search.

The whole algorithm differs from 1D binary search only in step 2 — the divmod conversion.

---

## Why the Strict Input Requirement?

For the algorithm to work, the flattened sequence must be sorted. Both conditions are needed:
- **Row sorted**: ensures each row is internally sorted.
- **First-of-next > last-of-prev**: ensures consecutive rows fit together end-to-start.

Without the second condition, the flattened sequence might not be sorted. For example, `[[1, 5], [3, 7]]` has both rows sorted but flattens to `[1, 5, 3, 7]` — not sorted. Binary search would give wrong answers. That looser structure (row-sorted + column-sorted, but no end-to-start guarantee) is what staircase search (the Staircase Search lesson) handles.

---

## Key Takeaway

The flattened-index trick: `mid → (mid / M, mid % M)`. Binary search the index range `[0, N·M - 1]`; convert on each iteration. `O(log(N·M))`. Now the implementation.

***

# Implementation


```python run viz=array viz-root=matrix
from typing import List

class Solution:
    def binary_search_2d(
        self, matrix: List[List[int]], target: int
    ) -> bool:

        # Get the number of rows in the matrix
        rows: int = len(matrix)

        # Get the number of columns in the matrix
        cols: int = len(matrix[0])

        # Initialize the low index
        low: int = 0

        # Initialize the high index
        high: int = rows * cols - 1

        # Perform binary search until low index crosses the high index
        while low <= high:

            # Calculate the middle index
            mid: int = low + (high - low) // 2

            # Map the middle index to 2D coordinates
            row, col = mid // cols, mid % cols

            # If the middle value is equal to the target, return
            # true
            if matrix[row][col] == target:
                return True

            # Else if the middle value is less than the target, update
            # the low index
            elif matrix[row][col] < target:
                low = mid + 1

            # Else if the middle value is greater than the target,
            # update the high index
            else:
                high = mid - 1

        # Return False if the target is not found
        return False


# Examples from the problem statement
m = [[1, 2, 2, 4], [5, 5, 5, 5], [9, 10, 11, 12]]
print(Solution().binary_search_2d(m, 12))   # True
print(Solution().binary_search_2d(m, 5))    # True
print(Solution().binary_search_2d(m, 13))   # False

# Edge cases
print(Solution().binary_search_2d([[7]], 7))                   # True  — 1x1, present
print(Solution().binary_search_2d([[7]], 3))                   # False — 1x1, absent
print(Solution().binary_search_2d([[1, 2, 3, 4, 5]], 3))      # True  — 1xN row, present
print(Solution().binary_search_2d([[1, 2, 3, 4, 5]], 6))      # False — 1xN row, absent
print(Solution().binary_search_2d([[1, 2, 2, 4], [5, 5, 5, 5], [9, 10, 11, 12]], 1))  # True — first element
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public boolean binarySearch2D(int[][] matrix, int target) {

            // Get the number of rows in the matrix
            int rows = matrix.length;

            // Get the number of columns in the matrix
            int cols = matrix[0].length;

            // Initialize the low index
            int low = 0;

            // Initialize the high index
            int high = rows * cols - 1;

            // Perform binary search until low index crosses the high index
            while (low <= high) {

                // Calculate the middle index
                int mid = low + (high - low) / 2;

                // Map the middle index to 2D coordinates
                int row = mid / cols;
                int col = mid % cols;

                // If the middle value is equal to the target, return
                // true
                if (matrix[row][col] == target) {
                    return true;
                }

                // Else if the middle value is less than the target, update
                // the low index
                else if (matrix[row][col] < target) {
                    low = mid + 1;
                }

                // Else if the middle value is greater than the target,
                // update the high index
                else {
                    high = mid - 1;
                }
            }

            // Return false if the target is not found
            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        int[][] m = {{1, 2, 2, 4}, {5, 5, 5, 5}, {9, 10, 11, 12}};
        System.out.println(new Solution().binarySearch2D(m, 12));   // true
        System.out.println(new Solution().binarySearch2D(m, 5));    // true
        System.out.println(new Solution().binarySearch2D(m, 13));   // false

        // Edge cases
        System.out.println(new Solution().binarySearch2D(new int[][]{{7}}, 7));                   // true  — 1x1, present
        System.out.println(new Solution().binarySearch2D(new int[][]{{7}}, 3));                   // false — 1x1, absent
        System.out.println(new Solution().binarySearch2D(new int[][]{{1, 2, 3, 4, 5}}, 3));      // true  — 1xN row, present
        System.out.println(new Solution().binarySearch2D(new int[][]{{1, 2, 3, 4, 5}}, 6));      // false — 1xN row, absent
        System.out.println(new Solution().binarySearch2D(new int[][]{{1, 2, 2, 4}, {5, 5, 5, 5}, {9, 10, 11, 12}}, 1));  // true — first element
    }
}
```


***

# Complexity Analysis

| Resource | Cost |
|---|---|
| **Time** | `O(log(N·M))` |
| **Space** | `O(1)` |

The algorithm performs at most `log₂(N·M)` iterations, each `O(1)`. Same as 1D binary search on a flattened array of size `N·M`.

---

## Key Takeaway

2D binary search: `O(log(N·M))`, `O(1)` space, requires a "globally sorted" matrix. Now the canonical exercise.

***

# 2D Binary Search Problem

---

## The Problem

Given an `N × M` matrix where each row is sorted and each row's first element is greater than the previous row's last element, return `true` if `target` is in the matrix, else `false`. **Must run in `O(log(N·M))`.**

```
Input:  matrix = [[1,2,2,4],[5,5,5,5],[9,10,11,12]], target = 12
Output: true

Input:  matrix = [[1,2,2,4],[5,5,5,5],[9,10,11,12]], target = 5
Output: true

Input:  matrix = [[1,2,2,4],[5,5,5,5],[9,10,11,12]], target = 13
Output: false
```

---

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

The implementation matches the version above. See [Implementation](#implementation).

### Edge Cases

| Case | Example | Expected |
|---|---|---|
| Single cell match | `[[5]], target = 5` | `true` |
| Single cell miss | `[[5]], target = 7` | `false` |
| Single row | `[[1, 2, 3, 4, 5]], target = 3` | `true` |
| Single-row miss | `[[1, 2, 3, 4, 5]], target = 6` | `false` |
| Target in first cell | `[[1,2,2,4],[5,5,5,5],[9,10,11,12]], target = 1` | `true` |
| Target in last cell | `target = matrix[N-1][M-1]` | `true` |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


2D binary search reuses 1D binary search's exact algorithm by treating the matrix as a flattened sorted array. The index-flattening trick (`r = i / M`, `c = i % M`) is the only addition. `O(log(N·M))` total.

The next lesson handles the looser case: matrices where rows and columns are sorted *individually* but the row-end-row-start property doesn't hold. **Staircase search** finds a target in `O(rows + cols)` by walking diagonally — a fundamentally different attack, with a fundamentally different complexity.

**Transfer challenge — try before the Staircase Search lesson:** What if the matrix is row-sorted and column-sorted but the row-end-row-start property *doesn't* hold? Example: `[[1, 4, 7], [2, 5, 8], [3, 6, 9]]` (sorted by columns, sorted by rows, but the flattened sequence `[1, 4, 7, 2, 5, 8, 3, 6, 9]` is not sorted). Can 2D binary search still find `target = 5`? Why or why not?

</details>
<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

No, 2D binary search doesn't work on this kind of matrix. The flattened sequence isn't sorted, so binary search's invariant (`arr[mid] < target ⟹ target is to the right`) breaks.

For example, with `target = 5`:
- `mid = 4`, position `(1, 1)`, value `5`. Found! (lucky)
- For `target = 4`: `mid = 4`, value `5`. `5 > 4` → search left. But `4` is at index 1 in the flattened sequence, which is in the "left half" only because the matrix's column-sortedness happens to align with row-major flattening here. On a different example, this would fail.

The column-sorted-row-sorted-but-not-row-major-sorted matrix needs a different algorithm: **staircase search**, which we'll see in the Staircase Search lesson. It walks from a corner inward in `O(rows + cols)` — slower than 2D binary search's `O(log(N·M))` but applicable to a broader class of matrices.

**You just identified the structural difference between the two 2D search algorithms.**

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: The Hook — missing, needs to be written -->
<!--       Guidance: real-world story opening before any definition -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->

<!-- TODO: Final Takeaway — missing, needs to be written -->
<!--       Guidance: exactly 3 typed bullets: Core mechanic / Dominant tradeoff / One thing to remember -->
