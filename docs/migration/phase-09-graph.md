# Phase 9 — Graph

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/10.graph/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/04-graphs/`

## Stats

| | Count |
|---|---:|
| Chapters | 16 (source) / 20 (destination flat files) |
| Source lessons | 77 |
| Interactive diagrams | 21 |
| Destination size | 14,736 lines across 20 files |
| Indicative sessions | 12-15 |

Note: destination uses a flat structure (one `.md` per chapter, no
sub-directories). Source uses sub-directories. The mapping is
1:1 for the 16 source chapters; destination has 4 additional
chapters (likely topological-sort variants and bipartite matching)
that need to be cross-checked.

## Widget readiness — IMPORTANT

Graph algorithms (DFS, BFS, Dijkstra, MST, max-flow) need a
**`graph-traversal` widget** that doesn't exist. The
`array-traversal` widget can show the visited array / distance array
state but not the graph structure itself.

**Recommendation**: defer all interactive-diagram widget conversion
in this phase. Do code/example/problem-statement alignment now.

For the patterns that maintain a distance array or visited array
(Dijkstra's `dist[]`, BFS's `visited[]`), `array-traversal` could
show the *state evolution* of those arrays as a secondary
visualisation alongside a static graph diagram.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 9.1 | Introduction to Graphs | 4 | 2 | Vertex / edge / directed / weighted. |
| 9.2 | Adjacency Matrix Representation | 3 | 0 | 2D array; static diagram OK. |
| 9.3 | Adjacency List Representation | 6 | 0 | `Map<Vertex, List<Edge>>`. |
| 9.4 | Traversing a Graph | 4 | 2 | DFS + BFS basics. |
| 9.5 | Traversing a Grid | 5 | 2 | Grid as implicit graph. |
| 9.6 | Cycle Detection | 4 | 2 | Directed + undirected cycle detection. |
| 9.7 | Topological Sort | 4 | 1 | Kahn's + DFS-based. |
| 9.8 | Single-Source Shortest Path | 7 | 4 | Dijkstra, Bellman-Ford, BFS-on-unweighted. |
| 9.9 | All-Pairs Shortest Path | 3 | 1 | Floyd-Warshall. |
| 9.10 | Max-Flow Min-Cut Theorem | 5 | 0 | Ford-Fulkerson; concept-heavy. |
| 9.11 | Maximum Bipartite Matching | 4 | 0 | Hungarian / Hopcroft-Karp. |
| 9.12 | Pattern: Depth-First Search | 6 | 2 | DFS template + apps. |
| 9.13 | Pattern: Connected Components | 6 | 2 | DFS / Union-Find. |
| 9.14 | Pattern: Two Colouring | 6 | 2 | Bipartite check. |
| 9.15 | Pattern: Shortest Path BFS | 5 | 1 | Unweighted shortest path. |
| 9.16 | Pattern: Shortest Path Dijkstra | 5 | 1 | Weighted shortest path. |
| (destination only) | MST, SCC, Bridges-and-Articulation, 2-SAT | varies | varies | Cross-check against source's later chapters in `algorithms/` if available. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 9.1 Intro + Ch 9.2 Matrix + Ch 9.3 List (representation triple) |
| 2 | Ch 9.4 Traversing a Graph (DFS + BFS) |
| 3 | Ch 9.5 Traversing a Grid |
| 4 | Ch 9.6 Cycle Detection |
| 5 | Ch 9.7 Topological Sort |
| 6 | Ch 9.8 Single-Source Shortest Path — first half (Dijkstra) |
| 7 | Ch 9.8 finish + Ch 9.9 All-Pairs Shortest Path |
| 8 | Ch 9.10 Max Flow Min Cut |
| 9 | Ch 9.11 Bipartite Matching |
| 10 | Ch 9.12 Pattern: DFS |
| 11 | Ch 9.13 Connected Components |
| 12 | Ch 9.14 Two Colouring |
| 13 | Ch 9.15 + Ch 9.16 Shortest-path patterns |
| 14 | Destination-only chapters (MST, SCC, Bridges) — cross-check vs source |
| 15 | Phase verification + status |

## Session task templates

### Session 1 (Representations)

```
Phase 9 — Graph, Session 1.

Ch 9.1 Introduction + Ch 9.2 Adjacency Matrix + Ch 9.3 Adjacency
List. Align the basic graph operations: addEdge, addVertex,
neighbors, hasEdge. Source uses specific data-structure choices per
representation (2D int array for matrix; `Map<Integer, List<Integer>>`
for list in Java). Match exactly.

One commit per representation. Defer widgets.
```

### Sessions 2-13 (template)

```
Phase 9 — Graph, Session <N>.

Chapter <9.X> (<algorithm name>). Source's algorithm decomposition
typically includes:
- A main function (e.g., `dijkstra`, `topologicalSort`)
- One or more helpers (e.g., `dfsVisit`, `relaxEdge`)
- Standard data structures (PriorityQueue, Queue, visited set,
  distance array)

Destination must extract every helper. Match source's variable
names (`dist`, `parent`, `visited`, `inDegree`, etc.) exactly.

Defer graph-traversal widget conversion. One commit per problem.
```

### Session 14 (destination-only chapters)

```
Phase 9 — Graph, Session 14.

Destination has additional chapters not in source's main graph
directory:
- 17-minimum-spanning-trees.md
- 18-strongly-connected-components.md
- 19-bridges-and-articulation-points.md
- 20-2-sat.md

Check if source has these elsewhere (algorithms/) and align. If
source doesn't cover them, keep destination's existing content but
verify the code follows established conventions.
```

### Session 15 (closeout)

```
Phase 9 — Graph, Session 15 (closeout).

Phase verification + deferred-widgets status note pointing at the
future graph-traversal widget build.
```

## Gotchas

- **Directed vs undirected** — source treats them as separate
  topics. addEdge in undirected adds both `(u,v)` and `(v,u)`;
  directed adds only `(u,v)`. Match per problem.
- **Vertex 0-indexed vs 1-indexed** — different problems use
  different conventions. Match source per problem; don't normalise.
- **Dijkstra negative-weight handling** — source's Dijkstra
  PreCondition assumes non-negative weights; Bellman-Ford handles
  negatives. Keep this distinction explicit in destination prose.
- **Grid neighbour enumeration** — source uses `dr[] = {-1, 1, 0,
  0}, dc[] = {0, 0, -1, 1}` for 4-connectivity (or 8). Match the
  exact direction array.
- **Edge representation in PriorityQueue** — source typically uses
  `int[]{distance, vertex}` or `Pair(distance, vertex)`. Match the
  exact field order (distance first for natural ordering).
