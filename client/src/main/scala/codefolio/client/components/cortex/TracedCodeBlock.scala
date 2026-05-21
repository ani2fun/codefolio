package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.RunRequest
import codefolio.shared.viz.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * Step-through visualisation of a Python source. The server-side `/api/run` is unchanged — the shared
 * [[PythonTracer]] wraps the user source in a `sys.settrace` harness, the client posts the wrapped program,
 * then [[PythonTracer.parse]] splits the captured trace out of stdout and this component renders code +
 * locals panel + step controls.
 *
 * The harness is delimited by markers so the user's real `print()` output (if any) survives the parse and
 * shows up in a "Program output" panel.
 *
 * **Collapsed by default.** The header is always visible (so the chapter shows that an interactive tracer is
 * available); the body — source, locals, caption, program output — only appears once the reader clicks
 * "Trace" (which expands and runs) or "Show" (after a previous trace). A "Hide" toggle in the header
 * re-collapses without losing trace state.
 *
 * Python is the only runtime in v1. The component renders a friendly error for non-python languages so the
 * chapter doesn't silently fail if someone copy-pastes a `java trace` fence later.
 */
object TracedCodeBlock:

  @js.native @JSImport("@markdown/runtime", "highlightWithPrism")
  private def highlightWithPrism(code: String, lang: String): String = js.native

  final case class Props(language: String, source: String)

  // ---------------------------------------------------------------------------
  // Trace data — the heap-snapshot trace comes from the shared PythonTracer.
  // ---------------------------------------------------------------------------

  /** A ready trace: the decoded heap-snapshot steps plus the user program's own stdout. */
  final private case class Trace(steps: List[HeapStep], programStdout: String)

  // ---------------------------------------------------------------------------
  // UI state machine
  // ---------------------------------------------------------------------------

  sealed private trait Phase
  private case object Idle                                                       extends Phase
  private case object Running                                                    extends Phase
  final private case class Failed(message: String)                               extends Phase
  final private case class Ready(trace: Trace, stepIndex: Int, playing: Boolean) extends Phase

  private val StepDelayMs = 700

  private given ExecutionContext = JSExecutionContext.queue

  // ---------------------------------------------------------------------------
  // Component
  // ---------------------------------------------------------------------------

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState[Phase](Idle)
      // Body is auto-hidden by default — chapter prose doesn't get a giant code+locals panel sitting under
      // every fence. The header stays visible so the affordance is discoverable.
      .useState(false)
      .useRefBy(_ => Option.empty[Int])
      // depsBy returns a primitive tuple so we don't need a `Reusability[Phase]`. The effect only cares
      // about the auto-advance loop: (playing, idx, totalSteps). Idle / Running / Failed collapse to
      // (false, -1, 0) — guaranteed to differ from any Ready trigger, so the cancel-then-install loop runs.
      .useEffectWithDepsBy { (_, phaseS, _, _) =>
        phaseS.value match
          case Ready(t, i, p) => (p, i, t.steps.length)
          case _              => (false, -1, 0)
      } { (_, phaseS, _, timeoutRef) => (playing, idx, total) =>
        Callback {
          timeoutRef.value.foreach(dom.window.clearTimeout)
          timeoutRef.value = None
          if playing then
            if idx < total - 1 then
              val id = dom.window.setTimeout(
                () =>
                  phaseS
                    .modState {
                      case Ready(t, i, true) if i < t.steps.length - 1 => Ready(t, i + 1, true)
                      case Ready(t, i, _)                              => Ready(t, i, false)
                      case other                                       => other
                    }
                    .runNow(),
                StepDelayMs.toDouble
              )
              timeoutRef.value = Some(id)
            else
              phaseS.modState {
                case Ready(t, i, _) => Ready(t, i, false)
                case other          => other
              }.runNow()
        }
      }
      .render { (props, phaseS, visibleS, _) =>
        if !isPython(props.language) then
          <.div(
            ^.className := "traced-code__error",
            <.p(^.className := "traced-code__error-title", "Traced execution: language not supported"),
            <.p(
              ^.className := "traced-code__error-message",
              s"v1 traces Python only; got '${props.language}'. The fence ` ```python trace ` is the only" +
                " supported form."
            )
          )
        else
          val runTrace: Callback =
            visibleS.setState(true) >> phaseS.setState(Running) >> Callback {
              val wrapped = PythonTracer.wrap(props.source)
              val req     = RunRequest(language = "python", source = wrapped, stdin = None)
              ApiClient.runCode(req).onComplete {
                case Success(resp) =>
                  val r      = resp.result
                  val ok     = r.statusId == 3
                  val parsed = PythonTracer.parse(Option(r.stdout).getOrElse(""))
                  val trace  = Trace(parsed.trace.steps, parsed.programStdout)
                  if !ok && trace.steps.isEmpty then
                    val errMsg =
                      Seq(
                        Option(r.statusDescription).filter(_.nonEmpty),
                        Option(r.compileOutput).filter(_.nonEmpty).map(s => s"compile: $s"),
                        Option(r.stderr).filter(_.nonEmpty).map(s => s"stderr: $s")
                      ).flatten.mkString(" — ")
                    phaseS.setState(Failed(if errMsg.nonEmpty then errMsg else "execution failed")).runNow()
                  else phaseS.setState(Ready(trace, 0, false)).runNow()
                case Failure(t) =>
                  phaseS.setState(Failed(Option(t.getMessage).getOrElse("network error"))).runNow()
              }
            }

          val visible = visibleS.value
          // Hiding while auto-playing would advance steps invisibly; pause as part of the hide step so
          // re-showing leaves the reader on a deterministic step. `visible` is captured at render time,
          // so we know whether this toggle is a hide-action (and need to pause) or a show-action (no-op).
          val toggleVisible: Callback =
            if visible then
              visibleS.setState(false) >> phaseS.modState {
                case Ready(t, i, _) => Ready(t, i, false)
                case other          => other
              }
            else visibleS.setState(true)

          phaseS.value match
            case Idle =>
              renderShell(
                props,
                idleControls(runTrace, visible, toggleVisible, hasTrace = false),
                None,
                hasTrace = false,
                visible = visible
              )
            case Running =>
              renderShell(
                props,
                runningControls,
                None,
                hasTrace = false,
                visible = visible
              )
            case Failed(msg) =>
              renderShell(
                props,
                failedControls(msg, runTrace, visible, toggleVisible),
                None,
                hasTrace = false,
                visible = visible
              )
            case Ready(trace, idx, playing) =>
              val ctrls = readyControls(
                idx,
                trace.steps.length,
                playing,
                visible,
                onPrev = phaseS.modState {
                  case Ready(t, i, _) => Ready(t, math.max(0, i - 1), false)
                  case other          => other
                },
                onNext = phaseS.modState {
                  case Ready(t, i, _) => Ready(t, math.min(t.steps.length - 1, i + 1), false)
                  case other          => other
                },
                onReset = phaseS.modState {
                  case Ready(t, _, _) => Ready(t, 0, false)
                  case other          => other
                },
                onTogglePlay = phaseS.modState {
                  case Ready(t, i, p) =>
                    if !p && i >= t.steps.length - 1 then Ready(t, 0, true)
                    else Ready(t, i, !p)
                  case other => other
                },
                onReTrace = runTrace,
                onToggleVisible = toggleVisible
              )
              renderShell(props, ctrls, Some((trace, idx)), hasTrace = true, visible = visible)
      }

  private def isPython(lang: String): Boolean =
    val l = lang.toLowerCase
    l == "python" || l == "py" || l == "python3"

  // ---------------------------------------------------------------------------
  // Subviews
  // ---------------------------------------------------------------------------

  private def renderShell(
      props: Props,
      controls: VdomElement,
      readyState: Option[(Trace, Int)],
      hasTrace: Boolean,
      visible: Boolean
  ): VdomElement =
    val rootClasses =
      val base = if hasTrace then "traced-code traced-code--has-trace" else "traced-code"
      if visible then base else s"$base traced-code--collapsed"
    <.div(
      ^.className := rootClasses,
      <.div(
        ^.className := "traced-code__header",
        <.span(^.className := "traced-code__language-label", "🐍 Python · traced"),
        controls
      ),
      if !visible then EmptyVdom
      else
        React.Fragment(
          <.div(
            ^.className := "traced-code__body",
            renderSource(props.source, readyState.map { case (t, i) => t.steps(i).line }),
            readyState match
              case Some((trace, idx)) => renderLocalsPanel(trace.steps(idx))
              case None               => EmptyVdom
          ),
          readyState match
            case Some((trace, idx)) =>
              val step = trace.steps(idx)
              <.p(
                ^.className := "traced-code__caption",
                ^.aria.live := "polite",
                s"${step.event} in ${step.fn}() — line ${step.line}"
              )
            case None => EmptyVdom,
          readyState match
            case Some((trace, _)) if trace.programStdout.nonEmpty =>
              <.details(
                ^.className := "traced-code__details",
                <.summary(^.className := "traced-code__details-summary", "Program output"),
                <.pre(^.className     := "traced-code__details-pre", trace.programStdout)
              )
            case _ => EmptyVdom
        )
    )

  private def renderSource(source: String, currentLine: Option[Int]): VdomElement =
    val lines = source.split("\n", -1)
    <.pre(
      ^.className := "traced-code__source not-prose",
      lines.zipWithIndex.toVdomArray { case (line, i) =>
        val lineNo    = i + 1
        val isCurrent = currentLine.contains(lineNo)
        val cls =
          if isCurrent then "traced-code__line traced-code__line--current"
          else "traced-code__line"
        <.span(
          ^.key       := lineNo.toString,
          ^.className := cls,
          <.span(^.className := "traced-code__line-number", f"$lineNo%4d"),
          <.code(
            ^.className               := "traced-code__line-code",
            ^.dangerouslySetInnerHtml := highlightWithPrism(if line.isEmpty then " " else line, "python")
          )
        )
      }
    )

  private def renderLocalsPanel(step: HeapStep): VdomElement =
    <.div(
      ^.className := "traced-code__locals",
      <.p(^.className := "traced-code__locals-title", s"Locals — ${step.fn}()"),
      if step.locals.isEmpty then
        <.p(^.className := "traced-code__locals-empty", "(no locals yet)"): VdomNode
      else
        <.table(
          ^.className := "traced-code__locals-table",
          <.tbody(
            step.locals.toVdomArray { case (k, v) =>
              <.tr(
                ^.key := k,
                <.td(^.className := "traced-code__locals-name", k),
                <.td(^.className := "traced-code__locals-value", displayValue(v, step.heap))
              )
            }
          )
        )
    )

  /** Render a captured local for the locals table: scalars inline, objects as a compact type tag. */
  private def displayValue(v: HeapValue, heap: Map[String, HeapObject]): String =
    v match
      case HeapValue.Scalar(HeapScalar.I(n)) => n.toString
      case HeapValue.Scalar(HeapScalar.D(d)) => d.toString
      case HeapValue.Scalar(HeapScalar.B(b)) => if b then "True" else "False"
      case HeapValue.Scalar(HeapScalar.S(s)) => "\"" + s + "\""
      case HeapValue.Scalar(HeapScalar.Null) => "None"
      case HeapValue.Ref(id) =>
        heap.get(id) match
          case Some(HeapObject.Instance(cls, _))        => cls
          case Some(HeapObject.Arr(ArrKind.Lst, items)) => s"list[${items.size}]"
          case Some(HeapObject.Arr(ArrKind.Tup, items)) => s"tuple[${items.size}]"
          case Some(HeapObject.Dict(entries))           => s"dict[${entries.size}]"
          case None                                     => "<ref>"

  // Eye-icon toggle for show/hide. Lives in the header alongside other controls; label flips with state.
  private def visibilityToggle(visible: Boolean, onToggle: Callback): VdomElement =
    <.button(
      ^.tpe := "button",
      ^.onClick --> onToggle,
      ^.aria.label := (if visible then "Hide tracer" else "Show tracer"),
      ^.className  := "traced-code__button traced-code__button--ghost",
      if visible then LucideIcons.EyeOff(LucideIcons.withClass("traced-code__button-icon"))
      else LucideIcons.Eye(LucideIcons.withClass("traced-code__button-icon")),
      if visible then "Hide" else "Show"
    )

  private def idleControls(
      onTrace: Callback,
      visible: Boolean,
      onToggleVisible: Callback,
      hasTrace: Boolean
  ): VdomElement =
    <.div(
      ^.className := "traced-code__controls",
      <.button(
        ^.tpe := "button",
        ^.onClick --> onTrace,
        ^.className := "traced-code__button traced-code__button--primary",
        LucideIcons.Play(LucideIcons.withClass("traced-code__button-icon")),
        "Trace"
      ),
      // Only show Hide/Show when there's something in the body worth toggling. In Idle with no prior trace,
      // hiding the (empty) body has no effect — and the "Trace" button itself acts as the expand affordance.
      if hasTrace || visible then visibilityToggle(visible, onToggleVisible) else EmptyVdom
    )

  private def runningControls: VdomElement =
    <.div(
      ^.className := "traced-code__controls",
      <.span(
        ^.className := "traced-code__loading",
        LucideIcons.Loader2(LucideIcons.withClass("traced-code__loading-icon")),
        "Tracing…"
      )
    )

  private def failedControls(
      message: String,
      onRetry: Callback,
      visible: Boolean,
      onToggleVisible: Callback
  ): VdomElement =
    <.div(
      ^.className := "traced-code__controls",
      <.span(^.className := "traced-code__error-inline", message),
      <.button(
        ^.tpe := "button",
        ^.onClick --> onRetry,
        ^.className := "traced-code__button",
        LucideIcons.RotateCcw(LucideIcons.withClass("traced-code__button-icon")),
        "Retry"
      ),
      visibilityToggle(visible, onToggleVisible)
    )

  private def readyControls(
      idx: Int,
      total: Int,
      playing: Boolean,
      visible: Boolean,
      onPrev: Callback,
      onNext: Callback,
      onReset: Callback,
      onTogglePlay: Callback,
      onReTrace: Callback,
      onToggleVisible: Callback
  ): VdomElement =
    val atStart = idx == 0
    val atEnd   = idx == total - 1
    <.div(
      ^.className := "traced-code__controls",
      // Step controls only make sense while the body is visible. Hidden state keeps the trace in memory but
      // collapses to just the language label + Show toggle.
      if visible then
        React.Fragment(
          <.button(
            ^.tpe := "button",
            ^.onClick --> onPrev,
            ^.disabled   := atStart,
            ^.aria.label := "Previous step",
            ^.className  := "traced-code__button",
            LucideIcons.ArrowLeft(LucideIcons.withClass("traced-code__button-icon")),
            "Prev"
          ),
          <.button(
            ^.tpe := "button",
            ^.onClick --> onTogglePlay,
            ^.aria.label := (if playing then "Pause" else "Play"),
            ^.className  := "traced-code__button traced-code__button--primary",
            if playing then LucideIcons.Pause(LucideIcons.withClass("traced-code__button-icon"))
            else LucideIcons.Play(LucideIcons.withClass("traced-code__button-icon")),
            if playing then "Pause" else "Play"
          ),
          <.button(
            ^.tpe := "button",
            ^.onClick --> onNext,
            ^.disabled   := atEnd,
            ^.aria.label := "Next step",
            ^.className  := "traced-code__button",
            "Next",
            LucideIcons.ArrowRight(LucideIcons.withClass("traced-code__button-icon"))
          ),
          <.button(
            ^.tpe := "button",
            ^.onClick --> onReset,
            ^.disabled   := atStart,
            ^.aria.label := "Reset",
            ^.className  := "traced-code__button traced-code__button--icon",
            LucideIcons.RotateCcw(LucideIcons.withClass("traced-code__button-icon"))
          ),
          <.span(
            ^.className := "traced-code__progress",
            s"Step ${idx + 1} / $total"
          ),
          <.button(
            ^.tpe := "button",
            ^.onClick --> onReTrace,
            ^.aria.label := "Re-run trace",
            ^.className  := "traced-code__button traced-code__button--ghost",
            "Re-trace"
          )
        )
      else
        <.span(
          ^.className := "traced-code__progress",
          s"trace ready · ${total} step${if total == 1 then "" else "s"}"
        )
      ,
      visibilityToggle(visible, onToggleVisible)
    )
