# 8. Pattern: Lower Bound

The binary-search pattern (the Binary Search Pattern lesson) handled "is X in this sorted array?" The lower-bound pattern handles a richer family of questions: "where would X go?", "first occurrence of X", "first element ≥ X", "closest element to X". All of them collapse to a single primitive: **lower bound** — find the first index where `arr[i] >= target`.

By the end of this lesson you'll know the diagnostic checks for "this is a lower-bound problem," four worked problems showing different phrasings of the same primitive (insertion position, first-and-last range, closest element, k-closest window), and the canonical strategy: convert the question into "first index ≥ X" and apply the lower-bound algorithm from the Lower Bound lesson.

## Table of contents

1. [Identifying the lower bound pattern](#identifying-the-lower-bound-pattern)
2. [Search insert position](#search-insert-position)
3. [First and last position](#first-and-last-position)
4. [Closest element](#closest-element)
5. [K closest elements](#k-closest-elements)

***

# Identifying the Lower Bound Pattern

Three diagnostic questions:

| # | Question | If "yes," lower bound fits because... |
|---|---|---|
| **Q1** | Is the data sorted in ascending order? | Lower bound needs monotone non-decreasing input. |
| **Q2** | Are we looking for a *position* (first occurrence, insertion point, neighbour)? | Lower bound returns position; that's its native output. |
| **Q3** | Could the target be absent? Or appear multiple times? | Plain binary search returns "any" or "none"; lower bound returns a *consistent* position even on edges. |

If all three hold, lower bound is the right primitive.

---

## Common Disguises

- **"Insert in sorted order"** → lower bound directly returns the insertion position.
- **"First occurrence" or "Last occurrence"** → first = `lower_bound(t)`; last = `lower_bound(t+1) - 1`.
- **"Closest element to target"** → lower bound finds the threshold; check it and the previous element.
- **"K elements closest to target"** → lower bound anchors a sliding window that expands outward.

---

# Search Insert Position

Direct lower-bound application.

## The Problem

Given a sorted array `arr` and `target`, return target's index if present; otherwise return the index where target would be inserted to keep the array sorted.

```
Input:  arr = [1, 2, 3, 4, 5, 6], target = 3
Output: 2

Input:  arr = [1, 2, 7, 8, 9, 10], target = 3
Output: 2   (would insert before 7)

Input:  arr = [1, 2, 7, 9, 10, 11], target = 8
Output: 3
```

<details>
<summary><h2>The Solution</h2></summary>


Just lower bound.


```python run
from typing import List

class Solution:
    def lower_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the lower bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # >= the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than arr[target], then find in
            # right subarray
            if arr[mid] < target:
                low = mid + 1

            # If arr[mid] is greater than or equal to target, then it may
            # be the answer. So, instead of high = mid - 1, we do high = mid
            # to include mid in the next search space
            else:
                high = mid

        # Return the lower bound index, it could be equal to len(arr)
        # if all elements are less than target
        return low

    def search_insert_position(self, arr: List[int], target: int) -> int:
        return self.lower_bound(arr, target)


# Examples from the problem statement
print(Solution().search_insert_position([1, 2, 3, 4, 5, 6], 3))   # 2
print(Solution().search_insert_position([1, 2, 7, 8, 9, 10], 3))  # 2
print(Solution().search_insert_position([1, 2, 7, 9, 10, 11], 8)) # 3

# Edge cases
print(Solution().search_insert_position([], 5))                    # 0 — empty array
print(Solution().search_insert_position([5], 5))                   # 0 — single element match
print(Solution().search_insert_position([5], 3))                   # 0 — insert before only element
print(Solution().search_insert_position([5], 7))                   # 1 — insert after only element
print(Solution().search_insert_position([1, 2, 3, 4, 5, 6], 7))   # 6 — insert at end
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int lowerBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the lower bound index would be equal to
            // arr.length
            int high = arr.length;

            // 'high' is exclusive (can be arr.length), so we use 'low <
            // high' instead of 'low <= high'. This loop finds the first
            // index where the element is
            // >= the target without going out of bounds.
            while (low < high) {

                // Find the middle index
                int mid = low + (high - low) / 2;

                // If arr[mid] is less than arr[target], then find in
                // right subarray
                if (arr[mid] < target) {
                    low = mid + 1;
                }

                // If arr[mid] is greater than or equal to target, then it
                // may be the answer. So, instead of high = mid - 1, we do
                // high = mid to include mid in the next search space
                else {
                    high = mid;
                }
            }

            // Return the lower bound index, it could be equal to arr.length
            // if all elements are less than target
            return low;
        }

        public int searchInsertPosition(int[] arr, int target) {
            return lowerBound(arr, target);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().searchInsertPosition(new int[]{1, 2, 3, 4, 5, 6}, 3));   // 2
        System.out.println(new Solution().searchInsertPosition(new int[]{1, 2, 7, 8, 9, 10}, 3));  // 2
        System.out.println(new Solution().searchInsertPosition(new int[]{1, 2, 7, 9, 10, 11}, 8)); // 3

        // Edge cases
        System.out.println(new Solution().searchInsertPosition(new int[]{}, 5));                    // 0 — empty array
        System.out.println(new Solution().searchInsertPosition(new int[]{5}, 5));                   // 0 — single element match
        System.out.println(new Solution().searchInsertPosition(new int[]{5}, 3));                   // 0 — insert before only element
        System.out.println(new Solution().searchInsertPosition(new int[]{5}, 7));                   // 1 — insert after only element
        System.out.println(new Solution().searchInsertPosition(new int[]{1, 2, 3, 4, 5, 6}, 7));   // 6 — insert at end
    }
}
```


`O(log n)` time, `O(1)` space.

</details>

***

# First and Last Position

Two `O(log n)` queries — `lower_bound(target)` for the first index, `lower_bound(target + 1) - 1` for the last.

## The Problem

Given a sorted array and target, return `[first_index, last_index]` of target, or `[-1, -1]` if absent.

```
Input:  arr = [1, 2, 2, 2, 3, 4], target = 2
Output: [1, 3]

Input:  arr = [1, 2, 2, 2, 3, 4], target = 3
Output: [4, 4]

Input:  arr = [1, 2, 2, 2, 3, 4], target = 5
Output: [-1, -1]
```

<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def lower_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the lower bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # >= the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than arr[target], then find in
            # right subarray
            if arr[mid] < target:
                low = mid + 1

            # If arr[mid] is greater than or equal to target, then it may
            # be the answer. So, instead of high = mid - 1, we do high = mid
            # to include mid in the next search space
            else:
                high = mid

        # Return the lower bound index, it could be equal to len(arr)
        # if all elements are less than target
        return low

    def first_and_last_position(
        self, arr: List[int], target: int
    ) -> List[int]:

        # Initialize the result list with -1 values
        result = [-1, -1]

        # Find the first occurrence of target
        first: int = self.lower_bound(arr, target)

        # Find the lower bound of target+1 and subtract 1 to get the
        # last occurrence of target
        last: int = self.lower_bound(arr, target + 1) - 1

        # Check if the lower bound index is within the list bounds and if
        # the value is the target
        if first < len(arr) and arr[first] == target:

            # Return the range [first, last]
            return [first, last]

        # Return the default result list
        return result


# Examples from the problem statement
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 2))  # [1, 3]
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 3))  # [4, 4]
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 5))  # [-1, -1]

