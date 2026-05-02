package codefolio.client.components.sections

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object About:

  /** Helper for the photo grid: an image + a translucent blue overlay on
    * top, both rounded. Mirrors the original `<Image fill />` + overlay div
    * pattern — without next/image's layout=fill we use absolute positioning
    * inside a relatively-positioned parent.
    */
  private def photoTile(
      gridArea: String,
      src: String,
      alt: String,
      objectFit: Boolean = true
  ): VdomNode =
    <.div(
      ^.className := s"relative $gridArea",
      <.img(
        ^.src := src,
        ^.alt := alt,
        VdomAttr("loading") := "lazy",
        ^.className :=
          (if objectFit then "absolute inset-0 w-full h-full object-cover rounded-lg"
           else "w-full h-full object-cover rounded-lg")
      ),
      <.div(^.className := "inset-0 bg-blue-200 dark:bg-blue-600 opacity-50 rounded-lg")
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    <.section(
      ^.id := "about",
      ^.className := "min-h-screen px-8 rounded-lg shadow-md pt-32 scroll-mt-24",
      <.div(
        ^.className := "w-full grid grid-cols-1 lg:grid-cols-10 gap-8",
        // Left: prose
        <.div(
          ^.className := "col-span-full lg:col-span-6 flex flex-col justify-center",
          <.h2(^.className := "text-3xl md:text-5xl font-bold text-blue-700 mb-6", "About"),
          <.p(
            ^.className := "text-gray-700 mb-6 text-lg md:text-2xl leading-relaxed dark:text-gray-300",
            "Backend-leaning Software Engineer with ",
            <.span(^.className := "text-blue-700 font-semibold", "ten years"),
            " on production systems. At ",
            <.span(^.className := "font-bold text-blue-700", "Disneyland Paris"),
            " I co-led a pilot that turned into a multi-million-euro replacement of the legacy marketing platform. At ",
            <.span(^.className := "font-bold text-blue-700", "Audi"),
            " I built the labeling pipeline behind their autonomous-driving model training."
          ),
          <.p(
            ^.className := "text-gray-700 mb-6 text-lg md:text-2xl leading-relaxed dark:text-gray-300",
            "Currently at ",
            <.span(^.className := "text-green-700 font-bold", "Europcar International"),
            ", building the customer authentication and account lifecycle on Kotlin and Spring Boot, plus the wider reservation platform around it. Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP."
          ),
          <.p(
            ^.className := "text-gray-700 mb-6 text-lg md:text-2xl leading-relaxed dark:text-gray-300",
            "Outside work I run a small four-node ",
            <.span(^.className := "font-bold text-blue-700", "K3s cluster"),
            " on commodity hardware at home and self-host my own services on it — including this site. I also maintain an open-source ",
            <.span(^.className := "font-bold text-blue-700", "Gradle plugin"),
            " on the official Plugin Portal for publishing JVM artefacts to Maven Central, and I'm wrapping up the ",
            <.span(^.className := "font-bold text-blue-700", "Diplôme Data Engineer"),
            " (RNCP Niveau 7) at Sorbonne University / DataScientest."
          ),
          <.p(
            ^.className := "text-gray-700 mb-6 text-base md:text-xl leading-relaxed dark:text-gray-400",
            "Master's in Information Technology from ",
            <.span(^.className := "font-semibold", "ISEP Paris"),
            " (Grande École). B.E. in Electronics & Telecommunications from the ",
            <.span(^.className := "font-semibold", "University of Mumbai"),
            "."
          )
        ),
        // Right: 4-image collage
        <.div(
          ^.className := "col-span-full lg:col-span-4",
          <.div(
            ^.className := "grid grid-cols-8 grid-rows-8 gap-2",
            photoTile(
              "row-start-2 row-span-3 col-start-2 col-span-3",
              "/img/portfolio/image1.webp",
              "Aniket on a viewing platform overlooking Amsterdam"
            ),
            photoTile(
              "row-start-5 row-span-4 col-span-4",
              "/img/portfolio/image2.webp",
              "Aniket — portrait"
            ),
            photoTile(
              "row-start-1 row-span-5 col-start-5 col-span-3",
              "/img/portfolio/image3.webp",
              "Aniket — outdoors"
            ),
            photoTile(
              "row-start-6 row-span-2 col-start-5 col-span-2",
              "/img/portfolio/image4.webp",
              "Image 4",
              objectFit = false
            )
          )
        )
      )
    )
  }
