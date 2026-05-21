# 6. Cycle detection

This lesson teaches you to answer one of the most common questions asked of graphs: **"is there a cycle?"** — and shows you why the answer is *one* algorithm for undirected graphs and a *different* algorithm for directed graphs, even though both look like DFS at first glance.

## Table of contents

1. [Why cycles matter](#why-cycles-matter)
2. [Cycle detection in an undirected graph](#cycle-detection-in-an-undirected-graph)
3. [Undirected — implementation](#undirected--implementation)
4. [Why directed graphs need a different rule](#why-directed-graphs-need-a-different-rule)
5. [Cycle detection in a directed graph](#cycle-detection-in-a-directed-graph)
6. [Directed — implementation](#directed--implementation)

***

# Why Cycles Matter

A **cycle** is a path that starts and ends at the same node without crossing any other node twice. Cycles aren't just an academic curiosity — they decide whether a piece of software actually works:

- **Build systems** (Make, Bazel, npm) refuse to compile when their dependency graph has a cycle. *"Module A imports B which imports A"* is unrunnable.
- **Spreadsheets** must reject `A1 = B1 + 1; B1 = A1 + 1` — a circular reference. Excel literally pops up an error.
- **Operating-system schedulers** detect cycles in resource-acquisition graphs to avoid deadlock.
- **Course planners** can't grant you a degree if the prerequisite graph has cycles.

So the question "does this graph have a cycle?" lives at the heart of dozens of real systems. The catch: the answer differs based on whether the graph is **undirected** or **directed**.

> *Before reading on — sketch a 4-node undirected graph and a 4-node directed graph. For each, find the smallest example with a cycle. Notice anything different about what counts as a cycle in each case?*

In an undirected graph, any cycle has at least 3 nodes (you can't form a cycle with just 2 — the edge would have to be both "there" and "back" simultaneously). In a directed graph, cycles can be tighter: even a single self-loop or a 2-node `A → B → A` qualifies. That structural difference is what forces two different algorithms.

***

# Cycle Detection in an Undirected Graph

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    subgraph Cyc["Has a cycle"]
      direction LR
      C0((0)) --- C1((1))
      C1 --- C2((2))
      C2 --- C3((3))
      C3 --- C0
    end
    subgraph Acyc["No cycle (a tree)"]
      direction LR
      A0((0)) --- A1((1))
      A0 --- A2((2))
      A1 --- A3((3))
    end
```

<p align="center"><strong>Two undirected graphs of 4 nodes. The left has the 4-cycle 0→1→2→3→0; the right is a tree.</strong></p>

The simplest possible rule for cycle detection in an undirected graph is also the right one:

> **During a DFS, if you reach a node you've already visited (and that node isn't the parent you just came from), there's a cycle.**

That's the entire algorithm. Run DFS, track which nodes you've seen, and when you cross an edge to a *non-parent* visited node, you've closed a loop.

---

## Why "Non-Parent"?

Without the parent exclusion, every undirected edge would falsely register as a cycle. Walk through it concretely:

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    A((A)) --- B((B))
```

A trivial 2-node, 1-edge graph. Run DFS from A:

1. Mark A visited.
2. Walk to B (A's neighbour). Mark B visited.
3. From B, look at neighbours. B's only neighbour is **A** — and A is already visited!
4. If we naively cried "cycle!", we'd be wrong: the only "loop" is just walking back across the same edge.

The fix is to remember which node we came *from* — A is B's parent in the DFS tree. When we look at B's neighbours, we ignore the one that's our parent. Now A→B and walking-back-to-A is silent. **A real cycle would require visiting a non-parent visited node.**

> *Before reading on — does the parent rule still work for a 3-cycle 0–1–2–0? Trace it once before scrolling.*

DFS from 0: visit 0. Visit 1 (parent=0). From 1's neighbours: 0 (parent → skip), 2 (unvisited → recurse). Visit 2 (parent=1). From 2's neighbours: 1 (parent → skip), 0 (visited, non-parent → **cycle!**). Yes — the rule fires exactly where we want.

---

## The Algorithm

> **`hasCycle(node, parent, graph, visited)`**
> 1. Mark `node` as visited.
> 2. For each `neighbour` in `graph[node]`:
>    - If `neighbour` is not visited, recursively call `hasCycle(neighbour, node, …)`. If it returns `true`, propagate `true`.
>    - Otherwise (neighbour is visited): if `neighbour != parent`, return `true` — a cycle.
> 3. Return `false`.
>
> **`detectCycleUndirected(graph)`**
> 1. Create empty `visited` set.
> 2. For each `node`: if not visited, call `hasCycle(node, -1, …)`. If it returns `true`, return `true`.
> 3. Return `false`.

The outer loop covers disconnected graphs — a cycle could live in a component that's unreachable from node 0.

---

## Proof of Correctness (Sketch)

Why is "non-parent visited neighbour" both *necessary* and *sufficient* for a cycle in an undirected graph?

**Sufficient direction.** If during DFS from `a` we reach `x` and find a non-parent visited neighbour `v`, then `v` was visited earlier in the *same* DFS tree (we'll defend that in a second). Both `v` and `x` are in the current DFS tree, so there's a tree path from `a → v` and a tree path from `a → x`. Joining them via the edge `x – v` closes a loop. Cycle.

**Why "same DFS tree"?** If `v` had been visited in some *earlier* DFS (a different connected component), then since `v` is connected to `x` via an undirected edge, that earlier DFS would have walked across the edge `v – x` and visited `x` too — contradicting our assumption that `x` was just being entered fresh.

**Necessary direction.** If a cycle exists, DFS from any of its nodes must eventually walk around the loop and bump into a visited non-parent. (DFS visits everything reachable; a cycle is reachable from every node on it.)

That's the whole proof. The key insight: **the parent check is the precise filter that distinguishes "I'm walking back across the edge I just came in on" from "I'm closing a loop"**.

***

# Undirected — Implementation


```python run
from typing import List, Set

class Solution:
    def has_cycle(
        self,
        graph: List[List[int]],
        node: int,
        parent: int,
        visited: Set[int],
    ) -> bool:

        # Mark the current node as visited in the graph to avoid
        # visiting it again
        visited.add(node)

        # Recursively visit all the adjacent nodes
        for neighbour in graph[node]:

            # If the neighbour node is not visited, visit it recursively
            if neighbour not in visited:
                if self.has_cycle(graph, neighbour, node, visited):
                    return True

            # If the neighbour node is already visited and is not the
            # parent node, a cycle is detected
            elif neighbour != parent:
                return True

        # No cycle detected
        return False

    def detect_cycle_in_undirected_graph(
        self, graph: List[List[int]]
    ) -> bool:

        # Set to keep track of visited nodes
        visited = set()

        # Perform DFS on each unvisited node
        for node in range(len(graph)):
            if node not in visited:
                if self.has_cycle(graph, node, -1, visited):
                    return True

        return False


# Examples from the problem statement
print(Solution().detect_cycle_in_undirected_graph([[1,2],[0,4],[0,3],[2,4],[1,3]]))  # True
print(Solution().detect_cycle_in_undirected_graph([[1],[0,2],[1]]))                  # False

# Edge cases
print(Solution().detect_cycle_in_undirected_graph([[]]))                             # False — single isolated node
print(Solution().detect_cycle_in_undirected_graph([[1],[0]]))                        # False — single edge, no cycle
print(Solution().detect_cycle_in_undirected_graph([[1,2],[0,2],[0,1]]))              # True — triangle
print(Solution().detect_cycle_in_undirected_graph([[1],[0],[3],[2]]))                # False — two disconnected edges
print(Solution().detect_cycle_in_undirected_graph([[0]]))                            # True — self-loop
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private boolean hasCycle(
            List<List<Integer>> graph,
            int node,
            int parent,
            Set<Integer> visited
        ) {

            // Mark the current node as visited in the graph to avoid
            // visiting it again
            visited.add(node);

            // Recursively visit all the adjacent nodes
            for (int neighbour : graph.get(node)) {

                // If the neighbour node is not visited, visit it recursively
                if (!visited.contains(neighbour)) {
                    if (hasCycle(graph, neighbour, node, visited)) {
                        return true;
                    }
                }

                // If the neighbour node is already visited and is not the
                // parent node, a cycle is detected
                else if (neighbour != parent) {
                    return true;
                }
            }

            // No cycle detected
            return false;
        }

        public boolean detectCycleInUndirectedGraph(
            List<List<Integer>> graph
        ) {

            // Set to keep track of visited nodes
            Set<Integer> visited = new HashSet<>();

            // Perform DFS on each unvisited node
            for (int node = 0; node < graph.size(); node++) {
                if (!visited.contains(node)) {
                    if (hasCycle(graph, node, -1, visited)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(1,2),List.of(0,4),List.of(0,3),List.of(2,4),List.of(1,3))));  // true
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(1),List.of(0,2),List.of(1))));            // false

        // Edge cases
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(new ArrayList<>())));                              // false — isolated node
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(1),List.of(0))));                         // false — single edge
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(1,2),List.of(0,2),List.of(0,1))));        // true — triangle
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(1),List.of(0),List.of(3),List.of(2))));   // false — two edges
        System.out.println(new Solution().detectCycleInUndirectedGraph(
            List.of(List.of(0))));                                     // true — self-loop
    }
}
```


## Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(N + E) | Each node is visited at most once; each edge is examined at most twice (once per endpoint) |
| **Space** | O(N) | Visited set + recursion stack are both bounded by N |

The early-return on cycle detection means we may terminate well before O(N + E) in practice — but worst-case (a long acyclic chain) we examine the whole graph.

***

# Why Directed Graphs Need a Different Rule

Try to apply the undirected algorithm to this directed graph:

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    A((A)) --> B((B))
    A --> C((C))
    B --> D((D))
    C --> D
```

<p align="center"><strong>A "diamond" DAG. No cycle exists, but a naive DFS will see <code>D</code> twice.</strong></p>

DFS from A: visit A → B → D. Backtrack. Try A → C → **D — already visited!** A naive "visited node = cycle" rule would falsely declare a cycle. But there isn't one — D was just reached by a *different* path from A. The diamond is acyclic.

The undirected parent trick doesn't save us either, because the *direction* of the edges matters: in a directed graph, the path A → C → D wasn't going "back across" any edge we just came in on.

So we need a stricter rule: **a cycle exists only when DFS reaches a node that's currently on the stack — i.e. on the path from the DFS root to the current node**, not just any visited node.

---

## Three States Per Node

The cleanest way to capture this distinction is to give each node *one of three states* during DFS:

| State | Meaning |
|---|---|
| **White / unvisited** | DFS hasn't seen this node yet |
| **Grey / in current path** | DFS entered this node and hasn't finished it yet — it's on the recursion stack |
| **Black / fully done** | DFS has finished this node and all its descendants |

```d2
direction: right

states: "Three node states during DFS" {
  grid-rows: 3
  grid-columns: 1
  grid-gap: 0
  white: |md
    **WHITE** — never visited
  |
  grey: |md
    **GREY** — entered, not yet finished (on the call stack)
  |
  black: |md
    **BLACK** — fully processed (popped off the stack)
  |
}
```

<p align="center"><strong>Each node passes through White → Grey → Black exactly once. Hitting a Grey neighbour during DFS is the unmistakable signature of a cycle.</strong></p>

The rule is now sharp: **if you find a directed edge to a Grey node, you've closed a loop**. Hitting a Black node is fine — that's just the diamond case (a node finished long ago, on a different path).

In practice we don't usually maintain three explicit colours; we maintain two sets — `visited` (= grey ∪ black) and `nodesInPath` (= grey). A node is "grey" if it's in `nodesInPath`. We *add* to `nodesInPath` on entry and *remove* on exit; that's how a node transitions Grey → Black.

> *Before reading on — for the diamond DAG above, walk through the DFS and write down each node's state at each step. Where does each transition happen?*

Steps for the diamond:

| Step | Action | A | B | C | D |
|---|---|---|---|---|---|
| 1 | enter A | grey | white | white | white |
| 2 | enter B | grey | grey | white | white |
| 3 | enter D from B | grey | grey | white | grey |
| 4 | leave D (no neighbours) | grey | grey | white | **black** |
| 5 | leave B | grey | **black** | white | black |
| 6 | enter C | grey | black | grey | black |
| 7 | from C, look at D — D is **black**, not grey → no cycle | grey | black | grey | black |
| 8 | leave C, leave A | **black** | black | **black** | black |

No grey hit ⇒ no cycle. Now repeat the trace with an extra edge `D → A`:

| Step | Action | A | B | C | D |
|---|---|---|---|---|---|
| 1 | enter A | grey | white | white | white |
| 2 | enter B | grey | grey | white | white |
| 3 | enter D | grey | grey | white | grey |
| 4 | from D, look at A — A is **grey** → **cycle!** | | | | |

Grey hit ⇒ cycle. The state machine catches the difference between "diamond" and "loop" perfectly.

***

# Cycle Detection in a Directed Graph

The algorithm:

> **`hasCycle(node, graph, visited, nodesInPath)`**
> 1. Mark `node` as visited.
> 2. Add `node` to `nodesInPath` (= turn it grey).
> 3. For each `neighbour` in `graph[node]`:
>    - If `neighbour` is in `nodesInPath` → return `true` (cycle).
>    - Else if `neighbour` is not visited → recurse; if it returns `true`, propagate.
> 4. Remove `node` from `nodesInPath` (= turn it black).
> 5. Return `false`.
>
> **`detectCycleDirected(graph)`**
> 1. Create empty `visited` and `nodesInPath` sets.
> 2. For each `node`: if not visited, call `hasCycle`. If true, return true.
> 3. Return `false`.

The crucial line is `nodesInPath.remove(node)` at step 4 — the **back-tracking** step that flips a finished node from Grey to Black. Without it, every visited node would stay grey forever and the diamond would falsely register as a cycle.

***

# Directed — Implementation


```python run
from typing import List, Set

class Solution:
    def has_cycle(
        self,
        graph: List[List[int]],
        node: int,
        visited: Set[int],
        nodes_in_path: Set[int],
    ) -> bool:

        # Mark the current node as visited in the graph to avoid
        # visiting it again
        visited.add(node)

        # Insert the current node into the set of nodes in the current
        # path to detect cycles
        nodes_in_path.add(node)

        # Recursively visit all the adjacent nodes
        for neighbour in graph[node]:

            # If the neighbour node is not visited, visit it recursively
            if neighbour not in visited:
                if self.has_cycle(
                    graph, neighbour, visited, nodes_in_path
                ):
                    return True

            # If the neighbour node is already visited and present in
            # the current path, a cycle is detected
            elif neighbour in nodes_in_path:
                return True

        # Remove the current node from the current path as we are done
        # exploring it
        nodes_in_path.remove(node)

        # No cycle detected
        return False

    def detect_cycle_in_directed_graph(
        self, graph: List[List[int]]
    ) -> bool:

        # Set to keep track of visited nodes
        visited: Set[int] = set()

        # Set to keep track of nodes in the current path
        nodes_in_path: Set[int] = set()

        # Perform DFS on each unvisited node
        for node in range(len(graph)):
            if node not in visited:
                if self.has_cycle(graph, node, visited, nodes_in_path):
                    return True

        return False


# Examples from the problem statement
print(Solution().detect_cycle_in_directed_graph([[1,2],[4],[3],[0],[2,3]]))  # True
print(Solution().detect_cycle_in_directed_graph([[4],[5],[3],[5],[1],[]]))   # False

# Edge cases
print(Solution().detect_cycle_in_directed_graph([[]]))                       # False — isolated node
print(Solution().detect_cycle_in_directed_graph([[1],[0]]))                  # False — no directed cycle
print(Solution().detect_cycle_in_directed_graph([[1],[2],[0]]))              # True — 3-node cycle
print(Solution().detect_cycle_in_directed_graph([[0]]))                      # True — self-loop
print(Solution().detect_cycle_in_directed_graph([[1],[]]))                   # False — one edge, no cycle
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private boolean hasCycle(
            List<List<Integer>> graph,
            int node,
            Set<Integer> visited,
            Set<Integer> nodesInPath
        ) {

            // Mark the current node as visited in the graph to avoid
            // visiting it again
            visited.add(node);

            // Insert the current node into the set of nodes in the current
            // path to detect cycles
            nodesInPath.add(node);

            // Recursively visit all the adjacent nodes
            for (int neighbour : graph.get(node)) {

                // If the neighbour node is not visited, visit it recursively
                if (!visited.contains(neighbour)) {
                    if (hasCycle(graph, neighbour, visited, nodesInPath)) {
                        return true;
                    }
                }

                // If the neighbour node is already visited and present in
                // the current path, a cycle is detected
                else if (nodesInPath.contains(neighbour)) {
                    return true;
                }
            }

            // Remove the current node from the current path as we are done
            // exploring it
            nodesInPath.remove(node);

            // No cycle detected
            return false;
        }

        public boolean detectCycleInDirectedGraph(
            List<List<Integer>> graph
        ) {

            // Set to keep track of visited nodes
            Set<Integer> visited = new HashSet<>();

            // Set to keep track of nodes in the current path
            Set<Integer> nodesInPath = new HashSet<>();

            // Perform DFS on each unvisited node
            for (int node = 0; node < graph.size(); node++) {
                if (!visited.contains(node)) {
                    if (hasCycle(graph, node, visited, nodesInPath)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(1,2),List.of(4),List.of(3),List.of(0),List.of(2,3))));  // true
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(4),List.of(5),List.of(3),List.of(5),List.of(1),new ArrayList<>())));  // false

        // Edge cases
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(new ArrayList<>())));                              // false
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(1),List.of(0))));                         // false — no directed cycle
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(1),List.of(2),List.of(0))));              // true — 3-node cycle
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(0))));                                     // true — self-loop
        System.out.println(new Solution().detectCycleInDirectedGraph(
            List.of(List.of(1),new ArrayList<>())));                   // false
    }
}
```


<details>
<summary><strong>Trace — graph = [[1], [2], [0]] (a 3-cycle)</strong></summary>

```
Step │ Stack      │ Action                                  │ visited   │ nodes_in_path
─────┼────────────┼─────────────────────────────────────────┼───────────┼───────────────
1    │ dfs(0)     │ enter 0; mark visited+nodes_in_path     │ {0}       │ {0}
2    │ dfs(1)     │ neighbour 1 unvisited; recurse          │ {0,1}     │ {0,1}
3    │ dfs(2)     │ neighbour 2 unvisited; recurse          │ {0,1,2}   │ {0,1,2}
4    │ dfs(2)     │ from 2, neighbour 0 IN nodes_in_path!   │           │
       │            │ → return true (cycle)                   │           │
