package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.RunRequest
import codefolio.shared.viz.VizGraph.given
import codefolio.shared.viz.{HeapToGraph, VizGraph}
import io.circe.syntax.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * The Visualise modal (ADR-0018). Opened from a runnable Python code block's "Visualise" button, it traces
 * the *current editor content* with [[PythonTracer]], adapts the heap trace into a [[VizGraph]] with
 * `HeapToGraph`, and hands the JSON to the standalone D3 renderer (`@d3/index`). Code (left, current line
 * highlighted) and the diagram (right) step together — the D3 renderer reports its step index back via an
 * `onStep` callback that drives the code pane.
 *
 * Chrome — dark backdrop, body-scroll lock, `Esc`/`+`/`-`/`0` shortcuts, zoom — mirrors [[DiagramZoom]]. The
 * overlay renders through a React portal into `document.body` so its `position: fixed` resolves against the
 * viewport regardless of where the code block sits in the chapter. The parent owns `isOpen`; this component
 * is always mounted and renders nothing when closed.
 */
object VisualiseModal:

  @js.native @JSImport("@markdown/runtime", "highlightWithPrism")
  private def highlightWithPrism(code: String, lang: String): String = js.native

  // The pure-TypeScript + D3 renderer — the only Scala↔D3 interop point. `onStep` fires with the step index
  // whenever the widget's stepper advances, so the modal's code pane stays in sync with the diagram.
  @js.native @JSImport("@d3/index", "renderWidget")
  private def renderWidget(
      containerId: String,
      jsonStr: String,
      onStep: js.Function1[Int, Unit]
  ): Unit = js.native

  // React portal — renders the overlay as a direct child of <body> so the `position: fixed` chrome resolves
  // against the viewport, escaping any ancestor that establishes a containing block for fixed descendants
  // (e.g. an off-screen `content-visibility: auto` section deep in a long chapter).
  @js.native @JSImport("react-dom", "createPortal")
  private def reactCreatePortal(
      child: japgolly.scalajs.react.facade.React.Node,
      container: dom.Node
  ): japgolly.scalajs.react.facade.React.Node = js.native

  private def portal(content: VdomElement): VdomNode =
    VdomNode(reactCreatePortal(content.rawElement, dom.document.body))

  /**
   * @param isOpen
   *   whether the modal is shown — owned by the parent code block
   * @param onClose
   *   flips the parent's open state back to closed
   * @param pythonSource
   *   the live code-editor content to trace
   * @param vizHint
   *   the fence's `viz=` value — the layout hint (e.g. `binary-tree`)
   * @param vizRoot
   *   the optional `viz-root=` variable naming the structure root
   * @param title
   *   shown in the toolbar title strip and on the diagram
   */
  final case class Props(
      isOpen: Boolean,
      onClose: Callback,
      pythonSource: String,
      vizHint: String,
      vizRoot: Option[String],
      title: String
  )

  // Unique, stable host-div id per renderWidget mount.
  private var hostSeq: Int = 0

  private def nextHostId(): String =
    hostSeq += 1
    s"viz-modal-host-$hostSeq"

  sealed private trait Phase
  private case object Idle                                    extends Phase
  private case object Tracing                                 extends Phase
  final private case class Failed(message: String)            extends Phase
  final private case class Ready(viz: VizGraph, json: String) extends Phase

  private val MinZoom = 0.5
  private val MaxZoom = 4.0
  private val Step    = 0.25

  private given ExecutionContext = JSExecutionContext.queue

  /** Run the trace → adapt pipeline against `props.pythonSource`, pushing phases through `setPhase`. */
  private def runTrace(props: Props, setPhase: Phase => Unit): Unit =
    setPhase(Tracing)
    val wrapped = PythonTracer.wrap(props.pythonSource)
    ApiClient.runCode(RunRequest(language = "python", source = wrapped, stdin = None)).onComplete {
      case Success(resp) =>
        val r      = resp.result
        val parsed = PythonTracer.parse(Option(r.stdout).getOrElse(""))
        HeapToGraph.adapt(
          parsed.trace,
          props.pythonSource,
          props.vizHint,
          props.vizRoot,
          props.title
        ) match
          case Right(vg) => setPhase(Ready(vg, vg.asJson.noSpaces))
          case Left(msg) =>
            // If the program never traced AND the run itself failed, the run error is the real cause.
            val runErr =
              if parsed.trace.steps.isEmpty && r.statusId != 3 then
                Seq(
                  Option(r.statusDescription).filter(_.nonEmpty),
                  Option(r.compileOutput).filter(_.nonEmpty).map(s => s"compile: $s"),
                  Option(r.stderr).filter(_.nonEmpty).map(s => s"stderr: $s")
                ).flatten.mkString(" — ")
              else ""
            setPhase(Failed(if runErr.nonEmpty then runErr else msg))
      case Failure(t) =>
        setPhase(Failed(Option(t.getMessage).getOrElse("network error")))
    }

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState[Phase](Idle)
      .useState(0)                                   // step index — driven by the D3 widget's onStep
      .useState(1.0)                                 // zoom
      .useRefBy(_ => js.Array[js.Function0[Unit]]()) // keydown + scroll-lock teardown
      .useRefBy(_ => nextHostId())
      // ─── open/close: chrome (scroll-lock + keyboard) and kicking the trace ────────────────────────────
      .useEffectWithDepsBy((props, _, _, _, _, _) => props.isOpen) {
        (props, phaseS, stepS, zoomS, cleanupRef, _) => isOpen =>
          val tearDown: Callback = Callback {
            val arr = cleanupRef.value
            for i <- 0 until arr.length do arr(i)()
            cleanupRef.value = js.Array()
          }

          val install: Callback =
            if !isOpen then phaseS.setState(Idle) >> zoomS.setState(1.0) >> stepS.setState(0)
            else
              Callback {
                val previousOverflow = dom.document.body.style.overflow
                dom.document.body.style.overflow = "hidden"

                val onKey: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
                  e.key match
                    case "Escape" =>
                      e.preventDefault()
                      props.onClose.runNow()
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
                runTrace(props, p => phaseS.setState(p).runNow())
                ()
              }

          tearDown >> install
      }
      // ─── hand the VizGraph JSON to the D3 renderer once the trace is Ready ────────────────────────────
      .useEffectWithDepsBy { (_, phaseS, _, _, _, _) =>
        phaseS.value match
          case Ready(_, json) => json
          case _              => ""
      } { (_, _, stepS, _, _, hostId) => json =>
        Callback {
          if json.nonEmpty then
            val onStep: js.Function1[Int, Unit] = (i: Int) => stepS.setState(i).runNow()
            renderWidget(hostId.value, json, onStep)
        }
      }
      .render { (props, phaseS, stepS, zoomS, _, hostId) =>
        if !props.isOpen then EmptyVdom
        else
          val zoom                = zoomS.value
          val zoomIn: Callback    = zoomS.modState(z => math.min(MaxZoom, z + Step))
          val zoomOut: Callback   = zoomS.modState(z => math.max(MinZoom, z - Step))
          val zoomReset: Callback = zoomS.setState(1.0)
          val reTrace: Callback   = Callback(runTrace(props, p => phaseS.setState(p).runNow()))

          val modalRoot: VdomElement =
            <.div(
              ^.role       := "dialog",
              ^.aria.modal := true,
              ^.aria.label := "Visualise code",
              ^.className  := "viz-modal not-prose",
              ^.onClick --> props.onClose,
              <.div(
                ^.className := "viz-modal__toolbar",
                ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                <.div(
                  ^.className := "viz-modal__title",
                  <.span(^.className := "viz-modal__title-eyebrow", "VISUALISING"),
                  <.span(^.className := "viz-modal__title-name", props.title)
                ),
                <.button(
                  ^.tpe        := "button",
                  ^.className  := "viz-modal__button",
                  ^.aria.label := "Re-trace",
                  ^.onClick --> reTrace,
                  LucideIcons.RotateCcw(LucideIcons.withClass("viz-modal__button-icon")),
                  "Re-trace"
                ),
                <.button(
                  ^.tpe        := "button",
                  ^.className  := "viz-modal__button",
                  ^.aria.label := "Zoom out",
                  ^.onClick --> zoomOut,
                  LucideIcons.ZoomOut(LucideIcons.withClass("viz-modal__button-icon"))
                ),
                <.span(^.className := "viz-modal__zoom-readout", s"${math.round(zoom * 100).toInt}%"),
                <.button(
                  ^.tpe        := "button",
                  ^.className  := "viz-modal__button",
                  ^.aria.label := "Zoom in",
                  ^.onClick --> zoomIn,
                  LucideIcons.ZoomIn(LucideIcons.withClass("viz-modal__button-icon"))
                ),
                <.button(
                  ^.tpe        := "button",
                  ^.className  := "viz-modal__button",
                  ^.aria.label := "Reset zoom",
                  ^.onClick --> zoomReset,
                  LucideIcons.Maximize2(LucideIcons.withClass("viz-modal__button-icon"))
                ),
                <.button(
                  ^.tpe        := "button",
                  ^.className  := "viz-modal__button",
                  ^.aria.label := "Close",
                  ^.onClick --> props.onClose,
                  LucideIcons.X(LucideIcons.withClass("viz-modal__button-icon"))
                )
              ),
              <.div(
                ^.className := "viz-modal__viewport",
                <.div(
                  ^.className := "viz-modal__card",
                  ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                  ^.style := js.Dynamic
                    .literal(width = s"${85.0 * zoom}vw", height = s"${78.0 * zoom}vh")
                    .asInstanceOf[js.Object],
                  renderBody(props, phaseS.value, hostId.value, stepS.value)
                )
              )
            )

          portal(modalRoot)
      }

  private def renderBody(props: Props, phase: Phase, hostId: String, step: Int): VdomNode =
    phase match
      case Idle | Tracing =>
        <.div(
          ^.className := "viz-modal__status",
          LucideIcons.Loader2(LucideIcons.withClass("viz-modal__status-icon")),
          <.span("Tracing the code…")
        )
      case Failed(message) =>
        <.div(
          ^.className := "viz-modal__status viz-modal__status--error",
          <.p(^.className   := "viz-modal__status-title", "Couldn't visualise this code"),
          <.pre(^.className := "viz-modal__status-detail", message)
        )
      case Ready(viz, _) =>
        val currentLine = viz.steps.lift(step).map(_.line)
        <.div(
          ^.className := "viz-modal__panes",
          <.div(
            ^.className := "viz-modal__code",
            renderSource(props.pythonSource, currentLine)
          ),
          <.div(
            ^.className := "viz-modal__diagram",
            <.div(^.id := hostId, ^.className := "viz-modal__host")
          )
        )

  private def renderSource(source: String, currentLine: Option[Int]): VdomElement =
    val lines = source.split("\n", -1)
    <.pre(
      ^.className := "viz-modal__source not-prose",
      lines.zipWithIndex.toVdomArray { case (line, i) =>
        val lineNo = i + 1
        val cls =
          if currentLine.contains(lineNo) then "viz-modal__line viz-modal__line--current"
          else "viz-modal__line"
        <.span(
          ^.key       := lineNo.toString,
          ^.className := cls,
          <.span(^.className := "viz-modal__line-number", f"$lineNo%4d"),
          <.code(
            ^.className               := "viz-modal__line-code",
            ^.dangerouslySetInnerHtml := highlightWithPrism(if line.isEmpty then " " else line, "python")
          )
        )
      }
    )
