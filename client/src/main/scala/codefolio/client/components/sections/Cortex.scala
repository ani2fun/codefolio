package codefolio.client.components.sections

import codefolio.client.api.ApiClient
import codefolio.client.components.cortex.BookGrid
import codefolio.client.components.icons.LucideIcons
import codefolio.client.components.ui.Section
import codefolio.client.util.AsyncFetch
import codefolio.shared.api.Endpoints.CortexIndex
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 * TO DO: REMOVE THIS COMPONENT and IT's RELEVANT COMMENTED CSS CLASSES.
 *
 * Home-page preview of the Cortex — first 6 books from the `/api/cortex/index` endpoint, with a "Browse all"
 * CTA below. Card markup itself lives in `BookGrid`; this component owns the section heading, the limit, and
 * the CTA.
 */
object Cortex:

  private val PreviewLimit = 6

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(AsyncFetch.initial[CortexIndex])
      .useEffectOnMountBy { (_, state) =>
        AsyncFetch.run(
          setState = state.setState,
          fetch = ApiClient.getCortexIndex,
          errorPrefix = "Failed to load index"
        )
      }
      .render { (_, state) =>
        Section("cortex", "cortex")(
          <.div(
            ^.className := "cortex__inner",
            <.div(^.className := "cortex__eyebrow", "CORTEX · LONG-FORM NOTES"),
            <.div(
              ^.className := "cortex__heading-row",
              <.h2(
                ^.className := "cortex__title",
                "Writing",
                <.br,
                "I'm working through."
              ),
              state.value.render(
                loaded = idx =>
                  idx.books.headOption match
                    case Some(book) =>
                      <.div(
                        ^.className := "cortex__pulse-pill",
                        <.span(^.className := "cortex__pulse-dot", ^.aria.hidden := true),
                        <.span(
                          ^.className := "cortex__pulse-text",
                          s"Currently writing: ${book.title}"
                        )
                      )
                    case None => EmptyVdom
                ,
                errored = _ => EmptyVdom
              )
            ),
            state.value.render(
              loaded = idx =>
                <.div(
                  BookGrid.Component(BookGrid.Props(idx.books.toList, limit = Some(PreviewLimit))),
                  if idx.books.size > PreviewLimit then
                    <.div(^.className := "cortex__cta-row", browseAllCta)
                  else EmptyVdom
                ),
              errored = _ => browseAllCta
            )
          )
        )
      }

  private def browseAllCta: VdomNode =
    <.a(
      ^.href      := "/cortex",
      ^.className := "cortex__cta",
      "Browse all",
      LucideIcons.ArrowRight(LucideIcons.withClass("cortex__cta-icon"))
    )
