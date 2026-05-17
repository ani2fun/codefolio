# Widget Spec — `call-stack`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render a vertical stack of activation frames as a function is called,
recurses, returns, and unwinds. One widget, two modes:

- `mode: "stack"` — the canonical bottom-up frame ladder. New frames push on
  top; returns pop. Each frame shows function name, locals, and (on
  return) the value handed back to the caller. Used to teach the memory
  model, stack overflow, and "what happens during a function call".
- `mode: "recursion-tree"` — the same call graph rendered as a tree
  (root = top-level call; children = sub-calls), with values bubbling up
  edges on return. Used to teach the **structure** of a recursive
  decomposition (head/tail/multiple/multidimensional patterns).

A single widget covers both because the underlying data is identical (a
sequence of `call` / `return` events over named frames); only the layout
projection differs. Step semantics: each step advances by one
`call` or `return` event, with optional intermediate "show local update"
beats for chapters that need them.

## 2. Source-diagram inventory

All sources under
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/algorithms/1.recursion/`.
10 interactive diagrams total in source Phase 10:

| Source file | Line | Frames | What | Suggested mode |
|---|---:|---:|---|---|
| `02-nested-functions/02-understanding-nested-functions.md` | 126 | 13 | Every function call creates its own stack frame | `stack` |
| `02-nested-functions/03-understanding-stack-overflow.md` | 143 | 10 | Too many nested calls → stack overflow | `stack` |
| `02-nested-functions/03-understanding-stack-overflow.md` | 240 | 4 | Stack overflow from large locals | `stack` |
| `02-nested-functions/03-understanding-stack-overflow.md` | 379 | 8 | Stack overflow from deep nesting + large locals | `stack` |
| `03-recursion/02-exploring-a-possible-solution.md` | 14 | 4 | Asking question to the person in front (intuition) | `stack` |
| `03-recursion/02-exploring-a-possible-solution.md` | 36 | 7 | Adding answers (return-path animation) | `stack` |
| `03-recursion/03-key-components-of-recursion.md` | 47 | 11 | Recursion tree — 5th person in queue | `recursion-tree` |
| `03-recursion/04-implementing-recursive-algorithms.md` | 114 | 10 | Function calls (push frames) | `stack` |
| `03-recursion/04-implementing-recursive-algorithms.md` | 152 | 7 | Stack unwinding (pop frames with return values) | `stack` |
| `07-pattern-multidimensional-recursion/02-identifying-multidimensional-recursion.md` | 59 | 39 | Processing of `choose(5, 3)` — multi-branch recursion | `recursion-tree` |

Plus orphan usage (no source diagrams):

- `01-foundations/02-recurrence-and-master-theorem.md` — cost-decomposition
  recurrence trees. Per audit decision, `recurrence-tree` is a **separate**
  widget (different concern: cost annotations + level sums); `call-stack` is
  not asked to cover that chapter.
- `05-algorithms-by-strategy/01-recursion/04-pattern-head-recursion.md`
  through `07-pattern-multidimensional-recursion.md` — pattern chapters
  that often need an additional `recursion-tree` example beyond what source
  provides.

## 3. Destination chapter usage

| Destination chapter | Diagrams | Default mode |
|---|---:|---|
| `05-algorithms-by-strategy/01-recursion/01-introduction-to-memory-model.md` | 0 (intuition prose only — optional small `stack` demo) | `stack` |
| `05-algorithms-by-strategy/01-recursion/02-nested-functions.md` | 4 (1 plain stack, 3 overflow variants) | `stack` |
| `05-algorithms-by-strategy/01-recursion/03-recursion.md` | 5 (2 intro stacks, 1 recursion-tree, 1 push, 1 unwind) | mixed |
| `05-algorithms-by-strategy/01-recursion/04-pattern-head-recursion.md` | 0 (orphan-style) — author `recursion-tree` for a head-recursive `print(1..n)` | `recursion-tree` |
| `05-algorithms-by-strategy/01-recursion/05-pattern-tail-recursion.md` | 0 (orphan-style) — author `recursion-tree` showing constant-depth trace | `recursion-tree` |
| `05-algorithms-by-strategy/01-recursion/06-pattern-multiple-recursion.md` | 0 (orphan-style) — `recursion-tree` for Fibonacci or binary recursion | `recursion-tree` |
| `05-algorithms-by-strategy/01-recursion/07-pattern-multidimensional-recursion.md` | 1 (`choose(5,3)`) | `recursion-tree` |

The widget is **not** used for `01-foundations/02-recurrence-and-master-theorem.md`
(that's the standalone `recurrence-tree` widget per the audit's conditional-decision
table).

## 4. Payload schema sketch

The schema is event-based: a list of `frames` (definition) + a list of
`events` (a call/return tape). The widget computes per-step frame state by
replaying events up to the current step. Steps line up with events 1:1 by
default; authors can add `pause` events that emit a step without
mutating frame state (used for "show locals" beats).

```jsonc
{
  "title": "Recursive sum(3) push/pop",
  "mode": "stack",            // "stack" | "recursion-tree"
  "frameDefs": [
    {
      "id": "sum-3",
      "name": "sum(3)",
      "locals": [{"name": "n", "value": "3"}]
    },
    {
      "id": "sum-2",
      "name": "sum(2)",
      "locals": [{"name": "n", "value": "2"}],
      "parent": "sum-3"       // required in recursion-tree mode; ignored in stack mode
    },
    {
      "id": "sum-1",
      "name": "sum(1)",
      "locals": [{"name": "n", "value": "1"}],
      "parent": "sum-2"
    }
  ],
  "events": [
    {"kind": "call",   "frameId": "sum-3", "msg": "Top-level sum(3) — push frame"},
    {"kind": "update", "frameId": "sum-3", "locals": [{"name": "n", "value": "3"}], "msg": "Local n = 3"},
    {"kind": "call",   "frameId": "sum-2", "msg": "Recurse sum(2) — push frame"},
    {"kind": "call",   "frameId": "sum-1", "msg": "Recurse sum(1) — push frame"},
    {"kind": "return", "frameId": "sum-1", "value": "1", "msg": "Base case: return 1"},
    {"kind": "return", "frameId": "sum-2", "value": "3", "msg": "Return 2 + 1 = 3"},
    {"kind": "return", "frameId": "sum-3", "value": "6", "msg": "Return 3 + 3 = 6"}
  ],
  // Optional: limit the visible stack depth + render a "stack memory" frame
  // around the whole widget, with a red-zone band for overflow visualisations.
  "stackLimit": 5,
  "overflowAt": 4              // when current depth ≥ this, paint frames in red
}
```

### Canonical frame statuses (closed catalog)

- `active` — top of stack (currently executing).
- `waiting` — paused, waiting for a recursive call to return.
- `returning` — most recent return (highlighted as it pops + value bubbles to parent).
- `popped` — drawn in recursion-tree mode after return, dimmed in stack mode.
- `overflow` — frame painted red when `depth ≥ overflowAt`.

### Mode-specific layout

- **`stack`** — frames stack vertically; bottom is the first call. Frame
  width fixed; height grows with `locals` count. A "stack pointer" arrow on
  the right tracks the current top. Optional rendered "stack memory" outline
  with overflow zone.
- **`recursion-tree`** — laid out top-down via the `parent` field. Sibling
  frames spread horizontally at the same depth. Return values render as
  labels on the edge from child → parent at the moment of return, with a
  brief upward animation (200ms).

## 5. POC payloads

### 5.1 Stack mode — basic push/pop

````d3 widget=call-stack
{
  "title": "sum(3) push and pop",
  "mode": "stack",
  "frameDefs": [
    {"id": "sum-3", "name": "sum(3)", "locals": [{"name": "n", "value": "3"}]},
    {"id": "sum-2", "name": "sum(2)", "locals": [{"name": "n", "value": "2"}]},
    {"id": "sum-1", "name": "sum(1)", "locals": [{"name": "n", "value": "1"}]}
  ],
  "events": [
    {"kind": "call",   "frameId": "sum-3", "msg": "Push sum(3)"},
    {"kind": "call",   "frameId": "sum-2", "msg": "Push sum(2)"},
    {"kind": "call",   "frameId": "sum-1", "msg": "Push sum(1)"},
    {"kind": "return", "frameId": "sum-1", "value": "1", "msg": "Base case: return 1"},
    {"kind": "return", "frameId": "sum-2", "value": "3", "msg": "Return 2 + 1 = 3"},
    {"kind": "return", "frameId": "sum-3", "value": "6", "msg": "Return 3 + 3 = 6"}
  ]
}
````

### 5.2 Stack mode — stack overflow

````d3 widget=call-stack
{
  "title": "Unbounded recursion → stack overflow",
  "mode": "stack",
  "stackLimit": 5,
  "overflowAt": 5,
  "frameDefs": [
    {"id": "f1", "name": "f(1)"},
    {"id": "f2", "name": "f(2)"},
    {"id": "f3", "name": "f(3)"},
    {"id": "f4", "name": "f(4)"},
    {"id": "f5", "name": "f(5)"}
  ],
  "events": [
    {"kind": "call", "frameId": "f1", "msg": "Push f(1)"},
    {"kind": "call", "frameId": "f2", "msg": "Recurse: push f(2)"},
    {"kind": "call", "frameId": "f3", "msg": "Recurse: push f(3)"},
    {"kind": "call", "frameId": "f4", "msg": "Recurse: push f(4) — nearing limit"},
    {"kind": "call", "frameId": "f5", "msg": "Push f(5) — STACK OVERFLOW"}
  ]
}
````

### 5.3 Recursion-tree mode — Fibonacci

````d3 widget=call-stack
{
  "title": "fib(4) recursion tree",
  "mode": "recursion-tree",
  "frameDefs": [
    {"id": "fib-4",  "name": "fib(4)"},
    {"id": "fib-3a", "name": "fib(3)", "parent": "fib-4"},
    {"id": "fib-2a", "name": "fib(2)", "parent": "fib-4"},
    {"id": "fib-2b", "name": "fib(2)", "parent": "fib-3a"},
    {"id": "fib-1a", "name": "fib(1)", "parent": "fib-3a"},
    {"id": "fib-1b", "name": "fib(1)", "parent": "fib-2b"},
    {"id": "fib-0a", "name": "fib(0)", "parent": "fib-2b"},
    {"id": "fib-1c", "name": "fib(1)", "parent": "fib-2a"},
    {"id": "fib-0b", "name": "fib(0)", "parent": "fib-2a"}
  ],
  "events": [
    {"kind": "call",   "frameId": "fib-4",  "msg": "fib(4)"},
    {"kind": "call",   "frameId": "fib-3a", "msg": "Recurse fib(3) (left child)"},
    {"kind": "call",   "frameId": "fib-2b", "msg": "Recurse fib(2)"},
    {"kind": "call",   "frameId": "fib-1b", "msg": "Recurse fib(1)"},
    {"kind": "return", "frameId": "fib-1b", "value": "1", "msg": "fib(1) = 1"},
    {"kind": "call",   "frameId": "fib-0a", "msg": "Recurse fib(0)"},
    {"kind": "return", "frameId": "fib-0a", "value": "0", "msg": "fib(0) = 0"},
    {"kind": "return", "frameId": "fib-2b", "value": "1", "msg": "fib(2) = 1 + 0 = 1"},
    {"kind": "call",   "frameId": "fib-1a", "msg": "Recurse fib(1)"},
    {"kind": "return", "frameId": "fib-1a", "value": "1", "msg": "fib(1) = 1"},
    {"kind": "return", "frameId": "fib-3a", "value": "2", "msg": "fib(3) = 1 + 1 = 2"},
    {"kind": "call",   "frameId": "fib-2a", "msg": "Recurse fib(2) (right child of fib(4))"},
    {"kind": "call",   "frameId": "fib-1c", "msg": "Recurse fib(1)"},
    {"kind": "return", "frameId": "fib-1c", "value": "1", "msg": "fib(1) = 1"},
    {"kind": "call",   "frameId": "fib-0b", "msg": "Recurse fib(0)"},
    {"kind": "return", "frameId": "fib-0b", "value": "0", "msg": "fib(0) = 0"},
    {"kind": "return", "frameId": "fib-2a", "value": "1", "msg": "fib(2) = 1"},
    {"kind": "return", "frameId": "fib-4",  "value": "3", "msg": "fib(4) = 2 + 1 = 3"}
  ]
}
````

### 5.4 Recursion-tree — `choose(5, 3)` (compressed from source's 39 frames)

````d3 widget=call-stack
{
  "title": "C(5, 3) — multidimensional recursion",
  "mode": "recursion-tree",
  "frameDefs": [
    {"id": "c53", "name": "C(5,3)"},
    {"id": "c43", "name": "C(4,3)", "parent": "c53"},
    {"id": "c42", "name": "C(4,2)", "parent": "c53"},
    {"id": "c33", "name": "C(3,3)", "parent": "c43"},
    {"id": "c32", "name": "C(3,2)", "parent": "c43"},
    {"id": "c32b", "name": "C(3,2)", "parent": "c42"},
    {"id": "c31", "name": "C(3,1)", "parent": "c42"}
  ],
  "events": [
    {"kind": "call",   "frameId": "c53", "msg": "C(5,3) = C(4,3) + C(4,2)"},
    {"kind": "call",   "frameId": "c43", "msg": "Recurse left: C(4,3)"},
    {"kind": "call",   "frameId": "c33", "msg": "C(3,3) — base case (k = n)"},
    {"kind": "return", "frameId": "c33", "value": "1", "msg": "C(3,3) = 1"},
    {"kind": "call",   "frameId": "c32", "msg": "C(3,2) — recurse further (compressed)"},
    {"kind": "return", "frameId": "c32", "value": "3", "msg": "C(3,2) = 3"},
    {"kind": "return", "frameId": "c43", "value": "4", "msg": "C(4,3) = 1 + 3 = 4"},
    {"kind": "call",   "frameId": "c42", "msg": "Recurse right: C(4,2)"},
    {"kind": "call",   "frameId": "c32b", "msg": "C(3,2) — already shown returns 3"},
    {"kind": "return", "frameId": "c32b", "value": "3", "msg": "C(3,2) = 3"},
    {"kind": "call",   "frameId": "c31", "msg": "C(3,1) — recurse further (compressed)"},
    {"kind": "return", "frameId": "c31", "value": "3", "msg": "C(3,1) = 3"},
    {"kind": "return", "frameId": "c42", "value": "6", "msg": "C(4,2) = 3 + 3 = 6"},
    {"kind": "return", "frameId": "c53", "value": "10", "msg": "C(5,3) = 4 + 6 = 10"}
  ]
}
````

### 5.5 Stack mode — local-variable update beat

````d3 widget=call-stack
{
  "title": "main calls greet(\"Ada\"); local mutation visible",
  "mode": "stack",
  "frameDefs": [
    {"id": "main",  "name": "main()"},
    {"id": "greet", "name": "greet(\"Ada\")", "locals": [{"name": "name", "value": "\"Ada\""}, {"name": "msg", "value": "\"\""}]}
  ],
  "events": [
    {"kind": "call",   "frameId": "main",  "msg": "Push main()"},
    {"kind": "call",   "frameId": "greet", "msg": "Push greet(\"Ada\")"},
    {"kind": "update", "frameId": "greet", "locals": [{"name": "name", "value": "\"Ada\""}, {"name": "msg", "value": "\"hi Ada\""}], "msg": "msg assigned"},
    {"kind": "return", "frameId": "greet", "value": "\"hi Ada\"", "msg": "Return"},
    {"kind": "return", "frameId": "main",  "value": "0", "msg": "Exit"}
  ]
}
````

## 6. Closest existing widget to mimic

`LinkedList` for the node-rendering boundary (per-frame `<g>` keyed on
`frame.id`, with smooth transform transitions on push/pop). The `frames`
list under `stack` mode behaves like a vertically-stacked variant of
`LinkedList`'s nodes; the `recursion-tree` mode shares its layout
philosophy with the `decision-tree` widget (see decision-tree spec).

`ArrayTraversal`'s `Spec` + `Step` separation maps directly: top-level
`frameDefs` ≈ `Spec.items`, per-step state derived by replaying events ≈
per-step `markers`/`range`.

## 7. D3 selections plan

One top-level `g.call-stack__canvas`. The widget pre-computes a derived
`StackState` per step (replaying events; cached) so each render is a pure
function of `(spec, stepIndex)`.

Inside the canvas:

- **Frames**: `selectAll("g.call-stack__frame").data(visibleFrames, f => f.id)`
  - Enter: append `<g>` with `<rect>` (frame body) + `<text>` (name) +
    `<g class="call-stack__locals">` (locals list). Initial transform
    starts above its target slot then transitions in (push animation).
  - Update: transition `transform` (300ms) to new slot; rebind class for
    `active` / `waiting` / `returning` / `overflow` state.
  - Exit (stack mode): fade + slide up off the stack pointer (pop animation).
  - Exit (recursion-tree mode): suppressed — popped frames stay drawn but
    dim to `popped` state.
- **Stack pointer** (stack mode only): `selectAll("path.call-stack__pointer").data([topFrame])`,
  drawn as a `▶` glyph on the right of the active frame.
- **Tree edges** (recursion-tree mode only):
  `selectAll("path.call-stack__edge").data(edges, e => `${e.parent}-${e.child}`)`
  drawn as cubic Béziers from parent bottom to child top.
- **Return-value labels** (recursion-tree mode only): when a frame
  transitions through `returning`, append a `<text class="call-stack__return-label">`
  positioned on the edge and animate it up to the parent over 400ms before
  removing.
- **Stack memory chrome** (optional): outer `<rect class="call-stack__memory">`
  with a red-zone band at the `overflowAt` boundary, drawn once on mount.

Layout helpers:

- `stackY(slot)` — `paddingY + slot * frameHeight` (slot 0 at the bottom).
- `treeLayout(frames)` — Reingold-Tilford-style x assignment via a depth+order
  walk (simpler than full Reingold-Tilford; sufficient for ≤ 20 frames). Reuses
  the `LayeredLayout` candidate-shared abstraction from the `graph-explorer`
  spec.

## 8. Shared abstractions

Reuse:

- `Stepper.hook` — drives the step index over the events tape.
- `PayloadDecoder` — JSON envelope; canonical-status validation.

Probable shared with `decision-tree` / `dp-table` (defer extraction until
second consumer lands):

- **LayeredLayout** — assigns `(x, y)` per node given `parent` pointers,
  with sibling x-spread. Same logic the `decision-tree` and `dp-table`
  top-down recursion-tree need.
- **Bubble-up edge label animation** — return-value text travelling up the
  parent edge. Recurs in `decision-tree`'s "result of subproblem" overlay
  and `dp-table` top-down's "memoized return". Live in a future
  `widgets/internal/EdgeLabelTween.scala` if extracted.

## 9. Estimated build session count

**1 session.** The event-replay model is simple; both modes share the same
underlying `frames + events` structure. The recursion-tree layout is the
only non-trivial new code; everything else is enter/update/exit selections
with smooth transforms.

## 10. POC chapter

Demo book chapter at
`content/cortex/dsa-widget-catalog/04-call-stack.md` exhibits all
five POC payloads above, plus an `update`-beat-heavy payload that shows
a `for` loop incrementing a counter local across multiple steps without
push/pop, to validate `update` events render cleanly.

First real production usage is
`05-algorithms-by-strategy/01-recursion/02-nested-functions.md` (single
mode: `stack`; uses three of the four overflow variants). Then
`05-algorithms-by-strategy/01-recursion/03-recursion.md` brings in the
first `recursion-tree` payload (chapter 03 source line 47 — `5th person in queue`).
