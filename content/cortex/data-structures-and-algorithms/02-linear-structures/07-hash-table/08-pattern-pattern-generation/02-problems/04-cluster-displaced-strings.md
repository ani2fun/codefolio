---
title: "Cluster Displaced Strings"
summary: "Given an array strs of lowercase strings, group strings that belong to the same displacing sequence. A displacing sequence shifts every character by the same amount, with wrap-around (e.g. abc → bcd →"
prereqs:
  - 08-pattern-pattern-generation/01-pattern
difficulty: medium
---

# Cluster displaced strings

## Problem Statement

Given an array `strs` of lowercase strings, group strings that belong to the same **displacing sequence**. A displacing sequence shifts every character by the same amount, with wrap-around (e.g. `abc → bcd → ... → xyz → yza`).

### Example 1
> -   **Input:** `["abc","ghi","xyz","b","c","ab","cd"]`
> -   **Output:** `[["abc","ghi","xyz"], ["b","c"], ["ab","cd"]]`

### Example 2
> -   **Input:** `["ad","k","cf"]`
> -   **Output:** `[["ad","cf"], ["k"]]`

### Example 3
> -   **Input:** `["abcd","efg","hi","j"]`
> -   **Output:** `[["abcd"],["efg"],["hi"],["j"]]`

<details>
<summary><h2>Approach</h2></summary>


Two strings are in the same displacing class iff the **gaps between consecutive characters** are identical (modulo 26). `abc` has gaps `(b−a, c−b) = (1, 1)`. `ghi` has gaps `(h−g, i−h) = (1, 1)`. `xyz` has gaps `(y−x, z−y) = (1, 1)`. All three: `(1, 1)`. They cluster.

The key per string is the **gap-sequence**, with negative gaps wrapped to `+ 26` so `xyz → yza` doesn't break the encoding (`a − z = -25` becomes `+1` after wrap).

Single-character strings have an empty gap sequence and all cluster together — but the example shows them split. The catch: a single-character string has gap-sequence `""`, which is the same key for every single-character string. The example actually splits them; this happens because the example uses a different rule (each of `b`, `c` alone is its own class? — re-checking: example 1 puts `b` and `c` together as `[b, c]`, which is the empty-sequence cluster). So our keying works — single-char strings form one cluster, two-char strings clustered by their single gap, etc.

> 🖼 Diagram — Cluster displaced strings — the key is the sequence of consecutive-character gaps (modulo 26 to handle wrap). Strings with identical gap sequences belong to the same displacing class and collide into the same hash-map bucket.
```d2
direction: right

inputs: input strings {
  grid-rows: 2
  grid-gap: 8
  s1: abc
  s2: ghi
  s3: xyz
  s4: ab
  s5: cd
  s6: b
  s7: c
}

keys: gap-sequence keys {
  k1: "1,1,"
  k2: "1,"
  k3: "(empty)"
}

inputs.s1 -> keys.k1: "gaps (1,1)"
inputs.s2 -> keys.k1: "gaps (1,1)"
inputs.s3 -> keys.k1: "gaps (1,1)"
inputs.s4 -> keys.k2: "gap (1,)"
inputs.s5 -> keys.k2: "gap (1,)"
inputs.s6 -> keys.k3: "no gaps"
inputs.s7 -> keys.k3: "no gaps"
```

<p align="center"><strong>Cluster displaced strings — the key is the sequence of consecutive-character gaps (modulo 26 to handle wrap). Strings with identical gap sequences belong to the same displacing class and collide into the same hash-map bucket.</strong></p>

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run viz=graph viz-root=strs
from typing import List

class Solution:
    def generate_pattern(self, s: str) -> str:
        pattern = ""

        for i in range(1, len(s)):

            # Find the difference between consecutive characters
            difference = ord(s[i]) - ord(s[i - 1])
            if difference < 0:

                # Handle wrap-around case (e.g., from 'z' to 'a')
                difference += 26

            # Add the displacement to the pattern
            pattern += str(difference) + ","

        return pattern

    def cluster_displaced_strings(
        self, strs: List[str]
    ) -> List[List[str]]:
        clusters = {}

        # Process each string and group them by their displacement
        # pattern
        for str in strs:

            # Generate the pattern for each string
            pattern = self.generate_pattern(str)

            # Group the strings with the same pattern
            if pattern not in clusters:
                clusters[pattern] = []
            clusters[pattern].append(str)

        # Prepare the result with all grouped strings
        result = []
        for group in clusters.values():

            # Add each group to the result
            result.append(group)

        return result


# Examples from the problem statement
r1 = Solution().cluster_displaced_strings(["abc", "ghi", "xyz", "b", "c", "ab", "cd"])
print(sorted([sorted(g) for g in r1]))  # [['abc', 'ghi', 'xyz'], ['ab', 'cd'], ['b', 'c']]

r2 = Solution().cluster_displaced_strings(["ad", "k", "cf"])
print(sorted([sorted(g) for g in r2]))  # [['ad', 'cf'], ['k']]

