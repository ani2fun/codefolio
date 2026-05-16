---
title: Introduction to Heaps
summary: Two invariants — completeness and heap-ordering — give you O(1) peek and O(log n) push/pop. The structure behind every priority queue, every Dijkstra, every top-K leaderboard.
prereqs:
  - trees-binary-tree-introduction-to-binary-trees
  - foundations-asymptotic-analysis
---

# 1. Introduction to Heaps

## The Hook

Picture a hospital ER on a Friday night. Three doctors. Forty patients. New people arrive every minute — a sprained ankle, a chest pain, a child with a fever, a heart attack. Doctors finish with one patient and immediately need *the next-most-critical case*, **right now**, no scanning, no waiting.

This is the **streaming maximum** problem, and it's everywhere. Operating-system process schedulers picking the highest-priority thread to run. Web servers picking which request to process first. Dijkstra's shortest-path algorithm picking the closest unvisited node. Event simulations picking the next event to fire by timestamp. Network routers picking the next packet to send. Every one of them needs the same primitive: a data structure that **always knows the smallest (or largest) value it's holding**, and lets you yank it out in microseconds even when items are flying in and out at the same time.

A linked list will do it — slowly. A sorted array will do it — slowly. A balanced BST will do it — well, but with overhead. The data structure built specifically for this job, used by every standard library on the planet under the name `priority_queue` / `PriorityQueue` / `heapq`, is the **heap**: a tree with a single elegant rule that gives you O(log n) insert and O(log n) extract-min — *and* fits in a flat array, no pointers, no allocations per node.

This first lesson sets up the language: what problem heaps solve, what makes a heap a heap, the two flavours (max and min), and the operations every heap supports. The next lesson is where the magic shows up: how to store this whole tree-shaped thing in a plain old array using nothing but index arithmetic.

---

## Table of Contents

