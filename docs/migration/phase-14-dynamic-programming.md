# Phase 14 — Dynamic Programming

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/5.dynamic-programming/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/05-algorithms-by-strategy/05-dynamic-programming/`

## Stats

| | Count |
|---|---:|
| Chapters | 18 (source) / 19 (destination) |
| Source lessons | 67 |
| Interactive diagrams | 20 |
| Destination size | 11,964 lines across 19 files |
| Indicative sessions | 10-12 |

Most chapters are concept-heavy; widget count is moderate.

## Widget readiness

DP's natural visualisation is a **2D memo table being filled**.
The existing `array-traversal` widget handles 1D DP (linear DP,
Fibonacci, climbing stairs) cleanly. 2D problems (LCS, edit distance,
knapsack with explicit table) need a grid widget that doesn't exist.

**Strategy**: convert 1D DP widgets now; defer 2D-table widgets.
For 2D problems, the *transition equation* can be shown as a static
mermaid flowchart while the table-evolution defers to a future
widget.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 14.1 | Linear DP | 2 | 0 | Concept-only. |
| 14.2 | Longest Increasing Subsequence (LIS) | 5 | 3 | 1D DP; widget fits. |
| 14.3 | Longest Common Subsequence (LCS) | 5 | 2 | 2D table; widget defer. |
| 14.4 | Longest Common Substring | 4 | 2 | 2D table; defer. |
| 14.5 | Edit Distance | 4 | 2 | 2D table; defer. |
| 14.6 | Longest Palindromic Subsequence | 4 | 2 | 2D table; defer. |
| 14.7 | Longest Palindromic Substring | 3 | 3 | 2D bool table; defer. |
| 14.8 | Palindrome Partitioning | 5 | 4 | DP + backtracking. |
| 14.9 | 0/1 Knapsack | 6 | 2 | 2D table; defer. |
| 14.10 | Unbounded Knapsack | 5 | 2 | 2D / 1D; defer. |
| 14.11 | Bounded Knapsack | 3 | 2 | 2D; defer. |
| 14.12 | Counting Knapsack | 1 | 0 | Concept. |
| 14.13 | Matrix Chain Multiplication | 2 | 0 | Concept. |
| 14.14 | Pattern: Edit Distance | 2 | 0 | Pattern intro. |
| 14.15 | Pattern: Subset Sum | 2 | 0 | Pattern intro. |
| 14.16 | Pattern: 2D Grid | 4 | 0 | Grid DP problems. |
| 14.17 | Pattern: Prefix Sum | 3 | 0 | Cumulative-sum pattern. |
| 14.18 | Practice | 7 | 0 | Mixed problems. |
| (destination only) | (1 extra file) | — | — | Cross-check. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 14.1 + 14.2 (Linear DP intro + LIS) |
| 2 | Ch 14.3 + 14.4 (LCS + LC Substring) |
| 3 | Ch 14.5 Edit Distance |
| 4 | Ch 14.6 + 14.7 Palindromic patterns |
| 5 | Ch 14.8 Palindrome Partitioning |
| 6 | Ch 14.9 0/1 Knapsack |
| 7 | Ch 14.10 + 14.11 Unbounded + Bounded Knapsack |
| 8 | Ch 14.12 + 14.13 Counting Knapsack + MCM |
| 9 | Ch 14.14 + 14.15 Edit Distance + Subset Sum patterns |
| 10 | Ch 14.16 2D Grid + Ch 14.17 Prefix Sum |
| 11 | Ch 14.18 Practice |
| 12 | Phase verification + status |

## Session task templates

### Session 1 (LIS)

```
Phase 14 — Dynamic Programming, Session 1.

Ch 14.1 Linear DP + Ch 14.2 LIS. LIS source typically has TWO
solutions: O(n^2) DP and O(n log n) patience-sorting / binary
search. Align both. Source's helper-function decomposition for the
binary-search variant uses `lower_bound` from the searching phase.

Convert the O(n^2) DP frame sequence to `array-traversal` widget
showing the `dp[]` array being filled.
```

### Sessions 2-7 (2D DP problems)

```
Phase 14 — Dynamic Programming, Session <N>.

Chapter <14.X> (<problem>). Align both the recursive-with-memo and
the bottom-up tabulation solutions. Source's transition equation is
the algorithmic heart — match the indexing exactly.

Defer 2D-table widget conversion to a future arc; if source has an
interactive sequence showing the table filling, note it as deferred
in the commit message.

For the recursive solutions, source uses a `memo` map (Python dict /
HashMap in Java); the bottom-up uses a 2D array. Match exactly.
```

### Sessions 8-10 (Patterns + Knapsack variants)

```
Phase 14 — Dynamic Programming, Session <N>.

Chapter <14.X>. Knapsack variants differ only in the inner loop
condition (whether item is reused or not). Match source's exact
loop structure — Reversed for 0/1, forward for unbounded.

For pattern chapters: source factors out the pattern template; do
the same. One commit per problem.
```

### Session 11 (Practice)

```
Phase 14 — Dynamic Programming, Session 11.

Ch 14.18 Practice. Mix of all patterns. Apply Verbatim Code
Alignment per problem.
```

### Session 12 (closeout)

```
Phase 14 — Dynamic Programming, Session 12 (closeout).

Phase verification + deferred 2D-table widget list for the future
widget arc.
```

## Gotchas

- **1D vs 2D DP space optimization** — many problems can use a 1D
  array if you iterate carefully. Source may show both; match
  source's preferred form per language. Don't unify to one.
- **Memoization key** — Python uses tuple keys for multi-arg memo
  (e.g., `(i, j)`); Java uses `i * n + j` or 2D array. Match per
  language.
- **`min` vs `max`** — many DP problems are mirror twins (longest
  vs shortest, max vs min). Flipping one operator inverts the
  answer.
- **Off-by-one in table size** — `dp[n+1][m+1]` (with sentinel row
  0) is common; source may use `dp[n][m]` instead. Verify source's
  exact dimensions.
- **Base case initialization** — `dp[0][0] = 0` or `dp[i][0] = i`
  — wrong initialization is the #1 DP bug. Match source's exact
  init code.
