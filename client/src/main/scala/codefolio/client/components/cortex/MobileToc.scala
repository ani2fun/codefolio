package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import codefolio.client.markdown.MarkdownRenderer
import codefolio.client.util.HashScroll
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
            ^.className := "cortex-reader-toc-mobile",
            <.button(
              ^.tpe := "button",
              ^.onClick --> openS.modState(!_),
              ^.aria.expanded := openS.value,
              ^.className     := "cortex-reader-toc-mobile__toggle",
              <.span(
                ^.className := "cortex-reader-toc-mobile__label",
                LucideIcons.ListTree(LucideIcons.withClass("h-4 w-4")),
                " On This Page"
              ),
              LucideIcons.ChevronDown(
                LucideIcons.withClass(
                  if openS.value then
                    "cortex-reader-toc-mobile__chevron cortex-reader-toc-mobile__chevron--open"
                  else "cortex-reader-toc-mobile__chevron"
                )
              )
            ),
            if openS.value then
              <.ul(
                ^.className := "cortex-reader-toc-mobile__list",
                props.toc.toTagMod { item =>
                  <.li(
                    ^.key := item.slug,
                    ^.style := js.Dynamic
                      .literal(paddingLeft = (item.depth - 2) * 12)
                      .asInstanceOf[js.Object],
                    <.a(
                      ^.href := s"#${item.slug}",
                      ^.onClick ==> ((e: ReactMouseEvent) =>
                        HashScroll.onHashLinkClick(e, item.slug) >> openS.setState(false)
                      ),
                      ^.className := "cortex-reader-toc-mobile__link",
                      item.text
                    )
                  )
                }
              )
            else EmptyVdom
          )
      }
