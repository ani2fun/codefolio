# 10. Pattern: Prefix Sum

## The Hook

You're tracking the running balance in a bank account. After every transaction, you write down the new balance. Now someone asks: "Was there ever a stretch of consecutive transactions that exactly cancelled out — net zero change?". Walking every pair of indices and summing the transactions between them is O(N²). Brutal for a million-row ledger.

But notice — *if the balance after transaction i equals the balance after transaction j*, then the transactions between them must have summed to zero. The two balances are identical; whatever happened in between cancelled out perfectly. Suddenly, "find a zero-sum stretch" becomes "find any two equal balances" — and that's a one-pass scan with a hash map.

That's the **prefix-sum + hash** pattern. Where sliding windows fail (negatives! non-monotonic conditions!), prefix sums step in. The trick is brutal in its elegance: convert "*does some subarray sum to S?*" into "*do two prefix sums differ by S?*" — and a hash map of prefix sums turns *that* into O(1) per element. Subarray-sum-equals-K, balanced binary subarray, equilibrium points, zero-sum stretches, longest-with-property — every one of them is a small twist on the same idea.

This is the last hash-table pattern in the section, and it's the one that finishes the toolkit. Once you internalise *"subarray sum = difference of prefix sums"*, you'll start seeing it in problems that don't even mention sums.

---

## Table of contents

1. [Understanding the prefix-sum pattern](#understanding-the-prefix-sum-pattern)
2. [Identifying the prefix-sum pattern](#identifying-the-prefix-sum-pattern)
3. [First equilibrium point](#first-equilibrium-point)
4. [Self excluded array product](#self-excluded-array-product)
5. [Balanced binary subarray](#balanced-binary-subarray)
6. [Zero sum subarrays](#zero-sum-subarrays)

***

# Understanding the prefix-sum pattern

The **prefix sum** of an array `arr` at index `i` is the sum of all elements from `arr[0]` through `arr[i]`, inclusive. Define `P[0] = 0` and `P[i] = arr[0] + arr[1] + ... + arr[i-1]` for `i ≥ 1`. With this convention, the sum of any subarray `arr[l..r]` equals **`P[r+1] − P[l]`** — a single subtraction once the prefix sums are computed.

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


***

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

***

# First equilibrium point

## Problem Statement

Given an array `arr`, return the first index `i` such that `sum(arr[0..i-1]) == sum(arr[i+1..n-1])`. Return `-1` if no such index exists.

### Example 1
> -   **Input:** `arr = [1, 3, 5, 2, 2]` → **Output:** `2` (sum left = sum right = 4)

### Example 2
> -   **Input:** `arr = [5, 5, 5, 5, 5]` → **Output:** `2` (sum left = sum right = 10)

### Example 3
> -   **Input:** `arr = [1, 3, 5, 10]` → **Output:** `-1`

<details>
<summary><h2>Approach</h2></summary>


Build a `prefix_sum` array where `prefix_sum[i]` holds the sum of the first `i` elements (so `prefix_sum[0] = 0`). With it, any range sum is a constant-time subtraction. For candidate index `i − 1`, the elements strictly to the left sum to `prefix_sum[i] − arr[i - 1]`, and the elements strictly to the right sum to `prefix_sum[n] − prefix_sum[i]`. Walk the array and return the first index where those two equal. One warm-up pass to fill `prefix_sum`, one pass to scan — **O(N)**.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def first_equilibrium_point(self, arr: List[int]) -> int:

        # calculate the prefix sum of the array
        prefix_sum = [0] * (len(arr) + 1)
        for i in range(1, len(arr) + 1):
            prefix_sum[i] = prefix_sum[i - 1] + arr[i - 1]

        # check for equilibrium point
        for i in range(1, len(arr) + 1):

            # calculate sum of elements before and after the current
            # index
            left_sum = prefix_sum[i] - arr[i - 1]
            right_sum = prefix_sum[len(arr)] - prefix_sum[i]

            # if both sums are equal, return the current index as
            # equilibrium point
            if left_sum == right_sum:
                return i - 1

        # no equilibrium point found
        return -1


# Examples from the problem statement
print(Solution().first_equilibrium_point([1, 3, 5, 2, 2]))  # 2
print(Solution().first_equilibrium_point([5, 5, 5, 5, 5]))  # 2
print(Solution().first_equilibrium_point([1, 3, 5, 10]))    # -1

# Edge cases
print(Solution().first_equilibrium_point([1]))               # 0
print(Solution().first_equilibrium_point([0, 0, 0]))         # 0
print(Solution().first_equilibrium_point([1, 2, 3]))         # -1
print(Solution().first_equilibrium_point([2, 1, 2]))         # 1
print(Solution().first_equilibrium_point([0]))               # 0
```

```java run
public class Main {
    static class Solution {
        public int firstEquilibriumPoint(int[] arr) {

            // calculate the prefix sum of the array
            int[] prefixSum = new int[arr.length + 1];
            prefixSum[0] = 0;
            for (int i = 1; i <= arr.length; i++) {
                prefixSum[i] = prefixSum[i - 1] + arr[i - 1];
            }

            // check for equilibrium point
            for (int i = 1; i <= arr.length; i++) {

                // calculate sum of elements before and after the current
                // index
                int leftSum = prefixSum[i] - arr[i - 1];
                int rightSum = prefixSum[arr.length] - prefixSum[i];

                // if both sums are equal, return the current index as
                // equilibrium point
                if (leftSum == rightSum) {
                    return i - 1;
                }
            }

            // no equilibrium point found
            return -1;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 3, 5, 2, 2}));  // 2
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{5, 5, 5, 5, 5}));  // 2
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 3, 5, 10}));    // -1

        // Edge cases
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1}));               // 0
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{0, 0, 0}));         // 0
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{1, 2, 3}));         // -1
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{2, 1, 2}));         // 1
        System.out.println(new Solution().firstEquilibriumPoint(new int[]{0}));               // 0
    }
}
```

</details>


***

# Self excluded array product

## Problem Statement

Given `arr`, return an array `product` where `product[i]` equals the product of all elements **except** `arr[i]`. Solve in `O(n)` and **without division**.

### Example 1
> -   **Input:** `[1, 2, 3, 4]` → **Output:** `[24, 12, 8, 6]`

### Example 2
> -   **Input:** `[2, 3, 0]` → **Output:** `[0, 0, 6]`

### Example 3
> -   **Input:** `[3, 4]` → **Output:** `[4, 3]`

<details>
<summary><h2>Approach</h2></summary>


This is the prefix-sum trick generalised to **prefix products**. Build two arrays:

- `prefix[i] = arr[0] * arr[1] * ... * arr[i-1]` (product of everything strictly before `i`).
- `suffix[i] = arr[i+1] * arr[i+2] * ... * arr[n-1]` (product of everything strictly after `i`).

Then `product[i] = prefix[i] * suffix[i]`. Two passes (one left-to-right, one right-to-left), no division, O(N) time and O(N) space (which can be optimised to O(1) extra by computing one direction in-place).

```d2
direction: right

