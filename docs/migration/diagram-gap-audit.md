# DSA Diagram Gap Audit

> **Read first:** [`methodology.md`](methodology.md) for the standing migration
> brief. This file is the **scope + backlog** — what's in source, what's in
> destination, where the gaps are, and what order to tackle them.
> Per-diagram payload skeletons live in [`conversion-manifest.json`](conversion-manifest.json).

## Executive summary

| | Count |
|---|---:|
| Source `// Interactive Diagram` markers (across all phases) | **517** |
| Source lessons containing at least one interactive diagram | 366 (of 914) |
| Destination D3 widget instances (current) | **94** |
| Destination mermaid block instances | 1,129 |
| Destination chapter `.md` files | 219 |
| Source-to-destination phase mappings (1:1) | 16 |
| Orphan destination chapters (no source counterpart) | ~75 |
| Net new widget types required | **25** |
| Total widgets in catalog after Arc 1 | **27** (2 existing + 25 new) |

**Gap delta: ~423 source diagrams need destination widget instances. The
current 94 cover roughly Phase 0 (Arrays) + Phase 1 (Singly Linked List).**

## Source → destination phase mapping

The destination's directory structure mirrors source one-to-one for source's
14 substantive phases (phase 15 bit-manipulation has 0 source diagrams). Each
source chapter directory consolidates into a single destination `.md` file.

| Source dir | Destination dir | Source chapters | Source diagrams | Notes |
|---|---|---:|---:|---|
| `data-structure/1.arrays` | `02-linear-structures/01-arrays/` | 11 | 24 | Phase 0 done |
| `data-structure/2.singly-linked-list` | `02-linear-structures/03-singly-linked-list/` | 13 | 55 | Phase 1 done |
| `data-structure/3.doubly-linked-list` | `02-linear-structures/04-doubly-linked-list/` | 9 | 44 | Phase 2 — not started |
| `data-structure/4.hash-table` | `02-linear-structures/07-hash-table/` | 11 | 57 | Phase 3 — not started |
| `data-structure/5.stack` | `02-linear-structures/05-stack/` | 12 | 44 | Phase 4 — not started |
| `data-structure/6.queue` | `02-linear-structures/06-queue/` | 7 | 19 | Phase 5 — not started |
| `data-structure/7.binary-tree` | `03-trees/01-binary-tree/` | 18 | 39 | Phase 6 — not started |
| `data-structure/8.binary-search-tree` | `03-trees/02-binary-search-tree/` | 13 | 45 | Phase 7 — not started |
| `data-structure/9.heap` | `03-trees/03-heap/` | 5 | 20 | Phase 8 — not started |
| `data-structure/10.graph` | `04-graphs/` (flat) | 16 | 22 | Phase 9 — not started |
| `algorithms/1.recursion` | `05-algorithms-by-strategy/01-recursion/` | 7 | 10 | Phase 10 — not started |
| `algorithms/2.backtracking` | `05-algorithms-by-strategy/04-backtracking/` | 4 | 13 | Phase 11 — not started |
| `algorithms/3.sorting` | `06-sorting-and-searching/01-sorting/` | 12 | 56 | Phase 12 — not started |
| `algorithms/4.searching` | `06-sorting-and-searching/02-searching/` | 11 | 45 | Phase 13 — partial (1 widget) |
| `algorithms/5.dynamic-programming` | `05-algorithms-by-strategy/05-dynamic-programming/` | 18 | 24 | Phase 14 — not started |
| `algorithms/6.bit-manipulation` | `08-bit-tricks/` | 6 | 0 | Phase 15 — no source diagrams |

### Orphan destination chapters (no source counterpart) — IN SCOPE per user

Per user direction (2026-05-17): orphan chapters are in scope. Payloads are
written from destination prose + widget demo examples.

