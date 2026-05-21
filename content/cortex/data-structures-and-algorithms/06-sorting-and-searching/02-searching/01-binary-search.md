# 1. Binary Search

A teacher wants to find which of her ten students scored 85 marks. The list is sorted by score. She *could* scan top-to-bottom, but with ten names that's no big deal. Now imagine the same task at a university — 50,000 students, sorted alphabetically and by score. Linear scan? Half a million ops on average. There's a much faster way.

**Look at the middle student.** Did they score 85? Done. If they scored less than 85, the answer is in the upper half — discard the lower half. If they scored more, the answer is in the lower half. Repeat on whichever half remains. Each step *halves* the search space. After ~16 steps you've narrowed 50,000 candidates to one.

That's binary search. It's the foundation of every other algorithm in this section. By the end of this lesson you'll know the algorithm, the off-by-one traps that turn correct-looking code into infinite loops, why the middle-index calculation uses `low + (high - low) / 2` instead of `(low + high) / 2`, and the precise complexity guarantees.

## Table of contents

1. [Why linear search loses at scale](#why-linear-search-loses-at-scale)
2. [Understanding binary search](#understanding-binary-search)
3. [Implementation](#implementation)
4. [Complexity analysis](#complexity-analysis)
5. [Binary search problem](#binary-search-problem)

***

# Why Linear Search Loses at Scale

Linear search examines elements one at a time. For an array of size `n`, the worst case is `n` comparisons (the target is at the end, or absent). For `n = 10`, that's negligible. For `n = 1,000,000`, that's a million comparisons per query — repeated millions of times a day in a real database, that's hours of CPU.

The deeper problem: linear search **doesn't use the structure of the input**. Even if the array is sorted, linear search ignores the order. The sortedness is wasted information.

> *Pause and predict — for a sorted array of <code>1,000,000</code> elements, how many comparisons does linear search need in the worst case? How few comparisons would binary search need?*

Linear: `1,000,000` worst case. Binary: at most `log₂(1,000,000) ≈ 20` comparisons. **A million-fold improvement,** unlocked entirely by exploiting the sort order.

---

## The One Requirement

Binary search has one absolute prerequisite: **the input must be sorted**. The algorithm cannot work on unsorted data — there's no way to know which half to discard if the elements aren't in order. If the input isn't sorted, you'd have to sort it first (`O(n log n)`) — and at that point, linear search through the unsorted version is faster (`O(n)`) for a single query.

Binary search shines when:
1. The data is **already sorted** (databases, file indexes, lookup tables).
2. You'll do **many queries** on the same sorted data (sorting cost amortises across many `O(log n)` lookups).
3. The data is **immutable or rarely changing** (re-sorting after every change defeats the purpose).

If your data is unsorted and queried once, use linear search. If it's sorted (or queried often enough to be worth sorting), use binary search.

---

## Key Takeaway

Linear search is `O(n)`; binary search is `O(log n)`. The trade-off: binary needs sorted input. For sorted data, the speedup is exponential — a million-element search drops from one million comparisons to twenty. Now we'll formalise the algorithm.

***

# Understanding Binary Search

The algorithm maintains a **search range** `[low, high]` representing positions where the target might be. Each iteration looks at the middle of the range, compares with the target, and either:
- **Equals**: found! return `mid`.
- **Middle < target**: discard the left half. Set `low = mid + 1`.
- **Middle > target**: discard the right half. Set `high = mid - 1`.

When `low > high`, the search range is empty — target is absent, return `-1`.

```d2
direction: down

s0: "Initial: arr = [1, 3, 5, 7, 9, 11, 13], target = 9\nlow=0, high=6, mid=3 → arr[3]=7 < 9 → discard left half" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
s1: "low=4, high=6, mid=5 → arr[5]=11 > 9 → discard right half" {style.fill: "#fde68a"; style.stroke: "#d97706"}
s2: "low=4, high=4, mid=4 → arr[4]=9 == target → return 4" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}

s0 -> s1 -> s2
```

<p align="center"><strong>Each iteration halves the range. For an array of 7 elements, ≤ 3 iterations are enough (because <code>log₂(7) ≈ 3</code>).</strong></p>

Step through the same example interactively — use the controls below to advance `low`, `mid`, and `high` one iteration at a time. The shaded band is the live search range.

```d3 widget=array-traversal
{
  "items": ["1", "3", "5", "7", "9", "11", "13"],
  "title": "Binary search for 9",
  "steps": [
    {
      "markers": [
        { "name": "lo",  "index": 0, "color": "#3b82f6" },
        { "name": "mid", "index": 3, "color": "#10b981" },
        { "name": "hi",  "index": 6, "color": "#f59e0b" }
      ],
      "range": { "lo": 0, "hi": 6 },
      "msg": "arr[mid]=7 < 9 → discard left half; set lo = mid + 1"
    },
    {
      "markers": [
        { "name": "lo",  "index": 4, "color": "#3b82f6" },
        { "name": "mid", "index": 5, "color": "#10b981" },
        { "name": "hi",  "index": 6, "color": "#f59e0b" }
      ],
      "range": { "lo": 4, "hi": 6 },
      "msg": "arr[mid]=11 > 9 → discard right half; set hi = mid - 1"
    },
    {
      "markers": [
        { "name": "lo",  "index": 4, "color": "#3b82f6" },
        { "name": "mid", "index": 4, "color": "#10b981" },
        { "name": "hi",  "index": 4, "color": "#f59e0b" }
      ],
      "range": { "lo": 4, "hi": 4 },
      "msg": "arr[mid]=9 = target → return 4"
    }
  ]
}
```

---

## Why `mid = low + (high - low) / 2` Instead of `(low + high) / 2`?

Both compute the same value mathematically. The second can **overflow integers** for large `low + high` — in Java, `int` overflows around 2 billion, so `low + high` can wrap to a negative number when both are around 1 billion. The first form keeps the intermediate value `(high - low)` small (at most the array size) and avoids the overflow.

Most of the time it doesn't matter — small arrays are nowhere near overflow. But it's a famous bug (Joshua Bloch documented it in `java.util.Arrays.binarySearch` in 2006), so the safer form is now standard.

---

## Why `<=` and Not `<` in the Loop Condition?

The loop runs while `low <= high`. If we used `low < high`, we'd skip the case where `low == high` — a search range with exactly one element. That single element could be the target. **Stopping the loop one iteration early is the most common binary-search bug.**

The off-by-one trap: most binary-search variants we'll see in the next several lessons (Lower Bound, Upper Bound, 2D Binary Search, Staircase Search, Sorted Rotated Array) use a *different* loop condition (sometimes `low < high`, sometimes `low + 1 < high`) depending on what they're searching for. Each variant has its own correct condition; mixing them up produces subtle bugs.

> *Predict before reading on — what happens if we accidentally write <code>low &lt; high</code> instead of <code>low &lt;= high</code> for an array of length 1?*

`low = 0, high = 0`. The condition `low < high` is `0 < 0` → false. Loop never executes. Return `-1` — but the single element might *be* the target. Wrong answer for inputs of size 1. **The off-by-one matters.**

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **`O(log n)` time** | Halving the search space each step gives logarithmic complexity. |
| **`O(1)` space** | Iterative version uses only a few index variables. |
| **Versatile** | Generalises to lower bound, upper bound, predicate search, 2D, rotated arrays, and many more variants (covered in the rest of this section). |
| **Predictable** | Best, average, worst case all `O(log n)`. |

| Limitation | Detail |
|---|---|
| **Requires sorted input** | One-time `O(n log n)` sort is acceptable only if many queries follow. |
| **Random-access only** | Doesn't work on linked lists (computing `arr[mid]` is `O(n)` on a linked list, breaking the speedup). |
| **Off-by-one prone** | Easy to write incorrectly; many variants have subtly different loop conditions. |

---

## Key Takeaway

Binary search halves the search range until the target is found or the range is empty. Three correct primitives: `<= high` for the loop, `low + (high - low) / 2` for mid, return early on equality. Now the implementation.

***

# Implementation


```python run
from typing import List

class Solution:
    def binary_search(self, arr: List[int], target: int) -> int:

        # Starting index of the search range
        low: int = 0

        # Ending index of the search range
        high: int = len(arr) - 1

        while low <= high:

            # Calculate the middle index
            mid: int = low + (high - low) // 2

            # Found the target, return the index
            if arr[mid] == target:
                return mid

            # If the arr[mid] is less than the target, adjust the search
            # range to the right half
            elif arr[mid] < target:
                low = mid + 1

            # Else if the arr[mid] is greater than the target, adjust
            # the search range to the left half
            else:
                high = mid - 1

        # Target not found in the array
        return -1
```

```java run
class Solution {
    public int binarySearch(int[] arr, int target) {

        // Starting index of the search range
        int low = 0;

        // Ending index of the search range
        int high = arr.length - 1;

        while (low <= high) {

            // Calculate the middle index
            int mid = low + (high - low) / 2;

            // Found the target, return the index
            if (arr[mid] == target) {
                return mid;
            }

            // If the arr[mid] is less than the target, adjust the search
            // range to the right half
            else if (arr[mid] < target) {
                low = mid + 1;
            }

            // Else if the arr[mid] is greater than the target, adjust
            // the search range to the left half
            else {
                high = mid - 1;
            }
        }

        // Target not found in the array
        return -1;
    }
}
```

### Step through the execution

Click **Trace** to run the algorithm with a Python tracer attached. The current line lights up; the locals panel on the right shows `low`, `mid`, `high`, and `target` as they change. The same `(low, high, mid)` triple you scrubbed through above now derives from the live execution rather than authored steps.

```python trace
def binary_search(arr, target):
    low, high = 0, len(arr) - 1
    while low <= high:
        mid = low + (high - low) // 2
        if arr[mid] == target:
            return mid
        if arr[mid] < target:
            low = mid + 1
        else:
            high = mid - 1
    return -1


result = binary_search([1, 3, 5, 7, 9, 11, 13], 9)
print(result)
```

***

# Complexity Analysis

| Resource | Best | Average | Worst |
|---|---|---|---|
| **Time** | `O(1)` | `O(log n)` | `O(log n)` |
| **Space** | `O(1)` | `O(1)` | `O(1)` |

**Best case** — target is at the middle of the array; found in one iteration.

**Average / worst case** — target is anywhere else (or absent); each iteration halves the range. After `k` iterations, `n / 2^k` elements remain. Setting this to 1 gives `k = log₂(n)`.

The space complexity is `O(1)` for the iterative version. A recursive version uses `O(log n)` stack — each recursive call replaces the loop iteration. Either version works; the iterative one is preferred to avoid stack overhead.

---

## Why Binary Search Is the Universal Search Tool

For a sorted array of size `n`, binary search beats every alternative:

| Algorithm | Time |
|---|---|
| Linear search | `O(n)` |
| Linear search with sentinel | `O(n)` (smaller constant) |
| Binary search | `O(log n)` |
| Interpolation search (uniform data) | `O(log log n)` average, `O(n)` worst |

For most use cases, binary search wins. Interpolation search is faster on uniformly-distributed data but has worst-case `O(n)` and is rarely used outside specialized contexts.

The variants we'll see in the rest of this section — lower bound, upper bound, predicate search, 2D search, rotated array — are all binary search with one of the three branches modified. The core skeleton is unchanged.

---

## Key Takeaway

Binary search: `O(log n)` time, `O(1)` space, on any sorted array. The skeleton — `low / high / mid`, three branches, `<=` loop condition — is the foundation for every algorithm in this section. Now the canonical exercise.

***

# Binary Search Problem

---

## The Problem

Given a sorted integer array `arr` and an integer `target`, return the index of `target` in `arr`, or `-1` if it's absent. **Must run in `O(log n)`.**

```
Input:  arr = [1, 2, 3, 4, 5, 6], target = 3
Output: 2

Input:  arr = [1, 2, 3, 4, 5, 6], target = 6
Output: 5

Input:  arr = [1, 2, 3, 4, 5, 6], target = 10
Output: -1
```

---

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

The implementation matches the version above. See [Implementation](#implementation).

### Edge Cases

| Case | Example | Expected |
|---|---|---|
| Empty | `[], target = 5` | `-1` |
| Single match | `[5], target = 5` | `0` |
| Single miss | `[5], target = 7` | `-1` |
| Target at start | `[1, 2, 3], target = 1` | `0` |
| Target at end | `[1, 2, 3], target = 3` | `2` |
| Duplicates | `[1, 2, 2, 2, 3], target = 2` | `2` (any of 1, 2, 3 — algorithm's choice) |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Binary search is the foundation. The next two lessons — **lower bound** (the Lower Bound lesson) and **upper bound** (the Upper Bound lesson) — modify the algorithm to handle duplicates. When `target` appears multiple times, plain binary search returns *some* index, but you might want the *first* or *last* occurrence specifically. Lower/upper bound variants give you exactly that, with the same `O(log n)` complexity.

After that, **2D binary search** (the 2D Binary Search lesson) extends the technique to sorted matrices, **staircase search** (the Staircase Search lesson) shows a different attack on partially-sorted matrices, **sorted-rotated-array** (the Sorted Rotated Array lesson) handles arrays where the sort order is broken by a rotation, and the five pattern lessons (Binary Search, Lower Bound, Upper Bound, Minimum Predicate Search, Maximum Predicate Search) generalise binary search to predicate-based searches across continuous ranges.

**Transfer challenge — try before the Lower Bound lesson:** For an array `[1, 2, 2, 2, 3]` and `target = 2`, plain binary search might return any of indices 1, 2, or 3 depending on where it lands. Modify the binary search to *guarantee* it returns the leftmost (smallest) index where `target` appears. (Hint: when `arr[mid] == target`, don't return — keep searching the left half.)

</details>
<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

```python run
class Solution:
    def leftmost_index(self, arr, target):
        low, high = 0, len(arr) - 1
        result = -1
        while low <= high:
            mid = low + (high - low) // 2
            if arr[mid] == target:
                result = mid                            # remember match
                high = mid - 1                          # but keep searching left
            elif arr[mid] < target:
                low = mid + 1
            else:
                high = mid - 1
        return result


print(Solution().leftmost_index([1, 2, 2, 2, 3], 2))   # 1
```

The trick: don't `return` on equality — record the index and continue the search to the left. The final `result` is the leftmost match. This is essentially **lower bound**, the subject of the Lower Bound lesson. **You just rediscovered the next algorithm.**

</details>
