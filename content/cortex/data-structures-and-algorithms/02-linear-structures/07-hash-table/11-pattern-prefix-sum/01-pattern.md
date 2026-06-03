---
title: "Pattern: Prefix Sum"
summary: "Precompute a cumulative-sum array so any range-sum query is O(1); hash map maps prefix values to indices for subarray-sum problems."
prereqs:
  - 02-linear-structures/07-hash-table/01-introduction-to-hash-tables
---

# Understanding the prefix-sum pattern

The **prefix sum** of an array `arr` at index `i` is the sum of all elements from `arr[0]` through `arr[i]`, inclusive. Define `P[0] = 0` and `P[i] = arr[0] + arr[1] + ... + arr[i-1]` for `i ≥ 1`. With this convention, the sum of any subarray `arr[l..r]` equals **`P[r+1] − P[l]`** — a single subtraction once the prefix sums are computed.

> 🖼 Diagram — Prefix sums in action — once P is built, any subarray sum is one subtraction. Every subarray-sum question becomes a question about differences between prefix sums, which is exactly the kind of question a hash map answers in O(1).
```d2
direction: right

arr: arr {
  grid-columns: 6
  grid-gap: 0
  a0: "arr[0] = 3"
  a1: "arr[1] = 1"
  a2: "arr[2] = 4"
  a3: "arr[3] = 1"
  a4: "arr[4] = 5"
  a5: "arr[5] = 9"
}

p: prefix sums P {
  grid-columns: 6
  grid-gap: 0
  p0: "P[0] = 0"
  p1: "P[1] = 3"
  p2: "P[2] = 4"
  p3: "P[3] = 8"
  p4: "P[4] = 9"
  p5: "P[5] = 14"
}

q: |md
  **sum arr[2..4] = P[5] - P[2] = 14 - 4 = 10**
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}

arr -> p
p -> q
```

<p align="center"><strong>Prefix sums in action — once <code>P</code> is built, any subarray sum is <em>one subtraction</em>. Every subarray-sum question becomes a question about <em>differences</em> between prefix sums, which is exactly the kind of question a hash map answers in O(1).</strong></p>

The hash-map combination is the magic. Walking the array left-to-right while maintaining the running prefix sum, at each index we ask: *"is there an earlier prefix sum that, when subtracted from mine, gives the target?"* If we keep a hash map `{prefixSum → index}` (or list of indices), the answer is a single lookup — O(1) per index, O(N) overall.

## Prefix-sum technique

Two flavours of the technique come up over and over:

- **Difference search** — given target `K`, find subarrays summing to `K`. Iterate while maintaining `P` and a map `{prefixSum → index/count}`. At each step, look up `P_current − K`; if it exists, we've found a subarray.
- **Same-value search** — given a property "subarray X is balanced" (zero-sum, equal 0s and 1s, etc.), encode the property so that "balanced" means *the same prefix value appears twice*. Iterate maintaining `P` and `{prefixSum → first-index}`; whenever `P_current` reappears, the slice between the two indices is balanced.

> 🖼 Diagram — Two flavours of the prefix-sum trick — both convert a hard subarray question into a fast lookup. The difference-search flavour solves "sum equals K"; the same-value-search flavour solves "balanced/zero-net" via clever encoding.
```d2
direction: right

diff: "Difference search — subarray sum = K" {
  d1: "maintain P_current"
  d2: "look up P_current - K in map"
  d3: "if hit -> subarray found"
  d1 -> d2 -> d3
}

same: "Same-value search — balanced subarray" {
  s1: "encode '+1/-1' so balanced = sum 0"
  s2: "look up P_current in map"
  s3: "if seen earlier -> balanced subarray"
  s1 -> s2 -> s3
}
```

<p align="center"><strong>Two flavours of the prefix-sum trick — both convert a hard subarray question into a fast lookup. The difference-search flavour solves "sum equals K"; the same-value-search flavour solves "balanced/zero-net" via clever encoding.</strong></p>

## Why Naive Isn't Enough

The obvious way to answer a subarray-sum question recomputes each subarray's sum from scratch. Fix a start index, fix an end index, then walk every element between them to add up the slice. The result is correct, but the inner walk re-adds elements the previous slice already summed.

This brute force pays for work it has already done. There are `O(N²)` start-end pairs, and summing each candidate slice is another `O(N)` walk, so the total is `O(N³)` time for `O(1)` space. A first optimisation caches the running sum per start index, dropping it to `O(N²)` time — still quadratic, because every one of the `O(N²)` pairs is visited.

To make this concrete: on `arr = [6, 3, -1, -3, 4]` the slice `arr[2..4]` and the slice `arr[1..4]` overlap in `arr[2]`, `arr[3]`, and `arr[4]`. The naive scan re-adds those three shared elements for each pair even though their partial sum never changed. As `N` grows, the fraction of repeated additions grows with it.

