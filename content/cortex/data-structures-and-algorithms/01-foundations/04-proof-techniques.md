---
title: Proof Techniques
summary: Induction, contradiction, and loop invariants — the three tools that turn "I tested it on a few inputs and it looks right" into "this code is correct on all inputs". The engineer's daily-use math.
prereqs:
  - foundations-asymptotic-analysis
---

# 4. Proof Techniques

## The Hook

You write a binary search. It works on `[1, 2, 3, 4, 5]`. It works on `[5]`. It works on the empty array. You ship it.

Three weeks later, production. Your binary search is called on an array of a million elements, and silently returns the wrong index for some queries. Logs are clean. Tests are green. The bug is in a corner case you never tested.

A few unit tests cover *finite* inputs. They don't prove anything about the *infinitely* many inputs you didn't test. The gap between "works on the cases I tried" and "works on all cases" is the gap that proof techniques close. Engineers who can write a loop invariant — even informally, in a comment — catch the bug before it ships. Engineers who can't, ship and patch.

This chapter is the engineer's-eye view of proof. Not the textbook lemma-axiom-theorem version. The version where you can stare at a piece of code, write down what's true at the top of every loop iteration, and use that to argue *why the code is correct on every input it might ever see*. The pay-off is real: every difficult bug you ever fix is a place where the code's actual behaviour drifted from the invariant the author *thought* held.

---

## Table of contents

