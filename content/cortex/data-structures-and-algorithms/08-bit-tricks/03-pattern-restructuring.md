# 3. Bit Restructuring

So far we've inspected, set, and located individual bits. Now we change the *layout* — restructure an integer's bits without changing how many are on. Two canonical operations: **reverse bits** end-to-end (bit 1 swaps with bit 32, bit 2 with bit 31, etc.) and **circular shift** (rotate the bits left or right by `k`, where bits falling off one end wrap around to the other). These show up in cryptography (every block cipher round shuffles bits), checksum computation (CRCs use reversed-bit polynomial representations), networking (byte-order conversions in protocol headers), and graphics (texture address swizzling). Both run in O(1) — no scan over `n` because the work is bounded by the bit-width.

By the end of this lesson you'll know **bit reversal** by LSB-extraction-and-rebuild, and **circular shift** as `(n << k) | (n >> (32 - k))` — the OR-of-two-shifts pattern that wraps bits without losing them.

## Table of contents

1. [Reverse Bits](#reverse-bits)
2. [Circular Shift Bits](#circular-shift-bits)
3. [Final Takeaway](#final-takeaway)

***

# Reverse Bits

## The Problem

Given a 32-bit unsigned integer `num`, return the integer formed by reversing its 32 bits — bit 1 becomes bit 32, bit 2 becomes bit 31, and so on.

```
Input:  num = 28
Output: 939524096
        Binary  00000000 00000000 00000000 00011100
        Reversed 00111000 00000000 00000000 00000000

Input:  num = 1
Output: 2147483648            (highest bit becomes set)

Input:  num = 3415
Output: 3937402880
```

<details>
<summary><h2>The Recurrence — LSB Extract, Append</h2></summary>


Build the reversed integer one bit at a time. For 32 iterations:
1. Shift `result` left by 1 — make room for one more bit at the bottom.
2. Take the LSB of `num` (`num & 1`) and OR it into `result`'s new bottom slot.
3. Shift `num` right by 1 — discard the bit we just consumed.

After 32 rounds, `num`'s original bit 1 has migrated all the way up to bit 32 in `result`, bit 2 to bit 31, etc. — exactly the reverse layout.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  S0["result = 0"]
  S0 --> S1["1. result <<= 1<br/>(make slot for new bit)"]
  S1 --> S2["2. result |= num & 1<br/>(append LSB of num)"]
  S2 --> S3["3. num >>= 1<br/>(discard consumed bit)"]
  S3 -->|"32 iterations"| S0
```

<p align="center"><strong>One iteration extracts <code>num</code>'s LSB and appends it as <code>result</code>'s new LSB. After 32 iterations, the original bit order is reversed end-to-end.</strong></p>

> *Pause. Why 32 iterations exactly? Predict the consequence of stopping early.*

Because the integer has 32 bits — every position must be processed for the reversal to land bits at the correct *symmetric* positions. Stopping at, say, 16 iterations would only reverse the lower half and leave the upper half zero. The loop count is bound to the bit-width, not to `num`'s actual content.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
class Solution:
    def reverse_bits(self, num: int) -> int:

        # Initialize the variable to store the reversed bits
        result: int = 0

        for _ in range(32):

            # Left shift the result by 1 to make room for the next bit
            result = result << 1

            # Get the least significant bit of num using bitwise AND with
            # 1
            bit: int = num & 1

            # Add the bit to the result
            result = result + bit

            # Right shift num by 1 to discard the least significant bit
            num = num >> 1

        # Return the reversed bits
        return result


# Examples from the problem statement
print(Solution().reverse_bits(28))      # 939524096
print(Solution().reverse_bits(3415))    # 3937402880
print(Solution().reverse_bits(1))       # 2147483648

# Edge cases
print(Solution().reverse_bits(0))       # 0
print(Solution().reverse_bits(2))       # 1073741824
print(Solution().reverse_bits(4))       # 536870912
print(Solution().reverse_bits(2147483648))  # 1
```

```java run
public class Main {
    static class Solution {
        public int reverseBits(int num) {

            // Initialize the variable to store the reversed bits
            int result = 0;

            for (int i = 0; i < 32; i++) {

                // Left shift the result by 1 to make room for the next bit
                result = result << 1;

                // Get the least significant bit of num using bitwise AND
                // with 1
                int bit = num & 1;

                // Add the bit to the result
                result = result + bit;

                // Right shift num by 1 to discard the least significant bit
                num = num >> 1;
            }

            // Return the reversed bits
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        // Note: Java int is signed; we print as unsigned long for clarity
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(28)));     // 939524096
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(3415)));   // 3937402880
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(1)));      // 2147483648

        // Edge cases
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(0)));              // 0
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(2)));              // 1073741824
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(4)));              // 536870912
        System.out.println(Integer.toUnsignedLong(new Solution().reverseBits(-2147483648)));     // 1
    }
}
```


<details>
<summary><strong>Trace — num = 28 (0b11100)</strong></summary>

```
Initial: result = 0, num = 0b11100

Iter  num         num & 1   result <<= 1  result |= bit  num >>= 1
0     ...11100    0         0             0              ...01110
1     ...01110    0         0             0              ...00111
2     ...00111    1         0             1              ...00011
3     ...00011    1         10            11             ...00001
4     ...00001    1         110           111            ...00000
5–31: num is 0, so we keep shifting result left, appending 0s.

After iteration 31:
  result has 28's lowest 5 bits (11100, processed in reverse order)
  pushed to the top of a 32-bit space.
  result = 0b00111000 00000000 00000000 00000000 = 939524096 ✓
```

</details>

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(32) = O(1)` — fixed bit-width loop |
| Space | `O(1)` |

</details>
<details>
<summary><h2>Faster Alternative — Divide and Conquer</h2></summary>


For hot loops, the divide-and-conquer approach uses 5 swap stages with magic masks:
1. Swap adjacent bits with masks `0x55555555` and `0xAAAAAAAA`.
2. Swap adjacent pairs with `0x33333333` and `0xCCCCCCCC`.
3. Swap adjacent nibbles with `0x0F0F0F0F` and `0xF0F0F0F0`.
4. Swap adjacent bytes (or use byte-swap intrinsic).
5. Swap halves.

5 ops total, no loop, ~6× faster on most CPUs. Beyond this lesson but worth knowing.

</details>

***

# Circular Shift Bits

## The Problem

Given a 32-bit unsigned integer `num`, an integer `k`, and a flag `rotateLeft`, rotate `num`'s bits left by `k` (if `rotateLeft = true`) or right by `k` (otherwise). Bits falling off one end wrap around to the other end — they don't disappear.

```
Input:  num = 28, k = 2, rotateLeft = true
Output: 112
        Binary 00000000 00000000 00000000 00011100
        After  00000000 00000000 00000000 01110000

Input:  num = 1, k = 1, rotateLeft = false
Output: 2147483648            Bit 1 wraps around to bit 32
```

<details>
<summary><h2>The Recurrence — Two Shifts ORed Together</h2></summary>


Standard left/right shift loses bits that fall off the edge. To wrap them, take *both* shifts and combine:

- **Left rotate by k**: `(num << k) | (num >> (32 - k))`
  - `num << k` shifts left, losing the top `k` bits.
  - `num >> (32 - k)` shifts the top `k` bits down to the bottom.
  - OR combines: top bits land at the bottom, everything shifts left by `k`.

- **Right rotate by k**: `(num >> k) | (num << (32 - k))` — symmetric.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  N["num"]
  N -->|"<< k"| LSHIFT["lower 32-k bits<br/>shifted up"]
  N -->|">> (32 - k)"| RSHIFT["top k bits<br/>shifted to bottom"]
  LSHIFT --> OR["OR<br/>= rotated value"]
  RSHIFT --> OR
```

<p align="center"><strong>Left rotation: combine the leftshift (which drops top bits) with a rightshift of <em>complementary</em> distance (which extracts those same top bits and lands them at the bottom). OR the two together for a lossless rotate.</strong></p>

> *Pause. Why do we need the <code>0xFFFFFFFF</code> mask (Python) or the unsigned right shift <code>&gt;&gt;&gt;</code> (Java)? Predict what goes wrong without them.*

Python's integers are arbitrary-precision and *signed*, so `num >> k` propagates sign bits indefinitely and `num << k` can grow beyond 32 bits — both corrupt the OR. Masking with `0xFFFFFFFF` (`mask_int`) clamps the result back to 32 bits, recovering the rotation semantics. Java has a fixed-width `int`, but its `>>` is *arithmetic* (sign-extending); the unsigned form `>>>` zero-fills the high bits, which is what rotation needs.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
class Solution:

    # Assuming a 32-bit integer
    size_int: int = 32

    # Mask to ensure the result is a 32-bit integer
    mask_int: int = 0xFFFFFFFF

    def circular_shift_bits(
        self, num: int, k: int, rotate_left: bool
    ) -> int:
        if rotate_left:
            return (
                num << k | num >> (self.size_int - k)
            ) & self.mask_int

        # Perform circular right shift, and apply the mask after OR
        # operation
        return (num >> k | num << (self.size_int - k)) & self.mask_int


# Examples from the problem statement
print(Solution().circular_shift_bits(28, 2, True))             # 112
print(Solution().circular_shift_bits(1234567890, 8, True))     # 2516767305
print(Solution().circular_shift_bits(1, 1, False))             # 2147483648

# Edge cases
print(Solution().circular_shift_bits(0, 4, True))              # 0
print(Solution().circular_shift_bits(1, 1, True))              # 2
print(Solution().circular_shift_bits(2, 1, False))             # 1
print(Solution().circular_shift_bits(28, 2, False))            # 7
```

```java run
public class Main {
    static class Solution {

        // Number of bits in an integer
        private int sizeInt = Integer.SIZE;

        public int circularShiftBits(int num, int k, boolean rotateLeft) {
            if (rotateLeft) {

                // Perform circular left shift
                return (num << k) | (num >>> (sizeInt - k));
            }

            // Perform circular right shift
            return (num >>> k) | (num << (sizeInt - k));
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(28, 2, true)));             // 112
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(1234567890, 8, true)));     // 2516767305
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(1, 1, false)));             // 2147483648

        // Edge cases
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(0, 4, true)));              // 0
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(1, 1, true)));              // 2
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(2, 1, false)));             // 1
        System.out.println(Integer.toUnsignedLong(new Solution().circularShiftBits(28, 2, false)));            // 7
    }
}
```

### Complexity

| Aspect | Cost |
|---|---|
| Time | `O(1)` — two shifts and an OR |
| Space | `O(1)` |

</details>

***

# Final Takeaway

Bit restructuring is the "rearrange without losing" branch of bit manipulation. Two shapes, both O(1):

| Operation | Recipe |
|---|---|
| Reverse bits | LSB-extract loop: shift result left, append num's LSB, shift num right |
| Circular shift | OR of two shifts: `(num << k) \| (num >> (size - k))` for left rotate |

**You didn't just learn two restructuring tricks. You internalised the OR-of-complementary-shifts pattern — used in cryptographic round functions, byte-order swaps, and any algorithm that needs lossless bit movement. The reduction `k %= bitwidth` is the small but critical step that prevents undefined behaviour on overshift.**

> *Transfer challenge for the next lesson:* You have an array where every element appears an even number of times *except one* that appears an odd number of times. Find the odd one out in O(n) time and O(1) space — without sorting, without a hash map. Predict the trick.

<details>
<summary><strong>Answer</strong></summary>

XOR all elements together. Each pair of equal values cancels (`a ^ a = 0`), leaving only the odd-occurring element behind. The next lesson exploits this **"XOR cancels duplicates"** property across six progressively richer problems — from finding one odd-occurring element to recovering both a missing *and* duplicated number from a single linear pass.

</details>