So the key idea is: recomputing each slice discards the cumulative total the previous index already produced, and one precomputed prefix array replaces every full re-scan with a single subtraction.

## The Core Idea

The fix precomputes one cumulative-sum array `P` so every later range query is a subtraction. Define `P[0] = 0` and `P[i] = arr[0] + ... + arr[i-1]`, so `P` holds the running total at each boundary. Once `P` is built, the sum of any slice `arr[l..r]` is `P[r+1] − P[l]` — constant time, no walk.

A hash map of past prefix values turns the *search* for a matching slice into a lookup. Two cases recur:

- **Difference search** — to find a slice summing to `K`, ask whether `P_current − K` has appeared before. A hit names a left boundary in `O(1)`.
- **Same-value search** — to find a *balanced* slice (zero net, equal counts), ask whether `P_current` has appeared before. A repeat marks the two ends of a zero-sum slice in `O(1)`.

To make this concrete: counting subarrays that sum to `0` keeps a map `{prefixSum → indices}`; each time a prefix value reappears, every earlier occurrence pairs with the current index to form one zero-sum slice. The core insight is: the per-index cost drops from `O(N)` (re-sum the slice) to `O(1)` (one map lookup), so the total drops from `O(N²)` to `O(N)`.

## How the Pointers Move

A single index sweeps left to right while a running aggregate and a hash map trail behind it. The index `i` advances every iteration; the aggregate absorbs `arr[i]` as `i` moves; the map remembers every prefix value the sweep has produced.

Each iteration runs the same three actions in a fixed order:

- **Accumulate** — fold `arr[i]` into the running aggregate to get the prefix value at `i`.
- **Query** — look up the value that would complete a solution (`aggregate − K` for difference search, `aggregate` itself for same-value search).
- **Record** — store the current prefix value with its index in the map, ready for a later query to match against.

To make this concrete: `i` sweeps from `0` to `N − 1`, so the loop runs exactly `N` times and never revisits an index. The aggregate is updated by one operation per step, and each map read and write is `O(1)` amortised. The core insight is: the sweep is one-directional and never backtracks, so a problem that looks like it needs nested loops collapses into a single `O(N)` pass.

## Algorithm — generic difference-search version

> **Algorithm**
>
> -   **Step 1:** Initialise `prefixSum = 0` and `map = {0: -1}` (the empty prefix has sum 0 and notional "index −1").
> -   **Step 2:** For `i` from 0 to `n − 1`:
>     -   `prefixSum += arr[i]`
>     -   If `prefixSum − K` is a key of `map`, the subarray `arr[map[prefixSum−K]+1 .. i]` sums to K.
>     -   Insert `prefixSum → i` into `map` (if not already there, depending on whether you want longest/first/count).

The base case `map[0] = -1` is what handles "the entire prefix from index 0 sums to K" — without it, you'd miss subarrays that start at index 0. *That is the single most common bug in prefix-sum code; if your tests show "off by one starting at index 0", check this line.*

## Implementation

A concrete example: count how many subarrays sum to K (a closely related variant of the longest-subarray problem from Lesson 9).


```python run viz=array viz-root=else
def prefix_sum_technique(arr: List[int], f) -> Dict[int, List[int]]:

    # Initialize a dictionary to map aggregated values to a list of indices
    prefix_sum_indices: Dict[int, List[int]] = {}

    # Initialize an aggregate with a default value
    aggregate = 0

    # Traverse the array from start to end
    for i in range(len(arr)):
        # Compute the prefix sum (or custom aggregation) from 0 to i
        aggregate = f(aggregate, arr[i])

        # Map the aggregated value to the current index
        if aggregate in prefix_sum_indices:
            prefix_sum_indices[aggregate].append(i)
        else:
            prefix_sum_indices[aggregate] = [i]

    return prefix_sum_indices
```

```java run viz=array viz-root=else
public class prefixSum {

    public HashMap<Integer, List<Integer>> prefixSumTechnique(List<Integer> arr) {
        // Initialize a hash map to map prefix aggregated values
        // to a list of indices
        HashMap<Integer, List<Integer>> prefixSumIndices = new HashMap<>();

        // Initialize an aggregate with a default value
        int aggregate = 0;

        // Traverse the array from start to end
        for (int i = 0; i < arr.size(); i++) {

            // Compute the prefix sum from 0 to i
            aggregate = f(aggregate, arr.get(i)); // Replace `f` with the appropriate function

            // Map aggregated value to current index
            if (prefixSumIndices.containsKey(aggregate)) {
                prefixSumIndices.get(aggregate).add(i);
            } else {
                List<Integer> indices = new ArrayList<>();
                indices.add(i);
                prefixSumIndices.put(aggregate, indices);
            }
        }
        return prefixSumIndices;
    }
}
```