1. [Why "I tested it" isn't a proof](#why-i-tested-it-isnt-a-proof)
2. [Mathematical induction](#mathematical-induction)
3. [Proof by contradiction](#proof-by-contradiction)
4. [Loop invariants — the engineer's daily tool](#loop-invariants-the-engineers-daily-tool)
5. [Worked example: binary search](#worked-example-binary-search)
6. [Worked example: merge sort termination + correctness](#worked-example-merge-sort)
7. [A runnable demo: invariant-checked code](#a-runnable-demo)
8. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
9. [Production reality](#production-reality)
10. [Practice ladder](#practice-ladder)
11. [Cross-links](#cross-links)
12. [Final takeaway](#final-takeaway)

***

# Why "I tested it" isn't a proof

Tests check a finite set of inputs. The set of inputs the code might see in production is, for any non-trivial program, *infinite*. No finite collection of tests can cover an infinite set unless every test is *deductively* representative of all the cases it stands in for.

A proof, in this chapter's sense, is an argument that the code's behaviour holds for *all* inputs in some class — not because we ran each one, but because the structure of the argument forces the conclusion. The three tools we'll use:

| Tool | Use when | Strength |
|---|---|---|
| **Induction** | The thing you're proving has a repeating structure (recursive call, loop iteration, integer counter). | The workhorse for recursive correctness and complexity. |
| **Contradiction** | You want to prove "X is impossible" or "no Y has property Z". | Often shorter than a direct proof. |
| **Loop invariants** | The thing you're proving is "this code computes the right answer". | The most concrete, most engineer-useful tool. |

We'll cover each, and then spend the bulk of the chapter on loop invariants because they're the technique you'll actually use in code review.

***

# Mathematical induction

Induction proves a statement `P(n)` for all integers `n ≥ n₀` by chaining two simpler claims.

> **Induction.**
> 1. **Base case:** Prove `P(n₀)` directly.
> 2. **Inductive step:** Assume `P(k)` for some `k ≥ n₀`. Use that to prove `P(k+1)`.
>
> If both hold, then `P(n)` is true for every `n ≥ n₀`.

The intuition: imagine an infinite line of dominoes. Knock the first one over (base case). Show that any standing domino, when knocked, knocks the next one (inductive step). Conclusion: every domino falls.

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
  D0["P(n₀)<br/>base case"]
  D1["P(n₀+1)"]
  D2["P(n₀+2)"]
  D3["P(n₀+3)"]
  D4["…"]
  D0 -->|step| D1 -->|step| D2 -->|step| D3 -->|step| D4
  style D0 fill:#bbf7d0,stroke:#16a34a
```

<p align="center"><strong>Induction is the chain-of-dominoes proof. Base case knocks the first; the inductive step knocks each successive one.</strong></p>

## Worked example: sum of first n integers

> **Claim:** For all `n ≥ 0`, the sum `0 + 1 + 2 + … + n = n(n+1)/2`.

**Base case** (`n = 0`): the sum is `0`. The formula gives `0 · 1 / 2 = 0`. ✓

**Inductive step.** Assume `0 + 1 + … + k = k(k+1)/2`. Show `0 + 1 + … + k + (k+1) = (k+1)(k+2)/2`.

Start from the assumption:

```
0 + 1 + … + k = k(k+1)/2
```

Add `(k+1)` to both sides:

```
0 + 1 + … + k + (k+1) = k(k+1)/2 + (k+1)
                     = (k+1) · (k/2 + 1)
                     = (k+1) · (k + 2) / 2
                     = (k+1)(k+2)/2  ✓
```

Done.

This formula is what makes "the worst-case quicksort recurrence sums to `Θ(n²)`" rigorous — you'll see it cited there.

## Strong induction

A variant: instead of assuming `P(k)` to prove `P(k+1)`, you assume `P(j)` for *all* `n₀ ≤ j ≤ k` to prove `P(k+1)`. Strong induction is useful when the inductive step needs information from earlier than just the immediate predecessor — for example, proving correctness of recursive algorithms whose recursion is on `n/2`, or that every integer ≥ 2 has a prime factorization.

You'll meet strong induction every time we prove a divide-and-conquer recurrence's closed form. The classic Fibonacci-time proof — that `fib(n) ≥ φ^(n-2)` for `n ≥ 2`, where `φ` is the golden ratio — needs strong induction because `fib(k+1)` depends on both `fib(k)` and `fib(k-1)`.

***

# Proof by contradiction

> **Contradiction.** To prove `P`: assume `not P` and derive an absurdity. Conclude `P`.

The classic example: prove there are infinitely many primes.

> **Claim:** there are infinitely many primes.
>
> **Proof.** Assume for contradiction that there are only finitely many primes — call them `p₁, p₂, …, p_n`. Consider the number `N = p₁ · p₂ · … · p_n + 1`. Either `N` is prime — but then `N` is a prime not in our list, contradicting "those are all the primes". Or `N` is composite — but then `N` has some prime factor `p`, and that `p` must be one of our `p_i` — but `N` is `1 mod p_i` for every `p_i` in the list, so no `p_i` divides `N`, contradicting "every composite has a prime factor in the list". Either way: contradiction. So our assumption was wrong. There must be infinitely many primes. □

The shape is universal: assume the negation, follow the implications, find that the world breaks, conclude the original claim.

In algorithm correctness, contradiction is most often used to prove **lower bounds** ("no algorithm can sort with fewer than `n log n` comparisons" — assume one could; derive that the decision tree would have fewer than `n!` leaves; contradiction with the pigeonhole principle) or **uniqueness** ("there's exactly one shortest path with these properties — assume two; show they'd have to be equal; contradiction").

***

# Loop Invariants — The Engineer's Daily Tool

A **loop invariant** is a condition that holds:

1. **Initialization** — before the loop's first iteration.
2. **Maintenance** — at the top of every iteration (assuming it held at the top of the previous one).
3. **Termination** — when the loop exits, combined with the loop's exit condition, it tells you the result is correct.

Once you have an invariant, the rest writes itself. You prove (1) directly. You prove (2) by induction. You prove (3) by combining the invariant with the exit condition.

This is the engineer's most-used proof technique because *you can write the invariant as a comment in the code*. Reading code with a clearly-stated invariant feels like reading code with the proof bolted on. Every reviewer can check the invariant against the code; every off-by-one bug is a place the invariant didn't hold.

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
  S["Start of loop"]
  I["Invariant<br/>holds here"]
  COND{"Loop<br/>condition?"}
  BODY["Body executes<br/>(may temporarily<br/>break invariant)"]
  END["After loop"]
  S --> I --> COND
  COND -->|true| BODY
  BODY --> I
  COND -->|false| END
  style I fill:#fef9c3,stroke:#f59e0b
  style END fill:#bbf7d0,stroke:#16a34a
```

<p align="center"><strong>The invariant holds at the entry to the body and at the exit from the body. The body might temporarily break it, but it must restore it before the next iteration.</strong></p>

***

# Worked example: binary search

Binary search is the smallest non-trivial algorithm where loop invariants pay off. The standard implementation:

```python
def binary_search(arr, target):
    lo, hi = 0, len(arr) - 1
    while lo <= hi:
        mid = lo + (hi - lo) // 2
        if arr[mid] == target:
            return mid
        if arr[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1
```

Looks fine. It is fine. Let's prove it — and in proving it, find the off-by-ones that the next person to modify it might introduce.

**Loop invariant:** *if `target` is in `arr`, then it is in `arr[lo..hi]`* (the inclusive subarray from index `lo` to index `hi`).

Equivalently: every index `i` with `i < lo` or `i > hi` has `arr[i] != target`. The subarray we still need to search shrinks each iteration; the discarded portions never had the answer.

**Initialization.** Before the loop, `lo = 0` and `hi = len(arr) - 1`. The candidate range is the whole array. Trivially, if `target` is in `arr`, it's in `arr[0..len(arr)-1]`. ✓

**Maintenance.** Assume the invariant holds at the top of the iteration, with current range `[lo, hi]`. We compute `mid = lo + (hi - lo) // 2`, so `lo ≤ mid ≤ hi`.

- If `arr[mid] == target`: we return `mid`, exiting correctly. (Not maintenance, but a *correct exit*.)
- If `arr[mid] < target`: since `arr` is sorted, every `i ≤ mid` has `arr[i] ≤ arr[mid] < target`, so `target` cannot be at index `i ≤ mid`. The invariant was "if `target` is in `arr`, it's in `arr[lo..hi]`". Combined with the new fact that it's not at any `i ≤ mid`, it must be in `arr[mid+1..hi]`. Setting `lo = mid + 1` makes the new range exactly that. The invariant holds. ✓
- If `arr[mid] > target`: symmetric — every `i ≥ mid` has `arr[i] > target`, so the answer (if any) is in `arr[lo..mid-1]`. Setting `hi = mid - 1` makes that the new range. ✓

**Termination.** The loop exits when `lo > hi`. Combined with the invariant: if `target` is in `arr`, it's in `arr[lo..hi]` — but `arr[lo..hi]` is empty when `lo > hi`. Contradiction. So `target` is not in `arr`. Returning `-1` is correct. ✓

We've also shown the loop *terminates*: each iteration either exits via the `==` check or strictly decreases `hi - lo`. After at most `log₂(len(arr))` iterations, `hi - lo` reaches `-1` and the loop exits.

> *Predict before reading on:* what would happen if the line `mid = lo + (hi - lo) // 2` were instead `mid = (lo + hi) // 2`?

For most inputs, identical behaviour. For very large arrays where `lo + hi > 2³¹` (roughly `len ≥ 2³¹` on 32-bit `int`), the addition overflows to a negative number, the `//` produces nonsense, and the search reads out-of-bounds. This bug shipped in Java's `Arrays.binarySearch` for nine years before [Joshua Bloch found it in 2006](https://research.googleblog.com/2006/06/extra-extra-read-all-about-it-nearly.html). Tests on small arrays don't catch it; the loop invariant *would* have, because `lo + (hi - lo) // 2` is the version that satisfies `lo ≤ mid ≤ hi` for all valid `lo, hi` with no overflow.

***

# Worked example: merge sort

Merge sort sorts an array recursively. The whole algorithm is two parts: the recursion that splits into halves, and the merge that combines two sorted halves. Both have invariants worth stating.

## Termination

Every recursive call is on a strictly smaller array (`len(arr) // 2 < len(arr)` for `len ≥ 2`; `len ≤ 1` is the base case). Strong induction on array length: base case `len ≤ 1` returns immediately; for `len ≥ 2`, recursive calls are on arrays of length `< len`, which terminate by inductive hypothesis. Therefore merge sort always terminates.

## Correctness

The recursion is correct if the merge step is correct. The merge step:

```python
def merge(left, right):
    out = []
    i = j = 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            out.append(left[i]); i += 1
        else:
            out.append(right[j]); j += 1
    out.extend(left[i:])
    out.extend(right[j:])
    return out
```

**Invariant for the loop:** `out` is a sorted permutation of `left[0..i] + right[0..j]`.

**Initialization.** `out = []`, `i = j = 0`. The empty list is trivially a sorted permutation of nothing. ✓

**Maintenance.** Assume the invariant holds. The next iteration appends the smaller of `left[i]` or `right[j]`. WLOG it's `left[i]`. We append `left[i]` and increment `i`. The new `out` is the old `out` (which was sorted and contained `left[0..i] + right[0..j]`) followed by `left[i]`. We need to show this is still sorted.

The old `out` was sorted, so its last element was the maximum of `left[0..i] + right[0..j]`. We're appending `left[i]`, which is `≤` everything in `left[i+1..]` (because `left` is sorted) and *also* `≤` `right[j]` (the loop just chose `left[i]`). For the *new* `out` to be sorted, we need `left[i]` `≥` the previous max.

Hmm — is this true? The previous max of `out` is some element from `left[0..i] + right[0..j]`. Is it `≤ left[i]`?

- If it came from `left[0..i]`, yes — `left` is sorted, so `left[k] ≤ left[i]` for `k < i`.
- If it came from `right[0..j]`, then it's some `right[k]` for `k < j`. We need `right[k] ≤ left[i]`. We know `right[k]` was appended in some previous iteration where it was less than the `left` index *at that time*; since `i` has only grown, and `left` is sorted, `right[k] < left[i_at_that_time] ≤ left[i]`. ✓

So the invariant is maintained.

**Termination.** The loop exits when `i == len(left)` or `j == len(right)`. The two `extend` lines append the remainder of whichever list still has elements. The result `out` is a sorted permutation of `left + right`. ✓

The recursive call is correct by strong induction on array length, with the merge step as the inductive step's worker.

***

# A runnable demo

The code below implements binary search with explicit invariant assertions. Run it. Every assertion either holds (invariant maintained, code correct) or fires (bug — somewhere the implementation drifted from the spec).

```python run
def binary_search(arr, target):
    # Precondition: arr is sorted ascending.
    assert all(arr[i] <= arr[i + 1] for i in range(len(arr) - 1)), "arr must be sorted"

    lo, hi = 0, len(arr) - 1
    while lo <= hi:
        # Invariant: if target is in arr, it is in arr[lo..hi].
        # Equivalently: for all i < lo, arr[i] != target; for all i > hi, arr[i] != target.
        if lo > 0:
            assert arr[lo - 1] < target, f"invariant broken: arr[{lo-1}]={arr[lo-1]} >= target={target}"
        if hi < len(arr) - 1:
            assert arr[hi + 1] > target, f"invariant broken: arr[{hi+1}]={arr[hi+1]} <= target={target}"

        mid = lo + (hi - lo) // 2
        if arr[mid] == target:
            return mid
        if arr[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1

    # Postcondition: target is not in arr.
    assert target not in arr, f"contract violation: should have found {target}"
    return -1


if __name__ == "__main__":
    import random
    random.seed(42)

    # Spot tests.
    assert binary_search([], 1) == -1
    assert binary_search([1], 1) == 0
    assert binary_search([1], 2) == -1
    assert binary_search([1, 3, 5, 7, 9], 5) == 2
    assert binary_search([1, 3, 5, 7, 9], 4) == -1
    assert binary_search([1, 3, 5, 7, 9], 9) == 4

    # Random stress test — invariants do the verification, not the test cases.
    for _ in range(1000):
        n = random.randint(0, 100)
        arr = sorted(random.randint(0, 50) for _ in range(n))
        target = random.randint(0, 50)
        result = binary_search(arr, target)
        if target in arr:
            assert result != -1 and arr[result] == target
        else:
            assert result == -1

    print("All 1000 random tests pass.")
    print("Invariants held throughout. Binary search is correct on every input it saw.")
```

```java run
import java.util.*;

public class Main {
    static int binarySearch(int[] arr, int target) {
        // Precondition: arr is sorted.
        for (int i = 0; i < arr.length - 1; i++) {
            assert arr[i] <= arr[i + 1] : "arr must be sorted";
        }

        int lo = 0, hi = arr.length - 1;
        while (lo <= hi) {
            // Invariant: if target is in arr, it's in arr[lo..hi].
            if (lo > 0)            assert arr[lo - 1] < target;
            if (hi < arr.length - 1) assert arr[hi + 1] > target;

            int mid = lo + (hi - lo) / 2;
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        // Run with: java -ea Main    (-ea enables assertions)
        Random rng = new Random(42);
        for (int trial = 0; trial < 1000; trial++) {
            int n = rng.nextInt(100);
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) arr[i] = rng.nextInt(50);
            Arrays.sort(arr);
            int target = rng.nextInt(50);
            int result = binarySearch(arr, target);
            if (Arrays.binarySearch(arr, target) >= 0) {
                assert result != -1 && arr[result] == target;
            } else {
                assert result == -1;
            }
        }
        System.out.println("All 1000 random tests pass; invariants held throughout.");
    }
}
```

The assertions never fire, because the implementation respects the invariant. If you change `mid = lo + (hi - lo) // 2` to `mid = lo + (hi - lo) // 2 + 1` (a subtle off-by-one), some assertion will fire on some test case — the loop invariant catches the bug *before* the wrong index gets returned.

***

# Edge cases and pitfalls

- **The "obvious" base case isn't always the smallest one.** For an algorithm that works on arrays, the empty array (`len = 0`) is usually a special case worth listing in the base case. Off-by-one bugs love to live there.
- **The inductive step assumes the wrong thing.** A common mistake: assuming `P(k)` and proving `P(k+1)` *under additional, unjustified assumptions*. If your inductive step uses a fact you haven't established, it isn't a valid step.
- **Strong vs weak induction confusion.** Some recurrences (Fibonacci, divide-and-conquer) need strong induction. If your inductive step refers to *multiple* prior cases (not just `k`), state that you're using strong induction.
- **Loop invariants that hold but don't say anything useful.** "*The variables exist*" is a true invariant. It's also useless. The invariant should be just strong enough that the exit condition implies the postcondition. Too weak and you can't conclude anything; too strong and maintenance is hard to prove.
- **Forgetting to prove termination.** A loop that maintains its invariant *forever* is still wrong — it never returns. For every loop, separately argue *why* it terminates: typically a strict decrease of some non-negative quantity (`hi - lo`, the size of a remaining sublist, etc.).
- **Asserting the postcondition in the postcondition check.** The runnable demo's `assert target not in arr` at the end is a sanity check, not a proof — you've used a separate check to catch contract violations. In production code, you'd remove the linear scan (it'd dominate the cost) but keep the loop invariant as a comment.
- **Proof by contradiction that just renames the goal.** "Assume `not P`. Then `not P` would be true. But that's `not P`, contradicting `P`." This is a fake proof. The contradiction has to come from independent facts, not from the assumption itself.
- **Confusing necessary and sufficient.** "Every sorted array is binary-searchable" is true. "Every binary-searchable array is sorted" is *also* true (with one exception: a constant array). But the two statements are different and proving one doesn't prove the other.

***

# Production reality

- **Invariant comments in production code.** Look at the source of CPython's `Lib/heapq.py`, the Linux kernel's `lib/rbtree.c`, or the JDK's `TreeMap.java` — every non-trivial loop has a comment block stating the invariant. The invariant *is* the documentation. When the code drifts (a bug fix, a refactor, a performance tweak), the invariant comment is the first thing to update; if it's still true after the change, you've kept correctness.
- **Contract programming.** Languages and frameworks formalise the invariant idea: D's `invariant`, Java's `assert` (with `-ea`), C's `assert.h`, Eiffel's `require`/`ensure`, Rust's `debug_assert!`. The pattern is the same: the invariant lives in the code, runs in development/debug builds, and is compiled out in release builds. Many subtle production bugs are caught by the assert firing in CI.
- **Proof assistants in industry.** Coq, Lean, Isabelle/HOL, and Agda are used for *machine-checked* proofs of correctness. CompCert is a C compiler whose optimisations are formally proved correct; seL4 is a microkernel with a machine-checked proof of functional correctness. These are for systems where a wrong answer literally costs lives or large amounts of money — flight control, medical devices, cryptocurrency consensus. For most production code, well-stated invariants in code review are the practical sweet spot.
- **The Rust borrow checker is an invariant checker.** Every Rust program that compiles has had its memory-safety invariants *proved*: at every point, every reference is either unique-and-mutable or shared-and-read-only. The compiler does the proof for you. The reason Rust has so many advanced fans is that this single invariant rules out half the bug catalogue of C++.
- **TLA+ and model checking.** Amazon Web Services, Microsoft Azure, and several other distributed-systems teams write their service designs in TLA+ — a formal specification language — and use TLC (the TLA+ model checker) to verify safety and liveness invariants of their distributed protocols. The investment is high; the pay-off is catching design-level bugs (race conditions, split-brain scenarios, data loss under partition) *before* implementation. [Leslie Lamport](https://lamport.azurewebsites.net/tla/tla.html) won the Turing Award partly for inventing TLA+.

***

# Practice ladder

1. **Prove by induction.** Show that `1 + 3 + 5 + … + (2n − 1) = n²` for all `n ≥ 1`.
   > *Hint:* base case `n=1`: `1 = 1²`. Inductive step: assume sum to `2k-1` is `k²`. Add `(2(k+1) − 1) = 2k + 1`. Total: `k² + 2k + 1 = (k+1)²`. ✓

2. **Find the broken proof.** Someone claims to prove "all horses are the same colour" by induction on the number of horses. Base case `n=1`: a single horse is trivially the same colour as itself. Inductive step: assume any group of `k` horses is the same colour. Take a group of `k+1` horses. Remove one to get a group of `k` (same colour by IH). Put it back, remove a different one to get another group of `k` (same colour). Therefore the original `k+1` are all the same colour. Find the bug.
   > *Hint:* the inductive step assumes the two groups of `k` *overlap*. For `k=1`, removing one horse from a 2-horse group gives a 1-horse "group" each time, and the two singleton groups don't overlap. The induction step is invalid for the base-case-to-step transition. Total mathematical bug.

3. **Loop invariant: `findMax`.** Write a loop invariant for the standard "find max in array" loop. Then use it to argue the post-condition (the returned value is the maximum).
   ```python
   def find_max(arr):
       best = arr[0]
       for i in range(1, len(arr)):
           if arr[i] > best:
               best = arr[i]
       return best
   ```
   > *Hint:* invariant: `best == max(arr[0..i])` (where `i` is the loop index). Initialization: `best = arr[0] = max(arr[0..1])`. Maintenance: after the iteration, `best = max(arr[0..i+1])`. Termination: `i = len(arr)`, so `best = max(arr)`.

4. **Proof by contradiction.** Prove that if `n²` is even, then `n` is even.
   > *Hint:* assume `n` is odd. Then `n = 2k+1` for some integer `k`. So `n² = 4k² + 4k + 1`, which is odd. But this contradicts `n²` being even. So `n` is even.

5. **Find the off-by-one via invariants.** This loop is supposed to count occurrences of `target` in a sorted array. State the invariant the implementation tries to maintain. Spot the bug.
   ```python
   def count_occurrences(arr, target):
       lo, hi = 0, len(arr)
       while lo < hi:
           mid = (lo + hi) // 2
           if arr[mid] < target: lo = mid + 1
           else: hi = mid
       left = lo
       lo, hi = 0, len(arr)
       while lo < hi:
           mid = (lo + hi) // 2
           if arr[mid] <= target: lo = mid + 1
           else: hi = mid
       right = lo
       return right - left + 1   # ← bug here
   ```
   > *Hint:* the two while loops correctly find `left` (first index `≥ target`) and `right` (first index `> target`). The number of occurrences is `right - left`, *not* `right - left + 1`. The `+ 1` is an off-by-one that miscounts every result. The invariants of each loop are correct; the wrong assembly of their answers is the bug.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Once these proof patterns are second nature, you can sketch a correctness argument as fast as you can write code.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Two parts of a proof by induction?</summary>

**A:** **Base case** — prove `P(n₀)` directly. **Inductive step** — assume `P(k)`, prove `P(k+1)`.

</details>
<details>
<summary><strong>Q:</strong> Difference between weak and strong induction?</summary>

**A:** Weak: assume `P(k)` to prove `P(k+1)`. Strong: assume `P(j)` for all `n₀ ≤ j ≤ k` to prove `P(k+1)`. Strong is needed for divide-and-conquer correctness and for Fibonacci-style recurrences.

</details>
<details>
<summary><strong>Q:</strong> Three properties a loop invariant must satisfy?</summary>

**A:** **Initialization** (holds before first iteration), **Maintenance** (held entering iteration → holds entering next), **Termination** (combined with exit condition, implies the postcondition).

</details>
<details>
<summary><strong>Q:</strong> Loop invariant for binary search?</summary>

**A:** "If `target` is in `arr`, then it is in `arr[lo..hi]` (the inclusive subarray still being searched)."

</details>
<details>
<summary><strong>Q:</strong> Why <code>mid = lo + (hi - lo) / 2</code> instead of <code>(lo + hi) / 2</code>?</summary>

**A:** Avoids integer overflow when `lo + hi > Int.MaxValue`. Joshua Bloch found this bug in Java's `Arrays.binarySearch` after nine years.

</details>
<details>
<summary><strong>Q:</strong> Structure of a proof by contradiction?</summary>

**A:** Assume `not P`. Derive an absurdity (a contradiction with established facts). Conclude `P`.

</details>
<details>
<summary><strong>Q:</strong> Why must you prove termination separately from correctness?</summary>

**A:** Correctness says "*if* the function returns, the answer is right". Termination says "the function returns". A loop that maintains its invariant forever is still wrong.

</details>
<details>
<summary><strong>Q:</strong> Standard pattern for proving termination?</summary>

**A:** Identify a non-negative integer-valued *measure* that strictly decreases each iteration (or each recursive call). Since it can't go below zero, the loop terminates.

</details>
<details>
<summary><strong>Q:</strong> Where do invariants live in production code?</summary>

**A:** As comments on the loop, as `assert` statements (debug builds), and in formal contract languages (D's `invariant`, Eiffel's `require`/`ensure`). Compile out in release builds.

</details>
<details>
<summary><strong>Q:</strong> Why does the Rust borrow checker count as an invariant checker?</summary>

**A:** It machine-proves: at every program point, every reference is either *unique-and-mutable* or *shared-and-read-only*. Ruling out half the C++ bug catalogue.

</details>

## Code template

```python
# Loop-invariant template — annotate the invariant directly on the loop.

def binary_search(arr, target):
    """
    Precondition: arr is sorted ascending.
    Postcondition: returns i with arr[i] == target, or -1 if absent.
    """
    assert all(arr[i] <= arr[i + 1] for i in range(len(arr) - 1))

    lo, hi = 0, len(arr) - 1
    while lo <= hi:
        # Invariant: if target is in arr, it is in arr[lo..hi].
        # Termination measure: hi - lo (strictly decreases each iteration).
        mid = lo + (hi - lo) // 2          # avoid overflow
        if arr[mid] == target: return mid
        if arr[mid] < target:  lo = mid + 1
        else:                  hi = mid - 1
    # Postcondition: target is not in arr; lo > hi.
    return -1


# Induction-on-length skeleton for recursive correctness:
#
# def f(arr):
#     if len(arr) <= 1: return base_case(arr)         # base
#     left  = f(arr[:mid])                            # IH: f correct for size < n
#     right = f(arr[mid:])                            # IH: same
#     return combine(left, right)                     # show: combine preserves the postcondition
```

## Pattern triggers

- **Loop with off-by-one suspicion** → write the invariant as a comment; check Initialization / Maintenance / Termination
- **Recursive function correctness** → strong induction on input size
- **"Prove this is impossible" / lower bound** → contradiction
- **"Prove infinite supply / unbounded count"** → contradiction (assume finite, derive a contradiction)
- **Operation that shrinks a measure** → use that measure as the termination argument
- **Concurrent/shared state** → Rust's borrow checker, or formal model checking (TLA+)
- **`if (map.containsKey(k)) map.put(k, v)`** → not atomic under concurrency; use atomic compound operations
- **Production assertion fires in CI** → invariant violated; fix is to either correct the code or weaken the invariant

***

# Cross-links

- **Prerequisite:** [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — the language proofs are written in.
- **Used everywhere later** — every "this algorithm is correct" claim in the rest of the book leans on induction or loop invariants, even when the proof isn't spelled out. This chapter teaches you to read those claims like an engineer.
- **Closely related:** [Recurrence Relations](/cortex/data-structures-and-algorithms/foundations-recurrence-relations-and-master-theorem) — the substitution method is induction in action.
- **Production usage:** [DSA in Real Systems: Linux Red-Black Tree](/cortex/data-structures-and-algorithms/dsa-in-real-systems-linux-red-black-tree-in-the-cfs-scheduler) — *stub* — the kernel's RB-tree maintains five formal invariants every operation must preserve. Reading the source is a master-class in invariant-driven code.

***

# Final Takeaway

Proof techniques turn "I tested some inputs" into "this code is correct on every input it could ever see". Three patterns to internalise:

1. **Induction is the workhorse for recursion.** Every recursive function's correctness lives in a base case + inductive step. If you can articulate both, you can prove the function. If you can't articulate either, you're guessing.
2. **Loop invariants are the engineer's daily tool.** Stating the invariant as a comment turns a confusing loop into self-documenting code. Most off-by-one bugs are places the invariant *should* hold but doesn't.
3. **Termination is a separate proof.** Correctness ("if the function returns, the answer is right") and termination ("the function returns") are independent. Prove both. Most infinite loops in production are correctness-correct functions whose authors forgot to argue termination.

The next chapter turns from *correctness* to *performance reality*: the gap between Big-O and wall-clock time, and the memory model that explains where it comes from.
