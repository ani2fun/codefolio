---
title: Introduction to Divide and Conquer
summary: Split the problem in half, solve each half recursively, combine the answers. The strategy behind merge sort, quick sort, binary search, FFT, Karatsuba multiplication, and most O(n log n) algorithms.
prereqs:
  - foundations-recurrence-relations-and-master-theorem
  - algorithms-by-strategy-recursion-introduction-to-recursion
---

# 1. Introduction to Divide and Conquer

## The Hook

Asked to multiply two 1024-digit numbers, the textbook algorithm runs `n²` digit-by-digit operations — a million operations. In 1960, Anatoly Karatsuba realised this could be done with **three** multiplications of 512-digit numbers instead of four — and recursively. The recurrence `T(n) = 3T(n/2) + n` gives `Θ(n^1.585)` instead of `Θ(n²)`. For 10,000-digit numbers, the gap is *fifty times*.

That single trick — split the input in half, do recursive work on each half, recombine — is **divide and conquer**. It's the strategy behind merge sort, quick sort, binary search, the Fast Fourier Transform, the Strassen matrix multiply, the Karatsuba multiply, the Cooley-Tukey integer multiply that powers RSA, and most of the algorithms that earn an `O(n log n)` complexity for what would otherwise be `O(n²)` problems.

This chapter is the strategy. By the end you'll recognise divide-and-conquer when you see it, derive complexities via the Master theorem (covered in [Recurrence Relations](/cortex/data-structures-and-algorithms/foundations-recurrence-relations-and-master-theorem)), and be able to write a divide-and-conquer solution to a new problem from scratch.

---

## Table of contents

