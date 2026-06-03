---
title: Suffix Array
summary: An array of every suffix of a string, sorted lexicographically. The compact alternative to a suffix tree — supports substring search, longest-common-substring, and many string-algorithm primitives in O(log n) per query after O(n log n) construction.
prereqs:
  - strings-string-matching-naive
  - sorting-and-searching-searching-binary-search
---

# 6. Suffix Array

## The Hook

A **suffix array** of a string `S` is an array `SA` where `SA[i]` is the starting index of the `i`-th lexicographically smallest suffix of `S`. For `S = "banana"`, the suffixes are:

```
0: "banana"        →  6: "" (empty, sometimes excluded)
1: "anana"         →  5: "a"
2: "nana"          →  3: "ana"
3: "ana"           →  1: "anana"
4: "na"            →  0: "banana"
5: "a"             →  4: "na"
6: ""              →  2: "nana"
```

Sorted, the suffix array is `SA = [5, 3, 1, 0, 4, 2]` (omitting the empty suffix).

The suffix array is the **compact alternative to the suffix tree**. Suffix trees are linear in size but with a large constant factor (10×–20× the original string in memory). Suffix arrays are exactly `n` integers, plus an auxiliary `LCP` (Longest Common Prefix) array of `n` integers — total `~8n` bytes for 32-bit ints.

With a suffix array, you can:

- **Search for a pattern `P` in `O(m log n)`** via binary search.
- **Count distinct substrings** in `O(n)` after a one-time `O(n log n)` setup.
- **Find longest common substring of two strings** in `O(n)`.
- **Find the longest repeated substring** in `O(n)`.

---

## Table of contents

