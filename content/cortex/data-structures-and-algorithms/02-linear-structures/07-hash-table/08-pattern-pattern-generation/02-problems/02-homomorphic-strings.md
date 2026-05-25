---
title: "Homomorphic Strings"
summary: "Given two strings s and t, return true if they are homomorphic: each unique character of s can be replaced (consistently, no two distinct characters mapping to the same target) to produce t."
prereqs:
  - 08-pattern-pattern-generation/01-pattern
difficulty: medium
---

# Homomorphic strings

## Problem Statement

Given two strings `s` and `t`, return `true` if they are **homomorphic**: each unique character of `s` can be replaced (consistently, no two distinct characters mapping to the same target) to produce `t`.

### Example 1
> -   **Input:** `s = "add", t = "qpp"` → **Output:** `true`

### Example 2
> -   **Input:** `s = "dad", t = "mom"` → **Output:** `true`

### Example 3
> -   **Input:** `s = "all", t = "mom"` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


Apply the `generatePattern` function we built above to both strings; compare the resulting keys. The first-occurrence-index encoding *is* the canonical shape of a string, so two strings are homomorphic iff their patterns match exactly.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run viz=graph viz-root=char_to_index
from typing import Dict

class Solution:
    def generate_pattern(self, s: str) -> str:
        char_to_index: Dict[str, int] = {}
        pattern = ""
        index = 0

        # Create a mapped value based on the first occurrence of each
        # character
        for ch in s:
            if ch not in char_to_index:
                char_to_index[ch] = index
                index += 1
            pattern += str(char_to_index[ch]) + ","

        return pattern

    def homomorphic_strings(self, s: str, t: str) -> bool:

        # Strings of different lengths can't be homomorphic
        if len(s) != len(t):
            return False

        # If the generated patterns are the same, the strings are
        # homomorphic
        return self.generate_pattern(s) == self.generate_pattern(t)


# Examples from the problem statement
print(Solution().homomorphic_strings("add", "qpp"))   # True
print(Solution().homomorphic_strings("dad", "mom"))   # True
print(Solution().homomorphic_strings("all", "mom"))   # False

# Edge cases
print(Solution().homomorphic_strings("", ""))         # True
print(Solution().homomorphic_strings("a", "b"))       # True
print(Solution().homomorphic_strings("ab", "aa"))     # False
print(Solution().homomorphic_strings("aa", "ab"))     # False
print(Solution().homomorphic_strings("abc", "xyz"))   # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private String generatePattern(String str) {
            Map<Character, Integer> charToIndex = new HashMap<>();
            StringBuilder pattern = new StringBuilder();
            int index = 0;

            // Create a mapped value based on the first occurrence of each
            // character
            for (char ch : str.toCharArray()) {
                if (!charToIndex.containsKey(ch)) {
                    charToIndex.put(ch, index++);
                }
                pattern.append(charToIndex.get(ch)).append(",");
            }

            return pattern.toString();
        }

        public boolean homomorphicStrings(String s, String t) {

            // Strings of different lengths can't be homomorphic
            if (s.length() != t.length()) {
                return false;
            }

            // If the generated patterns are the same, the strings are
            // homomorphic
            return generatePattern(s).equals(generatePattern(t));
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().homomorphicStrings("add", "qpp"));  // true
        System.out.println(new Solution().homomorphicStrings("dad", "mom"));  // true
        System.out.println(new Solution().homomorphicStrings("all", "mom"));  // false

        // Edge cases
        System.out.println(new Solution().homomorphicStrings("", ""));        // true
        System.out.println(new Solution().homomorphicStrings("a", "b"));      // true
        System.out.println(new Solution().homomorphicStrings("ab", "aa"));    // false
        System.out.println(new Solution().homomorphicStrings("aa", "ab"));    // false
        System.out.println(new Solution().homomorphicStrings("abc", "xyz"));  // true
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
