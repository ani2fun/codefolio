---
title: "First Non-Repeating Character"
summary: "Given a string s, find and return the index of the first non-repeating character. Return -1 if no such character exists."
prereqs:
  - 07-pattern-counting/01-pattern
difficulty: easy
---

# First non repeating character

## Problem Statement

Given a string `s`, find and return the index of the first non-repeating character. Return `-1` if no such character exists.

### Example 1
> -   **Input:** `s = "codeintuition"`
> -   **Output:** `0`
> -   **Explanation:** `'c'` is the first non-repeating character.

### Example 2
> -   **Input:** `s = "aaabcd"`
> -   **Output:** `3`
> -   **Explanation:** `'b'` is the first non-repeating character.

### Example 3
> -   **Input:** `s = "aaabbccdd"`
> -   **Output:** `-1`
> -   **Explanation:** No character appears exactly once.

<details>
<summary><h2>Solution</h2></summary>


Two passes: build the frequency map, then re-walk to find the first frequency-1 character.


```python run
from collections import defaultdict
from typing import Dict

class Solution:
    def count_frequency(self, s: str) -> Dict[str, int]:
        frequency = defaultdict(int)

        # Traverse the string and store the frequency of each character
        # in a hash map
        for ch in s:
            frequency[ch] = frequency.get(ch, 0) + 1

        return frequency

    def first_non_repeating_character(self, s: str) -> int:

        # Create a map to store the frequency of each character in the
        # string
        frequency = self.count_frequency(s)

        # Traverse the string again and return the index of the first
        # non-repeating character
        for i, ch in enumerate(s):
            if frequency[ch] == 1:
                return i

        return -1


# Examples from the problem statement
print(Solution().first_non_repeating_character("codeintuition"))  # 0
print(Solution().first_non_repeating_character("aaabcd"))         # 3
print(Solution().first_non_repeating_character("aaabbccdd"))      # -1

# Edge cases
print(Solution().first_non_repeating_character(""))               # -1
print(Solution().first_non_repeating_character("a"))              # 0
print(Solution().first_non_repeating_character("aabb"))           # -1
print(Solution().first_non_repeating_character("abcabc"))         # -1
print(Solution().first_non_repeating_character("abcd"))           # 0
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private Map<Character, Integer> countFrequency(String s) {
            Map<Character, Integer> frequency = new HashMap<>();

            // Traverse the string and store the frequency of each character
            // in a hash map
            for (char ch : s.toCharArray()) {
                frequency.put(ch, frequency.getOrDefault(ch, 0) + 1);
            }

            return frequency;
        }

        public int firstNonRepeatingCharacter(String s) {

            // Create a map to store the frequency of each character in the
            // string
            Map<Character, Integer> frequency = countFrequency(s);

            // Traverse the string again and return the index of the first
            // non-repeating character
            for (int i = 0; i < s.length(); i++) {
                if (frequency.get(s.charAt(i)) == 1) {
                    return i;
                }
            }

            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().firstNonRepeatingCharacter("codeintuition")); // 0
        System.out.println(new Solution().firstNonRepeatingCharacter("aaabcd"));        // 3
        System.out.println(new Solution().firstNonRepeatingCharacter("aaabbccdd"));     // -1

        // Edge cases
        System.out.println(new Solution().firstNonRepeatingCharacter(""));              // -1
        System.out.println(new Solution().firstNonRepeatingCharacter("a"));             // 0
        System.out.println(new Solution().firstNonRepeatingCharacter("aabb"));          // -1
        System.out.println(new Solution().firstNonRepeatingCharacter("abcabc"));        // -1
        System.out.println(new Solution().firstNonRepeatingCharacter("abcd"));          // 0
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
