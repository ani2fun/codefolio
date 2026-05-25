---
title: "Reverse a List"
summary: "Given the head of a doubly linked list, write a function to reverse the list in place and return the head of the reversed list."
prereqs:
  - 06-pattern-reversal/01-pattern
difficulty: easy
---

# Reverse a list

## The Problem

> Given the **head** of a doubly linked list, write a function to reverse the list in place and return the head of the reversed list.

```
Input:  head = [5, 7, 3, 10, 3]
Output: [3, 10, 3, 7, 5]
```

<details>
<summary><h2>The Solution</h2></summary>


This is the whole-list special case. Walk every node with a `current`/`previous` pair; at each node **swap its `prev` and `next` pointers** so both chains flip at once, and return `previous` — once the walk ends, it holds the original tail, which is the new head.


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


# Examples from the problem statement
print(to_list(Solution().reverse_a_list(from_list([5, 7, 3, 10, 3]))))   # [3, 10, 3, 7, 5]

# Edge cases
print(to_list(Solution().reverse_a_list(None)))                            # []
print(to_list(Solution().reverse_a_list(from_list([42]))))                 # [42]
print(to_list(Solution().reverse_a_list(from_list([1, 2]))))               # [2, 1]
print(to_list(Solution().reverse_a_list(from_list([1, 2, 3, 4]))))        # [4, 3, 2, 1]
print(to_list(Solution().reverse_a_list(from_list([5, 5, 5]))))           # [5, 5, 5]
print(to_list(Solution().reverse_a_list(from_list([1, 2, 3, 2, 1]))))    # [1, 2, 3, 2, 1]
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
        public ListNode reverseAList(ListNode head) {

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
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toList(new Solution().reverseAList(fromList(5, 7, 3, 10, 3))));   // [3, 10, 3, 7, 5]

        // Edge cases
        System.out.println(toList(new Solution().reverseAList(null)));                         // []
        System.out.println(toList(new Solution().reverseAList(fromList(42))));                 // [42]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 2))));               // [2, 1]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 2, 3, 4))));        // [4, 3, 2, 1]
        System.out.println(toList(new Solution().reverseAList(fromList(5, 5, 5))));           // [5, 5, 5]
        System.out.println(toList(new Solution().reverseAList(fromList(1, 2, 3, 2, 1))));    // [1, 2, 3, 2, 1]
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10, 3]</strong></summary>

```
Initial: head → 5 ⇄ 7 ⇄ 3 ⇄ 10 ⇄ 3,   current = 5,   previous = null

Step 1 │ current = 5      │ next_node = 7   │ swap 5:  prev 7, next null  │ previous = 5,  current = 7
Step 2 │ current = 7      │ next_node = 3   │ swap 7:  prev 3, next 5     │ previous = 7,  current = 3
Step 3 │ current = 3 (m)  │ next_node = 10  │ swap 3:  prev 10, next 7    │ previous = 3,  current = 10
Step 4 │ current = 10     │ next_node = 3   │ swap 10: prev 3, next 3     │ previous = 10, current = 3
Step 5 │ current = 3 (t)  │ next_node = null│ swap 3:  prev null, next 10 │ previous = 3,  current = null
Done   │ current == null — return previous (= 3, original tail)

Result: head → 3 ⇄ 10 ⇄ 3 ⇄ 7 ⇄ 5  ✓
```

Each step swaps the node's `prev` and `next` in one stroke — `next_node` is saved *before* the swap because the swap overwrites `current.next`. Both chains flip together: the original tail ends with `prev = null` (it is the new head), and `previous` walks one step behind `current`, so it lands on that tail the moment `current` falls off the end.

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
