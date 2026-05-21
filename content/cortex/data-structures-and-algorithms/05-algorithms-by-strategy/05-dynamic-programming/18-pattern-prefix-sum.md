# 18. The Prefix-Sum Pattern

A friend asks "what's the sum of items 5 through 12 in this array?" — you sum 8 values, no big deal. But what if they ask 100 times? Or 100,000? Or what if the array has a million entries and someone asks "what's the sum of every contiguous slice"? Naively each query is `O(slice length)`, and 1M queries on a 1M-element array crawls toward `O(N²)` — minutes when seconds were the budget. The fix is one of the simplest, most-reused tricks in algorithmics: precompute a **prefix sum** array, where `prefix[i] = arr[0] + arr[1] + ... + arr[i-1]`. After that single linear-time pass, *any* contiguous slice sum is `arr[l..r] = prefix[r+1] - prefix[l]` — O(1) per query.

By the end of this lesson you'll know the **prefix-sum pattern** in 1D and 2D. You'll have written three problems that all rest on the same precompute-then-query trick: **k-limited submatrix sum** (find the largest sum of a fixed-size window), **maximum submatrix sum** (find the largest sum of *any* submatrix), and **range-sum finder** (a class supporting `O(1)` rectangle-sum queries after `O(N²)` setup). The pattern isn't quite DP in the table-fill sense — it's a *precompute* that makes downstream queries instant.

## Table of contents

