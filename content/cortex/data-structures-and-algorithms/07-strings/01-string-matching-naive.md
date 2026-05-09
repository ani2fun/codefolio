---
title: Naive String Matching
summary: The O(nm) baseline every string algorithm beats. Worth implementing once, both to establish the floor and to feel why the smarter algorithms matter.
prereqs:
  - linear-structures-arrays-introduction
---

# 1. Naive String Matching

## The Hook

You have a text `T` of length `n` and a pattern `P` of length `m`. Find every position in `T` where `P` matches. The obvious algorithm: try every position; at each, compare character-by-character until you mismatch or finish.

```pseudocode
function naiveMatch(T, P):
    n ← length(T); m ← length(P)
    matches ← []
    for i from 0 to n − m:
        j ← 0
        while j < m AND T[i + j] = P[j]:
            j ← j + 1
        if j = m:
            matches.append(i)
    return matches
```

`O(nm)` worst case (every character of `P` matches every character of `T` until the last one). For `n = m = 10⁶`, that's `10¹²` operations — *eleven days* on a fast laptop.

The good news: for random text, average behaviour is `O(n + m)` because mismatches happen quickly. The bad news: production text isn't random — long repeats of single characters or near-matches are common (DNA, log files, source code), and the worst case bites in practice.

The rest of the [Strings module](/cortex/data-structures-and-algorithms/strings-index) is about beating the `O(nm)` worst case. Naive matching is the floor. This chapter is short — there isn't much to say beyond "this is the baseline" — but it's worth implementing once so the algorithms that follow have something to be measured against.

***

## Implementation

```python run
def naive_match(T, P):
    n, m = len(T), len(P)
    matches = []
    for i in range(n - m + 1):
        j = 0
        while j < m and T[i + j] == P[j]:
            j += 1
        if j == m:
            matches.append(i)
    return matches


if __name__ == "__main__":
    T = "ababcababcabc"
    P = "abc"
    print(f"matches at: {naive_match(T, P)}")
    # Worst-case adversarial input
    T = "a" * 100 + "b"
    P = "a" * 50 + "b"
    print(f"adversarial matches: {naive_match(T, P)}")
```

```java run
import java.util.*;

class Solution {
    static List<Integer> naiveMatch(String T, String P) {
        int n = T.length(), m = P.length();
        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m && T.charAt(i + j) == P.charAt(j)) j++;
            if (j == m) matches.add(i);
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(naiveMatch("ababcababcabc", "abc"));
    }
}
```

```c run
#include <stdio.h>
#include <string.h>

void naive_match(const char *T, const char *P) {
    int n = strlen(T), m = strlen(P);
    for (int i = 0; i <= n - m; i++) {
        int j = 0;
        while (j < m && T[i + j] == P[j]) j++;
        if (j == m) printf("%d ", i);
    }
    printf("\n");
}

int main(void) {
    naive_match("ababcababcabc", "abc");
    return 0;
}
```

```scala run
object Solution {
  def naiveMatch(T: String, P: String): List[Int] = {
    val n = T.length; val m = P.length
    val out = scala.collection.mutable.ListBuffer.empty[Int]
    for (i <- 0 to n - m) {
      var j = 0
      while (j < m && T(i + j) == P(j)) j += 1
      if (j == m) out += i
    }
    out.toList
  }

  def main(args: Array[String]): Unit = println(naiveMatch("ababcababcabc", "abc"))
}
```

***

## Edge cases

- **Pattern longer than text.** `n - m + 1 ≤ 0`, so the loop doesn't execute. No matches. Correct.
- **Empty pattern.** Conventionally matches at every position; the loop returns `[0, 1, …, n]`.
- **Multi-byte characters.** Naive matching by *bytes* fails on UTF-8 — use *codepoints* (Python's `for ch in s` iterates codepoints; in Java/C use `String` directly or normalize first).

## Production reality

- **Tiny patterns / tiny texts.** A search-and-replace in a 100-line config file is fine with naive — the constant factor wins.
- **Most language standard libraries** (`str.find` in Python, `String.indexOf` in Java) use a *hybrid* — naive for small patterns, switch to faster algorithms (Boyer-Moore, Two-Way) for larger ones.
- **`grep` and `ripgrep`** start with Boyer-Moore-Horspool (a simple acceleration of naive) and fall back to other methods for regex patterns. Pure naive matching is rarely shipped as the *only* algorithm.

## Cross-links

- **Next:** [KMP](/cortex/data-structures-and-algorithms/strings-kmp) — `O(n + m)` via the failure function.
- **Sibling:** [Z-Algorithm](/cortex/data-structures-and-algorithms/strings-z-algorithm) — different mental model, same `O(n + m)` cost.

## Final takeaway

Naive matching is the floor. Three patterns to internalise:

1. **`O(nm)` is bad enough to demand attention.** A million-character text and a thousand-character pattern is one of the most common shapes; naive at `10⁹` ops is borderline.
2. **The next chapters all beat this in worst case.** KMP, Z, Rabin-Karp all hit `O(n + m)` or close.
3. **Implement once; measure against the smarter algorithms.** A benchmark with naive as baseline makes the speedup of the better algorithms concrete.
