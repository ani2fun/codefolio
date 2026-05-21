package codefolio.client.components.cortex.widgets

import codefolio.client.components.cortex.widgets.PayloadDecoder.*
import codefolio.client.components.icons.LucideIcons
import codefolio.client.d3.D3
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/**
 * Graph explorer — the fifth widget in the D3 catalog. One widget covers source Phase 9 (Graph, 22 source
 * diagrams) plus four advanced-graph orphan chapters plus several real-systems chapters that visualise DAGs
 * (Git Merkle, Aho-Corasick goto-graph, suffix automaton).
 *
 * Three layout modes share one renderer:
 *
 *   - `layout: "force"` — generic graphs (DFS, BFS, MST, Dijkstra, Bellman-Ford, cycle detection, SCC,
 *     bridges, 2-SAT). Authors pin `(x, y)` per node for pedagogical stability — no live force simulation.
 *   - `layout: "grid"` — grid-shaped graphs (grid traversal, BFS shortest-path on grid). Nodes carry `(row,
 *     col)`; the renderer computes `(x, y)` from a fixed cell size.
 *   - `layout: "dag"` — layered DAGs (topological sort, suffix automaton, Aho-Corasick, Git Merkle). Nodes
 *     carry `layer: Int`; renderer stacks vertically by layer (TB) or horizontally (LR), auto-x-spreads
 *     within layer.
 *
 * Per-step `nodeStates` overlays a closed catalog of statuses (unseen / frontier / active / settled / white /
 * gray / black / colourA / colourB / componentN). Per-step `edgeStates` overlays a closed catalog of statuses
 * (default / tree / back / cross / forward / relaxed / tight / on-path / considered / rejected / failure).
 * Both render as CSS modifiers (`--<state>`) so palette changes happen in one place.
 *
 * Optional `directed: true` swaps line edges for arrows (renders a small `<defs><marker>` arrowhead per
 * edge-state colour). Optional `weighted: true` renders a weight label at each edge midpoint.
 *
 * Optional `panels` is an ordered list of sidecar tables that update per step via `step.tables`:
 *
 *   - `kind: "array"` — keyed row of cells with labels above (dist[], prev[], in-degree counter).
 *   - `kind: "list"` — horizontal stack of pill cells (queue contents, visit order, topological order).
 *
 * Per-step `markers` are accepted but unused in Arc 1 — the schema reserves the field so a chapter author who
 * wants a `current` pointer overlay later doesn't have to migrate. Marker names route through [[MarkerCanon]]
 * for colour resolution; non-canonical names show a warning badge.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":    "Dijkstra from node 0",
 *   "layout":   "force",                                                  // "force" | "grid" | "dag"
 *   "directed": true,                                                     // default false
 *   "weighted": true,                                                     // default false
 *   "orientation": "TB",                                                  // dag only: "TB" | "LR"
 *   "nodes": [
 *     {"id": "0", "label": "0", "x": 60, "y": 120},                       // force
 *     {"id": "0,0", "label": "S", "row": 0, "col": 0},                    // grid
 *     {"id": "A", "label": "A", "layer": 0}                               // dag
 *   ],
 *   "edges": [
 *     {"from": "0", "to": "1", "weight": 4}                               // weight optional
 *   ],
 *   "panels": [
 *     {"name": "dist",  "kind": "array", "labels": ["0","1","2","3"]},
 *     {"name": "queue", "kind": "list"}
 *   ],
 *   "steps": [{
 *     "nodeStates": { "0": "settled", "1": "frontier" },
 *     "edgeStates": { "0-1": "tree" },
 *     "tables":     { "dist": ["0", "4", "inf", "inf"], "queue": ["1"] },
 *     "msg":        "Settle 0; enqueue 1"
 *   }]
 * }
 * }}}
 */
