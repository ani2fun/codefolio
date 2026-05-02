package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.LucideIcons
import codefolio.shared.api.Endpoints.{RunRequest, RunResult}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * Port of `portfolio-app/src/components/knowledge/RunnableCodeBlock.tsx`.
 *
 * Renders an editable code editor + Run/Cancel/Reset controls + an output panel. Posts to /api/run via
 * [[ApiClient.runCode]] and shows the Judge0-shaped result. Mirrors the original look-and-feel closely.
 *
 * `bare = true` skips the outer card wrapper (used by RunnableCodeGroup, which provides its own);
 * `hideLanguageLabel = true` hides the in-header language name (the group renders it as a tab instead).
 *
 * The `runId` counter doesn't actually cancel the in-flight HTTP request (sttp's FetchBackend has no signal
 * hook); it only discards late results for stale runs. That's good enough as long as `tag` is computed from
 * the **post-modState** state, not the render-time snapshot.
 */
object RunnableCodeBlock:

  @js.native @JSImport("react-simple-code-editor", JSImport.Default)
  private object EditorRaw extends js.Object

  private trait EditorProps extends js.Object:
    var value: String
    var onValueChange: js.Function1[String, Unit]
    var highlight: js.Function1[String, String]
    var padding: js.UndefOr[Int]              = js.undefined
    var tabSize: js.UndefOr[Int]              = js.undefined
    var insertSpaces: js.UndefOr[Boolean]     = js.undefined
    var textareaClassName: js.UndefOr[String] = js.undefined
    var style: js.UndefOr[js.Object]          = js.undefined

  private val Editor = JsComponent[EditorProps, Children.None, Null](EditorRaw)

  @js.native @JSImport("@markdown/runtime", "highlightWithPrism")
  private def highlightWithPrism(code: String, lang: String): String = js.native

  final case class Props(
      language: String,
      source: String,
      languageLabel: Option[String] = None,
      bare: Boolean = false,
      hideLanguageLabel: Boolean = false
  )

  private val StatusOk = 3

  private enum RunState:
    case Idle, Running, Done

  final private case class State(
      code: String,
      runState: RunState,
      result: Option[RunResult],
      error: Option[String],
      runId: Long
  )

  private object State:
    def initial(source: String): State = State(source, RunState.Idle, None, None, 0L)

  private given ExecutionContext = JSExecutionContext.queue

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useStateBy(p => State.initial(p.source))
      .render { (props, st) =>
        val s     = st.value
        val dirty = s.code != props.source

        val minHeight =
          val lines = math.max(props.source.split("\n").length, 5)
          math.min(lines * 22 + 24, 600)

        val resetCb: Callback =
          st.modState(_ => State.initial(props.source))

        val cancelCb: Callback =
          st.modState(prev => prev.copy(runState = RunState.Idle, runId = prev.runId + 1))

        def runCb: Callback = Callback.suspend {
          val codeAtRun = st.value.code
          val tag       = st.value.runId + 1
          st.modState(_.copy(
            runState = RunState.Running,
            error = None,
            result = None,
            runId = tag
          )) >> Callback {
            val req = RunRequest(props.language, codeAtRun, None)
            ApiClient.runCode(req).onComplete {
              case Success(resp) =>
                st.modState { current =>
                  if current.runId != tag then current
                  else current.copy(runState = RunState.Done, result = Some(resp.result))
                }.runNow()
              case Failure(err) =>
                st.modState { current =>
                  if current.runId != tag then current
                  else current.copy(runState = RunState.Done, error = Some(err.getMessage))
                }.runNow()
            }
          }
        }

        val onKeyDown: ReactKeyboardEvent => Callback = (e: ReactKeyboardEvent) =>
          if (e.metaKey || e.ctrlKey) && e.key == "Enter" then
            e.preventDefaultCB >> runCb
          else Callback.empty

        val header =
          <.div(
            ^.className := "rcb__header",
            <.span(
              ^.className := "rcb__language-label",
              if props.hideLanguageLabel then "" else props.languageLabel.getOrElse(props.language)
            ),
            <.div(
              ^.className := "rcb__controls",
              if dirty && s.runState != RunState.Running then
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> resetCb,
                  ^.className := "rcb__button",
                  LucideIcons.RotateCcw(LucideIcons.withClass("rcb__button-icon")),
                  "Reset"
                )
              else EmptyVdom,
              if s.runState == RunState.Running then
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> cancelCb,
                  ^.className := "rcb__button rcb__button--cancel",
                  LucideIcons.Square(LucideIcons.withClass("rcb__button-icon")),
                  "Cancel"
                )
              else
                <.button(
                  ^.tpe   := "button",
                  ^.title := "Run (⌘+Enter)",
                  ^.onClick --> runCb,
                  ^.className := "rcb__button rcb__button--run",
                  LucideIcons.Play(LucideIcons.withClass("rcb__button-icon")),
                  "Run"
                )
            )
          )

        val editorProps = (new js.Object).asInstanceOf[EditorProps]
        editorProps.value = s.code
        editorProps.onValueChange = (next: String) => st.modState(_.copy(code = next)).runNow()
        editorProps.highlight = (c: String) => highlightWithPrism(c, props.language)
        editorProps.padding = 16
        editorProps.tabSize = 4
        editorProps.insertSpaces = true
        editorProps.textareaClassName = "rcb__editor-textarea"
        editorProps.style = js.Dynamic
          .literal(
            fontFamily =
              "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace",
            fontSize = 13,
            lineHeight = 1.6,
            minHeight = minHeight
          )
          .asInstanceOf[js.Object]

        val editor =
          <.div(
            ^.className := "rsce-editor rcb__editor",
            ^.style     := js.Dynamic.literal(minHeight = minHeight).asInstanceOf[js.Object],
            ^.onKeyDown ==> onKeyDown,
            Editor(editorProps)
          )

        val output: VdomNode =
          if s.runState == RunState.Idle && s.result.isEmpty && s.error.isEmpty then EmptyVdom
          else
            <.div(
              ^.className := "rcb__output",
              if s.runState == RunState.Running then
                <.p(
                  ^.className := "rcb__loading",
                  LucideIcons.Loader2(LucideIcons.withClass("rcb__loading-icon")),
                  "Running…"
                )
              else EmptyVdom,
              s.error.map(err => <.pre(^.className := "rcb__error", err): VdomNode).getOrElse(EmptyVdom),
              s.result.map(renderResult).getOrElse(EmptyVdom)
            )

        if props.bare then React.Fragment(header, editor, output)
        else
          <.div(
            ^.className := "rcb",
            header,
            editor,
            output
          )
      }

  private def renderResult(r: RunResult): VdomNode =
    val ok = r.statusId == StatusOk
    val statusCls =
      if ok then "rcb__status rcb__status--ok"
      else "rcb__status rcb__status--err"

    <.div(
      ^.className := "rcb__result",
      <.div(
        ^.className := "rcb__status-row",
        <.span(^.className := statusCls, r.statusDescription),
        r.time
          .map(t => <.span(^.className := "rcb__status-meta", s"${t}s"): VdomNode)
          .getOrElse(EmptyVdom),
        r.memory
          .map(m => <.span(^.className := "rcb__status-meta", s"${(m / 1024).toInt} MB"): VdomNode)
          .getOrElse(EmptyVdom)
      ),
      Option(r.compileOutput).filter(_.nonEmpty).map { co =>
        <.details(
          ^.open := true,
          <.summary(^.className := "rcb__details-summary", "compile output"),
          <.pre(^.className     := "rcb__details-pre--compile", co)
        ): VdomNode
      }.getOrElse(EmptyVdom),
      Option(r.stderr).filter(_.nonEmpty).map { e =>
        <.details(
          ^.open := true,
          <.summary(^.className := "rcb__details-summary", "stderr"),
          <.pre(^.className     := "rcb__details-pre--stderr", e)
        ): VdomNode
      }.getOrElse(EmptyVdom),
      if Option(r.stdout).exists(_.nonEmpty) then
        <.pre(^.className := "rcb__stdout", r.stdout): VdomNode
      else if Option(r.stderr).forall(_.isEmpty) && Option(r.compileOutput).forall(_.isEmpty) then
        <.p(^.className := "rcb__no-output", "(no output)"): VdomNode
      else EmptyVdom
    )