| Section | Chapters | Widget(s) needed |
|---|---|---|
| `01-foundations` | asymptotic-analysis, recurrence + master theorem, amortized analysis, proof techniques, memory + cache | `growth-rate-chart`, `recurrence-tree` (or `call-stack` mode), `amortized-payment`, `cache-hit-miss` |
| `03-trees` (beyond binary/BST/heap) | trie, self-balancing-bst-overview, avl-tree, red-black-tree, b-tree, segment-tree, fenwick-tree, dsu | `trie`, `binary-tree` modes (`avl`/`rbt`), reuse `BTreeWalker`, `segment-tree`, `fenwick-tree`, `dsu-forest` |
| `04-graphs` (advanced) | minimum-spanning-trees, strongly-connected-components, bridges-and-articulation-points, 2-sat | `graph-explorer` |
| `05-algorithms-by-strategy` (beyond recursion/backtracking/dp) | divide-and-conquer, greedy, randomized-algorithms | `decision-tree`, `array-traversal` |
| `07-strings` | naive-matching, kmp, z, rabin-karp, trie-strings, suffix-array, suffix-automaton, aho-corasick | `string-matcher`, `suffix-array-builder`, reuse `trie`, reuse `graph-explorer` |
| `09-probabilistic-and-advanced` | skip-list, bloom-filter, count-min-sketch, hyperloglog, treap, persistent | `skip-list`, `bloom-filter`, `count-min-sketch`, `hyperloglog`, `binary-tree` modes |
| `10-concurrency-and-systems` | CAS, lock-free-queue, concurrent-hash-map, RCU, distributed-teaser | `concurrent-timeline`, `stack-queue` mode, `hash-table` mode, reuse `RaftAnimator` |
| `11-dsa-in-real-systems` | postgres-b-tree, linux-rbt-cfs, redis-encodings, git-merkle-dag, lsm-trees, network-data-plane | Mostly reuse: `BTreeWalker`, `binary-tree`, `hash-table`/`linked-list`/`array-traversal`, `graph-explorer`, `lsm-tree`, `stack-queue` |

## Widget catalog (27 widgets total)

### Tier 1 — Core source-DSA topologies (11 widgets)

| # | Widget | Status | Spec file |
|---:|---|---|---|
| 1 | `array-traversal` | ✓ exists | [widget-specs/array-traversal.md](widget-specs/array-traversal.md) |
| 2 | `linked-list` | ✓ exists | [widget-specs/linked-list.md](widget-specs/linked-list.md) |
| 3 | `hash-table` | new | [widget-specs/hash-table.md](widget-specs/hash-table.md) |
| 4 | `stack-queue` | new | [widget-specs/stack-queue.md](widget-specs/stack-queue.md) |
| 5 | `binary-tree` | new | [widget-specs/binary-tree.md](widget-specs/binary-tree.md) |
| 6 | `heap-tree` | new | [widget-specs/heap-tree.md](widget-specs/heap-tree.md) |
| 7 | `graph-explorer` | new | [widget-specs/graph-explorer.md](widget-specs/graph-explorer.md) |
| 8 | `call-stack` | new | [widget-specs/call-stack.md](widget-specs/call-stack.md) |
| 9 | `decision-tree` | new | [widget-specs/decision-tree.md](widget-specs/decision-tree.md) |
| 10 | `dp-table` | new | [widget-specs/dp-table.md](widget-specs/dp-table.md) |
| 11 | `trie` | new | [widget-specs/trie.md](widget-specs/trie.md) |

### Tier 2 — Foundations widgets (4 widgets)

| # | Widget | Spec file |
|---:|---|---|
| 12 | `growth-rate-chart` | [widget-specs/growth-rate-chart.md](widget-specs/growth-rate-chart.md) |
| 13 | `recurrence-tree` | [widget-specs/recurrence-tree.md](widget-specs/recurrence-tree.md) |
| 14 | `amortized-payment` | [widget-specs/amortized-payment.md](widget-specs/amortized-payment.md) |
| 15 | `cache-hit-miss` | [widget-specs/cache-hit-miss.md](widget-specs/cache-hit-miss.md) |