1. [Construction](#construction)
2. [The LCP array](#the-lcp-array)
3. [Pattern search via binary search](#pattern-search-via-binary-search)
4. [Implementation](#implementation)
5. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
6. [Production reality](#production-reality)
7. [Cross-links](#cross-links)
8. [Final takeaway](#final-takeaway)

***

# Construction

The naive approach: list all suffixes, sort. Sorting takes `O(n log n)` comparisons, each comparison up to `O(n)` chars — total `O(n² log n)`. Bad.

The standard fast construction is the **prefix-doubling algorithm** (Manber-Myers, 1993) running in `O(n log² n)` or `O(n log n)` with radix-sort tricks. The intuition: after sorting suffixes by their first character, you have ranks. After sorting by first two characters, you have new ranks. Doubling at each step, after `log n` rounds you've sorted by all `n` characters.

**SA-IS** (DC3, Suffix Array Induced Sorting, 2009) achieves `O(n)` construction. It's complex enough that competitive programmers usually implement prefix-doubling for clarity and accept the extra `log` factor.

For the chapter implementation, I'll use the simplest correct version — Python's built-in sort — for clarity. Production should use a library (`pysuffix`, `SDSL` in C++) for serious work.

***

# The LCP array

The **Longest Common Prefix array** `LCP[i]` stores the length of the longest common prefix between adjacent suffixes in `SA`:

```
LCP[i] = lcp(SA[i - 1], SA[i])
```

For `S = "banana"` with `SA = [5, 3, 1, 0, 4, 2]`:

```
SA[0] = 5: "a"
SA[1] = 3: "ana"        LCP[1] = 1   ("a")
SA[2] = 1: "anana"      LCP[2] = 3   ("ana")
SA[3] = 0: "banana"     LCP[3] = 0
SA[4] = 4: "na"         LCP[4] = 0
SA[5] = 2: "nana"       LCP[5] = 2   ("na")
```

The LCP array enables: longest repeated substring (max of LCP), distinct substring counting (`Σ (n - SA[i] - LCP[i])`), and many other queries.

**Kasai's algorithm** computes LCP in `O(n)` given SA. It's a beautiful linear-time algorithm worth knowing.

***

# Pattern search via binary search

To find pattern `P` in `S`:

1. Binary-search the SA for the first suffix that starts with `P`.
2. Binary-search for the last such suffix.
3. The matches are `SA[lo..hi]`.

Each binary search comparison takes `O(m)` (compare `P` to `S[SA[mid]..SA[mid] + m]`). Total: `O(m log n)` per pattern. Better than naive's `O(nm)`.

***

# Implementation

```python run viz=array viz-root=lcp
def build_sa(s):
    """O(n² log n) naive construction — fine for educational use."""
    n = len(s)
    return sorted(range(n), key=lambda i: s[i:])

def build_lcp(s, sa):
    """Kasai's O(n) algorithm."""
    n = len(s)
    rank = [0] * n
    for i, idx in enumerate(sa):
        rank[idx] = i
    lcp = [0] * n
    k = 0
    for i in range(n):
        if rank[i] == 0:
            k = 0
            continue
        j = sa[rank[i] - 1]
        while i + k < n and j + k < n and s[i + k] == s[j + k]:
            k += 1
        lcp[rank[i]] = k
        if k > 0:
            k -= 1
    return lcp

def search(s, sa, p):
    """Find all occurrences of p in s using binary search on the suffix array."""
    n, m = len(s), len(p)
    # Binary search for the first suffix >= p
    lo, hi = 0, n
    while lo < hi:
        mid = (lo + hi) // 2
        if s[sa[mid]:sa[mid] + m] < p:
            lo = mid + 1
        else:
            hi = mid
    start = lo
    # Binary search for first suffix > p
    lo, hi = start, n
    while lo < hi:
        mid = (lo + hi) // 2
        if s[sa[mid]:sa[mid] + m] <= p:
            lo = mid + 1
        else:
            hi = mid
    return sorted(sa[start:lo])


if __name__ == "__main__":
    s = "banana"
    sa = build_sa(s)
    lcp = build_lcp(s, sa)
    print(f"S = {s!r}")
    print(f"SA = {sa}")
    print(f"Suffixes in sorted order:")
    for i, idx in enumerate(sa):
        print(f"  SA[{i}]={idx}: {s[idx:]!r}, LCP={lcp[i]}")

    print(f"\nMatches for 'ana': {search(s, sa, 'ana')}")        # [1, 3]
    print(f"Matches for 'na': {search(s, sa, 'na')}")            # [2, 4]
    print(f"Matches for 'xy': {search(s, sa, 'xy')}")            # []

    # Longest repeated substring is the max LCP value
    max_lcp_i = max(range(len(lcp)), key=lambda i: lcp[i])
    if lcp[max_lcp_i] > 0:
        print(f"\nLongest repeated substring: {s[sa[max_lcp_i]:sa[max_lcp_i] + lcp[max_lcp_i]]!r}")
```

```java run viz=array viz-root=lcp
import java.util.*;

public class Main {
    static int[] buildSA(String s) {
        int n = s.length();
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, (a, b) -> s.substring(a).compareTo(s.substring(b)));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) sa[i] = indices[i];
        return sa;
    }

    static int[] buildLCP(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        int[] lcp = new int[n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) { k = 0; continue; }
            int j = sa[rank[i] - 1];
            while (i + k < n && j + k < n && s.charAt(i + k) == s.charAt(j + k)) k++;
            lcp[rank[i]] = k;
            if (k > 0) k--;
        }
        return lcp;
    }

    public static void main(String[] args) {
        String s = "banana";
        int[] sa = buildSA(s);
        int[] lcp = buildLCP(s, sa);
        System.out.println("SA = " + Arrays.toString(sa));
        System.out.println("LCP = " + Arrays.toString(lcp));
    }
}
```

***

# Edge cases and pitfalls

- **Empty suffix.** Some constructions include the empty suffix (giving `n + 1` entries); others don't. Be consistent.
- **Equal characters.** Strings like `"aaaa"` produce a degenerate SA where successive suffixes share most of their content. The `O(n²)` naive build hits its worst case here. SA-IS or DC3 handles it in `O(n)`.
- **The `max(LCP)` trick for longest repeated substring** doesn't *always* give a non-overlapping repeat. For overlapping repeats (e.g., `"aaaa"` has the longest repeated substring `"aaa"`, overlapping itself), you may need extra checks.
- **Binary search in `search()` is `O(m log n)` only with naive comparison.** With the LCP array and a more sophisticated approach, it becomes `O(m + log n)`.

***

# Production reality

- **Bioinformatics.** Whole-genome alignment tools (Bowtie, BWA) use suffix arrays (often the FM-index, a compressed variant) for fast read alignment. Genomic search of millions of short reads against a 3-billion-character reference is the canonical workload.
- **Information retrieval.** Some search engines use suffix arrays for substring queries on documents that don't fit the inverted-index model.
- **Compression algorithms.** The Burrows-Wheeler Transform (used in `bzip2`) is built on suffix arrays.
- **Plagiarism detection.** Suffix arrays of two documents allow finding all common substrings in `O(n)` total — much faster than naive pairwise comparison.
- **Standard libraries.** No mainstream language stdlib ships a suffix array. Production code uses libraries: `SDSL` (C++), `pysuffix3` (Python), `sufdsk` (Rust). For competitive programming, Yuhei Kitanaka and others publish well-tested templates.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Suffix array + LCP is the dense representation of "every substring of S"; once you have it, a dozen string queries reduce to one-pass scans.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> What's <code>SA[i]</code>?</summary>

**A:** The starting index of the `i`-th lexicographically smallest suffix of `S`.

</details>
<details>
<summary><strong>Q:</strong> Time complexity of suffix-array construction (best practical algorithm)?</summary>

**A:** `O(n log n)` (prefix doubling) or `O(n)` (SA-IS / DC3). Naive sort-of-suffixes is `O(n² log n)` and only good for small inputs.

</details>
<details>
<summary><strong>Q:</strong> What's <code>LCP[i]</code>?</summary>

**A:** Longest common prefix length between `SA[i-1]` and `SA[i]` — adjacent suffixes in lex order.

</details>
<details>
<summary><strong>Q:</strong> Time to build LCP given SA?</summary>

**A:** `O(n)` via Kasai's algorithm.

</details>
<details>
<summary><strong>Q:</strong> Pattern-search complexity using SA?</summary>

**A:** `O(m log n)` via binary search on SA. With LCP-aware speedup: `O(m + log n)`.

</details>
<details>
<summary><strong>Q:</strong> Longest repeated substring via LCP?</summary>

**A:** `max(LCP[i])` — the largest LCP value gives the longest substring shared by two suffixes.

</details>
<details>
<summary><strong>Q:</strong> Distinct substrings count via SA + LCP?</summary>

**A:** `Σ (n − SA[i] − LCP[i])` over all `i`. `n − SA[i]` is suffix length; `LCP[i]` is what was already counted.

</details>
<details>
<summary><strong>Q:</strong> Why is the FM-index more popular than raw suffix array in bioinformatics?</summary>

**A:** FM-index compresses the SA + auxiliary structures using BWT, fitting genomes in a fraction of the memory while retaining `O(m)` substring-count queries.

</details>

## Code template

```python
def build_sa(s):                                  # naive O(n² log n) — fine for educational use
    n = len(s)
    return sorted(range(n), key=lambda i: s[i:])

def build_lcp(s, sa):                             # Kasai's O(n)
    n = len(s)
    rank = [0] * n
    for i, idx in enumerate(sa): rank[idx] = i
    lcp = [0] * n
    k = 0
    for i in range(n):
        if rank[i] == 0: k = 0; continue
        j = sa[rank[i] - 1]
        while i + k < n and j + k < n and s[i + k] == s[j + k]: k += 1
        lcp[rank[i]] = k
        if k > 0: k -= 1
    return lcp
```

## Pattern triggers

- **"Pattern matching against a fixed text, many queries"** → suffix array binary search
- **"Longest repeated substring"** → SA + LCP, take max LCP
- **"Count distinct substrings"** → SA + LCP, sum `(n - SA[i] - LCP[i])`
- **"Longest common substring of two strings"** → concatenate with separator; SA + LCP
- **"Burrows-Wheeler Transform / bzip2"** → SA construction is the prerequisite
- **"DNA / genome alignment"** → FM-index (compressed SA variant)
- **"Distinct substrings in a string"** → SA + LCP
- **"K-th lexicographically smallest substring"** → walk SA in order, count substrings as you go

***

# Cross-links

- **Prerequisites:** [Naive String Matching](/cortex/data-structures-and-algorithms/strings-string-matching-naive), [Binary Search](/cortex/data-structures-and-algorithms/sorting-and-searching-searching-binary-search).
- **Sibling structure:** [Suffix Automaton](/cortex/data-structures-and-algorithms/strings-suffix-automaton) — different representation of the same suffix universe.
- **Generalisation:** [Aho-Corasick](/cortex/data-structures-and-algorithms/strings-aho-corasick) — multi-pattern matching via a trie with failure links.

***

# Final takeaway

The suffix array is the compact representation of every suffix of a string. Three patterns to internalise:

1. **Sorted suffixes are surprisingly powerful.** Substring search, longest repeated substring, distinct substring counting — all reduce to scans over SA + LCP.
2. **The LCP array is the secret sauce.** Without LCP, SA is just a sorted list. With LCP, it answers a dozen string queries in linear time.
3. **Production-grade construction is non-trivial.** Naive `O(n² log n)` is fine for educational use. Real systems use SA-IS, DC3, or library implementations to hit `O(n)` or `O(n log n)` on multi-million-character inputs.

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

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->
