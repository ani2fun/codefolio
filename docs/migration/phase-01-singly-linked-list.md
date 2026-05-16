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

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `linked-list` (single direction) | ✓ ADR-0014, commit `a371b5f` | Built as Phase 1 precursor. Handles pointer rewiring, traversal, pattern-matching (fast/slow, sliding window), and cycle detection via top-level `cycleTarget` |
| `array-traversal` | ✓ reused | Used by ch 1.1's two array-vs-list comparison diagrams (already converted, commit `a7062f2`) |
| `linked-list` (double direction) | ✓ available | Not exercised in Phase 1 (ch 1.13 is "Design a Singly Linked List", no LRU/DLL content). First use in Phase 2 ch 2.1 |

Per ADR-0006 (updated 2026-05-16): every phase's Definition of
Done includes converting source Interactive Diagrams to D3
widgets. Phase 1's widget precursor work is **done** (commits
`9a46768` ADR + `a371b5f` widget code); the chapter-by-chapter
conversion happens in **Phase 1.5** (see section below).

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
| 1.13 | Design | 1 | 0 | 612 | "Design a Singly Linked List" exercise (head + currentSize + optional tail trade-offs). 1 widget after Phase 1.5. |

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

## Phase 1 — DONE (2026-05-15)

All 13 chapters of the Singly Linked List phase are now
code-aligned. Recap:

| Chapter | Status | Key alignment work |
|---|---|---|
| 1.1 Introduction | ✓ | Boundary Node chained-elif (commit `2b50bd2`) |
| 1.2 Traversal | ✓ | Node Search, Length-of-List, Node Expedition (`8ccc477`, `b1194d1`, follow-ups) |
| 1.3 Insertion | ✓ | 10 problems aligned (head/tail/before/after insertion) |
| 1.4 Deletion | ✓ | 15 problems aligned (largest chapter) |
| 1.5 Cycle Detection | ✓ | Floyd's algorithm |
| 1.6 Reversal | ✓ | Iterative + recursive reversal |
| 1.7 Reversal Subproblem | ✓ | k-group reversal |
| 1.8 Sliding Window Traversal | ✓ | Forward window |
| 1.9 Fast and Slow Pointers | ✓ | Middle node, halves, palindrome (`3bf1eda`–`ec60a4a`) |
| 1.10 Split | ✓ | Even/odd, alternate groups, modulo, k-way (`0080e5d`–`d18e086`) |
| 1.11 Merge | ✓ | Alternate fusion, sorted merge, sorted-merge-II reverse-then-merge, list addition (`d140b86`, `e3407fa`, `6740d21`, `76adfbf`, `4e480f8`) |
| 1.12 Reorder | ✓ | Split-+-merge helper decomposition restored across all four problems (`3bfd62c`, `ffd6852`, `c15f5a6`, `cb5813c`) |
| 1.13 Design | ✓ | Verified already aligned (no changes needed) |

**No longer deferred (2026-05-16):** the `linked-list` widget
(ADR-0014, commit `a371b5f`) lands as a Phase 1 precursor. The
Phase 1.5 arc converts all 23 mandatory drift cases plus ~70
static-d2 linked-list upgrades — ~93 widget instances total. See
the "Phase 1.5" section below.

**New drift patterns captured in memory** (`dsa_align_source_code_verbatim.md`):

- Single-function collapse of split + merge decomposition (Ch 1.12).
- Three-loop list addition flattened to single loop (Ch 1.11).
- Scala `.v` typo (should be `.val`) recurred across 3 chapters.
- C `c run` blocks are independent translation units — duplicate
  function names across blocks compile cleanly.
- Helper methods in Java/Scala carry `private`; port the modifier.

## Phase 1.5 — Interactive Diagram conversion — DONE (2026-05-16)

After ADR-0014 + commit `a371b5f` landed the `linked-list` widget,
Phase 1.5 retroactively converted source's `// Interactive Diagram`
markers and major static d2 linked-list blocks into D3 widget
instances. One commit per chapter.

| Ch | File | Widgets | Commit |
|---:|---|---:|---|
| 1.1 | `01-introduction-…` | 5 (+ 2 existing array widgets) | `8970ae7` |
| 1.2 | `02-traversal-…` | 1 | `a82ce6d` |
| 1.3 | `03-insertion-…` | 14 | `aea09d9` |
| 1.4 | `04-deletion-…` | 14 (3 reused via replace_all across sub-chapters) | `173e680` |
| 1.5 | `05-detecting-cycle-…` | 3 (Floyd's, uses `cycleTarget`) | `4b6114f` |
| 1.6 | `06-pattern-reversal` | 6 | `8ede8f7` |
| 1.7 | `07-pattern-reversal-subproblem` | 4 | `9b1a89a` |
| 1.8 | `08-pattern-sliding-window-traversal` | 5 | `3e14821` |
| 1.9 | `09-pattern-fast-and-slow-pointers` | 3 | `8c5fd19` |
| 1.10 | `10-pattern-split` | 1 | `b34f970` |
| 1.11 | `11-pattern-merge` | 1 | `7fd8e65` |
| 1.12 | `12-pattern-reorder` | 1 | `c0a0be2` |
| 1.13 | `13-design` | 1 (no DLL content in Phase 1) | `29fbe8f` |

**Total: 59 `linked-list` widgets + 2 `array-traversal` widgets = 61.**

The "comprehensive" target was 93 (23 mandatory drift + 70 d2
upgrades). The realised 59 represents all 23 mandatory cases plus
~36 of the higher-value d2 upgrades. The remaining ~34 static
blocks (conceptual flow charts, complexity analysis grids,
variable-state cards, multi-array dummies/tails layouts) didn't
fit the `linked-list` widget shape well. Those are good candidates
for future widget types — e.g. a "variable-state" or "multi-list"
widget — but defer until a need surfaces.

**Doubly-linked-list mode unverified in Phase 1** — chapter 1.13
is "Design a Singly Linked List" (not LRU), so no `direction:
"double"` payload was authored. The widget's doubly mode will be
exercised first in Phase 2 chapter 2.1; residual risk acknowledged
in ADR-0014.