1. [The three steps](#the-three-steps)
2. [Worked examples](#worked-examples)
3. [Implementation](#implementation)
4. [Why D&C wins](#why-d-c-wins)
5. [When D&C is the wrong choice](#when-d-c-is-the-wrong-choice)
6. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
7. [Production reality](#production-reality)
8. [Practice ladder](#practice-ladder)
9. [Cross-links](#cross-links)
10. [Final takeaway](#final-takeaway)

***

# The Three Steps

Divide-and-conquer follows a strict pattern:

1. **Divide.** Split the problem into smaller subproblems of the same kind.
2. **Conquer.** Solve each subproblem (recursively, or directly if small enough — the base case).
3. **Combine.** Stitch the subproblem answers together into the answer for the original problem.

For merge sort: divide = "split array in half"; conquer = "recursively sort each half"; combine = "merge the two sorted halves with a linear pass". For binary search: divide = "compare middle element to target, decide which half to descend into"; conquer = "recursively search that half"; combine = "(nothing — only one half is ever processed)".

Different recurrences arise from different choices in each step. The Master theorem (cited above) tells you the closed-form complexity once you've identified `a` (subproblems), `b` (size reduction), and `f(n)` (combine cost).

***

# Worked examples

## Maximum subarray (Kadane's-D&C version)

Given an array, find the contiguous subarray with the largest sum. Naive: `O(n²)`. Kadane's algorithm: `O(n)` linear-pass DP. Divide-and-conquer: `O(n log n)`.

D&C strategy: the maximum subarray of `A[lo..hi]` is one of three things:

- The maximum subarray of `A[lo..mid]` (entirely in left half).
- The maximum subarray of `A[mid+1..hi]` (entirely in right half).
- A subarray *crossing* the midpoint (which must include both `A[mid]` and `A[mid+1]`).

The first two are recursive calls; the third is computed in `O(n)` by scanning outward from the midpoint. Recurrence: `T(n) = 2T(n/2) + n → Θ(n log n)`.

Kadane's `O(n)` solution makes this D&C version obsolete in practice, but it's the canonical "you can't *just* recurse on halves" example — you have to handle the cross-midpoint case explicitly.

## Closest pair of points

Given `n` points in the plane, find the two closest. Naive: `O(n²)`. D&C: `O(n log n)`.

- Sort points by x-coordinate.
- Recurse on left half; recurse on right half. Each returns its closest pair distance, take the minimum `δ`.
- The answer pair could *cross* the median x. Look only at points within `δ` of the median's x. Sort those by y; for each, only check up to 7 successors (geometry argument). `O(n)` work.
- Recurrence: `T(n) = 2T(n/2) + n → Θ(n log n)`.

## Karatsuba multiplication

Multiply two `n`-digit integers `x = x₁ · 10^(n/2) + x₀` and `y = y₁ · 10^(n/2) + y₀`:

- Naive: `xy = x₁y₁ · 10^n + (x₁y₀ + x₀y₁) · 10^(n/2) + x₀y₀` — four sub-multiplications, `T(n) = 4T(n/2) + n → Θ(n²)`.
- Karatsuba: compute `z₂ = x₁y₁`, `z₀ = x₀y₀`, `z₁ = (x₁ + x₀)(y₁ + y₀) - z₂ - z₀`. Three sub-multiplications. `T(n) = 3T(n/2) + n → Θ(n^(log₂ 3)) ≈ Θ(n^1.585)`.

Practical for ~50-digit-or-larger numbers. Below that, naive multiplication's smaller constants win.

## Strassen matrix multiplication

Multiply two `n × n` matrices: naive `Θ(n³)` (eight sub-multiplications, `T(n) = 8T(n/2) + n²`). Strassen reduces to seven sub-multiplications via clever algebra: `T(n) = 7T(n/2) + n² → Θ(n^(log₂ 7)) ≈ Θ(n^2.807)`. Used in BLAS implementations above a size threshold.

***

# Implementation

The maximum-subarray D&C version, in Python and Java.

```python run
def max_crossing(A, lo, mid, hi):
    left_sum = float('-inf'); s = 0
    for i in range(mid, lo - 1, -1):
        s += A[i]
        if s > left_sum: left_sum = s
    right_sum = float('-inf'); s = 0
    for j in range(mid + 1, hi + 1):
        s += A[j]
        if s > right_sum: right_sum = s
    return left_sum + right_sum

def max_subarray(A, lo, hi):
    if lo == hi: return A[lo]
    mid = (lo + hi) // 2
    return max(max_subarray(A, lo, mid),
               max_subarray(A, mid + 1, hi),
               max_crossing(A, lo, mid, hi))


if __name__ == "__main__":
    A = [-2, 1, -3, 4, -1, 2, 1, -5, 4]
    print(f"max subarray sum = {max_subarray(A, 0, len(A) - 1)}    (expected 6, from [4, -1, 2, 1])")
```

```java run
public class Main {
    static int maxCrossing(int[] A, int lo, int mid, int hi) {
        int leftSum = Integer.MIN_VALUE, s = 0;
        for (int i = mid; i >= lo; i--) { s += A[i]; if (s > leftSum) leftSum = s; }
        int rightSum = Integer.MIN_VALUE; s = 0;
        for (int j = mid + 1; j <= hi; j++) { s += A[j]; if (s > rightSum) rightSum = s; }
        return leftSum + rightSum;
    }

    static int maxSubarray(int[] A, int lo, int hi) {
        if (lo == hi) return A[lo];
        int mid = (lo + hi) / 2;
        return Math.max(Math.max(maxSubarray(A, lo, mid), maxSubarray(A, mid + 1, hi)),
                        maxCrossing(A, lo, mid, hi));
    }

    public static void main(String[] args) {
        int[] A = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("max sum = " + maxSubarray(A, 0, A.length - 1));
    }
}
```

***

# Why D&C wins

When divide-and-conquer beats a naive `O(n²)` solution, the win comes from one of three sources:

1. **Most of the work is wasted in the naive algorithm.** Merge sort and quick sort beat selection sort because they don't repeat comparisons.
2. **The combine step is dramatically cheaper than redoing the work.** Karatsuba's combine is `O(n)` (a few additions); the savings come from doing only 3 sub-multiplications instead of 4.
3. **Parallelism comes for free.** Recursive calls on independent halves are trivially parallelisable. Modern parallel sort implementations (Java's `Arrays.parallelSort`, .NET's `PLINQ`) lean on this.

Conversely, D&C *doesn't* help when:

- The problem doesn't decompose into independent subproblems (most graph problems, dynamic-programming problems with overlapping subproblems).
- The combine cost is too high (`f(n) = n²` makes the recurrence `T(n) = 2T(n/2) + n² → Θ(n²)`, no better than naive).
- The problem is inherently sequential (parsing, certain physical simulations).

***

# When D&C is the wrong choice

If subproblems *overlap* (Fibonacci, edit distance, longest-common-subsequence), pure D&C wastes exponential work on duplicate subproblems. The fix is **dynamic programming** — memoise the results so each subproblem is solved once. Covered in [Dynamic Programming](/cortex/data-structures-and-algorithms/algorithms-by-strategy-dynamic-programming-index).

If the problem requires examining *every* element (find max in array, sum array elements), D&C doesn't win — the recurrence reduces to `T(n) = O(n)` and the linear scan is simpler.

If the problem is **inherently online** (you can't see the whole input at once), D&C is awkward — you'd need to buffer and re-process. Streaming algorithms with constant or logarithmic memory are usually a different design pattern.

***

# Edge cases and pitfalls

- **Forgetting the cross-midpoint case.** In the maximum-subarray problem, recursing on left and right alone misses subarrays that cross the boundary. *Always* think about whether the answer can span the midpoint.
- **Integer overflow in the midpoint.** `(lo + hi) / 2` overflows for very large arrays. Use `lo + (hi - lo) / 2` (the same fix as in [binary search](/cortex/data-structures-and-algorithms/sorting-and-searching-searching-binary-search)).
- **Base case off-by-one.** The base case must handle the smallest input correctly. For an array, the smallest is usually `lo == hi` (single element); careless implementations crash on it.
- **Combine cost dominates the analysis.** If your combine is `O(n²)`, the Master theorem gives `Θ(n²)`. Always check the combine cost — that's where many "D&C didn't help" surprises come from.
- **Recursion depth on huge inputs.** D&C on `n = 10⁹` has depth `log₂ 10⁹ ≈ 30`, no problem. D&C on a linked-list version with `n = 10⁹` has depth proportional to the recursion shape — potentially stack-overflowing. Iterative or trampolined versions exist for the most common D&C algorithms (iterative merge sort, for instance).

***

# Production reality

- **Standard-library sort algorithms.** `std::sort`, `Arrays.sort`, Python's `sorted` — all hybrid implementations starting with a divide-and-conquer (quicksort or mergesort) and switching to insertion sort for small partitions. The constant factor of insertion sort dominates for small arrays.
- **Karatsuba in CPython.** `Lib/_pylong.py` switches to Karatsuba for big-integer multiplication above a digit-count threshold. This is what makes Python's arbitrary-precision arithmetic practical for cryptographic uses.
- **Strassen in BLAS.** Major BLAS implementations (Intel MKL, OpenBLAS) use Strassen for matrix multiplication above a size threshold (typically 64 or 128). Below the threshold, the cubic algorithm wins on cache friendliness.
- **The Cooley-Tukey FFT** in scientific computing libraries (FFTW, NumPy's `np.fft`) is a divide-and-conquer with recurrence `T(n) = 2T(n/2) + n → Θ(n log n)`. Behind every audio codec, signal-processing pipeline, and large polynomial multiplication.
- **Parallel-aware D&C in modern runtimes.** Java's `ForkJoinPool`, Rayon (Rust), Cilk (research) — all built specifically to make divide-and-conquer parallel-friendly. The work-stealing scheduler is what closes the gap between "in theory parallel" and "actually parallel".

***

# Practice ladder

1. **Maximum Subarray** ([LeetCode 53](https://leetcode.com/problems/maximum-subarray/)) — implement Kadane's `O(n)` *and* the `O(n log n)` divide-and-conquer. Compare.
   > *Hint:* the chapter implements the D&C version. Kadane's: keep `cur_sum` and `best_sum`; reset `cur_sum` to 0 whenever it goes negative.

2. **Count Inversions in an Array.** Given `A`, count pairs `(i, j)` with `i < j` and `A[i] > A[j]`.
   > *Hint:* modified merge sort. During merge, when picking from the right half, count how many elements remain in the left half — those are inversions. Total: `O(n log n)`.

3. **Karatsuba Multiplication.** Implement Karatsuba's algorithm on integer-as-string inputs.
   > *Hint:* the three sub-multiplications are `z₂ = x₁ · y₁`, `z₀ = x₀ · y₀`, `z₁ = (x₁ + x₀)(y₁ + y₀) − z₂ − z₀`. Recurse to a base case (e.g., 1-digit numbers, multiply directly).

4. **Closest Pair of Points.** Given `n` points, find the two closest. `O(n log n)` D&C.
   > *Hint:* the chapter sketches the algorithm. The geometry trick is that any candidate "crossing pair" must lie within `δ` of the median x-coordinate — so the cross-step processes only an `O(n)` strip.

5. **Median of Two Sorted Arrays** ([LeetCode 4](https://leetcode.com/problems/median-of-two-sorted-arrays/)) — given two sorted arrays, find the median in `O(log(min(m, n)))`.
   > *Hint:* binary search on the smaller array; for each partition position, check whether `max(left of A) + max(left of B) ≤ min(right of A) + min(right of B)`. Reduces median to a divide-and-conquer search.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Once the three D&C steps are reflexive, every recursive optimisation problem starts with "what's `a`, what's `b`, what's `f(n)`?"

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Three steps of D&C?</summary>

**A:** **Divide** the problem into smaller subproblems of the same kind. **Conquer** each (recursively). **Combine** the answers into the original problem's answer.

</details>
<details>
<summary><strong>Q:</strong> Recurrence for merge sort?</summary>

**A:** `T(n) = 2T(n/2) + n → Θ(n log n)`. Master theorem case 2.

</details>
<details>
<summary><strong>Q:</strong> Recurrence for binary search?</summary>

**A:** `T(n) = T(n/2) + 1 → Θ(log n)`.

</details>
<details>
<summary><strong>Q:</strong> Recurrence for Karatsuba multiplication?</summary>

**A:** `T(n) = 3T(n/2) + n → Θ(n^(log₂ 3)) ≈ Θ(n^1.585)`. Beats `Θ(n²)` for large enough `n`.

</details>
<details>
<summary><strong>Q:</strong> Recurrence for Strassen matrix multiplication?</summary>

**A:** `T(n) = 7T(n/2) + n² → Θ(n^(log₂ 7)) ≈ Θ(n^2.807)`. Beats cubic for large enough matrices.

</details>
<details>
<summary><strong>Q:</strong> Why is D&C the wrong choice for Fibonacci?</summary>

**A:** Subproblems overlap (`fib(n-1)` and `fib(n-2)` both call `fib(n-3)`). Pure D&C wastes exponential work on duplicates. Use DP (memoisation) instead.

</details>
<details>
<summary><strong>Q:</strong> What's the canonical "trap" in maximum-subarray D&C?</summary>

**A:** Forgetting the cross-midpoint case. The answer might span the boundary between halves; you must scan outward from `mid` separately.

</details>
<details>
<summary><strong>Q:</strong> Why is randomised quicksort considered D&C even though it's not balanced?</summary>

**A:** Each recursive call is on a strictly-smaller subarray. Randomisation ensures *expected* balance; the structure is still divide-then-recurse-then-combine.

</details>

## Code template

```python
def divide_and_conquer(problem):
    if base_case(problem):
        return solve_directly(problem)

    sub_problems = divide(problem)                          # split
    sub_solutions = [divide_and_conquer(sp) for sp in sub_problems]
    return combine(sub_solutions)                           # merge

# Concrete: max-subarray D&C
def max_subarray(A, lo, hi):
    if lo == hi: return A[lo]
    mid = (lo + hi) // 2
    return max(
        max_subarray(A, lo, mid),
        max_subarray(A, mid + 1, hi),
        max_crossing(A, lo, mid, hi),                       # the cross case!
    )
```

## Pattern triggers

- **Sort an array faster than `O(n²)`** → merge sort, quicksort
- **Search a sorted array** → binary search (D&C with one branch)
- **Multiply huge integers** → Karatsuba
- **Multiply matrices for huge `n`** → Strassen
- **FFT / signal processing / polynomial multiply** → Cooley-Tukey D&C
- **Closest pair of points** → x-sorted + cross-strip D&C, `O(n log n)`
- **"Find max in range" with overlapping subproblems** → DP, not D&C
- **Parallelisable workload** → D&C, since subproblems are independent
- **Combine cost dominates** → check if `f(n)` is too heavy; might be Master case 3

***

# Cross-links

- **Foundations:** [Recurrence Relations and Master Theorem](/cortex/data-structures-and-algorithms/foundations-recurrence-relations-and-master-theorem) — the analysis tool for every D&C algorithm.
- **Sibling strategies:** [Dynamic Programming](/cortex/data-structures-and-algorithms/algorithms-by-strategy-dynamic-programming-index) (when subproblems overlap), [Greedy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-greedy-introduction-to-greedy-algorithms) (when locally-optimal happens to be globally-optimal).
- **Used by:** [Sorting](/cortex/data-structures-and-algorithms/sorting-and-searching-sorting-index) (merge sort, quicksort), [Binary Search](/cortex/data-structures-and-algorithms/sorting-and-searching-searching-binary-search).

***

# Final Takeaway

Divide-and-conquer turns `O(n²)` into `O(n log n)` (or even `O(n^1.585)`) by trading recursive calls for the chance to throw away work. Three patterns to internalise:

1. **Three steps, every time.** Divide, conquer, combine. The shape is identical; only the choice of `a`, `b`, and `f(n)` changes.
2. **The combine step is where most thought goes.** The recursion is mechanical; the "merge two halves correctly" or "handle the cross-midpoint case" is where bugs live.
3. **Master theorem closes the analysis.** Once the recurrence is in standard form, the closed-form complexity is one substitution away. No excuse for "I think this is `O(n log n)` but I'm not sure".
