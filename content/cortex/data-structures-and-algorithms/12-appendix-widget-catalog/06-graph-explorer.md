---
title: graph-explorer
summary: Arbitrary graphs (directed or undirected, weighted or unweighted) animated by canonical traversal / shortest-path / topological-sort algorithms. Three layout modes (force / grid / dag); per-step node + edge state overlays with optional sidecar panels (dist[], queue, in-degree, visit order).
prereqs: []
---

# `graph-explorer`

## Purpose

A single widget covers all of source Phase 9 (Graph — 22 source diagrams across cycle detection, DFS, BFS, topological sort, Dijkstra, Bellman-Ford, Floyd-Warshall, connected components, two-colouring, grid traversal, grid shortest-path) plus the four advanced-graph orphan chapters (MST, SCC, bridges/articulation, 2-SAT) plus three real-systems DAG chapters (Git Merkle, Aho-Corasick goto, suffix automaton). The largest Tier 1 widget by far.

Three layout modes share one renderer:

- `layout: "force"` — generic graphs. Authors pin `(x, y)` per node for pedagogical stability — no live force simulation. 17 of the 22 source diagrams plus several orphans use this.
- `layout: "grid"` — grid-shaped graphs (chapter `05-traversing-a-grid` plus the two grid shortest-path chapters). Nodes carry `(row, col)`; the widget renders a faint grid backdrop and computes `(x, y)` from a fixed cell size.
- `layout: "dag"` — layered DAGs (topological sort, suffix automaton, Aho-Corasick goto, Git Merkle commit graph). Nodes carry `layer: Int`; the widget stacks vertically by layer (`orientation: "TB"`, default) or horizontally (`orientation: "LR"`).

Per-step `nodeStates` overlays a closed catalog of statuses — `unseen` / `frontier` / `active` / `settled` cover most traversal/shortest-path payloads; `white` / `gray` / `black` cover the directed-cycle-detection colouring; `colourA` / `colourB` cover the bipartite check; `component0..7` cover the connected-component colouring. Per-step `edgeStates` overlays `default` / `tree` / `back` / `cross` / `forward` / `relaxed` / `tight` / `on-path` / `considered` / `rejected` / `failure`. Both render as CSS modifiers — the palette lives in one place.

Optional `directed: true` swaps line edges for arrows (the widget creates one SVG `<marker>` per edge state so the head colour tracks the line colour). Optional `weighted: true` renders a weight label at each edge midpoint. Optional `panels` is an ordered list of sidecar tables (`kind: "array"` for `dist[]` / `prev[]` / in-degree counter; `kind: "list"` for queue / visit order / topological order) that update per step via `step.tables`.

Per-step `markers` are accepted in the schema but unused by the Arc 1 payloads — algorithms encode "the algorithm is here" via `nodeState: "active"` rather than a separate cursor. The marker slot stays in place so a future chapter that wants an explicit pointer overlay can add one without migrating the schema.

> **Source spec**: `docs/migration/widget-specs/graph-explorer.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/GraphExplorer.scala`
>
> **State catalogs**: node states + edge states are closed-set, validated at parse time. Unknown values fall back to `unseen` / `default` and log a console warning naming the canonical vocabulary.

## Payload schema (reference card)

```ts
{
  title:       string,
  layout:      "force" | "grid" | "dag",
  directed?:   boolean,                       // default false; renders arrowheads
  weighted?:   boolean,                       // default false; renders weight labels
  orientation?:"TB" | "LR",                   // dag only; default TB
  nodes: [{
    id:     string,
    label?: string,                           // defaults to id
    x?:     number, y?: number,               // force layout
    row?:   number, col?: number,             // grid layout
    layer?: number                            // dag layout
  }],
  edges: [{
    from:    string,                          // node id
    to:      string,                          // node id
    weight?: number                           // weighted mode only
  }],
  panels?: [{                                 // sidecar tables
    name:   string,                           // matches step.tables[name]
    kind:   "array" | "list",
    labels?:string[]                          // array kind only — header labels above cells
  }],
  steps: [{
    nodeStates?: { [nodeId: string]:
      "unseen"|"frontier"|"active"|"settled"
      | "white"|"gray"|"black"
      | "colourA"|"colourB"
      | "component0"|...|"component7"
    },
    edgeStates?: { [fromId-toId: string]:
      "default"|"tree"|"back"|"cross"|"forward"
      |"relaxed"|"tight"|"on-path"
      |"considered"|"rejected"|"failure"
    },
    tables?:     { [panelName: string]: string[] },
    markers?:    [{ name: string, nodeId: string }],
    msg:         string
  }]
}
```

