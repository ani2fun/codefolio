# codefolio

A fullstack Scala personal portfolio + interactive knowledge base. Frontend
is **Scala.js + scalajs-react** (with a small TypeScript helper module
hosting the unified/remark/rehype markdown pipeline), backend is **ZIO +
zio-http + tapir**, the API contract is **OpenAPI-first** with code
generation, and everything runs in **Docker Compose**.

> **New here?** The full engineering tour lives in the knowledge base
> itself. After `./bin/dev`, open
> <http://localhost:5173/knowledge/codefolio-onboarding/overview> for a
> walk-through of the architecture, the request lifecycle, the markdown
> pipeline, and how to extend the project. The chapters are also
> readable as plain markdown under
> [`content/knowledge/codefolio-onboarding/`](content/knowledge/codefolio-onboarding/).

## What's inside

- **Portfolio landing page** — Hero, About, Experience (tab picker),
  Projects, Certifications timeline, Knowledge Base preview, Footer.
- **Knowledge base** — `/knowledge` lists books; `/knowledge/<book>/<chapter>`
  renders markdown with KaTeX math, mermaid + D2 diagrams, GFM tables, and
  *runnable* code blocks. The markdown pipeline lives in TypeScript
  (`client/src/markdown/render.ts`) and is invoked from Scala.js through a
  lazy-loaded gateway, so its multi-MB dependency tree doesn't ship with
  the home page.
- **Code execution** — `POST /api/run` proxies to a configured Piston
  server (production) or to the local `code-runner` container (dev). The
  `RunnableCodeBlock` UI component lives in scalajs-react and exercises
  this end-to-end.
- **Hello demo** — kept at `/demo` (the original codefolio skeleton). Hits
  Postgres/Redis/Mongo end-to-end; useful as an integration smoke test.

## Backing stores

| Store | Role |
|---|---|
| **Postgres** | Visit counter for the kept Hello demo (Liquibase migrations, plain JDBC + HikariCP). |
| **Redis** (Lettuce) | Read-through cache for `/api/hello` (~10s TTL) — response carries a `cached` flag. |
| **MongoDB** (sync driver) | Append-only log of every `/api/hello` call; surfaced via `/api/recent`. |
| **code-runner** (local container) | Speaks the Judge0 submissions API; backs `/api/run` for development. Production points `PISTON_URL` at a Piston server instead. |

## Quick start

```bash
docker compose up --build
```

Then open <http://localhost:8080>. Swagger UI is at <http://localhost:8080/docs>.

## Prerequisites

- JDK 21+
- sbt 1.10+
- Node 20+ (for the Vite frontend)
- Docker (for Postgres + Redis + MongoDB + code-runner + the production image)

## Local development — one command

```bash
./bin/dev
```

Brings up Postgres / Redis / Mongo / code-runner (in Docker), an sbt
watcher that re-runs `server/reStart` and `client/fastLinkJS` on source
change, and the Vite dev server on :5173 — all in one terminal with
prefixed, colour-coded logs. Ctrl-C stops everything.

| URL | What |
|---|---|
| <http://localhost:5173> | Frontend with Vite HMR (proxies `/api` and `/docs` to :8080) |
| <http://localhost:8080> | ZIO server (API + static fallback) |
| <http://localhost:8080/docs> | Swagger UI |

### Manual / four-terminal alternative

If you'd rather run pieces separately:

```bash
# A — backing stores
docker compose up db redis mongo code-runner

# B — backend, auto-restarts on source change
sbt "~server/reStart"

# C — Scala.js continuous compile (re-links on change, picked up by Vite HMR)
sbt "~client/fastLinkJS"

# D — Vite dev server (proxies /api and /docs to :8080)
cd client && npm install && npm run dev
```

## Environment variables

| Variable | What | Default |
|---|---|---|
| `PORT` | Server HTTP port | `8080` |
| `STATIC_DIR` | Where the Vite production bundle lives | `./client/dist` (in the prod image: `/app/static`) |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Postgres | `jdbc:postgresql://localhost:5432/codefolio` / `codefolio` / `codefolio` |
| `REDIS_URL` | Redis | `redis://localhost:6379` |
| `REDIS_TTL_SECS` | Cache TTL for the Hello payload | `10` |
| `MONGO_URI` / `MONGO_DB` | MongoDB | `mongodb://localhost:27017` / `codefolio` |
| `PISTON_URL` | Piston API base (production); when set, used for languages Piston supports | unset |
| `CODE_RUNNER_URL` | Local Code Runner base (Judge0 submissions API protocol) | unset (`http://code-runner:2358` in compose) |
| `CODE_RUNNER_AUTHN_TOKEN` | Optional `X-Auth-Token` for the local runner | unset |
| `KNOWLEDGE_ROOT` | On-disk root of the knowledge content tree | `./content/knowledge` (in the prod image: `/app/content/knowledge`) |

If neither `PISTON_URL` nor `CODE_RUNNER_URL` is configured, `/api/run`
returns 503 with a hint to set one of them.

## API surface

