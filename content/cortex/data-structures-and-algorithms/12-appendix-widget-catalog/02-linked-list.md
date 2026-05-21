---
title: linked-list
summary: Singly or doubly linked chain of value-bearing nodes connected by `next` (and auto-derived `prev`) arrows, with per-step markers (head, current, previous, next, slow, fast, headA/B/C, …) attached to specific nodes. Optional cycle back-edge, row wrapping for long chains, sectioned progress bar for multi-phase walks, and per-node styles (new / removed / highlight).
prereqs: []
---

# `linked-list`

## Purpose

The second widget in the D3 catalog, and the workhorse for Phase 1
(Singly Linked List) and Phase 2 (Doubly Linked List) of the source
DSA book. `direction:"single"` (default) renders a single row of nodes
with `next` arrows between them. `direction:"double"` renders the same
row but adds a dashed `prev` arrow under each forward link —
auto-derived from the forward links unless the author overrides
per-link `kind:"prev"`. The two modes share one schema; a chapter
switches modes by flipping a single flag.

Three optional axes layer on top of the base topology:

- **`cycleTarget`** — draws a cubic-Bezier back-edge from the tail
  node's `next` to the targeted node id. Used for Floyd's algorithm
  and any list-with-cycle chapter.
- **`sections`** — partitions the progress bar into named macro-phases
  with dividers and labels. Used for multi-stage walks like Reorder
  (Split → Reverse → Merge) where the step count is high enough that
  a flat progress bar loses the macro shape.
- **`wrapAt`** — wraps long node chains across multiple rows every N
  nodes, with diagonal cross-row arrows. Used when a chain exceeds
  what fits in the prose column on one row.

Per-node `style:"new" | "removed" | "highlight"` adds visual emphasis
for nodes being inserted, deleted, or highlighted in the current step.

> **Source spec**: `docs/migration/widget-specs/linked-list.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala`
>
> **Marker canon**: [`MarkerCanon.scala`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala)
> — `head`/`slow` = blue; `previous`/`start` = amber;
> `current`/`end` = emerald; `next` = violet; `fast` = rose;
> `tail`/`dummy` = slate; multi-list `headA`/`headB`/`headC` =
> blue / cyan / violet with `tailA`/`tailB`/`tailC` in slate variants.

## Payload schema (reference card)

```ts
{
  title?:       string,
  direction?:   "single" | "double",          // default "single"
  nodes:        Array<{
                  id: string,
                  value: string,
                  style?: "new" | "removed" | "highlight"
                }>,
  head?:        string,                       // node id; defaults to nodes[0].id
  cycleTarget?: string,                       // node id; draws back-edge from tail
  wrapAt?:      number,                       // wrap to a new row every N nodes
  sections?:    Array<{ name: string, startIdx: number }>,
  steps: Array<{
    nodes?:   Array<{ id, value, style }>,    // optional per-step override
    links:    Array<                          // arrows for this step
                | [string, string]            // shorthand: [from, to] → next
                | { from: string, to: string, kind?: "next" | "prev" | "broken" }
              >,
    markers:  Array<{
                name: "head" | "tail" | "previous" | "current" | "next" |
                      "slow" | "fast" | "dummy" | "start" | "end" |
                      "headA" | "headB" | "headC" |
                      "tailA" | "tailB" | "tailC",
                nodeId: string
              }>,
    head?:    string,                         // per-step head override (reversal)
    msg:      string
  }>
}
```

**Required**: `nodes`, `steps[].links`, `steps[].markers`,
`steps[].msg`.
**Optional**: `title`, `direction`, `head`, `cycleTarget`, `wrapAt`,
`sections`, per-step `nodes` and `head` overrides.

**Marker rule (canon-only)**: every step enumerates the markers it
wants shown. A marker whose `nodeId` is not in the step's `nodes`
(spec-level or per-step override) is skipped silently. Author-supplied
`color` fields on markers are dropped at parse time — colours come
from [`MarkerCanon`](../../../../client/src/main/scala/codefolio/client/components/cortex/widgets/MarkerCanon.scala).
Non-canonical names render as inline warning badges (`⚠ name` in
rose) so typos are obvious.

