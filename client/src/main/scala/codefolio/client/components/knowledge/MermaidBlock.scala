package codefolio.client.components.knowledge

import codefolio.client.components.Theme
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/** Renders a single mermaid diagram. The actual SVG generation lives in
  * `client/src/markdown/runtime.ts#renderMermaidInto`, which dynamic-
  * imports the mermaid library on first call.
  *
  * Re-renders when `props.source` changes or when the theme flips between
  * light and dark — mermaid bakes the resolved theme into the SVG.
  */
object MermaidBlock:

  @js.native @JSImport("@markdown/runtime", "renderMermaidInto")
  private def renderMermaidInto(
      target: dom.HTMLElement,
      source: String,
      dark: Boolean
  ): js.Promise[Unit] = js.native

  final case class Props(source: String)

  private given ExecutionContext = JSExecutionContext.queue

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(Option.empty[String])  // error message (None on success)
      .useState(Theme.current)          // mode tracked locally so we re-render
      .useRefToVdom[dom.html.Div]
      // CRITICAL: depend on `(source, mode)` so the effect only fires when
      // the diagram source or theme actually changes. The earlier version
      // used plain `useEffectBy` (no deps), which fires on EVERY render —
      // and because the success branch calls `setState(None)`, that itself
      // schedules another render → effect fires again → another
      // `mermaid.render` call → infinite loop. Each `mermaid.render` bumps
      // mermaid's internal id counter and leaves a `<div id="dmermaid-N">`
      // scratch element in <body>, so the loop manifests as a runaway id
      // counter and a body full of leaked divs.
      // Deps are (source, isDark Boolean) — `Theme.Mode` doesn't have a
      // Reusability instance, so we cast it to a Boolean here. That's all
      // mermaid actually cares about anyway.
      .useEffectWithDepsBy((p, _, modeS, _) => (p.source, modeS.value == Theme.Mode.Dark)) {
        (_, errS, _, ref) => (source, isDark) =>
          ref.foreach { el =>
            renderMermaidInto(el.asInstanceOf[dom.HTMLElement], source, isDark).toFuture
              .onComplete {
                case Success(_) =>
                  // Only setState if we were previously in an error state;
                  // an unconditional setState(None) would be a no-op for
                  // value but still enqueue a render.
                  if errS.value.nonEmpty then errS.setState(None).runNow()
                case Failure(t) =>
                  errS.setState(Some(Option(t.getMessage).getOrElse("Diagram failed"))).runNow()
              }
          }
      }
      .useEffectOnMountBy { (_, _, modeS, _) =>
        // Watch the <html class="dark"> attribute via a MutationObserver and
        // bump local mode state when it flips. Avoids spamming re-renders;
        // only fires on actual class changes.
        Callback {
          val obs = new dom.MutationObserver({ (_, _) =>
            val now = Theme.current
            if now != modeS.value then modeS.setState(now).runNow()
          })
          obs.observe(
            dom.document.documentElement,
            new dom.MutationObserverInit {
              attributes = true
              attributeFilter = js.Array("class")
            }
          )
          // No teardown — we accept the observer running for the page's
          // lifetime; it's a no-op when no MermaidBlock is mounted.
        }
      }
      .render { (_, errS, _, ref) =>
        errS.value match
          case Some(msg) =>
            <.div(
              ^.className :=
                "my-6 rounded-lg border border-destructive/40 bg-destructive/10 p-4 " +
                  "text-sm text-destructive",
              <.p(^.className := "font-semibold mb-2", "Mermaid render error"),
              <.pre(^.className := "whitespace-pre-wrap text-xs", msg)
            )
          case None =>
            <.div.withRef(ref)(
              ^.className :=
                "my-6 flex justify-center overflow-x-auto rounded-lg border border-border " +
                  "bg-card p-4 [&_svg]:max-w-full",
              ^.aria.label := "Diagram"
            )
      }
