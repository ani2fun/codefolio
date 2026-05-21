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
 * Call-stack stepper — eighth widget in the D3 catalog. Animates a function-call timeline as a tape of `call`
 * / `return` / `update` / `pause` events, with two layout projections of the same underlying data:
 *
 *   - `mode:"stack"` — the canonical bottom-up activation-frame ladder. New frames push on top; returns pop.
 *     Each frame shows its function name, any locals, and (on return) the value handed back to the caller. A
 *     stack pointer arrow tracks the active (top) frame. Optional `stackLimit` + `overflowAt` paint a "stack
 *     memory" chrome with a red overflow zone — the canonical visualisation for the source's stack-overflow
 *     chapter.
 *
 *   - `mode:"recursion-tree"` — the SAME call graph rendered as a top-down tree (root = top-level call;
 *     children = sub-calls). Frames are positioned via depth-first traversal with siblings spread
 *     horizontally at the same depth. Return values appear as labels on the edge from child to parent and
 *     stay drawn once produced — the reader sees the complete return graph by the end of the trace. Popped
 *     frames remain visible (dimmed) so the structure of the decomposition is preserved as a whole.
 *
 * The underlying data is identical across modes — a list of [[FrameDef]] + an [[Event]] tape — and the
 * renderer computes a per-step [[StackState]] by replaying events `[0..stepIdx]`. The visible frames in stack
 * mode are derived from the live stack; in recursion-tree mode every called frame stays visible (the tree
 * topology is the whole point).
 *
 * Per-event step semantics: each event emits one widget step with its own `msg`. `update` events change
 * locals on a frame without pushing or popping; `pause` events emit a beat with no state change (useful for
 * narrative pauses).
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":      "sum(3) push and pop",
 *   "mode":       "stack",                                 // "stack" | "recursion-tree"
 *   "stackLimit": 5,                                       // optional: visible slot count chrome
 *   "overflowAt": 5,                                       // optional: depth where frames go red
 *   "frameDefs": [
 *     {
 *       "id":     "sum-3",
 *       "name":   "sum(3)",
 *       "locals": [{"name": "n", "value": "3"}],
 *       "parent": "sum-3"                                  // recursion-tree mode only
 *     }
 *   ],
 *   "events": [
 *     {"kind": "call",   "frameId": "sum-3", "msg": "Push sum(3)"},
 *     {"kind": "update", "frameId": "sum-3", "locals": [{"name": "n", "value": "3"}], "msg": "n=3"},
 *     {"kind": "return", "frameId": "sum-3", "value": "6", "msg": "Return 6"},
 *     {"kind": "pause",  "msg": "Wait — note the stack is empty now."}
 *   ]
 * }
 * }}}
 *
 * Marker names (closed canon — see [[MarkerCanon]]): `base` (blue, bottom-of-stack), `caller` (slate, frame
 * below the top), `callee` (emerald, frame just pushed), plus the shared `top` family. Frame status (active /
 * waiting / returning / popped / overflow) is COMPUTED from the stack state, not author-supplied — so authors
 * don't need to specify it.
 */
