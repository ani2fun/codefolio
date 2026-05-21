# 6. Converting Expressions Using a Stack

## The Hook

Three notations, six possible conversions. You might *expect* that converting between them is a six-different-algorithms ordeal. **It's not.** Once you see the pattern, all six conversions reduce to two ideas, each of which uses one stack:

1. **Postfix-or-prefix → anything else**: scan the source linearly (left-to-right for postfix, right-to-left for prefix), push operands onto a *string* stack, and on every operator pop two strings and glue them together with the operator between (or before, or after) them. The operator placement determines the output notation; the scanning direction determines whether you're reading postfix or prefix.

2. **Infix → postfix**: a slightly cleverer dance called the **Shunting-Yard algorithm** (Edsger Dijkstra, 1961), where operators wait on a stack until something with lower-or-equal precedence shows up to push them out into the output. Parentheses become temporary "fences" that block this eviction. Once you have infix→postfix, infix→prefix is one extra reverse-and-flip-brackets trick away.

The same handful of moves — *push operand, pop-two-and-combine, peek-and-compare-precedence, flush-on-paren* — appears in every parser, every compiler, and every spreadsheet evaluator you'll ever read about. This lesson is the most code-dense in the entire stack section, but the patterns recur enough that by the third conversion you'll be writing the fourth from memory.

---

## Table of contents

