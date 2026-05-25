---
title: "Insertion In Singly Linked Lists"
summary: "Five insertion variants for a singly linked list — at head, at tail, after a given node, before a given node, at a distance — all reducing to the same three-line splice. The cost lives in the walk, never in the wire-up."
---

# 3. Insertion in Singly Linked Lists

## The Hook

In an array, "insert" is a lie. You don't actually insert — you **shift**. Every element past the insertion point slides one slot to the right, a cascade of memory writes that grows with the array. That's why `list.insert(0, x)` in Python on a million-element list is *slow* and `list.append(x)` is fast: one is an O(n) shift, the other is an O(1) write.

Linked lists flip the script. Insert at the head? **Two pointer assignments. O(1).** Doesn't matter if the list has 10 nodes or 10 million. What you give up is random access — inserting in the *middle* still requires walking there first. The entire zoo of "insert at X" problems in this lesson comes down to one question: **where do we already have a pointer?** If it's the head, insertion is O(1). If it's somewhere in the middle, we pay O(n) to walk, then O(1) to splice.

Five variations follow — head, tail, after-given, before-given, at-distance. They all reduce to the same three-line splice: **create a node, link it into its successor, link its predecessor to it**. Master the splice once, and every insertion problem you'll ever face is solved.

---

## Table of contents

