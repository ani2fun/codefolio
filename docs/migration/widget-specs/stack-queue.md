# Widget Spec — `stack-queue`

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `stack-queue` and Arc 3 chapter payload writing.

## 1. Purpose

A single widget covers the four LIFO/FIFO topologies Phase 4 + Phase 5
teach: **stack** (LIFO with a top pointer), **queue** (FIFO with front +
back), **deque** (double-ended), and **priority queue** (ordering by
comparator). Each step renders the container state, the active operation
(push / pop / enqueue / dequeue / peek), and optional **input/output
strips** for the expression-conversion chapters (infix/postfix/prefix) —
the strip is a horizontal source-token row above the container and an
output-token row below, with markers showing the read cursor and append
position.

## 2. Source-diagram inventory

Source roots: 63 interactive diagrams total across the two phases.

### Phase 4 — Stack (`mode:"stack"`, 44 diagrams)

Source: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/5.stack/`.

#### Introduction & motivation

- `01-introduction-to-stacks/01-understanding-the-problem.md:11` —
  "Web browsers rely on LIFO" (9 frames → 5 steps)
- `01-introduction-to-stacks/01-understanding-the-problem.md:35` —
  "Text editors rely on LIFO undo" (7 frames → 5 steps)
- `01-introduction-to-stacks/01-understanding-the-problem.md:55` —
  "Nested function calls rely on LIFO" (7 frames → 5 steps)
- `01-introduction-to-stacks/02-exploring-a-possible-solution.md:9` —
  "Stack of plates" (11 frames → 6 steps)
- `01-introduction-to-stacks/02-exploring-a-possible-solution.md:43` —
  "Stack data structure mimics LIFO" (10 frames → 6 steps)
- `01-introduction-to-stacks/04-overview-of-supported-operations.md:9` —
  "Push operation" (4 frames → 3 steps)
- `01-introduction-to-stacks/04-overview-of-supported-operations.md:25` —
  "Pop operation" (4 frames → 3 steps)

#### Array-backed & linked-list-backed implementations

- `02-array-implementation-of-stacks/06-pushing-an-item-onto-the-stack.md:21`
  — "Push when stack not full" (3 frames → 2 steps)
- `02-array-implementation-of-stacks/07-popping-an-item-from-the-top-of-the-stack.md:21`
  — "Pop from a non-empty stack" (4 frames → 3 steps)
- `02-array-implementation-of-stacks/02-implementing-the-stack-class-using-an-array.md:294`
  — "Execution of code on stack class instance" (9 frames → 5 steps)
- `03-linked-list-implementation-of-stacks/06-pushing-an-item-onto-the-stack.md:21`
  — "Push when stack not full" (6 frames → 4 steps)
- `03-linked-list-implementation-of-stacks/07-popping-an-item-from-the-top-of-the-stack.md:21`
  — "Pop from a non-empty stack" (7 frames → 4 steps)
- `03-linked-list-implementation-of-stacks/02-implementing-the-stack-class-using-linked-list.md:418`
  — "Execution of code" (9 frames → 5 steps)

#### Expression-conversion (needs input/output strips)

- `04-infix-postfix-and-prefix-notations/01-understanding-the-infix-notation.md:31`
  — "Evaluating expression with one type of operation" (9 frames → 5 steps)
- `04-infix-postfix-and-prefix-notations/01-understanding-the-infix-notation.md:59`
  — "Evaluating expression with mixed operations" (9 frames → 5 steps)
- `04-infix-postfix-and-prefix-notations/02-understanding-the-postfix-notation.md:31`
  — "Evaluating a postfix expression" (11 frames → 6 steps)
- `04-infix-postfix-and-prefix-notations/03-understanding-the-prefix-notation.md:31`
  — "Evaluating a prefix expression" (11 frames → 6 steps)
- `05-evaluating-expressions-using-stack/01-understanding-the-evaluation-of-postfix-expressions.md:17`
  — "Evaluating postfix using stack" (30 frames → 10 steps)
- `05-evaluating-expressions-using-stack/03-understanding-the-evaluation-of-prefix-expressions.md:17`
  — "Evaluating prefix using stack" (30 frames → 10 steps)
- `06-converting-expressions-using-stack/01-understanding-postfix-to-prefix-conversion.md:29`
  — "postfix → prefix" (25 frames → 9 steps)
- `06-converting-expressions-using-stack/03-understanding-postfix-to-infix-conversion.md:19`
  — "postfix → infix" (25 frames → 9 steps)
- `06-converting-expressions-using-stack/05-understanding-prefix-to-postfix-conversion.md:29`
  — "prefix → postfix" (25 frames → 9 steps)
- `06-converting-expressions-using-stack/07-understanding-prefix-to-infix-conversion.md:19`
  — "prefix → infix" (25 frames → 9 steps)
- `06-converting-expressions-using-stack/09-understanding-infix-to-postfix-conversion.md:19`
  — "infix → postfix (intro)" (23 frames → 8 steps)
- `06-converting-expressions-using-stack/09-understanding-infix-to-postfix-conversion.md:75`
  — "infix → postfix (worked)" (31 frames → 10 steps)
- `06-converting-expressions-using-stack/11-understanding-infix-to-prefix-conversion.md:23`
  — "infix → prefix (intro)" (25 frames → 9 steps)
- `06-converting-expressions-using-stack/11-understanding-infix-to-prefix-conversion.md:83`
  — "infix → prefix (worked)" (33 frames → 10 steps)

#### Stack-pattern chapters

- `07-pattern-reversal/01-understanding-the-reversal-pattern.md:19` —
  "Reverse an array using a stack" (22 frames → 8 steps)
- `07-pattern-reversal/02-identifying-the-reversal-pattern.md:27` —
  "Reverse a string using a stack" (21 frames → 8 steps)
- `08-pattern-previous-closest-occurrence/01-understanding-the-previous-closest-occurrence-pattern.md:29`
  — "Find previous greater for all items" (40 frames → 10 steps)
- `08-pattern-previous-closest-occurrence/02-identifying-the-previous-closest-occurrence-pattern.md:23`
  — "Find previous greater for arr2 in arr1" (24 frames → 9 steps)
- `08-pattern-previous-closest-occurrence/02-identifying-the-previous-closest-occurrence-pattern.md:241`
  — "Find previous greater for all items in arr1" (32 frames → 10 steps)
- `08-pattern-previous-closest-occurrence/02-identifying-the-previous-closest-occurrence-pattern.md:309`
  — "Use indexMap to find results for arr2" (11 frames → 6 steps)
- `09-pattern-next-closest-occurance/01-understanding-the-next-closest-occurrence.md:25`
  — "Find previous greater in reverse" (40 frames → 10 steps)
- `09-pattern-next-closest-occurance/01-understanding-the-next-closest-occurrence.md:294`
  — "Find next greater for all items" (31 frames → 10 steps)
- `09-pattern-next-closest-occurance/02-identifying-the-next-closest-occurrence-pattern.md:23`
  — "Find next greater for arr2 in arr1" (23 frames → 9 steps)
- `09-pattern-next-closest-occurance/02-identifying-the-next-closest-occurrence-pattern.md:235`
  — "Find next greater for all items in arr1" (29 frames → 9 steps)
- `09-pattern-next-closest-occurance/02-identifying-the-next-closest-occurrence-pattern.md:297`
  — "Use indexMap to find results for arr2" (11 frames → 6 steps)
- `10-pattern-sequence-validation/01-understanding-the-sequence-validation-pattern.md:30`
  — "Validate sequence of start/end events" (15 frames → 7 steps)
- `10-pattern-sequence-validation/01-understanding-the-sequence-validation-pattern.md:64`
  — same, alternate (9 frames → 5 steps)
- `10-pattern-sequence-validation/02-identifying-the-sequence-validation-pattern.md:29`
  — "Validate sequence of brackets" (15 frames → 7 steps)
- `11-pattern-linear-evaluation/01-understanding-the-linear-evaluation-pattern.md:27`
  — "Evaluate linear sequence with triggers" (24 frames → 9 steps)
- `11-pattern-linear-evaluation/02-identifying-the-linear-evaluation-pattern.md:48`
  — "Simplify the linux path" (32 frames → 10 steps)
- `11-pattern-linear-evaluation/02-identifying-the-linear-evaluation-pattern.md:118`
  — same, worked (6 frames → 4 steps)

### Phase 5 — Queue (`mode:"queue"`, 19 diagrams)

Source: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/6.queue/`.

