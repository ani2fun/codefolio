package codefolio.client.components.knowledge

import codefolio.client.markdown.MarkdownRenderer
import codefolio.shared.api.Endpoints.Book
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/** 3-column reader layout: sidebar (chapters) | main content | TOC.
  * Mobile collapses to a single column with the sidebar in a drawer and
  * the TOC in a collapsible panel above the content.
  */
object KnowledgeReaderLayout:

  final case class Props(
      book: Book,
      activeChapterSlug: String,
      toc: List[MarkdownRenderer.TocEntry],
      content: VdomNode
  )

  val Component = ScalaFnComponent[Props] { props =>
    <.div(
      ^.className :=
        "grid grid-cols-1 lg:grid-cols-[280px_minmax(0,1fr)] " +
          "xl:grid-cols-[280px_minmax(0,1fr)_240px] gap-0 max-w-[1400px] mx-auto",
      KnowledgeSidebar.Component(
        KnowledgeSidebar.Props(props.book, props.activeChapterSlug)
      ),
      <.main(
        ^.className := "px-5 md:px-10 py-10 min-w-0",
        props.content
      ),
      KnowledgeToc.Component(KnowledgeToc.Props(props.toc))
    )
  }
