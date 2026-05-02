package codefolio.client.components.knowledge

import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object KnowledgeBreadcrumb:

  final case class Props(bookSlug: String, bookTitle: String, chapterTitle: String)

  val Component = ScalaFnComponent[Props] { props =>
    <.nav(
      ^.aria.label := "Breadcrumb",
      ^.className := "text-xs text-muted-foreground mb-6",
      <.ol(
        ^.className := "flex flex-wrap items-center gap-1.5",
        <.li(<.a(^.className := "hover:text-primary", ^.href := "/", "Home")),
        LucideIcons.ChevronRight(LucideIcons.withClass("h-3 w-3")),
        <.li(<.a(^.className := "hover:text-primary", ^.href := "/knowledge", "Knowledge Base")),
        LucideIcons.ChevronRight(LucideIcons.withClass("h-3 w-3")),
        <.li(
          <.a(
            ^.className := "hover:text-primary",
            ^.href := s"/knowledge/${props.bookSlug}",
            props.bookTitle
          )
        ),
        LucideIcons.ChevronRight(LucideIcons.withClass("h-3 w-3")),
        <.li(^.className := "text-foreground font-medium truncate", props.chapterTitle)
      )
    )
  }
