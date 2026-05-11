package codefolio.client.components.cortex

import codefolio.shared.cortex.{Block, BlockDecodeError, Blocks}
import org.scalajs.dom

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Walk a rendered Chapter's `<article>` for Cortex Block placeholders, extract their attribute / child data,
 * URI-decode and JSON-parse where the markdown pipeline encoded them, and call into [[Blocks]] for structural
 * validation. The pure decoders live in `shared/`; this module is the JS-side adapter that owns the DOM walk
 * + URI / JSON shims that can't cross-compile to JVM.
 *
 * Errors are returned in [[DiscoveryResult.errors]] *and* simultaneously logged via `dom.console.warn` —
 * preserves the pre-extraction observable behaviour so existing diagnostics still work.
 */
object BlockDiscovery:

  /**
   * The outcome of one discovery pass over an article.
   *
   *   - `blocks` is the list of successfully-decoded `(placeholder, Block)` pairs in document order, ready to
   *     be rendered + mounted.
   *   - `errors` is the list of `(placeholder, BlockDecodeError)` pairs for placeholders that failed
   *     validation. Already logged to the console; surfaced here so callers (or future tests) can observe.
   */
  final case class DiscoveryResult(
      blocks: List[(dom.HTMLElement, Block)],
      errors: List[(dom.HTMLElement, BlockDecodeError)]
  )

  def discover(article: dom.Element): DiscoveryResult =
    val blocks = List.newBuilder[(dom.HTMLElement, Block)]
    val errors = List.newBuilder[(dom.HTMLElement, BlockDecodeError)]

    Discoverers.foreach { d =>
      val nodes = article.querySelectorAll(s"div.${d.className}")
      var i     = 0
      while i < nodes.length do
        val node = nodes.item(i).asInstanceOf[dom.HTMLElement]
        d.decode(node) match
          case Right(block) => blocks += node -> block
          case Left(err) =>
            warn(d.className, err)
            errors += node -> err
        i += 1
    }

    DiscoveryResult(blocks.result(), errors.result())

  // ---------------------------------------------------------------------------
  // Per-placeholder discoverers. Each owns its CSS class + the JS-side extraction
  // step (attribute reads, URI-decoding, JSON parsing of `data-tabs`, child-walk
  // for `d2-slides`). Structural validation is delegated to `Blocks.*`.
  // ---------------------------------------------------------------------------

  private trait Discoverer:
    def className: String
    def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block]

  private val Discoverers: List[Discoverer] =
    List(RunnableCode, RunnableGroup, Mermaid, D2Slides, D2Inline)

  private object RunnableCode extends Discoverer:
    override val className: String = "runnable-code"

    override def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block] =
      val lang   = nonEmpty(node.getAttribute("data-lang"))
      val src    = nonEmpty(node.getAttribute("data-source")).flatMap(uriDecode)
      val label  = nonEmpty(node.getAttribute("data-language-label"))
      Blocks.decodeRunnableCode(lang, src, label)

  private object RunnableGroup extends Discoverer:
    override val className: String = "runnable-group"

    override def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block] =
      val raw  = nonEmpty(node.getAttribute("data-tabs"))
      val tabs = raw.flatMap(uriDecode).map(parseRawTabs).getOrElse(Nil)
      Blocks.decodeRunnableGroup(tabs)

    // Defensive JSON → List[RawTab]. Anything that throws (malformed JSON, non-array root)
    // collapses to an empty list, which `Blocks.decodeRunnableGroup` then rejects as
    // `EmptyContent` — same observable outcome as the pre-extraction code's "skip the group".
    private def parseRawTabs(decoded: String): List[Blocks.RawTab] =
      Try {
        val parsed = js.JSON.parse(decoded).asInstanceOf[js.Array[js.Dynamic]]
        parsed.toList.map { obj =>
          Blocks.RawTab(
            language = obj.language.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty),
            languageLabel = obj.languageLabel.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty),
            source = obj.source.asInstanceOf[js.UndefOr[String]].toOption.filter(_.nonEmpty),
            runnable = obj.runnable.asInstanceOf[js.UndefOr[Boolean]].toOption
          )
        }
      } match
        case Success(v) => v
        case Failure(t) =>
          dom.console.warn(s"chapter: skipping runnable-group — malformed data-tabs JSON: ${t.getMessage}")
          Nil

  private object Mermaid extends Discoverer:
    override val className: String = "mermaid-block"

    override def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block] =
      val src = nonEmpty(node.getAttribute("data-mermaid-source")).flatMap(uriDecode)
      Blocks.decodeMermaid(src)

  private object D2Slides extends Discoverer:
    override val className: String = "d2-slides"

    override def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block] =
      val slides = (0 until node.children.length).iterator.flatMap { j =>
        val child = node.children.item(j).asInstanceOf[dom.HTMLElement]
        if child.classList.contains("d2-slide") && child.innerHTML.nonEmpty then Some(child.innerHTML)
        else None
      }.toList
      val caption = nonEmpty(node.getAttribute("data-caption"))
      Blocks.decodeD2Slides(slides, caption)

  private object D2Inline extends Discoverer:
    override val className: String = "d2-diagram"

    override def decode(node: dom.HTMLElement): Either[BlockDecodeError, Block] =
      Blocks.decodeD2Inline(node.innerHTML)

  // ---------------------------------------------------------------------------
  // Shims
  // ---------------------------------------------------------------------------

  private def nonEmpty(s: String): Option[String] =
    Option(s).filter(_.nonEmpty)

  // `decodeURIComponent` throws URIError on a malformed escape sequence (e.g. `%2`).
  // Wrap so one bad block doesn't kill the rest of the discovery pass; the caller
  // surfaces the absence as a `MissingAttribute` (the JS-side decode produced no value).
  private def uriDecode(encoded: String): Option[String] =
    Try(js.Dynamic.global.decodeURIComponent(encoded).asInstanceOf[String]) match
      case Success(v) => Some(v)
      case Failure(t) =>
        dom.console.warn(s"chapter: malformed URI in placeholder: ${t.getMessage}")
        None

  private def warn(blockKind: String, err: BlockDecodeError): Unit = err match
    case BlockDecodeError.MissingAttribute(_, attr) =>
      dom.console.warn(s"chapter: skipping $blockKind — missing $attr")
    case BlockDecodeError.EmptyContent(_, what) =>
      dom.console.warn(s"chapter: skipping $blockKind — empty $what")
    case BlockDecodeError.MalformedTab(i, missing) =>
      dom.console.warn(s"chapter: skipping $blockKind — tab $i missing $missing")
