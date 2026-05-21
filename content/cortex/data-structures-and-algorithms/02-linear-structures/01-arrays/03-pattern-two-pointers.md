# 3. Pattern: Two pointers

This section introduces the direct-application version of the two-pointer pattern and walks through representative problems.

## Table of contents

1. [Understanding the two pointer pattern](#understanding-the-two-pointer-pattern)
2. [Identifying direct application](#identifying-direct-application)
3. [Flip characters](#flip-characters)
4. [Palindrome checker](#palindrome-checker)
5. [Vowel exchange](#vowel-exchange)
6. [Reverse words](#reverse-words)
7. [Reverse segments](#reverse-segments)
8. [Reverse word order](#reverse-word-order)

***

# Understanding the Two Pointer Pattern

## Why Single-Direction Traversal Isn't Always Enough

When you work with arrays, a single loop moving left to right is your default tool. It handles most problems cleanly. But some problems have a property that a single direction completely ignores: **both ends of the array matter at the same time**.

Think about checking if a word is a palindrome. You need to compare the first character with the last, the second with the second-to-last — you're always looking at two positions simultaneously, one from each end. A single forward loop forces you to either:
- Use a second nested loop (O(n²) — very slow), or
- Store results and do two passes (extra space)

The **two-pointer technique** solves this elegantly: use two variables, `left` and `right`, as indices that start at opposite ends and march toward each other in a single pass.

---

## The Core Idea

Two pointers (`left` and `right`) start at opposite ends of the array and converge toward the middle. At each step, you do some work using both `arr[left]` and `arr[right]`, then move one or both pointers inward.

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
  L(["left = 0<br/>→"]) -->|"starts here"| A0

  subgraph ARR["Array"]
    direction LR
    A0["arr[0]"] --- A1["arr[1]"] --- A2["arr[2]"] --- MID["···"] --- An2["arr[n-3]"] --- An1["arr[n-2]"] --- An["arr[n-1]"]
  end

  An -->|"starts here"| R(["← right = n-1"])
  MID -.->|"loop ends when<br/>left ≥ right"| STOP(["✓ done"])
```

<p align="center"><strong>The two-pointer traversal — <code>left</code> starts at index 0 and advances right; <code>right</code> starts at index n−1 and retreats left. They meet in the middle.</strong></p>

The loop terminates when `left >= right`. At that point, every pair of equidistant positions has been visited exactly once — which is why the algorithm runs in **O(n) time with O(1) extra space**.

---

## How the Pointers Move

Here is the full picture of a single traversal on an array of size 7:

```d3 widget=array-traversal
{
  "items": ["A", "B", "C", "D", "E", "F", "G"],
  "title": "Two-pointer traversal on a 7-element array",
  "steps": [
    {
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "range":   { "lo": 0, "hi": 6 },
      "msg": "Initial state — left = 0, right = 6. The whole array lies between the pointers."
    },
    {
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 5, "color": "#f59e0b" }
      ],
      "range":   { "lo": 1, "hi": 5 },
      "msg": "After iteration 1 — A and G have been processed; left++, right--."
    },
    {
      "markers": [
        { "name": "left",  "index": 2, "color": "#3b82f6" },
        { "name": "right", "index": 4, "color": "#f59e0b" }
      ],
      "range":   { "lo": 2, "hi": 4 },
      "msg": "After iteration 2 — B and F processed; left++, right--."
    },
    {
      "markers": [
        { "name": "left",  "index": 3, "color": "#3b82f6" },
        { "name": "right", "index": 3, "color": "#f59e0b" }
      ],
      "range":   { "lo": 3, "hi": 3 },
      "msg": "After iteration 3 — C and E processed; left = right = 3."
    },
    {
      "markers": [],
      "msg": "left ≥ right — loop ends. The centre element D is handled separately if the problem requires it."
    }
  ]
}
```

<p align="center"><strong>Iteration-by-iteration view of the two-pointer traversal on a 7-element array — each step processes one pair of equidistant elements and closes the gap by one on each side.</strong></p>

---

## The Generic Algorithm

The two-pointer pattern follows this skeleton for every problem that uses it directly:

**Step 1.** Initialise `left = 0`, `right = n − 1` (or whatever starting positions the problem requires, as long as `left < right`).

**Step 2.** Loop while `left < right`:
- **Step 2.1** — do some work on `arr[left]` and `arr[right]`
- **Step 2.2** — move `left` forward by some number of steps (if the problem requires it)
- **Step 2.3** — move `right` backward by some number of steps (if the problem requires it)

**Step 3.** Return the result.

The specific "work" and "step size" in steps 2.1–2.3 change per problem. Everything else stays the same.

---

## Generic Implementation


```python run
from typing import List

class Solution:
    # Generic code for two-pointer traversal
    def two_pointer(self, arr: List[int]) -> None:

        # Initialize left and right to the ends of the array
        left = 0
        right = len(arr) - 1

        while left < right:
            left_val = arr[left]
            right_val = arr[right]

            # Check if the left pointer should be incremented
            if self.increment_left(left_val, right_val):
                # Increment the left pointer by some steps
                left += self.left_step(left_val, right_val)

            # Check if the right pointer should be decremented
            if self.decrement_right(left_val, right_val):
                # Decrement the right pointer by some steps
                right -= self.right_step(left_val, right_val)

    # Decide whether to move the left pointer
    def increment_left(self, left_val: int, right_val: int) -> bool:
        # Example condition: move left if sum < 10
        return left_val + right_val < 10

    # Decide whether to move the right pointer
    def decrement_right(self, left_val: int, right_val: int) -> bool:
        # Example condition: move right if sum > 10
        return left_val + right_val > 10

    # Steps to move the left pointer
    def left_step(self, left_val: int, right_val: int) -> int:
        return 1  # Example: move 1 step

    # Steps to move the right pointer
    def right_step(self, left_val: int, right_val: int) -> int:
        return 1  # Example: move 1 step
```

```java run
import java.util.List;

class Solution {

    // Generic code for two-pointer traversal
    public void twoPointer(List<Integer> arr) {

        // Initialize left and right to the ends of the array
        int left = 0;
        int right = arr.size() - 1;

        while (left < right) {
            int leftVal = arr.get(left);
            int rightVal = arr.get(right);

            // Check if the left pointer should be incremented
            if (incrementLeft(leftVal, rightVal)) {
                // Increment the left pointer by some steps
                left += leftStep(leftVal, rightVal);
            }

            // Check if the right pointer should be decremented
            if (decrementRight(leftVal, rightVal)) {
                // Decrement the right pointer by some steps
                right -= rightStep(leftVal, rightVal);
            }
        }
    }

    // Decide whether to move the left pointer
    private boolean incrementLeft(int leftVal, int rightVal) {
        // Example condition: move left if sum < 10
        return (leftVal + rightVal < 10);
    }

    // Decide whether to move the right pointer
    private boolean decrementRight(int leftVal, int rightVal) {
        // Example condition: move right if sum > 10
        return (leftVal + rightVal > 10);
    }

    // Steps to move the left pointer
    private int leftStep(int leftVal, int rightVal) {
        return 1; // Example: move 1 step
    }

    // Steps to move the right pointer
    private int rightStep(int leftVal, int rightVal) {
        return 1; // Example: move 1 step
    }
}
```


---

## Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | `left` and `right` together visit every index exactly once. No element is processed twice. |
| **Space** | O(1) | Only two integer variables (`left` and `right`) are needed regardless of input size. |

This is true for every problem that directly applies the two-pointer technique — **both best and worst case are O(n) time, O(1) space**.

The power of this pattern is that it reduces problems which naively need O(n²) nested loops to a single O(n) pass.

---

## Three Ways to Apply Two Pointers

Not every two-pointer problem is identical. Problems in this pattern fall into three categories:

```d2
Root: Two-Pointer Pattern Problems

D: |md
  **Direct Application**

  Two pointers applied as-is

  (e.g. reverse, palindrome check)
|

R: |md
  **Reduction**

  Problem reduced to an
  equivalent two-pointer problem
|

S: |md
  **Subproblems**

  One step of the solution
  uses two pointers internally
|

Root -> D
Root -> R
Root -> S
```

<p align="center"><strong>Three categories of two-pointer pattern problems — we start with Direct Application, which is the simplest and most common.</strong></p>

In the next lessons, we work through the **Direct Application** category in depth — problems where the two-pointer template above applies almost verbatim.

***

# Identifying Direct Application

## What Makes a Problem a "Direct Application"?

The two-pointer technique can be applied **directly** when the problem asks you to do something with pairs of elements — one from each end — while moving both pointers inward until they meet. No clever transformation needed. The template fits as-is.

The mental checklist:

> ✅ We need to look at two positions simultaneously
> ✅ One position starts near the beginning, one near the end
> ✅ Both move inward with each iteration
> ✅ The work done at each step is simple (swap, compare, copy…)

If those four boxes are checked, you're looking at a direct application.

---

## The Canonical Example: Reverse an Array In-Place

**Problem statement:** Given an array `arr`, reverse it in-place. Do not create and return a new array — modify the original.

```
Input:  arr = [1, 2, 3, 4, 5]
Output: arr is modified to [5, 4, 3, 2, 1]
```

```d2
direction: right

before: "Original" {
  grid-columns: 5
  grid-gap: 0
  a: "1"
  b: "2"
  c: "3"
  d: "4"
  e: "5"
}

after: "Reversed (in-place)" {
  grid-columns: 5
  grid-gap: 0
  a: "5"
  b: "4"
  c: "3"
  d: "2"
  e: "1"
}

before -> after
```

<p align="center"><strong>Reverse the array in-place — the original array (top) becomes the reversed array (bottom) without allocating new memory.</strong></p>

---

## Brute Force: Two Passes + Temp Array

The naive approach copies elements in reverse into a temporary array, then copies back:

1. Walk `arr` backwards and fill `temp` forwards
2. Walk `temp` forwards and copy back into `arr`

```d2
direction: right

ORIG: "Original arr" {
  grid-columns: 5
  grid-gap: 0
  a: "1"
  b: "2"
  c: "3"
  d: "4"
  e: "5"
}

TEMP: "temp (copy arr backwards)" {
  grid-columns: 5
  grid-gap: 0
  a: "5"
  b: "4"
  c: "3"
  d: "2"
  e: "1"
}

BACK: "arr (copy temp back)" {
  grid-columns: 5
  grid-gap: 0
  a: "5"
  b: "4"
  c: "3"
  d: "2"
  e: "1"
}

ORIG -> TEMP: pass 1 — backwards copy
TEMP -> BACK: pass 2 — forwards copy
```

<p align="center"><strong>Brute-force reversal — two full passes and O(n) extra space for the temp array.</strong></p>


```python run
from typing import List

class BruteForce:
    def reverse(self, arr: List[int]) -> None:
        n = len(arr)
        temp = [0] * n

        # Pass 1: copy arr backwards into temp.
        for i in range(n - 1, -1, -1):
            temp[n - 1 - i] = arr[i]

        # Pass 2: copy temp back into arr.
        for i in range(n):
            arr[i] = temp[i]


arr = [1, 2, 3, 4, 5]
BruteForce().reverse(arr)
print(arr)   # [5, 4, 3, 2, 1]
```

```java run
import java.util.Arrays;

public class Main {
    static class BruteForce {
        void reverse(int[] arr) {
            int n = arr.length;
            int[] temp = new int[n];
            // Pass 1: copy backwards into temp.
            for (int i = n - 1; i >= 0; i--) temp[n - 1 - i] = arr[i];
            // Pass 2: copy temp back into arr.
            for (int i = 0; i < n; i++) arr[i] = temp[i];
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        new BruteForce().reverse(arr);
        System.out.println(Arrays.toString(arr));
    }
}
```


This works, but it uses O(n) extra space and touches every element twice. We can do better.

---

## Two-Pointer Solution: One Pass, Zero Extra Space

**Key insight:** to reverse an array, we just need to swap equidistant elements from both ends — `arr[0] ↔ arr[n-1]`, `arr[1] ↔ arr[n-2]`, and so on. Each swap needs exactly two positions: one from the left, one from the right. That's the two-pointer template.

```d3 widget=array-traversal
{
  "items": ["1", "2", "3", "4", "5"],
  "title": "Two-pointer reversal on [1, 2, 3, 4, 5]",
  "steps": [
    {
      "items":   ["1", "2", "3", "4", "5"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 4, "color": "#f59e0b" }
      ],
      "msg": "Initial — left = 0, right = 4. Swap arr[0]=1 with arr[4]=5."
    },
    {
      "items":   ["5", "2", "3", "4", "1"],
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 3, "color": "#f59e0b" }
      ],
      "msg": "Move inward — left = 1, right = 3. Swap arr[1]=2 with arr[3]=4."
    },
    {
      "items":   ["5", "4", "3", "2", "1"],
      "markers": [
        { "name": "left",  "index": 2, "color": "#3b82f6" },
        { "name": "right", "index": 2, "color": "#f59e0b" }
      ],
      "msg": "left = right = 2 — pointers meet, the middle element stays. Result: [5, 4, 3, 2, 1]."
    }
  ]
}
```

<p align="center"><strong>Two-pointer reversal on <code>[1, 2, 3, 4, 5]</code> — two swaps close the gap from both ends; the middle element needs no swap.</strong></p>


```python run
from typing import List

class Solution:
    def reverse(self, arr: List[int]) -> None:

        # Initialize two pointers, one pointing to the beginning of the
        # array and the other pointing to the end of the array
        left: int = 0
        right = len(arr) - 1

        # Use a while loop to traverse the array using the two pointers
        while left < right:

            # Swap the values pointed by the left and right pointers
            arr[left], arr[right] = arr[right], arr[left]

            # Move the pointers towards the center of the array
            left += 1
            right -= 1
```

```java run
class Solution {
    public void reverse(int[] arr) {

        // Initialize two pointers, one pointing to the beginning of the
        // array and the other pointing to the end of the array
        int left = 0;
        int right = arr.length - 1;

        // Use a while loop to traverse the array using the two pointers
        while (left < right) {

            // Swap the values pointed by the left and right pointers
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;

            // Move the pointers towards the center of the array
            left++;
            right--;
        }
    }
}
```


One pass. No extra memory. The two-pointer template applied directly.

<details>
<summary><strong>Trace — arr = [1, 2, 3, 4, 5]</strong></summary>

```
arr = [1, 2, 3, 4, 5]   left = 0,  right = 4

Step 1 │ left=0 (1),  right=4 (5) │ 0 < 4 → swap │ [5, 2, 3, 4, 1] │ left=1, right=3
Step 2 │ left=1 (2),  right=3 (4) │ 1 < 3 → swap │ [5, 4, 3, 2, 1] │ left=2, right=2
Step 3 │ left=2 (3),  right=2 (3) │ 2 == 2 → left < right is false → loop exits

Result: [5, 4, 3, 2, 1] ✓

Note: The middle element (3) never needed a swap — it's equidistant from both ends
      and sits in its correct reversed position automatically.

Even-length check — arr = [1, 2, 3, 4]:
Step 1 │ left=0 (1),  right=3 (4) │ 0 < 3 → swap │ [4, 2, 3, 1] │ left=1, right=2
Step 2 │ left=1 (2),  right=2 (3) │ 1 < 2 → swap │ [4, 3, 2, 1] │ left=2, right=1
Step 3 │ left=2,      right=1     │ 2 > 1 → loop exits (pointers crossed)

Result: [4, 3, 2, 1] ✓

Note: For even-length arrays the pointers cross (left > right) rather than meet —
      all pairs are handled before that happens.
```

</details>

---

## Fitting the Template

Let's verify this problem matches all four checkboxes:

| Checkpoint | This Problem |
|---|---|
| Two positions at once? | ✅ `arr[left]` and `arr[right]` |
| One near start, one near end? | ✅ `left=0`, `right=n-1` |
| Both move inward? | ✅ `left++`, `right--` each iteration |
| Simple work per step? | ✅ One swap |

---

### Checkpoint 1 — Why "two positions at once"?

**WHAT:** A reversal requires swapping pairs of elements. A swap is an inherently two-element operation — you can't swap a single element with nothing.

**WHY it means two pointers fit:** Every step of the algorithm needs `arr[left]` and `arr[right]` simultaneously. One pointer alone can't do the job — it would need to "remember" the element it picked up and go look for where to put it, which is exactly what the brute force does (it stores the whole array in `temp`).

**What breaks with a single pointer:** Walk forward with one pointer and try to reverse in-place — when you overwrite `arr[0]` with `arr[4]`, the original `arr[0]` is gone. You'd need to save it somewhere first. That "somewhere" is the temp array. Two pointers sidestep the need entirely: they swap atomically (`a, b = b, a`), so nothing is lost.

> **Rule of thumb:** If the problem needs to operate on two elements that "belong together" (a pair, a palindrome check, a sum), two simultaneous pointers are the natural fit.

---

### Checkpoint 2 — Why "one near start, one near end"?

**WHAT:** `left` starts at index `0` (the first element), `right` starts at index `n-1` (the last element).

**WHY this specific placement:** After reversal, element at index `0` must land at index `n-1` and vice versa. Element at index `1` must land at `n-2`. The pattern is: **element at distance `d` from the left end swaps with element at distance `d` from the right end**.

Placing one pointer at each end directly aligns with this structure — at every step, `left` and `right` are pointing at exactly the pair that needs to swap next.

**What breaks with a different placement:** If both pointers started from the left (like in many sliding window problems), you'd never naturally reach the element at `n-1` first. You'd be doing something fundamentally different — not a reversal.

**Concrete check:** `[1, 2, 3, 4, 5]`
- `left=0, right=4`: `1` and `5` are the farthest pair — swap first ✓
- `left=1, right=3`: `2` and `4` are next closest pair — swap second ✓
- `left=2, right=2`: single middle element — no swap needed ✓

---

### Checkpoint 3 — Why "both move inward"?

**WHAT:** After each swap, both `left` increments by 1 and `right` decrements by 1.

**WHY inward movement:** After swapping `arr[left]` and `arr[right]`, those two positions are finalized — they now hold the correct values for a reversed array. The unsolved subproblem is the inner portion: `arr[left+1 .. right-1]`. Moving both pointers inward shrinks the problem by 2 each step and focuses attention on exactly what's left to solve.

**Why the stop condition is `left < right` (not `left <= right`):**
- When `left == right` (odd-length arrays): both pointers are at the same middle element. That element is already in the correct position — swapping it with itself is a no-op. We stop before that unnecessary step.
- When `left > right` (even-length arrays): the pointers have crossed, all pairs have been handled. No element remains.

**What breaks if you stop at `left <= right`:** For odd-length arrays like `[1, 2, 3, 4, 5]`, when `left = right = 2`, you'd execute `arr[2], arr[2] = arr[2], arr[2]` — harmless but wasteful. For the stop condition to be correct, `left < right` is sufficient and exact.

---

### Checkpoint 4 — Why "simple work per step"?

**WHAT:** At each step, we do exactly one swap: `arr[left], arr[right] = arr[right], arr[left]`.

**WHY "simple" matters:** The two-pointer direct application template is powerful precisely because the loop body is O(1) — a fixed amount of work per step. Combined with O(n) steps (n/2 swaps), the total is O(n) time. The simplicity of the per-step work is what keeps the whole algorithm lean.

**HOW to recognize "simple work" in other problems:** The work per step should not require nested loops, searching, or recursion. It should be a direct operation on the two elements currently pointed at — compare, swap, copy, check equality. If the per-step work itself requires a loop, you may be looking at a subproblem pattern instead (section 05).

**What this rules out:** If you found yourself needing to scan the entire remaining array on each step, that's O(n²) and the direct-application template no longer applies cleanly.

---

That's a direct application — all four checkboxes confirmed.

---

## Problems in This Category

The following lessons each apply the two-pointer technique in exactly this direct way. Each is a small variation on the same theme:

| Problem | Two-pointer work per step |
|---|---|
| **Flip Characters** | Swap characters from both ends |
| **Palindrome Checker** | Compare characters from both ends |
| **Vowel Exchange** | Find and swap vowels from both ends |
| **Reverse Words** | Reverse each word's characters with inner two pointers |
| **Reverse Segments** | Reverse the first *k* characters of every *2k* block |
| **Reverse Word Order** | Reverse entire string, then reverse each word |

Each is a small twist on the same pattern — same skeleton, different work in the loop body.

***

# Flip Characters

## The Problem

Given an array of characters `arr`, reverse the array by **swapping equidistant elements** from the start and the end. The reversal must happen **in-place** — modify the input array directly and use **O(1) extra space**.

```
Input:  arr = [a, e, i, o, u]
Output:       [u, o, i, e, a]
```

This is the canonical direct application of the two-pointer pattern — the template and the algorithm are identical.

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1**
```
Input:  arr = [a, e, i, o, u]
Output:       [u, o, i, e, a]
```

**Example 2**
```
Input:  arr = [a, b, c, d, e]
Output:       [e, d, c, b, a]
```

**Example 3 — empty array**
```
Input:  arr = []
Output:       []
```

</details>
<details>
<summary><h2>Intuition</h2></summary>


To reverse a sequence, the first element must become the last, the second must become the second-to-last, and so on. Every character has a **mirror partner** equidistant from the opposite end. We just need to swap each pair.

Two pointers are perfect for this: `left` starts at index 0 (the first character), `right` starts at index `n-1` (the last character). Swap the pair, then move both inward. Repeat until they meet.

```d3 widget=array-traversal
{
  "items": ["a", "e", "i", "o", "u"],
  "title": "Reversing [a, e, i, o, u] in place with two pointers",
  "steps": [
    {
      "items":   ["a", "e", "i", "o", "u"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 4, "color": "#f59e0b" }
      ],
      "msg": "Initial — left = 0, right = 4. Swap arr[left] and arr[right] (a ↔ u)."
    },
    {
      "items":   ["u", "e", "i", "o", "a"],
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 3, "color": "#f59e0b" }
      ],
      "msg": "Move inward — left = 1, right = 3. Swap arr[left] and arr[right] (e ↔ o)."
    },
    {
      "items":   ["u", "o", "i", "e", "a"],
      "markers": [
        { "name": "left",  "index": 2, "color": "#3b82f6" },
        { "name": "right", "index": 2, "color": "#f59e0b" }
      ],
      "msg": "Pointers meet at index 2 — the middle element is its own mirror; no swap needed."
    },
    {
      "items":   ["u", "o", "i", "e", "a"],
      "markers": [],
      "msg": "Done — arr is reversed: [u, o, i, e, a]."
    }
  ]
}
```

<p align="center"><strong>Flipping <code>[a, e, i, o, u]</code> in place — two swaps reverse the array; the middle element at index 2 is its own mirror.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Flip Characters |
|---|---|
| ✅ Two positions simultaneously? | Yes — `chars[left]` and `chars[right]` are read and swapped together at every step |
| ✅ One near start, one near end? | Yes — `left = 0`, `right = n-1` |
| ✅ Both move inward? | Yes — `left++`, `right--` after every swap |
| ✅ Simple work at each step? | Yes — one swap per iteration |

Every box is checked with nothing extra needed. This is the purest direct application — the template and the algorithm are identical.

**Why does every element have exactly one partner?** Because reversal is a bijection: element at position `i` maps to position `n-1-i`. Two pointers exploit this directly — `left` tracks "the element at distance 0 from the left" and `right` tracks "the element at distance 0 from the right." Every step, both advance one position inward, so the i-th iteration handles the i-th mirror pair. When `left >= right`, all pairs have been processed.

**What breaks if you use one pointer instead?** A single forward pointer at position `i` can move `chars[i]` to its destination at `n-1-i`, but it has already overwritten whatever was at `n-1-i` — you need a temp variable and a second loop. Two pointers avoid this entirely: the swap is symmetric, so both elements land in their correct positions in one step, no temp array required.

</details>
<details>
<summary><h2>Approach</h2></summary>


1. Set `left = 0`, `right = len(chars) - 1`
2. While `left < right`:
   - Swap `chars[left]` and `chars[right]`
   - `left += 1`, `right -= 1`
3. Done — the array is reversed in-place

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
from typing import List

class Solution:
    def flip_characters(self, arr: List[str]) -> None:

        # Initialize two pointers, one pointing to the beginning of the
        # array and the other pointing to the end of the array
        left: int = 0
        right = len(arr) - 1

        # Use a while loop to traverse the array using the two pointers
        while left < right:

            # Swap the characters pointed by the left and right pointers
            arr[left], arr[right] = arr[right], arr[left]

            # Move the pointers towards the center of the array
            left  += 1
            right -= 1


# Examples from the problem statement
a1 = ['a', 'e', 'i', 'o', 'u']
Solution().flip_characters(a1); print(a1)         # ['u', 'o', 'i', 'e', 'a']

a2 = ['a', 'b', 'c', 'd', 'e']
Solution().flip_characters(a2); print(a2)         # ['e', 'd', 'c', 'b', 'a']

a3: List[str] = []
Solution().flip_characters(a3); print(a3)         # []

# Edge cases
a4 = ['x']                                        # single element — no swap
Solution().flip_characters(a4); print(a4)         # ['x']

a5 = ['x', 'y']                                   # two elements — single swap
Solution().flip_characters(a5); print(a5)         # ['y', 'x']

a6 = ['a', 'b', 'a']                              # palindrome stays a palindrome
Solution().flip_characters(a6); print(a6)         # ['a', 'b', 'a']

a7 = ['z', 'z', 'z', 'z']                         # all same — output identical
Solution().flip_characters(a7); print(a7)         # ['z', 'z', 'z', 'z']

a8 = list('abcdefghij')                           # longer even-length input
Solution().flip_characters(a8); print(a8)         # ['j', 'i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
```

```java run
import java.util.Arrays;

public class Main {
    static class Solution {
        void flipCharacters(char[] arr) {

            // Initialize two pointers, one pointing to the beginning of the
            // array and the other pointing to the end of the array
            int left  = 0;
            int right = arr.length - 1;

            // Use a while loop to traverse the array using the two pointers
            while (left < right) {

                // Swap the characters pointed by the left and right pointers
                char tmp     = arr[left];
                arr[left]    = arr[right];
                arr[right]   = tmp;

                // Move the pointers towards the center of the array
                left++;
                right--;
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        char[] a1 = {'a', 'e', 'i', 'o', 'u'};
        new Solution().flipCharacters(a1);
        System.out.println(Arrays.toString(a1));     // [u, o, i, e, a]

        char[] a2 = {'a', 'b', 'c', 'd', 'e'};
        new Solution().flipCharacters(a2);
        System.out.println(Arrays.toString(a2));     // [e, d, c, b, a]

        char[] a3 = {};
        new Solution().flipCharacters(a3);
        System.out.println(Arrays.toString(a3));     // []

        // Edge cases
        char[] a4 = {'x'};                           // single element — no swap
        new Solution().flipCharacters(a4);
        System.out.println(Arrays.toString(a4));     // [x]

        char[] a5 = {'x', 'y'};                      // two elements — single swap
        new Solution().flipCharacters(a5);
        System.out.println(Arrays.toString(a5));     // [y, x]

        char[] a6 = {'a', 'b', 'a'};                 // palindrome stays a palindrome
        new Solution().flipCharacters(a6);
        System.out.println(Arrays.toString(a6));     // [a, b, a]

        char[] a7 = {'z', 'z', 'z', 'z'};            // all same — output identical
        new Solution().flipCharacters(a7);
        System.out.println(Arrays.toString(a7));     // [z, z, z, z]

        char[] a8 = "abcdefghij".toCharArray();      // longer even-length input
        new Solution().flipCharacters(a8);
        System.out.println(Arrays.toString(a8));     // [j, i, h, g, f, e, d, c, b, a]
    }
}
```

### Dry Run — Example 1

`arr = [a, e, i, o, u]`, `n = 5`

| Iteration | `left` | `right` | Swap | Array after swap |
|---|---|---|---|---|
| 1 | 0 | 4 | `a ↔ u` | `[u, e, i, o, a]` |
| 2 | 1 | 3 | `e ↔ o` | `[u, o, i, e, a]` |
| — | 2 | 2 | `left ≥ right` — stop | `[u, o, i, e, a]` ✓ |

The middle element at index 2 (`i`) is its own mirror — no swap needed.

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Each character is visited once; `left` and `right` together make n/2 swaps |
| **Space** | O(1) | Only two pointer variables — no auxiliary array |

### Edge Cases

| Scenario | Input | Output | Note |
|---|---|---|---|
| Empty array | `[]` | `[]` | `left = 0 > right = -1` — loop never runs |
| Single character | `['A']` | `['A']` | `left = right = 0` — loop never runs |
| Two characters | `['A','B']` | `['B','A']` | One swap, then `left = right = 1` — stops |
| Even length | `['A','B','C','D']` | `['D','C','B','A']` | All pairs swapped, no middle element |
| Odd length | `['A','B','C']` | `['C','B','A']` | Two pairs swapped, middle `'B'` unchanged |

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Flip Characters is the two-pointer reversal pattern applied to a character array. The mechanics are identical to reversing integers — the only difference is the element type. Every future problem in this section is a variation on this same core swap-and-converge idea.

</details>

***

# Palindrome Checker

## The Problem

Given a string `s`, return `true` if it is a **palindrome** — a string that reads the same forwards and backwards **after converting all uppercase letters to lowercase and removing all non-alphanumeric characters**. Return `false` otherwise.

Alphanumeric characters are letters and digits: `[a–z]`, `[A–Z]`, `[0–9]`. Everything else (spaces, punctuation) is skipped.

```
Input:  s = "a man nam a"   →   Output: True   (after filtering: "amannama")
Input:  s = "race car rac ecar" → Output: True (after filtering: "racecarracecar")
Input:  s = "This is codeintuition" → Output: False
```

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1**
```
Input:  s = "a man nam a"
Output: True
Explanation: Removing spaces and lower-casing gives "amannama", which is a palindrome.
```

**Example 2**
```
Input:  s = "race car rac ecar"
Output: True
Explanation: After filtering, "racecarracecar" is a palindrome.
```

**Example 3**
```
Input:  s = "This is codeintuition"
Output: False
Explanation: After filtering, "thisiscodeintuition" reads differently forwards vs backwards.
```

**Example 4 — single character**
```
Input:  s = "a"
Output: True   (every single alphanumeric character is trivially a palindrome)
```

</details>
<details>
<summary><h2>Intuition</h2></summary>


A string is a palindrome when its first alphanumeric character (lowercased) equals its last, its second equals its second-to-last, and so on all the way to the middle.

That's a mirror-pair relationship — exactly what two pointers are built for. Place `left` at the start and `right` at the end. At each step, examine `s[left]` and `s[right]`:

- If either is **not alphanumeric** → skip it (advance `left` or retreat `right` past it)
- If both are alphanumeric and **match** (case-insensitive) → the pair is fine, move both inward and continue
- If both are alphanumeric and **don't match** → it's not a palindrome, return `False` immediately
- If `left >= right` without any mismatch → every pair matched, return `True`

No extra memory needed. One pass.

```d3 widget=array-traversal
{
  "items": ["r", "a", "c", "e", "c", "a", "r"],
  "title": "Checking \"racecar\" for palindrome",
  "steps": [
    {
      "keys":    ["r0", "a0", "c0", "e", "c1", "a1", "r1"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "msg": "Initial — compare s[0]='r' with s[6]='r' → match, move both inward."
    },
    {
      "keys":    ["r0", "a0", "c0", "e", "c1", "a1", "r1"],
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 5, "color": "#f59e0b" }
      ],
      "msg": "Compare s[1]='a' with s[5]='a' → match, move both inward."
    },
    {
      "keys":    ["r0", "a0", "c0", "e", "c1", "a1", "r1"],
      "markers": [
        { "name": "left",  "index": 2, "color": "#3b82f6" },
        { "name": "right", "index": 4, "color": "#f59e0b" }
      ],
      "msg": "Compare s[2]='c' with s[4]='c' → match, move both inward."
    },
    {
      "keys":    ["r0", "a0", "c0", "e", "c1", "a1", "r1"],
      "markers": [
        { "name": "left",  "index": 3, "color": "#3b82f6" },
        { "name": "right", "index": 3, "color": "#f59e0b" }
      ],
      "msg": "Pointers meet at index 3 (the middle 'e'). All pairs matched — return True."
    }
  ]
}
```

<p align="center"><strong>Checking <code>"racecar"</code> for palindrome — every mirror pair matches; when pointers meet at the centre, the check passes.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Palindrome Checker |
|---|---|
| ✅ Two positions simultaneously? | Yes — `s[left]` and `s[right]` are compared together at every step |
| ✅ One near start, one near end? | Yes — `left = 0`, `right = n-1` |
| ✅ Both move inward? | Yes — `left++`, `right--` after every matching pair |
| ✅ Simple work at each step? | Yes — one comparison per iteration, return immediately on mismatch |

The structure is identical to Flip Characters — the only difference is the loop body: **compare** instead of **swap**.

**Why check from both ends simultaneously?** A palindrome's definition is symmetric: the character at position `i` from the left must equal the character at position `i` from the right, for every `i` from `0` to `n/2`. Two pointers map this requirement directly — `left` and `right` track the pair at distance `i` from each end. Moving both inward covers every required pair in exactly `n/2` steps.

**What breaks if you use only one pointer?** A single pointer could reverse the string and compare — but that costs O(n) extra space for the reversed copy and a second O(n) pass. Two pointers do it in one pass with O(1) space, and gain the early-exit advantage: as soon as any pair mismatches, `False` is returned without inspecting the rest. For a string like `"abcde...xyz" + "XYZ"`, the mismatch at position 0 stops the algorithm immediately.

</details>
<details>
<summary><h2>What Failure Looks Like</h2></summary>


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
  subgraph FAIL["Checking 'hello'  —  left=0, right=4"]
    direction LR
    FL(["left"]) --> H["h"] --- E["e"] --- L1["l"] --- L2["l"] --- O["o"]
    O --> FR(["right"])
  end
  CMP{"'h' = 'o' ?"}
  NO(["✗  return False"])

  FAIL --> CMP
  CMP -->|"No"| NO
```

<p align="center"><strong>The check fails immediately on the first pair — <code>'h' ≠ 'o'</code> is enough to return <code>False</code> without looking at the rest.</strong></p>

This early-exit property makes two-pointer palindrome checking efficient in practice — you never process more pairs than necessary.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
class Solution:
    def palindrome_checker(self, s: str) -> bool:
        if not s:

            # An empty string is considered a palindrome
            return True

        left = 0
        right = len(s) - 1

        while left < right:
            char_left = s[left]
            char_right = s[right]

            # Skip non-alphanumeric characters from the left
            if not char_left.isalnum():
                left += 1

            # Skip non-alphanumeric characters from the end
            elif not char_right.isalnum():
                right -= 1

            # Check if the characters are equal ignoring case
            elif char_left.lower() != char_right.lower():

                # Characters are not equal, so it's not a palindrome
                return False

            # Move both pointers towards the center
            else:
                left += 1
                right -= 1

        # All characters have been checked and are equal, so it's a
        # palindrome
        return True


# Examples from the problem statement
print(Solution().palindrome_checker("a man nam a"))          # True
print(Solution().palindrome_checker("race car rac ecar"))    # True
print(Solution().palindrome_checker("This is codeintuition"))# False

# Edge cases
print(Solution().palindrome_checker(""))                     # True — empty string
print(Solution().palindrome_checker("a"))                    # True — single char
print(Solution().palindrome_checker("aa"))                   # True — two same chars
print(Solution().palindrome_checker("ab"))                   # False — two diff chars
print(Solution().palindrome_checker("A man, a plan, a canal: Panama"))  # True
print(Solution().palindrome_checker("Was it a car or a cat I saw?"))    # True
```

```java run
public class Main {
    static class Solution {
        public boolean palindromeChecker(String s) {
            if (s.isEmpty()) {

                // An empty string is considered a palindrome
                return true;
            }

            // Initialize two pointers, one pointing to the beginning of the
            // string and the other pointing to the end of the string
            int left = 0;
            int right = s.length() - 1;

            while (left < right) {
                char charLeft = s.charAt(left);
                char charRight = s.charAt(right);

                // Skip non-alphanumeric characters from the left
                if (!Character.isLetterOrDigit(charLeft)) {
                    left++;
                }

                // Skip non-alphanumeric characters from the end
                else if (!Character.isLetterOrDigit(charRight)) {
                    right--;
                }

                // Check if the characters are equal ignoring case
                else if (
                    Character.toLowerCase(charLeft) !=
                    Character.toLowerCase(charRight)
                ) {

                    // Characters are not equal, so it's not a palindrome
                    return false;
                }

                // Move both pointers towards the center
                else {
                    left++;
                    right--;
                }
            }

            // All characters have been checked and are equal, so it's a
            // palindrome
            return true;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().palindromeChecker("a man nam a"));          // true
        System.out.println(new Solution().palindromeChecker("race car rac ecar"));    // true
        System.out.println(new Solution().palindromeChecker("This is codeintuition"));// false

        // Edge cases
        System.out.println(new Solution().palindromeChecker(""));                     // true — empty string
        System.out.println(new Solution().palindromeChecker("a"));                    // true — single char
        System.out.println(new Solution().palindromeChecker("aa"));                   // true — two same chars
        System.out.println(new Solution().palindromeChecker("ab"));                   // false — two diff chars
        System.out.println(new Solution().palindromeChecker("A man, a plan, a canal: Panama")); // true
        System.out.println(new Solution().palindromeChecker("Was it a car or a cat I saw?"));   // true
    }
}
```

### Dry Run — "racecar"

`s = "racecar"`, `n = 7`

| Iteration | `left` | `right` | `s[left]` | `s[right]` | Match? |
|---|---|---|---|---|---|
| 1 | 0 | 6 | `'r'` | `'r'` | ✅ |
| 2 | 1 | 5 | `'a'` | `'a'` | ✅ |
| 3 | 2 | 4 | `'c'` | `'c'` | ✅ |
| — | 3 | 3 | — | — | `left ≥ right` → stop |

**Return `True`** ✓

### Dry Run — "hello"

| Iteration | `left` | `right` | `s[left]` | `s[right]` | Match? |
|---|---|---|---|---|---|
| 1 | 0 | 4 | `'h'` | `'o'` | ❌ → return `False` immediately |

**Return `False`** ✓

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) worst case | Every mirror pair checked once if all match; exits early on first mismatch |
| **Space** | O(1) | Only two pointer variables |

