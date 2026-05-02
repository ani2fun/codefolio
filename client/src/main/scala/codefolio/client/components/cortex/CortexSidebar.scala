package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.{Book, ChapterRef}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object CortexSidebar:

  final case class Props(book: Book, activeChapterSlug: String)

  /**
   * Sidebar tree node. Sections nest other nodes; leaves wrap a chapter. The forest at the top level can
   * interleave top-level chapters and top-level sections (`groupPath` may be empty for some chapters and
   * non-empty for others).
   */
  sealed private trait Node:
    def containsActive(activeSlug: String): Boolean

  final private case class Section(title: String, depth: Int, children: List[Node]) extends Node:
    def containsActive(activeSlug: String): Boolean = children.exists(_.containsActive(activeSlug))

  final private case class Leaf(chapter: ChapterRef) extends Node:
    def containsActive(activeSlug: String): Boolean = chapter.slug == activeSlug

  /**
   * Group chapters into a forest by their `groupPath`. Section ordering is determined by first-seen-wins
   * within each level — preserving the server-side traversal order, which is numeric-prefix-sorted.
   */
  private def buildForest(chapters: Seq[ChapterRef]): List[Node] =
    val root = scala.collection.mutable.LinkedHashMap.empty[String, Either[Leaf, SectionBuilder]]
    chapters.foreach { ch =>
      val path = ch.groupPath.getOrElse(Seq.empty)
      if path.isEmpty then
        // Leaves at the root use their slug as the LinkedHashMap key (unique
        // per book by construction). The Leaf/Section sum makes both kinds
        // co-exist in a single ordered map.
        root.update(s"leaf:${ch.slug}", Left(Leaf(ch)))
      else
        val first = path.head
        val key   = s"sec:$first"
        val sec = root.get(key) match
          case Some(Right(sb)) => sb
          case _ =>
            val sb = SectionBuilder(first, depth = 0)
            root.update(key, Right(sb))
            sb
        sec.insert(path.tail.toList, ch)
    }
    root.values.iterator.map {
      case Left(leaf) => leaf
      case Right(sb)  => sb.build
    }.toList

  /** Mutable builder that mirrors the LinkedHashMap pattern recursively. */
  final private class SectionBuilder(title: String, depth: Int):

    private val children =
      scala.collection.mutable.LinkedHashMap.empty[String, Either[Leaf, SectionBuilder]]

    def insert(remainingPath: List[String], ch: ChapterRef): Unit =
      remainingPath match
        case Nil =>
          children.update(s"leaf:${ch.slug}", Left(Leaf(ch)))
        case head :: rest =>
          val key = s"sec:$head"
          val sub = children.get(key) match
            case Some(Right(sb)) => sb
            case _ =>
              val sb = SectionBuilder(head, depth + 1)
              children.update(key, Right(sb))
              sb
          sub.insert(rest, ch)

    def build: Section =
      Section(
        title,
        depth,
        children.values.iterator.map {
          case Left(leaf) => leaf
          case Right(sb)  => sb.build
        }.toList
      )

  // ---- Rendering ---------------------------------------------------------

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
    val forest = buildForest(book.chapters)
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
