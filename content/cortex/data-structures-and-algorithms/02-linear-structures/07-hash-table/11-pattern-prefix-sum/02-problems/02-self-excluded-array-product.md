---
title: "Self Excluded Array Product"
summary: "Given arr, return an array product where product[i] equals the product of all elements except arr[i]. Solve in O(n) and without division."
prereqs:
  - 11-pattern-prefix-sum/01-pattern
difficulty: medium
---

# Self excluded array product

## Problem Statement

Given `arr`, return an array `product` where `product[i]` equals the product of all elements **except** `arr[i]`. Solve in `O(n)` and **without division**.

### Example 1
> -   **Input:** `[1, 2, 3, 4]` → **Output:** `[24, 12, 8, 6]`

### Example 2
> -   **Input:** `[2, 3, 0]` → **Output:** `[0, 0, 6]`

### Example 3
> -   **Input:** `[3, 4]` → **Output:** `[4, 3]`

<details>
<summary><h2>Approach</h2></summary>


This is the prefix-sum trick generalised to **prefix products**. Build two arrays:

- `prefix[i] = arr[0] * arr[1] * ... * arr[i-1]` (product of everything strictly before `i`).
- `suffix[i] = arr[i+1] * arr[i+2] * ... * arr[n-1]` (product of everything strictly after `i`).

Then `product[i] = prefix[i] * suffix[i]`. Two passes (one left-to-right, one right-to-left), no division, O(N) time and O(N) space (which can be optimised to O(1) extra by computing one direction in-place).

> 🖼 Diagram — Self-excluded product — prefix product holds "everything before me", suffix product holds "everything after me", and their pointwise product is the answer. The technique is prefix-sum's multiplicative cousin.
```d2
direction: right

arr: "arr" {
  grid-columns: 4
  grid-gap: 0
  a0: "1"
  a1: "2"
  a2: "3"
  a3: "4"
}

prefix: "prefix (product of arr[0..i-1])" {
  grid-columns: 4
  grid-gap: 0
  p0: "1"
  p1: "1"
  p2: "2"
  p3: "6"
}

suffix: "suffix (product of arr[i+1..n-1])" {
  grid-columns: 4
  grid-gap: 0
  s0: "24"
  s1: "12"
  s2: "4"
  s3: "1"
}

product: "product = prefix * suffix" {
  grid-columns: 4
  grid-gap: 0
  r0: "24" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r1: "12" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r2: "8" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r3: "6" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
}
```

<p align="center"><strong>Self-excluded product — prefix product holds "everything before me", suffix product holds "everything after me", and their pointwise product is the answer. The technique is prefix-sum's multiplicative cousin.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def self_excluded_array_product(self, arr: List[int]) -> List[int]:
        n: int = len(arr)
        prefix_product: List[int] = [0] * n
        suffix_product: List[int] = [0] * n
        result: List[int] = [0] * n

        # Prefix product
        prefix_product[0] = arr[0]
        for i in range(1, n):
            prefix_product[i] = prefix_product[i - 1] * arr[i]

        # Suffix product
        suffix_product[n - 1] = arr[n - 1]
        for i in range(n - 2, -1, -1):
            suffix_product[i] = suffix_product[i + 1] * arr[i]

        # Result calculation
        result[0] = suffix_product[1]
        result[n - 1] = prefix_product[n - 2]
        for i in range(1, n - 1):
            result[i] = prefix_product[i - 1] * suffix_product[i + 1]

        return result


# Examples from the problem statement
print(Solution().self_excluded_array_product([1, 2, 3, 4]))  # [24, 12, 8, 6]
print(Solution().self_excluded_array_product([2, 3, 0]))     # [0, 0, 6]
print(Solution().self_excluded_array_product([3, 4]))        # [4, 3]

# Edge cases
print(Solution().self_excluded_array_product([1, 1]))        # [1, 1]
print(Solution().self_excluded_array_product([2, 2]))        # [2, 2]
print(Solution().self_excluded_array_product([1, 2, 3]))     # [6, 3, 2]
print(Solution().self_excluded_array_product([0, 0]))        # [0, 0]
print(Solution().self_excluded_array_product([5, 1, 1, 1]))  # [1, 5, 5, 5]
```

```java run
import java.util.Arrays;

public class Main {
    static class Solution {
        public int[] selfExcludedArrayProduct(int[] arr) {
            int n = arr.length;
            int[] prefixProduct = new int[n];
            int[] suffixProduct = new int[n];
            int[] result = new int[n];

            // Prefix product
            prefixProduct[0] = arr[0];
            for (int i = 1; i < n; i++) {
                prefixProduct[i] = prefixProduct[i - 1] * arr[i];
            }

            // Suffix product
            suffixProduct[n - 1] = arr[n - 1];
            for (int i = n - 2; i >= 0; i--) {
                suffixProduct[i] = suffixProduct[i + 1] * arr[i];
            }

            // Result calculation
            result[0] = suffixProduct[1];
            result[n - 1] = prefixProduct[n - 2];
            for (int i = 1; i < n - 1; i++) {
                result[i] = prefixProduct[i - 1] * suffixProduct[i + 1];
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 2, 3, 4})));  // [24, 12, 8, 6]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{2, 3, 0})));     // [0, 0, 6]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{3, 4})));        // [4, 3]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 1})));        // [1, 1]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{2, 2})));        // [2, 2]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 2, 3})));     // [6, 3, 2]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{0, 0})));        // [0, 0]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{5, 1, 1, 1}))); // [1, 5, 5, 5]
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
