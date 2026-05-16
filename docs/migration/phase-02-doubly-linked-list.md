# Phase 2 — Doubly Linked List

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/04-doubly-linked-list/`

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `linked-list` (double mode) | ✓ reused | Built in Phase 1 (ADR-0014); set `direction: "double"` in payloads |

Per ADR-0006 (updated 2026-05-16): every phase's Definition of
Done includes converting source Interactive Diagrams to D3 widgets.
Phase 2 reuses Phase 1's `linked-list` widget; no new widget code
needed before content sessions begin. The widget auto-derives
`prev` arrows from each forward link when `direction: "double"`.

## Stats

| | Count |
|---|---:|
| Chapters | 9 |
| Source lessons | 58 |
| Interactive diagrams | 23 |
| Destination size | 12,875 lines across 9 files |
| Indicative sessions | 8-10 |

## Widget readiness

Same situation as Phase 1 — most diagrams visualise **prev/next
pointer rewiring**. Defer linked-list widget conversion until the
widget is built; do code/example alignment now.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 2.1 | Introduction to Doubly Linked Lists | 5 | 5 | Includes a `Boundary Node` problem; mirror the Phase 1 ch 1.1 alignment pattern. |
| 2.2 | Traversal in Doubly Linked Lists | 4 | 2 | Forward + backward traversal. |
| 2.3 | Insertion in Doubly Linked Lists | 10 | 10 | Insert-before / insert-after; both directions of pointer fix-up. |
| 2.4 | Deletion in Doubly Linked Lists | 15 | 15 | Largest chapter; budget 2 sessions. |
| 2.5 | Pattern: Reversal | 6 | 5 | Swap prev/next in each node. |
| 2.6 | Pattern: Reversal Subproblem | 5 | 1 | k-group reversal in DLL. |
| 2.7 | Pattern: Two Pointers | 6 | 2 | Forward + backward two-pointer. |
| 2.8 | Pattern: Reorder | 6 | 4 | DLL reorder. |
| 2.9 | Design | 1 | 0 | LRU cache via DLL. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 2.1 (Intro + Boundary Node) + Ch 2.2 (Traversal, Length, Search) |
| 2 | Ch 2.3 Insertion — concept + first half of problems |
| 3 | Ch 2.3 Insertion — second half |
| 4 | Ch 2.4 Deletion — first half |
| 5 | Ch 2.4 Deletion — second half |
| 6 | Ch 2.5 Reversal (full) |
| 7 | Ch 2.6 Reversal Subproblem (full) |
| 8 | Ch 2.7 Two Pointers (full) |
| 9 | Ch 2.8 Reorder (full) |
| 10 | Ch 2.9 Design + phase verification |

## Session task templates

### Session 1

```
Phase 2 — Doubly Linked List, Session 1.

Align Ch 2.1 (Introduction to Doubly Linked Lists) and Ch 2.2
(Traversal in Doubly Linked Lists) — code only, defer widgets.

For Ch 2.1: mirror the Phase 1 Boundary Node alignment shape
(commit 2b50bd2) — chained-elif with verbose source comments. For
Ch 2.2: align Node Search and Length-of-the-List code with the
verbose four-step comment narrative used in Phase 1 (commit b1194d1).

One commit per problem.
```

### Session 2 / 3 (Ch 2.3 Insertion)

```
Phase 2 — Doubly Linked List, Session <N> (Insertion <half>).

Ch 2.3 Insertion in Doubly Linked Lists — align problem code in all
5 language tabs. Pay attention to the prev/next pointer fix-up
sequencing in source — destination must match the order of pointer
updates (otherwise traces will diverge).

Defer widgets. One commit per problem.
```

### Sessions 4-9 (template)

```
Phase 2 — Doubly Linked List, Session <N>.

Full code alignment for Chapter <X.Y> (<name>): problem statement +
examples + 5-language solution code. Apply Verbatim Code Alignment.
Defer linked-list-pointer widget conversion until the widget is
built. One commit per problem.
```

### Session 10 (closeout)

```
Phase 2 — Doubly Linked List, Session 10 (closeout).

Ch 2.9 Design + phase-wide verification. Same closeout pattern as
Phase 1 — scalafmt, tests, build, browser smoke. End with a status
note pointing at Phase 3.
```

## Gotchas

- **Prev pointer ordering** — when inserting / deleting in a DLL,
  the order in which `prev` and `next` are updated matters. Source's
  code carries the canonical order with verbose comments; don't
  reorder for cleanliness.
- **Head and tail sentinels** — source may use sentinel nodes
  (`head.prev = null`, `tail.next = null`). Match source's
  convention; destination's existing prose may diverge.
- **DLL reversal swaps prev↔next, not values** — source's
  reversal solution swaps the *pointer fields* per node rather than
  moving values. Match this exactly.
