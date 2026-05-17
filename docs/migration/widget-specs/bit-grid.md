# Widget Spec — `bit-grid`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise integers as **binary-cell grids** and animate the seven
canonical bit operations as cell-level transformations:

| Op | Visualised as |
|---|---|
| `set`   | Specific cell flips from 0 to 1 (filled, blue flash) |
| `clear` | Specific cell flips from 1 to 0 (unfilled, fade) |
| `toggle`/`xor-bit` | Specific cell flips its current value |
| `xor`   | Cell-by-cell `a[i] ^ b[i]`; result row appears below |
| `and`/`or` | Cell-by-cell logical combination; result row appears below |
| `not`   | Every cell inverts; result row appears below |
| `shift` | All cells slide left or right by `k` positions; bits falling off / new zero cells fading in |

Two specialised composite modes also live in the catalog:

- `mask` — render a *mask* row above an integer row; intersection cells
  light up to show which bits the mask selects (used by all five
  `pattern-*` chapters).
- `multi-xor` — render N integer rows stacked; animate the chained XOR
  fold; cell-level cancellation (pairs colored amber → fade to grey)
  surfaces the "duplicates vanish" intuition that powers the entire
  pattern-xor chapter, including the classic **XOR-pair-finding**
  example: `arr = [2, 2, 2, 1, 3, 1, 3]` → result `2`.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

Per `diagram-gap-audit.md` Phase 15: source has **0 interactive
diagrams** for bit manipulation. Payloads are authored from destination
prose at `content/cortex/data-structures-and-algorithms/08-bit-tricks/`:

- `01-pattern-kth-bit.md` — set/clear/toggle/check k-th bit
- `02-pattern-set-bit-finder.md` — `i & (i-1)`, popcount loops
- `03-pattern-restructuring.md` — bit reversal, segment masks
- `04-pattern-xor.md` — the XOR-cancels-pairs intuition (POC source)
- `05-pattern-bitmasking.md` — subset enumeration, mask iteration
- `06-pattern-applications.md` — Gray code, bit reversal, parity

## 3. Destination chapter usage

| Chapter | Instances |
|---|---|
| `08-bit-tricks/01-pattern-kth-bit.md` | 4 — set bit k, clear bit k, toggle bit k, check bit k |
| `08-bit-tricks/02-pattern-set-bit-finder.md` | 3 — `n & (n-1)` drops lowest set bit (3-step animation); Kernighan popcount; `n & -n` isolates lowest set bit |
| `08-bit-tricks/03-pattern-restructuring.md` | 3 — left/right shift demo; bit reversal in-place; segment-mask construction |
| `08-bit-tricks/04-pattern-xor.md` | 4 — `Why XOR Cancels` (pair-cancel demo, includes POC-1 below); `Odd-Occurring Element` (the XOR-pair-finding demo, POC-3); `Swap Without Temp` (3-XOR cell mutation); `Missing and Duplicated` (combined-XOR with pivot bit) |
| `08-bit-tricks/05-pattern-bitmasking.md` | 3 — subset iteration `mask = (mask-1) & full`; bitmask-as-set lookup; subset-sum DP teaser |
| `08-bit-tricks/06-pattern-applications.md` | 2 — Gray code n-th value, parity check |

Total at landing: **19 instances** across 6 chapters. The widget is
the workhorse of the bit-tricks section.

## 4. Payload schema sketch

