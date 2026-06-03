---
title: "Array Implementation Of Stacks"
summary: "A bounded stack on a fixed buffer: treat the last index as the top, and push, pop, and peek all collapse to O(1) one-liners over contiguous, cache-friendly memory."
---

# 2. Array Implementation of Stacks

## The Hook

A stack needs to push, pop, and peek in **O(1)** — and you already own the perfect tool: an array. Treat the *last index* of the array as the top, and every stack operation collapses to a one-liner. Push? Write to `arr[topIndex+1]` and bump `topIndex`. Pop? Read `arr[topIndex]` and decrement. Peek? Read `arr[topIndex]`. Size? `topIndex + 1`. The whole stack interface, three integers and a one-dimensional buffer, no allocator dance per operation, no pointer chasing — just contiguous memory the CPU's prefetcher loves.

Two reasons array-backed stacks dominate in practice: **cache locality** (sequential access patterns blow linked-list stacks out of the water on real hardware), and **predictable cost** (no per-operation `malloc`, no fragmentation). The trade-off is a fixed capacity: once the array fills, you either reject new pushes (a *bounded* stack — what we'll build) or pay an occasional O(N) cost to copy into a larger buffer (a *growable* stack — what `std::stack`, Java's `ArrayDeque`, and Python's `list` do under the hood).

This lesson builds the bounded version end-to-end in Python and Java, then closes with a beautiful interview question — *can you fit two stacks into a single array of length N without wasting any slots?* The answer is yes, and the trick is one of those "wait, that's clever" moments worth carrying around.

---

## Table of contents

1. [Understanding the problem](#understanding-the-problem)
2. [Structure of an array-based stack](#structure-of-an-array-based-stack)
3. [Supported operations](#supported-operations)
4. [Internal mechanics](#internal-mechanics)
5. [Implementing the stack class](#implementing-the-stack-class)
6. [Determining the size of the stack](#determining-the-size-of-the-stack)
7. [Checking if the stack is empty](#checking-if-the-stack-is-empty)
8. [Accessing the top of the stack](#accessing-the-top-of-the-stack)
9. [Pushing an item onto the stack](#pushing-an-item-onto-the-stack)
10. [Popping an item from the stack](#popping-an-item-from-the-stack)
11. [Working example](#working-example)
12. [Design a stack using an array](#design-a-stack-using-an-array)
13. [Design two stacks in an array](#design-two-stacks-in-an-array)
14. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
15. [Production reality](#production-reality)
16. [Quiz](#quiz)
17. [Practice ladder](#practice-ladder)
18. [Further reading](#further-reading)
19. [Cross-links](#cross-links)
20. [Final takeaway](#final-takeaway)

***

# Understanding the Problem

A stack only ever touches one end, which makes an array its ideal backing store. The two operations a stack exposes — push and pop — both act on the **top**, never the middle and never the front. An array gives `O(1)` read and write at any index it already owns. So if the top always lives at a known index, every stack operation is one index computation plus one memory access.

The decision that follows is whether that array can grow. Two designs split here:

- **Bounded stack** — fixed capacity, set once at construction; a push that would exceed it is rejected.
- **Growable stack** — the buffer doubles when full, paying an occasional `O(n)` copy to stay unbounded.

Using the bounded design: a stack created with capacity `4` accepts four pushes, then refuses the fifth. It returns `false` rather than touching memory it does not own. That refusal is deliberate. Writing past the last owned index is the classic out-of-bounds bug — the kind that corrupts neighbouring data or crashes the program. So the key idea is: an array backs a stack perfectly because both only work at one end, and the design choice is whether to cap capacity (this lesson) or pay resize costs to grow (the next lesson).

***

# Structure of an array-based stack

Three fields and a buffer. That's it.

```d2
cls: "Stack (array-backed)" {
  grid-rows: 3
  grid-gap: 0
  a: "arr: fixed-size array of capacity slots"
  t: "topIndex: index of the topmost item (−1 if empty)"
  c: "capacity: max items the stack can hold"
}
```

<p align="center"><strong>An array-backed stack is just three things — the buffer, the top-of-stack index, and the buffer's capacity. Everything else (size, empty, push, pop, peek) is computed from these.</strong></p>

## State information

### Top index

`topIndex` points at the array slot that currently holds the top of the stack. The convention used everywhere in this lesson:

- **Empty** stack ⇒ `topIndex = -1` (no valid index points at anything).
- **One element** stack ⇒ `topIndex = 0`.
- **Full** stack ⇒ `topIndex = capacity - 1`.

```d2
direction: right

arr: "capacity-4 array" {
  grid-columns: 4
  grid-gap: 0
  v0: |md
    **3**

    `0`
  |
  v1: |md
    **5**

    `1`
  |
  v2: |md
    **7**

    `2`
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  v3: |md
    **—**

    `3`
  |
}

tip: "topIndex = 2" {
  shape: oval
}
tip -> arr.v2
```

<p align="center"><strong>Capacity-4 array, three items stored — <code>topIndex = 2</code>. The slot at index 3 is unused but allocated. Push will write at index 3 and bump <code>topIndex</code> to 3; pop will read index 2 and drop <code>topIndex</code> to 1.</strong></p>

### Size

The number of currently-stored items is **`topIndex + 1`**. A separate counter is unnecessary — the index already tells us. This is the single most common identity in array-backed stacks; if it doesn't feel obvious yet, pause and run the numbers on a few example states until it does.

### Capacity

`capacity` is the length of the underlying buffer — the maximum number of items the stack can ever hold. Fixed at construction; checked on every push to detect overflow.

> *Predict before reading on — if <code>topIndex == capacity − 1</code>, what does the next push attempt do? And if <code>topIndex == −1</code>, what does <code>pop()</code> return?*
>
> Push on a full stack rejects (returns false / throws). Pop on an empty stack returns the sentinel `-1` (or throws, depending on convention). These two boundary checks are the only "interesting" code in the entire implementation; everything else is a one-liner.

## Representation in memory

Stacks drawn vertically in textbook diagrams are stored *horizontally* in memory — a single contiguous buffer where the rightmost-used index is the top. This compactness is the whole performance argument: pushing or popping touches one cache line; iterating an N-item stack iterates N adjacent bytes; `realloc` can grow the buffer in place if the OS has room behind it.

```d2
mem: "Memory layout — capacity 6, 3 items" {
  grid-columns: 6
  grid-gap: 0
  m0: |md
    `@1000`

    **3**
  |
  m1: |md
    `@1004`

    **5**
  |
  m2: |md
    `@1008`

    **7**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  m3: |md
    `@1012`

    **—**
  |
  m4: |md
    `@1016`

    **—**
  |
  m5: |md
    `@1020`

    **—**
  |
}

note: |md
  topIndex = 2 → @1008.

  Adjacent bytes; CPU prefetches them for free.
|
note -> mem.m2: "" {style.stroke-dash: 3}
```

<p align="center"><strong>An array-backed stack in actual memory — six 4-byte int slots laid out contiguously. The CPU loads cache lines of 64 bytes, so 16 ints come along for the ride on every push or pop. This is why array stacks beat linked-list stacks in wall-clock time despite identical asymptotic complexity.</strong></p>

***

# Supported Operations

Five operations make up the entire interface, and every one is `O(1)`. The set is deliberately tiny. A stack offers no indexed access, no search, and no middle insertion — those would break the LIFO contract that gives the structure its value. What remains divides into two reads, two mutations, and one query:

| Operation | Time | Space | What it does |
|---|---|---|---|
| `size()` | `O(1)` | `O(1)` | Returns `topIndex + 1` — the count of stored items |
| `empty()` | `O(1)` | `O(1)` | Returns whether `topIndex == -1` |
| `top()` | `O(1)` | `O(1)` | Reads `arr[topIndex]` without removing it (peek) |
| `push(val)` | `O(1)` | `O(1)` | Writes `val` above the current top; returns `false` if full |
| `pop()` | `O(1)` | `O(1)` | Removes and returns the top; returns `-1` if empty |

The two reads differ in intent: `top()` inspects, `pop()` consumes. Using a capacity-`4` stack holding `[3, 5, 7]`, `top()` returns `7` and leaves the stack unchanged, while `pop()` returns `7` and shrinks the stack to `[3, 5]`. So the core insight is: the whole interface is read-or-write at a single tracked index, which is exactly why none of the five operations ever depends on how many items the stack holds.

***

# Internal Mechanics

Every operation is a rule expressed in terms of `topIndex`, and the buffer is the passive storage those rules read and write. The buffer never moves and never resizes in a bounded stack — only the index slides. That single integer is the entire bookkeeping:

- **Push** increments `topIndex`, then writes `arr[topIndex] = val`.
- **Pop** reads `arr[topIndex]`, then decrements `topIndex`.
- **Top** reads `arr[topIndex]` and leaves the index alone.

A popped slot is never erased. Decrementing `topIndex` is enough, because the stack is defined as the values between index `0` and `topIndex`. Anything beyond `topIndex` is invisible to every operation, and the next push that lands there overwrites it. Using a capacity-`3` stack: push `1`, `2`, `3` leaves `topIndex = 2` over buffer `[1, 2, 3]`. One pop drops `topIndex` to `1`, so the buffer still physically reads `[1, 2, 3]` but the trailing `3` is logically gone. So the core insight is: the buffer is passive storage and `topIndex` is the only live state — correctness reduces to keeping that one index on the true top.

***

# Implementing the stack class

We'll build the class incrementally — first the skeleton (constructor + stub methods), then fill in size, empty, top, push, pop in order. Each operation is a one-liner; the only "logic" is the boundary checks for empty and full.

```d2
cls: "Stack class" {
  grid-columns: 2
  grid-gap: 24
  pub: "public API" {
    grid-rows: 5
    grid-gap: 0
    s: "size()"
    e: "empty()"
    p: "top()"
    psh: "push(val) → bool"
    pop: "pop() → val"
  }
  priv: "private internals" {
    grid-rows: 3
    grid-gap: 0
    a: "arr"
    t: "topIndex"
    c: "capacity"
  }
}
```

<p align="center"><strong>The class as we'll build it — three private fields, five public methods. Encapsulation hides <code>topIndex</code>; callers see only the operations.</strong></p>

## Stack class — skeleton


```python run viz=array viz-root=arr viz-kind=stack
class Stack:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.arr      = [0] * capacity      # fixed-size buffer
        self.top_idx  = -1                  # -1 = empty

    def size(self):  pass
    def empty(self): pass
    def top(self):   pass
    def push(self, val): pass
    def pop(self):   pass

s = Stack(4)
print("created stack with capacity 4")
```

```java run viz=array viz-root=arr viz-kind=stack
public class Main {
    static class Stack {
        private int[] arr;
        private int   capacity;
        private int   topIndex;
        Stack(int capacity) {
            this.capacity = capacity;
            this.arr      = new int[capacity];
            this.topIndex = -1;
        }
        int     size()  { return 0; }
        boolean empty() { return true; }
        int     top()   { return -1; }
        boolean push(int val) { return false; }
        int     pop()   { return -1; }
    }
    public static void main(String[] args) {
        Stack s = new Stack(4);
        System.out.println("created stack with capacity 4");
    }
}
```


***

# Determining the size of the stack

The cleverness of `topIndex = -1 means empty` pays off here: **`size() = topIndex + 1`**. No counter, no traversal, no allocation. One add, return.

```d2
direction: right

e1: |md
  topIndex = -1

  (empty)
|
s1: "size = 0"
e1 -> s1

e2: |md
  topIndex = 0

  (one item)
|
s2: "size = 1"
e2 -> s2

e3: |md
  topIndex = 3

  (four items)
|
s3: "size = 4"
e3 -> s3
```

<p align="center"><strong>Why <code>size() = topIndex + 1</code> works — the indices 0..topIndex hold valid data, so the count of valid entries is <code>topIndex + 1</code>. The −1 sentinel for empty makes the formula uniform across all states (including empty, where 0 + (−1) = 0).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return `topIndex + 1`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import Optional, List, Any

class Stack:
    def __init__(self, capacity: int) -> None:

        # Array to store the stack elements
        self.arr: List[int] = [0] * capacity

        # Maximum capacity of the stack
        self.capacity: int = capacity

        # Index of the top element in the stack
        self.top_index: int = -1

    def size(self) -> int:

        # Size of the stack is the index of the top element plus 1
        return self.top_index + 1
```

```java run
class Stack {

    // Array to store the stack elements
    public int[] arr;

    // Maximum capacity of the stack
    public int capacity;

    // Index of the top element in the stack
    public int topIndex;

    public Stack(int capacity) {
        this.capacity = capacity;

        // Dynamically allocate memory for the stack array
        arr = new int[capacity];

        // Set initial top index to -1 (indicating an empty stack)
        topIndex = -1;
    }

    public int size() {

        // Size of the stack is the index of the top element plus 1
        return topIndex + 1;
    }
}
```

### Complexity Analysis

> **All cases**
>
> -   Time: **O(1)** | Space: **O(1)**

</details>

***

# Checking if the stack is empty

A direct application of the size formula. The stack is empty iff `size() == 0`, equivalently iff `topIndex == -1`.

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
    Q["empty()"] --> CHK{"topIndex == -1?"}
    CHK -->|"yes"| T["true"]
    CHK -->|"no"| F["false"]
```

<p align="center"><strong>Empty check — one comparison. Most callers use this as a guard before <code>top()</code> or <code>pop()</code> to avoid the empty-stack sentinel.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return `topIndex == -1`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import Optional, List, Any

class Stack:
    def __init__(self, capacity: int) -> None:

        # Array to store the stack elements
        self.arr: List[int] = [0] * capacity

        # Maximum capacity of the stack
        self.capacity: int = capacity

        # Index of the top element in the stack
        self.top_index: int = -1

    def size(self) -> int:

        # Size of the stack is the index of the top element plus 1
        return self.top_index + 1

    def empty(self) -> bool:

        # If top index is -1, the stack is empty
        return self.top_index == -1
```

```java run
class Stack {

    // Array to store the stack elements
    public int[] arr;

    // Maximum capacity of the stack
    public int capacity;

    // Index of the top element in the stack
    public int topIndex;

    public Stack(int capacity) {
        this.capacity = capacity;

        // Dynamically allocate memory for the stack array
        arr = new int[capacity];

        // Set initial top index to -1 (indicating an empty stack)
        topIndex = -1;
    }

    public int size() {

        // Size of the stack is the index of the top element plus 1
        return topIndex + 1;
    }

    public boolean empty() {

        // If top index is -1, the stack is empty
        return topIndex == -1;
    }
}
```

### Complexity Analysis

> **All cases** — Time: **O(1)** | Space: **O(1)**

</details>

***

# Accessing the top of the stack

`top()` reads the top element *without removing it*. Two cases:

<details>
<summary><h2>1. Stack is empty</h2></summary>


`topIndex == -1`. Return the sentinel `-1` (or throw, depending on the API). There's no top to return.

</details>
<details>
<summary><h2>2. Stack is not empty</h2></summary>


`topIndex >= 0`. Return `arr[topIndex]`. One memory read, done.

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
    Q["top()"] --> E{"empty?"}
    E -->|"yes"| R1["return -1"]
    E -->|"no"|  R2["return arr[topIndex]"]
```

<p align="center"><strong>Top — one branch on the empty case, one array read on the populated case. Constant-time peek; the stack's contents are unchanged after the call.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If `empty()` is true, return `-1`.
> -   **Step 2:** Return `arr[topIndex]`.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import Optional, List, Any

class Stack:
    def __init__(self, capacity: int) -> None:

        # Array to store the stack elements
        self.arr: List[int] = [0] * capacity

        # Maximum capacity of the stack
        self.capacity: int = capacity

        # Index of the top element in the stack
        self.top_index: int = -1

    def size(self) -> int:

        # Size of the stack is the index of the top element plus 1
        return self.top_index + 1

    def empty(self) -> bool:

        # If top index is -1, the stack is empty
        return self.top_index == -1

    def top(self) -> int:
        if self.empty():

            # Return -1 if the stack is empty
            return -1

        # Return the element at the top index of the stack
        return self.arr[self.top_index]
```

```java run
class Stack {

    // Array to store the stack elements
    public int[] arr;

    // Maximum capacity of the stack
    public int capacity;

    // Index of the top element in the stack
    public int topIndex;

    public Stack(int capacity) {
        this.capacity = capacity;

        // Dynamically allocate memory for the stack array
        arr = new int[capacity];

        // Set initial top index to -1 (indicating an empty stack)
        topIndex = -1;
    }

    public int size() {

        // Size of the stack is the index of the top element plus 1
        return topIndex + 1;
    }

    public boolean empty() {

        // If top index is -1, the stack is empty
        return topIndex == -1;
    }

    public int top() {
        if (empty()) {

            // Return -1 if the stack is empty
            return -1;
        }

        // Return the element at the top index of the stack
        return arr[topIndex];
    }
}
```

### Complexity Analysis

> **All cases** — Time: **O(1)** | Space: **O(1)**

</details>

***

# Pushing an item onto the stack

Push adds an item to the top. Two cases:

<details>
<summary><h2>1. Stack is full</h2></summary>


`topIndex == capacity - 1`. Reject the push — return `false`. Bounded stacks don't grow.

</details>
<details>
<summary><h2>2. Stack is not full</h2></summary>


Increment `topIndex` and write into the new slot. One increment, one write. Return `true`.

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
    Q["push(9)"] --> F{"topIndex ==<br/>capacity-1?"}
    F -->|"yes"| RJ["return false<br/>(stack full)"]
    F -->|"no"| W["arr[++topIndex] = 9<br/>return true"]
    style RJ fill:#fee2e2,stroke:#ef4444
    style W fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Push — one boundary check, one pre-incremented write. The pre-increment <code>++topIndex</code> bumps the index <em>before</em> using it, which is exactly the right slot to write into.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If `topIndex == capacity - 1`, return `false`.
> -   **Step 2:** Increment `topIndex`, set `arr[topIndex] = val`, return `true`.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import Optional, List, Any

class Stack:
    def __init__(self, capacity: int) -> None:

        # Array to store the stack elements
        self.arr: List[int] = [0] * capacity

        # Maximum capacity of the stack
        self.capacity: int = capacity

        # Index of the top element in the stack
        self.top_index: int = -1

    def size(self) -> int:

        # Size of the stack is the index of the top element plus 1
        return self.top_index + 1

    def empty(self) -> bool:

        # If top index is -1, the stack is empty
        return self.top_index == -1

    def top(self) -> int:
        if self.empty():

            # Return -1 if the stack is empty
            return -1

        # Return the element at the top index of the stack
        return self.arr[self.top_index]

    def push(self, val: int) -> bool:
        if self.top_index == self.capacity - 1:

            # Return False if the stack is already full
            return False

        # Increment top index and add the val to the new top position
        self.top_index += 1
        self.arr[self.top_index] = val

        # Return True to indicate successful push operation
        return True
```

```java run
class Stack {

    // Array to store the stack elements
    public int[] arr;

    // Maximum capacity of the stack
    public int capacity;

    // Index of the top element in the stack
    public int topIndex;

    public Stack(int capacity) {
        this.capacity = capacity;

        // Dynamically allocate memory for the stack array
        arr = new int[capacity];

        // Set initial top index to -1 (indicating an empty stack)
        topIndex = -1;
    }

    public int size() {

        // Size of the stack is the index of the top element plus 1
        return topIndex + 1;
    }

    public boolean empty() {

        // If top index is -1, the stack is empty
        return topIndex == -1;
    }

    public int top() {
        if (empty()) {

            // Return -1 if the stack is empty
            return -1;
        }

        // Return the element at the top index of the stack
        return arr[topIndex];
    }

    public boolean push(int val) {
        if (topIndex == capacity - 1) {

            // Return false if the stack is already full
            return false;
        }

        // Increment top index and add the val to the new top position
        arr[++topIndex] = val;

        // Return true to indicate successful push operation
        return true;
    }
}
```

### Complexity Analysis

> **All cases** — Time: **O(1)** | Space: **O(1)**

</details>

***

# Popping an item from the stack

Pop removes and returns the top item. Two cases:

<details>
<summary><h2>1. Stack is empty</h2></summary>


`topIndex == -1`. Return the sentinel `-1`.

</details>
<details>
<summary><h2>2. Stack is not empty</h2></summary>


Read `arr[topIndex]`, decrement `topIndex`, return the read value. The slot we just "freed" is still in memory — there's nothing to physically erase. The next push will overwrite it.

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
    Q["pop()"] --> E{"empty?"}
    E -->|"yes"| R["return -1"]
    E -->|"no"|  ACT["v = arr[topIndex]<br/>topIndex--<br/>return v"]
    style ACT fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Pop — read the slot, decrement the index. The stale value at the old top stays in the buffer but is now <em>logically gone</em> (size and topIndex no longer cover it). Next push overwrites it.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If `empty()`, return `-1`.
> -   **Step 2:** Return `arr[topIndex--]` (read at old top, then decrement).

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run viz=array viz-root=arr viz-kind=stack
class Stack:
    def __init__(self, c): self.capacity, self.arr, self.top_idx = c, [0]*c, -1
    def empty(self): return self.top_idx == -1
    def push(self, v):
        if self.top_idx == self.capacity - 1: return False
        self.top_idx += 1; self.arr[self.top_idx] = v
        return True
    def pop(self):
        if self.empty(): return -1
        v = self.arr[self.top_idx]; self.top_idx -= 1
        return v

s = Stack(3)
s.push(1); s.push(2); s.push(3)
print(s.pop(), s.pop(), s.pop(), s.pop())   # 3 2 1 -1
```

```java run viz=array viz-root=arr viz-kind=stack
public class Main {
    static class Stack {
        private final int[] arr; private final int capacity; private int topIndex;
        Stack(int c){ this.capacity = c; arr = new int[c]; topIndex = -1; }
        boolean empty() { return topIndex == -1; }
        boolean push(int v) {
            if (topIndex == capacity - 1) return false;
            arr[++topIndex] = v;
            return true;
        }
        int pop() {
            if (empty()) return -1;
            return arr[topIndex--];
        }
    }
    public static void main(String[] args) {
        Stack s = new Stack(3);
        s.push(1); s.push(2); s.push(3);
        System.out.println(s.pop() + " " + s.pop() + " " + s.pop() + " " + s.pop());
    }
}
```

### Complexity Analysis

> **All cases** — Time: **O(1)** | Space: **O(1)**

</details>

***

# Working Example

Watching `topIndex` move through a full push-then-pop cycle is the fastest way to make the five operations click. Start with `Stack(3)` — capacity `3`, buffer `[0, 0, 0]`, and `topIndex = -1` marking it empty. Each step below changes exactly one index and at most one slot:

1. **`push(1)`** — not full (`topIndex -1 ≠ capacity-1`), so `topIndex` becomes `0` and `arr[0] = 1`. Buffer `[1, 0, 0]`, `size() == 1`.
2. **`push(2)`** — `topIndex` becomes `1`, `arr[1] = 2`. Buffer `[1, 2, 0]`, `size() == 2`.
3. **`push(3)`** — `topIndex` becomes `2`, `arr[2] = 3`. Buffer `[1, 2, 3]`, `size() == 3` — the stack is now full (`topIndex == capacity-1`).
4. **`pop()`** — not empty, so read `arr[2] == 3`, drop `topIndex` to `1`, return `3`. Buffer still reads `[1, 2, 3]`, but the trailing `3` is logically gone.
5. **`pop()`** — read `arr[1] == 2`, drop `topIndex` to `0`, return `2`.
6. **`pop()`** — read `arr[0] == 1`, drop `topIndex` to `-1`, return `1` — the stack is empty again.
7. **`pop()`** — `empty()` is now true, so return the sentinel `-1` without touching the buffer.

The output sequence is `3 2 1 -1` — the three values in reverse insertion order, then the empty sentinel. That reversal is LIFO made literal. The last value pushed (`3`) is the first popped; the first value pushed (`1`) waits at the bottom until everything above it is gone. So the core insight is: a push-pop cycle is a mirrored climb and descent of `topIndex`, and the values return in exactly reverse order regardless of capacity.

***

# Design a stack using an array

## Problem Statement

Implement a `Stack` class with the operations from this lesson, backed by an array.

> -   **`Stack(int capacity)`** — initialise with the given capacity.
> -   **`size()`** — current size.
> -   **`empty()`** — is the stack empty?
> -   **`top()`** — value at the top, or `-1` if empty.
> -   **`push(int val)`** — push onto the top; return `true` on success, `false` if full.
> -   **`pop()`** — pop and return the top, or `-1` if empty.

> **Constraint:** Use an **array** as the internal data structure.

> **Example:**
>
> -   **Input ops:** `[Stack, push, push, top, empty, pop, top, push, push, empty]`
> -   **Input args:** `[[2], [2], [3], [], [], [], [], [8], [9], []]`
> -   **Output:** `[null, true, true, 3, false, 3, 2, true, false, false]`

<details>
<summary><h2>Solution</h2></summary>


The full implementation is exactly what we built incrementally above, in Python and Java.


```python run viz=array viz-root=arr viz-kind=stack
from typing import Optional, List, Any

class Stack:
    def __init__(self, capacity: int) -> None:

        # Array to store the stack elements
        self.arr: List[int] = [0] * capacity

        # Maximum capacity of the stack
        self.capacity: int = capacity

        # Index of the top element in the stack
        self.top_index: int = -1

    def size(self) -> int:

        # Size of the stack is the index of the top element plus 1
        return self.top_index + 1

    def empty(self) -> bool:

        # If top index is -1, the stack is empty
        return self.top_index == -1

    def top(self) -> int:
        if self.empty():

            # Return -1 if the stack is empty
            return -1

        # Return the element at the top index of the stack
        return self.arr[self.top_index]

    def push(self, val: int) -> bool:
        if self.top_index == self.capacity - 1:

            # Return False if the stack is already full
            return False

        # Increment top index and add the val to the new top position
        self.top_index += 1
        self.arr[self.top_index] = val

        # Return True to indicate successful push operation
        return True

    def pop(self) -> int:
        if self.empty():

            # Return -1 if the stack is empty (nothing to pop)
            return -1

        # Return the element at the top index and decrement top index
        val = self.arr[self.top_index]
        self.top_index -= 1
        return val


# Example from the problem statement
s = Stack(2)
print(s.push(2))   # True
print(s.push(3))   # True
print(s.top())     # 3
print(s.empty())   # False
print(s.pop())     # 3
print(s.top())     # 2
print(s.push(8))   # True
print(s.push(9))   # False — stack is full
print(s.empty())   # False
```

```java run viz=array viz-root=arr viz-kind=stack
import java.util.*;

public class Main {
    static class Stack {

        // Array to store the stack elements
        private int[] arr;

        // Maximum capacity of the stack
        private int capacity;

        // Index of the top element in the stack
        private int topIndex;

        public Stack(int capacity) {
            this.capacity = capacity;

            // Dynamically allocate memory for the stack array
            arr = new int[capacity];

            // Set initial top index to -1 (indicating an empty stack)
            topIndex = -1;
        }

        public int size() {

            // Size of the stack is the index of the top element plus 1
            return topIndex + 1;
        }

        public boolean empty() {

            // If top index is -1, the stack is empty
            return topIndex == -1;
        }

        public int top() {
            if (empty()) {

                // Return -1 if the stack is empty
                return -1;
            }

            // Return the element at the top index of the stack
            return arr[topIndex];
        }

        public boolean push(int val) {
            if (topIndex == capacity - 1) {

                // Return false if the stack is already full
                return false;
            }

            // Increment top index and add the val to the new top position
            arr[++topIndex] = val;

            // Return true to indicate successful push operation
            return true;
        }

        public int pop() {
            if (empty()) {

                // Return -1 if the stack is empty (nothing to pop)
                return -1;
            }

            // Return the element at the top index and decrement top index
            return arr[topIndex--];
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        Stack s = new Stack(2);
        System.out.println(s.push(2));   // true
        System.out.println(s.push(3));   // true
        System.out.println(s.top());     // 3
        System.out.println(s.empty());   // false
        System.out.println(s.pop());     // 3
        System.out.println(s.top());     // 2
        System.out.println(s.push(8));   // true
        System.out.println(s.push(9));   // false — stack is full
        System.out.println(s.empty());   // false
    }
}
```

</details>


***

# Design two stacks in an array

## Problem Statement

Pack **two stacks** into a single shared array. The two stacks together must use no more total capacity than the array provides — and ideally, neither stack should run out of room while the *other* one still has free slots.

> -   **`TwoStack(int capacity)`** — initialise; total capacity shared between the two stacks.
> -   **`top1()`, `top2()`** — read the top of stack 1 / stack 2 (or `-1` if empty).
> -   **`push1(int val)`, `push2(int val)`** — push onto stack 1 / stack 2; return `false` if no room.
> -   **`pop1()`, `pop2()`** — pop from stack 1 / stack 2; return `-1` if empty.

> **Constraint:** Use a **single array** to back both stacks. No second array, no linked nodes — one buffer, two stacks.

<details>
<summary><h2>The Twist — *grow them toward each other*</h2></summary>


The naïve approach is to split the array down the middle: stack 1 owns indices `[0..N/2-1]`, stack 2 owns `[N/2..N-1]`. That works, but it wastes capacity — if stack 1 is full and stack 2 is empty, you can't push onto stack 1 even though half the array is free.

The clever approach: let stack 1 grow **rightward from index 0** (top1 starts at −1) and stack 2 grow **leftward from index capacity−1** (top2 starts at `capacity`). They meet in the middle, but only when the *combined* size hits the array's length. Either stack can use up to N − 1 of the slots, as long as the other stays small.

```d2
direction: right

arr: "two stacks in one array (capacity 5)" {
  grid-columns: 5
  grid-gap: 0
  v0: |md
    **3**

    `0`
  | {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  v1: |md
    **5**

    `1`
  | {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
  v2: |md
    **—**

    `2`
  |
  v3: |md
    **7**

    `3`
  | {style.fill: "#ede9fe"; style.stroke: "#7c3aed"}
  v4: |md
    **11**

    `4`
  | {style.fill: "#ede9fe"; style.stroke: "#7c3aed"}
}

t1: "top1 = 1" { shape: oval; style.fill: "#dbeafe"; style.stroke: "#3b82f6" }
t2: "top2 = 3" { shape: oval; style.fill: "#ede9fe"; style.stroke: "#7c3aed" }

t1 -> arr.v1
t2 -> arr.v3
```

<p align="center"><strong>Two stacks in one array — stack 1 (blue) grows right from index 0; stack 2 (purple) grows left from index capacity−1. They collide only when <code>top1 + 1 == top2</code>, which means the array is genuinely full.</strong></p>

The "full" check changes from "is *my* stack full?" to "are the two tops about to overlap?":

- **`push1` is rejected when** `topIndex1 + 1 >= topIndex2` (no room to advance into).
- **`push2` is rejected when** `topIndex2 - 1 <= topIndex1` (same condition, mirrored).

Initial sentinels:
- `topIndex1 = -1` → stack 1 empty (would-write at index 0 next).
- `topIndex2 = capacity` → stack 2 empty (would-write at index capacity−1 next).

> *Predict before reading on — with capacity 6, all 6 in stack 1, none in stack 2: top1 = ?, top2 = ?, and what does push2 do?*
>
> top1 = 5, top2 = 6 (still its empty sentinel). push2 checks `top2 - 1 <= top1` → `5 <= 5` → true → reject. The array is fully consumed by stack 1, so stack 2 has no room. The check is exactly the same shape as a single-stack "full" check, generalised to the meeting point of two stacks.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run viz=array viz-root=arr viz-kind=stack
from typing import List

class TwoStack:
    def __init__(self, capacity: int) -> None:

        # Array to store elements
        self.arr: List[int] = [0] * capacity

        # Capacity of the array
        self.capacity: int = capacity

        # Top index of the first stack
        self.top_index_1: int = -1

        # Top index of the second stack
        self.top_index_2: int = capacity

    def top1(self) -> int:
        if self.top_index_1 == -1:

            # Stack 1 is empty, return -1
            return -1

        # Return the element at the top of Stack 1
        return self.arr[self.top_index_1]

    def top2(self) -> int:
        if self.top_index_2 == self.capacity:

            # Stack 2 is empty, return -1
            return -1

        # Return the element at the top of Stack 2
        return self.arr[self.top_index_2]

    def push1(self, val: int) -> bool:
        if self.top_index_1 + 1 >= self.top_index_2:

            # Stack 1 is full, cannot push more elements
            return False

        # Increment top index of Stack 1 and assign val to that position
        self.top_index_1 += 1
        self.arr[self.top_index_1] = val

        # Push operation was successful
        return True

    def push2(self, val: int) -> bool:
        if self.top_index_2 - 1 <= self.top_index_1:

            # Stack 2 is full, cannot push more elements
            return False

        # Decrement top index of Stack 2 and assign val to that position
        self.top_index_2 -= 1
        self.arr[self.top_index_2] = val

        # Push operation was successful
        return True

    def pop1(self) -> int:
        if self.top_index_1 == -1:

            # Stack 1 is empty, cannot pop any element, return -1
            return -1

        # Return the element at the top of Stack 1 and decrement top
        # index
        result = self.arr[self.top_index_1]
        self.top_index_1 -= 1
        return result

    def pop2(self) -> int:
        if self.top_index_2 == self.capacity:

            # Stack 2 is empty, cannot pop any element, return -1
            return -1

        # Return the element at the top of Stack 2 and increment top
        # index
        result = self.arr[self.top_index_2]
        self.top_index_2 += 1
        return result


# Example from the problem statement
ts = TwoStack(6)
print(ts.push1(2))   # True
print(ts.push2(3))   # True
print(ts.pop1())     # 2
print(ts.pop2())     # 3
print(ts.top1())     # -1
print(ts.top2())     # -1
print(ts.push1(8))   # True
print(ts.push1(9))   # True
print(ts.top1())     # 9
```

```java run viz=array viz-root=arr viz-kind=stack
import java.util.*;

public class Main {
    static class TwoStack {

        // Array to store elements
        private int[] arr;

        // Capacity of the array
        private int capacity;

        // Top index of the first stack
        private int topIndex1;

        // Top index of the second stack
        private int topIndex2;

        public TwoStack(int capacity) {
            this.capacity = capacity;
            arr = new int[capacity];

            // Initialize top index of the first stack as -1 (empty)
            topIndex1 = -1;

            // Initialize top index of the second stack as capacity (empty)
            topIndex2 = capacity;
        }

        public int top1() {
            if (topIndex1 == -1) {

                // Stack 1 is empty, return -1
                return -1;
            }

            // Return the element at the top of Stack 1
            return arr[topIndex1];
        }

        public int top2() {
            if (topIndex2 == capacity) {

                // Stack 2 is empty, return -1
                return -1;
            }

            // Return the element at the top of Stack 2
            return arr[topIndex2];
        }

        public boolean push1(int val) {
            if (topIndex1 + 1 >= topIndex2) {

                // Stack 1 is full, cannot push more elements
                return false;
            }

            // Increment top index of Stack 1 and assign val to that position
            arr[++topIndex1] = val;

            // Push operation was successful
            return true;
        }

        public boolean push2(int val) {
            if (topIndex2 - 1 <= topIndex1) {

                // Stack 2 is full, cannot push more elements
                return false;
            }

            // Decrement top index of Stack 2 and assign val to that position
            arr[--topIndex2] = val;

            // Push operation was successful
            return true;
        }

        public int pop1() {
            if (topIndex1 == -1) {

                // Stack 1 is empty, cannot pop any element
                return -1;
            }

            // Return the element at the top of Stack 1 and decrement top
            // index
            return arr[topIndex1--];
        }

        public int pop2() {
            if (topIndex2 == capacity) {

                // Stack 2 is empty, cannot pop any element
                return -1;
            }

            // Return the element at the top of Stack 2 and increment top
            // index
            return arr[topIndex2++];
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        TwoStack ts = new TwoStack(6);
        System.out.println(ts.push1(2));   // true
        System.out.println(ts.push2(3));   // true
        System.out.println(ts.pop1());     // 2
        System.out.println(ts.pop2());     // 3
        System.out.println(ts.top1());     // -1
        System.out.println(ts.top2());     // -1
        System.out.println(ts.push1(8));   // true
        System.out.println(ts.push1(9));   // true
        System.out.println(ts.top1());     // 9
    }
}
```

</details>
<details>
<summary><h2>Wrapping Up the Two-Stack Trick</h2></summary>


The two-stack design earns its place because it packs two stacks into one buffer with zero wasted slots. Every operation stays `O(1)` time and the whole structure stays `O(n)` space in the array length — the same costs as a single stack, now shared. Three points carry the design:

1. **Treat the last index as the top.** With `topIndex = -1` for empty, `size = topIndex + 1` is the identity that drives every other formula.
2. **Boundary checks are the only logic.** Pop-on-empty and push-on-full are the only conditions that are not trivially constant time. Everything else is one increment plus one read or write.
3. **Bidirectional growth packs two stacks into one array.** Stack 1 grows rightward from index `0`; stack 2 grows leftward from index `capacity-1`. Both end at the meeting point, so either stack can consume up to `N-1` slots while the other stays small — far more flexible than splitting the buffer in half.

</details>

***

# Edge Cases and Pitfalls

The array-backed stack fits on one screen, yet most of its bugs cluster around the two boundary indices: `-1` for empty and `capacity-1` for full. Keep this list open the next time a stack misbehaves at the edges:

- **Off-by-one on the full check.** The stack is full when `topIndex == capacity - 1`, not when `topIndex == capacity`. Writing `arr[topIndex + 1]` past a full stack indexes one slot beyond the buffer. That is an `IndexError` in Python, an `ArrayIndexOutOfBoundsException` in Java, or silent memory corruption in C.
- **The `-1` sentinel collides with real data.** Returning `-1` from `top()` or `pop()` to mean "empty" is only safe when `-1` cannot be a stored value. If the stack holds arbitrary integers, `-1` is ambiguous. Prefer an explicit `empty()` guard before every `pop()`, or raise an exception instead of returning a sentinel.
- **Popping does not clear the slot.** A popped value stays physically in the buffer until the next push overwrites it. For plain integers this is harmless, but a stack of object references keeps those objects alive and uneligible for garbage collection. In Java, null the slot (`arr[topIndex--] = null`) when storing references.
- **Forgetting that capacity is fixed.** A bounded stack rejects the push when full; it does not grow. Code that assumes pushes always succeed will silently drop data when `push()` returns `false` and the caller ignores the result. Always check the boolean return.
- **Pushing before checking, popping before checking.** The full check must run before the increment-and-write, and the empty check before the read-and-decrement. Reordering them — incrementing `topIndex` first, then testing — leaves the index in a corrupt state when the boundary is hit.
- **Two-stack collision check is mirrored, not identical.** In the shared-array design, `push1` rejects when `topIndex1 + 1 >= topIndex2` and `push2` rejects when `topIndex2 - 1 <= topIndex1`. Copying one condition into the other without flipping the comparison lets the two stacks overwrite each other at the meeting point.

***

# Production Reality

The array-backed stack is the default stack everywhere a maximum depth is known or memory must stay contiguous. The systems below are worth knowing by name.

**[The CPython interpreter's value stack]** — uses **a contiguous array per frame, indexed by a stack pointer** — because bytecode evaluation pushes and pops operands millions of times per second, and a contiguous buffer keeps every push and pop a single `O(1)` pointer move with no allocation.

**[The JVM's operand stack]** — uses **a fixed-size array sized at method-compile time** — because the maximum depth is computed by the verifier ahead of execution, so a bounded array is provably safe and needs no resize check on the hot path.

**[Java's `ArrayDeque` used as a stack]** — uses **a growable circular array behind `push` / `pop`** — because the standard library wants `O(1)` amortised stack operations with far better cache locality than the legacy `Stack` class built on `Vector`.

**[A recursion-to-iteration rewrite]** — uses **an explicit array-backed stack of saved state** — because converting deep recursion to a manual loop avoids blowing the call stack, and a pre-sized array bounds the worst-case memory exactly.

**[An expression evaluator]** — uses **an array of operands and an array of operators** — because postfix and infix evaluation are pure LIFO workloads, and the maximum depth is bounded by the expression length known up front.

**[A backtracking solver's path stack]** — uses **a fixed array sized to the search depth** — because the recursion depth is capped by the problem (board size, word length), so a bounded array never needs to grow and never wastes a heap allocation per step.

***

# Quiz

Test your grip before moving on. One answer per question; reveal only after you have committed to one.

**[Recall] Q: For a stack with `topIndex = 4`, what does `size()` return, and what is the formula?**
`size()` returns `5`, computed as `topIndex + 1` — the indices `0` through `topIndex` all hold valid data.

**[Recall] Q: What two index values are the empty and full sentinels for a stack of `capacity` slots?**
Empty is `topIndex == -1` (no valid index); full is `topIndex == capacity - 1` (the last owned slot is occupied).

**[Reasoning] Q: Why does `pop()` not need to erase the value it removes from the buffer?**
The stack is defined as the values from index `0` to `topIndex`, so dropping `topIndex` makes the old top invisible to every operation, and the next push overwrites that slot anyway.

**[Reasoning] Q: In the two-stack design, why do both stacks share a single "collision" check instead of each having its own "full" check?**
Neither stack has a fixed boundary — they grow toward each other. "Full" therefore means the two tops are about to overlap (`topIndex1 + 1 >= topIndex2`), which lets either stack use up to `N-1` slots.

**[Tradeoff] Q: When would you choose a bounded array-backed stack over a growable one, and what do you give up?**
Choose bounded when the maximum depth is known and per-operation latency must be predictable. You give up the freedom to exceed capacity, since a full push is rejected rather than triggering an `O(n)` resize-and-copy.

***

# Practice Ladder

Five problems that exercise the stack as a tool, easiest first. Try each unaided; hit the hint after ten minutes; do not peek at solutions until you have written something runnable.

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | [Stack Inversion](./08-pattern-reversal/02-problems/01-stack-inversion.md) | [Reversal](./08-pattern-reversal/01-pattern.md) | Easy | Pop every item off one stack and push it onto a second — the order flips for free. `O(n)` time, `O(n)` space. |
| 2 | [Reverse the String](./08-pattern-reversal/02-problems/02-reverse-the-string.md) | [Reversal](./08-pattern-reversal/01-pattern.md) | Easy | Push each character, then pop them all — LIFO reverses the sequence. The stack is the whole algorithm. |
| 3 | [Parentheses Checker](./11-pattern-sequence-validation/02-problems/01-parentheses-checker.md) | [Sequence Validation](./11-pattern-sequence-validation/01-pattern.md) | Easy | Push every opener; on a closer, pop and check it matches. A leftover or mismatched pop means unbalanced. `O(n)` time. |
| 4 | [Preceding Superior Element](./09-pattern-previous-closest-occurrence/02-problems/01-preceding-superior-element.md) | [Previous Closest Occurrence](./09-pattern-previous-closest-occurrence/01-pattern.md) | Medium | Keep a stack of candidates; pop everything smaller than the current value before reading the answer off the top. Monotonic-stack reflex. |
| 5 | [Largest Rectangle Area](./10-pattern-next-closest-occurrence/02-problems/07-largest-rectangle-area.md) | [Next Closest Occurrence](./10-pattern-next-closest-occurrence/01-pattern.md) | Hard | Maintain an increasing stack of bar indices; when a shorter bar arrives, pop and compute the area each popped bar bounds. `O(n)` with one pass. |

Once these feel automatic, push and pop have stopped being syntax and become a structural reflex — exactly what the stack pattern chapters build on.

***

# Further Reading

Curated paths in, not a syllabus. Read in order of the annotation; come back for the rest when you need depth.

- **[CLRS — Chapter 10.1: Stacks and Queues](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)**
  ★ Essential — the canonical array-backed stack with `PUSH` / `POP` and the `top` index, including the empty- and full-stack error conditions formalised.
- **[CPython `ceval.c` — the bytecode evaluation loop](https://github.com/python/cpython/blob/main/Python/ceval.c)**
  ◆ Advanced — the real value stack that every Python expression runs on; the `PUSH` / `POP` / `TOP` macros over a contiguous per-frame array are this lesson's design at production scale.
- **[Java `ArrayDeque` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/ArrayDeque.java)**
  ◆ Advanced — the growable circular-array stack the JDK recommends over `Stack`; shows how the bounded design extends to amortised `O(1)` growth without losing cache locality.
- **[JVM Specification — §2.6.2 Operand Stacks](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html)**
  → Reference — how a fixed-size operand stack is sized at compile time and verified ahead of execution; the textbook case for a bounded array-backed stack.

***

# Cross-Links

**Prerequisites**

- [Introduction to Stacks](/cortex/data-structures-and-algorithms/linear-structures-stack-introduction-to-stacks) — the LIFO contract and the push / pop / top / empty / size interface this lesson implements over an array.
- [Introduction to Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-introduction) — contiguous layout and `O(1)` indexed access, the two array properties that make the whole implementation constant time.
- [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — what `O(1)` per operation and `O(n)` space in capacity actually mean, and how to read them off the code.

**What comes next**

- [Linked-List Implementation of Stacks](/cortex/data-structures-and-algorithms/linear-structures-stack-linked-list-implementation-of-stacks) — the same five operations with the tradeoff flipped: no fixed capacity and no resize, but one allocation per node and no cache locality.
- [Design Min Stack](/cortex/data-structures-and-algorithms/linear-structures-stack-design-min-stack-design-min-stack) — extends the array-backed stack to report the minimum in `O(1)` by carrying a second stack of running minima.

***

## Final Takeaway

1. **Core mechanic:** treat the last used index as the top, track it in one `topIndex` integer, and every operation becomes `O(1)` time and `O(1)` extra space — `size()` is `topIndex + 1`, push increments then writes, pop reads then decrements.
2. **Dominant tradeoff:** you gain cache-friendly contiguous storage and predictable per-operation cost with no allocation; you give up the freedom to grow, since a bounded stack rejects a push once `topIndex == capacity - 1`.
3. **One thing to remember:** the buffer is passive storage and `topIndex` is the only live state — keep that index pointing at the true top after every mutation and the entire stack is correct.
