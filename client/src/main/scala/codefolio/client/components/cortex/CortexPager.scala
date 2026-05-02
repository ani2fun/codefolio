package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.ChapterRef
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object CortexPager:

  final case class Props(bookSlug: String, prev: Option[ChapterRef], next: Option[ChapterRef])

  val Component = ScalaFnComponent[Props] { props =>
    if props.prev.isEmpty && props.next.isEmpty then EmptyVdom
    else
      <.div(
        ^.className := "cortex-reader-pager",
        props.prev match
          case Some(p) =>
            <.a(
              ^.href      := s"/cortex/${props.bookSlug}/${p.slug}",
              ^.className := "cortex-reader-pager__card group",
              <.span(
                ^.className := "cortex-reader-pager__label",
                LucideIcons.ArrowLeft(LucideIcons.withClass("cortex-reader-pager__label-icon")),
                "Previous"
              ),
              <.span(^.className := "cortex-reader-pager__title", p.title)
            )
          case None => <.span(),
        props.next match
          case Some(n) =>
            <.a(
              ^.href      := s"/cortex/${props.bookSlug}/${n.slug}",
              ^.className := "cortex-reader-pager__card cortex-reader-pager__card--next group",
              <.span(
                ^.className := "cortex-reader-pager__label cortex-reader-pager__label--next",
                "Next ",
                LucideIcons.ArrowRight(LucideIcons.withClass("cortex-reader-pager__label-icon"))
              ),
              <.span(^.className := "cortex-reader-pager__title", n.title)
            )
          case None => EmptyVdom
      )
  }
