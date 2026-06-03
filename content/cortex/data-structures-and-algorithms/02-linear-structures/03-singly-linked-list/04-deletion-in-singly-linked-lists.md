---
title: "Deletion In Singly Linked Lists"
summary: "Seven deletion variants for a singly linked list — first, last, by data, after a given node, before a given node, the given node itself, and at a distance. Every variant reduces to a two-line splice; the cost lives in the predecessor hunt, never in the wire-up."
---

# 4. Deletion in Singly Linked Lists

## The Hook

Deletion is the mirror image of insertion — and it has the same twist. An array's `del arr[0]` silently shifts every remaining element one slot left: 999,999 memory writes to remove a single byte. On a linked list, the same operation is **one pointer update**. The list gets shorter by snipping a single link; the rest of the chain stays exactly where it is in memory.

But singly-linked deletion has a cruel subtlety: to snip a link, you need the node **before** the victim — its `.next` is what you'll redirect. And a singly linked node cannot look backward. So for every "delete X" problem, the question collapses to: **how fast can we find the predecessor?** Head deletion? Instant — there is no predecessor, we just advance `head`. Middle deletion? Walk until `cur.next == victim`. Tail deletion? Walk the entire list. Every variant in this lesson is a different answer to the same predecessor-hunt question.

Seven variations follow. They all reduce to a two-line splice: **`prev.next = victim.next; free(victim)`**. Master the splice once, and every deletion problem you see — interview, production, embedded — solves itself.

---

## Table of contents

