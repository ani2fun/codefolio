# Trace format validated against the 16 planned bespoke renderers

The visualiser pipeline today is `tracer JSON → MarkedTrace.decodeTrace →
HeapTrace → HeapToGraph.adapt → VizCases → d3 renderer`. ADR-0024 committed to
16 bespoke renderers (one per data-structure shape) on top of this pipeline,
and the longer "visualiser architecture migration" plan (link below) needs to
know — BEFORE the pipeline is refactored — whether the existing `HeapTrace`
primitive set (`Instance` / `Arr` / `Dict` + `Ref` / `Scalar`) can in fact
express every one of those 16 shapes.

**This ADR records the answer (corrected after a post-implementation audit):
the trace FORMAT is sufficient, but the generic RENDERER and the planned Scala
CODEGEN are not — and that reshapes Phase 1.** All three hardest-shape fixtures
*adapt* cleanly (the heap holds every needed reference; no `Set` primitive, no
cycle annotation, no richer Instance metadata required). But:

1. **Rendering**: the generic multi-card renderer drops edges that cross card
   boundaries, so graph / hashmap / restructuring-tree shapes render with their
   core structure invisible. These shapes need **bespoke renderers** (ADR-0024).
   The Phase-0 fixture tests asserted card counts, not structure, so they passed
   on broken output — corrected below.
2. **Schema codegen**: `openapi-typescript` handles the `oneOf` design cleanly;
   `sbt-openapi-codegen 1.11.22` does **not** (verified by running it). Phase 1's
   "single schema → codegen both sides" premise is revised to "yaml → TS codegen;
   Scala hand-written + circe + conformance test".

The Slice-1 stack chapters are pinned as additional regression baselines (spec
written; PNGs pending a backend-up capture run).

## Plan + scope

Plan: `~/.claude/plans/before-we-proceed-further-swirling-storm.md` (Phase 0
deliverables 1–8). Phases 1–6 follow; Phase 1 starts only once every Phase 0
exit criterion is met (see below).

This ADR documents the *evidence*. Specific implementation decisions about
the next-phase schema location and adapter hosting are recorded in the plan
itself; cited here so each can be revisited if the evidence reveals a flaw.

## Three hardest-shape fixtures

`shared/src/test/scala/codefolio/shared/viz/HeapTraceFixtures.scala` defines
three fixtures — the deliberate hardest cases per family — and their adapter
spec proves each one adapts cleanly. The companion Playwright tests render
them via the production d3 pipeline and capture a baseline screenshot.

### 1. `avlRotationTrace` — recursive Instance with rich metadata

Three `AvlNode(val, left, right, height, balance)` instances around a single
right rotation. Tests:

- Recursive `Instance` chained by `left`/`right` Refs.
- Per-node scalar metadata (`height`, `balance`) → surfaces as `VizField` sub-labels.
- Edge-label diff across the rotation (the `left`/`right` slot swap).
- 3 nodes per step preserved; per-card `layoutKind = tree-binary` inferred.

**Limitation discovered.** The adapter resolves `rootId` once per segment from
the first step's `root` local. When `root` rebinds during the trace (as it
does in a rotation — the new subtree root is a different `Instance`), the
adapter keeps the original rootId and the rotated subtree's other two nodes
appear as `removed` (faded) under their original card while the new root
stands alone in a fresh card. The rendered shape is therefore "rotated-out
nodes fading in card A; new root alone in card B" instead of "tree rebalanced
in one card". Three options for Phase 1+:

1. Re-resolve `rootId` per step from the named local (cheap; risks bouncing
   between roots in noisy traces).
2. Track the most-recently-bound named local across the segment and union
   the per-step subgraphs (more robust; needs careful diffing).
3. Keep current behaviour and document AVL-class shapes as "use
   `viz-kind=tree` with explicit hint instead of relying on local rebinding".

Decision deferred to Phase 1. This ADR captures the limitation so it isn't
re-discovered as a Phase 2 regression.

### 2. `graphBfsTrace` — graph with cycles, revisits, and an off-root visited set

