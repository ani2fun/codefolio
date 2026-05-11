# 15. Pattern: Shortest path (Breadth-first search)

This lesson teaches you the **BFS-shortest-path pattern** — the recipe for any "minimum steps in an unweighted graph" problem. It's the cleanest, fastest path-finding pattern you have, and it works whenever every edge counts the same.

## Table of contents

1. [When BFS is the answer](#when-bfs-is-the-answer)
2. [The BFS shortest-path template](#the-bfs-shortest-path-template)
3. [Identifying the pattern](#identifying-the-pattern)
4. [Problem: Minimum steps in a grid](#problem-minimum-steps-in-a-grid)
5. [Problem: Nearest distance (multi-source BFS)](#problem-nearest-distance-multi-source-bfs)
6. [Problem: Shortest word transformation](#problem-shortest-word-transformation)

***

# When BFS Is the Answer

Earlier in the chapter you met BFS as a *traversal* — a way to visit every node ring-by-ring outward. That ring-by-ring property has a hidden superpower: **the depth at which BFS first encounters a node is exactly its shortest distance from the source** (in number of edges, not weighted distance).

That single fact turns BFS into the simplest, fastest algorithm for *unweighted* shortest-path problems. No priority queue, no relaxation, no clever proofs needed — the FIFO queue alone guarantees correctness.

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
    S(("Source<br/>(d=0)")) --> A(("A<br/>(d=1)"))
    S --> B(("B<br/>(d=1)"))
    A --> C(("C<br/>(d=2)"))
    B --> C
    B --> D(("D<br/>(d=2)"))
    D --> T(("Target<br/>(d=3)"))
```

<p align="center"><strong>BFS layers from the source. Every node's <code>d</code> value is its minimum number of hops from S. The first time BFS sees a node, that's the answer.</strong></p>

The pattern shows up wherever distance == hop-count:

- *"Minimum steps from start to end on an unweighted maze"*
- *"Number of moves from initial chess board state to checkmate"*
- *"Minimum word transformations connecting two dictionary words"*
- *"Closest restaurant within N hops"*
- *"Time for a flood/fire/infection to reach every cell"*
- *"Degrees of separation between two people in a network"*

For all of these, weight per edge is uniform (every step costs 1). BFS solves them in O(V + E).

> *Before reading on — for a 4-cycle 0–1–2–3, what's the shortest distance from 0 to 2? From 0 to 3?*

`0 → 2`: 2 hops (via 1 or via 3). `0 → 3`: 1 hop (direct edge). BFS would discover them in that order at depths 1 and 2 — the queue's FIFO order does the work for you.

***

# The BFS Shortest-Path Template

The plain BFS code is *almost* the pattern; we just need to (a) record distance, and (b) stop when we hit the target.

```
shortestPathBFS(graph, source, target):
    queue = [(source, 0)]
    visited = {source}
    while queue not empty:
        (node, dist) = queue.popleft()
        if node == target:
            return dist
        for neighbour in graph[node]:
            if neighbour not in visited:
                visited.add(neighbour)
                queue.push((neighbour, dist + 1))
    return -1   # target unreachable
```

Two important habits, lifted from the BFS lesson:

1. **Mark visited at PUSH time, not POP time.** A node could otherwise enter the queue multiple times via different parents, ballooning queue size and (worse) producing wrong answers in some variants.
2. **Carry the distance with each queue entry.** This avoids a separate `dist[]` array and makes the early-exit at the target trivial.

For grid problems, the graph is implicit and "neighbours" are computed via a 4- or 8-direction array — no adjacency list construction needed.

***

# Identifying the Pattern

Look for these signals in problem statements:

- *"Minimum number of steps / moves / changes / transformations"*
- *"Shortest path"* in an **unweighted** graph (no edge weights, or all weights are 1)
- *"Minimum jumps"*, *"fewest hops"*
- *"Time to reach every cell from a source"* (for spreading processes)
- The word **"BFS"** is *almost* a giveaway, but more useful is the implicit *"each move costs the same"*

If the problem includes weighted edges with varying costs, you're past BFS — that's Dijkstra (next lesson). If the problem asks for *all paths* rather than *shortest*, that's the DFS pattern.

***

# Problem: Minimum Steps in a Grid

## The Problem

In an N×M grid where `1` = walkable and `0` = wall, find the minimum number of cardinal-direction moves from `(0, 0)` to `(N-1, M-1)`. Return -1 if no path exists.

```
Input:  grid = [[1, 0, 1, 1],
                [1, 1, 1, 1],
                [0, 1, 0, 1]]
Output: 5
```

## Pattern Mapping

The grid is a graph with implicit edges. BFS from `(0,0)`. As soon as the dequeued cell is `(N-1, M-1)`, return the carried distance. If the queue empties without finding the destination, return -1.

## The Solution


```pseudocode
function minimumStepsInAGrid(grid):
    if start or end is a wall: return −1
    visited ← rows×cols matrix of false
    queue ← empty queue
    enqueue (0, 0, 0) to queue     # (row, col, steps)
    visited[0][0] ← true
    while queue is not empty:
        (r, c, steps) ← dequeue from queue
        if r = rows−1 AND c = cols−1: return steps
        for each (dr, dc) in DIRS:
            nr, nc ← r+dr, c+dc
            if isValid(grid, nr, nc) AND NOT visited[nr][nc]:
                visited[nr][nc] ← true
                enqueue (nr, nc, steps+1) to queue
    return −1
```

```python run
from typing import List
from collections import deque

DIRS = [(-1, 0), (0, 1), (1, 0), (0, -1)]

class Solution:
    def is_valid(self, grid: List[List[int]], r: int, c: int) -> bool:
        rows, cols = len(grid), len(grid[0])
        return 0 <= r < rows and 0 <= c < cols and grid[r][c] == 1

    def minimum_steps_in_a_grid(self, grid: List[List[int]]) -> int:
        if not grid or not grid[0]:
            return -1
        rows, cols = len(grid), len(grid[0])
        if grid[0][0] == 0 or grid[rows - 1][cols - 1] == 0:
            return -1                          # start or end is a wall

        visited = [[False] * cols for _ in range(rows)]
        # Queue carries (row, col, steps_so_far).
        queue = deque([(0, 0, 0)])
        visited[0][0] = True

        while queue:
            r, c, steps = queue.popleft()
            # Early exit: BFS guarantees this is the minimum.
            if r == rows - 1 and c == cols - 1:
                return steps
            for dr, dc in DIRS:
                nr, nc = r + dr, c + dc
                if self.is_valid(grid, nr, nc) and not visited[nr][nc]:
                    visited[nr][nc] = True       # mark at PUSH
                    queue.append((nr, nc, steps + 1))
        return -1


grid = [[1, 0, 1, 1], [1, 1, 1, 1], [0, 1, 0, 1]]
print(Solution().minimum_steps_in_a_grid(grid))   # 5
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        static final int[][] DIRS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        boolean isValid(int[][] grid, int r, int c) {
            return r >= 0 && r < grid.length && c >= 0 && c < grid[0].length && grid[r][c] == 1;
        }

        public int minimumStepsInAGrid(int[][] grid) {
            int rows = grid.length, cols = grid[0].length;
            if (grid[0][0] == 0 || grid[rows-1][cols-1] == 0) return -1;
            boolean[][] visited = new boolean[rows][cols];
            Queue<int[]> q = new ArrayDeque<>();
            q.offer(new int[]{0, 0, 0});
            visited[0][0] = true;
            while (!q.isEmpty()) {
                int[] cur = q.poll();
                if (cur[0] == rows - 1 && cur[1] == cols - 1) return cur[2];
                for (int[] d : DIRS) {
                    int nr = cur[0] + d[0], nc = cur[1] + d[1];
                    if (isValid(grid, nr, nc) && !visited[nr][nc]) {
                        visited[nr][nc] = true;
                        q.offer(new int[]{nr, nc, cur[2] + 1});
                    }
                }
            }
            return -1;
        }
    }

    public static void main(String[] args) {
        int[][] grid = {{1, 0, 1, 1}, {1, 1, 1, 1}, {0, 1, 0, 1}};
        System.out.println(new Solution().minimumStepsInAGrid(grid));
    }
}
```

```c run
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

static const int DIRS[4][2] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

int minimum_steps_in_a_grid(int** grid, int rows, int cols) {
    if (grid[0][0] == 0 || grid[rows-1][cols-1] == 0) return -1;
    bool** visited = malloc(rows * sizeof(bool*));
    for (int i = 0; i < rows; i++) visited[i] = calloc(cols, sizeof(bool));
    int (*queue)[3] = malloc(rows * cols * sizeof(*queue));
    int head = 0, tail = 0;
    queue[tail][0] = 0; queue[tail][1] = 0; queue[tail][2] = 0; tail++;
    visited[0][0] = true;
    int answer = -1;
    while (head < tail) {
        int r = queue[head][0], c = queue[head][1], steps = queue[head][2]; head++;
        if (r == rows - 1 && c == cols - 1) { answer = steps; break; }
        for (int d = 0; d < 4; d++) {
            int nr = r + DIRS[d][0], nc = c + DIRS[d][1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                && grid[nr][nc] == 1 && !visited[nr][nc]) {
                visited[nr][nc] = true;
                queue[tail][0] = nr; queue[tail][1] = nc; queue[tail][2] = steps + 1; tail++;
            }
        }
    }
    free(queue);
    for (int i = 0; i < rows; i++) free(visited[i]);
    free(visited);
    return answer;
}

int main() {
    int data[3][4] = {{1, 0, 1, 1}, {1, 1, 1, 1}, {0, 1, 0, 1}};
    int* grid[3];
    for (int i = 0; i < 3; i++) grid[i] = data[i];
    printf("%d\n", minimum_steps_in_a_grid(grid, 3, 4));
    return 0;
}
```

```scala run
import scala.collection.mutable

object Main extends App {
  val DIRS = Array((-1, 0), (0, 1), (1, 0), (0, -1))

  class Solution {
    def isValid(grid: Array[Array[Int]], r: Int, c: Int): Boolean =
      r >= 0 && r < grid.length && c >= 0 && c < grid(0).length && grid(r)(c) == 1

    def minimumStepsInAGrid(grid: Array[Array[Int]]): Int = {
      val rows = grid.length; val cols = grid(0).length
      if (grid(0)(0) == 0 || grid(rows-1)(cols-1) == 0) return -1
      val visited = Array.ofDim[Boolean](rows, cols)
      val q = mutable.Queue[(Int, Int, Int)]()
      q.enqueue((0, 0, 0))
      visited(0)(0) = true
      while (q.nonEmpty) {
        val (r, c, steps) = q.dequeue()
        if (r == rows - 1 && c == cols - 1) return steps
        for ((dr, dc) <- DIRS) {
          val nr = r + dr; val nc = c + dc
          if (isValid(grid, nr, nc) && !visited(nr)(nc)) {
            visited(nr)(nc) = true
            q.enqueue((nr, nc, steps + 1))
          }
        }
      }
      -1
    }
  }

  val grid = Array(Array(1, 0, 1, 1), Array(1, 1, 1, 1), Array(0, 1, 0, 1))
  println(new Solution().minimumStepsInAGrid(grid))
}
```


***

# Problem: Nearest Distance (Multi-Source BFS)

## The Problem

Grid of `0`s and `1`s. For *every* cell, return its distance to the nearest `1` (Manhattan distance, only horizontal/vertical movement).

```
Input:  grid = [[0, 0, 0, 0],
                [0, 0, 1, 0],
                [0, 0, 0, 0],
                [0, 0, 0, 0]]
Output: [[3, 2, 1, 2],
         [2, 1, 0, 1],
         [3, 2, 1, 2],
         [4, 3, 2, 3]]
```

## Pattern Mapping — Multi-Source BFS

The trick: instead of running BFS from each `1` separately (`O(N * (R*C))`), **enqueue every `1` simultaneously at distance 0**, then BFS once. This is **multi-source BFS** — and it's a beautiful, common, often-missed pattern.

The wavefront expands from *all* the 1s in lockstep. The first time any wave reaches a cell, that's the distance to the nearest 1.

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
    Step0["Initial:<br/>queue = [all 1s @ d=0]<br/>result = grid<br/>(0s = INF)"] --> Step1["Pop a cell, look at 4 neighbours.<br/>If new dist is better, update and push."]
    Step1 --> Step2["Wavefront spreads outward.<br/>Each cell's first wave hit = answer."]
```

<p align="center"><strong>Multi-source BFS. Seed all 1s at distance 0; the unified wavefront races outward and fills the rest of the grid.</strong></p>

> *Before reading on — why does enqueueing every 1 at distance 0 work? Why doesn't it confuse the algorithm?*

Because BFS is **breadth-first** — it dequeues all distance-0 nodes before any distance-1 node, all distance-1 before any distance-2, and so on. The starting set is just bigger than usual; the order property still holds. Every cell still gets reached at its true minimum distance to *any* 1.

## The Solution


```pseudocode
function nearestDistance(grid):
    result ← rows×cols matrix of ∞
    queue ← empty queue
    for each cell (r, c) where grid[r][c] = 1:
        result[r][c] ← 0
        enqueue (r, c, 0) to queue   # multi-source: all 1s start at distance 0
    while queue is not empty:
        (r, c, d) ← dequeue from queue
        for each (dr, dc) in DIRS:
            nr, nc ← r+dr, c+dc
            if in bounds AND d+1 < result[nr][nc]:
                result[nr][nc] ← d+1
                enqueue (nr, nc, d+1) to queue
    return result
```

```python run
from typing import List
from collections import deque

DIRS = [(-1, 0), (0, 1), (1, 0), (0, -1)]
INF = float('inf')

class Solution:
    def nearest_distance(self, grid: List[List[int]]) -> List[List[int]]:
        rows, cols = len(grid), len(grid[0])
        result = [[INF] * cols for _ in range(rows)]
        queue = deque()

        # Multi-source BFS: enqueue every '1' at distance 0.
        for r in range(rows):
            for c in range(cols):
                if grid[r][c] == 1:
                    result[r][c] = 0
                    queue.append((r, c, 0))

        while queue:
            r, c, d = queue.popleft()
            for dr, dc in DIRS:
                nr, nc = r + dr, c + dc
                if 0 <= nr < rows and 0 <= nc < cols and d + 1 < result[nr][nc]:
                    # Update only when strictly better — naturally avoids re-queues.
                    result[nr][nc] = d + 1
                    queue.append((nr, nc, d + 1))
        return result


grid = [[0, 0, 0, 0], [0, 0, 1, 0], [0, 0, 0, 0], [0, 0, 0, 0]]
for row in Solution().nearest_distance(grid):
    print(row)
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        static final int[][] DIRS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        public int[][] nearestDistance(int[][] grid) {
            int rows = grid.length, cols = grid[0].length;
            int[][] result = new int[rows][cols];
            for (int[] row : result) Arrays.fill(row, Integer.MAX_VALUE);
            Queue<int[]> q = new ArrayDeque<>();

            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    if (grid[r][c] == 1) { result[r][c] = 0; q.offer(new int[]{r, c, 0}); }

            while (!q.isEmpty()) {
                int[] cur = q.poll();
                int r = cur[0], c = cur[1], d = cur[2];
                for (int[] dir : DIRS) {
                    int nr = r + dir[0], nc = c + dir[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && d + 1 < result[nr][nc]) {
                        result[nr][nc] = d + 1;
                        q.offer(new int[]{nr, nc, d + 1});
                    }
                }
            }
            return result;
        }
    }

    public static void main(String[] args) {
        int[][] grid = {{0, 0, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        for (int[] r : new Solution().nearestDistance(grid)) System.out.println(Arrays.toString(r));
    }
}
```

```c run
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

static const int DIRS[4][2] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

int** nearest_distance(int** grid, int rows, int cols) {
    int** result = malloc(rows * sizeof(int*));
    for (int i = 0; i < rows; i++) {
        result[i] = malloc(cols * sizeof(int));
        for (int j = 0; j < cols; j++) result[i][j] = INT_MAX;
    }
    int (*queue)[3] = malloc(rows * cols * 4 * sizeof(*queue));
    int head = 0, tail = 0;
    for (int r = 0; r < rows; r++)
        for (int c = 0; c < cols; c++)
            if (grid[r][c] == 1) {
                result[r][c] = 0;
                queue[tail][0] = r; queue[tail][1] = c; queue[tail][2] = 0; tail++;
            }
    while (head < tail) {
        int r = queue[head][0], c = queue[head][1], d = queue[head][2]; head++;
        for (int dd = 0; dd < 4; dd++) {
            int nr = r + DIRS[dd][0], nc = c + DIRS[dd][1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && d + 1 < result[nr][nc]) {
                result[nr][nc] = d + 1;
                queue[tail][0] = nr; queue[tail][1] = nc; queue[tail][2] = d + 1; tail++;
            }
        }
    }
    free(queue);
    return result;
}

int main() {
    int data[4][4] = {{0, 0, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
    int* grid[4];
    for (int i = 0; i < 4; i++) grid[i] = data[i];
    int** r = nearest_distance(grid, 4, 4);
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) printf("%d ", r[i][j]);
        printf("\n");
        free(r[i]);
    }
    free(r);
    return 0;
}
```

```scala run
import scala.collection.mutable

object Main extends App {
  val DIRS = Array((-1, 0), (0, 1), (1, 0), (0, -1))

  class Solution {
    def nearestDistance(grid: Array[Array[Int]]): Array[Array[Int]] = {
      val rows = grid.length; val cols = grid(0).length
      val result = Array.fill(rows, cols)(Int.MaxValue)
      val q = mutable.Queue[(Int, Int, Int)]()
      for (r <- 0 until rows; c <- 0 until cols if grid(r)(c) == 1) {
        result(r)(c) = 0; q.enqueue((r, c, 0))
      }
      while (q.nonEmpty) {
        val (r, c, d) = q.dequeue()
        for ((dr, dc) <- DIRS) {
          val nr = r + dr; val nc = c + dc
          if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && d + 1 < result(nr)(nc)) {
            result(nr)(nc) = d + 1
            q.enqueue((nr, nc, d + 1))
          }
        }
      }
      result
    }
  }

  val grid = Array(Array(0, 0, 0, 0), Array(0, 0, 1, 0), Array(0, 0, 0, 0), Array(0, 0, 0, 0))
  new Solution().nearestDistance(grid).foreach(r => println(r.mkString(" ")))
}
```


The multi-source pattern is **massively** more efficient than running BFS once per source (`O(K * R*C)` where K = number of sources). Multi-source BFS is `O(R*C)` regardless of how many sources you start with. Same algorithm, fundamentally better complexity.

***

# Problem: Shortest Word Transformation

## The Problem

Given two words `source` and `target` and a `wordList`, find the minimum number of words in a transformation chain `source → s1 → s2 → … → target`, where:

- Each consecutive pair differs by exactly one letter.
- Every word `s1, s2, …, target` is in `wordList`.

Return 0 if no transformation exists.

```
Input:  source = "hit", target = "cog", wordList = ["hot", "dot", "dog", "lot", "log", "cog"]
Output: 5  (hit → hot → dot → dog → cog)
```

## Pattern Mapping

The graph is **implicit**:

- Node = a word.
- Edge = "differ by exactly one letter".

The graph is unweighted (each transformation costs 1), and we want minimum hops. **BFS shortest path.**

The trick is generating neighbours efficiently. Two main approaches:

1. **Per-position substitution.** For each character position, try every other letter; check if the mutated word is in `wordList`. O(L × 26) neighbours per word, where L = word length.
2. **Pattern keys.** Pre-build a map from "h*t" → ["hit", "hot", "hat", …]. Each word has L neighbour-pattern keys.

We'll use approach 1 — simpler to code, still fast for typical inputs. The implementation builds the adjacency on the fly during BFS.

## The Solution (per-position substitution)


```pseudocode
function shortestWordTransformation(source, target, wordList):
    wordSet ← set of wordList
    if target is not in wordSet: return 0
    queue ← empty queue
    enqueue (source, 1) to queue
    visited ← empty set
    add source to visited
    while queue is not empty:
        (word, level) ← dequeue from queue
        if word = target: return level
        for i from 0 to length(word)−1:
            for each letter c in 'a' to 'z':
                candidate ← word with position i replaced by c
                if candidate is in wordSet AND candidate is not in visited:
                    add candidate to visited
                    enqueue (candidate, level+1) to queue
    return 0
```

```python run
from typing import List
from collections import deque

class Solution:
    def shortest_word_transformation(self,
                                      source: str,
                                      target: str,
                                      word_list: List[str]) -> int:
        word_set = set(word_list)
        if target not in word_set:
            return 0

        queue = deque([(source, 1)])     # (word, level)
        visited = {source}

        while queue:
            word, level = queue.popleft()
            if word == target:
                return level

            # Try every single-letter mutation.
            for i in range(len(word)):
                for ch in 'abcdefghijklmnopqrstuvwxyz':
                    if ch == word[i]:
                        continue
                    candidate = word[:i] + ch + word[i+1:]
                    if candidate in word_set and candidate not in visited:
                        visited.add(candidate)
                        queue.append((candidate, level + 1))
        return 0


print(Solution().shortest_word_transformation("hit", "cog",
                                              ["hot", "dot", "dog", "lot", "log", "cog"]))
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int shortestWordTransformation(String source, String target, List<String> wordList) {
            Set<String> wordSet = new HashSet<>(wordList);
            if (!wordSet.contains(target)) return 0;
            Queue<String[]> q = new ArrayDeque<>();
            q.offer(new String[]{source, "1"});
            Set<String> visited = new HashSet<>();
            visited.add(source);
            while (!q.isEmpty()) {
                String[] cur = q.poll();
                String word = cur[0]; int level = Integer.parseInt(cur[1]);
                if (word.equals(target)) return level;
                char[] chars = word.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char orig = chars[i];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == orig) continue;
                        chars[i] = c;
                        String cand = new String(chars);
                        if (wordSet.contains(cand) && !visited.contains(cand)) {
                            visited.add(cand);
                            q.offer(new String[]{cand, Integer.toString(level + 1)});
                        }
                    }
                    chars[i] = orig;
                }
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().shortestWordTransformation(
            "hit", "cog", List.of("hot", "dot", "dog", "lot", "log", "cog")));
    }
}
```

```c run
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

// Tiny linear scan word lookup — fine for small lists.
static bool in_list(char* w, char** list, int n) {
    for (int i = 0; i < n; i++) if (strcmp(w, list[i]) == 0) return true;
    return false;
}

int shortest_word_transformation(const char* source, const char* target, char** word_list, int n) {
    if (!in_list((char*)target, word_list, n)) return 0;
    int len = strlen(source);
    char** queue_words = malloc(10000 * sizeof(char*));
    int* queue_levels = malloc(10000 * sizeof(int));
    int head = 0, tail = 0;
    queue_words[tail] = strdup(source); queue_levels[tail] = 1; tail++;
    char** visited = malloc(10000 * sizeof(char*));
    int v_count = 0;
    visited[v_count++] = strdup(source);
    int answer = 0;
    while (head < tail) {
        char* word = queue_words[head]; int level = queue_levels[head]; head++;
        if (strcmp(word, target) == 0) { answer = level; break; }
        char buf[64]; strcpy(buf, word);
        for (int i = 0; i < len; i++) {
            char orig = buf[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == orig) continue;
                buf[i] = c;
                if (in_list(buf, word_list, n)) {
                    bool seen = false;
                    for (int k = 0; k < v_count; k++)
                        if (strcmp(visited[k], buf) == 0) { seen = true; break; }
                    if (!seen) {
                        visited[v_count++] = strdup(buf);
                        queue_words[tail] = strdup(buf); queue_levels[tail] = level + 1; tail++;
                    }
                }
            }
            buf[i] = orig;
        }
    }
    for (int i = 0; i < tail; i++) free(queue_words[i]);
    for (int i = 0; i < v_count; i++) free(visited[i]);
    free(queue_words); free(queue_levels); free(visited);
    return answer;
}

int main() {
    char* list[] = {"hot", "dot", "dog", "lot", "log", "cog"};
    printf("%d\n", shortest_word_transformation("hit", "cog", list, 6));
    return 0;
}
```

```scala run
import scala.collection.mutable

object Main extends App {
  class Solution {
    def shortestWordTransformation(source: String, target: String, wordList: List[String]): Int = {
      val wordSet = wordList.toSet
      if (!wordSet.contains(target)) return 0
      val q = mutable.Queue[(String, Int)]()
      q.enqueue((source, 1))
      val visited = mutable.Set(source)
      while (q.nonEmpty) {
        val (word, level) = q.dequeue()
        if (word == target) return level
        for (i <- word.indices) {
          val orig = word(i)
          for (c <- 'a' to 'z' if c != orig) {
            val candidate = word.updated(i, c)
            if (wordSet.contains(candidate) && !visited.contains(candidate)) {
              visited.add(candidate); q.enqueue((candidate, level + 1))
            }
          }
        }
      }
      0
    }
  }

  println(new Solution().shortestWordTransformation(
    "hit", "cog", List("hot", "dot", "dog", "lot", "log", "cog")))
}
```


## Complexity Analysis

| Problem | Time | Space |
|---|---|---|
| Minimum steps in a grid | O(R × C) | O(R × C) |
| Nearest distance (multi-source) | O(R × C) | O(R × C) |
| Shortest word transformation | O(N × L × 26) | O(N × L) |

Where N = words, L = word length. The word problem is dominated by the per-word neighbour generation (L positions × 26 letters).

---

## Final Takeaway

BFS shortest path is the **simplest, most reliable, fastest** algorithm for *unweighted* shortest-path problems. The recipe is dead easy: queue with distance, mark on push, early exit at target. The two power-ups — **multi-source BFS** (seed every starting point at d=0) and **implicit graphs** (compute neighbours on the fly) — turn the same algorithm into a swiss-army knife for grid, word-ladder, and graph-shortest-path problems alike.

When BFS isn't enough — when edges have varying weights — you upgrade to Dijkstra, the next pattern. The mental shift is small: replace the FIFO queue with a min-heap that orders by *cumulative weight* instead of *insertion order*. Same shape, different ordering principle.

> **Transfer challenge.** A virus enters a hospital ward modelled as a grid. Initially several rooms are infected. Each minute, the virus spreads to all empty 4-cardinal neighbours. Find the minimum minutes until every room is infected, or `-1` if some rooms can never be reached. *Hint: this is multi-source BFS in disguise.*

<details>
<summary><strong>Sketch</strong></summary>

Multi-source BFS from all initially-infected rooms, distance 0. Carry distance with each cell; the maximum distance reached is the answer. If any "empty" room remains unvisited at the end, return -1 (it can never be reached). Same code as nearest-distance, with one extra max-tracking variable.

This is *exactly* LeetCode's "Rotting Oranges" problem. Multi-source BFS = the right tool.

</details>
