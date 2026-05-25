---
title: "Preceding Superior Element"
summary: "Given two arrays arr1 and arr2 (where arr2 is a subset of arr1 and all elements are unique), return for each value in arr2 its preceding superior element in arr1 — the first strictly-greater element t"
prereqs:
  - 09-pattern-previous-closest-occurrence/01-pattern
difficulty: easy
---

# Preceding superior element

## Problem Statement

Given two arrays `arr1` and `arr2` (where `arr2` is a subset of `arr1` and all elements are unique), return for each value in `arr2` its **preceding superior element** in `arr1` — the first strictly-greater element to its left in `arr1`. Return `-1` for values with no preceding superior.

### Example 1
> -   **Input:** `arr1 = [3, 5, 1, 6, 8, 7]`, `arr2 = [3, 1, 8, 7]`
> -   **Output:** `[-1, 5, -1, 8]`

### Example 2
> -   **Input:** `arr1 = [5, 9, 7, 8, 1]`, `arr2 = [5, 9, 7]`
> -   **Output:** `[-1, -1, 9]`

<details>
<summary><h2>Approach</h2></summary>


Two passes:

1. Compute the previous-greater-element array `pge` for `arr1` using the monotonic stack (O(N)).
2. Build a `value → index` map for `arr1`. Then for each query in `arr2`, look up its index and read `pge[index]`.

Total: O(N + M) time, O(N) space.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def preceding_superior_element(
        self, arr_1: List[int], arr_2: List[int]
    ) -> List[int]:

        # Array to store the previous greater elements for arr_1
        previous_greater = [-1] * len(arr_1)

        # Map to store the last index of each element in arr_1
        index_map = {}

        # Stack to help find the previous greater element efficiently
        stack = []

        # Step 1: Build the previous greater elements array for arr_1
        for i, num in enumerate(arr_1):

            # Remove elements from the stack that are smaller than or
            # equal to the current element
            while stack and stack[-1] <= num:
                stack.pop()

            # If the stack is not empty, set the previous greater element
            if stack:
                previous_greater[i] = stack[-1]

            # Push the current element onto the stack for future elements
            stack.append(num)

            # Store the index of the current element in the index map
            index_map[num] = i

        # Step 2: Process arr_2 to generate the result
        result = []
        for num in arr_2:

            # Push the previous greater element if found, otherwise -1
            result.append(
                previous_greater[index_map[num]]
                if num in index_map
                else -1
            )

        return result


# Examples from the problem statement
print(Solution().preceding_superior_element([3,5,1,6,8,7], [3,1,8,7]))   # [-1, 5, -1, 8]
print(Solution().preceding_superior_element([5,9,7,8,1], [5,9,7]))       # [-1, -1, 9]

# Edge cases
print(Solution().preceding_superior_element([1,2,3], [1,2,3]))           # [-1, -1, -1] — sorted ascending
print(Solution().preceding_superior_element([3,2,1], [3,2,1]))           # [-1, 3, 2] — sorted descending
print(Solution().preceding_superior_element([5], [5]))                   # [-1] — single element
print(Solution().preceding_superior_element([1,3,2], [3,2]))             # [-1, 3]
print(Solution().preceding_superior_element([4,1,2], [1,2]))             # [4, 4]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[] precedingSuperiorElement(int[] arr1, int[] arr2) {

            // Array to store the previous greater elements for arr1
            int[] previousGreater = new int[arr1.length];
            Arrays.fill(previousGreater, -1);

            // Map to store the last index of each element in arr1
            Map<Integer, Integer> indexMap = new HashMap<>();

            // Stack to help find the previous greater element efficiently
            Stack<Integer> stack = new Stack<>();

            // Step 1: Build the previous greater elements array for arr1
            for (int i = 0; i < arr1.length; i++) {
                int num = arr1[i];

                // Remove elements from the stack that are smaller than or
                // equal to the current element
                while (!stack.isEmpty() && stack.peek() <= num) {
                    stack.pop();
                }

                // If the stack is not empty, set the previous greater
                // element
                if (!stack.isEmpty()) {
                    previousGreater[i] = stack.peek();
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

                // Push the previous greater element if found, otherwise -1
                result[i] = indexMap.containsKey(num)
                    ? previousGreater[indexMap.get(num)]
                    : -1;
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{3,5,1,6,8,7}, new int[]{3,1,8,7})
        ));  // [-1, 5, -1, 8]
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{5,9,7,8,1}, new int[]{5,9,7})
        ));  // [-1, -1, 9]

        // Edge cases
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{1,2,3}, new int[]{1,2,3})
        ));  // [-1, -1, -1]
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{3,2,1}, new int[]{3,2,1})
        ));  // [-1, 3, 2]
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{5}, new int[]{5})
        ));  // [-1]
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{1,3,2}, new int[]{3,2})
        ));  // [-1, 3]
        System.out.println(Arrays.toString(
            new Solution().precedingSuperiorElement(new int[]{4,1,2}, new int[]{1,2})
        ));  // [4, 4]
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
