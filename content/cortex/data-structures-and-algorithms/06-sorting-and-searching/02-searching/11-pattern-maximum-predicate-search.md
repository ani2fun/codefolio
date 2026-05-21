# 11. Pattern: Maximum Predicate Search

The dual of the Minimum Predicate Search Pattern lesson. Where the previous pattern found the *minimum* value satisfying a predicate (false-then-true), this pattern finds the *maximum* value satisfying one (true-then-false). The algorithm is the mirror image — same binary search shell, flipped predicate direction.

By the end of this lesson you'll know the diagnostic checks, the canonical "maximum-x-with-P-true" template, and four worked problems: integer square root, staircase building, ribbon cutting, and water equalisation.

## Table of contents

1. [Identifying the pattern](#identifying-the-pattern)
2. [Calculate square root](#calculate-square-root)
3. [Build staircase](#build-staircase)
4. [K ribbons](#k-ribbons)
5. [Equalise water](#equalise-water)

***

# Identifying the Pattern

| # | Question | If "yes," the pattern fits because... |
|---|---|---|
| **Q1** | We're optimizing for the *maximum* value satisfying a constraint? | Binary search finds the flip point. |
| **Q2** | The constraint is *monotonic* — if `x` works, then `x − 1` also works? | Required for predicate to have a unique flip. |
| **Q3** | Predicate evaluable in `O(f(n))`? | Total cost: `O(f(n) · log range)`. |

---

## The Template

The *upper* form differs from minimum-predicate-search in two ways:
1. The mid calculation uses `low + (high - low + 1) / 2` to avoid infinite loops when `low` and `high` are adjacent.
2. The successful predicate moves `low = mid` (not `high = mid`); the failure moves `high = mid - 1`.

```python run
def max_predicate_search(low, high, predicate):
    while low < high:
        mid = low + (high - low + 1) // 2          # +1 ensures mid != low when adjacent
        if predicate(mid):                          # mid works → try larger
            low = mid
        else:                                        # mid doesn't work → try smaller
            high = mid - 1
    return low
```

The `+ 1` in the mid calculation is the key fix. Without it, when `low = high - 1` and the predicate is true at `mid = low`, we'd set `low = low` and loop forever.

---

# Calculate Square Root

## The Problem

Given a non-negative integer `num`, return its integer square root (floor).

```
Input:  num = 4
Output: 2

Input:  num = 5
Output: 2

Input:  num = 50
Output: 7
```

<details>
<summary><h2>The Solution</h2></summary>


Binary-search `x` in `[1, num]`. Predicate: `x * x <= num` (use `x <= num / x` to avoid overflow).


```python run
class Solution:

    # Predicate: checks if square of mid is less than or equal to num
    def is_square(self, mid: int, num: int) -> bool:
        return mid <= num // mid

    def calculate_square_root(self, num: int) -> int:
        if num == 0:
            return 0

        # Lowest possible square root
        low = 1

        # Highest possible square root
        high = num

        while low < high:

            # Calculate the middle value by adding 1 to get upper mid to
            # prevent infinite loop when low and high are adjacent
            mid = low + (high - low + 1) // 2

            # If the square of mid is less than or equal to num, low is
            # a valid candidate. Update the lower boundary to mid
            if self.is_square(mid, num):
                low = mid

            # If the square of mid is greater than num, update the
            # higher boundary
            else:
                high = mid - 1

        # Return the last valid candidate rounded down to
        # the nearest integer
        return low


# Examples from the problem statement
print(Solution().calculate_square_root(4))    # 2
print(Solution().calculate_square_root(5))    # 2
print(Solution().calculate_square_root(50))   # 7

# Edge cases
print(Solution().calculate_square_root(0))    # 0   (zero)
print(Solution().calculate_square_root(1))    # 1   (perfect square)
print(Solution().calculate_square_root(2))    # 1   (floor of sqrt(2))
print(Solution().calculate_square_root(9))    # 3   (perfect square)
print(Solution().calculate_square_root(8))    # 2   (floor of sqrt(8))
print(Solution().calculate_square_root(100))  # 10  (perfect square)
print(Solution().calculate_square_root(99))   # 9   (just below perfect square)
```

```java run
public class Main {
    static class Solution {

        // Predicate: checks if square of mid is less than or equal to num
        private boolean isSquare(int mid, int num) {

            // avoid overflow
            return mid <= num / mid;
        }

        public int calculateSquareRoot(int num) {
            if (num == 0) {
                return 0;
            }

            // Lowest possible square root
            int low = 1;

            // Highest possible square root
            int high = num;

            while (low < high) {

                // Calculate the middle value by adding 1 to get upper mid to
                // prevent infinite loop when low and high are adjacent
                int mid = low + (high - low + 1) / 2;

                // If the square of mid is less than or equal to num, low is
                // a valid candidate. Update the lower boundary to mid
                if (isSquare(mid, num)) {
                    low = mid;
                }

                // If the square of mid is greater than num, update the
                // higher boundary
                else {
                    high = mid - 1;
                }
            }

            // Return the last valid candidate rounded down to
            // the nearest integer
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().calculateSquareRoot(4));    // 2
        System.out.println(new Solution().calculateSquareRoot(5));    // 2
        System.out.println(new Solution().calculateSquareRoot(50));   // 7

        // Edge cases
        System.out.println(new Solution().calculateSquareRoot(0));    // 0
        System.out.println(new Solution().calculateSquareRoot(1));    // 1
        System.out.println(new Solution().calculateSquareRoot(2));    // 1
        System.out.println(new Solution().calculateSquareRoot(9));    // 3
        System.out.println(new Solution().calculateSquareRoot(8));    // 2
        System.out.println(new Solution().calculateSquareRoot(100));  // 10
        System.out.println(new Solution().calculateSquareRoot(99));   // 9
    }
}
```

</details>


***

# Build Staircase

## The Problem

Given `n` coins, build a staircase where the `i`th stair needs `i` coins. Return the number of complete stairs.

```
Input:  n = 6
Output: 3   (1 + 2 + 3 = 6)

Input:  n = 5
Output: 2   (1 + 2 = 3; can't build 3rd stair)

Input:  n = 7
Output: 3   (1 + 2 + 3 = 6; 1 coin left over, not enough for 4th)
```

<details>
<summary><h2>The Solution</h2></summary>


Binary-search `k` in `[0, n]`. Predicate: `k(k+1)/2 <= n`.


```python run
class Solution:

    # Predicate: Checks if mid rows can fit within n blocks
    def can_build(self, mid: int, n: int) -> bool:

        # sum of first mid natural numbers: mid*(mid+1)/2
        return mid * (mid + 1) // 2 <= n

    def build_staircase(self, n: int) -> int:

        # Lowest possible value for a complete row
        low = 0

        # Highest possible value for a complete row
        high = n

        while low < high:

            # Calculate the middle value by adding 1 to get upper mid to
            # prevent infinite loop when low and high are adjacent
            mid = low + (high - low + 1) // 2

            # If we can build mid rows, this is a possible answer
            # Update the lower boundary to mid
            if self.can_build(mid, n):
                low = mid

            # The sum is larger, search in the left half
            else:
                high = mid - 1

        # Return the largest complete row smaller than the given sum
        return low


# Examples from the problem statement
print(Solution().build_staircase(6))   # 3
print(Solution().build_staircase(5))   # 2
print(Solution().build_staircase(7))   # 3

# Edge cases
print(Solution().build_staircase(0))   # 0   (no coins)
print(Solution().build_staircase(1))   # 1   (exactly one stair)
print(Solution().build_staircase(2))   # 1   (1 coin short of 2 stairs)
print(Solution().build_staircase(3))   # 2   (1+2=3 coins)
print(Solution().build_staircase(10))  # 4   (1+2+3+4=10)
print(Solution().build_staircase(11))  # 4   (11 coins, 5th stair needs 5)
```

```java run
public class Main {
    static class Solution {

        // Predicate: Checks if mid rows can fit within n blocks
        private boolean canBuild(int mid, int n) {

            // sum of first mid natural numbers: mid*(mid+1)/2
            return (mid * (mid + 1)) / 2 <= n;
        }

        public int buildStaircase(int n) {

            // Lowest possible value for a complete row
            int low = 0;

            // Highest possible value for a complete row
            int high = n;

            while (low < high) {

                // Calculate the middle value by adding 1 to get upper mid to
                // prevent infinite loop when low and high are adjacent
                int mid = low + (high - low + 1) / 2;

                // If we can build mid rows, this is a possible answer
                // Update the lower boundary to mid
                if (canBuild(mid, n)) {
                    low = mid;
                }

                // The sum is larger, search in the left half
                else {
                    high = mid - 1;
                }
            }

            // Return the largest complete row smaller than the given sum
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().buildStaircase(6));   // 3
        System.out.println(new Solution().buildStaircase(5));   // 2
        System.out.println(new Solution().buildStaircase(7));   // 3

        // Edge cases
        System.out.println(new Solution().buildStaircase(0));   // 0
        System.out.println(new Solution().buildStaircase(1));   // 1
        System.out.println(new Solution().buildStaircase(2));   // 1
        System.out.println(new Solution().buildStaircase(3));   // 2
        System.out.println(new Solution().buildStaircase(10));  // 4
        System.out.println(new Solution().buildStaircase(11));  // 4
    }
}
```

</details>


***

# K Ribbons

## The Problem

Array of ribbons. Cut them to produce at least `k` pieces of equal length. Return the *maximum* such length, or `0` if impossible.

```
Input:  ribbons = [9, 7, 5], k = 3
Output: 5

Input:  ribbons = [9, 7, 5], k = 4
Output: 4

Input:  ribbons = [9, 7, 5], k = 30
Output: 0
```

<details>
<summary><h2>The Solution</h2></summary>


Predicate: "can we cut at least `k` ribbons of length `length`?" — sum of `r // length` for each ribbon. Binary-search `length` in `[1, 10^7]` (a safe ceiling from the problem constraints — wider than `max(ribbons)` but still `O(log range)`). After the loop, re-check the predicate at `low` to distinguish "no length works" (return `0`) from a real answer.


```python run
from typing import List

class Solution:

    # Predicate: checks if it's possible to cut at least 'k' ribbons of
    # length 'length'
    def can_cut(self, ribbons: List[int], length: int, k: int) -> bool:
        count = 0

        # Count how many pieces of 'length' we can cut from each ribbon
        for ribbon in ribbons:
            count += ribbon // length

        # Return true if we can cut at least 'k' ribbons of this length
        return count >= k

    def k_ribbons(self, ribbons: List[int], k: int) -> int:

        # Initialize the search range for ribbon lengths
        low = 1

        # Initialize the search range for ribbon lengths
        high = int(1e7)

        while low < high:

            # Calculate the middle value by adding 1 to get upper mid to
            # prevent infinite loop when low and high are adjacent
            mid = low + (high - low + 1) // 2

            # If we can cut at least 'k' ribbons of length 'mid' it is a
            # possible answer, so update the lower boundary to mid
            if self.can_cut(ribbons, mid, k):

                # Try to find a larger length
                low = mid

            # Otherwise, we can't cut 'k' ribbons of length 'mid' from
            # the given ribbons array.
            else:

                # Try to find a smaller length
                high = mid - 1

        # After the search, low is the maximum length we can cut
        # Check if we can actually cut at least 'k' ribbons of this
        # length
        if not self.can_cut(ribbons, low, k):
            return 0

        # Return the maximum ribbon length that can be obtained
        return low


# Examples from the problem statement
print(Solution().k_ribbons([9, 7, 5], 3))    # 5
print(Solution().k_ribbons([9, 7, 5], 4))    # 4
print(Solution().k_ribbons([9, 7, 5], 30))   # 0

# Edge cases
print(Solution().k_ribbons([1], 1))          # 1   (single ribbon, single cut)
print(Solution().k_ribbons([10], 1))         # 10  (whole ribbon)
print(Solution().k_ribbons([10], 3))         # 3   (10//3=3 ribbons of length 3)
print(Solution().k_ribbons([5, 5, 5], 3))    # 5   (each ribbon exactly k length)
print(Solution().k_ribbons([1, 2, 3], 10))   # 0   (not enough total length)
```

```java run
public class Main {
    static class Solution {

        // Predicate: checks if it's possible to cut at least 'k' ribbons of
        // length 'length'
        private boolean canCut(int[] ribbons, int length, int k) {
            int count = 0;

            // Count how many pieces of 'length' we can cut from each ribbon
            for (int ribbon : ribbons) {
                count += ribbon / length;
            }

            // Return true if we can cut at least 'k' ribbons of this length
            return count >= k;
        }

        public int kRibbons(int[] ribbons, int k) {

            // Initialize the search range for ribbon lengths
            int low = 1;

            // Initialize the search range for ribbon lengths
            int high = (int) 1e7;

            while (low < high) {

                // Calculate the middle value by adding 1 to get upper mid to
                // prevent infinite loop when low and high are adjacent
                int mid = low + (high - low + 1) / 2;

                // If we can cut at least 'k' ribbons of length 'mid' it is a
                // possible answer, so update the lower boundary to mid
                if (canCut(ribbons, mid, k)) {

                    // Try to find a larger length
                    low = mid;
                }

                // Otherwise, we can't cut 'k' ribbons of length 'mid' from
                // the given ribbons array.
                else {

                    // Try to find a smaller length
                    high = mid - 1;
                }
            }

            // After the search, low is the maximum length we can cut
            // Check if we can actually cut at least 'k' ribbons of this
            // length
            if (!canCut(ribbons, low, k)) {
                return 0;
            }

            // Return the maximum ribbon length that can be obtained
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().kRibbons(new int[]{9, 7, 5}, 3));    // 5
        System.out.println(new Solution().kRibbons(new int[]{9, 7, 5}, 4));    // 4
        System.out.println(new Solution().kRibbons(new int[]{9, 7, 5}, 30));   // 0

        // Edge cases
        System.out.println(new Solution().kRibbons(new int[]{1}, 1));          // 1
        System.out.println(new Solution().kRibbons(new int[]{10}, 1));         // 10
        System.out.println(new Solution().kRibbons(new int[]{10}, 3));         // 3
        System.out.println(new Solution().kRibbons(new int[]{5, 5, 5}, 3));    // 5
        System.out.println(new Solution().kRibbons(new int[]{1, 2, 3}, 10));   // 0
    }
}
```

</details>


***

# Equalise Water

## The Problem

Given an array of bucket water amounts and a `loss%` for transfers, find the maximum equal-water level achievable across all buckets.

```
Input:  buckets = [1, 5, 10], loss = 20
Output: 5.00000

Input:  buckets = [2, 4, 6], loss = 50
Output: 3.50000

Input:  buckets = [10, 10, 10, 10], loss = 40
Output: 10.00000
```

<details>
<summary><h2>The Solution</h2></summary>


Binary-search the target water level (scaled to avoid floating-point precision). Predicate: total available excess (after loss) ≥ total deficit. Use integer arithmetic with a scale factor of `1e5`.


```python run
from typing import List

class Solution:

    # Scale factor to avoid floating-point precision issues while
    # performing binary search. We scale all water amounts by 1e5
    # and perform integer arithmetic instead of floating-point.
    SCALE: int = int(1e5)

    # Predicate: checks if it's possible to make all buckets contain at
    # least 'target' liters of water
    def can_achieve_target(
        self, buckets: List[int], loss: float, target: int
    ) -> bool:
        total_excess = 0
        total_deficit = 0

        for water in buckets:

            # Scale the water amount to avoid floating-point precision
            water *= self.SCALE

            # If water in the bucket is more than the target, calculate
            # the excess
            if water > target:

                # Water that can be effectively transferred
                total_excess += ((water - target) * (100 - loss)) / 100
            else:

                # Water needed to fill this bucket
                total_deficit += target - water

        # We can achieve the target if total_excess is greater than or
        # equal to total_deficit
        return total_excess >= total_deficit

    def equalise_water(self, buckets: List[int], loss: float) -> float:

        # Binary search range is [0, max(bucket) * SCALE]
        low = 0
        high = max(buckets) * self.SCALE

        # Tolerance factor of 1e-5
        while low < high:

            # Calculate the middle target by adding 1 to get upper mid to
            # prevent infinite loop when low and high are adjacent
            mid = low + (high - low + 1) // 2

            # If we can achieve this target, this is a potential answer
            # So update the lower boundary to mid
            if self.can_achieve_target(buckets, loss, mid):

                # Try a larger target
                low = mid
            else:

                # Try a smaller target
                high = mid - 1

        return high / self.SCALE


# Examples from the problem statement
print(Solution().equalise_water([1, 5, 10], 20))       # 5.0
print(Solution().equalise_water([2, 4, 6], 50))        # 3.5
print(Solution().equalise_water([10, 10, 10, 10], 40)) # 10.0

# Edge cases
print(Solution().equalise_water([5], 0))               # 5.0  (single bucket)
print(Solution().equalise_water([5], 100))             # 5.0  (single bucket, any loss)
print(Solution().equalise_water([1, 1], 0))            # 1.0  (already equal, no loss)
print(Solution().equalise_water([0, 10], 0))           # 5.0  (no loss, perfect split)
print(Solution().equalise_water([0, 10], 100))         # 0.0  (100% loss — can't transfer)
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Scale factor to avoid floating-point precision issues while
        // performing binary search. We scale all water amounts by 1e5
        // and perform integer arithmetic instead of floating-point.
        private final int SCALE = 100000;

        // Predicate: checks if it's possible to make all buckets contain at
        // least 'target' liters of water
        private boolean canAchieveTarget(
            int[] buckets,
            double loss,
            int target
        ) {
            long totalExcess = 0;
            long totalDeficit = 0;

            for (long water : buckets) {

                // Scale the water amount to avoid floating-point precision
                water *= SCALE;

                // If water in the bucket is more than the target, calculate
                // the excess
                if (water > target) {

                    // Water that can be effectively transferred
                    totalExcess +=
                    ((water - target) * (100 - (long) loss)) / 100;
                }

                // If water in the bucket is less than the target, calculate
                // the deficit
                else {

                    // Water needed to fill this bucket
                    totalDeficit += target - water;
                }
            }

            // We can achieve the target if totalExcess is greater than or
            // equal to totalDeficit
            return totalExcess >= totalDeficit;
        }

        public double equaliseWater(int[] buckets, double loss) {

            // Binary search range is [0, max(bucket) * SCALE]
            int low = 0;
            int high = Arrays.stream(buckets).max().getAsInt() * SCALE;

            // Tolerance factor of 1e-5
            while (low < high) {

                // Calculate the middle target by adding 1 to get upper mid
                // to prevent infinite loop when low and high are adjacent
                int mid = low + (high - low + 1) / 2;

                // If we can achieve this target, this is a potential answer
                // So update the lower boundary to mid
                if (canAchieveTarget(buckets, loss, mid)) {

                    // Try a larger target
                    low = mid;
                }

                // If we can't achieve this target, try for a smaller target
                else {
                    high = mid - 1;
                }
            }

            return (double) high / SCALE;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().equaliseWater(new int[]{1, 5, 10}, 20));       // 5.0
        System.out.println(new Solution().equaliseWater(new int[]{2, 4, 6}, 50));        // 3.5
        System.out.println(new Solution().equaliseWater(new int[]{10, 10, 10, 10}, 40)); // 10.0

        // Edge cases
        System.out.println(new Solution().equaliseWater(new int[]{5}, 0));               // 5.0
        System.out.println(new Solution().equaliseWater(new int[]{5}, 100));             // 5.0
        System.out.println(new Solution().equaliseWater(new int[]{1, 1}, 0));            // 1.0
        System.out.println(new Solution().equaliseWater(new int[]{0, 10}, 0));           // 5.0
        System.out.println(new Solution().equaliseWater(new int[]{0, 10}, 100));         // 0.0
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Maximum-predicate-search is the dual of the Minimum Predicate Search Pattern lesson. Same algorithm shell, mirrored direction; the `+ 1` in the mid calculation prevents the infinite-loop pitfall when `low` and `high` become adjacent. The four problems showed integer square root (predicate: `mid² ≤ num`), staircase building (`k(k+1)/2 ≤ n`), ribbon cutting, and water equalisation.

This closes the searching section. You came in with linear scan; you leave with binary search and its variants (lower bound, upper bound), 2D extensions (matrix search, staircase), broken-input handling (rotated array), and the binary-search-on-the-answer family (predicate search) — covering practically every searching problem you'll encounter.

The next major topic is **dynamic programming**. DP builds on memoization (introduced in the Recursion section) and on this section's "binary search on the answer" mindset: many DP problems can be reformulated as predicate searches, and many predicate searches benefit from DP-style state caching inside their predicate.

</details>