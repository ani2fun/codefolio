package codefolio.client.components.cortex.widgets

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * B-tree walker — the senior moment of Lesson 9 made visceral. Shows the *shape* of a B-tree index for a
 * configurable table size: how many internal levels, how many pages at each level, and the lookup path from
 * root → leaf (one page per level). Below the tree, a comparison panel shows the corresponding
 * sequential-scan cost — `O(N)` pages of read traffic for the same query — making the order-of-magnitude
 * difference between an indexed lookup and a full table scan unavoidable.
 *
 * The slider lets the reader drag from 100 rows to 100 million. The point of the visualisation: tree depth
 * grows as `log_fanout(rowCount)`, so for *any realistic table* (millions of rows, fanout ~100) an indexed
 * lookup is 3–4 random page reads. The sequential scan grows linearly. By 1 M rows the indexed lookup is
 * ~1000× faster; by 100 M rows it's ~100 000× faster.
 *
 * Re-used in:
 *   - Lesson 9 (relational databases) — primary.
 *   - Lesson 22 (LSM trees vs B-trees) — contrast against LSM read amplification.
 *   - Capstone 37 (URL shortener) — when discussing the short_code → long_url lookup.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":             "B-tree index lookup — 3 page reads instead of N",
 *   "rowCount":          1000000,
 *   "rowCountRange":     [100, 100000000],
 *   "fanout":            100,
 *   "rowsPerPage":       100,
 *   "randomReadMs":      0.1,
 *   "sequentialReadMs":  0.01
 * }
 * }}}
 *
 *   - `rowCount` is the slider's starting position; the slider is log-mapped so the user can drag from
 *     `rowCountRange[0]` to `rowCountRange[1]` with even resolution per decade.
 *   - `fanout` is the average number of entries per index page. 100 is typical for Postgres on a narrow
 *     key; 200+ for very narrow keys; 50 for wide keys.
 *   - `rowsPerPage` is the heap density — typical 100 for narrow rows, 50 for wide ones.
 *   - `randomReadMs` is the per-page latency for an index-lookup page (random read; usually cached, but
 *     the worst case is a cold SSD seek). 0.1 ms is a reasonable Postgres-on-SSD default.
 *   - `sequentialReadMs` is the per-page latency for a sequential scan (read-ahead drops the per-page
 *     cost). 0.01 ms for SSD with read-ahead is a good default.
 *
 * SVG is built as a string and injected via `dangerouslySetInnerHTML`. State is just the log-slider value;
 * the tree and stats derive from it.
 */
