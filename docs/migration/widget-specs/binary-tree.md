# Widget Spec — `binary-tree`

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `binary-tree` and Arc 3 chapter payload writing.

## 1. Purpose

Animate node-by-node operations over a rooted binary tree across five
variants: **plain binary tree** (Phase 6, 18 chapters), **BST** (Phase 7,
13 chapters), **AVL** (orphan tree chapter, balance-factor + rotations),
**RBT** (orphan, red/black colour + rotations), and **treap** (orphan,
priority + rotations). One widget covers all five via a `mode` flag.
Every step renders: tree topology, per-node visit highlights, optional
sidebar iterator stack (the running call-stack of a recursive traversal,
or the explicit stack of an iterative iterator), and the per-step
explanatory message. Supports all four DFS traversals (pre/in/post + LCA
walks), level-order BFS, recursive + iterative insertion, recursive +
iterative deletion (3 cases), constructor from sorted/unsorted arrays,
and forward/reverse BST iterators.

## 2. Source-diagram inventory

Source roots: 84+ interactive diagrams across two phases. Orphan AVL/RBT/
Treap chapters add 0 source diagrams (payloads will be authored from
destination prose).

### Phase 6 — Binary Tree (`mode:"binary"`, 39 diagrams)

Source: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/7.binary-tree/`.

#### Recursive traversals

- `04-recursive-traversals-in-binary-trees/02-understanding-recursive-preorder-traversal.md:11`
  — "Preorder Traversal" (30 frames → 9 steps)
- `04-recursive-traversals-in-binary-trees/04-understanding-recursive-inorder-traversal.md:11`
  — "Inorder Traversal" (31 frames → 9 steps)
- `04-recursive-traversals-in-binary-trees/06-understanding-recursive-postorder-traversal.md:11`
  — "Postorder Traversal" (30 frames → 9 steps)

#### Iterative traversals (need sidebar iterator stack)

- `05-iterative-traversals-in-binary-trees/01-understanding-the-problem.md:9`
  — "Every function call creates a stack frame" (13 frames → 6 steps)
- `05-iterative-traversals-in-binary-trees/01-understanding-the-problem.md:41`
  — "Too many nested calls leads to stack overflow" (10 frames → 5 steps)
- `05-iterative-traversals-in-binary-trees/02-understanding-iterative-preorder-traversal.md:26`
  — "Iter preorder step 1" (16 frames → 7 steps)
- `05-iterative-traversals-in-binary-trees/02-understanding-iterative-preorder-traversal.md:79`
  — "Iter preorder step 2" (6 frames → 4 steps)
- `05-iterative-traversals-in-binary-trees/04-understanding-iterative-inorder-traversal.md:27`
  — "Iter inorder step 1" (8 frames → 5 steps)
- `05-iterative-traversals-in-binary-trees/04-understanding-iterative-inorder-traversal.md:63`
  — "Iter inorder step 2" (5 frames → 3 steps)
- `05-iterative-traversals-in-binary-trees/04-understanding-iterative-inorder-traversal.md:86`
  — "Iter inorder step 3" (3 frames → 2 steps)
- `05-iterative-traversals-in-binary-trees/06-understanding-iterative-postorder-traversal.md:42`
  — "Iter postorder step 1" (9 frames → 5 steps)
- `05-iterative-traversals-in-binary-trees/06-understanding-iterative-postorder-traversal.md:83`
  — "Iter postorder step 2" (6 frames → 4 steps)
- `05-iterative-traversals-in-binary-trees/06-understanding-iterative-postorder-traversal.md:124`
  — "Iter postorder step 3" (6 frames → 4 steps)
- `05-iterative-traversals-in-binary-trees/08-understanding-level-order-traversal.md:20`
  — "Level Order example" (8 frames → 5 steps)
- `05-iterative-traversals-in-binary-trees/08-understanding-level-order-traversal.md:42`
  — "Level Order algorithm" (31 frames → 9 steps)

#### Constructing trees

- `06-constructing-a-binary-tree/04-understanding-construction-using-preorder-and-inorder-traversal.md:27`
  — "Construct from inorder + preorder" (27 frames → 9 steps)
- `06-constructing-a-binary-tree/06-understanding-construction-using-postorder-and-inorder-traversal.md:27`
  — "Construct from inorder + postorder" (27 frames → 9 steps)

#### Insertion

- `07-insertion-in-binary-trees/03-understanding-recursive-insertion-of-a-leaf.md:16`
  — "Recursive insert leaf 9" (8 frames → 5 steps)
- `07-insertion-in-binary-trees/05-understanding-iterative-insertion-of-a-leaf.md:9`
  — "Iterative insert leaf 9" (8 frames → 5 steps)
- `07-insertion-in-binary-trees/07-understanding-insertion-of-a-child.md:12`
  — "Insert child step 1: search parent" (18 frames → 7 steps)
- `07-insertion-in-binary-trees/09-understanding-insertion-of-a-parent.md:38`
  — "Insert parent step 1: search parent" (7 frames → 4 steps)

#### Traversal patterns (the meaty chapters)

- `08-pattern-preorder-traversal-stateless/01-understanding-the-stateless-preorder-traversal-pattern.md:33`
  — "Aggregate f over root-to-node path" (39 frames → 10 steps)
- `08-pattern-preorder-traversal-stateless/02-identifying-the-stateless-preorder-traversal-pattern.md:29`
  — "Update node with prefix sum" (39 frames → 10 steps)
- `09-pattern-preorder-traversal-stateful/01-understanding-the-stateful-preorder-traversal-pattern.md:33`
  — "Process with aggregated value of f" (47 frames → 10 steps)
- `09-pattern-preorder-traversal-stateful/02-identifying-the-stateful-preorder-traversal-pattern.md:35`
  — "Count nodes with duplicates in path" (36 frames → 10 steps)
- `10-pattern-postorder-traversal-stateless/01-understanding-the-stateless-postorder-traversal-pattern.md:37`
  — "Process with f over left+right subtrees" (28 frames → 9 steps)
- `10-pattern-postorder-traversal-stateless/02-identifying-the-stateless-postorder-traversal-pattern.md:29`
  — "Find sum of all leaves" (23 frames → 8 steps)
- `11-pattern-postorder-traversal-stateful/01-understanding-the-stateful-postorder-traversal-pattern.md:27`
  — "Stateful postorder using f and g" (35 frames → 10 steps)
- `11-pattern-postorder-traversal-stateful/02-identifying-the-stateful-postorder-traversal-pattern.md:67`
  — "Find diameter of binary tree" (34 frames → 10 steps)
- `12-pattern-root-to-leaf-path-stateless/01-understanding-the-stateless-root-to-leaf-path-pattern.md:47`
  — "Aggregate over root-to-leaf paths" (33 frames → 10 steps)
- `12-pattern-root-to-leaf-path-stateless/02-identifying-the-stateless-root-to-leaf-path-pattern.md:45`
  — "Find if any path sums to 9" (35 frames → 10 steps)
- `13-pattern-root-to-leaf-path-stateful/01-understanding-the-stateful-root-to-leaf-path-pattern.md:31`
  — "Stateful version" (28 frames → 9 steps)
- `13-pattern-root-to-leaf-path-stateful/02-identifying-the-stateful-root-to-leaf-path-pattern.md:41`
  — "Find all paths with sum 9" (33 frames → 10 steps)
- `14-pattern-level-order-traversal/01-understanding-the-level-order-traversal-pattern.md:25`
  — "Aggregate nodes in a level" (37 frames → 10 steps)
- `14-pattern-level-order-traversal/02-identifying-the-level-order-traversal-pattern.md:29`
  — "Return level-wise sums" (37 frames → 10 steps)
- `16-pattern-lowest-common-anscestor/01-understanding-the-lowest-common-ancestor-pattern.md:109`
  — "Find LCA for a set of nodes" (23 frames → 8 steps)
- `16-pattern-lowest-common-anscestor/02-identifying-the-lowest-common-ancestor-pattern.md:33`
  — "Find LCA for nodes f and e" (29 frames → 9 steps)
- `17-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md:27`
  — "Find aggregated value over corresponding nodes" (31 frames → 9 steps)
- `17-pattern-simultaneous-traversal/02-identifying-the-simultaneous-traversal-pattern.md:33`
  — "Find if two trees are identical" (28 frames → 9 steps)

### Phase 7 — BST (`mode:"bst"`, 45 diagrams)

Source: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/8.binary-search-tree/`.

