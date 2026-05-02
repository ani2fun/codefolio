package codefolio.client.components.sections

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.client.components.ui.Section
import codefolio.shared.api.Endpoints.{Book, CortexIndex}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * Home-page preview of the Cortex — first 6 books from the `/api/cortex/index` endpoint, with a
 * "Browse all" CTA below.
 */
object Cortex:

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(Option.empty[Either[String, CortexIndex]])
      .useEffectOnMountBy { (_, state) =>
        Callback {
          ApiClient.getCortexIndex.onComplete {
            case Success(idx) => state.setState(Some(Right(idx))).runNow()
            case Failure(t) =>
              state
                .setState(Some(Left(Option(t.getMessage).getOrElse("Failed to load index"))))
                .runNow()
          }
        }
      }
      .render { (_, state) =>
        Section("cortex", "cortex")(
          <.h2(^.className := "cortex__title", "Cortex"),
          <.p(
            ^.className := "cortex__subtitle",
            "Long-form notes from books, courses, and rabbit holes. Click any topic to start reading."
          ),
          state.value match
            case None =>
              <.p(^.className := "cortex__status", "Loading…")
            case Some(Left(_)) =>
              browseAllCta
            case Some(Right(idx)) if idx.books.isEmpty =>
              <.p(^.className := "cortex__status", "No books published yet — check back soon.")
            case Some(Right(idx)) =>
              val first6 = idx.books.take(6).toList
              <.div(
                <.div(^.className := "cortex__grid", first6.toTagMod(renderCard)),
                if idx.books.size > 6 then
                  <.div(^.className := "cortex__cta-row", browseAllCta)
                else EmptyVdom
              )
        )
      }

  private def renderCard(book: Book): VdomNode =
    val chapterCount   = book.chapters.size
    val plural         = if chapterCount == 1 then "" else "s"
    val readingMinutes = book.estimatedReadingMinutes.fold("")(m => s" · $m min read")
    <.a(
      ^.key       := book.slug,
      ^.href      := s"/cortex/${book.slug}",
      ^.className := "cortex__card group",
      <.div(
        ^.className := "cortex__card-meta",
        LucideIcons.BookOpen(LucideIcons.withClass("cortex__card-meta-icon")),
        <.span(^.className := "cortex__card-meta-text", s"$chapterCount chapter$plural$readingMinutes")
      ),
      <.h3(^.className := "cortex__card-title", book.title),
      <.p(^.className  := "cortex__card-description", book.description),
      <.div(
        ^.className := "cortex__card-footer",
        book.tags.toList.flatten.toTagMod { tag =>
          <.span(^.key := tag, ^.className := "cortex__card-tag", tag)
        },
        <.span(
          ^.className := "cortex__card-cta",
          "Read",
          LucideIcons.ArrowRight(LucideIcons.withClass("cortex__card-cta-icon"))
        )
      )
    )

  private def browseAllCta: VdomNode =
    <.a(
      ^.href      := "/cortex",
      ^.className := "cortex__cta",
      "Browse all",
      LucideIcons.ArrowRight(LucideIcons.withClass("cortex__cta-icon"))
    )
