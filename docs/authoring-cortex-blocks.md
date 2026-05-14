# Authoring guide — interactive blocks in Cortex chapters

This is the working reference for the typed [Blocks](../shared/src/main/scala/codefolio/shared/cortex/Blocks.scala) you can embed in any `.md` file under `content/cortex/`. Each block is a fenced code (or HTML) marker that the markdown pipeline turns into a placeholder, which a Scala.js component then mounts into.

Existing block types — `runnable-code`, `runnable-group`, `mermaid`, `d2`, `d2-slides` — are covered briefly with examples. The two new block types — **`d3` interactive widgets** and **`python trace` step-through** — are documented in depth below.

---

## Quick reference

| What you want | Fence syntax |
|---|---|
| Run a Python / Java / Scala / C / C++ / Go / Rust / Kotlin / TypeScript / JavaScript / SQL snippet | ` ```python run ` (etc.) |
| Same source in multiple languages, behind tabs | Adjacent runnable fences auto-merge. Add ` ```pseudocode ` (no `run` needed) to lead the group |
| Static Mermaid diagram | ` ```mermaid ` |
| Static D2 diagram | ` ```d2 ` |
| Step-through frames of a D2 diagram | `<div class="d2-slides">…</div>` wrapping multiple ` ```d2 ` fences |
| **Interactive D3 widget** (e.g. array-traversal stepper) | ` ```d3 widget=<name> ` with a JSON payload body |
| **Python trace** (collapsible, runs through `/api/run`, shows code + locals) | ` ```python trace ` |

---

## D3 widgets

A named, parameterised Scala.js + D3 component on the client. The catalog is closed — unknown names render an inline error. To grow the catalog, add a `case` to [`D3WidgetBlock`](../client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala) and a new component under `client/.../components/cortex/widgets/`.

### Markdown shape

````
```d3 widget=<widget-name>
{
  "<key>": <value>,
  …
}
```
````

The info string carries the widget name; the body is the **JSON payload** the widget interprets. Schema is per-widget — the shared layer does not validate field shapes (it only requires `widget` and `payload` to be present), so a typo in field names will surface as a render error inside the widget, not a chapter-load failure.

### Catalog

#### `array-traversal` — row of indexed cells with named markers + range band

A horizontal array of cells with one or more labelled markers per step and an optional highlighted range. Drives linear scan, two-pointer, binary search, sliding window, and sort-step visualisations from one component.

**Schema:**

```jsonc
{
  "items": ["1", "3", "5", "7", "9", "11", "13"],   // cell labels; strings or numbers (numbers are stringified)
  "title": "Binary search for 9",                    // optional caption above the SVG
  "steps": [
    {
      "markers": [                                   // 0..N markers per step; rendered as coloured triangles + labels
        { "name": "lo",  "index": 0, "color": "#3b82f6" },
        { "name": "mid", "index": 3, "color": "#10b981" },
        { "name": "hi",  "index": 6, "color": "#f59e0b" }
      ],
      "range": { "lo": 0, "hi": 6 },                 // optional shaded band between two indices (inclusive)
      "msg": "arr[mid]=7 < 9 → discard left half"    // optional caption shown below the SVG
    }
  ]
}
```

Per-step fields:
- `markers[].color` is optional — if omitted the widget cycles through a built-in palette (blue, green, orange, purple, red).
- `range` is optional — useful for showing "the current search window."
- `msg` is optional but recommended — it's the only text the reader sees about *why* this step exists.

**Cookbook:**

```d3 widget=array-traversal
{
  "items": [1, 3, 5, 7, 9, 11, 13],
  "title": "Linear search for 9",
  "steps": [
    { "markers": [{ "name": "i", "index": 0 }], "msg": "arr[0]=1 ≠ 9, continue" },
    { "markers": [{ "name": "i", "index": 1 }], "msg": "arr[1]=3 ≠ 9, continue" },
    { "markers": [{ "name": "i", "index": 4 }], "msg": "arr[4]=9 = target — return 4" }
  ]
}
```

For two pointers, use two markers per step (e.g. `l`, `r`). For sliding window, set `range` to the window bounds. For binary search, three markers (`lo`, `mid`, `hi`) + the range = `lo..hi`.

### Adding a new widget

1. Create `client/src/main/scala/codefolio/client/components/cortex/widgets/<MyWidget>.scala`. Take `Props(payload: String)`. Parse the JSON in a `useMemoBy(_.payload)` to keep re-renders cheap. Build the SVG as a string and inject via `dangerouslySetInnerHTML` (matches Mermaid / D2 precedent — scalajs-react's HTML namespace doesn't carry SVG tags).
2. Add a new `case "<my-widget>" => MyWidget.Component(...)` to [`D3WidgetBlock.scala`](../client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala).
3. Add CSS in a new `client/src/styles/components/<my-widget>.css` and import it from `client/tailwind.css`.
4. Done — no server, shared, or OpenAPI changes.

---

## Python trace

Step through a Python program with a current-line cursor + locals panel. The server (`/api/run`) is unchanged — the client wraps the user source in a `sys.settrace` harness (with `__CFTRACE_BEGIN__` / `__CFTRACE_END__` markers on stdout), posts it through, and parses the trace JSON out of stdout.

### Markdown shape

````
```python trace
def my_algo(...):
    …

