# Widget Spec — `skip-list`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise a probabilistic multi-level sorted linked list. Each node carries an
intrinsic height (number of express-lane levels it participates in), assigned
by repeated coin flips at insertion. The widget animates the two operations
that make a skip list pedagogically interesting:

1. **Upward-descending search.** Start at the highest level of the head;
   walk right until the next node's key exceeds the target; drop a level;
   repeat until you land at level 0. The animated trace makes the
   "halve the search space per level" intuition concrete.
2. **Random-level coin-flip on insert.** A new node's height is decided by
   flipping coins until a tail (`p = 0.5` by default). The widget exposes
   the flip outcomes so the reader sees *why* a particular node ended up
   at height 3 rather than 1 — the probabilistic core of the structure.

Distinct enough from `linked-list` (single-level flat chain with no
height concept) to warrant a standalone widget; the layered express-lane
rendering and the coin-flip animation would balloon `linked-list`'s
schema (decision logged in `diagram-gap-audit.md` § Conditional widget
decisions).

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

The DSA Lesson Source has no skip-list chapter; the destination chapter
`09-probabilistic-and-advanced/01-skip-list.md` is wholly destination-authored.
Payload milestones come from the chapter's worked examples (the inline
`flowchart LR` 4-level diagram in § The structure, the `search` /
`insert` / `delete` pseudocode in § Search and § Insert and delete, and the
"why expected O(log n)" derivation).

## 3. Destination chapter usage

