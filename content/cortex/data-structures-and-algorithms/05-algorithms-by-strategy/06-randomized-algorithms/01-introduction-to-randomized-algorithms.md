---
title: Introduction to Randomized Algorithms
summary: Use randomness as a primitive. Monte Carlo (might be wrong, fast), Las Vegas (always right, expected fast). Randomized quicksort, randomized quickselect, reservoir sampling, and the trick that makes hashing safe against adversaries.
prereqs:
  - foundations-asymptotic-analysis
  - foundations-amortized-analysis
---

# 1. Introduction to Randomized Algorithms

## The Hook

The textbook quicksort has a worst case of `O(n²)` — adversarial inputs (already-sorted, reverse-sorted, or all-equal) collapse the recursion tree into a chain. The fix isn't a fancier algorithm; it's a **coin flip**. Pick the pivot *at random* instead of at the start of the array. Now there's no adversarial input — the adversary doesn't know which pivot you'll pick. Expected time becomes `O(n log n)`. Worst case is still theoretically `O(n²)`, but the probability of hitting it on a million elements is roughly `2^(-1000)` — astronomically smaller than the chance of hardware failure.

This is the deal randomised algorithms offer: *trade worst-case guarantees for expected-case performance, by making the algorithm's behaviour depend on coin flips you control*. When the deal is favourable, you get simpler algorithms with better practical performance — randomised quicksort, randomised quickselect, hash tables with random seeds, treaps, skip lists, primality testing, randomised MIN-CUT.

This chapter covers the mental model (Monte Carlo vs Las Vegas), three canonical algorithms, and the trick that makes most hash tables safe against denial-of-service attacks.

---

## Table of contents

