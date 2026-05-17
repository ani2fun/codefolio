#!/usr/bin/env python3
"""Generate /Users/aniket/Development/homelab/codefolio/docs/migration/conversion-manifest.json
from source diagram markers."""

import json
import os
import re
from collections import OrderedDict

SOURCE_ROOT = "/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA"
DEST_ROOT = "content/cortex/data-structures-and-algorithms"

# Source-phase → destination-section + primary widget
PHASE_MAP = OrderedDict([
    ("data-structure/1.arrays", {
        "phase_name": "Phase 0 — Arrays",
        "dest_dir": "02-linear-structures/01-arrays",
        "primary_widget": "array-traversal",
        "status": "done",
    }),
    ("data-structure/2.singly-linked-list", {
        "phase_name": "Phase 1 — Singly Linked List",
        "dest_dir": "02-linear-structures/03-singly-linked-list",
        "primary_widget": "linked-list",
        "status": "done",
    }),
    ("data-structure/3.doubly-linked-list", {
        "phase_name": "Phase 2 — Doubly Linked List",
        "dest_dir": "02-linear-structures/04-doubly-linked-list",
        "primary_widget": "linked-list",
        "widget_mode": "direction:double",
        "status": "not-started",
    }),
    ("data-structure/4.hash-table", {
        "phase_name": "Phase 3 — Hash Table",
        "dest_dir": "02-linear-structures/07-hash-table",
        "primary_widget": "hash-table",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for hash-table",
    }),
    ("data-structure/5.stack", {
        "phase_name": "Phase 4 — Stack",
        "dest_dir": "02-linear-structures/05-stack",
        "primary_widget": "stack-queue",
        "widget_mode": "mode:stack",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for stack-queue",
    }),
    ("data-structure/6.queue", {
        "phase_name": "Phase 5 — Queue",
        "dest_dir": "02-linear-structures/06-queue",
        "primary_widget": "stack-queue",
        "widget_mode": "mode:queue",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for stack-queue",
    }),
    ("data-structure/7.binary-tree", {
        "phase_name": "Phase 6 — Binary Tree",
        "dest_dir": "03-trees/01-binary-tree",
        "primary_widget": "binary-tree",
        "widget_mode": "mode:binary",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for binary-tree",
    }),
    ("data-structure/8.binary-search-tree", {
        "phase_name": "Phase 7 — BST",
        "dest_dir": "03-trees/02-binary-search-tree",
        "primary_widget": "binary-tree",
        "widget_mode": "mode:bst",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for binary-tree",
    }),
    ("data-structure/9.heap", {
        "phase_name": "Phase 8 — Heap",
        "dest_dir": "03-trees/03-heap",
        "primary_widget": "heap-tree",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for heap-tree",
    }),
    ("data-structure/10.graph", {
        "phase_name": "Phase 9 — Graph",
        "dest_dir": "04-graphs",
        "dest_layout": "flat",
        "primary_widget": "graph-explorer",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for graph-explorer",
    }),
    ("algorithms/1.recursion", {
        "phase_name": "Phase 10 — Recursion",
        "dest_dir": "05-algorithms-by-strategy/01-recursion",
        "primary_widget": "call-stack",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for call-stack",
    }),
    ("algorithms/2.backtracking", {
        "phase_name": "Phase 11 — Backtracking",
        "dest_dir": "05-algorithms-by-strategy/04-backtracking",
        "primary_widget": "decision-tree",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for decision-tree",
    }),
    ("algorithms/3.sorting", {
        "phase_name": "Phase 12 — Sorting",
        "dest_dir": "06-sorting-and-searching/01-sorting",
        "primary_widget": "array-traversal",
        "status": "not-started",
    }),
    ("algorithms/4.searching", {
        "phase_name": "Phase 13 — Searching",
        "dest_dir": "06-sorting-and-searching/02-searching",
        "primary_widget": "array-traversal",
        "widget_mode": "layout:1d or layout:2d",
        "status": "partial",
    }),
    ("algorithms/5.dynamic-programming", {
        "phase_name": "Phase 14 — Dynamic Programming",
        "dest_dir": "05-algorithms-by-strategy/05-dynamic-programming",
        "primary_widget": "dp-table",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for dp-table",
    }),
    ("algorithms/6.bit-manipulation", {
        "phase_name": "Phase 15 — Bit Manipulation",
        "dest_dir": "08-bit-tricks",
        "primary_widget": "bit-grid",
        "status": "not-started",
        "blocked_on": "Arc 1 widget build for bit-grid",
        "note": "Source has 0 interactive diagrams; payloads come from destination prose",
    }),
])

