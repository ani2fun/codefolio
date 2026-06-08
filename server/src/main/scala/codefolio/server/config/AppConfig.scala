package codefolio.server.config

import zio.*
import zio.config.magnolia.deriveConfig

/**
 * Configuration for the static portfolio server. The portfolio has no databases and no auth — it serves the
 * Vite-built SPA bundle from `staticDir` and a trivial `/api/health` endpoint, so the only knobs are the
 * listen `port` and the `staticDir`. Both are read from the `codefolio { … }` block of `application.conf`
 * with env-var overrides (`PORT`, `STATIC_DIR`).
 */
final case class AppConfig(port: Int, staticDir: String)

object AppConfig:

  val config: Config[AppConfig] = deriveConfig[AppConfig].nested("codefolio")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(ZIO.config(config))
