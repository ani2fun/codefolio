# 16. The Subset-Sum Pattern

You're balancing two trays on a scale, throwing items onto one tray or the other. Some items will land left, some right; the question is *whether* (and *how close*) the two trays can equalise. That's the subset-sum pattern in physical form. Algorithmically: given an array of integers, can a subset sum to exactly `target`? The answer is yes for some targets, no for others. Once you have that one boolean, two natural follow-ups become trivial: can the array be *partitioned* into two equal-sum subsets (target = total/2)? What's the *smallest* difference achievable between two subsets (find the largest reachable sum ≤ total/2)?

By the end of this lesson you'll know the **subset-sum pattern** — a single boolean DP that powers a family of partition, target-sum, and discrepancy problems. You'll have written two of those: **partition with equal sum** and **sets with smallest discrepancy**. Both problems reduce in two lines to the underlying boolean DP.

## Table of contents

1. [The Subset-Sum Pattern](#the-subset-sum-pattern)
2. [Partition with Equal Sum](#partition-with-equal-sum)
3. [Sets With Smallest Discrepancy](#sets-with-smallest-discrepancy)
4. [Final Takeaway](#final-takeaway)

***

# The Subset-Sum Pattern

You saw subset sum in lesson 11 (knapsack applications) — a 2D boolean DP keyed on item count and remaining target. The pattern's structure:

```
dp[i][s] = dp[i - 1][s]                          — exclude arr[i - 1]
        OR (arr[i - 1] ≤ s AND dp[i - 1][s - arr[i - 1]])   — include arr[i - 1]
```

with base cases `dp[i][0] = true` (empty subset hits 0) and `dp[0][s > 0] = false` (no items can hit a positive sum).

Why give it a "pattern" lesson? Because two distinct downstream problems both reduce to "compute the subset-sum table, then read it differently."

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  TBL["Subset-sum table<br/>dp[i][s] = subset of first i hits sum s?"]
  TBL --> P1["Partition equal sum:<br/>read dp[n][total/2]"]
  TBL --> P2["Smallest discrepancy:<br/>find largest j ≤ total/2<br/>with dp[n][j] = true"]
```

<p align="center"><strong>One DP table, two readings. The table is the same; only the question changes — which cell to consult, or which range to scan.</strong></p>

> *Pause. Why do partition-equal-sum and smallest-discrepancy share a table? Predict the connection.*

Both ask "what subset sums are reachable from this array?" Equal-sum partition wants the specific cell `dp[n][total/2]`. Smallest discrepancy wants the *largest* reachable sum at or below `total/2` — once you have one subset's sum `s`, the other has sum `total - s`, and their difference is `total - 2s`. Minimising the difference means maximising `s` (subject to `s ≤ total/2`). Both reductions are mechanical once the table exists.

## Where this shows up

Beyond the two problems in this lesson: target-sum (assign +/- to each element to hit a target), count of subsets summing to `s` (replace OR with sum, getting an int DP), the partition number-theoretic problems that show up in scheduling, and the `0/1` integer-knapsack feasibility variant.

---

## Key Takeaway

The subset-sum pattern is one DP table answering "which sums are reachable using which prefix of items." Many partition/discrepancy problems reduce to a one-line read of the table.

***

# Partition with Equal Sum

## The Problem

Given an array of non-negative integers, return `true` if it can be partitioned into two subsets with equal sums.

```
Input:  arr = [1, 5, 4, 10]
Output: true                       Subsets [1, 5, 4] (sum 10) and [10] (sum 10)

Input:  arr = [1, 2, 3, 4, 6]
Output: true                       Subsets [1, 3, 4] (sum 8) and [2, 6] (sum 8)

Input:  arr = [1, 2]
Output: false                      Total = 3 — odd; no equal split possible
```

<details>
<summary><h2>The Reduction</h2></summary>


If the array can be partitioned into two equal-sum subsets, each subset sums to `total / 2`. So the problem reduces to: "is there a subset summing to exactly `total / 2`?" — pure subset sum.

Two quick filters before the DP:
- If `total` is odd, return `false` immediately. Two equal integer sums can't add to an odd number.
- If any single element exceeds `total / 2`, the partition is impossible (that element can't be balanced).

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  TOT["total = sum(arr)"]
  TOT -->|"odd"| F["return false"]
  TOT -->|"even"| HALF["target = total / 2"]
  HALF --> SS["subset_sum(arr, target)?"]
  SS -->|"true"| T["return true"]
  SS -->|"false"| F
```

<p align="center"><strong>Reduction in three steps: parity check, then the subset-sum DP on <code>total / 2</code>. The boolean answer flows straight through.</strong></p>

> *Predict before reading on — for `arr = [3, 3, 3, 4, 5]`, `total = 18`, `target = 9`, is there a subset summing to 9?*

Yes — `{4, 5}` sums to 9, leaving `{3, 3, 3}` summing to 9. So partition is possible. (Also `{3, 3, 3} ∪ {4, 5}` are the two subsets.)

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class Solution:
    def partition_with_equal_sum(self, arr: List[int]) -> bool:
        total_sum: int = sum(arr)

        # If total sum is odd, it can't be divided into two equal subsets
        if total_sum % 2 != 0:
            return False

        target: int = total_sum // 2
        n: int = len(arr)

        # Create a 2D DP array to store the subset sum results
        dp: List[List[bool]] = [
            [False] * (target + 1) for _ in range(n + 1)
        ]

        # Base cases
        for i in range(n + 1):

            # An empty subset can always have a sum of 0
            dp[i][0] = True

        for i in range(1, n + 1):
            for j in range(1, target + 1):
                if j >= arr[i - 1]:

                    # If the current element can be included, check if
                    # there is a subset in the previous elements that has
                    # a sum equal to j or j minus the current element's
                    # value
                    dp[i][j] = dp[i - 1][j] or dp[i - 1][j - arr[i - 1]]
                else:

                    # If the current element is too large to be included,
                    # inherit the value from the previous elements
                    # without including it
                    dp[i][j] = dp[i - 1][j]

        return dp[n][target]


# Examples from the problem statement
print(Solution().partition_with_equal_sum([1, 5, 4, 10]))   # True
print(Solution().partition_with_equal_sum([1, 2, 3, 4, 6])) # True
print(Solution().partition_with_equal_sum([1, 2]))           # False

# Edge cases
print(Solution().partition_with_equal_sum([1]))              # False — single element, odd
print(Solution().partition_with_equal_sum([2, 2]))           # True  — two equal elements
print(Solution().partition_with_equal_sum([1, 1, 1, 1]))     # True  — all same
print(Solution().partition_with_equal_sum([3, 1, 1, 2, 2, 1])) # True
print(Solution().partition_with_equal_sum([1, 3, 5]))        # False — odd total
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public boolean partitionWithEqualSum(int[] arr) {
            int totalSum = 0;
            for (int num : arr) {
                totalSum += num;
            }

            // If total sum is odd, it can't be divided into two equal
            // subsets
            if (totalSum % 2 != 0) {
                return false;
            }

            int target = totalSum / 2;
            int n = arr.length;

            // Create a 2D DP array to store the subset sum results
            boolean[][] dp = new boolean[n + 1][target + 1];

            // Base cases
            for (int i = 0; i <= n; i++) {

                // An empty subset can always have a sum of 0
                dp[i][0] = true;
            }

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= target; j++) {
                    if (j >= arr[i - 1]) {

                        // If the current element can be included, check if
                        // there is a subset in the previous elements that
                        // has a sum equal to j or j minus the current
                        // element's value
                        dp[i][j] = dp[i - 1][j] || dp[i - 1][j - arr[i - 1]];
                    } else {

                        // If the current element is too large to be
                        // included, inherit the value from the previous
                        // elements without including it
                        dp[i][j] = dp[i - 1][j];
                    }
                }
            }

            return dp[n][target];
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1, 5, 4, 10}));   // true
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1, 2, 3, 4, 6})); // true
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1, 2}));           // false

        // Edge cases
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1}));              // false
        System.out.println(new Solution().partitionWithEqualSum(new int[]{2, 2}));           // true
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1, 1, 1, 1}));     // true
        System.out.println(new Solution().partitionWithEqualSum(new int[]{3, 1, 1, 2, 2, 1})); // true
        System.out.println(new Solution().partitionWithEqualSum(new int[]{1, 3, 5}));        // false
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(n × total)` |
| Space | `O(n × total)` (reducible to `O(total)` with downward 1D iteration) |

</details>

***

# Sets With Smallest Discrepancy

## The Problem

Given an array of non-negative integers, partition it into two subsets `S1` and `S2` such that `|sum(S1) − sum(S2)|` is minimum. Return that minimum.

```
Input:  arr = [1, 5, 3, 10]
Output: 1                          [1, 5, 3] (sum 9) and [10] (sum 10) → diff 1

