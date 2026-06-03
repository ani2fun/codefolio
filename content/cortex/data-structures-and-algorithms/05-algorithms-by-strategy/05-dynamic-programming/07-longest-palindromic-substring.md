---
title: "Longest Palindromic Substring"
summary: "<!-- TODO: summary -->"
---

# 7. Longest Palindromic Substring

The previous lesson found the longest palindromic *subsequence* — characters that read the same backward, gaps allowed. **Substring** is the contiguous version: the palindrome must be a continuous slice of the original string. The two-character difference between "subsequence" and "substring" changes everything: now we need a *boolean* table — "is this slice a palindrome?" — and then the longest contiguous palindrome falls out.

By the end of this lesson you'll know the **Longest Palindromic Substring** recurrence (`isPalin[i][j] = (s[i] == s[j]) and isPalin[i+1][j-1]`), how to track the longest palindrome's length and start position as the table fills, and how the same problem can be solved in `O(n²)` time with `O(1)` extra space using the "expand around center" trick.

## Table of contents

1. [The Palindromic-Substring Problem](#the-palindromic-substring-problem)
2. [The `isPalindrome` Recurrence](#the-ispalindrome-recurrence)
3. [Longest Palindromic Substring](#longest-palindromic-substring)
4. [Alternate: Expand Around Center](#alternate-expand-around-center)

***

# The Palindromic-Substring Problem

Given a string `s`, find the longest substring (contiguous) that is itself a palindrome.

```d2
direction: right
ex: "Example: s = 'forgeeksskeegfor'" {
  grid-rows: 2
  grid-columns: 16
  grid-gap: 0
  c0: "f"
  c1: "o"
  c2: "r"
  c3: "g" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c4: "e" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c5: "e" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c6: "k" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c7: "s" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c8: "s" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c9: "k" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c10:"e" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c11:"e" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c12:"g" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  c13:"f"
  c14:"o"
  c15:"r"
  l0: "[0]"
  l1: "[1]"
  l2: "[2]"
  l3: "[3]"
  l4: "[4]"
  l5: "[5]"
  l6: "[6]"
  l7: "[7]"
  l8: "[8]"
  l9: "[9]"
  l10:"[10]"
  l11:"[11]"
  l12:"[12]"
  l13:"[13]"
  l14:"[14]"
  l15:"[15]"
}
```

<p align="center"><strong>The longest palindromic substring of <code>"forgeeksskeegfor"</code> is <code>"geeksskeeg"</code> (length 10), spanning indices [3..12]. The characters must be contiguous — no gaps — to count as a substring.</strong></p>

> *Predict before reading on — for <code>s = "babad"</code>, what's the LPSubstr?</em>

`"bab"` or `"aba"` — both are length 3. Either is correct.

---

## Key Takeaway

LPSubstr requires contiguity. Brute force (check all `O(n²)` substrings, palindromicity check `O(n)`) is `O(n³)`. DP brings it to `O(n²)`.

***

# The `isPalindrome` Recurrence

Define `isPalin[i][j]` = `True` if `s[i..j]` is a palindrome, `False` otherwise. Three cases:

- **Length 1** (`i == j`): always palindromic. `isPalin[i][i] = True`.
- **Length 2** (`j == i + 1`): palindromic iff `s[i] == s[j]`.
- **Length ≥ 3**: palindromic iff `s[i] == s[j]` *and* `isPalin[i+1][j-1]` is `True`.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  CASE["s[i..j] palindromic?"]
  CASE -->|"length 1"| YES1["True"]
  CASE -->|"length 2"| EQ2["s[i] == s[j]?"]
  CASE -->|"length ≥ 3"| EQ3["s[i] == s[j]<br/>AND<br/>isPalin[i+1][j-1]?"]
```

<p align="center"><strong>Three sub-cases of the palindromic-substring check. Length 1: always true. Length 2: endpoint equality. Length ≥ 3: endpoints equal AND interior is palindromic.</strong></p>

Once the table is full, the answer is the longest range `(i, j)` with `isPalin[i][j] == True`.

## Why an Inner Substring Has to Be Palindromic Too

Imagine a palindrome of length 7. Strip the first and last characters — the result is still a palindrome (length 5). Strip again — still palindromic (length 3). The recursion bottoms out at length 1 or 2. So if `s[i..j]` is a palindrome and longer than 2, *its interior* `s[i+1..j-1]` must also be palindromic. This is exactly what the recurrence checks.

---

## Key Takeaway

Substring palindromicity is a single-bit predicate that propagates inward. Filling order is by length; the answer is the longest range marked `True`.

***

# Longest Palindromic Substring

## The Problem

Given a string `s`, return its longest palindromic substring (any one is acceptable on ties).

```
Input:  s = "babad"
Output: "bab"   (or "aba")

Input:  s = "cbbd"
Output: "bb"

Input:  s = "forgeeksskeegfor"
Output: "geeksskeeg"
```

---

<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| # | Question | Answer |
|---|---|---|
| **Q1** | Optimal substructure? | **Yes** — interior must be palindromic. |
| **Q2** | Overlapping subproblems? | **Yes** — `(i+1, j-1)` is reached from many `(i, j)`. |
| **Q3** | 2D state? | **Yes** — `(i, j)` substring range. |
| **Q4** | Filling order? | **By length, ascending** — same as LPS. |

### Q1 — Why "Yes"?

**Mental model.** A palindrome is recursively defined: it's empty (length 0), a single character (length 1), or two equal endpoints surrounding a smaller palindrome.

**Concrete numbers.** For `"abba"`: outer pair `('a', 'a')` matches; inner `"bb"` is also a palindrome → `"abba"` is palindromic.

**What breaks otherwise.** If we required only endpoint equality (no inner check), `"abca"` would be considered a palindrome — wrong.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

Track the longest palindrome's length and start position as the table fills.


```python run viz=graph viz-root=is_palindrome
from typing import List

class Solution:
    def longest_palindromic_substring(self, s: str) -> str:

        n: int = len(s)
        if n == 0:
            return ""

        # is_palindrome[i][j] stores whether substring s[i..j] is a palindrome
        is_palindrome: List[List[bool]] = [[False] * n for _ in range(n)]

        max_length: int = 1
        start: int = 0

        # Base case: every single character is a palindrome
        for i in range(n):
            is_palindrome[i][i] = True

        # Fill the table by increasing substring length
        for length in range(2, n + 1):
            for i in range(n - length + 1):
                j: int = i + length - 1

                # Boundary characters must match for s[i..j] to be a palindrome
                if s[i] == s[j]:
                    # Either length is 2, or the inner substring is a palindrome
                    is_palindrome[i][j] = (length == 2) or is_palindrome[i + 1][j - 1]

                # Update longest palindrome length if current substring is longer
                if is_palindrome[i][j] and length > max_length:
                    max_length = length
                    start = i

        return s[start : start + max_length]


if __name__ == "__main__":
    print(Solution().longest_palindromic_substring("forgeeksskeegfor"))   # "geeksskeeg"
```

```java run viz=graph viz-root=is_palindrome
public class Main {
    static class Solution {

        public String longestPalindromicSubstring(String s) {

            int n = s.length();
            if (n == 0) return "";

            // isPalindrome[i][j] stores whether substring s[i..j] is a palindrome
            boolean[][] isPalindrome = new boolean[n][n];

            int maxLength = 1;
            int start = 0;

            // Base case: every single character is a palindrome
            for (int i = 0; i < n; i++) {
                isPalindrome[i][i] = true;
            }

            // Fill the table by increasing substring length
            for (int length = 2; length <= n; length++) {
                for (int i = 0; i <= n - length; i++) {
                    int j = i + length - 1;

                    // Boundary characters must match for s[i..j] to be a palindrome
                    if (s.charAt(i) == s.charAt(j)) {
                        // Either length is 2, or the inner substring is a palindrome
                        isPalindrome[i][j] = (length == 2) || isPalindrome[i + 1][j - 1];
                    }

                    // Update longest palindrome length if current substring is longer
                    if (isPalindrome[i][j] && length > maxLength) {
                        maxLength = length;
                        start = i;
                    }
                }
            }

            return s.substring(start, start + maxLength);
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().longestPalindromicSubstring("forgeeksskeegfor"));
    }
}
```

### Complexity

| Aspect | Cost | Why |
|---|---|---|
| Time | `O(n²)` | One cell per `(i, j)` pair with `i ≤ j`. |
| Space | `O(n²)` | The boolean table. |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Empty | `""` | `""` | Guard returns empty. |
| Single | `"a"` | `"a"` | Length-1 base case. |
| All same | `"aaaa"` | `"aaaa"` | Whole string is palindromic. |
| All distinct | `"abc"` | `"a"` | Best is length 1. |
| Even-length palindrome | `"cbbd"` | `"bb"` | Found via length-2 case. |

</details>

***

# Alternate: Expand Around Center

A palindrome has a *center* — a character (odd-length palindrome) or a gap between two characters (even-length palindrome). There are `2n - 1` possible centers in a string of length `n`. For each, expand outward as far as the substring stays palindromic. The longest expansion is the answer.

This runs in `O(n²)` time and **O(1) space** — no DP table needed. Same Big-O as the DP approach but constant-factor faster and trivial memory.

```python run viz=array viz-root=s
class Solution:
    def longest_palindromic_substring_center(self, s: str) -> str:
        if not s:
            return ""
        start, max_len = 0, 1
        for centre in range(len(s)):
            # Odd-length palindromes (single-character centre)
            l, r = centre, centre
            while l >= 0 and r < len(s) and s[l] == s[r]:
                if r - l + 1 > max_len:
                    start, max_len = l, r - l + 1
                l -= 1
                r += 1
            # Even-length palindromes (gap centre, between centre and centre+1)
            l, r = centre, centre + 1
            while l >= 0 and r < len(s) and s[l] == s[r]:
                if r - l + 1 > max_len:
                    start, max_len = l, r - l + 1
                l -= 1
                r += 1
        return s[start : start + max_len]
```

There's also **Manacher's algorithm**, which solves LPSubstr in `O(n)` linear time using a clever extension of expand-around-center — but it's intricate and beyond this section.

---

## Final Takeaway

LPSubstr replaces LPS's "max length" DP with an "is it a palindrome?" boolean DP. Track the running longest as the table fills. Same time complexity as LPS (`O(n²)`); same interval-DP filling order.

> *Transfer challenge for the next lesson:* Both LPS and LPSubstr returned a *single* palindrome. The next problem asks: how many *partitions* does it take to split the string so that *every part* is a palindrome? Predict the recurrence's shape.

<details>
<summary><strong>Answer</strong></summary>

`cuts(i)` = minimum cuts needed for `s[0..i]`. If `s[0..i]` is already a palindrome, 0 cuts. Otherwise, try every split point `j`: `cuts(i) = 1 + cuts(j) + 0` if `s[j+1..i]` is palindromic. Take the min. The next lesson formalises this.

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: The Hook — missing, needs to be written -->
<!--       Guidance: real-world story opening before any definition -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->