1. [The Prefix-Sum Pattern](#the-prefix-sum-pattern)
2. [K-Limited Submatrix Sum](#k-limited-submatrix-sum)
3. [Maximum Submatrix Sum](#maximum-submatrix-sum)
4. [Range Sum Finder](#range-sum-finder)
5. [Final Takeaway](#final-takeaway)

***

# The Prefix-Sum Pattern

The 1D pattern:

```
prefix[0] = 0
prefix[i] = prefix[i - 1] + arr[i - 1]    for i = 1..n

sum(arr[l..r]) = prefix[r + 1] - prefix[l]    O(1)
```

Construction is `O(n)`. Each query is `O(1)`. After construction, you can answer arbitrarily many sum queries on different `(l, r)` ranges in constant time per query.

The 2D extension is the same idea on a matrix:

```
prefix[i][j] = sum of matrix[0..i-1][0..j-1]
prefix[i][j] = prefix[i-1][j] + prefix[i][j-1] - prefix[i-1][j-1] + matrix[i-1][j-1]

sum of rectangle (r1, c1) to (r2, c2) inclusive
  = prefix[r2+1][c2+1] - prefix[r1][c2+1] - prefix[r2+1][c1] + prefix[r1][c1]
```

The "subtract twice, add back once" formula is **inclusion-exclusion** — when you compute the prefix to the right edge and the prefix to the bottom edge, the top-left corner gets *subtracted twice*, so you add it back.

```d2
direction: right
incex: "Inclusion-exclusion in 2D" {
  grid-rows: 4
  grid-columns: 4
  grid-gap: 0
  c00: "+TL"
  c01: ""
  c02: ""
  c03: "−"
  c10: ""
  c11: ""
  c12: ""
  c13: ""
  c20: ""
  c21: ""
  c22: ""
  c23: ""
  c30: "−"
  c31: ""
  c32: ""
  c33: "+BR"
}
```

<p align="center"><strong>Inclusion-exclusion for a rectangle: <code>+BR − TR − BL + TL</code>. The top-left contributes +1 because subtracting both top-row and left-column subtracts it twice; we add it back once.</strong></p>

> *Pause. Why is the formula NOT just `prefix[r2+1][c2+1] - prefix[r1][c1]`? Predict the failure case.*

Because that subtraction would overshoot. `prefix[r1][c1]` is the sum of cells *above-left* of `(r1, c1)`. Removing it from `prefix[r2+1][c2+1]` doesn't surgically excise the rectangle's "above" and "left" strips — it removes only the corner. We need to remove the whole *strip above* (`prefix[r1][c2+1]`) and the whole *strip left* (`prefix[r2+1][c1]`), then add back the corner that got subtracted twice (`prefix[r1][c1]`).

## Where this shows up

Image processing (integral images for fast convolution / Haar features in face detection), data analytics (group-by sums on time-series, sliding-window aggregates over OHLCV bars), competitive programming (any "sum over a range" query, with extensions to count, max, GCD via more sophisticated structures), and bioinformatics (counting matches in genome windows).

---

## Key Takeaway

Prefix sums precompute "all sums up to here" so every later range-sum query becomes `O(1)`. 1D uses subtraction; 2D uses inclusion-exclusion (subtract twice, add back once).

***

# K-Limited Submatrix Sum

## The Problem

Given an `n × m` matrix and an integer `k`, find the maximum sum among all `k × k` submatrices.

```
Input:  matrix = [[1, 2, 9],
                  [5, 3, 8],
                  [4, 6, 7]],
        k = 2
Output: 24                         Submatrix at (1,1)-(2,2): 3 + 8 + 6 + 7 = 24

Input:  matrix = [[1, 2, 3],
                  [4, 5, 6],
                  [7, 8, 9]],
        k = 3
Output: 45                         The whole matrix is the only k × k submatrix
```

<details>
<summary><h2>The Approach</h2></summary>


Naively: enumerate every `k × k` submatrix (there are `(n-k+1) × (m-k+1)` of them), and sum each in `O(k²)` — total `O(n × m × k²)`. With prefix sums precomputed in `O(n × m)`, each submatrix sum is `O(1)` — total drops to `O(n × m)`.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List
import sys

class Solution:
    def k_limited_submatrix_sum(
        self, matrix: List[List[int]], k: int
    ) -> int:
        rows: int = len(matrix)
        cols: int = len(matrix[0])

        # Precompute the prefix sum matrix
        prefix_sum: List[List[int]] = [
            [0] * (cols + 1) for _ in range(rows + 1)
        ]
        for i in range(1, rows + 1):
            for j in range(1, cols + 1):

                # Calculate the sum of values in the submatrix (0,0) to
                # (i-1,j-1) and store it in prefix_sum[i][j]
                prefix_sum[i][j] = (
                    prefix_sum[i - 1][j]
                    + prefix_sum[i][j - 1]
                    - prefix_sum[i - 1][j - 1]
                    + matrix[i - 1][j - 1]
                )

        max_sum: int = -sys.maxsize
        max_sum_submatrix: List[List[int]] = [[0] * k for _ in range(k)]

        # Find the maximum sum submatrix
        for i in range(rows - k + 1):
            for j in range(cols - k + 1):

                # Calculate the sum of values in the submatrix (i,j) to
                # (i+k-1,j+k-1)
                sum_ = (
                    prefix_sum[i + k][j + k]
                    - prefix_sum[i][j + k]
                    - prefix_sum[i + k][j]
                    + prefix_sum[i][j]
                )

                if sum_ > max_sum:
                    max_sum = sum_
        return max_sum


# Examples from the problem statement
print(Solution().k_limited_submatrix_sum([[1,2,9],[5,3,8],[4,6,7]], 2))   # 24
print(Solution().k_limited_submatrix_sum([[1,2,3],[4,5,6],[7,8,9]], 3))   # 45

# Edge cases
print(Solution().k_limited_submatrix_sum([[5]], 1))                        # 5  — 1x1 matrix, k=1
print(Solution().k_limited_submatrix_sum([[1,2],[3,4]], 1))                # 4  — k=1, max element
print(Solution().k_limited_submatrix_sum([[1,2],[3,4]], 2))                # 10 — whole 2x2
print(Solution().k_limited_submatrix_sum([[-1,-2],[-3,-4]], 1))            # -1 — negative matrix
print(Solution().k_limited_submatrix_sum([[1,2,3],[4,5,6],[7,8,9]], 2))   # 28 — bottom-right 2x2
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int kLimitedSubmatrixSum(int[][] matrix, int k) {
            int rows = matrix.length;
            int cols = matrix[0].length;

            // Precompute the prefix sum matrix
            int[][] prefixSum = new int[rows + 1][cols + 1];
            for (int i = 1; i <= rows; i++) {
                for (int j = 1; j <= cols; j++) {

                    // Calculate the sum of values in the submatrix (0,0) to
                    // (i-1,j-1) and store it in prefixSum[i][j]
                    prefixSum[i][j] =
                        prefixSum[i - 1][j] +
                        prefixSum[i][j - 1] -
                        prefixSum[i - 1][j - 1] +
                        matrix[i - 1][j - 1];
                }
            }

            int maxSum = Integer.MIN_VALUE;
            int[][] maxSumSubmatrix = new int[k][k];

            // Find the maximum sum submatrix
            for (int i = 0; i <= rows - k; i++) {
                for (int j = 0; j <= cols - k; j++) {

                    // Calculate the sum of values in the submatrix (i,j) to
                    // (i+k-1,j+k-1)
                    int sum =
                        prefixSum[i + k][j + k] -
                        prefixSum[i][j + k] -
                        prefixSum[i + k][j] +
                        prefixSum[i][j];

                    if (sum > maxSum) {
                        maxSum = sum;
                    }
                }
            }

            return maxSum;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{1,2,9},{5,3,8},{4,6,7}}, 2));   // 24
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{1,2,3},{4,5,6},{7,8,9}}, 3));   // 45

        // Edge cases
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{5}}, 1));                        // 5
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{1,2},{3,4}}, 1));                // 4
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{1,2},{3,4}}, 2));                // 10
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{-1,-2},{-3,-4}}, 1));            // -1
        System.out.println(new Solution().kLimitedSubmatrixSum(new int[][]{{1,2,3},{4,5,6},{7,8,9}}, 2));   // 28
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(n × m)` — `O(n × m)` precompute + `O((n-k+1) × (m-k+1))` queries |
| Space | `O(n × m)` for the prefix table |

</details>

***

# Maximum Submatrix Sum

## The Problem

Given an `n × n` matrix (with possibly negative values), find the maximum sum among **all** submatrices (any size, any position).

```
Input:  matrix = [[1, 2, 9],
                  [-5, 3, 8],
                  [4, 6, -7]]
