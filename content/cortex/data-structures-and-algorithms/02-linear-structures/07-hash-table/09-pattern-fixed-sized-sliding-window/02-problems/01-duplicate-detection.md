---
title: "Duplicate Detection"
summary: "Given an integer array arr and a positive integer k, return true if any subarray of size k contains a duplicate, false otherwise."
prereqs:
  - 09-pattern-fixed-sized-sliding-window/01-pattern
difficulty: easy
---

# Duplicate detection

## Problem Statement

Given an integer array `arr` and a positive integer `k`, return `true` if any subarray of size `k` contains a duplicate, `false` otherwise.

### Example 1
> -   **Input:** `arr = [2, 1, 2, 3, 2, 1, 4, 5], k = 5` → **Output:** `true`

### Example 2
> -   **Input:** `arr = [1, 1, 2, 4], k = 3` → **Output:** `true`

### Example 3
> -   **Input:** `arr = [1, 2, 3, 4], k = 2` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


Slide a window of size `k`; maintain frequencies. The instant any frequency exceeds 1 inside a `k`-window, we have a duplicate — return `true`.

> *Mental shortcut* — duplicate-in-window is "is the size of the window's distinct-set less than k?". Equivalently, "did any insert push a frequency above 1?". The hash map gives both views in O(1).

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from collections import defaultdict
from typing import List

class Solution:
    def duplicate_detection(self, arr: List[int], k: int) -> bool:

        # Map to store elements within the window and their counts
        frequency = defaultdict(int)

        # The start and end pointers for the window
        start, end = 0, 0

        while end < len(arr):

            # Add the current element to the window
            end_element = arr[end]
            frequency[end_element] += 1

            # Adjust the window size if it exceeds k
            if end - start >= k:
                start_element = arr[start]
                frequency[start_element] -= 1

                # Erase the current element from the window if its
                # frequency becomes 0
                if frequency[start_element] == 0:
                    del frequency[start_element]
                start += 1

            # Check if there's a duplicate in the window
            if frequency[end_element] > 1:
                return True

            # Move the end pointer to expand the window
            end += 1

        return False


# Examples from the problem statement
print(Solution().duplicate_detection([2, 1, 2, 3, 2, 1, 4, 5], 5))  # True
print(Solution().duplicate_detection([1, 1, 2, 4], 3))               # True
print(Solution().duplicate_detection([1, 2, 3, 4], 2))               # False

# Edge cases
print(Solution().duplicate_detection([], 3))                          # False
print(Solution().duplicate_detection([1], 1))                         # False
print(Solution().duplicate_detection([1, 2], 1))                      # False
print(Solution().duplicate_detection([1, 1], 2))                      # True
print(Solution().duplicate_detection([1, 2, 1], 5))                   # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public boolean duplicateDetection(int[] arr, int k) {

            // Map to store elements within the window and their counts
            Map<Integer, Integer> frequency = new HashMap<>();

            // The start and end pointers for the window
            int start = 0;
            int end = 0;

            while (end < arr.length) {

                // Add the current element to the window
                int endElement = arr[end];
                frequency.put(
                    endElement,
                    frequency.getOrDefault(endElement, 0) + 1
                );

                // Adjust the window size if it exceeds k
                if (end - start >= k) {
                    int startElement = arr[start];
                    frequency.put(
                        startElement,
                        frequency.get(startElement) - 1
                    );

                    // Erase the current element from the window if its
                    // frequency becomes 0
                    if (frequency.get(startElement) == 0) {
                        frequency.remove(startElement);
                    }
                    start++;
                }

                // Check if there's a duplicate in the window
                if (frequency.get(endElement) > 1) {
                    return true;
                }

                // Move the end pointer to expand the window
                end++;
            }

            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().duplicateDetection(new int[]{2, 1, 2, 3, 2, 1, 4, 5}, 5)); // true
        System.out.println(new Solution().duplicateDetection(new int[]{1, 1, 2, 4}, 3));              // true
        System.out.println(new Solution().duplicateDetection(new int[]{1, 2, 3, 4}, 2));              // false

        // Edge cases
        System.out.println(new Solution().duplicateDetection(new int[]{}, 3));                        // false
        System.out.println(new Solution().duplicateDetection(new int[]{1}, 1));                       // false
        System.out.println(new Solution().duplicateDetection(new int[]{1, 2}, 1));                    // false
        System.out.println(new Solution().duplicateDetection(new int[]{1, 1}, 2));                    // true
        System.out.println(new Solution().duplicateDetection(new int[]{1, 2, 1}, 5));                 // true
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