**Required**: `title`, `layout`, `nodes` (non-empty), `steps` (non-empty), `steps[].msg`.
**Optional**: everything else. **No carry-forward**: each step lists exactly the states/tables it wants shown. A node id absent from `nodeStates` renders as `unseen`; an edge id absent from `edgeStates` renders as `default`; a panel name absent from `tables` renders empty cells. Always restate the `dist[]` / `prev[]` / `queue` etc. on every step they should be visible — even the "all done" final step needs the tables echoed back if you want them to stay on screen.

**Node-state rule**: each step lists exactly the node-states to apply for THIS step — there is no implicit "this node was settled, keep it settled". Authors enumerate per-step state so the transitions read explicitly. A node id that doesn't appear in `nodeStates` renders as `unseen`.

**Edge-state rule**: edge keys are `${from}-${to}` (matching the order in the `edges` array). For undirected graphs the renderer still distinguishes direction at the data layer; if your payload writes an edge as `{from: "0", to: "1"}` the state key is `"0-1"`.

**Edge id collision**: grid-layout node ids look like `"0,0"` and `"0,1"`, so edge ids like `"0,0-0,1"` are unambiguous in practice — the renderer treats them as opaque strings, never parses them. Authors don't need to escape the comma.

**State validation**: unknown `nodeStates` / `edgeStates` values fall back to the default modifier (`unseen` / `default`) AND log a console warning listing the canonical vocabulary. Typos are visible immediately.

**Panel `kind: "array"` cells** are keyed by column index — labels stay fixed across steps; only the cell values animate. **Panel `kind: "list"` cells** are keyed by `value#index` so identical values in different slots remain distinct DOM nodes; pills enter/exit with a fade as the list grows or shrinks.

## Representative payloads

### Payload 1 — minimum (force layout, undirected DFS)

The smallest meaningful traversal. Five nodes, four tree edges, two-panel sidecar (`stack`, `visited`). Exercises the renderer's basic node + edge + panel paths in `layout: "force"` mode without weighted edges or directed arrowheads — the foundation every other payload composes onto.

```d3 widget=graph-explorer
{
  "title": "DFS from node 0",
  "layout": "force",
  "directed": false,
  "nodes": [
    {"id": "0", "label": "0", "x":  60, "y": 120},
    {"id": "1", "label": "1", "x": 180, "y":  60},
    {"id": "2", "label": "2", "x": 180, "y": 180},
    {"id": "3", "label": "3", "x": 300, "y":  60},
    {"id": "4", "label": "4", "x": 300, "y": 180}
  ],
  "edges": [
    {"from": "0", "to": "1"},
    {"from": "0", "to": "2"},
    {"from": "1", "to": "3"},
    {"from": "2", "to": "4"}
  ],
  "panels": [
    {"name": "stack",   "kind": "list"},
    {"name": "visited", "kind": "list"}
  ],
  "steps": [
    {"nodeStates": {"0": "active"}, "tables": {"stack": ["0"], "visited": ["0"]}, "msg": "Push 0; visit 0."},
    {"nodeStates": {"0": "settled", "1": "active"}, "edgeStates": {"0-1": "tree"}, "tables": {"stack": ["0", "1"], "visited": ["0", "1"]}, "msg": "Recurse into 1."},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "active"}, "edgeStates": {"0-1": "tree", "1-3": "tree"}, "tables": {"stack": ["0", "1", "3"], "visited": ["0", "1", "3"]}, "msg": "Recurse into 3."},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "settled", "2": "active"}, "edgeStates": {"0-1": "tree", "1-3": "tree", "0-2": "tree"}, "tables": {"stack": ["0", "2"], "visited": ["0", "1", "3", "2"]}, "msg": "Pop 3 then 1; recurse into 2."},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "settled", "2": "settled", "4": "settled"}, "edgeStates": {"0-1": "tree", "1-3": "tree", "0-2": "tree", "2-4": "tree"}, "tables": {"stack": [], "visited": ["0", "1", "3", "2", "4"]}, "msg": "All nodes settled."}
  ]
}
```

