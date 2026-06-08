package codefolio.server

import codefolio.server.config.AppConfig
import codefolio.server.http.StaticRoutes
import zio.*
import zio.http.*

/**
 * Thin HTTP-binding layer for the static portfolio: a trivial `/api/health` (for the Kubernetes probe) plus
 * the production static-file server (the Vite bundle + the SPA index.html fallback). No databases, no API
 * surface beyond health.
 */
trait HttpApp:
  def serve: Task[Unit]

object HttpApp:

  val live: ZLayer[AppConfig, Nothing, HttpApp] =
    ZLayer.fromFunction(HttpAppLive(_))

final private class HttpAppLive(cfg: AppConfig) extends HttpApp:

  // Trivial health endpoint for the Kubernetes startup/readiness probe. Plain zio-http (no tapir): the
  // portfolio derives no endpoints from an OpenAPI spec, so a hand-written route keeps the dependency
  // surface minimal.
  private val healthRoutes: Routes[Any, Response] = Routes(
    Method.GET / "api" / "health" -> handler(Response.json("""{"status":"ok"}"""))
  )

  private val staticRoutes = StaticRoutes.from(cfg.staticDir)

  override def serve: Task[Unit] =
    ZIO.logInfo(s"Starting server on port ${cfg.port}; ${staticRoutes.startupInfo}") *>
      Server
        .serve(healthRoutes ++ staticRoutes.routes)
        .provide(
          ZLayer.succeed(Server.Config.default.port(cfg.port)),
          Server.live
        )
        .unit
