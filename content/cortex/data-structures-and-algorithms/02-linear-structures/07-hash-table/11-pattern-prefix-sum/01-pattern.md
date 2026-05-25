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


```python run
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

```java run
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

# Identifying the prefix-sum pattern

This pattern fits problems where:

1. The answer involves a **subarray sum** (or a property reducible to a subarray sum, e.g. counts).
2. The array can contain **negatives** or **zeros** (which break sliding-window monotonicity).
3. The query is "exists / count / longest subarray with sum X" or "subarray that balances some property".

**Template:**
> Walk the array maintaining a running prefix value (sum, signed count, balance). At each step, query a hash map of past prefix values to find any earlier index that turns the current state into a solution. Update the map; repeat.

If the problem can be rephrased as "two prefix values that differ by D" (or "two prefix values that are equal"), this template fits.

## Example — equal 0s and 1s

> **Problem:** Given a binary array, find the longest subarray with an equal number of 0s and 1s.

The encoding trick: **treat 0 as −1 and 1 as +1**. Now "equal counts" becomes "subarray sum = 0", which becomes "two prefix sums are equal" — and the same-value-search flavour kicks in.

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

## Example problems

> -   First equilibrium point — split point where left sum equals right sum
> -   Self excluded array product — every position holds the product of all *other* elements
> -   Balanced binary subarray — longest run with equal 0s and 1s
> -   Zero sum subarrays — all subarrays that sum to zero

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

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
