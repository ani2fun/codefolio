// TypeScript mirror of the `VizGraph` JSON contract (ADR-0018) — keep in sync with
// shared/src/main/scala/codefolio/shared/viz/VizGraph.scala.
//
// This is the *only* thing the pure-D3 renderer knows about Scala: a JSON shape.
// `HeapToGraph` produces a `VizGraph`; the Scala.js Visualise modal circe-encodes
// it and hands the string to `renderWidget`.

export interface VizField {
  name: string;
  value: string;
}

export interface VizNode {
  id: string;
  label: string;
  // The originating Python class for an instance, or a synthetic kind for a
  // collection member: "cell" (an array element) or "entry" (a dict entry).
  kind: string;
  meta: VizField[];
  // An array cell's 0-based index; `null` for instances and dict entries.
  // The array layout (A1) places cells by `slot`.
  slot: number | null;
}

export interface VizEdge {
  from: string;
  to: string;
  label: string;
}

// A frame-local variable pointing into the structure — drawn as a labelled, role-coloured
// caret over the node. `color` is the role colour assigned by HeapToGraph (MarkerColors).
export interface VizCursor {
  name: string;
  target: string;
  color: string;
}

export interface VizGraphStep {
  nodes: VizNode[];
  edges: VizEdge[];
  cursor: VizCursor[];
  // `highlight` is the persistent layer (a new node keeps its tint); `changed` and
  // `removed` are the transient layer — a one-step flash / fade. A `removed` node is
  // re-emitted into `nodes` for this step only so it can be drawn fading out.
  highlight: string[];
  changed: string[];
  removed: string[];
  annotation: string;
  line: number;
}

// One traced test case's animation.
export interface VizGraph {
  steps: VizGraphStep[];
  layoutHint: string;
  title: string;
  truncated: boolean;
}

// The full payload `renderWidget` receives — one VizGraph per traced test case
// (a DSA `main` usually runs several). `renderWidget` is told which case to draw
// via its `caseIndex` argument; the modal owns the "Case N / M" selector.
export interface VizCases {
  cases: VizGraph[];
}
