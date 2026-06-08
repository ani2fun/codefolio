package codefolio.server

import codefolio.server.config.AppConfig
import zio.*
import zio.config.typesafe.TypesafeConfigProvider

/**
 * Server entry point for the static portfolio.
 *
 * The portfolio has no databases and no auth: the server serves the Vite-built SPA bundle from `staticDir`
 * plus a trivial `/api/health` endpoint for the Kubernetes probe (see [[HttpApp]]). Config (`port`,
 * `staticDir`) comes from `application.conf` with env-var overrides.
 *
 * `provide(...)` wires the dependency layers ZIO assembles into a DAG; a missing service is a compile error
 * before the process starts.
 */
object Main extends ZIOAppDefault:

  // ZIO's `Config` API reads from a `ConfigProvider`. The default uses env vars only; override it to read
  // `application.conf` first and let env vars layer over it. `AppConfig.live` materialises the typed case
  // class via this provider.
  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  override def run: ZIO[Any, Throwable, Unit] =
    ZIO.serviceWithZIO[HttpApp](_.serve).provide(AppConfig.live, HttpApp.live)