1. [Understanding the Problem](#understanding-the-problem)
2. [Supported Operations](#supported-operations)
3. [Internal Mechanics](#internal-mechanics)
4. [Understanding deletion of first node](#understanding-deletion-of-first-node)
5. [Delete first node](#delete-first-node)
6. [Understanding deletion of last node](#understanding-deletion-of-last-node)
7. [Delete last node](#delete-last-node)
8. [Understanding deletion by given data](#understanding-deletion-by-given-data)
9. [Delete node with given data](#delete-node-with-given-data)
10. [Delete nodes with given data](#delete-nodes-with-given-data)
11. [Understanding deletion after a given node](#understanding-deletion-after-a-given-node)
12. [Delete node after the given node](#delete-node-after-the-given-node)
13. [Understanding deletion before a given node](#understanding-deletion-before-a-given-node)
14. [Delete node before the given node](#delete-node-before-the-given-node)
15. [Understanding deletion of the given node](#understanding-deletion-of-the-given-node)
16. [Delete the given node](#delete-the-given-node)
17. [Understanding deletion at a given distance](#understanding-deletion-at-a-given-distance)
18. [Delete node at given distance](#delete-node-at-given-distance)
19. [Working Example](#working-example)
20. [Edge Cases and Pitfalls](#edge-cases-and-pitfalls)
21. [Production Reality](#production-reality)
22. [Practice Ladder](#practice-ladder)
23. [Quiz](#quiz)
24. [Further Reading](#further-reading)
25. [Cross-Links](#cross-links)
26. [Final Takeaway](#final-takeaway)

***

# Understanding the Problem

Deletion is the operation that exposes the asymmetry of singly linked lists. An array's `del arr[0]` *looks* free but quietly shifts every later element one slot left — a million writes to remove a single byte from a million-element list. A linked list's `delete head` is **two pointer reads and one assignment** — `head = head.next` — regardless of length. The same word, two completely different machines underneath.

The catch is what the splice needs:

- **Arrays** need the *index* — `O(1)` time random access lets the structure find the cut point instantly, but then `O(n)` time goes into shifting everything past it.
- **Singly linked lists** need the *predecessor* — the node whose `.next` will be redirected. Once the predecessor is in hand, the splice is `O(1)` time; finding it is the work.

To make this concrete: deleting the head of a million-node linked list is `O(1)` time — `head = head.next; free(old_head)`. Deleting the tail is `O(n)` time — every step of the walk from the head is a probe for "is your `.next` the tail?" Same operation name, same data structure, costs that differ by a factor of a million.

So the key idea is: every deletion in this lesson is the same two-line splice (`prev.next = victim.next`); the seven variants differ only in **how the predecessor is located** — and that's where the cost lives.

---

# Supported Operations

A singly linked list supports seven deletion variants — distinguished by *what reference the caller already holds* and *what predicate selects the victim*. The splice is always `O(1)` time; the variability is purely in the predecessor hunt.

| Operation | Inputs | Time | Space | Notes |
|---|---|---|---|---|
| Delete first node | `head` | `O(1)` | `O(1)` | No predecessor — advance `head` to `head.next`. |
| Delete last node | `head` | `O(n)` | `O(1)` | Walk until `cur.next.next == null` to find the tail's predecessor. |
| Delete node with given data | `head`, `data` | `O(n)` worst, `O(1)` best (head match) | `O(1)` | Walk `prev`/`cur` until `cur.val == data`; deletes the first match only. |
| Delete all nodes with given data | `head`, `data` | `O(n)` | `O(1)` | Single walk; a sentinel/dummy head removes the head-vs-mid branch. |
| Delete node after given node | `node` | `O(1)` | `O(1)` | The given node *is* the predecessor — pure splice, no walk. |
| Delete node before given node | `head`, `node` | `O(n)` worst, `O(1)` if `node == head.next` | `O(1)` | Walk to find the predecessor of the predecessor; no-op if `node == head`. |
| Delete the given node | `head`, `node` | `O(n)` worst, `O(1)` if `node == head` | `O(1)` | Walk to find the given node's predecessor, then splice. |
| Delete node at distance `X` | `head`, `X` | `O(X)` | `O(1)` | Walk `X − 1` steps to the predecessor, splice, return new head if `X = 0`. |

Two pieces are constant across the table: every variant ends in the **same splice** (`prev.next = victim.next`) and every variant frees **one** node (`O(1)` extra space). The variability is purely in the walk.

To make this concrete: deleting the node a caller already holds a reference to (`O(n)` time when given a *value*; `O(1)` time when given the *predecessor*) and deleting the head (`O(1)` time) take the same wall-clock time on a billion-node list when the caller cooperates with the right inputs. The same delete on a billion-node list when only a value or a position is supplied is `O(n)` time — the walk dominates.

So the tradeoff is: linked-list deletion gives `O(1)` time splicing in exchange for `O(n)` time predecessor search — there is no random access to amortise the hunt away. If predecessor lookup matters more than node-locality, switch to a doubly linked list.

---

# Internal Mechanics

Every deletion in a singly linked list — first, last, by value, after-a-node, before-a-node, by-reference, at-distance — compiles down to the same three operations. Lock these into muscle memory and every variant in this lesson collapses to a five-minute exercise.

The three operations:

- **Locate the predecessor**: walk from the head until you hold a reference `prev` whose `prev.next` is the victim. For *delete head*, the predecessor is the `head` variable itself; for *delete after node*, the given node *is* the predecessor.
- **Splice**: assign `prev.next = victim.next`. One write. The victim is now unreachable from the list.
- **Free**: in C / C++ call `free(victim)`. In garbage-collected languages (Python, Java) dropping the reference is sufficient — the runtime reclaims the node when no live reference remains.

The order matters: splice **before** free. If you free first, `victim.next` becomes a dangling read in languages without GC, and the splice writes garbage into `prev.next`.

To make this concrete: deleting `7` from `5 → 7 → 3` with `prev` pointing at `5`:

- `prev.next = victim.next` — `5.next` now points at `3` (was `7`).
- `free(victim)` — node `7` is reclaimed.

The list is now `5 → 3`. Reverse the order — `free(victim); prev.next = victim.next` — and the second line reads a freed pointer.

The same three operations cover every variant. Head deletion uses `head` as the predecessor (`head = head.next; free(old_head)`). Tail deletion walks to the second-to-last node (`prev.next.next == null`), then sets `prev.next = null` and frees. Sentinel-head implementations skip the head special case entirely — `dummy.next = head` makes the head "just another node" with a predecessor of `dummy`.

So the core insight is: every deletion is "find predecessor, one splice, free node" — what differs across the seven variants is only how the predecessor is found.

> 🖼 Diagram — TODO: three-frame splice — `prev` and `victim` identified, `prev.next` redirected to `victim.next`, `victim` freed; a fourth frame contrasting head-deletion (no predecessor — advance `head`) against mid-list deletion.

---

# Understanding deletion of first node

Similar to insertion, deletion is one of the most common operations in a linked list. Deleting the first node of a singly linked list is similar to **inserting a node at the beginning**. We need to consider two cases.

## 1\. The list is empty

When the list is empty, any attempt to delete a node is unnecessary because there are no nodes in the list. The list remains unchanged. We return the existing **head** — which is already `null`.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The list is not empty

Update **head** to hold the reference of the next node (the second node), effectively unlinking the first node. In GC languages (Java, Python, JS), the old head is automatically collected. In C/C++, we explicitly free/delete it.

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
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        },
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Before: head → 5 → 7 → 3 → 10. Mark first node for removal.",
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
          "target": "n2",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n1"
      ],
      "annotation": "head = head.next — head now points to node(7)",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
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
          "target": "n2",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Old head unreachable → freed (GC in Java/Python; explicit free in C). O(1).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete first node — advance head; old head is freed"
}
```

<p align="center"><strong>Case 2 — non-empty list: advance head to the second node; the first node is unlinked and freed.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Store the current head in a temporary pointer.
> -   **Step 2:** Move the head pointer to the next node.
> -   **Step 3:** Free/delete the old head node.
> -   **Step 4:** Return the new head node.

**What if there is only one node?** No special logic needed. The single node's `next` is `null`, so after `head = head.next`, head becomes `null` — correctly representing an empty list.

## Implementation


```python run viz=linked-list viz-root=head
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_first_node(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # Check if the list is empty
        if head is None:

            # Return None since there are no nodes to delete
            return None

        # Create a temporary pointer to store the current head node
        node_to_be_deleted: ListNode = head

        # Move the head pointer to the next node
        head = head.next

        # Delete the original head node to free memory
        del node_to_be_deleted

        # Return the new head node
        return head
```

```java run viz=linked-list viz-root=head
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
    public ListNode deleteFirstNode(ListNode head) {

        // Check if the list is empty
        if (head == null) {

            // Return null since there are no nodes to delete
            return null;
        }

        // Create a temporary pointer to store the current head node
        ListNode nodeToBeDeleted = head;

        // Move the head pointer to the next node
        head = head.next;

        // Delete the original head node to free memory
        nodeToBeDeleted = null;

        // Return the new head node
        return head;
    }
}
```


## Complexity Analysis

In any case, we only change the head pointer and free one node — no traversal required.

> **Best Case / Worst Case**
>
> -   Space Complexity — **O(1)**
> -   Time Complexity — **O(1)**

***

# Delete first node

## Problem Statement

Given the **head** of a singly linked list, write a function to delete the first node from this list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\]
> -   **Output:** \[7, 3, 10\]

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
    def delete_first_node(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # Check if the list is empty
        if head is None:

            # Return None since there are no nodes to delete
            return None

        # Create a temporary pointer to store the current head node
        node_to_be_deleted: ListNode = head

        # Move the head pointer to the next node
        head = head.next

        # Delete the original head node to free memory
        del node_to_be_deleted

        # Return the new head node
        return head


# Example from the problem statement
print(to_list(Solution().delete_first_node(from_list([5, 7, 3, 10]))))  # [7, 3, 10]

# Edge cases
print(to_list(Solution().delete_first_node(None)))                       # []
print(to_list(Solution().delete_first_node(from_list([42]))))            # []
print(to_list(Solution().delete_first_node(from_list([1, 2]))))          # [2]
print(to_list(Solution().delete_first_node(from_list([1, 2, 3, 4, 5])))) # [2, 3, 4, 5]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteFirstNode(ListNode head) {

            // Check if the list is empty
            if (head == null) {

                // Return null since there are no nodes to delete
                return null;
            }

            // Create a temporary pointer to store the current head node
            ListNode nodeToBeDeleted = head;

            // Move the head pointer to the next node
            head = head.next;

            // Delete the original head node to free memory
            nodeToBeDeleted = null;

            // Return the new head node
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().deleteFirstNode(fromList(5, 7, 3, 10))));  // [7, 3, 10]

        // Edge cases
        System.out.println(toList(new Solution().deleteFirstNode(null)));                   // []
        System.out.println(toList(new Solution().deleteFirstNode(fromList(42))));           // []
        System.out.println(toList(new Solution().deleteFirstNode(fromList(1, 2))));         // [2]
        System.out.println(toList(new Solution().deleteFirstNode(fromList(1, 2, 3, 4, 5)))); // [2, 3, 4, 5]
    }
}
```

</details>


***

# Understanding deletion of last node

We must access the second last node to delete the last node from a linked list. Then, we can update the pointer of this second last node to `null` and delete the last node. This process involves traversing the linked list and keeping track of the previous node (similar to inserting a node at the end). Let's go through the specific cases we need to consider.

## 1\. The list is empty

When the list is empty, meaning it contains no elements, any attempt to delete a node is unnecessary because there are no nodes in the list. Since there is nothing to remove, the list remains unchanged. We can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The list has only one node

Deleting the last node is the same as deleting the first node when only one node is in the list. We follow the same steps in both cases, such as deleting the first/last node. This involves deleting the head node and returning null.

> **Algorithm**
>
> -   **Step 1:** Delete the head node to free up memory.
> -   **Step 2:** Return `null` as the list is now empty.

## 3\. The list has more than one node

In this scenario, we need to update the pointer of the second last node in the list to hold `null` and then delete the last node. We need access to the list's last and second last nodes to accomplish this. We will traverse the list from the beginning while keeping track of the **current** and  nodes. This way, when we reach the last node, we will have access to the second last node. Thereafter, we can update the pointer of the second last node to `null`, or more intuitively, to the next of the last node, which should already be `null`, and then delete the last node.

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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        },
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Start: previous=head, current=head.next",
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
          "name": "previous",
          "target": "n3",
          "color": "#f59e0b"
        },
        {
          "name": "current",
          "target": "n4",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Walk until current is the tail (current.next == null). previous now holds the second-last.",
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
        }
      ],
      "cursor": [
        {
          "name": "previous",
          "target": "n3",
          "color": "#f59e0b"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n4"
      ],
      "annotation": "previous.next = null — tail is now unreachable",
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
      "annotation": "Free old tail. Final list: 5 → 7 → 3. O(n) walk + O(1) unlink.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete last node — walk with previous + current; unlink tail"
}
```

<p align="center"><strong>To delete the tail we keep two pointers, <code>previous</code> and <code>current</code>, so when <code>current</code> reaches the tail, <code>previous</code> is one step behind — ready to have its <code>next</code> set to <code>null</code>.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the list while keeping track of the `current` and `previous` nodes until reaching the last node.
> -   **Step 2:** Set the `next` pointer of the `previous` node to `null`.
> -   **Step 3:** Delete the last node to free up memory.
> -   **Step 4:** Return the original head node.

## Implementation

When implementing the logic for deleting the last node operation, we consider all the possible cases and subcases and write the code for each in conditional blocks.

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteLastNode(ListNode *head) {

        // If the list is empty, there's nothing to delete
        if (head == nullptr) {
            return nullptr;
        }

        // If there's only one node in the list, delete it and return
        // nullptr
        if (head->next == nullptr) {
            delete head;
            return nullptr;
        }

        // current node being iterated
        ListNode *current = head;

        // previous node
        ListNode *previous = nullptr;

        // Traverse the list until the last node is reached
        while (current != nullptr && current->next != nullptr) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current->next;
        }

        // At this point, current is pointing to the last node and
        // previous is pointing to the second-to-last node Update the
        // next pointer of the second-to-last node to skip the last node
        previous->next = current->next;

        // Delete the last node
        delete current;

        // Return the updated head of the list
        return head;
    }
};
```

Java

```java
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
    public ListNode deleteLastNode(ListNode head) {

        // If the list is empty, there's nothing to delete
        if (head == null) {
            return null;
        }

        // If there's only one node in the list, delete it and return
        // nullptr
        if (head.next == null) {
            head = null;
            return null;
        }

        // current node being iterated
        ListNode current = head;

        // previous node
        ListNode previous = null;

        // Traverse the list until the last node is reached
        while (current != null && current.next != null) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // At this point, current is pointing to the last node and
        // previous is pointing to the second-to-last node Update the
        // next pointer of the second-to-last node to skip the last node
        previous.next = current.next;

        // Delete the last node
        current = null;

        // Return the updated head of the list
        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteLastNode(head: ListNode | null): ListNode | null {

        // If the list is empty, there's nothing to delete
        if (head == null) {
            return null;
        }

        // If there's only one node in the list, delete it and return
        // nullptr
        if (head.next == null) {
            head = null;
            return null;
        }

        // current node being iterated
        let current: ListNode | null = head;

        // previous node
        let previous: ListNode | null = null;

        // Traverse the list until the last node is reached
        while (current !== null && current.next !== null) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // At this point, current is pointing to the last node and
        // previous is pointing to the second-to-last node Update the
        // next pointer of the second-to-last node to skip the last node
        previous.next = current?.next || null;

        // Delete the last node
        current = null;

        // Return the updated head of the list
        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteLastNode(head) {

        // If the list is empty, there's nothing to delete
        if (head == null) {
            return null;
        }

        // If there's only one node in the list, delete it and return
        // nullptr
        if (head.next == null) {
            head = null;
            return null;
        }

        // current node being iterated
        let current = head;

        // previous node
        let previous = null;

        // Traverse the list until the last node is reached
        while (current !== null && current.next !== null) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // At this point, current is pointing to the last node and
        // previous is pointing to the second-to-last node Update the
        // next pointer of the second-to-last node to skip the last node
        previous.next = current?.next || null;

        // Delete the last node
        current = null;

        // Return the updated head of the list
        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_last_node(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty, there's nothing to delete
        if head is None:
            return None

        # If there's only one node in the list, delete it and return
        # None
        if head.next is None:
            head = None
            return None

        # current node being iterated
        current: Optional[ListNode] = head

        # previous node
        previous: Optional[ListNode] = None

        # Traverse the list until the last node is reached
        while current is not None and current.next is not None:

            # Move the previous pointer to the current node
            previous = current

            # Move the current pointer to the next node
            current = current.next

        # At this point, current is pointing to the last node and
        # previous is pointing to the second-to-last node Update the
        # next pointer of the second-to-last node to skip the last node
        if previous and current:
            previous.next = current.next

        # Delete the last node
        current = None

        # Return the updated head of the list
        return head
```

## Complexity Analysis

The time complexity of the above function depends on the number of nodes in the linked list. Since we must traverse the entire list to reach the end, its time complexity is **O(N)**, where **N** is the number of nodes in the list.

The function's space complexity is **O(1)** because it only creates a single new node and does not use any additional data structures.

> **Best Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**
>
> **Worst Case**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Delete last node

## Problem Statement

Given the **head** of a singly linked list, write a function to delete the last node from this linked list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\]
> -   **Output:** \[5, 7, 3\]

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
    def delete_last_node(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:
        if head is None:

            # If the list is empty, there's nothing to delete
            return None

        if head.next is None:

            # If there's only one node in the list, delete it and return
            # None
            head = None
            return None

        # current node being iterated
        current: Optional[ListNode] = head

        # previous node
        previous: Optional[ListNode] = None

        # Traverse the list until the last node is reached
        while current is not None and current.next is not None:

            # Move the previous pointer to the current node
            previous = current

            # Move the current pointer to the next node
            current = current.next

        # At this point, current is pointing to the last node and
        # previous is pointing to the second-to-last node. Update the
        # next pointer of the second-to-last node to skip the last node
        if previous and current:
            previous.next = current.next

        # Delete the last node
        current = None

        # Return the updated head of the list
        return head


# Example from the problem statement
print(to_list(Solution().delete_last_node(from_list([5, 7, 3, 10]))))  # [5, 7, 3]

# Edge cases
print(to_list(Solution().delete_last_node(None)))                       # []
print(to_list(Solution().delete_last_node(from_list([42]))))            # []
print(to_list(Solution().delete_last_node(from_list([1, 2]))))          # [1]
print(to_list(Solution().delete_last_node(from_list([1, 2, 3, 4, 5])))) # [1, 2, 3, 4]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteLastNode(ListNode head) {

            // If the list is empty, there's nothing to delete
            if (head == null) {
                return null;
            }

            // If there's only one node in the list, delete it and return
            // nullptr
            if (head.next == null) {
                head = null;
                return null;
            }

            // current node being iterated
            ListNode current = head;

            // previous node
            ListNode previous = null;

            // Traverse the list until the last node is reached
            while (current != null && current.next != null) {

                // Move the previous pointer to the current node
                previous = current;

                // Move the current pointer to the next node
                current = current.next;
            }

            // At this point, current is pointing to the last node and
            // previous is pointing to the second-to-last node Update the
            // next pointer of the second-to-last node to skip the last node
            previous.next = current.next;

            // Delete the last node
            current = null;

            // Return the updated head of the list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().deleteLastNode(fromList(5, 7, 3, 10))));  // [5, 7, 3]

        // Edge cases
        System.out.println(toList(new Solution().deleteLastNode(null)));                   // []
        System.out.println(toList(new Solution().deleteLastNode(fromList(42))));           // []
        System.out.println(toList(new Solution().deleteLastNode(fromList(1, 2))));         // [1]
        System.out.println(toList(new Solution().deleteLastNode(fromList(1, 2, 3, 4, 5)))); // [1, 2, 3, 4]
    }
}
```

</details>


***

# Understanding deletion by given data

Deleting a node with the given data in a singly linked list can be implemented by piggybacking on the search operation. Instead of returning the data after finding it, we delete it in this operation. Let’s consider the possible cases when deleting a node with the given data.

## 1\. The list is empty

When the list is empty, meaning it contains no elements, any attempt to delete a node is unnecessary because there are no nodes in the list. Since there is nothing to remove, the list remains unchanged. We can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The first node is deleted

If the data matches the first node, this case becomes the same as **deleting the first node**. We update the **head** to store the reference to the second node and delete the old head.

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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        },
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Before: target matches the head",
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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n2",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n1"
      ],
      "annotation": "head = head.next — old head unreachable, freed. O(1).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Target is the head — single pointer update, head advances"
}
```

<p align="center"><strong>Deleting the head is a single pointer update — move <code>head</code> forward and the old head becomes unreachable (garbage-collected or freed).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a temporary pointer to store the current head node.
> -   **Step 2:** Move the head pointer to the next node.
> -   **Step 3:** Delete the original head node to free up memory.
> -   **Step 4:** Return the new head node.

## 3\. The node to be deleted is not the first node

To delete a node that is not the first node of the linked list, we need access to the node 1 step before the one to be deleted. We will traverse the list from the beginning while keeping track of the **current** and nodes. This way, when we reach the node with the given data, we will have access to its previous node, which we need to update.Deleting the given node involves a three-step process.

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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        },
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Walk: prev=node(5), current=node(7). Match. Stop.",
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
          "to": "n3",
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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n2"
      ],
      "annotation": "prev.next = current.next — node(5) now points past node(7) to node(3)",
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
      "annotation": "Free current. Final: 5 → 3 → 10. O(n) walk + O(1) unlink.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Interior deletion — prev + current walk, then splice prev.next over current"
}
```

<p align="center"><strong>To delete an interior node, we need its predecessor. A two-pointer walk (<code>prev</code> + <code>current</code>) gives us both — then <code>prev.next = current.next</code> unlinks the target in O(1).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the list, keeping track of `current` and `previous` nodes until reaching the `given` node.
> -   **Step 2:** Set the `previous` node's `next` pointer to hold the node's reference stored in the `next` pointer of the `current` node.
> -   **Step 3:** Delete the `current` node to free up memory.
> -   **Step 4:** Return the original head node.

## 4\. The node to be deleted could not be found

If the data provided does not match the data of any node in the linked list, then such a node does not exist in the list, so we return the existing **head**.

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
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        },
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Looking for value 99. Start at head.",
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
      "annotation": "Walk through 5 → 7 → 3 → 10 — no match",
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
      "annotation": "current = current.next becomes null — target not found. Return head unchanged.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Target not in list — traversal falls off, return head unchanged"
}
```

<p align="center"><strong>If we reach the tail without finding the target, the list contains no node with that value — return the head unchanged.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the list, keeping track of `current` and `previous` nodes until reaching the `given` node.
> -   **Step 2:** Return the original head node.

## Implementation

When implementing the logic for deleting a node with a given data operation, we consider all the possible cases and write the code for each in conditional blocks.

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteNodeWithGivenData(ListNode *head, int data) {

        // If the list is empty, return nullptr
        if (head == nullptr)
            return nullptr;

        // If the head node contains the given data
        if (head->val == data) {

            // Create a temporary pointer to the head node
            ListNode *nodeToBeDeleted = head;

            // Update the head pointer to the next node
            head = head->next;

            // Delete the previous head node
            delete nodeToBeDeleted;

            // Return the updated head pointer
            return head;
        }

        // Pointer to the current node, starting from the head
        ListNode *current = head;

        // Pointer to the previous node, initially nullptr
        ListNode *previous = nullptr;

        // If the target data is not in the first node, search for it in
        // the rest of the list
        while (current != nullptr && current->val != data) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current->next;
        }

        // If the given data is not found, return the original head
        // pointer
        if (current == nullptr) {
            return head;
        }

        // Update the next pointer of the previous node to skip the
        // current node
        previous->next = current->next;

        // Delete the node with the given data
        delete current;

        // Return the head of the list, with the target data node removed
        return head;
    }
};
```

Java

```java
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
    public ListNode deleteNodeWithGivenData(ListNode head, int data) {

        // If the list is empty, return null
        if (head == null) return null;

        // If the head node contains the given data
        if (head.val == data) {

            // Create a temporary pointer to the head node
            ListNode nodeToBeDeleted = head;

            // Update the head pointer to the next node
            head = head.next;

            // Delete the previous head node
            nodeToBeDeleted = null;

            // Return the updated head pointer
            return head;
        }

        // Pointer to the current node, starting from the head
        ListNode current = head;

        // Pointer to the previous node, initially null
        ListNode previous = null;

        // If the target data is not in the first node, search for it in
        // the rest of the list
        while (current != null && current.val != data) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // If the given data is not found, return the original head
        // pointer
        if (current == null) {
            return head;
        }

        // Update the next pointer of the previous node to skip the
        // current node
        previous.next = current.next;

        // Delete the node with the given data
        current = null;

        // Return the head of the list, with the target data node removed
        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteNodeWithGivenData(
        head: ListNode | null,
        data: number
    ): ListNode | null {

        // If the list is empty, return null
        if (head === null) {
            return null;
        }

        // If the head node contains the given data
        if (head.val === data) {

            // Create a temporary pointer to the head node
            let nodeToBeDeleted: ListNode | null = head;

            // Update the head pointer to the next node
            head = head.next;

            // Delete the previous head node
            nodeToBeDeleted = null;

            // Return the updated head pointer
            return head;
        }

        // Pointer to the current node, starting from the head
        let current: ListNode | null = head;

        // Pointer to the previous node, initially null
        let previous: ListNode | null = null;

        // If the target data is not in the first node, search for it in
        // the rest of the list
        while (current !== null && current.val !== data) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // If the given data is not found, return the original head
        // pointer
        if (current === null) {
            return head;
        }

        // Update the next pointer of the previous node to skip the
        // current node
        if (previous !== null) {
            previous.next = current.next;
        }

        // Delete the node with the given data
        current = null;

        // Return the head of the list, with the target data node removed
        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteNodeWithGivenData(head, data) {

        // If the list is empty, return null
        if (head === null) {
            return null;
        }

        // If the head node contains the given data
        if (head.val === data) {

            // Create a temporary pointer to the head node
            let nodeToBeDeleted = head;

            // Update the head pointer to the next node
            head = head.next;

            // Delete the previous head node
            nodeToBeDeleted = null;

            // Return the updated head pointer
            return head;
        }

        // Pointer to the current node, starting from the head
        let current = head;

        // Pointer to the previous node, initially null
        let previous = null;

        // If the target data is not in the first node, search for it in
        // the rest of the list
        while (current !== null && current.val !== data) {

            // Move the previous pointer to the current node
            previous = current;

            // Move the current pointer to the next node
            current = current.next;
        }

        // If the given data is not found, return the original head
        // pointer
        if (current === null) {
            return head;
        }

        // Update the next pointer of the previous node to skip the
        // current node
        if (previous !== null) {
            previous.next = current.next;
        }

        // Delete the node with the given data
        current = null;

        // Return the head of the list, with the target data node removed
        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_node_with_given_data(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # If the list is empty, return None
        if head is None:
            return None

        # If the head node contains the given data
        if head.val == data:

            # Create a temporary pointer to the head node
            node_to_be_deleted: ListNode = head

            # Update the head pointer to the next node
            head = head.next

            # Delete the previous head node
            del node_to_be_deleted

            # Return the updated head pointer
            return head

        # Pointer to the current node, starting from the head
        current: Optional[ListNode] = head

        # Pointer to the previous node, initially None
        previous: Optional[ListNode] = None

        # If the target data is not in the first node, search for it in
        # the rest of the list
        while current is not None and current.val != data:

            # Move the previous pointer to the current node
            previous = current

            # Move the current pointer to the next node
            current = current.next

        # If the given data is not found, return the original head
        # pointer
        if current is None:
            return head

        # Update the next pointer of the previous node to skip the
        # current node
        if previous and current:
            previous.next = current.next

        # Delete the node with the given data
        del current

        # Return the head of the list, with the target data node removed
        return head
```

## Complexity Analysis

The time complexity of deleting a node with the given data depends on the position of the node in the linked list. Since the list must be traversed to locate the node containing the specified data, the number of operations varies based on where the node is found.

### Best case

The best case occurs when the given data matches the first node. In this case, the function must delete the first node of the list. This process takes **constant** time, regardless of the linked list's size.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: target
  next
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "·"
  next: "null"
}
cost: |md
  **1 comparison**

  **1 pointer update**

  `O(1)`
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n1.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Best case — target is the head. No walking needed; single pointer update. <strong>O(1)</strong>.</strong></p>

### Worst case

On the other hand, the worst case occurs when the given data matches the last node. In this case, the function must delete the last node of the list. This process takes linear time proportional to the length of the linked list, i.e., **O(N)**.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: "·"
  next
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "target (tail)"
  next: "null"
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
cost: |md
  **n−1 hops to reach predecessor**

  **1 pointer update**

  `O(n)`
| {style.fill: "#fee2e2"; style.stroke: "#dc2626"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n4.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — target is the tail. We must walk the entire list to reach its predecessor. <strong>O(n)</strong>.</strong></p>

The function's space complexity is constant, as it only creates a few variables that take up a fixed amount of space regardless of the size of the linked list.

> **Best Case** - The node with given data is the first node
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**
>
> **Worst Case** - The node with the given data is the last node
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Delete node with given data

## Problem Statement

Given the **head** of a singly linked list and a **data** value, write a function to delete the first node with the given data from the list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], data = 3
> -   **Output:** \[5, 7, 10\]

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
    def delete_node_with_given_data(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # If the list is empty, return None
        if head is None:
            return None

        # If the head node contains the given data
        if head.val == data:

            # Create a temporary pointer to the head node
            node_to_be_deleted: ListNode = head

            # Update the head pointer to the next node
            head = head.next

            # Delete the previous head node
            del node_to_be_deleted

            # Return the updated head pointer
            return head

        # Pointer to the current node, starting from the head
        current: Optional[ListNode] = head

        # Pointer to the previous node, initially None
        previous: Optional[ListNode] = None

        # If the target data is not in the first node, search for it in
        # the rest of the list
        while current is not None and current.val != data:

            # Move the previous pointer to the current node
            previous = current

            # Move the current pointer to the next node
            current = current.next

        # If the given data is not found, return the original head
        # pointer
        if current is None:
            return head

        # Update the next pointer of the previous node to skip the
        # current node
        if previous and current:
            previous.next = current.next

        # Delete the node with the given data
        del current

        # Return the head of the list, with the target data node removed
        return head


# Example from the problem statement
print(to_list(Solution().delete_node_with_given_data(from_list([5, 7, 3, 10]), 3)))  # [5, 7, 10]

# Delete head
print(to_list(Solution().delete_node_with_given_data(from_list([5, 7, 3, 10]), 5)))  # [7, 3, 10]

# Delete last node
print(to_list(Solution().delete_node_with_given_data(from_list([5, 7, 3, 10]), 10))) # [5, 7, 3]

# Data not in list
print(to_list(Solution().delete_node_with_given_data(from_list([1, 2, 3]), 99)))      # [1, 2, 3]

# Empty list
print(to_list(Solution().delete_node_with_given_data(None, 5)))                       # []

# Single-node list — found
print(to_list(Solution().delete_node_with_given_data(from_list([7]), 7)))             # []

# Delete first occurrence only (duplicate values)
print(to_list(Solution().delete_node_with_given_data(from_list([3, 3, 3]), 3)))       # [3, 3]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteNodeWithGivenData(ListNode head, int data) {

            // If the list is empty, return null
            if (head == null) {
                return null;
            }

            // If the head node contains the given data
            if (head.val == data) {

                // Create a temporary pointer to the head node
                ListNode nodeToBeDeleted = head;

                // Update the head pointer to the next node
                head = head.next;

                // Delete the previous head node
                nodeToBeDeleted = null;

                // Return the updated head pointer
                return head;
            }

            // Pointer to the current node, starting from the head
            ListNode current = head;

            // Pointer to the previous node, initially null
            ListNode previous = null;

            // If the target data is not in the first node, search for it in
            // the rest of the list
            while (current != null && current.val != data) {

                // Move the previous pointer to the current node
                previous = current;

                // Move the current pointer to the next node
                current = current.next;
            }

            // If the given data is not found, return the original head
            // pointer
            if (current == null) {
                return head;
            }

            // Update the next pointer of the previous node to skip the
            // current node
            previous.next = current.next;

            // Delete the node with the given data
            current = null;

            // Return the head of the list, with the target data node removed
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(5, 7, 3, 10), 3)));  // [5, 7, 10]

        // Delete head
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(5, 7, 3, 10), 5)));  // [7, 3, 10]

        // Delete last node
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(5, 7, 3, 10), 10))); // [5, 7, 3]

        // Data not in list
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(1, 2, 3), 99)));      // [1, 2, 3]

        // Empty list
        System.out.println(toList(new Solution().deleteNodeWithGivenData(null, 5)));                    // []

        // Single-node list — found
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(7), 7)));             // []

        // Delete first occurrence only (duplicate values)
        System.out.println(toList(new Solution().deleteNodeWithGivenData(fromList(3, 3, 3), 3)));       // [3, 3]
    }
}
```

</details>


***

# Delete nodes with given data

## Problem Statement

Given the **head** of a singly linked list and a **data** value, write a function to delete **all** the nodes with the given data from the list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10, 3, 12\], data = 3
> -   **Output:** \[5, 7, 10, 12\]

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
    def delete_nodes_with_given_data(
        self, head: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Check if the list is empty
        if head is None:
            return None

        # Remove any nodes at the beginning of the list with the given
        # data
        while head is not None and head.val == data:
            node_to_be_deleted: ListNode = head
            head = head.next
            del node_to_be_deleted

        # If the list becomes empty after removing nodes, return None
        if head is None:
            return None

        # Initialize pointers for previous and current nodes
        previous: Optional[ListNode] = head
        current: Optional[ListNode] = head.next

        # Traverse the list and remove nodes with the given data
        while current is not None:

            # Remove any nodes with the given data
            while current is not None and current.val == data:
                node_to_be_deleted = current
                current = current.next
                del node_to_be_deleted

            # Update the links between nodes to bypass the removed nodes
            if previous:
                previous.next = current
            previous = current

            # Move to the next node
            if current is not None:
                current = current.next

        # Return the modified list
        return head


# Example from the problem statement
print(to_list(Solution().delete_nodes_with_given_data(from_list([5, 7, 3, 10, 3, 12]), 3)))  # [5, 7, 10, 12]

# All nodes match — empty result
print(to_list(Solution().delete_nodes_with_given_data(from_list([3, 3, 3]), 3)))              # []

# Head matches multiple times, then stops
print(to_list(Solution().delete_nodes_with_given_data(from_list([3, 3, 1, 2]), 3)))           # [1, 2]

# No match — list unchanged
print(to_list(Solution().delete_nodes_with_given_data(from_list([1, 2, 3]), 9)))              # [1, 2, 3]

# Empty list
print(to_list(Solution().delete_nodes_with_given_data(None, 5)))                              # []

# Single-node list, match
print(to_list(Solution().delete_nodes_with_given_data(from_list([7]), 7)))                    # []

# Duplicates at tail
print(to_list(Solution().delete_nodes_with_given_data(from_list([1, 2, 5, 5]), 5)))           # [1, 2]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteNodesWithGivenData(ListNode head, int data) {

            // Check if the list is empty
            if (head == null) {
                return null;
            }

            // Remove any nodes at the beginning of the list with the given
            // data
            while (head != null && head.val == data) {
                ListNode nodeToBeDeleted = head;
                head = head.next;

                // Delete the node
                nodeToBeDeleted = null;
            }

            // If the list becomes empty after removing nodes, return null
            if (head == null) {
                return null;
            }

            // Initialize pointers for previous and current nodes
            ListNode previous = head;
            ListNode current = head.next;

            // Traverse the list and remove nodes with the given data
            while (current != null) {

                // Remove any nodes with the given data
                while (current != null && current.val == data) {
                    ListNode nodeToBeDeleted = current;
                    current = current.next;

                    // Delete the node
                    nodeToBeDeleted = null;
                }

                // Update the links between nodes to bypass the removed nodes
                previous.next = current;
                previous = current;

                // Move to the next node
                if (current != null) {
                    current = current.next;
                }
            }

            // Return the modified list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(5, 7, 3, 10, 3, 12), 3)));  // [5, 7, 10, 12]

        // All nodes match — empty result
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(3, 3, 3), 3)));              // []

        // Head matches multiple times, then stops
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(3, 3, 1, 2), 3)));           // [1, 2]

        // No match — list unchanged
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(1, 2, 3), 9)));              // [1, 2, 3]

        // Empty list
        System.out.println(toList(new Solution().deleteNodesWithGivenData(null, 5)));                           // []

        // Single-node list, match
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(7), 7)));                    // []

        // Duplicates at tail
        System.out.println(toList(new Solution().deleteNodesWithGivenData(fromList(1, 2, 5, 5), 5)));           // [1, 2]
    }
}
```

</details>


***

# Understanding deletion after a given node

When deleting a node, we require access to the node one step before the node to be deleted to manipulate its pointer. If we already have the previous node, the deletion process becomes straightforward. This is what makes this deletion operation the simplest of all delete operations. Let's examine all the cases we need to consider.

## 1\. The list is empty

If the list is empty and contains no elements, we cannot find the given node because it does not exist within the list. Deleting the node after the given node is not possible because there is no reference point within the list to perform the deletion. In this case, we can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The given node is the last node

When the given node is the last node in the list, attempting to delete a node after it becomes an invalid operation. This is because, by definition, the last node has no successor, i.e., no node following it in the sequence. We can return the **head** because no other operation needs to be done.

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
      "annotation": "given is node(3); given.next is null — nothing to delete. Return head unchanged.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete after given node — given is the tail: no successor, return unchanged"
}
```

<p align="center"><strong>If the given node is the tail, there is no "node after it" to delete — return the list unchanged.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 3\. The given node is not the last node

To delete a node after a given node, we can update the pointer of the given node to skip over the node that needs to be deleted. Then, we can remove the node that we want to delete.

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
          "target": "n2",
          "color": "#3b82f6"
        },
        {
          "name": "next",
          "target": "n3",
          "color": "#8b5cf6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "given = node(7); given.next = node(3) is the victim",
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
          "to": "n4",
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
      "removed": [
        "n3"
      ],
      "annotation": "given.next = victim.next — node(7) now points past node(3) to node(10)",
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
      "annotation": "Free victim. Final: 5 → 7 → 10. O(1).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete after given node — O(1): redirect given.next past the victim"
}
```

<p align="center"><strong>Deleting the node after a given node is O(1) — we already have the predecessor (the given node itself). Just redirect <code>given.next</code> past the victim.</strong></p>

> **Algorithm:**
>
> -   **Step 1:** Create a temporary pointer to store the reference of the node after the `given` node.
> -   **Step 2:** Set the `next` pointer of the `given` node to hold the node's reference stored in the `next` pointer of the node after the `given` node.
> -   **Step 3:** Delete the node after the given node to free up memory.
> -   **Step 4:** Return the original head node.

## Implementation

When implementing the logic for deleting a node after a given node operation, we consider all the possible cases and write the code for each in conditional blocks.

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteNodeAfterTheGivenNode(
        ListNode *head,
        ListNode *node
    ) {

        // If the list is empty, there's nothing to delete, so return
        // nullptr.
        if (head == nullptr) {
            return nullptr;
        }

        // If the given node is nullptr or it is the last node in the
        // list, there's no node to delete, so return the original head.
        if (node == nullptr || node->next == nullptr) {
            return head;
        }

        // Store the next node in a temporary variable.
        ListNode *nodeToBeDeleted = node->next;

        // Link the current node (node) to the node after the one being
        // deleted.
        node->next = nodeToBeDeleted->next;

        // Delete the node that was after the given node.
        delete nodeToBeDeleted;

        // Return the original head.
        return head;
    }
};
```

Java

```java
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
    public ListNode deleteNodeAfterTheGivenNode(
        ListNode head,
        ListNode node
    ) {

        // If the list is empty, there's nothing to delete, so return
        // null.
        if (head == null) {
            return null;
        }

        // If the given node is null or it is the last node in the list,
        // there's no node to delete, so return the original head.
        if (node == null || node.next == null) {
            return head;
        }

        // Store the next node in a temporary variable.
        ListNode nodeToBeDeleted = node.next;

        // Link the current node (node) to the node after the one being
        // deleted.
        node.next = nodeToBeDeleted.next;

        // Delete the node that was after the given node.
        nodeToBeDeleted = null;

        // Return the original head.
        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteNodeAfterTheGivenNode(
        head: ListNode | null,
        node: ListNode | null
    ): ListNode | null {

        // If the list is empty, there's nothing to delete, so return
        // null.
        if (head === null) {
            return null;
        }

        // If the given node is null or it is the last node in the list,
        // there's no node to delete, so return the original head.
        if (node === null || node.next === null) {
            return head;
        }

        // Store the next node in a temporary variable.
        const nodeToBeDeleted = node.next;

        // Link the current node (node) to the node after the one being
        // deleted.
        node.next = nodeToBeDeleted.next;

        // Delete the node that was after the given node.
        nodeToBeDeleted.next = null;

        // Return the original head.
        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteNodeAfterTheGivenNode(head, node) {

        // If the list is empty, there's nothing to delete, so return
        // null.
        if (head === null) {
            return null;
        }

        // If the given node is null or it is the last node in the list,
        // there's no node to delete, so return the original head.
        if (node === null || node.next === null) {
            return head;
        }

        // Store the next node in a temporary variable.
        const nodeToBeDeleted = node.next;

        // Link the current node (node) to the node after the one being
        // deleted.
        node.next = nodeToBeDeleted.next;

        // Delete the node that was after the given node.
        nodeToBeDeleted.next = null;

        // Return the original head.
        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_node_after_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty, there's nothing to delete, so return
        # None.
        if head is None:
            return None

        # If the given node is None or it is the last node in the list,
        # there's no node to delete, so return the original head.
        if node is None or node.next is None:
            return head

        # Store the next node in a temporary variable.
        node_to_be_deleted: ListNode = node.next

        # Link the current node (node) to the node after the one being
        # deleted.
        node.next = node_to_be_deleted.next

        # Delete the node that was after the given node.
        del node_to_be_deleted

        # Return the original head.
        return head
```

**Does the order of operations matter here?**It is important to emphasize the order of these assignment and deletion operations. We must ensure we do not access a node after it is deleted, so deletion should be the last step. An incorrect sequence of operations can lead to a program crash, and such errors are hard to debug.

## Complexity Analysis

We need to make some pointer manipulations to delete the node. Therefore, the time complexity is constant. Similarly, we don't create any new nodes in all cases, so the space complexity is also constant, i.e., **O(1)**.

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

# Delete node after the given node

## Problem Statement

Given the **head** of a singly linked list and a **random** **node** in a linked list, write a function to delete the node after the given node and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], node = 7
> -   **Output:** \[5, 7, 10\]

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
    def delete_node_after_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty, there's nothing to delete, so return
        # None.
        if head is None:
            return None

        # If the given node is None or it is the last node in the list,
        # there's no node to delete, so return the original head.
        if node is None or node.next is None:
            return head

        # Store the next node in a temporary variable.
        node_to_be_deleted: ListNode = node.next

        # Link the current node (node) to the node after the one being
        # deleted.
        node.next = node_to_be_deleted.next

        # Delete the node that was after the given node.
        del node_to_be_deleted

        # Return the original head.
        return head


# Example from the problem statement — delete after node with val 7
h1 = from_list([5, 7, 3, 10])
print(to_list(Solution().delete_node_after_the_given_node(h1, h1.next)))      # [5, 7, 10]

# Delete after head
h2 = from_list([1, 2, 3])
print(to_list(Solution().delete_node_after_the_given_node(h2, h2)))           # [1, 3]

# Given node is last — no deletion
h3 = from_list([1, 2, 3])
last = h3.next.next
print(to_list(Solution().delete_node_after_the_given_node(h3, last)))         # [1, 2, 3]

# node is None — no deletion
h4 = from_list([1, 2])
print(to_list(Solution().delete_node_after_the_given_node(h4, None)))         # [1, 2]

# Empty list
print(to_list(Solution().delete_node_after_the_given_node(None, None)))       # []

# Two-node list — delete after head
h5 = from_list([1, 2])
print(to_list(Solution().delete_node_after_the_given_node(h5, h5)))           # [1]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteNodeAfterTheGivenNode(
            ListNode head,
            ListNode node
        ) {

            // If the list is empty, there's nothing to delete, so return
            // null.
            if (head == null) {
                return null;
            }

            // If the given node is null or it is the last node in the list,
            // there's no node to delete, so return the original head.
            if (node == null || node.next == null) {
                return head;
            }

            // Store the next node in a temporary variable.
            ListNode nodeToBeDeleted = node.next;

            // Link the current node (node) to the node after the one being
            // deleted.
            node.next = nodeToBeDeleted.next;

            // Delete the node that was after the given node.
            nodeToBeDeleted = null;

            // Return the original head.
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement — delete after node with val 7
        ListNode h1 = fromList(5, 7, 3, 10);
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(h1, h1.next)));      // [5, 7, 10]

        // Delete after head
        ListNode h2 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(h2, h2)));           // [1, 3]

        // Given node is last — no deletion
        ListNode h3 = fromList(1, 2, 3);
        ListNode last = h3.next.next;
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(h3, last)));         // [1, 2, 3]

        // node is null — no deletion
        ListNode h4 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(h4, null)));         // [1, 2]

        // Empty list
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(null, null)));       // []

        // Two-node list — delete after head
        ListNode h5 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteNodeAfterTheGivenNode(h5, h5)));           // [1]
    }
}
```

</details>


***

# Understanding deletion before a given node

Deleting the node before the given node is similar to **inserting before the given node**. We need access to the node before the one that has to be deleted. Keeping this in mind, let's try to understand the different cases we must consider before coming up with a general solution.

## 1\. The list is empty

If the list is empty and contains no elements, we cannot find the given node because it does not exist within the list. Deleting the node after the given node is not possible because there is no reference point within the list to perform the deletion. In this case, we can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The given node is the first node

When the given node is the first node in the list, attempting to delete a node before it becomes an invalid operation. This is because, by definition, the first node has no predecessor, i.e., no node preceding it in the sequence. We can return the **head** because no other operation needs to be done.

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
        }
      ],
      "cursor": [
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "given == head — no node precedes it. Return head unchanged.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete before given node — given is the head: no predecessor, return unchanged"
}
```

