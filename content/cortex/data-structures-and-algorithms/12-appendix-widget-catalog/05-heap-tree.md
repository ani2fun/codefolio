---
title: heap-tree
summary: Heap animations across max / min / custom-comparator modes via a dual view — the binary tree on top, the contiguous backing array below. Both panels keyed by item.id so a swap visibly slides the same DOM elements between positions. Per-step markers attach to both views; pending-swap arcs bow above the soon-to-be-swapped pair; optional compare panel, input stream, and output strip cover sift / build / top-k / extract / comparator patterns.
prereqs: []
---

# `heap-tree`

## Purpose

A single widget covers source Phase 8 (Heap — 20 source diagrams across 5 chapters) by rendering the **dual representation** the source teaches: the tree view (the binary tree with the heap-order invariant) on top, the array view (the contiguous backing array with index labels) below. Both panels are keyed by `item.id` so a swap visibly slides the same DOM elements between positions — the reader can see, in one motion, both "the parent and child exchange in the tree" and "the values at the two array indices swap in place".

Three modes share one renderer through a `mode` flag:

- `mode: "max"`    — max-heap (largest at root). The mode badge tints red; the compare-result panel reads the source's `parent ≥ child` invariant.
- `mode: "min"`    — min-heap (smallest at root). The badge tints blue; the invariant reads `parent ≤ child`.
- `mode: "custom"` — comparator pattern (Phase 8 chapter 4). The badge tints violet and a sibling `comparatorLabel` text renders next to it. The per-item optional `sortKey` field renders as a small `key=…` badge above each tree node so the reader can compare by the sort dimension rather than the display value.

Per-step `itemsOverride` is a **full replacement** of the items array (not sparse — heap operations restate the entire heap-state per pedagogical frame, and a sparse override would re-introduce the "is this slot empty or did I just forget to mention it" ambiguity the source explicitly avoids). A step that wants the prior step's items omits `itemsOverride` entirely.

Per-step `markers` highlight specific indices in BOTH views — a `current` marker at index 2 paints the tree node at index 2 AND the array cell at index 2 with the same colour. Marker names come from the closed canon (`current`, `parent`, `left`, `right`, `swap`, `root`, `last`, `i`, `j`, `kth`, `top`); author-supplied `color` fields are silently dropped.

Per-step `pendingSwap` overlays a dashed amber arc between two indices in both views — the canonical "this swap will happen on the next step" cue before the items array actually mutates. The arc bows upward in both the tree and the array view so the reader's eye can connect the two endpoints across panels.

Optional per-step decorators:

- `compareResult` — one-line badge at the top: `"18 > 8 → true"` (green on true, rose on false). Used during sift-up / sift-down narration.
- `inputArray` — horizontal stream strip above the heap with a downward triangle cursor at the active value. Top-k streaming patterns put the unconsumed stream here.
- `outputItems` — horizontal output strip below the array, accumulating a top-k result step by step. Each new value enters as a fresh cell.

Optional top-level `view` toggle: `"both"` (default; tree + array stacked), `"tree"` (heap-shape-only demo), `"array"` (indexing-only demo).

> **Source spec**: `docs/migration/widget-specs/heap-tree.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/HeapTree.scala`
>
> **Tree-index math**: index `i` sits at level `floor(log2(i+1))`, at within-level position `i - (2^level - 1)`. Parent of `i` is `(i-1)/2`; children at `2i+1` and `2i+2`. The tree's horizontal footprint reserves slots for the full binary tree at the deepest level the spec ever reaches, so a shrink (extract-top) lifts the missing leaf rather than re-flowing siblings.

## Payload schema (reference card)

```ts
{
  title:             string,
  mode:              "max" | "min" | "custom",
  comparatorLabel?:  string,                          // "custom" mode only
  view?:             "both" | "tree" | "array",       // default "both"
  items: [{ id: string, value: string, sortKey?: string }],
  steps: [{
    // FULL replacement; omit to keep the prior step's items.
    itemsOverride?: [{ id: string, value: string, sortKey?: string }],
    // Per-step index markers. Same index can carry multiple markers — they
    // stack vertically.
    markers?:       [{ name: string, index: number }],
    // Pending swap — dashed amber arc above both panels.
    pendingSwap?:   { from: number, to: number },
    // Top compare-result badge: "<left> <op> <right> → true|false".
    compareResult?: { left: string, right: string, op: string, result: boolean },
    // Above-the-array input stream + cursor.
    inputArray?:    { items: string[], cursor?: number, label?: string },
    // Below-the-array result strip (k-largest / k-smallest accumulator).
    outputItems?:   string[],
    msg:            string
  }]
}
```