- **Primary owner.** `09-probabilistic-and-advanced/01-skip-list.md` —
  three instances:
  - § The structure: static 4-level skip list (no animation, frame index 0
    only) to anchor the layered visual.
  - § Search: animated descend from top-level head down to target node
    (10 nodes, 4 levels, search for key 30 — the chapter's worked example).
  - § Insert and delete: animated insert of a new key with the coin-flip
    sub-animation visible per step.
- **Reuse in `11-dsa-in-real-systems/05-lsm-trees-rocksdb-cassandra.md`.**
  The LSM chapter's memtable section explicitly cites a skip list as
  RocksDB's memtable structure; a single static instance there suffices.
- **Possible reuse in `10-concurrency-and-systems/03-concurrent-hash-map.md`.**
  If we add a sidebar on Java's `ConcurrentSkipListMap`, one static
  instance.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  maxLevel: number,             // height of head node; usually 4 for didactic
  head: string,                 // node id of the head (level >= maxLevel)
  nil:  string,                 // node id of the trailing sentinel
  nodes: Array<{
    id:     string,
    key:    string | number,    // displayed inside the node box
    height: number,             // 1..maxLevel — how many express lanes
    style?: "new" | "removed" | "highlight"
  }>,
  steps: Array<{
    // For an animated step, every field below is optional and overrides
    // the previous step's state. For the static-frame use the steps array
    // length 1 with the full layout snapshot.
    msg:    string,
    // Per-level link overrides. Each entry is one level's complete
    // forward-pointer chain in left-to-right order.
    links?: Array<{
      level: number,
      chain: Array<string>      // node ids; first must be head, last must be nil
    }>,
    // Search/insert traversal cursor. One named marker; colour is canon-fixed
    // (see § 8). Renders as a small diamond on the upper-left corner of the
    // node it points to, with the current level number badge.
    cursor?: {
      nodeId: string,
      level:  number,
      name:   "search" | "insert" | "delete"
    },
    // Coin-flip overlay for insert steps. Renders a small inset panel
    // showing the sequence of H/T flips above the new node, with the
    // tail that terminated the flips highlighted.
    coinFlip?: {
      nodeId: string,           // the node whose height is being decided
      flips:  Array<"H" | "T">, // chronological order; tail terminates
      finalHeight: number       // = (count of H before first T) + 1
    },
    // Add / remove visualisation. New nodes render with style "new" until
    // the next step that omits them from this list; removed nodes fade.
    addedNodes?:   Array<string>,
    removedNodes?: Array<string>
  }>,
  sections?: Array<{ name: string, startIdx: number }>  // optional macro phases
}
```

Validation rules (raised as `Left(msg)` parse errors, render inline):

- `head` and `nil` must be present in `nodes`.
- For every `step.links[].chain`, the first id must be `head` and the last
  must be `nil`.
- `cursor.level` must satisfy `0 <= level < maxLevel`.
- `coinFlip.finalHeight` must equal `flips.takeWhile(_ == "H").length + 1`
  (else parse-time `invalid("flip count mismatches finalHeight")`).
- Per-node `height` must satisfy `1 <= height <= maxLevel`.

## 5. POC payloads

### 5a. Static four-level structure (§ The structure)

```d3 widget=skip-list
{
  "title": "Four-level skip list — 10 keys, head/nil sentinels",
  "maxLevel": 4,
  "head": "H",
  "nil":  "T",
  "nodes": [
    { "id": "H",  "key": "H",  "height": 4 },
    { "id": "n10", "key": 10, "height": 1 },
    { "id": "n20", "key": 20, "height": 2 },
    { "id": "n30", "key": 30, "height": 1 },
    { "id": "n50", "key": 50, "height": 3 },
    { "id": "n70", "key": 70, "height": 1 },
    { "id": "T",  "key": "nil", "height": 4 }
  ],
  "steps": [
    {
      "msg": "Level 3 skips straight to 50; level 0 holds every key.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ]
    }
  ]
}
```

### 5b. Search for key 30 (§ Search — upward-descending walk)

```d3 widget=skip-list
{
  "title": "search(30) — descend from level 3 to level 0",
  "maxLevel": 4,
  "head": "H",
  "nil":  "T",
  "nodes": [
    { "id": "H",  "key": "H",  "height": 4 },
    { "id": "n10", "key": 10, "height": 1 },
    { "id": "n20", "key": 20, "height": 2 },
    { "id": "n30", "key": 30, "height": 1, "style": "highlight" },
    { "id": "n50", "key": 50, "height": 3 },
    { "id": "n70", "key": 70, "height": 1 },
    { "id": "T",  "key": "nil", "height": 4 }
  ],
  "steps": [
    {
      "msg": "Start at H, level 3. Next is 50 — too big (50 > 30). Drop a level.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ],
      "cursor": { "nodeId": "H", "level": 3, "name": "search" }
    },
    {
      "msg": "At H, level 2. Next is 20 (20 <= 30) — advance.",
      "cursor": { "nodeId": "H", "level": 2, "name": "search" }
    },
    {
      "msg": "At 20, level 2. Next is 50 — too big. Drop a level.",
      "cursor": { "nodeId": "n20", "level": 2, "name": "search" }
    },
    {
      "msg": "At 20, level 1. Next is 50 — too big. Drop a level.",
      "cursor": { "nodeId": "n20", "level": 1, "name": "search" }
    },
    {
      "msg": "At 20, level 0. Next is 30 — advance.",
      "cursor": { "nodeId": "n20", "level": 0, "name": "search" }
    },
    {
      "msg": "At 30. Match — return.",
      "cursor": { "nodeId": "n30", "level": 0, "name": "search" }
    }
  ]
}
```

### 5c. Insert key 25 with coin-flip height selection (§ Insert and delete)

```d3 widget=skip-list
{
  "title": "insert(25) — coin-flips decide height = 2, splice into levels 0 and 1",
  "maxLevel": 4,
  "head": "H",
  "nil":  "T",
  "nodes": [
    { "id": "H",  "key": "H",  "height": 4 },
    { "id": "n10", "key": 10, "height": 1 },
    { "id": "n20", "key": 20, "height": 2 },
    { "id": "n25", "key": 25, "height": 2, "style": "new" },
    { "id": "n30", "key": 30, "height": 1 },
    { "id": "n50", "key": 50, "height": 3 },
    { "id": "n70", "key": 70, "height": 1 },
    { "id": "T",  "key": "nil", "height": 4 }
  ],
  "steps": [
    {
      "msg": "search(25) records the predecessor at every level.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ],
      "cursor": { "nodeId": "n20", "level": 0, "name": "insert" }
    },
    {
      "msg": "Coin flip: H (continue), T (stop). Height = 2 → splice into levels 0 and 1.",
      "coinFlip": { "nodeId": "n25", "flips": ["H","T"], "finalHeight": 2 }
    },
    {
      "msg": "Splice into level 0 between 20 and 30.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n25","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ],
      "addedNodes": ["n25"]
    },
    {
      "msg": "Splice into level 1 between 20 and 50.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n25","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n25","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ]
    }
  ]
}
```

### 5d. Insert key 80 with a "lucky" 4-level coin-flip

```d3 widget=skip-list
{
  "title": "insert(80) — H,H,H,T → height = 4 (lucky)",
  "maxLevel": 4,
  "head": "H",
  "nil":  "T",
  "nodes": [
    { "id": "H",  "key": "H",  "height": 4 },
    { "id": "n10", "key": 10, "height": 1 },
    { "id": "n20", "key": 20, "height": 2 },
    { "id": "n50", "key": 50, "height": 3 },
    { "id": "n70", "key": 70, "height": 1 },
    { "id": "n80", "key": 80, "height": 4, "style": "new" },
    { "id": "T",  "key": "nil", "height": 4 }
  ],
  "steps": [
    {
      "msg": "Three heads then a tail — height 4.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ],
      "coinFlip": { "nodeId": "n80", "flips": ["H","H","H","T"], "finalHeight": 4 }
    },
    {
      "msg": "Splice 80 into all four levels.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n50","n70","n80","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","n80","T"] },
        { "level": 2, "chain": ["H","n20","n50","n80","T"] },
        { "level": 3, "chain": ["H","n50","n80","T"] }
      ],
      "addedNodes": ["n80"]
    }
  ]
}
```

### 5e. Delete key 50 (multi-level unlink)

```d3 widget=skip-list
{
  "title": "delete(50) — unlink from every level it appears in (3 levels)",
  "maxLevel": 4,
  "head": "H",
  "nil":  "T",
  "nodes": [
    { "id": "H",  "key": "H",  "height": 4 },
    { "id": "n10", "key": 10, "height": 1 },
    { "id": "n20", "key": 20, "height": 2 },
    { "id": "n30", "key": 30, "height": 1 },
    { "id": "n50", "key": 50, "height": 3, "style": "removed" },
    { "id": "n70", "key": 70, "height": 1 },
    { "id": "T",  "key": "nil", "height": 4 }
  ],
  "steps": [
    {
      "msg": "Search records the predecessor at every level.",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n30","n50","n70","T"] },
        { "level": 1, "chain": ["H","n20","n50","n70","T"] },
        { "level": 2, "chain": ["H","n20","n50","T"] },
        { "level": 3, "chain": ["H","n50","T"] }
      ],
      "cursor": { "nodeId": "n30", "level": 0, "name": "delete" }
    },
    {
      "msg": "Unlink from all three levels (heights 0, 1, 2).",
      "links": [
        { "level": 0, "chain": ["H","n10","n20","n30","n70","T"] },
        { "level": 1, "chain": ["H","n20","n70","T"] },
        { "level": 2, "chain": ["H","n20","T"] },
        { "level": 3, "chain": ["H","T"] }
      ],
      "removedNodes": ["n50"]
    }
  ]
}
```

## 6. Closest existing widget to mimic

- **`LinkedList.scala`** is the structural ancestor for the node-and-arrow
  rendering on a single level. Reuse: D3 keyed join on `node.id`, marker
  decoration on the upper-left of a node, the parse-time canon validation
  pattern for marker names + colours (ADR-0016 analogue).
  - **Diverges from `linked-list`** in: multi-level layout (Y-axis = level
    index), per-level link chains, height-bearing nodes (tall rectangles
    spanning level rows), coin-flip overlay subcomponent.
- Not appropriate to extend `linked-list` itself — see § 1 and the
  conditional-widget decision in `diagram-gap-audit.md`.
- The existing system-design widgets (`BTreeWalker`, `RaftAnimator`,
  `CacheStampedeSimulator`) don't share enough machinery to be worth
  modelling on; they're snapshot-style stringly-typed SVG, not D3 keyed
  joins.

## 7. D3 selections plan

- Host `<svg>` with `viewBox = "0 0 ${width} ${height}"` where
  `height = padding * 2 + maxLevel * levelGap + nodeHeaderH + cursorLaneH`.
- **Node group selection** keyed by `node.id`.
  - `<rect>` per node, full height = `node.height * levelGap` so taller
    nodes visually span the express lanes they participate in.
  - `<text>` for the key, centred at the bottom (level-0) face.
- **Per-level link selection** keyed by `${level}-${from}-${to}`.
  - Forward arrows drawn as straight horizontal segments at
    `y = paddingTop + (maxLevel - 1 - level) * levelGap + nodeWidth/2`.
  - Arrowhead `<marker>` definition once, shared.
- **Cursor selection** (1 element max).
  - Small diamond decoration on the named level row of the cursor node.
  - Level-number badge to the left.
- **Coin-flip overlay** (1 group max, visible only when step has
  `coinFlip`).
  - Sequence of H/T circles drawn above the target node; tail-circle
    coloured rose, heads coloured emerald.
- Transitions: 350 ms for link rewires; 250 ms fade-in for new nodes;
  coin-flip circles staggered 120 ms each.
- Sentinel handling: `head` and `nil` always render at maxLevel height
  regardless of declared `height` (sentinels are taller than the tallest
  data node by convention).

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset + section dividers (reuse
  as-is; same machinery `linked-list` and `array-traversal` use).
- **`PayloadDecoder.scala`** for `js.Dynamic` field extraction with
  collapse-to-`Left(msg)` semantics.
- **Canon marker module** mirroring ADR-0016 — the three cursor names
  (`search`, `insert`, `delete`) each get a single canonical colour:
  - `search`  → `#3b82f6` (blue)
  - `insert`  → `#10b981` (emerald)
  - `delete`  → `#ef4444` (rose)
