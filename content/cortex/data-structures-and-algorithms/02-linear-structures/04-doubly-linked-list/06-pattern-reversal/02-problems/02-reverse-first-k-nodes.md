---
title: "Reverse First K Nodes"
summary: "Given the head of a doubly linked list and a non-negative integer k, write a function to reverse the first K nodes of the list and return the head of the resulting list. Reverse in place."
prereqs:
  - 06-pattern-reversal/01-pattern
difficulty: easy
---

# Reverse first K nodes

## The Problem

> Given the **head** of a doubly linked list and a non-negative integer **k**, write a function to reverse the **first K** nodes of the list and return the head of the resulting list. Reverse in place.

```
Input:  head = [5, 7, 3, 10, 3], k = 2
Output: [7, 5, 3, 10, 3]
```

The first K nodes form a prefix segment. After reversing it, we have a new list of length K plus an unchanged suffix of length `N − K`. Two stitches matter: the **new head** of the reversed prefix becomes the new head of the whole list, and the **new tail** of the reversed prefix (= the original `head`) must be re-linked to the suffix.

> *Quick prediction — what should the new tail's `prev` look like, and what should the suffix's first node's `prev` look like, after the reversal? Try to draw it before reading on.*

<details>
<summary><h2>The Solution</h2></summary>


Walk K steps, swapping each node's `prev` and `next` pointers, and stop. After the loop:

- `previous` holds the new head of the reversed prefix (originally the K-th node).
- The original `head` is now the *last* node of the reversed prefix (its `next` currently points at `null` after its own swap).
- `current` is sitting on the (K+1)-th node — the start of the untouched suffix, or `null` if `k >= N`.

We then re-stitch in three writes: the original head's `next` becomes `current` (the suffix start), the suffix's first node's `prev` points back at the original head, and the new head's `prev` is cleared to `null` — joining the reversed prefix to the remaining list in both directions.


```python run viz=linked-list viz-root=head
from typing import Optional, List, Any


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
    def reverse_first_k_nodes(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:

        # if K is less than or equal to 0, return the original head
        if k <= 0:
            return head

        # Initialize pointers current and previous
        current: Optional[ListNode] = head
        previous: Optional[ListNode] = None
        count = 0

        while current is not None and count < k:

            # Save the address of next node
            next_node = current.next

            # Swap the previous and next nodes pointers of the current
            # node
            current.prev, current.next = current.next, current.prev

            # Move previous to hold current node
            previous = current

            # Move current ahead
            current = next_node

            # Increment count
            count += 1

        # Connect the reversed sublist with the remaining part
        if head is not None:
            head.next = current

        # Update prev of the next node to point back to new tail
        if current is not None:
            current.prev = head

        # Mark the previous pointer of the new head to None
        if previous is not None:
            previous.prev = None

        return previous


# Examples from the problem statement
print(to_list(Solution().reverse_first_k_nodes(from_list([5, 7, 3, 10, 3]), 2)))   # [7, 5, 3, 10, 3]

# Edge cases
print(to_list(Solution().reverse_first_k_nodes(None, 3)))                            # []
print(to_list(Solution().reverse_first_k_nodes(from_list([5, 7, 3, 10, 3]), 0)))    # [5, 7, 3, 10, 3]
print(to_list(Solution().reverse_first_k_nodes(from_list([5, 7, 3, 10, 3]), 1)))    # [5, 7, 3, 10, 3]
print(to_list(Solution().reverse_first_k_nodes(from_list([5, 7, 3, 10, 3]), 5)))    # [3, 10, 3, 7, 5]
print(to_list(Solution().reverse_first_k_nodes(from_list([1, 2]), 2)))               # [2, 1]
print(to_list(Solution().reverse_first_k_nodes(from_list([42]), 1)))                 # [42]
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
        public ListNode reverseFirstKNodes(ListNode head, int k) {

            // if K is less than or equal to 0, return the original head
            if (k <= 0) {
                return head;
            }

            // Initialize pointers current and previous
            ListNode current = head;
            ListNode previous = null;
            int count = 0;

            while (current != null && count < k) {

                // Save the address of next node
                ListNode next = current.next;

                // Swap the previous and next nodes pointers of the current
                // node
                ListNode temp = current.prev;
                current.prev = current.next;
                current.next = temp;

                // Move previous to hold current node
                previous = current;

                // Move current ahead
                current = next;

                // Increment count
                count++;
            }

            // Connect the reversed sublist with the remaining part
            if (head != null) {
                head.next = current;
            }

            // Update prev of the next node to point back to new tail
            if (current != null) {
                current.prev = head;
            }

            // Mark the previous pointer of the new head to nullptr
            if (previous != null) {
                previous.prev = null;
            }

            return previous;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(5, 7, 3, 10, 3), 2)));  // [7, 5, 3, 10, 3]

        // Edge cases
        System.out.println(toList(new Solution().reverseFirstKNodes(null, 3)));                       // []
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(5, 7, 3, 10, 3), 0)));  // [5, 7, 3, 10, 3]
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(5, 7, 3, 10, 3), 1)));  // [5, 7, 3, 10, 3]
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(5, 7, 3, 10, 3), 5)));  // [3, 10, 3, 7, 5]
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(1, 2), 2)));             // [2, 1]
        System.out.println(toList(new Solution().reverseFirstKNodes(fromList(42), 1)));               // [42]
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10, 3], k = 2</strong></summary>

```
Initial: 5 ⇄ 7 ⇄ 3 ⇄ 10 ⇄ 3,   current = 5,  previous = null,  count = 0

Step 1 │ count=0 < k=2 │ current=5 │ next_node=7 │ swap 5: prev 7, next null │ previous=5, current=7, count=1
Step 2 │ count=1 < k=2 │ current=7 │ next_node=3 │ swap 7: prev 3, next 5    │ previous=7, current=3, count=2
Loop exits (count == k).

Stitch:
  head=5 (now the last node of the prefix) → head.next = current = node(3)
  current=3 is non-null → current.prev = head = node(5)   (mirror the boundary)
  previous=7 is the new head → previous.prev = null
  return previous=7 (new head)

Result: 7 ⇄ 5 ⇄ 3 ⇄ 10 ⇄ 3  ✓
```

The prefix `[5, 7]` flips to `[7, 5]` while the suffix `[3, 10, 3]` is left completely untouched. Three boundary writes reconnect the two parts in both directions: `head.next` forward into the suffix, the suffix's `prev` back at the old head, and the new head's `prev` cleared to `null`.

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
