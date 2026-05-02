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
├── content/cortex/            ← markdown books (you're reading this from here)
├── runner/                       ← local Code Runner (Node + sandboxed exec)
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
├── Main.scala                ← ZIO entry point; layer wiring
├── HttpApp.scala             ← tapir → zio-http; static-file routes; SPA fallback
├── handlers/
│   ├── HelloHandler.scala         /api/hello, /api/recent, /api/health
│   ├── CodeRunHandler.scala       /api/run — picks Piston or Code Runner
│   └── CortexHandler.scala     /api/cortex/*
├── runner/
│   ├── Languages.scala            10-entry registry (python, java, ...)
│   ├── Piston.scala               public Piston backend
│   └── CodeRunner.scala           local Judge0-protocol Code Runner backend
├── db/                       ← HikariCP DataSource, Liquibase migrations, VisitsRepo
├── cache/RedisCache.scala
├── eventlog/HelloEventLog.scala  ← Mongo capped collection of recent /hello calls
└── config/AppConfig.scala
```

The handlers are organised by **endpoint family**, not by entity. If you're looking for "where does `/api/run` happen", grep for `runCode` and start in `CodeRunHandler.scala`.

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
│   │   ├── sections/         ← Hero, About, Experience, Projects, ...
│   │   ├── cortex/        ← ChapterContent, RunnableCodeBlock, MermaidBlock, D2Diagram, ...
│   │   ├── ui/               ← Button (CVA-style variants in Scala)
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

Almost nothing **by hand** — it's mostly codegen output. The one hand-written file is a smoke test under `shared/src/test/scala/` that imports the generated `Endpoints` to verify codegen ran. If `sbt test` passes, the generation worked and the schemas line up.

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

## What lives in `runner/`

A small Node container that speaks the Judge0 `/submissions/?wait=true` wire protocol. It's the local execution backend used by `CODE_RUNNER_URL=http://code-runner:2358`. In production we typically use **Piston** (public) instead, configured via `PISTON_URL`.

The historical name reason: the project once used Judge0 itself, but Judge0 has no ARM image and is heavy. We kept the wire protocol and reimplemented it ourselves. The class name `CodeRunner` reflects what we actually call; the `Judge0` name has been retired everywhere.

## Where to look first for a given task

| If you want to … | Start in … |
| --- | --- |
| Change a request/response shape | `api/openapi.yaml`, then chase compile errors |
| Add a new HTTP endpoint | `api/openapi.yaml` → handler under `server/handlers/` → wire in `HttpApp.scala` → call from `client/api/ApiClient.scala` |
| Tweak how a chapter looks | `client/src/markdown/render.ts` (HTML emitted) and/or the relevant component under `client/.../components/cortex/` |
| Add a runnable language | `server/.../runner/Languages.scala` + Prism grammar in `client/src/markdown/runtime.ts` |
| Fix a routing issue | `client/.../Router.scala` (SPA side) **and** `server/.../HttpApp.scala`'s static fallback (server side) |
| Wire a new env var | `server/.../config/AppConfig.scala` + `application.conf` + `docker-compose.yml` |
