# 6. Sorted Rotated Array

A sorted array is rotated by some unknown amount: `[1, 2, 3, 4, 5, 6, 7]` becomes `[4, 5, 6, 7, 1, 2, 3]`. Plain binary search doesn't work — the array isn't sorted globally. But it's *almost* sorted: it consists of two sorted segments, with the second's values smaller than the first's. Can we still binary-search it in `O(log n)`?

Yes — and this lesson covers two related problems:
1. **Find the minimum** — locate the rotation point (the start of the smaller segment).
2. **Search for a target** — find a specific value in the rotated array.

Both run in `O(log n)`. The trick: at any midpoint, *one of the two halves is guaranteed sorted*. Determining which half by comparing `arr[mid]` with `arr[low]` (or `arr[high]`) lets us decide which half could possibly contain the target — same divide-and-conquer logic as binary search, with one extra comparison per iteration.

By the end of this lesson you'll know both algorithms, the "one half is always sorted" insight that makes them work, and the precise per-iteration decision tree.

## Table of contents

1. [The rotated array structure](#the-rotated-array-structure)
2. [Finding the minimum](#finding-the-minimum)
3. [Searching for a target](#searching-for-a-target)
4. [Complexity analysis](#complexity-analysis)
5. [Rotated array minimum problem](#rotated-array-minimum-problem)
6. [Rotated array search problem](#rotated-array-search-problem)

***

# The Rotated Array Structure

A sorted array `[a₀, a₁, ..., a_{n-1}]` rotated at pivot `k` becomes:

```
[a_k, a_{k+1}, ..., a_{n-1}, a_0, a_1, ..., a_{k-1}]
```

Three properties hold:
1. The first `n - k` elements form a sorted segment (the original suffix).
2. The last `k` elements form another sorted segment (the original prefix).
3. **All elements in the first segment are larger than all elements in the second segment.**

Visually:

```d2
direction: right

input: "Original sorted: [1, 2, 3, 4, 5, 6, 7]" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
rotated: "After rotating at k=4:\n[5, 6, 7, 1, 2, 3, 4]\n  ↑       ↑\n  segment 1 (sorted, larger values)\n          segment 2 (sorted, smaller values)" {style.fill: "#fde68a"; style.stroke: "#d97706"}

input -> rotated: rotate left by k=4 (or right by n-k=3)
```

<p align="center"><strong>A sorted-rotated array is two sorted segments stitched together, with all elements of the first larger than all elements of the second.</strong></p>

The minimum of the array is at the start of segment 2 — the rotation point.

---

## The Key Property

For any midpoint `mid` in a sorted-rotated array, *at least one of the two halves `[low, mid]` and `[mid, high]` is fully sorted*. Why?

- If `arr[low] ≤ arr[mid]`, then `low..mid` lies entirely within one segment → that half is sorted.
- Otherwise, `arr[mid] < arr[low]`, meaning `mid` is in segment 2 while `low` is in segment 1 → the right half `mid..high` lies entirely within segment 2 → that half is sorted.

Either way, exactly one half is sorted. We can apply standard binary-search reasoning to that half (compare target with its endpoints), then either find the target there or restrict the search to the other half.

This is the entire algorithmic insight. Now we apply it.

---

## Key Takeaway

A rotated sorted array has two sorted segments with all-bigger followed by all-smaller. At any midpoint, one half is fully sorted. Use that half to decide which half to discard. Now the two algorithms.

***

# Finding the Minimum

The minimum is at the start of segment 2 — the only place where `arr[i-1] > arr[i]` (the unique "discontinuity"). Find that index.

<details>
<summary><h2>Algorithm</h2></summary>


Compare `arr[mid]` with `arr[high]`:
- If `arr[mid] > arr[high]`: the discontinuity is in the right half — the minimum is somewhere in `(mid, high]`. Set `low = mid + 1`.
- Otherwise: the right half is sorted from `mid` onward — the minimum is in `[low, mid]`. Set `high = mid` (keeping `mid` as a candidate).

Loop until `low == high`. Return `low`.

```
arr = [4, 5, 6, 1, 2, 3]

low=0, high=5, mid=2, arr[mid]=6, arr[high]=3. 6 > 3 → low = 3
low=3, high=5, mid=4, arr[mid]=2, arr[high]=3. 2 ≤ 3 → high = 4
low=3, high=4, mid=3, arr[mid]=1, arr[high]=2. 1 ≤ 2 → high = 3
low=3, high=3 → loop exits
return 3 (arr[3] = 1, the minimum). ✓
```

</details>
<details>
<summary><h2>Why Compare with `arr[high]` (Not `arr[low]`)?</h2></summary>


Comparing with `arr[high]` is unambiguous. If `arr[mid] > arr[high]`, the second segment must be in `(mid, high]` (because `arr[high]` is in segment 2 and `arr[mid]` is in segment 1, so the boundary is between them). Comparing with `arr[low]` works for distinct elements but breaks if duplicates are allowed (e.g., `arr[mid] == arr[low]` doesn't tell us which segment `mid` is in).

</details>
<details>
<summary><h2>Implementation</h2></summary>



```python run
from typing import List

class Solution:
    def rotated_array_minimum(self, arr: List[int]) -> int:
        low: int = 0
        high: int = len(arr) - 1

        # Perform binary search until low becomes equal to high
        while low < high:
            mid: int = low + (high - low) // 2

            # If the middle element is greater than the element at high
            # index, it means the minimum element lies in the right part
            # of the array.
            if arr[mid] > arr[high]:
                low = mid + 1

            # Otherwise, the minimum element lies in the left part of the
            # array.
            else:
                high = mid

        # Return the index of the minimum element
        return low


# Examples from the problem statement
print(Solution().rotated_array_minimum([4, 5, 6, 1, 2, 3]))  # 3
print(Solution().rotated_array_minimum([5, 6, 1, 2, 3, 4]))  # 2
print(Solution().rotated_array_minimum([6, 7, 2, 3, 4, 5]))  # 2

# Edge cases
print(Solution().rotated_array_minimum([1]))                  # 0 — single element
print(Solution().rotated_array_minimum([2, 1]))               # 1 — two elements, rotated by 1
print(Solution().rotated_array_minimum([1, 2]))               # 0 — two elements, no rotation
print(Solution().rotated_array_minimum([1, 2, 3, 4, 5]))     # 0 — no rotation (pivot = 0)
print(Solution().rotated_array_minimum([2, 3, 4, 5, 1]))     # 4 — rotated by N-1
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int rotatedArrayMinimum(int[] arr) {
            int low = 0;
            int high = arr.length - 1;

            // Perform binary search until low becomes equal to high
            while (low < high) {
                int mid = low + (high - low) / 2;

                // If the middle element is greater than the element at high
                // index, it means the minimum element lies in the right part
                // of the array.
                if (arr[mid] > arr[high]) {
                    low = mid + 1;
                }

                // Otherwise, the minimum element lies in the left part of
                // the array.
                else {
                    high = mid;
                }
            }

            // Return the index of the minimum element
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{4, 5, 6, 1, 2, 3}));  // 3
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{5, 6, 1, 2, 3, 4}));  // 2
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{6, 7, 2, 3, 4, 5}));  // 2

        // Edge cases
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{1}));                  // 0 — single element
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{2, 1}));               // 1 — two elements, rotated by 1
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{1, 2}));               // 0 — two elements, no rotation
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{1, 2, 3, 4, 5}));     // 0 — no rotation (pivot = 0)
        System.out.println(new Solution().rotatedArrayMinimum(new int[]{2, 3, 4, 5, 1}));     // 4 — rotated by N-1
    }
}
```

</details>


***

# Searching for a Target

Standard binary search structure plus one extra check: identify the sorted half and decide whether the target is in it.

<details>
<summary><h2>Algorithm</h2></summary>


```
while low <= high:
    mid = (low + high) / 2
    if arr[mid] == target: return mid

    if arr[mid] >= arr[low]:               # left half [low, mid] is sorted
        if arr[low] <= target < arr[mid]: high = mid - 1   # target in left half
        else: low = mid + 1                                 # target in right half
    else:                                  # right half [mid, high] is sorted
        if arr[mid] < target <= arr[high]: low = mid + 1   # target in right half
        else: high = mid - 1                                # target in left half
```

The decision tree at each iteration:
1. Found target? Return.
2. Otherwise, identify the sorted half by checking `arr[mid] >= arr[low]` (left sorted) or `arr[mid] < arr[low]` (right sorted).
3. If target falls within the sorted half's value range → search there. Else → search the other half.

</details>
<details>
<summary><h2>A Walkthrough</h2></summary>


`arr = [4, 5, 6, 1, 2, 3]`, `target = 2`.

```
low=0, high=5, mid=2, arr[mid]=6.
  6 != 2. arr[mid]=6 >= arr[low]=4 → left half [4,5,6] is sorted.
  Is 4 <= 2 < 6? no → target not in left → low = 3.

low=3, high=5, mid=4, arr[mid]=2.
  2 == 2 → return 4.
```

Two iterations to find the target on a 6-element array.

</details>
<details>
<summary><h2>Implementation</h2></summary>



```python run
from typing import List

class Solution:
    def rotated_array_search(self, arr: List[int], target: int) -> int:
        low = 0
        high = len(arr) - 1

        while low <= high:
            mid = low + (high - low) // 2

            # If the middle element is the target, return its index
            if arr[mid] == target:
                return mid

            # If the left half is sorted
            if arr[mid] >= arr[low]:

                # If the target is within the range of the left half
                # Update the high index to search in the left half
                if arr[low] <= target and target < arr[mid]:
                    high = mid - 1

                # Otherwise, update the low index to search in the right
                # half
                else:
                    low = mid + 1

            # Otherwise, if the right half is sorted
            else:

                # If the target is within the range of the right half
                # Update the low index to search in the right half
                if arr[mid] < target and target <= arr[high]:
                    low = mid + 1

                # Otherwise, update the high index to search in the left
                # half
                else:
                    high = mid - 1

        # Target not found
        return -1


# Examples from the problem statement
print(Solution().rotated_array_search([4, 5, 6, 1, 2, 3], 3))   # 5
print(Solution().rotated_array_search([5, 6, 1, 2, 3, 4], 6))   # 1
print(Solution().rotated_array_search([6, 1, 2, 3, 4, 5], 10))  # -1

# Edge cases
print(Solution().rotated_array_search([1], 1))                   # 0 — single element present
print(Solution().rotated_array_search([1], 2))                   # -1 — single element absent
print(Solution().rotated_array_search([1, 2, 3, 4, 5], 3))      # 2 — no rotation
print(Solution().rotated_array_search([2, 3, 4, 5, 1], 1))      # 4 — target at last position
print(Solution().rotated_array_search([4, 5, 6, 1, 2, 3], 4))   # 0 — target at first position
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int rotatedArraySearch(int[] arr, int target) {
            int low = 0;
            int high = arr.length - 1;

            while (low <= high) {
                int mid = low + (high - low) / 2;

                // If the middle element is the target, return its index
                if (arr[mid] == target) {
                    return mid;
                }

                // If the left half is sorted
                if (arr[mid] >= arr[low]) {

                    // If the target is within the range of the left half
                    // Update the high index to search in the left half
                    if (arr[low] <= target && target < arr[mid]) {
                        high = mid - 1;
                    }

                    // Otherwise, update the low index to search in the right
                    // half
                    else {
                        low = mid + 1;
                    }
                }

                // Otherwise, if the right half is sorted
                else {

                    // If the target is within the range of the right half
                    // Update the low index to search in the right half
                    if (arr[mid] < target && target <= arr[high]) {
                        low = mid + 1;
                    }

                    // Otherwise, update the high index to search in the left
                    // half
                    else {
                        high = mid - 1;
                    }
                }
            }

            // Target not found
            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().rotatedArraySearch(new int[]{4, 5, 6, 1, 2, 3}, 3));   // 5
        System.out.println(new Solution().rotatedArraySearch(new int[]{5, 6, 1, 2, 3, 4}, 6));   // 1
        System.out.println(new Solution().rotatedArraySearch(new int[]{6, 1, 2, 3, 4, 5}, 10));  // -1

        // Edge cases
        System.out.println(new Solution().rotatedArraySearch(new int[]{1}, 1));                   // 0 — single element present
        System.out.println(new Solution().rotatedArraySearch(new int[]{1}, 2));                   // -1 — single element absent
        System.out.println(new Solution().rotatedArraySearch(new int[]{1, 2, 3, 4, 5}, 3));      // 2 — no rotation
        System.out.println(new Solution().rotatedArraySearch(new int[]{2, 3, 4, 5, 1}, 1));      // 4 — target at last position
        System.out.println(new Solution().rotatedArraySearch(new int[]{4, 5, 6, 1, 2, 3}, 4));   // 0 — target at first position
    }
}
```

</details>


***

# Complexity Analysis

| Algorithm | Time | Space |
|---|---|---|
| **Find minimum** | `O(log n)` | `O(1)` |
| **Search target** | `O(log n)` | `O(1)` |

Both algorithms halve the search range each iteration. The extra check ("which half is sorted?") is `O(1)` per iteration, so the total stays `O(log n)`.

***

# Rotated Array Minimum Problem

Given a sorted-then-rotated array of *distinct* elements, return the index of the minimum.

```
Input:  arr = [4, 5, 6, 1, 2, 3]
Output: 3

Input:  arr = [5, 6, 1, 2, 3, 4]
Output: 2

Input:  arr = [6, 7, 2, 3, 4, 5]
Output: 2
```

The implementation matches the version above. See [Finding the Minimum](#finding-the-minimum).

---

## Edge Cases

| Case | Example | Expected |
|---|---|---|
| Not actually rotated | `[1, 2, 3]` | `0` (minimum at start) |
| Single element | `[5]` | `0` |
| Two elements rotated | `[2, 1]` | `1` |
| Two elements not rotated | `[1, 2]` | `0` |

***

# Rotated Array Search Problem

Given a sorted-then-rotated array of *distinct* elements and a target, return the index of target, or `-1`.

```
Input:  arr = [4, 5, 6, 1, 2, 3], target = 3
Output: 5

Input:  arr = [5, 6, 1, 2, 3, 4], target = 6
Output: 1

Input:  arr = [6, 1, 2, 3, 4, 5], target = 10
Output: -1
```

The implementation matches the version above. See [Searching for a Target](#searching-for-a-target).

---

## Edge Cases

| Case | Example | Expected |
|---|---|---|
| Empty array | `[], target = 5` | `-1` |
| Not rotated, target present | `[1, 2, 3], target = 2` | `1` |
| Single element match | `[5], target = 5` | `0` |
| Target absent in rotated array | `[4, 5, 1, 2], target = 7` | `-1` |
| Target at the rotation boundary | `[4, 5, 1, 2], target = 1` | `2` |

---

## Final Takeaway

Sorted-rotated arrays look broken but are amenable to binary search via one extra check per iteration: which half is sorted? Once you know, the standard binary-search logic applies to the sorted half. `O(log n)` for both finding the minimum and searching for a target.

The next lesson shifts gears entirely — the Binary Search Pattern lesson begins the **pattern lessons** for searching. The five pattern lessons that follow cover four canonical patterns: **binary search pattern** (the Binary Search Pattern lesson), **lower-bound pattern** (the Lower Bound Pattern lesson), **upper-bound pattern** (the Upper Bound Pattern lesson), and the two **predicate-search patterns** (Minimum Predicate Search and Maximum Predicate Search). Each pattern lesson has 4 worked problems showing how to recognise and apply binary-search-style algorithms in scenarios that don't *look* like binary search at first glance.

**Transfer challenge — try before the Binary Search Pattern lesson:** What if the rotated array contains *duplicates*? For example, `[2, 2, 2, 0, 1, 2, 2]`. Does the minimum-finding algorithm still work? Why or why not?

<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

The algorithm partially breaks. When `arr[mid] == arr[high]`, we can't tell which segment they're in — both could be in segment 1 (so the right half could contain the minimum) or split across (so the right half is sorted from `mid` onward).

Fix: when `arr[mid] == arr[high]`, conservatively decrement `high` by 1. This is the only safe move — we can't make a binary decision.

```python run
class Solution:
    def rotated_min_with_dups(self, arr):
        low, high = 0, len(arr) - 1
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] > arr[high]: low = mid + 1
            elif arr[mid] < arr[high]: high = mid
            else: high -= 1                             # conservative: skip the duplicate
        return low


print(Solution().rotated_min_with_dups([2, 2, 2, 0, 1, 2, 2]))   # 3
```

Time complexity degrades to `O(n)` in the worst case (all elements equal — we decrement `high` `n - 1` times). The average case is still `O(log n)` for typical inputs. **You just discovered why "rotated array with duplicates" is a separate, harder problem (LeetCode #154 vs #153).**

</details>
