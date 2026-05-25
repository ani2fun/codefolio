package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.RunRequest
import codefolio.shared.viz.VizGraph.given
import codefolio.shared.viz.{HeapToGraph, VizCases}
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
 * the *current editor content* with [[PythonTracer]], adapts the heap trace into a [[VizCases]] with
 * `HeapToGraph`, and hands the JSON to the standalone D3 renderer (`@d3/index`). Code (left, current line
 * highlighted) and the diagram (right) step together — the D3 renderer reports its step index back via an
 * `onStep` callback that drives the code pane.
 *
 * A traced `main` usually runs several test cases; `HeapToGraph` segments the trace into one animation per
 * case and a "Case N / M" strip under the toolbar switches between them — each case replays independently
 * from step 0.
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
  // whenever the widget's stepper advances, so the modal's code pane stays in sync; `caseIndex` selects
  // which test case to draw — the modal re-invokes this when the Case selector switches.
  @js.native @JSImport("@d3/index", "renderWidget")
  private def renderWidget(
      containerId: String,
      jsonStr: String,
      onStep: js.Function1[Int, Unit],
      caseIndex: Int
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
   * @param vizCase
   *   the optional `viz-case=` override forcing the number of test cases the trace segments into
   * @param title
   *   shown in the toolbar title strip and on the diagram
   */
  final case class Props(
      isOpen: Boolean,
      onClose: Callback,
      pythonSource: String,
      vizHint: String,
      vizRoot: Option[String],
      vizCase: Option[Int],
      title: String
  )

  // Unique, stable host-div id per renderWidget mount.
  private var hostSeq: Int = 0

  private def nextHostId(): String =
    hostSeq += 1
    s"viz-modal-host-$hostSeq"

  sealed private trait Phase
  private case object Idle                                      extends Phase
  private case object Tracing                                   extends Phase
  final private case class Failed(message: String)              extends Phase
  final private case class Ready(cases: VizCases, json: String) extends Phase

  private val MinZoom = 0.5
  private val MaxZoom = 4.0
  private val Step    = 0.25

  private given ExecutionContext = JSExecutionContext.queue

  // Per-session memo of completed traces, keyed on every input that determines the VizCases. Re-opening a
  // modal whose editor content is unchanged is then instant — no /api/run round-trip, no Tracing flicker.
  // Only successes are cached; the Re-trace button forces a fresh run. Cleared on page reload.
  private val traceCache =
    scala.collection.mutable.Map.empty[(String, Option[String], String, Option[Int], String), Ready]

  /**
   * Run the trace → adapt pipeline against `props.pythonSource`, pushing phases through `setPhase`. A cache
   * hit short-circuits to the stored `Ready`; `force` (the Re-trace button) skips the cache and refreshes it.
   */
  private def runTrace(props: Props, setPhase: Phase => Unit, force: Boolean = false): Unit =
    val key = (props.pythonSource, props.vizRoot, props.vizHint, props.vizCase, props.title)
    (if force then None else traceCache.get(key)) match
      case Some(ready) => setPhase(ready)
      case None =>
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
              props.vizCase,
              props.title
            ) match
              case Right(vc) =>
                val ready = Ready(vc, vc.asJson.noSpaces)
                traceCache.update(key, ready)
                setPhase(ready)
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
      .useState(0)                                   // case index — driven by the Case selector
      .useState(1.0)                                 // zoom
      .useRefBy(_ => js.Array[js.Function0[Unit]]()) // keydown + scroll-lock teardown
      .useRefBy(_ => nextHostId())
      .useState(false)      // help popover open/closed
      .useRefBy(_ => false) // mirrors help-open so the keydown handler reads the live value
      // ─── open/close: chrome (scroll-lock + keyboard) and kicking the trace ────────────────────────────
      .useEffectWithDepsBy((props, _, _, _, _, _, _, _, _) => props.isOpen) {
        (props, phaseS, stepS, caseS, zoomS, cleanupRef, _, helpOpenS, helpOpenRef) => isOpen =>
          val tearDown: Callback = Callback {
            val arr = cleanupRef.value
            for i <- 0 until arr.length do arr(i)()
            cleanupRef.value = js.Array()
          }

          val install: Callback =
            if !isOpen then
              phaseS.setState(Idle) >> zoomS.setState(1.0) >> stepS.setState(0) >>
                caseS.setState(0) >> helpOpenS.setState(false) >>
                Callback { helpOpenRef.value = false }
            else
              Callback {
                val previousOverflow = dom.document.body.style.overflow
                dom.document.body.style.overflow = "hidden"

                val onKey: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
                  e.key match
                    case "Escape" =>
                      e.preventDefault()
                      // Layered dismiss: the help popover closes before the modal does.
                      if helpOpenRef.value then
                        helpOpenRef.value = false
                        helpOpenS.setState(false).runNow()
                      else props.onClose.runNow()
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
      // ─── hand the VizCases JSON to the D3 renderer once the trace is Ready ────────────────────────────
      // Keyed on (json, caseIndex): a new trace OR a Case-selector switch re-invokes renderWidget, which
      // clears the host and redraws the chosen case from step 0 — per-case replay.
      .useEffectWithDepsBy { (_, phaseS, _, caseS, _, _, _, _, _) =>
        phaseS.value match
          case Ready(_, json) => (json, caseS.value)
          case _              => ("", 0)
      } { (_, _, stepS, _, _, _, hostId, _, _) => (json, caseIdx) =>
        Callback {
          if json.nonEmpty then
            val onStep: js.Function1[Int, Unit] = (i: Int) => stepS.setState(i).runNow()
            renderWidget(hostId.value, json, onStep, caseIdx)
        }
      }
      // ─── scroll the code pane so the highlighted traced line is always in view ────────────────────────
      // Keyed on the current case + step's source line: each step (Next / Prev / Play, a click in the
      // diagram, or a Case switch) scrolls the `--current` line element to the centre of the code pane.
      .useEffectWithDepsBy { (_, phaseS, stepS, caseS, _, _, _, _, _) =>
        phaseS.value match
          case Ready(cases, _) =>
            cases.cases.lift(caseS.value).flatMap(_.steps.lift(stepS.value)).fold("")(_.line.toString)
          case _ => ""
      } { (_, _, _, _, _, _, _, _, _) => _ =>
        Callback {
          val line = dom.document.querySelector(".viz-modal__code .viz-modal__line--current")
          if line != null then
            val _ = line
              .asInstanceOf[js.Dynamic]
              .scrollIntoView(
                js.Dynamic.literal(block = "center", behavior = "smooth", inline = "nearest")
              )
        }
      }
      .render { (props, phaseS, stepS, caseS, zoomS, _, hostId, helpOpenS, helpOpenRef) =>
        if !props.isOpen then EmptyVdom
        else
          val zoom                = zoomS.value
          val zoomIn: Callback    = zoomS.modState(z => math.min(MaxZoom, z + Step))
          val zoomOut: Callback   = zoomS.modState(z => math.max(MinZoom, z - Step))
          val zoomReset: Callback = zoomS.setState(1.0)
          val reTrace: Callback =
            caseS.setState(0) >> stepS.setState(0) >>
              Callback(runTrace(props, p => phaseS.setState(p).runNow(), force = true))
          val selectCase: Int => Callback = i => caseS.setState(i) >> stepS.setState(0)

          // The help popover. `helpOpenRef` mirrors the state so the keydown handler — installed once
          // per open — reads the live value for its layered-dismiss check.
          val setHelp: Boolean => Callback = open =>
            Callback { helpOpenRef.value = open } >> helpOpenS.setState(open)

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
                  <.span(^.className := "viz-modal__title-name", props.title),
                  renderHelp(helpOpenS.value, setHelp)
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
              renderCaseStrip(phaseS.value, caseS.value, selectCase),
              <.div(
                ^.className := "viz-modal__viewport",
                <.div(
                  ^.className := "viz-modal__card",
                  ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                  ^.style := js.Dynamic
                    .literal(width = s"${85.0 * zoom}vw", height = s"${78.0 * zoom}vh")
                    .asInstanceOf[js.Object],
                  renderBody(props, phaseS.value, hostId.value, stepS.value, caseS.value)
                )
              )
            )

          portal(modalRoot)
      }

  /**
   * The "Case N / M" selector — a strip of numbered buttons under the toolbar, shown only when the traced
   * `main` ran more than one test case (so `HeapToGraph` segmented the trace). Picking a case replays its
   * animation independently, from step 0.
   */
  private def renderCaseStrip(phase: Phase, caseIndex: Int, selectCase: Int => Callback): VdomNode =
    phase match
      case Ready(cases, _) if cases.cases.sizeIs > 1 =>
        <.div(
          ^.className := "viz-modal__cases",
          ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
          <.span(^.className := "viz-modal__cases-label", "Test case"),
          <.div(
            ^.className := "viz-modal__cases-list",
            cases.cases.zipWithIndex.toVdomArray { case (_, i) =>
              <.button(
                ^.key        := i.toString,
                ^.tpe        := "button",
                ^.aria.label := s"Test case ${i + 1}",
                ^.className :=
                  (if i == caseIndex then "viz-modal__case-button viz-modal__case-button--active"
                   else "viz-modal__case-button"),
                ^.onClick --> selectCase(i),
                (i + 1).toString
              )
            }
          )
        )
      case _ => EmptyVdom

  /**
   * The "How it works" help — an `(i)` button in the title strip and the dismissible popover it toggles (a
   * popover, not a hover-tooltip — there is too much to say). A full-viewport transparent backdrop catches an
   * outside click; the `×`, the `(i)` toggle, and `Esc` (see the keydown handler) all close it. The popover
   * explains that the diagram is a replay of a real trace, how to step it, the multi-case selector, and the
   * colour legend.
   */
  private def renderHelp(open: Boolean, setHelp: Boolean => Callback): VdomElement =
    <.div(
      ^.className := "viz-modal__help",
      <.button(
        ^.tpe                     := "button",
        ^.className               := "viz-modal__help-button",
        ^.aria.label              := "How Visualise works",
        ^.title                   := "How it works",
        VdomAttr("aria-expanded") := (if open then "true" else "false"),
        VdomAttr("aria-haspopup") := "dialog",
        ^.onClick --> setHelp(!open),
        LucideIcons.Info(LucideIcons.withClass("viz-modal__help-button-icon"))
      ),
      if open then
        TagMod(
          <.div(^.className := "viz-modal__help-backdrop", ^.onClick --> setHelp(false)),
          helpPopover(setHelp(false))
        )
      else EmptyVdom
    )

  private def helpPopover(close: Callback): VdomElement =
    <.div(
      ^.className  := "viz-modal__help-popover",
      ^.role       := "dialog",
      ^.aria.label := "How Visualise works",
      <.div(
        ^.className := "viz-modal__help-head",
        <.span(^.className := "viz-modal__help-heading", "How Visualise works"),
        <.button(
          ^.tpe        := "button",
          ^.className  := "viz-modal__help-close",
          ^.aria.label := "Close",
          ^.onClick --> close,
          LucideIcons.X(LucideIcons.withClass("viz-modal__help-close-icon"))
        )
      ),
      helpSection(
        "What this is",
        "We ran your Python and recorded the data structure after every line. This is a replay of " +
          "the real run — not a simulation."
      ),
      helpSection(
        "Navigating",
        "Prev and Next move one line at a time, Play auto-advances, Reset returns to the start. " +
          "The code on the left highlights the line being executed."
      ),
      helpSection(
        "Multiple test cases",
        "When the program runs several cases, a Test case strip appears under the toolbar. Each " +
          "case replays independently from its first step."
      ),
      <.div(
        ^.className := "viz-modal__help-section",
        <.span(^.className := "viz-modal__help-section-title", "Colours"),
        <.ul(
          ^.className := "viz-modal__help-legend",
          helpLegendItem("viz-modal__help-swatch--cursor", "", "a variable points here"),
          helpLegendItem("viz-modal__help-swatch--new", "", "new this step"),
          helpLegendItem("viz-modal__help-swatch--pointer", "▾", "a named pointer")
        ),
        <.p(
          ^.className := "viz-modal__help-note",
          "An amber ring marks a value that changed; a red ring, a node removed this step."
        )
      )
    )

  private def helpSection(title: String, body: String): VdomElement =
    <.div(
      ^.className := "viz-modal__help-section",
      <.span(^.className := "viz-modal__help-section-title", title),
      <.p(^.className    := "viz-modal__help-section-body", body)
    )

  private def helpLegendItem(swatchModifier: String, glyph: String, text: String): VdomElement =
    <.li(
      ^.className := "viz-modal__help-legend-item",
      <.span(^.className := s"viz-modal__help-swatch $swatchModifier", glyph),
      <.span(text)
    )

  private def renderBody(
      props: Props,
      phase: Phase,
      hostId: String,
      step: Int,
      caseIndex: Int
  ): VdomNode =
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
      case Ready(cases, _) =>
        val currentLine = cases.cases.lift(caseIndex).flatMap(_.steps.lift(step)).map(_.line)
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