**Link rule (auto-derived prev for doubly)**: when
`direction:"double"` and no `kind:"prev"` link appears in a step, the
renderer auto-derives one back-edge per forward link by reversing it.
Authors usually only need to list the forward links; the dashed
back-row arrows appear for free.

## Representative payloads

### Payload 1 — minimum (singly, `current` walks 4 nodes)

Smallest meaningful sequence — single marker (`current`) walks
left-to-right through a 4-node singly linked list, then advances off
the end to `null`. Exercises the basic chain layout, the marker lane,
and the canon's `head` (blue) + `current` (emerald) pairing.

```d3 widget=linked-list
{
  "title": "Singly linked list — current walks 4 nodes",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "10"},
    {"id": "n2", "value": "20"},
    {"id": "n3", "value": "30"},
    {"id": "n4", "value": "40"}
  ],
  "head": "n1",
  "steps": [
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}, {"name": "current", "nodeId": "n1"}],
      "msg": "current = head = node(10)."
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}, {"name": "current", "nodeId": "n2"}],
      "msg": "current = current.next → node(20)."
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}, {"name": "current", "nodeId": "n3"}],
      "msg": "current = current.next → node(30)."
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}, {"name": "current", "nodeId": "n4"}],
      "msg": "current = current.next → node(40). Last node reached."
    },
    {
      "links": [["n1","n2"],["n2","n3"],["n3","n4"]],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "current = current.next → null. Traversal ends."
    }
  ]
}
```

### Payload 2 — typical (singly insert with previous/current/next rewire)

Classic three-pointer splice — walk to the insertion site with
`current`, save `current.next` as `next`, allocate the new node,
rewire `current.next` and `newNode.next`. The new node uses
`style:"new"` (emerald outline, fade-in) so it visibly enters the
chain rather than appearing without ceremony. Per-step `nodes`
override controls when the new node appears — it's absent until the
allocation step.

```d3 widget=linked-list
{
  "title": "Insert node 25 between 20 and 30 — three-pointer rewire",
  "direction": "single",
  "nodes": [
    {"id": "n10", "value": "10"},
    {"id": "n20", "value": "20"},
    {"id": "n25", "value": "25", "style": "new"},
    {"id": "n30", "value": "30"},
    {"id": "n40", "value": "40"}
  ],
  "head": "n10",
  "steps": [
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n30"],["n30","n40"]],
      "markers": [{"name": "head", "nodeId": "n10"}, {"name": "current", "nodeId": "n20"}],
      "msg": "Walked to the insertion site: current = node(20)."
    },
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n30"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Save next = current.next = node(30) — we'll need it after the rewire."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Allocate node(25); rewire current.next = node(25). The 25→30 link is missing — node(30) is briefly orphaned."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n25","n30"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Rewire node(25).next = next. Splice complete."
    },
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n25", "value": "25"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n25"],["n25","n30"],["n30","n40"]],
      "markers": [{"name": "head", "nodeId": "n10"}],
      "msg": "Final list: 10 → 20 → 25 → 30 → 40."
    }
  ]
}
```

### Payload 3 — typical (doubly insert exercises the back-edge lane)

The same insert-25-after-20 narrative as Payload 2, but in
`direction:"double"`. The visible difference: a dashed `prev` arrow
appears under every forward arrow, auto-derived by the widget — the
payload's `links` list still only enumerates the forward edges. The
splice steps are identical to Payload 2 in code; the renderer is
doing the back-row work for free.

```d3 widget=linked-list
{
  "title": "Doubly linked list — insert node 25 between 20 and 30",
  "direction": "double",
  "nodes": [
    {"id": "n10", "value": "10"},
    {"id": "n20", "value": "20"},
    {"id": "n25", "value": "25", "style": "new"},
    {"id": "n30", "value": "30"},
    {"id": "n40", "value": "40"}
  ],
  "head": "n10",
  "steps": [
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n30"],["n30","n40"]],
      "markers": [{"name": "head", "nodeId": "n10"}, {"name": "current", "nodeId": "n20"}],
      "msg": "current = node(20). The dashed back-row arrows are auto-derived from each forward link."
    },
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n30"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Capture next = current.next = node(30)."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Rewire current.next = node(25). Auto-derived: node(25).prev = current."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n25","n30"],["n30","n40"]],
      "markers": [
        {"name": "head", "nodeId": "n10"},
        {"name": "current", "nodeId": "n20"},
        {"name": "next", "nodeId": "n30"}
      ],
      "msg": "Rewire node(25).next = next; auto-derived next.prev = node(25). Splice done."
    },
    {
      "nodes": [
        {"id": "n10", "value": "10"},
        {"id": "n20", "value": "20"},
        {"id": "n25", "value": "25"},
        {"id": "n30", "value": "30"},
        {"id": "n40", "value": "40"}
      ],
      "links": [["n10","n20"],["n20","n25"],["n25","n30"],["n30","n40"]],
      "markers": [{"name": "head", "nodeId": "n10"}],
      "msg": "Final: 10 ↔ 20 ↔ 25 ↔ 30 ↔ 40."
    }
  ]
}
```