# Source chapter dir → destination .md file name (within the dest_dir)
# Heuristic: the destination file has the same numeric prefix + name as the source dir,
# but consolidated into one file. Below are explicit overrides where mapping isn't trivial.
DEST_FILE_OVERRIDES = {
    "data-structure/1.arrays/01-introduction-to-arrays": "01-introduction.md",
    "data-structure/1.arrays/02-multidimensional-arrays": "02-multidimensional.md",
    "data-structure/1.arrays/03-pattern-two-pointers": "03-pattern-two-pointers.md",
    "data-structure/1.arrays/04-pattern-two-pointers-reduction": "04-pattern-two-pointers-reduction.md",
    "data-structure/1.arrays/05-pattern-two-pointers-subproblem": "05-pattern-two-pointers-subproblem.md",
    "data-structure/1.arrays/06-pattern-simultaneous-traversal": "06-pattern-simultaneous-traversal.md",
    "data-structure/1.arrays/07-pattern-fixed-sized-sliding-window": "07-pattern-fixed-sliding-window.md",
    "data-structure/1.arrays/08-pattern-variable-sized-sliding-window": "08-pattern-variable-sliding-window.md",
    "data-structure/1.arrays/09-pattern-interval-merging": "09-pattern-interval-merging.md",
    "data-structure/1.arrays/10-pattern-maximum-overlap": "10-pattern-maximum-overlap.md",
    "data-structure/1.arrays/11-design": "11-design-a-dynamic-array.md",
    # Spelling fixups (source has typos; destination has correct spellings)
    "data-structure/5.stack/09-pattern-next-closest-occurance": "09-pattern-next-closest-occurrence.md",
    "data-structure/7.binary-tree/16-pattern-lowest-common-anscestor": "16-pattern-lowest-common-ancestor.md",
    "data-structure/8.binary-search-tree/08-lowest-common-ansestor-in-binary-search-trees": "08-lowest-common-ancestor-in-binary-search-trees.md",
    "data-structure/8.binary-search-tree/09-iterators-in-a-binary-search-trees": "09-iterators-in-binary-search-trees.md",
    "data-structure/10.graph/10-max-flow-min-cut-theoram": "10-max-flow-min-cut-theorem.md",
    # DP knapsacks consolidate into 2 destination files
    "algorithms/5.dynamic-programming/09-0-1-knapsack": "10-knapsack.md",
    "algorithms/5.dynamic-programming/10-unbounded-knapsack": "10-knapsack.md",
    "algorithms/5.dynamic-programming/11-bounded-knapsack": "10-knapsack.md",
    "algorithms/5.dynamic-programming/12-counting-knapsack": "11-knapsack-applications.md",
    # DP chapters with 0 diagrams — point at the closest destination by name (no widgets anyway)
    "algorithms/5.dynamic-programming/13-matrix-chain-multiplication": "14-matrix-chain-multiplication.md",
    "algorithms/5.dynamic-programming/14-pattern-edit-distance": "15-pattern-edit-distance.md",
    "algorithms/5.dynamic-programming/15-pattern-subset-sum": "16-pattern-subset-sum.md",
    "algorithms/5.dynamic-programming/16-pattern-2d-grid": "17-pattern-2d-grid.md",
    "algorithms/5.dynamic-programming/17-pattern-prefix-sum": "18-pattern-prefix-sum.md",
    "algorithms/5.dynamic-programming/18-practise": "19-practise.md",
}

