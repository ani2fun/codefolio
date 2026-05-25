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
| Cache | Redis via `io.lettuce:lettuce-core` 6.5.x (async, wrapped with `ZIO.fromCompletionStage`) — also backs the `/api/run` rate limiter |
| Event log | MongoDB via `org.mongodb:mongodb-driver-sync` 5.2.x (blocking, wrapped with `ZIO.attemptBlocking`) |
| Migrations | Liquibase 4.x, YAML master changelog including SQL changesets |
| Auth | OIDC via Keycloak (`apps-prod` realm, GitHub IdP); server validates JWTs with `com.nimbusds:nimbus-jose-jwt` 9.x; SPA uses `keycloak-js` 26.x (PKCE). See ADR-0013 |
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
├── shared/                 # cross-compiled (JVM + JS); codegen lands in src_managed
│   └── src/main/scala/codefolio/shared/
│       ├── runner/         # CodeExecutor (pure Idle→Running→Done state machine)
│       ├── cortex/         # SidebarForest (pure flat-list-to-tree builder)
│       ├── api/            # generated tapir endpoints + case classes (Endpoints.*)
│       └── AppRoutes.scala # shared route segments + SpaRoutes topology (ADR-0009)
├── server/                 # ZIO + zio-http + tapir + Postgres/Redis/MongoDB
│   └── src/main/scala/codefolio/server/
│       ├── db/             # DataSource, Migrations (Postgres infra)
│       ├── content/        # MtimeCachedIndex — mtime-keyed cache shared by the
│       │                   # Cortex + Blog pipelines (ADR-0008)
│       ├── helloPipeline/  # /api/hello,/recent,/health — one deep module with internal
│       │                   # Visits/GreetingCache/EventLog seams (ADR-0003)
│       ├── codeRunPipeline/# /api/run — one deep module with a single CodeExecutionBackend
│       │                   # seam (Piston + Code Runner adapters), pure PistonWire/
│       │                   # CodeRunnerWire wire-format modules, public RunFailure ADT;
│       │                   # Languages is the single source of truth for language
│       │                   # dispatch — backend ids + effectiveSource (ADR-0004, ADR-0011)
│       ├── cortexPipeline/ # /api/cortex/* — one deep module with an internal CortexFs
│       │                   # seam (FS scan only); MtimeCachedIndex for the cache; the
│       │                   # pure CortexIndexWalker is invoked by the pipeline, not
│       │                   # behind the seam (ADR-0004); CortexFailure is the seam's
│       │                   # error type
│       ├── blogPipeline/   # /api/blogs/* — same shape as cortexPipeline over a BlogFs
│       │                   # seam + MtimeCachedIndex; BlogFailure is the error type
│       ├── http/           # ApiRoutes (+ handlerEndpoint wiring helper, ADR-0012),
│       │                   # ApiErrors.toHttp(HandlerFailure union); StaticRoutes +
│       │                   # CortexAssetRoutes over a shared FileServer/ContentTypes
│       │                   # (ADR-0010); LikeC4ProxyRoutes
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

- **Route ordering matters.** Don't put a `Method.GET / trailing` catch-all anywhere in the static-routes set — it shadows the specific tapir routes for `/api/*` and `/docs/*` and you'll get HTML/404 instead of JSON/Swagger. [http/StaticRoutes.scala](server/src/main/scala/codefolio/server/http/StaticRoutes.scala) uses *only* specific patterns — fixed routes (`/`, `/index.html`, `/assets/*`, …) plus an SPA `index.html` fallback **derived** from `AppRoutes.SpaRoutes` (ADR-0009), one leaf route per `SpaRoute` and a `/segment/trailing` route for the nested ones:
  ```scala
  Method.GET / Root                      // → index.html
  Method.GET / "index.html"              // → index.html
  Method.GET / "assets" / trailing       // → assets/<rest>  (via FileServer)
  Method.GET / "cortex"                  // → index.html  (SpaRoute leaf)
  Method.GET / "cortex" / trailing       // → index.html  (SpaRoute, hasNestedRoutes)
  ```
  And the entire route set is replaced with `Routes.empty` when `client/dist` doesn't exist (dev mode) — `FileServer.exists` is false. File serving + the path-traversal guard live in `http/FileServer.scala` (ADR-0010).

- **`sbt run` / `reStart` working directory** is set to the build root via:
  ```scala
  Compile / run / baseDirectory := (LocalRootProject / baseDirectory).value
  reStart      / baseDirectory  := (LocalRootProject / baseDirectory).value
  ```
  Without this, the JVM's cwd defaults to `server/` and the `./client/dist` static path doesn't resolve.

- **Stale forked JVMs on :8080.** `sbt server/run` forks; `kill $sbt_pid` kills sbt but leaves the child JVM holding port 8080. `bin/dev` has a port pre-flight that detects and kills these. If you're not using `bin/dev`, run `sbt server/reStop` (or `kill $(lsof -ti:8080)`) before relaunching.