### Tier 3 — Advanced trees (3 widgets, plus mode-extensions to tier-1)

| # | Widget | Spec file |
|---:|---|---|
| 16 | `segment-tree` | [widget-specs/segment-tree.md](widget-specs/segment-tree.md) |
| 17 | `fenwick-tree` | [widget-specs/fenwick-tree.md](widget-specs/fenwick-tree.md) |
| 18 | `dsu-forest` | [widget-specs/dsu-forest.md](widget-specs/dsu-forest.md) |
| reuse | `BTreeWalker` (existing system-design widget) | n/a |

### Tier 4 — String algorithms (2 widgets)

| # | Widget | Spec file |
|---:|---|---|
| 19 | `string-matcher` | [widget-specs/string-matcher.md](widget-specs/string-matcher.md) |
| 20 | `suffix-array-builder` | [widget-specs/suffix-array-builder.md](widget-specs/suffix-array-builder.md) |

### Tier 5 — Bit tricks (1 widget)

| # | Widget | Spec file |
|---:|---|---|
| 21 | `bit-grid` | [widget-specs/bit-grid.md](widget-specs/bit-grid.md) |

### Tier 6 — Probabilistic / advanced (4 widgets)

| # | Widget | Spec file |
|---:|---|---|
| 22 | `skip-list` | [widget-specs/skip-list.md](widget-specs/skip-list.md) |
| 23 | `bloom-filter` | [widget-specs/bloom-filter.md](widget-specs/bloom-filter.md) |
| 24 | `count-min-sketch` | [widget-specs/count-min-sketch.md](widget-specs/count-min-sketch.md) |
| 25 | `hyperloglog` | [widget-specs/hyperloglog.md](widget-specs/hyperloglog.md) |

### Tier 7 — Concurrency (1 widget)

| # | Widget | Spec file |
|---:|---|---|
| 26 | `concurrent-timeline` | [widget-specs/concurrent-timeline.md](widget-specs/concurrent-timeline.md) |

### Tier 8 — Real systems (1 widget)

| # | Widget | Spec file |
|---:|---|---|
| 27 | `lsm-tree` | [widget-specs/lsm-tree.md](widget-specs/lsm-tree.md) |

## Conditional widget decisions

These were flagged in the plan as "could fold into existing widget" — final
calls made here:

| Conditional | Decision | Rationale |
|---|---|---|
| `recurrence-tree` standalone vs `call-stack` mode | **Standalone widget** (`recurrence-tree`) | Recurrence trees in the Foundations chapter visualize cost decomposition at each level (`T(n) = aT(n/b) + f(n)`), not function frames. The visual is different enough (level-sum overlay, geometric series highlight) to warrant its own module. `call-stack`'s recursion-tree mode handles algorithmic recursion (different concern). |
| `skip-list` standalone vs `linked-list` extension | **Standalone widget** (`skip-list`) | Multi-level pointer rendering, express/data-lane animations, and probabilistic level-selection are sufficiently distinct from `linked-list`'s flat-chain rendering. Extending `linked-list` with layers would balloon the schema and complicate validation. |
| `grid-traversal` standalone vs `array-traversal` 2D mode | **Mode extension** (`array-traversal` gets `layout: "1d" \| "2d"`) | 2D matrix searches and grid graph problems share the cell-grid rendering; reusing the existing widget's transition machinery is cheaper than a parallel module. Adds 1 retrofit session to Arc 1. |
| `trie` standalone vs `binary-tree` mode | **Standalone widget** (`trie`) | Tries are multi-way (not binary), use character-edge labels, and have terminal-node highlight semantics. Different rendering pipeline. |
| `BTree-walker` reuse vs new build | **Reuse** the existing `client/.../widgets/BTreeWalker.scala` for B-Tree chapter + Postgres real-systems chapter. Possibly extend with insertion/deletion modes. |
| `RaftAnimator` reuse | **Reuse** for `10-concurrency-and-systems/05-distributed-data-structures-teaser.md`. |

