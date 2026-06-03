---
title: "Traversal In Singly Linked Lists"
summary: "Walk a singly linked list by following `node.next` pointers from head until null. The O(n) loop that underpins every other linked-list operation — search, length, insert, delete, reverse."
---

# 2. Traversal in Singly Linked Lists

## The Hook

In an array, reaching `arr[999]` is a single CPU instruction — a multiplication, an addition, a memory read. **Constant time.** In a linked list, reaching the 1000th node means following 999 pointers, one at a time, hopping through random memory addresses. So why would anyone *use* a linked list? Because O(1) random access isn't free — arrays pay for it with painful insertions and a fixed size. Linked lists pay for their flexibility with O(n) traversal. Every data structure trades one superpower for another.

In this lesson you'll master the traversal loop that underpins **every other linked-list operation** you'll ever write. Insertion walks until it finds the right spot. Deletion walks until it finds the victim. Reversal walks while flipping pointers. Cycle detection walks at two speeds. If you can't traverse fluently, none of the rest works — so let's nail this one cold.

---

## Table of contents

1. [Understanding traversal](#understanding-traversal)
2. [Node expedition](#node-expedition)
3. [Node search](#node-search)
4. [Length of the list](#length-of-the-list)
5. [Understanding the problem](#understanding-the-problem)
6. [Supported operations](#supported-operations)
7. [Internal mechanics](#internal-mechanics)
8. [Working example](#working-example)
9. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
10. [Production reality](#production-reality)
11. [Quiz](#quiz)
12. [Practice ladder](#practice-ladder)
13. [Further reading](#further-reading)
14. [Cross-links](#cross-links)
15. [Final takeaway](#final-takeaway)

***

# Understanding traversal

Arrays and singly linked lists are both linear data structures. To better understand the traversal algorithm for a singly linked list, first, let us revisit the traversal algorithm in arrays.

## Traversal in arrays

In arrays, we have indexes to access the individual items of the array, e.g. `0`, `1`, `2`, etc, and for traversal, we just loop on the size of the array and traverse it with the loop control variable as our array index.

```d2
direction: right

arr: "Array  —  contiguous, index-addressable" {
  grid-columns: 4
  grid-gap: 0
  a0: |md
    `[0]`

    **5**
  |
  a1: |md
    `[1]`

    **7**
  |
  a2: |md
    `[2]`

    **3**
  |
  a3: |md
    `[3]`

    **10**
  |
}

idx: "i ∈ {0, 1, 2, 3}" {
  shape: oval
  style.fill: "#dbeafe"
  style.stroke: "#1d4ed8"
  style.stroke-width: 2
  style.bold: true
}

idx -> arr.a0: "arr[i] — O(1) direct read" {style.stroke-width: 2; style.bold: true}
```

<p align="center"><strong>Array traversal uses an integer index <code>i</code> that increments from <code>0</code> to <code>n-1</code> — direct O(1) access at each step.</strong></p>

```d3 widget=array-1d
{
  "steps": [
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "i",
          "target": "0",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i = 0 — enter the loop, condition i < 4 holds",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "i",
          "target": "0",
          "color": "#3b82f6"
        }
      ],
      "highlight": [
        "0"
      ],
      "changed": [],
      "removed": [],
      "annotation": "arr[0] = 5 — O(1) direct access via the index",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "i",
          "target": "1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [
        "1"
      ],
      "changed": [],
      "removed": [],
      "annotation": "i = 1 → arr[1] = 7",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "i",
          "target": "2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [
        "2"
      ],
      "changed": [],
      "removed": [],
      "annotation": "i = 2 → arr[2] = 3",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [
        {
          "name": "i",
          "target": "3",
          "color": "#3b82f6"
        }
      ],
      "highlight": [
        "3"
      ],
      "changed": [],
      "removed": [],
      "annotation": "i = 3 → arr[3] = 10",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "0",
          "label": "5",
          "kind": "cell",
          "meta": [],
          "slot": 0,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "1",
          "label": "7",
          "kind": "cell",
          "meta": [],
          "slot": 1,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "2",
          "label": "3",
          "kind": "cell",
          "meta": [],
          "slot": 2,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "3",
          "label": "10",
          "kind": "cell",
          "meta": [],
          "slot": 3,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [],
      "cursor": [],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "i = 4 — condition i < 4 fails, loop ends. O(n) total, O(1) per access.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Array traversal — i increments 0 → n-1, arr[i] is O(1) each step"
}
```

<p align="center"><strong>Step through the array traversal — six frames mirror the source: enter, four O(1) reads, exit. The index <code>i</code> walks the cells; the highlighted band shows what <code>arr[i]</code> resolves to at each step.</strong></p>

```python run viz=array viz-root=arr
arr = [5, 7, 3, 10]

# For loop — index-based traversal
for i in range(len(arr)):
    print(arr[i], end=" ")  # Direct access via index

print()

# While loop — equivalent form
i = 0
while i < len(arr):
    print(arr[i], end=" ")
    i += 1
```

```java run viz=array viz-root=arr
public class Main {
    public static void main(String[] args) {
        int[] arr = {5, 7, 3, 10};

        // For loop — index-based traversal
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");  // Direct access via index
        }
        System.out.println();

        // While loop — equivalent form
        int i = 0;
        while (i < arr.length) {
            System.out.print(arr[i] + " ");
            i++;
        }
    }
}
```


## Traversal in singly linked lists

Data in a singly linked list is not stored in continuous memory, so we do not have indexes for random access like arrays. All the nodes are present at different memory locations. So, how do we traverse a linked list from start to end?

Instead of an integer loop control variable representing an item's index in an array, we use a variable referencing a node in the linked list as the loop control variable. Every time we want to move forward, we assign the node's reference in the linked list to this variable. We can get the node's reference by looking at the value stored in the pointer of the current node.

Since linked lists are dynamic, we don't know their length in advance, and therefore, we have to keep traversing until we reach a node with a **`null`** value stored in its pointer. That is how we know we have reached the end of a linked list.

```d3 widget=list-single
{
  "steps": [
    {
      "nodes": [
        {
          "id": "n1",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        },
        {
          "from": "n2",
          "to": "n3",
          "label": "next"
        },
        {
          "from": "n3",
          "to": "n4",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        },
        {
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current ← head — start at node 5",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n1",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        },
        {
          "from": "n2",
          "to": "n3",
          "label": "next"
        },
        {
          "from": "n3",
          "to": "n4",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current = current.next — hop to node 7",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n1",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        },
        {
          "from": "n2",
          "to": "n3",
          "label": "next"
        },
        {
          "from": "n3",
          "to": "n4",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "current",
          "target": "n3",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current = current.next — hop to node 3",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n1",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        },
        {
          "from": "n2",
          "to": "n3",
          "label": "next"
        },
        {
          "from": "n3",
          "to": "n4",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "current",
          "target": "n4",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current = current.next — hop to node 10 (the tail)",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n1",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        },
        {
          "from": "n2",
          "to": "n3",
          "label": "next"
        },
        {
          "from": "n3",
          "to": "n4",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current.next is null — stop. Traversed all 4 nodes in O(n).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Linked-list traversal — current hops node-by-node until it reaches null"
}
```

<p align="center"><strong>Linked list traversal — a <code>current</code> pointer starts at <code>head</code> and hops forward via <code>current = current.next</code> until it reaches <code>null</code>.</strong></p>

Given below is the code implementation of singly linked list traversal.


```python run viz=linked-list viz-root=head
class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val; self.next = next

def traverse(head):
    current = head              # Start at the head node
    while current is not None:  # Stop when current falls off the end
        print(current.val, end=" ")
        current = current.next  # Advance to the next node

# Build list: 5 → 7 → 3 → 10
n4 = ListNode(10)
n3 = ListNode(3,  n4)
n2 = ListNode(7,  n3)
n1 = ListNode(5,  n2)
traverse(n1)  # 5 7 3 10
```

```java run viz=linked-list viz-root=head
public class Main {
    static class ListNode { int val; ListNode next; ListNode(int v){val=v;} }

    static class Solution {
        public void traverse(ListNode head) {
            for (ListNode current = head; current != null; current = current.next) {
                System.out.print(current.val + " ");  // Advance: current = current.next
            }
        }
    }

    public static void main(String[] args) {
        ListNode n1=new ListNode(5), n2=new ListNode(7),
                 n3=new ListNode(3), n4=new ListNode(10);
        n1.next=n2; n2.next=n3; n3.next=n4;
        new Solution().traverse(n1);  // 5 7 3 10
    }
}
```


Later in the course, we will learn more about how to piggyback on this generic traversal logic to do various things as we traverse the singly linked list.

***

# Node expedition

## Problem Statement

Given the **head** of a singly linked list, write a function to print a comma (`,`) separated list of all the values from the start to the end.

### Example

> -   **Input:** head = \[5, 7, 3, 10\]
> -   **Output:** 5, 7, 3, 10

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


class Solution:
    def node_expedition(self, head: Optional[ListNode]) -> None:

        # Start from the head of the linked list
        current: Optional[ListNode] = head

        # Iterate until the current node is not null
        while current is not None:

            # Print the value of the current node
            print(current.val, end="")

            # If there is a next node, print a comma after the value
            if current.next is not None:
                print(", ", end="")

            # Move to the next node
            current = current.next


# Examples from the problem statement
Solution().node_expedition(from_list([5, 7, 3, 10])); print()  # 5, 7, 3, 10

# Edge cases
Solution().node_expedition(from_list([])); print()             # (empty)
Solution().node_expedition(from_list([42])); print()           # 42
Solution().node_expedition(from_list([1, 2])); print()         # 1, 2
Solution().node_expedition(from_list([1, 1, 1])); print()      # 1, 1, 1
Solution().node_expedition(from_list([10, 20, 30, 40, 50])); print()  # 10, 20, 30, 40, 50
```

```java run viz=linked-list viz-root=head
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

    static class Solution {
        public void nodeExpedition(ListNode head) {

            // Start from the head of the linked list
            ListNode current = head;

            // Iterate until the current node is not null
            while (current != null) {

                // Print the value of the current node
                System.out.print(current.val);

                // If there is a next node, print a comma after the value
                if (current.next != null) {
                    System.out.print(", ");
                }

                // Move to the next node
                current = current.next;
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        new Solution().nodeExpedition(fromList(5, 7, 3, 10)); System.out.println();  // 5, 7, 3, 10

        // Edge cases
        new Solution().nodeExpedition(fromList()); System.out.println();             // (empty)
        new Solution().nodeExpedition(fromList(42)); System.out.println();           // 42
        new Solution().nodeExpedition(fromList(1, 2)); System.out.println();         // 1, 2
        new Solution().nodeExpedition(fromList(1, 1, 1)); System.out.println();      // 1, 1, 1
        new Solution().nodeExpedition(fromList(10, 20, 30, 40, 50)); System.out.println(); // 10, 20, 30, 40, 50
    }
}
```


### Complexity & Key Idea

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | Every node visited exactly once |
| **Space** | O(1) | One pointer variable; output stream is not counted |

The **only** subtlety is the comma placement — emit a comma *before* every value except the first, or *after* every value except the last. The `current.next != null` check is the "am I the last?" test you already know from the previous lesson's boundary-node problem. Look how quickly the primitives compound.

</details>

***

# Node Search

## Problem Statement

Given the **head** of a singly linked list and a **data** value, write a function to return the first node containing the given data. If no such node is found, return `null`.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10\], data = 3
> -   **Output:** 3

### Example 2

> -   **Input:** head = \[5, 7, 6, 10\], data = 3
> -   **Output:** null

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


class Solution:
    def node_search(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Start from the head of the linked list
        current: Optional[ListNode] = head

        while current is not None:

            # Found the value, return the current node
            if current.val == data:
                return current

            # Move to the next node
            current = current.next

        # Value not found in the linked list
        return None


# Examples from the problem statement
result = Solution().node_search(from_list([5, 7, 3, 10]), 3)
print(result.val if result else None)                        # 3

result = Solution().node_search(from_list([5, 7, 6, 10]), 3)
print(result.val if result else None)                        # None

# Edge cases
result = Solution().node_search(None, 5)
print(result.val if result else None)                        # None

result = Solution().node_search(from_list([7]), 7)
print(result.val if result else None)                        # 7

result = Solution().node_search(from_list([7]), 1)
print(result.val if result else None)                        # None

result = Solution().node_search(from_list([1, 2, 3, 4]), 1)
print(result.val if result else None)                        # 1 (head match)

result = Solution().node_search(from_list([1, 2, 3, 4]), 4)
print(result.val if result else None)                        # 4 (tail match)
```

```java run viz=linked-list viz-root=head
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

    static class Solution {
        public ListNode nodeSearch(ListNode head, int data) {

            // Start from the head of the linked list
            ListNode current = head;

            while (current != null) {

                // Found the value, return the current node
                if (current.val == data) {
                    return current;
                }

                // Move to the next node
                current = current.next;
            }

            // Value not found in the linked list
            return null;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        ListNode r1 = new Solution().nodeSearch(fromList(5, 7, 3, 10), 3);
        System.out.println(r1 != null ? r1.val : null);      // 3

        ListNode r2 = new Solution().nodeSearch(fromList(5, 7, 6, 10), 3);
        System.out.println(r2 != null ? r2.val : null);      // null

        // Edge cases
        ListNode r3 = new Solution().nodeSearch(null, 5);
        System.out.println(r3 != null ? r3.val : null);      // null

        ListNode r4 = new Solution().nodeSearch(fromList(7), 7);
        System.out.println(r4 != null ? r4.val : null);      // 7

        ListNode r5 = new Solution().nodeSearch(fromList(7), 1);
        System.out.println(r5 != null ? r5.val : null);      // null

        ListNode r6 = new Solution().nodeSearch(fromList(1, 2, 3, 4), 1);
        System.out.println(r6 != null ? r6.val : null);      // 1 (head match)

        ListNode r7 = new Solution().nodeSearch(fromList(1, 2, 3, 4), 4);
        System.out.println(r7 != null ? r7.val : null);      // 4 (tail match)
    }
}
```


### Complexity & Key Idea

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) worst case, O(1) best case | Worst case: target absent or at tail. Best case: target is the head |
| **Space** | O(1) | Single pointer variable |

Notice the **early return**: the instant we find a match, we exit — we don't keep walking "just to be sure". This is linear search on a linked list, identical in shape to linear search on an array. The only difference is how we advance the cursor (`cur = cur.next` vs `i++`).

> *Before the next section — predict: if we need the list's length, does counting require any extra data? Or can we piggyback on the traversal we already wrote?*

</details>

***

# Length of the List

## Problem Statement

Given the **head** of a singly linked list, write a function that returns the length of the list.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10\]
> -   **Output:** 4

### Example 2

> -   **Input:** head = \[\]
> -   **Output:** 0

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


class Solution:
    def length_of_the_list(self, head: Optional[ListNode]) -> int:

        # Initialize count to 0
        count: int = 0

        # Set current to hold the head of the list
        current: Optional[ListNode] = head

        while current is not None:

            # Increment count by 1
            count += 1

            # Set current to hold the next node in the list
            current = current.next

        # Return count as the length of the linked list
        return count


# Examples from the problem statement
print(Solution().length_of_the_list(from_list([5, 7, 3, 10])))  # 4
print(Solution().length_of_the_list(from_list([])))              # 0

# Edge cases
print(Solution().length_of_the_list(from_list([42])))            # 1
print(Solution().length_of_the_list(from_list([1, 2])))          # 2
print(Solution().length_of_the_list(from_list([1, 1, 1, 1, 1]))) # 5
```

```java run viz=linked-list viz-root=head
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

    static class Solution {
        public int lengthOfTheList(ListNode head) {

            // Initialize count to 0
            int count = 0;

            // Set current to hold the head of the list
            ListNode current = head;

            while (current != null) {

                // Increment count by 1
                count++;

                // Set current to hold the next node in the list
                current = current.next;
            }

            // Return count as the length of the linked list
            return count;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().lengthOfTheList(fromList(5, 7, 3, 10)));  // 4
        System.out.println(new Solution().lengthOfTheList(fromList()));              // 0

        // Edge cases
        System.out.println(new Solution().lengthOfTheList(fromList(42)));            // 1
        System.out.println(new Solution().lengthOfTheList(fromList(1, 2)));          // 2
        System.out.println(new Solution().lengthOfTheList(fromList(1, 1, 1, 1, 1))); // 5
    }
}
```


### Complexity & Key Idea

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(n) | We must visit every node to count them — no shortcut |
| **Space** | O(1) | A single integer counter |

This is the painful part of linked lists: **there is no `.length` you can read in O(1)**. Every length query walks the entire list. That's why production linked-list implementations often cache a `size` field on the list object itself and update it on every insert/delete — trading a tiny bit of bookkeeping for O(1) size queries.

</details>
***

# Understanding the Problem

A singly linked list scatters its nodes across memory and chains them with `.next` pointers. That layout buys cheap insertion and deletion — but it also takes away the one thing arrays gave you for free: a way to address any node by index. There is no `list[7]` because there is no contiguous block to do address arithmetic over.

So the question every linked-list algorithm has to answer is: how do you *reach* a node you haven't been handed a reference to? You can't jump. You can't binary-search. You can only step.

**Traversal** is that one step, repeated:

- start at a known reference — almost always `head`
- read or compare the current node
- advance via `current = current.next`
- stop when `current` is `null`

To make this concrete: if a function needs the third node in `5 → 7 → 3 → 10`, it cannot ask for `list[2]`. It receives `head` (pointing at `5`), follows `head.next` to `7`, then `7.next` to `3`. Three node visits to reach index two. The same pattern, no matter what the operation looks like on the surface.

So the key idea is: traversal is the *only* way to reach any node beyond the head — every other singly-linked-list operation (search, insert, delete, reverse, merge) is a traversal with a small twist in the loop body.

---

## Key Takeaway

Without random access, every operation on a singly linked list begins by walking the chain. Master the walk first; everything else is bookkeeping inside the loop.

***

# Supported Operations

Traversal is not an end in itself — it is the substrate for the operations a singly linked list actually exposes. The table below names them and pins down their cost so you can see, at a glance, which operations the structure gets right and which ones make you pay.

| Operation | Time | Space | Why |
|---|---|---|---|
| **Access by index** `list[i]` | O(n) | O(1) | Must walk `i` `.next` hops — no address arithmetic |
| **Search by value** | O(n) | O(1) | Walk and compare; no ordering or hashing assumed |
| **Length** | O(n) | O(1) | Walk and count; no `.length` to read in O(1) unless cached |
| **Insert at head** | O(1) | O(1) | Re-point `new_node.next = head; head = new_node` |
| **Insert at tail** | O(n) | O(1) | Walk to the last node first, *unless* a `tail` pointer is cached |
| **Insert after a known node** | O(1) | O(1) | Re-point two `.next` references; no shifting |
| **Delete head** | O(1) | O(1) | `head = head.next` |
| **Delete by value** | O(n) | O(1) | Walk to find the predecessor, then re-point one `.next` |
| **Traversal (all nodes)** | O(n) | O(1) | The canonical loop — every other op is built on it |

Two patterns explain the whole table.

- **Anything that needs a reference to an arbitrary node is O(n)** — access, search, length, insert at tail without a cached pointer, delete by value.
- **Anything that already holds the right reference is O(1)** — insert at head, insert after a known node, delete head, delete after a known node.

To make this concrete: a stack on a singly linked list pushes and pops at the head — both O(1). A queue needs a head *and* a tail pointer to make both ends O(1), otherwise enqueue degrades to O(n).

So the core insight is: a singly linked list is a "what you already hold" structure — you pay O(n) the moment you need to *find* a node, but everything you can do *at* a node is O(1).

---

## Key Takeaway

The cost of any linked-list operation is the cost of getting to the right node, plus a constant for the pointer surgery itself. If you can avoid the walk by caching a pointer, you turn an O(n) operation into O(1).

***

# Internal Mechanics

Every traversal collapses into the same three-line loop — and the loop only works because of the four invariants the singly linked list maintains across all of its nodes.

The structural facts:

- **`head`** is the single externally-held reference; lose it and the entire list is unreachable
- every node holds a **value** field and a **`.next`** field
- the last node — the **tail** — has `.next == null`; the `null` is the only out-of-band signal in the whole structure
- a node's memory address has no relationship to its position in the chain — neighbours in the list may be far apart in memory

Given those, the traversal algorithm only needs a single cursor — the visit step is whatever the calling algorithm needs to do at each node (print, compare, count, rewire):

```
current = head
while current is not null:
    <do something with current.val or current.next>
    current = current.next
```

To make this concrete: on `5 → 7 → 3 → 10`, the cursor takes the values `head`, `head.next`, `head.next.next`, `head.next.next.next`, then `null`. The loop runs four times — once per node — and stops on the fifth check. Total: O(n) time, O(1) extra space.

So the core insight is: the loop is correct because of the tail's `null` sentinel. Drop that invariant — corrupt a `.next`, or build a cycle — and the loop either crashes on a null dereference or runs forever.

### Why `null` is the only stop condition

The cursor cannot know "how many nodes are left" because the structure does not store its own length. It can only ask the current node: *"is there a next one?"* The `null` in the tail's `.next` is the structure's way of saying *no*. That single bit of information is what makes the unbounded `while` loop terminate.

### Recursive vs iterative form

The same walk can be written recursively — `traverse(node.next)` after visiting `node`. It is shorter on paper, but it costs O(n) stack frames; on a list of a million nodes, a recursive walk will overflow the call stack in most runtimes. <!-- VERIFY: Python's default recursion limit is 1000; CPython does not have tail-call optimisation. --> The iterative form keeps space at O(1), so production code prefers it unless the recursion expresses something the iteration cannot.

So the tradeoff is: recursion mirrors the structure beautifully and runs in O(n) time — but it pays for that beauty with O(n) stack space, which large inputs will not tolerate.

---

## Key Takeaway

The whole linked-list world rests on three primitives: `head`, `.next`, and the tail's `null`. Internalise them and the loop writes itself.

***

# Working Example

To make the loop concrete, walk it on a four-node list — `5 → 7 → 3 → 10` — and trace every variable on every iteration.

Initial state, before the loop:

| Step | `current` | `current.val` | `current.next` | Action |
|---|---|---|---|---|
| 0 — init | node(5) | 5 | node(7) | `current ← head` — enter the loop, condition holds |
| 1 | node(5) | 5 | node(7) | visit 5; advance: `current ← current.next` |
| 2 | node(7) | 7 | node(3) | visit 7; advance |
| 3 | node(3) | 3 | node(10) | visit 3; advance |
| 4 | node(10) | 10 | `null` | visit 10; advance: `current ← null` |
| 5 — exit | `null` | — | — | condition fails; loop ends |

Four visits, one extra check that fails, then exit. Five iterations of the `while` test for four nodes — that's the +1 the loop pays to *detect* the end.

To make this concrete:

- the cursor `current` is the only mutable variable — no counter, no index, no extra storage
- the visit happens *before* the advance; reversing the order would skip the head
- the loop exits because `node(10).next` is `null`, not because the cursor knows the list has four nodes

So the key idea is: every linked-list algorithm you will write reuses this trace shape — different `visit()`, same `advance()`, same `null` exit.

---

## Key Takeaway

One pointer, one `.next` advance, one `null` exit — and four real nodes get touched in O(n) time with O(1) space.

***

# Edge Cases and Pitfalls

The traversal loop is five lines long and ships with a long list of small bugs. The list below catches the ones every reviewer eventually develops a reflex for.

- **Empty list — `head` is `null`.** The loop's entry condition `current is not null` already handles this; the body never executes. The bug is forgetting to write the check and dereferencing `head.val` directly — `NullPointerException` in Java, `AttributeError` in Python.
- **Single-node list.** The loop runs exactly once; `current.next` is `null` on the first iteration. Algorithms that compare adjacent pairs (`current.next.val`) must guard the `null` before reading further.
- **Reading `current.next.val` without a null check.** If `current` is the tail, `current.next` is `null` and the dereference crashes. Inside the loop body, every step beyond `current` itself needs its own null check.
- **Modifying `.next` mid-walk.** If the visit function rewires `current.next` (insert, delete, reverse), the loop loses its way unless you save the next reference *before* the modification: `next_ref = current.next; … rewire … ; current = next_ref`.
- **Forgetting to advance.** Omitting `current = current.next` turns the loop into an infinite spin on the head node — the most common beginner bug, easy to spot only because the program hangs.
- **Cycles.** If some node's `.next` points back into the chain (a programmer error, or a deliberate cycle in problems like cycle detection), the loop never terminates because no node satisfies `.next == null`. Fast-and-slow pointers detect this in O(n) time, O(1) space — covered in chapter 5.
- **Lost head reference.** A function that mutates `head` (e.g. `head = head.next` to delete the first node) must `return` the new head; the caller's local variable still points at the original first node otherwise. This is the linked-list equivalent of pass-by-value vs pass-by-reference confusion.
- **Off-by-one when counting hops.** "Walk to the kth node" means k advances, not k node visits. A list of length 5 has indices `0..4`; reaching index 3 from head requires three `.next` hops, not four.
- **Stale cached length.** If you cache a `size` field on the list object for O(1) length queries, every insert and delete must update it — miss one and `length()` silently lies. <!-- VERIFY: most production linked-list implementations (Java's LinkedList) do cache a `size` field. -->

***

# Production Reality

The "follow `.next` until null" walk shows up far beyond toy examples. The five places below put it on a load-bearing path.

**[Linux kernel — `struct list_head`]** — uses **an intrusively-linked doubly linked list walked node-by-node** — because the kernel needs zero-allocation list discipline across thousands of subsystems, and the cost is dominated by the per-step pointer chase, not by indexing.

The `list_for_each_entry` macro is a direct expansion of the traversal loop discussed in this lesson — initialise a cursor at the list head, advance via `cursor->next`, stop at the sentinel. Every process descriptor, every open file, every loadable module sits on at least one of these lists. Source: [include/linux/list.h](https://github.com/torvalds/linux/blob/master/include/linux/list.h).

**[Java's `HashMap` collision chain]** — uses **a singly linked list (then a tree above 8 entries) for the entries that hash to the same bucket** — because the *expected* chain length is below 1, so an O(n) walk over the chain is effectively O(1) on the hot path.

When a bucket has two or more entries, `get` and `put` walk the chain comparing keys until match or end. The walk is exactly the traversal in this lesson. Hot-path performance assumes the chain stays short; the tree threshold (`TREEIFY_THRESHOLD = 8`) exists to bound the worst case when a bad hash function packs a bucket. Source: [HashMap.java](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/HashMap.java).

**[Redis adlist]** — uses **a generic doubly linked list with head, tail, and length cached on the list object** — because most operations (`LRANGE`, `LPUSH`, list-iterator commands) only ever walk forward or backward from an end, and the cached length turns `LLEN` into O(1).

The traversal loop is identical; the cached `len` is the optimisation called out in the previous section's table. Without it, `LLEN` would cost O(n) every call. Source: [adlist.c](https://github.com/redis/redis/blob/unstable/src/adlist.c).

**[Garbage collectors — free lists]** — uses **a singly linked list of free memory blocks walked first-fit or best-fit on allocation** — because allocation is hot but the free list is typically short, and the walk fuses naturally with size-checking each block.

When `malloc` (or a GC's small-object allocator) needs `n` bytes, it walks the free list looking for a block of at least that size. This is the traversal loop with a `size >= n` filter in the visit step. Source: [glibc malloc.c](https://github.com/bminor/glibc/blob/master/malloc/malloc.c).

**[Web browsers — event listener chains]** — uses **a singly linked list of registered callbacks per event type, walked in registration order on dispatch** — because the dispatcher must call every listener in order and the list is rarely long enough for an array's contiguity win to matter.

When a DOM event fires, the browser walks the linked list of listeners for that event type and invokes each one. The list is built incrementally over the page's lifetime; rebuilding an array on each `addEventListener` would dominate the operation's cost. <!-- VERIFY: Chrome's Blink uses a HeapVector for listener lists, not a linked list — list-based dispatchers are common in older or simpler engines. -->

***

# Quiz

Force the answer to surface before reading the response — that is the test of whether the loop is internalised.

**[Recall] Q: What condition stops the standard singly-linked-list traversal loop?**
`current is null` — the tail node's `.next` is `null` by invariant, so the cursor eventually lands there and the `while` test fails.

**[Recall] Q: What are the time and space complexities of traversing a list of `n` nodes?**
O(n) time (each node visited once) and O(1) space (one cursor variable).

**[Reasoning] Q: Why can't you access the `k`-th node of a singly linked list in O(1) like an array?**
Nodes are scattered across memory and chained by pointers — no base address plus stride formula exists, so reaching index `k` requires `k` `.next` hops.

**[Reasoning] Q: Why does the standard loop save `current.next` into a local variable when the visit step modifies pointers (insert, delete, reverse)?**
The visit may rewire `current.next` away from the original successor; without a saved reference, the loop would advance into the wrong subgraph (or into `null`) and skip the rest of the list.

**[Tradeoff] Q: When is the recursive form of traversal a worse choice than the iterative form, and why?**
On any list long enough that O(n) stack frames matter — large production inputs — recursion risks a stack overflow because singly linked traversal is not tail-call-optimised in most runtimes (Python, default JVM). The iterative form keeps space at O(1).

***

# Practice Ladder

Five problems, easiest first. Each one is the traversal loop with a small twist plugged into the visit step.

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | [Middle of the Linked List](https://leetcode.com/problems/middle-of-the-linked-list/) | [Fast and Slow Pointers](./10-pattern-fast-and-slow-pointers/02-problems/01-middle-node-search.md) | Easy | Two cursors; advance one by `.next`, the other by `.next.next`. When fast falls off the end, slow sits at the middle. One pass, `O(n)` time, `O(1)` space. |
| 2 | [Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/) | [Reversal](./07-pattern-reversal/02-problems/01-reverse-a-list.md) | Easy | Standard traversal with three cursors: `prev`, `current`, `next_ref`. On each step, flip `current.next` to `prev` *after* saving the original next. |
| 3 | [Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/) | [Sliding Window Traversal](./09-pattern-sliding-window-traversal/02-problems/02-trim-nth-node.md) | Medium | Two cursors `n` nodes apart. Advance both until the lead falls off — the trailing one now points at the predecessor of the victim. One pass. |
| 4 | [Linked List Cycle](https://leetcode.com/problems/linked-list-cycle/) | [Fast and Slow Pointers](./10-pattern-fast-and-slow-pointers/01-pattern.md) | Easy | Same traversal, two speeds. If a cycle exists, fast laps slow inside the loop; otherwise fast hits `null`. `O(n)` time, `O(1)` space — better than a hash set. |
| 5 | [Palindrome Linked List](https://leetcode.com/problems/palindrome-linked-list/) | [Fast and Slow Pointers](./10-pattern-fast-and-slow-pointers/02-problems/04-palindrome-checker.md) | Easy | One pass to find the middle, reverse the back half, then walk both halves comparing values. Three traversals stacked — `O(n)` time, `O(1)` space. |

Once these feel reflexive, you have rehearsed every variation the rest of the chapter will demand.

***

# Further Reading

Curated paths in. The annotation tells you which to open first.

- **[CLRS — Chapter 10.2: Linked Lists](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)**
  ★ Essential — the canonical reference; covers singly, doubly, and circular variants with the same invariants used here.
- **[Linus Torvalds — "Linked list cleanup" mailing list post](https://lkml.org/lkml/2006/6/17/49)**
  ◆ Advanced — Torvalds explains why the kernel's intrusive doubly linked list avoids the special-case-the-head bug that beginners always write.
- **[Sedgewick & Wayne — *Algorithms* (4th ed), §1.3](https://algs4.cs.princeton.edu/13stacks/)**
  ★ Essential — stacks and queues built directly on the traversal primitive; the cleanest companion read after this lesson.
- **[CPython `Lib/queue.py` — `SimpleQueue` and `_PySimpleQueue`](https://github.com/python/cpython/blob/main/Lib/queue.py)**
  → Reference — a real-world linked-list-backed queue; instructive to read alongside the patterns in this chapter.
- **[Java `LinkedList` source — `LinkedList.java`](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/LinkedList.java)**
  ◆ Advanced — the doubly linked list behind `java.util.LinkedList`; trace how `add`, `remove`, and `get` map back to the operations in the table above.

***

# Cross-Links

**Prerequisites**

- [Introduction to Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-introduction-to-singly-linked-lists) — the node, the `.next` pointer, and why `head` is the only externally-held reference.
- [Introduction to Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-introduction) — the contrast that makes O(n) traversal feel painful: O(1) indexed access via address arithmetic.

**What comes next**

- [Insertion in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-insertion-in-singly-linked-lists) — the first operation built on top of the traversal loop: walk to a position, then rewire two pointers.
- [Deletion in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-deletion-in-singly-linked-lists) — traversal again, this time to find the predecessor before unlinking the victim.
- [Detecting a Cycle in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-detecting-cycle-in-singly-linked-lists) — what happens when the `null` exit condition breaks, and how two cursors at different speeds fix it.

***

## Final Takeaway

1. **Core mechanic:** start a cursor at `head`, do work, advance via `current = current.next`, and stop when `current` is `null` — that's traversal in singly linked lists, in O(n) time and O(1) space.
2. **Dominant tradeoff:** you gain cheap pointer rewiring (insert and delete are O(1) once you hold the right reference) but give up O(1) random access — every operation that needs an arbitrary node pays O(n) to *find* it.
3. **One thing to remember:** every other operation on a singly linked list — search, length, insert, delete, reverse, merge, cycle detection — is this loop with a different body; master the walk and the rest follows.
