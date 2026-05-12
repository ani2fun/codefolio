package codefolio.client.components.cortex.widgets

import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Array traversal stepper — the first widget in the D3 catalog. Renders a row of cells with one or more named
 * markers per step and an optional highlighted range. Step controls (prev/next/play/reset) scrub through the
 * author-defined steps.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "items":  ["1", "3", "5", "7", "9", "11", "13"],
 *   "title":  "Binary search for 11",
 *   "steps":  [
 *     { "markers": [{"name": "lo", "index": 0}, {"name": "mid", "index": 3}, {"name": "hi", "index": 6}],
 *       "range":   { "lo": 0, "hi": 6 },
 *       "msg":     "mid=7 ≠ 11; go right" },
 *     ...
 *   ]
 * }
 * }}}
 *
 * SVG is built as a string and injected via `dangerouslySetInnerHTML` — same pattern Mermaid + D2 use. The
 * scalajs-react VDOM doesn't include SVG tags under the `html_<^` import that the rest of this package uses,
 * and mixing namespaces churns the build for very little gain on a fixed-cardinality layout. CSS transitions
 * give us smooth interpolation between steps without imperative D3 selections.
 */
object ArrayTraversal:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose.
  // ---------------------------------------------------------------------------

  final case class Marker(name: String, index: Int, color: Option[String])
  final case class RangeBand(lo: Int, hi: Int)
  final case class Step(markers: List[Marker], range: Option[RangeBand], msg: String)
  final case class Spec(items: List[String], title: Option[String], steps: List[Step])

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Layout constants. Chosen so the widget fits comfortably inside a chapter's
  // prose column without horizontal scrolling for arrays up to ~12 elements.
  // ---------------------------------------------------------------------------

  private val CellSize     = 56.0
  private val CellGap      = 6.0
  private val MarkerLaneH  = 44.0
  private val RangeBandH   = 8.0
  private val PaddingX     = 16.0
  private val PaddingY     = 12.0
  private val StepDelayMs  = 1200
  private val DefaultColor = "#3b82f6"

  private val PaletteByIndex = Vector(
    "#3b82f6", // lo / first
    "#10b981", // mid
    "#f59e0b", // hi
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
      val raw      = js.JSON.parse(json).asInstanceOf[js.Dynamic]
      val itemsJ   = raw.items.asInstanceOf[js.Array[js.Any]]
      val items    = itemsJ.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String])
      val title    = raw.title.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val rawSteps = raw.steps.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]].toOption.getOrElse(js.Array())
      val steps = rawSteps.toList.map { s =>
        val mks = s.markers.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]].toOption
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
        val msg = s.msg.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        Step(mks, rng, msg)
      }
      Spec(items, title, steps)
    } match
      case Success(spec) if spec.items.isEmpty => Left("payload.items must be non-empty")
      case Success(spec)                       => Right(spec)
      case Failure(t)                          => Left(Option(t.getMessage).getOrElse("invalid payload JSON"))

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

  // Escape values that land inside SVG attribute strings / text nodes. Items
  // are author-controlled and not expected to contain markup, but a stray `<`
  // or `&` should not break the diagram.
  private def esc(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

  // ---------------------------------------------------------------------------
  // SVG string-building. Range band sits behind cells; markers sit in front so
  // labels never disappear.
  // ---------------------------------------------------------------------------

  private def rangeBandSvg(range: Option[RangeBand], itemCount: Int, y: Double): String =
    range.filter(r => r.lo <= r.hi && r.lo >= 0 && r.hi < itemCount) match
      case None => ""
      case Some(r) =>
        val x  = cellX(r.lo)
        val xR = cellX(r.hi) + CellSize
        s"""<rect class="array-traversal__range" x="$x" y="$y" width="${xR - x}" height="$RangeBandH" rx="3"/>"""

  private def cellsSvg(items: List[String], range: Option[RangeBand]): String =
    val cellY = PaddingY
    items.zipWithIndex
      .map { case (label, i) =>
        val inRange = range.exists(r => i >= r.lo && i <= r.hi)
        val cls =
          if inRange then "array-traversal__cell array-traversal__cell--in-range"
          else "array-traversal__cell"
        s"""<g>
           |  <rect class="$cls" x="${cellX(i)}" y="$cellY" width="$CellSize" height="$CellSize" rx="6"/>
           |  <text class="array-traversal__cell-label" x="${cellX(
            i
          ) + CellSize / 2}" y="${cellY + CellSize / 2 + 5}" text-anchor="middle">${esc(label)}</text>
           |  <text class="array-traversal__cell-index" x="${cellX(
            i
          ) + CellSize / 2}" y="${cellY + CellSize + 12}" text-anchor="middle">$i</text>
           |</g>""".stripMargin
      }
      .mkString("\n")

  private def markersSvg(markers: List[Marker], itemCount: Int): String =
    val laneY = PaddingY + CellSize + 20.0
    markers.zipWithIndex
      .flatMap { case (m, fallbackIdx) =>
        if m.index < 0 || m.index >= itemCount then None
        else
          val color = colorFor(m, fallbackIdx)
          val cx    = cellX(m.index) + CellSize / 2
          Some(
            s"""<g class="array-traversal__marker">
               |  <path d="M ${cx - 5} ${laneY + 6} L ${cx + 5} ${laneY + 6} L $cx ${laneY - 2} Z" fill="$color"/>
               |  <text class="array-traversal__marker-label" x="$cx" y="${laneY + MarkerLaneH - 14}" text-anchor="middle" fill="$color">${esc(
                m.name
              )}</text>
               |</g>""".stripMargin
          )
      }
      .mkString("\n")

  private def buildSvg(spec: Spec, step: Step): String =
    val bandY = PaddingY + CellSize + 18
    s"""<svg viewBox="0 0 ${viewBoxWidth(spec.items.size)} $viewBoxHeight"
       |     class="array-traversal__svg" role="img"
       |     aria-label="${esc(spec.title.getOrElse("Array traversal"))}"
       |     xmlns="http://www.w3.org/2000/svg">
       |  ${rangeBandSvg(step.range, spec.items.size, bandY)}
       |  ${cellsSvg(spec.items, step.range)}
       |  ${markersSvg(step.markers, spec.items.size)}
       |</svg>""".stripMargin

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .useState(0)
      .useState(false)
      .useRefBy(_ => Option.empty[Int])
      .useEffectWithDepsBy((_, specM, indexS, playingS, _) =>
        (specM.value.toOption.fold(0)(_.steps.size), indexS.value, playingS.value)
      ) { (_, _, indexS, playingS, timeoutRef) => (count, index, playing) =>
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
      .render { (_, specM, indexS, playingS, _) =>
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
              if count == 0 then Step(Nil, None, "No steps defined.")
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
                ^.className               := "array-traversal__frame",
                ^.dangerouslySetInnerHtml := buildSvg(spec, currentStep)
              ),
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
