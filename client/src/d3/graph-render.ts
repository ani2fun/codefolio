// The one generic D3 renderer for the trace-driven Visualise feature (ADR-0018).
//
// Consumes a `VizGraph` (a sequence of graph states) and a pluggable `LayoutFn`,
// draws nodes + edges, and animates between steps. Everything here is shape-agnostic
// — *where* a node sits is the layout's job. Adding a data structure adds a layout,
// never a renderer. Knows nothing about Scala or React; `d3-transition` is loaded
// once by client/main.js.
//
// Layout is computed once from the UNION of every node + edge across all steps, so a
// node holds a stable slot for the whole animation and merely fades in / out as the
// steps that contain it come and go. D3 keyed joins (nodes by id, edges by from->to)
// diff each step against the last.

import * as d3 from "d3";
import type { VizGraph, VizGraphStep, VizNode, VizEdge } from "./types";
import type { LayoutFn, GraphLayout } from "./tree-layout";
import { Stepper } from "./stepper";

const MOVE_MS = 450;
const FADE_MS = 350;
const STEP_DELAY_MS = 1400;
const NODE_RADIUS = 22;
const SVG_NS = "http://www.w3.org/2000/svg";

// Bumped per render so each widget's arrowhead <marker> gets a unique id — two
// graphs on one page must not cross-reference each other's def.
let widgetSeq = 0;

interface EdgeDatum {
  id: string;
  from: string;
  to: string;
  label: string;
}