Input:  arr = [1, 2, 3, 4, 5]
Output: 1                          [5, 3] (sum 8) and [1, 2, 4] (sum 7) → diff 1

Input:  arr = [1, 1]
Output: 0                          Both [1] subsets → diff 0
```

<details>
<summary><h2>The Reduction</h2></summary>


If one subset sums to `s`, the other sums to `total − s`. The difference is `total − 2s`. To minimise the difference (with `s ≤ total / 2` to keep it non-negative), maximise `s`.

So:
1. Build the same subset-sum table `dp[n][s]` for `s ∈ [0, total]` (but you only need up to `total / 2`).
2. Walk `s` downward from `total / 2` to `0`. The first `s` for which `dp[n][s] = true` is the largest reachable subset sum ≤ `total / 2`.
3. Answer = `total − 2s`.

```d2
direction: right
flow: "Discrepancy via subset sum" {
  grid-rows: 1
  grid-columns: 3
  grid-gap: 20
  step1: |md
    **1. Build dp**
    `dp[n][s]` for `s` up to `total / 2`
  |
  step2: |md
    **2. Scan downward**
    Find largest `s ≤ total / 2`
    with `dp[n][s] = true`
  |
  step3: |md
    **3. Answer**
    `total − 2 · s`
  |
}
```

<p align="center"><strong>Three-step reduction. The DP gives the achievable-sums oracle; the scan picks the best half-sum; arithmetic recovers the discrepancy.</strong></p>

> *Pause. Why scan downward from `total / 2`, not upward from 0?*

Because we want the *largest* feasible `s ≤ total / 2`. Scanning down hits it first; we break and return. Scanning up would force us to walk all the way to `total / 2` and remember the largest hit — same final answer, but uglier code.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List
import sys

class Solution:
    def sets_with_smallest_discrepancy(self, arr: List[int]) -> int:
        n: int = len(arr)
        total_sum: int = sum(arr)

        # Initialize the dp array
        dp: List[List[bool]] = [
            [False] * (total_sum + 1) for _ in range(n + 1)
        ]

        # Base cases
        # If the sum is 0, we can always achieve it by not selecting any
        # element
        for i in range(n + 1):
            dp[i][0] = True

        # Fill the dp array
        for i in range(1, n + 1):
            for j in range(1, total_sum + 1):

                # If we can achieve the sum without including the current
                # element
                dp[i][j] = dp[i - 1][j]

                # If the current element is smaller than or equal to the
                # required sum, we can either include it or exclude it
                if arr[i - 1] <= j:
                    dp[i][j] = dp[i][j] or dp[i - 1][j - arr[i - 1]]

        min_diff: int = sys.maxsize

        # Find the minimum difference between two subsets
        # Start from total_sum/2 and go downwards to find the maximum
        # possible sum that can be achieved by one subset
        for j in range(total_sum // 2, -1, -1):
            if dp[n][j]:

                # If the current sum is achievable, calculate the
                # difference between the total sum and the sum of the
                # current subset
                min_diff = total_sum - 2 * j
                break

        return min_diff


# Examples from the problem statement
print(Solution().sets_with_smallest_discrepancy([1, 5, 3, 10]))   # 1
print(Solution().sets_with_smallest_discrepancy([1, 2, 3, 4, 5])) # 1
print(Solution().sets_with_smallest_discrepancy([1, 1]))           # 0

# Edge cases
print(Solution().sets_with_smallest_discrepancy([1]))              # 1  — single element
print(Solution().sets_with_smallest_discrepancy([5, 5]))           # 0  — two equal
print(Solution().sets_with_smallest_discrepancy([1, 2, 3]))        # 0  — [3] vs [1,2]
print(Solution().sets_with_smallest_discrepancy([10, 1, 1, 1]))    # 7  — 13 total
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int setsWithSmallestDiscrepancy(int[] arr) {
            int n = arr.length;
            int totalSum = 0;

            // Calculating the total sum of all elements in the array
            for (int i = 0; i < n; i++) {
                totalSum += arr[i];
            }

            // Initialize the dp array
            boolean[][] dp = new boolean[n + 1][totalSum + 1];

            // Base cases
            // If the sum is 0, we can always achieve it by not selecting any
            // element
            for (int i = 0; i <= n; i++) {
                dp[i][0] = true;
            }

            // Fill the dp array
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= totalSum; j++) {

                    // If we can achieve the sum without including the
                    // current element
                    dp[i][j] = dp[i - 1][j];

                    // If the current element is smaller than or equal to the
                    // required sum, we can either include it or exclude it
                    if (arr[i - 1] <= j) {
                        dp[i][j] = dp[i][j] || dp[i - 1][j - arr[i - 1]];
                    }
                }
            }

            int minDiff = Integer.MAX_VALUE;

            // Find the minimum difference between two subsets
            // Start from totalSum/2 and go downwards to find the maximum
            // possible sum that can be achieved by one subset
            for (int j = totalSum / 2; j >= 0; j--) {
                if (dp[n][j]) {

                    // If the current sum is achievable, calculate the
                    // difference between the total sum and the sum of the
                    // current subset
                    minDiff = totalSum - 2 * j;
                    break;
                }
            }

            return minDiff;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{1, 5, 3, 10}));   // 1
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{1, 2, 3, 4, 5})); // 1
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{1, 1}));           // 0

        // Edge cases
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{1}));              // 1
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{5, 5}));           // 0
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{1, 2, 3}));        // 0
        System.out.println(new Solution().setsWithSmallestDiscrepancy(new int[]{10, 1, 1, 1}));    // 7
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(n × total)` for the DP + `O(total)` for the scan |
| Space | `O(n × total)` |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Single element | `[7]` | `7` | Only subset split: `{7}` and `{}` → diff 7. |
| All zeros | `[0, 0, 0]` | `0` | Any partition is balanced. |
| Two equal | `[5, 5]` | `0` | Each subset gets one. |
| Large outlier | `[1, 1, 1, 100]` | `97` | Best is `{1, 1, 1}` (sum 3) and `{100}` (sum 100) → diff 97. |
| Already balanced | `[1, 2, 3, 4, 5, 5, 4, 3, 2, 1]` | `0` | Total 30, half 15; reachable. |

