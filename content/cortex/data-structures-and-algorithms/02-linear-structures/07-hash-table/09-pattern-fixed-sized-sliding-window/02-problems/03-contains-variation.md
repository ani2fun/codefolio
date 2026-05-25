---
title: "Contains Variation"
summary: "Given two strings s1 and s2, return true if s2 contains a permutation of s1, else false."
prereqs:
  - 09-pattern-fixed-sized-sliding-window/01-pattern
difficulty: medium
---

# Contains variation

## Problem Statement

Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`, else `false`.

### Example 1
> -   **Input:** `s1 = "abc", s2 = "edbaclm"` → **Output:** `true` (`"bac"` is a permutation of `"abc"`)

### Example 2
> -   **Input:** `s1 = "cod", s2 = "intdoce"` → **Output:** `true` (`"doc"`)

### Example 3
> -   **Input:** `s1 = "abc", s2 = "defghiab"` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


The window size is fixed: `len(s1)`. A window is a permutation of `s1` iff the window's frequency map equals `s1`'s frequency map. Slide the window over `s2`; compare the maps each time the window is the right size.

To avoid O(K) map comparisons every step, you can maintain a counter `matches` of how many distinct characters have *exactly* the right count. We use the simpler `map == map` here for clarity; the optimisation matters only for large K.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from collections import defaultdict
from typing import Dict

class Solution:
    def count_frequency(self, s: str) -> Dict[str, int]:
        frequency = defaultdict(int)
        for ch in s:
            frequency[ch] += 1

        return frequency

    def contains_variation(self, s1: str, s2: str) -> bool:

        # Frequency map for s1
        s1_frequency = self.count_frequency(s1)

        # Frequency maps for characters in sliding window in s2
        frequency = defaultdict(int)

        # The start and end pointers for the window
        start, end = 0, 0

        while end < len(s2):

            # Add the current character to the window
            char_end = s2[end]
            frequency[char_end] += 1

            # If the window size matches s1's length, check for a match
            if end - start + 1 == len(s1):
                if frequency == s1_frequency:
                    return True

                # Shrink the window from the left
                char_start = s2[start]
                frequency[char_start] -= 1
                if frequency[char_start] == 0:
                    del frequency[char_start]
                start += 1

            # Expand the window to the right
            end += 1

        return False


# Examples from the problem statement
print(Solution().contains_variation("abc", "edbaclm"))   # True
print(Solution().contains_variation("cod", "intdoce"))   # True
print(Solution().contains_variation("abc", "defghiab"))  # False

# Edge cases
print(Solution().contains_variation("a", "a"))           # True
print(Solution().contains_variation("ab", "ba"))         # True
print(Solution().contains_variation("abc", "abc"))       # True
print(Solution().contains_variation("abc", "ab"))        # False
print(Solution().contains_variation("aa", "aab"))        # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private Map<Character, Integer> countFrequency(String s) {
            Map<Character, Integer> frequency = new HashMap<>();
            for (char ch : s.toCharArray()) {
                frequency.put(ch, frequency.getOrDefault(ch, 0) + 1);
            }

            return frequency;
        }

        public boolean containsVariation(String s1, String s2) {

            // Frequency map for s1
            Map<Character, Integer> s1Frequency = countFrequency(s1);

            // Frequency maps for characters in sliding window in s2
            Map<Character, Integer> frequency = new HashMap<>();

            // The start and end pointers for the window
            int start = 0;
            int end = 0;

            while (end < s2.length()) {

                // Add the current character to the window
                char endChar = s2.charAt(end);
                frequency.put(
                    endChar,
                    frequency.getOrDefault(endChar, 0) + 1
                );

                // If the window size matches s1's length, check for a match
                if (end - start + 1 == s1.length()) {
                    if (frequency.equals(s1Frequency)) {
                        return true;
                    }

                    // Shrink the window from the left
                    char startChar = s2.charAt(start);
                    frequency.put(startChar, frequency.get(startChar) - 1);
                    if (frequency.get(startChar) == 0) {
                        frequency.remove(startChar);
                    }
                    start++;
                }

                // Expand the window to the right
                end++;
            }

            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().containsVariation("abc", "edbaclm"));   // true
        System.out.println(new Solution().containsVariation("cod", "intdoce"));   // true
        System.out.println(new Solution().containsVariation("abc", "defghiab"));  // false

        // Edge cases
        System.out.println(new Solution().containsVariation("a", "a"));           // true
        System.out.println(new Solution().containsVariation("ab", "ba"));         // true
        System.out.println(new Solution().containsVariation("abc", "abc"));       // true
        System.out.println(new Solution().containsVariation("abc", "ab"));        // false
        System.out.println(new Solution().containsVariation("aa", "aab"));        // true
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
