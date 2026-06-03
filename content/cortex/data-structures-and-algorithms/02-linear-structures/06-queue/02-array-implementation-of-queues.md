---
title: "Array Implementation Of Queues"
summary: "A bounded FIFO on a fixed buffer: track a front index and a back index, wrap both with one modulo, and enqueue, dequeue, and peek all collapse to O(1) over contiguous, cache-friendly memory."
---

# 2. Array Implementation of Queues

## The Hook

A queue needs to enqueue, dequeue, and peek in **O(1)** — and once again the array is the obvious tool. Treat one index as the *front*, another as the *back*, and the whole interface collapses to integer arithmetic and a single array store. Enqueue? Bump back, write `arr[back]`. Dequeue? Read `arr[front]`, bump front. Size, front, back? One field each.

There's a wrinkle this time. A stack only needs one moving index — the top. A queue needs *two*, and **both march forward** as data flows through. Enqueue advances the back; dequeue advances the front. Run a queue long enough and the back hits the end of the array while the *front* is still in the middle, leaving a graveyard of vacated slots at index 0..front−1 that the naïve algorithm cannot reuse. You'd run out of room while the queue is half empty.

The fix is one of the prettiest tricks in introductory data structures: **treat the array as a circle**. When the back hits the last index, the next enqueue wraps around to index 0 and keeps going. The same trick on dequeue. One modulo (`(idx + 1) % capacity`) per operation, and the array's vacated slots are reusable forever. The result is called a **circular** (or *ring*) **buffer**, and it powers everything from kernel I/O ring buffers to Linux `kfifo`, audio pipelines, networking stacks, and Disruptor-style high-frequency-trading queues. Ten lines of code, multibillion-dollar applications.

This lesson builds that circular queue end-to-end in Python and Java, deriving the modulo trick from first principles and showing exactly why every operation is still O(1).

---

## Table of contents

