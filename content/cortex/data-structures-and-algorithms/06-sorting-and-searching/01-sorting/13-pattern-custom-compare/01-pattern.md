---
title: "Pattern: Custom Compare"
summary: "Sort by a derived key instead of natural order — bit count, frequency, composite string concatenation, or any monotone criterion."
prereqs:
  - 06-sorting-and-searching/01-sorting/01-introduction-to-sorting
---

# Understanding Custom Comparators

A **comparator** is a function (or callable object) that takes two elements `a` and `b` and returns:

- **Negative** if `a` should come before `b`.
- **Zero** if `a` and `b` are equivalent.
- **Positive** if `a` should come after `b`.

The exact convention varies by language — some use `(a, b) → bool` returning "is `a < b`?" — but the underlying idea is the same: the algorithm asks "which one comes first?" and the comparator answers.

> 🖼 Diagram — The comparator interface. Every sort algorithm in this section can be adapted to a new ordering rule by swapping out the comparator.
```d2
direction: right

elem_a: "a (one element)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
elem_b: "b (another element)" {style.fill: "#fde68a"; style.stroke: "#d97706"}
cmp: "comparator(a, b)" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
result: "negative / zero / positive"

elem_a -> cmp
elem_b -> cmp
cmp -> result
```

<p align="center"><strong>The comparator interface. Every sort algorithm in this section can be adapted to a new ordering rule by swapping out the comparator.</strong></p>

The comparator must implement a **total order**:
- *Antisymmetric* — `cmp(a, b)` and `cmp(b, a)` should have opposite signs.
- *Transitive* — if `a < b` and `b < c`, then `a < c`.
- *Total* — every pair has a defined ordering (not always equal).

Violating any of these breaks the sort — you'll get incorrect results or, worse, infinite loops in some algorithms. The most common bug is **forgetting to handle ties**: a comparator that returns `0` for equal-key elements is correct, but one that always returns `-1` or always `1` is not.

---

## Why the Sort Algorithm Doesn't Care What You Compare

Every comparison sort in this section — bubble, selection, insertion, quicksort, merge sort, heapsort — uses comparisons like `arr[i] < arr[j]` exactly four to ten times. *Replace those comparisons with `cmp(arr[i], arr[j]) < 0`* and the algorithm sorts by the comparator's rule instead of the natural numeric order.

This is what library `sort` functions do. They have a single implementation (typically quicksort or TimSort) and accept a comparator as an argument. The user supplies the comparator; the algorithm does the rest.

---

## A Concrete Example

Sort points `[(3, 1), (1, 2), (2, 4), (1, 1)]` by x-coordinate, breaking ties by y-coordinate.

The comparator: "compare `a.x` to `b.x`; if equal, compare `a.y` to `b.y`."

```python run viz=array viz-root=points
points = [(3, 1), (1, 2), (2, 4), (1, 1)]
points.sort(key=lambda p: (p[0], p[1]))   # tuple comparison gives lex order
print(points)   # [(1, 1), (1, 2), (2, 4), (3, 1)]
```

The `key=` parameter transforms each element into a sort-key, and Python compares tuples lexicographically. The same effect can be achieved with `functools.cmp_to_key` and an explicit comparator function.

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **Universal** | Works with any sorting algorithm — the algorithm just calls the comparator. |
| **Composable** | Multi-key sorts compose: `(primary_key, secondary_key, ...)`. |
| **Decouples sort from data** | The same sort works on integers, strings, structs, anything orderable. |

| Limitation | Detail |
|---|---|
| **Comparator overhead** | Function-call cost per comparison. Slower than inline `<` for primitives. |
| **Easy to bug** | Non-transitive or non-antisymmetric comparators silently produce wrong sorts. |
| **Stability depends on the algorithm** | A custom comparator preserves stability only if the underlying sort is stable. |

In practice, custom compare is used:
- Every time you `sort([{...}, {...}, ...])` with a non-trivial key.
- Database `ORDER BY` clauses (each column is a comparator stage).
- Sorting search results by relevance score, then date, then ID.
- Sorting versions, paths, custom domain objects.

