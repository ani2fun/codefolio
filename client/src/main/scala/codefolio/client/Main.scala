package codefolio.client

import japgolly.scalajs.react.ReactDOMClient
import org.scalajs.dom

/**
 * SPA bootstrap.
 *
 * Vite/Scala.js calls `Main.main(Array.empty)` once when the bundle loads. We grab `<div id="root">` (defined
 * in `client/index.html`) and create a single React root that owns the entire SPA tree. Everything else —
 * routing, page lookup, layout — lives behind [[Router.Component]].
 *
 * Look at this file second when onboarding (after `Router.scala`); it's intentionally tiny so the routing
 * surface stays the obvious entry.
 */
object Main:

  def main(args: Array[String]): Unit =
    val container = dom.document.getElementById("root")
    // Defensive: in tests / SSR-like scenarios `#root` might not exist yet.
    // In normal operation the index.html guarantees it, so this is just a
    // belt-and-braces noop rather than an error path worth surfacing.
    if container != null then
      val root = ReactDOMClient.createRoot(container)
      root.render(Router.Component())
