package codefolio.client.components.sections

import codefolio.client.components.ui.Section
import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js

/**
 * Tabbed experience section. Left rail lists company short names; clicking one shows the position, dates,
 * summary, bullet points, optional results panel, and tech-tag list on the right.
 */
object Experience:

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(0)
      .render { (_, selectedS) =>
        val items = PortfolioData.experience
        val total = items.length
        val selectedIdx =
          if total == 0 then -1
          else if selectedS.value >= total then 0
          else selectedS.value

        Section("experience", "experience")(
          <.h2(^.className := "experience__title", "Experience"),
          <.div(
            ^.className := "experience__layout",
            <.div(
              ^.className := "experience__rail",
              <.ul(
                ^.className := "experience__rail-list",
                items.zipWithIndex.toList.toTagMod { case (exp, idx) =>
                  val active = idx == selectedIdx
                  val cls =
                    if active then "experience__rail-item experience__rail-item--active"
                    else "experience__rail-item"
                  <.li(
                    ^.key       := exp.company.short,
                    ^.className := cls,
                    ^.onClick --> selectedS.setState(idx),
                    exp.company.short
                  )
                }
              )
            ),
            <.div(
              ^.className := "experience__pane",
              if selectedIdx < 0 then
                <.p(^.className := "experience__empty", "Select a company to see the experience details.")
              else renderDetails(items(selectedIdx))
            )
          )
        )
      }

  private def renderDetails(exp: PortfolioData.Experience): VdomNode =
    <.div(
      <.h2(^.className := "experience__position", exp.position),
      <.a(^.href       := exp.company.url, ^.className := "experience__company", exp.company.name),
      <.p(^.className  := "experience__time", exp.time),
      <.p(^.className  := "experience__summary", exp.description),
      <.ul(
        ^.className := "experience__bullets",
        exp.items.toList.zipWithIndex.toTagMod { case (text, idx) =>
          <.li(^.key := idx, ^.className := "experience__bullet", text)
        }
      ),
      renderResults(exp.results),
      <.p(^.className := "experience__stack-label", "Stack:"),
      <.div(
        ^.className := "experience__stack-row",
        exp.leveragedKnowledgeIn.toList.toTagMod { tech =>
          <.span(^.key := tech, ^.className := "experience__tech-tag", tech)
        }
      )
    )

  private def renderResults(maybe: js.UndefOr[js.Array[String]]): VdomNode =
    val results = maybe.toOption.toList.flatMap(_.toList)
    if results.isEmpty then EmptyVdom
    else
      <.div(
        ^.className := "experience__results",
        <.p(^.className := "experience__results-title", "Results"),
        <.ul(
          ^.className := "experience__results-list",
          results.zipWithIndex.toTagMod { case (res, idx) => <.li(^.key := idx, res) }
        )
      )
