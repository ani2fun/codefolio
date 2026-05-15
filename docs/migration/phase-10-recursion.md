# Phase 10 — Recursion

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/1.recursion/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/05-algorithms-by-strategy/01-recursion/`

## Stats

| | Count |
|---|---:|
| Chapters | 7 |
| Source lessons | 35 |
| Interactive diagrams | 6 |
| Destination size | 8,215 lines across 7 files |
| Indicative sessions | 6-7 |

Lightweight on interactive diagrams (just 6 across the whole phase).
Most chapters are concept-heavy and don't need widget conversion.

## Widget readiness

Recursion's natural visualisation is a **call-stack tree**. Source's
diagrams typically show call stack frames evolving. Neither
`array-traversal` nor a future widget fits perfectly.

For multidimensional recursion / memo-table visualisations, the
`array-traversal` widget can show 1D state evolution. 2D memo tables
need a grid widget that doesn't exist.

**Recommendation**: defer widget conversion. Focus on:
1. Code alignment — recursion is where source's exact base-case +
   recursive-call structure matters most. Off-by-one in the base
   case is catastrophic.
2. Memory-model and call-stack explanations — destination's prose
   here may differ from source; preserve destination's pedagogy
   unless it contradicts source's algorithm.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 10.1 | Introduction to Memory Model | 4 | 0 | Stack vs heap; concept-only. |
| 10.2 | Nested Functions | 3 | 4 | Closures, lexical scope. |
| 10.3 | Recursion | 4 | 5 | Definition + base case + recursive case. |
| 10.4 | Pattern: Head Recursion | 6 | 0 | Recurse first, then act. |
| 10.5 | Pattern: Tail Recursion | 6 | 0 | Act first, then recurse. |
| 10.6 | Pattern: Multiple Recursion | 6 | 0 | More than one recursive call. |
| 10.7 | Pattern: Multidimensional Recursion | 6 | 1 | 2D / nD recursion. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 10.1 + 10.2 (Memory Model + Nested Functions) |
| 2 | Ch 10.3 Recursion (full) |
| 3 | Ch 10.4 Head Recursion (full) |
| 4 | Ch 10.5 Tail Recursion (full) |
| 5 | Ch 10.6 Multiple Recursion (full) |
| 6 | Ch 10.7 Multidimensional Recursion (full) |
| 7 | Phase verification |

## Session task templates

### Session 1

```
Phase 10 — Recursion, Session 1.

Ch 10.1 Introduction to Memory Model + Ch 10.2 Nested Functions.
These are concept-heavy. Align any code examples (closure demos)
verbatim to source. Skip widget conversion.

One commit per chapter.
```

### Sessions 2-6 (Recursion + Patterns)

```
Phase 10 — Recursion, Session <N>.

Chapter <10.X> (<pattern>). Align problem code (5 langs) verbatim
to source. Pay extreme attention to:
- Base case condition (exact equality vs <, easy to flip)
- Recursive call argument (n-1 vs n-2 vs other reduction)
- Helper-method signatures (source may take extra "accumulator"
  parameter that destination might have hidden in a class field)

Source's recursion patterns (head / tail / multiple) are
pedagogically distinct — preserve source's classification. One
commit per problem.
```

### Session 7 (closeout)

```
Phase 10 — Recursion, Session 7 (closeout).

Phase verification. End with status note for Phase 11.
```

## Gotchas

- **Tail recursion in Python** — Python doesn't optimize tail calls.
  Source's "tail recursion" lessons might use iterative-equivalent
  code in Python or recursion with helper parameter. Preserve
  source's exact form per language.
- **Recursion depth** — Python's default recursion limit is 1000.
  Some problems (Fibonacci up to N) hit the limit. Source may
  include `sys.setrecursionlimit(...)`. Match if so.
- **Accumulator parameter convention** — source's tail-recursion
  helpers carry the accumulator as a parameter: `factorial(n, acc)`
  with `acc` defaulting to 1. Destination must use the same shape.
- **Memoization decorator vs manual cache** — source uses manual
  dict; don't replace with `functools.lru_cache` in Python.
