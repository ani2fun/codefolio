---
title: "Pattern: Set-Bit Finder"
summary: "The n & (n−1) identity strips the lowest set bit; applications: isolate the only set bit and find the rightmost set bit."
prereqs:
  - 08-bit-tricks/01-pattern-kth-bit/01-pattern
---

# The `n & (n - 1)` Identity

Subtracting 1 from a binary number flips its **rightmost set bit** to 0 and sets every bit below it to 1.

> 🖼 Diagram — Subtracting 1 from n ripples through the trailing zeros, turning them all to 1, and clears the lowest set bit. AND-ing the two together cancels both the original lowest bit and the freshly-flipped trailing 1s.
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

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Pattern — missing, needs to be written -->
<!--       Guidance: umbrella H2 with the subsections below -->

<!-- TODO: Why Naive Isn't Enough — missing, needs to be written -->
<!--       Guidance: motivation for why the obvious approach fails -->

<!-- TODO: The Core Idea — missing, needs to be written -->
<!--       Guidance: one paragraph: the central trick -->

<!-- TODO: How the Pointers/Window Move — missing, needs to be written -->
<!--       Guidance: mechanics of the moving parts -->

<!-- TODO: The Generic Algorithm — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Generic Implementation — missing, needs to be written -->
<!--       Guidance: Python block + Java block of the skeleton -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table -->

<!-- TODO: Variants / Taxonomy — missing, needs to be written -->
<!--       Guidance: enumerate sub-shapes of this pattern -->

<!-- TODO: Identifying — missing, needs to be written -->
<!--       Guidance: per-variant: recognition checklist + canonical example -->

<!-- TODO: Recognition Checklist — missing, needs to be written -->
<!--       Guidance: 4-question diagnostic — the source of the Problem-section Diagnostic Questions -->

<!-- TODO: Canonical Example — missing, needs to be written -->
<!--       Guidance: fully worked example: brute force → optimised → template fit -->

<!-- TODO: Problems in This Category — missing, needs to be written -->
<!--       Guidance: table with links to the 02-problems/ files -->
