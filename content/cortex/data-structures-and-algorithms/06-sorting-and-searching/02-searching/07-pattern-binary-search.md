# 7. Pattern: Binary Search

The first six lessons (Binary Search through Sorted Rotated Array) covered the *direct* applications of binary search and its variants. The five pattern lessons that follow generalise the technique. **Pattern 1 — Binary Search** covers problems where the binary-search algorithm itself is the answer, but spotting that takes practice. The "needle in a haystack" might be a code in a recovery list, a target in a descending array, or a shared element across multiple sorted rows.

By the end of this lesson you'll know the diagnostic checks for "this looks like a binary-search problem," four worked problems showing different ways the algorithm appears, and the canonical strategy: if the data is sorted (in any monotone direction), binary search is your default tool.

## Table of contents

1. [Identifying the binary search pattern](#identifying-the-binary-search-pattern)
2. [Recovery validation](#recovery-validation)
3. [Reverse binary search](#reverse-binary-search)
4. [Minimum shared element](#minimum-shared-element)
5. [Intersecting elements](#intersecting-elements)

***

# Identifying the Binary Search Pattern

Two diagnostic questions decide whether plain binary search applies.

| # | Question | If "yes," binary search fits because... |
|---|---|---|
| **Q1** | Is the data sorted (ascending or descending)? | Binary search needs a monotone sequence to halve the search space. |
| **Q2** | Are we looking up *whether/where* a specific target exists, not transforming it? | Binary search returns position; lookups are its native use case. |

If both are "yes," `O(log n)` per query is the best you can do.

---

## Common Disguises

Binary search problems often *look* like nested loops or hash lookups at first. Watch for:

- **"Sorted" + "search for"** — direct application.
- **"Sorted in descending order"** — reverse the comparison; algorithm is otherwise identical.
- **"Each row is sorted"** — binary search per row turns `O(n)` into `O(log n)` per row.
- **"Find common elements"** — iterate one collection, binary-search each in the other.
- **Multi-key lookup** — binary search on the key field; verify other fields after.

The trade-off: binary search needs sorted input. If the data isn't sorted but is queried many times, sorting once (`O(n log n)`) and then binary-searching (`O(log n)` per query) beats repeated linear scans (`O(n)` per query) after roughly `log n` queries.

---

## Strategy

When you see a search-style problem on sorted data:
1. Identify the sort axis.
2. Identify the target.
3. Apply binary search (or its variants) with the right comparison.

The four worked problems below show this strategy applied in different settings: a single sorted array (recovery), a descending array (reverse search), a sorted-row matrix with column-wise checks (shared element), and the same with multi-result extraction (intersection).

---

# Recovery Validation

A list of valid recovery codes (sorted) and a list of attempts. Did *any* attempt succeed?

## The Problem

Given a sorted array `recoveryCodes` and an array `attempts`, return `true` if any attempt is in the recovery codes list. **Must run in `O(N log N)`** where `N = len(attempts)`, `M = len(recoveryCodes)`.

```
Input:  recoveryCodes = [1, 4, 7], attempts = [2, 4]
Output: true   (4 is in recovery codes)

Input:  recoveryCodes = [5, 9, 11, 12], attempts = [2, 9, 12]
Output: true

Input:  recoveryCodes = [1, 2, 3], attempts = [5, 6]
Output: false
```

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

Linear scan over `attempts`; binary-search each one against `recoveryCodes`. Total: `O(N log M)`. Stop on first match.


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
            mid: int = (low + high) // 2

            # Found the target, return the index
            if arr[mid] == target:
                return mid

            # If the arr[mid] is less than the target, adjust the search
            # range to the right half
            if arr[mid] < target:
                low = mid + 1

            # Else if the arr[mid] is greater than the target, adjust
            # the search range to the left half
            else:
                high = mid - 1

        # Target not found in the array
        return -1

    def recovery_validation(
        self, recovery_codes: List[int], attempts: List[int]
    ) -> bool:

        # Iterate through each attempted recovery code
        for attempt in attempts:

            # If the recovery code is acceptable, return true
            if self.binary_search(recovery_codes, attempt) != -1:
                return True

        # No acceptable recovery code found, return false
        return False


# Examples from the problem statement
print(Solution().recovery_validation([1, 4, 7], [2, 4]))         # True
print(Solution().recovery_validation([5, 9, 11, 12], [2, 9, 12]))  # True
print(Solution().recovery_validation([1, 2, 3], [5, 6]))         # False

# Edge cases
print(Solution().recovery_validation([1, 2, 3], []))              # False — no attempts
print(Solution().recovery_validation([5], [5]))                   # True  — single code match
print(Solution().recovery_validation([5], [6]))                   # False — single code miss
print(Solution().recovery_validation([1, 3, 5, 7], [2, 4, 6]))   # False — all misses
print(Solution().recovery_validation([10], [10, 20, 30]))         # True  — first attempt matches
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int binarySearch(int[] arr, int target) {

            // Starting index of the search range
            int low = 0;

            // Ending index of the search range
            int high = arr.length - 1;

            while (low <= high) {

                // Calculate the middle index
                int mid = (low + high) / 2;

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

        public boolean recoveryValidation(
            int[] recoveryCodes,
            int[] attempts
        ) {

            // Iterate through each attempted recovery code
            for (int attempt : attempts) {

                // If the recovery code is acceptable, return true
                if (binarySearch(recoveryCodes, attempt) != -1) {
                    return true;
                }
            }

            // No acceptable recovery code found, return false
            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().recoveryValidation(new int[]{1, 4, 7}, new int[]{2, 4}));           // true
        System.out.println(new Solution().recoveryValidation(new int[]{5, 9, 11, 12}, new int[]{2, 9, 12}));  // true
        System.out.println(new Solution().recoveryValidation(new int[]{1, 2, 3}, new int[]{5, 6}));           // false

        // Edge cases
        System.out.println(new Solution().recoveryValidation(new int[]{1, 2, 3}, new int[]{}));               // false — no attempts
        System.out.println(new Solution().recoveryValidation(new int[]{5}, new int[]{5}));                    // true  — single code match
        System.out.println(new Solution().recoveryValidation(new int[]{5}, new int[]{6}));                    // false — single code miss
        System.out.println(new Solution().recoveryValidation(new int[]{1, 3, 5, 7}, new int[]{2, 4, 6}));    // false — all misses
        System.out.println(new Solution().recoveryValidation(new int[]{10}, new int[]{10, 20, 30}));          // true  — first attempt matches
    }
}
```

### Complexity

`O(N log M)` time where `N = len(attempts)`, `M = len(recoveryCodes)`. `O(1)` space.

</details>

***

# Reverse Binary Search

Same algorithm with one comparison flipped: works on descending-sorted arrays.

## The Problem

Given a descending-sorted array `arr` and `target`, return target's index, or `-1`.

```
Input:  arr = [6, 5, 4, 3, 2, 1], target = 3
Output: 3

Input:  arr = [6, 5, 4, 3, 2, 1], target = 6
Output: 0

Input:  arr = [6, 5, 4, 3, 2, 1], target = 10
Output: -1
```

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

The skeleton is identical to plain binary search — only the comparison logic flips. In ascending order: `arr[mid] < target` means "look right." In descending order: `arr[mid] < target` means "look *left*" (because larger values are on the left).


```python run
from typing import List

class Solution:
    def reverse_binary_search(self, arr: List[int], target: int) -> int:

        # Starting index of the search range (leftmost element)
        low = 0

        # Ending index of the search range (rightmost element)
        high = len(arr) - 1

        while low <= high:

            # Calculate the middle index to avoid potential overflow
            mid = low + (high - low) // 2

            # Found the target, return the index
            if arr[mid] == target:
                return mid

            # Since the array is sorted in descending order:
            # If arr[mid] is smaller than the target,
            # move to the left half (where larger elements are)
            elif arr[mid] < target:
                high = mid - 1

            # Else if arr[mid] is greater than the target,
            # move to the right half (where smaller elements are)
            else:
                low = mid + 1

        # Target not found in the array
        return -1


# Examples from the problem statement
print(Solution().reverse_binary_search([6, 5, 4, 3, 2, 1], 3))   # 3
print(Solution().reverse_binary_search([6, 5, 4, 3, 2, 1], 6))   # 0
print(Solution().reverse_binary_search([6, 5, 4, 3, 2, 1], 10))  # -1

# Edge cases
print(Solution().reverse_binary_search([], 3))                    # -1 — empty array
print(Solution().reverse_binary_search([5], 5))                   # 0  — single element present
print(Solution().reverse_binary_search([5], 3))                   # -1 — single element absent
print(Solution().reverse_binary_search([6, 5, 4, 3, 2, 1], 1))   # 5  — target at last index
print(Solution().reverse_binary_search([5, 3], 3))                # 1  — two elements, last
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int reverseBinarySearch(int[] arr, int target) {

            // Starting index of the search range (leftmost element)
            int low = 0;

            // Ending index of the search range (rightmost element)
            int high = arr.length - 1;

            while (low <= high) {

                // Calculate the middle index to avoid potential overflow
                int mid = low + (high - low) / 2;

                // Found the target, return the index
                if (arr[mid] == target) {
                    return mid;
                }

                // Since the array is sorted in descending order:
                // If arr[mid] is smaller than the target,
                // move to the left half (where larger elements are)
                else if (arr[mid] < target) {
                    high = mid - 1;
                }

                // Else if arr[mid] is greater than the target,
                // move to the right half (where smaller elements are)
                else {
                    low = mid + 1;
                }
            }

            // Target not found in the array
            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseBinarySearch(new int[]{6, 5, 4, 3, 2, 1}, 3));   // 3
        System.out.println(new Solution().reverseBinarySearch(new int[]{6, 5, 4, 3, 2, 1}, 6));   // 0
        System.out.println(new Solution().reverseBinarySearch(new int[]{6, 5, 4, 3, 2, 1}, 10));  // -1

        // Edge cases
        System.out.println(new Solution().reverseBinarySearch(new int[]{}, 3));                    // -1 — empty array
        System.out.println(new Solution().reverseBinarySearch(new int[]{5}, 5));                   // 0  — single element present
        System.out.println(new Solution().reverseBinarySearch(new int[]{5}, 3));                   // -1 — single element absent
        System.out.println(new Solution().reverseBinarySearch(new int[]{6, 5, 4, 3, 2, 1}, 1));   // 5  — target at last index
        System.out.println(new Solution().reverseBinarySearch(new int[]{5, 3}, 3));                // 1  — two elements, last
    }
}
```

### Complexity

`O(log n)` time, `O(1)` space.

</details>

***

# Minimum Shared Element

Multi-row sorted matrix. Find the smallest element that appears in *every* row.

## The Problem

Given an `N × M` matrix where each row is sorted ascending, return the smallest element present in all rows. Return `-1` if no such element exists. **Must run in `O(N log M)`.**

```
Input:  matrix = [[1, 2, 3]]
Output: 1   (only one row; smallest element is 1)

Input:  matrix = [[2, 3, 4], [1, 3, 5], [1, 2, 3]]
Output: 3   (only 3 is in every row; rows have [2,3,4], [1,3,5], [1,2,3])

Input:  matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
Output: -1   (no shared element)
```

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

Iterate over the elements of the first row (left to right, ascending). For each, binary-search every other row. The first element that's found in all rows is the answer (smallest because the first row is sorted).


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
            mid: int = (low + high) // 2

            # Found the target, return the index
            if arr[mid] == target:
                return mid

            # If the arr[mid] is less than the target, adjust the search
            # range to the right half
            if arr[mid] < target:
                low = mid + 1

            # Else if the arr[mid] is greater than the target, adjust
            # the search range to the left half
            else:
                high = mid - 1

        # Target not found in the array
        return -1

    def minimum_shared_element(self, matrix: List[List[int]]) -> int:
        rows: int = len(matrix)

        # If the matrix has no rows, return -1
        if rows == 0:
            return -1

        cols: int = len(matrix[0])

        # Iterate through the columns of the matrix
        for col in range(cols):
            target: int = matrix[0][col]
            found: bool = True

            # Check if the target element is present in all rows
            for row in range(1, rows):

                # Use binary search to check if target is present in this
                # row
                if self.binary_search(matrix[row], target) == -1:

                    # Target not found in this row, break out of the loop
                    found = False
                    break

            # If target is found in all rows, it is the smallest common
            # element
            if found:
                return target

        # No common element found in all rows
        return -1


# Examples from the problem statement
print(Solution().minimum_shared_element([[1, 2, 3]]))                         # 1
print(Solution().minimum_shared_element([[2, 3, 4], [1, 3, 5], [1, 2, 3]]))  # 3
print(Solution().minimum_shared_element([[1, 2, 3], [4, 5, 6], [7, 8, 9]]))  # -1

# Edge cases
print(Solution().minimum_shared_element([[]]))                                # -1 — empty row
print(Solution().minimum_shared_element([[5], [5], [5]]))                     # 5  — single-col all same
print(Solution().minimum_shared_element([[5], [6]]))                          # -1 — single-col no match
print(Solution().minimum_shared_element([[1, 2, 3], [1, 2, 3]]))              # 1  — identical rows
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int binarySearch(int[] arr, int target) {

            // Starting index of the search range
            int low = 0;

            // Ending index of the search range
            int high = arr.length - 1;

            while (low <= high) {

                // Calculate the middle index
                int mid = (low + high) / 2;

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

        public int minimumSharedElement(int[][] matrix) {
            int rows = matrix.length;

            // If the matrix has no rows, return -1
            if (rows == 0) {
                return -1;
            }

            int cols = matrix[0].length;

            // Iterate through the columns of the matrix
            for (int col = 0; col < cols; col++) {
                int target = matrix[0][col];
                boolean found = true;

                // Check if the target element is present in all rows
                for (int row = 1; row < rows; row++) {

                    // Use binary search to check if target is present in
                    // this row
                    if (binarySearch(matrix[row], target) == -1) {

                        // Target not found in this row, break out of the
                        // loop
                        found = false;
                        break;
                    }
                }

                // If target is found in all rows, it is the smallest common
                // element
                if (found) {
                    return target;
                }
            }

            // No common element found in all rows
            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().minimumSharedElement(new int[][]{{1, 2, 3}}));                               // 1
        System.out.println(new Solution().minimumSharedElement(new int[][]{{2, 3, 4}, {1, 3, 5}, {1, 2, 3}}));        // 3
        System.out.println(new Solution().minimumSharedElement(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}));        // -1

        // Edge cases
        System.out.println(new Solution().minimumSharedElement(new int[][]{{5}, {5}, {5}}));                           // 5  — single-col all same
        System.out.println(new Solution().minimumSharedElement(new int[][]{{5}, {6}}));                                // -1 — single-col no match
        System.out.println(new Solution().minimumSharedElement(new int[][]{{1, 2, 3}, {1, 2, 3}}));                    // 1  — identical rows
    }
}
```

### Complexity

`O(M · N · log M)` where `N = rows`, `M = cols`. The outer loop iterates over the first row (`M` elements); for each, we binary-search the remaining `N - 1` rows in `O(log M)` each.

</details>

***

# Intersecting Elements

Same setup as the previous problem; collect *all* shared elements instead of just the smallest.

## The Problem

Given an `N × M` matrix where each row is sorted ascending, return a sorted list of all elements present in every row.

```
Input:  matrix = [[1, 2, 3, 4], [0, 1, 4, 5]]
Output: [1, 4]

Input:  matrix = [[5, 9, 11], [1, 4, 5], [2, 5, 9]]
Output: [5]

Input:  matrix = [[1, 2, 3, 4], [0, 1, 4, 5], [6, 7, 8]]
Output: []
```

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

Same as the previous problem but accumulate matches into a result list instead of returning the first.


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
            mid: int = (low + high) // 2

            # Found the target, return the index
            if arr[mid] == target:
                return mid

            # If the arr[mid] is less than the target, adjust the search
            # range to the right half
            if arr[mid] < target:
                low = mid + 1

            # Else if the arr[mid] is greater than the target, adjust
            # the search range to the left half
            else:
                high = mid - 1

        # Target not found in the array
        return -1

    def intersecting_elements(
        self, matrix: List[List[int]]
    ) -> List[int]:
        rows: int = len(matrix)

        # If the matrix has no rows, return an empty result
        if rows == 0:
            return []

        cols: int = len(matrix[0])
        result: List[int] = []

        # Iterate through the columns of the matrix
        for col in range(cols):
            target: int = matrix[0][col]
            found: bool = True

            # Check if the target element is present in all rows
            for row in range(1, rows):

                # Use binary search to check if target is present in this
                # row
                if self.binary_search(matrix[row], target) == -1:

                    # Target not found in this row, break out of the loop
                    found = False
                    break

            # If target is found in all rows, add it to the result
            if found:
                result.append(target)

        # Return the intersection of elements present in all rows
        return result


# Examples from the problem statement
print(Solution().intersecting_elements([[1, 2, 3, 4], [0, 1, 4, 5]]))           # [1, 4]
print(Solution().intersecting_elements([[5, 9, 11], [1, 4, 5], [2, 5, 9]]))     # [5]
print(Solution().intersecting_elements([[1, 2, 3, 4], [0, 1, 4, 5], [6, 7, 8]]))  # []

# Edge cases
print(Solution().intersecting_elements([[1, 2, 3]]))                              # [1, 2, 3] — single row
print(Solution().intersecting_elements([[5], [5], [5]]))                          # [5] — single-col match
print(Solution().intersecting_elements([[5], [6]]))                               # [] — single-col no match
print(Solution().intersecting_elements([[1, 2, 3], [1, 2, 3]]))                   # [1, 2, 3] — identical rows
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private int binarySearch(int[] arr, int target) {

            // Starting index of the search range
            int low = 0;

            // Ending index of the search range
            int high = arr.length - 1;

            while (low <= high) {

                // Calculate the middle index
                int mid = (low + high) / 2;

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

        public List<Integer> intersectingElements(int[][] matrix) {
            int rows = matrix.length;

            // If the matrix has no rows, return an empty result
            if (rows == 0) {
                return new ArrayList<>();
            }

            int cols = matrix[0].length;
            List<Integer> result = new ArrayList<>();

            // Iterate through the columns of the matrix
            for (int col = 0; col < cols; col++) {
                int target = matrix[0][col];
                boolean found = true;

                // Check if the target element is present in all rows
                for (int row = 1; row < rows; row++) {

                    // Use binary search to check if target is present in
                    // this row
                    if (binarySearch(matrix[row], target) == -1) {

                        // Target not found in this row, break out of the
                        // loop
                        found = false;
                        break;
                    }
                }

                // If target is found in all rows, add it to the result
                if (found) {
                    result.add(target);
                }
            }

            // Return the intersection of elements present in all rows
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().intersectingElements(new int[][]{{1, 2, 3, 4}, {0, 1, 4, 5}}));           // [1, 4]
        System.out.println(new Solution().intersectingElements(new int[][]{{5, 9, 11}, {1, 4, 5}, {2, 5, 9}}));     // [5]
        System.out.println(new Solution().intersectingElements(new int[][]{{1, 2, 3, 4}, {0, 1, 4, 5}, {6, 7, 8}}));  // []

        // Edge cases
        System.out.println(new Solution().intersectingElements(new int[][]{{1, 2, 3}}));                              // [1, 2, 3] — single row
        System.out.println(new Solution().intersectingElements(new int[][]{{5}, {5}, {5}}));                          // [5] — single-col match
        System.out.println(new Solution().intersectingElements(new int[][]{{5}, {6}}));                               // [] — single-col no match
        System.out.println(new Solution().intersectingElements(new int[][]{{1, 2, 3}, {1, 2, 3}}));                   // [1, 2, 3] — identical rows
    }
}
```

### Complexity

Same as the previous problem: `O(M · N · log M)` time, `O(M)` space for the result.

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


The binary search pattern: when the input is sorted (in any monotone direction), `O(log n)` lookup beats linear scan. Recognise it by the sorted structure plus a "find this thing" question. The four problems show four flavours: single-array lookup, descending sort, multi-row matrix membership, and intersection extraction — all using binary search as the inner primitive.

The next lesson lifts the lookup to **lower bound** problems — same pattern, but the question is "where does this go?" rather than "is it there?" That's the right tool for insertion-position queries, "first occurrence of," and many real-world database operations.

**Transfer challenge — try before the Lower Bound Pattern lesson:** Given two sorted arrays `A` and `B`, return their intersection (elements in both). Use binary search to make it `O(min(N, M) · log max(N, M))`. Hint: iterate over the shorter, binary-search in the longer.

</details>