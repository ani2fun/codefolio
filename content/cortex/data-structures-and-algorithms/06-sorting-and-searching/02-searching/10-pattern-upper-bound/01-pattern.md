---
title: "Pattern: Upper Bound"
summary: "Find the rightmost position where a condition last holds — count elements in a range, find the ceiling, or locate the breaking index."
prereqs:
  - 06-sorting-and-searching/02-searching/03-upper-bound
---

# Identifying the Upper Bound Pattern

| # | Question | If "yes," upper bound fits because... |
|---|---|---|
| **Q1** | Is the data sorted ascending? | Upper bound needs monotone non-decreasing input. |
| **Q2** | Are we looking for the *first index where the element exceeds a threshold*? | Upper bound returns exactly that. |
| **Q3** | Could "no element exceeds" be a possible answer? | Upper bound returns `n` in that case — caller can interpret. |

Common phrasings: "count of elements ≤ X", "first index after target", "first element greater than X", "ceiling of X", "first index satisfying f(i) > threshold."

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

<!-- TODO: Recognition Checklist — missing, needs to be written -->
<!--       Guidance: 4-question diagnostic — the source of the Problem-section Diagnostic Questions -->

<!-- TODO: Canonical Example — missing, needs to be written -->
<!--       Guidance: fully worked example: brute force → optimised → template fit -->

<!-- TODO: Problems in This Category — missing, needs to be written -->
<!--       Guidance: table with links to the 02-problems/ files -->
