# DSA migration — session prompt

Paste this at the start of every DSA-migration session, replacing the
**Session task** block at the top with what you want done this turn.

The full text below is **the standing brief**. Read everything before
touching files. The Arrays section under
`content/cortex/data-structures-and-algorithms/02-linear-structures/01-arrays/`
is the **gold standard** — when in doubt, mimic how that section looks.

---

## Session task

> _**Fill this in per session.**_ Examples:
>
> - "Continue Phase 1. Do Ch 1.3 Insertion — align all problem code to source verbatim, then add D3 widgets for the 5 source interactive diagrams. Commit per problem."
> - "Run a code-only pass across all of Phase 1 (chapters 1.2-1.13). Skip widgets and prose; surface any code that diverges from source's helper-method decomposition and align it. One commit per chapter."
> - "Phase 2 — singly→doubly linked list. Start with chapters 2.1 and 2.2 (Intro + Traversal). Code + interactive diagrams. Commit per problem."

---

## Background

There is an authoritative raw lesson source at
`/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/`,
split into:

- `data-structure/` — `1.arrays`, `2.singly-linked-list`, …, `10.graph`
- `algorithms/` — `1.recursion`, …, `6.bit-manipulation`

Each leaf is a chapter directory holding 3-15 lesson `.md` files plus
`*.assets/` image folders. **Lesson Source** is the canonical term — see
`CONTEXT.md`.

Destination lives at
`/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/`,
already structured to mirror the source one-to-one. Each destination
chapter is a single `.md` file collecting all of source's lessons in
that chapter into one page.

## Phase plan

| Phase | Source | Destination |
|---|---|---|
| 1 | `data-structure/2.singly-linked-list` | `02-linear-structures/03-singly-linked-list` |
| 2 | `data-structure/3.doubly-linked-list` | `02-linear-structures/04-doubly-linked-list` |
| 3 | `data-structure/4.hash-table` | `02-linear-structures/07-hash-table` |
| 4 | `data-structure/5.stack` | `02-linear-structures/05-stack` |
| 5 | `data-structure/6.queue` | `02-linear-structures/06-queue` |
| 6 | `data-structure/7.binary-tree` | `03-trees/01-binary-tree` |
| 7 | `data-structure/8.binary-search-tree` | `03-trees/02-binary-search-tree` |
| 8 | `data-structure/9.heap` | `03-trees/03-heap` |
| 9 | `data-structure/10.graph` | `04-graphs` |
| 10 | `algorithms/1.recursion` | `05-algorithms-by-strategy/01-recursion` |
| 11 | `algorithms/2.backtracking` | `05-algorithms-by-strategy/04-backtracking` |
| 12 | `algorithms/3.sorting` | `06-sorting-and-searching/01-sorting` |
| 13 | `algorithms/4.searching` | `06-sorting-and-searching/02-searching` |
| 14 | `algorithms/5.dynamic-programming` | `05-algorithms-by-strategy/05-dynamic-programming` |
| 15 | `algorithms/6.bit-manipulation` | `08-bit-tricks` |

Phase 0 — Arrays — is **done**. Phase 1 has begun (Ch 1.1 Boundary
Node + Ch 1.2 Node Search + Length of the List code-aligned as of
commits `2b50bd2`, `8ccc477`, `b1194d1`).

---

## Hard rules (non-negotiable)

1. **No `Co-Authored-By:` trailers** on commits. Solo author.
   See `memory/sole_author.md`.
2. **Commit freely locally; never `git push`** unless the user
   explicitly says "push it". See `memory/commit_freely_dont_push_until_told.md`.
3. **Never modify** the source under
   `/Users/aniket/Development/others/tutorial_dsa/`. Read-only.
4. **Five language tabs only** — Pseudocode + Python + Java + C + Scala,
   in that file order. No C++, TypeScript, Go, Rust.
   See `memory/dsa_book_language_tabs.md`.
5. **scalafmt rules** — when touching `.scala` (rare in content work,
   common when extending widgets): scope per-feature with
   `scalafmtOnly`; run sbt under Java 21
   (`export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home`).
   See `memory/scalafmt_drift_partial_format.md`.
