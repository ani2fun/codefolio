package codefolio.client.components.sections

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.{Book, KnowledgeIndex}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/** Home-page preview of the knowledge base — first 6 books from the
  * `/api/knowledge/index` endpoint, with a "Browse all" CTA below.
  */
object KnowledgeBase:

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(Option.empty[Either[String, KnowledgeIndex]])
      .useEffectOnMountBy { (_, state) =>
        Callback {
          ApiClient.getKnowledgeIndex.onComplete {
            case Success(idx) => state.setState(Some(Right(idx))).runNow()
            case Failure(t) =>
              state
                .setState(Some(Left(Option(t.getMessage).getOrElse("Failed to load index"))))
                .runNow()
          }
        }
      }
      .render { (_, state) =>
        <.section(
          ^.id := "knowledge",
          ^.className := "px-4 md:px-8 pt-28 md:pt-32 pb-12 rounded-lg shadow-md font-sans scroll-mt-24",
          <.h2(
            ^.className := "text-3xl md:text-5xl font-bold text-center text-blue-700 mb-3",
            "Knowledge Base"
          ),
          <.p(
            ^.className :=
              "text-center text-gray-600 dark:text-gray-400 mb-10 text-sm md:text-base max-w-2xl mx-auto",
            "Long-form notes from books, courses, and rabbit holes. Click any topic to start reading."
          ),
          state.value match
            case None =>
              <.p(^.className := "text-center text-muted-foreground", "Loading…")
            case Some(Left(_)) =>
              // Quietly fall back to the CTA — the home page shouldn't break
              // because the knowledge endpoint had a transient failure.
              browseAllCta
            case Some(Right(idx)) if idx.books.isEmpty =>
              <.p(
                ^.className := "text-center text-muted-foreground",
                "No books published yet — check back soon."
              )
            case Some(Right(idx)) =>
              val first6 = idx.books.take(6).toList
              <.div(
                <.div(
                  ^.className :=
                    "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 max-w-6xl mx-auto",
                  first6.toTagMod(renderCard)
                ),
                if idx.books.size > 6 then
                  <.div(^.className := "flex justify-center mt-8", browseAllCta)
                else EmptyVdom
              )
        )
      }

  private def renderCard(book: Book): VdomNode =
    val chapterCount = book.chapters.size
    val plural = if chapterCount == 1 then "" else "s"
    val readingMinutes = book.estimatedReadingMinutes.fold("")(m => s" · $m min read")
    <.a(
      ^.key := book.slug,
      ^.href := s"/knowledge/${book.slug}",
      ^.className :=
        "group flex flex-col rounded-lg border border-blue-500 dark:border-gray-50 p-5 " +
          "hover:bg-blue-50 dark:hover:bg-gray-900 transition-colors",
      <.div(
        ^.className := "flex items-center gap-2 mb-2",
        LucideIcons.BookOpen(LucideIcons.withClass("h-4 w-4 text-blue-600 dark:text-blue-400")),
        <.span(
          ^.className :=
            "text-[11px] uppercase tracking-wider text-gray-600 dark:text-gray-400 font-semibold",
          s"$chapterCount chapter$plural$readingMinutes"
        )
      ),
      <.h3(
        ^.className :=
          "text-lg font-bold text-blue-900 dark:text-gray-50 group-hover:text-blue-600 " +
            "dark:group-hover:text-blue-400 transition-colors mb-2",
        book.title
      ),
      <.p(
        ^.className := "text-sm text-gray-700 dark:text-gray-300 leading-relaxed mb-4 flex-1",
        book.description
      ),
      <.div(
        ^.className := "flex flex-wrap items-center gap-1.5 mt-auto",
        book.tags.toList.flatten.toTagMod { tag =>
          <.span(
            ^.key := tag,
            ^.className :=
              "text-[10px] md:text-xs px-1.5 py-0.5 rounded text-blue-900 bg-blue-200 " +
                "dark:text-gray-50 dark:bg-blue-600",
            tag
          )
        },
        <.span(
          ^.className :=
            "ml-auto inline-flex items-center text-xs text-blue-600 dark:text-blue-400 font-semibold",
          "Read",
          LucideIcons.ArrowRight(
            LucideIcons.withClass(
              "h-3 w-3 ml-1 group-hover:translate-x-0.5 transition-transform"
            )
          )
        )
      )
    )

  private def browseAllCta: VdomNode =
    <.a(
      ^.href := "/knowledge",
      ^.className :=
        "inline-flex items-center gap-2 px-4 py-2 rounded-md " +
          "border border-blue-500 dark:border-blue-400 text-blue-600 dark:text-blue-400 " +
          "hover:bg-blue-50 dark:hover:bg-blue-950 font-semibold text-sm transition-colors",
      "Browse all",
      LucideIcons.ArrowRight(LucideIcons.withClass("h-4 w-4"))
    )
