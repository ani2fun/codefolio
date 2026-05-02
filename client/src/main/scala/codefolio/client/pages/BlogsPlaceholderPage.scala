package codefolio.client.pages

import codefolio.client.util.PageTitle
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/** "Coming soon" placeholder for /blogs and /blogs/:slug, mirrors
  * portfolio-app's blog page.
  */
object BlogsPlaceholderPage:

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useEffectOnMountBy(_ => PageTitle.set("Blog — Aniket Kakde"))
      .render { _ =>
        <.main(
          ^.className := "container mx-auto py-24 text-center",
          <.h1(^.className := "text-3xl font-bold text-foreground", "Blog"),
          <.p(^.className := "mt-4 text-muted-foreground", "Coming soon.")
        )
      }
