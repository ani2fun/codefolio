---
title: Count-Min Sketch
summary: A probabilistic data structure for streaming frequency estimation. Estimates "how many times has X appeared in this stream?" in sublinear memory. The structure behind real-time analytics dashboards and DDoS detection.
prereqs:
  - probabilistic-and-advanced-bloom-filter
---

# 3. Count-Min Sketch

## The Hook

You're running a CDN. Each second, a million HTTP requests stream past. You need to answer, in real time: *which IPs are hitting us most often, and how often?* A counter per IP works for thousands of IPs but fails for billions of IPs (the long tail of the global internet).

The **Count-Min Sketch (CMS)** estimates frequencies in a stream using fixed memory, regardless of how many distinct items appear. Like the Bloom filter, it's probabilistic — it returns "frequency at most X" with high probability, with overestimation possible (never underestimation).

The structure: a 2D grid of counters of size `d × w`, plus `d` independent hash functions. To **add** an item, hash it `d` times; increment each of the `d` cells. To **query** an item's frequency, hash it `d` times; return the *minimum* of the `d` cells.

Compared to a hash map: CMS is `O(d × w)` memory regardless of stream size. Compared to a Bloom filter: CMS counts (not just membership). The error grows with the stream's "weight"; tuning `d` and `w` controls the error bounds.

---

## Table of contents

