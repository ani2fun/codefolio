package codefolio.shared.runner

/**
 * Per-backend execution limits for the code-run pipeline.
 *
 * The tracer harness uses [[maxStdoutBytes]] to size its dynamic-truncation budget (the
 * Python-harness "quarter-drop" loop, ported for Java in Slice 4). Both client and server
 * can import this object — it lives in `shared` so neither side needs to call an endpoint
 * just to know the backend's caps.
 *
 * Hard-coded for now (no `/api/runner-info` endpoint). When Slice 8 makes Code Runner the
 * default, the client consults [[BackendLimits.codeRunner]] directly; the server exposes the
 * active limits via the info endpoint as a follow-up. The values here match:
 *   - Piston public service: stdout capped at ~64 KB per stream.
 *   - Code Runner (self-hosted): default `max_output` of 1 MB.
 *
 * See plan §"Risks" → R2 and ADR-0021 for context.
 */
object BackendLimits:

  /** Execution limits for one backend. */
  final case class Limits(
      /** Maximum bytes accepted from the program's stdout stream. */
      maxStdoutBytes: Int,
      /** Maximum bytes the backend will accept as the source payload. */
      maxSourceBytes: Int,
      /** Default run-time timeout in milliseconds. */
      defaultRunTimeoutMs: Int
  )

  /** Limits for the Piston public service (fallback — Code Runner is the default as of Slice 8). */
  val piston: Limits = Limits(
    maxStdoutBytes     = 64 * 1024,   // 64 KB — Piston hard cap per stdout stream
    maxSourceBytes     = 64 * 1024,   // 64 KB
    defaultRunTimeoutMs = 10_000
  )

  /**
   * Limits for the self-hosted Code Runner backend (Judge0-protocol; see Slice 8 for the
   * operator switch procedure).
   */
  val codeRunner: Limits = Limits(
    maxStdoutBytes     = 1024 * 1024, // 1 MB — Code Runner `max_output` default
    maxSourceBytes     = 64 * 1024,   // 64 KB
    defaultRunTimeoutMs = 10_000
  )
