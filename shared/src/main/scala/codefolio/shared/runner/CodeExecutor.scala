package codefolio.shared.runner

import codefolio.shared.api.Endpoints.RunResult

/**
 * Pure state machine for a single Runnable Code Block.
 *
 * Sits behind any UI (React, terminal, anything) that drives a snippet through Idle → Running → Done. Each
 * run is tagged with a [[RunHandle]] — an opaque token issued by [[started]] / [[cancel]]. Results that
 * arrive for a stale handle are ignored, which is how cancel + restart works without an actual HTTP-cancel
 * mechanism. Handles can be compared (`==`/`!=`) but never fabricated from outside this module — the only way
 * to obtain one is through the state-machine API.
 *
 * Lives in `shared` so the JS client and JVM tests both see the same logic. No React or Scala.js
 * dependencies.
 */
object CodeExecutor:

  enum RunState:
    case Idle, Running, Done

  /** Identifies the latest run for [[completed]] / [[failed]]. Read off `state.runId`. */
  opaque type RunHandle = Long

  private object RunHandle:
    val initial: RunHandle            = 0L
    def next(h: RunHandle): RunHandle = h + 1L

  final case class State(
      code: String,
      runState: RunState,
      result: Option[RunResult],
      error: Option[String],
      runId: RunHandle
  )

  def initial(source: String): State =
    State(code = source, runState = RunState.Idle, result = None, error = None, runId = RunHandle.initial)

  /** Reset to the initial source, discarding any in-flight or completed run. */
  def reset(source: String): State = initial(source)

  /**
   * Stop watching for in-flight results; UI returns to Idle. The runId bump means stale results land as
   * no-ops (see [[completed]] / [[failed]]).
   */
  def cancel(prev: State): State =
    prev.copy(runState = RunState.Idle, runId = RunHandle.next(prev.runId))

  /** Mark a new run as started: issue a fresh handle, clear previous result/error, transition to Running. */
  def started(prev: State): State =
    prev.copy(
      runState = RunState.Running,
      error = None,
      result = None,
      runId = RunHandle.next(prev.runId)
    )

  /** Apply a successful result if the handle matches the latest run; otherwise leave state untouched. */
  def completed(prev: State, handle: RunHandle, result: RunResult): State =
    if prev.runId != handle then prev
    else prev.copy(runState = RunState.Done, result = Some(result))

  /** Apply a failure if the handle matches the latest run; otherwise leave state untouched. */
  def failed(prev: State, handle: RunHandle, error: String): State =
    if prev.runId != handle then prev
    else prev.copy(runState = RunState.Done, error = Some(error))

  /** Update the editor contents without touching run state. */
  def setCode(prev: State, code: String): State =
    prev.copy(code = code)

  /** True when the editor has been modified relative to the original source. */
  def isDirty(state: State, source: String): Boolean =
    state.code != source
