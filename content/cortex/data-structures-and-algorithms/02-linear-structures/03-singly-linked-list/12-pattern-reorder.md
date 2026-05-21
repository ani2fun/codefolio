# 12. Pattern: Reorder

## The Hook

You've now met **split** (lesson 10) and **merge** (lesson 11) as separate patterns. Here's the reveal: they were never really separate. They're the two halves of a single technique called **reorder**, and almost every "rearrange the nodes of a list" problem you'll ever see — reverse alternating, move even-to-back, group by parity, interleave halves, shuffle in zig-zag — is just **split followed by merge** with a problem-specific classifier and selector.

The beauty of this framing is that you've already done the hard work. The split routes nodes into sub-lists based on a classifier `f1`. The merge weaves them back together based on a selector `f2`. Picking `f1` and `f2` is the entire problem; the skeleton is two function calls. Once you see this, "reorder the linked list" stops being a scary problem and becomes a fill-in-the-blanks exercise.

This lesson is the **capstone** of the split-merge-reorder trio. Nail it, and you've mastered the most reusable pattern family in linked lists.

---

## Table of contents

1. [Understanding the reorder pattern](#understanding-the-reorder-pattern)
2. [Identifying the reorder pattern](#identifying-the-reorder-pattern)
3. [Relocate node](#relocate-node)
4. [Parity order](#parity-order)
5. [Value partition](#value-partition)
6. [Shuffle list](#shuffle-list)

***

# Understanding the reorder pattern

Some linked list problems require us to reorder the nodes of the given list in place based on some conditions. In most cases, this requires first splitting the list based on the outcome of some function `f1` and then merging back the split list together either by using another function `f2` or simply concatenating them. These are generally **medium** difficulty problems that require either the split or merge technique we learned earlier or both. Many such problems may also require using other techniques, such as the reversal or fast and slow pointer technique.

```mermaid
flowchart LR
    ORIG["Original<br/>1 → 2 → 3 → 4 → 5 → 6"] -->|"SPLIT f1"| PARTS["List A: 1 → 2 → 3<br/>List B: 6 → 5 → 4"]
    PARTS -->|"MERGE f2"| FINAL["Reordered<br/>1 → 6 → 2 → 5 → 3 → 4"]
```

<p align="center"><strong>Every reorder decomposes into <strong>split</strong> (lesson 10) + <strong>merge</strong> (lesson 11). Split routes nodes into temporary sub-lists; merge weaves them back together in the target order. Two primitives you've already built.</strong></p>

## Reordering technique

Consider that we are given a singly linked list whose nodes must be reordered. The problem almost always has a split function `f1` that we use to split the list into multiple lists using the split technique.

Consider the example execution below, where we use the function `f1` to split the list into two lists such that nodes with even indices go to one list and those with odd indices go to the other list.

```mermaid
flowchart TB
    ORIG["Original: 1 → 2 → 3 → 4 → 5 → 6"]
    F1{"classifier f1"}
    A["List A"]
    B["List B"]
    ORIG --> F1
    F1 -->|"e.g. first half"| A
    F1 -->|"e.g. reversed second half"| B
```

<p align="center"><strong>Step 1 — <strong>split</strong>. A classifier <code>f1</code> routes nodes into temporary sub-lists using the pattern from lesson 10. Every reorder begins here.</strong></p>

In most cases, concatenating these split lists to merge them is sufficient, but sometimes, we may also have a function `f2` that must be used to merge the lists. We use the merge technique to merge them back together to solve the problem.

Consider the example execution below, where we use the function `f2` that merges alternate nodes to merge back the split lists starting with the second list, effectively reordering the nodes.

```mermaid
flowchart TB
    A["List A"]
    B["List B"]
    F2{"selector f2"}
    OUT["Reordered list"]
    A --> F2
    B --> F2
    F2 -->|"(e.g. alternate A, B, A, B)"| OUT
```

<p align="center"><strong>Step 2 — <strong>merge</strong>. A selector <code>f2</code> weaves the sub-lists back into one using the pattern from lesson 11. The combination of <code>f1</code> and <code>f2</code> IS the reorder algorithm.</strong></p>

The reordering technique is simply a combination of the split and merge techniques used in tandem to reorder nodes in the given list.

## Algorithm

The algorithm given below summarizes the reorder technique for **two** lists. It can be easily extended for `k` lists.

> **Algorithm**
>
> -   **Step 1:** Use the split technique to split the list in **two** using the function `f1`
> -   **Step 2:** Use the merge technique to merge the **two** lists using the function `f2`.
> -   **Step 3:** Return the head of the merged list.

## Implementation

Given below is the generic code implementation to split a list in **two** using the function `f1` and then merging them using the function `f2`.


```python run

"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

def reorder_nodes(head: ListNode) -> ListNode:
    # Create dummy nodes and tail references for the two split lists
    dummyA = ListNode(0)
    tailA = dummyA

    dummyB = ListNode(0)
    tailB = dummyB

    # Create current reference to iterate through the list
    current = head

    while current is not None:
        # Use the function `f1` to decide which list this node should go to
        split_first = f1(current)

        if split_first:
            # `current` node goes to the first split list
            tailA.next = current
            tailA = tailA.next  # Move tailA forward
        else:
            # `current` node goes to the second split list
            tailB.next = current
            tailB = tailB.next  # Move tailB forward

        # Move to the next node in the original list
        current = current.next

    # Ensure the two split lists end properly
    tailA.next = None
    tailB.next = None

    # Move ahead dummy nodes of split lists to hold the real head
    currentA = dummyA.next
    currentB = dummyB.next

    # Create dummy node and tail reference for the merged list
    dummy = ListNode(0)
    tail = dummy

    while currentA is not None and currentB is not None:
        # Use the function `f2` to determine which node to merge
        mergeA = f2(currentA, currentB)

        if mergeA:
            tail.next = currentA  # Merge node from currentA
            currentA = currentA.next  # Move currentA forward
        else:
            tail.next = currentB  # Merge node from currentB
            currentB = currentB.next  # Move currentB forward

        # Move tail forward to the merged node
        tail = tail.next

    # If currentA is not completely traversed, attach remaining nodes
    if currentA is not None:
        tail.next = currentA

    # If currentB is not completely traversed, attach remaining nodes
    if currentB is not None:
        tail.next = currentB

    # Capture the merged list's head
    new_head = dummy.next

    return new_head
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

class ReorderNodes {
    // Function to reorder nodes based on conditions defined by f1 and f2
    public ListNode reorderNodes(ListNode head) {
        // Create dummy nodes and tail references for the two split lists
        ListNode dummyA = new ListNode(0);
        ListNode tailA = dummyA;

        ListNode dummyB = new ListNode(0);
        ListNode tailB = dummyB;

        // Create current reference to iterate through the list
        ListNode current = head;

        while (current != null) {
            // Use the function `f1` to decide which list this node should go to
            boolean splitFirst = f1(current);

            if (splitFirst) {
                // `current` node goes to the first split list
                tailA.next = current;
                tailA = tailA.next; // Move tailA forward
            } else {
                // `current` node goes to the second split list
                tailB.next = current;
                tailB = tailB.next; // Move tailB forward
            }

            // Move to the next node in the original list
            current = current.next;
        }

        // Ensure the two split lists end properly
        tailA.next = null;
        tailB.next = null;

        // Move ahead dummy nodes of split lists to hold the real head
        ListNode currentA = dummyA.next;
        ListNode currentB = dummyB.next;

        // Dummy nodes are not needed anymore, no delete operation in Java

        // Create dummy node and tail reference for the merged list
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        while (currentA != null && currentB != null) {
            // Use the function `f2` to determine which node to merge
            boolean mergeA = f2(currentA, currentB);

            if (mergeA) {
                tail.next = currentA; // Merge node from currentA
                currentA = currentA.next; // Move currentA forward
            } else {
                tail.next = currentB; // Merge node from currentB
                currentB = currentB.next; // Move currentB forward
            }

            // Move tail forward to the merged node
            tail = tail.next;
        }

        // If currentA is not completely traversed, attach remaining nodes
        if (currentA != null) {
            tail.next = currentA;
        }

        // If currentB is not completely traversed, attach remaining nodes
        if (currentB != null) {
            tail.next = currentB;
        }

        // Capture the merged list's head
        ListNode newHead = dummy.next;

        return newHead;
    }

    // Placeholder for the function `f1`, which decides how to split the nodes
    private boolean f1(ListNode node) {
        // Implement your condition for splitting nodes
        return node.val % 2 == 0; // Example: send even-valued nodes to list A
    }

    // Placeholder for the function `f2`, which decides how to merge nodes
    private boolean f2(ListNode a, ListNode b) {
        // Implement your condition for merging nodes
        return a.val <= b.val; // Example: merge nodes in ascending order
    }
}
```


## Complexity Analysis

The runtime and space complexity for the reorder technique that splits the list into **two** lists is pretty easy to understand. We traverse the entire list to split it that has a linear **O(N)** runtime complexity. If we only need to concatenate the split lists to merge them, it takes constant **O(1)** time; otherwise, we may need to traverse both split lists completely in the worst case, which has a linear, total **O(N)** runtime complexity. We traverse the entire list to split it in any case, and so the runtime complexity in any case is **O(N)**.

When we reorder a list by splitting it into two, we only create two dummy nodes and update references, so the space complexity is constant, **O(1)**, in any case.

> **Best Case:**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**
>
> **Worst Case:**
>
> -   Space Complexity - **O(1)**
> -   Time Complexity - **O(N)**

***

# Identifying the reorder pattern

The linked list problems that require reordering in place in the list are the only problems that can be solved using the reorder technique. These are generally **medium** problems where we split the list using some function and then merge them back together using another function. Many such problems also have smaller subproblems that require other techniques like reversal or fast and slow pointers to find the middle. If the problem statement or its solution follows the generic template below, it can be solved by applying the split list technique.

**Template:**

Given a linked list, reorder its nodes.

## Example

Let's consider the following problem as an example to better understand how to identify and solve a problem using the reorder technique.

> **Problem statement:** Given a singly linked list, reorder its nodes so all nodes at even indices come after the nodes at odd indices. The indices start with 1.

```d3 widget=linked-list
{
  "title": "Odd-even index reorder — odd-indexed nodes first, then even-indexed",
  "direction": "single",
  "nodes": [
    {"id": "n0", "value": "1"},
    {"id": "n1", "value": "2"},
    {"id": "n2", "value": "3"},
    {"id": "n3", "value": "4"},
    {"id": "n4", "value": "5"},
    {"id": "n5", "value": "6"}
  ],
  "head": "n0",
  "steps": [
    {
      "links": [["n0","n1"],["n1","n2"],["n2","n3"],["n3","n4"],["n4","n5"]],
      "markers": [{"name": "head", "nodeId": "n0"}],
      "msg": "Before: indices 0..5 with values 1..6"
    },
    {
      "nodes": [
        {"id": "n1", "value": "2"},
        {"id": "n3", "value": "4"},
        {"id": "n5", "value": "6"},
        {"id": "n0", "value": "1"},
        {"id": "n2", "value": "3"},
        {"id": "n4", "value": "5"}
      ],
      "links": [["n1","n3"],["n3","n5"],["n5","n0"],["n0","n2"],["n2","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "After: odd-indexed (2, 4, 6) first, then even-indexed (1, 3, 5)"
    }
  ]
}
```

<p align="center"><strong>Odd-even reorder — example target shape. All nodes at odd indices ([1], [3], [5]) come before all nodes at even indices ([0], [2], [4]). A clean split-then-concatenate case.</strong></p>

### Reorder technique solution

We need to reorder the nodes in the given list, and this fits the generic template from the reorder pattern we learned earlier.

**Template:**

Given a linked list, reorder its nodes.

To reorder the nodes, we use the split technique to split the given linked list into two such that the first and second split lists have all the nodes with odd and even indices nodes, respectively.  We create two dummy nodes `dummyA`, `dummyB` and tail references `tailA` and `tailB` and initialize them with the respective dummy nodes. We initialize a counter variable `index` with 0 and iterate the list from start to end using `current` which is initialized with the head of the list.

In each iteration, we check if the `index` is odd or even and add the `current` node to the end of the correct list using one of the tail references. We then increment `index` and move ahead to repeat the process for the next iteration.

```mermaid
flowchart TB
    IN["1 → 2 → 3 → 4 → 5 → 6"]
    ODD["Odd-indexed: 2 → 4 → 6"]
    EVEN["Even-indexed: 1 → 3 → 5"]
    IN -->|"f1: index % 2"| ODD
    IN -->|"f1: index % 2"| EVEN
```

<p align="center"><strong>Split step — classifier <code>f1(node, index) = index % 2</code> routes odd-indexed nodes into one bucket, even-indexed into another. Same mechanics as the split pattern (lesson 10).</strong></p>

We don't need to use the merge technique to merge the lists, as we can concatenate them in this case. We use the tail and dummy references from the split technique to concatenate them by updating references.

```mermaid
flowchart LR
    ODD["Odd-indexed: 2 → 4 → 6"]
    EVEN["Even-indexed: 1 → 3 → 5"]
    CONCAT["odd_tail.next = even_head"]
    OUT["2 → 4 → 6 → 1 → 3 → 5"]
    ODD --> CONCAT
    EVEN --> CONCAT
    CONCAT --> OUT
```

<p align="center"><strong>Merge step — the simplest selector <code>f2</code>: concatenate. Append the entire second list after the first by setting <code>odd_tail.next = even_head</code>. One pointer update.</strong></p>

The implementation of the split list solution is given as follows.


```python run
"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

from typing import Optional, List

class Solution:
    def split_by_parity(
        self, head: Optional[ListNode]
    ) -> List[Optional[ListNode]]:

        # Initialize head and tail references for the two split lists
        odd_dummy = ListNode(0)
        odd_tail = odd_dummy

        even_dummy = ListNode(0)
        even_tail = even_dummy

        # Create current reference to iterate through the list
        current = head

        # To track alternate positions
        counter = 1

        # Iterate through the list and split nodes into two lists
        while current is not None:

            # If the counter is odd then the node goes to the odd list
            if counter % 2 == 1:

                # `current` node goes to the odd split list
                odd_tail.next = current

                # Move odd_tail forward
                odd_tail = odd_tail.next

            # Otherwise, the node goes to the even list
            else:

                # `current` node goes to the even split list
                even_tail.next = current

                # Move even_tail forward
                even_tail = even_tail.next

            # Move to the next node in the original list
            current = current.next
            counter += 1

        # Terminate the odd list
        odd_tail.next = None

        # Terminate the even list
        even_tail.next = None

        return [odd_dummy.next, even_dummy.next]

    def merge_odd_and_even_lists(
        self, odd_head: Optional[ListNode], even_head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the odd list is empty return the even list
        if odd_head is None:
            return even_head

        # If the even list is empty return the odd list
        if even_head is None:
            return odd_head

        # Traverse to the end of the odd list
        current = odd_head
        while current is not None and current.next is not None:
            current = current.next

        # Connect the even list at the end of the odd list
        current.next = even_head
        return odd_head

    def even_odd_list(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty or contains only one node, no splitting is
        # necessary
        if head is None or head.next is None:
            return head

        # Split the list odd and even lists
        odd_head, even_head = self.split_by_parity(head)

        # Append the even list at the end of the odd list and return
        # the head of the merged list
        return self.merge_odd_and_even_lists(odd_head, even_head)
```

```java run
import java.util.*;

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
    public List<ListNode> splitByParity(ListNode head) {

        // Initialize head and tail references for the two split lists
        ListNode oddDummy = new ListNode(0);
        ListNode oddTail = oddDummy;

        ListNode evenDummy = new ListNode(0);
        ListNode evenTail = evenDummy;

        // Create current reference to iterate through the list
        ListNode current = head;

        // To track alternate positions
        int counter = 1;

        // Iterate through the list and split nodes into two lists
        while (current != null) {

            // If the counter is odd then the node goes to the odd list
            if (counter % 2 == 1) {

                // `current` node goes to the odd split list
                oddTail.next = current;

                // Move oddTail forward
                oddTail = oddTail.next;
            }

            // Otherwise, the node goes to the even list
            else {

                // `current` node goes to the even split list
                evenTail.next = current;

                // Move evenTail forward
                evenTail = evenTail.next;
            }

            // Move to the next node in the original list
            current = current.next;
            counter++;
        }

        // Terminate the odd list
        oddTail.next = null;

        // Terminate the even list
        evenTail.next = null;

        return Arrays.asList(oddDummy.next, evenDummy.next);
    }

    public ListNode mergeOddAndEvenLists(
        ListNode oddHead,
        ListNode evenHead
    ) {

        // If the odd list is empty return the even list
        if (oddHead == null) {
            return evenHead;
        }

        // If the even list is empty return the odd list
        if (evenHead == null) {
            return oddHead;
        }

        // Traverse to the end of the odd list
        ListNode current = oddHead;
        while (current != null && current.next != null) {
            current = current.next;
        }

        // Connect the even list at the end of the odd list
        current.next = evenHead;
        return oddHead;
    }

    public ListNode evenOddList(ListNode head) {

        // If the list is empty or contains only one node, no splitting
        // is necessary
        if (head == null || head.next == null) {
            return head;
        }

        // Split the list odd and even lists
        List<ListNode> heads = splitByParity(head);
        ListNode oddHead = heads.get(0);
        ListNode evenHead = heads.get(1);

        // Append the even list at the end of the odd list and return
        // the head of the merged list
        return mergeOddAndEvenLists(oddHead, evenHead);
    }
}
```


The above implementation uses the split list technique to split the list into two lists and merge them together by concatenating them.

## Example problems

Most problems that fall under this category are**medium**problems and can be solved by splitting and then contacting the split list. Sometimes, using the merge technique may be required for merging the split using some function, and often, other techniques like reversal or fast and slow pointer techniques may be needed to solve some subproblems. A list of a few problems is given below.

> -   **[Relocate node](#relocate-node)**
> -   **[Parity order](#parity-order)**
> -   **[Value partition](#value-partition)**
> -   **[Shuffle list](#shuffle-list)**

We will now solve these problems to understand the reorder technique better.

***

# Relocate node

## Problem Statement

Given the **head** of a singly linked list, write a function to move the last node of the list to the start and return the headof the reordered list.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10, 6, 8\]
> -   **Output:** \[8, 5, 7, 3, 10, 6\]
> -   **Explanation:** The last node with the value 8 is moved to the start of the list.

### Example 2

> -   **Input:** head = \[5, 7\]
> -   **Output:** \[7, 5\]
> -   **Explanation:** The last node with the value 7 is moved to the start of the list.

### Example 3

> -   **Input:** head = \[5\]
> -   **Output:** \[5\]
> -   **Explanation:** There is nothing to move as the list only has one node, which is both the first and the last node at the same time.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import Optional, Tuple


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
    def split_last_node(
        self, head: ListNode
    ) -> Tuple[ListNode, ListNode]:
        current = head
        previous = None

        # Traverse the list until the last node is reached
        while current.next is not None:

            # Keep track of the previous node
            previous = current

            # Move to the next node
            current = current.next

        # Disconnect the last node
        if previous is not None:
            previous.next = None

        # Return {head of remaining list, last node}
        return head, current

    def merge_last_node(
        self,
        last_node: Optional[ListNode],
        first_node: Optional[ListNode],
    ) -> Optional[ListNode]:

        # If there is no last node, return the first node
        if not last_node:
            return first_node

        # Connect the last node to the first node
        last_node.next = first_node
        return last_node

    def relocate_node(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty or contains only one node, no need to
        # modify it
        if not head or not head.next:
            return head

        # Split the last node from the list
        first_node, last_node = self.split_last_node(head)

        # Merge the last node at the front
        return self.merge_last_node(last_node, first_node)


# Examples from the problem statement
print(to_list(Solution().relocate_node(from_list([5, 7, 3, 10, 6, 8]))))  # [8, 5, 7, 3, 10, 6]
print(to_list(Solution().relocate_node(from_list([5, 7]))))                # [7, 5]
print(to_list(Solution().relocate_node(from_list([5]))))                   # [5]

# Edge cases
print(to_list(Solution().relocate_node(None)))                             # []
print(to_list(Solution().relocate_node(from_list([1, 2, 3]))))             # [3, 1, 2]
print(to_list(Solution().relocate_node(from_list([9, 9, 9, 9]))))          # [9, 9, 9, 9]  (all same)
print(to_list(Solution().relocate_node(from_list([1, 2, 3, 4, 5]))))       # [5, 1, 2, 3, 4]
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

    static java.util.List<Integer> toList(ListNode head) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        private List<ListNode> splitLastNode(ListNode head) {
            ListNode current = head;
            ListNode previous = null;

            // Traverse the list until the last node is reached
            while (current.next != null) {

                // Keep track of the previous node
                previous = current;

                // Move to the next node
                current = current.next;
            }

            // Disconnect the last node
            if (previous != null) {
                previous.next = null;
            }

            // Return {head of remaining list, last node}
            return Arrays.asList(head, current);
        }

        private ListNode mergeLastNode(ListNode lastNode, ListNode firstNode) {

            // If there is no last node, return the first node
            if (lastNode == null) {
                return firstNode;
            }

            // Connect the last node to the first node
            lastNode.next = firstNode;
            return lastNode;
        }

        public ListNode relocateNode(ListNode head) {

            // If the list is empty or contains only one node, no need to
            // modify it
            if (head == null || head.next == null) {
                return head;
            }

            // Split the last node from the list
            List<ListNode> heads = splitLastNode(head);
            ListNode firstNode = heads.get(0);
            ListNode lastNode = heads.get(1);

            // Merge the last node at the front
            return mergeLastNode(lastNode, firstNode);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().relocateNode(fromList(5, 7, 3, 10, 6, 8))));  // [8, 5, 7, 3, 10, 6]
        System.out.println(toList(new Solution().relocateNode(fromList(5, 7))));                // [7, 5]
        System.out.println(toList(new Solution().relocateNode(fromList(5))));                   // [5]

        // Edge cases
        System.out.println(toList(new Solution().relocateNode(null)));                          // []
        System.out.println(toList(new Solution().relocateNode(fromList(1, 2, 3))));             // [3, 1, 2]
        System.out.println(toList(new Solution().relocateNode(fromList(9, 9, 9, 9))));          // [9, 9, 9, 9]  (all same)
        System.out.println(toList(new Solution().relocateNode(fromList(1, 2, 3, 4, 5))));       // [5, 1, 2, 3, 4]
    }
}
```

</details>


***

# Parity order

## Problem Statement

Given the **head** of a singly linked list, write a function to group all the nodes that appear on odd indices together, followed by the nodes that appear on even indices, and return the head of the reordered list.  

The indices start with `1`.

### Example 1

> -   **Input:** head = \[2, 1, 3, 4, 8\]
> -   **Output:** \[2, 3, 8, 1, 4\]
> -   **Explanation:** After grouping the nodes at odd indices followed by even indices, the list becomes \[2, 3, 8, 1, 4\].

### Example 2

> -   **Input:** head = \[\]
> -   **Output:** \[\]
> -   **Explanation:** Since the input list is empty, the output list will also be empty.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import Optional, List


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
    def split_by_parity(
        self, head: Optional[ListNode]
    ) -> List[Optional[ListNode]]:

        # Initialize head and tail references for the two split lists
        odd_dummy = ListNode(0)
        odd_tail = odd_dummy

        even_dummy = ListNode(0)
        even_tail = even_dummy

        # Create current reference to iterate through the list
        current = head

        # To track alternate positions
        counter = 1

        # Iterate through the list and split nodes into two lists
        while current is not None:

            # If the counter is odd then the node goes to the odd list
            if counter % 2 == 1:

                # `current` node goes to the odd split list
                odd_tail.next = current

                # Move odd_tail forward
                odd_tail = odd_tail.next

            # Otherwise, the node goes to the even list
            else:

                # `current` node goes to the even split list
                even_tail.next = current

                # Move even_tail forward
                even_tail = even_tail.next

            # Move to the next node in the original list
            current = current.next
            counter += 1

        # Terminate the odd list
        odd_tail.next = None

        # Terminate the even list
        even_tail.next = None

        return [odd_dummy.next, even_dummy.next]

    def merge_odd_and_even_lists(
        self, odd_head: Optional[ListNode], even_head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the odd list is empty return the even list
        if odd_head is None:
            return even_head

        # If the even list is empty return the odd list
        if even_head is None:
            return odd_head

        # Traverse to the end of the odd list
        current = odd_head
        while current is not None and current.next is not None:
            current = current.next

        # Connect the even list at the end of the odd list
        current.next = even_head
        return odd_head

    def parity_order(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty or contains only one node, no splitting is
        # necessary
        if head is None or head.next is None:
            return head

        # Split the list odd and even lists
        odd_head, even_head = self.split_by_parity(head)

        # Append the even list at the end of the odd list and return
        # the head of the merged list
        return self.merge_odd_and_even_lists(odd_head, even_head)


# Examples from the problem statement
print(to_list(Solution().parity_order(from_list([2, 1, 3, 4, 8]))))        # [2, 3, 8, 1, 4]
print(to_list(Solution().parity_order(None)))                               # []

# Edge cases
print(to_list(Solution().parity_order(from_list([1]))))                     # [1]
print(to_list(Solution().parity_order(from_list([1, 2]))))                  # [1, 2]
print(to_list(Solution().parity_order(from_list([1, 2, 3]))))               # [1, 3, 2]
print(to_list(Solution().parity_order(from_list([1, 2, 3, 4]))))            # [1, 3, 2, 4]
print(to_list(Solution().parity_order(from_list([5, 5, 5, 5, 5]))))         # [5, 5, 5, 5, 5]  (all same)
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

    static java.util.List<Integer> toList(ListNode head) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        private List<ListNode> splitByParity(ListNode head) {

            // Initialize head and tail references for the two split lists
            ListNode oddDummy = new ListNode(0);
            ListNode oddTail = oddDummy;

            ListNode evenDummy = new ListNode(0);
            ListNode evenTail = evenDummy;

            // Create current reference to iterate through the list
            ListNode current = head;

            // To track alternate positions
            int counter = 1;

            // Iterate through the list and split nodes into two lists
            while (current != null) {

                // If the counter is odd then the node goes to the odd list
                if (counter % 2 == 1) {

                    // `current` node goes to the odd split list
                    oddTail.next = current;

                    // Move oddTail forward
                    oddTail = oddTail.next;
                }

                // Otherwise, the node goes to the even list
                else {

                    // `current` node goes to the even split list
                    evenTail.next = current;

                    // Move evenTail forward
                    evenTail = evenTail.next;
                }

                // Move to the next node in the original list
                current = current.next;
                counter++;
            }

            // Terminate the odd list
            oddTail.next = null;

            // Terminate the even list
            evenTail.next = null;

            return Arrays.asList(oddDummy.next, evenDummy.next);
        }

        private ListNode mergeOddAndEvenLists(
            ListNode oddHead,
            ListNode evenHead
        ) {

            // If the odd list is empty return the even list
            if (oddHead == null) {
                return evenHead;
            }

            // If the even list is empty return the odd list
            if (evenHead == null) {
                return oddHead;
            }

            // Traverse to the end of the odd list
            ListNode current = oddHead;
            while (current != null && current.next != null) {
                current = current.next;
            }

            // Connect the even list at the end of the odd list
            current.next = evenHead;
            return oddHead;
        }

        public ListNode parityOrder(ListNode head) {

            // If the list is empty or contains only one node, no splitting
            // is necessary
            if (head == null || head.next == null) {
                return head;
            }

            // Split the list odd and even lists
            List<ListNode> heads = splitByParity(head);
            ListNode oddHead = heads.get(0);
            ListNode evenHead = heads.get(1);

            // Append the even list at the end of the odd list and return
            // the head of the merged list
            return mergeOddAndEvenLists(oddHead, evenHead);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().parityOrder(fromList(2, 1, 3, 4, 8))));        // [2, 3, 8, 1, 4]
        System.out.println(toList(new Solution().parityOrder(null)));                            // []

        // Edge cases
        System.out.println(toList(new Solution().parityOrder(fromList(1))));                     // [1]
        System.out.println(toList(new Solution().parityOrder(fromList(1, 2))));                  // [1, 2]
        System.out.println(toList(new Solution().parityOrder(fromList(1, 2, 3))));               // [1, 3, 2]
        System.out.println(toList(new Solution().parityOrder(fromList(1, 2, 3, 4))));            // [1, 3, 2, 4]
        System.out.println(toList(new Solution().parityOrder(fromList(5, 5, 5, 5, 5))));         // [5, 5, 5, 5, 5]  (all same)
    }
}
```

</details>


***

# Value partition

## Problem Statement

Given the **head** of a singly linked list and a value **X**, write a function to partition the list such that all nodes less than X come before nodes greater than or equal to X and return the head of the reordered list. The original relative order of the nodes in each of the two partitions should be preserved.

### Example 1

> -   **Input:** head = \[1, 4, 3, 2, 5, 2\], X = 3
> -   **Output:** \[1, 2, 2, 4, 3, 5\]
> -   **Explanation:** Nodes with values 1, 2, and 2 are less than 3. Therefore, they will be placed before the nodes with values greater than or equal to 3.

### Example 2

> -   **Input:** head = \[2, 1\], X = 2
> -   **Output:** \[1, 2\]
> -   **Explanation:** Node with value 1 is less than 2. Therefore, it will be placed before the nodes with values greater than or equal to 2.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import Optional, List


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
    def split_list_by_value(
        self, head: Optional[ListNode], X: int
    ) -> List[Optional[ListNode]]:

        # Create dummy nodes to initialize the heads of two separate
        # lists. List for nodes with values less than X.
        less_head = ListNode(0)
        less_tail = less_head

        # List for nodes with values greater than or equal to X.
        greater_head = ListNode(0)
        greater_tail = greater_head

        # Start traversing the original list from the head.
        current = head

        # Traverse and split nodes based on the value of X.
        while current is not None:

            # If the value of the current node is less than X, it should
            # be appended to the list for nodes < X.
            if current.val < X:

                # Append current node to list for nodes < X.
                less_tail.next = current

                # Move less_tail to the newly added node.
                less_tail = less_tail.next

            # Otherwise, the value of the current node is greater than
            # or equal to X, and it should be appended to the list for
            # nodes >= X.
            else:

                # Append current node to list for nodes >= X.
                greater_tail.next = current

                # Move greater_tail to the newly added node.
                greater_tail = greater_tail.next

            # Proceed to the next node in the original list.
            current = current.next

        # End both lists by setting the next pointers of their tails to
        # None.
        less_tail.next = None
        greater_tail.next = None

        # Return heads of both lists, excluding dummy nodes.
        return [less_head.next, greater_head.next]

    def merge_less_and_greater_lists(
        self,
        less_head: Optional[ListNode],
        greater_head: Optional[ListNode],
    ) -> Optional[ListNode]:

        # If the first list (less_head) is empty, return greater_head as
        # the concatenated list.
        if less_head is None:
            return greater_head

        # If the second list (greater_head) is empty, return less_head as
        # the concatenated list.
        if greater_head is None:
            return less_head

        # Find the end of the first list (less_head) to append
        # greater_head.
        current = less_head
        while current is not None and current.next is not None:
            current = current.next

        # Append greater_head to the end of less_head.
        current.next = greater_head
        return less_head

    def value_partition(
        self, head: Optional[ListNode], X: int
    ) -> Optional[ListNode]:

        # Return the head if the list is empty or has only one node.
        if head is None or head.next is None:
            return head

        # Split the original list into two lists: nodes < X and nodes >=
        # X.
        less_head, greater_head = self.split_list_by_value(head, X)

        # Merge both lists and return the head of the combined list.
        return self.merge_less_and_greater_lists(less_head, greater_head)


# Examples from the problem statement
print(to_list(Solution().value_partition(from_list([1, 4, 3, 2, 5, 2]), 3)))  # [1, 2, 2, 4, 3, 5]
print(to_list(Solution().value_partition(from_list([2, 1]), 2)))               # [1, 2]

# Edge cases
print(to_list(Solution().value_partition(None, 5)))                            # []
print(to_list(Solution().value_partition(from_list([3]), 3)))                  # [3]  (single node >= X)
print(to_list(Solution().value_partition(from_list([1]), 3)))                  # [1]  (single node < X)
print(to_list(Solution().value_partition(from_list([3, 3, 3]), 3)))            # [3, 3, 3]  (all >= X)
print(to_list(Solution().value_partition(from_list([1, 2, 2]), 3)))            # [1, 2, 2]  (all < X)
print(to_list(Solution().value_partition(from_list([5, 1, 3, 2, 4]), 3)))      # [1, 2, 5, 3, 4]
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

    static java.util.List<Integer> toList(ListNode head) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        private List<ListNode> splitListByValue(ListNode head, int X) {

            // Create dummy nodes to initialize the heads of two separate
            // lists. List for nodes with values less than X.
            ListNode lessHead = new ListNode(0);
            ListNode lessTail = lessHead;

            // List for nodes with values greater than or equal to X.
            ListNode greaterHead = new ListNode(0);
            ListNode greaterTail = greaterHead;

            // Start traversing the original list from the head.
            ListNode current = head;

            // Traverse and split nodes based on the value of X.
            while (current != null) {

                // If the value of the current node is less than X, it should
                // be appended to the list for nodes < X.
                if (current.val < X) {

                    // Append current node to list for nodes < X.
                    lessTail.next = current;

                    // Move lessTail to the newly added node.
                    lessTail = lessTail.next;
                }

                // Otherwise, the value of the current node is greater than
                // or equal to X, and it should be appended to the list for
                // nodes >= X.
                else {

                    // Append current node to list for nodes >= X.
                    greaterTail.next = current;

                    // Move greaterTail to the newly added node.
                    greaterTail = greaterTail.next;
                }

                // Proceed to the next node in the original list.
                current = current.next;
            }

            // End both lists by setting the next pointers of their tails to
            // null.
            lessTail.next = null;
            greaterTail.next = null;

            // Return heads of both lists, excluding dummy nodes.
            return Arrays.asList(lessHead.next, greaterHead.next);
        }

        private ListNode mergeLessAndGreaterLists(
            ListNode lessHead,
            ListNode greaterHead
        ) {

            // If the first list (lessHead) is empty, return greaterHead as
            // the concatenated list.
            if (lessHead == null) {
                return greaterHead;
            }

            // If the second list (greaterHead) is empty, return lessHead as
            // the concatenated list.
            if (greaterHead == null) {
                return lessHead;
            }

            // Find the end of the first list (lessHead) to append
            // greaterHead.
            ListNode current = lessHead;
            while (current != null && current.next != null) {
                current = current.next;
            }

            // Append greaterHead to the end of lessHead.
            current.next = greaterHead;
            return lessHead;
        }

        public ListNode valuePartition(ListNode head, int X) {

            // Return the head if the list is empty or has only one node.
            if (head == null || head.next == null) {
                return head;
            }

            // Split the original list into two lists: nodes < X and nodes >=
            // X.
            List<ListNode> heads = splitListByValue(head, X);

            // Head of list with nodes < X.
            ListNode lessHead = heads.get(0);

            // Head of list with nodes >= X.
            ListNode greaterHead = heads.get(1);

            // Merge both lists and return the head of the combined list.
            return mergeLessAndGreaterLists(lessHead, greaterHead);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().valuePartition(fromList(1, 4, 3, 2, 5, 2), 3)));  // [1, 2, 2, 4, 3, 5]
        System.out.println(toList(new Solution().valuePartition(fromList(2, 1), 2)));               // [1, 2]

        // Edge cases
        System.out.println(toList(new Solution().valuePartition(null, 5)));                         // []
        System.out.println(toList(new Solution().valuePartition(fromList(3), 3)));                  // [3]  (single node >= X)
        System.out.println(toList(new Solution().valuePartition(fromList(1), 3)));                  // [1]  (single node < X)
        System.out.println(toList(new Solution().valuePartition(fromList(3, 3, 3), 3)));            // [3, 3, 3]  (all >= X)
        System.out.println(toList(new Solution().valuePartition(fromList(1, 2, 2), 3)));            // [1, 2, 2]  (all < X)
        System.out.println(toList(new Solution().valuePartition(fromList(5, 1, 3, 2, 4), 3)));      // [1, 2, 5, 3, 4]
    }
}
```

</details>


***

# Shuffle list

## Problem Statement

Given the **head** of a singly linked list that can be represented as **L0 -> L1 -> … -> Ln - 1 -> Ln**. Reorder the list **in place** to match the following format: 

**L0 -> Ln -> L1 -> Ln - 1 -> L2 -> Ln - 2 -> …**

### Example 1

> -   **Input:** head = \[1, 2, 3, 4\]
> -   **Output:** \[1, 4, 2, 3\]
> -   **Explanation:** After reordering, the list becomes \[1, 4, 2, 3\].

### Example 2

> -   **Input:** head = \[1, 2, 3, 4, 5\]
> -   **Output:** \[1, 5, 2, 4, 3\]
> -   **Explanation:** After reordering, the list becomes \[1, 5, 2, 4, 3\].

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
    def reverse_list(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = head
        previous: Optional[ListNode] = None

        while current:
            next_node: Optional[ListNode] = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def split_list_in_half(
        self, head: Optional[ListNode]
    ) -> [Optional[ListNode], Optional[ListNode]]:

        # Initialize slow and fast pointers to find the middle of the
        # list
        slow: Optional[ListNode] = head
        fast: Optional[ListNode] = head
        prev_to_slow: Optional[ListNode] = None

        # Move slow by one and fast by two nodes until fast reaches the
        # end
        while fast and fast.next:
            prev_to_slow = slow
            slow = slow.next
            fast = fast.next.next

        # Split for even length list
        if fast is None:
            second_half: Optional[ListNode] = prev_to_slow.next
            prev_to_slow.next = None

        # Split for odd length list
        else:
            second_half: Optional[ListNode] = slow.next
            slow.next = None

        return [head, second_half]

    def merge_alternate_nodes(
        self,
        first_half: Optional[ListNode],
        second_half: Optional[ListNode],
    ) -> Optional[ListNode]:

        # Create a dummy node to form the merged list
        dummy: ListNode = ListNode(0)
        tail: ListNode = dummy

        # Boolean to switch between nodes from each list
        merge_first: bool = True

        # Alternate between the nodes of each list
        while first_half and second_half:
            if merge_first:
                tail.next = first_half
                first_half = first_half.next
            else:
                tail.next = second_half
                second_half = second_half.next
            tail = tail.next
            merge_first = not merge_first

        # Append any remaining nodes from first_half or second_half
        if first_half:
            tail.next = first_half
        elif second_half:
            tail.next = second_half

        return dummy.next

    def shuffle_list(self, head: Optional[ListNode]) -> None:

        # No need to reorder if the list is empty or has only one element
        if not head or not head.next:
            return

        # Split the list into two halves
        first_half: Optional[ListNode]
        second_half: Optional[ListNode]
        first_half, second_half = self.split_list_in_half(head)

        # Reverse the second half of the list
        reversed_second_half: Optional[ListNode] = self.reverse_list(
            second_half
        )

        # Alternatively merge the first list and the reversed second list
        self.merge_alternate_nodes(first_half, reversed_second_half)


# Examples from the problem statement
h1 = from_list([1, 2, 3, 4])
Solution().shuffle_list(h1); print(to_list(h1))       # [1, 4, 2, 3]

h2 = from_list([1, 2, 3, 4, 5])
Solution().shuffle_list(h2); print(to_list(h2))       # [1, 5, 2, 4, 3]

# Edge cases
h3 = None
Solution().shuffle_list(h3); print(to_list(h3))       # []

h4 = from_list([1])
Solution().shuffle_list(h4); print(to_list(h4))       # [1]

h5 = from_list([1, 2])
Solution().shuffle_list(h5); print(to_list(h5))       # [1, 2]

h6 = from_list([1, 2, 3])
Solution().shuffle_list(h6); print(to_list(h6))       # [1, 3, 2]

h7 = from_list([1, 2, 3, 4, 5, 6])
Solution().shuffle_list(h7); print(to_list(h7))       # [1, 6, 2, 5, 3, 4]
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

    static java.util.List<Integer> toList(ListNode head) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        private ListNode reverse(ListNode head) {
            ListNode current = head;
            ListNode previous = null;

            while (current != null) {
                ListNode next = current.next;
                current.next = previous;
                previous = current;
                current = next;
            }

            return previous;
        }

        private List<ListNode> splitListInHalf(ListNode head) {

            // Initialize slow and fast pointers to find the middle of the
            // list
            ListNode slow = head, fast = head;
            ListNode prevToSlow = null;

            // Move slow by one and fast by two nodes until fast reaches the
            // end
            while (fast != null && fast.next != null) {
                prevToSlow = slow;
                slow = slow.next;
                fast = fast.next.next;
            }

            ListNode secondHalf;

            // Split for even length list
            if (fast == null) {
                secondHalf = prevToSlow.next;
                prevToSlow.next = null;
            }

            // Split for odd length list
            else {
                secondHalf = slow.next;
                slow.next = null;
            }

            return Arrays.asList(head, secondHalf);
        }

        private ListNode mergeAlternateNodes(
            ListNode firstHalf,
            ListNode secondHalf
        ) {

            // Create a dummy node to form the merged list
            ListNode dummy = new ListNode(0);
            ListNode tail = dummy;

            // Boolean to switch between nodes from each list
            boolean mergeFirst = true;

            // Alternate between the nodes of each list
            while (firstHalf != null && secondHalf != null) {
                if (mergeFirst) {
                    tail.next = firstHalf;
                    firstHalf = firstHalf.next;
                } else {
                    tail.next = secondHalf;
                    secondHalf = secondHalf.next;
                }
                tail = tail.next;
                mergeFirst = !mergeFirst;
            }

            // Append any remaining nodes from firstHalf or secondHalf
            if (firstHalf != null) {
                tail.next = firstHalf;
            } else if (secondHalf != null) {
                tail.next = secondHalf;
            }

            return dummy.next;
        }

        public void shuffleList(ListNode head) {

            // No need to reorder if the list is empty or has only one
            // element
            if (head == null || head.next == null) {
                return;
            }

            // Split the list into two halves
            List<ListNode> heads = splitListInHalf(head);
            ListNode firstHalf = heads.get(0);
            ListNode secondHalf = heads.get(1);

            // Reverse the second half of the list
            ListNode reversedSecondHalf = reverse(secondHalf);

            // Alternatively merge the first list and the reversed second
            // list
            mergeAlternateNodes(firstHalf, reversedSecondHalf);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        ListNode h1 = fromList(1, 2, 3, 4);
        new Solution().shuffleList(h1);
        System.out.println(toList(h1));       // [1, 4, 2, 3]

        ListNode h2 = fromList(1, 2, 3, 4, 5);
        new Solution().shuffleList(h2);
        System.out.println(toList(h2));       // [1, 5, 2, 4, 3]

        // Edge cases
        ListNode h3 = null;
        new Solution().shuffleList(h3);
        System.out.println(toList(h3));       // []

        ListNode h4 = fromList(1);
        new Solution().shuffleList(h4);
        System.out.println(toList(h4));       // [1]

        ListNode h5 = fromList(1, 2);
        new Solution().shuffleList(h5);
        System.out.println(toList(h5));       // [1, 2]

        ListNode h6 = fromList(1, 2, 3);
        new Solution().shuffleList(h6);
        System.out.println(toList(h6));       // [1, 3, 2]

        ListNode h7 = fromList(1, 2, 3, 4, 5, 6);
        new Solution().shuffleList(h7);
        System.out.println(toList(h7));       // [1, 6, 2, 5, 3, 4]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Reorder is the composition of two patterns you already know:

```
def reorder(head):
    sub_lists = split(head, classifier = f1)   # lesson 10
    return merge(*sub_lists, selector = f2)    # lesson 11
```

Picking `f1` and `f2` specialises the template to any reorder problem:

| Problem | `f1` (split classifier) | `f2` (merge selector) |
|---|---|---|
| Odd/even index split | `i % 2` | concatenate |
| Zig-zag reorder (1st, last, 2nd, 2nd-last, ...) | first half / reversed second half | alternate A, B, A, B |
| Parity partition (odd values first) | `val % 2` | concatenate |
| Value partition (<, ≥ pivot) | `val < pivot` | concatenate |
| Pure shuffle into alternating even-odd | index parity | alternate A, B, A, B |

Four insights worth burning in:

| Insight | Why it matters |
|---|---|
| Reorder = split + merge | Stop inventing bespoke algorithms for each reorder variant. Reuse the two primitives. |
| Classifier + selector are the whole problem | The template is 5 lines. Every variant customises exactly two functions. |
| Reversing a sub-list is a valid `f1` extension | For zig-zag reorder, split the list at the middle and reverse the second half — then the merge selector is plain alternation. |
| O(n) time, O(1) extra space | Every node is visited once in split, once in merge. No allocations beyond a few dummy heads. |

When you next see "rearrange in place", "reorder by pattern", "zig-zag", "partition", "shuffle by index" — reach for `split → merge` first. Pick your `f1` and `f2` and ship it.

> **Transfer Challenge:** Given a linked list `1 → 2 → 3 → 4 → 5 → 6`, produce the zig-zag reorder `1 → 6 → 2 → 5 → 3 → 4`. What are your `f1` and `f2`?
>
> <details><summary><strong>Solution hint</strong></summary>
>
> <strong>f1</strong> — split at the middle (using the fast-and-slow pattern from lesson 9), then <strong>reverse the second half</strong> (using the reversal pattern from lesson 6). You end with two lists: <code>1 → 2 → 3</code> and <code>6 → 5 → 4</code>.<br>
> <strong>f2</strong> — alternate-fuse A, B, A, B (the selector from the merge lesson).<br>
> Result: <code>1 → 6 → 2 → 5 → 3 → 4</code>. This problem alone touches <strong>four</strong> patterns you've learned — reversal, fast-and-slow, split, and merge. That's the power of composing primitives.
>
> </details>

</details>