/** Render `data` into `container`. Idempotent — clears any prior render. */
export function renderGraph(
  container: HTMLElement,
  data: VizGraph,
  layout: LayoutFn,
  onStep?: (index: number) => void,
): void {
  container.innerHTML = "";

  const root = document.createElement("div");
  root.className = "viz-graph not-prose";

  if (data.title) {
    const titleEl = document.createElement("p");
    titleEl.className = "viz-graph__title";
    titleEl.textContent = data.title;
    root.appendChild(titleEl);
  }

  const frame = document.createElement("div");
  frame.className = "viz-graph__frame";
  root.appendChild(frame);

  const caption = document.createElement("p");
  caption.className = "viz-graph__caption";
  caption.setAttribute("aria-live", "polite");
  root.appendChild(caption);

  if (data.truncated) {
    const notice = document.createElement("p");
    notice.className = "viz-graph__notice";
    notice.textContent = "Trace truncated — showing the first part of the run.";
    root.appendChild(notice);
  }

  const controls = document.createElement("div");
  controls.className = "viz-graph__controls";
  const btnPrev = makeButton("Prev");
  const btnPlay = makeButton("Play");
  const btnNext = makeButton("Next");
  const btnReset = makeButton("Reset");
  const progress = document.createElement("span");
  progress.className = "viz-graph__progress";
  controls.append(btnPrev, btnPlay, btnNext, btnReset, progress);
  root.appendChild(controls);

  // A static key for the node colours + the pointer caret.
  root.appendChild(buildLegend(data));

  container.appendChild(root);

  const steps = data.steps;
  if (steps.length === 0) {
    caption.textContent = "No steps to display.";
    controls.style.display = "none";
    return;
  }

  // Layout once from the union of every node + edge across all steps.
  const unionNodes = new Map<string, VizNode>();
  const unionEdges = new Map<string, VizEdge>();
  for (const s of steps) {
    for (const n of s.nodes) unionNodes.set(n.id, n);
    for (const e of s.edges) unionEdges.set(`${e.from}->${e.to}`, e);
  }
  const placed: GraphLayout = layout([...unionNodes.values()], [...unionEdges.values()]);

  const svg = document.createElementNS(SVG_NS, "svg");
  svg.setAttribute("class", "viz-graph__svg");
  svg.setAttribute("viewBox", `0 0 ${placed.width} ${placed.height}`);
  svg.setAttribute("width", String(placed.width));
  svg.setAttribute("height", String(placed.height));
  svg.setAttribute("role", "img");
  svg.setAttribute("aria-label", data.title || "Code visualisation");

  // Arrowhead marker — one shared <marker> every edge points at via marker-end, so
  // each edge reads as a directed parent→child reference. `context-stroke` makes the
  // arrowhead inherit its edge's stroke (incl. a traversal tint); the CSS carries a
  // muted fallback for the rare engine without it.
  const arrowId = `viz-graph-arrow-${(widgetSeq += 1)}`;
  const defs = document.createElementNS(SVG_NS, "defs");
  const marker = document.createElementNS(SVG_NS, "marker");
  marker.setAttribute("id", arrowId);
  marker.setAttribute("class", "viz-graph__arrowhead");
  marker.setAttribute("viewBox", "0 0 10 10");
  marker.setAttribute("refX", "8.5");
  marker.setAttribute("refY", "5");
  marker.setAttribute("markerUnits", "userSpaceOnUse");
  marker.setAttribute("markerWidth", "9");
  marker.setAttribute("markerHeight", "9");
  marker.setAttribute("orient", "auto");
  const arrowPath = document.createElementNS(SVG_NS, "path");
  arrowPath.setAttribute("d", "M 1 1 L 9 5 L 1 9 Z");
  marker.appendChild(arrowPath);
  defs.appendChild(marker);
  svg.appendChild(defs);

  const edgesG = document.createElementNS(SVG_NS, "g");
  edgesG.setAttribute("class", "viz-graph__edges");
  const nodesG = document.createElementNS(SVG_NS, "g");
  nodesG.setAttribute("class", "viz-graph__nodes");
  svg.appendChild(edgesG);
  svg.appendChild(nodesG);
  frame.appendChild(svg);

  function centerOf(id: string): [number, number] {
    const p = placed.positions.get(id);
    if (p === undefined) return [0, 0];
    return [p.x, p.y];
  }

  function transformOf(id: string): string {
    const [x, y] = centerOf(id);
    return `translate(${x},${y})`;
  }

  // Edge from the source's circle edge to the target's circle edge, so the stroke
  // never tunnels through the node bodies.
  function edgePath(fromId: string, toId: string): string {
    const [px, py] = centerOf(fromId);
    const [cx, cy] = centerOf(toId);
    const dx = cx - px;
    const dy = cy - py;
    const len = Math.max(1e-6, Math.sqrt(dx * dx + dy * dy));
    const ux = dx / len;
    const uy = dy / len;
    return `M ${px + ux * NODE_RADIUS} ${py + uy * NODE_RADIUS} L ${cx - ux * NODE_RADIUS} ${cy - uy * NODE_RADIUS}`;
  }

  // Midpoint of an edge — where its pointer-name label (left / right / next / …) sits.
  function edgeMid(fromId: string, toId: string): [number, number] {
    const [px, py] = centerOf(fromId);
    const [cx, cy] = centerOf(toId);
    return [(px + cx) / 2, (py + cy) / 2];
  }

  function renderStep(index: number, animate: boolean): void {
    const step: VizGraphStep = steps[index];
    const present = step.nodes;
    const presentIds = new Set(present.map((n) => n.id));
    const edges: EdgeDatum[] = [];
    for (const e of step.edges) {
      if (presentIds.has(e.from) && presentIds.has(e.to)) {
        edges.push({ id: `${e.from}->${e.to}`, from: e.from, to: e.to, label: e.label });
      }
    }
    // Frame-local variables pointing into the structure, grouped by the node they target,
    // so a node held by several locals shows every name (e.g. a long-lived `root` + a `node`).
    // Each carries its role colour from MarkerColors.
    const cursorsByNode = new Map<string, { name: string; color: string }[]>();
    for (const c of step.cursor) {
      const held = cursorsByNode.get(c.target);
      if (held !== undefined) held.push({ name: c.name, color: c.color });
      else cursorsByNode.set(c.target, [{ name: c.name, color: c.color }]);
    }
    const highlights = new Set(step.highlight);
    const changedSet = new Set(step.changed);
    const removedSet = new Set(step.removed);

    // Cursor traversal — a cursor that moved A→B since the previous step tints the
    // edge between A and B in that cursor's colour, for this step only. The tint is
    // an inline stroke; the next step clears it and CSS eases the edge back to muted.
    const traversedEdges = new Map<string, string>();
    if (index > 0) {
      const prevByName = new Map<string, string>();
      for (const c of steps[index - 1].cursor) prevByName.set(c.name, c.target);
      for (const c of step.cursor) {
        const was = prevByName.get(c.name);
        if (was !== undefined && was !== c.target) {
          const fwd = `${was}->${c.target}`;
          const bwd = `${c.target}->${was}`;
          if (edges.some((e) => e.id === fwd)) traversedEdges.set(fwd, c.color);
          else if (edges.some((e) => e.id === bwd)) traversedEdges.set(bwd, c.color);
        }
      }
    }

    // --- edges (drawn under nodes) ---
    const edgeSel = d3
      .select(edgesG)
      .selectAll("path.viz-graph__edge")
      .data(edges, (e: any) => e.id);
    const edgeEnter = edgeSel
      .enter()
      .append("path")
      .attr("class", "viz-graph__edge")
      .attr("fill", "none")
      .attr("marker-end", `url(#${arrowId})`)
      .attr("d", (e: any) => edgePath(e.from, e.to))
      .attr("opacity", 0);
    edgeSel.exit().remove();
    const edgeAll = edgeEnter.merge(edgeSel);
    // Traversal tint — set/cleared every step so it never lingers past its step.
    edgeAll
      .classed("viz-graph__edge--traversed", (e: any) => traversedEdges.has(e.id))
      .style("stroke", (e: any) => traversedEdges.get(e.id) ?? null);
    if (animate) {
      // Named transitions — the enter fade-in and the path move must NOT share
      // D3's default (unnamed) transition namespace, or the move interrupts the
      // fade and the edge never reaches opacity 1.
      edgeEnter.transition("edge-fade").duration(FADE_MS).attr("opacity", 1);
      edgeAll
        .transition("edge-move")
        .duration(MOVE_MS)
        .ease(d3.easeCubicInOut)
        .attr("d", (e: any) => edgePath(e.from, e.to));
    } else {
      edgeAll.attr("opacity", 1).attr("d", (e: any) => edgePath(e.from, e.to));
    }

    // --- edge labels (the pointer name each edge came from: left / right / next / …) ---
    const edgeLabelSel = d3
      .select(edgesG)
      .selectAll("text.viz-graph__edge-label")
      .data(edges, (e: any) => e.id);
    const edgeLabelEnter = edgeLabelSel
      .enter()
      .append("text")
      .attr("class", "viz-graph__edge-label")
      .attr("text-anchor", "middle")
      .attr("dy", "0.32em")
      .attr("x", (e: any) => edgeMid(e.from, e.to)[0])
      .attr("y", (e: any) => edgeMid(e.from, e.to)[1])
      .attr("opacity", 0)
      .text((e: any) => e.label);
    edgeLabelSel.exit().remove();
    const edgeLabelAll = edgeLabelEnter.merge(edgeLabelSel);
    edgeLabelAll.text((e: any) => e.label);
    if (animate) {
      edgeLabelEnter.transition("edge-label-fade").duration(FADE_MS).attr("opacity", 1);
      edgeLabelAll
        .transition("edge-label-move")
        .duration(MOVE_MS)
        .ease(d3.easeCubicInOut)
        .attr("x", (e: any) => edgeMid(e.from, e.to)[0])
        .attr("y", (e: any) => edgeMid(e.from, e.to)[1]);
    } else {
      edgeLabelAll
        .attr("opacity", 1)
        .attr("x", (e: any) => edgeMid(e.from, e.to)[0])
        .attr("y", (e: any) => edgeMid(e.from, e.to)[1]);
    }

    // --- nodes ---
    const nodeSel = d3
      .select(nodesG)
      .selectAll("g.viz-graph__node")
      .data(present, (n: any) => n.id);
    const nodeEnter = nodeSel
      .enter()
      .append("g")
      .attr("class", "viz-graph__node")
      .attr("transform", (n: any) => transformOf(n.id))
      .attr("opacity", 0);
    nodeEnter.append("circle").attr("class", "viz-graph__circle").attr("r", NODE_RADIUS);
    // Transient-state adornment ring — invisible by default; the --changed / --removed
    // modifiers light it amber / red. A separate element so the flash / fade never fights
    // the persistent --cursor / --new circle tint (the two states layer instead).
    nodeEnter
      .append("circle")
      .attr("class", "viz-graph__node-ring")
      .attr("r", NODE_RADIUS + 4);
    nodeEnter
      .append("text")
      .attr("class", "viz-graph__value")
      .attr("text-anchor", "middle")
      .attr("dy", "0.32em")
      .text((n: any) => n.label);
    // Per-node field meta (e.g. an AVL `height`), below the circle.
    nodeEnter
      .append("text")
      .attr("class", "viz-graph__meta")
      .attr("text-anchor", "middle")
      .attr("y", NODE_RADIUS + 15)
      .text((n: any) => metaText(n));
    // Cursor marker above the circle — the frame-local variable name(s) pointing at the
    // node, plus a caret. Its text is set per step in the nodeAll update below.
    nodeEnter
      .append("text")
      .attr("class", "viz-graph__cursor-mark")
      .attr("text-anchor", "middle")
      .attr("y", -(NODE_RADIUS + 10));
    nodeSel.exit().remove();
    const nodeAll = nodeEnter.merge(nodeSel);
    // Persistent layer (--cursor / --new) tints the circle; transient layer
    // (--changed / --removed) lights the adornment ring — disjoint, never overwrite.
    nodeAll
      .classed("viz-graph__node--cursor", (n: any) => cursorsByNode.has(n.id))
      .classed("viz-graph__node--new", (n: any) => highlights.has(n.id))
      .classed("viz-graph__node--changed", (n: any) => changedSet.has(n.id))
      .classed("viz-graph__node--removed", (n: any) => removedSet.has(n.id));
    nodeAll.select("text.viz-graph__value").text((n: any) => n.label);
    nodeAll.select("text.viz-graph__meta").text((n: any) => metaText(n));
    // Cursor caret — each pointer name in its own role colour (MarkerColors); the caret
    // glyph takes the first pointer's colour. Rebuilt as tspans each step.
    nodeAll.select("text.viz-graph__cursor-mark").each(function (n: any) {
      const sel = d3.select(this as SVGTextElement);
      sel.selectAll("tspan").remove();
      const cs = cursorsByNode.get(n.id);
      if (cs === undefined || cs.length === 0) return;
      cs.forEach((c, i) => {
        sel
          .append("tspan")
          .attr("fill", c.color || null)
          .text(i === 0 ? c.name : `, ${c.name}`);
      });
      sel.append("tspan").attr("fill", cs[0].color || null).text("  ▾");
    });
    // A node removed this step is re-emitted once at reduced opacity, so it visibly
    // fades out before the next step drops it entirely.
    const opacityOf = (n: any): number => (removedSet.has(n.id) ? 0.25 : 1);
    if (animate) {
      nodeEnter.transition("node-fade").duration(FADE_MS).attr("opacity", 1);
      nodeAll
        .transition("node-move")
        .duration(MOVE_MS)
        .ease(d3.easeCubicInOut)
        .attr("transform", (n: any) => transformOf(n.id))
        .attr("opacity", opacityOf);
    } else {
      nodeAll.attr("opacity", opacityOf).attr("transform", (n: any) => transformOf(n.id));
    }

    caption.textContent = step.annotation;
  }

  function updateControls(index: number, playing: boolean): void {
    btnPrev.disabled = index <= 0;
    btnNext.disabled = index >= steps.length - 1;
    btnReset.disabled = index === 0 && !playing;
    btnPlay.textContent = playing ? "Pause" : "Play";
    progress.textContent = `Step ${index + 1} / ${steps.length}`;
  }

  const stepper = new Stepper(steps.length, STEP_DELAY_MS, (index, playing) => {
    renderStep(index, true);
    updateControls(index, playing);
    if (onStep !== undefined) onStep(index);
  });
  btnPrev.addEventListener("click", () => stepper.previous());
  btnNext.addEventListener("click", () => stepper.next());
  btnReset.addEventListener("click", () => stepper.reset());
  btnPlay.addEventListener("click", () => stepper.togglePlay());

  if (steps.length <= 1) controls.style.display = "none";

  renderStep(0, false);
  updateControls(0, false);
  if (onStep !== undefined) onStep(0);
}