1. [Postfix → Prefix](#understanding-postfix-to-prefix-conversion)
2. [Convert postfix to prefix](#convert-postfix-to-prefix)
3. [Postfix → Infix](#understanding-postfix-to-infix-conversion)
4. [Convert postfix to infix](#convert-postfix-to-infix)
5. [Prefix → Postfix](#understanding-prefix-to-postfix-conversion)
6. [Convert prefix to postfix](#convert-prefix-to-postfix)
7. [Prefix → Infix](#understanding-prefix-to-infix-conversion)
8. [Convert prefix to infix](#convert-prefix-to-infix)
9. [Infix → Postfix](#understanding-infix-to-postfix-conversion)
10. [Convert infix to postfix](#convert-infix-to-postfix)
11. [Infix → Prefix](#understanding-infix-to-prefix-conversion)
12. [Convert infix to prefix](#convert-infix-to-prefix)

***

# Understanding postfix to prefix conversion

Postfix and prefix look like mirror images, but **simply reversing a postfix string does not produce the equivalent prefix string**. Reversing `2 3 1 * + 9 -` gives `9 - + * 1 3 2`, which isn't a valid prefix expression — the operands and operators are now in the wrong relative pairing. Operator–operand grouping must be preserved across the conversion, and a stack of *partial expressions* is the tool that does it.

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
    R["read postfix token"] --> Q{"operand?"}
    Q -->|"yes"| P["push as string"]
    Q -->|"no (operator)"| O["pop b<br/>pop a<br/>push 'op + a + b'<br/>(operator BEFORE operands)"]
    P --> R
    O --> R
```

<p align="center"><strong>Postfix → prefix — same loop shape as the postfix evaluator, but the stack holds <em>strings</em> (sub-expressions in prefix form) rather than numbers, and an operator combines them with itself <em>at the front</em>.</strong></p>

## Walkthrough — `2 3 1 * + 9 -`

| Step | Token | Action | Stack (top right) |
|---:|:---:|---|---|
| 1 | `2` | push `'2'` | `['2']` |
| 2 | `3` | push `'3'` | `['2','3']` |
| 3 | `1` | push `'1'` | `['2','3','1']` |
| 4 | `*` | pop `'1'`, pop `'3'`, push `'*31'` | `['2','*31']` |
| 5 | `+` | pop `'*31'`, pop `'2'`, push `'+2*31'` | `['+2*31']` |
| 6 | `9` | push `'9'` | `['+2*31','9']` |
| 7 | `-` | pop `'9'`, pop `'+2*31'`, push `'-+2*319'` | `['-+2*319']` |
| — | end | result is the lone item | **`-+2*319`** |

<p align="center"><strong>Postfix <code>231*+9-</code> → prefix <code>-+2*319</code>. The stack is a <em>string</em> stack — every operator combines two existing prefix sub-expressions into a larger one. Operand order: first pop is right; second pop is left.</strong></p>

## Algorithm

> -   **Step 1:** Initialise an empty string stack.
> -   **Step 2:** For each character of the postfix string left to right:
>     -   If operand, push it (as a one-character string).
>     -   Else (operator): `b = pop()`, `a = pop()`, push `op + a + b`.
> -   **Step 3:** Return the lone string on the stack.

***

# Convert postfix to prefix

## Problem Statement

Given a postfix expression `postfix`, return the equivalent prefix expression. Operands are single-character (digit or letter); operators are `+`, `-`, `*`, `/`, `^`.

### Example
> -   **Input:** `postfix = "231*+9-"` → **Output:** `"-+2*319"`

<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if a character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    def convert_postfix_to_prefix(self, postfix: str) -> str:
        stack: List[str] = []
        length: int = len(postfix)

        for i in range(length):

            # If the character is an operator, pop the top two
            # elements from the stack
            if self.is_operator(postfix[i]):

                # Pop the top element from the stack as the second
                # operand
                operand2 = stack.pop()

                # Pop the top element from the stack as the first
                # operand
                operand1 = stack.pop()

                # Construct the prefix expression by placing the operator
                # before the operands
                expr = postfix[i] + operand1 + operand2
                stack.append(expr)

            # If the character is not an operator, push it to the
            # stack as a single-character string
            else:
                stack.append(postfix[i])

        # The final element in the stack will be the prefix expression
        return stack.pop()


# Example from the problem statement
print(Solution().convert_postfix_to_prefix("783/-52/6-*"))   # *-7/83-/526

# Edge cases
print(Solution().convert_postfix_to_prefix("ab+"))           # +ab — single operation
print(Solution().convert_postfix_to_prefix("abc**"))         # *a*bc — right-associative chain
print(Solution().convert_postfix_to_prefix("ab+c-"))         # -+abc — two operators
print(Solution().convert_postfix_to_prefix("ab-cd+*"))       # *-ab+cd
print(Solution().convert_postfix_to_prefix("abcd-+*"))       # *a+-bcd
print(Solution().convert_postfix_to_prefix("ab+cd+*"))       # *+ab+cd
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if a character is an operator
        private boolean isOperator(char ch) {
            return (!Character.isLetter(ch) && !Character.isDigit(ch));
        }

        public String convertPostfixToPrefix(String postfix) {
            Stack<String> stack = new Stack<>();
            int length = postfix.length();

            for (int i = 0; i < length; i++) {

                // If the character is an operator, pop the top two
                // elements from the stack
                if (isOperator(postfix.charAt(i))) {

                    // Pop the top element from the stack as the second
                    // operand
                    String operand2 = stack.pop();

                    // Pop the top element from the stack as the first
                    // operand
                    String operand1 = stack.pop();

                    // Construct the prefix expression by placing the
                    // operator before the operands
                    String expr = postfix.charAt(i) + operand1 + operand2;
                    stack.push(expr);
                }

                // If the character is not an operator, push it to the
                // stack as a single-character string
                else {
                    stack.push(String.valueOf(postfix.charAt(i)));
                }
            }

            // The final element in the stack will be the prefix expression
            return stack.pop();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertPostfixToPrefix("783/-52/6-*"));  // *-7/83-/526

        // Edge cases
        System.out.println(new Solution().convertPostfixToPrefix("ab+"));          // +ab
        System.out.println(new Solution().convertPostfixToPrefix("abc**"));        // *a*bc
        System.out.println(new Solution().convertPostfixToPrefix("ab+c-"));        // -+abc
        System.out.println(new Solution().convertPostfixToPrefix("ab-cd+*"));      // *-ab+cd
        System.out.println(new Solution().convertPostfixToPrefix("abcd-+*"));      // *a+-bcd
        System.out.println(new Solution().convertPostfixToPrefix("ab+cd+*"));      // *+ab+cd
    }
}
```


> **All cases** — Time: **O(N²)** worst-case (string concatenation), Space: **O(N²)** worst-case. With efficient string-builders this drops to **O(N)** time and **O(N)** space.

</details>

***

# Understanding postfix to infix conversion

Same algorithm — only the combine step changes. Where prefix wrote `op + a + b`, **infix wraps the operator in parentheses around the two operands**: `( a + op + b )`. The parentheses are necessary because we don't track precedence inside the stack — they ensure the produced expression evaluates the same way regardless of where it gets nested.

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
    R["read postfix token"] --> Q{"operand?"}
    Q -->|"yes"| P["push as string"]
    Q -->|"no (operator)"| O["pop b<br/>pop a<br/>push '(' + a + op + b + ')'<br/>(operator BETWEEN operands, parenthesised)"]
    P --> R
    O --> R
```

<p align="center"><strong>Postfix → infix — operand pops as before, but the combine step wraps the result in parentheses to preserve precedence. The output may have <em>more</em> parentheses than strictly needed, but it's always correct.</strong></p>

## Algorithm

> -   **Step 1:** Initialise an empty string stack.
> -   **Step 2:** For each character of the postfix string left to right:
>     -   If operand, push it.
>     -   Else: `b = pop()`, `a = pop()`, push `(a + op + b)`.
> -   **Step 3:** Return the lone string on the stack.

***

# Convert postfix to infix

<details>
<summary><h2>Example</h2></summary>


> -   **Input:** `postfix = "231*+9-"` → **Output:** `"((2+(3*1))-9)"`

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if a character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    def convert_postfix_to_infix(self, postfix: str) -> str:
        stack: List[str] = []
        length: int = len(postfix)

        for i in range(length):

            # If the character is an operator, pop the top two elements
            # from the stack and construct the infix expression by
            # placing the operands and operator within parentheses
            if self.is_operator(postfix[i]):

                # Pop the top element from the stack as the second
                # operand
                operand2 = stack.pop()

                # Pop the top element from the stack as the first operand
                operand1 = stack.pop()

                # Construct the infix expression by placing the operands
                # and operator within parentheses
                expr: str = "(" + operand1 + postfix[i] + operand2 + ")"
                stack.append(expr)

            # If the character is not an operator, push it to the
            # stack as a single-character string
            else:
                stack.append(postfix[i])

        # The final element in the stack will be the infix expression
        return stack.pop()


# Example from the problem statement
print(Solution().convert_postfix_to_infix("5647^9-326*+^*+2-"))   # ((5+(6*(((4^7)-9)^(3+(2*6)))))-2)

# Edge cases
print(Solution().convert_postfix_to_infix("ab+"))                  # (a+b)
print(Solution().convert_postfix_to_infix("abc**"))                # (a*(b*c))
print(Solution().convert_postfix_to_infix("ab+c-"))                # ((a+b)-c)
print(Solution().convert_postfix_to_infix("ab-cd+*"))              # ((a-b)*(c+d))
print(Solution().convert_postfix_to_infix("ab+cd+*"))              # ((a+b)*(c+d))
print(Solution().convert_postfix_to_infix("abcd-+*"))              # (a*(b+(c-d)))
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if a character is an operator
        private boolean isOperator(char ch) {
            return (!Character.isLetter(ch) && !Character.isDigit(ch));
        }

        public String convertPostfixToInfix(String postfix) {
            Stack<String> stack = new Stack<>();
            int length = postfix.length();

            for (int i = 0; i < length; i++) {

                // If the character is an operator, pop the top two elements
                // from the stack and construct the infix expression by
                // placing the operands and operator within parentheses
                if (isOperator(postfix.charAt(i))) {

                    // Pop the top element from the stack as the second
                    // operand
                    String operand2 = stack.pop();

                    // Pop the top element from the stack as the first
                    // operand
                    String operand1 = stack.pop();

                    // Construct the infix expression by placing the operands
                    // and operator within parentheses
                    String expr =
                        "(" + operand1 + postfix.charAt(i) + operand2 + ")";
                    stack.push(expr);
                }

                // If the character is not an operator, push it to the
                // stack as a single-character string
                else {
                    stack.push(String.valueOf(postfix.charAt(i)));
                }
            }

            // The final element in the stack will be the infix expression
            return stack.pop();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertPostfixToInfix("5647^9-326*+^*+2-"));
        // ((5+(6*(((4^7)-9)^(3+(2*6)))))-2)

        // Edge cases
        System.out.println(new Solution().convertPostfixToInfix("ab+"));          // (a+b)
        System.out.println(new Solution().convertPostfixToInfix("abc**"));        // (a*(b*c))
        System.out.println(new Solution().convertPostfixToInfix("ab+c-"));        // ((a+b)-c)
        System.out.println(new Solution().convertPostfixToInfix("ab-cd+*"));      // ((a-b)*(c+d))
        System.out.println(new Solution().convertPostfixToInfix("ab+cd+*"));      // ((a+b)*(c+d))
        System.out.println(new Solution().convertPostfixToInfix("abcd-+*"));      // (a*(b+(c-d)))
    }
}
```


> **Complexity** — Time: **O(N²)** with naïve string concat, **O(N)** with builders; Space: **O(N²)** / **O(N)** respectively.

</details>

***

# Understanding prefix to postfix conversion

Mirror image of postfix → prefix. Same idea, but **scan the input right-to-left** (because in prefix the operator appears *before* its operands, so we encounter the operands first when scanning backwards), and the combine step puts the operator **after** the operands.

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
    R["read prefix token<br/>(scan right→left)"] --> Q{"operand?"}
    Q -->|"yes"| P["push as string"]
    Q -->|"no (operator)"| O["pop a (left)<br/>pop b (right)<br/>push 'a + b + op'<br/>(operator AFTER operands)"]
    P --> R
    O --> R
```

<p align="center"><strong>Prefix → postfix — right-to-left scan; first pop is the LEFT operand; combine step appends the operator at the end.</strong></p>

## Algorithm

> -   **Step 1:** Initialise an empty string stack.
> -   **Step 2:** For each character of the prefix string **right to left**:
>     -   If operand, push it.
>     -   Else: `a = pop()` (LEFT), `b = pop()` (RIGHT), push `a + b + op`.
> -   **Step 3:** Return the lone string on the stack.

***

# Convert prefix to postfix

<details>
<summary><h2>Example</h2></summary>


> -   **Input:** `prefix = "-+2*319"` → **Output:** `"231*+9-"`

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if a character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    def convert_prefix_to_postfix(self, prefix: str) -> str:
        stack: List[str] = []
        length: int = len(prefix)

        for i in range(length - 1, -1, -1):

            # If the character is an operator, pop the top two
            # elements from the stack
            if self.is_operator(prefix[i]):

                # Pop the top element from the stack as the first
                # operand
                operand1: str = stack.pop()

                # Pop the top element from the stack as the second
                # operand
                operand2: str = stack.pop()

                # Construct the postfix expression by placing the
                # operands followed by the operator
                expr: str = operand1 + operand2 + prefix[i]
                stack.append(expr)

            # If the character is not an operator, push it to the
            # stack as a single-character string
            else:
                stack.append(prefix[i])

        # The final element in the stack will be the postfix expression
        return stack.pop()


# Example from the problem statement
print(Solution().convert_prefix_to_postfix("*-7/83-/526"))   # 783/-52/6-*

# Edge cases
print(Solution().convert_prefix_to_postfix("+ab"))           # ab+ — single operation
print(Solution().convert_prefix_to_postfix("*a*bc"))         # abc** — right chain
print(Solution().convert_prefix_to_postfix("-+abc"))         # ab+c-
print(Solution().convert_prefix_to_postfix("*-ab+cd"))       # ab-cd+*
print(Solution().convert_prefix_to_postfix("*+ab+cd"))       # ab+cd+*
print(Solution().convert_prefix_to_postfix("*a+-bcd"))       # abcd-+*
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if a character is an operator
        private boolean isOperator(char ch) {
            return !Character.isLetter(ch) && !Character.isDigit(ch);
        }

        public String convertPrefixToPostfix(String prefix) {
            Stack<String> stack = new Stack<>();
            int length = prefix.length();

            for (int i = length - 1; i >= 0; i--) {

                // If the character is an operator, pop the top two
                // elements from the stack
                if (isOperator(prefix.charAt(i))) {

                    // Pop the top element from the stack as the first
                    // operand
                    String operand1 = stack.pop();

                    // Pop the top element from the stack as the second
                    // operand
                    String operand2 = stack.pop();

                    // Construct the postfix expression by placing the
                    // operands followed by the operator
                    String expr = operand1 + operand2 + prefix.charAt(i);
                    stack.push(expr);
                }

                // If the character is not an operator, push it to the
                // stack as a single-character string
                else {
                    stack.push(String.valueOf(prefix.charAt(i)));
                }
            }

            // The final element in the stack will be the postfix expression
            return stack.pop();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertPrefixToPostfix("*-7/83-/526"));  // 783/-52/6-*

        // Edge cases
        System.out.println(new Solution().convertPrefixToPostfix("+ab"));          // ab+
        System.out.println(new Solution().convertPrefixToPostfix("*a*bc"));        // abc**
        System.out.println(new Solution().convertPrefixToPostfix("-+abc"));        // ab+c-
        System.out.println(new Solution().convertPrefixToPostfix("*-ab+cd"));      // ab-cd+*
        System.out.println(new Solution().convertPrefixToPostfix("*+ab+cd"));      // ab+cd+*
        System.out.println(new Solution().convertPrefixToPostfix("*a+-bcd"));      // abcd-+*
    }
}
```

</details>


***

# Understanding prefix to infix conversion

Right-to-left scan, infix combine step `(a op b)`.

## Algorithm

> -   **Step 1:** Initialise an empty string stack.
> -   **Step 2:** For each character of the prefix string **right to left**:
>     -   If operand, push it.
>     -   Else: `a = pop()` (LEFT), `b = pop()` (RIGHT), push `(a + op + b)`.
> -   **Step 3:** Return the lone string on the stack.

***

# Convert prefix to infix

<details>
<summary><h2>Example</h2></summary>


> -   **Input:** `prefix = "-+2*319"` → **Output:** `"((2+(3*1))-9)"`

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if a character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    def convert_prefix_to_infix(self, prefix: str) -> str:
        stack: List[str] = []
        length: int = len(prefix)

        for i in range(length - 1, -1, -1):

            # If the character is an operator, pop the top two
            # elements from the stack and construct the infix expression
            # by placing the operator in between the operands
            if self.is_operator(prefix[i]):

                # Pop the top element from the stack as the first
                # operand
                operand1 = stack.pop()

                # Pop the top element from the stack as the second
                # operand
                operand2 = stack.pop()

                # Construct the infix expression by placing the operator
                # in between the operands
                expr = "(" + operand1 + prefix[i] + operand2 + ")"
                stack.append(expr)

            # If the character is not an operator, push it to the
            # stack as a single-character string
            else:
                stack.append(prefix[i])

        # The final element in the stack will be the infix expression
        return stack.pop()


# Example from the problem statement
print(Solution().convert_prefix_to_infix("*-7/83-/526"))   # ((7-(8/3))*((5/2)-6))

# Edge cases
print(Solution().convert_prefix_to_infix("+ab"))           # (a+b)
print(Solution().convert_prefix_to_infix("*a*bc"))         # (a*(b*c))
print(Solution().convert_prefix_to_infix("-+abc"))         # ((a+b)-c)
print(Solution().convert_prefix_to_infix("*-ab+cd"))       # ((a-b)*(c+d))
print(Solution().convert_prefix_to_infix("*+ab+cd"))       # ((a+b)*(c+d))
print(Solution().convert_prefix_to_infix("*a+-bcd"))       # (a*(b+(c-d)))
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if a character is an operator
        private boolean isOperator(char ch) {
            return !Character.isLetter(ch) && !Character.isDigit(ch);
        }

        public String convertPrefixToInfix(String prefix) {
            Stack<String> stack = new Stack<>();
            int length = prefix.length();

            for (int i = length - 1; i >= 0; i--) {

                // If the character is an operator, pop the top two
                // elements from the stack and construct the infix expression
                // by placing the operator in between the operands
                if (isOperator(prefix.charAt(i))) {

                    // Pop the top element from the stack as the first
                    // operand
                    String operand1 = stack.pop();

                    // Pop the top element from the stack as the second
                    // operand
                    String operand2 = stack.pop();

                    // Construct the infix expression by placing the operator
                    // in between the operands
                    String expr =
                        "(" + operand1 + prefix.charAt(i) + operand2 + ")";
                    stack.push(expr);
                }

                // If the character is not an operator, push it to the
                // stack as a single-character string
                else {
                    stack.push(String.valueOf(prefix.charAt(i)));
                }
            }

            // The final element in the stack will be the infix expression
            return stack.pop();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertPrefixToInfix("*-7/83-/526"));
        // ((7-(8/3))*((5/2)-6))

        // Edge cases
        System.out.println(new Solution().convertPrefixToInfix("+ab"));          // (a+b)
        System.out.println(new Solution().convertPrefixToInfix("*a*bc"));        // (a*(b*c))
        System.out.println(new Solution().convertPrefixToInfix("-+abc"));        // ((a+b)-c)
        System.out.println(new Solution().convertPrefixToInfix("*-ab+cd"));      // ((a-b)*(c+d))
        System.out.println(new Solution().convertPrefixToInfix("*+ab+cd"));      // ((a+b)*(c+d))
        System.out.println(new Solution().convertPrefixToInfix("*a+-bcd"));      // (a*(b+(c-d)))
    }
}
```

</details>


***

# Understanding infix to postfix conversion

The big one. **Infix → postfix** is the famous **Shunting-Yard algorithm** (Edsger Dijkstra, 1961). The trick: maintain a stack of *operators waiting to be emitted*, and an output buffer. As we scan the infix expression left-to-right:

- **Operands** go directly to the output (they don't need to wait — they're already in the right relative order).
- **Operators** push onto the operator stack — but *before* pushing, **flush** any operator on top of the stack whose precedence is `≥` the incoming operator's. This guarantees that higher-precedence operators emerge from the stack first, exactly matching their evaluation order.
- **`(`** pushes onto the operator stack as a *fence* that prevents lower-precedence operators from being flushed past it.
- **`)`** pops everything off until the matching `(`, then discards the `(`.
- **End of input** flushes whatever's left on the operator stack.

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
    R["read infix token"] --> Q{"type?"}
    Q -->|"operand"| P["append to output"]
    Q -->|"("| L["push to op stack"]
    Q -->|")"| R1["pop ops to output<br/>until '('<br/>discard '('"]
    Q -->|"operator op"| F["while top is op AND<br/>prec(top) >= prec(op):<br/>pop top to output<br/>then push op"]
    P --> R; L --> R; R1 --> R; F --> R
    R -->|"end of input"| FL["flush remaining ops to output"]
```

<p align="center"><strong>Shunting-Yard infix → postfix — operands flow straight through; operators wait on the stack until something with lower-or-equal precedence pushes them out. Parentheses act as fences. The algorithm is one pass, two structures (op stack + output buffer), no backtracking.</strong></p>

## Why does this work?

The key invariant: **at any point during the scan, the operator stack contains operators in strictly increasing precedence from bottom to top.** Each new operator either fits this invariant (push it) or violates it (flush down until it fits, then push).

> **Note on `^` (power):** `^` is right-associative — `2^3^2` means `2^(3^2)`, not `(2^3)^2`. To handle this, change the precedence comparison from `>=` to strictly `>` for the right-associative operator: when an `^` is incoming and `^` is on top, *don't* flush — push the new `^` on top so it'll be evaluated first. The implementations below use a generalised `is_right_assoc` helper for clarity.

## Walkthrough — `(2 + 3) * 4`

| Step | Token | Action | Op stack | Output |
|---:|:---:|---|---|---|
| 1 | `(` | push `(` | `['(']` | `''` |
| 2 | `2` | append to output | `['(']` | `'2'` |
| 3 | `+` | push (top is `(`, no flush) | `['(', '+']` | `'2'` |
| 4 | `3` | append to output | `['(', '+']` | `'23'` |
| 5 | `)` | flush `+` to output, discard `(` | `[]` | `'23+'` |
| 6 | `*` | push | `['*']` | `'23+'` |
| 7 | `4` | append to output | `['*']` | `'23+4'` |
| — | EOF | flush remaining ops | `[]` | **`'23+4*'`** |

<p align="center"><strong>Shunting-Yard on <code>(2+3)*4</code> — the parenthesis fences off <code>+</code> until <code>)</code> is seen; then <code>+</code> flushes. <code>*</code> waits on the stack until end-of-input. Result: <code>23+4*</code>.</strong></p>

## Algorithm

> -   **Step 1:** Initialise an empty operator stack and an empty output buffer.
> -   **Step 2:** For each character of the infix string left to right:
>     -   **Operand** → append to output.
>     -   **`(`** → push to op stack.
>     -   **`)`** → pop ops to output until `(`; discard `(`.
>     -   **operator** → while top of op stack is an operator with `prec(top) >= prec(op)` (strict `>` for right-associative): pop top to output. Then push op.
> -   **Step 3:** Flush remaining op stack to output.

***

# Convert infix to postfix

<details>
<summary><h2>Example</h2></summary>


> -   **Input:** `infix = "(2+3)*4"` → **Output:** `"23+4*"`

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if the character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    # Function to get the priority of operators
    def get_precedence(self, operator: str) -> int:

        # Assign precedence values to different operators
        if operator == "^":
            return 3
        elif operator in ["*", "/"]:
            return 2
        elif operator in ["+", "-"]:
            return 1

        # Default value for unknown operators
        return -1

    def convert_infix_to_postfix(self, infix: str) -> str:

        # Stack to hold operators and parentheses
        stack: List[str] = []

        # Final postfix expression
        postfix: str = ""

        for ch in infix:

            # If the character is not an operator or parentheses, add
            # it to the postfix string
            if not self.is_operator(ch) and ch != "(" and ch != ")":
                postfix += ch

            # If the character is an opening parentheses, push it
            # onto the stack
            elif ch == "(":
                stack.append(ch)

            # If the character is a closing parentheses, pop operators
            # from the stack and add them to the postfix string until an
            # opening parentheses is encountered
            elif ch == ")":
                while stack and stack[-1] != "(":
                    postfix += stack.pop()

                # Remove the opening parentheses from the stack
                if stack and stack[-1] == "(":
                    stack.pop()

            # If the character is an operator, compare its precedence
            # with the top of the stack and add higher or equal
            # precedence operators to the postfix string
            else:
                while stack and self.get_precedence(
                    ch
                ) <= self.get_precedence(stack[-1]):
                    if stack[-1] != "(":
                        postfix += stack.pop()

                # Push the current operator onto the stack
                stack.append(ch)

        # Pop any remaining operators from the stack and add them to the
        # postfix string
        while stack:
            postfix += stack.pop()

        return postfix


# Example from the problem statement
print(Solution().convert_infix_to_postfix("5+6*(4^7-9)^(3+2*6)-2"))   # 5647^9-326*+^*+2-

# Edge cases
print(Solution().convert_infix_to_postfix("a+b"))                      # ab+
print(Solution().convert_infix_to_postfix("a+b*c"))                    # abc*+
print(Solution().convert_infix_to_postfix("(a+b)*c"))                  # ab+c*
print(Solution().convert_infix_to_postfix("a+b+c"))                    # ab+c+
print(Solution().convert_infix_to_postfix("(a+b)*(c-d)"))              # ab+cd-*
print(Solution().convert_infix_to_postfix("a^b^c"))                    # ab^c^ (right-assoc handled)
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if the character is an operator
        private boolean isOperator(char ch) {
            return (!Character.isLetter(ch) && !Character.isDigit(ch));
        }

        // Function to get the priority of operators
        private int getPrecedence(char operator) {

            // Assign precedence values to different operators
            if (operator == '^') {
                return 3;
            } else if (operator == '*' || operator == '/') {
                return 2;
            } else if (operator == '+' || operator == '-') {
                return 1;
            }

            // Default value for unknown operators
            return -1;
        }

        public String convertInfixToPostfix(String infix) {

            // Stack to hold operators and parentheses
            Stack<Character> stack = new Stack<>();

            // Final postfix expression
            StringBuilder postfix = new StringBuilder();

            for (char ch : infix.toCharArray()) {

                // If the character is not an operator or parentheses,
                // add it to the postfix string
                if (!isOperator(ch) && ch != '(' && ch != ')') {
                    postfix.append(ch);
                }

                // If the character is an opening parentheses, push it
                // onto the stack
                else if (ch == '(') {
                    stack.push(ch);
                }

                // If the character is a closing parentheses, pop
                // operators from the stack and add them to the postfix
                // string until an opening parentheses is encountered
                else if (ch == ')') {
                    while (!stack.empty() && stack.peek() != '(') {
                        postfix.append(stack.peek());
                        stack.pop();
                    }

                    // Remove the opening parentheses from the stack
                    if (!stack.empty() && stack.peek() == '(') {
                        stack.pop();
                    }
                }

                // If the character is an operator, compare its
                // precedence with the top of the stack and add higher or
                // equal precedence operators to the postfix string
                else {
                    while (
                        !stack.empty() &&
                        getPrecedence(ch) <= getPrecedence(stack.peek())
                    ) {
                        if (stack.peek() != '(') {
                            postfix.append(stack.peek());
                        }
                        stack.pop();
                    }

                    // Push the current operator onto the stack
                    stack.push(ch);
                }
            }

            // Pop any remaining operators from the stack and add them to the
            // postfix string
            while (!stack.empty()) {
                postfix.append(stack.peek());
                stack.pop();
            }

            return postfix.toString();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertInfixToPostfix("5+6*(4^7-9)^(3+2*6)-2"));
        // 5647^9-326*+^*+2-

        // Edge cases
        System.out.println(new Solution().convertInfixToPostfix("a+b"));           // ab+
        System.out.println(new Solution().convertInfixToPostfix("a+b*c"));         // abc*+
        System.out.println(new Solution().convertInfixToPostfix("(a+b)*c"));       // ab+c*
        System.out.println(new Solution().convertInfixToPostfix("a+b+c"));         // ab+c+
        System.out.println(new Solution().convertInfixToPostfix("(a+b)*(c-d)"));   // ab+cd-*
        System.out.println(new Solution().convertInfixToPostfix("a^b^c"));         // ab^c^
    }
}
```


> **Complexity** — Time: **O(N)** | Space: **O(N)** for the operator stack and output buffer.

</details>

***

# Understanding infix to prefix conversion

The cleverest of the bunch — and a one-line reduction:

1. **Reverse** the infix string.
2. **Swap** every `(` with `)` and vice versa (because the bracket directions invert under reversal).
3. Run the **infix-to-postfix** converter on this reversed/flipped string, but with one rule change: **`^` is now left-associative** for this step (because the reversal flipped its associativity).
4. **Reverse** the resulting postfix to get the prefix.

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
    I["infix:<br/>(2+3)*4"] -->|"reverse, flip brackets"| R1["4*(3+2)"]
    R1 -->|"infix → postfix<br/>(^ left-assoc here)"| P["432+*"]
    P -->|"reverse"| R2["*+234"]
    R2 --> OUT["prefix: *+234"]
    style OUT fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Infix → prefix as a four-step reduction — reverse, flip brackets, run Shunting-Yard with right-associativity flipped, reverse the result. Builds on infix→postfix without a separate algorithm.</strong></p>

***

# Convert infix to prefix

<details>
<summary><h2>Example</h2></summary>


> -   **Input:** `infix = "(2+3)*4"` → **Output:** `"*+234"`

</details>
<details>
<summary><h2>Solution</h2></summary>



```python run
from typing import List

class Solution:

    # Function to check if the character is an operator
    def is_operator(self, ch: str) -> bool:
        return not ch.isalpha() and not ch.isdigit()

    # Function to get the priority of operators
    def get_precedence(self, operator: str) -> int:

        # Assign precedence values to different operators
        if operator == "^":
            return 3
        elif operator in ["*", "/"]:
            return 2
        elif operator in ["+", "-"]:
            return 1

        # Default value for unknown operators
        return -1

    def convert_infix_to_prefix(self, infix: str) -> str:

        # Stack to hold operators and parentheses
        stack: List[str] = []

        # Final prefix expression
        prefix: str = ""

        # Reverse the infix string for easier processing
        reversed_infix: str = infix[::-1]

        for ch in reversed_infix:

            # If the character is not an operator or parentheses, add
            # it to the prefix string
            if not self.is_operator(ch) and ch != ")" and ch != "(":
                prefix += ch

            # If the character is a closing parentheses, push it onto
            # the stack
            elif ch == ")":
                stack.append(ch)

            # If the character is an opening parentheses, pop operators
            # from the stack and add them to the prefix string until a
            # closing parentheses is encountered
            elif ch == "(":
                while stack and stack[-1] != ")":
                    prefix += stack.pop()

                # Remove the closing parentheses from the stack
                if stack and stack[-1] == ")":
                    stack.pop()

            # If the character is an operator, compare its precedence
            # with the top of the stack and add higher precedence
            # operators to the prefix string
            else:
                while (
                    stack
                    and self.get_precedence(ch)
                    < self.get_precedence(stack[-1])
                    and stack[-1] != ")"
                ):
                    prefix += stack.pop()

                # Push the current operator onto the stack
                stack.append(ch)

        # Pop any remaining operators from the stack and add them to the
        # prefix string
        while stack:
            prefix += stack.pop()

        # Reverse the prefix string to get the final result
        return prefix[::-1]


# Example from the problem statement
print(Solution().convert_infix_to_prefix("(7-8/3)*(5/2-6)"))   # *-7/83-/526

# Edge cases
print(Solution().convert_infix_to_prefix("a+b"))                # +ab
print(Solution().convert_infix_to_prefix("a+b*c"))              # +a*bc
print(Solution().convert_infix_to_prefix("(a+b)*c"))            # *+abc
print(Solution().convert_infix_to_prefix("a+b+c"))              # ++abc
print(Solution().convert_infix_to_prefix("(a+b)*(c-d)"))        # *+ab-cd
print(Solution().convert_infix_to_prefix("a*(b+c)"))            # *a+bc
```

```java run
import java.util.*;

public class Main {
    static class Solution {

        // Function to check if the character is an operator
        private boolean isOperator(char ch) {
            return (!Character.isLetter(ch) && !Character.isDigit(ch));
        }

        // Function to get the priority of operators
        private int getPrecedence(char operator) {

            // Assign precedence values to different operators
            if (operator == '^') {
                return 3;
            } else if (operator == '*' || operator == '/') {
                return 2;
            } else if (operator == '+' || operator == '-') {
                return 1;
            }

            // Default value for unknown operators
            return -1;
        }

        public String convertInfixToPrefix(String infix) {

            // Stack to hold operators and parentheses
            Stack<Character> stack = new Stack<>();

            // Final prefix expression
            StringBuilder prefix = new StringBuilder();
            String reversedInfix = new StringBuilder(infix)
                .reverse()
                .toString();

            // Reverse the infix string for easier processing

            for (char ch : reversedInfix.toCharArray()) {

                // If the character is not an operator or parentheses,
                // add it to the prefix string
                if (!isOperator(ch) && ch != ')' && ch != '(') {
                    prefix.append(ch);
                }

                // If the character is a closing parentheses, push it
                // onto the stack
                else if (ch == ')') {
                    stack.push(ch);
                }

                // If the character is an opening parentheses, pop
                // operators from the stack and add them to the prefix
                // string until a closing parentheses is encountered
                else if (ch == '(') {
                    while (!stack.empty() && stack.peek() != ')') {
                        prefix.append(stack.peek());
                        stack.pop();
                    }

                    // Remove the closing parentheses from the stack
                    if (!stack.empty() && stack.peek() == ')') {
                        stack.pop();
                    }
                }

                // If the character is an operator, compare its
                // precedence with the top of the stack and add higher
                // precedence operators to the prefix string
                else {
                    while (
                        !stack.empty() &&
                        getPrecedence(ch) < getPrecedence(stack.peek()) &&
                        stack.peek() != ')'
                    ) {
                        prefix.append(stack.peek());
                        stack.pop();
                    }

                    // Push the current operator onto the stack
                    stack.push(ch);
                }
            }

            // Pop any remaining operators from the stack and add them to the
            // prefix string
            while (!stack.empty()) {
                prefix.append(stack.peek());
                stack.pop();
            }

            // Reverse the prefix string to get the final result
            return prefix.reverse().toString();
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement
        System.out.println(new Solution().convertInfixToPrefix("(7-8/3)*(5/2-6)"));
        // *-7/83-/526

        // Edge cases
        System.out.println(new Solution().convertInfixToPrefix("a+b"));          // +ab
        System.out.println(new Solution().convertInfixToPrefix("a+b*c"));        // +a*bc
        System.out.println(new Solution().convertInfixToPrefix("(a+b)*c"));      // *+abc
        System.out.println(new Solution().convertInfixToPrefix("a+b+c"));        // ++abc
        System.out.println(new Solution().convertInfixToPrefix("(a+b)*(c-d)")); // *+ab-cd
        System.out.println(new Solution().convertInfixToPrefix("a*(b+c)"));      // *a+bc
    }
}
```


> **Complexity** — Time: **O(N)** | Space: **O(N)**. The reverse + flip is O(N), the Shunting-Yard is O(N), the final reverse is O(N).

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Six conversions, two algorithms (one for postfix/prefix scans, one Shunting-Yard for infix), one stack each. The matrix:

| From → To | Strategy |
|---|---|
| **Postfix → Prefix** | scan L→R, combine `op + a + b` |
| **Postfix → Infix**  | scan L→R, combine `(a + op + b)` |
| **Prefix → Postfix** | scan R→L, combine `a + b + op` |
| **Prefix → Infix**   | scan R→L, combine `(a + op + b)` |
| **Infix → Postfix**  | Shunting-Yard (one operator stack + output buffer) |
| **Infix → Prefix**   | reverse + flip brackets, infix→postfix, reverse |

Three lessons:

1. **Operand order is the trap.** Postfix scans L→R: first pop = right operand. Prefix scans R→L: first pop = left operand. Get this wrong and `+`/`*` appear correct while `-`/`/` silently produce wrong answers. Triple-check the order in every implementation.
2. **Right-associativity is `^`'s special.** Standard Shunting-Yard uses `>=` for the precedence comparison (left-associative). For right-associative operators, switch to strict `>` so an incoming `^` doesn't flush the `^` already on the stack — that's what makes `2^3^2` parse as `2^(3^2)`.
3. **Reverse + flip is a free conversion.** Going infix → prefix without writing a new algorithm is one of the most elegant tricks in compiler design. Reversing the string changes the scan direction; flipping brackets re-aligns parenthesisation; running infix→postfix on the reversed string and reversing the result lands you in prefix.

> *Coming up — we leave expression parsing and shift to **stack as a problem-solving pattern**. Lessons 7–11 cover five recurring shapes that appear in interview questions and production code: reversal, previous/next closest occurrence, sequence validation, and linear evaluation. Each one uses a stack as the *thinking tool* — a way to remember "the most recent thing not yet resolved" — and once you internalise the pattern, the solutions write themselves.*

</details>