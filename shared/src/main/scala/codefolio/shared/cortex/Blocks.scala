package codefolio.shared.cortex

/**
 * Cortex **Blocks** — typed payloads of the placeholder `<div>`s that the markdown pipeline emits inside a
 * Chapter's rendered HTML. The pipeline emits seven flavours: `RunnableCode`, `RunnableGroup`, `Mermaid`,
 * `D2Slides`, `D2Inline`, `D3Widget`, `TracedCode`. Each one mounts a Scala.js React component on the client.
 *
 * This module owns the **structural validation** that turns raw attribute / child data into a typed `Block`
 * (or a `BlockDecodeError`). It runs identically on the JVM and on Scala.js so the validation is
 * unit-testable without a DOM. The Scala.js DOM walk + URI / JSON shims live in
 * `client.components.cortex.BlockDiscovery`, which feeds these decoders.
 *
 * Mirrors ADR-0004's server-side bytes↔values split: live wire adapters do the IO, pure modules do the
 * shape-checking, tests bypass IO and exercise the pure modules directly.
 */
object Blocks:

  /**
   * `RawTab` is the JSON-decoded shape of one entry in a `data-tabs` array. Every field is `Option` because
   * the JSON arrives untyped — `js.JSON.parse` on the client produces `js.Dynamic`, and the field-by-field
   * extraction can yield a missing string. The shared decoder validates required-vs-optional centrally.
   */
  final case class RawTab(
      language: Option[String],
      languageLabel: Option[String],
      source: Option[String],
      runnable: Option[Boolean]
  )

  /**
   * Decode a `runnable-code` placeholder.
   *
   *   - `language` (from `data-lang`) and `source` (from URI-decoded `data-source`) are required.
   *   - `languageLabel` (from `data-language-label`) is optional — falls through as `None`.
   */
  def decodeRunnableCode(
      language: Option[String],
      source: Option[String],
      languageLabel: Option[String]
  ): Either[BlockDecodeError, Block.RunnableCode] =
    for
      lang <- language.toRight(BlockDecodeError.MissingAttribute("runnable-code", "data-lang"))
      src  <- source.toRight(BlockDecodeError.MissingAttribute("runnable-code", "data-source"))
    yield Block.RunnableCode(lang, src, languageLabel.filter(_.nonEmpty))

  /**
   * Decode a `runnable-group` placeholder. Empty `tabs` is treated as a "skip me" signal — the markdown
   * pipeline can emit a placeholder with no children if the source group block is empty, and rendering an
   * empty tab strip is worse than rendering nothing.
   *
   * Each `RawTab` requires `language`, `languageLabel`, and `source`. `runnable` defaults to `true` when
   * absent (older payloads / future tabs that omit the field still get a runnable tab).
   */
  def decodeRunnableGroup(
      rawTabs: List[RawTab]
  ): Either[BlockDecodeError, Block.RunnableGroup] =
    if rawTabs.isEmpty then Left(BlockDecodeError.EmptyContent("runnable-group", "tabs"))
    else
      val zero: Either[BlockDecodeError, List[Block.Tab]] = Right(Nil)
      val converted = rawTabs.zipWithIndex.foldLeft(zero) {
        case (Left(e), _) => Left(e)
        case (Right(acc), (raw, i)) =>
          for
            lang  <- raw.language.toRight(BlockDecodeError.MalformedTab(i, "language"))
            label <- raw.languageLabel.toRight(BlockDecodeError.MalformedTab(i, "languageLabel"))
            src   <- raw.source.toRight(BlockDecodeError.MalformedTab(i, "source"))
          yield acc :+ Block.Tab(lang, label, src, raw.runnable.getOrElse(true))
      }
      converted.map(Block.RunnableGroup(_))

  /** Decode a `mermaid-block` placeholder. Source (URI-decoded `data-mermaid-source`) is required. */
  def decodeMermaid(
      source: Option[String]
  ): Either[BlockDecodeError, Block.Mermaid] =
    source
      .toRight(BlockDecodeError.MissingAttribute("mermaid-block", "data-mermaid-source"))
      .map(Block.Mermaid(_))

  /**
   * Decode a `d2-slides` placeholder. `slides` is the inner-HTML of each `div.d2-slide` child element, in
   * order. Empty `slides` is treated as "skip me" (no useful diagram to render). Caption is optional;
   * empty-string captions are normalised to `None` to match the original behaviour.
   */
  def decodeD2Slides(
      slides: List[String],
      caption: Option[String]
  ): Either[BlockDecodeError, Block.D2Slides] =
    if slides.isEmpty then Left(BlockDecodeError.EmptyContent("d2-slides", "slides"))
    else Right(Block.D2Slides(slides, caption.filter(_.nonEmpty)))

  /**
   * Decode a `d2-diagram` placeholder. `svgHtml` is the inner-HTML of the placeholder element (the rendered
   * SVG markup). Empty content → skip.
   */
  def decodeD2Inline(
      svgHtml: String
  ): Either[BlockDecodeError, Block.D2Inline] =
    if svgHtml.isEmpty then Left(BlockDecodeError.EmptyContent("d2-diagram", "innerHTML"))
    else Right(Block.D2Inline(svgHtml))

  /**
   * Decode a `d3-widget` placeholder. `widget` (from `data-widget`) names a Scala.js + D3 component in the
   * client-side catalog; `payload` (URI-decoded `data-payload`) is the raw JSON the widget interprets. The
   * shared decoder only validates that both are present and non-empty — per-widget schema validation lives in
   * the widget itself, mirroring the `D2Slides(slides: List[String])` precedent where shared keeps the
   * payload structural and the client renderer parses it.
   */
  def decodeD3Widget(
      widget: Option[String],
      payload: Option[String]
  ): Either[BlockDecodeError, Block.D3Widget] =
    for
      name <- widget.toRight(BlockDecodeError.MissingAttribute("d3-widget", "data-widget"))
      data <- payload.toRight(BlockDecodeError.MissingAttribute("d3-widget", "data-payload"))
    yield Block.D3Widget(name, data)

  /**
   * Decode a `traced-code-block` placeholder. `language` (from `data-lang`) is the runtime to execute under
   * (only "python" is supported in v1; the field is kept so adding Java later doesn't require an attribute
   * rename). `source` (URI-decoded `data-source`) is the user program. The actual `sys.settrace` wrapper
   * lives on the client — the server-side `/api/run` is unchanged. See ADR-0007.
   */
  def decodeTracedCode(
      language: Option[String],
      source: Option[String]
  ): Either[BlockDecodeError, Block.TracedCode] =
    for
      lang <- language.toRight(BlockDecodeError.MissingAttribute("traced-code-block", "data-lang"))
      src  <- source.toRight(BlockDecodeError.MissingAttribute("traced-code-block", "data-source"))
    yield Block.TracedCode(lang, src)

  /**
   * Decode a `likec4-iframe` placeholder. `src` (from `data-src`) is the URL of the LikeC4 view (e.g.
   * `/c4/view/foo`) and is the only required attribute. `height` (from `data-height`) is the inline iframe
   * height in pixels; malformed values fall through as `None` so the renderer uses its default. `title` (from
   * `data-title`) is the accessible label for the iframe; empty-string is normalised to `None`.
   *
   * Validation stays minimal — the LikeC4 SPA itself decides what to render for a given `src`, and a 404
   * inside the iframe is a runtime concern, not a placeholder-decode concern.
   */
  def decodeLikeC4(
      src: Option[String],
      height: Option[String],
      title: Option[String]
  ): Either[BlockDecodeError, Block.LikeC4] =
    src
      .toRight(BlockDecodeError.MissingAttribute("likec4-iframe", "data-src"))
      .map(s => Block.LikeC4(s, height.flatMap(_.toIntOption), title.filter(_.nonEmpty)))