r3 = Solution().cluster_displaced_strings(["abcd", "efg", "hi", "j"])
print(sorted([sorted(g) for g in r3]))  # [['abcd'], ['efg'], ['hi'], ['j']]

# Edge cases
print(Solution().cluster_displaced_strings([]))              # []
print(Solution().cluster_displaced_strings(["a"]))           # [['a']]
r6 = Solution().cluster_displaced_strings(["az", "ba"])
print(sorted([sorted(g) for g in r6]))  # [['az', 'ba']]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private String generatePattern(String s) {
            StringBuilder pattern = new StringBuilder();

            for (int i = 1; i < s.length(); i++) {

                // Find the difference between consecutive characters
                int difference = s.charAt(i) - s.charAt(i - 1);
                if (difference < 0) {

                    // Handle wrap-around case (e.g., from 'z' to 'a')
                    difference += 26;
                }

                // Add the displacement to the pattern
                pattern.append(difference).append(",");
            }

            return pattern.toString();
        }

        public List<List<String>> clusterDisplacedStrings(String[] strs) {
            Map<String, List<String>> clusters = new HashMap<>();

            // Process each string and group them by their displacement
            // pattern
            for (String str : strs) {

                // Generate the pattern for each string
                String pattern = generatePattern(str);

                // Group the strings with the same pattern
                clusters.putIfAbsent(pattern, new ArrayList<>());
                clusters.get(pattern).add(str);
            }

            // Prepare the result with all grouped strings
            List<List<String>> result = new ArrayList<>();
            for (List<String> group : clusters.values()) {

                // Add each group to the result
                result.add(group);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        var r1 = new Solution().clusterDisplacedStrings(new String[]{"abc", "ghi", "xyz", "b", "c", "ab", "cd"});
        r1.forEach(g -> { Collections.sort(g); System.out.print(g + " "); }); System.out.println();
        // [abc, ghi, xyz] [b, c] [ab, cd] (group order may vary)

        var r2 = new Solution().clusterDisplacedStrings(new String[]{"ad", "k", "cf"});
        r2.forEach(g -> { Collections.sort(g); System.out.print(g + " "); }); System.out.println();
        // [ad, cf] [k] (group order may vary)

        var r3 = new Solution().clusterDisplacedStrings(new String[]{"abcd", "efg", "hi", "j"});
        r3.forEach(g -> System.out.print(g + " ")); System.out.println();
        // [abcd] [efg] [hi] [j]

        // Edge cases
        System.out.println(new Solution().clusterDisplacedStrings(new String[]{}));     // []
        System.out.println(new Solution().clusterDisplacedStrings(new String[]{"a"}));  // [[a]]
        var r6 = new Solution().clusterDisplacedStrings(new String[]{"az", "ba"});
        r6.forEach(g -> { Collections.sort(g); System.out.print(g + " "); }); System.out.println();
        // [az, ba]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


The key-generation pattern is the rosetta stone of hash-table problem solving. *Anywhere you can define what makes two inputs "the same"*, you can encode that sameness as a key, throw the keys at a hash map, and let the structure do the grouping for you.

The skill is **inventing the key**. A few common shapes:

- **Sorted form** — for anagrams (`"cab" → "abc"`).
- **First-occurrence index** — for shape/homomorphism (`"add" → "0,1,1"`).
- **Gap sequence (mod cyclic group)** — for displaced/shifted strings.
- **Frequency tuple** — for multiset equality.
- **Categorical id** — for keyboard rows, parity classes, modular buckets.

Once the key is right, the rest is one line:

```python
groups[key(x)].append(x)
```

> *Coming up — the **fixed-size sliding window** pattern. So far we've used hash maps to summarise *static* sequences. Next we'll learn to slide a fixed-size window across a long sequence while the hash map tracks the multiset *inside* the window in O(1) per shift. Fixed-window anagrams, frequency-bounded substrings, repeating-character runs — the same hash map you've been building for two lessons becomes the engine of a moving picture.*

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Examples — missing, needs to be written -->
<!--       Guidance: min 3 examples: basic / variant / edge -->

<!-- TODO: Intuition — missing, needs to be written -->
<!--       Guidance: 3 paragraphs: brute force / observation / pattern fit -->

<!-- TODO: Applying the Diagnostic Questions — missing, needs to be written -->
<!--       Guidance: REQUIRED, never optional -->
<!--       Guidance: 4-row table. Columns: 'Check' | 'Answer for [Problem Name]' -->
<!--       Guidance: Rows: two positions simultaneously / one near start one near end / both move inward / simple O(1) work at each step -->

<!-- TODO: Approach — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Solution — missing, needs to be written -->
<!--       Guidance: Python block then Java block -->

<!-- TODO: Dry Run — missing, needs to be written -->
<!--       Guidance: walk through a small example step by step -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table: time / space / why -->

<!-- TODO: Edge Cases — missing, needs to be written -->
<!--       Guidance: table, min 5 rows -->

<!-- TODO: Key Takeaway — missing, needs to be written -->
<!--       Guidance: 1–2 sentences -->