4 `GraphNode(id, neighbors: Ref→Arr[Ref])` instances forming `A → B → D → A`
(cycle) plus `A → C → D`. BFS from A with a `visited` Dict and a `queue` Arr
in the active frame's locals. Tests:

- Cycle representation — `D.neighbors → [A]` re-references a reachable node.
  No format crutch needed (no special "visited" annotation, no cycle break).
- Revisit handling at the algorithm level — `visited` lives in the locals,
  not in the renderable graph; the trace expresses "the algorithm checked
  this set" without the format itself needing a `Set` primitive.
- Per-card inference — GraphNode → `graph-generic`; each adjacency Arr →
  `array-1d`; 4+4 = 8 lifetime cards.

**Observation.** The `visited` Dict and the `queue` Arr in the frame locals
are NOT reachable from `root` (which is the entry GraphNode), so they don't
surface as their own cards. This is intentional today — the renderable shape
is the GRAPH being traversed, not the algorithm's bookkeeping. Phase 1 could
add a "show locals as additional cards" affordance; the format already
supports it (locals carry Refs), so no format change is needed.

### 3. `hashmapChainedCollisionsTrace` — Dict-of-Arr-of-Instance with redundant chain

A Dict mapping bucket index (Int scalar) → Ref(Arr) where each Arr's items
are Refs to `Entry(key, value, next)` Instances. The `next` field on each
Entry redundantly expresses the bucket chain. Tests:

- Dict with scalar keys + Ref values (the standard hashmap shape).
- Arr of Refs to Instances (the bucket array).
- Instances with mixed scalar + Ref fields (the Entry node with `next`).
- Real hashmap traces emit BOTH the bucket Arr ordering AND the per-Entry
  `next` ref — the format accommodates this redundancy without ambiguity.
- 5 lifetime cards: Dict + 2 bucket Arrs + 1 collision-chain Entry card
  (n_apple ↔ n_grape) + 1 singleton Entry card.

**Outcome for `Set` primitive.** The plan flagged "may need a `Set` heap
object kind" as a possible extension. Not needed — `Dict[K, I(1)]` with the
adapter's existing Dict layout-kind ("hashmap") is sufficient for the test
fixtures and any production set in a tracer (Python's `set` becomes
`{type: "dict"}` in the harness output by convention; Java's `HashSet`
becomes the same).

## Companion: Slice-1 chapter regression baselines

`client/test/e2e/stack-baselines.spec.ts` adds 3 representative Slice-1
stack chapters:

- `stack-inversion` — Group A canonical sweep target.
- `parentheses-checker` — Group A different stack pattern.
- `succeeding-inferior-element` — Group C, root-switched from `next_smaller`
  to `stack`.

Each test waits for the bespoke `.stack-renderer` block to mount through the
real trace flow (Visualise click → `/api/run` → MarkedTrace.decodeTrace →
HeapToGraph.adapt → d3 renderer). When the dev backend isn't reachable, the
tests skip cleanly so the Phase-0 suite stays green locally. Phase 5 expands
this with request-mocking and pixel-tolerance tuning for CI; Phase 0 ships
the pattern.

## Schema-design prototype (oneOf round-trip) — corrected after running the real codegen

> **Correction (post-audit).** An earlier draft of this ADR claimed "both
> codegens accept oneOf cleanly; stay with the design." That was based on the
> TS codegen plus *hand-written* Scala codecs. When the actual
> `sbt-openapi-codegen` was run against the prototype (a throwaway `vizproto`
> sbt project), it did **not** produce compiling Scala. The corrected findings
> are below. This is exactly the gap-4 risk the prototype existed to surface —
> it surfaced.

`viz-schema-prototype.yaml` encodes the three riskiest sum types: `HeapObject
= Instance | Arr | Dict`, `HeapValue = Scalar | Ref`, `HeapScalar = I | D | B
| S | Null`, with discriminator field `kind`.

