package codefolio.client.components.cortex

import codefolio.client.components.Theme
import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * Renders a single mermaid diagram with the same fullscreen-zoom affordance as D2 diagrams.
 *
 * The mermaid SVG is generated client-side by `client/src/markdown/runtime.ts#renderMermaidInto`. Once the
 * inline SVG is in the DOM we capture its `outerHTML` on demand and re-mount it inside a modal driven by
 * `openS`/`zoomS` — same shape as [[D2Diagram]].
 *
 * Re-renders when `props.source` changes or when the theme flips — mermaid bakes the resolved theme into the
 * SVG.
 */
object MermaidBlock:

  @js.native @JSImport("@markdown/runtime", "renderMermaidInto")
  private def renderMermaidInto(
      target: dom.HTMLElement,
      source: String,
      dark: Boolean
  ): js.Promise[Unit] = js.native

  final case class Props(source: String)

  private val MinZoom = 0.5
  private val MaxZoom = 4.0
  private val Step    = 0.25

  private given ExecutionContext = JSExecutionContext.queue

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(Option.empty[String])                       // errS
      .useState(Theme.current)                              // modeS
      .useState(false)                                      // openS
      .useState(1.0)                                        // zoomS
      .useState(Option.empty[String])                       // capturedSvgS
      .useRefBy(_ => js.Array[js.Function0[Unit]]())        // cleanupRef (modal keydown listeners)
      .useRefToVdom[dom.html.Div]                           // inlineRef (where mermaid renders into)
      .useRefToVdom[dom.html.Div]                           // modalSvgRef (the modal's SVG container)
      // ─── render mermaid into the inline container ──────────────────────────────────────────────────────
      .useEffectWithDepsBy((p, _, modeS, _, _, _, _, _, _) => (p.source, modeS.value == Theme.Mode.Dark)) {
        (_, errS, _, _, _, _, _, inlineRef, _) => (source, isDark) =>
          inlineRef.foreach { el =>
            renderMermaidInto(el.asInstanceOf[dom.HTMLElement], source, isDark).toFuture
              .onComplete {
                case Success(_) =>
                  if errS.value.nonEmpty then errS.setState(None).runNow()
                case Failure(t) =>
                  errS.setState(Some(Option(t.getMessage).getOrElse("Diagram failed"))).runNow()
              }
          }
      }
      // ─── watch theme class on <html> so dark/light flips trigger re-render ──────────────────────────────
      .useEffectOnMountBy { (_, _, modeS, _, _, _, _, _, _) =>
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
      // ─── modal keyboard handling + body scroll lock ─────────────────────────────────────────────────────
      .useEffectWithDepsBy((_, _, _, openS, _, _, _, _, _) => openS.value) {
        (_, _, _, openS, zoomS, _, cleanupRef, _, _) => isOpen =>
          val tearDown: Callback = Callback {
            val arr = cleanupRef.value
            for i <- 0 until arr.length do arr(i)()
            cleanupRef.value = js.Array()
          }

          val install: Callback =
            if !isOpen then Callback.empty
            else
              Callback {
                val previousOverflow = dom.document.body.style.overflow
                dom.document.body.style.overflow = "hidden"

                val onKey: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
                  e.key match
                    case "Escape" =>
                      e.preventDefault()
                      openS.setState(false).runNow()
                      zoomS.setState(1.0).runNow()
                    case "+" | "=" =>
                      e.preventDefault()
                      zoomS.modState(z => math.min(MaxZoom, z + Step)).runNow()
                    case "-" =>
                      e.preventDefault()
                      zoomS.modState(z => math.max(MinZoom, z - Step)).runNow()
                    case "0" =>
                      e.preventDefault()
                      zoomS.setState(1.0).runNow()
                    case _ => ()

                dom.document.addEventListener("keydown", onKey)
                cleanupRef.value.push { () =>
                  dom.document.removeEventListener("keydown", onKey)
                  dom.document.body.style.overflow = previousOverflow
                  ()
                }
                ()
              }

          tearDown >> install
      }
      // ─── inside the modal, force the cloned SVG to fill its card ────────────────────────────────────────
      .useEffectWithDepsBy((_, _, _, openS, _, _, _, _, _) => openS.value) {
        (_, _, _, _, _, _, _, _, modalSvgRef) => isOpen =>
          if !isOpen then Callback.empty
          else
            modalSvgRef.foreach { wrapper =>
              val svg = wrapper.querySelector("svg")
              if svg != null then
                svg.setAttribute("preserveAspectRatio", "xMidYMid meet")
                val style = svg.asInstanceOf[js.Dynamic].style
                style.width = "100%"
                style.height = "100%"
                style.display = "block"
            }
      }
      .render { (_, errS, _, openS, zoomS, capturedSvgS, _, inlineRef, modalSvgRef) =>
        val close: Callback = openS.setState(false) >> zoomS.setState(1.0)

        val openModal: Callback =
          inlineRef.foreach { el =>
            val svg = el.querySelector("svg")
            if svg != null then
              capturedSvgS.setState(Some(svg.asInstanceOf[dom.html.Element].outerHTML)).runNow()
          } >> zoomS.setState(1.0) >> openS.setState(true)

        val zoomIn: Callback    = zoomS.modState(z => math.min(MaxZoom, z + Step))
        val zoomOut: Callback   = zoomS.modState(z => math.max(MinZoom, z - Step))
        val zoomReset: Callback = zoomS.setState(1.0)

        errS.value match
          case Some(msg) =>
            <.div(
              ^.className := "mermaid__error",
              <.p(^.className   := "mermaid__error-title", "Mermaid render error"),
              <.pre(^.className := "mermaid__error-message", msg)
            )
          case None =>
            <.div(
              <.div(
                ^.className := "mermaid group not-prose",
                <.div.withRef(inlineRef)(
                  ^.className  := "mermaid__svg",
                  ^.aria.label := "Diagram"
                ),
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> openModal,
                  ^.aria.label := "Open diagram in fullscreen",
                  ^.className  := "mermaid__zoom-button",
                  LucideIcons.Maximize2(LucideIcons.withClass("mermaid__zoom-icon")),
                  "Zoom"
                )
              ),
              if openS.value && capturedSvgS.value.isDefined then
                <.div(
                  ^.role       := "dialog",
                  ^.aria.modal := true,
                  ^.aria.label := "Diagram fullscreen view",
                  ^.className  := "diagram-modal not-prose",
                  ^.onClick --> close,
                  <.div(
                    ^.className := "diagram-modal__toolbar",
                    ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                    <.button(
                      ^.tpe := "button",
                      ^.onClick --> zoomOut,
                      ^.aria.label := "Zoom out",
                      ^.className  := "diagram-modal__button",
                      LucideIcons.ZoomOut(LucideIcons.withClass("diagram-modal__button-icon"))
                    ),
                    <.span(
                      ^.className := "diagram-modal__zoom-readout",
                      s"${math.round(zoomS.value * 100).toInt}%"
                    ),
                    <.button(
                      ^.tpe := "button",
                      ^.onClick --> zoomIn,
                      ^.aria.label := "Zoom in",
                      ^.className  := "diagram-modal__button",
                      LucideIcons.ZoomIn(LucideIcons.withClass("diagram-modal__button-icon"))
                    ),
                    <.button(
                      ^.tpe := "button",
                      ^.onClick --> zoomReset,
                      ^.aria.label := "Reset zoom",
                      ^.className  := "diagram-modal__button",
                      LucideIcons.RotateCcw(LucideIcons.withClass("diagram-modal__button-icon"))
                    ),
                    <.button(
                      ^.tpe := "button",
                      ^.onClick --> close,
                      ^.aria.label := "Close",
                      ^.className  := "diagram-modal__button",
                      LucideIcons.X(LucideIcons.withClass("diagram-modal__button-icon"))
                    )
                  ),
                  <.div(
                    ^.className := "diagram-modal__viewport",
                    <.div(
                      ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                      ^.style := js.Dynamic
                        .literal(
                          width = s"${85.0 * zoomS.value}vw",
                          height = s"${78.0 * zoomS.value}vh"
                        )
                        .asInstanceOf[js.Object],
                      ^.className := "diagram-modal__card",
                      <.div.withRef(modalSvgRef)(
                        ^.className               := "diagram-modal__svg not-prose",
                        ^.dangerouslySetInnerHtml := capturedSvgS.value.getOrElse("")
                      )
                    )
                  )
                )
              else EmptyVdom
            )
      }