</details>

***

# Final Takeaway

The subset-sum pattern is one boolean DP that powers an entire family:

| Problem | Reduction |
|---|---|
| Subset sum | Direct: `dp[n][target]` |
| Partition equal sum | `dp[n][total / 2]` (with parity check) |
| Smallest discrepancy | Largest `s ≤ total / 2` with `dp[n][s] = true`; answer `total − 2s` |
| Target sum (assign +/−) | Equivalent to subset sum on `(total + target) / 2` |
| Count of subsets summing to `s` | Replace OR with sum, getting an int DP |

**You didn't just learn two more problems. You learned that a *single* boolean DP table — "which sums are achievable with which prefix" — is the substrate for many competitive-programming partition problems. Build the table; reformulate the question as a read of it; collect your answer.**

> *Transfer challenge for the next lesson:* Drop arrays of integers entirely. Now you have a 2D grid (rows × cols), and you're walking from the top-left corner to the bottom-right corner, moving only right or down. Can you compute the path with the smallest sum of cell values? Predict the recurrence shape — note that the state is naturally 2D in the *grid coordinates*, not in some derived index.

<details>
<summary><strong>Answer</strong></summary>

`dp[r][c]` = minimum-sum path from `(0, 0)` to `(r, c)`. Recurrence: `dp[r][c] = grid[r][c] + min(dp[r-1][c], dp[r][c-1])`. Base cases: row 0 and column 0 are running prefix sums. Same shape covers max-path, count-of-paths (replace min with sum), unique-paths-with-obstacles, and many other "walk a 2D grid" problems. The next lesson formalises this as the **2D-grid DP pattern**.

</details>
