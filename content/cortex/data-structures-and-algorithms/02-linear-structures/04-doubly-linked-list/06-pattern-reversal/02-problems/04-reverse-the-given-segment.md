---
title: "Reverse the Given Segment"
summary: "Given the head of a doubly linked list and two integers left and right with left ≤ right, reverse the nodes from position left to position right (1-indexed) and return the head of the resulting list."
prereqs:
  - 06-pattern-reversal/01-pattern
difficulty: medium
---

# Reverse the given segment

## The Problem

> Given the **head** of a doubly linked list and two integers **left** and **right** with **left ≤ right**, reverse the nodes from position **left** to position **right** (1-indexed) and return the head of the resulting list.

```
Example 1:
  Input:  head = [5, 7, 3, 10, 6], left = 2, right = 4
  Output: [5, 10, 3, 7, 6]
  Explanation: positions 2..4 were [7, 3, 10] → reversed to [10, 3, 7].

Example 2:
  Input:  head = [5], left = 1, right = 1
  Output: [5]
  Explanation: a single-node segment is its own reverse.
```

This is the most general direct application — a segment between two arbitrary positions. The work splits cleanly:

1. Walk to position `left` and capture it as `start`.
2. Walk to position `right` and capture it as `end`.
3. Call the segment-reversal primitive on `(start, end)`.
4. If `left == 1`, the new head of the list is `end` (because the reversed segment includes the original head). Otherwise, the head is unchanged.

The fourth step is the only spot where the head reference can shift, so it's the only spot where we have to think carefully about the return value.

<details>
<summary><h2>The Solution</h2></summary>



```python run viz=linked-list viz-root=head
from typing import Optional

class ListNode:
    def __init__(self, val=0, prev=None, nxt=None):
        self.val = val
        self.prev = prev
        self.next = nxt


def from_list(values):
    if not values:
        return None
    head = ListNode(values[0])
    cur = head
    for v in values[1:]:
        node = ListNode(v, prev=cur)
        cur.next = node
        cur = node
    return head


def to_list(head):
    out = []
    while head is not None:
        out.append(head.val)
        head = head.next
    return out


class Solution:
    def get_node_at_position(
        self, head: Optional[ListNode], position: int
    ) -> Optional[ListNode]:
        current: Optional[ListNode] = head
        for _ in range(1, position):
            if current is None:
                break
            current = current.next
        return current

    def reverse(
        self, start: Optional[ListNode], end: Optional[ListNode]
    ) -> None:

        # If the start is null or start is the end, there's nothing to
        # reverse
        if start is None or start == end:
            return

        # Pointers to keep track of the bounds
        left_bound = start.prev
        right_bound = end.next
        current = start
        previous = left_bound

        # Reverse nodes until the right boundary
        while current != right_bound:

            # Save the address of next node
            next_node = current.next

            # Swap the previous and next nodes pointers of the current
            # node
            current.prev, current.next = current.next, current.prev

            # Store the previous node in the previous pointer
            previous = current

            # Move the current pointer to the next node
            current = next_node

        # Adjust connections with the new boundaries
        start.next = right_bound
        if right_bound:
            right_bound.prev = start

        end.prev = left_bound
        if left_bound:
            left_bound.next = end

    def reverse_the_given_segment(
        self, head: Optional[ListNode], left: int, right: int
    ) -> Optional[ListNode]:

        # Handle cases where reversal is not needed
        if head is None or head.next is None or left == right:
            return head

        # Get the node at the 'left' position
        start: Optional[ListNode] = self.get_node_at_position(head, left)

        # Get the node at the 'right' position
        end: Optional[ListNode] = self.get_node_at_position(head, right)

        # Reverse the segment between start and end
        self.reverse(start, end)

        # Return the new head if the reversal included the head node
        return end if left == 1 else head


# Examples from the problem statement
head = from_list([5, 7, 3, 10, 6])
print(to_list(Solution().reverse_the_given_segment(head, 2, 4)))  # [5, 10, 3, 7, 6]

head = from_list([5])
print(to_list(Solution().reverse_the_given_segment(head, 1, 1)))  # [5]

# Edge cases
head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_the_given_segment(head, 1, 5)))  # [5, 4, 3, 2, 1]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_the_given_segment(head, 1, 3)))  # [3, 2, 1, 4, 5]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_the_given_segment(head, 3, 5)))  # [1, 2, 5, 4, 3]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_the_given_segment(head, 2, 2)))  # [1, 2, 3, 4, 5]

head = from_list([1, 2])
print(to_list(Solution().reverse_the_given_segment(head, 1, 2)))  # [2, 1]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_the_given_segment(head, 2, 4)))  # [1, 4, 3, 2, 5]
```