DIAGRAM_REGEX = re.compile(r"^// Interactive Diagram \((\d+) frames\): (.+)$")


def find_dest_file(source_chapter_dir, dest_dir, dest_layout=None):
    """Map a source chapter dir to its consolidated destination .md file."""
    override = DEST_FILE_OVERRIDES.get(source_chapter_dir)
    if override:
        return f"{dest_dir}/{override}"
    # Default heuristic: strip prefix, replace dashes, prepend dest_dir
    chapter_name = os.path.basename(source_chapter_dir)
    # Destination may have flat layout (one .md per source chapter dir)
    candidate = f"{dest_dir}/{chapter_name}.md"
    # Check existence
    full_path = os.path.join("/Users/aniket/Development/homelab/codefolio", candidate)
    if os.path.exists(full_path):
        return candidate
    # Try without numeric prefix matching
    # E.g., source has "01-introduction-to-arrays" → dest might be "01-introduction.md"
    # Caller should override if needed; we return best-guess
    return candidate


def step_compression(frame_count):
    """Compress N source frames to a reasonable widget step count (5-10)."""
    if frame_count <= 5:
        return frame_count
    elif frame_count <= 10:
        return frame_count  # No compression needed
    elif frame_count <= 20:
        return 8  # Compress to 8 steps
    elif frame_count <= 40:
        return 10  # Compress to 10 steps
    else:
        return 12  # Cap at 12 for very long sequences


def derive_anchor_hint(source_md_filename):
    """Best-guess H1/H2 anchor in destination from source lesson filename."""
    name = os.path.splitext(source_md_filename)[0]
    # Strip leading number prefix
    name = re.sub(r"^\d+-", "", name)
    # Filter out 'understanding-' prefix (most source has this)
    name = name.replace("understanding-", "")
    # Convert dashes to spaces, capitalize
    return f"# {' '.join(w.capitalize() for w in name.split('-'))}"


def collect_phase(source_phase_dir, phase_info):
    """Walk a source phase directory and collect chapter + diagram info."""
    abs_phase = os.path.join(SOURCE_ROOT, source_phase_dir)
    if not os.path.isdir(abs_phase):
        return {"chapters": {}, "diagram_count": 0}

    chapters = OrderedDict()
    total_diagrams = 0

    for entry in sorted(os.listdir(abs_phase)):
        chapter_dir = os.path.join(abs_phase, entry)
        if not os.path.isdir(chapter_dir):
            continue

        source_chapter_path = f"{source_phase_dir}/{entry}"

        # Overrides take precedence regardless of layout
        if source_chapter_path in DEST_FILE_OVERRIDES:
            dest_file = f"{phase_info['dest_dir']}/{DEST_FILE_OVERRIDES[source_chapter_path]}"
        elif phase_info.get("dest_layout") == "flat":
            # Flat-layout phases (graph, bit-manipulation): one destination .md per source chapter dir
            dest_file = f"{phase_info['dest_dir']}/{entry}.md"
        else:
            dest_file = find_dest_file(
                source_chapter_path,
                phase_info["dest_dir"],
                phase_info.get("dest_layout"),
            )

        source_lessons = []
        interactive_diagrams = []

        for lesson_file in sorted(os.listdir(chapter_dir)):
            if not lesson_file.endswith(".md"):
                continue
            lesson_path = os.path.join(chapter_dir, lesson_file)
            rel_lesson_path = f"{source_chapter_path}/{lesson_file}"
            source_lessons.append(rel_lesson_path)

            try:
                with open(lesson_path, "r", encoding="utf-8") as f:
                    for line_no, line in enumerate(f, start=1):
                        m = DIAGRAM_REGEX.match(line.rstrip("\n"))
                        if m:
                            frame_count = int(m.group(1))
                            description = m.group(2)
                            interactive_diagrams.append({
                                "source_path": rel_lesson_path,
                                "line": line_no,
                                "frame_count": frame_count,
                                "step_target": step_compression(frame_count),
                                "description": description,
                                "destination_anchor_hint": derive_anchor_hint(lesson_file),
                                "widget": phase_info["primary_widget"],
                                "widget_mode": phase_info.get("widget_mode", ""),
                            })
            except IOError:
                pass

        total_diagrams += len(interactive_diagrams)
        chapters[entry] = {
            "destination_file": dest_file,
            "source_lessons": source_lessons,
            "lesson_count": len(source_lessons),
            "interactive_diagram_count": len(interactive_diagrams),
            "interactive_diagrams": interactive_diagrams,
        }

    return {"chapters": chapters, "diagram_count": total_diagrams}


