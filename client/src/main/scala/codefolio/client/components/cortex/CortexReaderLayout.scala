package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import codefolio.shared.api.Endpoints.Book
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/**
 * 3-column reader layout: sidebar (chapters) | main content | TOC. Mobile collapses to a single column with
 * the sidebar in a drawer and the TOC in a collapsible panel above the content.
 */
object CortexReaderLayout:

  final case class Props(
      book: Book,
      activeChapterSlug: String,
      toc: List[MarkdownRenderer.TocEntry],
      content: VdomNode
  )

  val Component = ScalaFnComponent[Props] { props =>
    <.div(
      ^.className := "cortex-reader-layout",
      CortexSidebar.Component(CortexSidebar.Props(props.book, props.activeChapterSlug)),
      <.main(^.className := "cortex-reader-layout__main", props.content),
      CortexToc.Component(CortexToc.Props(props.toc)),
      ScrollToTop.Component()
    )
  }
