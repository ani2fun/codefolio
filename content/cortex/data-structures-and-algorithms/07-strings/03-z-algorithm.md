---
title: Z-Algorithm
summary: A sibling of KMP at the same O(n + m) cost via a different array. The Z-array tells you, for each position, the length of the longest substring starting there that matches a prefix of the string. One of the cleanest string-algorithm implementations.
prereqs:
  - strings-kmp
---

# 3. Z-Algorithm

## The Hook

KMP gives you `O(n + m)` matching via the failure function. The **Z-algorithm** gives you the same `O(n + m)` matching via a different precomputed array — the **Z-array** — which has independent uses beyond pattern matching.

For a string `S`, the Z-array `Z` is defined: `Z[i]` is the length of the longest substring starting at position `i` that matches a *prefix* of `S`. By convention `Z[0] = 0` (or sometimes `n`, depending on the variant).

To match a pattern `P` in text `T`, build the Z-array of the concatenated string `S = P + "$" + T` (using a separator `$` not in either). Any position `i > m` with `Z[i] = m` means the pattern starts at position `i - m - 1` in `T`. Done.

The implementation is shorter than KMP's — about 20 lines — and the algorithm has applications beyond matching: counting distinct substrings, periodic-string analysis, and a few competitive-programming standards.

---

## Table of contents