#### Height & balance

- `02-height-and-balance-in-binary-search-trees/02-understanding-the-balance-of-a-tree.md:31`
  — "Creating tree where |bal| ≤ 1" (10 frames → 5 steps)

#### Recursive searching

- `03-recursive-searching-in-binary-search-trees/01-understanding-recursive-search.md:18`
  — "Searching for a value" (5 frames → 4 steps)
- `03-recursive-searching-in-binary-search-trees/03-understanding-recursive-minimum-search.md:15`
  — "Minimum search" (5 frames → 4 steps)
- `03-recursive-searching-in-binary-search-trees/05-understanding-recursive-maximum-search.md:15`
  — "Maximum search" (5 frames → 4 steps)
- `03-recursive-searching-in-binary-search-trees/07-understanding-recursive-lower-bound-search.md:13`
  — "Lower bound — value present" (6 frames → 4 steps)
- `03-recursive-searching-in-binary-search-trees/07-understanding-recursive-lower-bound-search.md:35`
  — "Lower bound of 54" (8 frames → 5 steps)
- `03-recursive-searching-in-binary-search-trees/07-understanding-recursive-lower-bound-search.md:57`
  — "Lower bound of 63" (8 frames → 5 steps)
- `03-recursive-searching-in-binary-search-trees/09-understanding-recursive-upper-bound-search.md:9`
  — "Upper bound of 54" (8 frames → 5 steps)

