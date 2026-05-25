---
title: "Calculate Square Root"
summary: "Given a non-negative integer num, return its integer square root (floor)."
prereqs:
  - 12-pattern-maximum-predicate-search/01-pattern
difficulty: easy
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

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Examples — missing, needs to be written -->
<!--       Guidance: min 3 examples: basic / variant / edge -->

<!-- TODO: Intuition — missing, needs to be written -->
<!--       Guidance: 3 paragraphs: brute force / observation / pattern fit -->

<!-- TODO: Applying the Diagnostic Questions — missing, needs to be written -->
<!--       Guidance: REQUIRED, never optional -->
<!--       Guidance: 4-row table. Columns: 'Check' | 'Answer for [Problem Name]' -->
<!--       Guidance: Rows: two positions simultaneously / one near start one near end / both move inward / simple O(1) work at each step -->

<!-- TODO: Approach — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Solution — missing, needs to be written -->
<!--       Guidance: Python block then Java block -->

<!-- TODO: Dry Run — missing, needs to be written -->
<!--       Guidance: walk through a small example step by step -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table: time / space / why -->

<!-- TODO: Edge Cases — missing, needs to be written -->
<!--       Guidance: table, min 5 rows -->

<!-- TODO: Key Takeaway — missing, needs to be written -->
<!--       Guidance: 1–2 sentences -->
