---
title: "Pattern: Bitmasking"
summary: "Represent a set as a bitmask — toggle pairs of bits, enumerate all 2^n subsets by counting from 0 to 2^n−1."
prereqs:
  - 08-bit-tricks/01-pattern-kth-bit/01-pattern
---

# What "Bitmasking" Means

Bitmasking has two distinct meanings, both common:

**Meaning A — A constant pattern that selects bits.** A "mask" is a number whose set bits select a region of interest. The kth-bit operations from lesson 1 are the simplest examples; this lesson generalises to multi-bit masks like `0x55555555` (every other bit, starting from bit 1) and `0xAAAAAAAA` (the complement). These masks let you operate on whole groups of bits in parallel — swap odd-positioned bits with even-positioned bits, count bits in groups, etc.

**Meaning B — A subset encoded as bits.** With `n` items, an `n`-bit integer can represent any subset: bit i set ⇒ element i is in. Subsets of `{a, b, c}` map to 8 integers `0..7`:

```
0b000 = 0  →  {}
0b001 = 1  →  {a}
0b010 = 2  →  {b}
0b011 = 3  →  {a, b}
0b100 = 4  →  {c}
0b101 = 5  →  {a, c}
0b110 = 6  →  {b, c}
0b111 = 7  →  {a, b, c}
```

Looping over `mask = 0` through `2^n - 1` enumerates every subset. Inside the loop, `mask & (1 << j)` checks "is element j in?", `mask | (1 << j)` adds, `mask & ~(1 << j)` removes. Set operations become bitwise ops with O(1) cost.

> 🖼 Diagram — Both flavours appear in this lesson. Pairwise bits swap uses the constant-mask flavour; unique subsets uses the subset-encoding flavour.
```d2
direction: right
both: "Two flavors of bitmasking" {
  grid-rows: 1
  grid-columns: 2
  grid-gap: 20
  a: |md
    **A — Constant masks**
    `0x55555555` selects odd bits
    `0xAAAAAAAA` selects even bits
    Use for parallel bit-group ops
  |
  b: |md
    **B — Subset encoding**
    Bit i set ⇒ element i is in
    Loop `mask = 0..2^n` enumerates all subsets
    Set ops become bitwise ops
  |
}
```

<p align="center"><strong>Both flavours appear in this lesson. Pairwise bits swap uses the constant-mask flavour; unique subsets uses the subset-encoding flavour.</strong></p>

> *Predict before reading on — for an array of <code>n = 4</code> elements, how many distinct subsets exist?*

`2^4 = 16`, including the empty subset and the full set. The set of *all* subsets is called the **power set**, and its size is always `2^n`.

---

## Key Takeaway

Bitmasking gives you two parallel powers: constant masks for parallel bit-group operations, and subset-encoded masks for combinatorial enumeration in linear-loop time.

# Final Takeaway

Bitmasking turns "iterate every subset" — exponential by nature — into a one-line for-loop. And constant masks like `0x55555555` and `0xAAAAAAAA` turn "swap groups of bits" into branchless O(1) code:

| Idiom | Use case |
|---|---|
| `for mask in 0..2^n` | Enumerate every subset of `n` items |
| `mask & (1 << j)` | Test if element `j` is in subset `mask` |
| `mask \| (1 << j)` | Add element `j` to subset |
| `mask & ~(1 << j)` | Remove element `j` |
| `(num & 0x55555555) << 1` | Shift odd-positioned bits to even positions |

**You didn't just learn to enumerate subsets. You learned that any combinatorial problem on `n ≤ 30` items can be solved in `O(2^n × poly(n))` by iterating bit-mask subsets — the foundation of bitmask DP, traveling-salesman-style problems, and packed-state algorithms. Constant masks turn bit-group manipulations into branchless one-liners that compilers love.**

> *Transfer challenge for the next lesson:* You want to compute `num^n` (`num` to the power of `n`) using only multiplication — but `n` could be up to a billion. A naive loop is too slow. Predict how the bits of `n` give you a logarithmic algorithm.

<details>
<summary><strong>Answer</strong></summary>

Write `n` in binary. For each bit set in `n`, multiply the result by `num²ⁱ` (where `i` is the bit's position). The trick: maintain `num` as you iterate, repeatedly squaring it — by the time you reach bit `i`, `num` already equals `original_num^(2^i)`. Total multiplications = number of set bits + bit-width = `O(log n)`. The next lesson formalises this as **fast exponentiation** alongside parity checking and power-of-2 testing — three classic bit-trick applications.

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