#### Iterative searching

- `04-iterative-searching-in-binary-search-trees/01-understanding-iterative-search.md:9`
  — "Searching for a value" (5 frames → 4 steps)
- `04-iterative-searching-in-binary-search-trees/03-understanding-iterative-minimum-search.md:5`
  — "Iterative minimum" (5 frames → 4 steps)
- `04-iterative-searching-in-binary-search-trees/05-understanding-iterative-maximum-search.md:9`
  — "Iterative maximum" (5 frames → 4 steps)
- `04-iterative-searching-in-binary-search-trees/07-understanding-iterative-lower-bound-search.md:13`
  — "Lower bound — value present" (6 frames → 4 steps)
- `04-iterative-searching-in-binary-search-trees/07-understanding-iterative-lower-bound-search.md:35`
  — "Lower bound of 54" (8 frames → 5 steps)
- `04-iterative-searching-in-binary-search-trees/07-understanding-iterative-lower-bound-search.md:57`
  — "Lower bound of 63" (8 frames → 5 steps)
- `04-iterative-searching-in-binary-search-trees/09-understanding-iterative-upper-bound-search.md:9`
  — "Upper bound of 54" (8 frames → 5 steps)

#### Insertion

- `05-insertion-in-binary-search-trees/01-understanding-recursive-insertion.md:16`
  — "Inserting a value" (7 frames → 4 steps)
- `05-insertion-in-binary-search-trees/03-understanding-iterative-insertion.md:9`
  — "Iterative insertion" (10 frames → 5 steps)

#### Deletion (the three cases)

- `06-deletion-in-binary-search-trees/01-understanding-recursive-deletion.md:20`
  — "Deletion — leaf" (9 frames → 5 steps)
- `06-deletion-in-binary-search-trees/01-understanding-recursive-deletion.md:47`
  — "Deletion — one child" (7 frames → 4 steps)
- `06-deletion-in-binary-search-trees/01-understanding-recursive-deletion.md:80`
  — "Deletion — two children" (13 frames → 6 steps)
- `06-deletion-in-binary-search-trees/04-understanding-iterative-deletion.md:17`
  — "Iter deletion — leaf" (9 frames → 5 steps)
- `06-deletion-in-binary-search-trees/04-understanding-iterative-deletion.md:48`
  — "Iter deletion — one child" (8 frames → 5 steps)
- `06-deletion-in-binary-search-trees/04-understanding-iterative-deletion.md:94`
  — "Iter deletion — two children" (12 frames → 6 steps)

#### Constructing BSTs

- `07-constructing-a-binary-search-tree/01-understanding-construction-from-a-sorted-array.md:31`
  — "Construct balanced BST from sorted array" (44 frames → 10 steps)
