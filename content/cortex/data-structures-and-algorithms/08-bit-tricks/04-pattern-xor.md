# 4. The XOR Pattern

XOR is the most underrated operator in programming. It looks like just-another-bitwise op until you notice three properties most operators don't have: it's **commutative** (`a ^ b = b ^ a`), **associative** (`(a ^ b) ^ c = a ^ (b ^ c)`), and **self-inverse** (`a ^ a = 0`, `a ^ 0 = a`). Combined, those three give XOR a magical talent: **applied twice, it cancels**. Apply XOR to a sequence of numbers in any order; each duplicate vanishes; only the singleton survivors remain. That single insight powers an entire family of algorithms — finding odd-occurring elements, swapping without a temporary, counting differing bits, recovering missing-and-duplicated values from a single pass — all in O(n) time and O(1) space.

By the end of this lesson you'll have written seven XOR-based algorithms, ranging from simple sign-comparison to recovering both a missing *and* duplicated number from one array. The throughline: **XOR cancels pairs**.

## Table of contents

1. [Why XOR Cancels](#why-xor-cancels)
2. [Have Opposite Signs](#have-opposite-signs)
3. [Swap Numbers Without a Temporary](#swap-numbers-without-a-temporary)
4. [Toggle Count](#toggle-count)
5. [Odd-Occurring Element](#odd-occurring-element)
6. [Odd-Occurring Element II](#odd-occurring-element-ii)
7. [Duplicate Element](#duplicate-element)
8. [Missing and Duplicated Elements](#missing-and-duplicated-elements)
9. [Final Takeaway](#final-takeaway)

***

# Why XOR Cancels

Three identities form the algebraic foundation for everything in this lesson:

| Identity | Why it matters |
|---|---|
| `a ^ a = 0` | Same-with-same cancels |
| `a ^ 0 = a` | Identity element |
| `(a ^ b) ^ c = a ^ (b ^ c)` and `a ^ b = b ^ a` | Order doesn't matter |

The combination means that XORing a sequence in *any* order, with arbitrary regrouping, gives the same answer. So if numbers come in pairs (each appearing twice), the pairs cancel pairwise — regardless of how they're interleaved. Whatever's left after all the cancellation is the XOR of the *unpaired* elements.

```d2
direction: right
ex: "XOR cancels pairs across an array" {
  grid-rows: 2
  grid-columns: 7
  grid-gap: 0
  v0: "2"
  v1: "2"
  v2: "2"
  v3: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  v4: "3"
  v5: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  v6: "3"
  l0: ""
  l1: "cancels"
  l2: ""
  l3: ""
  l4: ""
  l5: "cancels"
  l6: ""
}
```

<p align="center"><strong>For <code>arr = [2, 2, 2, 1, 3, 1, 3]</code>, the XOR of all elements collapses pairs (the two 1s cancel, both 3s cancel, two of the three 2s cancel) — leaving just the unpaired <code>2</code>. Order doesn't matter because XOR is commutative and associative.</strong></p>

> *Predict before reading on — for <code>[5, 5, 5, 5, 7]</code>, what's the XOR of all elements?*

`7`. Four 5s pair off and cancel completely (`5 ^ 5 ^ 5 ^ 5 = 0`); then `0 ^ 7 = 7`. The number of times each value appears determines whether it cancels (even count) or survives (odd count).

---

## Key Takeaway

XOR + commutative + associative + self-inverse = pairs cancel regardless of order. The XOR of an array is the XOR of its *odd-occurring* values.

***

# Have Opposite Signs

## The Problem

Given two 32-bit signed integers `num1` and `num2`, return `true` if they have opposite signs (one positive, one negative), `false` otherwise. Treat `0` as positive.

```
Input:  num1 = 10, num2 = -1     →  true
Input:  num1 = 2, num2 = -3      →  true
Input:  num1 = 9, num2 = 1       →  false
```

<details>
<summary><h2>The Recurrence</h2></summary>


In two's complement, the highest bit is the sign bit (1 ⇒ negative, 0 ⇒ non-negative). XORing two numbers gives a result with sign-bit set iff the two original sign-bits differ. A negative result therefore signals opposite signs.

```
opposite = (num1 ^ num2) < 0
```

> *Pause. Why does <code>(num1 ^ num2) < 0</code> work despite ignoring the lower 31 bits?*

Because in two's complement, "less than zero" is determined by the sign bit alone. Lower bits could be anything — the comparison only inspects the top bit. The XOR's top bit equals 1 iff the inputs' top bits differ. So this becomes a pure sign-bit XOR, expressed as a comparison.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def have_opposite_signs(self, num1: int, num2: int) -> bool:

        # XOR operation (^) will result in a negative number
        # only if the signs of n1 and n2 are different.
        return (num1 ^ num2) < 0


# Examples from the problem statement
print(Solution().have_opposite_signs(10, -1))     # True
print(Solution().have_opposite_signs(2, -3))      # True
print(Solution().have_opposite_signs(9, 1))       # False

# Edge cases
print(Solution().have_opposite_signs(-5, -10))    # False
print(Solution().have_opposite_signs(0, 0))       # False
print(Solution().have_opposite_signs(0, -1))      # False
print(Solution().have_opposite_signs(1, -1))      # True
print(Solution().have_opposite_signs(-1, 1))      # True
```

```java run
public class Main {
    static class Solution {
        public boolean haveOppositeSigns(int num1, int num2) {

            // XOR operation (^) will result in a negative number
            // only if the signs of n1 and n2 are different.
            return ((num1 ^ num2) < 0);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().haveOppositeSigns(10, -1));     // true
        System.out.println(new Solution().haveOppositeSigns(2, -3));      // true
        System.out.println(new Solution().haveOppositeSigns(9, 1));       // false

        // Edge cases
        System.out.println(new Solution().haveOppositeSigns(-5, -10));    // false
        System.out.println(new Solution().haveOppositeSigns(0, 0));       // false
        System.out.println(new Solution().haveOppositeSigns(0, -1));      // false
        System.out.println(new Solution().haveOppositeSigns(1, -1));      // true
        System.out.println(new Solution().haveOppositeSigns(-1, 1));      // true
    }
}
```

</details>


***

# Swap Numbers Without a Temporary

## The Problem

Given two integers, swap their values *in place* without using a third (temporary) variable.

```
Input:  num1 = 10, num2 = 1   →  num1 = 1, num2 = 10
Input:  num1 = 9, num2 = 1    →  num1 = 1, num2 = 9
```

<details>
<summary><h2>The Recurrence — Three XORs</h2></summary>


```
num1 = num1 ^ num2          (now num1 holds num1 ^ num2)
num2 = num1 ^ num2          (= num1 ^ num2 ^ num2 = num1, the original)
num1 = num1 ^ num2          (= (num1 ^ num2) ^ original_num1 = original_num2)
```

Each step relies on the self-inverse property: applying the same XOR twice cancels back to the input.

> *Pause. What happens if <code>num1</code> and <code>num2</code> are at the same memory location? Predict.*

Disaster. Step 1 sets the location to `x ^ x = 0`. Step 2 sets it to `0 ^ 0 = 0`. Step 3 same. You get `0, 0` instead of the original values. So XOR-swap is *only* safe when the two operands are guaranteed distinct memory locations. Add a guard `if num1 != num2` (or by-pointer-equality) when in doubt — the original C++ code does this.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def swap_numbers(self, num1: int, num2: int) -> None:

        # Check if the numbers are already equal
        if num1 != num2:

            # Perform XOR swap_numbers algorithm
            # Step 1: Perform XOR operation to store the XOR of num1 and
            # num2 in num1
            num1 = num1 ^ num2

            # Step 2: Perform XOR operation to store the XOR of updated
            # num1 and original num2 in num2
            num2 = num1 ^ num2

            # Step 3: Perform XOR operation to store the XOR of updated
            # num1 and updated num2 in num1
            num1 = num1 ^ num2

        # Print the swap_numbersped values
        print(str(num1) + ", " + str(num2))


# Examples from the problem statement
Solution().swap_numbers(10, 1)     # 1, 10
Solution().swap_numbers(2, 3)      # 3, 2
Solution().swap_numbers(9, 1)      # 1, 9

# Edge cases
Solution().swap_numbers(0, 0)      # 0, 0
Solution().swap_numbers(5, 5)      # 5, 5
Solution().swap_numbers(-1, 1)     # 1, -1
Solution().swap_numbers(0, 7)      # 7, 0
```

```java run
public class Main {
    static class Solution {
        public void swapNumbers(int num1, int num2) {

            // Check if the numbers are already equal
            if (num1 != num2) {

                // Perform XOR swapNumbers algorithm
                // Step 1: Perform XOR operation to store the XOR of num1 and
                // num2 in num1
                num1 = num1 ^ num2;

                // Step 2: Perform XOR operation to store the XOR of updated
                // num1 and original num2 in num2
                num2 = num1 ^ num2;

                // Step 3: Perform XOR operation to store the XOR of updated
                // num1 and updated num2 in num1
                num1 = num1 ^ num2;
            }

            // Print the swapNumbersped values
            System.out.println(num1 + ", " + num2);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        new Solution().swapNumbers(10, 1);     // 1, 10
        new Solution().swapNumbers(2, 3);      // 3, 2
        new Solution().swapNumbers(9, 1);      // 1, 9

        // Edge cases
        new Solution().swapNumbers(0, 0);      // 0, 0
        new Solution().swapNumbers(5, 5);      // 5, 5
        new Solution().swapNumbers(-1, 1);     // 1, -1
        new Solution().swapNumbers(0, 7);      // 7, 0
    }
}
```

</details>


***

# Toggle Count

## The Problem

Given two integers `num1` and `num2`, return the number of bits that need to be flipped to convert one into the other.

```
Input:  num1 = 10, num2 = 1   →  3       Binary 1010 vs 0001 — 3 differing positions
Input:  num1 = 2, num2 = 3    →  1       Binary 10 vs 11 — only LSB differs
```

<details>
<summary><h2>The Recurrence — XOR Then Popcount</h2></summary>


`num1 ^ num2` has a 1 in every position where `num1` and `num2` *differ*. Counting set bits in the XOR gives the number of differing positions — exactly the toggle count.

The set-bit count uses **Brian Kernighan's algorithm**: repeatedly clear the lowest set bit (`n & (n - 1)`) until zero, counting iterations. Faster than scanning all 32 positions when the bit count is small.

> *Pause. Why is Brian Kernighan's algorithm faster than a 32-position scan?*

It runs in **O(set-bit count)** rather than O(bit-width). For sparse integers (few bits set), this is much faster — `n = 1` runs one iteration vs. 32. For dense integers it's about the same. CPUs also have a `popcount` instruction that's faster still; we use the manual version here for clarity.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
class Solution:
    def toggle_count(self, num1: int, num2: int) -> int:

        # take XOR of num1 and num2 and store in num
        num: int = num1 ^ num2

        # Using Brian Kernighan's algorithm to count set bits

        # count stores the total bits set in num
        count: int = 0
        while num:

            # clear the least significant bit set
            num = num & (num - 1)
            count += 1

        return count


# Examples from the problem statement
print(Solution().toggle_count(10, 1))     # 3
print(Solution().toggle_count(2, 3))      # 1
print(Solution().toggle_count(9, 1))      # 1

# Edge cases
print(Solution().toggle_count(0, 0))      # 0
print(Solution().toggle_count(5, 5))      # 0
print(Solution().toggle_count(0, 7))      # 3
print(Solution().toggle_count(15, 0))     # 4
print(Solution().toggle_count(7, 1))      # 2
```

```java run
public class Main {
    static class Solution {
        public int toggleCount(int num1, int num2) {

            // take XOR of num1 and num2 and store in num
            int num = num1 ^ num2;

            // Using Brian Kernighan's algorithm to count set bits

            // count stores the total bits set in num
            int count = 0;
            while (num != 0) {

                // clear the least significant bit set
                num = num & (num - 1);
                count++;
            }

            return count;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().toggleCount(10, 1));     // 3
        System.out.println(new Solution().toggleCount(2, 3));      // 1
        System.out.println(new Solution().toggleCount(9, 1));      // 1

        // Edge cases
        System.out.println(new Solution().toggleCount(0, 0));      // 0
        System.out.println(new Solution().toggleCount(5, 5));      // 0
        System.out.println(new Solution().toggleCount(0, 7));      // 3
        System.out.println(new Solution().toggleCount(15, 0));     // 4
        System.out.println(new Solution().toggleCount(7, 1));      // 2
    }
}
```

</details>


***

# Odd-Occurring Element

## The Problem

Given an array where every element appears an even number of times *except one* that appears an odd number of times, find the odd-occurring element. Required: O(n) time, O(1) space.

```
Input:  [2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1]   →  2     (the 2 appears 3 times — odd)
Input:  [1, 2, 1, 1, 2, 1, 1]                →  1
Input:  [6, 7, 6, 7, 6, 7, 6]                →  7
```

<details>
<summary><h2>The Recurrence — XOR All</h2></summary>


XOR every element. Pairs cancel; the odd-out survives. One linear pass; one accumulator variable.

```
result = arr[0] ^ arr[1] ^ ... ^ arr[n-1]
```

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def odd_occurring_element(self, arr: List[int]) -> int:
        result: int = 0
        for val in arr:

            # Perform bitwise XOR operation with each element
            result ^= val

        # Return the result (element with odd occurrences)
        return result


# Examples from the problem statement
print(Solution().odd_occurring_element([2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1]))    # 2
print(Solution().odd_occurring_element([1, 2, 1, 1, 2, 1, 1]))                 # 1
print(Solution().odd_occurring_element([6, 7, 6, 7, 6, 7, 6]))                 # 7

# Edge cases
print(Solution().odd_occurring_element([5]))                                    # 5
print(Solution().odd_occurring_element([3, 3, 3]))                              # 3
print(Solution().odd_occurring_element([1, 1, 2, 2, 3]))                        # 3
print(Solution().odd_occurring_element([0, 0, 0]))                              # 0
```

```java run
public class Main {
    static class Solution {
        public int oddOccurringElement(int[] arr) {
            int result = 0;
            for (int val : arr) {

                // Perform bitwise XOR operation with each element
                result = result ^ val;
            }

            // Return the result (element with odd occurrences)
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().oddOccurringElement(new int[]{2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1}));    // 2
        System.out.println(new Solution().oddOccurringElement(new int[]{1, 2, 1, 1, 2, 1, 1}));                 // 1
        System.out.println(new Solution().oddOccurringElement(new int[]{6, 7, 6, 7, 6, 7, 6}));                 // 7

        // Edge cases
        System.out.println(new Solution().oddOccurringElement(new int[]{5}));                                    // 5
        System.out.println(new Solution().oddOccurringElement(new int[]{3, 3, 3}));                              // 3
        System.out.println(new Solution().oddOccurringElement(new int[]{1, 1, 2, 2, 3}));                        // 3
        System.out.println(new Solution().oddOccurringElement(new int[]{0, 0, 0}));                              // 0
    }
}
```

</details>


***

# Odd-Occurring Element II

## The Problem

Same setup, but **two** elements occur an odd number of times. Return both, in any order.

```
Input:  [2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1, 5]   →  [2, 5]
Input:  [1, 2, 1, 1, 2, 3, 1, 3, 1, 3]          →  [1, 3]
Input:  [1, 2]                                  →  [1, 2]
```

<details>
<summary><h2>The Recurrence — Partition by a Differing Bit</h2></summary>


XOR everything: result = `a ^ b`, where `a, b` are the two odd-occurring elements.

But `a ^ b` is a single number; we need to *separate* them. Find any bit where `a` and `b` differ — that bit must be set in `a ^ b`. The lowest such bit is `(a ^ b) & -(a ^ b)`.

Now partition the array by that bit: elements with the bit set XOR to `a`; elements without XOR to `b`. (Inside each partition, all even-count elements still cancel pairwise — they're either entirely in one bucket or the other.)

```d2
direction: right
flow: "Partition the array by the lowest bit where a and b differ" {
  grid-rows: 1
  grid-columns: 3
  grid-gap: 20
  s1: |md
    **Step 1**
    XOR all → `a ^ b`
  |
  s2: |md
    **Step 2**
    Isolate lowest set bit of `a ^ b`
  |
  s3: |md
    **Step 3**
    Partition; XOR each bucket → `a` and `b`
  |
}
```

<p align="center"><strong>Two passes total. The lowest differing bit cleanly splits <code>a</code> from <code>b</code>; even-count elements stay paired inside their bucket.</strong></p>

> *Pause. Why is the lowest differing bit guaranteed to exist?*

Because `a != b` (otherwise they'd be the same element). At least one bit must differ; the *lowest* such bit is just the lowest set bit of `a ^ b`. If `a == b` had been allowed, `a ^ b = 0` and isolation would fail — but the problem rules out that case.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def odd_occurring_element_ii(self, arr: List[int]) -> List[int]:
        result: int = 0

        # Finding the XOR of all elements in the array
        for val in arr:
            result = result ^ val

        # Finding the position of the rightmost set bit in the result
        rightMostSetBitPos: int = result & -result

        num1: int = 0
        num2: int = 0

        # Splitting the array into two subarrays based on the rightmost
        # set bit
        for num in arr:

            # If the rightmost set bit is set in the number
            if num & rightMostSetBitPos:

                # XOR the number with num1 to find the first odd
                # occurring element
                num1 = num1 ^ num
            else:

                # XOR the number with num2 to find the second odd
                # occurring element
                num2 = num2 ^ num

        # Return the two odd occurring elements as a list
        return [num1, num2]


# Examples from the problem statement
print(sorted(Solution().odd_occurring_element_ii([2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1, 5])))    # [2, 5]
print(sorted(Solution().odd_occurring_element_ii([1, 2, 1, 1, 2, 3, 1, 3, 1, 3])))           # [1, 3]
print(sorted(Solution().odd_occurring_element_ii([1, 2])))                                    # [1, 2]

# Edge cases
print(sorted(Solution().odd_occurring_element_ii([3, 5])))                                    # [3, 5]
print(sorted(Solution().odd_occurring_element_ii([7, 7, 7, 4])))                              # [4, 7]
print(sorted(Solution().odd_occurring_element_ii([0, 1, 0, 0, 1, 1, 2, 2, 2, 3])))           # [1, 3]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<Integer> oddOccurringElementII(int[] arr) {
            int result = 0;

            // Finding the XOR of all elements in the array
            for (int val : arr) {
                result = result ^ val;
            }

            // Finding the position of the rightmost set bit in the result
            int rightMostSetBitPos = Integer.numberOfTrailingZeros(
                result & -result
            );

            int num1 = 0, num2 = 0;

            // Splitting the array into two subarrays based on the rightmost
            // set bit
            for (int num : arr) {

                // If the rightmost set bit is set in the number
                if ((num & (1 << rightMostSetBitPos)) != 0) {

                    // XOR the number with num1 to find the first odd
                    // occurring element
                    num1 = num1 ^ num;
                } else {

                    // XOR the number with num2 to find the second odd
                    // occurring element
                    num2 = num2 ^ num;
                }
            }

            List<Integer> resultArr = new ArrayList<>();

            // Adding the two odd occurring elements to the result list
            resultArr.add(num1);
            resultArr.add(num2);

            // Return the two odd occurring elements
            return resultArr;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        List<Integer> r1 = new Solution().oddOccurringElementII(new int[]{2, 2, 2, 1, 3, 1, 4, 3, 1, 4, 1, 5});
        Collections.sort(r1); System.out.println(r1);    // [2, 5]

        List<Integer> r2 = new Solution().oddOccurringElementII(new int[]{1, 2, 1, 1, 2, 3, 1, 3, 1, 3});
        Collections.sort(r2); System.out.println(r2);    // [1, 3]

        List<Integer> r3 = new Solution().oddOccurringElementII(new int[]{1, 2});
        Collections.sort(r3); System.out.println(r3);    // [1, 2]

        // Edge cases
        List<Integer> r4 = new Solution().oddOccurringElementII(new int[]{3, 5});
        Collections.sort(r4); System.out.println(r4);    // [3, 5]

        List<Integer> r5 = new Solution().oddOccurringElementII(new int[]{7, 7, 7, 4});
        Collections.sort(r5); System.out.println(r5);    // [4, 7]

        List<Integer> r6 = new Solution().oddOccurringElementII(new int[]{0, 1, 0, 0, 1, 1, 2, 2, 2, 3});
        Collections.sort(r6); System.out.println(r6);    // [1, 3]
    }
}
```

</details>


***

# Duplicate Element

## The Problem

You're given an array of size `n` containing elements from `1` to `n - 1`. Exactly one element appears twice; everyone else appears once. Find the duplicate.

```
Input:  [1, 4, 3, 2, 2]    →  2
Input:  [4, 1, 5, 3, 2, 5] →  5
Input:  [1, 1]             →  1
```

<details>
<summary><h2>The Recurrence — XOR Array Against XOR of `1..n-1`</h2></summary>


XOR all array elements together; XOR `1, 2, …, n-1` together; XOR those two results.

- The unique elements appear once in the array and once in `1..n-1`, so they cancel.
- The duplicate appears *twice* in the array and once in `1..n-1` — three appearances total → it survives (odd count).

```
result = (arr[0] ^ arr[1] ^ ... ^ arr[n-1]) ^ (1 ^ 2 ^ ... ^ (n-1))
       = duplicate
```

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def duplicate_element(self, arr: List[int]) -> int:
        n: int = len(arr)
        num: int = 0

        # take xor of all array elements
        for i in range(n):
            num ^= arr[i]

        # take xor of numbers from 1 to `n-1`
        for i in range(1, n):
            num ^= i

        # same elements will cancel each other as a ^ a = 0,
        # 0 ^ 0 = 0 and a ^ 0 = a

        # num will contain the missing number
        return num


# Examples from the problem statement
print(Solution().duplicate_element([1, 4, 3, 2, 2]))     # 2
print(Solution().duplicate_element([4, 1, 5, 3, 2, 5]))  # 5
print(Solution().duplicate_element([1, 1]))               # 1

# Edge cases
print(Solution().duplicate_element([2, 1, 2]))            # 2
print(Solution().duplicate_element([3, 1, 2, 3]))         # 3
print(Solution().duplicate_element([1, 2, 3, 3]))         # 3
print(Solution().duplicate_element([2, 2]))               # 2
```

```java run
public class Main {
    static class Solution {
        public int duplicateElement(int[] arr) {
            int n = arr.length;
            int num = 0;

            // take xor of all array elements
            for (int i = 0; i < n; i++) {
                num ^= arr[i];
            }

            // take xor of numbers from 1 to `n-1`
            for (int i = 1; i <= n - 1; i++) {
                num ^= i;
            }

            // same elements will cancel each other as a ^ a = 0,
            // 0 ^ 0 = 0 and a ^ 0 = a

            // num will contain the missing number
            return num;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().duplicateElement(new int[]{1, 4, 3, 2, 2}));     // 2
        System.out.println(new Solution().duplicateElement(new int[]{4, 1, 5, 3, 2, 5}));  // 5
        System.out.println(new Solution().duplicateElement(new int[]{1, 1}));               // 1

        // Edge cases
        System.out.println(new Solution().duplicateElement(new int[]{2, 1, 2}));            // 2
        System.out.println(new Solution().duplicateElement(new int[]{3, 1, 2, 3}));         // 3
        System.out.println(new Solution().duplicateElement(new int[]{1, 2, 3, 3}));         // 3
        System.out.println(new Solution().duplicateElement(new int[]{2, 2}));               // 2
    }
}
```

</details>


***

# Missing and Duplicated Elements

## The Problem

An array of size `n` should contain elements from `1` to `n`, but one element is missing and another is duplicated (appears twice). Return both — in any order.

```
Input:  [1, 5, 2, 4, 2]    →  [2, 3]   (2 is duplicated, 3 is missing)
Input:  [2, 4, 1, 3, 6, 6] →  [5, 6]   (6 is duplicated, 5 is missing)
Input:  [1, 1]             →  [1, 2]
```

<details>
<summary><h2>The Recurrence — Same Trick as "Two Odd Elements"</h2></summary>


XOR the array together with `1..n`. Most elements cancel; only the missing and the duplicated survive — with the duplicated appearing 2× in the array and 1× in `1..n` (3 total → survives), and the missing appearing 0× in the array and 1× in `1..n` (1 total → survives). Result is `missing ^ duplicated`.

Now we have two unknowns and one equation — not enough. Apply the **odd-occurring-element II partition trick**: isolate any differing bit, split into two buckets, XOR each bucket against the array *and* against `1..n`. Each bucket isolates exactly one of the two values.

Final step: figure out which value is the missing one (it's *not* in the array) and which is the duplicate. Linear scan to disambiguate.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List
import math

class Solution:
    def missing_and_duplicated_elements(
        self, arr: List[int]
    ) -> List[int]:
        n: int = len(arr)

        result: int = n

        # XOR all the elements of the array with their indices and n
        # The result will be the XOR of the missing and duplicate numbers
        for i in range(n):
            result = result ^ arr[i] ^ i

        num1: int = 0
        num2: int = 0

        # Find the rightmost set bit position in the result
        rightmost_set_bit_pos: int = int(math.log2(result & -result))

        # XOR all the elements of the array based on the rightmost set
        # bit position
        for num in arr:

            # The numbers with the rightmost set bit as 1 will XOR with
            # num1
            if num & (1 << rightmost_set_bit_pos):
                num1 = num1 ^ num

            # The numbers with the rightmost set bit as 0 will XOR with
            # num2
            else:
                num2 = num2 ^ num

        # XOR all the numbers from 1 to n based on the rightmost set bit
        # position
        for i in range(1, n + 1):

            # The numbers with the rightmost set bit as 1 will XOR with
            # num1
            if i & (1 << rightmost_set_bit_pos):
                num1 = num1 ^ i

            # The numbers with the rightmost set bit as 0 will XOR with
            # num2
            else:
                num2 = num2 ^ i

        # Check if num1 is missing in the array
        # If it is missing, return [num2, num1], else return [num1, num2]
        if num1 not in arr:
            return [num2, num1]

        return [num1, num2]


# Examples from the problem statement
print(Solution().missing_and_duplicated_elements([1, 5, 2, 4, 2]))       # [2, 3]
print(Solution().missing_and_duplicated_elements([2, 4, 1, 3, 6, 6]))    # [5, 6]
print(Solution().missing_and_duplicated_elements([1, 1]))                 # [1, 2]

# Edge cases
print(Solution().missing_and_duplicated_elements([2, 2]))                 # [1, 2]
print(Solution().missing_and_duplicated_elements([1, 3, 3]))              # [2, 3]
print(Solution().missing_and_duplicated_elements([3, 1, 2, 4, 4]))        # [5, 4]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<Integer> missingAndDuplicatedElements(int[] arr) {
            int n = arr.length;

            int result = n;

            // XOR all the elements of the array with their indices and n
            // The result will be the XOR of the missing and duplicate
            // numbers
            for (int i = 0; i < n; i++) {
                result = result ^ arr[i] ^ i;
            }

            int num1 = 0, num2 = 0;

            // Find the rightmost set bit position in the result
            int rightMostSetBitPos = Integer.numberOfTrailingZeros(result);

            // XOR all the elements of the array based on the rightmost set
            // bit position
            for (int num : arr) {

                // The numbers with the rightmost set bit as 1 will XOR with
                // num1
                if ((num & (1 << rightMostSetBitPos)) != 0) {
                    num1 = num1 ^ num;
                }

                // The numbers with the rightmost set bit as 0 will XOR with
                // num2
                else {
                    num2 = num2 ^ num;
                }
            }

            // XOR all the numbers from 1 to n based on the rightmost set bit
            // position
            for (int i = 1; i <= n; i++) {

                // The numbers with the rightmost set bit as 1 will XOR with
                // num1
                if ((i & (1 << rightMostSetBitPos)) != 0) {
                    num1 = num1 ^ i;
                }

                // The numbers with the rightmost set bit as 0 will XOR with
                // num2
                else {
                    num2 = num2 ^ i;
                }
            }

            // Linear search for the missing element
            boolean isNum1Found = false;
            for (int num : arr) {
                if (num == num1) {
                    isNum1Found = true;
                    break;
                }
            }

            if (!isNum1Found) {
                return List.of(num2, num1);
            }

            return List.of(num1, num2);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{1, 5, 2, 4, 2}));       // [2, 3]
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{2, 4, 1, 3, 6, 6}));    // [5, 6]
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{1, 1}));                 // [1, 2]

        // Edge cases
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{2, 2}));                 // [1, 2]
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{1, 3, 3}));              // [2, 3]
        System.out.println(new Solution().missingAndDuplicatedElements(new int[]{3, 1, 2, 4, 4}));        // [5, 4]
    }
}
```

</details>


***

# Final Takeaway

Seven problems, one operator. The XOR pattern's recipe:

| Problem | What XOR cancels | What survives |
|---|---|---|
| Opposite signs | (none — direct sign-bit comparison) | sign-difference flag |
| Swap | self-inversion | the swapped values |
| Toggle count | matching bits | a value whose popcount = differences |
| Odd-occurring | even-count pairs | the odd-count value |
| Two odd-occurring | even-count pairs | partition into two singletons |
| Duplicate | unique values | the doubled-and-once-more = three-times value |
| Missing + duplicated | matched values | partition into missing + duplicated |

**You didn't just learn seven tricks. You internalised the most algebraically elegant operator in computing — XOR — and saw how its three properties (commutative, associative, self-inverse) collapse seemingly hard problems into linear scans. From here on, "XOR everything together" is a tool in your toolkit, and `xor & -xor` to isolate a differing bit is the partition primitive that handles the two-unknown case.**

> *Transfer challenge for the next lesson:* You have a 32-bit integer and want to swap *every adjacent pair* of bits — bit 1 with bit 2, bit 3 with bit 4, etc. — without an explicit loop. Predict how the magic constants `0x55555555` and `0xAAAAAAAA` come into play.

<details>
<summary><strong>Answer</strong></summary>

`0x55555555` masks the odd-positioned bits (1, 3, 5, …); `0xAAAAAAAA` masks the even-positioned bits. Take `(num & 0x55555555) << 1` to slide odd-positioned bits up by one, and `(num & 0xAAAAAAAA) >> 1` to slide even-positioned bits down by one. OR them together — adjacent pairs are now swapped. The next lesson formalises this as the **bitmasking pattern** and uses similar ideas to enumerate every subset of an array.

</details>