**Required**: `title`, `mode`, `steps` (non-empty), `steps[].msg`.
**Optional**: `comparatorLabel`, `view`, `items` (defaults to empty), every per-step field except `msg`.

**Marker name rule**: only canonical names render in colour. Unknown names render with the canon's warning colour and a `⚠` prefix on the label, and log a console warning naming the full vocabulary. Heap-specific names live in `MarkerCanon.scala`: `root` (blue, alias of `head`), `last` (slate, alias of `tail`), `swap` (rose, alias of `fast`), `kth` (violet, alias of `next`). Shared names that read naturally on a heap: `current` (emerald), `parent` (slate), `left`/`right` (blue/rose), `top` (emerald), `i`/`j` (blue/amber).

**Items-override rule**: a step's `itemsOverride` replaces the WHOLE items array. To express "no change", omit the field. To express "the heap shrunk by one", list one fewer item. To express "the heap grew by one", list one more.

**Mode-specific decorators**: `comparatorLabel` only renders when `mode == "custom"`. `sortKey` on items only renders as a badge in `mode == "custom"`. Mixed-mode payloads (e.g. a `sortKey` on a max-mode item) are silently ignored.

**View-suppression**: `view: "tree"` removes the array panel from the canvas (cells + index labels + array markers + array pending-swap arc). `view: "array"` removes the tree panel (nodes + edges + tree markers + tree pending-swap arc). Compare / input / output bands render regardless.

## Representative payloads

### Payload 1 — minimum (empty heap, peek shows null)

The smallest meaningful payload — an empty max-heap; `peek()` returns null. Exercises the renderer's empty-state path: no nodes, no edges, no array cells, just a caption. Confirms the canvas renders without errors when both panels are empty.

```d3 widget=heap-tree
{
  "title": "Empty heap — peek returns null",
  "mode": "max",
  "items": [],
  "steps": [
    {"msg": "Heap is empty. peek() = null; extractTop() throws / returns null."}
  ]
}
```

### Payload 2 — typical (insert with sift-up; pending-swap arcs + compare badge)

Insert 18 into a max-heap of [15, 10, 8, 7, 5, 3]. Walks the canonical sift-up trajectory: append at the end → compare with parent → pending-swap arc → commit swap (the same `n18` item slides from index 6 to index 2 in BOTH panels) → compare again with the new parent → swap again → terminate at the root.

Exercises:

- `itemsOverride` mutating the items list per step (append, then position swap)
- `compareResult` badge alternating green (`>` holds → swap) across sift-up rounds
- `pendingSwap` arc bowing upward between two indices in both views
- `markers: current` and `markers: parent` co-residing in a single step (multi-marker per step)
- The id-keyed swap-slide: the `n18` node visibly travels through three positions in the tree without flickering

