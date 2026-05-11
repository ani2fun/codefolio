package codefolio.server.http

import zio.*
import zio.http.*

import java.io.File
import java.nio.file.Path as NioPath

/**
 * Serves binary assets from the Cortex content tree.
 *
 * URL: `GET /api/cortex/asset/{book}/{rest...}` → file at `${cortexRoot}/{book}/{rest}`.
 *
 * Why under `/api`? The Vite dev server proxies `/api → :8080` already, so the same URL
 * works in both `bin/dev` (Vite at :5173 proxying to the JVM at :8080) and in production
 * (single JVM at :8080 fronting both API and static frontend). Putting the asset route
 * under `/cortex/...` would require an extra Vite proxy rule and would collide with the
 * SPA chapter routes that are intentionally swallowed by index.html.
 *
 * Path-safety: the resolved real path must remain inside `cortexRoot`. Symlinks that
 * escape, `..` traversals, and non-files all 404.
 */
object CortexAssetRoutes:

  def from(cortexRoot: String): Routes[Any, Response] =
    val resolvedRoot = File(cortexRoot).getAbsoluteFile
    val rootExists   = resolvedRoot.isDirectory
    val rootRealPath: NioPath =
      if rootExists then resolvedRoot.toPath.toRealPath()
      else resolvedRoot.toPath

    def serveAsset(rel: String): ZIO[Any, Nothing, Response] =
      val cleaned = rel.stripPrefix("/")
      if !rootExists || cleaned.isEmpty then ZIO.succeed(Response.status(Status.NotFound))
      else
        val candidate = File(resolvedRoot, cleaned)
        val candidateReal =
          try Some(candidate.toPath.toRealPath())
          catch case _: Throwable => None
        candidateReal match
          case Some(p) if p.startsWith(rootRealPath) && candidate.isFile =>
            serveStaticFile(candidate)
          case _ => ZIO.succeed(Response.status(Status.NotFound))

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

    Routes(
      Method.GET / "api" / "cortex" / "asset" / trailing ->
        Handler.fromFunctionZIO[(zio.http.Path, Request)] { case (rest, _) =>
          serveAsset(rest.encode)
        }
    )

  /** Same content-type table StaticRoutes uses — keeps SVG / PNG / WOFF / etc. served correctly. */
  private def contentTypeFor(name: String): String =
    val lower = name.toLowerCase
    if lower.endsWith(".svg") then "image/svg+xml"
    else if lower.endsWith(".png") then "image/png"
    else if lower.endsWith(".jpg") || lower.endsWith(".jpeg") then "image/jpeg"
    else if lower.endsWith(".webp") then "image/webp"
    else if lower.endsWith(".gif") then "image/gif"
    else if lower.endsWith(".ico") then "image/x-icon"
    else if lower.endsWith(".pdf") then "application/pdf"
    else if lower.endsWith(".txt") || lower.endsWith(".md") then "text/plain; charset=utf-8"
    else if lower.endsWith(".json") then "application/json; charset=utf-8"
    else if lower.endsWith(".mmd") || lower.endsWith(".d2") || lower.endsWith(".dsl") || lower.endsWith(".puml") then
      "text/plain; charset=utf-8"
    else "application/octet-stream"