/**
 * The decoded payload of a single placeholder `<div>` in a rendered Chapter. Each variant maps 1:1 to a
 * Scala.js React component on the client; the renderer (`ChapterContent`) is a total `Block => VdomElement`
 * dispatch.
 */
sealed trait Block

object Block:

  final case class RunnableCode(
      language: String,
      source: String,
      languageLabel: Option[String]
  ) extends Block

  final case class Tab(
      language: String,
      languageLabel: String,
      source: String,
      runnable: Boolean
  )

  final case class RunnableGroup(tabs: List[Tab]) extends Block

  final case class Mermaid(source: String) extends Block

  final case class D2Slides(
      slides: List[String],
      caption: Option[String]
  ) extends Block

  final case class D2Inline(svgHtml: String) extends Block

  /**
   * Named entry in the client-side D3 widget catalog plus the raw JSON payload it interprets. Schema is
   * deliberately loose at this layer — each widget owns its own decoder, so the catalog can grow without
   * regenerating shared types.
   */
  final case class D3Widget(widget: String, payload: String) extends Block

  /**
   * Step-through visualisation for a code block whose execution we want to inspect. `language` is the runtime
   * ("python" in v1); `source` is the user program. On the client, a `sys.settrace` harness wraps the source
   * and posts to the existing `/api/run`; the trace is parsed out of stdout and rendered as code + locals
   * panel + step controls.
   */
  final case class TracedCode(language: String, source: String) extends Block

  /**
   * Embedded LikeC4 diagram view, surfaced as an `<iframe>` pointing at the LikeC4 SPA (proxied under `/c4`).
   * `src` is the upstream URL (e.g. `/c4/view/foundations_cp_cluster_containers`); `height` is the optional
   * inline pixel height the author asked for (renderer falls back to a default when absent); `title` is the
   * optional accessible label (already markdown-author-controlled via the original `title` attribute).
   *
   * The component wraps the iframe with a hover-visible Zoom button that opens a near-fullscreen modal —
   * mirrors the `D2Inline` affordance but delegates pan/zoom inside the diagram to the LikeC4 SPA itself.
   */
  final case class LikeC4(src: String, height: Option[Int], title: Option[String]) extends Block

/**
 * Why a placeholder `<div>` could not be turned into a `Block`. Both the JVM specs and the Scala.js
 * `BlockDiscovery` adapter use these — the adapter logs them via `console.warn` and continues (preserving
 * pre-extraction behaviour: one bad block doesn't block the rest of the chapter).
 */
sealed trait BlockDecodeError

object BlockDecodeError:
  /** A required HTML attribute was absent (or URI-decoding produced no value). */
  final case class MissingAttribute(blockKind: String, attr: String) extends BlockDecodeError

  /** Required content was empty (e.g. a `d2-diagram` with no inner SVG). */
  final case class EmptyContent(blockKind: String, what: String) extends BlockDecodeError

  /** A tab inside a `runnable-group` was missing a required field. `index` is the tab position (0-based). */
  final case class MalformedTab(index: Int, missingField: String) extends BlockDecodeError
