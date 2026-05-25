---
title: "Pattern: Binary Search"
summary: "Classic binary search on a fully sorted or recoverable sorted array — find a target, validate, or locate a shared element."
prereqs:
  - 06-sorting-and-searching/02-searching/01-binary-search
---

# Identifying the Binary Search Pattern

Two diagnostic questions decide whether plain binary search applies.

| # | Question | If "yes," binary search fits because... |
|---|---|---|
| **Q1** | Is the data sorted (ascending or descending)? | Binary search needs a monotone sequence to halve the search space. |
| **Q2** | Are we looking up *whether/where* a specific target exists, not transforming it? | Binary search returns position; lookups are its native use case. |

If both are "yes," `O(log n)` per query is the best you can do.

---

## Common Disguises

Binary search problems often *look* like nested loops or hash lookups at first. Watch for:

- **"Sorted" + "search for"** — direct application.
- **"Sorted in descending order"** — reverse the comparison; algorithm is otherwise identical.
- **"Each row is sorted"** — binary search per row turns `O(n)` into `O(log n)` per row.
- **"Find common elements"** — iterate one collection, binary-search each in the other.
- **Multi-key lookup** — binary search on the key field; verify other fields after.

The trade-off: binary search needs sorted input. If the data isn't sorted but is queried many times, sorting once (`O(n log n)`) and then binary-searching (`O(log n)` per query) beats repeated linear scans (`O(n)` per query) after roughly `log n` queries.

---

## Strategy

When you see a search-style problem on sorted data:
1. Identify the sort axis.
2. Identify the target.
3. Apply binary search (or its variants) with the right comparison.

The four worked problems below show this strategy applied in different settings: a single sorted array (recovery), a descending array (reverse search), a sorted-row matrix with column-wise checks (shared element), and the same with multi-result extraction (intersection).

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