- `07-constructing-a-binary-search-tree/03-understanding-construction-from-an-unsorted-array.md:9`
  — "Insert values one at a time" (7 frames → 4 steps)
- `07-constructing-a-binary-search-tree/03-understanding-construction-from-an-unsorted-array.md:338`
  — "Best case time complexity" (9 frames → 5 steps)
- `07-constructing-a-binary-search-tree/03-understanding-construction-from-an-unsorted-array.md:364`
  — "Worst case time complexity" (9 frames → 5 steps)

#### LCA

- `08-lowest-common-ansestor-in-binary-search-trees/01-understanding-the-lowest-common-ancestor.md:45`
  — "Find LCA" (10 frames → 5 steps)

#### Iterators (need sidebar iterator stack heavily)

- `09-iterators-in-a-binary-search-trees/01-understanding-iterators-in-binary-search-trees.md:11`
  — "Recursive traversal traverses entire tree at once" (32 frames → 10 steps)
- `09-iterators-in-a-binary-search-trees/01-understanding-iterators-in-binary-search-trees.md:79`
  — "Iterator traverses on demand" (24 frames → 8 steps)
- `09-iterators-in-a-binary-search-trees/02-understanding-the-forward-bst-iterator.md:7`
  — "Init forward iterator pushes left nodes" (7 frames → 4 steps)
- `09-iterators-in-a-binary-search-trees/02-understanding-the-forward-bst-iterator.md:25`
  — "After top, push left of right child" (6 frames → 4 steps)
- `09-iterators-in-a-binary-search-trees/02-understanding-the-forward-bst-iterator.md:41`
  — "Working of forward iterator" (36 frames → 10 steps)
- `09-iterators-in-a-binary-search-trees/04-understanding-the-reverse-bst-iterator.md:7`
  — "Init reverse iterator" (7 frames → 4 steps)
- `09-iterators-in-a-binary-search-trees/04-understanding-the-reverse-bst-iterator.md:25`
  — "After top of stack" (6 frames → 4 steps)
- `09-iterators-in-a-binary-search-trees/04-understanding-the-reverse-bst-iterator.md:41`
  — "Working of reverse iterator" (36 frames → 10 steps)

#### Sorted/Reversed/Range/Two-pointer patterns

- `10-pattern-sorted-traversal/01-understanding-the-sorted-traversal-pattern.md:27`
  — "Process nodes in sorted order" (47 frames → 10 steps)
- `10-pattern-sorted-traversal/02-identifying-the-sorted-traversal-pattern.md:49`
  — "Find min absolute difference" (45 frames → 10 steps)
- `11-pattern-reversed-sorted-traversal/01-understanding-the-reversed-sorted-traversal-pattern.md:27`
  — "Process in reverse sorted order" (47 frames → 10 steps)
- `11-pattern-reversed-sorted-traversal/02-identifying-the-reverse-sorted-traversal-pattern.md:35`
  — "Find kth largest element" (24 frames → 8 steps)
- `12-pattern-range-postorder/01-understanding-the-range-postorder-pattern.md:37`
  — "Process nodes within range" (32 frames → 10 steps)
- `12-pattern-range-postorder/02-identifying-the-range-postorder-pattern.md:35`
  — "Add sum of descendants within range" (38 frames → 10 steps)
- `13-pattern-two-pointer/01-understanding-the-two-pointer-pattern.md:27`
  — "Sorted + reverse sorted simultaneously" (19 frames → 7 steps)
- `13-pattern-two-pointer/02-identifying-the-two-pointer-pattern.md:21`
  — "Find pair with sum 13" (24 frames → 8 steps)
- `13-pattern-two-pointer/02-identifying-the-two-pointer-pattern.md:500`
  — same (20 frames → 8 steps)

### Orphan modes — AVL / RBT / Treap (0 source diagrams)

Payloads authored from destination prose:

- `mode:"avl"` — show balance-factor labels on each node + left/right
  rotation animations. Used by
  `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/06-avl-tree/01-introduction-to-avl-trees.md`.
- `mode:"rbt"` — show node colour (red/black) + recolour + rotation
  animations. Used by destination
  `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/07-red-black-tree/`.
