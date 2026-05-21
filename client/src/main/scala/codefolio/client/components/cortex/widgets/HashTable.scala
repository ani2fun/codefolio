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
 * Hash-table stepper — sixth widget in the D3 catalog. Animates the bucket-by-bucket life of a hash table
 * across the four collision-resolution strategies Phase 3 teaches, via a `mode` flag:
 *
 *   - `mode: "chaining"` — buckets hold a linked chain of entries; rendered as a horizontal row of entry
 *     cells with arrows between consecutive entries.
 *   - `mode: "linear"` — open addressing with linear probing; each bucket holds at most one entry; on
 *     collision the next probe is `(h + 1) mod m`.
 *   - `mode: "quadratic"` — open addressing with quadratic probing; on collision the next probe is `(h + i²)
 *     mod m`.
 *   - `mode: "double"` — open addressing with double hashing; on collision the next probe is `(h + i * h2(k))
 *     mod m` (second hash function).
 *
 * The bucket array renders as a vertical column of rows (one per bucket index). For chaining mode a row holds
 * 0..N entry cells separated by arrows; for probing modes a row holds at most one entry cell. A sidecar probe
 * lane to the right of the bucket column shows the probe-sequence cursor + trail when a step's `probe` field
 * is set — the canonical "tried 2, full; tried 3, full; landed at 5" walkthrough.
 *
 * Per-step bucket overrides accumulate over time: each step's `bucketsOverride` is sparse and only lists
 * buckets whose contents change at THAT step. The effective state at step N is `spec.buckets` plus the union
 * of all overrides from step 0 through N. This mirrors how the source insert/delete demos show "step 3:
 * bucket 2 now holds X" without restating buckets 0-1 / 3-4.
 *
 * Per-step `highlight` paints a tinted overlay on one or more buckets — `target` (where the op will land),
 * `miss` (probed and empty), `match` (key found), `collision` (probed and full). Per-step `chainCursor`
 * overlays a small triangle marker over a specific position in a chain (chaining mode only) — used during the
 * search-along-chain walkthrough.
 *
 * Payload schema (JSON):
 * {{{
 * {
 *   "title":            "Insert ('David', 23) — chaining, bucket 1 occupied",
 *   "mode":             "chaining",                       // "chaining" | "linear" | "quadratic" | "double"
 *   "capacity":         5,                                 // bucket count
 *   "primaryHashLabel": "h(k) = hash(k) mod 5",
 *   "secondHashLabel":  "h2(k) = 1 + (k mod 6)",          // mode="double" only
 *   "buckets": [
 *     {"index": 0, "entries": []},
 *     {"index": 1, "entries": [{"key": "Bob", "value": "12"}, {"key": "Carol", "value": "9"}]},
 *     {"index": 2, "entries": [{"key": "Alice", "value": "7"}]}
 *   ],
 *   "steps": [{
 *     "op":              "insert",
 *     "opKey":           "David",
 *     "opValue":         "23",
 *     "bucketsOverride": [{"index": 1, "entries": [...]}],
 *     "probe":           {"indices": [3, 4, 5], "labels": ["h1=3", "+h2=4", "+h2=5"]},
 *     "chainCursor":     {"bucket": 1, "position": 0},
 *     "highlight":       [{"bucket": 1, "kind": "target"}],
 *     "msg":             "Bucket 1 occupied — walk chain looking for 'David'."
 *   }]
 * }
 * }}}
 *
 * Highlight `kind` values outside the canonical set ("target", "miss", "match", "collision") fall back to
 * "target" and log a console warning. Mode-specific decorators are silently ignored for the wrong mode — a
 * `secondHashLabel` in a chaining payload renders nothing; a `chainCursor` in a probing payload is dropped.
 */
