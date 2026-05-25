---
title: "Balanced Binary Subarray"
summary: "Given a binary array arr (only 0s and 1s), return the length of the longest subarray with an equal number of 0s and 1s."
prereqs:
  - 11-pattern-prefix-sum/01-pattern
difficulty: medium
---

# Balanced binary subarray

## Problem Statement

Given a binary array `arr` (only 0s and 1s), return the length of the longest subarray with an **equal** number of 0s and 1s.

### Example 1
> -   **Input:** `[1, 0, 1, 1, 1, 0, 0]` → **Output:** `6` (indices 1..6)

### Example 2
> -   **Input:** `[0, 0, 1, 1, 0]` → **Output:** `4`

### Example 3
> -   **Input:** `[1, 1, 1, 1]` → **Output:** `0`

<details>
<summary><h2>Approach</h2></summary>


The encoding trick: **0 → −1, 1 → +1**. Now "equal 0s and 1s" becomes "subarray sums to 0", which becomes "two prefix sums are equal". Walk the array maintaining `prefixSum`; for each new value, check if it has been seen before. If yes, the subarray between the first occurrence and now has sum 0 → equal counts. Track the max length.

The `prefixSumIndex[0] = -1` base case handles subarrays starting at index 0.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def balanced_binary_subarray(self, arr: List[int]) -> int:

        # Dictionary to store the first occurrence of each prefix sum
        prefix_sum_index = {}

        # To store the maximum length of the subarray
        max_length = 0

        # Initialize the prefix sum
        prefix_sum = 0

        # Add a base case for prefix_sum = 0
        prefix_sum_index[0] = -1

        for i in range(len(arr)):

            # Treat 0 as -1 for the prefix sum calculation
            prefix_sum += -1 if arr[i] == 0 else 1

            # If the prefix sum has been seen before
            if prefix_sum in prefix_sum_index:

                # Calculate the length of the subarray
                length = i - prefix_sum_index[prefix_sum]
                max_length = max(max_length, length)

            # Otherwise, store the first occurrence of this prefix
            # sum
            else:
                prefix_sum_index[prefix_sum] = i

        return max_length


# Examples from the problem statement
print(Solution().balanced_binary_subarray([1, 0, 1, 1, 1, 0, 0]))  # 6
print(Solution().balanced_binary_subarray([0, 0, 1, 1, 0]))        # 4
print(Solution().balanced_binary_subarray([1, 1, 1, 1]))            # 0

# Edge cases
print(Solution().balanced_binary_subarray([]))                       # 0
print(Solution().balanced_binary_subarray([0]))                      # 0
print(Solution().balanced_binary_subarray([0, 1]))                   # 2
print(Solution().balanced_binary_subarray([0, 0, 1, 1]))             # 4
print(Solution().balanced_binary_subarray([1, 0]))                   # 2
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int balancedBinarySubarray(int[] arr) {

            // Map to store the first occurrence of each prefix sum
            Map<Integer, Integer> prefixSumIndex = new HashMap<>();

            // To store the maximum length of the subarray
            int maxLength = 0;

            // Initialize the prefix sum
            int prefixSum = 0;

            // Add a base case for prefixSum = 0
            prefixSumIndex.put(0, -1);

            for (int i = 0; i < arr.length; i++) {

                // Treat 0 as -1 for the prefix sum calculation
                prefixSum += (arr[i] == 0 ? -1 : 1);

                // If the prefix sum has been seen before
                if (prefixSumIndex.containsKey(prefixSum)) {

                    // Calculate the length of the subarray
                    int length = i - prefixSumIndex.get(prefixSum);
                    maxLength = Math.max(maxLength, length);
                }

                // Otherwise, store the first occurrence of this prefix
                // sum
                else {
                    prefixSumIndex.put(prefixSum, i);
                }
            }

            return maxLength;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 0, 1, 1, 1, 0, 0})); // 6
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 0, 1, 1, 0}));       // 4
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 1, 1, 1}));           // 0

        // Edge cases
        System.out.println(new Solution().balancedBinarySubarray(new int[]{}));                     // 0
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0}));                    // 0
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 1}));                 // 2
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 0, 1, 1}));           // 4
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 0}));                 // 2
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
