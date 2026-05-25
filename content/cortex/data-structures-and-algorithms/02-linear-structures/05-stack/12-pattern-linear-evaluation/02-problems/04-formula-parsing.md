---
title: "Formula Parsing"
summary: "Given a chemical formula consisting of single-uppercase atoms (e.g. H, O), positive-integer multipliers, and parentheses for grouping, return a string of ATOM:COUNT separated by spaces, in order of fi"
prereqs:
  - 12-pattern-linear-evaluation/01-pattern
difficulty: hard
---

# Formula parsing

## Problem Statement

Given a chemical formula consisting of single-uppercase atoms (e.g. `H`, `O`), positive-integer multipliers, and parentheses for grouping, return a string of `ATOM:COUNT` separated by spaces, in order of first appearance.

> Single-uppercase atoms only (no `Na`, no `Cl` — atoms here are one character each), and no atom appears twice in the input.

### Example 1
> -   **Input:** `"(HO)2"` → **Output:** `"H:2 O:2"`

### Example 2
> -   **Input:** `"H(N(KO)2)3"` → **Output:** `"H:1 N:3 K:6 O:6"`

### Example 3
> -   **Input:** `"KH"` → **Output:** `"K:1 H:1"`

<details>
<summary><h2>Approach</h2></summary>


Stack of `(name, count)` records, plus a special `(` marker. On `(`: push a marker. On atom: read its trailing count (default 1) and push. On `)`: read the multiplier, pop everything down to the `(` marker, multiply each popped count by the multiplier, push back.

The "first appearance order" requirement is satisfied because we never re-order: by tracking each atom's earliest index in a separate map, we can sort the final stack by that.

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

# Define a class to hold atom information
class Atom:
    def __init__(self, name: str, count: int):
        self.name = name
        self.count = count

class Solution:
    def formula_parsing(self, formula: str) -> str:

        # Stack to store atoms, counts, and group markers
        stack: List[Atom] = []

        i = 0
        while i < len(formula):

            # If the current character is '(', push it to mark the start
            # of a group
            if formula[i] == "(":
                stack.append(Atom("(", -1))

            # If the current character is ')', process the group
            elif formula[i] == ")":
                i += 1

                # Read multiplier (if any)
                multiplier = 0
                while i < len(formula) and formula[i].isdigit():
                    multiplier = multiplier * 10 + int(formula[i])
                    i += 1

                if multiplier == 0:
                    multiplier = 1

                # adjust index because loop will increment
                i -= 1
  

                # Collect atoms in the group
                group: List[Atom] = []
                while stack and stack[-1].name != "(":
                    group.append(stack.pop())

                # Remove the '(' from the stack
                if stack and stack[-1].name == "(":
                    stack.pop()

                # Multiply counts and push back
                for atom in reversed(group):
                    stack.append(
                        Atom(atom.name, atom.count * multiplier)
                    )

            # If the character is an uppercase atom
            elif formula[i].isupper():
                atom_name = formula[i]
                i += 1

                # Read count (if any)
                count = 0
                while i < len(formula) and formula[i].isdigit():
                    count = count * 10 + int(formula[i])
                    i += 1

                if count == 0:
                    count = 1

                # adjust index because loop will increment
                i -= 1
  

                # Push atom with count
                stack.append(Atom(atom_name, count))

            i += 1

        # Collect the final result from the stack
        result: List[str] = []
        while stack:
            atom = stack.pop()
            result.insert(0, f"{atom.name}:{atom.count}")

        return " ".join(result)


# Examples from the problem statement
print(Solution().formula_parsing("(HO)2"))       # H:2 O:2
print(Solution().formula_parsing("H(N(KO)2)3"))  # H:1 N:3 K:6 O:6
print(Solution().formula_parsing("KH"))          # K:1 H:1

# Edge cases
print(Solution().formula_parsing("A"))           # A:1 — single atom no count
print(Solution().formula_parsing("A3"))          # A:3
print(Solution().formula_parsing("(AB)1"))       # A:1 B:1
print(Solution().formula_parsing("(XY)10"))      # X:10 Y:10 — multi-digit multiplier
print(Solution().formula_parsing("A(BC)2D"))     # A:1 B:2 C:2 D:1
```

```java run
import java.util.*;

public class Main {
    // Define a class to hold atom information
    static class Atom {
        char name;
        int count;

