# DSA Migration — Methodology Brief

> Read this first before touching any chapter, widget, or commit. This is the
> single source of truth for **how** the DSA migration is executed. See
> `docs/migration/diagram-gap-audit.md` for **what** is in scope and
> `docs/migration/conversion-manifest.json` for the per-diagram mapping that
> drives execution.

## What the migration is

The DSA book at `content/cortex/data-structures-and-algorithms/` is being
realigned against the canonical **Lesson Source** at
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/`. Source
uses pre-rendered PNG frame sequences marked with `// Interactive Diagram
(N frames): …`. Destination replaces these with D3.js interactive widgets
(closed catalog — ADR-0006) and static Mermaid/D2 figures for single-frame
concepts.

### Two parallel workstreams

1. **Code alignment** — the destination's per-problem code (5 language tabs:
   Pseudocode + Python + Java + C + Scala) must mirror the source's exact
   method decomposition, variable names, and inline comments. See **Verbatim
   Code Alignment** below.
2. **Diagram migration** — every source `// Interactive Diagram` marker must
   become a D3 widget instance in destination, compressed from N frames to
   5-10 meaningful steps. See **Widget catalog** below.

Both workstreams operate on the same chapter files but are governed by
different rules.

## Source / destination glossary

| Term | Meaning |
|---|---|
| **Lesson Source** | The read-only canonical reference at `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/`. **Never modify.** Each leaf chapter dir holds 3-15 lesson `.md` files + co-located `.assets/` directories with PNG diagrams. |
| **Destination chapter** | One `.md` file in `content/cortex/data-structures-and-algorithms/` that consolidates all source lessons from one source chapter dir. E.g., source `data-structure/2.singly-linked-list/03-insertion-…/*.md` (10 lessons) → destination `02-linear-structures/03-singly-linked-list/03-insertion-in-singly-linked-lists.md` (1 file). |
| **Interactive Diagram** | Source marker `// Interactive Diagram (N frames): <description>` followed by N PNG frame refs. Converts to one D3 widget instance in destination. |
| **Verbatim Code Alignment** | The rule that destination's code must structurally mirror source's. See dedicated section below. |
| **Orphan chapter** | Destination chapter with no source counterpart (e.g., `09-probabilistic-and-advanced/02-bloom-filter.md`). In scope for this migration; payloads written from destination prose + widget demo examples. |
| **Widget catalog** | The closed set of D3 widget types registered in `client/.../components/cortex/D3WidgetBlock.scala`. Adding a new widget requires ADR + Scala module + CSS + dispatcher case + demo book chapter. |
| **Demo book** | `content/cortex/dsa-widget-catalog/` — a Cortex book exhibiting every widget with 3-5 representative payloads. Acts as the living authoring reference. |

## Authority matrix

Who wins each section of a destination chapter:

### Source wins (port verbatim)

- **Problem statement** — port verbatim; de-escape backslashes; fix indentation
- **Examples (I/O pairs)** — use source's exact arrays + outputs
- **Solution code in each of the 5 tabs** — see Verbatim Code Alignment below
- **Inline code comments** — translate idiomatically per language but preserve wording
- **Method decomposition** — if source extracts helpers like `skipDuplicatesLeft`, `closestTwoSum`, destination must too
- **Function naming convention per language** — Python `snake_case`, Java/Scala `camelCase`, C `snake_case`

### Destination wins (keep as-is unless data is stale)

- **Intuition prose** — destination's narrative is genuinely stronger than source's terse explanations
- **Diagnostic Questions / Applying-the-Q sections** — pedagogically valuable; keep
- **Approach prose** — keep
- **Dry-Run text** — keep (but the example *data* should match source's first example)
- **Complexity Analysis** — keep
- **Edge Cases tables** — keep
- **Key Takeaway / Final Takeaway** — keep
- **Section structure (h1/h2 ordering)** — keep
- **5-language tab order** — Pseudocode + Python + Java + C + Scala (no C++/TS/Go/Rust)
- **Diagram conventions (theme, colours, fence syntax)** — keep

When in doubt: read both the source lesson and the destination chapter, then
diff each section before editing. **Don't paraphrase.**

## Verbatim Code Alignment — exact rules

The single most common drift failure mode is "I implemented the same algorithm
but with cleaner variable names". This is **drift** and has been flagged
multiple times (memory `dsa_align_source_code_verbatim`). Apply these rules
to every problem's Solution code:

1. **Method decomposition.** If source has `skipDuplicatesLeft` /
   `skipDuplicatesRight` / `duplicateAwareTwoSum` helper methods (and a
   `fourSum` / `threeSum` orchestrator), the destination must have them too
   — all 5 tabs.
2. **Variable names match source.** `sum` (not `total`), `remainingTarget`
   (not `need`), `maxSum` (not `max_total`), `count_ones` (not `ones`),
   `min_rooms` (not `peak`). Match the source's casing per language (`sum_`
   in Python where `sum` shadows the builtin).
