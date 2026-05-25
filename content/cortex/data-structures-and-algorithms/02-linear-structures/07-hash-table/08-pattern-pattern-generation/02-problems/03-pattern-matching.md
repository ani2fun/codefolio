---
title: "Pattern Matching"
summary: "Given a pattern string and a string s of space-separated words, return true if s follows pattern — meaning there is a bijection between letters of pattern and non-empty words of s."
prereqs:
  - 08-pattern-pattern-generation/01-pattern
difficulty: medium
---

# Pattern matching

## Problem Statement

Given a `pattern` string and a string `s` of space-separated words, return `true` if `s` follows `pattern` — meaning there is a **bijection** between letters of `pattern` and non-empty words of `s`.

### Example 1
> -   **Input:** `pattern = "mom", s = "hello world hello"` → **Output:** `true`

### Example 2
> -   **Input:** `pattern = "abc", s = "hello my name"` → **Output:** `true`

### Example 3
> -   **Input:** `pattern = "abc", s = "hello my my"` → **Output:** `false`

<details>
<summary><h2>Approach</h2></summary>


The key generator works on *any* iterable. Treat `pattern` as a sequence of characters and `s` as a sequence of words; generate a first-occurrence-index pattern for each. The strings match iff the keys are equal.

The bijection requirement is a real constraint: no two pattern letters can map to the same word, *and* no two words can map to the same pattern letter. The first-occurrence-index encoding handles both directions: if it would assign two different items the same index, it doesn't — each gets a fresh index. So if `s` has more distinct words than `pattern` has distinct letters (or vice versa), the keys differ.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run viz=graph viz-root=words
from typing import List

class Solution:
    def generate_pattern(self, words: List[str]) -> str:
        word_to_index = {}
        pattern = ""
        index = 0

        # Create a mapped value based on the first occurrence of each
        # word
        for word in words:
            if word not in word_to_index:
                word_to_index[word] = index
                index += 1
            pattern += str(word_to_index[word]) + ","

        return pattern

    def pattern_matching(self, pattern: str, s: str) -> bool:

        # Split the string s into an array of words
        words = s.split(" ")

        # If the length of pattern and words are different, return false
        if len(pattern) != len(words):
            return False

        # If the generated patterns are the same, return true
        return self.generate_pattern(
            list(pattern)
        ) == self.generate_pattern(words)


# Examples from the problem statement
print(Solution().pattern_matching("mom", "hello world hello"))  # True
print(Solution().pattern_matching("abc", "hello my name"))      # True
print(Solution().pattern_matching("abc", "hello my my"))        # False

# Edge cases
print(Solution().pattern_matching("a", "hello"))                # True
print(Solution().pattern_matching("aa", "hello world"))         # False
print(Solution().pattern_matching("ab", "hello hello"))         # False
print(Solution().pattern_matching("aab", "x x y"))              # True
print(Solution().pattern_matching("abc", "a b c"))              # True
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private List<String> stringToList(String s) {

            // Convert pattern string into a list of single-character strings
            List<String> result = new ArrayList<>();
            for (char c : s.toCharArray()) {
                result.add(String.valueOf(c));
            }
            return result;
        }

        private String generatePattern(List<String> words) {
            Map<String, Integer> wordToIndex = new HashMap<>();
            StringBuilder pattern = new StringBuilder();
            int index = 0;

            // Create a mapped value based on the first occurrence of each
            // word
            for (String word : words) {
                if (!wordToIndex.containsKey(word)) {
                    wordToIndex.put(word, index++);
                }
                pattern.append(wordToIndex.get(word)).append(",");
            }

            return pattern.toString();
        }

        public boolean patternMatching(String pattern, String s) {

            // Split the string s into an array of words
            List<String> words = List.of(s.split(" "));

            // If the length of pattern and words are different, return false
            if (pattern.length() != words.size()) {
                return false;
            }

            // If the generated patterns are the same, return true
            return generatePattern(stringToList(pattern)).equals(
                generatePattern(words)
            );
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().patternMatching("mom", "hello world hello")); // true
        System.out.println(new Solution().patternMatching("abc", "hello my name"));     // true
        System.out.println(new Solution().patternMatching("abc", "hello my my"));       // false

        // Edge cases
        System.out.println(new Solution().patternMatching("a", "hello"));               // true
        System.out.println(new Solution().patternMatching("aa", "hello world"));        // false
        System.out.println(new Solution().patternMatching("ab", "hello hello"));        // false
        System.out.println(new Solution().patternMatching("aab", "x x y"));             // true
        System.out.println(new Solution().patternMatching("abc", "a b c"));             // true
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