## Per-chapter inventory

Per-chapter source diagram count, destination widget count, gap delta, and
which widget covers the chapter. Used as input for Arc 3 chapter-batch
agents.

### Phase 0 — Arrays (DONE, audit only)

Source: `data-structure/1.arrays/` (11 chapter dirs, 24 diagrams)
Destination: `02-linear-structures/01-arrays/` (11 .md files)
Widget: `array-traversal`

| Source chapter | Source diagrams | Dest file | Dest widgets | Status |
|---|---:|---|---:|---|
| `01-introduction-to-arrays` | 1 | `01-introduction.md` | 1 | ✓ |
| `02-multidimensional-arrays` | 3 | `02-multidimensional.md` | 0 | ⚠ gap (uses 16 mermaid; needs `array-traversal` 2D mode after retrofit) |
| `03-pattern-two-pointers` | 3 | `03-pattern-two-pointers.md` | 7 | ✓ (over-covered with extra demo steps) |
| `04-pattern-two-pointers-reduction` | 2 | `04-pattern-two-pointers-reduction.md` | 3 | ✓ |
| `05-pattern-two-pointers-subproblem` | 1 | `05-pattern-two-pointers-subproblem.md` | 2 | ✓ |
| `06-pattern-simultaneous-traversal` | 3 | `06-pattern-simultaneous-traversal.md` | 2 | ⚠ -1 gap |
| `07-pattern-fixed-sized-sliding-window` | 2 | `07-pattern-fixed-sliding-window.md` | 3 | ✓ |
| `08-pattern-variable-sized-sliding-window` | 3 | `08-pattern-variable-sliding-window.md` | 3 | ✓ |
| `09-pattern-interval-merging` | 3 | `09-pattern-interval-merging.md` | 3 | ✓ |
| `10-pattern-maximum-overlap` | 3 | `10-pattern-maximum-overlap.md` | 3 | ✓ |
| `11-design` | 0 | `11-design-a-dynamic-array.md` | 1 | ✓ (capacity-doubling widget) |

### Phase 1 — Singly Linked List (DONE, audit only)

Source: `data-structure/2.singly-linked-list/` (13 chapter dirs, 55 diagrams)
Destination: `02-linear-structures/03-singly-linked-list/` (13 .md files)
Widget: `linked-list`

| Source chapter | Source diagrams | Dest file | Dest widgets |
|---|---:|---|---:|
| `01-introduction-to-singly-linked-lists` | 5 | `01-introduction-to-singly-linked-lists.md` | 8 |
| `02-traversal-in-singly-linked-lists` | 2 | `02-traversal-in-singly-linked-lists.md` | 2 |
| `03-insertion-in-singly-linked-lists` | 11 | `03-insertion-in-singly-linked-lists.md` | 15 |
| `04-deletion-in-singly-linked-lists` | 14 | `04-deletion-in-singly-linked-lists.md` | 14 |
| `05-detecting-cycle-in-singly-linked-lists` | 2 | `05-detecting-cycle-in-singly-linked-lists.md` | 3 |
| `06-pattern-reversal` | 4 | `06-pattern-reversal.md` | 6 |
| `07-pattern-reversal-subproblem` | 1 | `07-pattern-reversal-subproblem.md` | 4 |
| `08-pattern-sliding-window-traversal` | 3 | `08-pattern-sliding-window-traversal.md` | 5 |
| `09-pattern-fast-and-slow-pointers` | 2 | `09-pattern-fast-and-slow-pointers.md` | 3 |
| `10-pattern-split` | 5 | `10-pattern-split.md` | 1 (⚠ -4 gap) |
| `11-pattern-merge` | 2 | `11-pattern-merge.md` | 2 |
| `12-pattern-reorder` | 4 | `12-pattern-reorder.md` | 1 (⚠ -3 gap) |
| `13-design` | 0 | `13-design.md` | 0 |

