---
title: "Reverse an Array"
summary: "Given an integer array arr, reverse its elements in place using a stack. Don't return a new array — mutate the input."
prereqs:
  - 08-pattern-reversal/01-pattern
difficulty: easy
---

# Reverse an array

## Problem Statement

Given an integer array `arr`, reverse its elements **in place** using a stack. Don't return a new array — mutate the input.

### Example 1
> -   **Input:** `arr = [1, 2, 3, 4, 5, 6]` → after the call `arr = [6, 5, 4, 3, 2, 1]`

### Example 2
> -   **Input:** `arr = []` → still `[]`

<details>
<summary><h2>Solution</h2></summary>


Same recipe; the destination is the input array itself. Pass 1 pushes; pass 2 overwrites positions 0..n−1 with stack pops.


```python run
from typing import List

class Solution:
    def reverse_an_array(self, arr: List[int]) -> None:

        # Create a stack to store elements of arr
        stack: List[int] = []

        # Pushing elements of arr into the stack
        for num in arr:
            stack.append(num)

        counter: int = 0

        # Popping elements from the stack and storing them back into arr
        # in reverse order
        while stack:
            arr[counter] = stack.pop()
            counter += 1


# Examples from the problem statement
a1 = [1, 2, 3, 4, 5, 6]
Solution().reverse_an_array(a1); print(a1)   # [6, 5, 4, 3, 2, 1]

a2: List[int] = []
Solution().reverse_an_array(a2); print(a2)   # []

# Edge cases
a3 = [7]
Solution().reverse_an_array(a3); print(a3)   # [7] — single element

a4 = [1, 2]
Solution().reverse_an_array(a4); print(a4)   # [2, 1] — two elements

a5 = [5, 5, 5]
Solution().reverse_an_array(a5); print(a5)   # [5, 5, 5] — all same

a6 = [-3, 0, 3]
Solution().reverse_an_array(a6); print(a6)   # [3, 0, -3] — negatives

a7 = [1, 2, 3]
Solution().reverse_an_array(a7); print(a7)   # [3, 2, 1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public void reverseAnArray(int[] arr) {

            // Create a stack to store elements of arr
            Stack<Integer> stack = new Stack<>();

            // Pushing elements of arr into the stack
            for (int i = 0; i < arr.length; i++) {
                stack.push(arr[i]);
            }

            int counter = 0;

            // Popping elements from the stack and storing them back into arr
            // in reverse order
            while (!stack.empty()) {
                arr[counter++] = stack.pop();
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        int[] a1 = {1, 2, 3, 4, 5, 6};
        new Solution().reverseAnArray(a1);
        System.out.println(Arrays.toString(a1));   // [6, 5, 4, 3, 2, 1]

        int[] a2 = {};
        new Solution().reverseAnArray(a2);
        System.out.println(Arrays.toString(a2));   // []

        // Edge cases
        int[] a3 = {7};
        new Solution().reverseAnArray(a3);
        System.out.println(Arrays.toString(a3));   // [7]

        int[] a4 = {1, 2};
        new Solution().reverseAnArray(a4);
        System.out.println(Arrays.toString(a4));   // [2, 1]

        int[] a5 = {5, 5, 5};
        new Solution().reverseAnArray(a5);
        System.out.println(Arrays.toString(a5));   // [5, 5, 5]

        int[] a6 = {-3, 0, 3};
        new Solution().reverseAnArray(a6);
        System.out.println(Arrays.toString(a6));   // [3, 0, -3]

        int[] a7 = {1, 2, 3};
        new Solution().reverseAnArray(a7);
        System.out.println(Arrays.toString(a7));   // [3, 2, 1]
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
