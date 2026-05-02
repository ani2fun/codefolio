package codefolio.client.components.knowledge

import codefolio.client.markdown.MarkdownRenderer
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/** Right-rail "On This Page" TOC with IntersectionObserver-driven active
  * heading tracking. Hidden below xl. The desktop variant is rendered as a
  * separate column in `KnowledgeReaderLayout`; mobile uses `MobileToc`.
  */
object KnowledgeToc:

  final case class Props(toc: List[MarkdownRenderer.TocEntry])

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(Option.empty[String]) // active slug
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
              // We don't disconnect on unmount — the observer is short-lived
              // (a few extra invocations after navigating away aren't
              // observable). On chapter change, the dependency triggers this
              // effect again and the new observer simply replaces the role.
            ()
          }
      }
      .render { (props, activeS) =>
        if props.toc.isEmpty then EmptyVdom
        else
          <.aside(
            ^.className :=
              "hidden xl:block sticky top-20 self-start max-h-[calc(100vh-5rem)] overflow-y-auto p-6",
            <.p(
              ^.className := "text-xs font-bold uppercase tracking-wider text-foreground mb-3",
              "On This Page"
            ),
            <.ul(
              ^.className := "space-y-1.5 border-l border-border",
              props.toc.toTagMod { item =>
                val isActive = activeS.value.contains(item.slug)
                val cls =
                  if isActive then
                    "block -ml-px border-l-2 pl-3 py-1 text-xs transition-colors " +
                      "border-primary text-primary font-semibold"
                  else
                    "block -ml-px border-l-2 pl-3 py-1 text-xs transition-colors " +
                      "border-transparent text-muted-foreground hover:text-foreground"
                <.li(
                  ^.key := item.slug,
                  ^.style := js.Dynamic
                    .literal(paddingLeft = (item.depth - 2) * 12)
                    .asInstanceOf[js.Object],
                  <.a(^.href := s"#${item.slug}", ^.className := cls, item.text)
                )
              }
            )
          )
      }
