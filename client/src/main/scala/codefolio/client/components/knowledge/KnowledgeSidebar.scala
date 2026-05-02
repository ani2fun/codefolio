package codefolio.client.components.knowledge

import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.{Book, ChapterRef}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object KnowledgeSidebar:

  final case class Props(book: Book, activeChapterSlug: String)

  /** Run-length-group consecutive chapters by the optional `group` field.
    *
    * Uses `acc.lastOption` because the fold appends to the tail of `acc`;
    * the previous version pattern-matched the *head* (the very first
    * group), so any later chapter that re-entered the same group started
    * a new section instead of merging — sidebar showed "FOUNDATIONS"
    * twice for `consistency-models` and `replication-and-quorums`.
    */
  private def groupChapters(chapters: Seq[ChapterRef]): List[(Option[String], List[ChapterRef])] =
    chapters.foldLeft(List.empty[(Option[String], List[ChapterRef])]) { (acc, ch) =>
      acc.lastOption match
        case Some((g, items)) if g == ch.group => acc.init :+ (g -> (items :+ ch))
        case _                                  => acc :+ (ch.group -> List(ch))
    }

  private def renderInner(book: Book, activeSlug: String, onLinkClick: Callback): VdomNode =
    <.div(
      ^.className := "flex flex-col gap-6 p-6",
      <.div(
        <.a(
          ^.href := "/knowledge",
          ^.className := "text-xs uppercase tracking-wider text-muted-foreground hover:text-primary",
          "← Knowledge Base"
        ),
        <.h2(
          ^.className := "mt-2 text-lg font-bold leading-snug text-foreground",
          book.title
        ),
        if book.description.nonEmpty then
          <.p(
            ^.className := "mt-1 text-xs text-muted-foreground leading-relaxed",
            book.description
          )
        else EmptyVdom
      ),
      <.nav(
        <.ol(
          ^.className := "space-y-5",
          groupChapters(book.chapters).zipWithIndex.toTagMod { case ((groupName, chapters), gi) =>
            <.li(
              ^.key := s"${groupName.getOrElse("ungrouped")}-$gi",
              groupName.fold(EmptyVdom: VdomNode) { name =>
                <.p(
                  ^.className :=
                    "mb-2 text-xs font-bold uppercase tracking-wider text-foreground",
                  s"${gi + 1}. $name"
                )
              },
              <.ul(
                ^.className := "space-y-1 border-l border-border ml-1",
                chapters.toTagMod { ch =>
                  val isActive = ch.slug == activeSlug
                  val cls =
                    if isActive then
                      "block -ml-px border-l-2 pl-3 py-1.5 text-sm transition-colors " +
                        "border-primary text-primary font-semibold bg-primary/5"
                    else
                      "block -ml-px border-l-2 pl-3 py-1.5 text-sm transition-colors " +
                        "border-transparent text-muted-foreground hover:text-foreground hover:border-border"
                  <.li(
                    ^.key := ch.slug,
                    <.a(
                      ^.href := s"/knowledge/${book.slug}/${ch.slug}",
                      ^.onClick --> onLinkClick,
                      ^.className := cls,
                      ch.title
                    )
                  )
                }
              )
            )
          }
        )
      )
    )

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false) // mobile drawer open
      .render { (props, openS) =>
        val close: Callback = openS.setState(false)

        <.div(
          // Mobile open button
          <.button(
            ^.tpe := "button",
            ^.onClick --> openS.setState(true),
            ^.aria.label := "Open chapters",
            ^.className :=
              "lg:hidden fixed bottom-5 left-5 z-40 rounded-full bg-primary text-primary-foreground " +
                "p-3 shadow-lg",
            LucideIcons.Menu(LucideIcons.withClass("h-5 w-5"))
          ),
          // Mobile drawer
          if openS.value then
            <.div(
              ^.className := "lg:hidden fixed inset-0 z-50 bg-black/40",
              ^.onClick --> close,
              <.aside(
                ^.className :=
                  "absolute left-0 top-0 h-full w-[85%] max-w-sm overflow-y-auto bg-background border-r border-border",
                ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> close,
                  ^.aria.label := "Close chapters",
                  ^.className := "absolute top-4 right-4 text-muted-foreground",
                  LucideIcons.X(LucideIcons.withClass("h-5 w-5"))
                ),
                renderInner(props.book, props.activeChapterSlug, close)
              )
            )
          else EmptyVdom,
          // Desktop sticky sidebar
          <.aside(
            ^.className :=
              "hidden lg:block sticky top-20 self-start max-h-[calc(100vh-5rem)] " +
                "overflow-y-auto border-r border-border",
            renderInner(props.book, props.activeChapterSlug, Callback.empty)
          )
        )
      }
