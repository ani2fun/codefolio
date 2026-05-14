package codefolio.server.http

import codefolio.server.blogPipeline.BlogPipeline
import codefolio.server.codeRunPipeline.CodeRunPipeline
import codefolio.server.cortexPipeline.CortexPipeline
import codefolio.server.helloPipeline.HelloPipeline
import codefolio.shared.api.Endpoints
import codefolio.shared.api.Endpoints.{
  ApiError,
  BlogIndex,
  BlogPostPayload,
  ChapterPayload,
  CortexIndex,
  Greeting,
  RecentCalls,
  RunRequest,
  RunResponse
}
import codefolio.shared.api.EndpointsJsonSerdes.*
import codefolio.shared.api.EndpointsSchemas.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.json.circe.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.{Response, Routes}

object ApiRoutes:

  private val RecentLimit = 10

  def routes(
      helloPipeline: HelloPipeline,
      codeRun: CodeRunPipeline,
      cortex: CortexPipeline,
      blog: BlogPipeline
  ): Routes[Any, Response] =
    val endpoints = serverEndpoints(helloPipeline, codeRun, cortex, blog)
    val swaggerEndpoints =
      SwaggerInterpreter()
        .fromServerEndpoints[Task](endpoints, "Codefolio API", "0.1.0")

    ZioHttpInterpreter().toHttp(endpoints ++ swaggerEndpoints)

  // The uniform error output for every fallible endpoint: an HTTP status code chosen at runtime plus a
  // JSON `ApiError` envelope.
  private val apiErrorOut = statusCode and jsonBody[ApiError]

  /**
   * Wire a derived endpoint to its pipeline logic: attach the uniform `(StatusCode, ApiError)` error output
   * and the `HandlerFailure → ApiErrors.toHttp` mapping in one place. An endpoint built through this helper
   * cannot ship without its error plumbing — the only way to produce a `ZServerEndpoint` here is to hand over
   * logic typed as `I => IO[HandlerFailure, O]`, and the helper always applies `errorOut` + `mapError`.
   */
  private def handlerEndpoint[I, O](
      base: PublicEndpoint[I, Unit, O, Any]
  )(logic: I => IO[ApiErrors.HandlerFailure, O]): ZServerEndpoint[Any, Any] =
    base
      .errorOut(apiErrorOut)
      .zServerLogic(input => logic(input).mapError(ApiErrors.toHttp))

  private def serverEndpoints(
      helloPipeline: HelloPipeline,
      codeRun: CodeRunPipeline,
      cortex: CortexPipeline,
      blog: BlogPipeline
  ): List[ZServerEndpoint[Any, Any]] =

    // Endpoints are defined here rather than reusing the generated `Endpoints.*` values directly so the
    // server can choose the status code at runtime while still using generated request/response case
    // classes and codecs. `handlerEndpoint` bundles the error wiring so it can't be forgotten. Health
    // stays on the generated endpoint because it never fails (`UIO[HealthStatus]` — degradation is part
    // of the success body, not the error channel).

    val helloEndpoint = handlerEndpoint(
      endpoint.get.in("api" / "hello").out(jsonBody[Greeting])
    )(_ => helloPipeline.greet)

    val recentEndpoint = handlerEndpoint(
      endpoint.get.in("api" / "recent").out(jsonBody[RecentCalls])
    )(_ => helloPipeline.recent(RecentLimit))

    val healthEndpoint: ZServerEndpoint[Any, Any] =
      Endpoints.getHealth.zServerLogic(_ => helloPipeline.health)

    val runEndpoint = handlerEndpoint(
      endpoint.post.in("api" / "run").in(jsonBody[RunRequest]).out(jsonBody[RunResponse])
    )(req => codeRun.run(req))

    val cortexIndexEndpoint = handlerEndpoint(
      endpoint.get.in("api" / "cortex" / "index").out(jsonBody[CortexIndex])
    )(_ => cortex.index)

    val cortexChapterEndpoint = handlerEndpoint(
      endpoint.get
        .in("api" / "cortex" / path[String]("book") / path[String]("chapter"))
        .out(jsonBody[ChapterPayload])
    ) { case (book, chapter) => cortex.chapter(book, chapter) }

    val blogIndexEndpoint = handlerEndpoint(
      endpoint.get.in("api" / "blogs" / "index").out(jsonBody[BlogIndex])
    )(_ => blog.index)

    val blogPostEndpoint = handlerEndpoint(
      endpoint.get.in("api" / "blogs" / path[String]("slug")).out(jsonBody[BlogPostPayload])
    )(slug => blog.post(slug))

    List(
      helloEndpoint,
      recentEndpoint,
      healthEndpoint,
      runEndpoint,
      cortexIndexEndpoint,
      cortexChapterEndpoint,
      blogIndexEndpoint,
      blogPostEndpoint
    )
