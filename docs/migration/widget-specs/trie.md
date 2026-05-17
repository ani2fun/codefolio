# Widget Spec — `trie`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render a prefix tree (trie) with:

- Multi-way nodes (up to ~26 children for lowercase alphabet; arbitrary
  string-set alphabet supported).
- **Character-edge labels** — every edge carries one character (or
  occasionally a short string in radix-tree variants).
- Terminal-node highlight — nodes that mark end-of-word render with a
  distinct ring (and optional value/count badge).
- Step animations for the three canonical operations:
  - `insert(word)` — walk existing prefix, append new nodes for new
    suffix, set terminal on the last node.
  - `search(word)` — walk and report hit / miss.
  - `prefix-walk(prefix)` — walk to the prefix node and highlight the
    subtree of all words sharing that prefix.

Step semantics: each step advances by one character (or "create new node"
event) for the active operation. 4-10 events per typical insertion/search.

## 2. Source-diagram inventory

**Zero.** Tries are absent from source's data-structure phases (source
covers arrays → SLL → DLL → hash → stack → queue → binary tree → BST →
heap → graph; no trie chapter). All payloads for this widget are authored
from destination prose + canonical reference examples.

Grep confirms:

```bash
grep -rn "^// Interactive Diagram" \
  /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/ \
  | grep -i trie
# (no matches)
```

The widget exists solely to serve destination's trie chapters and the
string-applications chapter that reuses tries as a base structure.

## 3. Destination chapter usage

| Destination chapter | Diagrams needed | Notes |
|---|---:|---|
| `03-trees/04-trie/01-introduction-to-tries.md` | 3-5 | Empty trie → insert "cat" → insert "car" → insert "card" → search hits / misses → prefix walk on "ca" |
| `07-strings/05-trie-string-applications.md` | 3-5 | Re-uses trie shape for: autocomplete suggestions, longest-common-prefix, word-break, suffix-trie (preview before suffix array chapter) |
| `09-probabilistic-and-advanced/...` (potential reuse) | 0-1 | Burst trie / HAT-trie mentions; reuse if a payload helps the prose |

Note: the audit assigns `trie` to chapters `03-trees/04-trie/` (just
`01-introduction-to-tries.md` currently — single-file directory) and
`07-strings/05-trie-string-applications.md`. The Aho-Corasick chapter
(`07-strings/08-aho-corasick.md`) reuses the **graph-explorer** widget
(`layout: "dag"`) for its goto/failure links, NOT the trie widget — see
graph-explorer spec.

## 4. Payload schema sketch

Nodes carry an `id`, optional `label` (rendered inside the circle —
typically empty; the edge carries the character), and `isTerminal` flag.
Edges always carry a single-character `label` (or string for radix
variants).

```jsonc
{
  "title": "Insert \"car\" then \"card\"",
  "operation": "insert",        // "insert" | "search" | "prefix-walk"
  "word": "card",               // operation target

  // The complete final-state trie (all nodes/edges that exist after the
  // last step). Per-step state derives by replaying events; this lets
  // the widget pre-position nodes via layout and just toggle visibility.
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c",    "isTerminal": false},
    {"id": "ca",   "isTerminal": false},
    {"id": "car",  "isTerminal": true},
    {"id": "card", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",    "label": "c"},
    {"from": "c",    "to": "ca",   "label": "a"},
    {"from": "ca",   "to": "car",  "label": "r"},
    {"from": "car",  "to": "card", "label": "d"}
  ],

  "events": [
    {"kind": "visit",    "nodeId": "root", "char": "",  "msg": "Start at root"},
    {"kind": "visit",    "nodeId": "c",    "char": "c", "msg": "Edge c already exists — follow"},
    {"kind": "visit",    "nodeId": "ca",   "char": "a", "msg": "Edge a already exists — follow"},
    {"kind": "visit",    "nodeId": "car",  "char": "r", "msg": "Edge r already exists — follow"},
    {"kind": "create",   "nodeId": "card", "char": "d", "msg": "No edge for d — create new node"},
    {"kind": "terminal", "nodeId": "card", "msg": "Mark card as terminal — word complete"}
  ]
}
```

### Canonical event kinds (closed catalog)

| Kind | Effect |
|---|---|
| `visit` | Highlight node + the incoming edge (`active` state). Pre-existing nodes stay drawn; new nodes are hidden until `create`. |
| `create` | Reveal a node and its incoming edge (fade-in 300ms). Marks node `new` for the duration of this step. |
| `terminal` | Add the terminal ring + (optional) badge to a node. |
| `hit` | Search success — flash the final node green. |
| `miss` | Search failure — flash the last-visited node red and draw a "no edge for X" callout. |
| `prefix-highlight` | Mark all descendants of `nodeId` as `in-prefix` (lighter blue halo) — used by `prefix-walk` to show "all words sharing this prefix". |

### Canonical node statuses

`hidden` (not yet created) / `default` / `active` / `new` / `terminal` /
`in-prefix` / `hit` / `miss`.

