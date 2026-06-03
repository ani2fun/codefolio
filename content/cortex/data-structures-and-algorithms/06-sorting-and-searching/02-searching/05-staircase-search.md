---
title: "Staircase Search"
summary: "<!-- TODO: summary -->"
---

# 5. Staircase Search

The 2D Binary Search lesson needed a strict matrix structure: every row sorted *and* the first element of each row greater than the previous row's last element. That's restrictive — many real matrices have rows and columns sorted independently but not "globally" in row-major order.

```
matrix = [[1,  4,  7, 11],
          [2,  5,  8, 12],
          [3,  6,  9, 16],
          [10, 13, 14, 17]]
```

Each row is sorted left-to-right. Each column is sorted top-to-bottom. But the flattened sequence `[1, 4, 7, 11, 2, 5, ...]` is *not* sorted. 2D binary search doesn't work here.

**Staircase search** does. It walks from a corner of the matrix toward the opposite corner, moving one row down or one column left at each step based on the current cell's value. The path looks like a staircase. Total steps: at most `rows + cols`. Worse than 2D binary search's `O(log(N·M))` but applicable to a *broader* class of matrices.

By the end of this lesson you'll know the algorithm, why starting from the top-right corner (or bottom-left) is essential, why other corners don't work, and the complexity trade-off.

## Table of contents

