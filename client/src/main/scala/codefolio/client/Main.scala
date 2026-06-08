package codefolio.client

import japgolly.scalajs.react.ReactDOMClient
import org.scalajs.dom

/**
 * SPA bootstrap.
 *
 * Vite/Scala.js calls `Main.main(Array.empty)` once when the bundle loads. We grab `<div id="root">` (defined
 * in `client/index.html`) and create a single React root that renders the [[Router.Component]] — routing,
 * page lookup, and layout for the single-page portfolio.
 *
 * Look at this file second when onboarding (after `Router.scala`); it's intentionally tiny so the routing
 * surface stays the obvious entry.
 */
object Main:

  def main(args: Array[String]): Unit =
    val container = dom.document.getElementById("root")
    // Defensive: `#root` might not exist in test/SSR-like scenarios; index.html guarantees it in normal use.
    if container != null then
      val root = ReactDOMClient.createRoot(container)
      root.render(Router.Component())
