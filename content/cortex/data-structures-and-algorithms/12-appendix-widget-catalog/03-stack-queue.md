---
title: stack-queue
summary: Stack (LIFO push/pop with top pointer) and queue (FIFO enqueue/dequeue with front/back pointers) in one widget, switched by a mode flag. Optional capacity renders muted slot backgrounds for array-backed implementations.
prereqs: []
---

# `stack-queue`

## Purpose

A single widget covers the two essential LIFO/FIFO topologies the DSA book teaches in Phase 4 (Stack) and Phase 5 (Queue). `mode:"stack"` renders items bottom-up in a vertical column with the `top` pointer to the right of the topmost cell. `mode:"queue"` renders items left-to-right in a horizontal row with `front` and `back` pointers above the relevant cells. Optional `capacity` renders the unoccupied slots as muted backgrounds so array-backed implementations show "available room" rather than "rendered cells".

`deque` and `priority` modes plus the expression-conversion input/output strips (used in the Phase 4 infix→postfix chapters) are listed in the widget spec but deferred to a follow-on iteration — neither blocks the Phase 4/5 chapter migration.

> **Source spec**: `docs/migration/widget-specs/stack-queue.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/StackQueue.scala`
>
> **Marker canon**: [`MarkerCanon.scala`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala) — `top` = emerald, `front` = blue, `back` = rose

## Payload schema (reference card)

```ts
{
  title:    string,
  mode:     "stack" | "queue",
  capacity?: number,                                 // omitted = unbounded; set = N slot backgrounds
  steps: [
    {
      op?:      "push" | "pop" | "enqueue" | "dequeue" | string,
      opValue?: string,                              // appended to the op banner
      items:    [{ id: string, value: string }],    // current contents — order matters
      markers?: [{ name: "top" | "front" | "back", itemId: string }],
      msg:      string                                // narration shown under the SVG
    }
  ]
}
```

**Required**: `title`, `mode`, `steps[].items`, `steps[].msg`.
**Optional**: `capacity`, `steps[].op`, `steps[].opValue`, `steps[].markers`.

**Item ordering convention**:
- `mode:"stack"` — `items[0]` is the bottom of the stack (oldest), `items.last` is the top (newest). A push appends to the end.
- `mode:"queue"` — `items[0]` is the front (oldest, next to dequeue), `items.last` is the back (newest, just enqueued). An enqueue appends to the end; a dequeue drops `items.head`.

**Marker rule (carry-forward)**: every step that has at least one item should enumerate the pointers it wants shown. A marker whose `itemId` is not in the step's items is skipped silently (no "into the void" arrow). Author-supplied `color` fields on markers are dropped at parse time — colours come from [`MarkerCanon`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala) so `top`/`front`/`back` carry the same hex wherever they appear in the book.

## Representative payloads

### Payload 1 — minimum (stack push×3, pop×1)

Smallest meaningful sequence — three pushes then a pop. Exercises the keyed-item transition (`A`, `B`, `C` slide into stack slots; `C` fades out on pop). The `top` pointer moves with each step.

```d3 widget=stack-queue
{
  "title": "Stack — push A, B, C then pop",
  "mode": "stack",
  "capacity": 5,
  "steps": [
    {"op": "push", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "top", "itemId": "a"}], "msg": "Push A onto an empty stack. top = A."},
    {"op": "push", "opValue": "B", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "Push B. top = B."},
    {"op": "push", "opValue": "C", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "top", "itemId": "c"}], "msg": "Push C. top = C."},
    {"op": "pop", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "Pop. Returns C, top = B."}
  ]
}
```

### Payload 2 — typical (stack as a call-stack analogue)

Tracks the lifecycle of three nested function calls — `main()` invokes `foo()` which invokes `bar()`, each pushing its frame onto the stack; then bar returns, foo returns, main returns, each popping its frame. Mirrors the source diagram in [02-algorithms/01-recursion/02-nested-functions/02-understanding-nested-functions.md:126](../../../../content/cortex/data-structures-and-algorithms/02-algorithms/01-recursion/02-nested-functions/02-understanding-nested-functions.md) (13 frames → 7 steps).