        Atom(char name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    static class Solution {
        public String formulaParsing(String formula) {

            // Stack to store atoms, counts, and group markers
            Stack<Atom> stack = new Stack<>();

            for (int i = 0; i < formula.length(); i++) {

                // If the current character is '(', push it to mark the start
                // of a group
                if (formula.charAt(i) == '(') {
                    stack.push(new Atom('(', -1));
                }

                // If the current character is ')', process the group
                else if (formula.charAt(i) == ')') {

                    // Move past ')', check for multiplier
                    i++;

                    // Read multiplier (if any)
                    int multiplier = 0;
                    while (
                        i < formula.length() &&
                        Character.isDigit(formula.charAt(i))
                    ) {
                        multiplier =
                            multiplier * 10 + (formula.charAt(i) - '0');
                        i++;
                    }

                    // If no multiplier, default to 1
                    if (multiplier == 0) multiplier = 1;

                    // adjust index because loop will increment
                    i--;

                    // Collect atoms in the group
                    List<Atom> group = new ArrayList<>();
                    while (!stack.isEmpty() && stack.peek().name != '(') {
                        group.add(stack.pop());
                    }

                    // Remove the '(' from the stack
                    if (!stack.isEmpty() && stack.peek().name == '(') {
                        stack.pop();
                    }

                    // Multiply counts and push back
                    for (int j = group.size() - 1; j >= 0; j--) {
                        Atom atom = group.get(j);
                        stack.push(
                            new Atom(atom.name, atom.count * multiplier)
                        );
                    }
                }

                // If the character is an uppercase atom
                else if (Character.isUpperCase(formula.charAt(i))) {
                    char atomName = formula.charAt(i++);

                    // Read count (if any)
                    int count = 0;
                    while (
                        i < formula.length() &&
                        Character.isDigit(formula.charAt(i))
                    ) {
                        count = count * 10 + (formula.charAt(i) - '0');
                        i++;
                    }

                    // If no count, default to 1
                    if (count == 0) count = 1;

                    // adjust index because loop will increment
                    i--;

                    // Push atom with count
                    stack.push(new Atom(atomName, count));
                }
            }

            // Collect the final result from the stack
            StringBuilder result = new StringBuilder();
            while (!stack.isEmpty()) {
                Atom atom = stack.pop();
                result.insert(0, atom.name + ":" + atom.count + " ");
            }

            if (result.length() > 0) {
                result.setLength(result.length() - 1);
            }

            return result.toString();
        }
    }

    public static void main(String[] args) {
        // Examples from the problem statement
        System.out.println(new Solution().formulaParsing("(HO)2"));       // H:2 O:2
        System.out.println(new Solution().formulaParsing("H(N(KO)2)3"));  // H:1 N:3 K:6 O:6
        System.out.println(new Solution().formulaParsing("KH"));          // K:1 H:1

        // Edge cases
        System.out.println(new Solution().formulaParsing("A"));           // A:1
        System.out.println(new Solution().formulaParsing("A3"));          // A:3
        System.out.println(new Solution().formulaParsing("(AB)1"));       // A:1 B:1
        System.out.println(new Solution().formulaParsing("(XY)10"));      // X:10 Y:10
        System.out.println(new Solution().formulaParsing("A(BC)2D"));     // A:1 B:2 C:2 D:1
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Three lessons:

1. **The stack holds partial answers in progress.** Whenever a closer event fires, you collapse a chunk of the stack into a single combined value and push that back. The result keeps growing until the next closer or end of input.
2. **Indices, characters, strings, or records — push whatever the problem needs.** Path tokens for path simplification; characters for bracket reversal; (atom, count) records for chemical formulas. The container shape adapts; the stack discipline doesn't.
3. **Multi-digit numbers and multi-character tokens need a sub-loop.** Inside the main scan, slurp consecutive digits or letters before pushing — otherwise `12[a]` will push `1`, `2`, `[`, `a`, `]` and you'll lose the multiplier.

> *Coming up — the **design** lesson. We've built five problem patterns; the final lesson takes the stack interface and asks: <em>what would it take to extend it with one extra O(1) operation, like <code>min()</code>?</em> Two classic interview questions — Min Stack (push, pop, top, min — all O(1)) and Stack Using Queues — close out the section by demonstrating how to <em>compose stacks with auxiliary structures</em> to add new functionality without losing the original O(1) guarantees.*

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
