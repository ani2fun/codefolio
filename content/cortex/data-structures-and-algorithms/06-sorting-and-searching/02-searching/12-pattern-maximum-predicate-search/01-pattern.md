---
title: "Pattern: Maximum Predicate Search"
summary: "Binary search on the answer space — find the largest value for which a monotone feasibility predicate holds."
prereqs:
  - 06-sorting-and-searching/02-searching/10-pattern-upper-bound/01-pattern
---

# Identifying the Pattern

| # | Question | If "yes," the pattern fits because... |
|---|---|---|
| **Q1** | We're optimizing for the *maximum* value satisfying a constraint? | Binary search finds the flip point. |
| **Q2** | The constraint is *monotonic* — if `x` works, then `x − 1` also works? | Required for predicate to have a unique flip. |
| **Q3** | Predicate evaluable in `O(f(n))`? | Total cost: `O(f(n) · log range)`. |

---

## The Template

The *upper* form differs from minimum-predicate-search in two ways:
1. The mid calculation uses `low + (high - low + 1) / 2` to avoid infinite loops when `low` and `high` are adjacent.
2. The successful predicate moves `low = mid` (not `high = mid`); the failure moves `high = mid - 1`.

```python run
def max_predicate_search(low, high, predicate):
    while low < high:
        mid = low + (high - low + 1) // 2          # +1 ensures mid != low when adjacent
        if predicate(mid):                          # mid works → try larger
            low = mid
        else:                                        # mid doesn't work → try smaller
            high = mid - 1
    return low
```

The `+ 1` in the mid calculation is the key fix. Without it, when `low = high - 1` and the predicate is true at `mid = low`, we'd set `low = low` and loop forever.

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