## Complexity Analysis

The sweep visits each index once and does a constant number of `O(1)` hash-map operations per index — one accumulate, one query, one record. Total: **O(N)** time, where `N` is the length of the array.

The map stores at most one entry per distinct prefix value. When every prefix value is unique it holds `N` entries; when they all collide it holds one key mapped to `N` indices. Either way the space is **O(N)**.

> **Best/Average/Worst case** — O(N) time, O(N) space. The technique is not sensitive to input distribution; an adversarial array costs the same single pass as a friendly one.

# Identifying the prefix-sum pattern

This pattern fits problems where:

1. The answer involves a **subarray sum** (or a property reducible to a subarray sum, e.g. counts).
2. The array can contain **negatives** or **zeros** (which break sliding-window monotonicity).
3. The query is "exists / count / longest subarray with sum X" or "subarray that balances some property".

**Template:**
> Walk the array maintaining a running prefix value (sum, signed count, balance). At each step, query a hash map of past prefix values to find any earlier index that turns the current state into a solution. Update the map; repeat.

If the problem can be rephrased as "two prefix values that differ by D" (or "two prefix values that are equal"), this template fits.

## Recognition Checklist

Four questions confirm a problem fits the prefix-sum pattern. If every answer is "yes," the generic sweep drops in with only the per-problem aggregate and query to specialise.

1. **Does the answer depend on a subarray sum — or a property that reduces to one?** Sums, counts of zero-sum slices, "equal 0s and 1s", and net-zero balances all qualify, because each is a difference between two prefix values.
2. **Is the input a linear sequence — an array or string walked once, left to right?** The prefix value is defined by traversal order, so the data must be iterable end to end.
3. **Can the matching slice be found by a hash-map lookup on past prefix values?** Either `aggregate − K` (difference search) or `aggregate` itself (same-value search) must answer the question in `O(1)`.
4. **Does the rule survive negatives and zeros?** This is the disqualifier for a sliding window and the reason for prefix sums; if the values force a non-monotonic rule, this pattern is the right tool.

These four questions reappear as the **Diagnostic Questions** table in every problem write-up that follows.

## Canonical Example

Walk a full problem end to end to see the pattern click into place — the same-value-search flavour applied to a binary array.

### Problem Statement

> **Problem:** Given a binary array, find the longest subarray with an equal number of 0s and 1s.

Take `arr = [1, 0, 1, 1, 1, 0, 0]`. The expected answer is `6` — the slice from index `1` to index `6` holds three `0`s and three `1`s.

### Brute Force

The most direct approach fixes each start index, then extends an end index while tallying `0`s and `1`s, recording the length whenever the two counts match. It works, but the nested extension is the problem. Each of the `O(N)` start indices runs an `O(N)` extension, so the total is `O(N²)` time for `O(1)` space — unusable once `N` passes a few thousand.

### Key Insight

The encoding trick converts the count question into a sum question: **treat `0` as `−1` and `1` as `+1`**. Now "equal counts" becomes "the slice sums to `0`", which becomes "two prefix sums are equal". The core insight is: whenever a prefix value reappears, the slice between the two occurrences nets to zero, so the longest gap between two equal prefix sums is the answer — found in one pass with a hash map of first occurrences.

> 🖼 Diagram — Equal 0s/1s via re-encoding — turning 0 → −1 makes "equal counts" equivalent to "prefix-sum difference is 0". The longest subarray with equal counts is the longest gap between two equal prefix sums.
```d2
direction: right

arr: arr {
  grid-columns: 7
  grid-gap: 0
  a0: "1"
  a1: "0"
  a2: "1"
  a3: "1"
  a4: "1"
  a5: "0"
  a6: "0"
}

enc: "encoded (0 -> -1, 1 -> +1)" {
  grid-columns: 7
  grid-gap: 0
  e0: "+1"
  e1: "-1"
  e2: "+1"
  e3: "+1"
  e4: "+1"
  e5: "-1"
  e6: "-1"
}

p: prefix sums P {
  grid-columns: 8
  grid-gap: 0
  p0: "P[0]=0"
  p1: "P[1]=1"
  p2: "P[2]=0"
  p3: "P[3]=1"
  p4: "P[4]=2"
  p5: "P[5]=3"
  p6: "P[6]=2"
  p7: "P[7]=1"
}

arr -> enc
enc -> p
```

<p align="center"><strong>Equal 0s/1s via re-encoding — turning <code>0 → −1</code> makes "equal counts" equivalent to "prefix-sum difference is 0". The longest subarray with equal counts is the longest gap between two equal prefix sums.</strong></p>

