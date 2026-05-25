---
title: "First and Last Position"
summary: "Given a sorted array and target, return [first_index, last_index] of target, or [-1, -1] if absent."
prereqs:
  - 09-pattern-lower-bound/01-pattern
difficulty: medium
---

# First and Last Position

Two `O(log n)` queries — `lower_bound(target)` for the first index, `lower_bound(target + 1) - 1` for the last.

## The Problem

Given a sorted array and target, return `[first_index, last_index]` of target, or `[-1, -1]` if absent.

```
Input:  arr = [1, 2, 2, 2, 3, 4], target = 2
Output: [1, 3]

Input:  arr = [1, 2, 2, 2, 3, 4], target = 3
Output: [4, 4]

Input:  arr = [1, 2, 2, 2, 3, 4], target = 5
Output: [-1, -1]
```

<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def lower_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the lower bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # >= the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than arr[target], then find in
            # right subarray
            if arr[mid] < target:
                low = mid + 1

            # If arr[mid] is greater than or equal to target, then it may
            # be the answer. So, instead of high = mid - 1, we do high = mid
            # to include mid in the next search space
            else:
                high = mid

        # Return the lower bound index, it could be equal to len(arr)
        # if all elements are less than target
        return low

    def first_and_last_position(
        self, arr: List[int], target: int
    ) -> List[int]:

        # Initialize the result list with -1 values
        result = [-1, -1]

        # Find the first occurrence of target
        first: int = self.lower_bound(arr, target)

        # Find the lower bound of target+1 and subtract 1 to get the
        # last occurrence of target
        last: int = self.lower_bound(arr, target + 1) - 1

        # Check if the lower bound index is within the list bounds and if
        # the value is the target
        if first < len(arr) and arr[first] == target:

            # Return the range [first, last]
            return [first, last]

        # Return the default result list
        return result


# Examples from the problem statement
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 2))  # [1, 3]
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 3))  # [4, 4]
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 5))  # [-1, -1]

# Edge cases
print(Solution().first_and_last_position([], 1))                    # [-1, -1] — empty array
print(Solution().first_and_last_position([5], 5))                   # [0, 0] — single element match
print(Solution().first_and_last_position([5], 3))                   # [-1, -1] — single element miss
print(Solution().first_and_last_position([2, 2, 2, 2], 2))         # [0, 3] — all same
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 1))   # [0, 0] — target at first
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int lowerBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the lower bound index would be equal to
            // arr.length
            int high = arr.length;

            // 'high' is exclusive (can be arr.length), so we use 'low <
            // high' instead of 'low <= high'. This loop finds the first
            // index where the element is
            // >= the target without going out of bounds.
            while (low < high) {

                // Find the middle index
                int mid = low + (high - low) / 2;

                // If arr[mid] is less than arr[target], then find in
                // right subarray
                if (arr[mid] < target) {
                    low = mid + 1;
                }

                // If arr[mid] is greater than or equal to target, then it
                // may be the answer. So, instead of high = mid - 1, we do
                // high = mid to include mid in the next search space
                else {
                    high = mid;
                }
            }

            // Return the lower bound index, it could be equal to arr.length
            // if all elements are less than target
            return low;
        }

        public int[] firstAndLastPosition(int[] arr, int target) {

            // Initialize the result array with -1 values
            int[] result = new int[] { -1, -1 };

            // Find the first occurrence of target
            int first = lowerBound(arr, target);

            // Find the lower bound of target+1 and subtract 1 to get the
            // last occurrence of target
            int last = lowerBound(arr, target + 1) - 1;

            // Check if the lower bound index is within the array bounds and
            // if the value is the target
            if (first < arr.length && arr[first] == target) {

                // Return the range [first, last]
                return new int[] { first, last };
            }

            // Return the default result array
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 2)));  // [1, 3]
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 3)));  // [4, 4]
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 5)));  // [-1, -1]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{}, 1)));                    // [-1, -1] — empty array
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{5}, 5)));                   // [0, 0] — single element match
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{5}, 3)));                   // [-1, -1] — single element miss
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{2, 2, 2, 2}, 2)));         // [0, 3] — all same
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 1)));   // [0, 0] — target at first
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
