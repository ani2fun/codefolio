# 10. Pattern: Minimum Predicate Search

The previous patterns binary-searched a *sorted array* for a target index. This pattern binary-searches a **range of integer values** for the smallest value `x` that satisfies some predicate `P(x)`. The "search space" isn't an array — it's a numeric range. The "comparison" isn't `arr[mid] vs target` — it's a custom function `P(mid)` returning true or false.

This is **"binary search on the answer."** Many real-world questions fit:
- "Minimum speed needed to arrive on time?" — search speed values; predicate = "can we arrive within `hour` hours?"
- "Minimum cost penalty after k operations?" — search penalty values; predicate = "can we achieve this penalty?"
- "Smallest divisor that fits a budget?" — search divisor values.

The pattern requires *monotonicity*: if `P(x)` is true, then `P(x + 1)` must also be true (and so on). Once the predicate "flips" from false to true, it stays true. Binary search exploits this flip — the answer is the *first true*.

By the end of this lesson you'll know the diagnostic checks, the canonical "minimum-x-with-P-true" template, and four worked problems showing different predicates.

## Table of contents

1. [Identifying the pattern](#identifying-the-pattern)
2. [Punctual arrival speed](#punctual-arrival-speed)
3. [Penalty with balls](#penalty-with-balls)
4. [Minimum shipping capacity](#minimum-shipping-capacity)
5. [Trip completion frenzy](#trip-completion-frenzy)

***

# Identifying the Pattern

Three diagnostic questions:

| # | Question | If "yes," the pattern fits because... |
|---|---|---|
| **Q1** | We're optimizing for the *minimum* value satisfying a constraint? | Binary search finds the flip point. |
| **Q2** | The constraint is *monotonic* — if `x` works, then `x + 1` also works? | Required for the predicate to have a unique flip point. |
| **Q3** | Can we *evaluate* the predicate `P(x)` in `O(f(n))` time? | Each iteration costs `O(f(n))`; total is `O(f(n) · log range)`. |

If all three hold, the pattern applies. The total time is `O(log(range) · f(n))` — much faster than brute-forcing every value.

---

## The Template

```python run
def min_predicate_search(low, high, predicate):
    while low < high:
        mid = low + (high - low) // 2
        if predicate(mid):                    # mid works → try smaller
            high = mid
        else:                                  # mid doesn't work → try larger
            low = mid + 1
    return low                                 # smallest x with predicate(x) == true
```

Identical structure to lower bound on an array — the only difference is the predicate replaces `arr[mid] >= target`. The search space is `[low, high]` numeric range; the answer is `low` after the loop.

---

# Punctual Arrival Speed

## The Problem

Given bus distances `distance[]` and time budget `hour`, find the minimum integer speed at which all buses must travel for arrival within `hour`. Each bus departs at the next integer time after the previous bus arrives. Return `-1` if impossible.

```
Input:  distance = [1, 3, 5], hour = 2.5
Output: 10

Input:  distance = [1, 4, 9], hour = 6
Output: 3

Input:  distance = [1, 8, 10], hour = 2
Output: -1
```

<details>
<summary><h2>The Solution</h2></summary>


Predicate `can_reach(speed)`: simulate the rides; for the last leg, partial time counts; for prior legs, you must round up to the next integer. Binary-search speed in `[1, 10^7]`.


```python run
import math
from typing import List

class Solution:

    # Predicate: checks if it's possible to reach the destination on
    # time with a given speed
    def can_reach_on_time(
        self, distance: List[int], hour: float, speed: int
    ) -> bool:
        total_time = 0

        # Calculate the total time required to reach each checkpoint
        for i in range(len(distance) - 1):
            total_time += math.ceil(distance[i] / speed)

        # Add the time required to reach the final destination
        total_time += distance[-1] / speed

        # Check if the total time is less than or equal to the given hour
        return total_time <= hour

    def punctual_arrival_speed(
        self, distance: List[int], hour: float
    ) -> int:

        # Initialise the search space for speed with low as 1
        low: int = 1

        # Initialise high to a large value (1e7) as per problem
        # constraints
        high: int = int(1e7)

        # Perform binary search to find the minimum speed required to
        # reach the destination on time
        while low < high:

            # Find the middle speed to check if it is possible to reach
            # the destination on time
            mid = low + (high - low) // 2

            # mid is a possible speed, update the result and search
            # for a smaller speed
            if self.can_reach_on_time(distance, hour, mid):

                # Try to find a smaller speed
                high = mid

            # mid is not a possible speed, search for a larger speed
            else:

                # Try to find a larger speed
                low = mid + 1

        # After the search, low is the candidate minimum speed
        # Check if it actually works, as it could be possible that no
        # speed allows reaching on time
        if not self.can_reach_on_time(distance, hour, low):
            return -1

        # Return the minimum speed found
        return low


# Examples from the problem statement
print(Solution().punctual_arrival_speed([1, 3, 5], 2.5))   # 10
print(Solution().punctual_arrival_speed([1, 4, 9], 6))     # 3
print(Solution().punctual_arrival_speed([1, 8, 10], 2))    # -1

# Edge cases
print(Solution().punctual_arrival_speed([1], 1))           # 1   (single ride, exactly on time)
print(Solution().punctual_arrival_speed([5], 1))           # 5   (single ride, speed = distance)
print(Solution().punctual_arrival_speed([1, 1, 1], 3))     # 1   (minimal speed)
print(Solution().punctual_arrival_speed([1, 2], 1.9))      # -1  (impossible — need integer depart)
print(Solution().punctual_arrival_speed([10, 10, 10], 5))  # 10  (last ride is exact)
```

```java run
public class Main {
    static class Solution {

        // Predicate: checks if it's possible to reach the destination on
        // time with a given speed
        private boolean canReachOnTime(
            int[] distance,
            double hour,
            int speed
        ) {
            double totalTime = 0;

            // Calculate the total time required to reach each checkpoint
            for (int i = 0; i < distance.length - 1; i++) {
                totalTime += Math.ceil((double) distance[i] / speed);
            }

            // Add the time required to reach the final destination
            totalTime += (double) distance[distance.length - 1] / speed;

            // Check if the total time is less than or equal to the given
            // hour
            return totalTime <= hour;
        }

        public int punctualArrivalSpeed(int[] distance, double hour) {

            // Initialise the search space for speed with low as 1
            int low = 1;

            // Initialise high to a large value (1e7) as per problem
            // constraints
            int high = (int) 1e7;

            // Perform binary search to find the minimum speed required to
            // reach the destination on time
            while (low < high) {

                // Find the middle speed to check if it is possible to reach
                // the destination on time
                int mid = low + (high - low) / 2;

                // mid is a possible speed, update the result and search
                // for a smaller speed
                if (canReachOnTime(distance, hour, mid)) {

                    // Try to find a smaller speed
                    high = mid;
                }

                // mid is not a possible speed, search for a larger speed
                else {

                    // Try to find a larger speed
                    low = mid + 1;
                }
            }

            // After the search, low is the candidate minimum speed
            // Check if it actually works, as it could be possible that no
            // speed allows reaching on time
            if (!canReachOnTime(distance, hour, low)) {
                return -1;
            }

            // Return the minimum speed found
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1, 3, 5}, 2.5));   // 10
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1, 4, 9}, 6));     // 3
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1, 8, 10}, 2));    // -1

        // Edge cases
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1}, 1));           // 1
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{5}, 1));           // 5
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1, 1, 1}, 3));     // 1
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{1, 2}, 1.9));      // -1
        System.out.println(new Solution().punctualArrivalSpeed(new int[]{10, 10, 10}, 5));  // 10
    }
}
```

</details>


***

# Penalty With Balls

## The Problem

Given bag sizes `bags[]` and `maxOperations`, you can split any bag into two non-zero parts (counts as one operation). Minimize the largest bag size after at most `maxOperations` splits.

```
Input:  bags = [9, 7, 6], maxOperations = 3
Output: 5

Input:  bags = [4, 8], maxOperations = 1
Output: 4

Input:  bags = [4, 2], maxOperations = 4
Output: 1
```

<details>
<summary><h2>The Solution</h2></summary>


Predicate: "can we achieve max-bag-size = penalty?" — a bag of size `b > penalty` needs `(b - 1) // penalty` splits. Sum and check against `maxOperations`. Binary-search penalty in `[1, max(bags)]`.


```python run
from typing import List

class Solution:

    # Predicate: checks if it's possible to achieve a given penalty
    # (max number of balls in any bag)
    def can_achieve_penalty(
        self, bags: List[int], max_operations: int, penalty: int
    ) -> bool:
        operations = 0
        for balls in bags:

            # If a bag has more than 'penalty' balls, we need to split
            # it. The number of splits required for a bag with 'balls'
            # is (balls - 1) / penalty
            if balls > penalty:

                # This is the number of splits required
                operations += (balls - 1) // penalty

        # Check if we can do the splits within max_operations
        return operations <= max_operations

    def penalty_with_balls(
        self, bags: List[int], max_operations: int
    ) -> int:

        # The minimum penalty is at least 1 ball in a bag
        low = 1

        # The maximum penalty is the maximum number of balls in a
        # single bag
        high = max(bags)

        while low < high:

            # Calculate the middle penalty
            mid = low + (high - low) // 2

            # If we can achieve this penalty, this is a potential answer
            # so try to find a smaller one
            if self.can_achieve_penalty(bags, max_operations, mid):

                # Try a smaller penalty
                high = mid

            # If we can't achieve this penalty, try a larger one
            else:

                # Try a larger penalty
                low = mid + 1

        # After the search, low is the minimum penalty achievable
        return low


# Examples from the problem statement
print(Solution().penalty_with_balls([9, 7, 6], 3))   # 5
print(Solution().penalty_with_balls([4, 8], 1))      # 4
print(Solution().penalty_with_balls([4, 2], 4))      # 1

# Edge cases
print(Solution().penalty_with_balls([1], 0))         # 1   (single bag, no ops)
print(Solution().penalty_with_balls([1], 5))         # 1   (already size 1)
print(Solution().penalty_with_balls([10], 1))        # 5   (split once: [5,5])
print(Solution().penalty_with_balls([6, 6], 0))      # 6   (no operations)
print(Solution().penalty_with_balls([2, 4, 6], 6))   # 1   (enough ops for penalty=1)
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Predicate: checks if it's possible to achieve a given penalty
        // (max number of balls in any bag)
        private boolean canAchievePenalty(
            int[] bags,
            int maxOperations,
            int penalty
        ) {
            int operations = 0;
            for (int balls : bags) {

                // If a bag has more than 'penalty' balls, we need to split
                // it. The number of splits required for a bag with 'balls'
                // is (balls - 1) / penalty
                if (balls > penalty) {

                    // This is the number of splits required
                    operations += (balls - 1) / penalty;
                }
            }

            // Check if we can do the splits within maxOperations
            return operations <= maxOperations;
        }

        public int penaltyWithBalls(int[] bags, int maxOperations) {

            // The minimum penalty is at least 1 ball in a bag
            int low = 1;

            // The maximum penalty is the maximum number of balls in a
            // single bag
            int high = Arrays.stream(bags).max().getAsInt();

            while (low < high) {

                // Calculate the middle penalty
                int mid = low + (high - low) / 2;

                // If we can achieve this penalty, this is a potential answer
                // so try to find a smaller one
                if (canAchievePenalty(bags, maxOperations, mid)) {

                    // Try a smaller penalty
                    high = mid;
                }

                // If we can't achieve this penalty, try a larger one
                else {

                    // Try a larger penalty
                    low = mid + 1;
                }
            }

            // After the search, low is the minimum penalty achievable
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().penaltyWithBalls(new int[]{9, 7, 6}, 3));   // 5
        System.out.println(new Solution().penaltyWithBalls(new int[]{4, 8}, 1));      // 4
        System.out.println(new Solution().penaltyWithBalls(new int[]{4, 2}, 4));      // 1

        // Edge cases
        System.out.println(new Solution().penaltyWithBalls(new int[]{1}, 0));         // 1
        System.out.println(new Solution().penaltyWithBalls(new int[]{1}, 5));         // 1
        System.out.println(new Solution().penaltyWithBalls(new int[]{10}, 1));        // 5
        System.out.println(new Solution().penaltyWithBalls(new int[]{6, 6}, 0));      // 6
        System.out.println(new Solution().penaltyWithBalls(new int[]{2, 4, 6}, 6));   // 1
    }
}
```

</details>


***

# Minimum Shipping Capacity

## The Problem

Given package weights `weights[]` (in order) and `days`, find the minimum ship capacity that allows all packages to be shipped within `days`. Packages must be shipped in input order.

```
Input:  weights = [20, 10, 25, 35], days = 3
Output: 35

Input:  weights = [20, 10, 40, 30], days = 3
Output: 40

Input:  weights = [6, 3, 9], days = 3
Output: 18
```

<details>
<summary><h2>The Solution</h2></summary>


Predicate: "can we ship within `days` days at capacity `cap`?" — greedy: sum weights into a bucket; when adding next would exceed `cap`, start a new day. Count days. Binary-search `cap` in `[max(weights), sum(weights)]`.


```python run
from typing import List

class Solution:

    # Predicate: checks if it's possible to ship all packages within D days
    # given a maximum ship capacity
    def can_ship_capacity(
        self, weights: List[int], days: int, capacity: int
    ) -> bool:
        days_required = 1
        current_load = 0

        for weight in weights:

            # If a single package exceeds capacity, it's impossible
            if weight > capacity:
                return False

            # If adding this package doesn't exceed capacity, add it to
            # current day
            if current_load + weight <= capacity:
                current_load += weight
            else:

                # Otherwise, start a new day and put this package there
                days_required += 1
                current_load = weight

        # Return true if all packages can be shipped within D days
        return days_required <= days

    def minimum_shipping_capacity(
        self, weights: List[int], days: int
    ) -> int:

        # Minimum possible capacity is at least the heaviest package
        low = max(weights)

        # Maximum possible capacity is sum of all packages
        high = sum(weights)

        while low < high:

            # Calculate the middle capacity
            mid = low + (high - low) // 2

            # If it's possible, this is a potential answer
            # so try to find a smaller capacity
            if self.can_ship_capacity(weights, days, mid):

                # Try a smaller capacity
                high = mid

            # If it's not possible to ship with this capacity, try a
            # larger one
            else:

                # Try a larger capacity
                low = mid + 1

        # low is the minimum capacity to ship within D days
        return low


# Examples from the problem statement
print(Solution().minimum_shipping_capacity([20, 10, 25, 35], 3))  # 35
print(Solution().minimum_shipping_capacity([20, 10, 40, 30], 3))  # 40
print(Solution().minimum_shipping_capacity([6, 3, 9], 3))         # 18

# Edge cases
print(Solution().minimum_shipping_capacity([1], 1))               # 1   (single package)
print(Solution().minimum_shipping_capacity([5], 1))               # 5   (single heavy package)
print(Solution().minimum_shipping_capacity([1, 2, 3, 4, 5], 5))   # 5   (one package per day)
print(Solution().minimum_shipping_capacity([1, 2, 3, 4, 5], 1))   # 15  (all in one day)
print(Solution().minimum_shipping_capacity([3, 2, 2, 4, 1, 4], 3))  # 6
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Predicate: checks if it's possible to ship all packages within D
        // days given a maximum ship capacity
        private boolean canShipCapacity(
            int[] weights,
            int days,
            int capacity
        ) {
            int daysRequired = 1;
            int currentLoad = 0;

            for (int weight : weights) {

                // If a single package exceeds capacity, it's impossible
                if (weight > capacity) {
                    return false;
                }

                // If adding this package doesn't exceed capacity, add it to
                // current day
                if (currentLoad + weight <= capacity) {
                    currentLoad += weight;
                }

                // Otherwise, start a new day and put this package there
                else {
                    daysRequired++;
                    currentLoad = weight;
                }
            }

            // Return true if all packages can be shipped within D days
            return daysRequired <= days;
        }

        public int minimumShippingCapacity(int[] weights, int days) {

            // Minimum possible capacity is at least the heaviest package
            int low = Arrays.stream(weights).max().getAsInt();

            // Maximum possible capacity is sum of all packages
            int high = Arrays.stream(weights).sum();

            while (low < high) {

                // Calculate the middle capacity
                int mid = low + (high - low) / 2;

                // If it's possible, this is a potential answer
                // so try to find a smaller capacity
                if (canShipCapacity(weights, days, mid)) {

                    // Try a smaller capacity
                    high = mid;
                }

                // If it's not possible to ship with this capacity, try a
                // larger one
                else {

                    // Try a larger capacity
                    low = mid + 1;
                }
            }

            // low is the minimum capacity to ship within D days
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().minimumShippingCapacity(new int[]{20, 10, 25, 35}, 3));  // 35
        System.out.println(new Solution().minimumShippingCapacity(new int[]{20, 10, 40, 30}, 3));  // 40
        System.out.println(new Solution().minimumShippingCapacity(new int[]{6, 3, 9}, 3));         // 18

        // Edge cases
        System.out.println(new Solution().minimumShippingCapacity(new int[]{1}, 1));               // 1
        System.out.println(new Solution().minimumShippingCapacity(new int[]{5}, 1));               // 5
        System.out.println(new Solution().minimumShippingCapacity(new int[]{1, 2, 3, 4, 5}, 5));   // 5
        System.out.println(new Solution().minimumShippingCapacity(new int[]{1, 2, 3, 4, 5}, 1));   // 15
        System.out.println(new Solution().minimumShippingCapacity(new int[]{3, 2, 2, 4, 1, 4}, 3));  // 6
    }
}
```

</details>


***

# Trip Completion Frenzy

## The Problem

Given an array `times[]` (each plane's per-trip duration) and `totalTrips`, find the minimum time required for the planes (operating independently) to complete `totalTrips` trips total.

```
Input:  times = [3, 4, 5], totalTrips = 4
Output: 6

Input:  times = [1, 2, 3], totalTrips = 5
Output: 3

Input:  times = [1], totalTrips = 5
Output: 5
```

<details>
<summary><h2>The Solution</h2></summary>


Predicate: "in time `t`, can we complete `totalTrips`?" — each plane finishes `t / times[i]` trips. Sum and check ≥ totalTrips. Binary-search `t` in `[0, max(times) * totalTrips]` (the slowest plane finishing every trip alone is a safe upper bound).


```python run
from typing import List

class Solution:

    # Predicate: checks if it's possible to complete at least
    # 'totalTrips' in 'time' time
    def can_complete_trips(
        self, times: List[int], total_trips: int, time: int
    ) -> bool:
        trips_completed = 0
        for t in times:

            # Calculate how many trips each plane can complete in 'time'
            trips_completed += time // t

        # Check if the total trips are enough
        return trips_completed >= total_trips

    def trip_completion_frenzy(
        self, times: List[int], total_trips: int
    ) -> int:

        # The minimum time required is 0
        low = 0

        # The high boundary is the maximum time taken by any plane
        high = max(times) * total_trips

        while low < high:

            # Calculate the middle time
            mid = low + (high - low) // 2

            # If we can complete the trips in 'mid' time, try for
            # smaller time
            if self.can_complete_trips(times, total_trips, mid):

                # Try a smaller time
                high = mid

            # If we can't complete the trips, try larger time
            else:

                # Try a larger time
                low = mid + 1

        # After the search, low is the minimum time required
        return low


# Examples from the problem statement
print(Solution().trip_completion_frenzy([3, 4, 5], 4))   # 6
print(Solution().trip_completion_frenzy([1, 2, 3], 5))   # 3
print(Solution().trip_completion_frenzy([1], 5))         # 5

# Edge cases
print(Solution().trip_completion_frenzy([1], 1))         # 1   (single plane, one trip)
print(Solution().trip_completion_frenzy([2], 3))         # 6   (single slow plane)
print(Solution().trip_completion_frenzy([1, 1], 4))      # 2   (two identical fast planes)
print(Solution().trip_completion_frenzy([5, 10], 1))     # 5   (fastest plane handles single trip)
print(Solution().trip_completion_frenzy([1, 2, 3], 10))  # 6
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Predicate: checks if it's possible to complete at least
        // 'totalTrips' in 'time' time
        private boolean canCompleteTrips(
            int[] times,
            int totalTrips,
            int time
        ) {
            int tripsCompleted = 0;
            for (int t : times) {

                // Calculate how many trips each plane can complete in 'time'
                tripsCompleted += time / t;
            }

            // Check if the total trips are enough
            return tripsCompleted >= totalTrips;
        }

        public int tripCompletionFrenzy(int[] times, int totalTrips) {

            // The minimum time required is 0
            int low = 0;

            // The high boundary is the maximum time taken by any plane
            int high = Arrays.stream(times).max().getAsInt() * totalTrips;

            while (low < high) {

                // Calculate the middle time
                int mid = low + (high - low) / 2;

                // If we can complete the trips in 'mid' time, try for
                // smaller time
                if (canCompleteTrips(times, totalTrips, mid)) {

                    // Try a smaller time
                    high = mid;
                }

                // If we can't complete the trips, try larger time
                else {

                    // Try a larger time
                    low = mid + 1;
                }
            }

            // After the search, low is the minimum time required
            return low;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{3, 4, 5}, 4));   // 6
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{1, 2, 3}, 5));   // 3
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{1}, 5));         // 5

        // Edge cases
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{1}, 1));         // 1
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{2}, 3));         // 6
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{1, 1}, 4));      // 2
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{5, 10}, 1));     // 5
        System.out.println(new Solution().tripCompletionFrenzy(new int[]{1, 2, 3}, 10));  // 6
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


The minimum-predicate-search pattern: when you're optimizing for the smallest value satisfying a monotonic predicate, binary-search the value range. The four problems showed four different predicate functions — "can-reach," "can-achieve-penalty," "can-ship," "can-complete-trips" — each independently a creative greedy/simulation, but all sharing the same outer binary-search shell.

The next lesson (the Maximum Predicate Search Pattern lesson) is the dual: **maximum-predicate-search** — when you're optimizing for the largest value satisfying a predicate that's true-then-false (instead of false-then-true).

</details>