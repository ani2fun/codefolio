package codefolio.client.components.cortex

import codefolio.client.api.ApiClient
import codefolio.client.auth.{AuthStore, IdentityChip}
import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import codefolio.shared.api.Endpoints.{RunRequest, RunResult, UserInfo}
import codefolio.shared.runner.CodeExecutor
import codefolio.shared.runner.CodeExecutor.{EditMode, RunState}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.{Failure, Success, Try}

/**
 * Renders a code editor + Run/Edit/Cancel controls + an output panel. State machine lives in [[CodeExecutor]]
 * (shared module — testable on the JVM); this file owns the React surface only.
 *
 * **Auth gate (ADR-0013).** Editing is gated behind a signed-in identity:
 *   - anonymous (or auth still booting) — the source renders as a read-only highlighted block; the **Edit**
 *     button wears a lock badge and opens the sign-in modal on click; **Run** still works (the canonical
 *     source executes, metered per-IP);
 *   - signed in — **Edit** is unlocked; clicking it swaps in the live editor; an identity chip shows in the
 *     header and an hourly-quota notice slides in under the block as the budget runs low;
 *   - auth disabled server-side (`AUTH_ENABLED=false`, dev) — editing is open to everyone, no chip.
 *
 * `bare = true` skips the outer card wrapper (used by RunnableCodeGroup, which provides its own);
 * `hideLanguageLabel = true` hides the in-header language name (the group renders it as a tab instead).
 *
 * The `RunHandle` issued by `CodeExecutor.started` doesn't actually cancel the in-flight HTTP request (sttp's
 * FetchBackend has no signal hook); it only discards late results for stale runs.
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
      // false → display-only tab (e.g. pseudocode). Suppresses Run/Edit/Cancel
      // controls, the output panel, and the auth gate entirely.
      runnable: Boolean = true,
      // When set, a "Visualise" button opens VisualiseModal and traces the live
      // editor content. `viz` is the layout hint (e.g. "binary-tree"); `vizRoot`
      // optionally names the structure's root variable; `vizCase` optionally
      // overrides the multi-case segmentation count. Set only on Python tabs.
      viz: Option[String] = None,
      vizRoot: Option[String] = None,
      vizCase: Option[Int] = None
  )

  private val StatusOk = 3

  // Show the quota notice once the signed-in user has burned ≥ 70% of the hourly bucket.
  // Integer compare (used*10 ≥ limit*7) avoids floating point.
  private def quotaRunningLow(q: codefolio.shared.api.Endpoints.Quota): Boolean =
    q.limit > 0 && q.used * 10 >= q.limit * 7

  /** Format an epoch-millis instant as `HH:MM` in UTC, for the quota-reset caption. */
  private def resetClockUtc(epochMs: Long): String =
    val d  = new js.Date(epochMs.toDouble)
    val hh = d.getUTCHours().toInt
    val mm = d.getUTCMinutes().toInt
    f"$hh%02d:$mm%02d"

  private given ExecutionContext = JSExecutionContext.queue

  // ─── Discovery cue ──────────────────────────────────────────────────────────────────────────────
  // A one-time nudge toward the Visualise button. The first viz block a visitor actually scrolls into
  // view claims it — `discoveryClaimed` caps it at one nudge per page load, `DiscoveryKey` in
  // localStorage caps it at one per browser. See the `useEffectOnMountBy` IntersectionObserver.
  private val DiscoveryKey     = "cortex-reader.vizDiscovered"
  private var discoveryClaimed = false
  private var vizSeq           = 0

  private def nextVizId(): String =
    vizSeq += 1
    s"rcb-viz-$vizSeq"

  // On failure (e.g. localStorage blocked in private browsing) treat the cue as already seen — never
  // nag rather than risk nagging on every visit.
  private def discoverySeen(): Boolean =
    Try(dom.window.localStorage.getItem(DiscoveryKey) != null).getOrElse(true)

  private def markDiscoverySeen(): Unit =
    Try(dom.window.localStorage.setItem(DiscoveryKey, "1")).getOrElse(())

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useStateBy(p => CodeExecutor.initial(p.source))
      .useState(AuthStore.current)
      // Mirror the global auth snapshot into local state so the block re-renders on sign-in/out.
      .useEffectOnMountBy { (_, _, authS) =>
        CallbackTo {
          val unsubscribe = AuthStore.subscribe(s => authS.setState(s).runNow())
          Callback(unsubscribe())
        }
      }
      .useState(false)            // VisualiseModal open state
      .useState(false)            // discovery cue visible
      .useRefBy(_ => nextVizId()) // stable id for the Visualise button wrapper
      // First-encounter discovery cue: observe this block's Visualise button and show the one-time
      // nudge when it first scrolls into view (so the cue is claimed by a button actually seen).
      .useEffectOnMountBy { (props, _, _, _, cueS, vizIdRef) =>
        CallbackTo {
          val target =
            if props.viz.isDefined && !discoveryClaimed && !discoverySeen() then
              Option(dom.document.getElementById(vizIdRef.value))
            else None
          target match
            case None => Callback.empty
            case Some(el) =>
              val callback
                  : js.Function2[js.Array[dom.IntersectionObserverEntry], dom.IntersectionObserver, Unit] =
                (entries, obs) => {
                  val seen = entries.exists(_.isIntersecting)
                  if seen && !discoveryClaimed && !discoverySeen() then
                    discoveryClaimed = true
                    markDiscoverySeen()
                    cueS.setState(true).runNow()
                  if seen then obs.disconnect()
                }
              val observer = new dom.IntersectionObserver(callback)
              observer.observe(el)
              Callback(observer.disconnect())
        }
      }
      .render { (props, st, authS, vizOpenS, cueS, vizIdRef) =>
        val s = st.value

        val status  = authS.value.status
        val editing = s.editMode == EditMode.Editing

        // Who may edit: a signed-in user, or anyone when auth is switched off server-side.
        val canEdit = status match
          case AuthStore.Status.Disabled     => true
          case AuthStore.Status.Authed(_, _) => true
          case _                             => false

        val authedUser: Option[UserInfo] = status match
          case AuthStore.Status.Authed(user, _) => Some(user)
          case _                                => None

        val minHeight =
          val lines = math.max(props.source.split("\n").length, 5)
          math.min(lines * 22 + 24, 600)

        val cancelRunCb: Callback =
          st.modState(CodeExecutor.cancel)

        // Edit button: signed-in → enter edit mode; otherwise → open the sign-in modal.
        val onEditClick: Callback =
          if canEdit then st.modState(CodeExecutor.enterEdit)
          else Callback(AuthStore.openSignIn())

        val onCancelEdit: Callback =
          st.modState(prev => CodeExecutor.cancelEdit(prev, props.source))

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
          if (e.metaKey || e.ctrlKey) && e.key == "Enter" then e.preventDefaultCB >> runCb
          else if e.key == "Escape" && editing then e.preventDefaultCB >> onCancelEdit
          // ⌥V opens the Visualise modal. macOS Option+V emits "√" as `key`, so accept both.
          else if e.altKey && props.viz.isDefined && (e.key.equalsIgnoreCase("v") || e.key == "√") then
            e.preventDefaultCB >> vizOpenS.setState(true)
          else Callback.empty

        val labelText =
          if props.hideLanguageLabel then "" else props.languageLabel.getOrElse(props.language)

        // Real brand icons for languages where the emoji-only label is too vague
        // (e.g. Scala). Suppressed alongside the text when `hideLanguageLabel` is set.
        val labelNode: VdomNode =
          <.span(
            ^.className := "rcb__language-label",
            if !props.hideLanguageLabel && props.language.equalsIgnoreCase("scala") then
              TagMod(BrandIcons.Scala("rcb__brand-icon rcb__brand-icon--scala"), labelText)
            else labelText
          )

        val identityNode: VdomNode =
          authedUser match
            case Some(user) =>
              IdentityChip.Component(
                IdentityChip.Props(user.preferredUsername, Callback(AuthStore.signOut()))
              )
            case None => EmptyVdom

        // Edit / Cancel-edit control.
        val editControl: VdomNode =
          if editing then
            <.button(
              ^.tpe := "button",
              ^.onClick --> onCancelEdit,
              ^.className := "rcb__button",
              LucideIcons.RotateCcw(LucideIcons.withClass("rcb__button-icon")),
              "Cancel"
            )
          else
            <.button(
              ^.tpe   := "button",
              ^.title := (if canEdit then "Edit and run" else "Sign in with GitHub to edit"),
              ^.onClick --> onEditClick,
              ^.className := "rcb__button" + (if canEdit then "" else " rcb__button--locked"),
              if canEdit then EmptyVdom
              else
                <.span(
                  ^.className := "rcb__lock",
                  LucideIcons.Lock(LucideIcons.withClass("rcb__lock-icon"))
                )
              ,
              LucideIcons.Pencil(LucideIcons.withClass("rcb__button-icon")),
              "Edit"
            )

        // Run / Cancel-run control.
        val runControl: VdomNode =
          if s.runState == RunState.Running then
            <.button(
              ^.tpe := "button",
              ^.onClick --> cancelRunCb,
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
              if editing then "Run edit" else "Run"
            )

        // Visualise — opens VisualiseModal and traces the live editor content. While the discovery
        // cue is showing, the button pulses and a one-time tip sits beneath it (see `renderVizCue`).
        val cueVisible           = cueS.value && props.viz.isDefined
        val dismissCue: Callback = cueS.setState(false)
        val vizControl: VdomNode =
          if props.viz.isDefined then
            <.span(
              ^.className := "rcb__viz",
              ^.id        := vizIdRef.value,
              <.button(
                ^.tpe   := "button",
                ^.title := "Visualise (⌥V)",
                ^.onClick --> (vizOpenS.setState(true) >> dismissCue),
                ^.aria.label := "Visualise code",
                ^.className :=
                  (if cueVisible then "rcb__button rcb__button--viz rcb__button--viz-cue"
                   else "rcb__button rcb__button--viz"),
                LucideIcons.Network(LucideIcons.withClass("rcb__button-icon")),
                "Visualise"
              ),
              if cueVisible then renderVizCue(dismissCue) else EmptyVdom
            )
          else EmptyVdom

        // Display-only tabs (pseudocode): no controls, no auth, static <pre>.
        val header: VdomNode =
          if !props.runnable then
            if props.hideLanguageLabel then EmptyVdom
            else <.div(^.className := "rcb__header", labelNode)
          else
            <.div(
              ^.className := "rcb__header",
              labelNode,
              <.div(
                ^.className := "rcb__controls",
                identityNode,
                editControl,
                vizControl,
                runControl
              )
            )

        // The source as a static, syntax-highlighted, read-only block. Used for
        // pseudocode tabs AND for the anonymous / not-editing runnable view.
        val staticView: VdomNode =
          // `not-prose` opts out of Tailwind Typography's <pre> overrides so the
          // editor styling wins.
          <.pre(
            ^.className               := "rcb__editor rcb__editor--static not-prose",
            ^.style                   := js.Dynamic.literal(minHeight = minHeight).asInstanceOf[js.Object],
            ^.dangerouslySetInnerHtml := s"<code>${highlightWithPrism(props.source, props.language)}</code>"
          )

        val editableView: VdomNode =
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

        // Editing → the live editor; otherwise (pseudocode, or signed-out, or
        // signed-in-but-not-editing) → the read-only highlighted block.
        val editor: VdomNode = if editing then editableView else staticView

        // Bottom edit bar — visible only while editing.
        val editBar: VdomNode =
          if editing then
            val changed = CodeExecutor.changedLineCount(s, props.source)
            <.div(
              ^.className := "rcb__edit-bar",
              <.span(
                ^.className := "rcb__edit-bar-dirty",
                s"● $changed ${if changed == 1 then "line" else "lines"} changed"
              ),
              <.span(^.className := "rcb__edit-bar-spacer"),
              <.span(
                ^.className := "rcb__edit-bar-hint",
                <.kbd("⌘"),
                " ",
                <.kbd("↵"),
                " Run · ",
                <.kbd("Esc"),
                " Cancel · session limited to 30s, 64MB"
              )
            )
          else EmptyVdom

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

        // Quota notice — slides in under the block when the signed-in user's
        // hourly run budget is running low.
        val quotaNotice: VdomNode =
          authedUser.map(_.quota).filter(quotaRunningLow) match
            case Some(q) =>
              <.div(
                ^.className := "quota-notice",
                ^.role      := "status",
                <.span(
                  ^.className := "quota-notice__icon",
                  LucideIcons.AlertTriangle(LucideIcons.withClass("quota-notice__icon-svg"))
                ),
                <.div(
                  <.div(^.className := "quota-notice__title", "Sandbox quota"),
                  <.div(
                    ^.className := "quota-notice__body",
                    "You've used ",
                    <.span(^.className := "quota-notice__figure", s"${q.used} / ${q.limit}"),
                    " runs this hour. Quota resets at ",
                    <.span(^.className := "quota-notice__figure", s"${resetClockUtc(q.resetEpochMs)} UTC"),
                    ". Anonymous read-only runs are unaffected."
                  )
                )
              )
            case None => EmptyVdom

        val vizModal: VdomNode =
          props.viz match
            case Some(hint) =>
              VisualiseModal.Component(
                VisualiseModal.Props(
                  isOpen = vizOpenS.value,
                  onClose = vizOpenS.setState(false),
                  pythonSource = s.code,
                  vizHint = hint,
                  vizRoot = props.vizRoot,
                  vizCase = props.vizCase,
                  title = "Code visualisation"
                )
              )
            case None => EmptyVdom

        val body = React.Fragment(header, editor, editBar, output, quotaNotice)

        if props.bare then React.Fragment(body, vizModal)
        else React.Fragment(<.div(^.className := "rcb", body), vizModal)
      }

  /**
   * The first-encounter discovery cue beneath the Visualise button — a one-time, dismissible tip, shown once
   * per browser (see the `useEffectOnMountBy` IntersectionObserver). Dismissed by its `×` or by opening the
   * modal.
   */
  private def renderVizCue(dismiss: Callback): VdomNode =
    <.div(
      ^.className           := "rcb__viz-cue",
      ^.role                := "status",
      VdomAttr("aria-live") := "polite",
      <.span(^.className := "rcb__viz-cue-text", "New — watch this code run, step by step"),
      <.button(
        ^.tpe        := "button",
        ^.className  := "rcb__viz-cue-close",
        ^.aria.label := "Dismiss tip",
        ^.onClick --> dismiss,
        LucideIcons.X(LucideIcons.withClass("rcb__viz-cue-close-icon"))
      )
    )

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
