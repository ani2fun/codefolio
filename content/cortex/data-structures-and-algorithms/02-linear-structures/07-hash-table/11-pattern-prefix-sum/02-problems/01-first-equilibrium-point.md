---
title: "First Equilibrium Point"
summary: "Given an array arr, return the first index i such that sum(arr[0..i-1]) == sum(arr[i+1..n-1]). Return -1 if no such index exists."
prereqs:
  - 11-pattern-prefix-sum/01-pattern
difficulty: easy
---

# First equilibrium point

## Problem Statement

Given an array `arr`, return the first index `i` such that `sum(arr[0..i-1]) == sum(arr[i+1..n-1])`. Return `-1` if no such index exists.

### Example 1
> -   **Input:** `arr = [1, 3, 5, 2, 2]` → **Output:** `2` (sum left = sum right = 4)

### Example 2
> -   **Input:** `arr = [5, 5, 5, 5, 5]` → **Output:** `2` (sum left = sum right = 10)

### Example 3
> -   **Input:** `arr = [1, 3, 5, 10]` → **Output:** `-1`

<details>
<summary><h2>Approach</h2></summary>


Build a `prefix_sum` array where `prefix_sum[i]` holds the sum of the first `i` elements (so `prefix_sum[0] = 0`). With it, any range sum is a constant-time subtraction. For candidate index `i − 1`, the elements strictly to the left sum to `prefix_sum[i] − arr[i - 1]`, and the elements strictly to the right sum to `prefix_sum[n] − prefix_sum[i]`. Walk the array and return the first index where those two equal. One warm-up pass to fill `prefix_sum`, one pass to scan — **O(N)**.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def first_equilibrium_point(self, arr: List[int]) -> int:

        # calculate the prefix sum of the array
        prefix_sum = [0] * (len(arr) + 1)
        for i in range(1, len(arr) + 1):
            prefix_sum[i] = prefix_sum[i - 1] + arr[i - 1]

        # check for equilibrium point
        for i in range(1, len(arr) + 1):

            # calculate sum of elements before and after the current
            # index
            left_sum = prefix_sum[i] - arr[i - 1]
            right_sum = prefix_sum[len(arr)] - prefix_sum[i]

            # if both sums are equal, return the current index as
            # equilibrium point
            if left_sum == right_sum:
                return i - 1

        # no equilibrium point found
        return -1


# Examples from the problem statement
print(Solution().first_equilibrium_point([1, 3, 5, 2, 2]))  # 2
print(Solution().first_equilibrium_point([5, 5, 5, 5, 5]))  # 2
print(Solution().first_equilibrium_point([1, 3, 5, 10]))    # -1

# Edge cases
print(Solution().first_equilibrium_point([1]))               # 0
print(Solution().first_equilibrium_point([0, 0, 0]))         # 0
print(Solution().first_equilibrium_point([1, 2, 3]))         # -1
print(Solution().first_equilibrium_point([2, 1, 2]))         # 1
print(Solution().first_equilibrium_point([0]))               # 0
```

```java run
public class Main {
    static class Solution {
        public int firstEquilibriumPoint(int[] arr) {

            // calculate the prefix sum of the array
            int[] prefixSum = new int[arr.length + 1];
            prefixSum[0] = 0;
            for (int i = 1; i <= arr.length; i++) {
                prefixSum[i] = prefixSum[i - 1] + arr[i - 1];
            }

            // check for equilibrium point
            for (int i = 1; i <= arr.length; i++) {

                // calculate sum of elements before and after the current
                // index
                int leftSum = prefixSum[i] - arr[i - 1];
                int rightSum = prefixSum[arr.length] - prefixSum[i];

                // if both sums are equal, return the current index as
                // equilibrium point
                if (leftSum == rightSum) {
                    return i - 1;
                }
            }

            // no equilibrium point found
            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 3, 5, 2, 2}));  // 2
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{5, 5, 5, 5, 5}));  // 2
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 3, 5, 10}));    // -1

        // Edge cases
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1}));               // 0
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{0, 0, 0}));         // 0
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 2, 3}));         // -1
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{2, 1, 2}));         // 1
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{0}));               // 0
    }
}
```

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Examples — missing, needs to be written -->
<!--       Guidance: min 3 examples: basic / variant / edge -->

<!-- TODO: Intuition — missing, needs to be written -->
<!--       Guidance: 3 paragraphs: brute force / observation / pattern fit -->

<!-- TODO: Applying the Diagnostic Questions — missing, needs to be written -->
<!--       Guidance: REQUIRED, never optional -->
<!--       Guidance: 4-row table. Columns: 'Check' | 'Answer for [Problem Name]' -->
<!--       Guidance: Rows: two positions simultaneously / one near start one near end / both move inward / simple O(1) work at each step -->

<!-- TODO: Approach — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Solution — missing, needs to be written -->
<!--       Guidance: Python block then Java block -->

<!-- TODO: Dry Run — missing, needs to be written -->
<!--       Guidance: walk through a small example step by step -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table: time / space / why -->

<!-- TODO: Edge Cases — missing, needs to be written -->
<!--       Guidance: table, min 5 rows -->

<!-- TODO: Key Takeaway — missing, needs to be written -->
<!--       Guidance: 1–2 sentences -->
