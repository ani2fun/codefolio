package codefolio.client.components.sections

import codefolio.client.components.ToggleMode
import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js

object Footer:

  private def socialLink(href: String, icon: VdomNode): VdomNode =
    <.a(
      ^.href      := href,
      ^.rel       := "noopener noreferrer",
      ^.target    := "_blank",
      ^.className := "footer__social-link",
      icon
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    val year = new js.Date().getFullYear().toInt

    <.footer(
      ^.className := "footer container",
      <.div(
        ^.className := "footer__row",
        <.div(
          ^.className := "footer__credit",
          "Made with",
          LucideIcons.Heart(LucideIcons.withClass("footer__credit-icon [fill:currentColor]")),
          s"in Paris by Aniket © $year"
        ),
        <.div(^.className := "footer__toggle", ToggleMode.Component()),
        <.div(
          ^.className := "footer__socials",
          <.div(
            ^.className := "footer__social-row",
            socialLink(
              "https://www.linkedin.com/in/aniketkakde/",
              BrandIcons.LinkedIn("footer__social-icon")
            ),
            socialLink(
              "https://github.com/ani2fun",
              BrandIcons.Github("footer__social-icon")
            )
          )
        )
      )
    )
  }
