package codefolio.server

import codefolio.server.cache.RedisCache
import codefolio.server.config.AppConfig
import codefolio.server.db.{DataSource, Migrations, VisitsRepo}
import codefolio.server.eventlog.HelloEventLog
import codefolio.server.handlers.{CodeRunHandler, CortexHandler, HelloHandler}
import org.slf4j.bridge.SLF4JBridgeHandler
import zio.*
import zio.config.typesafe.TypesafeConfigProvider

/**
 * Server entry point.
 *
 * The flow at a glance:
 *
 * Main.run │ ├── bootstrap: install Typesafe config provider so `application.conf` │ (and env-var overrides)
 * feed `AppConfig.live`. │ ├── Migrations.run (Liquibase against Postgres) │ └── HttpApp.serve (binds the
 * port, blocks until shutdown)
 *
 * `provide(...)` wires the **dependency layers** that produce each service from raw config + the JVM. ZIO
 * assembles them into a DAG; if any layer is missing the compiler complains with a "missing service" error
 * before the process ever starts. Order in the `provide` list does not matter.
 *
 * If you add a new service:
 *   1. Define it as `trait MyThing` + `object MyThing { val live: ZLayer[..., MyThing] }`. 2. Append
 *      `MyThing.live` to the `provide` list below. 3. Inject it into whichever layer needs it (typically
 *      `HttpApp.live`).
 */
object Main extends ZIOAppDefault:

  // Liquibase logs through `java.util.logging`; without this bridge those
  // records bypass our Logback config and sbt-revolver flags them as stderr
  // noise. `locally { ... }` runs at class load before any ZIO machinery.
  locally {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  // ZIO's `Config` API reads from a `ConfigProvider`. The default uses
  // env vars only; we override to pick up `application.conf` first and let
  // env vars layer over it. `AppConfig.live` then materialises the typed
  // case class via this provider.
  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  override def run: ZIO[Any, Throwable, Unit] =
    // Run schema migrations *before* binding the HTTP port — if they fail
    // we'd rather crash on boot than serve traffic against a stale schema.
    val program: ZIO[HttpApp & DataSourceEnv, Throwable, Unit] =
      Migrations.run *> ZIO.serviceWithZIO[HttpApp](_.serve)

    program
      .provide(
        AppConfig.live,      // reads `application.conf` + env vars
        DataSource.live,     // HikariCP pool over Postgres
        VisitsRepo.live,     // Hello demo: row updates + counter reads
        RedisCache.live,     // Hello demo: 10s TTL cache for /api/hello
        HelloEventLog.live,  // Hello demo: append-only Mongo log
        HelloHandler.live,   // /api/hello, /api/recent, /api/health
        CodeRunHandler.live, // /api/run (Piston / Code Runner)
        CortexHandler.live,  // /api/cortex/*
        HttpApp.live         // tapir + zio-http + static + SPA fallback
      )

  // Type alias kept short to make the `run` signature read cleanly above.
  // (Migrations.run requires a DataSource; we surface that requirement
  // here so the `provide` list resolves cleanly.)
  private type DataSourceEnv = javax.sql.DataSource
