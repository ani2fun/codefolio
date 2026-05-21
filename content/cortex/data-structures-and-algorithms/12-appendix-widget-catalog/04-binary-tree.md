---
title: binary-tree
summary: Rooted binary tree across five mode flavours (binary / bst / avl / rbt / treap). Renders node-by-node traversals, insertions, deletions, and rotations with optional sidebar (call-stack or iterator-stack) and optional output strip for traversal sequences.
prereqs: []
---

# `binary-tree`

## Purpose

A single widget covers all five tree-flavour topologies the DSA book needs across Phase 6 (binary tree, 18 chapters), Phase 7 (BST, 13 chapters), and three orphan chapters (AVL, RBT, treap). The mode flag selects per-node decorators — balance-factor labels for AVL, red/black fills for RBT, heap-priority badges for treap — while sharing one tree-layout core and one set of selections.

`mode:"binary"` and `mode:"bst"` are visually identical (the flag is a brand for chapter authors, not a renderer branch). `mode:"avl"` adds a `bf=N` text below each node. `mode:"rbt"` fills each node circle red or black based on the per-node `color` field. `mode:"treap"` adds a `p=N` priority text above each node.

Optional `sidebar` renders the recursion call-stack or an explicit iterator stack to the right of the tree — the heaviest user is the BST iterator chapter (forward + reverse iterators). Optional per-step `outputItems` renders a horizontal strip of cells below the tree showing the running traversal output (e.g. the inorder sequence growing one element per step).

Per-step `nodesOverride` mutates topology + node fields for insertion / rotation / recolour / value-copy animations; per-step `nodeStyles` is a lighter sidecar for traversal-mark passes ("after visiting node 5, mark it `visited`").

The secondary-tree layout for Phase 6 chapter 17 (simultaneous-traversal — two trees side-by-side, e.g. "are these two trees identical?") is listed in the widget spec but deferred — it doesn't block the bulk of the Phase 6/7 chapter migration, and the renderer's selection model assumes a single tree.

> **Source spec**: `docs/migration/widget-specs/binary-tree.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/BinaryTree.scala`
>
> **Marker canon**: [`MarkerCanon.scala`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala) — `current` = emerald, `parent` = slate, `successor`/`predecessor` = violet, `p`/`q` = amber/cyan (LCA queries), `lca`/`found` = emerald

## Payload schema (reference card)

```ts
{
  title:    string,
  mode:     "binary" | "bst" | "avl" | "rbt" | "treap",
  root:     string,                                  // id of the root node
  nodes:    [{
    id:             string,
    value:          string,
    left?:          string | null,                   // child node id; null/missing = no child
    right?:         string | null,
    color?:         "red" | "black",                 // rbt only
    balanceFactor?: number,                          // avl only (-2..2)
    priority?:      number,                          // treap only
    style?:         "new" | "removed" | "highlight" | "match" | "visited"
  }],
  sidebar?: {
    kind:   "call-stack" | "iterator-stack",
    label?: string                                   // section label above frames
  },
  steps: [{
    markers?:       [{ name: string, nodeId: string }],
    nodeStyles?:    { [nodeId: string]: "visited" | "highlight" | "removed" | "match" | "new" },
    nodesOverride?: [{ id, value?, left?, right?, color?, balanceFactor?, priority?, style? }],
    sidebarFrames?: [{ label: string, kind?: "active" | "returned" | "queued" }],
    outputItems?:   string[],                        // running traversal output
    root?:          string,                          // per-step root override — required for rotations
    msg:            string
  }]
}
```

**Required**: `title`, `mode`, `root`, `nodes`, `steps[].msg`.
**Optional**: everything per-step (`markers`, `nodeStyles`, `nodesOverride`, `sidebarFrames`, `outputItems`); `sidebar`; mode-specific decorators on nodes.

**Sidebar frame order**: `sidebarFrames[0]` is at the visual TOP (oldest frame, deepest in the call chain when you look up at where the recursion started); `sidebarFrames[last]` is at the visual BOTTOM (the most recently pushed / currently-active frame). This matches the recursion-stack convention "older calls above, latest below" — read the sidebar top-to-bottom to retrace the call chain from the outermost call down to the active one.

**Marker rule (carry-forward)**: each step lists exactly the markers it wants shown — the widget does not auto-inject `current` or any other pointer. A marker whose `nodeId` is not in the reachable tree is dropped silently (no rendering "into the void"). Multiple markers on the same node stack their labels vertically without overlap; the triangle is rendered for the first marker at each node only. Author-supplied `color` fields on markers are dropped at parse time — colours always come from [`MarkerCanon`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala).

