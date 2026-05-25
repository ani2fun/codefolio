---
title: "Pattern: Shortest Path (Dijkstra)"
summary: "Extend BFS with a min-heap keyed on cumulative cost — greedily settle the cheapest unvisited vertex until the target is reached."
prereqs:
  - 04-graphs/08-single-source-shortest-path
---

# When BFS Isn't Enough — and Dijkstra Is

BFS gives you the shortest path in *number of hops*. Dijkstra gives you the shortest path in *cumulative weight*. The difference is whether each edge counts as 1 or as a varying real cost.

A grid where every cell is 1 step? BFS. A grid where each cell has a different toll cost? Dijkstra.

> 🖼 Diagram — The two siblings. Same shape — repeatedly visit the "closest" unvisited node — but "closest" means depth in BFS and weighted-distance in Dijkstra.
```d2
direction: right

bfs: "BFS — fewest hops" {
  grid-rows: 1
  grid-columns: 1
  grid-gap: 0
  l: |md
    Every step costs 1.

    First time you reach a node = shortest distance (in hops).

    FIFO queue is enough.
  |
}

dij: "Dijkstra — minimum cumulative weight" {
  grid-rows: 1
  grid-columns: 1
  grid-gap: 0
  l: |md
    Steps have varying cost.

    First time you POP a node from a min-heap = shortest weighted distance.

    Min-heap (priority queue) is required.
  |
}
```

<p align="center"><strong>The two siblings. Same shape — repeatedly visit the "closest" unvisited node — but "closest" means depth in BFS and weighted-distance in Dijkstra.</strong></p>

The Dijkstra pattern shows up wherever:

- **Tolls / fees / costs** vary across edges.
- **Travel times** change per route.
- **Resource consumption** differs by transition.
- **Reliability scores** stack.

The constraint: **all edge weights must be ≥ 0**. Negative weights break Dijkstra (we covered why in lesson 8) — for those, you'd reach for Bellman-Ford.

> *Before reading on — for a graph with weights 1, 1, 1, 1, would Dijkstra and BFS produce the same answer? What about for weights 1, 2, 3, 4?*

For uniform weights, yes — Dijkstra degenerates to BFS, just slower (log-factor heap overhead). For varying weights, BFS would give the wrong answer in cumulative cost — it'd return the path with fewest edges, not lowest cost. Dijkstra is the right tool the moment any weight varies.

# The Dijkstra-Pattern Template

The skeleton:

```
dijkstra(graph, source):
    distance = [∞] * N
    distance[source] = 0
    heap = min-heap of (distance, node) pairs, seeded with (0, source)
    while heap not empty:
        (d, node) = heappop(heap)
        if d > distance[node]: continue            # stale entry
        for each (neighbour, weight) in graph[node]:
            new_dist = d + weight
            if new_dist < distance[neighbour]:
                distance[neighbour] = new_dist
                heappush(heap, (new_dist, neighbour))
    return distance
```

Key elements that *every* Dijkstra-pattern problem will share:

1. **Min-heap keyed by cumulative cost.** First out = node with lowest known cost so far.
2. **Distance array, initialised to infinity.** Final values = answers.
3. **Lazy stale-entry skip.** Updates push new entries instead of decreasing keys; stale ones are filtered by `if d > distance[node]: continue`.
4. **Weight-aware relaxation.** `if d + weight < distance[neighbour]` is the line that does the real work.

For grid-flavoured Dijkstra (most interview problems), the graph is implicit — neighbours come from a direction array, not an adjacency list.

# Identifying the Pattern

The signal-words:

- *"Minimum cost / time / distance / fuel / weight / total"*
- *"Shortest path"* with **edge weights**
- *"Cheapest flight"*, *"fastest route"*, *"least toll"*
- *"Minimise [some additive quantity]"* on a graph or grid

If the problem is unweighted (or all weights equal), use BFS. If the graph has negative weights, use Bellman-Ford. Otherwise: **Dijkstra**.

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Pattern — missing, needs to be written -->
<!--       Guidance: umbrella H2 with the subsections below -->

<!-- TODO: Why Naive Isn't Enough — missing, needs to be written -->
<!--       Guidance: motivation for why the obvious approach fails -->

<!-- TODO: The Core Idea — missing, needs to be written -->
<!--       Guidance: one paragraph: the central trick -->

<!-- TODO: How the Pointers/Window Move — missing, needs to be written -->
<!--       Guidance: mechanics of the moving parts -->

<!-- TODO: The Generic Algorithm — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Generic Implementation — missing, needs to be written -->
<!--       Guidance: Python block + Java block of the skeleton -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table -->

<!-- TODO: Variants / Taxonomy — missing, needs to be written -->
<!--       Guidance: enumerate sub-shapes of this pattern -->

<!-- TODO: Recognition Checklist — missing, needs to be written -->
<!--       Guidance: 4-question diagnostic — the source of the Problem-section Diagnostic Questions -->

<!-- TODO: Canonical Example — missing, needs to be written -->
<!--       Guidance: fully worked example: brute force → optimised → template fit -->

<!-- TODO: Problems in This Category — missing, needs to be written -->
<!--       Guidance: table with links to the 02-problems/ files -->
