---
title: "Succeeding Superior Nodes"
summary: "Given the head of a singly-linked list, return an array where result[i] is the value of the next node strictly greater than node i (1-indexed). Use 0 if no such node exists."
prereqs:
  - 10-pattern-next-closest-occurrence/01-pattern
difficulty: medium
---

# Succeeding superior nodes

## Problem Statement

Given the head of a singly-linked list, return an array where `result[i]` is the value of the next node strictly greater than node `i` (1-indexed). Use `0` if no such node exists.

### Example 1
> -   **Input:** `head = [2, 1, 5]` → **Output:** `[5, 5, 0]`

### Example 2
> -   **Input:** `head = [2, 7, 4, 3, 5]` → **Output:** `[7, 0, 5, 5, 0]`

<details>
<summary><h2>Approach</h2></summary>


Same algorithm — but the data source is a linked list, so we walk it once with a pointer, tracking each node's index. Stack stores `(index, value)` pairs; on each new value, pop and resolve as before.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import Optional, List


class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None


def from_list(values):
    if not values:
        return None
    head = ListNode(values[0])
    cur = head
    for v in values[1:]:
        cur.next = ListNode(v)
        cur = cur.next
    return head


# Struct to store index and value of each node
class NodeInfo:
    def __init__(self, index: int, value: int):
        self.index = index
        self.value = value

class Solution:
    def succeeding_superior_nodes(
        self, head: Optional[ListNode]
    ) -> List[int]:

        # Stores the next larger elements
        result: List[int] = []

        # Stores the elements in a stack along with their indices
        stack: List[NodeInfo] = []

        # Keeps track of the current index
        index = 0

        while head is not None:

            # Initialize the result for the current node as 0
            result.append(0)

            # While the stack is not empty and the value of the current
            # node is greater than the value of the element at the top
            # of the stack
            while stack and head.val > stack[-1].value:

                # Get the element at the top of the stack
                top = stack.pop()

                # Set the result at the index of the top element to the
                # value of the current node
                result[top.index] = head.val

            # Push the current node's index and value to the stack
            stack.append(NodeInfo(index, head.val))
            index += 1

            # Move to the next node
            head = head.next

        # Return the list containing the next larger elements
        return result


# Examples from the problem statement
print(Solution().succeeding_superior_nodes(from_list([2, 1, 5])))       # [5, 5, 0]
print(Solution().succeeding_superior_nodes(from_list([2, 7, 4, 3, 5]))) # [7, 0, 5, 5, 0]

# Edge cases
print(Solution().succeeding_superior_nodes(None))                       # []
print(Solution().succeeding_superior_nodes(from_list([1])))             # [0]
print(Solution().succeeding_superior_nodes(from_list([1, 2])))          # [2, 0]
print(Solution().succeeding_superior_nodes(from_list([2, 1])))          # [0, 0]
print(Solution().succeeding_superior_nodes(from_list([1, 2, 3, 4])))    # [2, 3, 4, 0]
print(Solution().succeeding_superior_nodes(from_list([4, 3, 2, 1])))    # [0, 0, 0, 0]
```

```java run
import java.util.*;

public class Main {
    static class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
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

    // Class to store index and value of each node
    static class NodeInfo {
        int index;
        int value;
        NodeInfo(int index, int value) {
            this.index = index;
            this.value = value;
        }
    }

    static class Solution {
        public List<Integer> succeedingSuperiorNodes(ListNode head) {

            // Stores the next larger elements
            List<Integer> result = new ArrayList<>();

            // Stores the elements in a stack along with their indices
            Stack<NodeInfo> stack = new Stack<>();

            // Keeps track of the current index
            int index = 0;

            while (head != null) {

                // Initialize the result for the current node as 0
                result.add(0);

                // While the stack is not empty and the value of the current
                // node is greater than the value of the element at the top
                // of the stack
                while (!stack.isEmpty() && head.val > stack.peek().value) {

                    // Get the element at the top of the stack
                    NodeInfo top = stack.pop();

                    // Set the result at the index of the top element to the
                    // value of the current node
                    result.set(top.index, head.val);
                }

                // Push the current node's index and value to the stack
                stack.push(new NodeInfo(index++, head.val));

                // Move to the next node
                head = head.next;
            }

            // Return the list containing the next larger elements
            return result;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(2, 1, 5)));       // [5, 5, 0]
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(2, 7, 4, 3, 5))); // [7, 0, 5, 5, 0]

        // Edge cases
        System.out.println(new Solution().succeedingSuperiorNodes(null));                    // []
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(1)));             // [0]
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(1, 2)));          // [2, 0]
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(2, 1)));          // [0, 0]
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(1, 2, 3, 4)));    // [2, 3, 4, 0]
        System.out.println(new Solution().succeedingSuperiorNodes(fromList(4, 3, 2, 1)));    // [0, 0, 0, 0]
    }
}
```

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
