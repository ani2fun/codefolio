package codefolio.client.components.knowledge

import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.ChapterRef
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object KnowledgePager:

  final case class Props(bookSlug: String, prev: Option[ChapterRef], next: Option[ChapterRef])

  val Component = ScalaFnComponent[Props] { props =>
    if props.prev.isEmpty && props.next.isEmpty then EmptyVdom
    else
      <.div(
        ^.className := "mt-12 pt-6 border-t border-border grid grid-cols-1 sm:grid-cols-2 gap-4",
        props.prev match
          case Some(p) =>
            <.a(
              ^.href := s"/knowledge/${props.bookSlug}/${p.slug}",
              ^.className :=
                "group rounded-lg border border-border p-4 hover:border-primary transition-colors",
              <.span(
                ^.className := "flex items-center gap-1 text-xs text-muted-foreground mb-1",
                LucideIcons.ArrowLeft(LucideIcons.withClass("h-3 w-3")),
                "Previous"
              ),
              <.span(
                ^.className := "block font-semibold text-foreground group-hover:text-primary",
                p.title
              )
            )
          case None => <.span()
        ,
        props.next match
          case Some(n) =>
            <.a(
              ^.href := s"/knowledge/${props.bookSlug}/${n.slug}",
              ^.className :=
                "group rounded-lg border border-border p-4 hover:border-primary " +
                  "transition-colors text-right sm:col-start-2",
              <.span(
                ^.className := "flex items-center justify-end gap-1 text-xs text-muted-foreground mb-1",
                "Next ",
                LucideIcons.ArrowRight(LucideIcons.withClass("h-3 w-3"))
              ),
              <.span(
                ^.className := "block font-semibold text-foreground group-hover:text-primary",
                n.title
              )
            )
          case None => EmptyVdom
      )
  }
