package codefolio.client.components.cortex.widgets

import codefolio.client.components.cortex.widgets.PayloadDecoder.*
import codefolio.client.components.icons.LucideIcons
import codefolio.client.d3.D3
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/**
 * Binary-tree stepper — fourth widget in the D3 catalog. Renders rooted binary trees and animates
 * node-by-node operations across five mode flavours sharing a single tree-layout core:
 *
 *   - `mode:"binary"` — plain binary tree (Phase 6, 18 chapters).
 *   - `mode:"bst"` — binary search tree (Phase 7, 13 chapters).
 *   - `mode:"avl"` — AVL tree with per-node balance-factor labels.
 *   - `mode:"rbt"` — red-black tree with red/black node fills.
 *   - `mode:"treap"` — treap with per-node heap-priority badges.
 *
 * Optional `sidebar` renders the recursion call-stack OR an explicit iterator stack to the right of the tree
 * (used by iterative-traversal and BST-iterator chapters — the heaviest user of this feature). Optional
 * `outputItems` per step renders a horizontal strip of cells below the tree showing the running traversal
 * output (e.g. inorder sequence so far).
 *
 * Per-step `nodesOverride` mutates one or more nodes (value / left / right / color / balanceFactor / priority
 * / style) on top of the spec's base topology — used for insertion, rotation, recolour, and BST-delete
 * value-copy animations. Per-step `nodeStyles` is a lighter sidecar that only flips the style class (e.g.
 * mark a node `"visited"` after a postorder traversal returns from it).
 *
 * Layout: a recursive subtree-width algorithm — leaves take one slot; each internal node sits at the midpoint
 * of its children's allocated ranges. Handles unbalanced and skewed trees without overlap (a left-skewed
 * chain renders as a diagonal cascade rather than three nodes stacked at the same x). The layout function is
 * intentionally local — once a second consumer materialises (`heap-tree`, `recurrence-tree`,
 * `decision-tree`), it will be lifted to a shared `TreeLayout` module per the widget-spec's Arc 2 note.
 *
 * Rendering follows the same React + D3 boundary as the other catalog widgets (ADR-0013): React owns a host
 * `<div>`; D3 owns the `<svg>` it creates inside that div. Nodes keyed by `node.id` so a rotation transitions
 * transforms smoothly; edges keyed by `${parent.id}->${child.id}` so a rewired edge enters/exits rather than
 * slides through wrong endpoints. Marker triangles + labels sit above each node like the queue-mode pointers
 * in `StackQueue`. Sidebar frames are keyed by `${depth}-${label}` so a push/pop animates as enter/exit
 * rather than re-flowing the whole stack.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title": "Recursive inorder traversal",
 *   "mode":  "binary",                              // binary | bst | avl | rbt | treap
 *   "root":  "n5",                                  // id of the root node
 *   "nodes": [
 *     {"id": "n5", "value": "5", "left": "n3", "right": "n7"},
 *     {"id": "n3", "value": "3", "left": null, "right": null,
 *      "color": "red",                              // rbt only
 *      "balanceFactor": -1,                         // avl only
 *      "priority": 42,                              // treap only
 *      "style": "highlight"                         // optional pre-set style
 *     },
 *     ...
 *   ],
 *   "sidebar": {"kind": "call-stack", "label": "Recursion"},   // optional
 *   "steps": [
 *     {
 *       "markers":       [{"name": "current", "nodeId": "n5"}],
 *       "nodeStyles":    {"n3": "visited"},
 *       "nodesOverride": [{"id": "n5", "value": "6", "left": null}],
 *       "sidebarFrames": [{"label": "inorder(5)", "kind": "active"}],
 *       "outputItems":   ["1", "3", "5"],
 *       "msg":           "Visit 5; append to output."
 *     },
 *     ...
 *   ]
 * }
 * }}}
 *
 * Marker names are checked against [[MarkerCanon]]; author-supplied `color` fields on markers are dropped at
 * parse time. Style strings outside the canonical set (`visited`, `highlight`, `removed`, `match`, `new`) are
 * dropped with a console warning. Mode-specific decorators are silently ignored for the wrong mode (a `color`
 * field on a `binary` mode node renders nothing).
 */
