package codefolio.client.pages

import codefolio.client.api.ApiClient
import codefolio.client.components.cortex.{
  ChapterContent,
  CortexBreadcrumb,
  CortexErrorView,
  CortexPager,
  CortexReaderLayout,
  MobileToc
}
import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.{AsyncFetch, PageTitle}
import codefolio.shared.api.Endpoints.{ChapterPayload, ChapterRef}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * Reads `/api/cortex/{book}/{chapter}`, runs the markdown through the client-side pipeline, and lays out the
 * result with sidebar + breadcrumb + TOC + pager.
 */
object ChapterPage:

  final case class Props(book: String, chapter: String)

  /**
   * Combined fetch + render bundle. We keep both stages so the UI shows loading until both the API call and
   * the markdown renderer have finished.
   */
  final private case class Loaded(payload: ChapterPayload, render: MarkdownRenderer.Result)

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(AsyncFetch.initial[Loaded])
      .useEffectWithDepsBy((props, _) => (props.book, props.chapter)) { (_, state) => deps =>
        val (book, chapter) = deps
        AsyncFetch.run(
          setState = state.setState,
          fetch =
            for
              payload  <- ApiClient.getCortexChapter(book, chapter)
              rendered <- MarkdownRenderer.render(payload.raw)
            yield Loaded(payload, rendered),
          errorPrefix = s"Failed to load $book/$chapter",
          onLoaded = loaded =>
            PageTitle.set(s"${loaded.payload.frontmatter.title} — ${loaded.payload.book.title}")
        )
      }
      .render { (props, state) =>
        state.value.render(
          loaded = renderLoaded,
          loading = <.main(
            ^.className := "container mx-auto py-24 text-center text-muted-foreground",
            s"Loading ${props.book}/${props.chapter}…"
          ),
          errored = msg => <.main(^.className := "container mx-auto py-16 text-center", CortexErrorView(msg))
        )
      }

  private def renderLoaded(loaded: Loaded): VdomNode =
    val payload = loaded.payload
    val toc     = loaded.render.toc

    val prev: Option[ChapterRef] =
      payload.prevSlug.flatMap(s => payload.book.chapters.find(_.slug == s))
    val next: Option[ChapterRef] =
      payload.nextSlug.flatMap(s => payload.book.chapters.find(_.slug == s))

    val content: VdomNode =
      <.div(
        CortexBreadcrumb.Component(
          CortexBreadcrumb.Props(
            bookSlug = payload.book.slug,
            bookTitle = payload.book.title,
            chapterTitle = payload.frontmatter.title
          )
        ),
        <.h1(
          ^.className := "text-3xl md:text-4xl font-bold text-foreground mb-3",
          payload.frontmatter.title
        ),
        payload.frontmatter.summary
          .map(s =>
            <.p(
              ^.className := "text-base text-muted-foreground leading-relaxed mb-8",
              s
            ): VdomNode
          )
          .getOrElse(EmptyVdom),
        MobileToc.Component(MobileToc.Props(toc)),
        ChapterContent.Component(ChapterContent.Props(loaded.render)),
        CortexPager.Component(
          CortexPager.Props(payload.book.slug, prev, next)
        )
      )

    <.div(
      ^.className := "pt-20",
      CortexReaderLayout.Component(
        CortexReaderLayout.Props(
          book = payload.book,
          activeChapterSlug = payload.chapter.slug,
          toc = toc,
          content = content
        )
      )
    )
