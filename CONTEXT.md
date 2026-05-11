# Codefolio

A personal portfolio + filesystem-driven knowledge book ("Cortex"), written end-to-end in Scala. Single deploy unit; no microservices.

## Language

### Cortex (knowledge book)

**Cortex**:
The filesystem-driven knowledge base under `content/cortex/`. Each immediate subdirectory is a Book; nested directories are Sections; `.md` files are Chapters.
_Avoid_: Knowledge Base, Wiki, Docs

**Book**:
A top-level subdirectory of `content/cortex/`. Has a Slug, a Title, optional Sections, and one or more Chapters.
_Avoid_: Course, Topic, Module (overloaded with the architecture sense)

**Section**:
A nested directory inside a Book. Renders as a collapsible group in the sidebar; capped at depth 6.
_Avoid_: Folder, Group, Category

**Chapter**:
A `.md` file inside a Book or Section. May carry YAML frontmatter (`title`, `summary`).
_Avoid_: Page (overloaded with the React routing sense), Article

**Slug**:
The chapter identifier derived from its path joined with `-` (e.g. `01-data-structures/02-arrays/03-traversal.md` → `data-structures-arrays-traversal`). Numeric prefixes are stripped before display. Globally unique per index — collisions surface as `IndexInvalid`.

**Frontmatter**:
Optional YAML block at the top of a Chapter providing `title` and `summary`. The display name falls back to the stripped filename when absent.

**Sidebar Forest**:
The tree of Books, Sections, and Chapters rendered in the Cortex reader's left panel. Built from a flat chapter list.

**Runnable Code Block**:
A code block in a Chapter that calls the Code Runner via `/api/run`. One variant of a Block.

**Block**:
A typed payload extracted from a placeholder `<div>` in a rendered Chapter's HTML. Five variants: `RunnableCode`, `RunnableGroup`, `Mermaid`, `D2Slides`, `D2Inline`. Pure structural decoders live in `shared.cortex.Blocks` (JVM-tested); the JS-side DOM walk + URI / JSON shims live in `client.components.cortex.BlockDiscovery`. The renderer (`ChapterContent`) is a total `Block => VdomElement` dispatch.
_Avoid_: Widget, Plugin, Embed

### Hello pipeline (three-store demo)

**Visit Count**:
A Postgres-backed monotonically increasing integer; the canonical value behind every `/api/hello` response.

**Greeting**:
The `/api/hello` payload — `{ message, visits, cached }`. The `cached` flag reflects how this response was served, not the canonical record.

**Hello Event**:
A `{ timestampEpochMs, visits }` record appended to MongoDB on every `/api/hello`. Surfaced via `/api/recent`.

**Cached Flag**:
True when the Greeting was served from Redis; false on a cache miss. The canonical Redis value is always written with `cached=false` and flipped on read.

**Degraded**:
The state where Redis or Mongo is unreachable but `/api/hello` still returns a Greeting because Postgres is the only critical store. Non-critical-store failures are logged-then-ignored.

**Health**:
The `/api/health` payload — per-store ping status (`postgres`, `redis`, `mongo`).

### Code runner

**Run Request**:
The tapir input — `{ language, code, stdin? }`.

**Run Result**:
The tapir success output — `{ stdout, stderr, exitCode, runtimeMs }`.

**Run Failure**:
The tapir error type — invalid language, executor failure, timeout.

**Code Execution Backend**:
A single internal seam (`CodeExecutionBackend`) inside the Code Run pipeline that declares whether it supports a given language and executes a Run Request. Two adapters in production: **Piston** (remote public service, supports a fixed Judge0-id whitelist) and **Code Runner** (local Judge0-API-compatible container, universal fallback that supports every language in `Languages`). The orchestration walks the configured backends in priority order and picks the first whose `supports(lang)` is true. Wire-format mapping (request body shape, response → `RunResult`) lives in pure side modules `PistonWire` / `CodeRunnerWire`, exercised directly by golden-fixture specs without HTTP. Not a public port — the only public surface is `CodeRunPipeline.run`.

### HTTP layer

**API Error**:
The uniform JSON payload returned to clients on any handler failure — `{ message, details? }` plus an HTTP status code.

**Handler Failure**:
Any error a pipeline can produce; converted to an API Error + status by a single mapper. Implemented as a Scala 3 union type `HandlerFailure = RunFailure | CortexFailure | HelloFailure` in `ApiErrors`, with one `toHttp` method dispatching across all variants. Each variant lives in its own pipeline package (`codeRunPipeline.RunFailure`, `cortexPipeline.CortexFailure`, `helloPipeline.HelloFailure`). New pipeline error types extend the union and add a case to the match.

## Relationships

- A **Book** contains zero or more **Sections** and one or more **Chapters**.
- Every **Chapter** has a globally unique **Slug**; collisions are an Index error.
- The **Cortex** index is rebuilt when filesystem mtime changes.
- A `/api/hello` request reads a **Cached Greeting** (Redis) → falls back to **Visit Count** (Postgres) → always appends a **Hello Event** (Mongo).
- **Degraded** = a non-critical store failed; Greeting still returned.
- A **Runnable Code Block** in a **Chapter** calls a **Code Execution Backend** via the runner endpoint.
- The markdown pipeline emits a placeholder `<div>` per **Block**; the client decodes them into a typed `Block` ADT and mounts a Scala.js component for each.

## Example dialogue

> **Reviewer:** "If a **Chapter** moves to a new **Section**, does its **Slug** change?"
> **Author:** "Yes — slugs are derived from the path. Renaming or moving rewrites the URL. That's the whole point of being filesystem-driven; no separate manifest to drift."
>
> **Reviewer:** "What if Mongo is down when an `/api/hello` comes in?"
> **Author:** "**Degraded** mode. Postgres still increments, the **Greeting** still ships back, the **Hello Event** is dropped (logged). `/api/recent` is missing that entry; that's the trade."

## Flagged ambiguities

- "Page" is used in two senses: a React route (`HomePage`, `ChapterPage`) and a Chapter. **Resolved**: **Page** = React route; **Chapter** = content unit.
- "Module" is used in two senses: a Scala package/file group, and the architecture sense from `LANGUAGE.md` (anything with an interface and an implementation). **Resolved**: when discussing architecture, "module" = architecture sense; when discussing code structure, prefer "package" or "file."
- "Knowledge Base" appears in older docs (and the `content/knowledge/codefolio-onboarding/` path noted in memory). The current canonical term is **Cortex**; the rename happened in commit `8f6d8c4`. Treat any "Knowledge Base" reference as a stale alias.
</content>
</invoke>