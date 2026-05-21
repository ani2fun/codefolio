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
 * Trie stepper — eleventh widget in the D3 catalog and the last Arc 1 Tier 1 entry. Renders a character-edge
 * prefix tree and animates the three canonical operations — `insert`, `search`, `prefix-walk` — over the same
 * payload shape. Each step advances one character (or one structural event like `create` / `terminal` / `hit`
 * / `miss` / `prefix-highlight`).
 *
 * Node state splits into a PERSISTENT layer (visibility / terminal / in-prefix — once set, never overwritten)
 * and a TRANSIENT adornment layer (active / just-created / hit / miss — reset at the head of every
 * applyEvent), exactly matching the pattern lifted from dp-table. The split eliminates the terminal-status
 * preservation guard the decision-tree widget needed: a `visit` event that passes over a terminal node can't
 * strip the terminal marker because terminal is a separate orthogonal boolean, not a competing enum value.
 *
 * Layout is Reingold-Tilford over the FINAL-STATE trie topology — computed once at mount, stable across
 * steps. Hidden nodes simply aren't rendered, but their slot stays reserved so newly-created nodes appear at
 * their final position rather than shifting siblings around.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":     "Insert \"cat\" into empty trie",
 *   "operation": "insert",                       // "insert" | "search" | "prefix-walk"
 *   "word":      "cat",                          // metadata — labels the demo
 *
 *   // The complete final-state trie (all nodes/edges that exist at the END of the trace).
 *   // Per-step visibility is derived: every node NOT listed in a "create" event is visible
 *   // from step 0; "create" events reveal additional nodes mid-trace.
 *   "nodes": [
 *     {"id": "root", "label": "*"},
 *     {"id": "c"},
 *     {"id": "ca"},
 *     {"id": "cat", "isTerminal": true}
 *   ],
 *   "edges": [
 *     {"from": "root", "to": "c",   "label": "c"},
 *     {"from": "c",    "to": "ca",  "label": "a"},
 *     {"from": "ca",   "to": "cat", "label": "t"}
 *   ],
 *
 *   "events": [
 *     {"kind": "visit",    "nodeId": "root", "msg": "..."},
 *     {"kind": "create",   "nodeId": "c",    "char": "c", "msg": "..."},
 *     {"kind": "terminal", "nodeId": "cat",  "msg": "..."},
 *     {"kind": "hit",      "nodeId": "car",  "msg": "..."},
 *     {"kind": "miss",     "nodeId": "ca",   "msg": "..."},
 *     {"kind": "prefix-highlight", "nodeId": "ca", "msg": "..."}
 *   ]
 * }
 * }}}
 *
 * The widget shares the keyed `<g>` selection pattern with [[LinkedList]], the Reingold-Tilford
 * sibling-spread layout with [[DecisionTree]] and [[DpTable]]'s recursion tree, and the event-tape replay
 * shape with every other Arc 1 widget. No payload-supplied markers — node status is computed.
 */
