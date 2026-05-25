---
title: "Parentheses Checker"
summary: "Given a string s containing only (, ), [, ], {, }, return true iff every bracket is matched and closed in the right order."
prereqs:
  - 11-pattern-sequence-validation/01-pattern
difficulty: easy
---

# Parentheses checker

## Problem Statement

Given a string `s` containing only `(`, `)`, `[`, `]`, `{`, `}`, return `true` iff every bracket is matched and closed in the right order.

### Example 1
> -   **Input:** `s = "()"` → **Output:** `true`

### Example 2
> -   **Input:** `s = "(({}))[]"` → **Output:** `true`

### Example 3
> -   **Input:** `s = "({{)[]"` → **Output:** `false`

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def is_matching_pair(self, opening: str, closing: str) -> bool:
        return (
            (opening == "(" and closing == ")")
            or (opening == "{" and closing == "}")
            or (opening == "[" and closing == "]")
        )

    def parentheses_checker(self, s: str) -> bool:

        # Create a stack to store the opening parentheses
        stack: List[str] = []

        # Iterate through each character in the string
        for ch in s:

            # If the character is an opening parenthesis, push it onto
            # the stack
            if ch == "(" or ch == "{" or ch == "[":

                # Push opening parentheses onto the stack
                stack.append(ch)

            # If the character is a closing parenthesis
            else:

                # If the stack is empty, the closing parenthesis does
                # not match the corresponding opening parenthesis
                # Return false as the string is invalid
                if not stack or not self.is_matching_pair(stack[-1], ch):
                    return False

                # Remove the corresponding opening parenthesis from the
                # stack
                stack.pop()

        # If the stack is empty at the end, the string is valid
        return not stack


# Examples from the problem statement
print(Solution().parentheses_checker("()"))          # True
print(Solution().parentheses_checker("(({}))[]{"))  # False — extra open
print(Solution().parentheses_checker("({{)[]{"))    # False

# Edge cases
print(Solution().parentheses_checker(""))            # True — empty string is valid
print(Solution().parentheses_checker("("))           # False — unmatched open
print(Solution().parentheses_checker(")"))           # False — unmatched close
print(Solution().parentheses_checker("([{}])"))      # True
print(Solution().parentheses_checker("([)]"))        # False — wrong order
print(Solution().parentheses_checker("{[()]}"))      # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private boolean isMatchingPair(char opening, char closing) {
            return (
                (opening == '(' && closing == ')') ||
                (opening == '{' && closing == '}') ||
                (opening == '[' && closing == ']')
            );
        }

        public boolean parenthesesChecker(String s) {

            // Create a stack to store the opening parentheses
            Stack<Character> stack = new Stack<>();

            // Iterate through each character in the string
            for (char ch : s.toCharArray()) {

                // If the character is an opening parenthesis, push it onto
                // the stack
                if (ch == '(' || ch == '{' || ch == '[') {

                    // Push opening parentheses onto the stack
                    stack.push(ch);
                }

                // If the character is a closing parenthesis
                else {

                    // If the stack is empty, the closing parenthesis does
                    // not match the corresponding opening parenthesis
                    // Return false as the string is invalid
                    if (
                        stack.isEmpty() || !isMatchingPair(stack.peek(), ch)
                    ) {
                        return false;
                    }

                    // Remove the corresponding opening parenthesis from the
                    // stack
                    stack.pop();
                }
            }

            // If the stack is empty at the end, the string is valid
            return stack.isEmpty();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().parenthesesChecker("()"));          // true
        System.out.println(new Solution().parenthesesChecker("(({}))[]{"));  // false
        System.out.println(new Solution().parenthesesChecker("({{)[]{"));    // false

        // Edge cases
        System.out.println(new Solution().parenthesesChecker(""));            // true
        System.out.println(new Solution().parenthesesChecker("("));           // false
        System.out.println(new Solution().parenthesesChecker(")"));           // false
        System.out.println(new Solution().parenthesesChecker("([{}])"));      // true
        System.out.println(new Solution().parenthesesChecker("([)]"));        // false
        System.out.println(new Solution().parenthesesChecker("{[()]}"));      // true
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