result = my_algo(...)
print(result)
```
````

Use the same `python` language tag as `run`, but with `trace` instead. The block is **collapsed by default** — only the language label and a Trace button are visible. Clicking Trace expands the body and runs the program; an `Eye` / `EyeOff` toggle in the header collapses again without losing trace state.

### What you see

- The full source with line numbers; the line currently executing is highlighted.
- A locals panel listing every in-scope variable as `repr(value)` (truncated at 120 chars).
- Step controls (Prev / Play / Next / Reset) and a Re-trace button.
- A collapsible "Program output" section showing any `print()` output your code produced.

### Authoring tips

- **Keep the source self-contained.** The harness `exec`s the program in a fresh namespace; top-level statements (assignments, function defs, calls) are all fine. Imports work. No `if __name__ == "__main__":` boilerplate required.
- **Avoid printing the trace markers.** A program that literally prints `__CFTRACE_BEGIN__` or `__CFTRACE_END__` will confuse the parser. Don't.
- **Step limit is 2000 frames.** The harness calls `sys.settrace(None)` past that, so very long algorithms get truncated silently. Fine for DSA examples; ill-suited for benchmarks or huge inputs.
- **Custom classes show as `<MyClass object at 0x…>`** in the locals panel unless you give them a `__repr__`. Define one if you want readable values:

  ```python
  class TreeNode:
      def __init__(self, val, left=None, right=None):
          self.val, self.left, self.right = val, left, right
      def __repr__(self):
          return f"TreeNode({self.val})"
  ```

- **Pair with a D3 widget when you can.** The widget shows the *intuition* (a clean, captioned animation); the trace shows the *truth* (real execution, real values). Binary search uses both in [`01-binary-search.md`](../content/cortex/data-structures-and-algorithms/06-sorting-and-searching/02-searching/01-binary-search.md) for this reason.

### What's not supported (yet)

- **Java traces.** The `language` slot is preserved so we can land Java later via JDI, but v1 only handles Python. A `java trace` fence falls through to default rendering.
- **Auto-visualising structures.** Locals are shown as `repr()` text. A future improvement is harness annotations (e.g. `# @viz tree(root)`) that route a runtime value into a D3 widget renderer — that's a separate workstream, no Scala changes are needed per topic today.

---

## Existing block types — examples

For reference, the patterns the new blocks build on:

### Single runnable snippet

````
```python run
print("hi")
```
````

### Tabbed group across languages

Place adjacent runnable fences; the pipeline merges them into one tab strip. Pseudocode joins by language alone (no `run` flag); runnable languages need `run` on each fence.

````
```pseudocode
function f(n):
  return n * 2
```

```python run
def f(n):
    return n * 2
```

```java run
int f(int n) { return n * 2; }
```
````

### Mermaid

````
```mermaid
flowchart LR
  A --> B
```
````

### D2 (static)

````
```d2
direction: right
A -> B -> C
```
````

### D2 step-through slides

Wrap a run of D2 fences in a `<div class="d2-slides">` marker — each fence becomes one frame with Prev / Play / Next controls.

```html
<div class="d2-slides" data-caption="A walking right">

```d2
direction: right
A -> B
```

```d2
direction: right
B -> C
```

</div>
```

---

## Anatomy reminder

Every block follows the same plumbing:

1. **Markdown fence** → `render.ts` codeHandler emits a placeholder `<div class="<block-type>" data-…>`.
2. **Discoverer** in [`BlockDiscovery.scala`](../client/src/main/scala/codefolio/client/components/cortex/BlockDiscovery.scala) walks the rendered article, finds placeholders by class, URI-decodes attributes, hands them to a structural decoder in [`shared.cortex.Blocks`](../shared/src/main/scala/codefolio/shared/cortex/Blocks.scala).
3. **Dispatch** in [`ChapterContent.scala`](../client/src/main/scala/codefolio/client/components/cortex/ChapterContent.scala) is a total `Block => VdomElement` match. Adding a new variant breaks the match exhaustively at compile time — that's the safety net.
4. **Component** mounts into the placeholder via `ReactDOMClient.createRoot`.

Naming convention: the placeholder class is the block-type slug (e.g. `mermaid-block`, `traced-code-block`); the widget root class is something distinct (e.g. `mermaid`, `traced-code`) to avoid BEM double-styling. New blocks should follow the same split.