---

## Key Takeaway

A comparator is a function that defines "which element comes first." Any sort algorithm can use any comparator. This decoupling is what makes sorting universal. Now we'll see the three syntactic styles for providing a comparator.

# The Three Styles

Most languages support three syntactic styles for custom compare. Each has its place.

---

## Style 1 — Operator Overloading

Make the *type* itself orderable by overloading `<` (or implementing the language's "comparable" interface). The sort uses the type's natural order.

**Use when:** the order is intrinsic to the type — e.g., `Money` always compares by amount, `Date` always compares chronologically.

**Languages:** C++ (`operator<`), Python (`__lt__`), Java (`Comparable<T>`).

```python run viz=array viz-root=arr
class Entry:
    def __init__(self, x, y):
        self.x, self.y = x, y
    def __lt__(self, other):
        return (self.x, self.y) < (other.x, other.y)

arr = [Entry(2, 3), Entry(1, 4), Entry(2, 1)]
arr.sort()   # uses __lt__
```

---

## Style 2 — Lambda / Inline Function

Pass an anonymous function inline at the call site. Most flexible; doesn't require modifying the type.

**Use when:** the order is *context-specific* — different parts of your code want different orderings of the same data.

```python run viz=array viz-root=arr
arr = [(2, 3), (1, 4), (2, 1)]
arr.sort(key=lambda t: (t[0], -t[1]))   # by x ascending, then y descending
```

---

## Style 3 — Comparator Class / Object

Define a class implementing the language's "Comparator" or "Compare" interface. Reusable; can hold state.

**Use when:** the comparator needs internal state (e.g., a frequency map, a pivot value) or is reused across many sort calls.

```python run viz=array viz-root=freq
from functools import cmp_to_key

class FrequencyComparator:
    def __init__(self, freq_map):
        self.freq = freq_map
    def __call__(self, a, b):
        if self.freq[a] != self.freq[b]:
            return self.freq[b] - self.freq[a]   # higher frequency first
        return a - b                              # tiebreak by value

freq = {1: 3, 2: 1, 3: 2}
arr = [1, 2, 3, 1, 1, 3]
arr.sort(key=cmp_to_key(FrequencyComparator(freq)))
```

---

## When to Use Each

| Need | Style |
|---|---|
| The type has *one* obvious ordering | Style 1 (overloading) |
| You need different orders in different places | Style 2 (lambda) |
| The comparator captures runtime state (freq map, pivot) | Style 3 (comparator object) |

In practice, lambdas are by far the most common in modern code. They're concise, readable, and don't pollute the type system. Operator overloading is reserved for types where the order is *the* natural ordering. Comparator classes are for stateful comparisons.

---

## Key Takeaway

Three styles, one underlying idea: provide a function that orders pairs. Pick the style that matches your context. Now we'll learn how to spot custom-compare problems.

# Identifying Custom-Compare Problems

Two diagnostic questions decide whether the custom-compare pattern fits.

| # | Question | If "yes," custom compare fits because... |
|---|---|---|
| **Q1** | Does the desired order require a *transformation* of the elements (not their raw values)? | The transformation produces a sort key. |
| **Q2** | Can the transformation be expressed as a function of the element alone (or with bounded extra state)? | A comparator can call the transformation deterministically. |

---

## A Useful Mental Model — Transform Then Compare

Most custom-compare problems boil down to:
1. Define a transformation function `t(elem) → key`.
2. Sort by `key` using the natural ordering on `key`.

Examples:
- "Sort by frequency" → `t(c) = (-frequency[c], c)` (negative because we want descending frequency, then character for tiebreaks).
- "Sort by digit count" → `t(n) = (number of digits in n, n)`.
- "Sort by distance to point P" → `t(point) = distance(point, P)`.

The transform-then-compare pattern collapses every custom-compare problem into "what's the right `t`?" Once you have `t`, the comparator is `cmp(a, b) = (t(a) < t(b))`.

---

## Common Phrasings

Custom compare is usually signalled by:
- "Sort by [some derived property]."
- "Sort by [primary], breaking ties by [secondary]."
- "Sort the array such that ..."
- "Find the largest / smallest ... where the ordering is ..."

If the problem describes an ordering that isn't the values' natural order, custom compare applies.

---

## Key Takeaway

Two checks — a transformation is needed, and the transformation is a function of each element — gate every custom-compare problem. Pass them both and the recipe is "transform, then sort by the natural order on the transform." Now four worked problems.

# Final Takeaway

Custom compare is the universal interface between sort algorithms and ordering rules. Three styles: operator overloading, lambda, comparator class. Two questions to ask: *what's the transformation?* and *is it a function of the element alone?*

Once you internalise this, every "sort by X" problem reduces to "find the right `t(elem) → key`," and the algorithm is whatever your standard library provides.

This closes the sorting section. You came in with bubble sort. You're leaving with:
- 10 different sorting algorithms across the comparison and counting families.
- Three classification systems: stability, in-place, adaptiveness.
- Two divide-and-conquer paradigms: pivot-based (quicksort) and split-based (merge sort).
- One linear-time sort (counting) and a near-linear three-way partition (Dutch flag).
- Quickselect for `O(n)` k-th element queries.
- The custom-compare pattern that makes any sort universal.

The next major topic in the algorithms section is **searching** — binary search and its many variants. Many of those variants assume the input is sorted, which is exactly what you've spent the last 12 lessons learning to do efficiently.

**Transfer challenge — close out the sorting section:** Write a custom comparator that sorts strings *case-insensitively* but breaks ties by the original case (uppercase before lowercase). For example, `["banana", "Apple", "apple", "Banana"]` should sort to `["Apple", "apple", "Banana", "banana"]`.

<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

```python run viz=array viz-root=arr
class Solution:
    def case_insensitive_sort(self, arr):
        # Sort by (lowercase, original) — lowercase comparison first, original is the tiebreaker.
        # Original case ordering: uppercase letters have lower ASCII values, so they come first.
        arr.sort(key=lambda s: (s.lower(), s))


arr = ["banana", "Apple", "apple", "Banana"]
Solution().case_insensitive_sort(arr)
print(arr)   # ['Apple', 'apple', 'Banana', 'banana']
```

The transformation `(s.lower(), s)` produces a tuple sort-key. Tuples compare lexicographically: first compare the lowercase strings, then break ties on the original (which puts uppercase before lowercase due to ASCII order).

This pattern — *case-insensitive primary, case-sensitive tiebreaker* — appears in file managers, text editors, and any system that displays user-facing sorted lists. **You just generalised every "sort by X with Y as tiebreaker" problem you'll ever see.**

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Pattern — missing, needs to be written -->
<!--       Guidance: umbrella H2 with the subsections below -->

<!-- TODO: Why Naive Isn't Enough — missing, needs to be written -->
<!--       Guidance: motivation for why the obvious approach fails -->

<!-- TODO: The Core Idea — missing, needs to be written -->
<!--       Guidance: one paragraph: the central trick -->

<!-- TODO: How the Pointers/Window Move — missing, needs to be written -->
<!--       Guidance: mechanics of the moving parts -->

<!-- TODO: The Generic Algorithm — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Generic Implementation — missing, needs to be written -->
<!--       Guidance: Python block + Java block of the skeleton -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table -->

<!-- TODO: Variants / Taxonomy — missing, needs to be written -->
<!--       Guidance: enumerate sub-shapes of this pattern -->

<!-- TODO: Recognition Checklist — missing, needs to be written -->
<!--       Guidance: 4-question diagnostic — the source of the Problem-section Diagnostic Questions -->

<!-- TODO: Canonical Example — missing, needs to be written -->
<!--       Guidance: fully worked example: brute force → optimised → template fit -->

<!-- TODO: Problems in This Category — missing, needs to be written -->
<!--       Guidance: table with links to the 02-problems/ files -->
