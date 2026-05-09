---
title: Rabin-Karp and Rolling Hash
summary: Hash the pattern; slide a window over the text and hash each position. With the rolling-hash trick, each window-shift is O(1). Probabilistic O(n + m) average; the trick behind plagiarism detection and substring caching.
prereqs:
  - strings-string-matching-naive
  - linear-structures-hash-table-introduction-to-hash-tables
---

# 4. Rabin-Karp and Rolling Hash

## The Hook

The naive substring comparison is `O(m)` per text position — you compare every character. The trick of Rabin-Karp: don't compare characters, **compare hashes**. Hash the pattern once. As you slide a window of length `m` over the text, maintain a rolling hash of the window. At each position, compare the window's hash against the pattern's. If equal, *verify* with a character-by-character comparison (because hash collisions exist). If unequal, the substring is definitely not a match — skip in `O(1)`.

The win is the **rolling hash**: updating the window's hash from position `i` to position `i + 1` doesn't require rehashing all `m` characters. With a polynomial hash function, you remove the contribution of the leaving character and add the contribution of the entering character — both `O(1)`.

For random text, average time is `O(n + m)`. Worst case (adversarial collisions) is `O(nm)`, same as naive. In practice, with a good hash and a large prime modulus, the probability of any false match is around `m / p` (where `p` is the modulus, often `10⁹+`) — vanishingly small.

This chapter covers the rolling hash, the algorithm, and where Rabin-Karp shines (multiple-pattern matching, plagiarism detection, fingerprinting).

---

## Table of contents