### Edge Cases

| Scenario | Input | Output | Note |
|---|---|---|---|
| Empty string | `""` | `True` | `left = 0 > right = -1` — loop never runs, vacuously true |
| Single character | `"a"` | `True` | `left = right` — loop never runs |
| Two identical chars | `"aa"` | `True` | One comparison, both match |
| Two different chars | `"ab"` | `False` | One comparison, immediate mismatch |
| All same characters | `"aaaa"` | `True` | Every pair matches |

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Palindrome checking is the comparison variant of the two-pointer pattern. Where "Flip Characters" *swapped* mirror pairs, here we *compare* them. The pointer movement is identical — the work in the loop body is the only difference. This is the pattern: same skeleton, swap the operation.

</details>

***

# Vowel Exchange

## The Problem

Given a string `s`, **reverse the vowels** in the string and return the updated string. All non-vowel characters stay in place. The vowels to consider are the English-alphabet ones — `a`, `e`, `i`, `o`, `u`, in both uppercase and lowercase.

```
Input:  s = "random"     →   Output: "rondam"
Input:  s = "afegijoku"  →   Output: "ufogijeka"
Input:  s = "bcdf"       →   Output: "bcdf"
```

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1**
```
Input:  s = "random"
Output: "rondam"
Explanation: The vowels 'a' (index 1) and 'o' (index 2) are swapped.
```

