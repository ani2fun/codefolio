---
title: Extending the Project
summary: Three concrete recipes — add a knowledge chapter, add an API endpoint, add a runnable language. Follow these and you can do almost any feature work.
group: Working on it
---

## Recipe 1: add a knowledge chapter

The smallest meaningful change. Five minutes, no rebuild needed.

1. Pick a book — say `distributed-systems`.
2. Drop a markdown file into `content/knowledge/distributed-systems/<slug>.md` with frontmatter:

   ```yaml
   ---
   title: Quorums in Practice
   summary: How read/write quorums actually behave under partial failure.
   group: Foundations
   ---

   ## Section 1
   ...
   ```

3. Add an entry to `content/knowledge/distributed-systems/meta.json`:

   ```json
   {
     "slug": "quorums-in-practice",
     "title": "Quorums in Practice",
     "group": "Foundations"
   }
   ```

4. Reload the chapter URL in the browser. The server reads from disk on every request — no restart.

The `slug` in `meta.json` **must** match the filename without `.md`. The server's `KnowledgeHandler` does a path-traversal-safe lookup against `meta.json`, then opens `<slug>.md`. Slugs not in `meta.json` 404.

### Adding a new book

Same pattern, one level up. Create `content/knowledge/<book>/meta.json` with a `chapters` array, then add a row to `content/knowledge/meta.json`:

```json
{
  "slug": "<book>",
  "title": "...",
  "description": "...",
  "tags": ["..."],
  "estimatedReadingMinutes": 20
}
```

The home page's "Knowledge Base" preview pulls from `/api/knowledge/index` and shows whatever's there — no other code change needed.

## Recipe 2: add an API endpoint

This one threads through every layer. Take it slowly the first time.

```d2
direction: down
spec: api/openapi.yaml
shared: shared (codegen) {
  case_class: case classes
  endpoint: tapir Endpoint values
}
handler: server/handlers/MyHandler.scala
wire: server/HttpApp.scala
client: client/api/ApiClient.scala
component: client/components/...

spec -> shared.case_class
spec -> shared.endpoint
shared.case_class -> handler
shared.endpoint -> wire
wire -> handler
shared.case_class -> client
shared.endpoint -> client
client -> component
```

### Step-by-step

1. **Edit `api/openapi.yaml`.** Add the path, request body schema, response body schema, and any error schema. Reference shared `ApiError` for failures so error shapes stay consistent across the API. Use camelCase for JSON fields — circe codecs match that by default.

2. **Run `sbt compile`.** Codegen emits new case classes and a new `Endpoints.<yourEndpoint>` value into `shared/`. Both server and client now break — that's the codegen safety net at work.

3. **Write the handler.** Create `server/.../handlers/MyHandler.scala`:

   ```scala
   trait MyHandler:
     def doThing(req: MyRequest): IO[MyFailure, MyResponse]

   final class MyHandlerLive(/* injected services */) extends MyHandler:
     def doThing(req: MyRequest): IO[MyFailure, MyResponse] = ???

   object MyHandler:
     val live: ZLayer[Deps, Nothing, MyHandler] = ZLayer.fromFunction(MyHandlerLive(_))

   enum MyFailure:
     case Invalid(detail: String)
     case NotFound

   object MyFailure:
     def toApiError(f: MyFailure): ApiError = f match
       case Invalid(d) => ApiError("Invalid input", Some(d), None)
       case NotFound   => ApiError("Not found", None, None)
   println("Handler boundary uses domain failures, not HTTP codes.")
   ```

   The handler is **not** HTTP-aware. It returns domain failures; the HTTP layer maps them to status codes.

4. **Wire it up in `HttpApp.scala`.** Pattern-match on the failure to choose a status:

   ```scala
   private val myEndpoint: ZServerEndpoint[Any, Any] =
     Endpoints.myEndpoint
       .errorOut(statusCode and jsonBody[ApiError])
       .zServerLogic { req =>
         myHandler.doThing(req).mapError { f =>
           val status = f match
             case MyFailure.Invalid(_) => StatusCode.BadRequest
             case MyFailure.NotFound   => StatusCode.NotFound
           (status, MyFailure.toApiError(f))
         }
       }
   println("Add to apiEndpoints: List[...]; Main.scala provides the layer.")
   ```

5. **Add the live layer to `Main.scala`'s `provide(...)` block.**