Output: 22                         Submatrix excluding the negatives optimally

Input:  matrix = [[1, -2, -3],
                  [-4, -5, -6],
                  [-7, -8, -9]]
Output: 1                          Single cell (0, 0)
```

<details>
<summary><h2>The Approach</h2></summary>


There are `O(n²)` choices of top-left corner and `O(n²)` choices of bottom-right corner — `O(n⁴)` submatrices total. With prefix sums, each is `O(1)` to evaluate. Total: `O(n⁴)`.

(There's a faster `O(n³)` algorithm using Kadane's-on-collapsed-rows, but this lesson sticks to the straightforward prefix-sum quadruple-loop, which makes the pattern's structure transparent.)

> *Pause. Why is `O(n⁴)` already a major win over the naive baseline?*

Without prefix sums, computing each submatrix's sum requires scanning all its cells — up to `O(n²)` per submatrix, total `O(n⁶)`. Prefix sums kill the inner sum loop, dropping the total by a factor of `n²`.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List
import sys

class Solution:
    def maximum_submatrix_sum(self, matrix: List[List[int]]) -> int:
        n: int = len(matrix)

        # Create a prefix sum matrix with size (n+1) x (n+1)
        prefix_sum: List[List[int]] = [
            [0] * (n + 1) for _ in range(n + 1)
        ]

        # Compute the prefix sum matrix
        for i in range(1, n + 1):
            for j in range(1, n + 1):

                # Each cell in the prefix sum matrix is the sum of the
                # corresponding submatrix in the original matrix
                prefix_sum[i][j] = (
                    prefix_sum[i - 1][j]
                    + prefix_sum[i][j - 1]
                    - prefix_sum[i - 1][j - 1]
                    + matrix[i - 1][j - 1]
                )

        max_sum: int = -sys.maxsize

        # Iterate over all possible submatrices
        for r1 in range(1, n + 1):
            for c1 in range(1, n + 1):
                for r2 in range(r1, n + 1):
                    for c2 in range(c1, n + 1):

                        # Compute the sum of the submatrix using the
                        # prefix sum matrix
                        sum: int = (
                            prefix_sum[r2][c2]
                            - prefix_sum[r1 - 1][c2]
                            - prefix_sum[r2][c1 - 1]
                            + prefix_sum[r1 - 1][c1 - 1]
                        )

                        # Update the maximum sum if the current sum is
                        # greater
                        max_sum = max(max_sum, sum)

        return max_sum


# Examples from the problem statement
print(Solution().maximum_submatrix_sum([[1,2,9],[-5,3,8],[4,6,-7]]))      # 22
print(Solution().maximum_submatrix_sum([[1,-2,-3],[-4,-5,-6],[-7,-8,-9]])) # 1

# Edge cases
print(Solution().maximum_submatrix_sum([[5]]))                             # 5  — 1x1
print(Solution().maximum_submatrix_sum([[-1]]))                            # -1 — all negative 1x1
print(Solution().maximum_submatrix_sum([[1,2],[3,4]]))                     # 10 — whole 2x2
print(Solution().maximum_submatrix_sum([[-1,-2],[-3,-4]]))                 # -1 — all negative
print(Solution().maximum_submatrix_sum([[1,2,3],[4,5,6],[7,8,9]]))         # 45 — all positive
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int maximumSubmatrixSum(int[][] matrix) {
            int n = matrix.length;

            // Create a prefix sum matrix with size (n+1) x (n+1)
            int[][] prefixSum = new int[n + 1][n + 1];

            // Compute the prefix sum matrix
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {

                    // Each cell in the prefix sum matrix is the sum of the
                    // corresponding submatrix in the original matrix
                    prefixSum[i][j] =
                        prefixSum[i - 1][j] +
                        prefixSum[i][j - 1] -
                        prefixSum[i - 1][j - 1] +
                        matrix[i - 1][j - 1];
                }
            }

            int maxSum = Integer.MIN_VALUE;

            // Iterate over all possible submatrices
            for (int r1 = 1; r1 <= n; r1++) {
                for (int c1 = 1; c1 <= n; c1++) {
                    for (int r2 = r1; r2 <= n; r2++) {
                        for (int c2 = c1; c2 <= n; c2++) {

                            // Compute the sum of the submatrix using the
                            // prefix sum matrix
                            int sum =
                                prefixSum[r2][c2] -
                                prefixSum[r1 - 1][c2] -
                                prefixSum[r2][c1 - 1] +
                                prefixSum[r1 - 1][c1 - 1];

                            // Update the maximum sum if the current sum is
                            // greater
                            maxSum = Math.max(maxSum, sum);
                        }
                    }
                }
            }

            return maxSum;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{1,2,9},{-5,3,8},{4,6,-7}}));       // 22
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{1,-2,-3},{-4,-5,-6},{-7,-8,-9}})); // 1

        // Edge cases
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{5}}));                             // 5
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{-1}}));                            // -1
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{1,2},{3,4}}));                     // 10
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{-1,-2},{-3,-4}}));                 // -1
        System.out.println(new Solution().maximumSubmatrixSum(new int[][]{{1,2,3},{4,5,6},{7,8,9}}));         // 45
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(n⁴)` for the brute-force enumeration (`O(n³)` possible with Kadane variant) |
| Space | `O(n²)` for prefix table |

