package codefolio.server.codeRunPipeline

import codefolio.server.codeRunPipeline.Languages.Language
import codefolio.server.config.RunnerConfig
import codefolio.shared.api.Endpoints.{RunRequest, RunResponse, RunResult}
import zio.*

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets

/**
 * Errors that bubble out of `/api/run`. The HTTP layer (`ApiErrors.toHttp`) maps each variant to its proper
 * status code + envelope.
 */
sealed trait RunFailure extends Product with Serializable

object RunFailure:
  /** Bad request (unknown language, missing field). 400. */
  final case class BadInput(error: String, hint: Option[String] = None) extends RunFailure

  /** Payload too large (source > 64 KiB or stdin > 16 KiB). 413. */
  final case class PayloadTooLarge(error: String) extends RunFailure

  /** No execution backend configured. 503. */
  case object NotConfigured extends RunFailure

  /**
   * Backend itself errored (Piston / Code Runner returned 5xx, network failure, etc.). 502.
   */
  final case class BackendFailure(error: String, detail: Option[String] = None) extends RunFailure

/**
 * Internal seam for `/api/run`. CONTEXT.md term: **Code Execution Backend**. Two adapters in production —
 * Piston (remote public service) and Code Runner (local Judge0-compatible container) — plus the test fakes
 * `FakePiston` / `FakeCodeRunner`. Production wires both via [[CodeRunPipeline.live]]; tests inject lists of
 * fakes via [[CodeRunPipeline.from]].
 *
 * The orchestration walks the configured list in priority order and picks the first backend whose
 * `supports(lang)` is true. Code Runner reports `supports = true` unconditionally (universal fallback);
 * Piston reports support only for languages in its protocol map ([[PistonWire.supports]]).
 */
private[codeRunPipeline] trait CodeExecutionBackend:
  def supports(lang: Language): Boolean
  def run(source: String, stdin: Option[String], lang: Language): Task[RunResult]

/**
 * Two-backend pipeline for `/api/run`.
 *
 *   - Validates payload size, resolves the language alias.
 *   - Walks the configured backends in priority order; picks the first one whose `supports(lang)` is true.
 *   - Wraps backend `Throwable`s as `RunFailure.BackendFailure`.
 *
 * Mirrors the ADR-0003 internal-seams pattern: the [[CodeExecutionBackend]] trait is package-private; the
 * only public surface is `run`. Wire-format mapping (JSON ↔ `RunResult`) lives in [[PistonWire]] /
 * [[CodeRunnerWire]] so it can be unit-tested directly.
 */
trait CodeRunPipeline:
  def run(req: RunRequest): IO[RunFailure, RunResponse]

