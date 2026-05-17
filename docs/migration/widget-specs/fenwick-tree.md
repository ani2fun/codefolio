# Widget Spec — `fenwick-tree`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise a **Binary Indexed Tree (BIT)** as a 1-indexed array of cells with
**parent-index arrows** revealing the implicit forest that the `i & -i`
bit-trick walks. Two animation modes drive the chapter's two operations:

1. **Prefix-sum query** — descend from index `i` toward `0` by stripping the
   lowest set bit (`i -= i & -i`); accumulate `bit[i]` at each stop;
   highlight the descending path.
2. **Point update** — climb from index `i` toward `n` by adding the lowest
   set bit (`i += i & -i`); add `delta` to `bit[i]` at each stop; highlight
   the climbing path.

Every step exposes the **LOWBIT bit-arithmetic** explicitly: an inset row
shows `i`, `-i` (two's-complement), and `i & -i` in binary, so the reader
sees *why* the next jump lands where it does.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

The DSA Lesson Source's bit-manipulation phase has zero interactive
diagrams (per `diagram-gap-audit.md` Phase 15 row), and the orphan
`03-trees/10-fenwick-tree/` chapter has no source counterpart at all.
Payloads are authored from destination prose at
`content/cortex/data-structures-and-algorithms/03-trees/10-fenwick-tree/01-introduction-to-fenwick-trees.md`
(implementation walkthrough lines 105-275, edge cases lines 281-302).

## 3. Destination chapter usage

| Chapter | Sections needing widget instance |
|---|---|
| `03-trees/10-fenwick-tree/01-introduction-to-fenwick-trees.md` | "How the implicit tree works" (one tree-layout instance), "Prefix sum (`sum of A[1..i]`)" (one descend animation for `i=13`), "Point update (`A[i] += delta`)" (one climb animation for `i=5`), "Range sum (`sum of A[l..r]`)" (one `r=6, l-1=2` two-call composition) |
| `03-trees/10-fenwick-tree/` (future operations chapters, when authored) | Build-from-array `O(n)` trick, 2D extension teaser |

Total instances at landing: **4** (introduction chapter only). Headroom
for ~4 more across subsequent chapters in this orphan section.

## 4. Payload schema sketch

```ts
{
  title:    string,                 // header above the widget
  array:    number[],               // underlying A[]; 1-indexed in display
  bit:      number[],               // current bit[] cell values (size n+1; bit[0] = 0 sentinel)
  steps: Array<{
    mode:    "query" | "update" | "build" | "static",
    cursor:  number,                // current i in {1..n} (or 0 to indicate "done")
    path:    number[],              // indices touched so far (for highlight trail)
    lowbit: { i: number, twosComp: string, lowbit: number, binary: string },
                                    // bit-arithmetic inset; e.g. {i:12, twosComp:"11110100",
                                    //   lowbit:4, binary:"00000100"}
    bit?:    number[],              // optional override of the current bit[] snapshot
    accumulator?: number,           // running sum (query mode) or running update progress
    delta?:  number,                // for update mode
    range?:  { lo: number, hi: number },  // for range-sum composition
    msg:     string,                // step caption shown under controls
  }>,
  edges?: Array<{ from: number, to: number, kind: "parent" }>,
                                    // optional pre-computed parent links (i → i + lowbit(i))
}
```

**Parent-edge derivation.** When `edges` is omitted, the widget computes
`parent(i) = i + (i & -i)` for every `i ∈ {1..n}` with `i + lowbit ≤ n` and
draws each edge as an arc that arches above the cell row. Cells without a
parent (`bit[8]`, `bit[16]`, …) are the roots of the implicit forest.

**LOWBIT inset.** The `lowbit` field is mandatory on every step regardless
of `mode`. It renders as a small fixed-width table below the array:

```
i        = 12   = 0000 1100
−i              = 1111 0100
i & −i   =  4   = 0000 0100
```

This is the "explicit LOWBIT bit-arithmetic step" the spec callout
demands.

## 5. POC payloads

### POC-1 — Static topology with all parent arrows for `n = 8`

````markdown
```d3 widget=fenwick-tree
{
  "title": "Implicit forest over bit[1..8]",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "bit":   [0, 1, 3, 3, 10, 5, 11, 7, 36],
  "steps": [
    {
      "mode": "static",
      "cursor": 0,
      "path": [],
      "lowbit": { "i": 0, "twosComp": "—", "lowbit": 0, "binary": "—" },
      "msg": "Each bit[i] covers a power-of-2 slice ending at i. Edges show parent = i + (i & −i)."
    }
  ]
}
```
````

### POC-2 — Prefix sum query `prefixSum(13)` walking `13 → 12 → 8 → 0`

(Built over an array of length 16 so the descent fully demonstrates three
hops with explicit LOWBIT arithmetic at each.)

````markdown
```d3 widget=fenwick-tree
{
  "title": "prefixSum(13) — descend by stripping lowbit",
  "array": [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3],
  "bit":   [0, 3, 4, 4, 9, 5, 14, 2, 24, 5, 8, 5, 18, 9, 16, 9, 80],
  "steps": [
    {
      "mode": "query", "cursor": 13, "path": [13], "accumulator": 0,
      "lowbit": { "i": 13, "twosComp": "1111 0011", "lowbit": 1, "binary": "0000 1101 & 1111 0011 = 0000 0001" },
      "msg": "Start at i=13 (binary 1101). lowbit(13)=1 — peel off the trailing 1."
    },
    {
      "mode": "query", "cursor": 13, "path": [13], "accumulator": 9,
      "lowbit": { "i": 13, "twosComp": "1111 0011", "lowbit": 1, "binary": "0000 1101" },
      "msg": "s += bit[13] = 9. Next: i ← 13 − 1 = 12."
    },
    {
      "mode": "query", "cursor": 12, "path": [13, 12], "accumulator": 9,
      "lowbit": { "i": 12, "twosComp": "1111 0100", "lowbit": 4, "binary": "0000 1100 & 1111 0100 = 0000 0100" },
      "msg": "At i=12 (binary 1100). lowbit(12)=4 — peel off the trailing 100."
    },
    {
      "mode": "query", "cursor": 12, "path": [13, 12], "accumulator": 27,
      "lowbit": { "i": 12, "twosComp": "1111 0100", "lowbit": 4, "binary": "0000 1100" },
      "msg": "s += bit[12] = 18. s = 27. Next: i ← 12 − 4 = 8."
    },
    {
      "mode": "query", "cursor": 8, "path": [13, 12, 8], "accumulator": 27,
      "lowbit": { "i": 8, "twosComp": "1111 1000", "lowbit": 8, "binary": "0000 1000 & 1111 1000 = 0000 1000" },
      "msg": "At i=8 (binary 1000). lowbit(8)=8 — the only set bit IS the value."
    },
    {
      "mode": "query", "cursor": 8, "path": [13, 12, 8], "accumulator": 51,
      "lowbit": { "i": 8, "twosComp": "1111 1000", "lowbit": 8, "binary": "0000 1000" },
      "msg": "s += bit[8] = 24. s = 51. Next: i ← 8 − 8 = 0."
    },
    {
      "mode": "query", "cursor": 0, "path": [13, 12, 8], "accumulator": 51,
      "lowbit": { "i": 0, "twosComp": "0000 0000", "lowbit": 0, "binary": "—" },
      "msg": "i = 0 — loop exits. prefixSum(13) = 51. Three hops in a length-16 array (log₂ 16 = 4 bound)."
    }
  ]
}
```
````

### POC-3 — Point update `update(5, +10)` climbing `5 → 6 → 8 → 16`

````markdown
```d3 widget=fenwick-tree
{
  "title": "update(5, +10) — climb by adding lowbit",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "bit":   [0, 1, 3, 3, 10, 5, 11, 7, 36],
  "steps": [
    {
      "mode": "update", "cursor": 5, "path": [5], "delta": 10,
      "lowbit": { "i": 5, "twosComp": "1111 1011", "lowbit": 1, "binary": "0000 0101 & 1111 1011 = 0000 0001" },
      "msg": "Start at i=5 (binary 101). lowbit(5)=1 — climb +1."
    },
    {
      "mode": "update", "cursor": 5, "path": [5], "delta": 10,
      "bit": [0, 1, 3, 3, 10, 15, 11, 7, 36],
      "lowbit": { "i": 5, "twosComp": "1111 1011", "lowbit": 1, "binary": "0000 0101" },
      "msg": "bit[5] += 10 → 15. Next: i ← 5 + 1 = 6."
    },
    {
      "mode": "update", "cursor": 6, "path": [5, 6], "delta": 10,
      "bit": [0, 1, 3, 3, 10, 15, 11, 7, 36],
      "lowbit": { "i": 6, "twosComp": "1111 1010", "lowbit": 2, "binary": "0000 0110 & 1111 1010 = 0000 0010" },
      "msg": "At i=6 (binary 110). lowbit(6)=2 — climb +2."
    },
    {
      "mode": "update", "cursor": 6, "path": [5, 6], "delta": 10,
      "bit": [0, 1, 3, 3, 10, 15, 21, 7, 36],
      "lowbit": { "i": 6, "twosComp": "1111 1010", "lowbit": 2, "binary": "0000 0110" },
      "msg": "bit[6] += 10 → 21. Next: i ← 6 + 2 = 8."
    },
    {
      "mode": "update", "cursor": 8, "path": [5, 6, 8], "delta": 10,
      "bit": [0, 1, 3, 3, 10, 15, 21, 7, 36],
      "lowbit": { "i": 8, "twosComp": "1111 1000", "lowbit": 8, "binary": "0000 1000 & 1111 1000 = 0000 1000" },
      "msg": "At i=8 (binary 1000). lowbit(8)=8 — climb +8."
    },
    {
      "mode": "update", "cursor": 8, "path": [5, 6, 8], "delta": 10,
      "bit": [0, 1, 3, 3, 10, 15, 21, 7, 46],
      "lowbit": { "i": 8, "twosComp": "1111 1000", "lowbit": 8, "binary": "0000 1000" },
      "msg": "bit[8] += 10 → 46. Next: i ← 8 + 8 = 16 — past n. Loop exits. Three cells touched."
    }
  ]
}
```
````

### POC-4 — Range sum `rangeSum(3, 6) = prefixSum(6) − prefixSum(2)`

(Compact 4-step composition over the standard `[1..8]` array.)

````markdown
```d3 widget=fenwick-tree
{
  "title": "rangeSum(3, 6) = prefixSum(6) − prefixSum(2)",
  "array": [1, 2, 3, 4, 5, 6, 7, 8],
  "bit":   [0, 1, 3, 3, 10, 5, 11, 7, 36],
  "steps": [
    {
      "mode": "query", "cursor": 6, "path": [6], "accumulator": 11,
      "range": { "lo": 3, "hi": 6 },
      "lowbit": { "i": 6, "twosComp": "1111 1010", "lowbit": 2, "binary": "0000 0110" },
      "msg": "prefixSum(6): s += bit[6] = 11. Next: 6 − 2 = 4."
    },
    {
      "mode": "query", "cursor": 4, "path": [6, 4], "accumulator": 21,
      "range": { "lo": 3, "hi": 6 },
      "lowbit": { "i": 4, "twosComp": "1111 1100", "lowbit": 4, "binary": "0000 0100" },
      "msg": "s += bit[4] = 10. s = 21 = sum [1..6]. Next: 4 − 4 = 0 — done."
    },
    {
      "mode": "query", "cursor": 2, "path": [2], "accumulator": 3,
      "range": { "lo": 3, "hi": 6 },
      "lowbit": { "i": 2, "twosComp": "1111 1110", "lowbit": 2, "binary": "0000 0010" },
      "msg": "prefixSum(2): s += bit[2] = 3. s = 3 = sum [1..2]. Next: 2 − 2 = 0 — done."
    },
    {
      "mode": "query", "cursor": 0, "path": [6, 4, 2], "accumulator": 18,
      "range": { "lo": 3, "hi": 6 },
      "lowbit": { "i": 0, "twosComp": "—", "lowbit": 0, "binary": "—" },
      "msg": "rangeSum(3, 6) = 21 − 3 = 18. Verify: 3+4+5+6 = 18. ✓"
    }
  ]
}
```
````

## 6. Closest existing widget to mimic

- **`linked-list`** for the **cell row + arched parent-edge** rendering.
  The parent-arrow layer is mathematically identical to `linked-list`'s
  next-arrow layer — same arrowhead marker, same bezier curve, but the
  source/target are derived from `i + lowbit(i)` rather than authored
  per-step.
- **`array-traversal`** for the **cell grid + path-trail highlight + step
  caption** layout. The cursor visualisation reuses the same triangle
  marker; the path trail reuses the same `.array-traversal__cell--past`
  / `--active` class scheme.

Lift the `Stepper.scala` controller wholesale. The LOWBIT inset is a new
sub-component: a static-width 3-row text grid below the array; only its
text content changes per step.

## 7. D3 selections plan

| Layer | Selector | Bound data | Update on step |
|---|---|---|---|
| Cells | `g.fenwick-tree__cells` ← `g.fenwick-tree__cell` keyed by `i` | `bit[]` entries | text update (smoothly via `.transition().tween`); class toggle for `--cursor`, `--past`, `--touched` |
| Parent edges | `g.fenwick-tree__edges` ← `path.fenwick-tree__edge` keyed by `${from}→${to}` | Derived from `lowbit(i)` of each `i ≤ n` | static after first mount; opacity toggle `--dim` for unused edges |
| Path trail | `g.fenwick-tree__trail` ← `circle.fenwick-tree__trail-dot` keyed by `i` | `step.path` | enter fades in at cell center; exit fades out on reset |
| Cursor marker | `g.fenwick-tree__cursor` ← `polygon` (single) | `step.cursor` | translate transition between cells |
| LOWBIT inset | `g.fenwick-tree__lowbit` ← three `text.lowbit-row` | `step.lowbit` | text update |
| Accumulator badge | `text.fenwick-tree__accumulator` (single) | `step.accumulator` | text update with tween from prev to next |
| Caption | `div.fenwick-tree__msg` (React-controlled) | `step.msg` | reactive render |

D3 transitions:
- Cells touched in update mode: `fill` flashes amber → settles back to base.
- Cursor: `transform: translate(cellX(i), 0)` with `.duration(450)` (matches
  `ArrayTraversal`'s `TransitionDurationMs`).
- LOWBIT inset: pure text swap; no transition (visual would be noisy).

## 8. Shared abstractions

| Lift | From / To |
|---|---|
| `Stepper.scala` | Reuse as-is — same prev/next/play/reset controls |
| `PayloadDecoder.scala` | Reuse `d.int`, `d.string`, `d.stringList`, `d.dynList`; add a small helper for the nested `lowbit` object |
| `viewBoxWidth` / `cellX` / `singleRowHeight` helpers | Copy from `ArrayTraversal`; adjust for the extra 80 px the LOWBIT inset row needs at the bottom |
| Arched parent-edge bezier path | Lift from `LinkedList`'s `pathFor(link)` (the doubly-linked back-edge variant); same control-point heuristic, just inverted apex |
| Canon: marker colours | Match the LinkedList canon — cursor = `#3b82f6` (blue), path-trail = `#10b981` (emerald), touched = `#f59e0b` (amber) |
| BEM block | New file `client/src/styles/components/fenwick-tree.css`; mirror the `linked-list.css` structure (`__cell`, `__edge`, `__cursor`, `__trail-dot`, `__accumulator`, `__lowbit`, `--cursor`, `--past`, `--touched`, `--dim`) |

## 9. Estimated build session count

**1-2 sessions.**

Session 1 (≈ 4-6 hours): scaffold the Scala module, port the cell-row +
arched-edge rendering from a copy of `LinkedList.scala`, wire the four POC
payloads through the dispatcher, get them rendering without transitions.

Session 2 (≈ 2-3 hours): polish the LOWBIT inset, add cell-flash and
cursor-translate transitions, write the BEM CSS, register in
`D3WidgetBlock.scala`, add a demo book chapter under
`content/cortex/dsa-widget-catalog/`. Run the four-gate verification
(scalafmt, compile, test, vite build) + browser smoke.

The widget is mid-complexity: simpler than `linked-list` (no rewiring,
no multi-list, no Floyd cycles) but more involved than `array-traversal`
(parent-edge layer + LOWBIT inset + per-step bit-arithmetic readout). The
estimate matches `binary-tree` and `heap-tree` budgets in
`diagram-gap-audit.md`.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/03-trees/01-fenwick-tree.md` (NEW —
created alongside the widget build session).

Houses all four POC payloads above, each with surrounding prose taken
from the destination chapter's analogous sections (lowbit-trick recap,
implicit-forest framing, query/update/range-sum walkthroughs). Acts as
the living authoring reference for the widget; any future schema change
re-renders these four payloads as a smoke test before landing.
