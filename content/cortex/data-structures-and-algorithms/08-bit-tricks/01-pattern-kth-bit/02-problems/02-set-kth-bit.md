---
title: "Set Kth Bit"
summary: "Given num and k, return num with its kth bit forced to 1. If the bit was already 1, the value is unchanged."
prereqs:
  - 01-pattern-kth-bit/01-pattern
difficulty: easy
---

# Set Kth Bit

## The Problem

Given `num` and `k`, return `num` with its kth bit forced to 1. If the bit was already 1, the value is unchanged.

```
Input:  num = 0, k = 1
Output: 1                        0000 → 0001

Input:  num = 2, k = 2
Output: 2                        0010 — bit 2 already set; no change

Input:  num = 2, k = 1
Output: 3                        0010 → 0011
```

<details>
<summary><h2>The Recurrence</h2></summary>


Build mask `1 << (k - 1)`. OR with `num`: every bit of `num` is preserved except bit k, which becomes 1 (because `1 OR anything = 1`).

```
result = num | (1 << (k - 1))
```

> *Pause. Why does OR preserve the other bits exactly? Predict.*

OR's truth table: `0 OR 0 = 0`, `1 OR 0 = 1`, `0 OR 1 = 1`, `1 OR 1 = 1`. ORing with 0 (every bit of the mask except bit k) leaves the original bit untouched. ORing with 1 (only bit k of the mask) forces that bit to 1 regardless of its prior value. Surgical edit.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def set_kth_bit(self, num: int, k: int) -> int:

        # To turn on the Kth bit of a number, we can use bitwise OR
        # operation. We create a mask by left-shifting 1 by (k-1)
        # positions. Then, we perform bitwise OR between the given number
        # and the mask to turn on the Kth bit.
        return num | (1 << (k - 1))


# Examples from the problem statement
print(Solution().set_kth_bit(0, 1))     # 1
print(Solution().set_kth_bit(2, 2))     # 2
print(Solution().set_kth_bit(2, 1))     # 3

# Edge cases
print(Solution().set_kth_bit(0, 4))     # 8
print(Solution().set_kth_bit(7, 1))     # 7
print(Solution().set_kth_bit(7, 4))     # 15
print(Solution().set_kth_bit(16, 1))    # 17
print(Solution().set_kth_bit(1, 1))     # 1
```

```java run
public class Main {
    static class Solution {
        public int setKthBit(int num, int k) {

            // To turn on the Kth bit of a number, we can use bitwise OR
            // operation. We create a mask by left-shifting 1 by (k-1)
            // positions. Then, we perform bitwise OR between the given
            // number and the mask to turn on the Kth bit.

            return num | (1 << (k - 1));
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().setKthBit(0, 1));     // 1
        System.out.println(new Solution().setKthBit(2, 2));     // 2
        System.out.println(new Solution().setKthBit(2, 1));     // 3

        // Edge cases
        System.out.println(new Solution().setKthBit(0, 4));     // 8
        System.out.println(new Solution().setKthBit(7, 1));     // 7
        System.out.println(new Solution().setKthBit(7, 4));     // 15
        System.out.println(new Solution().setKthBit(16, 1));    // 17
        System.out.println(new Solution().setKthBit(1, 1));     // 1
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
