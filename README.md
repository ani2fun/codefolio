# codefolio

A fullstack Scala personal portfolio app. Frontend is **Scala.js + React**, backend is **ZIO + zio-http**, the API contract is **OpenAPI-first** with code generation, and everything runs in **Docker Compose**. The skeleton wires three backing stores end-to-end so you can see the integration patterns:

| Store | Role |
|---|---|
| **Postgres** | Persistent visit counter (Liquibase migrations, plain JDBC + HikariCP) |
| **Redis** (Lettuce) | Read-through cache for the `/api/hello` payload (~10s TTL) — response carries a `cached` flag |
| **MongoDB** (sync driver) | Append-only log of every `/api/hello` call; surfaced via `/api/recent` |

## Quick start

```bash
docker compose up --build
```

Then open <http://localhost:8080>. Swagger UI is at <http://localhost:8080/docs>.

## Prerequisites

- JDK 21+
- sbt 1.10+
- Node 20+ (for the Vite frontend)
- Docker (for Postgres + the production image)

## Local development — one command

```bash
./bin/dev
```

Brings up Postgres (Docker), an sbt watcher that re-runs `server/reStart` and `client/fastLinkJS` on source change, and the Vite dev server on :5173 — all in one terminal with prefixed, colour-coded logs. Ctrl-C stops everything.

| URL | What |
|---|---|
| <http://localhost:5173> | Frontend with Vite HMR (proxies `/api` and `/docs` to :8080) |
| <http://localhost:8080> | ZIO server (API + static fallback) |
| <http://localhost:8080/docs> | Swagger UI |

### Manual / four-terminal alternative

If you'd rather run pieces separately:

```bash
# A — Postgres only
docker compose up db

# B — backend, auto-restarts on source change
sbt "~server/reStart"

# C — Scala.js continuous compile (re-links on change, picked up by Vite HMR)
sbt "~client/fastLinkJS"

# D — Vite dev server (proxies /api and /docs to :8080)
cd client && npm install && npm run dev
```

## Common sbt commands

| Command | What it does |
|---|---|
| `sbt clean` | Delete all `target/` directories across modules |
| `sbt compile` | Compile everything; triggers OpenAPI codegen for `shared` |
| `sbt test` | Run all tests (currently the shared codegen smoke spec, on JVM + JS) |
| `sbt sharedJVM/test` / `sbt sharedJS/test` | Run shared tests on a single platform |
| `sbt server/run` | Run the backend once (no auto-reload) |
| `sbt "~server/reStart"` | Run the backend with auto-restart on source change (uses sbt-revolver) |
| `sbt server/reStop` | Stop the backgrounded backend started by `reStart` |
| `sbt "~client/fastLinkJS"` | Continuously link Scala.js for development (debug-friendly) |
| `sbt client/fullLinkJS` | One-shot optimised Scala.js link (production) |
| `sbt server/Universal/stage` | Stage server launcher at `server/target/universal/stage/bin/codefolio-server` |
| `sbt server/Universal/packageBin` | Build a runnable `.zip` of the server |
| `sbt update` | Resolve and download all dependencies |
| `sbt reload` | Reload `build.sbt` after editing it |
| `sbt console` | Open a Scala 3 REPL with project classpath |
| `sbt projects` | List all sbt sub-projects (`shared`, `server`, `client`, `root`) |
| `sbt dependencyTree` | Show the resolved dependency graph (helpful for version conflicts) |

> Tip: open `sbt` once and run multiple commands in its shell rather than re-invoking `sbt …` from the OS — much faster (no JVM cold start).

## Common frontend (npm) commands

Run from `client/`:

| Command | What it does |
|---|---|
| `npm install` | Install Vite, React, Tailwind, the scalajs-vite plugin |
| `npm run dev` | Start the Vite dev server on :5173 with HMR (proxies `/api` and `/docs` to :8080) |
| `npm run build` | Production bundle to `client/dist/` (calls `sbt client/fullLinkJS` via the plugin) |
| `npm run preview` | Locally serve the built `dist/` to verify the production bundle |

## Docker

| Command | What it does |
|---|---|
| `docker compose up --build` | Build the image and bring up `db` + `redis` + `mongo` + `app` (full prod-like run) |
| `docker compose up db redis mongo` | Just the backing stores (used during local dev) |
| `docker compose up app` | Just the app container (assumes the stores are already up) |
| `docker compose down` | Stop and remove containers (volumes preserved) |
| `docker compose down -v` | Same plus drop the `pgdata` and `mongodata` volumes — **wipes both DBs** |
| `docker compose logs -f app` | Tail backend logs |
| `docker compose exec db psql -U codefolio` | Open a `psql` shell against the Postgres container |
| `docker compose exec redis redis-cli` | Open a `redis-cli` shell |
| `docker compose exec mongo mongosh codefolio` | Open a `mongosh` shell on the `codefolio` DB |
| `docker build -t codefolio .` | Build the production image without compose |

## API-first workflow

`api/openapi.yaml` is the source of truth. `sbt compile` triggers `sbt-openapi-codegen`, which writes tapir endpoint descriptions and case classes into the cross-compiled `shared` module under `codefolio.shared.api.Endpoints`. Both backend and frontend compile against those generated types — schema drift becomes a compile error.

To change the API:
1. Edit `api/openapi.yaml`.
2. Run `sbt compile`.
3. Fix the resulting compile errors in `server/` (`HelloHandler`) and `client/` (`ApiClient`).

## Trying it out

After `./bin/dev`:

```bash
# Cold call: cached=false, increments the Postgres counter
curl http://localhost:8080/api/hello

# Within 10s — cached=true, same visit count, served from Redis
curl http://localhost:8080/api/hello

# After ~10s, the cache expires; next call refreshes it
sleep 11 && curl http://localhost:8080/api/hello

# Last 10 calls, read from MongoDB
curl http://localhost:8080/api/recent

# Liveness across all three stores
curl http://localhost:8080/api/health
```

## Stack

Scala 3 · sbt · ZIO 2 · zio-http · tapir · Postgres · Liquibase · Redis (Lettuce) · MongoDB · Scala.js · scalajs-react · Vite · Tailwind v4
