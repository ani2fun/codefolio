# Wave 1 Foundations — revisit retrospective

> Written at the end of the second pass over Part 1 lessons (foundations 1–5).
> The first pass shipped the prose; this revisit upgraded the diagram and
> interactivity stack from "static SVG + Mermaid + Python" to "LikeC4 +
> in-page widgets", per the plan at
> `~/.claude/plans/system-design-curriculum-partitioned-fog.md`.

## What changed, lesson by lesson

| Lesson | Before | After |
|---|---|---|
| **1 — What "system design" actually means** | 2 Structurizr-rendered SVG `<img>` (personal + scaled book tracker), 3 Mermaid blocks | 2 live LikeC4 iframe views (`foundations_book_tracker_personal`, `foundations_book_tracker_scaled`), same Mermaid block. Frontmatter, broken-link fixes, slug URLs across cross-Part references. |
| **2 — Numbers every engineer should know** | 16-row latency table + Python `measure()` calibration | Added `LatencyScaledTime` widget (log-axis bar chart, click any row to see the human-scale equivalent). NVMe Gen5 bump in the bandwidth table. New Exercise 4 (cross-region cost reflex: 1 ns + 16 µs + 150 ms → cross-region hop is 99.989% of total). Fixed two broken external links. |
| **3 — Back-of-envelope estimation** | Worked Twitter / YouTube / WhatsApp BOTE + Python template | Added `EstimationCalculator` widget — preset chips (Twitter/X, YouTube views, WhatsApp, personal book tracker) + 4 numeric inputs + 7-row live output. Verified the Twitter preset reproduces the lesson body's 4K/12K/100K/300K/25× numbers and the WhatsApp solution's 600K/1.8M/1× numbers. Fixed three broken external links (USENIX SREcon, YouTube blog, High Scalability) and three cross-Part links. |
| **4 — The CAP theorem and PACELC, honestly** | 1 Structurizr-rendered SVG (CP cluster) + Python `examples/04-cap-pacelc-simulator/` | LikeC4 view `foundations_cp_cluster_containers` replaces the SVG. New `PartitionSimulator` widget — 4 step-replay scenarios (CP minority-refuses, AP LWW reconciliation, CP majority-keeps-writing, CP happy path) with mode-badged CP/AP chips, three-node SVG with broken-link rendering and a REFUSED badge. Dropped a fabricated "Capstone 11" cross-reference. |
| **5 — Latency, throughput, and the USL** | Mermaid flowcharts + Python `examples/05-littles-law-queueing/` simulator | Added `QueueingSimulator` widget — bar chart of M/M/1 inflation across ten ρ samples (0.10 → 0.99), live slider, readout with mean W / queue length / verdict band aligned with the lesson's ρ ≈ 0.6–0.7 production target. Off-chart bars overflow with a red arrow so the cliff is visceral. Dropped the broken Cloudflare citation; replaced with Discord trillion-messages (on-topic). |
| **bin/dev** | `LIKEC4_URL=http://localhost:8090` 502'd via the ZIO proxy on macOS | Fixed to `http://127.0.0.1:8090` — the JDK HttpClient prefers `::1` for `localhost` on macOS, and Docker Desktop binds IPv4-only. |
| **Walker** | `c4/` lesson subdirs would render as ghost Sections | Added `"c4"` to `CortexIndexWalker.ReservedAuxDirs` (alongside `"examples"`); covered by spec. |
| **LikeC4 build pipeline** | Only collected `content/cortex/system-design/05-application-architecture/c4/workspace.c4` | `Dockerfile.likec4` and the workflow's path filter both generalise to `content/cortex/**/*.c4`. Each Part keeps its lesson `.c4` files in a sibling `c4/` subdir; LikeC4 reads them recursively into one project. |

## Foundations exit-gate sweep

- Every Structurizr SVG under `content/cortex/system-design/01-foundations/` deleted. The `diagrams/` directory itself is gone.
- `Makefile`, `docker-compose.diagrams.yml`, and the `content/cortex/**/diagrams/*.puml` line in `.gitignore` are removed. The Structurizr → PlantUML → SVG pipeline is retired.
- Smoke tests:
  - `sbt scalafmtCheckAll scalafmtSbtCheck` clean.
  - `sbt test` green (server + shared).
  - `bin/dev` renders all 5 lessons with the iframes and widgets working.

## Conventions that crystallised this wave

1. **LikeC4 multi-file project, one shared spec.** `workspace.c4` (the homelab) is the sole owner of `specification { element actor; element system; element container }`. Every lesson `.c4` file declares only `model { … }` and `views { … }`, inheriting the kinds. Duplicate `element <kind>` declarations across files trigger a LikeC4 compile error. View names live in a flat global namespace; lesson views must carry their lesson scope as a prefix (`foundations_book_tracker_personal`, `foundations_cp_cluster_containers`). Recorded in `memory/likec4_multi_file_convention.md`.

2. **One commit per lesson.** Every lesson rewrite ships in its own commit with a focused message that itemises (a) the widget, (b) the LikeC4 port if any, (c) the link fixes, (d) the verification. Easy to revert; reads cleanly in the log.