object CodeRunPipeline:

  /**
   * Direct construction from explicit backends in priority order. Used by tests; production wires via
   * [[live]].
   */
  def from(backends: List[CodeExecutionBackend]): CodeRunPipeline =
    CodeRunPipelineLive(backends)

  /**
   * Resource-free layer: builds the configured Piston and Code Runner adapters from `RunnerConfig`. Either
   * may be absent if its URL is unset; if both are unset, the pipeline returns `NotConfigured` per request.
   * Code Runner is listed first so it wins when both backends are configured — the self-hosted runner is
   * preferred because it controls the Java JDK version (needed for `jdk.compiler` tracing) and has a 1 MB
   * stdout cap versus Piston's 64 KB. Piston remains as the fallback if Code Runner is unreachable.
   */
  val live: ZLayer[RunnerConfig, Nothing, CodeRunPipeline] =
    ZLayer.fromFunction { (cfg: RunnerConfig) =>
      CodeRunPipelineLive(liveBackends(cfg))
    }

  // ---------------------------------------------------------------------------
  // Live adapters — thin shells that wrap the protocol-specific wire parsers in
  // a `postJson` HTTP transport. The wire layer (`PistonWire`, `CodeRunnerWire`)
  // owns the JSON ↔ RunResult mapping so it can be unit-tested against golden
  // fixtures without an HTTP server.
  // ---------------------------------------------------------------------------

  private def liveBackends(cfg: RunnerConfig): List[CodeExecutionBackend] =
    val piston: Option[CodeExecutionBackend] =
      cfg.pistonUrl.filter(_.nonEmpty).map(LivePistonBackend(_))
    val codeRunner: Option[CodeExecutionBackend] =
      cfg.codeRunnerUrl
        .filter(_.nonEmpty)
        .map(LiveCodeRunnerBackend(_, cfg.codeRunnerAuthToken.filter(_.nonEmpty)))
    // Code Runner first: preferred over Piston for traced runs (higher stdout cap,
    // controlled JDK version with jdk.compiler). Piston is the fallback.
    List(codeRunner, piston).flatten

  final private class LivePistonBackend(baseUrl: String) extends CodeExecutionBackend:

    override def supports(lang: Language): Boolean = PistonWire.supports(lang)

    override def run(
        source: String,
        stdin: Option[String],
        lang: Language
    ): Task[RunResult] =
      val body = PistonWire.buildRequestBody(source, stdin, lang)
      postJson(baseUrl, "/api/v2/execute", body, PistonWire.parseRunResult, "Piston")

  final private class LiveCodeRunnerBackend(baseUrl: String, authToken: Option[String])
      extends CodeExecutionBackend:

    override def supports(lang: Language): Boolean = true

    override def run(
        source: String,
        stdin: Option[String],
        lang: Language
    ): Task[RunResult] =
      val body         = CodeRunnerWire.buildRequestBody(source, stdin, lang)
      val extraHeaders = authToken.fold(Map.empty[String, String])(t => Map("X-Auth-Token" -> t))
      postJson(
        baseUrl,
        CodeRunnerWire.PathAndQuery,
        body,
        CodeRunnerWire.parseRunResult,
        "Code Runner",
        extraHeaders
      )

  // ---- Shared HTTP plumbing ----------------------------------------------

  // Force HTTP/1.1: piston (and our Code Runner Judge0 image) is Express
  // over plaintext, which doesn't speak HTTP/2. Java HttpClient's default
  // (HTTP_2) sends `Connection: Upgrade, HTTP2-Settings: …` headers on
  // every plaintext POST as part of h2c discovery, and Express's
  // body-parser middleware rejects the upgrade-laden request with a
  // bare-text `400 Bad Request` (no JSON body) before our handler ever
  // sees it. Pinning HTTP/1.1 sidesteps the h2c handshake entirely.
  private val httpClient: HttpClient =
    HttpClient
      .newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(java.time.Duration.ofSeconds(10))
      .build()

  /**
   * POST `payload` (JSON string) to `${baseUrl}${pathAndQuery}` with the standard JSON content type and a
   * 100s timeout, then either parse the response body via `parse` or fail with a `${errorPrefix} returned
   * <status>: <body>` runtime exception. `extraHeaders` lets a backend add per-request auth (e.g. Code
   * Runner's `X-Auth-Token`) without rebuilding the whole request flow.
   *
   * Wraps the entire send + parse in `ZIO.attemptBlocking` so the JDK's blocking HTTP client doesn't pin a
   * platform thread.
   */
  private[codeRunPipeline] def postJson[A](
      baseUrl: String,
      pathAndQuery: String,
      payload: String,
      parse: String => A,
      errorPrefix: String,
      extraHeaders: Map[String, String] = Map.empty
  ): Task[A] =
    ZIO.attemptBlocking {
      val cleanedBase = baseUrl.replaceAll("/+$", "")
      val builder = HttpRequest
        .newBuilder()
        .uri(URI.create(s"$cleanedBase$pathAndQuery"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        // 100 s, not 30: a cold `scala-cli` run in the local Code Runner can
        // outlast 30 s; its own 90 s per-language budget should fire first so
        // the failure reads as a clean TLE rather than an opaque HTTP timeout.
        .timeout(java.time.Duration.ofSeconds(100))
        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
      extraHeaders.foreach { case (k, v) => builder.header(k, v) }
      val resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if resp.statusCode() < 200 || resp.statusCode() >= 300 then
        throw new RuntimeException(s"$errorPrefix returned ${resp.statusCode()}: ${resp.body()}")
      parse(resp.body())
    }

final private class CodeRunPipelineLive(
    backends: List[CodeExecutionBackend]
) extends CodeRunPipeline:

  override def run(req: RunRequest): IO[RunFailure, RunResponse] =
    for
      _ <- validate(req)
      lang <- ZIO
        .fromOption(Languages.resolve(req.language))
        .mapError(_ => RunFailure.BadInput(s"Language '${req.language}' is not runnable"))
      result <- pickAndRun(lang, req)
    yield RunResponse(result = result, language = lang.info)

  private def validate(req: RunRequest): IO[RunFailure, Unit] =
    val sourceBytes = req.source.getBytes(StandardCharsets.UTF_8).length
    val stdinBytes  = req.stdin.fold(0)(_.getBytes(StandardCharsets.UTF_8).length)
    if sourceBytes > Languages.MaxSourceBytes then
      ZIO.fail(RunFailure.PayloadTooLarge("Source exceeds size limit"))
    else if stdinBytes > Languages.MaxStdinBytes then
      ZIO.fail(RunFailure.PayloadTooLarge("stdin exceeds size limit"))
    else ZIO.unit

  /**
   * Walk backends in priority order; pick the first whose `supports(lang)` is true. Empty list →
   * `NotConfigured`; non-empty but no support → `BadInput`. Backend exceptions are wrapped as
   * [[RunFailure.BackendFailure]].
   */
  private def pickAndRun(lang: Language, req: RunRequest): IO[RunFailure, RunResult] =
    if backends.isEmpty then ZIO.fail(RunFailure.NotConfigured)
    else
      backends.find(_.supports(lang)) match
        case Some(b) => b.run(req.source, req.stdin, lang).mapError(toBackendFailure)
        case None =>
          ZIO.fail(
            RunFailure.BadInput(
              s"Language '${lang.label}' is not supported by the configured execution backend."
            )
          )

  private def toBackendFailure(t: Throwable): RunFailure =
    RunFailure.BackendFailure(error = "Code execution failed", detail = Option(t.getMessage))