</details>

***

# Range Sum Finder

## The Problem

Build a class `RangeSumFinder` that takes a 2D matrix in its constructor and supports `O(1)` rectangle-sum queries afterward.

```
Operations: [RangeSumFinder, sumRegion, sumRegion, sumRegion]
Matrix:     [[1, 2, 3],
             [4, 5, 6],
             [7, 8, 9]]
Queries:    [[], [1, 1, 1, 1], [0, 0, 2, 2], [1, 1, 2, 2]]

Output:     [null, 5, 45, 28]
```

The interface is the standard `(row1, col1, row2, col2)` rectangle. The contract: each query *must* be `O(1)` after construction.

<details>
<summary><h2>The Approach</h2></summary>


Build a prefix-sum table once in `O(n × m)`. Each `sumRegion` is then a four-term inclusion-exclusion subtraction — `O(1)`.

> *Pause. Why is this important? Why not just compute each query on the fly?*

Because the query rate dominates the cost. If `k` queries each take `O(n × m)` work, total is `O(k × n × m)`. With prefix sums, `O(n × m + k)` — almost `n × m` cheaper for large `k`. Many real systems answer billions of range queries per day; the constant-time per-query is critical.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class RangeSumFinder:
    def __init__(self, matrix: List[List[int]]):

        # Number of rows in the matrix
        rows: int = len(matrix)

        # Number of columns in the matrix
        cols: int = len(matrix[0])

        # Create a new matrix with dimensions (rows+1) x (cols+1) and
        # initialize all elements to 0
        self.prefix_sum: List[List[int]] = [
            [0] * (cols + 1) for _ in range(rows + 1)
        ]

        # Fill in the values of the new matrix using dynamic programming
        for i in range(1, rows + 1):
            for j in range(1, cols + 1):

                # The value at prefix_sum[i][j] is the sum of the current
                # element in matrix, the value above it, the value to its
                # left, and the value in the top-left corner, all
                # subtracted by the value in the top-left corner (to
                # avoid double counting)
                self.prefix_sum[i][j] = (
                    matrix[i - 1][j - 1]
                    + self.prefix_sum[i - 1][j]
                    + self.prefix_sum[i][j - 1]
                    - self.prefix_sum[i - 1][j - 1]
                )

    # Method to calculate the sum of elements within a given rectangle
    def sum_region(
        self, row1: int, col1: int, row2: int, col2: int
    ) -> int:

        # The sum of the rectangle is calculated using the values in the
        # new matrix prefixSum The formula is: sum =
        # prefixSum[row2+1][col2+1] - prefixSum[row1][col2 + 1] -
        # prefixSum[row2 + 1][col1] + prefixSum[row1][col1] It subtracts
        # the sum of elements above and to the left of the rectangle,
        # and adds back the sum of the elements above and to the left of
        # the rectangle, to avoid double subtraction
        return (
            self.prefix_sum[row2 + 1][col2 + 1]
            - self.prefix_sum[row1][col2 + 1]
            - self.prefix_sum[row2 + 1][col1]
            + self.prefix_sum[row1][col1]
        )


