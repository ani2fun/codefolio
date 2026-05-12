package codefolio.server.config

import zio.*
import zio.config.magnolia.deriveConfig

final case class DbConfig(url: String, user: String, password: String)
final case class RedisConfig(url: String, ttlSecs: Int)
final case class MongoConfig(uri: String, database: String)

/**
 * Configuration for the code-execution proxy.
 *
 * `pistonUrl` is for the Piston public service (production); `codeRunnerUrl` points at the local
 * Judge0-API-compatible Code Runner container in dev. Either may be unset; if both are unset the /api/run
 * endpoint returns 503. The handler prefers Piston when both are set and the language is Piston-supported,
 * falling back to Code Runner otherwise.
 *
 * `codeRunnerAuthToken` is an optional `X-Auth-Token` header — kept for parity with what the original
 * portfolio-app exposed even though the local Code Runner image doesn't currently require auth.
 */
final case class RunnerConfig(
    pistonUrl: Option[String],
    codeRunnerUrl: Option[String],
    codeRunnerAuthToken: Option[String]
)

/**
 * Upstream URL for the LikeC4 reverse proxy that fronts `/c4/`. Defaults to the in-cluster Kubernetes Service
 * name; override with `LIKEC4_URL` for local dev (`http://localhost:8090`) or docker compose
 * (`http://likec4:8080`).
 */
final case class LikeC4Config(upstreamUrl: String)

/**
 * Path to the Cortex content tree. Default is `./content/cortex` relative to the working directory (matches
 * the layout shipped with the Docker image, where the Dockerfile copies content/ to /app/content). The
 * structure is fully convention-driven: each immediate subdirectory is a book; nested directories become
 * sections; `.md` files are chapters. Optional `book.json` / `_section.json` provide titles and metadata.
 *
 * `autoReload` checks the maximum mtime of files under the root on every index request and rebuilds the
 * cached index if anything changed. Cheap (~10ms at 200 chapters), and means you can drop a new directory and
 * refresh the page without restarting the server. Disable in prod where content ships baked-in.
 */
final case class CortexConfig(root: String, autoReload: Boolean)

/**
 * Path to the blog content tree. Default `./content/blogs` relative to the working directory. Each immediate
 * `*.md` file under the root is a single post; the slug is the filename without `.md`. `autoReload` checks
 * the maximum mtime of files under the root on every index request and rebuilds the cached index if anything
 * changed — same pattern as Cortex. Disable in prod by setting `BLOG_AUTO_RELOAD=false`.
 */
final case class BlogConfig(root: String, autoReload: Boolean)

final case class AppConfig(
    port: Int,
    staticDir: String,
    db: DbConfig,
    redis: RedisConfig,
    mongo: MongoConfig,
    runner: RunnerConfig,
    likec4: LikeC4Config,
    cortex: CortexConfig,
    blog: BlogConfig
)

object AppConfig:

  val config: Config[AppConfig] = deriveConfig[AppConfig].nested("codefolio")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(ZIO.config(config))