**Example 2**
```
Input:  s = "afegijoku"
Output: "ufogijeka"
Explanation: The vowels are swapped in mirror-pair order:
             - 'a' (index 0) is swapped with 'u' (index 8)
             - 'e' (index 2) is swapped with 'o' (index 6)
             - 'i' (index 4) is its own mirror; it stays in place.
```

**Example 3**
```
Input:  s = "bcdf"
Output: "bcdf"
Explanation: No vowels — the string is unchanged.
```

**Example 4 — all vowels**
```
Input:  s = "aeiou"
Output: "uoiea"
Explanation: Every position is a vowel, so the swap reduces to a full reversal.
```

</details>
<details>
<summary><h2>Intuition</h2></summary>


The classic two-pointer reversal swaps every pair. This problem adds a filter: **only swap when both pointers are sitting on vowels**. When a pointer is sitting on a consonant, just skip it — slide inward until you find the next vowel.

Think of the two pointers as scouts. The left scout hunts for the next vowel from the left; the right scout hunts for the next vowel from the right. When both scouts have found a vowel, they swap and then both advance. If either scout hits the middle first, we're done.

```d3 widget=array-traversal
{
  "items": ["a", "f", "e", "g", "i", "j", "o", "k", "u"],
  "title": "Vowel exchange on \"afegijoku\"",
  "steps": [
    {
      "items":   ["a", "f", "e", "g", "i", "j", "o", "k", "u"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 8, "color": "#f59e0b" }
      ],
      "msg": "Both pointers are on vowels — swap arr[left]='a' with arr[right]='u'."
    },
    {
      "items":   ["u", "f", "e", "g", "i", "j", "o", "k", "a"],
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 7, "color": "#f59e0b" }
      ],
      "msg": "Move inward — arr[left]='f' is a consonant (skip), arr[right]='k' is a consonant (skip)."
    },
    {
      "items":   ["u", "f", "e", "g", "i", "j", "o", "k", "a"],
      "markers": [
        { "name": "left",  "index": 2, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "msg": "Both pointers are on vowels — swap arr[left]='e' with arr[right]='o'."
    },
    {
      "items":   ["u", "f", "o", "g", "i", "j", "e", "k", "a"],
      "markers": [
        { "name": "left",  "index": 3, "color": "#3b82f6" },
        { "name": "right", "index": 5, "color": "#f59e0b" }
      ],
      "msg": "arr[left]='g' is a consonant (skip), arr[right]='j' is a consonant (skip)."
    },
    {
      "items":   ["u", "f", "o", "g", "i", "j", "e", "k", "a"],
      "markers": [
        { "name": "left",  "index": 4, "color": "#3b82f6" },
        { "name": "right", "index": 4, "color": "#f59e0b" }
      ],
      "msg": "Pointers meet at index 4 — the middle 'i' is its own mirror; nothing to swap. Result: \"ufogijeka\"."
    }
  ]
}
```

