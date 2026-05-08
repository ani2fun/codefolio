package codefolio.server.http

import codefolio.shared.AppRoutes
import zio.*
import zio.http.*

import java.io.File

final case class StaticRoutes(routes: Routes[Any, Response], startupInfo: String)

object StaticRoutes:

  def from(staticDir: String): StaticRoutes =
    val resolvedStaticDir = File(staticDir).getAbsoluteFile
    val staticDirExists   = resolvedStaticDir.isDirectory
    val staticRootPath =
      if staticDirExists then resolvedStaticDir.toPath.toRealPath()
      else resolvedStaticDir.toPath

    def staticIndex: ZIO[Any, Nothing, Response] =
      serveStaticFile(File(resolvedStaticDir, AppRoutes.IndexHtml))

    def serveStaticFile(file: File): ZIO[Any, Nothing, Response] =
      ZIO
        .attemptBlockingIO(file.exists() && file.isFile())
        .flatMap {
          case true =>
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

    def serveUnder(prefix: String, rel: String): ZIO[Any, Nothing, Response] =
      val cleaned = rel.stripPrefix("/")
      if cleaned.isEmpty then ZIO.succeed(Response.status(Status.NotFound))
      else
        val candidate = File(resolvedStaticDir, s"$prefix/$cleaned")
        val candidateReal =
          try Some(candidate.toPath.toRealPath())
          catch case _: Throwable => None
        candidateReal match
          case Some(path) if path.startsWith(staticRootPath) && candidate.isFile =>
            serveStaticFile(candidate)
          case _ => ZIO.succeed(Response.status(Status.NotFound))

    def trailingFileHandler(prefix: String): Handler[Any, Response, (zio.http.Path, Request), Response] =
      Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (path, _) =>
        serveUnder(prefix, path.encode)
      }

    val routes =
      if !staticDirExists then Routes.empty
      else
        Routes(
          // Vite emits index.html and assets/*; serve them verbatim.
          Method.GET / Root                        -> handler(staticIndex),
          Method.GET / AppRoutes.IndexHtml         -> handler(staticIndex),
          Method.GET / AppRoutes.Assets / trailing -> trailingFileHandler(AppRoutes.Assets),
          // Files copied from client/public/ at the dist root.
          Method.GET / AppRoutes.Images / trailing -> trailingFileHandler(AppRoutes.Images),
          Method.GET / AppRoutes.Certificates / trailing ->
            trailingFileHandler(AppRoutes.Certificates),
          Method.GET / AppRoutes.CvFile -> handler(
            serveStaticFile(File(resolvedStaticDir, AppRoutes.CvFile))
          ),
          // SPA fallback for known top-level routes. A hard reload of e.g.
          // /cortex/distributed-systems/introduction must return index.html
          // so the client-side router can re-resolve the page.
          Method.GET / AppRoutes.Demo   -> handler(staticIndex),
          Method.GET / AppRoutes.Cortex -> handler(staticIndex),
          Method.GET / AppRoutes.Cortex / trailing ->
            Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (_, _) => staticIndex },
          Method.GET / AppRoutes.Blogs -> handler(staticIndex),
          Method.GET / AppRoutes.Blogs / trailing ->
            Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (_, _) => staticIndex }
          // NOTE: we deliberately don't have a catch-all `/ trailing`
          // wildcard here. zio-http's combined routing makes a wildcard
          // greedy enough to swallow all GETs, and even moving tapir
          // ahead in the concat doesn't restore the precedence — so an
          // SPA fallback wildcard would shadow tapir's `/api/health`,
          // `/api/hello`, `/api/recent`, `/api/cortex/*`, and `/docs`.
          // Typo'd URLs returning 404 is the lesser evil.
        )

    val startupInfo =
      if staticDirExists then s"serving frontend from $resolvedStaticDir"
      else s"no frontend bundle at $resolvedStaticDir (dev mode — Vite serves the UI on :5173)"

    StaticRoutes(routes, startupInfo)

  /**
   * Map a filename's extension to a sensible Content-Type. The browser refuses to load `.js` ESM modules with
   * no/wrong type ("Failed to fetch dynamically imported module"), and woff/woff2 files trigger CORS warnings
   * without a `font/...` type, so we cover everything Vite emits.
   */
  private def contentTypeFor(name: String): String =
    val lower = name.toLowerCase
    if lower.endsWith(".js") || lower.endsWith(".mjs") then "application/javascript; charset=utf-8"
    else if lower.endsWith(".css") then "text/css; charset=utf-8"
    else if lower.endsWith(".html") || lower.endsWith(".htm") then "text/html; charset=utf-8"
    else if lower.endsWith(".json") then "application/json; charset=utf-8"
    else if lower.endsWith(".svg") then "image/svg+xml"
    else if lower.endsWith(".png") then "image/png"
    else if lower.endsWith(".jpg") || lower.endsWith(".jpeg") then "image/jpeg"
    else if lower.endsWith(".webp") then "image/webp"
    else if lower.endsWith(".gif") then "image/gif"
    else if lower.endsWith(".ico") then "image/x-icon"
    else if lower.endsWith(".woff2") then "font/woff2"
    else if lower.endsWith(".woff") then "font/woff"
    else if lower.endsWith(".ttf") then "font/ttf"
    else if lower.endsWith(".otf") then "font/otf"
    else if lower.endsWith(".pdf") then "application/pdf"
    else if lower.endsWith(".wasm") then "application/wasm"
    else if lower.endsWith(".map") then "application/json; charset=utf-8"
    else if lower.endsWith(".txt") then "text/plain; charset=utf-8"
    else "application/octet-stream"
