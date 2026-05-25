---
title: "Toggle Kth Bit"
summary: "Given num and k, flip the kth bit — 0 becomes 1, 1 becomes 0."
prereqs:
  - 01-pattern-kth-bit/01-pattern
difficulty: easy
---

# Toggle Kth Bit

## The Problem

Given `num` and `k`, flip the kth bit — 0 becomes 1, 1 becomes 0.

```
Input:  num = 1, k = 1
Output: 0                        0001 → 0000

Input:  num = 3, k = 1
Output: 2                        0011 → 0010

Input:  num = 3, k = 2
Output: 1                        0011 → 0001
```

<details>
<summary><h2>The Recurrence</h2></summary>


Build mask `1 << (k - 1)`. XOR with `num`: every bit of `num` is preserved except bit k, which is flipped.

```
result = num ^ (1 << (k - 1))
```

> *Pause. Why does XOR flip exactly one bit? Predict the truth-table reason.*

XOR's truth table: `0 ^ 0 = 0`, `1 ^ 0 = 1`, `0 ^ 1 = 1`, `1 ^ 1 = 0`. XORing with 0 (every mask bit except bit k) leaves the original bit untouched. XORing with 1 (only bit k of the mask) flips that bit. The "preserve under XOR with 0, flip under XOR with 1" property is the engine behind every XOR-based algorithm in this section.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def toggle_kth_bit(self, num: int, k: int) -> int:

        # To toggle the Kth bit of a number, we can use bitwise XOR
        # operation. We create a mask by left-shifting 1 by (k-1)
        # positions. Then, we perform bitwise XOR between the given
        # number and the mask to toggle the Kth bit.

        return num ^ (1 << (k - 1))


# Examples from the problem statement
print(Solution().toggle_kth_bit(1, 1))    # 0
print(Solution().toggle_kth_bit(3, 1))    # 2
print(Solution().toggle_kth_bit(3, 2))    # 1

# Edge cases
print(Solution().toggle_kth_bit(0, 1))    # 1
print(Solution().toggle_kth_bit(0, 3))    # 4
print(Solution().toggle_kth_bit(7, 1))    # 6
print(Solution().toggle_kth_bit(7, 3))    # 3
print(Solution().toggle_kth_bit(8, 4))    # 0
```

```java run
public class Main {
    static class Solution {
        public int toggleKthBit(int num, int k) {

            // To toggle the Kth bit of a number, we can use bitwise XOR
            // operation. We create a mask by left-shifting 1 by (k-1)
            // positions. Then, we perform bitwise XOR between the given
            // number and the mask to toggle the Kth bit.

            return num ^ (1 << (k - 1));
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().toggleKthBit(1, 1));    // 0
        System.out.println(new Solution().toggleKthBit(3, 1));    // 2
        System.out.println(new Solution().toggleKthBit(3, 2));    // 1

        // Edge cases
        System.out.println(new Solution().toggleKthBit(0, 1));    // 1
        System.out.println(new Solution().toggleKthBit(0, 3));    // 4
        System.out.println(new Solution().toggleKthBit(7, 1));    // 6
        System.out.println(new Solution().toggleKthBit(7, 3));    // 3
        System.out.println(new Solution().toggleKthBit(8, 4));    // 0
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