<p align="center"><strong>Vowel exchange on <code>"afegijoku"</code> — each pointer scans past consonants until it finds a vowel; matched vowel pairs swap; pointers meet at the middle <code>'i'</code>.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Vowel Exchange |
|---|---|
| ✅ Two positions simultaneously? | Yes — `chars[left]` and `chars[right]` are both evaluated, and swapped together once both sit on vowels |
| ✅ One near start, one near end? | Yes — `left = 0`, `right = n-1` |
| ✅ Both move inward? | Yes — when both are on vowels, both advance after the swap; when one sits on a consonant, that side alone advances |
| ✅ Simple work at each step? | Yes — one constant-time check on each pointer, with at most one swap per iteration |

This is a direct application with one variation: not every iteration produces a swap. The loop body uses an `if / elif / else` cascade — if `chars[left]` is a consonant, advance `left`; otherwise if `chars[right]` is a consonant, retreat `right`; otherwise both are vowels and the pair gets swapped. The template is still the same; some iterations just slide a pointer instead of swapping.

**Why advance only one pointer per iteration on a consonant?** Vowels and consonants are interleaved arbitrarily. Each side needs to settle on a vowel before any swap is safe, but the two sides reach their next vowel at different speeds. By advancing one pointer at a time, the `while left < right` guard still controls termination cleanly — there is no inner loop that could run past `right` (or under `left`) and break the invariant. When both pointers happen to land on vowels in the same iteration, the `else` branch swaps them and steps both inward at once.