1. [The 2D counter grid](#the-2d-counter-grid)
2. [Why "min"](#why-min)
3. [Tuning `d` and `w`](#tuning-d-and-w)
4. [Implementation](#implementation)
5. [Heavy hitters extension](#heavy-hitters-extension)
6. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
7. [Production reality](#production-reality)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The 2D counter grid

A CMS is a `d × w` matrix of integer counters, all initially 0, plus `d` independent hash functions `h_1, h_2, …, h_d`, each mapping items to `[0, w)`.

```
sketch[d=4, w=8]:
       col 0  col 1  col 2  col 3  col 4  col 5  col 6  col 7
row 0:    0      0      0      0      0      0      0      0
row 1:    0      0      0      0      0      0      0      0
row 2:    0      0      0      0      0      0      0      0
row 3:    0      0      0      0      0      0      0      0
```

`add(x)`: `for i in 0..d: sketch[i][h_i(x)] += 1`.

`query(x)`: `min over i of sketch[i][h_i(x)]`.

***

# Why "min"

Each counter cell `sketch[i][h_i(x)]` *overestimates* the count of `x` because it accumulates from any other item `y` that happens to hash to the same cell. By taking the minimum across the `d` cells, we get the tightest upper bound — the cell with the *fewest* accidental hits.

Formally: if `f(x)` is the true count, the CMS query returns `f̂(x)` such that `f(x) ≤ f̂(x) ≤ f(x) + ε · ||T||_1` with probability ≥ `1 − δ`, where `||T||_1` is the total stream weight (count of all items). `ε` and `δ` are controlled by `w` and `d`.

***

# Tuning `d` and `w`

For target additive error `ε · N` (where `N` is total stream count) and confidence `1 − δ`:

```
w = ⌈e / ε⌉
d = ⌈ln(1/δ)⌉
```

Example: `ε = 0.001` (error within 0.1% of N), `δ = 0.01` (1% chance of exceeding the bound). `w ≈ 2718`, `d ≈ 5`. Total memory: `~14k` 32-bit counters = **56 KB**, *regardless of how many distinct items appear*.

For an alternative with more variance but better practical accuracy, "Conservative Update" only increments the *smallest* cells in the row — but the analysis is more complex.

***

# Implementation

```python run
import hashlib

class CountMinSketch:
    def __init__(self, w=2718, d=5):
        self.w = w
        self.d = d
        self.table = [[0] * w for _ in range(d)]

    def _hashes(self, x):
        data = str(x).encode()
        for i in range(self.d):
            h = int(hashlib.sha256(data + str(i).encode()).hexdigest()[:16], 16)
            yield h % self.w

    def add(self, x, count=1):
        for i, h in enumerate(self._hashes(x)):
            self.table[i][h] += count

    def query(self, x):
        return min(self.table[i][h] for i, h in enumerate(self._hashes(x)))


if __name__ == "__main__":
    cms = CountMinSketch(w=2718, d=5)

    # Stream simulation: skewed distribution
    import random
    random.seed(42)
    stream = []
    for _ in range(100_000):
        if random.random() < 0.1:
            stream.append("hot_item")
        elif random.random() < 0.5:
            stream.append(f"warm_{random.randint(0, 9)}")
        else:
            stream.append(f"cold_{random.randint(0, 99_999)}")

    for x in stream:
        cms.add(x)

    # True count for verification
    from collections import Counter
    truth = Counter(stream)
    print(f"hot_item:  truth={truth['hot_item']}      cms={cms.query('hot_item')}")
    print(f"warm_3:    truth={truth['warm_3']}      cms={cms.query('warm_3')}")
    print(f"cold_50:   truth={truth.get('cold_50', 0)}      cms={cms.query('cold_50')}")
    print(f"never_seen: truth=0      cms={cms.query('never_seen')}")
    print(f"Memory: {cms.w * cms.d * 4 / 1024:.1f} KB for any stream size")
```

```java run
import java.util.*;

class Solution {
    int w = 2718, d = 5;
    int[][] table = new int[d][w];

    int[] hashes(String x) {
        int[] out = new int[d];
        long h = x.hashCode();
        for (int i = 0; i < d; i++) {
            h = (h * 31 + i * 0xdeadbeefL) & 0xffffffffL;
            out[i] = (int) (Math.abs(h) % w);
        }
        return out;
    }

    void add(String x) {
        int[] hs = hashes(x);
        for (int i = 0; i < d; i++) table[i][hs[i]]++;
    }

    int query(String x) {
        int[] hs = hashes(x);
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < d; i++) min = Math.min(min, table[i][hs[i]]);
        return min;
    }

    public static void main(String[] args) {
        Solution cms = new Solution();
        for (int i = 0; i < 100_000; i++) cms.add("hot");
        System.out.println("query('hot') -> " + cms.query("hot"));
    }
}
```

***

# Heavy hitters extension

A common application: find the **top-K** most-frequent items in a stream — the "heavy hitters". The naive approach (sorted hash map of all items) costs `O(distinct items)` memory. The CMS approach:

1. Maintain a min-heap of size K.
2. For each incoming item, query its CMS estimate.
3. If it's larger than the heap minimum, push it.

`O(N log K + d × N)` time, `O(K + d × w)` memory. Used in network monitoring, real-time recommendation systems, and DDoS detection.

***

# Edge cases and pitfalls

- **Overestimation only.** CMS never underestimates. If your application can tolerate underestimates (which would be unusual), use a different sketch.
- **Cold items get inflated counts.** An item that appeared 0 times might query as 5+ if it happens to collide in every row. The min reduces but doesn't eliminate this. For low-frequency items, the relative error is large.
- **Hash function quality.** Like Bloom filters, CMS depends on independent uniform hashes. Bad hashes correlate cells and inflate errors.
- **Counter overflow.** With 32-bit counters, you can fit `~4 × 10⁹` total updates before any cell overflows. For longer streams or higher rates, use 64-bit counters or periodic decay.
- **Decay over time.** For "frequency in the last hour" rather than "all-time frequency", you need *windowed* sketches. Two CMS instances rotated periodically, or "exponential decay" multiplying counters by 0.99 per second.

***

# Production reality

- **Real-time analytics.** Druid, Pinot, ClickHouse, and other OLAP databases use CMS-style sketches for top-K queries on cardinality data.
- **DDoS detection.** Cloudflare, Akamai monitor IP-traffic frequencies in real time using sketches; high-frequency IPs trigger rate-limiting.
- **Network telemetry.** Cisco, Juniper switches use CMS-like sketches for "heavy flows" detection on telemetry streams.
- **Recommendation systems.** "Items seen with this user" frequency estimation in online recommender systems uses sketches when the catalog is huge.
- **Apache DataSketches.** A widely-used Java/C++ library implementing CMS, HyperLogLog, KLL quantiles, and other sketches. Used at Yahoo, Twitter, etc.
- **Redis Stream Data Types** (since v6.2) include `cms.*` commands for Count-Min Sketch as a first-class data type.

***

# Cross-links

- **Sibling structures:** [Bloom Filter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-bloom-filter) (membership), [HyperLogLog](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-hyperloglog) (cardinality estimation).
- **Used by:** real-time analytics, network monitoring, recommendation systems.

***

# Final takeaway

Count-Min Sketch is the streaming frequency estimator. Three patterns to internalise:

1. **Sublinear memory regardless of distinct items.** `d × w` cells; doesn't grow with stream cardinality.
2. **Overestimation only.** The "min over d rows" gives the tightest upper bound. Underestimation is impossible.
3. **The standard pair with Bloom filters.** Bloom: "is X in the set?". CMS: "how often is X in the stream?". Both probabilistic, both production-deployed, both tunable to your error budget.
