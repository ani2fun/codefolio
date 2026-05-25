---
title: "Unique Character Span"
summary: "Given a string s, return the length of the longest substring with distinct characters."
prereqs:
  - 10-pattern-variable-sized-sliding-window/01-pattern
difficulty: easy
---

# Unique character span

## Problem Statement

Given a string `s`, return the length of the longest substring with **distinct** characters.

### Example 1
> -   **Input:** `s = "abcbed"` → **Output:** `4` (`"cbed"`)

### Example 2
> -   **Input:** `s = "aaaaabc"` → **Output:** `3` (`"abc"`)

### Example 3
> -   **Input:** `s = "abcdefgh"` → **Output:** `8` (the whole string)

<details>
<summary><h2>Solution</h2></summary>


Already implemented above as the canonical example. The core invariant: when the loop body finishes, the window contains only distinct characters.


```python run
class Solution:
    def unique_character_span(self, s: str) -> int:

        # Dictionary to store character frequencies
        frequency = {}

        # To store the maximum length of the substring
        max_length = 0

        # Sliding window pointers
        start, end = 0, 0

        while end < len(s):

            # Add the end character to the map
            end_char = s[end]
            frequency[end_char] = frequency.get(end_char, 0) + 1

            # If a character appears more than once, shrink the window
            while frequency.get(end_char, 0) > 1:
                start_char = s[start]
                frequency[start_char] -= 1

                # Remove character if count is 0
                if frequency[start_char] == 0:
                    del frequency[start_char]

                # Move the start pointer to shrink the window
                start += 1

            # Update the maximum length of the valid substring
            max_length = max(max_length, end - start + 1)

            # Expand the window
            end += 1

        return max_length


# Examples from the problem statement
print(Solution().unique_character_span("abcbed"))    # 4
print(Solution().unique_character_span("aaaaabc"))   # 3
print(Solution().unique_character_span("abcdefgh"))  # 8

# Edge cases
print(Solution().unique_character_span(""))          # 0
print(Solution().unique_character_span("a"))         # 1
print(Solution().unique_character_span("aa"))        # 1
print(Solution().unique_character_span("ab"))        # 2
print(Solution().unique_character_span("aab"))       # 2
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int uniqueCharacterSpan(String s) {

            // Map to store character frequencies
            Map<Character, Integer> frequency = new HashMap<>();

            // To store the maximum length of the substring
            int maxLength = 0;

            // Sliding window pointers
            int start = 0;
            int end = 0;

            while (end < s.length()) {

                // Add the end character to the map
                char endChar = s.charAt(end);
                frequency.put(
                    endChar,
                    frequency.getOrDefault(endChar, 0) + 1
                );

                // If a character appears more than once, shrink the window
                while (frequency.get(endChar) > 1) {
                    char startChar = s.charAt(start);
                    frequency.put(startChar, frequency.get(startChar) - 1);

                    // Remove character if count is 0
                    if (frequency.get(startChar) == 0) {
                        frequency.remove(startChar);
                    }

                    // Move the start pointer to shrink the window
                    start++;
                }

                // Update the maximum length of the valid substring
                maxLength = Math.max(maxLength, end - start + 1);

                // Expand the window
                end++;
            }

            return maxLength;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().uniqueCharacterSpan("abcbed"));    // 4
        System.out.println(new Solution().uniqueCharacterSpan("aaaaabc"));   // 3
        System.out.println(new Solution().uniqueCharacterSpan("abcdefgh"));  // 8

        // Edge cases
        System.out.println(new Solution().uniqueCharacterSpan(""));          // 0
        System.out.println(new Solution().uniqueCharacterSpan("a"));         // 1
        System.out.println(new Solution().uniqueCharacterSpan("aa"));        // 1
        System.out.println(new Solution().uniqueCharacterSpan("ab"));        // 2
        System.out.println(new Solution().uniqueCharacterSpan("aab"));       // 2
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
