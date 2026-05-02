package codefolio.client.components.knowledge

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/** A tabbed group of runnable code blocks. The markdown pipeline merges
  * adjacent ` ```<lang> run ` fences into a single placeholder div with a
  * `data-tabs` JSON payload; the post-mount walker decodes that payload
  * into `Tab` records and instantiates this component.
  *
  * Each tab renders a `bare` RunnableCodeBlock (no inner card — the group
  * provides the outer card) toggled via `hidden`, mirroring the original
  * TS port. Per-tab state lives inside each child block.
  */
object RunnableCodeGroup:

  final case class Tab(language: String, languageLabel: String, source: String)
  final case class Props(tabs: List[Tab])

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(0) // active tab index
      .render { (props, activeS) =>
        if props.tabs.isEmpty then EmptyVdom
        else if props.tabs.sizeIs == 1 then
          val only = props.tabs.head
          RunnableCodeBlock.Component(
            RunnableCodeBlock.Props(only.language, only.source, Some(only.languageLabel))
          )
        else
          val active = if activeS.value >= props.tabs.size then 0 else activeS.value
          <.div(
            ^.className := "my-6 rounded-lg border border-border overflow-hidden",
            <.div(
              ^.role := "tablist",
              ^.className := "flex border-b border-border bg-muted/40 overflow-x-auto",
              props.tabs.zipWithIndex.toTagMod { case (tab, i) =>
                val isActive = i == active
                <.button(
                  ^.key := s"${tab.language}-$i",
                  ^.role := "tab",
                  ^.tpe := "button",
                  ^.aria.selected := isActive,
                  ^.onClick --> activeS.setState(i),
                  ^.className :=
                    s"px-4 py-2 text-xs font-semibold whitespace-nowrap border-b-2 transition-colors ${
                        if isActive then "border-primary text-foreground"
                        else "border-transparent text-muted-foreground hover:text-foreground"
                      }",
                  tab.languageLabel
                )
              }
            ),
            // Render every tab; toggle visibility so each tab keeps its own
            // editor/state across switches.
            props.tabs.zipWithIndex.toTagMod { case (tab, i) =>
              <.div(
                ^.key := s"${tab.language}-$i",
                ^.hidden := (i != active),
                RunnableCodeBlock.Component(
                  RunnableCodeBlock.Props(
                    language = tab.language,
                    source = tab.source,
                    languageLabel = Some(tab.languageLabel),
                    bare = true,
                    hideLanguageLabel = true
                  )
                )
              )
            }
          )
      }
