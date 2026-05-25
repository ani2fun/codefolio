---
title: "Pattern: Bit Restructuring"
summary: "Rearrange bits by reversing all 32 bits or circularly shifting left/right by K positions using bitwise primitives."
prereqs:
  - 08-bit-tricks/01-pattern-kth-bit/01-pattern
---

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