```ts
{
  title:     string,
  bitWidth:  number,                // typically 8, 16, or 32; controls cell-row length
  endianDisplay?: "msb-left" | "lsb-left",  // default "msb-left" (matches how prose writes binary)
  rows:      Array<{
    label:   string,                // "a", "b", "mask", "result", "arr[0]", etc.
    value:   number,                // current value; widget computes per-bit cells
    role?:   "operand" | "mask" | "result" | "intermediate",
  }>,
  steps: Array<{
    op:      "set" | "clear" | "toggle" | "and" | "or" | "xor" | "not" |
             "shift-left" | "shift-right" | "highlight" | "multi-xor-fold" |
             "mask-overlay" | "noop",
    targets: string[],              // row labels the op affects, e.g. ["a"] for set, ["a","b"] for xor
    rows?:   Array<{                // optional post-step row overrides (e.g. new "result" row appearing)
      label: string, value: number, role?: string,
    }>,
    bitIndex?:    number,           // for set/clear/toggle/check on a single bit
    shiftAmount?: number,           // for shift-left/shift-right
    cellHighlights?: Array<{        // per-cell highlights (any row, any bit position)
      rowLabel: string, bitIdx: number,
      kind: "active" | "matched" | "cancelled" | "result-1" | "result-0",
    }>,
    msg:     string,
  }>,
}
```

**Cell rendering.** Each cell is a 28×28 square; filled blue for 1,
empty (light grey outline) for 0. Each row is one horizontal strip of
`bitWidth` cells, prefixed by a `label` text element on the left and
the **decimal value** on the right as a running readout.

**Per-cell highlights.** A `kind` enum drives the per-cell flash colour:

| `kind` | Meaning | Colour |
|---|---|---|
| `active` | The cell being read/written this step | blue `#3b82f6` |
| `matched` | A mask bit aligning with a set operand bit | emerald `#10b981` |
| `cancelled` | A pair of 1s XORing to 0 in this round | amber `#f59e0b` |
| `result-1` | A 1 cell in a freshly-computed result row | emerald `#10b981` |
| `result-0` | A 0 cell in a freshly-computed result row | slate `#64748b` |

**Multi-XOR fold animation.** When `op: "multi-xor-fold"` and the
spec has N≥2 operand rows: the widget renders a running XOR row at
the bottom and progressively folds each operand into it. Cancelled
pairs (positions where both bits are 1, producing 0) flash amber, then
fade. Final surviving 1-bits in the result row stay emerald.

## 5. POC payloads

### POC-1 — XOR pair-cancel demo (the classic `pattern-xor` opener)

`arr = [2, 2, 2, 1, 3, 1, 3]` folded via XOR; pairs `(1,1)` and `(3,3)`
cancel; two of the three `2`s cancel; result is `2`.

````markdown
```d3 widget=bit-grid
{
  "title": "XOR cancels pairs — fold arr = [2, 2, 2, 1, 3, 1, 3] → 2",
  "bitWidth": 4,
  "rows": [
    { "label": "arr[0] = 2", "value": 2, "role": "operand" },
    { "label": "arr[1] = 2", "value": 2, "role": "operand" },
    { "label": "arr[2] = 2", "value": 2, "role": "operand" },
    { "label": "arr[3] = 1", "value": 1, "role": "operand" },
    { "label": "arr[4] = 3", "value": 3, "role": "operand" },
    { "label": "arr[5] = 1", "value": 1, "role": "operand" },
    { "label": "arr[6] = 3", "value": 3, "role": "operand" }
  ],
  "steps": [
    {
      "op": "noop", "targets": [],
      "msg": "Seven values. Three 2s, two 1s, two 3s. The two 1s and two 3s should cancel; one 2 should survive."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[0]", "arr[1]"],
      "rows": [{ "label": "acc", "value": 0, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "arr[0] = 2", "bitIdx": 1, "kind": "cancelled" },
        { "rowLabel": "arr[1] = 2", "bitIdx": 1, "kind": "cancelled" }
      ],
      "msg": "acc ^= arr[0] ^ arr[1] = 2 ^ 2 = 0. Bit 1 of both rows is set — they cancel."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[2]"],
      "rows": [{ "label": "acc", "value": 2, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "arr[2] = 2", "bitIdx": 1, "kind": "result-1" },
        { "rowLabel": "acc", "bitIdx": 1, "kind": "result-1" }
      ],
      "msg": "acc ^= arr[2] = 0 ^ 2 = 2. The third 2 has no partner yet — it appears in acc."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[3]"],
      "rows": [{ "label": "acc", "value": 3, "role": "result" }],
      "msg": "acc ^= arr[3] = 2 ^ 1 = 3 (binary 0011). Bits 0 and 1 both set."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[4]"],
      "rows": [{ "label": "acc", "value": 0, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "acc", "bitIdx": 0, "kind": "cancelled" },
        { "rowLabel": "acc", "bitIdx": 1, "kind": "cancelled" }
      ],
      "msg": "acc ^= arr[4] = 3 ^ 3 = 0. Bits 0 and 1 both cancel."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[5]"],
      "rows": [{ "label": "acc", "value": 1, "role": "result" }],
      "msg": "acc ^= arr[5] = 0 ^ 1 = 1."
    },
    {
      "op": "multi-xor-fold", "targets": ["arr[6]"],
      "rows": [{ "label": "acc", "value": 2, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "acc", "bitIdx": 0, "kind": "cancelled" },
        { "rowLabel": "acc", "bitIdx": 1, "kind": "result-1" }
      ],
      "msg": "acc ^= arr[6] = 1 ^ 3 = 2. Bit 0 cancels (1 ^ 1 = 0); bit 1 survives (0 ^ 1 = 1). Final: 2."
    }
  ]
}
```
````