```java run
import java.util.*;

public class Main {
    static class ListNode {
        int val;
        ListNode prev;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
    }

    static ListNode fromList(int... values) {
        if (values.length == 0) return null;
        ListNode head = new ListNode(values[0]);
        ListNode cur = head;
        for (int i = 1; i < values.length; i++) {
            ListNode node = new ListNode(values[i]);
            node.prev = cur;
            cur.next = node;
            cur = node;
        }
        return head;
    }

    static java.util.List<Integer> toList(ListNode head) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out;
    }

    static class Solution {
        private ListNode getNodeAtPosition(ListNode head, int position) {
            ListNode current = head;
            for (int i = 1; i < position; ++i) {
                current = current.next;
            }
            return current;
        }

        private void reverse(ListNode start, ListNode end) {

            // If the start is null or start is the end, there's nothing to
            // reverse
            if (start == null || start == end) {
                return;
            }

            // Pointers to keep track of the bounds
            ListNode leftBound = start.prev;
            ListNode rightBound = end.next;
            ListNode current = start;
            ListNode previous = leftBound;

            // Reverse nodes until the right boundary
            while (current != rightBound) {

                // Save the address of next node
                ListNode next = current.next;

                // Swap the previous and next nodes pointers of the current
                // node
                ListNode temp = current.prev;
                current.prev = current.next;
                current.next = temp;

                // Store the previous node in the previous pointer
                previous = current;

                // Move the current pointer to the next node
                current = next;
            }

            // Adjust connections with the new boundaries
            start.next = rightBound;
            if (rightBound != null) {
                rightBound.prev = start;
            }

            end.prev = leftBound;
            if (leftBound != null) {
                leftBound.next = end;
            }
        }

        public ListNode reverseTheGivenSegment(
            ListNode head,
            int left,
            int right
        ) {

            // Handle cases where reversal is not needed
            if (head == null || head.next == null || left == right) {
                return head;
            }

            // Get the node at the 'left' position
            ListNode start = getNodeAtPosition(head, left);

            // Get the node at the 'right' position
            ListNode end = getNodeAtPosition(head, right);

            // Reverse the segment between start and end
            reverse(start, end);

            // Return the new head if the reversal included the head node
            return left == 1 ? end : head;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(5, 7, 3, 10, 6), 2, 4)));  // [5, 10, 3, 7, 6]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(5), 1, 1)));               // [5]

        // Edge cases
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2, 3, 4, 5), 1, 5)));  // [5, 4, 3, 2, 1]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2, 3, 4, 5), 1, 3)));  // [3, 2, 1, 4, 5]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2, 3, 4, 5), 3, 5)));  // [1, 2, 5, 4, 3]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2, 3, 4, 5), 2, 2)));  // [1, 2, 3, 4, 5]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2), 1, 2)));            // [2, 1]
        System.out.println(toList(new Solution().reverseTheGivenSegment(fromList(1, 2, 3, 4, 5), 2, 4)));  // [1, 4, 3, 2, 5]
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10, 6], left = 2, right = 4</strong></summary>

```
Setup: left = 2, right = 4, left != right → proceed
  start = get_node_at_position(head, left = 2)  = node(7)
  end   = get_node_at_position(head, right = 4) = node(10)

