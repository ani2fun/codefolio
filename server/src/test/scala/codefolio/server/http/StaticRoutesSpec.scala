package codefolio.server.http

import codefolio.shared.AppRoutes
import zio.*
import zio.http.*
import zio.test.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

/**
 * Tests the production static-file server for the single-page portfolio. `/` (and the fixed asset routes)
 * serve index.html / files; the portfolio has no SPA sub-routes (`AppRoutes.SpaRoutes` is empty), so a deep
 * unknown path falls through to 404, and no routes mount at all when the dist directory is absent (dev mode).
 */
object StaticRoutesSpec extends ZIOSpecDefault:

  private val IndexMarker = "SPA-INDEX-MARKER"

  override def spec: Spec[Any, Throwable] = suite("StaticRoutes")(
    test("serves index.html at the root") {
      ZIO.scoped {
        for
          dir  <- tempDist
          res  <- runGet(StaticRoutes.from(dir.toString).routes, "/")
          body <- res.body.asString
        yield assertTrue(res.status == Status.Ok, body.contains(IndexMarker))
      }
    },
    test("the portfolio declares no extra top-level SPA routes") {
      assertTrue(AppRoutes.SpaRoutes.isEmpty)
    },
    test("an unknown deep path is not served (no SPA fallback)") {
      ZIO.scoped {
        for
          dir <- tempDist
          res <- runGet(StaticRoutes.from(dir.toString).routes, "/anything/deep")
        yield assertTrue(res.status == Status.NotFound)
      }
    },
    test("mounts no routes when the dist directory is absent (dev mode)") {
      for
        tmp <- ZIO.attempt(Files.createTempDirectory("codefolio-staticroutes-gone-"))
        _   <- ZIO.attempt(Files.delete(tmp))
        sr = StaticRoutes.from(tmp.toString)
        res <- runGet(sr.routes, "/")
      yield assertTrue(res.status == Status.NotFound, sr.startupInfo.contains("dev mode"))
    }
  )

  // StaticRoutes' handlers never fail (they produce a Response directly), and `runZIO` turns an unmatched
  // route into a 404 Response in the success channel — so this is an infallible effect. `runZIO` allocates
  // request-scoped resources, so it runs inside its own `ZIO.scoped`.
  private def runGet(routes: Routes[Any, Response], path: String): ZIO[Any, Nothing, Response] =
    ZIO.scoped(routes.runZIO(get(path)))

  private def get(path: String): Request =
    Request.get(
      URL.decode(path).toOption.getOrElse(throw new IllegalArgumentException(s"bad path: $path"))
    )

  private val tempDist: ZIO[Scope, Throwable, Path] =
    ZIO.acquireRelease(
      ZIO.attempt {
        val dir = Files.createTempDirectory("codefolio-staticroutes-")
        Files.writeString(
          dir.resolve(AppRoutes.IndexHtml),
          s"<!doctype html><title>$IndexMarker</title>",
          StandardCharsets.UTF_8
        )
        dir
      }
    )(dir => ZIO.attempt(deleteRecursively(dir)).orDie)

  private def deleteRecursively(root: Path): Unit =
    if Files.exists(root) then
      val paths = Files.walk(root)
      try paths.iterator().asScala.toList.reverse.foreach(path => Files.deleteIfExists(path))
      finally paths.close()