```d3 widget=heap-tree
{
  "title": "Insert 18 into the max heap — sift up to maintain the invariant",
  "mode": "max",
  "items": [
    {"id": "n15", "value": "15"},
    {"id": "n10", "value": "10"},
    {"id": "n8",  "value": "8"},
    {"id": "n7",  "value": "7"},
    {"id": "n5",  "value": "5"},
    {"id": "n3",  "value": "3"}
  ],
  "steps": [
    {
      "itemsOverride": [
        {"id": "n15", "value": "15"}, {"id": "n10", "value": "10"},
        {"id": "n8",  "value": "8"},  {"id": "n7",  "value": "7"},
        {"id": "n5",  "value": "5"},  {"id": "n3",  "value": "3"},
        {"id": "n18", "value": "18"}
      ],
      "markers": [{"name": "current", "index": 6}],
      "msg": "Append 18 at index 6 (next free leaf). Heap invariant likely broken."
    },
    {
      "markers": [{"name": "current", "index": 6}, {"name": "parent", "index": 2}],
      "compareResult": {"left": "18", "right": "8", "op": ">", "result": true},
      "msg": "Compare child 18 vs parent (index 2 = 8). 18 > 8 → swap."
    },
    {
      "markers": [{"name": "current", "index": 6}, {"name": "parent", "index": 2}],
      "pendingSwap": {"from": 6, "to": 2},
      "msg": "Pending swap: indices 6 ↔ 2."
    },
    {
      "itemsOverride": [
        {"id": "n15", "value": "15"}, {"id": "n10", "value": "10"},
        {"id": "n18", "value": "18"}, {"id": "n7",  "value": "7"},
        {"id": "n5",  "value": "5"},  {"id": "n3",  "value": "3"},
        {"id": "n8",  "value": "8"}
      ],
      "markers": [{"name": "current", "index": 2}, {"name": "parent", "index": 0}],
      "compareResult": {"left": "18", "right": "15", "op": ">", "result": true},
      "msg": "After swap: 18 at index 2. Compare with root (15). 18 > 15 → swap again."
    },
    {
      "itemsOverride": [
        {"id": "n18", "value": "18"}, {"id": "n10", "value": "10"},
        {"id": "n15", "value": "15"}, {"id": "n7",  "value": "7"},
        {"id": "n5",  "value": "5"},  {"id": "n3",  "value": "3"},
        {"id": "n8",  "value": "8"}
      ],
      "markers": [{"name": "current", "index": 0}, {"name": "root", "index": 0}],
      "msg": "Swapped. 18 is now at the root (index 0). No parent — sift-up terminates."
    }
  ]
}
```

### Payload 3 — large-N (build heap bottom-up; the `i` cursor walks back from the last non-leaf)

The canonical heapify walkthrough: convert `[3, 5, 8, 7, 10, 15]` into a max-heap by sweeping `i` from the last non-leaf (`(n/2)-1 = 2`) back to the root. Each `i` runs a local sift-down using the larger of its two children. Demonstrates the `i` cursor marker walking right-to-left across the array, the `left`/`right` markers tagging the current children, and one nested sift-down where the parent's first sift triggers a second sift on the now-displaced child.

Exercises:

- The longest payload in this chapter (8 steps) — verifies the stepper doesn't drop frames at higher counts
- Three different child-comparison patterns (left-only at index 2, both children at index 1, both children at index 0 → recursive sift)
- A recursive sift-down at step 7 — after swapping into index 2, the displaced original value sifts down further to its leaf

```d3 widget=heap-tree
{
  "title": "Build a max heap bottom-up from [3, 5, 8, 7, 10, 15] — heapify from last non-leaf",
  "mode": "max",
  "items": [
    {"id": "a", "value": "3"},
    {"id": "b", "value": "5"},
    {"id": "c", "value": "8"},
    {"id": "d", "value": "7"},
    {"id": "e", "value": "10"},
    {"id": "f", "value": "15"}
  ],
  "steps": [
    {
      "markers": [{"name": "i", "index": 2}],
      "msg": "Last non-leaf index = (n/2) - 1 = 2. Start heapifying from index 2."
    },
    {
      "markers": [{"name": "i", "index": 2}, {"name": "left", "index": 5}],
      "compareResult": {"left": "15", "right": "8", "op": ">", "result": true},
      "msg": "Index 2 = 8; left child (index 5) = 15. 15 > 8 → swap."
    },
    {
      "itemsOverride": [
        {"id": "a", "value": "3"}, {"id": "b", "value": "5"},
        {"id": "f", "value": "15"},{"id": "d", "value": "7"},
        {"id": "e", "value": "10"},{"id": "c", "value": "8"}
      ],
      "markers": [{"name": "i", "index": 1}],
      "msg": "Index 2 now holds 15. Move i back to index 1."
    },
    {
      "markers": [
        {"name": "i", "index": 1},
        {"name": "left", "index": 3}, {"name": "right", "index": 4}
      ],
      "compareResult": {"left": "10", "right": "5", "op": ">", "result": true},
      "msg": "Index 1 = 5. Larger child = index 4 (10). 10 > 5 → swap."
    },
    {
      "itemsOverride": [
        {"id": "a", "value": "3"}, {"id": "e", "value": "10"},
        {"id": "f", "value": "15"},{"id": "d", "value": "7"},
        {"id": "b", "value": "5"}, {"id": "c", "value": "8"}
      ],
      "markers": [{"name": "i", "index": 0}, {"name": "root", "index": 0}],
      "msg": "Done at index 1. Move i back to the root (index 0)."
    },
    {
      "markers": [
        {"name": "i", "index": 0}, {"name": "root", "index": 0},
        {"name": "left", "index": 1}, {"name": "right", "index": 2}
      ],
      "compareResult": {"left": "15", "right": "3", "op": ">", "result": true},
      "msg": "Root = 3. Larger child = index 2 (15). 15 > 3 → swap, then recurse on the sub-tree."
    },
    {
      "itemsOverride": [
        {"id": "f", "value": "15"},{"id": "e", "value": "10"},
        {"id": "a", "value": "3"}, {"id": "d", "value": "7"},
        {"id": "b", "value": "5"}, {"id": "c", "value": "8"}
      ],
      "markers": [{"name": "current", "index": 2}, {"name": "left", "index": 5}],
      "compareResult": {"left": "8", "right": "3", "op": ">", "result": true},
      "msg": "Recurse: index 2 = 3 (sifted down). Compare with child 8 → swap."
    },
    {
      "itemsOverride": [
        {"id": "f", "value": "15"},{"id": "e", "value": "10"},
        {"id": "c", "value": "8"}, {"id": "d", "value": "7"},
        {"id": "b", "value": "5"}, {"id": "a", "value": "3"}
      ],
      "markers": [{"name": "root", "index": 0}],
      "msg": "Final max heap: [15, 10, 8, 7, 5, 3]. Tree shape: root 15 with children 10 + 8."
    }
  ]
}
```