> *Predict before reading on — in the encoded array <code>+1, -1, +1, +1, +1, -1, -1</code> with prefix sums <code>0, 1, 0, 1, 2, 3, 2</code>, prefix value <code>1</code> first appears at index 1 and reappears at index 3 (length 2) and index 7 (length 6). Prefix value <code>2</code> first appears at index 4 and reappears at index 6 (length 2). The best run is length 6 — that's our answer for "balanced binary subarray".*

### Optimized Solution

Apply the generic sweep with the aggregate as the `±1` running sum and a map keyed on first occurrence:

1. Accumulate — add `+1` for a `1` and `−1` for a `0` to the running `prefixSum`, seeding the map with `{0: -1}` so a balanced slice starting at index `0` is caught.
2. Query — if `prefixSum` has appeared before at index `j`, the slice `arr[j+1..i]` nets to zero, so its length is `i − j`; keep the maximum.
3. Record — store `prefixSum → i` only on its *first* appearance, which keeps the matched gap as long as possible.

The full Python and Java implementations live in [Balanced Binary Subarray](02-problems/03-balanced-binary-subarray). The base case `prefixSumIndex[0] = -1` there is what lets a balanced slice anchored at index `0` register its full length.

### Trace

Walk the example — `arr = [1, 0, 1, 1, 1, 0, 0]`, encoded as `+1, -1, +1, +1, +1, -1, -1`. The map stores each prefix value's first index; a repeat marks a balanced slice:

```
prefixSum=0, map={0:-1}, max=0

i=0  +1  P=1   first seen → store {1:0}            max=0
i=1  -1  P=0   seen @ -1 → len 1−(−1)=2            max=2
i=2  +1  P=1   seen @ 0  → len 2−0=2               max=2
i=3  +1  P=2   first seen → store {2:3}            max=2
i=4  +1  P=3   first seen → store {3:4}            max=2
i=5  -1  P=2   seen @ 3  → len 5−3=2               max=2
i=6  -1  P=1   seen @ 0  → len 6−0=6               max=6

result = 6
```

The result `6` matches the expected output — prefix value `1` first appears at index `0` and reappears at index `6`, so the slice `arr[1..6]` is the longest balanced run.

### Fitting the Template

| Check | Answer for Balanced Binary Subarray |
|---|---|
| **Q1.** Does the answer reduce to a subarray sum? | **Yes** — re-encoding `0 → −1` turns "equal counts" into "slice sums to `0`". |
| **Q2.** Is the input a linear sequence walked once? | **Yes** — a binary array, swept index by index. |
| **Q3.** Is the matching slice found by a hash-map lookup? | **Yes** — a repeated prefix value names both ends of a zero-sum slice in `O(1)`. |
| **Q4.** Does the rule survive negatives and zeros? | **Yes** — the `±1` encoding *introduces* negatives, exactly where a sliding window would fail. |

## Variants / Taxonomy

Every prefix-sum problem reuses the same sweep and varies only in what the aggregate encodes and what the map stores. Three shapes appear repeatedly across the problems that follow:

- **No-map prefix array** — precompute the prefix sums into an array and read range sums by subtraction; no hash map needed when the query is positional, not value-based. *Example:* First Equilibrium Point (left sum vs right sum at each split).
- **Same-value search** — store each prefix value's first index (for *longest*) or list of indices (for *all*); a repeat marks a balanced slice. *Examples:* Balanced Binary Subarray (longest zero-net run), Zero Sum Subarrays (every zero-sum slice).
- **Multiplicative cousin** — swap the additive aggregate for a product, building prefix and suffix products to answer "everything except me". *Example:* Self Excluded Array Product.

So the core insight is: the sweep never changes — only the aggregate (sum, signed count, product) and the map's payload (none, first-index, index-list) vary across the variants.

## Problems in This Category

The four problems below each specialise the prefix-sum sweep — only the aggregate and the query change:

| # | Problem | Variant | Twist on the sweep |
|---|---|---|---|
| 1 | [First Equilibrium Point](02-problems/01-first-equilibrium-point) | No-map prefix array | Read left and right range sums by subtraction at each split index |
| 2 | [Self Excluded Array Product](02-problems/02-self-excluded-array-product) | Multiplicative cousin | Combine a prefix product with a suffix product, no division |
| 3 | [Balanced Binary Subarray](02-problems/03-balanced-binary-subarray) | Same-value search | Re-encode `0 → −1`; longest gap between two equal prefix sums |
| 4 | [Zero Sum Subarrays](02-problems/04-zero-sum-subarrays) | Same-value search | Map each prefix sum to *all* its indices; emit every repeated pair |

Each is a small variation on the same sweep — only the aggregate and the question asked of the map change.
