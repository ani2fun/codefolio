---
title: Repository Tour
summary: Module layout, the OpenAPI codegen flow, and a map of where each kind of code lives.
---

## Top-level layout

```
codefolio/
├── api/openapi.yaml              ← single source of truth for HTTP contracts
├── shared/                       ← codegen output: case classes + tapir Endpoints
├── server/                       ← ZIO + zio-http backend
├── client/                       ← Scala.js + scalajs-react frontend + Vite
├── content/cortex/               ← markdown books (you're reading this from here)
├── runner/go-judge/              ← go-judge sandbox image (Dockerfile + toolchains)
├── bin/dev                       ← the dev script you'll run most days
├── build.sbt                     ← sbt build, defines the three sbt projects
├── Dockerfile, docker-compose.yml
└── README.md
```

## The three sbt projects

```d2
direction: right

api: api/openapi.yaml {
  shape: page
}

shared: shared {
  jvm: shared.jvm (case classes + tapir Endpoints)
  js:  shared.js  (case classes + tapir Endpoints)
}

server: server {
  shape: rectangle
}

client: client {
  shape: rectangle
}

api -> shared.jvm: codegen
api -> shared.js:  codegen
shared.jvm -> server: depends on
shared.js  -> client: depends on
```

`build.sbt` defines:

- `shared` — a **cross-compiled** project (`shared.jvm` and `shared.js`). Both depend on the codegen step that turns `api/openapi.yaml` into Scala source files.
- `server` — JVM-only; depends on `shared.jvm`.
- `client` — Scala.js-only; depends on `shared.js`.

That cross-compilation is what makes the same `RunRequest(language, source, stdin)` case class usable on both sides without copy-paste.

### What gets generated

```scala
// Pseudo-Scala — illustrative, not executable.
// `sbt-openapi-codegen` reads api/openapi.yaml and emits, into
// shared/.{jvm,js}/target/scala-3.6.2/src_managed/main/sbt-openapi-codegen/:
//
//   Endpoints.scala            — case classes (RunRequest, ChapterPayload, ...)
//                                + tapir endpoint values (Endpoints.runCode, ...)
//   EndpointsJsonSerdes.scala  — circe encoders/decoders
//   EndpointsSchemas.scala     — tapir Schemas (for OpenAPI doc + validation)
println("Edit api/openapi.yaml -> sbt compile -> both sides break until updated.")
```

The contract is enforced by *the compiler*, not by tests. If you change a field's name on the spec, the server handler stops compiling, and so does any frontend code that pattern-matches on it. That's intentional — drift between spec and code is the most common bug class on full-stack projects, and codegen makes drift a compile error.

## What lives in `server/`

```
server/src/main/scala/codefolio/server/
├── Main.scala                            ← ZIO entry point; layer wiring
├── HttpApp.scala                         ← tapir → zio-http binding (delegates routes to ApiRoutes)
├── content/
│   └── MtimeCachedIndex.scala                mtime-keyed cache shared by the Cortex
│                                             and Blog pipelines (ADR-0008)
├── helloPipeline/
│   └── HelloPipeline.scala                   /api/hello, /api/recent, /api/health —
│                                             one deep module with internal Visits /
│                                             GreetingCache / EventLog seams (ADR-0003)
├── codeRunPipeline/
│   ├── CodeRunPipeline.scala                 /api/run — single CodeExecutionBackend
│   │                                         seam; public RunFailure ADT (ADR-0004)
│   ├── Languages.scala                       single source of truth for language
│   │                                         dispatch — ids, aliases, goJudge
│   │                                         (GoJudgeSpec), effectiveSource (ADR-0011)
│   ├── GoJudgeWire.scala                      go-judge /run request + response mapping
│   └── JavaSourceRewriter.scala              normalises `public class Foo` to Main
├── cortexPipeline/
│   ├── CortexPipeline.scala                  /api/cortex/* — internal CortexFs seam;
│   │                                         MtimeCachedIndex cache; CortexFailure type
│   └── ChapterAssetRewrite.scala             rewrites relative asset URLs in chapters
├── blogPipeline/
│   ├── BlogPipeline.scala                    /api/blogs/index, /api/blogs/{slug}
│   └── BlogFrontmatter.scala                 YAML frontmatter parser for posts
├── http/
│   ├── ApiRoutes.scala                       composes every pipeline's tapir endpoints;
│   │                                         handlerEndpoint wiring helper (ADR-0012)
│   ├── ApiErrors.scala                       HandlerFailure union → HTTP status mapping
│   ├── FileServer.scala                      static file serving + path-traversal guard,
│   │                                         shared by both static surfaces (ADR-0010)
│   ├── ContentTypes.scala                    one extension → Content-Type table
│   ├── StaticRoutes.scala                    /assets/*, SPA index.html fallback derived
│   │                                         from AppRoutes.SpaRoutes (ADR-0009)
│   ├── CortexAssetRoutes.scala               /api/cortex/asset/* over a FileServer
│   └── LikeC4ProxyRoutes.scala               /c4/* reverse proxy to the LikeC4 service
├── db/
│   ├── DataSource.scala                      HikariCP pool
│   └── Migrations.scala                      Liquibase runner
└── config/
    └── AppConfig.scala                       AppConfig + per-store config types
```

