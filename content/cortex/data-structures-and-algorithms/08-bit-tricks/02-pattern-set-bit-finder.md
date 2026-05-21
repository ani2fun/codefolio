# 2. Set-Bit Finder

The previous lesson taught how to *manipulate* a specific bit. This one inverts the question: given a number, can we *locate* its set bits — quickly, without scanning all 32 positions? Two flavours of the question turn out to have elegant single-line answers. **Only-set-bit:** assuming exactly one bit is on, where is it? Equivalent to "is this a power of 2, and if so, which one?" **Rightmost-set-bit:** for an arbitrary number, where is the lowest-position bit that's on? Both rest on the legendary identity `n & (n - 1)`, which clears the rightmost set bit — and once you've internalised that one trick, half of bit-manipulation interview questions feel scripted.

By the end of this lesson you'll know **`n & (n - 1)` clears the rightmost set bit** and `n & -n` *isolates* it — the two complementary operations that let you find, count, and clear set bits in O(set-bit count) instead of O(bit-width).

## Table of contents

1. [The `n & (n - 1)` Identity](#the-n--n--1-identity)
2. [Only Set Bit](#only-set-bit)
3. [Rightmost Set Bit](#rightmost-set-bit)
4. [Final Takeaway](#final-takeaway)

***

# The `n & (n - 1)` Identity

Subtracting 1 from a binary number flips its **rightmost set bit** to 0 and sets every bit below it to 1.

```d2
direction: right
flow: "n = 12 → n - 1 = 11" {
  grid-rows: 2
  grid-columns: 2
  grid-gap: 20
  n: |md
    n = 12
    `0000 1100`
    rightmost set bit is bit 3
  |
  n_minus_1: |md
    n − 1 = 11
    `0000 1011`
    bit 3 cleared; bits 1-2 flipped to 1
  |
}
```

<p align="center"><strong>Subtracting 1 from <code>n</code> ripples through the trailing zeros, turning them all to 1, and clears the lowest set bit. AND-ing the two together cancels both the original lowest bit and the freshly-flipped trailing 1s.</strong></p>

So `n & (n - 1)` clears the rightmost set bit but leaves every higher set bit untouched. That single fact powers two complementary tricks:

- **Diagnostic** — `(n & (n - 1)) == 0` exactly when `n` has *zero or one* set bits. For non-zero `n`, this is the one-line "is `n` a power of 2?" test.
- **Iterative bit removal** — repeated `n = n & (n - 1)` strips set bits one at a time. Counting iterations until `n == 0` gives the population count (Brian Kernighan's algorithm — used in lesson 4).

The dual is `n & -n` (using two's complement): instead of *clearing* the rightmost set bit, it **isolates** it, returning a power of 2 marking that bit's position.

```
n = 12        ⇒ n & (n - 1) = 8     (clears bit 3)
n = 12        ⇒ n & -n       = 4    (isolates bit 3)
n = 0b101000  ⇒ n & (n - 1) = 0b100000   (clears bit 4)
n = 0b101000  ⇒ n & -n       = 0b001000  (isolates bit 4)
```

> *Predict before reading on — for <code>n = 7</code> (binary <code>0111</code>), what does <code>n & (n - 1)</code> give? What about <code>n & -n</code>?*

`n & (n - 1) = 6` (binary `0110`) — clears the lowest set bit (bit 1). `n & -n = 1` (binary `0001`) — isolates the lowest set bit.

---

## Key Takeaway

`n & (n - 1)` clears the rightmost set bit. `n & -n` isolates it. Together they're the most-used pair of one-liners in bit manipulation.

***

# Only Set Bit

## The Problem

Given a 32-bit integer `num`, find the position (1-indexed) of the only set bit. If `num` has more than one set bit, return `-1`.

```
Input:  num = 16
Output: 5                    Binary 0001 0000 — only bit 5 is set

Input:  num = 2
Output: 2                    Binary 0010

Input:  num = 10
Output: -1                   Binary 1010 — two set bits
```

<details>
<summary><h2>The Recurrence</h2></summary>


Two steps:

1. **Validate**: `(num & (num - 1)) != 0` ⇒ more than one set bit ⇒ return `-1`.
2. **Find position**: take `log₂(num) + 1`. (For a power of 2, `log₂` is an integer; the +1 converts to 1-indexing.)

> *Pause. Why does step 1 reject zero correctly?*

For `num = 0`, `num - 1` wraps to `-1` (or all-1s in two's-complement), so `num & (num - 1) = 0`. The first test passes — it thinks zero has "≤ 1 set bit" (true). Then `log₂(0)` is undefined. Most implementations either special-case `num == 0` or rely on the platform's `log` returning `-inf` and the conversion landing somewhere harmless. The C++ original assumes `num > 0` per the problem; we'll guard explicitly here.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
import math

class Solution:
    def only_set_bit(self, num: int) -> int:

        # Check if num is not a power of 2 (has more than one set bit)
        if num & (num - 1):

            # Return -1 if num is not a power of 2
            return -1

        # Calculate the position of the set bit by taking the base-2
        # logarithm of num and adding 1
        return int(math.log2(num) + 1)


# Examples from the problem statement
print(Solution().only_set_bit(16))    # 5
print(Solution().only_set_bit(2))     # 2
print(Solution().only_set_bit(10))    # -1

# Edge cases
print(Solution().only_set_bit(1))     # 1
print(Solution().only_set_bit(4))     # 3
print(Solution().only_set_bit(8))     # 4
print(Solution().only_set_bit(7))     # -1
print(Solution().only_set_bit(3))     # -1
```

```java run
public class Main {
    static class Solution {
        public int onlySetBit(int num) {

            // Check if num is not a power of 2 (has more than one set bit)
            if ((num & (num - 1)) != 0) {

                // Return -1 if num is not a power of 2
                return -1;
            }

            // Calculate the position of the set bit by taking the base-2
            // logarithm of num and adding 1
            return (int) (Math.log(num) / Math.log(2)) + 1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().onlySetBit(16));    // 5
        System.out.println(new Solution().onlySetBit(2));     // 2
        System.out.println(new Solution().onlySetBit(10));    // -1

        // Edge cases
        System.out.println(new Solution().onlySetBit(1));     // 1
        System.out.println(new Solution().onlySetBit(4));     // 3
        System.out.println(new Solution().onlySetBit(8));     // 4
        System.out.println(new Solution().onlySetBit(7));     // -1
        System.out.println(new Solution().onlySetBit(3));     // -1
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(1)` — constant-time bitwise check + log |
| Space | `O(1)` |

</details>

***

# Rightmost Set Bit

## The Problem

Given a 32-bit integer `num`, find the 1-indexed position of its rightmost (lowest) set bit.

```
Input:  num = 10
Output: 2                   Binary 1010 — lowest set bit is at position 2

Input:  num = 16
Output: 5                   Binary 0001 0000 — bit 5 is the only set bit

Input:  num = 17
Output: 1                   Binary 0001 0001 — bit 1 is the rightmost set bit
```

<details>
<summary><h2>The Recurrence</h2></summary>


Three steps:

1. **Fast path** — if the LSB is set (`num & 1`), bit 1 is already the rightmost set bit; return 1 immediately.
2. **Isolate the rightmost set bit** — `num & (num - 1)` clears it, and XOR-ing that against `num` leaves just the rightmost set bit standing. (Equivalent to `num & -num`, but written in terms of the lesson's primary `n & (n - 1)` identity.)
3. **Find its position by right-shifting** — repeatedly shift the isolated bit one position to the right and count iterations until it falls off. The final count is the 1-indexed position.

```
if num & 1: return 1
num = num ^ (num & (num - 1))     # isolate rightmost set bit
index = 0
while num != 0:
    num >>= 1
    index += 1
return index
```

> *Pause. Why does <code>num ^ (num & (num - 1))</code> isolate the lowest set bit? Predict before reading on.*

`num & (num - 1)` clears the rightmost set bit and leaves every other bit untouched. XOR-ing that with the original `num` cancels every bit they share — i.e. every higher set bit — and the only bit that differs is the one that was cleared. The result has a 1 in exactly that position. (Alternative single-step form: `num & -num`, which uses two's complement to achieve the same isolation.)

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
class Solution:
    def rightmost_set_bit(self, num: int) -> int:

        # Check if the least significant bit is set (num & 1)
        # If it is set, return 1 as the rightmost set bit position
        if num & 1:
            return 1

        # Clear the rightmost set bit using XOR operation
        num = num ^ (num & (num - 1))

        # Variable to store the index of the rightmost set bit
        index: int = 0

        # Iterate until num becomes zero
        while num:

            # Right shift num by 1 to check the next bit
            num = num >> 1

            # Increment the index by 1
            index += 1

        # Return the index of the rightmost set bit
        return index


# Examples from the problem statement
print(Solution().rightmost_set_bit(10))    # 2
print(Solution().rightmost_set_bit(16))    # 5
print(Solution().rightmost_set_bit(17))    # 1

# Edge cases
print(Solution().rightmost_set_bit(1))     # 1
print(Solution().rightmost_set_bit(2))     # 2
print(Solution().rightmost_set_bit(4))     # 3
print(Solution().rightmost_set_bit(8))     # 4
print(Solution().rightmost_set_bit(12))    # 3
```

```java run
public class Main {
    static class Solution {
        public int rightmostSetBit(int num) {

            // Check if the least significant bit is set (num & 1)
            // If it is set, return 1 as the rightmost set bit position
            if ((num & 1) != 0) {
                return 1;
            }

            // Clear the rightmost set bit using XOR operation
            num = num ^ (num & (num - 1));

            // Variable to store the index of the rightmost set bit
            int index = 0;

            // Iterate until num becomes zero
            while (num != 0) {

                // Right shift num by 1 to check the next bit
                num = num >> 1;

                // Increment the index by 1
                index++;
            }

            // Return the index of the rightmost set bit
            return index;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().rightmostSetBit(10));    // 2
        System.out.println(new Solution().rightmostSetBit(16));    // 5
        System.out.println(new Solution().rightmostSetBit(17));    // 1

        // Edge cases
        System.out.println(new Solution().rightmostSetBit(1));     // 1
        System.out.println(new Solution().rightmostSetBit(2));     // 2
        System.out.println(new Solution().rightmostSetBit(4));     // 3
        System.out.println(new Solution().rightmostSetBit(8));     // 4
        System.out.println(new Solution().rightmostSetBit(12));    // 3
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(p)` where `p` is the position of the rightmost set bit — bounded by the bit-width (32 for an `int`), so `O(1)` for fixed-width integers |
| Space | `O(1)` |

</details>

***

# Final Takeaway

`n & (n - 1)` and `n & -n` are the two most-reused identities in all of bit manipulation:

| Trick | Effect | Use |
|---|---|---|
| `n & (n - 1)` | Clears rightmost set bit | Power-of-2 test, popcount loop, set-bit iteration |
| `n & -n` | Isolates rightmost set bit | Find position of lowest 1, partition by lowest bit |

**You didn't just solve two find-the-bit problems. You learned the two trickiest one-liners in bit manipulation — and the dozens of algorithms built on top of them. From here on, you'll see them used as primitives, not derived: "clear lowest bit" and "isolate lowest bit" become single steps in larger compositions.**

> *Transfer challenge for the next lesson:* Reverse the bits of a 32-bit integer end-to-end (bit 1 becomes bit 32, bit 2 becomes bit 31, …). Predict whether you can do it without an explicit loop. (Hint: yes, but the trick involves divide-and-conquer with magic mask constants.)

<details>
<summary><strong>Answer</strong></summary>

The straightforward solution is a 32-iteration loop: shift result left, OR in the LSB of `num`, shift `num` right. The next lesson uses this approach. There's also a clever divide-and-conquer version using "swap adjacent pairs, then adjacent quads, then bytes" with magic constants like `0xAAAAAAAA` and `0x55555555` — the same magic constants appear in lesson 5 for pairwise swaps. Both are O(1) but the loop is more readable; the divide-and-conquer is faster when bit-reversal is a hot loop.

</details>