- `01-introduction-to-queues/01-understanding-the-problem.md:11` —
  "Music Players rely on FIFO" (10 frames → 5 steps)
- `01-introduction-to-queues/01-understanding-the-problem.md:37` —
  "Call scheduling FIFO" (9 frames → 5 steps)
- `01-introduction-to-queues/01-understanding-the-problem.md:61` —
  "Disk schedulers rely on FIFO" (8 frames → 5 steps)
- `01-introduction-to-queues/02-exploring-a-possible-solution.md:9` —
  "Queue of people at counter" (11 frames → 6 steps)
- `01-introduction-to-queues/02-exploring-a-possible-solution.md:43` —
  "Queue data structure" (11 frames → 6 steps)
- `01-introduction-to-queues/04-overview-of-supported-operations.md:9` —
  "Enqueue" (5 frames → 4 steps)
- `01-introduction-to-queues/04-overview-of-supported-operations.md:27` —
  "Dequeue" (4 frames → 3 steps)
- `02-array-implementation-of-queues/02-cyclic-nature-of-array-based-queues.md:5`
  — "Cyclic queue — add to / remove from different ends" (14 frames → 6 steps)
- `02-array-implementation-of-queues/02-cyclic-nature-of-array-based-queues.md:39`
  — "Adding/removing moves queue forward" (8 frames → 5 steps)