Each *pipeline* package is a **deep module** with a public entry point and small internal seams (see ADR-0003 and ADR-0004 in `docs/adr/`). If you're looking for "where does `/api/run` happen", grep for `runCode` and start in `CodeRunPipeline.scala`. Cross-cutting wiring (route composition, error → HTTP mapping, static fallback) lives in `http/`.

## What lives in `client/`

```
client/src/
├── main/scala/codefolio/client/
│   ├── Main.scala            ← createRoot + render Router
│   ├── Router.scala          ← scalajs-react Router config
│   ├── Page.scala            ← sealed trait Page = Home | Chapter | ...
│   ├── Layout.scala          ← Header / outlet / Footer wrapper
│   ├── pages/                ← one file per route
│   ├── components/
│   │   ├── sections/         ← Hero, About, Experience, Projects, Cortex, ...
│   │   ├── cortex/           ← CortexSidebar, CortexToc, ChapterContent, BookGrid, ...
│   │   ├── blog/             ← PostGrid, blog post renderers
│   │   ├── ui/               ← Section, Button (CVA-style variants in Scala)
│   │   └── icons/            ← lucide-react via JsComponent
│   ├── api/ApiClient.scala   ← typed HTTP client built from tapir endpoints
│   ├── markdown/MarkdownRenderer.scala  ← thin Scala facade over render.ts
│   ├── data/portfolioData.ts ← TS bridge to JSON content
│   └── util/                 ← Theme, PageTitle, Cn (clsx + tailwind-merge), ...
├── markdown/                 ← TS-side of the markdown pipeline
│   ├── render.ts             ← unified/remark/rehype + shiki/d2/mermaid/katex
│   ├── runtime.ts            ← Mermaid renderer + Prism highlighter
│   └── loader.ts             ← lazy gateway: dynamic-import wrapper
├── styles/                   ← per-section/component BEM stylesheets
│   ├── sections/             ← hero.css, about.css, experience.css, ...
│   └── components/           ← cortex-reader.css, diagrams.css, runnable-code.css, ...
├── tailwind.css              ← v4 entry: @import "tailwindcss" + @theme + @utility + section imports
├── vite.config.mjs
└── index.html                ← inline theme-bootstrap script lives here
```

A few patterns to internalise:

- **Pages are routes.** Every file in `pages/` is exactly one route in `Router.scala`. To add a route, add a file, add a `Page` case, add a rule in `Router`.
- **`components/sections/`** = portfolio sections on the home page. **`components/cortex/`** = anything that participates in chapter rendering.
- **`markdown/render.ts`** is the single point of contact with the JS markdown ecosystem. Resist the urge to add per-plugin Scala facades — wrap once in TS, call from Scala.
- **CSS is BEM via Tailwind `@apply`.** Every multi-class `^.className` lives as a `.block__element--modifier` rule in `client/src/styles/{sections,components}/*.css`, wrapped in `@layer components` so utilities applied at the call site still win. The companion Scala primitive is `components/ui/Section.scala`. Tailwind v4 is configured CSS-first — there is **no** `tailwind.config.ts` and **no** `postcss.config.mjs`.

## What lives in `shared/`

`shared/` is the smallest of the three sbt projects but it's the keystone — it's what stops the server and the client from drifting apart.

