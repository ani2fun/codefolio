// Single entry point for the trace-driven Visualise renderer (ADR-0018). The
// Scala.js Visualise modal calls `renderWidget(containerId, jsonStr, onStep?)`;
// `jsonStr` is a `VizGraph` payload produced by `HeapToGraph` and serialized with
// circe.
//
// This module — and everything it imports — is plain TypeScript + D3. It has no
// knowledge of Scala, Scala.js, or React.

import type { VizGraph } from "./types";
import type { LayoutFn } from "./tree-layout";
import { renderGraph } from "./graph-render";
import { treeLayout } from "./tree-layout";

// layoutHint → layout. Adding a data structure adds an entry here, never a renderer.
const LAYOUTS: Record<string, LayoutFn> = {
  "binary-tree": treeLayout,
};

/**
 * Render a `VizGraph` into the DOM element with id `containerId`. The layout is
 * chosen from `data.layoutHint`; `onStep` (if given) fires with the step index
 * whenever the animation advances, so a host can keep a code pane in sync.
 * Malformed input degrades to an inline error message rather than throwing.
 */
export function renderWidget(
  containerId: string,
  jsonStr: string,
  onStep?: (index: number) => void,
): void {
  const container = document.getElementById(containerId);
  if (container === null) {
    console.error(`[d3] renderWidget: container #${containerId} not found`);
    return;
  }

  let data: VizGraph;
  try {
    data = JSON.parse(jsonStr) as VizGraph;
  } catch (err) {
    container.textContent = "Widget error: could not parse visualization data.";
    console.error("[d3] renderWidget: JSON parse failed", err);
    return;
  }

  const layout = LAYOUTS[data.layoutHint] ?? treeLayout;
  renderGraph(container, data, layout, onStep);
}