- `02-array-implementation-of-queues/02-cyclic-nature-of-array-based-queues.md:69`
  — "Start of array becomes new back index" (8 frames → 5 steps)
- `02-array-implementation-of-queues/08-enqueuing-an-item-in-the-queue.md:25`
  — "Enqueue when back != last" (4 frames → 3 steps)
- `02-array-implementation-of-queues/08-enqueuing-an-item-in-the-queue.md:46`
  — "Enqueue when back == last" (4 frames → 3 steps)
- `02-array-implementation-of-queues/09-dequeuing-an-item-from-the-queue.md:25`
  — "Dequeue when front != last" (4 frames → 3 steps)
- `02-array-implementation-of-queues/09-dequeuing-an-item-from-the-queue.md:46`
  — "Dequeue when front == last" (4 frames → 3 steps)
- `02-array-implementation-of-queues/03-implementing-the-queue-class-using-an-array.md:359`
  — "Execution of code" (10 frames → 5 steps)
- `03-linked-list-implementation-of-queues/07-enqueuing-an-item-in-the-queue.md:21`
  — "Enqueue to empty queue" (5 frames → 4 steps)
- `03-linked-list-implementation-of-queues/07-enqueuing-an-item-in-the-queue.md:46`
  — "Enqueue to non-empty queue" (6 frames → 4 steps)
- `03-linked-list-implementation-of-queues/08-dequeuing-an-item-from-the-queue.md:21`
  — "Dequeue when not empty" (7 frames → 4 steps)
- `03-linked-list-implementation-of-queues/02-implementing-a-queue-class-using-a-linked-list.md:424`
  — "Execution of code" (10 frames → 5 steps)

**Compression target**: 63 source diagrams → ~50 widget instances.

## 3. Destination chapter usage

### Phase 4 — Stack chapters

All under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/05-stack/`:

- `01-introduction-to-stacks.md` — 7 instances, `mode:"stack"` mostly with
  the LIFO-narrative payloads (browser back, undo).
- `02-array-implementation-of-stacks.md` — 3 instances of `mode:"stack"` (POC; see §10).
- `03-linked-list-implementation-of-stacks.md` — 3 instances of `mode:"stack"`.
- `04-infix-postfix-and-prefix-notations.md` — 4 instances of `mode:"stack"`
  WITH `inputStrip` / `outputStrip` for the conversion walkthroughs.
- `05-evaluating-expressions-using-stack.md` — 2 instances, same.
- `06-converting-expressions-using-stack.md` — 8 instances, same. This is
  the heaviest expression-conversion chapter.
- `07-pattern-reversal.md` — 2 instances of `mode:"stack"` (push everything, pop everything).
- `08-pattern-previous-closest-occurrence.md` — 4 instances.
- `09-pattern-next-closest-occurrence.md` — 5 instances.
- `10-pattern-sequence-validation.md` — 3 instances (bracket matcher).
- `11-pattern-linear-evaluation.md` — 3 instances (linux-path simplifier).
- `12-design.md` — 0 source diagrams.

### Phase 5 — Queue chapters

All under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/06-queue/`:

- `01-introduction-to-queues.md` — 7 instances, `mode:"queue"`.
- `02-array-implementation-of-queues.md` — 5 instances, `mode:"queue"` with
  the cyclic-array overlay (back/front wrap visually).
- `03-linked-list-implementation-of-queues.md` — 3 instances of `mode:"queue"`.
- `04-design.md` — 0 source diagrams.

### Orphan reuse

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/10-concurrency-and-systems/02-lock-free-queue.md`
  — `mode:"queue"` with CAS-loop annotations on the back pointer.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/05-algorithms-by-strategy/01-recursion/*`
  could reuse `mode:"stack"` to visualise the call stack — though
  `call-stack` widget is separately planned for that. Stick with
  `call-stack` for recursion; `stack-queue` for actual data-structure
  chapters.
- Priority-queue use (Phase 5 chapter
  `06-priority-queue` in the source, which the destination consolidates
  into the Queue chapter index) uses `mode:"priority"` to demonstrate
  the conceptual API before the Heap chapter takes over with `heap-tree`.

## 4. Payload schema sketch

```ts
{
  title: string,
  mode: "stack" | "queue" | "deque" | "priority",
  // Optional bounded-capacity rendering (array-backed mode). Absent =
  // unbounded (linked-list-backed). When set, the widget renders the
  // capacity cells in muted style for unoccupied slots.
  capacity?: number,
  // Optional priority-comparator label for `mode:"priority"`.
  // E.g. "min-heap (smaller priority first)" — purely informational.
  comparatorLabel?: string,
  items: Array<{
    id: string,
    value: string,
    // priority mode only — drives sort order in render
    priority?: number
  }>,
  // Optional expression-conversion strips. Either both absent or both
  // present. When present, the widget renders an input token row above
  // the container and an output token row below. Tokens are author-
  // defined strings — usually operands, operators, and parentheses.
  inputStrip?: {
    tokens: string[],
    label?: string                            // e.g. "infix: A + B * C"
  },
  outputStrip?: {
    tokens: string[],                          // initial state; usually empty
    label?: string                             // e.g. "postfix output"
  },
  steps: Array<{
    // Per-step container state. Items are listed in the order they were
    // pushed/enqueued; the widget renders bottom-to-top for stack,
    // front-to-back left-to-right for queue, etc.
    items?: Array<{ id, value, priority? }>,
    // The active operation banner.
    op?: "push" | "pop" | "enqueue" | "dequeue" | "peek" |
         "pushFront" | "pushBack" | "popFront" | "popBack" |
         "insert" | "extractTop",
    opValue?: string,
    // For expression-conversion steps:
    inputCursor?: number,                       // index into inputStrip.tokens
    outputItems?: string[],                     // current outputStrip.tokens state
    // Optional sub-pointer annotations on the container. Stack uses
    // "top"; queue uses "front" + "back"; deque uses both ends.
    markers?: Array<{
      name: "top" | "front" | "back" | "head" | "tail",
      itemId?: string,                          // bind to an item id
      slot?: number                              // OR a slot index (array mode)
    }>,
    msg: string
  }>
}
```

## 5. POC payloads

### 5.1 Minimal — empty stack, single push

```d3 widget=stack-queue
{
  "title": "Push 'A' onto an empty stack",
  "mode": "stack",
  "items": [],
  "steps": [
    {"op": "push", "opValue": "A", "items": [], "msg": "Empty stack. Push 'A'."},
    {"op": "push", "opValue": "A", "items": [{"id": "a1", "value": "A"}], "markers": [{"name": "top", "itemId": "a1"}], "msg": "'A' sits at the top."}
  ]
}
```

### 5.2 Typical stack — push three, pop one (POC chapter)