<p align="center"><strong>If the given node is the head, there is no "node before it" to delete — return the list unchanged.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 3\. The given node is the second node

This is a unique situation because removing the node before the second node essentially means deleting the linked list's head node. As learned earlier, this scenario is identical to **deleting the first node**. We need to update the head to store the reference to the second node and then delete the old head.

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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        },
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        },
        {
          "name": "next",
          "target": "n1",
          "color": "#8b5cf6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "given == second node; the predecessor is the head — head is the victim",
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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n2",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n1"
      ],
      "annotation": "head = head.next — node(5) freed. O(1).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete before given node — given is second: collapses to 'delete the head'"
}
```

<p align="center"><strong>When <code>given</code> is the second node, the node before it is the head. This special case collapses to "delete the head" — handled in one pointer update.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a temporary pointer to store the current head node.
> -   **Step 2:** Move the head pointer to the next node.
> -   **Step 3:** Delete the original head node to free up memory.
> -   **Step 4:** Return the new head node.

## 4\. The given node is any other node

To delete the node before a given node, we need to access the node two steps before the given node. We traverse the linked list while keeping track of the **current**,  and **previousToPrevious** nodes. As soon as we reach the given node, we update the pointer of the **previousToPrevious** node to hold the reference to the current node and then delete the node.

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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        },
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Walk: prev=node(5), current=node(7). Match. Stop.",
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
          "to": "n3",
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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n2"
      ],
      "annotation": "prev.next = current.next — node(5) now points past node(7) to node(3)",
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
      "annotation": "Free current. Final: 5 → 3 → 10. O(n) walk + O(1) unlink.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Interior deletion — prev + current walk, then splice prev.next over current"
}
```

