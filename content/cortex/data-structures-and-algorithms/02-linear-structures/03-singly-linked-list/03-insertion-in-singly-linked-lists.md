# 3. Insertion in Singly Linked Lists

## The Hook

In an array, "insert" is a lie. You don't actually insert — you **shift**. Every element past the insertion point slides one slot to the right, a cascade of memory writes that grows with the array. That's why `list.insert(0, x)` in Python on a million-element list is *slow* and `list.append(x)` is fast: one is an O(n) shift, the other is an O(1) write.

Linked lists flip the script. Insert at the head? **Two pointer assignments. O(1).** Doesn't matter if the list has 10 nodes or 10 million. What you give up is random access — inserting in the *middle* still requires walking there first. The entire zoo of "insert at X" problems in this lesson comes down to one question: **where do we already have a pointer?** If it's the head, insertion is O(1). If it's somewhere in the middle, we pay O(n) to walk, then O(1) to splice.

Five variations follow — head, tail, after-given, before-given, at-distance. They all reduce to the same three-line splice: **create a node, link it into its successor, link its predecessor to it**. Master the splice once, and every insertion problem you'll ever face is solved.

---

## Table of contents

1. [Understanding insertion at beginning](#understanding-insertion-at-beginning)
2. [Insert at beginning](#insert-at-beginning)
3. [Understanding insertion at end](#understanding-insertion-at-end)
4. [Insert at end](#insert-at-end)
5. [Understanding insertion after the given node](#understanding-insertion-after-the-given-node)
6. [Insert after the given node](#insert-after-the-given-node)
7. [Understanding insertion before the given node](#understanding-insertion-before-the-given-node)
8. [Insert before the given node](#insert-before-the-given-node)
9. [Understanding insertion at a given distance](#understanding-insertion-at-a-given-distance)
10. [Insert at given distance](#insert-at-given-distance)

***

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



```python run
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



```python run
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



```python run
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



```python run
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



```python run
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
<details>
<summary><h2>Final Takeaway</h2></summary>


Five insertion variants, one splice pattern. Look at what they share:

| Variant | Walk cost | Splice cost | Total |
|---|---|---|---|
| At beginning | 0 steps (head is given) | O(1) | **O(1)** |
| At end (no tail ref) | n steps (walk to tail) | O(1) | **O(n)** |
| After given node | 0 steps (node is given) | O(1) | **O(1)** |
| Before given node | n steps (find predecessor) | O(1) | **O(n)** |
| At distance X | X steps | O(1) | **O(X)** |

Every row uses the exact same three-line splice at the end — `newNode.next = successor; predecessor.next = newNode`. The cost is always in the walk, never in the splice. **Whenever a linked-list problem offers you a pointer to "where", the insert is O(1). Whenever you have to find "where", the insert pays for the search.**

This is why production linked-list libraries (`java.util.LinkedList`, C++ `std::list`) cache a **tail pointer** alongside the head — it collapses the "insert at end" variant from O(n) back down to O(1). A singly linked list with cached tail has **four** O(1) operations (head insert, head delete, tail insert) and only loses to the doubly-linked version on tail *deletion*.

> **Transfer Challenge:** Your list holds 1 million nodes and you need to insert a node **right before the tail**. With only a `head` reference, what's the minimum number of pointer dereferences? If you also have a cached `tail` pointer — can you *still* do it in O(1), or does the single-direction nature of singly linked lists force an O(n) walk?
>
> <details><summary><strong>Answer</strong></summary>
>
> With only `head`: O(n) — you must walk until `cur.next.next == null` to find the predecessor of the tail.
>
> With a cached `tail`: still **O(n)**. The tail pointer gets you to the last node in O(1), but a singly linked list has no backward pointer — you cannot find the node *before* the tail without walking from the head. This is one of the few cases where doubly linked lists strictly win: they cache `prev` on every node, making "insert before tail" O(1).
>
> </details>

</details>