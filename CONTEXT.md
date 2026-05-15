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

**Lesson Source**:
The authoritative raw lesson under `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/<topic>/<chapter>/<lesson>.md` from which a Cortex Chapter is derived. For each problem on a Chapter, the Lesson Source determines: the **Problem Statement**, the **Examples** (input/output pairs), and the **Solution Code** — including its method decomposition, identifier names, and inline comments. The destination Chapter owns the surrounding pedagogy (Intuition, Diagnostic Questions, Approach prose, Dry Run, Complexity, Edge Cases, Key Takeaway). When a Lesson Source factors a problem into helper methods (e.g. `skipDuplicatesLeft`, `duplicateAwareTwoSum`, `closestTwoSum`), every language tab in the destination must mirror that decomposition — paraphrasing the algorithm inline is treated as drift.
_Avoid_: Reference Solution, Upstream Lesson (Reference is too generic; Upstream implies a sync relationship that doesn't exist).

**Verbatim Code Alignment**:
The rule that a Chapter's Solution code matches its Lesson Source's code shape verbatim across all five language tabs (Pseudocode, Python, Java, C, Scala) — same method decomposition, same identifier names (`sum`/`remainingTarget`/`maxResources`/`count_ones`/`minRooms`/…), same per-language naming convention (`snake_case` in Python/C; `camelCase` in Java/Scala), and idiomatically-translated copies of the source's inline comments. Sibling problems on the same Chapter must use a consistent decomposition (e.g. if Three Sum has helpers, Four Sum must have them too). When in doubt, open the Lesson Source first and port its method structure rather than writing fresh equivalent code.
_Avoid_: Source Sync, Code Mirror.

**Sidebar Forest**:
The tree of Books, Sections, and Chapters rendered in the Cortex reader's left panel. Built from a flat chapter list.

**Mtime-Cached Index**:
A derived in-memory index over a filesystem content tree, rebuilt when the tree's mtime watermark advances. The shared module `server.content.MtimeCachedIndex` owns the cache + invalidation; the Cortex and Blog pipelines each supply only a `currentMtime` and a `rebuild` effect. `autoReload` is on under `bin/dev`, off in production (the cache fills once at first request). See ADR-0008.
_Avoid_: Cache, Index Cache (too generic — the mtime-watermark invalidation is the defining trait).

**Runnable Code Block**:
A code block in a Chapter that calls the Code Runner via `/api/run`. One variant of a Block.

**Block**:
A typed payload extracted from a placeholder `<div>` in a rendered Chapter's HTML. Seven variants: `RunnableCode`, `RunnableGroup`, `Mermaid`, `D2Slides`, `D2Inline`, `D3Widget`, `TracedCode`. Pure structural decoders live in `shared.cortex.Blocks` (JVM-tested); the JS-side DOM walk + URI / JSON shims live in `client.components.cortex.BlockDiscovery`. The renderer (`ChapterContent`) is a total `Block => VdomElement` dispatch.
_Avoid_: Widget, Plugin, Embed (Widget appears as a sub-concept of `D3Widget` — see below; never use "Widget" alone for "Block".)

**Diagram Zoom**:
The shared fullscreen-zoom modal for the `Mermaid` and `D2Inline` Block variants — `client.components.cortex.DiagramZoom`. Owns zoom state, the keyboard shortcuts, and the body-scroll lock; each diagram block keeps only its own way of obtaining the SVG (D2 receives it pre-rendered as a prop, Mermaid snapshots it on open).
_Avoid_: Lightbox, Modal (Modal is too generic — this is diagram-specific).

**D3 Widget**:
The catalog of named, parameterised D3 visualisations referenced by `Block.D3Widget(name, payload)`. Each entry is a Scala.js + D3 component on the client; the markdown fence ` ```d3 widget=<name> ` carries a JSON payload that the widget interprets. Schema is widget-local — `shared.Blocks` keeps `D3Widget` payload structurally loose (the same precedent as `D2Slides` keeping slides as raw source). The catalog is a closed match in `D3WidgetBlock`; unknown names render an inline error rather than failing the chapter. First widget: **Array Traversal Stepper**.

**Array Traversal Stepper**:
First widget in the D3 catalog. Renders a row of indexed cells with one or more named markers per step (`lo`, `mid`, `hi`, …) and an optional highlighted range. Drives linear scan, two-pointer, binary search, sliding window, and bubble-sort step visualisations from one component. Used in `01-binary-search.md`.

**Traced Code**:
A Python source the author marks ` ```python trace ` for a step-through visualisation. The server-side `/api/run` is unchanged — the client wraps the source in a `sys.settrace` harness (with `__CFTRACE_BEGIN__` / `__CFTRACE_END__` markers on stdout), posts the wrapped program, then parses the trace out of stdout and renders code + current-line cursor + locals panel + step controls. **Collapsed by default** — only the language label and a Trace button are visible until the reader opts in; an `Eye` / `EyeOff` toggle in the header collapses again without losing trace state (the trace + frame index live in component state, not the DOM). Python only in v1; the language slot is preserved so Java can land later as a separate adapter.
_Avoid_: Debugger, Stepper (Stepper is reserved for the array-traversal widget's control row), Python Tutor (the embedded-iframe pattern was rejected — see ADR-0006).

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

**Language**:
One runnable language in the `Languages` table — `Languages.Language`, wrapping the public `RunnableLanguageInfo` (Judge0 id + label + aliases) and adding `pistonName: Option[String]` (the Piston runtime name, or `None` when Piston can't run it). `Languages` is the single source of truth for language dispatch: alias resolution, byte limits, the per-backend ids, and the Java-entrypoint preprocessing (`effectiveSource`) all live there. See ADR-0011.
_Avoid_: putting backend ids on `RunnableLanguageInfo` directly — it's the codegen'd API contract, and a backend protocol detail must not leak into it.

**Code Execution Backend**:
A single internal seam (`CodeExecutionBackend`) inside the Code Run pipeline that declares whether it supports a given language and executes a Run Request. Two adapters in production: **Piston** (remote public service, supports a fixed Judge0-id whitelist) and **Code Runner** (local Judge0-API-compatible container, universal fallback that supports every language in `Languages`). The orchestration walks the configured backends in priority order and picks the first whose `supports(lang)` is true. Wire-format mapping (request body shape, response → `RunResult`) lives in pure side modules `PistonWire` / `CodeRunnerWire`, exercised directly by golden-fixture specs without HTTP. Not a public port — the only public surface is `CodeRunPipeline.run`.

### C4 model embedding

**LikeC4 Proxy**:
The server-side reverse-proxy route `GET /c4/*` in `server.http.LikeC4ProxyRoutes`. Byte-level passthrough — forwards each request verbatim to the upstream LikeC4 service, preserves the upstream `Content-Type`, returns 502 on failure. Upstream URL is supplied by `AppConfig.likec4.upstreamUrl` (env var `LIKEC4_URL`) — defaults to the in-cluster Service `http://likec4` for production K8s, overridden to `http://localhost:8090` in `bin/dev` and `http://likec4:8080` in docker compose. Not a Code Execution Backend pattern: no internal seam, no typed protocol. Lives in `server.http` alongside the static-asset routes because it has the same flavour (HTTP passthrough), not because it shares a port.
_Avoid_: Diagram service, LikeC4 backend, C4 adapter.

**C4 Model Source**:
The DSL files under `content/cortex/system-design/05-application-architecture/c4/` (currently `workspace.c4`) authored in LikeC4's DSL. Co-located with the chapter that embeds them so the diagram and the prose evolve together. Compiled by `likec4 build --base /c4/` inside `Dockerfile.likec4` into a static SPA, then served by nginx in the `likec4` image. Codefolio's CI rebuilds and promotes the `likec4` image whenever files under that directory or `Dockerfile.likec4` change.
_Avoid_: Workspace, Diagram source, Architecture file.

**C4 View**:
A named projection of a C4 Model Source — e.g. `index`, `codefolioRuntime`, `gitops` — declared inside the `views { ... }` block. The URL pattern `/c4/view/<name>` resolves to client-side routing inside the SPA: nginx serves `index.html` for any path under `/c4/` and the LikeC4 JS handles the navigation. A view is *selection* over the model, not a redraw — adding a new element automatically lights it up in every view whose filter includes it.
_Avoid_: Diagram, Picture, Chart.

### HTTP layer

**API Error**:
The uniform JSON payload returned to clients on any handler failure — `{ message, details? }` plus an HTTP status code.

**Handler Failure**:
Any error a pipeline can produce; converted to an API Error + status by a single mapper. Implemented as a Scala 3 union type `HandlerFailure = RunFailure | CortexFailure | HelloFailure | BlogFailure` in `ApiErrors`, with one `toHttp` method dispatching across all variants. Each variant lives in its own pipeline package (`codeRunPipeline.RunFailure`, `cortexPipeline.CortexFailure`, `helloPipeline.HelloFailure`, `blogPipeline.BlogFailure`). New pipeline error types extend the union and add a case to the match.

**Handler Endpoint**:
The server-side wiring helper `ApiRoutes.handlerEndpoint` — bundles the `(StatusCode, ApiError)` error output and the `HandlerFailure → toHttp` mapping so every fallible endpoint is wired the same way and can't ship without its error plumbing. The client mirror is `ApiClient.callable`. See ADR-0012.

**Static File Server**:
`server.http.FileServer` — serves files from a fixed root directory with path-traversal containment (resolve to the real on-disk path, reject anything that escapes the root). Both the frontend-bundle routes (`StaticRoutes`) and the Cortex asset routes (`CortexAssetRoutes`) route through it, so the security-critical guard has one home. The content-type lookup is `server.http.ContentTypes`. See ADR-0010.
_Avoid_: File Handler, Asset Server (Asset is overloaded — `CortexAssetRoutes` is one *caller*, not the module).

**SPA Route**:
A top-level route the single-page app owns — `AppRoutes.SpaRoute(segment, hasNestedRoutes)`. `AppRoutes.SpaRoutes` is the single source of truth for the SPA route topology: the client `Router` builds its rules from it and the production server derives its `index.html` fallback list from it. See ADR-0009.
_Avoid_: Page (overloaded — see Flagged ambiguities; **SPA Route** is the *server-visible topology*, **Page** is the client routing ADT).

## Relationships

- A **Book** contains zero or more **Sections** and one or more **Chapters**.
- Every **Chapter** has a globally unique **Slug**; collisions are an Index error.
- The **Cortex** index (and the Blog index) is an **Mtime-Cached Index** — rebuilt when the content tree's filesystem mtime advances.
- A `/api/hello` request reads a **Cached Greeting** (Redis) → falls back to **Visit Count** (Postgres) → always appends a **Hello Event** (Mongo).
- **Degraded** = a non-critical store failed; Greeting still returned.
- A **Runnable Code Block** in a **Chapter** calls a **Code Execution Backend** via the runner endpoint.
- The markdown pipeline emits a placeholder `<div>` per **Block**; the client decodes them into a typed `Block` ADT and mounts a Scala.js component for each.
- A **Chapter** may embed a **C4 View** via an `<iframe>` whose `src` (`/c4/view/<name>`) resolves through the **LikeC4 Proxy** to the in-cluster `likec4` Service.
- The **LikeC4 Proxy** is a passthrough — single fixed upstream, no port, no alternate adapter — contrast with the **Code Execution Backend** which is a typed multi-adapter seam.
- A **D3 Widget** is a closed-catalog Block — the markdown picks a name, the client renders it from a shared catalog; the **TracedCode** Block reuses the **Code Execution Backend** via the existing `/api/run` (a tracer harness on the client wraps the source; no server-side seam was added).
- The **SPA Route** list in `AppRoutes` is read by both the client router and the server's `index.html` fallback — one declaration, two runtimes.
- Both the frontend-bundle routes and the Cortex asset routes serve files through one **Static File Server**.
- The **Mermaid** and **D2Inline** Blocks share one **Diagram Zoom** modal; they differ only in how each obtains its SVG.

## Example dialogue

> **Reviewer:** "If a **Chapter** moves to a new **Section**, does its **Slug** change?"
> **Author:** "Yes — slugs are derived from the path. Renaming or moving rewrites the URL. That's the whole point of being filesystem-driven; no separate manifest to drift."
>
> **Reviewer:** "What if Mongo is down when an `/api/hello` comes in?"
> **Author:** "**Degraded** mode. Postgres still increments, the **Greeting** still ships back, the **Hello Event** is dropped (logged). `/api/recent` is missing that entry; that's the trade."
>
> **Reviewer:** "Why isn't the **LikeC4 Proxy** a Code Execution Backend?"
> **Author:** "Different shape. The **Code Execution Backend** is a typed seam with two adapters (Piston + Code Runner) and orchestration in between. The **LikeC4 Proxy** is one hard-coded upstream, byte-level passthrough, no internal seam. *One adapter means a hypothetical seam* — we'd only promote it to a Backend if a second LikeC4-flavoured renderer (Structurizr, Mermaid C4) ever showed up."

## Flagged ambiguities

- "Page" is used in two senses: a React route (`HomePage`, `ChapterPage`) and a Chapter. **Resolved**: **Page** = React route; **Chapter** = content unit.
- "Module" is used in two senses: a Scala package/file group, and the architecture sense from `LANGUAGE.md` (anything with an interface and an implementation). **Resolved**: when discussing architecture, "module" = architecture sense; when discussing code structure, prefer "package" or "file."
- "Knowledge Base" appears in older docs (and the `content/knowledge/codefolio-onboarding/` path noted in memory). The current canonical term is **Cortex**; the rename happened in commit `8f6d8c4`. Treat any "Knowledge Base" reference as a stale alias.
</content>
</invoke>