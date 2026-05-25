---
title: "Power of 2"
summary: "Given an integer, return true if it's a positive power of 2 (1, 2, 4, 8, …); else false."
prereqs:
  - 06-pattern-applications/01-pattern
difficulty: easy
---

# Power of 2

## The Problem

Given an integer, return `true` if it's a positive power of 2 (1, 2, 4, 8, …); else `false`.

```
Input:  num = 1   →  true     2^0
Input:  num = 8   →  true     2^3
Input:  num = 3   →  false
```

<details>
<summary><h2>The Recurrence</h2></summary>


A power of 2 has *exactly one* set bit. From lesson 2, `(n & (n - 1)) == 0` exactly when `n` has zero or one set bits. Combine with `n > 0` to exclude zero (which has zero set bits):

```
is_power_of_2 = n > 0 and (n & (n - 1)) == 0
```

> *Pause. Why does <code>n > 0</code> matter? What does <code>(0 & -1) == 0</code> evaluate to?*

In two's complement, `0 - 1 = -1` (all bits 1). `0 & -1 = 0`. Without the `n > 0` guard, the function would return `true` for `n = 0` — but 0 isn't a power of 2 (`2^k > 0` for any integer k). The guard plugs that hole.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def power_of2(self, num: int) -> bool:

        # Check if the number is positive
        # and if the bitwise AND of num and (num - 1) is zero
        # If both conditions are true, return True, otherwise False
        return num > 0 and (num & (num - 1)) == 0


# Examples from the problem statement
print(Solution().power_of2(1))      # True
print(Solution().power_of2(8))      # True
print(Solution().power_of2(3))      # False

# Edge cases
print(Solution().power_of2(0))      # False
print(Solution().power_of2(-1))     # False
print(Solution().power_of2(2))      # True
print(Solution().power_of2(16))     # True
print(Solution().power_of2(6))      # False
```

```java run
public class Main {
    static class Solution {
        public boolean powerOf2(int num) {

            // Check if the number is positive
            // and if the bitwise AND of num and (num - 1) is zero
            // If both conditions are true, return true, otherwise false
            return num > 0 && (num & (num - 1)) == 0;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().powerOf2(1));      // true
        System.out.println(new Solution().powerOf2(8));      // true
        System.out.println(new Solution().powerOf2(3));      // false

        // Edge cases
        System.out.println(new Solution().powerOf2(0));      // false
        System.out.println(new Solution().powerOf2(-1));     // false
        System.out.println(new Solution().powerOf2(2));      // true
        System.out.println(new Solution().powerOf2(16));     // true
        System.out.println(new Solution().powerOf2(6));      // false
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
