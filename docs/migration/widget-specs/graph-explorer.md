# Widget Spec — `graph-explorer`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

A single widget that renders an arbitrary graph (directed or undirected,
weighted or unweighted) and animates the canonical traversal/shortest-path
algorithms over it. One widget covers **all** of source Phase 9 (Graph) plus
the four advanced-graph orphan chapters plus several real-systems chapters
that visualise DAGs (Git Merkle, Aho-Corasick goto-graph, suffix automaton).

Compressed step semantics: 5-10 meaningful frames per algorithm (e.g.
Dijkstra: init → relax edges around node 0 → settle node 0 → relax around
node X → settle node X → … → done). Source's 30-80 frame sequences get
collapsed to the pedagogical milestones.

The widget owns three concerns the simpler list/array widgets don't have:

1. **Layout** — graphs need an `(x, y)` per node. The widget supports three
   layout modes (see §4).
2. **Edge styling** — visited / frontier / on-path / relaxed edges all need
   distinct visual treatments; weighted graphs render weight labels on
   edges.
3. **Auxiliary panels** — most algorithms have a sidecar table (distance
   array, predecessor array, colour map, in-degree counter, visit order)
   that updates per step. The widget renders an optional `tables` panel
   next to the graph.

## 2. Source-diagram inventory

All sources are under
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/10.graph/`.
22 interactive diagrams total in source Phase 9:

| Source file | Line | Frames | What |
|---|---:|---:|---|
| `01-introduction-to-graphs/02-exploring-a-possible-solution.md` | 29 | 6 | Find minimum number of hops between two cities (BFS intro) |
| `01-introduction-to-graphs/02-exploring-a-possible-solution.md` | 47 | 13 | Maximum flights for budget 600 (weighted DAG exploration) |
| `04-traversing-a-graph/01-understanding-depth-first-traversal.md` | 55 | 40 | DFS in a graph |
| `04-traversing-a-graph/03-understanding-breadth-first-traversal.md` | 78 | 48 | BFS starting from node 1 |
| `05-traversing-a-grid/02-understanding-depth-first-traversal-on-a-grid.md` | 58 | 55 | DFS on a grid |
| `05-traversing-a-grid/04-understanding-breadth-first-traversal-on-a-grid.md` | 69 | 63 | BFS on a grid |
| `06-cycle-detection/01-understanding-cycle-detection-in-an-undirected-graph.md` | 67 | 27 | DFS to detect cycle (undirected) |
| `06-cycle-detection/03-understanding-cycle-detection-in-a-directed-graph.md` | 76 | 38 | DFS to detect cycle (directed, with white/gray/black colours) |
| `07-topological-sort/02-understanding-the-topological-sort-algorithm.md` | 53 | 49 | Topological order via Kahn's (in-degree zero queue) |
| `08-single-source-shortest-path/01-understanding-single-source-shortest-path-problem.md` | 39 | 8 | BFS finds shortest path in unweighted graphs |
| `08-single-source-shortest-path/02-understanding-dijkstras-algorithm.md` | 49 | 41 | Dijkstra (conceptual) |
| `08-single-source-shortest-path/03-implementing-dijkastras-algorithm.md` | 39 | 55 | Dijkstra (implementation walkthrough with dist[] panel) |
| `08-single-source-shortest-path/06-understanding-the-bellman-ford-algorithm.md` | 47 | 49 | Bellman-Ford relaxation rounds |
| `09-all-pairs-shortest-path/02-understanding-floyd-warshall-algorithm.md` | 30 | 81 | Floyd-Warshall k-loop over distance matrix |
| `12-pattern-depth-first-search/01-understanding-the-depth-first-search-pattern.md` | 57 | 50 | Aggregate all paths from node 0 to node 2 via f/g |
| `12-pattern-depth-first-search/02-identifying-the-depth-first-search-pattern.md` | 31 | 35 | Find all paths from node 0 to node 2 |
| `13-pattern-connected-components/01-understanding-the-connected-component-pattern.md` | 62 | 49 | Aggregate values in connected components |
| `13-pattern-connected-components/02-identifying-the-connected-component-pattern.md` | 37 | 52 | Collect nodes per connected component |
| `14-pattern-two-colouring/01-understanding-the-two-coloring-pattern.md` | 95 | 28 | Bipartite check via BFS two-colouring |
| `14-pattern-two-colouring/02-identifying-the-two-coloring-pattern.md` | 39 | 41 | Is the graph bipartite? |
| `15-pattern-shortest-path-breadth-first-search/01-identifying-breadth-first-search.md` | 43 | 65 | Shortest path (0,0)→(2,4) via BFS on grid |
| `16-pattern-shortest-path-dijkstra/01-identifying-dijkstras-algorithm-for-shortest-path.md` | 55 | 52 | Minimum-cost path (0,0)→(2,2) via Dijkstra on grid |

Plus orphan chapters and reuses with **no** source diagrams (payloads
authored from destination prose):

- `04-graphs/17-minimum-spanning-trees.md` — Kruskal's growing forest + Prim's frontier
- `04-graphs/18-strongly-connected-components.md` — Kosaraju (two DFS passes) / Tarjan (low-link)
- `04-graphs/19-bridges-and-articulation-points.md` — DFS tree + back edges + low-link
- `04-graphs/20-2-sat.md` — implication graph + SCC reduction
- `07-strings/07-suffix-automaton.md` — state machine as DAG (`layout: "dag"`)
- `07-strings/08-aho-corasick.md` — goto + failure transitions on trie-shaped DAG
- `11-dsa-in-real-systems/04-git-merkle-dag.md` — commit DAG with parent pointers

## 3. Destination chapter usage

| Destination chapter | Diagrams to host | Layout / mode |
|---|---:|---|
| `04-graphs/01-introduction-to-graphs.md` | 2 | `force` — generic city graph |
| `04-graphs/04-traversing-a-graph.md` | 2 | `force` — DFS + BFS |
| `04-graphs/05-traversing-a-grid.md` | 2 | `grid` — DFS + BFS on grid |
| `04-graphs/06-cycle-detection.md` | 2 | `force` — undirected + directed (with white/gray/black overlay) |
| `04-graphs/07-topological-sort.md` | 1 | `dag` — Kahn's queue + in-degree panel |
| `04-graphs/08-single-source-shortest-path.md` | 4 | `force` — BFS / Dijkstra (×2) / Bellman-Ford with dist[] + prev[] panels |
| `04-graphs/09-all-pairs-shortest-path.md` | 1 | `force` for graph + matrix panel for Floyd-Warshall |
| `04-graphs/12-pattern-depth-first-search.md` | 2 | `force` — path-aggregation traversal |
| `04-graphs/13-pattern-connected-components.md` | 2 | `force` — component colouring |
| `04-graphs/14-pattern-two-colouring.md` | 2 | `force` — bipartite colour check |
| `04-graphs/15-pattern-shortest-path-breadth-first-search.md` | 1 | `grid` — BFS shortest path |
| `04-graphs/16-pattern-shortest-path-dijkstra.md` | 1 | `grid` — Dijkstra shortest path |
| `04-graphs/17-minimum-spanning-trees.md` | 2 (orphan) | `force` — Kruskal / Prim |
| `04-graphs/18-strongly-connected-components.md` | 2 (orphan) | `force` — Kosaraju (with reverse-graph view) / Tarjan low-link panel |
| `04-graphs/19-bridges-and-articulation-points.md` | 1 (orphan) | `force` — DFS tree overlay |
| `04-graphs/20-2-sat.md` | 1 (orphan) | `dag` — implication graph + SCC mode |
| `07-strings/07-suffix-automaton.md` | 1 (orphan) | `dag` — suffix-link DAG |
| `07-strings/08-aho-corasick.md` | 1 (orphan) | `dag` — goto + failure-link overlay |
| `11-dsa-in-real-systems/04-git-merkle-dag.md` | 1 (orphan) | `dag` — commit graph with parent edges |

**Layout-mode enumeration:**

- `layout: "force"` — used for **all** generic graph diagrams (cycle
  detection, traversal, MST, SCC, bridges, 2-SAT, connected components,
  two-colouring, weighted shortest-path on generic graphs). 17 of the 22
  source diagrams + several orphans.
- `layout: "grid"` — used for grid-shaped graphs where rows/cols are the
  natural layout (chapter `05-traversing-a-grid`, the two grid shortest-path
  pattern chapters). 4 of the 22 source diagrams.
- `layout: "dag"` — topological-sort, suffix automaton, Aho-Corasick goto
  graph, Git Merkle commit graph. Layer-based positioning (Sugiyama-style or
  longest-path leveling); orientation `LR` or `TB`. 1 source diagram +
  4 orphans.

## 4. Payload schema sketch

The schema is wide because graphs need more shape than a list or array,
but the surface is structured so authors typically supply only the
fields relevant to their algorithm.

```jsonc
{
  "title": "Dijkstra from source node 0",
  "layout": "force",          // "force" | "grid" | "dag"
  "directed": true,           // default false; renders arrowheads on edges
  "weighted": true,           // default false; renders weight labels

  // Layout-specific positioning input. Authors pin x/y in force mode for
  // pedagogical stability (no jitter between page loads); grid mode infers
  // from row/col; dag mode infers from layer.
  "nodes": [
    {"id": "0", "label": "0", "x": 100, "y": 100},
    {"id": "1", "label": "1", "x": 220, "y": 60,  "row": 0, "col": 1},
    {"id": "2", "label": "2", "x": 220, "y": 160, "layer": 1}
  ],

  "edges": [
    {"from": "0", "to": "1", "weight": 4},
    {"from": "0", "to": "2", "weight": 1},
    {"from": "2", "to": "1", "weight": 2}
  ],

  // Optional sidecar table descriptors. Each updates per step from the
  // step's `tables` override; the widget renders them to the right of the
  // graph as labelled mini-tables.
  "panels": [
    {"name": "dist",  "kind": "array", "labels": ["0","1","2"]},
    {"name": "prev",  "kind": "array", "labels": ["0","1","2"]},
    {"name": "queue", "kind": "list"}
  ],

  "steps": [
    {
      // Per-node overlay state. Canonical statuses (closed catalog):
      //   "unseen"   — default
      //   "frontier" — in the queue / open set (Dijkstra, BFS)
      //   "active"   — currently being processed (top of stack / queue front)
      //   "settled"  — distance finalised (Dijkstra) or finished (DFS post-order)
      //   "white" | "gray" | "black"  — directed-cycle-detection colours
      //   "colourA" | "colourB"  — two-colouring (bipartite) labels
      //   "componentN" — connected-component index (N is 0..k-1)
      "nodeStates": {
        "0": "settled",
        "1": "frontier",
        "2": "active"
      },
      // Per-edge overlay. Canonical statuses:
      //   "default" | "tree" | "back" | "cross" | "forward" | "relaxed"
      //   | "tight" | "on-path" | "considered" | "rejected" | "failure"
      "edgeStates": {
        "0-1": "tree",
        "0-2": "relaxed",
        "2-1": "on-path"
      },
      // Optional table-cell content for this step. Keys match `panels[].name`.
      "tables": {
        "dist":  ["0", "3", "1"],       // array cells
        "prev":  ["-", "2", "0"],
        "queue": ["1"]                  // list contents
      },
      "msg": "Settle node 2 (d=1); relax (2→1) so dist[1] = 1+2 = 3"
    }
  ]
}
```

### Layout-specific fields

- **`force`** — nodes carry `(x, y)`; widget renders at the given coords
  with edge curves. No live force simulation (deterministic for screenshots).
- **`grid`** — nodes carry `(row, col)`; widget computes `(x, y)` from a
  fixed cell size; edges connect orthogonal neighbours by default but
  arbitrary edges are allowed.
- **`dag`** — nodes carry `layer` (Int); widget vertically stacks by layer
  with auto x-spread within layer; orientation defaults to `TB` (top-to-bottom)
  with `orientation: "LR"` opt-in.

## 5. POC payloads

### 5.1 Force layout — DFS traversal

````d3 widget=graph-explorer
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
  "panels": [{"name": "stack", "kind": "list"}, {"name": "visited", "kind": "list"}],
  "steps": [
    {"nodeStates": {"0": "active"}, "tables": {"stack": ["0"], "visited": ["0"]}, "msg": "Push 0; visit 0"},
    {"nodeStates": {"0": "settled", "1": "active"}, "edgeStates": {"0-1": "tree"}, "tables": {"stack": ["0", "1"], "visited": ["0", "1"]}, "msg": "Recurse into 1"},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "active"}, "edgeStates": {"0-1": "tree", "1-3": "tree"}, "tables": {"stack": ["0", "1", "3"], "visited": ["0", "1", "3"]}, "msg": "Recurse into 3"},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "settled", "2": "active"}, "edgeStates": {"0-1": "tree", "1-3": "tree", "0-2": "tree"}, "tables": {"stack": ["0", "2"], "visited": ["0", "1", "3", "2"]}, "msg": "Pop 3 then 1; recurse into 2"},
    {"nodeStates": {"0": "settled", "1": "settled", "3": "settled", "2": "settled", "4": "settled"}, "edgeStates": {"0-1": "tree", "1-3": "tree", "0-2": "tree", "2-4": "tree"}, "tables": {"stack": [], "visited": ["0", "1", "3", "2", "4"]}, "msg": "All nodes settled"}
  ]
}
````

### 5.2 Force + weighted — Dijkstra with dist[] panel

````d3 widget=graph-explorer
{
  "title": "Dijkstra from node 0",
  "layout": "force",
  "directed": false,
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
    {"nodeStates": {"0": "active"}, "tables": {"dist": ["0", "inf", "inf", "inf"], "prev": ["-", "-", "-", "-"]}, "msg": "Init dist[0] = 0"},
    {"nodeStates": {"0": "settled", "1": "frontier", "2": "frontier"}, "edgeStates": {"0-1": "relaxed", "0-2": "relaxed"}, "tables": {"dist": ["0", "4", "1", "inf"], "prev": ["-", "0", "0", "-"]}, "msg": "Settle 0; relax (0,1) and (0,2)"},
    {"nodeStates": {"0": "settled", "1": "frontier", "2": "active"}, "tables": {"dist": ["0", "3", "1", "6"], "prev": ["-", "2", "0", "2"]}, "edgeStates": {"0-2": "tight", "2-1": "relaxed", "2-3": "relaxed"}, "msg": "Settle 2 (d=1); relax (2,1) so dist[1] = 3; relax (2,3)"},
    {"nodeStates": {"0": "settled", "2": "settled", "1": "active"}, "tables": {"dist": ["0", "3", "1", "4"], "prev": ["-", "2", "0", "1"]}, "edgeStates": {"0-2": "tight", "2-1": "tight", "1-3": "relaxed"}, "msg": "Settle 1 (d=3); relax (1,3) so dist[3] = 4"},
    {"nodeStates": {"0": "settled", "1": "settled", "2": "settled", "3": "settled"}, "edgeStates": {"0-2": "on-path", "2-1": "on-path", "1-3": "on-path"}, "msg": "All settled; shortest path 0→2→1→3 of cost 4"}
  ]
}
````

### 5.3 Grid layout — BFS on grid

````d3 widget=graph-explorer
{
  "title": "BFS shortest path from (0,0) to (2,2)",
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
    {"from": "0,0", "to": "1,0"}, {"from": "0,1", "to": "1,1"}, {"from": "0,2", "to": "1,2"},
    {"from": "1,0", "to": "2,0"}, {"from": "1,2", "to": "2,2"},
    {"from": "2,0", "to": "2,1"}, {"from": "2,1", "to": "2,2"}
  ],
  "panels": [{"name": "queue", "kind": "list"}],
  "steps": [
    {"nodeStates": {"0,0": "active"}, "tables": {"queue": ["(0,0)"]}, "msg": "Enqueue start"},
    {"nodeStates": {"0,0": "settled", "0,1": "frontier", "1,0": "frontier"}, "edgeStates": {"0,0-0,1": "tree", "0,0-1,0": "tree"}, "tables": {"queue": ["(0,1)", "(1,0)"]}, "msg": "Visit (0,0); enqueue neighbours"},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "frontier", "2,0": "frontier"}, "edgeStates": {"0,0-0,1": "tree", "0,0-1,0": "tree", "0,1-0,2": "tree", "1,0-2,0": "tree"}, "tables": {"queue": ["(0,2)", "(2,0)"]}, "msg": "Skip (1,1) wall; enqueue (0,2) and (2,0)"},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "settled", "2,0": "settled", "1,2": "frontier", "2,1": "frontier"}, "tables": {"queue": ["(1,2)", "(2,1)"]}, "msg": "Expand frontier"},
    {"nodeStates": {"0,0": "settled", "0,1": "settled", "1,0": "settled", "0,2": "settled", "2,0": "settled", "1,2": "settled", "2,1": "settled", "2,2": "settled"}, "edgeStates": {"0,0-0,1": "on-path", "0,1-0,2": "on-path", "0,2-1,2": "on-path", "1,2-2,2": "on-path"}, "msg": "Reach goal; path length 4"}
  ]
}
````

### 5.4 DAG layout — Topological sort via Kahn's

````d3 widget=graph-explorer
{
  "title": "Kahn's topological sort",
  "layout": "dag",
  "directed": true,
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
    {"tables": {"indeg": ["0", "0", "1", "2", "2"], "queue": ["A", "B"], "order": []}, "msg": "Compute in-degrees; enqueue zero-degree nodes"},
    {"nodeStates": {"A": "settled"}, "edgeStates": {"A-C": "tree", "A-D": "tree"}, "tables": {"indeg": ["-", "0", "0", "1", "2"], "queue": ["B", "C"], "order": ["A"]}, "msg": "Dequeue A; decrement C and D"},
    {"nodeStates": {"A": "settled", "B": "settled"}, "edgeStates": {"A-C": "tree", "A-D": "tree", "B-D": "tree"}, "tables": {"indeg": ["-", "-", "0", "0", "2"], "queue": ["C", "D"], "order": ["A", "B"]}, "msg": "Dequeue B; decrement D"},
    {"nodeStates": {"A": "settled", "B": "settled", "C": "settled", "D": "settled"}, "edgeStates": {"A-C": "tree", "A-D": "tree", "B-D": "tree", "C-E": "tree", "D-E": "tree"}, "tables": {"indeg": ["-", "-", "-", "-", "0"], "queue": ["E"], "order": ["A", "B", "C", "D"]}, "msg": "Dequeue C then D; E becomes ready"},
    {"nodeStates": {"A": "settled", "B": "settled", "C": "settled", "D": "settled", "E": "settled"}, "tables": {"queue": [], "order": ["A", "B", "C", "D", "E"]}, "msg": "Done — topological order A, B, C, D, E"}
  ]
}
````

### 5.5 Force — Directed cycle detection (white/gray/black)

````d3 widget=graph-explorer
{
  "title": "Cycle detection in a directed graph (DFS colours)",
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
    {"nodeStates": {"0": "white", "1": "white", "2": "white", "3": "white"}, "msg": "All nodes white"},
    {"nodeStates": {"0": "gray", "1": "white", "2": "white", "3": "white"}, "msg": "Enter 0 (gray)"},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "white", "3": "white"}, "edgeStates": {"0-1": "tree"}, "msg": "Recurse 0 → 1 (gray)"},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "white"}, "edgeStates": {"0-1": "tree", "1-2": "tree"}, "msg": "Recurse 1 → 2 (gray)"},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "gray"}, "edgeStates": {"0-1": "tree", "1-2": "tree", "2-3": "tree"}, "msg": "Recurse 2 → 3 (gray)"},
    {"nodeStates": {"0": "gray", "1": "gray", "2": "gray", "3": "gray"}, "edgeStates": {"0-1": "tree", "1-2": "tree", "2-3": "tree", "3-1": "back"}, "msg": "Back edge 3 → 1 lands on gray — CYCLE detected"}
  ]
}
````

## 6. Closest existing widget to mimic

`LinkedList` is the structural parent: keyed node + edge selections,
per-step rebind with smooth transforms for nodes that survive across steps,
canonical status enum for marker colours. Reuse the
`Stepper` hook for the play loop, `PayloadDecoder` for the JSON envelope,
and the same `ensureSvg` / single update-pass pattern.

`ArrayTraversal`'s secondary-row machinery is the conceptual cousin of the
`panels` sidecar: a per-step optional data block that updates in lockstep
with the main canvas.

## 7. D3 selections plan

Two top-level D3 groups: `g.graph-explorer__canvas` for the graph,
`g.graph-explorer__panels` for the sidecar tables.

Inside the canvas:

- **Edges** (drawn first so nodes sit on top): `selectAll("path.graph-explorer__edge").data(edges, e => `${e.from}-${e.to}`)`
  - Enter: fade in (`opacity 0 → 1`, 200ms).
  - Update: re-bind class string for the current step's edge state.
  - Exit: fade out + remove.
  - Weight labels: separate `selectAll("text.graph-explorer__edge-weight")` keyed on the same id.
- **Nodes**: `selectAll("g.graph-explorer__node").data(nodes, n => n.id)`
  - Enter: append `<g>` with `<circle>` + `<text>`, position via `transform: translate(x, y)`.
  - Update: transition `transform` (450ms ease-cubic) so layout shifts animate; rebind class for nodeState.
- **Edge arrowheads** (directed mode): SVG `<defs><marker>` reused per edge type (default / tree / back / on-path).

Inside the panel group, one D3 sub-selection per panel descriptor:

- **`kind: "array"`** — keyed row of cells with labels above, values inside; cells animate value changes via text-tween.
- **`kind: "list"`** — horizontal stack of pill cells with enter/exit transitions (queue / order / stack visualisations).
- **`kind: "matrix"`** (added later for Floyd-Warshall) — 2D grid of cells with k-row + k-column highlight per step.

Layout-mode dispatch happens in a single `layoutNode(spec, node)` function
that returns `(x, y)` per mode:

- `force` → `(node.x, node.y)` straight through.
- `grid` → `(originX + col * cellW, originY + row * cellH)`.
- `dag` → `(originX + intraLayerOffset, originY + layer * layerH)` for `TB`,
  swapped for `LR`.

## 8. Shared abstractions

Already shared (reuse as-is):

- `Stepper.hook` — play/pause/scrub state machine.
- `PayloadDecoder` — JSON envelope + typed `js.Dynamic` readers.

Candidate new shared abstractions raised by this widget (decide during
Arc 2 hardening — don't extract speculatively in Arc 1):

- **Edge-curve helpers** — cubic Bezier between two points with a control
  offset to handle parallel edges (forward + reverse) without overlap. Will
  recur in `decision-tree`, `dp-table` top-down mode (recursion arrows), and
  `call-stack` recursion-tree mode. Live in a future
  `widgets/internal/EdgeCurve.scala` if extracted.
- **DAG / layered layout** — auto-x-spread within layer given `layer` plus
  optional `order` hints. Reused in `dp-table` top-down (recursion tree),
  `decision-tree`, `call-stack` recursion-tree mode, `trie`. Live in a
  future `widgets/internal/LayeredLayout.scala`.
- **Panel renderer** — `array` / `list` / `matrix` sidecar tables. Likely
  reused by `dp-table`, `call-stack`, `hash-table`. Defer extraction until
  the second consumer lands.

## 9. Estimated build session count

**2 sessions.** One for the core widget (force + grid layouts, all canonical
node/edge states, weighted edges, the `array` and `list` panel kinds), one
for `dag` layout + the matrix panel (Floyd-Warshall) + canon validation +
demo chapter. Largest Tier-1 widget by far — the schema breadth and the
three layout modes push it above the 1-session bar.

## 10. POC chapter

Demo book chapter at
`content/cortex/dsa-widget-catalog/03-graph-explorer.md` exhibits all
five POC payloads above, plus a Floyd-Warshall payload with the matrix panel
and an undirected MST (Kruskal) payload with edges flipping from
`considered` to `tree` or `rejected`.

The first real production usage is `04-graphs/04-traversing-a-graph.md`
(DFS + BFS, both `force` layout) — chosen because it's the simplest pair
that exercises node states (`unseen` / `frontier` / `active` / `settled`)
without weighted edges or panels. Validating the widget there before tackling
the Dijkstra/Bellman-Ford chapters keeps the first chapter migration honest.