reverse(start = 7, end = 10):
  left_bound  = start.prev = node(5)   right_bound = end.next = node(6)
  current = 7   │ next_node = 3   │ swap 7:  prev 3, next 5    │ current = 3
  current = 3   │ next_node = 10  │ swap 3:  prev 10, next 7   │ current = 10
  current = 10  │ next_node = 6   │ swap 10: prev 6, next 3    │ current = 6
  current == right_bound (6). Stop.
  Stitch tail:  start.next = right_bound → 7.next = node(6); right_bound.prev = start → 6.prev = node(7)
  Stitch head:  end.prev = left_bound   → 10.prev = node(5); left_bound.next = end  → 5.next = node(10)

left == 1 is false → return original head (5).

Result: 5 ⇄ 10 ⇄ 3 ⇄ 7 ⇄ 6  ✓
```

Phase 1 swaps `prev`/`next` on every interior node; phase 2 then re-stitches all four boundary pointers — `start.next`/`right_bound.prev` on the right and `end.prev`/`left_bound.next` on the left — so both directions stay consistent. Capturing `left_bound` and `right_bound` *before* the swaps begin is what keeps the algorithm bug-free.

</details>

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Reversal in a doubly linked list is a one-line idea — **swap `prev` and `next` on every node in the segment, then re-stitch the boundaries** — wrapped in a thin O(N) walk. Compared to a singly linked list, where you needed three pointers locked in a delicate dance, the DLL version is barely an algorithm; it's a habit. The same primitive solves four problems we just saw and a much longer list of indirect ones (rotate-by-K, reorder, palindrome check, undo-stacks, k-group rewinds). Once you recognise a problem as a "reverse this slice" problem, the implementation writes itself.

The two pieces of discipline to internalise:

1. **Save before clobber.** Capture `leftBound` and `rightBound` *before* any swap. Once swaps begin, the meanings of `start.prev` and `end.next` shift under your feet.
2. **Mirror every link.** Every connection in a DLL is two pointers. Set both, every time, or backward traversal silently breaks.

> **Transfer challenge** — Given the head of a doubly linked list and a positive integer K, reverse every consecutive group of K nodes (the last group may be shorter than K and should be reversed as well). Return the head. *Hint: you already have all the pieces.*

<details>
<summary><strong>Solution sketch</strong></summary>

Walk the list. At each step, locate `start` (current group's first node) and `end` (current group's K-th node, or the actual tail if fewer than K nodes remain). Apply the segment-reversal primitive on `(start, end)`. Track whether the very first group included the original head — that group's reversal returns the new head of the entire list. Move `current` to `start.next` (which is now the first node of the next group, post-stitch) and repeat. Each node is touched O(1) times across the whole algorithm, so the total cost is O(N).

The only new thing here is the *bookkeeping*, not the reversal itself — you've already seen that primitive four times.

</details>

Next time you see a problem that asks you to flip a chunk of a linked list — any chunk, anywhere — you won't reach for three pointers and a stack of temporary variables. You'll reach for one swap, one walk, and four boundary stitches. The reversal pattern stops being a trick and becomes a reflex.

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Examples — missing, needs to be written -->
<!--       Guidance: min 3 examples: basic / variant / edge -->

<!-- TODO: Intuition — missing, needs to be written -->
<!--       Guidance: 3 paragraphs: brute force / observation / pattern fit -->

<!-- TODO: Applying the Diagnostic Questions — missing, needs to be written -->
<!--       Guidance: REQUIRED, never optional -->
<!--       Guidance: 4-row table. Columns: 'Check' | 'Answer for [Problem Name]' -->
<!--       Guidance: Rows: two positions simultaneously / one near start one near end / both move inward / simple O(1) work at each step -->

<!-- TODO: Approach — missing, needs to be written -->
<!--       Guidance: numbered steps, no code -->

<!-- TODO: Solution — missing, needs to be written -->
<!--       Guidance: Python block then Java block -->

<!-- TODO: Dry Run — missing, needs to be written -->
<!--       Guidance: walk through a small example step by step -->

<!-- TODO: Complexity Analysis — missing, needs to be written -->
<!--       Guidance: table: time / space / why -->

<!-- TODO: Edge Cases — missing, needs to be written -->
<!--       Guidance: table, min 5 rows -->

<!-- TODO: Key Takeaway — missing, needs to be written -->
<!--       Guidance: 1–2 sentences -->
