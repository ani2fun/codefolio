---
title: Recurrence Relations and the Master Theorem
summary: When an algorithm calls itself on a smaller input, its complexity is hidden inside a recurrence. The Master theorem is the closed-form solver for the recurrences you'll meet 80% of the time.
prereqs:
  - foundations-asymptotic-analysis
---

# 2. Recurrence Relations and the Master Theorem

## The Hook

How do you know merge sort runs in `O(n log n)`?

Read the code: it splits the array in half, recurses on each half, and merges the two sorted halves with a single pass. There is no loop running `n log n` times you can point to — the `n log n` is *hidden* in the call structure. The line "this is `O(n log n)`" is not a measurement; it's a *theorem*, and the theorem is proved by solving an equation about how the function calls itself.

```
T(n) = 2 · T(n/2) + n
```

That equation is a **recurrence relation**. It says "the cost of merge-sorting `n` elements equals the cost of merge-sorting two halves of `n/2` elements, plus the linear cost of merging them." Every divide-and-conquer algorithm has one. Quicksort, binary search, Karatsuba multiplication, the FFT, the Strassen matrix multiplication — every line of "this is `O(stuff)`" you've ever read about a recursive algorithm is that algorithm's recurrence solved.

This chapter is the solver. By the end of it you'll be able to look at any standard divide-and-conquer recurrence, write down the closed-form complexity, and explain *why* without reaching for a textbook.

---

## Table of contents

