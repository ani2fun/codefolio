// Linked-list layout — a horizontal chain for the trace-driven Visualise
// renderer (ADR-0018). The conventional textbook idiom: nodes left-to-right,
// `next` arrows between them. Covers the singly- and doubly-linked list and the
// list-backed stack / queue.
//
// The generic `graph` layout already lays a chain out — but *vertically*, down
// its depth axis. This layout exists for the one thing that needs a bespoke
// answer: a left-to-right chain. `next` edges define the order, and the nodes
// are placed in a row in that order. `prev` edges (a doubly-linked list) are
// *not* used for placement — the `next` chain already fixes the order — so a
// doubly-linked list lays out identically to a singly-linked one. The rare
// payload holding two lists (a merge's two inputs) stacks them as two rows.
//
// A cyclic `next`-graph — Floyd's cycle detection loops the tail back — is
// handed to `graphLayout` instead. The renderer draws every edge as a straight
// centre-to-centre line and cannot curve a back-edge, so a flat row would run
// the loop-closing edge straight *through* the intervening nodes and hide the
// very loop the lesson is about; `graphLayout`'s force path settles a cycle
// into a visible loop. The same fallback catches any `next`-graph that is not a
// disjoint union of simple paths (a merge / fork / re-link).
//
// No d3, no DOM: positions are pure arithmetic, unit-tested in plain Node (see
// linked-list-layout.test.ts). A new data structure adds a LayoutFn like this
// one — never a new renderer.

import type { VizNode, VizEdge } from "./types";
import type { LayoutFn, GraphLayout, NodePos } from "./tree-layout";
import { graphLayout } from "./graph-layout";

// Geometry. NODE_R / RING_R mirror NODE_RADIUS and the adornment-ring radius in
// graph-render.ts — the layout sizes the canvas around the renderer's nodes.
const NODE_R = 22;
const RING_R = NODE_R + 4;
const NODE_DX = 96; // centre-to-centre along the chain — room for the `next` edge + its label
const ROW_DY = 112; // between stacked chains (the rare multi-list payload)
const PAD_X = 24; // gap from the outermost ring to the left / right border
const PAD_TOP = 48; // headroom above the top row for the cursor caret
const PAD_BOTTOM = 42; // room below the bottom row for a meta sub-label

/**
 * Place the union of every node + edge in a Visualise animation as a horizontal
 * chain. Deterministic — positions are a pure function of the `next`-edge order
 * and of input order — so re-opening the modal always redraws identically.
 */
export const linkedListLayout: LayoutFn = (nodes, edges) => {
  if (nodes.length === 0) return emptyLayout();

  const ids = new Set(nodes.map((n) => n.id));
  // Drop self-loops and dangling edges: neither informs placement, and a
  // self-loop would otherwise read as a one-node cycle (graph-layout drops them
  // for the same reason).
  const real = edges.filter((e) => e.from !== e.to && ids.has(e.from) && ids.has(e.to));

  const nextEdges = real.filter(isNext);
  // `next` / `nxt` is the near-universal chain-pointer field name. If a book
  // names it something else no edge matches — fall back to "every non-`prev`
  // edge is a chain edge" so an oddly-named singly-linked list still lays out.
  const forward = nextEdges.length > 0 ? nextEdges : real.filter((e) => !isPrev(e));

  const chains = asLinearForest(nodes, forward);
  // Not a disjoint union of simple paths — a cycle, a merge, a fork. The
  // generic graph layout draws that correctly; see the file header for why a
  // flat row is wrong for a cycle.
  if (chains === null) return graphLayout(nodes, edges);

  const raw = new Map<string, NodePos>();
  chains.forEach((chain, row) => {
    chain.forEach((id, col) => {
      raw.set(id, { x: col * NODE_DX, y: row * ROW_DY });
    });
  });
  return frame(nodes, raw);
};

/** A chain-forward edge — a `next` (or `nxt`) pointer. */
function isNext(e: VizEdge): boolean {
  const l = e.label.toLowerCase();
  return l === "next" || l === "nxt";
}

/** A backward edge — a doubly-linked list's `prev` pointer; never used for placement. */
function isPrev(e: VizEdge): boolean {
  const l = e.label.toLowerCase();
  return l === "prev" || l === "previous";
}

/**
 * Read `forward` as a linked list: a disjoint union of simple paths (a "linear
 * forest"). Returns one id list per chain — head-first, the chains ordered by
 * each head's first appearance in `nodes` — or `null` when `forward` is not a
 * linear forest: some node has two `next` edges, two nodes point `next` at one
 * node, or a cycle leaves nodes with no head. The caller delegates the `null`
 * case to the generic graph layout.
 */
function asLinearForest(nodes: VizNode[], forward: VizEdge[]): string[][] | null {
  const outDeg = new Map<string, number>();
  const inDeg = new Map<string, number>();
  for (const n of nodes) {
    outDeg.set(n.id, 0);
    inDeg.set(n.id, 0);
  }
  const nextOf = new Map<string, string>();
  for (const e of forward) {
    outDeg.set(e.from, (outDeg.get(e.from) ?? 0) + 1);
    inDeg.set(e.to, (inDeg.get(e.to) ?? 0) + 1);
    nextOf.set(e.from, e.to);
  }
  // A chain node has at most one `next` out and at most one `next` in.
  for (const d of outDeg.values()) if (d > 1) return null;
  for (const d of inDeg.values()) if (d > 1) return null;

  // out-degree <= 1 AND in-degree <= 1 means the graph is a disjoint union of
  // simple paths and simple cycles. Walk every path from its in-degree-0 head;
  // if the walk does not cover every node, a headless cycle remains.
  const heads = nodes.filter((n) => (inDeg.get(n.id) ?? 0) === 0).map((n) => n.id);
  const chains: string[][] = [];
  const seen = new Set<string>();
  for (const head of heads) {
    const chain: string[] = [];
    let cur: string | undefined = head;
    while (cur !== undefined && !seen.has(cur)) {
      seen.add(cur);
      chain.push(cur);
      cur = nextOf.get(cur);
    }
    chains.push(chain);
  }
  if (seen.size !== nodes.length) return null; // a cycle with no head
  return chains;
}

// Translate raw positions into a padded canvas and report its size. Mirrors the
// inset discipline of graph-layout.ts / array-layout.ts: the outermost node
// rings clear the border by PAD_X, with caret headroom above and meta room below.
function frame(nodes: VizNode[], raw: Map<string, NodePos>): GraphLayout {
  let minX = Infinity;
  let maxX = -Infinity;
  let minY = Infinity;
  let maxY = -Infinity;
  for (const n of nodes) {
    const p = raw.get(n.id);
    if (p === undefined) continue;
    minX = Math.min(minX, p.x);
    maxX = Math.max(maxX, p.x);
    minY = Math.min(minY, p.y);
    maxY = Math.max(maxY, p.y);
  }
  if (!Number.isFinite(minX)) return emptyLayout();

  const dx = PAD_X + RING_R - minX;
  const dy = PAD_TOP - minY;
  const positions = new Map<string, NodePos>();
  for (const n of nodes) {
    const p = raw.get(n.id) ?? { x: minX, y: minY };
    positions.set(n.id, { x: p.x + dx, y: p.y + dy });
  }
  return {
    positions,
    width: maxX - minX + 2 * (PAD_X + RING_R),
    height: maxY - minY + PAD_TOP + PAD_BOTTOM,
  };
}

function emptyLayout(): GraphLayout {
  return {
    positions: new Map(),
    width: 2 * (PAD_X + RING_R),
    height: PAD_TOP + PAD_BOTTOM,
  };
}
