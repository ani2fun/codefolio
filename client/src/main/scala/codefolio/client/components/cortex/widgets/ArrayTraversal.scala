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
 * Array traversal stepper — the first widget in the D3 catalog. Renders a row of cells with one or more named
 * markers per step and an optional highlighted range. Step controls (prev/next/play/reset) scrub through the
 * author-defined steps.
 *
 * Rendering uses D3 selections + transitions inside a React `useEffect`. React owns a host `<div>`; D3 owns
 * the `<svg>` it creates inside that div. On step change a single update pass re-binds data and transitions
 * positions so swap-style problems animate items moving between cells rather than flicker-rebuilding the SVG.
 * See ADR-0013.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "items":  ["a", "e", "i", "o", "u"],
 *   "title":  "Flip characters",
 *   "steps":  [
 *     {
 *       "items": ["a", "e", "i", "o", "u"],            // optional, overrides top-level for this step
 *       "keys":  ["a", "e", "i", "o", "u"],            // optional, defaults to items themselves
 *       "markers": [{"name": "left", "index": 0}, {"name": "right", "index": 4}],
 *       "range":   { "lo": 0, "hi": 4 },
 *       "msg":     "swap arr[0] and arr[4]"
 *     },
 *     ...
 *   ]
 * }
 * }}}
 *
 * The optional per-step `items` lets each step define the array contents. When the contents change across
 * steps, D3 keys the join by `keys` (or by the item label when `keys` is omitted), so an item moving from one
 * index to another transitions its `transform` rather than re-creating a fresh DOM node. Authors whose
 * `items` contain duplicate labels (e.g. palindrome checks on `["r","a","c","e","c","a","r"]`) must provide
 * explicit `keys` to avoid D3 collapsing duplicates.
 */