### Payload 4 — cycle detection (Floyd's, back-edge stress)

Floyd's tortoise-and-hare on a list with a cycle of length 4. The
`cycleTarget` field draws a cubic-Bezier back-edge from the tail
(node(7)) to node(4). `slow` (blue) advances one node per step;
`fast` (rose) advances two — when `fast` traverses the cycle edge it
wraps back into the cycle. They collide inside the cycle when their
positions modulo the cycle length match.

```d3 widget=linked-list
{
  "title": "Floyd's algorithm — slow + fast detect a cycle of length 4",
  "direction": "single",
  "nodes": [
    {"id": "a", "value": "1"},
    {"id": "b", "value": "2"},
    {"id": "c", "value": "3"},
    {"id": "d", "value": "4"},
    {"id": "e", "value": "5"},
    {"id": "f", "value": "6"},
    {"id": "g", "value": "7"}
  ],
  "head": "a",
  "cycleTarget": "d",
  "steps": [
    {
      "links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]],
      "markers": [{"name": "slow", "nodeId": "a"}, {"name": "fast", "nodeId": "a"}],
      "msg": "Start. slow = fast = head. The dashed back-edge from node(7) to node(4) closes the cycle."
    },
    {
      "links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]],
      "markers": [{"name": "slow", "nodeId": "b"}, {"name": "fast", "nodeId": "c"}],
      "msg": "slow += 1 → node(2); fast += 2 → node(3)."
    },
    {
      "links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]],
      "markers": [{"name": "slow", "nodeId": "c"}, {"name": "fast", "nodeId": "e"}],
      "msg": "slow → node(3); fast → node(5)."
    },
    {
      "links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]],
      "markers": [{"name": "slow", "nodeId": "d"}, {"name": "fast", "nodeId": "g"}],
      "msg": "slow → node(4); fast → node(7). fast is at the tail — next advance wraps via the cycle edge."
    },
    {
      "links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]],
      "markers": [{"name": "slow", "nodeId": "e"}, {"name": "fast", "nodeId": "e"}],
      "msg": "slow → node(5); fast wraps 7 → 4 → 5 and lands on node(5). slow == fast — cycle detected. ✓"
    }
  ]
}
```

### Payload 5 — multi-list (alternate-merge with sections)

Interleaves two sorted singly lists (`A = 1 → 3 → 5`, blue;
`B = 2 → 4 → 6`, cyan) into a single chain by repeatedly splicing
`B[i]` between `A[i]` and `A[i+1]`. Exercises the multi-list canon
(`headA`, `headB`) and the sections progress bar — the five-step walk
is partitioned into `Identify` (initial state), `Splice` (the three
splice operations), and `Done` (final chain).

