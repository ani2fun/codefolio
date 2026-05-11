# 5. Evaluating Expressions Using a Stack

## The Hook

We just learned that postfix and prefix encode the order of operations *by position alone* — no parentheses, no precedence rules. That's a beautiful property, but it's only useful if we can actually *evaluate* such expressions efficiently. The good news: with a stack in our toolbox, the evaluator is one of the cleanest, most satisfying algorithms in the whole course. **Single pass over the string. One stack. No look-ahead. No backtracking. No special cases.** Push when you see an operand; pop two and push the result when you see an operator. The final number sitting alone on the stack is your answer.

That's it. Twelve lines of code. Linear time. Constant code complexity. The same pattern that runs inside every Reverse-Polish HP calculator, the inner loop of Forth interpreters, and the operand stack of the JVM.

This lesson builds three evaluators:

1. **Postfix evaluator** — the canonical one; left-to-right scan.
2. **Prefix evaluator** — same idea, scan right-to-left (or reverse the string and reuse the postfix logic with operand-order flipped).
3. **Infix evaluator** — the cheat: convert to postfix using the algorithm from the next lesson, then evaluate. Two stacks total, but each one is doing one thing well.

By the end you'll have a calculator core that handles `(2+3)*(4/2)` with the same code path as `23*4/+`. Same engine, three input dialects.

---

## Table of contents

