---
title: DSA Widget Catalog
summary: Living authoring reference for the D3.js interactive widget catalog used in the DSA book. One chapter per widget with representative payloads.
prereqs: []
---

# DSA Widget Catalog

This book is the **living authoring reference** for D3.js interactive widgets in
the `data-structures-and-algorithms` Cortex book.

## What this book is

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

**Building a new widget (Arc 1)**: this book is also the build target.
Adding a widget = write Scala module + dispatcher case + CSS + **a chapter
here** with at least 3 payloads. The chapter doubles as visual QA — if a
payload doesn't render correctly in the browser, the widget isn't done.

**Reviewing an existing widget**: each chapter shows the widget in isolation,
so visual drift between source frames and widget output is easy to spot.

## Catalog index (27 widgets)

> Chapters land as Arc 1 progresses. Empty slots indicate widgets not yet
> built.

### Tier 1 — Core source-DSA topologies (11)

| Widget | Status |
|---|---|
| `array-traversal` | ✓ existing |
| `linked-list` | ✓ existing |
| `hash-table` | Arc 1 |
| `stack-queue` | Arc 1 |
| `binary-tree` | Arc 1 |
| `heap-tree` | Arc 1 |
| `graph-explorer` | Arc 1 |
| `call-stack` | Arc 1 |
| `decision-tree` | Arc 1 |
| `dp-table` | Arc 1 |
| `trie` | Arc 1 |

### Tier 2 — Foundations (4)

| Widget | Status |
|---|---|
| `growth-rate-chart` | Arc 1 |
| `recurrence-tree` | Arc 1 |
| `amortized-payment` | Arc 1 |
| `cache-hit-miss` | Arc 1 |

### Tier 3 — Advanced trees (3)

| Widget | Status |
|---|---|
| `segment-tree` | Arc 1 |
| `fenwick-tree` | Arc 1 |
| `dsu-forest` | Arc 1 |

### Tier 4 — Strings (2)

| Widget | Status |
|---|---|
| `string-matcher` | Arc 1 |
| `suffix-array-builder` | Arc 1 |

### Tier 5 — Bit tricks (1)

| Widget | Status |
|---|---|
| `bit-grid` | Arc 1 |

### Tier 6 — Probabilistic & advanced (4)

| Widget | Status |
|---|---|
| `skip-list` | Arc 1 |
| `bloom-filter` | Arc 1 |
| `count-min-sketch` | Arc 1 |
| `hyperloglog` | Arc 1 |

### Tier 7 — Concurrency (1)

| Widget | Status |
|---|---|
| `concurrent-timeline` | Arc 1 |

### Tier 8 — Real systems (1)

| Widget | Status |
|---|---|
| `lsm-tree` | Arc 1 |

## Reuses (existing infrastructure widgets)

These widgets already exist for the `system-design` Cortex book and are
**reused** in the DSA book without modification — they don't get their own
catalog chapter here:

- `BTreeWalker` — used for `03-trees/08-b-tree/` and
  `11-dsa-in-real-systems/01-postgres-b-tree-and-the-write-path.md`
- `RaftAnimator` — used for
  `10-concurrency-and-systems/05-distributed-data-structures-teaser.md`

## Related docs

- [`docs/migration/methodology.md`](../../../docs/migration/methodology.md) — standing methodology brief
- [`docs/migration/diagram-gap-audit.md`](../../../docs/migration/diagram-gap-audit.md) — scope + backlog + per-chapter inventory
- [`docs/migration/widget-specs/`](../../../docs/migration/widget-specs/) — per-widget specs (one file per widget)
- [`docs/migration/conversion-manifest.json`](../../../docs/migration/conversion-manifest.json) — per-diagram source→destination mapping
