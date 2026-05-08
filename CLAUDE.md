# CLAUDE.md — Codefolio

> Persistent project context for Claude Code. **This file is gitignored.**

## What this is

`codefolio` is a personal portfolio / homelab fullstack web app, written entirely in Scala.

- **Frontend:** Scala 3 → Scala.js → React (via `scalajs-react`), bundled with Vite, styled with Tailwind v4.
- **Backend:** Scala 3 → ZIO 2 → zio-http, fronting **three** backing stores:
  - **Postgres** — persistent visit counter (Liquibase migrations, plain JDBC + HikariCP)
  - **Redis (Lettuce)** — read-through cache for the `/api/hello` payload, ~10s TTL
  - **MongoDB (sync driver)** — append-only event log of every `/api/hello`, surfaced via `/api/recent`
- **API contract:** OpenAPI 3.0 in `api/openapi.yaml` is the single source of truth. The `sbt-openapi-codegen` plugin emits tapir endpoint descriptions and case classes into the cross-compiled `shared` module. Both server and client compile against the generated code, so a schema change breaks compilation on both sides until they're updated.
- **Author:** sole-author repo. **Do not add `Co-Authored-By:` trailers to commits.**

## Stack at a glance

| Layer | Tech |
|---|---|
| Build tool | sbt 1.10.7 |
| Language | Scala 3.6.x |
| Backend runtime | ZIO 2.1.x + zio-http 3.x |
| API codegen | `sbt-openapi-codegen` (1.11.22) → tapir endpoints + case classes in `codefolio.shared.api.Endpoints` |
| Server endpoint interpreter | `tapir-zio-http-server` (with `tapir-zio` for `zServerLogic` / `ZServerEndpoint`) |
| Client endpoint interpreter | `tapir-sttp-client` + `sttp-client3` `FetchBackend` (Scala.js) |
| API docs UI | `tapir-swagger-ui-bundle`, mounted at `/docs/` |
| JSON | circe (chosen because the codegen plugin doesn't support zio-json) |
| Config | zio-config + typesafe HOCON; root key is `codefolio` |
| DB | Postgres + plain JDBC + HikariCP |
| Cache | Redis via `io.lettuce:lettuce-core` 6.5.x (async, wrapped with `ZIO.fromCompletionStage`) |
| Event log | MongoDB via `org.mongodb:mongodb-driver-sync` 5.2.x (blocking, wrapped with `ZIO.attemptBlocking`) |
| Migrations | Liquibase 4.x, YAML master changelog including SQL changesets |
| Logging | Logback + JUL→SLF4J bridge so Liquibase logs aren't tagged `[ERROR]` |
| ScalaJS | 1.17+ via `sbt-scalajs` 1.21.0 |
| React binding | `scalajs-react` 3.0.x |
| Bundler | Vite 6 + `@scala-js/vite-plugin-scalajs` (configured with `cwd: ".."`) |
| CSS | Tailwind v4 (`@tailwindcss/vite`) |
| Server packaging | sbt-native-packager `JavaAppPackaging` |
| Tests | zio-test |
| Formatting | scalafmt (`.scalafmt.conf`, Scala 3 dialect, 110-col); `sbt-scalafmt` plugin |
| Tool pinning | `.tool-versions` (asdf/mise) — Java 21, sbt 1.10.7, Node 20 |
| Editor | `.editorconfig` — LF, 2-space, UTF-8 |
| License | MIT (`LICENSE`) |
| CI | GitHub Actions — `scalafmtCheckAll` + `sbt test` + Vite build + `docker build` smoke |

### Notable substitutions vs the original plan

- **circe instead of zio-json** — `sbt-openapi-codegen` only emits codecs for circe / jsoniter, so the entire stack goes through circe. The generated case classes carry circe `Encoder`/`Decoder` instances under `codefolio.shared.api.EndpointsJsonSerdes`.
- **Plain JDBC instead of zio-jdbc** — zio-jdbc 0.1.x is archived AND collides with zio-http 3 on `zio-schema`. We use HikariCP for the pool and write `PreparedStatement`s directly in repos. Cheap and works fine for the skeleton.
- **Lettuce + Mongo sync driver instead of zio-redis / a reactive Mongo wrapper** — keeps the codebase coherent with the same "Java client + ZIO blocking/CompletionStage wrapper" pattern used for Postgres. Easy to swap for ZIO-native libraries later if needed.
- **`Greeting` lives at `codefolio.shared.api.Endpoints.Greeting`** — codegen nests case classes inside the `Endpoints` object, so import as `import codefolio.shared.api.Endpoints.{Greeting, HealthStatus}`. The codecs are at `codefolio.shared.api.EndpointsJsonSerdes`.

## Directory contract

```
codefolio/
├── CONTEXT.md              # ★ project domain glossary (Cortex, Greeting, Handler Failure, …)
├── api/openapi.yaml        # ★ source of truth for the API
├── docs/adr/               # architecture decision records
│   ├── 0001-cortex-content-errors-are-lenient-by-default.md
│   ├── 0002-non-critical-store-failures-degrade-silently.md
│   ├── 0003-hello-pipeline-internal-seams.md
│   └── 0004-wire-adapters-and-unified-backends.md
├── shared/                 # cross-compiled (JVM + JS); codegen lands in src_managed
│   └── src/main/scala/codefolio/shared/
│       ├── runner/         # CodeExecutor (pure Idle→Running→Done state machine)
│       ├── cortex/         # SidebarForest (pure flat-list-to-tree builder)
│       └── api/            # generated tapir endpoints + case classes (Endpoints.*)
├── server/                 # ZIO + zio-http + tapir + Postgres/Redis/MongoDB
│   └── src/main/scala/codefolio/server/
│       ├── db/             # DataSource, Migrations (Postgres infra)
│       ├── helloPipeline/  # /api/hello,/recent,/health — one deep module with internal
│       │                   # Visits/GreetingCache/EventLog seams (ADR-0003)
│       ├── codeRunPipeline/# /api/run — one deep module with a single CodeExecutionBackend
│       │                   # seam (Piston + Code Runner adapters), pure PistonWire/
│       │                   # CodeRunnerWire wire-format modules, public RunFailure ADT,
│       │                   # and Languages table (ADR-0004)
│       ├── cortexPipeline/ # /api/cortex/* — one deep module with an internal CortexFs
│       │                   # seam (FS scan only), mtime cache, payload assembly; the
│       │                   # pure CortexIndexWalker is invoked by the pipeline, not
│       │                   # behind the seam (ADR-0004); CortexFailure is the
│       │                   # seam's error type
│       ├── http/           # ApiRoutes + ApiErrors.toHttp(HandlerFailure union)
│       ├── config/         # AppConfig + per-store config types
│       └── HttpApp.scala   # tapir → zio-http binding
├── client/                 # Scala.js + scalajs-react + Vite + Tailwind
│   └── src/main/scala/codefolio/client/
│       └── components/cortex/
│           ├── ChapterPlaceholders.scala  # catalog of interactive block descriptors
│           └── …                          # thin React renderers over shared logic
├── bin/dev                 # one-command dev launcher (db+redis+mongo + sbt + Vite)
├── Dockerfile              # multi-stage: sbt + node build → JRE runtime
├── docker-compose.yml      # services: db (postgres), redis, mongo, app
└── .github/workflows/ci.yml
```

## Common commands

| Goal | Command |
|---|---|
| **One-command dev mode** | **`./bin/dev`** (db+redis+mongo + sbt watcher + Vite, with port pre-flight + cleanup trap) |
| Compile everything | `sbt compile` |
| Run all tests | `sbt test` |
| Format / format-check | `sbt scalafmtAll` / `sbt scalafmtCheckAll scalafmtSbtCheck` |
| Run server once | `sbt server/run` |
| Run server with auto-restart | `sbt "~server/reStart"` |
| Stop a backgrounded server | `sbt server/reStop` |
| Recompile client JS on change | `sbt "~client/fastLinkJS"` |
| One-shot optimised client JS | `sbt client/fullLinkJS` |
| Vite dev server (HMR) | `cd client && npm install && npm run dev` |
| Production frontend bundle | `cd client && npm run build` |
| Stage server launcher | `sbt server/Universal/stage` |
| Full stack in Docker | `docker compose up --build` |
| Backing stores only (local dev) | `docker compose up db redis mongo` |
| `psql` shell | `docker compose exec db psql -U codefolio` |
| `redis-cli` shell | `docker compose exec redis redis-cli` |
| `mongosh` shell | `docker compose exec mongo mongosh codefolio` |
| Wipe all volumes | `docker compose down -v` |

Open the running stack at:

| URL | What |
|---|---|
| <http://localhost:5173> | Frontend with Vite HMR (proxies `/api` and `/docs` to :8080) |
| <http://localhost:8080> | ZIO server (API + static fallback in prod) |
| <http://localhost:8080/docs/> | Swagger UI |
| <http://localhost:8080/api/hello> | Returns `{message, visits, cached}` — read-through Redis (10s TTL) over the Postgres counter; also appended to MongoDB |
| <http://localhost:8080/api/recent> | `{entries: [{timestampEpochMs, visits}, …]}` — last 10 calls from MongoDB, newest first |
| <http://localhost:8080/api/health> | `{status, postgres, redis, mongo}` — pings each store |

## Demo flow (the three-store integration)

`HelloHandler.hello` is the canonical example for how the skeleton uses each store. Per request:

1. **Read** from Redis (`codefolio:greeting:latest`). Hit → return with `cached=true`.
2. **Miss** → increment `visits.count` in Postgres, build `Greeting`, write it back to Redis with TTL = `codefolio.redis.ttlSecs` (default 10).
3. **Always** append a `HelloEvent { timestampEpochMs, visits }` to the MongoDB `hello_events` collection (a descending-`timestampEpochMs` index is created at startup so `/api/recent` is cheap).

Failures in Redis or Mongo are caught and *logged-then-ignored* — `/api/hello` still returns a value as long as Postgres is reachable. This is intentional: the cache and the event log are non-critical from the request's point of view.

## API-first workflow

1. Edit `api/openapi.yaml`.
2. Run `sbt sharedJVM/compile` — codegen runs and produces updated `Endpoints` / case classes under `shared/.{jvm,js}/target/scala-3.x/src_managed/main/`.
3. The compiler will flag mismatched usages in `server/` (`HelloHandler`, `HttpApp`) and `client/` (`ApiClient`) — fix both.
4. Tests in `shared/src/test/` run against the generated types.

## Gotchas / things future-me will trip on

- **Cortex content is filesystem-driven — no manifests.** Each immediate subdirectory of `content/cortex/` is a book; nested directories become collapsible sidebar sections (capped at depth 6); `.md` files are chapters. Ordering uses numeric prefixes (`01-foo`, `02-bar`); the prefix is stripped before display. Optional escape hatches: `book.json` at the book root for title/description/tags, `_section.json` per directory for a pretty section title, YAML frontmatter (`title`, `summary`) per chapter. Slugs are derived from path joined with `-` (e.g. `01-data-structures/02-arrays/03-traversal.md` → `data-structures-arrays-traversal`); the handler asserts uniqueness and surfaces collisions as `IndexInvalid`. Do **not** reintroduce `meta.json` files — having two sources of truth is exactly what this layout exists to avoid. The dev server auto-reloads via mtime watermark (`CORTEX_AUTO_RELOAD=true`); prod ships with the cache filled at first request. See [CortexHandler.scala](server/src/main/scala/codefolio/server/handlers/CortexHandler.scala).

- **Tailwind v4: `container` lost auto-centering.** The v3 `theme.container.center: true` knob is gone in v4. Plain `container` is now `width: 100%` + breakpoint max-widths with **no `margin: auto`**, so `<main className="container">` goes left-flush. Restored in [tailwind.css](client/tailwind.css) via `@utility container { margin-inline: auto; }` — every `container` callsite (`HomePage`, `CortexIndexPage`, `Header`, `Footer`, …) relies on this rule, do not remove it.

- **v4 `@apply` rejects unknown utilities.** v3 silently dropped them; v4 errors at the Vite plugin and serves a blank page. The non-utilities I had to scrub during the BEM refactor: `text-2sm` (typo for `text-sm`), `items-left` (should be `items-start`), `custom-icon` (a hook class with no CSS), and the marker classes `group` and `container` (zero CSS — they must stay literal in markup, not be `@apply`'d). When extracting an inline className into a `.bem` class, run a quick eyeball over the utility names; if a class isn't valid Tailwind, the whole stylesheet fails to compile.

- **BEM blocks live in `client/src/styles/{sections,components}/*.css`** and are imported from [tailwind.css](client/tailwind.css). Each block uses the v3 layer model: utilities applied at the call site (`^.className := "experience"`) win over `@apply`'d defaults inside `@layer components`. Modifier classes (`--active`, `--highlight`, `--last`) layer on top of the base via the same mechanism. Per-instance values (dynamic colors, gridArea, inline `style`) stay inline.

- **Route ordering matters.** Don't put a `Method.GET / trailing` catch-all anywhere in the static-routes set — it shadows the specific tapir routes for `/api/*` and `/docs/*` and you'll get HTML/404 instead of JSON/Swagger. The static handler in [HttpApp.scala](server/src/main/scala/codefolio/server/HttpApp.scala) uses *only* specific patterns:
  ```scala
  Method.GET / Root                      // → index.html
  Method.GET / "index.html"              // → index.html
  Method.GET / "assets" / string("name") // → assets/<name>
  ```
  And the entire `staticRoutes` set is replaced with `Routes.empty` when `client/dist` doesn't exist (dev mode).

- **`sbt run` / `reStart` working directory** is set to the build root via:
  ```scala
  Compile / run / baseDirectory := (LocalRootProject / baseDirectory).value
  reStart      / baseDirectory  := (LocalRootProject / baseDirectory).value
  ```
  Without this, the JVM's cwd defaults to `server/` and the `./client/dist` static path doesn't resolve.

- **Stale forked JVMs on :8080.** `sbt server/run` forks; `kill $sbt_pid` kills sbt but leaves the child JVM holding port 8080. `bin/dev` has a port pre-flight that detects and kills these. If you're not using `bin/dev`, run `sbt server/reStop` (or `kill $(lsof -ti:8080)`) before relaunching.

- **The Vite plugin's `cwd: ".."`** in [client/vite.config.mjs](client/vite.config.mjs) — without it, `@scala-js/vite-plugin-scalajs` invokes a fresh sbt inside `client/` which has no build there.

- **Liquibase logs through `java.util.logging`** by default; `sbt-revolver` then tags everything on stderr as `[ERROR]`. We bridge JUL→SLF4J in `Main.scala` (`SLF4JBridgeHandler.install()`) and `logback.xml` adds a `LevelChangePropagator` to keep the bridge cheap. The `Database is up to date, no changesets to execute` line is a hardcoded `println` inside Liquibase — bypasses any logger, can't easily be silenced.

- **`OpenapiCodegenPlugin` lives on the cross project.** Enabled with `crossProject(...).enablePlugins(OpenapiCodegenPlugin)`. The settings (`openapiSwaggerFile`, `openapiPackage`, `openapiObject`, `openapiJsonSerdeLib`) are also at the cross-project level. Generated files go into each platform's `src_managed`.

- **`-Wconf:src=.*src_managed/.*:s`** in `scalacOptions` silences unused-import warnings in codegen output (we don't author those imports).

- **Codegen unused warnings vs `-Wunused:all`.** Don't drop the silencer — without it the codegen emits ~8 unused-import warnings per build and they pollute the output.

- **Scala 3 E198 false positives on chained calls** — sometimes flagged at the closing paren of a method-chain. Silenced project-wide with `-Wconf:msg=unused local definition:s` until upstream improves it.

- **Layer wiring order in `Main.scala`.** `HelloHandler.live` requires `AppConfig & VisitsRepo & RedisCache & HelloEventLog & JDataSource` — five inputs. ZIO's macro figures out the dependency graph automatically; just ensure every layer is in the `provide(...)` set. Adding a new store means adding *one* layer, no plumbing through intermediate constructors.

- **Cache key vs. cached-flag asymmetry** — `HelloHandler` always stores `Greeting(cached=false)` in Redis (the canonical form) and flips the flag to `true` only on read. Don't change this — otherwise the second cache write would set `cached=true` and the third read would still appear cold to the eye.

- **MongoDB indexes are created at startup** (`HelloEventLog.live`'s `createIndex` call). `createIndex` is idempotent in MongoDB so re-running is safe; if you change the index spec, give it a new `name` or drop the old one.

- **Lettuce `RedisClient.shutdown()` blocks** for ~2s by default while it gracefully closes Netty resources. The `ZIO.acquireRelease` guard handles this on layer teardown — visible if you Ctrl-C `bin/dev`.

## Useful upstream docs

- ZIO HTTP: https://zio.dev/zio-http/
- tapir + ZIO HTTP server: https://tapir.softwaremill.com/en/latest/server/ziohttp.html
- tapir sbt-openapi-codegen: https://tapir.softwaremill.com/en/latest/generator/sbt-openapi-codegen.html
- scalajs-react: https://github.com/japgolly/scalajs-react
- vite-plugin-scalajs: https://github.com/scala-js/vite-plugin-scalajs
- Tailwind v4 + Vite: https://tailwindcss.com/docs/installation/using-vite
- Liquibase: https://docs.liquibase.com/
- Lettuce (Redis client): https://lettuce.io/core/release/reference/
- MongoDB Java sync driver: https://www.mongodb.com/docs/drivers/java/sync/current/
</content>
</invoke>