function makeButton(label: string): HTMLButtonElement {
  const b = document.createElement("button");
  b.type = "button";
  b.className = "viz-graph__button";
  b.textContent = label;
  return b;
}

// A key for the diagram's visual vocabulary — the node colours and the pointer caret.
// Universal to the VizGraph model, so it lives in the renderer, not a layout. The
// changed / removed swatches appear only when the trace actually exercises them.
function buildLegend(data: VizGraph): HTMLDivElement {
  const legend = document.createElement("div");
  legend.className = "viz-graph__legend";
  legend.append(
    legendItem("viz-graph__legend-swatch--cursor", "", "a variable points here"),
    legendItem("viz-graph__legend-swatch--new", "", "new this step"),
  );
  if (data.steps.some((s) => s.changed.length > 0))
    legend.append(legendItem("viz-graph__legend-swatch--changed", "", "value changed"));
  if (data.steps.some((s) => s.removed.length > 0))
    legend.append(legendItem("viz-graph__legend-swatch--removed", "", "removed"));
  legend.append(
    legendItem("viz-graph__legend-swatch--pointer", "▾", "pointer — labelled with the variable"),
  );
  return legend;
}

function legendItem(modifier: string, glyph: string, text: string): HTMLSpanElement {
  const item = document.createElement("span");
  item.className = "viz-graph__legend-item";
  const swatch = document.createElement("span");
  swatch.className = `viz-graph__legend-swatch ${modifier}`;
  swatch.textContent = glyph;
  const label = document.createElement("span");
  label.textContent = text;
  item.append(swatch, label);
  return item;
}

// A node's field meta rendered as one line of `name=value` pairs (e.g. `height=2`).
function metaText(n: VizNode): string {
  return n.meta.map((f) => `${f.name}=${f.value}`).join("  ");
}
