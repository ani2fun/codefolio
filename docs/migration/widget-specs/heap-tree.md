# Widget Spec — `heap-tree`

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `heap-tree` and Arc 3 chapter payload writing.

## 1. Purpose

Animate heap operations across the dual representation the source teaches:
the **tree view** (a binary tree with the heap-order invariant rendered
above) and the **array view** (the contiguous backing array, with index
labels and parent/child arrow overlays). One widget renders both panels
side by side and keeps them synchronised step-to-step. Each step
illustrates one or more of: **sift up** (after insert), **sift down**
(after extract or delete), **build heap** top-down (insert-N-times), **build
heap** bottom-up (heapify from last non-leaf back to root), **comparator-
ordered** operations (min vs max vs custom-key), **extract top** (swap
root with last, shrink, sift down), and **k-largest / k-smallest**
streaming patterns.

## 2. Source-diagram inventory

Source: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/9.heap/`
(5 chapter dirs, 20 interactive diagrams).

### Introduction (`mode:"max"` for narrative)

- `01-introduction-to-heaps/01-understanding-the-problem.md:7` —
  "Patients assigned priorities to see doctors" (13 frames → 6 steps)
- `01-introduction-to-heaps/01-understanding-the-problem.md:43` —
  "Priority data added at head of linked list" (6 frames → 4 steps)
- `01-introduction-to-heaps/01-understanding-the-problem.md:59` —
  "Traverse linked list to find highest priority" (10 frames → 5 steps)
- `01-introduction-to-heaps/02-exploring-a-possible-solution.md:21` —
  "Patients with priorities using a priority queue" (16 frames → 7 steps)

### Array-backed heap operations

- `02-array-implementation-of-heaps/02-inserting-an-item-in-the-heap.md:13`
  — "Insert 18 into max heap" (27 frames → 9 steps) **— sift up**
- `02-array-implementation-of-heaps/03-deleting-an-item-from-the-heap.md:13`
  — "Delete 15 from heap" (20 frames → 8 steps) **— delete + sift down**
- `02-array-implementation-of-heaps/04-peeking-the-top-item-in-the-heap.md:9`
  — "Get max value" (3 frames → 2 steps) **— peek (no mutation)**
- `02-array-implementation-of-heaps/05-extracting-the-top-item-from-the-heap.md:9`
  — "Extract top from max heap" (35 frames → 10 steps) **— extract + sift down**
- `02-array-implementation-of-heaps/06-constructing-a-heap.md:17` —
  "Convert sequence to max heap (top-down, from leaves)" (55 frames → 10 steps)
- `02-array-implementation-of-heaps/06-constructing-a-heap.md:131` —
  "Convert sequence to max heap (bottom-up, from non-leaves)" (43 frames → 10 steps)

### Top-K patterns

- `03-pattern-top-k-elements/01-understanding-the-top-k-elements-pattern.md:25`
  — "Aggregate f over top k items" (27 frames → 9 steps)
- `03-pattern-top-k-elements/01-understanding-the-top-k-elements-pattern.md:83`
  — "Find aggregated value of f over items in minHeap" (19 frames → 7 steps)
- `03-pattern-top-k-elements/02-identifying-the-top-k-elements-pattern.md:27`
  — "Find average of k largest" (27 frames → 9 steps)
- `03-pattern-top-k-elements/02-identifying-the-top-k-elements-pattern.md:85`
  — "Find average of all items in minHeap" (20 frames → 7 steps)

### Comparator patterns

- `04-pattern-comparator/01-understanding-comparators.md:19` —
  "Comparator orders user-defined types" (18 frames → 7 steps)
- `04-pattern-comparator/02-understanding-the-comparator-pattern.md:47` —
  "Find k largest in transformed array" (27 frames → 9 steps)
- `04-pattern-comparator/02-understanding-the-comparator-pattern.md:105` —
  "Aggregate f over minHeap items" (19 frames → 7 steps)
- `04-pattern-comparator/03-identifying-the-comparator-pattern.md:33` —
  "Find frequency of all items" (13 frames → 6 steps)
- `04-pattern-comparator/03-identifying-the-comparator-pattern.md:71` —
  "Find k most frequent items using min heap" (20 frames → 7 steps)
- `04-pattern-comparator/03-identifying-the-comparator-pattern.md:115` —
  "Add heap items to result array" (12 frames → 6 steps)

**Compression target**: 20 source diagrams → ~17-18 widget instances (a
couple of 1-2-frame demos compress to a single static step that still
shows the dual view).

## 3. Destination chapter usage

All under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/03-heap/`:

