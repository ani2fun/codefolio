---
title: "Traversing A Grid"
summary: "<!-- TODO: summary -->"
---

# 5. Traversing a grid

This lesson teaches you to **see grids as graphs** — and once you do, every grid problem you'll ever face becomes a slightly disguised version of DFS or BFS.

## Table of contents

1. [The graph hiding in every grid](#the-graph-hiding-in-every-grid)
2. [DFS on a grid](#dfs-on-a-grid)
3. [DFS implementation](#dfs-implementation)
4. [BFS on a grid](#bfs-on-a-grid)
5. [BFS implementation](#bfs-implementation)

***

# The Graph Hiding in Every Grid

Open any list of "common DSA problems" and a startling fraction look like grid problems: number of islands, flood fill, rotting oranges, shortest path in a maze, word search, count enclaves, surrounded regions, walls and gates… The list goes on for pages.

But here's the secret: **none of these are really grid problems**. They're graph problems wearing a 2D costume. Once you see how to translate, every grid problem in existence collapses into the two algorithms you already know — DFS and BFS.

> *Before reading on — sketch a 3×3 grid and label its cells (0,0) through (2,2). Now think: from (1,1), how many neighbours does it have? From (0,0), how many? You're already thinking like a graph.*

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
    subgraph G1["A graph"]
      direction LR
      A((A)) --- B((B))
      A --- C((C))
      B --- C
      B --- D((D))
      C --- E((E))
      D --- E
    end
    subgraph G2["A grid"]
      direction LR
      C00(("(0,0)")) --- C01(("(0,1)"))
      C01 --- C02(("(0,2)"))
      C00 --- C10(("(1,0)"))
      C01 --- C11(("(1,1)"))
      C02 --- C12(("(1,2)"))
      C10 --- C11
      C11 --- C12
    end
```

<p align="center"><strong>Both pictures are graphs. The right one happens to have a regular structure where every node connects only to its grid neighbours — but the abstraction is identical.</strong></p>

The translation rule is dead simple:

- **Each cell becomes a node.** Its identity is `(row, col)` instead of an integer.
- **Each adjacency becomes an edge.** Most often: a cell is adjacent to its 4 cardinal neighbours (up, down, left, right). Some problems add the 4 diagonals for 8-directional movement.
- **Cell value is the per-node payload.** Often a binary flag: `1` means "walkable", `0` means "blocked", or land vs water, or healthy vs infected.

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
flowchart TB
    Center(("(r, c)")) --> Up(("(r-1, c)"))
    Center --> Down(("(r+1, c)"))
    Center --> Left(("(r, c-1)"))
    Center --> Right(("(r, c+1)"))
```

<p align="center"><strong>From any cell <code>(r, c)</code>, the 4 cardinal neighbours are computed by ±1 on row or column.</strong></p>

The thing that *looks* novel about grids is **we never build an adjacency list**. The cell coordinates *are* the IDs and the neighbours can be **computed on demand** from `(r ± 1, c)` and `(r, c ± 1)`. No pre-processing, no matrix, no adjacency list. The graph is implicit, encoded in the geometry itself.

That implicit graph still runs DFS and BFS — only the "get neighbours" step changes. Everything else (visited tracking, disconnected components, complexity) is identical to what you learned last lesson.

***

# DFS on a Grid

Take a 4×5 grid where `1` means walkable and `0` means blocked. The goal: starting from any walkable cell, visit every walkable cell connected to it (cardinally), then move on to the next unvisited walkable cell, until the entire grid is processed.

```d2
direction: right

grid: "Input grid (1 = walkable, 0 = blocked)" {
  grid-rows: 4
  grid-columns: 5
  grid-gap: 0
  c00: "1"
  c01: "1"
  c02: "0"
  c03: "1"
  c04: "0"
  c10: "1"
  c11: "1"
  c12: "0"
  c13: "0"
  c14: "0"
  c20: "0"
  c21: "0"
  c22: "0"
  c23: "1"
  c24: "1"
  c30: "0"
  c31: "0"
  c32: "1"
  c33: "1"
  c34: "0"
}
```

<p align="center"><strong>A 4×5 input grid. The 1s form three disconnected "islands"; DFS visits each island in turn, the way it visits each component of a disconnected graph.</strong></p>

The shape of the algorithm is *exactly* the disconnected-graph DFS from the previous lesson:

```
depthFirstTraversal(grid):
    visited = 2D bool array, all false
    for each cell (r, c) in grid:
        if grid[r][c] == 1 and not visited[r][c]:
            dfs(r, c, ...)
```

The recursive helper:

```
dfs(r, c, grid, visited):
    mark visited[r][c]
    record (r, c)
    for each (dr, dc) in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
        nr, nc = r + dr, c + dc
        if (nr, nc) is in-bounds and grid[nr][nc] == 1 and not visited[nr][nc]:
            dfs(nr, nc, ...)
```

The crucial new line is the **direction array** — a list of `(row delta, col delta)` pairs — and the `(nr, nc)` computation that turns "neighbour" into a small calculation rather than a list lookup.

---

## The Direction Array — Don't Hardcode Four `if`s

Hand-rolling four separate `if` statements for up/down/left/right is the most common beginner mistake on grid problems. The direction array eliminates duplication and makes 8-directional moves a one-line change.

```d2
direction: right

dirs: "Direction deltas" {
  grid-rows: 1
  grid-columns: 4
  grid-gap: 0
  d_up: |md
    **up**

    (-1, 0)
  |
  d_right: |md
    **right**

    (0, 1)
  |
  d_down: |md
    **down**

    (1, 0)
  |
  d_left: |md
    **left**

    (0, -1)
  |
}

cell: "(r, c) + delta = (r+dr, c+dc)" {
  grid-rows: 1
  grid-columns: 1
  grid-gap: 0
  c: |md
    From (1, 2):

    + (-1, 0) → (0, 2)

    + ( 0, 1) → (1, 3)

    + ( 1, 0) → (2, 2)

    + ( 0,-1) → (1, 1)
  |
}
```

<p align="center"><strong>One direction array, one loop, four neighbours. Adding diagonals later is just adding 4 more pairs to the array.</strong></p>

> **Memory trick — read the deltas as compass arrows.** `(-1, 0)` is "row decreases" = up. `(0, +1)` is "column increases" = right. Once that mental link clicks, the array is no longer arbitrary numbers.

---

## Bounds Checking — The Other Common Bug

Unlike a real graph, a grid has **edges of the world**. From `(0, 0)` the "up" neighbour `(-1, 0)` is off the map. Every neighbour computation must check:

```
0 <= nr < rows  AND  0 <= nc < cols
```

If you forget, you get `IndexError` (Python), `ArrayIndexOutOfBoundsException` (Java), or — worst — a silent crash because of negative indexing wrapping around in some languages.

A clean pattern: a tiny `is_valid_cell(grid, r, c)` helper that bundles bounds + walkable into one check.

> *Before reading on — for a 5×5 grid, how many of the 4 neighbours are valid for cell (0, 0)? For (2, 2)? For (4, 4)? Predict before scrolling.*

Cell `(0,0)`: only `(0,1)` and `(1,0)` are in bounds — 2 valid. Cell `(2,2)`: all four are in bounds — 4 valid. Cell `(4,4)`: only `(3,4)` and `(4,3)` are in bounds — 2 valid. Corners get 2, edges get 3, interiors get 4.

***

# DFS Implementation

The full algorithm in Python and Java.


```python run viz=grid viz-root=grid
from typing import List, Tuple

class Solution:
    def is_valid_cell(
        self, grid: List[List[int]], row: int, col: int
    ) -> bool:

        # Check if a cell is valid and belongs to a region of 1's, also
        # check that the cell is not water
        return (
            row >= 0
            and row < len(grid)
            and col >= 0
            and col < len(grid[0])
            and grid[row][col] == 1
        )

    def dfs(
        self,
        grid: List[List[int]],
        row: int,
        col: int,
        visited: List[List[bool]],
        result: List[Tuple[int, int]],
    ) -> None:

        # Mark the current cell as visited
        visited[row][col] = True

        # Add the current cell to the result
        result.append((row, col))

        # Define the possible movements: all 8 directions (up, right, 
        # down, left, and diagonals)
        directions: List[Tuple[int, int]] = [
            (-1, 0),  # up
            (0, 1),   # right
            (1, 0),   # down
            (0, -1)   # left
        ]

        for dr, dc in directions:
            new_row = row + dr
            new_col = col + dc

            # If the neighbour is not visited, recursively call the DFS
            # function on the neighbour
            if (
                self.is_valid_cell(grid, new_row, new_col)
                and not visited[new_row][new_col]
            ):
                self.dfs(grid, new_row, new_col, visited, result)

    def depth_first_traversal_on_a_grid(
        self, grid: List[List[int]]
    ) -> List[Tuple[int, int]]:
        rows = len(grid)

        # If the grid is empty, return an empty result
        if rows == 0:
            return []

        cols = len(grid[0])

        # Initialize a list to store the result of the DFS
        # which will contain the coordinates of the cells visited
        # during the DFS traversal
        result: List[Tuple[int, int]] = []

        # Initialize visited array
        visited = [[False] * cols for _ in range(rows)]

        # Traverse each cell of the grid
        for row in range(rows):
            for col in range(cols):

                # If the cells is not visitable or is already visited,
                # continue to the next cell
                if grid[row][col] == 0 or visited[row][col]:
                    continue

                # Perform DFS on this new cell to visit all the cells
                # connected to it.
                self.dfs(grid, row, col, visited, result)

        return result


# Examples from the problem statement
print(Solution().depth_first_traversal_on_a_grid(
    [[1,1,0,0],[0,0,1,1],[1,0,1,1],[1,0,0,0]]))
# [(0, 0), (0, 1), (1, 2), (1, 3), (2, 3), (2, 2), (2, 0), (3, 0)]

print(Solution().depth_first_traversal_on_a_grid(
    [[1,0,0,1],[0,0,0,0],[1,1,1,1],[0,0,0,1]]))
# [(0, 0), (0, 3), (2, 0), (2, 1), (2, 2), (2, 3), (3, 3)]

# Edge cases
print(Solution().depth_first_traversal_on_a_grid([]))                  # []
print(Solution().depth_first_traversal_on_a_grid([[0]]))               # []
print(Solution().depth_first_traversal_on_a_grid([[1]]))               # [(0, 0)]
print(Solution().depth_first_traversal_on_a_grid([[1, 1]]))            # [(0, 0), (0, 1)]
print(Solution().depth_first_traversal_on_a_grid([[0, 0], [0, 0]]))   # []
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private boolean isValidCell(int[][] grid, int row, int col) {

            // Check if a cell is valid and belongs to a region of 1's, also
            // check that the cell is not water
            return (
                row >= 0 &&
                row < grid.length &&
                col >= 0 &&
                col < grid[0].length &&
                grid[row][col] == 1
            );
        }

        private void dfs(
            int[][] grid,
            int row,
            int col,
            boolean[][] visited,
            List<List<Integer>> result
        ) {

            // Mark the current cell as visited
            visited[row][col] = true;

            // Add the current cell to the result
            result.add(List.of(row, col));

            // Define the possible movements: up, right, down, left
            int[][] directions = {
                {-1, 0}, // up
                {0, 1},  // right
                {1, 0},  // down
                {0, -1}  // left
            };

            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                // If the neighbour is not visited, recursively call the DFS
                // function on the neighbour
                if (
                    isValidCell(grid, newRow, newCol) &&
                    !visited[newRow][newCol]
                ) {
                    dfs(grid, newRow, newCol, visited, result);
                }
            }
        }

        public List<List<Integer>> depthFirstTraversalOnAGrid(int[][] grid) {
            int rows = grid.length;

            // If the grid is empty, return an empty result
            if (rows == 0) {
                return new ArrayList<>();
            }

            int cols = grid[0].length;

            // Initialize a list to store the result of the DFS
            // which will contain the coordinates of the cells visited
            // during the DFS traversal
            List<List<Integer>> result = new ArrayList<>();

            // Initialize visited array
            boolean[][] visited = new boolean[rows][cols];

            // Traverse each cell of the grid
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {

                    // If the cells is not visitable or is already visited,
                    // continue to the next cell
                    if (grid[row][col] == 0 || visited[row][col]) {
                        continue;
                    }

                    // Perform DFS on this new cell to visit all the cells
                    // connected to it.
                    dfs(grid, row, col, visited, result);
                }
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().depthFirstTraversalOnAGrid(
            new int[][]{{1,1,0,0},{0,0,1,1},{1,0,1,1},{1,0,0,0}}));
        // [[0, 0], [0, 1], [1, 2], [1, 3], [2, 3], [2, 2], [2, 0], [3, 0]]

        System.out.println(new Solution().depthFirstTraversalOnAGrid(
            new int[][]{{1,0,0,1},{0,0,0,0},{1,1,1,1},{0,0,0,1}}));
        // [[0, 0], [0, 3], [2, 0], [2, 1], [2, 2], [2, 3], [3, 3]]

        // Edge cases
        System.out.println(new Solution().depthFirstTraversalOnAGrid(new int[][]{{0}}));   // []
        System.out.println(new Solution().depthFirstTraversalOnAGrid(new int[][]{{1}}));   // [[0, 0]]
        System.out.println(new Solution().depthFirstTraversalOnAGrid(new int[][]{{1,1}})); // [[0, 0], [0, 1]]
        System.out.println(new Solution().depthFirstTraversalOnAGrid(new int[][]{{0,0},{0,0}}));  // []
    }
}
```


<details>
<summary><strong>Trace — first island in the example grid</strong></summary>

```
Step │ Stack (top = current)        │ Action                     │ visited cells
─────┼──────────────────────────────┼────────────────────────────┼─────────────────
1    │ dfs(0,0)                     │ visit (0,0)                │ (0,0)
2    │ dfs(0,0) → up (-1,0) invalid │ try right                  │
3    │ dfs(0,0) → dfs(0,1)          │ visit (0,1)                │ (0,0),(0,1)
4    │ → → up invalid, right=0 grid │ skip; try down (1,1)       │
5    │ → → dfs(1,1)                 │ visit (1,1)                │ (0,0),(0,1),(1,1)
6    │ → → → up=(0,1) visited;      │                            │
       right=(1,2)=0; down=(2,1)=0 │ try left (1,0)             │
7    │ → → → dfs(1,0)               │ visit (1,0)                │ (0,0),(0,1),(1,1),(1,0)
8    │ → → → → all neighbours       │ return through chain       │
       visited or 0; backtrack     │                            │
9    │ outer loop hits (0,3)        │ start new DFS for next island │
…    │ … same pattern repeats       │                            │
Result (one possible order): [(0,0),(0,1),(1,1),(1,0),(0,3),(2,3),(2,4),(3,3),(3,2)] ✓
```

</details>

---

## Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(R × C) | Each cell is visited at most once and contributes O(1) work (4 neighbour checks) |
| **Space** | O(R × C) | The `visited` matrix is R × C; recursion depth is at most R × C in the worst case (a snake-shaped path) |

The recursion depth in the **worst case** is the entire grid — imagine a snake-shaped path of 1s that fills the whole grid. That's a real concern: a 1000×1000 grid can hit recursion depth 10⁶, far past most language defaults. For grids of that size, convert to **iterative DFS** with an explicit stack — same algorithm, O(R×C) heap usage instead of stack frames.

DFS is great when you want to "sink" into one island, count something on the way, and move on. But what if the question is *"how many steps to reach the closest treasure?"* That's a shortest-path question — call BFS.

***

# BFS on a Grid

The translation from graph-BFS to grid-BFS is identical to the DFS one — only how you compute neighbours changes. Mechanically:

```
bfs(start_r, start_c):
    queue = [(start_r, start_c)]
    visited[start_r][start_c] = true
    while queue not empty:
        (r, c) = queue.pop_front()
        record (r, c)
        for each (dr, dc) in [(-1, 0), (0, 1), (1, 0), (0, -1)]:
            nr, nc = r + dr, c + dc
            if is_valid(nr, nc) and not visited[nr][nc]:
                visited[nr][nc] = true
                queue.push_back((nr, nc))
```

Notice the `visited[new_row][new_col] = true` happens at **push**, not pop. Same rule as graph-BFS, same reason: a cell could otherwise be queued multiple times by different parents, leading to bugs and bloat.

> *Before reading on — for the same example grid, predict the BFS visit order from cell (0, 0). Then check the trace.*

Same wrapping pattern for disconnected pieces: outer scan across every cell, kick off a fresh BFS each time you find an unvisited walkable cell.

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
    Start(("Start (0,0)<br/>d=0")) --> N1(("(0,1)<br/>d=1"))
    Start --> N2(("(1,0)<br/>d=1"))
    N1 --> N3(("(1,1)<br/>d=2"))
    N2 --> N3
```

<p align="center"><strong>BFS from (0,0) on the first island. The wavefront reaches all cells at distance 1 before any cell at distance 2 — exactly the property that lets BFS solve "shortest path on unweighted grids" for free.</strong></p>

***

# BFS Implementation


```python run viz=grid viz-root=grid
from typing import List, Tuple
from queue import Queue

class Solution:
    def is_valid_cell(
        self, grid: List[List[int]], row: int, col: int
    ) -> bool:

        # Check if a cell is valid and belongs to a region of 1's, also
        # check that the cell is not water
        return (
            row >= 0
            and row < len(grid)
            and col >= 0
            and col < len(grid[0])
            and grid[row][col] == 1
        )

    def bfs(
        self,
        grid: List[List[int]],
        row: int,
        col: int,
        visited: List[List[bool]],
        result: List[Tuple[int, int]],
    ) -> None:

        # Create a queue to perform breadth-first search
        queue: Queue[Tuple[int, int]] = Queue()

        # Add the current cell to the queue
        queue.put((row, col))

        # Mark the current cell as visited
        visited[row][col] = True

        # Define the possible movements: up, down, left, right
        directions: List[Tuple[int, int]] = [
            (-1, 0),  # up
            (0, 1),   # right
            (1, 0),   # down
            (0, -1)   # left
        ]

        while not queue.empty():

            # Get the front cell from the queue
            curr_row, curr_col = queue.get()

            # Add the current cell to the result
            result.append((curr_row, curr_col))

            for dr, dc in directions:
                new_row = curr_row + dr
                new_col = curr_col + dc

                # If the neighbour is not visited, add it to the queue
                if (
                    self.is_valid_cell(grid, new_row, new_col)
                    and not visited[new_row][new_col]
                ):

                    # Add the new cell to the result
                    queue.put((new_row, new_col))

                    # Mark the new cell as visited
                    visited[new_row][new_col] = True

    def breadth_first_traversal_on_a_grid(
        self, grid: List[List[int]]
    ) -> List[Tuple[int, int]]:
        rows = len(grid)

        # Check if the grid is empty
        if rows == 0:
            return []

        cols = len(grid[0])

        # Initialize a list to store the result of the BFS
        # which will contain the coordinates of the cells visited
        # during the BFS traversal
        result: List[Tuple[int, int]] = []

        # Initialize visited array
        visited = [[False] * cols for _ in range(rows)]

        # Traverse each cell of the grid
        for row in range(rows):
            for col in range(cols):

                # If the cells is not visitable or is already visited,
                # continue to the next cell
                if grid[row][col] == 0 or visited[row][col]:
                    continue

                # Perform BFS on this new cell to visit all the cells
                # connected to it.
                self.bfs(grid, row, col, visited, result)

        return result


# Examples from the problem statement
print(Solution().breadth_first_traversal_on_a_grid(
    [[1,1,0,0],[0,0,1,1],[1,0,1,1],[1,0,0,0]]))
# [(0, 0), (0, 1), (1, 2), (1, 3), (2, 2), (2, 3), (2, 0), (3, 0)]

print(Solution().breadth_first_traversal_on_a_grid(
    [[1,0,0,1],[0,0,0,0],[1,1,1,1],[0,0,0,1]]))
# [(0, 0), (0, 3), (2, 0), (2, 1), (2, 2), (2, 3), (3, 3)]

# Edge cases
print(Solution().breadth_first_traversal_on_a_grid([]))                # []
print(Solution().breadth_first_traversal_on_a_grid([[0]]))             # []
print(Solution().breadth_first_traversal_on_a_grid([[1]]))             # [(0, 0)]
print(Solution().breadth_first_traversal_on_a_grid([[1, 1]]))          # [(0, 0), (0, 1)]
print(Solution().breadth_first_traversal_on_a_grid([[0, 0], [0, 0]])) # []
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private boolean isValidCell(int[][] grid, int row, int col) {

            // Check if a cell is valid and belongs to a region of 1's, also
            // check that the cell is not water
            return (
                row >= 0 &&
                row < grid.length &&
                col >= 0 &&
                col < grid[0].length &&
                grid[row][col] == 1
            );
        }

        private void bfs(
            int[][] grid,
            int row,
            int col,
            boolean[][] visited,
            List<List<Integer>> result
        ) {

            // Create a queue to perform breadth-first search
            Queue<int[]> queue = new LinkedList<>();

            // Add the current cell to the queue
            queue.add(new int[] { row, col });

            // Mark the current cell as visited
            visited[row][col] = true;

            // Define the possible movements: up, right, down, left
            int[][] directions = {
                {-1, 0}, // up
                {0, 1},  // right
                {1, 0},  // down
                {0, -1}  // left
            };

            // Perform BFS
            while (!queue.isEmpty()) {

                // Get the front cell from the queue
                int[] current = queue.poll();
                row = current[0];
                col = current[1];

                // Add the current cell to the result
                result.add(List.of(row, col));

                // Explore all possible movements
                for (int[] dir : directions) {
                    int newRow = row + dir[0];
                    int newCol = col + dir[1];

                    // If the neighbour is not visited, add it to the queue
                    if (
                        isValidCell(grid, newRow, newCol) &&
                        !visited[newRow][newCol]
                    ) {

                        // Add the new cell to the queue
                        queue.add(new int[] { newRow, newCol });

                        // Mark the new cell as visited
                        visited[newRow][newCol] = true;
                    }
                }
            }
        }

        public List<List<Integer>> breadthFirstTraversalOnAGrid(
            int[][] grid
        ) {
            int rows = grid.length;

            // Check if the grid is empty
            if (rows == 0) {
                return new ArrayList<>();
            }

            int cols = grid[0].length;

            // Initialize a list to store the result of the BFS
            // which will contain the coordinates of the cells visited
            // during the BFS traversal
            List<List<Integer>> result = new ArrayList<>();

            // Initialize visited array
            boolean[][] visited = new boolean[rows][cols];

            // Traverse each cell of the grid
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {

                    // If the cells is not visitable or is already visited,
                    // continue to the next cell
                    if (grid[row][col] == 0 || visited[row][col]) {
                        continue;
                    }

                    // Perform BFS on this new cell to visit all the cells
                    // connected to it.
                    bfs(grid, row, col, visited, result);
                }
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().breadthFirstTraversalOnAGrid(
            new int[][]{{1,1,0,0},{0,0,1,1},{1,0,1,1},{1,0,0,0}}));
        // [[0, 0], [0, 1], [1, 2], [1, 3], [2, 2], [2, 3], [2, 0], [3, 0]]

        System.out.println(new Solution().breadthFirstTraversalOnAGrid(
            new int[][]{{1,0,0,1},{0,0,0,0},{1,1,1,1},{0,0,0,1}}));
        // [[0, 0], [0, 3], [2, 0], [2, 1], [2, 2], [2, 3], [3, 3]]

        // Edge cases
        System.out.println(new Solution().breadthFirstTraversalOnAGrid(new int[][]{{0}}));   // []
        System.out.println(new Solution().breadthFirstTraversalOnAGrid(new int[][]{{1}}));   // [[0, 0]]
        System.out.println(new Solution().breadthFirstTraversalOnAGrid(new int[][]{{1,1}})); // [[0, 0], [0, 1]]
        System.out.println(new Solution().breadthFirstTraversalOnAGrid(new int[][]{{0,0},{0,0}}));  // []
    }
}
```


<details>
<summary><strong>Trace — first island starting at (0,0)</strong></summary>

```
Step │ Queue                  │ Action                       │ visited
─────┼────────────────────────┼──────────────────────────────┼─────────────────────────
1    │ [(0,0)]                │ push (0,0); mark             │ (0,0)
2    │ [(0,1),(1,0)]          │ pop (0,0); push (0,1),(1,0)  │ (0,0),(0,1),(1,0)
3    │ [(1,0),(1,1)]          │ pop (0,1); push (1,1)        │ (0,0),(0,1),(1,0),(1,1)
4    │ [(1,1)]                │ pop (1,0); all neighbours    │
                              │ already visited or =0        │
5    │ [ ]                    │ pop (1,1); same — no pushes  │ (no change)
Outer scan continues; (0,3), (2,3), (3,2) start fresh BFS calls.
Result: [(0,0),(0,1),(1,0),(1,1),(0,3),(2,3),(2,4),(3,3),(3,2)] ✓
```

</details>

---

## Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(R × C) | Each cell enters the queue at most once; each examines 4 neighbours |
| **Space** | O(R × C) | `visited` matrix dominates; queue width is at most O(R + C) for a "diamond" wavefront |

The BFS queue's *peak size* is genuinely smaller than the total cell count — at any one moment it only holds the cells on the current wavefront, which for an open grid is roughly O(min(R, C)). But the **visited matrix is unconditionally** O(R × C), so that's what the bound looks like.

---

## Final Takeaway

Grids are graphs — period. Once you see the equivalence:

- **Cell** = node, identified by `(r, c)`.
- **Cardinal adjacency** = edge, computed via a tiny direction array.
- **Implicit graph** = no adjacency list, no matrix; the geometry encodes it.

Every grid problem you encounter reduces to "DFS or BFS the implicit graph". The DSA challenge then becomes deciding **which** of the two, and **what to record at each cell** (a count, a colour, a distance, a parent pointer). The structure stays the same.

Memorise the four-direction array. Memorise the `is_valid_cell` helper. Memorise the "mark on push" rule. With those three habits, you'll write grid traversal correctly on the first try, every time.

The next set of lessons starts piling actual *questions* on top of these traversals — *is there a cycle?*, *what's the topological order?*, *what's the shortest path?* — but each of them is a small mutation of the bones you just laid.

> **Transfer challenge.** A grid has rooms (`1`) and walls (`0`), plus a single fire source `2` and a single exit `3`. Each step, fire spreads to all 4-cardinal neighbours that are rooms. You start at the exit and want the longest possible path back to your starting room such that fire never catches up. Outline the algorithm in 3 sentences. *(Hint: two BFS waves — one for fire, one for you.)*

<details>
<summary><strong>Sketch</strong></summary>

1. Run BFS from the fire source first, recording `fire_time[r][c]` for every reachable cell — the step number at which fire arrives.
2. Run a modified BFS from the exit, recording `your_time[r][c]` and only allowing entry to a cell where `your_time < fire_time` (you must arrive *strictly before* fire).
3. The answer is `your_time[start]` if reachable, else "trapped".

This is the structure of LeetCode's "Escape the Spreading Fire" problem and the underlying technique generalises to every adversarial-grid puzzle.

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: The Hook — missing, needs to be written -->
<!--       Guidance: real-world story opening before any definition -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Edge Cases & Pitfalls — missing, needs to be written -->
<!--       Guidance: bulleted list of gotchas -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->