- `mode:"treap"` — show heap-priority label alongside BST key, rotate to
  maintain heap order. Used by
  `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/09-probabilistic-and-advanced/05-treap.md`.

**Compression target**: 84 source diagrams + ~10 orphan-authored ≈ ~70-80
widget instances after frame compression.

## 3. Destination chapter usage

### Phase 6 — Binary Tree (`mode:"binary"`)

All 18 chapters under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/01-binary-tree/`:

- `01-introduction-to-binary-trees.md`, `02-array-implementation-of-binary-trees.md`,
  `03-linked-list-implementation-of-binary-trees.md` — 0 or 1 instance each
  (mostly definitional).
- `04-recursive-traversals-in-binary-trees.md` — 3 instances (POC; see §10).
- `05-iterative-traversals-in-binary-trees.md` — 10 instances (sidebar
  iterator stack heavily used).
- `06-constructing-a-binary-tree.md` — 2 instances.
- `07-insertion-in-binary-trees.md` — 4 instances.
- `08..15-pattern-*.md` — 1-2 instances each (~14 total).
- `16-pattern-lowest-common-ancestor.md` — 2 instances.
- `17-pattern-simultaneous-traversal.md` — 2 instances (renders two trees
  side-by-side; needs `secondaryTree` field).
- `18-practice-mix-traversals.md` — 1-3 instances (practice problems).

### Phase 7 — BST (`mode:"bst"`)

All 13 chapters under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/02-binary-search-tree/`:

per source counts above. Iterator chapter (`09-iterators-in-binary-search-trees.md`)
is the heaviest user of the sidebar iterator-stack feature.

### Orphan tree chapters

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/06-avl-tree/01-introduction-to-avl-trees.md`
  — `mode:"avl"` payloads showing left-left, right-right, left-right,
  right-left rotations.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/07-red-black-tree/`
  (chapter file under construction) — `mode:"rbt"`.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/09-probabilistic-and-advanced/05-treap.md`
  — `mode:"treap"`.

### Real-systems reuse

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/11-dsa-in-real-systems/02-linux-red-black-tree-in-the-cfs-scheduler.md`
  — `mode:"rbt"` showing the CFS task tree.

## 4. Payload schema sketch

```ts
{
  title: string,
  mode: "binary" | "bst" | "avl" | "rbt" | "treap",
  // Tree definition. Nodes form a directed graph keyed by id; root is
  // explicit. Each node carries one or more annotations depending on
  // mode (colour for rbt, balanceFactor for avl, priority for treap).
  root: string,                            // node id
  nodes: Array<{
    id: string,
    value: string,
    left?: string | null,                   // child node id; null means missing
    right?: string | null,
    // Mode-specific annotations
    color?: "red" | "black",                // rbt only
    balanceFactor?: number,                 // avl only (-2..2)
    priority?: number,                      // treap only (heap priority)
    // Display state shared across modes
    style?: "new" | "removed" | "highlight" | "match" | "visited"
  }>,
  // Optional second tree for simultaneous-traversal mode (Phase 6 ch 17).
  secondaryTree?: {
    root: string,
    nodes: Array<{ id, value, left, right, style? }>
  },
  // Optional sidebar — when present, renders to the right of the tree
  // showing a vertical stack (one row per frame). Used by iterative
  // traversal + BST iterator chapters.
  sidebar?: {
    kind: "call-stack" | "iterator-stack",
    label?: string                          // e.g. "Stack" or "Iterator state"
  },
  steps: Array<{
    // Per-step node visit markers. Multiple markers per step (e.g.
    // `current`, `parent`, `lo`, `hi`).
    markers?: Array<{
      name: "current" | "parent" | "left" | "right" | "low" | "high" |
            "found" | "successor" | "predecessor" | "p" | "q" | "lca",
      nodeId: string
    }>,
    // Per-step node style overrides (e.g. mark a node "visited" after a
    // postorder traversal returns from it). Map of nodeId → style.
    nodeStyles?: Record<string, "visited" | "highlight" | "removed" | "match">,
    // Per-step node value/colour/balance overrides — for insertion,
    // rotation, recolour animations. Applied on top of spec.nodes.
    nodesOverride?: Array<{
      id, value?, left?, right?, color?, balanceFactor?, priority?, style?
    }>,
    // Sidebar contents for this step. Topmost frame appears at bottom.
    sidebarFrames?: Array<{
      label: string,                         // e.g. "f(node=5)" or "node = 7"
      kind?: "active" | "returned" | "queued"
    }>,
    // Optional output-strip showing the traversal output so far
    // (e.g. "5, 3, 7, 10" appended one per step). Used by sorted-traversal
    // chapters.
    outputItems?: string[],
    msg: string
  }>
}
```