- `01-introduction-to-heaps.md` — 4 instances; `mode:"max"` mostly with
  the priority-queue narrative payloads.
- `02-array-implementation-of-heaps.md` — 6 instances (POC; see §10). All
  the core operations: insert + sift-up, delete + sift-down, peek, extract,
  build top-down, build bottom-up.
- `03-pattern-top-k-elements.md` — 4 instances; `mode:"min"` (k-largest
  uses a min-heap of size k) and `mode:"max"` (k-smallest uses a max-heap
  of size k).
- `04-pattern-comparator.md` — 5 instances; `mode:"custom"` with an
  author-supplied `comparatorLabel` (the comparator function itself is
  rendered as a text label only — no JS in payloads).
- `05-design.md` — 0 source diagrams (design exercise, prose-only).

### Reuse outside Phase 8

- No direct reuse expected. `stack-queue` (priority mode) is the conceptual
  entry point; once a chapter shifts to a true heap topology, `heap-tree`
  takes over. The `04-graphs` Dijkstra chapter could use `heap-tree` to
  show the priority queue alongside the graph — defer that decision to
  Phase 9 widget build.

## 4. Payload schema sketch

```ts
{
  title: string,
  mode: "max" | "min" | "custom",            // ordering invariant
  comparatorLabel?: string,                  // mode="custom" only;
                                              // e.g. "by frequency desc, then by value asc"
  // The array view. Item 0 is the root in tree view. Children of index i
  // are at 2i+1 and 2i+2 (standard 0-indexed array heap).
  items: Array<{
    id: string,                              // stable across rebinds
    value: string,                           // displayed in tree node + array cell
    // Optional secondary key for mode="custom" displays (e.g. the
    // comparator's sort key shown as a small badge).
    sortKey?: string
  }>,
  // Optional view toggle — both panels by default; "tree" or "array" to
  // suppress the other. Used for diagrams that focus on one view only.
  view?: "both" | "tree" | "array",
  steps: Array<{
    // Per-step items override. The tree view auto-derives from this
    // (parent of i is (i-1)/2, children at 2i+1 / 2i+2).
    itemsOverride?: Array<{ id, value, sortKey? }>,
    // Per-step indices to highlight in both views. Multiple markers can
    // tag the same index (e.g. "current" + "parent").
    markers?: Array<{
      name: "current" | "parent" | "left" | "right" | "swap" | "root" |
            "last" | "i" | "j" | "kth" | "top",
      index: number
    }>,
    // Per-step swap arc — the widget draws an arc between two indices in
    // the array view AND a curved arrow between two nodes in the tree
    // view. Used to show the swap step visibly before committing it.
    pendingSwap?: { from: number, to: number },
    // Optional comparator inspection panel — for sift-up/sift-down steps,
    // show the per-step comparison ("18 > 12" / "false").
    compareResult?: { left: string, right: string, op: string, result: boolean },
    // Per-step output (k-largest / k-smallest patterns append one per step).
    outputItems?: string[],
    // Per-step "extra" array displayed below the main view (e.g. the
    // input array being consumed during build-heap).
    inputArray?: { items: string[], cursor?: number, label?: string },
    msg: string
  }>
}
```

## 5. POC payloads

### 5.1 Minimal — empty heap, peek shows null

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

### 5.2 Typical — insert with sift-up (POC chapter)