6. **Add the client method to `ApiClient.scala`.** sttp + tapir gives this for free:

   ```scala
   private val doThingRequest:
       MyRequest => sttp.client3.Request[Either[Unit, MyResponse], Any] =
     SttpClientInterpreter().toRequestThrowDecodeFailures(Endpoints.myEndpoint, baseUri)

   def doThing(req: MyRequest): Future[MyResponse] =
     backend.send(doThingRequest(req)).flatMap {
       case res if res.body.isRight => Future.successful(res.body.toOption.get)
       case res                     => Future.failed(RuntimeException(s"Failed (${res.code.code})"))
     }
   println("Same shape as runCode, getKnowledgeIndex, etc.")
   ```

7. **Use it in a component.** Standard `useEffectWithDepsBy(...) { ApiClient.doThing(req).onComplete { ... } }` shape.

### Common mistakes

- Forgetting step 5 (the layer). The compiler will complain about an unsatisfied dependency, but the message is verbose; don't panic.
- Adding HTTP status codes inside the handler instead of in `HttpApp.scala`. Keep the handler pure-domain; only the wiring layer should know about HTTP.
- Skipping the `errorOut(...)` pairing on the endpoint. Without it, any failure becomes a 500 with no body — the client has nothing to display.

## Recipe 3: add a runnable language

Touches both server (catalog) and client (Prism syntax highlighting).

1. **Add to `server/.../runner/Languages.scala`.** Each entry has a slug, label, Piston language id, and Code Runner language id (Judge0's numeric ids). Find the right ids on Piston's docs and Judge0's reference.

2. **Add to `client/src/markdown/runtime.ts`'s Prism setup.** Prism needs the grammar imported and registered:

   ```ts
   import "prismjs/components/prism-elixir";
   ```

   Without this, `highlightWithPrism(code, "elixir")` falls back to plain text and the editor shows un-highlighted source.

3. **Update the OpenAPI enum, if you use one.** `RunRequest.language` is a free-form string in the spec, but if you've added validation, extend the validator to include the new language.

4. **Smoke test.** Open a chapter, write ` ```elixir run ` ... ``` , and click Run. If Piston is up and the language id matches, output appears. If not, check the network tab — Piston returns a recognisable error JSON for unknown languages.

## Recipe 4: change the look of a chapter

This one is split between two files. The split surprises people.

- **HTML emitted** by the markdown pipeline → edit `client/src/markdown/render.ts` and the `codeHandler` / pre-passes within it.
- **CSS for the rendered HTML** → edit `client/.../components/knowledge/ChapterContent.scala`'s `<.article>` className. The `prose prose-blue ...` classes there control everything that's plain prose. For specific elements (D2 cards, runnable blocks, mermaid blocks), edit the relevant component under `client/.../components/knowledge/`.

When in doubt, **inspect the element in dev tools**. The class names map back to file names: `runnable-code` placeholder → `RunnableCodeBlock.scala`. `d2-diagram` → `D2Diagram.scala`. `mermaid-block` → `MermaidBlock.scala`. Each is the entire React tree for that placeholder, and is grep-able.

## Recipe 5: rename a route

Two places to change, in this order:

1. `client/.../Page.scala` and `client/.../Router.scala` — add the new case, route rule, and any links that point to the old route.
2. `server/.../HttpApp.scala`'s `staticRoutes` — add the new path to the SPA fallback list. **Without this, a hard reload of the new route will return 404.**

If you forget step 2, the SPA works for in-app navigation but reloads die. That's a real bug we've shipped before.

## Recipe 6: bump a dependency

- **Scala / Java**: edit `build.sbt`, run `sbt update`, then `sbt compile` and `sbt test`. Watch for binary-incompatible bumps in tapir, zio, and circe — they go in waves.
- **JS**: edit `client/package.json`, then `cd client && npm install`. Run `npm run build` to confirm the bundle still builds. Heavy deps (mermaid, d2, shiki, katex) deserve a manual size check after upgrading: a 200KB regression on the home page is a regression worth catching.

## A general principle

When you're not sure where a change should go, **start with the OpenAPI spec or the markdown content**, never with the implementation. Both are pure data, both have schema validation, and both are read by code on multiple sides. If you can express the change as "the contract is now X" or "the content is now Y", the implementation falls out of the codegen and the existing rendering logic.

The places where the codebase becomes complicated are the places where we *can't* push down to data — JS interop, React state in placeholders, the dual-protocol code runner. Treat those as cost centers and keep them small.