### Payload 2 — typical (force + weighted + directed + array panels, Dijkstra)

The canonical Dijkstra walkthrough: four-node weighted directed-ish graph, two `array` panels (`dist[]`, `prev[]`) that update per step, edges that transition between `relaxed` (just considered) → `tight` (in the shortest-path subgraph) → `on-path` (final result). Exercises the heaviest features simultaneously: weighted edges with midpoint labels, arrowhead markers per edge state, two per-step array panels.

Final path `0 → 2 → 1 → 3` of cost 4, which is the spec's POC walkthrough.

```d3 widget=graph-explorer
{
  "title": "Dijkstra from node 0",
  "layout": "force",
  "directed": true,
  "weighted": true,
  "nodes": [
    {"id": "0", "label": "0", "x":  60, "y": 120},
    {"id": "1", "label": "1", "x": 200, "y":  60},
    {"id": "2", "label": "2", "x": 200, "y": 180},
    {"id": "3", "label": "3", "x": 340, "y": 120}
  ],
  "edges": [
    {"from": "0", "to": "1", "weight": 4},
    {"from": "0", "to": "2", "weight": 1},
    {"from": "1", "to": "3", "weight": 1},
    {"from": "2", "to": "1", "weight": 2},
    {"from": "2", "to": "3", "weight": 5}
  ],
  "panels": [
    {"name": "dist", "kind": "array", "labels": ["0", "1", "2", "3"]},
    {"name": "prev", "kind": "array", "labels": ["0", "1", "2", "3"]}
  ],
  "steps": [
    {"nodeStates": {"0": "active"}, "tables": {"dist": ["0", "inf", "inf", "inf"], "prev": ["-", "-", "-", "-"]}, "msg": "Init dist[0] = 0; the source is the only known cost."},
    {"nodeStates": {"0": "settled", "1": "frontier", "2": "frontier"}, "edgeStates": {"0-1": "relaxed", "0-2": "relaxed"}, "tables": {"dist": ["0", "4", "1", "inf"], "prev": ["-", "0", "0", "-"]}, "msg": "Settle 0; relax (0,1) and (0,2). Frontier = {1, 2}."},
    {"nodeStates": {"0": "settled", "1": "frontier", "2": "active"}, "edgeStates": {"0-2": "tight", "2-1": "relaxed", "2-3": "relaxed"}, "tables": {"dist": ["0", "3", "1", "6"], "prev": ["-", "2", "0", "2"]}, "msg": "Settle 2 (d=1); relax (2,1) — better path to 1 via 2; relax (2,3)."},
    {"nodeStates": {"0": "settled", "2": "settled", "1": "active"}, "edgeStates": {"0-2": "tight", "2-1": "tight", "1-3": "relaxed"}, "tables": {"dist": ["0", "3", "1", "4"], "prev": ["-", "2", "0", "1"]}, "msg": "Settle 1 (d=3); relax (1,3) — better path to 3 via 1."},
    {"nodeStates": {"0": "settled", "1": "settled", "2": "settled", "3": "settled"}, "edgeStates": {"0-2": "on-path", "2-1": "on-path", "1-3": "on-path"}, "tables": {"dist": ["0", "3", "1", "4"], "prev": ["-", "2", "0", "1"]}, "msg": "All settled. Shortest 0->3 = 4 via path 0 -> 2 -> 1 -> 3."}
  ]
}
```

### Payload 3 — typical (grid layout, BFS shortest path with wall)

A 3×3 grid with a wall at (1,1); BFS from start (0,0) to goal (2,2). Exercises `layout: "grid"` (faint backdrop, row/col positioning), the wall via deliberate edge omission (no edges incident to (1,1) in the payload), and a `list` panel showing the queue contents. Final `on-path` highlight traces 0,0 → 0,1 → 0,2 → 1,2 → 2,2, length 4.

