# Bespoke renderers — reversal of ADR-0023's "one renderer, pluggable layouts"

ADR-0023 collapsed the Visualise pipeline onto a single generic graph renderer
fed by pure `LayoutFn`s. The maintenance argument was real and the layout
abstraction is correct geometry. **This ADR reverses the rendering half of
that decision**: alongside the generic renderer, we layer **17 bespoke
renderers** — one per data-structure shape — that wrap the same layouts in
structure-specific chrome (Stack's "↑ TOP" caret + reversed-index column,
Queue's head/tail badges, Heap's parent/child overlays, Trie's word
highlighting, …). Layouts continue to own *where* nodes sit; renderers now
own *which chrome* surrounds them. The generic renderer remains the fallback
for anything not yet ported.

## What "reversal" actually means

ADR-0023 listed two relevant rejection branches:

> **Keep the bespoke chapter widgets, share only the renderer.** Rejected —
> the duplication wasn't just the renderer …
>
> **One bespoke renderer per structure (drop the layout abstraction).**
> Rejected — the SVG idiom is the same for every structure … and the only
> thing that meaningfully varies is *where* nodes go.

We're not reversing the first branch — chapter widgets stay one-pipeline. We
are reversing **the second**: bespoke renderers *do* now exist per shape,
but they share the layout abstraction (the geometry stays generic). The
maintenance objection still bites — 17 more files to keep in sync as the
design evolves — and this ADR accepts that as a deliberate tax for
pedagogical fidelity.

## Why the reversal

Reviewing the Visualise modal against the Claude Design's
`renderers.jsx` after slice 22 shipped, the gap was wide enough to motivate
the reconsideration. The generic renderer paints every shape with the same
chrome — a card, a caption, a legend, role-coloured cursor marks. The
bespoke renderers wrap the same nodes in shape-specific scaffolding:

- **Stack**: a top-anchored column with a "↑ TOP" callout, a reversed-index
  column on the left, and a vertical "fall" animation as `pop` shrinks the
  array.
- **Queue / Deque**: head and tail callouts, ring-buffer offset markers,
  enqueue-from-right / dequeue-from-left direction badges.
- **Heap**: parent/child level rings, percolate-up animation as a value
  bubbles toward the root, and an array-vs-tree dual view.
- **Trie**: prefix highlighting along the path of the current insertion,
  end-of-word markers on accepting nodes.
- **Union-Find**: tree-with-path-compression visuals, rank badges.

The chrome is what makes a stack *read* like a stack; without it the chapter
just shows an array of cells with arrows. The generic renderer is good
enough for an unfamiliar shape, but the project's primary product is the
DSA book — each chapter is the artefact, and pedagogical fidelity beats
maintenance economy on the inputs we can author once and read often.

## Cost analysis (from the rollout plan)

| # | Renderer | Chapter | LOC | Notes |
|---|---|---|---|---|
| 1 | Stack | `02-linear-structures/05-stack` | ~70 | First renderer; sets the template |
| 2 | HashMap | `02-linear-structures/07-hash-table` | ~36 | |
| 3 | FenwickTree | `03-trees/10-fenwick-tree` | ~64 | |
| 4 | Bitset | `08-bit-tricks/05-pattern-bitmasking` | ~57 | |
| 5 | Deque | `02-linear-structures/06-queue-and-deque` | ~68 | |
| 6 | LinkedList | `02-linear-structures/03-singly-linked-list` | ~74 | |
| 7 | BinaryTree | `03-trees/01-binary-tree` | ~85 | |
| 8 | SegmentTree | `03-trees/09-segment-tree` | ~92 | |
| 9 | UnionFind | `03-trees/11-disjoint-set-union` | ~96 | |
| 10 | Graph | `04-graphs/*` | ~102 | |
| 11 | Matrix | `06-sorting-and-searching` + DP chapters | ~57 | |
| 12 | Trie | `03-trees/04-trie` | ~105 | |
| 13 | Heap | `03-trees/03-heap` | ~134 | Non-trivial bubble-up animation |
| 14 | Array | `02-linear-structures/01-arrays` | ~145 | Most callsites; replaces baseline |
| 15 | DoublyLinkedList | `02-linear-structures/04-doubly-linked-list` | ~146 | Trickiest D3 work — colour-split edges + legend |
| 16 | String | `07-strings/*` | ~179 | Caret rows + multi-cursor + in-window highlighting |
| 17 | SkipList | `09-probabilistic-and-advanced/02-skip-list` | ~100 | New chapter, may be stub |

**Total: ~1,610 LOC across 17 renderers; 75–95 hours of focused work spread
across 17 sessions** (one renderer per session per the
`handoff_at_every_session_end` rule). Realistically 5–8 weeks.

