# `linked-list` widget — interactive node-and-pointer animation, singly + doubly in one schema

Phase 1 (Singly Linked List) surfaced ~55 `// Interactive Diagram` markers in the raw lesson source, of which 21 were collapsed to static d2 / mermaid in destination (audit in commit `669ceb8`'s notes) and another ~70 static d2 linked-list blocks could pedagogically be upgraded too. The existing `array-traversal` widget (ADR-0006, animated by D3 per ADR-0013) renders a row of cells with markers and a range band — fine for arrays, useless for a linked list, because the *defining act* of every linked-list operation is `node.next` rebinding from one target to another. A row of cells has no arrows to animate.

**Decision**: add a single new widget `linked-list` to the catalog. One widget covers both **singly** and **doubly** via a `direction: "single" | "double"` field in the payload. This means Phase 2 (Doubly Linked List) gets zero new infrastructure — it just authors payloads with `direction: "double"` and the widget renders prev arrows in addition to next arrows.

  - **One widget for both directions, not two.** Singly and doubly differ only in whether the back-direction `prev` arrows are rendered. The schema, node model, marker model, layout, and animation pattern are identical otherwise. A second `doubly-linked-list` widget would be ~95% copy-pasted code, double the test surface, and a foot-gun (authors using the wrong one). Phase 1 emits `direction: "single"`; Phase 2 emits `direction: "double"`; the dispatcher routes both to the same `LinkedList` component.
  - **Payload schema — nodes + steps with per-step links and node deltas.** Authors declare a `nodes` array (each `{id, value, style?}`), a starting `head`, and a list of `steps`. Each step carries: a complete `links` array (forward arrows as `[fromId, toId]` pairs; optional per-link `kind: "next" | "prev" | "broken"`), a `markers` array (named pointers attached to a node by id, with optional color), an optional per-step `nodes` override (to add or remove nodes for that step — fade-in for new, fade-out for removed), an optional per-step `head` override, and a `msg` caption. The top level may carry `cycleTarget: "<nodeId>"` for Floyd's cycle visualisations — when set, the widget draws a curved back-edge from the tail node's `next` to the target node, styled distinctly.
  - **Animation pattern.** D3-owned SVG subtree under a React-managed `<div>`, same boundary as ADR-0013. Keyed-join by `node.id` for node groups means a node that moves slots transitions its `transform` smoothly rather than flicker-rebuilding. Keyed-join by `${from}→${to}` for arrows means a rewired pointer reuses the same DOM element, so its `d=` path attribute interpolates from old source/target to new. A `--rewire` class flashes the rewired arrow blue for 250 ms during the transition. Inserted nodes fade in (`opacity` 0→1, 450 ms `easeCubicInOut`); removed nodes fade out (1→0 over the same duration) before unmount. Arrow paths are computed each step as `M sx sy L tx ty` plus a triangle arrowhead at the target — straightforward enough to skip pulling in `d3-interpolate-path` and stay under the bundle cap.
  - **Closed-catalog growth, ADR per widget.** ADR-0006's closed-catalog rule is preserved: authors still name widgets in fences; unknown names render an inline error; rendering mechanism remains a widget's private business. The catalog now **grows per phase** as the DSA migration introduces new visualisation topologies (`hash-table`, `binary-tree`, `graph`, etc.). Each new widget gets its own ADR. ADR-0014 is the first growth case after the initial Phase 0 set; the same shape applies to widgets that follow.
  - **Bundle-budget impact.** Projected gzip delta ≈ 8 KB (Scala source ~650 LOC compiled, plus the CSS BEM block). Reuses existing D3 surface — `select`, `selectAll`, `data(_, keyFn)`, `transition().duration().ease().attr()`, `easeCubicInOut` — so no facade extension and no new `d3-*` submodule import. The +90 KB cap from ADR-0013 stays comfortable.
  - **Light + dark mode.** SVG text uses `fill: var(--color-fg-default, currentColor)` with `currentColor` as the explicit fallback — past widgets have lost text in dark mode when the CSS custom property wasn't defined for the SVG context. Arrow strokes use `var(--color-fg-muted, #64748b)`; node rects use `var(--color-card, #fff)` and `var(--color-border, #e5e7eb)`. Mandatory per-chapter dark-mode toggle test during the Phase 1 conversion arc to catch any residual regression.
  - **Failure modes.** Malformed JSON → `parsePayload` returns `Left(msg)` and the widget renders the standard `.d3-widget__error` placeholder. Link references an unknown node id → drop the link silently, emit one dev-mode console warning. `cycleTarget` references an unknown id → omit the back-edge silently. A step with zero nodes is allowed (rare; renders an empty frame); a step with a marker pointing at a non-existent node id → drop the marker silently.

**Canonical payload** (also embedded in the widget Scaladoc):

```json
{
  "title": "Insert after node 2",
  "direction": "single",
  "nodes": [
    {"id": "a", "value": "10"},
    {"id": "b", "value": "20"},
    {"id": "c", "value": "30"},
    {"id": "d", "value": "40"}
  ],
  "head": "a",
  "steps": [
    {
      "links": [["a","b"],["b","c"],["c","d"]],
      "markers": [{"name": "curr", "nodeId": "b"}],
      "msg": "curr at node 20"
    },
    {
      "nodes": [
        {"id": "a", "value": "10"},
        {"id": "b", "value": "20"},
        {"id": "n", "value": "25", "style": "new"},
        {"id": "c", "value": "30"},
        {"id": "d", "value": "40"}
      ],
      "links": [["a","b"],["b","n"],["n","c"],["c","d"]],
      "markers": [{"name": "new", "nodeId": "n"}],
      "msg": "splice new node in after curr"
    }
  ]
}
```

For Floyd's cycle detection (Phase 1 chapter 1.5), the same schema gains a top-level `cycleTarget: "<nodeId>"`. The widget then draws a cubic-Bezier back-edge from the tail node to the cycle entry node, styled with `.linked-list__cycle-edge`. **Tail detection**: prefer a node that appears as a link `to` but never as a link `from` (the true dangling tail of the forward chain); fall back to the last node in the `nodes` array (rightmost on screen). This matches author intuition — the cycle source is the last node visually on the row.

For doubly-linked-list usage (Phase 2 onwards — Phase 1 has no doubly-linked content), set `direction: "double"`. The widget then auto-derives reverse links from each forward link — `[a, b]` next implies `[b, a]` prev — unless the author overrides via per-link `kind: "prev"`. Prev arrows render below the node row with dashed styling.

The trade is **one new ~650-LOC Scala module, a CSS BEM block, and one ADR** for **a permanent solution to every linked-list visualisation across the next two phases of the DSA migration** — and the same growth pattern (ADR + module + CSS + dispatcher) for every subsequent phase's widget needs.