1. [Understanding staircase search](#understanding-staircase-search)
2. [Why the top-right corner](#why-the-top-right-corner)
3. [Implementation](#implementation)
4. [Complexity analysis](#complexity-analysis)
5. [Staircase search problem](#staircase-search-problem)

***

# Understanding Staircase Search

The required matrix structure (looser than 2D binary search):

1. **Each row is sorted** left to right.
2. **Each column is sorted** top to bottom.

That's it. No constraint that rows-fit-end-to-start. Matrices like the one above qualify.

The algorithm: start at the **top-right corner**. At each step:
- If the current cell equals target → found.
- If the current cell < target → target is *below* (smaller cells are above, so move down).
- If the current cell > target → target is *to the left* (larger cells are to the right, so move left).

The path traced is a staircase that descends-and-lefts toward the bottom-left. Each step eliminates either a whole row or a whole column from consideration. Total steps: at most `rows + cols - 1`.

```d2
direction: down

step1: "Start: (0, 3) — top-right" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
step2: "matrix[0][3] = 11. target = 9. 11 > 9 → col left to (0, 2)"
step3: "matrix[0][2] = 7. 7 < 9 → row down to (1, 2)"
step4: "matrix[1][2] = 8. 8 < 9 → row down to (2, 2)"
step5: "matrix[2][2] = 9. 9 == target → return true" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}

step1 -> step2 -> step3 -> step4 -> step5
```

<p align="center"><strong>Staircase walk for <code>target = 9</code>. Four steps to find. Each step eliminates a row or a column from possibilities.</strong></p>

---

## Why It Works

The top-right corner has a special property: every cell *below* it is larger (column sorted), and every cell *to its left* is smaller (row sorted). So:
- If the corner cell is too big, the target can't be in this column — move left.
- If the corner cell is too small, the target can't be in this row — move down.

After each step, we've eliminated a whole row or column. The new "current cell" is the top-right of the remaining sub-matrix. The invariant maintains itself — we're always looking at the top-right of an undiscarded region.

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **`O(N + M)`** | Linear in row + column count — fast for square-ish matrices. |
| **`O(1)` space** | Two integer pointers. |
| **Looser input requirements** | Works on any row-and-column sorted matrix. |

| Limitation | Detail |
|---|---|
| **Slower than 2D binary search** | When applicable, `O(log(N·M))` beats `O(N + M)`. Staircase is the fallback when 2D binary search doesn't apply. |
| **Tied to corners** | Must start at top-right or bottom-left; other corners produce ambiguous decisions (see next section). |

---

## Key Takeaway

Staircase search: walk from top-right (or bottom-left), move down on too-small, move left on too-big. `O(N + M)` time, `O(1)` space. Now we'll see why the corner choice is critical.

***

# Why the Top-Right Corner

The algorithm requires starting at a corner where the value is **maximal in its row AND minimal in its column** (or the inverse). Only the top-right and bottom-left corners satisfy this. Top-left and bottom-right don't — and starting there makes the algorithm ambiguous.

```d2
direction: right

tl: "Top-left (0, 0)\n— minimal in row\n— minimal in column\n— ambiguous: too small? row OR col can hold target" {style.fill: "#fecaca"; style.stroke: "#dc2626"}
tr: "Top-right (0, M-1)\n— maximal in row\n— minimal in column\n— too big? go left. Too small? go down. UNAMBIGUOUS" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
bl: "Bottom-left (N-1, 0)\n— minimal in row\n— maximal in column\n— too big? go up. Too small? go right. UNAMBIGUOUS" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
br: "Bottom-right (N-1, M-1)\n— maximal in row\n— maximal in column\n— ambiguous: too big? row OR col can hold target" {style.fill: "#fecaca"; style.stroke: "#dc2626"}
```

<p align="center"><strong>Only the top-right and bottom-left corners give the algorithm a deterministic step direction. Top-left and bottom-right are ambiguous.</strong></p>

If we started at the top-left and the cell was *too small*, the target could be either to the right (along the row) or below (along the column). We'd have no way to choose — and might miss the target. The corner-starting requirement is what makes the algorithm correct.

---

## A Walkthrough

`matrix = [[1, 4, 7, 11], [2, 5, 8, 12], [3, 6, 9, 16], [10, 13, 14, 17]]`, `target = 5`.

```
Start: (0, 3), value = 11. 11 > 5 → col--, now (0, 2)
(0, 2), value = 7. 7 > 5 → col--, now (0, 1)
(0, 1), value = 4. 4 < 5 → row++, now (1, 1)
(1, 1), value = 5. 5 == target → return true
```

Four steps. The "staircase" trace: right edge → left → left → down → found.

---

## Key Takeaway

Top-right (or bottom-left) is the only valid starting corner. The corner's cell value is unambiguous — too big means "go left," too small means "go down." Now the implementation.

***

# Implementation


```python run viz=array viz-root=matrix
from typing import List

class Solution:
    def staircase_search(
        self, matrix: List[List[int]], target: int
    ) -> bool:

        # Get the number of rows in the matrix
        rows: int = len(matrix)

        # Get the number of columns in the matrix
        cols: int = len(matrix[0])

        # Start from the first row
        row: int = 0

        # Start from the last column
        col: int = cols - 1

        while row < rows and col >= 0:

            # Continue until we reach the bottom-left or top-right corner
            # of the matrix

            # If the current element is equal to the target, return
            # True
            if matrix[row][col] == target:
                return True

            # Else if the current element is less than the target, move to
            # the next row
            elif matrix[row][col] < target:
                row += 1

            # Else if the current element is greater than the target, move
            # to the previous column
            else:
                col -= 1

        # Return False if the target is not found
        return False


# Examples from the problem statement
m = [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]
print(Solution().staircase_search(m, 12))   # True
print(Solution().staircase_search(m, 7))    # True
print(Solution().staircase_search(m, 13))   # False

# Edge cases
print(Solution().staircase_search([[5]], 5))                                 # True  — 1x1, present
print(Solution().staircase_search([[5]], 3))                                 # False — 1x1, absent
print(Solution().staircase_search([[1, 2, 3, 4]], 3))                        # True  — 1xN, present
print(Solution().staircase_search([[1, 2, 3, 4]], 5))                        # False — 1xN, absent
print(Solution().staircase_search([[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]], 1))   # True  — first element
```

```java run viz=array viz-root=matrix
import java.util.*;

public class Main {
    static class Solution {
        public boolean staircaseSearch(int[][] matrix, int target) {

            // Get the number of rows in the matrix
            int rows = matrix.length;

            // Get the number of columns in the matrix
            int cols = matrix[0].length;

            // Start from the first row
            int row = 0;

            // Start from the last column
            int col = cols - 1;

            while (row < rows && col >= 0) {

                // Continue until we reach the bottom-left or top-right
                // corner of the matrix

                // If the current element is equal to the target, return
                // true
                if (matrix[row][col] == target) {
                    return true;
                }

                // Else if the current element is less than the target, move
                // to the next row
                else if (matrix[row][col] < target) {
                    row++;
                }

                // Else if the current element is greater than the target,
                // move to the previous column
                else {
                    col--;
                }
            }

            // Return false if the target is not found
            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        int[][] m = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}};
        System.out.println(new Solution().staircaseSearch(m, 12));   // true
        System.out.println(new Solution().staircaseSearch(m, 7));    // true
        System.out.println(new Solution().staircaseSearch(m, 13));   // false

        // Edge cases
        System.out.println(new Solution().staircaseSearch(new int[][]{{5}}, 5));                                 // true  — 1x1, present
        System.out.println(new Solution().staircaseSearch(new int[][]{{5}}, 3));                                 // false — 1x1, absent
        System.out.println(new Solution().staircaseSearch(new int[][]{{1, 2, 3, 4}}, 3));                        // true  — 1xN, present
        System.out.println(new Solution().staircaseSearch(new int[][]{{1, 2, 3, 4}}, 5));                        // false — 1xN, absent
        System.out.println(new Solution().staircaseSearch(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}}, 1));  // true — first element
    }
}
```


***

# Complexity Analysis

| Resource | Best | Average | Worst |
|---|---|---|---|
| **Time** | `O(1)` (target at top-right) | `O(N + M)` | `O(N + M)` |
| **Space** | `O(1)` | `O(1)` | `O(1)` |

Each step decrements `col` or increments `row`. Both are bounded — `col` starts at `M-1` and goes to `-1`; `row` starts at `0` and goes to `N`. Total steps ≤ `N + M`.

---

## Staircase vs 2D Binary Search

| Algorithm | Time | Required structure |
|---|---|---|
| 2D binary search (the 2D Binary Search lesson) | `O(log(N·M))` | Row-sorted + first-of-row > last-of-prev (matrix is "globally" sorted) |
| Staircase search (this lesson) | `O(N + M)` | Row-sorted + column-sorted (each independently) |

If the matrix has the *stricter* structure, prefer 2D binary search. Staircase is the algorithm of choice when only the looser row/column-sortedness holds.

For a square matrix `N × N`: 2D binary search is `O(2 log N)`; staircase is `O(2N)`. The asymptotic gap is huge — but only if the matrix has the structure 2D binary search needs.

---

## Key Takeaway

Staircase search: `O(N + M)`, `O(1)` space, requires only row/column sortedness. The fallback when 2D binary search's stricter structure doesn't apply. Now the canonical exercise.

***

# Staircase Search Problem

---

## The Problem

Given an `N × M` matrix where each row is sorted left-to-right and each column is sorted top-to-bottom, return `true` if `target` is in the matrix, else `false`. **Must run in `O(N + M)`.**

```
Input:  matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]], target = 12
Output: true

Input:  matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]], target = 7
Output: true

Input:  matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]], target = 13
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
| Empty matrix | `[]` | `false` |
| Single cell | `[[5]], target = 5` | `true` |
| Target at top-right | `[[1, 5], [2, 6]], target = 5` | `true` (found in 1 step) |
| Target at bottom-left | `[[1, 5], [2, 6]], target = 2` | `true` (worst-case path) |
| Below all | `target < matrix[0][0]` | `false` (col walks off the left immediately) |
| Above all | `target > matrix[N-1][M-1]` | `false` (row walks off the bottom) |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Staircase search trades 2D binary search's `O(log(N·M))` for broader applicability — `O(N + M)` on any row-and-column-sorted matrix. The algorithm is mechanically simple: walk from a corner, step row or column at each comparison.

The next lesson handles a different broken-sortedness scenario: a 1D array that's been **rotated** at some pivot. `[4, 5, 6, 7, 0, 1, 2]` is sorted-then-rotated. Plain binary search doesn't find elements directly because the array isn't *globally* sorted — but it's *locally* sorted in two halves. We can still binary-search it in `O(log n)` with one extra check per iteration.

**Transfer challenge — try before the Sorted Rotated Array lesson:** Modify staircase search to return the *count of cells* equal to target (not just true/false). For a matrix with duplicates, how would you trace the staircase to count them? Hint: the path may need to visit multiple cells.

</details>
<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

```python run viz=array viz-root=matrix
class Solution:
    def count_target(self, matrix, target):
        if not matrix or not matrix[0]: return 0
        rows, cols = len(matrix), len(matrix[0])
        row, col = 0, cols - 1
        count = 0
        while row < rows and col >= 0:
            if matrix[row][col] == target:
                count += 1
                col -= 1                                # equal could continue left in row
                # alternatively, row += 1 to continue down — but col-- is enough
                # because all duplicates form a contiguous L-shape from (row, col_first) to (row_last, col)
            elif matrix[row][col] < target:
                row += 1
            else:
                col -= 1
        return count


m = [[1, 5, 5, 8], [2, 5, 5, 9], [3, 6, 7, 10]]
print(Solution().count_target(m, 5))   # 4 (the four 5s)
```

The trick: on equality, decrement `col` (or increment `row`) and keep walking. The duplicates form an **L-shaped contiguous region** because of row+column sortedness. A single staircase walk covers them all. Time: still `O(N + M)`.

**You just generalised staircase search to a count primitive — useful for "how many cells in this matrix equal X" queries.**

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
