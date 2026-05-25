---
title: "Zero Sum Subarrays"
summary: "Given an array arr, return the start/end indices of every subarray that sums to 0."
prereqs:
  - 11-pattern-prefix-sum/01-pattern
difficulty: medium
---

# Zero sum subarrays

## Problem Statement

Given an array `arr`, return the start/end indices of **every** subarray that sums to `0`.

### Example 1
> -   **Input:** `[6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7]`
> -   **Output:** `[[2, 4], [2, 6], [5, 6], [6, 9], [0, 10]]`

### Example 2
> -   **Input:** `[1, 2, 3, 4, 0]` → **Output:** `[[4, 4]]`

### Example 3
> -   **Input:** `[1, 2, 3]` → **Output:** `[]`

<details>
<summary><h2>Approach</h2></summary>


Same prefix-sum trick. Two indices `i < j` with `P[i] == P[j+1]` means `arr[i..j]` sums to zero. So maintain a hash map `{prefixSum → list of indices where it appeared}`; whenever we see a prefix sum that has appeared before, every previous occurrence is the *start − 1* of a zero-sum subarray ending at the current index.

The base case `prefixSumIndices[0] = [-1]` lets us catch zero-sum subarrays that start at index 0.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def zero_sum_subarrays(self, arr: List[int]) -> List[List[int]]:

        # Dictionary to store prefix sums and their indices
        prefix_sum_indices: dict[int, List[int]] = {}

        # To store the actual start and end indices of all subarrays
        result: List[List[int]] = []
        prefix_sum = 0

        # Add a base case for prefix_sum = 0
        prefix_sum_indices[0] = [-1]

        for i in range(len(arr)):
            prefix_sum += arr[i]

            # If the prefix_sum exists in the map, it means we found
            # subarrays summing to 0
            if prefix_sum in prefix_sum_indices:
                for prev_index in prefix_sum_indices[prefix_sum]:

                    # Add (prev_index + 1) as the correct start index
                    result.append([prev_index + 1, i])

            # Add the current index to the list of indices for this
            # prefix_sum
            if prefix_sum not in prefix_sum_indices:
                prefix_sum_indices[prefix_sum] = []
            prefix_sum_indices[prefix_sum].append(i)

        return result


# Examples from the problem statement
r1 = Solution().zero_sum_subarrays([6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7])
print(sorted([sorted(p) for p in r1]))   # [[0, 10], [2, 4], [2, 6], [5, 6], [6, 9]]

print(Solution().zero_sum_subarrays([1, 2, 3, 4, 0]))  # [[4, 4]]
print(Solution().zero_sum_subarrays([1, 2, 3]))         # []

# Edge cases
print(Solution().zero_sum_subarrays([]))                # []
print(Solution().zero_sum_subarrays([0]))               # [[0, 0]]
print(Solution().zero_sum_subarrays([-1, 1]))           # [[0, 1]]
print(Solution().zero_sum_subarrays([1, -1, 1, -1]))    # [[0, 1], [0, 3], [2, 3]]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<List<Integer>> zeroSumSubarrays(int[] arr) {

            // Map to store prefix sums and their indices
            Map<Integer, List<Integer>> prefixSumIndices = new HashMap<>();

            // To store the actual start and end indices of all subarrays
            List<List<Integer>> result = new ArrayList<>();
            int prefixSum = 0;

            // Add a base case for prefixSum = 0
            prefixSumIndices.put(0, new ArrayList<>());
            prefixSumIndices.get(0).add(-1);

            for (int i = 0; i < arr.length; i++) {
                prefixSum += arr[i];

                // If the prefixSum exists in the map, it means we found
                // subarrays summing to 0
                if (prefixSumIndices.containsKey(prefixSum)) {
                    for (int prevIndex : prefixSumIndices.get(prefixSum)) {

                        // Add (prevIndex + 1) as the correct start index
                        List<Integer> subarray = new ArrayList<>();
                        subarray.add(prevIndex + 1);
                        subarray.add(i);
                        result.add(subarray);
                    }
                }

                // Add the current index to the list of indices for this
                // prefixSum
                prefixSumIndices
                    .computeIfAbsent(prefixSum, k -> new ArrayList<>())
                    .add(i);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        var r1 = new Solution().zeroSumSubarrays(new int[]{6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7});
        r1.forEach(p -> { Collections.sort(p); System.out.print(p + " "); }); System.out.println();
        // [0, 10] [2, 4] [2, 6] [5, 6] [6, 9] (order may vary)

        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, 2, 3, 4, 0}));  // [[4, 4]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, 2, 3}));         // []

        // Edge cases
        System.out.println(new Solution().zeroSumSubarrays(new int[]{}));                // []
        System.out.println(new Solution().zeroSumSubarrays(new int[]{0}));               // [[0, 0]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{-1, 1}));           // [[0, 1]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, -1, 1, -1}));   // [[0, 1], [0, 3], [2, 3]]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Prefix sum is the bridge that turns *quadratic* subarray problems into *linear* ones. The pattern is so flexible it shows up in problems that don't even mention sums — anywhere "balanced" or "equal" or "net zero" can be encoded as a sum, the same machinery applies.

The four moves:

1. **Define the prefix.** Sum, signed-count, balance, parity — pick whatever encodes "the question".
2. **Re-encode if needed.** "Equal 0s and 1s" → 0 ↦ −1, 1 ↦ +1. "Equal As and non-As" → As ↦ +1, others ↦ −1. "Subarray sum = K" works directly.
3. **Hash map of prefix values.** What you store depends on the answer shape: first-index for *longest*, list of indices for *all*, count for *how many*.
4. **The `0 → −1` (or `0 → −1` index) base case.** Forgetting it is the canonical bug. Always start with `map[0] = -1` (for first-index) or `map[0] = 1` (for count).

> **A panoramic view of the five hash-table patterns:**
>
> | Pattern | Question shape | Best hash-map use |
> |---|---|---|
> | Counting | "how often / how many of X?" | freq map of items |
> | Key generation | "which inputs are equivalent?" | key → group |
> | Fixed sliding window | "for each window of size K, …?" | freq map of window |
> | Variable sliding window | "longest/shortest with property P?" | freq map + grow/shrink |
> | Prefix sum + hash | "subarray sum = X?" / "balanced subarray?" | prefix-value → index/count |

> *Coming up — a different kind of lesson. The next file is the **design** lesson: two classic interview problems (LRU cache, RandomisedSet) where you build whole composite data structures from a hash table plus one other piece (a doubly-linked list, a dynamic array). The patterns we just covered prepared us; design problems force us to combine them into a single coherent structure.*

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
