package codefolio.server.http

import zio.test.*

object LikeC4ProxyRoutesSpec extends ZIOSpecDefault:

  override def spec: Spec[Any, Nothing] = suite("LikeC4ProxyRoutes")(
    test("buildUpstreamPath joins /c4/ to the captured rest regardless of leading slash") {
      assertTrue(
        // Empty trailing path (e.g. GET /c4) maps to the upstream index.
        LikeC4ProxyRoutes.buildUpstreamPath("") == "/c4/",
        // Root-only trailing path same as empty.
        LikeC4ProxyRoutes.buildUpstreamPath("/") == "/c4/",
        // SPA route. Before the slash bug fix this produced /c4view/index
        // which nginx returned 404 for.
        LikeC4ProxyRoutes.buildUpstreamPath("view/index") == "/c4/view/index",
        LikeC4ProxyRoutes.buildUpstreamPath("/view/index") == "/c4/view/index",
        // Static asset fetched by the SPA's index.html.
        LikeC4ProxyRoutes.buildUpstreamPath("assets/index-Blavf7FJ.js") ==
          "/c4/assets/index-Blavf7FJ.js",
        LikeC4ProxyRoutes.buildUpstreamPath("/assets/index-Blavf7FJ.js") ==
          "/c4/assets/index-Blavf7FJ.js"
      )
    }
  )
