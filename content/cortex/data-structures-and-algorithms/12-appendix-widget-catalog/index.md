---
title: DSA Widget Catalog
summary: Living authoring reference for the D3.js interactive widget catalog used in the DSA book. One chapter per widget with representative payloads.
prereqs: []
---

# DSA Widget Catalog

This appendix is the **living authoring reference** for the D3.js interactive
widgets used throughout this book.

## What this appendix is

Each chapter exhibits **one widget type** from the closed widget catalog
registered in `client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala`.
For each widget, you get:

- A short purpose statement
- The **payload schema** (JSON shape, mode flags, optional fields)
- **3-5 representative payloads** wrapped in live widget fences — view them in
  the browser to exercise step controls, transitions, and edge cases
- A **source-frame to widget-step compression** note (when porting from the
  original Lesson Source's PNG frame sequences)
- The full spec document link in `docs/migration/widget-specs/<widget>.md`

## How to use it

**Authoring a chapter**: when writing widget payloads in DSA chapters, open
the relevant widget's catalog chapter side-by-side. Copy a payload that
matches the topology you need; adjust items, steps, and markers to fit the
chapter's narrative.

**Building a new widget (Arc 1)**: this appendix is also the build target.
Adding a widget = write Scala module + dispatcher case + CSS + **a chapter
here** with at least 3 payloads. The chapter doubles as visual QA — if a
payload doesn't render correctly in the browser, the widget isn't done.

**Reviewing an existing widget**: each chapter shows the widget in isolation,
so visual drift between source frames and widget output is easy to spot.

## Catalog index (27 widgets)

> **Arc 1 status (2026-05-18): functionally complete.** The 11 Tier 1
> widgets cover 100% of the current DSA book's widget-shaped diagram
> demand (267 + 149 + 142 + 131 + 68 + 58 + 32 + 20 + 12 + 5 = 884
> diagrams routed to 10 distinct widgets, all shipped). The 16 widgets
> across Tier 2-8 are **deferred** — they're designed for chapters that
> don't yet exist in the book. Build any one on demand, only when the
> first authored chapter exercises it.
>
> Routing-audit details live in the repo-local `docs/migration/routing-audit-2026-05-18.md`.

### Tier 1 — Core source-DSA topologies (11) — ✅ COMPLETE

Status legend: **Arc 1 done** = widget shipped *and* catalog chapter
landed (the pair of artefacts Arc 1 produces). **deferred** = spec'd
but no source diagrams currently demand it — build on demand.

| Widget | Status | Source diagrams |
|---|---|---:|
| `array-traversal` | Arc 1 done | 267 |
| `stack-queue` | Arc 1 done | 149 |
| `binary-tree` | Arc 1 done | 142 |
| `graph-explorer` | Arc 1 done | 131 |
| `hash-table` | Arc 1 done | 68 |
| `linked-list` | Arc 1 done | 58 |
| `heap-tree` | Arc 1 done | 32 |
| `call-stack` | Arc 1 done | 20 |
| `decision-tree` | Arc 1 done | 12 |
| `dp-table` | Arc 1 done | 5 |
| `trie` | Arc 1 done | 0 (future trie chapters) |


### Tier 2 — Foundations (4) — deferred

For an unwritten `01-foundations/` directory (recurrence + master
theorem, amortized analysis, growth-rate visualisations, cache models).
Note: 19 `recurrence-tree` keyword-router hits + 2 `amortized-payment`
hits were audited and confirmed to be false positives — they need
KaTeX math blocks, not these widgets. See routing-audit-2026-05-18.md.

| Widget | Status |
|---|---|
| `growth-rate-chart` | deferred |
| `recurrence-tree` | deferred |
| `amortized-payment` | deferred |
| `cache-hit-miss` | deferred |

### Tier 3 — Advanced trees (3) — deferred

For an unwritten "advanced trees" phase. Note: 1 `dsu-forest`
keyword-router hit was audited and re-routed to `graph-explorer` (it's
a max-flow min-cut diagram, not a union-find forest).

| Widget | Status |
|---|---|
| `segment-tree` | deferred |
| `fenwick-tree` | deferred |
| `dsu-forest` | deferred |

### Tier 4 — Strings (2) — deferred

The current book has no `07-strings/` directory. Build when string
chapters get authored.

| Widget | Status |
|---|---|
| `string-matcher` | deferred |
| `suffix-array-builder` | deferred |

### Tier 5 — Bit tricks (1) — deferred

| Widget | Status |
|---|---|
| `bit-grid` | deferred |

### Tier 6 — Probabilistic & advanced (4) — deferred

| Widget | Status |
|---|---|
| `skip-list` | deferred |
| `bloom-filter` | deferred |
| `count-min-sketch` | deferred |
| `hyperloglog` | deferred |

### Tier 7 — Concurrency (1) — deferred

Note: 4 `concurrent-timeline` keyword-router hits were audited and
re-routed to `array-traversal` (memory-as-row-of-blocks diagrams, not
concurrency).

| Widget | Status |
|---|---|
| `concurrent-timeline` | deferred |

### Tier 8 — Real systems (1) — deferred

| Widget | Status |
|---|---|
| `lsm-tree` | deferred |

## Reuses (existing infrastructure widgets)

These widgets already exist for the `system-design` Cortex book and are
**reused** in the DSA book without modification — they don't get their own
catalog chapter here:

- `BTreeWalker` — used for `03-trees/08-b-tree/` and
  `11-dsa-in-real-systems/01-postgres-b-tree-and-the-write-path.md`
- `RaftAnimator` — used for
  `10-concurrency-and-systems/05-distributed-data-structures-teaser.md`

## Related docs

These live in the repo-local `docs/` tree (not part of the published book):

- `docs/migration/methodology.md` — standing methodology brief
- `docs/migration/diagram-gap-audit.md` — scope + backlog + per-chapter inventory
- `docs/migration/widget-specs/` — per-widget specs (one file per widget)
- `docs/migration/conversion-manifest.json` — per-diagram source→destination mapping
