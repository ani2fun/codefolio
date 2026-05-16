package codefolio.client.components.cortex.widgets

import codefolio.client.components.icons.LucideIcons
import codefolio.client.d3.D3
import japgolly.scalajs.react.*
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
  final case class Marker(name: String, nodeId: String, color: Option[String])

  final case class Step(
      nodes: Option[List[Node]],
      links: List[Link],
      markers: List[Marker],
      head: Option[String],
      msg: String
  )

  final case class Spec(
      direction: String,
      nodes: List[Node],
      head: Option[String],
      cycleTarget: Option[String],
      title: Option[String],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants. Wider gap than ArrayTraversal so arrows have room to
  // render their arrowhead between nodes without crowding the cell rects.
  // ---------------------------------------------------------------------------

  private val NodeSize     = 56.0
  private val NodeGap      = 36.0
  private val MarkerLaneH  = 44.0
  private val BackLaneH    = 36.0 // doubly's prev-row + cycle-edge clearance
  private val PaddingX     = 24.0
  private val PaddingY     = 12.0
  private val StepDelayMs  = 1200
  private val ArrowheadW   = 8.0
  private val ArrowheadH   = 6.0
  private val DefaultColor = "#3b82f6"
  private val SvgNs        = "http://www.w3.org/2000/svg"

  private val PaletteByIndex = Vector(
    "#3b82f6", // first marker (e.g. curr / head)
    "#10b981", // second marker (e.g. prev / slow)
    "#f59e0b", // third (e.g. next / fast)
    "#a855f7",
    "#ef4444"
  )

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
        Node(
          id = n.id.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
          value = n.value.asInstanceOf[js.UndefOr[js.Any]].toOption.fold("")(v =>
            js.Dynamic.global.String(v).asInstanceOf[String]
          ),
          style = n.style.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
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
        Marker(
          name = m.name.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
          nodeId = m.nodeId.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
          color = m.color.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
        )
      }
      .filter(m => m.name.nonEmpty && m.nodeId.nonEmpty)

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
      Spec(dir, nodes, head, cycleTarget, title, steps)
    } match
      case Success(spec) if spec.nodes.isEmpty => Left("payload.nodes must be non-empty")
      case Success(spec)                       => Right(spec)
      case Failure(t)                          => Left(Option(t.getMessage).getOrElse("invalid payload JSON"))

  // ---------------------------------------------------------------------------
  // Layout helpers
  // ---------------------------------------------------------------------------

  private def clamp(i: Int, count: Int): Int =
    if count <= 0 then 0 else math.max(0, math.min(count - 1, i))

  private def nodeX(index: Int): Double =
    PaddingX + index * (NodeSize + NodeGap)

  private def nodeCenterX(index: Int): Double =
    nodeX(index) + NodeSize / 2

  private def viewBoxWidth(nodeCount: Int): Double =
    PaddingX * 2 + nodeCount * NodeSize + (nodeCount - 1).max(0) * NodeGap

  private def viewBoxHeight(hasBack: Boolean): Double =
    PaddingY * 2 + MarkerLaneH + NodeSize + (if hasBack then BackLaneH else 0.0)

  private def nodesFor(spec: Spec, step: Step): List[Node] =
    step.nodes.getOrElse(spec.nodes)

  private def hasBackLane(spec: Spec): Boolean =
    spec.direction == "double" || spec.cycleTarget.isDefined ||
      spec.steps.exists(_.links.exists(_.kind.contains("prev")))

  private def colorFor(marker: Marker, fallbackIdx: Int): String =
    marker.color
      .orElse(
        Option.when(fallbackIdx >= 0 && fallbackIdx < PaletteByIndex.length)(PaletteByIndex(fallbackIdx))
      )
      .getOrElse(DefaultColor)

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
      val width   = viewBoxWidth(maxNodes)
      val height  = viewBoxHeight(hasBackLane(spec))
      val titleAt = spec.title.getOrElse("Linked list")
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

  // `animate` parameter retained for future use if D3 transitions become
  // reliable on entering elements in this stack; currently all updates are
  // applied instantly (see comments in the body for the rationale).
  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val _            = animate // suppress unused-param warning
    val svg          = D3.select(svgEl)
    val nodes        = nodesFor(spec, step)
    val nodeCount    = nodes.size
    val hasBack      = hasBackLane(spec)
    val width        = viewBoxWidth(nodeCount)
    val height       = viewBoxHeight(hasBack)
    val nodeY        = PaddingY + MarkerLaneH
    val nodeCenterYV = nodeY + NodeSize / 2
    val markerLaneY  = PaddingY + MarkerLaneH - 8.0
    val backLaneY    = nodeY + NodeSize + 10.0

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
        .attr("y", nodeY + NodeSize / 2 + 6)
        .text("(empty list)")
      val _ = svg
        .selectAll("text.linked-list__empty-placeholder")
        .attr("x", ensureCanvasW / 2)
        .attr("y", nodeY + NodeSize / 2 + 6)
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

    val nodeEnter = nodeSel
      .enter()
      .append("g")
      .attr(
        "class",
        (
            (d, _) =>
              val style = d.asInstanceOf[js.Dynamic].style.asInstanceOf[String]
              if style.nonEmpty then s"linked-list__node linked-list__node--$style"
              else "linked-list__node"
        ): js.Function2[js.Any, Int, js.Any]
      )
      .attr(
        "transform",
        (
            (d, _) =>
              val i = d.asInstanceOf[js.Dynamic].index.asInstanceOf[Int]
              s"translate(${nodeX(i)}, $nodeY)"
        ): js.Function2[js.Any, Int, js.Any]
      )
      // Enter at full opacity. A fade-in via transition() was tried but the
      // transition reliably fails to fire on entering elements in this React
      // + Scala.js + D3 stack (verified with both default and named
      // transitions, and even an independently-imported d3 instance — the
      // element's `__transition__` slot never gets ticked). Showing entering
      // nodes instantly is the pragmatic choice.
      .attr("opacity", 1)

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

    // No fade-in transition — see comment on the enter-time opacity attr
    // above. Entering nodes render at full opacity from the first frame.

    // Exit: instant remove. (A fade-out + delayed remove would require either
    // extending the D3 facade with Transition.remove() or chaining a Promise
    // off transition.end(); for now nodes vanish on step change. The
    // pedagogical loss is minimal — the absence of the node tells the story.)
    val _ = nodeSel.exit().remove()

    // Update: re-bind class (style may change), text, and set transform to
    // the new index position (no animation — see enter-opacity comment).
    val nodeAll = svg.selectAll("g.linked-list__node")
    val nodeTransform: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val i = d.asInstanceOf[js.Dynamic].index.asInstanceOf[Int]
        s"translate(${nodeX(i)}, $nodeY)"
    val nodeClassFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val style = d.asInstanceOf[js.Dynamic].style.asInstanceOf[String]
        if style.nonEmpty then s"linked-list__node linked-list__node--$style"
        else "linked-list__node"
    val _ = nodeAll.attr("class", nodeClassFn)
    val _ = nodeAll
      .select("text.linked-list__node-label")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    // Set transform directly (no transition). D3 transitions don't fire
    // reliably in this React + Scala.js + D3 stack — see the enter-opacity
    // comment above for the diagnosis.
    val _ = nodeAll.attr("transform", nodeTransform)

    // ── Arrows (links) ───────────────────────────────────────────────────────
    val expandedLinks = expandLinksForDirection(spec.direction, step.links)
      .filter(l => idxOf.contains(l.from) && idxOf.contains(l.to))

    val linkData: js.Array[js.Any] = expandedLinks.map { l =>
      val kind = l.kind.getOrElse("next")
      js.Dynamic
        .literal(from = l.from, to = l.to, kind = kind)
        .asInstanceOf[js.Any]
    }.toJSArray
    val linkKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val dyn = d.asInstanceOf[js.Dynamic]
        s"${dyn.from.asInstanceOf[String]}>${dyn.to.asInstanceOf[String]}>${dyn.kind.asInstanceOf[String]}"

    def linkPath(d: js.Any): String =
      val dyn  = d.asInstanceOf[js.Dynamic]
      val from = dyn.from.asInstanceOf[String]
      val to   = dyn.to.asInstanceOf[String]
      val kind = dyn.kind.asInstanceOf[String]
      val si   = idxOf.getOrElse(from, -1)
      val ti   = idxOf.getOrElse(to, -1)
      if si < 0 || ti < 0 then ""
      else
        val sx0 = nodeX(si) + (if kind == "prev" then NodeSize / 2 - 8 else NodeSize)
        val sx1 = nodeCenterX(si)
        val tx0 = nodeX(ti) + (if kind == "prev" then NodeSize / 2 + 8 else 0)
        val tx1 = nodeCenterX(ti)
        if kind == "prev" then
          // Below the row, dashed; curve out and back to avoid overlapping the next-row arrow.
          val cy = backLaneY + BackLaneH * 0.55
          s"M $sx1 $nodeCenterYV C $sx1 $cy, $tx1 $cy, $tx1 $nodeCenterYV"
        else
          // Same-row straight arrow. End slightly before the target so the
          // arrowhead doesn't overlap the cell border.
          val arrowGap = 6.0
          val (start, end) =
            if ti > si then (sx0, nodeX(ti) - arrowGap)
            else if ti < si then (nodeX(si), nodeX(ti) + NodeSize + arrowGap)
            else (sx0, tx1) // self-loop fallback (unusual)
          s"M $start $nodeCenterYV L $end $nodeCenterYV"

    def arrowheadPath(d: js.Any): String =
      val dyn  = d.asInstanceOf[js.Dynamic]
      val from = dyn.from.asInstanceOf[String]
      val to   = dyn.to.asInstanceOf[String]
      val kind = dyn.kind.asInstanceOf[String]
      val si   = idxOf.getOrElse(from, -1)
      val ti   = idxOf.getOrElse(to, -1)
      if si < 0 || ti < 0 then ""
      else if kind == "prev" then
        // Arrowhead at the source-side (prev points back).
        val tipX = nodeCenterX(ti)
        val tipY = nodeCenterYV
        s"M ${tipX - ArrowheadW} ${tipY + ArrowheadH} L ${tipX + ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"
      else
        val arrowGap = 6.0
        val tipX     = if ti > si then nodeX(ti) - arrowGap else nodeX(ti) + NodeSize + arrowGap
        val tipY     = nodeCenterYV
        if ti > si then
          s"M ${tipX - ArrowheadW} ${tipY - ArrowheadH} L ${tipX - ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"
        else
          s"M ${tipX + ArrowheadW} ${tipY - ArrowheadH} L ${tipX + ArrowheadW} ${tipY + ArrowheadH} L $tipX $tipY Z"

    val linkSel = svg.selectAll("g.linked-list__arrow-group").data(linkData, linkKeyFn)

    val linkEnter = linkSel
      .enter()
      .append("g")
      .attr(
        "class",
        (
            (d, _) =>
              val kind = d.asInstanceOf[js.Dynamic].kind.asInstanceOf[String]
              val base = "linked-list__arrow-group"
              kind match
                case "prev"   => s"$base linked-list__arrow-group--back"
                case "broken" => s"$base linked-list__arrow-group--broken"
                case _        => base
        ): js.Function2[js.Any, Int, js.Any]
      )
      // Render at full opacity from frame 1 — see node-enter comment above
      // for why fade-in transitions are skipped.
      .attr("opacity", 1)
    val _ = linkEnter
      .append("path")
      .attr("class", "linked-list__arrow")
      .attr("fill", "none")
      .attr("d", ((d, _) => linkPath(d)): js.Function2[js.Any, Int, js.Any])
    val _ = linkEnter
      .append("path")
      .attr("class", "linked-list__arrowhead")
      .attr("d", ((d, _) => arrowheadPath(d)): js.Function2[js.Any, Int, js.Any])
    // Arrows enter at full opacity directly (no fade transition).

    // Exit: instant remove (same rationale as node-exit above).
    val _ = linkSel.exit().remove()

    val linkAll = svg.selectAll("g.linked-list__arrow-group")
    val _ = linkAll
      .select("path.linked-list__arrow")
      .attr("d", ((d, _) => linkPath(d)): js.Function2[js.Any, Int, js.Any])
    val _ = linkAll
      .select("path.linked-list__arrowhead")
      .attr("d", ((d, _) => arrowheadPath(d)): js.Function2[js.Any, Int, js.Any])

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

    val _ = cycleSel
      .enter()
      .append("path")
      .attr("class", "linked-list__cycle-edge")
      .attr("fill", "none")
    val _        = cycleSel.exit().remove()
    val cycleAll = svg.selectAll("path.linked-list__cycle-edge")
    val cyclePath: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val si  = idxOf.getOrElse(dyn.from.asInstanceOf[String], -1)
      val ti  = idxOf.getOrElse(dyn.to.asInstanceOf[String], -1)
      if si < 0 || ti < 0 then ""
      else
        val sx = nodeCenterX(si)
        val tx = nodeCenterX(ti)
        val sy = nodeCenterYV
        val cy = backLaneY + BackLaneH * 0.65
        s"M $sx $sy C $sx $cy, $tx $cy, $tx $sy"
    // Set cycle-edge path directly (no transition).
    val _ = cycleAll.attr("d", cyclePath)

    // ── Markers ──────────────────────────────────────────────────────────────
    // Multiple markers may attach to the same node (e.g. `head` + `current`
    // both at the head on traversal start). To avoid the labels overlapping,
    // we compute a per-node `rank`: the first marker at a node renders the
    // arrowhead triangle + its label one slot above the row; subsequent
    // markers at the same node stack their labels upward, no extra triangle.
    val activeMarkers = step.markers.filter(m => idxOf.contains(m.nodeId))
    val rankedMarkers = activeMarkers.zipWithIndex.map { case (m, idx) =>
      val rank = activeMarkers.take(idx).count(_.nodeId == m.nodeId)
      (m, rank)
    }
    val markerData: js.Array[js.Any] = rankedMarkers.zipWithIndex
      .map { case ((m, rank), fallbackIdx) =>
        js.Dynamic
          .literal(name = m.name, nodeId = m.nodeId, color = colorFor(m, fallbackIdx), rank = rank)
          .asInstanceOf[js.Any]
      }
      .toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name

    val centerOfMarker: (js.Any, Int) => Double =
      (d, _) =>
        val nodeId = d.asInstanceOf[js.Dynamic].nodeId.asInstanceOf[String]
        nodeCenterX(idxOf.getOrElse(nodeId, 0))

    val markerLaneRowH = 14.0
    val labelY: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val rank = d.asInstanceOf[js.Dynamic].rank.asInstanceOf[Int]
      (markerLaneY - 6 - rank * markerLaneRowH).toString
    val trianglePathFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val rank = d.asInstanceOf[js.Dynamic].rank.asInstanceOf[Int]
      if rank == 0 then s"M -5 ${markerLaneY - 2} L 5 ${markerLaneY - 2} L 0 ${markerLaneY + 6} Z"
      else ""

    val markerSel = svg.selectAll("g.linked-list__marker").data(markerData, markerKeyFn)

    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "linked-list__marker")
      .attr(
        "transform",
        ((d, i) => s"translate(${centerOfMarker(d, i)}, 0)"): js.Function2[js.Any, Int, js.Any]
      )
      // Markers enter at full opacity (see node-enter comment for fade-in
      // skip rationale).
      .attr("opacity", 1)
    val _ = markerEnter
      .append("path")
      .attr("d", trianglePathFn)
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
    val _ = markerEnter
      .append("text")
      .attr("class", "linked-list__marker-label")
      .attr("x", 0)
      .attr("y", labelY)
      .attr("text-anchor", "middle")
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    val _ = markerSel.exit().remove()

    val markerAll = svg.selectAll("g.linked-list__marker")
    val markerTransform: js.Function2[js.Any, Int, js.Any] =
      (d, i) => s"translate(${centerOfMarker(d, i)}, 0)"
    // Set marker transform directly (no transition — see comment above).
    val _ = markerAll.attr("transform", markerTransform)
    val _ = markerAll
      .select("path")
      .attr("d", trianglePathFn)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerAll
      .select("text.linked-list__marker-label")
      .attr("y", labelY)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .useState(0)                      // step index
      .useState(false)                  // playing
      .useRefBy(_ => Option.empty[Int]) // play timeout id
      .useRefToVdom[dom.html.Element]   // host div ref — D3 manages the <svg> inside
      .useRefBy(_ => false)             // hasRendered (mutable; avoids re-render cycle)
      // ── play-loop timer ─────────────────────────────────────────────────────
      .useEffectWithDepsBy((_, specM, indexS, playingS, _, _, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), indexS.value, playingS.value)
      ) { (_, _, indexS, playingS, timeoutRef, _, _) => (count, index, playing) =>
        Callback {
          timeoutRef.value.foreach(dom.window.clearTimeout)
          timeoutRef.value = None
          if playing then
            if index >= count - 1 then playingS.setState(false).runNow()
            else
              val id =
                dom.window.setTimeout(() => indexS.setState(index + 1).runNow(), StepDelayMs.toDouble)
              timeoutRef.value = Some(id)
        }
      }
      // ── D3 render on every step / spec change ───────────────────────────────
      .useEffectWithDepsBy((_, specM, indexS, _, _, _, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), indexS.value)
      ) { (_, specM, _, _, _, hostRef, hasRenderedRef) => (count, index) =>
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
      .render { (_, specM, indexS, playingS, _, hostRef, _) =>
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

            // Controls + step counter are only useful when there's more than
            // one step. For a single-step (static) widget, hide them entirely
            // — the diagram reads as a static figure with no implication of
            // a sequence to step through.
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
                  <.span(
                    ^.className := "linked-list__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
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
              controls
            )
      }
