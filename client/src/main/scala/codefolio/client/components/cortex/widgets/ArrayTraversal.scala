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
      msg: String,
      // Optional secondary row state for chapters that need two stacked arrays
      // (e.g. simultaneous traversal of two sequences, in-place reverse via temp
      // array). When `secondaryItems` is `None` here AND `Spec.secondaryItems`
      // is `None`, the widget renders a single row exactly as before.
      secondaryItems: Option[List[String]],
      secondaryKeys: Option[List[String]],
      secondaryMarkers: List[Marker],
      secondaryRange: Option[RangeBand]
  )

  final case class Spec(
      items: List[String],
      title: Option[String],
      steps: List[Step],
      primaryLabel: Option[String],
      secondaryItems: Option[List[String]],
      secondaryLabel: Option[String]
  )

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

  private def parseMarkers(arr: js.UndefOr[js.Array[js.Dynamic]]): List[Marker] =
    arr.toOption
      .getOrElse(js.Array())
      .toList
      .map { m =>
        Marker(
          name = m.name.asInstanceOf[js.UndefOr[String]].toOption.getOrElse(""),
          index = m.index.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0),
          color = m.color.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
        )
      }

  private def parseRange(r: js.UndefOr[js.Dynamic]): Option[RangeBand] =
    r.toOption.map { v =>
      RangeBand(
        lo = v.lo.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0),
        hi = v.hi.asInstanceOf[js.UndefOr[Int]].toOption.getOrElse(0)
      )
    }

  private def parseStringArray(arr: js.UndefOr[js.Array[js.Any]]): Option[List[String]] =
    arr.toOption.map(_.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String]))

  private def parsePayload(json: String): Either[String, Spec] =
    Try {
      val raw    = js.JSON.parse(json).asInstanceOf[js.Dynamic]
      val itemsJ = raw.items.asInstanceOf[js.Array[js.Any]]
      val items  = itemsJ.toList.map(v => js.Dynamic.global.String(v).asInstanceOf[String])
      val title  = raw.title.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val primaryLabel =
        raw.primaryLabel.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val secondaryItems =
        parseStringArray(raw.secondaryItems.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
      val secondaryLabel =
        raw.secondaryLabel.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val rawSteps = raw.steps
        .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
        .toOption
        .getOrElse(js.Array())
      val steps = rawSteps.toList.map { s =>
        val mks          = parseMarkers(s.markers.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]])
        val rng          = parseRange(s.range.asInstanceOf[js.UndefOr[js.Dynamic]])
        val perStepItems = parseStringArray(s.items.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
        val perStepKeys  = parseStringArray(s.keys.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
        val secItems =
          parseStringArray(s.secondaryItems.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
        val secKeys =
          parseStringArray(s.secondaryKeys.asInstanceOf[js.UndefOr[js.Array[js.Any]]])
        val secMarkers =
          parseMarkers(s.secondaryMarkers.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]])
        val secRange = parseRange(s.secondaryRange.asInstanceOf[js.UndefOr[js.Dynamic]])
        val msg      = s.msg.asInstanceOf[js.UndefOr[String]].toOption.getOrElse("")
        Step(perStepItems, perStepKeys, mks, rng, msg, secItems, secKeys, secMarkers, secRange)
      }
      Spec(items, title, steps, primaryLabel, secondaryItems, secondaryLabel)
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

  private def singleRowHeight: Double = CellSize + MarkerLaneH + RangeBandH + 6.0

  private def viewBoxHeight(hasSecondary: Boolean): Double =
    PaddingY * 2 + singleRowHeight + (if hasSecondary then RowGap + singleRowHeight else 0.0)

  private val RowGap = 20.0

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
  // Secondary-row helpers. Returns Some(items) when this widget instance has a
  // secondary row (either spec-level default OR per-step override); None means
  // single-row layout.
  // ---------------------------------------------------------------------------

  private def secondaryItemsFor(spec: Spec, step: Step): Option[List[String]] =
    step.secondaryItems.orElse(spec.secondaryItems)

  private def secondaryKeysFor(spec: Spec, step: Step): Option[List[String]] =
    step.secondaryKeys.orElse(secondaryItemsFor(spec, step))

  private def specHasSecondary(spec: Spec): Boolean =
    spec.secondaryItems.isDefined || spec.steps.exists(_.secondaryItems.isDefined)

  // ---------------------------------------------------------------------------
  // Mount the SVG element inside the host div on first call; return the SVG
  // element for D3 to manipulate. Subsequent calls return the existing SVG.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg          = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      val hasSecondary = specHasSecondary(spec)
      val width = viewBoxWidth(
        math.max(spec.items.size, spec.secondaryItems.fold(0)(_.size))
      )
      val height = viewBoxHeight(hasSecondary)
      svg.setAttribute("class", "array-traversal__svg")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse("Array traversal"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 $width $height")
      // Pin the intrinsic SVG size to the viewBox dimensions so the browser
      // doesn't stretch the SVG to fill its flex parent. CSS `max-w-full
      // h-auto` then shrinks long widgets to fit narrow screens while
      // preserving the aspect ratio. Cells render at their natural 56 px.
      svg.setAttribute("width", width.toString)
      svg.setAttribute("height", height.toString)
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // D3 render — runs after every step change. Idempotent: the first call sets up
  // the SVG structure via enter selections; subsequent calls update positions /
  // text / markers via transitions on the same elements.
  // ---------------------------------------------------------------------------

  // Row identifier used to namespace CSS classes when both primary and secondary
  // rows are present. "primary" maps to the original class names so existing
  // chapters render unchanged; "secondary" gets distinct classes so D3 can pick
  // its own row's elements without bleed-through.
  final private case class RowKey(suffix: String, classSuffix: String)
  private val PrimaryRow   = RowKey("", "")
  private val SecondaryRow = RowKey("--secondary", "--secondary")

  private def renderRow(
      svgEl: dom.Element,
      rowKey: RowKey,
      items: List[String],
      keys: List[String],
      markers: List[Marker],
      range: Option[RangeBand],
      cellY: Double,
      animate: Boolean
  ): Unit =
    val svg                        = D3.select(svgEl)
    val itemCount                  = items.size
    val cellGroupClass             = s"array-traversal__cell-group${rowKey.classSuffix}"
    val markerGroupClass           = s"array-traversal__marker${rowKey.classSuffix}"
    val rangeClass                 = s"array-traversal__range${rowKey.classSuffix}"
    val bandY                      = cellY + CellSize + 18
    val laneY                      = cellY + CellSize + 20.0
    val cellData: js.Array[js.Any] = items.toJSArray.asInstanceOf[js.Array[js.Any]]
    val cellKeys                   = keys.toJSArray
    val cellKeyFn: js.Function2[js.Any, Int, js.Any] =
      (_, i) => if i < cellKeys.length then cellKeys(i) else js.Dynamic.global.String(i)

    // Range band ----------------------------------------------------------------
    val rangeData: js.Array[js.Any] = range
      .filter(r => r.lo <= r.hi && r.lo >= 0 && r.hi < itemCount)
      .toList
      .map(r => js.Dynamic.literal(lo = r.lo, hi = r.hi).asInstanceOf[js.Any])
      .toJSArray
    val rangeJoin = svg
      .selectAll(s"rect.$rangeClass")
      .data(rangeData)
      .join("rect")
      .attr("class", rangeClass)
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

    // Cells --------------------------------------------------------------------
    val cellSel = svg.selectAll(s"g.$cellGroupClass").data(cellData, cellKeyFn)
    val cellEnter = cellSel
      .enter()
      .append("g")
      .attr("class", cellGroupClass)
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

    val cellAll = svg.selectAll(s"g.$cellGroupClass")
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
      range.exists(r => i >= r.lo && i <= r.hi)
    val _ = cellAll
      .select("rect.array-traversal__cell")
      .classed("array-traversal__cell--in-range", inRangeFn)

    // Markers ------------------------------------------------------------------
    // Multiple markers may attach to the same cell index (e.g. `left` and
    // `i` initialised together at index 0). To avoid the labels overlapping,
    // we compute a per-index `rank`: the first marker at an index renders
    // the arrowhead triangle + its label one slot below the row; subsequent
    // markers at the same index stack their labels downward, no extra
    // triangle.
    val activeMarkers = markers.filter(m => m.index >= 0 && m.index < itemCount)
    val rankedMarkers = activeMarkers.zipWithIndex.map { case (m, idx) =>
      val rank = activeMarkers.take(idx).count(_.index == m.index)
      (m, rank)
    }
    val markerData: js.Array[js.Any] = rankedMarkers.zipWithIndex
      .map { case ((m, rank), fallbackIdx) =>
        js.Dynamic
          .literal(name = m.name, index = m.index, color = colorFor(m, fallbackIdx), rank = rank)
          .asInstanceOf[js.Any]
      }
      .toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name
    val markerSel = svg.selectAll(s"g.$markerGroupClass").data(markerData, markerKeyFn)
    val centerOf: (js.Any, Int) => Double =
      (d, _) => cellX(d.asInstanceOf[js.Dynamic].index.asInstanceOf[Int]) + CellSize / 2

    val markerLaneRowH = 14.0
    val labelY: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val rank = d.asInstanceOf[js.Dynamic].rank.asInstanceOf[Int]
      (laneY + MarkerLaneH - 14 + rank * markerLaneRowH).toString
    val trianglePathFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val rank = d.asInstanceOf[js.Dynamic].rank.asInstanceOf[Int]
      if rank == 0 then s"M -5 ${laneY + 6} L 5 ${laneY + 6} L 0 ${laneY - 2} Z"
      else ""

    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", markerGroupClass)
      .attr(
        "transform",
        ((d, i) => s"translate(${centerOf(d, i)}, 0)"): js.Function2[js.Any, Int, js.Any]
      )
    val _ = markerEnter
      .append("path")
      .attr("d", trianglePathFn)
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
    val _ = markerEnter
      .append("text")
      .attr("class", "array-traversal__marker-label")
      .attr("x", 0)
      .attr("y", labelY)
      .attr("text-anchor", "middle")
      .attr(
        "fill",
        ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any]
      )
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])
    val _ = markerSel.exit().remove()

    val markerAll = svg.selectAll(s"g.$markerGroupClass")
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
      .attr("d", trianglePathFn)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerAll
      .select("text.array-traversal__marker-label")
      .attr("y", labelY)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(((d, _) => d.asInstanceOf[js.Dynamic].name): js.Function2[js.Any, Int, js.Any])

  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg          = D3.select(svgEl)
    val items        = itemsFor(spec, step)
    val keys         = keysFor(spec, step)
    val itemCount    = items.size
    val secItems     = secondaryItemsFor(spec, step)
    val hasSecondary = specHasSecondary(spec)
    val width = viewBoxWidth(
      math.max(itemCount, secItems.fold(0)(_.size))
    )
    val totalH = viewBoxHeight(hasSecondary)
    val _      = svg.attr("viewBox", s"0 0 $width $totalH")

    // Empty-step placeholder: when both rows are empty, render "(empty
    // array)" centred so the SVG canvas isn't a blank rectangle. Mirrors
    // the same protection in the `linked-list` widget.
    val bothRowsEmpty = itemCount == 0 && secItems.forall(_.isEmpty)
    if bothRowsEmpty then
      val ensureCanvasW            = math.max(width, 240.0)
      val _                        = svg.attr("viewBox", s"0 0 $ensureCanvasW $totalH")
      val _                        = svg.selectAll("g.array-traversal__cell-group").remove()
      val _                        = svg.selectAll("g.array-traversal__cell-group--secondary").remove()
      val _                        = svg.selectAll("g.array-traversal__marker").remove()
      val _                        = svg.selectAll("g.array-traversal__marker--secondary").remove()
      val _                        = svg.selectAll("rect.array-traversal__range").remove()
      val _                        = svg.selectAll("rect.array-traversal__range--secondary").remove()
      val phData: js.Array[js.Any] = js.Array("(empty array)").asInstanceOf[js.Array[js.Any]]
      val phKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "ph"
      val phSel = svg.selectAll("text.array-traversal__empty-placeholder").data(phData, phKeyFn)
      val _ = phSel
        .enter()
        .append("text")
        .attr("class", "array-traversal__empty-placeholder")
        .attr("text-anchor", "middle")
        .attr("x", ensureCanvasW / 2)
        .attr("y", PaddingY + CellSize / 2 + 6)
        .text("(empty array)")
      val _ = svg
        .selectAll("text.array-traversal__empty-placeholder")
        .attr("x", ensureCanvasW / 2)
        .attr("y", PaddingY + CellSize / 2 + 6)
      return ()
    else
      val _ = svg.selectAll("text.array-traversal__empty-placeholder").remove()

    renderRow(svgEl, PrimaryRow, items, keys, step.markers, step.range, PaddingY, animate)

    secItems match
      case Some(secondary) =>
        val secKeys  = secondaryKeysFor(spec, step).getOrElse(secondary)
        val secCellY = PaddingY + singleRowHeight + RowGap
        renderRow(
          svgEl,
          SecondaryRow,
          secondary,
          secKeys,
          step.secondaryMarkers,
          step.secondaryRange,
          secCellY,
          animate
        )
      case None =>
        // Tear down any secondary-row elements left over from a previous render
        // pass (shouldn't happen since secondary presence is fixed per Spec, but
        // belt-and-braces).
        val _ = svg.selectAll("g.array-traversal__cell-group--secondary").remove()
        val _ = svg.selectAll("g.array-traversal__marker--secondary").remove()
        val _ = svg.selectAll("rect.array-traversal__range--secondary").remove()

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
              if count == 0 then Step(None, None, Nil, None, "No steps defined.", None, None, Nil, None)
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
              controls
            )
      }
