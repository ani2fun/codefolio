#!/usr/bin/env python3
"""Extract a single destination chapter's slice from conversion-manifest.json.

Usage:
  ./chapter-slice.py 02-linear-structures/04-doubly-linked-list/03-insertion-in-doubly-linked-lists.md

Prints a JSON document containing:
  - source_lessons: list of source .md files feeding this destination chapter
  - interactive_diagrams: list of diagrams (with widget, step compression, etc.)
  - destination_file: the chapter path
  - widget_required: the primary widget for the phase
  - phase_metadata: phase status, blockers, etc.

Used by Arc 3 chapter-conversion agents: each agent gets one slice, reads the
source lessons + the destination file, and emits widget payloads at the right
anchors per the manifest.
"""

import json
import os
import sys

MANIFEST_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..",
    "conversion-manifest.json",
)


def find_slice(dest_chapter_relpath):
    """Find the manifest entry for the given destination chapter path.

    dest_chapter_relpath is relative to
    content/cortex/data-structures-and-algorithms/, e.g.
    '02-linear-structures/04-doubly-linked-list/03-insertion-in-doubly-linked-lists.md'
    """
    with open(MANIFEST_PATH) as f:
        m = json.load(f)

    # Search phases first
    for phase_id, phase in m["phases"].items():
        for ch_id, ch in phase["chapters"].items():
            if ch["destination_file"] == dest_chapter_relpath:
                return {
                    "source_phase": phase_id,
                    "phase_name": phase["phase_name"],
                    "phase_status": phase.get("status"),
                    "phase_blocked_on": phase.get("blocked_on"),
                    "widget_required": phase["primary_widget"],
                    "widget_mode": phase.get("widget_mode", ""),
                    "source_chapter_dir": f"{phase_id}/{ch_id}",
                    "destination_file": ch["destination_file"],
                    "source_lessons": ch["source_lessons"],
                    "interactive_diagrams": ch["interactive_diagrams"],
                    "interactive_diagram_count": ch["interactive_diagram_count"],
                }

    # Then orphan chapters
    if dest_chapter_relpath in m["orphan_chapters"]:
        orphan = m["orphan_chapters"][dest_chapter_relpath]
        return {
            "source_phase": None,
            "phase_name": "Orphan chapter",
            "phase_status": "orphan",
            "phase_blocked_on": None,
            "widget_required": orphan.get("widget"),
            "widget_mode": orphan.get("mode", ""),
            "source_chapter_dir": None,
            "destination_file": dest_chapter_relpath,
            "source_lessons": [],
            "interactive_diagrams": [],
            "interactive_diagram_count": 0,
            "note": "Orphan chapter — payloads derived from destination prose + demo book payloads",
        }

    return None


def main():
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <dest-chapter-relpath>", file=sys.stderr)
        sys.exit(1)

    slice_data = find_slice(sys.argv[1])
    if slice_data is None:
        print(f"No manifest entry for {sys.argv[1]}", file=sys.stderr)
        sys.exit(2)

    print(json.dumps(slice_data, indent=2))


if __name__ == "__main__":
    main()
