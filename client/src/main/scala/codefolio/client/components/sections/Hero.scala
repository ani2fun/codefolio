package codefolio.client.components.sections

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object Hero:

  val Component = ScalaFnComponent[Unit] { _ =>
    <.section(
      ^.id := "hero",
      ^.className := "min-h-screen flex items-center justify-center px-8 rounded-lg shadow-md",
      <.div(
        ^.className := "text-center",
        <.h1(
          ^.className := "text-lg md:text-4xl font-semibold text-gray-900 dark:text-gray-100 relative mb-6",
          "Hi, I'm",
          <.span(
            ^.className := "block text-3xl md:text-6xl font-bold mt-3 text-blue-700 dark:text-blue-700",
            "Aniket Kakde"
          )
        ),
        <.p(
          ^.className := "text-lg md:text-3xl mt-2 md:mt-8 text-gray-700 dark:text-gray-300 max-w-4xl mx-auto",
          <.span(
            ^.className := "font-bold text-blue-600 dark:text-blue-400",
            "Backend-leaning Software Engineer"
          ),
          <.span(" based in Paris."),
          <.br,
          <.span(
            ^.className := "text-sm md:text-2xl",
            "Ten years on production systems at Europcar, Audi, Dassault, and Disneyland Paris."
          ),
          <.br,
          <.span(
            ^.className := "text-sm md:text-xl text-gray-600 dark:text-gray-400",
            "Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP."
          )
        ),
        <.div(
          ^.className := "mt-8 flex flex-col md:flex-row justify-center gap-4",
          <.a(
            ^.href := "mailto:a.r.kakde@gmail.com?subject=Hi, Aniket",
            ^.className := "font-semibold border-2 border-blue-600 dark:bg-gray-100 dark:hover:bg-gray-900 text-blue-900 hover:text-gray-50 hover:bg-sky-400 py-2 px-6 rounded-md transition duration-300",
            "Get in touch"
          ),
          <.a(
            ^.href := "/Aniket-Kakde-CV-EN.pdf",
            ^.className := "font-semibold border-2 border-blue-600 bg-blue-600 text-gray-50 hover:bg-gray-50 hover:text-slate-900 py-2 px-6 rounded-md transition duration-300",
            "Download CV"
          )
        )
      )
    )
  }