**Phase 1 follow-ups**: chapters 10 + 12 are under-covered. Source has 9
diagrams not converted; defer to Phase 1.6 catch-up session.

### Phase 2 — Doubly Linked List (NEXT — uses existing `linked-list`)

Source: `data-structure/3.doubly-linked-list/` (9 chapter dirs, 44 diagrams)
Destination: `02-linear-structures/04-doubly-linked-list/` (need to enumerate)
Widget: `linked-list` (direction:"double" — UNTESTED IN PRODUCTION)

| Source chapter | Source diagrams | Notes |
|---|---:|---|
| `01-introduction-to-doubly-linked-lists` | 5 | First exercise of `direction:"double"` mode |
| `02-traversal-in-doubly-linked-lists` | 2 | Forward + reverse traversal |
| `03-insertion-in-doubly-linked-lists` | 10 | Mirrors SLL insertion |
| `04-deletion-in-doubly-linked-lists` | 15 | Largest chapter |
| `05-pattern-reversal` | 5 | |
| `06-pattern-reversal-subproblem` | 1 | |
| `07-pattern-two-pointers` | 2 | |
| `08-pattern-reorder` | 4 | |
| `09-design` | 0 | LRU cache exercise |

### Phase 3 — Hash Table

Source: `data-structure/4.hash-table/` (11 chapter dirs, 57 diagrams)
Destination: `02-linear-structures/07-hash-table/` (need to enumerate)
Widget: `hash-table` (Arc 1 build required)

| Source chapter | Source diagrams |
|---|---:|
| `01-introduction-to-hash-tables` | 4 |
| `02-separate-chaining` | 6 |
| `03-linear-probing` | 10 |
| `04-quadratic-probing` | 10 |
| `05-double-hashing` | 10 |
| `06-pattern-counting` | 4 |
| `07-pattern-pattern-generation` | 3 |
| `08-pattern-fixed-sized-sliding-window` | 4 |
| `09-pattern-variable-sized-sliding-window` | 4 |
| `10-pattern-prefix-sum` | 2 |
| `11-design` | 0 |

### Phase 4 — Stack

Source: `data-structure/5.stack/` (12 chapter dirs, 44 diagrams)
Destination: `02-linear-structures/05-stack/` (need to enumerate)
Widget: `stack-queue` (Arc 1 build required)

| Source chapter | Source diagrams |
|---|---:|
| `01-introduction-to-stacks` | 7 |
| `02-array-implementation-of-stacks` | 3 |
| `03-linked-list-implementation-of-stacks` | 3 |
| `04-infix-postfix-and-prefix-notations` | 4 |
| `05-evaluating-expressions-using-stack` | 2 |
| `06-converting-expressions-using-stack` | 8 |
| `07-pattern-reversal` | 2 |
| `08-pattern-previous-closest-occurrence` | 4 |
| `09-pattern-next-closest-occurance` | 5 |
| `10-pattern-sequence-validation` | 3 |
| `11-pattern-linear-evaluation` | 3 |
| `12-design` | 0 |

### Phase 5 — Queue

Source: `data-structure/6.queue/` (7 chapter dirs, 19 diagrams)
Destination: `02-linear-structures/06-queue/` (need to enumerate)
Widget: `stack-queue` (reuse from Phase 4)

| Source chapter | Source diagrams |
|---|---:|
| `01-introduction-to-queues` | 7 |
| `02-array-implementation-of-queues` | 5 |
| `03-linked-list-implementation-of-queues` | 3 |
| `04-pattern-fifo` | (varies) |
| `05-pattern-bfs` | (varies) |
| `06-priority-queue` | (varies) |
| `07-design` | 0 |

