# 5. Pattern: Tail Recursion

If head recursion is "ask first, work second," tail recursion is the opposite: **work first, then ask.** Every frame finishes its contribution *before* making the recursive call, accumulating the answer into a parameter that travels down the stack. By the time the base case fires, the answer is already complete — no unwinding needed.

That timing flip has a major consequence. Tail-recursive functions can — in some languages, with some flags — be compiled into ordinary loops, eliminating the stack-frame cost entirely. This is **tail-call optimisation** (TCO), and it's the reason functional languages like Scala and OCaml lean on tail recursion as their primary loop construct.

By the end of this lesson you'll know what makes a recursive call "tail" vs not, the diagnostic checks for spotting tail-recursion candidates, which languages actually optimise tail calls, and four worked problems that drill the pattern.

## Table of contents

1. [Understanding tail recursion](#understanding-tail-recursion)
2. [Identifying tail recursion](#identifying-tail-recursion)
3. [Reverse sequence](#reverse-sequence)
4. [Search element](#search-element)
5. [Is palindrome](#is-palindrome)
6. [Reverse a list](#reverse-a-list)

***

# Understanding Tail Recursion

A function call is a **tail call** if it's the *last* thing the calling function does — nothing remains to compute after it returns. A function is **tail-recursive** if its recursive call is a tail call: the function returns the result of the recursive call directly, doing no work afterwards.

Compare:

```
# Head recursion — work happens AFTER the recursive call (on ascent)
def head(n):
    if n == 0: return 0
    return n + head(n - 1)        # recursive call is INSIDE an expression;
                                  # the `n + _` runs after the call returns

# Tail recursion — work happens BEFORE the recursive call (on descent)
def tail(n, acc=0):
    if n == 0: return acc
    return tail(n - 1, acc + n)   # recursive call is the LAST action;
                                  # the `acc + n` is computed first and passed in
```

The difference looks small. The runtime impact is huge. In the head version, every frame must wait for its recursive call to return before it can run `n + _`. The frames pile up and unwind. In the tail version, the recursive call has nothing to return *to* — the current frame can be discarded the moment the call is made. With TCO, the frame is reused; without it, frames still pile up but the algorithm is *logically* a loop.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart TB
  subgraph DOWN["Descent — work happens here"]
    F5["f(5, acc=0)<br/>compute acc+5 = 5"] -->|"call f(4, 5)"| F4["f(4, acc=5)<br/>compute acc+4 = 9"]
    F4 -->|"call f(3, 9)"| F3["f(3, acc=9)<br/>compute acc+3 = 12"]
    F3 -->|"call f(2, 12)"| F2["f(2, acc=12)<br/>compute acc+2 = 14"]
    F2 -->|"call f(1, 14)"| F1["f(1, acc=14)<br/>compute acc+1 = 15"]
    F1 -->|"call f(0, 15)"| F0["f(0, acc=15)<br/>BASE — return 15"]
  end
  F0 -.->|"return 15 directly"| DONE["caller receives 15<br/>(no per-frame combine)"]
```

<p align="center"><strong>Tail recursion: each frame does its work on the way <em>down</em> and stuffs the partial answer into the <code>acc</code> parameter. The base case returns the final answer; nothing happens during unwinding.</strong></p>

This downward-flowing accumulator is the heart of tail recursion. The recursive call doesn't produce a value the current frame needs; the current frame produces a value the *next* recursive call needs.

---

## Tail Call Optimisation (TCO)

TCO is the compiler trick that turns tail-recursive functions into loops. Since the calling frame has nothing left to do once the tail call is made, the compiler can **reuse** the same stack frame for the recursive call instead of pushing a new one. The result: a tail-recursive function that would normally use `O(n)` stack space runs in `O(1)` stack space — same as a `while` loop.

```d2
direction: right

without: "Without TCO" {
  grid-rows: 1
  grid-columns: 1
  grid-gap: 0
  s: "Stack: 5 frames pile up\n(O(n) space)" {style.fill: "#fecaca"; style.stroke: "#dc2626"}
}

with: "With TCO" {
  grid-rows: 1
  grid-columns: 1
  grid-gap: 0
  s: "Stack: 1 frame, reused\n(O(1) space)" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}

without -> with: compiler optimises tail calls
```

<p align="center"><strong>With TCO, the same frame is reused for every tail call — recursion becomes a loop without you writing one.</strong></p>

But not every language optimises tail calls. Here's the per-language reality across a range of mainstream languages:

| Language | TCO? | How |
|---|---|---|
| **Scala** | ✅ Yes | `@tailrec` annotation; the compiler verifies the call is in tail position and rewrites to a loop. |
| **Kotlin** | ✅ Yes | `tailrec` modifier on the function; same idea as Scala. |
| **C** | ⚠️ Sometimes | GCC/Clang at `-O2` or higher; not part of the language spec, so you can't rely on it. |
| **C++** | ⚠️ Sometimes | Same as C — depends on compiler and optimisation level. |
| **Rust** | ⚠️ Sometimes | LLVM may apply TCO at `--release`; no language guarantee. |
| **Go** | ❌ No | The Go authors explicitly chose not to do TCO; goroutine stacks grow on demand instead. |
| **Java** | ❌ No | The JVM has no TCO; deep tail recursion still overflows the stack. |
| **JavaScript** | ❌ No (in practice) | ES2015 specced TCO ("PTC") but only Safari implemented it; V8 explicitly rejected it. |
| **TypeScript** | ❌ No | Compiles to JS — same situation. |
| **Python** | ❌ No (and won't be) | Guido van Rossum explicitly rejected TCO; better stack traces matter more in Python's design. |

> **Practical takeaway.** In Scala and Kotlin, write tail-recursive code freely — the compiler will turn it into a loop. In C/C++/Rust, it's a nice-to-have at high optimisation levels. In **Java, Python, JavaScript, TypeScript, and Go**, tail recursion gives you the *style* of recursion-as-a-loop but **still uses linear stack space**. For very deep recursions in those languages, you should rewrite to iteration explicitly.

> *Predict before reading on — for a function that recurses 100,000 times, which languages in the table will crash with a stack overflow if you write the function tail-recursively? List them before reading the answer.*

In Java, Python, JavaScript, TypeScript, Java, and (for native compilers without explicit TCO flags) sometimes C/C++/Rust as well, you'll crash. Go's growable stacks save you. Scala (with `@tailrec`) and Kotlin (with `tailrec`) compile to loops and run in `O(1)` stack — perfectly safe. The lesson: tail recursion gives you *correctness* in any language, but *space efficiency* only in some.

---

## What Tail Recursion Looks Like in Code

The generic shape:

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  EQ["f(n, acc) = f(h(n), g(acc, n))"]
  EQ --> H["h(n)<br/>reduce input toward base"]
  EQ --> G["g(acc, n)<br/>fold this step's contribution<br/>into the accumulator"]
  EQ -.->|"anchored by"| BASE["f(base, acc) = acc<br/>BASE CASE — return acc directly"]
```

<p align="center"><strong>Tail recursion's general equation: each call updates an accumulator with this step's work and recurses with a smaller input. The base case returns the accumulator unchanged.</strong></p>

The pseudocode:

```
function tail_recursion(n, acc):
    if n is base case:
        return acc                         ← step 0: stop and return accumulated answer

    new_acc = g(acc, n)                    ← step 1: fold this step's contribution
    next_n  = h(n)                         ← step 2: reduce the input
    return tail_recursion(next_n, new_acc) ← step 3: tail call (LAST action, nothing follows)
```

Notice the difference from head recursion:
- The combine step `g` runs **before** the recursive call (head recursion ran it after).
- The recursive call is the **last action** — no `+ result` or wrapping work follows.
- The base case returns `acc` itself — no further computation needed.

The result of the function is *complete* by the time the base case is reached. The base case just hands it back.

---

## Passing Data Down

In tail recursion, **the accumulator is the lifeblood of the algorithm.** It carries the answer-being-built down through every call. The accumulator is initialised at the top-level call (often to a sensible default like `0`, `""`, `[]`, or the input itself) and updated by `g` at every step.

A common pattern is to wrap the tail-recursive helper in a public method that hides the accumulator from the caller:

```python run
class Solution:
    def sum_to_n(self, n: int) -> int:
        return self._helper(n, 0)        # caller doesn't see the acc parameter

    def _helper(self, n: int, acc: int) -> int:
        if n == 0: return acc
        return self._helper(n - 1, acc + n)
```

The wrapper hides the accumulator's existence from the caller, who only knows about `sum_to_n(n)`. This is the canonical idiom — embrace it.

---

## Passing Data Up

Tail recursion barely passes anything *up*. Each frame returns whatever the next-deeper call returned, with no transformation. The base case's `return acc` is the only "real" return value; every other frame's `return helper(...)` is a pass-through.

That's the structural reason TCO works: there's literally nothing for the current frame to do after the recursive call returns. The frame's work is over. So why allocate the frame at all? TCO just doesn't.

---

## Algorithm

Putting it together:

> **tailRecursion(n, acc)**
>
> 1. **Stop** — if `n` is the base case, return `acc`.
> 2. **Fold** — compute `new_acc = g(acc, n)`.
> 3. **Reduce** — compute `next_n = h(n)`.
> 4. **Tail call** — return `tailRecursion(next_n, new_acc)` directly.

Steps 2 and 3 are this frame's work; step 4 is the tail call. The function never combines anything on the way back.

---

## Implementation

A clean, language-agnostic implementation of the generic template — `g` and `h` are placeholders the problem will fill in. **Pay attention to language-specific TCO annotations** where applicable.


```python run
from typing import List

class Solution:
    def tail_recursion(self, n: int, aggregate: List[int]) -> int:

        # Base case: If n is less than or equal to 0, we have reached
        # the end of recursion
        if n <= 0:
            return len(aggregate)  # Return the size of the aggregate as an example

        # Use the function h to reduce the input
        # for the next step
        input_value: int = self.h(n)

        # Use the function g to update aggregate
        # for the next step
        self.g(n, aggregate)

        # Recursive call with the reduced input
        # at the end of the function
        solution: int = self.tail_recursion(input_value, aggregate)

        # Return the solution for the current input
        return solution

    def g(self, n: int, aggregate: List[int]) -> None:
        # Placeholder for g - update the aggregate using the
        # current input on the previous aggregate
        # Implement your logic here
        if n % 2 == 0:
            aggregate.append(n)  # Example implementation

    def h(self, n: int) -> int:
        # Placeholder for h - get the input for the next step
        # from the current input
        return n - 1  # Example implementation
```

```java run
import java.util.ArrayList;

class Solution {

    public int tailRecursion(int n, ArrayList<Integer> aggregate) {

        // Base case: If n is less than or equal to 0, we have reached
        // the end of recursion
        if (n <= 0) {
            return aggregate.size(); // Return the size of the aggregate as an example
        }

        // Use the function h to reduce the input
        // for the next step
        int input = h(n);

        // Use the function g to update aggregate
        // for the next step
        g(n, aggregate);

        // Recursive call with the reduced input
        // at the end of the function
        int solution = tailRecursion(input, aggregate);

        // Return the solution for the current input
        return solution;
    }

    // Placeholder for g - update the aggregate using the
    // current input on the previous aggregate
    private void g(int n, ArrayList<Integer> aggregate) {
        // Implement your logic here
        if (n % 2 == 0) {
            aggregate.add(n); // Example implementation
        }
    }

    // Placeholder for h - get the input for the next step
    // from the current input
    private int h(int n) {
        // Implement your logic here
        return n - 1; // Example implementation
    }
}
```


---

## Complexity Analysis

| Resource | Cost (without TCO) | Cost (with TCO) | Why |
|---|---|---|---|
| **Time** | `O(n)` if `g`, `h` are `O(1)` | `O(n)` | Same total work either way. |
| **Space** | `O(n)` | `O(1)` | Without TCO, frames pile up; with TCO, one frame is reused. |

The space column tells the whole language-dependent story. Tail-recursive code in Scala or Kotlin (with the right annotation) is genuinely as efficient as a `while` loop. The same code in Java or Python uses linear stack and crashes on deep input. Identical algorithm, very different runtime profile.

> **Best Case** — Time `O(n)`, Space `O(1)` (with TCO) or `O(n)` (without)
>
> **Worst Case** — Same as best; no input variation changes the depth

---

## Key Takeaway

Tail recursion does its work on the descent and accumulates the answer in a parameter. The base case returns that accumulator unchanged. With TCO it's a loop in disguise; without TCO it's structurally a loop but pays for each iteration in stack space. Now we'll learn how to spot tail-recursion candidates without writing any code.

***

# Identifying Tail Recursion

Three diagnostic questions decide whether tail recursion fits.

| # | Question | If "yes," tail recursion fits because... |
|---|---|---|
| **Q1** | Can the answer be built up *as we descend*, with no need to look back? | The accumulator can carry the running answer down without revisiting frames. |
| **Q2** | Can each step's contribution be folded into a single value (the accumulator)? | We don't need to wait for the smaller answer; we update the accumulator and recurse. |
| **Q3** | Is the recursive call the *very last* thing the function does? | The call is in tail position — no work follows it, so TCO is even possible. |

If all three are "yes," the problem fits tail recursion's template.

### Q1 — Why "build down, no look-back"?

**Mental model.** Tail recursion never revisits a frame. Once we descend, we're committed — the frame is conceptually gone (with TCO, literally gone). The answer must be representable as a single value being mutated as we go.

**Concrete check.** For `sum(1..n)`: build down with `acc + n`, going `5 → 4 → 3 → 2 → 1 → 0` with running sums `5 → 9 → 12 → 14 → 15 → 15`. We never need to revisit any frame. ✓

**What breaks otherwise.** Consider Fibonacci's classical form `fib(n) = fib(n-1) + fib(n-2)`. The result for `n` requires *two* smaller answers, and the second `+` happens after both calls return. There's no single accumulator that can capture this on the descent — fib needs head/multiple recursion (the Multiple Recursion lesson), not tail.

### Q2 — Why "fold into a single accumulator"?

**Mental model.** The accumulator is the *only* state the recursion carries. If the answer needs two or more parameters that interact, you need that many accumulator parameters. If the answer is fundamentally a *tree* (like a sorted output of an unsorted set), an accumulator can't hold the structure cleanly.

**Concrete check.** For `is_palindrome(arr)` we don't even need an accumulator value — the answer is "as long as no mismatch found, keep going." That's a degenerate accumulator (the answer is implicit in the *absence* of an early return). Tail recursion still fits. ✓

**What breaks otherwise.** Building a sorted permutation of an array's elements? An accumulator would need to be a partial tree of decisions; we'd actually need branching recursion (the Multiple Recursion lesson) or backtracking. Tail recursion's single-thread-of-progress model can't handle branching.

### Q3 — Why "recursive call is the very last action"?

**Mental model.** The call is in tail position only if **the function returns the result of that call directly, without any wrapping work**. `return helper(...)` ✓. `return helper(...) + 1` ✗ (the `+ 1` is wrapping work). `return helper(...) * helper(...)` ✗ (the multiplication wraps two calls).

**Concrete check.** Linear search for an element: `if arr[i] == target return i; return search(arr, target, i+1)`. The recursive call is wrapped by nothing — pure tail call. ✓

**What breaks otherwise.** `return n * factorial(n-1)` — the multiplication runs *after* the recursive call returns. That's head recursion, not tail. The frame can't be discarded because it has work to do on the ascent. TCO won't apply.

---

## A Worked Example — Reverse a Sequence

> *Pause and predict — to print numbers from 5 down to 1, would you want head recursion or tail recursion? Why?*

Tail recursion fits naturally: each step prints `n` first, then recurses on `n-1`. The work (the print) happens on the descent, before the recursive call. Head recursion could also work, but it would print the values during *unwinding*, which means the printing order would be `1, 2, 3, 4, 5` — not `5, 4, 3, 2, 1`. Tail wins for descending order; head wins for ascending. **The order of operations is the order of the recursion's direction.**

We make this concrete in **Problem 1** below.

---

## Key Takeaway

Three checks — descent-only progress, single-accumulator answer, recursive call in tail position — gate every tail-recursion problem. Pass all three and the template snaps in. Four worked problems coming up. The first one mirrors Forward Sequence from the Head Recursion lesson but reverses the *direction* of work — same template, opposite ordering.

***

# Reverse Sequence

The mirror image of Forward Sequence from the Head Recursion lesson. Same problem family, but now we want the numbers in descending order — and tail recursion gives it to us essentially for free.

---

## The Problem

Given a positive integer `n`, return a list containing the numbers from `n` down to `1`. You **must** solve this recursively.

```
Input:  n = 5
Output: [5, 4, 3, 2, 1]

Input:  n = 1
Output: [1]
```

---

<details>
<summary><h2>Why Tail Recursion Fits Here</h2></summary>


Each frame's job is to append its `n`, then recurse on `n-1`. The append happens *before* the recursive call. By the time we hit the base case, the list is fully built. The base case has nothing to do but return.

Compare with the head-recursive Forward Sequence: the list was built *during unwinding*, with each ascending frame appending its number. Here, the list is built *during the descent*, with each descending frame appending its number. **The direction of work matches the direction of output.**

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart TB
  R5["reverse(5, [])<br/>append 5 → [5]<br/>recurse(4)"] --> R4["reverse(4, [5])<br/>append 4 → [5,4]<br/>recurse(3)"] --> R3["reverse(3, [5,4])<br/>append 3 → [5,4,3]<br/>recurse(2)"] --> R2["reverse(2, [5,4,3])"] --> R1["reverse(1, ...)"] --> R0["reverse(0, [5,4,3,2,1])<br/>BASE — return acc"]
```

<p align="center"><strong>Each descending frame appends and recurses. The list is fully built by the time the base case fires.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| # | Check | Answer |
|---|---|---|
| **Q1** | Build down without look-back? | **Yes** — append each `n` as we go; never revisit. |
| **Q2** | Single accumulator? | **Yes** — the list itself is the accumulator. |
| **Q3** | Recursive call last? | **Yes** — append, then `return helper(n-1, result)` with nothing after. |

### Q1 — Why "build down, no look-back"?

The output `[5, 4, 3, 2, 1]` is exactly the descent order. Nothing in the result depends on a smaller `n`; in fact, smaller numbers are *appended after* larger ones. We never look back at frames already done. ✓

### Q2 — Why "the list is the accumulator"?

The list is the running answer, mutated as we descend. There's no second piece of state. Tail recursion needs exactly this. ✓

### Q3 — Why "the call is in tail position"?

After appending, the recursive call is the function's last action. The return is `return helper(n-1, result)` (or in languages without explicit return, just the call). Nothing wraps it — the language can apply TCO if it supports it. ✓

</details>
<details>
<summary><h2>The Append-on-Descent Strategy (Visualised)</h2></summary>


<div class="d2-slides" data-caption="Each descending frame appends and recurses. The list is fully built when the base case fires.">

```d2
state: "Initial — start at n=5" {
  list: "result = []"
}
```

```d2
state: "n=5 — append, recurse(4)" {
  list: "result = [5]" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
}
```

```d2
state: "n=4 — append, recurse(3)" {
  list: "result = [5, 4]" {style.fill: "#fde68a"; style.stroke: "#d97706"}
}
```

```d2
state: "n=3 — append, recurse(2)" {
  list: "result = [5, 4, 3]" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}
```

```d2
state: "n=1 — append, recurse(0)" {
  list: "result = [5, 4, 3, 2, 1]" {style.fill: "#ede9fe"; style.stroke: "#7c3aed"}
}
```

```d2
state: "n=0 — base case fires, return" {
  list: "result = [5, 4, 3, 2, 1] (final)" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}
```

</div>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class Solution:
    def helper(self, n: int, result: List[int]) -> None:

        # Base case: If n is less than or equal to 0, we have reached the
        # end of recursion
        if n <= 0:

            # Exit the function, as there are no more numbers to add
            return

        # First, add the current number n to the result list
        result.append(n)

        # Recursive call to the helper function with n-1, to move towards
        # the base case
        self.helper(n - 1, result)

    def reverse_sequence(self, n: int) -> List[int]:

        # Initialize an empty list to store the result
        result: List[int] = []

        # Call the helper function to populate the result list with
        # numbers from n to 1
        self.helper(n, result)

        # Return the generated list containing numbers from n to 1
        return result


# Examples from the problem statement
print(Solution().reverse_sequence(5))   # [5, 4, 3, 2, 1]

# Edge cases
print(Solution().reverse_sequence(1))   # [1]
print(Solution().reverse_sequence(3))   # [3, 2, 1]
print(Solution().reverse_sequence(10))  # [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
```

```java run
import java.util.*;

public class Main {
    static class Solution {
        private void helper(int N, List<Integer> result) {

            // Base case: If N is less than or equal to 0, we have reached
            // the end of recursion
            if (N <= 0) {

                // Exit the function, as there are no more numbers to add
                return;
            }

            // First, add the current number N to the result list
            result.add(N);

            // Recursive call to the helper method with N-1, to move towards
            // the base case
            helper(N - 1, result);
        }

        public List<Integer> reverseSquence(int N) {

            // Initialize an empty list to store the result
            List<Integer> result = new ArrayList<>();

            // Call the helper method to populate the result list with
            // numbers from N to 1
            helper(N, result);

            // Return the generated list containing numbers from N to 1
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().reverseSquence(5));   // [5, 4, 3, 2, 1]

        // Edge cases
        System.out.println(new Solution().reverseSquence(1));   // [1]
        System.out.println(new Solution().reverseSquence(3));   // [3, 2, 1]
        System.out.println(new Solution().reverseSquence(10));  // [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
    }
}
```


<details>
<summary><strong>Trace — n = 5</strong></summary>

```
Step 1 │ n=5 │ append 5 → [5]            │ recurse(4)
Step 2 │ n=4 │ append 4 → [5, 4]         │ recurse(3)
Step 3 │ n=3 │ append 3 → [5, 4, 3]      │ recurse(2)
Step 4 │ n=2 │ append 2 → [5, 4, 3, 2]   │ recurse(1)
Step 5 │ n=1 │ append 1 → [5, 4, 3, 2, 1]│ recurse(0)
Step 6 │ n=0 │ BASE — return immediately

Result: [5, 4, 3, 2, 1]  (no per-frame combine on ascent)
```

The list is fully built during descent. The ascent is silent — every frame just returns.

</details>

### Complexity Analysis

| Resource | Cost (without TCO) | Cost (with TCO) | Why |
|---|---|---|---|
| **Time** | `O(n)` | `O(n)` | One append per integer. |
| **Space (output)** | `O(n)` | `O(n)` | The list has `n` elements. |
| **Space (stack)** | `O(n)` | `O(1)` | Without TCO, `n` frames; with TCO, one reused. |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Smallest valid | `n = 1` | `[1]` | Append 1, recurse(0) → base. |
| Smallest possible | `n = 0` | `[]` | Base case fires immediately. |
| Negative input | `n = -3` | `[]` | Guard catches it. |
| Large input | `n = 100_000` | descending list | Stack overflow on Java/Python/JS without TCO; safe on Scala/Kotlin/Go. |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Reverse Sequence is the tail-recursion mirror of Forward Sequence: same recursion shape, same depth, but the work happens on the way *down* rather than the way back. The next problem applies the same template to *search* — where the accumulator's job is to track "we haven't found it yet" rather than to build output.

</details>

***

# Search Element

Linear search done recursively. The accumulator is the index we're currently inspecting — start at 0, advance by 1 each call, return the index on a match or `-1` at the end.

---

## The Problem

Given an array `arr` and a `target`, return the index of the first occurrence of `target`, or `-1` if it's not present. You **must** solve this recursively.

```
Input:  arr = [2, 8, 3, 6, 4], target = 3
Output: 2

Input:  arr = [1, 2, 3, 4, 5], target = 5
Output: 4

Input:  arr = [2, 8, 1, 9, 4], target = 10
Output: -1
```

---

<details>
<summary><h2>Why Tail Recursion Fits Here</h2></summary>


The "answer being built" is the *current index*. We start at 0 and advance until either we find the target or we run off the end of the array. At each step we check the current element, return immediately on match, or recurse with the next index.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  S0["search(arr, t, i=0)<br/>arr[0] = 2 ≠ 3 → recurse(1)"] --> S1["search(arr, t, i=1)<br/>arr[1] = 8 ≠ 3 → recurse(2)"] --> S2["search(arr, t, i=2)<br/>arr[2] = 3 = target → return 2"]
```

<p align="center"><strong>Each call checks one element and either returns or tail-calls with <code>i + 1</code>.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| # | Check | Answer |
|---|---|---|
| **Q1** | Build down without look-back? | **Yes** — index advances forward; we never revisit. |
| **Q2** | Single accumulator? | **Yes** — the index itself. |
| **Q3** | Recursive call last? | **Yes** — return on match, or tail-call on no-match. |

### Q1 — Why "advance, don't look back"?

Linear search is left-to-right by definition. Once we've moved past index `i`, we never need to come back. The recursion's natural direction matches the algorithm's natural direction. ✓

### Q2 — Why "the index is the accumulator"?

The only state we need is "where am I in the array?" That's a single integer; perfect tail-recursion accumulator. ✓

### Q3 — Why "the call is in tail position"?

The function either returns `index` (match found), `-1` (off the end), or `helper(arr, target, index + 1)`. Each branch is a direct return — no wrapping work. ✓

</details>
<details>
<summary><h2>The Linear-Scan-with-Index Strategy (Visualised)</h2></summary>


<div class="d2-slides" data-caption="The index advances; the array is read-only. The accumulator IS the answer.">

```d2
state: "Step 0 — i = 0" {
  arr: "arr = [2, 8, 3, 6, 4],  target = 3" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  check: "arr[0] = 2 ≠ 3 → recurse(1)"
}
```

```d2
state: "Step 1 — i = 1" {
  arr: "arr = [2, 8, 3, 6, 4]"
  check: "arr[1] = 8 ≠ 3 → recurse(2)" {style.fill: "#fde68a"; style.stroke: "#d97706"}
}
```

```d2
state: "Step 2 — i = 2 — MATCH" {
  arr: "arr = [2, 8, 3, 6, 4]"
  check: "arr[2] = 3 = target → return 2" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}
```

</div>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class Solution:
    def _helper(self, arr: List[int], target: int, index: int) -> int:

        # Base case: If index reaches the size of the array,
        # the target is not found
        if index == len(arr):
            return -1

        # If the current element matches the target, return the index
        if arr[index] == target:
            return index

        # Recursive call to check the next element in the array
        return self._helper(arr, target, index + 1)

    def search_element(self, arr: List[int], target: int) -> int:
        return self._helper(arr, target, 0)


# Examples from the problem statement
print(Solution().search_element([2, 8, 3, 6, 4], 3))   # 2
print(Solution().search_element([1, 2, 3, 4, 5], 5))   # 4
print(Solution().search_element([2, 8, 1, 9, 4], 10))  # -1

# Edge cases
print(Solution().search_element([], 5))                # -1
print(Solution().search_element([7], 7))               # 0
print(Solution().search_element([7], 1))               # -1
print(Solution().search_element([1, 2, 3], 1))         # 0
```

```java run
public class Main {
    static class Solution {
        private int helper(int[] arr, int target, int index) {

            // Base case: If index reaches the size of the array,
            // the target is not found
            if (index == arr.length) {
                return -1;
            }

            // If the current element matches the target, return the index
            if (arr[index] == target) {
                return index;
            }

            // Recursive call to check the next element in the array
            return helper(arr, target, index + 1);
        }

        public int searchElement(int[] arr, int target) {
            return helper(arr, target, 0);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().searchElement(new int[]{2, 8, 3, 6, 4}, 3));   // 2
        System.out.println(new Solution().searchElement(new int[]{1, 2, 3, 4, 5}, 5));   // 4
        System.out.println(new Solution().searchElement(new int[]{2, 8, 1, 9, 4}, 10));  // -1

        // Edge cases
        System.out.println(new Solution().searchElement(new int[]{}, 5));                // -1
        System.out.println(new Solution().searchElement(new int[]{7}, 7));               // 0
        System.out.println(new Solution().searchElement(new int[]{7}, 1));               // -1
        System.out.println(new Solution().searchElement(new int[]{1, 2, 3}, 1));         // 0
    }
}
```


<details>
<summary><strong>Trace — arr = [2, 8, 3, 6, 4], target = 3</strong></summary>

```
Step 1 │ index=0, arr[0]=2  │ 2 ≠ 3      │ recurse(1)
Step 2 │ index=1, arr[1]=8  │ 8 ≠ 3      │ recurse(2)
Step 3 │ index=2, arr[2]=3  │ 3 = target │ return 2

Result: 2  (early termination on match — no further recursion)
```

When the match is found, every paused frame returns the same `index` value directly — no combining. With TCO, even the paused frames don't exist; without TCO, they unwind passing the answer up unchanged.

</details>

### Complexity Analysis

| Resource | Cost | Why |
|---|---|---|
| **Time** | `O(n)` worst case | We may scan the entire array. |
| **Space (stack)** | `O(n)` without TCO, `O(1)` with TCO | Recursion depth = scan length. |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Empty array | `arr = []` | `-1` | Base case 1 fires immediately. |
| Match at start | `arr = [3, ...]` | `0` | Base case 2 fires on first call. |
| Match at end | `arr = [..., 3]` | `n - 1` | Last call before "off the end" finds it. |
| No match | `arr = [..., target absent]` | `-1` | Recurses through all `n` elements. |
| Duplicates | `arr = [3, 1, 3, 5]` | `0` | Returns the first match — the index of the leftmost occurrence. |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Search Element shows tail recursion in its simplest "scanner" form: an index accumulator advances until a condition fires. Same template you'll use for "find first negative number," "find first repeated character," and dozens of single-pass tests. The next problem also uses the index-advance template, but with two indices that converge from opposite ends.

</details>

***

# Is Palindrome

Two-pointer recursion. Start at both ends, walk inward, fail fast on mismatch.

---

## The Problem

Given an array `arr`, return `true` if it reads the same forwards and backwards, else `false`. You **must** solve this recursively.

```
Input:  arr = [1, 2, 2, 1]
Output: true

Input:  arr = [1, 3, 2, 1]
Output: false

Input:  arr = []
Output: true   (an empty array is trivially a palindrome)
```

---

<details>
<summary><h2>Why Tail Recursion Fits Here</h2></summary>


Two pointers — `start` and `end` — converge from the array's edges toward the middle. Each call compares `arr[start]` and `arr[end]`. If they differ, return `false`. If they match, recurse with `start + 1` and `end - 1`. When `start >= end`, every pair has been checked.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  P1["palindrome(arr, 0, 3)<br/>arr[0]=1, arr[3]=1 → match<br/>recurse(1, 2)"] --> P2["palindrome(arr, 1, 2)<br/>arr[1]=2, arr[2]=2 → match<br/>recurse(2, 1)"] --> P3["palindrome(arr, 2, 1)<br/>start ≥ end → return true"]
```

<p align="center"><strong>Two pointers march toward each other; mismatch is an early-return false; meeting-or-crossing is a true return.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| # | Check | Answer |
|---|---|---|
| **Q1** | Build down without look-back? | **Yes** — pointers move monotonically toward the centre. |
| **Q2** | Single accumulator? | **Yes** — the pointer pair `(start, end)` is a degenerate accumulator (we don't really build anything; we just track positions). |
| **Q3** | Recursive call last? | **Yes** — early-return on mismatch, otherwise tail-call. |

### Q1 — Why "monotonic convergence"?

Each call narrows the unchecked range by 2 (one on each side). The window strictly shrinks; we never expand. Eventually `start >= end` and we're done. ✓

### Q2 — Why "the pointers are the accumulator"?

There's nothing to *build* — the answer is "no mismatch found, keep going" or "mismatch found, false." The pointers carry the position state forward; no value accumulation is needed. ✓

### Q3 — Why "the call is in tail position"?

Three branches: `return true` (done), `return false` (mismatch), `return helper(arr, start + 1, end - 1)` (tail call). All three are direct returns. ✓

</details>
<details>
<summary><h2>The Two-Pointer Convergence Strategy (Visualised)</h2></summary>


<div class="d2-slides" data-caption="Each call compares one pair and either returns false or recurses with both pointers moved inward.">

```d2
state: "arr = [1, 2, 2, 1]   start=0, end=3" {
  pair: "arr[0]=1 vs arr[3]=1 → match" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}
```

```d2
state: "arr = [1, 2, 2, 1]   start=1, end=2" {
  pair: "arr[1]=2 vs arr[2]=2 → match" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
}
```

```d2
state: "start=2, end=1   start ≥ end" {
  result: "All pairs matched → return true" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
}
```

</div>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import List

class Solution:
    def helper(self, arr: List[int], start: int, end: int) -> bool:

        # Base case: If start index crosses end index,
        # we have checked all elements
        if start >= end:
            return True

        # Check if the elements at the current indices are equal
        if arr[start] != arr[end]:
            return False

        # Recursive call moving towards the center of the list
        return self.helper(arr, start + 1, end - 1)

    def is_palindrome(self, arr: List[int]) -> bool:
        return self.helper(arr, 0, len(arr) - 1)


# Examples from the problem statement
print(Solution().is_palindrome([1, 2, 2, 1]))     # True
print(Solution().is_palindrome([1, 3, 2, 1]))     # False
print(Solution().is_palindrome([]))               # True

# Edge cases
print(Solution().is_palindrome([5]))              # True
print(Solution().is_palindrome([1, 2, 1]))        # True
print(Solution().is_palindrome([1, 2, 3]))        # False
print(Solution().is_palindrome([1, 1, 1, 1]))     # True
```

```java run
public class Main {
    static class Solution {
        private boolean helper(int[] arr, int start, int end) {

            // Base case: If start index crosses end index,
            // we have checked all elements
            if (start >= end) {
                return true;
            }

            // Check if the elements at the current indices are equal
            if (arr[start] != arr[end]) {
                return false;
            }

            // Recursive call moving towards the center of the list
            return helper(arr, start + 1, end - 1);
        }

        public boolean isPalindrome(int[] arr) {
            return helper(arr, 0, arr.length - 1);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().isPalindrome(new int[]{1, 2, 2, 1}));     // true
        System.out.println(new Solution().isPalindrome(new int[]{1, 3, 2, 1}));     // false
        System.out.println(new Solution().isPalindrome(new int[]{}));               // true

        // Edge cases
        System.out.println(new Solution().isPalindrome(new int[]{5}));              // true
        System.out.println(new Solution().isPalindrome(new int[]{1, 2, 1}));        // true
        System.out.println(new Solution().isPalindrome(new int[]{1, 2, 3}));        // false
        System.out.println(new Solution().isPalindrome(new int[]{1, 1, 1, 1}));     // true
    }
}
```


<details>
<summary><strong>Trace — arr = [1, 3, 2, 1]</strong></summary>

```
Step 1 │ start=0, end=3 │ arr[0]=1, arr[3]=1 │ match     │ recurse(1, 2)
Step 2 │ start=1, end=2 │ arr[1]=3, arr[2]=2 │ MISMATCH  │ return false

Result: false  (mismatch caught at step 2; no further recursion)
```

The early-return on mismatch is the algorithm's strength: we stop the moment we know the answer.

</details>

### Complexity Analysis

| Resource | Cost | Why |
|---|---|---|
| **Time** | `O(n)` worst case | At most `n / 2` comparisons. |
| **Space (stack)** | `O(n)` without TCO, `O(1)` with TCO | Depth = `n / 2`. |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Empty | `arr = []` | `true` | `start = 0 >= end = -1` immediately. |
| Single element | `arr = [5]` | `true` | `start = 0, end = 0` → base case. |
| Two same | `arr = [3, 3]` | `true` | Match, recurse to `start = 1, end = 0` → base. |
| Two different | `arr = [3, 4]` | `false` | Mismatch on first call. |
| Odd length | `arr = [1, 2, 1]` | `true` | Middle element never compared (correctly). |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Is-Palindrome is tail recursion with two converging pointers. The accumulator is positional state, not value-building. Once you see this two-pointer convergence pattern, you'll see it again in "find pair summing to target," in "trim a list from both sides," in any problem where the operation is symmetric across the middle. The next problem combines tail-recursion's downward work with a *destructive* operation: rewriting linked-list pointers.

</details>

***

# Reverse a List

The classic interview question. Reverse a singly linked list using only `O(1)` auxiliary memory (with TCO, anyway). The recursive solution is three lines and arguably clearer than the iterative one.

---

## The Problem

Given the head of a singly linked list, reverse it in place and return the new head. You **must** solve this recursively.

```
Input:  head → 5 → 7 → 3 → 10 → null
Output: head → 10 → 3 → 7 → 5 → null
```

---

<details>
<summary><h2>Why Tail Recursion Fits Here</h2></summary>


Reversing a linked list is a perfect tail-recursion candidate because we can carry both **the current node** and **the previous node** forward as accumulator parameters. Each step rewires `current.next = previous`, then advances both pointers by one. Because Python (and Java) have no TCO, the canonical implementation below collapses the tail recursion into its equivalent `while` loop — the logic and the per-step work are identical; only the frame mechanics change.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#777777"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
  S1["helper(5, null)<br/>save 7; 5.next = null<br/>recurse(7, 5)"] --> S2["helper(7, 5)<br/>save 3; 7.next = 5<br/>recurse(3, 7)"] --> S3["helper(3, 7)<br/>save 10; 3.next = 7<br/>recurse(10, 3)"] --> S4["helper(10, 3)<br/>save null; 10.next = 3<br/>recurse(null, 10)"] --> S5["helper(null, 10)<br/>BASE — return 10"]
```

<p align="center"><strong>The conceptual tail-recursive shape: each frame saves <code>current.next</code> in a local, rewires <code>current.next = previous</code>, then tail-calls with <code>(saved_next, current)</code>. The base case returns the last <code>previous</code> — the new head. The implementation below is the equivalent <code>while</code> loop — same per-step work, one reused frame.</strong></p>

</details>
<details>
<summary><h2>Applying the Diagnostic Questions</h2></summary>


| # | Check | Answer |
|---|---|---|
| **Q1** | Build down without look-back? | **Yes** — we walk forward through the list once, rewiring as we go. |
| **Q2** | Single accumulator? | **Yes** — the `previous` pointer is the running answer. |
| **Q3** | Recursive call last? | **Yes** — the tail-recursive shape `return helper(next, current)` would be the last action; here we collapse that shape into a `while` loop because Python/Java have no TCO. |

### Q1 — Why "single forward pass, no look-back"?

We never revisit a node. Each node is touched exactly once: we save its `.next`, rewire it, and move on. The walk is monotonically forward. ✓

### Q2 — Why "previous is the accumulator"?

The "answer being built" is the head of the reversed list — which, at any moment, is the most-recently-rewired node. That's exactly `previous`. ✓

### Q3 — Why "the call would be in tail position"?

After rewiring, a tail-recursive version would end with `return helper(next, current)` — nothing following the call. Because the languages here don't optimise tail calls, the canonical implementation hoists that same logic into a `while` loop: the loop body is the would-be tail-call body, and the `current is None` check is the would-be base case. Logic identical; frames flat. ✓

</details>
<details>
<summary><h2>The Pointer-Rewire Strategy (Visualised)</h2></summary>


<div class="d2-slides" data-caption="Each frame rewires one pointer and advances. The accumulator (`previous`) becomes the new head when we run off the end.">

```d2
state: "Initial — current=5, previous=null" {
  list: "5 → 7 → 3 → 10 → null" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  ptrs: "previous=null  current=5  next=7"
}
```

```d2
state: "After step 1 — current=7, previous=5" {
  list: "5 → null   then   7 → 3 → 10 → null"
  ptrs: "previous=5  current=7  next=3" {style.fill: "#fde68a"; style.stroke: "#d97706"}
}
```

```d2
state: "After step 2 — current=3, previous=7" {
  list: "7 → 5 → null   then   3 → 10 → null"
  ptrs: "previous=7  current=3  next=10"
}
```

```d2
state: "After step 3 — current=10, previous=3" {
  list: "3 → 7 → 5 → null   then   10 → null"
  ptrs: "previous=3  current=10  next=null"
}
```

```d2
state: "After step 4 — current=null, previous=10" {
  list: "10 → 3 → 7 → 5 → null" {style.fill: "#bbf7d0"; style.stroke: "#16a34a"}
  ptrs: "BASE — return previous=10 (new head)"
}
```

</div>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
from typing import Optional, List, Any


class ListNode:
    def __init__(self, val=0, nxt=None):
        self.val = val
        self.next = nxt


def from_list(values):
    if not values:
        return None
    head = ListNode(values[0])
    cur = head
    for v in values[1:]:
        cur.next = ListNode(v)
        cur = cur.next
    return head


def to_list(head):
    out = []
    while head is not None:
        out.append(head.val)
        head = head.next
    return out


class Solution:
    def reverse_a_list(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # Initialize pointers current and previous
        current: Optional[ListNode] = head
        previous: Optional[ListNode] = None

        while current is not None:

            # Save the address of next node
            next_node = current.next

            # Update the next of current node
            current.next = previous

            # Move previous to hold current node
            previous = current

            # Move current ahead
            current = next_node

        return previous


# Example from the problem statement
print(to_list(Solution().reverse_a_list(from_list([5, 7, 3, 10]))))   # [10, 3, 7, 5]

# Edge cases
print(to_list(Solution().reverse_a_list(None)))                        # []
print(to_list(Solution().reverse_a_list(from_list([42]))))             # [42]
print(to_list(Solution().reverse_a_list(from_list([1, 2]))))           # [2, 1]
print(to_list(Solution().reverse_a_list(from_list([1, 2, 3]))))        # [3, 2, 1]
print(to_list(Solution().reverse_a_list(from_list([1, 1, 1]))))        # [1, 1, 1]
```

```java run
import java.util.*;

public class Main {
    static class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    static ListNode fromList(int... values) {
        if (values.length == 0) return null;
        ListNode head = new ListNode(values[0]);
        ListNode cur = head;
        for (int i = 1; i < values.length; i++) {
            cur.next = new ListNode(values[i]);
            cur = cur.next;
        }
        return head;
    }

    static List<Integer> toList(ListNode head) {
        List<Integer> out = new ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        public ListNode reverseAList(ListNode head) {

            // Initialize pointers current and previous
            ListNode current = head;
            ListNode previous = null;

            while (current != null) {

                // Save the address of next node
                ListNode next = current.next;

                // Update the next of current node
                current.next = previous;

                // Move previous to hold current node
                previous = current;

                // Move current ahead
                current = next;
            }

            return previous;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().reverseAList(fromList(5, 7, 3, 10))));   // [10, 3, 7, 5]

        // Edge cases
        System.out.println(toList(new Solution().reverseAList(null)));                     // []
        System.out.println(toList(new Solution().reverseAList(fromList(42))));             // [42]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 2))));           // [2, 1]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 2, 3))));        // [3, 2, 1]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 1, 1))));        // [1, 1, 1]
    }
}
```


<details>
<summary><strong>Trace — head = 5 → 7 → 3 → 10 → null</strong></summary>

```
Initial: previous=null   current=5

Step 1 │ save next=7  │ 5.next = null     │ recurse(7, 5)
        new state: 5 → null  ;  7 → 3 → 10 → null  (two pieces)

Step 2 │ save next=3  │ 7.next = 5        │ recurse(3, 7)
        new state: 7 → 5 → null  ;  3 → 10 → null

Step 3 │ save next=10 │ 3.next = 7        │ recurse(10, 3)
        new state: 3 → 7 → 5 → null  ;  10 → null

Step 4 │ save next=null │ 10.next = 3     │ recurse(null, 10)
        new state: 10 → 3 → 7 → 5 → null

Step 5 │ current=null │ BASE — return previous=10

Final answer: head → 10 → 3 → 7 → 5 → null  ✓
```

The trace above tracks the conceptual tail-recursive form; the implementation hoists those same steps into a `while` loop, so each "recurse(...)" line corresponds to one loop iteration's advance of the `(previous, current)` pair, and "BASE" corresponds to the `current is None` loop exit. The list is reversed in place. In a language with TCO (Scala `@tailrec`, Kotlin `tailrec`), the recursive form runs in `O(1)` stack space; in Python/Java/JS/Go the loop form gets you the same `O(1)` stack directly.

</details>

### Complexity Analysis

| Resource | Cost | Why |
|---|---|---|
| **Time** | `O(n)` | Each node touched once. |
| **Space (stack)** | `O(1)` for the loop form here; `O(n)` for a recursive form without TCO, `O(1)` with TCO | The loop reuses one frame; the recursive form would push one frame per node unless the compiler optimises tail calls. |
| **Space (auxiliary)** | `O(1)` | Only a few pointers held at a time. |

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| Empty list | `head = null` | `null` | `current is None` on entry; the loop body never runs; `previous = null` is returned. |
| Single node | `head = [5]` | `[5]` | One iteration: rewire `5.next = null`, advance to `current = null`, exit loop. |
| Two nodes | `[5, 7]` | `[7, 5]` | Two iterations before the loop exits. |
| Already reversed | `[10, 3, 7, 5]` | `[5, 7, 3, 10]` | Reversal is its own inverse. |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Reverse-a-List is the canonical "two-pointer rewire" recursion: walk the list, save `next` before each rewire, advance both pointers in the tail call. The pattern shows up in linked-list rotation, in cycle detection (with care), in nth-from-end problems, and in any structural transformation of a linked list. The accumulator is the partially-reversed list; the recursion ends when we run off the end.

You came in with the timing flip — work first, ask later — as a fragile concept. You're leaving with four worked problems, three diagnostic questions, an understanding of TCO across languages, and a feel for when an accumulator-driven recursion beats the head-recursion alternative. The next lesson lifts the restriction "exactly one recursive call per frame": what happens when a function makes *two* recursive calls? The tree branches, the call count explodes, and we meet our first exponential-time recursion.

**Transfer challenge — try before the Multiple Recursion lesson:** Write a tail-recursive function that computes the **GCD of two non-negative integers** using Euclid's algorithm: `gcd(a, b) = gcd(b, a mod b)`, base case `gcd(a, 0) = a`. Use either language above. Three lines including the base case.

<details>
<summary><strong>Answer — open after you've written it</strong></summary>

```python run
class Solution:
    def gcd(self, a: int, b: int) -> int:
        if b == 0:
            return a            # Base case
        return self.gcd(b, a % b)   # Tail call — pure Euclidean step


print(Solution().gcd(48, 18))   # 6
```

The recursive relation `gcd(a, b) = gcd(b, a % b)` is Euclid's algorithm in pure tail-recursion form. The accumulator-style of "carry the running pair forward" is in plain sight. Depth is `O(log min(a, b))` (a beautiful number-theoretic fact). With Scala's `@tailrec` or Kotlin's `tailrec` this is genuinely a loop. Even without TCO, the depth is so shallow you'll never overflow.

**You just wrote one of the most efficient algorithms in human history in three lines.** That's tail recursion's gift: a clean accumulator-driven walk that mirrors the underlying mathematics exactly.

</details>

</details>