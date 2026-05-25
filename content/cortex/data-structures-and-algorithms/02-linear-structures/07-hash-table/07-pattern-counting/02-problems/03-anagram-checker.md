---
title: "Anagram Checker"
summary: "Given two strings s and p, return true if p is an anagram of s (same multiset of characters), else false."
prereqs:
  - 07-pattern-counting/01-pattern
difficulty: easy
---

# Anagram checker

## Problem Statement

Given two strings `s` and `p`, return `true` if `p` is an anagram of `s` (same multiset of characters), else `false`.

### Example 1
> -   **Input:** `s = "codeintuition", p = "cdoenoitiutni"`
> -   **Output:** `true`

### Example 2
> -   **Input:** `s = "abc", p = "ade"`
> -   **Output:** `false`

### Example 3
> -   **Input:** `s = "abcdef", p = "dfecba"`
> -   **Output:** `true`

<details>
<summary><h2>Approach</h2></summary>


Anagrams have the same length and the same character frequency map. Build the frequency of `s`, then walk `p` and decrement; if any character is missing or counts disagree, return `false`. The map ends empty iff the two are anagrams.

> *Mental shortcut* — anagram checking is "does the multiset match?". The frequency map *is* the multiset.

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

    def anagram_checker(self, s: str, t: str) -> bool:

        # If the strings are of different lengths, they cannot be
        # anagrams
        if len(s) != len(t):
            return False

        # Create a map to store the frequency of each character in the
        # first string
        s_frequency = self.count_frequency(s)

        # Traverse the second string and decrement the frequency of each
        # character in the hash map
        for ch in t:
            if ch not in s_frequency:
                return False

            s_frequency[ch] -= 1
            if s_frequency[ch] == 0:
                del s_frequency[ch]

        return len(s_frequency) == 0


# Examples from the problem statement
print(Solution().anagram_checker("codeintuition", "cdoenoitiutni"))  # True
print(Solution().anagram_checker("abc", "ade"))                      # False
print(Solution().anagram_checker("abcdef", "dfecba"))                # True

# Edge cases
print(Solution().anagram_checker("", ""))                            # True
print(Solution().anagram_checker("a", "a"))                          # True
print(Solution().anagram_checker("a", "b"))                          # False
print(Solution().anagram_checker("ab", "a"))                         # False
print(Solution().anagram_checker("aab", "baa"))                      # True
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

        public boolean anagramChecker(String s, String t) {

            // If the strings are of different lengths, they cannot be
            // anagrams
            if (s.length() != t.length()) {
                return false;
            }

            // Create a map to store the frequency of each character in the
            // first string
            Map<Character, Integer> sFrequency = countFrequency(s);

            // Traverse the second string and decrement the frequency of each
            // character in the hash map
            for (char ch : t.toCharArray()) {
                if (!sFrequency.containsKey(ch)) {
                    return false;
                }

                sFrequency.put(ch, sFrequency.get(ch) - 1);
                if (sFrequency.get(ch) == 0) {
                    sFrequency.remove(ch);
                }
            }

            return sFrequency.isEmpty();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().anagramChecker("codeintuition", "cdoenoitiutni")); // true
        System.out.println(new Solution().anagramChecker("abc", "ade"));                     // false
        System.out.println(new Solution().anagramChecker("abcdef", "dfecba"));               // true

        // Edge cases
        System.out.println(new Solution().anagramChecker("", ""));                           // true
        System.out.println(new Solution().anagramChecker("a", "a"));                         // true
        System.out.println(new Solution().anagramChecker("a", "b"));                         // false
        System.out.println(new Solution().anagramChecker("ab", "a"));                        // false
        System.out.println(new Solution().anagramChecker("aab", "baa"));                     // true
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