### Phase 6 — Binary Tree

Source: `data-structure/7.binary-tree/` (18 chapter dirs, 39 diagrams)
Destination: `03-trees/01-binary-tree/` (18 .md files)
Widget: `binary-tree` (Arc 1 build required)

Largest source phase by chapter count. Build `binary-tree` carefully.

### Phase 7 — BST

Source: `data-structure/8.binary-search-tree/` (13 chapter dirs, 45 diagrams)
Destination: `03-trees/02-binary-search-tree/` (13 .md files)
Widget: `binary-tree` (mode `bst`) — reuse

### Phase 8 — Heap

Source: `data-structure/9.heap/` (5 chapter dirs, 20 diagrams)
Destination: `03-trees/03-heap/` (5 .md files)
Widget: `heap-tree` (Arc 1 build required)

### Phase 9 — Graph

Source: `data-structure/10.graph/` (16 chapter dirs, 22 diagrams)
Destination: `04-graphs/` (20 .md files — flat, includes 4 advanced orphan chapters)
Widget: `graph-explorer` (Arc 1 build required)

### Phase 10 — Recursion

Source: `algorithms/1.recursion/` (7 chapter dirs, 10 diagrams)
Destination: `05-algorithms-by-strategy/01-recursion/` (7 .md files)
Widget: `call-stack` (Arc 1 build required)

### Phase 11 — Backtracking

Source: `algorithms/2.backtracking/` (4 chapter dirs, 13 diagrams)
Destination: `05-algorithms-by-strategy/04-backtracking/` (4 .md files)
Widget: `decision-tree` (Arc 1 build required)

### Phase 12 — Sorting

Source: `algorithms/3.sorting/` (12 chapter dirs, 56 diagrams)
Destination: `06-sorting-and-searching/01-sorting/` (12 .md files)
Widget: `array-traversal` (reuse)

### Phase 13 — Searching

Source: `algorithms/4.searching/` (11 chapter dirs, 45 diagrams)
Destination: `06-sorting-and-searching/02-searching/` (11 .md files)
Widget: `array-traversal` (reuse) + `array-traversal` 2D mode (retrofit)

### Phase 14 — Dynamic Programming

Source: `algorithms/5.dynamic-programming/` (18 chapter dirs, 24 diagrams)
Destination: `05-algorithms-by-strategy/05-dynamic-programming/` (19 .md files — extras are orphan)
Widget: `dp-table` (Arc 1 build required)

### Phase 15 — Bit Manipulation

Source: `algorithms/6.bit-manipulation/` (6 chapter dirs, 0 diagrams)
Destination: `08-bit-tricks/` (6 .md files)
Widget: `bit-grid` for orphan-style diagrams (no source mapping; agent
creates payloads from destination prose)

### Orphan section — Foundations

Destination: `01-foundations/` (5 .md files)
Widgets: `growth-rate-chart`, `recurrence-tree`, `amortized-payment`,
`cache-hit-miss`

### Orphan section — Advanced trees (beyond core 3)

Destination: `03-trees/04-trie/`, `05-self-balancing-bst-overview/`,
`06-avl-tree/`, `07-red-black-tree/`, `08-b-tree/`, `09-segment-tree/`,
`10-fenwick-tree/`, `11-disjoint-set-union/`
Widgets: `trie`, `binary-tree` (modes `avl` / `rbt`), reuse `BTreeWalker`,
`segment-tree`, `fenwick-tree`, `dsu-forest`

### Orphan section — Strings

Destination: `07-strings/` (8 .md files)
Widgets: `string-matcher`, `suffix-array-builder`, reuse `trie`, reuse
`graph-explorer`

### Orphan section — Probabilistic & advanced

Destination: `09-probabilistic-and-advanced/` (6 .md files)
Widgets: `skip-list`, `bloom-filter`, `count-min-sketch`, `hyperloglog`,
`binary-tree` (modes `treap`/`persistent`)

