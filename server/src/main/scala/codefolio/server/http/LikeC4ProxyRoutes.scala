package codefolio.server.http

import zio.*
import zio.http.*

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration

/**
 * Reverse-proxies `GET` requests under `/c4/` to the in-cluster LikeC4 service.
 *
 * LikeC4 ships as a static SPA built with `--base /c4/`, so its index.html references `/c4/assets/...` paths.
 * The browser hits codefolio at `kakde.eu/c4/...`; codefolio forwards those requests in-cluster to the
 * `likec4` Service. There is no public Ingress on the LikeC4 deployment.
 *
 * Posture mirrors piston: ClusterIP only + NetworkPolicy in apps-prod permitting codefolio → likec4 on port
 * 8080.
 *
 * v1 demo notes:
 *   - Upstream URL is hardcoded. If we ever need to override per environment, promote to AppConfig (same
 *     pattern as `RunnerConfig.pistonUrl`).
 *   - Forwards only the body bytes + Content-Type. Cache-Control / ETag are dropped to keep the pass-through
 *     simple; we can layer them in later.
 */
object LikeC4ProxyRoutes:

  private val UpstreamBaseUrl = "http://likec4"

  private val httpClient: HttpClient =
    HttpClient
      .newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(5))
      .build()

  def routes: Routes[Any, Response] =
    Routes(
      Method.GET / "c4" / trailing ->
        Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (rest, req) =>
          proxy(buildUpstreamUrl(rest, req))
        }
    )

  private def buildUpstreamUrl(rest: zio.http.Path, req: Request): String =
    val query    = req.url.queryParams.encode
    val querySep = if query.nonEmpty then "?" else ""
    s"$UpstreamBaseUrl${buildUpstreamPath(rest.encode)}$querySep$query"

  /**
   * Build the upstream path under `/c4/`. `trailing` captures the segments after `/c4` without a leading
   * slash (so `/c4/view/index` arrives as `"view/index"`), but we defensively strip a slash regardless and
   * always insert the join slash ourselves. Exposed (package-private) so `LikeC4ProxyRoutesSpec` can pin the
   * behaviour without spinning up an HTTP server.
   */
  private[http] def buildUpstreamPath(restEncoded: String): String =
    s"/c4/${restEncoded.stripPrefix("/")}"

  private def proxy(upstreamUrl: String): UIO[Response] =
    ZIO
      .attemptBlocking {
        val request = HttpRequest
          .newBuilder()
          .uri(URI.create(upstreamUrl))
          .GET()
          .timeout(Duration.ofSeconds(15))
          .build()
        val upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val status   = Status.fromInt(upstream.statusCode())
        val contentType =
          Option(upstream.headers().firstValue("content-type").orElse(null))
            .filter(_.nonEmpty)
            .getOrElse("application/octet-stream")
        Response(
          status = status,
          headers = Headers(Header.ContentType.parse(contentType).toOption.toList*),
          body = Body.fromArray(upstream.body())
        )
      }
      .catchAll(_ => ZIO.succeed(Response.status(Status.BadGateway)))