3. **Inline comments verbatim.** "Sort the array in non-decreasing order" /
   "Use a while loop to traverse the array using the two pointers" / "Move
   the left pointer to increase the sum" — translate idiomatically across
   languages but preserve the wording. Don't condense.
4. **Per-language naming convention.** Python uses `snake_case`
   (`four_sum`, `skip_duplicates_left`). Java/Scala use `camelCase`
   (`fourSum`, `skipDuplicatesLeft`). C uses `snake_case`.
5. **Pseudocode reflects the decomposition.** If source splits into
   helpers, the Pseudocode tab must too — multiple `function …` blocks, not
   one fat function.
6. **Sibling consistency.** After updating one problem on a chapter, grep
   the rest of the chapter for the same identifier patterns. If Three Sum
   uses `skipDuplicatesLeft`, Four Sum must too. Mixed styles on the same
   page are a code smell.

## Five language tabs only

Pseudocode → Python → Java → C → Scala, in that file order. No C++,
TypeScript, Go, Rust. (Memory: `dsa_book_language_tabs`.)

## Widget catalog rules (ADR-0006)

- Widgets are **closed catalog** — authors name a widget in the markdown
  fence (` ```d3 widget=<name> `), pass a JSON payload, and the dispatcher
  in `client/.../components/cortex/D3WidgetBlock.scala` routes to the
  appropriate Scala.js + D3 component.
- Authors do **not** write JS in markdown. Raw author-written JS was
  rejected as an `eval` surface and a shift from prose to programming.
- Each widget's payload schema is **widget-local** (the `shared` Block type
  stores raw JSON; the widget's Scala module parses + validates).
- Unknown widget names render an inline error rather than failing the
  chapter — the catalog is a closed match in `D3WidgetBlock`, and a typo
  in markdown does not break the page.
- Adding a new widget: write an ADR → Scala module → CSS BEM block →
  dispatcher case → demo book chapter → verification gates → commit. See
  the per-widget specs in `docs/migration/widget-specs/`.

## Markdown widget syntax

Embed a widget in any chapter `.md` file:

````markdown
```d3 widget=array-traversal
{
  "title": "students[] — 4 names in one contiguous block",
  "items": ["Alice", "Bob", "Carol", "David"],
  "steps": [
    { "marker": 0, "note": "Start at index 0" },
    { "marker": 1, "note": "Advance to index 1" }
  ]
}
```
````

Render pipeline (`client/src/markdown/render.ts` line ~674-695):

1. Custom code-fence handler recognises `d3 widget=<name>`.
2. Emits `<div class="d3-widget" data-widget="<name>" data-payload="<URI-encoded JSON>">`.
3. Client-side `ChapterContent` runs `BlockDiscovery` over the article DOM,
   parses each `<div class="d3-widget">` into a `Block.D3Widget(name, payload)`.
4. `D3WidgetBlock` matches on name and mounts the corresponding Scala.js +
   React + D3 component.

## Commit conventions

Extracted from `git log` history (Phase 0 + Phase 1 commits):

- **Code alignment**: `dsa-<topic>: realign chapter <N> against Lesson Source`
  or `dsa: align <Problem Name> code (ch X.Y <Section>) to source's <pattern>`
- **Diagram conversion**: `dsa: add <name> widget to ch X.Y <section>` for
  per-chapter widget instances; `dsa: ch X.Y <chapter> — restore source's
  N-frame <kind> widget` for re-conversion sweeps
- **Widget code**: `widgets: <action>` for changes under
  `client/.../components/cortex/widgets/`
- **Bulk fix sweeps**: `dsa: canon-fix sweep — N → 0 violations across all
  <topic> chapters (Phase Xa)`

### Cadence

- **One commit per problem** for code-alignment work
- **One commit per concept-section pair** when adding widgets to non-problem
  walkthroughs (e.g., "Understanding the Maximum Overlap Pattern")
- **One commit per chapter** for comment-only sweeps across an existing
  chapter where structure already matches source

### Commit message style

Use a heredoc:

```bash
git commit -m "$(cat <<'EOF'
dsa: align <Problem Name> code (ch X.Y <Section>) to source's <pattern>

<2-3 sentences describing the specific divergence and the fix. Mention
which language tabs changed. Note if behaviour is verified (e.g. "Python
verified to produce all four expected outputs").>
EOF
)"
```

### Hard rules

- **No `Co-Authored-By:` trailers.** Solo author. Memory: `sole_author`.
- **No `git push`** without explicit per-round authorization. Memory:
  `commit_freely_dont_push_until_told`.
- **Never modify** the source under `/Users/aniket/Development/others/tutorial_dsa/`. Read-only.
- **Never skip hooks** (`--no-verify`, `--no-gpg-sign`) without explicit
  user permission.

## Verification gates

Run after every commit, not just at session end:

```bash
# Format check — only if .scala touched
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
sbt 'scalafmtCheckAll' 'scalafmtSbtCheck'

# Compile shared + server + client (any time you touch shared/ or .scala)
sbt 'sharedJVM/compile' 'server/compile' 'client/fastLinkJS'

# Tests
sbt 'test'

# Production build (catches Vite/Tailwind issues)
cd client && npm run build
```

For **content-only changes** (chapter `.md` only), the build checks aren't
strictly required — but run them at chapter boundaries to catch markdown
fence imbalances early.

### Browser smoke

Server is usually running under `./bin/dev` (port 5173). Navigate to the
chapter URL and verify:

1. Page loads without console errors
2. Mermaid blocks render with `<svg>`, not `.mermaid__error`
3. D3 widgets render with cells + markers, no `.d3-widget__error`

Use `mcp__Claude_Preview__*` tools for autonomous verification:
`preview_screenshot` for visual capture; `preview_console_logs` for errors;
`preview_eval` for DOM inspection.

## scalafmt rules

When touching `.scala` files (rare in content work, common when extending
widgets):

1. Scope per-feature with `scalafmtOnly <file>` to avoid unrelated
   reformats.
2. Always run sbt under Java 21: `export JAVA_HOME=…/temurin-21.jdk/…`.
3. Expect a 2nd pass for docstrings — first pass formats code; second pass
   normalises comments.
4. Avoid `/*` substrings inside Scala 3 docstrings — the parser flags them
   as nested comments.

Memory: `scalafmt_drift_partial_format`.

## Mermaid parser pitfalls

Two gotchas (seen historically):

- **Unicode in node labels** (`∞`, `①`, `−` U+2212) trips mermaid v10+. Use
  `+inf`, `1.`, plain `-` instead.
- **Chained labelled edges** on one line (`A -->|"label"| B --> C`) can
  fail when combined with unicode. Split into two lines:
  `A -->|"label"| B` then `B --> C`.

If a mermaid render error surfaces in the browser, check these first.

## The migration recipe (per chapter)

### Step 1 — Survey

```bash
# Source structure
ls /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<topic>/<chapter>/

# Which source lessons have interactive diagrams
grep -l "// Interactive Diagram" \
  /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<topic>/<chapter>/*.md

# Destination outline
grep -n "^# \|^## " content/cortex/data-structures-and-algorithms/<dest-path>/<chapter>.md
```

State what you found in a short bullet list. **Don't start editing until you
have a clear picture of the chapter shape.**

### Step 2 — Per problem on the chapter

For each problem (intro/concept sections don't have code — skip the code
parts for those):

1. **Read the source lesson** containing this problem. Note source's
   solution code shape, helper methods, variable names, inline comments.
2. **Read the destination problem section.** Note existing Intuition /
   Diagnostic / Dry-Run / Complexity / Edge-Cases prose. Don't touch those
   unless they directly cite stale example data.
3. **Diff each slot per the Authority matrix.** Decide what changes.
4. **Apply edits in this order:**
   - Problem statement (if source diverges)
   - Examples block (if source diverges — also update any Dry-Run example
     data that referenced old examples)
   - Solution code — Pseudocode + Python + Java + C + Scala. Match source's
     structure verbatim. **Use `Edit` tool, not `Write`.** Don't lose the
     destination's runnable test harness (Python `print(...)`, Java `main`,
     C `int main(...)`, Scala `def main(...)`).
   - Trace block (collapsed `<details>` summaries) — update example data
     references if Examples changed.
   - Edge Cases table — only if return shape changed.
