package codefolio.client.pages

import codefolio.client.api.ApiClient
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * `/cortex/<book>` → fetch the index, look up the book, redirect via `history.replaceState` to its first
 * chapter. Falls back to a clear error message if the book is unknown.
 */
object BookRedirectPage:

  val Component =
    ScalaFnComponent
      .withHooks[String]
      .useState(Option.empty[String]) // error
      .useEffectOnMountBy { (book, errS) =>
        Callback {
          ApiClient.getCortexIndex.onComplete {
            case Success(idx) =>
              idx.books.find(_.slug == book) match
                case Some(b) if b.chapters.nonEmpty =>
                  // Use replaceState so the back button doesn't bounce here.
                  val target = s"/cortex/$book/${b.chapters.head.slug}"
                  dom.window.location.replace(target)
                case Some(_) =>
                  errS
                    .setState(Some(s"Book '$book' has no chapters yet."))
                    .runNow()
                case None =>
                  errS.setState(Some(s"Book '$book' not found.")).runNow()
            case Failure(t) =>
              errS
                .setState(Some(Option(t.getMessage).getOrElse("Failed to load book index")))
                .runNow()
          }
        }
      }
      .render { (book, errS) =>
        <.main(
          ^.className := "container mx-auto py-16 text-center",
          errS.value match
            case None =>
              <.p(
                ^.className := "text-muted-foreground",
                s"Loading $book…"
              )
            case Some(msg) =>
              <.div(
                <.p(^.className := "text-destructive font-semibold", msg),
                <.p(
                  ^.className := "mt-4 text-sm",
                  <.a(
                    ^.href      := "/cortex",
                    ^.className := "text-primary hover:underline",
                    "← Back to Cortex"
                  )
                )
              )
        )
      }
