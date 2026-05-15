# 7. Pattern: Reversal (Subproblem)

## The Hook

You learned the six-line reversal loop in the last lesson. Now the real game begins. What if you only want to reverse **part** of a list — first K, last K, every K, alternate segments? These problems *look* harder, but under the microscope they're all the same trick: **carve the list into windows, call reversal on each window, stitch the windows back together.** Reversal is the atom. Everything here is molecules.

The one new skill is **boundary tracking**. A full-list reversal has no boundaries — the endpoints are `head` and `null`. A segment reversal has four: the node before `start`, `start`, `end`, and the node after `end`. Lose any one of them and the list falls apart. Master the four-pointer boundary dance and you've unlocked every reversal-as-subroutine problem in the interview canon. Let's go.

---

## Table of contents

1. [Identifying reversal subproblem](#identifying-reversal-subproblem)
2. [Pairwise swap](#pairwise-swap)
3. [Reverse K-segments](#reverse-k-segments)
4. [Reverse increasing groups](#reverse-increasing-groups)
5. [Reverse alternate segments](#reverse-alternate-segments)

***

# Identifying reversal subproblem

Some problems may consist of smaller subproblems that can be solved using the reversal technique. Solving these subproblems may either partially or fully solve the original problem. These are usually **medium** or **hard** problems, as breaking down a problem into subproblems may not be obvious and may require some critical observation. These problems are also implementation-heavy, meaning the solution code is often big and complex, which makes it error-prone.

Asking yourself the following questions will help you determine whether a problem is a reversal subproblem pattern problem or not.

**Ask yourself questions:**

Q1. Can the problem or solution be broken down into smaller subproblems?

Q2. Can any subproblem be solved by reversing a part of the linked list?

## Example

Let's consider an example problem and see how to break it down into smaller subproblems that can be solved using the reversal algorithm to understand it better.

> **Problem statement:** Given a linked list, reverse the list in groups of K in-place. If the last group in the list does not have K nodes, don't reverse it.

Consider the following example with`k = 3`for a linked list of size 7.

```d2
direction: right

before: "Before — list = [1, 2, 3, 4, 5, 6, 7], k = 3" {
  direction: right
  n1: {grid-columns: 2; grid-gap: 0; value: 1; next}
  n2: {grid-columns: 2; grid-gap: 0; value: 2; next}
  n3: {grid-columns: 2; grid-gap: 0; value: 3; next}
  n4: {grid-columns: 2; grid-gap: 0; value: 4; next}
  n5: {grid-columns: 2; grid-gap: 0; value: 5; next}
  n6: {grid-columns: 2; grid-gap: 0; value: 6; next}
  n7: {grid-columns: 2; grid-gap: 0; value: 7; next: "null"}
  n1.next -> n2.value
  n2.next -> n3.value
  n3.next -> n4.value
  n4.next -> n5.value
  n5.next -> n6.value
  n6.next -> n7.value
}

after: "After — each group of 3 reversed in place (the trailing '7' stays put)" {
  direction: right
  n3: {grid-columns: 2; grid-gap: 0; value: 3; next}
  n2: {grid-columns: 2; grid-gap: 0; value: 2; next}
  n1: {grid-columns: 2; grid-gap: 0; value: 1; next}
  n6: {grid-columns: 2; grid-gap: 0; value: 6; next}
  n5: {grid-columns: 2; grid-gap: 0; value: 5; next}
  n4: {grid-columns: 2; grid-gap: 0; value: 4; next}
  n7: {grid-columns: 2; grid-gap: 0; value: 7; next: "null"}
  n3.next -> n2.value
  n2.next -> n1.value
  n1.next -> n6.value
  n6.next -> n5.value
  n5.next -> n4.value
  n4.next -> n7.value
}

before -> after
```

<p align="center"><strong>Reverse-in-groups-of-K — slice the list into chunks of <code>k</code>, reverse each chunk in place, and leave any trailing (fewer-than-<code>k</code>) nodes untouched. The core reversal loop is invoked once per chunk.</strong></p>

## Linked list reversal solution

Let's ask ourselves the questions we listed above to identify if we can reduce this problem to the two-pointer pattern problem.

**Template:**

Q1. Can the problem or solution be broken down into smaller subproblems?

A1. Yes, we can break down the solution as a combination `length / k` reversal operations, where `length` is the length of the linked list.

Q2. Can any subproblem be solved by reversing a part of the linked list?

A2. Yes, all subproblems except finding the length can be solved by reversing a part of the linked list.

The critical observation here is that reversing a group of size `k` is the same as reversing a part of the linked list between start and end. We traverse the linked list `k` nodes at a time and reverse each group as we go. We initialize a variable `groups` with the number of k-groups (`length / k`) to reverse, truncating the fractional part as the number of k groups will always be a whole number. We use `groups` to iterate, reversing a k-group in each iteration. 

```d2
direction: right
length: "length = 7"
k: "k = 3"
g: "groups = length / k = 2 (integer division)"
r: "remaining = length % k = 1 (trailing, untouched)"
length -> g
k -> g
length -> r
k -> r
```

<p align="center"><strong>Pre-compute <code>length</code> in one pass. The number of full reversible groups is <code>length / k</code>; the remainder <code>length % k</code> trails untouched.</strong></p>

We use two reference variables `start` and `end` to denote the boundary of a k-group that we need to reverse and a variable `leftBound` to hold the node before `start` that is used to correctly connect the head of the reversed segment to the list.

We initialize `start` and `end` with the `head` of the list and iterate `k-1` times using `end` to find the end of the first k-group. We initialize `leftBound` with null for the first k-group, as there is no node before the head of the list.

```d2
direction: right
h: head {shape: oval}
lb: |md
  **leftBound**

  (dummy before first group)
| {style.fill: "#ede9fe"; style.stroke: "#3b82f6"}
n1: |md
  **1**

  start
| {style.fill: "#fde68a"; style.stroke: "#d97706"}
n2: "2"
n3: |md
  **3**

  end
| {style.fill: "#fde68a"; style.stroke: "#d97706"}
n4: "4"
n5: "5"
n6: "6"
n7: "7"
h -> lb
lb -> n1
n1 -> n2
n2 -> n3
n3 -> n4
n4 -> n5
n5 -> n6
n6 -> n7
```

<p align="center"><strong>Three boundary pointers per group — <code>leftBound</code> (the node <em>before</em> <code>start</code>, needed so we can re-attach the reversed group to the rest of the list), <code>start</code> (first node of the group), and <code>end</code> (last node of the group, reached by advancing <code>start</code> by <code>k−1</code> hops).</strong></p>

After reversing the first k-group, we need to update the `head` of the list, as the previous `end` node will be the new head of the list.

```d2
direction: right

before: "Before first reversal" {
  direction: right
  h: head {shape: oval}
  n1: "1"
  n2: "2"
  n3: "3"
  n4: "4"
  n5: "·"
  h -> n1
  n1 -> n2
  n2 -> n3
  n3 -> n4
  n4 -> n5
}

after: "After — head now points at the NEW first node (3)" {
  direction: right
  h: head {shape: oval}
  n3: {value: 3; style.fill: "#dcfce7"; style.stroke: "#16a34a"}
  n2: "2"
  n1: "1"
  n4: "4"
  n5: "·"
  h -> n3.value
  n3.value -> n2
  n2 -> n1
  n1 -> n4
  n4 -> n5
}

before -> after
```

<p align="center"><strong>After the <em>first</em> group is reversed, its head becomes the new head of the entire list. Update <code>head</code> to point at <code>end</code> of the just-reversed group. Subsequent groups don't need this update — their previous group handles the re-attachment.</strong></p>

Similarly, after reversing the first k-group, the previous `start` and the node after it would be the `leftBound` and `start` for the next k-group respectively.

```d2
direction: right
h: head {shape: oval}
g1a: "3"
g1b: "2"
g1c: "1"
lb2: |md
  **leftBound**

  (= last node of just-reversed group)
| {style.fill: "#ede9fe"; style.stroke: "#3b82f6"}
s2: |md
  **4**

  start
| {style.fill: "#fde68a"; style.stroke: "#d97706"}
m: "5"
e2: |md
  **6**

  end
| {style.fill: "#fde68a"; style.stroke: "#d97706"}
r: "7"
h -> g1a
g1a -> g1b
g1b -> g1c
g1c -> lb2
lb2 -> s2
s2 -> m
m -> e2
e2 -> r
```

<p align="center"><strong>After processing one group, slide the boundary forward — the old <code>start</code> becomes the new <code>leftBound</code>, and <code>start</code> advances to the first node of the next group. The segment-reversal loop is now primed to repeat.</strong></p>

We repeat the process to find the `end` of the next segment and reverse the list between `start` and `end` and for all the subsequent k-group reversals, we use `leftBound` to connect the reversed head of the segment back to the list. At the end of all iterations, all the k-groups in the list are reversed in place. The complete execution of the linked list reversal solution is given below.

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
    INIT["length = count(head)<br/>groups = length / k<br/>leftBound = null<br/>start = head"]
    LOOP["For g = 0 to groups − 1:<br/>1. end = advance start by k−1<br/>2. reverse segment [start, end]<br/>3. stitch leftBound.next to new segment head<br/>4. if g == 0: head = new segment head<br/>5. leftBound = start (old start = new tail)<br/>6. start = leftBound.next"]
    END(["return head"])
    INIT --> LOOP --> END
```

<p align="center"><strong>The full algorithm — measure the list, iterate <code>length / k</code> times, and on each iteration slice off a group of <code>k</code>, flip it in place using the segment-reversal primitive, and slide the boundary pointers forward.</strong></p>

The implementation of the reversal algorithm solution is given below, where we create a reverse function to reverse segments between `start` and `end`.  We also create helper functions to find the length of the list  two helper functions to find the length of a linked list and reverse the list between `start` and `end` to keep the implementation simple and modular.


```pseudocode
# Reverse the list in groups of k. Walk the list, reverse each k-segment, stitch them together.
function findLength(head):
    length ← 0
    while head is not null:
        length ← length + 1
        head ← head.next
    return length

function getNodeAtPosition(head, position):
    current ← head
    for i ← 1 to position − 1:
        current ← current.next
    return current

function reverse(start, end):
    current ← start
    rightBound ← end.next
    previous ← rightBound

    while current ≠ rightBound:
        next ← current.next
        current.next ← previous
        previous ← current
        current ← next

    return previous

function reverseKSegments(head, k):

    # If the list is empty, has only one node, or k is 1, no need to reverse segments
    if head is null OR head.next is null OR k = 1:
        return head

    # Start of the current segment to be reversed
    start ← head

    # Pointer to the last node of the previous segment
    leftBound ← null

    # Find the total number of segments in the linked list
    totalSegments ← findLength(head) ÷ k

    # Loop through the list to reverse every k-length segment
    for i ← 0 to totalSegments − 1:

        # Get the end node of the current segment
        end ← getNodeAtPosition(start, k)

        # Get the head of the reversed segment.
        reversedHead ← reverse(start, end)

        # If leftBound is null, we're at the first segment — update head
        if leftBound is null:
            head ← reversedHead
        # Otherwise, connect leftBound's next to the new reversedHead
        else:
            leftBound.next ← reversedHead

        # Update leftBound to the current segment's start (which is now the end after reversal)
        leftBound ← start

        # Move to the next segment
        start ← leftBound.next

    # Return the head of the modified list
    return head
```

```python run
from typing import Optional

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

class Solution:
    def find_length(self, head: Optional[ListNode]) -> int:
        length = 0
        while head is not None:
            length += 1
            head = head.next
        return length

    def get_node_at_position(
        self, head: ListNode, position: int
    ) -> ListNode:
        current = head
        for _ in range(1, position):
            current = current.next
        return current

    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = start
        right_bound: Optional[ListNode] = end.next
        previous: Optional[ListNode] = right_bound

        while current != right_bound:
            next_node = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def reverse_k_segments(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:

        # If the list is empty, has only one node, or k is 1, no need to
        # reverse segments
        if head is None or head.next is None or k == 1:
            return head

        # Start of the current segment to be reversed
        start = head

        # Pointer to the last node of the previous segment
        left_bound = None

        # Find the total number of segments in the linked list
        total_segments = self.find_length(head) // k

        # Loop through the list to reverse every k-length segment
        for i in range(total_segments):

            # Get the end node of the current segment
            end = self.get_node_at_position(start, k)

            # Get the head of the reversed segment.
            reversed_head = self.reverse(start, end)

            # Check if there is a previous segment to connect to or
            # if the existing head needs to be updated.
            # If left_bound is None, it means we're at the first segment
            # So, we need to update the head to the reversed_head
            # Return the new head
            if left_bound is None:
                head = reversed_head

            # If there is a left_bound, connect its next to the new
            # reversed_head
            else:
                left_bound.next = reversed_head

            # Update left_bound to the current segment's start (which is
            # now the end after reversal)
            left_bound = start

            # Move to the next segment
            start = left_bound.next

        # Return the head of the modified list
        return head
```

```java run
class Solution {
    public int findLength(ListNode head) {
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }

    public ListNode getNodeAtPosition(ListNode head, int position) {
        ListNode current = head;
        for (int i = 1; i < position; ++i) {
            current = current.next;
        }
        return current;
    }

    public ListNode reverse(ListNode start, ListNode end) {
        ListNode current = start;
        ListNode rightBound = end.next;
        ListNode previous = rightBound;

        while (current != rightBound) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        return previous;
    }

    public ListNode reverseKSegments(ListNode head, int k) {

        // If the list is empty, has only one node, or k is 1, no need to
        // reverse segments
        if (head == null || head.next == null || k == 1) {
            return head;
        }

        // Start of the current segment to be reversed
        ListNode start = head;

        // Pointer to the last node of the previous segment
        ListNode leftBound = null;

        // Find the total number of segments in the linked list
        int totalSegments = findLength(head) / k;

        // Loop through the list to reverse every k-length segment
        for (int i = 0; i < totalSegments; i++) {

            // Get the end node of the current segment
            ListNode end = getNodeAtPosition(start, k);

            // Get the head of the reversed segment.
            ListNode reversedHead = reverse(start, end);

            // Check if there is a previous segment to connect to or
            // if the existing head needs to be updated.
            // If leftBound is null, it means we're at the first
            // segment So, we need to update the head to the
            // reversedHead
            if (leftBound == null) {
                head = reversedHead;
            }

            // If there is a leftBound, connect its next to the new
            // reversedHead
            else {
                leftBound.next = reversedHead;
            }

            // Update leftBound to the current segment's start (which is
            // now the end after reversal)
            leftBound = start;

            // Move to the next segment
            start = leftBound.next;
        }

        // Return the head of the modified list
        return head;
    }
}
```

```c run
typedef struct ListNode { int val; struct ListNode *next; } ListNode;

int findLength(ListNode *head) {
    int length = 0;
    while (head != NULL) {
        length++;
        head = head->next;
    }
    return length;
}

ListNode* getNodeAtPosition(ListNode *head, int position) {
    ListNode *current = head;
    for (int i = 1; i < position; ++i) {
        current = current->next;
    }
    return current;
}

ListNode* reverse(ListNode *start, ListNode *end) {
    ListNode *current = start;
    ListNode *rightBound = end->next;
    ListNode *previous = rightBound;

    while (current != rightBound) {
        ListNode *next = current->next;
        current->next = previous;
        previous = current;
        current = next;
    }

    return previous;
}

ListNode* reverseKSegments(ListNode *head, int k) {

    /* If the list is empty, has only one node, or k is 1, no need to
       reverse segments */
    if (head == NULL || head->next == NULL || k == 1) {
        return head;
    }

    /* Start of the current segment to be reversed */
    ListNode *start = head;

    /* Pointer to the last node of the previous segment */
    ListNode *leftBound = NULL;

    /* Find the total number of segments in the linked list */
    int totalSegments = findLength(head) / k;

    /* Loop through the list to reverse every k-length segment */
    for (int i = 0; i < totalSegments; i++) {

        /* Get the end node of the current segment */
        ListNode *end = getNodeAtPosition(start, k);

        /* Get the head of the reversed segment. */
        ListNode *reversedHead = reverse(start, end);

        /* Check if there is a previous segment to connect to or
           if the existing head needs to be updated.
           If leftBound is null, it means we're at the first
           segment So, we need to update the head to the
           reversedHead */
        if (leftBound == NULL) {
            head = reversedHead;
        }

        /* If there is a leftBound, connect its next to the new
           reversedHead */
        else {
            leftBound->next = reversedHead;
        }

        /* Update leftBound to the current segment's start (which is
           now the end after reversal) */
        leftBound = start;

        /* Move to the next segment */
        start = leftBound->next;
    }

    /* Return the head of the modified list */
    return head;
}
```

```scala run
object Solution {
  def findLength(head: ListNode): Int = {
    var length = 0
    var cur = head
    while (cur != null) {
      length += 1
      cur = cur.next
    }
    length
  }

  def getNodeAtPosition(head: ListNode, position: Int): ListNode = {
    var current = head
    var i = 1
    while (i < position) {
      current = current.next
      i += 1
    }
    current
  }

  def reverse(start: ListNode, end: ListNode): ListNode = {
    var current: ListNode = start
    val rightBound: ListNode = end.next
    var previous: ListNode = rightBound

    while (current ne rightBound) {
      val next = current.next
      current.next = previous
      previous = current
      current = next
    }

    previous
  }

  def reverseKSegments(headIn: ListNode, k: Int): ListNode = {

    // If the list is empty, has only one node, or k is 1, no need to
    // reverse segments
    if (headIn == null || headIn.next == null || k == 1) {
      return headIn
    }

    var head = headIn

    // Start of the current segment to be reversed
    var start = head

    // Pointer to the last node of the previous segment
    var leftBound: ListNode = null

    // Find the total number of segments in the linked list
    val totalSegments = findLength(head) / k

    // Loop through the list to reverse every k-length segment
    for (_ <- 0 until totalSegments) {

      // Get the end node of the current segment
      val end = getNodeAtPosition(start, k)

      // Get the head of the reversed segment.
      val reversedHead = reverse(start, end)

      // Check if there is a previous segment to connect to or
      // if the existing head needs to be updated.
      // If leftBound is null, it means we're at the first
      // segment So, we need to update the head to the
      // reversedHead
      if (leftBound == null) {
        head = reversedHead
      }

      // If there is a leftBound, connect its next to the new
      // reversedHead
      else {
        leftBound.next = reversedHead
      }

      // Update leftBound to the current segment's start (which is
      // now the end after reversal)
      leftBound = start

      // Move to the next segment
      start = leftBound.next
    }

    // Return the head of the modified list
    head
  }
}
```


The process above summarizes how we can identify a problem that can be broken down into smaller subproblems solvable by the reversal algorithm.

## Example problems

Most problems in this category are **medium** or **hard** problems, as subproblems may not be directly identifiable. Also, the implementation may be complex and require creating different functions, which can be error-prone. Below is a list of problems that fall under the reversal subproblem pattern.

> -   **[Pairwise swap](#pairwise-swap)**
> -   **[Reverse K-segments](#reverse-k-segments)**
> -   **[Reverse increasing groups](#reverse-increasing-groups)**
> -   **[Reverse alternate segments](#reverse-alternate-segments)**

We will now solve these problems to get a better understanding of breaking down a problem into subproblems solvable by the reversal algorithm.

***

# Pairwise swap

## Problem Statement

Given the **head** of a singly linked list, write a function to **swap every two adjacent nodes** of this list and return the head of the reordered list.

The problem needs to be solved without modifying the values in the list's nodes. The nodes should be reordered by updating links.

### Example

> -   **Input:** head = \[1, 2, 3, 4\]
> -   **Output:** \[2, 1, 4, 3\]
> -   **Explanation:** After swapping in pair, i.e. (1, 2) => (2, 1) and (3, 4) => (4, 3) the list becomes \[2, 1, 4, 3\].

## Solution


```pseudocode
# Reverse-k-segments specialised to k = 2 → swap consecutive pairs.
function reverse(start, end):
    current ← start
    rightBound ← end.next
    previous ← rightBound

    while current ≠ rightBound:
        next ← current.next
        current.next ← previous
        previous ← current
        current ← next

    return previous

function pairwiseSwap(head):

    # If the list is empty or has only one element, no reversal needed.
    if head is null OR head.next is null:

        # Return the original head
        return head

    # Start of the current pair to be reversed
    start ← head

    # Initialize the 'leftBound' pointer for the first pair's reversal.
    leftBound ← null

    # Loop while there are pairs to be swapped
    while start is not null AND start.next is not null:

        # Get the end node of the current pair
        end ← start.next

        # Get the head of the reversed pair.
        reversedHead ← reverse(start, end)

        # If leftBound is null, we're at the first segment — update head
        if leftBound is null:
            head ← reversedHead
        # Otherwise, connect leftBound.next to the new reversedHead
        else:
            leftBound.next ← reversedHead

        # Update leftBound to the current pair's start (which is now the end after reversal)
        leftBound ← start

        # Move start to the next pair
        start ← start.next

    # Return the head of the modified list
    return head
```

```python run
from typing import Optional

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

class Solution:
    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> Optional[ListNode]:
        current = start
        right_bound = end.next
        previous = right_bound

        while current != right_bound:
            next_node = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def pairwise_swap(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty or has only one element, no reversal
        # needed.
        if head is None or head.next is None:

            # Return the original head
            return head

        # Start of the current pair to be reversed
        start = head

        # Initialize the 'left_bound' pointer for the first pair's
        # reversal.
        left_bound = None

        # Loop while there are pairs to be swapped
        while start is not None and start.next is not None:

            # Get the end node of the current pair
            end = start.next

            # Get the head of the reversed pair.
            reversed_head = self.reverse(start, end)

            # Check if there is a previous segment to connect to or
            # if the existing head needs to be updated.
            # If leftBound is null, it means we're at the first segment
            # So, we need to update the head to the reversedHead
            if left_bound is None:
                head = reversed_head

            # If there is a leftBound, connect its next to the new
            # reversedHead
            else:
                left_bound.next = reversed_head

            # Update left_bound to the current pair's start
            # (which is now the end after reversal)
            left_bound = start

            # Move start to the next pair
            start = start.next

        # Return the head of the modified list
        return head

# Driver
def build(vals):
    dummy = ListNode(0)
    cur = dummy
    for v in vals:
        cur.next = ListNode(v)
        cur = cur.next
    return dummy.next

def to_list(head):
    result = []
    while head:
        result.append(head.val)
        head = head.next
    return result

head = build([1, 2, 3, 4])
print(to_list(Solution().pairwise_swap(head)))  # [2, 1, 4, 3]
```

```java run
public class Solution {
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int v) { val = v; }
        ListNode(int v, ListNode n) { val = v; next = n; }
    }

    static ListNode reverse(ListNode start, ListNode end) {
        ListNode current = start;
        ListNode rightBound = end.next;
        ListNode previous = rightBound;

        while (current != rightBound) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        // Return the new head of the reversed segment
        return previous;
    }

    static ListNode pairwiseSwap(ListNode head) {

        // If the list is empty or has only one element, no reversal
        // needed.
        if (head == null || head.next == null) {
            return head;
        }

        // Start of the current pair to be reversed
        ListNode start = head;

        // Initialize the 'leftBound' pointer for the first pair's
        // reversal.
        ListNode leftBound = null;

        // Loop while there are pairs to be swapped
        while (start != null && start.next != null) {

            // Get the end node of the current pair
            ListNode end = start.next;

            // Get the head of the reversed pair.
            ListNode reversedHead = reverse(start, end);

            // Check if there is a previous segment to connect to or
            // if the existing head needs to be updated.
            // If leftBound is null, it means we're at the first
            // segment So, we need to update the head to the
            // reversedHead
            if (leftBound == null) {
                head = reversedHead;
            }

            // If there is a leftBound, connect its next to the new
            // reversedHead
            else {
                leftBound.next = reversedHead;
            }

            // Update leftBound to the current pair's start
            // (which is now the end after reversal)
            leftBound = start;

            // Move start to the next pair
            start = start.next;
        }

        // Return the head of the modified list
        return head;
    }

    static ListNode build(int[] vals) {
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        for (int v : vals) { cur.next = new ListNode(v); cur = cur.next; }
        return dummy.next;
    }

    static String toStr(ListNode head) {
        StringBuilder sb = new StringBuilder("[");
        while (head != null) {
            sb.append(head.val);
            if (head.next != null) sb.append(", ");
            head = head.next;
        }
        return sb.append("]").toString();
    }

    public static void main(String[] args) {
        ListNode head = build(new int[]{1, 2, 3, 4});
        System.out.println(toStr(pairwiseSwap(head)));  // [2, 1, 4, 3]
    }
}
```

```c run
#include <stdio.h>
#include <stdlib.h>

typedef struct ListNode {
    int val;
    struct ListNode *next;
} ListNode;

ListNode* newNode(int v) {
    ListNode *n = malloc(sizeof *n);
    n->val = v;
    n->next = NULL;
    return n;
}

ListNode* reverse(ListNode *start, ListNode *end) {
    ListNode *current = start;
    ListNode *rightBound = end->next;
    ListNode *previous = rightBound;

    while (current != rightBound) {
        ListNode *next = current->next;
        current->next = previous;
        previous = current;
        current = next;
    }

    return previous;
}

ListNode* pairwiseSwap(ListNode *head) {

    /* If the list is empty or has only one element, no reversal
       needed. */
    if (head == NULL || head->next == NULL) {
        return head;
    }

    /* Start of the current pair to be reversed */
    ListNode *start = head;

    /* Initialize the 'leftBound' pointer for the first pair's
       reversal. */
    ListNode *leftBound = NULL;

    /* Loop while there are pairs to be swapped */
    while (start != NULL && start->next != NULL) {

        /* Get the end node of the current pair */
        ListNode *end = start->next;

        /* Get the head of the reversed pair. */
        ListNode *reversedHead = reverse(start, end);

        /* Check if there is a previous segment to connect to or
           if the existing head needs to be updated.
           If leftBound is null, it means we're at the first
           segment So, we need to update the head to the
           reversedHead */
        if (leftBound == NULL) {
            head = reversedHead;
        }

        /* If there is a leftBound, connect its next to the new
           reversedHead */
        else {
            leftBound->next = reversedHead;
        }

        /* Update leftBound to the current pair's start
           (which is now the end after reversal) */
        leftBound = start;

        /* Move start to the next pair */
        start = start->next;
    }

    /* Return the head of the modified list */
    return head;
}

void printList(ListNode *head) {
    printf("[");
    while (head) {
        printf("%d", head->val);
        if (head->next) printf(", ");
        head = head->next;
    }
    printf("]\n");
}

int main() {
    ListNode *head = newNode(1);
    head->next = newNode(2);
    head->next->next = newNode(3);
    head->next->next->next = newNode(4);

    printList(pairwiseSwap(head));  /* [2, 1, 4, 3] */
    return 0;
}
```

```scala run
class ListNode(var v: Int, var next: ListNode = null)

object Solution {
  def reverse(start: ListNode, end: ListNode): ListNode = {
    var current = start
    val rightBound = end.next
    var previous = rightBound

    while (current != rightBound) {
      val next = current.next
      current.next = previous
      previous = current
      current = next
    }

    // Return the new head of the reversed segment
    previous
  }

  def pairwiseSwap(headIn: ListNode): ListNode = {

    // If the list is empty or has only one element, no reversal
    // needed.
    if (headIn == null || headIn.next == null) {
      return headIn
    }

    var head = headIn

    // Start of the current pair to be reversed
    var start = head

    // Initialize the 'leftBound' pointer for the first pair's
    // reversal.
    var leftBound: ListNode = null

    // Loop while there are pairs to be swapped
    while (start != null && start.next != null) {

      // Get the end node of the current pair
      val end = start.next

      // Get the head of the reversed pair.
      val reversedHead = reverse(start, end)

      // Check if there is a previous segment to connect to or
      // if the existing head needs to be updated.
      // If leftBound is null, it means we're at the first
      // segment So, we need to update the head to the
      // reversedHead
      if (leftBound == null) {
        head = reversedHead
      }

      // If there is a leftBound, connect its next to the new
      // reversedHead
      else {
        leftBound.next = reversedHead
      }

      // Update leftBound to the current pair's start
      // (which is now the end after reversal)
      leftBound = start

      // Move start to the next pair
      start = start.next
    }

    // Return the head of the modified list
    head
  }

  def build(vals: List[Int]): ListNode = {
    val dummy = new ListNode(0)
    var cur = dummy
    for (v <- vals) { cur.next = new ListNode(v); cur = cur.next }
    dummy.next
  }

  def toStr(head: ListNode): String = {
    val sb = new StringBuilder("[")
    var cur = head
    while (cur != null) {
      sb.append(cur.v)
      if (cur.next != null) sb.append(", ")
      cur = cur.next
    }
    sb.append("]").toString
  }

  def main(args: Array[String]): Unit = {
    val head = build(List(1, 2, 3, 4))
    println(toStr(pairwiseSwap(head)))  // [2, 1, 4, 3]
  }
}
```


***

# Reverse K-segments

## Problem Statement

Given the **head** of a singly linked list and a positive integer **k**, write a function to reverse the list in groups of k and return the head of the reversed list.

If, at the end, the length of the remaining list is less than k, do not reverse that part of the list.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10, 6, 8\], k = 3
> -   **Output:** \[3, 7, 5, 8, 6, 10\]
> -   **Explanation:** Since the value of k is 3, we reverse every three nodes from the start.

### Example 2

> -   **Input:** head = \[5, 7, 3, 10, 6\], k = 2
> -   **Output:** \[7, 5, 10, 3, 6\]
> -   **Explanation:** Since the value of k is 2, we reverse every two nodes from the start. At the end, one node remains, it is left as it is.

### Example 3

> -   **Input:** head = \[5, 7, 3, 10, 6\], k = 8
> -   **Output:** \[5, 7, 3, 10, 6\]
> -   **Explanation:** Since the value of k is 8, we cannot reverse any part of the list as the size of the entire list is 6, which is less than 8.

## Solution


```pseudocode
# Reverse the list in groups of k. Skip any trailing fragment shorter than k.
function findLength(head):
    length ← 0
    while head is not null:
        length ← length + 1
        head ← head.next
    return length

function getNodeAtPosition(head, position):
    current ← head
    for i ← 1 to position − 1:
        current ← current.next
    return current

function reverse(start, end):
    current ← start
    rightBound ← end.next
    previous ← rightBound

    while current ≠ rightBound:
        next ← current.next
        current.next ← previous
        previous ← current
        current ← next

    return previous

function reverseKSegments(head, k):

    # If the list is empty, has only one node, or k is 1, no need to reverse segments
    if head is null OR head.next is null OR k = 1:
        return head

    # Start of the current segment to be reversed
    start ← head

    # Pointer to the last node of the previous segment
    leftBound ← null

    # Find the total number of segments in the linked list
    totalSegments ← findLength(head) ÷ k

    # Loop through the list to reverse every k-length segment
    for i ← 0 to totalSegments − 1:

        # Get the end node of the current segment
        end ← getNodeAtPosition(start, k)

        # Get the head of the reversed segment.
        reversedHead ← reverse(start, end)

        if leftBound is null:
            head ← reversedHead
        else:
            leftBound.next ← reversedHead

        # Update leftBound to the current segment's start (which is now the end after reversal)
        leftBound ← start

        # Move to the next segment
        start ← leftBound.next

    # Return the head of the modified list
    return head
```

```python run
from typing import Optional

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

class Solution:
    def find_length(self, head: Optional[ListNode]) -> int:
        length = 0
        while head is not None:
            length += 1
            head = head.next
        return length

    def get_node_at_position(
        self, head: ListNode, position: int
    ) -> ListNode:
        current = head
        for _ in range(1, position):
            current = current.next
        return current

    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = start
        right_bound: Optional[ListNode] = end.next
        previous: Optional[ListNode] = right_bound

        while current != right_bound:
            next_node = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def reverse_k_segments(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:

        # If the list is empty, has only one node, or k is 1, no need to
        # reverse segments
        if head is None or head.next is None or k == 1:
            return head

        # Start of the current segment to be reversed
        start = head

        # Pointer to the last node of the previous segment
        left_bound = None

        # Find the total number of segments in the linked list
        total_segments = self.find_length(head) // k

        # Loop through the list to reverse every k-length segment
        for i in range(total_segments):

            # Get the end node of the current segment
            end = self.get_node_at_position(start, k)

            # Get the head of the reversed segment.
            reversed_head = self.reverse(start, end)

            # Check if there is a previous segment to connect to or
            # if the existing head needs to be updated.
            # If left_bound is None, it means we're at the first segment
            # So, we need to update the head to the reversed_head
            # Return the new head
            if left_bound is None:
                head = reversed_head

            # If there is a left_bound, connect its next to the new
            # reversed_head
            else:
                left_bound.next = reversed_head

            # Update left_bound to the current segment's start (which is
            # now the end after reversal)
            left_bound = start

            # Move to the next segment
            start = left_bound.next

        # Return the head of the modified list
        return head

# Driver
def build(vals):
    dummy = ListNode(0)
    cur = dummy
    for v in vals:
        cur.next = ListNode(v)
        cur = cur.next
    return dummy.next

def to_list(head):
    res = []
    while head:
        res.append(head.val)
        head = head.next
    return res

head = build([5, 7, 3, 10, 6, 8])
print(to_list(Solution().reverse_k_segments(head, 3)))  # [3, 7, 5, 8, 6, 10]
```

```java run
public class Solution {
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int v) { val = v; }
        ListNode(int v, ListNode n) { val = v; next = n; }
    }

    static int findLength(ListNode head) {
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }

    static ListNode getNodeAtPosition(ListNode head, int position) {
        ListNode current = head;
        for (int i = 1; i < position; ++i) {
            current = current.next;
        }
        return current;
    }

    static ListNode reverse(ListNode start, ListNode end) {
        ListNode current = start;
        ListNode rightBound = end.next;
        ListNode previous = rightBound;

        while (current != rightBound) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        return previous;
    }

    static ListNode reverseKSegments(ListNode head, int k) {

        // If the list is empty, has only one node, or k is 1, no need to
        // reverse segments
        if (head == null || head.next == null || k == 1) {
            return head;
        }

        // Start of the current segment to be reversed
        ListNode start = head;

        // Pointer to the last node of the previous segment
        ListNode leftBound = null;

        // Find the total number of segments in the linked list
        int totalSegments = findLength(head) / k;

        // Loop through the list to reverse every k-length segment
        for (int i = 0; i < totalSegments; i++) {

            // Get the end node of the current segment
            ListNode end = getNodeAtPosition(start, k);

            // Get the head of the reversed segment.
            ListNode reversedHead = reverse(start, end);

            // Check if there is a previous segment to connect to or
            // if the existing head needs to be updated.
            // If leftBound is null, it means we're at the first
            // segment So, we need to update the head to the
            // reversedHead
            if (leftBound == null) {
                head = reversedHead;
            }

            // If there is a leftBound, connect its next to the new
            // reversedHead
            else {
                leftBound.next = reversedHead;
            }

            // Update leftBound to the current segment's start (which is
            // now the end after reversal)
            leftBound = start;

            // Move to the next segment
            start = leftBound.next;
        }

        // Return the head of the modified list
        return head;
    }

    static ListNode build(int[] vals) {
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        for (int v : vals) { cur.next = new ListNode(v); cur = cur.next; }
        return dummy.next;
    }

    static String toStr(ListNode head) {
        StringBuilder sb = new StringBuilder("[");
        while (head != null) {
            sb.append(head.val);
            if (head.next != null) sb.append(", ");
            head = head.next;
        }
        return sb.append("]").toString();
    }

    public static void main(String[] args) {
        ListNode head = build(new int[]{5, 7, 3, 10, 6, 8});
        System.out.println(toStr(reverseKSegments(head, 3)));  // [3, 7, 5, 8, 6, 10]
    }
}
```

```c run
#include <stdio.h>
#include <stdlib.h>

typedef struct ListNode {
    int val;
    struct ListNode *next;
} ListNode;

ListNode* newNode(int v) {
    ListNode *n = malloc(sizeof *n);
    n->val = v; n->next = NULL;
    return n;
}

int findLength(ListNode *head) {
    int length = 0;
    while (head != NULL) {
        length++;
        head = head->next;
    }
    return length;
}

ListNode* getNodeAtPosition(ListNode *head, int position) {
    ListNode *current = head;
    for (int i = 1; i < position; ++i) {
        current = current->next;
    }
    return current;
}

ListNode* reverse(ListNode *start, ListNode *end) {
    ListNode *current = start;
    ListNode *rightBound = end->next;
    ListNode *previous = rightBound;

    while (current != rightBound) {
        ListNode *next = current->next;
        current->next = previous;
        previous = current;
        current = next;
    }

    return previous;
}

ListNode* reverseKSegments(ListNode *head, int k) {

    /* If the list is empty, has only one node, or k is 1, no need to
       reverse segments */
    if (head == NULL || head->next == NULL || k == 1) {
        return head;
    }

    /* Start of the current segment to be reversed */
    ListNode *start = head;

    /* Pointer to the last node of the previous segment */
    ListNode *leftBound = NULL;

    /* Find the total number of segments in the linked list */
    int totalSegments = findLength(head) / k;

    /* Loop through the list to reverse every k-length segment */
    for (int i = 0; i < totalSegments; i++) {

        /* Get the end node of the current segment */
        ListNode *end = getNodeAtPosition(start, k);

        /* Get the head of the reversed segment. */
        ListNode *reversedHead = reverse(start, end);

        /* Check if there is a previous segment to connect to or
           if the existing head needs to be updated.
           If leftBound is null, it means we're at the first
           segment So, we need to update the head to the
           reversedHead */
        if (leftBound == NULL) {
            head = reversedHead;
        }

        /* If there is a leftBound, connect its next to the new
           reversedHead */
        else {
            leftBound->next = reversedHead;
        }

        /* Update leftBound to the current segment's start (which is
           now the end after reversal) */
        leftBound = start;

        /* Move to the next segment */
        start = leftBound->next;
    }

    /* Return the head of the modified list */
    return head;
}

void printList(ListNode *head) {
    printf("[");
    while (head) {
        printf("%d", head->val);
        if (head->next) printf(", ");
        head = head->next;
    }
    printf("]\n");
}

int main() {
    ListNode *head = newNode(5);
    head->next = newNode(7);
    head->next->next = newNode(3);
    head->next->next->next = newNode(10);
    head->next->next->next->next = newNode(6);
    head->next->next->next->next->next = newNode(8);

    printList(reverseKSegments(head, 3));  /* [3, 7, 5, 8, 6, 10] */
    return 0;
}
```

```scala run
class ListNode(var v: Int, var next: ListNode = null)

object Solution {
  def findLength(head: ListNode): Int = {
    var length = 0
    var cur = head
    while (cur != null) {
      length += 1
      cur = cur.next
    }
    length
  }

  def getNodeAtPosition(head: ListNode, position: Int): ListNode = {
    var current = head
    var i = 1
    while (i < position) {
      current = current.next
      i += 1
    }
    current
  }

  def reverse(start: ListNode, end: ListNode): ListNode = {
    var current = start
    val rightBound = end.next
    var previous = rightBound

    while (current != rightBound) {
      val next = current.next
      current.next = previous
      previous = current
      current = next
    }

    previous
  }

  def reverseKSegments(headIn: ListNode, k: Int): ListNode = {

    // If the list is empty, has only one node, or k is 1, no need to
    // reverse segments
    if (headIn == null || headIn.next == null || k == 1) {
      return headIn
    }

    var head = headIn

    // Start of the current segment to be reversed
    var start = head

    // Pointer to the last node of the previous segment
    var leftBound: ListNode = null

    // Find the total number of segments in the linked list
    val totalSegments = findLength(head) / k

    // Loop through the list to reverse every k-length segment
    for (_ <- 0 until totalSegments) {

      // Get the end node of the current segment
      val end = getNodeAtPosition(start, k)

      // Get the head of the reversed segment.
      val reversedHead = reverse(start, end)

      // Check if there is a previous segment to connect to or
      // if the existing head needs to be updated.
      // If leftBound is null, it means we're at the first
      // segment So, we need to update the head to the
      // reversedHead
      if (leftBound == null) {
        head = reversedHead
      }

      // If there is a leftBound, connect its next to the new
      // reversedHead
      else {
        leftBound.next = reversedHead
      }

      // Update leftBound to the current segment's start (which is
      // now the end after reversal)
      leftBound = start

      // Move to the next segment
      start = leftBound.next
    }

    // Return the head of the modified list
    head
  }

  def build(vals: List[Int]): ListNode = {
    val dummy = new ListNode(0); var cur = dummy
    for (v <- vals) { cur.next = new ListNode(v); cur = cur.next }
    dummy.next
  }

  def toStr(head: ListNode): String = {
    val sb = new StringBuilder("["); var cur = head
    while (cur != null) { sb.append(cur.v); if (cur.next != null) sb.append(", "); cur = cur.next }
    sb.append("]").toString
  }

  def main(args: Array[String]): Unit = {
    val head = build(List(5, 7, 3, 10, 6, 8))
    println(toStr(reverseKSegments(head, 3)))  // [3, 7, 5, 8, 6, 10]
  }
}
```


***

# Reverse increasing groups

## Problem Statement

Given the **head** of a singly linked list, write a function to reverse the list in groups of increasing size. The first group has size `1`, the next group size `2`, then `3`, and so on. Return the head of the reversed list.

If, at the end, the length of the remaining list is less than the required group size, do not reverse that part of the list.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10, 6, 8\]
> -   **Output:** \[5, 3, 7, 8, 6, 10\]
> -   **Explanation:** We get the above list by reversing the first three groups of sizes 1, 2 and 3, respectively.

### Example 2

> -   **Input:** head = \[5, 7, 3, 10, 6\]
> -   **Output:** \[5, 3, 7, 10, 6\]
> -   **Explanation:** We get the above list b reversing the first two groups of sizes 1 and 2. Since the remaining nodes are fewer than the next group size (3), they remain unreversed.

### Example 3

> -   **Input:** head = \[5\]
> -   **Output:** \[5\]
> -   **Explanation:** We get the above list by reversing the first group of size 1.

## Solution


```pseudocode
# Reverse the first 1, then next 2, then next 3, … nodes. Stop when the list is too short for the next group.
function findLength(head):
    length ← 0
    while head is not null:
        length ← length + 1
        head ← head.next
    return length

function getNodeAtPosition(head, position):
    current ← head
    for i ← 1 to position − 1:
        current ← current.next
    return current

function reverse(start, end):
    current ← start
    rightBound ← end.next
    previous ← rightBound

    while current ≠ rightBound:
        next ← current.next
        current.next ← previous
        previous ← current
        current ← next

    return previous

function reverseIncreasingGroups(head):

    # If the list is empty or has only one node, no need to reverse segments
    if head is null OR head.next is null:
        return head

    # Start of the current segment to be reversed
    start ← head

    # Pointer to the last node of the previous segment
    leftBound ← null

    # Find the length of the linked list
    length ← findLength(head)

    # Start with a group size of 1
    groupSize ← 1

    # Loop through the list to reverse segments of increasing size
    while length ≥ groupSize:

        # Get the end node of the current segment
        end ← getNodeAtPosition(start, groupSize)

        # Get the head of the reversed segment.
        reversedHead ← reverse(start, end)

        if leftBound is null:
            head ← reversedHead
        else:
            leftBound.next ← reversedHead

        # Update leftBound to the current segment's start (which is now the end after reversal)
        leftBound ← start

        # Move to the next segment
        start ← leftBound.next

        # Decrement the remaining length by the size of the current group
        length ← length − groupSize

        # Increment groupSize for the next segment
        groupSize ← groupSize + 1

    # Return the head of the modified list
    return head
```

```python run
from typing import Optional

class Solution:
    def find_length(self, head: Optional[ListNode]) -> int:
        length = 0
        while head is not None:
            length += 1
            head = head.next
        return length

    def get_node_at_position(
        self, head: ListNode, position: int
    ) -> ListNode:
        current = head
        for _ in range(1, position):
            current = current.next
        return current

    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = start
        right_bound: Optional[ListNode] = end.next
        previous: Optional[ListNode] = right_bound

        while current != right_bound:
            next_node = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def reverse_increasing_groups(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the list is empty or has only one node, no need to
        # reverse segments
        if head is None or head.next is None:
            return head

        # Start of the current segment to be reversed
        start = head

        # Pointer to the last node of the previous segment
        left_bound = None

        # Find the length of the linked list
        length = self.find_length(head)

        # Start with a group size of 1
        group_size = 1

        # Loop through the list to reverse segments of increasing size
        while length >= group_size:

            # Get the end node of the current segment
            end = self.get_node_at_position(start, group_size)

            # Get the head of the reversed segment.
            reversed_head = self.reverse(start, end)

            # Check if there is a previous segment to connect to or
            # if the existing head needs to be updated.
            # If left_bound is None, it means we're at the first segment
            # So, we need to update the head to the reversed_head
            # Return the new head
            if left_bound is None:
                head = reversed_head

            # If there is a left_bound, connect its next to the new
            # reversed_head
            else:
                left_bound.next = reversed_head

            # Update left_bound to the current segment's start (which is
            # now the end after reversal)
            left_bound = start

            # Move to the next segment
            start = left_bound.next

            # Decrement the remaining length by the size of the current
            # group
            length -= group_size

            # Increment group_size for the next segment
            group_size += 1

        # Return the head of the modified list
        return head
```

```java run
class Solution {
    private int findLength(ListNode head) {
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }

    private ListNode getNodeAtPosition(ListNode head, int position) {
        ListNode current = head;
        for (int i = 1; i < position; ++i) {
            current = current.next;
        }
        return current;
    }

    private ListNode reverse(ListNode start, ListNode end) {
        ListNode current = start;
        ListNode rightBound = end.next;
        ListNode previous = rightBound;

        while (current != rightBound) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        return previous;
    }

    public ListNode reverseIncreasingGroups(ListNode head) {

        // If the list is empty or has only one node, no need to
        // reverse segments
        if (head == null || head.next == null) {
            return head;
        }

        // Start of the current segment to be reversed
        ListNode start = head;

        // Pointer to the last node of the previous segment
        ListNode leftBound = null;

        // Find the length of the linked list
        int length = findLength(head);

        // Start with a group size of 1
        int groupSize = 1;

        // Loop through the list to reverse segments of increasing size
        while (length >= groupSize) {

            // Get the end node of the current segment
            ListNode end = getNodeAtPosition(start, groupSize);

            // Get the head of the reversed segment.
            ListNode reversedHead = reverse(start, end);

            // Check if there is a previous segment to connect to or
            // if the existing head needs to be updated.
            // If leftBound is null, it means we're at the first
            // segment So, we need to update the head to the
            // reversedHead
            if (leftBound == null) {
                head = reversedHead;
            }

            // If there is a leftBound, connect its next to the new
            // reversedHead
            else {
                leftBound.next = reversedHead;
            }

            // Update leftBound to the current segment's start (which is
            // now the end after reversal)
            leftBound = start;

            // Move to the next segment
            start = leftBound.next;

            // Decrement the remaining length by the size of the current
            // group
            length -= groupSize;

            // increment groupSize for the next segment
            groupSize++;
        }

        // Return the head of the modified list
        return head;
    }
}
```

```c run
ListNode* reverseIncreasingGroups(ListNode *head) {

    /* If the list is empty or has only one node, no need to
       reverse segments */
    if (head == NULL || head->next == NULL) {
        return head;
    }

    /* Start of the current segment to be reversed */
    ListNode *start = head;

    /* Pointer to the last node of the previous segment */
    ListNode *leftBound = NULL;

    /* Find the length of the linked list */
    int length = findLength(head);

    /* Start with a group size of 1 */
    int groupSize = 1;

    /* Loop through the list to reverse segments of increasing size */
    while (length >= groupSize) {

        /* Get the end node of the current segment */
        ListNode *end = getNodeAtPosition(start, groupSize);

        /* Get the head of the reversed segment. */
        ListNode *reversedHead = reverse(start, end);

        /* Check if there is a previous segment to connect to or
           if the existing head needs to be updated.
           If leftBound is null, it means we're at the first
           segment So, we need to update the head to the
           reversedHead */
        if (leftBound == NULL) {
            head = reversedHead;
        }

        /* If there is a leftBound, connect its next to the new
           reversedHead */
        else {
            leftBound->next = reversedHead;
        }

        /* Update leftBound to the current segment's start (which is
           now the end after reversal) */
        leftBound = start;

        /* Move to the next segment */
        start = leftBound->next;

        /* Decrement the remaining length by the size of the current
           group */
        length -= groupSize;

        /* increment groupSize for the next segment */
        groupSize++;
    }

    /* Return the head of the modified list */
    return head;
}
```

```scala run
object Solution {
  private def findLength(head: ListNode): Int = {
    var length = 0; var cur = head
    while (cur != null) { length += 1; cur = cur.next }
    length
  }

  private def getNodeAtPosition(head: ListNode, position: Int): ListNode = {
    var current = head
    var i = 1
    while (i < position) { current = current.next; i += 1 }
    current
  }

  private def reverse(start: ListNode, end: ListNode): ListNode = {
    var current = start
    val rightBound = end.next
    var previous = rightBound
    while (current != rightBound) {
      val next = current.next
      current.next = previous
      previous = current
      current = next
    }
    previous
  }

  def reverseIncreasingGroups(headIn: ListNode): ListNode = {

    // If the list is empty or has only one node, no need to
    // reverse segments
    if (headIn == null || headIn.next == null) {
      return headIn
    }

    var head = headIn

    // Start of the current segment to be reversed
    var start = head

    // Pointer to the last node of the previous segment
    var leftBound: ListNode = null

    // Find the length of the linked list
    var length = findLength(head)

    // Start with a group size of 1
    var groupSize = 1

    // Loop through the list to reverse segments of increasing size
    while (length >= groupSize) {

      // Get the end node of the current segment
      val end = getNodeAtPosition(start, groupSize)

      // Get the head of the reversed segment.
      val reversedHead = reverse(start, end)

      // Check if there is a previous segment to connect to or
      // if the existing head needs to be updated.
      // If leftBound is null, it means we're at the first
      // segment So, we need to update the head to the
      // reversedHead
      if (leftBound == null) {
        head = reversedHead
      }

      // If there is a leftBound, connect its next to the new
      // reversedHead
      else {
        leftBound.next = reversedHead
      }

      // Update leftBound to the current segment's start (which is
      // now the end after reversal)
      leftBound = start

      // Move to the next segment
      start = leftBound.next

      // Decrement the remaining length by the size of the current
      // group
      length -= groupSize

      // increment groupSize for the next segment
      groupSize += 1
    }

    // Return the head of the modified list
    head
  }
}
```


***

# Reverse alternate segments

## Problem Statement

Given the **head** of a singly linked list and a positive integer **k**, write a function to reverse alternate k nodes in the list and return the head of the reversed list.

If, at the end, the length of the remaining list is less than k, do not reverse that part of the list.

### Example 1

> -   **Input:** head = \[5, 7, 3, 10, 6, 8\], k = 2
> -   **Output:** \[7, 5, 3, 10, 8, 6\]
> -   **Explanation:** Since the value of k is 2, we reverse every alternate two nodes from the start, i.e., we reverse the first two nodes, skip the next two, and repeat the same pattern till the end of the list.

### Example 2

> -   **Input:** head = \[5, 7, 3, 10, 6\], k = 3
> -   **Output:** \[3, 7, 5, 10, 6\]
> -   **Explanation:** Since the value of k is 3, we reverse every three nodes from the start, i.e., we reverse the first three nodes, skip the next three, and repeat the same pattern till the end of the list.

### Example 3

> -   **Input:** head = \[5, 7, 3, 10, 6\], k = 8
> -   **Output:** \[5, 7, 3, 10, 6\]
> -   **Explanation:** Since the value of k is 8, we cannot reverse any part of the list as the size of the entire list is 6, which is less than 8.

## Solution


```pseudocode
# Reverse only the alternate (1st, 3rd, 5th, ...) k-segments. Toggle a flag each iteration.
function findLength(head):
    length ← 0
    while head is not null:
        length ← length + 1
        head ← head.next
    return length

function getNodeAtPosition(head, position):
    current ← head
    for i ← 1 to position − 1:
        current ← current.next
    return current

function reverse(start, end):
    current ← start
    rightBound ← end.next
    previous ← rightBound

    while current ≠ rightBound:
        next ← current.next
        current.next ← previous
        previous ← current
        current ← next

    return previous

function reverseAlternateSegments(head, k):

    # If the list is empty, has only one node, or k is 1, no need to reverse segments
    if head is null OR head.next is null OR k = 1:
        return head

    # Flag to determine whether to reverse the current segment
    shouldReverse ← true

    # Start of the current segment to be reversed
    start ← head

    # Pointer to the last node of the previous segment
    leftBound ← null

    # Find the total number of segments in the linked list
    totalSegments ← findLength(head) ÷ k

    # Loop through the list to reverse every alternate k-length segment
    for i ← 0 to totalSegments − 1:

        # Get the end node of the current segment
        end ← getNodeAtPosition(start, k)

        # Reverse the current segment if the flag is set
        if shouldReverse:

            # Get the head of the reversed segment.
            reversedHead ← reverse(start, end)

            if leftBound is null:
                head ← reversedHead
            else:
                leftBound.next ← reversedHead

            # Update leftBound to the current segment's start (which is now the end after reversal)
            leftBound ← start

        # If the flag is not set, skip reversing the current segment
        else:

            # Skip reversing this segment, move leftBound to the end of the segment
            leftBound ← end

        # Move to the next segment
        start ← leftBound.next

        # Toggle the flag for the next segment
        shouldReverse ← NOT shouldReverse

    # Return the new head of the list
    return head
```

```python run
from typing import Optional

class Solution:
    def find_length(self, head: Optional[ListNode]) -> int:
        length = 0
        while head is not None:
            length += 1
            head = head.next
        return length

    def get_node_at_position(
        self, head: Optional[ListNode], position: int
    ) -> Optional[ListNode]:
        current = head
        for i in range(1, position):
            current = current.next
        return current

    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = start
        right_bound: Optional[ListNode] = end.next
        previous: Optional[ListNode] = right_bound

        while current != right_bound:
            next_node = current.next
            current.next = previous
            previous = current
            current = next_node

        return previous

    def reverse_alternate_segments(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:

        # If the list is empty, has only one node, or k is 1, no need to
        # reverse segments
        if head is None or head.next is None or k == 1:
            return head

        # Flag to determine whether to reverse the current segment
        should_reverse = True

        # Start of the current segment to be reversed
        start: Optional[ListNode] = head

        # Pointer to the last node of the previous segment
        left_bound: Optional[ListNode] = None

        # Find the total number of segments in the linked list
        total_segments = self.find_length(head) // k

        # Loop through the list to reverse every alternate k-length
        # segment
        for i in range(total_segments):

            # Get the end node of the current segment
            end = self.get_node_at_position(start, k)

            # Reverse the current segment if the flag is set
            if should_reverse:

                # Get the head of the reversed segment.
                reversed_head = self.reverse(start, end)

                # Check if there is a previous segment to connect to or
                # if the existing head needs to be updated.
                # If left_bound is None, it means we're at the first
                # segment So, we need to update the head to the
                # reversed_head
                if left_bound is None:
                    head = reversed_head

                # If there is a left_bound, connect its next to the
                # new reversed_head
                else:
                    left_bound.next = reversed_head

                # Update left_bound to the current segment's start (which
                # is now the end after reversal)
                left_bound = start

            # If the flag is not set, skip reversing the current segment
            else:

                # Skip reversing this segment, move left_bound to the end
                # of the segment
                left_bound = end

            # Move to the next segment
            start = left_bound.next

            # Toggle the flag for the next segment
            should_reverse = not should_reverse

        # Return the new head of the list
        return head
```

```java run
class Solution {
    private int findLength(ListNode head) {
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }

    private ListNode getNodeAtPosition(ListNode head, int position) {
        ListNode current = head;
        for (int i = 1; i < position; ++i) {
            current = current.next;
        }
        return current;
    }

    private ListNode reverse(ListNode start, ListNode end) {
        ListNode current = start;
        ListNode rightBound = end.next;
        ListNode previous = rightBound;

        while (current != rightBound) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        return previous;
    }

    public ListNode reverseAlternateSegments(ListNode head, int k) {

        // If the list is empty, has only one node, or k is 1, no need to
        // reverse segments
        if (head == null || head.next == null || k == 1) {
            return head;
        }

        // Flag to determine whether to reverse the current segment
        boolean shouldReverse = true;

        // Start of the current segment to be reversed
        ListNode start = head;

        // Pointer to the last node of the previous segment
        ListNode leftBound = null;

        // Find the total number of segments in the linked list
        int totalSegments = findLength(head) / k;

        // Loop through the list to reverse every alternate k-length
        // segment
        for (int i = 0; i < totalSegments; i++) {

            // Get the end node of the current segment
            ListNode end = getNodeAtPosition(start, k);

            // Reverse the current segment if the flag is set
            if (shouldReverse) {

                // Get the head of the reversed segment.
                ListNode reversedHead = reverse(start, end);

                // Check if there is a previous segment to connect to or
                // if the existing head needs to be updated.
                // If leftBound is null, it means we're at the first
                // segment So, we need to update the head to the
                // reversedHead
                if (leftBound == null) {
                    head = reversedHead;
                }

                // If there is a leftBound, connect its next to the
                // new reversedHead
                else {
                    leftBound.next = reversedHead;
                }

                // Update leftBound to the current segment's start (which
                // is now the end after reversal)
                leftBound = start;
            }

            // If the flag is not set, skip reversing the current segment
            else {

                // Skip reversing this segment, move leftBound to the end
                // of the segment
                leftBound = end;
            }

            // Move to the next segment
            start = leftBound.next;

            // Toggle the flag for the next segment
            shouldReverse = !shouldReverse;
        }

        // Return the new head of the list
        return head;
    }
}
```

```c run
ListNode* reverseAlternateSegments(ListNode *head, int k) {

    /* If the list is empty, has only one node, or k is 1, no need to
       reverse segments */
    if (head == NULL || head->next == NULL || k == 1) {
        return head;
    }

    /* Flag to determine whether to reverse the current segment. */
    int shouldReverse = 1;

    /* Start of the current segment to be reversed */
    ListNode *start = head;

    /* Pointer to the last node of the previous segment */
    ListNode *leftBound = NULL;

    /* Find the total number of segments in the linked list */
    int totalSegments = findLength(head) / k;

    /* Loop through the list to reverse every alternate k-length
       segment */
    for (int i = 0; i < totalSegments; i++) {

        /* Get the end node of the current segment */
        ListNode *end = getNodeAtPosition(start, k);

        /* Reverse the current segment if the flag is set. */
        if (shouldReverse) {

            /* Get the head of the reversed segment. */
            ListNode *reversedHead = reverse(start, end);

            /* Check if there is a previous segment to connect to or
               if the existing head needs to be updated.
               If leftBound is null, it means we're at the first
               segment So, we need to update the head to the
               reversedHead */
            if (leftBound == NULL) {
                head = reversedHead;
            }

            /* If there is a leftBound, connect its next to the
               new reversedHead */
            else {
                leftBound->next = reversedHead;
            }

            /* Update leftBound to the current segment's start (which
               is now the end after reversal) */
            leftBound = start;
        }

        /* If the flag is not set, skip reversing the current segment */
        else {

            /* Skip reversing this segment, move leftBound to the end
               of the segment. */
            leftBound = end;
        }

        /* Move to the next segment */
        start = leftBound->next;

        /* Toggle the flag for the next segment */
        shouldReverse = !shouldReverse;
    }

    /* Return the new head of the list. */
    return head;
}
```

```scala run
object Solution {
  private def findLength(head: ListNode): Int = {
    var length = 0; var cur = head
    while (cur != null) { length += 1; cur = cur.next }
    length
  }

  private def getNodeAtPosition(head: ListNode, position: Int): ListNode = {
    var current = head
    var i = 1
    while (i < position) { current = current.next; i += 1 }
    current
  }

  private def reverse(start: ListNode, end: ListNode): ListNode = {
    var current = start
    val rightBound = end.next
    var previous = rightBound

    while (current != rightBound) {
      val next = current.next
      current.next = previous
      previous = current
      current = next
    }

    previous
  }

  def reverseAlternateSegments(headIn: ListNode, k: Int): ListNode = {

    // If the list is empty, has only one node, or k is 1, no need to
    // reverse segments
    if (headIn == null || headIn.next == null || k == 1) {
      return headIn
    }

    var head = headIn

    // Flag to determine whether to reverse the current segment
    var shouldReverse = true

    // Start of the current segment to be reversed
    var start = head

    // Pointer to the last node of the previous segment
    var leftBound: ListNode = null

    // Find the total number of segments in the linked list
    val totalSegments = findLength(head) / k

    // Loop through the list to reverse every alternate k-length
    // segment
    for (_ <- 0 until totalSegments) {

      // Get the end node of the current segment
      val end = getNodeAtPosition(start, k)

      // Reverse the current segment if the flag is set
      if (shouldReverse) {

        // Get the head of the reversed segment.
        val reversedHead = reverse(start, end)

        // Check if there is a previous segment to connect to or
        // if the existing head needs to be updated.
        // If leftBound is null, it means we're at the first
        // segment So, we need to update the head to the
        // reversedHead
        if (leftBound == null) {
          head = reversedHead
        }

        // If there is a leftBound, connect its next to the
        // new reversedHead
        else {
          leftBound.next = reversedHead
        }

        // Update leftBound to the current segment's start (which
        // is now the end after reversal)
        leftBound = start
      }

      // If the flag is not set, skip reversing the current segment
      else {

        // Skip reversing this segment, move leftBound to the end
        // of the segment
        leftBound = end
      }

      // Move to the next segment
      start = leftBound.next

      // Toggle the flag for the next segment
      shouldReverse = !shouldReverse
    }

    // Return the new head of the list
    head
  }
}
```


***

## Final Takeaway

Reversal-as-subroutine follows a universal recipe:

1. **Decide the window.** Based on the problem: first K, last K, groups of K, alternate segments, a named range. This is the only step that varies between problems.
2. **Track four boundaries.** `leftBound` (predecessor of segment head), `start`, `end`, `rightBound` (successor of segment tail). The two outer pointers exist purely so you can re-attach after reversing.
3. **Invoke the segment-reversal primitive.** Same six-line loop from lesson 6, just with `previous = rightBound` as the initial value so the reversed segment's tail automatically points at the right successor.
4. **Stitch the seams.** `leftBound.next = new_segment_head`. If this was the first segment, also update the list's `head` pointer.
5. **Slide the boundaries.** Old `start` (now the segment's tail) becomes new `leftBound`. Advance `start` to the next window's head. Repeat.

Four insights worth burning in:

| Insight | Why it matters |
|---|---|
| Reversal composes | Pairwise swap, reverse-K, reorder, palindrome — all built from segment reversal. |
| Always cache `rightBound` BEFORE reversing | The instant you start flipping pointers inside the segment, the link to the rest of the list disappears. |
| A sentinel "dummy head" simplifies edge cases | Placing a dummy node before the real head gives every segment (even the first) a proper `leftBound`, eliminating the "is this the first group?" special case. |
| One O(n) pass is enough | Even with all the boundary dance, each node is visited a constant number of times. The whole family of problems stays O(n). |

When you next see "reverse in groups", "swap pairs", "reverse alternate K", or "reverse a range [i, j]" — reach for the four-boundary pattern. The reversal loop writes itself; the only question is where to put the seams.

> **Transfer Challenge:** Reverse only the **odd-indexed** groups of K in a list (leaving even-indexed groups in their original order). E.g., for `[1, 2, 3, 4, 5, 6, 7, 8]` with `k = 2`, the odd groups `(1, 2)` and `(5, 6)` reverse to `(2, 1)` and `(6, 5)` while `(3, 4)` and `(7, 8)` stay put → output `[2, 1, 3, 4, 6, 5, 7, 8]`.
>
> <details><summary><strong>Solution hint</strong></summary>
>
> Maintain a boolean <code>should_reverse</code> that flips each group. On "reverse" groups run the segment-reversal primitive; on "skip" groups just advance <code>leftBound</code> and <code>start</code> by <code>k</code> without touching any pointers inside. Same O(n), same four-boundary dance — just a conditional around step 3.
>
> </details>
