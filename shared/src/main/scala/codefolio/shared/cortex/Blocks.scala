package codefolio.shared.cortex

/**
 * Cortex **Blocks** — typed payloads of the placeholder `<div>`s that the markdown pipeline emits inside a
 * Chapter's rendered HTML. The pipeline emits five flavours: `RunnableCode`, `RunnableGroup`, `Mermaid`,
 * `D2Slides`, `D2Inline`. Each one mounts a Scala.js React component on the client.
 *
 * This module owns the **structural validation** that turns raw attribute / child data into a typed `Block`
 * (or a `BlockDecodeError`). It runs identically on the JVM and on Scala.js so the validation is unit-testable
 * without a DOM. The Scala.js DOM walk + URI / JSON shims live in `client.components.cortex.BlockDiscovery`,
 * which feeds these decoders.
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
