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
 * Heap-tree stepper — fifth widget in the D3 catalog. Animates heap operations across the dual representation
 * source Phase 8 teaches: the **tree view** (binary tree with the heap-order invariant rendered above) and
 * the **array view** (the contiguous backing array with index labels). One widget renders both panels and
 * keeps them synchronised step-to-step — a sift-up swap visibly slides the same item in both views because
 * both panels are keyed by `item.id`.
 *
 * Three modes share one renderer through a `mode` flag:
 *
 *   - `mode:"max"` — max-heap (largest at root); the comparison badge reads `parent ≥ child`.
 *   - `mode:"min"` — min-heap (smallest at root); the comparison badge reads `parent ≤ child`.
 *   - `mode:"custom"` — comparator pattern; an author-supplied `comparatorLabel` text renders next to the
 *     mode badge ("by frequency desc, then by value asc"). The comparator function itself is never JS — it's
 *     a text label only, and the per-step `sortKey` field on items is what the reader compares visually.
 *
 * Each step's `itemsOverride` is a **full** replacement of the items array (not sparse). This mirrors how
 * heap operations restate the whole heap state per pedagogical frame in the source, and matches the dual-view
 * "both panels read off the same source-of-truth array" model. A step that wants to keep the prior step's
 * items omits `itemsOverride` entirely.
 *
 * Per-step `markers` highlight specific indices in BOTH views — a `current` marker at index 2 paints the tree
 * node at index 2 AND the array cell at index 2 with the same colour. Marker names come from [[MarkerCanon]];
 * author-supplied `color` fields are dropped at parse time. Per-step `pendingSwap` overlays a curved arc
 * between two indices in both views (the canonical "swap will happen at the next step" cue before the items
 * array actually mutates).
 *
 * Optional per-step decorators: `compareResult` renders a one-line comparison badge at the top of the canvas
 * ("18 > 8 → true"); `inputArray` renders a horizontal stream strip above the array view with a cursor
 * pointing at the currently-consumed value (top-k streaming); `outputItems` renders a horizontal output strip
 * below the array view (k-largest result accumulator).
 *
 * Optional top-level `view` toggle suppresses one of the panels — `"tree"` for a heap-shape-only demo,
 * `"array"` for an indexing-only demo. Default `"both"` stacks tree above array with a small gap between
 * them.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":             "Insert 18 into the max heap",
 *   "mode":              "max",                                 // "max" | "min" | "custom"
 *   "comparatorLabel":   "by frequency desc, then by value asc", // "custom" mode only
 *   "view":              "both",                                 // "both" (default) | "tree" | "array"
 *   "items": [
 *     {"id": "n15", "value": "15", "sortKey": "15"}
 *   ],
 *   "steps": [{
 *     "itemsOverride": [{"id": "n15", "value": "15"}, ...],     // full replacement; omit to keep prior
 *     "markers":       [{"name": "current", "index": 6}],
 *     "pendingSwap":   {"from": 6, "to": 2},
 *     "compareResult": {"left": "18", "right": "8", "op": ">", "result": true},
 *     "inputArray":    {"items": ["4","1","7"], "cursor": 1, "label": "stream"},
 *     "outputItems":   ["6", "9", "7"],
 *     "msg":           "Compare 18 vs parent (8). 18 > 8 → swap."
 *   }]
 * }
 * }}}
 *
 * Tree layout: index `i` sits at level `floor(log2(i+1))`, at within-level position `i - (2^level - 1)`. The
 * tree canvas reserves a full binary tree footprint at the max level used; partial trees with missing leaves
 * still get the right per-index x-coordinate (so an extract-top that shrinks the heap from 7 to 6 nodes shows
 * the bottom-right slot vanish, not the whole row reshuffle).
 */
