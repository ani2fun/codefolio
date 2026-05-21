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

  // Marker — a single case class with optional locator fields keeps the parser
  // and the renderer dispatch shallow. The widget's `Spec.layout` flag is the
  // *only* dispatch: 1D + flat-with-2d-overlay use `index`; 2D uses `(row, col)`.
  // `canonical` is set by the parser via `MarkerCanon.isCanonical(name)` and
  // drives the warning treatment in the renderer (non-canonical names render
  // with the canon's warning colour and a `⚠` prefix on the label).
  final case class Marker(
      name: String,
      index: Option[Int],
      row: Option[Int],
      col: Option[Int],
      canonical: Boolean
  )

  final case class RangeBand(lo: Int, hi: Int)
  final case class RegionRange(rowLo: Int, rowHi: Int, colLo: Int, colHi: Int)
  final case class RowColAnnotation(row: Int, col: Int)

  // Optional size bracket drawn ABOVE the cell row. Renders a horizontal line
  // with perpendicular end-ticks spanning all cells + a centered text label.
  // Used by chapters that introduce arrays ("size = 7" bracket above an array
  // of 7 cells). 1D layout only.
  final case class SizeAnnotation(label: String)

  // Per-cell text color override for a single step. Used by chapters that
  // highlight modified cells in green ("Array elements can be modified via
  // their indices" — value2 and value3 colored emerald).
  final case class CellHighlight(index: Int, color: String)

  final case class Step(
      items: Option[List[String]],
      keys: Option[List[String]],
      // 2D per-step grid override. When `None` the widget falls back to
      // `Spec.rows`. Only consulted in `layout:"2d"` mode.
      rows: Option[List[List[String]]],
      markers: List[Marker],
      range: Option[RangeBand],
      // 2D region highlight. Replaces `range` for `layout:"2d"`.
      regionRange: Option[RegionRange],
      // flat-with-2d-overlay per-step (row, col) annotation rendered under the
      // marker's cell so the (row, col) interpretation of the flat physical
      // index is legible.
      rowColAnnotation: Option[RowColAnnotation],
      msg: String,
      // Optional per-cell text-color overrides for this step. Empty list = no
      // overrides. Cells not listed keep the default cell-label color.
      cellHighlights: List[CellHighlight],
      // Optional secondary row state for chapters that need two stacked arrays
      // (e.g. simultaneous traversal of two sequences, in-place reverse via temp
      // array). When `secondaryItems` is `None` here AND `Spec.secondaryItems`
      // is `None`, the widget renders a single row exactly as before. Only
      // meaningful in `layout:"1d"`.
      secondaryItems: Option[List[String]],
      secondaryKeys: Option[List[String]],
      secondaryMarkers: List[Marker],
      secondaryRange: Option[RangeBand]
  )

  final case class Spec(
      // "1d" (default) | "2d" | "flat-with-2d-overlay". Unknown values collapse
      // to "1d" in the parser, mirroring StackQueue's `mode` collapse.
      layout: String,
      items: List[String],
      // 2D mode payload. Empty for non-2D layouts.
      rows: List[List[String]],
      // flat-with-2d-overlay partition width. Required when layout is overlay,
      // ignored otherwise.
      cols: Option[Int],
      title: Option[String],
      steps: List[Step],
      primaryLabel: Option[String],
      secondaryItems: Option[List[String]],
      secondaryLabel: Option[String],
      // Optional size bracket above the cell row. 1D layout only; ignored in
      // 2D and flat-with-2d-overlay modes.
      sizeAnnotation: Option[SizeAnnotation]
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
  private val SvgNs                = "http://www.w3.org/2000/svg"
  // Vertical space reserved above the cell row when sizeAnnotation is set.
  // Bracket line + perpendicular ticks + label fits in this band.
  private val SizeBracketSpace = 26.0
  // Vertical space reserved above each row when row labels (primaryLabel /
  // secondaryLabel) are rendered. Only added when secondaryItems is present.
  private val RowLabelSpace = 18.0
  // Default highlight color when a CellHighlight entry omits `color`. Emerald
  // matches the source's "modified cell" convention (value2/value3 in green).
  private val DefaultHighlightColor = "#10b981"

  // Layout discriminators on `Spec.layout`. Three closed values; anything else
  // collapses to `LayoutOneDee` in `parsePayload`.
  private val LayoutOneDee  = "1d"
  private val LayoutTwoDee  = "2d"
  private val LayoutOverlay = "flat-with-2d-overlay"

  // Minimal inline-markdown: `**bold**` → `<strong>bold</strong>` and
  // `` `code` `` → `<code>code</code>`. The input is HTML-escaped first so
  // payload-supplied text cannot inject arbitrary markup. Used to bold key
  // phrases inside step `msg` captions (e.g. `**size 7**`, `**address 3**`).
  private def renderInlineMarkdown(text: String): String =
    val escaped = text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
    val boldRe      = """\*\*(.+?)\*\*""".r
    val codeRe      = """`([^`]+?)`""".r
    val boldApplied = boldRe.replaceAllIn(escaped, m => s"<strong>${m.group(1)}</strong>")
    val full        = codeRe.replaceAllIn(boldApplied, m => s"<code>${m.group(1)}</code>")
    full

  // ---------------------------------------------------------------------------
  // Parsing — decoder lambda reads typed fields off `js.Dynamic`; any thrown
  // exception (missing field, validation failure) collapses to a `Left(msg)`
  // via `PayloadDecoder.run`, and the component renders an inline error
  // placeholder rather than crashing the chapter.
  // ---------------------------------------------------------------------------

  // Marker parser — canon-aware. Mirrors StackQueue.parseMarker: name checked
  // against MarkerCanon.isCanonical; author-supplied `color` field silently
  // dropped (canon owns colour) but warned so authors see they should remove it;
  // non-canonical names warned so typos surface immediately.
  private def parseMarker(d: js.Dynamic): Marker =
    val name      = d.string("name")
    val rawColor  = d.optString("color")
    val canonical = MarkerCanon.isCanonical(name)
    if rawColor.isDefined then MarkerCanon.warnAuthorColor("array-traversal", name)
    if name.nonEmpty && !canonical then MarkerCanon.warnUnknown("array-traversal", name)
    Marker(
      name = name,
      index = d.optInt("index"),
      row = d.optInt("row"),
      col = d.optInt("col"),
      canonical = canonical
    )

  private def parseRange(d: js.Dynamic): RangeBand =
    RangeBand(lo = d.int("lo"), hi = d.int("hi"))

  private def parseRegionRange(d: js.Dynamic): RegionRange =
    RegionRange(
      rowLo = d.int("rowLo"),
      rowHi = d.int("rowHi"),
      colLo = d.int("colLo"),
      colHi = d.int("colHi")
    )

  private def parseRowColAnnotation(d: js.Dynamic): RowColAnnotation =
    RowColAnnotation(row = d.int("row"), col = d.int("col"))

  private def parseSizeAnnotation(d: js.Dynamic): SizeAnnotation =
    SizeAnnotation(label = d.string("label"))

  private def parseCellHighlight(d: js.Dynamic): CellHighlight =
    CellHighlight(
      index = d.int("index"),
      color = d.optString("color").getOrElse(DefaultHighlightColor)
    )

  private def parseStep(d: js.Dynamic): Step =
    Step(
      items = d.stringList("items"),
      keys = d.stringList("keys"),
      rows = d.stringMatrix("rows"),
      markers = d.dynList("markers").map(parseMarker),
      range = d.optObj("range").map(parseRange),
      regionRange = d.optObj("regionRange").map(parseRegionRange),
      rowColAnnotation = d.optObj("rowColAnnotation").map(parseRowColAnnotation),
      msg = d.string("msg"),
      cellHighlights = d.dynList("cellHighlights").map(parseCellHighlight),
      secondaryItems = d.stringList("secondaryItems"),
      secondaryKeys = d.stringList("secondaryKeys"),
      secondaryMarkers = d.dynList("secondaryMarkers").map(parseMarker),
      secondaryRange = d.optObj("secondaryRange").map(parseRange)
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawLayout = d.optString("layout").getOrElse(LayoutOneDee)
      val layout = rawLayout match
        case LayoutTwoDee | LayoutOverlay => rawLayout
        case _                            => LayoutOneDee

      val items = d.stringList("items").getOrElse(Nil)
      val rows  = d.stringMatrix("rows").getOrElse(Nil)
      val cols  = d.optInt("cols").filter(_ > 0)

      layout match
        case LayoutTwoDee =>
          if rows.isEmpty || rows.exists(_.isEmpty) then
            throw PayloadDecoder.invalid("rows must be a non-empty matrix when layout=\"2d\"")
          if rows.map(_.size).distinct.size > 1 then
            throw PayloadDecoder.invalid("rows must have uniform width (every row same length)")
        case LayoutOverlay =>
          val colsVal = cols.getOrElse(
            throw PayloadDecoder.invalid("cols (>0) is required when layout=\"flat-with-2d-overlay\"")
          )
          if items.isEmpty then
            throw PayloadDecoder.invalid("items must be non-empty for flat-with-2d-overlay")
          if items.size % colsVal != 0 then
            throw PayloadDecoder.invalid(
              s"items.size (${items.size}) must be divisible by cols ($colsVal)"
            )
        case _ =>
          if items.isEmpty then throw PayloadDecoder.invalid("items must be non-empty for layout=\"1d\"")

      Spec(
        layout = layout,
        items = items,
        rows = rows,
        cols = cols,
        title = d.optString("title"),
        steps = d.dynList("steps").map(parseStep),
        primaryLabel = d.optString("primaryLabel"),
        secondaryItems = d.stringList("secondaryItems"),
        secondaryLabel = d.optString("secondaryLabel"),
        sizeAnnotation = d.optObj("sizeAnnotation").map(parseSizeAnnotation)
      )
    }

  // ---------------------------------------------------------------------------
  // Layout helpers
  // ---------------------------------------------------------------------------

  private def cellX(index: Int): Double =
    PaddingX + index * (CellSize + CellGap)

  private def viewBoxWidth(itemCount: Int): Double =
    PaddingX * 2 + itemCount * CellSize + (itemCount - 1).max(0) * CellGap

  private def singleRowHeight: Double = CellSize + MarkerLaneH + RangeBandH + 6.0

  // Per-spec top padding — bumps cell-row down when sizeAnnotation is set so
  // the bracket fits above the cells. Also reserves space when row labels
  // (only rendered for two-row chapters) are present.
  private def topOf(spec: Spec): Double =
    PaddingY +
      (if spec.sizeAnnotation.isDefined then SizeBracketSpace else 0.0) +
      (if spec.secondaryItems.isDefined && (spec.primaryLabel.isDefined || spec.secondaryLabel.isDefined)
       then RowLabelSpace
       else 0.0)

  private def viewBoxHeight(
      hasSecondary: Boolean,
      hasSize: Boolean = false,
      hasLabels: Boolean = false
  ): Double =
    PaddingY * 2 + singleRowHeight +
      (if hasSecondary then RowGap + singleRowHeight else 0.0) +
      (if hasSize then SizeBracketSpace else 0.0) +
      (if hasSecondary && hasLabels then RowLabelSpace * 2 else 0.0)

  private val RowGap = 20.0

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
  // 2D-mode layout. Grid cells positioned at (gridX(col), gridY(row)).
  // GridRowGap matches the horizontal CellGap so rows and columns share the
  // same visual rhythm. OverlayAnnotationDy is the vertical offset of the
  // flat-with-2d-overlay (row, col) annotation below the cell-index baseline.
  // ---------------------------------------------------------------------------

  private val GridRowGap = CellGap
  // Vertical offset of the (row, col) annotation below the cell-index baseline.
  // Sized to drop the annotation below the marker triangle + label so the
  // marker chevron doesn't overlap the "(row, col)" text. Pairs with a
  // viewBox-height bump in renderFlatWithOverlay.
  private val OverlayAnnotationDy = 54.0

  private def gridX(col: Int): Double = PaddingX + col * (CellSize + CellGap)
  private def gridY(row: Int): Double = PaddingY + row * (CellSize + GridRowGap)

  private def gridViewBoxWidth(colsN: Int): Double =
    PaddingX * 2 + colsN * CellSize + (colsN - 1).max(0) * CellGap

  private def gridViewBoxHeight(rowsN: Int): Double =
    PaddingY * 2 + rowsN * CellSize + (rowsN - 1).max(0) * GridRowGap

  private def rowsFor(spec: Spec, step: Step): List[List[String]] =
    step.rows.getOrElse(spec.rows)

  // ---------------------------------------------------------------------------
  // Mount the SVG element inside the host div on first call; return the SVG
  // element for D3 to manipulate. Subsequent calls return the existing SVG.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      val (width, height) = spec.layout match
        case LayoutTwoDee =>
          val rowsN = spec.rows.size.max(1)
          val colsN = spec.rows.headOption.fold(1)(_.size).max(1)
          (gridViewBoxWidth(colsN), gridViewBoxHeight(rowsN))
        case LayoutOverlay =>
          // Overlay needs extra vertical room for the (row, col) annotation
          // that sits below the marker label.
          (viewBoxWidth(spec.items.size), viewBoxHeight(false) + OverlayAnnotationDy)
        case _ =>
          val hasSec    = specHasSecondary(spec)
          val hasLabels = spec.primaryLabel.isDefined || spec.secondaryLabel.isDefined
          val w         = viewBoxWidth(math.max(spec.items.size, spec.secondaryItems.fold(0)(_.size)))
          (w, viewBoxHeight(hasSec, spec.sizeAnnotation.isDefined, hasLabels))
      svg.setAttribute("class", s"array-traversal__svg array-traversal__svg--${spec.layout}")
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
      animate: Boolean,
      cellHighlights: List[CellHighlight] = Nil,
      label: Option[String] = None
  ): Unit =
    val svg              = D3.select(svgEl)
    val itemCount        = items.size
    val cellGroupClass   = s"array-traversal__cell-group${rowKey.classSuffix}"
    val markerGroupClass = s"array-traversal__marker${rowKey.classSuffix}"
    val rangeClass       = s"array-traversal__range${rowKey.classSuffix}"
    val labelClass       = s"array-traversal__row-label${rowKey.classSuffix}"
    val bandY            = cellY + CellSize + 18
    val laneY            = cellY + CellSize + 20.0

    // Row label (drawn just above the first cell when `label` is set) ----------
    // Non-invasive: doesn't shift cells. Uses `text-anchor: start` at the same
    // x as cell 0. Renders as italic `arr_name =` annotation.
    val labelData: js.Array[js.Any] = label.toList.toJSArray.asInstanceOf[js.Array[js.Any]]
    val labelJoin = svg
      .selectAll(s"text.$labelClass")
      .data(labelData)
      .join("text")
      .attr("class", labelClass)
      .attr("x", cellX(0))
      .attr("y", cellY - 6)
      .attr("text-anchor", "start")
      .attr("font-style", "italic")
      .attr("font-size", "13")
      .attr("fill", "#64748b")
    val labelTextFn2: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[String] + " ="
    val _                          = labelJoin.text(labelTextFn2)
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
    // Per-cell text-color override from this step's cellHighlights. Cells not
    // listed fall back to the CSS default (var(--color-fg-default)). Setting
    // fill to the explicit default hex preserves the highlight-then-unhighlight
    // step transitions cleanly.
    val cellLabelFillFn: js.Function2[js.Any, Int, js.Any] = (_, i) =>
      cellHighlights.find(_.index == i).map(_.color).getOrElse("#0a0f25")
    val _ = cellAll
      .select("text.array-traversal__cell-label")
      .attr("fill", cellLabelFillFn)
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
    // we compute a per-index `rank`: the first canonical marker at an index
    // renders the arrowhead triangle + its label one slot below the row;
    // subsequent markers at the same index stack their labels downward, no
    // extra triangle. Non-canonical names (typos, names not in MarkerCanon)
    // suppress the triangle entirely and prefix the label with ⚠ — the
    // warning colour comes from `MarkerCanon.colorFor` via the unknown-name
    // fallback.
    val activeMarkers = markers.filter(m => m.index.exists(i => i >= 0 && i < itemCount))
    val rankedMarkers = activeMarkers.zipWithIndex.map { case (m, idx) =>
      val rank = activeMarkers.take(idx).count(_.index == m.index)
      (m, rank)
    }
    val markerData: js.Array[js.Any] = rankedMarkers
      .map { case (m, rank) =>
        js.Dynamic
          .literal(
            name = m.name,
            index = m.index.getOrElse(-1),
            color = MarkerCanon.colorFor(m.name),
            rank = rank,
            canonical = m.canonical
          )
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
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val rank      = dyn.rank.asInstanceOf[Int]
      if !canonical || rank != 0 then ""
      else s"M -5 ${laneY + 6} L 5 ${laneY + 6} L 0 ${laneY - 2} Z"
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val name      = dyn.name.asInstanceOf[String]
      if canonical then name else s"⚠ $name"

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
      .text(labelTextFn)
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
      .text(labelTextFn)

  // Render dispatcher. `Spec.layout` is the only branch; 2D and overlay each get
  // their own renderer, 1D keeps the secondary-row + empty-placeholder logic
  // unchanged in `renderOneDee`. Each renderer tears down stale elements from
  // other layouts at its top so dev-mode hot-reload across layout edits doesn't
  // leak DOM nodes.
  private def renderStep(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    spec.layout match
      case LayoutTwoDee  => renderGrid(svgEl, spec, step, animate)
      case LayoutOverlay => renderFlatWithOverlay(svgEl, spec, step, animate)
      case _             => renderOneDee(svgEl, spec, step, animate)

  // ---------------------------------------------------------------------------
  // 1D renderer — the original `renderStep` body, lifted verbatim. Handles
  // empty-state placeholder + primary/secondary row dispatch.
  // ---------------------------------------------------------------------------

  // Size bracket — horizontal line with perpendicular end-ticks spanning all
  // cells + a centered text label, drawn above the cell row at PaddingY. Only
  // emitted when spec.sizeAnnotation is set (1D layout only).
  private def renderSizeBracket(
      svg: D3.Selection,
      spec: Spec,
      itemCount: Int,
      animate: Boolean
  ): Unit =
    spec.sizeAnnotation match
      case None =>
        val _ = svg.selectAll("g.array-traversal__size-bracket").remove()
      case Some(sa) =>
        if itemCount <= 0 then
          val _ = svg.selectAll("g.array-traversal__size-bracket").remove()
        else
          val leftX   = cellX(0)
          val rightX  = cellX(itemCount - 1) + CellSize
          val baseY   = PaddingY + SizeBracketSpace - 8.0
          val tickH   = 5.0
          val centerX = (leftX + rightX) / 2.0
          val data: js.Array[js.Any] = js
            .Array(
              js.Dynamic
                .literal(
                  leftX = leftX,
                  rightX = rightX,
                  centerX = centerX,
                  baseY = baseY,
                  tickH = tickH,
                  label = sa.label
                )
                .asInstanceOf[js.Any]
            )
          val keyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "bracket"
          val sel = svg.selectAll("g.array-traversal__size-bracket").data(data, keyFn)
          val enter = sel
            .enter()
            .append("g")
            .attr("class", "array-traversal__size-bracket")
          // Main horizontal line.
          val _ = enter
            .append("line")
            .attr("class", "array-traversal__size-bracket-line")
            .attr("x1", leftX)
            .attr("y1", baseY)
            .attr("x2", rightX)
            .attr("y2", baseY)
          // Left tick (perpendicular).
          val _ = enter
            .append("line")
            .attr("class", "array-traversal__size-bracket-tick")
            .attr("x1", leftX)
            .attr("y1", baseY - tickH)
            .attr("x2", leftX)
            .attr("y2", baseY + tickH)
          // Right tick.
          val _ = enter
            .append("line")
            .attr("class", "array-traversal__size-bracket-tick")
            .attr("x1", rightX)
            .attr("y1", baseY - tickH)
            .attr("x2", rightX)
            .attr("y2", baseY + tickH)
          // Centered label sitting just above the line.
          val _ = enter
            .append("text")
            .attr("class", "array-traversal__size-bracket-label")
            .attr("x", centerX)
            .attr("y", baseY - 6.0)
            .attr("text-anchor", "middle")
            .text(sa.label)
          val _ = sel.exit().remove()
          // Update path/positions on existing bracket (in case items count shifted between steps).
          val all = svg.selectAll("g.array-traversal__size-bracket")
          val _ = all.select("line.array-traversal__size-bracket-line")
            .attr("x1", leftX).attr("y1", baseY).attr("x2", rightX).attr("y2", baseY)
          val _ = all.selectAll("line.array-traversal__size-bracket-tick")
          // Only update text content; the tick positions are static for this render.
          val _ = all.select("text.array-traversal__size-bracket-label")
            .attr("x", centerX).attr("y", baseY - 6.0).text(sa.label)

  private def renderOneDee(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg = D3.select(svgEl)
    // Belt-and-braces: tear down any grid / overlay elements left over from a
    // previous layout (only matters during dev when the author edits a payload
    // and the layout flips).
    val _            = svg.selectAll("g.array-traversal__cell-group--grid").remove()
    val _            = svg.selectAll("g.array-traversal__marker--grid").remove()
    val _            = svg.selectAll("rect.array-traversal__region").remove()
    val _            = svg.selectAll("text.array-traversal__overlay-annotation").remove()
    val items        = itemsFor(spec, step)
    val keys         = keysFor(spec, step)
    val itemCount    = items.size
    val secItems     = secondaryItemsFor(spec, step)
    val hasSecondary = specHasSecondary(spec)
    val hasSize      = spec.sizeAnnotation.isDefined
    val hasLabels    = spec.primaryLabel.isDefined || spec.secondaryLabel.isDefined
    val width = viewBoxWidth(
      math.max(itemCount, secItems.fold(0)(_.size))
    )
    val totalH = viewBoxHeight(hasSecondary, hasSize, hasLabels)
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

    // Size bracket (above cells) when sizeAnnotation is set on the spec.
    // Drawn before cells so the cell rects (drawn by renderRow) sit on top.
    val cellTop = topOf(spec)
    renderSizeBracket(svg, spec, itemCount, animate)

    // Show row labels only when a secondary row is present — single-row
    // chapters don't need an "arr =" annotation.
    val showLabels        = secItems.isDefined
    val primaryLabel      = if showLabels then spec.primaryLabel else None
    val secondaryLabelOpt = if showLabels then spec.secondaryLabel else None
    renderRow(
      svgEl,
      PrimaryRow,
      items,
      keys,
      step.markers,
      step.range,
      cellTop,
      animate,
      step.cellHighlights,
      primaryLabel
    )

    secItems match
      case Some(secondary) =>
        val secKeys  = secondaryKeysFor(spec, step).getOrElse(secondary)
        val secCellY = cellTop + singleRowHeight + RowGap
        renderRow(
          svgEl,
          SecondaryRow,
          secondary,
          secKeys,
          step.secondaryMarkers,
          step.secondaryRange,
          secCellY,
          animate,
          Nil,
          secondaryLabelOpt
        )
      case None =>
        // Tear down any secondary-row elements left over from a previous render
        // pass (shouldn't happen since secondary presence is fixed per Spec, but
        // belt-and-braces).
        val _ = svg.selectAll("g.array-traversal__cell-group--secondary").remove()
        val _ = svg.selectAll("g.array-traversal__marker--secondary").remove()
        val _ = svg.selectAll("rect.array-traversal__range--secondary").remove()

  // ---------------------------------------------------------------------------
  // 2D renderer — row × column grid for layout:"2d". Cells keyed by
  // ${row}-${col}-${value}; markers keyed by name; one regionRange rect per
  // step (drawn under cells). Marker labels sit above the cell like StackQueue
  // queue-mode.
  // ---------------------------------------------------------------------------

  private def renderGrid(svgEl: dom.Element, spec: Spec, step: Step, animate: Boolean): Unit =
    val svg = D3.select(svgEl)
    // Tear down stale 1D / overlay elements (dev hot-reload guard).
    val _      = svg.selectAll("g.array-traversal__cell-group").remove()
    val _      = svg.selectAll("g.array-traversal__cell-group--secondary").remove()
    val _      = svg.selectAll("g.array-traversal__marker").remove()
    val _      = svg.selectAll("g.array-traversal__marker--secondary").remove()
    val _      = svg.selectAll("rect.array-traversal__range").remove()
    val _      = svg.selectAll("rect.array-traversal__range--secondary").remove()
    val _      = svg.selectAll("text.array-traversal__empty-placeholder").remove()
    val _      = svg.selectAll("text.array-traversal__overlay-annotation").remove()
    val grid   = rowsFor(spec, step)
    val rowsN  = grid.size
    val colsN  = grid.headOption.fold(0)(_.size)
    val width  = gridViewBoxWidth(colsN.max(1))
    val height = gridViewBoxHeight(rowsN.max(1))
    val _      = svg.attr("viewBox", s"0 0 $width $height")

    // ── Region-range highlight (one rect per step, drawn UNDER cells) ───────
    val rrData: js.Array[js.Any] = step.regionRange
      .filter(r =>
        r.rowLo <= r.rowHi && r.colLo <= r.colHi
          && r.rowLo >= 0 && r.rowHi < rowsN
          && r.colLo >= 0 && r.colHi < colsN
      )
      .toList
      .map(r =>
        js.Dynamic
          .literal(
            x = gridX(r.colLo),
            y = gridY(r.rowLo),
            w = (r.colHi - r.colLo + 1) * CellSize + (r.colHi - r.colLo) * CellGap,
            h = (r.rowHi - r.rowLo + 1) * CellSize + (r.rowHi - r.rowLo) * GridRowGap
          )
          .asInstanceOf[js.Any]
      )
      .toJSArray
    val rrKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "region"
    val rrSel = svg.selectAll("rect.array-traversal__region").data(rrData, rrKeyFn)
    val _ = rrSel
      .enter()
      .append("rect")
      .attr("class", "array-traversal__region")
      .attr("rx", 4)
    val rrAll                                  = svg.selectAll("rect.array-traversal__region")
    val rrX: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].x
    val rrY: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].y
    val rrW: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].w
    val rrH: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].h
    val _ =
      if animate then
        rrAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("x", rrX)
          .attr("y", rrY)
          .attr("width", rrW)
          .attr("height", rrH)
      else rrAll.attr("x", rrX).attr("y", rrY).attr("width", rrW).attr("height", rrH)
    val _ = rrSel.exit().remove()

    // ── Cells (rows × cols grid) ────────────────────────────────────────────
    val cellTuples: List[(Int, Int, String)] =
      grid.zipWithIndex.flatMap { case (rowList, r) =>
        rowList.zipWithIndex.map { case (v, c) => (r, c, v) }
      }
    val cellData: js.Array[js.Any] = cellTuples.map { case (r, c, v) =>
      js.Dynamic.literal(row = r, col = c, value = v).asInstanceOf[js.Any]
    }.toJSArray
    val cellKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"${dyn.row}-${dyn.col}-${dyn.value}"
    val cellTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val r   = dyn.row.asInstanceOf[Int]
      val c   = dyn.col.asInstanceOf[Int]
      s"translate(${gridX(c)}, ${gridY(r)})"

    val cellSel = svg.selectAll("g.array-traversal__cell-group--grid").data(cellData, cellKeyFn)
    val cellEnter = cellSel
      .enter()
      .append("g")
      .attr("class", "array-traversal__cell-group--grid")
      .attr("transform", cellTransform)
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
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val cellAll = svg.selectAll("g.array-traversal__cell-group--grid")
    val _ =
      if animate then
        cellAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", cellTransform)
      else cellAll.attr("transform", cellTransform)
    val inRegionFn: js.Function2[js.Any, Int, Boolean] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val r   = dyn.row.asInstanceOf[Int]
      val c   = dyn.col.asInstanceOf[Int]
      step.regionRange.exists(rg => r >= rg.rowLo && r <= rg.rowHi && c >= rg.colLo && c <= rg.colHi)
    val _ = cellAll
      .select("rect.array-traversal__cell")
      .classed("array-traversal__cell--in-region", inRegionFn)
    val _ = cellSel.exit().remove()

    // ── Markers ─────────────────────────────────────────────────────────────
    val activeMarkers = step.markers.filter { m =>
      (for { r <- m.row; c <- m.col } yield r >= 0 && r < rowsN && c >= 0 && c < colsN)
        .getOrElse(false)
    }
    val markerData: js.Array[js.Any] = activeMarkers.map { m =>
      js.Dynamic
        .literal(
          name = m.name,
          row = m.row.getOrElse(-1),
          col = m.col.getOrElse(-1),
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val markerKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].name
    val markerTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val r   = dyn.row.asInstanceOf[Int]
      val c   = dyn.col.asInstanceOf[Int]
      // Marker label/arrow sits above the cell (StackQueue queue-mode style).
      s"translate(${gridX(c) + CellSize / 2}, ${gridY(r) - 4})"
    val markerSel = svg.selectAll("g.array-traversal__marker--grid").data(markerData, markerKeyFn)
    val markerEnter = markerSel
      .enter()
      .append("g")
      .attr("class", "array-traversal__marker--grid")
      .attr("transform", markerTransform)
      .attr("opacity", 0)
    val triangleD: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      if d.asInstanceOf[js.Dynamic].canonical.asInstanceOf[Boolean] then "M -5 -6 L 5 -6 L 0 2 Z"
      else ""
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn  = d.asInstanceOf[js.Dynamic]
      val name = dyn.name.asInstanceOf[String]
      if dyn.canonical.asInstanceOf[Boolean] then name else s"⚠ $name"
    val _ = markerEnter
      .append("path")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerEnter
      .append("text")
      .attr("class", "array-traversal__marker-label")
      .attr("x", 0)
      .attr("y", -10)
      .attr("text-anchor", "middle")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _         = markerEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val markerAll = svg.selectAll("g.array-traversal__marker--grid")
    val _ =
      if animate then
        markerAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", markerTransform)
          .attr("opacity", 1)
      else markerAll.attr("transform", markerTransform).attr("opacity", 1)
    val _ = markerAll
      .select("path")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = markerAll
      .select("text.array-traversal__marker-label")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _ = markerSel.exit().remove()

  // ---------------------------------------------------------------------------
  // flat-with-2d-overlay renderer — reuses 1D `renderRow` for the cells, then
  // overlays one `<text>` per active marker showing the step's rowColAnnotation
  // below the cell-index line.
  // ---------------------------------------------------------------------------

  private def renderFlatWithOverlay(
      svgEl: dom.Element,
      spec: Spec,
      step: Step,
      animate: Boolean
  ): Unit =
    val svg = D3.select(svgEl)
    // Tear down stale grid / secondary elements (dev hot-reload guard).
    val _     = svg.selectAll("g.array-traversal__cell-group--grid").remove()
    val _     = svg.selectAll("g.array-traversal__marker--grid").remove()
    val _     = svg.selectAll("rect.array-traversal__region").remove()
    val _     = svg.selectAll("g.array-traversal__cell-group--secondary").remove()
    val _     = svg.selectAll("g.array-traversal__marker--secondary").remove()
    val _     = svg.selectAll("rect.array-traversal__range--secondary").remove()
    val _     = svg.selectAll("text.array-traversal__empty-placeholder").remove()
    val items = step.items.getOrElse(spec.items)
    val keys  = step.keys.getOrElse(items)

    val width  = viewBoxWidth(items.size)
    val height = viewBoxHeight(false)
    val _      = svg.attr("viewBox", s"0 0 $width $height")

    renderRow(svgEl, PrimaryRow, items, keys, step.markers, step.range, PaddingY, animate)

    // (row, col) annotation per active marker. The annotation text comes from
    // the step's rowColAnnotation; the cell to place it under comes from the
    // marker's index. Filter to markers whose index is in range; if there are
    // multiple markers, all get the same annotation (acceptable — source
    // diagrams in the spec only ever use a single cursor at a time).
    val annotData: js.Array[js.Any] = step.rowColAnnotation match
      case Some(a) =>
        step.markers
          .flatMap(m => m.index.map(i => (m.name, i, a.row, a.col)))
          .filter { case (_, i, _, _) => i >= 0 && i < items.size }
          .map { case (name, i, r, c) =>
            js.Dynamic.literal(name = name, idx = i, txt = s"($r, $c)").asInstanceOf[js.Any]
          }
          .toJSArray
      case None => js.Array()
    val annotKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"annot-${d.asInstanceOf[js.Dynamic].name}"
    val annotSel = svg.selectAll("text.array-traversal__overlay-annotation").data(annotData, annotKeyFn)
    val annotXFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      cellX(d.asInstanceOf[js.Dynamic].idx.asInstanceOf[Int]) + CellSize / 2
    val annotY = PaddingY + CellSize + 12 + OverlayAnnotationDy
    val _ = annotSel
      .enter()
      .append("text")
      .attr("class", "array-traversal__overlay-annotation")
      .attr("text-anchor", "middle")
      .attr("y", annotY)
    val annotAll = svg.selectAll("text.array-traversal__overlay-annotation")
    val _ =
      if animate then
        annotAll
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("x", annotXFn)
      else annotAll.attr("x", annotXFn)
    val _ = annotAll.text(((d, _) => d.asInstanceOf[js.Dynamic].txt): js.Function2[js.Any, Int, js.Any])
    val _ = annotSel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      // Stepper hook — owns the step index, playing flag, timeout ref, and play-loop effect. Widget
      // supplies (stepCount, delayMs); receives state + prev/next/reset/togglePlay/jumpTo callbacks.
      .customBy { (_, specM) =>
        val stepCount = specM.value.toOption.fold(0)(_.steps.size)
        Stepper.hook(Stepper.Input(stepCount, StepDelayMs.toDouble))
      }
      .useRefToVdom[dom.html.Element] // host div ref — D3 manages the <svg> inside
      .useRefBy(_ => false)           // hasRendered (mutable; avoids re-render cycle)
      // ── D3 render on every step / spec change ───────────────────────────────
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
              <.p(^.className   := "d3-widget__error-title", "Array traversal payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then
                Step(None, None, None, Nil, None, None, None, "No steps defined.", Nil, None, None, Nil, None)
              else spec.steps(idx)

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
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "array-traversal__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("array-traversal__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "array-traversal__button array-traversal__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("array-traversal__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("array-traversal__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "array-traversal__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("array-traversal__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
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
                ^.dangerouslySetInnerHtml := (
                  if currentStep.msg.nonEmpty then renderInlineMarkdown(currentStep.msg) else " "
                )
              ),
              controls
            )
      }
