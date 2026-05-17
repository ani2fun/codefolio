# Widget Spec — `linked-list` (retrofit)

> Read [`../methodology.md`](../methodology.md) first. This spec drives Arc 1
> widget build for `linked-list` and Arc 3 chapter payload writing.
>
> **Status:** the widget already exists at
> `/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala`
> (1,552 LOC). It ships with `direction: "single" | "double"`, marker canon
> (ADR-0016), cycle back-edges, row wrapping, sectioned progress bars, and
> empty-step placeholders. This document is a **retrofit note**: it lists
> what to exercise / harden for Phase 2 (Doubly Linked List), and the
> decision *not* to extend it into Skip-List territory (per
> `diagram-gap-audit.md` "Conditional widget decisions").

## 1. Purpose

Animate node-by-node operations on a singly or doubly linked chain: pointer
rewiring (insert / delete), traversal (forward / reverse / two-pointer),
reversal (in-place / segment), cycle detection (Floyd's), reorder
(split + reverse + merge). Each step renders a snapshot: which nodes exist,
how they're linked (next + optional prev), where each named marker
(`head`, `current`, `previous`, `next`, `slow`, `fast`, `dummy`, segment
multi-list `headA`/`headB`/`tailA`/`tailB`) sits.

## 2. Source-diagram inventory — retrofit-only gaps

### Gap A — Exercise `direction: "double"` end-to-end (Phase 2)

The `direction` flag exists in code (`LinkedList.scala:268`) but Phase 1
(singly) never set it to `"double"`. Phase 2 is the first production run
through the doubly mode. The auto-derived `prev` arrows kick in via
`expandLinksForDirection` (line 324); test for visual collisions when
combined with the `current`/`previous` marker pair on the back-edge lane.

Source diagrams that will exercise it (44 total across Phase 2):

- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/01-introduction-to-doubly-linked-lists/01-understanding-the-problem.md:13`
  — "The node to be deleted is not the first node" (8 frames → 5 widget steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/01-introduction-to-doubly-linked-lists/01-understanding-the-problem.md:33`
  — "Need to traverse the list to find the node right before the one to
  insert" (8 frames → 5 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/01-introduction-to-doubly-linked-lists/02-exploring-a-possible-solution.md:17`
  — "Insertion and deletion before the given node do not require traversal"
  (2 frames → 1 step, single frame demo)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/01-introduction-to-doubly-linked-lists/02-exploring-a-possible-solution.md:25`
  — "Delete the given node in doubly linked list" (6 frames → 4 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/01-introduction-to-doubly-linked-lists/05-overview-of-supported-operations.md:11`
  — "Some operations on a doubly linked list" (8 frames → 5 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/02-traversal-in-doubly-linked-lists/01-understanding-traversal.md:9`
  — "Forward traversal using the next pointer" (5 frames → 4 steps)
- `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/3.doubly-linked-list/02-traversal-in-doubly-linked-lists/01-understanding-traversal.md:93`
  — "Reverse traversal using the previous pointer" (5 frames → 4 steps)
- 10 diagrams across `03-insertion-in-doubly-linked-lists/*` (mostly 5-10
  frame insert-at-beginning / end / before / after / at-distance)
- 15 diagrams across `04-deletion-in-doubly-linked-lists/*` (mirrors
  insertion shape; this is the largest source chapter)
- 5 diagrams under `05-pattern-reversal` (23-frame full reversal + 18-frame
  segment reversal + 3/5-frame connect-tail/head splices)
- 1 diagram under `06-pattern-reversal-subproblem` (25-frame K-group reverse)
- 2 diagrams under `07-pattern-two-pointers` (12-frame intro + 19-frame
  pair-with-sum)
- 4 diagrams under `08-pattern-reorder` (28/29/31/4-frame split + merge)

Total: 44 diagrams → roughly 30-40 widget instances after frame
compression.

### Gap B — Phase 1 catch-up backlog (uses existing `single` mode)

Phase 1 left 7 diagrams unconverted in two source chapters (per
`diagram-gap-audit.md` Phase 1 notes):

- `02-linear-structures/03-singly-linked-list/10-pattern-split.md` — currently
  1 widget instance; source has 5 diagrams → +4 instances needed
- `02-linear-structures/03-singly-linked-list/12-pattern-reorder.md` —
  currently 1 widget instance; source has 4 diagrams → +3 instances needed

No code change required for this gap — it's content-only.

### Decision — NOT extending into skip-list territory

Per `diagram-gap-audit.md` conditional decision (line 144): **skip-list
gets its own widget**, not a multi-layer extension of `linked-list`.
Reasoning is in the audit doc; for this spec, the load-bearing detail is
that the existing `linked-list` schema does not need to grow a `layer` /
`level` axis. The retrofit stays narrow: validate doubly mode, exercise
`headA`/`headB`/`headC` markers on multi-list operations, write down the
visual gotchas as they surface.

## 3. Destination chapter usage

### After Phase 1.6 catch-up

- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/03-singly-linked-list/10-pattern-split.md`
  — uses existing `direction: "single"`; add 4 instances.
- `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/03-singly-linked-list/12-pattern-reorder.md`
  — uses `headA`/`headB`/`tailA`/`tailB` markers; add 3 instances.

### Phase 2 — Doubly Linked List (POC scope)

All nine chapters under
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/04-doubly-linked-list/`
take a payload with `direction: "double"`. Per source diagram counts:

- `01-introduction-to-doubly-linked-lists.md` — 5 source diagrams
- `02-traversal-in-doubly-linked-lists.md` — 2 source diagrams (POC; see §10)
- `03-insertion-in-doubly-linked-lists.md` — 10 source diagrams
- `04-deletion-in-doubly-linked-lists.md` — 15 source diagrams (largest)
- `05-pattern-reversal.md` — 5 source diagrams
- `06-pattern-reversal-subproblem.md` — 1 source diagram (K-group reverse)
- `07-pattern-two-pointers.md` — 2 source diagrams
- `08-pattern-reorder.md` — 4 source diagrams
- `09-design.md` — 0 source diagrams (LRU cache exercise, prose-only)

### Reuse later (no code retrofit)

- `03-trees/04-trie/*` and `09-probabilistic-and-advanced/05-treap.md` — no.
  These get their own widgets.
- `11-dsa-in-real-systems/03-redis-internal-encodings.md` — uses `linked-list`
  to show Redis quicklist node chain, just one instance.
- `10-concurrency-and-systems/02-lock-free-queue.md` — uses `linked-list`
  with `cycleTarget` repurposed as the CAS-loop back-edge; one instance.

## 4. Payload schema sketch

No schema changes for this retrofit. Documenting the existing schema for
authoring reference:

```ts
{
  title?: string,
  direction?: "single" | "double",           // default "single"
  nodes: Array<{                              // global node pool; per-step can override
    id: string,
    value: string,
    style?: "new" | "removed" | "highlight"
  }>,
  head?: string,                              // node id — defaults to nodes[0].id
  cycleTarget?: string,                       // node id; draws a back-edge from tail
  wrapAt?: number,                            // wrap to a new row every N nodes (long chains)
  sections?: Array<{ name: string, startIdx: number }>,
  steps: Array<{
    nodes?: Array<{ id, value, style }>,      // optional per-step override
    links: Array<                              // arrows for this step
      | [string, string]                       // shorthand: [from, to] → next
      | { from: string, to: string, kind?: "next" | "prev" | "broken" }
    >,
    markers: Array<{                           // canonical names ONLY (ADR-0016)
      name: "head" | "tail" | "previous" | "current" | "next" | "slow" |
            "fast" | "dummy" | "start" | "end" |
            "headA" | "headB" | "headC" | "tailA" | "tailB" | "tailC",
      nodeId: string
      // NB: `color` field on marker is silently dropped at parse time
    }>,
    head?: string,                              // per-step head override (reversal)
    msg: string
  }>
}
```

## 5. POC payloads

### 5.1 Smallest — empty list step (edge case)

```d3 widget=linked-list
{
  "title": "Empty list — base case for insert",
  "direction": "single",
  "nodes": [
    {"id": "n1", "value": "a"}
  ],
  "head": "n1",
  "steps": [
    {
      "nodes": [],
      "links": [],
      "markers": [],
      "msg": "List is empty (head = null). Inserting any node creates the new head."
    },
    {
      "links": [],
      "markers": [{"name": "head", "nodeId": "n1"}],
      "msg": "Insert 'a' → head → a → null."
    }
  ]
}
```

### 5.2 Typical doubly insert (POC chapter for Phase 2)

```d3 widget=linked-list
{
  "title": "Insert 25 after node 20 in a doubly linked list",
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
      "markers": [{"name": "current", "nodeId": "n20"}],
      "msg": "current points at 20 — insertion site."
    },
    {
      "links": [["n10","n20"],["n20","n30"],["n30","n40"]],
      "markers": [
        {"name": "current", "nodeId": "n20"},
        {"name": "next",    "nodeId": "n30"}
      ],
      "msg": "Save next = current.next = 30. Allocate new node 25."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n30","n40"]],
      "markers": [
        {"name": "current", "nodeId": "n20"},
        {"name": "next",    "nodeId": "n30"}
      ],
      "msg": "Rewire current.next = 25; auto-derived 25.prev = current."
    },
    {
      "links": [["n10","n20"],["n20","n25"],["n25","n30"],["n30","n40"]],
      "markers": [
        {"name": "current", "nodeId": "n20"},
        {"name": "next",    "nodeId": "n30"}
      ],
      "msg": "Rewire 25.next = next (30); auto-derived 30.prev = 25. Done."
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
      "markers": [],
      "msg": "Final: 10 ↔ 20 ↔ 25 ↔ 30 ↔ 40."
    }
  ]
}
```

### 5.3 Large-N stress (cycle detection, Floyd's)

```d3 widget=linked-list
{
  "title": "Floyd's — slow + fast pointers detect a cycle of length 4",
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
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]], "markers": [{"name": "slow", "nodeId": "a"}, {"name": "fast", "nodeId": "a"}], "msg": "slow=fast=head."},
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]], "markers": [{"name": "slow", "nodeId": "b"}, {"name": "fast", "nodeId": "c"}], "msg": "slow advances 1, fast advances 2."},
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]], "markers": [{"name": "slow", "nodeId": "c"}, {"name": "fast", "nodeId": "e"}], "msg": "slow at 3, fast at 5."},
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]], "markers": [{"name": "slow", "nodeId": "d"}, {"name": "fast", "nodeId": "g"}], "msg": "slow at 4, fast at 7 (about to loop back via cycle edge)."},
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"],["f","g"]], "markers": [{"name": "slow", "nodeId": "e"}, {"name": "fast", "nodeId": "e"}], "msg": "Both at 5 — collision! Cycle detected."}
  ]
}
```

### 5.4 Edge case — segment reversal with `headA`/`tailA` markers

```d3 widget=linked-list
{
  "title": "Reverse segment between 'start' (b) and 'end' (e), then splice back",
  "direction": "single",
  "nodes": [
    {"id": "a", "value": "1"},
    {"id": "b", "value": "2"},
    {"id": "c", "value": "3"},
    {"id": "d", "value": "4"},
    {"id": "e", "value": "5"},
    {"id": "f", "value": "6"}
  ],
  "head": "a",
  "sections": [
    {"name": "Identify", "startIdx": 0},
    {"name": "Reverse", "startIdx": 1},
    {"name": "Splice", "startIdx": 3}
  ],
  "steps": [
    {"links": [["a","b"],["b","c"],["c","d"],["d","e"],["e","f"]], "markers": [{"name": "start", "nodeId": "b"}, {"name": "end", "nodeId": "e"}], "msg": "Mark the segment endpoints."},
    {"links": [["a","b"],["c","b"],["d","c"],["e","d"],["e","f"]], "markers": [{"name": "headA", "nodeId": "e"}, {"name": "tailA", "nodeId": "b"}], "msg": "Reverse the segment in isolation: 5 → 4 → 3 → 2."},
    {"links": [["a","b"],["c","b"],["d","c"],["e","d"],["e","f"]], "markers": [{"name": "headA", "nodeId": "e"}, {"name": "tailA", "nodeId": "b"}, {"name": "previous", "nodeId": "a"}], "msg": "Locate the parent (a) and the post-segment node (f)."},
    {"links": [["a","e"],["c","b"],["d","c"],["e","d"],["b","f"]], "markers": [{"name": "head", "nodeId": "a"}], "msg": "Splice: a.next = headA (e); tailA (b).next = f. Final list: 1 → 5 → 4 → 3 → 2 → 6."}
  ]
}
```

### 5.5 Multi-list — alternate-merge with `headA`/`headB`/`tailA`/`tailB`

```d3 widget=linked-list
{
  "title": "Alternate-merge two lists into one",
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
  "steps": [
    {"links": [["a1","a2"],["a2","a3"],["b1","b2"],["b2","b3"]], "markers": [{"name": "headA", "nodeId": "a1"}, {"name": "headB", "nodeId": "b1"}], "msg": "Two parallel lists: A=1→3→5, B=2→4→6."},
    {"links": [["a1","b1"],["a2","a3"],["b1","a2"],["b2","b3"]], "markers": [{"name": "headA", "nodeId": "a2"}, {"name": "headB", "nodeId": "b2"}], "msg": "Splice A[0] → B[0] → A[1]; advance both heads."},
    {"links": [["a1","b1"],["a2","b2"],["b1","a2"],["b2","a3"],["a3"," "]], "markers": [{"name": "headA", "nodeId": "a3"}, {"name": "headB", "nodeId": "b3"}], "msg": "Splice A[1] → B[1] → A[2]; advance."},
    {"links": [["a1","b1"],["a2","b2"],["a3","b3"],["b1","a2"],["b2","a3"]], "markers": [{"name": "head", "nodeId": "a1"}], "msg": "Final: 1 → 2 → 3 → 4 → 5 → 6."}
  ]
}
```

## 6. Closest existing widget to mimic

This **is** the widget being retrofitted —
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala`
(1,552 LOC). The retrofit is content-only; no code changes are expected.
The `expandLinksForDirection` helper (line 324) and the `hasBackLane`
check (line 315) already drive the doubly mode. Per-instance visual tuning
(label collisions on the back-edge lane, cycle-edge clipping with long
node values) lands as needed during the Phase 2 chapter sweep.

## 7. D3 selections plan

No selection changes for this retrofit. The existing keyed joins
(`g.linked-list__node` keyed by `node.id`, `g.linked-list__arrow-group`
keyed by `${from}-${kind}`, `g.linked-list__marker` keyed by `name`) cover
both directions. Authors driving doubly mode rely on the auto-derived
back-row arrows (`expandLinksForDirection` synthesises one `prev` arrow
per forward link in any step that doesn't explicitly include `kind:"prev"`).

## 8. Shared abstractions

Reuses
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala`
and
`/Users/aniket/Development/homelab/codefolio/client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala`
(both already wired). The widget already carries its own per-canon marker
colour table (ADR-0016) — keep that local; the upcoming widget specs that
need analogous canons (`stack-queue`, `binary-tree`) maintain their own.

## 9. Estimated build session count

**0 sessions** for code — there is no widget code to write or change for
this retrofit. The Phase 1.6 catch-up is **1 content session** (7
diagrams); Phase 2 is **5-7 content sessions** (44 diagrams across 9
chapters, batched per ROI in `diagram-gap-audit.md` Wave A item 1). If the
back-edge lane visibly breaks on a Phase 2 chapter, a focused 1-session
visual fix sits between the POC and the rest of Phase 2.

## 10. POC chapter

`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/04-doubly-linked-list/02-traversal-in-doubly-linked-lists.md`
— the smallest Phase 2 chapter (2 source diagrams, both 5-frame forward /
reverse traversals). Forward traversal proves `direction: "double"` doesn't
break singly-style payloads; reverse traversal exercises the back-edge
lane visibly. Anything that fails here surfaces before the larger
insertion / deletion / reversal chapters consume time.