```d3 widget=heap-tree
{
  "title": "Insert 18 into the max heap — sift up to maintain invariant",
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
    {"markers": [{"name": "current", "index": 6}], "itemsOverride": [{"id": "n15", "value": "15"}, {"id": "n10", "value": "10"}, {"id": "n8", "value": "8"}, {"id": "n7", "value": "7"}, {"id": "n5", "value": "5"}, {"id": "n3", "value": "3"}, {"id": "n18", "value": "18"}], "msg": "Append 18 at index 6 (next free leaf slot). Heap invariant likely broken."},
    {"markers": [{"name": "current", "index": 6}, {"name": "parent", "index": 2}], "itemsOverride": [{"id": "n15", "value": "15"}, {"id": "n10", "value": "10"}, {"id": "n8", "value": "8"}, {"id": "n7", "value": "7"}, {"id": "n5", "value": "5"}, {"id": "n3", "value": "3"}, {"id": "n18", "value": "18"}], "compareResult": {"left": "18", "right": "8", "op": ">", "result": true}, "msg": "Compare 18 vs parent (index 2 = 8). 18 > 8 → swap."},
    {"markers": [{"name": "current", "index": 2}], "pendingSwap": {"from": 6, "to": 2}, "itemsOverride": [{"id": "n15", "value": "15"}, {"id": "n10", "value": "10"}, {"id": "n8", "value": "8"}, {"id": "n7", "value": "7"}, {"id": "n5", "value": "5"}, {"id": "n3", "value": "3"}, {"id": "n18", "value": "18"}], "msg": "Pending swap: index 6 ↔ index 2."},
    {"markers": [{"name": "current", "index": 2}, {"name": "parent", "index": 0}], "itemsOverride": [{"id": "n15", "value": "15"}, {"id": "n10", "value": "10"}, {"id": "n18", "value": "18"}, {"id": "n7", "value": "7"}, {"id": "n5", "value": "5"}, {"id": "n3", "value": "3"}, {"id": "n8", "value": "8"}], "compareResult": {"left": "18", "right": "15", "op": ">", "result": true}, "msg": "After swap: 18 at index 2. Compare with parent (index 0 = 15). 18 > 15 → swap."},
    {"markers": [{"name": "current", "index": 0}], "itemsOverride": [{"id": "n18", "value": "18"}, {"id": "n10", "value": "10"}, {"id": "n15", "value": "15"}, {"id": "n7", "value": "7"}, {"id": "n5", "value": "5"}, {"id": "n3", "value": "3"}, {"id": "n8", "value": "8"}], "msg": "Swapped. 18 is now at root (index 0). No parent — sift-up terminates."}
  ]
}
```

### 5.3 Large-N — build heap bottom-up (heapify)

```d3 widget=heap-tree
{
  "title": "Build max heap bottom-up from arr = [3, 5, 8, 7, 10, 15] — heapify from last non-leaf",
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
    {"markers": [{"name": "i", "index": 2}], "msg": "Last non-leaf index = (n/2)-1 = 2. Start heapifying from index 2."},
    {"markers": [{"name": "i", "index": 2}, {"name": "left", "index": 5}], "compareResult": {"left": "15", "right": "8", "op": ">", "result": true}, "msg": "Index 2 = 8; left child (index 5) = 15. 15 > 8 → swap."},
    {"itemsOverride": [{"id": "a", "value": "3"}, {"id": "b", "value": "5"}, {"id": "f", "value": "15"}, {"id": "d", "value": "7"}, {"id": "e", "value": "10"}, {"id": "c", "value": "8"}], "markers": [{"name": "i", "index": 1}], "msg": "Index 2 now holds 15. Move to index 1."},
    {"itemsOverride": [{"id": "a", "value": "3"}, {"id": "b", "value": "5"}, {"id": "f", "value": "15"}, {"id": "d", "value": "7"}, {"id": "e", "value": "10"}, {"id": "c", "value": "8"}], "markers": [{"name": "i", "index": 1}, {"name": "left", "index": 3}, {"name": "right", "index": 4}], "compareResult": {"left": "10", "right": "5", "op": ">", "result": true}, "msg": "Index 1 = 5. Larger child = index 4 (10). 10 > 5 → swap."},
    {"itemsOverride": [{"id": "a", "value": "3"}, {"id": "e", "value": "10"}, {"id": "f", "value": "15"}, {"id": "d", "value": "7"}, {"id": "b", "value": "5"}, {"id": "c", "value": "8"}], "markers": [{"name": "i", "index": 0}], "msg": "Done at index 1. Move to root (index 0)."},
    {"itemsOverride": [{"id": "a", "value": "3"}, {"id": "e", "value": "10"}, {"id": "f", "value": "15"}, {"id": "d", "value": "7"}, {"id": "b", "value": "5"}, {"id": "c", "value": "8"}], "markers": [{"name": "i", "index": 0}, {"name": "left", "index": 1}, {"name": "right", "index": 2}], "compareResult": {"left": "15", "right": "3", "op": ">", "result": true}, "msg": "Root = 3. Larger child = index 2 (15). 15 > 3 → swap, then recurse on subtree."},
    {"itemsOverride": [{"id": "f", "value": "15"}, {"id": "e", "value": "10"}, {"id": "a", "value": "3"}, {"id": "d", "value": "7"}, {"id": "b", "value": "5"}, {"id": "c", "value": "8"}], "markers": [{"name": "i", "index": 2}, {"name": "left", "index": 5}], "compareResult": {"left": "8", "right": "3", "op": ">", "result": true}, "msg": "Recurse: index 2 now = 3 (sifted down). Compare with child 8 → swap."},
    {"itemsOverride": [{"id": "f", "value": "15"}, {"id": "e", "value": "10"}, {"id": "c", "value": "8"}, {"id": "d", "value": "7"}, {"id": "b", "value": "5"}, {"id": "a", "value": "3"}], "msg": "Final max heap: [15, 10, 8, 7, 5, 3]. Tree shape: root 15 with children 10 + 8."}
  ]
}
```

