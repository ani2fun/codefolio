# Sweep 3 — chapter coverage report
Chapter: `01-foundations`

One line per `inform`-mode section finished during the rewrite —
`author`-mode sections do not produce coverage rows.

Foundations status: **DONE** — 6 of 6 units complete.

## `01-foundations/lesson-01-asymptotic-analysis`  (pilot unit 3)

(LESSON_UNIT: all filled sections were `author` mode per the spec table.
Production Reality reshape applied the `**[System]** — uses [pattern] —
because [tradeoff]` format to existing entries. Practice Ladder kept the
numbered-list shape to preserve frozen indented code fences — VERIFY flag
on that decision is the lone open thread for the reviewer.

Embedded `## Memorize` migration: deleted from the lesson. The original
Quick recall, Code template, and Pattern triggers material survives in
git history at `sweep-3-baseline:<lesson-path>` for the CHAPTER_MEMORIZE_UNIT
to consult — it was retrieved during the chapter-memorize re-author.)

## `01-foundations/lesson-02-recurrence-relations-and-master-theorem`

(LESSON_UNIT: all filled sections — Quiz, Further Reading, plus Production
Reality / Practice Ladder / Cross-Links / Final Takeaway restructures —
were `author` mode. The four data-structure-shaped SWEEP-2 TODOs
(Understanding the Problem / Supported Operations / Internal Mechanics /
Working Example) were absorbed into the lesson's existing analytical
sections under the "merge / remove redundant" rule rather than authored
as duplicate sections.)

Embedded `## Memorize` migration: deleted from the lesson.

## `01-foundations/lesson-03-amortized-analysis`

03-amortized-analysis.md :: Understanding the Problem :: covered=[every-now-and-then framing, three-cost contrast, definition box, geometric-resize example]  legacy=[N/A — current-file authored]
03-amortized-analysis.md :: Internal Mechanics :: covered=[aggregate, accounting, potential]  legacy=[N/A — current-file authored]
03-amortized-analysis.md :: Supported Operations :: covered=[dyn-array push/pop, hash insert, binary counter, splay access, Fibonacci decrease-key/extract-min, Union-Find]  legacy=[N/A]
03-amortized-analysis.md :: Working Example :: covered=[dynamic array push (3 methods), binary counter increment]  legacy=[N/A]

Embedded `## Memorize` migration: deleted from the lesson (the subagent
missed the precondition; deleted from the main session before commit).
Cross-Links section was also missing from the agent's Pass A output and
re-authored in main with Prerequisites (Asymptotic Analysis, Recurrence
Relations) and What comes next (Memory Model, Arrays, Hash Tables).

One VERIFY flag in the file on Fibonacci-heap `extract-min` amortised
space — some references cite `O(log n)`, others cite `O(1)` auxiliary.
Reviewer to confirm.

## `01-foundations/lesson-04-proof-techniques`

(LESSON_UNIT: all filled sections were `author` mode. Four data-structure-
shaped SWEEP-2 TODOs were absorbed into the lesson's existing sections.
Production Reality entries lean into systems where formal proofs drove
the design — seL4, CompCert, AWS TLA+, Rust borrow checker, Linux RB-tree.
No legacy proof-techniques chapter at `~/Development/others/tutorial_dsa/`
matches by topic — `inform` sections silently degraded to `author`.)

Embedded `## Memorize` migration: deleted from the lesson.

## `01-foundations/lesson-05-memory-model-and-cache`

(LESSON_UNIT: all filled sections were `author` mode. Three existing
headers renamed to match the LESSON template (`'Big-O and wall-clock
disagree' → Understanding the problem`; `'The memory hierarchy' → Internal
mechanics`; `'Worked example' → Working example`). New Supported Operations
table inserted between Spatial/Temporal Locality and Working Example.
Production Reality covers NumPy, Postgres, Redis, ECS engines, JITs,
Linux kernel. Further Reading puts Drepper's memory paper first per
spec recommendation. Legacy tutorial has no parallel chapter on memory
hierarchy — `inform` sections silently degraded to `author`.)

Embedded `## Memorize` migration: deleted from the lesson + its trailing
Python fence.

Two VERIFY flags remain in-file — flagged by the subagent on technical
claims it was not fully confident on. Reviewer to confirm.

## `01-foundations/chapter-memorize-06-memorize`  (CHAPTER_MEMORIZE_UNIT)

06-memorize.md :: In a Hurry?         :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]
06-memorize.md :: One-Line Mnemonic   :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]
06-memorize.md :: Real-World Analogy  :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]
06-memorize.md :: Visual Summary      :: covered=[N/A — Sweep-5 placeholder]                              legacy=[N/A — author mode]
06-memorize.md :: Key Operations      :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]
06-memorize.md :: Common Mistakes     :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]
06-memorize.md :: Quick Recall        :: covered=[lesson-01, lesson-02, lesson-03, lesson-04, lesson-05]  legacy=[N/A — author mode]

The previous draft (authored from lesson-01 only in pilot unit 3) was
wholesale replaced. New file spans every lesson: 21 Quick Recall pairs
(was 12), 7 nested-bullet Common Mistakes (was 5), 15-row Key Operations
table (was 7), city-map Real-World Analogy tying zoom-level / growth /
schedule / structural-stamp / asphalt across the five lessons.

## Open questions for the reviewer

- Fibonacci-heap `extract-min` amortised space in
  `03-amortized-analysis.md` — `O(log n)` vs `O(1)` auxiliary.
- Two VERIFY flags in `05-memory-model-and-cache.md` — left in-file.
- Practice Ladder in `01-asymptotic-analysis.md` kept as a numbered list
  rather than the spec table — the baselined code fences nest inside list
  items at 3-space indent, which the table form would break. Decide
  whether the freeze policy bends or the table form relaxes for
  fence-in-list cases.
