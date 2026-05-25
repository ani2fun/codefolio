---
title: "Succeeding Inferior Element"
summary: "Same as above but strictly smaller. Maintain an *increasing* monotonic stack; resolve when current value is *smaller* than the stack's top."
prereqs:
  - 10-pattern-next-closest-occurrence/01-pattern
difficulty: easy
---

# Succeeding inferior element

## Problem Statement

Same as above but **strictly smaller**. Maintain an *increasing* monotonic stack; resolve when current value is *smaller* than the stack's top.

### Example 1
> -   **Input:** `arr1 = [3, 5, 1, 6, 8, 2]`, `arr2 = [3, 1, 8, 2]`
> -   **Output:** `[1, -1, 2, -1]`

### Example 2
> -   **Input:** `arr1 = [5, 9, 7, 8, 1]`, `arr2 = [5, 9, 7]`
> -   **Output:** `[1, 7, 1]`

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def succeeding_inferior_element(
        self, arr_1: List[int], arr_2: List[int]
    ) -> List[int]:

        # Array to store the next smaller elements for arr_1
        next_smaller = [-1] * len(arr_1)

        # Map to store the last index of each element in arr_1
        index_map = {}

        # Stack to help find the next smaller element efficiently
        stack = []

        # Step 1: Build the next smaller elements array for arr_1
        # (Traverse in reverse order)
        for i in range(len(arr_1) - 1, -1, -1):
            num = arr_1[i]

            # Remove elements from the stack that are greater than or
            # equal to the current element
            while stack and stack[-1] >= num:
                stack.pop()

            # If the stack is not empty, set the next smaller element
            if stack:
                next_smaller[i] = stack[-1]

            # Push the current element onto the stack for future elements
            stack.append(num)

            # Store the index of the current element in the index map
            index_map[num] = i

        # Step 2: Process arr_2 to generate the result
        result = []
        for num in arr_2:

            # Push the next smaller element if found, otherwise -1
            result.append(
                next_smaller[index_map[num]] if num in index_map else -1
            )

        return result


# Examples from the problem statement
print(Solution().succeeding_inferior_element([3, 5, 1, 6, 8, 9], [3, 1, 8, 9]))  # [1, -1, -1, -1]
print(Solution().succeeding_inferior_element([5, 9, 7, 8, 1], [5, 9, 7]))        # [1, 7, 1]

# Edge cases
print(Solution().succeeding_inferior_element([1], [1]))                           # [-1]
print(Solution().succeeding_inferior_element([1, 2], [1, 2]))                     # [-1, -1]
print(Solution().succeeding_inferior_element([2, 1], [2, 1]))                     # [1, -1]
print(Solution().succeeding_inferior_element([4, 3, 2, 1], [4, 2]))              # [3, 1]
print(Solution().succeeding_inferior_element([1, 2, 3, 4], [4, 1]))              # [-1, -1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[] succeedingInferiorElement(int[] arr1, int[] arr2) {

            // Array to store the next smaller elements for arr1
            int[] nextSmaller = new int[arr1.length];
            Arrays.fill(nextSmaller, -1);

            // Map to store the last index of each element in arr1
            Map<Integer, Integer> indexMap = new HashMap<>();

            // Stack to help find the next smaller element efficiently
            Stack<Integer> stack = new Stack<>();

            // Step 1: Build the next smaller elements array for arr1
            // (Traverse in reverse order)
            for (int i = arr1.length - 1; i >= 0; i--) {
                int num = arr1[i];

                // Remove elements from the stack that are greater than or
                // equal to the current element
                while (!stack.isEmpty() && stack.peek() >= num) {
                    stack.pop();
                }

                // If the stack is not empty, set the next smaller element
                if (!stack.isEmpty()) {
                    nextSmaller[i] = stack.peek();
                }

                // Push the current element onto the stack for future
                // elements
                stack.push(num);

                // Store the index of the current element in the index map
                indexMap.put(num, i);
            }

            // Step 2: Process arr2 to generate the result
            int[] result = new int[arr2.length];
            for (int i = 0; i < arr2.length; i++) {
                int num = arr2[i];

                // Push the next smaller element if found, otherwise -1
                result[i] = indexMap.containsKey(num)
                    ? nextSmaller[indexMap.get(num)]
                    : -1;
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{3, 5, 1, 6, 8, 9}, new int[]{3, 1, 8, 9})));  // [1, -1, -1, -1]
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{5, 9, 7, 8, 1}, new int[]{5, 9, 7})));        // [1, 7, 1]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{1}, new int[]{1})));                          // [-1]
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{1, 2}, new int[]{1, 2})));                    // [-1, -1]
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{2, 1}, new int[]{2, 1})));                    // [1, -1]
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{4, 3, 2, 1}, new int[]{4, 2})));             // [3, 1]
        System.out.println(Arrays.toString(new Solution().succeedingInferiorElement(new int[]{1, 2, 3, 4}, new int[]{4, 1})));             // [-1, -1]
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