```d3 widget=stack-queue
{
  "title": "Stack — push A, B, C then pop",
  "mode": "stack",
  "capacity": 5,
  "items": [],
  "steps": [
    {"op": "push", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "top", "itemId": "a"}], "msg": "Push A → top = A."},
    {"op": "push", "opValue": "B", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "Push B → top = B."},
    {"op": "push", "opValue": "C", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "top", "itemId": "c"}], "msg": "Push C → top = C."},
    {"op": "pop", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "Pop → returns C; top = B."}
  ]
}
```

### 5.3 Queue — cyclic-array enqueue + dequeue (large-N stress)

```d3 widget=stack-queue
{
  "title": "Cyclic queue — array capacity 5, enqueue 3 then dequeue 2",
  "mode": "queue",
  "capacity": 5,
  "items": [],
  "steps": [
    {"op": "enqueue", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "a"}], "msg": "Enqueue A → front = back = A."},
    {"op": "enqueue", "opValue": "B", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "b"}], "msg": "Enqueue B → back = B."},
    {"op": "enqueue", "opValue": "C", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "c"}], "msg": "Enqueue C → back = C."},
    {"op": "dequeue", "items": [{"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "b"}, {"name": "back", "itemId": "c"}], "msg": "Dequeue → returns A; front = B."},
    {"op": "dequeue", "items": [{"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "c"}, {"name": "back", "itemId": "c"}], "msg": "Dequeue → returns B; front = back = C."}
  ]
}
```

### 5.4 Edge case — deque (mode flag exercise)

```d3 widget=stack-queue
{
  "title": "Deque — pushFront, pushBack, popBack",
  "mode": "deque",
  "items": [],
  "steps": [
    {"op": "pushBack", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "a"}], "msg": "pushBack A."},
    {"op": "pushFront", "opValue": "B", "items": [{"id": "b", "value": "B"}, {"id": "a", "value": "A"}], "markers": [{"name": "front", "itemId": "b"}, {"name": "back", "itemId": "a"}], "msg": "pushFront B → [B, A]."},
    {"op": "pushBack", "opValue": "C", "items": [{"id": "b", "value": "B"}, {"id": "a", "value": "A"}, {"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "b"}, {"name": "back", "itemId": "c"}], "msg": "pushBack C → [B, A, C]."},
    {"op": "popBack", "items": [{"id": "b", "value": "B"}, {"id": "a", "value": "A"}], "markers": [{"name": "front", "itemId": "b"}, {"name": "back", "itemId": "a"}], "msg": "popBack → returns C → [B, A]."}
  ]
}
```

### 5.5 Priority queue (priority mode exercise)

```d3 widget=stack-queue
{
  "title": "Priority queue (min) — insert 3 then extract top",
  "mode": "priority",
  "comparatorLabel": "min-heap: smaller priority first",
  "items": [],
  "steps": [
    {"op": "insert", "opValue": "Task-A (p=5)", "items": [{"id": "a", "value": "Task-A", "priority": 5}], "msg": "Insert Task-A with priority 5."},
    {"op": "insert", "opValue": "Task-B (p=2)", "items": [{"id": "b", "value": "Task-B", "priority": 2}, {"id": "a", "value": "Task-A", "priority": 5}], "msg": "Insert Task-B (p=2). Auto-sorted: B precedes A."},
    {"op": "insert", "opValue": "Task-C (p=7)", "items": [{"id": "b", "value": "Task-B", "priority": 2}, {"id": "a", "value": "Task-A", "priority": 5}, {"id": "c", "value": "Task-C", "priority": 7}], "msg": "Insert Task-C (p=7) → tail."},
    {"op": "extractTop", "items": [{"id": "a", "value": "Task-A", "priority": 5}, {"id": "c", "value": "Task-C", "priority": 7}], "msg": "extractTop → returns Task-B (p=2); A and C remain in priority order."}
  ]
}
```

### 5.6 Expression conversion — infix → postfix (inputStrip + outputStrip)