| Endpoint | Method | What |
|---|---|---|
| `/api/hello` | GET | Increment Postgres counter, cache in Redis, log to Mongo. |
| `/api/recent` | GET | Last 10 `/api/hello` calls, newest first (from Mongo). |
| `/api/health` | GET | 200 if all three stores are reachable. |
| `/api/run` | POST | Execute a code snippet via Piston / Code Runner. |
| `/api/knowledge/index` | GET | List books + chapter refs. |
| `/api/knowledge/{book}/{chapter}` | GET | Frontmatter + raw markdown + prev/next slugs for one chapter. |
| `/docs` | GET | Swagger UI |
| `/`, `/knowledge/...`, `/demo`, `/blogs`, `/assets/*`, `/img/*`, `/certificates/*`, `/Aniket-Kakde-CV-EN.pdf` | GET | Static / SPA-fallback content. |

## Adding content

### A new book / chapter

1. Drop a directory under `content/knowledge/` named with the book's slug.
2. Add a `meta.json` for the book listing its chapters in reading order.
3. Add chapter `*.md` files. The frontmatter shape is small:
   ```markdown
   ---
   title: My Chapter
   summary: Optional one-liner shown above the body.
   group: Foundations
   ---

   ## Section heading
   ...
   ```
4. Reference the book from the root `content/knowledge/meta.json`.
5. Restart the server (the index is cached in memory). The new chapter
   shows up at `/knowledge/<book-slug>/<chapter-slug>`.

### A new runnable language

Edit `server/.../runner/Languages.scala` to add an entry to
`RUNNABLE_LANGUAGES` (Judge0 language ID + label + aliases), and add the
matching alias mapping in `client/src/markdown/runtime.ts` (Prism alias
table). For Piston-only languages, also add a row to `Piston.scala`'s
`pistonLanguage` map.

## Common sbt commands

| Command | What it does |
|---|---|
| `sbt clean` | Delete all `target/` directories across modules |
| `sbt compile` | Compile everything; triggers OpenAPI codegen for `shared` |
| `sbt test` | Run all tests (currently the shared codegen smoke spec) |
| `sbt server/run` | Run the backend once (no auto-reload) |
| `sbt "~server/reStart"` | Run the backend with auto-restart on source change |
| `sbt server/reStop` | Stop the backgrounded backend started by `reStart` |
| `sbt "~client/fastLinkJS"` | Continuously link Scala.js for development |
| `sbt client/fullLinkJS` | One-shot optimised Scala.js link (production) |
| `sbt server/Universal/stage` | Stage the server launcher |
| `sbt update` | Resolve and download all dependencies |

## Common frontend (npm) commands — run from `client/`

| Command | What it does |
|---|---|
| `npm install` | Install Vite, React, Tailwind, the markdown pipeline deps |
| `npm run dev` | Start the Vite dev server on :5173 (proxies `/api` and `/docs` to :8080) |
| `npm run build` | Production bundle to `client/dist/` (calls `sbt client/fullLinkJS`) |
| `npm run preview` | Locally serve the built `dist/` to verify the production bundle |

## Docker

| Command | What it does |
|---|---|
| `docker compose up --build` | Build the image and bring up all services (db + redis + mongo + code-runner + app) |
| `docker compose up db redis mongo code-runner` | Just the backing stores (used during local dev) |
| `docker compose up app` | Just the app container (assumes the stores are already up) |
| `docker compose down` | Stop and remove containers (volumes preserved) |
| `docker compose down -v` | Same plus drop the `pgdata` and `mongodata` volumes — **wipes the DBs** |
| `docker compose logs -f app` | Tail backend logs |
| `docker compose exec db psql -U codefolio` | Open a `psql` shell |
| `docker compose exec redis redis-cli` | Open a `redis-cli` shell |
| `docker compose exec mongo mongosh codefolio` | Open a `mongosh` shell |

## API-first workflow

`api/openapi.yaml` is the source of truth. `sbt compile` triggers
`sbt-openapi-codegen`, which writes tapir endpoint descriptions and case
classes into the cross-compiled `shared` module under
`codefolio.shared.api.Endpoints`. Both backend and frontend compile against
those generated types — schema drift becomes a compile error.

To change the API:
1. Edit `api/openapi.yaml`.
2. Run `sbt compile`.
3. Fix the resulting compile errors in `server/` (handlers) and `client/`
   (`ApiClient`).

## Trying it out

After `./bin/dev`:

```bash
# Hello demo: cold call
curl http://localhost:8080/api/hello

# Within 10s — cached=true, served from Redis
curl http://localhost:8080/api/hello

# Last 10 hello calls, from MongoDB
curl http://localhost:8080/api/recent

# Liveness across all three stores
curl http://localhost:8080/api/health

# Run a Python snippet via the local code-runner
curl -X POST http://localhost:8080/api/run \
  -H 'Content-Type: application/json' \
  -d '{"language":"python","source":"print(2+2)"}'

# List knowledge-base books
curl http://localhost:8080/api/knowledge/index

# Fetch one chapter (raw markdown + frontmatter)
curl http://localhost:8080/api/knowledge/distributed-systems/introduction
```

In the browser:

- <http://localhost:5173/> — landing page
- <http://localhost:5173/knowledge> — book index
- <http://localhost:5173/knowledge/distributed-systems/introduction> — chapter reader (markdown + mermaid + D2 + KaTeX + runnable code)
- <http://localhost:5173/demo> — kept Hello demo

## Stack

Scala 3 · sbt · ZIO 2 · zio-http · tapir · Postgres · Liquibase · Redis
(Lettuce) · MongoDB · Scala.js · scalajs-react · Vite · Tailwind v3 ·
unified · remark · rehype · shiki · mermaid · @terrastruct/d2 · KaTeX ·
Prism · react-simple-code-editor.
