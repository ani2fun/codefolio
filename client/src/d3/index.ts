// Single entry point for the trace-driven Visualise renderer (ADR-0018). The
// Scala.js Visualise modal calls `renderWidget(containerId, jsonStr, onStep?,
// caseIndex?)`; `jsonStr` is a `VizCases` payload produced by `HeapToGraph` and
// serialized with circe.
//
// This module — and everything it imports — is plain TypeScript + D3. It has no
// knowledge of Scala, Scala.js, or React.

import type { VizCases } from "./types";
import type { LayoutFn } from "./tree-layout";
import { renderGraph } from "./graph-render";
import { treeLayout } from "./tree-layout";
import { graphLayout } from "./graph-layout";
import { arrayLayout } from "./array-layout";
import { linkedListLayout } from "./linked-list-layout";
import { gridLayout } from "./grid-layout";

// layoutHint → layout. Adding a data structure adds an entry here, never a
// renderer. The generic `graph` layout doubles as the fallback for an
// unrecognised hint (see `renderWidget`), so the book can roll out on
// `viz=graph` before every bespoke layout lands.
const LAYOUTS: Record<string, LayoutFn> = {
  "binary-tree": treeLayout,
  graph: graphLayout,
  array: arrayLayout,
  "linked-list": linkedListLayout,
  grid: gridLayout,
};

/**
 * Render a `VizCases` payload into the DOM element with id `containerId`. A DSA
 * `main` usually runs several test cases; `caseIndex` (default 0) selects which
 * one to draw — the modal owns the "Case N / M" selector and re-invokes this
 * with a new index on a switch. The layout is chosen from the case's
 * `layoutHint` (an unrecognised hint falls back to the generic `graph`
 * layout); `onStep` (if given) fires with the step index whenever the
 * animation advances, so a host can keep a code pane in sync. Malformed input
 * degrades to an inline error message rather than throwing.
 */
export function renderWidget(
  containerId: string,
  jsonStr: string,
  onStep?: (index: number) => void,
  caseIndex?: number,
): void {
  const container = document.getElementById(containerId);
  if (container === null) {
    console.error(`[d3] renderWidget: container #${containerId} not found`);
    return;
  }

  let data: VizCases;
  try {
    data = JSON.parse(jsonStr) as VizCases;
  } catch (err) {
    container.textContent = "Widget error: could not parse visualization data.";
    console.error("[d3] renderWidget: JSON parse failed", err);
    return;
  }

  const cases = data.cases ?? [];
  if (cases.length === 0) {
    container.textContent = "Widget error: no test cases to visualize.";
    return;
  }

  const idx = Math.max(0, Math.min(cases.length - 1, caseIndex ?? 0));
  const graph = cases[idx];
  const layout = LAYOUTS[graph.layoutHint] ?? graphLayout;
  renderGraph(container, graph, layout, onStep);
}