```d3 widget=linked-list
{
  "title": "Alternate-merge — interleave two sorted lists into one",
  "direction": "single",
  "nodes": [
    {"id": "a1", "value": "1"},
    {"id": "a2", "value": "3"},
    {"id": "a3", "value": "5"},
    {"id": "b1", "value": "2"},
    {"id": "b2", "value": "4"},
    {"id": "b3", "value": "6"}
  ],
  "head": "a1",
  "sections": [
    {"name": "Identify", "startIdx": 0},
    {"name": "Splice", "startIdx": 1},
    {"name": "Done", "startIdx": 4}
  ],
  "steps": [
    {
      "links": [["a1","a2"],["a2","a3"],["b1","b2"],["b2","b3"]],
      "markers": [{"name": "headA", "nodeId": "a1"}, {"name": "headB", "nodeId": "b1"}],
      "msg": "Two lists. A = 1 → 3 → 5 (blue); B = 2 → 4 → 6 (cyan). Walk headA and headB in lock-step."
    },
    {
      "links": [["a1","b1"],["b1","a2"],["a2","a3"],["b2","b3"]],
      "markers": [{"name": "headA", "nodeId": "a2"}, {"name": "headB", "nodeId": "b2"}],
      "msg": "Splice b1 between a1 and a2 (a1→b1, b1→a2). Advance headA → a2, headB → b2."
    },
    {
      "links": [["a1","b1"],["b1","a2"],["a2","b2"],["b2","a3"]],
      "markers": [{"name": "headA", "nodeId": "a3"}, {"name": "headB", "nodeId": "b3"}],
      "msg": "Splice b2 between a2 and a3 (a2→b2, b2→a3). Advance."
    },
    {
      "links": [["a1","b1"],["b1","a2"],["a2","b2"],["b2","a3"],["a3","b3"]],
      "markers": [{"name": "headA", "nodeId": "a3"}, {"name": "headB", "nodeId": "b3"}],
      "msg": "Splice b3 onto the end (a3 → b3). headB is now the last node — nothing left to interleave."
    },
    {
      "links": [["a1","b1"],["b1","a2"],["a2","b2"],["b2","a3"],["a3","b3"]],
      "markers": [{"name": "head", "nodeId": "a1"}],
      "msg": "Final list: 1 → 2 → 3 → 4 → 5 → 6."
    }
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a
payload for this widget, target **4–8 widget steps** (most chapters
land at 5–6). Compression strategy:

- **Each pointer-rewire** (`node.next = …`) → one widget step. The
  link redraws and the relevant markers move in the same D3
  transition; don't author a separate "show the new arrow" step.
- **Walked-to-position frames** (cursor advances one cell at a time
  with no structural change) → merge into one "walked to the
  insertion site" step if the walk is the same across operations.
  Keep individual advance steps when the walk *is* the point (e.g.
  cycle detection, traversal demos).
- **Doubly mode** — leave the back-row arrows to the auto-derive
  helper. The forward links are the source of truth; the renderer
  fills in `prev` for every forward link in the step.
- **Cycle visualisations** — set `cycleTarget` once at the spec
  level. Don't author the cycle back-edge as a `kind:"prev"` link in
  every step; the widget draws it from the tail node automatically.
- **Multi-stage walks** (≥ 8 steps with a clear macro shape) — add
  `sections` for visual chunking. For reorder = Split + Reverse +
  Merge, three sections at the relevant `startIdx` values; the
  progress bar grows dividers + labels above each segment.

Example: source 13-frame doubly-insert diagram → 5 widget steps
(walked-to-site + capture-next + rewire-forward + rewire-back +
final). The 13 → 5 compression relies on the renderer animating each
rewire's link movement and marker shuffle in a single transition pass.

## Browser verification

Open this chapter at
`http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-linked-list` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause /
   Reset).
2. Confirm marker colours match the canon: `head`/`slow`/`headA` =
   blue; `current`/`end` = emerald; `previous`/`start` = amber;
   `next` = violet; `fast` = rose; `headB` = cyan — same hex
   wherever they appear, every payload.
3. Payload 2 — the `new`-styled node(25) fades in on its allocation
   step (emerald outline). The rewires animate as link path moves,
   not redraws.
4. Payload 3 — the back-row prev arrows track each forward link
   automatically. As the splice rewires forward arrows, the back-row
   mirrors update in the same transition.
5. Payload 4 — the cycle back-edge curves from node(7) under the
   chain to node(4). When `fast` "wraps" via that edge, its label
   jumps to the cycle's target node — the back-edge itself stays
   visible the whole walk.
6. Payload 5 — the sections progress bar shows three labelled
   segments (`Identify` / `Splice` / `Done`) with dividers between
   them. Clicking a segment jumps to its first step.
7. Confirm no `.d3-widget__error` divs render.
8. Confirm DevTools console is clean — no widget exceptions; no
   `MarkerCanon` warnings on these payloads (every marker name is
   canon).

If any payload fails, fix and re-verify before committing.