1. [Understanding the Problem](#understanding-the-problem)
2. [Supported Operations](#supported-operations)
3. [Internal Mechanics](#internal-mechanics)
4. [Understanding insertion at beginning](#understanding-insertion-at-beginning)
5. [Insert at beginning](#insert-at-beginning)
6. [Understanding insertion at end](#understanding-insertion-at-end)
7. [Insert at end](#insert-at-end)
8. [Understanding insertion after the given node](#understanding-insertion-after-the-given-node)
9. [Insert after the given node](#insert-after-the-given-node)
10. [Understanding insertion before the given node](#understanding-insertion-before-the-given-node)
11. [Insert before the given node](#insert-before-the-given-node)
12. [Understanding insertion at a given distance](#understanding-insertion-at-a-given-distance)
13. [Insert at given distance](#insert-at-given-distance)
14. [Working Example](#working-example)
15. [Edge Cases and Pitfalls](#edge-cases-and-pitfalls)
16. [Production Reality](#production-reality)
17. [Practice Ladder](#practice-ladder)
18. [Quiz](#quiz)
19. [Further Reading](#further-reading)
20. [Cross-Links](#cross-links)
21. [Final Takeaway](#final-takeaway)

***

# Understanding the Problem

Insertion is the operation that exposes what each data structure is *good at*. Arrays hide a cascade of `O(n)` element shifts behind a one-line `arr.insert(i, x)`; linked lists turn a five-character "insert at index 0" into a fast two-pointer-assignment splice. The same word, two completely different machines underneath.

The trap is in the verb itself:

- **Arrays** "insert" by *shifting* — every element past the cut point slides over to make room. Insertion in the middle is `O(n)` time and `O(1)` space.
- **Linked lists** "insert" by *re-wiring* — no element moves; only two `.next` pointers change. Insertion is `O(1)` time *once the right node is in hand*, `O(1)` space.

To make this concrete: prepending to a one-million-element Python list (`lst.insert(0, x)`) executes a million `memmove` writes; prepending to a one-million-node linked list flips two pointers and returns. The work is in the *layout*, not the operation name.

So the key idea is: in a linked list, inserting is two pointer assignments. The expensive part is **finding** the predecessor — and that's the cost you control by choosing which insertion variant to use.

---

# Supported Operations

A singly linked list supports five insertion variants — distinguished by *what reference you already hold* into the list. The wire-up is always the same three lines; the cost varies with how far you have to walk to reach the splice point.

| Operation | Inputs | Time | Space | Notes |
|---|---|---|---|---|
| Insert at beginning | `head`, `data` | `O(1)` | `O(1)` | Two pointer ops; cost is independent of list length. |
| Insert at end | `head`, `data` | `O(n)` time / `O(1)` with cached tail | `O(1)` | Walk to the tail unless the list caches a `tail` pointer. |
| Insert after given node | `node`, `data` | `O(1)` | `O(1)` | Node reference is given; no walk needed. |
| Insert before given node | `head`, `node`, `data` | `O(n)` worst, `O(1)` if `node == head` | `O(1)` | Walk to find the predecessor — singly linked lists are forward-only. |
| Insert at distance `X` | `head`, `X`, `data` | `O(X)` time | `O(1)` | Walk `X − 1` steps, then splice. Out-of-range `X` returns the list unchanged. |

Two pieces are constant across the table: every variant allocates **one** node (`O(1)` extra space) and every variant ends in the same splice. The variability is purely in the *walk*.

To make this concrete: inserting after a node you already hold (`O(1)`) and inserting at position `X = 0` (also `O(1)`) take the same wall-clock time on a billion-node list. Inserting at position `X = n − 1`, by contrast, is `O(n)` time — you walk the whole list to find the predecessor. The cost lives in the search, not the splice.

So the tradeoff is: linked-list insertion trades random-access (you can't jump to "position 500" in `O(1)` time) for cheap modification — once you've found the splice point, the structural change is two pointer assignments regardless of list size.

---

# Internal Mechanics

Every insertion in a singly linked list — at the head, at the tail, after a node, before a node, or at a distance — compiles down to the same three operations. Lock these into muscle memory and every variant in this lesson becomes a five-minute exercise.

The three operations:

- **Allocate**: create a fresh `ListNode` holding the new data. `new_node.next` starts as `null`.
- **Wire forward**: set `new_node.next` to the node that should follow the new one (its *successor*).
- **Wire backward**: update the *predecessor's* `.next` to point at the new node.

The order of those two pointer writes is not cosmetic — it is the entire correctness argument for the splice. **Always wire forward first.**

To make this concrete: suppose we want to insert `6` between `7` and `3` in `5 → 7 → 3`. The given node is `7`, so:

- `new_node.next = node.next` wires forward — `new_node` now points at `3`.
- `node.next = new_node` wires backward — `7` now points at `new_node`.

Reverse the lines and the list breaks. `node.next = new_node` runs first, so the reference to `3` is gone — `node.next` no longer holds it. The next line `new_node.next = node.next` then reads back `new_node` and writes it into `new_node.next` — a self-loop. Everything past `7` has been dropped on the floor.

The same rule covers the head case (the predecessor is the `head` variable itself) and the tail case (the successor is `null`). When the predecessor is given (`insert at beginning`, `insert after`), the splice is `O(1)` time. When the predecessor must be searched for (`insert before`, `insert at distance`), the walk dominates and the cost climbs to `O(n)` time.

So the core insight is: every insertion is "allocate, then two pointer writes, in the order successor-first, predecessor-second" — what differs across the five variants is only how the predecessor is found.

> 🖼 Diagram — TODO: three-frame splice — allocate the new node, wire forward (`new.next = successor`), wire backward (`predecessor.next = new`); a fourth frame illustrating the self-loop bug if the order is reversed.

---

# Understanding insertion at beginning

Inserting at the beginning of a linked list is a fundamental and commonly used operation. It is an efficient method, especially when extending a list, and requires only a few lines of code to implement. When designing an algorithm for any data structure, it's important not to make assumptions about its underlying characteristics and to design the logic for a general case. With that in mind, there are two cases to consider when inserting at the beginning of a singly linked list.

## 1\. The list is empty

In this scenario, if the linked list is empty, the **head** would be `null`. We need to initialize the **head** node of the linked list and ensure that the pointer of this newly created **head** node is `null`, as this new node will also be the last node of the list.

```d3 widget=linked-list
{
  "title": "Insert at beginning — empty list: the new node becomes both head and tail",
  "direction": "single",
  "nodes": [{"id": "n", "value": "6"}],
  "head": "n",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "Before: head = null (empty list)"
    },
    {
      "nodes": [{"id": "n", "value": "6", "style": "new"}],
      "links": [],
      "markers": [{"name": "head", "nodeId": "n"}],
      "msg": "Allocate the new node; head ← newNode. newNode.next = null (it's also the tail)."
    }
  ]
}
```

<p align="center"><strong>Case 1 — empty list: create a single node and make it the head; its <code>next</code> is <code>null</code> since it is also the tail.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the new node's `next` pointer to `null` since it's the only node.
> -   **Step 3:** Return the new node, as this node is also the **head** node.

## 2\. The list is not empty

In this scenario, we already have some data in the linked list, so the **head** is not `null`. Therefore, to insert a new node at the beginning of the list, we need to update the pointer of the newly created node to store the reference of the existing **head** node.

```d3 widget=linked-list
{
  "title": "Insert at beginning — non-empty list: two pointer updates, O(1)",
  "direction": "single",
  "nodes": [
    {"id": "new", "value": "6"},
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Before: head → 5 → 7 → 3"
    },
    {
      "nodes": [
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Step 1: allocate new node with value 6"
    },
    {
      "nodes": [
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["new","n1"],["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Step 2: newNode.next = head (point new node at old head)"
    },
    {
      "nodes": [
        {"id": "new", "value": "6"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["new","n1"],["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "new"}],
      "msg": "Step 3: head = newNode — done in O(1)"
    }
  ]
}
```

<p align="center"><strong>Case 2 — non-empty list: point the new node's <code>next</code> to the old head, then make the new node the new head.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the new node's `next` pointer to hold the reference of the current head.
> -   **Step 3:** Return the new node, as this is the new head.

## Implementation

When implementing the logic for the insert at the beginning operation, we consider both possible cases and write the code for each in conditional blocks.


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def insert_at_beginning(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If the list is empty (head is None)
        if head is None:

            # Set the next pointer to None since it's the only node
            new_node.next = None

            # Return the new_node as this is the new head
            return new_node

        # Set the next pointer of the new node to the current head,
        # making the new node the new head
        new_node.next = head

        # Return the new_node as this is the new head
        return new_node
```

```java run
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 * };
 */

class Solution {
    public ListNode insertAtBeginning(ListNode head, int data) {

        // Create a new node with the given data
        ListNode newNode = new ListNode(data);

        // If the list is empty (head is null)
        if (head == null) {

            // Set the next pointer to null since it's the only node
            newNode.next = null;

            // Return the newNode as this is the new head
            return newNode;
        }

        // Set the next pointer of the new node to the current head,
        // making the new node the new head
        newNode.next = head;

        // Return the newNode as this is the new head
        return newNode;
    }
}
```


## Complexity Analysis

The time complexity of the above function does not depend on the list size. In all cases, we always need to insert the node at the start of the list, which takes **constant** time, i.e., **O(1)**.

```d3 widget=linked-list
{
  "title": "Insert before the head — 2 pointer ops, O(1) regardless of list size",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [],
      "msg": "Before: head → 5 → 7 → 3 → null"
    },
    {
      "nodes": [
        {"id": "n0", "value": "6", "style": "new"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n0","n1"],["n1","n2"],["n2","n3"]],
      "markers": [{"name": "current", "nodeId": "n0"}],
      "head": "n1",
      "msg": "Step 1: newNode.next = head — the new node points at the old head (1 pointer op)"
    },
    {
      "nodes": [
        {"id": "n0", "value": "6", "style": "new"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n0","n1"],["n1","n2"],["n2","n3"]],
      "markers": [],
      "head": "n0",
      "msg": "Step 2: head = newNode — head moves to the new node (1 pointer op). Total: O(1), independent of list size."
    }
  ]
}
```

<p align="center"><strong>Insert before the head node — only 2 pointer assignments needed, regardless of list size: always O(1) time.</strong></p>

The space complexity of the function is also **O(1)** because it only creates a single new node and does not use any additional data structures.

> **Best Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**
>
> **Worst Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**

***

# Insert at beginning

## Problem Statement

Given the **head** of a singly linked list and a **data** value, write a function to insert a new node with the given data value at the beginning of the linked list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], data = 6
> -   **Output:** \[6, 5, 7, 3, 10\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional


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
    def insert_at_beginning(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If the list is empty (head is None)
        if head is None:

            # Set the next pointer to None since it's the only node
            new_node.next = None

            # Return the new_node as this is the new head
            return new_node

        # Set the next pointer of the new node to the current head,
        # making the new node the new head
        new_node.next = head

        # Return the new_node as this is the new head
        return new_node


# Example from the problem statement
print(to_list(Solution().insert_at_beginning(from_list([5, 7, 3, 10]), 6)))  # [6, 5, 7, 3, 10]

# Edge cases
print(to_list(Solution().insert_at_beginning(None, 1)))                      # [1]
print(to_list(Solution().insert_at_beginning(from_list([42]), 0)))           # [0, 42]
print(to_list(Solution().insert_at_beginning(from_list([1, 2]), 99)))        # [99, 1, 2]
print(to_list(Solution().insert_at_beginning(from_list([3, 3, 3]), 3)))      # [3, 3, 3, 3]
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
        public ListNode insertAtBeginning(ListNode head, int data) {

            // Create a new node with the given data
            ListNode newNode = new ListNode(data);

            // If the list is empty (head is null)
            if (head == null) {

                // Set the next pointer to null since it's the only node
                newNode.next = null;

                // Return the newNode as this is the new head
                return newNode;
            }

            // Set the next pointer of the new node to the current head,
            // making the new node the new head
            newNode.next = head;

            // Return the newNode as this is the new head
            return newNode;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().insertAtBeginning(fromList(5, 7, 3, 10), 6)));  // [6, 5, 7, 3, 10]

        // Edge cases
        System.out.println(toList(new Solution().insertAtBeginning(null, 1)));                   // [1]
        System.out.println(toList(new Solution().insertAtBeginning(fromList(42), 0)));           // [0, 42]
        System.out.println(toList(new Solution().insertAtBeginning(fromList(1, 2), 99)));        // [99, 1, 2]
        System.out.println(toList(new Solution().insertAtBeginning(fromList(3, 3, 3), 3)));      // [3, 3, 3, 3]
    }
}
```

</details>


***

# Understanding insertion at end

Inserting at the end of a list is a common operation used to extend the list. Unlike insertion at the beginning, this operation adds a new node at the end of the list. To add a new node at the end of a singly linked list, we first need to locate the tail node of the linked list. Then, we can create a new node and update the pointer of the tail node to point to the newly created node. Since we need to traverse the entire list to insert the new node, this operation is not as efficient as insertion at the beginning. When inserting at the end of a singly linked list, there are two cases to consider.

## 1\. The list is empty

If the linked list is empty, the **head** is `null`. We create a new node and make it the head — it is also the tail since it's the only node.

```d3 widget=linked-list
{
  "title": "Insert at end — empty list: same as insert at beginning when there's nothing yet",
  "direction": "single",
  "nodes": [{"id": "n", "value": "6"}],
  "head": "n",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "Before: head = null (empty list)"
    },
    {
      "nodes": [{"id": "n", "value": "6", "style": "new"}],
      "links": [],
      "markers": [{"name": "head", "nodeId": "n"}],
      "msg": "Create newNode and make it the head — newNode.next = null"
    }
  ]
}
```

<p align="center"><strong>Case 1 — empty list: the new node becomes both head and tail.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the new node's `next` pointer to `null`.
> -   **Step 3:** Return the new node as the head.

## 2\. The list is not empty

We traverse to the last node (whose `next` is `null`) and link the new node after it.

```d3 widget=linked-list
{
  "title": "Insert at end — non-empty list: walk to tail then attach",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"},
    {"id": "new", "value": "6"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "current", "nodeId": "n1"}],
      "msg": "Before: head → 5 → 7 → 3. Start curr at head."
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "current", "nodeId": "n3"}],
      "msg": "Walk to the tail (curr.next == null) — O(n)"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "new", "value": "6", "style": "new"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "current", "nodeId": "n3"}],
      "msg": "Allocate newNode"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "new", "value": "6"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","new"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "tail.next = newNode — done. Total O(n) (walk + attach)."
    }
  ]
}
```

<p align="center"><strong>Case 2 — non-empty list: traverse to the tail, then set <code>tail.next = newNode</code>.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Traverse the list until `current.next == null` (current is the tail).
> -   **Step 3:** Set `current.next = newNode`.
> -   **Step 4:** Return the original head.

## Implementation


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def insert_at_end(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If the list is empty
        if head is None:

            # Set the next pointer of the new node to None
            new_node.next = None

            # Return the new node as the new head of the list
            return new_node

        # Traverse the list to find the last node
        current: Optional[ListNode] = head
        while current is not None and current.next is not None:
            current = current.next

        # Set the next pointer of the new node to None
        new_node.next = None

        # Link the last node to the new node
        if current:
            current.next = new_node

        # Return the original head of the list
        return head
```

```java run
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 * };
 */

class Solution {
    public ListNode insertAtEnd(ListNode head, int data) {

        // Create a new node with the given data
        ListNode newNode = new ListNode(data);

        // If the list is empty
        if (head == null) {

            // Set the next pointer of the new node to null
            newNode.next = null;

            // Return the new node as the new head of the list
            return newNode;
        }

        // Traverse the list to find the last node
        ListNode current = head;
        while (current != null && current.next != null) {
            current = current.next;
        }

        // Set the next pointer of the new node to null
        newNode.next = null;

        // Link the last node to the new node
        current.next = newNode;

        // Return the original head of the list
        return head;
    }
}
```


## Complexity Analysis

To insert at the end, we must traverse the entire list to reach the tail node.

```d2
direction: right
n1: "val: 5"
n2: "val: 7"
n3: |md
  val: 3

  `next: null`

  (tail)
|
new: {
  val: 6
  next: "null"
  style.fill: "#dcfce7"
  style.stroke: "#16a34a"
}
n1 -> n2 -> n3
n3 -> new.val: "tail.next = newNode"

cur: "current\ntraverses n nodes" {shape: oval}
cur -> n3: "O(n) walk" {style.stroke-dash: 3}
```

<p align="center"><strong>Insert after the tail node — O(n) traversal to reach the tail, then O(1) pointer update.</strong></p>

> **Best Case / Worst Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(n)**

***

# Insert at end

## Problem Statement

Given the **head** of a singly linked list and a **data** value, write a function to insert a new node with the given data value at the end of the linked list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], data = 6
> -   **Output:** \[5, 7, 3, 10, 6\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional


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
    def insert_at_end(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If the list is empty
        if head is None:

            # Set the next pointer of the new node to None
            new_node.next = None

            # Return the new node as the new head of the list
            return new_node

        # Traverse the list to find the last node
        current: Optional[ListNode] = head
        while current is not None and current.next is not None:
            current = current.next

        # Set the next pointer of the new node to None
        new_node.next = None

        # Link the last node to the new node
        if current:
            current.next = new_node

        # Return the original head of the list
        return head


# Example from the problem statement
print(to_list(Solution().insert_at_end(from_list([5, 7, 3, 10]), 6)))  # [5, 7, 3, 10, 6]

# Edge cases
print(to_list(Solution().insert_at_end(None, 1)))                      # [1]
print(to_list(Solution().insert_at_end(from_list([42]), 99)))          # [42, 99]
print(to_list(Solution().insert_at_end(from_list([1, 2]), 3)))         # [1, 2, 3]
print(to_list(Solution().insert_at_end(from_list([5, 5, 5]), 5)))      # [5, 5, 5, 5]
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
        public ListNode insertAtEnd(ListNode head, int data) {

            // Create a new node with the given data
            ListNode newNode = new ListNode(data);

            // If the list is empty
            if (head == null) {

                // Set the next pointer of the new node to null
                newNode.next = null;

                // Return the new node as the new head of the list
                return newNode;
            }

            // Traverse the list to find the last node
            ListNode current = head;
            while (current != null && current.next != null) {
                current = current.next;
            }

            // Set the next pointer of the new node to null
            newNode.next = null;

            // Link the last node to the new node
            current.next = newNode;

            // Return the original head of the list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().insertAtEnd(fromList(5, 7, 3, 10), 6)));  // [5, 7, 3, 10, 6]

        // Edge cases
        System.out.println(toList(new Solution().insertAtEnd(null, 1)));                   // [1]
        System.out.println(toList(new Solution().insertAtEnd(fromList(42), 99)));          // [42, 99]
        System.out.println(toList(new Solution().insertAtEnd(fromList(1, 2), 3)));         // [1, 2, 3]
        System.out.println(toList(new Solution().insertAtEnd(fromList(5, 5, 5), 5)));      // [5, 5, 5, 5]
    }
}
```

</details>


***

# Understanding insertion after the given node

Inserting after a given node in a singly linked list is a relatively straightforward operation. Unlike arrays, we can insert data at any point in a linked list without recreating the entire list. When inserting after a node in a linked list, there are two cases to consider.

## 1\. The list is empty

If the list is empty and contains no elements, we cannot find the given node because it does not exist within the list. Inserting a new node after the given node is not possible because there is no reference point within the list to perform the insertion. In such a case, the method would return without making any changes.

```d3 widget=linked-list
{
  "title": "Insert after given node — empty list or null reference: nothing to do",
  "direction": "single",
  "nodes": [{"id": "placeholder", "value": "—"}],
  "head": "placeholder",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "node = null and/or head = null — no reference point. Return immediately."
    }
  ]
}
```

<p align="center"><strong>Case 1 — empty list or null node: the function returns immediately with no changes.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return from the function.

## 2\. The list is not empty

Since the new node will be inserted between two existing nodes, we must ensure that we properly set up the pointers of these nodes. Inserting after a given node is a three-step process.

```d3 widget=linked-list
{
  "title": "Insert 6 after node(7) — bridge new.next first, then redirect node.next",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "new", "value": "6"},
    {"id": "n3", "value": "3"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "current", "nodeId": "n2"}],
      "msg": "Before: list = [5, 7, 3]; insert 6 after node(7)"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"],["new","n3"]],
      "markers": [{"name": "current", "nodeId": "n2"}],
      "msg": "Step 1: newNode.next = node.next — new bridges over to node 3"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","new"],["new","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Step 2: node.next = newNode — splice complete. O(1)."
    }
  ]
}
```

<p align="center"><strong>Case 2 — non-empty list: bridge the new node in by wiring its <code>next</code> first, then redirecting the given node.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the `next` pointer of the new node to hold the node's reference stored in the `next` pointer of the `given` node.
> -   **Step 3:** Set the `next` pointer of the `given` node to hold the reference of the new node.

## Implementation

We will be given the node, after which we will perform the insertion. When implementing the logic for the operation, we consider both possible cases and write the code for each in conditional blocks.


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def insert_after_the_given_node(
        self, node: Optional[ListNode], data: int
    ) -> None:

        # Check if the given node is None
        if node is None:

            # If the given node is None, there is nothing to do
            return

        # Create a new node with the provided data
        new_node: ListNode = ListNode(data)

        # Set the next pointer of the new node to the next pointer of the
        # given node
        new_node.next = node.next

        # Set the next pointer of the given node to the new node
        node.next = new_node
```

```java run
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 * };
 */

class Solution {
    public void insertAfterTheGivenNode(ListNode node, int data) {

        // Check if the given node is null
        if (node == null) {

            // If the given node is null, there is nothing to do
            return;
        }

        // Create a new node with the provided data
        ListNode newNode = new ListNode(data);

        // Set the next pointer of the new node to the next pointer of
        // the given node
        newNode.next = node.next;

        // Set the next pointer of the given node to the new node
        node.next = newNode;
    }
}
```


**Will changing the order of these operations have any effect on the outcome?**

It is crucial to update the newly created node **before** modifying the pointer of the given node. If we modify the given node first, we will lose the reference to the next node in the chain — `node.next = newNode` overwrites the only pointer to the rest of the list, making `newNode.next = node.next` store a self-reference instead.

## Complexity Analysis

The time complexity of the above function is not affected by the length of the linked list because it only involves inserting a new node after the given node and performing pointer manipulations around the given node. Since these operations take constant time, the function's time complexity is **O(1)**.

```d2
direction: right
n1: "val: 5"
given: |md
  val: 7

  (given node)
|
new: {
  val: 6
  next
  style.fill: "#dcfce7"
  style.stroke: "#16a34a"
}
n3: "val: 3"
n4: |md
  val: 10

  `next: null`
|
n1 -> given -> new.val
new.next -> n3
n3 -> n4

note: "Only 2 pointer ops\nO(1) — no traversal" {shape: oval}
note -> given: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Insert after the given node — only 2 pointer assignments regardless of list size: always O(1) time.</strong></p>

The function's space complexity is **O(1)** because it only creates a single new node and does not use any additional data structures.

> **Best Case / Worst Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**

***

# Insert after the given node

## Problem Statement

Given a reference to a **random node** in a singly linked list and a **data** value, write a function to insert a new node with the given data value after the given node.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], node = 7, data = 6
> -   **Output:** \[5, 7, 6, 3, 10\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional


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
    def insert_after_the_given_node(
        self, node: Optional[ListNode], data: int
    ) -> None:

        # Check if the given node is None
        if node is None:

            # If the given node is None, there is nothing to do
            return

        # Create a new node with the provided data
        new_node: ListNode = ListNode(data)

        # Set the next pointer of the new node to the next pointer of the
        # given node
        new_node.next = node.next

        # Set the next pointer of the given node to the new node
        node.next = new_node


# Example from the problem statement — insert 6 after node with val 7
h1 = from_list([5, 7, 3, 10])
Solution().insert_after_the_given_node(h1.next, 6)           # insert after 7
print(to_list(h1))                                           # [5, 7, 6, 3, 10]

# Insert after last node
h2 = from_list([1, 2, 3])
Solution().insert_after_the_given_node(h2.next.next, 99)     # insert after 3 (tail)
print(to_list(h2))                                           # [1, 2, 3, 99]

# Insert after head (single-node list)
h3 = from_list([42])
Solution().insert_after_the_given_node(h3, 7)
print(to_list(h3))                                           # [42, 7]

# node is None — no-op
h4 = from_list([1, 2])
Solution().insert_after_the_given_node(None, 5)
print(to_list(h4))                                           # [1, 2]

# Insert after head in two-node list
h5 = from_list([1, 2])
Solution().insert_after_the_given_node(h5, 9)
print(to_list(h5))                                           # [1, 9, 2]
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
        public void insertAfterTheGivenNode(ListNode node, int data) {

            // Check if the given node is null
            if (node == null) {

                // If the given node is null, there is nothing to do
                return;
            }

            // Create a new node with the provided data
            ListNode newNode = new ListNode(data);

            // Set the next pointer of the new node to the next pointer of
            // the given node
            newNode.next = node.next;

            // Set the next pointer of the given node to the new node
            node.next = newNode;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement — insert 6 after node with val 7
        ListNode h1 = fromList(5, 7, 3, 10);
        new Solution().insertAfterTheGivenNode(h1.next, 6);  // insert after 7
        System.out.println(toList(h1));                       // [5, 7, 6, 3, 10]

        // Insert after last node
        ListNode h2 = fromList(1, 2, 3);
        new Solution().insertAfterTheGivenNode(h2.next.next, 99); // insert after 3 (tail)
        System.out.println(toList(h2));                       // [1, 2, 3, 99]

        // Insert after head (single-node list)
        ListNode h3 = fromList(42);
        new Solution().insertAfterTheGivenNode(h3, 7);
        System.out.println(toList(h3));                       // [42, 7]

        // node is null — no-op
        ListNode h4 = fromList(1, 2);
        new Solution().insertAfterTheGivenNode(null, 5);
        System.out.println(toList(h4));                       // [1, 2]

        // Insert after head in two-node list
        ListNode h5 = fromList(1, 2);
        new Solution().insertAfterTheGivenNode(h5, 9);
        System.out.println(toList(h5));                       // [1, 9, 2]
    }
}
```

</details>


***

# Understanding insertion before the given node

Inserting before a given node may seem simple, just like inserting after a node. However, upon closer observation, it is not that straightforward because we don't have access to the node one step before where the new node is inserted. In the previous lesson about inserting after a given node, the given node itself was the previous node, as the new node was inserted after the given node. In this case, however, the given node acts as the next node, and the node before the given node is what needs to be changed. Let's examine the cases we need to consider.

## 1\. The list is empty

If the list is empty and contains no elements, we cannot find the given node because it does not exist within the list. Inserting a new node before the given node is not possible. In such a case, we return the **head** node as-is.

```d3 widget=linked-list
{
  "title": "Insert before given node — empty list: return head unchanged",
  "direction": "single",
  "nodes": [{"id": "placeholder", "value": "—"}],
  "head": "placeholder",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "head = null — given node cannot exist. Return null."
    }
  ]
}
```

<p align="center"><strong>Case 1 — empty list: return the head immediately with no changes.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The given node is the first node

This is similar to **inserting at the beginning**, which we learned earlier. To determine if the given node is the first node, we compare it to the **head** node. If both are the same object, the given node is the head.

```d3 widget=linked-list
{
  "title": "Insert before given node — given == head: same as insert-at-beginning",
  "direction": "single",
  "nodes": [
    {"id": "new", "value": "6"},
    {"id": "n1", "value": "7"},
    {"id": "n2", "value": "3"},
    {"id": "n3", "value": "10"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "7"},
        {"id": "n2", "value": "3"},
        {"id": "n3", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}, {"name": "current", "nodeId": "n1"}],
      "msg": "Before: node == head. Given is node(7)."
    },
    {
      "nodes": [
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n1", "value": "7"},
        {"id": "n2", "value": "3"},
        {"id": "n3", "value": "10"}
      ],
      "links": [["new","n1"],["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "new"}],
      "msg": "newNode.next = head, return newNode — done in O(1)"
    }
  ]
}
```

<p align="center"><strong>Case 2 — given node is the head: same as insert-at-beginning; the new node becomes the new head.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the new node's `next` pointer to hold the reference of the current head.
> -   **Step 3:** Return the new node, as this is the new head.

## 3\. The given node is not the first node

This case is not easy, but it becomes simpler once we understand the concept behind it. The problem is that we don't have a reference to the node just before the given node. Without that predecessor, we can't rewire its `next` pointer after inserting.

```d3 widget=linked-list
{
  "title": "Problem — singly-linked lists are forward-only, so the predecessor is unknown",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "10"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n3"}],
      "msg": "Want to insert BEFORE node(3). But which node points to node(3)? We have no back-pointer."
    }
  ]
}
```

<p align="center"><strong>The challenge: we have a reference to the given node but not to the node before it — that predecessor is the one whose pointer must change.</strong></p>

**How do we get the reference of the previous node?**

We create a `previous` pointer initialised to `null`. As we traverse, we update both `current` and `previous` together at each step. When `current` reaches the given node, `previous` holds its predecessor. The problem then reduces to **inserting after the previous node** — which we already know how to do.

```d3 widget=linked-list
{
  "title": "Insert before given node — track previous + current as you walk",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "new", "value": "6"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "10"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "previous", "nodeId": "n1"}, {"name": "current", "nodeId": "n2"}, {"name": "next", "nodeId": "n3"}],
      "msg": "Start: previous=head, current=head.next. Given=node(3)."
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "previous", "nodeId": "n2"}, {"name": "current", "nodeId": "n3"}],
      "msg": "Walk forward — current == given (node 3). previous = node(7)."
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["new","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "new"}],
      "msg": "Allocate newNode; newNode.next = current (point at node 3)"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","new"],["new","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "previous.next = newNode — splice complete. O(n) walk + O(1) wire."
    }
  ]
}
```

<p align="center"><strong>Case 3 — non-head node: traverse with two pointers until <code>current == given</code>, then wire the new node between <code>previous</code> and <code>current</code>.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Traverse while keeping track of `current` and `previous` nodes until `current == given`.
> -   **Step 3:** Set the new node's `next` pointer to hold the reference of the `given` node (`current`).
> -   **Step 4:** Set the `next` pointer of the `previous` node to hold the reference of the new node.
> -   **Step 5:** Return the original head node.

## Implementation


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def insert_before_the_given_node(
        self,
        head: Optional[ListNode],
        node: Optional[ListNode],
        data: int,
    ) -> Optional[ListNode]:

        # Check if the head or node is None
        if head is None or node is None:
            return head

        # Create a new node with the given data
        new_node = ListNode(data)

        # If the given node is the head, insert the new node before it
        if node == head:
            new_node.next = head

            # Return the new_node as this is the new head
            return new_node

        # Traverse the linked list until the current node matches the
        # given node
        current = head
        previous = None

        while current is not None and current != node:
            previous = current
            current = current.next

        # If the current node is None, the given node was not found in
        # the linked list
        if current is None:
            return head

        # Insert the new node before the given node
        new_node.next = current
        previous.next = new_node

        # Return the head of the modified linked list
        return head
```

```java run
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 * };
 */

class Solution {
    public ListNode insertBeforeTheGivenNode(
        ListNode head,
        ListNode node,
        int data
    ) {

        // Check if the head or node is null
        if (head == null || node == null) {
            return head;
        }

        // Create a new node with the given data
        ListNode newNode = new ListNode(data);

        // If the given node is the head, insert the new node before it
        if (node == head) {
            newNode.next = head;

            // Return the newNode as this is the new head
            return newNode;
        }

        // Traverse the linked list until the current node matches the
        // given node
        ListNode current = head;
        ListNode previous = null;

        while (current != null && current != node) {
            previous = current;
            current = current.next;
        }

        // If the current node is null, the given node was not found in
        // the linked list
        if (current == null) {
            return head;
        }

        // Insert the new node before the given node
        newNode.next = current;
        previous.next = newNode;

        // Return the head of the modified linked list
        return head;
    }
}
```


**Does the order of updating `previous` and `current` matter?**

Yes — critically. These two lines must happen in this order:

```python
previous = current     # 1. Save current position as predecessor
current = current.next # 2. Then advance current
```

If you reverse them:

```python
current = current.next # Advance first...
previous = current     # Now previous == current (points to the new position, not the old one)
```

`previous` ends up pointing to the same node as `current`, losing the predecessor reference entirely. The invariant that must hold after every loop iteration is: `previous` is the node immediately before `current`.

## Complexity Analysis

The time complexity depends on where the given node sits in the list.

### Best case

The given node is the head. No traversal needed — just a pointer update. **O(1)**.

```d2
direction: right
new: |md
  val: 6

  (new)
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
n1: |md
  val: 7

  (was head)
|
n2: "val: 3"
n3: |md
  val: 10

  `next: null`
|
new -> n1: "newNode.next = head\n— 1 pointer op, O(1)"
n1 -> n2 -> n3
```

<p align="center"><strong>Best case — given node is the head: O(1), no traversal required.</strong></p>

### Worst case

The given node is the tail. The traversal visits every node to find its predecessor. **O(N)**.

```d2
direction: right
n1: "val: 5"
n2: "val: 7"
n3: "val: 3"
new: |md
  val: 6

  (new)
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
n4: |md
  val: 10

  (given, tail)
|
n1 -> n2 -> n3 -> new -> n4

cur: "current traverses\nn−1 nodes" {shape: oval}
cur -> n3: "O(n) walk" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — given node is the tail: O(N) traversal to find the predecessor.</strong></p>

> **Best Case** — given node is the head:
>
> -   Space Complexity — **O(1)**
> -   Time Complexity — **O(1)**
>
> **Worst Case** — given node is the tail:
>
> -   Space Complexity — **O(1)**
> -   Time Complexity — **O(N)**

***

# Insert before the given node

## Problem Statement

Given the **head** of a singly linked list, a reference to a **random node** in that linked list, and a **data** value, write a function to insert a new node with the given data before the given node and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], node = 7, data = 6
> -   **Output:** \[5, 6, 7, 3, 10\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional


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
    def insert_before_the_given_node(
        self,
        head: Optional[ListNode],
        node: Optional[ListNode],
        data: int,
    ) -> Optional[ListNode]:

        # Check if the head or node is null
        if head is None or node is None:
            return head

        # Create a new node with the given data
        new_node = ListNode(data)

        # If the given node is the head, insert the new node before it
        if node == head:
            new_node.next = head

            # Return the newNode as this is the new head
            return new_node

        # Traverse the linked list until the current node matches the
        # given node
        current = head
        previous = None

        while current is not None and current != node:
            previous = current
            current = current.next

        # If the current node is null, the given node was not found in
        # the linked list
        if current is None:
            return head

        # Insert the new node before the given node
        new_node.next = current
        previous.next = new_node

        # Return the head of the modified linked list
        return head


# Example from the problem statement — insert 6 before node with val 7
h1 = from_list([5, 7, 3, 10])
print(to_list(Solution().insert_before_the_given_node(h1, h1.next, 6)))      # [5, 6, 7, 3, 10]

# Insert before head
h2 = from_list([5, 7, 3, 10])
print(to_list(Solution().insert_before_the_given_node(h2, h2, 0)))            # [0, 5, 7, 3, 10]

# Insert before last node
h3 = from_list([1, 2, 3])
print(to_list(Solution().insert_before_the_given_node(h3, h3.next.next, 9))) # [1, 2, 9, 3]

# Single-node list — insert before the only node (becomes head)
h4 = from_list([42])
print(to_list(Solution().insert_before_the_given_node(h4, h4, 7)))           # [7, 42]

# node is None — no change
h5 = from_list([1, 2])
print(to_list(Solution().insert_before_the_given_node(h5, None, 5)))         # [1, 2]

# Two-node list — insert before second node
h6 = from_list([1, 2])
print(to_list(Solution().insert_before_the_given_node(h6, h6.next, 99)))     # [1, 99, 2]
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
        public ListNode insertBeforeTheGivenNode(
            ListNode head,
            ListNode node,
            int data
        ) {

            // Check if the head or node is null
            if (head == null || node == null) {
                return head;
            }

            // Create a new node with the given data
            ListNode newNode = new ListNode(data);

            // If the given node is the head, insert the new node before it
            if (node == head) {
                newNode.next = head;

                // Return the newNode as this is the new head
                return newNode;
            }

            // Traverse the linked list until the current node matches the
            // given node
            ListNode current = head;
            ListNode previous = null;

            while (current != null && current != node) {
                previous = current;
                current = current.next;
            }

            // If the current node is null, the given node was not found in
            // the linked list
            if (current == null) {
                return head;
            }

            // Insert the new node before the given node
            newNode.next = current;
            previous.next = newNode;

            // Return the head of the modified linked list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement — insert 6 before node with val 7
        ListNode h1 = fromList(5, 7, 3, 10);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h1, h1.next, 6)));      // [5, 6, 7, 3, 10]

        // Insert before head
        ListNode h2 = fromList(5, 7, 3, 10);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h2, h2, 0)));            // [0, 5, 7, 3, 10]

        // Insert before last node
        ListNode h3 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h3, h3.next.next, 9))); // [1, 2, 9, 3]

        // Single-node list — insert before the only node (becomes head)
        ListNode h4 = fromList(42);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h4, h4, 7)));           // [7, 42]

        // node is null — no change
        ListNode h5 = fromList(1, 2);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h5, null, 5)));         // [1, 2]

        // Two-node list — insert before second node
        ListNode h6 = fromList(1, 2);
        System.out.println(toList(new Solution().insertBeforeTheGivenNode(h6, h6.next, 99)));     // [1, 99, 2]
    }
}
```

</details>


***

# Understanding insertion at a given distance

Just as inserting before a given node is accomplished by piggybacking on the search algorithm, insertion at a given distance `X` can be achieved by piggybacking on the length-finding algorithm. Both search and length-finding rely on traversal. Let's examine all the cases we need to consider.

## 1\. The list is empty and X > 0

Attempting to insert a node at a position greater than 0 in an empty list is invalid. The only valid position in an empty list is position 0 (making the new node the head). When X > 0 but no nodes exist, we return the existing **head**.

```d3 widget=linked-list
{
  "title": "Insert at distance — empty list with X > 0: invalid position, return null",
  "direction": "single",
  "nodes": [{"id": "placeholder", "value": "—"}],
  "head": "placeholder",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "head = null, X > 0 — no valid position. Return null. (X = 0 with empty list is the create-new-head case.)"
    }
  ]
}
```

<p align="center"><strong>Case 1 — empty list with X > 0: no position exists to insert at, return unchanged.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. X = 0

Inserting at distance 0 means inserting at the **beginning** of the list — exactly what we covered in the very first insertion lesson.

```d3 widget=linked-list
{
  "title": "Insert at distance X = 0 — same as insert-at-beginning",
  "direction": "single",
  "nodes": [
    {"id": "new", "value": "6"},
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Before: head → 5 → 7 → 3, X = 0"
    },
    {
      "nodes": [
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"}
      ],
      "links": [["new","n1"],["n1","n2"],["n2","n3"]],
      "markers": [{"name": "head", "nodeId": "new"}],
      "msg": "newNode.next = head; head = newNode. O(1)."
    }
  ]
}
```

<p align="center"><strong>Case 2 — X = 0: insert-at-beginning; new node becomes the new head.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Set the new node's `next` pointer to hold the reference of the current head.
> -   **Step 3:** Return the new node, as this is the new head.

## 3\. X ≤ size of the list

Traverse the list while keeping a counter starting at 0. Increment the counter on each step. Stop when `counter == X - 1` — this lands us at the node just **before** where we want to insert. The problem then reduces to **inserting after that node**, which we already know.

```d3 widget=linked-list
{
  "title": "Insert at distance X = 2 — walk X−1 steps, then splice",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "new", "value": "6"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "10"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n1"}],
      "msg": "Start: current=head, counter=0. Target X=2 → stop at counter X−1 = 1."
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n2"}],
      "msg": "Walk: counter=1, current at node(7). Stop."
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6", "style": "new"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","n3"],["new","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "new"}],
      "msg": "newNode.next = current.next (point at node 3)"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "new", "value": "6"},
        {"id": "n3", "value": "3"},
        {"id": "n4", "value": "10"}
      ],
      "links": [["n1","n2"],["n2","new"],["new","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "current.next = newNode — splice complete. List is 5 → 7 → 6 → 3 → 10."
    }
  ]
}
```

<p align="center"><strong>Case 3 — valid position: traverse X−1 steps to land at the predecessor, then splice in the new node.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a new node with the given data.
> -   **Step 2:** Traverse X − 1 steps, tracking the `current` node.
> -   **Step 3:** Set the new node's `next` pointer to `current.next`.
> -   **Step 4:** Set `current.next` to the new node.
> -   **Step 5:** Return the original head node.

## 4\. X > size of the list

If `X` exceeds the list's length, the position doesn't exist. For example, inserting at position 5 in a 4-element list is invalid — we return the existing **head** unchanged.

```d3 widget=linked-list
{
  "title": "Insert at distance X = 5 in size-4 list — traversal falls off, return head unchanged",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "10"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n4"}],
      "msg": "Walking forward — counter reaches 3 at node(10). Target X−1 = 4."
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "current = current.next becomes null before counter hits 4. X = 5 exceeds size 4 → return head unchanged."
    }
  ]
}
```

<p align="center"><strong>Case 4 — X exceeds list size: traversal hits null before reaching X−1, return unchanged.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse X − 1 steps.
> -   **Step 2:** If `current` becomes `null` before reaching X − 1, return the original head.

## Implementation


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def insert_at_given_distance(
        self, head: Optional[ListNode], X: int, data: int
    ) -> Optional[ListNode]:

        # If the list is empty and X is greater than 0, insertion is not
        # possible, return None
        if head is None and X > 0:
            return None

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If X is 0, insert the new node at the beginning of the list
        if X == 0:

            # Set the next pointer of the new node to the current head,
            # making the new node the new head.
            new_node.next = head

            # Return the new_node as this is the new head
            return new_node

        # Pointer to traverse the list
        current = head

        # Counter to track the number of nodes traversed
        counter = 0

        # Traverse the list until reaching the desired distance or the
        # end of the list
        while current is not None and counter < X - 1:

            # Move to the next node
            current = current.next

            # Increment the counter
            counter += 1

        # If the list is shorter than X-1, it's not possible to insert
        # the new node, so return head.
        if current is None:
            return head

        # Set the next pointer of the new node to the current node
        new_node.next = current.next

        # Update the next pointer of the current node to point to the new
        # node
        current.next = new_node

        # Return the updated head of the list
        return head
```

```java run
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 * };
 */

class Solution {
    public ListNode insertAtGivenDistance(
        ListNode head,
        int X,
        int data
    ) {

        // If the list is empty and X is greater than 0, insertion is not
        // possible, return null
        if (head == null && X > 0) {
            return null;
        }

        // Create a new node with the given data
        ListNode newNode = new ListNode(data);

        // If X is 0, insert the new node at the beginning of the list
        if (X == 0) {

            // Set the next pointer of the new node to the current head,
            // making the new node the new head.
            newNode.next = head;

            // Return the newNode as this is the new head
            return newNode;
        }

        // Pointer to traverse the list
        ListNode current = head;

        // Counter to track the number of nodes traversed
        int counter = 0;

        // Traverse the list until reaching the desired distance or the
        // end of the list
        while (current != null && counter < X - 1) {

            // Move to the next node
            current = current.next;

            // Increment the counter
            counter++;
        }

        // If the list is shorter than X-1, it's not possible to insert
        // the new node, so return head.
        if (current == null) {
            return head;
        }

        // Set the next pointer of the new node to the current node
        newNode.next = current.next;

        // Update the next pointer of the current node to point to the
        // new node
        current.next = newNode;

        // Return the updated head of the list
        return head;
    }
}
```


## Complexity Analysis

The time complexity depends on the insertion position X relative to the list's length.

### Best case

X = 0 — insert at the beginning. No traversal needed. **O(1)**.

```d2
direction: right
new: |md
  val: 6

  (new)
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
n1: "val: 5"
n2: "val: 7"
n3: |md
  val: 3

  `next: null`
|
new -> n1: "newNode.next = head\n— O(1)"
n1 -> n2 -> n3
```

<p align="center"><strong>Best case — X = 0: insert at head, constant time.</strong></p>

### Worst case

X = length of the list — insert at the tail. Must traverse the entire list. **O(N)**.

```d2
direction: right
n1: "val: 5"
n2: "val: 7"
n3: "val: 3"
n4: |md
  val: 10

  `next: null`
|
new: |md
  val: 6

  (new)
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
n1 -> n2 -> n3 -> n4 -> new

cur: "current traverses\nn nodes — O(n)" {shape: oval}
cur -> n4: "walks to tail" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — X = list length: traverse to the tail, then append. O(N).</strong></p>

The function's space complexity is **O(1)** — only a few fixed variables regardless of list size.

> **Best Case** — X = 0:
>
> -   Space Complexity — **O(1)**
> -   Time Complexity — **O(1)**
>
> **Worst Case** — X = length of the list:
>
> -   Space Complexity — **O(1)**
> -   Time Complexity — **O(N)**

***

# Insert at given distance

## Problem Statement

Given the **head** of a singly linked list, a distance **X**, and a **data** value, write a function to insert a new node with the given data value at a distance X from the start of the linked list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], X = 1, data = 6
> -   **Output:** \[5, 6, 7, 3, 10\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional


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
    def insert_at_given_distance(
        self, head: Optional[ListNode], X: int, data: int
    ) -> Optional[ListNode]:

        # If the list is empty and x is greater than 0, insertion is not
        # possible, return None
        if head is None and X > 0:
            return None

        # Create a new node with the given data
        new_node: ListNode = ListNode(data)

        # If X is 0, insert the new node at the beginning of the list
        if X == 0:

            # Set the next pointer of the new node to the current head,
            # making the new node the new head.
            new_node.next = head

            # Return the newNode as this is the new head
            return new_node

        # Pointer to traverse the list
        current = head

        # Counter to track the number of nodes traversed
        counter = 0

        # Traverse the list until reaching the desired distance or the
        # end of the list
        while current is not None and counter < X - 1:

            # Move to the next node
            current = current.next

            # Increment the counter
            counter += 1

        # If the list is shorter than X-1, it's not possible to insert
        # the new node, so return head.
        if current is None:
            return head

        # Set the next pointer of the new node to the current node
        new_node.next = current.next

        # Update the next pointer of the current node to point to the new
        # node
        current.next = new_node

        # Return the updated head of the list
        return head


# Example from the problem statement
print(to_list(Solution().insert_at_given_distance(from_list([5, 7, 3, 10]), 1, 6)))  # [5, 6, 7, 3, 10]

# Insert at position 0 (head)
print(to_list(Solution().insert_at_given_distance(from_list([1, 2, 3]), 0, 99)))     # [99, 1, 2, 3]

# Insert at end
print(to_list(Solution().insert_at_given_distance(from_list([1, 2, 3]), 3, 4)))      # [1, 2, 3, 4]

# Empty list with X = 0
print(to_list(Solution().insert_at_given_distance(None, 0, 5)))                      # [5]

# Empty list with X > 0 — returns None
print(Solution().insert_at_given_distance(None, 2, 5))                               # None

# Single-node list, insert at position 0
print(to_list(Solution().insert_at_given_distance(from_list([7]), 0, 3)))            # [3, 7]

# Two-node list, insert after first node
print(to_list(Solution().insert_at_given_distance(from_list([1, 2]), 1, 9)))         # [1, 9, 2]
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
        public ListNode insertAtGivenDistance(
            ListNode head,
            int X,
            int data
        ) {

            // If the list is empty and X is greater than 0, insertion is not
            // possible, return null
            if (head == null && X > 0) {
                return null;
            }

            // Create a new node with the given data
            ListNode newNode = new ListNode(data);

            // If X is 0, insert the new node at the beginning of the list
            if (X == 0) {

                // Set the next pointer of the new node to the current head,
                // making the new node the new head.
                newNode.next = head;

                // Return the newNode as this is the new head
                return newNode;
            }

            // Pointer to traverse the list
            ListNode current = head;

            // Counter to track the number of nodes traversed
            int counter = 0;

            // Traverse the list until reaching the desired distance or the
            // end of the list
            while (current != null && counter < X - 1) {

                // Move to the next node
                current = current.next;

                // Increment the counter
                counter++;
            }

            // If the list is shorter than X-1, it's not possible to insert
            // the new node, so return head.
            if (current == null) {
                return head;
            }

            // Set the next pointer of the new node to the current node
            newNode.next = current.next;

            // Update the next pointer of the current node to point to the
            // new node
            current.next = newNode;

            // Return the updated head of the list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().insertAtGivenDistance(fromList(5, 7, 3, 10), 1, 6)));  // [5, 6, 7, 3, 10]

        // Insert at position 0 (head)
        System.out.println(toList(new Solution().insertAtGivenDistance(fromList(1, 2, 3), 0, 99)));     // [99, 1, 2, 3]

        // Insert at end
        System.out.println(toList(new Solution().insertAtGivenDistance(fromList(1, 2, 3), 3, 4)));      // [1, 2, 3, 4]

        // Empty list with X = 0
        System.out.println(toList(new Solution().insertAtGivenDistance(null, 0, 5)));                   // [5]

        // Empty list with X > 0 — returns null
        System.out.println(new Solution().insertAtGivenDistance(null, 2, 5));                           // null

        // Single-node list, insert at position 0
        System.out.println(toList(new Solution().insertAtGivenDistance(fromList(7), 0, 3)));            // [3, 7]

        // Two-node list, insert after first node
        System.out.println(toList(new Solution().insertAtGivenDistance(fromList(1, 2), 1, 9)));         // [1, 9, 2]
    }
}
```

</details>

***

# Working Example

Five insertion variants, one splice pattern. The table below walks the same data — `head → 5 → 7 → 3 → 10` — through each variant and shows where the cost goes.

| Variant | Walk cost | Splice cost | Total | Result |
|---|---|---|---|---|
| At beginning, `data = 6` | 0 steps (head is given) | `O(1)` | **`O(1)`** | `6 → 5 → 7 → 3 → 10` |
| At end, `data = 6` (no tail ref) | `n` steps (walk to tail) | `O(1)` | **`O(n)`** | `5 → 7 → 3 → 10 → 6` |
| After given node `node(7)`, `data = 6` | 0 steps (node is given) | `O(1)` | **`O(1)`** | `5 → 7 → 6 → 3 → 10` |
| Before given node `node(7)`, `data = 6` | `n` steps (find predecessor) | `O(1)` | **`O(n)`** | `5 → 6 → 7 → 3 → 10` |
| At distance `X = 2`, `data = 6` | 2 steps | `O(1)` | **`O(X)`** | `5 → 7 → 6 → 3 → 10` |

Every row ends in the same two-line splice: `new_node.next = successor; predecessor.next = new_node`. The cost is always in the walk, never in the splice.

So the core insight is: **whenever a linked-list problem hands you a pointer to "where", the insert is `O(1)` time. Whenever you have to find "where", the insert pays for the search.**

This is exactly why production linked-list libraries — `java.util.LinkedList`, C++ `std::list` — cache a **tail pointer** alongside the head. It collapses the "insert at end" variant from `O(n)` time back down to `O(1)` time. A singly linked list with a cached tail has **four** `O(1)` operations (head insert, head delete, tail insert, traversal start) and only loses to the doubly linked version on tail *deletion* — for which you need a backward pointer to find the new tail.

> **Transfer Challenge:** Your list holds 1 million nodes and you need to insert a node **right before the tail**. With only a `head` reference, what's the minimum number of pointer dereferences? If you also have a cached `tail` pointer — can you *still* do it in `O(1)` time, or does the single-direction nature of singly linked lists force an `O(n)` walk?
>
> <details><summary><strong>Answer</strong></summary>
>
> With only `head`: `O(n)` time — you must walk until `cur.next.next == null` to find the predecessor of the tail.
>
> With a cached `tail`: still **`O(n)`** time. The tail pointer gets you to the last node in `O(1)`, but a singly linked list has no backward pointer — you cannot find the node *before* the tail without walking from the head. This is one of the few cases where doubly linked lists strictly win: they cache a `prev` reference on every node, making "insert before tail" `O(1)` time.
>
> </details>

---

# Edge Cases and Pitfalls

The splice is short, which makes the edge cases sneaky — most insertion bugs land on the boundary between "list of zero" and "list of one", or on the order of two pointer writes. Keep this list open when you write any of the five variants.

- **Wrong pointer-write order.** `node.next = new_node` *before* `new_node.next = node.next` overwrites the only reference to the tail of the list. The next read sees `new_node` instead of the original successor, producing a self-loop and dropping everything past the splice point. Rule: **always wire the new node's `.next` first**, then redirect the predecessor.
- **Inserting into an empty list.** `head == null` is a separate code path for every variant. For *insert at head* and *insert at end* it means "the new node becomes the head"; for *insert after* / *insert before* it means "do nothing, the reference node cannot exist"; for *insert at distance* with `X > 0` it means "invalid position, return `null`". Skip the empty-list check and you'll dereference `null` on the first line.
- **Inserting into a single-node list.** The single node *is* the head and *is* the tail. *Insert at beginning* and *insert at end* both produce a two-node list, but via different code paths — verify both branches are tested.
- **`node` argument is `null` (insert-after, insert-before).** A `null` reference means there's nothing to insert next to. The function should return cleanly, not crash on `node.next`.
- **Given `node` is not in the list.** *Insert before* walks until `current == node` — if the node never appears, the walk falls off the end (`current == null`). Return the head unchanged; don't dereference the trailing `null`.
- **`X` is out of range (insert-at-distance).** `X > length(list)` and `X < 0` are both invalid positions. The reference implementation returns `head` unchanged for `X > length`; reject negatives explicitly if the caller can supply them.
- **Confusing `X = 0` with `X = 1`.** A position of `0` means *before* the head (the new node becomes the new head); `X = 1` means *between* node 0 and node 1. Off-by-one here produces results that look almost right and pass shallow tests.
- **Forgetting to return the new head when it changes.** *Insert at beginning* and *insert before head* both change which node is the head. If the function returns the old `head` reference, the caller's pointer still points at the second node — the new head is unreachable and silently leaks.
- **No tail pointer means `O(n)` end-insertion in a loop.** `for (val in xs) list.insert_at_end(val)` is `O(n²)` time when `insert_at_end` walks to the tail each call. Either cache the tail externally, build the list head-first and reverse, or use a deque/doubly-linked list when you need both ends cheap.

***

# Production Reality

Linked-list insertion looks academic until you notice how many "real systems" pick this structure precisely because insert/splice is `O(1)` time at a known reference point.

**[Linux kernel `list_head`]** — uses **a doubly linked intrusive list with `list_add` doing four pointer writes** — because every subsystem that holds an object reference (scheduler, VFS, network) can splice it in or out in `O(1)` time without a separate container allocation.

The kernel's circular doubly linked list is embedded directly into each struct via a `list_head` field. Any code that holds a pointer to the struct gets `O(1)` insertion and removal without touching the allocator — exactly what an interrupt-context path needs. Source: [include/linux/list.h](https://github.com/torvalds/linux/blob/master/include/linux/list.h).

**[Java's `LinkedList`]** — uses **a doubly linked list with cached `first` and `last` pointers** — because `addFirst`/`addLast` are both `O(1)` time, the price for `O(n)` random-access `get(i)`.

`java.util.LinkedList` keeps head and tail node references on the list itself, so prepending and appending are both two-pointer writes. Java leans on this for queue/deque workloads; for indexed access it costs `O(n)` time. Source: [LinkedList.java](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/LinkedList.java).

**[Redis adlist (`listAddNodeHead`)]** — uses **a doubly linked list with `len`, `head`, and `tail` fields** — because pub/sub queues and the slow-log are append-and-trim workloads where the per-operation cost dominates throughput.

Redis maintains a doubly linked list with cached endpoints. `LPUSH` and `RPUSH` are `O(1)` time; the trimming logic (`LTRIM`, `EXPIRE`) deletes from one end in `O(1)` time per node. Source: [adlist.c](https://github.com/redis/redis/blob/unstable/src/adlist.c).

**[Python's `collections.OrderedDict`]** — uses **a doubly linked list of dictionary entries** — because preserving insertion order across `move_to_end` operations requires `O(1)` splicing of any entry to either end.

The underlying hash table gives `O(1)` lookup; the linked list threaded through the entries gives `O(1)` reordering. `LRU` caches built on `OrderedDict.move_to_end(...)` rely entirely on this property. Source: [_collectionsmodule.c](https://github.com/python/cpython/blob/main/Modules/_collectionsmodule.c) <!-- VERIFY: OrderedDict implementation now lives in odictobject.c — confirm canonical source file -->.

**[Memory allocators — free lists]** — uses **a singly linked list of free blocks indexed by size class** — because freeing memory at the front of the list is two writes and allocating is one read, so `malloc`/`free` stay constant-time on the hot path.

`jemalloc`, `tcmalloc`, and most small-object allocators carve memory into size classes and keep each class's free blocks on a singly linked stack. Allocation pops the head; free pushes to the head. Both `O(1)` time without traversal. Source: [jemalloc internals](https://github.com/jemalloc/jemalloc/blob/dev/INSTALL.md).

**[Process scheduler ready-queues]** — uses **per-priority intrusive linked lists** — because the scheduler enqueues runnable tasks on every context switch and an `O(1)` insertion at the tail keeps that hot path off the allocator.

Linux's `CFS` red-black tree handles the general case, but real-time scheduling classes (`SCHED_FIFO`, `SCHED_RR`) use plain doubly linked lists per priority level — enqueue at the tail, dequeue at the head, no rebalancing. Source: [kernel/sched/rt.c](https://github.com/torvalds/linux/blob/master/kernel/sched/rt.c).

***

# Practice Ladder

Five problems, easiest first. Try each unaided; hit the hint only after ten minutes stuck; don't peek at solutions until you've made the splice *do something* in code.

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | [Reverse a List](./07-pattern-reversal/02-problems/01-reverse-a-list.md) | [Reversal](./07-pattern-reversal/01-pattern.md) | Easy | At each step, save `current.next`, then re-point `current.next` back to the previous node. Same three-pointer dance as insertion, repeated `n` times. |
| 2 | [Trim Nth Node](./09-pattern-sliding-window-traversal/02-problems/02-trim-nth-node.md) | [Sliding Window Traversal](./09-pattern-sliding-window-traversal/01-pattern.md) | Easy | Walk two pointers `n` apart; when the leader hits the tail, the follower points at the predecessor of the node to remove. Then it's a one-line splice. |
| 3 | [Middle Node Search](./10-pattern-fast-and-slow-pointers/02-problems/01-middle-node-search.md) | [Fast and Slow Pointers](./10-pattern-fast-and-slow-pointers/01-pattern.md) | Easy | Slow advances one step per loop; fast advances two. When fast hits the end, slow sits at the middle — useful when an insertion site is defined relative to length. |
| 4 | [Merge Sorted Lists](./12-pattern-merge/02-problems/02-merge-sorted-lists.md) | [Merge](./12-pattern-merge/01-pattern.md) | Easy | Use a dummy head and an `O(1)` "insert at tail" of a growing result list — the same `tail.next = node; tail = node` pattern from this lesson, applied in a loop. |
| 5 | [Relocate Node](./13-pattern-reorder/02-problems/01-relocate-node.md) | [Reorder](./13-pattern-reorder/01-pattern.md) | Medium | Detach the source node (delete + insert in two splices). The reusable trick: hold the predecessor of *both* the source and the destination before touching any `.next`. |

Once these feel automatic, you've internalised every move the reversal, sliding-window, fast/slow, and merge patterns will ask of you — and the splice itself disappears into muscle memory.

***

# Quiz

Test your grip before moving on. One answer per question; reveal only after you have committed to one.

**[Recall] Q: What is the time and space complexity of inserting at the beginning of a singly linked list?**
`O(1)` time and `O(1)` space — two pointer writes regardless of list length.

**[Recall] Q: For *insert at distance `X`* on a list of length `n`, what is the worst-case time complexity?**
`O(n)` time when `X = n` (insert at the very end) — the walk visits every node before the splice.

**[Reasoning] Q: Why must `new_node.next = node.next` happen *before* `node.next = new_node`?**
Because `node.next = new_node` overwrites the only reference to the original successor; reading `node.next` afterwards yields `new_node` itself, producing a self-loop and dropping the rest of the list.

**[Reasoning] Q: Why is *insert before a given node* `O(n)` time on a singly linked list even when the target node is already in hand?**
Singly linked lists have no backward pointer — to rewire the predecessor's `.next`, you must walk from the head until you find it.

**[Tradeoff] Q: When does caching a `tail` pointer alongside the head pay off, and when does it *fail* to help?**
Caching `tail` makes *insert at end* `O(1)` time, paying for one extra pointer of overhead per list. It still fails to help *insert before the tail* (`O(n)` time) or *delete the tail* (`O(n)` time) — both need the tail's predecessor, which only a doubly linked list keeps cheap.

***

# Further Reading

Curated paths in, not a syllabus. Read in order of the annotation; come back for the rest when you need depth.

- **[CLRS — Chapter 10: Elementary Data Structures](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)**
  ★ Essential — the canonical reference for linked-list insertion, deletion, and the sentinel-node trick that simplifies every edge case.
- **[Sedgewick & Wayne — Algorithms, 4th ed., §1.3 Bags, Queues and Stacks](https://algs4.cs.princeton.edu/13stacks/)**
  ★ Essential — implements linked lists as the storage backing for stack and queue, with diagrams that make the splice visual.
- **[The Linux Kernel Linked Lists API](https://www.kernel.org/doc/html/latest/core-api/kernel-api.html#list-management-functions)**
  ◆ Advanced — the intrusive-list pattern (`container_of` macro, embedded `list_head`) — how production C systems get zero-allocation `O(1)` insert.
- **[Java `LinkedList.linkFirst` / `linkLast` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/LinkedList.java)**
  → Reference — the exact head/tail pointer-caching idiom that turns `O(n)` end-insertion into `O(1)`.
- **[Open Data Structures — §3.1 SLList: A Singly Linked List](https://opendatastructures.org/ods-python/3_1_SLList_Singly_Linked_Lis.html)**
  → Reference — a clean academic walk through every insertion variant, with worked examples in Python and Java.

***

# Cross-Links

**Prerequisites**

- [Introduction to Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-introduction-to-singly-linked-lists) — node structure, `head` reference, and why pointers replace contiguous memory.
- [Traversal in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-traversal-in-singly-linked-lists) — the walk that every non-`O(1)` insertion variant piggybacks on.
- [Introduction to Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-introduction) — the cost model that insertion exposes the gap from (`O(n)` shift) vs (`O(1)` splice).

**What comes next**

- [Deletion in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-deletion-in-singly-linked-lists) — the mirror operation; same predecessor-search problem, with a few extra cases when the head moves.
- [Detecting Cycle in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-detecting-cycle-in-singly-linked-lists) — how Floyd's tortoise-and-hare uses pure traversal (no insertion) to expose a structural property.
- [Pattern: Reversal](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-pattern-reversal-pattern) — the first major linked-list pattern; reversal is "splice at the head" in a loop.
- [Pattern: Sliding Window Traversal](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-pattern-sliding-window-traversal-pattern) — the two-pointer walk that finds insertion sites defined by *offset from the tail*.

***

## Final Takeaway

1. **Core mechanic:** every insertion in a singly linked list is "allocate a node, wire its `.next` to the successor, redirect the predecessor's `.next` to the new node" — two pointer writes, always in that order.
2. **Dominant tradeoff:** insertion at a known reference point is `O(1)` time regardless of list size; insertion at an unknown position pays `O(n)` time to *find* the predecessor — there is no `O(1)` random access to amortise this away.
3. **One thing to remember:** the cost lives in the walk, not in the splice — so whenever you can preserve a reference to "where" (head, tail, predecessor), you keep insertion `O(1)`.
