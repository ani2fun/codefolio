---
title: call-stack
summary: Function-call timeline rendered two ways from the same event tape — `stack` mode stacks activation frames bottom-up with a ▶ pointer on the active frame; `recursion-tree` mode lays the call graph out top-down with sibling spread and persistent return-value labels on each child→parent edge. Optional stackLimit + overflowAt paint a "stack memory" chrome with a red overflow band. One step per `call` / `return` / `update` / `pause` event; per-step frame status (active / waiting / returning / popped / overflow) is computed from the live stack, not author-supplied.
prereqs: []
---

# `call-stack`

## Purpose

A single widget covers source Phase 10 (Recursion — 10 source diagrams across two chapters) plus the orphan-style recursion-tree variants Phase 10 chapters 04–07 each need. The data model is an event-based tape: a list of `frameDefs` (definitions of every frame that may be pushed at any point) + a list of `events` (a tape of `call` / `return` / `update` / `pause` operations). The renderer replays events to compute per-step state and projects to one of two layouts.

Two modes share one renderer through a `mode` flag:

- `mode: "stack"`           — the canonical bottom-up activation-frame ladder. New frames push on top; returns pop. Each frame shows its function name, any locals, and (on return) the value handed back to the caller. A stack pointer arrow (▶) sits to the right of the active frame. The mode badge tints blue. Optional `stackLimit` + `overflowAt` paint a "stack memory" chrome with a red overflow band — the canonical visualisation for the source's stack-overflow chapter.
- `mode: "recursion-tree"`  — the SAME call graph rendered as a top-down tree. Children spread horizontally at the same depth via a DFS sibling-spread layout. Return values appear on the edge from child → parent and **stay drawn** once produced — by the end of a trace the reader sees the complete return graph. Popped frames remain visible (dimmed `popped` style) so the structure of the decomposition is preserved as a whole. The mode badge tints violet.

The underlying `frameDefs + events` data is **identical** across modes — only the layout projection differs. Step semantics: **each event emits one widget step** with its own `msg`. `update` events change locals on a frame without push/pop (for "show local update" beats). `pause` events emit a step beat with no state change (useful for narrative pauses).

Frame status is **computed**, not author-supplied:

- `active`    — top of the live stack (currently executing). Emerald accent.
- `waiting`   — paused, waiting for a recursive call to return. Slate accent.
- `returning` — most recent return this step (highlighted as it pops; in tree mode it stays highlighted with the return-value label tinted fresh-green for one step before fading to the persistent violet). Violet accent + dashed border in stack mode.
- `popped`    — drawn in recursion-tree mode after return, dimmed in slate. Removed entirely in stack mode (the ghost frame appears for one return-step then exits).
- `overflow`  — stack mode only; frames at depth ≥ `overflowAt` paint red. The "stack memory" chrome also draws a faint red band behind the overflow zone so the danger reads even before a frame arrives there.

> **Source spec**: `docs/migration/widget-specs/call-stack.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/CallStack.scala`
>
> **Event-tape model**: the renderer's per-step state is computed by folding `applyEvent` over `events[0..stepIdx]`. Stack mode shows the live stack PLUS a "ghost return" frame for one step when the current event is a return (drawn in the slot the frame just vacated). Tree mode keeps every called frame visible and records return values cumulatively.

## Payload schema (reference card)

```ts
{
  title:       string,
  mode:        "stack" | "recursion-tree",
  stackLimit?: number,                      // visible slot count for the chrome
  overflowAt?: number,                      // depth (0-indexed) where frames go red
  frameDefs: [{
    id:      string,                        // unique within the spec
    name:    string,                        // display label, e.g. "sum(3)"
    locals?: [{ name: string, value: string }],
    parent?: string                         // recursion-tree mode only — defines tree edges
  }],
  events: [
    // call   — push frameId onto the stack
    { kind: "call",   frameId: string, msg: string },
    // return — pop the top frame (must match frameId); value flows to the caller
    { kind: "return", frameId: string, value?: string, msg: string },
    // update — replace locals on frameId without push/pop
    { kind: "update", frameId: string, locals: [{ name: string, value: string }], msg: string },
    // pause  — emit a step beat with no state change
    { kind: "pause",  msg: string }
  ]
}
```

