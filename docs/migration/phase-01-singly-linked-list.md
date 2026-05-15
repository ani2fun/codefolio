# Phase 1 — Singly Linked List

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/2.singly-linked-list/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/03-singly-linked-list/`

## Stats

| | Count |
|---|---:|
| Chapters | 13 |
| Source lessons | 80 |
| Interactive diagrams (frame sequences) | 30 |
| Destination size | 19,255 lines across 13 files |
| Indicative sessions | 12-15 |

## Widget readiness

Most diagrams in this phase visualise **linked-list pointer rewiring**
(insertion before/after a node, deletion, cycle detection, reversal).
The existing `array-traversal` widget can show a row of nodes-as-cells
but can't animate **pointer arrows snapping to new targets**. Two
strategies for this phase:

- **Option A — defer most widget conversions.** Do code + examples +
  problem-statement alignment now. Build a `linked-list` widget in a
  separate arc; come back later for widget conversion.
- **Option B — use `array-traversal` with secondary row** for the
  subset of diagrams that show array-vs-list comparison (ch 1.1 has
  two: array insert-by-copy, array delete-by-copy — these work fine
  with the existing widget).

This phase's recommended path: **Option A** for true linked-list
diagrams; **Option B** for the ch 1.1 array-comparison diagrams.

## Per-chapter breakdown

| # | Chapter | Source lessons | Interactive diagrams | Dest lines | Notes |
|---:|---|---:|---:|---:|---|
| 1.1 | Introduction to Singly Linked Lists | 6 | 5 | 904 | Boundary Node already code-aligned (commit `2b50bd2`). Two array-comparison diagrams fit `array-traversal`; three linked-list diagrams defer. |
| 1.2 | Traversal in Singly Linked Lists | 4 | 2 | 681 | Node Search + Length of the List already code-aligned (commits `8ccc477`, `b1194d1`). Node Expedition still to do. |
| 1.3 | Insertion in Singly Linked Lists | 10 | 11 | 2,261 | **Biggest by widget count.** 11 frame sequences across 5 source files. Defer most widgets; do code alignment first. |
| 1.4 | Deletion in Singly Linked Lists | 15 | 14 | 4,710 | **Largest chapter in the phase.** 15 source lessons; budget two sessions just for code. |
| 1.5 | Detecting Cycle in Singly Linked Lists | 3 | 2 | 949 | Floyd's algorithm; widget defer. |
| 1.6 | Pattern: Reversal | 6 | 4 | 1,400 | Iterative + recursive reversal. |
| 1.7 | Pattern: Reversal Subproblem | 5 | 1 | 1,649 | k-group reversal. |
| 1.8 | Pattern: Sliding Window Traversal | 6 | 3 | 1,394 | Linked-list sliding window. |
| 1.9 | Pattern: Fast and Slow Pointers | 6 | 2 | 1,135 | Tortoise-and-hare. |
| 1.10 | Pattern: Split | 6 | 5 | 1,262 | List bisection. |
| 1.11 | Pattern: Merge | 6 | 2 | 1,004 | Merge two sorted lists. |
| 1.12 | Pattern: Reorder | 6 | 4 | 1,135 | Reorder list interleave. |
| 1.13 | Design | 1 | 0 | 612 | LRU / linked list design. No widgets. |

## Recommended session breakdown

The 13 chapters split naturally into ~12-15 sessions:

| Session | Scope | Expected commits |
|---|---|---|
| 1 (DONE in part) | Ch 1.1 + 1.2 code alignment | ✓ commits 2b50bd2, 8ccc477, b1194d1 |
| 2 | Finish Ch 1.2 (Node Expedition); Ch 1.3 code for problems only | 5-7 |
| 3 | Ch 1.3 widgets (2-array comparisons) + concept walkthrough | 2-3 |
| 4 | Ch 1.4 Deletion — problems 1-4 code alignment | 4-5 |
| 5 | Ch 1.4 Deletion — problems 5-8 code alignment | 4-5 |
| 6 | Ch 1.5 Cycle Detection (full) | 2-3 |
| 7 | Ch 1.6 Reversal (full) | 4-5 |
| 8 | Ch 1.7 Reversal Subproblem (full) | 3-4 |
| 9 | Ch 1.8 Sliding Window Traversal (full) | 4-5 |
| 10 | Ch 1.9 Fast-and-Slow Pointers (full) | 4-5 |
| 11 | Ch 1.10 Split (full) | 4-5 |
| 12 | Ch 1.11 Merge (full) | 3-4 |
| 13 | Ch 1.12 Reorder (full) | 3-4 |
| 14 | Ch 1.13 Design + Phase verification | 2-3 |
| 15 (optional) | Linked-list widget build + retroactive conversions | — |

## Session task templates

Copy ONE of these into the **Session task** block of the standing
brief at the start of each session:

### Session 2

```
Phase 1 — Singly Linked List, Session 2.