# Edge cases
print(Solution().first_and_last_position([], 1))                    # [-1, -1] — empty array
print(Solution().first_and_last_position([5], 5))                   # [0, 0] — single element match
print(Solution().first_and_last_position([5], 3))                   # [-1, -1] — single element miss
print(Solution().first_and_last_position([2, 2, 2, 2], 2))         # [0, 3] — all same
print(Solution().first_and_last_position([1, 2, 2, 2, 3, 4], 1))   # [0, 0] — target at first
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int lowerBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the lower bound index would be equal to
            // arr.length
            int high = arr.length;

            // 'high' is exclusive (can be arr.length), so we use 'low <
            // high' instead of 'low <= high'. This loop finds the first
            // index where the element is
            // >= the target without going out of bounds.
            while (low < high) {

                // Find the middle index
                int mid = low + (high - low) / 2;

                // If arr[mid] is less than arr[target], then find in
                // right subarray
                if (arr[mid] < target) {
                    low = mid + 1;
                }

                // If arr[mid] is greater than or equal to target, then it
                // may be the answer. So, instead of high = mid - 1, we do
                // high = mid to include mid in the next search space
                else {
                    high = mid;
                }
            }

            // Return the lower bound index, it could be equal to arr.length
            // if all elements are less than target
            return low;
        }

        public int[] firstAndLastPosition(int[] arr, int target) {

            // Initialize the result array with -1 values
            int[] result = new int[] { -1, -1 };

            // Find the first occurrence of target
            int first = lowerBound(arr, target);

            // Find the lower bound of target+1 and subtract 1 to get the
            // last occurrence of target
            int last = lowerBound(arr, target + 1) - 1;

            // Check if the lower bound index is within the array bounds and
            // if the value is the target
            if (first < arr.length && arr[first] == target) {

                // Return the range [first, last]
                return new int[] { first, last };
            }

            // Return the default result array
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 2)));  // [1, 3]
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 3)));  // [4, 4]
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 5)));  // [-1, -1]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{}, 1)));                    // [-1, -1] — empty array
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{5}, 5)));                   // [0, 0] — single element match
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{5}, 3)));                   // [-1, -1] — single element miss
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{2, 2, 2, 2}, 2)));         // [0, 3] — all same
        System.out.println(Arrays.toString(new Solution().firstAndLastPosition(new int[]{1, 2, 2, 2, 3, 4}, 1)));   // [0, 0] — target at first
    }
}
```

</details>


***

# Closest Element

Use lower bound to find the threshold position; the answer is either at the threshold or just before it.

## The Problem

Given a sorted array and target, return the closest element. Ties broken by smaller value.

```
Input:  arr = [1, 2, 3, 4, 5, 6], target = 4
Output: 4