def main():
    out = OrderedDict()
    out["_meta"] = {
        "purpose": "Per-source-diagram → destination chapter + widget mapping.",
        "doc": "docs/migration/diagram-gap-audit.md is the human-readable index.",
        "methodology": "docs/migration/methodology.md governs interpretation.",
        "widget_specs": "docs/migration/widget-specs/<widget>.md per widget.",
        "generated_by": "docs/migration/scripts/gen-manifest.py (Arc 0)",
        "regenerate": "Re-run /tmp/gen-manifest.py and commit the diff.",
    }

    phases = OrderedDict()
    grand_total = 0
    for source_phase_dir, phase_info in PHASE_MAP.items():
        result = collect_phase(source_phase_dir, phase_info)
        phases[source_phase_dir] = {
            **phase_info,
            "source_chapter_count": len(result["chapters"]),
            "source_diagram_count": result["diagram_count"],
            "chapters": result["chapters"],
        }
        grand_total += result["diagram_count"]

    out["totals"] = {
        "source_diagram_count": grand_total,
        "source_phase_count": len(phases),
        "destination_chapter_count": sum(
            len(p["chapters"]) for p in phases.values()
        ),
    }

    out["phases"] = phases

    # Orphan destination chapters (no source counterpart) — declare them too
    out["orphan_chapters"] = OrderedDict([
        ("01-foundations/01-asymptotic-analysis.md", {"widget": "growth-rate-chart"}),
        ("01-foundations/02-recurrence-relations-and-master-theorem.md", {"widget": "recurrence-tree"}),
        ("01-foundations/03-amortized-analysis.md", {"widget": "amortized-payment"}),
        ("01-foundations/04-proof-techniques.md", {"widget": "static-only"}),
        ("01-foundations/05-memory-model-and-cache.md", {"widget": "cache-hit-miss"}),
        ("03-trees/04-trie/01-introduction-to-tries.md", {"widget": "trie"}),
        ("03-trees/05-self-balancing-bst-overview/01-self-balancing-bst-overview.md", {"widget": "binary-tree", "mode": "avl + rbt comparison"}),
        ("03-trees/06-avl-tree/01-introduction-to-avl-trees.md", {"widget": "binary-tree", "mode": "avl"}),
        ("03-trees/07-red-black-tree/01-introduction-to-red-black-trees.md", {"widget": "binary-tree", "mode": "rbt"}),
        ("03-trees/08-b-tree/01-introduction-to-b-trees.md", {"widget": "BTreeWalker (reuse existing)"}),
        ("03-trees/09-segment-tree/01-introduction-to-segment-trees.md", {"widget": "segment-tree"}),
        ("03-trees/10-fenwick-tree/01-introduction-to-fenwick-trees.md", {"widget": "fenwick-tree"}),
        ("03-trees/11-disjoint-set-union/01-introduction-to-disjoint-set-union.md", {"widget": "dsu-forest"}),
        ("04-graphs/10-max-flow-min-cut-theorem.md", {"widget": "graph-explorer"}),
        ("04-graphs/11-maximum-bipartite-matching.md", {"widget": "graph-explorer"}),
        ("04-graphs/17-minimum-spanning-trees.md", {"widget": "graph-explorer"}),
        ("04-graphs/18-strongly-connected-components.md", {"widget": "graph-explorer"}),
        ("04-graphs/19-bridges-and-articulation-points.md", {"widget": "graph-explorer"}),
        ("04-graphs/20-2-sat.md", {"widget": "graph-explorer"}),
        ("05-algorithms-by-strategy/02-divide-and-conquer/01-introduction-to-divide-and-conquer.md", {"widget": "decision-tree"}),
        ("05-algorithms-by-strategy/03-greedy/01-introduction-to-greedy-algorithms.md", {"widget": "array-traversal"}),
        ("05-algorithms-by-strategy/06-randomized-algorithms/01-introduction-to-randomized-algorithms.md", {"widget": "static-or-array-traversal"}),
        ("07-strings/01-string-matching-naive.md", {"widget": "string-matcher", "mode": "naive"}),
        ("07-strings/02-kmp.md", {"widget": "string-matcher", "mode": "kmp"}),
        ("07-strings/03-z-algorithm.md", {"widget": "string-matcher", "mode": "z"}),
        ("07-strings/04-rabin-karp-and-rolling-hash.md", {"widget": "string-matcher", "mode": "rabin-karp"}),
        ("07-strings/05-trie-string-applications.md", {"widget": "trie"}),
        ("07-strings/06-suffix-array.md", {"widget": "suffix-array-builder"}),
        ("07-strings/07-suffix-automaton.md", {"widget": "graph-explorer", "mode": "automaton"}),
        ("07-strings/08-aho-corasick.md", {"widget": "graph-explorer", "mode": "automaton"}),
        ("09-probabilistic-and-advanced/01-skip-list.md", {"widget": "skip-list"}),
        ("09-probabilistic-and-advanced/02-bloom-filter.md", {"widget": "bloom-filter"}),
        ("09-probabilistic-and-advanced/03-count-min-sketch.md", {"widget": "count-min-sketch"}),
        ("09-probabilistic-and-advanced/04-hyperloglog.md", {"widget": "hyperloglog"}),
        ("09-probabilistic-and-advanced/05-treap.md", {"widget": "binary-tree", "mode": "treap"}),
        ("09-probabilistic-and-advanced/06-persistent-data-structures.md", {"widget": "binary-tree", "mode": "persistent"}),
        ("10-concurrency-and-systems/01-cas-and-atomics.md", {"widget": "concurrent-timeline"}),
        ("10-concurrency-and-systems/02-lock-free-queue.md", {"widget": "stack-queue", "mode": "lock-free"}),
        ("10-concurrency-and-systems/03-concurrent-hash-map.md", {"widget": "hash-table", "mode": "concurrent"}),
        ("10-concurrency-and-systems/04-rcu-and-hazard-pointers.md", {"widget": "concurrent-timeline"}),
        ("10-concurrency-and-systems/05-distributed-data-structures-teaser.md", {"widget": "RaftAnimator (reuse existing)"}),
        ("11-dsa-in-real-systems/01-postgres-b-tree-and-the-write-path.md", {"widget": "BTreeWalker (reuse existing)"}),
        ("11-dsa-in-real-systems/02-linux-red-black-tree-in-the-cfs-scheduler.md", {"widget": "binary-tree", "mode": "rbt"}),
        ("11-dsa-in-real-systems/03-redis-internal-encodings.md", {"widget": "hash-table + linked-list + array-traversal"}),
        ("11-dsa-in-real-systems/04-git-merkle-dag.md", {"widget": "graph-explorer", "mode": "dag"}),
        ("11-dsa-in-real-systems/05-lsm-trees-rocksdb-cassandra.md", {"widget": "lsm-tree"}),
        ("11-dsa-in-real-systems/06-network-data-plane.md", {"widget": "stack-queue", "mode": "queue"}),
    ])

    out_path = "/Users/aniket/Development/homelab/codefolio/docs/migration/conversion-manifest.json"
    with open(out_path, "w") as f:
        json.dump(out, f, indent=2)
    print(f"Wrote {out_path}")
    print(f"Total source diagrams indexed: {grand_total}")


if __name__ == "__main__":
    main()