```d3 widget=stack-queue
{
  "title": "Convert infix A + B * C → postfix using a stack",
  "mode": "stack",
  "items": [],
  "inputStrip": {"tokens": ["A", "+", "B", "*", "C"], "label": "infix tokens"},
  "outputStrip": {"tokens": [], "label": "postfix output"},
  "steps": [
    {"inputCursor": 0, "outputItems": ["A"], "items": [], "msg": "Read 'A' (operand) → append to output."},
    {"inputCursor": 1, "outputItems": ["A"], "items": [{"id": "plus", "value": "+"}], "markers": [{"name": "top", "itemId": "plus"}], "msg": "Read '+' (operator) → push to stack."},
    {"inputCursor": 2, "outputItems": ["A", "B"], "items": [{"id": "plus", "value": "+"}], "markers": [{"name": "top", "itemId": "plus"}], "msg": "Read 'B' (operand) → append."},
    {"inputCursor": 3, "outputItems": ["A", "B"], "items": [{"id": "plus", "value": "+"}, {"id": "mul", "value": "*"}], "markers": [{"name": "top", "itemId": "mul"}], "msg": "Read '*' (higher precedence than '+') → push."},
    {"inputCursor": 4, "outputItems": ["A", "B", "C"], "items": [{"id": "plus", "value": "+"}, {"id": "mul", "value": "*"}], "markers": [{"name": "top", "itemId": "mul"}], "msg": "Read 'C' → append."},
    {"inputCursor": 5, "outputItems": ["A", "B", "C", "*"], "items": [{"id": "plus", "value": "+"}], "markers": [{"name": "top", "itemId": "plus"}], "msg": "End of input → pop '*' to output."},
    {"inputCursor": 5, "outputItems": ["A", "B", "C", "*", "+"], "items": [], "msg": "Pop '+' to output. Final postfix: A B C * +"}
  ]
}
```

## 6. Closest existing widget to mimic

Mimic
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
(784 LOC) for the stack/queue container — same horizontal cell row,
keyed-by-id transitions for items moving in and out. Reuse the
`secondaryItems` pattern as the model for the **outputStrip** (an
auxiliary row below the container with its own marker), and add a
**third** row above the container for `inputStrip`. The `markers` pattern
(named, coloured triangles) maps cleanly to top/front/back annotations.
Priority mode visually sorts items in-place each step — same `keys`
transition trick `ArrayTraversal` uses for sort animations.

## 7. D3 selections plan

- `g.stack-queue__item` keyed by `item.id`. Container layout function
  switches based on `mode`: stack stacks bottom-up (transform y by stack
  depth), queue/deque flow left-to-right, priority sorts by `priority`
  field.
- For `capacity` rendering: `g.stack-queue__slot` keyed by slot index
  (1..capacity). Unoccupied slots render with muted styling; occupied
  slots are transparent and let the item underneath show through.
- `g.stack-queue__pointer` keyed by `marker.name` — top, front, back. Same
  triangle-and-label pattern as `LinkedList` markers.
- `g.stack-queue__input-token` keyed by `${stepIdx}-${tokenIdx}` (with a
  highlight class when `inputCursor === tokenIdx`).
- `g.stack-queue__output-token` keyed by `${tokenIdx}` so output tokens
  enter from below and slide into place as the conversion progresses.
- Step changes transition item transforms (slide A from stack-pos-2 to
  the air during pop), token highlights (cursor moves), and pointer
  markers (front/back slide along the queue cells).

## 8. Shared abstractions

- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
  for parsing.
- Reuses
  `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`
  for step controls.
- New shared utility candidate: an `InputOutputStripRow` helper for the
  conversion-strip rendering — `binary-tree`'s expression-traversal mode
  and `dp-table`'s decision-recovery mode might both want it. **Defer to
  Arc 2** once duplication is visible.

## 9. Estimated build session count

**1 session** — single Scala module, mode dispatch is simple (each mode
differs in container layout + pointer set, container item handling is
shared). The expression strips are the trickiest piece visually; allocate
~30% of the session budget to those.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/05-stack/02-array-implementation-of-stacks.md`
— smallest mode-specific chapter (3 source diagrams, all in the 3-9 frame
range, pure `mode:"stack"`). No `inputStrip`/`outputStrip` complexity here
— that gets exercised once the basic container + markers land cleanly.
The queue POC follows naturally in the next session against the same
widget.