### POC-2 — Set / clear / toggle k-th bit on `n = 0b00101100 = 44`

````markdown
```d3 widget=bit-grid
{
  "title": "Set, clear, toggle k-th bit",
  "bitWidth": 8,
  "rows": [{ "label": "n", "value": 44, "role": "operand" }],
  "steps": [
    {
      "op": "noop", "targets": [],
      "msg": "Start: n = 44 (binary 00101100). Bits 2, 3, 5 are set."
    },
    {
      "op": "set", "targets": ["n"], "bitIndex": 4,
      "rows": [{ "label": "n", "value": 60, "role": "operand" }],
      "cellHighlights": [{ "rowLabel": "n", "bitIdx": 4, "kind": "active" }],
      "msg": "set(4): n |= (1 << 4). Bit 4 flips 0 → 1. n = 60 (00111100)."
    },
    {
      "op": "clear", "targets": ["n"], "bitIndex": 3,
      "rows": [{ "label": "n", "value": 52, "role": "operand" }],
      "cellHighlights": [{ "rowLabel": "n", "bitIdx": 3, "kind": "active" }],
      "msg": "clear(3): n &= ~(1 << 3). Bit 3 flips 1 → 0. n = 52 (00110100)."
    },
    {
      "op": "toggle", "targets": ["n"], "bitIndex": 5,
      "rows": [{ "label": "n", "value": 20, "role": "operand" }],
      "cellHighlights": [{ "rowLabel": "n", "bitIdx": 5, "kind": "active" }],
      "msg": "toggle(5): n ^= (1 << 5). Bit 5 flips 1 → 0. n = 20 (00010100)."
    }
  ]
}
```
````

### POC-3 — Mask overlay: `n & (1 << k)` checks if k-th bit is set

````markdown
```d3 widget=bit-grid
{
  "title": "Mask overlay — n & (1 << k) reveals bit k",
  "bitWidth": 8,
  "rows": [
    { "label": "n",        "value": 44, "role": "operand" },
    { "label": "1 << 3",   "value": 8,  "role": "mask" }
  ],
  "steps": [
    {
      "op": "mask-overlay", "targets": ["n", "1 << 3"],
      "cellHighlights": [
        { "rowLabel": "1 << 3", "bitIdx": 3, "kind": "active" },
        { "rowLabel": "n",      "bitIdx": 3, "kind": "matched" }
      ],
      "msg": "Mask isolates bit 3. n's bit 3 is set → mask overlays a matched cell."
    },
    {
      "op": "and", "targets": ["n", "1 << 3"],
      "rows": [{ "label": "n & mask", "value": 8, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "n & mask", "bitIdx": 3, "kind": "result-1" }
      ],
      "msg": "n & (1 << 3) = 8 ≠ 0 → bit 3 of n is set. The standard 'check k-th bit' idiom."
    }
  ]
}
```
````

### POC-4 — Shift-left animation `n << 2`

