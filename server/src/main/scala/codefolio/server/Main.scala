package codefolio.server

import codefolio.server.cache.RedisCache
import codefolio.server.config.AppConfig
import codefolio.server.db.{DataSource, Migrations, VisitsRepo}
import codefolio.server.eventlog.HelloEventLog
import codefolio.server.handlers.HelloHandler
import org.slf4j.bridge.SLF4JBridgeHandler
import zio.*
import zio.config.typesafe.TypesafeConfigProvider

object Main extends ZIOAppDefault:

  // Route java.util.logging (used by Liquibase) through SLF4J/Logback so its
  // INFO records don't get treated as stderr-noise by sbt-revolver.
  locally {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  override def run: ZIO[Any, Throwable, Unit] =
    val program: ZIO[HttpApp & DataSourceEnv, Throwable, Unit] =
      Migrations.run *> ZIO.serviceWithZIO[HttpApp](_.serve)

    program
      .provide(
        AppConfig.live,
        DataSource.live,
        VisitsRepo.live,
        RedisCache.live,
        HelloEventLog.live,
        HelloHandler.live,
        HttpApp.live
      )

  // Type alias kept short to make the `run` signature read cleanly above.
  private type DataSourceEnv = javax.sql.DataSource
