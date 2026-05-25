---
title: "Subarray Sum Equals K"
summary: "Given an integer array arr and target k, return the maximum length of a subarray whose elements sum to k. Return 0 if no such subarray exists."
prereqs:
  - 10-pattern-variable-sized-sliding-window/01-pattern
difficulty: medium
---

# Subarray sum equals k

## Problem Statement

Given an integer array `arr` and target `k`, return the maximum length of a subarray whose elements sum to `k`. Return `0` if no such subarray exists.

### Example 1
> -   **Input:** `arr = [4, 4, 2, 6, 4], k = 10` → **Output:** `3` (`[4, 4, 2]`)

### Example 2
> -   **Input:** `arr = [2, 2, 1, 2, 4, 3], k = 7` → **Output:** `4` (`[2, 2, 1, 2]`)

### Example 3
> -   **Input:** `arr = [2, 3, 1, 2, 4, 3], k = 100` → **Output:** `0`

<details>
<summary><h2>Approach</h2></summary>


> *A small detour from sliding windows* — when the array can contain negatives, the window-shrinking-on-violation trick fails (extending might *decrease* the sum, and shrinking might *increase* it; the rule isn't monotonic). The right tool here is a **prefix-sum + hash map**, which the next lesson covers in full. We touch on it here as a preview.

The trick: for each prefix sum `P[i]`, we want to find an earlier index `j` with `P[j] = P[i] − k` — because then the subarray `arr[j+1..i]` sums to exactly `k`. Maintain a hash map `sum_index_map` from each prefix sum to the earliest index at which it occurred; for each new prefix sum, look up `sum − k` and compute the length.

This is technically a hash-table technique, not a sliding window, but the original course groups it here.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List
from collections import defaultdict

class Solution:
    def subarray_sum_equals_k(self, arr: List[int], k: int) -> int:

        # Create a map to store the sum of elements up to each index
        sum_index_map = defaultdict(int)

        # Initialize the sum to zero and the maximum length to zero
        sum = 0
        max_len = 0

        # Initialize start and end to 0
        start = 0
        end = 0

        # Move the window one step to the right until it reaches the end
        # of the array
        while end < len(arr):

            # Add contribution of arr[end]
            sum += arr[end]

            # Check if the current sum equals the target value k
            if sum == k:

                # Update the maximum length
                max_len = end + 1

            # Check if sum - k exists in the map
            if sum - k in sum_index_map:

                # Update the maximum length if the current length is
                # greater
                max_len = max(max_len, end - sum_index_map[sum - k])

            # Store the current sum with the current index if not already
            # present
            if sum not in sum_index_map:
                sum_index_map[sum] = end

            # Move the end index
            end += 1

        # Return the maximum length
        return max_len


# Examples from the problem statement
print(Solution().subarray_sum_equals_k([4, 4, 2, 6, 4], 10))      # 3
print(Solution().subarray_sum_equals_k([2, 2, 1, 2, 4, 3], 7))    # 4
print(Solution().subarray_sum_equals_k([2, 3, 1, 2, 4, 3], 100))  # 0

# Edge cases
print(Solution().subarray_sum_equals_k([], 0))                     # 0
print(Solution().subarray_sum_equals_k([1], 1))                    # 1
print(Solution().subarray_sum_equals_k([1, 2, 3], 6))              # 3
print(Solution().subarray_sum_equals_k([1, -1, 1], 1))             # 3
print(Solution().subarray_sum_equals_k([1, 2, 3], 0))              # 0
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int subarraySumEqualsK(int[] arr, int k) {

            // Create a map to store the sum of elements up to each index
            HashMap<Integer, Integer> sumIndexMap = new HashMap<>();

            // Initialize the sum to zero and the maximum length to zero
            int sum = 0;
            int maxLen = 0;

            // Initialize start and end to 0
            int start = 0;
            int end = 0;

            // Move the window one step to the right until it reaches the end
            // of the array
            while (end < arr.length) {

                // Add contribution of arr[end]
                sum += arr[end];

                // Check if the current sum equals the target value k
                if (sum == k) {

                    // Update the maximum length
                    maxLen = end + 1;
                }

                // Check if sum - k exists in the map
                if (sumIndexMap.containsKey(sum - k)) {

                    // Update the maximum length if the current length is
                    // greater
                    maxLen = Math.max(
                        maxLen,
                        end - sumIndexMap.get(sum - k)
                    );
                }

                // Store the current sum with the current index if not
                // already present
                if (!sumIndexMap.containsKey(sum)) {
                    sumIndexMap.put(sum, end);
                }

                // Move the end index
                end++;
            }

            // Return the maximum length
            return maxLen;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().subarraySumEqualsK(new int[]{4, 4, 2, 6, 4}, 10));      // 3
        System.out.println(new Solution().subarraySumEqualsK(new int[]{2, 2, 1, 2, 4, 3}, 7));    // 4
        System.out.println(new Solution().subarraySumEqualsK(new int[]{2, 3, 1, 2, 4, 3}, 100));  // 0

        // Edge cases
        System.out.println(new Solution().subarraySumEqualsK(new int[]{}, 0));                    // 0
        System.out.println(new Solution().subarraySumEqualsK(new int[]{1}, 1));                   // 1
        System.out.println(new Solution().subarraySumEqualsK(new int[]{1, 2, 3}, 6));             // 3
        System.out.println(new Solution().subarraySumEqualsK(new int[]{1, -1, 1}, 1));            // 3
        System.out.println(new Solution().subarraySumEqualsK(new int[]{1, 2, 3}, 0));             // 0
    }
}
```


> *Spoiler* — this is the prefix-sum pattern, the topic of the next lesson. Read it as a preview; the full treatment is one click away.

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