# Example from the problem statement
rsf = RangeSumFinder([[1,2,3],[4,5,6],[7,8,9]])
print(rsf.sum_region(1, 1, 1, 1))  # 5
print(rsf.sum_region(0, 0, 2, 2))  # 45
print(rsf.sum_region(1, 1, 2, 2))  # 28

# Edge cases
rsf2 = RangeSumFinder([[1]])
print(rsf2.sum_region(0, 0, 0, 0)) # 1  — single cell

rsf3 = RangeSumFinder([[1,2],[3,4]])
print(rsf3.sum_region(0, 0, 0, 0)) # 1  — top-left cell
print(rsf3.sum_region(1, 1, 1, 1)) # 4  — bottom-right cell
print(rsf3.sum_region(0, 0, 1, 1)) # 10 — whole matrix

rsf4 = RangeSumFinder([[-1,-2],[-3,-4]])
print(rsf4.sum_region(0, 0, 1, 1)) # -10 — negative values
```

```java run
import java.util.*;

public class Main {
    static class RangeSumFinder {
        private int[][] prefixSum;

        // Constructor
        public RangeSumFinder(int[][] matrix) {

            // Number of rows in the matrix
            int rows = matrix.length;

            // Number of columns in the matrix
            int cols = matrix[0].length;

            // Create a new matrix with dimensions (rows+1) x (cols+1) and
            // initialize all elements to 0
            prefixSum = new int[rows + 1][cols + 1];

            // Fill in the values of the new matrix using dynamic programming
            for (int i = 1; i <= rows; i++) {
                for (int j = 1; j <= cols; j++) {

                    // The value at prefixSum[i][j] is the sum of the current
                    // element in matrix, the value above it, the value to
                    // its left, and the value in the top-left corner, all
                    // subtracted by the value in the top-left corner (to
                    // avoid double counting)
                    prefixSum[i][j] =
                        matrix[i - 1][j - 1] +
                        prefixSum[i - 1][j] +
                        prefixSum[i][j - 1] -
                        prefixSum[i - 1][j - 1];
                }
            }
        }