**Reachability filter**: only nodes reachable from `root` via `left`/`right` are rendered. After a delete override removes a child link, the orphaned subtree exits with a fade-out — the spec's `nodes` array can still contain the dropped ids; they'll just be filtered out per step. This is how the BST-delete payload's deleted node disappears cleanly.

**`nodesOverride` semantics**: each override merges onto the base node:
- `value`/`color`/`balanceFactor`/`priority`/`style` — present overrides replace; absent keeps base.
- `left`/`right` — present-and-string sets; present-and-`null` clears; absent keeps base. This three-state is necessary to distinguish "rotation sets left to a new child" from "rotation breaks the left link entirely".
- An override targeting an id NOT in `spec.nodes` synthesises a fresh node (used for insertions appearing mid-payload).

**Per-step `root` override**: rotations (AVL / RBT / treap) change which node is the root — what used to be the root becomes a child of its former child. The step's `root` field tells the renderer which id to start the reachability walk + layout from for THIS step. Defaults to `spec.root` when absent (the common case — most steps don't move the root).

## Representative payloads

### Payload 1 — minimum (single-node tree)

The smallest meaningful tree. One node, one step. Exercises the renderer's basic node + viewBox sizing path and confirms the marker triangle/label renders cleanly above an isolated circle.

```d3 widget=binary-tree
{
  "title": "Single-node binary tree — root with no children",
  "mode": "binary",
  "root": "n1",
  "nodes": [{"id": "n1", "value": "5", "left": null, "right": null}],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n1"}], "msg": "Root 5 is the only node. left = right = null."}
  ]
}
```

### Payload 2 — typical (recursive inorder with sidebar call-stack + output strip)

Recursive inorder traversal of a six-node BST-shaped tree. Exercises four features simultaneously: the `current` marker tracking the cursor; per-step `nodeStyles` accumulating "visited" marks; the sidebar call-stack growing/shrinking as recursion enters and unwinds; the output strip below the tree accumulating the inorder sequence one element per step.

For a BST-shaped tree the inorder traversal yields `1, 3, 4, 5, 7, 9` — sorted — which is exactly what the output strip shows by the final step.

```d3 widget=binary-tree
{
  "title": "Recursive inorder traversal — visit left, self, right",
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
  "sidebar": {"kind": "call-stack", "label": "Recursion"},
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n5"}], "sidebarFrames": [{"label": "inorder(5)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(5). Recurse left."},
    {"markers": [{"name": "current", "nodeId": "n3"}], "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(3). Recurse left."},
    {"markers": [{"name": "current", "nodeId": "n1"}], "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)"}, {"label": "inorder(1)", "kind": "active"}], "outputItems": [], "msg": "Enter inorder(1). Left null → visit 1."},
    {"markers": [{"name": "current", "nodeId": "n1"}], "nodeStyles": {"n1": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)", "kind": "active"}], "outputItems": ["1"], "msg": "Append 1. Right null → return to inorder(3)."},
    {"markers": [{"name": "current", "nodeId": "n3"}], "nodeStyles": {"n1": "visited", "n3": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)", "kind": "active"}], "outputItems": ["1", "3"], "msg": "Visit 3 → output. Recurse right to 4."},
    {"markers": [{"name": "current", "nodeId": "n4"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(3)"}, {"label": "inorder(4)", "kind": "active"}], "outputItems": ["1", "3", "4"], "msg": "Visit 4. Return all the way back to inorder(5)."},
    {"markers": [{"name": "current", "nodeId": "n5"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited"}, "sidebarFrames": [{"label": "inorder(5)", "kind": "active"}], "outputItems": ["1", "3", "4", "5"], "msg": "Visit 5 → output. Recurse right to 7."},
    {"markers": [{"name": "current", "nodeId": "n9"}], "nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited", "n7": "visited", "n9": "visited"}, "sidebarFrames": [{"label": "inorder(5)"}, {"label": "inorder(7)"}, {"label": "inorder(9)", "kind": "active"}], "outputItems": ["1", "3", "4", "5", "7", "9"], "msg": "Visit 7 then 9. Output complete: 1, 3, 4, 5, 7, 9 (sorted, since the tree is a BST)."},
    {"nodeStyles": {"n1": "visited", "n3": "visited", "n4": "visited", "n5": "visited", "n7": "visited", "n9": "visited"}, "sidebarFrames": [], "outputItems": ["1", "3", "4", "5", "7", "9"], "msg": "All frames returned. Recursion done."}
  ]
}
```

