package codefolio.client.api

import codefolio.shared.api.Endpoints
import codefolio.shared.api.Endpoints.{Greeting, RecentCalls}
import sttp.client3.{FetchBackend, SttpBackend, UriContext}
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext

object ApiClient:

  private given ExecutionContext = JSExecutionContext.queue

  private val backend: SttpBackend[Future, Any] = FetchBackend()
  // Same-origin: in dev the Vite proxy forwards /api/* to the server,
  // in prod the same server serves both API and statics.
  private val baseUri: Option[Uri] = Some(uri"")

  private val helloRequest: Unit => sttp.client3.Request[Either[Unit, Greeting], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getHello, baseUri)

  private val recentRequest: Unit => sttp.client3.Request[Either[Unit, RecentCalls], Any] =
    SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.getRecent, baseUri)

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
