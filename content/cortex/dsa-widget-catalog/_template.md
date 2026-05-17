<!--
TEMPLATE — copy this file as `<NN>-<widget-name>.md` when adding a new widget
chapter. NN is the next free numeric prefix (see existing chapters). Delete
this comment after copying.

Sections in order:
  1. Title + summary frontmatter
  2. Purpose (2-3 sentences)
  3. Payload schema reference card
  4. Representative payloads (3-5)
  5. Compression notes (frames → steps)
  6. Source-spec link
-->

---
title: <Widget Display Name>
summary: <One-sentence purpose. Mention the topology + the operations animated.>
prereqs: []
---

# `<widget-name>`

## Purpose

<2-3 sentences. What topology does it draw? What operations does it animate?
What modes are supported? Be concrete.>

> **Source spec**: [`docs/migration/widget-specs/<widget-name>.md`](../../../docs/migration/widget-specs/<widget-name>.md)
>
> **Scala module**: `client/src/main/scala/codefolio/client/components/cortex/widgets/<WidgetName>.scala`
>
> **ADR**: [`docs/adr/00XX-<widget-name>-widget.md`](../../../docs/adr/00XX-<widget-name>-widget.md)

## Payload schema (reference card)

```ts
{
  title: string,                  // displayed above the widget
  mode?: "<mode-A>" | "<mode-B>", // optional mode flag (if widget supports modes)
  // ... primary fields specific to this widget
  steps: [
    {
      // per-step state — what changes between step N and step N+1
    }
  ]
}
```

**Required fields**: `title`, `steps`.
**Optional fields**: `mode`, ...

## Representative payloads

### Payload 1 — minimum / empty case

Tests the widget's render behaviour on smallest meaningful input.

```d3 widget=<widget-name>
{
  "title": "...",
  "steps": []
}
```

### Payload 2 — typical case

Mirrors the most common usage in DSA chapters.

```d3 widget=<widget-name>
{
  "title": "...",
  "steps": [
    { /* step 1 */ },
    { /* step 2 */ }
  ]
}
```

### Payload 3 — large-N stress

Verifies layout under high item counts.

```d3 widget=<widget-name>
{
  "title": "...",
  "steps": [
    /* 8-12 steps */
  ]
}
```

### Payload 4 — edge case / mode variant

Exercises a specific feature: mode flag, optional overlay, special transition.

```d3 widget=<widget-name>
{
  "title": "...",
  "mode": "<mode-B>",
  "steps": [ /* ... */ ]
}
```

## Compression notes

When porting a source `// Interactive Diagram (N frames): …` to a payload
for this widget, target **5-10 widget steps**. Compression strategy:

- **Frames 1-3 of source** → typically one step in widget (intro frames are
  usually slow exposition; merge into a single "initial state" step).
- **Each pointer-move or marker-update in source** → one widget step.
- **Final 1-2 frames of source** → typically one step (showing the result
  after the last operation).

Example: source 14-frame diagram → 6-8 widget steps.

## Browser verification

Open this chapter at
`http://localhost:5173/cortex/dsa-widget-catalog/<widget-name>` and:

1. Exercise step controls on each payload (next, prev, jump to end).
2. Verify no `.d3-widget__error` divs render.
3. Verify console is clean (`mcp__Claude_Preview__preview_console_logs`).
4. Capture a screenshot for the build commit.

If any payload fails to render, the widget isn't done. Fix and re-verify
before committing.