## Design

### Two axes, one signal

`HeapToGraph.adapt` now produces a `VizGraphStep` carrying *two* dispatch
hints:

- `layoutKind` (already exists, per `VizNode.layoutKind`) — picks the
  geometry layout from `LAYOUTS` in `client/src/d3/index.ts`. Unchanged
  from ADR-0023.
- `structureType` (new, on `VizGraphStep`) — picks the bespoke renderer
  from a new `RENDERERS: Record<string, RendererFn>` map. When unset
  (`Option[String] = None`), the generic `renderGraph` takes over,
  preserving ADR-0023's behaviour bit-for-bit.

The signal is derived three ways, in priority order:

1. **Explicit `viz-kind=<name>` markdown attribute** on the runnable code
   fence — wins over inference. Authors who know their structure (stack,
   queue, heap, trie, …) declare it once and the renderer is locked in.
2. **`HeapToGraph` segment-wide inference** — for the simple
   Arr-as-stack/queue/deque case, scan the segment's frame locals and
   function names: `top` / `push` / `pop` → stack; `front` + `back` →
   deque; `front` or `enqueue` / `dequeue` → queue.
3. **Default** — `None`, falls back to the generic renderer.

Inference is intentionally conservative — a miss falls back to the generic
renderer (acceptable: the chapter still renders); a mis-tag would
mis-render (unacceptable: the user sees the wrong chrome). Anything
ambiguous expects an explicit `viz-kind=` from the author.

### `RENDERERS` registry

```ts
// client/src/d3/index.ts
export type RendererFn = (
  container: HTMLElement,
  data: VizGraph,
  layout: LayoutFn,
  onStep?: (i: number) => void,
  options?: RenderGraphOptions,
) => WidgetController;

const RENDERERS: Record<string, RendererFn> = {};
```

Each renderer slice (slices 1–17 in the rollout plan) ships one entry.
A renderer is free to:

- delegate to `renderGraph` for the underlying layout work and wrap it
  in its own chrome (the common case), or
- draw everything itself when the chrome is sufficiently invasive (Heap,
  Trie, possibly DLL).

Either way it returns the same `WidgetController` so the modal's Stepper
drives it unchanged.

### Tests

Three layers, every slice:

1. **`HeapToGraphSpec`** (zio-test, shared/JVM) — asserts the new
   `structureType` inference fires for canonical fixtures.
2. **`RendererSpec`** (zio-test, shared/JVM) — asserts the produced
   `VizGraphStep` JSON carries the expected `structureType` and that the
   `viz-kind=` override wins over inference.
3. **Browser DOM-walk** (`client/test/renderers/<name>.dom.md`) — open
   the chapter via `bin/dev`, click Visualise, run a list of
   `document.querySelector` assertions via MCP `preview_eval`. Lives as
   Markdown so it's also human-readable documentation of "what this
   renderer should look like".

## Maintenance tax (the honest part)

ADR-0023's maintenance argument bites *after* the programme — 17 renderers
to keep in sync as:

- the design system shifts (a token rename, a palette update),
- D3's API evolves (the v8 → v9 jump someday),
- Scala.js evolves (the next React binding revision),
- the layout-positioning conventions shift (a new `slot` invariant).

This is a deliberate tax. The mitigations:

- **Renderers are thin** — each delegates to `renderGraph` for the
  geometry and only wraps it. A design-token change usually touches one
  CSS file, not 17 renderer modules.
- **Tests catch geometry drift** — `RendererSpec` and the DOM-walk smoke
  tests fail fast if the chrome moves.
- **The generic renderer is always a working fallback** — if a bespoke
  renderer gets stuck behind a refactor, removing its `RENDERERS` entry
  reverts that chapter to the generic chrome, no data loss, no broken
  page.

## What this ADR does *not* change

- ADR-0023's layout abstraction (`LayoutFn` returning positions; one
  layout per shape) stays exactly as-is.
- The `VizGraph` JSON contract (ADR-0018 / ADR-0021) stays exactly as-is
  apart from the new `structureType` field.
- Inline `d3 widget=` fences stay generic-rendered (they're hand-curated
  payloads, not trace-derived, and don't benefit from bespoke chrome).
- The Scala-side modal (`VisualiseModal.scala`) stays unchanged in shape
  — it forwards the new `vizKind` prop through `HeapToGraph.adapt` and
  the rendered JSON; everything downstream is TypeScript / D3.

## Related ADRs

- **ADR-0018 / ADR-0021** — `VizGraph` JSON contract; the new
  `structureType` field is the only addition.
- **ADR-0023** — original "one renderer, pluggable layouts" decision.
  Amended with a top-of-file pointer back here.
