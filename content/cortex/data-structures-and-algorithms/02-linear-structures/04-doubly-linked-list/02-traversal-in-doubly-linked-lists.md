# 2. Traversal in Doubly Linked Lists

## The Hook

Picture a one-way street. Cars enter at one end, exit at the other, and any driver who needs the *previous* exit has to loop all the way around the block. That's a singly linked list — every traversal flows in exactly one direction. Now imagine a regular two-way street: same lane, same nodes, same destinations — but you can drive in either direction, and your start point dictates which direction makes sense.

This single change unlocks something subtle. With a doubly linked list, **the entry point you're given silently chooses your direction.** Hand you a `head`? You walk forward. Hand you a `tail`? You walk backward — and you don't lose any information; in fact, you gain a free reversal of the output for nothing more than starting from the other end. By the end of this lesson, you'll write a list-printer that *prints in reverse* without ever calling a reversal function — just by changing one pointer name.

---

## Table of contents

1. [Understanding traversal](#understanding-traversal)
2. [Node expedition](#node-expedition)
3. [Node expedition II](#node-expedition-ii)
4. [Node search](#node-search)

***

# Understanding traversal

Traversal is the most fundamental operation on a doubly linked list, and the basic mechanic is the same as in a singly linked list: hold a "cursor" variable, follow a pointer, repeat until the cursor lands on `null`. The extra information about the previous node that every node in a doubly linked list stores gives us a brand-new ability — the same loop, run on `prev` instead of `next`, walks the list in **reverse**. Two traversals for the price of one extra pointer per node.

## Forward Traversal

Forward traversal is moving from the **head** to the **tail** node in the doubly linked list. It is implemented exactly the way you remember from singly linked lists. We use a variable that holds a reference to a node in the linked list as the loop control variable, and every iteration we *advance* it by reading the `next` pointer of the current node — `current = current.next` — until it falls off the end and lands on `null`.

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
flowchart LR
    HEAD(["head"]) --> N1["5"]
    N1 <--> N2["7"] <--> N3["3"] <--> N4["10"] --> NULL_R(["null"])
    CUR1["current<br/>(step 1)"] -.-> N1
    CUR2["→ step 2"] -.-> N2
    CUR3["→ step 3"] -.-> N3
    CUR4["→ step 4"] -.-> N4
    STOP["→ step 5: null<br/>loop ends"] -.-> NULL_R
```

<p align="center"><strong>Forward traversal using the <code>next</code> pointer — start at <code>head</code>, follow <code>current.next</code> on each iteration, stop when <code>current</code> becomes <code>null</code>.</strong></p>

Given below is the code implementation of forward traversal in a doubly linked list, shown in two equivalent forms — a `for` loop that puts initialisation, condition, and advance on a single line, and a `while` loop that spreads them out for clarity.


```python run
# Python idiom — `while` is the canonical form for pointer-walking
current = head            # Start at the head — the only entry point given
while current is not None:
    # ... do something with current.val ...
    current = current.next  # Advance to the successor; falls off to None at the tail
```

```java run
// for loop — initialiser + condition + step on one line
for (ListNode current = head; current != null; current = current.next) {
    // ... do something with current.val ...
}

// while loop — same logic, three statements
ListNode current = head;
while (current != null) {
    // ... do something with current.val ...
    current = current.next;
}
```


## Reverse Traversal

Unlike a singly linked list, we can also traverse a doubly linked list in the reverse direction — from the **tail** node back to the **head** — thanks to the `prev` pointer in every node that stores the reference to the previous node. The shape of the loop is *identical* to forward traversal; only two things change: we start at `tail` instead of `head`, and we advance via `current.prev` instead of `current.next`. The terminator is still `null` — but now it's the head's `prev`, not the tail's `next`, that ends the walk.

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
flowchart LR
    NULL_L(["null"]) --- N1["5"]
    N1 <--> N2["7"] <--> N3["3"] <--> N4["10"]
    TAIL(["tail"]) --> N4
    CUR1["current<br/>(step 1)"] -.-> N4
    CUR2["→ step 2"] -.-> N3
    CUR3["→ step 3"] -.-> N2
    CUR4["→ step 4"] -.-> N1
    STOP["→ step 5: null<br/>loop ends"] -.-> NULL_L
```

<p align="center"><strong>Reverse traversal using the <code>prev</code> pointer — start at <code>tail</code>, follow <code>current.prev</code> on each iteration, stop when <code>current</code> becomes <code>null</code> (i.e. when we walk past the head).</strong></p>

Given below is the code implementation of reverse traversal — note how it is the *mirror image* of the forward version, swapping `head ↔ tail` and `next ↔ prev`.


```python run
# Reverse walk — start at tail, follow prev
current = tail
while current is not None:
    # ... do something with current.val ...
    current = current.prev   # Advance to the predecessor; falls off to None at the head
```

```java run
// for loop — note tail/prev instead of head/next
for (ListNode current = tail; current != null; current = current.prev) {
    // ... do something with current.val ...
}

// while loop
ListNode current = tail;
while (current != null) {
    // ... do something with current.val ...
    current = current.prev;
}
```


> *Pause and predict — if you wrote the forward traversal once and the reverse traversal once, the only differences would be: ___ and ___.*
>
> The starting reference (`head` ↔ `tail`) and the advance pointer (`next` ↔ `prev`). Everything else — the loop shape, the null check, the body — is byte-for-byte identical. **This symmetry is the whole point of a doubly linked list.** Write your traversal once, parameterise the direction, and you've doubled your toolkit.

Later in this course, we will learn more about how to piggyback on this generic forward and reverse traversal logic to do various things as we traverse a doubly linked list — printing, searching, summing, splitting, merging, and (much later) implementing operations like LRU eviction that need *both* directions running simultaneously.

***

# Node expedition

## The Problem

> Given the **head** of a doubly linked list, write a function to print a comma-separated list of all the values from the start to the end.
>
> *In TypeScript and JavaScript, use `process.stdout.write` instead of `console.log`, since `console.log` automatically appends a newline character to the output, which may cause the judge to fail the submission.*

```
Input:  head = [5, 7, 3, 10]
Output: 5, 7, 3, 10
```

### What we are practising

Forward traversal with a small twist: a **separator** between values, and *no trailing separator* after the last value. The classic "print joined" pattern — every loop body needs a way to know whether it's looking at the last element so it can suppress the comma.

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
flowchart LR
    HEAD(["head"]) --> N1["5"]
    N1 <--> N2["7"] <--> N3["3"] <--> N4["10"]
    OUT["Print: 5, 7, 3, 10"] -.-> N4
    NOTE["Comma after every node<br/>EXCEPT the one whose<br/>next is null"] -.-> N4
    style N4 fill:#fef9c3,stroke:#3b82f6
```

<p align="center"><strong>The "lookahead" trick — print the value first, then look at <code>current.next</code> to decide whether a comma follows. The tail node is the only one whose <code>next</code> is <code>null</code>, and it is the only one that does not get a trailing comma.</strong></p>

<details>
<summary><h2>The Solution</h2></summary>



```python run
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
Solution().node_expedition(from_list([5, 7, 3, 10])); print()    # 5, 7, 3, 10

# Edge cases
Solution().node_expedition(from_list([])); print()               # (empty)
Solution().node_expedition(from_list([42])); print()             # 42
Solution().node_expedition(from_list([1, 2])); print()           # 1, 2
Solution().node_expedition(from_list([1, 2, 3, 4, 5])); print()  # 1, 2, 3, 4, 5
Solution().node_expedition(from_list([7, 7, 7])); print()        # 7, 7, 7
```

```java run
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
        new Solution().nodeExpedition(fromList(5, 7, 3, 10)); System.out.println();    // 5, 7, 3, 10

        // Edge cases
        new Solution().nodeExpedition(fromList()); System.out.println();               // (empty)
        new Solution().nodeExpedition(fromList(42)); System.out.println();             // 42
        new Solution().nodeExpedition(fromList(1, 2)); System.out.println();           // 1, 2
        new Solution().nodeExpedition(fromList(1, 2, 3, 4, 5)); System.out.println(); // 1, 2, 3, 4, 5
        new Solution().nodeExpedition(fromList(7, 7, 7)); System.out.println();        // 7, 7, 7
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10]</strong></summary>

```
Step 1 │ current = node(5)  │ print "5",  next != null → print ", "   │ output: "5, "
Step 2 │ current = node(7)  │ print "7",  next != null → print ", "   │ output: "5, 7, "
Step 3 │ current = node(3)  │ print "3",  next != null → print ", "   │ output: "5, 7, 3, "
Step 4 │ current = node(10) │ print "10", next == null → no separator │ output: "5, 7, 3, 10"
Step 5 │ current = null     │ loop terminates
Result: "5, 7, 3, 10" ✓
```

The lookahead at `current.next` is what suppresses the trailing comma — the tail is the only node whose successor is `null`, so it's the only one that prints its value alone.

</details>

</details>

***

# Node expedition II

## The Problem

> Given the **tail** of a doubly linked list, write a function to print a comma-separated list of all the values from the tail to the head.
>
> *In TypeScript and JavaScript, use `process.stdout.write` instead of `console.log`, since `console.log` automatically appends a newline character to the output, which may cause the judge to fail the submission.*

```
Input:  tail = [5, 7, 3, 10]      # logical list, tail is node(10)
Output: 10, 3, 7, 5
```

### What we are practising

This is the *mirror image* of the previous problem. Same algorithm, same separator trick, same null-terminator — only the entry point and the advance direction flip. The lookahead this time is at `current.prev`, because the **head** is now the node we want to print without a trailing comma.

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
flowchart LR
    NULL_L(["null"]) --- N1["5"]
    N1 <--> N2["7"] <--> N3["3"] <--> N4["10"]
    TAIL(["tail"]) --> N4
    OUT["Print: 10, 3, 7, 5"] -.-> N1
    NOTE["Comma after every node<br/>EXCEPT the one whose<br/>prev is null (the head)"] -.-> N1
    style N1 fill:#fef9c3,stroke:#3b82f6
```

<p align="center"><strong>Reverse expedition — start at <code>tail</code>, advance via <code>prev</code>, suppress the trailing separator on the node whose <code>prev</code> is <code>null</code> (the head).</strong></p>

<details>
<summary><h2>The Solution</h2></summary>



```python run
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


def to_tail(head):
    """Return the tail node of a list built with from_list."""
    if head is None:
        return None
    cur = head
    while cur.next is not None:
        cur = cur.next
    return cur


class Solution:
    def node_expedition_ii(self, tail: Optional[ListNode]) -> None:

        # Start from the tail of the linked list
        current: Optional[ListNode] = tail

        # Traverse the linked list backwards starting from the current
        # node
        while current is not None:

            # Print the value of the current node
            print(current.val, end="")

            # Check if the current node has a previous node
            if current.prev is not None:

                # If a previous node exists, print a comma and space
                print(", ", end="")

            # Move to the previous node
            current = current.prev


# Examples from the problem statement
Solution().node_expedition_ii(to_tail(from_list([5, 7, 3, 10]))); print()    # 10, 3, 7, 5

# Edge cases
Solution().node_expedition_ii(None); print()                                  # (empty)
Solution().node_expedition_ii(to_tail(from_list([42]))); print()              # 42
Solution().node_expedition_ii(to_tail(from_list([1, 2]))); print()            # 2, 1
Solution().node_expedition_ii(to_tail(from_list([1, 2, 3, 4, 5]))); print()  # 5, 4, 3, 2, 1
Solution().node_expedition_ii(to_tail(from_list([7, 7, 7]))); print()         # 7, 7, 7
```

```java run
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

    static ListNode toTail(ListNode head) {
        if (head == null) return null;
        ListNode cur = head;
        while (cur.next != null) cur = cur.next;
        return cur;
    }

    static class Solution {
        public void nodeExpeditionII(ListNode tail) {

            // Start from the tail of the linked list
            ListNode current = tail;

            // Traverse the linked list backwards starting from the current
            // node
            while (current != null) {

                // Print the value of the current node
                System.out.print(current.val);

                // Check if the current node has a previous node
                if (current.prev != null) {

                    // If a previous node exists, print a comma and space
                    System.out.print(", ");
                }

                // Move to the previous node
                current = current.prev;
            }
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        new Solution().nodeExpeditionII(toTail(fromList(5, 7, 3, 10))); System.out.println();    // 10, 3, 7, 5

        // Edge cases
        new Solution().nodeExpeditionII(null); System.out.println();                             // (empty)
        new Solution().nodeExpeditionII(toTail(fromList(42))); System.out.println();             // 42
        new Solution().nodeExpeditionII(toTail(fromList(1, 2))); System.out.println();           // 2, 1
        new Solution().nodeExpeditionII(toTail(fromList(1, 2, 3, 4, 5))); System.out.println(); // 5, 4, 3, 2, 1
        new Solution().nodeExpeditionII(toTail(fromList(7, 7, 7))); System.out.println();        // 7, 7, 7
    }
}
```


<details>
<summary><strong>Trace — tail = node(10) of [5, 7, 3, 10]</strong></summary>

```
Step 1 │ current = node(10) │ print "10", prev != null → print ", "  │ output: "10, "
Step 2 │ current = node(3)  │ print "3",  prev != null → print ", "  │ output: "10, 3, "
Step 3 │ current = node(7)  │ print "7",  prev != null → print ", "  │ output: "10, 3, 7, "
Step 4 │ current = node(5)  │ print "5",  prev == null → no separator │ output: "10, 3, 7, 5"
Step 5 │ current = null     │ loop terminates
Result: "10, 3, 7, 5" ✓
```

We never called a reversal routine — just walked the chain from the *other* end. **Reverse output for free** is the doubly linked list's signature trick.

</details>

</details>

***

# Node search

## The Problem

> Given the **tail** of a doubly linked list and a **data** value, write a function to return the first node containing the given data, searching from the tail toward the head. If no such node is found, return `null`.

```
Input:  head = [5, 7, 3, 10], data = 3
Output: node containing 3

Input:  head = [5, 7, 6, 10], data = 3
Output: null
```

### What we are practising

A **linear search** that exploits the doubly linked list's reverse traversability. The mechanic is identical to "find first match" in any other linear container — walk, compare, return on first hit, return `null` if the walk finishes empty-handed.

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
flowchart LR
    N1["5"] <--> N2["7"] <--> N3["3<br/>(match!)"] <--> N4["10"]
    TAIL(["tail"]) --> N4
    S1["check 10 ≠ 3"] -.-> N4
    S2["check 3 == 3 ✓<br/>return this node"] -.-> N3
    style N3 fill:#dcfce7,stroke:#16a34a
```

<p align="center"><strong>Linear search from tail to head — return the first node whose value matches; if the loop falls off the head with no match, return <code>null</code>.</strong></p>

> *Why search from the tail when we could've gone from the head? In *this* problem the input is `tail`, so we have no choice — but the takeaway is more general. **Search direction is now a free parameter.** When you have a hint about *where* the target probably lives (recently added items live near the tail in an LRU; older items live near the head), you can pick the direction that statistically wins.*

<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

```python run
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


def to_tail(head):
    if head is None:
        return None
    cur = head
    while cur.next is not None:
        cur = cur.next
    return cur


class Solution:
    def node_search(
        self, tail: Optional[ListNode], data: int
    ) -> Optional[ListNode]:

        # Start from the tail of the linked list
        current: Optional[ListNode] = tail

        # Traverse the linked list backwards starting from the current
        # node
        while current is not None:

            # If a matching node is found, return the pointer to that
            # node
            if current.val == data:
                return current

            # Move to the previous node in the linked list
            current = current.prev

        # If the loop finishes without finding a matching node, return
        # None
        return None


# Examples from the problem statement
r1 = Solution().node_search(to_tail(from_list([5, 7, 3, 10])), 3)
print(r1.val if r1 else None)   # 3

r2 = Solution().node_search(to_tail(from_list([5, 7, 6, 10])), 3)
print(r2.val if r2 else None)   # None

# Edge cases
r3 = Solution().node_search(None, 5)
print(r3.val if r3 else None)   # None

r4 = Solution().node_search(to_tail(from_list([42])), 42)
print(r4.val if r4 else None)   # 42

r5 = Solution().node_search(to_tail(from_list([42])), 1)
print(r5.val if r5 else None)   # None

r6 = Solution().node_search(to_tail(from_list([1, 2, 3, 4, 5])), 1)
print(r6.val if r6 else None)   # 1 (head node found via backward traversal)

r7 = Solution().node_search(to_tail(from_list([1, 2, 3, 4, 5])), 5)
print(r7.val if r7 else None)   # 5
```

```java run
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

    static ListNode toTail(ListNode head) {
        if (head == null) return null;
        ListNode cur = head;
        while (cur.next != null) cur = cur.next;
        return cur;
    }

    static class Solution {
        public ListNode nodeSearch(ListNode tail, int data) {

            // Start from the tail of the linked list
            ListNode current = tail;

            // Traverse the linked list backwards starting from the current
            // node
            while (current != null) {

                // If a matching node is found, return the pointer to
                // that node
                if (current.val == data) {
                    return current;
                }

                // Move to the previous node in the linked list
                current = current.prev;
            }

            // If the loop finishes without finding a matching node, return
            // null
            return null;
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        ListNode r1 = new Solution().nodeSearch(toTail(fromList(5, 7, 3, 10)), 3);
        System.out.println(r1 != null ? r1.val : null);   // 3

        ListNode r2 = new Solution().nodeSearch(toTail(fromList(5, 7, 6, 10)), 3);
        System.out.println(r2 != null ? r2.val : null);   // null

        // Edge cases
        ListNode r3 = new Solution().nodeSearch(null, 5);
        System.out.println(r3 != null ? r3.val : null);   // null

        ListNode r4 = new Solution().nodeSearch(toTail(fromList(42)), 42);
        System.out.println(r4 != null ? r4.val : null);   // 42

        ListNode r5 = new Solution().nodeSearch(toTail(fromList(42)), 1);
        System.out.println(r5 != null ? r5.val : null);   // null

        ListNode r6 = new Solution().nodeSearch(toTail(fromList(1, 2, 3, 4, 5)), 1);
        System.out.println(r6 != null ? r6.val : null);   // 1

        ListNode r7 = new Solution().nodeSearch(toTail(fromList(1, 2, 3, 4, 5)), 5);
        System.out.println(r7 != null ? r7.val : null);   // 5
    }
}
```


<details>
<summary><strong>Trace 1 — tail = node(10) of [5, 7, 3, 10], data = 3</strong></summary>

```
Step 1 │ current = node(10) │ 10 ≠ 3 → continue │ current = current.prev = node(3)
Step 2 │ current = node(3)  │ 3 == 3 ✓          │ return node(3)
Result: node(3) ✓
```

The walk starts at `tail` and steps via `current.prev`, so it inspects nodes back-to-front — node(10) then node(3) — and stops the moment the value matches.

</details>
<details>
<summary><strong>Trace 2 — tail = node(10) of [5, 7, 6, 10], data = 3</strong></summary>

```
Step 1 │ current = node(10) │ 10 ≠ 3 → continue │ current = current.prev = node(6)
Step 2 │ current = node(6)  │ 6 ≠ 3 → continue  │ current = current.prev = node(7)
Step 3 │ current = node(7)  │ 7 ≠ 3 → continue  │ current = current.prev = node(5)
Step 4 │ current = node(5)  │ 5 ≠ 3 → continue  │ current = current.prev = null
Step 5 │ current = null     │ loop terminates   │ return null
Result: null ✓
```

The backward walk falls off the head — node(5)'s `prev` is `null` — and the loop ends. The "no-match" case is what makes the `null` return value non-negotiable: the function must distinguish *"I found nothing"* from *"I found the head"* (which is also a valid return). The head's `prev` terminator carries that information for free.

</details>

### Complexity Analysis

| Operation | Time | Space | Why |
|---|---|---|---|
| Forward / reverse traversal | **O(n)** | **O(1)** | Each node is visited at most once; only a single cursor variable is held. |
| Node expedition (print) | **O(n)** | **O(1)** | One print per node, no auxiliary data structure. |
| Node search (linear) | **O(n)** worst, O(1) best | **O(1)** | Best case the target is the entry node; worst case it's at the far end or absent. |

> *The space term is the headline — every doubly linked list traversal is **O(1) extra space**. We don't allocate a stack, a queue, or a copy. We mutate one pointer in place. This becomes load-bearing later when we attack problems with hard memory budgets — like reversing a list with a million nodes on a constrained system.*

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


A doubly linked list traversal is a *parameterised* singly linked list traversal — same loop, configurable direction. Pick `head + next` to walk forward, `tail + prev` to walk backward, and the same algorithm works for both. This symmetry is what makes the data structure punch above its weight: **every "do this thing forward" operation comes with a free "do it backward" twin** as long as you remember to pass the other endpoint.

> **Transfer challenge:** Given the head of a doubly linked list of integers, return the *sum of values whose index from the right is even* (i.e. the tail is index 0, the node before it is index 1, and so on) — without using extra arrays. Hint: you only need one pointer and one counter.
>
> <details>
> <summary>Solution sketch</summary>
>
> Find the tail in O(n) (walk forward once), then walk backward via `prev`, incrementing an index counter and adding `current.val` to the sum whenever the index is even. Two passes, O(1) space — no array, no stack. The reverse traversal is what makes "index from the right" cheap.
>
> </details>

In the next lesson we leave traversal behind and start *modifying* the list. Insertion is where the doubly linked list's second pointer earns its keep — we'll see operations the singly linked list literally cannot match in time complexity.

</details>