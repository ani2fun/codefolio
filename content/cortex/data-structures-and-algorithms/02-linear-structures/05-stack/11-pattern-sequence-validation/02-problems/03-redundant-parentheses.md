---
title: "Redundant Parentheses"
summary: "Given a balanced expression s (containing operators, operands, and parentheses), return true if there exists a redundant pair of parentheses — a pair that wraps either nothing or a single operand, con"
prereqs:
  - 11-pattern-sequence-validation/01-pattern
difficulty: medium
---

# Redundant parentheses

## Problem Statement

Given a balanced expression `s` (containing operators, operands, and parentheses), return `true` if there exists a redundant pair of parentheses — a pair that wraps **either nothing or a single operand**, contributing no precedence value.

### Example 1
> -   **Input:** `s = "((2+3))+7"` → **Output:** `true` (the outer parens around `(2+3)` are redundant)

### Example 2
> -   **Input:** `s = "(2+3)"` → **Output:** `false` (single pair around an operation; not redundant)

### Example 3
> -   **Input:** `s = "((2+3)+7)"` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


Push every character except `)`. When you hit `)`, look at what was pushed *between* the most recent `(` and now. **If only operands and no operators are inside, the parens are redundant.** Equivalently: if the top of the stack is `(` *immediately* (i.e. zero operators between this `)` and its `(`), the pair is redundant.

But we should also detect `(((expr)))` — wrapping an already-parenthesised expression in *another* pair. To catch that, we should pop characters until we hit `(`. If the top *was* `(` immediately, redundant. Otherwise, check whether at least one operator was popped — if not, even though there were operands, no operator means redundant.

The simpler formulation that's used in the canonical solution: when `)` arrives, **if the top of the stack is `(`, redundant**. Otherwise pop until matching `(`, also pop the `(`. (This works because operators pushed between `(` and `)` keep the stack from being `(` directly.)

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def redundant_parentheses(self, s: str) -> bool:

        # Edge case for single pair of parentheses
        if s == "()":
            return False

        # Create a stack to store characters
        stack: List[str] = []

        # Iterate through each character in the string
        for ch in s:

            # If the character is a closing parenthesis
            if ch == ")":

                # If top of stack is an opening parenthesis, it's
                # redundant
                if stack and stack[-1] == "(":
                    return True

                # Pop elements until we find the corresponding '('
                while stack and stack[-1] != "(":
                    stack.pop()

                # Pop the '(' as well
                stack.pop()

            # If the character is not a closing parenthesis, push it
            # onto the stack
            else:
                stack.append(ch)

        # No redundant parentheses found
        return False


# Examples from the problem statement
print(Solution().redundant_parentheses("((2+3))+7"))   # True
print(Solution().redundant_parentheses("(2+3)"))       # False
print(Solution().redundant_parentheses("((2+3)+7)"))   # False

# Edge cases
print(Solution().redundant_parentheses("()"))          # False — edge case handled explicitly
print(Solution().redundant_parentheses("(())"))        # True — empty inner parens
print(Solution().redundant_parentheses("(a+b)"))       # False
print(Solution().redundant_parentheses("((a+b))"))     # True
print(Solution().redundant_parentheses("(a+(b+c))"))   # False
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public boolean redundantParentheses(String s) {

            // Edge case for single pair of parentheses
            if ("()".equals(s)) {
                return false;
            }

            // Create a stack to store characters
            Stack<Character> stack = new Stack<>();

            // Iterate through each character in the string
            for (char ch : s.toCharArray()) {

                // If the character is a closing parenthesis
                if (ch == ')') {

                    // If top of stack is an opening parenthesis, it's
                    // redundant
                    if (!stack.isEmpty() && stack.peek() == '(') {
                        return true;
                    }

                    // Pop elements until we find the corresponding '('
                    while (!stack.isEmpty() && stack.peek() != '(') {
                        stack.pop();
                    }

                    // Pop the '(' as well
                    stack.pop();
                }

                // If the character is not a closing parenthesis, push it
                // onto the stack
                else {
                    stack.push(ch);
                }
            }

            // No redundant parentheses found
            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().redundantParentheses("((2+3))+7"));   // true
        System.out.println(new Solution().redundantParentheses("(2+3)"));       // false
        System.out.println(new Solution().redundantParentheses("((2+3)+7)"));   // false

        // Edge cases
        System.out.println(new Solution().redundantParentheses("()"));          // false
        System.out.println(new Solution().redundantParentheses("(())"));        // true
        System.out.println(new Solution().redundantParentheses("(a+b)"));       // false
        System.out.println(new Solution().redundantParentheses("((a+b))"));     // true
        System.out.println(new Solution().redundantParentheses("(a+(b+c))"));   // false
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
