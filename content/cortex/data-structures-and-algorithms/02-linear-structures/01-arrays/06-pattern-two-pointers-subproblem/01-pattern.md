---
title: "Pattern: Two Pointers Subproblem"
summary: "Two pointers at the outer level, with an inner two-pointer or linear pass solving a smaller subproblem at each step."
prereqs:
  - 02-linear-structures/01-arrays/05-pattern-two-pointers-reduction/01-pattern
---

# Identifying Two Pointer Subproblem

The two-pointer **subproblem** pattern handles problems too complex for a single two-pointer pass. The trick is to decompose the problem into smaller pieces — each piece solved with two pointers (directly or via reduction) — then stitch the pieces back together. This is the pattern that explains rotations, Three Sum, Four Sum, and the entire k-Sum family.

---

## Understanding the Pattern

### Why Naive Isn't Enough

Some problems do not yield to a single sweep of two pointers from opposite ends. Rotation, Three Sum, and Four Sum all ask for outcomes that span the whole array but cannot be settled in one pairwise pass — the search space is multi-dimensional, or the transformation requires several coordinated sub-steps that share state. A single two-pointer scan can only track two indices and one decisive direction; it has no way to fix one element while exploring pairs in the rest, and no way to coordinate three sequential sub-transformations on disjoint segments.

To make this concrete: rotating an array `[1, 2, 3, 4, 5, 6, 7, 8]` left by `k = 4` produces `[5, 6, 7, 8, 1, 2, 3, 4]`. Two pointers at the ends only know how to swap mirror pairs — they would produce `[8, 7, 6, 5, 4, 3, 2, 1]`, a reversal, not a rotation. The problem is one structural step bigger than what a single two-pointer pass can express.

So the key idea is: when a problem is one structural step bigger than a single two-pointer pass, decompose it into subproblems first, then apply two pointers inside each piece.

### The Core Idea

The pattern asks one question: **can the problem be broken into smaller pieces, where at least one piece is a two-pointer problem you already know how to solve?**

Three concrete decomposition shapes recur:

