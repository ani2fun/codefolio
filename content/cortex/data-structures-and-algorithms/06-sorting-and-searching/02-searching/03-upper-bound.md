---
title: "Upper Bound"
summary: "<!-- TODO: summary -->"
---

# 3. Upper Bound

Lower bound found the first element `>= target`. **Upper bound finds the first element strictly `> target`.** One operator changed; the rest of the algorithm is identical. The use case differs: lower bound gives you "where would I insert this if I want it before equal values"; upper bound gives you "where would I insert this if I want it after equal values."

The pair together is the most powerful idiom in binary search: **upper bound minus lower bound = number of occurrences**. Two `O(log n)` queries, total `O(log n)`. The same idiom lets you slice out all elements equal to `target` (the range `[lower, upper)`), count elements in any value range `[a, b]` (`upper(b) - lower(a)`), and answer dozens of similar range queries.

By the end of this lesson you'll know upper bound, the one-character difference from lower bound, and the count-occurrences technique.

## Table of contents

1. [Understanding upper bound](#understanding-upper-bound)
2. [Lower vs upper — the one-character difference](#lower-vs-upper--the-one-character-difference)
3. [Implementation](#implementation)
4. [Complexity analysis](#complexity-analysis)
5. [Upper bound problem](#upper-bound-problem)

***

# Understanding Upper Bound

The **upper bound** of a target `t` in a sorted array is the **smallest index `i` such that `arr[i] > t`**. If no such index exists (every element is `<= t`), the upper bound is `n`.

```
arr = [1, 5, 10, 15, 20, 25]

target = 10  →  upper_bound = 3  (arr[3] = 15, first > 10)
target = 17  →  upper_bound = 4  (arr[4] = 20, first > 17)
target = 25  →  upper_bound = 6  (no element > 25; n = 6)
target = 0   →  upper_bound = 0  (arr[0] = 1, first > 0)
```

For arrays with duplicates:

```
arr = [1, 2, 2, 2, 3]

target = 2  →  lower_bound = 1  (first >= 2)
target = 2  →  upper_bound = 4  (first > 2)
count of 2s  = upper - lower = 4 - 1 = 3 ✓
```

The pair `[lower, upper)` is exactly the half-open range of indices where `arr[i] == target`. This is what makes upper bound useful — it gives you the *end* of a run of duplicates.

---

## The One-Character Difference

Compared to lower bound, upper bound changes one comparison:

```
# Lower bound:
if arr[mid] < target:
    low = mid + 1
else:                    # arr[mid] >= target
    high = mid

# Upper bound:
if arr[mid] <= target:    # ← one character changed: < becomes <=
    low = mid + 1
else:                     # arr[mid] > target
    high = mid
```

The semantic shift: lower bound puts equal elements in the "right half" (advance past mid); upper bound puts equal elements in the "left half" (keep going right of mid). Same skeleton; one character flips the meaning.

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **`O(log n)`** | Same as binary and lower bound. |
| **Always returns a valid index** | Never returns `-1`; returns `n` if no element exceeds target. |
| **Pairs with lower bound** | Together they enable count-occurrences and range queries in `O(log n)`. |

| Limitation | Detail |
|---|---|
| **Returns `n` for "all elements ≤ target"** | Caller must distinguish "valid result" from "out of range." |
| **Same off-by-one risks as lower bound** | The three-change package must be consistent. |

---

## Key Takeaway

Upper bound = first index strictly > target. One character changed from lower bound; same `O(log n)`. Together with lower bound, they form the count-and-range-query primitive. Now we'll formalise the difference.

***

# Lower vs Upper — The One-Character Difference

Stack the two algorithms side by side:

```python run
# LOWER BOUND — first index where arr[i] >= target
def lower_bound(arr, target):
    low, high = 0, len(arr)
    while low < high:
        mid = low + (high - low) // 2
        if arr[mid] < target:        # strict <
            low = mid + 1
        else:
            high = mid
    return low

# UPPER BOUND — first index where arr[i] > target
def upper_bound(arr, target):
    low, high = 0, len(arr)
    while low < high:
        mid = low + (high - low) // 2
        if arr[mid] <= target:       # <= (the one-character change)
            low = mid + 1
        else:
            high = mid
    return low
```

The change is in the partition condition. Lower bound puts `arr[mid] == target` in the "candidate" branch (`high = mid`); upper bound puts it in the "discard" branch (`low = mid + 1`).

```d2
direction: down

scenario: "Walking arr = [1, 2, 2, 2, 3], target = 2"

lower: "Lower bound\nencounter arr[mid] = 2 → 'maybe answer, look left'\nhigh = mid\n→ converges to index 1 (first 2)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}

upper: "Upper bound\nencounter arr[mid] = 2 → 'definitely not answer, look right'\nlow = mid + 1\n→ converges to index 4 (first element > 2)" {style.fill: "#fde68a"; style.stroke: "#d97706"}
```

<p align="center"><strong>The semantic difference. Equal elements are "candidates" for lower bound, "non-candidates" for upper bound.</strong></p>

---

## Count Occurrences with the Pair

The most powerful idiom: count occurrences of `target` is `upper(target) - lower(target)`.

```
arr = [1, 2, 2, 2, 3, 4]

lower_bound(arr, 2) = 1
upper_bound(arr, 2) = 4
count = 4 - 1 = 3 ✓

lower_bound(arr, 5) = 6  (would insert at end)
upper_bound(arr, 5) = 6
count = 6 - 6 = 0 ✓ (5 is absent)
```

This is `O(log n)` total — two binary searches. Without it, you'd need a linear scan or a hash map.

---

## Range Counts

Count elements in the half-open value range `[a, b)`:

```
count = upper_bound(arr, b - 1) - lower_bound(arr, a)
```

For `arr = [1, 2, 2, 3, 5, 7, 9, 10]` and range `[3, 8)`:
- Elements in `[3, 8)`: `3, 5, 7` — count 3.
- `upper_bound(arr, 7) = 6` (first element > 7 is 9 at index 6).
- `lower_bound(arr, 3) = 3` (first element ≥ 3 is 3 at index 3).
- count = `6 - 3 = 3` ✓.

This is the foundation of every "range query" data structure. Order statistics, range medians, prefix-sum lookups — they all build on this primitive.

---

## Key Takeaway

Lower and upper bound are the same algorithm with one comparison changed. Their difference gives count of occurrences in `O(log n)`. Now the implementation.

***

# Implementation


```python run
from typing import List

class Solution:
    def upper_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than or equal to target, the upper bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # > the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than or equal to target, then find
            # in the right subarray
            if arr[mid] <= target:
                low = mid + 1

            # If arr[mid] is greater than the target, then it may be the answer.
            # So, instead of high = mid - 1, we do high = mid to include mid in
            # the next search space
            else:
                high = mid

        # Return the upper bound index, it could be equal to len(arr)
        # if all elements are less than or equal to target
        return low
```

```java run
class Solution {
    public int upperBound(int[] arr, int target) {

        // Initialise starting index to 0
        int low = 0;

        // Initialise ending index to arr.length instead of arr.length -
        // 1 to cover the entire array as if all elements in the array
        // are less than or equal to target, the upper bound index would
        // be equal to arr.length
        int high = arr.length;

        // 'high' is exclusive (can be arr.length), so we use 'low <
        // high' instead of 'low <= high'. This loop finds the first
        // index where the element is > the target without going out of
        // bounds.
        while (low < high) {

            // Find the middle index
            int mid = low + (high - low) / 2;

            // If arr[mid] is less than or equal to target, then find
            // in the right subarray
            if (arr[mid] <= target) {
                low = mid + 1;
            }

            // If arr[mid] is greater than the target, then it may be the
            // answer. So, instead of high = mid - 1, we do high = mid to
            // include mid in the next search space
            else {
                high = mid;
            }
        }

        // Return the upper bound index, it could be equal to arr.length
        // if all elements are less than or equal to target
        return low;
    }
}
```


***

# Complexity Analysis

| Resource | Cost |
|---|---|
| **Time** | `O(log n)` |
| **Space** | `O(1)` |

Same as lower bound and binary search.

---

## Key Takeaway

Upper bound: `O(log n)` time, `O(1)` space, one character different from lower bound. Now the canonical exercise.

***

# Upper Bound Problem

---

## The Problem

Given a sorted array `arr` and `target`, return the index of the first element strictly `> target`.

```
Input:  arr = [1, 5, 10, 15, 20, 25], target = 10
Output: 3

Input:  arr = [1, 5, 10, 15, 20, 25], target = 17
Output: 4

Input:  arr = [1, 5, 10, 15, 20, 25], target = 25
Output: 6
```

---

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

The implementation matches the version above. See [Implementation](#implementation).

### Edge Cases

| Case | Example | Expected |
|---|---|---|
| Empty | `[], target = 5` | `0` |
| All ≤ target | `[1, 2, 3], target = 5` | `3` (= n) |
| All > target | `[10, 20], target = 5` | `0` |
| Duplicates | `[1, 2, 2, 2, 3], target = 2` | `4` (one past last 2) |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Upper bound is the dual of lower bound. Their difference counts occurrences; their pair defines half-open value ranges. With binary search (the Binary Search lesson), lower bound (the Lower Bound lesson), and upper bound (the Upper Bound lesson) you have the three primitives that power every range-and-count query in sorted data.

The next lesson lifts binary search from 1D to 2D: searching a sorted matrix. **2D binary search** treats the matrix as a flattened sorted array; **staircase search** (the Staircase Search lesson) exploits the row/column-sorted structure differently. Both find a target in `O(log(rows × cols))` for one and `O(rows + cols)` for the other.

**Transfer challenge — try before the 2D Binary Search lesson:** Use lower and upper bounds to find the **range of indices where the target appears**, returning `[-1, -1]` if absent. Hint: if `lower_bound` returns an index where `arr[i] == target`, that's the first occurrence; `upper_bound - 1` is the last.

</details>
<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

```python run viz=array viz-root=arr
class Solution:
    def first_and_last(self, arr, target):
        first = self.lower_bound(arr, target)
        if first == len(arr) or arr[first] != target:
            return [-1, -1]
        last = self.upper_bound(arr, target) - 1
        return [first, last]

    def lower_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] < target: low = mid + 1
            else: high = mid
        return low

    def upper_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] <= target: low = mid + 1
            else: high = mid
        return low


print(Solution().first_and_last([1, 2, 2, 2, 3], 2))   # [1, 3]
print(Solution().first_and_last([1, 2, 3], 5))         # [-1, -1]
```

Two `O(log n)` calls. The check `arr[first] != target` distinguishes "target absent" from "target present" — lower bound returns the same value (the insertion position) in both cases, so we have to compare. **You just rediscovered LeetCode #34, "Find First and Last Position of Element in Sorted Array."**

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: The Hook — missing, needs to be written -->
<!--       Guidance: real-world story opening before any definition -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->

<!-- TODO: Final Takeaway — missing, needs to be written -->
<!--       Guidance: exactly 3 typed bullets: Core mechanic / Dominant tradeoff / One thing to remember -->
