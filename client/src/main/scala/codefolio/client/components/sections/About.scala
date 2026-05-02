package codefolio.client.components.sections

import codefolio.client.components.ui.Section
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

object About:

  /**
   * Helper for the photo grid: image + translucent overlay, both rounded. `gridArea` (the row/col span
   * utilities) is per-instance, so it stays inline; everything else lives in `.about__photo*` BEM classes.
   */
  private def photoTile(
      gridArea: String,
      src: String,
      alt: String,
      noFit: Boolean = false
  ): VdomNode =
    <.div(
      ^.className := s"about__photo $gridArea",
      <.img(
        ^.src               := src,
        ^.alt               := alt,
        VdomAttr("loading") := "lazy",
        ^.className         := (if noFit then "about__photo-img--no-fit" else "about__photo-img")
      ),
      <.div(^.className := "about__photo-overlay")
    )

  val Component = ScalaFnComponent[Unit] { _ =>
    Section("about", "about")(
      <.div(
        ^.className := "about__layout",
        // Left: prose
        <.div(
          ^.className := "about__prose",
          <.h2(^.className := "about__title", "About"),
          <.p(
            ^.className := "about__paragraph",
            "Backend-leaning Software Engineer with ",
            <.span(^.className := "about__emphasis--semibold", "ten years"),
            " on production systems. At ",
            <.span(^.className := "about__emphasis", "Disneyland Paris"),
            " I co-led a pilot that turned into a multi-million-euro replacement of the legacy marketing platform. For client ",
            <.span(^.className := "about__emphasis", "Audi"),
            " I worked on the core team responsible for building a labeling pipeline that supported autonomous-driving model training."
          ),
          <.p(
            ^.className := "about__paragraph",
            "Currently at ",
            <.span(^.className := "about__emphasis--green", "Europcar International"),
            ", building the customer authentication and account lifecycle on Kotlin and Spring Boot, plus the wider reservation platform around it. Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP."
          ),
          <.p(
            ^.className := "about__paragraph",
            "Outside work I run a small four-node ",
            <.span(^.className := "about__emphasis", "K3s cluster"),
            " on commodity hardware at home and self-host my own services on it — including this site. I also maintain an open-source ",
            <.span(^.className := "about__emphasis", "Gradle plugin"),
            " on the official Plugin Portal for publishing JVM artefacts to Maven Central, and I'm wrapping up the ",
            <.span(^.className := "about__emphasis", "Diplôme Data Engineer"),
            " (RNCP Niveau 7) at Sorbonne University / DataScientest."
          ),
          <.p(
            ^.className := "about__paragraph about__paragraph--small",
            "Master's in Information Technology from ",
            <.span(^.className := "about__emphasis--neutral", "ISEP Paris"),
            " (Grande École). B.E. in Electronics & Telecommunications from the ",
            <.span(^.className := "about__emphasis--neutral", "University of Mumbai"),
            "."
          )
        ),
        // Right: 4-image collage
        <.div(
          ^.className := "about__collage",
          <.div(
            ^.className := "about__collage-grid",
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
              noFit = true
            )
          )
        )
      )
    )
  }
