---
title: "Succeeding Superior Element II"
summary: "Circular variant — arr is treated as a ring; for each element find the next strictly-greater element, allowing one wrap-around to the start of the array."
prereqs:
  - 10-pattern-next-closest-occurrence/01-pattern
difficulty: medium
---

# Succeeding superior element II

## Problem Statement

Circular variant — `arr` is treated as a ring; for each element find the next strictly-greater element, allowing one wrap-around to the start of the array.

### Example 1
> -   **Input:** `arr = [2, 5, 1, 6, 10, 3]` → **Output:** `[5, 6, 6, 10, -1, 5]`

### Example 2
> -   **Input:** `arr = [6, 7, 8, 9, 8]` → **Output:** `[7, 8, 9, -1, 9]`

<details>
<summary><h2>Approach</h2></summary>


Same doubled-array trick from the previous lesson — iterate `2n` indices using `i % n`. Each element gets two passes; the second one resolves answers that depend on wrap-around.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def succeeding_superior_element_ii(
        self, arr: List[int]
    ) -> List[int]:
        n = len(arr)
        result = [-1] * n

        # Stack to store elements
        stack = []

        # Iterate twice through the array in reverse order (circularly)
        for i in range(2 * n - 1, -1, -1):

            # Circular index
            index = i % n
            num = arr[index]

            # Check if we can pop elements from the stack
            # (i.e., find the succeeding greater element for those
            # elements)
            while stack and stack[-1] <= num:
                stack.pop()

            # If stack is not empty, the top element is the succeeding
            # superior element
            if stack:
                result[index] = stack[-1]

            # Always push the element to the stack
            stack.append(num)

        return result


# Examples from the problem statement
print(Solution().succeeding_superior_element_ii([2, 5, 1, 6, 10, 3]))  # [5, 6, 6, 10, -1, 5]
print(Solution().succeeding_superior_element_ii([6, 7, 8, 9, 8]))      # [7, 8, 9, -1, 9]

# Edge cases
print(Solution().succeeding_superior_element_ii([]))                    # []
print(Solution().succeeding_superior_element_ii([5]))                   # [-1]
print(Solution().succeeding_superior_element_ii([1, 2]))                # [2, -1]
print(Solution().succeeding_superior_element_ii([1, 2, 3]))             # [2, 3, -1]
print(Solution().succeeding_superior_element_ii([3, 2, 1]))             # [-1, 3, 3]
print(Solution().succeeding_superior_element_ii([5, 5, 5]))             # [-1, -1, -1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[] succeedingSuperiorElementII(int[] arr) {
            int n = arr.length;
            int[] result = new int[n];

            // Initialize result with -1
            for (int i = 0; i < n; i++) {
                result[i] = -1;
            }

            // Stack to store elements
            Stack<Integer> stack = new Stack<>();

            // Iterate twice through the array in reverse order (circularly)
            for (int i = 2 * n - 1; i >= 0; i--) {

                // Circular index
                int index = i % n;
                int num = arr[index];

                // Check if we can pop elements from the stack
                // (i.e., find the succeeding greater element for those
                // elements)
                while (!stack.isEmpty() && stack.peek() <= num) {
                    stack.pop();
                }

                // If stack is not empty, the top element is the succeeding
                // superior element
                if (!stack.isEmpty()) {
                    result[index] = stack.peek();
                }

                // Always push the element to the stack
                stack.push(num);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{2, 5, 1, 6, 10, 3})));  // [5, 6, 6, 10, -1, 5]
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{6, 7, 8, 9, 8})));      // [7, 8, 9, -1, 9]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{})));                   // []
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{5})));                  // [-1]
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{1, 2})));               // [2, -1]
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{1, 2, 3})));            // [2, 3, -1]
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{3, 2, 1})));            // [-1, 3, 3]
        System.out.println(Arrays.toString(new Solution().succeedingSuperiorElementII(new int[]{5, 5, 5})));            // [-1, -1, -1]
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
