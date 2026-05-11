# Cortex Block discovery is a bytes↔values seam, mirrored on the client

The Cortex Chapter pipeline embeds five flavours of interactive **Block** in the rendered HTML — `RunnableCode`, `RunnableGroup`, `Mermaid`, `D2Slides`, `D2Inline`. Until this ADR was written, all five lived inside `client.components.cortex.ChapterContent` as a private `Block` trait + five render functions, each mixing the DOM walk, attribute extraction, URI-decoding, JSON-parsing, and `Option[VdomElement]` return into one place. Failures fell through to `dom.console.warn` with no test surface — the symmetric problem ADR-0004 solved on the server side for `PistonWire` / `CodeRunnerWire`.

**Decision**: the client takes the same shape as the server. Internal seams stop at the bytes↔values boundary; pure transformations live one step up.

  - **Pure structural decoders** live in `shared.cortex.Blocks`. Each variant has a typed decoder (`decodeRunnableCode`, `decodeRunnableGroup`, `decodeMermaid`, `decodeD2Slides`, `decodeD2Inline`) returning `Either[BlockDecodeError, Block]`. Cross-compiled JVM + JS, exercised by `BlocksSpec` against literal attribute / tab fixtures (no DOM, no JS-only helpers).
  - **The Scala.js DOM-walk + URI / JSON shims** live in `client.components.cortex.BlockDiscovery`. It owns `js.Dynamic.global.decodeURIComponent`, `js.JSON.parse`, the `data-tabs` JSON shape extraction, the per-`<div>.d2-slide>` child walk, and the `console.warn` logging. It produces a `DiscoveryResult(blocks, errors)` and feeds the decoders.
  - **`ChapterContent`** becomes a thin renderer: hooks lifecycle + click delegation + a total `Block => VdomElement` dispatch. Adding a new Block variant breaks the match exhaustively at compile time.

The trade is **a slightly larger client tree** (one extra file, one extra cross-tier import) for **a sharper test surface**:

  - Decoder bugs (missing-attribute handling, `runnable` defaulting to `true`, empty-content normalisation) surface in fast JVM unit tests.
  - The JS adapter is reduced to "extract attributes, URI-decode, JSON-parse, log warnings" — a thin shim that's hard to get wrong in interesting ways.
  - The JVM/JS asymmetry is honest: `js.Dynamic.global.decodeURIComponent` and `js.JSON.parse` cannot cross-compile, so they stay on the JS side; everything else moves to shared.

This is the client-tier sibling of ADR-0004. New Block types added in the future should follow the same pattern: a pure decoder in `Blocks`, a small extractor stanza in `BlockDiscovery`, an additional case in the `ChapterContent` render dispatch.