object BinaryTree:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose.
  // ---------------------------------------------------------------------------

  // Tree node — `left` / `right` are `Option[String]` child ids. Mode-specific
  // annotations are optional everywhere; the renderer only consults the ones
  // relevant to the current mode. `style` carries the per-node initial style;
  // per-step `nodeStyles` overrides it for a single step's render.
  final case class TreeNode(
      id: String,
      value: String,
      left: Option[String],
      right: Option[String],
      color: Option[String],
      balanceFactor: Option[Int],
      priority: Option[Int],
      style: Option[String]
  )

  // Marker — single loose case class with `nodeId` + canonical flag. No public
  // `color` field — colour resolves through `MarkerCanon`.
  final case class Marker(name: String, nodeId: String, canonical: Boolean)

  // Per-step override of a node's fields. Each Option distinguishes "the
  // override touches this field" from "the field is unchanged". For `left` /
  // `right` we need a third state (set to null = clear the child link); the
  // nested `Option[Option[String]]` represents (touched? × cleared?):
  //   - None              = the override doesn't touch left/right
  //   - Some(None)        = the override clears left/right (JSON: null)
  //   - Some(Some(id))    = the override sets left/right to `id`
  final case class NodeOverride(
      id: String,
      value: Option[String],
      left: Option[Option[String]],
      right: Option[Option[String]],
      color: Option[String],
      balanceFactor: Option[Int],
      priority: Option[Int],
      style: Option[String]
  )

  final case class SidebarFrame(label: String, kind: Option[String])
  final case class Sidebar(kind: String, label: Option[String])

  final case class Step(
      markers: List[Marker],
      nodeStyles: Map[String, String],
      nodesOverride: List[NodeOverride],
      sidebarFrames: List[SidebarFrame],
      outputItems: List[String],
      // Per-step root override — for rotations (AVL/RBT/treap) where the
      // node that's the root changes. Defaults to spec.root when absent.
      root: Option[String],
      msg: String
  )

  final case class Spec(
      mode: String,
      root: String,
      nodes: List[TreeNode],
      sidebar: Option[Sidebar],
      title: Option[String],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants — sized to fit comfortably inside a chapter's prose
  // column for trees up to ~7 nodes wide and ~5 levels deep.
  // ---------------------------------------------------------------------------

  private val NodeRadius           = 22.0
  private val HSpacing             = 70.0 // horizontal slot width per leaf
  private val VSpacing             = 76.0 // vertical gap between levels
  private val MarkerLane           = 32.0 // headroom above top row for marker labels
  private val PaddingX             = 20.0
  private val PaddingY             = 18.0
  private val SidebarWidth         = 150.0
  private val SidebarLabelH        = 22.0 // section label above frames
  private val SidebarRowH          = 30.0
  private val SidebarGap           = 4.0
  private val OutputStripGap       = 14.0
  private val OutputCellSize       = 40.0
  private val OutputCellGap        = 4.0
  private val OutputLabelH         = 18.0
  private val StepDelayMs          = 1400
  private val TransitionDurationMs = 450.0
  private val FadeMs               = 350.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // Mode names — closed set; unknown values collapse to `binary` in the parser.
  private val ModeBinary = "binary"
  private val ModeBst    = "bst"
  private val ModeAvl    = "avl"
  private val ModeRbt    = "rbt"
  private val ModeTreap  = "treap"
  private val Modes      = Set(ModeBinary, ModeBst, ModeAvl, ModeRbt, ModeTreap)

  // Canonical node-style vocabulary (mirrors LinkedList's). Unknown values are
  // dropped at parse with a console warning.
  private val CanonicalNodeStyles: Set[String] =
    Set("new", "removed", "highlight", "visited", "match")

  // Sidebar kind vocabulary — kept as plain strings.
  private val SidebarCallStack = "call-stack"
  private val SidebarIterStack = "iterator-stack"

  // RBT node colour values. Anything else collapses to None.
  private val NodeColorRed   = "red"
  private val NodeColorBlack = "black"
  private val RbtColors      = Set(NodeColorRed, NodeColorBlack)

  // Sidebar frame kinds — `active` is highlighted, `returned` is muted, `queued`
  // is the BFS / iterator-pending state. Default (no kind) renders plain.
  private val FrameKindActive   = "active"
  private val FrameKindReturned = "returned"
  private val FrameKindQueued   = "queued"
  private val FrameKinds        = Set(FrameKindActive, FrameKindReturned, FrameKindQueued)

  // ---------------------------------------------------------------------------
  // Parsing — every required field validated inside `PayloadDecoder.run`. Any
  // thrown exception collapses to `Left(msg)` and the renderer shows an inline
  // error placeholder instead of crashing the chapter.
  // ---------------------------------------------------------------------------

  private def parseNode(d: js.Dynamic): TreeNode =
    val rawStyle = d.optString("style")
    val style = rawStyle.flatMap { s =>
      if CanonicalNodeStyles.contains(s) then Some(s)
      else
        dom.console.warn(
          s"binary-tree: unknown node style '$s' (expected one of: ${CanonicalNodeStyles.toList.sorted.mkString(", ")}). Dropping."
        )
        None
    }
    val rawColor = d.optString("color")
    val color = rawColor.flatMap { c =>
      if RbtColors.contains(c) then Some(c)
      else
        dom.console.warn(
          s"binary-tree: unknown node color '$c' (expected: ${RbtColors.toList.sorted.mkString(", ")}). Dropping."
        )
        None
    }
    val rawValue = d.selectDynamic("value").asInstanceOf[js.UndefOr[js.Any]].toOption
    TreeNode(
      id = d.string("id"),
      value = rawValue.fold("")(v => js.Dynamic.global.String(v).asInstanceOf[String]),
      left = parseChildField(d, "left"),
      right = parseChildField(d, "right"),
      color = color,
      balanceFactor = d.optInt("balanceFactor"),
      priority = d.optInt("priority"),
      style = style
    )

  // Read a child-id field. Missing key → None; null → None; non-empty string → Some(id).
  // (Top-level spec.nodes treats null and missing the same — a node with no child.)
  private def parseChildField(d: js.Dynamic, key: String): Option[String] =
    val raw = d.selectDynamic(key)
    if js.isUndefined(raw) then None
    else if raw == null then None
    else
      val s = js.Dynamic.global.String(raw).asInstanceOf[String]
      if s.isEmpty then None else Some(s)

  // Read a child-id override. Distinguishes missing-key (keep) from null (clear).
  //   None              — the override doesn't touch this child link
  //   Some(None)        — the override clears this child link (JSON: null)
  //   Some(Some(id))    — the override sets this child link to `id`
  private def parseChildOverride(d: js.Dynamic, key: String): Option[Option[String]] =
    val raw = d.selectDynamic(key)
    if js.isUndefined(raw) then None
    else if raw == null then Some(None)
    else
      val s = js.Dynamic.global.String(raw).asInstanceOf[String]
      if s.isEmpty then Some(None) else Some(Some(s))

  private def parseMarker(d: js.Dynamic): Marker =
    val name      = d.string("name")
    val nodeId    = d.string("nodeId")
    val rawColor  = d.optString("color")
    val canonical = MarkerCanon.isCanonical(name)
    if rawColor.isDefined then MarkerCanon.warnAuthorColor("binary-tree", name)
    if name.nonEmpty && !canonical then MarkerCanon.warnUnknown("binary-tree", name)
    Marker(name, nodeId, canonical)

  private def parseNodeOverride(d: js.Dynamic): NodeOverride =
    val rawStyle = d.optString("style")
    val style = rawStyle.flatMap { s =>
      if CanonicalNodeStyles.contains(s) then Some(s)
      else
        dom.console.warn(s"binary-tree: unknown override style '$s'. Dropping.")
        None
    }
    val rawColor = d.optString("color")
    val color = rawColor.flatMap { c =>
      if RbtColors.contains(c) then Some(c) else None
    }
    val rawValue = d.selectDynamic("value").asInstanceOf[js.UndefOr[js.Any]].toOption
      .map(v => js.Dynamic.global.String(v).asInstanceOf[String])
    NodeOverride(
      id = d.string("id"),
      value = rawValue,
      left = parseChildOverride(d, "left"),
      right = parseChildOverride(d, "right"),
      color = color,
      balanceFactor = d.optInt("balanceFactor"),
      priority = d.optInt("priority"),
      style = style
    )

  private def parseNodeStyles(d: js.Dynamic): Map[String, String] =
    val raw = d.selectDynamic("nodeStyles").asInstanceOf[js.UndefOr[js.Dynamic]].toOption
    raw match
      case None => Map.empty
      case Some(obj) =>
        val keys = js.Object.keys(obj.asInstanceOf[js.Object]).toList
        keys.flatMap { k =>
          val v = obj.selectDynamic(k).asInstanceOf[js.UndefOr[String]].toOption
          v.flatMap { s =>
            if CanonicalNodeStyles.contains(s) then Some(k -> s)
            else
              dom.console.warn(s"binary-tree: unknown nodeStyles value '$s' for node '$k'. Dropping.")
              None
          }
        }.toMap

  private def parseSidebarFrame(d: js.Dynamic): SidebarFrame =
    val kind = d.optString("kind").filter(FrameKinds.contains)
    SidebarFrame(label = d.string("label"), kind = kind)

  private def parseSidebar(d: js.Dynamic): Sidebar =
    val kind = d.string("kind")
    val effective =
      if kind == SidebarCallStack || kind == SidebarIterStack then kind
      else SidebarCallStack
    Sidebar(kind = effective, label = d.optString("label"))

  private def parseStep(d: js.Dynamic): Step =
    Step(
      markers = d.dynList("markers").map(parseMarker).filter(m => m.name.nonEmpty && m.nodeId.nonEmpty),
      nodeStyles = parseNodeStyles(d),
      nodesOverride = d.dynList("nodesOverride").map(parseNodeOverride).filter(_.id.nonEmpty),
      sidebarFrames = d.dynList("sidebarFrames").map(parseSidebarFrame).filter(_.label.nonEmpty),
      outputItems = d.stringList("outputItems").getOrElse(Nil),
      root = d.optString("root"),
      msg = d.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawMode = d.optString("mode").getOrElse(ModeBinary)
      val mode    = if Modes.contains(rawMode) then rawMode else ModeBinary
      val nodes   = d.dynList("nodes").map(parseNode).filter(_.id.nonEmpty)
      if nodes.isEmpty then throw PayloadDecoder.invalid("nodes must be non-empty")
      val root = d.optString("root").getOrElse(nodes.head.id)
      if !nodes.exists(_.id == root) then
        throw PayloadDecoder.invalid(s"root '$root' is not in nodes")
      val steps = d.dynList("steps").map(parseStep)
      if steps.isEmpty then throw PayloadDecoder.invalid("steps must be non-empty")
      Spec(
        mode = mode,
        root = root,
        nodes = nodes,
        sidebar = d.optObj("sidebar").map(parseSidebar),
        title = d.optString("title"),
        steps = steps
      )
    }

  // ---------------------------------------------------------------------------
  // Per-step effective tree — base spec.nodes with the step's nodesOverride
  // applied. Returns nodes indexed by id so the renderer can look up
  // current values cheaply.
  // ---------------------------------------------------------------------------

  private def applyOverrides(spec: Spec, step: Step): Map[String, TreeNode] =
    val base = spec.nodes.map(n => n.id -> n).toMap
    step.nodesOverride.foldLeft(base) { (acc, ov) =>
      acc.get(ov.id) match
        case None =>
          // Override targets a node not in spec.nodes — author intent is "this is
          // a fresh node entering the tree this step". Synthesise a minimal
          // TreeNode from the override's fields; fall back to empty value when
          // the override doesn't include one (the author should always set value
          // for a new node — render an empty bubble if not).
          val newNode = TreeNode(
            id = ov.id,
            value = ov.value.getOrElse(""),
            left = ov.left.flatten,
            right = ov.right.flatten,
            color = ov.color,
            balanceFactor = ov.balanceFactor,
            priority = ov.priority,
            style = ov.style
          )
          acc + (ov.id -> newNode)
        case Some(n) =>
          val merged = n.copy(
            value = ov.value.getOrElse(n.value),
            left = ov.left.getOrElse(n.left),
            right = ov.right.getOrElse(n.right),
            color = ov.color.orElse(n.color),
            balanceFactor = ov.balanceFactor.orElse(n.balanceFactor),
            priority = ov.priority.orElse(n.priority),
            style = ov.style.orElse(n.style)
          )
          acc + (ov.id -> merged)
    }

  // ---------------------------------------------------------------------------
  // Tree layout — recursive subtree-width algorithm. Each leaf occupies one
  // horizontal slot; each internal node sits at the midpoint of its children's
  // assigned slot ranges. Returns Map[id → (layoutX, layoutY)] in slot units
  // (depth for y; abstract slot index for x). The renderer scales these to
  // pixel coordinates via `pixelXAtSlot`/`pixelYAtDepth`.
  // ---------------------------------------------------------------------------

  // Reachable nodes from root via left/right links — defends against orphan
  // ids left in spec.nodes after a delete override.
  private def reachableNodes(nodes: Map[String, TreeNode], root: String): Map[String, TreeNode] =
    if !nodes.contains(root) then Map.empty
    else
      val out   = mutable.Map.empty[String, TreeNode]
      val stack = mutable.Stack[String](root)
      while stack.nonEmpty do
        val id = stack.pop()
        if !out.contains(id) && nodes.contains(id) then
          val n = nodes(id)
          out += (id -> n)
          n.right.foreach(stack.push)
          n.left.foreach(stack.push)
      out.toMap

  // Pure layout — computes (slotX, depth) per node. Layout x is a Double so
  // a midpoint between siblings can be half-integer when a parent has only
  // one child (skewed chain).
  private def computeLayout(
      nodes: Map[String, TreeNode],
      root: String
  ): (Map[String, (Double, Double)], Int) =
    val positions = mutable.Map.empty[String, (Double, Double)]
    val nextSlot  = Array(0.0)
    var maxDepth  = 0
    def visit(id: String, depth: Int): Unit =
      if !nodes.contains(id) then ()
      else
        val n = nodes(id)
        if depth > maxDepth then maxDepth = depth
        n.left.foreach(visit(_, depth + 1))
        val selfSlot = (n.left, n.right) match
          case (Some(l), Some(r)) =>
            // After visiting left (already done via foreach above), `nextSlot`
            // is the slot AFTER the left subtree's rightmost descendant.
            // Reserve a gap slot between subtrees so the parent's midpoint
            // falls on an integer slot (cleaner visual rhythm).
            val leftEnd = positions(l)._1
            nextSlot(0) += 1.0
            visit(r, depth + 1)
            val rightStart = positions(r)._1
            (leftEnd + rightStart) / 2.0
          case (Some(_), None) =>
            val mySlot = nextSlot(0)
            nextSlot(0) += 1.0
            mySlot
          case (None, Some(r)) =>
            val mySlot = nextSlot(0)
            nextSlot(0) += 1.0
            visit(r, depth + 1)
            // Centre self slightly left of the right subtree's root so the
            // edge slopes naturally.
            val rightStart = positions(r)._1
            (mySlot + rightStart) / 2.0
          case (None, None) =>
            val mySlot = nextSlot(0)
            nextSlot(0) += 1.0
            mySlot
        positions(id) = (selfSlot, depth.toDouble)
    visit(root, 0)
    (positions.toMap, maxDepth)

  // ---------------------------------------------------------------------------
  // Pixel-coordinate helpers.
  // ---------------------------------------------------------------------------

  private def pixelX(layoutX: Double, slotCount: Double): Double =
    // slotCount is total slots used (== leaf count for a balanced tree). We
    // anchor (slot 0) at PaddingX + half-spacing so the leftmost leaf sits
    // one half-slot in from the padding edge.
    val _ = slotCount
    PaddingX + (layoutX + 0.5) * HSpacing

  private def pixelY(layoutY: Double): Double =
    PaddingY + MarkerLane + (layoutY + 0.5) * VSpacing

  private def treePixelWidth(slotCount: Double): Double =
    PaddingX * 2 + slotCount.max(1.0) * HSpacing

  private def treePixelHeight(maxDepth: Int): Double =
    PaddingY * 2 + MarkerLane + (maxDepth + 1) * VSpacing

  // ---------------------------------------------------------------------------
  // SVG bootstrap.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", s"binary-tree__svg binary-tree__svg--${spec.mode}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Binary tree (${spec.mode})"))
      svg.setAttribute("xmlns", SvgNs)
      // viewBox + width/height get set on every renderStep (size depends on
      // the per-step tree topology after overrides + the sidebar/output-strip
      // presence per step). 400×200 is a placeholder for first paint.
      svg.setAttribute("viewBox", "0 0 400 200")
      svg.setAttribute("width", "400")
      svg.setAttribute("height", "200")
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Render — runs after every step / spec change. Idempotent.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg                = D3.select(svgEl)
    val moveDur            = if animate then TransitionDurationMs else 0.0
    val fadeDur            = if animate then FadeMs else 0.0
    val effective          = applyOverrides(spec, step)
    val effectiveRoot      = step.root.getOrElse(spec.root)
    val reachable          = reachableNodes(effective, effectiveRoot)
    val (layout, maxDepth) = computeLayout(reachable, effectiveRoot)
    val slotCount          = layout.values.map(_._1).maxOption.fold(0.0)(_ + 1.0).max(1.0)
    val treeW              = treePixelWidth(slotCount)
    val treeH              = treePixelHeight(maxDepth)
    val hasSidebar         = spec.sidebar.isDefined
    val sidebarW           = if hasSidebar then SidebarWidth else 0.0
    val outputCount        = step.outputItems.size
    val hasOutput          = outputCount > 0
    val outputStripH =
      if !hasOutput then 0.0
      else OutputStripGap + OutputLabelH + OutputCellSize + 4.0
    val totalW = treeW + sidebarW
    val totalH = treeH + outputStripH

    val _ = svg.attr("viewBox", s"0 0 $totalW $totalH")
    val _ = svg.attr("width", totalW.toString)
    val _ = svg.attr("height", totalH.toString)

    // ── Empty-tree placeholder ─────────────────────────────────────────────
    if reachable.isEmpty then
      val _                                          = svg.selectAll("g.binary-tree__node-group").remove()
      val _                                          = svg.selectAll("path.binary-tree__edge").remove()
      val _                                          = svg.selectAll("g.binary-tree__marker").remove()
      val _                                          = svg.selectAll("g.binary-tree__sidebar-group").remove()
      val _                                          = svg.selectAll("g.binary-tree__output-group").remove()
      val phData: js.Array[js.Any]                   = js.Array("(empty tree)").asInstanceOf[js.Array[js.Any]]
      val phKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "ph"
      val phSel = svg.selectAll("text.binary-tree__empty-placeholder").data(phData, phKeyFn)
      val _ = phSel
        .enter()
        .append("text")
        .attr("class", "binary-tree__empty-placeholder")
        .attr("text-anchor", "middle")
        .attr("x", totalW / 2)
        .attr("y", totalH / 2 + 6)
        .text("(empty tree)")
      val _ = svg
        .selectAll("text.binary-tree__empty-placeholder")
        .attr("x", totalW / 2)
        .attr("y", totalH / 2 + 6)
      return ()
    else
      val _ = svg.selectAll("text.binary-tree__empty-placeholder").remove()

    // Defensive lookups — exit-pending elements briefly remain in the DOM
    // after their datum disappears from `layout` (the update-path
    // `svg.selectAll` picks them up before the exit() snap-remove fires);
    // if their attribute closures then try `layout(id)._1`, they throw
    // NoSuchElementException and the whole component unmounts. Returning a
    // safe fallback (0, 0) lets the transient frame render harmlessly until
    // the exit cleanup runs on the next tick.
    def cxAt(id: String): Double =
      layout.get(id).fold(0.0)(p => pixelX(p._1, slotCount))
    def cyAt(id: String): Double =
      layout.get(id).fold(0.0)(p => pixelY(p._2))

    // ── Edges ──────────────────────────────────────────────────────────────
    val edgeList: List[(String, String, String)] = reachable.values.toList.flatMap { n =>
      val l = n.left.filter(reachable.contains).map(c => (n.id, c, "left"))
      val r = n.right.filter(reachable.contains).map(c => (n.id, c, "right"))
      List(l, r).flatten
    }
    val edgeData: js.Array[js.Any] = edgeList.map { case (p, c, side) =>
      js.Dynamic
        .literal(parent = p, child = c, side = side)
        .asInstanceOf[js.Any]
    }.toJSArray
    val edgeKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"${dyn.parent.asInstanceOf[String]}->${dyn.child.asInstanceOf[String]}"
    val edgePathFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val p   = dyn.parent.asInstanceOf[String]
      val c   = dyn.child.asInstanceOf[String]
      val px  = cxAt(p)
      val py  = cyAt(p)
      val cx  = cxAt(c)
      val cy  = cyAt(c)
      // Start the line at the parent's bottom edge and end at the child's
      // top edge so the stroke doesn't tunnel through the circle bodies.
      val dx     = cx - px
      val dy     = cy - py
      val length = math.max(1e-6, math.sqrt(dx * dx + dy * dy))
      val ux     = dx / length
      val uy     = dy / length
      val sx     = px + ux * NodeRadius
      val sy     = py + uy * NodeRadius
      val ex     = cx - ux * NodeRadius
      val ey     = cy - uy * NodeRadius
      s"M $sx $sy L $ex $ey"

    val edgeSel = svg.selectAll("path.binary-tree__edge").data(edgeData, edgeKeyFn)
    val _ = edgeSel
      .enter()
      .append("path")
      .attr("class", "binary-tree__edge")
      .attr("fill", "none")
      .attr("d", edgePathFn)
      .attr("opacity", 0)
      .transition("bt-edge-fade-in")
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    // Snap-remove on exit (StackQueue's lesson): fading exits via .transition
    // schedules .remove() at the transition's end; a subsequent render that
    // fires before that end interrupts the transition and the remove never
    // runs, leaving stale elements in the DOM at opacity 0. They then also
    // get picked up by the update path's `selectAll` re-selection and produce
    // weird half-rendered ghosts. Snap removal keeps the entering elements'
    // fade-in (so the overall step transition still reads as motion) but
    // tears down vanishing elements cleanly.
    val _       = edgeSel.exit().remove()
    val edgeAll = svg.selectAll("path.binary-tree__edge")
    val _ = edgeAll
      .transition("bt-edge-path")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("d", edgePathFn)

    // ── Nodes ──────────────────────────────────────────────────────────────
    val nodeList = reachable.values.toList
    val nodeData: js.Array[js.Any] = nodeList.map { n =>
      val effectiveStyle = step.nodeStyles.getOrElse(n.id, n.style.getOrElse(""))
      js.Dynamic
        .literal(
          id = n.id,
          value = n.value,
          style = effectiveStyle,
          color = n.color.getOrElse(""),
          balanceFactor = n.balanceFactor.fold[js.Any](js.undefined)(_.asInstanceOf[js.Any]),
          priority = n.priority.fold[js.Any](js.undefined)(_.asInstanceOf[js.Any])
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val nodeKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].id
    val nodeTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val id = d.asInstanceOf[js.Dynamic].id.asInstanceOf[String]
      s"translate(${cxAt(id)}, ${cyAt(id)})"

    val nodeClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn   = d.asInstanceOf[js.Dynamic]
      val style = dyn.style.asInstanceOf[String]
      val color = dyn.color.asInstanceOf[String]
      val base  = "binary-tree__node-group"
      val withStyle =
        if style.nonEmpty then s"$base binary-tree__node-group--$style" else base
      val withColor =
        if spec.mode == ModeRbt && color.nonEmpty then
          s"$withStyle binary-tree__node-group--rbt-$color"
        else withStyle
      withColor

    val nodeSel = svg.selectAll("g.binary-tree__node-group").data(nodeData, nodeKeyFn)
    val nodeEnter = nodeSel
      .enter()
      .append("g")
      .attr("class", nodeClassFn)
      .attr("transform", nodeTransform)
      .attr("opacity", 0)

    val _ = nodeEnter
      .append("circle")
      .attr("class", "binary-tree__node-circle")
      .attr("r", NodeRadius)

    val _ = nodeEnter
      .append("text")
      .attr("class", "binary-tree__node-label")
      .attr("text-anchor", "middle")
      .attr("y", 5.0)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])

    // Balance-factor badge (avl only).
    if spec.mode == ModeAvl then
      val _ = nodeEnter
        .append("text")
        .attr("class", "binary-tree__node-bf")
        .attr("text-anchor", "middle")
        .attr("y", NodeRadius + 12.0)
        .text((
            (d, _) => {
              val dyn = d.asInstanceOf[js.Dynamic]
              val bf  = dyn.balanceFactor
              if js.isUndefined(bf) then "" else s"bf=${bf}"
            }
        ): js.Function2[js.Any, Int, js.Any])

    // Priority badge (treap only).
    if spec.mode == ModeTreap then
      val _ = nodeEnter
        .append("text")
        .attr("class", "binary-tree__node-priority")
        .attr("text-anchor", "middle")
        .attr("y", -NodeRadius - 6.0)
        .text((
            (d, _) => {
              val dyn = d.asInstanceOf[js.Dynamic]
              val p   = dyn.priority
              if js.isUndefined(p) then "" else s"p=${p}"
            }
        ): js.Function2[js.Any, Int, js.Any])

    val _ = nodeEnter
      .transition("bt-node-fade-in")
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)

    val _ = nodeSel.exit().remove()

    val nodeAll = svg.selectAll("g.binary-tree__node-group")
    val _       = nodeAll.attr("class", nodeClassFn)
    val _ = nodeAll
      .select("text.binary-tree__node-label")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    if spec.mode == ModeAvl then
      val _ = nodeAll
        .select("text.binary-tree__node-bf")
        .text((
            (d, _) => {
              val dyn = d.asInstanceOf[js.Dynamic]
              val bf  = dyn.balanceFactor
              if js.isUndefined(bf) then "" else s"bf=${bf}"
            }
        ): js.Function2[js.Any, Int, js.Any])
    if spec.mode == ModeTreap then
      val _ = nodeAll
        .select("text.binary-tree__node-priority")
        .text((
            (d, _) => {
              val dyn = d.asInstanceOf[js.Dynamic]
              val p   = dyn.priority
              if js.isUndefined(p) then "" else s"p=${p}"
            }
        ): js.Function2[js.Any, Int, js.Any])
    val _ = nodeAll
      .transition("bt-node-move")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("transform", nodeTransform)

    // ── Markers ────────────────────────────────────────────────────────────
    // Multiple markers may attach to the same node. Rank within the node so
    // labels stack vertically without overlap.
    val activeMarkers = step.markers.filter(m => reachable.contains(m.nodeId))
    val rankedMarkers = activeMarkers.zipWithIndex.map { case (m, idx) =>
      val rank = activeMarkers.take(idx).count(_.nodeId == m.nodeId)
      (m, rank)
    }
    val markerData: js.Array[js.Any] = rankedMarkers.map { case (m, rank) =>
      js.Dynamic
        .literal(
          name = m.name,
          nodeId = m.nodeId,
          rank = rank,
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      d.asInstanceOf[js.Dynamic].name
    val markerTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn  = d.asInstanceOf[js.Dynamic]
      val nid  = dyn.nodeId.asInstanceOf[String]
      val rank = dyn.rank.asInstanceOf[Int]
      val x    = cxAt(nid)
      val y    = cyAt(nid) - NodeRadius - 6.0 - rank * 13.0
      s"translate($x, $y)"
    val triangleD: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val rank      = dyn.rank.asInstanceOf[Int]
      if !canonical || rank != 0 then "" else "M -5 -8 L 5 -8 L 0 0 Z"
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val name      = dyn.name.asInstanceOf[String]
      if canonical then name else s"⚠ $name"

    val markerSel = svg.selectAll("g.binary-tree__marker").data(markerData, markerKeyFn)
    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "binary-tree__marker")
      .attr("transform", markerTransform)
      .attr("opacity", 0)
    val _ = markerEnter
      .append("path")
      .attr("class", "binary-tree__marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerEnter
      .append("text")
      .attr("class", "binary-tree__marker-label")
      .attr("text-anchor", "middle")
      .attr("y", -12.0)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _ = markerEnter
      .transition("bt-marker-fade-in")
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _         = markerSel.exit().remove()
    val markerAll = svg.selectAll("g.binary-tree__marker")
    val _ = markerAll
      .transition("bt-marker-move")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("transform", markerTransform)
    val _ = markerAll
      .select("path.binary-tree__marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerAll
      .select("text.binary-tree__marker-label")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)

    // ── Sidebar ─────────────────────────────────────────────────────────────
    renderSidebar(svg, spec, step, treeW, treeH, animate)

    // ── Output strip ───────────────────────────────────────────────────────
    renderOutputStrip(svg, step, totalW, treeH, animate)

  // ---------------------------------------------------------------------------
  // Sidebar renderer — vertical stack of frames to the right of the tree.
  // Frames are stacked with the FIRST frame at the top and the LAST frame
  // (most recently pushed) at the bottom, matching the recursion-stack
  // convention "older calls above, latest below".
  // ---------------------------------------------------------------------------

  private def renderSidebar(
      svg: D3.Selection,
      spec: Spec,
      step: Step,
      treeW: Double,
      treeH: Double,
      animate: Boolean
  ): Unit =
    spec.sidebar match
      case None =>
        // Tear down any leftover sidebar group from a prior render.
        val _ = svg.selectAll("g.binary-tree__sidebar-group").remove()
        val _ = svg.selectAll("text.binary-tree__sidebar-label").remove()
      case Some(sidebar) =>
        val fadeDur  = if animate then FadeMs else 0.0
        val moveDur  = if animate then TransitionDurationMs else 0.0
        val x        = treeW
        val labelY   = PaddingY + 14.0
        val framesY0 = PaddingY + SidebarLabelH + 4.0

        // Section label
        val labelText = sidebar.label.getOrElse(
          if sidebar.kind == SidebarIterStack then "Iterator stack" else "Recursion"
        )
        val labelData: js.Array[js.Any]                   = js.Array(labelText).asInstanceOf[js.Array[js.Any]]
        val labelKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "sidebar-label"
        val labelSel = svg.selectAll("text.binary-tree__sidebar-label").data(labelData, labelKeyFn)
        val _ = labelSel
          .enter()
          .append("text")
          .attr("class", "binary-tree__sidebar-label")
          .attr("text-anchor", "start")
          .attr("x", x + 10.0)
          .attr("y", labelY)
          .text(((d, _) => d): js.Function2[js.Any, Int, js.Any])
        val _ = svg
          .selectAll("text.binary-tree__sidebar-label")
          .attr("x", x + 10.0)
          .attr("y", labelY)
          .text(((d, _) => d): js.Function2[js.Any, Int, js.Any])
        val _ = labelSel.exit().remove()

        // Frames — keyed by ${depth}-${label} so a pushed frame at the same
        // label-text at a different stack depth enters as a new node rather
        // than re-keying onto the existing one.
        val framesIndexed = step.sidebarFrames.zipWithIndex
        val frameData: js.Array[js.Any] = framesIndexed.map { case (f, depth) =>
          js.Dynamic
            .literal(
              key = s"$depth-${f.label}",
              label = f.label,
              depth = depth,
              kind = f.kind.getOrElse("")
            )
            .asInstanceOf[js.Any]
        }.toJSArray
        val frameKeyFn: js.Function2[js.Any, Int, js.Any] =
          (d, _) => d.asInstanceOf[js.Dynamic].key
        val frameClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
          val kind = d.asInstanceOf[js.Dynamic].kind.asInstanceOf[String]
          val base = "binary-tree__sidebar-group"
          if kind.nonEmpty then s"$base binary-tree__sidebar-group--$kind" else base
        val frameTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
          val depth = d.asInstanceOf[js.Dynamic].depth.asInstanceOf[Int]
          val fy    = framesY0 + depth * (SidebarRowH + SidebarGap)
          s"translate($x, $fy)"

        val frameSel = svg.selectAll("g.binary-tree__sidebar-group").data(frameData, frameKeyFn)
        val frameEnter = frameSel
          .enter()
          .append("g")
          .attr("class", frameClassFn)
          .attr("transform", frameTransform)
          .attr("opacity", 0)
        val _ = frameEnter
          .append("rect")
          .attr("class", "binary-tree__sidebar-frame")
          .attr("x", 6.0)
          .attr("y", 0.0)
          .attr("width", SidebarWidth - 14.0)
          .attr("height", SidebarRowH)
          .attr("rx", 4)
        val _ = frameEnter
          .append("text")
          .attr("class", "binary-tree__sidebar-frame-label")
          .attr("x", 12.0)
          .attr("y", SidebarRowH / 2 + 4)
          .attr("text-anchor", "start")
          .text(((d, _) => d.asInstanceOf[js.Dynamic].label): js.Function2[js.Any, Int, js.Any])
        val _ = frameEnter
          .transition("bt-sidebar-fade-in")
          .duration(fadeDur)
          .ease(D3.easeCubicInOut)
          .attr("opacity", 1)
        val _        = frameSel.exit().remove()
        val frameAll = svg.selectAll("g.binary-tree__sidebar-group")
        val _        = frameAll.attr("class", frameClassFn)
        val _ = frameAll
          .select("text.binary-tree__sidebar-frame-label")
          .text(((d, _) => d.asInstanceOf[js.Dynamic].label): js.Function2[js.Any, Int, js.Any])
        val _ = frameAll
          .transition("bt-sidebar-move")
          .duration(moveDur)
          .ease(D3.easeCubicInOut)
          .attr("transform", frameTransform)

        // Suppress unused warning for treeH while keeping the parameter for
        // future "frames flow above the tree's vertical centre" alignment.
        val _ = treeH

  // ---------------------------------------------------------------------------
  // Output-strip renderer — horizontal row of cells below the tree showing
  // the running traversal output (e.g. inorder sequence). Cells are keyed by
  // their index so a newly-appended value enters with fade-in rather than
  // re-rendering the whole strip.
  // ---------------------------------------------------------------------------

  private def renderOutputStrip(
      svg: D3.Selection,
      step: Step,
      totalW: Double,
      treeH: Double,
      animate: Boolean
  ): Unit =
    if step.outputItems.isEmpty then
      val _ = svg.selectAll("g.binary-tree__output-group").remove()
      val _ = svg.selectAll("text.binary-tree__output-label").remove()
      return ()

    val fadeDur = if animate then FadeMs else 0.0
    val moveDur = if animate then TransitionDurationMs else 0.0
    val n       = step.outputItems.size
    val rowW    = n * OutputCellSize + (n - 1).max(0) * OutputCellGap
    val xStart  = (totalW - rowW) / 2.0
    val yLabel  = treeH + OutputStripGap
    val yCells  = yLabel + OutputLabelH

    val labelData: js.Array[js.Any]                   = js.Array("Output").asInstanceOf[js.Array[js.Any]]
    val labelKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "output-label"
    val labelSel = svg.selectAll("text.binary-tree__output-label").data(labelData, labelKeyFn)
    val _ = labelSel
      .enter()
      .append("text")
      .attr("class", "binary-tree__output-label")
      .attr("text-anchor", "middle")
      .text(((d, _) => d): js.Function2[js.Any, Int, js.Any])
    val _ = svg
      .selectAll("text.binary-tree__output-label")
      .attr("x", totalW / 2.0)
      .attr("y", yLabel + 12.0)
    val _ = labelSel.exit().remove()

    val cellData: js.Array[js.Any] = step.outputItems.zipWithIndex.map { case (v, i) =>
      js.Dynamic
        .literal(key = s"$i-$v", value = v, idx = i)
        .asInstanceOf[js.Any]
    }.toJSArray
    val cellKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      d.asInstanceOf[js.Dynamic].key
    val cellTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val i = d.asInstanceOf[js.Dynamic].idx.asInstanceOf[Int]
      s"translate(${xStart + i * (OutputCellSize + OutputCellGap)}, $yCells)"

    val cellSel = svg.selectAll("g.binary-tree__output-group").data(cellData, cellKeyFn)
    val cellEnter = cellSel
      .enter()
      .append("g")
      .attr("class", "binary-tree__output-group")
      .attr("transform", cellTransform)
      .attr("opacity", 0)
    val _ = cellEnter
      .append("rect")
      .attr("class", "binary-tree__output-cell")
      .attr("width", OutputCellSize)
      .attr("height", OutputCellSize)
      .attr("rx", 4)
    val _ = cellEnter
      .append("text")
      .attr("class", "binary-tree__output-text")
      .attr("text-anchor", "middle")
      .attr("x", OutputCellSize / 2)
      .attr("y", OutputCellSize / 2 + 5)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _ = cellEnter
      .transition("bt-output-fade-in")
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _       = cellSel.exit().remove()
    val cellAll = svg.selectAll("g.binary-tree__output-group")
    val _ = cellAll
      .transition("bt-output-move")
      .duration(moveDur)
      .ease(D3.easeCubicInOut)
      .attr("transform", cellTransform)
    val _ = cellAll
      .select("text.binary-tree__output-text")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])

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
              val svgEl   = ensureSvg(host, spec)
              val step    = spec.steps(index)
              val animate = hasRenderedRef.value
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
              <.p(^.className   := "d3-widget__error-title", "Binary-tree payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then Step(Nil, Map.empty, Nil, Nil, Nil, None, "No steps defined.")
              else spec.steps(idx)

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "binary-tree__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "binary-tree__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("binary-tree__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "binary-tree__button binary-tree__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("binary-tree__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("binary-tree__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "binary-tree__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("binary-tree__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "binary-tree__button binary-tree__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("binary-tree__button-icon"))
                  ),
                  <.span(
                    ^.className := "binary-tree__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"binary-tree binary-tree--${spec.mode} not-prose",
              spec.title
                .map(t => <.p(^.className := "binary-tree__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              <.div(
                ^.className := "binary-tree__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "binary-tree__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              controls
            )
      }
