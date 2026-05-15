# DSA migration — per-phase session prompts

This directory holds **per-phase session briefs** that go alongside
the standing methodology at
[`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md).

## How to use

1. **Read the standing brief once.** It covers the methodology that
   applies to every phase: hard rules, Authority matrix, Verbatim
   Code Alignment, widget conversion decision tree, verification
   gates, commit cadence, recovery procedure.
2. **Pick the phase you're working on** from the table below.
3. **Open that phase file.** It has phase-specific context: lesson
   counts, recommended session breakdown, gotchas. It also includes
   ready-to-paste **Session task** strings for each sub-session.
4. **Paste both into the new session.** The standing brief is the
   policy; the phase file is the plan; the Session task is the
   prompt.

## Phase status

| Phase | Section | Source | Destination | Status | File |
|---|---|---|---|---|---|
| 0 | Arrays | `data-structure/1.arrays` | `02-linear-structures/01-arrays` | **DONE** | — |
| 1 | Singly Linked List | `data-structure/2.singly-linked-list` | `02-linear-structures/03-singly-linked-list` | in progress | [`phase-01-singly-linked-list.md`](phase-01-singly-linked-list.md) |
| 2 | Doubly Linked List | `data-structure/3.doubly-linked-list` | `02-linear-structures/04-doubly-linked-list` | pending | [`phase-02-doubly-linked-list.md`](phase-02-doubly-linked-list.md) |
| 3 | Hash Table | `data-structure/4.hash-table` | `02-linear-structures/07-hash-table` | pending | [`phase-03-hash-table.md`](phase-03-hash-table.md) |
| 4 | Stack | `data-structure/5.stack` | `02-linear-structures/05-stack` | pending | [`phase-04-stack.md`](phase-04-stack.md) |
| 5 | Queue | `data-structure/6.queue` | `02-linear-structures/06-queue` | pending | [`phase-05-queue.md`](phase-05-queue.md) |
| 6 | Binary Tree | `data-structure/7.binary-tree` | `03-trees/01-binary-tree` | pending | [`phase-06-binary-tree.md`](phase-06-binary-tree.md) |
| 7 | Binary Search Tree | `data-structure/8.binary-search-tree` | `03-trees/02-binary-search-tree` | pending | [`phase-07-binary-search-tree.md`](phase-07-binary-search-tree.md) |
| 8 | Heap | `data-structure/9.heap` | `03-trees/03-heap` | pending | [`phase-08-heap.md`](phase-08-heap.md) |
| 9 | Graph | `data-structure/10.graph` | `04-graphs` | pending | [`phase-09-graph.md`](phase-09-graph.md) |
| 10 | Recursion | `algorithms/1.recursion` | `05-algorithms-by-strategy/01-recursion` | pending | [`phase-10-recursion.md`](phase-10-recursion.md) |
| 11 | Backtracking | `algorithms/2.backtracking` | `05-algorithms-by-strategy/04-backtracking` | pending | [`phase-11-backtracking.md`](phase-11-backtracking.md) |
| 12 | Sorting | `algorithms/3.sorting` | `06-sorting-and-searching/01-sorting` | pending | [`phase-12-sorting.md`](phase-12-sorting.md) |
| 13 | Searching | `algorithms/4.searching` | `06-sorting-and-searching/02-searching` | pending | [`phase-13-searching.md`](phase-13-searching.md) |
| 14 | Dynamic Programming | `algorithms/5.dynamic-programming` | `05-algorithms-by-strategy/05-dynamic-programming` | pending | [`phase-14-dynamic-programming.md`](phase-14-dynamic-programming.md) |
| 15 | Bit Manipulation | `algorithms/6.bit-manipulation` | `08-bit-tricks` | pending | [`phase-15-bit-manipulation.md`](phase-15-bit-manipulation.md) |

## Size at a glance

| Phase | Source lessons | Interactive diagrams (frame sequences) | Destination lines | Indicative session count |
|---|---:|---:|---:|---:|
| 1 — Singly Linked List | 80 | 30 | 19,255 | 12-15 |
| 2 — Doubly Linked List | 58 | 23 | 12,875 | 8-10 |
| 3 — Hash Table | 69 | 28 | 12,861 | 10-12 |
| 4 — Stack | 76 | 30 | 9,344 | 10-12 |
| 5 — Queue | 26 | 10 | 3,808 | 4-5 |
| 6 — Binary Tree | 109 | 32 | 9,993 | 12-15 |
| 7 — Binary Search Tree | 77 | 29 | 9,736 | 10-12 |
| 8 — Heap | 31 | 12 | 5,092 | 5-6 |
| 9 — Graph | 77 | 21 | 14,736 | 12-15 |
| 10 — Recursion | 35 | 6 | 8,215 | 6-7 |
| 11 — Backtracking | 21 | 7 | 5,410 | 4-5 |
| 12 — Sorting | 48 | 23 | 7,366 | 8-10 |
| 13 — Searching | 51 | 17 | 5,036 | 6-8 |
| 14 — Dynamic Programming | 67 | 20 | 11,964 | 10-12 |
| 15 — Bit Manipulation | 21 | 0 | 2,880 | 3-4 |

**Total**: ~846 source lessons, ~288 interactive diagrams,
~130k destination lines, **~120-150 sessions**.

The session counts assume the pacing in the standing brief: 3-5
problem alignments + 1-3 widget conversions per session, with
verification and commits between.

## Widget readiness by phase

Some phases need a **new widget type** that the current closed
catalog doesn't have. These are flagged in their individual phase
files:

- **Phases 1, 2** — linked list visualisations (nodes + arrows).
  Existing `array-traversal` covers list-as-cells but not pointer
  re-wiring animation. May need a `linked-list` widget.
- **Phases 6, 7, 8** — tree visualisations (insert / delete / rotate
  / heapify). Need a `tree-traversal` widget.
- **Phase 9** — graph visualisations (DFS / BFS / Dijkstra
  step-throughs). Need a `graph-traversal` widget.

For phases that need new widgets, treat the widget build as its own
ADR + arc — don't sketch them in mid-content. The phase file
suggests deferring widget-conversion sub-sessions until the widget
exists, while still doing code/example alignment in the meantime.

## Conventions reminder (these are non-negotiable)

- **Five-language tabs**: Pseudocode + Python + Java + C + Scala
- **Solo author**: no `Co-Authored-By:` trailers
- **Local commits only**: no `git push` without explicit per-round OK
- **Verbatim Code Alignment**: source's helpers + variable names +
  inline comments win. See
  [`memory/dsa_align_source_code_verbatim.md`](../../memory/dsa_align_source_code_verbatim.md).

Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
once before any phase session, then return here for the
phase-specific plan.
