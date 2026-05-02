package codefolio.client.components.sections

import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js

/** Tabbed experience section. Left rail lists company short names; clicking
  * one shows the position, dates, summary, bullet points, optional results
  * panel, and tech-tag list on the right.
  */
object Experience:

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(0) // selected index
      .render { (_, selectedS) =>
        val items = PortfolioData.experience
        val total = items.length
        val selectedIdx =
          if total == 0 then -1
          else if selectedS.value >= total then 0
          else selectedS.value

        <.section(
          ^.id := "experience",
          ^.className := "px-8 py-16 rounded-lg shadow-md font-sans scroll-mt-24",
          <.h2(
            ^.className := "text-3xl md:text-5xl font-bold text-center text-blue-700 mb-12",
            "Experience"
          ),
          <.div(
            ^.className := "flex flex-col md:flex-row md:h-[calc(70vh-8rem)] gap-8",
            // Left rail
            <.div(
              ^.className := "md:flex-none md:w-1/4 lg:w-1/5 rounded-md border-2 border-blue-600 dark:border-gray-50 overflow-x-auto md:overflow-y-auto",
              <.ul(
                ^.className := "flex flex-row md:flex-col min-w-max md:min-w-0",
                items.zipWithIndex.toList.toTagMod { case (exp, idx) =>
                  val active = idx == selectedIdx
                  val activeCls =
                    if active then "bg-blue-700 text-gray-50 dark:bg-blue-600 dark:text-gray-50"
                    else "text-blue-900 dark:text-gray-50"
                  <.li(
                    ^.key := exp.company.short,
                    ^.className :=
                      "cursor-pointer flex flex-col items-center justify-center " +
                        "md:text-xl lg:text-2xl text-center py-4 px-4 font-semibold whitespace-nowrap " +
                        "hover:bg-blue-400 hover:text-gray-50 " +
                        "dark:hover:bg-blue-600 dark:hover:text-gray-50 " +
                        activeCls,
                    ^.onClick --> selectedS.setState(idx),
                    exp.company.short
                  )
                }
              )
            ),
            // Right pane
            <.div(
              ^.className := "flex-1 rounded-md border-2 border-blue-500 dark:border-gray-50 p-4 overflow-y-auto",
              if selectedIdx < 0 then
                <.p("Select a company to see the experience details.")
              else
                renderDetails(items(selectedIdx))
            )
          )
        )
      }

  private def renderDetails(exp: PortfolioData.Experience): VdomNode =
    <.div(
      <.h2(^.className := "text-2sm md:text-3xl font-bold pb-2", exp.position),
      <.a(
        ^.href := exp.company.url,
        ^.className := "text-sm md:text-2xl text-blue-600 font-semibold hover:text-gray-600",
        exp.company.name
      ),
      <.p(
        ^.className := "text-sm md:text-xl text-rose-500 font-sans font-semibold pt-2",
        exp.time
      ),
      <.p(^.className := "pt-4 pb-4 text-sm md:text-2xl", exp.description),
      <.ul(
        ^.className := "list-disc pl-8 text-sm md:text-xl",
        exp.items.toList.zipWithIndex.toTagMod { case (text, idx) =>
          <.li(^.key := idx, ^.className := "p-2", text)
        }
      ),
      renderResults(exp.results),
      <.p(^.className := "p-2 pt-6 font-bold", "Stack:"),
      <.div(
        ^.className := "flex flex-wrap",
        exp.leveragedKnowledgeIn.toList.toTagMod { tech =>
          <.span(
            ^.key := tech,
            ^.className :=
              "m-1 px-2 py-1 text-sm rounded text-blue-900 bg-blue-200 dark:text-gray-50 dark:bg-blue-600",
            tech
          )
        }
      )
    )

  private def renderResults(maybe: js.UndefOr[js.Array[String]]): VdomNode =
    val results = maybe.toOption.toList.flatMap(_.toList)
    if results.isEmpty then EmptyVdom
    else
      <.div(
        ^.className :=
          "mt-4 border-l-4 border-rose-400 dark:border-rose-500 bg-rose-50 dark:bg-rose-950/30 rounded-r-md p-3 md:p-4",
        <.p(
          ^.className :=
            "text-sm md:text-lg font-bold uppercase tracking-wide text-rose-600 dark:text-rose-300 mb-2",
          "Results"
        ),
        <.ul(
          ^.className :=
            "list-disc pl-6 text-sm md:text-xl space-y-2 text-gray-800 dark:text-gray-100",
          results.zipWithIndex.toTagMod { case (res, idx) => <.li(^.key := idx, res) }
        )
      )
