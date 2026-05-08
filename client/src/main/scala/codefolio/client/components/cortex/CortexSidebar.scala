package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.Book
import codefolio.shared.cortex.SidebarForest
import codefolio.shared.cortex.SidebarForest.{Leaf, Node, Section}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/**
 * Renders the Cortex reader's left-panel **Sidebar Forest**. Tree assembly lives in [[SidebarForest]] (shared
 * module — testable on the JVM); this file owns the React rendering only.
 */
object CortexSidebar:

  final case class Props(book: Book, activeChapterSlug: String)

  private def renderNode(
      node: Node,
      activeSlug: String,
      onLinkClick: Callback,
      bookSlug: String
  ): VdomNode = node match
    case Leaf(ch) =>
      val isActive = ch.slug == activeSlug
      val cls =
        if isActive then
          "cortex-reader-sidebar__chapter-link cortex-reader-sidebar__chapter-link--active"
        else "cortex-reader-sidebar__chapter-link"
      <.li(
        ^.key       := s"leaf-${ch.slug}",
        ^.className := "cortex-reader-sidebar__chapter-item",
        <.a(
          ^.href := s"/cortex/$bookSlug/${ch.slug}",
          ^.onClick --> onLinkClick,
          ^.className := cls,
          ch.title
        )
      )
    case s @ Section(title, depth, children) =>
      val open = s.containsActive(activeSlug)
      <.li(
        ^.key       := s"sec-$depth-$title",
        ^.className := s"cortex-reader-sidebar__section cortex-reader-sidebar__section--depth-$depth",
        <.details(
          ^.className := "cortex-reader-sidebar__section-details",
          ^.open      := open,
          <.summary(
            ^.className := "cortex-reader-sidebar__section-summary",
            <.span(^.className := "cortex-reader-sidebar__section-name", title)
          ),
          <.ul(
            ^.className := "cortex-reader-sidebar__section-children",
            children.toTagMod(child => renderNode(child, activeSlug, onLinkClick, bookSlug))
          )
        )
      )

  private def renderInner(book: Book, activeSlug: String, onLinkClick: Callback): VdomNode =
    val forest = SidebarForest.build(book.chapters)
    <.div(
      ^.className := "cortex-reader-sidebar__inner",
      <.div(
        <.a(^.href       := "/cortex", ^.className := "cortex-reader-sidebar__back", "← Cortex"),
        <.h2(^.className := "cortex-reader-sidebar__title", book.title),
        if book.description.nonEmpty then
          <.p(^.className := "cortex-reader-sidebar__description", book.description)
        else EmptyVdom
      ),
      <.nav(
        <.ul(
          ^.className := "cortex-reader-sidebar__tree",
          forest.toTagMod(node => renderNode(node, activeSlug, onLinkClick, book.slug))
        )
      )
    )

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false)
      .render { (props, openS) =>
        val close: Callback = openS.setState(false)

        <.div(
          <.button(
            ^.tpe := "button",
            ^.onClick --> openS.setState(true),
            ^.aria.label := "Open chapters",
            ^.className  := "cortex-reader-sidebar__open-button",
            LucideIcons.Menu(LucideIcons.withClass("cortex-reader-sidebar__open-icon"))
          ),
          if openS.value then
            <.div(
              ^.className := "cortex-reader-sidebar__drawer-overlay",
              ^.onClick --> close,
              <.aside(
                ^.className := "cortex-reader-sidebar__drawer-panel",
                ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> close,
                  ^.aria.label := "Close chapters",
                  ^.className  := "cortex-reader-sidebar__close-button",
                  LucideIcons.X(LucideIcons.withClass("cortex-reader-sidebar__close-icon"))
                ),
                renderInner(props.book, props.activeChapterSlug, close)
              )
            )
          else EmptyVdom,
          <.aside(
            ^.className := "cortex-reader-sidebar__desktop",
            renderInner(props.book, props.activeChapterSlug, Callback.empty)
          )
        )
      }
