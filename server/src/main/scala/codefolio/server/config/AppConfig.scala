package codefolio.server.config

import zio.*
import zio.config.magnolia.deriveConfig

final case class DbConfig(url: String, user: String, password: String)
final case class RedisConfig(url: String, ttlSecs: Int)
final case class MongoConfig(uri: String, database: String)

/** Configuration for the code-execution proxy.
  *
  * `pistonUrl` is for the Piston public service (production); `codeRunnerUrl`
  * points at the local Judge0-API-compatible Code Runner container in dev.
  * Either may be unset; if both are unset the /api/run endpoint returns
  * 503. The handler prefers Piston when both are set and the language is
  * Piston-supported, falling back to Code Runner otherwise.
  *
  * `codeRunnerAuthToken` is an optional `X-Auth-Token` header — kept for
  * parity with what the original portfolio-app exposed even though the
  * local Code Runner image doesn't currently require auth.
  */
final case class RunnerConfig(
    pistonUrl: Option[String],
    codeRunnerUrl: Option[String],
    codeRunnerAuthToken: Option[String]
)

/** Path to the knowledge-base content tree. Default is `./content/knowledge`
  * relative to the working directory (matches the layout shipped with the
  * Docker image, where the Dockerfile copies content/ to /app/content). The
  * root contains a `meta.json` listing each book; each book directory has
  * its own `meta.json` + chapter `.md` files.
  */
final case class KnowledgeConfig(root: String)

final case class AppConfig(
    port: Int,
    staticDir: String,
    db: DbConfig,
    redis: RedisConfig,
    mongo: MongoConfig,
    runner: RunnerConfig,
    knowledge: KnowledgeConfig
)

object AppConfig:

  val config: Config[AppConfig] = deriveConfig[AppConfig].nested("codefolio")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(ZIO.config(config))
