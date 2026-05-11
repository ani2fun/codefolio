package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import codefolio.client.util.ReaderState
import codefolio.shared.api.Endpoints.{Book, ChapterRef}
import codefolio.shared.cortex.SidebarForest
import codefolio.shared.cortex.SidebarForest.{Leaf, Node, Section}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js

/**
 * Cortex reader's left-panel **Sidebar Forest**. Tree assembly lives in [[SidebarForest]] (shared,
 * JVM-tested); this file owns the React rendering plus three pieces of editorial UX bolted onto each chapter
 * row:
 *
 *   - **Search** — top-of-rail input that filters the chapter list by title (case-insensitive substring
 *     match). Sections whose chapters all filter out collapse automatically because the tree is rebuilt from
 *     the filtered chapter list. ⌘K / Ctrl+K focuses; Esc clears.
 *   - **Reading-progress rail** — 3px terracotta fill on the left edge of each row, height proportional to
 *     the last-saved scroll position for that chapter ([[ReaderState.progressFor]]).
 *   - **Time to read** — small mono `Xm` stamp on the right of each row, derived from the chapter's word
 *     count the first time it loads ([[ReaderState.minutesFor]]); blank until the chapter has been opened
 *     once.
 *
 * The progress + minutes maps are read once per render via `snapshotFor(chapterSlugs)` so we don't do N
 * storage round-trips inside the recursive node renderer.
 */