1. [Monte Carlo vs Las Vegas](#monte-carlo-vs-las-vegas)
2. [Randomized quicksort](#randomized-quicksort)
3. [Randomized quickselect](#randomized-quickselect)
4. [Reservoir sampling](#reservoir-sampling)
5. [Why random hashing prevents HashDoS](#why-random-hashing-prevents-hashdos)
6. [Implementation](#implementation)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Production reality](#production-reality)
9. [Practice ladder](#practice-ladder)
10. [Cross-links](#cross-links)
11. [Final takeaway](#final-takeaway)

***

# Monte Carlo vs Las Vegas

Two classes of randomised algorithm:

- **Monte Carlo.** Always finishes in bounded time, but the answer might be *wrong* with some bounded probability. Trade-off: faster, occasionally incorrect. *Examples:* Miller-Rabin primality test (probability of false positive is `< 4^-k` for `k` rounds), Bloom filters (false positives possible).
- **Las Vegas.** Always finishes with the *correct* answer, but the *running time* is a random variable. Expected time is bounded; worst case might be unbounded. *Examples:* randomised quicksort, randomised quickselect, treaps.

In practice, you tune Monte Carlo's error probability low enough to be irrelevant (run 50 rounds of Miller-Rabin → false positive rate `< 4^-50 ≈ 10^-30` — your hardware fails first). Las Vegas's worst-case time is similarly tuned away by the law of large numbers — `O(n log n)` *expected* with exponentially-tail-bounded variance is the same as `O(n log n)` for any practical purpose.

***

# Randomized Quicksort

Standard quicksort with one change: *pick the pivot uniformly at random* from the subarray.

**Expected time.** `O(n log n)`. The proof: each comparison happens between two specific elements `A[i] < A[j]` only if one of them is chosen as a pivot before any element in `(A[i], A[j])` is. The probability is `2 / (j - i + 1)`. Summing over all pairs gives an expected `O(n log n)` total comparisons.

**Worst-case time.** `O(n²)` (the same pathological cases as deterministic quicksort), but the probability of hitting them with `n = 10⁶` is essentially zero.

The same trick fixes hash tables (random hash seeds), treaps (random priorities), skip lists (random heights), and most other "deterministic version has a worst-case adversarial input" structures.

***

# Randomized Quickselect

Find the `k`-th smallest element of an array in *expected* `O(n)` time. (Deterministically `O(n)` is possible via the median-of-medians algorithm, but the constant factor is so much worse that randomised is the production choice.)

**Expected time.** `O(n)`. The recurrence (informally): half the time the pivot lands in the "good" middle 50%, so the subarray shrinks by at least 25%. The expected size shrinks by a constant factor each call, giving `O(n)` total work. Formal analysis: `T(n) ≤ T(3n/4) + n` in expectation, gives `T(n) = O(n)`.

Used in `nth_element` in C++ STL, and in Python's `heapq.nsmallest`/`nlargest` for some sizes.

***

# Reservoir Sampling

You're streaming a sequence of items, one at a time, and don't know how long the stream is. You want to pick **a uniform random sample of `k` items**.

**Algorithm (reservoir sampling, k = 1):** keep the first item. For each subsequent item `x_i` (1-indexed), replace the current sample with `x_i` with probability `1/i`. After processing `n` items, every item has probability `1/n` of being the sample.

**Algorithm (k items):** keep the first `k` items. For each subsequent item `x_i`, generate a random integer `j` in `[1, i]`. If `j ≤ k`, replace `sample[j-1]` with `x_i`. After processing, every item has probability `k/n`.

**Cost.** `O(n)` time, `O(k)` space. The space is the magic: we never have to know `n` in advance, and we never have to store more than `k` items.

Used in: log sampling at scale (sample 1% of requests for analysis), distributed systems with unknown stream sizes (Spark, Flink), database query result sampling.

***

# Why random hashing prevents HashDoS

In 2003, security researchers showed that adversaries who knew the hash function used by a web server could craft request URLs that all hashed to the same hash-table bucket. The hash table degraded to a linked list; lookups went from `O(1)` to `O(n)`; the server slowed to a crawl. **HashDoS** attacks were a real threat.

The fix: **per-process random hash seed**. Each process picks a random 64-bit seed at startup; the hash function mixes the input with the seed. The adversary can't predict the seed, can't precompute collisions.

Python adopted random hash seeds in PEP 456 (Python 3.3, 2012). Java's `HashMap` mitigates differently (it converts long collision chains to red-black trees once they exceed a threshold). Rust's `HashMap` uses SipHash with a per-process seed by default. Languages without this protection are still vulnerable in 2026.

This is randomisation as defence: not for performance, but for *security*. The same principle applies in Bloom filters' multiple hash functions, password salting, and DDoS-resistant routing.

***

# Implementation

```python run viz=graph viz-root=sample
import random

def partition(A, lo, hi):
    pivot = A[hi]
    i = lo - 1
    for j in range(lo, hi):
        if A[j] <= pivot:
            i += 1
            A[i], A[j] = A[j], A[i]
    A[i + 1], A[hi] = A[hi], A[i + 1]
    return i + 1

def quickselect(A, k):
    A = list(A)
    lo, hi = 0, len(A) - 1
    while lo < hi:
        pivot_idx = random.randint(lo, hi)
        A[pivot_idx], A[hi] = A[hi], A[pivot_idx]
        p = partition(A, lo, hi)
        if p == k: return A[p]
        if p < k: lo = p + 1
        else:     hi = p - 1
    return A[lo]

def reservoir_sample(stream, k):
    sample = []
    for i, x in enumerate(stream, start=1):
        if i <= k:
            sample.append(x)
        else:
            j = random.randint(1, i)
            if j <= k:
                sample[j - 1] = x
    return sample


if __name__ == "__main__":
    A = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]
    sorted_A = sorted(A)
    for k in range(len(A)):
        assert quickselect(A, k) == sorted_A[k], f"failed for k={k}"
    print("quickselect: 100% correct on test input")

    # Reservoir sampling: empirical uniformity check
    counts = [0] * 10
    trials = 100_000
    for _ in range(trials):
        sample = reservoir_sample(range(10), k=1)
        counts[sample[0]] += 1
    print(f"reservoir sampling 1-of-10 over {trials:,} trials:")
    print(f"  per-bucket counts: {counts} (expected ~{trials // 10})")
    print(f"  max deviation: {(max(counts) - min(counts)) / (trials / 10) * 100:.1f}%")
```

```java run viz=graph viz-root=sample
import java.util.*;

public class Main {
    static Random rng = new Random();

    static int partition(int[] A, int lo, int hi) {
        int pivot = A[hi], i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (A[j] <= pivot) { i++; int t = A[i]; A[i] = A[j]; A[j] = t; }
        }
        int t = A[i+1]; A[i+1] = A[hi]; A[hi] = t;
        return i + 1;
    }

    static int quickselect(int[] A, int k) {
        A = A.clone();
        int lo = 0, hi = A.length - 1;
        while (lo < hi) {
            int pi = rng.nextInt(hi - lo + 1) + lo;
            int t = A[pi]; A[pi] = A[hi]; A[hi] = t;
            int p = partition(A, lo, hi);
            if (p == k) return A[p];
            if (p < k) lo = p + 1; else hi = p - 1;
        }
        return A[lo];
    }

    public static void main(String[] args) {
        int[] A = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        int[] sorted = A.clone(); Arrays.sort(sorted);
        for (int k = 0; k < A.length; k++) {
            assert quickselect(A, k) == sorted[k];
        }
        System.out.println("quickselect: 100% correct");
    }
}
```

***

# Edge cases and pitfalls

- **Pseudo-random vs cryptographic random.** For algorithms that just need *unpredictable-to-typical-adversaries*, `random.random()` and `Math.random()` are fine. For security-sensitive contexts (HashDoS defence, password salting), use `random.SystemRandom`, `SecureRandom`, or `/dev/urandom`. Don't ship cryptographic decisions made by `Math.random()`.
- **Seeded RNG for reproducibility.** Sometimes you want randomised behaviour but reproducible results (debugging, testing). Seed the RNG explicitly: `random.seed(42)`. Forgetting this causes flaky tests.
- **Reservoir sampling with k=1: the off-by-one.** The chapter's algorithm uses 1-indexed `i`. Implementations using 0-indexed `i` need `j = random.randint(0, i)` and replace if `j == 0`. Easy to get backwards.
- **Quicksort/quickselect: recursive depth.** The expected depth is `O(log n)` but variance exists. For `n = 10⁹` arrays, switch to iterative or tail-call versions to avoid stack overflow. Production library implementations use iterative quickselect.
- **The "always pick the median" temptation.** Picking the *exact* median as pivot guarantees `O(n log n)` worst-case quicksort. But computing the exact median costs `O(n)` per call (or `O(n)` via median-of-medians). Net cost: same expected runtime as random pivots, with much higher constants. *Random* is the practical winner.
- **Probabilistic correctness vs probabilistic running time.** Bloom filters return wrong answers (false positives) but always finish quickly. Quicksort returns the right answer but might take a while. Don't conflate the two.

***

# Production reality

- **CPython's `heapq.nsmallest` / `nlargest`** uses quickselect for some sizes. The standard library "find the k smallest" is asymptotically better than sort-then-slice when `k << n`.
- **C++ STL's `nth_element`** is a linear-expected-time selection algorithm (essentially quickselect).
- **Database query optimisers** sample data to estimate cardinalities. Reservoir sampling is one strategy; "block sampling" (sample whole disk pages) is another. Postgres's `ANALYZE` and the autovacuum sampler use various flavours.
- **Bloom filters and Count-Min sketches.** Probabilistic data structures (covered in [Probabilistic and Advanced](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-index)) lean heavily on randomised hashing.
- **Treaps and skip lists** are randomised data structures with `O(log n)` expected operations. Skip lists in particular show up in LevelDB, Redis, and Java's `ConcurrentSkipListMap`.
- **Miller-Rabin primality testing.** OpenSSL, GnuPG, and every cryptographic library use Miller-Rabin (Monte Carlo) for primality testing during RSA key generation. With 50 rounds, false-positive rate is below cosmic-ray bit-flip probability.
- **Randomised load balancing.** "Power of two choices" — pick two random servers, route to the less loaded one. Beats a single random pick by a huge margin (logarithmic vs linear maximum load). Used in NGINX, HAProxy, Akamai.
- **Distributed consensus.** Some Byzantine fault-tolerant protocols (HoneyBadgerBFT) use randomised coin-flips to break ties.

***

# Practice ladder

1. **Implement randomised quicksort.** Verify correctness on stress tests. Time it against a deterministic-pivot quicksort on already-sorted input — the gap should be dramatic.
   > *Hint:* the chapter's quicksort. The deterministic pivot (always pick first or last) goes `O(n²)` on sorted input.

2. **Quickselect for Top-K** ([LeetCode 215](https://leetcode.com/problems/kth-largest-element-in-an-array/)) — `O(n)` expected solution.
   > *Hint:* the chapter's quickselect, asking for the `k`-th largest = `n-k`-th smallest.

3. **Linked List Random Node** ([LeetCode 382](https://leetcode.com/problems/linked-list-random-node/)) — return a random node of a singly-linked list, but you don't know its length in advance.
   > *Hint:* reservoir sampling with `k=1`.

4. **Random Pick with Weight** ([LeetCode 528](https://leetcode.com/problems/random-pick-with-weight/)) — given an array of weights, return a random index proportional to its weight.
   > *Hint:* prefix sum + binary search. Random number in `[0, total)`, then binary-search the first prefix sum exceeding it.

5. **Implement Miller-Rabin Primality Test.** Test if a 64-bit integer is prime in `O(k log³ n)` for `k` rounds.
   > *Hint:* write `n - 1 = 2^s · d`. Pick `k` random witnesses `a`. Compute `a^d mod n`; if it's 1 or `n - 1`, witness is happy. Otherwise, square `s - 1` times; if any equals `n - 1`, happy. If none, `n` is composite. After `k` happy witnesses, return "probably prime".

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Randomisation defuses adversarial inputs and turns worst cases into expected cases — but only if you reach for it deliberately.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Monte Carlo vs Las Vegas — what's the difference?</summary>

**A:** **Monte Carlo:** always finishes in bounded time, may be *wrong* with bounded probability. **Las Vegas:** always *correct*, running time is a random variable with bounded expectation.

</details>
<details>
<summary><strong>Q:</strong> Expected complexity of randomised quicksort?</summary>

**A:** `O(n log n)` expected; `O(n²)` worst case. Probability of hitting worst case on `n = 10⁶` is essentially zero.

</details>
<details>
<summary><strong>Q:</strong> Expected complexity of randomised quickselect?</summary>

**A:** `O(n)` expected; `O(n²)` worst case. Used for `nth_element` in C++ STL.

</details>
<details>
<summary><strong>Q:</strong> What is reservoir sampling?</summary>

**A:** Pick `k` uniform-random items from a stream of unknown length using `O(k)` memory. Each new item replaces a random slot with probability `k/i`.

</details>
<details>
<summary><strong>Q:</strong> Why do hash tables use random hash seeds?</summary>

**A:** **HashDoS defence.** Without random seeding, an attacker who knows the hash function can craft keys that all collide, degrading lookup to `O(n)` per operation.

</details>
<details>
<summary><strong>Q:</strong> Miller-Rabin primality testing — Monte Carlo or Las Vegas?</summary>

**A:** Monte Carlo. False-positive probability is `< 4^-k` for `k` rounds. With 50 rounds, `< 10^-30` — below cosmic-ray bit-flip rate.

</details>
<details>
<summary><strong>Q:</strong> Why is "power of two choices" load balancing better than random?</summary>

**A:** Pick two random servers; route to the less-loaded. Maximum load grows as `log log n` instead of `log n / log log n` for plain random — exponentially better tail behaviour.

</details>
<details>
<summary><strong>Q:</strong> Pseudo-random vs cryptographic random — when does the choice matter?</summary>

**A:** **Pseudo-random** (`random.random()`, `Math.random()`) is fine for shuffling, sampling, simulations. **Cryptographic** (`SecureRandom`, `/dev/urandom`) is mandatory for keys, salts, HashDoS defence, anything an attacker shouldn't predict.

</details>

## Code template

```python
import random

# Randomised quickselect — O(n) expected, O(n²) worst case.
def quickselect(A, k):
    A = list(A)
    lo, hi = 0, len(A) - 1
    while lo < hi:
        pivot_idx = random.randint(lo, hi)
        A[pivot_idx], A[hi] = A[hi], A[pivot_idx]
        p = partition(A, lo, hi)
        if p == k: return A[p]
        if p < k: lo = p + 1
        else:     hi = p - 1
    return A[lo]

# Reservoir sampling for k items from a stream of unknown length.
def reservoir_sample(stream, k):
    sample = []
    for i, x in enumerate(stream, start=1):
        if i <= k:
            sample.append(x)
        else:
            j = random.randint(1, i)
            if j <= k:
                sample[j - 1] = x
    return sample
```

## Pattern triggers

- **Worst-case input is suspiciously easy to construct** → randomise the algorithm; defuse the adversary
- **"Top-K from a huge unsorted array"** → randomised quickselect, `O(n)` expected
- **"Sample uniformly from a stream of unknown length"** → reservoir sampling
- **"Find a duplicate / collision in a hash"** → birthday paradox; `O(√n)` expected
- **Hash-flood / HashDoS attack** → random hash seed
- **Primality test for a giant number** → Miller-Rabin (Monte Carlo)
- **Probabilistic data structure (Bloom, CMS, HLL)** → randomness in the hash seed
- **Load balancing across servers** → "power of two choices"
- **Crypto / security-sensitive randomness** → use `SecureRandom`, never `Math.random()`

***

# Cross-links

- **Foundations:** [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) (expected vs worst-case), [Amortized Analysis](/cortex/data-structures-and-algorithms/foundations-amortized-analysis).
- **Used by:** [Sorting](/cortex/data-structures-and-algorithms/sorting-and-searching-sorting-quicksort) (randomised quicksort), [Hash Table](/cortex/data-structures-and-algorithms/linear-structures-hash-table-introduction-to-hash-tables) (random seed for HashDoS).
- **Probabilistic structures:** [Skip List, Bloom Filter, Count-Min Sketch, HyperLogLog, Treap](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-index).

***

# Final Takeaway

Randomised algorithms trade worst-case for expected-case. Three patterns to internalise:

1. **Monte Carlo and Las Vegas are different deals.** Monte Carlo: fast, occasionally wrong. Las Vegas: always right, expected fast. Pick deliberately.
2. **Randomisation defeats adversaries.** HashDoS, sorted-input quicksort, and a dozen other "worst case is suspiciously easy to construct" bugs are fixed by adding randomness to the algorithm. The adversary can't aim if they don't know the seed.
3. **The expected case is what matters in practice.** When variance is exponentially small (as in randomised quicksort), the expected-case bound is operationally indistinguishable from a worst-case bound. Don't be paranoid about the theoretical `O(n²)` if the practical probability is `2^-1000`.

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
