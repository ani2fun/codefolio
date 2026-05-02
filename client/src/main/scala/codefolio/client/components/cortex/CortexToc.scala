package codefolio.client.components.cortex

import codefolio.client.markdown.MarkdownRenderer
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/**
 * Right-rail "On This Page" TOC with IntersectionObserver-driven active heading tracking. Hidden below xl.
 * The desktop variant is rendered as a separate column in `CortexReaderLayout`; mobile uses `MobileToc`.
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
          <.aside(
            ^.className := "cortex-reader-toc",
            <.p(^.className := "cortex-reader-toc__heading", "On This Page"),
            <.ul(
              ^.className := "cortex-reader-toc__list",
              props.toc.toTagMod { item =>
                val isActive = activeS.value.contains(item.slug)
                val cls =
                  if isActive then "cortex-reader-toc__link cortex-reader-toc__link--active"
                  else "cortex-reader-toc__link"
                <.li(
                  ^.key := item.slug,
                  ^.style := js.Dynamic
                    .literal(paddingLeft = (item.depth - 2) * 12)
                    .asInstanceOf[js.Object],
                  <.a(
                    ^.href      := s"#${item.slug}",
                    ^.className := cls,
                    ^.onClick ==> { (e: ReactMouseEvent) =>
                      // Bypass scalajs-react Router's link interception (it
                      // sees `#anchor` as a URL change and rebuilds the route
                      // before the browser performs the default anchor
                      // scroll). Scroll manually + push the hash to history.
                      Callback {
                        val el = dom.document.getElementById(item.slug)
                        if el != null then
                          e.preventDefault()
                          e.stopPropagation()
                          // `behavior: 'instant'` overrides the page-wide
                          // `scroll-behavior: smooth` CSS — without this,
                          // the smooth scroll gets cancelled by the click's
                          // hashchange-driven re-render before it finishes.
                          val opts = js.Dynamic.literal(behavior = "instant", block = "start")
                          el.asInstanceOf[js.Dynamic].scrollIntoView(opts)
                          dom.window.history.replaceState(null, "", s"#${item.slug}")
                      }
                    },
                    item.text
                  )
                )
              }
            )
          )
      }
