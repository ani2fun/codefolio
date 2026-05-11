package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.HashScroll
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/**
 * Right-rail "On This Page" TOC. Mirrors the design system's `cx-toc` pattern:
 *
 *   - **Single aside** that's narrow at rest and expands its column width on hover/focus, instead of a
 *     separate detached panel sliding in over the prose. Holds every TOC entry; each row is a tick + label
 *     indented per heading depth.
 *   - **Bar ticks** — 4px-tall, 14px-wide. Active row's tick widens to 22px and tints terracotta.
 *   - **Labels fade in** when the aside is hovered/focused; at rest only the ticks are visible.
 *
 * The aside-expansion and parent-grid-widen are both driven by CSS `:hover` / `:focus-within` (with a sibling
 * `:has()` rule on `.cortex-reader-layout`), so this component owns no UI-state hook for the open/closed flag
 * — only the IntersectionObserver-driven active-section state.
 *
 * Hidden below xl; mobile uses [[MobileToc]].
 */
object CortexToc:

  final case class Props(toc: List[MarkdownRenderer.TocEntry])

  // The minimum heading depth in this chapter is the rail's "level 1" — typically h2, but some imported
  // books start at h3, so we normalise. Levels above the visible cap (3) clamp down so we don't generate
  // unbounded class names.
  private val MaxLevel = 3

  private def levelOf(entryDepth: Int, base: Int): Int =
    val raw = entryDepth - base + 1
    math.max(1, math.min(MaxLevel, raw))

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(Option.empty[String])
      .useEffectWithDepsBy((props, _) => props.toc.map(_.slug)) { (props, activeS) => _ =>
        if props.toc.isEmpty then Callback.empty
        else
          Callback {
            val headings = props.toc
              .flatMap(item => Option(dom.document.getElementById(item.slug)))
              .toJSArray

            if headings.length == 0 then ()
            else
              val cb: js.Function2[js.Array[dom.IntersectionObserverEntry], dom.IntersectionObserver, Unit] =
                (entries, _) =>
                  val visible = entries
                    .filter(_.isIntersecting)
                    .toList
                    .sortBy(_.boundingClientRect.top.toDouble)
                  visible.headOption.foreach { entry =>
                    val slug = entry.target.asInstanceOf[dom.Element].id
                    if activeS.value.contains(slug) then ()
                    else activeS.setState(Some(slug)).runNow()
                  }

              val opts = (new js.Object).asInstanceOf[dom.IntersectionObserverInit]
              opts.rootMargin = "-20% 0px -70% 0px"
              opts.threshold = js.Array[Double](0.0, 1.0)
              val observer = new dom.IntersectionObserver(cb, opts)
              for i <- 0 until headings.length do observer.observe(headings(i))
            ()
          }
      }
      .render { (props, activeS) =>
        if props.toc.isEmpty then EmptyVdom
        else
          val baseDepth = props.toc.map(_.depth).min

          // The visually-active row is the entry that's currently in the viewport; an additional
          // ancestor walk lets the rail also light the parent section's tick when the reader is inside
          // a sub-heading, so two rows can read as active at once (sub + its parent l1 anchor).
          val activeSlug = activeS.value
          val activeAncestor = activeSlug.flatMap { slug =>
            val idx = props.toc.indexWhere(_.slug == slug)
            if idx >= 0 then props.toc.take(idx + 1).reverse.find(_.depth == baseDepth).map(_.slug)
            else None
          }

          <.aside(
            ^.className  := "cortex-reader-toc",
            ^.aria.label := "On this page",
            <.p(^.className := "cortex-reader-toc__eyebrow", "Contents"),
            <.ul(
              ^.className := "cortex-reader-toc__list",
              props.toc.toTagMod { item =>
                val level    = levelOf(item.depth, baseDepth)
                val isActive = activeSlug.contains(item.slug) || activeAncestor.contains(item.slug)
                val rowCls = {
                  val base = s"cortex-reader-toc__row cortex-reader-toc__row--l$level"
                  if isActive then s"$base cortex-reader-toc__row--active" else base
                }
                <.li(
                  ^.key       := s"toc-${item.slug}",
                  ^.className := rowCls,
                  <.a(
                    ^.href      := s"#${item.slug}",
                    ^.className := "cortex-reader-toc__btn",
                    ^.onClick ==> ((e: ReactMouseEvent) => HashScroll.onHashLinkClick(e, item.slug)),
                    <.span(^.className := "cortex-reader-toc__tick", ^.aria.hidden := true),
                    <.span(^.className := "cortex-reader-toc__label", item.text)
                  )
                )
              }
            )
          )
      }
