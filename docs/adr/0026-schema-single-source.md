# Schema single-source for the viz render contract — yaml + TS codegen, hand-written Scala + conformance test

The viz pipeline keeps three representations of the render payload in sync by
hand: `VizGraph.scala` (Scala case classes + circe `Encoder`), `types.ts`
(TypeScript interfaces the d3 renderer imports), and — until now — a regex
codegen (`bin/gen-ts-types.py`) bridging the two. Slice 0's `structureType`
addition nearly shipped with the TS side silently out of sync. This ADR makes
**`viz-schema.yaml` the single source** for that contract and replaces the
hand-maintained bridge with codegen + drift guards.

This is the **"LITE"** form of the original migration Phase 1. The original
plan assumed `sbt-openapi-codegen` would generate *both* the Scala and TS
types from the yaml. ADR-0025's audit (running the plugin for real) showed it
**can't** do that cleanly for our types. So the Scala side stays hand-written
and is held to the yaml by a conformance test instead of generated from it.

## Why not `sbt-openapi-codegen` for Scala (the original plan)

Verified empirically in ADR-0025 (a throwaway `vizproto` project pointed at a
prototype yaml). `sbt-openapi-codegen 1.11.22`:

- generates good `sealed trait` + case classes and good circe codecs — **but
  only for endpoint *bodies*; a `paths: {}` schemas-only file emits no codecs**;
- its tapir `Schema` file references phantom `*Kind` types for single-value
  discriminator enums → **does not compile**, and the generated classes
  `import` that file so it can't simply be excluded;
- needs a non-present `circe-tagged-adt-codec` dependency for enum codecs.

`openapi-typescript` handles the same yaml perfectly. So: **yaml → TS via
openapi-typescript; Scala stays hand-written + circe, guarded by a conformance
test.** This captures the drift-reduction goal (the TS↔Scala contract is where
drift actually bit) without betting the build on a buggy codegen.

## Scope: the render contract only, not the trace wire format

A realization from implementation: there are **two different contracts** the
original plan conflated into "one viz-schema.yaml":

| Contract | Producer → consumer | Shape | Source of truth |
|---|---|---|---|
| **Viz render** (`VizCases`) | `HeapToGraph.adapt` (Scala, circe) → d3 renderer (TS) | circe field names; `kind`-less | **`viz-schema.yaml`** (this ADR) |
| **Trace wire** (`HeapTrace`) | Python / Java tracer → `MarkedTrace.decodeTrace` (Scala) | `type`-discriminated (`{type:"list", items}`, `{ref:id}`, locals/heap as objects) | embedded schema in `bin/validate-tracer-output.py` |

They have different discriminators, different consumers (the trace wire format
has **no TS consumer** — decoding happens in Scala), and the Scala `HeapTrace`
uses idiomatic tuples / `Map`s that don't 1:1 map to either wire shape. So
`viz-schema.yaml` covers the **render contract only**. The trace wire format
stays in the tracer gate; unifying it into a `trace-wire-schema.yaml` is future
work, best done in the server-move phase (which is the next place tracer output
handling is touched).

## The pipeline

```
viz-schema.yaml                       ← single source for the render contract
  │
  ├─ npm run codegen:viz (openapi-typescript)
  │     → client/src/d3/types.generated.ts   (components["schemas"][...])
  │     → client/src/d3/types.ts             (thin re-export: flat aliases)
  │
  └─ VizSchemaConformanceSpec (shared, snakeyaml)
        asserts VizGraph.scala case-class fields == yaml properties,
        and that every property is `required` (circe always emits every
        field → openapi-typescript must emit `T`, not `T?`).

VizGraph.scala                        ← hand-written; circe Encoder; Scala SoT
```

`types.ts` is a stable thin shim (`export type VizNode =
components["schemas"]["VizNode"]`, ×10) so the ~20 `import type { … } from
"./types"` callsites are untouched. The whole d3 renderer consumes only the
Viz render types (it never sees `HeapTrace`).

## Drift guards (replacing the retired `bin/gen-ts-types.py` + its test)

- **TS side**: `client/src/d3/types-codegen.test.ts` regenerates from the yaml
  into a temp file and asserts it matches the committed `types.generated.ts`
  (fails if someone edits the yaml without `npm run codegen:viz`). Pins the
  nullable invariants (`slot: number | null`, `structureType: string | null`).
- **Scala side**: `VizSchemaConformanceSpec` (above). Name-level + required
  invariant; not type-level (brittle, low marginal value — the nullable types
  are pinned on the TS side).
- **`bin/dev`** runs `npm run codegen:viz` once per session so the working tree
  stays fresh during iteration.

Adding a render field is now a deliberate **three-touch**: `VizGraph.scala` +
`viz-schema.yaml` + `npm run codegen:viz`. Miss any one and a guard fails.

## What this does NOT do (deferred)

- **No circe `Decoder`s yet.** `VizGraph` has Encoder-only; `HeapTrace` has no
  codecs. The server-move phase adds hand-written decoders when the server
  starts *consuming* `VizCases` / the request body.
- **No `tsc` gate.** The project type-checks via esbuild (no `tsc`), and a
  one-off `tsc --noEmit` surfaces pre-existing d3-selection generic noise + a
  redundant comparison in `stack-renderer.ts` — unrelated to this change.
  Adding a real `tsc` gate is a separate cleanup.
- **No real-tracer wiring** of `bin/validate-tracer-output.py` — deferred to
  the server-move phase (trace wire format is a separate contract anyway).

## Consequences

- The TS↔Scala render contract can no longer silently drift — the exact
  Slice-0-style regression that motivated the audit is now caught two ways.
- `viz-schema.yaml` is human-authored OpenAPI 3.0 (not generated). It carries
  the field-level docs as one-liners; the full prose stays in `VizGraph.scala`.
- The Scala types remain idiomatic (defaults, `Option`) — not codegen-shaped —
  which keeps `HeapToGraph` and the specs unchanged.
- If a future `sbt-openapi-codegen` (or a different tool) gains clean oneOf +
  schemas-only support, the Scala side *could* move to generation; the
  conformance test would become the equivalence check during that migration.

## Status

Done: `viz-schema.yaml`; `codegen:viz` + `types.generated.ts` + `types.ts`
shim; `bin/gen-ts-types.py` + old drift test retired; new TS drift guard;
`VizSchemaConformanceSpec`; `bin/dev` integration. Verified: `sbt
sharedJVM/test` green (incl. 11 conformance tests), `npm test` green (incl. the
drift guard), `sbt compile` clean.
