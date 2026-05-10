---
title: KMP (Knuth-Morris-Pratt)
summary: O(n + m) string matching via the failure function. Precompute "how much of the pattern's prefix is also a suffix?" so a mismatch lets you skip ahead instead of restarting.
prereqs:
  - strings-string-matching-naive
---

# 2. KMP (Knuth-Morris-Pratt)

## The Hook

In naive matching, when you mismatch at position `j` in the pattern, you slide the pattern by one position and start over from `j = 0`. That's wasteful. If the pattern is `ABCDABE` and you've matched the first six characters before mismatching on `E`, *you already know the next characters of the text are `ABCDABx`*. The pattern's first two characters are `AB`, which match positions 4–5 of the substring you already have. So instead of restarting at the next `T` position, you can skip ahead and resume the match at `j = 2`.

KMP precomputes this table — *for each pattern position `j`, what's the largest proper prefix of `P[0..j-1]` that is also a suffix?* The table is called the **failure function** (or **`pi` array**). With it, the matching loop runs in `O(n + m)`: each character of `T` is examined at most twice (once moving forward, once being skipped).

This chapter walks through the failure function, the matching algorithm, and the proof that both run in linear time.

---

## Table of contents

1. [The failure function](#the-failure-function)
2. [Building the failure function](#building-the-failure-function)
3. [The matching algorithm](#the-matching-algorithm)
4. [Implementation](#implementation)
5. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
6. [Production reality](#production-reality)
7. [Practice ladder](#practice-ladder)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The Failure Function

For pattern `P`, the failure function `pi` is an array of length `m`:

> `pi[i]` = length of the longest *proper* prefix of `P[0..i]` that is also a suffix of `P[0..i]`.

"Proper" means shorter than `P[0..i]` itself.

Examples for `P = "ABABC"`:

```
i:    0  1  2  3  4
P[i]: A  B  A  B  C
pi:   0  0  1  2  0
```

- `pi[0] = 0`: trivially.
- `pi[1] = 0`: `AB` has no proper prefix-suffix overlap.
- `pi[2] = 1`: `ABA` has prefix `A` matching suffix `A`.
- `pi[3] = 2`: `ABAB` has prefix `AB` matching suffix `AB`.
- `pi[4] = 0`: `ABABC` has no nontrivial overlap.

The intuition: `pi[i]` tells you, "if you've matched `P[0..i]` against `T[0..n]` and the next character mismatches, how much of `P` you can keep matching by sliding the pattern forward."

***

# Building the failure function

```pseudocode
function buildFailure(P):
    m ← length(P)
    pi ← array of m zeros
    k ← 0                                    # length of previous longest prefix-suffix
    for i from 1 to m − 1:
        while k > 0 AND P[k] ≠ P[i]:
            k ← pi[k − 1]                    # fall back to next-shorter candidate
        if P[k] = P[i]:
            k ← k + 1
        pi[i] ← k
    return pi
```

**Key insight.** When extending from `pi[i-1]` to `pi[i]`, we maintain `k` = "current candidate prefix length". If `P[k] == P[i]`, the prefix extends by one character. If not, we fall back to the next-shorter prefix that *is* also a suffix — `pi[k-1]` — and try again.

**Cost.** `O(m)`. The amortised analysis: `k` is incremented at most `m` times (once per `i`). Each `while` iteration *decreases* `k`. So total `while` iterations are bounded by total increments — at most `m`.

***

# The matching algorithm

```pseudocode
function kmpMatch(T, P):
    pi ← buildFailure(P)
    n ← length(T); m ← length(P)
    matches ← []
    j ← 0                                    # current pattern index
    for i from 0 to n − 1:
        while j > 0 AND T[i] ≠ P[j]:
            j ← pi[j − 1]                    # fall back
        if T[i] = P[j]:
            j ← j + 1
        if j = m:
            matches.append(i − m + 1)
            j ← pi[j − 1]                    # don't restart; allow overlapping matches
    return matches
```

**Cost.** `O(n)`. Same amortised argument: `j` is incremented at most `n` times; each `while` iteration decreases `j`; total iterations bounded by `n`.

**Combined cost.** `O(n + m)`. Beats naive's worst case `O(nm)` by a factor of `min(n, m)`.

***

# Implementation

```python run
def build_failure(P):
    m = len(P)
    pi = [0] * m
    k = 0
    for i in range(1, m):
        while k > 0 and P[k] != P[i]:
            k = pi[k - 1]
        if P[k] == P[i]:
            k += 1
        pi[i] = k
    return pi

def kmp_match(T, P):
    pi = build_failure(P)
    n, m = len(T), len(P)
    matches = []
    j = 0
    for i in range(n):
        while j > 0 and T[i] != P[j]:
            j = pi[j - 1]
        if T[i] == P[j]:
            j += 1
        if j == m:
            matches.append(i - m + 1)
            j = pi[j - 1]
    return matches


if __name__ == "__main__":
    print(f"failure for 'ABABC': {build_failure('ABABC')}")          # [0, 0, 1, 2, 0]
    print(f"failure for 'AABAACAABAA': {build_failure('AABAACAABAA')}")  # [0, 1, 0, 1, 2, 0, 1, 2, 3, 4, 5]

    print(f"matches: {kmp_match('ABABDABACDABABCABAB', 'ABABCABAB')}")
    print(f"adversarial: {kmp_match('a'*100 + 'b', 'a'*50 + 'b')}")
```

```java run
class Solution {
    static int[] buildFailure(String P) {
        int m = P.length();
        int[] pi = new int[m];
        int k = 0;
        for (int i = 1; i < m; i++) {
            while (k > 0 && P.charAt(k) != P.charAt(i)) k = pi[k - 1];
            if (P.charAt(k) == P.charAt(i)) k++;
            pi[i] = k;
        }
        return pi;
    }

    static java.util.List<Integer> kmpMatch(String T, String P) {
        int[] pi = buildFailure(P);
        int n = T.length(), m = P.length();
        java.util.List<Integer> matches = new java.util.ArrayList<>();
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && T.charAt(i) != P.charAt(j)) j = pi[j - 1];
            if (T.charAt(i) == P.charAt(j)) j++;
            if (j == m) { matches.add(i - m + 1); j = pi[j - 1]; }
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(kmpMatch("ABABDABACDABABCABAB", "ABABCABAB"));
    }
}
```

```c run
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int *build_failure(const char *P, int m) {
    int *pi = calloc(m, sizeof(int));
    int k = 0;
    for (int i = 1; i < m; i++) {
        while (k > 0 && P[k] != P[i]) k = pi[k - 1];
        if (P[k] == P[i]) k++;
        pi[i] = k;
    }
    return pi;
}

void kmp_match(const char *T, const char *P) {
    int n = strlen(T), m = strlen(P);
    int *pi = build_failure(P, m);
    int j = 0;
    for (int i = 0; i < n; i++) {
        while (j > 0 && T[i] != P[j]) j = pi[j - 1];
        if (T[i] == P[j]) j++;
        if (j == m) { printf("%d ", i - m + 1); j = pi[j - 1]; }
    }
    printf("\n");
    free(pi);
}

int main(void) {
    kmp_match("ABABDABACDABABCABAB", "ABABCABAB");
    return 0;
}
```

```scala run
object Solution {
  def buildFailure(P: String): Array[Int] = {
    val m = P.length
    val pi = new Array[Int](m)
    var k = 0
    for (i <- 1 until m) {
      while (k > 0 && P(k) != P(i)) k = pi(k - 1)
      if (P(k) == P(i)) k += 1
      pi(i) = k
    }
    pi
  }

  def kmpMatch(T: String, P: String): List[Int] = {
    val pi = buildFailure(P)
    val n = T.length; val m = P.length
    val out = scala.collection.mutable.ListBuffer.empty[Int]
    var j = 0
    for (i <- 0 until n) {
      while (j > 0 && T(i) != P(j)) j = pi(j - 1)
      if (T(i) == P(j)) j += 1
      if (j == m) { out += i - m + 1; j = pi(j - 1) }
    }
    out.toList
  }

  def main(args: Array[String]): Unit = println(kmpMatch("ABABDABACDABABCABAB", "ABABCABAB"))
}
```

***

# Edge cases and pitfalls

- **Off-by-one in the failure-function recurrence.** `pi[i]` represents prefix-suffix length *for the substring `P[0..i]`*. Easy to confuse with "length of the prefix matched so far" (which is `pi[i-1]` after a mismatch).
- **Pattern of length 1.** `pi = [0]`. The matching loop's `j` only takes values 0 and 1; works correctly.
- **Empty pattern.** Conventionally matches everywhere, or undefined; handle as a special case.
- **Pattern starts with a unique character.** Then `pi` is all zeros, and KMP behaves like naive (no skip benefit). KMP is at its best on patterns with internal repetition.
- **Overlapping matches.** When a match is found at position `i - m + 1`, KMP sets `j = pi[m - 1]` to allow the *next* match to overlap. This is correct for "find all matches"; if you only want non-overlapping, set `j = 0` instead.
- **Building a flawed failure function.** Manual debugging is hard. Test with a known-good reference (Python's `re` module's underlying behaviour, for instance).

***

# Production reality

- **`re` module in Python and `java.util.regex`.** Most regex engines compile patterns to NFAs/DFAs that subsume KMP's logic. KMP is a special case — for *literal* patterns (no metacharacters), some implementations switch to KMP or a similar dedicated algorithm.
- **`grep` (in literal mode).** Uses Boyer-Moore-Horspool by default for literal patterns; falls back to KMP-like algorithms for some cases.
- **DNA sequence search.** BLAST and similar tools use indexed methods (suffix arrays, BWT) for whole-genome work. For small queries, KMP-like linear-time scanning is one option among many.
- **Plagiarism detection.** "Find this 50-word phrase in a corpus of 10M documents" reduces to KMP per document. The total work is `O(sum(n_i) + m * num_docs)`.
- **Educational ubiquity.** KMP is taught as the canonical "stop being naive about string matching" algorithm. Knowing it cold is interview-table-stakes for systems and infrastructure roles.

***

# Practice ladder

1. **Implement strStr() / find().** ([LeetCode 28](https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/)) — find the first occurrence of a pattern in a string.
   > *Hint:* the chapter's KMP, returning the first match.

2. **Build the failure function on paper.** Compute `pi` for `P = "AABAACAABAA"`.
   > *Hint:* the answer is `[0, 1, 0, 1, 2, 0, 1, 2, 3, 4, 5]`. Check against the algorithm.

3. **Repeated Substring Pattern** ([LeetCode 459](https://leetcode.com/problems/repeated-substring-pattern/)) — does the string consist of a substring repeated multiple times?
   > *Hint:* compute the KMP failure function. The string is a repeated substring iff `n` is divisible by `n - pi[n - 1]` and `pi[n - 1] > 0`.

4. **Shortest Palindrome** ([LeetCode 214](https://leetcode.com/problems/shortest-palindrome/)) — find the shortest palindrome obtainable by adding characters in front of `s`.
   > *Hint:* let `s' = s + '#' + reverse(s)`. Compute KMP failure for `s'`. The longest palindromic prefix of `s` has length `pi[len(s') - 1]`.

5. **Find All Occurrences (overlapping).** Given `T` and `P`, return all starting indices including overlapping ones.
   > *Hint:* the chapter's `kmp_match`. Don't reset `j = 0` after a match; use `j = pi[m - 1]`.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. KMP is a procedural algorithm; if you can write the failure-function build and the match loop cold, you can solve every literal-pattern problem in linear time.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Total time complexity of KMP matching of pattern <code>P</code> (length <code>m</code>) in text <code>T</code> (length <code>n</code>)?</summary>

**A:** `O(n + m)`. The failure-function build is `O(m)`; the match loop is `O(n)`.

</details>

<details>
<summary><strong>Q:</strong> Worst-case time of naive substring matching?</summary>

**A:** `O(nm)`. KMP beats this by a factor of `min(n, m)`.

</details>

<details>
<summary><strong>Q:</strong> What does <code>pi[i]</code> represent in the failure function?</summary>

**A:** The length of the longest *proper* prefix of `P[0..i]` that is also a suffix of `P[0..i]`. *Proper* means strictly shorter than `P[0..i]`.

</details>

<details>
<summary><strong>Q:</strong> What does the failure function tell you on a mismatch?</summary>

**A:** How much of the matched prefix you can keep without restarting the comparison from `j = 0`.

</details>

<details>
<summary><strong>Q:</strong> Why are both the build and the match loop <code>O(m)</code> and <code>O(n)</code> respectively, despite the inner <code>while</code>?</summary>

**A:** Amortised: `j` (or `k`) is incremented at most `n` (or `m`) times across the whole loop; each `while` iteration *decreases* it; total iterations ≤ total increments.

</details>

<details>
<summary><strong>Q:</strong> What's <code>pi[0]</code>?</summary>

**A:** `0`. A single-character prefix has no proper prefix-suffix overlap.

</details>

<details>
<summary><strong>Q:</strong> After matching the entire pattern (<code>j == m</code>), what do you set <code>j</code> to in order to find <em>overlapping</em> matches?</summary>

**A:** `j = pi[m - 1]`. Setting `j = 0` would skip overlapping occurrences.

</details>

<details>
<summary><strong>Q:</strong> Compute <code>pi</code> for <code>P = "AABAACAABAA"</code>.</summary>

**A:** `[0, 1, 0, 1, 2, 0, 1, 2, 3, 4, 5]`.

</details>

<details>
<summary><strong>Q:</strong> Sibling algorithm with the same <code>O(n + m)</code> cost via a different array?</summary>

**A:** Z-algorithm. The Z-array stores prefix-match lengths starting at each index.

</details>

<details>
<summary><strong>Q:</strong> Generalisation to multi-pattern matching?</summary>

**A:** Aho-Corasick — KMP failure function on a trie of patterns.

</details>

## Code template

```python
def build_failure(P):
    """O(m) — pi[i] = longest proper prefix of P[0..i] that is also a suffix."""
    m = len(P)
    pi = [0] * m
    k = 0
    for i in range(1, m):
        while k > 0 and P[k] != P[i]:
            k = pi[k - 1]                       # fall back
        if P[k] == P[i]:
            k += 1
        pi[i] = k
    return pi

def kmp_match(T, P):
    """O(n + m) — return all starting indices where P occurs in T."""
    pi = build_failure(P)
    n, m = len(T), len(P)
    matches, j = [], 0
    for i in range(n):
        while j > 0 and T[i] != P[j]:
            j = pi[j - 1]                       # fall back
        if T[i] == P[j]:
            j += 1
        if j == m:
            matches.append(i - m + 1)
            j = pi[j - 1]                       # for overlapping; set 0 for non-overlapping
    return matches
```

## Pattern triggers

- **Find this substring in this string** → `string.find` for a one-shot; KMP if you'll repeat or analyse the pattern
- **Repeated substring pattern — does `S` equal `(sub)ᵏ`?** → KMP failure function: yes iff `n` is divisible by `n − pi[n-1]` and `pi[n-1] > 0`
- **Shortest palindrome by adding chars in front** → KMP on `s + '#' + reverse(s)`; answer length is `pi[len-1]`
- **Stream matches against many fixed patterns** → Aho-Corasick (KMP generalised to a trie)
- **Find pattern with at most one wildcard** → KMP variant or two passes
- **Period of a string** → `n − pi[n-1]` is the shortest period; or use Z-array (`Z[p] + p ≥ n`)
- **Longest border / longest prefix that's also a suffix** → `pi[n-1]` directly
- **Substring search that beats `O(nm)`** → KMP, Z, or Rabin-Karp depending on whether you also need rolling hash

***

# Cross-links

- **Prerequisite:** [Naive String Matching](/cortex/data-structures-and-algorithms/strings-string-matching-naive) — the floor KMP beats.
- **Sibling:** [Z-Algorithm](/cortex/data-structures-and-algorithms/strings-z-algorithm) — same `O(n + m)` cost via a different mental model.
- **Generalisation:** [Aho-Corasick](/cortex/data-structures-and-algorithms/strings-aho-corasick) — KMP extended to *multiple* patterns simultaneously.

***

# Final takeaway

KMP is the canonical linear-time string matcher. Three patterns to internalise:

1. **The failure function is the algorithm's brain.** Once you've internalised "how much prefix is also suffix", every operation in KMP makes sense.
2. **Linear time via amortisation.** Both the failure-function build and the matching loop are `O(m)` and `O(n)` respectively because the `while` loops can only decrease `j` (or `k`), bounded by the total increments.
3. **Beats naive on patterns with internal repetition.** For random patterns, naive is usually fast enough. KMP's wins are on adversarial-shaped or repetitive patterns — common in DNA, log files, and compressed text.
