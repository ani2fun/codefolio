package codefolio.client.components.sections

import codefolio.client.components.ui.Section
import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.vdom.svg_<^

/**
 * Vertical timeline of diplomas and course certificates. Highlighted entries (e.g. the Diploma) get a star
 * marker and a subtly shaded card background.
 */
object Certifications:

  private def starMarker: VdomNode =
    svg_<^.<.svg(
      ^.className      := "certifications__marker-icon",
      svg_<^.^.viewBox := "0 0 24 24",
      svg_<^.^.fill    := "currentColor",
      svg_<^.^.xmlns   := "http://www.w3.org/2000/svg",
      svg_<^.<.path(
        svg_<^.^.d :=
          "M12 2l2.39 4.84L19.78 7.7l-3.89 3.79.92 5.36L12 14.27l-4.81 2.58.92-5.36L4.22 7.7l5.39-.86L12 2z"
      )
    )

  private def trophyMarker: VdomNode =
    svg_<^.<.svg(
      ^.className             := "certifications__marker-icon",
      svg_<^.^.viewBox        := "0 0 24 24",
      svg_<^.^.fill           := "none",
      svg_<^.^.stroke         := "currentColor",
      svg_<^.^.strokeWidth    := 2.5,
      svg_<^.^.strokeLinecap  := "round",
      svg_<^.^.strokeLinejoin := "round",
      svg_<^.^.xmlns          := "http://www.w3.org/2000/svg",
      svg_<^.<.circle(svg_<^.^.cx := 12, svg_<^.^.cy := 8, svg_<^.^.r := 6),
      svg_<^.<.path(svg_<^.^.d    := "M15.477 12.89L17 22l-5-3-5 3 1.523-9.11")
    )

  private def viewArrow: VdomNode =
    svg_<^.<.svg(
      ^.className      := "certifications__view-arrow",
      svg_<^.^.viewBox := "0 0 24 24",
      svg_<^.^.fill    := "none",
      svg_<^.^.xmlns   := "http://www.w3.org/2000/svg",
      svg_<^.<.path(
        svg_<^.^.d              := "M14 3h7v7M10 14L21 3M21 14v7h-7M3 10V3h7",
        svg_<^.^.stroke         := "currentColor",
        svg_<^.^.strokeWidth    := 2,
        svg_<^.^.strokeLinecap  := "round",
        svg_<^.^.strokeLinejoin := "round"
      )
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    val items   = PortfolioData.certifications.toList
    val lastIdx = items.length - 1

    Section("certifications", "certifications")(
      <.h2(^.className := "certifications__title", "Certifications"),
      <.p(
        ^.className := "certifications__subtitle",
        "A timeline of diplomas and course certificates. Click any card to open the PDF."
      ),
      <.div(
        ^.className := "certifications__timeline",
        <.div(^.className := "certifications__spine", ^.aria.hidden := true),
        items.zipWithIndex.toTagMod { case (cert, idx) =>
          val entryCls =
            if idx == lastIdx then "certifications__entry certifications__entry--last"
            else "certifications__entry"
          val markerCls =
            if cert.highlight then "certifications__marker certifications__marker--highlight"
            else "certifications__marker"
          val cardCls =
            if cert.highlight then "certifications__card certifications__card--highlight group"
            else "certifications__card group"

          <.div(
            ^.key       := idx,
            ^.className := entryCls,
            <.div(
              ^.className   := markerCls,
              ^.aria.hidden := true,
              if cert.highlight then starMarker else trophyMarker
            ),
            <.div(
              ^.className := "certifications__meta",
              <.span(^.className := "certifications__date", cert.date),
              <.span(^.className := "certifications__length", s"· ${cert.length}")
            ),
            <.a(
              ^.href      := cert.url,
              ^.target    := "_blank",
              ^.rel       := "noopener noreferrer",
              ^.className := cardCls,
              <.div(
                ^.className := "certifications__issuer-row",
                <.span(^.className := "certifications__issuer", cert.issuer)
              ),
              <.h3(
                ^.className := "certifications__name",
                cert.name,
                if cert.highlight then <.span(^.className := "certifications__badge", "Diploma")
                else EmptyVdom
              ),
              <.p(^.className := "certifications__description", cert.description),
              <.div(
                ^.className := "certifications__footer",
                cert.tags.toList.toTagMod { tag =>
                  <.span(^.key := tag, ^.className := "certifications__tag", tag)
                },
                <.span(^.className := "certifications__view", "View", viewArrow)
              )
            )
          )
        }
      )
    )
  }
