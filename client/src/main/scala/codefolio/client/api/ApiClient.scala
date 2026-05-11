package codefolio.client.api

import codefolio.shared.api.Endpoints
import codefolio.shared.api.Endpoints.{
  BlogIndex,
  BlogPostPayload,
  ChapterPayload,
  CortexIndex,
  Greeting,
  RecentCalls,
  RunRequest,
  RunResponse
}
import sttp.client3.{FetchBackend, Request, SttpBackend}
import sttp.model.{StatusCode, Uri}
import sttp.tapir.client.sttp.SttpClientInterpreter

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext

/**
 * Typed HTTP client built **from the same tapir endpoints** the server implements. Single source of truth =
 * `api/openapi.yaml` → codegen → `Endpoints.scala` → consumed here.
 *
 * Each public method (e.g. [[runCode]]) follows the same shape:
 *   1. `SttpClientInterpreter().toRequestThrowDecodeFailures(endpoint, baseUri)` gives a function `Request =>
 *      sttp.client3.Request[Either[Unit, Response], Any]`. 2. `backend.send(req)` runs the request through
 *      the browser's `fetch`. 3. The `Either[Unit, Response]` body becomes a `Future[Response]` — Right =
 *      success, Left = the server returned a non-2xx that tapir doesn't know how to interpret as the success
 *      type.
 *
 * The base URI is intentionally `None` (relative URLs). See the comment on `baseUri` below — `Some(uri"")`
 * would crash at runtime.
 */
object ApiClient:

  private given ExecutionContext = JSExecutionContext.queue

  private val backend: SttpBackend[Future, Any] = FetchBackend()
  // Same-origin: emit relative URIs (`/api/hello`) and let the browser's
  // FetchBackend resolve them against `window.location.origin`. In dev the
  // Vite proxy forwards `/api/*` to :8080; in prod the same server serves
  // both API and statics. (sttp's `uri""` interpolator rejects the empty
  // string at runtime, so we can't use it as a placeholder base.)
  private val baseUri: Option[Uri] = None

  private val helloRequest: Unit => sttp.client3.Request[Either[Unit, Greeting], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getHello, baseUri)

  private val recentRequest: Unit => sttp.client3.Request[Either[Unit, RecentCalls], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getRecent, baseUri)

  private val runRequest: RunRequest => Request[Either[Endpoints.ApiError, RunResponse], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.runCode, baseUri)

  private val cortexIndexRequest: Unit => Request[Either[Endpoints.ApiError, CortexIndex], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getCortexIndex, baseUri)

  private val cortexChapterRequest
      : ((String, String)) => Request[Either[Endpoints.ApiError, ChapterPayload], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getCortexChapter, baseUri)

  private val blogIndexRequest: Unit => Request[Either[Endpoints.ApiError, BlogIndex], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getBlogIndex, baseUri)

  private val blogPostRequest: String => Request[Either[Endpoints.ApiError, BlogPostPayload], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getBlogPost, baseUri)

  private def send[I, E, O](
      build: I => Request[Either[E, O], Any],
      input: I,
      failureMessage: (StatusCode, E) => String
  ): Future[O] =
    backend.send(build(input)).flatMap { res =>
      res.body match
        case Right(value) => Future.successful(value)
        case Left(error)  => Future.failed(RuntimeException(failureMessage(res.code, error)))
    }

  private def statusOnly(message: String): (StatusCode, Unit) => String =
    (code, _) => s"$message (${code.code})"

  private def apiError(message: String): (StatusCode, Endpoints.ApiError) => String =
    (code, error) =>
      val detail = error.detail.filter(_.nonEmpty).map(d => s": $d").getOrElse("")
      s"$message (${code.code}): ${error.error}$detail"

  // ---- Hello demo ----------------------------------------------------------

  def getHello: Future[Greeting] =
    send(helloRequest, (), statusOnly("Failed to fetch greeting"))

  def getRecent: Future[RecentCalls] =
    send(recentRequest, (), statusOnly("Failed to fetch recent calls"))

  // ---- Code execution ------------------------------------------------------

  def runCode(req: RunRequest): Future[RunResponse] =
    send(runRequest, req, apiError("Run failed"))

  // ---- Cortex --------------------------------------------------------------

  def getCortexIndex: Future[CortexIndex] =
    send(cortexIndexRequest, (), apiError("Failed to fetch Cortex index"))

  def getCortexChapter(book: String, chapter: String): Future[ChapterPayload] =
    send(cortexChapterRequest, (book, chapter), apiError(s"Failed to fetch chapter $book/$chapter"))

  // ---- Blog ----------------------------------------------------------------

  def getBlogIndex: Future[BlogIndex] =
    send(blogIndexRequest, (), apiError("Failed to fetch blog index"))

  def getBlogPost(slug: String): Future[BlogPostPayload] =
    send(blogPostRequest, slug, apiError(s"Failed to fetch blog post $slug"))
