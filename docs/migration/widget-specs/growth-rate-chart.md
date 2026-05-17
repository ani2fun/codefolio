# Widget Spec — `growth-rate-chart`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render an interactive line plot that visualises how the standard complexity
classes — `O(1)`, `O(log n)`, `O(n)`, `O(n log n)`, `O(n^2)`, `O(2^n)`, `O(n!)`
— diverge as `n` grows. The chart's central pedagogical job is to make the
*gaps* between classes visceral: at `n = 10` they look comparable; at
`n = 100` the polynomial classes leap; by `n = 1,000` the exponential and
factorial classes are off the chart and the linearithmic class is twenty
times the linear one. A small slider scrubs `n` from a low value to a chosen
maximum; selected curves redraw; a tooltip on hover reads off the exact
operation count for that class at the cursor's `n`.

This is *not* a benchmark widget — there are no actual measurements. It is
a closed-form plot of the mathematical functions themselves, plotted on a
shared axis, so a reader can answer "how much worse is `O(n^2)` than
`O(n log n)`?" by eye rather than by table-lookup.

The widget covers the orphan chapter
[`01-foundations/01-asymptotic-analysis.md`](../../../content/cortex/data-structures-and-algorithms/01-foundations/01-asymptotic-analysis.md),
specifically the prose around "The growth-rate hierarchy" and the
"`n = 10` vs `n = 10^6`" mermaid block. It replaces the *table* (which
remains for reference) with an *eye-test*.

## 2. Source-diagram inventory

0 — orphan widget; payloads derived from destination chapter prose.

## 3. Destination chapter usage

The chapter has three natural insertion points; each justifies one widget
instance with a different payload:

1. **After "The growth-rate hierarchy" table** — show all eight classes
   plotted up to `n = 64`, log-scaled y-axis. Lets the reader feel why
   `O(2^n)` and `O(n!)` are "the line between feasible and not in this
   lifetime" (chapter's phrasing).
