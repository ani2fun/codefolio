package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.HashScroll
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Owns the full pipeline that turns a rendered markdown HTML string into a mounted, interactive Cortex
 * Chapter: drops the HTML into an `<article>` via `dangerouslySetInnerHTML`, walks it after mount, and
 * React-portal-mounts Scala.js components into the placeholder divs the TS markdown pipeline emits. The
 * placeholder catalog (Runnable Code Blocks, Mermaid, D2) was previously a separate file
 * (`ChapterPlaceholders`) but each catalog block had to know about DOM nodes and the renderer had to iterate
 * over opaque `(node, vdom)` tuples — folding the two together puts both halves of the pipeline in one place
 * and makes the catalog the natural unit-test surface.
 *
 * Doesn't render the TOC itself — `props.result.toc` is exposed for the sidebar/heading-tracker to consume
 * separately.
 *
 * Roots are unmounted on each chapter change so we don't leak component trees.
 */
object ChapterContent:

  final case class Props(result: MarkdownRenderer.Result)

  // We can't depend on japgolly's createRoot wrapper here (its API is wrapped); call ReactDOMClient
  // directly via a tiny facade.
  @js.native
  @js.annotation.JSImport("react-dom/client", "createRoot")
  private def createRoot(container: dom.Node): RootHandle = js.native

  @js.native
  private trait RootHandle extends js.Object:
    def render(node: js.Any): Unit = js.native
    def unmount(): Unit            = js.native

  /**
   * Mount a scalajs-react `Unmounted` into a DOM container by routing it through `ReactDOMClient.createRoot`.
   * Returns the root handle so the caller can unmount on cleanup.
   */
  private def mount(container: dom.Node, vdom: VdomElement): RootHandle =
    val root = createRoot(container)
    root.render(vdom.rawElement.asInstanceOf[js.Any])
    root

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useRefToVdom[dom.html.Element]
      // Track the React roots we mount into placeholder divs. Stored in a mutable ref to survive
      // re-renders without triggering them.
      .useRefBy(_ => js.Array[RootHandle]())
      // Re-decorate whenever the rendered HTML changes (i.e. new chapter). The article's innerHTML
      // is set via dangerouslySetInnerHTML; React re-runs that synchronously when `props.result.html`
      // changes, so by the time this effect fires the DOM is in sync.
      .useEffectWithDepsBy((props, _, _) => props.result.html) {
        (_, articleRef, rootsRef) => _ =>
          val tearDown: Callback = Callback {
            val prev = rootsRef.value
            for i <- 0 until prev.length do Try(prev(i).unmount())
            rootsRef.value = js.Array()
          }

          // articleRef.foreach's lambda is `Element => Unit`; the body runs imperatively when the
          // resulting Callback fires. Wrapping the body in `Callback { … }` would silently drop it
          // (a `-Wvalue-discard` warning at compile time, no mount at runtime — the bug that left
          // mermaid/runnable/d2 placeholders empty).
          val mountAll: Callback = articleRef.foreach { article =>
            article.addEventListener("click", onArticleClick)

            val newRoots = js.Array[RootHandle]()
            discover(article).foreach { case (node, vdom) =>
              val _ = newRoots.push(mount(node, vdom))
            }
            rootsRef.value = newRoots
            val _ = dom.window.setTimeout(() => HashScroll.scrollToCurrentHash(), 0)
          }

          tearDown >> mountAll
      }
      // Note: we don't add an unmount-only cleanup hook for the React roots. useEffectWithDepsBy(html)
      // already tears down the previous roots on every chapter change, which covers the common case.
      // If the parent unmounts the whole ChapterContent the placeholder DOM nodes go too, and the
      // orphaned React roots are harmless GC garbage.
      .render { (props, articleRef, _) =>
        <.article.withRef(articleRef)(
          ^.className               := "chapter-content",
          ^.dangerouslySetInnerHtml := props.result.html
        )
      }

  // ---- Block catalog -----------------------------------------------------

  // Walk the article DOM and return every recognised block as a `(node, vdom)` pair, ready to mount.
  // Skips placeholder divs whose required attributes are missing or malformed (decoder failures are
  // logged via `dom.console.warn`; one bad block doesn't break the rest of the chapter).
  private def discover(article: dom.Element): List[(dom.HTMLElement, VdomElement)] =
    blocks.flatMap { block =>
      val nodes = article.querySelectorAll(s"div.${block.className}")
      (0 until nodes.length).iterator.flatMap { i =>
        val node = nodes.item(i).asInstanceOf[dom.HTMLElement]
        block.render(node).map(vdom => (node, vdom))
      }.toList
    }

  private trait Block:
    def className: String
    def render(node: dom.HTMLElement): Option[VdomElement]

  // Every block type the markdown pipeline knows how to emit. Order doesn't matter.
  private val blocks: List[Block] = List(RunnableCode, RunnableGroup, Mermaid, D2Slides, D2Inline)

  private object RunnableCode extends Block:
    override val className: String = "runnable-code"

    override def render(node: dom.HTMLElement): Option[VdomElement] =
      val lang = Option(node.getAttribute("data-lang"))
      val src = Option(node.getAttribute("data-source")).flatMap(decodeURIComponent(className, "data-source"))
      val label = Option(node.getAttribute("data-language-label"))
      for
        l <- lang
        s <- src
      yield RunnableCodeBlock.Component(RunnableCodeBlock.Props(l, s, label))

  private object RunnableGroup extends Block:
    override val className: String = "runnable-group"

    override def render(node: dom.HTMLElement): Option[VdomElement] =
      val tabs = Option(node.getAttribute("data-tabs")).flatMap(decodeTabs).getOrElse(List.empty)
      Option.when(tabs.nonEmpty)(RunnableCodeGroup.Component(RunnableCodeGroup.Props(tabs)))

  private object Mermaid extends Block:
    override val className: String = "mermaid-block"

    override def render(node: dom.HTMLElement): Option[VdomElement] =
      Option(node.getAttribute("data-mermaid-source"))
        .flatMap(decodeURIComponent(className, "data-mermaid-source"))
        .map(src => MermaidBlock.Component(MermaidBlock.Props(src)))

  private object D2Slides extends Block:
    override val className: String = "d2-slides"

    override def render(node: dom.HTMLElement): Option[VdomElement] =
      val slides = (0 until node.children.length).iterator.flatMap { j =>
        val child = node.children.item(j).asInstanceOf[dom.HTMLElement]
        if child.classList.contains("d2-slide") && child.innerHTML.nonEmpty then Some(child.innerHTML)
        else None
      }.toList
      Option.when(slides.nonEmpty) {
        val caption = Option(node.getAttribute("data-caption")).filter(_.nonEmpty)
        D2Slideshow.Component(D2Slideshow.Props(slides, caption))
      }

  // Inline single-diagram D2 block. The component lives at `cortex.D2Diagram`; the block is named
  // differently to avoid a self-reference shadow inside this object.
  private object D2Inline extends Block:
    override val className: String = "d2-diagram"

    override def render(node: dom.HTMLElement): Option[VdomElement] =
      val inner = node.innerHTML
      Option.when(inner.nonEmpty)(D2Diagram.Component(D2Diagram.Props(inner)))

  // ---- decoders ----------------------------------------------------------

  // Wrap `js.Dynamic.global.decodeURIComponent` so a malformed URI on one block doesn't throw past the
  // discover loop. Logs the offending block + attribute to the console for diagnosis. `block` and
  // `attr` are passed in so the warning names the source rather than printing a bare URIError.
  private def decodeURIComponent(block: String, attr: String)(s: String): Option[String] =
    Try(js.Dynamic.global.decodeURIComponent(s).asInstanceOf[String]) match
      case Success(v) => Some(v)
      case Failure(t) =>
        dom.console.warn(s"chapter: skipping $block — malformed $attr: ${t.getMessage}")
        None

  private def decodeTabs(encoded: String): Option[List[RunnableCodeGroup.Tab]] =
    val attempt = Try {
      val decoded = js.Dynamic.global.decodeURIComponent(encoded).asInstanceOf[String]
      val parsed  = js.JSON.parse(decoded).asInstanceOf[js.Array[js.Dynamic]]
      parsed.toList.map { obj =>
        // `runnable` is optional — defaults to true so older payloads / future
        // tabs that omit the field still get a runnable tab.
        val runnable = obj.runnable.asInstanceOf[js.UndefOr[Boolean]].toOption.getOrElse(true)
        RunnableCodeGroup.Tab(
          language = obj.language.asInstanceOf[String],
          languageLabel = obj.languageLabel.asInstanceOf[String],
          source = obj.source.asInstanceOf[String],
          runnable = runnable
        )
      }
    }
    attempt match
      case Success(v) => Some(v)
      case Failure(t) =>
        dom.console.warn(s"chapter: skipping runnable-group — malformed data-tabs: ${t.getMessage}")
        None

  // ---- helpers -----------------------------------------------------------

  // Delegated click handler for in-article hash links. The rehype-autolink-headings pass adds
  // `<a href="#slug">` to each heading, and TOC bullets in markdown also resolve to the same shape.
  // We find the anchor (if any) and hand off to HashScroll, which owns the bypass-router-and-scroll
  // logic shared with CortexToc / MobileToc.
  private val onArticleClick: js.Function1[dom.MouseEvent, Unit] = (e: dom.MouseEvent) =>
    val target = e.target.asInstanceOf[dom.Element]
    val anchor =
      if target != null && target.matches("a[href^='#']") then target
      else if target != null then target.closest("a[href^='#']")
      else null
    if anchor != null && !e.defaultPrevented then
      val href = anchor.getAttribute("href")
      if href != null && href.length > 1 then
        HashScroll.onHashLinkNative(e, href.substring(1))
