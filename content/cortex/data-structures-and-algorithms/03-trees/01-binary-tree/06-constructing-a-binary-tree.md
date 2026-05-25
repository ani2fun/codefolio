---
title: "Constructing A Binary Tree"
summary: "<!-- TODO: summary -->"
---

# 6. Constructing a Binary Tree

## The Hook

The previous two lessons taught us how to *flatten* a tree into a one-dimensional sequence — preorder, inorder, postorder, level-order. Each traversal turns the tree into a list of values. Now we run the question backwards: **given the list, can we recover the tree?**

It would be amazing if a single traversal sufficed. *It does not* — and the proof is short. Different trees can have *identical* traversals when only one ordering is used. Show someone a preorder sequence and they can build *several* different trees that produce it; show them an inorder sequence and the situation is even worse (you can't even identify which value is the root). Postorder shares preorder's problem from the other end. Each ordering, alone, throws away information that *cannot* be recovered.

But — and here is the magic — *any two of these traversals together*, combined with one of them being inorder, **uniquely determine the tree**. Pre+in, post+in: each pair gives you exactly one tree, no ambiguity. The construction is a beautiful divide-and-conquer recursion: pre/postorder tells you who the root is; inorder tells you which values fell on the left of that root and which on the right; recurse on the two halves; done.

This is more than a theoretical curiosity. *Tree serialisation* — the process of turning a tree into a sequence so it can be sent over a network or written to disk — relies on this idea. So does *deserialisation* (the reverse). Many compilers and editors store ASTs as a *pair* of preorder + inorder dumps, then rebuild on load. The "list of nodes" you see when you `JSON.stringify` a parser's AST is, structurally, a serialised traversal — and the loader function is what we're about to build.

This lesson explains why no single traversal is enough, walks through *why* pre+in and post+in pair up to determine the tree uniquely, and implements both reconstruction algorithms in Python and Java. By the end you'll be able to build trees from traversals on demand — a frequent interview problem and a building block we'll lean on later.

---

## Table of contents

1. [Why one traversal is not enough](#why-one-traversal-is-not-enough)
2. [Why two traversals (with inorder) suffice](#why-two-traversals-with-inorder-suffice)
3. [Construction from preorder + inorder](#construction-from-preorder--inorder)
4. [Construction from postorder + inorder](#construction-from-postorder--inorder)
5. [What about preorder + postorder?](#what-about-preorder--postorder)

***

# Why one traversal is not enough

Consider this preorder sequence: **`[1, 2, 3]`**.

How many distinct binary trees produce that preorder? At least *five*:

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
    subgraph T1["A"]
        A1((1))
        A2((2))
        A3((3))
        A1 --> A2
        A1 --> A3
    end
    subgraph T2["B"]
        B1((1))
        B2((2))
        B3((3))
        B1 --> B2
        B2 --> B3
    end
    subgraph T3["C"]
        C1((1))
        C2((2))
        C3((3))
        C1 --> C2
        C2 -.- CN[" "]
        C2 --> C3
        style CN fill:none,stroke:none
    end
    subgraph T4["D"]
        D1((1))
        D2((2))
        D3((3))
        D1 --> D2
        D1 -.- DN[" "]
        D2 --> D3
        style DN fill:none,stroke:none
    end
    subgraph T5["E"]
        E1((1))
        E2((2))
        E3((3))
        E1 -.- EN[" "]
        E1 --> E2
        E2 --> E3
        style EN fill:none,stroke:none
    end
```

<p align="center"><strong>Five different trees, all with the same preorder <code>[1, 2, 3]</code>. Preorder fixes the order in which values are <em>visited</em>, but says nothing about whether the next value is the current node's left child, the current node's right child, or some ancestor's right child. The shape is genuinely ambiguous.</strong></p>

The root of *every* tree above is `1` — that part is unambiguous (preorder visits the root first). But after that, `2` could be `1`'s left child, or `1`'s right child (if `1` has no left). And `3` could be `2`'s left child, `2`'s right child, *or* `1`'s right child. The decision at each step is unconstrained.

## Inorder alone is even worse

For inorder, **you can't even identify the root**. Given `[4, 2, 1, 3, 7]`, where's the root? Anywhere. The root could be `4` (with `[]` to the left and `[2, 1, 3, 7]` to the right); or `2` (with `[4]` to the left and `[1, 3, 7]` to the right); or any of the others. Without more information, every value is equally plausible.

## Postorder alone has the same problem as preorder

Symmetrically, postorder *visits the root last* — so you know the root, but the rest of the sequence is ambiguous in the same way preorder's tail is.

## Level-order alone

Level-order at least identifies the root (always first), and identifies the children of each level — but it can't distinguish whether a node has a missing left child or a missing right child *unless null markers are explicitly included*. The standard "compact" level-order serialisation (no nulls) still leaves shape ambiguous; the "verbose" form (with nulls) is unambiguous but uses extra space proportional to the number of `null` markers.

> *Predict before reading on — for the preorder <code>[1, 2, 3]</code>, what does adding the inorder <code>[2, 1, 3]</code> uniquely tell us?*
>
> The root is `1` (from preorder's first element). In the inorder, `1` appears at index 1 — so `[2]` is the left subtree and `[3]` is the right subtree. That uniquely picks out **Tree A** from our five candidates above. Combining the two orderings turned five possibilities into one — the entire idea of this lesson.

***

# Why two traversals (with inorder) suffice

The recipe is the same for both pre+in and post+in. Here's the high-level recursion for **preorder + inorder**:

> 1. The **first** value in the *current preorder slice* is the root of the *current subtree*.
> 2. Find that root in the *current inorder slice*. Everything to its **left** in the inorder slice is the *left subtree*; everything to its **right** is the *right subtree*.
> 3. Recurse on the left subtree (using the matching prefix of the preorder slice).
> 4. Recurse on the right subtree (using the matching suffix of the preorder slice).

The *crux* is the inorder split: it tells you exactly *which* values belong to the left subtree and *which* to the right. Without it, you'd have to guess; with it, you can divide the sub-problem into two halves of *exactly the right shape*.

For **postorder + inorder**, the recipe is mirrored: postorder visits the root *last*, so the root of the current subtree is the *last* element of the postorder slice. Once you know the root, the inorder split works the same way.

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
    PRE["preorder = [1, 2, 4, 3, 7]
inorder  = [4, 2, 1, 3, 7]"]
    STEP1["root is preorder[0] = 1
find 1 in inorder → index 2
left subtree inorder = [4, 2]
right subtree inorder = [3, 7]
left subtree preorder = next 2 values = [2, 4]
right subtree preorder = remaining = [3, 7]"]
    STEP2["recurse on (preorder=[2,4], inorder=[4,2])
recurse on (preorder=[3,7], inorder=[3,7])"]
    PRE --> STEP1 --> STEP2
```

<p align="center"><strong>One step of the recursion — the preorder front gives the current root; the inorder split gives the left and right subtree boundaries. The matching slice of the preorder array is recovered by counting (the left subtree's preorder slice has the same length as the left subtree's inorder slice).</strong></p>

> **Why must one of the two be inorder?** Because *only* inorder lets you cleanly *partition* the array around the root. Pre+post (without inorder) tells you the root from both ends but gives you no partition — you can match a value across the two arrays but you can't tell which children are on the left vs right of the root.

***

# Construction from preorder + inorder

Let's tighten the algorithm into one we can implement.

<details>
<summary><h2>Algorithm</h2></summary>


We use two helpers: a *moving index* into the preorder array (the next root to consume), and a *range* `[inStart, inEnd]` describing which slice of the inorder array we're working with.

> **Algorithm**
>
> -   **Step 1:** Initialise `preIndex = 0`. Call `build(0, len(inorder) − 1)`.
> -   **Step 2:** `build(inStart, inEnd)`:
>     -   If `inStart > inEnd`, return `null` (empty subtree).
>     -   `rootVal = preorder[preIndex]`; `preIndex++`.
>     -   Create `node = TreeNode(rootVal)`.
>     -   Find `idx`, the index of `rootVal` in `inorder[inStart..inEnd]`.
>     -   `node.left  = build(inStart, idx − 1)`.
>     -   `node.right = build(idx + 1, inEnd)`.
>     -   Return `node`.

The `preIndex` advances **before** the recursive calls, and the order matters: the left subtree consumes preorder values *first* (because it's traversed first), then the right subtree.

</details>
<details>
<summary><h2>A subtlety — speeding up the lookup</h2></summary>


The implementation below uses a `find_index` helper that does a **linear scan** of `inorder[inStart..inEnd]` to locate the root — straightforward, but it makes the worst case **O(N²)** for a skew tree. For interview-quality solutions you can build a **value → index** hash map of the inorder array up front, making each lookup O(1) and the whole construction **O(N)**. We show the simple linear-scan version for clarity; swapping `find_index` for a pre-built map is the one change needed to reach O(N).

> *Predict before reading on — what's the complexity if you skip the hash map?*
>
> Worst case **O(N²)** — a skew tree forces every recursive call's `find_index` to scan O(N) of the array. The hash map fix makes that lookup O(1) and the overall complexity falls to O(N) — but the recursive partitioning still uses O(h) call-stack space.

</details>
<details>
<summary><h2>Worked example</h2></summary>


> Preorder: `[1, 2, 4, 3, 7]`
> Inorder:  `[4, 2, 1, 3, 7]`

| Call                         | preIndex | rootVal | idx in inorder | inStart..inEnd | Result        |
|------------------------------|----------|---------|----------------|----------------|---------------|
| `build(0, 4)` (whole tree)   | 0        | 1       | 2              | 0..4           | root          |
| `build(0, 1)` (left of 1)    | 1        | 2       | 1              | 0..1           | left subtree  |
| `build(0, 0)` (left of 2)    | 2        | 4       | 0              | 0..0           | leaf 4        |
| `build(0, −1)` (left of 4)   | 3        | —       | —              | empty          | `null`        |
| `build(1, 0)` (right of 4)   | 3        | —       | —              | empty          | `null`        |
| `build(2, 1)` (right of 2)   | 3        | —       | —              | empty          | `null`        |
| `build(3, 4)` (right of 1)   | 3        | 3       | 3              | 3..4           | right subtree |
| `build(3, 2)` (left of 3)    | 4        | —       | —              | empty          | `null`        |
| `build(4, 4)` (right of 3)   | 4        | 7       | 4              | 4..4           | leaf 7        |

Final tree:

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
    R((1))
    A((2))
    B((3))
    C((4))
    D((7))
    R --> A
    R --> B
    A --> C
    B --> D
    A -.- AN[" "]
    B -.- BN[" "]
    style AN fill:none,stroke:none
    style BN fill:none,stroke:none
```

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run viz=binary-tree viz-root=root
from typing import Optional, List, Any
from collections import deque


class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right


def to_level_order(root):
    """Serialize tree to level-order list with None for missing children."""
    if not root:
        return []
    result = []
    queue = deque([root])
    while queue:
        node = queue.popleft()
        if node:
            result.append(node.val)
            queue.append(node.left)
            queue.append(node.right)
        else:
            result.append(None)
    # Trim trailing Nones
    while result and result[-1] is None:
        result.pop()
    return result


class Solution:
    def __init__(self):

        # Global variable to keep track of the current index in the
        # preorder traversal
        self.pre_ind: int = 0

    # Helper function to find the index of a given value in the inorder
    # traversal
    def find_index(
        self, inorder: List[int], start: int, end: int, val: int
    ) -> int:
        for i in range(start, end + 1):
            if inorder[i] == val:
                return i

        # If the value is not found in the inorder array, return the
        # start index
        return start

    def build_tree(
        self,
        inorder: List[int],
        in_start: int,
        in_end: int,
        preorder: List[int],
    ) -> Optional[TreeNode]:

        # Base case: if the inorder range is empty, return None to
        # indicate an empty subtree
        if in_start > in_end:
            return None

        # Create a new node using the current value from the preorder
        # traversal
        current_node: TreeNode = TreeNode(preorder[self.pre_ind])

        # Find the index of the current value in the inorder traversal
        index = self.find_index(
            inorder, in_start, in_end, preorder[self.pre_ind]
        )

        # Move to the next value in the preorder traversal
        self.pre_ind += 1

        # Recursively construct the left and right subtrees using the
        # appropriate ranges of the inorder and preorder traversals
        current_node.left = self.build_tree(
            inorder, in_start, index - 1, preorder
        )
        current_node.right = self.build_tree(
            inorder, index + 1, in_end, preorder
        )

        # Return the current node, which is the root of the constructed
        # subtree
        return current_node

    def preorder_and_inorder_reconstruction(
        self, preorder: List[int], inorder: List[int]
    ) -> Optional[TreeNode]:

        # Call the recursive build_tree function with the entire ranges
        # of inorder and preorder traversals
        return self.build_tree(inorder, 0, len(inorder) - 1, preorder)


# Examples from the problem statement
r1 = Solution().preorder_and_inorder_reconstruction([1, 2, 4, 3, 7, 9], [4, 2, 1, 3, 9, 7])
print(to_level_order(r1))  # [1, 2, 3, 4, None, None, 7, None, None, 9]

r2 = Solution().preorder_and_inorder_reconstruction([1, 8, 6, 4], [8, 6, 1, 4])
print(to_level_order(r2))  # [1, 8, 4, None, 6]

# Edge cases
r3 = Solution().preorder_and_inorder_reconstruction([], [])
print(to_level_order(r3))  # []

r4 = Solution().preorder_and_inorder_reconstruction([1], [1])
print(to_level_order(r4))  # [1]

r5 = Solution().preorder_and_inorder_reconstruction([1, 2, 3], [1, 2, 3])
print(to_level_order(r5))  # [1, None, 2, None, 3]

r6 = Solution().preorder_and_inorder_reconstruction([1, 2, 3], [3, 2, 1])
print(to_level_order(r6))  # [1, 2, None, 3]

r7 = Solution().preorder_and_inorder_reconstruction([4, 2, 1, 3, 6, 5, 7], [1, 2, 3, 4, 5, 6, 7])
print(to_level_order(r7))  # [4, 2, 6, 1, 3, 5, 7]
```

```java run
import java.util.*;

public class Main {
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
    }

    static List<Integer> toLevelOrder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node != null) {
                result.add(node.val);
                queue.add(node.left);
                queue.add(node.right);
            } else {
                result.add(null);
            }
        }
        while (!result.isEmpty() && result.get(result.size() - 1) == null) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    static class Solution {

        // Global variable to keep track of the current index in the preorder
        // traversal
        private int preInd = 0;

        // Helper function to find the index of a given value in the inorder
        // traversal
        private int findIndex(int[] inorder, int start, int end, int val) {
            for (int i = start; i <= end; i++) {
                if (inorder[i] == val) {
                    return i;
                }
            }

            // If the value is not found in the inorder array, return the
            // start index
            return start;
        }

        private TreeNode buildTree(
            int[] inorder,
            int inStart,
            int inEnd,
            int[] preorder
        ) {

            // Base case: if the inorder range is empty, return null to
            // indicate an empty subtree
            if (inStart > inEnd) {
                return null;
            }

            // Create a new node using the current value from the preorder
            // traversal
            TreeNode currentNode = new TreeNode(preorder[preInd]);

            // Find the index of the current value in the inorder traversal
            int index = findIndex(inorder, inStart, inEnd, preorder[preInd]);

            // Move to the next value in the preorder traversal
            preInd++;

            // Recursively construct the left and right subtrees using the
            // appropriate ranges of the inorder and preorder traversals
            currentNode.left = buildTree(
                inorder,
                inStart,
                index - 1,
                preorder
            );
            currentNode.right = buildTree(
                inorder,
                index + 1,
                inEnd,
                preorder
            );

            // Return the current node, which is the root of the constructed
            // subtree
            return currentNode;
        }

        public TreeNode preorderAndInorderReconstruction(
            int[] preorder,
            int[] inorder
        ) {

            // Call the recursive buildTree function with the entire ranges
            // of inorder and preorder traversals
            return buildTree(inorder, 0, inorder.length - 1, preorder);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{1, 2, 4, 3, 7, 9}, new int[]{4, 2, 1, 3, 9, 7})));  // [1, 2, 3, 4, null, null, 7, null, null, 9]

        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{1, 8, 6, 4}, new int[]{8, 6, 1, 4})));               // [1, 8, 4, null, 6]

        // Edge cases
        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{}, new int[]{})));                                     // []

        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{1}, new int[]{1})));                                   // [1]

        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{1, 2, 3}, new int[]{1, 2, 3})));                      // [1, null, 2, null, 3]

        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{1, 2, 3}, new int[]{3, 2, 1})));                      // [1, 2, null, 3]

        System.out.println(toLevelOrder(new Solution().preorderAndInorderReconstruction(
            new int[]{4, 2, 1, 3, 6, 5, 7}, new int[]{1, 2, 3, 4, 5, 6, 7})));  // [4, 2, 6, 1, 3, 5, 7]
    }
}
```

### Complexity

The implementation above uses the linear-scan `find_index`, so:

- Each recursive call does an O(N) scan to find the root in the inorder slice → **O(N²) time** in the worst case (a skew tree).
- Swapping `find_index` for a pre-built **value → inorder index** hash map makes each lookup O(1) → **O(N) time**.
- Space: O(N) for the constructed tree, plus **O(h)** for the recursive call stack.

</details>

***

# Construction from postorder + inorder

The mirror image of the previous problem. Postorder visits the root *last*, so we walk *backwards* through the postorder array (or use a moving index that decrements).

A second mirror twist: when we discover the root and split the inorder into left/right halves, we then need to recurse into the **right** subtree *first* (because in postorder, the right subtree is processed *just before* the root). The left subtree's postorder values come *before* the right subtree's, so processing the right first lets us consume the postorder array from the back in the correct order.

<details>
<summary><h2>Algorithm</h2></summary>


> **Algorithm**
>
> -   **Step 1:** Initialise `postIndex = len(postorder) − 1`. Call `build(0, len(inorder) − 1)`.
> -   **Step 2:** `build(inStart, inEnd)`:
>     -   If `inStart > inEnd`, return `null`.
>     -   `rootVal = postorder[postIndex]`; `postIndex--`.
>     -   `node = TreeNode(rootVal)`.
>     -   Find `idx`, the index of `rootVal` in `inorder[inStart..inEnd]`.
>     -   `node.right = build(idx + 1, inEnd)`     ← right first!
>     -   `node.left  = build(inStart, idx − 1)`
>     -   Return `node`.

The "right first" reversal is the only structural difference from the pre+in version. Everything else (the linear-scan root lookup, the recursion, the complexity) is identical.

</details>
<details>
<summary><h2>Worked example</h2></summary>


> Postorder: `[4, 2, 7, 3, 1]`
> Inorder:   `[4, 2, 1, 3, 7]`

| Call                          | postIdx | rootVal | inorder split                          |
|-------------------------------|---------|---------|----------------------------------------|
| `build(0, 4)`                 | 4       | 1       | left `[4, 2]`, right `[3, 7]`          |
| `build(3, 4)` (right of 1)    | 3       | 3       | left `[]`, right `[7]`                 |
| `build(4, 4)` (right of 3)    | 2       | 7       | leaf                                   |
| `build(3, 2)` (left of 3)     | 1       | —       | empty → null                           |
| `build(0, 1)` (left of 1)     | 1       | 2       | left `[4]`, right `[]`                 |
| `build(1, 1)` (right of 2)    | 0       | —       | empty → null                           |
| `build(0, 0)` (left of 2)     | 0       | 4       | leaf                                   |

Result is the same tree as before — pre+in and post+in *both* uniquely reconstruct the same tree from the same input data.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

The Python and Java versions in full; compared to the pre+in code, the only differences are `pre_ind` becomes `post_ind` (initialised to the *last* index and *decremented*) and the recursion order swaps to right-then-left.


```python run viz=binary-tree viz-root=root
from typing import Optional, List
from collections import deque


class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right


def to_level_order(root):
    """Serialize tree to level-order list with None for missing children."""
    if not root:
        return []
    result = []
    queue = deque([root])
    while queue:
        node = queue.popleft()
        if node:
            result.append(node.val)
            queue.append(node.left)
            queue.append(node.right)
        else:
            result.append(None)
    while result and result[-1] is None:
        result.pop()
    return result


class Solution:
    def __init__(self):

        # Global variable to keep track of the index in the postorder
        # traversal
        self.post_ind: int = 0

    def find_index(
        self, inorder: List[int], start: int, end: int, val: int
    ) -> int:

        # Helper function to find the index of a given value in the
        # inorder traversal
        for i in range(start, end + 1):
            if inorder[i] == val:
                return i

        # If the value is not found in the inorder array, return the
        # start index
        return start

    def build_tree(
        self,
        inorder: List[int],
        in_start: int,
        in_end: int,
        postorder: List[int],
    ) -> Optional[TreeNode]:

        # Base case: If the current inorder range is empty, return None
        if in_start > in_end:
            return None

        # Create a new node with the current postorder element
        current_node: TreeNode = TreeNode(postorder[self.post_ind])

        # Find the index of this element in inorder
        index = self.find_index(
            inorder, in_start, in_end, postorder[self.post_ind]
        )

        # Move to the next postorder element
        self.post_ind -= 1

        # Recursively build the right subtree with elements after the
        # current index in inorder
        current_node.right = self.build_tree(
            inorder, index + 1, in_end, postorder
        )

        # Recursively build the left subtree with elements before the
        # current index in inorder
        current_node.left = self.build_tree(
            inorder, in_start, index - 1, postorder
        )

        # Return the current node with its left and right subtrees
        # constructed
        return current_node

    def postorder_and_inorder_reconstruction(
        self, postorder: List[int], inorder: List[int]
    ) -> Optional[TreeNode]:

        # Initialize the post_ind to the last index of the postorder
        # traversal.
        self.post_ind = len(postorder) - 1

        # Call the helper function with the full range of inorder
        # traversal.
        return self.build_tree(inorder, 0, len(inorder) - 1, postorder)


# Examples from the problem statement
r1 = Solution().postorder_and_inorder_reconstruction([4, 2, 9, 7, 3, 1], [4, 2, 1, 3, 9, 7])
print(to_level_order(r1))  # [1, 2, 3, 4, None, None, 7, None, None, 9]

r2 = Solution().postorder_and_inorder_reconstruction([6, 8, 4, 1], [8, 6, 1, 4])
print(to_level_order(r2))  # [1, 8, 4, None, 6]

# Edge cases
r3 = Solution().postorder_and_inorder_reconstruction([], [])
print(to_level_order(r3))  # []

r4 = Solution().postorder_and_inorder_reconstruction([1], [1])
print(to_level_order(r4))  # [1]

r5 = Solution().postorder_and_inorder_reconstruction([3, 2, 1], [1, 2, 3])
print(to_level_order(r5))  # [1, None, 2, None, 3]

r6 = Solution().postorder_and_inorder_reconstruction([3, 2, 1], [3, 2, 1])
print(to_level_order(r6))  # [1, 2, None, 3]

r7 = Solution().postorder_and_inorder_reconstruction([1, 3, 2, 5, 7, 6, 4], [1, 2, 3, 4, 5, 6, 7])
print(to_level_order(r7))  # [4, 2, 6, 1, 3, 5, 7]
```

```java run
import java.util.*;

public class Main {
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
    }

    static List<Integer> toLevelOrder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node != null) {
                result.add(node.val);
                queue.add(node.left);
                queue.add(node.right);
            } else {
                result.add(null);
            }
        }
        while (!result.isEmpty() && result.get(result.size() - 1) == null) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    static class Solution {

        // Global variable to keep track of the index in the postorder
        // traversal
        private int postInd;

        // Helper function to find the index of a given value in the inorder
        // traversal
        private int findIndex(int[] inorder, int start, int end, int val) {
            for (int i = start; i <= end; i++) {
                if (inorder[i] == val) {
                    return i;
                }
            }

            // If the value is not found in the inorder array, return the
            // start index
            return start;
        }

        private TreeNode buildTree(
            int[] inorder,
            int inStart,
            int inEnd,
            int[] postorder
        ) {

            // Base case: If the current inorder range is empty, return null
            if (inStart > inEnd) {
                return null;
            }

            // Create a new node with the current postorder element
            TreeNode currentNode = new TreeNode(postorder[postInd]);

            // Find the index of this element in inorder
            int index = findIndex(
                inorder,
                inStart,
                inEnd,
                postorder[postInd]
            );

            // Move to the next postorder element
            postInd--;

            // Recursively build the right subtree with elements after the
            // current index in inorder
            currentNode.right = buildTree(
                inorder,
                index + 1,
                inEnd,
                postorder
            );

            // Recursively build the left subtree with elements before the
            // current index in inorder
            currentNode.left = buildTree(
                inorder,
                inStart,
                index - 1,
                postorder
            );

            // Return the current node with its left and right subtrees
            // constructed
            return currentNode;
        }

        public TreeNode postorderAndInorderReconstruction(
            int[] postorder,
            int[] inorder
        ) {

            // Initialize the postInd to the last index of the postorder
            // traversal.
            postInd = postorder.length - 1;

            // Call the helper function with the full range of inorder
            // traversal.
            return buildTree(inorder, 0, inorder.length - 1, postorder);
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{4, 2, 9, 7, 3, 1}, new int[]{4, 2, 1, 3, 9, 7})));  // [1, 2, 3, 4, null, null, 7, null, null, 9]

        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{6, 8, 4, 1}, new int[]{8, 6, 1, 4})));               // [1, 8, 4, null, 6]

        // Edge cases
        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{}, new int[]{})));                                     // []

        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{1}, new int[]{1})));                                   // [1]

        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{3, 2, 1}, new int[]{1, 2, 3})));                      // [1, null, 2, null, 3]

        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{3, 2, 1}, new int[]{3, 2, 1})));                      // [1, 2, null, 3]

        System.out.println(toLevelOrder(new Solution().postorderAndInorderReconstruction(
            new int[]{1, 3, 2, 5, 7, 6, 4}, new int[]{1, 2, 3, 4, 5, 6, 7})));  // [4, 2, 6, 1, 3, 5, 7]
    }
}
```

### Complexity

Identical to pre+in: **O(N²) time** in the worst case with the linear-scan lookup (O(N) if you swap in an inorder hash map); **O(N)** space for the tree plus **O(h)** for the recursion.

</details>

***

# What about preorder + postorder?

A natural question: if pre+in works and post+in works, what about **pre+post** without inorder?

The answer is *almost* — but with a catch. Pre+post **uniquely determines the tree only when every internal node has exactly two children** (a *full binary tree*). For trees that have any node with only one child, pre+post is ambiguous.

Why? Consider these two trees:

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
    subgraph TA["A: 2 is left child of 1"]
        TA1((1))
        TA2((2))
        TA1 --> TA2
        TA1 -.- TAN[" "]
        style TAN fill:none,stroke:none
    end
    subgraph TB["B: 2 is right child of 1"]
        TB1((1))
        TB2((2))
        TB1 -.- TBN[" "]
        TB1 --> TB2
        style TBN fill:none,stroke:none
    end
```

Both trees have **preorder `[1, 2]`** and **postorder `[2, 1]`** — so the pair cannot distinguish them. The reason inorder works (and the others don't) is that *only* inorder reveals the *left/right split* around the root; pre+post both visit the root at known positions but neither tells you, for a single-child node, *which side* the child is on.

So in practice: prefer pre+in or post+in, and fall back to pre+post only if you know for certain the tree is full.

***

## Final Takeaway

Tree construction from traversals is a small jewel of recursive thinking. Three things to walk away with:

1. **One traversal is never enough.** Each individual traversal throws away too much information about the tree's *shape*. Preorder fixes the visit order but not the parent-child relationships; inorder hides the root entirely; postorder mirrors preorder's problem from the other end. Don't try to invert a single traversal.
2. **Pre+in and post+in are duals.** Both algorithms have the same shape — divide-and-conquer over the inorder slice, indexed by a moving pointer into the other array. Pre+in marches forward through preorder and recurses left-then-right; post+in marches backward through postorder and recurses right-then-left. Recognise the duality and you'll never need to look up either algorithm.
3. **Watch the inorder lookup cost.** The implementations above use a `find_index` linear scan to locate the root in the inorder slice — clear to read, but it makes every recursive call O(N), degrading the algorithm to O(N²) on skew trees. Pre-building a **value → inorder index** hash map turns each lookup into O(1) and brings the whole construction down to O(N). The map is a one-line change with massive payoff — reach for it in production code.

> *Coming up — the lessons that follow build on construction with <strong>insertion</strong> (adding a new node to an existing tree at a given position) and then dive into the <strong>11 binary-tree patterns</strong> that cover almost every interview question you'll see on this data structure: stateless and stateful preorder/postorder, root-to-leaf paths, level-order traversal, lowest common ancestor, simultaneous traversal of two trees, and a final practice mix. Each pattern is a recipe — once you've internalised the recursive shape from these first six lessons, the patterns are just <em>"what work do I do at the visit step?"</em> applied to specific problems.*

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Edge Cases & Pitfalls — missing, needs to be written -->
<!--       Guidance: bulleted list of gotchas -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->
