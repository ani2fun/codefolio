package codefolio.server.handlers

import codefolio.server.config.AppConfig
import codefolio.server.runner.{CodeRunner, Languages, Piston}
import codefolio.shared.api.Endpoints.{
  ApiError,
  RunRequest,
  RunResponse,
  RunResult,
  RunnableLanguageInfo
}
import zio.*

import java.nio.charset.StandardCharsets

/** Errors that bubble out of /api/run. The HttpApp layer maps each variant
  * to its proper HTTP status code.
  */
sealed trait RunFailure extends Product with Serializable
object RunFailure:
  /** Bad request (unknown language, missing field). 400. */
  final case class BadInput(error: String, hint: Option[String] = None) extends RunFailure

  /** Payload too large (source > 64 KiB or stdin > 16 KiB). 413. */
  final case class PayloadTooLarge(error: String) extends RunFailure

  /** No execution backend configured (neither Piston nor Code Runner). 503. */
  case object NotConfigured extends RunFailure

  /** Backend itself errored (Piston / Code Runner returned 5xx, network
    * failure, etc.). 502.
    */
  final case class BackendFailure(error: String, detail: Option[String] = None) extends RunFailure

  def toApiError(f: RunFailure): ApiError = f match
    case BadInput(error, hint) => ApiError(error = error, detail = None, hint = hint)
    case PayloadTooLarge(error)  => ApiError(error = error, detail = None, hint = None)
    case NotConfigured =>
      ApiError(
        error = "Code execution is not configured on this server.",
        detail = None,
        hint = Some("Set PISTON_URL (production) or CODE_RUNNER_URL (local dev).")
      )
    case BackendFailure(error, detail) =>
      ApiError(error = error, detail = detail, hint = None)

trait CodeRunHandler:
  /** Run a snippet. Failures use `RunFailure` so HttpApp can map to status
    * codes; the success path is the same `RunResponse` the OpenAPI promises.
    */
  def run(req: RunRequest): IO[RunFailure, RunResponse]

object CodeRunHandler:

  val live: ZLayer[AppConfig, Nothing, CodeRunHandler] =
    ZLayer.fromFunction(CodeRunHandlerLive(_))

final private class CodeRunHandlerLive(cfg: AppConfig) extends CodeRunHandler:

  override def run(req: RunRequest): IO[RunFailure, RunResponse] =
    for
      _    <- validate(req)
      lang <- ZIO
        .fromOption(Languages.resolve(req.language))
        .mapError(_ => RunFailure.BadInput(s"Language '${req.language}' is not runnable"))
      result <- pickBackend(lang, req).mapError {
        case e: RunFailure => e
        case t: Throwable  =>
          RunFailure.BackendFailure(
            error = "Code execution failed",
            detail = Option(t.getMessage)
          )
      }
    yield RunResponse(result = result, language = lang)

  private def validate(req: RunRequest): IO[RunFailure, Unit] =
    val sourceBytes = req.source.getBytes(StandardCharsets.UTF_8).length
    val stdinBytes  = req.stdin.fold(0)(_.getBytes(StandardCharsets.UTF_8).length)
    if sourceBytes > Languages.MaxSourceBytes then
      ZIO.fail(RunFailure.PayloadTooLarge("Source exceeds size limit"))
    else if stdinBytes > Languages.MaxStdinBytes then
      ZIO.fail(RunFailure.PayloadTooLarge("stdin exceeds size limit"))
    else ZIO.unit

  /** Choose Piston when configured AND the language is Piston-supported;
    * otherwise fall back to Code Runner. If neither path applies, surface
    * NotConfigured (or BadInput if only one backend is configured but the
    * language isn't supported there).
    */
  private def pickBackend(
      lang: RunnableLanguageInfo,
      req: RunRequest
  ): ZIO[Any, RunFailure | Throwable, RunResult] =
    val pistonAvailable     = cfg.runner.pistonUrl.exists(_.nonEmpty)
    val codeRunnerAvailable = cfg.runner.codeRunnerUrl.exists(_.nonEmpty)

    (pistonAvailable, codeRunnerAvailable) match
      case (false, false) => ZIO.fail(RunFailure.NotConfigured)
      case (true, _) if Piston.supports(lang.id) =>
        Piston.run(cfg.runner.pistonUrl.get, req.source, req.stdin, lang)
      case (_, true) =>
        CodeRunner.run(
          cfg.runner.codeRunnerUrl.get,
          cfg.runner.codeRunnerAuthToken.filter(_.nonEmpty),
          req.source,
          req.stdin,
          lang
        )
      case _ =>
        ZIO.fail(
          RunFailure.BadInput(
            s"Language '${lang.label}' is not supported by the configured execution backend."
          )
        )