### Payload 3 — typical (BST iterative search to a found node)

Iterative search down a seven-node BST for value 40. Exercises `mode:"bst"` and the `parent` trailing-pointer marker — the iterative-search algorithm carries a `parent` cursor so that on a not-found result, the caller knows where the value would be inserted. The marker pair `current`+`parent` is the canonical iterative-search vocabulary across the BST chapters.

```d3 widget=binary-tree
{
  "title": "BST iterative search for 40",
  "mode": "bst",
  "root": "n50",
  "nodes": [
    {"id": "n50", "value": "50", "left": "n30", "right": "n70"},
    {"id": "n30", "value": "30", "left": "n20", "right": "n40"},
    {"id": "n70", "value": "70", "left": "n60", "right": "n80"},
    {"id": "n20", "value": "20", "left": null, "right": null},
    {"id": "n40", "value": "40", "left": null, "right": null},
    {"id": "n60", "value": "60", "left": null, "right": null},
    {"id": "n80", "value": "80", "left": null, "right": null}
  ],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n50"}], "msg": "Start at root. Compare 40 < 50 → go left."},
    {"markers": [{"name": "current", "nodeId": "n30"}, {"name": "parent", "nodeId": "n50"}], "msg": "current = 30, parent = 50. Compare 40 > 30 → go right."},
    {"markers": [{"name": "current", "nodeId": "n40"}, {"name": "parent", "nodeId": "n30"}], "msg": "current = 40, parent = 30. Compare 40 == 40 → found."},
    {"markers": [{"name": "found", "nodeId": "n40"}], "nodeStyles": {"n40": "match"}, "msg": "Return current. The parent pointer would be the insertion site if the search had missed."}
  ]
}
```

### Payload 4 — edge / stress (BST delete two-children case via successor copy)

The hardest BST operation: deleting a node with two children. Standard algorithm — find the in-order successor (leftmost of right subtree), copy its value into the node being deleted, then remove the successor (which is now a leaf, easy case). Exercises three of the renderer's heaviest features at once: `nodesOverride` mutating values and links, the reachability filter dropping the deleted node, and topology that transitions smoothly across steps (the keyed-by-id node circles stay put; the keyed-by-`parent->child` edges fade out and back in as the link rewires).

