package codefolio.server

import codefolio.server.config.AppConfig
import codefolio.server.handlers.{
  CodeRunHandler,
  HelloHandler,
  KnowledgeFailure,
  KnowledgeHandler,
  RunFailure
}
import codefolio.shared.api.Endpoints
import codefolio.shared.api.Endpoints.ApiError
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.*

import java.io.File

/** The HTTP-binding layer.
  *
  * Three things compose into the server's request handling:
  *
  *   1. **API endpoints (tapir)** — typed `ZServerEndpoint`s built from the
  *      codegen'd `Endpoints` values. Each `xxxEndpoint` below pairs a
  *      generated description with a handler call and an error mapping.
  *      The full list goes into `apiEndpoints`, then through
  *      `ZioHttpInterpreter` to become `Routes[Any, Response]`.
  *
  *   2. **Static-file routes** — index.html, `/assets/`, `/img/`, etc.
  *      Mounted only when `STATIC_DIR` actually exists on disk (so the
  *      dev server doesn't shadow Vite's :5173).
  *
  *   3. **SPA fallback** — known top-level client routes (/knowledge,
  *      /demo, /blogs, ...) all serve `index.html` so a hard reload of
  *      a deep-link works. We list them explicitly rather than using a
  *      single wildcard — see the long comment further down for why.
  *
  * If you're adding an endpoint, the recipe is in the Onboarding book
  * (`Extending the Project`, Recipe 2). The short version: define the
  * endpoint in `api/openapi.yaml`, write a handler, then add a
  * `ZServerEndpoint` here and append it to `apiEndpoints`.
  */
trait HttpApp:
  def serve: Task[Unit]

object HttpApp:

  val live: ZLayer[AppConfig & HelloHandler & CodeRunHandler & KnowledgeHandler, Nothing, HttpApp] =
    ZLayer.fromFunction(HttpAppLive(_, _, _, _))

