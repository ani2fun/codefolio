# 10. Pattern: Split

## The Hook

You have a linked list of a million nodes and you need to route them into three output lists based on some rule — say, odd/even/zero, or a hash bucket, or "rank by 1000". The naive plan: walk the list three times, copying the matching nodes into three freshly-allocated lists. **Three passes. O(n) extra memory for all those copies. Off-by-one bugs in the seam logic.**

The split pattern does it in one pass with zero allocations beyond `k` dummy nodes. For each node, compute its bucket, tack it onto that bucket's tail, move on. The nodes themselves don't move in memory — only their `.next` pointers get rewired. At the end, you have `k` cleanly terminated sublists and the original list has been dismantled.

There's one trick that makes this pattern click: the **dummy head sentinel**. Each output list begins with a placeholder node so you never need to distinguish "first node to append" from "subsequent node to append" — every append is just `tail.next = node; tail = node`. That single-trick uniformity kills a whole class of edge cases and turns a gnarly conditional forest into a 10-line loop. Let's build it.

---

## Table of contents

1. [Understanding the split pattern](#understanding-the-split-pattern)
2. [Identifying the split pattern](#identifying-the-split-pattern)
3. [Even odd split](#even-odd-split)
4. [Split alternate groups](#split-alternate-groups)
5. [Split by modulo](#split-by-modulo)
6. [K-way list split](#k-way-list-split)

***

# Understanding the split pattern

Many linked list problems require splitting a given linked list into two or more lists based on the outcome of some function. One solution to this problem is traversing the list for every new list that has to be created and copying items from the original list into the new nodes created for the new lists. However, this requires multiple passes over the list and is inefficient. Also, in many cases, we need to split the original list into separate lists instead of creating copies of nodes. The linked list split technique can be applied to such problems to solve them efficiently in a single pass.

```mermaid
flowchart LR
    IN["Input list<br/>1 → 2 → 3 → 4 → 5 → 6 → 7"] --> F{"classifier f(node)"}
    F -->|"f → bucket 0"| B0["0: 1 → 4 → 7"]
    F -->|"f → bucket 1"| B1["1: 2 → 5"]
    F -->|"f → bucket 2"| B2["2: 3 → 6"]
```

<p align="center"><strong>The split pattern — every node is routed to one of <code>k</code> output lists by a classifier function <code>f</code>. Nothing is copied; the original nodes are re-linked into their destination list.</strong></p>

```d3 widget=linked-list
{
  "title": "Round-robin split into k=3 sub-lists — node i goes to list (i mod k)",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "1"},
    {"id": "n2", "value": "2"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "4"},
    {"id": "n5", "value": "5"},
    {"id": "n6", "value": "6"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"],["n4","n5"],["n5","n6"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Before: original list 1 → 2 → 3 → 4 → 5 → 6"
    },
    {
      "links": [["n1","n4"],["n2","n5"],["n3","n6"]],
      "markers": [{"name": "headA", "nodeId": "n1"}, {"name": "headB", "nodeId": "n2"}, {"name": "headC", "nodeId": "n3"}],
      "msg": "After: list 0 = (1,4); list 1 = (2,5); list 2 = (3,6). Same nodes — re-linked into 3 chains."
    }
  ]
}
```

<p align="center"><strong>Round-robin split — node <em>i</em> goes to list <em>i mod k</em>. Every original node ends up in exactly one sublist; no allocations, just re-linking.</strong></p>

## Linked list split technique

Consider we are given a singly linked list that we need to split into `k` lists using a function `f` that maps every node in the original list to the list it should go to after splitting. the function`f`simply round robins amongst all the`k`lists. The split technique uses dummy nodes to simplify splitting the original lists. We create two arrays of node references `dummy` and `tails` of size `k` each. Both the arrays initialized it with references of newly created dummy nodes where the item at the index `i` is the dummy node for the list `i`.

Consider the example below, where `k = 3`.

```d2
direction: right
d0: |md
  **dummy[0]**

  next: null
|
d1: |md
  **dummy[1]**

  next: null
|
d2: |md
  **dummy[2]**

  next: null
|
t0: "tail[0] → dummy[0]"
t1: "tail[1] → dummy[1]"
t2: "tail[2] → dummy[2]"
d0 -> t0: "" {style.stroke-dash: 3}
d1 -> t1: "" {style.stroke-dash: 3}
d2 -> t2: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Setup for <code>k = 3</code> — allocate <code>k</code> dummy heads and a parallel <code>tail[i]</code> pointer for each. The dummy pattern removes every "is this the first node in the output list?" special case: each tail just appends via <code>tail[i].next = current; tail[i] = current</code>, always.</strong></p>

We initialize a `current` reference with the head of the list and traverse the original list from start to end. In each iteration, we use the function `f` to identify which list the current node should go to. We get the tail node for that list from the `tail` array, update its next section to hold the current node, and update the tail reference. Then, we move `current` one step ahead for the next iteration and finally set the next section of the new tail node to `null`. This process is repeated until we reach the end of the list when the original list is split into `k` lists.

```mermaid
flowchart TB
    INIT["current = head"]
    LOOP["while current is not null:<br/>1. bucket = f(current)<br/>2. tail[bucket].next = current<br/>3. tail[bucket] = current<br/>4. current = current.next"]
    TERM["for each bucket i:<br/>tail[i].next = null<br/>(seal the output list)"]
    HEADS["real heads are dummy[i].next<br/>(skip past the dummies)"]
    INIT --> LOOP --> TERM --> HEADS
```

<p align="center"><strong>Single pass — for each node, compute its bucket, tack it onto that bucket's tail, advance. Afterwards, seal every bucket's tail and extract the real heads from <code>dummy[i].next</code>.</strong></p>

At the end of all iterations, we iterate in `dummy` and move the references one step ahead to hold the real head of the corresponding list and delete the dummy node.

## Algorithm

The algorithm given below summarizes the linked list split technique to split a list into `k` lists.

> **Algorithm**
>
> -   **Step 1:** Create two arrays of node references `dummy` and `tails` of size `k` and initialize each item in both arrays with the reference of a newly created dummy node.
> -   **Step 2:** Create a reference `current` and initialize it with the head of the list.
> -   **Step 3:** Loop while `current` != `null` and do the following:
>     -   **Step 3.1:** Apply the function `f` to the `current` node and retrieve `idx`, which is the index of the list where this node should be placed.
>     -   **Step 3.2:** Add the `current` node to the end of the list stored at `idx` using `tails` array.
>     -   **Step 3.3:** Update `tails\[idx\]` to now store the reference of the new tail node.
>     -   **Step 3.4:** Update the `current` pointer to hold the reference of the node after the `current` node.
>     -   **Step 3.5:** Set the next section of `tails\[idx\]` to `null`
> -   **Step 4:** Move all the dummy nodes one step ahead to obtain the heads of the split lists and delete the old dummy nodes.

## Implementation

Given below is the generic code implementation to split a given linked list into `k` lists based on the outcome of a function `f`. 


```python run

"""
Definition for singly-linked list.
class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
"""

def splitLists(head: ListNode, k: int) -> List[ListNode]:
    # Create an array of references for dummy and tail nodes
    dummy = [ListNode() for _ in range(k)]
    tails = dummy[:]

    # Iterate in the list using current
    current = head
    while current is not None:
        # Use the function `f` to decide which list this node should go to.
        idx = f(current)

        # Add node to the list and update tail
        tails[idx].next = current
        tails[idx] = current

        # Move current ahead
        current = current.next

        # Set the next of the tail node to None
        tails[idx].next = None

    # Remove the dummy nodes and return heads of the split lists
    for i in range(k):
        dummyNode = dummy[i]
        dummy[i] = dummy[i].next  # move head to the actual first node
        del dummyNode  # Python's garbage collection handles memory cleanup

    # Return the array of split lists' heads
    return dummy
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

class SplitLists {
    ListNode[] splitLists(ListNode head, int k) {
        // Create an array of references for dummy and tail nodes
        ListNode[] dummy = new ListNode[k];
        ListNode[] tails = new ListNode[k];

        // Initialize the dummy and tail nodes
        for (int i = 0; i < k; i++) {
            dummy[i] = new ListNode(); // create a new dummy node
            tails[i] = dummy[i]; // point tail to the dummy node
        }

        // Iterate in the list using current
        ListNode current = head;
        while (current != null) {
            // Use the function `f` to decide which list this node should go to.
            int idx = f(current);

            // Add node to the list and update tail
            tails[idx].next = current;
            tails[idx] = current;

            // Move current ahead
            current = current.next;

            // Set the next of the current tail node to null
            tails[idx].next = null;
        }

        // Remove the dummy nodes and assign actual heads to the dummy array
        for (int i = 0; i < k; i++) {
            ListNode dummyNode = dummy[i];
            dummy[i] = dummy[i].next;  // move the dummy head to the real first node
            dummyNode = null;          // delete the dummy node
        }

        // Return the array of split lists' heads
        return dummy;
    }
}
```


## Complexity Analysis

Looking at the algorithm, the runtime and space complexity are pretty easy to understand. We traverse the given linked list from start to end, so the time complexity is linear **O(N)** in any case.

We create two arrays of size k each to store references to dummy nodes and tail nodes. We also create k dummy nodes to simplify the implementation, so the space complexity is **O(K)** in any case.

> **Best Case:** K = 1
>
> -   Space Complexity - **O(K)**
> -   Time Complexity - **O(N)**
>
> **Worst Case:** K = N
>
> -   Space Complexity - **O(N)**
> -   Time Complexity - **O(N)**

***

# Identifying the split pattern

The linked list split technique can only be applied to some specific problems. These are generally easy or medium problems in which we must split a linked list into one or smaller lists. However, there may be some problems where we need to split a list that may have a more straightforward solution than applying the split technique. For example, we can split a list in half using the fast and slow pointer technique, and the split list technique may be overkill. If the problem statement or its solution follows the generic template below, it can be solved by applying the split list technique.

**Template:**

Given a linked list, split it into to `k` lists.

## Example

Let's consider the following problem as an example to better understand how to identify and solve a problem using the split technique.

> **Problem statement:** Given a singly linked list and an integer `k` split the list into `k` lists such that their concatenation results in the original lists. The length of all parts should be equal. If that is not possible, the difference between the size of any two lists should not be greater than one, and the list occurring earlier should have a greater size.

```mermaid
flowchart TB
    INIT["current = head"]
    LOOP["while current is not null:<br/>1. bucket = f(current)<br/>2. tail[bucket].next = current<br/>3. tail[bucket] = current<br/>4. current = current.next"]
    TERM["for each bucket i:<br/>tail[i].next = null<br/>(seal the output list)"]
    HEADS["real heads are dummy[i].next<br/>(skip past the dummies)"]
    INIT --> LOOP --> TERM --> HEADS
```

<p align="center"><strong>Single pass — for each node, compute its bucket, tack it onto that bucket's tail, advance. Afterwards, seal every bucket's tail and extract the real heads from <code>dummy[i].next</code>.</strong></p>

### Split technique solution

We need to split a given list into k parts, and this fits the generic template from the split pattern we learned earlier.

**Template:**

Given a linked list, split it into to `k` lists.

We first find the `length` of the given list and divide it by `k` to calculate the minimum number of nodes each of the `k` split lists will have. We save this value in a variable `partSize`. The length may not be multiple of `k`, which means that some split lists will have `partSize + 1` nodes. We create a variable `bigLists` and initialize it with `length % k`, which is the number of lists that will have `partSize + 1` nodes in them.

```d2
direction: right
length: "length = n"
k: "k = number of buckets"
base: |md
  base = n / k (integer)

  size of each 'small' sublist
|
rem: |md
  remainder = n % k

  = number of 'big' sublists (size base+1)
|
length -> base
k -> base
length -> rem
k -> rem
```

<p align="center"><strong>Unequal splitting — when <code>n</code> isn't divisible by <code>k</code>, the first <code>n mod k</code> sublists get one extra node (<code>base + 1</code>), and the rest get exactly <code>base</code>. Compute both quantities once before splitting.</strong></p>

We then apply the split list technique by creating two arrays of ListNode references `dummy` and `tails` of size `k` each and initialize all items in them with the references of newly created dummy nodes. We initialize `current` with head and use it to traverse the list from start to end. We also initialize two variables `idx` and `count` with 0 to to keep track of the current split list and the number of nodes already added to it. We then iterate the list and use the variables `bigLists` and `count` to update `idx` when we have added all nodes to the current split list.

```d2
state: "Initial state for unequal split" {
  grid-rows: 2
  grid-gap: 16
  d: |md
    **dummy[k]**

    k heads
    (one per output)
  |
  t: |md
    **tail[k]**

    k tails
    (tracks end of each)
  |
  cur: |md
    **current = head**

    (the walker)
  |
  big: |md
    **bigLists = n % k**

    (# of k+1-sized lists)
  |
  base: |md
    **baseSize = n / k**

    (size of small lists)
  |
  bkt: |md
    **bucket = 0**

    (round-robin counter)
  |
}
```

<p align="center"><strong>Initial state for unequal split — alongside the usual <code>dummy</code> / <code>tail</code> arrays, track how many nodes still belong to the current bucket and how many "big" buckets remain.</strong></p>

In each iteration, we add the node held in `current` at the end of the split list denoted by `idx` and increment `count`. If we have any `bigLists` left i.e. `bigLists > 0` it means the current split list (denoted by `idx`) should have `partSize + 1` nodes, otherwise it should only have `partSize` nodes.

We check for these conditions to correctly update `idx` for the subsequent iterations. If `bigLists > 0` we check if we have already added `partSize + 1` nodes to the current split list be checking if `count == partSize + 1` and reset `count` to 0, decrement `bigLists` and increment `idx`. Otherwise, if `bigLists == 0` we check if `count == partSize` reset `count` to 0 and increment `idx`. This ensures that we move to the next split list after we have added all nodes to the current split list.

```mermaid
flowchart TB
    INIT["current = head"]
    LOOP["while current is not null:<br/>1. bucket = f(current)<br/>2. tail[bucket].next = current<br/>3. tail[bucket] = current<br/>4. current = current.next"]
    TERM["for each bucket i:<br/>tail[i].next = null<br/>(seal the output list)"]
    HEADS["real heads are dummy[i].next<br/>(skip past the dummies)"]
    INIT --> LOOP --> TERM --> HEADS
```

<p align="center"><strong>Single pass — for each node, compute its bucket, tack it onto that bucket's tail, advance. Afterwards, seal every bucket's tail and extract the real heads from <code>dummy[i].next</code>.</strong></p>

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

# Function to calculate the length of the linked list
def lengthOfLinkedList(head: Optional[ListNode]) -> int:
    length = 0
    while head is not None:
        length += 1
        head = head.next
    return length

def kWayListSplit(head: Optional[ListNode], k: int) -> List[Optional[ListNode]]:
    # Count the number of nodes in the linked list
    length = lengthOfLinkedList(head)
    # Calculate the size of each part
    partSize = length // k
    # Calculate the number of lists with partSize + 1 nodes
    bigLists = length % k

    # Create an array of dummy and tail nodes
    dummy = [ListNode(0) for _ in range(k)]
    tails = dummy[:]

    # Iterate in the list using current pointer
    current = head
    count = 0
    idx = 0

    while current is not None:
        # Add node to the current split list and update tail
        tails[idx].next = current
        tails[idx] = current

        # Move current ahead
        current = current.next
        # Set the next section of tail node to None
        tails[idx].next = None

        # Increment count after adding the node
        count += 1

        # Determine when to move to the next list
        if bigLists > 0 and count == partSize + 1:
            count = 0
            idx += 1
            bigLists -= 1
        elif bigLists == 0 and count == partSize:
            count = 0
            idx += 1

    # Remove the dummy nodes and return the real heads
    for i in range(k):
        dummy[i] = dummy[i].next

    # Return the array of split lists' heads
    return dummy
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

class KWayListSplit {

    // Function to find the length of a linked list
    int lengthOfLinkedList(ListNode head) {
        int length = 0;
        while (head != null) {
            ++length;
            head = head.next;
        }
        return length;
    }

    List<ListNode> kWayListSplit(ListNode head, int k) {
        // Count the number of nodes in the linked list
        int length = lengthOfLinkedList(head);
        // Calculate the size of each part
        int partSize = length / k;
        // Calculate the number of lists with partSize + 1 nodes
        int bigLists = length % k;

        // Create an array of references for dummy and tail nodes
        List<ListNode> dummy = new ArrayList<>(Collections.nCopies(k, null));
        List<ListNode> tails = new ArrayList<>(Collections.nCopies(k, null));

        for (int i = 0; i < k; i++) {
            dummy.set(i, new ListNode(0)); // create a new dummy node
            tails.set(i, dummy.get(i)); // point tail to the dummy node
        }

        // Iterate in the list using current
        ListNode current = head;

        // Initialize counter to count number of nodes
        // added to current split list
        int count = 0;

        // Initialize variable to denote current split list
        int idx = 0;

        while (current != null) {
            // Add node to the current split list and update tail
            tails.get(idx).next = current;
            tails.set(idx, current);

            // Move current ahead
            current = current.next;

            // Set the next section of tail node to null
            tails.get(idx).next = null;

            // Increment count after adding the node
            count++;

            if (bigLists > 0 && count == partSize + 1) {
                count = 0; // Reset count to 0
                idx++; // Increment idx to add to next sublist from next iteration
                bigLists--; // Reduce the number of bigLists left
            } else if (bigLists == 0 && count == partSize) {
                count = 0; // Reset count to 0
                idx++; // Increment idx to add to next sublist from next iteration
            }
        }

        // Delete the dummy nodes
        for (int i = 0; i < k; i++) {
            dummy.set(i, dummy.get(i).next); // move the dummy head to the real first node
        }

        // Return the list of split lists' heads
        return dummy;
    }
}
```


The above implementation uses the template code of the split list technique to split the list into k lists in a single pass.

## Example problems

Most problems that fall under this category are**medium**problems; a list of a few is given below.

> -   **[Even odd split](#even-odd-split)**
> -   **[Split alternate groups](#split-alternate-groups)**
> -   **[Split by modulo](#split-by-modulo)**
> -   **[K-way list split](#k-way-list-split)**

We will now solve these problems to understand the split list technique better.

***

# Even odd split

## Problem Statement

Given the **head** of a singly linked list, write a function to split the list into two separate lists such that the first list contains the nodes with even values and the second list contains the nodes with odd values. Your function should return the heads of both these lists.

### Example 1

> -   **Input:** head = \[5, 2, 3, 10, 6, 8\]
> -   **Output:** \[\[2, 10, 6, 8\], \[5, 3\]\]
> -   **Explanation:** Above is the list containing the even and odd-valued nodes, respectively.

### Example 2

> -   **Input:** head = \[4, 2, 6, 10\]
> -   **Output:** \[\[4, 2, 6, 10\], \[\]\]
> -   **Explanation:** Above is the list containing the even and odd-valued nodes, respectively.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List, Optional


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
    def even_odd_split(
        self, head: Optional[ListNode]
    ) -> List[Optional[ListNode]]:

        # Initialize head and tail references for the two split lists
        even_dummy = ListNode(0)
        even_tail = even_dummy

        odd_dummy = ListNode(0)
        odd_tail = odd_dummy

        # Create current reference to iterate through the list
        current = head

        # Iterate through the list and split nodes into two lists
        while current is not None:

            # If the current node's value is even then the node goes to
            # the even list
            if current.val % 2 == 0:

                # `current` node goes to the even split list
                even_tail.next = current

                # Move even_tail forward
                even_tail = even_tail.next

            # Otherwise, the node goes to the odd list
            else:

                # `current` node goes to the odd split list
                odd_tail.next = current

                # Move odd_tail forward
                odd_tail = odd_tail.next

            # Move to the next node in the original list
            current = current.next

        # Terminate the even list
        even_tail.next = None

        # Terminate the odd list
        odd_tail.next = None

        return [even_dummy.next, odd_dummy.next]


e, o = Solution().even_odd_split(from_list([5, 2, 3, 10, 6, 8]))
print(to_list(e), to_list(o))   # [2, 10, 6, 8] [5, 3]

e, o = Solution().even_odd_split(from_list([4, 2, 6, 10]))
print(to_list(e), to_list(o))   # [4, 2, 6, 10] []

# Edge cases
e, o = Solution().even_odd_split(None)
print(to_list(e), to_list(o))   # [] []

e, o = Solution().even_odd_split(from_list([1]))
print(to_list(e), to_list(o))   # [] [1]

e, o = Solution().even_odd_split(from_list([2]))
print(to_list(e), to_list(o))   # [2] []

e, o = Solution().even_odd_split(from_list([1, 3, 5, 7]))
print(to_list(e), to_list(o))   # [] [1, 3, 5, 7]

e, o = Solution().even_odd_split(from_list([2, 4]))
print(to_list(e), to_list(o))   # [2, 4] []
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
        public List<ListNode> evenOddSplit(ListNode head) {

            // Initialize head and tail references for the two split lists
            ListNode evenDummy = new ListNode(0);
            ListNode evenTail = evenDummy;

            ListNode oddDummy = new ListNode(0);
            ListNode oddTail = oddDummy;

            // Create current reference to iterate through the list
            ListNode current = head;

            // Iterate through the list and split nodes into two lists
            while (current != null) {

                // If the current node's value is even then the node goes to
                // the even list
                if (current.val % 2 == 0) {

                    // `current` node goes to the even split list
                    evenTail.next = current;

                    // Move evenTail forward
                    evenTail = evenTail.next;
                }

                // Otherwise, the node goes to the odd list
                else {

                    // `current` node goes to the odd split list
                    oddTail.next = current;

                    // Move oddTail forward
                    oddTail = oddTail.next;
                }

                // Move to the next node in the original list
                current = current.next;
            }

            // Terminate the even list
            evenTail.next = null;

            // Terminate the odd list
            oddTail.next = null;

            return Arrays.asList(evenDummy.next, oddDummy.next);
        }
    }

    public static void main(String[] args) {
        List<ListNode> r1 = new Solution().evenOddSplit(fromList(5, 2, 3, 10, 6, 8));
        System.out.println(toList(r1.get(0)) + " " + toList(r1.get(1)));  // [2, 10, 6, 8] [5, 3]

        List<ListNode> r2 = new Solution().evenOddSplit(fromList(4, 2, 6, 10));
        System.out.println(toList(r2.get(0)) + " " + toList(r2.get(1)));  // [4, 2, 6, 10] []

        // Edge cases
        List<ListNode> r3 = new Solution().evenOddSplit(null);
        System.out.println(toList(r3.get(0)) + " " + toList(r3.get(1)));  // [] []

        List<ListNode> r4 = new Solution().evenOddSplit(fromList(1));
        System.out.println(toList(r4.get(0)) + " " + toList(r4.get(1)));  // [] [1]

        List<ListNode> r5 = new Solution().evenOddSplit(fromList(2));
        System.out.println(toList(r5.get(0)) + " " + toList(r5.get(1)));  // [2] []

        List<ListNode> r6 = new Solution().evenOddSplit(fromList(1, 3, 5, 7));
        System.out.println(toList(r6.get(0)) + " " + toList(r6.get(1)));  // [] [1, 3, 5, 7]

        List<ListNode> r7 = new Solution().evenOddSplit(fromList(2, 4));
        System.out.println(toList(r7.get(0)) + " " + toList(r7.get(1)));  // [2, 4] []
    }
}
```

</details>


***

# Split alternate groups

## Problem Statement

Given the **head** of a singly linked list and a positive integer **k**, write a function to split the list into two separate lists by alternating groups of `k` nodes and return the heads of both these lists.

If the remaining nodes at the end are fewer than k, include all of them in the respective group.

### Example 1

> -   **Input:** head = \[5, 2, 3, 10, 6, 8\], k = 2
> -   **Output:** \[\[5, 2, 6, 8\], \[3, 10\]\]
> -   **Explanation:** The list is split into groups of size 2 and assigned alternately: the first group \[5, 2\] to the first list, the second \[3, 10\] to the second list, and the third \[6, 8\] back to the first list.

### Example 2

> -   **Input:** head = \[6, 1, 3, 10, 6, 8\], k = 5
> -   **Output:** \[\[6, 1, 3, 10, 6\], \[8\]\]
> -   **Explanation:** The list is split into groups of size 5 and assigned alternately. The first group \[6, 1, 3, 10, 6\] goes to the first list. Only one node \[8\] remains, which is fewer than k, but it still goes to the second list as the next group.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List, Optional


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
    def split_alternate_groups(
        self, head: Optional[ListNode], k: int
    ) -> List[Optional[ListNode]]:

        # Head and tail references for the two resulting lists
        first_list_dummy: ListNode = ListNode(0)
        first_list_tail: ListNode = first_list_dummy

        second_list_dummy: ListNode = ListNode(0)
        second_list_tail: ListNode = second_list_dummy

        current: Optional[ListNode] = head

        # Flag to alternate between the two lists
        add_to_first_list: bool = True

        # Iterate through the original list
        while current is not None:

            # Start of the current chunk
            chunk_start: ListNode = current
            previous: Optional[ListNode] = None

            # Traverse up to k nodes for the current chunk
            for _ in range(k):
                if current is None:
                    break
                previous = current
                current = current.next

            # Disconnect the chunk from the rest of the list
            if previous:
                previous.next = None

            # Attach chunk to the appropriate list
            if add_to_first_list:

                # Attach chunk to the first list
                first_list_tail.next = chunk_start

                # Move tail to the end of the chunk
                first_list_tail = previous
            else:

                # Attach chunk to the second list
                second_list_tail.next = chunk_start

                # Move tail to the end of the chunk
                second_list_tail = previous

            # Alternate for next chunk
            add_to_first_list = not add_to_first_list

        # Return heads of the two lists
        return [first_list_dummy.next, second_list_dummy.next]


f, s = Solution().split_alternate_groups(from_list([5, 2, 3, 10, 6, 8]), 2)
print(to_list(f), to_list(s))   # [5, 2, 6, 8] [3, 10]

f, s = Solution().split_alternate_groups(from_list([6, 1, 3, 10, 6, 8]), 5)
print(to_list(f), to_list(s))   # [6, 1, 3, 10, 6] [8]

# Edge cases
f, s = Solution().split_alternate_groups(from_list([1]), 1)
print(to_list(f), to_list(s))   # [1] []

f, s = Solution().split_alternate_groups(from_list([1, 2]), 1)
print(to_list(f), to_list(s))   # [1] [2]

f, s = Solution().split_alternate_groups(from_list([1, 2, 3, 4]), 4)
print(to_list(f), to_list(s))   # [1, 2, 3, 4] []

f, s = Solution().split_alternate_groups(from_list([1, 2, 3, 4, 5, 6]), 3)
print(to_list(f), to_list(s))   # [1, 2, 3] [4, 5, 6]
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
        public List<ListNode> splitAlternateGroups(ListNode head, int k) {

            // Head and tail references for the two resulting lists
            ListNode firstListDummy = new ListNode(0);
            ListNode firstListTail = firstListDummy;

            ListNode secondListDummy = new ListNode(0);
            ListNode secondListTail = secondListDummy;

            ListNode current = head;

            // Flag to alternate between the two lists
            boolean addToFirstList = true;

            // Iterate through the original list
            while (current != null) {

                // Start of the current chunk
                ListNode chunkStart = current;
                ListNode previous = null;

                // Traverse up to k nodes for the current chunk
                for (int i = 0; i < k && current != null; i++) {
                    previous = current;
                    current = current.next;
                }

                // Disconnect the chunk from the rest of the list
                previous.next = null;

                // Attach chunk to the appropriate list
                if (addToFirstList) {

                    // Attach chunk to the first list
                    firstListTail.next = chunkStart;

                    // Move tail to the end of the chunk
                    firstListTail = previous;
                }

                // If not the first list, then it must be the second list
                else {

                    // Attach chunk to the second list
                    secondListTail.next = chunkStart;

                    // Move tail to the end of the chunk
                    secondListTail = previous;
                }

                // Alternate for next chunk
                addToFirstList = !addToFirstList;
            }

            // Return heads of the two lists
            return Arrays.asList(firstListDummy.next, secondListDummy.next);
        }
    }

    public static void main(String[] args) {
        List<ListNode> r1 = new Solution().splitAlternateGroups(fromList(5, 2, 3, 10, 6, 8), 2);
        System.out.println(toList(r1.get(0)) + " " + toList(r1.get(1)));  // [5, 2, 6, 8] [3, 10]

        List<ListNode> r2 = new Solution().splitAlternateGroups(fromList(6, 1, 3, 10, 6, 8), 5);
        System.out.println(toList(r2.get(0)) + " " + toList(r2.get(1)));  // [6, 1, 3, 10, 6] [8]

        // Edge cases
        List<ListNode> r3 = new Solution().splitAlternateGroups(fromList(1), 1);
        System.out.println(toList(r3.get(0)) + " " + toList(r3.get(1)));  // [1] []

        List<ListNode> r4 = new Solution().splitAlternateGroups(fromList(1, 2), 1);
        System.out.println(toList(r4.get(0)) + " " + toList(r4.get(1)));  // [1] [2]

        List<ListNode> r5 = new Solution().splitAlternateGroups(fromList(1, 2, 3, 4), 4);
        System.out.println(toList(r5.get(0)) + " " + toList(r5.get(1)));  // [1, 2, 3, 4] []

        List<ListNode> r6 = new Solution().splitAlternateGroups(fromList(1, 2, 3, 4, 5, 6), 3);
        System.out.println(toList(r6.get(0)) + " " + toList(r6.get(1)));  // [1, 2, 3] [4, 5, 6]
    }
}
```

</details>


***

# Split by modulo

## Problem Statement

Given the **head** of a singly linked list and a positive integer `**k**`, write a function to split the list into `k` separate lists. Each node should be placed into one of the `k` lists according to the remainder when its value is divided by `k`. Your function should return the heads of all `k` lists in order from remainder `0` to `k -1`.

### Example 1

> -   **Input:** head = \[5, 2, 3, 10, 6, 8\], k = 3
> -   **Output:** \[\[3, 6\], \[10\], \[5, 2, 8\]\]
> -   **Explanation:** The above list is split into k groups based on the remainder when each node’s value is divided by k.

### Example 2

> -   **Input:** head = \[4\], k = 3
> -   **Output:** \[\[\], \[4\], \[\]\]
> -   **Explanation:** The above list is split into k groups based on the remainder when each node’s value is divided by k.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List, Optional


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
    def split_by_modulo(
        self, head: Optional[ListNode], k: int
    ) -> List[Optional[ListNode]]:

        # Initialize head and tail references for k split lists
        dummy_heads: List[ListNode] = [ListNode(0) for _ in range(k)]
        tails: List[ListNode] = dummy_heads[:]

        # Create current reference to iterate through the list
        current = head

        # Iterate through the list and split nodes into k lists
        while current is not None:

            # Find group index using modulo operation
            group = current.val % k

            # `current` node goes to its modulo group
            tails[group].next = current

            # Move group tail forward
            tails[group] = tails[group].next

            # Move to the next node in the original list
            current = current.next

        # Terminate each list to avoid cycles
        for i in range(k):
            tails[i].next = None

        # Collect heads (excluding dummy nodes)
        result: List[Optional[ListNode]] = []
        for i in range(k):
            result.append(dummy_heads[i].next)

        return result


r = Solution().split_by_modulo(from_list([5, 2, 3, 10, 6, 8]), 3)
print([to_list(x) for x in r])   # [[3, 6], [10], [5, 2, 8]]

r = Solution().split_by_modulo(from_list([4]), 3)
print([to_list(x) for x in r])   # [[], [4], []]

# Edge cases
r = Solution().split_by_modulo(None, 2)
print([to_list(x) for x in r])   # [[], []]

r = Solution().split_by_modulo(from_list([1, 2, 3, 4]), 2)
print([to_list(x) for x in r])   # [[2, 4], [1, 3]]

r = Solution().split_by_modulo(from_list([0, 0, 0]), 3)
print([to_list(x) for x in r])   # [[0, 0, 0], [], []]

r = Solution().split_by_modulo(from_list([1]), 1)
print([to_list(x) for x in r])   # [[1]]
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
        public List<ListNode> splitByModulo(ListNode head, int k) {

            // Initialize head and tail references for k split lists
            List<ListNode> dummyHeads = new ArrayList<>();
            List<ListNode> tails = new ArrayList<>();
            for (int i = 0; i < k; i++) {

                // Dummy head nodes
                dummyHeads.add(new ListNode(0));

                // Tail starts at dummy
                tails.add(dummyHeads.get(i));
            }

            // Create current reference to iterate through the list
            ListNode current = head;

            // Iterate through the list and split nodes into k lists
            while (current != null) {

                // Find group index using modulo operation
                int group = current.val % k;

                // `current` node goes to its modulo group
                tails.get(group).next = current;

                // Move group tail forward
                tails.set(group, tails.get(group).next);

                // Move to the next node in the original list
                current = current.next;
            }

            // Terminate each list to avoid cycles
            for (int i = 0; i < k; i++) {
                tails.get(i).next = null;
            }

            // Collect heads (excluding dummy nodes)
            List<ListNode> result = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                result.add(dummyHeads.get(i).next);
            }

            return result;
        }
    }

    public static void main(String[] args) {
        List<ListNode> r1 = new Solution().splitByModulo(fromList(5, 2, 3, 10, 6, 8), 3);
        System.out.println(r1.stream().map(Main::toList).toList());  // [[3, 6], [10], [5, 2, 8]]

        List<ListNode> r2 = new Solution().splitByModulo(fromList(4), 3);
        System.out.println(r2.stream().map(Main::toList).toList());  // [[], [4], []]

        // Edge cases
        List<ListNode> r3 = new Solution().splitByModulo(null, 2);
        System.out.println(r3.stream().map(Main::toList).toList());  // [[], []]

        List<ListNode> r4 = new Solution().splitByModulo(fromList(1, 2, 3, 4), 2);
        System.out.println(r4.stream().map(Main::toList).toList());  // [[2, 4], [1, 3]]

        List<ListNode> r5 = new Solution().splitByModulo(fromList(0, 0, 0), 3);
        System.out.println(r5.stream().map(Main::toList).toList());  // [[0, 0, 0], [], []]

        List<ListNode> r6 = new Solution().splitByModulo(fromList(1), 1);
        System.out.println(r6.stream().map(Main::toList).toList());  // [[1]]
    }
}
```

</details>


***

# K-way list split

## Problem Statement

Given the **head** of a singly linked list and an integer **k**, write a function to split the linked list into k consecutive linked list parts. Your function should return the heads of all the split parts.

> The length of each part should be as equal as possible. No two parts should have a size differing by more than one. This may lead to some parts being `null`. The parts should be in the order of occurrence in the input list, and parts occurring earlier should always have a size greater than or equal to parts occurring later.

### Example 1

> -   **Input:** head = \[1, 2, 3\], k = 5
> -   **Output:** \[\[1\], \[2\], \[3\], \[\], \[\]\]
> -   **Explanation:** As we need to split the list into five parts, which should be as equal as possible, we can divide it into parts of size one. For the remaining parts, the list would be null.

### Example 2

> -   **Input:** head = \[1, 2, 3, 4, 5, 6, 7, 8, 9, 10\], k = 3
> -   **Output:** \[\[1, 2, 3, 4\], \[5, 6, 7\], \[8, 9, 10\]\]
> -   **Explanation:** As we need to split the list into 3 parts, which should be as equal as possible, we can divide it into parts of sizes 4, 3, and 3, respectively.

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List, Optional


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
    def find_length(self, head: Optional[ListNode]) -> int:
        length = 0
        while head is not None:
            length += 1
            head = head.next
        return length

    def k_way_list_split(
        self, head: Optional[ListNode], k: int
    ) -> List[Optional[ListNode]]:

        # Get total number of nodes
        length = self.find_length(head)

        # Base size of each part
        part_size = length // k

        # Remainder to distribute among parts
        extra_nodes = length % k

        # Result list to store part heads
        parts: List[Optional[ListNode]] = [None] * k

        # Pointer to traverse the list
        current = head

        for i in range(k):

            # Set the start of the current part
            parts[i] = current

            # Calculate the size for the current part
            current_part_size = part_size + (1 if extra_nodes > 0 else 0)

            # Traverse `current_part_size - 1` nodes ahead in the list
            for j in range(1, current_part_size):
                if current:
                    current = current.next

            # Move to the start of the next part, breaking the link
            if current:
                next_part_head = current.next
                current.next = None
                current = next_part_head

            # Decrease `extra_nodes` only if it was used for this part
            if extra_nodes > 0:
                extra_nodes -= 1

        return parts


r = Solution().k_way_list_split(from_list([1, 2, 3]), 5)
print([to_list(x) for x in r])   # [[1], [2], [3], [], []]

r = Solution().k_way_list_split(from_list([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]), 3)
print([to_list(x) for x in r])   # [[1, 2, 3, 4], [5, 6, 7], [8, 9, 10]]

# Edge cases
r = Solution().k_way_list_split(None, 3)
print([to_list(x) for x in r])   # [[], [], []]

r = Solution().k_way_list_split(from_list([1]), 1)
print([to_list(x) for x in r])   # [[1]]

r = Solution().k_way_list_split(from_list([1, 2]), 2)
print([to_list(x) for x in r])   # [[1], [2]]

r = Solution().k_way_list_split(from_list([1, 2, 3, 4, 5, 6]), 2)
print([to_list(x) for x in r])   # [[1, 2, 3], [4, 5, 6]]
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
        private int findLength(ListNode head) {
            int length = 0;
            while (head != null) {
                length++;
                head = head.next;
            }
            return length;
        }

        public List<ListNode> kWayListSplit(ListNode head, int k) {

            // Get total number of nodes
            int length = findLength(head);

            // Base size of each part
            int partSize = length / k;

            // Remainder to distribute among parts
            int extraNodes = length % k;

            // Result list to store part heads
            List<ListNode> parts = new ArrayList<>(k);

            // Pointer to traverse the list
            ListNode current = head;

            for (int i = 0; i < k; ++i) {

                // Set the start of the current part
                parts.add(current);

                // Calculate the size for the current part
                int currentPartSize = partSize + (extraNodes > 0 ? 1 : 0);

                // Traverse `currentPartSize - 1` nodes ahead in the list
                for (
                    int j = 1;
                    j < currentPartSize && current != null;
                    ++j
                ) {
                    current = current.next;
                }

                // Move to the start of the next part, breaking the link
                if (current != null) {
                    ListNode nextPartHead = current.next;
                    current.next = null;
                    current = nextPartHead;
                }

                // Decrease `extraNodes` only if it was used for this part
                if (extraNodes > 0) {
                    extraNodes--;
                }
            }

            return parts;
        }
    }

    public static void main(String[] args) {
        List<ListNode> r1 = new Solution().kWayListSplit(fromList(1, 2, 3), 5);
        System.out.println(r1.stream().map(Main::toList).toList());  // [[1], [2], [3], [], []]

        List<ListNode> r2 = new Solution().kWayListSplit(fromList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 3);
        System.out.println(r2.stream().map(Main::toList).toList());  // [[1, 2, 3, 4], [5, 6, 7], [8, 9, 10]]

        // Edge cases
        List<ListNode> r3 = new Solution().kWayListSplit(null, 3);
        System.out.println(r3.stream().map(Main::toList).toList());  // [[], [], []]

        List<ListNode> r4 = new Solution().kWayListSplit(fromList(1), 1);
        System.out.println(r4.stream().map(Main::toList).toList());  // [[1]]

        List<ListNode> r5 = new Solution().kWayListSplit(fromList(1, 2), 2);
        System.out.println(r5.stream().map(Main::toList).toList());  // [[1], [2]]

        List<ListNode> r6 = new Solution().kWayListSplit(fromList(1, 2, 3, 4, 5, 6), 2);
        System.out.println(r6.stream().map(Main::toList).toList());  // [[1, 2, 3], [4, 5, 6]]
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


The split pattern is a single template with a swappable classifier:

```
dummies = [ListNode() for _ in range(k)]
tails   = dummies[:]                  # each tail starts at its dummy
for node in original_list:
    b = classify(node)                # <-- the only problem-specific line
    tails[b].next = node
    tails[b] = node
for t in tails:
    t.next = None                     # seal every output list
heads = [d.next for d in dummies]     # real heads live one hop past the dummies
```

Four insights worth burning in:

| Insight | Why it matters |
|---|---|
| Dummy heads eliminate the "first node" special case | Without them, every append needs `if tail[b] is None: head[b] = node else: tail[b].next = node`. With them: always `tail[b].next = node`. |
| Re-link, don't copy | Nodes never move in memory; only `.next` pointers change. Zero allocations beyond the `k` dummies. One pass. |
| The classifier is the whole problem | Every variant (even/odd, alternate groups, round robin, modulo, unequal sizes) differs only in the `classify(node)` function. The skeleton is identical. |
| Sealing the tail is non-negotiable | The last node tacked onto each bucket still points into the middle of some other output list. Setting `tail[b].next = null` at the end is what turns a tangle of shared pointers into `k` independent lists. |

When you next see "split by rule", "bucket by hash", "round-robin distribute", "even/odd split", "partition by predicate" — reach for the dummy-and-tails template first. Then just write the one-line classifier.

> **Transfer Challenge:** Split a linked list into two lists where **list A contains all nodes whose value is less than the first node's value**, and **list B contains the rest** (preserving original order within each). What's your `classify(node)` function — and what extra state do you need to track?
>
> <details><summary><strong>Solution hint</strong></summary>
>
> Save <code>pivot = head.val</code> <em>before</em> the loop starts (you need it after the head itself gets routed). Then <code>classify(node) = 0 if node.val < pivot else 1</code>. Everything else is the standard 2-bucket template. This is also the partition step of quicksort on a linked list — a template with teeth.
>
> </details>

</details>