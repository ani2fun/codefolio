# 9. Pattern: Upper Bound

The upper-bound pattern handles "first element strictly greater than X" questions. The four worked problems in this lesson all reduce to one upper-bound query, with light post-processing for edge cases.

By the end of this lesson you'll know the diagnostic checks for "this is an upper-bound problem," four worked problems showing the recurring pattern, and the canonical strategy: convert the question into "first index where arr[i] > X" and apply the upper-bound algorithm from the Upper Bound lesson.

## Table of contents

1. [Identifying the upper bound pattern](#identifying-the-upper-bound-pattern)
2. [Limit count](#limit-count)
3. [Positive index](#positive-index)
4. [Ceiling index](#ceiling-index)
5. [Breaking index](#breaking-index)

***

# Identifying the Upper Bound Pattern

| # | Question | If "yes," upper bound fits because... |
|---|---|---|
| **Q1** | Is the data sorted ascending? | Upper bound needs monotone non-decreasing input. |
| **Q2** | Are we looking for the *first index where the element exceeds a threshold*? | Upper bound returns exactly that. |
| **Q3** | Could "no element exceeds" be a possible answer? | Upper bound returns `n` in that case — caller can interpret. |

Common phrasings: "count of elements ≤ X", "first index after target", "first element greater than X", "ceiling of X", "first index satisfying f(i) > threshold."

---

# Limit Count

## The Problem

Sorted array, integer `k`. Return the count of elements `≤ k`.

```
Input:  arr = [1, 3, 5, 8, 9], k = 7
Output: 3   (elements 1, 3, 5)

Input:  arr = [1, 2, 2, 2, 3, 4], k = 3
Output: 5

Input:  arr = [1, 2, 2, 2, 3, 4], k = 8
Output: 6
```

<details>
<summary><h2>The Solution</h2></summary>


The count of elements `≤ k` is exactly `upper_bound(arr, k)` — that's the first index strictly greater than `k`, which equals the number of elements `≤ k`.


```python run
from typing import List

class Solution:
    def upper_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the upper bound index would be equal to len(arr)
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

        # Return the upper bound index, it could be equal to arr.length
        # if all elements are less than target
        return low

    def limit_count(self, arr: List[int], k: int) -> int:

        # The number of elements <= k is given by the
        # upper bound index of k
        return self.upper_bound(arr, k)


# Examples from the problem statement
print(Solution().limit_count([1, 3, 5, 8, 9], 7))       # 3
print(Solution().limit_count([1, 2, 2, 2, 3, 4], 3))    # 5
print(Solution().limit_count([1, 2, 2, 2, 3, 4], 8))    # 6

# Edge cases
print(Solution().limit_count([], 5))                     # 0 — empty array
print(Solution().limit_count([5], 5))                    # 1 — single element equal to k
print(Solution().limit_count([5], 3))                    # 0 — single element greater than k
print(Solution().limit_count([5], 7))                    # 1 — single element less than k
print(Solution().limit_count([1, 2, 2, 2, 3, 4], 0))    # 0 — none qualify
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int upperBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the upper bound index would be equal to
            // arr.length
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
            // if all elements are less than target
            return low;
        }

        public int limitCount(int[] arr, int k) {

            // The number of elements <= k is given by the
            // upper bound index of k
            return upperBound(arr, k);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().limitCount(new int[]{1, 3, 5, 8, 9}, 7));       // 3
        System.out.println(new Solution().limitCount(new int[]{1, 2, 2, 2, 3, 4}, 3));    // 5
        System.out.println(new Solution().limitCount(new int[]{1, 2, 2, 2, 3, 4}, 8));    // 6

        // Edge cases
        System.out.println(new Solution().limitCount(new int[]{}, 5));                     // 0 — empty array
        System.out.println(new Solution().limitCount(new int[]{5}, 5));                    // 1 — single element equal to k
        System.out.println(new Solution().limitCount(new int[]{5}, 3));                    // 0 — single element greater than k
        System.out.println(new Solution().limitCount(new int[]{5}, 7));                    // 1 — single element less than k
        System.out.println(new Solution().limitCount(new int[]{1, 2, 2, 2, 3, 4}, 0));    // 0 — none qualify
    }
}
```

</details>


***

# Positive Index

## The Problem

Sorted array. Return the index of the first positive element, or `-1` if none.

```
Input:  arr = [-5, -3, -1, 0, 2, 4, 6]
Output: 4

Input:  arr = [-1, 2, 2, 2, 3, 4]
Output: 1

Input:  arr = [-1, -2]
Output: -1
```

<details>
<summary><h2>The Solution</h2></summary>


`upper_bound(arr, 0)` returns the first index where `arr[i] > 0` — exactly the first positive element. Return `-1` if it's `n`.


```python run
from typing import List

class Solution:
    def upper_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the upper bound index would be equal to len(arr)
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

        # Return the upper bound index, it could be equal to arr.length
        # if all elements are less than target
        return low

    def positive_index(self, arr: List[int]) -> int:

        # Find the upper bound index for 0 i.e first index where arr[i] >
        # 0
        upper_bound_index: int = self.upper_bound(arr, 0)

        # If upper_bound_index == len(arr), no positive element exists
        if upper_bound_index == len(arr):
            return -1

        # Return the first positive index
        return upper_bound_index


# Examples from the problem statement
print(Solution().positive_index([-5, -3, -1, 0, 2, 4, 6]))  # 4
print(Solution().positive_index([-1, 2, 2, 2, 3, 4]))        # 1
print(Solution().positive_index([-1, -2]))                    # -1

# Edge cases
print(Solution().positive_index([]))                          # -1  (empty)
print(Solution().positive_index([1]))                         # 0   (single positive)
print(Solution().positive_index([-5]))                        # -1  (single negative)
print(Solution().positive_index([0, 0, 0]))                   # -1  (all zero)
print(Solution().positive_index([-3, -2, -1, 0, 1]))          # 4   (positive at last)
print(Solution().positive_index([5, 10, 15]))                 # 0   (all positive)
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int upperBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the upper bound index would be equal to
            // arr.length
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
            // if all elements are less than target
            return low;
        }

        public int positiveIndex(int[] arr) {

            // Find the upper bound index for 0 i.e first index where arr[i]
            // > 0
            int upperBoundIndex = upperBound(arr, 0);

            // If upperBoundIndex == arr.length, no positive element exists
            if (upperBoundIndex == arr.length) {
                return -1;
            }

            // Return the first positive index
            return upperBoundIndex;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().positiveIndex(new int[]{-5, -3, -1, 0, 2, 4, 6}));  // 4
        System.out.println(new Solution().positiveIndex(new int[]{-1, 2, 2, 2, 3, 4}));        // 1
        System.out.println(new Solution().positiveIndex(new int[]{-1, -2}));                    // -1

        // Edge cases
        System.out.println(new Solution().positiveIndex(new int[]{}));                          // -1  (empty)
        System.out.println(new Solution().positiveIndex(new int[]{1}));                         // 0   (single positive)
        System.out.println(new Solution().positiveIndex(new int[]{-5}));                        // -1  (single negative)
        System.out.println(new Solution().positiveIndex(new int[]{0, 0, 0}));                   // -1  (all zero)
        System.out.println(new Solution().positiveIndex(new int[]{-3, -2, -1, 0, 1}));          // 4   (positive at last)
        System.out.println(new Solution().positiveIndex(new int[]{5, 10, 15}));                 // 0   (all positive)
    }
}
```

</details>


***

# Ceiling Index

## The Problem

Sorted array `arr` and a list of `queries`. For each query, return the smallest index `i` with `arr[i] > query`. Return `-1` for queries with no such index.

```
Input:  arr = [1, 4, 7], queries = [2, 4]
Output: [1, 2]

Input:  arr = [5], queries = [2]
Output: [0]

Input:  arr = [5], queries = [6]
Output: [-1]
```

<details>
<summary><h2>The Solution</h2></summary>


Run `upper_bound` for each query. Return `-1` if the result is `n`.


```python run
from typing import List

class Solution:
    def upper_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the upper bound index would be equal to len(arr)
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

        # Return the upper bound index, it could be equal to arr.length
        # if all elements are less than target
        return low

    def ceiling_index(
        self, arr: List[int], queries: List[int]
    ) -> List[int]:

        # Resultant list to store ceiling indices for each query
        result: List[int] = []

        # Iterate through each query
        for query in queries:

            # Find the upper bound index for the query
            upperBoundIndex: int = self.upper_bound(arr, query)

            # If upperBoundIndex is equal to length of arr, there is no
            # element greater than or equal to query
            if upperBoundIndex == len(arr):
                result.append(-1)

            # Otherwise, upperBoundIndex is the ceiling index
            else:
                result.append(upperBoundIndex)

        # Return the final result list containing ceiling indices for all
        # queries
        return result


# Examples from the problem statement
print(Solution().ceiling_index([1, 4, 7], [2, 4]))  # [1, 2]
print(Solution().ceiling_index([5], [2]))            # [0]
print(Solution().ceiling_index([5], [6]))            # [-1]

# Edge cases
print(Solution().ceiling_index([1, 4, 7], [7]))      # [-1]  (query equals last element)
print(Solution().ceiling_index([1, 4, 7], [0]))      # [0]   (query less than all)
print(Solution().ceiling_index([2, 2, 2], [1]))      # [0]   (duplicates, query below)
print(Solution().ceiling_index([2, 2, 2], [2]))      # [-1]  (duplicates, all equal — no strictly greater)
print(Solution().ceiling_index([1, 3, 5, 7], [2, 4, 6, 8]))  # [1, 2, 3, -1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int upperBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the upper bound index would be equal to
            // arr.length
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
            // if all elements are less than target
            return low;
        }

        public List<Integer> ceilingIndex(int[] arr, int[] queries) {

            // Result list to store ceiling index for each query
            List<Integer> result = new ArrayList<>();

            // Iterate through each query
            for (int query : queries) {

                // Find the upper bound index for the current query
                int upperBoundIndex = upperBound(arr, query);

                // If upperBoundIndex is equal to arr.size(), it means there
                // is no element greater than or equal to query, so we append
                // -1
                if (upperBoundIndex == arr.length) {
                    result.add(-1);
                }

                // Otherwise, upperBoundIndex is the ceiling index
                else {
                    result.add(upperBoundIndex);
                }
            }

            // Return the result list containing ceiling indices for all
            // queries
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().ceilingIndex(new int[]{1, 4, 7}, new int[]{2, 4}));  // [1, 2]
        System.out.println(new Solution().ceilingIndex(new int[]{5}, new int[]{2}));            // [0]
        System.out.println(new Solution().ceilingIndex(new int[]{5}, new int[]{6}));            // [-1]

        // Edge cases
        System.out.println(new Solution().ceilingIndex(new int[]{1, 4, 7}, new int[]{7}));      // [-1]
        System.out.println(new Solution().ceilingIndex(new int[]{1, 4, 7}, new int[]{0}));      // [0]
        System.out.println(new Solution().ceilingIndex(new int[]{2, 2, 2}, new int[]{1}));      // [0]
        System.out.println(new Solution().ceilingIndex(new int[]{2, 2, 2}, new int[]{2}));      // [-1]
        System.out.println(new Solution().ceilingIndex(new int[]{1, 3, 5, 7}, new int[]{2, 4, 6, 8}));  // [1, 2, 3, -1]
    }
}
```

</details>


***

# Breaking Index

## The Problem

Sorted array and integer `delta`. Return the first index `i` where `arr[i] - arr[0] > delta`, or `-1`.

```
Input:  arr = [1, 5, 10, 15, 20, 25], delta = 6
Output: 2   (arr[2] - arr[0] = 9 > 6)

Input:  arr = [1, 2, 4, 5], delta = 2
Output: 2

Input:  arr = [1, 5], delta = 6
Output: -1
```

<details>
<summary><h2>The Solution</h2></summary>


The condition `arr[i] - arr[0] > delta` is `arr[i] > arr[0] + delta`. So `upper_bound(arr, arr[0] + delta)` gives the answer.


```python run
from typing import List

class Solution:
    def upper_bound(self, arr: List[int], target: int) -> int:

        # Initialise starting index to 0
        low: int = 0

        # Initialise ending index to len(arr) instead of len(arr) - 1
        # to cover the entire array as if all elements in the array are less
        # than target, the upper bound index would be equal to len(arr)
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

        # Return the upper bound index, it could be equal to arr.length
        # if all elements are less than target
        return low

    def breaking_index(self, arr: List[int], delta: int) -> int:

        # If the array is empty, return -1
        if len(arr) == 0:
            return -1

        # Calculate the target value which is arr[0] + delta
        target = arr[0] + delta

        # Find the upper bound index for the target value
        upper_bound_index = self.upper_bound(arr, target)

        # If upper_bound_index is equal to arr.length, it means no
        # element is greater than target, so return -1
        if upper_bound_index == len(arr):
            return -1

        # Otherwise, return the upper bound index
        else:
            return upper_bound_index


# Examples from the problem statement
print(Solution().breaking_index([1, 5, 10, 15, 20, 25], 6))  # 2
print(Solution().breaking_index([1, 2, 4, 5], 2))            # 2
print(Solution().breaking_index([1, 5], 6))                   # -1

# Edge cases
print(Solution().breaking_index([], 5))                       # -1  (empty)
print(Solution().breaking_index([3], 0))                      # -1  (single element)
print(Solution().breaking_index([1, 2], 0))                   # 1   (delta=0, first i where arr[i]>arr[0])
print(Solution().breaking_index([1, 2], 5))                   # -1  (diff never exceeds delta)
print(Solution().breaking_index([1, 2, 3, 4, 5], 3))          # 4   (breaking at last index)
print(Solution().breaking_index([0, 0, 0, 0], 0))             # -1  (all same)
```

```java run
public class Main {
    static class Solution {
        public int upperBound(int[] arr, int target) {

            // Initialise starting index to 0
            int low = 0;

            // Initialise ending index to arr.length instead of arr.length -
            // 1 to cover the entire array as if all elements in the array
            // are less than target, the upper bound index would be equal to
            // arr.length
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
            // if all elements are less than target
            return low;
        }

        public int breakingIndex(int[] arr, int delta) {

            // If the array is empty, return -1
            if (arr.length == 0) {
                return -1;
            }

            // Calculate the target value which is arr[0] + delta
            int target = arr[0] + delta;

            // Find the upper bound index for the target value
            int upperBoundIndex = upperBound(arr, target);

            // If upperBoundIndex is equal to arr.length, it means no
            // element is greater than target, so return -1
            if (upperBoundIndex == arr.length) {
                return -1;
            }

            // Otherwise, return the upper bound index
            else {
                return upperBoundIndex;
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().breakingIndex(new int[]{1, 5, 10, 15, 20, 25}, 6));  // 2
        System.out.println(new Solution().breakingIndex(new int[]{1, 2, 4, 5}, 2));            // 2
        System.out.println(new Solution().breakingIndex(new int[]{1, 5}, 6));                   // -1

        // Edge cases
        System.out.println(new Solution().breakingIndex(new int[]{}, 5));                       // -1  (empty)
        System.out.println(new Solution().breakingIndex(new int[]{3}, 0));                      // -1  (single element)
        System.out.println(new Solution().breakingIndex(new int[]{1, 2}, 0));                   // 1   (delta=0)
        System.out.println(new Solution().breakingIndex(new int[]{1, 2}, 5));                   // -1  (diff never exceeds delta)
        System.out.println(new Solution().breakingIndex(new int[]{1, 2, 3, 4, 5}, 3));          // 4   (breaking at last index)
        System.out.println(new Solution().breakingIndex(new int[]{0, 0, 0, 0}, 0));             // -1  (all same)
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Upper bound is the dual primitive to lower bound. Together they cover most "where in this sorted data does X go?" questions. The four problems showed direct count, threshold-crossing, ceiling lookup, and a derived-target query — all reducing to a single upper-bound call.

The next two lessons generalise binary search beyond *array indexing* — Minimum Predicate Search and Maximum Predicate Search cover the **predicate search patterns**, where the search space is a *range of integer values* (or a continuous range) and the comparison is a custom *predicate* function. This is the technique behind algorithms like "minimum number of pages a student must read in K days," "smallest divisor that fits a budget," and many other "binary search on the answer" problems.

**Transfer challenge — try before the Minimum Predicate Search Pattern lesson:** Use upper bound to find the *largest element strictly less than target* in a sorted array. Hint: the answer is at index `lower_bound(target) - 1`.

</details>