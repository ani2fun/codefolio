package codefolio.client

import codefolio.client.pages.*
import codefolio.shared.AppRoutes
import japgolly.scalajs.react.extra.router.*

/**
 * scalajs-react Router using HTML5 history (clean URLs).
 *
 * Routes are declared bottom-up: each `staticRoute` / `dynamicRouteCT` line maps a **URL pattern** to a
 * `Page` ADT case to a **component to render**. The `Page` cases come from `client/Page.scala` (a sealed
 * trait — adding a new route means adding a case there too).
 *
 * Pattern matchers use the router DSL:
 *   - `root` → "/"
 *   - `staticRoute("cortex", ...)` → "/cortex"
 *   - `("cortex" / seg).caseClass[...]` → "/cortex/<one-segment>"
 *   - `("cortex" / seg / seg).caseClass` → "/cortex/<seg>/<seg>"
 *
 * Important pairing: every top-level path here MUST also be listed in `server/HttpApp.scala`'s
 * `staticRoutes`. Without that, a hard reload of (say) `/cortex/foo/bar` returns 404 because the server only
 * serves index.html for paths it recognises. (See the Onboarding "Local Development" chapter, foot-gun #3.)
 *
 * To add a route:
 *   1. Add a case to `Page` (e.g. `case Settings`). 2. Add a rule below. 3. Add the path to `staticRoutes` in
 *      `HttpApp.scala`.
 */
object Router:

  private def stripFragment(path: Path): Path =
    Path(path.value.takeWhile(_ != '#'))

  private val config: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl.*

    // Path matchers — `[^/?#]+` consumes a single path segment, stopping at
    // the first `/`, `?`, or `#`. The `#` exclusion matters: without it, an
    // in-page anchor click like `<a href="#section">` makes the router
    // capture `chapter#section` as the chapter slug, which then 404s on the
    // backend (slug regex rejects `#`). Same for query-string segments.
    val seg = string("[^/?#]+")

    // The | operator composes route rules. Each rule is "pattern ~> render".
    // `trimSlashes` normalises trailing/duplicate slashes before matching.
    val rules =
      trimSlashes
        | staticRoute(root, Page.Home) ~> render(HomePage.Component())
        | staticRoute(AppRoutes.Cortex, Page.CortexIndex) ~> render(CortexIndexPage.Component())
        // /cortex/{book}/{chapter} — the chapter reader.
        | dynamicRouteCT((AppRoutes.Cortex / seg / seg).caseClass[Page.Chapter]) ~>
        dynRender((p: Page.Chapter) => ChapterPage.Component(ChapterPage.Props(p.book, p.chapter)))
        // /cortex/{book} — redirect to the book's first chapter (handled
        // imperatively inside BookRedirectPage; the router treats it as a
        // normal page and the page does the navigation on mount).
        | dynamicRouteCT((AppRoutes.Cortex / seg).caseClass[Page.BookRedirect]) ~>
        dynRender((p: Page.BookRedirect) => BookRedirectPage.Component(p.book))
        | staticRoute(AppRoutes.Blogs, Page.Blogs) ~> render(BlogIndexPage.Component())
        | dynamicRouteCT((AppRoutes.Blogs / seg).caseClass[Page.BlogPost]) ~>
        dynRender((p: Page.BlogPost) => BlogPostPage.Component(BlogPostPage.Props(p.slug)))
        | staticRoute(AppRoutes.Demo, Page.Demo) ~> render(DemoPage.Component())

    rules
      // The router library parses from `window.location.href`, so the path it
      // sees includes any in-page fragment. Route matching should ignore the
      // fragment while leaving the address bar untouched for section scrolling.
      .modPath(identity, path => Some(stripFragment(path)))
      // Unknown path → home (replace, not push, so the bad URL doesn't
      // pollute the back-button history).
      .notFound(redirectToPage(Page.Home)(SetRouteVia.HistoryReplace))
      // Layout wraps every rendered page with Header + outlet + Footer.
      // The `ctl` (router controller) is passed down so links can use
      // `ctl.setRouteEH(Page.X)` instead of raw <a href>.
      .renderWith((ctl, res) => Layout.Component(Layout.Props(ctl, res)))
  }

  /**
   * A `Router` component already mounted at the page's origin, ready to drop into
   * `ReactDOMClient.createRoot(...).render(...)`. Rendered exactly once by [[Main]].
   */
  val Component = japgolly.scalajs.react.extra.router.Router(BaseUrl.fromWindowOrigin_/, config)