### Orphan section — Concurrency

Destination: `10-concurrency-and-systems/` (5 .md files)
Widgets: `concurrent-timeline`, `stack-queue` mode (lock-free), `hash-table`
mode (concurrent), reuse `RaftAnimator`

### Orphan section — Real systems

Destination: `11-dsa-in-real-systems/` (6 .md files)
Widgets: mostly reuse — `BTreeWalker`, `binary-tree` (mode `rbt`),
`hash-table`/`linked-list`/`array-traversal`, `graph-explorer` (DAG layout),
`lsm-tree`, `stack-queue`

## Existing widget bug audit framework

Approach: deferred to Arc 2 hardening, executed via browser automation
(`mcp__Claude_Preview__*`):

1. For each of the 94 existing widget instances (per `/tmp/dest-widgets.txt`):
   - Navigate to chapter URL: `http://localhost:5173/cortex/data-structures-and-algorithms-<slug>`
   - Take screenshot
   - Capture console logs
   - Exercise step controls (click next/prev N times)
   - Classify into ✓ / ⚠ / ✗
2. Record findings in `docs/migration/widget-bug-log.md` (created during
   Arc 2).
3. Group bug fixes into single-batch agent commits during Arc 2.

Known concerns to check first (from past commit messages):
- Linked-list canon violations (commit `996eddc` did a 186→0 sweep — verify
  no regressions)
- Mermaid Unicode in node labels (`∞`, `①`, `−`) — render errors
- Markdown fence imbalances around long payloads

## Sequenced execution backlog (Arc 3)

ROI-prioritized; each phase = 1-7 sessions depending on size. Phases sharing
a widget can be batched.

### Wave A — Re-use existing widgets (no precursor blocked)

1. **Phase 2** (Doubly Linked List) — uses `linked-list`. 9 chapters, ~44
   diagrams. **5-7 sessions** (or **2-3 parallel batches**).
2. **Phase 12** (Sorting) — uses `array-traversal`. 12 chapters, ~56
   diagrams. **5-7 sessions** (or **3 parallel batches**).
3. **Phase 13** (Searching) — uses `array-traversal` + 2D mode. 11
   chapters, ~45 diagrams. **4-5 sessions** (or **2 parallel batches**).
4. **Phase 1 catch-up** — convert remaining 7 diagrams in chapters 10 + 12.
   **1 session**.

### Wave B — Build precursor, then batch (Tier 1 widgets)

5. **`stack-queue` precursor → Phase 5 (Queue) → Phase 4 (Stack)** — single
   widget unblocks two phases. Precursor 1 session; Phase 5 = 3-4
   sessions; Phase 4 = 5-7 sessions. Total **9-12 sessions** or **4-5
   parallel batches**.
6. **`hash-table` precursor → Phase 3** — 1 + 6-8 sessions. **7-9 sessions**
   or **3-4 parallel batches**.
7. **`binary-tree` precursor → Phase 6 (Binary Tree) → Phase 7 (BST)** —
   single widget unblocks two phases. Precursor 1-2 sessions; Phase 6 =
   10-12 sessions; Phase 7 = 7-8 sessions. Total **18-22 sessions** or
   **8-10 parallel batches**.
8. **`heap-tree` precursor → Phase 8** — 1 + 3-4 sessions. **4-5 sessions**.
9. **`graph-explorer` precursor → Phase 9** — 1-2 + 8-10 sessions. Hardest
   widget; **9-12 sessions** or **4-5 parallel batches**.
10. **`call-stack` precursor → Phase 10** — 1 + 4-5 sessions. **5-6 sessions**.
11. **`decision-tree` precursor → Phase 11** — 1 + 3-4 sessions. **4-5
    sessions**.
12. **`dp-table` precursor → Phase 14** — 1 + 7-8 sessions. **8-9 sessions**.