1. [The Z-array defined](#the-z-array-defined)
2. [Computing the Z-array](#computing-the-z-array)
3. [Pattern matching via Z-array](#pattern-matching-via-z-array)
4. [Implementation](#implementation)
5. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
6. [Production reality](#production-reality)
7. [Practice ladder](#practice-ladder)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The Z-array defined

For a string `S` of length `n`:

> `Z[i]` = length of the longest substring `S[i..i+k-1]` that equals `S[0..k-1]`.

Examples for `S = "aabcaabxaaaz"`:

```
i:    0  1  2  3  4  5  6  7  8  9  10 11
S[i]: a  a  b  c  a  a  b  x  a  a  a  z
Z:    0  1  0  0  3  1  0  0  2  2  1  0
```

- `Z[1] = 1`: `S[1] = 'a'` matches `S[0]`; `S[2] = 'b'` ≠ `S[1] = 'a'`.
- `Z[4] = 3`: `S[4..6] = "aab"` matches `S[0..2]`; `S[7] = 'x'` ≠ `S[3] = 'c'`.
- `Z[8] = 2`: `S[8..9] = "aa"` matches `S[0..1]`; `S[10] = 'a'` ≠ `S[2] = 'b'`.

`Z[0]` is undefined or set to `n` by convention (the string trivially matches itself).

***

# Computing the Z-array

The naive computation: for each `i`, compare character-by-character with the prefix until mismatch. `O(n²)` worst case. The Z-algorithm reduces this to `O(n)` by maintaining a *Z-box* — the rightmost `[l, r]` interval discovered so far that matches a prefix of `S`.

The cleverness is the `min(r - i, Z[i - l])` step. If `i` lies within the current Z-box `[l, r]`, then `S[i..r-1]` is a known prefix of `S` (specifically `S[i-l..r-l-1]`). So `Z[i]` is at least `min(r - i, Z[i - l])`. If `Z[i - l]` is small enough that the inferred match doesn't reach `r`, we can copy `Z[i - l]` exactly. Otherwise we need to verify by scanning — but only past `r`, which the amortisation argument shows happens linearly often.

**Cost.** `O(n)`. Each character of `S` is "examined for the first time" at most twice — once when extending `r`, once when used in a copied `Z[i - l]`.

***

# Pattern matching via Z-array

To find all occurrences of `P` in `T`:

1. Build `S = P + "$" + T` where `$` is a separator that doesn't appear in `P` or `T`.
2. Compute `Z` for `S`.
3. For each position `i` in the `T` part of `S`, if `Z[i] = m` (length of `P`), then `P` occurs at position `i - m - 1` in `T`.

`O(n + m)` total: `O(m + 1 + n)` to build the Z-array of `S`.

***

# Implementation

```python run viz=array viz-root=Z
def z_array(S):
    n = len(S)
    Z = [0] * n
    l, r = 0, 0
    for i in range(1, n):
        if i < r:
            Z[i] = min(r - i, Z[i - l])
        while i + Z[i] < n and S[Z[i]] == S[i + Z[i]]:
            Z[i] += 1
        if i + Z[i] > r:
            l, r = i, i + Z[i]
    return Z

def find_pattern(T, P):
    if not P: return list(range(len(T) + 1))
    sep = '$'
    S = P + sep + T
    Z = z_array(S)
    m = len(P)
    return [i - m - 1 for i in range(m + 1, len(S)) if Z[i] == m]


if __name__ == "__main__":
    print(f"Z('aabcaabxaaaz') = {z_array('aabcaabxaaaz')}")
    print(f"matches of 'ab' in 'ababab': {find_pattern('ababab', 'ab')}")     # [0, 2, 4]
    print(f"matches of 'aab' in 'aabaaaabaaab': {find_pattern('aabaaaabaaab', 'aab')}")  # [0, 4, 8]
```

```java run viz=array viz-root=Z
import java.util.*;

public class Main {
    static int[] zArray(String S) {
        int n = S.length();
        int[] Z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i < r) Z[i] = Math.min(r - i, Z[i - l]);
            while (i + Z[i] < n && S.charAt(Z[i]) == S.charAt(i + Z[i])) Z[i]++;
            if (i + Z[i] > r) { l = i; r = i + Z[i]; }
        }
        return Z;
    }

    static List<Integer> findPattern(String T, String P) {
        String S = P + "$" + T;
        int[] Z = zArray(S);
        int m = P.length();
        List<Integer> matches = new ArrayList<>();
        for (int i = m + 1; i < S.length(); i++) {
            if (Z[i] == m) matches.add(i - m - 1);
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(findPattern("ababab", "ab"));
    }
}
```

***

# Edge cases and pitfalls

- **Choosing the separator.** The `$` (or `#`, or `\0`) must *not* appear in either the pattern or the text. For binary strings or arbitrary alphabets, find a sentinel value outside the alphabet, or use a different boundary check.
- **`Z[0]` convention.** Most implementations set `Z[0] = 0` (the chapter's). Some set it to `n`. Both are fine if you're consistent; the matching algorithm doesn't read `Z[0]`.
- **The `min(r - i, Z[i - l])` step.** Easy to flip the inequality. `r` is *exclusive* in some references and *inclusive* in others; check before copying.
- **Comparing Z-array vs failure function.** Both are `O(n)` precomputations giving `O(n + m)` matching. Z-array tends to be slightly faster in practice (simpler inner loop), and the array has more standalone uses.

***

# Production reality

- **Competitive programming.** Z-array is the more popular tool than KMP because the implementation fits in a few lines and the array itself answers many queries (counting distinct substrings, finding longest common prefix of suffixes, etc.).
- **Suffix-array construction.** Some `O(n log n)` suffix-array algorithms use Z-array as a subroutine.
- **Periodic string analysis.** A string `S` of length `n` has period `p` iff `Z[p] + p ≥ n`. Used in pattern-detection in DNA (tandem repeats), audio analysis (period detection).
- **Less common in production code than KMP** — most language standard libraries use Boyer-Moore-Horspool, Two-Way, or KMP-like methods. Z-algorithm is more an *educational and research* tool.

***

# Practice ladder

1. **Implement Z-array.** Test on `"aabcaabxaaaz"` and verify against the chapter's table.
   > *Hint:* the chapter's algorithm; print the array and check term-by-term.

2. **Pattern matching via Z-array.** Implement and verify identical results to KMP on stress tests.
   > *Hint:* generate random strings and patterns; compare against `T.find(P)` repeated calls.

3. **Count distinct substrings.** Use Z-array to count distinct substrings of `S` in `O(n²)`.
   > *Hint:* for each suffix `S[i..]`, count substrings starting there that haven't appeared in any earlier suffix. Z-array on suffix concatenation gives the longest prefix of an earlier suffix matching this one.

4. **Find all periods of a string.** A *period* of `S` is a value `p` such that `S[i] = S[i + p]` for `0 ≤ i < n − p`.
   > *Hint:* `p` is a period iff `Z[p] + p ≥ n`. Iterate over `p` from 1 to `n - 1`.

5. **Shortest period of a string.** Find the smallest `p` such that `S` is a prefix of `(S[0..p-1])*`.
   > *Hint:* the answer is `n − Z[?]` where `?` is the position whose Z-value reaches the end. Or: `n − (longest border of S)`. Both are derivable from Z-array.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Z-array is the cleaner-looking sibling of KMP — once you've internalised the Z-box invariant, every prefix-or-period question reduces to one Z-array build.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> What does <code>Z[i]</code> represent?</summary>

**A:** The length of the longest substring starting at `i` that matches a *prefix* of `S`. By convention `Z[0] = 0`.

</details>
<details>
<summary><strong>Q:</strong> Time complexity of building Z-array?</summary>

**A:** `O(n)`. Amortised: the Z-box's right edge `r` only moves forward across the loop.

</details>
<details>
<summary><strong>Q:</strong> What's the Z-box?</summary>

**A:** The interval `[l, r]` representing the rightmost discovered substring matching a prefix of `S`. Maintained across the loop to reuse work.

</details>
<details>
<summary><strong>Q:</strong> How do you match pattern <code>P</code> in text <code>T</code> using Z-array?</summary>

**A:** Build Z-array for `S = P + '$' + T` (where `$` doesn't appear in either). Any position `i > m` with `Z[i] = m` is a match in `T` at offset `i - m - 1`.

</details>
<details>
<summary><strong>Q:</strong> Why does the separator <code>$</code> matter?</summary>

**A:** It prevents the Z-array from continuing the match past the pattern boundary into `T`. Must be a character not in `P` or `T`.

</details>
<details>
<summary><strong>Q:</strong> Smallest period of <code>S</code> via Z-array?</summary>

**A:** Smallest `p` such that `Z[p] + p ≥ n`. The whole string is then a prefix of `(S[0..p-1])^k`.

</details>
<details>
<summary><strong>Q:</strong> Z-array vs KMP failure function — equivalent or different?</summary>

**A:** Equivalent matching power, both `O(n + m)`. Different mental model. Z-array tends to be slightly faster in practice; KMP is more universally taught.

</details>
<details>
<summary><strong>Q:</strong> Compute Z for <code>"aabcaabxaaaz"</code>.</summary>

**A:** `[0, 1, 0, 0, 3, 1, 0, 0, 2, 2, 1, 0]`.

</details>

## Code template

```python
def z_array(S):
    n = len(S)
    Z = [0] * n
    l, r = 0, 0
    for i in range(1, n):
        if i < r:
            Z[i] = min(r - i, Z[i - l])              # reuse work inside the Z-box
        while i + Z[i] < n and S[Z[i]] == S[i + Z[i]]:
            Z[i] += 1
        if i + Z[i] > r:
            l, r = i, i + Z[i]                       # extend the Z-box
    return Z

# Pattern matching: build Z for P + "$" + T; any Z[i] == m means a match.
```

## Pattern triggers

- **"Find pattern in text"** → Z-array of `P + "$" + T`
- **"Period / shortest repeating prefix"** → Z-array; smallest `p` with `Z[p] + p ≥ n`
- **"Distinct substring counting"** → suffix array (Z is more limited here)
- **"Need both prefix-match and period info"** → one Z-build does both
- **"Sibling of KMP, different mental model"** → Z-array
- **"Implementation should fit in 15 lines"** → Z-array beats KMP on brevity

***

# Cross-links

- **Sibling:** [KMP](/cortex/data-structures-and-algorithms/strings-kmp) — equivalent matching power, different mental model.
- **Used by:** suffix-array construction, periodic-string analysis.

***

# Final takeaway

The Z-algorithm is the cleaner-looking sibling of KMP. Three patterns to internalise:

1. **Z-array is a useful object on its own.** Beyond pattern matching, it answers periodicity, distinct-substring counting, and prefix-suffix-overlap questions cleanly.
2. **The Z-box is the trick.** Maintain the rightmost confirmed prefix-match interval; reuse it via `min(r - i, Z[i - l])`. The amortisation gives `O(n)`.
3. **Pattern match via concatenation.** `S = P + "$" + T` reduces matching to a Z-array computation. Sentinel choice matters; pick a character not in either string.

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->