```d3 widget=graph-explorer
{
  "title": "BFS shortest path on a 3x3 grid (0,0) -> (2,2), wall at (1,1)",
  "layout": "grid",
  "directed": false,
  "nodes": [
    {"id": "0,0", "label": "S", "row": 0, "col": 0},
    {"id": "0,1", "label": "",  "row": 0, "col": 1},
    {"id": "0,2", "label": "",  "row": 0, "col": 2},
    {"id": "1,0", "label": "",  "row": 1, "col": 0},
    {"id": "1,1", "label": "X", "row": 1, "col": 1},
    {"id": "1,2", "label": "",  "row": 1, "col": 2},
    {"id": "2,0", "label": "",  "row": 2, "col": 0},
    {"id": "2,1", "label": "",  "row": 2, "col": 1},
    {"id": "2,2", "label": "G", "row": 2, "col": 2}
  ],
  "edges": [
    {"from": "0,0", "to": "0,1"}, {"from": "0,1", "to": "0,2"},
    {"from": "0,0", "to": "1,0"}, {"from": "0,2", "to": "1,2"},
    {"from": "1,0", "to": "2,0"}, {"from": "1,2", "to": "2,2"},
    {"from": "2,0", "to": "2,1"}, {"from": "2,1", "to": "2,2"}
  ],
  "panels": [
    {"name": "queue", "kind": "list"}
  ],
  "steps": [
    {"nodeStates": {"0,0": "active"}, "tables": {"queue": ["(0,0)"]}, "msg": "Enqueue start. (1,1) — labelled X — has no incoming edges, so it never enters the frontier."},
    {"nodeStates": {"0,0": "settled", "0,1": "frontier", "1,0": "frontier"}, "edgeStates": {"0,0-0,1": "tree", "0,0-1,0": "tree"}, "tables": {"queue": ["(0,1)", "(1,0)"]}, "msg": "Visit (0,0); enqueue (0,1) and (1,0). Nothing routes through (1,1)."},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "frontier", "2,0": "frontier"}, "edgeStates": {"0,0-0,1": "tree", "0,0-1,0": "tree", "0,1-0,2": "tree", "1,0-2,0": "tree"}, "tables": {"queue": ["(0,2)", "(2,0)"]}, "msg": "Visit (0,1) and (1,0); enqueue their open neighbours."},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "settled", "2,0": "settled", "1,2": "frontier", "2,1": "frontier"}, "edgeStates": {"0,0-0,1": "tree", "0,0-1,0": "tree", "0,1-0,2": "tree", "1,0-2,0": "tree", "0,2-1,2": "tree", "2,0-2,1": "tree"}, "tables": {"queue": ["(1,2)", "(2,1)"]}, "msg": "Expand frontier; (1,2) and (2,1) are both one hop from settled cells."},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "settled", "2,0": "settled", "1,2": "settled", "2,1": "settled", "2,2": "settled"}, "edgeStates": {"0,0-0,1": "on-path", "0,1-0,2": "on-path", "0,2-1,2": "on-path", "1,2-2,2": "on-path"}, "tables": {"queue": []}, "msg": "Goal reached. Shortest path length 4: (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)."}
  ]
}
```

### Payload 4 — typical (dag layout, Kahn's topological sort)

Five-node DAG arranged in three layers; topological sort via Kahn's algorithm (process zero-in-degree nodes, decrement neighbours, repeat). Exercises `layout: "dag"` (auto-stack by `layer`), the `array` panel for the in-degree counter and TWO `list` panels (queue + order growing in lockstep). The `-` cells in the in-degree array mark already-processed nodes — same convention as the source diagram.

Final topological order is `A, B, C, D, E` — which the `order` panel accumulates one cell per step.

