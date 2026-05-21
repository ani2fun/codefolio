---
title: trie
summary: Character-edge prefix tree with three canonical operations — insert / search / prefix-walk — over a single payload shape. Per-node state splits cleanly into a persistent layer (visibility / terminal / in-prefix — once set, stays set) and a transient adornment layer (active / new / hit / miss — reset each step), so the cursor walking past a terminal node can't strip its end-of-word ring.
prereqs: []
---

# `trie`

## Purpose

A single widget covers source's trie content — currently the
`03-trees/04-trie/01-introduction-to-tries.md` chapter and the
`07-strings/05-trie-string-applications.md` reuse — plus future radix /
suffix-trie variants that share the same character-edge topology. The
model is a multi-way prefix tree where every edge carries one character
and terminal nodes mark stored end-of-word positions.

Three `operation` flags steer the renderer — same trie, three event-tape
shapes:

- `operation: "insert"` — events typically run `visit` × prefix-length
  → `create` × new-suffix-length → `terminal` to mark the inserted
  word's last node. The kind badge tints emerald (additive).
- `operation: "search"` — events run `visit` × until-hit-or-miss, ending
  in either `hit` (the word is stored) or `miss` (the word is a prefix
  but isn't itself a stored word, or a character has no matching edge).
  The kind badge tints blue (read-only).
- `operation: "prefix-walk"` — events run `visit` × prefix-length →
  `prefix-highlight` on the prefix node, painting the entire descendant
  subtree as "all words sharing this prefix". The kind badge tints
  violet (subtree query).

The data model is small: `nodes` (the full final-state trie node list with
optional `label` and `isTerminal` flags), `edges` (parent-to-child with a
single-character `label`), and `events` (a tape of `visit` / `create` /
`terminal` / `hit` / `miss` / `prefix-highlight` operations). The
renderer replays events to compute per-step state.

**Per-node status splits into two layers** — the same persistent /
transient pattern that dp-table introduced:

- **Persistent** (`visible` / `terminal` / `in-prefix`) — set once,
  never overwritten by later events. `create` adds to `visible`;
  `terminal` adds to `terminal`; `prefix-highlight` adds the named node
  *and its entire descendant subtree* to `in-prefix`. Pre-existing
  terminal nodes start in the terminal set at step 0.
- **Transient adornments** (`active` / `new` / `hit` / `miss`) — added
  as additional modifier classes on top of the persistent status for
  the CURRENT step only. Stacked, not exclusive: a `terminal` node that
  the cursor walks past in a search trace reads as "terminal (amber
  ring) + active (emerald halo)" mid-step, then drops back to just
  "terminal" once the cursor moves on.

Splitting persistent vs. transient means a `visit` event that passes
*over* a terminal node can't strip the end-of-word ring — terminal is
its own orthogonal boolean, not a competing enum value. No
terminal-status preservation guard inside the event applier is needed.