````markdown
```d3 widget=bit-grid
{
  "title": "Left shift n << 2 — cells slide; zeros fill from the right",
  "bitWidth": 8,
  "rows": [{ "label": "n", "value": 13, "role": "operand" }],
  "steps": [
    {
      "op": "noop", "targets": [],
      "msg": "n = 13 (00001101). Bits 0, 2, 3 set."
    },
    {
      "op": "shift-left", "targets": ["n"], "shiftAmount": 2,
      "rows": [{ "label": "n", "value": 52, "role": "operand" }],
      "cellHighlights": [
        { "rowLabel": "n", "bitIdx": 2, "kind": "active" },
        { "rowLabel": "n", "bitIdx": 4, "kind": "active" },
        { "rowLabel": "n", "bitIdx": 5, "kind": "active" }
      ],
      "msg": "n << 2: each set bit moves left by 2 (i.e. multiplied by 4). n = 52 (00110100)."
    }
  ]
}
```
````

### POC-5 — `n & (n - 1)` drops the lowest set bit (3-step animation)

````markdown
```d3 widget=bit-grid
{
  "title": "n & (n-1) — drops the lowest set bit",
  "bitWidth": 8,
  "rows": [
    { "label": "n",     "value": 44, "role": "operand" },
    { "label": "n - 1", "value": 43, "role": "operand" }
  ],
  "steps": [
    {
      "op": "noop", "targets": [],
      "msg": "n = 44 (00101100). Lowest set bit is bit 2."
    },
    {
      "op": "noop", "targets": [],
      "cellHighlights": [
        { "rowLabel": "n",     "bitIdx": 2, "kind": "active" },
        { "rowLabel": "n - 1", "bitIdx": 0, "kind": "active" },
        { "rowLabel": "n - 1", "bitIdx": 1, "kind": "active" }
      ],
      "msg": "n − 1 = 43 (00101011). The lowest set bit of n flipped to 0; all bits below it flipped to 1."
    },
    {
      "op": "and", "targets": ["n", "n - 1"],
      "rows": [{ "label": "n & (n-1)", "value": 40, "role": "result" }],
      "cellHighlights": [
        { "rowLabel": "n & (n-1)", "bitIdx": 3, "kind": "result-1" },
        { "rowLabel": "n & (n-1)", "bitIdx": 5, "kind": "result-1" }
      ],
      "msg": "n & (n-1) = 40 (00101000). The lowest set bit was dropped. Used by Kernighan's popcount: count iterations until n hits 0."
    }
  ]
}
```
````

## 6. Closest existing widget to mimic

- **`array-traversal`** for **the per-row cell-strip rendering** — each
  bit-grid row is structurally an `ArrayTraversal` of `bitWidth`
  binary-valued cells. The cell rect, per-cell highlight class scheme,
  and the per-row label-and-value gutter pattern all transfer.
- **Multi-row stack pattern from `suffix-array-builder`** (when that
  ships) — the same vertical-row + per-row D3 join with smooth row
  entry/exit transitions. If `suffix-array-builder` ships first, lift
  the row-layout helpers; if `bit-grid` ships first, write them here
  and `suffix-array-builder` reuses.

The cell-flash-and-cancel animation (multi-xor fold) is novel — most
existing widgets don't have a "cell pulses, then fades" idiom. Modest
new CSS animation work.

## 7. D3 selections plan

| Layer | Selector | Bound data | Update on step |
|---|---|---|---|
| Row container | `g.bit-grid__row` keyed by `row.label` | one per row | enter: slide in from below; exit: fade out; update: y-position interpolation when new rows insert above |
| Row cells | `g.bit-grid__row` ← `rect.bit-grid__cell` keyed by `bitIdx` | derived from `(value >> bitIdx) & 1` | fill colour transition (white ↔ blue) when bit flips; per-cell class for highlight kind |
| Row label | `text.bit-grid__row-label` per row | `row.label` | static after enter |
| Row value | `text.bit-grid__row-value` per row | decimal of `row.value` | numeric tween via `d3.interpolateNumber` (so reader sees the value rolling between steps) |
| Highlight overlays | `g.bit-grid__highlights` ← `circle.bit-grid__highlight-pulse` keyed by `step + rowLabel + bitIdx` | `step.cellHighlights` | enter: scale 0 → 1.2 → 1 pulse; exit on next step |
| Shift trail | `g.bit-grid__shift-trail` (single layer) | computed from prev/next row + `shiftAmount` | brief 400 ms ghost-trail of cells moving across positions |
| Caption | `div.bit-grid__msg` | `step.msg` | reactive React render |

