# Wave 2 Building Blocks — retrospective

> Written at the end of Wave 2 — Part 2 lessons 6 through 14. The Wave-1 retro
> shipped the diagram + interactivity stack (LikeC4 iframes + D3 widgets); this
> wave applied that stack to **nine lessons in a row**, growing the widget
> catalog from 5 to 12 and the LikeC4 view count from 3 to 13.

## What landed, lesson by lesson

| Lesson | Widget | LikeC4 views | Examples folder | Word count |
|---|---|---|---|---|
| **6 — Networking primer** | `HandshakeTimeline` (new) | `_overview`, `_cdn_internals` (2 new) | — | ~4.9k |
| **7 — Load balancing** | `ConsistentHashRing` (new) | `_l4`, `_l7` (2 new) | NGINX + 3 FastAPI replicas | ~3.8k |
| **8 — Caching** | `CacheStampedeSimulator` (new) | `_overview` (1 new) | Redis + `/no-coalesce` vs `/coalesced` | ~3.7k |
| **9 — Relational databases** | `BTreeWalker` (new) | `_internals` (1 new) | Postgres + EXPLAIN ANALYZE demo | ~4.2k |
| **10 — NoSQL families** | — (reuse `ConsistentHashRing`) | — | — | ~4.1k |
| **11 — Replication** | `ReplicationLagSimulator` (new) | `_single_leader`, `_multi_leader`, `_leaderless` (3 new) | Postgres primary + streaming replica | ~3.9k |
| **12 — Sharding and partitioning** | `HotShardSimulator` (new) | `_overview` (1 new) | Pure-Python four-strategy demo | ~3.8k |
| **13 — Consistency models** | — (reuse `ReplicationLagSimulator` + `PartitionSimulator`) | — | — | ~3.3k |
| **14 — Consensus (Raft)** | `RaftAnimator` (new) | `_cluster` (1 new) | — (etcd reference + Bendersky Go series) | ~4.7k |

Seven new widgets, ten new LikeC4 views, five new `examples/` directories, ~36k words of new lesson prose. Every lesson hit the Wave-1 quality bar: dated motivation, physical-world analogy, formal definitions with tables, worked example, quantified trade-off table, 6 edge-case subsections, 3 graded exercises with `<details>` solutions, 4–5 In-the-Wild citations.

## Conventions that crystallised this wave

1. **Per-lesson identifier-prefix policy is locked in.** `workspace.c4` keeps unprefixed canonical identifiers (`user`, `homelab`, `ms1`, `wk1`, `wk2`, `edge`); every lesson `.c4` file uses a short prefix (`bt`, `cp`, `net`, `lb`, `cache`, `rel`, `rep`, `shard`, `raft`). The Lesson 6 build hit `Duplicate element name user` because both `workspace.c4` and `networking.c4` declared `user`. The full refactor that locked the policy was commit `8d09413`; the memory note `likec4_multi_file_convention.md` records the prefix table.

2. **LikeC4 has three teaching gotchas, all now in the memory note.** (a) An actor cannot be the `of` target of a scoped view — only systems and containers. (b) `include *` in a landscape view (no `of`) pulls in every element across every `.c4` file in the project; use explicit `include <id1>, <id2>, …` for per-lesson scope. (c) `system → its-own-container` arrows at the top level are rejected as `Invalid parent-child relationship`; container relationships must be declared inside the parent system's block.

3. **Three widget-implementation gotchas, caught during verification.** All worth carrying forward to Wave 3:
   - `^.onChange ==>` accepts a function returning `Callback`, not `Unit`. If the helper's return type is `Unit`, the inner `setState` Callback is constructed and silently discarded; the slider feels dead.
   - `java.util.Arrays.sort(Array[Object], Comparator)` silently no-ops on Scala.js. Use `sortInPlaceBy` from the Scala stdlib instead.
   - Single-pass MurmurHash3 clusters short prefix-shared strings (`node-0-v0`, `node-1-v0`). For widgets that hash similar inputs (the consistent-hash ring), use a two-stream mix with a final avalanche pass.

4. **Lesson length calibration: 3.3k–4.9k words.** The plan called for 2,800–3,500. Wave 2's average came in at ~4.0k. The over-shoot is mostly the widget JSON payloads (which `wc -w` counts), the trade-off tables, and the worked-example code blocks. Actual prose is ~2,900–3,500. No lesson felt padded; no lesson felt rushed.

5. **Lesson 10 (NoSQL) and Lesson 13 (Consistency models) intentionally lighter.** Per the v4 plan, these are vocabulary-and-fit lessons rather than operational. No new widget, no LikeC4, no examples folder; pure prose + comparison tables. They ship at ~25 min of authoring time vs ~90 min for a widget-heavy lesson. Wave 3 will repeat the pattern: lessons 16 (pub/sub) and 19 (sagas) are similarly conceptual.

## Files / paths affected (Wave 2)

