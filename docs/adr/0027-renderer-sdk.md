# Renderer SDK + whole-graph renderers (cross-structure rendering)

ADR-0024 committed to bespoke renderers per data-structure shape. Slice 1
(Stack) was written standalone — it duplicated `graph-render.ts`'s chrome block
byte-for-byte and re-implemented the `WidgetController` lifecycle. That's fine
for one renderer; for 16 it's 16 copies to keep in sync. This ADR extracts a
**Renderer SDK** (`defineRenderer`) so each renderer is just its per-step DOM,
and introduces **whole-graph renderers** to fix the cross-structure rendering
gap the ADR-0025 audit found.

## Part 1 — the SDK (`renderer-sdk.ts`)

`defineRenderer(spec) → RendererFn`. The SDK owns everything that was
duplicated; the renderer provides only `className` + `build(ctx)`.

The SDK owns:
- the chrome (`.viz-graph` wrapper, title, `.viz-graph__frame`, caption,
  truncation notice, legend) — was identical in `renderGraph` + `stackRenderer`;
- the `data-card-content` tag on the content element (ArrowLayer's target);
- the `WidgetController` (`setStep` / `setHover` / `getStepCount` / `destroy`);
- the per-step caption (`step.annotation.title`);
- a `requestAnimationFrame` after each step so layout-dependent geometry (e.g.
  a pointer reading cell rects) settles before measurement;
- a `ResizeObserver` on the content element;
- persistent hover: the current hover key is re-applied after every step.

The renderer implements `RendererInstance` — `onStep` required; `onHover` /
`onResize` / `destroy` optional — and receives a `RendererContext` (`content`,
`frame`, `data`, `layout`, `emitHover`).

`stack-renderer.ts` is rewritten on the SDK as renderer #1 (behaviour identical;
verified in-browser by the `stack-push` Playwright fixture — terracotta top
cell, reversed-index column, sliding `TOP →` pointer). It now owns only the
cells column + side pointer (~110 lines, down from ~280).

## Part 2 — whole-graph renderers (the cross-structure fix)

The ADR-0025 audit found the generic multi-card path (`renderMultiCard` +
`filterStepToCard`) **drops every edge that crosses a card boundary**, and
`groupCards` only unions `Instance↔Instance` direct refs. So structures whose
nodes connect THROUGH a collection render with their structure invisible:

- **hashmap**: `Dict(index → Ref→Arr[Ref→Entry])` → shattered into a Dict card,
  bucket-Arr cards, and Entry-chain cards, with no visible bucket→chain link;
- **graph**: nodes connected through adjacency `Arr`s → isolated node cards.

Two renderer tiers now exist:

| Tier | Registry | Scope | Dispatch | Examples |
|---|---|---|---|---|
| **Per-card** | `RENDERERS` | one card's nodes (post-`filterStepToCard`) | inside `renderMultiCard`, via `pickRenderer` | stack, queue |
| **Whole-graph** | `WHOLE_GRAPH_RENDERERS` | the FULL `VizGraph` (all nodes + edges) | in `renderWidget`, via `pickWholeGraphRenderer`, **before** the multi-card split | hashmap |

A whole-graph renderer is selected by `structureType` (set by `viz-kind=` or
inference) and receives the complete graph, so it can reconstruct relationships
the per-card split would have dropped. Per-card renderers (stack) are unchanged
— `pickWholeGraphRenderer` misses for them and execution falls through to the
existing path.

### HashMap renderer (`hashmap-renderer.ts`, renderer #2 — cross-structure proof)

Reconstructs the hashmap from one step's nodes + edges:
- **Dict entries** (`kind === "entry"`) → buckets; `meta.key` = the index; a
  Dict-entry's out-edges point at its bucket Arr's cells.
- **cells** (`kind === "cell"`) → ordered by `slot`; each cell's out-edge points
  at the Entry instance it holds.
- **Entry instances** → chain members; rendered `meta.key : label` (`apple: 1`).

The intermediary cell nodes (label `·`) are implementation detail and are not
drawn — only the index + the entry chain, joined by `→`. Verified in-browser
(`hashmap-kind` fixture): `[0] apple:1 → grape:5` (the collision chain, grape
highlighted as new) and `[1] fig:3`. This is the coherent view the generic path
could not produce.

The two-renderer rule (ADR-0024 plan): the SDK is exercised by stack (per-card,
ported) AND hashmap (whole-graph, new) before being declared stable. Both pass.

## Graph + AVL — now done (were deferred; landed in the same Phase 2c)

Both remaining audit-broken shapes were fixed after the SDK + hashmap:

- **Graph renderer** (`graph-renderer.ts`, renderer #3) — a whole-graph renderer
  that synthesises direct `A → B` edges by composing `node → adjacency-cell →
  target`, drops the `kind="cell"` adjacency nodes, relabels each node from its
  `id` field (the adapter labels GraphNodes with the class name), and delegates
  to `renderGraph` + `graphLayout`. Registered `WHOLE_GRAPH_RENDERERS["graph"]`.
  Verified (`graph-kind` fixture): connected A→B→D→A + A→C→D with BFS carets.
- **AVL / in-place restructuring** — adapter fix in `adaptOne`: resolve the root
  PER STEP from the hint's current target, guarded so it only switches when the
  new target still reaches the original root (a rotation keeps the old root
  reachable; a recursive descent into a subtree does not). The avl-rotation
  fixture now renders one coherent card (node 2 → 1 + 3) instead of the stale-
  root "removed 2, 1". Every existing tree/recursion spec unchanged.

So all three shapes the ADR-0025 audit flagged (hashmap, graph, AVL) now render
coherently. Remaining: node *positions* during a rotation are an ADR-0018
layout-over-union compromise (edges/topology correct, positions static) — a
deeper per-step-layout change, out of scope here.

## Consequences

- Slices 2–16 are now ~the per-step DOM + a CSS BEM block + a `viz-kind=` sweep,
  not a chrome/controller copy. The cross-structure ones additionally register in
  `WHOLE_GRAPH_RENDERERS` and reconstruct from edges.
- `renderWidget`'s dispatch order is now: whole-graph renderer → multi-card
  (layoutKind) → single-layout + per-card bespoke. Existing chapters (no
  `viz-kind=`, or per-card kinds) are unaffected — verified: the generic
  `hashmap-chained-collisions` fixture (vizKind=None) still takes the multi-card
  path; only `viz-kind=hashmap` engages the bespoke renderer.
- Verified: `npm test` 78 (SDK + hashmap source-AST + the existing suites);
  `sbt sharedJVM/test` 184; Playwright fixtures 5/5 (incl. `stack-push` and
  `hashmap-kind`), screenshots reviewed.

## Honest caveats

- The HashMap renderer's reconstruction is specific to the `Dict→Arr→Entry`
  separate-chaining shape. A different hashmap heap layout (open addressing, or
  `Dict→Entry` directly) would need the reconstruction generalised. Acceptable:
  the DSA book's hashmap chapters use separate chaining.
- `structureType="hashmap"` must be set (via `viz-kind=hashmap` or inference) for
  the bespoke renderer to fire; without it a hashmap trace still falls to the
  (broken-for-this-shape) generic path. The chapter sweep that sets `viz-kind=`
  is part of resuming slices 2–16.