It's a **cross-compiled** Scala module: one source tree that compiles to JVM bytecode for the server *and* to Scala.js IR for the client. The same `case class Greeting(message, visits, cached)` is available to both sides, with the same JSON shape, the same field names, the same nullable rules. There is no hand-written DTO on either side — there's nothing to keep in sync.

What's in there:

- **Codegen output** (most of it) — `Endpoints.scala`, `EndpointsJsonSerdes.scala`, `EndpointsSchemas.scala` emitted under `src_managed/` by `sbt-openapi-codegen` from `api/openapi.yaml`. Includes every request/response case class plus tapir `Endpoint` values the server interprets and the client calls.
- **Two hand-written pure modules** — `runner/CodeExecutor.scala` (state machine for the runnable code blocks) and `cortex/SidebarForest.scala` (flat-list → tree builder for the sidebar). Both are platform-agnostic, both unit-tested on the JVM, both used in the browser.
- **One smoke test** under `shared/src/test/scala/` that imports the generated `Endpoints` to confirm codegen ran. If `sbt test` passes, the generation worked.

The payoff: rename `Greeting.message` → `Greeting.greeting` in `api/openapi.yaml`, run `sbt compile`, and the **server pipeline** and the **client API client** both stop compiling in the same build. The contract becomes a compile-time check.

The canonical walk-through of this is the [Hello World, End to End](../how-it-works/hello-world-end-to-end) chapter — it traces one `/api/hello` request through every step of the stack and shows exactly where the shared module pays off. The full mechanical details (`crossProject(JSPlatform, JVMPlatform)`, `crossType(CrossType.Pure)`, codegen plugin settings, how each generated file is laid out) are in the [Shared & Codegen](../deep-dive/shared-and-codegen) deep-dive.

## What lives in `content/`

```
content/cortex/                            ← books are auto-discovered (no root manifest)
├── codefolio-onboarding/
│   ├── book.json                          ← OPTIONAL — title/description/tags
│   ├── 01-start-here/                     ← directory = collapsible sidebar section
│   │   ├── _section.json                  ← OPTIONAL — pretty section title
│   │   └── *.md                           ← one chapter per file
│   └── ...
└── distributed-systems/
    └── ...
```

**Convention over configuration** — the on-disk layout *is* the index. Each immediate subdir of `content/cortex/` is a book; nested directories become sidebar sections (any depth, capped at 6); `.md` files are chapters; numeric prefixes (`01-`, `02-`) drive ordering. To add a chapter you drop a `.md` file in the right directory and reload — that's it. (See [Extending the Project](./extending).)

## What lives in `runner/go-judge/`

The build context for the **go-judge** sandbox image — a `Dockerfile` that layers the language toolchains (JDK 21, Python, C/C++, Go, Rust, Kotlin, TypeScript, JavaScript, SQLite) onto [`criyle/go-judge`](https://github.com/criyle/go-judge). go-judge runs each submission inside a Linux namespace + cgroup; the server talks to it over its `POST /run` command API on `:5050`. It's the **single** code-execution backend, configured with `EXECUTOR_URL`.

History (see ADR-0029): the project previously ran two backends — **Piston** (a public service) and a custom Node "Code Runner" that imitated the Judge0 submissions API. Both were retired in favour of one self-hosted go-judge sandbox: cgroup-v2-native, standalone (no extra database/queue), and properly sandboxed (namespaces + cgroups + seccomp).

## Where to look first for a given task

| If you want to … | Start in … |
| --- | --- |
| Change a request/response shape | `api/openapi.yaml`, then chase compile errors |
| Add a new HTTP endpoint | `api/openapi.yaml` → new `server/<feature>Pipeline/` package → register in `server/http/ApiRoutes.scala` → call from `client/api/ApiClient.scala` |
| Tweak how a chapter looks | `client/src/markdown/render.ts` (HTML emitted) and/or the relevant component under `client/.../components/cortex/` |
| Add a runnable language | a `GoJudgeSpec` (compile/run commands) in `server/.../codeRunPipeline/Languages.scala` + the toolchain in `runner/go-judge/Dockerfile` + Prism grammar in `client/src/markdown/runtime.ts` |
| Fix a routing issue | `client/.../Router.scala` (SPA side) **and** `server/http/StaticRoutes.scala`'s SPA fallback (server side) |
| Wire a new env var | `server/.../config/AppConfig.scala` + `application.conf` + `docker-compose.yml` |
