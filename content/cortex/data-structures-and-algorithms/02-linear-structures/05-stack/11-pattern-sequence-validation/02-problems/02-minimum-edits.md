---
title: "Minimum Edits"
summary: "Given a string s of ( and ) only, return the minimum number of insertions or deletions needed to make the sequence valid."
prereqs:
  - 11-pattern-sequence-validation/01-pattern
difficulty: medium
---

# Minimum edits

## Problem Statement

Given a string `s` of `(` and `)` only, return the minimum number of insertions or deletions needed to make the sequence valid.

### Example 1
> -   **Input:** `s = "())"` → **Output:** `1`

### Example 2
> -   **Input:** `s = "))"` → **Output:** `2`

### Example 3
> -   **Input:** `s = "(((())))"` → **Output:** `0`

<details>
<summary><h2>Approach</h2></summary>


Walk the string with a stack of `(`s. For each `)`:

- If the stack has a `(`, **pop** (matched).
- If the stack is empty, this `)` is unmatched — **count it** as one edit (insert a `(` before it, or delete this `)` — same cost).

At end of input, the stack holds every unmatched `(`. Each one needs an edit (insert a `)` after it or delete it). **Total edits = unmatched `(` left on stack + unmatched `)` counted on the fly.**

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def minimum_edits(self, s: str) -> int:

        # Stack to track unmatched '('
        stack: List[str] = []

        # Count of edits needed
        edits: int = 0

        for c in s:

            # If '(', push to stack to find a match later
            if c == "(":
                stack.append(c)

            # Else if ')', try to match with a '('
            else:

                # Found a ')', check for matching '('
                if stack and stack[-1] == "(":

                    # Found a match, pop the '(' from stack
                    stack.pop()

                # No matching '(', need an edit
                else:

                    # Need to insert a '(' before this ')' or delete
                    # this ')' which counts as one edit
                    edits += 1

        # Any unmatched '(' in stack need to be closed with ')' edits
        # plus the edits we made for unmatched ')'
        return len(stack) + edits


# Examples from the problem statement
print(Solution().minimum_edits("())"))       # 1
print(Solution().minimum_edits("))"))        # 2
print(Solution().minimum_edits("(((())))"))  # 0

# Edge cases
print(Solution().minimum_edits(""))          # 0 — empty string is already valid
print(Solution().minimum_edits("("))         # 1 — one unmatched open
print(Solution().minimum_edits(")"))         # 1 — one unmatched close
print(Solution().minimum_edits("()"))        # 0
print(Solution().minimum_edits("(("))        # 2
print(Solution().minimum_edits(")()("))      # 2
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int minimumEdits(String s) {

            // Stack to track unmatched '('
            Stack<Character> stack = new Stack<>();

            // Count of edits needed
            int edits = 0;

            for (char c : s.toCharArray()) {

                // If '(', push to stack to find a match later
                if (c == '(') {
                    stack.push(c);
                }

                // Else if ')', try to match with a '('
                else {

                    // Found a ')', check for matching '('
                    if (!stack.isEmpty() && stack.peek() == '(') {

                        // Found a match, pop the '(' from stack
                        stack.pop();
                    }

                    // No matching '(', need an edit
                    else {

                        // Need to insert a '(' before this ')' or delete
                        // this ')' which counts as one edit
                        edits++;
                    }
                }
            }

            // Any unmatched '(' in stack need to be closed with ')' edits
            // plus the edits we made for unmatched ')'
            return stack.size() + edits;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().minimumEdits("())"));       // 1
        System.out.println(new Solution().minimumEdits("))"));        // 2
        System.out.println(new Solution().minimumEdits("(((())))"));  // 0

        // Edge cases
        System.out.println(new Solution().minimumEdits(""));          // 0
        System.out.println(new Solution().minimumEdits("("));         // 1
        System.out.println(new Solution().minimumEdits(")"));         // 1
        System.out.println(new Solution().minimumEdits("()"));        // 0
        System.out.println(new Solution().minimumEdits("(("));        // 2
        System.out.println(new Solution().minimumEdits(")()("));      // 2
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