Result: true ✓
```

</details>

## Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(N + E) | Each node visited at most once; each edge examined at most once |
| **Space** | O(N) | Visited and nodesInPath sets each hold up to N entries; recursion stack at most N deep |

The complexity is identical to the undirected case — the directed version simply tracks an extra bit (`nodesInPath`) per node.

---

## Final Takeaway

The two algorithms differ in *exactly* one place:

- **Undirected:** "visited and not parent" → cycle.
- **Directed:** "in current DFS path" → cycle.

Both ride on top of a standard DFS. Both handle disconnected graphs the same way. Once you internalise the *reason* the rules differ — undirected edges are bidirectional and need a parent guard; directed edges aren't and need the path-membership test — you'll never confuse them.

Cycle detection is also the gateway to one of the most useful directed-graph algorithms in existence: **topological sort**, which orders nodes such that every edge points "forward" — and which is only possible *if and only if* the graph has no cycles. That's the next lesson.

> **Transfer challenge.** You have a build system with 1 000 packages and 5 000 dependency edges. A circular import is reported. Write the 4-line algorithm sketch (no language) that locates not just *whether* there's a cycle but *which packages* are in it. *(Hint: when you detect a cycle, the path from the cycle node back to itself is sitting on the DFS stack.)*

<details>
<summary><strong>Sketch</strong></summary>

1. Run the directed-graph DFS. Maintain a `parent[]` array recording, for each grey node, who DFS came from.
2. When the DFS finds an edge `cur → grey_node`, you have a cycle.
3. Walk `parent[]` backwards from `cur` until you re-encounter `grey_node` — collecting nodes as you go.
4. Reverse the collected list and prepend `grey_node` to get the cycle in path order.

Same algorithm as detection — only the bookkeeping changes.

</details>
