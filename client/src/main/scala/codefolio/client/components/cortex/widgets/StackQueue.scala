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
 * Stack / queue stepper — the third widget in the D3 catalog after `array-traversal` and `linked-list`.
 * Covers two Phase 4 + Phase 5 topologies in one widget through a `mode` flag:
 *
 *   - `mode: "stack"` — vertical column of cells, items grow bottom-up (items[0] is the oldest at the bottom;
 *     items.last is the newest at the top). The `top` pointer labels the top cell from the right.
 *   - `mode: "queue"` — horizontal row of cells, items grow left-to-right (items[0] is the oldest at the
 *     front; items.last is the newest at the back). The `front` and `back` pointers label their respective
 *     cells from above.
 *
 * `deque` and `priority` modes are not in this pilot — they're listed in the widget spec for a follow-on
 * iteration. The expression-strip overlay (input + output token rows, used by the infix→postfix /
 * prefix-conversion chapters) is also deferred.
 *
 * Rendering follows the same React + D3 boundary as the earlier widgets (ADR-0013): React owns a host
 * `<div>`; D3 owns the `<svg>` it creates inside that div. On step change a single update pass rebinds item
 * data (keyed by `item.id`) and pointer data (keyed by marker name) so an item moving from slot 2 to slot 3
 * (after a pop ahead of it) transitions its transform smoothly. Pointer colours come from [[MarkerCanon]] —
 * `top`/`front`/`back` are part of the shared canon so the same hex shows up wherever these pointers appear
 * in the book.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":    "Push A, B, C then pop",
 *   "mode":     "stack",                          // or "queue"
 *   "capacity": 5,                                 // optional: render N total slots, muted when empty
 *   "items":    [],                                // initial state (optional; defaults to empty)
 *   "steps": [
 *     {
 *       "op":       "push",                        // optional: drives the banner label
 *       "opValue":  "A",                           // optional: shown next to the op
 *       "items":    [{"id": "a", "value": "A"}],   // per-step container state (always required)
 *       "markers":  [{"name": "top", "itemId": "a"}],
 *       "msg":      "Push A → top = A."
 *     },
 *     ...
 *   ]
 * }
 * }}}
 *
 * Marker names are checked against [[MarkerCanon]]; any author-supplied `color` field is silently dropped at
 * parse time, same as LinkedList. A name not in the canon renders a warning-coloured badge so authors see
 * typos immediately. The `markers` list per step is treated as exhaustive — there is no auto-derived
 * `top`/`front`/`back` fallback. Authors enumerate the pointers they want shown so step-to-step transitions
 * are explicit.
 */
