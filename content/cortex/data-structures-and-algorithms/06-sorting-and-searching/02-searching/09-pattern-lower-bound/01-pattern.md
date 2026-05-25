---
title: "Pattern: Lower Bound"
summary: "Find the leftmost position where a condition first becomes true — insert position, first occurrence, and K-closest-to-target queries."
prereqs:
  - 06-sorting-and-searching/02-searching/02-lower-bound
---

# Identifying the Lower Bound Pattern

Three diagnostic questions:

| # | Question | If "yes," lower bound fits because... |
|---|---|---|
| **Q1** | Is the data sorted in ascending order? | Lower bound needs monotone non-decreasing input. |
| **Q2** | Are we looking for a *position* (first occurrence, insertion point, neighbour)? | Lower bound returns position; that's its native output. |
| **Q3** | Could the target be absent? Or appear multiple times? | Plain binary search returns "any" or "none"; lower bound returns a *consistent* position even on edges. |

If all three hold, lower bound is the right primitive.

---

## Common Disguises

- **"Insert in sorted order"** → lower bound directly returns the insertion position.
- **"First occurrence" or "Last occurrence"** → first = `lower_bound(t)`; last = `lower_bound(t+1) - 1`.
- **"Closest element to target"** → lower bound finds the threshold; check it and the previous element.
- **"K elements closest to target"** → lower bound anchors a sliding window that expands outward.

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