### Operation hints

- `operation: "insert"` — events are typically `visit` × prefix-length
  then `create` × new-suffix-length then `terminal`.
- `operation: "search"` — events are `visit` × until-hit-or-miss, ending
  in `hit` (with optional `terminal` re-highlight) or `miss`.
- `operation: "prefix-walk"` — `visit` × prefix-length then
  `prefix-highlight` on the prefix node.

The `operation` field is metadata — it labels the demo and seeds the
panel/header text. The actual behaviour is driven entirely by `events`.

## 5. POC payloads

### 5.1 Insert into empty trie

````d3 widget=trie
{
  "title": "Insert \"cat\" into empty trie",
  "operation": "insert",
  "word": "cat",
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c"},
    {"id": "ca"},
    {"id": "cat", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",   "label": "c"},
    {"from": "c",    "to": "ca",  "label": "a"},
    {"from": "ca",   "to": "cat", "label": "t"}
  ],
  "events": [
    {"kind": "visit",    "nodeId": "root", "msg": "Empty trie — start at root"},
    {"kind": "create",   "nodeId": "c",    "char": "c", "msg": "Create node c"},
    {"kind": "create",   "nodeId": "ca",   "char": "a", "msg": "Create node ca"},
    {"kind": "create",   "nodeId": "cat",  "char": "t", "msg": "Create node cat"},
    {"kind": "terminal", "nodeId": "cat",  "msg": "Mark cat as terminal — word \"cat\" stored"}
  ]
}
````

### 5.2 Insert with shared prefix

````d3 widget=trie
{
  "title": "Insert \"car\" reuses \"ca\" prefix",
  "operation": "insert",
  "word": "car",
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c"},
    {"id": "ca"},
    {"id": "cat", "isTerminal": true},
    {"id": "car", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",   "label": "c"},
    {"from": "c",    "to": "ca",  "label": "a"},
    {"from": "ca",   "to": "cat", "label": "t"},
    {"from": "ca",   "to": "car", "label": "r"}
  ],
  "events": [
    {"kind": "visit",    "nodeId": "root", "msg": "Start at root"},
    {"kind": "visit",    "nodeId": "c",    "char": "c", "msg": "c exists — follow"},
    {"kind": "visit",    "nodeId": "ca",   "char": "a", "msg": "a exists — follow"},
    {"kind": "create",   "nodeId": "car",  "char": "r", "msg": "No r edge — create"},
    {"kind": "terminal", "nodeId": "car",  "msg": "Mark car as terminal"}
  ]
}
````

### 5.3 Search hit

````d3 widget=trie
{
  "title": "Search for \"car\" — hit",
  "operation": "search",
  "word": "car",
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c"},
    {"id": "ca"},
    {"id": "cat", "isTerminal": true},
    {"id": "car", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",   "label": "c"},
    {"from": "c",    "to": "ca",  "label": "a"},
    {"from": "ca",   "to": "cat", "label": "t"},
    {"from": "ca",   "to": "car", "label": "r"}
  ],
  "events": [
    {"kind": "visit", "nodeId": "root", "msg": "Start at root"},
    {"kind": "visit", "nodeId": "c",    "char": "c", "msg": "Follow c"},
    {"kind": "visit", "nodeId": "ca",   "char": "a", "msg": "Follow a"},
    {"kind": "visit", "nodeId": "car",  "char": "r", "msg": "Follow r — arrived at terminal"},
    {"kind": "hit",   "nodeId": "car",  "msg": "Match — \"car\" is in the trie"}
  ]
}
````

### 5.4 Search miss

````d3 widget=trie
{
  "title": "Search for \"ca\" — miss (no terminal)",
  "operation": "search",
  "word": "ca",
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c"},
    {"id": "ca"},
    {"id": "cat", "isTerminal": true},
    {"id": "car", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",   "label": "c"},
    {"from": "c",    "to": "ca",  "label": "a"},
    {"from": "ca",   "to": "cat", "label": "t"},
    {"from": "ca",   "to": "car", "label": "r"}
  ],
  "events": [
    {"kind": "visit", "nodeId": "root", "msg": "Start at root"},
    {"kind": "visit", "nodeId": "c",    "char": "c", "msg": "Follow c"},
    {"kind": "visit", "nodeId": "ca",   "char": "a", "msg": "Follow a — arrived"},
    {"kind": "miss",  "nodeId": "ca",   "msg": "Not terminal — \"ca\" is a prefix but not a stored word"}
  ]
}
````

### 5.5 Prefix walk — autocomplete-style

