# 1. Introduction to Singly Linked Lists

## The Hook

Imagine you're keeping a guest list for a dinner party. You grab a notepad with exactly ten lines — the perfect size. Then a friend calls: *"Can I bring two more?"* Now you're ripping the page out, starting over, and copying every name by hand. Later someone cancels and you rewrite the whole list again. This is how **arrays** feel every time you insert or delete in the middle.

What if instead of rigid lines on a page, each guest held a slip of paper with a single instruction: *"the next person is Alice, who lives at 42 Park Street."* Add a guest? Hand them a slip, update one instruction. Remove a guest? Redirect the slip before them. No copying. No rewriting. That's a **linked list** — and once you see the difference, you'll understand why every stack, queue, graph, hash table, and tree you'll ever implement owes something to this idea.

The linked list is the most important data structure you'll ever learn. Everything else builds on it. Let's see why.

---

## Table of contents

1. [Understanding the problem](#understanding-the-problem)
2. [Exploring a possible solution](#exploring-a-possible-solution)
3. [Defining a node in singly linked list](#defining-a-node-in-singly-linked-list)
4. [Structure of a singly linked list](#structure-of-a-singly-linked-list)
5. [Overview of supported operations](#overview-of-supported-operations)
6. [Boundary node](#boundary-node)

***

# Understanding the problem

To better understand a linked list, let us first look at some common problems programmers face when designing software systems. When writing a program, we often need a collection of data items that can be accessed sequentially. E.g., a collection of names of all the students in a class. It is common for people to think this is not such a complex problem. What's so hard with it? We can use an **array** to store this data where the size of the array is equal to the number of students.

```d3 widget=array-traversal
{
  "title": "students[] — 4 names in one contiguous block, addressable by index",
  "items": ["Alice", "Bob", "Carol", "David"],
  "steps": [
    {
      "range": { "lo": 0, "hi": 3 },
      "msg": "Every cell sits adjacent to the next — the index doubles as the offset from the array's base, so arr[i] is O(1)."
    }
  ]
}
```

This is an easy way to store data, but what if a new student joins the class? In this case, we will have to increase the size of the array by one, which is **not** possible. Well, we can solve this problem by creating a new array of a larger size, copying all the data from the previous array, and then adding the new student to it. However, this will be quite inefficient in terms of space and time complexity.

```d3 widget=array-traversal
{
  "items": ["Alice", "Bob", "Carol", "David"],
  "title": "Insert by copying — allocate a bigger array, copy every element, then append the new item",
  "primaryLabel": "Source array (size 4)",
  "secondaryItems": ["·", "·", "·", "·", "·"],
  "secondaryLabel": "Destination array (size 5)",
  "steps": [
    {
      "markers": [{"name": "src", "index": 0}],
      "secondaryMarkers": [{"name": "dst", "index": 0}],
      "msg": "Allocate a new array of size 5. Both indices start at 0; the destination is empty."
    },
    {
      "secondaryItems": ["Alice", "·", "·", "·", "·"],
      "markers": [{"name": "src", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 1}],
      "msg": "Copy src[0]=Alice → dst[0]. Advance both indices."
    },
    {
      "secondaryItems": ["Alice", "Bob", "·", "·", "·"],
      "markers": [{"name": "src", "index": 2}],
      "secondaryMarkers": [{"name": "dst", "index": 2}],
      "msg": "Copy src[1]=Bob → dst[1]. Advance both indices."
    },
    {
      "secondaryItems": ["Alice", "Bob", "Carol", "·", "·"],
      "markers": [{"name": "src", "index": 3}],
      "secondaryMarkers": [{"name": "dst", "index": 3}],
      "msg": "Copy src[2]=Carol → dst[2]. Advance both indices."
    },
    {
      "secondaryItems": ["Alice", "Bob", "Carol", "David", "·"],
      "markers": [{"name": "src", "index": 4}],
      "secondaryMarkers": [{"name": "dst", "index": 4}],
      "msg": "Copy src[3]=David → dst[3]. Source is exhausted; one cell remains in the destination."
    },
    {
      "secondaryItems": ["Alice", "Bob", "Carol", "David", "Eve"],
      "markers": [{"name": "src", "index": 4}],
      "secondaryMarkers": [{"name": "dst", "index": 5}],
      "msg": "Append the new item Eve at dst[4]. Done — 4 copies + 1 write, O(n) work to insert a single element."
    }
  ]
}
```

<p align="center"><strong>Adding a new student requires allocating a brand-new array and copying every existing element — O(n) time and O(n) extra space.</strong></p>

Now, let's consider another scenario. What if a student leaves the class? We can use the same process again. This time, we create a new array of smaller size and copy all the data items except the one we want to delete.

```d3 widget=array-traversal
{
  "items": ["Alice", "Bob", "Carol", "David"],
  "title": "Delete by copying — allocate a smaller array and copy every element except the one being removed",
  "primaryLabel": "Source array (size 4)",
  "secondaryItems": ["·", "·", "·"],
  "secondaryLabel": "Destination array (size 3)",
  "steps": [
    {
      "markers": [{"name": "src", "index": 0}, {"name": "remove", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 0}],
      "msg": "Allocate a smaller array of size 3. Mark the index to remove (Bob at src[1]). Both indices start at 0."
    },
    {
      "secondaryItems": ["Alice", "·", "·"],
      "markers": [{"name": "src", "index": 1}, {"name": "remove", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 1}],
      "msg": "Copy src[0]=Alice → dst[0]. Advance both indices."
    },
    {
      "secondaryItems": ["Alice", "·", "·"],
      "markers": [{"name": "src", "index": 2}, {"name": "remove", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 1}],
      "msg": "src[1]=Bob is the deletion target — skip it. Advance src only; dst stays at 1."
    },
    {
      "secondaryItems": ["Alice", "Carol", "·"],
      "markers": [{"name": "src", "index": 3}, {"name": "remove", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 2}],
      "msg": "Copy src[2]=Carol → dst[1]. Advance both indices."
    },
    {
      "secondaryItems": ["Alice", "Carol", "David"],
      "markers": [{"name": "src", "index": 4}, {"name": "remove", "index": 1}],
      "secondaryMarkers": [{"name": "dst", "index": 3}],
      "msg": "Copy src[3]=David → dst[2]. Source exhausted. Done — 3 copies + 1 skip, still O(n) work to remove a single element."
    }
  ]
}
```

<p align="center"><strong>Deleting a student requires another full copy into a smaller array — the same O(n) cost applies for every insertion or deletion.</strong></p>

The examples we looked at above inserted or deleted data from the **ends** of a sequential collection. What if we had to insert or delete data items from somewhere in the **middle**?

## Limitations of arrays

Even though we can solve the problem using an array, it is inefficient if we have to insert and delete data items frequently. The solution above performs poorly.

> 1.  **Space complexity O(N):** Initializing a new array to add or remove a single data element would waste a lot of memory when we need just one extra block.
> 2.  **Time complexity O(N):** Our algorithm is relatively slow since we try to traverse the entire array and copy the elements to the new array whenever we want to add or remove an item.

An array has other fundamental problems that make it a bad choice for problems like these. For example, we cannot insert or delete data items **in place** in an array.

```d2
direction: down

mem: "students[]  —  contiguous memory, every cell fixed" {
  grid-columns: 4
  grid-gap: 0
  c0: |md
    **Alice**

    `addr 100`
  |
  c1: |md
    **Bob**

    `addr 104`
  |
  c2: |md
    **Carol**

    `addr 108`
  |
  c3: |md
    **David**

    `addr 112`
  |
}

ins: "Insert 'Tara'\nbetween Bob & Carol?" {
  shape: oval
  style.fill: "#fee2e2"
  style.stroke: "#dc2626"
  style.stroke-width: 2
  style.bold: true
}

ins -> mem.c2: "no free slot —\nshift Carol & David\nor reallocate" {
  style.stroke: "#dc2626"
  style.stroke-width: 2
  style.bold: true
}
```

<p align="center"><strong>Arrays occupy a contiguous block of memory — there is no physical gap between elements to insert into, so every in-place insertion forces a cascade of element shifts.</strong></p>

> The fundamental challenges of using an array are:
>
> -   **Fixed size:** Arrays have a fixed size defined at their creation and cannot be changed later.
> -   **Insertion and Deletion:** Data items in an array reside in contiguous memory blocks. Since an array has a fixed size, we cannot insert or delete data items; we can only overwrite them.

What if we had a magical data structure that could solve the above problem most efficiently?

***

# Exploring a possible solution

Now that we know arrays' limitations and the situations where those limitations lead to sub-optimal solutions, we can start to think about a data structure that can be used efficiently in such situations. A singly linked list is designed precisely for situations like this.

## Linked list

A linked list is a linear and dynamic data structure that stores data sequentially at random memory locations. Instead of storing all the data items in a contiguous block of memory like arrays, a linked list stores them at random locations in memory. Whenever a new item is to be added, a new memory block is dynamically created to store this new value, which is then added to the chain of already existing items, effectively extending the **linked list**.

```d3 widget=linked-list
{
  "title": "Singly linked list — each node points to the next; the tail points to null",
  "direction": "single",
  "nodes": [
    {"id": "a", "value": "Alice"},
    {"id": "b", "value": "Bob"},
    {"id": "c", "value": "Carol"},
    {"id": "d", "value": "David"}
  ],
  "head": "a",
  "steps": [
    {
      "links": [["a","b"],["b","c"],["c","d"]],
      "markers": [{"name": "head", "nodeId": "a"}],
      "msg": "head → Alice → Bob → Carol → David → null"
    }
  ]
}
```

<p align="center"><strong>Abstract representation of a singly linked list — each node holds a value and a pointer to the next node; the last node points to null.</strong></p>

## Linked lists vs arrays

A linked list guarantees the insertion and deletion of items from the **start** and **end** of the list in **O(1)** space and **O(1)** time. It also guarantees the insertion and deletion of any data item **without** using any extra space. You can imagine it as a dynamic sequential container whose size can be increased or decreased at will.

```d3 widget=linked-list
{
  "title": "Insert and delete at the head are both O(1) — pointer updates only, no shifting",
  "direction": "single",
  "nodes": [
    {"id": "z", "value": "Tara"},
    {"id": "a", "value": "Alice"},
    {"id": "b", "value": "Bob"},
    {"id": "c", "value": "Carol"}
  ],
  "head": "a",
  "steps": [
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "c", "value": "Carol"}
      ],
      "links": [["a","b"],["b","c"]],
      "markers": [{"name": "head", "nodeId": "a"}],
      "msg": "Starting list: head → Alice → Bob → Carol → null"
    },
    {
      "nodes": [
        {"id": "z", "value": "Tara", "style": "new"},
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "c", "value": "Carol"}
      ],
      "links": [["z","a"],["a","b"],["b","c"]],
      "markers": [{"name": "head", "nodeId": "z"}],
      "msg": "Insert at head: allocate Tara, point it to old head, repoint head — O(1)"
    },
    {
      "nodes": [
        {"id": "z", "value": "Tara"},
        {"id": "a", "value": "Alice", "style": "removed"},
        {"id": "b", "value": "Bob"},
        {"id": "c", "value": "Carol"}
      ],
      "links": [["z","a"],["a","b"],["b","c"]],
      "markers": [{"name": "head", "nodeId": "z"}],
      "msg": "Now delete Alice: mark for removal — pointer fix-ups next"
    },
    {
      "nodes": [
        {"id": "z", "value": "Tara"},
        {"id": "b", "value": "Bob"},
        {"id": "c", "value": "Carol"}
      ],
      "links": [["z","b"],["b","c"]],
      "markers": [{"name": "head", "nodeId": "z"}],
      "msg": "Repoint Tara.next → Bob (skipping Alice); free Alice — O(1)"
    }
  ]
}
```

<p align="center"><strong>Insertion and deletion at the head of a linked list are O(1) — no copying, no shifting, just pointer updates.</strong></p>

Let us look at an example of insertion in a singly linked list to understand this better.

```d3 widget=linked-list
{
  "title": "Insert 'Tara' after 'Bob' — three pointer updates, no shifting",
  "direction": "single",
  "nodes": [
    {"id": "a", "value": "Alice"},
    {"id": "b", "value": "Bob"},
    {"id": "z", "value": "Tara"},
    {"id": "c", "value": "Carol"},
    {"id": "d", "value": "David"}
  ],
  "head": "a",
  "steps": [
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "c", "value": "Carol"},
        {"id": "d", "value": "David"}
      ],
      "links": [["a","b"],["b","c"],["c","d"]],
      "markers": [{"name": "current", "nodeId": "b"}],
      "msg": "Walk to the insertion point — curr at Bob"
    },
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "z", "value": "Tara", "style": "new"},
        {"id": "c", "value": "Carol"},
        {"id": "d", "value": "David"}
      ],
      "links": [["a","b"],["b","c"],["c","d"]],
      "markers": [{"name": "current", "nodeId": "z"}],
      "msg": "Step 1: allocate a new node holding 'Tara'"
    },
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "z", "value": "Tara", "style": "new"},
        {"id": "c", "value": "Carol"},
        {"id": "d", "value": "David"}
      ],
      "links": [["a","b"],["b","c"],["z","c"],["c","d"]],
      "markers": [{"name": "current", "nodeId": "z"}],
      "msg": "Step 2: new.next = Bob.next (point Tara at Carol)"
    },
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "z", "value": "Tara"},
        {"id": "c", "value": "Carol"},
        {"id": "d", "value": "David"}
      ],
      "links": [["a","b"],["b","z"],["z","c"],["c","d"]],
      "markers": [{"name": "current", "nodeId": "b"}],
      "msg": "Step 3: Bob.next = new (Bob now points to Tara)"
    },
    {
      "nodes": [
        {"id": "a", "value": "Alice"},
        {"id": "b", "value": "Bob"},
        {"id": "z", "value": "Tara"},
        {"id": "c", "value": "Carol"},
        {"id": "d", "value": "David"}
      ],
      "links": [["a","b"],["b","z"],["z","c"],["c","d"]],
      "markers": [{"name": "head", "nodeId": "a"}],
      "msg": "Done — Alice → Bob → Tara → Carol → David. Three pointer updates, O(1)."
    }
  ]
}
```

<p align="center"><strong>Inserting 'Tara' after 'Bob' — redirect two pointers; no shifting, no copying, O(1) once the insertion point is known.</strong></p>

## Advantages

A single linked list has a few advantages over traditional arrays, which are listed below.

> -   **Dynamic size:** The size of a linked list is not fixed. Adding or removing items can increase or decrease at will during runtime.
> -   **Efficient performance:** Insertion and deletion of the first node is an **O(1)** operation.

## Limitations

Singly linked lists are not the solution to all our problems. They are very efficient for specific use cases but also have some limitations.

> -   **Extra space:** A little extra memory is required to store an item in a linked list compared to an array. The extra space is used to store the information of the next item in the sequence.
> -   **Traversal:** Traversal in a linked list is more time-consuming than an array since random access using an index is not possible. To access an item at position **n**, one must traverse all the items before it.

A linked list is the most basic but also the most important data structure. Almost all the other data structures build upon the concepts of a linked list, so if there is one data structure that you absolutely must master, it's the linked list.

***

# Defining a node in singly linked list

A **node** is the fundamental building block of a linked list. It holds the actual data item and information of the next node. Multiple nodes, when chained together, make up a single linked list. All the operations on a linked list are performed by manipulating individual nodes and their links. Inserting, deleting, or updating data items in a list are all performed using the list's nodes.

<details>
<summary><h2>Structure of a node</h2></summary>


A singly linked list node has two sections.

> -   **val:** The actual data item a node holds. This could be of any type.
> -   **next:** This is a reference to the next node in the list

```d2
direction: right

node: "A node  —  two fields, one address" {
  grid-columns: 2
  grid-gap: 0
  val: |md
    **val**

    the data (any type)
  |
  next: |md
    **next**

    `ListNode *`
  |
}

n2: "next node in the chain" {
  shape: rectangle
  style.fill: "#f1f5f9"
  style.stroke: "#475569"
}

nullnode: "null  (if this is the tail)" {
  shape: oval
  style.fill: "#fef3c7"
  style.stroke: "#d97706"
  style.italic: true
}

node.next -> n2 {style.stroke-width: 2}
node.next -> nullnode {style.stroke-dash: 4}
```

<p align="center"><strong>A singly linked list node stores two fields: <code>val</code> (the data) and <code>next</code> (the address of the following node, or <code>null</code> if it is the last).</strong></p>

</details>
<details>
<summary><h2>Implementing a node</h2></summary>


To define a node in code, we create a Node class that encapsulates the information a singly linked list node must have: **data** and a reference to the next node. Our class should also have a constructor to initialize the values in nodes at the time of its creation. We can pass in a data value stored in the node and the reference to the next node. We are responsible for linking it to any other node when we see fit.


```python run

class ListNode:
    def __init__(self, val):
        self.val = val
        self.next = None
```

```java run

class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
}
```

</details>


***

# Structure of a singly linked list

A linked list is just a chain of nodes. Below is how these nodes chain together to form a singly linked list.

```d3 widget=linked-list
{
  "title": "Logical chain — four nodes connected by next pointers; tail points to null",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "9"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "head → 5 → 7 → 3 → 9 → null"
    }
  ]
}
```

<p align="center"><strong>Logical representation — nodes appear sequential left to right, each pointing to the next, with the tail pointing to null.</strong></p>

When represented logically in a diagram, these nodes might look sequential (left to right, one after the other), but in reality, they are scattered all around in memory at random locations, and the only way to access a node is by using its address in memory.

```d2
direction: right
n1: |md
  `addr 0x1A4`

  **val: 5**

  `next: 0x3F2`
|
n2: |md
  `addr 0x3F2`

  **val: 7**

  `next: 0x0B8`
|
n3: |md
  `addr 0x0B8`

  **val: 3**

  `next: 0x2C1`
|
n4: |md
  `addr 0x2C1`

  **val: 9**

  `next: null`
|
n1 -> n2: "jump to 0x3F2" {style.stroke-dash: 3}
n2 -> n3: "jump to 0x0B8" {style.stroke-dash: 3}
n3 -> n4: "jump to 0x2C1" {style.stroke-dash: 3}
```

<p align="center"><strong>Physical memory — the four nodes are scattered at unrelated addresses; each node stores the address of the next one so the chain can be followed.</strong></p>

## Head Node

The first node of a linked list is also called the **head** node. As we know, a node in the linked list can only be accessed using its memory reference. This reference, however, is stored in the node before it in the logical representation, and this is true for every node except the first node, as it does not have any previous node. This is why, to access a linked list, we should always have the reference to the head node stored somewhere.

```d2
direction: right
head: head { shape: oval }
n1: {
  val: 5
  next
}
n2: {
  val: 7
  next
}
n3: {
  val: 3
  next: "null"
}
head -> n1.val: "entry point"
n1.next -> n2.val
n2.next -> n3.val
```

<p align="center"><strong>The <code>head</code> pointer is the only entry point to the list — without it, all nodes become unreachable.</strong></p>

## Tail Node

The last node of a linked list is called a **tail** node. Just like the first node does not have any node before it, the last node does not have any node after it. You may wonder what is stored in the pointer of the tail node. The pointer of the tail node stores a reference to `null`, which means nothing. As we will see later, this also helps us determine the end of the linked list.

```d2
direction: right
n1: {
  val: 5
  next
}
n2: {
  val: 7
  next
}
n3: {
  val: 3
  next
  style.fill: "#fef9c3"
  style.stroke: "#d97706"
}
tail: "null — end of list" { shape: oval }
n1.next -> n2.val
n2.next -> n3.val
n3.next -> tail
```

<p align="center"><strong>The tail node's <code>next</code> pointer holds <code>null</code>, signalling the end of the list — traversal stops here.</strong></p>

***

# Overview of supported operations

Now that we know what an individual node of a singly linked list looks like and how these individual nodes link up together to create a singly linked list, we can dive a bit deeper and understand the different operations that can be performed on it.

Every data structure is essentially used to store, retrieve, and manipulate data efficiently. Users can perform these functionalities through a set of operations on the data structure. On a high level, there are three basic types of operations on any data structure.

> -   Traversal
> -   Insertion
> -   Deletion

All other complex operations can be implemented by mixing or piggybacking these fundamental operations. Let's examine some operations we can perform on a singly linked list.

```d3 widget=linked-list
{
  "title": "Some operations on a singly linked list — traversal, insertion, deletion, search",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "5"},
    {"id": "n2", "value": "7"},
    {"id": "n3", "value": "3"},
    {"id": "n4", "value": "9"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Starting list: head → 5 → 7 → 3 → 9 → null"
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n1"}],
      "msg": "Traversal — start curr at head"
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "current", "nodeId": "n3"}],
      "msg": "Traversal — curr advances node-by-node (O(n))"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7"},
        {"id": "n3", "value": "3"},
        {"id": "ins", "value": "8", "style": "new"},
        {"id": "n4", "value": "9"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","ins"],["ins","n4"]],
      "markers": [{"name": "current", "nodeId": "ins"}],
      "msg": "Insertion — splice a new node holding 8 after the node holding 3"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n2", "value": "7", "style": "removed"},
        {"id": "n3", "value": "3"},
        {"id": "ins", "value": "8"},
        {"id": "n4", "value": "9"}
      ],
      "links": [["n1","n2"],["n2","n3"],["n3","ins"],["ins","n4"]],
      "markers": [{"name": "current", "nodeId": "n2"}],
      "msg": "Deletion — mark the node holding 7 for removal"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n3", "value": "3"},
        {"id": "ins", "value": "8"},
        {"id": "n4", "value": "9"}
      ],
      "links": [["n1","n3"],["n3","ins"],["ins","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Repoint 5.next → 3, free 7 — list is now 5 → 3 → 8 → 9"
    },
    {
      "nodes": [
        {"id": "n1", "value": "5"},
        {"id": "n3", "value": "3", "style": "highlight"},
        {"id": "ins", "value": "8"},
        {"id": "n4", "value": "9"}
      ],
      "links": [["n1","n3"],["n3","ins"],["ins","n4"]],
      "markers": [{"name": "current", "nodeId": "n3"}],
      "msg": "Search for value 3 — walk the chain, match found at this node (O(n))"
    }
  ]
}
```

<p align="center"><strong>The four fundamental operations on a singly linked list — traversal and search are always O(n); head insertion and deletion are O(1).</strong></p>

Don't worry if you don't understand all of these operations yet. We will explore them in more detail later in the course. Each of these operations is built from a combination of basic ones, and once you've mastered the fundamentals, the intuition behind the more complex operations will become clear.

***

# Boundary Node

A first problem to check that the two ideas you just met — **the `head` pointer** and **the tail's `next == null`** — have actually clicked. No traversal allowed. Two comparisons. Done.

## The Problem

> Given the **head** of a singly linked list and a reference to a **random** node in the same list, return:
>
> - `first` — the node is the head.
> - `last` — the node is the tail (its `next` is null).
> - `both` — it is simultaneously the head and the tail (list of size 1).
> - `none` — it is somewhere in the middle.
>
> **Constraint:** you must answer in **O(1)** — no walking the list. The list contains no duplicates.

```
Input:  head = [5, 7, 3, 10], node = 5
Output: first

Input:  head = [5, 7, 3, 10], node = 10
Output: last

Input:  head = [5], node = 5
Output: both

Input:  head = [5, 7, 3, 10], node = 3
Output: none
```

---

<details>
<summary><h2>What Makes a Node a "Boundary"?</h2></summary>


A linked list has exactly two structural landmarks: the **head** (entry point, nothing points *to* it) and the **tail** (exit point, nothing points *from* it — its `next` is `null`). Every other node is interior — reachable through its predecessor and pointing to a successor. The entire problem reduces to answering **two yes/no questions** about the given node:

- *Is this node the one the head reference points to?*
- *Is this node's `next` pointer null?*

```d2
direction: right
head: head { shape: oval }
n1: {
  val: 5
  next
  style.fill: "#dbeafe"
  style.stroke: "#3b82f6"
}
n2: {
  val: 7
  next
}
n3: {
  val: 3
  next
}
n4: {
  val: 10
  next: "null"
  style.fill: "#fef9c3"
  style.stroke: "#d97706"
}
head -> n1.val
n1.next -> n2.val
n2.next -> n3.val
n3.next -> n4.val

q1: "Q1: node == head?" {shape: oval}
q2: "Q2: node.next == null?" {shape: oval}
q1 -> n1: "" {style.stroke-dash: 3}
q2 -> n4: "" {style.stroke-dash: 3}
```

<p align="center"><strong>Two questions identify the four cases. The head answers yes to Q1 only, the tail to Q2 only, a single-node list to both, and every interior node to neither.</strong></p>

The truth table writes itself:

| `isHead` | `isTail` | Return |
|---|---|---|
| true | true | `both` |
| true | false | `first` |
| false | true | `last` |
| false | false | `none` |

> *Before reading the solution — why can we do this in O(1) instead of O(N)? What property of the given node lets us check "is this the tail?" without walking to the end?*

Because the tail is the *only* node whose `next` is `null`. The given node itself carries that information — we don't have to find the tail, we just ask "is your `next` null?" and believe the answer. That's the whole trick, and it's a preview of a powerful lesson: **a linked list node is a self-describing object**. Most questions about a single node can be answered locally, without traversal.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### The Solution

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
    def boundary_node(
        self, head: Optional[ListNode], node: Optional[ListNode]
    ) -> str:

        # If either head or node is None, return "none"
        if not head or not node:
            return "none"

        # If head and node are the same, and node has no next node,
        # return "both"
        elif node == head and not node.next:
            return "both"

        # If head and node are the same, but node has a next node, return
        # "first"
        elif node == head:
            return "first"

        # If node is the last node (i.e., it has no next node), return
        # "last"
        elif not node.next:
            return "last"

        # If none of the above conditions are met, return "none"
        return "none"


# Examples from the problem statement
h1 = from_list([5, 7, 3, 10])
print(Solution().boundary_node(h1, h1))                     # first

h2 = from_list([5, 7, 3, 10])
last = h2.next.next.next
print(Solution().boundary_node(h2, last))                   # last

h3 = from_list([5])
print(Solution().boundary_node(h3, h3))                     # both

h4 = from_list([5, 7, 3, 10])
mid = h4.next.next                                          # node with val 3
print(Solution().boundary_node(h4, mid))                    # none

# Edge cases
print(Solution().boundary_node(None, None))                 # none

h5 = from_list([1, 2])
print(Solution().boundary_node(h5, h5))                     # first
print(Solution().boundary_node(h5, h5.next))                # last
```

```java run
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
        public String boundaryNode(ListNode head, ListNode node) {

            // If either head or node is null, return "none"
            if (head == null || node == null) {
                return "none";
            }

            // If head and node are the same, and node has no next node,
            // return "both"
            else if (node == head && node.next == null) {
                return "both";
            }

            // If head and node are the same, but node has a next node,
            // return "first"
            else if (node == head) {
                return "first";
            }

            // If node is the last node (i.e., it has no next node), return
            // "last"
            else if (node.next == null) {
                return "last";
            }

            // If none of the above conditions are met, return "none"
            return "none";
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        ListNode h1 = fromList(5, 7, 3, 10);
        System.out.println(new Solution().boundaryNode(h1, h1));                 // first

        ListNode h2 = fromList(5, 7, 3, 10);
        ListNode last = h2.next.next.next;
        System.out.println(new Solution().boundaryNode(h2, last));               // last

        ListNode h3 = fromList(5);
        System.out.println(new Solution().boundaryNode(h3, h3));                 // both

        ListNode h4 = fromList(5, 7, 3, 10);
        ListNode mid = h4.next.next;                                             // node with val 3
        System.out.println(new Solution().boundaryNode(h4, mid));                // none

        // Edge cases
        System.out.println(new Solution().boundaryNode(null, null));             // none

        ListNode h5 = fromList(1, 2);
        System.out.println(new Solution().boundaryNode(h5, h5));                 // first
        System.out.println(new Solution().boundaryNode(h5, h5.next));            // last
    }
}
```


<details>
<summary><strong>Trace — head = [5, 7, 3, 10], node = the node holding 10</strong></summary>

```
Step 1: head != null AND node != null → not "none"
Step 2: node == head?           →  node holds 10, head holds 5   →  false → skip "both"
Step 3: node == head?           →  same check                    →  false → skip "first"
Step 4: node.next == null?      →  10's next is null             →  true  → return "last" ✓
```

</details>
<details>
<summary><strong>Trace — head = [5], node = the only node</strong></summary>

```
Step 1: head != null AND node != null → not "none"
Step 2: node == head AND node.next == null?
        both reference the same object, and next is null  →  true  → return "both" ✓

A single-node list is simultaneously the head AND the tail — the "both" branch
fires when both conditions hold in the same elif arm.
```

</details>

### Complexity Analysis

| | Complexity | Reasoning |
|---|---|---|
| **Time** | O(1) | Two constant-time comparisons — no loop, no traversal |
| **Space** | O(1) | Two booleans; no auxiliary structures |

The entire point of the problem is to prove the O(1) bound. Any solution that walks the list (to, say, find the tail by counting) already misses the lesson.

### Edge Cases

| Case | Example | Expected | Reasoning |
|---|---|---|---|
| `head == null` | empty list | `none` | No list → nothing is a boundary |
| `node == null` | caller passed nothing | `none` | Nothing to classify |
| Single-node list, node is it | `[5]`, node=5 | `both` | Head and tail coincide |
| Node is the head | `[5, 7, 3]`, node=5 | `first` | Only Q1 fires |
| Node is the tail | `[5, 7, 3]`, node=3 | `last` | Only Q2 fires |
| Interior node | `[5, 7, 3]`, node=7 | `none` | Neither Q1 nor Q2 fires |
| Two-node list, node is middle | impossible | — | In a 2-node list there is no middle — every node is a boundary |

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Every linked-list problem you'll ever solve starts with one of two questions: *"is this the head?"* or *"does this node have a successor?"* You just wrote the answer to both. Memorise the pattern — **`node == head`** is pointer identity, **`node.next == null`** is tail detection — because these two checks will reappear in every insertion, deletion, reversal, and traversal problem to come. Notice something deeper: we didn't actually need the *value* stored in the node. Linked-list problems are almost always about **structure**, not data.

> **Transfer Challenge:** Extend the function to return the node's **position** in the list — `0` for head, `-1` for tail, `-2` if it's both, or a positive index for interior nodes. Now you *are* allowed to traverse. What's the minimum number of traversals needed? One or two?
>
> <details><summary><strong>Solution hint</strong></summary>
>
> One pass suffices. Walk from the head, incrementing an index counter. When you hit the target node, remember its index and whether it was the head (index 0) — but keep walking to see if it's also the tail (its `next` was null when you hit it). Return `-2` if index was 0 and it was the tail, `0` if index was 0 and there's more list after it, `-1` if it was the tail but not the head, otherwise the positive index.
>
> </details>

</details>