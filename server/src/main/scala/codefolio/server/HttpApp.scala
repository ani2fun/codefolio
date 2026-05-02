package codefolio.server

import codefolio.server.config.AppConfig
import codefolio.server.handlers.HelloHandler
import codefolio.shared.api.Endpoints
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.*

import java.io.File

trait HttpApp:
  def serve: Task[Unit]

object HttpApp:

  val live: ZLayer[AppConfig & HelloHandler, Nothing, HttpApp] =
    ZLayer.fromFunction(HttpAppLive(_, _))

final private class HttpAppLive(cfg: AppConfig, hello: HelloHandler) extends HttpApp:

  // ---- API + Swagger UI (tapir) -------------------------------------------

  private val RecentLimit = 10

  private val helloEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getHello.zServerLogic(_ => hello.hello.orDie)

  private val recentEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getRecent.zServerLogic(_ => hello.recent(RecentLimit).orDie)

  private val healthEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getHealth.zServerLogic(_ => hello.health.orDie)

  private val apiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(helloEndpoint, recentEndpoint, healthEndpoint)

  private val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](apiEndpoints, "Codefolio API", "0.1.0")

  private val tapirRoutes: Routes[Any, Response] =
    ZioHttpInterpreter().toHttp(apiEndpoints ++ swaggerEndpoints)

  // ---- Static-file serving (Vite production bundle) -----------------------
  //
  // In production (Docker), `cfg.staticDir` (e.g. /app/static) contains the
  // Vite output:
  //   index.html
  //   assets/index-XXX.js
  //   assets/index-XXX.css
  //
  // In development the dir typically does not exist (Vite's dev server is
  // serving the frontend on :5173), so these routes are not mounted at all
  // — that way the Vite-bound /api proxy still reaches tapir without a
  // catch-all wildcard shadowing it.
  //
  // The patterns below are *intentionally narrow* — they must not overlap
  // with tapir's `/api/*` and `/docs/*` routes.

  private def serveFile(name: String): ZIO[Any, Nothing, Response] =
    val file = File(cfg.staticDir, name)
    ZIO
      .attemptBlockingIO(file.exists() && file.isFile())
      .flatMap {
        case true  => Body.fromFile(file).map(b => Response(body = b))
        case false => ZIO.succeed(Response.status(Status.NotFound))
      }
      .orDie

  private val resolvedStaticDir: File  = File(cfg.staticDir).getAbsoluteFile
  private val staticDirExists: Boolean = resolvedStaticDir.isDirectory

  private val staticRoutes: Routes[Any, Response] =
    if !staticDirExists then Routes.empty
    else
      Routes(
        Method.GET / Root         -> handler(serveFile("index.html")),
        Method.GET / "index.html" -> handler(serveFile("index.html")),
        Method.GET / "assets" / string("name") ->
          Handler.fromFunctionZIO[(String, Request)] { case (name, _) =>
            serveFile(s"assets/$name")
          }
      )

  // ---- compose & serve ----------------------------------------------------

  override def serve: Task[Unit] =
    val staticInfo =
      if staticDirExists then s"serving frontend from $resolvedStaticDir"
      else s"no frontend bundle at $resolvedStaticDir (dev mode — Vite serves the UI on :5173)"
    ZIO.logInfo(s"Starting server on port ${cfg.port}; $staticInfo") *>
      Server
        .serve(tapirRoutes ++ staticRoutes)
        .provide(
          ZLayer.succeed(Server.Config.default.port(cfg.port)),
          Server.live
        )
        .unit