- **Sequence of transformations** — express the whole as several ordered sub-operations applied to disjoint or overlapping segments (rotation as three reversals).
- **Fix-and-reduce** — lock one or more dimensions of the search space, leaving a lower-dimensional subproblem that two pointers can sweep linearly (Three Sum, Four Sum).
- **Per-element subroutine** — iterate one outer index and run a two-pointer pass over the remaining suffix for each value (Approximate Three Sum's closest-pair scan).

To make this concrete: Three Sum's search space is three-dimensional (`a + b + c = 0`). Fixing `a = arr[i]` collapses it to two dimensions: find `b + c = −arr[i]` in the sorted remainder — exactly Two Sum, an `O(n)` two-pointer sweep. The outer loop runs `n` times, the inner sweep `O(n)`, giving `O(n²)` total — a strict improvement over the `O(n³)` brute force of three nested loops.

The core insight is: every two-pointer subproblem problem reduces to "outer driver picks a subproblem; inner two-pointer solves it in linear time."

### How the Pointers/Window Move

The pattern has two layers of motion. The **outer driver** is whatever picks the next subproblem — it can be a numeric loop counter (Three Sum's `i`), a pair of nested loop counters (Four Sum's `i` and `j`), or a fixed sequence of calls (K Rotations' three `reverse` invocations). The outer driver does not behave like a converging two-pointer; it walks forward, fixes state, and hands the rest to the inner pass.

The **inner two-pointer** is the familiar shape from the direct-application and reduction patterns. Two indices sit at the ends of the current subproblem's window. Each iteration reads `arr[left]` and `arr[right]`, does constant-time work (compare against a target, swap, record a result), and advances one or both pointers inward. The inner pass terminates when `left >= right`, which guarantees `O(n)` work per inner invocation.

Crucially, the inner pass relies on the same decisive-direction property that makes any sorted-array two-pointer scan correct: moving `left` rightward only increases the sum (or moves toward the upper end of the value range), and moving `right` leftward only decreases it. The outer driver must establish this invariant before the inner pass runs — usually by sorting once, before the outer loop starts.

---

## The Generic Algorithm

The pattern follows the same four-step skeleton regardless of which decomposition shape it takes.

1. **Identify the subproblem.** Find a smaller piece of the problem whose shape matches a two-pointer technique you already know (direct application or reduction).
2. **Set up the invariant.** Do whatever preprocessing the inner two-pointer needs — usually `arr.sort()` to establish the decisive-direction property, or fixing the segment boundaries for a reversal-style sub-step.
3. **Drive the outer loop.** Walk the outer index (or fixed sequence of sub-operations). At each step, isolate the inputs for one inner subproblem call.
4. **Run the inner two-pointer.** Solve the subproblem in `O(n)` with the converging pointers, accumulate its result into the overall answer, and continue the outer driver.

If the subproblem itself has nested structure (Four Sum: fix two elements, then run Two Sum), repeat steps 2–4 recursively until the innermost piece is a plain `O(n)` two-pointer sweep.

---

## Complexity Analysis

| | Complexity | Reason |
|---|---|---|
| **Time** | `O(n^d)` where `d` = outer-loop depth + 1 | Each outer loop multiplies by `O(n)`; the inner two-pointer pass is `O(n)`. For one outer loop: `O(n²)` (Three Sum). For two: `O(n³)` (Four Sum). |
| **Space** | `O(1)` working + `O(k)` output | The pointers and counters are constant; only the result list grows. Sort may add `O(log n)` or `O(n)` depending on language. |

For sequence-style decompositions (K Rotations), the complexity collapses to `O(n)` time and `O(1)` space because the outer driver is a fixed-length sequence of inner calls, not a loop over `n` values.

---

## Variants / Taxonomy

The pattern shows up in three recognisable sub-shapes. Each maps to a different outer-driver style.

- **Sequence-of-transformations** — the outer driver is a fixed list of inner calls applied to disjoint or overlapping segments. K Rotations is the canonical example: three sequential `reverse` calls compose into a rotation. Time stays `O(n)` because the outer driver does not loop over the input.
- **Fix-one-and-reduce** — the outer driver loops over `i`, fixing `arr[i]`, and the inner two-pointer solves the resulting `(k − 1)`-sum on the suffix. Three Sum and Approximate Three Sum sit here. Time is `O(n²)`.
- **Fix-two-and-reduce** — two nested outer loops fix `arr[i]` and `arr[j]`; the inner two-pointer solves a Two Sum on `arr[j+1..n-1]`. Four Sum sits here. Time is `O(n³)`. The same construction generalises to `k-Sum` with `k − 2` nested outer loops and a Two Sum core: `O(n^(k−1))`.

The depth of outer-loop nesting determines the exponent in the complexity; the innermost operation is always the same `O(n)` two-pointer sweep.

---

## Recognition Checklist

The pattern fits when **all four** answers are "yes". The first two are the diagnostic questions for the pattern itself; the last two are the standard inner-pass checks that the chosen subproblem actually permits a two-pointer solve.

- Can the problem be decomposed into smaller subproblems — a sequence of sub-operations, a fixed-and-reduce form, or a per-outer-element sweep?
- Does at least one subproblem look like a two-pointer problem (direct application or reduction) you already know?
- Does the inner subproblem have a decisive direction — is the data (or sub-segment) sorted or otherwise monotonic so that pointer moves have a guaranteed effect on the running quantity?
- Is the per-step inner work `O(1)` — a comparison, a swap, a target check — so the inner pass stays `O(n)`?

Common surface signals: "all triplets / quadruplets summing to X," "rotate the array by k," "closest pair / closest triplet to a target," "k-Sum for any k ≥ 3," "transform the array via several reversals or swaps of segments."

---

## The Example: K Rotations LEFT - Rotate Array **`k`** Times Left

**Problem:** Given an array `arr` of size `n` and an integer `k`, rotate the array `k` positions to the left in-place.

```
Input:  arr = [1, 2, 3, 4, 5, 6, 7, 8],  k = 4
Output: arr = [5, 6, 7, 8, 1, 2, 3, 4]
```

> 🖼 Diagram — Rotate an array k=4 times to the left — the first 4 elements wrap around to the end.
```d2
direction: right

before: "Before  (k=4)" {
  grid-columns: 8
  grid-gap: 0
  a0: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a1: "2" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a2: "3" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a3: "4" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a4: "5"
  a5: "6"
  a6: "7"
  a7: "8"
}

after: "After rotating 4 left" {
  grid-columns: 8
  grid-gap: 0
  b0: "5"
  b1: "6"
  b2: "7"
  b3: "8"
  b4: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b5: "2" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b6: "3" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b7: "4" {style.fill: "#fde68a"; style.stroke: "#d97706"}
}

before -> after: "rotate left by k=4"
```

<p align="center"><strong>Rotate an array k=4 times to the left — the first 4 elements wrap around to the end.</strong></p>

---

## Brute Force: Temp Array, O(n) Space

Copy elements at k-shifted indices into a temp array, then copy back:

> 🖼 Diagram — Brute-force rotation using a temporary array — two passes, O(n) extra space.
```d2
direction: right

orig: "Original arr" {
  grid-columns: 8
  grid-gap: 0
  a0: "1"
  a1: "2"
  a2: "3"
  a3: "4"
  a4: "5"
  a5: "6"
  a6: "7"
  a7: "8"
}

temp: "temp:  temp[i] = arr[(i+k) % n]" {
  grid-columns: 8
  grid-gap: 0
  b0: "5"
  b1: "6"
  b2: "7"
  b3: "8"
  b4: "1"
  b5: "2"
  b6: "3"
  b7: "4"
}

copy: "Copy temp → arr" {
  grid-columns: 8
  grid-gap: 0
  c0: "5"
  c1: "6"
  c2: "7"
  c3: "8"
  c4: "1"
  c5: "2"
  c6: "3"
  c7: "4"
}

orig -> temp: "pass 1: fill temp"
temp -> copy: "pass 2: copy back"
```

<p align="center"><strong>Brute-force rotation using a temporary array — two passes, O(n) extra space.</strong></p>

> ▶ Interactive Diagram — Brute-force left-rotate by k=4 using a temporary array — every element copies to `temp[i] = arr[(i + k) mod n]`, then `temp` copies back.
```d3 widget=array-1d
{
  "steps": [
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Init: arr above, temp (empty) below. We will fill temp[i] = arr[(i + 4) mod 8].",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p4",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t0",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=0 → temp[0] = arr[(0+4) mod 8] = arr[4] = 5.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p5",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=1 → temp[1] = arr[5] = 6.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p6",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=2 → temp[2] = arr[6] = 7.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p7",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t3",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=3 → temp[3] = arr[7] = 8.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p0",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t4",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=4 → (4+4) mod 8 wraps to index 0 → temp[4] = arr[0] = 1.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p1",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t5",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=5 → temp[5] = arr[1] = 2.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "·",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p2",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t6",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=6 → temp[6] = arr[2] = 3.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "src",
          "target": "p3",
          "color": "#f59e0b"
        },
        {
          "name": "i",
          "target": "t7",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i=7 → temp[7] = arr[3] = 4. Pass 1 complete.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "p0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p5",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p6",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "p7",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t1",
          "label": "6",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t2",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t3",
          "label": "8",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t4",
          "label": "1",
          "kind": "cell",
          "meta": [],
          "slot": 4,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t5",
          "label": "2",
          "kind": "cell",
          "meta": [],
          "slot": 5,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t6",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 6,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "t7",
          "label": "4",
          "kind": "cell",
          "meta": [],
          "slot": 7,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Pass 2: copy temp back into arr → arr = [5, 6, 7, 8, 1, 2, 3, 4] ✓",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Brute-force left-rotate by k=4 using temp array"
}
```


```python run viz=array viz-root=arr
from typing import List

def k_rotate(arr: List[int], k: int) -> None:
    n = len(arr)
    k = k % n                              # k > n is just (k mod n) rotations.
    temp = [0] * n
    for i in range(n):
        temp[i] = arr[(i + k) % n]         # source index wraps with % n.
    for i in range(n):
        arr[i] = temp[i]


arr = [1, 2, 3, 4, 5, 6, 7, 8]
k_rotate(arr, 4)
print(arr)   # [5, 6, 7, 8, 1, 2, 3, 4]
```

```java run viz=array viz-root=arr
import java.util.Arrays;

public class Main {
    static void kRotate(int[] arr, int k) {
        int n = arr.length;
        k = k % n;
        int[] temp = new int[n];
        for (int i = 0; i < n; i++) temp[i] = arr[(i + k) % n];
        for (int i = 0; i < n; i++) arr[i] = temp[i];
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        kRotate(arr, 4);
        System.out.println(Arrays.toString(arr));
    }
}
```


<details>
<summary><strong>Trace — arr = [1, 2, 3, 4, 5, 6, 7, 8],  k = 4  (brute force)</strong></summary>

```
arr = [1, 2, 3, 4, 5, 6, 7, 8],  n = 8,  k = 4 % 8 = 4

Pass 1 — temp[i] = arr[(i + k) % n]:
  i=0: temp[0] = arr[(0+4)%8] = arr[4] = 5
  i=1: temp[1] = arr[(1+4)%8] = arr[5] = 6
  i=2: temp[2] = arr[(2+4)%8] = arr[6] = 7
  i=3: temp[3] = arr[(3+4)%8] = arr[7] = 8
  i=4: temp[4] = arr[(4+4)%8] = arr[0] = 1  ← wrap-around kicks in here
  i=5: temp[5] = arr[(5+4)%8] = arr[1] = 2
  i=6: temp[6] = arr[(6+4)%8] = arr[2] = 3
  i=7: temp[7] = arr[(7+4)%8] = arr[3] = 4

temp = [5, 6, 7, 8, 1, 2, 3, 4]

Pass 2 — copy temp → arr:
arr  = [5, 6, 7, 8, 1, 2, 3, 4] ✓

Note: The % operator is what handles the wrap-around.
      Without it, indices 4–7 would go out of bounds.
```

</details>

The brute force is correct but doubles memory: an `O(n)` temp array plus the original. The two-pointer subproblem reformulation eliminates the temp entirely.

---

## Key Insight: A Rotation Is Three Reversals

The decomposition that unlocks `O(1)` space is the observation that **a left rotation by `k` equals three in-place reversals** on disjoint then overlapping segments. Each reversal is a textbook two-pointer direct application — converging pointers, swap, repeat — so the whole rotation runs in `O(n)` time and `O(1)` space.

### Why "rotation = 3 in-place reversals"?

At first glance, rotating an array seems like it *must* involve moving elements around — shift the first element to the end, shift the second… that's O(n·k) moves. Or use a temp array and copy. But there's a beautiful shortcut.

**The insight:** A left rotation by `k` positions is *mathematically equivalent* to three in-place reversals of specific segments.

Here's the WHY step by step:

- After a left rotation by `k`, the first `k` elements move to the end, and the last `n-k` elements move to the front.
- Think of the array as two halves: `[LEFT | RIGHT]` where LEFT = first `k` elements, RIGHT = last `n-k` elements.
- The goal is to produce `[RIGHT | LEFT]` — swap those two halves.
- **How do 3 reversals achieve this?**
  1. Reverse LEFT → its internal order flips: `[LEFT_reversed | RIGHT]`
  2. Reverse RIGHT → its internal order flips: `[LEFT_reversed | RIGHT_reversed]`
  3. Reverse the entire array → everything flips, which **un-reverses both halves back to their original relative order** while swapping their positions: `[RIGHT | LEFT]` ✓

**Concrete check with `[1, 2, 3, 4 | 5, 6, 7, 8]`, k=4:**
- Step 1 (reverse LEFT): `[4, 3, 2, 1 | 5, 6, 7, 8]`
- Step 2 (reverse RIGHT): `[4, 3, 2, 1 | 8, 7, 6, 5]`
- Step 3 (reverse all): `[5, 6, 7, 8, 1, 2, 3, 4]` ✓

**What breaks if you skip a step?** If you only reversed the entire array without the two segment reversals first, you'd get `[8, 7, 6, 5, 4, 3, 2, 1]` — reversed, not rotated. The two prep reversals are what ensure each half's *internal order* is preserved after the final full reversal.

This is the subproblem decomposition: one complex O(n) operation (rotation) → three simpler O(n) operations (reversals), each solvable independently.

### Why "in-place reversal is the two-pointer flip"?

Reversing a subarray is the canonical two-pointer direct application you already know:
- Place `left` at the start of the segment, `right` at the end.
- Swap `arr[left]` and `arr[right]`, then move `left` inward and `right` inward.
- Repeat until they meet in the middle.

**Why two pointers?** Because the reversal problem has a natural symmetric structure — the element at position `i` from the left goes to position `i` from the right. Two pointers exploit that symmetry directly: one pointer tracks "the element that needs to go right" and the other tracks "the element that needs to go left." They meet in the middle when all swaps are done.

**What if you didn't use two pointers?** You'd need an extra array to copy the reversed segment into, then copy it back — that's O(n) space per reversal, and we'd be back to the brute-force approach. Two pointers make it O(1) space per reversal.

So the full chain of reasoning is:
> Rotation → 3 reversals → each reversal → two-pointer → O(1) space total

---

## Optimized Solution: Three Reversals

A left rotation by `k` is equivalent to three reversal operations:

1. Reverse the first `k` elements: `arr[0..k-1]`
2. Reverse the remaining `n-k` elements: `arr[k..n-1]`
3. Reverse the entire array: `arr[0..n-1]`

> 🖼 Diagram — Shift k=4 elements to the left by combining three in-place reversal subproblems — each reversal is solved with the two-pointer technique, O(1) space total.
```d2
direction: right

s0: "Original:  [1, 2, 3, 4 | 5, 6, 7, 8]" {
  grid-columns: 8
  grid-gap: 0
  a0: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a1: "2" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a2: "3" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a3: "4" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  a4: "5"
  a5: "6"
  a6: "7"
  a7: "8"
}

s1: "Step 1: Reverse first k=4  →  [4, 3, 2, 1 | 5, 6, 7, 8]" {
  grid-columns: 8
  grid-gap: 0
  b0: "4" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b1: "3" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b2: "2" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b3: "1" {style.fill: "#fde68a"; style.stroke: "#d97706"}
  b4: "5" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  b5: "6" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  b6: "7" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  b7: "8" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
}

s2: "Step 2: Reverse last n-k=4  →  [4, 3, 2, 1 | 8, 7, 6, 5]" {
  grid-columns: 8
  grid-gap: 0
  c0: "4"
  c1: "3"
  c2: "2"
  c3: "1"
  c4: "8" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  c5: "7" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  c6: "6" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  c7: "5" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
}

s3: "Step 3: Reverse entire array  →  [5, 6, 7, 8, 1, 2, 3, 4]" {
  grid-columns: 8
  grid-gap: 0
  d0: "5" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d1: "6" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d2: "7" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d3: "8" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d4: "1" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d5: "2" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d6: "3" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  d7: "4" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
}

s0 -> s1: "reverse arr[0..3]"
s1 -> s2: "reverse arr[4..7]"
s2 -> s3: "reverse arr[0..7]"
```

<p align="center"><strong>Shift k=4 elements to the left by combining three in-place reversal subproblems — each reversal is solved with the two-pointer technique, O(1) space total.</strong></p>

Each reversal is a direct two-pointer application (`left++`, `right--`, swap until they meet). Three subproblems, each O(n), solved in-place with O(1) extra memory.


```python run
class Solution:
    def reverse(self, arr: List[int], start: int, end: int) -> None:
        while start < end:
            arr[start], arr[end] = arr[end], arr[start]
            start += 1
            end -= 1

    def k_rotations(self, arr: List[int], k: int) -> None:
        n = len(arr)

        # Set k to be in the range of [0, n)
        k %= n

        # Reverse the first k elements using two pointer method
        self.reverse(arr, 0, k - 1)

        # Reverse the remaining elements using two pointer method
        self.reverse(arr, k, n - 1)

        # Reverse the entire array using two pointer method
        self.reverse(arr, 0, n - 1)
```

```java run
class Solution {
    public void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }

    public void kRotations(int[] arr, int k) {
        int n = arr.length;

        // Set k to be in the range of [0, n)
        k %= n;

        // Reverse the first k elements using two pointer method
        reverse(arr, 0, k - 1);

        // Reverse the remaining elements using two pointer method
        reverse(arr, k, n - 1);

        // Reverse the entire array using two pointer method
        reverse(arr, 0, n - 1);
    }
}
```


<details>
<summary><strong>Trace — arr = [1, 2, 3, 4, 5, 6, 7, 8],  k = 4  (two-pointer, 3 reversals)</strong></summary>

```
arr = [1, 2, 3, 4, 5, 6, 7, 8],  n = 8,  k = 4 % 8 = 4

━━━ reverse(arr, 0, 3) — flip LEFT segment [0..3] ━━━
  start=0 (1), end=3 (4) │ 0 < 3 → swap │ [4, 2, 3, 1, 5, 6, 7, 8] │ start=1, end=2
  start=1 (2), end=2 (3) │ 1 < 2 → swap │ [4, 3, 2, 1, 5, 6, 7, 8] │ start=2, end=1
  start=2,     end=1     │ 2 > 1 → done

After step 1: [4, 3, 2, 1, 5, 6, 7, 8]

━━━ reverse(arr, 4, 7) — flip RIGHT segment [4..7] ━━━
  start=4 (5), end=7 (8) │ 4 < 7 → swap │ [4, 3, 2, 1, 8, 6, 7, 5] │ start=5, end=6
  start=5 (6), end=6 (7) │ 5 < 6 → swap │ [4, 3, 2, 1, 8, 7, 6, 5] │ start=6, end=5
  start=6,     end=5     │ 6 > 5 → done

After step 2: [4, 3, 2, 1, 8, 7, 6, 5]

━━━ reverse(arr, 0, 7) — flip ENTIRE array [0..7] ━━━
  start=0 (4), end=7 (5) │ 0 < 7 → swap │ [5, 3, 2, 1, 8, 7, 6, 4] │ start=1, end=6
  start=1 (3), end=6 (6) │ 1 < 6 → swap │ [5, 6, 2, 1, 8, 7, 3, 4] │ start=2, end=5
  start=2 (2), end=5 (7) │ 2 < 5 → swap │ [5, 6, 7, 1, 8, 2, 3, 4] │ start=3, end=4
  start=3 (1), end=4 (8) │ 3 < 4 → swap │ [5, 6, 7, 8, 1, 2, 3, 4] │ start=4, end=3
  start=4,     end=3     │ 4 > 3 → done

After step 3: [5, 6, 7, 8, 1, 2, 3, 4] ✓

Total swaps: 2 + 2 + 4 = 8  (all within O(n))
Extra space:  0 — every swap is in-place
```

</details>

---

## Fitting the Template

| Check | Answer for K Rotations |
|---|---|
| **Q1.** Can the problem be decomposed into smaller subproblems? | **Yes** — a left rotation by `k` is the composition of three independent in-place reversals on segments `[0..k-1]`, `[k..n-1]`, and `[0..n-1]`. |
| **Q2.** Can any subproblem be solved with two pointers (directly or via reduction)? | **Yes** — in-place reversal of a segment is the canonical two-pointer direct application: pointers at the ends, swap, advance inward. |
| **Q3.** Does the subproblem have a decisive direction? | **Yes** — each reversal's inner loop advances `start` rightward and `end` leftward monotonically; no pointer ever backtracks. |
| **Q4.** Is the per-step inner work `O(1)`? | **Yes** — each reversal step performs one swap of two array slots, a constant-time operation. |

All four answers are "yes", so the two-pointer subproblem pattern applies. The outer driver is the fixed sequence of three `reverse(arr, lo, hi)` calls; the inner two-pointer is the loop inside `reverse`. Total cost: `O(n)` time, `O(1)` space.

---

## Problems in This Category

| Problem | Subproblems | How two pointers fit |
|---|---|---|
| **K Rotations** | 3 in-place reversals | Direct two-pointer on each reversal |
| **Three Sum** | Fix one element, solve Two Sum on the rest | Reduction: sort + two-pointer for each fixed element |
| **Approximate Three Sum** | Fix one element, find closest pair | Reduction: sort + two-pointer tracking minimum distance |
| **Four Sum** | Fix two elements, solve Two Sum on the rest | Two nested fixed elements + two-pointer inner pass |

Difficulty increases as the nesting depth grows — Four Sum has three nested loops where the innermost is two-pointer.
