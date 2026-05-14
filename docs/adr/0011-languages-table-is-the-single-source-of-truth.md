# The Languages table is the single source of truth for language dispatch

ADR-0004 put the Code Run pipeline's wire-format mapping in pure `PistonWire` / `CodeRunnerWire` modules.
But the *language dispatch* knowledge stayed scattered: `PistonWire` carried its own Judge0-id → Piston-name
`Map`, both wires hardcoded `lang.id == 62` to decide Java entrypoint rewriting, and `Languages` itself was
a shallow list callers read *around*. Adding a language meant editing three-plus files, with nothing
linking a `Languages` entry to the backend ids the wires actually sent.

**Decision**: `Languages` owns everything language-shaped. Its entry type is `Languages.Language`, which
*wraps* the codegen'd `RunnableLanguageInfo` (the public API shape — left untouched so a backend protocol
detail never leaks into the OpenAPI contract) and adds `pistonName: Option[String]` — the Piston runtime
name, or `None` when Piston can't run the language. `PistonWire.supports` is `pistonName.isDefined`;
`buildRequestBody` reads the name from the entry. The Java-entrypoint decision is `Languages.effectiveSource`
— one named `JavaId` constant in the table, called by both wires, so neither knows which language is
special. Adding a language is one entry in `Languages.all`; `LanguagesSpec` pins table consistency (unique
ids, unique aliases, every alias resolves back to its own language).

The trade is a wrapper type (`Language` rather than raw `RunnableLanguageInfo` everywhere) for **a registry
callers go through instead of read around**. This extends ADR-0004's rule — pure modules own protocol
mapping — to the per-backend id mapping itself.
