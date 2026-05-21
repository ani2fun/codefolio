# 16. Pattern: Shortest path (Dijkstra)

This lesson teaches you the **Dijkstra-pattern** — the recipe for any "minimum cost / weighted shortest path" problem on graphs with non-negative edge weights. It's the upgrade from BFS that handles arbitrary edge costs.

## Table of contents

1. [When BFS isn't enough — and Dijkstra is](#when-bfs-isnt-enough--and-dijkstra-is)
2. [The Dijkstra-pattern template](#the-dijkstra-pattern-template)
3. [Identifying the pattern](#identifying-the-pattern)
4. [Problem: Minimum cost path](#problem-minimum-cost-path)
5. [Problem: Cheapest flights with K stops](#problem-cheapest-flights-with-k-stops)
6. [Problem: Minimum travel time](#problem-minimum-travel-time)

***

# When BFS Isn't Enough — and Dijkstra Is

BFS gives you the shortest path in *number of hops*. Dijkstra gives you the shortest path in *cumulative weight*. The difference is whether each edge counts as 1 or as a varying real cost.

A grid where every cell is 1 step? BFS. A grid where each cell has a different toll cost? Dijkstra.

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

***

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

***

# Identifying the Pattern

The signal-words:

- *"Minimum cost / time / distance / fuel / weight / total"*
- *"Shortest path"* with **edge weights**
- *"Cheapest flight"*, *"fastest route"*, *"least toll"*
- *"Minimise [some additive quantity]"* on a graph or grid

If the problem is unweighted (or all weights equal), use BFS. If the graph has negative weights, use Bellman-Ford. Otherwise: **Dijkstra**.

***

# Problem: Minimum Cost Path

## The Problem

Grid where each cell is the cost to step on it. Find the minimum total cost from `(0, 0)` to `(N-1, M-1)`, where you can move in 4 cardinal directions.

```
Input:  grid = [[9, 4, 9, 9],
                [6, 7, 6, 4],
                [8, 3, 3, 7],
                [7, 4, 9, 10]]
Output: 43
```

<details>
<summary><h2>Pattern Mapping</h2></summary>


The grid is a graph where each cell is a node and each move is an edge. Edge weight from cell `A` to cell `B` is the cost of cell `B` (the cell you're stepping onto). The starting cell's cost is also added — that's the seed.

Standard Dijkstra from `(0, 0)`. Return `distance[N-1][M-1]`.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
import heapq
from typing import List, Tuple

# Structure to represent a Cell in the graph
class Cell:
    def __init__(self, row: int, col: int, cost: int):
        self.row = row
        self.col = col
        self.cost = cost

    # Comparator function for the priority queue to create a min-heap
    def __lt__(self, other):
        return self.cost < other.cost

class Solution:
    def is_valid_cell(
        self, row: int, col: int, rows: int, cols: int
    ) -> bool:
        return row >= 0 and row < rows and col >= 0 and col < cols

    def minimum_cost_path(self, grid: List[List[int]]) -> int:
        rows = len(grid)
        if rows == 0:
            return 0

        cols = len(grid[0])

        # Create a matrix to store the minimum cost to reach each cell
        min_cost = [[float("inf")] * cols for _ in range(rows)]

        # Create a priority queue (min-heap) to store the cells with
        # their costs
        pq = []

        # Assign the minimum cost of the starting point
        min_cost[0][0] = grid[0][0]

        # Enqueue starting cell and the cost to move on it
        heapq.heappush(pq, Cell(0, 0, grid[0][0]))

        # Define the possible movements: up, right, down, left
        directions: List[Tuple[int, int]] = [
            (-1, 0),  # up
            (0, 1),   # right
            (1, 0),   # down
            (0, -1)   # left
        ]

        while pq:
            curr_cell = heapq.heappop(pq)
            curr_row = curr_cell.row
            curr_col = curr_cell.col
            cost = curr_cell.cost

            # Explore the neighbours
            for dr, dc in directions:
                new_row = curr_row + dr
                new_col = curr_col + dc

                # Check if the new cell is within the grid
                if self.is_valid_cell(new_row, new_col, rows, cols):
                    new_cost = cost + grid[new_row][new_col]

                    # If a shorter path is found, update the minimum
                    # cost and add the new cell to the priority queue
                    if new_cost < min_cost[new_row][new_col]:

                        # Update the minimum cost for the new cell
                        min_cost[new_row][new_col] = new_cost

                        # Add the new cell to the priority queue
                        heapq.heappush(
                            pq, Cell(new_row, new_col, new_cost)
                        )

        # Return the minimum cost to reach the bottom right cell
        return min_cost[rows - 1][cols - 1]


# Examples from the problem statement
print(Solution().minimum_cost_path([[9,4,9,9],[6,7,6,4],[8,3,3,7],[7,4,9,10]]))  # 43
print(Solution().minimum_cost_path([[9,4,9,9],[1,7,6,4],[1,3,3,7],[1,2,2,10]]))  # 26

# Edge cases
print(Solution().minimum_cost_path([]))                                            # 0 — empty grid
print(Solution().minimum_cost_path([[5]]))                                         # 5 — single cell
print(Solution().minimum_cost_path([[1,2],[3,4]]))                                 # 7 — 2x2
print(Solution().minimum_cost_path([[1,100],[1,1]]))                               # 3 — prefer going down
print(Solution().minimum_cost_path([[1,1,1],[1,1,1],[1,1,1]]))                     # 5 — all ones
```

```java run
import java.util.*;

public class Main {
    // Structure to represent a Cell in the graph
    static class Cell {

        int row;
        int col;
        int cost;

        Cell(int row, int col, int cost) {
            this.row = row;
            this.col = col;
            this.cost = cost;
        }
    }

    // Comparator function for the priority queue to create a min-heap
    static class CompareMinHeap implements Comparator<Cell> {
        public int compare(Cell a, Cell b) {

            // Min-heap based on cost
            return Integer.compare(a.cost, b.cost);
        }
    }

    static class Solution {

        boolean isValidCell(int row, int col, int rows, int cols) {
            return row >= 0 && row < rows && col >= 0 && col < cols;
        }

        public int minimumCostPath(int[][] grid) {
            int rows = grid.length;
            if (rows == 0) {
                return 0;
            }

            int cols = grid[0].length;

            // Create a matrix to store the minimum cost to reach each cell
            int[][] minCost = new int[rows][cols];
            for (int[] row : minCost) {
                java.util.Arrays.fill(row, Integer.MAX_VALUE);
            }

            // Create a priority queue (min-heap) to store the cells with
            // their costs
            PriorityQueue<Cell> pq = new PriorityQueue<>(
                new CompareMinHeap()
            );

            // Assign the minimum cost of the starting point
            minCost[0][0] = grid[0][0];

            // Enqueue starting cell and the cost to move on it
            pq.add(new Cell(0, 0, grid[0][0]));

            // Define the possible movements: up, right, down, left
            int[][] directions = {
                {-1, 0}, // up
                {0, 1},  // right
                {1, 0},  // down
                {0, -1}  // left
            };

            while (!pq.isEmpty()) {
                Cell currCell = pq.poll();
                int currRow = currCell.row;
                int currCol = currCell.col;
                int cost = currCell.cost;

                // Explore the neighbours
                for (int[] dir : directions) {
                    int newRow = currRow + dir[0];
                    int newCol = currCol + dir[1];

                    // Check if the new cell is within the grid
                    if (isValidCell(newRow, newCol, rows, cols)) {
                        int newCost = cost + grid[newRow][newCol];

                        // If a shorter path is found, update the minimum
                        // cost and add the new cell to the priority queue
                        if (newCost < minCost[newRow][newCol]) {

                            // Update the minimum cost for the new cell
                            minCost[newRow][newCol] = newCost;

                            // Add the new cell to the priority queue
                            pq.add(new Cell(newRow, newCol, newCost));
                        }
                    }
                }
            }

            // Return the minimum cost to reach the bottom right cell
            return minCost[rows - 1][cols - 1];
        }
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        // Examples from the problem statement
        System.out.println(sol.minimumCostPath(new int[][]{{9,4,9,9},{6,7,6,4},{8,3,3,7},{7,4,9,10}}));  // 43
        System.out.println(sol.minimumCostPath(new int[][]{{9,4,9,9},{1,7,6,4},{1,3,3,7},{1,2,2,10}}));  // 26

        // Edge cases
        System.out.println(sol.minimumCostPath(new int[][]{}));                           // 0
        System.out.println(sol.minimumCostPath(new int[][]{{5}}));                        // 5
        System.out.println(sol.minimumCostPath(new int[][]{{1,2},{3,4}}));                // 7
        System.out.println(sol.minimumCostPath(new int[][]{{1,100},{1,1}}));              // 3
        System.out.println(sol.minimumCostPath(new int[][]{{1,1,1},{1,1,1},{1,1,1}}));   // 5
    }
}
```

</details>


***

# Problem: Cheapest Flights With K Stops

## The Problem

Given a list of one-way flights (`from`, `to`, `cost`), source city, destination, and `K` (max stops allowed), find the minimum-cost flight path. Return -1 if impossible.

```
Input:  flights = [[[1,2],[3,1]], [[4,4]], [[4,1]], [[2,2],[4,5]], []]
        source = 0, destination = 4, K = 2
Output: 4 (path 0 → 3 → 2 → 4 with 2 stops)
```

<details>
<summary><h2>Pattern Mapping — Dijkstra With State</h2></summary>


The crucial twist: you can't use the standard Dijkstra distance array, because two paths to the same city might have different stop counts — and the "fewer stops" path might still be useful even if it costs more.

**Solution: 2D distance array.** Track `min_cost[city][flights_taken]` instead of `min_cost[city]`. Every queue entry carries `(cost, city, flights_used)` — flights = the number of edges taken so far.

This is the **stateful Dijkstra** variant — the same pattern, but you augment the node with a state dimension that affects which transitions are valid.

> *Before reading on — why doesn't the standard 1D Dijkstra work here? What can go wrong?*

The standard Dijkstra finalises a city's distance the first time it's popped. But here, "first popped" might mean "popped after using K+1 flights" — useless because we've exceeded the budget. A 1D Dijkstra would close off the destination too early. The 2D state (cost + stops) lets us discover *paths within budget* even if they're longer.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
import heapq
from typing import List, Tuple, Dict

# Structure to represent the state of a stop
class Stop:
    def __init__(self, city: int, cost: int, flights: int):
        self.city = city
        self.cost = cost
        self.flights = flights

    # Comparator function for the priority queue to create a min-heap
    def __lt__(self, other):
        return self.cost < other.cost

class Solution:
    def cheapest_flights(
        self,
        flights: List[List[List[int]]],
        source: int,
        destination: int,
        k: int,
    ) -> int:
        nodes = len(flights)
        if nodes == 0:
            return -1

        # 2D minCost: nodes x (k + 2) (flights from 0 to k + 1)
        min_cost = [[float("inf")] * (k + 2) for _ in range(nodes)]

        # Create a priority queue (min-heap) to store the stops with
        # their costs
        pq = []

        # Assign the minimum cost of the starting point, need 0 flights
        # to reach source
        min_cost[source][0] = 0

        # Enqueue starting stop and the cost to move on it
        heapq.heappush(pq, Stop(source, 0, 0))

        while pq:
            curr_stop = heapq.heappop(pq)
            curr_city = curr_stop.city
            curr_cost = curr_stop.cost
            curr_flights = curr_stop.flights

            # If we reached the destination, return the cost. We check for K + 1 as
            # we can have as for K stops between source and destination we need K + 1 flights.
            # For example if K = 1, we can have a path like 0 -> 1 -> 2 which has 1 stop
            # but 2 flights.
            if curr_city == destination and curr_flights <= k + 1:
                return curr_cost

            # If the cost is greater than the recorded minimum cost, skip
            # processing
            if curr_cost > min_cost[curr_city][curr_flights]:
                continue

            # Can only take up to k + 1 flights
            if curr_flights < k + 1:
                for flight in flights[curr_city]:
                    new_city, new_cost_flight = flight
                    new_cost = curr_cost + new_cost_flight
                    if new_cost < min_cost[new_city][curr_flights + 1]:
                        min_cost[new_city][curr_flights + 1] = new_cost
                        heapq.heappush(
                            pq,
                            Stop(new_city, new_cost, curr_flights + 1),
                        )

        # If the destination is unreachable, return -1
        return -1


# Examples from the problem statement
print(Solution().cheapest_flights([[[1,2],[3,1]],[[4,4]],[[4,1]],[[2,2],[4,5]],[]], 0, 4, 2))  # 4
print(Solution().cheapest_flights([[[4,2]],[[3,3],[0,4]],[[4,3],[0,1]],[[2,1],[4,4]],[[1,5]]], 3, 0, 2))  # 2

# Edge cases
print(Solution().cheapest_flights([], 0, 0, 0))                                                 # -1 — empty graph
print(Solution().cheapest_flights([[]], 0, 0, 0))                                               # 0 — source is destination
print(Solution().cheapest_flights([[[1,5]],[]], 0, 1, 0))                                       # -1 — needs 1 stop, k=0
print(Solution().cheapest_flights([[[1,5]],[]], 0, 1, 1))                                       # 5 — direct flight, k=1
print(Solution().cheapest_flights([[[1,10],[2,3]],[[3,2]],[[1,4]],[]], 0, 3, 1))               # 9 — 0->2->1->3 needs 2 stops; within k=1: 0->1->3=12; answer -1 if only k=1
```

```java run
import java.util.*;

public class Main {
    // Structure to represent the state of a stop
    static class Stop {

        int city;
        int cost;
        int flights;

        Stop(int city, int cost, int flights) {
            this.city = city;
            this.cost = cost;
            this.flights = flights;
        }
    }

    // Comparator function for the priority queue to create a min-heap
    static class CompareMinHeap implements Comparator<Stop> {
        public int compare(Stop a, Stop b) {

            // Min-heap based on cost
            return Integer.compare(a.cost, b.cost);
        }
    }

    static class Solution {
        public int cheapestFlights(
            List<List<List<Integer>>> flights,
            int source,
            int destination,
            int K
        ) {
            int nodes = flights.size();
            if (nodes == 0) {
                return -1;
            }

            // 2D minCost: nodes x (K + 2) (flights from 0 to K + 1)
            int[][] minCost = new int[nodes][K + 2];
            for (int i = 0; i < nodes; i++) {
                Arrays.fill(minCost[i], Integer.MAX_VALUE);
            }

            // Create a priority queue (min-heap) to store the stops with
            // their costs
            PriorityQueue<Stop> pq = new PriorityQueue<>(
                new CompareMinHeap()
            );

            // Assign the minimum cost of the starting point, need 0 flights
            // to reach source
            minCost[source][0] = 0;

            // Enqueue starting stop and the cost to move on it
            pq.add(new Stop(source, 0, 0));

            while (!pq.isEmpty()) {
                Stop currStop = pq.poll();
                int currCity = currStop.city;
                int currCost = currStop.cost;
                int currFlights = currStop.flights;

                // If we reached the destination, return the cost. We check
                // for K + 1 as we can have as for K stops between source and
                // destination we need K + 1 flights. For example if K = 1,
                // we can have a path like 0 -> 1 -> 2 which has 1 stop but 2
                // flights.
                if (currCity == destination && currFlights <= K + 1) {
                    return currCost;
                }

                // If the cost is greater than the recorded minimum cost,
                // skip processing
                if (currCost > minCost[currCity][currFlights]) {
                    continue;
                }

                if (currFlights < K + 1) {
                    for (List<Integer> flight : flights.get(currCity)) {
                        int newCity = flight.get(0);
                        int newCost = currCost + flight.get(1);
                        if (newCost < minCost[newCity][currFlights + 1]) {
                            minCost[newCity][currFlights + 1] = newCost;
                            pq.add(
                                new Stop(newCity, newCost, currFlights + 1)
                            );
                        }
                    }
                }
            }

            // If the destination is unreachable, return -1
            return -1;
        }
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        // Examples from the problem statement
        System.out.println(sol.cheapestFlights(List.of(List.of(List.of(1,2),List.of(3,1)),List.of(List.of(4,4)),List.of(List.of(4,1)),List.of(List.of(2,2),List.of(4,5)),new ArrayList<>()), 0, 4, 2));  // 4
        System.out.println(sol.cheapestFlights(List.of(List.of(List.of(4,2)),List.of(List.of(3,3),List.of(0,4)),List.of(List.of(4,3),List.of(0,1)),List.of(List.of(2,1),List.of(4,4)),List.of(List.of(1,5))), 3, 0, 2));  // 2

        // Edge cases
        System.out.println(sol.cheapestFlights(new ArrayList<>(), 0, 0, 0));  // -1
        System.out.println(sol.cheapestFlights(List.of(new ArrayList<>()), 0, 0, 0));  // 0
        System.out.println(sol.cheapestFlights(List.of(List.of(List.of(1,5)),new ArrayList<>()), 0, 1, 0));  // -1
        System.out.println(sol.cheapestFlights(List.of(List.of(List.of(1,5)),new ArrayList<>()), 0, 1, 1));  // 5
    }
}
```

</details>


***

# Problem: Minimum Travel Time

## The Problem

A network of routes with travel times. From `source` to `destination`, find the minimum *total time*. Twist:

> If you arrive at a city at an **odd** time, you must wait 1 extra unit before continuing.
> If you arrive at an **even** time, you can continue immediately.

```
Input:  graph = [[[1, 1], [2, 4]], [[2, 2], [3, 2]], [[3, 1]], []]
        source = 0, destination = 3
Output: 4
```

<details>
<summary><h2>Pattern Mapping</h2></summary>


A standard Dijkstra computes arrival times. The twist: when we relax an edge, we don't just add the edge weight — we also **add 1 if our current arrival time is odd**.

This is **Dijkstra with a wait-time tweak** — the relaxation rule becomes:

```
wait = 1 if cur_time is odd else 0
arrival_time_at_neighbour = cur_time + wait + edge_weight
```

That tiny addition handles the parity rule cleanly within the standard pattern.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
import heapq
from typing import List, Tuple, Dict

# Structure to represent the state of a travel
class TravelState:
    def __init__(self, city: int, time: int):
        self.city = city
        self.time = time

    # Comparator function for the priority queue to create a min-heap
    def __lt__(self, other):
        return self.time < other.time

class Solution:
    def minimum_travel_time(
        self,
        routes: List[List[Tuple[int, int]]],
        source: int,
        destination: int,
    ) -> int:
        cities = len(routes)
        if cities == 0:
            return -1

        # Create a list to store the minimum arrival time at each city
        min_arrival_time = [float("inf")] * cities

        # Create a priority queue (min-heap) to store the stops with
        # their costs
        pq: List[TravelState] = []

        # Assign the minimum arrival time of the starting point with 0
        min_arrival_time[source] = 0

        # Enqueue starting city and the time to reach it
        heapq.heappush(pq, TravelState(source, 0))

        while pq:
            curr_travel_state = heapq.heappop(pq)
            curr_city = curr_travel_state.city
            curr_time = curr_travel_state.time

            # If we reached the destination, return the time
            if curr_city == destination:
                return curr_time

            # If the time is greater than the recorded minimum time, skip
            # processing
            if curr_time > min_arrival_time[curr_city]:
                continue

            for next_city, road_time in routes[curr_city]:

                # If you arrive at an odd time, wait 1 unit for a red
                # light.
                waiting_time = 1 if curr_time % 2 == 1 else 0

                arrival_time = curr_time + waiting_time + road_time
                if arrival_time < min_arrival_time[next_city]:
                    min_arrival_time[next_city] = arrival_time
                    heapq.heappush(
                        pq, TravelState(next_city, arrival_time)
                    )

        # If the destination is unreachable, return -1
        return -1


# Examples from the problem statement
print(Solution().minimum_travel_time([[[1,1],[2,4]],[[2,2],[3,2]],[[3,1]],[]], 0, 3))  # 4
print(Solution().minimum_travel_time([[[1,1],[2,2]],[[3,2]],[[3,1]],[]], 0, 3))        # 3

# Edge cases
print(Solution().minimum_travel_time([], 0, 0))                                         # -1 — empty
print(Solution().minimum_travel_time([[]], 0, 0))                                       # 0 — source is dest
print(Solution().minimum_travel_time([[[1,2]],[]], 0, 1))                               # 2 — single edge even time
print(Solution().minimum_travel_time([[[1,1]],[]], 0, 1))                               # 1 — single edge, arrive at odd; dest reached before wait
print(Solution().minimum_travel_time([[[1,2]],[[]]], 0, 1))                             # -1 — no path to dest
```

```java run
import java.util.*;

public class Main {
    // Structure to represent the state of a stop
    static class TravelState {

        int city;
        int time;

        TravelState(int city, int time) {
            this.city = city;
            this.time = time;
        }
    }

    // Comparator function for the priority queue to create a min-heap
    static class CompareMinHeap implements Comparator<TravelState> {
        public int compare(TravelState a, TravelState b) {

            // Min-heap based on time
            return Integer.compare(a.time, b.time);
        }
    }

    static class Solution {
        public int minimumTravelTime(
            List<List<List<Integer>>> routes,
            int source,
            int destination
        ) {
            int cities = routes.size();
            if (cities == 0) {
                return -1;
            }

            // Create a list to store the minimum arrival time at each city
            int[] minArrivalTime = new int[cities];
            Arrays.fill(minArrivalTime, Integer.MAX_VALUE);

            // Create a priority queue (min-heap) to store the stops with
            // their costs
            PriorityQueue<TravelState> pq = new PriorityQueue<>(
                new CompareMinHeap()
            );

            // Assign the minimum arrival time of the starting point with 0
            minArrivalTime[source] = 0;

            // Enqueue starting city and the time to reach it
            pq.add(new TravelState(source, 0));

            while (!pq.isEmpty()) {
                TravelState currTravelState = pq.poll();
                int currCity = currTravelState.city;
                int currTime = currTravelState.time;

                // If we reached the destination, return the time
                if (currCity == destination) {
                    return currTime;
                }

                // If the time is greater than the recorded minimum time,
                // skip processing
                if (currTime > minArrivalTime[currCity]) {
                    continue;
                }

                for (List<Integer> neighbor : routes.get(currCity)) {
                    int nextCity = neighbor.get(0);
                    int travelTime = neighbor.get(1);

                    // If you arrive at an odd time, wait 1 unit for a red
                    // light.
                    int waitingTime = 0;
                    if (currTime % 2 == 1) {
                        waitingTime = 1;
                    }

                    int arrivalTime = currTime + waitingTime + travelTime;
                    if (arrivalTime < minArrivalTime[nextCity]) {
                        minArrivalTime[nextCity] = arrivalTime;
                        pq.add(new TravelState(nextCity, arrivalTime));
                    }
                }
            }

            // If the destination is unreachable, return -1
            return -1;
        }
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        // Examples from the problem statement
        System.out.println(sol.minimumTravelTime(List.of(List.of(List.of(1,1),List.of(2,4)),List.of(List.of(2,2),List.of(3,2)),List.of(List.of(3,1)),new ArrayList<>()), 0, 3));  // 4
        System.out.println(sol.minimumTravelTime(List.of(List.of(List.of(1,1),List.of(2,2)),List.of(List.of(3,2)),List.of(List.of(3,1)),new ArrayList<>()), 0, 3));              // 3

        // Edge cases
        System.out.println(sol.minimumTravelTime(new ArrayList<>(), 0, 0));                   // -1
        System.out.println(sol.minimumTravelTime(List.of(new ArrayList<>()), 0, 0));          // 0
        System.out.println(sol.minimumTravelTime(List.of(List.of(List.of(1,2)),new ArrayList<>()), 0, 1));  // 2
        System.out.println(sol.minimumTravelTime(List.of(List.of(List.of(1,1)),new ArrayList<>()), 0, 1));  // 1
    }
}
```

### Complexity Analysis

| Problem | Time | Space |
|---|---|---|
| Minimum cost path | O((R × C) log(R × C)) | O(R × C) |
| Cheapest flights with K stops | O((N × (K+2)) log(N × (K+2))) | O(N × (K+2)) |
| Minimum travel time | O((N + E) log N) | O(N) |

Adding state dimensions multiplies the search space by the size of the state — so the cheapest-flights problem with K stops is K times slower than vanilla Dijkstra. The trade-off is worth it: a stateful Dijkstra handles a much richer family of constraints than the unstateful one.

</details>

# Problem: Teleporter Grid

## The Problem

Given an **NxM** **grid**, a **source** cell `(r1, c1)`, and a **destination** cell `(r2, c2)`, write a function to find and return the minimum cost to reach from the source to the destination. 

> -   The cost to move from a cell to its adjacent cell is `1`.
> -   A value of `0` in a cell means the call cannot be visited.
> -   A value of `1` in a cell means the cell can be visited.
> -   A value greater than `1` in a cell is a teleporter cell. All cells with the same number represent linked teleporters. Moving into a teleporter costs `1`, and you may instantly teleport to any other teleporter with the same ID at a cost of `1`.
> -   Each teleporter may be used at most once during the path.

> You must abide by the following constraint:
>
> -   You can only move in the four cardinal directions, i.e., `up`, `right`, `down`, and `left`.

```
Input:  grid = [[1, 5, 0, 2], [0, 1, 1, 0], [2, 0, 1, 1], [1, 5, 5, 1]], source = [0, 0], destination = [3, 3]
Output: 3
Input:  grid = [[1, 5, 0, 5], [0, 1, 1, 2], [2, 0, 1, 0], [1, 3, 3, 2]], source = [0, 0], destination = [3, 3]
Output: 5
```

<details>
<summary><h2>Pattern Mapping</h2></summary>


This is grid Dijkstra with a state dimension for teleporter usage. The puzzle says a teleporter may be used **at most once** on the whole path, so the search state is `(row, col, teleporter_used)` where `teleporter_used` is `0` or `1` — and `min_cost` becomes a 3D array indexed by that flag. The same cell reached without having teleported is a different state from the same cell reached after teleporting.

Cardinal moves cost `1` and keep the flag unchanged. When the popped cell sits on a teleporter and the flag is still `0`, every linked teleporter (same ID, excluding the current cell) is relaxed at cost `1` with the flag flipped to `1` — capturing the one-shot rule. The standard min-heap, infinity-initialised distances, and lazy stale-skip otherwise behave exactly as in plain Dijkstra.

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
import heapq
from typing import List, Tuple, Dict

# Structure to represent a Cell in the graph
class Cell:
    def __init__(
        self, row: int, col: int, cost: int, teleporter_used: int
    ):
        self.row = row
        self.col = col
        self.cost = cost
        self.teleporter_used = teleporter_used

    # Comparator function for the priority queue to create a min-heap
    def __lt__(self, other):
        return self.cost < other.cost

class Solution:
    def is_valid_cell(
        self, grid: List[List[int]], row: int, col: int
    ) -> bool:
        return (
            0 <= row < len(grid)
            and 0 <= col < len(grid[0])
            and grid[row][col] != 0
        )

    def build_teleporter_map(
        self, grid: List[List[int]]
    ) -> Dict[int, List[Tuple[int, int]]]:
        teleporters = {}
        for row in range(len(grid)):
            for col in range(len(grid[row])):
                if grid[row][col] > 1:
                    teleporters.setdefault(grid[row][col], []).append(
                        (row, col)
                    )
        return teleporters

    def teleporter_grid(
        self,
        grid: List[List[int]],
        source: Tuple[int, int],
        destination: Tuple[int, int],
    ) -> int:
        rows = len(grid)
        if rows == 0:
            return -1

        cols = len(grid[0])

        # 3D minCost: rows x cols x 2 (teleporter usage state)
        min_cost = [
            [[float("inf")] * 2 for _ in range(cols)]
            for _ in range(rows)
        ]

        # Build teleporter map for quick access to teleporter pairs
        teleporters = self.build_teleporter_map(grid)

        # Create a priority queue (min-heap) to store the cells with
        # their costs
        pq = []

        # Assign the minimum cost of the starting point with teleporter
        # unused
        min_cost[source[0]][source[1]][0] = 0

        # Enqueue starting cell and the cost to move on it
        heapq.heappush(pq, Cell(source[0], source[1], 0, 0))

        # Define the possible movements: up, right, down, left
        directions: List[Tuple[int, int]] = [
            (-1, 0),  # up
            (0, 1),   # right
            (1, 0),   # down
            (0, -1)   # left
        ]

        while pq:
            curr_cell = heapq.heappop(pq)
            curr_row = curr_cell.row
            curr_col = curr_cell.col
            cost = curr_cell.cost
            teleporter_used = curr_cell.teleporter_used

            # If we reached the destination, return the cost
            if curr_row == destination[0] and curr_col == destination[1]:
                return cost

            # If the cost is greater than the recorded minimum cost, skip
            # processing
            if cost > min_cost[curr_row][curr_col][teleporter_used]:
                continue

            # Explore the neighbours
            for dr, dc in directions:
                new_row = curr_row + dr
                new_col = curr_col + dc

                # Check if the new cell is within the grid
                if self.is_valid_cell(grid, new_row, new_col):

                    # Cost to move to an adjacent cell is always 1
                    new_cost = cost + 1

                    # If a shorter path is found, update the minimum
                    # cost and add the new cell to the priority queue
                    if (
                        new_cost
                        < min_cost[new_row][new_col][teleporter_used]
                    ):

                        # Update the minimum cost for the new cell
                        min_cost[new_row][new_col][
                            teleporter_used
                        ] = new_cost

                        # Add the new cell to the priority queue
                        heapq.heappush(
                            pq,
                            Cell(
                                new_row,
                                new_col,
                                new_cost,
                                teleporter_used,
                            ),
                        )

            # Add teleporter usage if on a teleporter cell and teleporter
            # not used yet
            if grid[curr_row][curr_col] > 1 and teleporter_used == 0:
                teleporter_id = grid[curr_row][curr_col]
                for new_row, new_col in teleporters.get(
                    teleporter_id, []
                ):

                    # Skip the current cell
                    if new_row == curr_row and new_col == curr_col:
                        continue

                    # Teleportation cost is 1 (same as moving to adjacent
                    # cell)
                    new_cost = cost + 1

                    # If a shorter path is found using the teleporter, update the minimum
                    # cost and add the new cell to the priority queue
                    if new_cost < min_cost[new_row][new_col][1]:
                        min_cost[new_row][new_col][1] = new_cost
                        heapq.heappush(
                            pq, Cell(new_row, new_col, new_cost, 1)
                        )

        # If the destination is unreachable, return -1
        return -1


# Examples from the problem statement
print(Solution().teleporter_grid([[1,5,0,2],[0,1,1,0],[2,0,1,1],[1,5,5,1]], (0,0), (3,3)))  # 3
print(Solution().teleporter_grid([[1,5,0,5],[0,1,1,2],[2,0,1,0],[1,3,3,2]], (0,0), (3,3)))  # 5

# Edge cases
print(Solution().teleporter_grid([], (0,0), (0,0)))                                         # -1 — empty grid
print(Solution().teleporter_grid([[1]], (0,0), (0,0)))                                      # 0 — source is dest
print(Solution().teleporter_grid([[1,1],[1,1]], (0,0), (1,1)))                              # 2 — no teleporters
print(Solution().teleporter_grid([[1,0],[0,1]], (0,0), (1,1)))                              # -1 — blocked path
print(Solution().teleporter_grid([[2,1],[1,2]], (0,0), (1,1)))                              # 1 — teleport shortcut
```

```java run
import java.util.*;

public class Main {
    // Structure to represent a Cell in the graph
    static class Cell {

        int row;
        int col;
        int cost;
        int teleporterUsed;

        Cell(int row, int col, int cost, int teleporterUsed) {
            this.row = row;
            this.col = col;
            this.cost = cost;
            this.teleporterUsed = teleporterUsed;
        }
    }

    // Comparator function for the priority queue to create a min-heap
    static class CompareMinHeap implements Comparator<Cell> {
        public int compare(Cell a, Cell b) {

            // Min-heap based on cost
            return Integer.compare(a.cost, b.cost);
        }
    }

    static class Solution {
        private boolean isValidCell(int[][] grid, int row, int col) {
            return (
                row >= 0 &&
                row < grid.length &&
                col >= 0 &&
                col < grid[0].length &&
                grid[row][col] != 0
            );
        }

        private Map<Integer, List<List<Integer>>> buildTeleporterMap(
            int[][] grid
        ) {
            Map<Integer, List<List<Integer>>> teleporters = new HashMap<>();
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    if (grid[row][col] > 1) {
                        teleporters
                            .computeIfAbsent(
                                grid[row][col],
                                k -> new ArrayList<>()
                            )
                            .add(Arrays.asList(row, col));
                    }
                }
            }
            return teleporters;
        }

        public int teleporterGrid(
            int[][] grid,
            List<Integer> source,
            List<Integer> destination
        ) {
            int rows = grid.length;
            if (rows == 0) {
                return -1;
            }

            int cols = grid[0].length;

            // 3D minCost: rows x cols x 2 (teleporter usage state)
            int[][][] minCost = new int[rows][cols][2];
            for (int[][] arr : minCost) {
                for (int[] a : arr) {
                    Arrays.fill(a, Integer.MAX_VALUE);
                }
            }

            // Build teleporter map for quick access to teleporter pairs
            Map<Integer, List<List<Integer>>> teleporters =
                buildTeleporterMap(grid);

            // Create a priority queue (min-heap) to store the cells with
            // their costs
            PriorityQueue<Cell> pq = new PriorityQueue<>(
                new CompareMinHeap()
            );

            // Assign the minimum cost of the starting point with teleporter
            // unused
            minCost[source.get(0)][source.get(1)][0] = 0;

            // Enqueue starting cell and the cost to move on it
            pq.add(new Cell(source.get(0), source.get(1), 0, 0));

            // Define the possible movements: up, right, down, left
            int[][] directions = {
                {-1, 0}, // up
                {0, 1},  // right
                {1, 0},  // down
                {0, -1}  // left
            };

            while (!pq.isEmpty()) {
                Cell currCell = pq.poll();
                int currRow = currCell.row;
                int currCol = currCell.col;
                int cost = currCell.cost;
                int teleporterUsed = currCell.teleporterUsed;

                // If we reached the destination, return the cost
                if (
                    currRow == destination.get(0) &&
                    currCol == destination.get(1)
                ) {
                    return cost;
                }

                // If the cost is greater than the recorded minimum cost,
                // skip processing
                if (cost > minCost[currRow][currCol][teleporterUsed]) {
                    continue;
                }

                // Explore the neighbours
                for (int[] dir : directions) {
                    int newRow = currRow + dir[0];
                    int newCol = currCol + dir[1];

                    // Check if the new cell is within the grid
                    if (isValidCell(grid, newRow, newCol)) {

                        // Cost to move to an adjacent cell is always 1
                        int newCost = cost + 1;

                        // If a shorter path is found, update the minimum
                        // cost and add the new cell to the priority queue
                        if (
                            newCost < minCost[newRow][newCol][teleporterUsed]
                        ) {

                            // Update the minimum cost for the new cell
                            minCost[newRow][newCol][teleporterUsed] =
                                newCost;

                            // Add the new cell to the priority queue
                            pq.add(
                                new Cell(
                                    newRow,
                                    newCol,
                                    newCost,
                                    teleporterUsed
                                )
                            );
                        }
                    }
                }

                // Add teleporter usage if on a teleporter cell and
                // teleporter not used yet
                if (grid[currRow][currCol] > 1 && teleporterUsed == 0) {
                    int teleporterID = grid[currRow][currCol];
                    for (List<Integer> teleporter : teleporters.get(
                        teleporterID
                    )) {
                        int newRow = teleporter.get(0);
                        int newCol = teleporter.get(1);

                        // Skip the current cell
                        if (newRow == currRow && newCol == currCol) {
                            continue;
                        }

                        // Teleportation cost is 1 (same as moving to
                        // adjacent cell)
                        int newCost = cost + 1;

                        // If a shorter path is found using the teleporter,
                        // update the minimum cost and add the new cell to
                        // the priority queue
                        if (newCost < minCost[newRow][newCol][1]) {
                            minCost[newRow][newCol][1] = newCost;
                            pq.add(new Cell(newRow, newCol, newCost, 1));
                        }
                    }
                }
            }

            // If the destination is unreachable, return -1
            return -1;
        }
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        // Examples from the problem statement
        System.out.println(sol.teleporterGrid(new int[][]{{1,5,0,2},{0,1,1,0},{2,0,1,1},{1,5,5,1}}, List.of(0,0), List.of(3,3)));  // 3
        System.out.println(sol.teleporterGrid(new int[][]{{1,5,0,5},{0,1,1,2},{2,0,1,0},{1,3,3,2}}, List.of(0,0), List.of(3,3)));  // 5

        // Edge cases
        System.out.println(sol.teleporterGrid(new int[][]{}, List.of(0,0), List.of(0,0)));            // -1
        System.out.println(sol.teleporterGrid(new int[][]{{1}}, List.of(0,0), List.of(0,0)));         // 0
        System.out.println(sol.teleporterGrid(new int[][]{{1,1},{1,1}}, List.of(0,0), List.of(1,1))); // 2
        System.out.println(sol.teleporterGrid(new int[][]{{1,0},{0,1}}, List.of(0,0), List.of(1,1))); // -1
        System.out.println(sol.teleporterGrid(new int[][]{{2,1},{1,2}}, List.of(0,0), List.of(1,1))); // 1
    }
}
```

</details>

<details>
<summary><h2>Final Takeaway</h2></summary>


Dijkstra is **the** weighted shortest-path tool, and the *pattern* extends naturally to stateful problems by augmenting nodes with extra dimensions (stops, parity, fuel left, …). The four-step recipe — *priority queue keyed by cost, distance array, lazy stale-skip, weight-aware relaxation* — handles essentially every "minimum cumulative cost" problem you'll meet, with or without state.

The full progression of shortest-path tools you now have:

| Tool | When |
|---|---|
| **BFS** | Unweighted graph; "minimum hops" |
| **Dijkstra** | Non-negative weights; "minimum cost" |
| **Bellman-Ford** | Negative weights allowed; detects negative cycles |
| **Floyd-Warshall** | All-pairs shortest path |

You've now completed the **graph chapter**. Together with the data-structure foundations (arrays, linked lists, hash tables, stacks, queues, trees) and the algorithmic patterns covered here (DFS, components, two-colour, BFS-shortest-path, Dijkstra), you have the toolkit to solve essentially every graph-shaped problem you'll meet — interview, real-world, or research.

> **Transfer challenge.** A real GPS routing system has to consider *real-time traffic* — edge weights change throughout the day. Sketch (don't implement) how you'd extend the Dijkstra pattern to handle "the cost of edge `u → v` depends on the time you arrive at `u`."

</details>
<details>
<summary><strong>Sketch</strong></summary>

This is **time-dependent Dijkstra**. Each edge weight is a function `w(u, v, t)` of arrival time `t` at `u`. When relaxing, compute `new_time = t + w(u, v, t)`. Push `(new_time, v)`. The classical Dijkstra invariant still holds *if* the cost function is non-decreasing in time (no time-travel arbitrage) — known as the *FIFO property*. Real GPS systems use this exact pattern with traffic predictions for `w`.

When the FIFO property fails (e.g. carpool lanes become free at certain times), you'd switch to label-correcting algorithms — closer to Bellman-Ford. The pattern stretches; the underlying logic is the same.

</details>
