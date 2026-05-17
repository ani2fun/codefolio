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

## The Solution

The count of elements `≤ k` is exactly `upper_bound(arr, k)` — that's the first index strictly greater than `k`, which equals the number of elements `≤ k`.


```pseudocode
# Number of elements ≤ k = upperBound(k) (returns the count, not the index).
function limitCount(arr, k):
    return upperBound(arr, k)
```

```python run
from typing import List

class Solution:
    def limit_count(self, arr: List[int], k: int) -> int:
        return self._upper_bound(arr, k)

    def _upper_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] <= target: low = mid + 1
            else: high = mid
        return low


if __name__ == "__main__":
    print(Solution().limit_count([1, 3, 5, 8, 9], 7))   # 3
```

```java run
public class Main {
    static class Solution {
        public int limitCount(int[] arr, int k) { return upperBound(arr, k); }
        private int upperBound(int[] arr, int target) {
            int low = 0, high = arr.length;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (arr[mid] <= target) low = mid + 1; else high = mid;
            }
            return low;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().limitCount(new int[]{1, 3, 5, 8, 9}, 7));   // 3
    }
}
```

```c run
int upper_bound(int *arr, int n, int target) {
    int low = 0, high = n;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] <= target) low = mid + 1; else high = mid;
    }
    return low;
}

int limit_count(int *arr, int n, int k) { return upper_bound(arr, n, k); }
```

```scala run
object Main extends App {
  class Solution {
    def limitCount(arr: Array[Int], k: Int): Int = upperBound(arr, k)
    private def upperBound(arr: Array[Int], target: Int): Int = {
      var low = 0; var high = arr.length
      while (low < high) {
        val mid = low + (high - low) / 2
        if (arr(mid) <= target) low = mid + 1 else high = mid
      }
      low
    }
  }

  println(new Solution().limitCount(Array(1, 3, 5, 8, 9), 7))   // 3
}
```


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

## The Solution

`upper_bound(arr, 0)` returns the first index where `arr[i] > 0` — exactly the first positive element. Return `-1` if it's `n`.


```pseudocode
# First index with arr[i] > 0 = upperBound(0). Returns −1 if no positive element.
function positiveIndex(arr):
    idx ← upperBound(arr, 0)
    if idx < length(arr):
        return idx
    return −1
```

```python run
from typing import List

class Solution:
    def positive_index(self, arr: List[int]) -> int:
        idx = self._upper_bound(arr, 0)
        return idx if idx < len(arr) else -1

    def _upper_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] <= target: low = mid + 1
            else: high = mid
        return low


if __name__ == "__main__":
    print(Solution().positive_index([-5, -3, -1, 0, 2, 4, 6]))   # 4
```

```java run
public class Main {
    static class Solution {
        public int positiveIndex(int[] arr) {
            int idx = upperBound(arr, 0);
            return idx < arr.length ? idx : -1;
        }
        private int upperBound(int[] arr, int target) {
            int low = 0, high = arr.length;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (arr[mid] <= target) low = mid + 1; else high = mid;
            }
            return low;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().positiveIndex(new int[]{-5, -3, -1, 0, 2, 4, 6}));   // 4
    }
}
```

```c run
int ub(int *arr, int n, int target) {
    int low = 0, high = n;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] <= target) low = mid + 1; else high = mid;
    }
    return low;
}

int positive_index(int *arr, int n) {
    int idx = ub(arr, n, 0);
    return idx < n ? idx : -1;
}
```

```scala run
object Main extends App {
  class Solution {
    def positiveIndex(arr: Array[Int]): Int = {
      val idx = upperBound(arr, 0)
      if (idx < arr.length) idx else -1
    }
    private def upperBound(arr: Array[Int], target: Int): Int = {
      var low = 0; var high = arr.length
      while (low < high) {
        val mid = low + (high - low) / 2
        if (arr(mid) <= target) low = mid + 1 else high = mid
      }
      low
    }
  }

  println(new Solution().positiveIndex(Array(-5, -3, -1, 0, 2, 4, 6)))   // 4
}
```


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

## The Solution

Run `upper_bound` for each query. Return `-1` if the result is `n`.


```pseudocode
# For each query q, the strict ceiling of q in arr = upperBound(q). −1 if past the end.
function ceilingIndex(arr, queries):
    result ← empty list
    for each q in queries:
        idx ← upperBound(arr, q)
        if idx < length(arr):
            append idx to result
        else:
            append −1 to result
    return result
```

```python run
from typing import List

class Solution:
    def ceiling_index(self, arr: List[int], queries: List[int]) -> List[int]:
        return [self._upper_bound(arr, q) if self._upper_bound(arr, q) < len(arr) else -1 for q in queries]

    def _upper_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] <= target: low = mid + 1
            else: high = mid
        return low


if __name__ == "__main__":
    print(Solution().ceiling_index([1, 4, 7], [2, 4]))   # [1, 2]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<Integer> ceilingIndex(int[] arr, int[] queries) {
            List<Integer> result = new ArrayList<>();
            for (int q : queries) {
                int idx = upperBound(arr, q);
                result.add(idx < arr.length ? idx : -1);
            }
            return result;
        }
        private int upperBound(int[] arr, int target) {
            int low = 0, high = arr.length;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (arr[mid] <= target) low = mid + 1; else high = mid;
            }
            return low;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().ceilingIndex(new int[]{1, 4, 7}, new int[]{2, 4}));   // [1, 2]
    }
}
```

