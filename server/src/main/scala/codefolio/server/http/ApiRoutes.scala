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

  private def serverEndpoints(
      helloPipeline: HelloPipeline,
      codeRun: CodeRunPipeline,
      cortex: CortexPipeline,
      blog: BlogPipeline
  ): List[ZServerEndpoint[Any, Any]] =

    val apiErrorOut = statusCode and jsonBody[ApiError]

    // Hello, recent, run, and cortex endpoints are defined manually instead of directly reusing
    // the generated values so the server can choose the status code at runtime while still using
    // generated request/response case classes and codecs. Health stays on the generated endpoint
    // because it never fails (`UIO[HealthStatus]` — degradation is part of the success body).
    val helloEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "hello")
        .errorOut(apiErrorOut)
        .out(jsonBody[Greeting])
        .zServerLogic { _ =>
          helloPipeline.greet.mapError(ApiErrors.toHttp)
        }

    val recentEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "recent")
        .errorOut(apiErrorOut)
        .out(jsonBody[RecentCalls])
        .zServerLogic { _ =>
          helloPipeline.recent(RecentLimit).mapError(ApiErrors.toHttp)
        }

    val healthEndpoint: ZServerEndpoint[Any, Any] =
      Endpoints.getHealth.zServerLogic(_ => helloPipeline.health)

    val runEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.post
        .in("api" / "run")
        .in(jsonBody[RunRequest])
        .errorOut(apiErrorOut)
        .out(jsonBody[RunResponse])
        .zServerLogic { req =>
          codeRun.run(req).mapError(ApiErrors.toHttp)
        }

    val cortexIndexEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "cortex" / "index")
        .errorOut(apiErrorOut)
        .out(jsonBody[CortexIndex])
        .zServerLogic { _ =>
          cortex.index.mapError(ApiErrors.toHttp)
        }

    val cortexChapterEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "cortex" / path[String]("book") / path[String]("chapter"))
        .errorOut(apiErrorOut)
        .out(jsonBody[ChapterPayload])
        .zServerLogic { case (book, chapter) =>
          cortex.chapter(book, chapter).mapError(ApiErrors.toHttp)
        }

    val blogIndexEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "blogs" / "index")
        .errorOut(apiErrorOut)
        .out(jsonBody[BlogIndex])
        .zServerLogic { _ =>
          blog.index.mapError(ApiErrors.toHttp)
        }

    val blogPostEndpoint: ZServerEndpoint[Any, Any] =
      endpoint.get
        .in("api" / "blogs" / path[String]("slug"))
        .errorOut(apiErrorOut)
        .out(jsonBody[BlogPostPayload])
        .zServerLogic { slug =>
          blog.post(slug).mapError(ApiErrors.toHttp)
        }

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