object CallStack:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose (ADR-0006).
  // ---------------------------------------------------------------------------

  // One local variable on a frame — both `name` and `value` are strings (no
  // typed value column; the source diagrams use the value as displayed text).
  final case class Local(name: String, value: String)

  // Frame definition — the static description of a frame that may be pushed
  // at some point during the event tape. `parent` only meaningful in
  // recursion-tree mode (defines tree edges); ignored in stack mode.
  final case class FrameDef(
      id: String,
      name: String,
      locals: List[Local],
      parent: Option[String]
  )

  // A single event in the tape. `kind` is the closed set { "call", "return",
  // "update", "pause" }; the renderer's event-applier dispatches on it.
  // - `call`   : push frameId onto the stack
  // - `return` : pop the top frame (must match frameId); `value` is the
  //              returned value rendered on the popped frame's slot for one
  //              step, then on the edge to parent in tree mode
  // - `update` : replace locals on frameId without push/pop
  // - `pause`  : noop — emits a step beat for narrative pause
  final case class Event(
      kind: String,
      frameId: Option[String],
      value: Option[String],
      locals: List[Local],
      msg: String
  )

  final case class Spec(
      mode: String,
      frameDefs: List[FrameDef],
      events: List[Event],
      stackLimit: Option[Int],
      overflowAt: Option[Int],
      title: Option[String]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Mode + event-kind constants. Unknown values collapse to safe defaults at
  // parse time so a typo never crashes the chapter.
  // ---------------------------------------------------------------------------

  private val ModeStack         = "stack"
  private val ModeRecursionTree = "recursion-tree"
  private val Modes             = Set(ModeStack, ModeRecursionTree)

  private val EventCall   = "call"
  private val EventReturn = "return"
  private val EventUpdate = "update"
  private val EventPause  = "pause"
  private val EventKinds  = Set(EventCall, EventReturn, EventUpdate, EventPause)

  // ---------------------------------------------------------------------------
  // Layout constants — sized so a 5–7 frame stack fits a prose column without
  // horizontal scroll, and a 9–11 node recursion tree fits without crowding.
  // ---------------------------------------------------------------------------

  // Stack-mode frame box. Height is dynamic (baseHeight + perLocalHeight * locals).
  private val StackFrameWidth  = 220.0
  private val StackFrameBaseH  = 36.0
  private val StackFrameLocalH = 18.0
  private val StackFrameGap    = 6.0
  private val StackPointerW    = 22.0 // arrow glyph horizontal extent
  private val StackPointerGap  = 8.0  // gap between frame and pointer
  private val StackMemoryGap   = 14.0 // gap inside stack memory chrome
  // Recursion-tree-mode node box. Fixed width/height; locals shown only in
  // stack mode (would overcrowd a tree).
  private val TreeNodeWidth      = 110.0
  private val TreeNodeHeight     = 30.0
  private val TreeNodeGap        = 56.0 // horizontal gap between sibling subtrees (slot width)
  private val TreeLevelH         = 72.0 // vertical gap between depth levels
  private val TreeReturnLabelGap = 6.0
  // Shared chrome.
  private val PaddingX             = 16.0
  private val PaddingY             = 14.0
  private val StepDelayMs          = 1500
  private val TransitionDurationMs = 450.0
  private val FadeMs               = 350.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // ---------------------------------------------------------------------------
  // Parsing — each parser is small + total; PayloadDecoder.run collapses any
  // thrown exception into Left(message) and the renderer shows an inline error.
  // ---------------------------------------------------------------------------

  private def parseLocal(d: js.Dynamic): Local =
    Local(name = d.string("name"), value = d.string("value"))

  private def parseLocals(d: js.Dynamic, field: String): List[Local] =
    d.selectDynamic(field)
      .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
      .toOption
      .fold(List.empty[Local])(_.toList.map(parseLocal).filter(_.name.nonEmpty))

  private def parseFrameDef(d: js.Dynamic): FrameDef =
    FrameDef(
      id = d.string("id"),
      name = d.string("name"),
      locals = parseLocals(d, "locals"),
      parent = d.optString("parent")
    )

  private def parseEvent(d: js.Dynamic): Event =
    val rawKind = d.optString("kind").getOrElse(EventPause)
    val kind    = if EventKinds.contains(rawKind) then rawKind else EventPause
    Event(
      kind = kind,
      frameId = d.optString("frameId"),
      value = d.optString("value"),
      locals = parseLocals(d, "locals"),
      msg = d.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawMode = d.optString("mode").getOrElse(ModeStack)
      val mode    = if Modes.contains(rawMode) then rawMode else ModeStack
      val frames  = d.dynList("frameDefs").map(parseFrameDef).filter(_.id.nonEmpty)
      val events  = d.dynList("events").map(parseEvent)
      if events.isEmpty then throw PayloadDecoder.invalid("events must be non-empty")
      Spec(
        mode = mode,
        frameDefs = frames,
        events = events,
        stackLimit = d.optInt("stackLimit"),
        overflowAt = d.optInt("overflowAt"),
        title = d.optString("title")
      )
    }

  // ---------------------------------------------------------------------------
  // Per-step state — computed by replaying events [0..stepIdx]. The stack
  // mode renders the live stack plus a "ghost return" frame for one step
  // when the current event is a return; the tree mode keeps all called
  // frames visible and records cumulative return values on edges.
  // ---------------------------------------------------------------------------

  final case class StackState(
      stack: List[String],                     // bottom → top; frame ids on the live stack
      ghostReturnFrame: Option[String],        // popped frame this step (stack mode highlight)
      ghostReturnValue: Option[String],        // value handed back this step
      localsByFrame: Map[String, List[Local]], // current locals per frame (update events refresh)
      calledFrames: List[String],              // ever-called frames in call order (tree mode)
      returnValueByFrame: Map[String, String]  // cumulative return values (tree mode edge labels)
  )

  private def emptyState(spec: Spec): StackState =
    val baseLocals = spec.frameDefs.map(f => f.id -> f.locals).toMap
    StackState(
      stack = Nil,
      ghostReturnFrame = None,
      ghostReturnValue = None,
      localsByFrame = baseLocals,
      calledFrames = Nil,
      returnValueByFrame = Map.empty
    )

  // Apply one event. Return value is the new state PLUS whether this event
  // produced a ghost return for stack mode (set only on `return` events).
  private def applyEvent(state: StackState, event: Event): StackState =
    event.kind match
      case EventCall =>
        event.frameId match
          case Some(fid) =>
            val newStack = state.stack :+ fid
            val calls =
              if state.calledFrames.contains(fid) then state.calledFrames
              else state.calledFrames :+ fid
            state.copy(
              stack = newStack,
              calledFrames = calls,
              ghostReturnFrame = None,
              ghostReturnValue = None
            )
          case None => state.copy(ghostReturnFrame = None, ghostReturnValue = None)
      case EventReturn =>
        event.frameId match
          case Some(fid) =>
            // Pop iff the top matches. If not, leave the stack alone (defensive
            // — a malformed payload that returns a non-top frame still won't
            // crash the renderer).
            val newStack = state.stack.lastOption match
              case Some(top) if top == fid => state.stack.init
              case _                       => state.stack
            val newReturns =
              event.value match
                case Some(v) => state.returnValueByFrame.updated(fid, v)
                case None    => state.returnValueByFrame
            state.copy(
              stack = newStack,
              ghostReturnFrame = Some(fid),
              ghostReturnValue = event.value,
              returnValueByFrame = newReturns
            )
          case None => state.copy(ghostReturnFrame = None, ghostReturnValue = None)
      case EventUpdate =>
        event.frameId match
          case Some(fid) =>
            val newLocals = state.localsByFrame.updated(fid, event.locals)
            state.copy(localsByFrame = newLocals, ghostReturnFrame = None, ghostReturnValue = None)
          case None => state.copy(ghostReturnFrame = None, ghostReturnValue = None)
      case _ =>
        // pause or unknown — clear ghost (it only sits for one step)
        state.copy(ghostReturnFrame = None, ghostReturnValue = None)

  private def computeState(spec: Spec, stepIdx: Int): StackState =
    var s     = emptyState(spec)
    val bound = math.min(stepIdx, spec.events.size - 1)
    for i <- 0 to bound do s = applyEvent(s, spec.events(i))
    s

  // ---------------------------------------------------------------------------
  // Stack-mode layout — frames stack vertically; bottom (slot 0) = first call.
  // The canvas reserves enough height for the deepest live stack depth across
  // all steps (so a payload that peaks at 6 frames lays out a 6-slot canvas
  // and never re-flows when only 3 are visible).
  // ---------------------------------------------------------------------------

  private def frameHeightStack(state: StackState, frameId: String, spec: Spec): Double =
    val locals = state.localsByFrame.getOrElse(frameId, baseLocalsFor(spec, frameId))
    StackFrameBaseH + locals.size.toDouble * StackFrameLocalH

  private def baseLocalsFor(spec: Spec, frameId: String): List[Local] =
    spec.frameDefs.find(_.id == frameId).map(_.locals).getOrElse(Nil)

  // Maximum frame depth (live stack + 1 for ghost) reached across all steps —
  // used to allocate the stack-mode canvas height.
  private def maxStackDepth(spec: Spec): Int =
    var s   = emptyState(spec)
    var max = 0
    for ev <- spec.events do
      s = applyEvent(s, ev)
      val withGhost = s.stack.size + (if s.ghostReturnFrame.isDefined then 1 else 0)
      if withGhost > max then max = withGhost
    max

  // Maximum frame height across all (frameId, step) pairs in the spec — used
  // so a payload that grows a frame's locals from 1 to 2 mid-stream reserves
  // the larger height up-front, preventing the stack from re-flowing on the
  // step where the update lands.
  private def maxFrameHeight(spec: Spec): Double =
    var s   = emptyState(spec)
    var max = StackFrameBaseH
    for ev <- spec.events do
      s = applyEvent(s, ev)
      val frames = s.stack ++ s.ghostReturnFrame.toList
      for f <- frames do
        val h = frameHeightStack(s, f, spec)
        if h > max then max = h
    max

  // ---------------------------------------------------------------------------
  // Recursion-tree-mode layout — DFS over the frameDefs' parent pointers, with
  // sibling spread. Each leaf takes the next slot; internal nodes sit at the
  // midpoint of their children's slot range. The layout is computed once from
  // the spec (every frame ever called appears in the tree; the renderer just
  // fades in / dims based on per-step state). Output: (id → (xSlot, depth)) +
  // maxDepth + slotCount.
  // ---------------------------------------------------------------------------

  private def computeTreeLayout(spec: Spec): (Map[String, (Double, Int)], Int, Double) =
    val frames     = spec.frameDefs
    val byId       = frames.map(f => f.id -> f).toMap
    val childrenOf = mutable.Map.empty[String, mutable.ArrayBuffer[String]]
    val roots      = mutable.ArrayBuffer.empty[String]
    // Preserve the spec's declaration order for siblings so the author can
    // arrange `left`/`right` recursive calls deliberately (e.g. fib(3) before
    // fib(2) in fib(4)'s children).
    for f <- frames do
      f.parent match
        case Some(pid) if byId.contains(pid) =>
          childrenOf.getOrElseUpdate(pid, mutable.ArrayBuffer.empty[String]) += f.id
        case _ => roots += f.id
    val positions = mutable.Map.empty[String, (Double, Int)]
    val nextSlot  = Array(0.0)
    var maxDepth  = 0
    def visit(id: String, depth: Int): Unit =
      if depth > maxDepth then maxDepth = depth
      val children = childrenOf.getOrElse(id, mutable.ArrayBuffer.empty[String]).toList
      if children.isEmpty then
        val mySlot = nextSlot(0)
        nextSlot(0) += 1.0
        positions(id) = (mySlot, depth)
      else
        // Visit children left-to-right; record their assigned slots; my slot
        // is the midpoint of (firstChildSlot, lastChildSlot).
        children.foreach(visit(_, depth + 1))
        val firstSlot = positions(children.head)._1
        val lastSlot  = positions(children.last)._1
        positions(id) = ((firstSlot + lastSlot) / 2.0, depth)
    roots.foreach(visit(_, 0))
    val slotCount = nextSlot(0).max(1.0)
    (positions.toMap, maxDepth, slotCount)

  // ---------------------------------------------------------------------------
  // SVG bootstrap — first paint only. viewBox + width/height refreshed per
  // render because the canvas size depends on the current mode + spec.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", s"call-stack__svg call-stack__svg--${spec.mode}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Call stack (${spec.mode})"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", "0 0 400 200")
      svg.setAttribute("width", "400")
      svg.setAttribute("height", "200")
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Per-step render — idempotent. Frames keyed by frameId so a push slides
  // the same DOM element in; a pop fades it out via the singleton-keyed
  // ghost slot. Tree-mode edges keyed by `${parentId}->${childId}`.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, stepIdx: Int, animate: Boolean): Unit =
    val svg     = D3.select(svgEl)
    val state   = computeState(spec, stepIdx)
    val moveDur = if animate then TransitionDurationMs else 0.0
    val fadeDur = if animate then FadeMs else 0.0

    if spec.mode == ModeRecursionTree then
      renderTreeMode(svg, spec, state, animate, fadeDur, moveDur)
      // Clear stack-mode elements when switching mode never happens (mode is
      // fixed per spec), but defensively remove them so a re-mount with a new
      // spec doesn't leave stale nodes.
      val _ = svg.selectAll("g.call-stack__frame").remove()
      val _ = svg.selectAll("path.call-stack__pointer").remove()
      val _ = svg.selectAll("rect.call-stack__memory").remove()
      val _ = svg.selectAll("text.call-stack__memory-label").remove()
    else
      renderStackMode(svg, spec, state, stepIdx, animate, fadeDur, moveDur)
      val _ = svg.selectAll("g.call-stack__tree-node").remove()
      val _ = svg.selectAll("path.call-stack__tree-edge").remove()
      val _ = svg.selectAll("text.call-stack__tree-return").remove()

  // ── Stack-mode renderer ───────────────────────────────────────────────────

  private def renderStackMode(
      svg: D3.Selection,
      spec: Spec,
      state: StackState,
      stepIdx: Int,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    // Compute the slot count: the canvas allocates max(maxStackDepth, stackLimit)
    // slots so the chrome doesn't grow / shrink between steps.
    val maxDepth  = maxStackDepth(spec)
    val slotCount = math.max(maxDepth, spec.stackLimit.getOrElse(0))
    val frameH    = maxFrameHeight(spec)
    val totalH = PaddingY * 2 + slotCount.toDouble * (frameH + StackFrameGap) - (if slotCount > 0 then
                                                                                   StackFrameGap
                                                                                 else 0)
    val totalW = PaddingX * 2 + StackFrameWidth + StackPointerGap + StackPointerW + StackMemoryGap

    val _ = svg.attr("viewBox", s"0 0 $totalW $totalH")
    val _ = svg.attr("width", totalW.toString)
    val _ = svg.attr("height", totalH.toString)

    // Helper: slot 0 is the bottom of the stack; slot N-1 is the top.
    // Y of a frame at slot s = total - paddingY - (s+1)*frameH - s*gap
    val frameX = PaddingX + StackMemoryGap / 2.0
    def slotY(slot: Int): Double =
      totalH - PaddingY - (slot + 1).toDouble * frameH - slot.toDouble * StackFrameGap

    renderStackMemoryChrome(svg, spec, slotCount, frameH, frameX, totalH, totalW)

    // Visible frames: live stack PLUS the ghost return frame (drawn one slot
    // ABOVE the current top, in the slot it just vacated).
    val stackSize = state.stack.size
    val visibleData = state.stack.zipWithIndex.map { case (fid, slot) =>
      (fid, slot, false) // (frameId, slot, isGhost)
    } ++ state.ghostReturnFrame.toList.map { fid =>
      (fid, stackSize, true)
    }

    renderStackFrames(
      svg,
      spec,
      state,
      visibleData,
      stepIdx,
      frameX,
      slotY,
      frameH,
      animate,
      fadeDur,
      moveDur
    )
    renderStackPointer(svg, state, frameX, slotY, frameH, animate)

  // ── Stack memory chrome ──────────────────────────────────────────────────
  // Optional outer rect drawn when stackLimit is set. The red overflow band
  // sits behind the overflow-threshold slot (paint hint — the frame itself
  // also picks up the `--overflow` class).

  private def renderStackMemoryChrome(
      svg: D3.Selection,
      spec: Spec,
      slotCount: Int,
      frameH: Double,
      frameX: Double,
      totalH: Double,
      totalW: Double
  ): Unit =
    if spec.stackLimit.isEmpty then
      val _ = svg.selectAll("rect.call-stack__memory").remove()
      val _ = svg.selectAll("rect.call-stack__overflow-band").remove()
      val _ = svg.selectAll("text.call-stack__memory-label").remove()
      return ()
    // Outer rect spans the full canvas; gives a "stack memory" visual frame.
    val memoryData: js.Array[js.Any] = js
      .Array(
        js.Dynamic.literal(
          x = PaddingX,
          y = PaddingY,
          w = totalW - PaddingX * 2,
          h = totalH - PaddingY * 2
        )
      )
      .asInstanceOf[js.Array[js.Any]]
    val memKey: js.Function2[js.Any, Int, js.Any] = (_, _) => "stack-memory"
    val xFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].x
    val yFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].y
    val wFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].w
    val hFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].h
    val memSel = svg.selectAll("rect.call-stack__memory").data(memoryData, memKey)
    val _ = memSel
      .enter()
      .append("rect")
      .attr("class", "call-stack__memory")
      .attr("x", xFn)
      .attr("y", yFn)
      .attr("width", wFn)
      .attr("height", hFn)
      .attr("rx", 6)
    val _ = svg
      .selectAll("rect.call-stack__memory")
      .attr("x", xFn)
      .attr("y", yFn)
      .attr("width", wFn)
      .attr("height", hFn)
    val _ = memSel.exit().remove()

    // Overflow band — light red rect behind the overflow-threshold zone.
    spec.overflowAt match
      case Some(threshold) if threshold >= 1 && threshold <= slotCount =>
        // overflowAt is depth-based (1-indexed): "frames at depth ≥ threshold
        // paint red". The band covers the slot range [threshold - 1 .. top].
        // Slot 0 is the bottom; slot N-1 is the top. The band's BOTTOM y is the
        // top of slot (threshold - 1), which is `slotY(threshold - 1) - StackFrameGap/2`.
        val bandTop    = PaddingY
        val bandBottom = totalH - PaddingY - (threshold - 1).toDouble * (frameH + StackFrameGap)
        val bandData: js.Array[js.Any] = js
          .Array(
            js.Dynamic.literal(
              x = frameX - 4,
              y = bandTop,
              w = StackFrameWidth + 8,
              h = math.max(0, bandBottom - bandTop)
            )
          )
          .asInstanceOf[js.Array[js.Any]]
        val bandKey: js.Function2[js.Any, Int, js.Any] = (_, _) => "overflow-band"
        val bandSel = svg.selectAll("rect.call-stack__overflow-band").data(bandData, bandKey)
        val _ = bandSel
          .enter()
          .append("rect")
          .attr("class", "call-stack__overflow-band")
          .attr("x", xFn)
          .attr("y", yFn)
          .attr("width", wFn)
          .attr("height", hFn)
          .attr("rx", 4)
        val _ = svg
          .selectAll("rect.call-stack__overflow-band")
          .attr("x", xFn)
          .attr("y", yFn)
          .attr("width", wFn)
          .attr("height", hFn)
        val _ = bandSel.exit().remove()
      case _ =>
        val _ = svg.selectAll("rect.call-stack__overflow-band").remove()

  // ── Stack frames ──────────────────────────────────────────────────────────
  // Group keyed by frameId. Each group contains a rect, the frame name,
  // optional locals list, and (for ghost-return frames only) a return-value
  // badge. The CSS rule `transition: transform 450ms ...` on the keyed
  // class animates the slot change (per pattern hint #5 — D3 transition().attr
  // doesn't propagate for multi-keyed groups whose only datum change is
  // coordinates).

  private def renderStackFrames(
      svg: D3.Selection,
      spec: Spec,
      state: StackState,
      visible: List[(String, Int, Boolean)],
      stepIdx: Int,
      frameX: Double,
      slotY: Int => Double,
      frameH: Double,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val topSlot = state.stack.size - 1 // index of active frame; -1 if empty
    // overflowAt is depth-based (1-indexed): "paint frames red when depth ≥
    // overflowAt". A frame at slot s has depth s+1, so the comparison reads
    // s+1 ≥ overflowAt — i.e. s ≥ overflowAt - 1.
    val overflowDepth = spec.overflowAt.getOrElse(Int.MaxValue)
    val data: js.Array[js.Any] = visible.map { case (fid, slot, isGhost) =>
      val fdef   = spec.frameDefs.find(_.id == fid)
      val name   = fdef.map(_.name).getOrElse(fid)
      val locals = state.localsByFrame.getOrElse(fid, fdef.map(_.locals).getOrElse(Nil))
      val x      = frameX
      val y      = slotY(slot)
      val status =
        if isGhost then "returning"
        else if slot == topSlot then "active"
        else "waiting"
      val isOverflow = !isGhost && (slot + 1) >= overflowDepth
      val ghostValue = if isGhost then state.ghostReturnValue.getOrElse("") else ""
      js.Dynamic
        .literal(
          id = fid,
          name = name,
          status = status,
          isOverflow = isOverflow,
          isGhost = isGhost,
          ghostValue = ghostValue,
          x = x,
          y = y,
          h = frameH,
          locals =
            locals.map(l => js.Dynamic.literal(name = l.name, value = l.value).asInstanceOf[js.Any]).toJSArray
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      // Key by frameId only — when a frame transitions from `active` to
      // `returning` (the step it pops), the SAME DOM element flips classes
      // via classFn and shows the return value, then exits the next step.
      // Keying ghosts separately would create two DOM elements at the same
      // slot for one step, causing a visible flicker.
      d.asInstanceOf[js.Dynamic].id.asInstanceOf[String]
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn        = d.asInstanceOf[js.Dynamic]
      val status     = dyn.status.asInstanceOf[String]
      val isOverflow = dyn.isOverflow.asInstanceOf[Boolean]
      val base       = s"call-stack__frame call-stack__frame--$status"
      if isOverflow then s"$base call-stack__frame--overflow" else base

    val sel = svg.selectAll("g.call-stack__frame").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", classFn)
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("rect")
      .attr("class", "call-stack__frame-rect")
      .attr("rx", 5)
      .attr("width", StackFrameWidth)
      .attr("height", ((d, _) => d.asInstanceOf[js.Dynamic].h): js.Function2[js.Any, Int, js.Any])
    val _ = enter
      .append("text")
      .attr("class", "call-stack__frame-name")
      .attr("x", 10)
      .attr("y", 22)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    val _ = enter
      .append("text")
      .attr("class", "call-stack__frame-return")
      .attr("x", StackFrameWidth - 10)
      .attr("y", 22)
      .attr("text-anchor", "end")
      .text((
          (d, _) => {
            val dyn = d.asInstanceOf[js.Dynamic]
            if dyn.isGhost.asInstanceOf[Boolean] then s"→ ${dyn.ghostValue.asInstanceOf[String]}"
            else ""
          }
      ): js.Function2[js.Any, Int, js.Any])
    val _ = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    // Synchronous .exit().remove() (HeapTree's pattern) — a transitioned exit
    // would leave the exiting elements in the DOM long enough for the
    // immediately-following `all.attr("opacity", 1)` to override the fade.
    // The frames don't need a fade-out — the ghost-return frame is the visual
    // cue that something just popped; the prior step's stack elements just
    // disappear.
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.call-stack__frame")
    val _   = all.attr("class", classFn)
    val _ = all
      .select("rect.call-stack__frame-rect")
      .attr("height", ((d, _) => d.asInstanceOf[js.Dynamic].h): js.Function2[js.Any, Int, js.Any])
    val _ = all
      .select("text.call-stack__frame-name")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    val _ = all
      .select("text.call-stack__frame-return")
      .text((
          (d, _) => {
            val dyn = d.asInstanceOf[js.Dynamic]
            if dyn.isGhost.asInstanceOf[Boolean] then s"→ ${dyn.ghostValue.asInstanceOf[String]}"
            else ""
          }
      ): js.Function2[js.Any, Int, js.Any])
    // Snap-set transform via .attr (CSS handles the slide — pattern hint #5).
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

    // Locals — D3's .data(fn) for nested selections isn't exposed in this
    // codebase's facade (only .data(Array) and .data(Array, keyFn)), so we
    // flatten locals to a top-level keyed selection. Each local row carries
    // absolute (x, y) coordinates derived from its parent frame's slot, plus
    // an `isGhost` flag that pulls the right key (so ghost frames don't
    // collide with their live counterpart's locals when both render
    // momentarily on a return step). The CSS rule on .call-stack__frame-local
    // gives it the same transform-transition as the frame itself, so the
    // locals slide alongside their frame.
    val localData: js.Array[js.Any] = visible.flatMap { case (fid, slot, _) =>
      val fdef   = spec.frameDefs.find(_.id == fid)
      val locals = state.localsByFrame.getOrElse(fid, fdef.map(_.locals).getOrElse(Nil))
      locals.zipWithIndex.map { case (l, i) =>
        val x = frameX + 14
        val y = slotY(slot) + StackFrameBaseH - 4 + i.toDouble * StackFrameLocalH
        js.Dynamic
          .literal(
            // Same key strategy as frames — frameId only, so a local stays
            // bound to its frame across the `active` → `returning` transition.
            key = s"local-$fid-${l.name}",
            text = s"${l.name} = ${l.value}",
            x = x,
            y = y
          )
          .asInstanceOf[js.Any]
      }
    }.toJSArray
    val localKey: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].key
    val localXFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].x
    val localYFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].y
    val localTxtFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val localSel = svg.selectAll("text.call-stack__frame-local").data(localData, localKey)
    val _ = localSel
      .enter()
      .append("text")
      .attr("class", "call-stack__frame-local")
      .attr("x", localXFn)
      .attr("y", localYFn)
      .attr("opacity", 0)
      .text(localTxtFn)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = svg
      .selectAll("text.call-stack__frame-local")
      .attr("x", localXFn)
      .attr("y", localYFn)
      .text(localTxtFn)
    val _ = localSel.exit().remove()
    val _ = stepIdx // referenced for parity with other widgets' renderer signatures

  // ── Stack pointer ─────────────────────────────────────────────────────────
  // ▶ glyph on the right of the active (top) frame. Hidden when the stack is
  // empty. Singleton-keyed — snap-set transform per pattern hint #4.

  private def renderStackPointer(
      svg: D3.Selection,
      state: StackState,
      frameX: Double,
      slotY: Int => Double,
      frameH: Double,
      animate: Boolean
  ): Unit =
    val topSlot = state.stack.size - 1
    val data: js.Array[js.Any] =
      if topSlot < 0 then js.Array[js.Any]()
      else
        val x = frameX + StackFrameWidth + StackPointerGap
        val y = slotY(topSlot) + frameH / 2
        js.Array(js.Dynamic.literal(x = x, y = y).asInstanceOf[js.Any])
    val keyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "pointer"
    val dFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"M $x ${y - 8} L ${x + StackPointerW - 4} $y L $x ${y + 8} Z"
    val sel = svg.selectAll("path.call-stack__pointer").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", "call-stack__pointer")
      .attr("d", dFn)
      .attr("opacity", 0)
      .transition()
      .duration(FadeMs)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = animate
    val _ = svg.selectAll("path.call-stack__pointer").attr("d", dFn)
    val _ = sel.exit().remove()

  // ── Recursion-tree-mode renderer ──────────────────────────────────────────
  // Every frameDef appears in the tree (the layout is static). Per-step state
  // determines: which nodes are dim ("popped"), which is active (top of
  // stack), and which return-value labels are visible (cumulative).

  private def renderTreeMode(
      svg: D3.Selection,
      spec: Spec,
      state: StackState,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val (positions, maxDepth, slotCount) = computeTreeLayout(spec)
    val treeWidth = math.max(1.0, slotCount) * (TreeNodeWidth + TreeNodeGap) - TreeNodeGap
    val totalW    = PaddingX * 2 + treeWidth
    val totalH    = PaddingY * 2 + (maxDepth + 1).toDouble * TreeLevelH

    val _ = svg.attr("viewBox", s"0 0 $totalW $totalH")
    val _ = svg.attr("width", totalW.toString)
    val _ = svg.attr("height", totalH.toString)

    def nodeX(id: String): Double =
      val slot = positions(id)._1
      PaddingX + slot * (TreeNodeWidth + TreeNodeGap)
    def nodeY(id: String): Double =
      val depth = positions(id)._2
      PaddingY + depth.toDouble * TreeLevelH
    def nodeCenterX(id: String): Double = nodeX(id) + TreeNodeWidth / 2.0
    def nodeBottomY(id: String): Double = nodeY(id) + TreeNodeHeight
    def nodeTopY(id: String): Double    = nodeY(id)

    renderTreeEdges(svg, spec, positions, nodeCenterX, nodeBottomY, nodeTopY, animate, fadeDur, moveDur)
    renderTreeNodes(svg, spec, state, positions, nodeX, nodeY, animate, fadeDur, moveDur)
    renderTreeReturnLabels(svg, spec, state, positions, nodeCenterX, nodeY, fadeDur)

  private def renderTreeEdges(
      svg: D3.Selection,
      spec: Spec,
      positions: Map[String, (Double, Int)],
      nodeCenterX: String => Double,
      nodeBottomY: String => Double,
      nodeTopY: String => Double,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val edges = spec.frameDefs.flatMap { child =>
      child.parent.filter(positions.contains).map(pid => (pid, child.id))
    }
    val data: js.Array[js.Any] = edges.map { case (pid, cid) =>
      val x1 = nodeCenterX(pid)
      val y1 = nodeBottomY(pid)
      val x2 = nodeCenterX(cid)
      val y2 = nodeTopY(cid)
      js.Dynamic
        .literal(
          id = s"$pid->$cid",
          d = s"M $x1 $y1 L $x2 $y2"
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val dFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].d
    val sel = svg.selectAll("path.call-stack__tree-edge").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", "call-stack__tree-edge")
      .attr("d", dFn)
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = animate
    val _ = moveDur
    val _ = svg.selectAll("path.call-stack__tree-edge").attr("d", dFn)
    val _ = sel.exit().remove()

  private def renderTreeNodes(
      svg: D3.Selection,
      spec: Spec,
      state: StackState,
      positions: Map[String, (Double, Int)],
      nodeX: String => Double,
      nodeY: String => Double,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val topFrame = state.stack.lastOption
    val data: js.Array[js.Any] = spec.frameDefs.collect {
      case f if positions.contains(f.id) =>
        val x           = nodeX(f.id)
        val y           = nodeY(f.id)
        val called      = state.calledFrames.contains(f.id)
        val onStack     = state.stack.contains(f.id)
        val isReturning = state.ghostReturnFrame.contains(f.id)
        val isActive    = topFrame.contains(f.id)
        val status =
          if !called then "pending"
          else if isReturning then "returning"
          else if isActive then "active"
          else if onStack then "waiting"
          else "popped"
        js.Dynamic
          .literal(
            id = f.id,
            name = f.name,
            status = status,
            x = x,
            y = y
          )
          .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val status = d.asInstanceOf[js.Dynamic].status.asInstanceOf[String]
      s"call-stack__tree-node call-stack__tree-node--$status"
    val sel = svg.selectAll("g.call-stack__tree-node").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", classFn)
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("rect")
      .attr("class", "call-stack__tree-rect")
      .attr("rx", 5)
      .attr("width", TreeNodeWidth)
      .attr("height", TreeNodeHeight)
    val _ = enter
      .append("text")
      .attr("class", "call-stack__tree-name")
      .attr("text-anchor", "middle")
      .attr("x", TreeNodeWidth / 2)
      .attr("y", TreeNodeHeight / 2 + 5)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.call-stack__tree-node")
    val _   = all.attr("class", classFn)
    val _ = all
      .select("text.call-stack__tree-name")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    // Snap-set transform — same pattern as HeapTree (pattern hint #5).
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ── Tree return labels ────────────────────────────────────────────────────
  // Persistent labels on edges showing the cumulative return values. Once a
  // frame returns, its label sits permanently on the edge from itself to
  // its parent (so the reader can scrub through and see the whole return
  // graph). The label is keyed by the child frame's id.

  private def renderTreeReturnLabels(
      svg: D3.Selection,
      spec: Spec,
      state: StackState,
      positions: Map[String, (Double, Int)],
      nodeCenterX: String => Double,
      nodeY: String => Double,
      fadeDur: Double
  ): Unit =
    val data: js.Array[js.Any] = spec.frameDefs.collect {
      case f if f.parent.exists(positions.contains) && state.returnValueByFrame.contains(f.id) =>
        val pid = f.parent.get
        val v   = state.returnValueByFrame(f.id)
        // Midpoint of the edge from parent to child — slight upward bias so
        // the text sits above the line rather than crossing it.
        val midX = (nodeCenterX(pid) + nodeCenterX(f.id)) / 2.0
        val midY = (nodeY(pid) + TreeNodeHeight + nodeY(f.id)) / 2.0 - TreeReturnLabelGap
        js.Dynamic
          .literal(
            id = f.id,
            text = s"→ $v",
            x = midX,
            y = midY,
            isFresh = state.ghostReturnFrame.contains(f.id)
          )
          .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      s"return-${d.asInstanceOf[js.Dynamic].id.asInstanceOf[String]}"
    val xFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].x
    val yFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].y
    val textFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val fresh = d.asInstanceOf[js.Dynamic].isFresh.asInstanceOf[Boolean]
      if fresh then "call-stack__tree-return call-stack__tree-return--fresh"
      else "call-stack__tree-return"
    val sel = svg.selectAll("text.call-stack__tree-return").data(data, keyFn)
    val _ = sel
      .enter()
      .append("text")
      .attr("class", classFn)
      .attr("text-anchor", "middle")
      .attr("x", xFn)
      .attr("y", yFn)
      .attr("opacity", 0)
      .text(textFn)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val all = svg.selectAll("text.call-stack__tree-return")
    val _ = all
      .attr("class", classFn)
      .attr("x", xFn)
      .attr("y", yFn)
      .text(textFn)
    val _ = sel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component — React owns the host div; D3 owns the SVG inside it. The mode
  // badge sits above the SVG as a static React node, not on the canvas,
  // because it doesn't animate per step.
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
        specM.value.toOption.filter(_.events.nonEmpty) match
          case Some(spec) =>
            hostRef.foreach { host =>
              val svgEl   = ensureSvg(host, spec)
              val animate = hasRenderedRef.value
              renderStep(svgEl, spec, index, animate)
              if !hasRenderedRef.value then hasRenderedRef.value = true
            }
          case None => Callback.empty
      }
      .render { (_, specM, stepper, hostRef, _) =>
        specM.value match
          case Left(err) =>
            <.div(
              ^.className := "d3-widget__error",
              <.p(^.className   := "d3-widget__error-title", "Call-stack payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.events.size
            val idx   = stepper.index
            val currentEvent =
              if count == 0 then Event(EventPause, None, None, Nil, "No events defined.")
              else spec.events(idx)

            val modeBadge: VdomNode =
              val text = spec.mode match
                case ModeStack         => "stack"
                case ModeRecursionTree => "recursion tree"
                case _                 => spec.mode
              <.div(
                ^.className := "call-stack__mode-row",
                <.span(^.className := s"call-stack__mode-badge call-stack__mode-badge--${spec.mode}", text)
              )

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "call-stack__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "call-stack__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("call-stack__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "call-stack__button call-stack__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("call-stack__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("call-stack__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "call-stack__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("call-stack__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "call-stack__button call-stack__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("call-stack__button-icon"))
                  ),
                  <.span(
                    ^.className := "call-stack__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"call-stack call-stack--${spec.mode} not-prose",
              spec.title
                .map(t => <.p(^.className := "call-stack__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              modeBadge,
              <.div(
                ^.className := "call-stack__frame-host"
              ).withRef(hostRef),
              <.p(
                ^.className := "call-stack__caption",
                ^.aria.live := "polite",
                if currentEvent.msg.nonEmpty then currentEvent.msg else " "
              ),
              controls
            )
      }