**What breaks if you use only one pointer?** A single forward pointer can collect vowel positions into a list, but then you need a second pass and a stack (or reverse of that list) to know which vowel to pair each one with. Two pointers eliminate that storage — the right pointer always tracks the vowel that the left's current vowel should swap with. The two-pointer structure implicitly encodes "pair the leftmost unswapped vowel with the rightmost unswapped vowel," which is exactly what reversal of vowels requires.

</details>
<details>
<summary><h2>Approach</h2></summary>


1. Convert the string to a list of characters (strings are immutable in Python)
2. `left = 0`, `right = len - 1`, define `vowels = set("aeiouAEIOU")`
3. While `left < right`:
   - If `chars[left]` is not a vowel, advance `left` and continue
   - Else if `chars[right]` is not a vowel, retreat `right` and continue
   - Else (both pointers are on vowels), swap `chars[left]` and `chars[right]`, then `left++`, `right--`
4. Return `"".join(chars)`

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
class Solution:
    def vowel_exchange(self, s: str) -> str:

        # Create a set to store all the vowels in both uppercase and
        # lowercase
        vowels = set(["a", "e", "i", "o", "u", "A", "E", "I", "O", "U"])

        # Initialize two pointers, one pointing to the beginning of the
        # string and the other pointing to the end of the string
        left: int = 0
        right: int = len(s) - 1

        # Convert the string to an array for easier manipulation
        chars = list(s)

        # Use a while loop to traverse the string using the two pointers
        while left < right:

            # Check if the character pointed by the first pointer is a
            # vowel. If it is not a vowel, move the pointer to the next
            # character
            if chars[left] not in vowels:
                left += 1

            # Check if the character pointed by the second pointer is a
            # vowel. If it is not a vowel, move the pointer to the
            # previous character
            elif chars[right] not in vowels:
                right -= 1

            # If both pointers point to vowels, swap the characters
            else:
                chars[left], chars[right] = chars[right], chars[left]
                left += 1
                right -= 1

        # Convert the array back to a string and return the modified
        # string
        return "".join(chars)


# Examples from the problem statement
print(Solution().vowel_exchange("random"))    # rondam
print(Solution().vowel_exchange("afegijoku")) # ufogijeka
print(Solution().vowel_exchange("bcdf"))      # bcdf

# Edge cases
print(Solution().vowel_exchange(""))          # ""
print(Solution().vowel_exchange("a"))         # a — single vowel, no swap
print(Solution().vowel_exchange("b"))         # b — single consonant
print(Solution().vowel_exchange("aeiou"))     # uoiea — all vowels reversed
print(Solution().vowel_exchange("AEIou"))     # uoIEA — mixed case vowels reversed
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        public String vowelExchange(String s) {

            // Create a hash set to store all the vowels in both uppercase
            // and lowercase
            HashSet<Character> vowels = new HashSet<Character>();
            vowels.add('a');
            vowels.add('e');
            vowels.add('i');
            vowels.add('o');
            vowels.add('u');
            vowels.add('A');
            vowels.add('E');
            vowels.add('I');
            vowels.add('O');
            vowels.add('U');

            // Initialize two pointers, one pointing to the beginning of the
            // string and the other pointing to the end of the string
            int left = 0;
            int right = s.length() - 1;

            // Convert the string to a character array for easier
            // manipulation
            char[] chars = s.toCharArray();

            // Use a while loop to traverse the string using the two pointers
            while (left < right) {

                // Check if the character pointed by the first pointer is a
                // vowel If it is not a vowel, move the pointer to the next
                // character
                if (!vowels.contains(chars[left])) {
                    left++;
                }

                // Check if the character pointed by the second pointer is a
                // vowel If it is not a vowel, move the pointer to the
                // previous character
                else if (!vowels.contains(chars[right])) {
                    right--;
                }

                // If both pointers point to vowels, swap the characters
                else {
                    char temp = chars[left];
                    chars[left] = chars[right];
                    chars[right] = temp;
                    left++;
                    right--;
                }
            }

            // Convert the character array back to a string and return the
            // modified string
            return new String(chars);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().vowelExchange("random"));    // rondam
        System.out.println(new Solution().vowelExchange("afegijoku")); // ufogijeka
        System.out.println(new Solution().vowelExchange("bcdf"));      // bcdf

        // Edge cases
        System.out.println(new Solution().vowelExchange(""));          // ""
        System.out.println(new Solution().vowelExchange("a"));         // a — single vowel, no swap
        System.out.println(new Solution().vowelExchange("b"));         // b — single consonant
        System.out.println(new Solution().vowelExchange("aeiou"));     // uoiea — all vowels reversed
        System.out.println(new Solution().vowelExchange("AEIou"));     // uoIEA — mixed case vowels reversed
    }
}
```

### Dry Run — "afegijoku"

`s = "afegijoku"`, `n = 9`. Vowels live at indices `0 (a)`, `2 (e)`, `4 (i)`, `6 (o)`, `8 (u)`.

| Iteration | `left` | `right` | Branch taken | Action | String |
|---|---|---|---|---|---|
| 1 | 0 (`a`, vowel) | 8 (`u`, vowel) | `else` (both vowels) | swap `a ↔ u`, then `left++`, `right--` | `"ufegijoka"` |
| 2 | 1 (`f`, consonant) | 7 (`k`, consonant) | `if` (left consonant) | `left++` | `"ufegijoka"` |
| 3 | 2 (`e`, vowel) | 7 (`k`, consonant) | `elif` (right consonant) | `right--` | `"ufegijoka"` |
| 4 | 2 (`e`, vowel) | 6 (`o`, vowel) | `else` (both vowels) | swap `e ↔ o`, then `left++`, `right--` | `"ufogijeka"` |
| 5 | 3 (`g`, consonant) | 5 (`j`, consonant) | `if` (left consonant) | `left++` | `"ufogijeka"` |
| 6 | 4 (`i`, vowel) | 5 (`j`, consonant) | `elif` (right consonant) | `right--` | `"ufogijeka"` |
| — | 4 | 4 | — | `left ≥ right` — stop | `"ufogijeka"` ✓ |

Each iteration takes exactly one branch of the cascade — either it slides one pointer past a consonant, or both pointers sit on vowels and the pair gets swapped. The middle character at index 4 (`'i'`) is its own mirror, so no swap touches it.

**Return `"ufogijeka"`** ✓

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Each character is visited at most once by each pointer — total work is O(n) |
| **Space** | O(n) | The `chars` list copy of the input string |

> If the input were a mutable character array (as in C++/Java), space would drop to O(1). In Python we need the list copy because strings are immutable.

### Edge Cases

| Scenario | Input | Output | Note |
|---|---|---|---|
| No vowels | `"bcdf"` | `"bcdf"` | Pointers never stop to swap |
| All vowels | `"aeiou"` | `"uoiea"` | Every step swaps |
| Single character | `"a"` | `"a"` | Loop never runs |
| Already reversed vowels | `"uoiea"` | `"aeiou"` | Swap brings original back |
| Mixed case | `"hEllo"` | `"hollE"` | Uppercase vowels counted too |

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Vowel Exchange introduces a new wrinkle: **not every position deserves a swap**. The `if / elif / else` cascade inside the outer `while` loop acts as a scanner — when a pointer sits on a non-vowel, only that pointer advances; only when both pointers sit on vowels do they swap and step inward together. This "slide past non-candidates, then act" pattern appears in many two-pointer problems where only a subset of elements are candidates for the operation.

</details>

***

# Reverse Words

## The Problem

Given a string `s`, reverse the characters of every word **while preserving the original word order** and the original whitespace exactly. The string may contain leading or trailing spaces, and words may be separated by more than a single space — every space stays where it was; only the letters inside each word are flipped.

```
Input:  s = "This is a string"
Output:     "sihT si a gnirts"
```

The words stay in place — only the characters inside each word are reversed.

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1**
```
Input:  s = "This is a string"
Output:     "sihT si a gnirts"
Explanation: All four words are reversed; spaces are preserved.
```

**Example 2 — multiple spaces between words**
```
Input:  s = "I  love  coding"
Output:     "I  evol  gnidoc"
Explanation: Words separated by more than one space — the double spaces stay
             intact; only the words' characters reverse.
