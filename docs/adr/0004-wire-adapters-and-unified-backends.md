# Wire and FS adapters return values; pipelines own pure transformations

The Code Run and Cortex pipelines both have an internal seam between the IO world (bytes over the wire, bytes on disk) and the domain world (`RunResult`, `CortexIndex`). Two earlier shapes had drifted away from that line:

- **Code Run** had two parallel package-private traits — `Piston` and `CodeRunner` — each bundling protocol-specific wire parsing with HTTP transport. The interfaces were asymmetric (`supports` only on `Piston`; `CodeRunner` was the universal fallback). Wire-format mapping (Piston JSON → `RunResult`, Judge0 base64 envelope → `RunResult`) lived as private functions in the companion object with no spec, exercised only through stubbed `Task[RunResult]` fakes.
- **Cortex** had a single `Cortex` trait whose `buildState(mtime)` mixed FS scan with pure walker invocation. `FakeCortex.buildState` produced a pre-built `CortexState`, bypassing `CortexIndexWalker` entirely. Walker integration with FS-loaded trees was only exercised by one live-FS test.

**Decision**: internal seams stop at the bytes ↔ values boundary. Pure transformations live one step up, on the pipeline side.

  - **Code Run**: a single `CodeExecutionBackend` trait with `supports(lang)` + `run(...)`. Two adapters: `LivePistonBackend`, `LiveCodeRunnerBackend`. Wire-format mapping lives in pure modules `PistonWire` and `CodeRunnerWire` (request body builders + response parsers + Piston's id-to-name map), exercised by `PistonWireSpec` / `CodeRunnerWireSpec` against golden JSON fixtures. The orchestration walks `List[CodeExecutionBackend]` and picks the first that supports the language: empty list → `NotConfigured`, no support → `BadInput`. Code Runner is `supports = true` unconditionally because it is the universal fallback.
  - **Cortex**: the seam is `CortexFs` with `loadRoots: IO[CortexFailure, List[CortexEntry]]`. `CortexPipelineLive` owns the composition `loadRoots → CortexIndexWalker.walk → indexErrorToFailure → CortexState`. `FakeCortexFs` returns a literal `List[CortexEntry]`, so every fake-based test exercises the walker integration too.

The trade is **a slightly larger pipeline body** (the walker invocation, the priority-walk over backends) for **a sharper test surface**:

  - Wire-format bugs surface in pure unit tests, not via stubbed HTTP servers.
  - Walker errors flow through the same `loadAndCache` path tests use, so cache-invalidation and walker integration are tested by the same fixtures.
  - The asymmetric `supports` smell collapses: one trait, n adapters, instead of two parallel traits with diverging interfaces.

For new pipelines: when the live impl wraps a Java client over a wire format, place the wire-format module beside the live adapter (a `*Wire` companion to the live class). When the live impl walks a filesystem tree, the seam returns the in-memory tree, not the assembled domain state.