object GraphExplorer:

  // ---------------------------------------------------------------------------
  // Schema — every required field validated inside `PayloadDecoder.run`; thrown
  // exceptions collapse to `Left(message)` and the renderer shows an inline
  // error placeholder instead of crashing.
  // ---------------------------------------------------------------------------

  final case class Node(
      id: String,
      label: String,
      x: Option[Double],
      y: Option[Double],
      row: Option[Int],
      col: Option[Int],
      layer: Option[Int]
  )

  final case class Edge(from: String, to: String, weight: Option[Double])

  final case class Panel(name: String, kind: String, labels: List[String])

  final case class Marker(name: String, nodeId: String, canonical: Boolean)

  final case class Step(
      nodeStates: Map[String, String],
      edgeStates: Map[String, String],
      tables: Map[String, List[String]],
      markers: List[Marker],
      msg: String
  )

  final case class Spec(
      title: Option[String],
      layout: String,
      directed: Boolean,
      weighted: Boolean,
      orientation: String,
      nodes: List[Node],
      edges: List[Edge],
      panels: List[Panel],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout modes + state catalogs. The catalogs are closed; an unknown value
  // falls through to the default modifier ("unseen" / "default") so authors see
  // a muted node/edge rather than a crash, and a console warning flags the typo.
  // ---------------------------------------------------------------------------

  private val LayoutForce = "force"
  private val LayoutGrid  = "grid"
  private val LayoutDag   = "dag"

  private val OrientTB = "TB"
  private val OrientLR = "LR"

  // Closed catalog of node states. Authors who supply a name not in this set
  // get a console warning + the "unseen" CSS modifier (default fill).
  val NodeStates: Set[String] = Set(
    "unseen",
    "frontier",
    "active",
    "settled",
    "white",
    "gray",
    "black",
    "colourA",
    "colourB",
    // Componentized colouring — one modifier per component index up to 8.
    "component0",
    "component1",
    "component2",
    "component3",
    "component4",
    "component5",
    "component6",
    "component7"
  )

  // Closed catalog of edge states. Mirrors the spec §4 enumeration.
  val EdgeStates: Set[String] = Set(
    "default",
    "tree",
    "back",
    "cross",
    "forward",
    "relaxed",
    "tight",
    "on-path",
    "considered",
    "rejected",
    "failure"
  )

  private val PanelArray = "array"
  private val PanelList  = "list"

  // ---------------------------------------------------------------------------
  // Layout constants — sized to fit inside a prose column.
  // ---------------------------------------------------------------------------

  private val NodeRadius       = 18.0
  private val GridCellSize     = 56.0
  private val GridCellGap      = 4.0
  private val DagLayerGap      = 80.0
  private val DagIntraLayerGap = 80.0
  private val PaddingX         = 24.0
  private val PaddingY         = 24.0
  private val WeightOffset     = 12.0

  private val ArrowHeadInset =
    4.0 // edge endpoint stops short of the node circle so the head lands on the rim
  private val PanelGap             = 32.0 // gap between canvas and panel area
  private val PanelLabelGap        = 6.0
  private val PanelTitleH          = 18.0
  private val PanelCellW           = 48.0
  private val PanelCellH           = 30.0
  private val PanelCellGap         = 4.0
  private val PanelRowGap          = 24.0
  private val StepDelayMs          = 1400
  private val TransitionDurationMs = 450.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // ---------------------------------------------------------------------------
  // Parsing
  // ---------------------------------------------------------------------------

  private def parseNode(d: js.Dynamic): Node =
    Node(
      id = d.string("id"),
      label = d.optString("label").getOrElse(d.string("id")),
      x = d.optDouble("x"),
      y = d.optDouble("y"),
      row = d.optInt("row"),
      col = d.optInt("col"),
      layer = d.optInt("layer")
    )

  private def parseEdge(d: js.Dynamic): Edge =
    Edge(
      from = d.string("from"),
      to = d.string("to"),
      weight = d.optDouble("weight")
    )

  private def parsePanel(d: js.Dynamic): Panel =
    val kindRaw = d.optString("kind").getOrElse(PanelList)
    val kind    = if kindRaw == PanelArray then PanelArray else PanelList
    Panel(
      name = d.string("name"),
      kind = kind,
      labels = d.stringList("labels").getOrElse(Nil)
    )

  private def parseMarker(d: js.Dynamic): Marker =
    val name      = d.string("name")
    val nodeId    = d.string("nodeId")
    val rawColor  = d.optString("color")
    val canonical = MarkerCanon.isCanonical(name)
    if rawColor.isDefined then MarkerCanon.warnAuthorColor("graph-explorer", name)
    if name.nonEmpty && !canonical then MarkerCanon.warnUnknown("graph-explorer", name)
    Marker(name, nodeId, canonical)

  // Read a flat `Map[String, String]` from a `{key: "value", ...}` JS object.
  // Returns empty map if the field is absent. Numeric values are coerced via
  // `String(v)` so authors who write `"dist": [0, 4, "inf"]` get strings.
  private def parseStringMap(parent: js.Dynamic, field: String): Map[String, String] =
    parent.optObj(field) match
      case None => Map.empty
      case Some(obj) =>
        val out = scala.collection.mutable.Map.empty[String, String]
        val keys =
          js.Object.keys(obj.asInstanceOf[js.Object]).asInstanceOf[js.Array[String]]
        var i = 0
        while i < keys.length do
          val k = keys(i)
          val v = obj.selectDynamic(k)
          out(k) = js.Dynamic.global.String(v).asInstanceOf[String]
          i += 1
        out.toMap

  // Read a `Map[String, List[String]]` from a `{name: [...], ...}` JS object.
  private def parseTables(parent: js.Dynamic): Map[String, List[String]] =
    parent.optObj("tables") match
      case None => Map.empty
      case Some(obj) =>
        val out = scala.collection.mutable.Map.empty[String, List[String]]
        val keys =
          js.Object.keys(obj.asInstanceOf[js.Object]).asInstanceOf[js.Array[String]]
        var i = 0
        while i < keys.length do
          val k = keys(i)
          val v = obj.selectDynamic(k).asInstanceOf[js.UndefOr[js.Array[js.Any]]]
          val list = v.toOption
            .map(_.toList.map(x => js.Dynamic.global.String(x).asInstanceOf[String]))
            .getOrElse(Nil)
          out(k) = list
          i += 1
        out.toMap

  private def parseStep(s: js.Dynamic): Step =
    val nodeStates = parseStringMap(s, "nodeStates")
    val edgeStates = parseStringMap(s, "edgeStates")
    val tables     = parseTables(s)
    val markers    = s.dynList("markers").map(parseMarker).filter(m => m.name.nonEmpty && m.nodeId.nonEmpty)
    // Warn on unknown state values so authors see typos in the console.
    nodeStates.values.foreach { v =>
      if v.nonEmpty && !NodeStates.contains(v) then
        dom.console.warn(
          s"graph-explorer: nodeState '$v' is not in the canonical vocabulary. Rendered as default. " +
            s"Canonical names: ${NodeStates.toList.sorted.mkString(", ")}."
        )
    }
    edgeStates.values.foreach { v =>
      if v.nonEmpty && !EdgeStates.contains(v) then
        dom.console.warn(
          s"graph-explorer: edgeState '$v' is not in the canonical vocabulary. Rendered as default. " +
            s"Canonical names: ${EdgeStates.toList.sorted.mkString(", ")}."
        )
    }
    Step(nodeStates, edgeStates, tables, markers, s.string("msg"))

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawLayout = d.optString("layout").getOrElse(LayoutForce)
      val layout =
        if rawLayout == LayoutGrid then LayoutGrid
        else if rawLayout == LayoutDag then LayoutDag
        else LayoutForce
      val orientation = d.optString("orientation").getOrElse(OrientTB) match
        case OrientLR => OrientLR
        case _        => OrientTB
      val nodes = d.dynList("nodes").map(parseNode).filter(_.id.nonEmpty)
      if nodes.isEmpty then throw PayloadDecoder.invalid("nodes must be non-empty")
      val edges  = d.dynList("edges").map(parseEdge).filter(e => e.from.nonEmpty && e.to.nonEmpty)
      val panels = d.dynList("panels").map(parsePanel).filter(_.name.nonEmpty)
      val steps  = d.dynList("steps").map(parseStep)
      if steps.isEmpty then throw PayloadDecoder.invalid("steps must be non-empty")
      Spec(
        title = d.optString("title"),
        layout = layout,
        directed = d.bool("directed", default = false),
        weighted = d.bool("weighted", default = false),
        orientation = orientation,
        nodes = nodes,
        edges = edges,
        panels = panels,
        steps = steps
      )
    }

  // ---------------------------------------------------------------------------
  // Layout — `layoutNode` resolves each node's (x, y) per layout mode.
  // ---------------------------------------------------------------------------

  // For force layout: read straight through from `node.x/y`, default to (0,0)
  // for nodes that forgot to pin coords (the warning surfaces visually).
  // For grid layout: derive from `row` / `col`.
  // For dag layout: derive from `layer` + intra-layer rank (computed once per spec).
  private def layoutPositions(spec: Spec): Map[String, (Double, Double)] =
    spec.layout match
      case `LayoutGrid` =>
        spec.nodes.map { n =>
          val r = n.row.getOrElse(0)
          val c = n.col.getOrElse(0)
          val x = PaddingX + c * (GridCellSize + GridCellGap) + GridCellSize / 2
          val y = PaddingY + r * (GridCellSize + GridCellGap) + GridCellSize / 2
          n.id -> (x, y)
        }.toMap
      case `LayoutDag` =>
        // Group nodes by layer, preserve author order within layer for x-rank.
        val grouped =
          spec.nodes.zipWithIndex.groupBy { case (n, _) => n.layer.getOrElse(0) }
        val maxLayer = grouped.keys.maxOption.getOrElse(0)
        val maxInLayer =
          grouped.values.map(_.size).maxOption.getOrElse(1)
        val rowWidth = math.max(1, maxInLayer) * DagIntraLayerGap
        val layerTo  = scala.collection.mutable.Map.empty[String, (Double, Double)]
        grouped.toSeq.sortBy(_._1).foreach { case (layer, members) =>
          val sorted = members.sortBy(_._2).map(_._1)
          val n      = sorted.size
          val width  = (n - 1).max(0) * DagIntraLayerGap
          val xStart = PaddingX + (rowWidth - width) / 2
          sorted.zipWithIndex.foreach { case (node, idx) =>
            val xy = spec.orientation match
              case `OrientLR` =>
                (PaddingX + layer * DagLayerGap + NodeRadius, PaddingY + xStart + idx * DagIntraLayerGap)
              case _ =>
                (xStart + idx * DagIntraLayerGap, PaddingY + layer * DagLayerGap + NodeRadius)
            layerTo(node.id) = xy
          }
        }
        layerTo.toMap
      case _ =>
        spec.nodes.map(n => n.id -> (n.x.getOrElse(0.0), n.y.getOrElse(0.0))).toMap

  private def viewBoxWidth(spec: Spec, positions: Map[String, (Double, Double)]): Double =
    val canvasW = spec.layout match
      case `LayoutGrid` =>
        val maxCol = spec.nodes.flatMap(_.col).maxOption.getOrElse(0)
        PaddingX * 2 + (maxCol + 1) * GridCellSize + maxCol * GridCellGap
      case `LayoutDag` if spec.orientation == OrientLR =>
        val maxLayer = spec.nodes.flatMap(_.layer).maxOption.getOrElse(0)
        PaddingX * 2 + (maxLayer + 1) * DagLayerGap
      case `LayoutDag` =>
        val byLayer  = spec.nodes.groupBy(_.layer.getOrElse(0))
        val maxCount = byLayer.values.map(_.size).maxOption.getOrElse(1)
        PaddingX * 2 + math.max(1, maxCount) * DagIntraLayerGap + NodeRadius
      case _ =>
        val maxX = positions.values.map(_._1).maxOption.getOrElse(0.0)
        maxX + PaddingX + NodeRadius
    canvasW + panelAreaWidth(spec)

  private def viewBoxHeight(spec: Spec, positions: Map[String, (Double, Double)]): Double =
    val canvasH = spec.layout match
      case `LayoutGrid` =>
        val maxRow = spec.nodes.flatMap(_.row).maxOption.getOrElse(0)
        PaddingY * 2 + (maxRow + 1) * GridCellSize + maxRow * GridCellGap
      case `LayoutDag` if spec.orientation == OrientLR =>
        val byLayer  = spec.nodes.groupBy(_.layer.getOrElse(0))
        val maxCount = byLayer.values.map(_.size).maxOption.getOrElse(1)
        PaddingY * 2 + math.max(1, maxCount) * DagIntraLayerGap + NodeRadius
      case `LayoutDag` =>
        val maxLayer = spec.nodes.flatMap(_.layer).maxOption.getOrElse(0)
        PaddingY * 2 + (maxLayer + 1) * DagLayerGap
      case _ =>
        val maxY = positions.values.map(_._2).maxOption.getOrElse(0.0)
        maxY + PaddingY + NodeRadius
    math.max(canvasH, panelAreaHeight(spec))

  private def panelAreaWidth(spec: Spec): Double =
    if spec.panels.isEmpty then 0.0
    else
      val cellsRowW = spec.panels.map(panelWidth).max
      PanelGap + cellsRowW + PaddingX

  private def panelAreaHeight(spec: Spec): Double =
    if spec.panels.isEmpty then PaddingY * 2
    else
      val total =
        spec.panels.map(p => PanelTitleH + PanelCellH + PanelRowGap).sum + PaddingY * 2
      total

  private def panelWidth(panel: Panel): Double =
    panel.kind match
      case `PanelArray` =>
        val n = math.max(panel.labels.size, 1)
        n * PanelCellW + (n - 1).max(0) * PanelCellGap
      case _ =>
        // List panels grow at runtime; reserve room for ~6 cells worth.
        6 * PanelCellW + 5 * PanelCellGap

  // The canvas (graph) extends to the right edge of the graph proper; panels
  // start to its right.
  private def canvasRight(spec: Spec, positions: Map[String, (Double, Double)]): Double =
    spec.layout match
      case `LayoutGrid` =>
        val maxCol = spec.nodes.flatMap(_.col).maxOption.getOrElse(0)
        PaddingX + (maxCol + 1) * GridCellSize + maxCol * GridCellGap
      case _ =>
        val maxX = positions.values.map(_._1).maxOption.getOrElse(0.0)
        maxX + NodeRadius

  // ---------------------------------------------------------------------------
  // SVG bootstrap — create the <svg> + a fixed <defs> block holding the
  // arrowhead `<marker>` per edge state. The marker fill comes from the same
  // CSS variable the matching `--<state>` modifier uses, so the head colour
  // tracks the line colour automatically.
  // ---------------------------------------------------------------------------

  private val ArrowMarkerDefs: List[(String, String)] = List(
    "default"    -> "var(--ge-edge-default, #94a3b8)",
    "tree"       -> "var(--ge-edge-tree, #10b981)",
    "back"       -> "var(--ge-edge-back, #ef4444)",
    "cross"      -> "var(--ge-edge-cross, #f59e0b)",
    "forward"    -> "var(--ge-edge-forward, #8b5cf6)",
    "relaxed"    -> "var(--ge-edge-relaxed, #3b82f6)",
    "tight"      -> "var(--ge-edge-tight, #10b981)",
    "on-path"    -> "var(--ge-edge-on-path, #10b981)",
    "considered" -> "var(--ge-edge-considered, #f59e0b)",
    "rejected"   -> "var(--ge-edge-rejected, #ef4444)",
    "failure"    -> "var(--ge-edge-failure, #ef4444)"
  )

  private def ensureSvg(host: dom.html.Element, spec: Spec, w: Double, h: Double): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then
      val s = existing.asInstanceOf[dom.Element]
      s.setAttribute("viewBox", s"0 0 $w $h")
      s.setAttribute("width", w.toString)
      s.setAttribute("height", h.toString)
      s
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", s"graph-explorer__svg graph-explorer__svg--${spec.layout}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Graph explorer ${spec.layout}"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 $w $h")
      svg.setAttribute("width", w.toString)
      svg.setAttribute("height", h.toString)
      // Arrowhead defs — created once, shared across all edges. Each state has
      // its own marker so the head colour matches the line stroke.
      val defs = dom.document.createElementNS(SvgNs, "defs")
      ArrowMarkerDefs.foreach { case (state, fill) =>
        val marker = dom.document.createElementNS(SvgNs, "marker").asInstanceOf[dom.Element]
        marker.setAttribute("id", s"ge-arrow-$state")
        marker.setAttribute("viewBox", "0 0 10 10")
        marker.setAttribute("refX", "9")
        marker.setAttribute("refY", "5")
        marker.setAttribute("markerWidth", "5")
        marker.setAttribute("markerHeight", "5")
        marker.setAttribute("orient", "auto")
        marker.setAttribute("markerUnits", "strokeWidth")
        val path = dom.document.createElementNS(SvgNs, "path").asInstanceOf[dom.Element]
        path.setAttribute("d", "M 0 0 L 10 5 L 0 10 z")
        path.setAttribute("class", s"graph-explorer__arrowhead graph-explorer__arrowhead--$state")
        path.setAttribute("fill", fill)
        marker.appendChild(path)
        defs.appendChild(marker)
      }
      svg.appendChild(defs)
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Rendering
  // ---------------------------------------------------------------------------

  private def edgeId(e: Edge): String = s"${e.from}-${e.to}"

  // Compute the endpoint of an edge, inset so an arrowhead lands on the node
  // rim rather than the centre.
  private def edgeEndpoint(
      from: (Double, Double),
      to: (Double, Double),
      inset: Double
  ): (Double, Double) =
    val (x1, y1) = from
    val (x2, y2) = to
    val dx       = x2 - x1
    val dy       = y2 - y1
    val len      = math.sqrt(dx * dx + dy * dy)
    if len < 0.0001 then to
    else
      val ux = dx / len
      val uy = dy / len
      (x2 - ux * inset, y2 - uy * inset)

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val positions = layoutPositions(spec)
    val svg       = D3.select(svgEl)

    // ── Canvas group ──────────────────────────────────────────────────────────
    val canvasSel = svg.selectAll("g.graph-explorer__canvas").data(js.Array(1).asInstanceOf[js.Array[js.Any]])
    val _         = canvasSel.enter().append("g").attr("class", "graph-explorer__canvas")
    val canvas    = svg.select("g.graph-explorer__canvas")

    // ── Edges (drawn first so nodes sit on top) ──────────────────────────────
    val edgeData: js.Array[js.Any] = spec.edges.flatMap { e =>
      (positions.get(e.from), positions.get(e.to)) match
        case (Some(p1), Some(p2)) =>
          val state    = step.edgeStates.getOrElse(edgeId(e), "default")
          val (ex, ey) = if spec.directed then edgeEndpoint(p1, p2, NodeRadius + ArrowHeadInset) else p2
          Some(
            js.Dynamic
              .literal(
                id = edgeId(e),
                from = e.from,
                to = e.to,
                x1 = p1._1,
                y1 = p1._2,
                x2 = ex,
                y2 = ey,
                mx = (p1._1 + p2._1) / 2,
                my = (p1._2 + p2._2) / 2,
                state = state,
                weight = e.weight.fold("")(w => if w == w.toInt then w.toInt.toString else w.toString)
              )
              .asInstanceOf[js.Any]
          )
        case _ => None
    }.toJSArray
    val edgeKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val edgeClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val state = d.asInstanceOf[js.Dynamic].state.asInstanceOf[String]
      s"graph-explorer__edge graph-explorer__edge--$state"
    val edgePathD: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x1  = dyn.x1.asInstanceOf[Double]
      val y1  = dyn.y1.asInstanceOf[Double]
      val x2  = dyn.x2.asInstanceOf[Double]
      val y2  = dyn.y2.asInstanceOf[Double]
      s"M $x1 $y1 L $x2 $y2"
    val edgeMarkerEnd: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      if !spec.directed then ""
      else
        val state = d.asInstanceOf[js.Dynamic].state.asInstanceOf[String]
        s"url(#ge-arrow-$state)"

    val edgeSel = canvas.selectAll("path.graph-explorer__edge").data(edgeData, edgeKeyFn)
    val edgeEnter = edgeSel
      .enter()
      .append("path")
      .attr("class", edgeClassFn)
      .attr("d", edgePathD)
      .attr("marker-end", edgeMarkerEnd)
      .attr("opacity", 0)
    val _      = edgeEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allEds = canvas.selectAll("path.graph-explorer__edge")
    val _      = allEds.attr("class", edgeClassFn).attr("marker-end", edgeMarkerEnd)
    val _ =
      if animate then
        allEds
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("d", edgePathD)
          .attr("opacity", 1)
      else allEds.attr("d", edgePathD).attr("opacity", 1)
    val _ = edgeSel.exit().remove()

    // ── Edge weight labels ───────────────────────────────────────────────────
    val weightData: js.Array[js.Any] =
      if !spec.weighted then js.Array()
      else
        spec.edges.flatMap { e =>
          (positions.get(e.from), positions.get(e.to)) match
            case (Some(p1), Some(p2)) =>
              e.weight.map { w =>
                val mx = (p1._1 + p2._1) / 2
                val my = (p1._2 + p2._2) / 2
                js.Dynamic
                  .literal(
                    id = edgeId(e),
                    mx = mx,
                    my = my - WeightOffset,
                    weight = if w == w.toInt then w.toInt.toString else w.toString
                  )
                  .asInstanceOf[js.Any]
              }
            case _ => None
        }.toJSArray
    val weightSel = canvas.selectAll("text.graph-explorer__edge-weight").data(weightData, edgeKeyFn)
    val weightX: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].mx
    val weightY: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].my
    val weightText: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].weight
    val weightEnter = weightSel
      .enter()
      .append("text")
      .attr("class", "graph-explorer__edge-weight")
      .attr("text-anchor", "middle")
      .attr("x", weightX)
      .attr("y", weightY)
      .attr("opacity", 0)
      .text(weightText)
    val _      = weightEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allWts = canvas.selectAll("text.graph-explorer__edge-weight")
    val _      = allWts.text(weightText)
    val _ =
      if animate then
        allWts.transition().duration(TransitionDurationMs).attr("x", weightX).attr("y", weightY).attr(
          "opacity",
          1
        )
      else allWts.attr("x", weightX).attr("y", weightY).attr("opacity", 1)
    val _ = weightSel.exit().remove()

    // ── Grid backdrop (grid layout only) ──────────────────────────────────────
    val backdropData: js.Array[js.Any] =
      if spec.layout != LayoutGrid then js.Array()
      else
        spec.nodes.flatMap { n =>
          (n.row, n.col) match
            case (Some(r), Some(c)) =>
              Some(
                js.Dynamic
                  .literal(
                    id = n.id,
                    x = PaddingX + c * (GridCellSize + GridCellGap),
                    y = PaddingY + r * (GridCellSize + GridCellGap),
                    label = n.label
                  )
                  .asInstanceOf[js.Any]
              )
            case _ => None
        }.toJSArray
    val backdropKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val backdropSel   = canvas.selectAll("rect.graph-explorer__grid-cell").data(backdropData, backdropKeyFn)
    val backdropEnter = backdropSel.enter().append("rect").attr("class", "graph-explorer__grid-cell")
    val _             = backdropEnter.attr("width", GridCellSize).attr("height", GridCellSize).attr("rx", 4)
    val allBacks      = canvas.selectAll("rect.graph-explorer__grid-cell")
    val _ = allBacks
      .attr("x", ((d, _) => d.asInstanceOf[js.Dynamic].x): js.Function2[js.Any, Int, js.Any])
      .attr("y", ((d, _) => d.asInstanceOf[js.Dynamic].y): js.Function2[js.Any, Int, js.Any])
    val _ = backdropSel.exit().remove()

    // ── Nodes ─────────────────────────────────────────────────────────────────
    val nodeData: js.Array[js.Any] = spec.nodes.flatMap { n =>
      positions.get(n.id).map { case (x, y) =>
        val rawState = step.nodeStates.getOrElse(n.id, "unseen")
        val state    = if NodeStates.contains(rawState) then rawState else "unseen"
        js.Dynamic
          .literal(
            id = n.id,
            label = n.label,
            x = x,
            y = y,
            state = state
          )
          .asInstanceOf[js.Any]
      }
    }.toJSArray
    val nodeKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val nodeClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val state = d.asInstanceOf[js.Dynamic].state.asInstanceOf[String]
      s"graph-explorer__node graph-explorer__node--$state"
    val nodeTransformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val nodeLabelFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].label

    val nodeSel = canvas.selectAll("g.graph-explorer__node").data(nodeData, nodeKeyFn)
    val nodeEnter = nodeSel
      .enter()
      .append("g")
      .attr("class", nodeClassFn)
      .attr("transform", nodeTransformFn)
      .attr("opacity", 0)
    val _ = nodeEnter
      .append("circle")
      .attr("class", "graph-explorer__node-circle")
      .attr("r", NodeRadius)
    val _ = nodeEnter
      .append("text")
      .attr("class", "graph-explorer__node-label")
      .attr("text-anchor", "middle")
      .attr("y", 5)
      .text(nodeLabelFn)
    val _ = nodeEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)

    val allNodes = canvas.selectAll("g.graph-explorer__node")
    val _        = allNodes.attr("class", nodeClassFn)
    // `.select()` (singular) — see panel renderer for the why.
    val _ = allNodes.select("text.graph-explorer__node-label").text(nodeLabelFn)
    val _ =
      if animate then
        allNodes
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", nodeTransformFn)
          .attr("opacity", 1)
      else allNodes.attr("transform", nodeTransformFn).attr("opacity", 1)
    val _ = nodeSel.exit().remove()

    // ── Markers (optional pointer overlay; defensive lookup via positions) ───
    val activeMarkers = step.markers.filter(m => positions.contains(m.nodeId))
    val markerData: js.Array[js.Any] = activeMarkers.map { m =>
      val (x, y) = positions.getOrElse(m.nodeId, (0.0, 0.0))
      js.Dynamic
        .literal(
          name = m.name,
          nodeId = m.nodeId,
          x = x,
          y = y - NodeRadius - 8,
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].name
    val markerTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val markerLabelFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val name      = dyn.name.asInstanceOf[String]
      if canonical then name else s"⚠ $name"
    val markerSel = canvas.selectAll("g.graph-explorer__marker").data(markerData, markerKeyFn)
    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "graph-explorer__marker")
      .attr("transform", markerTransform)
      .attr("opacity", 0)
    val _ = markerEnter
      .append("text")
      .attr("class", "graph-explorer__marker-label")
      .attr("text-anchor", "middle")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(markerLabelFn)
    val _        = markerEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allMarks = canvas.selectAll("g.graph-explorer__marker")
    val _ = allMarks
      .select("text.graph-explorer__marker-label")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(markerLabelFn)
    val _ =
      if animate then
        allMarks.transition().duration(TransitionDurationMs).attr("transform", markerTransform).attr(
          "opacity",
          1
        )
      else allMarks.attr("transform", markerTransform).attr("opacity", 1)
    val _ = markerSel.exit().remove()

    // ── Panels ────────────────────────────────────────────────────────────────
    renderPanels(svg, spec, step, positions, animate)

  // ---------------------------------------------------------------------------
  // Panel renderer — sidecar tables to the right of the canvas. Each panel
  // descriptor in `spec.panels` becomes a group; per-step `step.tables[name]`
  // populates the cells.
  // ---------------------------------------------------------------------------

  private def renderPanels(
      svg: D3.Selection,
      spec: Spec,
      step: Step,
      positions: Map[String, (Double, Double)],
      animate: Boolean
  ): Unit =
    if spec.panels.isEmpty then
      val _ = svg.selectAll("g.graph-explorer__panel").remove()
      return
    val originX = canvasRight(spec, positions) + PanelGap
    var cursorY = PaddingY
    val panelInfos = spec.panels.map { p =>
      val titleY = cursorY
      val rowY   = cursorY + PanelTitleH + PanelLabelGap
      val nextY  = rowY + PanelCellH + PanelRowGap
      val cells  = step.tables.getOrElse(p.name, Nil)
      val labels = p.labels
      val info   = (p, titleY, rowY, cells, labels)
      cursorY = nextY
      info
    }

    val panelData: js.Array[js.Any] = panelInfos.map { case (p, titleY, _, _, _) =>
      js.Dynamic
        .literal(
          name = p.name,
          kind = p.kind,
          titleY = titleY
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val panelKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].name

    val panelSel = svg.selectAll("g.graph-explorer__panel").data(panelData, panelKeyFn)
    val panelEnter = panelSel
      .enter()
      .append("g")
      .attr(
        "class",
        (
            (d, _) =>
              s"graph-explorer__panel graph-explorer__panel--${d.asInstanceOf[js.Dynamic].kind.asInstanceOf[String]}"
        ): js.Function2[js.Any, Int, js.Any]
      )
      .attr(
        "data-panel-name",
        ((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any]
      )
    val _ = panelEnter
      .append("text")
      .attr("class", "graph-explorer__panel-title")
      .attr("text-anchor", "start")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])

    // Position each panel group + render contents. Each group carries a
    // `data-panel-name` attribute so we can address it directly by name
    // without relying on a D3 filter that the local Selection facade
    // doesn't expose.
    val panelGroups = svg.selectAll("g.graph-explorer__panel")
    val _ = panelGroups
      .select("text.graph-explorer__panel-title")
      .attr("x", originX)
      .attr(
        "y",
        ((d, _) => d.asInstanceOf[js.Dynamic].titleY.asInstanceOf[Double] + 12): js.Function2[
          js.Any,
          Int,
          js.Any
        ]
      )

    panelInfos.foreach { case (panel, _, rowY, cells, labels) =>
      val sel = svg.select(s"g.graph-explorer__panel[data-panel-name='${panel.name}']")
      panel.kind match
        case `PanelArray` => renderArrayPanel(sel, panel, cells, labels, originX, rowY, animate)
        case _            => renderListPanel(sel, panel, cells, originX, rowY, animate)
    }

    val _ = panelSel.exit().remove()

  private def renderArrayPanel(
      panelGroup: D3.Selection,
      panel: Panel,
      cells: List[String],
      labels: List[String],
      originX: Double,
      rowY: Double,
      // Reserved for future cell-value tween transitions; current update path
      // sets attrs directly, which is fine for the Arc 1 payloads.
      @annotation.unused animate: Boolean
  ): Unit =
    val n = math.max(labels.size, cells.size)
    val rowData: js.Array[js.Any] = (0 until n).map { i =>
      js.Dynamic
        .literal(
          idx = i,
          label = labels.lift(i).getOrElse(""),
          value = cells.lift(i).getOrElse("")
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"${panel.name}-${dyn.idx.asInstanceOf[Int]}"
    val cellX: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val i = d.asInstanceOf[js.Dynamic].idx.asInstanceOf[Int]
      originX + i * (PanelCellW + PanelCellGap)
    val labelTextFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].label
    val valueTextFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].value

    // Cell groups
    val cellSel   = panelGroup.selectAll("g.graph-explorer__panel-cell").data(rowData, keyFn)
    val cellEnter = cellSel.enter().append("g").attr("class", "graph-explorer__panel-cell")
    val _ = cellEnter
      .append("text")
      .attr("class", "graph-explorer__panel-label")
      .attr("text-anchor", "middle")
      .attr("y", rowY - 4)
      .text(labelTextFn)
    val _ = cellEnter
      .append("rect")
      .attr("class", "graph-explorer__panel-rect")
      .attr("width", PanelCellW)
      .attr("height", PanelCellH)
      .attr("rx", 4)
      .attr("y", rowY)
    val _ = cellEnter
      .append("text")
      .attr("class", "graph-explorer__panel-value")
      .attr("text-anchor", "middle")
      .attr("y", rowY + PanelCellH / 2 + 5)
      .text(valueTextFn)

    val allCells = panelGroup.selectAll("g.graph-explorer__panel-cell")
    // `.select(...)` (singular) propagates the parent cell-group's CURRENT
    // datum to the child text/rect, so `text(valueTextFn)` reads the freshly
    // bound `value`. `selectAll(descendant).text(...)` would read the child's
    // stale enter-time datum — same trap as BinaryTree's `nodeAll.select(...)`
    // pattern.
    val labelXFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      cellX(d, 0).asInstanceOf[Double] + PanelCellW / 2
    val _ = allCells
      .select("text.graph-explorer__panel-label")
      .text(labelTextFn)
      .attr("x", labelXFn)
      .attr("y", rowY - 4)
    val _ = allCells
      .select("rect.graph-explorer__panel-rect")
      .attr("x", cellX)
      .attr("y", rowY)
    val _ = allCells
      .select("text.graph-explorer__panel-value")
      .text(valueTextFn)
      .attr("x", labelXFn)
      .attr("y", rowY + PanelCellH / 2 + 5)
    val _ = cellSel.exit().remove()

  private def renderListPanel(
      panelGroup: D3.Selection,
      panel: Panel,
      cells: List[String],
      originX: Double,
      rowY: Double,
      // Reserved for future enter/exit transitions on individual pills;
      // current update path sets attrs directly.
      @annotation.unused animate: Boolean
  ): Unit =
    val rowData: js.Array[js.Any] = cells.zipWithIndex.map { case (v, i) =>
      js.Dynamic
        .literal(
          idx = i,
          value = v,
          // Key on value+position so identical strings in different positions
          // get different identities. For pure FIFO this isn't strict but it
          // keeps DOM size stable.
          key = s"$v#$i"
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"${panel.name}-${dyn.key.asInstanceOf[String]}"
    val cellX: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val i = d.asInstanceOf[js.Dynamic].idx.asInstanceOf[Int]
      originX + i * (PanelCellW + PanelCellGap)
    val valueTextFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].value

    val cellSel   = panelGroup.selectAll("g.graph-explorer__panel-pill").data(rowData, keyFn)
    val cellEnter = cellSel.enter().append("g").attr("class", "graph-explorer__panel-pill").attr("opacity", 0)
    val _ = cellEnter
      .append("rect")
      .attr("class", "graph-explorer__panel-pill-rect")
      .attr("width", PanelCellW)
      .attr("height", PanelCellH)
      .attr("rx", PanelCellH / 2)
      .attr("y", rowY)
    val _ = cellEnter
      .append("text")
      .attr("class", "graph-explorer__panel-pill-text")
      .attr("text-anchor", "middle")
      .attr("y", rowY + PanelCellH / 2 + 5)
      .text(valueTextFn)
    val _ = cellEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)

    val allCells = panelGroup.selectAll("g.graph-explorer__panel-pill")
    val pillXFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      cellX(d, 0).asInstanceOf[Double] + PanelCellW / 2
    // Same `.select()`-not-`.selectAll()` rule as the array panel — propagate
    // the pill group's current datum down to the rect + text children.
    val _ = allCells
      .select("rect.graph-explorer__panel-pill-rect")
      .attr("x", cellX)
      .attr("y", rowY)
    val _ = allCells
      .select("text.graph-explorer__panel-pill-text")
      .text(valueTextFn)
      .attr("x", pillXFn)
      .attr("y", rowY + PanelCellH / 2 + 5)
    val _ = cellSel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .customBy { (_, specM) =>
        val stepCount = specM.value.toOption.fold(0)(_.steps.size)
        Stepper.hook(Stepper.Input(stepCount, StepDelayMs.toDouble))
      }
      .useRefToVdom[dom.html.Element]
      .useRefBy(_ => false) // hasRendered (mutable; avoids re-render cycle)
      .useEffectWithDepsBy((_, specM, stepper, _, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), stepper.index)
      ) { (_, specM, _, hostRef, hasRenderedRef) => (_, index) =>
        specM.value.toOption.filter(_.steps.nonEmpty) match
          case Some(spec) =>
            hostRef.foreach { host =>
              val positions = layoutPositions(spec)
              val w         = viewBoxWidth(spec, positions)
              val h         = viewBoxHeight(spec, positions)
              val svgEl     = ensureSvg(host, spec, w, h)
              val step      = spec.steps(index)
              val animate   = hasRenderedRef.value
              renderStep(svgEl, spec, step, animate)
              if !hasRenderedRef.value then hasRenderedRef.value = true
            }
          case None => Callback.empty
      }
      .render { (_, specM, stepper, hostRef, _) =>
        specM.value match
          case Left(err) =>
            <.div(
              ^.className := "d3-widget__error",
              <.p(^.className   := "d3-widget__error-title", "Graph-explorer payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then Step(Map.empty, Map.empty, Map.empty, Nil, "No steps defined.")
              else spec.steps(idx)

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "graph-explorer__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "graph-explorer__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("graph-explorer__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "graph-explorer__button graph-explorer__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("graph-explorer__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("graph-explorer__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "graph-explorer__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("graph-explorer__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "graph-explorer__button graph-explorer__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("graph-explorer__button-icon"))
                  ),
                  <.span(
                    ^.className := "graph-explorer__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"graph-explorer graph-explorer--${spec.layout} not-prose",
              spec.title
                .map(t => <.p(^.className := "graph-explorer__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              <.div(
                ^.className := "graph-explorer__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "graph-explorer__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              controls
            )
      }
