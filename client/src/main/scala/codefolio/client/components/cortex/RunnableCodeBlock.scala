package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import codefolio.shared.api.Endpoints.{RunRequest, RunResult}
import codefolio.shared.runner.CodeExecutor
import codefolio.shared.runner.CodeExecutor.RunState
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success}

/**
 * Renders an editable code editor + Run/Cancel/Reset controls + an output panel. State machine lives in
 * [[CodeExecutor]] (shared module — testable on the JVM); this file owns the React surface only:
 * `useStateBy(...)`, callback wiring, and presentation.
 *
 * `bare = true` skips the outer card wrapper (used by RunnableCodeGroup, which provides its own);
 * `hideLanguageLabel = true` hides the in-header language name (the group renders it as a tab instead).
 *
 * The `RunHandle` issued by `CodeExecutor.started` doesn't actually cancel the in-flight HTTP request (sttp's
 * FetchBackend has no signal hook); it only discards late results for stale runs. See
 * `CodeExecutor.completed` for the filter.
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
      hideLanguageLabel: Boolean = false,
      // false → display-only tab (e.g. pseudocode). Suppresses Run/Reset/Cancel
      // controls, the output panel, and swaps the editable editor for a static
      // syntax-highlighted <pre>.
      runnable: Boolean = true
  )

  private val StatusOk = 3

  private given ExecutionContext = JSExecutionContext.queue

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useStateBy(p => CodeExecutor.initial(p.source))
      .render { (props, st) =>
        val s     = st.value
        val dirty = CodeExecutor.isDirty(s, props.source)

        val minHeight =
          val lines = math.max(props.source.split("\n").length, 5)
          math.min(lines * 22 + 24, 600)

        val resetCb: Callback =
          st.modState(_ => CodeExecutor.reset(props.source))

        val cancelCb: Callback =
          st.modState(CodeExecutor.cancel)

        def runCb: Callback = Callback.suspend {
          val snapshot  = st.value
          val codeAtRun = snapshot.code
          val nextState = CodeExecutor.started(snapshot)
          val handle    = nextState.runId
          st.modState(_ => nextState) >> Callback {
            val req = RunRequest(props.language, codeAtRun, None)
            ApiClient.runCode(req).onComplete {
              case Success(resp) =>
                st.modState(prev => CodeExecutor.completed(prev, handle, resp.result)).runNow()
              case Failure(err) =>
                st.modState(prev => CodeExecutor.failed(prev, handle, err.getMessage)).runNow()
            }
          }
        }

        val onKeyDown: ReactKeyboardEvent => Callback = (e: ReactKeyboardEvent) =>
          if (e.metaKey || e.ctrlKey) && e.key == "Enter" then
            e.preventDefaultCB >> runCb
          else Callback.empty

        val labelText =
          if props.hideLanguageLabel then "" else props.languageLabel.getOrElse(props.language)

        // Real brand icons for languages where the emoji-only label is too vague
        // (e.g. Scala, where 🌀 reads as a generic swirl). Other languages keep
        // the emoji prefix in the label string. Suppressed alongside the text
        // when `hideLanguageLabel` is set — the group tab strip already shows
        // the brand icon, and emitting it again here would double-render it
        // inside the active tab's bare block.
        val labelNode: VdomNode =
          <.span(
            ^.className := "rcb__language-label",
            if !props.hideLanguageLabel && props.language.equalsIgnoreCase("scala") then
              TagMod(BrandIcons.Scala("rcb__brand-icon rcb__brand-icon--scala"), labelText)
            else labelText
          )

        // Display-only tabs (pseudocode) drop the Run/Cancel/Reset controls and
        // the output panel, and swap the editable editor for a static <pre>
        // with the same Prism highlighting hook (which falls back to
        // escapeHtml for unknown grammars — fine for pseudocode).
        val header: VdomNode =
          if !props.runnable then
            if props.hideLanguageLabel then EmptyVdom
            else
              <.div(
                ^.className := "rcb__header",
                labelNode
              )
          else
            <.div(
              ^.className := "rcb__header",
              labelNode,
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

        val editor: VdomNode =
          if !props.runnable then
            // `not-prose` is required: .chapter-content is a `prose` container,
            // and Tailwind Typography forces `padding: 0; background: transparent`
            // (with !important) on every descendant <pre> so rehype-pretty-code
            // blocks can style themselves. Opt out so the editor styling wins.
            <.pre(
              ^.className               := "rcb__editor rcb__editor--static not-prose",
              ^.style                   := js.Dynamic.literal(minHeight = minHeight).asInstanceOf[js.Object],
              ^.dangerouslySetInnerHtml := s"<code>${highlightWithPrism(props.source, props.language)}</code>"
            )
          else
            val editorProps = (new js.Object).asInstanceOf[EditorProps]
            editorProps.value = s.code
            editorProps.onValueChange = (next: String) =>
              st.modState(prev => CodeExecutor.setCode(prev, next)).runNow()
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

            <.div(
              ^.className := "rsce-editor rcb__editor",
              ^.style     := js.Dynamic.literal(minHeight = minHeight).asInstanceOf[js.Object],
              ^.onKeyDown ==> onKeyDown,
              Editor(editorProps)
            )

        val output: VdomNode =
          if !props.runnable then EmptyVdom
          else if s.runState == RunState.Idle && s.result.isEmpty && s.error.isEmpty then EmptyVdom
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