## 5. POC payloads

### 5.1 Minimal — single-node tree

```d3 widget=binary-tree
{
  "title": "Single-node binary tree",
  "mode": "binary",
  "root": "n1",
  "nodes": [{"id": "n1", "value": "5", "left": null, "right": null}],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n1"}], "msg": "Root is the only node; left = right = null."}
  ]
}
```

### 5.2 Typical — recursive inorder traversal (POC chapter)

```d3 widget=binary-tree
{
  "title": "Recursive inorder traversal — left, root, right",
  "mode": "binary",
  "root": "n5",
  "nodes": [
    {"id": "n5", "value": "5", "left": "n3", "right": "n7"},
    {"id": "n3", "value": "3", "left": "n1", "right": "n4"},
    {"id": "n7", "value": "7", "left": null, "right": "n9"},
    {"id": "n1", "value": "1", "left": null, "right": null},
    {"id": "n4", "value": "4", "left": null, "right": null},
    {"id": "n9", "value": "9", "left": null, "right": null}
  ],
  "sidebar": {"kind": "call-stack", "label": "Recursion stack"},
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n5"}], "sidebarFrames": [{"label": "inorder(5)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(5). Recurse left."},
    {"markers": [{"name": "current", "nodeId": "n3"}], "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(3). Recurse left."},
    {"markers": [{"name": "current", "nodeId": "n1"}], "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)"}, {"label": "inorder(1)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(1). Left is null → no recurse."},
    {"markers": [{"name": "current", "nodeId": "n1"}], "nodeStyles": {"n1": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)"}, {"label": "inorder(1)", "kind": "active"}], "outputItems": ["1"], "msg": "Visit 1; append to output. Right is null → return."},
    {"markers": [{"name": "current", "nodeId": "n3"}], "nodeStyles": {"n1": "visited", "n3": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)", "kind": "active"}], "outputItems": ["1", "3"], "msg": "Back at 3; visit 3 → output. Recurse right."},
    {"markers": [{"name": "current", "nodeId": "n4"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)"}, {"label": "inorder(4)", "kind": "active"}], "outputItems": ["1", "3", "4"], "msg": "inorder(4): visit 4; return."},
    {"markers": [{"name": "current", "nodeId": "n5"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited"}, "sidebarFrames": [{"label": "inorder(5)", "kind": "active"}], "outputItems": ["1", "3", "4", "5"], "msg": "Back at 5; visit 5; output. Recurse right."},
    {"markers": [{"name": "current", "nodeId": "n7"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited", "n7": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(7)", "kind": "active"}], "outputItems": ["1", "3", "4", "5", "7"], "msg": "inorder(7); visit 7; recurse right."},
    {"markers": [{"name": "current", "nodeId": "n9"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited", "n7": "visited", "n9": "visited"}, "sidebarFrames": [], "outputItems": ["1", "3", "4", "5", "7", "9"], "msg": "Done. In-order = 1, 3, 4, 5, 7, 9 — sorted (because this is a BST shape)."}
  ]
}
```

### 5.3 Large-N — BST deletion (two-children case)