object HashTable:

  // ---------------------------------------------------------------------------
  // Schema — every required field validated inside `PayloadDecoder.run`; thrown
  // exceptions collapse to `Left(message)` and the renderer shows an inline
  // error placeholder instead of crashing.
  // ---------------------------------------------------------------------------

  final case class Entry(key: String, value: Option[String], tombstone: Boolean)

  final case class Bucket(index: Int, entries: List[Entry])
  final case class BucketOverride(index: Int, entries: List[Entry])
  final case class Probe(indices: List[Int], labels: List[String])
  final case class ChainCursor(bucket: Int, position: Int)
  final case class Highlight(bucket: Int, kind: String)

  final case class Step(
      bucketsOverride: List[BucketOverride],
      op: Option[String],
      opKey: Option[String],
      opValue: Option[String],
      probe: Option[Probe],
      chainCursor: Option[ChainCursor],
      highlight: List[Highlight],
      msg: String
  )

  final case class Spec(
      mode: String,
      capacity: Int,
      primaryHashLabel: Option[String],
      secondHashLabel: Option[String],
      title: Option[String],
      buckets: List[Bucket],
      steps: List[Step]
  )

  final case class Props(payload: String)

  // ---------------------------------------------------------------------------
  // Modes + highlight kinds. Modes are closed; an unknown value collapses to
  // "chaining" in the parser (the POC mode from the spec).
  // ---------------------------------------------------------------------------

  private val ModeChaining  = "chaining"
  private val ModeLinear    = "linear"
  private val ModeQuadratic = "quadratic"
  private val ModeDouble    = "double"
  private val Modes         = Set(ModeChaining, ModeLinear, ModeQuadratic, ModeDouble)

  private def isProbing(mode: String): Boolean = mode != ModeChaining

  val HighlightKinds: Set[String] = Set("target", "miss", "match", "collision")

  // ---------------------------------------------------------------------------
  // Layout constants — sized to fit comfortably inside a chapter prose column.
  // Probe lane width budgets for a ~4-deep probe sequence plus its label.
  // ---------------------------------------------------------------------------

  private val PaddingX             = 16.0
  private val PaddingY             = 16.0
  private val BucketRowH           = 48.0
  private val BucketRowGap         = 6.0
  private val IndexLabelW          = 22.0
  private val IndexLabelGap        = 8.0
  private val BucketW              = 96.0 // probing mode bucket width
  private val ChainEntryW          = 88.0 // chaining mode per-entry width
  private val ChainEntryH          = 38.0 // chaining mode entry height (slightly shorter than row)
  private val ChainEntryGap        = 24.0 // gap between consecutive chain entries (room for arrow)
  private val ProbeLaneW           = 144.0
  private val ProbeLaneStagger     = 14.0 // x-offset per probe step within the lane
  private val ProbeDotR            = 5.5
  private val ProbeActiveDotR      = 8.0
  private val ProbeLabelOffset     = 12.0 // gap between active dot and its label
  private val ChainCursorH         = 12.0 // triangle height
  private val ChainCursorGap       = 6.0  // gap between cursor and entry top
  private val StepDelayMs          = 1500
  private val TransitionDurationMs = 450.0
  private val SvgNs                = "http://www.w3.org/2000/svg"

  // Arrowhead colour for the chain arrows — kept here so the CSS variable
  // overrides can be added in one place if needed.
  private val ChainArrowFill = "var(--ht-chain-arrow, #94a3b8)"

  // ---------------------------------------------------------------------------
  // Parsing
  // ---------------------------------------------------------------------------

  private def parseEntry(d: js.Dynamic): Entry =
    Entry(
      key = d.string("key"),
      value = d.optString("value"),
      tombstone = d.bool("tombstone", default = false)
    )

  private def parseBucket(d: js.Dynamic): Bucket =
    Bucket(
      index = d.int("index"),
      entries = d.dynList("entries").map(parseEntry).filter(_.key.nonEmpty)
    )

  private def parseBucketOverride(d: js.Dynamic): BucketOverride =
    BucketOverride(
      index = d.int("index"),
      entries = d.dynList("entries").map(parseEntry).filter(_.key.nonEmpty)
    )

  private def parseProbe(d: js.Dynamic): Probe =
    Probe(
      indices = d.intList("indices").getOrElse(Nil),
      labels = d.stringList("labels").getOrElse(Nil)
    )

  private def parseChainCursor(d: js.Dynamic): ChainCursor =
    ChainCursor(
      bucket = d.int("bucket"),
      position = d.int("position")
    )

  private def parseHighlight(d: js.Dynamic): Highlight =
    val rawKind = d.optString("kind").getOrElse("target")
    val kind =
      if HighlightKinds.contains(rawKind) then rawKind
      else
        dom.console.warn(
          s"hash-table: highlight kind '$rawKind' is not in the canonical vocabulary. Rendered as 'target'. " +
            s"Canonical kinds: ${HighlightKinds.toList.sorted.mkString(", ")}."
        )
        "target"
    Highlight(bucket = d.int("bucket"), kind = kind)

  private def parseStep(s: js.Dynamic): Step =
    Step(
      bucketsOverride = s.dynList("bucketsOverride").map(parseBucketOverride),
      op = s.optString("op"),
      opKey = s.optString("opKey"),
      opValue = s.optString("opValue"),
      probe = s.optObj("probe").map(parseProbe),
      chainCursor = s.optObj("chainCursor").map(parseChainCursor),
      highlight = s.dynList("highlight").map(parseHighlight),
      msg = s.string("msg")
    )

  private def parsePayload(json: String): Either[String, Spec] =
    PayloadDecoder.run(json) { d =>
      val rawMode = d.optString("mode").getOrElse(ModeChaining)
      val mode    = if Modes.contains(rawMode) then rawMode else ModeChaining
      val capacity = d
        .optInt("capacity")
        .filter(_ > 0)
        .getOrElse(throw PayloadDecoder.invalid("capacity must be a positive integer"))
      val buckets = d.dynList("buckets").map(parseBucket)
      val steps   = d.dynList("steps").map(parseStep)
      if steps.isEmpty then throw PayloadDecoder.invalid("steps must be non-empty")
      Spec(
        mode = mode,
        capacity = capacity,
        primaryHashLabel = d.optString("primaryHashLabel"),
        secondHashLabel = d.optString("secondHashLabel"),
        title = d.optString("title"),
        buckets = buckets,
        steps = steps
      )
    }

  // ---------------------------------------------------------------------------
  // Effective state — buckets at step N = spec.buckets + accumulated overrides
  // from step 0 through N (sparse overrides; only mentioned buckets mutate).
  // ---------------------------------------------------------------------------

  private def effectiveBuckets(spec: Spec, upToStep: Int): Map[Int, List[Entry]] =
    val base      = spec.buckets.map(b => b.index -> b.entries).toMap
    val overrides = spec.steps.take(upToStep + 1).flatMap(_.bucketsOverride)
    overrides.foldLeft(base)((acc, o) => acc + (o.index -> o.entries))

  // Max chain length across every step (chaining mode only) — determines canvas
  // width. Memoising per Spec isn't worth the complexity; this is O(steps × cap)
  // and only runs once per render.
  private def maxChainLength(spec: Spec): Int =
    if isProbing(spec.mode) then 1
    else
      val perStep = (0 until spec.steps.size).map { stepIdx =>
        effectiveBuckets(spec, stepIdx).values.map(_.size).maxOption.getOrElse(0)
      }
      perStep.maxOption.getOrElse(0).max(1)

  // ---------------------------------------------------------------------------
  // Layout helpers
  // ---------------------------------------------------------------------------

  private val BucketStartX: Double =
    PaddingX + IndexLabelW + IndexLabelGap

  private def bucketRowY(bucketIdx: Int): Double =
    PaddingY + bucketIdx * (BucketRowH + BucketRowGap)

  private def bucketRowCenterY(bucketIdx: Int): Double =
    bucketRowY(bucketIdx) + BucketRowH / 2

  // X of the i-th entry within a chain (chaining mode).
  private def chainEntryX(position: Int): Double =
    BucketStartX + position * (ChainEntryW + ChainEntryGap)

  // Y of an entry (chaining mode) — slightly inset so the row background frames
  // the entries above/below.
  private def chainEntryY(bucketIdx: Int): Double =
    bucketRowY(bucketIdx) + (BucketRowH - ChainEntryH) / 2

  private def bucketWidth(spec: Spec): Double =
    if isProbing(spec.mode) then BucketW
    else
      val n = maxChainLength(spec)
      n * ChainEntryW + (n - 1).max(0) * ChainEntryGap

  private def probeLaneX(spec: Spec): Double =
    BucketStartX + bucketWidth(spec) + 8.0

  private def viewBoxWidth(spec: Spec): Double =
    val coreW = BucketStartX + bucketWidth(spec)
    val totalW =
      if isProbing(spec.mode) then coreW + ProbeLaneW
      else coreW
    totalW + PaddingX

  private def viewBoxHeight(spec: Spec): Double =
    PaddingY * 2 + spec.capacity * BucketRowH + (spec.capacity - 1).max(0) * BucketRowGap

  // ---------------------------------------------------------------------------
  // SVG bootstrap — create the <svg> + a <defs> block with the chain arrowhead.
  // ---------------------------------------------------------------------------

  private def ensureSvg(host: dom.html.Element, spec: Spec, w: Double, h: Double): dom.Element =
    val existing = host.querySelector("svg")
    if existing != null then
      val s = existing.asInstanceOf[dom.Element]
      s.setAttribute("viewBox", s"0 0 $w $h")
      s.setAttribute("width", w.toString)
      s.setAttribute("height", h.toString)
      s
    else
      val svg = dom.document.createElementNS(SvgNs, "svg").asInstanceOf[dom.Element]
      svg.setAttribute("class", s"hash-table__svg hash-table__svg--${spec.mode}")
      svg.setAttribute("role", "img")
      svg.setAttribute("aria-label", spec.title.getOrElse(s"Hash table ${spec.mode}"))
      svg.setAttribute("xmlns", SvgNs)
      svg.setAttribute("viewBox", s"0 0 $w $h")
      svg.setAttribute("width", w.toString)
      svg.setAttribute("height", h.toString)
      // Chain arrowhead — single marker shared across all chain arrows.
      val defs   = dom.document.createElementNS(SvgNs, "defs")
      val marker = dom.document.createElementNS(SvgNs, "marker").asInstanceOf[dom.Element]
      marker.setAttribute("id", "ht-chain-arrow")
      marker.setAttribute("viewBox", "0 0 10 10")
      marker.setAttribute("refX", "9")
      marker.setAttribute("refY", "5")
      marker.setAttribute("markerWidth", "5")
      marker.setAttribute("markerHeight", "5")
      marker.setAttribute("orient", "auto")
      marker.setAttribute("markerUnits", "strokeWidth")
      val path = dom.document.createElementNS(SvgNs, "path").asInstanceOf[dom.Element]
      path.setAttribute("d", "M 0 0 L 10 5 L 0 10 z")
      path.setAttribute("class", "hash-table__arrowhead")
      path.setAttribute("fill", ChainArrowFill)
      marker.appendChild(path)
      defs.appendChild(marker)
      svg.appendChild(defs)
      host.appendChild(svg)
      svg

  // ---------------------------------------------------------------------------
  // Rendering — one flat update pass per step. All selections live at the
  // canvas-group level (not nested per bucket) so D3 enter/update/exit handles
  // the chain growth without complicating the key story.
  // ---------------------------------------------------------------------------

  private def renderStep(svgEl: dom.Element, spec: Spec, stepIdx: Int, animate: Boolean): Unit =
    val svg     = D3.select(svgEl)
    val step    = spec.steps(stepIdx)
    val buckets = effectiveBuckets(spec, stepIdx)
    val probing = isProbing(spec.mode)
    val bw      = bucketWidth(spec)
    val rowX    = BucketStartX

    // ── Canvas group ─────────────────────────────────────────────────────────
    val canvasSel = svg.selectAll("g.hash-table__canvas").data(js.Array(1).asInstanceOf[js.Array[js.Any]])
    val _         = canvasSel.enter().append("g").attr("class", "hash-table__canvas")
    val canvas    = svg.select("g.hash-table__canvas")

    // ── Bucket row backgrounds ───────────────────────────────────────────────
    val rowData: js.Array[js.Any] = (0 until spec.capacity).map { bIdx =>
      js.Dynamic
        .literal(
          bucket = bIdx,
          x = rowX,
          y = bucketRowY(bIdx),
          w = bw,
          h = BucketRowH
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val rowKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"row-${d.asInstanceOf[js.Dynamic].bucket.asInstanceOf[Int]}"
    val rowSel   = canvas.selectAll("rect.hash-table__row").data(rowData, rowKeyFn)
    val rowEnter = rowSel.enter().append("rect").attr("class", "hash-table__row").attr("rx", 6)
    val rowXFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].x
    val rowYFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].y
    val rowWFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].w
    val rowHFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].h
    val _       = rowEnter.attr("x", rowXFn).attr("y", rowYFn).attr("width", rowWFn).attr("height", rowHFn)
    val allRows = canvas.selectAll("rect.hash-table__row")
    val _       = allRows.attr("x", rowXFn).attr("y", rowYFn).attr("width", rowWFn).attr("height", rowHFn)
    val _       = rowSel.exit().remove()

    // ── Index labels (left of each row) ──────────────────────────────────────
    val indexData: js.Array[js.Any] = (0 until spec.capacity).map { bIdx =>
      js.Dynamic
        .literal(
          bucket = bIdx,
          text = bIdx.toString,
          x = PaddingX + IndexLabelW,
          y = bucketRowCenterY(bIdx) + 4
        )
        .asInstanceOf[js.Any]
    }.toJSArray
    val indexKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"idx-${d.asInstanceOf[js.Dynamic].bucket.asInstanceOf[Int]}"
    val indexSel = canvas.selectAll("text.hash-table__index").data(indexData, indexKeyFn)
    val indexEnter =
      indexSel.enter().append("text").attr("class", "hash-table__index").attr("text-anchor", "end")
    val indexXFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].x
    val indexYFn: js.Function2[js.Any, Int, js.Any]    = (d, _) => d.asInstanceOf[js.Dynamic].y
    val indexTextFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].text
    val _      = indexEnter.attr("x", indexXFn).attr("y", indexYFn).text(indexTextFn)
    val allIdx = canvas.selectAll("text.hash-table__index")
    val _      = allIdx.attr("x", indexXFn).attr("y", indexYFn).text(indexTextFn)
    val _      = indexSel.exit().remove()

    // ── Highlight overlays (per bucket; only rendered when present this step) ─
    val highlightById = step.highlight.map(h => h.bucket -> h.kind).toMap
    val highlightData: js.Array[js.Any] = highlightById.toList.flatMap { case (bIdx, kind) =>
      if bIdx < 0 || bIdx >= spec.capacity then None
      else
        Some(
          js.Dynamic
            .literal(
              bucket = bIdx,
              kind = kind,
              x = rowX,
              y = bucketRowY(bIdx),
              w = bw,
              h = BucketRowH
            )
            .asInstanceOf[js.Any]
        )
    }.toJSArray
    val hlKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"hl-${d.asInstanceOf[js.Dynamic].bucket.asInstanceOf[Int]}"
    val hlClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val k = d.asInstanceOf[js.Dynamic].kind.asInstanceOf[String]
      s"hash-table__highlight hash-table__highlight--$k"
    val hlSel   = canvas.selectAll("rect.hash-table__highlight").data(highlightData, hlKeyFn)
    val hlEnter = hlSel.enter().append("rect").attr("class", hlClassFn).attr("rx", 6).attr("opacity", 0)
    val _       = hlEnter.attr("x", rowXFn).attr("y", rowYFn).attr("width", rowWFn).attr("height", rowHFn)
    val _       = hlEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allHls  = canvas.selectAll("rect.hash-table__highlight")
    val _ = allHls.attr("class", hlClassFn).attr("x", rowXFn).attr("y", rowYFn).attr("width", rowWFn).attr(
      "height",
      rowHFn
    )
    val _ = hlSel.exit().remove()

    // ── Entries (across all buckets, flat) ────────────────────────────────────
    val entryData: js.Array[js.Any] = buckets.toList.flatMap { case (bIdx, entries) =>
      entries.zipWithIndex.map { case (entry, position) =>
        val x = if probing then rowX else chainEntryX(position)
        val y = if probing then bucketRowY(bIdx) else chainEntryY(bIdx)
        val w = if probing then bw else ChainEntryW
        val h = if probing then BucketRowH else ChainEntryH
        js.Dynamic
          .literal(
            id = s"$bIdx-${entry.key}",
            bucket = bIdx,
            key = entry.key,
            // Display: "key=value" when value present (chaining/probing both),
            // just "key" otherwise (set/frequency demos). Tombstone overlays an
            // R/strikethrough via CSS on the parent group's class.
            text = entry.value.map(v => s"${entry.key}=$v").getOrElse(entry.key),
            tombstone = entry.tombstone,
            position = position,
            x = x,
            y = y,
            w = w,
            h = h
          )
          .asInstanceOf[js.Any]
      }
    }.toJSArray
    val entryKeyFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].id
    val entryClassFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val tomb = d.asInstanceOf[js.Dynamic].tombstone.asInstanceOf[Boolean]
      val base = "hash-table__entry"
      if tomb then s"$base hash-table__entry--tombstone" else base
    val entryTransformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"

    val entrySel = canvas.selectAll("g.hash-table__entry").data(entryData, entryKeyFn)
    val entryEnter = entrySel
      .enter()
      .append("g")
      .attr("class", entryClassFn)
      .attr("transform", entryTransformFn)
      .attr("opacity", 0)
    val entryRectWFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].w
    val entryRectHFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].h
    val _ = entryEnter
      .append("rect")
      .attr("class", "hash-table__entry-rect")
      .attr("rx", 5)
      .attr("width", entryRectWFn)
      .attr("height", entryRectHFn)
    val entryTextXFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].w.asInstanceOf[Double] / 2
    val entryTextYFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].h.asInstanceOf[Double] / 2 + 4
    val entryTextFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].text
    val _ = entryEnter
      .append("text")
      .attr("class", "hash-table__entry-text")
      .attr("text-anchor", "middle")
      .attr("x", entryTextXFn)
      .attr("y", entryTextYFn)
      .text(entryTextFn)
    val _ = entryEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)

    val allEntries = canvas.selectAll("g.hash-table__entry")
    val _          = allEntries.attr("class", entryClassFn)
    // `.select(...)` (singular) propagates the parent group's CURRENT datum to
    // the child rect / text — same trap as BinaryTree / GraphExplorer: a
    // `.selectAll(...)` here would read the children's stale enter-time data
    // and miss the freshly-bound width / text.
    val _ = allEntries
      .select("rect.hash-table__entry-rect")
      .attr("width", entryRectWFn)
      .attr("height", entryRectHFn)
    val _ = allEntries
      .select("text.hash-table__entry-text")
      .attr("x", entryTextXFn)
      .attr("y", entryTextYFn)
      .text(entryTextFn)
    val _ =
      if animate then
        allEntries
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("transform", entryTransformFn)
          .attr("opacity", 1)
      else allEntries.attr("transform", entryTransformFn).attr("opacity", 1)
    val _ = entrySel.exit().remove()

    // ── Chain arrows (chaining mode only) ────────────────────────────────────
    val arrowData: js.Array[js.Any] =
      if probing then js.Array()
      else
        buckets.toList.flatMap { case (bIdx, entries) =>
          entries.zipWithIndex.sliding(2).flatMap {
            case List((from, fromIdx), (to, toIdx)) =>
              val x1 = chainEntryX(fromIdx) + ChainEntryW
              val x2 = chainEntryX(toIdx)
              val y  = bucketRowCenterY(bIdx)
              Some(
                js.Dynamic
                  .literal(
                    id = s"arrow-$bIdx-${from.key}->${to.key}",
                    x1 = x1,
                    x2 = x2,
                    y = y
                  )
                  .asInstanceOf[js.Any]
              )
            case _ => None
          }.toList
        }.toJSArray
    val arrowKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => d.asInstanceOf[js.Dynamic].id
    val arrowDFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x1  = dyn.x1.asInstanceOf[Double]
      val x2  = dyn.x2.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"M $x1 $y L $x2 $y"
    val arrowSel = canvas.selectAll("path.hash-table__arrow").data(arrowData, arrowKeyFn)
    val arrowEnter = arrowSel
      .enter()
      .append("path")
      .attr("class", "hash-table__arrow")
      .attr("d", arrowDFn)
      .attr("marker-end", "url(#ht-chain-arrow)")
      .attr("opacity", 0)
    val _         = arrowEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allArrows = canvas.selectAll("path.hash-table__arrow")
    val _ =
      if animate then
        allArrows
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("d", arrowDFn)
          .attr("opacity", 1)
      else allArrows.attr("d", arrowDFn).attr("opacity", 1)
    val _ = arrowSel.exit().remove()

    // ── Chain cursor (chaining mode only; triangle above an entry) ──────────
    val cursorData: js.Array[js.Any] =
      if probing then js.Array()
      else
        step.chainCursor.toList.flatMap { c =>
          val entries = buckets.getOrElse(c.bucket, Nil)
          if c.position < 0 || c.position >= entries.size then Nil
          else
            val x = chainEntryX(c.position) + ChainEntryW / 2
            val y = chainEntryY(c.bucket) - ChainCursorGap
            List(
              js.Dynamic
                .literal(
                  x = x,
                  y = y
                )
                .asInstanceOf[js.Any]
            )
        }.toJSArray
    val cursorKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "chain-cursor"
    val cursorTransformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val x   = dyn.x.asInstanceOf[Double]
      val y   = dyn.y.asInstanceOf[Double]
      s"translate($x, $y)"
    val cursorSel = canvas.selectAll("g.hash-table__chain-cursor").data(cursorData, cursorKeyFn)
    val cursorEnter = cursorSel
      .enter()
      .append("g")
      .attr("class", "hash-table__chain-cursor")
      .attr("transform", cursorTransformFn)
      .attr("opacity", 0)
    val _ = cursorEnter
      .append("path")
      .attr("class", "hash-table__chain-cursor-arrow")
      // Downward-pointing triangle, base above the entry, apex at (0, 0).
      .attr("d", s"M ${-ChainCursorH / 2} ${-ChainCursorH} L ${ChainCursorH / 2} ${-ChainCursorH} L 0 0 Z")
    val _          = cursorEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allCursors = canvas.selectAll("g.hash-table__chain-cursor")
    // Snap-set transform + opacity directly. `transition().attr(...)` for
    // singleton-keyed elements (constant key fn) doesn't reliably propagate
    // the new attr across step renders — the data IS rebound (we see
    // updated `__data__` on the element) but the transition attr stays at
    // the prior value. Setting directly sidesteps the transition entirely;
    // the small cursor jump isn't worth fighting D3 for.
    val _ = allCursors.attr("transform", cursorTransformFn).attr("opacity", 1)
    val _ = cursorSel.exit().remove()

    // ── Probe trail + cursor (probing modes only) ────────────────────────────
    renderProbe(canvas, spec, step, probing, animate)

  private def renderProbe(
      canvas: D3.Selection,
      spec: Spec,
      step: Step,
      probing: Boolean,
      animate: Boolean
  ): Unit =
    val laneX = probeLaneX(spec)
    // Trail: all probes EXCEPT the last (singleton cursor). Keyed by probe
    // index so the trail extends as the probe sequence grows.
    val trailData: js.Array[js.Any] =
      if !probing then js.Array()
      else
        step.probe.toList.flatMap { p =>
          if p.indices.isEmpty then Nil
          else
            val indexed = p.indices.zipWithIndex
            indexed.init.map { case (bucketIdx, i) =>
              js.Dynamic
                .literal(
                  id = i,
                  cx = laneX + i * ProbeLaneStagger,
                  cy = bucketRowCenterY(bucketIdx)
                )
                .asInstanceOf[js.Any]
            }
        }.toJSArray
    val trailKeyFn: js.Function2[js.Any, Int, js.Any] =
      (d, _) => s"trail-${d.asInstanceOf[js.Dynamic].id.asInstanceOf[Int]}"
    val trailCXFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].cx
    val trailCYFn: js.Function2[js.Any, Int, js.Any] = (d, _) => d.asInstanceOf[js.Dynamic].cy
    val trailSel = canvas.selectAll("circle.hash-table__probe-trail").data(trailData, trailKeyFn)
    val trailEnter = trailSel
      .enter()
      .append("circle")
      .attr("class", "hash-table__probe-trail")
      .attr("r", ProbeDotR)
      .attr("cx", trailCXFn)
      .attr("cy", trailCYFn)
      .attr("opacity", 0)
    val _         = trailEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allTrails = canvas.selectAll("circle.hash-table__probe-trail")
    val _ =
      if animate then
        allTrails
          .transition()
          .duration(TransitionDurationMs)
          .ease(D3.easeCubicInOut)
          .attr("cx", trailCXFn)
          .attr("cy", trailCYFn)
          .attr("opacity", 1)
      else allTrails.attr("cx", trailCXFn).attr("cy", trailCYFn).attr("opacity", 1)
    val _ = trailSel.exit().remove()

    // Cursor: singleton (one per step) at the LAST probe index. Includes the
    // active label next to it. Keyed by a constant so the cursor slides smoothly
    // between steps when the active bucket changes.
    val cursorData: js.Array[js.Any] =
      if !probing then js.Array()
      else
        step.probe.toList.flatMap { p =>
          p.indices.lastOption.toList.map { bucketIdx =>
            val lastI = p.indices.size - 1
            val label = p.labels.lift(lastI).getOrElse("")
            js.Dynamic
              .literal(
                cx = laneX + lastI * ProbeLaneStagger,
                cy = bucketRowCenterY(bucketIdx),
                label = label
              )
              .asInstanceOf[js.Any]
          }
        }.toJSArray
    val cursorKeyFn: js.Function2[js.Any, Int, js.Any] = (_, _) => "probe-cursor"
    val cursorTransformFn: js.Function2[js.Any, Int, js.Any] = (d, _) =>
      val dyn = d.asInstanceOf[js.Dynamic]
      val cx  = dyn.cx.asInstanceOf[Double]
      val cy  = dyn.cy.asInstanceOf[Double]
      s"translate($cx, $cy)"
    val cursorSel = canvas.selectAll("g.hash-table__probe-cursor").data(cursorData, cursorKeyFn)
    val cursorEnter = cursorSel
      .enter()
      .append("g")
      .attr("class", "hash-table__probe-cursor")
      .attr("transform", cursorTransformFn)
      .attr("opacity", 0)
    val _ = cursorEnter
      .append("circle")
      .attr("class", "hash-table__probe-cursor-dot")
      .attr("r", ProbeActiveDotR)
    val _ = cursorEnter
      .append("text")
      .attr("class", "hash-table__probe-cursor-label")
      .attr("text-anchor", "start")
      .attr("x", ProbeLabelOffset)
      .attr("y", 4)
      .text(((d, _) => d.asInstanceOf[js.Dynamic].label): js.Function2[js.Any, Int, js.Any])
    val _          = cursorEnter.transition().duration(TransitionDurationMs / 2).attr("opacity", 1)
    val allCursors = canvas.selectAll("g.hash-table__probe-cursor")
    val _ = allCursors
      .select("text.hash-table__probe-cursor-label")
      .text(((d, _) => d.asInstanceOf[js.Dynamic].label): js.Function2[js.Any, Int, js.Any])
    // Snap-set transform + opacity (same singleton-key transition trap as
    // the chain cursor above — `transition().attr(...)` on a constant-keyed
    // selection doesn't propagate the new datum's coords across step renders).
    val _ = allCursors.attr("transform", cursorTransformFn).attr("opacity", 1)
    val _ = cursorSel.exit().remove()

  // ---------------------------------------------------------------------------
  // Component — React owns the host div; D3 owns the SVG inside it. The hash
  // labels + op banner above the SVG live in React, not the SVG canvas — they
  // don't animate per step and React handles them more cleanly.
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
              val w       = viewBoxWidth(spec)
              val h       = viewBoxHeight(spec)
              val svgEl   = ensureSvg(host, spec, w, h)
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
              <.p(^.className   := "d3-widget__error-title", "Hash-table payload error"),
              <.pre(^.className := "d3-widget__error-message", err)
            )
          case Right(spec) =>
            val count = spec.steps.size
            val idx   = stepper.index
            val currentStep =
              if count == 0 then Step(Nil, None, None, None, None, None, Nil, "No steps defined.")
              else spec.steps(idx)

            val opBanner: VdomNode = currentStep.op match
              case Some(op) =>
                val keyPart   = currentStep.opKey.map(k => s" $k").getOrElse("")
                val valuePart = currentStep.opValue.map(v => s" = $v").getOrElse("")
                <.p(^.className := "hash-table__op", s"$op$keyPart$valuePart")
              case None => EmptyVdom

            val hashLabels: VdomNode =
              val primary = spec.primaryHashLabel
              val second  = if spec.mode == ModeDouble then spec.secondHashLabel else None
              (primary, second) match
                case (None, _) => EmptyVdom
                case (Some(p), None) =>
                  <.p(^.className := "hash-table__hash-label", p)
                case (Some(p), Some(s2)) =>
                  <.div(
                    ^.className := "hash-table__hash-labels",
                    <.p(^.className := "hash-table__hash-label", p),
                    <.p(^.className := "hash-table__hash-label", s2)
                  )

            val controls: VdomNode =
              if count <= 1 then EmptyVdom
              else
                <.div(
                  ^.className := "hash-table__controls",
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.previous,
                    ^.disabled   := stepper.atStart,
                    ^.aria.label := "Previous step",
                    ^.className  := "hash-table__button",
                    LucideIcons.ArrowLeft(LucideIcons.withClass("hash-table__button-icon")),
                    "Prev"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.togglePlay,
                    ^.disabled   := count == 0,
                    ^.aria.label := (if stepper.isPlaying then "Pause" else "Play"),
                    ^.className  := "hash-table__button hash-table__button--primary",
                    if stepper.isPlaying then
                      LucideIcons.Pause(LucideIcons.withClass("hash-table__button-icon"))
                    else LucideIcons.Play(LucideIcons.withClass("hash-table__button-icon")),
                    if stepper.isPlaying then "Pause" else "Play"
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.next,
                    ^.disabled   := stepper.atEnd,
                    ^.aria.label := "Next step",
                    ^.className  := "hash-table__button",
                    "Next",
                    LucideIcons.ArrowRight(LucideIcons.withClass("hash-table__button-icon"))
                  ),
                  <.button(
                    ^.tpe := "button",
                    ^.onClick --> stepper.reset,
                    ^.disabled   := stepper.atStart && !stepper.isPlaying,
                    ^.aria.label := "Reset",
                    ^.className  := "hash-table__button hash-table__button--icon",
                    LucideIcons.RotateCcw(LucideIcons.withClass("hash-table__button-icon"))
                  ),
                  <.span(
                    ^.className := "hash-table__progress",
                    s"Step ${idx + 1} / ${math.max(1, count)}"
                  )
                )

            <.div(
              ^.className := s"hash-table hash-table--${spec.mode} not-prose",
              spec.title
                .map(t => <.p(^.className := "hash-table__title", t): VdomNode)
                .getOrElse(EmptyVdom),
              hashLabels,
              opBanner,
              <.div(
                ^.className := "hash-table__frame"
              ).withRef(hostRef),
              <.p(
                ^.className := "hash-table__caption",
                ^.aria.live := "polite",
                if currentStep.msg.nonEmpty then currentStep.msg else " "
              ),
              controls
            )
      }