**Required**: `title`, `mode`, `events` (non-empty), `events[].msg`, `events[].kind` (closed set).
**Optional**: `stackLimit`, `overflowAt`, `frameDefs` (defaults to empty — but you'll want it for any meaningful spec), per-frame `locals` and `parent`, per-event `frameId` (required for call/return/update; ignored on pause), per-event `value` (return only), per-event `locals` (update only).

**Mode-specific rules**:

- In `stack` mode, the `parent` field on each frame is ignored — the layout is purely the live stack.
- In `recursion-tree` mode, frames without a `parent` are tree roots (typically one root per spec — the top-level call). Children render in their **declaration order** in `frameDefs`, so the author controls left/right ordering of recursive subcalls (e.g. `fib-3` before `fib-2` in `fib-4`'s children).
- `stackLimit` and `overflowAt` only render visibly in `stack` mode.

**Event-replay rule**: events apply in order. A `return` on a non-top frame leaves the stack alone (defensive — a malformed payload won't crash the renderer). A `call` for a frameId not in `frameDefs` still pushes (the renderer falls back to using the id as the display name) — but for any production payload, list every frame in `frameDefs` so the layout is stable.

**Frame status is computed** — you don't supply `active` / `waiting` / `returning` in the payload. The renderer derives them from `(stack, ghostReturnFrame, calledFrames)`.

**Locals on update**: an `update` event REPLACES the frame's locals list (not a sparse merge). To express "no local change", just don't emit an `update` event for that step. Use a `pause` event instead if you want a narrative beat without changing state.

## Representative payloads

### Payload 1 — minimum (single call, return, empty stack)

The smallest meaningful payload — `main()` pushed, then returned. Exercises the renderer's empty-state path (empty stack between calls), the single-frame stack, and the ghost-return on the final step.

```d3 widget=call-stack
{
  "title": "main() — push and return",
  "mode": "stack",
  "frameDefs": [
    {"id": "main", "name": "main()", "locals": [{"name": "argc", "value": "1"}]}
  ],
  "events": [
    {"kind": "pause",  "msg": "Before the program starts: the call stack is empty."},
    {"kind": "call",   "frameId": "main", "msg": "Push main() — execution begins."},
    {"kind": "return", "frameId": "main", "value": "0", "msg": "main() returns 0 — program exits."}
  ]
}
```

### Payload 2 — typical (recursive sum(3) push then unwind)

Classic head-recursive `sum(n) = n + sum(n-1)`. Three pushes, three pops (with return values bubbling up). Exercises:

- The id-keyed frame slide: `sum-2` and `sum-3` stay at their slots while `sum-1` pushes on top and pops back off; their `transform` doesn't change so they don't visibly move.
- The ghost-return frame: on each return event, the just-popped frame stays drawn for one step with the `returning` modifier (dashed violet outline) showing `→ <value>` on its right; the next step removes it.
- The pointer (▶) sliding to track the active frame after each push and after each pop.
- Locals rendered as `name = value` rows inside the frame box (each `sum-N` shows `n = N`).
- Return values cascading: `sum-1 = 1`, `sum-2 = 3`, `sum-3 = 6`.

```d3 widget=call-stack
{
  "title": "sum(3) push and pop",
  "mode": "stack",
  "frameDefs": [
    {"id": "sum-3", "name": "sum(3)", "locals": [{"name": "n", "value": "3"}]},
    {"id": "sum-2", "name": "sum(2)", "locals": [{"name": "n", "value": "2"}]},
    {"id": "sum-1", "name": "sum(1)", "locals": [{"name": "n", "value": "1"}]}
  ],
  "events": [
    {"kind": "call",   "frameId": "sum-3", "msg": "Push sum(3). The recursion starts."},
    {"kind": "call",   "frameId": "sum-2", "msg": "Recurse: push sum(2). sum-3 waits."},
    {"kind": "call",   "frameId": "sum-1", "msg": "Recurse: push sum(1). sum-2 waits."},
    {"kind": "return", "frameId": "sum-1", "value": "1", "msg": "Base case: sum(1) = 1. Pop."},
    {"kind": "return", "frameId": "sum-2", "value": "3", "msg": "sum(2) = 2 + 1 = 3. Pop."},
    {"kind": "return", "frameId": "sum-3", "value": "6", "msg": "sum(3) = 3 + 3 = 6. Pop. Stack empty."}
  ]
}
```

### Payload 3 — overflow (stackLimit + overflowAt, frames go red at the boundary)

Unbounded recursion (no base case) bumps into the `stackLimit: 5` ceiling at `overflowAt: 5`. The "stack memory" chrome draws the visible 5-slot box; the overflow band is a faint red wash behind the topmost slot; the 5th frame paints red when it lands. Exercises:

- The `stackLimit` chrome rendering (dashed slate outer rect).
- The `overflowAt` red band always-on behind the upper slot range (the band covers exactly the depth-≥-overflowAt region), signalling "this region is dangerous" even before a frame arrives.
- The `overflow` modifier on the offending frame — red rect, red stroke, red text. With `overflowAt: 5`, only the frame at depth 5 (the 5th push, slot 4) paints red.
- A pause event at the end that holds the overflowed state on screen without further push/pop.

```d3 widget=call-stack
{
  "title": "Unbounded recursion → stack overflow at depth 5",
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
    {"kind": "call",  "frameId": "f1", "msg": "Push f(1). Depth = 1 — well below the limit."},
    {"kind": "call",  "frameId": "f2", "msg": "Recurse: push f(2). Depth = 2."},
    {"kind": "call",  "frameId": "f3", "msg": "Recurse: push f(3). Depth = 3."},
    {"kind": "call",  "frameId": "f4", "msg": "Recurse: push f(4). Depth = 4 — one frame from the limit."},
    {"kind": "call",  "frameId": "f5", "msg": "Push f(5). Depth = 5 ≥ overflowAt. STACK OVERFLOW."},
    {"kind": "pause", "msg": "No base case. The recursion would continue; the runtime aborts."}
  ]
}
```

### Payload 4 — recursion-tree mode (fib(4) with persistent return-value labels)

The same head-recursion idea projected as a tree. `fib(4)` decomposes into `fib(3) + fib(2)`; each subcall decomposes further. Nine frames; eighteen events (nine calls, nine returns) on the canonical post-order trace. Exercises:

- The DFS sibling-spread layout: `fib-4`'s two children (`fib-3a` left, `fib-2a` right) spread horizontally; `fib-3a`'s subtree finishes before `fib-2a` starts.
- The `active` modifier (emerald) on the top-of-stack frame as the trace walks the tree.
- The `waiting` modifier (blue) on frames that are on the stack but not active (their child is being computed).
- The `popped` modifier (dimmed slate) on frames that have returned — they STAY drawn.
- Return-value labels persist on the edge from child → parent. On the step a frame returns, its label tints **fresh-green**; on the next step it settles to the canonical **violet**. By step 18 every edge has a label and the reader sees `fib(4) = fib(3) + fib(2) = 2 + 1 = 3` reconstructed visually.

```d3 widget=call-stack
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
    {"kind": "call",   "frameId": "fib-4",  "msg": "fib(4) — top-level call."},
    {"kind": "call",   "frameId": "fib-3a", "msg": "Recurse left: fib(3)."},
    {"kind": "call",   "frameId": "fib-2b", "msg": "Recurse left: fib(2) (child of fib-3a)."},
    {"kind": "call",   "frameId": "fib-1b", "msg": "Recurse left: fib(1)."},
    {"kind": "return", "frameId": "fib-1b", "value": "1", "msg": "fib(1) = 1. Pop."},
    {"kind": "call",   "frameId": "fib-0a", "msg": "Recurse right: fib(0)."},
    {"kind": "return", "frameId": "fib-0a", "value": "0", "msg": "fib(0) = 0. Pop."},
    {"kind": "return", "frameId": "fib-2b", "value": "1", "msg": "fib(2) = 1 + 0 = 1. Pop."},
    {"kind": "call",   "frameId": "fib-1a", "msg": "Recurse right: fib(1) (sibling of fib-2b)."},
    {"kind": "return", "frameId": "fib-1a", "value": "1", "msg": "fib(1) = 1. Pop."},
    {"kind": "return", "frameId": "fib-3a", "value": "2", "msg": "fib(3) = 1 + 1 = 2. Pop."},
    {"kind": "call",   "frameId": "fib-2a", "msg": "Recurse right of fib-4: fib(2)."},
    {"kind": "call",   "frameId": "fib-1c", "msg": "Recurse left: fib(1)."},
    {"kind": "return", "frameId": "fib-1c", "value": "1", "msg": "fib(1) = 1. Pop."},
    {"kind": "call",   "frameId": "fib-0b", "msg": "Recurse right: fib(0)."},
    {"kind": "return", "frameId": "fib-0b", "value": "0", "msg": "fib(0) = 0. Pop."},
    {"kind": "return", "frameId": "fib-2a", "value": "1", "msg": "fib(2) = 1 + 0 = 1. Pop."},
    {"kind": "return", "frameId": "fib-4",  "value": "3", "msg": "fib(4) = 2 + 1 = 3. Pop. Done."}
  ]
}
```

### Payload 5 — update events (locals mutate without push/pop; mixed with pause)

`main()` calls `greet("Ada")`. The `greet` frame initialises `msg = ""`, then `update`-events mutate `msg` to `"hi Ada"` mid-frame, then it returns. Exercises:

- `update` events: locals refresh on the same frame, but the stack structure doesn't change (no slide, no enter / exit on the frame group). The reader sees the locals text rebind.
- `pause` events between updates: a step beat with no state change at all — only the caption rotates. Useful for "let me explain what's about to happen" narration.
- A mix of stack mode mechanics (push, locals init, locals mutate via update, locals mutate again, return with value, return again to empty).

```d3 widget=call-stack
{
  "title": "main → greet(\"Ada\") with local mutation",
  "mode": "stack",
  "frameDefs": [
    {"id": "main",  "name": "main()", "locals": [{"name": "exit", "value": "0"}]},
    {
      "id":     "greet",
      "name":   "greet(\"Ada\")",
      "locals": [{"name": "name", "value": "\"Ada\""}, {"name": "msg", "value": "\"\""}]
    }
  ],
  "events": [
    {"kind": "call",   "frameId": "main",  "msg": "Push main(). exit = 0."},
    {"kind": "call",   "frameId": "greet", "msg": "Push greet(\"Ada\"). Locals: name = \"Ada\", msg = \"\"."},
    {"kind": "pause",  "msg": "Inside greet: about to compute the message."},
    {
      "kind":    "update",
      "frameId": "greet",
      "locals":  [{"name": "name", "value": "\"Ada\""}, {"name": "msg", "value": "\"hi \""}],
      "msg":     "Assign msg = \"hi \". Watch greet's locals rebind in place."
    },
    {
      "kind":    "update",
      "frameId": "greet",
      "locals":  [{"name": "name", "value": "\"Ada\""}, {"name": "msg", "value": "\"hi Ada\""}],
      "msg":     "Concatenate name onto msg. msg = \"hi Ada\"."
    },
    {"kind": "return", "frameId": "greet", "value": "\"hi Ada\"", "msg": "greet returns \"hi Ada\". Pop."},
    {"kind": "return", "frameId": "main",  "value": "0", "msg": "main returns 0. Exit."}
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` for this widget, target **6–18 widget steps** (the source diagrams average 4–39 frames; the longest single source diagram is `choose(5, 3)` at 39 frames → ~14 events). Compression strategy:

- **Per-frame fade-in narration** ("the frame appears at the top of the stack") → fold into the `call` event's `msg`. The renderer already fades the frame on enter; you don't need a separate beat.
- **Compute steps inside a frame** (intermediate arithmetic before the return) → use `update` events that mutate locals to surface the intermediate state, or fold them into one `pause` step + one `return` step with the value. Don't emit one event per machine instruction — the source isn't that fine-grained either.
- **Multi-level recursion unwind** (the most common pattern) → one `call` per recursive descent, one `return` per ascent. A `sum(5)` example produces 5 calls + 5 returns = 10 events. That's the canonical compression.
- **Stack-overflow chapter** → one `call` per frame up to the limit, then one `pause` at the boundary to hold the overflowed state on screen. Don't extend past the limit; the visualisation's job is to show the boundary being hit, not the post-abort behaviour.
- **Recursion-tree mode for the same trace** → the event count is identical (calls + returns in DFS order); only the layout differs. If you've authored a stack-mode payload for a trace, you can convert it to recursion-tree by adding `parent` to each frameDef and switching `mode`. The events stay byte-for-byte.

The `choose(5, 3)` source diagram (39 frames in the source) compresses to about 14 events in recursion-tree mode — one per `(call, return)` pair across the 7 visible frames. The spec's POC 5.4 payload uses exactly this compression as a reference.

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-call-stack` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm Payload 1 renders the empty-state pause step (no frames visible), then a single `main()` frame on step 2, then the same frame painted with the dashed-violet `returning` modifier on step 3 showing `→ 0` on the right. Step 3 is the last step; the next click of Next is disabled.
3. Confirm Payload 2's three frames stack bottom-up across steps 1–3 (`sum-3` at the bottom, `sum-1` on top by step 3). The pointer (▶) sits to the right of the active frame and visibly slides up after each push, down after each pop. At step 4 (`return sum-1, value 1`), `sum-1` stays drawn briefly with `→ 1` on its right and a dashed violet outline; at step 5 it's gone and `sum-2` is the new ghost with `→ 3`. By step 6 only `sum-3` (as ghost) is visible with `→ 6`.
4. Confirm Payload 3's stack-memory chrome (dashed slate outer rect) and the faint red overflow band are visible from step 1 onward (chrome doesn't depend on stack state). The band spans the topmost slot (depth ≥ 5 region). Frames f1–f4 paint normally (depth < 5 stays under the threshold); f5 paints with the `overflow` modifier (red rect + red text) at step 5 because depth 5 meets `overflowAt: 5`. The final `pause` step (step 6) holds the overflowed state without further push/pop — the caption changes but no frames slide.
5. Confirm Payload 4's recursion tree renders with all 9 frames visible from step 1 (every frame in `frameDefs` shows up; un-called frames render in the dashed-slate `pending` style). As the trace walks the tree, frames light up in emerald (`active`) / blue (`waiting`) / dimmed-slate (`popped`). At each `return` event the returning frame's parent edge gets a label tinted fresh-green for that step; on the next step the label settles to violet and persists. By the final step every edge has a label and the root reads `→ 3` on its incoming edge... well, the root has no parent so the root's return value (3) appears only as the frame's status (`returning` → `popped` after step 18). Sibling ordering: `fib-3a` (left) and `fib-2a` (right) under `fib-4`, declared in that order.
6. Confirm Payload 5's `update` events at steps 4 and 5 mutate the `msg` local inside `greet`'s frame box without moving the frame itself — the frame's `transform` shouldn't change, only the second locals row's text. Pause step 3 changes only the caption; the frame state is identical to step 2.
7. Confirm no `.d3-widget__error` divs render in the page.
8. Confirm devtools console is clean: no widget exceptions; no `call-stack:` marker-canon warnings on these payloads (none of them use payload-supplied markers — frame status is computed).

If any payload fails, fix and re-verify before committing.