````d3 widget=trie
{
  "title": "Prefix-walk \"ca\" — highlight all words below",
  "operation": "prefix-walk",
  "word": "ca",
  "nodes": [
    {"id": "root", "label": "*"},
    {"id": "c"},
    {"id": "ca"},
    {"id": "cat",  "isTerminal": true},
    {"id": "car",  "isTerminal": true},
    {"id": "card", "isTerminal": true}
  ],
  "edges": [
    {"from": "root", "to": "c",    "label": "c"},
    {"from": "c",    "to": "ca",   "label": "a"},
    {"from": "ca",   "to": "cat",  "label": "t"},
    {"from": "ca",   "to": "car",  "label": "r"},
    {"from": "car",  "to": "card", "label": "d"}
  ],
  "events": [
    {"kind": "visit",            "nodeId": "root", "msg": "Start at root"},
    {"kind": "visit",            "nodeId": "c",    "char": "c", "msg": "Follow c"},
    {"kind": "visit",            "nodeId": "ca",   "char": "a", "msg": "Follow a — prefix matched"},
    {"kind": "prefix-highlight", "nodeId": "ca",   "msg": "Highlight all descendants — completions: cat, car, card"}
  ]
}
````

## 6. Closest existing widget to mimic

`LinkedList` is the structural parent: keyed node + edge selections,
per-step rebind, smooth `transform` transitions, event-driven state
overlay. The trie's branching shape is wider than a list's linear chain
but the rendering machinery is the same shape — `<g>` per node, `<path>`
per edge, single update pass per step.

Conceptually the trie is also adjacent to `decision-tree` and `call-stack`
recursion-tree mode (all three are layered parent-pointer trees). All
three benefit from the same `LayeredLayout` candidate-shared abstraction
introduced in the `graph-explorer` spec.

## 7. D3 selections plan

Top-level `g.trie__canvas`. Pre-compute per step: `nodeStatus`, `visibleNodes`,
`visibleEdges`, `currentChar` (for the marker overlay).

Inside the canvas:

- **Edges** (drawn first): `selectAll("path.trie__edge").data(visibleEdges, e => e.to)`
  - Enter: appear via fade-in over 300ms; positioned by `LayeredLayout`.
  - Update: rebind class for status (`default` / `active` / `in-prefix`).
  - Exit: handled implicitly — once a node is visible, it stays (insertion
    operations are append-only).
  - Each edge carries a `<text class="trie__edge-label">` rendered at the
    midpoint with the character.
- **Nodes**: `selectAll("g.trie__node").data(visibleNodes, n => n.id)`
  - Enter: append `<g>` with `<circle>` (and optional `<text>` for root
    `*`). Initial scale 0.6 → 1 transition for `create` events.
  - Update: rebind class for status; toggle the terminal-ring `<circle.trie__terminal-ring>`
    when `isTerminal`.
  - `hit` / `miss` status flashes the circle fill via 600ms tween then
    settles back to `default`.
- **Active-character pill** (transient): per step, append a small floating
  `<g.trie__active-char>` near the active node showing the character
  currently being processed; fades out at next step.
- **Subtree halo** (prefix-walk mode): `<rect.trie__prefix-halo>` drawn
  behind the in-prefix subtree's bounding box, fades in over 400ms on
  `prefix-highlight` event.

Layout:

- Pre-compute `(x, y)` per node from the final-state tree topology using a
  Reingold-Tilford-style layered layout (depth = y, leaf-sweep = x).
- Layout is computed once at mount; nodes that are `hidden` simply aren't
  rendered, but their slot is reserved so the trie grows in stable positions
  as `create` events fire.

## 8. Shared abstractions

Reuse:

- `Stepper.hook` over events tape.
- `PayloadDecoder`.

Candidate shared with `call-stack` recursion-tree mode, `decision-tree`,
`dp-table` top-down mode:

- **LayeredLayout** — parent-pointer tree → `(x, y)`. The trie is the
  fourth consumer and the first one with branching > 2 children, so the
  layout helper must handle arbitrary fan-out (it should anyway — the
  `decision-tree` widget has the same requirement).

The trie has no novel cross-widget abstractions of its own. The
edge-label rendering pattern (character on the edge) is specific enough to
this widget to stay local.

## 9. Estimated build session count

**1 session.** Smallest of the five widgets in this batch:

- Single layout mode (no `force`/`grid`/`dag` toggle).
- Append-only state (no pop/undo machinery).
- Six event kinds, all simple to render.
- No sidecar panels.

The only nontrivial piece is the `prefix-highlight` subtree halo + the
character-edge-label positioning at midpoints; both fit within a normal
build session alongside the core renderer.

## 10. POC chapter

Demo book chapter at
`content/cortex/dsa-widget-catalog/07-trie.md` exhibits all five POC
payloads above, plus an "incrementally build the same trie via three
sequential inserts" example (three trie widget instances in one chapter
showing the trie state evolving from empty → after insert("cat") →
after insert("car") → after insert("card")) to validate that the same
final-state tree renders cleanly across multiple instances.

First real production usage is
`03-trees/04-trie/01-introduction-to-tries.md` — covers all three
operations (insert, search, prefix-walk) within a single chapter, which
is exactly what the POC payload set validates. Then
`07-strings/05-trie-string-applications.md` reuses the widget for
autocomplete + LCP + word-break payloads (each with a custom event
sequence over the same trie shape).
