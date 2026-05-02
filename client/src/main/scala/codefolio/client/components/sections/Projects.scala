package codefolio.client.components.sections

import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.vdom.svg_<^

object Projects:

  // Inline SVG icon paths (Material-style "open in new" + GitHub) lifted from
  // the Next.js source. Using raw <svg> nodes keeps the bundle minimal — these
  // never change so a lucide round-trip is overkill.
  private val openProjectPathD =
    "M14,3V5H17.59L7.76,14.83L9.17,16.24L19,6.41V10H21V3M19,19H5V5H12V3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V12H19V19Z"

  private val githubPathD =
    "M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.468-2.381 1.235-3.221-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.957-.267 1.98-.4 3-.405 1.02.005 2.043.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.221 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.21 0 1.595-.015 2.875-.015 3.27 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"

  private def iconLink(href: String, ariaLabel: String, title: String, pathD: String, extraCls: String): VdomNode =
    <.a(
      ^.href := href,
      ^.rel := "noopener noreferrer",
      ^.target := "_blank",
      ^.aria.label := ariaLabel,
      ^.className := s"hover:scale-125 $extraCls text-gray-700 dark:text-black hover:text-blue-800 dark:hover:text-blue-600 transition-transform duration-100",
      svg_<^.<.svg(
        VdomAttr("role") := "img",
        svg_<^.^.viewBox := "0 0 24 24",
        ^.className := "custom-icon w-7 h-7",
        svg_<^.^.xmlns := "http://www.w3.org/2000/svg",
        svg_<^.<.title(title),
        svg_<^.<.path(svg_<^.^.fill := "currentColor", svg_<^.^.d := pathD)
      )
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    <.section(
      ^.id := "projects",
      ^.className := "min-h-screen rounded-lg shadow-md md:p-10 p-2 pt-24 md:pt-28 scroll-mt-24",
      <.h2(
        ^.className := "text-3xl md:text-5xl font-bold text-center text-blue-700 mb-12",
        "Projects"
      ),
      <.div(
        ^.className := "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8",
        PortfolioData.projects.toList.zipWithIndex.toTagMod { case (project, idx) =>
          val isGithubOnly = project.projectUrl == project.githubUrl
          <.div(
            ^.key := idx,
            ^.className := "relative flex flex-col dark:bg-slate-300 rounded-lg shadow-md overflow-hidden",
            <.img(
              ^.src := project.image.url,
              ^.alt := project.image.alt,
              VdomAttr("width")   := "400",
              VdomAttr("height")  := "240",
              VdomAttr("loading") := "lazy",
              ^.className := "w-full h-48 object-cover"
            ),
            <.div(
              ^.className := "p-6 flex flex-col flex-1",
              <.div(
                ^.className := "flex",
                if isGithubOnly then EmptyVdom
                else
                  iconLink(
                    project.projectUrl,
                    s"Open ${project.name}",
                    "Open project",
                    openProjectPathD,
                    "mr-3 hover:text-blue-600 dark:hover:text-blue-600"
                  ),
                iconLink(
                  project.githubUrl,
                  s"${project.name} on GitHub",
                  "GitHub",
                  githubPathD,
                  ""
                )
              ),
              <.h3(^.className := "pt-6 text-xl font-semibold text-blue-700", project.name),
              <.p(^.className := "text-gray-700 mt-2 flex-1", project.description),
              <.div(
                ^.className := "flex flex-wrap mt-4",
                project.tags.toList.toTagMod { tag =>
                  <.span(
                    ^.key := tag,
                    ^.className :=
                      "m-1 px-2 py-1 text-xs rounded text-blue-900 bg-blue-200 dark:text-gray-50 dark:bg-blue-600",
                    tag
                  )
                }
              )
            )
          )
        }
      )
    )
  }
