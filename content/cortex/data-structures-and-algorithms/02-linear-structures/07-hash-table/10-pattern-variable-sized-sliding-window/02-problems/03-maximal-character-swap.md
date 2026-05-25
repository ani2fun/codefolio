---
title: "Maximal Character Swap"
summary: "Given an uppercase string s and integer k, you may replace at most k characters with any uppercase letters of your choice. Return the length of the longest substring of equal letters achievable."
prereqs:
  - 10-pattern-variable-sized-sliding-window/01-pattern
difficulty: medium
---

# Maximal character swap

## Problem Statement

Given an uppercase string `s` and integer `k`, you may replace at most `k` characters with any uppercase letters of your choice. Return the length of the longest substring of equal letters achievable.

### Example 1
> -   **Input:** `s = "ABAB", k = 2` → **Output:** `4` (replace either `A`s with `B`s)

### Example 2
> -   **Input:** `s = "ABCDEF", k = 4` → **Output:** `5` (pick a letter, replace 4 others)

### Example 3
> -   **Input:** `s = "A", k = 5` → **Output:** `1`

<details>
<summary><h2>Approach</h2></summary>


For a window `[start..end]` to be turn-able into all-same-letter with ≤ K replacements, it must satisfy `(window_size − count_of_most_frequent_letter) ≤ k`. The "extra" characters (everything except the dominant letter) are exactly what we'd need to replace.

So slide the window; track frequencies; track `maxFreq` (the highest count any letter has had so far in the window). The rule is `(end − start + 1) − maxFreq > k` → contract.

A subtle but allowed shortcut: when contracting, we *don't* need to shrink `maxFreq` — even a stale `maxFreq` is a valid lower bound, and the answer only cares about the maximum window seen, which only grows when `maxFreq` grows. This makes the algorithm clean and still correct.

> 🖼 Diagram — Maximal character swap — replacements needed = window size − count of most frequent letter. As long as that count is ≤ K, the window is achievable.
```d2
direction: right

w: |md
  **window 'AABA'**

  size 4

  most freq: A -> 3
| {style.fill: "#fef9c3"; style.stroke: "#d97706"}

calc: "replacements needed = 4 - 3 = 1"

ok: "<= k = 2 ? yes -> window valid" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}

w -> calc -> ok
```

<p align="center"><strong>Maximal character swap — replacements needed = window size − count of most frequent letter. As long as that count is ≤ K, the window is achievable.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from collections import defaultdict

class Solution:
    def maximal_character_swap(self, s: str, k: int) -> int:

        # Initialize the frequency map to track the count of characters
        # in the window
        frequency = defaultdict(int)

        # The start and end pointers for the window
        start, end = 0, 0

        # Tracks the frequency and length of the most common character in
        # the window
        max_freq = 0
        max_length = 0

        # Traverse the string using the while loop
        while end < len(s):

            # Add the current character to the frequency map
            char_end = s[end]
            frequency[char_end] += 1

            # Update maxFreq, the frequency of the most frequent
            # character in the window
            max_freq = max(max_freq, frequency[char_end])

            # If the current window size minus the frequency of the most
            # frequent character is greater than k. It means we have more
            # than k characters to replace, so we shrink the window
            while end - start + 1 - max_freq > k:
                char_start = s[start]
                frequency[char_start] -= 1

                # Shrink the window from the left
                start += 1

            # Update maxLength to the current window size
            max_length = max(max_length, end - start + 1)

            # Move the end pointer to expand the window
            end += 1

        return max_length


# Examples from the problem statement
print(Solution().maximal_character_swap("ABAB", 2))    # 4
print(Solution().maximal_character_swap("ABCDEF", 4))  # 5
print(Solution().maximal_character_swap("A", 5))       # 1

# Edge cases
print(Solution().maximal_character_swap("", 2))        # 0
print(Solution().maximal_character_swap("AA", 0))      # 2
print(Solution().maximal_character_swap("AB", 0))      # 1
print(Solution().maximal_character_swap("AABB", 1))    # 3
print(Solution().maximal_character_swap("AAAA", 2))    # 4
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int maximalCharacterSwap(String s, int k) {

            // Initialize the frequency map to track the count of characters
            // in the window
            Map<Character, Integer> frequency = new HashMap<>();

            // The start and end pointers for the window
            int start = 0;
            int end = 0;

            // Tracks the frequency and length of the most common character
            // in the window
            int maxFreq = 0;
            int maxLength = 0;

            // Traverse the string using the while loop
            while (end < s.length()) {

                // Add the current character to the frequency map
                char endChar = s.charAt(end);
                frequency.put(
                    endChar,
                    frequency.getOrDefault(endChar, 0) + 1
                );

                // Update maxFreq, the frequency of the most frequent
                // character in the window
                maxFreq = Math.max(maxFreq, frequency.get(endChar));

                // If the current window size minus the frequency of the most
                // frequent character is greater than k It means we have more
                // than k characters to replace, so we shrink the window
                while (end - start + 1 - maxFreq > k) {
                    char startChar = s.charAt(start);
                    frequency.put(startChar, frequency.get(startChar) - 1);

                    // Shrink the window from the left
                    start++;
                }

                // Update maxLength to the current window size
                maxLength = Math.max(maxLength, end - start + 1);

                // Move the end pointer to expand the window
                end++;
            }

            return maxLength;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().maximalCharacterSwap("ABAB", 2));    // 4
        System.out.println(new Solution().maximalCharacterSwap("ABCDEF", 4));  // 5
        System.out.println(new Solution().maximalCharacterSwap("A", 5));       // 1

        // Edge cases
        System.out.println(new Solution().maximalCharacterSwap("", 2));        // 0
        System.out.println(new Solution().maximalCharacterSwap("AA", 0));      // 2
        System.out.println(new Solution().maximalCharacterSwap("AB", 0));      // 1
        System.out.println(new Solution().maximalCharacterSwap("AABB", 1));    // 3
        System.out.println(new Solution().maximalCharacterSwap("AAAA", 2));    // 4
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
