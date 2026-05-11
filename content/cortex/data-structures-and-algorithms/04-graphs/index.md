# Graphs

The most general data structure in this book. Every linked list is a graph. Every tree is a graph. Every state machine, every dependency, every road network, every social connection, every Git commit history — all graphs. Once you can traverse one cleanly and know which algorithm to reach for, a vast class of problems collapses into a few well-named patterns.

## Place in the curriculum

- **Prerequisites:** [Trees](/cortex/data-structures-and-algorithms/trees-index) (DFS on a tree generalises to DFS on a graph; the only new wrinkle is the visited-set), [Linear Structures](/cortex/data-structures-and-algorithms/linear-structures-index) (queues for BFS, stacks for iterative DFS, hash tables for visited-sets).
- **Followed by:** [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) (Dijkstra is greedy; Bellman-Ford is DP; Floyd-Warshall is DP).

## Chapters

1. [Introduction to Graphs](/cortex/data-structures-and-algorithms/graphs-introduction-to-graphs) — vertex, edge, directed vs undirected, weighted, dense vs sparse.
2. [Adjacency Matrix Representation](/cortex/data-structures-and-algorithms/graphs-adjacency-matrix-representation) — O(V²) space; O(1) edge query; the right pick for dense graphs.
3. [Adjacency List Representation](/cortex/data-structures-and-algorithms/graphs-adjacency-list-representation) — O(V+E) space; the default representation for almost everything.
4. [Traversing a Graph](/cortex/data-structures-and-algorithms/graphs-traversing-a-graph) — BFS and DFS from first principles.
5. [Traversing a Grid](/cortex/data-structures-and-algorithms/graphs-traversing-a-grid) — the implicit-graph trick: every grid problem is a graph problem.
6. [Cycle Detection](/cortex/data-structures-and-algorithms/graphs-cycle-detection) — colouring DFS for directed graphs; union-find for undirected.
7. [Topological Sort](/cortex/data-structures-and-algorithms/graphs-topological-sort) — Kahn's algorithm, DFS ordering; build-system and course-prerequisite ordering.
8. [Single-Source Shortest Path](/cortex/data-structures-and-algorithms/graphs-single-source-shortest-path) — BFS for unweighted, Dijkstra for non-negative-weighted, Bellman-Ford for arbitrary.
9. [All-Pairs Shortest Path](/cortex/data-structures-and-algorithms/graphs-all-pairs-shortest-path) — Floyd-Warshall.
10. [Max Flow / Min Cut Theorem](/cortex/data-structures-and-algorithms/graphs-max-flow-min-cut-theorem) — the duality every flow problem boils down to.
11. [Maximum Bipartite Matching](/cortex/data-structures-and-algorithms/graphs-maximum-bipartite-matching) — flow as matching; the Hopcroft-Karp speed-up.
12. [Minimum Spanning Trees](/cortex/data-structures-and-algorithms/graphs-minimum-spanning-trees) — Kruskal (sort + union-find), Prim (heap-based).
13. [Strongly Connected Components](/cortex/data-structures-and-algorithms/graphs-strongly-connected-components) — Tarjan's lowlink and Kosaraju's two-pass DFS.
14. [Bridges and Articulation Points](/cortex/data-structures-and-algorithms/graphs-bridges-and-articulation-points) — the lowlink trick again, this time on undirected graphs.
15. [2-SAT](/cortex/data-structures-and-algorithms/graphs-2-sat) — implication graphs and SCCs; one of the few NP problems with a polynomial special case.

### Patterns

- [Pattern: Depth-First Search](/cortex/data-structures-and-algorithms/graphs-pattern-depth-first-search)
- [Pattern: Connected Components](/cortex/data-structures-and-algorithms/graphs-pattern-connected-components)
- [Pattern: Two Colouring](/cortex/data-structures-and-algorithms/graphs-pattern-two-colouring)
- [Pattern: Shortest Path BFS](/cortex/data-structures-and-algorithms/graphs-pattern-shortest-path-breadth-first-search)
- [Pattern: Shortest Path Dijkstra](/cortex/data-structures-and-algorithms/graphs-pattern-shortest-path-dijkstra)