6. **Don't add `Co-Authored-By:` trailers** (worth repeating — habit
   from upstream Claude Code defaults trips this up).

---

## Read these before editing

These two are **the methodology**. Anything that contradicts them is
wrong, no matter how clever it looks:

- `CONTEXT.md` — glossary entries **Lesson Source** and
  **Verbatim Code Alignment**.
- `memory/dsa_align_source_code_verbatim.md` — the feedback memory
  that captures past drift incidents (Target Limited Two Sum,
  Four Sum) and how to avoid them.

Useful reference, secondary priority:

- `docs/adr/0006-d3-widgets-as-a-closed-catalog.md` — widget catalog
  rules (named catalog, JSON payload, schema is widget-local).
- `docs/adr/0013-d3-for-widget-animation.md` — why D3, animation
  pattern, bundle budget.
- `memory/MEMORY.md` — index of everything; always loaded.

---

## Authority matrix (source vs destination)

The **Lesson Source** is authoritative for these slots in the
destination chapter:

| Slot | Source authority |
|---|---|
| Problem statement | **Source wins** — port verbatim (de-escape backslashes, fix the indentation) |
| Examples (I/O pairs) | **Source wins** — use source's exact arrays + outputs |
| Solution code in each of the 5 tabs | **Source wins** — see Verbatim Code Alignment below |
| Inline code comments | **Source wins** — translate idiomatically per language but preserve wording |
| Method decomposition | **Source wins** — if source extracts `skipDuplicatesLeft`, `closestTwoSum`, etc., destination must too |
| Function naming convention | **Source wins** — Python `snake_case`, Java/Scala `camelCase`, C `snake_case` |

The **destination's existing prose** is authoritative for these slots:

| Slot | Why destination wins |
|---|---|
| Intuition | Destination's narrative is genuinely stronger than source's terse explanations |
| Diagnostic Questions / Applying-the-Q sections | Pedagogically valuable; keep |
| Approach prose | Keep |
| Dry Run text | Keep (but the example *data* should match source's first example) |
| Complexity Analysis | Keep |
| Edge Cases tables | Keep |
| Key Takeaway / Final Takeaway | Keep |
| Section structure (h1/h2 ordering) | Keep |
| 5-language tab order | Keep (Pseudocode + Python + Java + C + Scala) |
| Diagram conventions (theme, colours, fence syntax) | Keep |

When in doubt: **read both the source lesson and the destination
chapter, then diff each section** before editing. Don't paraphrase.

---

## Verbatim Code Alignment — exact rules

The single most common failure mode is "I implemented the same
algorithm but with cleaner variable names". This is **drift** and the
user has flagged it twice already (Target Limited Two Sum,
Four Sum). Apply these rules to every problem's Solution code:

1. **Method decomposition.** If source has
   `skipDuplicatesLeft` / `skipDuplicatesRight` / `duplicateAwareTwoSum`
   helper methods (and a `fourSum` / `threeSum` orchestrator), the
   destination must have them too — all 5 tabs.
2. **Variable names match source.** `sum` (not `total`),
   `remainingTarget` (not `need`), `maxSum` (not `max_total`),
   `count_ones` (not `ones`), `min_rooms` (not `peak`). Match the
   source's casing per language (`sum_` in Python where `sum`
   shadows the builtin).
3. **Inline comments verbatim.** "Sort the array in non-decreasing
   order" / "Use a while loop to traverse the array using the two
   pointers" / "Move the left pointer to increase the sum" —
   translate idiomatically across languages but preserve the
   wording. Don't condense.
4. **Per-language naming convention.** Python uses `snake_case`
   (`four_sum`, `skip_duplicates_left`). Java/Scala use `camelCase`
   (`fourSum`, `skipDuplicatesLeft`). C uses `snake_case`.
5. **Pseudocode reflects the decomposition.** If source splits into
   helpers, the Pseudocode tab must too — multiple `function …`
   blocks, not one fat function.
6. **Sibling consistency.** After updating one problem on a Chapter,
   grep the rest of the Chapter for the same identifier patterns. If
   Three Sum uses `skipDuplicatesLeft`, Four Sum must too. Mixed
   styles on the same page are a code smell.

---

## The migration recipe (per chapter)

