package codefolio.client.components.cortex.widgets

import codefolio.client.components.icons.LucideIcons
import codefolio.client.d3.D3
import japgolly.scalajs.react.*
// `Reusability[Double]` for the speed-multiplier dep in the play-loop effect.
// `WithoutTolerance` is the strict-equality variant — we only compare the
// fixed 0.5 / 1.0 / 2.0 sentinel values that the speed toggle buttons set,
// so no floating-point tolerance is needed.
import japgolly.scalajs.react.Reusability.DecimalImplicitsWithoutTolerance.reusabilityDouble
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.util.{Failure, Success, Try}

/**
 * Linked-list stepper — second widget in the D3 catalog after `array-traversal`. Renders a horizontal row of
 * value-bearing nodes connected by `next` arrows, with one or more named markers attached to specific nodes.
 * Steps can rewire arrows, add/remove nodes, and move markers.
 *
 * One widget handles both singly and doubly via a `direction` flag in the payload. Singly omits the back-row
 * `prev` arrows; doubly renders them dashed underneath the node row, auto-derived from the forward links
 * unless the author overrides per-link `kind: "prev"`. Cycle visualisations (Phase 1 chapter 1.5 — Floyd's)
 * set a top-level `cycleTarget` node id; the widget draws a cubic-Bezier back-edge from the tail node's
 * `next` to that target, styled with `.linked-list__cycle-edge`.
 *
 * Rendering follows the same React + D3 boundary as `ArrayTraversal` (ADR-0013): React owns a host `<div>`;
 * D3 owns the `<svg>` it creates inside that div. On step change a single update pass re-binds node data
 * (keyed by `node.id`) and arrow data (keyed by `${from}→${to}`) so a node that survives slot-changes
 * transitions its `transform` smoothly and a rewired arrow appears via fade-in rather than flicker.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":     "Insert after node 2",
 *   "direction": "single",                // "single" (default) or "double"
 *   "nodes": [
 *     {"id": "a", "value": "10"},
 *     {"id": "b", "value": "20"},
 *     {"id": "c", "value": "30"},
 *     {"id": "d", "value": "40"}
 *   ],
 *   "head":         "a",                  // optional; defaults to first node
 *   "cycleTarget":  null,                 // optional; node id the tail loops back to
 *   "steps": [
 *     {
 *       "nodes":   [...],                 // optional override; defaults to spec.nodes
 *       "links":   [["a","b"], ["b","c"], ["c","d"]],
 *       "markers": [{"name": "curr", "nodeId": "b", "color": "#3b82f6"}],
 *       "head":    "a",                   // optional override
 *       "msg":     "curr at node 20"
 *     },
 *     ...
 *   ]
 * }
 * }}}
 *
 * Links may also be authored as objects when more control is needed: `{"from":"a", "to":"b", "kind":"next"}`
 * (`kind` ∈ `"next" | "prev" | "broken"`).
 *
 * Per-node `style` (optional): `"new"` (emerald outline, fade-in), `"removed"` (red, dimmed), `"highlight"`
 * (blue tint). The styling is purely visual; it does not affect layout.
 */
