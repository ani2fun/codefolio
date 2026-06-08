package codefolio.client

import codefolio.client.pages.HomePage
import japgolly.scalajs.react.extra.router.*

/**
 * scalajs-react Router using HTML5 history (clean URLs).
 *
 * The portfolio is a single-page site: `/` renders the [[codefolio.client.pages.HomePage]] and every other
 * path redirects home. In-page `#section` anchors (Work / About / Experience / …) are stripped from the
 * matched path so the router ignores the fragment while the address bar keeps it for section scrolling.
 */
object Router:

  private def stripFragment(path: Path): Path =
    Path(path.value.takeWhile(_ != '#'))

  private val config: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl.*

    val rules =
      trimSlashes
        | staticRoute(root, Page.Home) ~> render(HomePage.Component())

    rules
      // The router parses from `window.location.href`, so the path includes any in-page fragment. Ignore
      // the fragment for matching while leaving the address bar untouched for section scrolling.
      .modPath(identity, path => Some(stripFragment(path)))
      // Unknown path → home (replace, not push, so the bad URL doesn't pollute back-button history).
      .notFound(redirectToPage(Page.Home)(SetRouteVia.HistoryReplace))
      // Layout wraps the page with Header + outlet + Footer; `ctl` is passed down for SPA-internal links.
      .renderWith((ctl, res) => Layout.Component(Layout.Props(ctl, res)))
  }

  /**
   * A `Router` component already mounted at the page's origin, ready to drop into
   * `ReactDOMClient.createRoot(...).render(...)`. Rendered exactly once by [[Main]].
   */
  val Component = japgolly.scalajs.react.extra.router.Router(BaseUrl.fromWindowOrigin_/, config)