1. [Understanding the problem](#understanding-the-problem)
2. [Structure of an array-based queue](#structure-of-an-array-based-queue)
3. [The cyclic nature of array-based queues](#the-cyclic-nature-of-array-based-queues)
4. [Supported operations](#supported-operations)
5. [Internal mechanics](#internal-mechanics)
6. [Implementing the queue class](#implementing-the-queue-class)
7. [Determining the size of the queue](#determining-the-size-of-the-queue)
8. [Checking if the queue is empty](#checking-if-the-queue-is-empty)
9. [Accessing the front of the queue](#accessing-the-front-of-the-queue)
10. [Accessing the back of the queue](#accessing-the-back-of-the-queue)
11. [Enqueuing an item into the queue](#enqueuing-an-item-into-the-queue)
12. [Dequeuing an item from the queue](#dequeuing-an-item-from-the-queue)
13. [Working example](#working-example)
14. [Design a queue using a circular array](#design-a-queue-using-a-circular-array)
15. [Boss-fight demo](#boss-fight-demo)
16. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
17. [Production reality](#production-reality)
18. [Quiz](#quiz)
19. [Practice ladder](#practice-ladder)
20. [Further reading](#further-reading)
21. [Cross-links](#cross-links)
22. [Final takeaway](#final-takeaway)

***

# Understanding the Problem

A queue touches both ends, so an array backs it well only once you fix where each end lives. The two operations a queue exposes act on opposite ends: enqueue adds at the **back**, dequeue removes from the **front**. An array gives `O(1)` read and write at any index it owns. So if a `frontIndex` always marks the oldest item and a `backIndex` always marks the newest, every queue operation is one index computation plus one memory access.

A second decision follows, and it splits the same way it did for the stack:

- **Bounded queue** — fixed capacity, set once at construction; an enqueue that would exceed it is rejected.
- **Growable queue** — the buffer reallocates when full, paying an occasional `O(n)` copy to stay unbounded.

There is a wrinkle the stack never had. Both indices move *forward*, never back, so a long-running queue marches its live region down the array and strands the slots it leaves behind. Using a capacity-`6` queue: enqueue six items, dequeue three, and the front sits at index `3` while indices `0`–`2` are vacated but unreachable by a naïve scheme. The queue would report itself full at half capacity. So the key idea is: an array backs a queue well because both work at fixed ends, but the forward march of two indices forces one extra trick — treat the array as a circle so vacated slots become reusable.

***

# Structure of an array-based queue

Four fields and a buffer. That's it.

```d2
cls: "Queue (circular-array-backed)" {
  arr: "arr: fixed-size array of capacity slots"
  fidx: "frontIndex: index of the front item (0 when empty, by convention)"
  bidx: "backIndex: index of the back item (-1 when empty, by convention)"
  size: "currentSize: number of items currently in the queue"
  cap: "capacity: max items the queue can hold"
}
```

<p align="center"><strong>An array-backed queue is just five things — the buffer, two index pointers, the size counter, and the capacity. Everything else (empty, full, enqueue, dequeue, front, back) is computed from these.</strong></p>

## State information

### Front index

`frontIndex` points at the array slot that currently holds the front of the queue (the oldest item). Convention used throughout this lesson:

- **Empty** queue ⇒ `frontIndex = 0` (will become valid as soon as the first item is enqueued).
- **Non-empty** queue ⇒ `frontIndex` ∈ `[0, capacity − 1]`, points at the oldest item.

`frontIndex` only ever advances — every dequeue moves it forward (cyclically). Enqueue never touches it.

### Back index

`backIndex` points at the array slot that currently holds the back of the queue (the newest item). Convention:

- **Empty** queue ⇒ `backIndex = -1` (no item to point at).
- **Non-empty** queue ⇒ `backIndex` ∈ `[0, capacity − 1]`, points at the newest item.

`backIndex` only ever advances — every enqueue moves it forward (cyclically). Dequeue never touches it.

### Size

`currentSize` is the count of items currently in the queue, stored as a standalone counter rather than derived from the indices. The reason it must be standalone is the wrap-around. After the queue wraps, two states look identical from the indices alone:

- **Empty** — `frontIndex == backIndex + 1 (mod capacity)` with nothing between them.
- **Full** — `frontIndex == backIndex + 1 (mod capacity)` with every slot occupied.

Using a capacity-`6` queue: front at `3` and back at `2` describes both a zero-item queue and a six-item queue, and `backIndex < frontIndex` here is normal, not a bug. So the key idea is: a separate `currentSize` counter is the only way to tell empty from full in `O(1)` once the indices can wrap past each other.

### Capacity

The length of the underlying buffer. Fixed at construction. Used to detect overflow (refuse enqueue when `currentSize == capacity`) and to compute the modulo wrap-around.

```d2
arr: "capacity-6 array, three items at indices 1, 2, 3" {
  grid-columns: 6
  grid-gap: 0
  e0: |md
    `0`

    "—"
  |
  e1: |md
    `1`

    **3**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  e2: |md
    `2`

    5
  |
  e3: |md
    `3`

    **7**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  e4: |md
    `4`

    "—"
  |
  e5: |md
    `5`

    "—"
  |
}

state: "frontIndex = 1, backIndex = 3, currentSize = 3, capacity = 6" {
  shape: text
}
state -> arr
```

<p align="center"><strong>Capacity-6 array, three items stored at indices 1, 2, 3 — front at 1, back at 3. Indices 0 and 4–5 are unused but allocated. The next enqueue writes at index 4 and bumps back to 4; the next dequeue returns the value at index 1 and bumps front to 2.</strong></p>

> *Predict before reading on — given the state above, what happens after two more enqueues and two more dequeues?*
>
> Enqueue: write at 4 (back→4), then write at 5 (back→5). Now `[—, 3, 5, 7, e1, e2]`, front=1, back=5, size=5.
> Dequeue twice: return arr[1]=3 (front→2), return arr[2]=5 (front→3). Now `[—, —, —, 7, e1, e2]`, front=3, back=5, size=3.
> Notice that indices 0–2 are now "stranded" — the back will hit index 5 and want to wrap. The next section is exactly about that.

***

# The cyclic nature of array-based queues

Here's the problem and the trick that fixes it.

## The problem — both ends march forward

In an array-backed *stack*, only one index moves (the top), and it bounces up and down between 0 and `capacity − 1`. The array slots are reusable forever; there's never any wasted space.

In an array-backed *queue*, **both indices march forward**. Enqueue moves back forward; dequeue moves front forward. Eventually one of them hits the end of the array. Without a fix, you've now run out of room on the back end *even if the front end has plenty of slack*.

```d2
arr: "naive view: back has hit the last slot" {
  grid-columns: 6
  grid-gap: 0
  e0: |md
    `0`

    "—"
  |
  e1: |md
    `1`

    "—"
  |
  e2: |md
    `2`

    "—"
  |
  e3: |md
    `3`

    **7**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  e4: |md
    `4`

    11
  |
  e5: |md
    `5`

    **13**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
}

state: "front=3, back=5, size=3, capacity=6 — indices 0-2 stranded" {
  shape: text
}
state -> arr
```

<p align="center"><strong>The naïve view — back has hit the last slot, but the front has marched up to 3, leaving indices 0–2 vacated and unused. The queue holds only 3 of 6 capacity, yet a "linear" enqueue would now incorrectly report "full".</strong></p>

## The fix — wrap around

When the back hits the last index, the *next* enqueue should write at index 0 and treat it as the new back. The array becomes a **circle**:

```d2
direction: right

s0: "[0]"
s1: "[1]"
s2: "[2]"
s3: "[3]"
s4: "[4]"
s5: "[5]"

s0 -> s1
s1 -> s2
s2 -> s3
s3 -> s4
s4 -> s5
s5 -> s0: wrap
```

<p align="center"><strong>Treat the array as a ring — index <code>capacity − 1</code>'s "next" is index <code>0</code>, not "out of bounds". A single modulo expression encodes this: <code>nextIndex = (currentIndex + 1) % capacity</code>.</strong></p>

Now the same enqueue that previously failed succeeds — write at index 0, and back becomes 0:

```d2
arr: "after enqueue(17): back wrapped from 5 to 0" {
  grid-columns: 6
  grid-gap: 0
  e0: |md
    `0`

    **17**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  e1: |md
    `1`

    "—"
  |
  e2: |md
    `2`

    "—"
  |
  e3: |md
    `3`

    **7**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  e4: |md
    `4`

    11
  |
  e5: |md
    `5`

    13
  |
}

state: "front=3, back=0, size=4 — back has wrapped" {
  shape: text
}
state -> arr
```

<p align="center"><strong>After enqueueing 17 — back wrapped from 5 to 0 via <code>(5 + 1) % 6 = 0</code>. The queue's <em>logical</em> contents are now <code>[7, 11, 13, 17]</code> in order, even though <em>physically</em> they're stored as <code>[17, —, —, 7, 11, 13]</code>. The wrap is invisible to the caller.</strong></p>

The same wrap applies to dequeue. The expression `(index + 1) % capacity` advances `index` by one, *cycling back to 0* when it would otherwise step past the end. Two ring-buffer pointers, one cheap modulo, and the queue is now a fully circular structure that uses every slot.

> *Predict before reading on — once the queue wraps, what does "queue is full" actually look like? <code>backIndex == capacity − 1</code> can't be the right check anymore.*
>
> Right — `backIndex` could be anywhere on the ring. The clean test is `currentSize == capacity` (which is exactly why we maintain `currentSize` separately from the indices). Without that counter, distinguishing *empty* (back just behind front, nothing in between) from *full* (back just behind front, the gap is full) on a circular buffer requires either reserving one slot as a sentinel or storing a flag. The counter sidesteps that whole genus of bugs.

## Memory layout

Conceptually circular, physically still a flat contiguous array. The `% capacity` operator is the only thing that distinguishes a ring buffer from a normal array — there's no special hardware, no clever pointer arithmetic. Just one modulo per operation.

```d2
mem: "Memory layout — capacity 6, items stored at indices 3, 4, 5, 0" {
  grid-columns: 6
  grid-gap: 0
  m0: |md
    `@1000`

    **17**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  m1: |md
    `@1004`

    "—"
  |
  m2: |md
    `@1008`

    "—"
  |
  m3: |md
    `@1012`

    **7**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  m4: |md
    `@1016`

    11
  |
  m5: |md
    `@1020`

    13
  |
}

note: |md
  Logical order: 7, 11, 13, 17

  Physical order: 17, —, —, 7, 11, 13

  Same contiguous bytes; modulo arithmetic does the rest.
| { shape: text }
note -> mem.m3
```

<p align="center"><strong>A circular queue in actual memory — six 4-byte int slots laid out linearly. The "wrap" is a property of the access pattern, not the storage. The CPU still gets cache locality on each operation; the modulo costs a few nanoseconds.</strong></p>

***

# Supported Operations

Six operations make up the entire interface, and every one is `O(1)` time and `O(1)` space. The set is deliberately tiny. A queue offers no indexed access, no search, and no middle insertion — those would break the FIFO contract that gives the structure its value. What remains divides into one query, two reads, and two mutations, plus a size count:

| Operation | Time | Space | What it does |
|---|---|---|---|
| `size()` | `O(1)` | `O(1)` | Returns `currentSize` — the count of stored items |
| `empty()` | `O(1)` | `O(1)` | Returns whether `currentSize == 0` |
| `front()` | `O(1)` | `O(1)` | Reads `arr[frontIndex]` without removing it (peek) |
| `back()` | `O(1)` | `O(1)` | Reads `arr[backIndex]` without removing it (peek) |
| `enqueue(val)` | `O(1)` | `O(1)` | Writes `val` at the next back slot; returns `false` if full |
| `dequeue()` | `O(1)` | `O(1)` | Removes and returns the front item; returns `-1` if empty |

The two reads sit at opposite ends: `front()` inspects the oldest item, `back()` inspects the newest. Using a capacity-`6` queue holding `[3, 5, 7]` at indices `1`–`3`, `front()` returns `3` and `back()` returns `7`, both leaving the queue unchanged. So the core insight is: the whole interface is read-or-write at one of two tracked indices, which is exactly why none of the six operations ever depends on how many items the queue holds.

***

# Internal Mechanics

Every operation is a rule expressed in terms of `frontIndex`, `backIndex`, and `currentSize`, and the buffer is the passive storage those rules read and write. The buffer never moves and never resizes in a bounded queue — only the two indices slide, always forward, always modulo `capacity`. Those three integers are the entire bookkeeping:

- **Enqueue** advances `backIndex` to `(backIndex + 1) % capacity`, writes `arr[backIndex] = val`, then increments `currentSize`.
- **Dequeue** reads `arr[frontIndex]`, advances `frontIndex` to `(frontIndex + 1) % capacity`, then decrements `currentSize`.
- **Front** and **back** read `arr[frontIndex]` and `arr[backIndex]` and leave every index alone.

A dequeued slot is never erased. Advancing `frontIndex` is enough, because the queue is defined as the values between `frontIndex` and `backIndex` taken cyclically. Anything outside that arc is invisible to every operation, and the next enqueue that wraps around overwrites it. The `currentSize` counter does the one job the indices cannot: it tells *empty* from *full* when `frontIndex` and `backIndex` coincide on the ring. So the core insight is: the buffer is passive storage and the two indices plus the size are the only live state — correctness reduces to advancing each index cyclically and keeping `currentSize` exact.

***

# Implementing the queue class

We'll build the class incrementally — first the skeleton (constructor + stub methods), then fill in `size`, `empty`, `front`, `back`, `enqueue`, `dequeue` in order.

```d2
direction: right

cls: "Queue class" {
  priv: "private internals" {
    arr: arr
    fidx: frontIndex
    bidx: backIndex
    size: currentSize
    cap: capacity
  }
  pub: "public API" {
    sz: "size()"
    em: "empty()"
    fr: "front()"
    bk: "back()"
    enq: "enqueue(val) -> bool"
    deq: "dequeue() -> val"
  }
  pub -> priv
}
```

<p align="center"><strong>The class as we'll build it — five private fields, six public methods. The two index fields plus the modulo arithmetic are the only "interesting" code in the entire implementation.</strong></p>

## Queue class — skeleton


```python run viz=array viz-root=arr
class Queue:
    def __init__(self, capacity: int):
        self.capacity     = capacity
        self.arr          = [0] * capacity
        self.front_index  = 0          # 0 when empty, by convention
        self.back_index   = -1         # -1 when empty
        self.current_size = 0

    def size(self):       pass
    def empty(self):      pass
    def front(self):      pass
    def back(self):       pass
    def enqueue(self, v): pass
    def dequeue(self):    pass

q = Queue(4)
print("created queue with capacity 4")
```

```java run viz=array viz-root=arr
public class Main {
    static class Queue {
        private final int[] arr;
        private final int   capacity;
        private int         frontIndex   = 0;
        private int         backIndex    = -1;
        private int         currentSize  = 0;
        Queue(int capacity) {
            this.capacity = capacity;
            this.arr      = new int[capacity];
        }
        int     size()             { return 0; }
        boolean empty()            { return true; }
        int     front()            { return -1; }
        int     back()             { return -1; }
        boolean enqueue(int val)   { return false; }
        int     dequeue()          { return -1; }
    }
    public static void main(String[] args) {
        Queue q = new Queue(4);
        System.out.println("created queue with capacity 4");
    }
}
```


The skeleton is pure bookkeeping — five fields, six stubs. The next six sections fill in one stub at a time.

***

# Determining the size of the queue

The size operation reports the current number of items in the queue. The work is already done elsewhere: the constructor sets `currentSize = 0`, and `enqueue` and `dequeue` keep it exact as an invariant. The size method is then a one-line read of that counter, costing `O(1)` time and `O(1)` space.

> **Algorithm**
>
> -   **Step 1:** Return the value of `currentSize`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import List

class Queue:
    def __init__(self, capacity):

        # Pointer to the dynamic array representing the queue
        self.arr: List[int] = [None] * capacity

        # Maximum capacity of the queue
        self.capacity: int = capacity

        # Index of the front element in the queue
        self.front_index: int = 0

        # Index of the back element in the queue
        self.back_index: int = -1

        # Current number of elements in the queue
        self.current_size: int = 0

    def size(self):

        # Returns the current size of the queue
        return self.current_size
```

```java run
class Queue {

    // Pointer to the dynamic array representing the queue
    public int[] arr;

    // Maximum capacity of the queue
    public int capacity;

    // Index of the front element in the queue
    public int frontIndex;

    // Index of the back element in the queue
    public int backIndex;

    // Current number of elements in the queue
    public int currentSize;

    public Queue(int capacity) {
        this.capacity = capacity;

        // Allocating memory for the queue
        this.arr = new int[capacity];

        // Initializing front index to 0
        this.frontIndex = 0;

        // Initializing back index to -1
        this.backIndex = -1;

        // Initializing current size to 0
        this.currentSize = 0;
    }

    public int size() {

        // Returns the current size of the queue
        return currentSize;
    }
}
```

### Complexity Analysis

A single field read.

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Checking if the queue is empty

`empty()` returns `true` when there are no items in the queue. The cleanest definition is *"size is zero"* — and since `size` is already O(1), so is `empty`.

> **Algorithm**
>
> -   **Step 1:** Return `true` if `currentSize == 0`, else `false`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import List

class Queue:
    def __init__(self, capacity):

        # Pointer to the dynamic array representing the queue
        self.arr: List[int] = [None] * capacity

        # Maximum capacity of the queue
        self.capacity: int = capacity

        # Index of the front element in the queue
        self.front_index: int = 0

        # Index of the back element in the queue
        self.back_index: int = -1

        # Current number of elements in the queue
        self.current_size: int = 0

    def size(self):

        # Returns the current size of the queue
        return self.current_size

    def empty(self):

        # Returns True if the queue is empty, False otherwise
        return self.size() == 0
```

```java run
class Queue {

    // Pointer to the dynamic array representing the queue
    public int[] arr;

    // Maximum capacity of the queue
    public int capacity;

    // Index of the front element in the queue
    public int frontIndex;

    // Index of the back element in the queue
    public int backIndex;

    // Current number of elements in the queue
    public int currentSize;

    public Queue(int capacity) {
        this.capacity = capacity;

        // Allocating memory for the queue
        this.arr = new int[capacity];

        // Initializing front index to 0
        this.frontIndex = 0;

        // Initializing back index to -1
        this.backIndex = -1;

        // Initializing current size to 0
        this.currentSize = 0;
    }

    public int size() {

        // Returns the current size of the queue
        return currentSize;
    }

    public boolean empty() {

        // Returns true if the queue is empty, false otherwise
        return size() == 0;
    }
}
```

### Complexity Analysis

Calls `size`, returns a comparison.

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Accessing the front of the queue

`front()` returns the value of the oldest item in the queue without removing it. Two cases:

1. **Queue is empty** → return `-1` as a sentinel (production code would throw; we return `-1` for simplicity, matching the lesson's convention).
2. **Queue is non-empty** → return `arr[frontIndex]`.

```d2
arr: "front access" {
  grid-columns: 6
  grid-gap: 0
  e0: |md
    `0`

    "—"
  |
  e1: |md
    `1`

    **3**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  e2: |md
    `2`

    5
  |
  e3: |md
    `3`

    7
  |
  e4: |md
    `4`

    "—"
  |
  e5: |md
    `5`

    "—"
  |
}

note: "front() returns arr[frontIndex] = arr[1] = 3" {
  shape: text
}
note -> arr.e1
```

<p align="center"><strong>front() — read the slot at <code>frontIndex</code>. The queue is unchanged after the call.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If the queue is empty, return `-1`.
> -   **Step 2:** Otherwise return `arr[frontIndex]`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import List

class Queue:
    def __init__(self, capacity):

        # Pointer to the dynamic array representing the queue
        self.arr: List[int] = [None] * capacity

        # Maximum capacity of the queue
        self.capacity: int = capacity

        # Index of the front element in the queue
        self.front_index: int = 0

        # Index of the back element in the queue
        self.back_index: int = -1

        # Current number of elements in the queue
        self.current_size: int = 0

    def size(self):

        # Returns the current size of the queue
        return self.current_size

    def empty(self):

        # Returns True if the queue is empty, False otherwise
        return self.size() == 0

    def front(self):
        if self.empty():

            # Returns -1 if the queue is empty
            return -1

        # Returns the element at the front of the queue
        return self.arr[self.front_index]
```

```java run
class Queue {

    // Pointer to the dynamic array representing the queue
    public int[] arr;

    // Maximum capacity of the queue
    public int capacity;

    // Index of the front element in the queue
    public int frontIndex;

    // Index of the back element in the queue
    public int backIndex;

    // Current number of elements in the queue
    public int currentSize;

    public Queue(int capacity) {
        this.capacity = capacity;

        // Allocating memory for the queue
        this.arr = new int[capacity];

        // Initializing front index to 0
        this.frontIndex = 0;

        // Initializing back index to -1
        this.backIndex = -1;

        // Initializing current size to 0
        this.currentSize = 0;
    }

    public int size() {

        // Returns the current size of the queue
        return currentSize;
    }

    public boolean empty() {

        // Returns true if the queue is empty, false otherwise
        return size() == 0;
    }

    public int front() {
        if (empty()) {

            // Returns -1 if the queue is empty
            return -1;
        }

        // Returns the element at the front of the queue
        return arr[frontIndex];
    }
}
```

### Complexity Analysis

A predicate plus an array indexing — both O(1).

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Accessing the back of the queue

`back()` returns the value of the newest item in the queue without removing it. Same two cases — empty (`-1`) or read `arr[backIndex]`.

```d2
arr: "back access" {
  grid-columns: 6
  grid-gap: 0
  e0: |md
    `0`

    "—"
  |
  e1: |md
    `1`

    3
  |
  e2: |md
    `2`

    5
  |
  e3: |md
    `3`

    **7**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  e4: |md
    `4`

    "—"
  |
  e5: |md
    `5`

    "—"
  |
}

note: "back() returns arr[backIndex] = arr[3] = 7" {
  shape: text
}
note -> arr.e3
```

<p align="center"><strong>back() — read the slot at <code>backIndex</code>. The queue is unchanged after the call.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If the queue is empty, return `-1`.
> -   **Step 2:** Otherwise return `arr[backIndex]`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import List

class Queue:
    def __init__(self, capacity):

        # Pointer to the dynamic array representing the queue
        self.arr: List[int] = [None] * capacity

        # Maximum capacity of the queue
        self.capacity: int = capacity

        # Index of the front element in the queue
        self.front_index: int = 0

        # Index of the back element in the queue
        self.back_index: int = -1

        # Current number of elements in the queue
        self.current_size: int = 0

    def size(self):

        # Returns the current size of the queue
        return self.current_size

    def empty(self):

        # Returns True if the queue is empty, False otherwise
        return self.size() == 0

    def front(self):
        if self.empty():

            # Returns -1 if the queue is empty
            return -1

        # Returns the element at the front of the queue
        return self.arr[self.front_index]

    def back(self):
        if self.empty():

            # Returns -1 if the queue is empty
            return -1

        # Returns the element at the back of the queue
        return self.arr[self.back_index]
```

```java run
class Queue {

    // Pointer to the dynamic array representing the queue
    public int[] arr;

    // Maximum capacity of the queue
    public int capacity;

    // Index of the front element in the queue
    public int frontIndex;

    // Index of the back element in the queue
    public int backIndex;

    // Current number of elements in the queue
    public int currentSize;

    public Queue(int capacity) {
        this.capacity = capacity;

        // Allocating memory for the queue
        this.arr = new int[capacity];

        // Initializing front index to 0
        this.frontIndex = 0;

        // Initializing back index to -1
        this.backIndex = -1;

        // Initializing current size to 0
        this.currentSize = 0;
    }

    public int size() {

        // Returns the current size of the queue
        return currentSize;
    }

    public boolean empty() {

        // Returns true if the queue is empty, false otherwise
        return size() == 0;
    }

    public int front() {
        if (empty()) {

            // Returns -1 if the queue is empty
            return -1;
        }

        // Returns the element at the front of the queue
        return arr[frontIndex];
    }

    public int back() {
        if (empty()) {

            // Returns -1 if the queue is empty
            return -1;
        }

        // Returns the element at the back of the queue
        return arr[backIndex];
    }
}
```

### Complexity Analysis

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Enqueuing an item into the queue

Enqueue is the more interesting operation — the cyclic trick lives here. Two top-level cases:

1. **Queue is full** (`currentSize == capacity`) → reject; return `false`.
2. **Queue is not full** → advance `backIndex` (cyclically), write the value, increment size.

The cyclic advance is the one-liner that does all the work:

```text
backIndex = (backIndex + 1) % capacity
```

When `backIndex` is the last valid index, `(last + 1) % capacity == 0`, wrapping us back to slot 0. When `backIndex` is anywhere else, the modulo is a no-op and the result is `backIndex + 1`. One expression handles both the normal case *and* the wrap-around case — no `if`, no special branches.

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
    subgraph CASE1["case 1: backIndex < capacity − 1 (no wrap)"]
        direction LR
        C1A["state: front=1, back=2, size=2<br/>arr=[—,3,5,—,—,—]"]
        C1B["enqueue(7) → back = (2+1)%6 = 3<br/>arr=[—,3,5,7,—,—], size=3"]
        C1A --> C1B
    end
    subgraph CASE2["case 2: backIndex == capacity − 1 (wrap)"]
        direction LR
        C2A["state: front=3, back=5, size=3<br/>arr=[—,—,—,7,11,13]"]
        C2B["enqueue(17) → back = (5+1)%6 = 0<br/>arr=[17,—,—,7,11,13], size=4"]
        C2A --> C2B
    end
```

<p align="center"><strong>Enqueue branches handled by one modulo — when back+1 is in-bounds, the modulo is a no-op; when back+1 is out-of-bounds, the modulo wraps it to 0. Same line of code, both cases.</strong></p>

> **Algorithm**
>
> -   **Step 1:** If `currentSize == capacity`, return `false` (queue full).
> -   **Step 2:** Update `backIndex = (backIndex + 1) % capacity`.
> -   **Step 3:** Store `arr[backIndex] = val`.
> -   **Step 4:** Increment `currentSize`.
> -   **Step 5:** Return `true`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
def enqueue(self, val):
    if self.current_size == self.capacity: return False
    self.back_index           = (self.back_index + 1) % self.capacity
    self.arr[self.back_index] = val
    self.current_size        += 1
    return True
```

```java run
boolean enqueue(int val) {
    if (currentSize == capacity) return false;
    backIndex            = (backIndex + 1) % capacity;
    arr[backIndex]       = val;
    currentSize++;
    return true;
}
```


> **Note on the Rust signature** — we keep `back_index` as `i32` (signed) so the empty sentinel `-1` fits, then `rem_euclid` gives us the correct non-negative remainder for the wrap.

### Complexity Analysis

A bounds check, a modulo, an array write, an increment. No allocation. No loop.

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Dequeuing an item from the queue

Dequeue is the symmetric operation. Two cases:

1. **Queue is empty** → return `-1`.
2. **Queue is non-empty** → save `arr[frontIndex]`, advance `frontIndex` cyclically, decrement size, return the saved value.

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
    subgraph CASE1["case 1: frontIndex < capacity − 1 (no wrap)"]
        direction LR
        D1A["state: front=1, back=3, size=3<br/>arr=[—,3,5,7,—,—]"]
        D1B["dequeue() → 3<br/>front = (1+1)%6 = 2<br/>arr=[—,3,5,7,—,—] (slot 1 stranded), size=2"]
        D1A --> D1B
    end
    subgraph CASE2["case 2: frontIndex == capacity − 1 (wrap)"]
        direction LR
        D2A["state: front=5, back=1, size=3<br/>arr=[11,13,—,—,—,7]"]
        D2B["dequeue() → 7<br/>front = (5+1)%6 = 0<br/>arr=[11,13,—,—,—,7], size=2"]
        D2A --> D2B
    end
```

<p align="center"><strong>Dequeue mirrors enqueue — one modulo handles both the normal advance and the wrap from index <code>capacity − 1</code> back to <code>0</code>. The vacated slot is left untouched (its value is ignored, never read again until overwritten by a future enqueue).</strong></p>

> **Why don't we zero out the dequeued slot?**
>
> We don't need to. The queue's logical contents are *only* the slots between `frontIndex` and `backIndex` (cyclically). The dequeued slot is now outside that range, so it'll never be read by `front`/`back`/iteration. The next enqueue that comes around will *overwrite* it. Zeroing would just be wasted work — and for non-trivial element types (objects, smart pointers) the production trade-off is between memory pressure (clearing frees referenced memory sooner) and CPU cost (clearing isn't free). The simplest correct queue does no clearing; library queues for reference types may clear to release references.

> **Algorithm**
>
> -   **Step 1:** If `currentSize == 0`, return `-1`.
> -   **Step 2:** Save `dequeued = arr[frontIndex]`.
> -   **Step 3:** Update `frontIndex = (frontIndex + 1) % capacity`.
> -   **Step 4:** Decrement `currentSize`.
> -   **Step 5:** Return `dequeued`.

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from typing import List

class Queue:
    def __init__(self, capacity):

        # Pointer to the dynamic array representing the queue
        self.arr: List[int] = [None] * capacity

        # Maximum capacity of the queue
        self.capacity: int = capacity

        # Index of the front element in the queue
        self.front_index: int = 0

        # Index of the back element in the queue
        self.back_index: int = -1

        # Current number of elements in the queue
        self.current_size: int = 0

    def size(self):

        # Returns the current size of the queue
        return self.current_size

    def empty(self):

        # Returns True if the queue is empty, False otherwise
        return self.size() == 0

    def front(self):
        if self.empty():

            # Returns -1 if the queue is empty
            return -1

        # Returns the element at the front of the queue
        return self.arr[self.front_index]

    def back(self):
        if self.empty():

            # Returns -1 if the queue is empty
            return -1

        # Returns the element at the back of the queue
        return self.arr[self.back_index]

    def enqueue(self, val):
        if self.current_size == self.capacity:

            # Returns False if the queue is full and cannot enqueue more
            # elements
            return False

        # Calculates the next back index in a circular manner
        self.back_index = (self.back_index + 1) % self.capacity

        # Inserts the new element at the back of the queue
        self.arr[self.back_index] = val

        # Increments the current size
        self.current_size += 1

        # Returns True to indicate a successful enqueue operation
        return True

    def dequeue(self):
        if self.empty():

            # Returns -1 if the queue is empty and cannot dequeue
            # elements
            return -1

        # Stores the element to be dequeued
        dequeued_element = self.arr[self.front_index]

        # Calculates the next front index in a circular manner
        self.front_index = (self.front_index + 1) % self.capacity

        # Decrements the current size
        self.current_size -= 1

        # Returns the dequeued element
        return dequeued_element
```

```java run
class Queue {

    // Pointer to the dynamic array representing the queue
    public int[] arr;

    // Maximum capacity of the queue
    public int capacity;

    // Index of the front element in the queue
    public int frontIndex;

    // Index of the back element in the queue
    public int backIndex;

    // Current number of elements in the queue
    public int currentSize;

    public Queue(int capacity) {
        this.capacity = capacity;

        // Allocating memory for the queue
        this.arr = new int[capacity];

        // Initializing front index to 0
        this.frontIndex = 0;

        // Initializing back index to -1
        this.backIndex = -1;

        // Initializing current size to 0
        this.currentSize = 0;
    }

    public int size() {

        // Returns the current size of the queue
        return currentSize;
    }

    public boolean empty() {

        // Returns true if the queue is empty, false otherwise
        return size() == 0;
    }

    public int front() {
        if (empty()) {

            // Returns -1 if the queue is empty
            return -1;
        }

        // Returns the element at the front of the queue
        return arr[frontIndex];
    }

    public int back() {
        if (empty()) {

            // Returns -1 if the queue is empty
            return -1;
        }

        // Returns the element at the back of the queue
        return arr[backIndex];
    }

    public boolean enqueue(int val) {
        if (currentSize == capacity) {

            // Returns false if the queue is full and cannot enqueue more
            // elements
            return false;
        }

        // Calculates the next back index in a circular manner
        backIndex = (backIndex + 1) % capacity;

        // Inserts the new element at the back of the queue
        arr[backIndex] = val;

        // Increments the current size
        currentSize++;

        // Returns true to indicate a successful enqueue operation
        return true;
    }

    public int dequeue() {
        if (empty()) {

            // Returns -1 if the queue is empty and cannot dequeue
            // elements
            return -1;
        }

        // Stores the element to be dequeued
        int dequeuedElement = arr[frontIndex];

        // Calculates the next front index in a circular manner
        frontIndex = (frontIndex + 1) % capacity;

        // Decrements the current size
        currentSize--;

        // Returns the dequeued element
        return dequeuedElement;
    }
}
```

### Complexity Analysis

Same shape as enqueue — predicate, modulo, array read, decrement.

> **Best Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**
>
> **Worst Case**
>
> - Time:  **O(1)**
> - Space: **O(1)**

</details>

***

# Working Example

Watching `frontIndex` and `backIndex` chase each other around the ring is the fastest way to make the six operations click. Start with `Queue(3)` — capacity `3`, buffer `[—, —, —]`, `frontIndex = 0`, `backIndex = -1`, `currentSize = 0`. This run fills the queue, drains part of it, then enqueues again to force a wrap. Each step changes one index and at most one slot:

1. **`enqueue(1)`** — not full, so `backIndex` becomes `(−1 + 1) % 3 = 0`, `arr[0] = 1`, `currentSize = 1`. Buffer `[1, —, —]`.
2. **`enqueue(2)`** — `backIndex` becomes `(0 + 1) % 3 = 1`, `arr[1] = 2`, `currentSize = 2`. Buffer `[1, 2, —]`.
3. **`enqueue(3)`** — `backIndex` becomes `(1 + 1) % 3 = 2`, `arr[2] = 3`, `currentSize = 3` — the queue is now full (`currentSize == capacity`).
4. **`dequeue()`** — not empty, so read `arr[0] == 1`, advance `frontIndex` to `(0 + 1) % 3 = 1`, `currentSize = 2`, return `1`. Buffer still reads `[1, 2, 3]`, but slot `0` is now logically vacant.
5. **`enqueue(4)`** — not full, so `backIndex` becomes `(2 + 1) % 3 = 0` — the wrap. `arr[0] = 4` overwrites the vacated slot, `currentSize = 3`. Buffer `[4, 2, 3]`, front at `1`, back at `0`.
6. **`front()`** — read `arr[1] == 2`, no index changes. The oldest live item is `2`.
7. **`back()`** — read `arr[0] == 4`, no index changes. The newest item is `4`.

The values dequeued so far come out as `1`, and the live contents read `2, 3, 4` front-to-back — exactly insertion order. That ordering is FIFO made literal: the first value enqueued (`1`) is the first dequeued, while the newest (`4`) waits at the back. The wrap at step 5 is the only event that distinguishes a queue's array from a stack's. So the core insight is: enqueue and dequeue chase each other forward around the ring, the modulo recycles vacated slots, and values leave in exactly the order they arrived regardless of capacity.

***

# Design a queue using a circular array

## Problem Statement

Given the skeleton of a **Queue class**, complete it by implementing all the queue operations below.

> -   **`Queue(int capacity)`** — initialise the queue with the given capacity.
> -   **`size()`** — return the current size of the queue.
> -   **`empty()`** — return `true` if the queue is empty, else `false`.
> -   **`front()`** — return the front element; if empty, return `-1`.
> -   **`back()`** — return the back element; if empty, return `-1`.
> -   **`enqueue(int val)`** — add `val` at the back; return `true` if successful, `false` if full.
> -   **`dequeue()`** — remove and return the front element; if empty, return `-1`.

<details>
<summary><h2>Constraints</h2></summary>


1. Use **a single fixed-size array** as the internal data structure. No additional containers, no nodes.
2. The implementation **must be circular** — every vacated slot must be reusable before the queue declares itself full.

```d2
direction: right

ring: "circular layout — front=3, back=0 (wrapped), logical order 7 → 11 → 13 → 17" {
  grid-columns: 6
  grid-gap: 0
  s0: |md
    `[0]`

    **17**
  | {style.fill: "#fef9c3"; style.stroke: "#f59e0b"}
  s1: |md
    `[1]`

    "—"
  |
  s2: |md
    `[2]`

    "—"
  |
  s3: |md
    `[3]`

    **7**
  | {style.fill: "#dcfce7"; style.stroke: "#22c55e"}
  s4: |md
    `[4]`

    11
  |
  s5: |md
    `[5]`

    13
  |
}
ring.s5 -> ring.s0: wrap
```

<p align="center"><strong>Circular queue layout — front=3, back=0 (wrapped), logical order 7→11→13→17. Every slot is reachable; the array is reused indefinitely as long as the size never exceeds capacity.</strong></p>

</details>
<details>
<summary><h2>Worked Example</h2></summary>


> **Input** (operations array, operands array):
>
> `[Queue, enqueue, back, enqueue, front, empty, dequeue, front, enqueue, enqueue, empty]`
>
> `[[2], [2], [], [3], [], [], [], [], [8], [9], []]`
>
> **Expected Output:**
>
> `[null, true, 2, true, 2, false, 2, 3, true, false, false]`
>
> **Trace:**
>
> | Op | Result | Queue state |
> |---|---|---|
> | `Queue(2)` | — | `[]`  (capacity 2) |
> | `enqueue(2)` | `true` | `[2]` |
> | `back()` | `2` | `[2]` |
> | `enqueue(3)` | `true` | `[2, 3]` (full) |
> | `front()` | `2` | `[2, 3]` |
> | `empty()` | `false` | `[2, 3]` |
> | `dequeue()` | `2` | `[3]` |
> | `front()` | `3` | `[3]` |
> | `enqueue(8)` | `true` | `[3, 8]` (full again) |
> | `enqueue(9)` | `false` | `[3, 8]` (rejected — full) |
> | `empty()` | `false` | `[3, 8]` |

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run viz=array viz-root=arr
class Queue:
    def __init__(self, capacity: int):
        self.capacity     = capacity
        self.arr          = [0] * capacity
        self.front_index  = 0
        self.back_index   = -1
        self.current_size = 0

    def size(self):  return self.current_size
    def empty(self): return self.current_size == 0
    def front(self): return -1 if self.empty() else self.arr[self.front_index]
    def back(self):  return -1 if self.empty() else self.arr[self.back_index]
    def enqueue(self, v):
        if self.current_size == self.capacity: return False
        self.back_index           = (self.back_index + 1) % self.capacity
        self.arr[self.back_index] = v
        self.current_size        += 1
        return True
    def dequeue(self):
        if self.empty(): return -1
        v = self.arr[self.front_index]
        self.front_index  = (self.front_index + 1) % self.capacity
        self.current_size -= 1
        return v

# Boss-fight demo
q = Queue(2)
print(q.enqueue(2), q.back())       # True 2
print(q.enqueue(3), q.front())      # True 2
print(q.empty())                    # False
print(q.dequeue(), q.front())       # 2 3
print(q.enqueue(8), q.enqueue(9))   # True False (full)
print(q.empty())                    # False
```

```java run viz=array viz-root=arr
public class Main {
    static class Queue {
        private final int[] arr;
        private final int   capacity;
        private int frontIndex = 0, backIndex = -1, currentSize = 0;
        Queue(int capacity) {
            this.capacity = capacity;
            this.arr      = new int[capacity];
        }
        int     size()        { return currentSize; }
        boolean empty()       { return currentSize == 0; }
        int     front()       { return empty() ? -1 : arr[frontIndex]; }
        int     back()        { return empty() ? -1 : arr[backIndex]; }
        boolean enqueue(int v) {
            if (currentSize == capacity) return false;
            backIndex      = (backIndex + 1) % capacity;
            arr[backIndex] = v;
            currentSize++;
            return true;
        }
        int dequeue() {
            if (empty()) return -1;
            int v       = arr[frontIndex];
            frontIndex  = (frontIndex + 1) % capacity;
            currentSize--;
            return v;
        }
    }
    public static void main(String[] args) {
        Queue q = new Queue(2);
        System.out.println(q.enqueue(2) + " " + q.back());
        System.out.println(q.enqueue(3) + " " + q.front());
        System.out.println(q.empty());
        System.out.println(q.dequeue() + " " + q.front());
        System.out.println(q.enqueue(8) + " " + q.enqueue(9));
        System.out.println(q.empty());
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


A circular array queue is *the* canonical bounded-FIFO data structure — every operation is O(1), no allocation per call, perfect cache locality, and it composes beautifully with hardware features (DMA ring buffers, lock-free ring buffers, the entire networking stack).

1. **Two indices, one modulo.** Front and back both march forward; `(idx + 1) % capacity` is the only thing that distinguishes a queue's array from a stack's array. Once you internalise that line, the whole implementation is bookkeeping.
2. **Maintain `currentSize` separately.** It's the only unambiguous way to distinguish *empty* from *full* on a circular buffer. Trying to derive size from the index gap leads to off-by-one bugs and subtle ring-buffer puzzles (the "leave one slot empty" trick is one workaround; just storing the size is simpler).
3. **The trade-off is bounded capacity.** A circular array queue cannot grow without copying. If you need an unbounded queue, you either (a) use a linked list — next lesson — or (b) implement a *growable* ring buffer that reallocates and re-rolls when full. The bounded version is what you want for back-pressure-aware producers, fixed-memory environments (kernels, embedded), and lock-free designs.

> *Coming up — the linked-list implementation. Same interface, different trade-offs: O(1) enqueue and dequeue without amortisation, unbounded by default, but each node costs a heap allocation and pointer chasing destroys cache locality. The lesson after that pits stacks and queues against each other in two design problems that test whether you really understand the FIFO contract.*

</details>

***

# Edge Cases and Pitfalls

The circular queue fits on one screen, yet most of its bugs cluster around the two moving indices and the `currentSize` counter that disambiguates them. Keep this list open the next time a queue misbehaves at the edges:

- **Deriving size from the indices instead of storing it.** On a wrapped ring, `frontIndex == backIndex + 1 (mod capacity)` is ambiguous: it describes both an empty queue and a full one. Maintaining `currentSize` as a separate counter is the unambiguous fix; the index gap alone cannot tell `0` items from `capacity` items in `O(1)`.
- **Forgetting the modulo on one of the two advances.** Both `frontIndex` and `backIndex` must advance with `(idx + 1) % capacity`. Writing a plain `backIndex + 1` enqueues one slot past the buffer the moment the back reaches `capacity - 1`. That is an `IndexError` in Python, an `ArrayIndexOutOfBoundsException` in Java, or silent corruption in C.
- **Checking `backIndex == capacity - 1` for "full".** After a wrap the back can sit anywhere on the ring, so a position-based full check is wrong. The correct test is `currentSize == capacity`, which holds no matter where the indices landed.
- **The `-1` sentinel collides with real data.** Returning `-1` from `front()`, `back()`, or `dequeue()` to mean "empty" is only safe when `-1` cannot be a stored value. If the queue holds arbitrary integers, `-1` is ambiguous. Prefer an explicit `empty()` guard before every read, or raise an exception instead of returning a sentinel.
- **Dequeue does not clear the slot.** A dequeued value stays physically in the buffer until a wrapping enqueue overwrites it. For plain integers this is harmless, but a queue of object references keeps those objects alive and ineligible for garbage collection. In Java, null the slot after reading it when storing references.
- **Mutating before checking.** The full check must run before the back advance and write; the empty check before the front read and advance. Reordering them — advancing `backIndex` first, then testing — leaves an index and `currentSize` in a corrupt state when the boundary is hit.

***

# Production Reality

The array-backed circular queue is the default queue everywhere a maximum depth is known or memory must stay contiguous and lock-free. The systems below are worth knowing by name.

**[The Linux kernel `kfifo`]** — uses **a power-of-two circular byte buffer with masking instead of modulo** — because a single-producer/single-consumer ring needs no locks, and masking a power-of-two index is even cheaper than the `%` operator on the kernel's hot I/O path.

**[A network interface card driver's RX/TX rings]** — uses **fixed-size descriptor ring buffers shared with the hardware** — because the NIC and the CPU advance head and tail pointers independently, and a bounded ring gives back-pressure for free when the consumer falls behind.

**[An audio processing pipeline]** — uses **a lock-free ring buffer between the capture and playback threads** — because the real-time audio thread cannot block on allocation, so a pre-sized circular buffer guarantees `O(1)` enqueue and dequeue with no `malloc` in the callback.

**[The LMAX Disruptor]** — uses **a pre-allocated ring buffer indexed by a monotonically increasing sequence** — because high-frequency trading needs millions of messages per second, and reusing slots in a fixed ring eliminates per-message garbage-collection pressure.

**[A bounded producer-consumer work queue]** — uses **a circular array guarded by the `currentSize` counter** — because the bounded design applies back-pressure to producers when full rather than growing without limit and exhausting memory.

**[Java's `ArrayDeque` used as a queue]** — uses **a growable circular array behind `offer` / `poll`** — because the standard library wants `O(1)` amortised FIFO with far better cache locality than `LinkedList`, accepting an occasional resize to stay unbounded.

***

# Quiz

Test your grip before moving on. One answer per question; reveal only after you have committed to one.

**[Recall] Q: For a queue holding three items at indices `1`, `2`, `3`, what do `frontIndex`, `backIndex`, and `currentSize` hold?**
`frontIndex == 1`, `backIndex == 3`, `currentSize == 3` — front marks the oldest item, back marks the newest, and the counter tracks the live count directly.

**[Recall] Q: What single expression advances either index, and what does it compute when the index is `capacity - 1`?**
`(idx + 1) % capacity` advances either index, and when `idx == capacity - 1` it wraps to `0` rather than stepping out of bounds.

**[Reasoning] Q: Why does the queue maintain `currentSize` separately instead of deriving size from `frontIndex` and `backIndex`?**
On a wrapped ring the indices alone cannot distinguish an empty queue from a full one when they meet, so a separate `currentSize` counter is the only `O(1)` way to tell the two apart.

**[Reasoning] Q: Why does `dequeue()` not need to erase the value it removes from the buffer?**
The queue is defined as the values between `frontIndex` and `backIndex` taken cyclically, so advancing `frontIndex` makes the old front invisible to every operation, and the next wrapping enqueue overwrites that slot anyway.

**[Tradeoff] Q: When would you choose a bounded circular queue over a growable one, and what do you give up?**
Choose bounded when the maximum depth is known and per-operation latency must be predictable, as in kernels and real-time audio. You give up the freedom to exceed capacity, since a full enqueue is rejected rather than triggering an `O(n)` resize-and-copy.

***

# Practice Ladder

Five problems that exercise the queue contract and the circular-buffer mechanics, easiest first. Try each unaided; hit the hint after ten minutes; do not peek at solutions until you have written something runnable.

<!-- VERIFY: confirm links — the 06-queue chapter has no pattern-problem directories, so this ladder links the chapter's own lessons/design plus a sibling sliding-window pattern problem that a deque accelerates; revisit if dedicated queue problems are added. -->

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | [Array Implementation of Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-array-implementation-of-queues) | [Introduction to Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-introduction-to-queues) | Easy | Build the six operations from scratch over one buffer; the only logic is the full check and the two cyclic advances. `O(1)` per op, `O(n)` space. |
| 2 | [Linked-List Implementation of Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-linked-list-implementation-of-queues) | [Introduction to Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-introduction-to-queues) | Easy | Keep head and tail pointers; enqueue at the tail, dequeue at the head — no modulo, but one allocation per node. `O(1)` per op. |
| 3 | [Design a Queue using Stacks](/cortex/data-structures-and-algorithms/linear-structures-queue-design-a-queue-design-a-queue) | [Introduction to Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-introduction-to-queues) | Medium | Pour one stack into a second to reverse LIFO into FIFO; transfer lazily so each item moves at most once. Amortised `O(1)` per op. |
| 4 | [Maximum Subarray Sum](/cortex/data-structures-and-algorithms/linear-structures-arrays-pattern-variable-sliding-window-problems-maximum-subarray-sum) | [Variable Sliding Window](/cortex/data-structures-and-algorithms/linear-structures-arrays-pattern-variable-sliding-window-pattern) | Medium | Treat the window as a FIFO front-to-back; admit at the back, evict from the front when the running condition breaks. `O(n)` one pass. |
| 5 | [Subarray Size Equals K](/cortex/data-structures-and-algorithms/linear-structures-arrays-pattern-fixed-sliding-window-problems-subarray-size-equals-k) | [Fixed Sliding Window](/cortex/data-structures-and-algorithms/linear-structures-arrays-pattern-fixed-sliding-window-pattern) | Easy | A fixed-width window is a bounded queue: enqueue one element at the back and dequeue one at the front each step. `O(n)` time, `O(1)` extra. |

Once these feel automatic, enqueue and dequeue have stopped being syntax and become a structural reflex — exactly what BFS, scheduling, and stream-processing problems build on.

***

# Further Reading

Curated paths in, not a syllabus. Read in order of the annotation; come back for the rest when you need depth.

- **[CLRS — Chapter 10.1: Stacks and Queues](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)**
  ★ Essential — the canonical array-backed queue with `ENQUEUE` / `DEQUEUE`, the head and tail indices, and the wrap-around treated formally, including the empty- and full-queue error conditions.
- **[Linux kernel `kfifo` source](https://github.com/torvalds/linux/blob/master/lib/kfifo.c)**
  ◆ Advanced — a production single-producer/single-consumer ring buffer using a power-of-two size and bit masking instead of `%`; this lesson's design hardened for the kernel I/O path.
- **[The LMAX Disruptor technical paper](https://lmax-exchange.github.io/disruptor/disruptor.html)**
  ◆ Advanced — how a pre-allocated ring buffer indexed by a running sequence reaches millions of messages per second by reusing slots and avoiding garbage collection.
- **[Java `ArrayDeque` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/ArrayDeque.java)**
  → Reference — the growable circular-array deque the JDK recommends over `LinkedList`; shows how the bounded design extends to amortised `O(1)` growth without losing cache locality.

***

# Cross-Links

**Prerequisites**

- [Introduction to Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-introduction-to-queues) — the FIFO contract and the enqueue / dequeue / front / back / empty / size interface this lesson implements over an array.
- [Introduction to Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-introduction) — contiguous layout and `O(1)` indexed access, the two array properties that make the whole implementation constant time.
- [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — what `O(1)` per operation and `O(n)` space in capacity actually mean, and how to read them off the code.

**What comes next**

- [Linked-List Implementation of Queues](/cortex/data-structures-and-algorithms/linear-structures-queue-linked-list-implementation-of-queues) — the same six operations with the tradeoff flipped: no fixed capacity and no modulo, but one allocation per node and no cache locality.
- [Design a Queue](/cortex/data-structures-and-algorithms/linear-structures-queue-design-a-queue-design-a-queue) — builds a queue out of stacks and a stack out of queues, testing whether the FIFO and LIFO contracts are understood as composable primitives.

***

## Final Takeaway

1. **Core mechanic:** track a `frontIndex` and a `backIndex` that both march forward through `(idx + 1) % capacity`, keep a `currentSize` counter, and every operation becomes `O(1)` time and `O(1)` extra space — enqueue advances back then writes, dequeue reads then advances front.
2. **Dominant tradeoff:** you gain cache-friendly contiguous storage and predictable per-operation cost with no allocation; you give up the freedom to grow, since a bounded queue rejects an enqueue once `currentSize == capacity`.
3. **One thing to remember:** the modulo recycles vacated slots and the `currentSize` counter is what tells empty from full on a wrapped ring — keep both correct and the entire queue is correct.