1. [Why recursive algorithms hide their cost](#why-recursive-algorithms-hide-their-cost)
2. [Reading a recurrence](#reading-a-recurrence)
3. [The recursion-tree method](#the-recursion-tree-method)
4. [The substitution method](#the-substitution-method)
5. [The Master theorem](#the-master-theorem)
6. [Worked examples](#worked-examples)
7. [When the Master theorem doesn't apply](#when-the-master-theorem-doesnt-apply)
8. [A runnable demo](#a-runnable-demo)
9. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
10. [Production reality](#production-reality)
11. [Quiz](#quiz)
12. [Practice ladder](#practice-ladder)
13. [Further reading](#further-reading)
14. [Cross-links](#cross-links)
15. [Final takeaway](#final-takeaway)

***

# Why recursive algorithms hide their cost

A loop's cost is visible. You can stand the loop next to its iteration count and read off the work: `for i in range(n): …` is `n` iterations of whatever's inside, total cost `O(n × inside)`.

A recursive algorithm doesn't show you its iteration count. The call structure unfolds at runtime, branching, recombining, eventually bottoming out at a base case. Counting "how many times does the inner work run" is no longer a stare; it's a calculation.

Consider three recursive shapes you'll meet over and over:

```python
def shape_A(n):
    if n == 0: return
    do_constant_work()      # 1 step
    shape_A(n - 1)          # one recursive call on n-1

def shape_B(n):
    if n == 0: return
    do_n_work(n)            # n steps
    shape_B(n // 2)         # one recursive call on n/2

def shape_C(n):
    if n == 0: return
    do_n_work(n)            # n steps
    shape_C(n // 2)         # two recursive calls
    shape_C(n // 2)         # on n/2 each
```

You can probably guess the costs: A is linear, B is linear, C is `n log n`. But until you've solved each as a recurrence, the guesses are exactly that. The recurrence is what gives you a *proof*, and a proof is what lets you stand by the complexity claim under load testing six months from now.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  subgraph A["Shape A: T(n) = T(n-1) + 1"]
    A1["O(n)<br/>linear"]
  end
  subgraph B["Shape B: T(n) = T(n/2) + n"]
    B1["O(n)<br/>linear"]
  end
  subgraph C["Shape C: T(n) = 2T(n/2) + n"]
    C1["O(n log n)<br/>linearithmic"]
  end
  A --> B --> C
  style A1 fill:#bbf7d0,stroke:#16a34a
  style B1 fill:#fef9c3,stroke:#f59e0b
  style C1 fill:#fde68a,stroke:#d97706
```

<p align="center"><strong>Three small differences in shape produce three different complexity classes. The shape matters more than any individual line of code.</strong></p>

***

# Reading a recurrence

A recurrence relation for an algorithm has two parts:

1. **The recursive case.** A formula that expresses `T(n)` in terms of `T` at smaller inputs.
2. **The base case.** A constant for the smallest inputs the algorithm handles directly without recursing.

Standard form for the divide-and-conquer recurrences this chapter focuses on:

$$T(n) = a \cdot T(n/b) + f(n)$$

with `a ≥ 1` recursive calls, each on a problem of size `n/b` (`b > 1`), plus `f(n)` non-recursive work to split the problem and combine the answers.

Examples drawn from real algorithms:

| Algorithm | Recurrence | Spoken |
|---|---|---|
| Merge sort | `T(n) = 2T(n/2) + n` | two halves of size n/2; linear merge |
| Binary search | `T(n) = T(n/2) + 1` | one half; constant comparison |
| Quicksort (best/avg) | `T(n) = 2T(n/2) + n` | balanced split; linear partition |
| Quicksort (worst) | `T(n) = T(n-1) + n` | unbalanced split; linear partition |
| Karatsuba multiply | `T(n) = 3T(n/2) + n` | three sub-multiplications |
| Strassen matrix mul | `T(n) = 7T(n/2) + n²` | seven sub-multiplications |
| Tree traversal | `T(n) = T(k) + T(n-1-k) + 1` | unknown left/right split |
| Tower of Hanoi | `T(n) = 2T(n-1) + 1` | two recursive moves of n-1 disks |

The shape of the recurrence is everything. `2T(n/2)` is fundamentally different from `T(n-1)` — one halves the problem, the other shaves one off — and that difference shows up as `log n` vs `n` in the closed form.

**Base cases are usually `T(1) = O(1)`** and we usually leave them implicit. The algebra works out for any constant base case; you only need to be careful when the base case itself depends on `n` (rare in practice, common in textbook traps).

***

# The recursion-tree method

The most intuitive solver: draw the tree of recursive calls, write the work at each node, sum across levels.

For `T(n) = 2T(n/2) + n`:

- **Level 0** has one node doing `n` work.
- **Level 1** has two nodes, each doing `n/2` work. Total at this level: `2 × n/2 = n`.
- **Level 2** has four nodes, each doing `n/4` work. Total: `4 × n/4 = n`.
- ...
- **Level k** has `2^k` nodes, each doing `n/2^k` work. Total: `n`.
- The tree bottoms out when `n/2^k = 1`, i.e. at depth `k = log₂ n`.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart TB
  L0["Level 0:  n work"]
  L1a["n/2"]
  L1b["n/2"]
  L2a["n/4"]
  L2b["n/4"]
  L2c["n/4"]
  L2d["n/4"]
  Ld["… log n levels …"]
  Lb["base cases:  n × O(1)"]
  L0 --> L1a
  L0 --> L1b
  L1a --> L2a
  L1a --> L2b
  L1b --> L2c
  L1b --> L2d
  L2a --> Ld
  L2b --> Ld
  L2c --> Ld
  L2d --> Ld
  Ld --> Lb
  style L0 fill:#fef9c3,stroke:#f59e0b
  style Ld fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>Merge sort's recursion tree. Each level does <code>n</code> work; there are <code>log n</code> levels; total <code>n × log n</code>.</strong></p>

The total work is *number-of-levels × work-per-level* = `log n × n = n log n`. Done.

The recursion-tree method is great when:
- You want intuition for *where* the work lives (top-heavy, balanced, leaf-heavy).
- The work-per-level has a simple closed form.
- You want to spot-check your Master-theorem application.

It struggles when:
- Levels do non-uniform work.
- The recursion isn't balanced (e.g. `T(n) = T(n/3) + T(2n/3) + n`, the unbalanced quicksort recurrence).

***

# The substitution method

The most rigorous solver: *guess* the closed form, then *prove* it by induction.

For `T(n) = T(n-1) + 1, T(0) = 0`, guess `T(n) = n`. Prove by induction:

- **Base:** `T(0) = 0 = 0`. ✓
- **Step:** Assume `T(n-1) = n - 1`. Then `T(n) = T(n-1) + 1 = (n-1) + 1 = n`. ✓

So `T(n) = n`, exactly. `T(n) = O(n)`, asymptotically.

The substitution method is great when:
- You already have a guess (often from the recursion-tree method).
- You need a formal proof for a paper or textbook context.

It struggles when:
- Your guess is wrong (you'll find out, but only after wasted algebra).
- The recurrence isn't standard form (you may need messy algebra to push through).

In practice: use the recursion tree to guess, the substitution method to verify, and the Master theorem to skip both.

***

# The Master Theorem

For the standard form `T(n) = a · T(n/b) + f(n)` with `a ≥ 1`, `b > 1`, the Master theorem gives a closed form by comparing `f(n)` against `n^(log_b a)`.

> **Master theorem:** Let `T(n) = a · T(n/b) + f(n)`. Define the **threshold** `n^(log_b a)`. Compare `f(n)` to it.
>
> - **Case 1:** `f(n) = O(n^(log_b a − ε))` for some `ε > 0` *(threshold dominates)*. Then `T(n) = Θ(n^(log_b a))`.
> - **Case 2:** `f(n) = Θ(n^(log_b a))` *(they're equal)*. Then `T(n) = Θ(n^(log_b a) · log n)`.
> - **Case 3:** `f(n) = Ω(n^(log_b a + ε))` for some `ε > 0` and a regularity condition (`a · f(n/b) ≤ c · f(n)` for some `c < 1`) *(`f` dominates)*. Then `T(n) = Θ(f(n))`.

In English:

- **Case 1:** the leaves of the recursion tree do most of the work. The `n^(log_b a)` "leaf count" wins.
- **Case 2:** every level does the same amount of work. Total = work-per-level × number-of-levels = `f(n) · log n`.
- **Case 3:** the root does most of the work. The `f(n)` non-recursive cost wins.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  subgraph CASE1["Case 1: leaves win"]
    L1A["root: small work"] --> L1B["leaves: most work<br/>Θ(n^(log_b a))"]
  end
  subgraph CASE2["Case 2: balanced"]
    L2A["root: f(n)"] --> L2B["every level: f(n)<br/>Θ(f(n) · log n)"]
  end
  subgraph CASE3["Case 3: root wins"]
    L3A["root: f(n)<br/>most work"] --> L3B["leaves: small"]
  end
  style L1B fill:#bbf7d0,stroke:#16a34a
  style L2B fill:#fef9c3,stroke:#f59e0b
  style L3A fill:#fde68a,stroke:#d97706
```

<p align="center"><strong>The three Master-theorem cases as recursion-tree pictures. The case is decided by where the work lives.</strong></p>

***

# Worked examples

## Merge sort: `T(n) = 2T(n/2) + n`

`a = 2, b = 2, f(n) = n`. Threshold: `n^(log₂ 2) = n^1 = n`. `f(n) = Θ(n)` matches the threshold exactly → **Case 2**. So `T(n) = Θ(n log n)`. ✓

## Binary search: `T(n) = T(n/2) + 1`

`a = 1, b = 2, f(n) = 1`. Threshold: `n^(log₂ 1) = n^0 = 1`. `f(n) = Θ(1)` matches → **Case 2**. So `T(n) = Θ(log n)`. ✓

## Linear maximum: `T(n) = T(n/2) + n` *(scan-then-recurse, found in some streaming algorithms)*

`a = 1, b = 2, f(n) = n`. Threshold: `n^0 = 1`. `f(n) = n` is asymptotically larger → **Case 3** (regularity holds: `1 × (n/2) ≤ c · n` for `c = 0.5`). So `T(n) = Θ(n)`.

The intuition: the root of the recursion tree does `n` work, the next level does `n/2`, then `n/4`, … This is a *geometric* series summing to `2n`, dominated by the root. The recursive part is asymptotically free.

## Karatsuba multiplication: `T(n) = 3T(n/2) + n`

Karatsuba is the divide-and-conquer integer multiplication that reduces a multiplication of two `n`-digit numbers to *three* multiplications of `n/2`-digit numbers, plus linear addition.

`a = 3, b = 2, f(n) = n`. Threshold: `n^(log₂ 3) ≈ n^1.585`. `f(n) = n` is asymptotically smaller → **Case 1**. So `T(n) = Θ(n^(log₂ 3)) ≈ Θ(n^1.585)`.

This is *better* than the textbook `Θ(n²)` long-multiplication algorithm — and is the reason Python's built-in arbitrary-precision integers use Karatsuba above a threshold.

## Strassen matrix multiplication: `T(n) = 7T(n/2) + n²`

Strassen multiplies two `n × n` matrices via *seven* multiplications of `n/2 × n/2` submatrices plus quadratic-cost additions.

`a = 7, b = 2, f(n) = n²`. Threshold: `n^(log₂ 7) ≈ n^2.807`. `f(n) = n²` is asymptotically smaller → **Case 1**. So `T(n) = Θ(n^(log₂ 7)) ≈ Θ(n^2.807)`.

Better than the cubic textbook matrix multiply. Theoretical algorithms (Coppersmith-Winograd, Le Gall) push this lower; in practice Strassen is the largest-coefficient improvement actually implemented in real linear-algebra libraries.

## Worst-case quicksort: `T(n) = T(n-1) + n`

This is *not* in standard `T(n) = aT(n/b) + f(n)` form (the recursion is on `n-1`, not `n/b`). Master theorem doesn't apply directly. Use the recursion tree:

- Level 0: `n` work
- Level 1: `n-1` work
- Level 2: `n-2` work
- ...
- Level n: 1 work

Sum: `n + (n-1) + (n-2) + … + 1 = n(n+1)/2 = Θ(n²)`.

The pathological-input quicksort that drops to `O(n²)` is exactly this case: each pivot peels off one element, the recursion is one-sided, and the linear-cost partitions add up to a quadratic.

***

# When the Master theorem doesn't apply

The Master theorem covers a wide but bounded slice of recurrences. It *doesn't* cover:

1. **Subtractive recurrences** like `T(n) = T(n-1) + n` (use recursion-tree or substitution).
2. **Polylogarithmic factors** like `T(n) = 2T(n/2) + n log n` — falls between Case 2 and 3, needing the *generalised* Master theorem (or its extension by Akra-Bazzi).
3. **Non-constant `a` or `b`** — `T(n) = nT(n/2) + n` doesn't fit; the work is exotic.
4. **Unbalanced recursive calls** — `T(n) = T(n/3) + T(2n/3) + n` is unbalanced. The recursion-tree method handles many such cases. As long as the cuts are *fractional* (not subtractive), every root-to-leaf path is `O(log n)` long and the analysis still gives `Θ(n log n)`. But the standard Master theorem doesn't apply.
5. **Recurrences where `f(n)` is between Case 2 and Case 3** — i.e. `f(n)` is asymptotically larger than `n^(log_b a)` but only by a logarithmic factor, like `T(n) = 2T(n/2) + n log n`. Then `T(n) = Θ(n log² n)`. This is sometimes called the "generalised Case 2" or the "Master theorem with polylog gap".

The serious tool for irregular recurrences is the **Akra-Bazzi theorem** — a strict generalisation of the Master theorem that handles uneven splits and polylog factors. We won't derive it here; the rule of thumb is: recursion-tree first, Master if it fits, Akra-Bazzi if you need to publish.

***

# A runnable demo

The code below implements three recursive algorithms and times them as `n` grows. Run it; the columns should match the closed-form complexities the Master theorem predicts.

```python run viz=graph viz-root=out
import time, random

def merge_sort(a):
    if len(a) <= 1: return a
    mid = len(a) // 2
    left = merge_sort(a[:mid])
    right = merge_sort(a[mid:])
    out, i, j = [], 0, 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]: out.append(left[i]); i += 1
        else: out.append(right[j]); j += 1
    out.extend(left[i:]); out.extend(right[j:])
    return out

def binary_search(a, target):
    lo, hi = 0, len(a) - 1
    while lo <= hi:
        mid = lo + (hi - lo) // 2
        if a[mid] == target: return mid
        if a[mid] < target: lo = mid + 1
        else: hi = mid - 1
    return -1

def time_ms(fn):
    t0 = time.perf_counter()
    fn()
    return (time.perf_counter() - t0) * 1000

if __name__ == "__main__":
    print(f"{'n':>10} {'merge_sort (ms)':>18} {'binary_search (µs)':>22}")
    for n in [10_000, 100_000, 1_000_000]:
        a = [random.randint(0, 10**9) for _ in range(n)]
        ms = time_ms(lambda: merge_sort(a))
        sa = sorted(a)
        us = time_ms(lambda: [binary_search(sa, sa[i]) for i in range(0, n, max(1, n // 100))]) * 10  # ~100 lookups
        print(f"{n:>10} {ms:>18.1f} {us:>22.1f}")
```

```java run
import java.util.*;

public class Main {
    static int[] mergeSort(int[] a) {
        if (a.length <= 1) return a;
        int mid = a.length / 2;
        int[] left = mergeSort(Arrays.copyOfRange(a, 0, mid));
        int[] right = mergeSort(Arrays.copyOfRange(a, mid, a.length));
        int[] out = new int[a.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) out[k++] = left[i++];
            else out[k++] = right[j++];
        }
        while (i < left.length) out[k++] = left[i++];
        while (j < right.length) out[k++] = right[j++];
        return out;
    }

    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] sizes = {10_000, 100_000, 1_000_000};
        Random rng = new Random(42);
        System.out.printf("%10s %18s %22s%n", "n", "merge_sort (ms)", "binary_search (µs)");
        for (int n : sizes) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rng.nextInt(1_000_000_000);
            long t0 = System.nanoTime();
            int[] sorted = mergeSort(a);
            double ms = (System.nanoTime() - t0) / 1_000_000.0;
            t0 = System.nanoTime();
            int step = Math.max(1, n / 100);
            for (int i = 0; i < n; i += step) binarySearch(sorted, sorted[i]);
            double us = (System.nanoTime() - t0) / 1_000.0;
            System.out.printf("%10d %18.1f %22.1f%n", n, ms, us);
        }
    }
}
```

The merge-sort column should grow roughly as `n log n` (going `n × 10` should go runtime `× 11` or so). The binary-search column should grow logarithmically with the array size — almost constant per lookup.

***

# Edge cases and pitfalls

- **Wrong-form recurrences and the Master theorem.** The Master theorem applies *only* to `T(n) = aT(n/b) + f(n)`. Subtractive recurrences (`T(n-1) + 1`) need the recursion tree or substitution. Throwing the Master theorem at `T(n) = T(n-1) + 1` and getting `Θ(1)` is a common interview mistake.
- **Forgetting the regularity condition in Case 3.** `f(n)` being asymptotically larger than `n^(log_b a)` isn't enough. You also need `a · f(n/b) ≤ c · f(n)` for some `c < 1`. The condition fails for pathological `f(n)` like `f(n) = n^2 · sin(n)` — they oscillate. For all polynomially-bounded `f(n)`, the regularity condition holds automatically; you only think about it when `f(n)` is exotic.
- **The polylog gap.** `T(n) = 2T(n/2) + n log n` is *not* covered by the standard Master theorem — `f(n) = n log n` is asymptotically larger than `n^1` but only by a polylog factor, not polynomially. The "generalised Case 2" gives `Θ(n log² n)`. Get this wrong and you'll claim merge-sort-with-merge-step-doing-extra-log-work is `Θ(n log n)` when it's `Θ(n log² n)`.
- **Hidden loops in `f(n)`.** If your recursive function has a loop *inside* the recursion at each level, the loop counts as part of `f(n)`. People sometimes forget the loop and analyse the recursion alone, getting `Θ(log n)` for an algorithm that's actually `Θ(n log n)`.
- **Different base cases change the constant, not the asymptotic.** Whether `T(0) = 0` or `T(1) = 5` or `T(10) = 100`, the asymptotic complexity is identical. Don't waste time fiddling with the base case unless you're computing exact runtime.
- **Worst-case-vs-average recursion shape.** Quicksort's recurrence depends on pivot quality. The recurrence `T(n) = 2T(n/2) + n` (best/average) gives `n log n`. The recurrence `T(n) = T(n-1) + n` (worst) gives `n²`. Both are valid — they describe different *cases* of the same algorithm.

***

# Production Reality

**CPython's `int` multiplication** — uses **Karatsuba** — because `Θ(n^1.585)` beats schoolbook `Θ(n²)` once digits exceed roughly 70.

CPython's `Objects/longobject.c` switches from grade-school multiplication to Karatsuba above `KARATSUBA_CUTOFF`. The Master-theorem solution to `T(n) = 3T(n/2) + n` is what makes the switch worth the engineering effort: every RSA signing operation, every cryptographic hash that touches arbitrary-precision integers, every `**` on a big number rides on this recurrence's closed form.

**Intel MKL and OpenBLAS** — use **Strassen matrix multiplication** above a size threshold — because `Θ(n^2.807)` finally outruns cache-friendly cubic above ~64–128 rows.

Strassen's smaller asymptotic ceiling comes with larger constants and worse cache locality, so the cubic algorithm wins for small matrices. The crossover threshold is decided empirically per-CPU and lives in the library's heuristic dispatch table. The Master theorem says *that* Strassen wins eventually; the constant factors decide *when*.

**Hybrid sorting (`std::sort`, `Arrays.sort`, Timsort)** — uses **divide-and-conquer with insertion-sort cutoff** — because the recurrence's clean `Θ(n log n)` only describes the tree above the cutoff; below it, `Θ(n)` per call beats merge.

The merge-sort recurrence `T(n) = 2T(n/2) + n` gives `Θ(n log n)` *asymptotically*. For small `n`, the constant in front of `n log n` is large enough that insertion sort's `Θ(n²)` actually wins. Production sorters fold the two algorithms together: divide until subarrays are 16-ish elements, then insertion-sort the leaves. Asymptotic class is unchanged; constant factor improves.

**Cooley-Tukey FFT** — uses **divide-and-conquer over complex roots of unity** — because `T(n) = 2T(n/2) + n → Θ(n log n)` makes audio compression and large-integer multiplication feasible at all.

Every MP3 encoder, every signal-processing library, every numerical convolution routine reduces to an FFT. The Master theorem is the one-line proof that the algorithm is `Θ(n log n)` rather than the `Θ(n²)` cost of evaluating the naive discrete Fourier transform.

**Parallel divide-and-conquer (work-stealing schedulers, MapReduce)** — uses **a per-processor recurrence on critical-path depth** — because the sequential recurrence becomes `T(n) = T(n/b) + f(n)` along the critical path when `p ≥ a`.

Parallel analysis splits into two quantities. Total *work* sums across all leaves and matches the sequential recurrence. Critical-path *span* tracks the longest dependency chain. The Master theorem applies to both, but the span recurrence has `a = 1` because parallel branches don't add to depth. See [Concurrency and Systems](/cortex/data-structures-and-algorithms/concurrency-and-systems-index) for how this shows up in Java's `ForkJoinPool` and Go's runtime.

***

# Quiz

Cover the answer with your hand before reading it.

**[Recall] Q: What is the standard form of a divide-and-conquer recurrence, and what does each symbol mean?**
`T(n) = a · T(n/b) + f(n)`, where `a ≥ 1` is the number of recursive calls, `b > 1` is the size-reduction factor for each call, and `f(n)` is the non-recursive work done to split and combine.

**[Recall] Q: Which Master-theorem case applies to merge sort's recurrence `T(n) = 2T(n/2) + n`, and what is the closed form?**
Case 2 — `f(n) = Θ(n)` equals the threshold `n^(log₂ 2) = n` — giving `Θ(n log n)` time.

**[Reasoning] Q: Why does `T(n) = T(n-1) + n` give `Θ(n²)` while `T(n) = 2T(n/2) + n` gives `Θ(n log n)`, even though both do `n` non-recursive work per call?**
The subtractive recurrence has `n` levels each doing roughly linear work, so the work sums to `n + (n-1) + … + 1 = Θ(n²)`; the halving recurrence has only `log n` levels each totalling `n`, so the work sums to `Θ(n log n)`.

**[Tradeoff] Q: When should you reach for the recursion-tree method instead of the Master theorem, even when the Master theorem might apply?**
Pick the recursion tree when you need intuition about *where* the work lives — top-heavy, balanced, or leaf-heavy — or as a spot-check against a Master-theorem application, because the Master theorem hides the geometry behind a one-line answer.

**[Tradeoff] Q: Karatsuba multiplication has worse cache behaviour and higher constant factors than schoolbook multiplication; why do production libraries use it anyway?**
Karatsuba runs in `Θ(n^1.585)` time versus schoolbook's `Θ(n²)`, so once `n` exceeds the crossover threshold (`KARATSUBA_CUTOFF` ≈ 70 digits in CPython) the asymptotic win swamps the constant-factor loss; below the threshold, libraries fall back to schoolbook for exactly that reason.

***

# Practice ladder

Five exercises ordered from warm-up to hardest. Foundations chapters do not own pattern problems — these are textbook-style recurrence drills plus one LeetCode link where the closed-form analysis is the headline insight.

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | Solve `T(n) = T(n/3) + 1` directly | Master theorem, Case 2 | Easy | `a=1, b=3, f(n)=1`; threshold `n^(log₃ 1) = 1` matches `f(n)` → `Θ(log n)`. |
| 2 | Tower of Hanoi: `T(n) = 2T(n-1) + 1, T(0) = 0` | Recursion-tree / substitution | Easy | Master theorem does not apply; the tree has `2^n` leaves each doing `O(1)` work → `Θ(2^n)` time, `O(n)` space. |
| 3 | Identify the algorithm with recurrence `T(n) = 7T(n/2) + n²` | Master theorem, Case 1 | Medium | `a=7, b=2`; threshold `n^(log₂ 7) ≈ n^2.807` beats `f(n) = n²` → `Θ(n^2.807)`. The algorithm is Strassen matrix multiply. |
| 4 | Audit the claim: `T(n) = 4T(n/2) + n²` gives `Θ(n² log n)` | Master theorem, Case 2 | Medium | Threshold `n^(log₂ 4) = n²` matches `f(n) = n²` → Case 2 → `Θ(n² log n)`. The audit is correct; this is naive 4-way block matrix multiply. |
| 5 | Solve the unbalanced recurrence `T(n) = T(n/3) + T(2n/3) + n` | Recursion-tree, fractional split | Hard | Each level still totals `n` work; longest root-to-leaf path is `log_{3/2} n` → `Θ(n log n)` time, `O(log n)` space. This is the median-of-medians worst-case-balanced split. |
| 6 | [LeetCode 53 — Maximum Subarray (divide-and-conquer)](https://leetcode.com/problems/maximum-subarray/) | Master theorem, Case 2 | Medium | Divide-and-conquer split + linear cross-boundary merge → `T(n) = 2T(n/2) + n → Θ(n log n)` time, `O(log n)` space — beaten by Kadane's `O(n)` time, `O(1)` space, but the recurrence is the canonical Master-theorem warm-up. |

***

# Further Reading

- [Introduction to Algorithms (Cormen, Leiserson, Rivest, Stein) — Chapter 4: Divide-and-Conquer](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)
  ★ Essential — the canonical proof of the Master theorem, plus the recursion-tree and substitution methods with full algebra.
- [Akra-Bazzi method (Wikipedia)](https://en.wikipedia.org/wiki/Akra%E2%80%93Bazzi_method)
  ◆ Advanced — the generalisation that handles uneven splits and polylog factors. Reach for it when the Master theorem refuses to apply.
- [The Design and Analysis of Algorithms — Aho, Hopcroft, Ullman (1974)](https://archive.org/details/designanalysisof00ahoarich)
  ◆ Advanced — the original divide-and-conquer chapter that predates the Master theorem's clean statement, useful for the historical view of how these proofs were done by hand.
- [Master theorem cheat sheet — UC Davis CS](https://web.cs.ucdavis.edu/~amenta/w04/dazli/recurrences.pdf)
  → Reference — one-page table of cases and worked examples. Pin it next to the Big-O cheat sheet.
- [Strassen's algorithm — original 1969 paper](https://link.springer.com/article/10.1007/BF02165411)
  ◆ Advanced — four pages that shaved a third off matrix multiplication. Read it once to see what the Master theorem looks like in the wild.

***

# Cross-links

**Prerequisites**

- [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — the `Θ`, `O`, `Ω` vocabulary used throughout this chapter.

**What comes next**

- [Amortized Analysis](/cortex/data-structures-and-algorithms/foundations-amortized-analysis) — generalises recurrence reasoning to operations whose per-call cost varies but whose long-run average is small.
- [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) — every divide-and-conquer chapter calls the Master theorem to justify its headline complexity.
- [Sorting](/cortex/data-structures-and-algorithms/sorting-and-searching-sorting-index) — the `Θ(n log n)` lower bound and the algorithms that hit it are recurrence proofs.
- [DSA in Real Systems: Postgres B-Tree](/cortex/data-structures-and-algorithms/dsa-in-real-systems-postgres-b-tree-and-the-write-path) — *stub* — where similar recurrence reasoning gives the cost of a B-tree write.

***

# Final Takeaway

1. **Core mechanic:** a recurrence relation expresses a recursive algorithm's cost as a function of itself on smaller inputs; the Master theorem solves the standard `T(n) = a · T(n/b) + f(n)` shape in closed form by comparing `f(n)` to the threshold `n^(log_b a)`.
2. **Dominant tradeoff:** the Master theorem is fast and mechanical but applies only to balanced, polynomial-`f` divide-and-conquer recurrences; subtractive, unbalanced, or polylog-gapped recurrences fall back to the recursion tree or substitution, which are more general but slower to apply.
3. **One thing to remember:** the case is decided by where the work lives in the recursion tree — leaves win (Case 1), every level matches (Case 2), or the root wins (Case 3) — and that geometry is what makes an algorithm fast or slow.
