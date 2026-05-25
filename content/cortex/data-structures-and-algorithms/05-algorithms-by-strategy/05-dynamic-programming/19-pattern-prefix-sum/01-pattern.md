---
title: "Pattern: Prefix Sum (2D)"
summary: "Precompute a 2-D prefix-sum matrix so any rectangle-sum is O(1) — submatrix-sum queries and range aggregates."
prereqs:
  - 05-algorithms-by-strategy/05-dynamic-programming/01-linear-dp
---

# The Prefix-Sum Pattern

The 1D pattern:

```
prefix[0] = 0
prefix[i] = prefix[i - 1] + arr[i - 1]    for i = 1..n

sum(arr[l..r]) = prefix[r + 1] - prefix[l]    O(1)
```

Construction is `O(n)`. Each query is `O(1)`. After construction, you can answer arbitrarily many sum queries on different `(l, r)` ranges in constant time per query.

The 2D extension is the same idea on a matrix:

```
prefix[i][j] = sum of matrix[0..i-1][0..j-1]
prefix[i][j] = prefix[i-1][j] + prefix[i][j-1] - prefix[i-1][j-1] + matrix[i-1][j-1]

sum of rectangle (r1, c1) to (r2, c2) inclusive
  = prefix[r2+1][c2+1] - prefix[r1][c2+1] - prefix[r2+1][c1] + prefix[r1][c1]
```

The "subtract twice, add back once" formula is **inclusion-exclusion** — when you compute the prefix to the right edge and the prefix to the bottom edge, the top-left corner gets *subtracted twice*, so you add it back.

> 🖼 Diagram — Inclusion-exclusion for a rectangle: +BR − TR − BL + TL. The top-left contributes +1 because subtracting both top-row and left-column subtracts it twice; we add it back once.
```d2
direction: right
incex: "Inclusion-exclusion in 2D" {
  grid-rows: 4
  grid-columns: 4
  grid-gap: 0
  c00: "+TL"
  c01: ""
  c02: ""
  c03: "−"
  c10: ""
  c11: ""
  c12: ""
  c13: ""
  c20: ""
  c21: ""
  c22: ""
  c23: ""
  c30: "−"
  c31: ""
  c32: ""
  c33: "+BR"
}
```

<p align="center"><strong>Inclusion-exclusion for a rectangle: <code>+BR − TR − BL + TL</code>. The top-left contributes +1 because subtracting both top-row and left-column subtracts it twice; we add it back once.</strong></p>

> *Pause. Why is the formula NOT just `prefix[r2+1][c2+1] - prefix[r1][c1]`? Predict the failure case.*

Because that subtraction would overshoot. `prefix[r1][c1]` is the sum of cells *above-left* of `(r1, c1)`. Removing it from `prefix[r2+1][c2+1]` doesn't surgically excise the rectangle's "above" and "left" strips — it removes only the corner. We need to remove the whole *strip above* (`prefix[r1][c2+1]`) and the whole *strip left* (`prefix[r2+1][c1]`), then add back the corner that got subtracted twice (`prefix[r1][c1]`).

## Where this shows up

Image processing (integral images for fast convolution / Haar features in face detection), data analytics (group-by sums on time-series, sliding-window aggregates over OHLCV bars), competitive programming (any "sum over a range" query, with extensions to count, max, GCD via more sophisticated structures), and bioinformatics (counting matches in genome windows).

---

## Key Takeaway

Prefix sums precompute "all sums up to here" so every later range-sum query becomes `O(1)`. 1D uses subtraction; 2D uses inclusion-exclusion (subtract twice, add back once).

# Final Takeaway

The prefix-sum pattern is the simplest precompute-then-query trick in algorithmics — and one of the most reused. Three problems, three readings of the same table:

| Problem | Read |
|---|---|
| K-limited submatrix sum | One sweep over fixed-size windows |
| Maximum submatrix sum | Quadruple loop over all corner pairs |
| Range sum finder | One inclusion-exclusion per query |

The pattern stretches further with creative state choices: prefix counts (for "how many elements satisfy P in this range"), prefix XORs (for "find subarray with XOR = target"), or prefix mod-counts (for "how many subarrays have sum divisible by k"). Each variant trades the sum operator for another aggregation — so long as the operator is associative *and* invertible (i.e. has an inverse, like subtraction undoes addition), the prefix trick works.

**You didn't just learn three matrix problems. You learned that any operator that supports cheap "extend by one" *and* "subtract" can power a precompute-then-query data structure with `O(1)` queries. That insight unlocks Fenwick trees, segment trees, sparse tables, and a small library of advanced range-query structures you'll meet later — all variations on this same theme.**

> *Transfer challenge for the next lesson:* Now that you've seen 11 distinct DP shapes (linear DP, LIS, LCS, LCSubstr, edit distance, palindromic substring/subsequence, palindrome partitioning, word break, knapsack family, knapsack applications, optimal strategy, boolean parenthesisation, matrix chain, edit-distance pattern, subset-sum pattern, 2D-grid pattern, prefix-sum pattern), the next lesson is a **practice set** — a curated mixed bag of problems across all the patterns. Predict which one shape will appear most often.

<details>
<summary><strong>Answer</strong></summary>

The edit-distance pattern (2D prefix DP) and the knapsack family appear most often — they're the bread-and-butter of interview DP problems. The next lesson presents a practice set covering one problem per pattern, so you can self-test before moving on.

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Pattern — missing, needs to be written -->
<!--       Guidance: umbrella H2 with the subsections below -->

<!-- TODO: Why Naive Isn't Enough — missing, needs to be written -->
<!--       Guidance: motivation for why the obvious approach fails -->

<!-- TODO: The Core Idea — missing, needs to be written -->
<!--       Guidance: one paragraph: the central trick -->

<!-- TODO: How the Pointers/Window Move — missing, needs to be written -->
<!--       Guidance: mechanics of the moving parts -->

<!-- TODO: The Generic Algorithm — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Generic Implementation — missing, needs to be written -->
<!--       Guidance: Python block + Java block of the skeleton -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table -->

<!-- TODO: Variants / Taxonomy — missing, needs to be written -->
<!--       Guidance: enumerate sub-shapes of this pattern -->

<!-- TODO: Identifying — missing, needs to be written -->
<!--       Guidance: per-variant: recognition checklist + canonical example -->

<!-- TODO: Recognition Checklist — missing, needs to be written -->
<!--       Guidance: 4-question diagnostic — the source of the Problem-section Diagnostic Questions -->

<!-- TODO: Canonical Example — missing, needs to be written -->
<!--       Guidance: fully worked example: brute force → optimised → template fit -->

<!-- TODO: Problems in This Category — missing, needs to be written -->
<!--       Guidance: table with links to the 02-problems/ files -->