```c run
#include <stdlib.h>

int ub_(int *arr, int n, int target) {
    int low = 0, high = n;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] <= target) low = mid + 1; else high = mid;
    }
    return low;
}

int *ceiling_index(int *arr, int n, int *queries, int q, int *outLen) {
    int *result = (int *) malloc(q * sizeof(int));
    for (int i = 0; i < q; i++) {
        int idx = ub_(arr, n, queries[i]);
        result[i] = idx < n ? idx : -1;
    }
    *outLen = q;
    return result;
}
```

```scala run
object Main extends App {
  class Solution {
    def ceilingIndex(arr: Array[Int], queries: Array[Int]): List[Int] = {
      queries.map { q =>
        val idx = upperBound(arr, q)
        if (idx < arr.length) idx else -1
      }.toList
    }
    private def upperBound(arr: Array[Int], target: Int): Int = {
      var low = 0; var high = arr.length
      while (low < high) {
        val mid = low + (high - low) / 2
        if (arr(mid) <= target) low = mid + 1 else high = mid
      }
      low
    }
  }

  println(new Solution().ceilingIndex(Array(1, 4, 7), Array(2, 4)))   // List(1, 2)
}
```


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

## The Solution

The condition `arr[i] - arr[0] > delta` is `arr[i] > arr[0] + delta`. So `upper_bound(arr, arr[0] + delta)` gives the answer.


```pseudocode
# First index whose value exceeds arr[0] + delta = upperBound(arr[0] + delta).
function breakingIndex(arr, delta):
    if arr is empty: return −1
    target ← arr[0] + delta
    idx ← upperBound(arr, target)
    if idx < length(arr):
        return idx
    return −1
```

```python run
from typing import List

class Solution:
    def breaking_index(self, arr: List[int], delta: int) -> int:
        if not arr: return -1
        target = arr[0] + delta
        idx = self._upper_bound(arr, target)
        return idx if idx < len(arr) else -1

    def _upper_bound(self, arr, target):
        low, high = 0, len(arr)
        while low < high:
            mid = low + (high - low) // 2
            if arr[mid] <= target: low = mid + 1
            else: high = mid
        return low


if __name__ == "__main__":
    print(Solution().breaking_index([1, 5, 10, 15, 20, 25], 6))   # 2
```

```java run
public class Main {
    static class Solution {
        public int breakingIndex(int[] arr, int delta) {
            if (arr.length == 0) return -1;
            int target = arr[0] + delta;
            int idx = upperBound(arr, target);
            return idx < arr.length ? idx : -1;
        }
        private int upperBound(int[] arr, int target) {
            int low = 0, high = arr.length;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (arr[mid] <= target) low = mid + 1; else high = mid;
            }
            return low;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().breakingIndex(new int[]{1, 5, 10, 15, 20, 25}, 6));   // 2
    }
}
```

```c run
int ub3(int *arr, int n, int target) {
    int low = 0, high = n;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] <= target) low = mid + 1; else high = mid;
    }
    return low;
}

int breaking_index(int *arr, int n, int delta) {
    if (n == 0) return -1;
    int target = arr[0] + delta;
    int idx = ub3(arr, n, target);
    return idx < n ? idx : -1;
}
```

```scala run
object Main extends App {
  class Solution {
    def breakingIndex(arr: Array[Int], delta: Int): Int = {
      if (arr.isEmpty) return -1
      val target = arr(0) + delta
      val idx = upperBound(arr, target)
      if (idx < arr.length) idx else -1
    }
    private def upperBound(arr: Array[Int], target: Int): Int = {
      var low = 0; var high = arr.length
      while (low < high) {
        val mid = low + (high - low) / 2
        if (arr(mid) <= target) low = mid + 1 else high = mid
      }
      low
    }
  }

  println(new Solution().breakingIndex(Array(1, 5, 10, 15, 20, 25), 6))   // 2
}
```


***

## Final Takeaway

Upper bound is the dual primitive to lower bound. Together they cover most "where in this sorted data does X go?" questions. The four problems showed direct count, threshold-crossing, ceiling lookup, and a derived-target query — all reducing to a single upper-bound call.

The next two lessons generalise binary search beyond *array indexing* — Minimum Predicate Search and Maximum Predicate Search cover the **predicate search patterns**, where the search space is a *range of integer values* (or a continuous range) and the comparison is a custom *predicate* function. This is the technique behind algorithms like "minimum number of pages a student must read in K days," "smallest divisor that fits a budget," and many other "binary search on the answer" problems.

**Transfer challenge — try before the Minimum Predicate Search Pattern lesson:** Use upper bound to find the *largest element strictly less than target* in a sorted array. Hint: the answer is at index `lower_bound(target) - 1`.