object ArrayTraversal:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose.
  // ---------------------------------------------------------------------------

  final case class Marker(name: String, index: Int, color: Option[String])
  final case class RangeBand(lo: Int, hi: Int)

  final case class Step(
      items: Option[List[String]],
      keys: Option[List[String]],
      markers: List[Marker],
      range: Option[RangeBand],
      msg: String
  )

  final case class Spec(items: List[String], title: Option[String], steps: List[Step])

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants. Chosen so the widget fits comfortably inside a chapter's
  // prose column without horizontal scrolling for arrays up to ~12 elements.
  // ---------------------------------------------------------------------------

  private val CellSize             = 56.0
  private val CellGap              = 6.0
  private val MarkerLaneH          = 44.0
  private val RangeBandH           = 8.0
  private val PaddingX             = 16.0
  private val PaddingY             = 12.0
  private val StepDelayMs          = 1200
  private val TransitionDurationMs = 450.0
  private val DefaultColor         = "#3b82f6"
  private val SvgNs                = "http://www.w3.org/2000/svg"

  private val PaletteByIndex = Vector(
    "#3b82f6", // left / first
    "#10b981", // mid
    "#f59e0b", // right / hi
    "#a855f7",
    "#ef4444"
  )

  // ---------------------------------------------------------------------------
  // Parsing — `js.JSON.parse` + `js.Dynamic` mirrors BlockDiscovery.parseRawTabs.
  // Anything that throws collapses to a `Left(msg)`; the component renders an
  // error placeholder rather than crashing the chapter.
  // ---------------------------------------------------------------------------

  private def parsePayload(json: String): Either[String, Spec] =
    Try {
      val raw    = js.JSON.parse(json).asInstanceOf[js.Dynamic]
      val itemsJ = raw.items.asInstanceOf[js.Array[js.Any]]
      val items  = itemsJ.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String])
      val title  = raw.title.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val rawSteps = raw.steps
        .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
        .toOption
        .getOrElse(js.Array())
      val steps = rawSteps.toList.map { s =>
        val mks = s.markers
          .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
          .toOption
          .getOrElse(js.Array())
          .toList
          .map { m =>
            Marker(
              name = m.name.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
              index = m.index.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0),
              color = m.color.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
            )
          }
        val rng = s.range.asInstanceOf[js.UndefOr[js.Dynamic]].toOption.map { r =>
          RangeBand(
            lo = r.lo.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0),
            hi = r.hi.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0)
          )
        }
        val perStepItems = s.items.asInstanceOf[js.UndefOr[js.Array[js.Any]]].toOption.map { arr =>
          arr.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String])
        }
        val perStepKeys = s.keys.asInstanceOf[js.UndefOr[js.Array[js.Any]]].toOption.map { arr =>
          arr.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String])
        }
        val msg = s.msg.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        Step(perStepItems, perStepKeys, mks, rng, msg)
      }
      Spec(items, title, steps)
    } match
      case Success(spec) if spec.items.isEmpty => Left("payload.items must be non-empty")
      case Success(spec)                       => Right(spec)
      case Failure(t)                          => Left(Option(t.getMessage).getOrElse("invalid payload JSON"))

  // ---------------------------------------------------------------------------
  // Layout helpers
  // ---------------------------------------------------------------------------

  private def clamp(i: Int, count: Int): Int =
    if count <= 0 then 0 else math.max(0, math.min(count - 1, i))

  private def cellX(index: Int): Double =
    PaddingX + index * (CellSize + CellGap)

  private def viewBoxWidth(itemCount: Int): Double =
    PaddingX * 2 + itemCount * CellSize + (itemCount - 1).max(0) * CellGap

  private def viewBoxHeight: Double =
    PaddingY * 2 + CellSize + MarkerLaneH + RangeBandH + 6.0

  private def colorFor(marker: Marker, fallbackIdx: Int): String =
    marker.color
      .orElse(
        Option.when(fallbackIdx >= 0 && fallbackIdx < PaletteByIndex.length)(PaletteByIndex(fallbackIdx))
      )
      .getOrElse(DefaultColor)

  private def itemsFor(spec: Spec, step: Step): List[String] =
    step.items.getOrElse(spec.items)

  private def keysFor(spec: Spec, step: Step): List[String] =
    step.keys.getOrElse(itemsFor(spec, step))

  // ---------------------------------------------------------------------------
  // Mount the SVG element inside the host div on first call; return the SVG
  // element for D3 to manipulate. Subsequent calls return the existing SVG.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", "array-traversal__svg")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse("Array traversal"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 ${viewBoxWidth(spec.items.size)} $viewBoxHeight")
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // D3 render — runs after every step change. Idempotent: the first call sets up
  // the SVG structure via enter selections; subsequent calls update positions /
  // text / markers via transitions on the same elements.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg       = D3.select(svgEl)
    val items     = itemsFor(spec, step)
    val keys      = keysFor(spec, step)
    val itemCount = spec.items.size
    val cellY     = PaddingY
    val bandY     = PaddingY + CellSize + 18

    val _ = svg.attr("viewBox", s"0 0 ${viewBoxWidth(itemCount)} $viewBoxHeight")

    // ---- Range band: 0 or 1 elements ---------------------------------------
    val rangeData: js.Array[js.Any] = step.range
      .filter(r => r.lo <= r.hi && r.lo >= 0 && r.hi < itemCount)
      .toList
      .map(r => js.Dynamic.literal(lo = r.lo, hi = r.hi).asInstanceOf[js.Any])
      .toJSArray

    val rangeJoin = svg
      .selectAll("rect.array-traversal__range")
      .data(rangeData)
      .join("rect")
      .attr("class", "array-traversal__range")
      .attr("y", bandY)
      .attr("height", RangeBandH)
      .attr("rx", 3)

    val rangeX: js.Function2[js.Any, Int, js.Any] =
      (d, _) => cellX(d.asInstanceOf[js.Dynamic].lo.asInstanceOf[Int])
    val rangeW: js.Function2[js.Any, Int, js.Any] =
      (d, _) =>
        val dyn = d.asInstanceOf[js.Dynamic]
        val lo  = dyn.lo.asInstanceOf[Int]
        val hi  = dyn.hi.asInstanceOf[Int]
        cellX(hi) + CellSize - cellX(lo)
    val _ =
      if animate then
        rangeJoin
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("x", rangeX)
          .attr("width", rangeW)
      else rangeJoin.attr("x", rangeX).attr("width", rangeW)

    // ---- Cells: one <g> per item, keyed by user-supplied key (defaults to item label).
    val cellData: js.Array[js.Any] = items.toJSArray.asInstanceOf[js.Array[js.Any]]
    val cellKeys                   = keys.toJSArray
    val cellKeyFn: js.Function2[js.Any, Int, js.Any] =
      (_, i) => if i < cellKeys.length then cellKeys(i) else js.Dynamic.global.String(i)

    val cellSel = svg
      .selectAll("g.array-traversal__cell-group")
      .data(cellData, cellKeyFn)

    val cellEnter = cellSel
      .enter()
      .append("g")
      .attr("class", "array-traversal__cell-group")
      .attr(
        "transform",
        ((_, i) => s"translate(${cellX(i)}, $cellY)"): js.Function2[js.Any, Int, js.Any]
      )
    val _ = cellEnter
      .append("rect")
      .attr("class", "array-traversal__cell")
      .attr("x", 0)
      .attr("y", 0)
      .attr("width", CellSize)
      .attr("height", CellSize)
      .attr("rx", 6)
    val _ = cellEnter
      .append("text")
      .attr("class", "array-traversal__cell-label")
      .attr("x", CellSize / 2)
      .attr("y", CellSize / 2 + 5)
      .attr("text-anchor", "middle")
      .text(((d, _) => d): js.Function2[js.Any, Int, js.Any])
    val _ = cellEnter
      .append("text")
      .attr("class", "array-traversal__cell-index")
      .attr("x", CellSize / 2)
      .attr("y", CellSize + 12)
      .attr("text-anchor", "middle")
      .text(((_, i) => js.Dynamic.global.String(i)): js.Function2[js.Any, Int, js.Any])

    val _ = cellSel.exit().remove()

    // Re-select to capture both enter and update sets after the join.
    val cellAll = svg.selectAll("g.array-traversal__cell-group")
    val cellTransform: js.Function2[js.Any, Int, js.Any] =
      (_, i) => s"translate(${cellX(i)}, $cellY)"
    val _ =
      if animate then
        cellAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", cellTransform)
      else cellAll.attr("transform", cellTransform)

    val _ = cellAll
      .select("text.array-traversal__cell-label")
      .text(((d, _) => d): js.Function2[js.Any, Int, js.Any])
    val _ = cellAll
      .select("text.array-traversal__cell-index")
      .text(((_, i) => js.Dynamic.global.String(i)): js.Function2[js.Any, Int, js.Any])

    val inRangeFn: js.Function2[js.Any, Int, Boolean] = (_, i) =>
      step.range.exists(r => i >= r.lo && i <= r.hi)
    val _ = cellAll
      .select("rect.array-traversal__cell")
      .classed("array-traversal__cell--in-range", inRangeFn)

    // ---- Markers: one <g> per marker, keyed by marker name ------------------
    val markerData: js.Array[js.Any] = step.markers
      .filter(m => m.index >= 0 && m.index < itemCount)
      .zipWithIndex
      .map { case (m, fallbackIdx) =>
        js.Dynamic
          .literal(name = m.name, index = m.index, color = colorFor(m, fallbackIdx))
          .asInstanceOf[js.Any]
      }
      .toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name

    val markerSel = svg
      .selectAll("g.array-traversal__marker")
      .data(markerData, markerKeyFn)

    val laneY = PaddingY + CellSize + 20.0
    val centerOf: (js.Any, Int) => Double =
      (d, _) => cellX(d.asInstanceOf[js.Dynamic].index.asInstanceOf[Int]) + CellSize / 2

    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "array-traversal__marker")
      .attr(
        "transform",
        ((d, i) => s"translate(${centerOf(d, i)}, 0)"): js.Function2[js.Any, Int, js.Any]
      )
    val _ = markerEnter
      .append("path")
      .attr("d", s"M -5 ${laneY + 6} L 5 ${laneY + 6} L 0 ${laneY - 2} Z")
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
    val _ = markerEnter
      .append("text")
      .attr("class", "array-traversal__marker-label")
      .attr("x", 0)
      .attr("y", laneY + MarkerLaneH - 14)
      .attr("text-anchor", "middle")
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])

    val _ = markerSel.exit().remove()

    val markerAll = svg.selectAll("g.array-traversal__marker")
    val markerTransform: js.Function2[js.Any, Int, js.Any] =
      (d, i) => s"translate(${centerOf(d, i)}, 0)"
    val _ =
      if animate then
        markerAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", markerTransform)
      else markerAll.attr("transform", markerTransform)
    val _ = markerAll
      .select("path")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerAll
      .select("text.array-traversal__marker-label")
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
              <.p(^.className   := "d3-widget__error-title", "Array traversal payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = clamp(indexS.value, math.max(1, count))
            val currentStep =
              if count == 0 then Step(None, None, Nil, None, "No steps defined.")
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

            <.div(
              ^.className := "array-traversal not-prose",
              spec.title
                .map(t => <.p(^.className := "array-traversal__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              <.div(
                ^.className := "array-traversal__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "array-traversal__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              <.div(
                ^.className := "array-traversal__controls",
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> previous,
                  ^.disabled   := atStart,
                  ^.aria.label := "Previous step",
                  ^.className  := "array-traversal__button",
                  LucideIcons.ArrowLeft(LucideIcons.withClass("array-traversal__button-icon")),
                  "Prev"
                ),
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> togglePlay,
                  ^.disabled   := count == 0,
                  ^.aria.label := (if playingS.value then "Pause" else "Play"),
                  ^.className  := "array-traversal__button array-traversal__button--primary",
                  if playingS.value then
                    LucideIcons.Pause(LucideIcons.withClass("array-traversal__button-icon"))
                  else LucideIcons.Play(LucideIcons.withClass("array-traversal__button-icon")),
                  if playingS.value then "Pause" else "Play"
                ),
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> next,
                  ^.disabled   := atEnd,
                  ^.aria.label := "Next step",
                  ^.className  := "array-traversal__button",
                  "Next",
                  LucideIcons.ArrowRight(LucideIcons.withClass("array-traversal__button-icon"))
                ),
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> reset,
                  ^.disabled   := atStart && !playingS.value,
                  ^.aria.label := "Reset",
                  ^.className  := "array-traversal__button array-traversal__button--icon",
                  LucideIcons.RotateCcw(LucideIcons.withClass("array-traversal__button-icon"))
                ),
                <.span(
                  ^.className := "array-traversal__progress",
                  s"Step ${idx + 1} / ${math.max(1, count)}"
                )
              )
            )
      }
