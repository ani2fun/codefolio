---
title: "Pattern: Minimum Predicate Search"
summary: "Binary search on the answer space — find the smallest value that satisfies a monotone feasibility predicate."
prereqs:
  - 06-sorting-and-searching/02-searching/09-pattern-lower-bound/01-pattern
---

# Identifying the Pattern

Three diagnostic questions:

| # | Question | If "yes," the pattern fits because... |
|---|---|---|
| **Q1** | We're optimizing for the *minimum* value satisfying a constraint? | Binary search finds the flip point. |
| **Q2** | The constraint is *monotonic* — if `x` works, then `x + 1` also works? | Required for the predicate to have a unique flip point. |
| **Q3** | Can we *evaluate* the predicate `P(x)` in `O(f(n))` time? | Each iteration costs `O(f(n))`; total is `O(f(n) · log range)`. |

If all three hold, the pattern applies. The total time is `O(log(range) · f(n))` — much faster than brute-forcing every value.

---

## The Template

```python run
def min_predicate_search(low, high, predicate):
    while low < high:
        mid = low + (high - low) // 2
        if predicate(mid):                    # mid works → try smaller
            high = mid
        else:                                  # mid doesn't work → try larger
            low = mid + 1
    return low                                 # smallest x with predicate(x) == true
```

Identical structure to lower bound on an array — the only difference is the predicate replaces `arr[mid] >= target`. The search space is `[low, high]` numeric range; the answer is `low` after the loop.

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
