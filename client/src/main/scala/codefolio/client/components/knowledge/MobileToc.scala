package codefolio.client.components.knowledge

import codefolio.client.components.icons.LucideIcons
import codefolio.client.markdown.MarkdownRenderer
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js

/** Collapsible "On This Page" TOC for narrow screens (xl and below). */
object MobileToc:

  final case class Props(toc: List[MarkdownRenderer.TocEntry])

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false)
      .render { (props, openS) =>
        if props.toc.isEmpty then EmptyVdom
        else
          <.div(
            ^.className := "xl:hidden mb-8 rounded-lg border border-border",
            <.button(
              ^.tpe := "button",
              ^.onClick --> openS.modState(!_),
              ^.aria.expanded := openS.value,
              ^.className :=
                "w-full flex items-center justify-between px-4 py-3 text-sm font-semibold",
              <.span(
                ^.className := "flex items-center gap-2",
                LucideIcons.ListTree(LucideIcons.withClass("h-4 w-4")),
                " On This Page"
              ),
              LucideIcons.ChevronDown(
                LucideIcons.withClass(
                  s"h-4 w-4 transition-transform ${if openS.value then "rotate-180" else ""}"
                )
              )
            ),
            if openS.value then
              <.ul(
                ^.className := "px-4 pb-3 space-y-1.5 border-t border-border pt-3",
                props.toc.toTagMod { item =>
                  <.li(
                    ^.key := item.slug,
                    ^.style := js.Dynamic
                      .literal(paddingLeft = (item.depth - 2) * 12)
                      .asInstanceOf[js.Object],
                    <.a(
                      ^.href := s"#${item.slug}",
                      ^.onClick --> openS.setState(false),
                      ^.className := "text-sm text-muted-foreground hover:text-primary",
                      item.text
                    )
                  )
                }
              )
            else EmptyVdom
          )
      }
