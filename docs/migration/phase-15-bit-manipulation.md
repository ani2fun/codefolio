# Phase 15 — Bit Manipulation

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/6.bit-manipulation/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/08-bit-tricks/`

## Widgets required

None. Source has 0 Interactive Diagrams in this phase — bit
manipulation uses inline truth tables and side-by-side static
diagrams rather than frame sequences. If author later decides
animated bit-row visuals are worth adding, propose `bit-row` as
a new widget; otherwise Phase 15 ships with static blocks only.

## Stats

| | Count |
|---|---:|
| Chapters | 6 |
| Source lessons | 21 |
| Interactive diagrams | 0 |
| Destination size | 2,880 lines across 6 files |
| Indicative sessions | 3-4 |

**Smallest phase by every metric.** No interactive diagrams at all
in source — pure code-and-explanation. Should be fast.

## Widget readiness

No widget conversion needed in this phase. Source has zero
interactive diagrams. Static d2/mermaid bit-pattern diagrams in
destination should stay as-is.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 15.1 | Pattern: Kth Bit | 4 | 0 | Get/set/clear/toggle the k-th bit. |
| 15.2 | Pattern: Set Bit Finder | 2 | 0 | Lowest/highest set bit; popcount. |
| 15.3 | Pattern: Restructuring | 2 | 0 | Bit reversal, swap nibbles. |
| 15.4 | Pattern: XOR | 7 | 0 | XOR identities (a^a=0, a^0=a, swap). |
| 15.5 | Pattern: Bitmasking | 2 | 0 | Subsets via bitmask, DP on subsets. |
| 15.6 | Pattern: Applications | 4 | 0 | Single number, missing number etc. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 15.1 Kth Bit + Ch 15.2 Set Bit Finder |
| 2 | Ch 15.3 Restructuring + Ch 15.4 XOR |
| 3 | Ch 15.5 Bitmasking + Ch 15.6 Applications |
| 4 | Phase verification + status |

## Session task templates

### Session 1

```
Phase 15 — Bit Manipulation, Session 1.

Ch 15.1 Pattern: Kth Bit + Ch 15.2 Pattern: Set Bit Finder.

Align getBit / setBit / clearBit / toggleBit operations to source's
exact expressions:
- getBit:    (x >> k) & 1
- setBit:    x | (1 << k)
- clearBit:  x & ~(1 << k)
- toggleBit: x ^ (1 << k)

Source's expression order matters — destination must match (e.g.,
`(x >> k) & 1` vs `(x & (1 << k)) != 0`).

For Ch 15.2: source's lowest-set-bit uses `x & -x` (Brian Kernighan);
match the expression literally per language.

One commit per problem.
```

### Session 2

```
Phase 15 — Bit Manipulation, Session 2.

Ch 15.3 Restructuring + Ch 15.4 XOR. XOR identities are subtle —
source's helper functions (like `findSingleNumber`, `findTwoSingles`)
have specific bit-grouping logic. Match the exact bit selector
(e.g., `xor & -xor` for the rightmost set bit).
```

### Session 3

```
Phase 15 — Bit Manipulation, Session 3.

Ch 15.5 Bitmasking + Ch 15.6 Applications. Subset enumeration via
`for (int mask = 0; mask < (1 << n); mask++)` — match source's
exact mask iteration order.

Pay attention to integer overflow:
- Python: arbitrary precision; no concern
- Java: `int` is 32-bit; `1 << 31` is INT_MIN
- C: `unsigned int` for shifts beyond 30
Match source's signedness/type choices.
```

### Session 4 (closeout)

```
Phase 15 — Bit Manipulation, Session 4 (closeout).

Phase verification. End with the final migration status note —
this is the last phase. The whole DSA section is now aligned.
```

## Gotchas

- **Language-specific integer widths** — Python has unbounded ints;
  Java/C/Scala have fixed-width. Some bit tricks (e.g., negating
  via two's complement) behave differently. Source's code per
  language already accounts for this; match exactly.
- **Negative numbers and right-shift** — `>>` in Java/C is
  arithmetic (sign-extending) for signed types, logical for
  unsigned. Python's `>>` for negatives gives floor (rounds toward
  -inf). Source may use `>>>` (Java unsigned right shift) for
  specific bit tricks; match.
- **Bit count / popcount** — Python `bin(x).count('1')`,
  Java `Integer.bitCount(x)`, C usually has a custom loop. Source's
  choice per language matters.
- **No off-by-one in bit index** — bit 0 is the LSB. Source uses
  `(x >> k) & 1` where k=0 gives the LSB. Don't flip to k=0 being
  MSB.
