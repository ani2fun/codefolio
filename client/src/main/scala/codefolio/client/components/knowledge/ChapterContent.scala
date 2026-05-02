package codefolio.client.components.knowledge

import codefolio.client.markdown.MarkdownRenderer
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/** Renders a fully-processed chapter: drops the rendered HTML into an
  * `<article>` via `dangerouslySetInnerHTML`, then walks the article DOM
  * after mount and React-portal-mounts Scala.js components into the
  * placeholder divs the markdown pipeline emitted (`.runnable-code`,
  * `.runnable-group`, `.mermaid-block`, `.d2-diagram`).
  *
  * Roots are tracked across re-renders and unmounted on chapter change so
  * we don't leak component trees.
  *
  * Doesn't render the TOC itself — `props.result.toc` is exposed for the
  * sidebar/heading-tracker to consume separately.
  */
object ChapterContent:

  final case class Props(result: MarkdownRenderer.Result)

  // We can't depend on japgolly's createRoot wrapper here (its API is
  // wrapped); call ReactDOMClient directly via a tiny facade.
  @js.native
  @js.annotation.JSImport("react-dom/client", "createRoot")
  private def createRoot(container: dom.Node): RootHandle = js.native

  @js.native
  private trait RootHandle extends js.Object:
    def render(node: js.Any): Unit = js.native
    def unmount(): Unit            = js.native

  /** Mount a scalajs-react `Unmounted` into a DOM container by routing it
    * through `ReactDOMClient.createRoot`. Returns the root handle so the
    * caller can unmount on cleanup.
    */
  private def mount(container: dom.Node, vdom: VdomElement): RootHandle =
    val root = createRoot(container)
    root.render(vdom.rawElement.asInstanceOf[js.Any])
    root

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useRefToVdom[dom.html.Element]
      // Track the React roots we mount into placeholder divs. Stored in a
      // mutable ref to survive re-renders without triggering them.
      .useRefBy(_ => js.Array[RootHandle]())
      // Re-decorate whenever the rendered HTML changes (i.e. new chapter).
      // The article's innerHTML is set via dangerouslySetInnerHTML; React
      // re-runs that synchronously when `props.result.html` changes, so by
      // the time this effect fires the DOM is in sync.
      .useEffectWithDepsBy((props, _, _) => props.result.html) {
        (_, articleRef, rootsRef) => _ =>
          val tearDown: Callback = Callback {
            val prev = rootsRef.value
            for i <- 0 until prev.length do Try(prev(i).unmount())
            rootsRef.value = js.Array()
          }

          // articleRef.foreach's lambda is `Element => Unit`; the body runs
          // imperatively when the resulting Callback fires. Wrapping the
          // body in `Callback { … }` would silently drop it (a `-Wvalue-
          // discard` warning at compile time, no mount at runtime — the
          // bug that left mermaid/runnable/d2 placeholders empty).
          val mountAll: Callback = articleRef.foreach { article =>
            val newRoots = js.Array[RootHandle]()

            // ---- Runnable code blocks --------------------------------
            val runnable = article.querySelectorAll("div.runnable-code")
            for i <- 0 until runnable.length do
              val node  = runnable.item(i).asInstanceOf[dom.HTMLElement]
              val lang  = node.getAttribute("data-lang")
              val src   = Option(node.getAttribute("data-source")).map(decodeURIComponent)
              val label = Option(node.getAttribute("data-language-label"))
              if lang != null && src.isDefined then
                val vdom = RunnableCodeBlock.Component(
                  RunnableCodeBlock.Props(lang, src.get, label)
                )
                val _ = newRoots.push(mount(node, vdom))

            // ---- Runnable groups -------------------------------------
            val groups = article.querySelectorAll("div.runnable-group")
            for i <- 0 until groups.length do
              val node    = groups.item(i).asInstanceOf[dom.HTMLElement]
              val encoded = Option(node.getAttribute("data-tabs"))
              val tabs = encoded.flatMap(decodeTabs).getOrElse(List.empty)
              if tabs.nonEmpty then
                val vdom = RunnableCodeGroup.Component(RunnableCodeGroup.Props(tabs))
                val _ = newRoots.push(mount(node, vdom))

            // ---- Mermaid blocks --------------------------------------
            val mermaids = article.querySelectorAll("div.mermaid-block")
            for i <- 0 until mermaids.length do
              val node    = mermaids.item(i).asInstanceOf[dom.HTMLElement]
              val encoded = Option(node.getAttribute("data-mermaid-source"))
              encoded.map(decodeURIComponent).foreach { source =>
                val vdom = MermaidBlock.Component(MermaidBlock.Props(source))
                val _ = newRoots.push(mount(node, vdom))
              }

            // ---- D2 diagrams -----------------------------------------
            val d2s = article.querySelectorAll("div.d2-diagram")
            for i <- 0 until d2s.length do
              val node      = d2s.item(i).asInstanceOf[dom.HTMLElement]
              val innerHtml = node.innerHTML
              if innerHtml.nonEmpty then
                val vdom = D2Diagram.Component(D2Diagram.Props(innerHtml))
                val _ = newRoots.push(mount(node, vdom))

            rootsRef.value = newRoots
          }

          tearDown >> mountAll
      }
      // Note: we don't add an unmount-only cleanup hook for the React roots.
      // useEffectWithDepsBy(html) already tears down the previous roots on
      // every chapter change, which covers the common case. If the parent
      // unmounts the whole ChapterContent the placeholder DOM nodes go too,
      // and the orphaned React roots are harmless GC garbage.
      .render { (props, articleRef, _) =>
        <.article.withRef(articleRef)(
          ^.className :=
            "prose prose-blue dark:prose-invert max-w-none " +
              "prose-headings:scroll-mt-28 prose-pre:p-0 prose-pre:bg-transparent " +
              "prose-pre:border-0 prose-img:my-0",
          ^.dangerouslySetInnerHtml := props.result.html
        )
      }

  // ---- helpers ------------------------------------------------------------

  private def decodeURIComponent(s: String): String =
    js.Dynamic.global.decodeURIComponent(s).asInstanceOf[String]

  private def decodeTabs(encoded: String): Option[List[RunnableCodeGroup.Tab]] =
    Try {
      val decoded = decodeURIComponent(encoded)
      val parsed = js.JSON.parse(decoded).asInstanceOf[js.Array[js.Dynamic]]
      parsed.toList.map { obj =>
        RunnableCodeGroup.Tab(
          language = obj.language.asInstanceOf[String],
          languageLabel = obj.languageLabel.asInstanceOf[String],
          source = obj.source.asInstanceOf[String]
        )
      }
    }.toOption