object Trie:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose (ADR-0006).
  // ---------------------------------------------------------------------------

  // One node. `label` is optional inner text (typically "*" for root, empty
  // elsewhere — the edge carries the character). `isTerminal` reflects the
  // FINAL state; mid-trace terminal-ness is computed from the event tape.
  final case class NodeDef(id: String, label: Option[String], isTerminal: Boolean)

  // One edge. `label` is the single character (or short string for radix
  // variants) the edge carries.
  final case class EdgeDef(from: String, to: String, label: String)

  // One event in the tape.
  //   - "visit"            : the cursor steps onto an already-visible node;
  //                          `char` (if present) accumulates into `consumed`.
  //   - "create"           : reveal a node (toggle from hidden → visible);
  //                          set just-created + active; `char` accumulates.
  //   - "terminal"         : mark `nodeId` as end-of-word (terminal ring fades
  //                          in). Persistent — survives all subsequent events.
  //   - "hit"              : search success flash on `nodeId`.
  //   - "miss"             : search failure flash on `nodeId`.
  //   - "prefix-highlight" : mark `nodeId` and its entire descendant subtree
  //                          as `in-prefix` (persistent halo). Used by
  //                          `prefix-walk` to call out "all words sharing
  //                          this prefix".
  final case class Event(kind: String, nodeId: String, char: Option[String], msg: String)

  final case class Spec(
      operation: String, // "insert" | "search" | "prefix-walk"
      word: Option[String],
      nodes: List[NodeDef],
      edges: List[EdgeDef],
      events: List[Event],
      title: Option[String]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Closed-set constants. Unknown values collapse to safe defaults at parse
  // time so a typo never crashes the chapter.
  // ---------------------------------------------------------------------------

  private val OpInsert     = "insert"
  private val OpSearch     = "search"
  private val OpPrefixWalk = "prefix-walk"
  private val Operations   = Set(OpInsert, OpSearch, OpPrefixWalk)

  private val EventVisit           = "visit"
  private val EventCreate          = "create"
  private val EventTerminal        = "terminal"
  private val EventHit             = "hit"
  private val EventMiss            = "miss"
  private val EventPrefixHighlight = "prefix-highlight"

  private val EventKinds =
    Set(EventVisit, EventCreate, EventTerminal, EventHit, EventMiss, EventPrefixHighlight)

  // ---------------------------------------------------------------------------
  // Layout constants — sized so a 4-level trie (~6 nodes, depth 4) fits in a
  // prose column without horizontal scroll. SlotSize is the horizontal pitch
  // per leaf; LevelHeight is the vertical drop per depth.
  // ---------------------------------------------------------------------------

  private val NodeRadius         = 16.0
  private val TerminalRingRadius = 21.0  // outer ring drawn outside the node circle
  private val SlotSize           = 86.0  // horizontal slot per leaf
  private val LevelHeight        = 64.0  // vertical pitch per tree depth
  private val PaddingX           = 24.0
  private val PaddingY           = 22.0
  private val ActiveCharDy       = -30.0 // bias the floating char pill above the active node
  private val StepDelayMs        = 1700
  private val FadeMs             = 350.0
  private val SvgNs              = "http://www.w3.org/2000/svg"

  // ---------------------------------------------------------------------------
  // Parsing — each parser is small + total; PayloadDecoder.run collapses any
  // thrown exception into Left(message) and the renderer shows an inline error.
  // ---------------------------------------------------------------------------

  private def parseNode(d: js.Dynamic): NodeDef =
    NodeDef(
      id = d.string("id"),
      label = d.optString("label"),
      isTerminal = d.bool("isTerminal", false)
    )

  private def parseEdge(d: js.Dynamic): EdgeDef =
    EdgeDef(
      from = d.string("from"),
      to = d.string("to"),
      label = d.string("label")
    )

  private def parseEvent(d: js.Dynamic): Event =
    val rawKind = d.optString("kind").getOrElse(EventVisit)
    val kind    = if EventKinds.contains(rawKind) then rawKind else EventVisit
    Event(
      kind = kind,
      nodeId = d.string("nodeId"),
      char = d.optString("char"),
      msg = d.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawOp     = d.optString("operation").getOrElse(OpInsert)
      val operation = if Operations.contains(rawOp) then rawOp else OpInsert
      val nodes     = d.dynList("nodes").map(parseNode).filter(_.id.nonEmpty)
      if nodes.isEmpty then throw PayloadDecoder.invalid("nodes must be non-empty")
      val edges  = d.dynList("edges").map(parseEdge).filter(e => e.from.nonEmpty && e.to.nonEmpty)
      val events = d.dynList("events").map(parseEvent)
      if events.isEmpty then throw PayloadDecoder.invalid("events must be non-empty")
      Spec(
        operation = operation,
        word = d.optString("word"),
        nodes = nodes,
        edges = edges,
        events = events,
        title = d.optString("title")
      )
    }

  // ---------------------------------------------------------------------------
  // Per-step state — computed by replaying events [0..stepIdx].
  //
  // PERSISTENT fields are set once and never overwritten:
  //   - visible  : nodes drawn this step (initial set + `create` accumulation)
  //   - terminal : nodes carrying the end-of-word ring (initial set from
  //                `isTerminal` for pre-existing nodes + `terminal` events for
  //                created nodes)
  //   - inPrefix : nodes in a prefix-walk's subtree (set by
  //                `prefix-highlight`; cumulative if multiple highlights fire)
  //   - consumed : characters walked so far (concatenation of `char` fields
  //                from `visit` + `create` events)
  //
  // TRANSIENT fields are reset at the head of every applyEvent:
  //   - active       : cursor position this step (the node "we're at")
  //   - justCreated  : node revealed in THIS step (drives a one-step new flash)
  //   - flashHit     : search-success flash this step
  //   - flashMiss    : search-failure flash this step
  //   - activeChar   : character pill floating near the active node this step
  //
  // The split eliminates the terminal-status preservation guard the
  // decision-tree widget needed. A `visit` event that lands on a terminal
  // node only mutates the transient layer; the persistent `terminal` set
  // is untouched, so the terminal ring stays drawn.
  // ---------------------------------------------------------------------------

  final case class WalkState(
      visible: Set[String],
      terminal: Set[String],
      inPrefix: Set[String],
      active: Option[String],
      justCreated: Option[String],
      flashHit: Option[String],
      flashMiss: Option[String],
      activeChar: Option[String],
      consumed: String
  )

  // Children-of map — built from `edges` (NOT from a node `parent` field; the
  // schema has no parent pointer). Preserves edge-declaration order so sibling
  // positions are stable.
  private def buildChildrenOf(spec: Spec): Map[String, List[String]] =
    val accum = mutable.Map.empty[String, mutable.ArrayBuffer[String]]
    for e <- spec.edges do
      accum.getOrElseUpdate(e.from, mutable.ArrayBuffer.empty[String]) += e.to
    accum.view.mapValues(_.toList).toMap

  // Parent map — `to → from`. The trie is single-parent (it's a tree), so a
  // simple Map suffices.
  private def buildParentOf(spec: Spec): Map[String, String] =
    spec.edges.map(e => e.to -> e.from).toMap

  // All descendants of `rootId` including `rootId` itself. Used by
  // `prefix-highlight` to mark a whole subtree.
  private def subtreeIds(rootId: String, childrenOf: Map[String, List[String]]): List[String] =
    val out = mutable.ArrayBuffer.empty[String]
    def walk(id: String): Unit =
      out += id
      childrenOf.getOrElse(id, Nil).foreach(walk)
    walk(rootId)
    out.toList

  // Initially-visible nodes: every node EXCEPT those that will be `create`d
  // later in the trace. The root + any "already-stored" branches show from
  // step 0; nodes for the word being inserted appear as their `create` events
  // fire.
  private def emptyWalkState(spec: Spec): WalkState =
    val createdIds       = spec.events.iterator.collect { case e if e.kind == EventCreate => e.nodeId }.toSet
    val initiallyVisible = spec.nodes.iterator.map(_.id).filterNot(createdIds.contains).toSet
    // Pre-existing terminal nodes start in the terminal set. Nodes whose
    // `isTerminal: true` matches a node that will be `create`d later only
    // become terminal when their `terminal` event fires (the spec convention
    // pairs every `create` of an end-word node with a following `terminal`).
    val initialTerminal =
      spec.nodes.iterator.filter(n => n.isTerminal && initiallyVisible.contains(n.id)).map(_.id).toSet
    WalkState(
      visible = initiallyVisible,
      terminal = initialTerminal,
      inPrefix = Set.empty,
      active = None,
      justCreated = None,
      flashHit = None,
      flashMiss = None,
      activeChar = None,
      consumed = ""
    )

  // Reset transient adornments. Called at the head of every applyEvent so a
  // visit / create / terminal / hit / miss / prefix-highlight all start from
  // a clean transient state.
  private def clearTransient(state: WalkState): WalkState =
    state.copy(
      active = None,
      justCreated = None,
      flashHit = None,
      flashMiss = None,
      activeChar = None
    )

  private def applyEvent(
      state: WalkState,
      event: Event,
      childrenOf: Map[String, List[String]]
  ): WalkState =
    val base = clearTransient(state)
    event.kind match
      case EventVisit =>
        base.copy(
          active = Some(event.nodeId),
          activeChar = event.char,
          consumed = base.consumed + event.char.getOrElse("")
        )
      case EventCreate =>
        base.copy(
          visible = base.visible + event.nodeId,
          active = Some(event.nodeId),
          justCreated = Some(event.nodeId),
          activeChar = event.char,
          consumed = base.consumed + event.char.getOrElse("")
        )
      case EventTerminal =>
        // Persistent: add to terminal set. The active cursor stays on the
        // node so the reader can see WHICH node just got the ring.
        base.copy(
          terminal = base.terminal + event.nodeId,
          active = Some(event.nodeId)
        )
      case EventHit =>
        base.copy(
          active = Some(event.nodeId),
          flashHit = Some(event.nodeId)
        )
      case EventMiss =>
        base.copy(
          active = Some(event.nodeId),
          flashMiss = Some(event.nodeId)
        )
      case EventPrefixHighlight =>
        val ids = subtreeIds(event.nodeId, childrenOf)
        base.copy(
          inPrefix = base.inPrefix ++ ids,
          active = Some(event.nodeId)
        )
      case _ => base

  private def computeState(
      spec: Spec,
      stepIdx: Int,
      childrenOf: Map[String, List[String]]
  ): WalkState =
    var s     = emptyWalkState(spec)
    val bound = math.min(stepIdx, spec.events.size - 1)
    for i <- 0 to bound do s = applyEvent(s, spec.events(i), childrenOf)
    s

  // ---------------------------------------------------------------------------
  // Layout — Reingold-Tilford-flavoured: leaves take the next free slot;
  // internal nodes sit at the midpoint of their children's slot range. Each
  // node's `(slot, depth)` is stable across steps (topology never changes
  // mid-trace). Output: (id → (xSlot, depth)) + maxDepth + slotCount.
  //
  // The layout is computed over the FINAL-STATE topology — every node in
  // `spec.nodes`, not just those visible at the current step — so the trie
  // grows in stable positions as `create` events reveal nodes one by one.
  // ---------------------------------------------------------------------------

  private def computeLayout(
      spec: Spec,
      childrenOf: Map[String, List[String]],
      parentOf: Map[String, String]
  ): (Map[String, (Double, Int)], Int, Double) =
    val byId      = spec.nodes.map(n => n.id -> n).toMap
    val roots     = spec.nodes.filter(n => !parentOf.get(n.id).exists(byId.contains)).map(_.id)
    val positions = mutable.Map.empty[String, (Double, Int)]
    val nextSlot  = Array(0.0)
    var maxDepth  = 0
    def visit(id: String, depth: Int): Unit =
      if depth > maxDepth then maxDepth = depth
      val children = childrenOf.getOrElse(id, Nil)
      if children.isEmpty then
        val mySlot = nextSlot(0)
        nextSlot(0) += 1.0
        positions(id) = (mySlot, depth)
      else
        children.foreach(visit(_, depth + 1))
        val firstSlot = positions(children.head)._1
        val lastSlot  = positions(children.last)._1
        positions(id) = ((firstSlot + lastSlot) / 2.0, depth)
    roots.foreach(visit(_, 0))
    val slotCount = nextSlot(0).max(1.0)
    (positions.toMap, maxDepth, slotCount)

  // ---------------------------------------------------------------------------
  // SVG bootstrap — first paint only. viewBox + width/height refreshed per
  // render because the canvas size depends on the layout (computed once at
  // mount, so this is effectively static after the first call — but cheap
  // to re-set).
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", "trie__svg")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Trie (${spec.operation})"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", "0 0 400 200")
      svg.setAttribute("width", "400")
      svg.setAttribute("height", "200")
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Per-step render — idempotent. Edges keyed by `${from}->${to}`; edge labels
  // keyed by the same; nodes keyed by id; terminal rings keyed by id;
  // active-char pill is a singleton at most one per step.
  // ---------------------------------------------------------------------------

  private def renderStep(
      svgEl: dom.Element,
      spec: Spec,
      stepIdx: Int,
      animate: Boolean,
      childrenOf: Map[String, List[String]],
      positions: Map[String, (Double, Int)],
      maxDepth: Int,
      slotCount: Double
  ): Unit =
    val svg     = D3.select(svgEl)
    val state   = computeState(spec, stepIdx, childrenOf)
    val fadeDur = if animate then FadeMs else 0.0

    val totalW = math.max(1.0, slotCount) * SlotSize + PaddingX * 2
    val totalH = (maxDepth + 1).toDouble * LevelHeight + PaddingY * 2

    val _ = svg.attr("viewBox", s"0 0 $totalW $totalH")
    val _ = svg.attr("width", totalW.toString)
    val _ = svg.attr("height", totalH.toString)

    def nodeCx(id: String): Double = PaddingX + positions(id)._1 * SlotSize + SlotSize / 2.0
    def nodeCy(id: String): Double = PaddingY + positions(id)._2.toDouble * LevelHeight + NodeRadius

    renderEdges(svg, spec, state, nodeCx, nodeCy, fadeDur)
    renderEdgeLabels(svg, spec, state, nodeCx, nodeCy, fadeDur)
    renderNodes(svg, spec, state, nodeCx, nodeCy, fadeDur)
    renderActiveChar(svg, state, nodeCx, nodeCy, fadeDur)

  // Visibility predicate — an edge shows iff BOTH endpoints are visible.
  private def edgeVisible(state: WalkState, e: EdgeDef): Boolean =
    state.visible.contains(e.from) && state.visible.contains(e.to)

  // ── Edges ─────────────────────────────────────────────────────────────────
  // Drawn first so node circles sit on top. Status comes from the CHILD: if
  // the child is `active` this step, the edge highlights as "just walked";
  // if the child is `in-prefix`, the edge picks up the prefix halo class.

  private def renderEdges(
      svg: D3.Selection,
      spec: Spec,
      state: WalkState,
      nodeCx: String => Double,
      nodeCy: String => Double,
      fadeDur: Double
  ): Unit =
    val visibleEdges = spec.edges.filter(edgeVisible(state, _))
    val data: js.Array[js.Any] = visibleEdges.map { e =>
      val px       = nodeCx(e.from)
      val py       = nodeCy(e.from)
      val cx       = nodeCx(e.to)
      val cy       = nodeCy(e.to)
      val isActive = state.active.contains(e.to)
      val inPrefix = state.inPrefix.contains(e.to) && state.inPrefix.contains(e.from)
      val status =
        if isActive then "active"
        else if inPrefix then "in-prefix"
        else "default"
      js.Dynamic
        .literal(
          id = s"${e.from}->${e.to}",
          d = s"M $px $py L $cx $cy",
          status = status
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val dFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].d
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val s = d.asInstanceOf[js.Dynamic].status.asInstanceOf[String]
      s"trie__edge trie__edge--$s"
    val sel = svg.selectAll("path.trie__edge").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", classFn)
      .attr("d", dFn)
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val all = svg.selectAll("path.trie__edge")
    val _   = all.attr("class", classFn).attr("d", dFn).attr("opacity", 1)
    val _   = sel.exit().remove()

  // ── Edge labels ───────────────────────────────────────────────────────────
  // Single character per edge (or short string for radix tries). Rendered at
  // the midpoint with a matching-bg stroke halo so the glyph cuts cleanly
  // across the edge line (memory hint: paint-order: stroke fill).

  private def renderEdgeLabels(
      svg: D3.Selection,
      spec: Spec,
      state: WalkState,
      nodeCx: String => Double,
      nodeCy: String => Double,
      fadeDur: Double
  ): Unit =
    val visibleEdges = spec.edges.filter(edgeVisible(state, _))
    val data: js.Array[js.Any] = visibleEdges.map { e =>
      val midX     = (nodeCx(e.from) + nodeCx(e.to)) / 2.0
      val midY     = (nodeCy(e.from) + nodeCy(e.to)) / 2.0
      val isActive = state.active.contains(e.to)
      val inPrefix = state.inPrefix.contains(e.to) && state.inPrefix.contains(e.from)
      val status =
        if isActive then "active"
        else if inPrefix then "in-prefix"
        else "default"
      js.Dynamic
        .literal(
          id = s"${e.from}->${e.to}",
          text = e.label,
          x = midX,
          y = midY,
          status = status
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"el-${d.asInstanceOf[js.Dynamic].id.asInstanceOf[String]}"
    val xFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].x
    val yFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].y
    val textFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val s = d.asInstanceOf[js.Dynamic].status.asInstanceOf[String]
      s"trie__edge-label trie__edge-label--$s"
    val sel = svg.selectAll("text.trie__edge-label").data(data, keyFn)
    val _ = sel
      .enter()
      .append("text")
      .attr("class", classFn)
      .attr("text-anchor", "middle")
      .attr("x", xFn)
      .attr("y", yFn)
      .text(textFn)
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val all = svg.selectAll("text.trie__edge-label")
    val _   = all.attr("class", classFn).attr("x", xFn).attr("y", yFn).text(textFn).attr("opacity", 1)
    val _   = sel.exit().remove()

  // ── Nodes ─────────────────────────────────────────────────────────────────
  // Group keyed by node id. Each `<g>` carries the persistent status + any
  // transient adornments as space-separated modifier classes (pattern #11).
  // Children: a base `<circle>` (filled, status-coloured), an outer terminal
  // ring `<circle>` shown only when `--terminal`, and an optional inner
  // `<text>` label (typically just for root "*").

  private def renderNodes(
      svg: D3.Selection,
      spec: Spec,
      state: WalkState,
      nodeCx: String => Double,
      nodeCy: String => Double,
      fadeDur: Double
  ): Unit =
    val visibleNodes = spec.nodes.filter(n => state.visible.contains(n.id))
    val data: js.Array[js.Any] = visibleNodes.map { n =>
      val cx          = nodeCx(n.id)
      val cy          = nodeCy(n.id)
      val isTerminal  = state.terminal.contains(n.id)
      val inPrefix    = state.inPrefix.contains(n.id)
      val isActive    = state.active.contains(n.id)
      val justCreated = state.justCreated.contains(n.id)
      val isHit       = state.flashHit.contains(n.id)
      val isMiss      = state.flashMiss.contains(n.id)
      // Persistent status: in-prefix wins over default; terminal is layered
      // ON TOP via its own modifier (terminal + in-prefix can co-exist).
      val persistent = if inPrefix then "in-prefix" else "default"
      js.Dynamic
        .literal(
          id = n.id,
          label = n.label.getOrElse(""),
          persistent = persistent,
          isTerminal = isTerminal,
          isActive = isActive,
          justCreated = justCreated,
          isHit = isHit,
          isMiss = isMiss,
          cx = cx,
          cy = cy
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val cx  = dyn.cx.asInstanceOf[Double]
      val cy  = dyn.cy.asInstanceOf[Double]
      s"translate($cx, $cy)"
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn        = d.asInstanceOf[js.Dynamic]
      val persistent = dyn.persistent.asInstanceOf[String]
      val isTerminal = dyn.isTerminal.asInstanceOf[Boolean]
      val isActive   = dyn.isActive.asInstanceOf[Boolean]
      val newFlag    = dyn.justCreated.asInstanceOf[Boolean]
      val isHit      = dyn.isHit.asInstanceOf[Boolean]
      val isMiss     = dyn.isMiss.asInstanceOf[Boolean]
      val sb         = new StringBuilder("trie__node trie__node--").append(persistent)
      if isTerminal then sb.append(" trie__node--terminal")
      if isActive then sb.append(" trie__node--active")
      if newFlag then sb.append(" trie__node--new")
      if isHit then sb.append(" trie__node--hit")
      if isMiss then sb.append(" trie__node--miss")
      sb.toString
    val labelFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].label
    val sel                                        = svg.selectAll("g.trie__node").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", classFn)
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("circle")
      .attr("class", "trie__terminal-ring")
      .attr("r", TerminalRingRadius)
      .attr("cx", 0)
      .attr("cy", 0)
    val _ = enter
      .append("circle")
      .attr("class", "trie__node-circle")
      .attr("r", NodeRadius)
      .attr("cx", 0)
      .attr("cy", 0)
    val _ = enter
      .append("text")
      .attr("class", "trie__node-label")
      .attr("text-anchor", "middle")
      .attr("x", 0)
      .attr("y", 4)
      .text(labelFn)
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.trie__node")
    val _   = all.attr("class", classFn).attr("transform", transformFn).attr("opacity", 1)
    val _   = all.select("text.trie__node-label").text(labelFn)

  // ── Active-character pill (transient) ─────────────────────────────────────
  // Floating `<g.trie__active-char>` rendered near the active node showing the
  // character being processed this step. D3's exit() automatically removes it
  // on a step where `activeChar` is empty.

  private def renderActiveChar(
      svg: D3.Selection,
      state: WalkState,
      nodeCx: String => Double,
      nodeCy: String => Double,
      fadeDur: Double
  ): Unit =
    val data: js.Array[js.Any] = (state.active, state.activeChar) match
      case (Some(id), Some(ch)) if ch.nonEmpty =>
        val cx = nodeCx(id)
        val cy = nodeCy(id) + ActiveCharDy
        js.Array(
          js.Dynamic.literal(id = s"$id-$ch", text = ch, x = cx, y = cy).asInstanceOf[js.Any]
        )
      case _ => js.Array[js.Any]()
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val textFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val sel                                       = svg.selectAll("g.trie__active-char").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "trie__active-char")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("rect")
      .attr("class", "trie__active-char-rect")
      .attr("rx", 5)
      .attr("x", -10)
      .attr("y", -10)
      .attr("width", 20)
      .attr("height", 18)
    val _ = enter
      .append("text")
      .attr("class", "trie__active-char-text")
      .attr("text-anchor", "middle")
      .attr("x", 0)
      .attr("y", 3)
      .text(textFn)
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val all = svg.selectAll("g.trie__active-char")
    val _   = all.attr("transform", transformFn).attr("opacity", 1)
    val _   = all.select("text.trie__active-char-text").text(textFn)
    val _   = sel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component — React owns the host div; D3 owns the SVG inside it. Operation
  // badge + consumed-prefix readout + caption render as HTML siblings.
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .customBy { (_, specM) =>
        val stepCount = specM.value.toOption.fold(0)(_.events.size)
        Stepper.hook(Stepper.Input(stepCount, StepDelayMs.toDouble))
      }
      .useRefToVdom[dom.html.Element]
      .useRefBy(_ => false) // hasRendered (mutable; avoids re-render cycle)
      .useEffectWithDepsBy((_, specM, stepper, _, _) =>
        (specM.value.toOption.fold(0)(_.events.size), stepper.index)
      ) { (_, specM, _, hostRef, hasRenderedRef) => (_, index) =>
        specM.value.toOption.filter(s => s.events.nonEmpty && s.nodes.nonEmpty) match
          case Some(spec) =>
            hostRef.foreach { host =>
              val svgEl                            = ensureSvg(host, spec)
              val animate                          = hasRenderedRef.value
              val childrenOf                       = buildChildrenOf(spec)
              val parentOf                         = buildParentOf(spec)
              val (positions, maxDepth, slotCount) = computeLayout(spec, childrenOf, parentOf)
              renderStep(svgEl, spec, index, animate, childrenOf, positions, maxDepth, slotCount)
              if !hasRenderedRef.value then hasRenderedRef.value = true
            }
          case None => Callback.empty
      }
      .render { (_, specM, stepper, hostRef, _) =>
        specM.value match
          case Left(err) =>
            <.div(
              ^.className := "d3-widget__error",
              <.p(^.className   := "d3-widget__error-title", "Trie payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.events.size
            val idx   = stepper.index
            val currentEvent =
              if count == 0 then Event(EventVisit, "", None, "No events defined.")
              else spec.events(idx)
            val childrenOf = buildChildrenOf(spec)
            val state      = computeState(spec, idx, childrenOf)

            val opBadge: VdomNode =
              <.div(
                ^.className := "trie__operation-row",
                <.span(
                  ^.className := s"trie__operation-badge trie__operation-badge--${spec.operation}",
                  spec.operation
                ),
                spec.word
                  .map[VdomNode] { w =>
                    <.code(
                      ^.className := "trie__operation-word",
                      "\"",
                      w,
                      "\""
                    )
                  }
                  .getOrElse(EmptyVdom)
              )

            // Consumed-prefix readout — what the cursor has walked so far,
            // glyph by glyph. Min-height preserves layout while empty.
            val consumed: VdomNode =
              <.p(
                ^.className := "trie__consumed",
                <.span(^.className := "trie__consumed-label", "consumed:"),
                " ",
                <.code(
                  ^.className := "trie__consumed-value",
                  if state.consumed.isEmpty then " " else state.consumed
                )
              )

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "trie__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "trie__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("trie__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "trie__button trie__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("trie__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("trie__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "trie__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("trie__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "trie__button trie__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("trie__button-icon"))
                  ),
                  <.span(
                    ^.className := "trie__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"trie trie--${spec.operation} not-prose",
              spec.title
                .map(t => <.p(^.className := "trie__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              opBadge,
              <.div(
                ^.className := "trie__canvas"
              ).withRef(hostRef),
              consumed,
              <.p(
                ^.className := "trie__caption",
                ^.aria.live := "polite",
                if currentEvent.msg.nonEmpty then currentEvent.msg else " "
              ),
              controls
            )
      }
