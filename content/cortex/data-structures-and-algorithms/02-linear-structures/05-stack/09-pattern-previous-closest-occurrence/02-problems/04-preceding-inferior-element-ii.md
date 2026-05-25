---
title: "Preceding Inferior Element II"
summary: "Circular variant of preceding inferior. Same approach with the comparison flipped."
prereqs:
  - 09-pattern-previous-closest-occurrence/01-pattern
difficulty: medium
---

# Preceding inferior element II

## Problem Statement

Circular variant of preceding inferior. Same approach with the comparison flipped.

### Example 1
> -   **Input:** `arr = [2, 5, 1, 6, 10, 3]`
> -   **Output:** `[1, 2, -1, 1, 6, 1]`

### Example 2
> -   **Input:** `arr = [6, 7, 8, 9, 8]`
> -   **Output:** `[-1, 6, 7, 8, 7]`

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def preceding_inferior_element_ii(self, arr: List[int]) -> List[int]:
        n = len(arr)
        result = [-1] * n

        # Stack to store elements
        stack = []

        # Iterate twice through the array (circularly)
        for i in range(2 * n):

            # Circular index
            index = i % n
            num = arr[index]

            # Check if we can pop elements from the stack
            # (i.e., find the preceding smaller element)
            while stack and stack[-1] >= num:
                stack.pop()

            # If stack is not empty, the top element is the preceding
            # inferior element
            if stack:
                result[index] = stack[-1]

            # Always push the element to the stack
            stack.append(num)

        return result


# Examples from the problem statement
print(Solution().preceding_inferior_element_ii([2, 5, 1, 6, 10, 3]))  # [1, 2, -1, 1, 6, 1]
print(Solution().preceding_inferior_element_ii([6, 7, 8, 9, 8]))      # [-1, 6, 7, 8, 7]

# Edge cases
print(Solution().preceding_inferior_element_ii([]))                    # []
print(Solution().preceding_inferior_element_ii([5]))                   # [-1]
print(Solution().preceding_inferior_element_ii([3, 1]))                # [1, -1]
print(Solution().preceding_inferior_element_ii([1, 2, 3]))             # [-1, 1, 2]
print(Solution().preceding_inferior_element_ii([3, 2, 1]))             # [1, 1, -1]
print(Solution().preceding_inferior_element_ii([5, 5, 5]))             # [-1, -1, -1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[] precedingInferiorElementII(int[] arr) {
            int n = arr.length;
            int[] result = new int[n];

            // Initialize result with -1
            for (int i = 0; i < n; i++) {
                result[i] = -1;
            }

            // Stack to store elements
            Stack<Integer> stack = new Stack<>();

            // Iterate twice through the array (circularly)
            for (int i = 0; i < 2 * n; i++) {

                // Circular index
                int index = i % n;
                int num = arr[index];

                // Check if we can pop elements from the stack
                // (i.e., find the preceding smaller element)
                while (!stack.isEmpty() && stack.peek() >= num) {
                    stack.pop();
                }

                // If stack is not empty, the top element is the preceding
                // inferior element
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
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{2, 5, 1, 6, 10, 3})));  // [1, 2, -1, 1, 6, 1]
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{6, 7, 8, 9, 8})));      // [-1, 6, 7, 8, 7]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{})));                   // []
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{5})));                  // [-1]
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{3, 1})));               // [1, -1]
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{1, 2, 3})));            // [-1, 1, 2]
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{3, 2, 1})));            // [1, 1, -1]
        System.out.println(Arrays.toString(new Solution().precedingInferiorElementII(new int[]{5, 5, 5})));            // [-1, -1, -1]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Three lessons:

1. **A monotonic stack stores un-disqualified candidates.** The moment a new element arrives that "dominates" something on the stack (greater or smaller, depending on the variant), the dominated value is no longer a viable answer for any future query. Pop it. The stack stays clean.
2. **Amortised O(N) is the magic.** A nested `while` looks like O(N²) but each element enters and leaves the stack at most once, capping total stack ops at 2N.
3. **Circular arrays double the iteration, not the memory.** Iterate `2*n` times with `i % n` indexing; the second pass catches answers that need to wrap around the start.

> *Coming up — same machinery, opposite direction. **Lesson 9** does **next-closest** — for each element, find the closest <em>later</em> element satisfying the condition. Two ways to set this up: scan right-to-left with the same stack rules as previous-closest, or scan left-to-right and resolve answers retroactively when an element pops. The latter is more elegant; the former is more straightforward. Both come up in interviews.*

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