### Wave C — Orphan chapter widgets

13. **`bit-grid` precursor → Phase 15 + `08-bit-tricks`** — 1 + 3-4 sessions.
14. **`growth-rate-chart` + `amortized-payment` + `cache-hit-miss` +
    `recurrence-tree` precursors → `01-foundations`** — 4 + 3-4 sessions.
15. **`trie` precursor → `03-trees/04-trie` + `07-strings/05-trie-string-applications`** —
    1 + 2 sessions.
16. **`segment-tree` + `fenwick-tree` + `dsu-forest` precursors → `03-trees/09..11`** —
    3 + 3 sessions.
17. **`binary-tree` mode extensions (avl/rbt/treap/persistent) → `03-trees/06..07` + `09-probabilistic.../05..06`** —
    1 retrofit session + 4-5 sessions.
18. **`string-matcher` + `suffix-array-builder` precursors → `07-strings/01..04 + 06`** —
    2 + 4-5 sessions.
19. **`bloom-filter` + `count-min-sketch` + `hyperloglog` + `skip-list`
    precursors → `09-probabilistic-and-advanced/01..04`** — 4 + 4 sessions.
20. **`concurrent-timeline` precursor + extensions → `10-concurrency.../01..04`** —
    1 + 3-5 sessions.
21. **`lsm-tree` precursor + reuse mapping → `11-dsa-in-real-systems/01..06`** —
    1 + 4-5 sessions.

### Wave D — Catalog hardening (Arc 2)

22. **Edge-case payload sweep** for each widget — 1 session.
23. **Shared abstraction extraction** (extend `Stepper.scala` etc.) —
    1 session.
24. **Per-widget canon validators** — 1 session.
25. **Authoring guide** `docs/migration/widget-authoring.md` — 1 session.

### Total optimized arc

| Arc | Sequential sessions | Parallel batches |
|---|---:|---:|
| Arc 0 (this) | 1 | 1 |
| Arc 1 (25 widget builds) | 25-30 | **5-7 batches** |
| Arc 2 (hardening) | 4 | 1-2 |
| Arc 3 (content migration) | 100-130 | **15-25 batches** |
| **Total** | ~140 sessions | **~25-35 batches** ≈ **1-2 weeks focused / 3-5 days AFK** |

## Source-to-destination chapter map (excerpt)

For each phase, the mechanical mapping `source/<topic>/<chapter-dir>/*.md →
destination/<dest-section>/<chapter>.md` is encoded in
[`conversion-manifest.json`](conversion-manifest.json). Arc 3 agents read the
manifest to know which source files feed which destination file and what
widget+payload skeleton to emit per source diagram marker.

## Verification

- Source counts (517 markers) verified via:
  `grep -rh "^// Interactive Diagram" /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/ | wc -l`
- Destination widget counts (94 instances) verified via:
  ``grep -rn "^```d3 widget=" content/cortex/data-structures-and-algorithms/ | wc -l``
- Per-phase counts cross-checked against `/tmp/source-per-chapter-flat.txt`
  and `/tmp/dest-per-chapter.txt` (generated at audit time).
- Per-widget POC chapter references resolved against
  `docs/migration/widget-specs/<widget>.md`.

## Open decisions (revisit at Arc 0 close or per-phase)

- **Branch strategy** — main vs new worktree vs cherry-pick. This doc lands
  on whatever branch the user prefers.
- **Demo book retention** — keep `content/cortex/dsa-widget-catalog/` as
  living authoring reference (recommended) or delete after Arc 3.
- **Existing infra widget reuse depth** — `RaftAnimator`,
  `CacheStampedeSimulator`, `ConsistentHashRing`, etc. Decide per-chapter
  during Arc 3.
- **Static-fallback policy** for diagrams that don't compress well into 5-10
  steps — could leave as mermaid/D2 with a note rather than force a widget.
