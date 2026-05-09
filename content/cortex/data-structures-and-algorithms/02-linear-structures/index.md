# Linear Structures

Data laid out **in a row**. Every structure here gives you a sequence of values you can walk from one end to the other. The differences are in *how* they support insertion, lookup, and removal — and those differences cascade through every higher-level data structure you'll meet later.

## Place in the curriculum

- **Prerequisites:** [Foundations](/cortex/data-structures-and-algorithms/foundations-index) (asymptotic analysis is cited heavily in this module).
- **Followed by:** [Trees](/cortex/data-structures-and-algorithms/trees-index) (binary tree's array layout is the same trick as the heap-on-array layout you'll meet here in the heap chapter), [Graphs](/cortex/data-structures-and-algorithms/graphs-index) (adjacency lists are linked lists), [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) (most patterns operate on linear data first).

## Topics

1. [Arrays](/cortex/data-structures-and-algorithms/linear-structures-arrays-index) — contiguous memory, O(1) random access, fixed or dynamic size. The default for "I have a sequence of values".
2. **Strings** — *stub*. UTF-8 vs ASCII, immutability, the "string is just an array of characters" lie.
3. [Singly Linked List](/cortex/data-structures-and-algorithms/linear-structures-singly-linked-list-index) — pointer-chained nodes, O(1) head insert, O(n) random access. Trades cache-friendliness for cheap insertion anywhere.
4. [Doubly Linked List](/cortex/data-structures-and-algorithms/linear-structures-doubly-linked-list-index) — bidirectional pointers, O(1) deletion given the node. The structure behind every textbook LRU cache.
5. [Stack](/cortex/data-structures-and-algorithms/linear-structures-stack-index) — LIFO. Behind every undo button, every recursion, every expression evaluator.
6. [Queue](/cortex/data-structures-and-algorithms/linear-structures-queue-index) — FIFO. Behind every BFS, every job runner, every print spooler.
7. [Hash Table](/cortex/data-structures-and-algorithms/linear-structures-hash-table-index) — O(1) average insert/lookup/delete via a hash function and a backing array. The default for "I need a Map".