D3 transitions:
- Cell flip: `fill` interpolated from white (`#f1f5f9`) to blue
  (`#3b82f6`) over 350 ms.
- Highlight pulse: `circle` overlay at cell center, scale animation.
- Shift: cells in target row transition `transform: translate(-cellSize * shiftAmount, 0)` over 500 ms; new zero cells fade in from the right.
- Cancel (multi-xor): amber flash on both cancelled cells for 250 ms
  before they both fade to white together.

## 8. Shared abstractions

| Lift | From / To |
|---|---|
| `Stepper.scala` | Reuse as-is |
| `PayloadDecoder.scala` | Reuse `d.string`, `d.int`, `d.dynList`, `d.stringList`; add `d.intList` (if not already in catalog); add `d.optInt` |
| Per-cell rendering | Copy from `ArrayTraversal`'s cell layer; adapt to fixed 28 px square (smaller than the 56 px in `ArrayTraversal`) |
| Multi-row stack + row keyed join | Either lift from `suffix-array-builder` (if ships first) or author here and contribute back |
| Numeric tween for row value | Lift from `ArrayTraversal`'s text-update pattern; extend with `d3.interpolateNumber` for rolling-digit feel |
| Canon: marker colours | Bit on = blue `#3b82f6`; bit off = pale slate `#f1f5f9` outlined `#cbd5e1`; active = blue pulse; matched = emerald `#10b981`; cancelled = amber `#f59e0b`; result-1 = emerald; result-0 = slate `#64748b` |
| BEM block | New file `client/src/styles/components/bit-grid.css`; modifiers `__row`, `__cell`, `__row-label`, `__row-value`, `__highlight-pulse`, `__shift-trail`, `--bit-on`, `--bit-off`, `--active`, `--matched`, `--cancelled`, `--result` |

## 9. Estimated build session count

**1-2 sessions.**

Session 1 (≈ 5-6 hours): Scaffold the Scala module; port per-cell
rendering and per-row layout from `ArrayTraversal` (with the smaller
cell size); wire ops `set`, `clear`, `toggle`, `and`, `or`, `xor`,
`not` end-to-end; get POC-2 (set/clear/toggle) and POC-3 (mask
overlay) rendering with smooth fill transitions.

Session 2 (≈ 4-5 hours): Add shift animation (POC-4), `n & (n-1)`
composite (POC-5), and the multi-xor-fold mode (POC-1) with cancel
flashes. Write BEM CSS, register in `D3WidgetBlock.scala`, build demo
book chapter housing all five POCs. Run the four-gate verification
(scalafmt, compile, test, vite build) + browser smoke.

Lower-mid complexity — the per-cell rendering is simpler than
`linked-list`'s arrow logic, but the variety of ops + the multi-xor
cancel-flash animation pushes session 2 to ~5 hours. Estimate matches
the `growth-rate-chart` budget in `diagram-gap-audit.md` Tier 2.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/08-bit-tricks/01-bit-grid.md` (NEW —
created alongside the widget build session).

Houses all five POC payloads above, each preceded by the analogous
destination prose (XOR-cancels-pairs intuition, set/clear/toggle
idioms, mask overlay, left-shift semantics, `n & (n-1)` Kernighan
trick). Acts as the living authoring reference. Future schema changes
re-render these five payloads as a smoke test before landing —
particularly important because the bit-tricks chapters are the widget's
heaviest user (19 instances projected) and any regression would
ripple across all of them.