No sidebar here — this is the iterative-delete variant where you don't track a recursion stack.

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
    {"markers": [{"name": "current", "nodeId": "n5"}], "msg": "Locate 5 → has two children → cannot simply remove it."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n7"}], "msg": "Walk into the right subtree to find the in-order successor (leftmost descendant)."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n6"}], "msg": "Successor cursor walks left → 6 is the leftmost. Successor = 6."},
    {"markers": [{"name": "current", "nodeId": "n5"}, {"name": "successor", "nodeId": "n6"}], "nodesOverride": [{"id": "n5", "value": "6", "style": "highlight"}], "msg": "Copy 6's value into the node being deleted. Node id stays — only the displayed value flips to 6."},
    {"markers": [{"name": "current", "nodeId": "n5"}], "nodesOverride": [{"id": "n5", "value": "6", "style": "highlight"}, {"id": "n7", "left": null}], "msg": "Break the link from 7 to the original 6. Reachability filter drops the original 6 next render."},
    {"nodesOverride": [{"id": "n5", "value": "6"}, {"id": "n7", "left": null}], "msg": "Final tree: 8 with left = 6, right = 10. BST invariant preserved (3 < 6 < 7 < 8 < 10)."}
  ]
}
```

### Payload 5 — advanced (AVL right-rotation restoring balance after left-left insert)

AVL mode — every node carries a `bf=N` balance-factor decorator below it. The starting tree is a left-skewed cascade (a fresh insert just dropped 10 in as a left-left grandchild of 30); the balance factors show 30 at -2 (imbalance), 20 at -1, 10 at 0. A right-rotation around 30 restores balance: 20 becomes the new root, 10 stays as its left child, 30 becomes its right child, and all balance factors zero out.

Exercises `mode:"avl"`, the balance-factor decorator, `nodesOverride` for both rotation topology (re-pointing links) and decorator values (new balance factors), and the in-order layout's natural transition from a left-skewed cascade to a balanced root-with-two-children shape.

```d3 widget=binary-tree
{
  "title": "AVL right-rotation around 30 — restore balance after left-left insert",
  "mode": "avl",
  "root": "n30",
  "nodes": [
    {"id": "n30", "value": "30", "left": "n20", "right": null, "balanceFactor": -2},
    {"id": "n20", "value": "20", "left": "n10", "right": null, "balanceFactor": -1},
    {"id": "n10", "value": "10", "left": null, "right": null, "balanceFactor": 0, "style": "new"}
  ],
  "steps": [
    {"markers": [{"name": "current", "nodeId": "n30"}], "msg": "After inserting 10, balance factor at 30 is -2. Left-left case → right-rotate around 30."},
    {"markers": [{"name": "current", "nodeId": "n20"}], "root": "n20", "nodesOverride": [{"id": "n20", "left": "n10", "right": "n30", "balanceFactor": 0}, {"id": "n30", "left": null, "right": null, "balanceFactor": 0}, {"id": "n10", "style": "highlight"}], "msg": "20 becomes the new root (per-step root override). 10 is its left, 30 (with its original null left/right) is its right. All balance factors back to 0."}
  ]
}
```

(Authoring an RBT chapter? Same shape — set `mode:"rbt"` and put `"color": "red"` or `"color": "black"` on each node. Treap chapter? `mode:"treap"` with `"priority": N` per node. Both render through the same renderer; the mode flag flips which decorator is drawn.)

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload for this widget, target **4–10 widget steps** (the source diagrams in the widget spec average 23–47 frames each → 8–10 widget steps). Compression strategy:

- **Frames 1–3 of source** (intro: tree appears, problem stated) → typically one "start" step with the initial marker placement.
- **Each recursion call / iteration step in source** → one widget step. The marker moving and the sidebar frame appearing/disappearing happen in the same step; the widget animates both via D3 transitions.
- **Frames showing the same state with different visual emphasis** (e.g. node re-coloured, label re-drawn) → merge into one step. The widget's keyed-by-id node + keyed-by-edge join handles "this is still the same node, only its style changed" without a payload entry.
- **Frames where two trees are compared side-by-side** (Phase 6 ch 17 simultaneous-traversal) → defer; the secondary-tree feature isn't built yet. Author single-tree payloads showing one side of the comparison, or wait for the secondary-tree extension.
- **Final 1–2 frames of source** (result / footer) → merge into one "result" step with the outcome in the `msg`. The output strip + sidebar both empty back out on this step to signal "done".

Example: the source recursive-inorder diagram for a 6-node tree is 31 frames; Payload 2 above compresses it to 9 steps (one per visit / one per fully-returned subtree). The 31 → 9 compression keeps every load-bearing pedagogical moment (visit, return, output append) without the cosmetic per-frame redraws.

For the **BST iterator** chapter (Phase 7 ch 09 — heaviest user of `sidebar:"iterator-stack"`), each iterator method call (`init`, `next`, `next`, ...) becomes one widget step; the sidebar frames are pushed/popped per step to mirror the iterator's state machine.

For **rotation** payloads (AVL / RBT / treap), the rotation itself is **one** widget step — the entire topology change (re-point three links + recompute balance factors / recolour) happens in a single `nodesOverride`. The reader watches all the moving parts animate together; splitting a rotation across two steps obscures the atomicity that makes rotations a single algorithmic operation.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-binary-tree` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm marker colours: `current` is emerald, `parent` is slate, `successor` is violet, `found` is emerald, in every payload where they appear.
3. Confirm Payload 2's sidebar grows and shrinks as recursion enters/unwinds; the most recently pushed frame (the `--active` one) is at the visual **bottom** of the stack; older callers are above it.
4. Confirm Payload 2's output strip accumulates one cell per step at the bottom of the SVG, finishing at `1, 3, 4, 5, 7, 9`.
5. Confirm Payload 4's deleted node (`n6`) visibly fades out when the link from `n7` to `n6` is broken in step 5; the keyed-by-id `n5` node circle stays put while its value text transitions from `5` to `6` (step 4).
6. Confirm Payload 5's tree shape transitions from the left-skewed cascade (30 top-right, 10 bottom-left) to the balanced root-with-two-children (20 centred, 10 left, 30 right) in a single Next click; the `bf=N` labels below each node update on the same transition.
7. Confirm no `.d3-widget__error` divs render in the page.
8. Confirm devtools console is clean: no widget exceptions; no marker-canonical-vocabulary warnings on these payloads (every marker name — `current`, `parent`, `successor`, `found` — is in the canon).

If any payload fails, fix and re-verify before committing.