Input:  arr = [2, 4, 6, 8, 10, 12], target = 5
Output: 4   (4 and 6 are equidistant; smaller wins)

Input:  arr = [1, 10], target = 7
Output: 10
```

<details>
<summary><h2>The Solution</h2></summary>


`lower_bound(target)` gives the smallest index `i` with `arr[i] >= target`. The closest element is either `arr[i]` or `arr[i - 1]` — compare distances.


```python run
from typing import List

class Solution:
    def lower_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the lower bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # >= the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than arr[target], then find in
            # right subarray
            if arr[mid] < target:
                low = mid + 1

            # If arr[mid] is greater than or equal to target, then it may
            # be the answer. So, instead of high = mid - 1, we do high = mid
            # to include mid in the next search space
            else:
                high = mid

        # Return the lower bound index, it could be equal to len(arr)
        # if all elements are less than target
        return low

    def closest_element(self, arr: List[int], target: int) -> int:

        # Return -1 if the array is empty
        if not arr:
            return -1

        lower_bound_index = self.lower_bound(arr, target)

        # If lower bound index is 0, return the first element
        if lower_bound_index == 0:
            return arr[0]

        # If lower bound index is equal to the size of the array,
        # return the last element
        elif lower_bound_index == len(arr):
            return arr[-1]

        # Else, return the element which is closest to the target
        # among the two closest elements
        else:

            # Get the element strictly less than target
            lower_element = arr[lower_bound_index - 1]

            # Get the element greater than or equal to target
            upper_element = arr[lower_bound_index]

            # Return the closest element
            if target - lower_element <= upper_element - target:
                return lower_element
            else:
                return upper_element


# Examples from the problem statement
print(Solution().closest_element([1, 2, 3, 4, 5, 6], 4))    # 4
print(Solution().closest_element([2, 4, 6, 8, 10, 12], 5))  # 4
print(Solution().closest_element([1, 10], 7))                # 10

