---
title: "Reverse Last K Nodes"
summary: "Given the head of a doubly linked list and a non-negative integer k, write a function to reverse the last K nodes of the list and return the head. Reverse in place."
prereqs:
  - 06-pattern-reversal/01-pattern
difficulty: medium
---

# Reverse last K nodes

## The Problem

> Given the **head** of a doubly linked list and a non-negative integer **k**, write a function to reverse the **last K** nodes of the list and return the head. Reverse in place.

```
Input:  head = [5, 7, 3, 10, 3], k = 2
Output: [5, 7, 3, 3, 10]
```

The last K nodes form a suffix segment. We don't know where the suffix starts without knowing the list length, so the algorithm has three pieces:

1. Compute the length `N` of the list.
2. If `K >= N`, the suffix is the entire list — just call the whole-list reversal.
3. Otherwise, walk to the `(N − K)`-th node, snip the suffix off, reverse it as a standalone list, and re-stitch.

The "snip and reverse" trick is the cleanest way to reuse the whole-list reversal we already have. We disconnect the suffix by setting the `(N − K)`-th node's `next` to `null` and the suffix's first `prev` to `null`, run `reverseAList` on the suffix in isolation, then reattach.

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
    def length_of_list(self, head: Optional[ListNode]) -> int:
        length: int = 0

        # Traverse the list and increment the length until the end
        while head:
            length += 1
            head = head.next

        # Return the length
        return length

    def reverse_a_list(
        self, head: Optional[ListNode]
    ) -> Optional[ListNode]:

        # If the head is null or if it's the only node in the list,
        # return the head as it is
        if head is None or (head.prev is None and head.next is None):
            return head

        # Pointer to track the current node
        current = head

        # Pointer to track the previous node
        previous = None

        while current is not None:

            # Save the address of next node
            next_node = current.next

            # Swap the previous and next nodes pointers of the current
            # node
            current.prev, current.next = current.next, current.prev

            # Store the previous node in the previous pointer
            previous = current

            # Move the current pointer to the next node
            current = next_node

        # Return the new head, which is stored in the previous pointer
        return previous

    def reverse_last_k_nodes(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:

        # if K is less than or equal to 0, return the original head
        if k <= 0:
            return head

        # Find the length of the list
        length = self.length_of_list(head)

        # If k is greater than or equal to length, reverse the entire
        # list
        if k >= length:
            return self.reverse_a_list(head)

        # Find the (length - k)th node after which the reversal should
        # occur
        current = head
        for _ in range(1, length - k):
            current = current.next

        # Disconnect the last k nodes from the main list
        if current.next is not None:
            current.next.prev = None

        # Reverse the last k nodes
        last_k_reverse_head = self.reverse_a_list(current.next)

        # Connect the (length - k)th node to the new head
        current.next = last_k_reverse_head

        # Connect the new head of the reversed list to the
        # (length - k)th node
        if last_k_reverse_head is not None:
            last_k_reverse_head.prev = current

        return head


# Examples from the problem statement
head = from_list([5, 7, 3, 10, 3])
print(to_list(Solution().reverse_last_k_nodes(head, 2)))  # [5, 7, 3, 3, 10]

# Edge cases
head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_last_k_nodes(head, 0)))  # [1, 2, 3, 4, 5]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_last_k_nodes(head, 1)))  # [1, 2, 3, 4, 5]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_last_k_nodes(head, 5)))  # [5, 4, 3, 2, 1]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_last_k_nodes(head, 10)))  # [5, 4, 3, 2, 1]

head = from_list([1, 2, 3, 4, 5])
print(to_list(Solution().reverse_last_k_nodes(head, 3)))  # [1, 2, 5, 4, 3]

head = from_list([7])
print(to_list(Solution().reverse_last_k_nodes(head, 1)))  # [7]

head = from_list([3, 9])
print(to_list(Solution().reverse_last_k_nodes(head, 1)))  # [3, 9]