```d3 widget=stack-queue
{
  "title": "Call stack — nested invocations of main → foo → bar",
  "mode": "stack",
  "capacity": 4,
  "steps": [
    {"op": "push", "opValue": "main()", "items": [{"id": "m", "value": "main"}], "markers": [{"name": "top", "itemId": "m"}], "msg": "main() begins. Frame pushed."},
    {"op": "push", "opValue": "foo()", "items": [{"id": "m", "value": "main"}, {"id": "f", "value": "foo"}], "markers": [{"name": "top", "itemId": "f"}], "msg": "main() calls foo(). Frame for foo pushed on top."},
    {"op": "push", "opValue": "bar()", "items": [{"id": "m", "value": "main"}, {"id": "f", "value": "foo"}, {"id": "b", "value": "bar"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "foo() calls bar(). Frame for bar pushed on top."},
    {"items": [{"id": "m", "value": "main"}, {"id": "f", "value": "foo"}, {"id": "b", "value": "bar"}], "markers": [{"name": "top", "itemId": "b"}], "msg": "bar() executes — its frame is the active one."},
    {"op": "pop", "items": [{"id": "m", "value": "main"}, {"id": "f", "value": "foo"}], "markers": [{"name": "top", "itemId": "f"}], "msg": "bar() returns. Its frame is popped. foo() resumes."},
    {"op": "pop", "items": [{"id": "m", "value": "main"}], "markers": [{"name": "top", "itemId": "m"}], "msg": "foo() returns. Its frame is popped. main() resumes."},
    {"op": "pop", "items": [], "msg": "main() returns. Call stack empty — program ends."}
  ]
}
```

### Payload 3 — typical (queue enqueue×3, dequeue×2)

Standard FIFO sequence. Three enqueues fill the queue from the back; two dequeues drain from the front. Exercises `front` and `back` pointer carry-forward — they're at the same item when the queue holds one element, then diverge as more enter.

```d3 widget=stack-queue
{
  "title": "Queue — enqueue A, B, C then dequeue twice",
  "mode": "queue",
  "capacity": 5,
  "steps": [
    {"op": "enqueue", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "a"}], "msg": "Enqueue A. front = back = A (single item)."},
    {"op": "enqueue", "opValue": "B", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "b"}], "msg": "Enqueue B at the back. front = A, back = B."},
    {"op": "enqueue", "opValue": "C", "items": [{"id": "a", "value": "A"}, {"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "a"}, {"name": "back", "itemId": "c"}], "msg": "Enqueue C at the back. front = A, back = C."},
    {"op": "dequeue", "items": [{"id": "b", "value": "B"}, {"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "b"}, {"name": "back", "itemId": "c"}], "msg": "Dequeue. Returns A. front = B."},
    {"op": "dequeue", "items": [{"id": "c", "value": "C"}], "markers": [{"name": "front", "itemId": "c"}, {"name": "back", "itemId": "c"}], "msg": "Dequeue. Returns B. front = back = C."}
  ]
}
```

### Payload 4 — edge case (empty-stack pop banner)

The empty state is a real teaching moment — what happens when you pop an empty stack. The widget renders the `(empty)` placeholder, the `op` banner stays visible, and the message narrates the error. No `top` pointer is rendered because there's nothing to point at — the carry-forward rule explicitly drops markers whose `itemId` is not present in the step.

```d3 widget=stack-queue
{
  "title": "Edge case — pop from an empty stack",
  "mode": "stack",
  "capacity": 3,
  "steps": [
    {"items": [], "msg": "Stack starts empty."},
    {"op": "push", "opValue": "A", "items": [{"id": "a", "value": "A"}], "markers": [{"name": "top", "itemId": "a"}], "msg": "Push A. top = A."},
    {"op": "pop", "items": [], "msg": "Pop. Returns A. Stack is empty again."},
    {"op": "pop", "items": [], "msg": "Pop on empty stack — implementations typically raise StackEmptyException."}
  ]
}
```

### Payload 5 — large-N stress (10-item bounded queue, partial fill cycles)

Stress-tests the layout on a longer container — 10 capacity slots, items churning through enqueue and dequeue. Exercises pointer movement across more cells and confirms cells stay visually contained in the prose column.

