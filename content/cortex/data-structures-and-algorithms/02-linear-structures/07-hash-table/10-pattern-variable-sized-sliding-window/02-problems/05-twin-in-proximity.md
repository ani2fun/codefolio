---
title: "Twin in Proximity"
summary: "Given an array arr and integer k, return true if there are two distinct indices i and j with arr[i] == arr[j] and |i − j| ≤ k. Otherwise return false."
prereqs:
  - 10-pattern-variable-sized-sliding-window/01-pattern
difficulty: medium
---

# Twin in proximity

## Problem Statement

Given an array `arr` and integer `k`, return `true` if there are two distinct indices `i` and `j` with `arr[i] == arr[j]` and `|i − j| ≤ k`. Otherwise return `false`.

### Example 1
> -   **Input:** `arr = [1,2,3,4,1], k = 5` → **Output:** `true` (indices 0, 4; distance 4 ≤ 5)

### Example 2
> -   **Input:** `arr = [1,2,3,4,5,6,1], k = 5` → **Output:** `false` (closest twin is distance 6)

### Example 3
> -   **Input:** `arr = [1,7], k = 5` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


Keep a hash map `element_index` from each value to the **most recent index** at which it appeared. When the right pointer reaches `arr[end]`, look the value up: if it's in the map *and* the gap to its stored index is `≤ k`, the two occurrences are a twin within distance `k` — return `true`. Otherwise overwrite the map entry with the current index. Conceptually the map's live entries are a sliding set of the last `k + 1` values; the window is kept that size by deleting `arr[start]`'s entry once `end − start ≥ k` and advancing `start`.

> 🖼 Diagram — Twin in proximity — maintain a set of the last k+1 values; if the new element is already in the set, a twin exists within distance k.
```d2
direction: right

inp: "arr = [1, 2, 3, 4, 1], k = 5"

s: "set after [1, 2, 3, 4]" {
  grid-columns: 4
  grid-gap: 0
  e1: "1"
  e2: "2"
  e3: "3"
  e4: "4"
}

check: "read 1 -> already in set"

r: "return true" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}

inp -> s -> check -> r
```

<p align="center"><strong>Twin in proximity — maintain a set of the last <code>k+1</code> values; if the new element is already in the set, a twin exists within distance <code>k</code>.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def twin_in_proximity(self, arr: List[int], k: int) -> bool:

        # Dictionary to store the most recent index of each element
        element_index = {}

        # Sliding window pointers
        start, end = 0, 0

        while end < len(arr):

            # Check if the current element exists in the map and is
            # within range
            if (
                arr[end] in element_index
                and end - element_index[arr[end]] <= k
            ):

                # Found a duplicate within the required range
                return True

            # Update the map with the current element's index
            element_index[arr[end]] = end

            # Maintain the window size by removing elements out of range
            if end - start >= k:
                del element_index[arr[start]]

                # Shrink the window
                start += 1

            # Expand the window
            end += 1

        # No duplicates found within the range
        return False


# Examples from the problem statement
print(Solution().twin_in_proximity([1, 2, 3, 4, 1], 5))        # True
print(Solution().twin_in_proximity([1, 2, 3, 4, 5, 6, 1], 5))  # False
print(Solution().twin_in_proximity([1, 7], 5))                  # False

# Edge cases
print(Solution().twin_in_proximity([], 1))                      # False
print(Solution().twin_in_proximity([1], 1))                     # False
print(Solution().twin_in_proximity([1, 1], 1))                  # True
print(Solution().twin_in_proximity([1, 2, 1], 1))               # False
print(Solution().twin_in_proximity([1, 2, 1], 2))               # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public boolean twinInProximity(int[] arr, int k) {

            // Map to store the most recent index of each element
            Map<Integer, Integer> elementIndex = new HashMap<>();

            // Sliding window pointers
            int start = 0;
            int end = 0;

            while (end < arr.length) {

                // Check if the current element exists in the map and is
                // within range
                if (
                    elementIndex.containsKey(arr[end]) &&
                    end - elementIndex.get(arr[end]) <= k
                ) {

                    // Found a duplicate within the required range
                    return true;
                }

                // Update the map with the current element's index
                elementIndex.put(arr[end], end);

                // Maintain the window size by removing elements out of range
                if (end - start >= k) {
                    elementIndex.remove(arr[start]);

                    // Shrink the window
                    start++;
                }

                // Expand the window
                end++;
            }

            // No duplicates found within the range
            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().twinInProximity(new int[]{1, 2, 3, 4, 1}, 5));        // true
        System.out.println(new Solution().twinInProximity(new int[]{1, 2, 3, 4, 5, 6, 1}, 5));  // false
        System.out.println(new Solution().twinInProximity(new int[]{1, 7}, 5));                  // false

        // Edge cases
        System.out.println(new Solution().twinInProximity(new int[]{}, 1));                      // false
        System.out.println(new Solution().twinInProximity(new int[]{1}, 1));                     // false
        System.out.println(new Solution().twinInProximity(new int[]{1, 1}, 1));                  // true
        System.out.println(new Solution().twinInProximity(new int[]{1, 2, 1}, 1));               // false
        System.out.println(new Solution().twinInProximity(new int[]{1, 2, 1}, 2));               // true
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


The variable-sized sliding window is the most flexible hash-table technique in this section. It handles a vast family of "find the longest/shortest contiguous something with property P" problems in **O(N)**, replacing nested-loop brute force with a single pass of two pointers.

Three lessons:

1. **Expand greedily, contract conditionally.** The right pointer always moves forward by one. The left pointer moves forward *only when the rule is violated*, and as far as needed to restore it. This asymmetry is what gives the algorithm its O(N) bound.
2. **`while`, not `if`.** Contract until the rule is satisfied — not just by one step. A single expansion can blow past the rule by many slots; the loop has to drain all of them before the next expansion.
3. **The map summarises the window.** Frequencies, distinct-counts, max-counts, sums — the map is whatever the rule needs to check in O(1). Pick the *smallest* summary that lets you decide expand-vs-contract; bigger summaries are wasted work.

> *Coming up — the **prefix-sum + hash** pattern. Sliding windows fail when the rule is non-monotonic (think arrays with negatives, or "exact sum equals K"). The prefix-sum trick rescues these problems by transforming "subarray sum" into "difference of two prefix sums" — and a hash map of prefix sums turns that into a single-pass O(N) algorithm. We saw a teaser in the subarray-sum-equals-k problem above; the next lesson opens the toolbox.*

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