head = from_list([3, 9])
print(to_list(Solution().reverse_last_k_nodes(head, 2)))  # [9, 3]
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
        private int lengthOfList(ListNode head) {
            int length = 0;

            // Traverse the list and increment the length until the end
            while (head != null) {
                length++;
                head = head.next;
            }

            // Return the length
            return length;
        }

        private ListNode reverseAList(ListNode head) {

            // If the head is null or if it's the only node in the list,
            // return the head as it is
            if (head == null || (head.prev == null && head.next == null)) {
                return head;
            }

            // Pointer to track the current node
            ListNode current = head;

            // Pointer to track the previous node
            ListNode previous = null;

            while (current != null) {

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

            // Return the new head, which is stored in the previous pointer
            return previous;
        }

        public ListNode reverseLastKNodes(ListNode head, int k) {

            // if K is less than or equal to 0, return the original head
            if (k <= 0) {
                return head;
            }

            // Find the length of the list
            int length = lengthOfList(head);

            // If k is greater than or equal to length, reverse the entire
            // list
            if (k >= length) {
                return reverseAList(head);
            }

            // Find the (length - k)th node after which the reversal should
            // occur
            ListNode current = head;
            for (int i = 1; i < length - k; i++) {
                current = current.next;
            }

            // Disconnect the last k nodes from the main list
            if (current.next != null) {
                current.next.prev = null;
            }

            // Reverse the last k nodes
            ListNode lastKReverseHead = reverseAList(current.next);

            // Connect the (length - k)th node to the new head
            current.next = lastKReverseHead;

            // Connect the new head of the reversed list to the
            // (length - k)th node
            if (lastKReverseHead != null) {
                lastKReverseHead.prev = current;
            }

            return head;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(5, 7, 3, 10, 3), 2)));  // [5, 7, 3, 3, 10]

        // Edge cases
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(1, 2, 3, 4, 5), 0)));   // [1, 2, 3, 4, 5]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(1, 2, 3, 4, 5), 1)));   // [1, 2, 3, 4, 5]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(1, 2, 3, 4, 5), 5)));   // [5, 4, 3, 2, 1]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(1, 2, 3, 4, 5), 10)));  // [5, 4, 3, 2, 1]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(1, 2, 3, 4, 5), 3)));   // [1, 2, 5, 4, 3]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(7), 1)));               // [7]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(3, 9), 1)));            // [3, 9]
        System.out.println(toList(new Solution().reverseLastKNodes(fromList(3, 9), 2)));            // [9, 3]
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10, 3], k = 2</strong></summary>

```
length = 5,  k = 2,  k < length  → suffix is positions 4..5 = [10, 3]

Walk: current = head, loop range(1, length - k) = range(1, 3) → 2 steps
  Start  current = 5
  Step 1 current = 5.next = 7
  Step 2 current = 7.next = 3   (this is the (length - k)-th = 3rd node)

Disconnect the suffix:
  current.next is node(10) ≠ null → current.next.prev = null
  (node 10 drops its back-link, so the suffix [10, 3] is a standalone list)

Reverse the suffix:
  reverse_a_list(current.next) = reverse_a_list(10 ⇄ 3) → 3 ⇄ 10
  last_k_reverse_head = 3

Re-stitch:
  current.next = last_k_reverse_head = node(3)         (prefix tail → new suffix head)
  last_k_reverse_head is non-null → last_k_reverse_head.prev = current = node(3)   (mirror)

Result: 5 ⇄ 7 ⇄ 3 ⇄ 3 ⇄ 10  ✓
```

The find-reverse-stitch pattern reuses the whole-list `reverse_a_list` helper verbatim — no edge-case branching for "where exactly does the segment start". Because the suffix is detached as its own list, the helper does the prev/next swaps internally; the re-stitch then restores both directions across the cut (`current.next` forward and `last_k_reverse_head.prev` backward). The price is two extra walks (one to count length, one to find the cut).

</details>

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
