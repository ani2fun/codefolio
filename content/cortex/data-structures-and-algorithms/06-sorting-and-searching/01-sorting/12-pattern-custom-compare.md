# 12. Pattern: Custom Compare

Every sorting algorithm in this section has compared elements with `<` (or `>`). For integers, that's obvious. For strings, the language gives you lexicographic comparison for free. But what about: sort users by *full name (last, first)*? Sort orders by *priority then date*? Sort numbers by *number of 1-bits in their binary representation*? Sort versions like `1.10.0` to come *after* `1.2.0`?

The sort algorithm doesn't care about *what* you're comparing — only that there's a comparison function. **Custom compare** is the pattern of providing a problem-specific comparison rule and letting any sort algorithm handle the rest. It's how `std::sort`, `Arrays.sort`, `array.sort`, and friends become universal — they don't compare elements directly; they call your comparator.

This file is the final pattern lesson in the sorting section. By the end you'll know the three styles of custom compare (operator overloading, lambda, comparator class), the diagnostic checks for spotting "I need a custom comparator," and four worked problems: bitwise sort, sort by frequency, largest number, and sort people by height.

## Table of contents

1. [Understanding custom comparators](#understanding-custom-comparators)
2. [The three styles](#the-three-styles)
3. [Identifying custom-compare problems](#identifying-custom-compare-problems)
4. [Bitwise sort](#bitwise-sort)
5. [Sort characters by frequency](#sort-characters-by-frequency)
6. [Largest number](#largest-number)
7. [Sort people by height](#sort-people-by-height)

***

# Understanding Custom Comparators

A **comparator** is a function (or callable object) that takes two elements `a` and `b` and returns:

- **Negative** if `a` should come before `b`.
- **Zero** if `a` and `b` are equivalent.
- **Positive** if `a` should come after `b`.

The exact convention varies by language — some use `(a, b) → bool` returning "is `a < b`?" — but the underlying idea is the same: the algorithm asks "which one comes first?" and the comparator answers.

```d2
direction: right

elem_a: "a (one element)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
elem_b: "b (another element)" {style.fill: "#fde68a"; style.stroke: "#d97706"}
cmp: "comparator(a, b)" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
result: "negative / zero / positive"

elem_a -> cmp
elem_b -> cmp
cmp -> result
```

<p align="center"><strong>The comparator interface. Every sort algorithm in this section can be adapted to a new ordering rule by swapping out the comparator.</strong></p>

The comparator must implement a **total order**:
- *Antisymmetric* — `cmp(a, b)` and `cmp(b, a)` should have opposite signs.
- *Transitive* — if `a < b` and `b < c`, then `a < c`.
- *Total* — every pair has a defined ordering (not always equal).

Violating any of these breaks the sort — you'll get incorrect results or, worse, infinite loops in some algorithms. The most common bug is **forgetting to handle ties**: a comparator that returns `0` for equal-key elements is correct, but one that always returns `-1` or always `1` is not.

---

## Why the Sort Algorithm Doesn't Care What You Compare

Every comparison sort in this section — bubble, selection, insertion, quicksort, merge sort, heapsort — uses comparisons like `arr[i] < arr[j]` exactly four to ten times. *Replace those comparisons with `cmp(arr[i], arr[j]) < 0`* and the algorithm sorts by the comparator's rule instead of the natural numeric order.

This is what library `sort` functions do. They have a single implementation (typically quicksort or TimSort) and accept a comparator as an argument. The user supplies the comparator; the algorithm does the rest.

---

## A Concrete Example

Sort points `[(3, 1), (1, 2), (2, 4), (1, 1)]` by x-coordinate, breaking ties by y-coordinate.

The comparator: "compare `a.x` to `b.x`; if equal, compare `a.y` to `b.y`."

```python run
points = [(3, 1), (1, 2), (2, 4), (1, 1)]
points.sort(key=lambda p: (p[0], p[1]))   # tuple comparison gives lex order
print(points)   # [(1, 1), (1, 2), (2, 4), (3, 1)]
```

The `key=` parameter transforms each element into a sort-key, and Python compares tuples lexicographically. The same effect can be achieved with `functools.cmp_to_key` and an explicit comparator function.

---

## Strengths and Limitations

| Strength | Detail |
|---|---|
| **Universal** | Works with any sorting algorithm — the algorithm just calls the comparator. |
| **Composable** | Multi-key sorts compose: `(primary_key, secondary_key, ...)`. |
| **Decouples sort from data** | The same sort works on integers, strings, structs, anything orderable. |

| Limitation | Detail |
|---|---|
| **Comparator overhead** | Function-call cost per comparison. Slower than inline `<` for primitives. |
| **Easy to bug** | Non-transitive or non-antisymmetric comparators silently produce wrong sorts. |
| **Stability depends on the algorithm** | A custom comparator preserves stability only if the underlying sort is stable. |

In practice, custom compare is used:
- Every time you `sort([{...}, {...}, ...])` with a non-trivial key.
- Database `ORDER BY` clauses (each column is a comparator stage).
- Sorting search results by relevance score, then date, then ID.
- Sorting versions, paths, custom domain objects.

---

## Key Takeaway

A comparator is a function that defines "which element comes first." Any sort algorithm can use any comparator. This decoupling is what makes sorting universal. Now we'll see the three syntactic styles for providing a comparator.

***

# The Three Styles

Most languages support three syntactic styles for custom compare. Each has its place.

---

## Style 1 — Operator Overloading

Make the *type* itself orderable by overloading `<` (or implementing the language's "comparable" interface). The sort uses the type's natural order.

**Use when:** the order is intrinsic to the type — e.g., `Money` always compares by amount, `Date` always compares chronologically.

**Languages:** C++ (`operator<`), Python (`__lt__`), Java (`Comparable<T>`).

```python run
class Entry:
    def __init__(self, x, y):
        self.x, self.y = x, y
    def __lt__(self, other):
        return (self.x, self.y) < (other.x, other.y)

arr = [Entry(2, 3), Entry(1, 4), Entry(2, 1)]
arr.sort()   # uses __lt__
```

---

## Style 2 — Lambda / Inline Function

Pass an anonymous function inline at the call site. Most flexible; doesn't require modifying the type.

**Use when:** the order is *context-specific* — different parts of your code want different orderings of the same data.

```python run
arr = [(2, 3), (1, 4), (2, 1)]
arr.sort(key=lambda t: (t[0], -t[1]))   # by x ascending, then y descending
```

---

## Style 3 — Comparator Class / Object

Define a class implementing the language's "Comparator" or "Compare" interface. Reusable; can hold state.

**Use when:** the comparator needs internal state (e.g., a frequency map, a pivot value) or is reused across many sort calls.

```python run
from functools import cmp_to_key

class FrequencyComparator:
    def __init__(self, freq_map):
        self.freq = freq_map
    def __call__(self, a, b):
        if self.freq[a] != self.freq[b]:
            return self.freq[b] - self.freq[a]   # higher frequency first
        return a - b                              # tiebreak by value

freq = {1: 3, 2: 1, 3: 2}
arr = [1, 2, 3, 1, 1, 3]
arr.sort(key=cmp_to_key(FrequencyComparator(freq)))
```

---

## When to Use Each

| Need | Style |
|---|---|
| The type has *one* obvious ordering | Style 1 (overloading) |
| You need different orders in different places | Style 2 (lambda) |
| The comparator captures runtime state (freq map, pivot) | Style 3 (comparator object) |

In practice, lambdas are by far the most common in modern code. They're concise, readable, and don't pollute the type system. Operator overloading is reserved for types where the order is *the* natural ordering. Comparator classes are for stateful comparisons.

---

## Key Takeaway

Three styles, one underlying idea: provide a function that orders pairs. Pick the style that matches your context. Now we'll learn how to spot custom-compare problems.

***

# Identifying Custom-Compare Problems

Two diagnostic questions decide whether the custom-compare pattern fits.

| # | Question | If "yes," custom compare fits because... |
|---|---|---|
| **Q1** | Does the desired order require a *transformation* of the elements (not their raw values)? | The transformation produces a sort key. |
| **Q2** | Can the transformation be expressed as a function of the element alone (or with bounded extra state)? | A comparator can call the transformation deterministically. |

---

## A Useful Mental Model — Transform Then Compare

Most custom-compare problems boil down to:
1. Define a transformation function `t(elem) → key`.
2. Sort by `key` using the natural ordering on `key`.

Examples:
- "Sort by frequency" → `t(c) = (-frequency[c], c)` (negative because we want descending frequency, then character for tiebreaks).
- "Sort by digit count" → `t(n) = (number of digits in n, n)`.
- "Sort by distance to point P" → `t(point) = distance(point, P)`.

The transform-then-compare pattern collapses every custom-compare problem into "what's the right `t`?" Once you have `t`, the comparator is `cmp(a, b) = (t(a) < t(b))`.

---

## Common Phrasings

Custom compare is usually signalled by:
- "Sort by [some derived property]."
- "Sort by [primary], breaking ties by [secondary]."
- "Sort the array such that ..."
- "Find the largest / smallest ... where the ordering is ..."

If the problem describes an ordering that isn't the values' natural order, custom compare applies.

---

## Key Takeaway

Two checks — a transformation is needed, and the transformation is a function of each element — gate every custom-compare problem. Pass them both and the recipe is "transform, then sort by the natural order on the transform." Now four worked problems.

***

# Bitwise Sort

Sort numbers by the count of `1` bits in their binary representation. Ties broken by value.

---

## The Problem

Given an integer array `arr`, sort it ascending by the number of `1`s in each element's binary representation. For elements with the same bit-count, sort by value.

```
Input:  arr = [7, 10, 12, 18, 26]
Output: [10, 12, 18, 7, 26]
Explanation:
  10 = 1010 → 2 ones
  12 = 1100 → 2 ones
  18 = 10010 → 2 ones
  7  = 111   → 3 ones
  26 = 11010 → 3 ones
  Sorted by (ones, value): (2,10), (2,12), (2,18), (3,7), (3,26)

Input:  arr = [3, 7, 10, 18, 2, 9, 15, 31]
Output: [2, 3, 9, 10, 18, 7, 15, 31]

Input:  arr = [1, 2, 4, 8, 16]
Output: [1, 2, 4, 8, 16]   (each has exactly 1 bit; sort by value)
```

---

<details>
<summary><h2>The Custom Compare</h2></summary>


Transform: `t(n) = (popcount(n), n)`. Sort by `t` ascending.

`popcount(n)` is the number of set bits — many languages have a built-in (`__builtin_popcount` in C/C++, `Integer.bitCount` in Java, `bin(n).count("1")` in Python).

</details>
<details>
<summary><h2>The Solution</h2></summary>



```python run
from typing import List

class Solution:
    def bitwise_sort(self, arr: List[int]) -> None:

        # Sort the array using custom key
        arr.sort(
            key=lambda num: (

                # Count set bits in 'num' using built-in function
                bin(num).count("1"),

                # If set bits are equal, sort by the actual number
                num,
            )
        )


# Examples from the problem statement
a1 = [7, 10, 12, 18, 26]
Solution().bitwise_sort(a1); print(a1)            # [10, 12, 18, 7, 26]

a2 = [3, 7, 10, 18, 2, 9, 15, 31]
Solution().bitwise_sort(a2); print(a2)            # [2, 3, 9, 10, 18, 7, 15, 31]

a3 = [1, 2, 4, 8, 16]
Solution().bitwise_sort(a3); print(a3)            # [1, 2, 4, 8, 16]

# Edge cases
a4: List[int] = []                                # empty array
Solution().bitwise_sort(a4); print(a4)            # []

a5 = [5]                                          # single element
Solution().bitwise_sort(a5); print(a5)            # [5]

a6 = [3, 1]                                       # two elements
Solution().bitwise_sort(a6); print(a6)            # [1, 3]

a7 = [7, 7, 7]                                    # all duplicates
Solution().bitwise_sort(a7); print(a7)            # [7, 7, 7]

a8 = [0, 1, 2, 3]                                 # includes zero
Solution().bitwise_sort(a8); print(a8)            # [0, 1, 2, 3]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public void bitwiseSort(int[] arr) {

            // Convert array to a list for sorting
            List<Integer> list = new ArrayList<>();
            for (int num : arr) {
                list.add(num);
            }

            // Sort using a lambda comparator
            list.sort((num1, num2) -> {

                // Count set bits in 'a' using Integer.bitCount()
                int setBitCountNum1 = Integer.bitCount(num1);

                // Count set bits in 'b' using Integer.bitCount()
                int setBitCountNum2 = Integer.bitCount(num2);

                // Sort based on set bit count, then numerically if equal
                if (setBitCountNum1 == setBitCountNum2) {
                    return num1 - num2;
                }

                return setBitCountNum1 - setBitCountNum2;
            });

            // Copy sorted values back into the array
            for (int i = 0; i < arr.length; i++) {
                arr[i] = list.get(i);
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        int[] a1 = {7, 10, 12, 18, 26};
        new Solution().bitwiseSort(a1);
        System.out.println(Arrays.toString(a1));  // [10, 12, 18, 7, 26]

        int[] a2 = {3, 7, 10, 18, 2, 9, 15, 31};
        new Solution().bitwiseSort(a2);
        System.out.println(Arrays.toString(a2));  // [2, 3, 9, 10, 18, 7, 15, 31]

        int[] a3 = {1, 2, 4, 8, 16};
        new Solution().bitwiseSort(a3);
        System.out.println(Arrays.toString(a3));  // [1, 2, 4, 8, 16]

        // Edge cases
        int[] a4 = {};                            // empty array
        new Solution().bitwiseSort(a4);
        System.out.println(Arrays.toString(a4));  // []

        int[] a5 = {5};                           // single element
        new Solution().bitwiseSort(a5);
        System.out.println(Arrays.toString(a5));  // [5]

        int[] a6 = {3, 1};                        // two elements
        new Solution().bitwiseSort(a6);
        System.out.println(Arrays.toString(a6));  // [1, 3]

        int[] a7 = {7, 7, 7};                     // all duplicates
        new Solution().bitwiseSort(a7);
        System.out.println(Arrays.toString(a7));  // [7, 7, 7]

        int[] a8 = {0, 1, 2, 3};                  // includes zero
        new Solution().bitwiseSort(a8);
        System.out.println(Arrays.toString(a8));  // [0, 1, 2, 3]
    }
}
```

</details>


***

# Sort Characters by Frequency

Sort a string's characters by frequency (descending), with lexicographic tiebreaks.

---

## The Problem

Given a string `s`, return it with characters reordered: highest-frequency first, then lexicographic order on ties.

```
Input:  s = "eeeaaabc"
Output: "aaaeeebc"

Input:  s = "zzzxxyyyb"
Output: "yyyzzzxxb"

Input:  s = "zzzxxyyb"
Output: "zzzxxyyb"
```

---

<details>
<summary><h2>The Custom Compare</h2></summary>


Transform: `t(c) = (-frequency[c], c)`. The negative sign reverses the order on frequency (we want descending). The tuple's second element gives lex tiebreaks ascending.

</details>
<details>
<summary><h2>The Solution</h2></summary>


```python run
from collections import Counter

class Solution:
    def sort_characters_by_frequency(self, s: str) -> str:

        # Create a Counter to store the frequency of characters
        frequency = Counter(s)

        # Convert frequency items into a list of tuples (character,
        # frequency)
        char_freq = list(frequency.items())

        # Sort the list using a lambda function
        char_freq.sort(key=lambda x: (-x[1], x[0]))

        # Explanation:
        # -x[1] => sort by frequency descending
        # x[0]  => sort by character ascending for ties

        # Build the final string by repeating each character by its frequency
        # and joining them all together
        return "".join([ch * freq for ch, freq in char_freq])


# Examples from the problem statement
print(Solution().sort_characters_by_frequency("eeeaaabc"))    # aaaeeebc
print(Solution().sort_characters_by_frequency("zzzxxyyyb"))   # yyyzzzxxb
print(Solution().sort_characters_by_frequency("zzzxxyyb"))    # zzzxxyyb

# Edge cases
print(Solution().sort_characters_by_frequency("a"))           # a
print(Solution().sort_characters_by_frequency("aa"))          # aa
print(Solution().sort_characters_by_frequency("ab"))          # ab
print(Solution().sort_characters_by_frequency("aabb"))        # aabb
print(Solution().sort_characters_by_frequency("ccbbaa"))      # aabbcc
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public String sortCharactersByFrequency(String s) {

            // Create a map to store the frequency of characters
            Map<Character, Integer> frequency = new HashMap<>();

            // Count the frequencies of each character in the input string
            for (char ch : s.toCharArray()) {
                frequency.put(ch, frequency.getOrDefault(ch, 0) + 1);
            }

            // Convert the frequency map into a list of Map.Entry<Character,
            // Integer>
            List<Map.Entry<Character, Integer>> charFreq = new ArrayList<>(
                frequency.entrySet()
            );

            // Sort the list using a lambda comparator
            charFreq.sort((a, b) -> {

                // Compare the frequencies of characters 'a' and 'b'
                // If the frequency of 'a' is greater than the frequency of
                // 'b', or if the frequencies are equal but 'a' comes before
                // 'b' in lexicographical order, then 'a' should come before
                // 'b' in the sorted string.
                if (a.getValue().equals(b.getValue())) {

                    // lexicographical order
                    return a.getKey() - b.getKey();
                }

                // Otherwise, sort by frequency in descending order
                return b.getValue() - a.getValue();
            });

            // Build the sorted string by repeating each character by its
            // frequency and appending them all together
            StringBuilder result = new StringBuilder();
            for (Map.Entry<Character, Integer> entry : charFreq) {
                for (int i = 0; i < entry.getValue(); i++) {
                    result.append(entry.getKey());
                }
            }

            return result.toString();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().sortCharactersByFrequency("eeeaaabc"));   // aaaeeebc
        System.out.println(new Solution().sortCharactersByFrequency("zzzxxyyyb"));  // yyyzzzxxb
        System.out.println(new Solution().sortCharactersByFrequency("zzzxxyyb"));   // zzzxxyyb

        // Edge cases
        System.out.println(new Solution().sortCharactersByFrequency("a"));          // a
        System.out.println(new Solution().sortCharactersByFrequency("aa"));         // aa
        System.out.println(new Solution().sortCharactersByFrequency("ab"));         // ab
        System.out.println(new Solution().sortCharactersByFrequency("aabb"));       // aabb
        System.out.println(new Solution().sortCharactersByFrequency("ccbbaa"));     // aabbcc
    }
}
```

The Python and Java implementations follow the same pattern: build a frequency map, sort the keys (or pairs) by `(-freq, char)`, then expand. The exact comparator syntax differs per language (lambda, comparator class, etc.) but the transform `(-freq, char)` is universal.

</details>

***

# Largest Number

Concatenate numbers to form the largest possible number. The custom compare here is *non-obvious*: we don't sort by value or by digit count. We sort by which order produces a larger concatenation.

---

## The Problem

Given non-negative integers `arr`, return the largest number formable by concatenating them, as a string.

```
Input:  arr = [200, 3]
Output: "3200"   (3+200 vs 200+3: "3200" > "2003")

Input:  arr = [200, 8, 1, 3]
Output: "832001"

Input:  arr = [50, 20, 10, 5]
Output: "5502010"
```

---

<details>
<summary><h2>The Custom Compare</h2></summary>


The non-obvious insight: to maximise the concatenation `a + b` vs `b + a`, compare the two string concatenations directly.

```
cmp(a, b) = +1 if str(a)+str(b) < str(b)+str(a)    # b should come first
            -1 if str(a)+str(b) > str(b)+str(a)    # a should come first
             0 if equal
```

This pairwise greedy is correct because of a (non-trivial) transitivity argument: if `(a+b) > (b+a)` and `(b+c) > (c+b)`, then `(a+c) > (c+a)`. We won't prove it here; the result is that this comparator works.

</details>
<details>
<summary><h2>The Solution</h2></summary>


```python run
from typing import List

class Solution:
    def largest_number(self, arr: List[int]) -> str:

        # Sort the numbers using the custom comparator
        arr.sort(key=lambda x: str(x) * 10, reverse=True)

        # Edge case: If the largest number is '0', return "0"
        # (e.g., [0, 0])
        if arr[0] == 0:
            return "0"

        # Concatenate the sorted numbers
        return "".join(map(str, arr))


# Examples from the problem statement
print(Solution().largest_number([200, 3]))          # 3200
print(Solution().largest_number([200, 8, 1, 3]))    # 832001
print(Solution().largest_number([50, 20, 10, 5]))   # 5502010

# Edge cases
print(Solution().largest_number([0, 0]))            # 0
print(Solution().largest_number([0]))               # 0
print(Solution().largest_number([1]))               # 1
print(Solution().largest_number([9, 1]))            # 91
print(Solution().largest_number([10, 2]))           # 210
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public String largestNumber(int[] arr) {

            // Convert int[] to Integer[] for lambda comparator
            Integer[] nums = Arrays
                .stream(arr)
                .boxed()
                .toArray(Integer[]::new);

            // Sort the numbers using a lambda comparator
            Arrays.sort(
                nums,
                (num1, num2) -> {
                    String num1Str = String.valueOf(num1);
                    String num2Str = String.valueOf(num2);

                    // Larger concatenation comes first
                    return (num2Str + num1Str).compareTo(num1Str + num2Str);
                }
            );

            // Edge case: If the largest number is '0', return "0"
            // (e.g., [0, 0])
            if (nums[0] == 0) {
                return "0";
            }

            // Concatenate the sorted numbers
            StringBuilder result = new StringBuilder();
            for (int num : nums) {
                result.append(num);
            }

            return result.toString();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().largestNumber(new int[]{200, 3}));         // 3200
        System.out.println(new Solution().largestNumber(new int[]{200, 8, 1, 3}));   // 832001
        System.out.println(new Solution().largestNumber(new int[]{50, 20, 10, 5}));  // 5502010

        // Edge cases
        System.out.println(new Solution().largestNumber(new int[]{0, 0}));           // 0
        System.out.println(new Solution().largestNumber(new int[]{0}));              // 0
        System.out.println(new Solution().largestNumber(new int[]{1}));              // 1
        System.out.println(new Solution().largestNumber(new int[]{9, 1}));           // 91
        System.out.println(new Solution().largestNumber(new int[]{10, 2}));          // 210
    }
}
```

Both implementations follow this pattern: convert numbers to strings, sort with a comparator that compares `a+b` vs `b+a`, concatenate, handle the all-zeros edge case.

</details>
<details>
<summary><h2>Why the Edge Case?</h2></summary>


If `arr = [0, 0]`, the sorted concatenation is `"00"` — but the largest number formable is `"0"`, not `"00"`. The check `if result[0] == "0"` catches this.

</details>

***

# Sort People by Height

A two-step problem: first sort by a custom rule, then *reconstruct* the order based on a per-element index. The custom compare gets us the initial sort; a clever insertion gets us the final answer.

---

## The Problem

Given `people = [[h_i, k_i], ...]` where `h_i` is the i-th person's height and `k_i` is the number of people standing in front of them whose height is `≥ h_i`. Reorder `people` so the resulting queue satisfies these `k_i` constraints.

```
Input:  people = [[5, 1], [5, 0]]
Output: [[5, 0], [5, 1]]

Input:  people = [[1, 4], [2, 3], [3, 2], [4, 1], [5, 0]]
Output: [[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]
```

---

<details>
<summary><h2>The Two-Step Algorithm</h2></summary>


**Step 1 — sort.** Sort `people` by height descending, breaking ties by `k` ascending. After this, the tallest people are first; among same-height people, the one with smaller `k` is first.

**Step 2 — reconstruct.** Insert each person from the sorted list into a result array at index `k`. Because we process tallest first, by the time person `i` is inserted, exactly `k_i` people of `≥ height` are already in the result — they're the only ones we've inserted, and there's exactly `k_i` of them in front of position `k_i`.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class Solution:
    def sort_people_by_height(
        self, people: list[list[int]]
    ) -> list[list[int]]:

        # Step 1: Sort the people array using a custom lambda comparator
        # Sort by height descending, then k ascending
        people.sort(key=lambda x: (-x[0], x[1]))

        # Step 2: Reconstruct the queue by inserting people at their
        # respective positions
        result = []
        for person in people:

            # Insert at index person[1]
            result.insert(person[1], person)

        return result


# Examples from the problem statement
print(Solution().sort_people_by_height([[5, 1], [5, 0]]))                              # [[5, 0], [5, 1]]
print(Solution().sort_people_by_height([[1, 4], [2, 3], [3, 2], [4, 1], [5, 0]]))     # [[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]
print(Solution().sort_people_by_height([[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]))     # [[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]

# Edge cases
print(Solution().sort_people_by_height([[1, 0]]))                                      # [[1, 0]]
print(Solution().sort_people_by_height([[7, 0], [4, 4], [7, 1], [5, 0], [6, 1], [5, 2]]))  # [[7, 0], [7, 1], [6, 1], [5, 0], [5, 2], [4, 4]]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public int[][] sortPeopleByHeight(int[][] people) {

            // Step 1: Sort the people array
            // Sort by height in descending order,
            // if heights are equal, sort by 'number of people in front' in
            // ascending order
            Arrays.sort(
                people,
                (person1, person2) -> {
                    if (person1[0] != person2[0]) {

                        // Descending order by height
                        return person2[0] - person1[0];
                    } else {

                        // Ascending order by k
                        return person1[1] - person2[1];
                    }
                }
            );

            // Step 2: Reconstruct the queue
            // Insert each person at the index equal to 'number of people in
            // front'
            int[][] result = new int[people.length][2];

            // current size of the result array
            int size = 0;

            for (int[] person : people) {

                // Shift elements to make space for insertion at person[1]
                for (int j = size; j > person[1]; j--) {
                    result[j] = result[j - 1];
                }

                // Insert the person at index person[1]
                result[person[1]] = person;
                size++;
            }

            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        int[][] r1 = new Solution().sortPeopleByHeight(new int[][]{{5, 1}, {5, 0}});
        System.out.println(Arrays.deepToString(r1));  // [[5, 0], [5, 1]]

        int[][] r2 = new Solution().sortPeopleByHeight(new int[][]{{1, 4}, {2, 3}, {3, 2}, {4, 1}, {5, 0}});
        System.out.println(Arrays.deepToString(r2));  // [[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]

        int[][] r3 = new Solution().sortPeopleByHeight(new int[][]{{5, 0}, {4, 1}, {3, 2}, {2, 3}, {1, 4}});
        System.out.println(Arrays.deepToString(r3));  // [[5, 0], [4, 1], [3, 2], [2, 3], [1, 4]]

        // Edge cases
        int[][] r4 = new Solution().sortPeopleByHeight(new int[][]{{1, 0}});
        System.out.println(Arrays.deepToString(r4));  // [[1, 0]]

        int[][] r5 = new Solution().sortPeopleByHeight(new int[][]{{7, 0}, {4, 4}, {7, 1}, {5, 0}, {6, 1}, {5, 2}});
        System.out.println(Arrays.deepToString(r5));  // [[7, 0], [7, 1], [6, 1], [5, 0], [5, 2], [4, 4]]
    }
}
```

The custom compare is the `(-height, k)` sort key. The reconstruction is the second clever step. The Python and Java implementations follow the same recipe.

### Complexity

- Step 1 (sort): `O(n log n)`.
- Step 2 (reconstruct): `O(n²)` worst case because each `insert` at an arbitrary index is `O(n)`.

Total: `O(n²)`. Better data structures (Fenwick tree, balanced BST) can reduce step 2 to `O(n log n)`.

</details>

***

# Final Takeaway

Custom compare is the universal interface between sort algorithms and ordering rules. Three styles: operator overloading, lambda, comparator class. Two questions to ask: *what's the transformation?* and *is it a function of the element alone?*

Once you internalise this, every "sort by X" problem reduces to "find the right `t(elem) → key`," and the algorithm is whatever your standard library provides.

This closes the sorting section. You came in with bubble sort. You're leaving with:
- 10 different sorting algorithms across the comparison and counting families.
- Three classification systems: stability, in-place, adaptiveness.
- Two divide-and-conquer paradigms: pivot-based (quicksort) and split-based (merge sort).
- One linear-time sort (counting) and a near-linear three-way partition (Dutch flag).
- Quickselect for `O(n)` k-th element queries.
- The custom-compare pattern that makes any sort universal.

The next major topic in the algorithms section is **searching** — binary search and its many variants. Many of those variants assume the input is sorted, which is exactly what you've spent the last 12 lessons learning to do efficiently.

**Transfer challenge — close out the sorting section:** Write a custom comparator that sorts strings *case-insensitively* but breaks ties by the original case (uppercase before lowercase). For example, `["banana", "Apple", "apple", "Banana"]` should sort to `["Apple", "apple", "Banana", "banana"]`.

<details>
<summary><strong>Answer — open after you've thought about it</strong></summary>

```python run
class Solution:
    def case_insensitive_sort(self, arr):
        # Sort by (lowercase, original) — lowercase comparison first, original is the tiebreaker.
        # Original case ordering: uppercase letters have lower ASCII values, so they come first.
        arr.sort(key=lambda s: (s.lower(), s))


arr = ["banana", "Apple", "apple", "Banana"]
Solution().case_insensitive_sort(arr)
print(arr)   # ['Apple', 'apple', 'Banana', 'banana']
```

The transformation `(s.lower(), s)` produces a tuple sort-key. Tuples compare lexicographically: first compare the lowercase strings, then break ties on the original (which puts uppercase before lowercase due to ASCII order).

This pattern — *case-insensitive primary, case-sensitive tiebreaker* — appears in file managers, text editors, and any system that displays user-facing sorted lists. **You just generalised every "sort by X with Y as tiebreaker" problem you'll ever see.**

</details>
