# Unified Visualise pipeline — one renderer, pluggable layouts (Slice 22)

> **Partially reversed by [ADR-0024](0024-bespoke-renderers-reversal.md) (2026-05-28).** The layout
> abstraction below remains in force; the "one renderer" half is reversed — 17 bespoke renderers now
> wrap the layouts in structure-specific chrome, with the generic renderer kept as the fallback for
> anything not yet ported. See ADR-0024 for the cost/benefit data behind the reversal.

The Visualise feature now goes through **one** pipeline end to end: a JSON
`VizGraph` payload (`{steps:[{nodes,edges,cursor,highlight,changed,removed,
annotation,line,...}]}`) is fed to `renderWidget` in
[client/src/d3/index.ts](../../client/src/d3/index.ts), which dispatches to a
`LayoutFn` from the `LAYOUTS` map. Modal canvas and inline chapter diagrams
share the same entry point and the same layouts; the only difference is the
source of the JSON — `HeapToGraph.adapt(trace, …)` produces it for the modal,
chapter authors hand-curate it inside ` ```d3 widget=<layoutKind>` fences.

Before slice 1, two parallel pipelines lived in the codebase: the modal canvas
called a generic D3 renderer driven by a per-trace `layoutHint`, while every
inline chapter widget had its own bespoke `scalajs-react` component
(`ArrayTraversal.scala`, `BinaryTree.scala`, `LinkedList.scala`,
`GraphExplorer.scala`, `HashTable.scala`, `HeapTree.scala`, `Trie.scala`,
`StackQueue.scala`, `CallStack.scala`) carrying its own JSON schema. Every new
data structure paid the cost twice — once in Scala (for the chapter renderer)
and once in TypeScript (when the modal eventually wanted to draw it). The two
pipelines also drifted on conventions: pointer-canon palettes, cell sizing,
caption styling, transition timing.

Slices 1–21 retire the bespoke chapter widgets, build out the layout catalog,
and reframe the documentation. The end state is:

- **13 TypeScript layout files** under `client/src/d3/`:
  [`array-layout.ts`](../../client/src/d3/array-layout.ts),
  [`linked-list-layout.ts`](../../client/src/d3/linked-list-layout.ts),
  [`stack-queue-layout.ts`](../../client/src/d3/stack-queue-layout.ts),
  [`tree-layout.ts`](../../client/src/d3/tree-layout.ts),
  [`graph-layout.ts`](../../client/src/d3/graph-layout.ts),
  [`grid-layout.ts`](../../client/src/d3/grid-layout.ts),
  [`hashmap-layout.ts`](../../client/src/d3/hashmap-layout.ts),
  [`trie-layout.ts`](../../client/src/d3/trie-layout.ts),
  [`union-find-layout.ts`](../../client/src/d3/union-find-layout.ts),
  [`segment-tree-layout.ts`](../../client/src/d3/segment-tree-layout.ts),
  [`fenwick-layout.ts`](../../client/src/d3/fenwick-layout.ts),
  [`bitset-layout.ts`](../../client/src/d3/bitset-layout.ts),
  [`skiplist-layout.ts`](../../client/src/d3/skiplist-layout.ts).
- **One renderer** ([`graph-render.ts`](../../client/src/d3/graph-render.ts))
  consumed by every layout via the `LayoutFn` interface (positions only — D3
  owns transforms, CSS owns paint).
- **One dispatch map** in `index.ts` — `LAYOUTS: Record<string, LayoutFn>` —
  carrying both the new shape-driven keys (`array-1d`, `tree-binary`,
  `list-single`, `list-double`, `hashmap`, `trie`, `stack`, `queue`,
  `union-find`, `segment-tree`, `fenwick`, `bitset`, `skiplist`,
  `array-2d` → grid, `graph-generic`) and the legacy aliases (`array`,
  `binary-tree`, `linked-list`, `grid`, `graph`) so existing chapters keep
  resolving.
- **Zero bespoke trace-driven widget components** — `client/.../widgets/` now
  contains only the 13 parameter-driven simulators that aren't trace-driven
  (BTreeWalker, DecisionTree, DpTable, HandshakeTimeline, ConsistentHashRing,
  RaftAnimator, QueueingSimulator, HotShardSimulator, PartitionSimulator,
  ReplicationLagSimulator, CacheStampedeSimulator, EstimationCalculator,
  LatencyScaledTime). The dispatch fork in `D3WidgetBlock` keeps these on
  their existing Scala components.
- **Catalog book reframed** (slice 21) — every chapter under
  `content/cortex/data-structures-and-algorithms/12-appendix-widget-catalog/`
  reads as **layout** documentation (payload schema, slot convention,
  pointer-canon table) rather than widget documentation.

## Key design choices

- **`VizGraph` is the contract.** A `VizGraph` (Scala:
  [`shared/src/main/scala/codefolio/shared/viz/VizGraph.scala`](../../shared/src/main/scala/codefolio/shared/viz/VizGraph.scala);
  TS mirror: [`client/src/d3/types.ts`](../../client/src/d3/types.ts)) is a
  sequence of `VizGraphStep`s. Each step carries `nodes`, `edges`, `cursor`,
  `highlight`, `changed`, `removed`, `annotation`, and `line`. The schema is
  shared by both directions of the pipeline; adding a structure adds a layout
  (a new TS file + a `LAYOUTS` entry), never a renderer.
- **Slot-based vs edge-based positioning.** Array-family layouts
  (array, stack, queue, grid, fenwick, bitset, segment tree, skip list,
  union-find parents row) place nodes by `slot` — `x = slot * CELL_DX` —
  so a value moves between fixed boxes when an in-place algorithm swaps it,
  and the column *is* the index. Graph-family layouts (tree, linked-list,
  generic graph, trie, hashmap chains) place nodes from the edge set —
  parents above children, head left of tail, force-directed where neither
  applies. The split is a property of the layout, not of the renderer.
- **`LayoutFn` interface.** Each layout is a pure function
  `(nodes, edges) → GraphLayout` returning per-node positions plus the
  viewBox. No DOM, no D3 inside layouts — they're unit-tested in plain
  Node (see `array-layout.test.ts`, `grid-layout.test.ts`,
  `graph-layout.test.ts`, `linked-list-layout.test.ts`). The renderer
  consumes the positions and binds them to SVG.
- **Union-graph then per-step render.** Each layout receives the *union*
  of every node + edge across the entire animation up front; positions
  are computed once. Per-step rendering then toggles visibility,
  cursors, and diff-state classes against the fixed coordinates. This
  keeps element identity stable across steps and lets D3 transitions
  do the right thing — `cell-3` stays at `x=168` whether step 2 highlights
  it or not.
- **Deterministic layout.** The force-directed graph layout uses a fixed
  PRNG seed and a bounded iteration count, so re-opening the modal on the
  same trace redraws the diagram pixel-identically. No "the graph looked
  different last time" surprises.
- **Slot convention.** `CELL_DX = 56` for array-family layouts (room for
  a 22-radius node plus a 4-radius adornment ring plus padding). `slot=0`
  is the **bottom** for stack layouts (so `push` grows upward, matching
  textbook diagrams) and the **front** for queue layouts (so `dequeue`
  comes off the left). These conventions live in the layout files —
  authors don't need to pre-flip indices in the payload.
- **Per-object dispatch (auto-routing).** When `HeapToGraph.adapt(...)`
  produces the JSON (the modal path), each `VizNode` carries `cardId` and
  `layoutKind` annotations: cells of an array share the array's heap-object
  id and `layoutKind="array-1d"`; instances connected by left/right field
  refs share a representative id and `layoutKind="tree-binary"`; an
  isolated dict gets `"hashmap"`; an ambiguous shape falls back to
  `"graph-generic"`. `renderWidget` detects per-object annotations and
  splits the canvas into stacked sub-cards, each routed to its own layout
  via `renderMultiCard`. Hand-curated fences omit the annotations and fall
  through to the single-layout path keyed by `VizGraph.layoutHint`
  (injected from the fence's `widget=` attribute in `InlineDiagramBlock`).
- **Inline dispatch fork.**
  [`D3WidgetBlock.scala`](../../client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala)
  carries a `KnownLayouts: Set[String]` of layout keys. If the fence's
  widget name is in the set, dispatch goes to
  [`InlineDiagramBlock`](../../client/src/main/scala/codefolio/client/components/cortex/InlineDiagramBlock.scala)
  (which calls `renderWidget` from the unified pipeline); otherwise it
  routes to one of the 13 simulator components by name. The fork is a
  single pattern match — adding a new layout means appending a key to the
  set.
- **Error guards (slice 22).** `InlineDiagramBlock` wraps both `JSON.parse`
  on the payload and the `renderWidget` call in a `Try`, rendering the
  shared `d3-widget__error` placeholder inline on failure. A malformed
  fence shows the parse error to the author *in the chapter* instead of
  crashing the whole page. The same CSS block already serves
  `D3WidgetBlock`'s unknown-widget fallback, so styling is uniform.

## Related ADRs

- **ADR-0021** (Trace model — `HeapTrace` canonicalised). `HeapTrace` is the
  single trace schema that the modal-side `HeapToGraph.adapt` consumes to
  produce the `VizGraph` JSON this ADR routes through `renderWidget`.
- **ADR-0018** (referenced by ADR-0021 and the headers of `types.ts` /
  `index.ts` / `graph-render.ts` as the originating `VizGraph` design). The
  schema described here is the production form of that design; no separate
  ADR document was authored.

## Considered alternatives (and why rejected)

- **Keep the bespoke chapter widgets, share only the renderer.** Rejected —
  the duplication wasn't just the renderer. Each widget carried its own
  payload schema, its own pointer-canon palette, its own caption / progress
  / control-bar idioms, its own JSON-encoding round-trip with the chapter
  pipeline. Sharing the renderer alone left every other axis of drift on
  the table. The "one JSON contract end to end" decision is what actually
  collapses the maintenance surface.
- **One bespoke renderer per structure (drop the layout abstraction).**
  Rejected — the SVG idiom is the same for every structure
  (cells / nodes / edges / cursors / highlight / changed / removed) and the
  only thing that meaningfully varies is *where* nodes go. Pulling that
  variation into pure `LayoutFn`s keeps each layout small, testable in
  isolation, and free of D3. A new structure becomes ~150 lines of
  arithmetic + a unit test, not a new D3 component.
- **Use a force-directed default for every layout.** Rejected — force
  layouts violate the slot invariant. An in-place sort displayed in a
  force graph has elements drifting around as values swap, instead of
  values moving between fixed boxes. Slot-based positioning *is* the
  pedagogy for array-family structures. Force-directed stays as the
  fallback for `graph-generic` and as the placement inside graph-family
  layouts that don't have a stronger structural cue.
- **Drop the legacy layout-key aliases (`array`, `binary-tree`, etc.) on
  cutover.** Rejected — every chapter using a `d3 widget=<old-name>` fence
  would have to be rewritten in lockstep with the dispatch flip. Keeping
  both keys in the `LAYOUTS` map lets new chapters use the shape-driven
  names while old chapters continue to render. The two name families
  resolve to the same `LayoutFn`, so behaviour is identical.
- **Render inline diagrams server-side as static SVG.** Rejected — the
  diagrams aren't static. Even the hand-curated chapter payloads have a
  step axis, an annotation, and (eventually) a cursor; the interactive
  affordance is the point. Server-rendering the first frame would save
  one D3 call at the cost of a second code path with its own bugs.
- **Make `InlineDiagramBlock` accept a parsed `VizCases` object instead of
  the raw JSON string.** Rejected — the renderer accepts a JSON string at
  its boundary because the modal path also stringifies (it goes through
  the `@JSImport`ed bundle). Re-using the string interface for inline
  fences keeps both callers on the same code path; the parse failure that
  motivates the slice-22 error guard is a hard requirement either way.

## Consequences worth calling out

- **Adding a new data structure is a layout file + a `LAYOUTS` entry +
  (optionally) an entry in `KnownLayouts` for inline use.** Slices 17–20
  followed this loop unchanged: write `<name>-layout.ts`, register it in
  `index.ts`, append to `KnownLayouts`, write a catalog chapter, ship.
  Roughly a day per structure when the slot convention is decided.
- **The catalog book is now layout documentation.** Slice 21 reframed the
  11 chapters under
  `content/cortex/data-structures-and-algorithms/12-appendix-widget-catalog/`
  from per-widget docs to per-layout docs. The payload schemas, the
  pointer-canon palettes, the slot conventions — all documented *once*
  for the layout, no longer duplicated per widget.
- **The `12-appendix-widget-catalog/_template.md`** prescribes the
  layout-doc structure: payload schema, supported `cursor` names, slot
  rules, modifier flags (`highlight`, `changed`, `removed`), and a
  representative inline rendering. New layouts copy the template.
- **Bundle size shrinks.** Every retired widget was Scala.js code in the
  client bundle plus a per-widget D3 helper. The unified pipeline collapses
  to one `graph-render.ts` + 13 layout files; the chapter dispatch is a
  set lookup and a single `<.div>`-mounting component.
- **Error pathway is now uniform.** Unknown widget names, malformed
  `d3 widget=` payloads, and render-time exceptions all surface as the same
  inline `d3-widget__error` block (red border, monospace message), so
  chapter authors get the same feedback whichever way they break things.
- **Auto-dispatch is opt-in per trace.** `vizHint` from the fence (or the
  modal's hint) still wins as a per-trace override. The modal's
  `HeapToGraph` only emits per-object `layoutKind` when it confidently
  matches a shape; ambiguous shapes fall back to `graph-generic` without
  guessing. So a chapter author who knows the layout can name it,
  while an arbitrary `HeapTrace` from a fresh `python trace` block still
  gets a reasonable canvas split.

## Deferred to a later ADR / plan

- **Per-language tracers beyond Python + Java.** Kotlin and Scala remain
  source-display tabs with a `LanguageLockBanner`. When a chapter needs a
  Kotlin / Scala trace, the mechanism choice (ASM-based bytecode rewriting
  vs. some other path) is its own ADR.
- **Comparison mode** — two traces pinned side-by-side in the modal —
  still deferred from `IMPLEMENTATION_PROMPT.md` §6.16. The unified
  pipeline doesn't block it (each pane would just be its own
  `renderWidget` invocation), but the UI work hasn't been scoped.
- **Inline auto-tracing.** Today `python trace` / `java trace` blocks
  open the modal on Visualise click. "Render an inline diagram driven
  by the live trace at chapter-load time" is a future plan with its
  own pre-trace infrastructure decisions.
