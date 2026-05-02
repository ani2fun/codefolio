package codefolio.server.cache

import codefolio.server.config.AppConfig
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.{RedisClient, SetArgs}
import zio.*

/** Tiny string-keyed cache backed by Lettuce. Wraps the async Lettuce API in
  * `ZIO.fromCompletionStage` so callers get `Task` everywhere. */
trait RedisCache:
  def get(key: String): Task[Option[String]]
  def setWithTtl(key: String, value: String, ttl: Duration): Task[Unit]
  def ping: Task[Boolean]

object RedisCache:

  /** Resource-safe layer: builds a Lettuce client + connection, releases both
    * in reverse order on shutdown. */
  val live: ZLayer[AppConfig, Throwable, RedisCache] =
    ZLayer.scoped {
      for
        cfg        <- ZIO.service[AppConfig]
        client     <- ZIO.acquireRelease(
                        ZIO.attemptBlocking(RedisClient.create(cfg.redis.url))
                      )(c => ZIO.attempt(c.shutdown()).orDie)
        connection <- ZIO.acquireRelease(
                        ZIO.attemptBlocking(client.connect())
                      )(c => ZIO.attempt(c.close()).orDie)
      yield RedisCacheLive(connection)
    }

private final class RedisCacheLive(conn: StatefulRedisConnection[String, String]) extends RedisCache:

  private val async = conn.async()

  override def get(key: String): Task[Option[String]] =
    ZIO.fromCompletionStage(async.get(key).toCompletableFuture).map(Option(_))

  override def setWithTtl(key: String, value: String, ttl: Duration): Task[Unit] =
    val args = SetArgs.Builder.ex(ttl.toSeconds)
    ZIO.fromCompletionStage(async.set(key, value, args).toCompletableFuture).unit

  override def ping: Task[Boolean] =
    ZIO
      .fromCompletionStage(async.ping().toCompletableFuture)
      .map(_ == "PONG")
      .catchAll(_ => ZIO.succeed(false))
