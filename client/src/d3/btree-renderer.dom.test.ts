// @vitest-environment jsdom
//
// Rendering test for the B-tree renderer: feeds a VizGraph shaped exactly like
// HeapToGraph emits for a B-tree (BNode instances + keys/children Arr cells, the
// two-hop parent→child link through a children cell) and asserts the renderer
// reconstructs the right boxes, keys, and child pointers. This is the regression
// guard for the data-shape contract the renderer depends on.

import { describe, it, expect, beforeEach } from "vitest";
import { btreeRenderer } from "./btree-renderer";
import type { VizGraph, VizGraphStep, VizNode, VizEdge, VizCursor } from "./types";
import type { LayoutFn } from "./tree-layout";

const noopLayout: LayoutFn = () => ({ positions: new Map(), width: 0, height: 0 });

function node(id: string, label: string, kind: string, slot: number | null, leaf?: boolean): VizNode {
  const meta = leaf === undefined ? [] : [{ name: "leaf", value: String(leaf) }];
  return { id, label, kind, meta, slot, cardId: "", layoutKind: "" } as VizNode;
}
const edge = (from: string, to: string, label: string): VizEdge => ({ from, to, label });

function step(over: Partial<VizGraphStep>): VizGraphStep {
  return {
    nodes: [],
    edges: [],
    cursor: [],
    highlight: [],
    changed: [],
    removed: [],
    annotation: { eyebrow: "line", title: "", body: "" },
    line: 0,
    frames: [],
    cardCursor: [],
    unchanged: false,
    structureType: "btree",
    ...over,
  } as VizGraphStep;
}

// A B-tree: root [30] over children [10,20] and [40,50,60]. Mirrors HeapToGraph:
// a BNode is an Instance(kind="BNode"); keys/children are Arr cells (kind="cell");
// parent --"children"--> childCell --""--> child BNode.
function btreeStep(extra: Partial<VizGraphStep> = {}): VizGraphStep {
  const nodes: VizNode[] = [
    // The container BTree instance + its `root` edge — present when viz-root=tree.
    // The renderer must ignore this non-BNode node and still find b_root as the root.
    node("bt", "BTree", "BTree", null),
    node("b_root", "BNode", "BNode", null, false),
    node("a_rk#0", "30", "cell", 0),
    node("a_rc#0", "·", "cell", 0),
    node("a_rc#1", "·", "cell", 1),
    node("b_c1", "BNode", "BNode", null, true),
    node("a_c1k#0", "10", "cell", 0),
    node("a_c1k#1", "20", "cell", 1),
    node("b_c2", "BNode", "BNode", null, true),
    node("a_c2k#0", "40", "cell", 0),
    node("a_c2k#1", "50", "cell", 1),
    node("a_c2k#2", "60", "cell", 2),
  ];
  const edges: VizEdge[] = [
    edge("bt", "b_root", "root"), // BTree.root → the root BNode (a non-child link)
    edge("b_root", "a_rk#0", "keys"),
    edge("b_root", "a_rc#0", "children"),
    edge("b_root", "a_rc#1", "children"),
    edge("a_rc#0", "b_c1", ""),
    edge("a_rc#1", "b_c2", ""),
    edge("b_c1", "a_c1k#0", "keys"),
    edge("b_c1", "a_c1k#1", "keys"),
    edge("b_c2", "a_c2k#0", "keys"),
    edge("b_c2", "a_c2k#1", "keys"),
    edge("b_c2", "a_c2k#2", "keys"),
  ];
  return step({ nodes, edges, ...extra });
}

function graph(...steps: VizGraphStep[]): VizGraph {
  return { steps, layoutHint: "graph", title: "B-tree", truncated: false } as VizGraph;
}

describe("btree-renderer — rendering (jsdom)", () => {
  let container: HTMLElement;
  beforeEach(() => {
    container = document.createElement("div");
    document.body.appendChild(container);
  });

  it("reconstructs 3 BNode boxes with the right keys and 2 child pointers", () => {
    btreeRenderer(container, graph(btreeStep()), noopLayout);

    const boxes = [...container.querySelectorAll(".btree-renderer__node")];
    expect(boxes).toHaveLength(3);

    // Every key cell label, across all boxes.
    const keys = [...container.querySelectorAll(".btree-renderer__key")].map((k) => k.textContent);
    expect(keys.sort()).toEqual(["10", "20", "30", "40", "50", "60"]);

    // Each box's key set — the root is [30]; the leaves are [10,20] and [40,50,60].
    const keysPerBox = boxes.map((b) =>
      [...b.querySelectorAll(".btree-renderer__key")].map((k) => k.textContent).join(","),
    );
    expect(keysPerBox).toContain("30");
    expect(keysPerBox).toContain("10,20");
    expect(keysPerBox).toContain("40,50,60");

    // Two parent→child pointers (root→each leaf); no empty-state.
    expect(container.querySelectorAll(".btree-renderer__edge")).toHaveLength(2);
    expect(container.querySelector(".btree-renderer__empty")).toBeNull();
  });

  it("flashes a freshly-inserted key and tints the node a local points at", () => {
    // A BNode is both a node and a card root, so a local surfaces in BOTH sets.
    const cursor: VizCursor = { name: "node", target: "b_c2", color: "#e11" };
    btreeRenderer(
      container,
      graph(btreeStep({ highlight: ["a_c2k#2"], cursor: [cursor], cardCursor: [cursor] })),
      noopLayout,
    );

    const newKey = container.querySelector(".btree-renderer__key--new");
    expect(newKey?.textContent).toBe("60");

    // The node a local points at is TINTED in the cursor colour. The NAME is NOT drawn
    // on the node — the shared ArrowLayer already labels the pointer, and an on-node
    // badge collided with the arrow's tip (regression guard: no badge element).
    const cursored = container.querySelector<HTMLElement>(".btree-renderer__node--cursor");
    expect(cursored).not.toBeNull();
    expect(cursored?.style.getPropertyValue("--node-color")).toBe("#e11");
    expect(container.querySelector(".btree-renderer__badge")).toBeNull();
  });

  it("renders the empty state when a step has no BNodes", () => {
    btreeRenderer(container, graph(step({})), noopLayout);
    expect(container.querySelector(".btree-renderer__empty")).not.toBeNull();
    expect(container.querySelectorAll(".btree-renderer__node")).toHaveLength(0);
  });
});
