package codefolio.client.api

import codefolio.shared.api.Endpoints
import codefolio.shared.api.Endpoints.{
  ChapterPayload,
  CortexIndex,
  Greeting,
  RecentCalls,
  RunRequest,
  RunResponse
}
import sttp.client3.{FetchBackend, SttpBackend}
import sttp.model.Uri
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

  private val runRequest: RunRequest => sttp.client3.Request[Either[Unit, RunResponse], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.runCode, baseUri)

  private val cortexIndexRequest: Unit => sttp.client3.Request[Either[Unit, CortexIndex], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getCortexIndex, baseUri)

  private val cortexChapterRequest
      : ((String, String)) => sttp.client3.Request[Either[Unit, ChapterPayload], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getCortexChapter, baseUri)

  // ---- Hello demo ----------------------------------------------------------

  def getHello: Future[Greeting] =
    backend.send(helloRequest(())).flatMap { res =>
      res.body match
        case Right(g) => Future.successful(g)
        case Left(_)  => Future.failed(RuntimeException("Failed to fetch greeting"))
    }

  def getRecent: Future[RecentCalls] =
    backend.send(recentRequest(())).flatMap { res =>
      res.body match
        case Right(r) => Future.successful(r)
        case Left(_)  => Future.failed(RuntimeException("Failed to fetch recent calls"))
    }

  // ---- Code execution ------------------------------------------------------

  def runCode(req: RunRequest): Future[RunResponse] =
    backend.send(runRequest(req)).flatMap { res =>
      res.body match
        case Right(r) => Future.successful(r)
        case Left(_)  => Future.failed(RuntimeException(s"Run failed (${res.code.code})"))
    }

  // ---- Cortex --------------------------------------------------------------

  def getCortexIndex: Future[CortexIndex] =
    backend.send(cortexIndexRequest(())).flatMap { res =>
      res.body match
        case Right(idx) => Future.successful(idx)
        case Left(_) =>
          Future.failed(RuntimeException(s"Failed to fetch Cortex index (${res.code.code})"))
    }

  def getCortexChapter(book: String, chapter: String): Future[ChapterPayload] =
    backend.send(cortexChapterRequest((book, chapter))).flatMap { res =>
      res.body match
        case Right(payload) => Future.successful(payload)
        case Left(_) =>
          Future.failed(
            RuntimeException(s"Failed to fetch chapter $book/$chapter (${res.code.code})")
          )
    }