### Payload 4 — edge (extract-top; the heap shrinks and the tree's last leaf vanishes)

Extract from a 7-node max-heap: save the root (18), swap it with the last leaf (3), shrink to size 6, then sift the swapped value down. Exercises the **shrink** behaviour — between step 2 and step 3 the items array loses its tail entry; the renderer keeps the tree-layout footprint constant so the bottom-right leaf slot visibly empties rather than every leaf re-spacing.

Also exercises:

- The `top` marker (the root being extracted) co-residing with `last` (the last-cell tracker) in one step
- A sift-down comparison badge that flips `result: false` (the second sift-down stops at a leaf, because no child > 3 at that level)
- The pending-swap arc spanning the longest possible distance in the tree (root ↔ last leaf, the canonical extract-top swap)

```d3 widget=heap-tree
{
  "title": "extractTop on a max heap — swap root with last, shrink, sift down",
  "mode": "max",
  "items": [
    {"id": "a", "value": "18"},
    {"id": "b", "value": "15"},
    {"id": "c", "value": "10"},
    {"id": "d", "value": "8"},
    {"id": "e", "value": "7"},
    {"id": "f", "value": "5"},
    {"id": "g", "value": "3"}
  ],
  "steps": [
    {
      "markers": [{"name": "top", "index": 0}, {"name": "last", "index": 6}],
      "msg": "extractTop → save root value (18). Will swap with the last leaf (3)."
    },
    {
      "markers": [{"name": "top", "index": 0}, {"name": "last", "index": 6}],
      "pendingSwap": {"from": 0, "to": 6},
      "msg": "Pending swap: root ↔ last."
    },
    {
      "itemsOverride": [
        {"id": "g", "value": "3"}, {"id": "b", "value": "15"},
        {"id": "c", "value": "10"},{"id": "d", "value": "8"},
        {"id": "e", "value": "7"}, {"id": "f", "value": "5"},
        {"id": "a", "value": "18"}
      ],
      "markers": [{"name": "current", "index": 0}, {"name": "last", "index": 6}],
      "msg": "After swap: 3 at root; 18 at index 6 (about to be removed). 18 is the extract result."
    },
    {
      "itemsOverride": [
        {"id": "g", "value": "3"}, {"id": "b", "value": "15"},
        {"id": "c", "value": "10"},{"id": "d", "value": "8"},
        {"id": "e", "value": "7"}, {"id": "f", "value": "5"}
      ],
      "markers": [
        {"name": "current", "index": 0},
        {"name": "left", "index": 1}, {"name": "right", "index": 2}
      ],
      "compareResult": {"left": "15", "right": "10", "op": ">", "result": true},
      "msg": "Shrunk to size 6. Larger child of root = 15 (index 1). 3 < 15 → sift down."
    },
    {
      "itemsOverride": [
        {"id": "b", "value": "15"},{"id": "g", "value": "3"},
        {"id": "c", "value": "10"},{"id": "d", "value": "8"},
        {"id": "e", "value": "7"}, {"id": "f", "value": "5"}
      ],
      "markers": [
        {"name": "current", "index": 1},
        {"name": "left", "index": 3}, {"name": "right", "index": 4}
      ],
      "compareResult": {"left": "8", "right": "7", "op": ">", "result": true},
      "msg": "At index 1: 3. Larger child = 8 (index 3). 3 < 8 → sift down again."
    },
    {
      "itemsOverride": [
        {"id": "b", "value": "15"},{"id": "d", "value": "8"},
        {"id": "c", "value": "10"},{"id": "g", "value": "3"},
        {"id": "e", "value": "7"}, {"id": "f", "value": "5"}
      ],
      "markers": [{"name": "current", "index": 3}],
      "msg": "At index 3: 3 is a leaf. Sift-down terminates. Heap = [15, 8, 10, 3, 7, 5]. Returned 18."
    }
  ]
}
```