```

**Example 3 — single word**
```
Input:  s = "random"
Output:     "modnar"
Explanation: The string contains one word; it is reversed.
```

**Example 4 — single-character words**
```
Input:  s = "a b c"
Output:     "a b c"
Explanation: Reversing a single character is a no-op.
```

</details>
<details>
<summary><h2>Intuition</h2></summary>


You already know how to reverse a contiguous block of characters with two pointers — that's exactly what "Flip Characters" did. This problem asks you to apply that same operation **multiple times**, once per word.

The key insight: **spaces act as word boundaries**. Walk through the array character by character. When you find the start of a word, scan forward to find its end (the next space or the array boundary). Now you have a `[word_start, word_end]` range — apply the two-pointer reversal to that range. Then continue scanning for the next word.

```d3 widget=array-traversal
{
  "items": ["t", "h", "e", " ", "s", "k", "y"],
  "title": "Reverse each word in \"the sky\"",
  "steps": [
    {
      "items":   ["t", "h", "e", " ", "s", "k", "y"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 2, "color": "#f59e0b" }
      ],
      "range":   { "lo": 0, "hi": 2 },
      "msg": "Scan finds the first word at indices [0..2]. Reverse it: swap arr[0]='t' with arr[2]='e'."
    },
    {
      "items":   ["e", "h", "t", " ", "s", "k", "y"],
      "markers": [
        { "name": "left",  "index": 1, "color": "#3b82f6" },
        { "name": "right", "index": 1, "color": "#f59e0b" }
      ],
      "range":   { "lo": 0, "hi": 2 },
      "msg": "Inside word 1, the pointers meet at index 1; the middle 'h' is its own mirror."
    },
    {
      "items":   ["e", "h", "t", " ", "s", "k", "y"],
      "markers": [
        { "name": "left",  "index": 4, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "range":   { "lo": 4, "hi": 6 },
      "msg": "Scan skips the space at index 3, then finds the second word at indices [4..6]. Reverse it: swap arr[4]='s' with arr[6]='y'."
    },
    {
      "items":   ["e", "h", "t", " ", "y", "k", "s"],
      "markers": [
        { "name": "left",  "index": 5, "color": "#3b82f6" },
        { "name": "right", "index": 5, "color": "#f59e0b" }
      ],
      "range":   { "lo": 4, "hi": 6 },
      "msg": "Inside word 2, the pointers meet at index 5. Result: \"eht yks\"."
    }
  ]
}
```

<p align="center"><strong>Reverse Words on <code>"the sky"</code> — the outer scan finds each word's boundaries (highlighted band); two pointers then reverse the characters inside that range.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Reverse Words |
|---|---|
| ✅ Two positions simultaneously? | Yes — inside each word's reversal, `chars[left]` and `chars[right]` are swapped together |
| ✅ One near start, one near end? | Yes — for each word, `left = word_start`, `right = word_end` |
| ✅ Both move inward? | Yes — `left++`, `right--` within each word's reversal loop |
| ✅ Simple work at each step? | Yes — one swap per pair within the word |

The outer scan that finds word boundaries is bookkeeping — once a `[word_start, word_end]` range is identified, the inner two-pointer reversal is a textbook direct application on that sub-range.

**Why find word boundaries with a linear scan instead of outer two pointers?** This problem operates on each word independently, not on a single pair of positions across the whole string. The outer scan moves linearly left-to-right, identifying the next word. For each word found, two inner pointers cover it from start to end. Trying to maintain two outer pointers across the full string wouldn't give word-by-word control — you'd lose the ability to identify where each word's characters start and end.

**What connects this to the direct-application pattern?** The `reverse(chars, l, r)` helper is a pure direct application. The outer scan is word-boundary discovery. Reverse Words decomposes as: discover word boundary → directly apply two-pointer reversal on that range → repeat. Composing multiple direct applications, each on a different sub-range, is still the direct-application pattern — the same four checks hold for every inner reversal call.

</details>
<details>
<summary><h2>Approach</h2></summary>


1. Convert the string to a mutable character list (if needed)
2. Use an outer pointer `i` to scan from left to right
3. For each position `i`:
   - If `chars[i]` is not a space, it's the **start of a word** — record `word_start = i`
   - Advance `i` until you hit a space or the end of the array — `i - 1` is `word_end`
   - Apply two-pointer reversal on `chars[word_start : word_end]`
4. Return the joined result

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
class Solution:
    def find_word_end(self, arr, start):

        # Assign the start index to the end index
        end = start

        # Iterate through the string until a space is encountered
        while end < len(arr) and arr[end] != " ":
            end += 1

        # Return the index of the last character of the word
        return end - 1

    def reverse_word(self, arr, left, right):

        # Use a while loop to traverse the string using the two pointers
        while left < right:

            # Swap the characters pointed by the left and right pointers
            arr[left], arr[right] = arr[right], arr[left]

            # Move the pointers towards the center of the string
            left += 1
            right -= 1

    def reverse_words(self, s: str) -> str:
        arr = list(s)
        start = 0

        # Iterate through the string
        while start < len(arr):

            # Skip any leading spaces
            if arr[start] == " ":
                start = start + 1
                continue

            # Find the end of the current word
            end = self.find_word_end(arr, start)

            # Reverse the characters in the current word using two
            # pointer method
            self.reverse_word(arr, start, end)

            # Move the start pointer to the next word
            start = end + 1

        return "".join(arr)


# Examples from the problem statement
print(Solution().reverse_words("This is a string"))  # sihT si a gnirts
print(Solution().reverse_words("I love coding"))     # I evol gnidoc
print(Solution().reverse_words("random"))            # modnar

# Edge cases
print(Solution().reverse_words(""))                  # ""
print(Solution().reverse_words("a"))                 # a — single char word
print(Solution().reverse_words("ab"))                # ba — two char word
print(Solution().reverse_words("  hello  world  ")) # "  olleh  dlrow  " — spaces preserved
print(Solution().reverse_words("aa bb cc"))          # aa bb cc — palindrome words unchanged
```

```java run
public class Main {
    static class Solution {
        private int findWordEnd(char[] arr, int start) {

            // Assign the start index to the end index
            int end = start;

            // Iterate through the string until a space is encountered
            while (end < arr.length && arr[end] != ' ') {
                end++;
            }

            // Return the index of the last character of the word
            return end - 1;
        }

        private void reverseWord(char[] arr, int left, int right) {

            // Use a while loop to traverse the string using the two pointers
            while (left < right) {

                // Swap the characters pointed by the left and right pointers
                char temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;

                // Move the pointers towards the center of the string
                left++;
                right--;
            }
        }

        public String reverseWords(String s) {
            char[] arr = s.toCharArray();
            int start = 0;

            // Iterate through the string
            while (start < s.length()) {

                // Skip any leading spaces
                if (arr[start] == ' ') {
                    start++;
                    continue;
                }

                // Find the end of the current word
                int end = findWordEnd(arr, start);

                // Reverse the characters in the current word using two
                // pointer method
                reverseWord(arr, start, end);

                // Move the start pointer to the next word
                start = end + 1;
            }

            return new String(arr);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseWords("This is a string")); // sihT si a gnirts
        System.out.println(new Solution().reverseWords("I love coding"));    // I evol gnidoc
        System.out.println(new Solution().reverseWords("random"));           // modnar

        // Edge cases
        System.out.println(new Solution().reverseWords(""));                 // ""
        System.out.println(new Solution().reverseWords("a"));                // a — single char word
        System.out.println(new Solution().reverseWords("ab"));               // ba — two char word
        System.out.println(new Solution().reverseWords("  hello  world  ")); // "  olleh  dlrow  " — spaces preserved
        System.out.println(new Solution().reverseWords("aa bb cc"));         // aa bb cc — palindrome words unchanged
    }
}
```

### Dry Run — "the sky"

`arr = ['t','h','e',' ','s','k','y']`, `n = 7`

**Word 1:** outer scan finds non-space at `start = 0`; `findWordEnd` returns `end = 2`.

| Step | `left` | `right` | Swap | Array |
|---|---|---|---|---|
| 1 | 0 | 2 | `t ↔ e` | `['e','h','t',' ','s','k','y']` |
| — | 1 | 1 | `left ≥ right` — stop | — |

`start` advances to `end + 1 = 3`. The space at index 3 is skipped; `start = 4`.

**Word 2:** `findWordEnd` returns `end = 6`.

| Step | `left` | `right` | Swap | Array |
|---|---|---|---|---|
| 1 | 4 | 6 | `s ↔ y` | `['e','h','t',' ','y','k','s']` |
| — | 5 | 5 | `left ≥ right` — stop | — |

`start` advances to `end + 1 = 7 = n` — outer loop exits.

**Return `"eht yks"`** ✓

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Each character is visited at most twice — once by the outer scan, once by the inner reversal |
| **Space** | O(n) | The `chars` list (O(1) if the input were already mutable) |

### Edge Cases

| Scenario | Input | Output | Note |
|---|---|---|---|
| Empty string | `""` | `""` | Outer loop never runs |
| Single word | `"hello"` | `"olleh"` | One reversal |
| All spaces | `"   "` | `"   "` | Every char is a space — no reversals |
| Leading/trailing spaces | `" hi "` | `" ih "` | Spaces skipped; only `"hi"` reversed |
| Single-char words | `"a b"` | `"a b"` | Reversing one character is a no-op |

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Reverse Words composes two ideas: **scanning to find word boundaries** and **two-pointer reversal within a range**. The outer loop handles discovery; the inner two pointers handle the work. This composition — "find a range, then operate on it with two pointers" — is a pattern that recurs throughout the two-pointer family of problems.

</details>

***

# Reverse Segments

## The Problem

Given a string `s` and an integer `k`, process the string in groups of `2k` characters and reverse the **first `k` characters of every group**. Return the updated string.

Two rules cover the tail of the string when a full `2k` group doesn't fit:

- If fewer than `k` characters remain, reverse all of them.
- If at least `k` but fewer than `2k` characters remain, reverse only the first `k` and leave the rest unchanged.

```
Input:  s = "abcdefghij",  k = 2
Output: "bacdfeghji"

  groups of 2k = 4:    abcd      efgh      ij
  reverse first k = 2: [ba]cd    [fe]gh    [ji]     (last group: only k chars, reverse all)
  result:              bacdfeghji
```

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1 — full groups plus a trailing chunk of exactly `k`**
```
Input:  s = "abcdefghij",  k = 2
Output: "bacdfeghji"
```
- The first 2k characters are `abcd`; reverse the first k: `ab` → `ba`.
- The next 2k characters are `efgh`; reverse the first k: `ef` → `fe`.
- `ij` is left — `k` characters, fewer than `2k` — so reverse the first k: `ij` → `ji`.

**Example 2 — fewer than `k` characters remain**
```
Input:  s = "dfgh",  k = 5
Output: "hgfd"
```
- There are fewer than `k` characters left, so reverse all of them.

**Example 3 — exactly `2k` characters, second half untouched**
```
Input:  s = "qwerty",  k = 3
Output: "ewqrty"
```
- The first k characters are reversed: `qwe` → `ewq`.
- The remaining characters (`rty`) are left unchanged.

**Example 4 — trailing chunk between `k` and `2k`**
```
Input:  s = "abcdefg",  k = 2
Output: "bacdfeg"
```
- Groups are `abcd` and `efg`. In `abcd`, reverse `ab` → `ba`. `efg` has 3 characters (≥ `k`, < `2k`), so reverse the first `k` — `ef` → `fe` — and leave `g`.

</details>
<details>
<summary><h2>Intuition</h2></summary>


You already have the exact tool for this: the two-pointer reversal from "Flip Characters". Reversing the first `k` characters of a block is just that reversal applied to a sub-range — start `left` at the block's first index and `right` `k - 1` positions later.

The whole problem is then a simple outer loop: jump through the string in strides of `2k`, and at each landing point reverse one `k`-wide window. Two details make the tail rules disappear:

- **Stride by `2k`, not `k`.** Only the first `k` of each block is touched; the second `k` is skipped. Stepping `start` by `2k` lands directly on each window and steps *over* the untouched halves — so "leave the rest unchanged" needs no code, those indices are simply never visited.
- **Clamp `right` with `min`.** Set `right = min(start + k - 1, n - 1)`. When a full `k`-window fits, this is `start + k - 1`. When fewer than `k` characters remain, `start + k - 1` runs off the end and `min` pulls it back to the last index — so the "reverse whatever's left" rule is handled by arithmetic, not a branch.

```d2
direction: down

ORIG: "Original — s = abcdefghij, k = 2  (block size 2k = 4)" {
  grid-columns: 10
  grid-gap: 0
  a: "a"
  b: "b"
  c: "c"
  d: "d"
  e: "e"
  f: "f"
  g: "g"
  h: "h"
  i: "i"
  j: "j"
}
ORIG.a.style.fill: "#fde68a"
ORIG.b.style.fill: "#fde68a"
ORIG.e.style.fill: "#fde68a"
ORIG.f.style.fill: "#fde68a"
ORIG.i.style.fill: "#fde68a"
ORIG.j.style.fill: "#fde68a"

RESULT: "Result — first k of every 2k block reversed" {
  grid-columns: 10
  grid-gap: 0
  a: "b"
  b: "a"
  c: "c"
  d: "d"
  e: "f"
  f: "e"
  g: "g"
  h: "h"
  i: "j"
  j: "i"
}
RESULT.a.style.fill: "#dcfce7"
RESULT.b.style.fill: "#dcfce7"
RESULT.e.style.fill: "#dcfce7"
RESULT.f.style.fill: "#dcfce7"
RESULT.i.style.fill: "#dcfce7"
RESULT.j.style.fill: "#dcfce7"

ORIG -> RESULT: "reverse first k=2 of every 2k=4 block (last block has only k chars left)"
```

<p align="center"><strong>Reversing the first <code>k</code> of every <code>2k</code> block in <code>abcdefghij</code> — the highlighted cells are the windows that get reversed; the gaps (<code>cd</code>, <code>gh</code>) are stepped over entirely.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Reverse Segments |
|---|---|
| ✅ Two positions simultaneously? | Yes — within each `k`-window, `arr[left]` and `arr[right]` are swapped together |
| ✅ One near start, one near end? | Yes — for each window, `left = start` and `right = min(start + k - 1, n - 1)` |
| ✅ Both move inward? | Yes — `left++`, `right--` within each window's reversal |
| ✅ Simple work at each step? | Yes — one swap per iteration |

Reverse Segments is structurally identical to Flip Characters — the only difference is that `left` and `right` start from a computed window `(start, start + k - 1)` instead of `(0, n-1)`. The two-pointer pattern is unchanged; the `2k` stride and the `min` clamp just decide *which* sub-ranges to feed it.

**Why is this still "direct application" and not something more complex?** The reversal inside each window is the unmodified swap-and-converge loop. The only thing wrapped around it is a `for` loop that picks window start positions by counting in `2k` strides. There's no data transformation, no searching for a range, no condition-based pointer movement inside the window. Two pointers enter a window, march toward each other, and exit.

**Why doesn't the "leave the rest unchanged" rule need any code?** Because the outer loop strides by `2k` and each reversal only touches `start .. start + k - 1`. The second half of every block — indices `start + k .. start + 2k - 1` — is never an endpoint and never swapped. The rule is satisfied by *omission*: those indices are simply skipped, so they keep their original values for free.

</details>
<details>
<summary><h2>Approach</h2></summary>


1. Convert `s` to a mutable character array `arr` (strings are immutable in most languages).
2. For each block start `start` = `0, 2k, 4k, …` while `start < n`:
   - Set `left = start` and `right = min(start + k - 1, n - 1)` — the `min` clamps the short tail.
   - While `left < right`: swap `arr[left]` and `arr[right]`, `left++`, `right--`.
3. Join `arr` back into a string and return it.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
from typing import List

class Solution:
    def reverse_segment(
        self, arr: List[str], left: int, right: int
    ) -> None:

        # Use a while loop to traverse the string using the two pointers
        while left < right:

            # Swap the characters pointed by the left and right pointers
            arr[left], arr[right] = arr[right], arr[left]

            # Move the pointers towards the center of the string
            left += 1
            right -= 1

    def reverse_segments(self, s: str, k: int) -> str:

        # convert the string to list for in-place modification
        arr = list(s)
        n = len(arr)

        for start in range(0, n, 2 * k):

            # Initialize left and right pointers to the current segment
            left = start
            right = min(start + k - 1, n - 1)

            # Reverse the segment using the two-pointer method
            self.reverse_segment(arr, left, right)

        # convert the list back to string
        return "".join(arr)


# Examples from the problem statement
print(Solution().reverse_segments("abcdefghij", 2))  # bacdfeghji
print(Solution().reverse_segments("dfgh", 5))         # hgfd
print(Solution().reverse_segments("qwerty", 3))       # ewqrty

# Edge cases
print(Solution().reverse_segments("", 2))             # ""
print(Solution().reverse_segments("a", 1))            # a — single char
print(Solution().reverse_segments("ab", 1))           # ab — k=1 reverses nothing visible
print(Solution().reverse_segments("abcdef", 6))       # fedcba — k >= len reverses all
print(Solution().reverse_segments("abcdef", 1))       # abcdef — k=1, each group of 1 reversed = unchanged
```

```java run
public class Main {
    static class Solution {
        private void reverseSegment(char[] arr, int left, int right) {

            // Use a while loop to traverse the string using the two pointers
            while (left < right) {

                // Swap the characters pointed by the left and right pointers
                char temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;

                // Move the pointers towards the center of the string
                left++;
                right--;
            }
        }

        public String reverseSegments(String s, int k) {
            char[] arr = s.toCharArray();
            int n = arr.length;
            for (int start = 0; start < n; start += 2 * k) {

                // Initialize left and right pointers to the current segment
                int left = start;
                int right = Math.min(start + k - 1, n - 1);

                // Reverse the segment using the two-pointer method
                reverseSegment(arr, left, right);
            }
            return new String(arr);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseSegments("abcdefghij", 2)); // bacdfeghji
        System.out.println(new Solution().reverseSegments("dfgh", 5));        // hgfd
        System.out.println(new Solution().reverseSegments("qwerty", 3));      // ewqrty

        // Edge cases
        System.out.println(new Solution().reverseSegments("", 2));            // ""
        System.out.println(new Solution().reverseSegments("a", 1));           // a — single char
        System.out.println(new Solution().reverseSegments("ab", 1));          // ab — k=1 reverses nothing visible
        System.out.println(new Solution().reverseSegments("abcdef", 6));      // fedcba — k >= len reverses all
        System.out.println(new Solution().reverseSegments("abcdef", 1));      // abcdef — k=1, unchanged
    }
}
```

### Dry Run — Example 1

`s = "abcdefghij"`, `k = 2` → `n = 10`, stride `2k = 4`. Block starts: `0, 4, 8`.

**Block start = 0:** `left = 0`, `right = min(0 + 1, 9) = 1`

| Step | `left` | `right` | Swap | Array |
|---|---|---|---|---|
| 1 | 0 | 1 | `a ↔ b` | `bacdefghij` |
| — | 1 | 0 | stop | — |

**Block start = 4:** `left = 4`, `right = min(4 + 1, 9) = 5`

| Step | `left` | `right` | Swap | Array |
|---|---|---|---|---|
| 1 | 4 | 5 | `e ↔ f` | `bacdfeghij` |
| — | 5 | 4 | stop | — |

**Block start = 8:** `left = 8`, `right = min(8 + 1, 9) = 9` — only `k` characters remain; the clamp is a no-op here

| Step | `left` | `right` | Swap | Array |
|---|---|---|---|---|
| 1 | 8 | 9 | `i ↔ j` | `bacdfeghji` |
| — | 9 | 8 | stop | — |

**Result: `"bacdfeghji"`** ✓

### Complexity Analysis

Let `n` = the length of the string.

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Every index is an endpoint of at most one swap; the skipped second-halves aren't visited at all. Building the character array and joining it back are also O(n). |
| **Space** | O(n) | The mutable character array is a copy of the string — unavoidable because strings are immutable (in Python, Java, Scala). The two-pointer reversal itself adds only O(1) on top of that copy. |

### Edge Cases

| Scenario | Input | Effect |
|---|---|---|
| `k = 1` | any `s` | Every window is a single character — `left = right`, the loop never runs; the string returns unchanged |
| `k ≥ n` | `s = "dfgh", k = 5` | One block; `right` clamps to `n - 1`; the whole string is reversed |
| Last block `< k` chars | trailing chunk | `min` clamps `right`; the short tail is fully reversed |
| Last block `k`–`2k` chars | trailing chunk | First `k` reversed; the rest is never visited, left as-is |
| Empty string | `s = ""` | `n = 0`; the outer loop never runs; returns `""` |
| `k = 0` | — | Out of scope — the problem constraints assume `k ≥ 1` (a `2k = 0` stride would not advance) |

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Reverse Segments shows that the two-pointer reversal is a **reusable utility** — not just a technique for one specific problem. By extracting it into `reverse_segment(arr, left, right)`, you get a building block that can be aimed at any sub-range. The new idea here is letting *index arithmetic* carry the irregular requirements: a `2k` stride skips the untouched halves, and a `min` clamp folds two separate tail rules into one expression — no branching needed. The next problem, Reverse Word Order, composes sub-range reversals the same way to flip word order while keeping each word intact.

</details>

***

# Reverse Word Order

## The Problem

Given a string `s`, return a new string with the **words in reverse order**, separated by a single space, with no leading or trailing whitespace. Words in the input are separated by **one or more** spaces, and the input may contain leading, trailing, or multiple spaces between words — all of which must be normalised in the output.

```
Input:  s = "This is a    string"
Output:     "string a is This"
```

The words flip order; each word's characters stay intact; redundant whitespace disappears.

---

<details>
<summary><h2>Examples</h2></summary>


**Example 1 — multiple spaces inside the string**
```
Input:  s = "This is a    string"
Output:     "string a is This"
Explanation: All four words are concatenated in reverse order and separated
             by a single space.
```

**Example 2 — leading and trailing spaces**
```
Input:  s = "   fizz buzz  "
Output:     "buzz fizz"
Explanation: Leading and trailing spaces are removed.
```

**Example 3 — single word**
```
Input:  s = "random"
Output:     "random"
Explanation: Only one word; the result is the input.
```

**Example 4**
```
Input:  s = "a good example"
Output:     "example good a"
```

</details>
<details>
<summary><h2>Intuition</h2></summary>


This is the hardest problem in the section, and it has a beautiful two-step trick.

If you reverse the entire string first, the words appear in reverse order — but each word's own characters are also reversed. So in step two, you reverse each word's characters back to normal. The two operations cancel each other out for the characters inside words, but compound their effect on word order.

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
  ORIG["Original string<br/>'the sky is blue'"]
  STEP1["Step 1: Reverse the entire string<br/>'eulb si yks eht'"]
  STEP2["Step 2: Reverse each individual word<br/>'blue is sky the'"]
  DONE(["✓  Word order reversed, characters intact"])

  ORIG -->|"two-pointer reverse full string"| STEP1
  STEP1 -->|"two-pointer reverse each word"| STEP2
  STEP2 --> DONE
```

<p align="center"><strong>The two-step trick — reversing the whole string flips word order but scrambles each word; reversing each word individually unscrambles the characters while keeping the new word order.</strong></p>

</details>
<details>
<summary><h2>Why This Works — The Intuition</h2></summary>


Let's trace exactly what each step does to `"the sky"`:

```d3 widget=array-traversal
{
  "items": ["t", "h", "e", " ", "s", "k", "y"],
  "title": "Reversing word order in \"the sky\"",
  "steps": [
    {
      "items":   ["t", "h", "e", " ", "s", "k", "y"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "range":   { "lo": 0, "hi": 6 },
      "msg": "Step 1: reverse the entire string. Pointers start at the two ends."
    },
    {
      "items":   ["y", "k", "s", " ", "e", "h", "t"],
      "markers": [],
      "range":   { "lo": 0, "hi": 6 },
      "msg": "After Step 1: the string is reversed to 'yks eht'. Word order is flipped; each word's letters are also flipped."
    },
    {
      "items":   ["y", "k", "s", " ", "e", "h", "t"],
      "markers": [
        { "name": "left",  "index": 0, "color": "#3b82f6" },
        { "name": "right", "index": 2, "color": "#f59e0b" }
      ],
      "range":   { "lo": 0, "hi": 2 },
      "msg": "Step 2a: scan finds the first word at indices [0..2]. Reverse it."
    },
    {
      "items":   ["s", "k", "y", " ", "e", "h", "t"],
      "markers": [
        { "name": "left",  "index": 4, "color": "#3b82f6" },
        { "name": "right", "index": 6, "color": "#f59e0b" }
      ],
      "range":   { "lo": 4, "hi": 6 },
      "msg": "Step 2b: scan finds the second word at indices [4..6]. Reverse it."
    },
    {
      "items":   ["s", "k", "y", " ", "t", "h", "e"],
      "markers": [],
      "msg": "Final: 'sky the' — words are in reverse order, characters intact."
    }
  ]
}
```

<p align="center"><strong>Step-by-step on <code>"the sky"</code> — Step 1 reverses the whole string (flipping word order but scrambling each word); Step 2 reverses each word, unscrambling letters while keeping the new word order.</strong></p>

Each word is reversed **twice** in total — once by the full-string reversal, once by the per-word reversal. Two reversals cancel out, returning each word's characters to their original order. But the words themselves have moved to their new positions.

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| Check | Answer for Reverse Word Order |
|---|---|
| ✅ Two positions simultaneously? | Yes — in both Step 1 and Step 2, `chars[left]` and `chars[right]` are swapped together |
| ✅ One near start, one near end? | Yes — Step 1: `left=0`, `right=n-1`; Step 2: per word, `left=word_start`, `right=word_end` |
| ✅ Both move inward? | Yes — `left++`, `right--` in both reversal passes |
| ✅ Simple work at each step? | Yes — one swap per iteration in each pass |

This is a **composed** direct application: two separate two-pointer passes applied in sequence. Step 1 (full reverse) is Flip Characters on the whole string. Step 2 (per-word reverse) is Reverse Words from the previous lesson. Each pass passes all four checks independently.

**Why does composing two reversals give word-order reversal?** Because reversing is its own inverse: reverse a sequence twice and you get back the original. For each word, the full-string reversal scrambles its characters, and the per-word reversal unscrambles them — net effect on the word's characters: zero. But the word's **position** in the string only experiences the full-string reversal (the per-word reversal doesn't change inter-word positions, only intra-word order). So the characters come out intact, but the word slots have been rearranged. See the "Why This Works" section above for the full concrete trace.

**What breaks if you only do Step 1?** After `reverse(0, n-1)`, words are in reverse order — correct — but every word's characters are also reversed — wrong. `"the sky"` → `"yks eht"` instead of `"sky the"`. Step 2 is what restores each word's internal character order without disturbing the newly achieved word-order reversal.

</details>
<details>
<summary><h2>Approach</h2></summary>


1. Convert string to a mutable character list
2. **Step 1:** Reverse the entire character array with two pointers (`left=0`, `right=n-1`)
3. **Step 2:** Scan through the array; for each word found at range `[l, r]`, reverse `chars[l..r]` with two pointers
4. Return `"".join(chars)`

This reuses `reverse_segment(arr, left, right)` from the previous lesson twice.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Solution

```python run
class Solution:
    def remove_extra_spaces(self, s: str) -> str:
        return " ".join(s.split()).strip()

    def find_word_end(self, arr, start):

        # Assign the start index to the end index
        end = start

        # Iterate through the string until a space is encountered
        while end < len(arr) and arr[end] != " ":
            end += 1

        # Return the index of the last character of the word
        return end - 1

    def reverse_word(self, arr, left, right):

        # Use a while loop to traverse the string using the two pointers
        while left < right:

            # Swap the characters pointed by the left and right pointers
            arr[left], arr[right] = arr[right], arr[left]

            # Move the pointers towards the center of the string
            left += 1
            right -= 1

    def reverse_word_order(self, s: str) -> str:

        # Reverse the string
        s = s[::-1]

        # Convert string to list of characters
        arr = list(s)

        start = 0
        while start < len(arr):
            if arr[start] == " ":
                start += 1
                continue

            # Find the end of the current word
            end = self.find_word_end(arr, start)

            # Reverse the current word using two pointer method
            self.reverse_word(arr, start, end)

            # Move start to the start of the next word
            start = end + 1

        # Convert list to string and remove extra spaces
        return self.remove_extra_spaces("".join(arr))


# Examples from the problem statement
print(Solution().reverse_word_order("This is a    string"))  # string a is This
print(Solution().reverse_word_order("   fizz buzz  "))       # buzz fizz
print(Solution().reverse_word_order("random"))               # random

# Edge cases
print(Solution().reverse_word_order(""))                     # ""
print(Solution().reverse_word_order("a"))                    # a — single word
print(Solution().reverse_word_order("  a  b  "))             # b a — extra spaces stripped
print(Solution().reverse_word_order("one two three"))        # three two one
print(Solution().reverse_word_order("hello world"))          # world hello
```

```java run
public class Main {
    static class Solution {
        private String removeExtraSpaces(String s) {

            // Use regex to replace multiple spaces with a single space and
            // trim leading/trailing spaces
            return s.replaceAll("\\s+", " ").trim();
        }

        private String reverse(String s) {

            // Use StringBuilder to reverse the string
            StringBuilder sb = new StringBuilder(s);

            // Reverse the string
            sb.reverse();

            // Return the reversed string
            return sb.toString();
        }

        private int findWordEnd(char[] arr, int start) {

            // Assign the start index to the end index
            int end = start;

            // Iterate through the string until a space is encountered
            while (end < arr.length && arr[end] != ' ') {
                end++;
            }

            // Return the index of the last character of the word
            return end - 1;
        }

        private void reverseWord(char[] arr, int left, int right) {

            // Use a while loop to traverse the string using the two pointers
            while (left < right) {

                // Swap the characters pointed by the left and right pointers
                char temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;

                // Move the pointers towards the center of the string
                left++;
                right--;
            }
        }

        public String reverseWordOrder(String s) {

            // Reverse the string
            s = reverse(s);

            // Convert string to array of characters
            char[] arr = s.toCharArray();

            int start = 0;
            while (start < arr.length) {
                if (arr[start] == ' ') {
                    start++;
                    continue;
                }

                // Find the end of the current word
                int end = findWordEnd(arr, start);

                // Reverse the current word using two pointer method
                reverseWord(arr, start, end);

                // Move start to the start of the next word
                start = end + 1;
            }

            // Convert char array to string and remove extra spaces using
            // regex
            return removeExtraSpaces(new String(arr));
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseWordOrder("This is a    string")); // string a is This
        System.out.println(new Solution().reverseWordOrder("   fizz buzz  "));      // buzz fizz
        System.out.println(new Solution().reverseWordOrder("random"));              // random

        // Edge cases
        System.out.println(new Solution().reverseWordOrder(""));                    // ""
        System.out.println(new Solution().reverseWordOrder("a"));                   // a — single word
        System.out.println(new Solution().reverseWordOrder("  a  b  "));            // b a — extra spaces stripped
        System.out.println(new Solution().reverseWordOrder("one two three"));       // three two one
        System.out.println(new Solution().reverseWordOrder("hello world"));         // world hello
    }
}
```

### Dry Run — "the sky is blue"

**Step 1: Reverse entire string**

```
"the sky is blue"
       ↓
"eulb si yks eht"
```

(Characters are in exact reverse order — words are backwards, characters within each word are backwards)

**Step 2: Reverse each word**

| Word found | Range | Before | After |
|---|---|---|---|
| `"eulb"` | [0, 3] | `eulb` | `blue` |
| `"si"` | [5, 6] | `si` | `is` |
| `"yks"` | [8, 10] | `yks` | `sky` |
| `"eht"` | [12, 14] | `eht` | `the` |

**Final result: `"blue is sky the"`** ✓

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Step 1 visits every character once (O(n)); Step 2 visits every character once more (O(n)); total = O(2n) = O(n) |
| **Space** | O(n) | The `chars` list (O(1) if working with a mutable char array) |

### Edge Cases

| Scenario | Input | Output | Note |
|---|---|---|---|
| Single word | `"hello"` | `"hello"` | Full reverse gives `"olleh"`; reversing single word gives `"hello"` back |
| Two words | `"hi there"` | `"there hi"` | One full reverse + two word reverses |
| Leading space | `" hello"` | `"hello"` | Space normalised out by `removeExtraSpaces` |
| Trailing space | `"hello "` | `"hello"` | Trailing space stripped |
| Multiple spaces | `"hi   there"` | `"there hi"` | Multi-space runs collapsed to one |
| Empty string | `""` | `""` | No words; result is empty |

</details>
<details>
<summary><h2>The Full Picture: All Six Problems</h2></summary>


You've now seen every direct-application two-pointer problem in this section. They all share the same skeleton — what changes is the work done inside the loop:

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
  TP["Two-Pointer Direct Application"]
  FC["Flip Characters<br/>Swap chars from both ends"]
  PC["Palindrome Checker<br/>Compare chars from both ends"]
  VE["Vowel Exchange<br/>Scan to vowel, then swap"]
  RW["Reverse Words<br/>Apply reversal per word"]
  RS["Reverse Segments<br/>Reverse first k of every 2k block"]
  RWO["Reverse Word Order<br/>Full reverse + per-word reverse"]

  TP --> FC
  TP --> PC
  TP --> VE
  TP --> RW
  TP --> RS
  TP --> RWO
```

<p align="center"><strong>All six direct-application problems — one template, six variations in the loop body.</strong></p>

</details>
<details>
<summary><h2>Key Takeaway</h2></summary>


Reverse Word Order is the culminating problem of this section — it composes two two-pointer operations in sequence, and it's the first problem where the trick isn't immediately obvious. The insight (reverse-all, then reverse-each) is a pattern worth memorising: it appears in several string manipulation problems and is a favourite in technical interviews. Once you see it, you never forget it.

</details>