# Edge cases
print(Solution().closest_element([], 5))                     # -1 — empty array
print(Solution().closest_element([5], 5))                    # 5  — single element match
print(Solution().closest_element([5], 1))                    # 5  — single element, target before
print(Solution().closest_element([5], 9))                    # 5  — single element, target after
print(Solution().closest_element([1, 2, 3, 4, 5, 6], 0))    # 1  — target before all elements
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int lowerBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the lower bound index would be equal to
            // arr.length
            int high = arr.length;

            // 'high' is exclusive (can be arr.length), so we use 'low <
            // high' instead of 'low <= high'. This loop finds the first
            // index where the element is
            // >= the target without going out of bounds.
            while (low < high) {

                // Find the middle index
                int mid = low + (high - low) / 2;

                // If arr[mid] is less than arr[target], then find in
                // right subarray
                if (arr[mid] < target) {
                    low = mid + 1;
                }

                // If arr[mid] is greater than or equal to target, then it
                // may be the answer. So, instead of high = mid - 1, we do
                // high = mid to include mid in the next search space
                else {
                    high = mid;
                }
            }

            // Return the lower bound index, it could be equal to arr.length
            // if all elements are less than target
            return low;
        }

        public int closestElement(int[] arr, int target) {

            // Return -1 if the array is empty
            if (arr.length == 0) {
                return -1;
            }

            int lowerBoundIndex = lowerBound(arr, target);

            // If lower bound index is 0, return the first element
            if (lowerBoundIndex == 0) {
                return arr[0];
            }

            // If lower bound index is equal to the size of the array,
            // return the last element
            else if (lowerBoundIndex == arr.length) {
                return arr[arr.length - 1];
            }

            // Else, return the element which is closest to the target
            // among the two closest elements
            else {

                // Get the element strictly less than target
                int lowerElement = arr[lowerBoundIndex - 1];

                // Get the element greater than or equal to target
                int upperElement = arr[lowerBoundIndex];

                // Return the closest element
                if (target - lowerElement <= upperElement - target) {
                    return lowerElement;
                } else {
                    return upperElement;
                }
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().closestElement(new int[]{1, 2, 3, 4, 5, 6}, 4));    // 4
        System.out.println(new Solution().closestElement(new int[]{2, 4, 6, 8, 10, 12}, 5));  // 4
        System.out.println(new Solution().closestElement(new int[]{1, 10}, 7));                // 10

        // Edge cases
        System.out.println(new Solution().closestElement(new int[]{}, 5));                     // -1 — empty array
        System.out.println(new Solution().closestElement(new int[]{5}, 5));                    // 5  — single element match
        System.out.println(new Solution().closestElement(new int[]{5}, 1));                    // 5  — single element, target before
        System.out.println(new Solution().closestElement(new int[]{5}, 9));                    // 5  — single element, target after
        System.out.println(new Solution().closestElement(new int[]{1, 2, 3, 4, 5, 6}, 0));    // 1  — target before all elements
    }
}
```

</details>


***

# K Closest Elements

Lower bound anchors a sliding window that expands outward.

## The Problem

Given a sorted array, integer `k`, and integer `target`, return the `k` closest elements to target, sorted ascending. Ties broken by smaller value first.

```
Input:  arr = [1, 2, 3, 4, 5, 6], k = 3, target = 4
Output: [3, 4, 5]

Input:  arr = [1, 4, 5, 6, 7, 8], k = 3, target = 4
Output: [4, 5, 6]

Input:  arr = [1, 5, 8, 10, 12, 13], k = 3, target = 10
Output: [8, 10, 12]
```

<details>
<summary><h2>The Solution</h2></summary>


`lower_bound(target)` gives the first index `>= target`; that splits the array into a left side (all `< target`) and a right side (all `>= target`). Set `right` to that index and `left` to `right - 1` — the two candidates straddling the target. Expand the window outward `k` times: on each step, take whichever of `arr[left]` / `arr[right]` is closer to `target` (ties broken by the smaller value, i.e. the `left` side), stepping that pointer outward; if one side has run off the array, take the other. The `k` elements collected between the final pointers — `arr[left + 1 : right]` — are the answer, already sorted ascending.


```python run
from typing import List

class Solution:
    def lower_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the lower bound index would be equal to len(arr)
        high: int = len(arr)

        # 'high' is exclusive (can be len(arr)), so we use 'low < high' instead
        # of 'low <= high'. This loop finds the first index where the element is
        # >= the target without going out of bounds.
        while low < high:

            # Find the middle index
            mid: int = low + (high - low) // 2

            # If arr[mid] is less than arr[target], then find in
            # right subarray
            if arr[mid] < target:
                low = mid + 1

            # If arr[mid] is greater than or equal to target, then it may
            # be the answer. So, instead of high = mid - 1, we do high = mid
            # to include mid in the next search space
            else:
                high = mid

        # Return the lower bound index, it could be equal to len(arr)
        # if all elements are less than target
        return low

    def k_closest_elements(
        self, arr: List[int], k: int, target: int
    ) -> List[int]:
        if not arr or k <= 0:
            return []

        right = self.lower_bound(arr, target)
        left = right - 1

        # Expand the window to the left and right
        remaining = k
        while remaining > 0:
            remaining -= 1

            # If left pointer is out of bounds,
            # move the right pointer
            if left < 0:
                right += 1

            # If right pointer is out of bounds,
            # move the left pointer
            elif right >= len(arr):
                left -= 1

            # If the element at left pointer is closer to target,
            # move the left pointer
            elif target - arr[left] <= arr[right] - target:
                left -= 1

            # Else, if the element at right pointer is closer to target,
            # move the right pointer
            else:
                right += 1

        # Return the k closest elements collected between left and right
        # pointers
        return arr[left + 1: right]