3. **Widget = ~300 LOC + BEM CSS in `d3-widgets.css`.** Every D3 widget mirrors the `ArrayTraversal` precedent — case-class `Spec`, `js.JSON.parse` → `Either` parser, SVG-string builder injected via `dangerouslySetInnerHTML`, a single `Component` value. CSS lives alongside `array-traversal__*` in one shared BEM file. No per-widget CSS imports.

4. **Per-widget spec test is a follow-up.** The plan called for a `WidgetSpec.scala` per widget; the client sbt project has no test framework today and the `ArrayTraversal` precedent has no spec either. Adding a client test runtime + back-filling specs for `ArrayTraversal`, `LatencyScaledTime`, `EstimationCalculator`, `PartitionSimulator`, and `QueueingSimulator` is a single follow-up infra commit.

5. **Cross-chapter links use absolute Cortex slug URLs.** Same-Part `./04-cap-and-pacelc.md` and cross-Part `../2.building-blocks/08-caching.md` both became `/cortex/system-design/foundations-cap-and-pacelc` and `/cortex/system-design/building-blocks-caching`. The frontend's slug routing is the single source of truth.

6. **Frontmatter is mandatory.** Each lesson gets `title` + `summary`; the title matches the in-body `# ` heading exactly; the summary becomes the sidebar tooltip. The walker uses frontmatter title first, then H1, then humanised filename.

## What didn't go into Wave 1 but should into Wave 2

- **Client test infrastructure.** Add zio-test (or similar) to the `client` sbt project so per-widget JSON-parser specs can land. Back-fill all five existing widgets.
- **Visual regression smoke** for the widgets. The render verification we did this wave was per-widget snapshot via the preview MCP tool; a permanent Playwright test would catch CSS / theme regressions automatically.
- **Promote `EstimationCalculator` and `QueueingSimulator` upstream**: both widgets are re-usable across capstones. When Wave 2 capstones land, their authors should reach for these before inventing new ones.
- **Capstone-side cross-references go stale fast.** The slug URL convention is correct, but until the capstones actually exist the links 404. Once Wave 5 starts, do a one-pass sweep over every Wave-1 lesson and confirm the forward references resolve.

## Time and shape

Wave 1 revisit ran in roughly one focused session. Per-lesson time was dominated by widget authoring (~30 min each) and verification (~15 min each). The biggest single time-sink was the off-screen-click discovery in Lesson 2 (the preview tool's coord-based click missed the targeted SVG hit rect), which surfaced as a benign bug in the test driver, not the widget. Worth knowing for Wave 2: for any new widget, dispatch test clicks via `element.dispatchEvent(new MouseEvent('click', { bubbles: true }))` rather than `preview_click(selector)`.

## File-level summary of what landed

```
content/cortex/system-design/01-foundations/
├── 01-what-system-design-means.md         # rewrite (+frontmatter, 2 iframes, 5 link fixes)
├── 02-numbers-every-engineer-should-know.md  # rewrite (+widget, +Ex 4, link fixes, NVMe Gen5)
├── 03-back-of-envelope-estimation.md      # rewrite (+widget, link fixes)
├── 04-cap-and-pacelc.md                   # rewrite (+iframe, +widget, link fixes)
├── 05-latency-throughput-usl.md           # rewrite (+widget, link fixes)
├── c4/
│   ├── book-tracker.c4                    # new (Lesson 1)
│   └── cp-cluster.c4                      # new (Lesson 4)
└── examples/                              # unchanged

client/src/main/scala/codefolio/client/components/cortex/widgets/
├── ArrayTraversal.scala                   # unchanged (precedent)
├── LatencyScaledTime.scala                # new (Lesson 2)
├── EstimationCalculator.scala             # new (Lesson 3)
├── PartitionSimulator.scala               # new (Lesson 4)
└── QueueingSimulator.scala                # new (Lesson 5)

client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala
                                            # 4 new cases registered
client/src/styles/components/d3-widgets.css # 4 new BEM blocks

shared/src/main/scala/codefolio/shared/cortex/CortexIndexWalker.scala
                                            # "c4" added to ReservedAuxDirs
shared/src/test/.../CortexIndexWalkerSpec.scala
                                            # spec for the new ReservedAux

bin/dev                                     # LIKEC4_URL → 127.0.0.1
Dockerfile.likec4                           # globs content/cortex/**/c4/*.c4
.github/workflows/likec4-build-push-promote.yml
                                            # trigger path generalised
.gitignore                                  # dropped the .puml ignore line
```

Files removed:
```
Makefile
docker-compose.diagrams.yml
content/cortex/system-design/01-foundations/diagrams/   # whole directory
  ├── book-tracker.dsl
  ├── cp-cluster.dsl
  ├── pipeline-test.{dsl,d2,mmd}
  ├── structurizr-CpCluster{Context,Containers}.svg
  ├── structurizr-Personal{Context,Containers}.svg
  ├── structurizr-Pipeline{Context,Containers}.svg
  └── structurizr-Scaled{Context,Containers}.svg
```