1. [Understanding the evaluation of postfix expressions](#understanding-the-evaluation-of-postfix-expressions)
2. [Evaluate a postfix expression](#evaluate-a-postfix-expression)
3. [Understanding the evaluation of prefix expressions](#understanding-the-evaluation-of-prefix-expressions)
4. [Evaluate a prefix expression](#evaluate-a-prefix-expression)
5. [Evaluate an infix expression](#evaluate-an-infix-expression)

***

# Understanding the evaluation of postfix expressions

The recipe — three sentences:

1. Walk the string left to right.
2. If the token is an **operand**, push it onto the stack.
3. If the token is an **operator**, pop the top two values, apply the operator, push the result.

When the walk ends, the lone item on the stack is the answer.

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
    R["read token"] --> Q{"digit?"}
    Q -->|"yes"| P["push token"]
    Q -->|"no (operator)"| O["pop b<br/>pop a<br/>push (a OP b)"]
    P --> R
    O --> R
```

<p align="center"><strong>Postfix evaluator — every iteration is either a push (operand) or a pop-two-push-one (operator). At end-of-input, the stack holds exactly one element: the result.</strong></p>

> **Crucial: operand order matters.**
>
> When you see `a b -` and pop in order, the *first* value popped is `b` (it was pushed second, so it's on top), and the *second* value popped is `a` (pushed first, now exposed). The operation is `a − b`, not `b − a`. Convention: name them `operand2 = stack.pop()` (popped first), then `operand1 = stack.pop()` (popped second), and call `op(operand1, operand2)`. For commutative operators (`+`, `*`) the order doesn't matter; for `-`, `/`, `^` it does, and getting it backwards silently produces wrong answers.

## Walkthrough — `2 3 1 * + 9 -`

The input is the postfix form of `(2 + 3*1) - 9 = -4`. Walk it:

| Step | Token | Action | Stack (top right) |
|---:|:---:|---|---|
| 1 | `2` | push | `[2]` |
| 2 | `3` | push | `[2, 3]` |
| 3 | `1` | push | `[2, 3, 1]` |
| 4 | `*` | pop 1, pop 3, push `3*1=3` | `[2, 3]` |
| 5 | `+` | pop 3, pop 2, push `2+3=5` | `[5]` |
| 6 | `9` | push | `[5, 9]` |
| 7 | `-` | pop 9, pop 5, push `5-9=-4` | `[-4]` |
| — | end | result is the lone item | **`-4`** |

<p align="center"><strong>Walking <code>2 3 1 * + 9 -</code> step by step — every operator collapses two stack entries into one, so the stack never grows past O(operands). The final element is the answer.</strong></p>

## Algorithm

> **Algorithm**
>
> -   **Step 1:** Initialise an empty stack.
> -   **Step 2:** For each character `ch` in the postfix string, left to right:
>     -   **2.1** If `ch` is a digit, push its numeric value.
>     -   **2.2** Else (`ch` is an operator):
>         -   `op2 = stack.pop()` (popped first → right operand)
>         -   `op1 = stack.pop()` (popped second → left operand)
>         -   push `apply(op1, ch, op2)`
> -   **Step 3:** Return `stack.top()`.

## Implementation


```pseudocode
function perform(a, b, op):
    if op = '+': return a + b
    if op = '−': return a − b
    if op = '*': return a * b
    if op = '/': return a / b

function evaluatePostfix(postfix):
    stack ← empty stack
    for each ch in postfix:
        if ch is digit: push float(ch)
        else:
            b ← pop()   # right operand
            a ← pop()   # left operand
            push perform(a, b, ch)
    return top of stack
```

```python run
def perform(a: float, b: float, op: str) -> float:
    if op == '+': return a + b
    if op == '-': return a - b
    if op == '*': return a * b
    if op == '/': return a / b
    return 0.0

def evaluate_postfix(postfix: str) -> float:
    stack = []
    for ch in postfix:
        if ch.isdigit():
            stack.append(float(ch))
        else:
            b = stack.pop()                    # popped first → right operand
            a = stack.pop()                    # popped second → left operand
            stack.append(perform(a, b, ch))
    return stack[-1]

print(evaluate_postfix("231*+9-"))    # -4.0
print(evaluate_postfix("23*"))        # 6.0
print(evaluate_postfix("234*+"))      # 14.0  (== 2 + 3*4)
```

```java run
import java.util.*;

public class Main {
    static float perform(float a, float b, char op) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            default:  return 0;
        }
    }
    static float evaluatePostfix(String postfix) {
        Deque<Float> st = new ArrayDeque<>();
        for (char ch : postfix.toCharArray()) {
            if (Character.isDigit(ch)) st.push((float)(ch - '0'));
            else {
                float b = st.pop(), a = st.pop();
                st.push(perform(a, b, ch));
            }
        }
        return st.peek();
    }
    public static void main(String[] args) {
        System.out.println(evaluatePostfix("231*+9-"));   // -4.0
        System.out.println(evaluatePostfix("23*"));       // 6.0
        System.out.println(evaluatePostfix("234*+"));     // 14.0
    }
}
```

```c run
#include <stdio.h>
#include <ctype.h>
#include <string.h>

static float perform_op(float a, float b, char op) {
    switch (op) {
        case '+': return a + b;
        case '-': return a - b;
        case '*': return a * b;
        case '/': return a / b;
        default:  return 0;
    }
}

float evaluate_postfix(const char *postfix) {
    float stack[256]; int top = -1;
    for (const char *p = postfix; *p; p++) {
        if (isdigit((unsigned char)*p)) stack[++top] = (float)(*p - '0');
        else {
            float b = stack[top--];
            float a = stack[top--];
            stack[++top] = perform_op(a, b, *p);
        }
    }
    return stack[top];
}

int main() {
    printf("%.1f\n", evaluate_postfix("231*+9-"));   // -4.0
    printf("%.1f\n", evaluate_postfix("23*"));       // 6.0
    printf("%.1f\n", evaluate_postfix("234*+"));     // 14.0
}
```

```scala run
import scala.collection.mutable

def perform(a: Float, b: Float, op: Char): Float = op match {
  case '+' => a + b; case '-' => a - b
  case '*' => a * b; case '/' => a / b
  case _   => 0.0f
}
def evaluatePostfix(postfix: String): Float = {
  val st = mutable.Stack[Float]()
  for (ch <- postfix) {
    if (ch.isDigit) st.push((ch - '0').toFloat)
    else {
      val b = st.pop(); val a = st.pop()
      st.push(perform(a, b, ch))
    }
  }
  st.top
}

object Main extends App {
  println(evaluatePostfix("231*+9-"))
  println(evaluatePostfix("23*"))
  println(evaluatePostfix("234*+"))
}
```


## Complexity Analysis

Every character is processed once. Each operator triggers at most three stack operations (two pops, one push). The stack's maximum depth is bounded by the number of operands, which is bounded by the input length.

> **All cases** — Time: **O(N)** | Space: **O(N)**

***

# Evaluate a postfix expression

## Problem Statement

Given a string `postfix` representing a postfix expression with single-digit operands and the operators `+`, `-`, `*`, `/`, evaluate it and return the result as a float.

### Example

> -   **Input:** `postfix = "231*+9-"`
> -   **Output:** `-4.000`
> -   **Explanation:** Equivalent infix is `(2 + 3*1) - 9 = -4`.

## Solution

The full evaluator from above, written compactly. Same code, just packaged as the answer to the problem.


```pseudocode
function evaluatePostfix(postfix):
    ops ← { '+': add, '−': sub, '*': mul, '/': div }
    stack ← empty stack
    for each ch in postfix:
        if ch is digit: push float(ch)
        else:
            b ← pop(); a ← pop()
            push ops[ch](a, b)
    return top of stack
```

```python run
def evaluate_postfix(postfix: str) -> float:
    OPS = { '+': lambda a, b: a + b,
            '-': lambda a, b: a - b,
            '*': lambda a, b: a * b,
            '/': lambda a, b: a / b }
    stack = []
    for ch in postfix:
        if ch.isdigit(): stack.append(float(ch))
        else:
            b = stack.pop(); a = stack.pop()
            stack.append(OPS[ch](a, b))
    return stack[-1]

print(evaluate_postfix("231*+9-"))   # -4.0
```

```java run
public class Main {
    static float evaluatePostfix(String postfix) {
        java.util.Deque<Float> st = new java.util.ArrayDeque<>();
        for (char ch : postfix.toCharArray()) {
            if (Character.isDigit(ch)) st.push((float)(ch - '0'));
            else {
                float b = st.pop(), a = st.pop();
                st.push(switch (ch) {
                    case '+' -> a + b; case '-' -> a - b;
                    case '*' -> a * b; case '/' -> a / b;
                    default  -> 0;
                });
            }
        }
        return st.peek();
    }
    public static void main(String[] args) {
        System.out.println(evaluatePostfix("231*+9-"));
    }
}
```

```c run
#include <stdio.h>
#include <ctype.h>

float evaluate_postfix(const char *postfix) {
    float st[256]; int top = -1;
    for (const char *p = postfix; *p; p++) {
        if (isdigit((unsigned char)*p)) st[++top] = (float)(*p - '0');
        else {
            float b = st[top--], a = st[top--];
            switch (*p) {
                case '+': st[++top] = a + b; break;
                case '-': st[++top] = a - b; break;
                case '*': st[++top] = a * b; break;
                case '/': st[++top] = a / b; break;
            }
        }
    }
    return st[top];
}

int main() { printf("%.3f\n", evaluate_postfix("231*+9-")); }
```

```scala run
import scala.collection.mutable

def evaluatePostfix(postfix: String): Float = {
  val st = mutable.Stack[Float]()
  for (ch <- postfix) {
    if (ch.isDigit) st.push((ch - '0').toFloat)
    else {
      val b = st.pop(); val a = st.pop()
      st.push(ch match {
        case '+' => a + b; case '-' => a - b
        case '*' => a * b; case '/' => a / b
      })
    }
  }
  st.top
}

object Main extends App { println(evaluatePostfix("231*+9-")) }
```


***

# Understanding the evaluation of prefix expressions

Prefix is postfix's mirror. Same algorithm with two changes:

1. **Scan right to left** instead of left to right.
2. **Operand order is flipped.** When we hit an operator, the *first* value popped is the *left* operand (because under right-to-left scan, the most recently seen operand is the leftmost one), and the second pop is the *right* operand. This is the opposite of postfix.

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
    R["read token<br/>(scan right→left)"] --> Q{"digit?"}
    Q -->|"yes"| P["push token"]
    Q -->|"no (operator)"| O["pop a (left operand)<br/>pop b (right operand)<br/>push (a OP b)"]
    P --> R
    O --> R
```

<p align="center"><strong>Prefix evaluator — same shape as postfix; only the scan direction and the operand-pop order change. Easiest to implement by reversing the input string and reusing the postfix loop, taking care to flip the order of operand assignment.</strong></p>

## Walkthrough — `- + 8 / 6 3 2`

Equivalent infix: `(8 + 6/3) - 2 = 8`. Reversed string: `2 3 6 / 8 + -`. Walk the reversed string left-to-right, treating the first pop as the left operand:

| Step | Token | Action (first pop = left operand) | Stack (top right) |
|---:|:---:|---|---|
| 1 | `2` | push | `[2]` |
| 2 | `3` | push | `[2, 3]` |
| 3 | `6` | push | `[2, 3, 6]` |
| 4 | `/` | pop a=6, pop b=3, push `6/3=2` | `[2, 2]` |
| 5 | `8` | push | `[2, 2, 8]` |
| 6 | `+` | pop a=8, pop b=2, push `8+2=10` | `[2, 10]` |
| 7 | `-` | pop a=10, pop b=2, push `10-2=8` | `[8]` |
| — | end | result is the lone item | **`8`** |

<p align="center"><strong>Prefix evaluation, after reversing the input — same single-pass shape as postfix, but the first pop is the <em>left</em> operand. Notice <code>6/3=2</code>, not <code>3/6</code>; the operand order matters and the swap is the only thing that's changed from postfix.</strong></p>

***

# Evaluate a prefix expression

## Problem Statement

Given a string `prefix` (single-digit operands, operators `+`, `-`, `*`, `/`), evaluate and return the result.

### Example

> -   **Input:** `prefix = "-+8/632"`
> -   **Output:** `8.000`
> -   **Explanation:** Equivalent infix is `(8 + 6/3) - 2 = 8`.

## Solution


```pseudocode
function evaluatePrefix(prefix):
    stack ← empty stack
    for each ch in prefix scanned right to left:
        if ch is digit: push float(ch)
        else:
            a ← pop()   # LEFT operand (first pop under R→L scan)
            b ← pop()   # RIGHT operand
            push perform(a, b, ch)
    return top of stack
```

```python run
def perform(a: float, b: float, op: str) -> float:
    if op == '+': return a + b
    if op == '-': return a - b
    if op == '*': return a * b
    if op == '/': return a / b
    return 0.0

def evaluate_prefix(prefix: str) -> float:
    stack = []
    for ch in reversed(prefix):                # scan right → left
        if ch.isdigit():
            stack.append(float(ch))
        else:
            a = stack.pop()                    # popped first → LEFT operand
            b = stack.pop()                    # popped second → RIGHT operand
            stack.append(perform(a, b, ch))
    return stack[-1]

print(evaluate_prefix("-+8/632"))    # 8.0
print(evaluate_prefix("+23"))        # 5.0
print(evaluate_prefix("*+23-41"))    # 15.0  (== (2+3) * (4-1))
```

```java run
public class Main {
    static float perform(float a, float b, char op) {
        switch (op) {
            case '+': return a + b; case '-': return a - b;
            case '*': return a * b; case '/': return a / b;
            default:  return 0;
        }
    }
    static float evaluatePrefix(String prefix) {
        java.util.Deque<Float> st = new java.util.ArrayDeque<>();
        for (int i = prefix.length() - 1; i >= 0; i--) {
            char ch = prefix.charAt(i);
            if (Character.isDigit(ch)) st.push((float)(ch - '0'));
            else {
                float a = st.pop();    // LEFT operand
                float b = st.pop();    // RIGHT operand
                st.push(perform(a, b, ch));
            }
        }
        return st.peek();
    }
    public static void main(String[] args) {
        System.out.println(evaluatePrefix("-+8/632"));
        System.out.println(evaluatePrefix("+23"));
        System.out.println(evaluatePrefix("*+23-41"));
    }
}
```

```c run
#include <stdio.h>
#include <ctype.h>
#include <string.h>

static float perform_op(float a, float b, char op) {
    switch (op) { case '+': return a+b; case '-': return a-b;
                  case '*': return a*b; case '/': return a/b; default: return 0; }
}

float evaluate_prefix(const char *prefix) {
    float st[256]; int top = -1;
    int n = (int)strlen(prefix);
    for (int i = n - 1; i >= 0; i--) {
        char ch = prefix[i];
        if (isdigit((unsigned char)ch)) st[++top] = (float)(ch - '0');
        else {
            float a = st[top--];   // LEFT
            float b = st[top--];   // RIGHT
            st[++top] = perform_op(a, b, ch);
        }
    }
    return st[top];
}

int main() {
    printf("%.3f\n", evaluate_prefix("-+8/632"));
    printf("%.3f\n", evaluate_prefix("+23"));
    printf("%.3f\n", evaluate_prefix("*+23-41"));
}
```

```scala run
import scala.collection.mutable

def perform(a: Float, b: Float, op: Char): Float = op match {
  case '+' => a + b; case '-' => a - b
  case '*' => a * b; case '/' => a / b
  case _   => 0f
}
def evaluatePrefix(prefix: String): Float = {
  val st = mutable.Stack[Float]()
  for (ch <- prefix.reverse) {
    if (ch.isDigit) st.push((ch - '0').toFloat)
    else { val a = st.pop(); val b = st.pop(); st.push(perform(a, b, ch)) }
  }
  st.top
}

object Main extends App {
  println(evaluatePrefix("-+8/632"))
  println(evaluatePrefix("+23"))
  println(evaluatePrefix("*+23-41"))
}
```


> **Algorithm**
>
> -   **Step 1:** Initialise an empty stack.
> -   **Step 2:** For each character `ch` in the prefix string, **right to left**:
>     -   **2.1** If `ch` is a digit, push its numeric value.
>     -   **2.2** Else: `op1 = stack.pop()` (LEFT), `op2 = stack.pop()` (RIGHT), push `apply(op1, ch, op2)`.
> -   **Step 3:** Return `stack.top()`.

## Complexity Analysis

> **All cases** — Time: **O(N)** | Space: **O(N)**

***

# Evaluate an infix expression

## Problem Statement

Given an infix expression like `(1+2)*(3/4)`, evaluate it and return the result.

### Example

> -   **Input:** `infix = "(1+2)*(3/4)"`
> -   **Output:** `2.250`

## Approach

The trick: **don't evaluate infix directly** — *convert it to postfix* (using the algorithm in the next lesson, which uses one stack), and then evaluate the postfix (using the algorithm we just built, which uses one stack). Two passes; two stacks; same overall O(N).

The full conversion from infix to postfix gets its own lesson because there's quite a bit of nuance — operator precedence comparisons, parentheses handling, the fact that `^` is right-associative while `*` and `/` are left-associative. We'll show the converter inline below for completeness, but the *teaching* of how it works is in lesson 6.

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
    INFIX["infix:<br/>(1+2)*(3/4)"] -->|"convert<br/>(stack 1)"| POSTFIX["postfix:<br/>12+34/*"]
    POSTFIX -->|"evaluate<br/>(stack 2)"| RESULT["2.250"]
    style RESULT fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Infix evaluator — convert first (lesson 6 covers the converter), then evaluate. Each stage is a simple single-stack algorithm; combined, they handle parentheses, precedence, and associativity in two linear passes.</strong></p>

## Solution


```pseudocode
function evaluateInfix(infix):
    return evaluatePostfix(infixToPostfix(infix))

function infixToPostfix(infix):
    ops ← empty stack; out ← empty list
    for each ch in infix:
        if ch is alnum: append ch to out
        else if ch = '(': push ch
        else if ch = ')':
            while top ≠ '(': append pop() to out
            pop '('
        else: # operator
            while ops not empty AND top ≠ '(' AND prec(top) ≥ prec(ch):
                append pop() to out
            push ch
    while ops not empty: append pop() to out
    return join(out)
```

```python run
PREC = {'^': 3, '*': 2, '/': 2, '+': 1, '-': 1}

def is_op(ch): return ch in PREC

def infix_to_postfix(infix: str) -> str:
    st, out = [], []
    for ch in infix:
        if ch.isalnum():            out.append(ch)
        elif ch == '(':              st.append(ch)
        elif ch == ')':
            while st and st[-1] != '(': out.append(st.pop())
            if st and st[-1] == '(':    st.pop()
        elif is_op(ch):
            while st and st[-1] != '(' and PREC.get(st[-1], 0) >= PREC[ch]:
                out.append(st.pop())
            st.append(ch)
    while st: out.append(st.pop())
    return ''.join(out)

def perform(a, b, op):
    if op == '+': return a + b
    if op == '-': return a - b
    if op == '*': return a * b
    if op == '/': return a / b
    return 0.0

def evaluate_postfix(postfix: str) -> float:
    st = []
    for ch in postfix:
        if ch.isdigit(): st.append(float(ch))
        else:
            b = st.pop(); a = st.pop()
            st.append(perform(a, b, ch))
    return st[-1]

def evaluate_infix(infix: str) -> float:
    return evaluate_postfix(infix_to_postfix(infix))

print(evaluate_infix("(1+2)*(3/4)"))   # 2.25
print(evaluate_infix("2+3*4"))         # 14.0
print(evaluate_infix("(2+3)*4"))       # 20.0
```

```java run
import java.util.*;

public class Main {
    static int prec(char op) {
        switch (op) { case '^': return 3; case '*': case '/': return 2;
                      case '+': case '-': return 1; default: return 0; }
    }
    static boolean isOp(char ch) { return "+-*/^".indexOf(ch) >= 0; }

    static String infixToPostfix(String infix) {
        Deque<Character> st = new ArrayDeque<>();
        StringBuilder out = new StringBuilder();
        for (char ch : infix.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) out.append(ch);
            else if (ch == '(') st.push(ch);
            else if (ch == ')') {
                while (!st.isEmpty() && st.peek() != '(') out.append(st.pop());
                if (!st.isEmpty()) st.pop();
            } else if (isOp(ch)) {
                while (!st.isEmpty() && st.peek() != '(' && prec(st.peek()) >= prec(ch))
                    out.append(st.pop());
                st.push(ch);
            }
        }
        while (!st.isEmpty()) out.append(st.pop());
        return out.toString();
    }

    static float perform(float a, float b, char op) {
        switch (op) { case '+': return a+b; case '-': return a-b;
                      case '*': return a*b; case '/': return a/b; default: return 0; }
    }
    static float evaluatePostfix(String postfix) {
        Deque<Float> st = new ArrayDeque<>();
        for (char ch : postfix.toCharArray()) {
            if (Character.isDigit(ch)) st.push((float)(ch - '0'));
            else { float b = st.pop(), a = st.pop(); st.push(perform(a, b, ch)); }
        }
        return st.peek();
    }

    static float evaluateInfix(String infix) { return evaluatePostfix(infixToPostfix(infix)); }

    public static void main(String[] args) {
        System.out.println(evaluateInfix("(1+2)*(3/4)"));
        System.out.println(evaluateInfix("2+3*4"));
        System.out.println(evaluateInfix("(2+3)*4"));
    }
}
```

```c run
#include <stdio.h>
#include <string.h>
#include <ctype.h>

int prec(char op) { switch(op){case '^':return 3;case '*':case '/':return 2;case '+':case '-':return 1;default:return 0;} }
int is_op(char c) { return c == '+'||c == '-'||c == '*'||c == '/'||c == '^'; }

void infix_to_postfix(const char *infix, char *out) {
    char st[256]; int top = -1; int o = 0;
    for (const char *p = infix; *p; p++) {
        char c = *p;
        if (isalnum((unsigned char)c)) out[o++] = c;
        else if (c == '(') st[++top] = c;
        else if (c == ')') {
            while (top >= 0 && st[top] != '(') out[o++] = st[top--];
            if (top >= 0) top--;
        } else if (is_op(c)) {
            while (top >= 0 && st[top] != '(' && prec(st[top]) >= prec(c)) out[o++] = st[top--];
            st[++top] = c;
        }
    }
    while (top >= 0) out[o++] = st[top--];
    out[o] = 0;
}

float perform_op(float a, float b, char op) { switch(op){case '+':return a+b;case '-':return a-b;case '*':return a*b;case '/':return a/b;default:return 0;} }

float evaluate_postfix(const char *postfix) {
    float st[256]; int top = -1;
    for (const char *p = postfix; *p; p++) {
        if (isdigit((unsigned char)*p)) st[++top] = (float)(*p - '0');
        else { float b = st[top--], a = st[top--]; st[++top] = perform_op(a, b, *p); }
    }
    return st[top];
}

float evaluate_infix(const char *infix) { char buf[256]; infix_to_postfix(infix, buf); return evaluate_postfix(buf); }

int main() {
    printf("%.3f\n", evaluate_infix("(1+2)*(3/4)"));
    printf("%.3f\n", evaluate_infix("2+3*4"));
    printf("%.3f\n", evaluate_infix("(2+3)*4"));
}
```

```scala run
import scala.collection.mutable

def prec(op: Char): Int = op match { case '^' => 3; case '*' | '/' => 2; case '+' | '-' => 1; case _ => 0 }
def isOp(c: Char): Boolean = "+-*/^".contains(c)

def infixToPostfix(infix: String): String = {
  val st = mutable.Stack[Char](); val out = new StringBuilder
  for (c <- infix) {
    if (c.isLetterOrDigit) out.append(c)
    else if (c == '(') st.push(c)
    else if (c == ')') {
      while (st.nonEmpty && st.top != '(') out.append(st.pop())
      if (st.nonEmpty) st.pop()
    } else if (isOp(c)) {
      while (st.nonEmpty && st.top != '(' && prec(st.top) >= prec(c)) out.append(st.pop())
      st.push(c)
    }
  }
  while (st.nonEmpty) out.append(st.pop())
  out.toString
}

def perform(a: Float, b: Float, op: Char): Float = op match { case '+' => a+b; case '-' => a-b; case '*' => a*b; case '/' => a/b; case _ => 0 }

def evaluatePostfix(postfix: String): Float = {
  val st = mutable.Stack[Float]()
  for (c <- postfix) {
    if (c.isDigit) st.push((c - '0').toFloat)
    else { val b = st.pop(); val a = st.pop(); st.push(perform(a, b, c)) }
  }
  st.top
}

def evaluateInfix(infix: String): Float = evaluatePostfix(infixToPostfix(infix))

object Main extends App {
  println(evaluateInfix("(1+2)*(3/4)"))
  println(evaluateInfix("2+3*4"))
  println(evaluateInfix("(2+3)*4"))
}
```


***

## Final Takeaway

Three evaluators, one architecture: **a stack of operands plus a left-to-right or right-to-left scan**. The differences between the three notations collapse to a few lines of code.

Three lessons:

1. **The stack is the working memory.** Every partial result lives there until the next operator consumes it. The maximum stack depth is bounded by the number of nested operations; for sane expressions, that's tiny.
2. **Operand order matters for non-commutative operators.** Postfix: first pop = right operand. Prefix: first pop = left operand. Get this backwards and `+` and `*` will still be correct, but `-` and `/` will silently produce wrong answers.
3. **Infix is a wrapper, not a primitive.** Real infix evaluators don't try to evaluate infix directly — they convert to postfix (or to an AST, which is just a tree-shaped postfix) and evaluate that. Two simple stages compose into a calculator that handles arbitrary precedence and parentheses.

> *Coming up — the **infix-to-postfix converter** that this lesson skipped over. Lesson 6 is the formal Shunting-Yard algorithm: one stack of operators, one output buffer, careful precedence comparisons, parentheses handling. It's the algorithm Edsger Dijkstra invented in 1961 and that every serious calculator and parser still uses today. Once you have it, you have a complete arithmetic evaluator.*