1. [Understanding the problem](#understanding-the-problem)
2. [Exploring a possible solution](#exploring-a-possible-solution)
3. [Understanding a heap](#understanding-a-heap)
4. [Types of heaps](#types-of-heaps)
5. [Overview of supported operations](#overview-of-supported-operations)
6. [Tree heap validator](#tree-heap-validator)

***

# Understanding the problem

Consider a busy hospital emergency ward: a small number of doctors, an unbounded stream of incoming patients, and every patient tagged with a *priority* number measuring how urgent their case is. The rule is simple — whenever a doctor becomes free, they treat **the highest-priority patient currently waiting**. Lower priorities wait, regardless of how long they've been queued.

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
    subgraph In["Incoming patients"]
        P1["Alice<br/>priority = 5"]
        P2["Bob<br/>priority = 9"]
        P3["Carol<br/>priority = 2"]
        P4["Dan<br/>priority = 8"]
    end
    subgraph Q["Priority store"]
        Q1["?"]
    end
    subgraph Out["Doctor takes the most urgent"]
        D1["Bob (9) → first"]
        D2["Dan (8) → next"]
        D3["Alice (5) → next"]
        D4["Carol (2) → last"]
    end
    In --> Q --> Out
    style Q fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>The streaming-priority problem: arbitrary insertions, but every <em>extract</em> must produce the highest-priority item.</strong></p>

A natural first attempt: keep all patients in a **linked list**. Insertion is easy — append to the head, O(1). But what about finding the highest-priority patient when a doctor frees up? You have to **scan the whole list**.

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
    H[head] --> N1["Alice (5)"]
    N1 --> N2["Bob (9)"]
    N2 --> N3["Carol (2)"]
    N3 --> N4["Dan (8)"]
    N4 --> NIL["null"]
```

<p align="center"><strong>Patients in a linked list. Insertion at the head is O(1), but finding the maximum priority requires walking every node.</strong></p>

Inserting a new patient is still cheap:

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
    H[head] --> NEW["Eve (7)<br/>NEW"]
    NEW --> N1["Alice (5)"]
    N1 --> N2["Bob (9)"]
    N2 --> N3["Carol (2)"]
    N3 --> N4["Dan (8)"]
    style NEW fill:#bbf7d0,stroke:#16a34a
```

<p align="center"><strong>A new patient is inserted at the head — O(1).</strong></p>

But the *extract-max* path — which is the operation we run every time a doctor opens up — is brutal:

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
    H[head] --> N1["Alice (5)"]
    N1 --> N2["Bob (9) ⭐"]
    N2 --> N3["Carol (2)"]
    N3 --> N4["Dan (8)"]
    SCAN["Scan ALL nodes<br/>to find max — O(n)"]
    SCAN -.- H
    style N2 fill:#fde68a,stroke:#d97706
```

<p align="center"><strong>Finding the highest-priority patient in a linked list requires a linear scan of every node. Scaling to a 100k-patient national emergency system, this is hopeless.</strong></p>

## Limitations of a linked list

Two problems with the linked-list approach:

1. **Extra space.** Every node carries `next` (and `prev` if doubly-linked) pointers. For a million-element queue, that's millions of pointer bytes burnt on bookkeeping.
2. **Performance.** The most frequent operation — find-and-extract-the-max — is O(n). At a national 911 scale with hundreds of thousands of cases live at once, an O(n) scan per dispatch is unworkable.

We need a *purpose-built* data structure for this job — one that gives us **O(1) peek** at the maximum and **O(log n) insert and extract**. That structure is the **priority queue**, and the most popular implementation of a priority queue is the **heap**.

***

# Exploring a possible solution

A **priority queue** is the *abstract* solution to the streaming-priority problem: a container that always exposes the highest-priority item and lets you efficiently push new items at any priority.

## What is a priority queue?

A priority queue stores `(value, priority)` pairs. It supports two main operations:

- **Push** — insert a new item with a given priority (no fixed position; the queue figures out where it goes).
- **Extract** — remove and return the item with the **highest priority**. (Or lowest, depending on the flavour.)

It looks like an ordinary queue from the outside — items go in, items come out — but the order is *priority-driven*, not arrival-driven. Two items with the same priority are typically served in FIFO order (insertion order) as a tiebreaker.

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
    PUSH["push(item, priority)"] --> PQ["Priority queue"]
    PQ --> POP["extract() → highest-priority item"]
    PQ --> PEEK["peek() → look without removing"]
    style PQ fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>The priority-queue ADT. <code>push</code> goes in anywhere; <code>extract</code> always pulls the maximum.</strong></p>

## Real life example

Back to the ER. With a priority queue:

- **A new patient arrives** → `push((patient, urgency))`. O(log n).
- **A doctor frees up** → `extract()` returns the most urgent patient. O(log n).

That's it. The priority queue does the bookkeeping; the dispatch loop is a tight five lines.

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
    A["new patient arrives"] --> B["push((name, priority))"]
    B --> PQ["Priority queue"]
    C["doctor frees up"] --> D["extract()"]
    D --> PQ
    PQ --> E["highest-priority patient<br/>handed to doctor"]
    style PQ fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>The dispatch loop, expressed in priority-queue calls.</strong></p>

A priority queue is an **abstract data type** — it specifies *what* operations are supported and their semantics, not *how* they're implemented. Many implementations exist: sorted arrays, unsorted arrays, balanced BSTs, leftist trees, Fibonacci heaps. The one that wins on simplicity, cache friendliness, and constant factors is the **binary heap** — a tree whose physical layout is just a flat array.

That's what the rest of this chapter is about.

***

# Understanding a heap

A **heap** is a binary tree that obeys two rules at all times:

> 1. The tree is a **complete binary tree**.
> 2. Every node satisfies the **heap ordering property** — its value is more important (higher priority) than both of its children's values.

These two rules together are what make heap operations cheap. The completeness rule guarantees the tree is *as balanced as it can be* (no path is much longer than another), so every operation runs in O(log n). The ordering rule guarantees the highest-priority value is *always at the root*, so peeking is O(1).

## Complete binary tree

A **complete binary tree** is a binary tree where every level is fully filled, **except possibly the last**, which fills **left-to-right**. No gaps in the middle, no missing nodes on the left while the right is occupied — the tree is packed.

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
    A((A))
    B((B))
    C((C))
    D((D))
    E((E))
    F((F))
    G((G))
    H((H))
    I((I))
    A --> B
    A --> C
    B --> D
    B --> E
    C --> F
    C --> G
    D --> H
    D --> I
    style A fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>A complete binary tree of 9 nodes. Every level is fully filled except the last; the last level is filled from the left without gaps.</strong></p>

Why does completeness matter? Two reasons:

1. **Height stays logarithmic.** A complete binary tree of `n` nodes has height exactly `⌊log₂ n⌋`. Operations that walk a single root-to-leaf path are O(log n).
2. **The tree fits in an array.** Because the structure is rigid (no missing internal nodes), we can store it index-by-index in a flat array — and the parent/child relationships become arithmetic, not pointer chases. The next lesson will derive these formulas in detail.

## Heap ordering property

The second rule is what gives the heap its *name* and its **superpower**:

> **Heap ordering property:** every node has higher priority than both of its children.

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
    N(("node N"))
    L(("left child<br/>lower priority"))
    R(("right child<br/>lower priority"))
    N --> L
    N --> R
    style N fill:#fef9c3,stroke:#f59e0b
    style L fill:#dbeafe,stroke:#3b82f6
    style R fill:#dbeafe,stroke:#3b82f6
```

<p align="center"><strong>The heap-ordering property at a single node. The same picture must hold at every node in the tree.</strong></p>

Pause and notice what this rule does **NOT** say:

- It says nothing about left vs right (unlike a BST). The two children just have to be lower-priority than the parent — the *order between them doesn't matter*.
- It says nothing about a node's relationship to its grandchildren. Just parent-vs-child, applied recursively.

The first omission is the cheap-insert escape hatch: when you push a new value, you don't have to find a *specific* slot like in a BST — any vacancy that fixes the parent comparison is fine. This is what gives the heap its O(log n) insert.

The two consequences together — *completeness* + *parent-bigger-than-children* — produce one beautiful invariant: **the maximum (or minimum) priority is always at the root**. Peek is O(1).

***

# Types of Heaps

Whether "high priority" means "big number" or "small number" is a problem-specific choice. Two flavours:

| Flavour | Ordering rule | Root holds |
|---|---|---|
| **Max heap** | Every node ≥ both children | Maximum value |
| **Min heap** | Every node ≤ both children | Minimum value |

### Max heap

A max heap is the heap where bigger numbers are higher priority. It's used when "highest score wins": top-K largest values, leaderboards, top-load servers, recently-most-used caches with size scores.

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
    R((50))
    A((30))
    B((40))
    C((10))
    D((20))
    E((35))
    F((25))
    R --> A
    R --> B
    A --> C
    A --> D
    B --> E
    B --> F
    style R fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>A max heap with 7 nodes. Each parent (e.g. <code>50, 30, 40, ...</code>) is at least as big as both its children. The maximum value <code>50</code> sits at the root.</strong></p>

### Min Heap

A min heap is the mirror — smaller numbers win. It's used when "lowest cost wins": Dijkstra's shortest path (closest unvisited node), event simulators (next event by timestamp), deadline schedulers (earliest deadline), and any "extract the smallest" workload.

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
    R((10))
    A((20))
    B((15))
    C((30))
    D((25))
    E((40))
    F((35))
    R --> A
    R --> B
    A --> C
    A --> D
    B --> E
    B --> F
    style R fill:#fef9c3,stroke:#f59e0b
```

<p align="center"><strong>A min heap with 7 nodes. Each parent is no greater than its children; the minimum <code>10</code> sits at the root.</strong></p>

The two flavours are symmetric — every algorithm in this chapter has a mirror version that swaps `<` for `>`. Most languages' standard libraries default to one (Python's `heapq` is min-heap; Java's `PriorityQueue` is min-heap; C++'s `std::priority_queue` is max-heap) and let you flip the comparator to get the other. We'll meet comparators in lesson 4.

***

# Overview of supported operations

Five operations cover virtually everything you'll ever do with a heap. Each one keeps the two heap rules — completeness and ordering — invariant.

## Insert

**`insert(value)`** — add a new value to the heap. Place it in the next "complete" slot (so completeness is preserved), then **bubble it up** the tree, swapping with its parent whenever it's higher-priority than the parent. Stops when the parent is higher-priority or we hit the root. O(log n).

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
    subgraph Before["Before insert(53)"]
        direction TB
        B1((50))
        B2((30))
        B3((40))
        B4((10))
        B5((20))
        B6((35))
        B7((25))
        B1 --> B2
        B1 --> B3
        B2 --> B4
        B2 --> B5
        B3 --> B6
        B3 --> B7
    end
    subgraph After["After insert(53)"]
        direction TB
        A1((53))
        A2((50))
        A3((40))
        A4((10))
        A5((30))
        A6((35))
        A7((25))
        A8((20))
        A1 --> A2
        A1 --> A3
        A2 --> A4
        A2 --> A5
        A3 --> A6
        A3 --> A7
        A5 --> A8
        style A1 fill:#bbf7d0,stroke:#16a34a
    end
    Before --> After
```

<p align="center"><strong>Insert <code>53</code> into a max-heap. New value lands in the next complete slot (under <code>30</code>), then bubbles up because <code>53 &gt; 30</code> and <code>53 &gt; 50</code>. Final position: the root.</strong></p>

## Delete

**`delete(value)`** — remove a specific value from the heap. Find it (linear scan, since heaps are not searchable like BSTs), replace it with the last node in the tree, then either bubble up or sift down to restore the ordering property. O(n) for the find + O(log n) for the fix; usually we don't need this operation in raw heaps — `extract` covers most use cases.

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
    subgraph Before["Before delete(20)"]
        direction TB
        B1((50))
        B2((30))
        B3((40))
        B4((10))
        B5((20))
        B6((35))
        B7((25))
        B1 --> B2
        B1 --> B3
        B2 --> B4
        B2 --> B5
        B3 --> B6
        B3 --> B7
        style B5 fill:#fecaca,stroke:#ef4444
    end
    subgraph After["After delete(20)"]
        direction TB
        A1((50))
        A2((30))
        A3((40))
        A4((10))
        A6((35))
        A7((25))
        A1 --> A2
        A1 --> A3
        A2 --> A4
        A3 --> A6
        A3 --> A7
    end
    Before --> After
```

<p align="center"><strong>Delete <code>20</code> from a max-heap. The last node <code>25</code> takes <code>20</code>'s place, then sifts as needed (here no sifting is required; the heap shape is preserved).</strong></p>

## Peek

**`peek()`** — return the root's value without removing it. The root *is* the highest-priority item by the heap-ordering property, so this is just `arr[0]`. **O(1)**, the cheapest operation in the heap toolkit.

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
    R((50))
    A((30))
    B((40))
    C((10))
    D((20))
    E((35))
    F((25))
    R --> A
    R --> B
    A --> C
    A --> D
    B --> E
    B --> F
    style R fill:#bbf7d0,stroke:#16a34a
    OUT["peek() returns 50<br/>(O(1) — just look at the root)"]
    style OUT fill:#bbf7d0,stroke:#16a34a
```

<p align="center"><strong><code>peek()</code> on a max-heap reads the root in O(1) — no traversal, no comparisons.</strong></p>

## Extract

**`extract()`** — remove **and** return the highest-priority value. Take the root, replace it with the last node, then **sift the new root down** the tree by swapping with the higher-priority of its two children whenever they out-prioritise the parent. Stops when both children are lower-priority or we hit a leaf. O(log n).

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
    subgraph Before["Before extract()"]
        direction TB
        B1((50))
        B2((30))
        B3((40))
        B4((10))
        B5((20))
        B6((35))
        B7((25))
        B1 --> B2
        B1 --> B3
        B2 --> B4
        B2 --> B5
        B3 --> B6
        B3 --> B7
        style B1 fill:#fecaca,stroke:#ef4444
    end
    subgraph After["After extract() (returns 50)"]
        direction TB
        A1((40))
        A2((30))
        A3((35))
        A4((10))
        A5((20))
        A6((25))
        A1 --> A2
        A1 --> A3
        A2 --> A4
        A2 --> A5
        A3 --> A6
        style A1 fill:#bbf7d0,stroke:#16a34a
    end
    Before --> After
```

<p align="center"><strong>Extract the maximum. The root <code>50</code> is removed and returned. The last node <code>25</code> moves to the root, then sifts down (swap with the larger child <code>40</code>; further sifts not needed because <code>40 ≥ 35, 25</code>).</strong></p>

## Construct

**`construct(values)`** — build a heap from an existing array of values. The naive approach is to insert one-by-one (O(n log n) total), but there's a clever bottom-up "heapify" algorithm that does it in **O(n)**. We'll see it in detail in lesson 2.

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
    A["Input array<br/>[10, 30, 20, 50, 40, 35, 25]"] --> B["construct() — O(n)"]
    B --> C["Valid max-heap<br/>(50 at the root, all parent-child pairs satisfy max-heap rule)"]
    style C fill:#bbf7d0,stroke:#16a34a
```

<p align="center"><strong>Constructing a heap from an unsorted array. The bottom-up algorithm runs in O(n), faster than n separate inserts.</strong></p>

We'll meet all five operations in detail in the next lesson, where we replace the "imaginary tree" picture with a flat array and watch the index arithmetic do all the work.

***

# Tree heap validator

Before we build heaps, let's verify what one *is* — by writing a function that checks whether a given binary tree satisfies the **min-heap ordering property**. (We'll skip the completeness check here for brevity; in practice you'd verify both rules.)

## Problem Statement

Given the **root** of a binary tree, return `true` if the tree represents a valid **min heap**, `false` otherwise.

> A valid min heap requires every node's value to be **≤** the values of both its children. (We'll assume the tree is already complete — i.e. structurally a heap shape.)

### Example 1

> - **Input:** `root = [1, 2, 3, 4, 5, 6, 7]`
> - **Output:** `true`

### Example 2

> - **Input:** `root = [1, 4, 3, 2, 5, 6, 7]`
> - **Output:** `false`
> - **Explanation:** `2` (left grandchild of root) is less than its parent `4`, violating the min-heap rule.

## The Strategy

The min-heap rule is purely local: every node's value is `≥` its parent's value. Walk the tree recursively, passing the parent's value down. At each node:

- If the node's value `< parent.val`, the rule is violated → return `false`.
- Otherwise recurse into both children with this node as the new parent.

The root has no parent, so we use `-∞` as a sentinel — it's smaller than anything, so any root passes.

## The Solution


```pseudocode
function isValidHeap(root, parentVal):
    if root is null: return true           # empty subtree is trivially valid
    if root.val < parentVal: return false  # min-heap rule: child must be ≥ parent
    return isValidHeap(root.left,  root.val)
       AND isValidHeap(root.right, root.val)

function treeHeapValidator(root):
    if root is null: return true
    return isValidHeap(root, −∞)           # −∞ sentinel so root passes unconditionally
```

```python run
class Solution:
    def is_valid_heap(self, root, parent_val):
        # Empty subtree trivially satisfies the heap rule.
        if root is None:
            return True
        # Min-heap rule: each node must be ≥ its parent.
        if root.val < parent_val:
            return False
        # Recurse into both children, passing this node as their parent.
        return (self.is_valid_heap(root.left,  root.val) and
                self.is_valid_heap(root.right, root.val))

    def tree_heap_validator(self, root):
        if root is None:                          # empty tree is a valid heap by convention
            return True
        # Use -infinity as the sentinel parent for the root.
        return self.is_valid_heap(root, float("-inf"))
```

```java run
public class Main {
    static class TreeNode { int val; TreeNode left, right; TreeNode(int v){val=v;} }

    static class Solution {
        private boolean isValidHeap(TreeNode root, int parentVal) {
            if (root == null) return true;                                                                                                          // empty subtree → valid
            if (root.val < parentVal) return false;                                                                                                 // min-heap rule violated
            return isValidHeap(root.left,  root.val)
                && isValidHeap(root.right, root.val);                                                                                               // recurse with this node as parent
        }

        public boolean treeHeapValidator(TreeNode root) {
            if (root == null) return true;
            return isValidHeap(root, Integer.MIN_VALUE);                                                                                            // -∞ sentinel for the root
        }
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        root.left  = new TreeNode(4); root.right = new TreeNode(3);
        root.left.left  = new TreeNode(2); root.left.right  = new TreeNode(5);
        root.right.left = new TreeNode(6); root.right.right = new TreeNode(7);
        System.out.println(new Solution().treeHeapValidator(root));  // false
    }
}
```

```c run
#include <limits.h>
#include <stdbool.h>

static bool is_valid_heap(struct TreeNode *root, int parent_val) {
    if (root == NULL) return true;                                                                                                                // empty subtree
    if (root->val < parent_val) return false;                                                                                                     // min-heap violation
    return is_valid_heap(root->left,  root->val)
        && is_valid_heap(root->right, root->val);
}

bool treeHeapValidator(struct TreeNode *root) {
    if (root == NULL) return true;
    return is_valid_heap(root, INT_MIN);                                                                                                          // -∞ sentinel
}
```

```scala run
class TreeNode(var value: Int, var left: TreeNode = null, var right: TreeNode = null)

object Main extends App {
  class Solution {
    private def isValidHeap(root: TreeNode, parentVal: Int): Boolean = {
      if (root == null) return true                                                                                                                     // empty subtree
      if (root.value < parentVal) return false                                                                                                          // min-heap violation
      isValidHeap(root.left,  root.value) && isValidHeap(root.right, root.value)
    }

    def treeHeapValidator(root: TreeNode): Boolean = {
      if (root == null) return true
      isValidHeap(root, Int.MinValue)                                                                                                                   // -∞ sentinel
    }
  }

  val root = new TreeNode(1,
    new TreeNode(4, new TreeNode(2), new TreeNode(5)),
    new TreeNode(3, new TreeNode(6), new TreeNode(7)))
  println(new Solution().treeHeapValidator(root))  // false
}
```


<details>
<summary><strong>Trace — root = [1, 4, 3, 2, 5, 6, 7]</strong></summary>

```
Tree shape (level order: [1, 4, 3, 2, 5, 6, 7]):
            1
          /   \
         4     3
        / \   / \
       2   5 6   7

Step 1 │ root=1, parent=−∞ → 1 ≥ −∞ ✓ → recurse with parent=1
Step 2 │ root=4, parent=1  → 4 ≥ 1  ✓ → recurse with parent=4
Step 3 │ root=2, parent=4  → 2 < 4  ✗ → return FALSE
Result: false ✓ (the rule fails at node 2)
```

</details>

***

# Edge Cases and Pitfalls

The heap is small enough to memorise but big enough to ambush you with the same handful of bugs every engineer hits at least once. The list below is the bug catalogue — keep it open the next time you're debugging a priority queue.

- **Sift-down picks the wrong child.** When sifting down a max-heap, you must compare the parent against the **larger** of the two children, not just the left child. The "obvious" loop *swap-with-left-if-bigger, then swap-with-right-if-bigger* produces a heap that looks balanced but violates the ordering rule on alternating subtrees. Always compute *both* children, pick the bigger one, then compare to the parent.
- **`(low + high) / 2` overflows.** Anywhere you do midpoint arithmetic in heap helpers, `(low + high) / 2` overflows to a negative number once `low + high > Int.MaxValue`. Use `low + (high - low) / 2`. The same bug bit Java's `Arrays.binarySearch` for nine years before Joshua Bloch [found it in 2006](https://research.googleblog.com/2006/06/extra-extra-read-all-about-it-nearly.html).
- **Last-node tracking after extraction.** `extract()` replaces the root with `arr[size − 1]`, then decrements `size`. If you decrement *first* and then read `arr[size]`, you grab the index past the actual last element. Off-by-one. Decrement *after* the swap.
- **Comparator violates strict weak ordering.** A custom comparator must be **antisymmetric** (`compare(a, b) > 0` ⇔ `compare(b, a) < 0`) and **transitive**. Java's `PriorityQueue` and C++'s `std::priority_queue` will silently corrupt their internal state — not throw — if you violate this, and the bug surfaces hours later as a wrong top-K answer in a totally different code path.
- **Insertion order is not preserved among ties.** Two elements with equal priority can come out in either order. If you need FIFO-among-ties (a "stable" priority queue), pair the priority with a monotonically-increasing tiebreaker: `(priority, insertionCount)`. Languages do not give you stability for free.
- **`PriorityQueue` is not thread-safe.** In Java, `PriorityQueue` is unsynchronised. Two threads pushing concurrently will eventually corrupt the heap. Reach for `PriorityBlockingQueue` for concurrent producers, or wrap in a lock.
- **Decreasing a key is O(n) on a binary heap.** A binary heap doesn't index its elements by value, so finding "the node holding x" to update its priority requires a linear scan. Dijkstra implementations that pretend `decrease-key` is O(log n) are using a Fibonacci heap, or maintaining a separate `value → index` map. The plain binary heap will silently give you O(n + log n) per `decrease-key` — usually fine, occasionally fatal.
- **Heaps are not searchable.** `delete(value)` and `contains(value)` are both O(n). If you find yourself asking "is `x` in the heap?", the heap is the wrong data structure — reach for a hash set, a BST, or a heap *combined* with a hash set indexed by value.

***

# Production Reality

The heap is one of the most-deployed data structures on the planet. The five places below are worth knowing by name; you'll meet at least three of them in any real backend.

- **CPython's `heapq`** ([`Lib/heapq.py`](https://github.com/python/cpython/blob/main/Lib/heapq.py)) — pure-Python min-heap operating in-place on an ordinary `list`. The `heappush` / `heappop` you reach for in interview answers and most production Python; ~250 lines of well-commented source, worth reading once end-to-end. The "min-heap by default; tuple `(neg, x)` for max-heap" idiom is the language's way of saying it doesn't intend to ship a separate `heapq.maxheap`.
- **Java's `java.util.PriorityQueue`** — array-backed binary min-heap; `O(log n)` add and poll; *not* synchronised. The single most common priority queue you'll see in JVM-language production code. The contract on the comparator is documented but easy to break by accident.
- **C++'s `std::priority_queue`** (in `<queue>`) plus the lower-level `std::make_heap` / `push_heap` / `pop_heap` (in `<algorithm>`). These ship as templates, so the comparator is inlined at the call site — measurably faster than the Python and Java versions for tight loops.
- **The Linux process scheduler** is *not* heap-based — note the contrast. The **Completely Fair Scheduler (CFS)** keys tasks by virtual runtime in a **red-black tree**, not a heap, because the kernel needs ordered iteration and lookup-by-key, neither of which a binary heap supports cheaply. Heaps win when you only need "give me the most important". RB-trees take over the moment you also need ordered traversal. We'll cover the contrast in the [self-balancing BST overview](/cortex/data-structures-and-algorithms/trees-self-balancing-bst-overview).
- **Dijkstra's shortest-path inside graph engines** — every BGP daemon, every routing engine, every map application runs Dijkstra with a binary or Fibonacci heap as the *open set*. The heap is what makes "shortest path between two cities" feel instant on a graph of fifty million road segments. You'll see this in detail in [Single-Source Shortest Path](/cortex/data-structures-and-algorithms/graphs-single-source-shortest-path).
- **Streaming top-K analytics** — when you need "the top 10 most-frequent IPs in the last hour" without holding every IP in memory, the standard move is a fixed-size min-heap of size K. Push every observation; evict the smallest if the heap exceeds K. Total cost: O(n log K). The pattern is so common it has its own lesson — see [Pattern: Top K Elements](/cortex/data-structures-and-algorithms/trees-heap-pattern-top-k-elements).

***

# Practice Ladder

Five problems, easiest first. Each one has a hint — try the problem unaided first, hit the hint only if you've been stuck for ten minutes, and don't peek at solutions until you've made the heap *do something* in code.

1. **Kth Largest Element in an Array** ([LeetCode 215](https://leetcode.com/problems/kth-largest-element-in-an-array/)) — given an unsorted array and `k`, return the `k`-th largest element.
   > *Hint:* a min-heap of size `k`. Push every element; pop when size exceeds `k`. The heap's root is the answer at the end.

2. **Last Stone Weight** ([LeetCode 1046](https://leetcode.com/problems/last-stone-weight/)) — repeatedly take the two heaviest stones, smash them, return the heavier-minus-lighter back to the pile until one or zero stones remain.
   > *Hint:* max-heap. Pop twice each round; push back `|a − b|` if non-zero.

3. **Merge K Sorted Lists** ([LeetCode 23](https://leetcode.com/problems/merge-k-sorted-lists/)) — merge `k` sorted linked lists into one sorted list.
   > *Hint:* min-heap of size `k` holding one node from each list. Pop the smallest, append to output, push that node's `next` if it exists. Total: O(N log k) where N is total nodes — much better than the O(Nk) you'd get pairwise-merging.

4. **Find Median from Data Stream** ([LeetCode 295](https://leetcode.com/problems/find-median-from-data-stream/)) — design a class supporting `addNum(x)` and `findMedian()` as numbers stream in.
   > *Hint:* two heaps. A max-heap for the lower half, a min-heap for the upper half. Keep the sizes within one of each other. Median is the top of the larger heap, or the average of the two tops if sizes are equal.

5. **Reorganise String** ([LeetCode 767](https://leetcode.com/problems/reorganize-string/)) — rearrange a string so no two adjacent characters are equal, or report it's impossible.
   > *Hint:* max-heap by frequency. Pop the most-frequent character, append it to the output, then *park* it (don't push it back yet) until *the next* iteration so the same character can't be picked twice in a row. If the heap empties before you've placed every character, it's impossible.

Once these feel automatic, you've internalised every move in the heap toolkit — and you're ready for the array-implementation lesson where these patterns turn into 5–10 lines of tight, cache-friendly code.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Reading the chapter once builds understanding; cementing the items below is what makes recall fast.

## Quick Recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> Worst-case complexity of <code>heap.extract()</code>?</summary>

**A:** O(log n). Sift-down walks one root-to-leaf path.

</details>

<details>
<summary><strong>Q:</strong> Worst-case complexity of <code>heap.insert(x)</code>?</summary>

**A:** O(log n). Sift-up walks one leaf-to-root path.

</details>

<details>
<summary><strong>Q:</strong> Worst-case complexity of <code>heap.peek()</code>?</summary>

**A:** O(1). Read the root.

</details>

<details>
<summary><strong>Q:</strong> Cost of building a heap from an array of <code>n</code> items?</summary>

**A:** O(n) via bottom-up heapify. The naive insert-one-by-one approach is O(n log n).

</details>

<details>
<summary><strong>Q:</strong> Cost of <code>heap.delete(arbitrary value)</code> (not the root)?</summary>

**A:** O(n). Heaps aren't searchable; the find is linear, the structural fix is O(log n).

</details>

<details>
<summary><strong>Q:</strong> What invariant must every heap node satisfy?</summary>

**A:** Parent has higher priority than both children. (Nothing about left vs right — that's a BST rule.)

</details>

<details>
<summary><strong>Q:</strong> How is a binary heap stored in memory?</summary>

**A:** Flat array. Parent of index `i` is `(i-1)/2`; left child is `2i+1`; right child is `2i+2`.

</details>

<details>
<summary><strong>Q:</strong> How do you implement a max-heap with Python's <code>heapq</code> (which is min-heap only)?</summary>

**A:** Negate keys on push and pop, or push `(-priority, value)` tuples.

</details>

<details>
<summary><strong>Q:</strong> Is Java's <code>PriorityQueue</code> thread-safe?</summary>

**A:** No. Use `PriorityBlockingQueue` for concurrent producers.

</details>

<details>
<summary><strong>Q:</strong> Why does Linux's CFS scheduler use an RB-tree, not a heap, when both are O(log n)?</summary>

**A:** Heaps don't support ordered iteration or lookup-by-key. CFS needs both — to pick the leftmost task and to migrate tasks to other CPUs.

</details>

## Code Template

```python
import heapq

# Min-heap (default)
h = []
heapq.heappush(h, x)        # O(log n)
top = h[0]                  # O(1) peek
top = heapq.heappop(h)      # O(log n)

# Max-heap via negation
heapq.heappush(h, -x)
top = -h[0]
top = -heapq.heappop(h)

# Build from existing list — O(n), not O(n log n)
heapq.heapify(arr)

# Top-K largest from a stream — O(n log k)
heap = []
for x in stream:
    heapq.heappush(heap, x)
    if len(heap) > k:
        heapq.heappop(heap)
# heap holds the k largest; heap[0] is the smallest of them.
```

## Pattern Triggers

- **Top K largest elements** → min-heap of size K
- **Top K smallest elements** → max-heap of size K
- **Streaming median** → two heaps: max-heap of lower half + min-heap of upper half
- **Merge K sorted lists / streams** → min-heap with one entry per list
- **Schedule the next event by timestamp** → min-heap keyed by time
- **Pick the next-most-urgent task** → priority queue (heap)
- **Reorganise so no two adjacent items are equal** → max-heap by frequency, *park* the used element for one round
- **Dijkstra's shortest path** → min-heap of `(distance, vertex)` frontier
- **K-th largest element in a stream** → min-heap of size K; root is the answer
- **Optimal merge cost / Huffman tree** → min-heap of frequencies; pop two, push their sum

***

# Cross-Links

- **Prerequisite:** [Binary Tree](/cortex/data-structures-and-algorithms/trees-binary-tree-introduction-to-binary-trees) — the tree-shape vocabulary (root, height, completeness) used throughout this chapter.
- **Prerequisite for complexity claims:** [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — what `O(log n)` actually means, and how the height of a complete binary tree gives it to you for free.
- **Next chapter:** [Array Implementation of Heaps](/cortex/data-structures-and-algorithms/trees-heap-array-implementation-of-heaps) — store the imaginary tree in a flat array; parent and child relationships become arithmetic, not pointer chases.
- **Patterns built on this chapter:** [Top K Elements](/cortex/data-structures-and-algorithms/trees-heap-pattern-top-k-elements), [Comparator](/cortex/data-structures-and-algorithms/trees-heap-pattern-comparator).
- **Production deep dive (stub):** [Redis Internal Encodings](/cortex/data-structures-and-algorithms/dsa-in-real-systems-redis-internal-encodings) — where heaps and skip-lists appear inside Redis's data-structure layer.

***

## Final Takeaway

A heap is a binary tree with two simple invariants — **completeness** and **heap-ordering** — that together give you O(1) peek at the highest-priority value, O(log n) insert, and O(log n) extract. It's the data structure behind every priority queue you've ever seen: ER triage, Dijkstra, schedulers, event simulators, top-K leaderboards.

Three patterns to internalise from this lesson:

1. **Two invariants do all the work.** Completeness keeps the tree shallow (height O(log n)); the ordering rule keeps the root the most-important value. Every operation in the chapter is "do something simple, then restore both invariants".
2. **Heaps don't sort, they prioritise.** A heap is *not* a sorted array. Looking at any non-root node tells you *nothing* about the relative order of its descendants beyond the parent-child rule. The cheap O(log n) operations are the trade-off for not maintaining a full ordering.
3. **The two flavours are mirrors.** Min-heap and max-heap have identical algorithms with `<` swapped for `>`. Pick whichever fits your "high priority" definition — and language libraries make it a one-line comparator change.

The next lesson is where the magic shows up: we'll forget the imaginary tree and store the heap as **a flat array**. Parent and child indices become arithmetic, no pointers, no allocations per node — and every operation we just sketched compiles into 5–10 lines of tight, cache-friendly code.
