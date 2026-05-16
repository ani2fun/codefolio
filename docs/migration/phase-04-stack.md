# Phase 4 — Stack

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/5.stack/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/05-stack/`

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `stack-queue` | to-build | ADR pending. Source has 44 Interactive Diagrams — push/pop animations with a single growing/shrinking column of cells, expression-evaluation traces. Phase 5 (Queue) reuses this widget |

Per ADR-0006 (updated 2026-05-16): widget builds run as a precursor
session at the start of the phase before any chapter content work.
Phase 4 builds the `stack-queue` widget (used by both Phase 4 and
Phase 5).

## Stats

| | Count |
|---|---:|
| Chapters | 12 |
| Source lessons | 76 |
| Interactive diagrams | 30 |
| Destination size | 9,344 lines across 12 files |
| Indicative sessions | 10-12 |

## Widget readiness

Stack visualisations are **vertical with a `top` marker**. The
existing `array-traversal` widget displays cells horizontally;
authors can interpret index 0 as the bottom and the rightmost
populated cell as the top with a `top` marker. Push / pop animations
become item-append / item-clear at the right end.

For expression-conversion problems (infix↔postfix↔prefix), the 2-row
layout works well: input expression on top, operator stack on
bottom.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 4.1 | Introduction to Stacks | 4 | 7 | LIFO concept; push/pop/peek/empty operations. |
| 4.2 | Array Implementation of Stacks | 9 | 3 | Stack as fixed array + top index. |
| 4.3 | Linked-List Implementation of Stacks | 8 | 3 | Stack as singly linked list. |
| 4.4 | Infix, Postfix, and Prefix Notations | 3 | 4 | Notation theory; mostly concept. |
| 4.5 | Evaluating Expressions Using Stack | 5 | 2 | Postfix evaluation. |
| 4.6 | Converting Expressions Using Stack | 12 | 8 | **Biggest chapter** — 6 conversion variants. |
| 4.7 | Pattern: Reversal | 6 | 2 | Reverse via push-all-then-pop-all. |
| 4.8 | Pattern: Previous Closest Occurrence | 6 | 4 | Monotonic stack (decreasing). |
| 4.9 | Pattern: Next Closest Occurrence | 9 | 5 | Monotonic stack (increasing). |
| 4.10 | Pattern: Sequence Validation | 6 | 3 | Bracket matching. |
| 4.11 | Pattern: Linear Evaluation | 6 | 3 | Stream evaluation. |
| 4.12 | Design | 2 | 0 | Min stack, max stack. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 4.1 + 4.2 (Intro + Array Implementation) |
| 2 | Ch 4.3 Linked-list Implementation (full) |
| 3 | Ch 4.4 Notations + Ch 4.5 Evaluating Expressions |
| 4 | Ch 4.6 Converting Expressions — first half (3 conversions) |
| 5 | Ch 4.6 Converting Expressions — second half (3 conversions) |
| 6 | Ch 4.7 Reversal + Ch 4.8 Previous Closest |
| 7 | Ch 4.8 finish + Ch 4.9 Next Closest |
| 8 | Ch 4.9 finish (it's 9 lessons) |
| 9 | Ch 4.10 Sequence Validation |
| 10 | Ch 4.11 Linear Evaluation |
| 11 | Ch 4.12 Design + phase verification |
| 12 (optional) | Retroactive widget improvements |

## Session task templates

### Session 1

```
Phase 4 — Stack, Session 1.

Ch 4.1 (Introduction to Stacks) + Ch 4.2 (Array Implementation of
Stacks). Align operation code (push, pop, peek, isEmpty, isFull) to
source's verbatim style across all 5 language tabs. Source uses
explicit `top` index variable — destination must too.

Convert push/pop interactive diagrams to `array-traversal` with a
`top` marker if they fit a horizontal layout.

One commit per operation.
```

### Sessions 2-3 (Stack implementations)

```
Phase 4 — Stack, Session <N>.

Chapter <4.X>. Align stack operations (push, pop, peek, isEmpty,
size) to source. For Ch 4.3 LL implementation: pay attention to
which end the stack grows from — source's convention is usually
"push at head" for O(1).
```

### Sessions 4-5 (Expression conversion — the heavy chapter)

```
Phase 4 — Stack, Session <N>.

Ch 4.6 Converting Expressions Using Stack — <half>. Source has 6
conversion variants:
- Infix to Postfix
- Infix to Prefix
- Postfix to Infix
- Postfix to Prefix
- Prefix to Infix
- Prefix to Postfix

Align <three of them> this session. The operator-precedence helper
(`precedence(c)` or `getPrecedence(c)`) is a key helper to extract
per source. One commit per conversion.

For the 2-row widget visualisations, use input-on-top + stack-on-
bottom layout.
```

### Sessions 6-8 (Monotonic stack patterns)

```
Phase 4 — Stack, Session <N>.

Ch <4.X> (<previous / next closest>). Monotonic stack pattern —
when popping, source's loop condition spells out the comparison
direction. Match it exactly (`<` vs `<=`, increasing vs
non-decreasing). Widgets: stack-on-bottom, scanning-array-on-top,
2-row layout.
```

### Sessions 9-11 (remaining patterns + design)

```
Phase 4 — Stack, Session <N>.

Chapter <4.X> (<name>). Full code alignment + widget conversion
where the 2-row stack layout fits. One commit per problem.
```

## Gotchas

- **Stack-overflow / underflow handling** — source typically uses
  explicit `if (isFull) throw` / `if (isEmpty) return -1`.
  Destination may omit. Match source's error handling exactly.
- **Char vs string keys** — bracket-matching uses single characters;
  source's Java uses `char`, Python uses single-char `str`. Match
  per language.
- **Operator precedence table** — source's `precedence` helper
  returns specific integers (1 for `+/-`, 2 for `*/`, 3 for `^`).
  Use the same numbers.
