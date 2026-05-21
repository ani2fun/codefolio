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
  kind: string;
  meta: VizField[];
}

export interface VizEdge {
  from: string;
  to: string;
  label: string;
}

// A frame-local variable pointing into the structure — drawn as a labelled caret over the node.
export interface VizCursor {
  name: string;
  target: string;
}

export interface VizGraphStep {
  nodes: VizNode[];
  edges: VizEdge[];
  cursor: VizCursor[];
  highlight: string[];
  annotation: string;
  line: number;
}

export interface VizGraph {
  steps: VizGraphStep[];
  layoutHint: string;
  title: string;
  truncated: boolean;
}
