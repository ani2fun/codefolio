# DSA Curriculum — Editorial Enrichment Pass (next session)

> Handoff prompt for the next agent picking up the DSA rebuild on `redesign/codefolio-design-system`. Self-contained: read this top to bottom, then ask the user the three open-decision questions before touching content.

## Context — what's already done

The DSA Cortex book at `content/cortex/data-structures-and-algorithms/` was rebuilt across two prior sessions on the **`redesign/codefolio-design-system`** branch:

1. **Structural rebuild.** 2 sections → 11 modules. 45 brand-new chapters covering Foundations, Trees additions (trie / AVL / RB / B-tree / segment / Fenwick / DSU), Graphs fill-ins (MST / SCC / bridges / 2-SAT), Greedy / D&C / Randomized, the entire Strings module (8 chapters), Probabilistic & Advanced (6), Concurrency & Systems (5), and DSA in Real Systems synthesis (6). Squashed into commit `7376ac7` then continued.

2. **Memorize rollout.** All 46 new chapters now have a `Memorize` section between Practice Ladder and Cross-Links. Format: collapsible `<details>`/`<summary>` Q/A pairs, fenced Python "Code Template" (or "Source Pointers" for the synthesis chapters), bulleted "When you see X → reach for Y" Pattern Triggers. CSS for collapsibility + the project-wide blockquote fix lives in `client/src/styles/components/chapter-content.css`.

3. **Tab cleanup.** All 200+ chapters use only 5 runnable code tabs: **Pseudocode + Python + Java + Scala + C**. C++/TypeScript/Go/Rust were stripped from the older chapters in a single mechanical pass. Project memory at `memory/dsa_book_language_tabs.md` records this.

## Context — what's NOT done (this session's scope)

**Editorial enrichment of the pre-rebuild chapters.** ~150 older chapters were copied through the reorg with the language-tab cleanup but **don't yet have the rebuild's quality bar** (Edge Cases & Pitfalls / Production Reality / Practice Ladder / Cross-Links / Memorize).

Affected chapters:

```
02-linear-structures/
  01-arrays/                  (intro + 11 pattern/design chapters)
  03-singly-linked-list/      (13 chapters)
  04-doubly-linked-list/      (9 chapters)
  05-stack/                   (12 chapters)
  06-queue/                   (4 chapters)
  07-hash-table/              (11 chapters)

03-trees/
  01-binary-tree/             (intro + 17 pattern chapters)
  02-binary-search-tree/      (13 chapters)
  03-heap/                    (intro DONE; 5 pattern/design chapters remaining)

04-graphs/                    (existing 16 chapters; intro/representations have content but lack the rebuild's tail sections)

05-algorithms-by-strategy/
  01-recursion/               (8 chapters)
  04-backtracking/            (4 chapters)
  05-dynamic-programming/     (~20 chapters)

06-sorting-and-searching/
  01-sorting/                 (12 chapters)
  02-searching/               (11 chapters)

08-bit-tricks/                (6 pattern chapters)

01-pseudocode-guide.md        (one short chapter; arguably already complete)
```

Plus one trivial cleanup:
- **Filename typo:** `05-algorithms-by-strategy/05-dynamic-programming/12-optimal-stratergy.md` → `12-optimal-strategy.md`. Fix slug references in the dynamic-programming index.

## Reference templates (the shape to copy)

Three calibration chapters establish the tone, density, and section order. Read these before writing any new content:

- **Structure intro:** `03-trees/03-heap/01-introduction-to-heaps.md`
- **Foundations / concept:** `01-foundations/01-asymptotic-analysis.md`
- **Algorithm:** `07-strings/02-kmp.md`

Each has the full quality bar in this order:
1. **Hook** (existing — keep)
2. ToC (existing — keep)
3. Body sections (existing)
4. **Edge cases & pitfalls** ← NEW for older chapters
5. **Production reality** ← NEW
6. **Practice ladder** (with hints, not solutions) ← NEW
7. **Memorize** (collapsible Q/A + Code Template + Pattern Triggers) ← NEW
8. **Cross-links** (existing or new)
9. **Final takeaway** (existing or new)

The Memorize template in detail is documented in `client/src/styles/components/chapter-content.css` (the CSS comment) and demonstrated in the three calibration chapters.

