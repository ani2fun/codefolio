---
title: "K Characters Span"
summary: "Given a string s and integer k, return the length of the longest substring with at most K distinct characters."
prereqs:
  - 10-pattern-variable-sized-sliding-window/01-pattern
difficulty: medium
---

# K characters span

## Problem Statement

Given a string `s` and integer `k`, return the length of the longest substring with **at most K distinct** characters.

### Example 1
> -   **Input:** `s = "abcbed", k = 2` → **Output:** `3` (`"bcb"`)

### Example 2
> -   **Input:** `s = "aaaaabc", k = 3` → **Output:** `7` (whole string)

### Example 3
> -   **Input:** `s = "abcdefgh", k = 3` → **Output:** `3` (`"abc"`, `"bcd"`, etc.)

<details>
<summary><h2>Approach</h2></summary>


Same skeleton; the **rule** is now "at most K distinct characters in the window", which is exactly `len(freq_map) ≤ k`. Expand `end` greedily; when the map has more than K keys, contract from the left until it doesn't.

> *Observation* — `len(freq_map)` is the distinct-count *only if* you delete keys whose frequency drops to zero. The boundary work is the same as in the fixed-window pattern; only the rule changed.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
class Solution:
    def k_characters_span(self, s: str, k: int) -> int:

        # Dictionary to store character frequencies
        frequency = {}

        # To store the maximum length of the substring
        max_length = 0

        # Sliding window pointers
        start, end = 0, 0

        while end < len(s):

            # Add the end character to the dictionary
            end_char = s[end]
            frequency[end_char] = frequency.get(end_char, 0) + 1

            # If the number of distinct characters exceeds k, shrink the
            # window
            while len(frequency) > k:
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
print(Solution().k_characters_span("abcbed", 2))    # 3
print(Solution().k_characters_span("aaaaabc", 3))   # 7
print(Solution().k_characters_span("abcdefgh", 3))  # 3

# Edge cases
print(Solution().k_characters_span("", 2))          # 0
print(Solution().k_characters_span("a", 1))         # 1
print(Solution().k_characters_span("aaa", 1))       # 3
print(Solution().k_characters_span("abc", 0))       # 0
print(Solution().k_characters_span("aab", 2))       # 3
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int kCharactersSpan(String s, int k) {

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

                // If the number of distinct characters exceeds k, shrink the
                // window
                while (frequency.size() > k) {
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
        System.out.println(new Solution().kCharactersSpan("abcbed", 2));    // 3
        System.out.println(new Solution().kCharactersSpan("aaaaabc", 3));   // 7
        System.out.println(new Solution().kCharactersSpan("abcdefgh", 3));  // 3

        // Edge cases
        System.out.println(new Solution().kCharactersSpan("", 2));          // 0
        System.out.println(new Solution().kCharactersSpan("a", 1));         // 1
        System.out.println(new Solution().kCharactersSpan("aaa", 1));       // 3
        System.out.println(new Solution().kCharactersSpan("abc", 0));       // 0
        System.out.println(new Solution().kCharactersSpan("aab", 2));       // 3
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
