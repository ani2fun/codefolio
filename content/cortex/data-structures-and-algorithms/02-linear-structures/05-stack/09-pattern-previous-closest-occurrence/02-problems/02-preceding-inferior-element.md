---
title: "Preceding Inferior Element"
summary: "Same as above but inferior = strictly smaller. Maintain an *increasing* monotonic stack; pop while top ≥ current."
prereqs:
  - 09-pattern-previous-closest-occurrence/01-pattern
difficulty: easy
---

# Preceding inferior element

## Problem Statement

Same as above but **inferior** = strictly smaller. Maintain an *increasing* monotonic stack; pop while top `≥` current.

### Example 1
> -   **Input:** `arr1 = [3, 5, 1, 6, 8, 2]`, `arr2 = [3, 1, 8, 2]`
> -   **Output:** `[-1, -1, 6, 1]`

### Example 2
> -   **Input:** `arr1 = [5, 9, 7, 8, 1]`, `arr2 = [5, 9, 7]`
> -   **Output:** `[-1, 5, 5]`

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def preceding_inferior_element(
        self, arr_1: List[int], arr_2: List[int]
    ) -> List[int]:

        # Array to store the previous smaller elements for arr_1
        previous_smaller = [-1] * len(arr_1)

        # Map to store the last index of each element in arr_1
        index_map = {}

        # Stack to help find the previous smaller element efficiently
        stack = []

        # Step 1: Build the previous smaller elements array for arr_1
        for i, num in enumerate(arr_1):

            # Remove elements from the stack that are greater than or
            # equal to the current element
            while stack and stack[-1] >= num:
                stack.pop()

            # If the stack is not empty, set the previous smaller element
            if stack:
                previous_smaller[i] = stack[-1]

            # Push the current element onto the stack for future elements
            stack.append(num)

            # Store the index of the current element in the index map
            index_map[num] = i

        # Step 2: Process arr_2 to generate the result
        result = []
        for num in arr_2:

            # Push the previous smaller element if found, otherwise -1
            result.append(
                previous_smaller[index_map[num]]
                if num in index_map
                else -1
            )

        return result


# Examples from the problem statement
print(Solution().preceding_inferior_element([3,5,1,6,8,2], [3,1,8,2]))   # [-1, -1, 6, 1]
print(Solution().preceding_inferior_element([5,9,7,8,1], [5,9,7]))       # [-1, 5, 5]

# Edge cases
print(Solution().preceding_inferior_element([1,2,3], [1,2,3]))           # [-1, 1, 2] — ascending
print(Solution().preceding_inferior_element([3,2,1], [3,2,1]))           # [-1, -1, -1] — descending
print(Solution().preceding_inferior_element([5], [5]))                   # [-1] — single element
print(Solution().preceding_inferior_element([2,5,3], [5,3]))             # [2, 2]
print(Solution().preceding_inferior_element([4,1,3], [1,3]))             # [-1, 1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[] precedingInferiorElement(int[] arr1, int[] arr2) {

            // Array to store the previous smaller elements for arr1
            int[] previousSmaller = new int[arr1.length];
            Arrays.fill(previousSmaller, -1);

            // Map to store the last index of each element in arr1
            Map<Integer, Integer> indexMap = new HashMap<>();

            // Stack to help find the previous smaller element efficiently
            Stack<Integer> stack = new Stack<>();

            // Step 1: Build the previous smaller elements array for arr1
            for (int i = 0; i < arr1.length; i++) {
                int num = arr1[i];

                // Remove elements from the stack that are greater than or
                // equal to the current element
                while (!stack.isEmpty() && stack.peek() >= num) {
                    stack.pop();
                }

                // If the stack is not empty, set the previous smaller
                // element
                if (!stack.isEmpty()) {
                    previousSmaller[i] = stack.peek();
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

                // Push the previous smaller element if found, otherwise -1
                result[i] = indexMap.containsKey(num)
                    ? previousSmaller[indexMap.get(num)]
                    : -1;
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{3,5,1,6,8,2}, new int[]{3,1,8,2})
        ));  // [-1, -1, 6, 1]
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{5,9,7,8,1}, new int[]{5,9,7})
        ));  // [-1, 5, 5]

        // Edge cases
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{1,2,3}, new int[]{1,2,3})
        ));  // [-1, 1, 2]
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{3,2,1}, new int[]{3,2,1})
        ));  // [-1, -1, -1]
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{5}, new int[]{5})
        ));  // [-1]
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{2,5,3}, new int[]{5,3})
        ));  // [2, 2]
        System.out.println(Arrays.toString(
            new Solution().precedingInferiorElement(new int[]{4,1,3}, new int[]{1,3})
        ));  // [-1, 1]
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
