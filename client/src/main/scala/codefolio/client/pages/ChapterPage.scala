package codefolio.client.pages

import codefolio.client.api.ApiClient
import codefolio.client.components.knowledge.{
  ChapterContent,
  KnowledgeBreadcrumb,
  KnowledgePager,
  KnowledgeReaderLayout,
  MobileToc
}
import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.PageTitle
import codefolio.shared.api.Endpoints.{ChapterPayload, ChapterRef}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/** Reads `/api/knowledge/{book}/{chapter}`, runs the markdown through the
  * client-side pipeline, and lays out the result with sidebar + breadcrumb
  * + TOC + pager.
  */
object ChapterPage:

  final case class Props(book: String, chapter: String)

  /** Combined fetch + render state. We keep both stages so the UI can show
    * a loading spinner while the API is in flight or the renderer is busy.
    */
  private final case class Loaded(payload: ChapterPayload, render: MarkdownRenderer.Result)

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(Option.empty[Either[String, Loaded]])
      .useEffectWithDepsBy((props, _) => (props.book, props.chapter)) { (_, state) => deps =>
        val (book, chapter) = deps
        Callback {
          state.setState(None).runNow()
          val fut = for
            payload <- ApiClient.getKnowledgeChapter(book, chapter)
            rendered <- MarkdownRenderer.render(payload.raw)
          yield Loaded(payload, rendered)
          fut.onComplete {
            case Success(loaded) =>
              state.setState(Some(Right(loaded))).runNow()
              PageTitle
                .set(s"${loaded.payload.frontmatter.title} — ${loaded.payload.book.title}")
                .runNow()
            case Failure(t) =>
              state
                .setState(Some(Left(Option(t.getMessage).getOrElse("Failed to load chapter"))))
                .runNow()
          }
        }
      }
      .render { (props, state) =>
        state.value match
          case None =>
            <.main(
              ^.className := "container mx-auto py-24 text-center text-muted-foreground",
              s"Loading ${props.book}/${props.chapter}…"
            )
          case Some(Left(msg)) =>
            <.main(
              ^.className := "container mx-auto py-16 text-center",
              <.p(^.className := "text-destructive font-semibold", msg),
              <.p(
                ^.className := "mt-4 text-sm",
                <.a(
                  ^.href := "/knowledge",
                  ^.className := "text-primary hover:underline",
                  "← Back to Knowledge Base"
                )
              )
            )
          case Some(Right(loaded)) =>
            renderLoaded(loaded)
      }

  private def renderLoaded(loaded: Loaded): VdomNode =
    val payload = loaded.payload
    val toc = loaded.render.toc

    val prev: Option[ChapterRef] =
      payload.prevSlug.flatMap(s => payload.book.chapters.find(_.slug == s))
    val next: Option[ChapterRef] =
      payload.nextSlug.flatMap(s => payload.book.chapters.find(_.slug == s))

    val content: VdomNode =
      <.div(
        KnowledgeBreadcrumb.Component(
          KnowledgeBreadcrumb.Props(
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
        KnowledgePager.Component(
          KnowledgePager.Props(payload.book.slug, prev, next)
        )
      )

    <.div(
      ^.className := "pt-20",
      KnowledgeReaderLayout.Component(
        KnowledgeReaderLayout.Props(
          book = payload.book,
          activeChapterSlug = payload.chapter.slug,
          toc = toc,
          content = content
        )
      )
    )
