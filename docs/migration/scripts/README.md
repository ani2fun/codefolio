# Migration helper scripts

Generators and slicers for the DSA migration manifest.

## `gen-manifest.py`

Regenerates `../conversion-manifest.json` from the source lesson tree at
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/`.

```bash
python3 docs/migration/scripts/gen-manifest.py
```

Re-run whenever:
- A new source lesson is added (unlikely — source is supposed to be frozen)
- A destination chapter is renamed (update `DEST_FILE_OVERRIDES` first)
- A new orphan chapter is created (update the `orphan_chapters` dict)
- A widget→phase assignment changes (update `PHASE_MAP`)

Idempotent. Commit the diff.

## `chapter-slice.py`

Extracts one destination chapter's slice from the manifest. Used by Arc 3
chapter-conversion agents.

```bash
python3 docs/migration/scripts/chapter-slice.py \
  02-linear-structures/04-doubly-linked-list/03-insertion-in-doubly-linked-lists.md
```

Outputs JSON with:
- `source_phase`, `phase_name`, `phase_status`, `phase_blocked_on`
- `widget_required`, `widget_mode`
- `source_chapter_dir` — read all `*.md` from here
- `destination_file` — the chapter to edit
- `source_lessons` — list of source `.md` files feeding this chapter
- `interactive_diagrams` — list of diagrams (source_path, line, frame_count,
  step_target, description, destination_anchor_hint, widget, widget_mode)

An Arc 3 agent prompt looks like:

```
Pipe the slice JSON to your agent. The agent reads each source lesson, finds
the H1/H2 anchor closest to destination_anchor_hint in the destination file,
and emits a `d3 widget=<widget_required>` block with N steps where
N = step_target.
```