### 5.4 Edge case — extract top (swap-and-sift-down)

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
    {"markers": [{"name": "top", "index": 0}, {"name": "last", "index": 6}], "msg": "extractTop → save root (18). Swap with last (3)."},
    {"itemsOverride": [{"id": "g", "value": "3"}, {"id": "b", "value": "15"}, {"id": "c", "value": "10"}, {"id": "d", "value": "8"}, {"id": "e", "value": "7"}, {"id": "f", "value": "5"}, {"id": "a", "value": "18"}], "markers": [{"name": "current", "index": 0}], "msg": "After swap: 3 at root; 18 at index 6 (will be removed)."},
    {"itemsOverride": [{"id": "g", "value": "3"}, {"id": "b", "value": "15"}, {"id": "c", "value": "10"}, {"id": "d", "value": "8"}, {"id": "e", "value": "7"}, {"id": "f", "value": "5"}], "markers": [{"name": "current", "index": 0}, {"name": "left", "index": 1}, {"name": "right", "index": 2}], "compareResult": {"left": "15", "right": "10", "op": ">", "result": true}, "msg": "Shrunk to size 6. Larger child of root = 15 (index 1). 3 < 15 → swap."},
    {"itemsOverride": [{"id": "b", "value": "15"}, {"id": "g", "value": "3"}, {"id": "c", "value": "10"}, {"id": "d", "value": "8"}, {"id": "e", "value": "7"}, {"id": "f", "value": "5"}], "markers": [{"name": "current", "index": 1}, {"name": "left", "index": 3}, {"name": "right", "index": 4}], "compareResult": {"left": "8", "right": "7", "op": ">", "result": true}, "msg": "At index 1: 3. Larger child = 8 (index 3). 3 < 8 → swap."},
    {"itemsOverride": [{"id": "b", "value": "15"}, {"id": "d", "value": "8"}, {"id": "c", "value": "10"}, {"id": "g", "value": "3"}, {"id": "e", "value": "7"}, {"id": "f", "value": "5"}], "markers": [{"name": "current", "index": 3}], "msg": "At index 3: 3 is now a leaf — sift-down terminates. Heap: [15, 8, 10, 3, 7, 5]. Returned 18."}
  ]
}
```

### 5.5 Mode exercise — k-largest with min-heap

```d3 widget=heap-tree
{
  "title": "Stream: find 3 largest of [4, 1, 7, 3, 9, 2, 6] using a min-heap of size 3",
  "mode": "min",
  "items": [],
  "steps": [
    {"inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 0, "label": "stream"}, "msg": "Start with empty min-heap. Stream value 4 → push since heap.size < 3."},
    {"itemsOverride": [{"id": "v4", "value": "4"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 1, "label": "stream"}, "msg": "Heap = [4]. Stream 1 → push."},
    {"itemsOverride": [{"id": "v1", "value": "1"}, {"id": "v4", "value": "4"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 2, "label": "stream"}, "msg": "Heap = [1, 4]. Stream 7 → push."},
    {"itemsOverride": [{"id": "v1", "value": "1"}, {"id": "v4", "value": "4"}, {"id": "v7", "value": "7"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 3, "label": "stream"}, "msg": "Heap = [1, 4, 7] (size 3). Stream 3 → 3 > top (1) → swap-top-and-sift-down."},
    {"itemsOverride": [{"id": "v3", "value": "3"}, {"id": "v4", "value": "4"}, {"id": "v7", "value": "7"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 4, "label": "stream"}, "msg": "Heap = [3, 4, 7]. Stream 9 → 9 > 3 → replace top."},
    {"itemsOverride": [{"id": "v4", "value": "4"}, {"id": "v9", "value": "9"}, {"id": "v7", "value": "7"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 5, "label": "stream"}, "msg": "Heap = [4, 9, 7]. Stream 2 → 2 < 4 → skip."},
    {"itemsOverride": [{"id": "v4", "value": "4"}, {"id": "v9", "value": "9"}, {"id": "v7", "value": "7"}], "inputArray": {"items": ["4", "1", "7", "3", "9", "2", "6"], "cursor": 6, "label": "stream"}, "msg": "Stream 6 → 6 > 4 → replace top."},
    {"itemsOverride": [{"id": "v6", "value": "6"}, {"id": "v9", "value": "9"}, {"id": "v7", "value": "7"}], "outputItems": ["6", "9", "7"], "msg": "End of stream. The 3 largest are: 6, 7, 9 (in heap order)."}
  ]
}
```

## 6. Closest existing widget to mimic

Mimic
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
(784 LOC) for the **array view** panel — its keyed-by-id transitions
handle in-place swaps cleanly (an item swap visibly slides across cells
rather than flickers). Mimic the upcoming `binary-tree` widget for the
**tree view** panel — share the `TreeLayout` helper if it gets extracted
during `binary-tree`'s session. Where this widget differs: the parent/
child relationships are computed (index 2i+1, 2i+2) instead of stored
on nodes, and both panels must rebind from the same source-of-truth
items array.

## 7. D3 selections plan

- `g.heap-tree__array-cell` keyed by `item.id` — array view. Same swap-
  slide transition as `ArrayTraversal`. Index labels below each cell
  (0, 1, 2, ...).
- `g.heap-tree__tree-node` keyed by `item.id` — tree view. Layout
  computed by an array→tree pass: index `i` sits at level `floor(log2(i+1))`,
  with horizontal position derived from `i` and the layer width. Swaps
  in the array trigger tree-node `transform` transitions so the visual
  effect is identical in both views.
- `path.heap-tree__tree-edge` keyed by `${parentId}->${childId}` from
  index math. Auto-derived per step from `itemsOverride`.
- `path.heap-tree__pending-swap` — drawn between `pendingSwap.from` and
  `pendingSwap.to` in both views; animates in via stroke-dasharray
  shimmer.
- `g.heap-tree__compare-panel` — a one-line text panel at the top
  rendering `compareResult.left compareResult.op compareResult.right →
  compareResult.result`.
- `g.heap-tree__input-strip` (when `inputArray` is set) — a horizontal
  row above the array view with a cursor marker.

## 8. Shared abstractions

- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
  and
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`.
- Reuses `TreeLayout` if extracted by the `binary-tree` build session;
  otherwise inlines its own array→(x,y) layout function. **Build order
  matters**: do `binary-tree` first, then `heap-tree` so `TreeLayout`
  can be shared.
- New shared utility candidate: a `SyncedDualView` controller that keeps
  two panels animating in sync from a single source-of-truth array.
  Only this widget needs it today; **defer extraction** until a second
  consumer appears (likely `lsm-tree`, which also has a logical/physical
  dual view).

## 9. Estimated build session count

**1 session** — the schema is small and the dual-view rendering is well-
contained. The trickiest piece is keeping the tree edges in sync with the
array on per-step `itemsOverride` rebinds; that's a few lines of index
math, not a structural concern. Authoring 4-5 demo payloads (insert,
extract, build top-down, build bottom-up, comparator) fills the demo
chapter.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/03-heap/02-array-implementation-of-heaps.md`
— the only chapter that exercises every core operation (peek, insert,
delete, extract, build top-down, build bottom-up — 6 source diagrams).
Lands the full mode + dual-view spec in one chapter so subsequent
pattern chapters (`03-pattern-top-k-elements.md`, `04-pattern-comparator.md`)
are pure content work with no rendering surprises.