5. **Verify the Python tab runs.** Quick `python3 /tmp/check.py` stub that
   calls the function on each example and compares to source's expected
   output. Catch sign errors / off-by-one before committing.
6. **Commit this problem.** One commit per problem.

### Step 3 — Interactive diagrams

For every `// Interactive Diagram (N frames): <description>` in source:

1. **Check the conversion manifest** at `docs/migration/conversion-manifest.json`
   — it lists the widget name + payload skeleton for this diagram. If
   present, follow it.
2. **If the widget exists in the catalog**, pick it; emit the widget
   instance in destination, replacing whatever placeholder (mermaid / d2 /
   nothing) was there before.
3. **Compress frames.** Source often has 20–40 frames per diagram; compress
   to 5–10 meaningful steps. Don't lose pedagogical milestones; do drop
   redundant intermediate frames.
4. **If the widget doesn't exist yet**, halt content work — raise an ADR +
   build the widget first as a precursor session (Arc 1).

### Static diagrams

Source's single-image `// Diagram:` markers (not "Interactive Diagram (N
frames)") map to **one static Mermaid or D2 block** in destination. Leave
them alone if destination already has a good static diagram for that
concept.

## Concurrency model — parallel agent ownership

When multiple agents run concurrently (Arc 1 widget builds, Arc 3 chapter
migrations):

