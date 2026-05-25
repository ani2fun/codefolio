---
title: "Subarray Distinctness"
summary: "Given arr and a positive integer k, return an array containing the count of distinct elements in every contiguous subarray of size k."
prereqs:
  - 09-pattern-fixed-sized-sliding-window/01-pattern
difficulty: medium
---

# Subarray distinctness

## Problem Statement

Given `arr` and a positive integer `k`, return an array containing the count of distinct elements in every contiguous subarray of size `k`.

### Example 1
> -   **Input:** `arr = [2,1,2,3,2,1,4,5], k = 5` → **Output:** `[3, 3, 4, 5]`

### Example 2
> -   **Input:** `arr = [1,1,2,4], k = 3` → **Output:** `[2, 3]`

### Example 3
> -   **Input:** `arr = [1,2,3,4], k = 1` → **Output:** `[1, 1, 1, 1]`

<details>
<summary><h2>Approach</h2></summary>


The number of *distinct* elements in the window is exactly `len(freq_map)` — the number of keys with non-zero count. The trick: when a frequency drops to zero on contraction, **delete the key** from the map so the size reflects only currently-present elements.

> 🖼 Diagram — Distinct count via map size — every distinct element is one key in the map. Maintain the map's invariant that "count is non-zero" by deleting zero-count keys on contraction, and len(map) is your answer.
```d2
direction: right

w: "window contents" {
  grid-columns: 5
  grid-gap: 0
  a0: "2"
  a1: "1"
  a2: "2"
  a3: "3"
  a4: "2"
}

m: "freq map" {
  m1: "2 -> 3"
  m2: "1 -> 1"
  m3: "3 -> 1"
}

d: |md
  **distinct = len(freq) = 3**
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}

w -> m
m -> d
```

<p align="center"><strong>Distinct count via map size — every distinct element is one key in the map. Maintain the map's invariant that "count is non-zero" by deleting zero-count keys on contraction, and <code>len(map)</code> is your answer.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from collections import defaultdict
from typing import List

class Solution:
    def subarray_distinctness(self, arr: List[int], k: int) -> List[int]:

        # Initialize a dictionary to keep track of the count of elements
        # in the current window
        frequency = defaultdict(int)

        # Initialize the start and end indices of the window
        start, end = 0, 0

        # Initialize the result list to hold the count of distinct
        # elements in every subarray
        result = []

        # Loop through the array
        while end < len(arr):

            # Add the current element to the count dictionary
            frequency[arr[end]] += 1

            # If the current window size is equal to k, calculate the
            # count of distinct elements
            if end - start + 1 == k:
                result.append(len(frequency))

                # Remove the leftmost element from the count dictionary
                frequency[arr[start]] -= 1
                if frequency[arr[start]] == 0:
                    del frequency[arr[start]]

                # Contract the window
                start += 1

            # Expand the window to the right
            end += 1

        return result


# Examples from the problem statement
print(Solution().subarray_distinctness([2, 1, 2, 3, 2, 1, 4, 5], 5))  # [3, 3, 4, 5]
print(Solution().subarray_distinctness([1, 1, 2, 4], 3))               # [2, 3]
print(Solution().subarray_distinctness([1, 2, 3, 4], 1))               # [1, 1, 1, 1]

# Edge cases
print(Solution().subarray_distinctness([1], 1))                         # [1]
print(Solution().subarray_distinctness([1, 1, 1, 1], 2))               # [1, 1, 1]
print(Solution().subarray_distinctness([1, 2, 3, 4], 4))               # [4]
print(Solution().subarray_distinctness([5, 5, 5], 3))                  # [1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<Integer> subarrayDistinctness(int[] arr, int k) {

            // Initialize a map to keep track of the count of elements in the
            // current window
            Map<Integer, Integer> frequency = new HashMap<>();

            // Initialize the start and end indices of the window
            int start = 0;
            int end = 0;

            // Initialize the result list to hold the count of distinct
            // elements in every subarray
            List<Integer> result = new ArrayList<>();

            // Loop through the array
            while (end < arr.length) {

                // Add the current element to the count map
                frequency.put(
                    arr[end],
                    frequency.getOrDefault(arr[end], 0) + 1
                );

                // If the current window size is equal to k, calculate the
                // count of distinct elements
                if (end - start + 1 == k) {
                    result.add(frequency.size());

                    // Remove the leftmost element from the count map
                    int startElement = arr[start];
                    frequency.put(
                        startElement,
                        frequency.get(startElement) - 1
                    );
                    if (frequency.get(startElement) == 0) {
                        frequency.remove(startElement);
                    }

                    // Contract the window
                    start++;
                }

                // Expand the window to the right
                end++;
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().subarrayDistinctness(new int[]{2, 1, 2, 3, 2, 1, 4, 5}, 5)); // [3, 3, 4, 5]
        System.out.println(new Solution().subarrayDistinctness(new int[]{1, 1, 2, 4}, 3));              // [2, 3]
        System.out.println(new Solution().subarrayDistinctness(new int[]{1, 2, 3, 4}, 1));              // [1, 1, 1, 1]

        // Edge cases
        System.out.println(new Solution().subarrayDistinctness(new int[]{1}, 1));                       // [1]
        System.out.println(new Solution().subarrayDistinctness(new int[]{1, 1, 1, 1}, 2));              // [1, 1, 1]
        System.out.println(new Solution().subarrayDistinctness(new int[]{1, 2, 3, 4}, 4));              // [4]
        System.out.println(new Solution().subarrayDistinctness(new int[]{5, 5, 5}, 3));                 // [1]
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