**TypeScript codegen** (`openapi-typescript 7.13`) — ✅ **works.** Emits clean
discriminated unions (`HeapObject = HeapInstance | HeapArr | HeapDict`); TS
narrows on `obj.kind`. Verified via `npm run codegen:viz-proto`.

**Scala codegen** (`sbt-openapi-codegen 1.11.22`) — ⚠️ **does NOT cleanly
work.** Verified empirically by pointing a throwaway `vizproto` project at the
prototype yaml:

| Aspect | Result |
|---|---|
| `oneOf` → `sealed trait` + case-class variants + `def kind: String` | ✅ Generated correctly (`VizProto.scala`) — the shape we want. |
| circe `Encoder`/`Decoder` with `kind` discriminator dispatch | ✅ Generated correctly (`VizProtoJsonSerdes.scala`) — **but only when a type is used as an endpoint BODY.** A `paths: {}` schemas-only file emits **no** circe codecs. |
| tuple-as-array (`items: { oneOf: [string, $ref] }`) | ❌ `scala.NotImplementedError` in the circe serde generator. **Must encode tuples as objects** (`{name, value}` / `{key, value}`). Fixed in the prototype. |
| tapir `Schema` file (`VizProtoSchemas.scala`) | ❌ References phantom `*Kind` types (`HeapScalarBKind`, `HeapInstanceKind`, …) for every single-value discriminator enum → **does not compile** (plugin bug). |
| Can the broken Schema file just be excluded? | ❌ No — `VizProto.scala` (the classes) `import VizProtoSchemas._`, so dropping it breaks the classes too. |
| enum codec derivation | ❌ Pulls in `org.latestbit:circe-tagged-adt-codec` (not a current dep) and still errors on the `derives` clause. |

`VizSchemaPrototypeSpec` (Scala) still passes — it round-trips *hand-written*
discriminator codecs and proves **circe itself** handles discriminated unions
(canonical Instance JSON, all 5 scalars, unknown-kind rejection). It does
**not** prove the *plugin* emits them — the table above is the authority on
that. (Note: the spec uses array-encoded tuples, which predates the
"tuples-must-be-objects" finding; the real schema uses object encoding.)

**Decision (revised): do NOT treat `sbt-openapi-codegen` as a drop-in for the
viz types.** Getting compiling circe codecs out of it requires *all* of:
object-tuples + a dummy endpoint + adding `circe-tagged-adt-codec` +
suppressing/patching the broken Schema file and its import. That's fragile and
high-effort. The recommended Phase-1 path is therefore **yaml → TS via
openapi-typescript (works); Scala stays hand-written + circe with a
yaml-conformance test** — capturing the drift-reduction goal (the TS↔Scala
contract is where drift actually bit) without betting on a buggy codegen.
Alternatives if full Scala codegen is still wanted: upgrade the plugin to a
newer release and re-run this experiment, or switch Scala codegen tools. This
reshapes Phase 1 (see the migration plan).

## Decode-path latency baseline

`client/test/bench/decode-latency.bench.ts` baselines the current
`MarkedTrace.parse → js.JSON.parse + structural walk` cost on a 500-step
synthetic trace (~680 KB — the plan said ~30 KB; the larger size is closer
to a real `graph-bfs` modal payload at 2.3 KB/step). Captured numbers
recorded in `docs/perf-baselines.md`:

- mean **4.46 ms**, p75 4.49 ms, p99 6.57 ms (Node 20, vitest 4, baseline
  commit `fa835495`).