        // Method to calculate the sum of elements within a given rectangle
        public int sumRegion(int row1, int col1, int row2, int col2) {

            // The sum of the rectangle is calculated using the values in the
            // new matrix prefixSum The formula is: sum =
            // prefixSum[row2+1][col2+1] - prefixSum[row1][col2 + 1] -
            // prefixSum[row2 + 1][col1] + prefixSum[row1][col1] It subtracts
            // the sum of elements above and to the left of the rectangle,
            // and adds back the sum of the elements above and to the left of
            // the rectangle, to avoid double subtraction
            return (
                prefixSum[row2 + 1][col2 + 1] -
                prefixSum[row1][col2 + 1] -
                prefixSum[row2 + 1][col1] +
                prefixSum[row1][col1]
            );
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        RangeSumFinder rsf = new RangeSumFinder(new int[][]{{1,2,3},{4,5,6},{7,8,9}});
        System.out.println(rsf.sumRegion(1, 1, 1, 1));  // 5
        System.out.println(rsf.sumRegion(0, 0, 2, 2));  // 45
        System.out.println(rsf.sumRegion(1, 1, 2, 2));  // 28

        // Edge cases
        RangeSumFinder rsf2 = new RangeSumFinder(new int[][]{{1}});
        System.out.println(rsf2.sumRegion(0, 0, 0, 0)); // 1

        RangeSumFinder rsf3 = new RangeSumFinder(new int[][]{{1,2},{3,4}});
        System.out.println(rsf3.sumRegion(0, 0, 0, 0)); // 1
        System.out.println(rsf3.sumRegion(1, 1, 1, 1)); // 4
        System.out.println(rsf3.sumRegion(0, 0, 1, 1)); // 10

        RangeSumFinder rsf4 = new RangeSumFinder(new int[][]{{-1,-2},{-3,-4}});
        System.out.println(rsf4.sumRegion(0, 0, 1, 1)); // -10
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Construction | `O(n × m)` |
| Each query | `O(1)` |
| Space | `O(n × m)` |

</details>

***

# Final Takeaway

The prefix-sum pattern is the simplest precompute-then-query trick in algorithmics — and one of the most reused. Three problems, three readings of the same table:

| Problem | Read |
|---|---|
| K-limited submatrix sum | One sweep over fixed-size windows |
| Maximum submatrix sum | Quadruple loop over all corner pairs |
| Range sum finder | One inclusion-exclusion per query |

The pattern stretches further with creative state choices: prefix counts (for "how many elements satisfy P in this range"), prefix XORs (for "find subarray with XOR = target"), or prefix mod-counts (for "how many subarrays have sum divisible by k"). Each variant trades the sum operator for another aggregation — so long as the operator is associative *and* invertible (i.e. has an inverse, like subtraction undoes addition), the prefix trick works.

**You didn't just learn three matrix problems. You learned that any operator that supports cheap "extend by one" *and* "subtract" can power a precompute-then-query data structure with `O(1)` queries. That insight unlocks Fenwick trees, segment trees, sparse tables, and a small library of advanced range-query structures you'll meet later — all variations on this same theme.**

> *Transfer challenge for the next lesson:* Now that you've seen 11 distinct DP shapes (linear DP, LIS, LCS, LCSubstr, edit distance, palindromic substring/subsequence, palindrome partitioning, word break, knapsack family, knapsack applications, optimal strategy, boolean parenthesisation, matrix chain, edit-distance pattern, subset-sum pattern, 2D-grid pattern, prefix-sum pattern), the next lesson is a **practice set** — a curated mixed bag of problems across all the patterns. Predict which one shape will appear most often.

<details>
<summary><strong>Answer</strong></summary>

The edit-distance pattern (2D prefix DP) and the knapsack family appear most often — they're the bread-and-butter of interview DP problems. The next lesson presents a practice set covering one problem per pattern, so you can self-test before moving on.

</details>