object HeapTree:

  // ---------------------------------------------------------------------------
  // Schema — parsed lazily from the JSON payload string. Each widget owns its
  // own schema; shared keeps Block.D3Widget structurally loose (ADR-0006).
  // ---------------------------------------------------------------------------

  final case class Item(id: String, value: String, sortKey: Option[String])

  // Single loose Marker case class (same shape as StackQueue / ArrayTraversal):
  // `index` is the heap index it tags; `canonical` flags whether the name was
  // recognised by the canon (drives the warning treatment in the renderer).
  final case class Marker(name: String, index: Int, canonical: Boolean)

  final case class PendingSwap(from: Int, to: Int)

  final case class CompareResult(left: String, right: String, op: String, result: Boolean)

  final case class InputArray(items: List[String], cursor: Option[Int], label: Option[String])

  final case class Step(
      itemsOverride: Option[List[Item]],
      markers: List[Marker],
      pendingSwap: Option[PendingSwap],
      compareResult: Option[CompareResult],
      outputItems: List[String],
      inputArray: Option[InputArray],
      msg: String
  )

  final case class Spec(
      mode: String,
      comparatorLabel: Option[String],
      items: List[Item],
      view: String,
      title: Option[String],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Modes + views. Unknown values collapse to safe defaults at parse time so
  // a typo never crashes the chapter.
  // ---------------------------------------------------------------------------

  private val ModeMax    = "max"
  private val ModeMin    = "min"
  private val ModeCustom = "custom"
  private val Modes      = Set(ModeMax, ModeMin, ModeCustom)

  private val ViewBoth  = "both"
  private val ViewTree  = "tree"
  private val ViewArray = "array"
  private val Views     = Set(ViewBoth, ViewTree, ViewArray)

  // ---------------------------------------------------------------------------
  // Layout constants — sized so a 7-node heap (3 levels deep) fits inside a
  // chapter prose column without horizontal scrolling. Larger heaps overflow
  // into the SVG's horizontal scroll wrapper rather than shrink each node.
  // ---------------------------------------------------------------------------

  private val PaddingX             = 16.0
  private val PaddingY             = 12.0
  private val NodeRadius           = 20.0
  private val MarkerLaneH          = 26.0 // headroom above top tree row for marker labels
  private val CompareLaneH         = 26.0 // top compare-result text band
  private val InputLabelH          = 16.0
  private val InputCellSize        = 30.0
  private val InputCellGap         = 3.0
  private val InputCursorH         = 10.0 // triangle height pointing down at active cell
  private val InputStripGap        = 14.0 // gap between input strip and tree/array below
  private val TreeLevelH           = 64.0 // vertical gap between tree levels
  private val TreeLeafSlotW        = 60.0 // horizontal slot per leaf at the deepest level
  private val TreeArrayGap         = 30.0 // gap between tree and array view in "both" view
  private val ArrayCellW           = 44.0
  private val ArrayCellH           = 44.0
  private val ArrayCellGap         = 4.0
  private val ArrayIndexLabelH     = 16.0 // label band below the array cells
  private val OutputStripGap       = 14.0
  private val OutputLabelH         = 16.0
  private val OutputCellSize       = 36.0
  private val OutputCellGap        = 4.0
  private val StepDelayMs          = 1500
  private val TransitionDurationMs = 450.0
  private val FadeMs               = 350.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // ---------------------------------------------------------------------------
  // Parsing — each parser is small + total; PayloadDecoder.run collapses any
  // thrown exception into Left(message) and the renderer shows an inline error.
  // ---------------------------------------------------------------------------

  private def parseItem(d: js.Dynamic): Item =
    Item(
      id = d.string("id"),
      value = d.string("value"),
      sortKey = d.optString("sortKey")
    )

  private def parseItems(d: js.Dynamic): Option[List[Item]] =
    d.selectDynamic("itemsOverride")
      .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
      .toOption
      .map(_.toList.map(parseItem).filter(_.id.nonEmpty))

  private def parseMarker(d: js.Dynamic): Marker =
    val name      = d.string("name")
    val rawColor  = d.optString("color")
    val canonical = MarkerCanon.isCanonical(name)
    if rawColor.isDefined then MarkerCanon.warnAuthorColor("heap-tree", name)
    if name.nonEmpty && !canonical then MarkerCanon.warnUnknown("heap-tree", name)
    Marker(name = name, index = d.int("index"), canonical = canonical)

  private def parsePendingSwap(d: js.Dynamic): PendingSwap =
    PendingSwap(from = d.int("from"), to = d.int("to"))

  private def parseCompareResult(d: js.Dynamic): CompareResult =
    CompareResult(
      left = d.string("left"),
      right = d.string("right"),
      op = d.string("op"),
      result = d.bool("result")
    )

  private def parseInputArray(d: js.Dynamic): InputArray =
    InputArray(
      items = d.stringList("items").getOrElse(Nil),
      cursor = d.optInt("cursor"),
      label = d.optString("label")
    )

  private def parseStep(d: js.Dynamic): Step =
    Step(
      itemsOverride = parseItems(d),
      markers = d.dynList("markers").map(parseMarker).filter(_.name.nonEmpty),
      pendingSwap = d.optObj("pendingSwap").map(parsePendingSwap),
      compareResult = d.optObj("compareResult").map(parseCompareResult),
      outputItems = d.stringList("outputItems").getOrElse(Nil),
      inputArray = d.optObj("inputArray").map(parseInputArray),
      msg = d.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawMode = d.optString("mode").getOrElse(ModeMax)
      val mode    = if Modes.contains(rawMode) then rawMode else ModeMax
      val rawView = d.optString("view").getOrElse(ViewBoth)
      val view    = if Views.contains(rawView) then rawView else ViewBoth
      val items   = d.dynList("items").map(parseItem).filter(_.id.nonEmpty)
      val steps   = d.dynList("steps").map(parseStep)
      if steps.isEmpty then throw PayloadDecoder.invalid("steps must be non-empty")
      Spec(
        mode = mode,
        comparatorLabel = if mode == ModeCustom then d.optString("comparatorLabel") else None,
        items = items,
        view = view,
        title = d.optString("title"),
        steps = steps
      )
    }

  // ---------------------------------------------------------------------------
  // Effective items per step — itemsOverride is a FULL replacement (not sparse).
  // A step that omits itemsOverride keeps the prior step's items. Walks from
  // spec.items forward through the steps so an early step that didn't define
  // an override keeps the spec's baseline.
  // ---------------------------------------------------------------------------

  private def effectiveItems(spec: Spec, stepIdx: Int): List[Item] =
    var current = spec.items
    for i <- 0 to stepIdx do
      spec.steps(i).itemsOverride.foreach(items => current = items)
    current

  // ---------------------------------------------------------------------------
  // Tree-layout helpers — pure index math. Index `i` sits at level
  // `floor(log2(i+1))`; within-level position is `i - (2^level - 1)`. The
  // bit-shift loop avoids floating-point rounding on log2 near power-of-two
  // boundaries (a 7-node heap should report level 2 for the leaf at index 6,
  // not level 3 due to a 0.0000001 overshoot).
  // ---------------------------------------------------------------------------

  private def indexLevel(i: Int): Int =
    if i < 0 then 0
    else
      var k   = i + 1
      var lvl = 0
      while k > 1 do
        k >>= 1
        lvl += 1
      lvl

  private def levelStart(level: Int): Int = (1 << level) - 1

  private def withinLevel(i: Int): Int = i - levelStart(indexLevel(i))

  // Deepest level used by an item count — the layout reserves slot width for
  // the full binary tree at that depth, so a shrink (extract-top) lifts the
  // missing leaf rather than reshuffling siblings.
  private def maxLevelFor(count: Int): Int =
    if count <= 0 then 0 else indexLevel(count - 1)

  private def parentIndex(i: Int): Int = if i <= 0 then -1 else (i - 1) >> 1

  // ---------------------------------------------------------------------------
  // Pixel-coordinate helpers. Tree x-coordinates derive from the deepest
  // possible level the spec ever reaches across all steps (so the tree's
  // horizontal layout is stable as items append/shrink). Array x's are
  // index-based with a fixed cell pitch.
  // ---------------------------------------------------------------------------

  // Maximum count across all steps, including the spec baseline — drives the
  // canvas width allocation. The renderer doesn't reflow per step when the
  // count changes; instead the wider footprint is reserved once.
  private def maxItemCount(spec: Spec): Int =
    val baseCount = spec.items.size
    val perStep   = (0 until spec.steps.size).map(i => effectiveItems(spec, i).size)
    (baseCount :: perStep.toList).maxOption.getOrElse(0)

  // Tree-width derived from the deepest level the spec ever uses. Each leaf
  // at the deepest level occupies `TreeLeafSlotW`; full row width is
  // `2^maxLevel * TreeLeafSlotW`. A 0- or 1-node heap still claims one slot.
  private def treeCanvasWidth(spec: Spec): Double =
    val mlvl  = maxLevelFor(maxItemCount(spec))
    val leafs = 1 << mlvl
    math.max(1, leafs).toDouble * TreeLeafSlotW

  private def arrayCanvasWidth(spec: Spec): Double =
    val n = math.max(1, maxItemCount(spec))
    val w = n * ArrayCellW + (n - 1).max(0) * ArrayCellGap
    w

  // The visible width budget = whichever panel demands more. Both panels then
  // center-align inside that budget so they read as a single coordinated view.
  private def panelWidth(spec: Spec): Double =
    val tree   = if spec.view != ViewArray then treeCanvasWidth(spec) else 0.0
    val array  = if spec.view != ViewTree then arrayCanvasWidth(spec) else 0.0
    val input  = inputCanvasWidth(spec)
    val output = outputCanvasWidth(spec)
    List(tree, array, input, output).max

  private def inputCanvasWidth(spec: Spec): Double =
    val maxInput = spec.steps.flatMap(_.inputArray).map(_.items.size).maxOption.getOrElse(0)
    if maxInput == 0 then 0.0
    else maxInput * InputCellSize + (maxInput - 1).max(0) * InputCellGap

  private def outputCanvasWidth(spec: Spec): Double =
    val maxOut = spec.steps.map(_.outputItems.size).maxOption.getOrElse(0)
    if maxOut == 0 then 0.0
    else maxOut * OutputCellSize + (maxOut - 1).max(0) * OutputCellGap

  // Vertical band slicing — each optional band only consumes height when it's
  // present anywhere in the spec. The renderer keeps band positions stable
  // across steps so a step with no compareResult doesn't shift the tree up.
  private def hasCompare(spec: Spec): Boolean = spec.steps.exists(_.compareResult.isDefined)
  private def hasInput(spec: Spec): Boolean   = spec.steps.exists(_.inputArray.isDefined)
  private def hasOutput(spec: Spec): Boolean  = spec.steps.exists(_.outputItems.nonEmpty)

  // y-offsets for each band, derived once per render. The tree (or array, in
  // tree-suppressed view) starts after the optional compare + input bands.
  private case class YBands(
      compareY: Double,
      inputLabelY: Double,
      inputCellsY: Double,
      treeY: Double,  // top of the tree's marker lane (markers sit above the topmost node row)
      arrayY: Double, // top of the array cells
      arrayIndexY: Double,
      outputLabelY: Double,
      outputCellsY: Double,
      totalH: Double
  )

  private def computeYBands(spec: Spec): YBands =
    var y        = PaddingY
    val compareY = y
    if hasCompare(spec) then y += CompareLaneH
    val inputLabelY = y
    val inputCellsY = y + InputLabelH
    if hasInput(spec) then y += InputLabelH + InputCellSize + InputCursorH + InputStripGap

    val maxLvl       = maxLevelFor(maxItemCount(spec))
    val treeY        = y // top of marker lane
    val treeNodeTopY = y + MarkerLaneH
    val treeBottomY  = treeNodeTopY + (maxLvl + 1).toDouble * TreeLevelH - (TreeLevelH - NodeRadius * 2)

    val arrayY =
      if spec.view == ViewArray then treeY
      else if spec.view == ViewTree then treeY // unused
      else treeBottomY + TreeArrayGap
    val arrayCellsBottom =
      if spec.view == ViewTree then treeBottomY
      else arrayY + ArrayCellH
    val arrayIndexY =
      if spec.view == ViewTree then treeBottomY
      else arrayCellsBottom + 4.0

    val outputStart =
      if spec.view == ViewTree then treeBottomY + OutputStripGap
      else arrayIndexY + ArrayIndexLabelH + OutputStripGap
    val outputLabelY = outputStart
    val outputCellsY = outputStart + OutputLabelH
    val totalH =
      if hasOutput(spec) then outputCellsY + OutputCellSize + PaddingY
      else if spec.view == ViewTree then treeBottomY + PaddingY
      else arrayIndexY + ArrayIndexLabelH + PaddingY

    YBands(
      compareY = compareY,
      inputLabelY = inputLabelY,
      inputCellsY = inputCellsY,
      treeY = treeY,
      arrayY = arrayY,
      arrayIndexY = arrayIndexY,
      outputLabelY = outputLabelY,
      outputCellsY = outputCellsY,
      totalH = totalH
    )

  // X-helpers — accept the per-render `panelW` to centre-align each band's
  // contents inside the widest panel.

  private def treeNodeX(i: Int, panelW: Double, spec: Spec): Double =
    val tW    = treeCanvasWidth(spec)
    val xBase = PaddingX + (panelW - tW) / 2.0
    val lvl   = indexLevel(i)
    val wi    = withinLevel(i)
    val slotW = tW / (1 << lvl).toDouble
    xBase + slotW * (wi + 0.5)

  private def treeNodeY(i: Int, bands: YBands): Double =
    val lvl = indexLevel(i)
    bands.treeY + MarkerLaneH + NodeRadius + lvl.toDouble * TreeLevelH

  private def arrayCellX(i: Int, panelW: Double, spec: Spec): Double =
    val aW    = arrayCanvasWidth(spec)
    val xBase = PaddingX + (panelW - aW) / 2.0
    xBase + i.toDouble * (ArrayCellW + ArrayCellGap)

  private def arrayCellCenterX(i: Int, panelW: Double, spec: Spec): Double =
    arrayCellX(i, panelW, spec) + ArrayCellW / 2.0

  private def inputCellX(i: Int, panelW: Double, spec: Spec): Double =
    val iw    = inputCanvasWidth(spec)
    val xBase = PaddingX + (panelW - iw) / 2.0
    xBase + i.toDouble * (InputCellSize + InputCellGap)

  private def outputCellX(i: Int, panelW: Double, outputCount: Int): Double =
    // Output strip's width depends on THIS step's count, not the spec max —
    // the output grows step-by-step and shrinking it would feel wrong.
    val ow    = outputCount * OutputCellSize + (outputCount - 1).max(0) * OutputCellGap
    val xBase = PaddingX + (panelW - ow) / 2.0
    xBase + i.toDouble * (OutputCellSize + OutputCellGap)

  // ---------------------------------------------------------------------------
  // SVG bootstrap — first paint only. viewBox + width/height update on each
  // renderStep because the spec's overall bounding box is stable but every
  // band's presence is checked per render.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then existing.asInstanceOf[dom.Element]
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", s"heap-tree__svg heap-tree__svg--${spec.mode}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Heap (${spec.mode})"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", "0 0 400 200")
      svg.setAttribute("width", "400")
      svg.setAttribute("height", "200")
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Per-step render — idempotent. Edges + nodes + array cells all keyed by
  // item.id so a swap visibly slides the same DOM elements between positions.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, stepIdx: Int, animate: Boolean): Unit =
    val svg     = D3.select(svgEl)
    val step    = spec.steps(stepIdx)
    val items   = effectiveItems(spec, stepIdx)
    val bands   = computeYBands(spec)
    val panelW  = panelWidth(spec)
    val totalW  = panelW + PaddingX * 2
    val totalH  = bands.totalH
    val moveDur = if animate then TransitionDurationMs else 0.0
    val fadeDur = if animate then FadeMs else 0.0

    val _ = svg.attr("viewBox", s"0 0 $totalW $totalH")
    val _ = svg.attr("width", totalW.toString)
    val _ = svg.attr("height", totalH.toString)

    // Index by id so the renderer can resolve marker indices to (id, position)
    // for keyed-by-id lookups.
    val itemsByIndex = items.zipWithIndex.map { case (it, idx) => idx -> it }.toMap

    renderCompare(svg, step, bands, totalW)
    renderInput(svg, step, bands, panelW, spec, animate, fadeDur, moveDur)
    if spec.view != ViewArray then
      renderTreeEdges(svg, items, panelW, spec, bands, animate, fadeDur, moveDur)
      renderTreeNodes(svg, items, panelW, spec, bands, animate, fadeDur, moveDur)
      renderTreePendingSwap(svg, step, items, panelW, spec, bands, fadeDur)
      renderTreeMarkers(svg, step, items, panelW, spec, bands, animate, fadeDur, moveDur)
    else
      val _ = svg.selectAll("g.heap-tree__tree-node").remove()
      val _ = svg.selectAll("path.heap-tree__tree-edge").remove()
      val _ = svg.selectAll("path.heap-tree__tree-pending").remove()
      val _ = svg.selectAll("g.heap-tree__tree-marker").remove()
    if spec.view != ViewTree then
      renderArrayCells(svg, items, panelW, spec, bands, animate, fadeDur, moveDur)
      renderArrayIndexLabels(svg, items, panelW, spec, bands)
      renderArrayPendingSwap(svg, step, items, panelW, spec, bands, fadeDur)
      renderArrayMarkers(svg, step, items, panelW, spec, bands, animate, fadeDur, moveDur)
    else
      val _ = svg.selectAll("g.heap-tree__array-cell").remove()
      val _ = svg.selectAll("text.heap-tree__array-index").remove()
      val _ = svg.selectAll("path.heap-tree__array-pending").remove()
      val _ = svg.selectAll("g.heap-tree__array-marker").remove()
    renderOutput(svg, step, bands, panelW, animate, fadeDur, moveDur)

    // Suppress unused-binding warning in case future code paths add per-step
    // lookups that need this map.
    val _ = itemsByIndex

  // ── Compare panel ─────────────────────────────────────────────────────────
  // Singleton text band at the top — shows "L op R → result". When the step
  // has no compareResult we still keep the band's vertical slot (so the layout
  // doesn't shift between steps) but the text node is removed.

  private def renderCompare(svg: D3.Selection, step: Step, bands: YBands, totalW: Double): Unit =
    val data: js.Array[js.Any] = step.compareResult.toList.map { c =>
      val verdict = if c.result then "true" else "false"
      js.Dynamic.literal(text = s"${c.left} ${c.op} ${c.right} → $verdict", result = c.result).asInstanceOf[
        js.Any
      ]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any]  = (_, _) => "compare"
    val xMid                                      = totalW / 2.0
    val yMid                                      = bands.compareY + CompareLaneH / 2.0 + 4.0
    val textFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val classFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val r    = d.asInstanceOf[js.Dynamic].result.asInstanceOf[Boolean]
      val base = "heap-tree__compare"
      if r then s"$base heap-tree__compare--true" else s"$base heap-tree__compare--false"

    val sel = svg.selectAll("text.heap-tree__compare").data(data, keyFn)
    val enter = sel.enter().append("text").attr("class", classFn).attr("text-anchor", "middle").attr(
      "x",
      xMid
    ).attr("y", yMid)
    val _   = enter.text(textFn)
    val all = svg.selectAll("text.heap-tree__compare")
    val _   = all.attr("class", classFn).attr("x", xMid).attr("y", yMid).text(textFn)
    val _   = sel.exit().remove()

  // ── Input strip ───────────────────────────────────────────────────────────
  // Horizontal row of cells above the array view, with a label + a downward
  // triangle cursor sitting above the active cell. Keyed by `${index}-${value}`
  // so an item entering at the same index as a prior value enters as a new
  // cell rather than re-skinning the existing one.

  private def renderInput(
      svg: D3.Selection,
      step: Step,
      bands: YBands,
      panelW: Double,
      spec: Spec,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val ia = step.inputArray
    if ia.isEmpty then
      val _ = svg.selectAll("g.heap-tree__input-cell").remove()
      val _ = svg.selectAll("text.heap-tree__input-label").remove()
      val _ = svg.selectAll("path.heap-tree__input-cursor").remove()
      return ()

    val input = ia.get
    // Label (left-anchored to the strip's left edge so it doesn't shove cells).
    val labelText                                      = input.label.getOrElse("input")
    val labelData: js.Array[js.Any]                    = js.Array(labelText).asInstanceOf[js.Array[js.Any]]
    val labelKey: js.Function2[js.Any, Int, js.Any]    = (_, _) => "input-label"
    val labelXFn: js.Function2[js.Any, Int, js.Any]    = (_, _) => PaddingX
    val labelYFn: js.Function2[js.Any, Int, js.Any]    = (_, _) => bands.inputLabelY + 12.0
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d
    val labelSel = svg.selectAll("text.heap-tree__input-label").data(labelData, labelKey)
    val _ = labelSel
      .enter()
      .append("text")
      .attr("class", "heap-tree__input-label")
      .attr("text-anchor", "start")
      .attr("x", labelXFn)
      .attr("y", labelYFn)
      .text(labelTextFn)
    val _ =
      svg.selectAll("text.heap-tree__input-label").attr("x", labelXFn).attr("y", labelYFn).text(labelTextFn)
    val _ = labelSel.exit().remove()

    val cellY = bands.inputCellsY + 4.0
    val cellData: js.Array[js.Any] = input.items.zipWithIndex.map { case (v, i) =>
      val x = inputCellX(i, panelW, spec)
      js.Dynamic
        .literal(
          key = s"$i-$v",
          idx = i,
          value = v,
          x = x,
          y = cellY
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val cellKey: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].key
    val cellTransform: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"translate(${dyn.x.asInstanceOf[Double]}, ${dyn.y.asInstanceOf[Double]})"
    val sel = svg.selectAll("g.heap-tree__input-cell").data(cellData, cellKey)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__input-cell")
      .attr("transform", cellTransform)
      .attr("opacity", 0)
    val _ = enter.append("rect").attr("class", "heap-tree__input-rect").attr("rx", 4).attr(
      "width",
      InputCellSize
    ).attr("height", InputCellSize)
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__input-text")
      .attr("text-anchor", "middle")
      .attr("x", InputCellSize / 2)
      .attr("y", InputCellSize / 2 + 4)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val all = svg.selectAll("g.heap-tree__input-cell")
    val _ = all
      .select("text.heap-tree__input-text")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", cellTransform).attr("opacity", 1)
    val _ = sel.exit().remove()

    // Cursor — triangle pointing down at the active cell. Singleton keyed by a
    // constant; snap-set translate to sidestep the singleton-keyed transition
    // trap noted in HashTable (transitions on a constant-keyed selection don't
    // reliably propagate the new datum's coords across step renders).
    val cursorData: js.Array[js.Any] = input.cursor.toList.flatMap { ci =>
      if ci < 0 || ci >= input.items.size then Nil
      else
        val cx = inputCellX(ci, panelW, spec) + InputCellSize / 2.0
        val cy = cellY - 4.0
        List(js.Dynamic.literal(cx = cx, cy = cy).asInstanceOf[js.Any])
    }.toJSArray
    val cursorKey: js.Function2[js.Any, Int, js.Any] = (_, _) => "input-cursor"
    val cursorDFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val cx  = dyn.cx.asInstanceOf[Double]
      val cy  = dyn.cy.asInstanceOf[Double]
      s"M ${cx - InputCursorH / 2} ${cy - InputCursorH} L ${cx + InputCursorH / 2} ${cy - InputCursorH} L $cx $cy Z"
    val cursorSel = svg.selectAll("path.heap-tree__input-cursor").data(cursorData, cursorKey)
    val _ = cursorSel
      .enter()
      .append("path")
      .attr("class", "heap-tree__input-cursor")
      .attr("d", cursorDFn)
    val _ = svg.selectAll("path.heap-tree__input-cursor").attr("d", cursorDFn)
    val _ = cursorSel.exit().remove()

  // ── Tree edges ────────────────────────────────────────────────────────────
  // Edge keyed by ${parentId}->${childId}. Indices > 0 connect to their
  // parent at (i-1)/2. Edges enter with fade-in; positions animate.

  private def renderTreeEdges(
      svg: D3.Selection,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val edges = items.zipWithIndex.drop(1).map { case (child, i) =>
      val parent = items(parentIndex(i))
      (parent.id, child.id, i)
    }
    val data: js.Array[js.Any] = edges.map { case (pid, cid, ci) =>
      val parentI = parentIndex(ci)
      val px      = treeNodeX(parentI, panelW, spec)
      val py      = treeNodeY(parentI, bands)
      val cx      = treeNodeX(ci, panelW, spec)
      val cy      = treeNodeY(ci, bands)
      // Trim each end to meet the node edge (not the centre). Pre-trim here
      // rather than inside `dFn` so the transition's destination string is
      // a simple "M sx sy L ex ey" without helper-fn dependence.
      val dx     = cx - px
      val dy     = cy - py
      val length = math.max(1e-6, math.sqrt(dx * dx + dy * dy))
      val ux     = dx / length
      val uy     = dy / length
      val sx     = px + ux * NodeRadius
      val sy     = py + uy * NodeRadius
      val ex     = cx - ux * NodeRadius
      val ey     = cy - uy * NodeRadius
      js.Dynamic
        .literal(
          id = s"$pid->$cid",
          d = s"M $sx $sy L $ex $ey"
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val dFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].d
    val sel = svg.selectAll("path.heap-tree__tree-edge").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", "heap-tree__tree-edge")
      .attr("d", dFn)
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("path.heap-tree__tree-edge")
    // Snap-set the path data; CSS animates the d attribute via the same
    // transform-transition rule used by the nodes (browsers honour
    // `transition: d ...` on SVG paths in recent versions; for older ones
    // the snap-set degrades to instant edge redraw on swap).
    val _ = animate
    val _ = moveDur
    val _ = all.attr("d", dFn)

  // ── Tree nodes ────────────────────────────────────────────────────────────
  // Group keyed by item.id (the heart of the dual-view: same item, two
  // positions; the renderer derives both from the item's current array index).
  // Each group holds a circle, the value label, and (custom mode only) a small
  // sortKey badge above the node.

  private def renderTreeNodes(
      svg: D3.Selection,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    // Precompute x/y per-datum (mirrors GraphExplorer's working pattern).
    // Reading `dyn.x` / `dyn.y` from the datum inside transformFn is what
    // makes D3's `transition().attr("transform", fn)` actually animate the
    // attribute change — passing an `idx` plus a helper-fn closure left the
    // attribute stuck at its enter-time value (the transition was scheduled
    // but the destination didn't take effect, see browser-verification
    // session notes).
    val data: js.Array[js.Any] = items.zipWithIndex.map { case (it, i) =>
      val x = treeNodeX(i, panelW, spec)
      val y = treeNodeY(i, bands)
      js.Dynamic
        .literal(
          id = it.id,
          value = it.value,
          sortKey = it.sortKey.getOrElse(""),
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
    val sel = svg.selectAll("g.heap-tree__tree-node").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__tree-node")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter.append("circle").attr("class", "heap-tree__tree-circle").attr("r", NodeRadius)
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__tree-value")
      .attr("text-anchor", "middle")
      .attr("y", 5.0)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    // Custom-mode sortKey badge — only renders when sortKey is non-empty AND
    // mode is custom. Mixed-mode payloads with a stray sortKey on a non-custom
    // spec are silently ignored.
    if spec.mode == ModeCustom then
      val _ = enter
        .append("text")
        .attr("class", "heap-tree__tree-sortkey")
        .attr("text-anchor", "middle")
        .attr("y", -NodeRadius - 4)
        .text((
            (d, _) => {
              val s = d.asInstanceOf[js.Dynamic].sortKey.asInstanceOf[String]
              if s.isEmpty then "" else s"key=$s"
            }
        ): js.Function2[js.Any, Int, js.Any])
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.heap-tree__tree-node")
    // `.select` (singular) propagates the parent group's CURRENT datum to the
    // child text — same trap as BinaryTree / GraphExplorer / HashTable: a
    // `.selectAll` here reads the children's stale enter-time data.
    val _ = all
      .select("text.heap-tree__tree-value")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    if spec.mode == ModeCustom then
      val _ = all
        .select("text.heap-tree__tree-sortkey")
        .text((
            (d, _) => {
              val s = d.asInstanceOf[js.Dynamic].sortKey.asInstanceOf[String]
              if s.isEmpty then "" else s"key=$s"
            }
        ): js.Function2[js.Any, Int, js.Any])
    // Snap-set transform + opacity directly (no d3 transition). For id-keyed
    // groups whose datum changes only in coordinate fields between renders,
    // `selection.transition().attr("transform", fn)` does NOT propagate the
    // new value to the rendered attribute — the data IS rebound (we see
    // updated `__data__` on the element) but the SVG transform attribute
    // stays at its prior string. The CSS rule `transition: transform ...`
    // on `.heap-tree__tree-node` animates the snap-set so the swap-slide
    // is still smooth visually.
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ── Tree pending-swap arc ─────────────────────────────────────────────────
  // A dashed arc between the two indices' tree positions, drawn ABOVE the
  // edge so it sits visually on top. Singleton-keyed (one pending swap per
  // step max), so snap-set the path attr — same trap as HashTable.

  private def renderTreePendingSwap(
      svg: D3.Selection,
      step: Step,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      fadeDur: Double
  ): Unit =
    val data: js.Array[js.Any] = step.pendingSwap.toList.flatMap { ps =>
      if ps.from < 0 || ps.to < 0 || ps.from >= items.size || ps.to >= items.size then Nil
      else
        val x1 = treeNodeX(ps.from, panelW, spec)
        val y1 = treeNodeY(ps.from, bands)
        val x2 = treeNodeX(ps.to, panelW, spec)
        val y2 = treeNodeY(ps.to, bands)
        // Quadratic Bézier above the line joining the two centres — the
        // control point sits perpendicular to the midpoint, ~36px above.
        val mx     = (x1 + x2) / 2
        val my     = (y1 + y2) / 2
        val dx     = x2 - x1
        val dy     = y2 - y1
        val length = math.max(1e-6, math.sqrt(dx * dx + dy * dy))
        // Perpendicular unit vector (left-hand normal).
        val nx = -dy / length
        val ny = dx / length
        // Bow direction: always upward in screen coords (negative y), so
        // negate if the perpendicular ended up pointing down.
        val arch = 36.0
        val cx   = if ny < 0 then mx + nx * arch else mx - nx * arch
        val cy   = if ny < 0 then my + ny * arch else my - ny * arch
        List(js.Dynamic.literal(d = s"M $x1 $y1 Q $cx $cy $x2 $y2").asInstanceOf[js.Any])
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "tree-pending"
    val dFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].d
    val sel = svg.selectAll("path.heap-tree__tree-pending").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", "heap-tree__tree-pending")
      .attr("d", dFn)
      .attr("fill", "none")
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = svg.selectAll("path.heap-tree__tree-pending").attr("d", dFn).attr("opacity", 1)
    val _ = sel.exit().remove()

  // ── Tree markers ──────────────────────────────────────────────────────────
  // Same family as BinaryTree's markers but indexed by heap index rather than
  // node-id (the spec uses indices for both views' marker resolution). Rank
  // within the index so multiple markers on the same node stack vertically.

  private def renderTreeMarkers(
      svg: D3.Selection,
      step: Step,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val active = step.markers.filter(m => m.index >= 0 && m.index < items.size)
    val ranked = active.zipWithIndex.map { case (m, idx) =>
      val rank = active.take(idx).count(_.index == m.index)
      (m, rank)
    }
    val data: js.Array[js.Any] = ranked.map { case (m, rank) =>
      val x = treeNodeX(m.index, panelW, spec)
      val y = treeNodeY(m.index, bands) - NodeRadius - 8.0 - rank * 13.0
      js.Dynamic
        .literal(
          name = m.name,
          index = m.index,
          rank = rank,
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical,
          x = x,
          y = y
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => s"tree-${d.asInstanceOf[js.Dynamic].name}"
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
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

    val sel = svg.selectAll("g.heap-tree__tree-marker").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__tree-marker")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("path")
      .attr("class", "heap-tree__tree-marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__tree-marker-label")
      .attr("text-anchor", "middle")
      .attr("y", -12.0)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.heap-tree__tree-marker")
    val _ = all
      .select("path.heap-tree__tree-marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = all
      .select("text.heap-tree__tree-marker-label")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ── Array cells ───────────────────────────────────────────────────────────
  // Group keyed by item.id (mirrors tree). On a swap the same group's
  // transform slides to the new index.

  private def renderArrayCells(
      svg: D3.Selection,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val data: js.Array[js.Any] = items.zipWithIndex.map { case (it, i) =>
      val x = arrayCellX(i, panelW, spec)
      val y = bands.arrayY
      js.Dynamic
        .literal(
          id = it.id,
          value = it.value,
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
    val sel = svg.selectAll("g.heap-tree__array-cell").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__array-cell")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("rect")
      .attr("class", "heap-tree__array-rect")
      .attr("rx", 4)
      .attr("width", ArrayCellW)
      .attr("height", ArrayCellH)
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__array-text")
      .attr("text-anchor", "middle")
      .attr("x", ArrayCellW / 2)
      .attr("y", ArrayCellH / 2 + 5)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.heap-tree__array-cell")
    val _ = all
      .select("text.heap-tree__array-text")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    // Same snap-set rule as renderTreeNodes — CSS handles the slide.
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ── Array index labels ────────────────────────────────────────────────────
  // Static labels (0, 1, 2, ...) below each array cell. They don't slide on
  // swap — the labels are FIXED to positions, not to items. The position-key
  // ensures they rebind by index.

  private def renderArrayIndexLabels(
      svg: D3.Selection,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands
  ): Unit =
    val data: js.Array[js.Any] = items.zipWithIndex.map { case (_, i) =>
      val x = arrayCellCenterX(i, panelW, spec)
      val y = bands.arrayIndexY + 11
      js.Dynamic.literal(idx = i, x = x, y = y, text = i.toString).asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"idx-${d.asInstanceOf[js.Dynamic].idx.asInstanceOf[Int]}"
    val xFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].x
    val yFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].y
    val textFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val sel = svg.selectAll("text.heap-tree__array-index").data(data, keyFn)
    val _ = sel
      .enter()
      .append("text")
      .attr("class", "heap-tree__array-index")
      .attr("text-anchor", "middle")
      .attr("x", xFn)
      .attr("y", yFn)
      .text(textFn)
    val all = svg.selectAll("text.heap-tree__array-index")
    val _   = all.attr("x", xFn).attr("y", yFn).text(textFn)
    val _   = sel.exit().remove()

  // ── Array pending-swap arc ────────────────────────────────────────────────
  // Mirrors the tree pending-swap: a quadratic Bézier above the two array
  // cells' tops. Singleton-keyed.

  private def renderArrayPendingSwap(
      svg: D3.Selection,
      step: Step,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      fadeDur: Double
  ): Unit =
    val data: js.Array[js.Any] = step.pendingSwap.toList.flatMap { ps =>
      if ps.from < 0 || ps.to < 0 || ps.from >= items.size || ps.to >= items.size then Nil
      else
        val x1   = arrayCellCenterX(ps.from, panelW, spec)
        val x2   = arrayCellCenterX(ps.to, panelW, spec)
        val y    = bands.arrayY
        val mx   = (x1 + x2) / 2
        val arch = math.min(28.0, math.max(14.0, math.abs(x2 - x1) / 4.0))
        List(js.Dynamic.literal(d = s"M $x1 $y Q $mx ${y - arch} $x2 $y").asInstanceOf[js.Any])
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "array-pending"
    val dFn: js.Function2[js.Any, Int, js.Any]   = (d, _) => d.asInstanceOf[js.Dynamic].d
    val sel = svg.selectAll("path.heap-tree__array-pending").data(data, keyFn)
    val _ = sel
      .enter()
      .append("path")
      .attr("class", "heap-tree__array-pending")
      .attr("d", dFn)
      .attr("fill", "none")
      .attr("opacity", 0)
      .transition()
      .duration(fadeDur)
      .ease(D3.easeCubicInOut)
      .attr("opacity", 1)
    val _ = svg.selectAll("path.heap-tree__array-pending").attr("d", dFn).attr("opacity", 1)
    val _ = sel.exit().remove()

  // ── Array markers ─────────────────────────────────────────────────────────
  // The array-side companion to tree markers — a small coloured triangle +
  // label below the array cell. Keyed by name (one marker name per render);
  // rank stacks duplicates below each other.

  private def renderArrayMarkers(
      svg: D3.Selection,
      step: Step,
      items: List[Item],
      panelW: Double,
      spec: Spec,
      bands: YBands,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    val active = step.markers.filter(m => m.index >= 0 && m.index < items.size)
    val ranked = active.zipWithIndex.map { case (m, idx) =>
      val rank = active.take(idx).count(_.index == m.index)
      (m, rank)
    }
    val data: js.Array[js.Any] = ranked.map { case (m, rank) =>
      val x = arrayCellCenterX(m.index, panelW, spec)
      val y = bands.arrayY - 6.0 - rank * 13.0
      js.Dynamic
        .literal(
          name = m.name,
          index = m.index,
          rank = rank,
          color = MarkerCanon.colorFor(m.name),
          canonical = m.canonical,
          x = x,
          y = y
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => s"array-${d.asInstanceOf[js.Dynamic].name}"
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val triangleD: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val rank      = dyn.rank.asInstanceOf[Int]
      if !canonical || rank != 0 then "" else "M -4 -7 L 4 -7 L 0 0 Z"
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn       = d.asInstanceOf[js.Dynamic]
      val canonical = dyn.canonical.asInstanceOf[Boolean]
      val name      = dyn.name.asInstanceOf[String]
      if canonical then name else s"⚠ $name"

    val sel = svg.selectAll("g.heap-tree__array-marker").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__array-marker")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter
      .append("path")
      .attr("class", "heap-tree__array-marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__array-marker-label")
      .attr("text-anchor", "middle")
      .attr("y", -10.0)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.heap-tree__array-marker")
    val _ = all
      .select("path.heap-tree__array-marker-arrow")
      .attr("d", triangleD)
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
    val _ = all
      .select("text.heap-tree__array-marker-label")
      .attr("fill", ((d, _) => d.asInstanceOf[js.Dynamic].color): js.Function2[js.Any, Int, js.Any])
      .text(labelTextFn)
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ── Output strip ──────────────────────────────────────────────────────────
  // Horizontal row of cells below the array view, accumulating top-k results
  // step by step. Keyed by `${i}-${value}` so a value entering at the end of
  // the strip enters as a fresh cell rather than re-skinning a prior one.

  private def renderOutput(
      svg: D3.Selection,
      step: Step,
      bands: YBands,
      panelW: Double,
      animate: Boolean,
      fadeDur: Double,
      moveDur: Double
  ): Unit =
    if step.outputItems.isEmpty then
      val _ = svg.selectAll("g.heap-tree__output-cell").remove()
      val _ = svg.selectAll("text.heap-tree__output-label").remove()
      return ()
    val n                                              = step.outputItems.size
    val labelText                                      = "Output"
    val labelData: js.Array[js.Any]                    = js.Array(labelText).asInstanceOf[js.Array[js.Any]]
    val labelKey: js.Function2[js.Any, Int, js.Any]    = (_, _) => "output-label"
    val labelXFn: js.Function2[js.Any, Int, js.Any]    = (_, _) => PaddingX
    val labelYFn: js.Function2[js.Any, Int, js.Any]    = (_, _) => bands.outputLabelY + 12.0
    val labelTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d
    val labelSel = svg.selectAll("text.heap-tree__output-label").data(labelData, labelKey)
    val _ = labelSel
      .enter()
      .append("text")
      .attr("class", "heap-tree__output-label")
      .attr("text-anchor", "start")
      .attr("x", labelXFn)
      .attr("y", labelYFn)
      .text(labelTextFn)
    val _ =
      svg.selectAll("text.heap-tree__output-label").attr("x", labelXFn).attr("y", labelYFn).text(labelTextFn)
    val _ = labelSel.exit().remove()

    val yCells = bands.outputCellsY
    val data: js.Array[js.Any] = step.outputItems.zipWithIndex.map { case (v, i) =>
      val x = outputCellX(i, panelW, n)
      js.Dynamic.literal(key = s"$i-$v", idx = i, value = v, x = x, y = yCells).asInstanceOf[js.Any]
    }.toJSArray
    val keyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].key
    val transformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      s"translate(${dyn.x.asInstanceOf[Double]}, ${dyn.y.asInstanceOf[Double]})"
    val sel = svg.selectAll("g.heap-tree__output-cell").data(data, keyFn)
    val enter = sel
      .enter()
      .append("g")
      .attr("class", "heap-tree__output-cell")
      .attr("transform", transformFn)
      .attr("opacity", 0)
    val _ = enter.append("rect").attr("class", "heap-tree__output-rect").attr("rx", 4).attr(
      "width",
      OutputCellSize
    ).attr("height", OutputCellSize)
    val _ = enter
      .append("text")
      .attr("class", "heap-tree__output-text")
      .attr("text-anchor", "middle")
      .attr("x", OutputCellSize / 2)
      .attr("y", OutputCellSize / 2 + 4)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _   = enter.transition().duration(fadeDur).ease(D3.easeCubicInOut).attr("opacity", 1)
    val _   = sel.exit().remove()
    val all = svg.selectAll("g.heap-tree__output-cell")
    val _ = all
      .select("text.heap-tree__output-text")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].value): js.Function2[js.Any, Int, js.Any])
    val _ = animate
    val _ = moveDur
    val _ = all.attr("transform", transformFn).attr("opacity", 1)

  // ---------------------------------------------------------------------------
  // Component — React owns the host div; D3 owns the SVG inside it. The mode
  // badge + comparator label (custom mode only) sit above the SVG as static
  // React nodes, not on the canvas, because they don't animate per step.
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
              <.p(^.className   := "d3-widget__error-title", "Heap-tree payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then Step(None, Nil, None, None, Nil, None, "No steps defined.")
              else spec.steps(idx)

            val modeBadge: VdomNode =
              val text = spec.mode match
                case ModeMax    => "max-heap"
                case ModeMin    => "min-heap"
                case ModeCustom => "custom comparator"
                case _          => spec.mode
              <.div(
                ^.className := "heap-tree__mode-row",
                <.span(^.className := s"heap-tree__mode-badge heap-tree__mode-badge--${spec.mode}", text),
                spec.comparatorLabel
                  .filter(_ => spec.mode == ModeCustom)
                  .map(l => <.span(^.className := "heap-tree__comparator-label", l): VdomNode)
                  .getOrElse(EmptyVdom)
              )

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "heap-tree__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "heap-tree__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("heap-tree__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "heap-tree__button heap-tree__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("heap-tree__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("heap-tree__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "heap-tree__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("heap-tree__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "heap-tree__button heap-tree__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("heap-tree__button-icon"))
                  ),
                  <.span(
                    ^.className := "heap-tree__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"heap-tree heap-tree--${spec.mode} not-prose",
              spec.title
                .map(t => <.p(^.className := "heap-tree__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              modeBadge,
              <.div(
                ^.className := "heap-tree__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "heap-tree__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              controls
            )
      }