```d3 widget=stack-queue
{
  "title": "10-slot queue — enqueue heavily then drain",
  "mode": "queue",
  "capacity": 10,
  "steps": [
    {"op": "enqueue", "opValue": "A1", "items": [{"id": "a1", "value": "A1"}], "markers": [{"name": "front", "itemId": "a1"}, {"name": "back", "itemId": "a1"}], "msg": "Enqueue A1."},
    {"op": "enqueue", "opValue": "A2", "items": [{"id": "a1", "value": "A1"}, {"id": "a2", "value": "A2"}], "markers": [{"name": "front", "itemId": "a1"}, {"name": "back", "itemId": "a2"}], "msg": "Enqueue A2."},
    {"op": "enqueue", "opValue": "A3", "items": [{"id": "a1", "value": "A1"}, {"id": "a2", "value": "A2"}, {"id": "a3", "value": "A3"}], "markers": [{"name": "front", "itemId": "a1"}, {"name": "back", "itemId": "a3"}], "msg": "Enqueue A3."},
    {"op": "enqueue", "opValue": "A4", "items": [{"id": "a1", "value": "A1"}, {"id": "a2", "value": "A2"}, {"id": "a3", "value": "A3"}, {"id": "a4", "value": "A4"}], "markers": [{"name": "front", "itemId": "a1"}, {"name": "back", "itemId": "a4"}], "msg": "Enqueue A4. Four items, six slots free."},
    {"op": "enqueue", "opValue": "A5", "items": [{"id": "a1", "value": "A1"}, {"id": "a2", "value": "A2"}, {"id": "a3", "value": "A3"}, {"id": "a4", "value": "A4"}, {"id": "a5", "value": "A5"}], "markers": [{"name": "front", "itemId": "a1"}, {"name": "back", "itemId": "a5"}], "msg": "Enqueue A5. Halfway full."},
    {"op": "dequeue", "items": [{"id": "a2", "value": "A2"}, {"id": "a3", "value": "A3"}, {"id": "a4", "value": "A4"}, {"id": "a5", "value": "A5"}], "markers": [{"name": "front", "itemId": "a2"}, {"name": "back", "itemId": "a5"}], "msg": "Dequeue. Returns A1. front = A2."},
    {"op": "dequeue", "items": [{"id": "a3", "value": "A3"}, {"id": "a4", "value": "A4"}, {"id": "a5", "value": "A5"}], "markers": [{"name": "front", "itemId": "a3"}, {"name": "back", "itemId": "a5"}], "msg": "Dequeue. Returns A2. Items slide forward."},
    {"op": "enqueue", "opValue": "A6", "items": [{"id": "a3", "value": "A3"}, {"id": "a4", "value": "A4"}, {"id": "a5", "value": "A5"}, {"id": "a6", "value": "A6"}], "markers": [{"name": "front", "itemId": "a3"}, {"name": "back", "itemId": "a6"}], "msg": "Enqueue A6. back = A6."},
    {"op": "dequeue", "items": [{"id": "a4", "value": "A4"}, {"id": "a5", "value": "A5"}, {"id": "a6", "value": "A6"}], "markers": [{"name": "front", "itemId": "a4"}, {"name": "back", "itemId": "a6"}], "msg": "Dequeue. Returns A3. Two items dequeued, two slots in use."}
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload for this widget, target **3–8 widget steps** (most chapters land at 4–6). Compression strategy:

- **Frames 1–3 of source** (intro: empty stack, label appears, op announced) → typically merge into one "initial state" step or one "first push" step.
- **Each push / pop / enqueue / dequeue in source** → one widget step. The item arriving and the marker moving happen in the same step; the widget animates both via D3 transitions.
- **Frames showing the same state with different visual emphasis** (e.g. arrow re-drawn, label re-coloured) → merge into one step. The widget's marker carry-forward handles "the pointer is still here" without a payload entry.
- **Final 1–2 frames of source** (result + footer) → merge into one "result" step with the outcome in the `msg`.

Example: source 13-frame call-stack diagram → 7 widget steps (one per function-call lifecycle event). The 13 → 7 compression is documented inline in the Payload 2 above.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-stack-queue` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm marker colours: `top` is emerald, `front` is blue, `back` is rose — same hex wherever they appear, every time.
3. Confirm pointers carry through visually: `top` stays attached to whatever's at the top of the stack as it changes; `front`/`back` track the queue's leading and trailing items respectively. The empty-stack-pop edge case correctly hides the marker when there's no item to attach to.
4. Confirm no `.d3-widget__error` divs render.
5. Confirm devtools console is clean (no widget exceptions; no canonical-vocabulary warnings on these payloads — every marker name is in the canon).

If any payload fails, fix and re-verify before committing.
