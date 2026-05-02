package codefolio.server.handlers

import codefolio.server.cache.RedisCache
import codefolio.server.config.AppConfig
import codefolio.server.db.VisitsRepo
import codefolio.server.eventlog.HelloEventLog
import codefolio.shared.api.Endpoints.{Greeting, HealthStatus, HelloEvent, RecentCalls}
import codefolio.shared.api.EndpointsJsonSerdes.given
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import zio.*

import javax.sql.DataSource as JDataSource

trait HelloHandler:
  /** Greet the caller, threading through Postgres → Redis → MongoDB. */
  def hello: Task[Greeting]

  /** Most recent calls to /api/hello, read from MongoDB (newest first). */
  def recent(limit: Int): Task[RecentCalls]

  /** Liveness across all three stores. */
  def health: Task[HealthStatus]

object HelloHandler:

  val live: ZLayer[AppConfig & VisitsRepo & RedisCache & HelloEventLog & JDataSource, Nothing, HelloHandler] =
    ZLayer.fromFunction(HelloHandlerLive(_, _, _, _, _))

final private class HelloHandlerLive(
    cfg: AppConfig,
    visits: VisitsRepo,
    cache: RedisCache,
    eventLog: HelloEventLog,
    ds: JDataSource
) extends HelloHandler:

  private val CacheKey = "codefolio:greeting:latest"

  /**
   * Try Redis first; on miss, increment Postgres and refresh the cache. Either way append a record to the
   * MongoDB event log.
   */
  override def hello: Task[Greeting] =
    val readFromCache: Task[Option[Greeting]] =
      cache.get(CacheKey).flatMap {
        case Some(json) =>
          ZIO
            .fromEither(decode[Greeting](json))
            .map(_.copy(cached = true))
            .map(Some(_))
            .catchAll(_ => ZIO.none) // ignore malformed cache entries
        case None => ZIO.none
      }

    val coldRead: Task[Greeting] =
      for
        count <- visits.incrementAndGet
        fresh = Greeting(message = "Hello from ZIO + Postgres", visits = count, cached = false)
        // Cache the canonical (cached=false) form; we flip the flag on read.
        _ <- cache
          .setWithTtl(CacheKey, fresh.asJson.noSpaces, cfg.redis.ttlSecs.seconds)
          .catchAll(e => ZIO.logWarning(s"Redis SET failed: ${e.getMessage}"))
      yield fresh

    for
      maybeCached <- readFromCache.catchAll(e =>
        ZIO.logWarning(s"Redis GET failed: ${e.getMessage}").as(None)
      )
      greeting <- maybeCached match
        case Some(g) => ZIO.succeed(g)
        case None    => coldRead
      _ <- eventLog
        .append(HelloEvent(java.lang.System.currentTimeMillis(), greeting.visits))
        .catchAll(e => ZIO.logWarning(s"Mongo append failed: ${e.getMessage}"))
    yield greeting

  override def recent(limit: Int): Task[RecentCalls] =
    eventLog.recent(limit).map(RecentCalls(_))

  override def health: Task[HealthStatus] =
    for
      pg <- ZIO
        .attemptBlocking {
          val conn = ds.getConnection
          try conn.isValid(2)
          finally conn.close()
        }
        .catchAll(_ => ZIO.succeed(false))
      rd <- cache.ping.catchAll(_ => ZIO.succeed(false))
      mg <- eventLog.ping.catchAll(_ => ZIO.succeed(false))
      ok = pg && rd && mg
    yield HealthStatus(
      status = if ok then "ok" else "degraded",
      postgres = pg,
      redis = rd,
      mongo = mg
    )
