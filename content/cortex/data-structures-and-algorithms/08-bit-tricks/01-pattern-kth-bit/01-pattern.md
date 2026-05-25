---
title: "Pattern: Kth-Bit Operations"
summary: "Four one-line primitives — check, set, clear, toggle the bit at position K using a mask built by bit-shifting 1."
prereqs:

---

# The Bit-Manipulation Toolkit

A 32-bit integer is a row of 32 boolean cells. The bit at *position k* (counting from 1, starting at the least-significant end) is what the operations target. The trick: every kth-bit operation builds a **mask** — `1 << (k - 1)` — and combines it with the integer using a bitwise operator.

> 🖼 Diagram — Mask 1 &lt;&lt; (k - 1) for k = 3 isolates exactly bit 3. Every kth-bit operation combines this mask with the input via AND / OR / NOT-AND / XOR — one operator per intent.
```d2
direction: right
mask: "Mask for k = 3:  1 << (k - 1)" {
  grid-rows: 2
  grid-columns: 8
  grid-gap: 0
  b7: "0"
  b6: "0"
  b5: "0"
  b4: "0"
  b3: "0"
  b2: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b1: "0"
  b0: "0"
  l7: "[8]"
  l6: "[7]"
  l5: "[6]"
  l4: "[5]"
  l3: "[4]"
  l2: "[3]"
  l1: "[2]"
  l0: "[1]"
}
```

<p align="center"><strong>Mask <code>1 &lt;&lt; (k - 1)</code> for <code>k = 3</code> isolates exactly bit 3. Every kth-bit operation combines this mask with the input via AND / OR / NOT-AND / XOR — one operator per intent.</strong></p>

The four operators map onto the four operations:

| Intent | Operator | Why |
|---|---|---|
| **Check** if bit is on | `&` | AND with mask isolates bit k; non-zero ⇒ on |
| **Set** bit to 1 | `\|` | OR with mask forces bit k to 1; other bits unchanged |
| **Unset** bit to 0 | `& ~` | AND with inverted mask clears bit k; other bits unchanged |
| **Toggle** bit | `^` | XOR with mask flips bit k; other bits unchanged |

The mask is the unifying piece. Every kth-bit operation is "build the mask, apply the right operator." Different operator = different operation.

> *Predict before reading on — what does <code>1 &lt;&lt; (k - 1)</code> compute for <code>k = 1, 2, 3, 8</code>?*

`1, 2, 4, 128`. Each shift left by one doubles the value. `1 << 0 = 1`, `1 << 1 = 2`, `1 << 2 = 4`, `1 << 7 = 128`. The shift count is one less than the position because we 1-index positions but 0-index shifts.

## Indexing — 1-based vs 0-based

This section uses **1-based** bit positions throughout (`k = 1` is the least-significant bit). Many APIs and language libraries use **0-based** positions instead (`k = 0` is the LSB). Translation: the `(k - 1)` in our masks becomes plain `k` if you switch conventions. Pick one and stick with it; mixing is how off-by-one bugs sneak in.

---

## Key Takeaway

Every kth-bit primitive is `mask = 1 << (k - 1)` plus one of four operators. Memorise the mask; the rest is operator selection.

# Final Takeaway

The kth-bit operations are the alphabet of bit manipulation. Four primitives, one mask:

| Operation | Expression | Operator's Magic |
|---|---|---|
| Check | `num & (1 << (k - 1))` | AND isolates the bit |
| Set | `num \| (1 << (k - 1))` | OR forces to 1 |
| Unset | `num & ~(1 << (k - 1))` | NOT-AND forces to 0 |
| Toggle | `num ^ (1 << (k - 1))` | XOR flips |

**You didn't just learn four one-liners. You internalised the "build a mask, apply an operator" pattern that powers every higher-level bit-manipulation algorithm — bitmask DP, packed flag fields, fast subset enumeration, even cryptographic primitives. The next several lessons add more sophisticated mask-building tricks, but the operator selection stays the same.**

> *Transfer challenge for the next lesson:* Given a number, *find* the position of the only set bit (assuming there is exactly one) — without using a loop or the math library. Predict the trick.

<details>
<summary><strong>Answer</strong></summary>

For a number with exactly one bit set (i.e., a power of 2), `n & (n - 1)` equals 0. To find the *position* of that bit, you can either: take `log2(n) + 1` (math-based), or count bit positions by repeatedly right-shifting until the LSB is 1. The next lesson uses `n & (n - 1)` as the diagnostic test for "is there exactly one set bit?" and combines it with position-finding for two related problems.

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