- **Auth in dev: a local Keycloak container.** `docker-compose.yml` has a `keycloak` service (dev mode, in-memory H2) that imports the `codefolio` realm from `docker/keycloak/import/` on first boot — a public PKCE client `codefolio-web` plus a local `tester` / `tester` user, so the whole GitHub-sign-in flow runs end-to-end with no homelab dependency or GitHub OAuth app. `bin/dev` starts it, waits for the realm import, and defaults `AUTH_ENABLED=true` (pointing the server + SPA at `http://localhost:8081/realms/codefolio`). For fast content/runner iteration with no auth and no Keycloak wait, run `AUTH_ENABLED=false ./bin/dev` — then the JWT verifier short-circuits, the `/api/run` rate limiter no-ops, the editor is unlocked for everyone, and the SPA skips `keycloak-js`. The local realm has no GitHub IdP, so the modal's "Continue with GitHub" lands on Keycloak's own login form (sign in as `tester`/`tester`); production points at the homelab Keycloak which does federate GitHub. Full-stack `docker compose up` keeps auth off by default — browser-side OIDC needs Keycloak on one origin for both browser and the in-container server, which `bin/dev` (server on the host) satisfies but full-docker doesn't without pinning `KC_HOSTNAME`. See ADR-0013.

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

## DSA book migration (active project)

The Cortex book at `content/cortex/data-structures-and-algorithms/` is in the middle of a multi-phase migration from a 4-language tutorial (pseudocode + Python + Java + C + Scala, verbose per-problem scaffolding) to a Python+Java-only book with a strict collapsible problem-section spec. **Before editing any DSA chapter, read [tools/DSA_MIGRATION.md](tools/DSA_MIGRATION.md)** — it covers:

- The corrected source location at `~/Development/others/tutorial_dsa/code_block_corrected_version/data-structures-and-algorithms/` (outside this repo).
- The 6-script pipeline under `tools/dsa_*.py` (strip → extract → replace → wrap → merge → tidy) with order and idempotence.
- The problem-section spec — flat `## Problem Statement`, then `<details>` for Examples / Intuition & Brute Force (merged) / Solution & Analysis (merged: Solution + Dry Run + Result Size + Complexity + Edge Cases) / Key Takeaway.
- Six real gotchas (wrap boundary bug, slug drift, misnamed source files, multi-token info strings, pre-existing malformed fences, `python` vs `python run`).
- Verification recipes — `<details>` balance check, no-legacy-langs grep, browser DOM walk.

Quick-reference rules for in-flight DSA edits:

- Source code is canonical for `## Solution` / `## The Solution` / `## Implementation` blocks. Replace destination Py/Java with source verbatim — but only if the info string is exactly `python run` / `java run` (plain `python`/`java` are inline snippets that stay).
- Never re-create what `dsa_fence_parser.py` already does. Import `iter_blocks` / `FenceBlock` for any tool that touches code fences.
- After any wrap/merge/edit pass on a chapter, run `dsa_tidy_details_gaps.py` to normalise sibling-collapsible spacing — otherwise the chapter renders with too-large gaps compared to other chapters.

### Pilot-finalised directory convention (arrays section is the reference)

The `02-linear-structures/01-arrays/` section is the completed pilot. Its structure is the template for all other DSA sections. Three file types, used in fixed order within any section directory:

**1 — Lesson files** (flat `.md`, no subdirectory)
Plain tutorial chapters. Stay as `NN-name.md` files directly in the section folder.
Example: `01-introduction.md`, `02-multidimensional.md`

**2 — Pattern directories** (`NN-pattern-NAME/`)
Each monolithic pattern `.md` is split into a directory:
```
NN-pattern-NAME/
  _section.json              {"title": "Pattern Name"}
  01-pattern.md              Understanding + Identifying sections + diagram labels
  02-problems/
    _section.json            {"title": "Problems"}
    01-slug.md               frontmatter + problem content verbatim
    02-slug.md
    …
  03-memorize.md             frontmatter + TODO stub
```
Diagram labels use `> 🖼 Diagram — caption` (static) or `> ▶ Interactive Diagram — caption` (d3 widget with `"steps"`), placed on the line immediately before the opening fence.

**3 — Design directories** (`NN-design-NAME/`)
Each design challenge `.md` is wrapped into a directory with a single content file:
```
NN-design-NAME/
  _section.json              {"title": "Design"}
  01-design-NAME.md          frontmatter + full content verbatim (top # heading stripped)
```
Example: `11-design-a-dynamic-array/` with `01-design-a-dynamic-array.md`.

**Index maintenance** — `tools/gen_cortex_index.py` regenerates `index.md` for any section directory. Run it after any rename/move/split. A `.claude/settings.json` PostToolUse hook fires it automatically after every `Write`/`Edit` to `content/cortex/`.

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