object CortexSidebar:

  final case class Props(book: Book, activeChapterSlug: String)

  private case class RowState(progress: Int, minutes: Option[Int], number: Int)

  private val EmptyRow = RowState(0, None, 0)

  /** Two-digit chapter number ("01", "12"); empty for n=0 (chapter not in the book). */
  private def numberLabel(n: Int): String = if n <= 0 then "" else f"$n%02d"

  /** Filter chapters by case-insensitive substring of title. Empty query passes everything. */
  private def filterChapters(chapters: Seq[ChapterRef], query: String): Seq[ChapterRef] =
    val q = query.trim.toLowerCase
    if q.isEmpty then chapters
    else chapters.filter(_.title.toLowerCase.contains(q))

  /**
   * Render `query`-highlighted chapter title — the matched substring becomes a `<mark>` so the hit is visible
   * even when the chapter is mid-list. Falls back to plain text when no query is present.
   */
  private def highlight(title: String, query: String): VdomNode =
    val q = query.trim
    if q.isEmpty then title
    else
      val lower = title.toLowerCase
      val ql    = q.toLowerCase
      val idx   = lower.indexOf(ql)
      if idx < 0 then title
      else
        val before = title.substring(0, idx)
        val hit    = title.substring(idx, idx + q.length)
        val after  = title.substring(idx + q.length)
        <.span(before, <.mark(^.className := "cortex-reader-sidebar__mark", hit), after)

  private def renderNode(
      node: Node,
      activeSlug: String,
      onLinkClick: Callback,
      bookSlug: String,
      query: String,
      rows: Map[String, RowState]
  ): VdomNode = node match
    case Leaf(ch) =>
      val isActive = ch.slug == activeSlug
      val cls =
        if isActive then
          "cortex-reader-sidebar__chapter-link cortex-reader-sidebar__chapter-link--active"
        else "cortex-reader-sidebar__chapter-link"
      val row = rows.getOrElse(ch.slug, EmptyRow)
      <.li(
        ^.key       := s"leaf-${ch.slug}",
        ^.className := "cortex-reader-sidebar__chapter-item",
        <.a(
          ^.href := s"/cortex/$bookSlug/${ch.slug}",
          ^.onClick --> onLinkClick,
          ^.className := cls,
          <.span(
            ^.className   := "cortex-reader-sidebar__progress",
            ^.aria.hidden := true,
            <.span(
              ^.className := "cortex-reader-sidebar__progress-fill",
              ^.style     := js.Dynamic.literal(height = s"${row.progress}%").asInstanceOf[js.Object]
            )
          ),
          <.span(^.className := "cortex-reader-sidebar__num", numberLabel(row.number)),
          <.span(^.className := "cortex-reader-sidebar__name", highlight(ch.title, query)),
          row.minutes
            .map(m => <.span(^.className := "cortex-reader-sidebar__min", s"${m}m"): VdomNode)
            .getOrElse(EmptyVdom)
        )
      )
    case s @ Section(title, depth, children) =>
      val open = s.containsActive(activeSlug) || query.trim.nonEmpty
      <.li(
        ^.key       := s"sec-$depth-$title",
        ^.className := s"cortex-reader-sidebar__section cortex-reader-sidebar__section--depth-$depth",
        <.details(
          ^.className := "cortex-reader-sidebar__section-details",
          ^.open      := open,
          <.summary(
            ^.className := "cortex-reader-sidebar__section-summary",
            // Lucide chevron — rotated 90° via CSS when the parent <details> is open. Replaces
            // the previous CSS ::before Unicode "▸" which rendered too small to read at the
            // 12px summary font-size and didn't match the Lucide vocabulary used elsewhere.
            LucideIcons.ChevronRight(
              LucideIcons.withClass("cortex-reader-sidebar__section-chevron")
            ),
            <.span(^.className := "cortex-reader-sidebar__section-name", title)
          ),
          <.ul(
            ^.className := "cortex-reader-sidebar__section-children",
            children.toTagMod(child => renderNode(child, activeSlug, onLinkClick, bookSlug, query, rows))
          )
        )
      )

  private def renderInner(
      book: Book,
      activeSlug: String,
      onLinkClick: Callback,
      query: String,
      onQuery: String => Callback,
      rows: Map[String, RowState]
  ): VdomNode =
    val filtered = filterChapters(book.chapters, query)
    val forest   = SidebarForest.build(filtered)
    <.div(
      ^.className := "cortex-reader-sidebar__inner",
      <.div(
        <.a(^.href       := "/cortex", ^.className := "cortex-reader-sidebar__back", "← Cortex"),
        <.h2(^.className := "cortex-reader-sidebar__title", book.title),
        if book.description.nonEmpty then
          <.p(^.className := "cortex-reader-sidebar__description", book.description)
        else EmptyVdom,
        <.label(
          ^.className := "cortex-reader-sidebar__search",
          LucideIcons.Search(LucideIcons.withClass("cortex-reader-sidebar__search-icon")),
          <.input(
            ^.tpe                          := "search",
            ^.placeholder                  := "Search chapters",
            ^.aria.label                   := "Search chapters in this book",
            ^.value                        := query,
            ^.className                    := "cortex-reader-sidebar__search-input",
            VdomAttr("data-cortex-search") := "true",
            ^.onChange ==> { (e: ReactEventFromInput) => onQuery(e.target.value) },
            ^.onKeyDown ==> { (e: ReactKeyboardEvent) =>
              if e.key == "Escape" then e.preventDefaultCB >> onQuery("")
              else Callback.empty
            }
          ),
          <.span(^.className := "cortex-reader-sidebar__search-kbd", "⌘K")
        )
      ),
      <.nav(
        if forest.isEmpty then
          <.p(
            ^.className := "cortex-reader-sidebar__empty",
            "No chapters match ",
            <.em(s""""${query.trim}""""),
            "."
          )
        else
          <.ul(
            ^.className := "cortex-reader-sidebar__tree",
            forest.toTagMod(node => renderNode(node, activeSlug, onLinkClick, book.slug, query, rows))
          )
      )
    )

  // Global ⌘K / Ctrl+K focus shortcut. The handler looks up the active search input by data-attribute
  // each time it fires so we don't have to hold a ref. Idempotent across re-mounts: the install marker
  // on `window` short-circuits a second install if the sidebar mounts again (e.g. navigating between
  // books) without ever cleaning up the listener.
  private val ShortcutMarker = "__codefolioCortexSearchShortcut"

  private def installShortcut(): Unit =
    val win = dom.window.asInstanceOf[js.Dynamic]
    if !win.selectDynamic(ShortcutMarker).asInstanceOf[js.UndefOr[Boolean]].getOrElse(false) then
      val handler: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
        val isK = e.key == "k" || e.key == "K"
        if isK && (e.metaKey || e.ctrlKey) then
          val input = dom.document.querySelector("input[data-cortex-search='true']")
          if input != null then
            e.preventDefault()
            input.asInstanceOf[dom.html.Input].focus()
      dom.window.addEventListener("keydown", handler)
      win.updateDynamic(ShortcutMarker)(true)

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false)
      .useState("")
      .useEffectOnMountBy { (_, _, _) =>
        Callback(installShortcut())
      }
      .render { (props, openS, queryS) =>
        val close: Callback = openS.setState(false) >> queryS.setState("")

        // Snapshot localStorage on every render. The sidebar re-renders whenever its props change
        // (chapter navigation in particular), which is the only moment the cached values can have
        // shifted, so the map.apply per render is well within budget. The chapter number is the 1-based
        // index into book.chapters — the codebase has no native num field, so we derive one (matches
        // the design's `01`, `02`, … badges).
        val storage = ReaderState.snapshotFor(props.book.chapters.map(_.slug))
        val rows: Map[String, RowState] = props.book.chapters.iterator.zipWithIndex.map {
          case (ch, idx) =>
            val (p, m) = storage.getOrElse(ch.slug, (0, None))
            ch.slug -> RowState(p, m, idx + 1)
        }.toMap

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
                renderInner(
                  props.book,
                  props.activeChapterSlug,
                  close,
                  queryS.value,
                  q => queryS.setState(q),
                  rows
                )
              )
            )
          else EmptyVdom,
          <.aside(
            ^.className := "cortex-reader-sidebar__desktop",
            renderInner(
              props.book,
              props.activeChapterSlug,
              Callback.empty,
              queryS.value,
              q => queryS.setState(q),
              rows
            )
          )
        )
      }
