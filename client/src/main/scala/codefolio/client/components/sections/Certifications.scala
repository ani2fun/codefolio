package codefolio.client.components.sections

import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.vdom.svg_<^

/** Vertical timeline of diplomas and course certificates. Highlighted entries
  * (e.g. the Diploma) get a star marker and a subtly shaded card background.
  */
object Certifications:

  // Marker SVGs — both 24×24, kept inline since they don't change.
  private def starMarker: VdomNode =
    svg_<^.<.svg(
      ^.className := "w-3 h-3 md:w-3.5 md:h-3.5 text-white",
      svg_<^.^.viewBox := "0 0 24 24",
      svg_<^.^.fill := "currentColor",
      svg_<^.^.xmlns := "http://www.w3.org/2000/svg",
      svg_<^.<.path(
        svg_<^.^.d :=
          "M12 2l2.39 4.84L19.78 7.7l-3.89 3.79.92 5.36L12 14.27l-4.81 2.58.92-5.36L4.22 7.7l5.39-.86L12 2z"
      )
    )

  private def trophyMarker: VdomNode =
    svg_<^.<.svg(
      ^.className := "w-3 h-3 md:w-3.5 md:h-3.5 text-white",
      svg_<^.^.viewBox := "0 0 24 24",
      svg_<^.^.fill := "none",
      svg_<^.^.stroke := "currentColor",
      svg_<^.^.strokeWidth := 2.5,
      svg_<^.^.strokeLinecap := "round",
      svg_<^.^.strokeLinejoin := "round",
      svg_<^.^.xmlns := "http://www.w3.org/2000/svg",
      svg_<^.<.circle(svg_<^.^.cx := 12, svg_<^.^.cy := 8, svg_<^.^.r := 6),
      svg_<^.<.path(svg_<^.^.d := "M15.477 12.89L17 22l-5-3-5 3 1.523-9.11")
    )

  private def viewArrow: VdomNode =
    svg_<^.<.svg(
      ^.className := "w-3 h-3 ml-1 group-hover:translate-x-0.5 transition-transform",
      svg_<^.^.viewBox := "0 0 24 24",
      svg_<^.^.fill := "none",
      svg_<^.^.xmlns := "http://www.w3.org/2000/svg",
      svg_<^.<.path(
        svg_<^.^.d := "M14 3h7v7M10 14L21 3M21 14v7h-7M3 10V3h7",
        svg_<^.^.stroke := "currentColor",
        svg_<^.^.strokeWidth := 2,
        svg_<^.^.strokeLinecap := "round",
        svg_<^.^.strokeLinejoin := "round"
      )
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    val items = PortfolioData.certifications.toList
    val lastIdx = items.length - 1

    <.section(
      ^.id := "certifications",
      ^.className := "px-4 md:px-8 pt-28 md:pt-32 pb-12 rounded-lg shadow-md font-sans scroll-mt-24",
      <.h2(
        ^.className := "text-3xl md:text-5xl font-bold text-center text-blue-700 mb-3",
        "Certifications"
      ),
      <.p(
        ^.className := "text-center text-gray-600 dark:text-gray-400 mb-10 text-sm md:text-base",
        "A timeline of diplomas and course certificates. Click any card to open the PDF."
      ),
      <.div(
        ^.className := "relative max-w-3xl mx-auto pl-10 md:pl-16",
        // Vertical timeline spine
        <.div(
          ^.className :=
            "absolute left-3 md:left-6 top-1 bottom-1 w-0.5 rounded-full " +
              "bg-gradient-to-b from-blue-600 via-blue-500 to-blue-300 " +
              "dark:from-blue-400 dark:via-blue-500 dark:to-blue-700",
          ^.aria.hidden := true
        ),
        items.zipWithIndex.toTagMod { case (cert, idx) =>
          val markerCls =
            if cert.highlight then
              "bg-rose-500 ring-rose-100 dark:ring-rose-950 shadow-md shadow-rose-500/30"
            else "bg-blue-600 ring-blue-100 dark:ring-blue-950"

          val cardCls =
            if cert.highlight then
              "border border-blue-700 dark:border-blue-400 bg-blue-50/40 dark:bg-blue-950/40 " +
                "hover:bg-blue-100/60 dark:hover:bg-blue-900/40"
            else
              "border border-blue-500 dark:border-gray-50 hover:bg-blue-50 dark:hover:bg-gray-900"

          val containerCls =
            if idx == lastIdx then "relative"
            else "relative mb-5 md:mb-6"

          <.div(
            ^.key := idx,
            ^.className := containerCls,
            // Marker dot
            <.div(
              ^.className :=
                "absolute -left-[34px] md:-left-[48px] top-1.5 flex items-center justify-center " +
                  "w-6 h-6 md:w-7 md:h-7 rounded-full ring-[3px] " + markerCls,
              ^.aria.hidden := true,
              if cert.highlight then starMarker else trophyMarker
            ),
            // Date + length row
            <.div(
              ^.className := "flex flex-wrap items-baseline gap-x-2 mb-1.5",
              <.span(
                ^.className := "text-xs md:text-sm font-bold text-rose-500 uppercase tracking-wide",
                cert.date
              ),
              <.span(
                ^.className := "text-xs text-gray-500 dark:text-gray-400",
                s"· ${cert.length}"
              )
            ),
            // Card
            <.a(
              ^.href := cert.url,
              ^.target := "_blank",
              ^.rel := "noopener noreferrer",
              ^.className := s"group block rounded-md px-4 py-3 md:px-5 md:py-4 transition-colors $cardCls",
              <.div(
                ^.className := "flex flex-wrap items-baseline gap-x-2 mb-1",
                <.span(
                  ^.className :=
                    "text-[11px] md:text-xs uppercase tracking-wider text-gray-600 dark:text-gray-400 font-semibold",
                  cert.issuer
                )
              ),
              <.h3(
                ^.className :=
                  "text-base md:text-lg font-bold text-blue-900 dark:text-gray-50 leading-snug mb-1.5 " +
                    "group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors",
                cert.name,
                if cert.highlight then
                  <.span(
                    ^.className :=
                      "ml-2 align-middle inline-block text-[10px] px-1.5 py-0.5 rounded " +
                        "bg-rose-500 text-white font-semibold uppercase tracking-wide",
                    "Diploma"
                  )
                else EmptyVdom
              ),
              <.p(
                ^.className := "text-xs md:text-sm text-gray-700 dark:text-gray-300 mb-2.5 leading-relaxed",
                cert.description
              ),
              <.div(
                ^.className := "flex flex-wrap items-center gap-1.5",
                cert.tags.toList.toTagMod { tag =>
                  <.span(
                    ^.key := tag,
                    ^.className :=
                      "text-[10px] md:text-xs px-1.5 py-0.5 rounded text-blue-900 bg-blue-200 dark:text-gray-50 dark:bg-blue-600",
                    tag
                  )
                },
                <.span(
                  ^.className :=
                    "ml-auto inline-flex items-center text-xs md:text-sm text-blue-600 dark:text-blue-400 font-semibold",
                  "View",
                  viewArrow
                )
              )
            )
          )
        }
      )
    )
  }