- **Canon node styles** (`new`, `removed`, `highlight`) — same set as
  `linked-list`; reuse the validator helper.
- **Coin-flip subcomponent** is novel; lives in
  `widgets/skipList/CoinFlipOverlay.scala` (file-local private object)
  and is owned wholly by this widget — no cross-widget reuse expected
  unless another probabilistic structure later needs it.
- **`LucideIcons.scala`** for the cursor diamond and the coin-flip H/T
  glyphs (or plain text — final call at build time).

## 9. Estimated build session count

**2 sessions.**

- Session 1: schema + parsing + single-step static render across all 5
  POC payloads. Lift node-and-link rendering from `LinkedList.scala`
  patterns; add the level-row vertical layout. Verify the static
  4-level structure renders correctly at chapter URL.
- Session 2: cursor decoration, coin-flip overlay, transitions, demo
  book chapter, scalafmt + verification gates. Wire dispatcher case in
  `D3WidgetBlock.scala`.

Risk: the coin-flip overlay may pull a third session if visual polish
matters (the entire pedagogy of insert hinges on it). Budget extra at
session 2; defer to a follow-up only if the basic overlay reads clearly.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/skip-list.md` — exhibits all 5 POC
payloads above (static 4-level, search descend, insert with height = 2,
insert with height = 4 lucky flip, multi-level delete). Each payload
gets a 1-sentence caption naming which destination chapter it backs.