arr: "arr" {
  grid-columns: 4
  grid-gap: 0
  a0: "1"
  a1: "2"
  a2: "3"
  a3: "4"
}

prefix: "prefix (product of arr[0..i-1])" {
  grid-columns: 4
  grid-gap: 0
  p0: "1"
  p1: "1"
  p2: "2"
  p3: "6"
}

suffix: "suffix (product of arr[i+1..n-1])" {
  grid-columns: 4
  grid-gap: 0
  s0: "24"
  s1: "12"
  s2: "4"
  s3: "1"
}

product: "product = prefix * suffix" {
  grid-columns: 4
  grid-gap: 0
  r0: "24" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r1: "12" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r2: "8" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  r3: "6" {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
}
```

<p align="center"><strong>Self-excluded product — prefix product holds "everything before me", suffix product holds "everything after me", and their pointwise product is the answer. The technique is prefix-sum's multiplicative cousin.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def self_excluded_array_product(self, arr: List[int]) -> List[int]:
        n: int = len(arr)
        prefix_product: List[int] = [0] * n
        suffix_product: List[int] = [0] * n
        result: List[int] = [0] * n

        # Prefix product
        prefix_product[0] = arr[0]
        for i in range(1, n):
            prefix_product[i] = prefix_product[i - 1] * arr[i]

        # Suffix product
        suffix_product[n - 1] = arr[n - 1]
        for i in range(n - 2, -1, -1):
            suffix_product[i] = suffix_product[i + 1] * arr[i]

        # Result calculation
        result[0] = suffix_product[1]
        result[n - 1] = prefix_product[n - 2]
        for i in range(1, n - 1):
            result[i] = prefix_product[i - 1] * suffix_product[i + 1]

        return result


# Examples from the problem statement
print(Solution().self_excluded_array_product([1, 2, 3, 4]))  # [24, 12, 8, 6]
print(Solution().self_excluded_array_product([2, 3, 0]))     # [0, 0, 6]
print(Solution().self_excluded_array_product([3, 4]))        # [4, 3]

# Edge cases
print(Solution().self_excluded_array_product([1, 1]))        # [1, 1]
print(Solution().self_excluded_array_product([2, 2]))        # [2, 2]
print(Solution().self_excluded_array_product([1, 2, 3]))     # [6, 3, 2]
print(Solution().self_excluded_array_product([0, 0]))        # [0, 0]
print(Solution().self_excluded_array_product([5, 1, 1, 1]))  # [1, 5, 5, 5]
```

```java run
import java.util.Arrays;

public class Main {
    static class Solution {
        public int[] selfExcludedArrayProduct(int[] arr) {
            int n = arr.length;
            int[] prefixProduct = new int[n];
            int[] suffixProduct = new int[n];
            int[] result = new int[n];

            // Prefix product
            prefixProduct[0] = arr[0];
            for (int i = 1; i < n; i++) {
                prefixProduct[i] = prefixProduct[i - 1] * arr[i];
            }

            // Suffix product
            suffixProduct[n - 1] = arr[n - 1];
            for (int i = n - 2; i >= 0; i--) {
                suffixProduct[i] = suffixProduct[i + 1] * arr[i];
            }

            // Result calculation
            result[0] = suffixProduct[1];
            result[n - 1] = prefixProduct[n - 2];
            for (int i = 1; i < n - 1; i++) {
                result[i] = prefixProduct[i - 1] * suffixProduct[i + 1];
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 2, 3, 4})));  // [24, 12, 8, 6]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{2, 3, 0})));     // [0, 0, 6]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{3, 4})));        // [4, 3]

        // Edge cases
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 1})));        // [1, 1]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{2, 2})));        // [2, 2]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{1, 2, 3})));     // [6, 3, 2]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{0, 0})));        // [0, 0]
        System.out.println(Arrays.toString(new Solution().selfExcludedArrayProduct(new int[]{5, 1, 1, 1}))); // [1, 5, 5, 5]
    }
}
```

</details>


***

# Balanced binary subarray

## Problem Statement

Given a binary array `arr` (only 0s and 1s), return the length of the longest subarray with an **equal** number of 0s and 1s.

### Example 1
> -   **Input:** `[1, 0, 1, 1, 1, 0, 0]` → **Output:** `6` (indices 1..6)

### Example 2
> -   **Input:** `[0, 0, 1, 1, 0]` → **Output:** `4`

### Example 3
> -   **Input:** `[1, 1, 1, 1]` → **Output:** `0`

<details>
<summary><h2>Approach</h2></summary>


The encoding trick: **0 → −1, 1 → +1**. Now "equal 0s and 1s" becomes "subarray sums to 0", which becomes "two prefix sums are equal". Walk the array maintaining `prefixSum`; for each new value, check if it has been seen before. If yes, the subarray between the first occurrence and now has sum 0 → equal counts. Track the max length.

The `prefixSumIndex[0] = -1` base case handles subarrays starting at index 0.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def balanced_binary_subarray(self, arr: List[int]) -> int:

        # Dictionary to store the first occurrence of each prefix sum
        prefix_sum_index = {}

        # To store the maximum length of the subarray
        max_length = 0

        # Initialize the prefix sum
        prefix_sum = 0

        # Add a base case for prefix_sum = 0
        prefix_sum_index[0] = -1

        for i in range(len(arr)):

            # Treat 0 as -1 for the prefix sum calculation
            prefix_sum += -1 if arr[i] == 0 else 1

            # If the prefix sum has been seen before
            if prefix_sum in prefix_sum_index:

                # Calculate the length of the subarray
                length = i - prefix_sum_index[prefix_sum]
                max_length = max(max_length, length)

            # Otherwise, store the first occurrence of this prefix
            # sum
            else:
                prefix_sum_index[prefix_sum] = i

        return max_length


# Examples from the problem statement
print(Solution().balanced_binary_subarray([1, 0, 1, 1, 1, 0, 0]))  # 6
print(Solution().balanced_binary_subarray([0, 0, 1, 1, 0]))        # 4
print(Solution().balanced_binary_subarray([1, 1, 1, 1]))            # 0

# Edge cases
print(Solution().balanced_binary_subarray([]))                       # 0
print(Solution().balanced_binary_subarray([0]))                      # 0
print(Solution().balanced_binary_subarray([0, 1]))                   # 2
print(Solution().balanced_binary_subarray([0, 0, 1, 1]))             # 4
print(Solution().balanced_binary_subarray([1, 0]))                   # 2
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int balancedBinarySubarray(int[] arr) {

            // Map to store the first occurrence of each prefix sum
            Map<Integer, Integer> prefixSumIndex = new HashMap<>();

            // To store the maximum length of the subarray
            int maxLength = 0;

            // Initialize the prefix sum
            int prefixSum = 0;

            // Add a base case for prefixSum = 0
            prefixSumIndex.put(0, -1);

            for (int i = 0; i < arr.length; i++) {

                // Treat 0 as -1 for the prefix sum calculation
                prefixSum += (arr[i] == 0 ? -1 : 1);

                // If the prefix sum has been seen before
                if (prefixSumIndex.containsKey(prefixSum)) {

                    // Calculate the length of the subarray
                    int length = i - prefixSumIndex.get(prefixSum);
                    maxLength = Math.max(maxLength, length);
                }

                // Otherwise, store the first occurrence of this prefix
                // sum
                else {
                    prefixSumIndex.put(prefixSum, i);
                }
            }

            return maxLength;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 0, 1, 1, 1, 0, 0})); // 6
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 0, 1, 1, 0}));       // 4
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 1, 1, 1}));           // 0

        // Edge cases
        System.out.println(new Solution().balancedBinarySubarray(new int[]{}));                     // 0
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0}));                    // 0
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 1}));                 // 2
        System.out.println(new Solution().balancedBinarySubarray(new int[]{0, 0, 1, 1}));           // 4
        System.out.println(new Solution().balancedBinarySubarray(new int[]{1, 0}));                 // 2
    }
}
```

</details>


***

# Zero sum subarrays

## Problem Statement

Given an array `arr`, return the start/end indices of **every** subarray that sums to `0`.

### Example 1
> -   **Input:** `[6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7]`
> -   **Output:** `[[2, 4], [2, 6], [5, 6], [6, 9], [0, 10]]`

### Example 2
> -   **Input:** `[1, 2, 3, 4, 0]` → **Output:** `[[4, 4]]`

### Example 3
> -   **Input:** `[1, 2, 3]` → **Output:** `[]`

<details>
<summary><h2>Approach</h2></summary>


Same prefix-sum trick. Two indices `i < j` with `P[i] == P[j+1]` means `arr[i..j]` sums to zero. So maintain a hash map `{prefixSum → list of indices where it appeared}`; whenever we see a prefix sum that has appeared before, every previous occurrence is the *start − 1* of a zero-sum subarray ending at the current index.

The base case `prefixSumIndices[0] = [-1]` lets us catch zero-sum subarrays that start at index 0.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:
    def zero_sum_subarrays(self, arr: List[int]) -> List[List[int]]:

        # Dictionary to store prefix sums and their indices
        prefix_sum_indices: dict[int, List[int]] = {}

        # To store the actual start and end indices of all subarrays
        result: List[List[int]] = []
        prefix_sum = 0

        # Add a base case for prefix_sum = 0
        prefix_sum_indices[0] = [-1]

        for i in range(len(arr)):
            prefix_sum += arr[i]

            # If the prefix_sum exists in the map, it means we found
            # subarrays summing to 0
            if prefix_sum in prefix_sum_indices:
                for prev_index in prefix_sum_indices[prefix_sum]:

                    # Add (prev_index + 1) as the correct start index
                    result.append([prev_index + 1, i])

            # Add the current index to the list of indices for this
            # prefix_sum
            if prefix_sum not in prefix_sum_indices:
                prefix_sum_indices[prefix_sum] = []
            prefix_sum_indices[prefix_sum].append(i)

        return result


# Examples from the problem statement
r1 = Solution().zero_sum_subarrays([6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7])
print(sorted([sorted(p) for p in r1]))   # [[0, 10], [2, 4], [2, 6], [5, 6], [6, 9]]

print(Solution().zero_sum_subarrays([1, 2, 3, 4, 0]))  # [[4, 4]]
print(Solution().zero_sum_subarrays([1, 2, 3]))         # []

# Edge cases
print(Solution().zero_sum_subarrays([]))                # []
print(Solution().zero_sum_subarrays([0]))               # [[0, 0]]
print(Solution().zero_sum_subarrays([-1, 1]))           # [[0, 1]]
print(Solution().zero_sum_subarrays([1, -1, 1, -1]))    # [[0, 1], [0, 3], [2, 3]]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public List<List<Integer>> zeroSumSubarrays(int[] arr) {

            // Map to store prefix sums and their indices
            Map<Integer, List<Integer>> prefixSumIndices = new HashMap<>();

            // To store the actual start and end indices of all subarrays
            List<List<Integer>> result = new ArrayList<>();
            int prefixSum = 0;

            // Add a base case for prefixSum = 0
            prefixSumIndices.put(0, new ArrayList<>());
            prefixSumIndices.get(0).add(-1);

            for (int i = 0; i < arr.length; i++) {
                prefixSum += arr[i];

                // If the prefixSum exists in the map, it means we found
                // subarrays summing to 0
                if (prefixSumIndices.containsKey(prefixSum)) {
                    for (int prevIndex : prefixSumIndices.get(prefixSum)) {

                        // Add (prevIndex + 1) as the correct start index
                        List<Integer> subarray = new ArrayList<>();
                        subarray.add(prevIndex + 1);
                        subarray.add(i);
                        result.add(subarray);
                    }
                }

                // Add the current index to the list of indices for this
                // prefixSum
                prefixSumIndices
                    .computeIfAbsent(prefixSum, k -> new ArrayList<>())
                    .add(i);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        var r1 = new Solution().zeroSumSubarrays(new int[]{6, 3, -1, -3, 4, -2, 2, 4, 6, -12, -7});
        r1.forEach(p -> { Collections.sort(p); System.out.print(p + " "); }); System.out.println();
        // [0, 10] [2, 4] [2, 6] [5, 6] [6, 9] (order may vary)

        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, 2, 3, 4, 0}));  // [[4, 4]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, 2, 3}));         // []

        // Edge cases
        System.out.println(new Solution().zeroSumSubarrays(new int[]{}));                // []
        System.out.println(new Solution().zeroSumSubarrays(new int[]{0}));               // [[0, 0]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{-1, 1}));           // [[0, 1]]
        System.out.println(new Solution().zeroSumSubarrays(new int[]{1, -1, 1, -1}));   // [[0, 1], [0, 3], [2, 3]]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Prefix sum is the bridge that turns *quadratic* subarray problems into *linear* ones. The pattern is so flexible it shows up in problems that don't even mention sums — anywhere "balanced" or "equal" or "net zero" can be encoded as a sum, the same machinery applies.

The four moves:

1. **Define the prefix.** Sum, signed-count, balance, parity — pick whatever encodes "the question".
2. **Re-encode if needed.** "Equal 0s and 1s" → 0 ↦ −1, 1 ↦ +1. "Equal As and non-As" → As ↦ +1, others ↦ −1. "Subarray sum = K" works directly.
3. **Hash map of prefix values.** What you store depends on the answer shape: first-index for *longest*, list of indices for *all*, count for *how many*.
4. **The `0 → −1` (or `0 → −1` index) base case.** Forgetting it is the canonical bug. Always start with `map[0] = -1` (for first-index) or `map[0] = 1` (for count).

> **A panoramic view of the five hash-table patterns:**
>
> | Pattern | Question shape | Best hash-map use |
> |---|---|---|
> | Counting | "how often / how many of X?" | freq map of items |
> | Key generation | "which inputs are equivalent?" | key → group |
> | Fixed sliding window | "for each window of size K, …?" | freq map of window |
> | Variable sliding window | "longest/shortest with property P?" | freq map + grow/shrink |
> | Prefix sum + hash | "subarray sum = X?" / "balanced subarray?" | prefix-value → index/count |

> *Coming up — a different kind of lesson. The next file is the **design** lesson: two classic interview problems (LRU cache, RandomisedSet) where you build whole composite data structures from a hash table plus one other piece (a doubly-linked list, a dynamic array). The patterns we just covered prepared us; design problems force us to combine them into a single coherent structure.*

</details>