```d3 widget=graph-explorer
{
  "title": "Kahn's topological sort over a 5-node DAG",
  "layout": "dag",
  "directed": true,
  "orientation": "TB",
  "nodes": [
    {"id": "A", "label": "A", "layer": 0},
    {"id": "B", "label": "B", "layer": 0},
    {"id": "C", "label": "C", "layer": 1},
    {"id": "D", "label": "D", "layer": 1},
    {"id": "E", "label": "E", "layer": 2}
  ],
  "edges": [
    {"from": "A", "to": "C"},
    {"from": "A", "to": "D"},
    {"from": "B", "to": "D"},
    {"from": "C", "to": "E"},
    {"from": "D", "to": "E"}
  ],
  "panels": [
    {"name": "indeg", "kind": "array", "labels": ["A", "B", "C", "D", "E"]},
    {"name": "queue", "kind": "list"},
    {"name": "order", "kind": "list"}
  ],
  "steps": [
    {"nodeStates": {"A": "frontier", "B": "frontier"}, "tables": {"indeg": ["0", "0", "1", "2", "2"], "queue": ["A", "B"], "order": []}, "msg": "Compute in-degrees; enqueue zero-in-degree nodes A and B."},
    {"nodeStates": {"A": "settled", "B": "frontier", "C": "frontier"}, "edgeStates": {"A-C": "tree", "A-D": "tree"}, "tables": {"indeg": ["-", "0", "0", "1", "2"], "queue": ["B", "C"], "order": ["A"]}, "msg": "Dequeue A; decrement C (1->0, enqueue) and D (2->1, stays)."},
    {"nodeStates": {"A": "settled", "B": "settled", "C": "frontier", "D": "frontier"}, "edgeStates": {"A-C": "tree", "A-D": "tree", "B-D": "tree"}, "tables": {"indeg": ["-", "-", "0", "0", "2"], "queue": ["C", "D"], "order": ["A", "B"]}, "msg": "Dequeue B; decrement D (1->0, enqueue)."},
    {"nodeStates": {"A": "settled", "B": "settled", "C": "settled", "D": "settled", "E": "frontier"}, "edgeStates": {"A-C": "tree", "A-D": "tree", "B-D": "tree", "C-E": "tree", "D-E": "tree"}, "tables": {"indeg": ["-", "-", "-", "-", "0"], "queue": ["E"], "order": ["A", "B", "C", "D"]}, "msg": "Dequeue C then D; E's in-degree reaches 0 and enters the queue."},
    {"nodeStates": {"A": "settled", "B": "settled", "C": "settled", "D": "settled", "E": "settled"}, "edgeStates": {"A-C": "tree", "A-D": "tree", "B-D": "tree", "C-E": "tree", "D-E": "tree"}, "tables": {"indeg": ["-", "-", "-", "-", "-"], "queue": [], "order": ["A", "B", "C", "D", "E"]}, "msg": "Dequeue E. Done — topological order: A, B, C, D, E."}
  ]
}
```

### Payload 5 — edge (directed cycle detection via white / gray / black DFS)

Four-node directed graph with a 1 → 2 → 3 → 1 cycle. DFS colours each node `white` (unvisited) → `gray` (on the active recursion stack) → `black` (fully processed). A `back` edge into a `gray` node is the cycle witness. Exercises the white/gray/black palette (distinct from the unseen/frontier/active/settled traversal palette) plus the `back` edge style.

This is the canonical pedagogy for directed-cycle detection — the same five-step shape recurs in source's chapter `06-cycle-detection/03-understanding-cycle-detection-in-a-directed-graph.md`.