object BTreeWalker:

  // ===========================================================================
  // Schema
  // ===========================================================================

  final case class Spec(
      title: Option[String],
      rowCount: Int,
      rowMin: Int,
      rowMax: Int,
      fanout: Int,
      rowsPerPage: Int,
      randomReadMs: Double,
      sequentialReadMs: Double
  )

  final case class Props(payload: String)

  // ===========================================================================
  // Parsing
  // ===========================================================================

  private def parseRange(d: js.Dynamic, default: (Int, Int)): (Int, Int) =
    d.asInstanceOf[js.UndefOr[js.Array[Double]]].toOption.map(_.toList) match
      case Some(lo :: hi :: _) => (lo.toInt, hi.toInt)
      case _                   => default

  private def parsePayload(json: String): Either[String, Spec] =
    Try {
      val raw    = js.JSON.parse(json).asInstanceOf[js.Dynamic]
      val title  = raw.title.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty)
      val rc     = raw.rowCount.asInstanceOf[js.UndefOr[Double]].toOption.getOrElse(1_000_000.0).toInt
      val rcR    = parseRange(raw.rowCountRange, (100, 100_000_000))
      val fanout = raw.fanout.asInstanceOf[js.UndefOr[Double]].toOption.getOrElse(100.0).toInt
      val rpp    = raw.rowsPerPage.asInstanceOf[js.UndefOr[Double]].toOption.getOrElse(100.0).toInt
      val rrMs   = raw.randomReadMs.asInstanceOf[js.UndefOr[Double]].toOption.getOrElse(0.1)
      val srMs   = raw.sequentialReadMs.asInstanceOf[js.UndefOr[Double]].toOption.getOrElse(0.01)
      Spec(title, rc, rcR._1, rcR._2, fanout, rpp, rrMs, srMs)
    } match
      case Success(s) if s.rowMin < 1 || s.rowMax < s.rowMin =>
        Left("payload.rowCountRange must satisfy 1 ≤ min ≤ max")
      case Success(s) if s.rowCount < s.rowMin || s.rowCount > s.rowMax =>
        Left(s"payload.rowCount must fall within rowCountRange [${s.rowMin}, ${s.rowMax}]")
      case Success(s) if s.fanout < 2 || s.fanout > 1000 =>
        Left("payload.fanout must be in [2, 1000]")
      case Success(s) if s.rowsPerPage < 1 =>
        Left("payload.rowsPerPage must be ≥ 1")
      case Success(s) if s.randomReadMs <= 0 || s.sequentialReadMs <= 0 =>
        Left("payload.randomReadMs and sequentialReadMs must be > 0")
      case Success(s) => Right(s)
      case Failure(t) => Left(Option(t.getMessage).getOrElse("invalid payload JSON"))

  // ===========================================================================
  // Math
  // ===========================================================================

  private def leafPagesOf(rowCount: Int, rowsPerPage: Int): Int =
    math.max(1, math.ceil(rowCount.toDouble / rowsPerPage.toDouble).toInt)

  /**
   * Tree depth = 1 (leaf level itself) + the number of internal levels needed to fan in to that many leaves.
   * Computed iteratively in integer arithmetic so we don't hit floating-point rounding cliffs (the previous
   * `1 + ceil(log(leaves) / log(fanout))` returned depth 4 at `leaves = 10000, fanout = 100` because the
   * ratio computed to `2.0000000001` and ceiled up).
   */
  private def depthOf(rowCount: Int, fanout: Int, rowsPerPage: Int): Int =
    var leaves = leafPagesOf(rowCount, rowsPerPage)
    var depth  = 1 // the leaf level itself
    while leaves > 1 do
      leaves = math.max(1, math.ceil(leaves.toDouble / fanout.toDouble).toInt)
      depth += 1
    depth

  /**
   * Pages at each level, root → leaf. The leaf level holds `leafPages` pages exactly; every level above is
   * a ceil-division of the level below by the fanout, growing from 1 page at the root.
   */
  private def pagesPerLevel(rowCount: Int, fanout: Int, rowsPerPage: Int): List[Int] =
    val depth  = depthOf(rowCount, fanout, rowsPerPage)
    val leaves = leafPagesOf(rowCount, rowsPerPage)
    if depth == 1 then List(leaves)
    else
      // Build leaf → root, then reverse.
      val builder = scala.collection.mutable.ListBuffer[Int](leaves)
      var current = leaves
      while builder.size < depth do
        current = math.max(1, math.ceil(current.toDouble / fanout.toDouble).toInt)
        builder.prepend(current)
      builder.toList

  // ===========================================================================
  // Log-mapped slider — even resolution per decade across rowCountRange
  // ===========================================================================

  private val SliderTicks = 1000

  /**
   * Map slider position to a "nice" row count from the 1-2-5 series (so the labels land on round numbers
   * the reader recognises — 100, 200, 500, 1K, 2K, …, 1M, 10M, 100M — instead of 1,010,370). This also
   * keeps the depth calculation pedagogically clean: at exactly 1 M rows + fanout 100 the depth is 3, and
   * the snapped slider lands at exactly 1 M when the reader drags to that visual position.
   */
  private def niceRound(n: Int): Int =
    if n <= 1 then 1
    else
      val mag  = math.pow(10.0, math.floor(math.log10(n.toDouble))).toInt
      val frac = n.toDouble / mag.toDouble
      val mult =
        if frac < 1.5 then 1
        else if frac < 3.5 then 2
        else if frac < 7.5 then 5
        else 10
      mult * mag

  private def sliderToRowCount(sliderValue: Int, rowMin: Int, rowMax: Int): Int =
    val frac    = sliderValue.toDouble / SliderTicks.toDouble
    val logSpan = math.log(rowMax.toDouble) - math.log(rowMin.toDouble)
    val log     = math.log(rowMin.toDouble) + frac * logSpan
    niceRound(math.exp(log).toInt)

  private def rowCountToSlider(rowCount: Int, rowMin: Int, rowMax: Int): Int =
    val logSpan = math.log(rowMax.toDouble) - math.log(rowMin.toDouble)
    val frac    = (math.log(rowCount.toDouble) - math.log(rowMin.toDouble)) / logSpan
    math.round(frac * SliderTicks.toDouble).toInt

  // ===========================================================================
  // Formatting
  // ===========================================================================

  private def humanCount(n: Int): String =
    if n >= 1_000_000 then f"${n / 1_000_000.0}%.1fM"
    else if n >= 1_000 then f"${n / 1_000.0}%.1fK"
    else n.toString

  private def humanMs(ms: Double): String =
    if ms >= 1_000 then f"${ms / 1_000}%.1f s"
    else if ms >= 1 then f"$ms%.1f ms"
    else f"${ms * 1_000}%.0f µs"

  // ===========================================================================
  // SVG layout
  // ===========================================================================

  private val ViewBoxWidth = 720.0
  private val TopPad       = 36.0
  private val LeftPad      = 130.0 // for the level labels on the left
  private val RightPad     = 200.0 // for the page-count text on the right
  private val LevelHeight  = 40.0
  private val BarMinW      = 30.0
  private val BarMaxW      = ViewBoxWidth - LeftPad - RightPad

  private def esc(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

  // Map page count → bar width on a log scale so 1 page and 10M pages both fit.
  private def pagesToBarW(pages: Int, maxPages: Int): Double =
    if pages <= 1 then BarMinW
    else
      val frac    = math.log(pages.toDouble) / math.log(math.max(maxPages, 10).toDouble)
      val clamped = math.max(0.0, math.min(1.0, frac))
      BarMinW + clamped * (BarMaxW - BarMinW)

  private def buildSvg(spec: Spec, rowCount: Int): String =
    val levels      = pagesPerLevel(rowCount, spec.fanout, spec.rowsPerPage)
    val depth       = levels.size
    val leafPages   = levels.last
    val maxBarPages = math.max(leafPages, 10) // for the log scale

    val treeRows = levels.zipWithIndex
      .map { case (pages, i) =>
        val y          = TopPad + i * LevelHeight
        val barW       = pagesToBarW(pages, maxBarPages)
        val barH       = 22.0
        val barX       = LeftPad
        val barY       = y
        val levelLabel = if i == 0 then "Root" else if i == depth - 1 then "Leaf" else s"Level $i"
        val levelText =
          s"""<text class="btree-walker__level-label" x="${LeftPad - 10}" y="${barY + 16}" text-anchor="end">${esc(
              levelLabel
            )}</text>"""
        val bar =
          s"""<rect class="btree-walker__bar btree-walker__bar--internal" x="$barX" y="$barY" width="$barW" height="$barH" rx="3"/>"""
        // The lookup path: one page per level, drawn at the leftmost end of each bar.
        val pathDot =
          s"""<circle class="btree-walker__path-dot" cx="${barX + 11}" cy="${barY + barH / 2}" r="6"/>"""
        val pageText =
          s"""<text class="btree-walker__page-count" x="${barX + barW + 10}" y="${barY + 16}" text-anchor="start">${esc(
              s"${humanCount(pages)} page${if pages == 1 then "" else "s"}"
            )}</text>"""
        s"$levelText\n$bar\n$pathDot\n$pageText"
      }
      .mkString("\n")

    // Connect the path dots with a faint vertical line.
    val pathLineX = LeftPad + 11
    val pathLine =
      if depth > 1 then
        val y1 = TopPad + 11
        val y2 = TopPad + (depth - 1) * LevelHeight + 11
        s"""<line class="btree-walker__path-line" x1="$pathLineX" y1="$y1" x2="$pathLineX" y2="$y2"/>"""
      else ""

    // The "seq scan" comparison row at the bottom.
    val seqY        = TopPad + depth * LevelHeight + 24
    val seqBarW     = pagesToBarW(leafPages, maxBarPages)
    val seqBarH     = 22.0
    val seqRow      =
      s"""<text class="btree-walker__level-label btree-walker__level-label--seq" x="${LeftPad - 10}" y="${seqY + 16}" text-anchor="end">Seq scan</text>
         |<rect class="btree-walker__bar btree-walker__bar--seq" x="$LeftPad" y="$seqY" width="$seqBarW" height="$seqBarH" rx="3"/>
         |<text class="btree-walker__page-count" x="${LeftPad + seqBarW + 10}" y="${seqY + 16}" text-anchor="start">${esc(
          s"all ${humanCount(leafPages)} heap pages"
        )}</text>""".stripMargin

    val pathCaption =
      s"""<text class="btree-walker__caption" x="$LeftPad" y="${TopPad - 12}" text-anchor="start">Index lookup path — $depth random page read${if depth == 1 then ""
        else "s"}</text>"""

    s"""<svg viewBox="0 0 $ViewBoxWidth ${seqY + seqBarH + 24}"
       |     class="btree-walker__svg" role="img"
       |     xmlns="http://www.w3.org/2000/svg">
       |  $pathCaption
       |  $pathLine
       |  $treeRows
       |  $seqRow
       |</svg>""".stripMargin

  // ===========================================================================
  // Component
  // ===========================================================================

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useMemoBy(_.payload)(_ => payload => parsePayload(payload))
      .useStateBy(_ => 500) // overridden on mount
      .useEffectOnMountBy { (_, specM, sliderS) =>
        specM.value match
          case Right(spec) =>
            sliderS.setState(rowCountToSlider(spec.rowCount, spec.rowMin, spec.rowMax))
          case _ => Callback.empty
      }
      .render { (_, specM, sliderS) =>
        specM.value match
          case Left(err) =>
            <.div(
              ^.className := "d3-widget__error",
              <.p(^.className   := "d3-widget__error-title", "B-tree-walker widget payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val rowCount  = sliderToRowCount(sliderS.value, spec.rowMin, spec.rowMax)
            val depth     = depthOf(rowCount, spec.fanout, spec.rowsPerPage)
            val leafPages = leafPagesOf(rowCount, spec.rowsPerPage)
            val indexMs   = depth * spec.randomReadMs
            val seqMs     = leafPages * spec.sequentialReadMs
            val speedup   = if indexMs <= 0 then Double.PositiveInfinity else seqMs / indexMs

            <.div(
              ^.className := "btree-walker not-prose",
              spec.title
                .map(t => <.p(^.className := "btree-walker__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              <.div(
                ^.className               := "btree-walker__frame",
                ^.dangerouslySetInnerHtml := buildSvg(spec, rowCount)
              ),
              <.div(
                ^.className := "btree-walker__controls",
                <.label(
                  ^.className := "btree-walker__slider-row",
                  <.span(
                    ^.className := "btree-walker__slider-text",
                    s"Table size: ${humanCount(rowCount)} rows  (≈ ${humanCount(leafPages)} heap pages, fanout ${spec.fanout})"
                  ),
                  <.input(
                    ^.tpe       := "range",
                    ^.className := "btree-walker__slider",
                    ^.min       := "0",
                    ^.max       := SliderTicks.toString,
                    ^.step      := "1",
                    ^.value     := sliderS.value.toString,
                    ^.onChange ==> ((e: ReactEventFromInput) => {
                      val v = Try(e.target.value.toInt).getOrElse(500)
                      sliderS.setState(math.max(0, math.min(SliderTicks, v)))
                    })
                  )
                )
              ),
              <.div(
                ^.className := "btree-walker__readout",
                readoutRow(
                  "Indexed point lookup",
                  s"$depth page reads — ${humanMs(indexMs)}",
                  s"depth ≈ log_${spec.fanout}(${humanCount(leafPages)}) at ${humanMs(spec.randomReadMs)}/page random"
                ),
                readoutRow(
                  "Sequential scan",
                  s"${humanCount(leafPages)} page reads — ${humanMs(seqMs)}",
                  s"every heap page touched, ${humanMs(spec.sequentialReadMs)}/page with read-ahead"
                ),
                readoutRow(
                  "Index speedup",
                  if speedup.isInfinite || speedup < 1.0 then "—" else f"${speedup}%.0f×",
                  "the asymmetry is constant-vs-linear — it compounds with every order of magnitude of growth"
                )
              )
            )
      }

  private def readoutRow(label: String, value: String, note: String): VdomNode =
    <.div(
      ^.className := "btree-walker__readout-row",
      <.span(^.className := "btree-walker__readout-label", label),
      <.span(^.className := "btree-walker__readout-value", value),
      <.span(^.className := "btree-walker__readout-note", note)
    )