1. [Polynomial hashing](#polynomial-hashing)
2. [The rolling-hash trick](#the-rolling-hash-trick)
3. [The Rabin-Karp algorithm](#the-rabin-karp-algorithm)
4. [Implementation](#implementation)
5. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
6. [Production reality](#production-reality)
7. [Practice ladder](#practice-ladder)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# Polynomial Hashing

A **polynomial hash** of a string `S = s_0 s_1 … s_{m-1}` is:

```
h(S) = (s_0 · b^(m-1) + s_1 · b^(m-2) + … + s_{m-1} · b^0)  mod p
```

where `b` is a base (often 31, 53, or 257) and `p` is a large prime (often `10⁹+7` or `2⁶¹ − 1`).

Two practical considerations:

- **Base.** Pick `b` larger than the alphabet so different characters hash to different values. For ASCII (256 chars), `b ≥ 257`. For lowercase only (26 chars), `b ≥ 27`.
- **Modulus.** Pick a prime large enough that collisions are rare. `10⁹ + 7` is the competitive-programming default. For higher security, use `2⁶¹ − 1` (a Mersenne prime) or *two* hashes with different primes (drastically reduces collision chance).

***

# The rolling-hash trick

Window from position `i` to `i + m - 1` hashes to:

```
h_i = s_i · b^(m-1) + s_{i+1} · b^(m-2) + … + s_{i+m-1} · b^0
```

Window from position `i + 1` to `i + m`:

```
h_{i+1} = s_{i+1} · b^(m-1) + s_{i+2} · b^(m-2) + … + s_{i+m} · b^0
       = (h_i − s_i · b^(m-1)) · b + s_{i+m}
```

Subtract the leaving character (multiplied by `b^(m-1)` to align), shift left by `b`, add the new character. `O(1)` arithmetic operations (with modular reduction).

***

# The Rabin-Karp algorithm

```pseudocode
function rabinKarp(T, P):
    n ← length(T); m ← length(P)
    if m > n: return []
    b ← 257; p ← 10^9 + 7
    pHash ← 0; tHash ← 0; bm ← 1
    for i from 0 to m − 1:
        pHash ← (pHash · b + P[i]) mod p
        tHash ← (tHash · b + T[i]) mod p
        if i < m − 1: bm ← (bm · b) mod p              # b^(m-1) mod p

    matches ← []
    for i from 0 to n − m:
        if pHash = tHash:
            if T[i..i + m − 1] = P:                    # verify against false collision
                matches.append(i)
        if i < n − m:
            tHash ← ((tHash − T[i] · bm) · b + T[i + m]) mod p
            if tHash < 0: tHash ← tHash + p
    return matches
```

**Average cost.** `O(n + m)`. **Worst case.** `O(nm)` (every hash collides; verify always runs). In practice with a 10⁹ prime, false collisions are vanishingly rare.

***

# Implementation

```python run
def rabin_karp(T, P):
    n, m = len(T), len(P)
    if m > n: return []
    if m == 0: return list(range(n + 1))

    b = 257
    p = 10**9 + 7

    p_hash = 0; t_hash = 0; bm = 1
    for i in range(m):
        p_hash = (p_hash * b + ord(P[i])) % p
        t_hash = (t_hash * b + ord(T[i])) % p
        if i < m - 1:
            bm = (bm * b) % p

    matches = []
    for i in range(n - m + 1):
        if p_hash == t_hash and T[i:i + m] == P:               # verify
            matches.append(i)
        if i < n - m:
            t_hash = ((t_hash - ord(T[i]) * bm) * b + ord(T[i + m])) % p
            if t_hash < 0:
                t_hash += p
    return matches


if __name__ == "__main__":
    print(f"matches: {rabin_karp('ababcababcabcabc', 'abc')}")
    print(f"matches: {rabin_karp('AABAACAADAABAABA', 'AABA')}")
    # Stress test against naive
    import random
    random.seed(7)
    for _ in range(100):
        T = ''.join(random.choices('abc', k=200))
        P = ''.join(random.choices('abc', k=5))
        rk = rabin_karp(T, P)
        # naive reference
        naive = [i for i in range(len(T) - len(P) + 1) if T[i:i + len(P)] == P]
        assert rk == naive, f"mismatch on T={T!r}, P={P!r}: rk={rk}, naive={naive}"
    print("100 random tests pass")
```

```java run
import java.util.*;

class Solution {
    static List<Integer> rabinKarp(String T, String P) {
        int n = T.length(), m = P.length();
        List<Integer> matches = new ArrayList<>();
        if (m > n) return matches;

        long b = 257, mod = 1_000_000_007L;
        long pHash = 0, tHash = 0, bm = 1;
        for (int i = 0; i < m; i++) {
            pHash = (pHash * b + P.charAt(i)) % mod;
            tHash = (tHash * b + T.charAt(i)) % mod;
            if (i < m - 1) bm = (bm * b) % mod;
        }

        for (int i = 0; i <= n - m; i++) {
            if (pHash == tHash && T.regionMatches(i, P, 0, m)) matches.add(i);
            if (i < n - m) {
                tHash = ((tHash - T.charAt(i) * bm) * b + T.charAt(i + m)) % mod;
                if (tHash < 0) tHash += mod;
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(rabinKarp("ababcababcabcabc", "abc"));
    }
}
```

```c run
#include <stdio.h>
#include <string.h>

void rabin_karp(const char *T, const char *P) {
    int n = strlen(T), m = strlen(P);
    if (m > n) return;
    long long b = 257, mod = 1000000007LL;
    long long p_hash = 0, t_hash = 0, bm = 1;
    for (int i = 0; i < m; i++) {
        p_hash = (p_hash * b + P[i]) % mod;
        t_hash = (t_hash * b + T[i]) % mod;
        if (i < m - 1) bm = (bm * b) % mod;
    }
    for (int i = 0; i <= n - m; i++) {
        if (p_hash == t_hash && memcmp(T + i, P, m) == 0) printf("%d ", i);
        if (i < n - m) {
            t_hash = ((t_hash - T[i] * bm) * b + T[i + m]) % mod;
            if (t_hash < 0) t_hash += mod;
        }
    }
    printf("\n");
}

int main(void) {
    rabin_karp("ababcababcabcabc", "abc");
    return 0;
}
```

```scala run
object Solution {
  def rabinKarp(T: String, P: String): List[Int] = {
    val n = T.length; val m = P.length
    if (m > n) return Nil
    val b = 257L; val mod = 1000000007L
    var pHash = 0L; var tHash = 0L; var bm = 1L
    for (i <- 0 until m) {
      pHash = (pHash * b + T(0)) % mod                                                          // initialised below
    }
    pHash = 0; tHash = 0
    for (i <- 0 until m) {
      pHash = (pHash * b + P(i).toLong) % mod
      tHash = (tHash * b + T(i).toLong) % mod
      if (i < m - 1) bm = (bm * b) % mod
    }
    val matches = scala.collection.mutable.ListBuffer.empty[Int]
    for (i <- 0 to n - m) {
      if (pHash == tHash && T.substring(i, i + m) == P) matches += i
      if (i < n - m) {
        tHash = ((tHash - T(i).toLong * bm) * b + T(i + m).toLong) % mod
        if (tHash < 0) tHash += mod
      }
    }
    matches.toList
  }

  def main(args: Array[String]): Unit = println(rabinKarp("ababcababcabcabc", "abc"))
}
```

***

# Edge cases and pitfalls

- **Negative modular arithmetic.** Subtracting can produce negative values. Add `mod` after the subtraction to renormalise; otherwise the hash drift is wrong.
- **Modular overflow.** `tHash * b` can overflow 64-bit if not careful with the modulus. Use `__int128` in C++ for very-large primes, or split the modulus.
- **Single hash is collision-prone for adversarial input.** A determined attacker can find collisions for any single fixed prime. Use *double hashing* (two distinct `(b, p)` pairs) for adversarial settings — collision probability becomes `(m / p)²`.
- **Empty pattern.** Conventionally matches every position. Handle as a separate case to avoid divide-by-zero or empty-loop weirdness.
- **The `bm` precompute.** `bm = b^(m-1) mod p`. Off-by-one: if you use `bm = b^m`, the rolling-hash subtraction is wrong.
- **Choice of `p` and `b`.** Standard combinations: `(b=31, p=10⁹+9)`, `(b=53, p=998244353)`, `(b=257, p=10⁹+7)`. For two-hash setups, use two of these.

***

# Production reality

- **Plagiarism detection.** MOSS (the academic plagiarism checker), TurnItIn, and similar tools fingerprint documents using rolling hashes. Each document's fingerprint is the set of "winning" hashes from rolling windows. Two documents with overlapping fingerprints are flagged for further inspection.
- **Git's content-defined chunking** uses rolling hashes (FastCDC) to split files into variable-sized chunks. The same chunk recurring across files (e.g., a header in many source files) hashes the same way, allowing deduplication.
- **rsync.** The `rsync` algorithm uses rolling hashes (the Adler-32 checksum) to identify which blocks of a file have changed and need transmission, avoiding sending the whole file.
- **Substring caching.** A web framework cacheing fragments of HTML can use rolling hashes to identify "the user just edited the second of seven sections; the other six are unchanged". Saves recompilation work.
- **Bioinformatics.** Sliding window analysis of DNA sequences (counting GC content, finding tandem repeats) uses rolling hashes to summarise window contents.
- **`std::hash` is not a rolling hash** — the C++ STL's hash is for whole strings, not for window-sliding. Implementations of Rabin-Karp ship their own hash function.

***

# Practice ladder

1. **Implement Rabin-Karp.** Stress-test against naive on random inputs.
   > *Hint:* the chapter's algorithm. Pay attention to the rolling-update arithmetic.

2. **Repeated DNA Sequences** ([LeetCode 187](https://leetcode.com/problems/repeated-dna-sequences/)) — find all 10-character substrings of a DNA string that occur more than once.
   > *Hint:* rolling hash over windows of size 10. Use a hash map to track which hashes have been seen.

3. **Longest Duplicate Substring** ([LeetCode 1044](https://leetcode.com/problems/longest-duplicate-substring/)) — find the longest substring of `s` that appears at least twice.
   > *Hint:* binary search on the length `L`. For each `L`, use rolling-hash to find any duplicate substring of length `L` in `O(n)`. Total `O(n log n)`.

4. **Distinct Substrings.** Count the number of distinct substrings of a string in `O(n²)`.
   > *Hint:* for each starting position, generate all rolling hashes; insert into a hash set. (Suffix arrays/automata give `O(n)` solutions; rolling-hash is the simpler `O(n²)` baseline.)

5. **Find First Repeated Substring of Length K.** Given `s` and `k`, find any substring of length `k` that appears twice.
   > *Hint:* exactly the rolling-hash + hash-set pattern from LeetCode 187, generalised.

***

# Cross-links

- **Prerequisites:** [Naive String Matching](/cortex/data-structures-and-algorithms/strings-string-matching-naive), [Hash Table](/cortex/data-structures-and-algorithms/linear-structures-hash-table-introduction-to-hash-tables).
- **Sibling:** [KMP](/cortex/data-structures-and-algorithms/strings-kmp), [Z-Algorithm](/cortex/data-structures-and-algorithms/strings-z-algorithm).
- **Production deep-dive:** [Git's Merkle DAG](/cortex/data-structures-and-algorithms/dsa-in-real-systems-git-merkle-dag) — *stub* — Git's chunking uses rolling hash.

***

# Final takeaway

Rabin-Karp reduces string matching to *hash comparison*. Three patterns to internalise:

1. **Hash compare, then verify.** A hash match is a *candidate* match, not a confirmed one. Always verify byte-by-byte after a hash hit.
2. **Rolling hash makes the slide O(1).** The polynomial-hash recurrence `h_{i+1} = (h_i − leave) · b + enter` is the entire optimisation.
3. **Best for multiple patterns and fingerprinting.** Pure single-pattern matching is usually faster with KMP. Rabin-Karp wins when you're computing many hashes (multiple patterns, document fingerprints, deduplication chunks).