```d3 widget=graph-explorer
{
  "title": "Cycle detection in a directed graph (DFS white / gray / black)",
  "layout": "force",
  "directed": true,
  "nodes": [
    {"id": "0", "label": "0", "x":  60, "y":  60},
    {"id": "1", "label": "1", "x": 200, "y":  60},
    {"id": "2", "label": "2", "x": 200, "y": 180},
    {"id": "3", "label": "3", "x":  60, "y": 180}
  ],
  "edges": [
    {"from": "0", "to": "1"},
    {"from": "1", "to": "2"},
    {"from": "2", "to": "3"},
    {"from": "3", "to": "1"}
  ],
  "steps": [
    {"nodeStates": {"0": "white", "1": "white", "2": "white", "3": "white"}, "msg": "All nodes white. Start DFS from 0."},
    {"nodeStates": {"0": "gray", "1": "white", "2": "white", "3": "white"}, "msg": "Enter 0. Mark gray (on the recursion stack)."},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "white", "3": "white"}, "edgeStates": {"0-1": "tree"}, "msg": "Recurse 0 -> 1. Mark 1 gray."},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "white"}, "edgeStates": {"0-1": "tree", "1-2": "tree"}, "msg": "Recurse 1 -> 2. Mark 2 gray."},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "gray"}, "edgeStates": {"0-1": "tree", "1-2": "tree", "2-3": "tree"}, "msg": "Recurse 2 -> 3. Mark 3 gray."},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "gray"}, "edgeStates": {"0-1": "tree", "1-2": "tree", "2-3": "tree", "3-1": "back"}, "msg": "From 3, attempt edge to 1. 1 is gray (on the stack) -> back edge -> CYCLE detected."}
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload for this widget, target **5–10 widget steps** (the source diagrams in the widget spec average 30–80 frames each → 5–10 widget steps). Compression strategy:

- **Setup frames** (problem statement, graph appears, source marked) → one `init` step that establishes the starting node-states + any initial panel contents.
- **Each node visit / settle / dequeue** in source → one widget step. The node-state transition AND every panel mutation that happens in that step share the same widget step — the renderer animates both in one go.
- **Per-edge relaxation frames** in Dijkstra/Bellman-Ford source diagrams → fold multiple `relaxed` edges into one step when they all happen during processing of the same `active` node. Splitting per-edge makes the widget step count balloon.
- **Cycle-detection back-edge moment** → one explicit step where the `back` edge appears. Don't merge with the prior recursion step; the back edge is the pedagogical climax.
- **Final 1–2 frames of source** (result / footer / "all done") → one step that promotes the answer edges to `on-path` (shortest path) or empties the queue panel (topological sort done).

Example: the source Dijkstra implementation walkthrough (`08-single-source-shortest-path/03-implementing-dijkastras-algorithm.md`, 55 frames) compresses to 5–8 widget steps — one per settled node, plus the init + final. Payload 2 above is the same compression applied to a smaller graph.

For **Floyd-Warshall** (source 81 frames for the k-loop over a distance matrix), Arc 2 will add a `matrix` panel kind so the dist[] table updates 2D-style. Until then, port the algorithm as a `force`-layout payload that shows the *graph* with the current `k` highlighted, and add an `array` panel showing the row being updated.

For **grid traversal** (source 40-63 frames), preserve the "wall" cells in the `nodes` array even though they're never visited — the grid backdrop renders one cell per node, and missing nodes leave visible holes. The simplest "wall" idiom is to label the cell `X` and just not include any edges incident to it; the cell stays in the default `unseen` state across every step (Payload 3 above is the canonical example). If a future chapter wants a louder visual distinction, the `wall` node-state can be added to the canon.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-graph-explorer` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm Payload 1's edges fade in as the DFS tree grows; the `stack` and `visited` list panels accumulate pills with each step and the stack shrinks on the pop-pop-recurse transition (step 3 → step 4).
3. Confirm Payload 2's edge styles transition `relaxed` → `tight` → `on-path` correctly; the `dist[]` and `prev[]` cells animate their text contents in place (no full re-render); weight labels stay readable over the line strokes.
4. Confirm Payload 3's `(1,1)` wall (label `X`) is never enqueued — it has no incoming edges in the payload, so it stays in the default `unseen` state across every step; the queue panel grows and shrinks as BFS expands; the final `on-path` highlight traces the goal route.
5. Confirm Payload 4's DAG layout — A and B sit at the top, C and D in the middle, E at the bottom; the `indeg` cells flip to `-` as nodes are dequeued; the `queue` panel pulses (B added then drained); the `order` panel grows monotonically to `A, B, C, D, E`.
6. Confirm Payload 5's white-gray-black palette is visually distinct from the frontier/active/settled palette in other payloads; the `back` edge in the final step renders dashed-red and points from 3 back to 1.
7. Confirm no `.d3-widget__error` divs render in the page.
8. Confirm devtools console is clean: no widget exceptions; no nodeState/edgeState canonical-vocabulary warnings on these payloads. Node states used: `unseen` / `frontier` / `active` / `settled` (Payloads 1-4) and `white` / `gray` (Payload 5). Edge states used: `default` / `tree` / `back` / `relaxed` / `tight` / `on-path`. All canonical.

If any payload fails, fix and re-verify before committing.
