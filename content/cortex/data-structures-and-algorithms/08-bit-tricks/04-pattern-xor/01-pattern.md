---
title: "Pattern: XOR"
summary: "XOR self-cancels paired values — swap without temp, toggle bits, detect lone odd-occurring elements, and find missing/duplicate pairs."
prereqs:
  - 08-bit-tricks/01-pattern-kth-bit/01-pattern
---

# Why XOR Cancels

Three identities form the algebraic foundation for everything in this lesson:

| Identity | Why it matters |
|---|---|
| `a ^ a = 0` | Same-with-same cancels |
| `a ^ 0 = a` | Identity element |
| `(a ^ b) ^ c = a ^ (b ^ c)` and `a ^ b = b ^ a` | Order doesn't matter |

The combination means that XORing a sequence in *any* order, with arbitrary regrouping, gives the same answer. So if numbers come in pairs (each appearing twice), the pairs cancel pairwise — regardless of how they're interleaved. Whatever's left after all the cancellation is the XOR of the *unpaired* elements.

> 🖼 Diagram — For arr = [2, 2, 2, 1, 3, 1, 3], the XOR of all elements collapses pairs (the two 1s cancel, both 3s cancel, two of the three 2s cancel) — leaving just the unpaired 2. Order doesn't matter because XOR is commutative and associative.
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