Finish Ch 1.2: align Node Expedition code (Pseudocode + Python + Java
+ C + Scala) to source `02-traversal-in-singly-linked-lists/02-node-expedition.md`.

Then start Ch 1.3 Insertion: align problem code only — skip widgets
for now (defer until the linked-list widget is built). Source
problems are at `03-insertion-in-singly-linked-lists/02-…` through
`10-…`. Apply Verbatim Code Alignment.

One commit per problem. End-of-session summary with the recommended
Session 3 task.
```

### Session 3

```
Phase 1 — Singly Linked List, Session 3.

Ch 1.3 widgets only. The chapter has 11 interactive frame sequences
in source; defer the linked-list-pointer ones. Convert any
array-comparison frame sequences (insertion-via-array-copy etc.) to
`array-traversal` widgets — use 2-row layout if needed.

Also align Ch 1.3's concept-walkthrough sections (Understanding
Insertion, etc.) — these may have prose that cites stale example
data after code alignment in Session 2.
```

### Session 4

```
Phase 1 — Singly Linked List, Session 4.

Ch 1.4 Deletion — first half. Align problem code for the first four
problems on the chapter (read destination outline with `grep "^# "`
to identify them). Skip widgets. One commit per problem. Apply
Verbatim Code Alignment.
```

### Session 5

```
Phase 1 — Singly Linked List, Session 5.

Ch 1.4 Deletion — second half. Same approach as Session 4 for the
remaining problems.
```

### Session 6

```
Phase 1 — Singly Linked List, Session 6.

Ch 1.5 Detecting Cycle in Singly Linked Lists. Full alignment —
problem statement, examples, code (5 langs), Floyd's-algorithm
helper decomposition if source uses one. Skip widgets.
```

### Sessions 7-13

```
Phase 1 — Singly Linked List, Session <N>.

Full alignment for Chapter <X.Y> (<chapter name>):
- Problem statement + examples per the Authority matrix
- Code in all 5 language tabs per Verbatim Code Alignment
- Defer interactive-diagram widget conversions (linked-list widget
  doesn't exist yet)

One commit per problem.
```

### Session 14 (closeout)

```
Phase 1 — Singly Linked List, Session 14 (closeout).

Ch 1.13 Design pass. Then run phase-wide verification:
- sbt scalafmtCheckAll, scalafmtSbtCheck
- sbt test
- npm run build (Vite production)
- Browser smoke test: visit each chapter under
  /cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-<slug>
  and verify no mermaid errors, no widget errors, code blocks compile.

End with a phase status note pointing at Phase 2.
```

## Gotchas specific to this phase

- **`ListNode` definition** — source comments out the definition in
  most lessons (assumes judge platform provides it). Destination's
  runnable blocks MUST define it inline (one Python class, one Java
  static inner class, one C struct + `newNode`, one Scala class).
  Keep destination's test harness; only swap the function body.
- **`null` vs `None` vs `nullptr` vs `NULL`** — match the language.
  Source's comments often say "null" generically; preserve "null" in
  Python comments if source says "null", but the code uses `None`.
- **Length-of-list reverse direction** — source's `lengthOfTheList`
  returns `0` for empty input; verify destination returns `0` too
  (commit `b1194d1` already did this).
- **Pattern chapters use "left/right" or "slow/fast"** — different
  chapters use different pointer-naming conventions. Match source
  per chapter; don't normalise across chapters.

## Definition of done for Phase 1

- All 13 chapters have problem code matching source verbatim in all
  5 language tabs.
- All chapters' Python tabs verified to produce source's example
  outputs.
- All static d2/mermaid diagrams render without error in the
  browser.
- (Optional) Interactive-diagram conversion done if the linked-list
  widget has been built by then.
- Memory file `dsa_align_source_code_verbatim.md` updated with any
  new drift patterns discovered.
- One Phase 1 closeout commit summarising the work.
