package codefolio.server.http

import codefolio.server.codeRunPipeline.RunFailure
import codefolio.server.cortexPipeline.CortexFailure
import codefolio.server.helloPipeline.HelloFailure
import codefolio.shared.api.Endpoints.ApiError
import sttp.model.StatusCode

object ApiErrors:

  /**
   * Anything a handler can fail with that the HTTP layer must translate into a status code + envelope. New
   * handler error types are added to this union and to the match in [[toHttp]] — the compiler flags missing
   * cases at the call site.
   */
  type HandlerFailure = RunFailure | CortexFailure | HelloFailure

  def toHttp(failure: HandlerFailure): (StatusCode, ApiError) = failure match
    case RunFailure.BadInput(error, hint) =>
      StatusCode.BadRequest -> ApiError(error = error, detail = None, hint = hint)
    case RunFailure.PayloadTooLarge(error) =>
      StatusCode.PayloadTooLarge -> ApiError(error = error, detail = None, hint = None)
    case RunFailure.NotConfigured =>
      StatusCode.ServiceUnavailable -> ApiError(
        error = "Code execution is not configured on this server.",
        detail = None,
        hint = Some("Set PISTON_URL (production) or CODE_RUNNER_URL (local dev).")
      )
    case RunFailure.BackendFailure(error, detail) =>
      StatusCode.BadGateway -> ApiError(error = error, detail = detail, hint = None)
    case CortexFailure.NotFound =>
      StatusCode.NotFound -> ApiError(error = "Not found", detail = None, hint = None)
    case CortexFailure.IO(detail) =>
      StatusCode.InternalServerError ->
        ApiError(error = "Cortex IO error", detail = Some(detail), hint = None)
    case CortexFailure.IndexInvalid(detail) =>
      StatusCode.InternalServerError ->
        ApiError(error = "Cortex index is invalid", detail = Some(detail), hint = None)
    case HelloFailure.GreetingUnavailable(detail) =>
      StatusCode.ServiceUnavailable ->
        ApiError(error = "Greeting unavailable", detail = detail, hint = None)
    case HelloFailure.RecentUnavailable(detail) =>
      StatusCode.ServiceUnavailable ->
        ApiError(error = "Recent calls unavailable", detail = detail, hint = None)
