package codefolio.client.components.knowledge

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

/** Port of `portfolio-app/src/components/knowledge/RunnableCodeBlock.tsx`.
  *
  * Renders an editable code editor + Run/Cancel/Reset controls + an output
  * panel. Posts to /api/run via [[ApiClient.runCode]] and shows the
  * Judge0-shaped result. Mirrors the original look-and-feel closely.
  *
  * `bare = true` skips the outer card wrapper (used by RunnableCodeGroup,
  * which provides its own); `hideLanguageLabel = true` hides the in-header
  * language name (the group renders it as a tab instead).
  *
  * The `runId` counter doesn't actually cancel the in-flight HTTP request
  * (sttp's FetchBackend has no signal hook); it only discards late results
  * for stale runs. That's good enough as long as `tag` is computed from
  * the **post-modState** state, not the render-time snapshot.
  */
object RunnableCodeBlock:

  // ---- react-simple-code-editor JsComponent facade -----------------------

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

  // ---- Prism highlight bridge ---------------------------------------------

  @js.native @JSImport("@markdown/runtime", "highlightWithPrism")
  private def highlightWithPrism(code: String, lang: String): String = js.native

  // ---- Component -----------------------------------------------------------

  final case class Props(
      language: String,
      source: String,
      languageLabel: Option[String] = None,
      /** Skip the outer rounded-border card (used by RunnableCodeGroup). */
      bare: Boolean = false,
      /** Suppress the language label in the header (the group shows it as a tab). */
      hideLanguageLabel: Boolean = false
  )

  private val StatusOk = 3

  private enum RunState:
    case Idle, Running, Done

  private final case class State(
      code: String,
      runState: RunState,
      result: Option[RunResult],
      error: Option[String],
      // Monotonically-increasing ID; bumped on every run/cancel so a late
      // resolving Future from a prior run doesn't clobber newer state.
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
        val s = st.value
        val dirty = s.code != props.source

        val minHeight =
          val lines = math.max(props.source.split("\n").length, 5)
          math.min(lines * 22 + 24, 600)

        // ---- handlers -----------------------------------------------------

        val resetCb: Callback =
          st.modState(_ => State.initial(props.source))

        val cancelCb: Callback =
          st.modState(prev => prev.copy(runState = RunState.Idle, runId = prev.runId + 1))

        def runCb: Callback = Callback.suspend {
          // Read state at click time BEFORE calling modState. The previous
          // version captured `prev.code` and `next.runId` into outer vars
          // from inside the modState updater — but in scalajs-react's
          // Hooks, the updater can fire deferred (React 18 batching), so
          // by the time the chained `>> Callback { ... }` runs, those
          // vars may still hold their initial empty values. That bug sent
          // an empty `source` on every second-and-later click, producing
          // the runner's "Accepted (no output)" symptom. Capturing from
          // `st.value` here is straightforward: it reflects the latest
          // committed state at click time, which is exactly what we want
          // to send.
          val codeAtRun = st.value.code
          val tag       = st.value.runId + 1
          st.modState(_.copy(
            runState = RunState.Running,
            error    = None,
            result   = None,
            runId    = tag
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

        // Cmd/Ctrl+Enter to run.
        val onKeyDown: ReactKeyboardEvent => Callback = (e: ReactKeyboardEvent) =>
          if (e.metaKey || e.ctrlKey) && e.key == "Enter" then
            e.preventDefaultCB >> runCb
          else
            Callback.empty

        // ---- rendering ----------------------------------------------------

        val header =
          <.div(
            ^.className := "flex items-center justify-between gap-2 px-3 py-2 border-b border-border bg-muted/40",
            <.span(
              ^.className := "text-xs font-medium text-muted-foreground",
              if props.hideLanguageLabel then "" else props.languageLabel.getOrElse(props.language)
            ),
            <.div(
              ^.className := "flex items-center gap-2",
              if dirty && s.runState != RunState.Running then
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> resetCb,
                  ^.className :=
                    "inline-flex items-center gap-1.5 rounded-md border border-border " +
                      "px-2 py-1 text-xs font-semibold hover:bg-accent",
                  LucideIcons.RotateCcw(LucideIcons.withClass("h-3 w-3")),
                  "Reset"
                )
              else EmptyVdom,
              if s.runState == RunState.Running then
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> cancelCb,
                  ^.className :=
                    "inline-flex items-center gap-1.5 rounded-md border border-border " +
                      "px-3 py-1 text-xs font-semibold hover:bg-accent",
                  LucideIcons.Square(LucideIcons.withClass("h-3 w-3")),
                  "Cancel"
                )
              else
                <.button(
                  ^.tpe := "button",
                  ^.title := "Run (⌘+Enter)",
                  ^.onClick --> runCb,
                  ^.className :=
                    "inline-flex items-center gap-1.5 rounded-md bg-primary text-primary-foreground " +
                      "px-3 py-1 text-xs font-semibold hover:bg-primary/90",
                  LucideIcons.Play(LucideIcons.withClass("h-3 w-3")),
                  "Run"
                )
            )
          )

        val editorProps = (new js.Object).asInstanceOf[EditorProps]
        editorProps.value         = s.code
        editorProps.onValueChange = (next: String) => st.modState(_.copy(code = next)).runNow()
        editorProps.highlight     = (c: String) => highlightWithPrism(c, props.language)
        editorProps.padding       = 16
        editorProps.tabSize       = 4
        editorProps.insertSpaces  = true
        editorProps.textareaClassName = "focus:outline-none"
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
            ^.className := "rsce-editor bg-[#2d2d2d] text-zinc-100",
            ^.style := js.Dynamic.literal(minHeight = minHeight).asInstanceOf[js.Object],
            ^.onKeyDown ==> onKeyDown,
            Editor(editorProps)
          )

        val output: VdomNode =
          if s.runState == RunState.Idle && s.result.isEmpty && s.error.isEmpty then EmptyVdom
          else
            <.div(
              ^.className := "border-t border-border bg-muted/20 px-4 py-3 text-sm font-mono",
              if s.runState == RunState.Running then
                <.p(
                  ^.className := "flex items-center gap-2 text-muted-foreground",
                  LucideIcons.Loader2(LucideIcons.withClass("h-3.5 w-3.5 animate-spin")),
                  "Running…"
                )
              else EmptyVdom,
              s.error.map(err =>
                <.pre(^.className := "whitespace-pre-wrap text-destructive text-xs", err): VdomNode
              ).getOrElse(EmptyVdom),
              s.result.map(renderResult).getOrElse(EmptyVdom)
            )

        if props.bare then
          React.Fragment(header, editor, output)
        else
          <.div(
            ^.className := "my-6 rounded-lg border border-border overflow-hidden",
            header,
            editor,
            output
          )
      }

  private def renderResult(r: RunResult): VdomNode =
    val ok = r.statusId == StatusOk

    <.div(
      ^.className := "space-y-2",
      <.div(
        ^.className := "flex flex-wrap items-center gap-2 text-xs",
        <.span(
          ^.className := s"inline-flex items-center rounded px-1.5 py-0.5 font-semibold ${
              if ok then "bg-emerald-500/15 text-emerald-700 dark:text-emerald-400"
              else "bg-rose-500/15 text-rose-700 dark:text-rose-400"
            }",
          r.statusDescription
        ),
        r.time
          .map(t => <.span(^.className := "text-muted-foreground", s"${t}s"): VdomNode)
          .getOrElse(EmptyVdom),
        r.memory
          .map(m =>
            <.span(^.className := "text-muted-foreground", s"${(m / 1024).toInt} MB"): VdomNode
          )
          .getOrElse(EmptyVdom)
      ),
      Option(r.compileOutput).filter(_.nonEmpty).map { co =>
        (<.details(
          ^.open := true,
          <.summary(^.className := "cursor-pointer text-xs text-muted-foreground", "compile output"),
          <.pre(
            ^.className := "mt-1 whitespace-pre-wrap text-xs text-amber-700 dark:text-amber-400",
            co
          )
        ): VdomNode)
      }.getOrElse(EmptyVdom),
      Option(r.stderr).filter(_.nonEmpty).map { e =>
        (<.details(
          ^.open := true,
          <.summary(^.className := "cursor-pointer text-xs text-muted-foreground", "stderr"),
          <.pre(
            ^.className := "mt-1 whitespace-pre-wrap text-xs text-rose-700 dark:text-rose-400",
            e
          )
        ): VdomNode)
      }.getOrElse(EmptyVdom),
      if Option(r.stdout).exists(_.nonEmpty) then
        <.pre(^.className := "whitespace-pre-wrap text-xs text-foreground", r.stdout): VdomNode
      else if Option(r.stderr).forall(_.isEmpty) && Option(r.compileOutput).forall(_.isEmpty) then
        <.p(^.className := "text-xs text-muted-foreground italic", "(no output)"): VdomNode
      else EmptyVdom
    )
