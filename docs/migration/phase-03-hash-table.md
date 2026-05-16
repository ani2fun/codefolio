# Phase 3 — Hash Table

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/4.hash-table/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/07-hash-table/`

## Widgets required

| Widget | Status | Notes |
|---|---|---|
| `hash-table` | to-build | ADR pending. Source has ~57 Interactive Diagrams (largest of any phase) — primarily bucket arrays with chained or probed entries, insertion / deletion / collision-handling animations |

Per ADR-0006 (updated 2026-05-16): widget builds run as a precursor
session at the start of the phase before any chapter content work.
Phase 3 requires building the `hash-table` widget first (ADR-NNNN
→ Scala module → CSS → dispatcher → POC, mirroring ADR-0014 /
LinkedList.scala).

## Stats

| | Count |
|---|---:|
| Chapters | 11 |
| Source lessons | 69 |
| Interactive diagrams | 28 |
| Destination size | 12,861 lines across 11 files |
| Indicative sessions | 10-12 |

## Widget readiness

Hash table visualisations show **buckets + collision behaviour**.
Most diagrams fit a 2-row `array-traversal` layout (bucket array on
top, value sequence/chain on bottom). The collision strategies
(separate chaining / linear / quadratic / double hashing) each have
unique probing patterns that can be marker-driven.

Best fit: **extend existing `array-traversal` widget usage** to
represent buckets-with-collisions. The pattern chapters (counting,
prefix-sum, sliding-window) can reuse arrays widgets directly since
they're hash-table-as-counter applications.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 3.1 | Introduction to Hash Tables | 7 | 4 | Hash function, load factor, basic operations. |
| 3.2 | Separate Chaining | 7 | 6 | Bucket array + linked list per bucket. |
| 3.3 | Linear Probing | 7 | 10 | Open addressing; lots of probing-step frame sequences. |
| 3.4 | Quadratic Probing | 7 | 10 | Open addressing with i² stepping. |
| 3.5 | Double Hashing | 7 | 10 | Two hash functions. |
| 3.6 | Pattern: Counting | 7 | 4 | Frequency counter; widgets fit `array-traversal`. |
| 3.7 | Pattern: Pattern Generation | 6 | 3 | Anagram-style problems. |
| 3.8 | Pattern: Fixed-Sized Sliding Window | 6 | 4 | Hash + sliding window; widgets reuse Phase 0 templates. |
| 3.9 | Pattern: Variable-Sized Sliding Window | 7 | 4 | Same — borrow Phase 0 widget patterns. |
| 3.10 | Pattern: Prefix Sum | 6 | 2 | Hash for prefix-sum lookup. |
| 3.11 | Design | 2 | 0 | Hash-set / hash-map design. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 3.1 Introduction (full) |
| 2 | Ch 3.2 Separate Chaining — concept + first half of problems |
| 3 | Ch 3.2 second half |
| 4 | Ch 3.3 Linear Probing (full or first half if heavy) |
| 5 | Ch 3.3 finish + Ch 3.4 Quadratic Probing concept |
| 6 | Ch 3.4 Quadratic Probing problems + Ch 3.5 Double Hashing concept |
| 7 | Ch 3.5 Double Hashing problems |
| 8 | Ch 3.6 Counting + Ch 3.7 Pattern Generation |
| 9 | Ch 3.8 Fixed Sliding Window (reuse Phase 0 widget templates) |
| 10 | Ch 3.9 Variable Sliding Window |
| 11 | Ch 3.10 Prefix Sum |
| 12 | Ch 3.11 Design + phase verification |

## Session task templates

### Session 1

```
Phase 3 — Hash Table, Session 1.

Ch 3.1 Introduction to Hash Tables. Align Hash Function and Load
Factor problems' code (5 langs) per Verbatim Code Alignment. Source
introduces the hash + bucket vocabulary — match destination's
glossary if it diverges. Skip widgets if they require new
visualisation types.

One commit per problem.
```

### Sessions 2-7 (template — collision strategies)

```
Phase 3 — Hash Table, Session <N>.

Chapter <3.X> (<strategy>): align <Insert / Search / Delete> code in
all 5 language tabs. Source's helper-method decomposition: there's
usually a `probe(i)` or `hash_<n>(key)` helper — destination must
extract the same helper. Apply Verbatim Code Alignment.

Convert probing-sequence interactive diagrams to `array-traversal`
widgets where they fit a linear bucket array. One commit per
problem.
```

### Sessions 8-11 (pattern chapters)

```
Phase 3 — Hash Table, Session <N>.

Chapter <3.X> (pattern). These problems use hash table + array
patterns from Phase 0; reuse the `array-traversal` widget templates
established in Phase 0 commits (e.g., 56a8adb fixed-window, 5a1537d
consecutive-ones). Full alignment + widget conversion.
```

### Session 12 (closeout)

```
Phase 3 — Hash Table, Session 12 (closeout).

Ch 3.11 Design + phase-wide verification. End with status note for
Phase 4.
```

## Gotchas

- **Hash function visualisation** — source frames often show hash
  computation (key → hash code → bucket index). This is a 3-step
  flow that fits a static d2 better than `array-traversal`. Keep as
  d2 unless source has a multi-step animation.
- **Load factor / rehashing** — destination may already have good
  prose; only adjust the code if source's rehash trigger condition
  (`size > capacity * 0.75`) differs.
- **Open-addressing tombstones** — linear / quadratic / double
  hashing may use a sentinel `DELETED` marker. Match source's
  spelling (e.g. `Tombstone`, `DELETED`, `-1`).