final private class HttpAppLive(
    cfg: AppConfig,
    hello: HelloHandler,
    codeRun: CodeRunHandler,
    knowledge: KnowledgeHandler
) extends HttpApp:

  // ApiError isn't referenced from any endpoint in the OpenAPI spec yet, so
  // the codegen doesn't emit circe codecs for it. We supply them locally —
  // tapir's `jsonBody[ApiError]` below needs both an encoder and a decoder.
  private given io.circe.Encoder[ApiError] = deriveEncoder
  private given io.circe.Decoder[ApiError] = deriveDecoder

  // ---- API + Swagger UI (tapir) -------------------------------------------

  private val RecentLimit = 10

  private val helloEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getHello.zServerLogic(_ => hello.hello.orDie)

  private val recentEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getRecent.zServerLogic(_ => hello.recent(RecentLimit).orDie)

  private val healthEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getHealth.zServerLogic(_ => hello.health.orDie)

  // /api/run is the only endpoint with structured errors. Tapir's `.errorOut`
  // on top of the codegen'd endpoint pairs a HTTP status code with an
  // `ApiError` JSON body — the handler returns either side cleanly.
  private val runEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.runCode
      .errorOut(statusCode and jsonBody[codefolio.shared.api.Endpoints.ApiError])
      .zServerLogic { req =>
        codeRun.run(req).mapError { failure =>
          val status = failure match
            case RunFailure.BadInput(_, _)        => StatusCode.BadRequest
            case RunFailure.PayloadTooLarge(_)    => StatusCode.PayloadTooLarge
            case RunFailure.NotConfigured         => StatusCode.ServiceUnavailable
            case RunFailure.BackendFailure(_, _)  => StatusCode.BadGateway
          (status, RunFailure.toApiError(failure))
        }
      }

  // Knowledge endpoints. Errors map to 404 (not found / traversal) or 500
  // (IO / corrupt index). All pair the status code with an ApiError body
  // for consistency with /api/run.
  private val knowledgeIndexEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getKnowledgeIndex
      .errorOut(statusCode and jsonBody[ApiError])
      .zServerLogic { _ =>
        knowledge.index.mapError(failureToApiError)
      }

  private val knowledgeChapterEndpoint: ZServerEndpoint[Any, Any] =
    Endpoints.getKnowledgeChapter
      .errorOut(statusCode and jsonBody[ApiError])
      .zServerLogic { case (book, chapter) =>
        knowledge.chapter(book, chapter).mapError(failureToApiError)
      }

  private def failureToApiError(f: KnowledgeFailure): (StatusCode, ApiError) = f match
    case KnowledgeFailure.NotFound =>
      (StatusCode.NotFound, ApiError(error = "Not found", detail = None, hint = None))
    case KnowledgeFailure.IO(detail) =>
      (StatusCode.InternalServerError,
       ApiError(error = "Knowledge IO error", detail = Some(detail), hint = None))
    case KnowledgeFailure.IndexInvalid(detail) =>
      (StatusCode.InternalServerError,
       ApiError(error = "Knowledge index is invalid", detail = Some(detail), hint = None))

  private val apiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(
      helloEndpoint,
      recentEndpoint,
      healthEndpoint,
      runEndpoint,
      knowledgeIndexEndpoint,
      knowledgeChapterEndpoint
    )

  private val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](apiEndpoints, "Codefolio API", "0.1.0")

  private val tapirRoutes: Routes[Any, Response] =
    ZioHttpInterpreter().toHttp(apiEndpoints ++ swaggerEndpoints)

  // ---- Static-file serving (Vite production bundle) -----------------------
  //
  // In production (Docker), `cfg.staticDir` (e.g. /app/static) contains the
  // Vite output. Vite copies the `client/public/` tree to the dist root, so
  // the layout on disk is roughly:
  //   index.html
  //   assets/index-XXX.{js,css}
  //   img/...
  //   certificates/...
  //   Aniket-Kakde-CV-EN.pdf
  //
  // In development the dir typically does not exist (Vite's dev server is
  // serving the frontend on :5173), so the static routes are not mounted at
  // all — that way the Vite-bound /api proxy still reaches tapir without a
  // catch-all wildcard shadowing it.
  //
  // We deliberately enumerate the static-file *prefixes* and the SPA
  // top-level routes explicitly rather than using a single `Method.GET /
  // trailing` catch-all. zio-http's route table doesn't reliably resolve
  // tapir's specific routes ahead of a sibling top-level wildcard, so the
  // wildcard would shadow `/api/hello` and friends. Listing prefixes keeps
  // dispatch unambiguous: tapir owns `/api/*` and `/docs/*`; this block
  // owns the static file tree and the known SPA paths.

  private val resolvedStaticDir: File  = File(cfg.staticDir).getAbsoluteFile
  private val staticRootPath: java.nio.file.Path =
    if resolvedStaticDir.isDirectory then resolvedStaticDir.toPath.toRealPath()
    else resolvedStaticDir.toPath
  private val staticDirExists: Boolean = resolvedStaticDir.isDirectory

  private def staticIndex: ZIO[Any, Nothing, Response] =
    serveStaticFile(File(resolvedStaticDir, "index.html"))

  /** Map a filename's extension to a sensible Content-Type. The browser
    * refuses to load `.js` ESM modules with no/wrong type ("Failed to fetch
    * dynamically imported module"), and woff/woff2 files trigger CORS
    * warnings without a `font/...` type, so we cover everything Vite emits.
    */
  private def contentTypeFor(name: String): String =
    val lower = name.toLowerCase
    if lower.endsWith(".js") || lower.endsWith(".mjs")  then "application/javascript; charset=utf-8"
    else if lower.endsWith(".css")                       then "text/css; charset=utf-8"
    else if lower.endsWith(".html") || lower.endsWith(".htm") then "text/html; charset=utf-8"
    else if lower.endsWith(".json")                      then "application/json; charset=utf-8"
    else if lower.endsWith(".svg")                       then "image/svg+xml"
    else if lower.endsWith(".png")                       then "image/png"
    else if lower.endsWith(".jpg") || lower.endsWith(".jpeg") then "image/jpeg"
    else if lower.endsWith(".webp")                      then "image/webp"
    else if lower.endsWith(".gif")                       then "image/gif"
    else if lower.endsWith(".ico")                       then "image/x-icon"
    else if lower.endsWith(".woff2")                     then "font/woff2"
    else if lower.endsWith(".woff")                      then "font/woff"
    else if lower.endsWith(".ttf")                       then "font/ttf"
    else if lower.endsWith(".otf")                       then "font/otf"
    else if lower.endsWith(".pdf")                       then "application/pdf"
    else if lower.endsWith(".wasm")                      then "application/wasm"
    else if lower.endsWith(".map")                       then "application/json; charset=utf-8"
    else if lower.endsWith(".txt")                       then "text/plain; charset=utf-8"
    else "application/octet-stream"

  private def serveStaticFile(file: File): ZIO[Any, Nothing, Response] =
    ZIO
      .attemptBlockingIO(file.exists() && file.isFile())
      .flatMap {
        case true  =>
          Body.fromFile(file).map { b =>
            Response(
              status = Status.Ok,
              headers = Headers(Header.ContentType.parse(contentTypeFor(file.getName)).toOption.toList*),
              body = b
            )
          }
        case false => ZIO.succeed(Response.status(Status.NotFound))
      }
      .orDie

  /** Serve a path-traversal-safe file from a known sub-tree of staticDir. */
  private def serveUnder(prefix: String, rel: String): ZIO[Any, Nothing, Response] =
    val cleaned = rel.stripPrefix("/")
    if cleaned.isEmpty then ZIO.succeed(Response.status(Status.NotFound))
    else
      val candidate = File(resolvedStaticDir, s"$prefix/$cleaned")
      val candReal: Option[java.nio.file.Path] =
        try Some(candidate.toPath.toRealPath())
        catch case _: Throwable => None
      candReal match
        case Some(c) if c.startsWith(staticRootPath) && candidate.isFile =>
          serveStaticFile(candidate)
        case _ => ZIO.succeed(Response.status(Status.NotFound))

  // Static sub-tree handler: matches `<prefix>/<rest>` and serves the file.
  private def trailingFileHandler(prefix: String): Handler[Any, Response, (zio.http.Path, Request), Response] =
    Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (path, _) =>
      serveUnder(prefix, path.encode)
    }

  private val staticRoutes: Routes[Any, Response] =
    if !staticDirExists then Routes.empty
    else
      Routes(
        // Vite emits index.html and assets/*; serve them verbatim.
        Method.GET / Root                          -> handler(staticIndex),
        Method.GET / "index.html"                  -> handler(staticIndex),
        Method.GET / "assets"      / trailing      -> trailingFileHandler("assets"),
        // Files copied from client/public/ at the dist root.
        Method.GET / "img"         / trailing      -> trailingFileHandler("img"),
        Method.GET / "certificates"/ trailing      -> trailingFileHandler("certificates"),
        Method.GET / "Aniket-Kakde-CV-EN.pdf"      -> handler(
          serveStaticFile(File(resolvedStaticDir, "Aniket-Kakde-CV-EN.pdf"))
        ),
        // SPA fallback for known top-level routes. A hard reload of e.g.
        // /knowledge/distributed-systems/introduction must return index.html
        // so the client-side router can re-resolve the page.
        Method.GET / "demo"                        -> handler(staticIndex),
        Method.GET / "knowledge"                   -> handler(staticIndex),
        Method.GET / "knowledge" / trailing        ->
          Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (_, _) => staticIndex },
        Method.GET / "blogs"                       -> handler(staticIndex),
        Method.GET / "blogs"     / trailing        ->
          Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (_, _) => staticIndex }
        // NOTE: we deliberately don't have a catch-all `/ trailing`
        // wildcard here. zio-http's combined routing makes a wildcard
        // greedy enough to swallow all GETs, and even moving tapir
        // ahead in the concat doesn't restore the precedence — so an
        // SPA fallback wildcard would shadow tapir's `/api/health`,
        // `/api/hello`, `/api/recent`, `/api/knowledge/*`, and `/docs`.
        // Typo'd URLs returning 404 is the lesser evil.
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
