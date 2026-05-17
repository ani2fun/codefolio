# Widget Spec — `amortized-payment`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise the per-operation accounting of an amortized algorithm as a
**table of operations augmented with three running columns**: actual
cost, amortized cost (banker's/charged), and accumulated potential
(`Φ(D)`). The widget steps through a sequence of operations one at a
time; each step adds a row to the table, increments the running totals
in a header readout, and overlays a small horizontal bar comparing
actual vs amortized to make the per-step difference visible. When the
table is complete a summary card prints the average actual cost, the
average amortized cost, and the maximum single-op cost — the three
numbers the reader needs to internalise the "occasionally slow, fast on
average" pattern.

The widget's central pedagogical job is to make all three classical
amortization methods (aggregate, banker's, potential) tell *the same
story* about a single sequence of operations. The table's three
columns aren't separate methods — they're the same operations viewed
through three accounting lenses, and the totals must agree.

The widget covers the orphan chapter
[`01-foundations/03-amortized-analysis.md`](../../../content/cortex/data-structures-and-algorithms/01-foundations/03-amortized-analysis.md),
specifically "Three ways to amortize" and "Worked example: dynamic
array push". It replaces a static mermaid block (the resize-cost
sequence diagram) with a live, scrubable table.

## 2. Source-diagram inventory

0 — orphan widget; payloads derived from destination chapter prose.

## 3. Destination chapter usage

Three natural insertion points, mapped to three payloads:

1. **In "Worked example: dynamic array push"** — the canonical
   table. Each step is one push; columns show actual cost (1 for cheap
   pushes, `k` for resize-at-capacity-`k`), amortized cost (the
   banker's choice of `3`), and potential `Φ(D) = 2(size − capacity/2)`.
   Highlights resize rows in amber; final summary shows
   "total actual = 3n − 1, total amortized = 3n, both per-op = 3 = O(1)".
2. **In "Worked example: incrementing a binary counter"** — a different
   sequence (16 increments of a 5-bit counter). Actual cost is the
   number of bit flips per increment; amortized cost is 2; potential
   is the number of 1-bits in the counter. Same visual story, different
   data — reinforces the pattern.
3. **In "Three ways to amortize" alongside the method-comparison
   mermaid** — a tiny 8-step sequence on the dynamic array with a
   *side-by-side comparison* view (the same eight operations counted
   three ways: aggregate, banker's, potential). Each column ends with
   the same per-operation average, driving home the chapter's "three
   different proofs, same answer" tagline.

## 4. Payload schema sketch

```typescript
{
  title?: string;
  // Closed catalog of known algorithms. The widget owns the per-op cost
  // function, the banker's charge, and the potential-function formula
  // for each name. Authors do not write potential functions in JSON —
  // they pick an algorithm and a sequence.
  algorithm:
    | "dynamic-array-doubling"      // canonical: push-only, capacity doubles
    | "binary-counter"              // incrementing a binary counter
    | "stack-with-multipop"         // textbook variant (multipop)
    | "custom";                     // see customCosts below
  // For the closed-catalog algorithms, a sequence of operations. Each
  // entry's shape varies by algorithm — for dynamic-array-doubling it's
  // just the push count; for stack-with-multipop it's a list of
  // {kind: "push"|"pop"|"multipop", k?: number}.
  operations: Array<unknown>;
  // For algorithm = "custom", the widget renders whatever the author
  // hands it without trying to interpret semantics.
  customCosts?: Array<{
    label: string;       // e.g. "push 1", "resize then push"
    actual: number;
    amortized: number;   // banker's-method charge per operation
    potentialAfter: number; // Φ(D_i) after this operation
  }>;
  // Default true. When false, the potential column is hidden — useful
  // for the aggregate-only view.
  showPotential?: boolean;
  showBankerColumn?: boolean;        // default true
  // Default true; when false, the summary card is hidden (compact mode
  // for inline use mid-prose).
  showSummary?: boolean;
}
```

## 5. POC payloads

Dynamic-array doubling — the canonical 16-push sequence:

````markdown
```d3 widget=amortized-payment
{
  "title": "Dynamic-array push: O(1) amortized",
  "algorithm": "dynamic-array-doubling",
  "operations": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
}
```
````

Binary counter — 16 increments of a 5-bit register:

````markdown
```d3 widget=amortized-payment
{
  "title": "Binary counter: O(1) amortized per increment",
  "algorithm": "binary-counter",
  "operations": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
}
```
````

Three-method side-by-side on a small dynamic array:

````markdown
```d3 widget=amortized-payment
{
  "title": "Same eight pushes — aggregate, banker's, potential agree",
  "algorithm": "dynamic-array-doubling",
  "operations": [1, 2, 3, 4, 5, 6, 7, 8],
  "showPotential": true,
  "showBankerColumn": true,
  "showSummary": true
}
```
````

Custom — author-supplied costs for a sequence the catalog doesn't know:

````markdown
```d3 widget=amortized-payment
{
  "title": "Custom: mixed-cost sequence",
  "algorithm": "custom",
  "operations": [],
  "customCosts": [
    { "label": "op 1 (cheap)", "actual": 1, "amortized": 3, "potentialAfter": 2 },
    { "label": "op 2 (cheap)", "actual": 1, "amortized": 3, "potentialAfter": 4 },
    { "label": "op 3 (cheap)", "actual": 1, "amortized": 3, "potentialAfter": 6 },
    { "label": "op 4 (resize)", "actual": 4, "amortized": 3, "potentialAfter": 2 },
    { "label": "op 5 (cheap)", "actual": 1, "amortized": 3, "potentialAfter": 4 }
  ]
}
```
````

## 6. Closest existing widget to mimic

No catalog widget is a table-based render. The closest *structural*
analogue is `array-traversal` — both step through a sequence of states
with a per-step `msg` and a final summary. The key shape differences:

- Output is a vertical table that grows row-by-row, not a
  horizontal row of cells that animates in place.
- Per-step transition is a row fade-in plus a header counter tick (the
  running totals); there is no positional animation of existing rows.
- A small inline "cost bar" SVG sits in the actual-cost column per row
  to give the resize spikes visual weight; everything else is HTML
  table elements.

This is the first catalog widget where the *core render* is HTML rather
than `<svg>`. The Scala module still uses the React + D3 boundary
pattern from `LinkedList.scala`, but D3 only owns the tiny inline cost
bars; React owns the table itself.

## 7. D3 selections plan

D3 owns only the inline `<svg>` cost bars and the optional sparkline at
the bottom of the table:

- `<svg class="amortized-payment__bar">` per row (one per actual-cost
  cell): a single `<rect>` whose `width` scales to the actual cost,
  with a horizontal dashed line at the amortized cost for comparison.
  Pure render, no transitions.
- `<svg class="amortized-payment__sparkline">` below the table: the
  full sequence of actual costs as a line chart, with the amortized
  charge shown as a horizontal reference. Re-renders on every step.

The HTML table is managed by React: `useState` holds the current step;
the table maps over `operations.slice(0, step + 1)` to render rows.
The header readout uses `useMemo` to derive running totals.

## 8. Shared abstractions

- **`PayloadDecoder`** — parses the closed-enum `algorithm`, the
  `operations` array (validated per-algorithm), and the optional
  `customCosts`. Throws on schema mismatch; the chapter renders the
  inline error fallback rather than crashing.
- **`Stepper.scala`** — reuse for the prev / next / play / reset
  controls. Step count equals `operations.length` (or
  `customCosts.length`).
- **A new tiny `AmortizedAlgorithmCanon`** inside the widget module:
  for each closed-enum algorithm, returns a function
  `(opSequence) => Array<{label, actual, amortized, potentialAfter}>`.
  Lets the binary-counter, dynamic-array, and multipop-stack share the
  rendering pipeline without each writing its own table builder.

## 9. Estimated build session count

**1 build session**:

- Scala module (~250–350 LOC; smallest planned widget so far — the
  rendering is mostly HTML, the math is closed-form per algorithm)
- CSS BEM block (table + cost-bar + sparkline + summary card)
- Dispatcher case in `D3WidgetBlock.scala`
- Demo book chapter with the four payloads above
- Verification gates + browser smoke pass

Risk factors: the "potential function" column for dynamic-array-doubling
must match the chapter's definition exactly (`Φ(D) = 2(size − capacity/2)`).
The widget owns this formula; bug-class concern is that the chapter's
prose and the widget's computation drift apart. Mitigation: cite the
formula in both the chapter text and the widget's docstring, and add a
small assertion in the demo chapter ("after 8 pushes Φ = ___; the widget
should show ___ in the gutter").

## 10. POC chapter

`content/cortex/dsa-widget-catalog/02-foundations/03-amortized-payment.md` —
a new chapter under the demo book exhibiting the four payloads with
prose contextualising each method. Created as part of the widget build
session; serves as the canonical authoring reference.
