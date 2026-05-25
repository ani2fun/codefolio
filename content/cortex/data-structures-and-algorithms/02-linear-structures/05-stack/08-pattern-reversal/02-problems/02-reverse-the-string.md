---
title: "Reverse the String"
summary: "Given a string s, return its reverse using a stack."
prereqs:
  - 08-pattern-reversal/01-pattern
difficulty: easy
---

# Reverse the string

## Problem Statement

Given a string `s`, return its reverse using a stack.

### Example 1
> -   **Input:** `s = "abcdefgh"` → **Output:** `"hgfedcba"`

### Example 2
> -   **Input:** `s = "c"` → **Output:** `"c"`

<details>
<summary><h2>Solution</h2></summary>


The textbook two-pass: push every character, then pop until empty into a result string.


```python run
from typing import List

class Solution:
    def reverse_the_string(self, s: str) -> str:

        # Create a stack to store characters
        stack: List[str] = []

        # Create an empty string to store the reversed string
        result: str = ""

        # Push each character into the stack
        for ch in s:
            stack.append(ch)

        # Pop characters from the stack to form the reversed string
        while stack:

            # Append the top character to the result string
            result += stack.pop()

        # Return the reversed string
        return result


# Examples from the problem statement
print(Solution().reverse_the_string("abcdefgh"))   # hgfedcba
print(Solution().reverse_the_string("c"))          # c

# Edge cases
print(Solution().reverse_the_string(""))           # "" — empty string
print(Solution().reverse_the_string("ab"))         # ba — two characters
print(Solution().reverse_the_string("aba"))        # aba — palindrome unchanged
print(Solution().reverse_the_string("12345"))      # 54321 — digits
print(Solution().reverse_the_string("aAbB"))       # BbAa — mixed case
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public String reverseTheString(String s) {

            // Create a stack to store characters
            Stack<Character> stack = new Stack<>();

            // Create an empty string to store the reversed string
            StringBuilder result = new StringBuilder();

            // Push each character into the stack
            for (char ch : s.toCharArray()) {
                stack.push(ch);
            }

            // Pop characters from the stack to form the reversed string
            while (!stack.empty()) {

                // Append the top character to the result string
                result.append(stack.pop());
            }

            // Return the reversed string
            return result.toString();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseTheString("abcdefgh"));   // hgfedcba
        System.out.println(new Solution().reverseTheString("c"));          // c

        // Edge cases
        System.out.println(new Solution().reverseTheString(""));           // "" — empty
        System.out.println(new Solution().reverseTheString("ab"));         // ba
        System.out.println(new Solution().reverseTheString("aba"));        // aba — palindrome
        System.out.println(new Solution().reverseTheString("12345"));      // 54321
        System.out.println(new Solution().reverseTheString("aAbB"));       // BbAa
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
