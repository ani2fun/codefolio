package codefolio.shared.viz

import io.circe.Encoder
import io.circe.generic.semiauto.*

/**
 * The renderer's contract — a generic, layout-agnostic graph animation (ADR-0018).
 *
 * [[HeapToGraph]] produces a `VizGraph` from a [[HeapTrace]]; the Scala.js Visualise modal circe-encodes it
 * to JSON and hands it to the standalone D3 renderer (`client/src/d3`). The renderer draws nodes + edges and
 * animates the steps; *where* a node sits is decided by a pluggable layout selected from
 * [[VizGraph.layoutHint]]. The renderer is one generic module — adding a data structure adds a layout, never
 * a renderer.
 */

/** An extra display field on a node — a non-pointer scalar (e.g. `height=2`), shown as a sub-label. */
final case class VizField(name: String, value: String)

/**
 * A node in one animation step.
 *
 *   - `kind` — the originating Python class for an instance, or a synthetic kind for a collection member:
 *     `"cell"` (an array / list element) or `"entry"` (a dict entry). It drives CSS.
 *   - `meta` — the object's other scalar fields (everything but the primary value), surfaced as small
 *     sub-labels so the diagram shows per-node state (an AVL `height`, a colour, a dict entry's `key`, …),
 *     not just the value.
 *   - `slot` — an array cell's 0-based index; `None` for instances and dict entries. The array layout places
 *     cells by `slot`, so a value moves between fixed boxes the way an in-place sort does.
 */
final case class VizNode(
    id: String,
    label: String,
    kind: String,
    meta: List[VizField],
    slot: Option[Int] = None
)

/** A directed edge; `label` is the field it came from (e.g. "left" / "right"). */
final case class VizEdge(from: String, to: String, label: String)

/**
 * A frame-local variable pointing into the structure: `name` is the Python variable, `target` the id of the
 * node it references, `color` its role colour from [[MarkerColors]] (assigned by `HeapToGraph`, stable for
 * the whole trace). Drawn as a labelled, role-coloured caret over the node, so a step holding several
 * references — a long-lived `root` plus a moving `node` — shows *which* pointer sits where.
 */
final case class VizCursor(name: String, target: String, color: String = "")

/**
 * One animation step: the graph state + emphasis + the source line that produced it.
 *
 *   - `cursor` — the frame's local variables that point into the structure, each a name → node-id pair.
 *   - `highlight` — node ids that are new this step (freshly created / attached).
 *   - `changed` — node ids whose display value differs from the previous step.
 *   - `removed` — node ids gone since the previous step, re-emitted into `nodes` once so the renderer can
 *     fade them out; they are absent from every later step.
 *   - `annotation` — a short generated narration of this step's diff, shown beneath the diagram (the raw
 *     source line it replaces still shows, highlighted, in the modal's code pane).
 *   - `line` — 1-based source line, so the modal's code pane can highlight in sync.
 *
 * `highlight` is a persistent-layer cue (a new node keeps its tint); `changed` / `removed` are
 * transient-layer cues (a one-step flash / fade) — the renderer keeps the two on separate class sets.
 */
final case class VizGraphStep(
    nodes: List[VizNode],
    edges: List[VizEdge],
    cursor: List[VizCursor],
    highlight: List[String],
    changed: List[String],
    removed: List[String],
    annotation: String,
    line: Int
)

/** One traced test case's animation. `truncated` mirrors [[HeapTrace.truncated]]. */
final case class VizGraph(
    steps: List[VizGraphStep],
    layoutHint: String,
    title: String,
    truncated: Boolean
)

/**
 * The full payload handed to the D3 renderer — one [[VizGraph]] per traced test case.
 *
 * A DSA `main` typically runs several test cases in sequence; [[HeapToGraph]] segments the trace at each case
 * boundary (the viz-root variable rebound to a fresh structure) and emits one `VizGraph` per case. The
 * payload is a *list* of graphs rather than one graph carrying boundary indices because ADR-0018 computes
 * layout once over the union of every step — and each case is a different structure with a different union,
 * so each wants its own. The modal renders one case at a time and offers a "Case N / M" selector; a
 * single-case trace is just a `VizCases` of one.
 */
final case class VizCases(cases: List[VizGraph])

object VizGraph:
  given Encoder[VizField]     = deriveEncoder
  given Encoder[VizNode]      = deriveEncoder
  given Encoder[VizEdge]      = deriveEncoder
  given Encoder[VizCursor]    = deriveEncoder
  given Encoder[VizGraphStep] = deriveEncoder
  given Encoder[VizGraph]     = deriveEncoder
  given Encoder[VizCases]     = deriveEncoder