```
content/cortex/system-design/02-building-blocks/
├── index.md                              # rewritten this wave (outcomes + widget table + examples list)
├── 06-networking-primer.md               # full rewrite from stub (was 2 lines)
├── 07-load-balancing.md                  # full rewrite from stub
├── 08-caching.md                         # full rewrite from stub
├── 09-relational-databases.md            # full rewrite from stub
├── 10-nosql-families.md                  # full rewrite from stub
├── 11-replication.md                     # full rewrite from stub
├── 12-sharding-and-partitioning.md       # full rewrite from stub
├── 13-consistency-models.md              # full rewrite from stub
├── 14-consensus-paxos-and-raft.md        # full rewrite from stub
├── c4/
│   ├── caching.c4                        # new — 1 view
│   ├── load-balancing.c4                 # new — 2 views
│   ├── networking.c4                     # new — 2 views
│   ├── raft.c4                           # new — 1 view
│   ├── relational-databases.c4           # new — 1 view
│   ├── replication.c4                    # new — 3 views
│   └── sharding.c4                       # new — 1 view
└── examples/
    ├── 07-load-balancing-nginx/          # new — NGINX + 3 Py
    ├── 08-caching-redis-stampede/        # new — Redis + 2 endpoints
    ├── 09-relational-explain-analyze/    # new — Postgres + EXPLAIN
    ├── 11-replication-postgres-streaming/# new — primary + replica
    └── 12-sharding-strategies/           # new — pure Python

client/src/main/scala/codefolio/client/components/cortex/widgets/
├── BTreeWalker.scala                     # new — Lesson 9
├── CacheStampedeSimulator.scala          # new — Lesson 8
├── ConsistentHashRing.scala              # new — Lesson 7
├── HandshakeTimeline.scala               # new — Lesson 6
├── HotShardSimulator.scala               # new — Lesson 12
├── RaftAnimator.scala                    # new — Lesson 14
└── ReplicationLagSimulator.scala         # new — Lesson 11

client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala
                                          # 7 new cases registered (now 12 total)
client/src/styles/components/d3-widgets.css
                                          # 7 new BEM blocks

content/cortex/system-design/01-foundations/c4/
├── book-tracker.c4                       # refactor: `bt` prefix on all identifiers
└── cp-cluster.c4                         # refactor: `cp` prefix on all identifiers

docs/system-design/wave-2-retro.md        # this file
```

## What didn't go into Wave 2 but should into Wave 3

- **Client test infrastructure**, carried over from Wave 1. Per-widget JSON-parser specs + visual regression smoke. The current verification cycle is "drive the widget in `bin/dev`, eyeball the readout"; a permanent test harness would catch CSS / theme regressions and parser-edge-case breaks automatically.
- **Capstone forward references resolve to 404s.** Every Building Blocks lesson cross-references `/cortex/system-design/capstones-url-shortener` and friends. Once Wave 5 starts, do a sweep over Wave-2 lessons and confirm every forward reference still resolves correctly (the capstone slugs will be stable, but the deeper anchors won't).
- **Promote `EstimationCalculator` upstream into Lesson 12.** The hot-shard math exercise computes effective shard capacity; that's the same shape `EstimationCalculator` does. A widget call-out in Lesson 12 would extend the catalog reuse story.

## Time and shape

Wave 2 ran across several sessions, roughly:

- Lesson 6 (Networking primer) — ~90 min including the bin/dev recovery + LikeC4-bug debugging.
- Lessons 7–9 — ~75–90 min each, dominated by widget authoring (~45 min) + verification (~20 min) + markdown (~15 min).
- Lesson 10 (NoSQL) — ~25 min (no widget, no LikeC4, no examples).
- Lessons 11–12 — ~75 min each.
- Lesson 13 (Consistency models) — ~25 min.
- Lesson 14 (Consensus) — ~90 min; the RaftAnimator with three scripted scenarios was the most substantial widget.

The first three lessons paid the most debugging tax — every kind of LikeC4 gotcha (actor-as-view-root, system→its-own-container arrow, identifier collisions) and every kind of widget gotcha (Callback discard, Scala.js Arrays.sort no-op, MurmurHash3 clustering) surfaced in Lessons 6–9. Lessons 10 onward ran cleanly because the gotchas were caught and the memory note was updated as they happened.

The Wave-2 audit pass (this commit) caught one stale element — the `Status: stubs. Wave 2 of the build plan.` line on the Building Blocks index — and surfaced a small set of cross-references that strengthened the lesson web. No structural rewrites; the lessons hit the quality bar on first pass.

## Catalog at end of Wave 2

| Widget | Origin lesson | Reused in |
|---|---|---|
| `array-traversal` | DSA book | — |
| `latency-scaled-time` | Foundations 2 | (no Wave-2 reuse yet) |
| `estimation-calculator` | Foundations 3 | (Lesson 12 forward) |
| `partition-simulator` | Foundations 4 | Lesson 13 |
| `queueing-simulator` | Foundations 5 | (no Wave-2 reuse yet) |
| `handshake-timeline` | Lesson 6 | (Capstone 37 forward) |
| `consistent-hash-ring` | Lesson 7 | Lessons 8 + 10 (referenced); 12 |
| `cache-stampede` | Lesson 8 | (Capstones 37, 38 forward) |
| `btree-walker` | Lesson 9 | (Capstone 37 forward; Lesson 22 forward) |
| `replication-lag` | Lesson 11 | Lesson 13 |
| `hot-shard` | Lesson 12 | (Capstones 38, 39 forward) |
| `raft-animator` | Lesson 14 | (Capstone 44 forward) |

12 widgets total. Six of the seven new widgets have at least one in-Wave-2 reuse or near-term forward reference. The reuse claim in each widget's doc comment was preserved through scalafmt passes and is accurate at the end of Wave 2.