object StackQueue:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose.
  // ---------------------------------------------------------------------------

  final case class Item(id: String, value: String)
  final case class Marker(name: String, itemId: String, canonical: Boolean)

  final case class Step(
      items: List[Item],
      markers: List[Marker],
      op: Option[String],
      opValue: Option[String],
      msg: String
  )

  final case class Spec(
      mode: String,
      capacity: Option[Int],
      title: Option[String],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants — sized to fit comfortably inside a chapter's prose column.
  // ---------------------------------------------------------------------------

  private val CellSize             = 56.0
  private val CellGap              = 4.0
  private val MarkerLaneH          = 36.0
  private val PaddingX             = 16.0
  private val PaddingY             = 16.0
  private val StepDelayMs          = 1200
  private val TransitionDurationMs = 450.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // Stack-pointer marker lane (right of the column) gets its own width budget so
  // labels like `top` fit without overlapping the column.
  private val StackMarkerLaneW = 64.0

  // Modes — kept as plain strings on the spec side; the widget collapses unknown
  // values to "stack" rather than failing, so a typo renders something visible.
  private val ModeStack = "stack"
  private val ModeQueue = "queue"

  // ---------------------------------------------------------------------------
  // Parsing — every required field validated inside `PayloadDecoder.run`; any
  // thrown exception (missing field, bad shape) collapses to `Left(msg)` and
  // the renderer shows an inline error placeholder instead of crashing.
  // ---------------------------------------------------------------------------

  private def parseItem(d: js.Dynamic): Item =
    Item(
      id = d.string("id"),
      value = d.string("value")
    )

  private def parseMarker(m: js.Dynamic): Marker =
    val name      = m.string("name")
    val itemId    = m.string("itemId")
    val rawColor  = m.optString("color")
    val canonical = MarkerCanon.isCanonical(name)
    if rawColor.isDefined then MarkerCanon.warnAuthorColor("stack-queue", name)
    if name.nonEmpty && !canonical then MarkerCanon.warnUnknown("stack-queue", name)
    Marker(name, itemId, canonical)

  private def parseStep(s: js.Dynamic): Step =
    Step(
      items = s.dynList("items").map(parseItem).filter(_.id.nonEmpty),
      markers = s.dynList("markers").map(parseMarker).filter(m => m.name.nonEmpty && m.itemId.nonEmpty),
      op = s.optString("op"),
      opValue = s.optString("opValue"),
      msg = s.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawMode  = d.optString("mode").getOrElse(ModeStack)
      val mode     = if rawMode == ModeQueue then ModeQueue else ModeStack
      val capacity = d.optInt("capacity").filter(_ > 0)
      val steps    = d.dynList("steps").map(parseStep)
      if steps.isEmpty then throw PayloadDecoder.invalid("steps must be non-empty")
      Spec(
        mode = mode,
        capacity = capacity,
        title = d.optString("title"),
        steps = steps
      )
    }

  // ---------------------------------------------------------------------------
  // Layout helpers — `slotCount` is the maximum number of cells ever rendered
  // (capacity if set, otherwise the max items seen across all steps).
  // ---------------------------------------------------------------------------

  private def slotCount(spec: Spec): Int =
    spec.capacity match
      case Some(c) => math.max(1, c)
      case None    => math.max(1, spec.steps.iterator.map(_.items.size).maxOption.getOrElse(1))

  private def viewBoxWidth(spec: Spec): Double =
    spec.mode match
      case `ModeStack` =>
        PaddingX * 2 + CellSize + StackMarkerLaneW
      case _ =>
        val n = slotCount(spec)
        PaddingX * 2 + n * CellSize + (n - 1).max(0) * CellGap

  private def viewBoxHeight(spec: Spec): Double =
    spec.mode match
      case `ModeStack` =>
        val n = slotCount(spec)
        PaddingY * 2 + n * CellSize + (n - 1).max(0) * CellGap
      case _ =>
        PaddingY * 2 + CellSize + MarkerLaneH

  // Slot 0 lives at the BOTTOM of the stack and at the LEFT of the queue.
  // `slot` is the position (0-indexed) within the container, not the item
  // index in the list.
  private def cellX(spec: Spec, slot: Int): Double =
    spec.mode match
      case `ModeStack` => PaddingX
      case _           => PaddingX + slot * (CellSize + CellGap)

  private def cellY(spec: Spec, slot: Int): Double =
    spec.mode match
      case `ModeStack` =>
        val n = slotCount(spec)
        // Slot 0 (bottom) sits at the largest Y; slot N-1 (top) at the smallest.
        PaddingY + (n - 1 - slot) * (CellSize + CellGap)
      case _ =>
        // Queue: cells sit below the marker lane so pointer arrows have room above.
        PaddingY + MarkerLaneH

  // The slot a given itemId occupies in the current step. -1 when not present
  // (defensive; the parser already filters bad ids).
  private def slotOf(step: Step, itemId: String): Int =
    step.items.indexWhere(_.id == itemId)

  // ---------------------------------------------------------------------------
  // SVG bootstrap — set up once on first render so the host div + SVG element
  // exist before D3 starts binding data.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg    = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      val width  = viewBoxWidth(spec)
      val height = viewBoxHeight(spec)
      svg.setAttribute("class", s"stack-queue__svg stack-queue__svg--${spec.mode}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Stack/queue ${spec.mode}"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 $width $height")
      svg.setAttribute("width", width.toString)
      svg.setAttribute("height", height.toString)
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // D3 render — runs after every step/spec change. Idempotent: first call sets
  // up via `enter()`; subsequent calls re-bind data + transition positions.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg = D3.select(svgEl)
    val n   = slotCount(spec)

    // ── Slot rectangles (capacity background) ────────────────────────────────
    // When `capacity` is set we always render `n` background cells; the item-
    // bearing layer above paints over the occupied ones. When `capacity` is
    // absent we skip slots and only render the items themselves so the widget
    // grows with the data.
    val slotData: js.Array[js.Any] =
      if spec.capacity.isEmpty then js.Array()
      else (0 until n).map(i => js.Dynamic.literal(slot = i).asInstanceOf[js.Any]).toJSArray
    val slotKeyFn: js.Function2[js.Any, Int, js.Any] =
      (_, i) => s"slot-$i"
    val slotSel = svg.selectAll("rect.stack-queue__slot").data(slotData, slotKeyFn)
    val _ = slotSel
      .enter()
      .append("rect")
      .attr("class", "stack-queue__slot")
      .attr("width", CellSize)
      .attr("height", CellSize)
      .attr("rx", 6)
    val slotAll = svg.selectAll("rect.stack-queue__slot")
    val slotX: js.Function2[js.Any, Int, js.Any] =
      (d, _) => cellX(spec, d.asInstanceOf[js.Dynamic].slot.asInstanceOf[Int])
    val slotY: js.Function2[js.Any, Int, js.Any] =
      (d, _) => cellY(spec, d.asInstanceOf[js.Dynamic].slot.asInstanceOf[Int])
    val _ = slotAll.attr("x", slotX).attr("y", slotY)
    val _ = slotSel.exit().remove()

    // ── Items (the data-bearing cells) ───────────────────────────────────────
    // Keyed by `item.id` so a slot change transitions transform rather than
    // recreating the DOM node. Each item's slot is its index in `step.items`.
    val itemData: js.Array[js.Any] = step.items.zipWithIndex.map { case (it, slot) =>
      js.Dynamic.literal(id = it.id, value = it.value, slot = slot).asInstanceOf[js.Any]
    }.toJSArray
    val itemKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].id
    val itemTransform: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val slot = d.asInstanceOf[js.Dynamic].slot.asInstanceOf[Int]
        s"translate(${cellX(spec, slot)}, ${cellY(spec, slot)})"
    val itemSel = svg.selectAll("g.stack-queue__item").data(itemData, itemKeyFn)
    val itemEnter = itemSel
      .enter()
      .append("g")
      .attr("class", "stack-queue__item")
      .attr("transform", itemTransform)
      .attr("opacity", 0)
    val _ = itemEnter
      .append("rect")
      .attr("class", "stack-queue__cell")
      .attr("width", CellSize)
      .attr("height", CellSize)
      .attr("rx", 6)
    val _ = itemEnter
      .append("text")
      .attr("class", "stack-queue__cell-label")
      .attr("x", CellSize / 2)
      .attr("y", CellSize / 2 + 5)
      .attr("text-anchor", "middle")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _        = itemEnter.transition().duration(TransitionDurationMs).attr("opacity", 1)
    val allItems = svg.selectAll("g.stack-queue__item")
    val _ =
      if animate then
        allItems
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", itemTransform)
          .attr("opacity", 1)
      else allItems.attr("transform", itemTransform).attr("opacity", 1)
    // Update value text in case the same id changed value (rare but supported).
    val _ = svg
      .selectAll("g.stack-queue__item text.stack-queue__cell-label")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    // Exit items immediately rather than via a fade-out transition. Fade-out
    // schedules `.remove()` at the transition's end, but a subsequent render
    // that fires before that end interrupts the transition and the remove
    // never runs — leaving stale `<g>` nodes at `opacity: 0` in the DOM. Snap
    // removal trades the fade animation for clean teardown; the entering
    // items' fade-in still keeps the overall step transition visually smooth.
    val _ = itemSel.exit().remove()

    // ── Markers (top / front / back) ─────────────────────────────────────────
    // Filter to markers whose itemId actually exists in the current step's
    // items so a marker doesn't render "into the void" when the author leaves
    // a stale id in the payload. Author-supplied unknown names still render
    // with the warning colour so the typo is visible.
    val activeMarkers = step.markers.filter(m => slotOf(step, m.itemId) >= 0)
    val markerData: js.Array[js.Any] = activeMarkers.map { m =>
      val slot = slotOf(step, m.itemId)
      js.Dynamic
        .literal(
          name = m.name,
          itemId = m.itemId,
          slot = slot,
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name

    // Marker transform: for stack mode, anchor at the RIGHT of the column
    // (cell right edge + small gap); for queue, anchor above the cell (top
    // edge - small gap so the triangle/text sit in the marker lane).
    val markerTransform: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val slot = d.asInstanceOf[js.Dynamic].slot.asInstanceOf[Int]
        spec.mode match
          case `ModeStack` =>
            val cx = cellX(spec, slot) + CellSize + 8
            val cy = cellY(spec, slot) + CellSize / 2
            s"translate($cx, $cy)"
          case _ =>
            val cx = cellX(spec, slot) + CellSize / 2
            val cy = cellY(spec, slot) - 6
            s"translate($cx, $cy)"

    val markerSel = svg.selectAll("g.stack-queue__marker").data(markerData, markerKeyFn)
    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "stack-queue__marker")
      .attr("transform", markerTransform)
      .attr("opacity", 0)
    // Triangle — points LEFT in stack mode (into the cell from the right),
    // DOWN in queue mode (into the cell from above). Skip for non-canonical
    // names so the warning state is visually unmistakeable.
    val triangleD: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      if !canonical then ""
      else
        spec.mode match
          case `ModeStack` => "M 6 -5 L 6 5 L -2 0 Z"
          case _           => "M -5 -6 L 5 -6 L 0 2 Z"
    val _ = markerEnter
      .append("path")
      .attr("class", "stack-queue__marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerEnter
      .append("text")
      .attr("class", "stack-queue__marker-label")
      .attr("x", ((_, _) => if spec.mode == ModeStack then 14 else 0): js.Function2[js.Any, Int, js.Any])
      .attr("y", ((_, _) => if spec.mode == ModeStack then 4 else -10): js.Function2[js.Any, Int, js.Any])
      .attr(
        "text-anchor",
        ((_, _) => if spec.mode == ModeStack then "start" else "middle"): js.Function2[js.Any, Int, js.Any]
      )
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text((
          (d, _) => {
            val dyn       = d.asInstanceOf[js.Dynamic]
            val canonical = dyn.canonical.asInstanceOf[Boolean]
            val name      = dyn.name.asInstanceOf[String]
            if canonical then name else s"⚠ $name"
          }
      ): js.Function2[js.Any, Int, js.Any])
    val _          = markerEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allMarkers = svg.selectAll("g.stack-queue__marker")
    val _ =
      if animate then
        allMarkers
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", markerTransform)
          .attr("opacity", 1)
      else allMarkers.attr("transform", markerTransform).attr("opacity", 1)
    // Snap-remove markers for the same reason items snap-remove on exit.
    val _ = markerSel.exit().remove()

    // ── Empty-state placeholder ──────────────────────────────────────────────
    // When the container is empty AND no capacity grid is rendered (so there's
    // nothing else to look at), drop a centred "(empty)" hint so the step
    // doesn't render as a blank rectangle.
    val showEmpty = step.items.isEmpty && spec.capacity.isEmpty
    val emptyData: js.Array[js.Any] =
      if showEmpty then js.Array(js.Dynamic.literal(t = "(empty)").asInstanceOf[js.Any]) else js.Array()
    val emptyKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "empty"
    val emptySel = svg.selectAll("text.stack-queue__empty").data(emptyData, emptyKeyFn)
    val _ = emptySel
      .enter()
      .append("text")
      .attr("class", "stack-queue__empty")
      .attr("text-anchor", "middle")
      .attr("x", viewBoxWidth(spec) / 2)
      .attr("y", viewBoxHeight(spec) / 2 + 5)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].t): js.Function2[js.Any, Int, js.Any])
    val _ = emptySel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component — React owns the host div; D3 owns the SVG inside it.
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
              <.p(^.className   := "d3-widget__error-title", "Stack/queue payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then Step(Nil, Nil, None, None, "No steps defined.")
              else spec.steps(idx)

            val banner: VdomNode = currentStep.op match
              case Some(op) =>
                val label =
                  currentStep.opValue.fold(op)(v => s"$op $v")
                <.p(^.className := "stack-queue__op", label)
              case None => EmptyVdom

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "stack-queue__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "stack-queue__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("stack-queue__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "stack-queue__button stack-queue__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("stack-queue__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("stack-queue__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "stack-queue__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("stack-queue__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "stack-queue__button stack-queue__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("stack-queue__button-icon"))
                  ),
                  <.span(
                    ^.className := "stack-queue__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"stack-queue stack-queue--${spec.mode} not-prose",
              spec.title
                .map(t => <.p(^.className := "stack-queue__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              banner,
              <.div(
                ^.className := "stack-queue__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "stack-queue__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              controls
            )
      }