```d3 widget=binary-tree
{
  "title": "BST delete 5 — two-children case (replace with in-order successor 6)",
  "mode": "bst",
  "root": "n8",
  "nodes": [
    {"id": "n8", "value": "8", "left": "n5", "right": "n10"},
    {"id": "n5", "value": "5", "left": "n3", "right": "n7"},
    {"id": "n3", "value": "3", "left": null, "right": null},
    {"id": "n7", "value": "7", "left": "n6", "right": null},
    {"id": "n6", "value": "6", "left": null, "right": null},
    {"id": "n10", "value": "10", "left": null, "right": null}
  ],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n5"}], "msg": "Locate 5 → has two children → cannot remove directly."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n7"}], "msg": "Walk to right subtree to find in-order successor (leftmost)."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n6"}], "msg": "Walk left → 6 is the successor."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n6"}], "nodesOverride": [{"id": "n5", "value": "6", "style": "highlight"}], "msg": "Copy 6's value into the node being deleted."},
    {"markers": [{"name": "current", "nodeId": "n6"}], "nodeStyles": {"n6": "removed"}, "nodesOverride": [{"id": "n5", "value": "6"}, {"id": "n7", "left": null}], "msg": "Remove the original 6 (leaf case)."},
    {"nodesOverride": [{"id": "n5", "value": "6"}, {"id": "n7", "left": null}], "msg": "Final tree: 8 with left = 6, right = 10 — BST invariant preserved."}
  ]
}
```

### 5.4 Edge case — forward BST iterator (sidebar iterator-stack mode)

```d3 widget=binary-tree
{
  "title": "Forward BST iterator — init pushes left chain to stack",
  "mode": "bst",
  "root": "n5",
  "nodes": [
    {"id": "n5", "value": "5", "left": "n3", "right": "n7"},
    {"id": "n3", "value": "3", "left": "n1", "right": null},
    {"id": "n7", "value": "7", "left": null, "right": null},
    {"id": "n1", "value": "1", "left": null, "right": null}
  ],
  "sidebar": {"kind": "iterator-stack", "label": "Stack"},
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n5"}], "sidebarFrames": [], "msg": "init(): start at root. Push 5; descend left."},
    {"markers": [{"name": "current", "nodeId": "n3"}], "sidebarFrames": [{"label": "5"}], "msg": "Push 3; descend left."},
    {"markers": [{"name": "current", "nodeId": "n1"}], "sidebarFrames": [{"label": "5"}, {"label": "3"}], "msg": "Push 1; left is null → init complete."},
    {"sidebarFrames": [{"label": "5"}, {"label": "3"}, {"label": "1", "kind": "active"}], "msg": "Stack top = 1. next() returns 1, pops it, then descends right of 1 (null) → nothing pushed."},
    {"nodeStyles": {"n1": "visited"}, "sidebarFrames": [{"label": "5"}, {"label": "3", "kind": "active"}], "msg": "Top = 3. next() returns 3; right is null → no push."},
    {"nodeStyles": {"n1": "visited", "n3": "visited"}, "sidebarFrames": [{"label": "5", "kind": "active"}], "msg": "Top = 5. next() returns 5; right = 7 → descend left of 7."},
    {"markers": [{"name": "current", "nodeId": "n7"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n5": "visited"}, "sidebarFrames": [{"label": "7", "kind": "active"}], "msg": "Push 7; left null. Top = 7."},
    {"nodeStyles": {"n1": "visited", "n3": "visited", "n5": "visited", "n7": "visited"}, "sidebarFrames": [], "msg": "next() returns 7. Stack empty → iterator exhausted. Final sequence: 1, 3, 5, 7."}
  ]
}
```

### 5.5 AVL rotation (mode flag exercise)

```d3 widget=binary-tree
{
  "title": "AVL right-rotation — restore balance after left-left insert",
  "mode": "avl",
  "root": "n30",
  "nodes": [
    {"id": "n30", "value": "30", "left": "n20", "right": null, "balanceFactor": -2},
    {"id": "n20", "value": "20", "left": "n10", "right": null, "balanceFactor": -1},
    {"id": "n10", "value": "10", "left": null, "right": null, "balanceFactor": 0}
  ],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n30"}], "msg": "Imbalance at 30: balance factor -2 → left-left case → right-rotate around 30."},
    {"nodesOverride": [{"id": "n20", "left": "n10", "right": "n30", "balanceFactor": 0}, {"id": "n30", "left": null, "right": null, "balanceFactor": 0}], "msg": "After rotation: 20 becomes root, 10 left child, 30 right child. All balance factors back to 0."}
  ]
}
```

### 5.6 RBT recolour-and-rotate (mode exercise)

