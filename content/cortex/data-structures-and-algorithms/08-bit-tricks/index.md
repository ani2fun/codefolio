# Bit Tricks

Below the level of "integer", "boolean", or "character", there are just bits — and a small handful of operations on them (`AND`, `OR`, `XOR`, `NOT`, shift) that let you do things with breathtaking efficiency: pack a set into a single 64-bit word, swap two integers without a temp, count the number of 1s in a register in 4 instructions, build a hash that fits in a CPU pipeline. This module collects the moves competitive programmers and systems engineers reach for before falling back to a higher-level structure.

## Place in the curriculum

- **Prerequisites:** [Foundations](/cortex/data-structures-and-algorithms/foundations-index) (the memory-model chapter explains binary and two's complement, which every trick here assumes).
- **Followed by:** [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) (bitmask DP relies on every pattern in this module).

## Chapters

- **Introduction and Mental Model** — *stub*. Binary first, two's complement, signed vs unsigned, the operator table you'll use forever.
- [Pattern: Kth Bit](/cortex/data-structures-and-algorithms/bit-tricks-pattern-kth-bit) — read, set, clear, toggle the kth bit of an integer.
- [Pattern: Set Bit Finder](/cortex/data-structures-and-algorithms/bit-tricks-pattern-set-bit-finder) — locate the lowest / highest set bit.
- [Pattern: Restructuring](/cortex/data-structures-and-algorithms/bit-tricks-pattern-restructuring) — pack, unpack, reverse, swap.
- [Pattern: XOR](/cortex/data-structures-and-algorithms/bit-tricks-pattern-xor) — XOR's algebraic properties (self-inverse, associative, commutative) and the problems they collapse.
- [Pattern: Bitmasking](/cortex/data-structures-and-algorithms/bit-tricks-pattern-bitmasking) — represent a set as an integer; iterate over subsets.
- [Pattern: Applications](/cortex/data-structures-and-algorithms/bit-tricks-pattern-applications) — composite tricks: parity check, divisibility, power-of-two test.
- **Popcount and Low-Level Tricks** — *stub*. `__builtin_popcount`, `ctz`/`clz`, Brian Kernighan's bit-counting, SWAR.