# Examples from the problem statement
print(Solution().k_closest_elements([1, 2, 3, 4, 5, 6], 3, 4))     # [3, 4, 5]
print(Solution().k_closest_elements([1, 4, 5, 6, 7, 8], 3, 4))     # [4, 5, 6]
print(Solution().k_closest_elements([1, 5, 8, 10, 12, 13], 3, 10)) # [8, 10, 12]

# Edge cases
print(Solution().k_closest_elements([], 3, 4))                      # [] — empty array
print(Solution().k_closest_elements([1, 2, 3, 4, 5, 6], 0, 4))     # [] — k = 0
print(Solution().k_closest_elements([5], 1, 5))                     # [5] — single element match
print(Solution().k_closest_elements([1, 2, 3, 4, 5, 6], 6, 4))     # [1, 2, 3, 4, 5, 6] — k = all
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int lowerBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the lower bound index would be equal to
            // arr.length
            int high = arr.length;

            // 'high' is exclusive (can be arr.length), so we use 'low <
            // high' instead of 'low <= high'. This loop finds the first
            // index where the element is
            // >= the target without going out of bounds.
            while (low < high) {

                // Find the middle index
                int mid = low + (high - low) / 2;

                // If arr[mid] is less than arr[target], then find in
                // right subarray
                if (arr[mid] < target) {
                    low = mid + 1;
                }

                // If arr[mid] is greater than or equal to target, then it
                // may be the answer. So, instead of high = mid - 1, we do
                // high = mid to include mid in the next search space
                else {
                    high = mid;
                }
            }

            // Return the lower bound index, it could be equal to arr.length
            // if all elements are less than target
            return low;
        }

        public int[] kClosestElements(int[] arr, int k, int target) {
            if (arr.length == 0 || k <= 0) {
                return new int[0];
            }

            int right = lowerBound(arr, target);
            int left = right - 1;

            // Expand the window to the left and right
            while (k-- > 0) {

                // If left pointer is out of bounds,
                // move the right pointer
                if (left < 0) {
                    right++;
                }

                // If right pointer is out of bounds,
                // move the left pointer
                else if (right >= arr.length) {
                    left--;
                }

                // If the element at left pointer is closer to target,
                // move the left pointer
                else if (target - arr[left] <= arr[right] - target) {
                    left--;
                }

                // Else, if the element at right pointer is closer to target,
                // move the right pointer
                else {
                    right++;
                }
            }

            // Return the k closest elements collected between left and right
            // pointers
            int[] result = new int[right - left - 1];
            for (int i = left + 1, j = 0; i < right; i++, j++) {
                result[j] = arr[i];
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{1, 2, 3, 4, 5, 6}, 3, 4)));     // [3, 4, 5]
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{1, 4, 5, 6, 7, 8}, 3, 4)));     // [4, 5, 6]
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{1, 5, 8, 10, 12, 13}, 3, 10))); // [8, 10, 12]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{}, 3, 4)));                      // [] — empty array
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{1, 2, 3, 4, 5, 6}, 0, 4)));     // [] — k = 0
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{5}, 1, 5)));                     // [5] — single element match
        System.out.println(Arrays.toString(new Solution().kClosestElements(new int[]{1, 2, 3, 4, 5, 6}, 6, 4)));     // [1, 2, 3, 4, 5, 6] — k = all
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Lower bound is the single primitive behind a family of "where should this go?" queries. The four problems showed insertion position, range queries, closest neighbour, and k-closest. Each adds a small post-processing step to the same `O(log n)` lower bound query.

The next lesson (the Upper Bound Pattern lesson) handles the dual — **upper bound pattern** for problems that require the *first index strictly greater than target*.

**Transfer challenge — try before the Upper Bound Pattern lesson:** Use lower bound to count how many elements in a sorted array are *strictly less than* target. (Hint: that's the lower bound's return value.)

</details>