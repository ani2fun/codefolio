# Trees

The moment your data has **hierarchy** — a parent above children, a path from a root to a leaf — you've left the world of linear structures. Trees are the answer when "next-and-previous" stops being enough: filesystem directories, organisation charts, expression parsers, syntax highlighters, every database index, every priority queue.

This module starts with the bare binary tree (just shape and pointers), adds the BST rule (one constraint that turns a tree into a map), then climbs through the structures that real systems run on: balanced BSTs, B-trees, tries, segment trees, Fenwick trees, and the disjoint-set union-find.

## Place in the curriculum

- **Prerequisites:** [Linear Structures](/cortex/data-structures-and-algorithms/linear-structures-index) (the array layout used by heaps; the linked-list pointer model used by every other tree).
- **Followed by:** [Graphs](/cortex/data-structures-and-algorithms/graphs-index) (a tree is a connected acyclic graph; once you've internalised tree traversal, BFS/DFS on graphs is one extra rule away).

## Topics

1. [Binary Tree](/cortex/data-structures-and-algorithms/trees-binary-tree-index) — root, leaves, levels, height, traversals (preorder, inorder, postorder, level-order).
2. [Binary Search Tree](/cortex/data-structures-and-algorithms/trees-binary-search-tree-index) — the BST property; search, insert, delete; lowest common ancestor; ordered iteration.
3. [Heap](/cortex/data-structures-and-algorithms/trees-heap-index) — completeness + heap-ordering; priority queues; top-K patterns; the array layout that compiles into 5 lines of tight code.
4. [Trie](/cortex/data-structures-and-algorithms/trees-trie-introduction-to-tries) — prefix-shared paths; behind every autocomplete and `grep -F`.
5. [Self-Balancing BSTs — Overview](/cortex/data-structures-and-algorithms/trees-self-balancing-bst-overview-self-balancing-bst-overview) — why naive BST goes O(n); the rebalancing menu (AVL, RB, treap, skip list).
6. [AVL Tree](/cortex/data-structures-and-algorithms/trees-avl-tree-introduction-to-avl-trees) — strict balance, four rotation cases, faster lookups than RB but slower writes.
7. [Red-Black Tree](/cortex/data-structures-and-algorithms/trees-red-black-tree-introduction-to-red-black-trees) — five invariants; the structure inside Linux's `lib/rbtree.c`, Java's `TreeMap`, C++'s `std::map`.
8. [B-Tree](/cortex/data-structures-and-algorithms/trees-b-tree-introduction-to-b-trees) — the disk-aware tree; behind every relational database index.
9. [Segment Tree](/cortex/data-structures-and-algorithms/trees-segment-tree-introduction-to-segment-trees) — range queries and updates in O(log n) with lazy propagation.
10. [Fenwick Tree (Binary Indexed Tree)](/cortex/data-structures-and-algorithms/trees-fenwick-tree-introduction-to-fenwick-trees) — the half-page implementation that competitive programmers reach for first.
11. [Disjoint Set Union (Union-Find)](/cortex/data-structures-and-algorithms/trees-disjoint-set-union-introduction-to-disjoint-set-union) — path compression + union by rank; the secret weapon behind Kruskal's MST and offline connectivity.