The benchmark walks the same shape `MarkedTrace.decodeTrace → decodeStep →
decodeFrame → decodeValue` does — V8's `JSON.parse` plus a hand walk. Phase 2
will swap `js.JSON.parse + walk` for `circe.decode[HeapTrace]` on Scala.js;
the budget is **p99 ≤ 13 ms** (= 2× Phase-0 p99). Past that, the plan
fallback (keep `js.JSON.parse` with codegen'd runtime validators) kicks in.

## Tracer-output schema gate

`bin/validate-tracer-output.py` is a scaffolded CI gate that validates raw
tracer JSON (the between-markers payload) against the schema MarkedTrace
already implicitly assumes. Today it works as:

- `--self-test` — validates an inline fixture exercising every variant of
  `HeapObject` + `HeapValue`; PASS.
- `--check-adapter-fixtures` — validates the Phase-0 `VizCases` fixtures
  against the TRACER schema (they're the wrong shape on purpose); all 3
  REJECTED-AS-EXPECTED, proving the gate isn't too permissive.

Phase 1 wires this script to actual tracer invocations: run `PythonTracer.wrap`
against a tree-insert program, run `tools/jvm_tracer/run_probe.sh` against a
BFS program, capture markered output, validate. If either tracer drifts from
`viz-schema.yaml`, the CI gate blocks the merge. Phase 0 ships the scaffold;
Phase 1 closes the loop.

## Phase 0 exit criteria — corrected status

The infrastructure landed, but a post-implementation audit (viewing the actual
baselines + running the real Scala codegen) found that two exit criteria were
**reported met but not actually met**. Honest status:

- ✓ Playwright installed; smoke test green; harness + fixture pipeline real.
- ⚠️ **3 trace-shape tests pass but validate the wrong thing.** They assert
  card counts + "no console errors", not structural correctness. Viewing the
  baselines shows the generic multi-card renderer **drops cross-card edges**,
  so graph-bfs renders as 8 edge-less cards, hashmap loses its bucket→chain
  mapping, and AVL (root rebind) shows the tree from the stale root with the
  real tree greyed as "removed". The format is fine; the **generic fallback
  renderer is not** for these shapes. Decision taken: graph/hashmap/AVL get
  **bespoke renderers** (ADR-0024) that draw their own edges; the fixtures
  must be re-pointed to validate *those*, asserting edges/structure.
- ⚠️ 3 Slice-1 baselines wired but **no PNG baselines captured** (needs a
  `./bin/dev`-up run). The "Phases 1–3 keep these green" net is not yet active.
- ✓ Decode latency recorded — but it measures a **TS proxy in Node**, not the
  real Scala.js decode in a browser. Treat the p99 budget as provisional until
  re-baselined in-browser (Phase 2/6).
- ✗ **`oneOf` prototype: TS codegen works; Scala `sbt-openapi-codegen` does
  NOT** (see the corrected section above). This *changes Phase 1's approach*.
- ✓ Tracer-output validation **scaffold** in place (self-test PASS). It does
  not yet invoke the real tracers — gap 1's "catch drift now" is deferred to
  Phase 1, which is acceptable since the schema it validates against is itself
  a Phase 1 deliverable.
- ✓ ADR-0025 (this file) — written, then corrected by the audit.

## Consequences

- The trace **format** (`Instance/Arr/Dict + Ref/Scalar`) is sufficient — all
  the references for graph/hashmap/tree-restructure are present in the heap.
  The work is in the **adapter + renderers**, not the format.
- **Graph, hashmap, and restructuring-tree shapes need bespoke renderers** (or
  an adapter that groups + draws cross-collection edges). This is a first-class
  design item for the renderer SDK, not a footnote. Several of slices 2–16 are
  these shapes.
- **Phase 1 is reshaped**: do not rely on `sbt-openapi-codegen` for the viz
  types. yaml → openapi-typescript for TS; Scala hand-written + circe with a
  yaml-conformance test. This shrinks Phase 1 and de-risks it.
- The current fixture screenshots are baselines of **known-imperfect** generic
  output; they should be re-captured once the bespoke renderers exist, not
  defended as "correct".

## Honest caveats

- The decode-latency bench is Node-not-browser and uses a TS reimplementation,
  not the real Scala.js `decodeTrace`. The number is a rough proxy; re-baseline
  in-browser before trusting the Phase 2 "within 2×" gate.
- The Slice-1 baselines need a one-off `./bin/dev`-up capture run.
- The `VizSchemaPrototypeSpec` proves *circe* handles discriminated unions, not
  that the *plugin* emits them — the codegen table above is the authority.