- **Per-chapter ownership.** Each Arc 3 agent claims exactly one
  destination `.md` file. No overlap.
- **Per-widget ownership.** Each Arc 1 agent claims exactly one widget
  (one Scala module, one ADR, one CSS file, one demo chapter). No overlap.
- **Shared file: `D3WidgetBlock.scala`.** All Arc 1 agents append their
  dispatcher case to this file. Use `Edit` with replace-all-false; merge
  conflicts resolved by single batch-merge agent at end of batch.
- **Shared file: `package.json`.** No widget should require new npm deps;
  the existing `d3@^7.9.0` is the only allowed runtime dep for widgets.
- **Manifest as coordinator.** Arc 3 agents read
  `docs/migration/conversion-manifest.json` to find their chapter's widget
  payload skeletons. No re-discovery; manifest is the source of truth.

## When you finish a session

End every session with:

### 1. Status summary (3-6 bullets)

- What landed this session (commit hashes + 1-line each)
- What's in progress (file paths + state)
- Recommended next chunk for the following session
- Anything surprising that future-you should know

### 2. Next-session prompt (paste-ready)

Wrap in a fenced markdown block so the user can copy-paste:

```markdown
You are continuing the DSA migration on the codefolio repo.

**Read first:**
1. `docs/migration/methodology.md` — standing methodology brief
2. `docs/migration/diagram-gap-audit.md` — scope + per-phase backlog
3. `docs/migration/conversion-manifest.json` — per-diagram mapping

## Session task — Arc <N>, Session <M>

<Specific task. Don't say "continue Arc 1" — say "build the `hash-table`
widget end-to-end per spec at `docs/migration/widget-specs/hash-table.md`".>

**Constraints:** one commit per widget / problem / concept-pair; no push;
no Co-Authored-By; verify Python tab; export JAVA_HOME for Scala.

**End of session:** write a 3-6 bullet summary AND a paste-ready
next-session prompt per the methodology brief.
```

The user pastes the next-session prompt verbatim. If it needs editing, you
wrote it wrong.

## Out of scope (don't get drawn into these)

- **Rewriting prose for style.** Destination's prose is good enough. Only
  edit prose if it references stale example data or the Authority matrix
  says source wins.
- **Building widgets *during* a chapter-alignment session.** Widget builds
  are their own dedicated sessions at the start of Arc 1. Don't invent a
  widget mid-conversion.
- **Touching the build, dependencies, ADRs unrelated to widgets, or
  pipeline code.** That's a different kind of task.
- **Pushing to the remote.** Hard rule — explicit per-round authorization
  required.

## Key files (read at session start)

- `client/src/main/scala/codefolio/client/components/cortex/D3WidgetBlock.scala` — widget dispatcher
- `client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala` — exemplar widget #1 (1,552 LOC)
- `client/src/main/scala/codefolio/client/components/cortex/widgets/ArrayTraversal.scala` — exemplar widget #2 (784 LOC)
- `client/src/main/scala/codefolio/client/components/cortex/widgets/Stepper.scala` — shared step controller
- `client/src/main/scala/codefolio/client/components/cortex/widgets/PayloadDecoder.scala` — type-safe JSON parser
- `client/src/markdown/render.ts` — markdown→placeholder pipeline (line ~674-695 for d3 fence)
- `CLAUDE.md` memory files: `dsa_align_source_code_verbatim`, `dsa_book_language_tabs`, `scalafmt_drift_partial_format`, `sole_author`, `commit_freely_dont_push_until_told`
