package codefolio.client.components.sections

import codefolio.client.components.ui.Section
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object Hero:

  val Component = ScalaFnComponent[Unit] { _ =>
    Section("hero", "hero")(
      <.div(
        ^.className := "hero__inner",
        <.h1(
          ^.className := "hero__title",
          "Hi, I'm",
          <.span(^.className := "hero__title-name", "Aniket Kakde")
        ),
        <.p(
          ^.className := "hero__byline",
          <.span(^.className := "hero__byline-strong", "Backend-leaning Software Engineer"),
          <.span(" based in Paris."),
          <.br,
          <.span(
            ^.className := "hero__byline-secondary",
            "Ten years on production systems at Europcar, Audi, Dassault, and Disneyland Paris."
          ),
          <.br,
          <.span(
            ^.className := "hero__byline-tertiary",
            "Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP."
          )
        ),
        <.div(
          ^.className := "hero__cta-row",
          <.a(
            ^.href      := "mailto:a.r.kakde@gmail.com?subject=Hi, Aniket",
            ^.className := "hero__cta hero__cta--secondary",
            "Get in touch"
          ),
          <.a(
            ^.href      := "/Aniket-Kakde-CV-EN.pdf",
            ^.className := "hero__cta hero__cta--primary",
            "Download CV"
          )
        )
      )
    )
  }
