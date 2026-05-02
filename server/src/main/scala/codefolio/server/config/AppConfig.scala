package codefolio.server.config

import zio.*
import zio.config.magnolia.deriveConfig

final case class DbConfig(url: String, user: String, password: String)
final case class RedisConfig(url: String, ttlSecs: Int)
final case class MongoConfig(uri: String, database: String)

final case class AppConfig(
    port: Int,
    staticDir: String,
    db: DbConfig,
    redis: RedisConfig,
    mongo: MongoConfig
)

object AppConfig:

  val config: Config[AppConfig] = deriveConfig[AppConfig].nested("codefolio")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(ZIO.config(config))
