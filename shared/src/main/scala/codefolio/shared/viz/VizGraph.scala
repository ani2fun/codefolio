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
 * A node in one animation step. `kind` is the originating class name (drives CSS); `meta` carries the
 * object's other scalar fields (everything but the primary value), surfaced as small sub-labels so the
 * diagram shows per-node state (an AVL `height`, a colour, …), not just the value.
 */
final case class VizNode(id: String, label: String, kind: String, meta: List[VizField])

/** A directed edge; `label` is the field it came from (e.g. "left" / "right"). */
final case class VizEdge(from: String, to: String, label: String)

/**
 * A frame-local variable pointing into the structure: `name` is the Python variable, `target` the id of the
 * node it references. Drawn as a labelled caret over the node, so a step holding several references — a
 * long-lived `root` plus a moving `node` — shows *which* pointer sits where, not anonymous carets.
 */
final case class VizCursor(name: String, target: String)

/**
 * One animation step: the graph state + emphasis + the source line that produced it.
 *
 *   - `cursor` — the frame's local variables that point into the structure, each a name → node-id pair.
 *   - `highlight` — node ids that are new this step (freshly created / attached).
 *   - `annotation` — the executing source line, shown beneath the diagram.
 *   - `line` — 1-based source line, so the modal's code pane can highlight in sync.
 */
final case class VizGraphStep(
    nodes: List[VizNode],
    edges: List[VizEdge],
    cursor: List[VizCursor],
    highlight: List[String],
    annotation: String,
    line: Int
)

/** The full payload handed to the D3 renderer. `truncated` mirrors [[HeapTrace.truncated]]. */
final case class VizGraph(
    steps: List[VizGraphStep],
    layoutHint: String,
    title: String,
    truncated: Boolean
)

object VizGraph:
  given Encoder[VizField]     = deriveEncoder
  given Encoder[VizNode]      = deriveEncoder
  given Encoder[VizEdge]      = deriveEncoder
  given Encoder[VizCursor]    = deriveEncoder
  given Encoder[VizGraphStep] = deriveEncoder
  given Encoder[VizGraph]     = deriveEncoder