object LinkedList:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose.
  // ---------------------------------------------------------------------------

  final case class Node(id: String, value: String, style: Option[String])
  final case class Link(from: String, to: String, kind: Option[String])
  // `canonical` is false when the marker name isn't in `CanonicalMarkers` —
  // the widget then renders an inline warning badge at the marker's node
  // instead of the usual triangle + label. Authors fix the typo (or amend
  // ADR-0016 to admit a new name); colours are always resolved from the
  // canon, never from the payload (the `color` field is dropped at parse
  // time, per ADR-0016's hard-reject stance).
  final case class Marker(name: String, nodeId: String, canonical: Boolean)

  final case class Step(
      nodes: Option[List[Node]],
      links: List[Link],
      markers: List[Marker],
      head: Option[String],
      msg: String
  )

  // Optional grouping of steps into named macro-phases — drawn as dividers in
  // the progress bar with a label above each segment. Use for long-step
  // sequences (≥ 10 steps) that have a clear macro shape, e.g. for Reorder:
  // `[{Split, 0}, {Reverse, 12}, {Merge, 19}]`. Empty list = flat progress
  // bar with no dividers.
  final case class Section(name: String, startIdx: Int)

  final case class Spec(
      direction: String,
      nodes: List[Node],
      head: Option[String],
      cycleTarget: Option[String],
      title: Option[String],
      steps: List[Step],
      sections: List[Section],
      // Optional row-wrap. When set, nodes split across rows of `wrapAt`
      // columns; cross-row links draw as a diagonal bezier from row N's
      // tail to row N+1's head. When unset (the common case for textbook
      // examples ≤ 8 nodes), the widget renders a single row — same layout
      // as before this option existed.
      wrapAt: Option[Int]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants. Wider gap than ArrayTraversal so arrows have room to
  // render their arrowhead between nodes without crowding the cell rects.
  // ---------------------------------------------------------------------------

  private val NodeSize     = 56.0
  private val NodeGap      = 36.0
  private val MarkerLaneH  = 44.0
  private val BackLaneH    = 36.0      // doubly's prev-row + cycle-edge clearance
  private val PaddingX     = 24.0
  private val PaddingY     = 12.0
  private val StepDelayMs  = 1200
  private val ArrowheadW   = 8.0
  private val ArrowheadH   = 6.0
  private val WarningColor = "#ef4444" // rose — used for canon-violation badges
  private val SvgNs        = "http://www.w3.org/2000/svg"

  // ── Canon (ADR-0016) ────────────────────────────────────────────────────────
  // The closed marker vocabulary. Each name carries one role and one colour
  // across every linked-list diagram in the section, so a reader's mental
  // model is consistent — `head` is always blue, `curr` is always emerald,
  // `slow` and `fast` always blue/rose, regardless of which chapter or which
  // operation. Authors cannot override colours; the `color` field on
  // payload markers is silently dropped at parse time.
  //
  // To grow the canon (e.g. adding `temp` or `pivot` for a future pattern):
  // amend ADR-0016, then add an entry here. Every other code path reads
  // through this map.
  private val CanonicalMarkers: Map[String, String] = Map(
    "head"  -> "#3b82f6", // blue   — list entry
    "tail"  -> "#64748b", // slate  — explicit last-node tracker
    "prev"  -> "#f59e0b", // amber  — trailing pointer (reversal)
    "curr"  -> "#10b981", // emerald — active pointer
    "next"  -> "#a855f7", // violet — saved-next reference
    "slow"  -> "#3b82f6", // blue   — slow (Floyd, two-pointer)
    "fast"  -> "#ef4444", // rose   — fast (Floyd, two-pointer)
    "dummy" -> "#64748b", // slate  — sentinel / dummy head
    "start" -> "#f59e0b", // amber  — segment start
    "end"   -> "#10b981"  // emerald — segment end
  )

  // Canonical node-style vocabulary. Unknown styles are dropped at parse
  // time (the node renders with the default look) and a dev-mode console
  // warning is logged so the author sees the typo in their preview.
  private val CanonicalNodeStyles: Set[String] = Set("new", "removed", "highlight")

  // ---------------------------------------------------------------------------
  // Parsing — `js.JSON.parse` + `js.Dynamic` mirrors ArrayTraversal.parsePayload.
  // Anything that throws collapses to a `Left(msg)`; the component renders an
  // error placeholder rather than crashing the chapter.
  // ---------------------------------------------------------------------------

  private def parseNodes(arr: js.UndefOr[js.Array[js.Dynamic]]): List[Node] =
    arr.toOption
      .getOrElse(js.Array())
      .toList
      .map { n =>
        val rawStyle = n.style.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
        val style = rawStyle.flatMap { s =>
          if CanonicalNodeStyles.contains(s) then Some(s)
          else
            dom.console.warn(
              s"linked-list: unknown node style '$s' (expected one of: ${CanonicalNodeStyles.mkString(", ")}). Dropping."
            )
            None
        }
        Node(
          id = n.id.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
          value = n.value.asInstanceOf[js.UndefOr[js.Any]].toOption.fold("")(v =>
            js.Dynamic.global.String(v).asInstanceOf[String]
          ),
          style = style
        )
      }
      .filter(_.id.nonEmpty)

  private def parseLinks(arr: js.UndefOr[js.Array[js.Any]]): List[Link] =
    arr.toOption.getOrElse(js.Array()).toList.flatMap { entry =>
      // Accept both ["a","b"] and {"from":"a","to":"b","kind":"next"}.
      entry match
        case a: js.Array[?] =>
          val arr2 = a.asInstanceOf[js.Array[js.Any]]
          if arr2.length >= 2 then
            val from = js.Dynamic.global.String(arr2(0)).asInstanceOf[String]
            val to   = js.Dynamic.global.String(arr2(1)).asInstanceOf[String]
            if from.nonEmpty && to.nonEmpty then Some(Link(from, to, None)) else None
          else None
        case o =>
          val dyn  = o.asInstanceOf[js.Dynamic]
          val from = dyn.from.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
          val to   = dyn.to.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
          val kind = dyn.kind.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
          if from.nonEmpty && to.nonEmpty then Some(Link(from, to, kind)) else None
    }

  private def parseMarkers(arr: js.UndefOr[js.Array[js.Dynamic]]): List[Marker] =
    arr.toOption
      .getOrElse(js.Array())
      .toList
      .map { m =>
        val name      = m.name.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        val nodeId    = m.nodeId.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        val rawColor  = m.color.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
        val canonical = CanonicalMarkers.contains(name)
        // Hard-reject author colours (ADR-0016) — log a one-line dev warning
        // so the author sees they wrote something that's being ignored.
        if rawColor.isDefined then
          dom.console.warn(
            s"linked-list: marker '$name' carries a `color` field — dropping (colour is resolved from the canon, not the payload)."
          )
        if name.nonEmpty && !canonical then
          dom.console.warn(
            s"linked-list: marker name '$name' is not in the canonical vocabulary. Rendered as an inline warning. Canonical names: ${CanonicalMarkers.keys.toList.sorted.mkString(", ")}."
          )
        Marker(name, nodeId, canonical)
      }
      .filter(m => m.name.nonEmpty && m.nodeId.nonEmpty)

  private def parseSections(arr: js.UndefOr[js.Array[js.Dynamic]], stepCount: Int): List[Section] =
    val parsed = arr.toOption.getOrElse(js.Array()).toList.flatMap { s =>
      val name     = s.name.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
      val startIdx = s.startIdx.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(-1)
      if name.nonEmpty && startIdx >= 0 && startIdx < stepCount then Some(Section(name, startIdx))
      else
        if name.nonEmpty then
          dom.console.warn(
            s"linked-list: section '$name' has invalid startIdx=$startIdx (steps=$stepCount). Dropping."
          )
        None
    }
    // Enforce strictly-increasing startIdx — keep the first section per start
    // and drop any later one that doesn't advance.
    parsed
      .sortBy(_.startIdx)
      .foldLeft(List.empty[Section]) { (acc, s) =>
        if acc.isEmpty || acc.last.startIdx < s.startIdx then acc :+ s else acc
      }

  private def parsePayload(json: String): Either[String, Spec] =
    Try {
      val raw = js.JSON.parse(json).asInstanceOf[js.Dynamic]
      val dir = raw.direction.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty).getOrElse("single")
      val nodes = parseNodes(raw.nodes.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]])
      val head  = raw.head.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val cycleTarget =
        raw.cycleTarget.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val title = raw.title.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val rawSteps = raw.steps
        .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
        .toOption
        .getOrElse(js.Array())
      val steps = rawSteps.toList.map { s =>
        val perStepNodes =
          s.nodes.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]].toOption.map(arr => parseNodes(arr))
        val links =
          parseLinks(s.links.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
        val markers =
          parseMarkers(s.markers.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]])
        val stepHead =
          s.head.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
        val msg = s.msg.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        Step(perStepNodes, links, markers, stepHead, msg)
      }
      val sections =
        parseSections(raw.sections.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]], steps.size)
      val wrapAt = raw.wrapAt.asInstanceOf[js.UndefOr[Int]].toOption.filter(_ > 0)
      Spec(dir, nodes, head, cycleTarget, title, steps, sections, wrapAt)
    } match
      case Success(spec) if spec.nodes.isEmpty => Left("payload.nodes must be non-empty")
      case Success(spec)                       => Right(spec)
      case Failure(t)                          => Left(Option(t.getMessage).getOrElse("invalid payload JSON"))

  // ---------------------------------------------------------------------------
  // Layout helpers
  // ---------------------------------------------------------------------------

  private def clamp(i: Int, count: Int): Int =
    if count <= 0 then 0 else math.max(0, math.min(count - 1, i))

  // Column-relative x. `col` is the position within a row (0..nodesPerRow-1).
  private def nodeXAtCol(col: Int): Double =
    PaddingX + col * (NodeSize + NodeGap)

  private def nodeCenterXAtCol(col: Int): Double =
    nodeXAtCol(col) + NodeSize / 2

  private def rowHeightOf(hasBack: Boolean): Double =
    MarkerLaneH + NodeSize + (if hasBack then BackLaneH else 0.0)

  // Top of node row, given `row` (0..rowsCount-1). The marker lane sits above
  // each row of nodes (at `y - 8`), the back lane below (`y + NodeSize + 10`).
  private def nodeYAtRow(row: Int, hasBack: Boolean): Double =
    PaddingY + row * rowHeightOf(hasBack) + MarkerLaneH

  private def rowsCount(nodeCount: Int, nodesPerRow: Int): Int =
    if nodeCount <= 0 then 0
    else ((nodeCount - 1) / nodesPerRow) + 1

  private def viewBoxWidth(nodeCount: Int, nodesPerRow: Int): Double =
    val cols = math.min(nodesPerRow, math.max(1, nodeCount))
    PaddingX * 2 + cols * NodeSize + (cols - 1).max(0) * NodeGap

  private def viewBoxHeight(nodeCount: Int, nodesPerRow: Int, hasBack: Boolean): Double =
    PaddingY * 2 + rowsCount(nodeCount, nodesPerRow) * rowHeightOf(hasBack)

  // Effective wrap width — `spec.wrapAt` if set, else `nodeCount` (single row).
  // Clamped to ≥ 1 so an empty payload doesn't divide by zero.
  private def nodesPerRowOf(spec: Spec, nodeCount: Int): Int =
    math.max(1, spec.wrapAt.getOrElse(math.max(1, nodeCount)))

  private def nodesFor(spec: Spec, step: Step): List[Node] =
    step.nodes.getOrElse(spec.nodes)

  private def hasBackLane(spec: Spec): Boolean =
    spec.direction == "double" || spec.cycleTarget.isDefined ||
      spec.steps.exists(_.links.exists(_.kind.contains("prev")))

  private def colorFor(marker: Marker): String =
    CanonicalMarkers.getOrElse(marker.name, WarningColor)

  // For doubly mode: if author didn't include any prev links, auto-derive them
  // by reversing each forward link.
  private def expandLinksForDirection(direction: String, links: List[Link]): List[Link] =
    if direction != "double" then links
    else
      val hasAnyPrev = links.exists(_.kind.contains("prev"))
      if hasAnyPrev then links
      else
        val backs = links.filterNot(_.kind.contains("broken")).map(l => Link(l.to, l.from, Some("prev")))
        links ++ backs

  // Rough text width estimate for the 11px font used by marker labels.
  // 6.5 px/char is a serviceable mean for a proportional sans (Geist Sans);
  // the warning prefix `⚠ ` adds about two visual char-widths. Used by
  // `assignMarkerRanks` for collision detection without a costly
  // `getComputedTextLength()` round-trip through the DOM.
  private def estimateLabelWidth(name: String, canonical: Boolean): Double =
    val effectiveLen = if canonical then name.length else name.length + 2
    effectiveLen * 6.5 + 4.0

  // Greedy rank assignment — for each marker in payload order, find the
  // lowest rank (vertical row above the node row) where its label's
  // estimated x-range doesn't overlap with any earlier marker already
  // assigned to that rank. Adjacent nodes whose long labels would clash
  // get stacked: the first stays at rank 0, the second moves up to rank 1,
  // etc. Replaces the old per-node-only rank logic, which only handled
  // multiple markers AT THE SAME node and let cross-node labels overlap.
  private def assignMarkerRanks(
      markers: List[Marker],
      nodeCenterXOf: String => Double
  ): List[(Marker, Int)] =
    val ranges = markers.map { m =>
      val w  = estimateLabelWidth(m.name, m.canonical)
      val cx = nodeCenterXOf(m.nodeId)
      (cx - w / 2, cx + w / 2)
    }
    val assigned = scala.collection.mutable.ListBuffer.empty[(Int, (Double, Double))]
    val ranks = markers.indices.map { i =>
      val rng = ranges(i)
      val r = LazyList
        .from(0)
        .find { candidate =>
          !assigned.exists { case (existRank, existRng) =>
            existRank == candidate && existRng._2 > rng._1 && existRng._1 < rng._2
          }
        }
        .get
      assigned += (r -> rng)
      r
    }.toList
    markers.zip(ranks)

  // ---------------------------------------------------------------------------
  // Mount the SVG element inside the host div on first call; return the SVG
  // element for D3 to manipulate. Subsequent calls return the existing SVG.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      val maxNodes = spec.steps.foldLeft(spec.nodes.size) { (acc, s) =>
        math.max(acc, s.nodes.fold(spec.nodes.size)(_.size))
      }
      val nodesPerRow = nodesPerRowOf(spec, maxNodes)
      val hasBack     = hasBackLane(spec)
      val width       = viewBoxWidth(maxNodes, nodesPerRow)
      val height      = viewBoxHeight(maxNodes, nodesPerRow, hasBack)
      val titleAt     = spec.title.getOrElse("Linked list")
      svg.setAttribute("class", "linked-list__svg")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", titleAt)
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 $width $height")
      // Pin the intrinsic SVG size to the viewBox dimensions so the browser
      // doesn't stretch the SVG to fill its flex parent. CSS `max-w-full
      // h-auto` then shrinks long widgets to fit narrow screens while
      // preserving the aspect ratio.
      svg.setAttribute("width", width.toString)
      svg.setAttribute("height", height.toString)
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // D3 render — runs after every step change. Idempotent: the first call sets up
  // the SVG structure via enter selections; subsequent calls update positions /
  // text / arrows via transitions on the same elements.
  // ---------------------------------------------------------------------------

  // Per-render transition durations. `animate=false` snaps into place — the
  // first render of every widget instance, so the chapter doesn't visibly
  // animate on page load. Subsequent renders (step changes) use full
  // durations so rewires, slot-moves, and marker shuffles read as motion.
  // Powered by the `d3-transition` side-effect import in `D3.scala` —
  // without that import these all silently no-op.
  private val MoveDurMs = 250.0
  private val FadeInMs  = 450.0
  private val FadeOutMs = 300.0

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val moveDur     = if animate then MoveDurMs else 0.0
    val fadeIn      = if animate then FadeInMs else 0.0
    val fadeOut     = if animate then FadeOutMs else 0.0
    val svg         = D3.select(svgEl)
    val nodes       = nodesFor(spec, step)
    val nodeCount   = nodes.size
    val hasBack     = hasBackLane(spec)
    val nodesPerRow = nodesPerRowOf(spec, math.max(1, nodeCount))
    val width       = viewBoxWidth(nodeCount, nodesPerRow)
    val height      = viewBoxHeight(nodeCount, nodesPerRow, hasBack)

    // Per-node layout closures. These thread `nodesPerRow` and `hasBack`
    // through every callsite that used to read the row-0 constants
    // (`nodeY`, `nodeCenterYV`, `markerLaneY`, `backLaneY`). For a single-
    // row chain (the common case — `spec.wrapAt` unset), every index has
    // row=0 and the values match the original constants exactly.
    def rowOf(i: Int): Int            = i / nodesPerRow
    def colOf(i: Int): Int            = i % nodesPerRow
    def xAt(i: Int): Double           = nodeXAtCol(colOf(i))
    def yAt(i: Int): Double           = nodeYAtRow(rowOf(i), hasBack)
    def cxAt(i: Int): Double          = nodeCenterXAtCol(colOf(i))
    def cyAt(i: Int): Double          = yAt(i) + NodeSize / 2
    def markerLaneYAt(i: Int): Double = yAt(i) - 8.0
    def backLaneYAt(i: Int): Double   = yAt(i) + NodeSize + 10.0

    val _ = svg.attr("viewBox", s"0 0 $width $height")

    // ── Empty-step placeholder ───────────────────────────────────────────────
    // Steps with no active nodes (used pedagogically for "the list is empty
    // → nothing to do" cases) would otherwise render an empty SVG rectangle.
    // Show a centred "(empty list)" instead. Tear down anything left over
    // from a previous step at the same time so a multi-step widget that
    // transitions from a populated step into an empty step also clears its
    // node/arrow/marker DOM.
    if nodes.isEmpty then
      val ensureCanvasW                              = math.max(width, 240.0) // keep some breathing room
      val placeholderY                               = nodeYAtRow(0, hasBack) + NodeSize / 2 + 6
      val _                                          = svg.attr("viewBox", s"0 0 $ensureCanvasW $height")
      val _                                          = svg.selectAll("g.linked-list__node").remove()
      val _                                          = svg.selectAll("g.linked-list__arrow-group").remove()
      val _                                          = svg.selectAll("g.linked-list__marker").remove()
      val _                                          = svg.selectAll("path.linked-list__cycle-edge").remove()
      val phData: js.Array[js.Any]                   = js.Array("(empty list)").asInstanceOf[js.Array[js.Any]]
      val phKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "ph"
      val phSel = svg.selectAll("text.linked-list__empty-placeholder").data(phData, phKeyFn)
      val _ = phSel
        .enter()
        .append("text")
        .attr("class", "linked-list__empty-placeholder")
        .attr("text-anchor", "middle")
        .attr("x", ensureCanvasW / 2)
        .attr("y", placeholderY)
        .text("(empty list)")
      val _ = svg
        .selectAll("text.linked-list__empty-placeholder")
        .attr("x", ensureCanvasW / 2)
        .attr("y", placeholderY)
      return ()
    else
      // Tear down placeholder if a prior step rendered it
      val _ = svg.selectAll("text.linked-list__empty-placeholder").remove()

    // Build an index-by-id lookup for arrow-endpoint math. Nodes that disappear
    // mid-step still appear here briefly if they're in the post-step `nodes`
    // list; arrows pointing at unknown ids drop silently.
    val idxOf: Map[String, Int] = nodes.zipWithIndex.map { case (n, i) => n.id -> i }.toMap

    // ── Nodes ────────────────────────────────────────────────────────────────
    val nodeData: js.Array[js.Any] = nodes.zipWithIndex.map { case (n, i) =>
      js.Dynamic
        .literal(id = n.id, value = n.value, style = n.style.getOrElse(""), index = i)
        .asInstanceOf[js.Any]
    }.toJSArray
    val nodeKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].id

    val nodeSel = svg.selectAll("g.linked-list__node").data(nodeData, nodeKeyFn)

    val nodeClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val style = d.asInstanceOf[js.Dynamic].style.asInstanceOf[String]
      if style.nonEmpty then s"linked-list__node linked-list__node--$style"
      else "linked-list__node"
    val nodeTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val i = d.asInstanceOf[js.Dynamic].index.asInstanceOf[Int]
      s"translate(${xAt(i)}, ${yAt(i)})"

    val nodeEnter = nodeSel
      .enter()
      .append("g")
      .attr("class", nodeClassFn)
      .attr("transform", nodeTransform)
      // Enter at opacity 0; fade in via the transition started after the
      // child elements are appended below. The fade-in keeps inserted nodes
      // (style "new") from popping; with `animate=false` (first render) the
      // 0-duration transition snaps to 1 immediately.
      .attr("opacity", 0)

    val _ = nodeEnter
      .append("rect")
      .attr("class", "linked-list__node-rect")
      .attr("x", 0)
      .attr("y", 0)
      .attr("width", NodeSize)
      .attr("height", NodeSize)
      .attr("rx", 8)

    val _ = nodeEnter
      .append("text")
      .attr("class", "linked-list__node-label")
      .attr("x", NodeSize / 2)
      .attr("y", NodeSize / 2 + 5)
      .attr("text-anchor", "middle")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])

    val _ = nodeEnter
      .transition("ll-node-fade-in")
      .duration(fadeIn)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)

    // Exit: fade out, then remove. The named transition keeps fade-out from
    // colliding with concurrent enter/update transitions on overlapping
    // selections (D3's default-namespace would otherwise cancel one).
    val _ = nodeSel
      .exit()
      .transition("ll-node-fade-out")
      .duration(fadeOut)
      .attr("opacity", 0)
      .remove()

    // Update: re-bind class (style may change), text, and slide transform to
    // the new index position. The transform transition is the load-bearing
    // animation — a reordered node visibly slides to its new slot.
    val nodeAll = svg.selectAll("g.linked-list__node")
    val _       = nodeAll.attr("class", nodeClassFn)
    val _ = nodeAll
      .select("text.linked-list__node-label")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _ = nodeAll
      .transition("ll-node-move")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("transform", nodeTransform)

    // ── Arrows (links) ───────────────────────────────────────────────────────
    val expandedLinks = expandLinksForDirection(spec.direction, step.links)
      .filter(l => idxOf.contains(l.from) && idxOf.contains(l.to))

    val linkData: js.Array[js.Any] = expandedLinks.map { l =>
      val kind = l.kind.getOrElse("next")
      js.Dynamic
        .literal(from = l.from, to = l.to, kind = kind)
        .asInstanceOf[js.Any]
    }.toJSArray
    // Key arrows by source + kind (not by full from→to). This way, when a
    // node's `next` rewires from one target to another between steps, the
    // SAME arrow element survives — d3's keyed-join update path then runs a
    // path-attribute transition that smoothly interpolates the embedded
    // x/y numbers, so the reader watches the arrow's tip slide from old
    // target to new. Without this re-keying the old arrow would exit and a
    // new one would enter, producing a snap rather than a slide.
    val linkKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val dyn = d.asInstanceOf[js.Dynamic]
        s"${dyn.from.asInstanceOf[String]}-${dyn.kind.asInstanceOf[String]}"

    def linkPath(d: js.Any): String =
      val dyn      = d.asInstanceOf[js.Dynamic]
      val from     = dyn.from.asInstanceOf[String]
      val to       = dyn.to.asInstanceOf[String]
      val kind     = dyn.kind.asInstanceOf[String]
      val si       = idxOf.getOrElse(from, -1)
      val ti       = idxOf.getOrElse(to, -1)
      val arrowGap = 6.0
      if si < 0 || ti < 0 then ""
      else if rowOf(si) != rowOf(ti) && kind != "prev" then
        // Cross-row wrap arrow — S-curve from source's right edge down/over
        // to the target's left edge on the next row. Used when `spec.wrapAt`
        // splits a long chain across rows; lets the chain read like wrapped
        // prose without forcing a single oversized SVG.
        val sx   = xAt(si) + NodeSize
        val sy   = cyAt(si)
        val tx   = xAt(ti) - arrowGap
        val ty   = cyAt(ti)
        val cp1x = sx + 30.0
        val cp2x = tx - 30.0
        s"M $sx $sy C $cp1x $sy, $cp2x $ty, $tx $ty"
      else
        val sx0      = xAt(si) + (if kind == "prev" then NodeSize / 2 - 8 else NodeSize)
        val sx1      = cxAt(si)
        val tx1      = cxAt(ti)
        val sCenterY = cyAt(si)
        val tCenterY = cyAt(ti)
        if kind == "prev" then
          // Below the row, dashed; curve out and back to avoid overlapping
          // the next-row arrow. Anchors to the source's row's back lane —
          // if a cross-row prev happens (rare, auto-derived for doubly),
          // the curve will visually mis-route but won't crash.
          val cy = backLaneYAt(si) + BackLaneH * 0.55
          s"M $sx1 $sCenterY C $sx1 $cy, $tx1 $cy, $tx1 $tCenterY"
        else
          // Same-row straight arrow. End slightly before the target so the
          // arrowhead doesn't overlap the cell border.
          val (start, end) =
            if ti > si then (sx0, xAt(ti) - arrowGap)
            else if ti < si then (xAt(si), xAt(ti) + NodeSize + arrowGap)
            else (sx0, tx1) // self-loop fallback (unusual)
          s"M $start $sCenterY L $end $sCenterY"

    def arrowheadPath(d: js.Any): String =
      val dyn      = d.asInstanceOf[js.Dynamic]
      val from     = dyn.from.asInstanceOf[String]
      val to       = dyn.to.asInstanceOf[String]
      val kind     = dyn.kind.asInstanceOf[String]
      val si       = idxOf.getOrElse(from, -1)
      val ti       = idxOf.getOrElse(to, -1)
      val arrowGap = 6.0
      if si < 0 || ti < 0 then ""
      else if rowOf(si) != rowOf(ti) && kind != "prev" then
        // Cross-row: arrowhead points right at target's left edge (target's
        // approach direction is essentially horizontal at the end of the S-
        // curve, so a rightward triangle reads naturally).
        val tipX = xAt(ti) - arrowGap
        val tipY = cyAt(ti)
        s"M ${tipX - ArrowheadW} ${tipY - ArrowheadH} L ${tipX - ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"
      else if kind == "prev" then
        // Arrowhead at the source-side (prev points back).
        val tipX = cxAt(ti)
        val tipY = cyAt(ti)
        s"M ${tipX - ArrowheadW} ${tipY + ArrowheadH} L ${tipX + ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"
      else
        val tipX = if ti > si then xAt(ti) - arrowGap else xAt(ti) + NodeSize + arrowGap
        val tipY = cyAt(ti)
        if ti > si then
          s"M ${tipX - ArrowheadW} ${tipY - ArrowheadH} L ${tipX - ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"
        else
          s"M ${tipX + ArrowheadW} ${tipY - ArrowheadH} L ${tipX + ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"

    val linkClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val kind = d.asInstanceOf[js.Dynamic].kind.asInstanceOf[String]
      val base = "linked-list__arrow-group"
      kind match
        case "prev"   => s"$base linked-list__arrow-group--back"
        case "broken" => s"$base linked-list__arrow-group--broken"
        case _        => base
    val linkDFn: js.Function2[js.Any, Int, js.Any]      = (d, _) => linkPath(d)
    val arrowheadDFn: js.Function2[js.Any, Int, js.Any] = (d, _) => arrowheadPath(d)

    val linkSel = svg.selectAll("g.linked-list__arrow-group").data(linkData, linkKeyFn)

    val linkEnter = linkSel
      .enter()
      .append("g")
      .attr("class", linkClassFn)
      .attr("opacity", 0)
    val _ = linkEnter
      .append("path")
      .attr("class", "linked-list__arrow")
      .attr("fill", "none")
      .attr("d", linkDFn)
    val _ = linkEnter
      .append("path")
      .attr("class", "linked-list__arrowhead")
      .attr("d", arrowheadDFn)
    val _ = linkEnter
      .transition("ll-arrow-fade-in")
      .duration(fadeIn)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)

    // Exit: fade out, then remove. Named transition so it can run alongside
    // concurrent enter/update transitions without D3's default-namespace
    // cancellation rule killing one.
    val _ = linkSel
      .exit()
      .transition("ll-arrow-fade-out")
      .duration(fadeOut)
      .attr("opacity", 0)
      .remove()

    // Update: re-bind the class (kind may shift e.g. next → broken) and
    // smoothly transition the path `d` attributes. Because the arrow + the
    // arrowhead are two paths inside the same <g>, both get the same named
    // transition so they stay in sync as the line and the tip slide together.
    val linkAll = svg.selectAll("g.linked-list__arrow-group")
    val _       = linkAll.attr("class", linkClassFn)
    val _ = linkAll
      .select("path.linked-list__arrow")
      .transition("ll-arrow-path")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("d", linkDFn)
    val _ = linkAll
      .select("path.linked-list__arrowhead")
      .transition("ll-arrowhead-path")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("d", arrowheadDFn)

    // ── Cycle back-edge (Floyd's) ────────────────────────────────────────────
    val cycleData: js.Array[js.Any] = spec.cycleTarget
      .filter(idxOf.contains)
      .toList
      .map { targetId =>
        // Tail = the rightmost node in the row. For an explicit cycle list,
        // authors place the loop-back source as the last node in `nodes`.
        // Heuristic: prefer a node that's a `to` but never a `from` (true
        // dangling tail of the forward chain); fall back to nodes.last.
        val froms = step.links.filterNot(_.kind.contains("prev")).map(_.from).toSet
        val dangling =
          nodes.find(n => !froms.contains(n.id) && n.id != targetId).map(_.id)
        val tailId = dangling.getOrElse(nodes.last.id)
        js.Dynamic.literal(from = tailId, to = targetId).asInstanceOf[js.Any]
      }
      .toJSArray
    val cycleKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "cycle"
    val cycleSel = svg.selectAll("path.linked-list__cycle-edge").data(cycleData, cycleKeyFn)

    val cyclePath: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val si  = idxOf.getOrElse(dyn.from.asInstanceOf[String], -1)
      val ti  = idxOf.getOrElse(dyn.to.asInstanceOf[String], -1)
      if si < 0 || ti < 0 then ""
      else
        val sx = cxAt(si)
        val tx = cxAt(ti)
        val sy = cyAt(si)
        val ty = cyAt(ti)
        val cy = backLaneYAt(si) + BackLaneH * 0.65
        s"M $sx $sy C $sx $cy, $tx $cy, $tx $ty"
    val _ = cycleSel
      .enter()
      .append("path")
      .attr("class", "linked-list__cycle-edge")
      .attr("fill", "none")
      .attr("d", cyclePath)
      .attr("opacity", 0)
      .transition("ll-cycle-fade-in")
      .duration(fadeIn)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = cycleSel
      .exit()
      .transition("ll-cycle-fade-out")
      .duration(fadeOut)
      .attr("opacity", 0)
      .remove()
    val cycleAll = svg.selectAll("path.linked-list__cycle-edge")
    val _ = cycleAll
      .transition("ll-cycle-path")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("d", cyclePath)

    // ── Markers ──────────────────────────────────────────────────────────────
    // Multiple markers may attach to the same node (e.g. `head` + `current`
    // both at the head on traversal start). To avoid the labels overlapping,
    // we compute a per-node `rank`: the first marker at a node renders the
    // arrowhead triangle + its label one slot above the row; subsequent
    // markers at the same node stack their labels upward, no extra triangle.
    val activeMarkers = step.markers.filter(m => idxOf.contains(m.nodeId))
    // Globally-rank-assigned (Phase 1.6) so long names like `⚠ previous=null`
    // attached to adjacent nodes stack vertically instead of overlapping
    // horizontally. Two markers at the same node still stack the same way.
    val rankedMarkers = assignMarkerRanks(
      activeMarkers,
      nodeId => cxAt(idxOf.getOrElse(nodeId, 0))
    )
    val markerData: js.Array[js.Any] = rankedMarkers
      .map { case (m, rank) =>
        js.Dynamic
          .literal(
            name = m.name,
            nodeId = m.nodeId,
            color = colorFor(m),
            rank = rank,
            canonical = m.canonical
          )
          .asInstanceOf[js.Any]
      }
      .toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name

    def markerLaneYForNode(nodeId: String): Double =
      markerLaneYAt(idxOf.getOrElse(nodeId, 0))

    val centerOfMarker: (js.Any, Int) => Double =
      (d, _) =>
        val nodeId = d.asInstanceOf[js.Dynamic].nodeId.asInstanceOf[String]
        cxAt(idxOf.getOrElse(nodeId, 0))

    val markerLaneRowH = 14.0
    val labelY: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn    = d.asInstanceOf[js.Dynamic]
      val rank   = dyn.rank.asInstanceOf[Int]
      val nodeId = dyn.nodeId.asInstanceOf[String]
      val lY     = markerLaneYForNode(nodeId)
      (lY - 6 - rank * markerLaneRowH).toString
    // Skip the triangle for non-canonical markers — the warning text speaks
    // for itself and the missing arrowhead makes the violation visually
    // obvious. Canonical markers get a triangle only at rank 0 (so stacked
    // markers at the same node share one pointer). Triangle Y comes from
    // the marker's node's row (multi-row layouts have a marker lane per row).
    val trianglePathFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val rank      = dyn.rank.asInstanceOf[Int]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val nodeId    = dyn.nodeId.asInstanceOf[String]
      val lY        = markerLaneYForNode(nodeId)
      if !canonical || rank > 0 then ""
      else s"M -5 ${lY - 2} L 5 ${lY - 2} L 0 ${lY + 6} Z"

    val markerTransform: js.Function2[js.Any, Int, js.Any] =
      (d, i) => s"translate(${centerOfMarker(d, i)}, 0)"
    val markerFill: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].color
    val markerNameFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val dyn       = d.asInstanceOf[js.Dynamic]
        val canonical = dyn.canonical.asInstanceOf[Boolean]
        val name      = dyn.name.asInstanceOf[String]
        if canonical then name else s"⚠ $name" // ⚠ — visually flag the violation
    val markerClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val canonical = d.asInstanceOf[js.Dynamic].canonical.asInstanceOf[Boolean]
      if canonical then "linked-list__marker"
      else "linked-list__marker linked-list__marker--warning"

    val markerSel = svg.selectAll("g.linked-list__marker").data(markerData, markerKeyFn)

    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", markerClassFn)
      .attr("transform", markerTransform)
      .attr("opacity", 0)
    val _ = markerEnter
      .append("path")
      .attr("d", trianglePathFn)
      .attr("fill", markerFill)
    val _ = markerEnter
      .append("text")
      .attr("class", "linked-list__marker-label")
      .attr("x", 0)
      .attr("y", labelY)
      .attr("text-anchor", "middle")
      .attr("fill", markerFill)
      .text(markerNameFn)
    val _ = markerEnter
      .transition("ll-marker-fade-in")
      .duration(fadeIn)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)

    val _ = markerSel
      .exit()
      .transition("ll-marker-fade-out")
      .duration(fadeOut)
      .attr("opacity", 0)
      .remove()

    val markerAll = svg.selectAll("g.linked-list__marker")
    val _         = markerAll.attr("class", markerClassFn)
    val _ = markerAll
      .transition("ll-marker-move")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("transform", markerTransform)
    val _ = markerAll
      .select("path")
      .attr("d", trianglePathFn)
      .attr("fill", markerFill)
    val _ = markerAll
      .select("text.linked-list__marker-label")
      .attr("y", labelY)
      .attr("fill", markerFill)
      .text(markerNameFn)

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .useState(0)                      // step index
      .useState(false)                  // playing
      .useState(1.0)                    // speed multiplier (0.5 / 1.0 / 2.0)
      .useRefBy(_ => Option.empty[Int]) // play timeout id
      .useRefToVdom[dom.html.Element]   // host div ref — D3 manages the <svg> inside
      .useRefBy(_ => false)             // hasRendered (mutable; avoids re-render cycle)
      // ── play-loop timer ─────────────────────────────────────────────────────
      // Tick the step forward every (StepDelayMs / speed) ms while playing.
      // Speed-toggle changes are a dep so the timer reschedules immediately,
      // not at the next tick — flipping to 2× while playing shouldn't wait
      // 1.2s for the change to take effect.
      .useEffectWithDepsBy((_, specM, indexS, playingS, speedS, _, _, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), indexS.value, playingS.value, speedS.value)
      ) { (_, _, indexS, playingS, _, timeoutRef, _, _) => (count, index, playing, speed) =>
        Callback {
          timeoutRef.value.foreach(dom.window.clearTimeout)
          timeoutRef.value = None
          if playing then
            if index >= count - 1 then playingS.setState(false).runNow()
            else
              val delayMs = StepDelayMs / math.max(0.25, speed)
              val id      = dom.window.setTimeout(() => indexS.setState(index + 1).runNow(), delayMs)
              timeoutRef.value = Some(id)
        }
      }
      // ── D3 render on every step / spec change ───────────────────────────────
      .useEffectWithDepsBy((_, specM, indexS, _, _, _, _, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), indexS.value)
      ) { (_, specM, _, _, _, _, hostRef, hasRenderedRef) => (count, index) =>
        specM.value.toOption.filter(_.steps.nonEmpty) match
          case Some(spec) =>
            hostRef.foreach { host =>
              val svgEl   = ensureSvg(host, spec)
              val step    = spec.steps(clamp(index, count))
              val animate = hasRenderedRef.value
              renderStep(svgEl, spec, step, animate)
              if !hasRenderedRef.value then hasRenderedRef.value = true
            }
          case None => Callback.empty
      }
      .render { (_, specM, indexS, playingS, speedS, _, hostRef, _) =>
        specM.value match
          case Left(err) =>
            <.div(
              ^.className := "d3-widget__error",
              <.p(^.className   := "d3-widget__error-title", "Linked list payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = clamp(indexS.value, math.max(1, count))
            val currentStep =
              if count == 0 then Step(None, Nil, Nil, None, "No steps defined.")
              else spec.steps(idx)
            val atStart = idx == 0
            val atEnd   = count == 0 || idx == count - 1

            val previous =
              playingS.setState(false) >> indexS.modState(i => clamp(i - 1, math.max(1, count)))
            val next =
              playingS.setState(false) >> indexS.modState(i => clamp(i + 1, math.max(1, count)))
            val reset =
              playingS.setState(false) >> indexS.setState(0)
            val togglePlay =
              if playingS.value then playingS.setState(false)
              else
                val rewind = if atEnd then indexS.setState(0) else Callback.empty
                rewind >> playingS.setState(true)
            def jumpTo(i: Int): Callback =
              playingS.setState(false) >> indexS.setState(clamp(i, math.max(1, count)))

            // Helper — render section labels above the progress bar. Each
            // label spans from its section's startIdx to the next section's
            // startIdx (or to the end), positioned by percent over the bar.
            def sectionLabels: VdomNode =
              if spec.sections.isEmpty then EmptyVdom
              else
                <.div(
                  ^.className := "linked-list__section-labels",
                  spec.sections.zipWithIndex.toVdomArray { case (s, sIdx) =>
                    val nextStart = spec.sections.lift(sIdx + 1).map(_.startIdx).getOrElse(count)
                    val leftPct   = s.startIdx.toDouble / count * 100
                    val widthPct  = (nextStart - s.startIdx).toDouble / count * 100
                    <.span(
                      ^.key       := s"section-${s.startIdx}",
                      ^.className := "linked-list__section-label",
                      ^.style := js.Dynamic.literal(
                        left = s"$leftPct%",
                        width = s"$widthPct%"
                      ),
                      s.name
                    )
                  }
                )

            // Segmented progress bar — one clickable button per step. Active
            // step is the current one; past steps render filled; future steps
            // hollow. Section-start segments carry a left border to act as a
            // section divider, so authors can drop in sections without
            // separate divider elements.
            def progressBar: VdomNode =
              <.div(
                ^.className := "linked-list__progress-bar",
                (0 until count).toVdomArray { i =>
                  val isActive         = i == idx
                  val isPast           = i < idx
                  val isAtSectionStart = i > 0 && spec.sections.exists(_.startIdx == i)
                  val modifiers = List(
                    Option.when(isActive)("linked-list__progress-segment--active"),
                    Option.when(isPast)("linked-list__progress-segment--past"),
                    Option.when(isAtSectionStart)("linked-list__progress-segment--section-start")
                  ).flatten
                  val cls = ("linked-list__progress-segment" :: modifiers).mkString(" ")
                  <.button(
                    ^.key        := s"seg-$i",
                    ^.tpe        := "button",
                    ^.className  := cls,
                    ^.aria.label := s"Jump to step ${i + 1}",
                    ^.onClick --> jumpTo(i)
                  )
                }
              )

            // Speed toggle — 0.5×, 1×, 2× radio-button-style. Only meaningful
            // for multi-step widgets; the speed state still persists between
            // step changes (resets only on payload change via useState init).
            def speedButton(s: Double, label: String): VdomNode =
              val active = speedS.value == s
              <.button(
                ^.key := s"speed-$s",
                ^.tpe := "button",
                ^.className := (
                  if active then "linked-list__speed-button linked-list__speed-button--active"
                  else "linked-list__speed-button"
                ),
                ^.aria.label := s"Set playback speed to $label",
                ^.onClick --> speedS.setState(s),
                label
              )

            val progressBlock: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "linked-list__progress",
                  sectionLabels,
                  progressBar,
                  <.span(
                    ^.className := "linked-list__progress-text",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "linked-list__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> previous,
                    ^.disabled   := atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "linked-list__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("linked-list__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if playingS.value then "Pause" else "Play"),
                    ^.className  := "linked-list__button linked-list__button--primary",
                    if playingS.value then
                      LucideIcons.Pause(LucideIcons.withClass("linked-list__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("linked-list__button-icon")),
                    if playingS.value then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> next,
                    ^.disabled   := atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "linked-list__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("linked-list__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> reset,
                    ^.disabled   := atStart && !playingS.value,
                    ^.aria.label := "Reset",
                    ^.className  := "linked-list__button linked-list__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("linked-list__button-icon"))
                  ),
                  <.div(
                    ^.className := "linked-list__speed-toggle",
                    <.span(^.className := "linked-list__speed-label", "Speed"),
                    speedButton(0.5, "½×"),
                    speedButton(1.0, "1×"),
                    speedButton(2.0, "2×")
                  )
                )

            <.div(
              ^.className := "linked-list not-prose",
              spec.title
                .map(t => <.p(^.className := "linked-list__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              <.div(
                ^.className := "linked-list__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "linked-list__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              progressBlock,
              controls
            )
      }
