package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.HashScroll
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/**
 * Right-rail "On This Page" TOC rendered as a compact minimap. The visible rail shows only top-level
 * sections — defined as entries at the chapter's shallowest heading depth (typically h2) — as a vertical
 * stack of horizontal-line ticks. The active rail tick is the *parent* of the currently-visible heading,
 * so a rail tick stays highlighted while the reader works through its sub-sections. Hovering or focusing
 * the aside reveals an absolutely-positioned panel with the FULL heading list (every depth, hierarchy
 * preserved). Hidden below xl; mobile uses `MobileToc`.
 */
object CortexToc:

  final case class Props(toc: List[MarkdownRenderer.TocEntry])

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
          // Rail = only the shallowest heading depth in this chapter (typically h2). Most chapters have
          // h2 sections; some imported books start at h3, so use the actual minimum rather than hard-
          // coding 2. The rail tick that's "active" is the parent of the currently-visible heading —
          // walk back from the active entry until we hit the first rail-eligible one.
          val railDepth     = props.toc.map(_.depth).min
          val railToc       = props.toc.filter(_.depth == railDepth)
          val activeRailSlug = activeS.value.flatMap { slug =>
            val idx = props.toc.indexWhere(_.slug == slug)
            if idx >= 0 then props.toc.take(idx + 1).reverse.find(_.depth == railDepth).map(_.slug)
            else None
          }
          <.aside(
            ^.className  := "cortex-reader-toc",
            ^.aria.label := "On this page",
            <.div(
              ^.className := "cortex-reader-toc__rail",
              railToc.toTagMod { item =>
                val isActive = activeRailSlug.contains(item.slug)
                val tickCls =
                  if isActive then "cortex-reader-toc__tick cortex-reader-toc__tick--active"
                  else "cortex-reader-toc__tick"
                <.a(
                  ^.key        := s"tick-${item.slug}",
                  ^.href       := s"#${item.slug}",
                  ^.className  := tickCls,
                  ^.aria.label := item.text,
                  ^.onClick ==> ((e: ReactMouseEvent) => HashScroll.onHashLinkClick(e, item.slug))
                )
              }
            ),
            <.div(
              ^.className := "cortex-reader-toc__panel",
              <.p(^.className := "cortex-reader-toc__heading", "On This Page"),
              <.ul(
                ^.className := "cortex-reader-toc__list",
                props.toc.toTagMod { item =>
                  val isActive = activeS.value.contains(item.slug)
                  val cls =
                    if isActive then "cortex-reader-toc__link cortex-reader-toc__link--active"
                    else "cortex-reader-toc__link"
                  <.li(
                    ^.key := s"panel-${item.slug}",
                    ^.style := js.Dynamic
                      .literal(paddingLeft = (item.depth - 2) * 12)
                      .asInstanceOf[js.Object],
                    <.a(
                      ^.href      := s"#${item.slug}",
                      ^.className := cls,
                      ^.onClick ==> ((e: ReactMouseEvent) => HashScroll.onHashLinkClick(e, item.slug)),
                      item.text
                    )
                  )
                }
              )
            )
          )
      }
