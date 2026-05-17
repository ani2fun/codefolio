# Widget Spec — `dsu-forest`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise the **Disjoint Set Union (Union-Find)** forest evolving across a
sequence of `find` and `union` operations. The widget renders a
*parent-pointer forest* — each element is a circle, the parent pointer is
an upward arrow, and roots are crowned with a rank/size badge.

Two paired animation overlays make the textbook optimisations visible:

1. **Path compression overlay** — `find(x)` traces a walk up to the root,
   then re-parents every node on the walk directly to the root. The
   widget animates this in two beats: first the walk (orange highlight
   on each visited node), then the flatten (arrows snap from old parent
   to root with a fade transition on the discarded edges).
2. **Union-by-rank/size overlay** — `union(x, y)` shows both roots
   pulsing, the rank/size badges briefly highlighted, then the smaller
   tree's root reparented under the larger root. The merged tree
   shows the new combined rank/size.

The widget supports a **naive vs optimised toggle** at the spec level
(`mode: "naive" | "compress" | "union-by-rank" | "both"`), so the chapter
can show the same sequence of operations on two adjacent widget
instances and contrast tree-height growth.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

DSU has no source counterpart in the Lesson Source's data-structure
phases; it's an orphan in the destination's
`03-trees/11-disjoint-set-union/` section. Payloads are authored from
the destination chapter's prose at
`content/cortex/data-structures-and-algorithms/03-trees/11-disjoint-set-union/01-introduction-to-disjoint-set-union.md`
(mermaid sketches on lines 78-181, implementation on lines 233-378,
edge cases on lines 381-398).

## 3. Destination chapter usage

| Chapter | Sections needing widget instance |
|---|---|
| `03-trees/11-disjoint-set-union/01-introduction-to-disjoint-set-union.md` | "DSU: parent pointers" (one before/after union), "Path compression" (naive vs compressed walk), "Union by rank (or size)" (rank-comparison demo), "Implementation" (multi-edge demo over 10 elements) |
| `03-trees/11-disjoint-set-union/02-operations…` (future) | Iterative path compression, union by size variant, component-size-augmentation demo |
| `04-graphs/04-minimum-spanning-trees.md` (when authored, reuses widget) | Kruskal's edge-sort + union sequence over a sample weighted graph |
| `04-graphs/02-cycle-detection.md` (reuses widget) | "Same root → cycle" demo |

Total at landing: **4** in introduction; **2-3 more** reused in graph
chapters once those orphans are filled in.

## 4. Payload schema sketch

```ts
{
  title:    string,
  mode:     "naive" | "compress" | "union-by-rank" | "both",
                                    // affects which optimisation overlays render
  nodes: Array<{ id: string, label?: string }>,
                                    // labels default to id; e.g. "a", "b", or "0", "1"
  steps: Array<{
    op:     "find" | "union" | "init" | "highlight",
    args:   string[],               // ["x"] for find; ["x", "y"] for union
    parent: Record<string, string>, // current parent map (post-step state)
    rank:   Record<string, number>, // optional; only when mode contains union-by-rank
    size?:  Record<string, number>, // optional; only when mode = "union-by-size" extension
    walkPath?: string[],            // for find: nodes visited on the walk-up
    compressFrom?: string,          // for find: which node compressed first (animation key)
    unionRoots?: { rx: string, ry: string, winner: string },
                                    // for union: which root won, for the rank-badge pulse
    msg:     string,
  }>,
  layout?: {
    levelSpacing?: number,          // default 60 px
    nodeSpacing?:  number,          // default 64 px
  }
}
```

**Layout algorithm.** Each step rebuilds the forest from the `parent`
map: roots (where `parent[x] = x`) are placed at the top of their tree;
descendants are placed at depth `depth(parent) + 1` and laid out under
their parent. The widget computes a deterministic left-to-right slot
ordering so a node that survives a union keeps its horizontal position
(D3 join keyed by `id`); arrows transition smoothly when re-parented.

**Two-beat union animation.** Each `union` step actually unrolls into
two visual frames: (a) both roots pulsing + rank badges briefly
enlarged; (b) the loser's parent pointer transitioning across the gap
to point at the winner. The Stepper exposes both as a single step from
the author's POV — the widget internally chains the two transitions.

**Two-beat find-with-compression animation.** Similarly, a `find` in
`mode: "compress"` plays as: (a) walk up — sequential highlight of each
walked node; (b) flatten — all walked nodes' parent arrows snap from
their old target to the root.

## 5. POC payloads

### POC-1 — Naive parent pointers, no optimisations