### Step 1 — Survey

```bash
# Source structure
ls /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<topic>/<phase>/
# Which source lessons have interactive diagrams
grep -l "// Interactive Diagram" /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<topic>/<phase>/*/*.md
# Destination outline
grep -n "^# \|^## " /Users/aniket/Development/homelab/codefolio/content/.../<chapter>.md
```

State what you found in a short bullet list. Don't start editing until
you have a clear picture of the chapter shape.

### Step 2 — Per problem on the chapter

For each problem (Intro/Concept sections don't have code — skip the
code parts for those):

1. **Read the source lesson** containing this problem.
   Note: source's solution code shape, helper methods, variable
   names, inline comments.
2. **Read the destination problem section**. Note: existing
   Intuition/Diagnostic/Dry-Run/Complexity/Edge-Cases prose. Don't
   touch those unless they directly cite stale example data.
3. **Diff each slot per the Authority matrix.** Decide what changes.
4. **Apply edits in this order:**
   - Problem statement (if source diverges)
   - Examples block (if source diverges — also update any Dry-Run
     example data that referenced old examples)
   - Solution code — Pseudocode + Python + Java + C + Scala. Match
     source's structure verbatim. **Use Edit tool, not Write.**
     Don't lose the destination's runnable test harness (Python
     `print(...)`, Java `main`, C `int main(...)`, Scala `def main(...)`).
   - Trace block (collapsed `<details>` summaries) — update example
     data references if Examples changed.
   - Edge Cases table — only if return shape changed (e.g.
     Peak Resource Requirement when it went from scalar to triple).
5. **Verify the Python tab runs.** Quick `python3 /tmp/check.py`
   stub that calls the function on each example and compares to
   source's expected output. Catch sign errors / off-by-one before
   committing.
6. **Commit this problem.** One commit per problem (precedent from
   ch 04 commits `e5b9bab`, `f00e55e`).

### Step 3 — Interactive diagrams

For every `// Interactive Diagram (N frames): <description>` in
source, decide:

- **Does the existing `array-traversal` widget fit?** It supports a
  single linear array with markers (`left`, `right`, `start`, `end`,
  `i`, `j`, …), an optional range band, and per-step `items` /
  `keys` for swap animations. Now also supports a **secondary row**
  (`secondaryItems`, `secondaryMarkers`, `secondaryRange`) for
  two-array scenarios — see commit `75793e0`.
- **Yes — single array with markers** → author an
  `array-traversal` widget instance with one step per logical event
  in source's frame sequence. Source often has 20-30 frames; compress
  to ~5-10 meaningful steps. Place it inside the relevant H2 section,
  usually right after the static d2/mermaid diagram or replacing it.
  Examples: ch 04 brute-force / two-pointer Two Sum widgets in
  commit `465e739`.
- **Yes — two arrays** → use the secondary-row extension. Examples:
  ch 05 rotate-via-temp (`7776a56`), ch 06 simultaneous traversal +
  subsequence checker (`cd5f9d1`), ch 09 interval merging
  (`6db8cc8`).
- **No** — the visualisation needs a 2D grid (ch 02 row-major /
  column-major) or an interval timeline. Flag in the commit message
  as deferred. Don't build a new widget type ad hoc; that warrants
  its own ADR + arc.

### Static diagrams

Source's single-image `// Diagram:` markers (not "Interactive
Diagram (N frames)") map to **one static Mermaid or D2 block** in
destination. Leave them alone if destination already has a good
static diagram for that concept.

### Mermaid parser pitfalls

Two gotchas seen in commit `398d2e8`:

- **Unicode in node labels** (`∞`, `①`, `−` U+2212) trip mermaid
  v10+. Use `+inf`, `1.`, plain `-` instead.
- **Chained labelled edges** on one line (`A -->|"label"| B --> C`)
  can fail when combined with the above. Split into two lines:
  `A -->|"label"| B` then `B --> C`.

If you hit a mermaid render error in the browser, check those two
issues first.

---

## Verification gates

Run these **after each commit**, not just at session end:

```bash
# scalafmt — only if you touched .scala (rare for content work)
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

For content-only changes (`content/cortex/.../*.md`), the build
checks aren't strictly required — but run them at chapter boundaries
to catch markdown-fence imbalances early.

### Browser smoke

```bash
# Server is usually already running under bin/dev (port 5173)
# Navigate to the chapter URL and verify:
# 1. Page loads without console errors
# 2. Mermaid blocks render with <svg>, not .mermaid__error
# 3. D3 widgets you added render with cells + markers
```

Use the `mcp__Claude_Preview__*` tools if available. The `preview_eval`
shortcuts for checking mermaid health are documented in earlier
commits' messages.

---

## Commit cadence

- **One commit per problem** for code-alignment work — matches the
  precedent set by Arrays alignment commits.
- **One commit per concept-section pair** if you're adding a widget
  to a non-problem concept walkthrough (e.g.
  "Understanding the Maximum Overlap Pattern").
- **One commit per chapter** if you're doing a comment-only sweep
  across an existing chapter where structure already matches source.

Commit message style (heredoc with `EOF`):

```bash
git commit -m "$(cat <<'EOF'
dsa: align <Problem Name> code (ch X.Y <Section>) to source's <pattern>

<2-3 sentences describing the specific divergence and the fix.
Mention which language tabs changed. Note if behaviour is verified
(e.g. "Python verified to produce all four expected outputs").>
EOF
)"
```

**Never** include `Co-Authored-By:` trailers. See hard rule 1.

---

## Pacing guidance

The Arrays section was migrated over many sessions because the work
is dense. **Don't try to finish a Phase in one session.** A
sustainable cadence is:

- **3-5 problem alignments per session** — leaves headroom for
  verification and pivoting if the user redirects mid-session.
- **1-3 widget conversions per session** — each requires reading
  source frames, picking representative steps, and writing the JSON
  payload.
- **Commit every 20-30 minutes of focused work** — never let a
  session end with uncommitted changes.

When the session is winding down or the user redirects:

1. Commit anything in progress (if at a clean stopping point).
2. Write a short status report listing what's done, what's
   in-progress, and the recommended next chunk for the following
   session.
3. Update `memory/MEMORY.md` if you discovered a new convention or
   pitfall worth capturing for future sessions.

---

## Recovering from a mid-session interruption

If the previous session left work in progress:

```bash
git status
git log --oneline -20
```

Look for recent commits with `dsa: align` or `dsa: add … widget`
prefixes — that's where you are. Read the most recent commit message
for context, then continue from there.

If `git status` shows uncommitted changes from the previous session,
**don't blindly commit them** — read the diff first, decide if it's
finished work that just wasn't committed, or partial work that needs
finishing.

---

## Out of scope (don't get drawn into these)

- **Rewriting prose for style.** Destination's prose is good enough.
  Only edit prose if it references stale example data or the
  Authority matrix says source wins.
- **Building new widget types.** Each new widget (2D grid, interval
  timeline, tree visualiser) is its own ADR + arc. Note them as
  deferred; don't sketch them in.
- **Touching the build, dependencies, ADRs, or pipeline code.**
  That's a different kind of task. Bring it up with the user if
  you think it's blocking content work.
- **Pushing to the remote.** See hard rule 2.

---

## Quick reference — useful greps

```bash
# Find every "Interactive Diagram" marker in a phase's source
grep -rn "// Interactive Diagram" \
  /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<phase-dir>/

# Count existing array-traversal widgets in destination
grep -c "d3 widget=array-traversal" \
  /Users/aniket/Development/homelab/codefolio/content/cortex/.../<chapter>.md

# Find a problem's solution in source (Python is usually the most
# readable reference)
grep -l "def <function_name>" \
  /Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/<phase-dir>/*/*.md

# Check for variable-name drift across sibling problems
grep -nE "(total|sum_|need|remaining_target)" \
  /Users/aniket/Development/homelab/codefolio/content/cortex/.../<chapter>.md
```

---

## Closing the session

End every session with a short summary message (3-6 bullets):

- **What landed this session** (commit hashes + 1-line each).
- **What's in progress** (file paths + state).
- **Recommended next chunk** for the following session.
- **Anything surprising** that future-you should know.

That's the entire methodology. Begin by reading the **Session task**
block at the top of this file (or the user's first message) and
proceed.
