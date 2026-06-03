---
title: "Balanced Binary Subarray"
summary: "Given a binary array arr (only 0s and 1s), return the length of the longest subarray with an equal number of 0s and 1s."
prereqs:
  - 11-pattern-prefix-sum/01-pattern
difficulty: medium
---

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
<summary><strong>Examples</strong></summary>

**Example 1**
```
Input:  [1, 0, 1, 1, 1, 0, 0]
Output: 6
Explanation: the slice from index 1 to 6 holds three 0s and three 1s → length 6.
No longer balanced slice exists.
```

**Example 2**
```
Input:  [0, 0, 1, 1, 0]
Output: 4
Explanation: the slice from index 0 to 3 holds two 0s and two 1s → length 4. The
slice 1..4 is also length 4; either is a valid longest.
```

**Example 3**
```
Input:  [1, 1, 1, 1]
Output: 0
Explanation: all 1s — no slice can balance 0s against 1s, so the answer is 0.
```

**Example 4**
```
Input:  [0, 1]
Output: 2
Explanation: one 0 and one 1 — the whole array is balanced → length 2.
```

</details>
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

## Intuition

A balanced subarray holds an equal number of `0`s and `1`s. The brute-force read counts both within every candidate slice: fix a start, extend an end, tally the two counts, record the length whenever they match. Each of the `O(N)` starts runs an `O(N)` extension, so the work is `O(N²)` time — and the inner tally re-counts elements the previous slice already saw.

The re-encoding trick turns counting into summing: **treat `0` as `−1` and `1` as `+1`**. Now a slice has equal counts exactly when its encoded elements sum to `0`, which happens exactly when two prefix sums are equal. Maintain a running `prefix_sum` over the `±1` values and a hash map from each prefix value to the *first* index where it appeared. When the current `prefix_sum` matches an earlier index `j`, the slice `arr[j+1..i]` nets to zero, so its length is `i − j`.

This is the same-value-search flavour of prefix sums, and the hash map is what makes it `O(1)` per index. What breaks without the `prefix_sum_index[0] = -1` base case is any balanced slice anchored at index `0` — its prefix value is `0`, and without the seeded `−1` index there is nothing earlier to match against. The diagnostic signal is "longest slice with a net-zero property", which the same-value search answers in one pass.

## Applying the Diagnostic Questions

| Check | Answer for Balanced Binary Subarray |
|---|---|
| **Q1.** Does the answer reduce to a subarray sum? | **Yes** — re-encoding `0 → −1` turns "equal counts" into "slice sums to `0`". |
| **Q2.** Is the input a linear sequence walked once? | **Yes** — a binary array, swept index by index in a single pass. |
| **Q3.** Is the matching slice found by a hash-map lookup? | **Yes** — a repeated prefix value names both ends of a zero-sum slice in `O(1)`. |
| **Q4.** Does the rule survive negatives and zeros? | **Yes** — the `±1` encoding deliberately introduces negatives, exactly where a sliding window would fail. |

## Approach

1. Initialise `prefix_sum = 0`, `max_length = 0`, and a map `prefix_sum_index` seeded with `{0: -1}` so a balanced slice starting at index `0` is caught.
2. Sweep `i` across the array. Add `+1` to `prefix_sum` for a `1` and `−1` for a `0`.
3. If `prefix_sum` has appeared before at index `j`, the slice `arr[j+1..i]` nets to zero — compute its length `i − j` and update `max_length` if it is larger.
4. Otherwise, record `prefix_sum → i` as its *first* occurrence; storing only the earliest index keeps every future matched slice as long as possible.
5. After the loop, return `max_length`.

## Dry Run

Walk Example 1: `arr = [1, 0, 1, 1, 1, 0, 0]`, encoded as `+1, -1, +1, +1, +1, -1, -1`, expected output `6`. The map stores each prefix value's first index; a repeat marks a balanced slice:

```
prefix_sum=0, map={0:-1}, max=0

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

## Complexity Analysis

| | Cost | Why |
|---|---|---|
| **Time** | **O(N)** | One pass over the array; each step does a constant number of `O(1)` hash-map reads and at most one write. |
| **Space** | **O(N)** | The map holds one entry per distinct prefix value — up to `N + 1` entries when every prefix sum is unique. |

## Edge Cases

| Input | Output | Why |
|---|---|---|
| `[]` | `0` | Empty array — the loop never runs, `max_length` stays `0`. |
| `[0]` | `0` | A single `0` can never balance against a `1`. |
| `[1, 1, 1, 1]` | `0` | All `1`s — no prefix value ever repeats except via the seeded `0`, which never recurs. |
| `[0, 1]` | `2` | One `0`, one `1` — the whole array nets to zero → length `2`. |
| `[1, 0]` | `2` | Order does not matter; the full slice is still balanced. |
| `[0, 0, 1, 1]` | `4` | Two `0`s then two `1`s — the entire array balances → length `4`. |

## Key Takeaway

Re-encoding `0 → −1` converts "equal counts" into "two prefix sums are equal", so a hash map of first occurrences finds the longest balanced subarray in one `O(N)` pass — and the `map[0] = -1` base case is what catches slices anchored at index `0`.