### Payload 5 — mode exercise (k-largest with min-heap; input stream + output strip)

Top-k pattern from Phase 8 chapter 3 — find the 3 largest values in the stream `[4, 1, 7, 3, 9, 2, 6]` using a **min-heap of size 3**. The heap's smallest sits at the root; each new value either pushes (when size < 3) or compares against the root and conditionally replaces it. Exercises the `mode: "min"` badge tint (blue), the `inputArray` stream strip + cursor (advances left-to-right through 7 values), the `outputItems` final result strip (only present on the last step, showing the 3 retained values in heap order), and the `kth` marker pinning the would-be k-th-largest at the root through the streaming process.

This is the canonical "min-heap is the right shape for k-largest" pedagogy — the reader sees the heap stay tiny (always ≤ 3 nodes) while a 7-value stream flows through it.

```d3 widget=heap-tree
{
  "title": "Stream: find 3 largest of [4, 1, 7, 3, 9, 2, 6] using a min-heap of size 3",
  "mode": "min",
  "items": [],
  "steps": [
    {
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 0, "label": "stream"},
      "msg": "Start with an empty min-heap of capacity 3. Stream value 4 → push (heap.size < 3)."
    },
    {
      "itemsOverride": [{"id": "v4", "value": "4"}],
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 1, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "msg": "Heap = [4]. Stream value 1 → push (size < 3)."
    },
    {
      "itemsOverride": [
        {"id": "v1", "value": "1"}, {"id": "v4", "value": "4"}
      ],
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 2, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "msg": "Heap = [1, 4]. Stream value 7 → push (size < 3)."
    },
    {
      "itemsOverride": [
        {"id": "v1", "value": "1"}, {"id": "v4", "value": "4"}, {"id": "v7", "value": "7"}
      ],
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 3, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "compareResult": {"left": "3", "right": "1", "op": ">", "result": true},
      "msg": "Heap full ([1, 4, 7]). Stream 3 → compare with top (1). 3 > 1 → replace top + sift down."
    },
    {
      "itemsOverride": [
        {"id": "v3", "value": "3"}, {"id": "v4", "value": "4"}, {"id": "v7", "value": "7"}
      ],
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 4, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "compareResult": {"left": "9", "right": "3", "op": ">", "result": true},
      "msg": "Heap = [3, 4, 7]. Stream 9 → 9 > 3 → replace top + sift down."
    },
    {
      "itemsOverride": [
        {"id": "v4", "value": "4"}, {"id": "v9", "value": "9"}, {"id": "v7", "value": "7"}
      ],
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 5, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "compareResult": {"left": "2", "right": "4", "op": ">", "result": false},
      "msg": "Heap = [4, 9, 7]. Stream 2 → 2 < 4 (top) → skip."
    },
    {
      "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 6, "label": "stream"},
      "markers": [{"name": "kth", "index": 0}],
      "compareResult": {"left": "6", "right": "4", "op": ">", "result": true},
      "msg": "Stream 6 → 6 > 4 (top) → replace top + sift down."
    },
    {
      "itemsOverride": [
        {"id": "v6", "value": "6"}, {"id": "v9", "value": "9"}, {"id": "v7", "value": "7"}
      ],
      "outputItems": ["6", "9", "7"],
      "markers": [{"name": "kth", "index": 0}],
      "msg": "End of stream. The 3 largest are: 6, 7, 9 (heap order, smallest first)."
    }
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload for this widget, target **5–10 widget steps** (the source diagrams in the widget spec average 13–35 frames each → 5–10 widget steps). Compression strategy:

- **Setup frames** (problem statement, initial heap fades in node by node) → one step with the full `items` baseline established and a caption naming the operation that's about to happen. Skip the per-node fade-in narration — the renderer already fades nodes on first paint.
- **Sift-up / sift-down compare frames** (a single "left vs right" comparison) → one widget step with `compareResult` set and `markers` tagging the participating indices. The arithmetic is in the `op` + `result` rather than the `msg`, so the message can be one short sentence.
- **Pending-swap frame** (source draws an arrow before committing) → one step with `pendingSwap` set; the next step commits via `itemsOverride`. Two steps total for the swap pair (visibility → commit). For a tight payload you can fold the pending arc into the comparison step.
- **The actual swap commit** → one step with `itemsOverride` reflecting the new positions. The id-keyed renderer slides the same DOM elements between the old and new positions in both panels.
- **Build-heap sweeps** → one step per `i` value that the sweep visits, plus extra steps for any recursive sift-down. Don't combine consecutive `i` indices into one step — each is a self-contained sub-operation.
- **Extract-top + shrink** → two steps minimum (swap-root-with-last, then shrunk-and-sift), more if the sift-down is multi-level. Confirm the shrink by reducing the `itemsOverride` list size.
- **Top-k streaming** → one step per stream value, regardless of whether the value gets pushed, replaced, or skipped. Advance `inputArray.cursor`; the `compareResult` (when present) tells the reader why each value's fate was decided. Final step adds `outputItems`.
- **Comparator-mode comparisons** → use the `sortKey` field on each item so the badge above each tree node renders the comparator's projection. The compare-result panel reads the projection's comparison, not the display value.

Example: the source `extractTop on a max heap` walkthrough (35 frames) compresses to 6 widget steps — one per semantic boundary (announce, pending-swap, swap-and-shrink, sift-down-round-1, sift-down-round-2, terminate-at-leaf). Payload 4 above is exactly this compression.

For **build-heap bottom-up** (43 frames in the source), compress to 8 widget steps — start, sift-at-index-2, advance-to-1, sift-at-index-1-with-both-children, advance-to-0, sift-at-root-with-both-children, recursive-sift-of-displaced-value, terminate. Payload 3 follows this pattern.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-heap-tree` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm Payload 1 renders the empty-state — caption shows the peek-returns-null message, no SVG nodes/edges/cells inside the frame.
3. Confirm Payload 2's `n18` node visibly slides from the bottom-right of the tree (index 6) up to the index-2 position, then to the root (index 0), across steps 1 → 3 → 5. The array cells slide in lockstep. Compare-result badges read `18 > 8 → true` then `18 > 15 → true`. The pending-swap arc bows upward in both views at step 3.
4. Confirm Payload 3's `i` marker walks right-to-left across indices 2 → 1 → 0 with the cursor visibly relocating in both panels. At step 7 a recursive sift-down kicks in: after the root swap, the displaced value (originally at index 0) continues sifting down to a leaf.
5. Confirm Payload 4's shrink: between steps 2 (size 7) and 3 (size 6), the bottom-right leaf of the tree vanishes WITHOUT every other leaf reshuffling — the layout footprint is reserved for the original 7-leaf shape. The pending-swap arc at step 1 spans the full tree height.
6. Confirm Payload 5's mode badge tints blue (min-heap); the stream cursor advances left-to-right across 7 values; the heap never exceeds 3 nodes; the `kth` marker stays pinned at index 0 throughout; the output strip appears only on the final step with `[6, 9, 7]`.
7. Confirm no `.d3-widget__error` divs render in the page.
8. Confirm devtools console is clean: no widget exceptions; no `heap-tree:` marker-canon warnings on these payloads. Markers used: `current`, `parent`, `root`, `last`, `top`, `left`, `right`, `i`, `kth` — all canonical.

If any payload fails, fix and re-verify before committing.