```d3 widget=binary-tree
{
  "title": "Red-black tree — fix red-red violation after insert",
  "mode": "rbt",
  "root": "n10",
  "nodes": [
    {"id": "n10", "value": "10", "left": "n5", "right": "n15", "color": "black"},
    {"id": "n5",  "value": "5",  "left": "n3", "right": null,  "color": "red"},
    {"id": "n15", "value": "15", "left": null, "right": null,  "color": "red"},
    {"id": "n3",  "value": "3",  "left": null, "right": null,  "color": "red"}
  ],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n3"}], "msg": "Newly inserted 3 is red; parent 5 is red → red-red violation."},
    {"markers": [{"name": "current", "nodeId": "n3"}, {"name": "parent", "nodeId": "n5"}], "msg": "Uncle (15) is red → case 1: recolour parent + uncle to black; grandparent to red."},
    {"nodesOverride": [{"id": "n5", "color": "black"}, {"id": "n15", "color": "black"}, {"id": "n10", "color": "red"}], "msg": "Recoloured. Grandparent is now red — but it's the root, so force to black."},
    {"nodesOverride": [{"id": "n5", "color": "black"}, {"id": "n15", "color": "black"}, {"id": "n10", "color": "black"}], "msg": "Final: root black, both children black, new red 3 sits under black 5. Black-height invariant restored."}
  ]
}
```

## 6. Closest existing widget to mimic

Mimic
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala`
(1,552 LOC) for the node + edge rendering model (keyed-by-id joins,
named-marker triangles, ADR-0016-style canonical marker vocabulary). The
tree layout itself is new — use a top-down Reingold-Tilford layout
(D3.js's `d3-hierarchy.tree()` works fine, but a hand-rolled
recursive-shift layout keeps the dependency footprint at zero). Sidebar
iterator/call stack reuses
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`'s
vertical-row pattern; render it as a vertical SVG group to the right of
the tree.

## 7. D3 selections plan

- `g.binary-tree__node` keyed by `node.id`. Transform is computed once
  per step by a layout pass over `spec.nodes` honouring `nodesOverride`
  edits. Mode-specific decorators (colour swatch for rbt,
  balance-factor label for avl, priority badge for treap) live as
  conditional inner elements that show/hide via class toggles.
- `path.binary-tree__edge` keyed by `${parent.id}->${child.id}` so an
  edge rewired by rotation transitions smoothly rather than fading.
- `g.binary-tree__marker` keyed by `marker.name`. Marker triangle
  positions are computed from the node's centre.
- Sidebar: `g.binary-tree__sidebar-frame` keyed by `${depth}-${label}`.
  New frames slide in from the right; popped frames fade out and
  collapse upward.
- Second-tree mode (Phase 6 ch 17): mirror the primary layout on the
  right half of the canvas. Same selections under `g.binary-tree__node--secondary` etc.

## 8. Shared abstractions

- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
  and
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`.
- New shared utility candidate: `TreeLayout.scala` — a pure
  Reingold-Tilford-style layout that takes a node-id graph + root and
  returns (x, y) per node. Both `heap-tree` and (later) `recurrence-tree`
  and `decision-tree` will want it. **Defer to Arc 2** once the second
  consumer appears; until then keep the layout function `private` inside
  `BinaryTree.scala`.
- New shared utility: `SidebarStack` — the vertical-call-stack
  visualisation. Used here for both call-stack and iterator-stack; the
  `call-stack` widget will also want it. **Probably extract during
  Arc 1's call-stack build session**, when the second consumer
  materialises.

## 9. Estimated build session count

**1-2 sessions** — this is the most schema-heavy widget in the catalog.
Session 1: `mode:"binary"` + `mode:"bst"` end-to-end, including the
sidebar iterator-stack feature. Session 2 (optional): orphan
`mode:"avl"` / `"rbt"` / `"treap"` decorators and the secondary-tree
rendering for the simultaneous-traversal chapter. Bundle into one
session if time allows; split if rotation animations get hairy.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/03-trees/01-binary-tree/04-recursive-traversals-in-binary-trees.md`
— smallest chapter that genuinely needs the widget (3 source diagrams,
all in the 30-31 frame range, pure `mode:"binary"`, exercises the
sidebar call-stack). All three traversal payloads share the same tree
shape, so the chapter doubles as a built-in stress test of node-style
transitions across step changes. Once this renders cleanly the Phase 6
iterative-traversal chapter — which is the heaviest user of sidebar
iterator-stack — follows in the next session.