2. **Inside "Big-O, formally" near the `n^2 + 1000n + 10^6` discussion** —
   plot the three claims `n`, `n^2`, `n^2 + 1000n + 10^6` to show that
   despite the lower-order terms looking dominant for small `n`, the
   quadratic shape eventually swamps them (the "drop constants, drop
   lower-order terms" rule made visible).
3. **In "A runnable demo" as a precursor to the timed table** — plot just
   `n` and `n^2` over the size range used in the benchmark
   (`1,000 / 5,000 / 10,000 / 20,000`), so the predicted shape sits next
   to the measured one.

In every case the widget plays a *make-the-curve-visible* role; the chapter
already gives the algebra.

## 4. Payload schema sketch

```typescript
{
  title?: string;
  // The complexity classes to plot. Each entry is a known key from the
  // standard catalog; the widget owns the closed-form function and a
  // canonical colour per class. Authors do not write formulas in the
  // payload — they pick names.
  classes: Array<
    | "constant"        // O(1)
    | "log"             // O(log n)
    | "linear"          // O(n)
    | "linearithmic"    // O(n log n)
    | "quadratic"       // O(n^2)
    | "cubic"           // O(n^3)
    | "exponential"     // O(2^n)
    | "factorial"       // O(n!)
  >;
  // Optional custom curves for the "with-constants" demonstration. Closed
  // catalog of shapes; the widget composes them, so authors still don't
  // write formulas.
  customCurves?: Array<{
    label: string;          // e.g. "n^2 + 1000n + 10^6"
    color?: string;         // optional override; otherwise next palette slot
    // Composed from primitive terms; the widget evaluates the sum.
    terms: Array<{
      coefficient: number;       // e.g. 1000
      exponent: number;          // 0 for constant, 1 for n, 2 for n^2, …
    }>;
  }>;
  // Slider configuration.
  nMin: number;             // default 1
  nMax: number;             // default 100
  nDefault?: number;        // cursor position on first render; defaults nMax / 2
  // y-axis scaling. Default "log" because exponential/factorial swamp linear
  // axes; authors can override to "linear" for chapters where only the
  // polynomial classes are plotted.
  yScale?: "linear" | "log";
  // Optional pinned annotations; the widget draws horizontal reference
  // lines with labels. Useful for showing "a billion ops ≈ 1 second on a
  // typical laptop".
  annotations?: Array<{ value: number; label: string }>;
}
```

## 5. POC payloads

The eight-class overview (asymptotic-analysis chapter, primary instance):

````markdown
```d3 widget=growth-rate-chart
{
  "title": "The growth-rate hierarchy",
  "classes": ["constant", "log", "linear", "linearithmic", "quadratic", "cubic", "exponential", "factorial"],
  "nMin": 1,
  "nMax": 32,
  "nDefault": 16,
  "yScale": "log",
  "annotations": [
    { "value": 1e9, "label": "~1 second on a typical laptop" }
  ]
}
```
````

The "drop lower-order terms" instance:

````markdown
```d3 widget=growth-rate-chart
{
  "title": "n^2 + 1000n + 10^6 is in O(n^2) — the lower-order terms drop out",
  "classes": ["linear", "quadratic"],
  "customCurves": [
    {
      "label": "n^2 + 1000n + 10^6",
      "terms": [
        { "coefficient": 1,       "exponent": 2 },
        { "coefficient": 1000,    "exponent": 1 },
        { "coefficient": 1000000, "exponent": 0 }
      ]
    }
  ],
  "nMin": 1,
  "nMax": 5000,
  "nDefault": 1001,
  "yScale": "log"
}
```
````

The "feasible vs not" instance:

````markdown
```d3 widget=growth-rate-chart
{
  "title": "n = 10 vs n = 1,000,000 — the feasibility cliff",
  "classes": ["log", "linear", "linearithmic", "quadratic", "exponential"],
  "nMin": 10,
  "nMax": 1000000,
  "nDefault": 1000000,
  "yScale": "log",
  "annotations": [
    { "value": 1e9, "label": "~1 second" },
    { "value": 1e13, "label": "~3 hours" },
    { "value": 1e17, "label": "~3 years" }
  ]
}
```
````

The "predict the demo" instance:

````markdown
```d3 widget=growth-rate-chart
{
  "title": "Predicted shape for has_duplicate — linear vs quadratic",
  "classes": ["linear", "quadratic"],
  "nMin": 1000,
  "nMax": 20000,
  "nDefault": 20000,
  "yScale": "linear"
}
```
````

## 6. Closest existing widget to mimic

No existing catalog widget is a close shape — `array-traversal` and
`linked-list` are both step-driven discrete renders. The structural shape
closest in the catalog is the **`growth-rate-chart` mode** of the
homelab's existing infra widgets that plot a function over a numeric range
(e.g. anything driven by `D3.scaleLog`).

For implementation reference, look at:

- `client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala`
  — for the React + D3 boundary pattern, payload parsing via
  `PayloadDecoder`, and the inline-error fallback.
- D3's `d3.line`, `d3.scaleLinear`, `d3.scaleLog`, and `d3.axisBottom` —
  the rendering primitives. The widget's draw loop binds one `<path>` per
  selected class, transitions the `d` attribute on slider scrub, and
  re-renders a vertical "cursor" line at the slider's `n`.

The slider is a plain `<input type="range">` in the host `<div>`; D3 owns
only the `<svg>`.

## 7. D3 selections plan

The mount has three layers:

- `<g class="growth-rate-chart__axes">` — `axisBottom` for `n`,
  `axisLeft` for ops; redrawn only when `nMax` or `yScale` changes.
- `<g class="growth-rate-chart__curves">` — one `<path>` per class, keyed
  by class name. `enter` appends with `opacity: 0` and fades in; `update`
  transitions the `d` attribute; `exit` fades out (rare — classes are
  fixed per payload but selectable curves let the user toggle individual
  series).
- `<g class="growth-rate-chart__cursor">` — vertical line + tooltip box
  bound to the slider's current `n`; updates on every slider tick without
  re-drawing the axes or curves.

Hover behaviour: the chart listens for `pointermove` on the SVG, finds the
nearest `n` integer, and overlays a tooltip listing each visible class's
operation count at that `n`. Tooltip layout uses the standard
"crosshair + readout box" pattern; values are formatted with `d3.format`
(`~s` for SI prefixes — `1.2M`, `3.1B`).

## 8. Shared abstractions

- **`PayloadDecoder`** — already used by every catalog widget; parses
  required `classes`, optional `customCurves`, `nMin`, `nMax`, `nDefault`,
  `yScale`, `annotations`. The decoder validates that each class name is
  in the closed enum and emits a one-line warning for unknown names
  (rather than failing the chapter).
- **No `Stepper`** — this is a slider-driven widget, not step-based, so
  the existing `Stepper.scala` does not apply.
- **A new tiny `ColourCanon`** for the standard complexity classes lives
  inside the widget module (eight entries; not worth promoting until a
  second widget needs it). Same convention as `LinkedList.CanonicalMarkers`.

## 9. Estimated build session count

**1 build session**, including:

- Scala module (~300–400 LOC; smaller than `ArrayTraversal` because there
  is no step controller, no swap animations, no per-step state)
- CSS BEM block under `client/src/styles/components/`
- Dispatcher case in `D3WidgetBlock.scala`
- Demo book chapter under `content/cortex/dsa-widget-catalog/`
  with the four payloads above
- Verification gates (`sbt scalafmtCheckAll`, `sharedJVM/compile`,
  `server/compile`, `client/fastLinkJS`, `npm run build`) and a browser
  smoke pass against the demo chapter

Risk factors: log-scale axes interact poorly with `0` values (factorial of
`1` is `1`, log is `0`; render starts at the cursor's lowest non-zero
position). Factorial of `n` for `n > 20` exceeds `Number.MAX_SAFE_INTEGER`;
the widget clips at `1e18` and shows a "value clamped" badge so the curve
stops at the visible ceiling rather than going to `Infinity` and disappearing.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/02-foundations/01-growth-rate-chart.md` — a
new chapter under the demo book that walks the four payloads in section
order, with prose contextualising each. Created as part of the widget
build session; serves as the canonical authoring reference for the
chart's payload schema.