## Recommended approach for next session

### Phase 1 — Pick a scope

Editorial enrichment of ~150 chapters at the calibrated quality bar is **multi-week work**. Don't try to do all of it in one session. Recommended scopes:

**Option A — Topic intros only (~20 chapters).** Apply the full quality bar to every "Introduction to X" chapter in the older modules. Pattern chapters remain untouched. ~1-2 sessions.

**Option B — One module end-to-end.** Pick the highest-traffic module (probably **Linear Structures** or **Sorting**) and bring every chapter to the quality bar. ~1 session per module.

**Option C — Pattern chapters only.** They're shorter; the editorial additions (Memorize especially) are higher value per word. ~1 session for ~20-30 chapters.

I'd recommend **Option A** (topic intros only) as the next step. It's the highest-leverage subset because topic intros are most-trafficked.

### Phase 2 — Calibrate on 1 chapter

Before bulk rollout, pick one older chapter (e.g., `02-linear-structures/01-arrays/01-introduction.md`) and add the four new sections (Edge Cases / Production Reality / Practice Ladder / Memorize). **Stop and present for review** before proceeding — same calibration loop as the previous two sessions.

### Phase 3 — Module-by-module commits

After calibration approval, do one module per commit, following the same pattern the rebuild used:

```
git commit -m "DSA editorial enrichment: <module> topic intros"
```

### Open decisions to resolve before starting

1. **Scope** (A vs B vs C above). Default to A.
2. **Pattern-chapter Memorize sections** — they're often short. Maybe 4–6 Q/As per pattern chapter (vs 8–10 for topic intros)?
3. **Existing rich chapters** — some older chapters (`07-hash-table/01-introduction-to-hash-tables.md`, `01-arrays/01-introduction.md`) are already 800+ lines and very strong. They might only need Memorize + minor Edge Cases additions, not full rewrites.

Ask the user these three questions before starting.

## Other deferred items (lower priority, surface only if asked)

- **`12-optimal-stratergy.md` typo fix.** Trivial; can be done as a one-off commit.
- **Older chapters lack frontmatter** (`title:` / `summary:` YAML). The rebuild chapters all carry frontmatter; older ones don't. Consider adding during the editorial pass.
- **No-Memorize stubs in indexes.** Some module indexes still link to "stub" chapters that have since been written. Re-grep `*stub*` markers and clean up.
- **`MEMORY.md` audit.** The user added typography polish to `chapter-content.css` (serif h2, sans h3). If a memory file should record their typography token system, ask before writing.

## Workflow rules (reminder)

- Sole-author repo: **NO `Co-Authored-By:` trailers** in commits.
- 5-tab standard: Pseudocode + Python + Java + Scala + C. Don't reintroduce C++/TS/Go/Rust.
- Use `Edit` for surgical changes, `Write` only for new files or full rewrites.
- After each module commit, post a brief progress report and stop. Don't barrel through 5 modules in silence.

## Branch state at handoff

```
redesign/codefolio-design-system
├── 7376ac7  DSA senior-engineer curriculum buildout (squashed rebuild)
├── 407651c  DSA: add Memorize section calibration on 3 chapters
├── e7ea9a5  DSA Memorize: collapsible Q/A, bulleted triggers, blockquote fix
├── 3ea91ab  DSA Memorize: Foundations module (4 chapters)
├── fd64430  DSA Memorize: Trees module (8 new topic chapters)
├── 4429e88  DSA Memorize: Graphs module (4 fill-in chapters)
├── 70d9c5d  DSA Memorize: Algorithms by Strategy module (3 chapters)
├── 1eae48e  DSA Memorize: Strings module (7 chapters)
├── fd949c5  DSA Memorize: Probabilistic and Advanced (6 chapters)
├── 3fc0005  DSA Memorize: Concurrency and Systems (5 chapters)
└── d4105d6  DSA Memorize: DSA in Real Systems (6 chapters)
```

`./bin/dev` should render every chapter cleanly. The Memorize section appears between Practice Ladder and Cross-Links in all 46 new chapters; click any Q to expand.

---

**Begin by reading the three calibration chapters listed above, then ask the user the three open-decision questions before touching a single chapter.**
