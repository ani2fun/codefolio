package codefolio.client

import codefolio.client.pages.*
import japgolly.scalajs.react.extra.router.*

/** scalajs-react Router using HTML5 history (clean URLs).
  *
  * Routes are declared bottom-up: each `staticRoute` / `dynamicRouteCT`
  * line maps a **URL pattern** to a `Page` ADT case to a **component to
  * render**. The `Page` cases come from `client/Page.scala` (a sealed
  * trait — adding a new route means adding a case there too).
  *
  * Pattern matchers use the router DSL:
  *   - `root`                                 → "/"
  *   - `staticRoute("knowledge", ...)`        → "/knowledge"
  *   - `("knowledge" / seg).caseClass[...]`   → "/knowledge/<one-segment>"
  *   - `("knowledge" / seg / seg).caseClass`  → "/knowledge/<seg>/<seg>"
  *
  * Important pairing: every top-level path here MUST also be listed in
  * `server/HttpApp.scala`'s `staticRoutes`. Without that, a hard reload
  * of (say) `/knowledge/foo/bar` returns 404 because the server only
  * serves index.html for paths it recognises. (See the Onboarding
  * "Local Development" chapter, foot-gun #3.)
  *
  * To add a route:
  *   1. Add a case to `Page` (e.g. `case Settings`).
  *   2. Add a rule below.
  *   3. Add the path to `staticRoutes` in `HttpApp.scala`.
  */
object Router:

  private val config: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl.*

    // Path matchers — `string("[^/]+")` consumes a single path segment
    // (anything but `/`). Used for {book} and {chapter} dynamic segments.
    val seg = string("[^/]+")

    // The | operator composes route rules. Each rule is "pattern ~> render".
    // `trimSlashes` normalises trailing/duplicate slashes before matching.
    val rules =
      trimSlashes
        | staticRoute(root, Page.Home) ~> render(HomePage.Component())
        | staticRoute("knowledge", Page.KnowledgeIndex) ~> render(KnowledgeIndexPage.Component())
        // /knowledge/{book}/{chapter} — the chapter reader.
        | dynamicRouteCT(("knowledge" / seg / seg).caseClass[Page.Chapter]) ~>
            dynRender((p: Page.Chapter) => ChapterPage.Component(ChapterPage.Props(p.book, p.chapter)))
        // /knowledge/{book} — redirect to the book's first chapter (handled
        // imperatively inside BookRedirectPage; the router treats it as a
        // normal page and the page does the navigation on mount).
        | dynamicRouteCT(("knowledge" / seg).caseClass[Page.BookRedirect]) ~>
            dynRender((p: Page.BookRedirect) => BookRedirectPage.Component(p.book))
        | staticRoute("blogs", Page.Blogs) ~> render(BlogsPlaceholderPage.Component())
        | dynamicRouteCT(("blogs" / seg).caseClass[Page.BlogPost]) ~>
            dynRender((_: Page.BlogPost) => BlogsPlaceholderPage.Component())
        | staticRoute("demo", Page.Demo) ~> render(DemoPage.Component())

    rules
      // Unknown path → home (replace, not push, so the bad URL doesn't
      // pollute the back-button history).
      .notFound(redirectToPage(Page.Home)(SetRouteVia.HistoryReplace))
      // Layout wraps every rendered page with Header + outlet + Footer.
      // The `ctl` (router controller) is passed down so links can use
      // `ctl.setRouteEH(Page.X)` instead of raw <a href>.
      .renderWith((ctl, res) => Layout.Component(Layout.Props(ctl, res)))
  }

  /** A `Router` component already mounted at the page's origin, ready to drop
    * into `ReactDOMClient.createRoot(...).render(...)`. Rendered exactly once
    * by [[Main]].
    */
  val Component = japgolly.scalajs.react.extra.router.Router(BaseUrl.fromWindowOrigin_/, config)
