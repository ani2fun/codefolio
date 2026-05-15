package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.HashScroll
import codefolio.shared.cortex.Block
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/**
 * Owns the post-render mounting half of the Chapter pipeline: drops the rendered HTML into an `<article>` via
 * `dangerouslySetInnerHTML`, asks [[BlockDiscovery]] to find every Cortex Block placeholder, and React-
 * portal-mounts a Scala.js component into each one.
 *
 * Discovery + structural validation live in `BlockDiscovery` (DOM walk + JS shims) and `shared.cortex.Blocks`
 * (pure decoders, JVM-tested). This module owns only the React-side concerns: hook lifecycle, root teardown
 * across chapter changes, click delegation for in-article anchors, and the total `Block => VdomElement`
 * dispatch.
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
            BlockDiscovery.discover(article).blocks.foreach { case (node, block) =>
              val _ = newRoots.push(mount(node, render(block)))
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

  // Total `Block => VdomElement` dispatch. Adding a new Block variant breaks the match
  // exhaustively at compile time — the missing-case error names exactly what's missing.
  private def render(block: Block): VdomElement = block match
    case Block.RunnableCode(language, source, languageLabel) =>
      RunnableCodeBlock.Component(RunnableCodeBlock.Props(language, source, languageLabel))
    case Block.RunnableGroup(tabs) =>
      RunnableCodeGroup.Component(RunnableCodeGroup.Props(tabs.map(toGroupTab)))
    case Block.Mermaid(source) =>
      MermaidBlock.Component(MermaidBlock.Props(source))
    case Block.D2Slides(slides, caption) =>
      D2Slideshow.Component(D2Slideshow.Props(slides, caption))
    case Block.D2Inline(svgHtml) =>
      D2Diagram.Component(D2Diagram.Props(svgHtml))
    case Block.D3Widget(widget, payload) =>
      D3WidgetBlock.Component(D3WidgetBlock.Props(widget, payload))
    case Block.TracedCode(language, source) =>
      TracedCodeBlock.Component(TracedCodeBlock.Props(language, source))
    case Block.LikeC4(src, height, title) =>
      LikeC4Block.Component(LikeC4Block.Props(src, height, title))

  private def toGroupTab(t: Block.Tab): RunnableCodeGroup.Tab =
    RunnableCodeGroup.Tab(
      language = t.language,
      languageLabel = t.languageLabel,
      source = t.source,
      runnable = t.runnable
    )

  // Delegated click handler for in-article hash links. The rehype-autolink-headings pass adds
  // `<a href="#slug">` to each heading, and TOC bullets in markdown also resolve to the same shape.
  // We find the anchor (if any) and hand off to HashScroll, which owns the bypass-router-and-scroll
  // logic shared with CortexToc.
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