(Demonstrates the chapter's "DSU: parent pointers" section. Mirrors the
mermaid sketch's `before union(c, f)` / `after union(c, f)` pair.)

````markdown
```d3 widget=dsu-forest
{
  "title": "Naive union(c, f) — make root of e a child of root of c",
  "mode": "naive",
  "nodes": [
    { "id": "a" }, { "id": "b" }, { "id": "c" },
    { "id": "d" }, { "id": "e" }, { "id": "f" }, { "id": "g" }
  ],
  "steps": [
    {
      "op": "init", "args": [],
      "parent": { "a":"d", "b":"a", "c":"a", "d":"d", "e":"e", "f":"e", "g":"e" },
      "rank": {},
      "msg": "Initial forest: tree rooted at d holds {a, b, c, d}; tree rooted at e holds {e, f, g}."
    },
    {
      "op": "find", "args": ["c"], "walkPath": ["c", "a", "d"],
      "parent": { "a":"d", "b":"a", "c":"a", "d":"d", "e":"e", "f":"e", "g":"e" },
      "rank": {},
      "msg": "find(c) walks c → a → d. Returns root d."
    },
    {
      "op": "find", "args": ["f"], "walkPath": ["f", "e"],
      "parent": { "a":"d", "b":"a", "c":"a", "d":"d", "e":"e", "f":"e", "g":"e" },
      "rank": {},
      "msg": "find(f) walks f → e. Returns root e."
    },
    {
      "op": "union", "args": ["c", "f"], "unionRoots": { "rx":"d", "ry":"e", "winner":"d" },
      "parent": { "a":"d", "b":"a", "c":"a", "d":"d", "e":"d", "f":"e", "g":"e" },
      "rank": {},
      "msg": "Reparent root e under root d. Both trees now share root d. No rank tracked (naive)."
    }
  ]
}
```
````

### POC-2 — Path compression: chain of 5 collapses to depth 1

(Mirrors the chapter's `BEFORE / AFTER find(g)` mermaid — chain
`root → a → b → c → d → g`. After `find(g)` with path compression every
walked node points directly to root.)

````markdown
```d3 widget=dsu-forest
{
  "title": "Path compression: find(g) flattens a length-5 chain",
  "mode": "compress",
  "nodes": [
    { "id": "root" }, { "id": "a" }, { "id": "b" },
    { "id": "c" }, { "id": "d" }, { "id": "g" }
  ],
  "steps": [
    {
      "op": "init", "args": [],
      "parent": { "root":"root", "a":"root", "b":"a", "c":"b", "d":"c", "g":"d" },
      "rank": {},
      "msg": "Chain of length 5. find(any leaf) currently O(5)."
    },
    {
      "op": "find", "args": ["g"], "walkPath": ["g", "d", "c", "b", "a", "root"],
      "compressFrom": "g",
      "parent": { "root":"root", "a":"root", "b":"a", "c":"b", "d":"c", "g":"d" },
      "rank": {},
      "msg": "find(g) walks up: g → d → c → b → a → root. 5 hops."
    },
    {
      "op": "find", "args": ["g"], "walkPath": [], "compressFrom": "g",
      "parent": { "root":"root", "a":"root", "b":"root", "c":"root", "d":"root", "g":"root" },
      "rank": {},
      "msg": "Path compression: every walked node now points directly to root. Future find on any of {a, b, c, d, g} is O(1)."
    },
    {
      "op": "find", "args": ["c"], "walkPath": ["c", "root"],
      "parent": { "root":"root", "a":"root", "b":"root", "c":"root", "d":"root", "g":"root" },
      "rank": {},
      "msg": "find(c) — 1 hop. Compression amortises subsequent queries."
    }
  ]
}
```
````

### POC-3 — Union by rank: smaller-rank root attaches under larger

````markdown
```d3 widget=dsu-forest
{
  "title": "Union by rank — taller tree wins; rank only grows on tie",
  "mode": "union-by-rank",
  "nodes": [
    { "id": "0" }, { "id": "1" }, { "id": "2" }, { "id": "3" }, { "id": "4" }
  ],
  "steps": [
    {
      "op": "init", "args": [],
      "parent": { "0":"0", "1":"0", "2":"0", "3":"3", "4":"3" },
      "rank":   { "0": 1, "1": 0, "2": 0, "3": 1, "4": 0 },
      "msg": "Two trees: {0, 1, 2} with rank[0]=1; {3, 4} with rank[3]=1. Equal ranks."
    },
    {
      "op": "union", "args": ["1", "4"], "unionRoots": { "rx":"0", "ry":"3", "winner":"0" },
      "parent": { "0":"0", "1":"0", "2":"0", "3":"0", "4":"3" },
      "rank":   { "0": 2, "1": 0, "2": 0, "3": 1, "4": 0 },
      "msg": "union(1, 4): roots 0 and 3, ranks equal — pick 0 as winner, attach 3 under 0, rank[0] increments to 2."
    },
    {
      "op": "union", "args": ["0", "new5"], "unionRoots": { "rx":"0", "ry":"new5", "winner":"0" },
      "parent": { "0":"0", "1":"0", "2":"0", "3":"0", "4":"3", "new5":"0" },
      "rank":   { "0": 2, "1": 0, "2": 0, "3": 1, "4": 0, "new5": 0 },
      "msg": "union(0, new5): rank[0]=2 > rank[new5]=0. new5 attaches under 0. rank[0] unchanged — the taller tree absorbed the shorter."
    }
  ]
}
```
````

(Note: POC-3 introduces `new5` to demonstrate the rank-doesn't-grow case;
the widget treats nodes referenced only in later steps as auto-introduced
roots.)

### POC-4 — Both optimisations together: 10-node chain + 5-edge merge sequence

(Demonstrates the chapter's Python `__main__` driver — 10 elements, 5
unions, then a same-set query — as a single multi-step animation.)

````markdown
```d3 widget=dsu-forest
{
  "title": "DSU with both optimisations — 10 elements, 5 unions, 4 components",
  "mode": "both",
  "nodes": [
    { "id":"0" }, { "id":"1" }, { "id":"2" }, { "id":"3" }, { "id":"4" },
    { "id":"5" }, { "id":"6" }, { "id":"7" }, { "id":"8" }, { "id":"9" }
  ],
  "steps": [
    {
      "op":"init", "args":[],
      "parent": { "0":"0","1":"1","2":"2","3":"3","4":"4","5":"5","6":"6","7":"7","8":"8","9":"9" },
      "rank":   { "0":0,"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0 },
      "msg":"10 elements, each its own set. 10 components."
    },
    {
      "op":"union", "args":["0","1"], "unionRoots":{ "rx":"0","ry":"1","winner":"0" },
      "parent": { "0":"0","1":"0","2":"2","3":"3","4":"4","5":"5","6":"6","7":"7","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0 },
      "msg":"union(0, 1): tie → 1 under 0, rank[0]=1."
    },
    {
      "op":"union", "args":["1","2"], "unionRoots":{ "rx":"0","ry":"2","winner":"0" },
      "parent": { "0":"0","1":"0","2":"0","3":"3","4":"4","5":"5","6":"6","7":"7","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0 },
      "msg":"union(1, 2): find(1)=0, find(2)=2. rank[0]>rank[2] → 2 under 0. rank unchanged."
    },
    {
      "op":"union", "args":["3","4"],
      "parent": { "0":"0","1":"0","2":"0","3":"3","4":"3","5":"5","6":"6","7":"7","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":1,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0 },
      "msg":"union(3, 4): tie → 4 under 3, rank[3]=1."
    },
    {
      "op":"union", "args":["5","6"],
      "parent": { "0":"0","1":"0","2":"0","3":"3","4":"3","5":"5","6":"5","7":"7","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":1,"4":0,"5":1,"6":0,"7":0,"8":0,"9":0 },
      "msg":"union(5, 6): tie → 6 under 5, rank[5]=1."
    },
    {
      "op":"union", "args":["6","7"],
      "parent": { "0":"0","1":"0","2":"0","3":"3","4":"3","5":"5","6":"5","7":"5","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":1,"4":0,"5":1,"6":0,"7":0,"8":0,"9":0 },
      "msg":"union(6, 7): find(6)=5, find(7)=7. rank[5]>rank[7] → 7 under 5."
    },
    {
      "op":"find", "args":["0","2"], "walkPath":["2","0"],
      "parent": { "0":"0","1":"0","2":"0","3":"3","4":"3","5":"5","6":"5","7":"5","8":"8","9":"9" },
      "rank":   { "0":1,"1":0,"2":0,"3":1,"4":0,"5":1,"6":0,"7":0,"8":0,"9":0 },
      "msg":"same_set(0, 2)? find(0)=0, find(2)=0. True. 4 components remain: {0,1,2}, {3,4}, {5,6,7}, {8}, {9}."
    }
  ]
}
```
````

## 6. Closest existing widget to mimic

- **`linked-list`** for the **node-with-circle + curved-edge** primitive
  and the **per-node ID-keyed D3 join** (so a node that survives a union
  doesn't flicker). The cycle-back edge bezier (used in Floyd
  visualisations) is a near-perfect template for the upward parent
  arrows in a multi-level tree.
- **`call-stack`** (Tier 1, not yet built but spec'd) for the **upward
  hierarchy layout**. Since `call-stack` doesn't exist yet, mimic the
  call-stack-mode behaviour in the `binary-tree` widget once it lands;
  for now, hand-roll a tidy-tree layout via `d3-hierarchy.tree()`.

The widget is more layout-heavy than `linked-list` (multi-level tree)
but shares the same edge-arrow rendering style.

## 7. D3 selections plan

| Layer | Selector | Bound data | Update on step |
|---|---|---|---|
| Tree containers | `g.dsu-forest__tree` keyed by root id | One per current root | enter slides in from below; exit fades; tree position computed by tidy-layout |
| Nodes | `g.dsu-forest__nodes` ← `g.dsu-forest__node` keyed by `node.id` | `nodes` (all, regardless of current root) | transform transition between (tree, depth, slot) coordinates — driven by recomputed layout |
| Parent edges | `g.dsu-forest__edges` ← `path.dsu-forest__edge` keyed by `${child}→${parent}` | derived from `parent` map | enter: fade-in along path; update: path interpolation when the target changes (smooth re-aim); exit: fade-out when path-compressed |
| Rank/size badges | `g.dsu-forest__badges` ← `text.dsu-forest__badge` keyed by `root` | filtered to nodes where `parent[id] = id` | text update with `tween` between numeric values; flash background on union step |
| Walk-trail highlight | `g.dsu-forest__walk` ← `circle.dsu-forest__walk-dot` keyed by `step + node` | `step.walkPath` | enter sequentially (200 ms staggered); all exit on next step |
| Compression flash | overlay arrows for old-parent → root | derived from `compressFrom` | brief 300 ms dashed-stroke flash before edges snap to compressed state |
| Caption | `div.dsu-forest__msg` | `step.msg` | reactive React render |

D3 transitions:
- Edge re-aim on union: `path` `d` attribute interpolated via
  `d3.interpolateString` (smooth bezier morph).
- Path-compression flatten: stagger 80 ms per edge; old-edge dashed
  fade-out concurrent with new-edge fade-in.
- Rank/size badge: scale 1 → 1.3 → 1 over 400 ms when the badge value
  changes.

## 8. Shared abstractions

| Lift | From / To |
|---|---|
| `Stepper.scala` | Reuse as-is for prev/next/play/reset |
| `PayloadDecoder.scala` | Reuse `d.dynList`, `d.string`, `d.stringList`; add `d.intMap` / `d.stringMap` helper for `parent` and `rank` (new — also useful for `dp-table`) |
| Tidy-tree layout | `d3.hierarchy()` + `d3.tree()` from `d3-hierarchy`; bounded `nodeSize` for predictable spacing |
| Node-with-circle SVG primitive | Lift from `LinkedList` — same 56 px circle + label text composition |
| Parent-arrow bezier | Lift from `LinkedList`'s back-edge variant; orient apex above the child node |
| Canon: marker colours | Roots: amber `#f59e0b` (matches the mermaid sketches in the destination); walk-trail: blue `#3b82f6`; flatten-flash: emerald `#10b981`; loser of union: slate `#64748b` |
| BEM block | New file `client/src/styles/components/dsu-forest.css`; mirror `linked-list.css` structure (`__tree`, `__node`, `__edge`, `__badge`, `__walk-dot`, `--root`, `--walked`, `--flattened`, `--winner`, `--loser`) |

## 9. Estimated build session count

**2 sessions.**

Session 1 (≈ 5-7 hours): Scaffold the Scala module, port the
node-circle + bezier-edge rendering from `LinkedList`, wire the
tidy-tree layout via `d3-hierarchy`. Get POC-1 (naive) rendering with
correct multi-tree layout and a smooth union animation.

Session 2 (≈ 4-5 hours): Add the path-compression two-beat animation,
the union-by-rank badge pulse, write the BEM CSS, register in
`D3WidgetBlock.scala`, polish edge transitions, build the demo book
chapter, run the four-gate verification (scalafmt, compile, test, vite
build) + browser smoke.

Higher than `fenwick-tree` (no tidy-tree layout needed there) but
lower than `binary-tree` (no in-place insert/delete rebalancing). The
estimate matches `heap-tree` and `graph-explorer` budgets in
`diagram-gap-audit.md` Wave-B Tier 1.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/03-trees/02-dsu-forest.md` (NEW —
created alongside the widget build session).

Houses all four POC payloads above, each preceded by the analogous
destination prose (naive parent pointers, path-compression mechanic,
union-by-rank tie-breaking, full 10-element merge sequence). Acts as
the living authoring reference. Future schema changes re-render these
four payloads as a smoke test before landing.
