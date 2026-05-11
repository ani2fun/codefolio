package codefolio.client.components.cortex

import codefolio.client.components.Theme
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * Renders a single mermaid diagram. The actual SVG generation lives in
 * `client/src/markdown/runtime.ts#renderMermaidInto`, which dynamic- imports the mermaid library on first
 * call.
 *
 * Re-renders when `props.source` changes or when the theme flips between light and dark — mermaid bakes the
 * resolved theme into the SVG.
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
      .useState(Option.empty[String])
      .useState(Theme.current)
      .useRefToVdom[dom.html.Div]
      .useEffectWithDepsBy((p, _, modeS, _) => (p.source, modeS.value == Theme.Mode.Dark)) {
        (_, errS, _, ref) => (source, isDark) =>
          ref.foreach { el =>
            renderMermaidInto(el.asInstanceOf[dom.HTMLElement], source, isDark).toFuture
              .onComplete {
                case Success(_) =>
                  if errS.value.nonEmpty then errS.setState(None).runNow()
                case Failure(t) =>
                  errS.setState(Some(Option(t.getMessage).getOrElse("Diagram failed"))).runNow()
              }
          }
      }
      .useEffectOnMountBy { (_, _, modeS, _) =>
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
        }
      }
      .render { (_, errS, _, ref) =>
        errS.value match
          case Some(msg) =>
            <.div(
              ^.className := "mermaid__error",
              <.p(^.className   := "mermaid__error-title", "Mermaid render error"),
              <.pre(^.className := "mermaid__error-message", msg)
            )
          case None =>
            <.div.withRef(ref)(
              ^.className  := "mermaid",
              ^.aria.label := "Diagram"
            )
      }