<p align="center"><strong>To delete an interior node, we need its predecessor. A two-pointer walk (<code>prev</code> + <code>current</code>) gives us both — then <code>prev.next = current.next</code> unlinks the target in O(1).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the list, keeping track of `current`, `previous` and `previousToPrevious` nodes until reaching the given node.
> -   **Step 2:** Set the `previousToPrevious` node's `next` pointer to hold the reference of the `current` node.
> -   **Step 3:** Delete the `previous` node to free up memory.
> -   **Step 4:** Return the original head node.

## Implementation

When implementing the logic for deleting the node before the given node, we consider all the possible cases and write the code for each in conditional blocks. 

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteNodeBeforeTheGivenNode(
        ListNode *head,
        ListNode *node
    ) {

        // If the head or the given node is nullptr, there is nothing to
        // delete Return the existing head
        if (head == nullptr || node == nullptr) {
            return head;
        }

        // If the given node is the head node, we cannot delete the node
        // before it
        if (node == head) {
            return head;
        }

        // If the node to delete is the immediate next node of the head
        // Update the head to point to the next node, delete the original
        // head, and return the updated head
        if (head->next != nullptr && head->next == node) {
            ListNode *nodeToBeDeleted = head;
            head = head->next;
            delete nodeToBeDeleted;
            return head;
        }

        // Initialize variables for traversal
        // current node being examined
        ListNode *current = head->next;

        // Node preceding the current node
        ListNode *previous = head;

        // Node preceding the previous node
        ListNode *previousToprevious = nullptr;

        // Traverse the linked list until we find the node or reach the
        // end.
        while (current != nullptr && current != node) {
            previousToprevious = previous;
            previous = current;
            current = current->next;
        }

        // If the node to delete was not found, return the head as is.
        if (current == nullptr) {
            return head;
        }

        // Connect the previous node to the current node, bypassing the
        // node to delete.
        previousToprevious->next = current;
        delete previous;

        return head;
    }
};
```

Java

```java
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
    public ListNode deleteNodeBeforeTheGivenNode(
        ListNode head,
        ListNode node
    ) {

        // If the head or the given node is null, there is nothing to
        // delete Return the existing head
        if (head == null || node == null) {
            return head;
        }

        // If the given node is the head node, we cannot delete the node
        // before it
        if (node == head) {
            return head;
        }

        // If the node to delete is the immediate next node of the head
        // Update the head to point to the next node, delete the original
        // head, and return the updated head
        if (head.next != null && head.next == node) {
            ListNode nodeToBeDeleted = head;
            head = head.next;

            // Dereference for garbage collection
            nodeToBeDeleted = null;
            return head;
        }

        // Initialize variables for traversal
        // current node being examined
        ListNode current = head.next;

        // Node preceding the current node
        ListNode previous = head;

        // Node preceding the previous node
        ListNode previousToprevious = null;

        // Traverse the linked list until we find the node or reach the
        // end.
        while (current != null && current != node) {
            previousToprevious = previous;
            previous = current;
            current = current.next;
        }

        // If the node to delete was not found, return the head as is.
        if (current == null) {
            return head;
        }

        // Connect the previous node to the current node, bypassing the
        // node to delete.
        previousToprevious.next = current;

        // Dereference for garbage collection
        previous = null;

        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteNodeBeforeTheGivenNode(
        head: ListNode | null,
        node: ListNode | null
    ): ListNode | null {

        // If the head or the given node is null, there is nothing to
        // delete Return the existing head
        if (head === null || node === null) {
            return head;
        }

        // If the given node is the head node, we cannot delete the node
        // before it
        if (node === head) {
            return head;
        }

        // If the node to delete is the immediate next node of the head
        // Update the head to point to the next node, delete the original
        // head, and return the updated head
        if (head.next !== null && head.next === node) {
            let nodeToBeDeleted = head;
            head = head.next;

            // Dereference for garbage collection
            nodeToBeDeleted = null;
            return head;
        }

        // Initialize variables for traversal
        // current node being examined
        let current: ListNode | null = head.next;

        // Node preceding the current node
        let previous: ListNode | null = head;

        // Node preceding the previous node
        let previousToprevious: ListNode | null = null;

        // Traverse the linked list until we find the node or reach the
        // end.
        while (current !== null && current !== node) {
            previousToprevious = previous;
            previous = current;
            current = current.next;
        }

        // If the node to delete was not found, return the head as is.
        if (current === null) {
            return head;
        }

        // Connect the previous node to the current node, bypassing the
        // node to delete.
        previousToprevious.next = current;

        // Dereference for garbage collection
        previous = null;

        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteNodeBeforeTheGivenNode(head, node) {

        // If the head or the given node is null, there is nothing to
        // delete Return the existing head
        if (head === null || node === null) {
            return head;
        }

        // If the given node is the head node, we cannot delete the node
        // before it
        if (node === head) {
            return head;
        }

        // If the node to delete is the immediate next node of the head
        // Update the head to point to the next node, delete the original
        // head, and return the updated head
        if (head.next !== null && head.next === node) {
            let nodeToBeDeleted = head;
            head = head.next;

            // Dereference for garbage collection
            nodeToBeDeleted = null;
            return head;
        }

        // Initialize variables for traversal
        // current node being examined
        let current = head.next;

        // Node preceding the current node
        let previous = head;

        // Node preceding the previous node
        let previousToprevious = null;

        // Traverse the linked list until we find the node or reach the
        // end.
        while (current !== null && current !== node) {
            previousToprevious = previous;
            previous = current;
            current = current.next;
        }

        // If the node to delete was not found, return the head as is.
        if (current === null) {
            return head;
        }

        // Connect the previous node to the current node, bypassing the
        // node to delete.
        previousToprevious.next = current;

        // Dereference for garbage collection
        previous = null;

        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_node_before_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the head or the given node is None, there is nothing to delete
        # Return the existing head
        if head is None or node is None:
            return head

        # If the given node is the head node, we cannot delete the node
        # before it
        if node == head:
            return head

        # If the node to delete is the immediate next node of the head
        # Update the head to point to the next node, delete the original
        # head, and return the updated head
        if head.next is not None and head.next == node:
            node_to_be_deleted = head
            head = head.next
            node_to_be_deleted = (

                # Dereference for garbage collection
                None
            )
            return head

        # Initialize variables for traversal
        # current node being examined
        current = head.next

        # Node preceding the current node
        previous = head

        # Node preceding the previous node
        previous_toprevious = None

        # Traverse the linked list until we find the node or reach the
        # end.
        while current is not None and current != node:
            previous_toprevious = previous
            previous = current
            current = current.next

        # If the node to delete was not found, return the head as is.
        if current is None:
            return head

        # Connect the previous node to the current node, bypassing the
        # node to delete.
        previous_toprevious.next = current

        # Dereference for garbage collection
        previous = None

        return head
```

## Complexity Analysis

The time complexity of deleting a node before a given node depends on the position of the target node in the linked list. Since the list must be traversed to locate the node and its predecessor, the number of operations varies based on where the deletion occurs.

### Best case

The best case occurs when the given node is the second node of the list. In this case, the function must delete the first node of the list. This process takes **constant** time, regardless of the linked list's size.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: target
  next
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "·"
  next: "null"
}
cost: |md
  **1 comparison**

  **1 pointer update**

  `O(1)`
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n1.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Best case — target is the head. No walking needed; single pointer update. <strong>O(1)</strong>.</strong></p>

### Worst case

On the other hand, the worst case occurs when the given data matches the last node. In this case, the function must delete the second last node of the list. This process takes linear time proportional to the length of the linked list, i.e., **O(N)**.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: "·"
  next
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "target (tail)"
  next: "null"
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
cost: |md
  **n−1 hops to reach predecessor**

  **1 pointer update**

  `O(n)`
| {style.fill: "#fee2e2"; style.stroke: "#dc2626"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n4.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — target is the tail. We must walk the entire list to reach its predecessor. <strong>O(n)</strong>.</strong></p>

The function's space complexity is constant, as it only creates a few variables that take up a fixed amount of space regardless of the size of the linked list.

> **Best Case** - The given node is the second node in the list.
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**
>
> **Worst Case** - The given node is the last node in the list.
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Delete node before the given node

## Problem Statement

Given the **head** of a singly linked list and a **random node** in the list, write a function to delete the node before the given node and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], node = 3
> -   **Output:** \[5, 3, 10\]

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
    def delete_node_before_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the head or the given node is None, there is nothing to
        # delete. Return the existing head
        if head is None or node is None:
            return head

        # If the given node is the head node, we cannot delete the node
        # before it
        if node == head:
            return head

        # If the node to delete is the immediate next node of the head
        # Update the head to point to the next node, delete the original
        # head, and return the updated head
        if head.next is not None and head.next == node:
            node_to_be_deleted = head
            head = head.next
            node_to_be_deleted = (

                # Dereference for garbage collection
                None
            )
            return head

        # Initialize variables for traversal
        # current node being examined
        current = head.next

        # Node preceding the current node
        previous = head

        # Node preceding the previous node
        previous_toprevious = None

        # Traverse the linked list until we find the node or reach the
        # end.
        while current is not None and current != node:
            previous_toprevious = previous
            previous = current
            current = current.next

        # If the node to delete was not found, return the head as is.
        if current is None:
            return head

        # Connect the previous node to the current node, bypassing the
        # node to delete.
        previous_toprevious.next = current

        # Dereference for garbage collection
        previous = None

        return head


# Example from the problem statement — delete before node with val 3
h1 = from_list([5, 7, 3, 10])
print(to_list(Solution().delete_node_before_the_given_node(h1, h1.next.next)))  # [5, 3, 10]

# Given node is head — no deletion
h2 = from_list([1, 2, 3])
print(to_list(Solution().delete_node_before_the_given_node(h2, h2)))            # [1, 2, 3]

# Delete head (node is second)
h3 = from_list([1, 2, 3])
print(to_list(Solution().delete_node_before_the_given_node(h3, h3.next)))       # [2, 3]

# Delete before last node
h4 = from_list([1, 2, 3, 4])
print(to_list(Solution().delete_node_before_the_given_node(h4, h4.next.next.next)))  # [1, 2, 4]

# node is None — no deletion
h5 = from_list([1, 2])
print(to_list(Solution().delete_node_before_the_given_node(h5, None)))          # [1, 2]

# Two-node list, given node is second
h6 = from_list([1, 2])
print(to_list(Solution().delete_node_before_the_given_node(h6, h6.next)))       # [2]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteNodeBeforeTheGivenNode(
            ListNode head,
            ListNode node
        ) {

            // If the head or the given node is null, there is nothing to
            // delete Return the existing head
            if (head == null || node == null) {
                return head;
            }

            // If the given node is the head node, we cannot delete the node
            // before it
            if (node == head) {
                return head;
            }

            // If the node to delete is the immediate next node of the head
            // Update the head to point to the next node, delete the original
            // head, and return the updated head
            if (head.next != null && head.next == node) {
                ListNode nodeToBeDeleted = head;
                head = head.next;

                // Dereference for garbage collection
                nodeToBeDeleted = null;
                return head;
            }

            // Initialize variables for traversal
            // current node being examined
            ListNode current = head.next;

            // Node preceding the current node
            ListNode previous = head;

            // Node preceding the previous node
            ListNode previousToprevious = null;

            // Traverse the linked list until we find the node or reach the
            // end.
            while (current != null && current != node) {
                previousToprevious = previous;
                previous = current;
                current = current.next;
            }

            // If the node to delete was not found, return the head as is.
            if (current == null) {
                return head;
            }

            // Connect the previous node to the current node, bypassing the
            // node to delete.
            previousToprevious.next = current;

            // Dereference for garbage collection
            previous = null;

            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement — delete before node with val 3
        ListNode h1 = fromList(5, 7, 3, 10);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h1, h1.next.next)));  // [5, 3, 10]

        // Given node is head — no deletion
        ListNode h2 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h2, h2)));            // [1, 2, 3]

        // Delete head (node is second)
        ListNode h3 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h3, h3.next)));       // [2, 3]

        // Delete before last node
        ListNode h4 = fromList(1, 2, 3, 4);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h4, h4.next.next.next)));  // [1, 2, 4]

        // node is null — no deletion
        ListNode h5 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h5, null)));          // [1, 2]

        // Two-node list, given node is second
        ListNode h6 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteNodeBeforeTheGivenNode(h6, h6.next)));       // [2]
    }
}
```

</details>


***

# Understanding deletion of the given node

Deleting the given node is identical to **deleting the node with the given data**. The only difference is that instead of seeking the node with the specified data value, we will search for the node that matches the given node. Let's examine the various scenarios we need to consider.

## 1\. The list is empty

If the list is empty and contains no elements, we cannot find the given node because it does not exist within the list. Therefore, deleting the given node is not possible because there is no reference point within the list to perform the deletion. In this case, we can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. The first node is deleted

If the given node matches the first node, this case becomes the same as **deleting the first node**. We update the **head** to store the reference to the second node and delete the old head.

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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n1",
          "color": "#10b981"
        },
        {
          "name": "current",
          "target": "n1",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Before: target matches the head",
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
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n2",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n1"
      ],
      "annotation": "head = head.next — old head unreachable, freed. O(1).",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Target is the head — single pointer update, head advances"
}
```

<p align="center"><strong>Deleting the head is a single pointer update — move <code>head</code> forward and the old head becomes unreachable (garbage-collected or freed).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Create a temporary pointer to store the current head node.
> -   **Step 2:** Move the head pointer to the next node.
> -   **Step 3:** Delete the original head node to free up memory.
> -   **Step 4:** Return the new head node.

## 3\. The node to be deleted is not the first node

To delete a node that is not the first node of the linked list, we need access to the node 1 step before the one to be deleted. We will traverse the list from the beginning while keeping track of the **current** and nodes. This way, when we reach the given node, we will have access to its previous node, which we need to update. Deleting the given node involves a three step process.

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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        },
        {
          "name": "current",
          "target": "n2",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Walk: prev=node(5), current=node(7). Match. Stop.",
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
          "to": "n3",
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
          "name": "previous",
          "target": "n1",
          "color": "#f59e0b"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n2"
      ],
      "annotation": "prev.next = current.next — node(5) now points past node(7) to node(3)",
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
      "annotation": "Free current. Final: 5 → 3 → 10. O(n) walk + O(1) unlink.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Interior deletion — prev + current walk, then splice prev.next over current"
}
```

<p align="center"><strong>To delete an interior node, we need its predecessor. A two-pointer walk (<code>prev</code> + <code>current</code>) gives us both — then <code>prev.next = current.next</code> unlinks the target in O(1).</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the list, keeping track of `current` and `previous` nodes until reaching the given node.
> -   **Step 2:** Set the `previous` node's `next` pointer to hold the node's reference stored in the `next` pointer of the `current` node.
> -   **Step 3:** Delete the `current` node to free up memory.
> -   **Step 4:** Return the original head node.

## Implementation

When implementing the logic to delete the given node, we consider all the possible cases and subcases and write the code for each in conditional blocks.

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteTheGivenNode(ListNode *head, ListNode *node) {

        // Check if either the head or the given node is null
        if (head == nullptr || node == nullptr) {
            return head;
        }

        // The given node is the head node
        if (node == head) {

            // Update the head to the next node
            head = head->next;

            // Delete the given node
            delete node;

            // Return the updated head
            return head;
        }

        // Pointer to traverse the list
        ListNode *current = head;

        // Pointer to track the previous node
        ListNode *previous = nullptr;

        // Traverse the list until the current node matches the given
        // node
        while (current != nullptr && current != node) {

            // Update the previous node
            previous = current;

            // Move to the next node
            current = current->next;
        }

        // If the current node becomes null, the given node was not found
        // in the list
        if (current == nullptr) {

            // Return the original head
            return head;
        }

        // Update the previous node's next pointer to skip the current
        // node
        previous->next = current->next;

        // Delete the current node
        delete current;

        // Return the head of the modified list
        return head;
    }
};
```

Java

```java
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
    public ListNode deleteTheGivenNode(ListNode head, ListNode node) {

        // Check if either the head or the given node is null
        if (head == null || node == null) {
            return head;
        }

        // The given node is the head node
        if (node == head) {

            // Update the head to the next node
            head = head.next;

            // Delete the given node
            node = null;

            // Return the updated head
            return head;
        }

        // Pointer to traverse the list
        ListNode current = head;

        // Pointer to track the previous node
        ListNode previous = null;

        // Traverse the list until the current node matches the given
        // node
        while (current != null && current != node) {

            // Update the previous node
            previous = current;

            // Move to the next node
            current = current.next;
        }

        // If the current node becomes null, the given node was not found
        // in the list
        if (current == null) {

            // Return the original head
            return head;
        }

        // Update the previous node's next pointer to skip the current
        // node
        previous.next = current.next;

        // Delete the current node
        current = null;

        // Return the head of the modified list
        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteTheGivenNode(
        head: ListNode | null,
        node: ListNode | null
    ): ListNode | null {

        // Check if either the head or the given node is null
        if (head === null || node === null) {
            return head;
        }

        // The given node is the head node
        if (node === head) {

            // Update the head to the next node
            head = head.next;

            // Delete the given node
            node = null;

            // Return the updated head
            return head;
        }

        // Pointer to traverse the list
        let current: ListNode | null = head;

        // Pointer to track the previous node
        let previous: ListNode | null = null;

        // Traverse the list until the current node matches the given
        // node
        while (current !== null && current !== node) {

            // Update the previous node
            previous = current;

            // Move to the next node
            current = current.next;
        }

        // If the current node becomes null, the given node was not found
        // in the list
        if (current === null) {

            // Return the original head
            return head;
        }

        // Update the previous node's next pointer to skip the current
        // node
        if (previous !== null) {
            previous.next = current.next;
        }

        // Delete the current node
        current = null;

        // Return the head of the modified list
        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteTheGivenNode(head, node) {

        // Check if either the head or the given node is null
        if (head === null || node === null) {
            return head;
        }

        // The given node is the head node
        if (node === head) {

            // Update the head to the next node
            head = head.next;

            // Delete the given node
            node = null;

            // Return the updated head
            return head;
        }

        // Pointer to traverse the list
        let current = head;

        // Pointer to track the previous node
        let previous = null;

        // Traverse the list until the current node matches the given
        // node
        while (current !== null && current !== node) {

            // Update the previous node
            previous = current;

            // Move to the next node
            current = current.next;
        }

        // If the current node becomes null, the given node was not found
        // in the list
        if (current === null) {

            // Return the original head
            return head;
        }

        // Update the previous node's next pointer to skip the current
        // node
        if (previous !== null) {
            previous.next = current.next;
        }

        // Delete the current node
        current = null;

        // Return the head of the modified list
        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional, List, Any

class Solution:
    def delete_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # Check if either the head or the given node is None
        if head is None or node is None:
            return head

        # The given node is the head node
        if node == head:

            # Update the head to the next node
            head = head.next

            # Delete the given node
            del node

            # Return the updated head
            return head

        # Pointer to traverse the list
        current: Optional[ListNode] = head

        # Pointer to track the previous node
        previous: Optional[ListNode] = None

        # Traverse the list until the current node matches the given node
        while current is not None and current != node:

            # Update the previous node
            previous = current

            # Move to the next node
            current = current.next

        # If the current node becomes None, the given node was not found
        # in the list
        if current is None:

            # Return the original head
            return head

        # Update the previous node's next pointer to skip the current
        # node
        if previous and current:
            previous.next = current.next

        # Delete the current node
        del current

        # Return the head of the modified list
        return head
```

## Complexity Analysis

The time complexity of deleting a given node depends on its position in the linked list. As the node must be located through traversal before removal, the number of operations varies based on where the node appears in the list.

### Best case

The best case occurs when the given node is the first node. In this case, the function must delete the first node of the list. This process takes **constant** time, regardless of the linked list's size.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: target
  next
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "·"
  next: "null"
}
cost: |md
  **1 comparison**

  **1 pointer update**

  `O(1)`
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n1.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Best case — target is the head. No walking needed; single pointer update. <strong>O(1)</strong>.</strong></p>

### Worst case

On the other hand, the worst case occurs when the given node is the last node. In this case, the function must delete the last node of the list. This process takes linear time proportional to the length of the linked list, i.e., **O(N)**.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: "·"
  next
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "target (tail)"
  next: "null"
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
cost: |md
  **n−1 hops to reach predecessor**

  **1 pointer update**

  `O(n)`
| {style.fill: "#fee2e2"; style.stroke: "#dc2626"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n4.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — target is the tail. We must walk the entire list to reach its predecessor. <strong>O(n)</strong>.</strong></p>

The function's space complexity is constant, as it only creates a few variables that take up a fixed amount of space regardless of the size of the linked list.

> **Best Case** - The given node is the first node.
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**
>
> **Worst Case** - The given node is the last node.
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Delete the given node

## Problem Statement

Given the **head** of a singly linked list and a **random node** in that linked list, write a function to delete that node from the list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], node = 7
> -   **Output:** \[5, 3, 10\]

<details>
<summary><h2>Solution</h2></summary>



```python run viz=linked-list viz-root=head
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
    def delete_the_given_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> Optional[ListNode]:

        # Check if either the head or the given node is None
        if head is None or node is None:
            return head

        # The given node is the head node
        if node == head:

            # Update the head to the next node
            head = head.next

            # Delete the given node
            del node

            # Return the updated head
            return head

        # Pointer to traverse the list
        current: Optional[ListNode] = head

        # Pointer to track the previous node
        previous: Optional[ListNode] = None

        # Traverse the list until the current node matches the given node
        while current is not None and current != node:

            # Update the previous node
            previous = current

            # Move to the next node
            current = current.next

        # If the current node becomes None, the given node was not found
        # in the list
        if current is None:

            # Return the original head
            return head

        # Update the previous node's next pointer to skip the current
        # node
        if previous and current:
            previous.next = current.next

        # Delete the current node
        del current

        # Return the head of the modified list
        return head


# Example from the problem statement — delete node with val 7
h1 = from_list([5, 7, 3, 10])
print(to_list(Solution().delete_the_given_node(h1, h1.next)))          # [5, 3, 10]

# Delete head
h2 = from_list([1, 2, 3])
print(to_list(Solution().delete_the_given_node(h2, h2)))                # [2, 3]

# Delete last node
h3 = from_list([1, 2, 3])
print(to_list(Solution().delete_the_given_node(h3, h3.next.next)))      # [1, 2]

# Single-node list — delete the only node
h4 = from_list([42])
print(to_list(Solution().delete_the_given_node(h4, h4)))                # []

# node is None — no deletion
h5 = from_list([1, 2])
print(to_list(Solution().delete_the_given_node(h5, None)))              # [1, 2]

# Empty list
print(to_list(Solution().delete_the_given_node(None, None)))            # []

# Two-node list — delete first
h6 = from_list([1, 2])
print(to_list(Solution().delete_the_given_node(h6, h6)))                # [2]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteTheGivenNode(ListNode head, ListNode node) {

            // Check if either the head or the given node is null
            if (head == null || node == null) {
                return head;
            }

            // The given node is the head node
            if (node == head) {

                // Update the head to the next node
                head = head.next;

                // Delete the given node
                node = null;

                // Return the updated head
                return head;
            }

            // Pointer to traverse the list
            ListNode current = head;

            // Pointer to track the previous node
            ListNode previous = null;

            // Traverse the list until the current node matches the given
            // node
            while (current != null && current != node) {

                // Update the previous node
                previous = current;

                // Move to the next node
                current = current.next;
            }

            // If the current node becomes null, the given node was not found
            // in the list
            if (current == null) {

                // Return the original head
                return head;
            }

            // Update the previous node's next pointer to skip the current
            // node
            previous.next = current.next;

            // Delete the current node
            current = null;

            // Return the head of the modified list
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement — delete node with val 7
        ListNode h1 = fromList(5, 7, 3, 10);
        System.out.println(toList(new Solution().deleteTheGivenNode(h1, h1.next)));          // [5, 3, 10]

        // Delete head
        ListNode h2 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().deleteTheGivenNode(h2, h2)));               // [2, 3]

        // Delete last node
        ListNode h3 = fromList(1, 2, 3);
        System.out.println(toList(new Solution().deleteTheGivenNode(h3, h3.next.next)));     // [1, 2]

        // Single-node list — delete the only node
        ListNode h4 = fromList(42);
        System.out.println(toList(new Solution().deleteTheGivenNode(h4, h4)));               // []

        // node is null — no deletion
        ListNode h5 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteTheGivenNode(h5, null)));             // [1, 2]

        // Empty list
        System.out.println(toList(new Solution().deleteTheGivenNode(null, null)));           // []

        // Two-node list — delete first
        ListNode h6 = fromList(1, 2);
        System.out.println(toList(new Solution().deleteTheGivenNode(h6, h6)));               // [2]
    }
}
```

</details>


***

# Understanding deletion at a given distance

Deleting a node at a distance `X` is similar to **inserting a node at a given distance**. Just like inserting at a distance `X` We can solve this problem without keeping track of the previous node while traversing. However, we must consider a few special cases, so let’s examine them.

## 1\. The list is empty

When the list is empty, meaning it contains no elements, any attempt to delete a node is unnecessary because there are no nodes in the list. Since there is nothing to remove, the list remains unchanged. We can return the existing **head**, as the list is empty, and no node needs to be deleted.

> **Algorithm**
>
> -   **Step 1:** Return the original head node.

## 2\. X = 0

When X equals 0, we need to **delete the first node**. We have previously covered this concept. We should update the head to store the reference to the second node and then delete the old head.

X = 0

> **Algorithm**
>
> -   **Step 1:** Create a temporary pointer to store the current head node.
> -   **Step 2:** Move the head pointer to the next node.
> -   **Step 3:** Delete the original head node to free up memory.
> -   **Step 4:** Return the new head node.

## 3\. X < size of the list

When we need to delete a specific node from a list, we should traverse the list until we reach the node just before the one we want to delete. Keep track of the current node and traverse `X-1` steps instead of `X`. At the end of the loop, we will reach the node one step before the node that needs to be deleted. Then, the problem becomes **deleting a node after a given node**, where the given node is the node one step before the node that has to be deleted. Update the reference in the given node's pointer to point to the node after the one that has to be deleted. Once the connections have been updated, safely delete the next node.

```d3 widget=list-single
{
  "steps": [
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "4",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
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
          "target": "n0",
          "color": "#3b82f6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Start: current=head, counter=0. Walk X−1 = 1 hop.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "4",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
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
          "name": "next",
          "target": "n2",
          "color": "#8b5cf6"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "After 1 hop, current at node(7). current.next = node(3) is the victim.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "4",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
        {
          "from": "n1",
          "to": "n3",
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
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [
        "n2"
      ],
      "annotation": "current.next = current.next.next — node(7) now points past node(3) to node(10)",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n3",
          "label": "10",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n4",
          "label": "4",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
        {
          "from": "n1",
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
          "target": "n0",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "Free victim. Final: 5 → 7 → 10 → 4. O(X) walk + O(1) unlink.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete at distance X = 2 in size-5 list — walk X−1 hops, then unlink"
}
```

<p align="center"><strong>When <code>X</code> is within bounds, walk <code>X−1</code> steps to reach the predecessor and splice out its successor.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the distance X - 1 while keeping track of the `current` node.
> -   **Step 2:** Set the `next` pointer of the `current` node to hold the node's reference stored in the `next` pointer of the node to be deleted.
> -   **Step 3:** Delete the node after the `current` node to free up memory.
> -   **Step 4:** Return the original head node.

## 4\. X >= the size of the list

This indicates an invalid query. For example, we cannot delete the 10th node in a list of size 3. We will return the existing **head** node.

**What about the case when X == size of the linked list?**

This is also an invalid case. To clarify, let's consider a list of size 5. In this scenario, the potential values of `X` could range from 0 to 4, meaning `[0, 4]`. Therefore, an input 5 would be invalid. It's important to note that X represents the distance from the head node, not the node's position.

```d3 widget=list-single
{
  "steps": [
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
        {
          "from": "n1",
          "to": "n2",
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
      "annotation": "Walking forward — counter reaches 2 at node(3). Target X−1 = 4.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    },
    {
      "nodes": [
        {
          "id": "n0",
          "label": "5",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n1",
          "label": "7",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        },
        {
          "id": "n2",
          "label": "3",
          "kind": "node",
          "meta": [],
          "slot": null,
          "cardId": "",
          "layoutKind": ""
        }
      ],
      "edges": [
        {
          "from": "n0",
          "to": "n1",
          "label": "next"
        },
        {
          "from": "n1",
          "to": "n2",
          "label": "next"
        }
      ],
      "cursor": [
        {
          "name": "head",
          "target": "n0",
          "color": "#10b981"
        }
      ],
      "highlight": [],
      "changed": [],
      "removed": [],
      "annotation": "current = current.next becomes null before counter hits 4. X=5 out of range (valid range [0, 2]). Return head unchanged.",
      "line": 0,
      "frames": [],
      "cardCursor": []
    }
  ],
  "title": "Delete at distance X = 5 in size-3 list — walk falls off, return head unchanged"
}
```

<p align="center"><strong>When <code>X</code> is ≥ list size, there is no node at that index — return the list unchanged without modifying anything.</strong></p>

> **Algorithm**
>
> -   **Step 1:** Traverse the distance X - 1 while keeping track of the `current` node.
> -   **Step 2:** Return the original head node.

## Implementation

When implementing the logic for deleting nodes at a distance, we consider all the possible cases and write the code for each in conditional blocks.

C++

```cpp
/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode() : val(0), next(nullptr) {}
 *     ListNode(int val) : val(val), next(nullptr) {}
 * };
 */

using namespace std;

class Solution {
public:
    ListNode *deleteNodeAtGivenDistance(ListNode *head, int X) {

        // If the head is nullptr (empty list), return nullptr
        if (head == nullptr) {
            return nullptr;
        }

        // If X is 0, delete the head node
        if (X == 0) {
            ListNode *nodeToBeDeleted = head;

            // Update the head to the next node
            head = head->next;

            // Delete the original head node
            delete nodeToBeDeleted;

            // Return the updated head
            return head;
        }

        int counter = 0;
        ListNode *current = head;

        // Traverse to the node at position X - 1
        while (current != nullptr && counter < X - 1) {

            // Move to the next node
            current = current->next;

            // Increment the counter
            counter++;
        }

        // If the node at position X - 1 is null or the next node is
        // null, return the head
        if (current == nullptr || current->next == nullptr) {
            return head;
        }

        // Store the node to be deleted
        ListNode *nodeToBeDeleted = current->next;

        // Update the next pointer of current node
        current->next = current->next->next;

        // Delete the node at position X
        delete nodeToBeDeleted;

        // Return the head
        return head;
    }
};
```

Java

```java
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
    public ListNode deleteNodeAtGivenDistance(ListNode head, int X) {

        // If the head is null (empty list), return null
        if (head == null) {
            return null;
        }

        // If X is 0, delete the head node
        if (X == 0) {
            ListNode nodeToBeDeleted = head;

            // Update the head to the next node
            head = head.next;

            // Delete the original head node
            nodeToBeDeleted = null;

            // Return the updated head
            return head;
        }

        int counter = 0;
        ListNode current = head;

        // Traverse to the node at position X - 1
        while (current != null && counter < X - 1) {

            // Move to the next node
            current = current.next;

            // Increment the counter
            counter++;
        }

        // If the node at position X - 1 is null or the next node is
        // null, return the head
        if (current == null || current.next == null) {
            return head;
        }

        // Store the node to be deleted
        ListNode nodeToBeDeleted = current.next;

        // Update the next pointer of current node
        current.next = current.next.next;

        // Delete the node at position X
        nodeToBeDeleted = null;

        // Return the head
        return head;
    }
```

Typescript

```typescript
/**
 * Definition for singly-linked list.
 * class ListNode {
 *     val: number
 *     next: ListNode | null
 *     constructor(val?: number, next?: ListNode | null) {
 *         this.val = (val===undefined ? 0 : val)
 *         this.next = (next===undefined ? null : next)
 *     }
 * }
 */

export class Solution {
    deleteNodeAtGivenDistance(
        head: ListNode | null,
        X: number
    ): ListNode | null {

        // If the head is null (empty list), return null
        if (head === null) {
            return null;
        }

        // If X is 0, delete the head node
        if (X === 0) {
            const nodeToBeDeleted: ListNode = head;

            // Update the head to the next node
            head = head.next;

            // Delete the original head node
            nodeToBeDeleted.next = null;

            // Return the updated head
            return head;
        }

        let counter = 0;
        let current: ListNode | null = head;

        // Traverse to the node at position X - 1
        while (current !== null && counter < X - 1) {

            // Move to the next node
            current = current.next;

            // Increment the counter
            counter++;
        }

        // If the node at position X - 1 is null or the next node is
        // null, return the head
        if (current === null || current.next === null) {
            return head;
        }

        // Store the node to be deleted
        const nodeToBeDeleted: ListNode = current.next;

        // Update the next pointer of current node
        current.next = current.next.next;

        // Delete the node at position X
        nodeToBeDeleted.next = null;

        // Return the head
        return head;
    }
```

Javascript

```javascript
/**
 * Definition for singly-linked list.
 * function ListNode(val, next) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.next = (next===undefined ? null : next)
 * }
 */

export class Solution {
    deleteNodeAtGivenDistance(head, X) {

        // If the head is null (empty list), return null
        if (head === null) {
            return null;
        }

        // If X is 0, delete the head node
        if (X === 0) {
            const nodeToBeDeleted = head;

            // Update the head to the next node
            head = head.next;

            // Delete the original head node
            nodeToBeDeleted.next = null;

            // Return the updated head
            return head;
        }
        let counter = 0;
        let current = head;

        // Traverse to the node at position X - 1
        while (current !== null && counter < X - 1) {

            // Move to the next node
            current = current.next;

            // Increment the counter
            counter++;
        }

        // If the node at position X - 1 is null or the next node is
        // null, return the head
        if (current === null || current.next === null) {
            return head;
        }

        // Store the node to be deleted
        const nodeToBeDeleted = current.next;

        // Update the next pointer of current node
        current.next = current.next.next;

        // Delete the node at position X
        nodeToBeDeleted.next = null;

        // Return the head
        return head;
    }
```

Python

```python
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional

class Solution:
    def delete_node_at_given_distance(
        self, head: Optional[ListNode], x: int
    ) -> Optional[ListNode]:

        # If the head is None (empty list), return None
        if head is None:
            return None

        # If x is 0, delete the head node
        if x == 0:
            node_to_be_deleted: ListNode = head

            # Update the head to the next node
            head = head.next

            # Delete the original head node
            del node_to_be_deleted

            # Return the updated head
            return head

        counter: int = 0
        current: Optional[ListNode] = head

        # Traverse to the node at position x - 1
        while current is not None and counter < x - 1:

            # Move to the next node
            current = current.next

            # Increment the counter
            counter += 1

        # If the node at position x - 1 is None or the next node is None,
        # return the head
        if current is None or current.next is None:
            return head

        # Store the node to be deleted
        node_to_be_deleted = current.next

        # Update the next pointer of the current node
        current.next = current.next.next

        # Delete the node at position x
        del node_to_be_deleted

        # Return the head
        return head
```

## Complexity Analysis

The time complexity of deleting a node at a given distance `X` depends on the value of `X` and the size of the linked list. Since the list must be traversed up to the specified distance to locate the node, the number of operations varies based on how far the node is from the beginning.

### Best case

The best case occurs when `X` is equal to 0. In this case, the function must delete the first node of the list. This process takes **constant** time, regardless of the linked list's size.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: "X=0 (victim)"
  next
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "·"
  next: "null"
}
cost: |md
  **0 walking hops**

  **1 pointer update**

  `O(1)`
| {style.fill: "#dcfce7"; style.stroke: "#16a34a"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n1.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Best case — <code>X = 0</code>, victim is the head. No traversal; constant time.</strong></p>

### Worst case

On the other hand, the worst case occurs when `X` is one less than the size of the list. In this case, the function must delete the last node of the list. This process takes linear time proportional to the length of the linked list, i.e., **O(N)**.

```d2
direction: right
h: head {shape: oval}
n1: {
  value: "·"
  next
}
n2: {
  value: "·"
  next
}
n3: {
  value: "·"
  next
}
n4: {
  value: "X=n−1 (victim — tail)"
  next: "null"
  style.fill: "#fde68a"
  style.stroke: "#d97706"
}
cost: |md
  **n−1 walking hops**

  **1 pointer update**

  `O(n)`
| {style.fill: "#fee2e2"; style.stroke: "#dc2626"}
h -> n1.value
n1.next -> n2.value
n2.next -> n3.value
n3.next -> n4.value
n4.value -> cost: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Worst case — <code>X = n − 1</code>, victim is the tail. Full walk to reach its predecessor. Linear time.</strong></p>

The function's space complexity is constant, as it only creates a few variables that take up a fixed amount of space regardless of the size of the linked list.

> **Best Case** - When X = 0
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(1)**
>
> **Worst Case** - When X = length of the list - 1
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Delete node at given distance

## Problem Statement

Given the **head** of a singly linked list and distance **X**, write a function to delete the node at a distance **X** from the start of the linked list and return the head of the updated list.

### Example

> -   **Input:** head = \[5, 7, 3, 10\], X = 1
> -   **Output:** \[5, 3, 10\]

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
    def delete_node_at_given_distance(
        self, head: Optional[ListNode], x: int
    ) -> Optional[ListNode]:

        # If the head is None (empty list), return None
        if head is None:
            return None

        # If x is 0, delete the head node
        if x == 0:
            node_to_be_deleted: ListNode = head

            # Update the head to the next node
            head = head.next

            # Delete the original head node
            del node_to_be_deleted

            # Return the updated head
            return head

        counter: int = 0
        current: Optional[ListNode] = head

        # Traverse to the node at position x - 1
        while current is not None and counter < x - 1:

            # Move to the next node
            current = current.next

            # Increment the counter
            counter += 1

        # If the node at position x - 1 is None or the next node is None,
        # return the head
        if current is None or current.next is None:
            return head

        # Store the node to be deleted
        node_to_be_deleted = current.next

        # Update the next pointer of the current node
        current.next = current.next.next

        # Delete the node at position x
        del node_to_be_deleted

        # Return the head
        return head


# Example from the problem statement
print(to_list(Solution().delete_node_at_given_distance(from_list([5, 7, 3, 10]), 1)))  # [5, 3, 10]

# Delete head (X = 0)
print(to_list(Solution().delete_node_at_given_distance(from_list([1, 2, 3]), 0)))      # [2, 3]

# Delete last node
print(to_list(Solution().delete_node_at_given_distance(from_list([1, 2, 3]), 2)))      # [1, 2]

# Empty list
print(to_list(Solution().delete_node_at_given_distance(None, 0)))                      # []

# Single-node list, X = 0
print(to_list(Solution().delete_node_at_given_distance(from_list([7]), 0)))            # []

# X out of range — head unchanged
print(to_list(Solution().delete_node_at_given_distance(from_list([1, 2]), 5)))         # [1, 2]

# Two-node list, X = 1
print(to_list(Solution().delete_node_at_given_distance(from_list([1, 2]), 1)))         # [1]
```

```java run viz=linked-list viz-root=head
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
        public ListNode deleteNodeAtGivenDistance(ListNode head, int X) {

            // If the head is null (empty list), return null
            if (head == null) {
                return null;
            }

            // If X is 0, delete the head node
            if (X == 0) {
                ListNode nodeToBeDeleted = head;

                // Update the head to the next node
                head = head.next;

                // Delete the original head node
                nodeToBeDeleted = null;

                // Return the updated head
                return head;
            }

            int counter = 0;
            ListNode current = head;

            // Traverse to the node at position X - 1
            while (current != null && counter < X - 1) {

                // Move to the next node
                current = current.next;

                // Increment the counter
                counter++;
            }

            // If the node at position X - 1 is null or the next node is
            // null, return the head
            if (current == null || current.next == null) {
                return head;
            }

            // Store the node to be deleted
            ListNode nodeToBeDeleted = current.next;

            // Update the next pointer of current node
            current.next = current.next.next;

            // Delete the node at position X
            nodeToBeDeleted = null;

            // Return the head
            return head;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(5, 7, 3, 10), 1)));  // [5, 3, 10]

        // Delete head (X = 0)
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(1, 2, 3), 0)));      // [2, 3]

        // Delete last node
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(1, 2, 3), 2)));      // [1, 2]

        // Empty list
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(null, 0)));                   // []

        // Single-node list, X = 0
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(7), 0)));            // []

        // X out of range — head unchanged
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(1, 2), 5)));         // [1, 2]

        // Two-node list, X = 1
        System.out.println(toList(new Solution().deleteNodeAtGivenDistance(fromList(1, 2), 1)));         // [1]
    }
}
```

</details>
***

# Working Example

Seven deletion variants, one predecessor-hunt pattern. The table below walks the same data — `head → 5 → 7 → 3 → 10` — through each variant and shows where the cost goes.

| Variant | Hunt cost | Splice cost | Total | Result |
|---|---|---|---|---|
| First node | 0 hops (head is given) | `O(1)` | **`O(1)`** | `7 → 3 → 10` |
| Last node | `n − 1` hops (walk to find predecessor) | `O(1)` | **`O(n)`** | `5 → 7 → 3` |
| By given data `7` | 0 to `n` hops (first match) | `O(1)` | **`O(n)` worst** | `5 → 3 → 10` |
| After given node `node(7)` | 0 hops (given is the predecessor) | `O(1)` | **`O(1)`** | `5 → 7 → 10` |
| Before given node `node(7)` | 0 to `n` hops (find predecessor of given) | `O(1)` | **`O(n)` worst** | `7 → 3 → 10` |
| The given node `node(7)` | 0 to `n` hops (find predecessor of given) | `O(1)` | **`O(n)` worst** | `5 → 3 → 10` |
| At distance `X = 2` | `X` hops | `O(1)` | **`O(X)`** | `5 → 7 → 10` |

Every row splices with the same one line — `prev.next = victim.next` — followed by the free (explicit in C/C++, implicit in GC languages). The cost is always in the *hunt*, never in the *splice*.

So the core insight is: **whenever a linked-list problem hands you the predecessor, the delete is `O(1)` time. Whenever it hands you the victim (or a value, or a position), you pay `O(n)` time to walk back to the predecessor — because singly linked nodes cannot look backward.**

This is why doubly linked lists feel magical: every node already has a `prev` pointer, so predecessor lookup drops to `O(1)` time everywhere. For the cost of one extra pointer per node, every deletion in this lesson becomes constant time.

> **Transfer Challenge:** Your list has 1 million nodes and you receive a sequence of 1 million `delete-by-value` operations. Naïve per-call cost is `O(n)` time; total is `O(n²)` = 10¹² operations. Can you preprocess the list once to bring the total to `O(n)` time or better?
>
> <details><summary><strong>Answer</strong></summary>
>
> Yes — use a hash map. One `O(n)` pass builds `value → (node, predecessor)` entries. Each subsequent deletion is now `O(1)` time: look up the pair, splice, update the map. Total: `O(n)` preprocess + `O(n)` deletes = **`O(n)` time**. The catch: when you splice out a node, update the map entry for its successor (its predecessor just changed) — still `O(1)` time per op.
>
> </details>

***

# Edge Cases and Pitfalls

The splice is short, which makes the edge cases sneaky — most deletion bugs land on the boundary between "list of zero" and "list of one", or on the head-vs-mid asymmetry. Keep this list open when you write any of the seven variants.

- **Empty list.** `head == null` is a separate code path for every variant. *Delete first*, *delete last*, and *delete by data* return `null` unchanged; *delete after/before/the-given-node* return `null` when `node` is also `null`; *delete at distance* returns `null`. Skip the empty-list check and you'll dereference `null` on the first line.
- **Single-node list.** The lone node *is* the head and *is* the tail. *Delete first*, *delete last*, *delete by data* (when the value matches), *delete the given node*, and *delete at distance 0* all collapse to "`head = null` and free the node" — but via different code paths. Verify each branch is tested.
- **Head deletion changes the return value.** *Delete first*, *delete by data* (when head matches), *delete the given node* (when `node == head`), and *delete at distance 0* all change which node is the head. If the function returns the old `head` reference, the caller still points at the deleted node. Always return the (possibly new) head.
- **Value not found / node not in list.** *Delete by data* with no match must traverse to the end and return the list unchanged — not crash on `cur == null`. *Delete the given node* / *delete before given node* with a `node` that isn't in the list must also fall through cleanly.
- **Tail deletion needs the predecessor, not the tail.** The natural stopping condition is `cur.next.next == null` — not `cur.next == null`. Walking one step too far drops the tail's predecessor and breaks the splice. A singleton (`head.next == null`) is the edge that this condition mis-handles — special-case it.
- **`node` argument is `null` (after/before/the-given-node).** A `null` reference means there is nothing to delete next to or at. The function should return the head unchanged, not crash on `node.next`.
- **Use-after-free on the victim.** In C/C++, code that reads `victim.next` *after* calling `free(victim)` reads a dangling pointer. Rule: splice first (`prev.next = victim.next`), free second (`free(victim)`). Garbage-collected languages dodge this — but the discipline transfers cleanly.
- **Sentinel/dummy head changes the boundary.** Using `dummy.next = head` removes the head special case, but then *delete by data* must return `dummy.next` rather than `head` — forgetting to "unwrap" the sentinel returns a permanently leaked node.
- **Deleting the only occurrence vs all occurrences.** *Delete node with given data* deletes the **first** match and returns; *delete nodes with given data* (plural) walks the whole list. Confusing the two breaks correctness silently when duplicates exist.
- **Off-by-one on distance.** *Delete at distance `X = 0`* deletes the head; `X = n − 1` deletes the tail; `X ≥ n` is out of range and returns the list unchanged. The walk needs `X − 1` steps to land `prev` at the predecessor — one fewer than the distance to the victim.

***

# Production Reality

Linked-list deletion looks academic until you notice how many real systems pick this structure precisely because `O(1)` splice at a known reference point keeps the hot path off the allocator and out of `memmove`.

**[Linux kernel `list_head`]** — uses **a doubly linked intrusive list with `list_del` doing four pointer writes** — because every subsystem holding an object reference (scheduler, VFS, network) can splice the object out in `O(1)` time without a separate container traversal.

The kernel's circular doubly linked list is embedded directly into each struct via a `list_head` field. `list_del` updates the neighbours' pointers in `O(1)` time — no walk, no allocator interaction. That's exactly what an interrupt-context path needs. Source: [include/linux/list.h](https://github.com/torvalds/linux/blob/master/include/linux/list.h).

**[Java's `LinkedList.remove(Object)`]** — uses **a doubly linked list, walks from head or tail (whichever is closer) to find the node** — because the API accepts a value and must search; once found, the `O(1)` splice uses both `prev` and `next` pointers.

`java.util.LinkedList` walks bidirectionally to halve the average search cost (`n / 2` worst case), then runs an `O(1)` splice. The same class's `removeFirst` / `removeLast` are pure `O(1)` time — the head and tail pointers eliminate the hunt. Source: [LinkedList.java](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/LinkedList.java).

**[Redis `listDelNode`]** — uses **a doubly linked list with cached `head`, `tail`, and `len`** — because pub/sub queues, the slow log, and `LREM` need predictable per-operation cost; the cached endpoints make removal `O(1)` time once the node is held.

Redis `LPOP`, `RPOP`, and `LREM` (after the walk) all bottom out in `listDelNode`, which is a four-pointer-write splice plus a `zfree`. Source: [adlist.c `listDelNode`](https://github.com/redis/redis/blob/unstable/src/adlist.c).

**[Python's `collections.OrderedDict.popitem`]** — uses **a doubly linked list of dictionary entries** — because LRU-cache eviction must remove from either end in `O(1)` time regardless of which key is being evicted.

The underlying hash table gives `O(1)` lookup; the linked list threaded through the entries gives `O(1)` removal from either end. `functools.lru_cache` and any user-built LRU rely on this property. Source: [odictobject.c](https://github.com/python/cpython/blob/main/Objects/odictobject.c) <!-- VERIFY: confirm odictobject.c is the canonical source file for OrderedDict in current CPython -->.

**[Memory allocators — free lists]** — uses **a singly linked list of free blocks indexed by size class** — because freeing memory at the front of the list is two writes and allocating is one read; both stay `O(1)` time without traversal.

`jemalloc`, `tcmalloc`, and most small-object allocators carve memory into size classes and keep each class's free blocks on a singly linked stack. `free` pushes a block onto the head; allocation pops the head. Deletion is `O(1)` time at a known location — no walk. Source: [jemalloc internals](https://github.com/jemalloc/jemalloc/blob/dev/INSTALL.md).

**[Garbage collectors — remembered sets and dead-object lists]** — uses **intrusive singly linked lists threaded through object headers** — because the GC enumerates and removes objects at the rate of allocation; `O(1)` time removal at the front of the list keeps the GC's marking and sweeping phases off the allocator's critical path.

Mark-sweep and concurrent collectors maintain per-thread or per-region linked lists of candidate objects; sweeping unlinks dead entries in `O(1)` time per node. Source: [HotSpot G1 source](https://github.com/openjdk/jdk/tree/master/src/hotspot/share/gc/g1) <!-- VERIFY: G1 uses linked-list remembered-set structures specifically; confirm against current GC literature -->.

***

# Practice Ladder

Five problems, easiest first. Try each unaided; hit the hint only after ten minutes stuck; don't peek at the solution until you've made the splice *do something* in code.

| # | Problem | Pattern | Difficulty | Hint |
|---|---------|---------|------------|------|
| 1 | [Trim Nth Node](./09-pattern-sliding-window-traversal/02-problems/02-trim-nth-node.md) | [Sliding Window Traversal](./09-pattern-sliding-window-traversal/01-pattern.md) | Easy | Walk two pointers `n` apart; when the leader hits the tail, the follower points at the predecessor of the node to delete. Then it's a one-line splice. |
| 2 | [Reverse a List](./07-pattern-reversal/02-problems/01-reverse-a-list.md) | [Reversal](./07-pattern-reversal/01-pattern.md) | Easy | Reversal is *iterated deletion + re-insertion at the head* — each step "deletes" the current node's forward link and re-wires it backward. Same three-pointer dance as deletion, applied `n` times. |
| 3 | [Middle Node Search](./10-pattern-fast-and-slow-pointers/02-problems/01-middle-node-search.md) | [Fast and Slow Pointers](./10-pattern-fast-and-slow-pointers/01-pattern.md) | Easy | Slow advances one step per loop; fast advances two. When fast hits the end, slow sits at the middle — useful when a deletion site is defined relative to length. |
| 4 | [Even Odd Split](./11-pattern-split/02-problems/01-even-odd-split.md) | [Split](./11-pattern-split/01-pattern.md) | Easy | Walk once and "delete" each odd node from the main list by splicing it onto a second list — the splice in this lesson is the engine of every list-rebuilding pattern. |
| 5 | [Relocate Node](./13-pattern-reorder/02-problems/01-relocate-node.md) | [Reorder](./13-pattern-reorder/01-pattern.md) | Medium | Detach the source node (delete) and re-insert it at the destination (insert). The reusable trick: hold the predecessor of *both* the source and the destination before touching any `.next`. |

Once these feel automatic, you've internalised every move the sliding-window, reversal, fast/slow, split, and reorder patterns will ask of you — and the splice itself disappears into muscle memory.

***

# Quiz

Test your grip before moving on. One answer per question; reveal only after you have committed to one.

**[Recall] Q: What is the time and space complexity of deleting the first node of a singly linked list?**
`O(1)` time and `O(1)` space — `head = head.next` plus the free, regardless of list length.

**[Recall] Q: For *delete last node* on a list of length `n`, what is the worst-case time complexity, and why?**
`O(n)` time — singly linked lists are forward-only, so finding the tail's predecessor requires walking from the head until `cur.next.next == null`.

**[Reasoning] Q: Why must the splice (`prev.next = victim.next`) run *before* the free (`free(victim)`) in C / C++?**
Because `victim.next` becomes a dangling pointer once `free(victim)` returns; reading it afterwards yields undefined behaviour and the splice writes garbage into `prev.next`.

**[Reasoning] Q: Why is *delete a given node* `O(n)` time on a singly linked list even when the caller already holds a pointer to the node?**
Because the splice needs the *predecessor's* `.next`, not the victim's, and singly linked nodes have no backward pointer — finding the predecessor requires walking from the head.

**[Tradeoff] Q: When does caching a `tail` pointer alongside the head pay off for deletion, and when does it *fail* to help?**
Caching `tail` makes the *delete first* and *traversal-start* operations `O(1)` time with one extra pointer of overhead per list. It still fails to help *delete last* (`O(n)` time) — finding the tail's *predecessor* still requires walking from the head. Only a doubly linked list (or a back-pointer) makes tail deletion `O(1)` time.

***

# Further Reading

Curated paths in, not a syllabus. Read in order of the annotation; come back for the rest when you need depth.

- **[CLRS — Chapter 10: Elementary Data Structures](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/)**
  ★ Essential — the canonical reference for linked-list insertion, deletion, and the sentinel-node trick that removes the head-vs-mid special case.
- **[Sedgewick & Wayne — Algorithms, 4th ed., §1.3 Bags, Queues and Stacks](https://algs4.cs.princeton.edu/13stacks/)**
  ★ Essential — implements linked lists as the storage backing for stack and queue, with diagrams that make the splice visual.
- **[The Linux Kernel Linked Lists API](https://www.kernel.org/doc/html/latest/core-api/kernel-api.html#list-management-functions)**
  ◆ Advanced — the intrusive-list `list_del` pattern: how production C systems get zero-allocation `O(1)` deletion in interrupt context.
- **[Java `LinkedList.unlink` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/LinkedList.java)**
  → Reference — the canonical `unlink` implementation: four pointer writes plus a size decrement, with the head/tail special cases broken out.
- **[Open Data Structures — §3.1 SLList: A Singly Linked List](https://opendatastructures.org/ods-python/3_1_SLList_Singly_Linked_Lis.html)**
  → Reference — a clean academic walk through every deletion variant, with worked examples in Python and Java.

***

# Cross-Links

**Prerequisites**

- [Introduction to Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-introduction-to-singly-linked-lists) — node structure, `head` reference, and why pointers replace contiguous memory.
- [Traversal in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-traversal-in-singly-linked-lists) — the walk that every non-`O(1)` deletion variant piggybacks on; the `prev` / `cur` two-pointer move starts here.
- [Insertion in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-insertion-in-singly-linked-lists) — the mirror operation; the same predecessor-search problem, with a different splice on the wire-up.
- [Introduction to Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-introduction) — the cost model that deletion exposes the gap from (`O(n)` shift) vs (`O(1)` splice).

**What comes next**

- [Detecting Cycle in Singly Linked Lists](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-detecting-cycle-in-singly-linked-lists) — how Floyd's tortoise-and-hare uses pure traversal (no deletion) to expose a structural property the splice cannot.
- [Pattern: Reversal](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-pattern-reversal-pattern) — iterated re-wiring; reversal is "delete the forward link and re-attach backward" in a loop.
- [Pattern: Sliding Window Traversal](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-pattern-sliding-window-traversal-pattern) — the two-pointer walk that finds deletion sites defined by *offset from the tail*.
- [Pattern: Split](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-pattern-split-pattern) — the first pattern that *uses* deletion as a primitive — every split is "delete from the input list, append to one of the output lists".

***

## Final Takeaway

1. **Core mechanic:** every deletion in a singly linked list is "locate the predecessor, run one splice (`prev.next = victim.next`), free the victim" — one pointer write, regardless of which variant.
2. **Dominant tradeoff:** deletion at a known predecessor is `O(1)` time regardless of list size; deletion at any other handle (head-pointer, value, victim-reference, position) pays `O(n)` time to *find* the predecessor — there is no random access to amortise the hunt away.
3. **One thing to remember:** the cost lives in the predecessor hunt, not in the splice — so whenever you can preserve a reference to "the node before", you keep deletion `O(1)`.
