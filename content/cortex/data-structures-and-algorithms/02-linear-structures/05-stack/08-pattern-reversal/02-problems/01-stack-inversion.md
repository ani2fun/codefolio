---
title: "Stack Inversion"
summary: "Given a stack s, return a new stack containing the same elements in *reversed* order."
prereqs:
  - 08-pattern-reversal/01-pattern
difficulty: easy
---

# Stack inversion

## Problem Statement

Given a stack `s`, return a new stack containing the same elements in *reversed* order.

### Example
> -   **Input:** `s = [9, 5, 1, 2]` (top is `2`)
> -   **Output:** `[2, 1, 5, 9]` (top is `9`)

<details>
<summary><h2>Approach</h2></summary>


Two stacks. Pop everything from the input and push onto the output — *that single transfer reverses the order, because the topmost element of the input is pushed first onto the output, ending up at the bottom*.

> 🖼 Diagram — Stack inversion — pop the input top, push to output. The first popped item lands at the bottom of the output, which is exactly where it started in the input. The whole stack flips.
```d2
direction: right

inp: "input stack" {
  grid-rows: 4
  grid-gap: 0
  i1: "2 ← top"
  i2: "1"
  i3: "5"
  i4: "9 ← bot"
}

out: "output stack" {
  grid-rows: 4
  grid-gap: 0
  o1: "9 ← top"
  o2: "5"
  o3: "1"
  o4: "2 ← bot"
}

inp -> out: "pop, push"
```

<p align="center"><strong>Stack inversion — pop the input top, push to output. The first popped item lands at the bottom of the output, which is exactly where it started in the input. The whole stack flips.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def stack_inversion(self, s: List[int]) -> List[int]:
        reversed_stack: List[int] = []

        # Transfer elements from original stack to reversed stack
        while s:

            # Get the top element from the original stack
            top = s[-1]

            # Remove the top element from the original stack
            s.pop()

            # Push the element onto the reversed stack
            reversed_stack.append(top)

        # Return the reversed stack
        return reversed_stack


# Example from the problem statement
print(Solution().stack_inversion([9, 5, 1, 2]))     # [2, 1, 5, 9]

# Edge cases
print(Solution().stack_inversion([]))               # [] — empty stack
print(Solution().stack_inversion([7]))              # [7] — single element
print(Solution().stack_inversion([1, 2]))           # [2, 1] — two elements
print(Solution().stack_inversion([3, 3, 3]))        # [3, 3, 3] — all same
print(Solution().stack_inversion([1, 2, 3, 4, 5])) # [5, 4, 3, 2, 1]
print(Solution().stack_inversion([-1, 0, 1]))       # [1, 0, -1] — negatives
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public Stack<Integer> stackInversion(Stack<Integer> s) {
            Stack<Integer> reversedStack = new Stack<>();

            // Transfer elements from original stack to reversed stack
            while (!s.empty()) {

                // Get the top element from the original stack
                int top = s.peek();

                // Remove the top element from the original stack
                s.pop();

                // Push the element onto the reversed stack
                reversedStack.push(top);
            }

            // Return the reversed stack
            return reversedStack;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        Stack<Integer> s1 = new Stack<>();
        for (int v : new int[]{9, 5, 1, 2}) s1.push(v);
        System.out.println(new Solution().stackInversion(s1));     // [2, 1, 5, 9]

        // Edge cases
        Stack<Integer> s2 = new Stack<>();
        System.out.println(new Solution().stackInversion(s2));     // [] — empty

        Stack<Integer> s3 = new Stack<>();
        s3.push(7);
        System.out.println(new Solution().stackInversion(s3));     // [7]

        Stack<Integer> s4 = new Stack<>();
        s4.push(1); s4.push(2);
        System.out.println(new Solution().stackInversion(s4));     // [1, 2] — top was 2

        Stack<Integer> s5 = new Stack<>();
        for (int v : new int[]{1, 2, 3, 4, 5}) s5.push(v);
        System.out.println(new Solution().stackInversion(s5));     // [1, 2, 3, 4, 5]

        Stack<Integer> s6 = new Stack<>();
        for (int v : new int[]{-1, 0, 1}) s6.push(v);
        System.out.println(new Solution().stackInversion(s6));     // [-1, 0, 1]
    }
}
```


> **Complexity** — Time: **O(N)** | Space: **O(N)**.

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
