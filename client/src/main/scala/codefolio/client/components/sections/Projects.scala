package codefolio.client.components.sections

import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import codefolio.client.components.ui.Section
import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object Projects:

  private def iconLink(href: String, ariaLabel: String, modifier: String, icon: VdomNode): VdomNode =
    val cls = if modifier.isEmpty then "projects__icon-link" else s"projects__icon-link $modifier"
    <.a(
      ^.href       := href,
      ^.rel        := "noopener noreferrer",
      ^.target     := "_blank",
      ^.aria.label := ariaLabel,
      ^.className  := cls,
      icon
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    Section("projects", "projects")(
      <.h2(^.className := "projects__title", "Projects"),
      <.div(
        ^.className := "projects__grid",
        PortfolioData.projects.toList.zipWithIndex.toTagMod { case (project, idx) =>
          val isGithubOnly = project.projectUrl == project.githubUrl
          <.div(
            ^.key       := idx,
            ^.className := "projects__card",
            <.img(
              ^.src               := project.image.url,
              ^.alt               := project.image.alt,
              VdomAttr("width")   := "400",
              VdomAttr("height")  := "240",
              VdomAttr("loading") := "lazy",
              ^.className         := "projects__image"
            ),
            <.div(
              ^.className := "projects__body",
              <.div(
                ^.className := "projects__icon-row",
                if isGithubOnly then EmptyVdom
                else
                  iconLink(
                    project.projectUrl,
                    s"Open ${project.name}",
                    "projects__icon-link--open",
                    LucideIcons.ExternalLink(LucideIcons.withClass("projects__icon-svg"))
                  )
                ,
                iconLink(
                  project.githubUrl,
                  s"${project.name} on GitHub",
                  "",
                  BrandIcons.Github("projects__icon-svg")
                )
              ),
              <.h3(^.className := "projects__name", project.name),
              <.p(^.className  := "projects__description", project.description),
              <.div(
                ^.className := "projects__tag-row",
                project.tags.toList.toTagMod { tag =>
                  <.span(^.key := tag, ^.className := "projects__tag", tag)
                }
              )
            )
          )
        }
      )
    )
  }