> **Source spec**: `docs/migration/widget-specs/trie.md`
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/Trie.scala`
>
> **Event-tape model**: the renderer's per-step state is computed by
> folding `applyEvent` over `events[0..stepIdx]`. Transient adornments are
> reset at the head of every event so they NEVER leak across steps;
> persistent statuses accumulate. Layout (Reingold-Tilford slot
> assignment) is computed once at mount over the FINAL-STATE topology,
> so newly-revealed nodes from `create` events appear at their final
> position rather than shifting siblings around.

## Payload schema (reference card)

```ts
{
  title:     string,
  operation: "insert" | "search" | "prefix-walk",
  word?:     string,                              // metadata — rendered as the operation badge subtitle

  // The complete FINAL-state trie. Per-step visibility is derived: nodes NOT
  // mentioned in any "create" event are visible from step 0; "create" events
  // reveal additional nodes mid-trace.
  nodes: [
    { id: string, label?: string, isTerminal?: boolean }
  ],
  edges: [
    { from: string, to: string, label: string }   // label = the character on the edge
  ],

  events: [
    // visit — cursor steps onto an already-visible node. `char` (if present)
    //         accumulates into the consumed-prefix readout.
    { kind: "visit",            nodeId: string, char?: string, msg: string },
    // create — reveal a node (toggle from hidden → visible); set just-created
    //          + active; `char` accumulates.
    { kind: "create",           nodeId: string, char?: string, msg: string },
    // terminal — add the end-of-word ring to a node. Persistent.
    { kind: "terminal",         nodeId: string, msg: string },
    // hit — search success flash.
    { kind: "hit",              nodeId: string, msg: string },
    // miss — search failure flash.
    { kind: "miss",             nodeId: string, msg: string },
    // prefix-highlight — mark `nodeId` and its entire descendant subtree as
    //                    in-prefix (persistent violet halo + ring tint).
    { kind: "prefix-highlight", nodeId: string, msg: string }
  ]
}
```

**Required**: `title`, `operation`, `nodes` (non-empty), `edges`,
`events` (non-empty), `events[].kind` (closed set), `events[].nodeId`,
`events[].msg`.
**Optional**: `word` (metadata only — used to label the operation
badge), `nodes[].label` (typically just `"*"` for root), `nodes[].isTerminal`
(defaults to false; only meaningful for end-of-word nodes that exist at
step 0 — created-during-trace nodes get their terminal mark via a
`terminal` event), `edges[].label` (defaults to empty string, but you
should always supply it), `events[].char` (only `visit` and `create`
care; the character accumulates into the consumed-prefix readout).

**Closed sets** (unknown values fall back to a safe default at parse
time, so a typo never crashes the chapter):

- `operation` ∈ `{ "insert", "search", "prefix-walk" }` — unknown →
  `"insert"`.
- `events[].kind` ∈ `{ "visit", "create", "terminal", "hit", "miss",
  "prefix-highlight" }` — unknown → `"visit"`.

**Event-replay rules**:

- `visit` sets the active cursor on the named node and accumulates
  `char` into the consumed-prefix readout. Does NOT touch persistent
  state — terminal nodes the cursor passes over keep their ring.
- `create` is `visit` + reveals the node. Sets the `new` adornment for
  one step (drives a stronger emerald paint and the fade-in animation).
- `terminal` adds the named node to the persistent `terminal` set. The
  cursor stays on the node so the reader sees WHICH node just got the
  ring. Does NOT consume a character.
- `hit` flashes the named node green; cursor stays. Does NOT consume a
  character (the cursor has already walked to this position via prior
  `visit` events).
- `miss` flashes the named node rose; cursor stays. Does NOT consume a
  character.
- `prefix-highlight` adds the named node AND its entire descendant
  subtree to the persistent `in-prefix` set. The cursor stays on the
  named node; the violet halo paints every descendant + its incoming
  edges + its terminal rings.
- Every event resets the transient adornments at the head, so a `visit`
  after a `create` clears the previous step's `--new` flash.

**Node-status combinations** (stacked classes — examples):

| Persistent           | Adornments                  | Reading                                                 |
|---                   |---                          |---                                                      |
| `default`            | —                           | Visible, no special state                               |
| `default`            | `active`                    | Cursor is here this step                                |
| `default`            | `active` + `new`            | Just created — fade-in flash                            |
| `default` + `terminal` | —                         | Stored end-of-word; ring stays drawn                    |
| `default` + `terminal` | `active`                  | Cursor walking past a stored word — ring preserved      |
| `default` + `terminal` | `hit`                     | Search succeeded — green flash, ring still drawn        |
| `default`            | `miss`                      | Search failed — rose flash                              |
| `in-prefix`          | —                           | Descendant of a prefix-walk node                        |
| `in-prefix` + `terminal` | —                       | A stored word inside a prefix-walk subtree              |
| `in-prefix`          | `active`                    | Prefix-walk target node — cursor sitting on it          |

## Representative payloads

### Payload 1 — Insert "cat" into empty trie

The simplest case. Start from an empty trie (only root visible), walk
the `create` ladder to add each character of "cat", then mark the last
node terminal. Exercises:

- Multiple `create` events in a row revealing nodes one at a time.
- The just-created `--new` adornment fading in over 350ms per node.
- The `char` field of `create` events accumulating into the consumed-
  prefix readout below the canvas (`""` → `"c"` → `"ca"` → `"cat"`).
- The `terminal` event adding the amber ring without touching the
  cursor (active stays on `cat` for the final step).

```d3 widget=trie
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
    {"kind": "create",   "nodeId": "c",    "char": "c", "msg": "No edge for c — create node c"},
    {"kind": "create",   "nodeId": "ca",   "char": "a", "msg": "No edge for a — create node ca"},
    {"kind": "create",   "nodeId": "cat",  "char": "t", "msg": "No edge for t — create node cat"},
    {"kind": "terminal", "nodeId": "cat",  "msg": "Mark cat as terminal — word \"cat\" stored"}
  ]
}
```

### Payload 2 — Insert "car" reuses the "ca" prefix

The interesting case: the trie already contains "cat", so inserting
"car" walks the shared prefix (`visit` × 3) before branching off with a
single `create` for the new suffix. Exercises:

- Pre-existing nodes (`c`, `ca`, `cat`) visible from step 0 — the trie
  isn't empty when the trace starts.
- A pre-existing terminal node (`cat`) carrying its amber ring at step
  0 *and* surviving the cursor walking past it (cursor lands on `c`
  then `ca`, then branches to `car` — `cat` keeps its ring the entire
  time, including the steps where the cursor sits on its parent `ca`).
- One `create` introducing a new sibling node (`car`) under an existing
  internal node (`ca`).
- The just-created `--new` flash lighting up `car` for step 4, then
  fading by step 5 (when `terminal car` fires and replaces it).

```d3 widget=trie
{
  "title": "Insert \"car\" reuses the \"ca\" prefix",
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
    {"kind": "visit",    "nodeId": "c",    "char": "c", "msg": "Edge c exists — follow"},
    {"kind": "visit",    "nodeId": "ca",   "char": "a", "msg": "Edge a exists — follow"},
    {"kind": "create",   "nodeId": "car",  "char": "r", "msg": "No edge for r — create node car"},
    {"kind": "terminal", "nodeId": "car",  "msg": "Mark car as terminal — word \"car\" stored"}
  ]
}
```

### Payload 3 — Search for "car" — hit

A search trace over the same two-word trie. Walk the path `root → c →
ca → car`, then a terminal `hit` event flashes the success. Exercises:

- The search-mode (blue) operation badge — visually distinct from
  insert (emerald) and prefix-walk (violet).
- The cursor sitting on a `terminal` node (`car`) — the amber ring
  stays drawn even though the active emerald halo paints on top
  (modifier classes stack via the persistent/transient split).
- The terminal `hit` event adding a green flash that's distinct from
  both the `--active` emerald (which `visit` produces) and the
  `--terminal` amber ring (which is pre-existing) — three concentric
  rings of state on the same final node.
- The consumed-prefix readout finishing at `"car"`, matching the
  searched word.

```d3 widget=trie
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
    {"kind": "hit",   "nodeId": "car",  "msg": "Match — \"car\" is stored in the trie"}
  ]
}
```

### Payload 4 — Search for "ca" — miss (prefix without terminal)

A search trace that ends in `miss` because the destination node exists
but isn't marked terminal — `"ca"` is a prefix of `"cat"` and `"car"`,
but isn't a stored word itself. Exercises:

- The `miss` event flashing a rose halo on a NON-terminal node — the
  ring is absent, distinguishing this from "wrong character" misses
  (which would also be `miss` but on a node that has no matching
  outgoing edge).
- A search ending shorter than the search word's full length is still
  valid — the trace is the cursor's actual path, not necessarily
  consumed-prefix.length == word.length.

```d3 widget=trie
{
  "title": "Search for \"ca\" — miss (prefix exists but isn't a stored word)",
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
    {"kind": "visit", "nodeId": "ca",   "char": "a", "msg": "Follow a — arrived at node ca"},
    {"kind": "miss",  "nodeId": "ca",   "msg": "ca isn't terminal — \"ca\" is a prefix but NOT a stored word"}
  ]
}
```

### Payload 5 — Prefix-walk "ca" — autocomplete-style subtree highlight

A three-word trie (`cat`, `car`, `card`) where a prefix-walk on `"ca"`
marks every descendant of the `ca` node as "matching the prefix" — the
canonical autocomplete operation. Exercises:

- A deeper trie (5 nodes below root, depth 4 at `card`).
- A second-level branching shape (`ca` has two children `cat`/`car`,
  and `car` has one child `card`).
- The `prefix-highlight` event flooding the entire descendant subtree
  with the violet `--in-prefix` paint in one step — including the
  incoming edges of every descendant and the terminal rings on `cat`,
  `car`, `card` (which switch from amber → violet via the layered
  `.trie__node--terminal.trie__node--in-prefix .trie__terminal-ring`
  CSS rule).
- The `ca` prefix node itself getting BOTH `--active` (cursor sitting
  on it) AND `--in-prefix` (it's part of its own subtree) — the active
  emerald wins for the node circle while the in-prefix paint propagates
  to the edges below.
- Scrubbing backwards to step 3 (`visit ca`) removes the violet
  highlight from the subtree — the in-prefix set is persistent during
  forward play but the renderer recomputes from `events[0..stepIdx]`
  on every step, so scrubbing back gives a clean re-render.

```d3 widget=trie
{
  "title": "Prefix-walk \"ca\" — highlight all words below the prefix",
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
    {"kind": "visit",            "nodeId": "ca",   "char": "a", "msg": "Follow a — prefix \"ca\" matched"},
    {"kind": "prefix-highlight", "nodeId": "ca",   "msg": "Highlight all descendants — completions: cat, car, card"}
  ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` for this
widget (source has no trie diagrams; this guidance applies to any
future radix / suffix-trie chapter that authors trie payloads from
scratch), target **4-10 widget steps per operation**:

- **Per-character walk frames** in source (highlight edge → highlight
  child → move cursor → advance) → fold into a single `visit` or
  `create` event. The renderer paints the active emerald halo, the
  active-character pill, and the consumed-prefix accumulation in one
  step.
- **End-of-word multi-frame ring fade-in** → one `terminal` event. The
  CSS opacity transition over 300ms handles the visual.
- **Search-success multi-frame pulse** → one `hit` event. The
  green flash + ring preservation are layered automatically.
- **Subtree highlight ripple** (the source's "halo fades in from prefix
  node outward over N frames") → one `prefix-highlight` event. The
  whole subtree paints together — sequencing the ripple frame-by-frame
  would be visually noisy in the catalog and adds no information.
- **Animation-only frames** (the source's "node bounce on creation",
  "edge dash-stroke draw-in") → not modelled. The widget's standard
  enter transition (fade-in over 350ms + the `--new` adornment paint)
  does the same job in one step.

A typical insert trace is 4-6 events (one `visit` per shared prefix
character + one `create` per new suffix character + one `terminal`); a
typical search is 3-5 events (one `visit` per consumed character + one
`hit`/`miss`); a typical prefix-walk is 3-5 events (one `visit` per
prefix character + one `prefix-highlight`).

## Browser verification

Open this chapter at `http://localhost:5173/cortex/data-structures-and-algorithms/appendix-widget-catalog-trie` and:

1. Exercise step controls on each payload (Prev / Next / Play / Pause / Reset).
2. Confirm Payload 1's 5 steps walk the create ladder: step 1 shows ONLY the root node (with `*` inside) as the trie is empty. Step 2 reveals `c` (fades in with emerald `--new` paint, edge `root→c` carries `c` label). Step 3 reveals `ca` (with `a` label on its edge). Step 4 reveals `cat` (with `t` label on its edge). Step 5 lights up the amber terminal ring around `cat` without moving the cursor. The consumed readout reads `""` → `"c"` → `"ca"` → `"cat"` → `"cat"` across the 5 steps.
3. Confirm Payload 2's prefix reuse: at step 0 (before play), the trie shows ALL four nodes `root → c → ca → cat` with `cat`'s amber terminal ring already drawn (pre-existing). As the cursor walks `root → c → ca` (steps 1-3), the active emerald halo moves but `cat`'s amber ring stays drawn the entire time (terminal-status preservation via the persistent layer split). Step 4 reveals `car` as a sibling of `cat` (emerald `--new` paint, edge `ca→car` carries `r` label). Step 5 adds the amber ring to `car`.
4. Confirm Payload 3's search hit: at step 0, both `cat` and `car` carry amber terminal rings. Cursor walks `root → c → ca → car` (steps 1-4) — `car`'s amber ring stays drawn even when the cursor sits on `c` and `ca`. At step 5 (`hit car`), `car` gets a green flash on top of its amber ring (THREE concentric layers visible: green flash circle fill, emerald active halo from the prior visit's persistence in this last step, and the amber terminal ring outside). The operation badge reads `search` (blue).
5. Confirm Payload 4's search miss: cursor walks `root → c → ca` (steps 1-3). At step 4 (`miss ca`), `ca` gets a rose flash — and crucially, `ca` does NOT have a terminal ring (it never did), so the visual reads as "wrong endpoint" not "wrong terminal". The consumed readout finishes at `"ca"`.
6. Confirm Payload 5's prefix-walk: at step 0, all six nodes (`root`, `c`, `ca`, `cat`, `car`, `card`) are visible; `cat`, `car`, `card` carry amber terminal rings. Cursor walks `root → c → ca` (steps 1-3). At step 4 (`prefix-highlight ca`), the entire `ca` subtree (the node itself + `cat` + `car` + `card`) paints with the violet `--in-prefix` halo: node circles get the violet stroke, incoming edges (`ca→cat`, `ca→car`, `car→card`) brighten violet, and the terminal rings on `cat`/`car`/`card` flip from amber to violet (via the layered `.trie__node--terminal.trie__node--in-prefix .trie__terminal-ring` rule). The `ca` node itself gets BOTH the emerald active halo (cursor still there) AND the violet in-prefix circle fill. Scrub back to step 3 — the violet highlight disappears from the entire subtree (terminal rings revert to amber) because the renderer recomputes state from `events[0..stepIdx]`.
7. Confirm the operation badge tint matches the operation: emerald for insert (Payloads 1, 2), blue for search (Payloads 3, 4), violet for prefix-walk (Payload 5). The badge subtitle shows the quoted word (`"cat"`, `"car"`, etc.).
8. Confirm the active-character pill — a small amber rounded rectangle with white text — floats just above the active node on `visit` and `create` events that carry a `char` field, and disappears on `terminal` / `hit` / `miss` / `prefix-highlight` events (which don't consume a character).
9. Confirm no `.d3-widget__error` divs render in the page.
10. Confirm devtools console is clean: no widget exceptions; no `trie:` warnings (this widget doesn't use marker canon — node status is computed).

If any payload fails, fix and re-verify before committing.
