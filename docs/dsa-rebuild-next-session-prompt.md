# DSA Editorial Enrichment — Next Session Prompt

> Continuation prompt for the next agent. Branch: `redesign/codefolio-design-system`. Full context in [`docs/dsa-rebuild-handoff.md`](dsa-rebuild-handoff.md).

## Directive

Proceed with **Option A** from the handoff doc: editorial enrichment of older topic-intro chapters. Apply the calibrated quality bar (Edge Cases & Pitfalls / Production Reality / Practice Ladder / Memorize / Cross-Links) to each.

### Constraints

- **One chapter per commit.** Don't batch into module commits — granular history is required.
- **Don't skip any chapter** in the scope below. Every one gets the treatment, including chapters that already look strong (they still need Memorize at minimum).
- **Resume gracefully across sessions.** After each commit, post a one-line progress note (`Editorial enrichment: X of N done, next: Y`) and continue. Stop only when the user pauses or the session naturally ends.

## Scope — ~16 chapters in order

Process in this order so highest-traffic chapters land first:

1. `02-linear-structures/01-arrays/01-introduction.md`
2. `02-linear-structures/07-hash-table/01-introduction-to-hash-tables.md`
3. `02-linear-structures/05-stack/01-introduction-to-stacks.md`
4. `02-linear-structures/06-queue/01-introduction-to-queues.md`
5. `02-linear-structures/03-singly-linked-list/01-introduction-to-singly-linked-lists.md`
6. `02-linear-structures/04-doubly-linked-list/01-introduction-to-doubly-linked-lists.md`
7. `03-trees/01-binary-tree/01-introduction-to-binary-trees.md`
8. `03-trees/02-binary-search-tree/01-introduction-to-binary-search-trees.md`
9. `04-graphs/01-introduction-to-graphs.md`
10. `05-algorithms-by-strategy/01-recursion/01-introduction-to-memory-model.md`
11. `05-algorithms-by-strategy/04-backtracking/01-introduction-to-backtracking.md`
12. `05-algorithms-by-strategy/05-dynamic-programming/01-linear-dp.md`
13. `06-sorting-and-searching/01-sorting/01-introduction-to-sorting.md`
14. `06-sorting-and-searching/02-searching/01-binary-search.md`
15. `08-bit-tricks/01-pattern-kth-bit.md` *(entry-point chapter; bit-tricks has no separate intro)*

> **Verify exact filenames before starting.** Some may differ slightly. Run `ls content/cortex/data-structures-and-algorithms/<topic-dir>/` against each topic to confirm.

## Per-chapter procedure

For each chapter, in order:

1. **Read the chapter** end-to-end. Understand the existing hook, body, what's strong, what's missing.
2. **Add the four new sections** between the chapter's existing body and any Cross-Links / Final Takeaway:
   - `# Edge Cases and Pitfalls` — bulleted list, ~6-10 entries. Specific failure modes engineers hit.
   - `# Production Reality` — bulleted list, ~5-8 entries. Real systems where this lives, with file paths or product names.
   - `# Practice Ladder` — 5 problems, easy → interview-grade, with **hints not solutions**.
   - `# Memorize` — collapsible Q/A (~8-10 atomic cards) + Code Template + bulleted Pattern Triggers.
3. **Surround each new section with `***`** separators, matching the existing chapter style.
4. **If the chapter lacks a Cross-Links section, add one** linking to prerequisites and successors.
5. **If the chapter lacks YAML frontmatter, add it** (`title:`, `summary:`, optional `prereqs:`).
6. **Commit the single chapter** with a message like `DSA editorial enrichment: <chapter slug>`.
7. **Post a one-line progress update.** Example: `Editorial enrichment: 3 of 16 done. Next: linked-list intro.`
8. **Continue to the next chapter unless the user pauses.**

## Calibration — the first chapter

**Stop after chapter 1 (arrays intro)** and present for review. Same calibration loop the rebuild used. The user will signal "voice approved" before you proceed to chapter 2.

After calibration is approved, no further stops within this session — proceed sequentially through the list.

## Reference templates

Copy the section shape verbatim from the three calibration chapters:

- `03-trees/03-heap/01-introduction-to-heaps.md` — structure intro template
- `01-foundations/01-asymptotic-analysis.md` — concept / foundations template
- `07-strings/02-kmp.md` — algorithm template

The Memorize sub-block format (collapsible `<details>`, fenced Python code template, bulleted triggers) is documented in CSS comments at `client/src/styles/components/chapter-content.css`.

## Resume protocol (across sessions)

To check progress:

```bash
git log --oneline | grep "editorial enrichment"
```

To find the next unprocessed chapter, walk the scope list and check whether each has a `# Memorize` section:

```bash
grep -l "^# Memorize" content/cortex/data-structures-and-algorithms/<chapter-path>
```

If grep returns nothing for a chapter, it still needs the treatment. Pick up from the first one missing.

## Workflow rules (reminder)

- Sole-author repo: **NO `Co-Authored-By:` trailers** in commits.
- 5-tab standard for any new code blocks: Pseudocode + Python + Java + Scala + C. Don't reintroduce C++/TS/Go/Rust.
- Use `Edit` for surgical insertions; **never `Write` an existing chapter** (would clobber the existing content).
- Commit per chapter. Don't batch into module commits.
- Don't push without explicit instruction.

## Out of scope (explicitly defer to a later session)

These remain as future work and are tracked in [`docs/dsa-rebuild-handoff.md`](dsa-rebuild-handoff.md):

- Pattern chapters (~80 of them across the older modules).
- The `12-optimal-stratergy.md` typo fix.
- Frontmatter-only updates without editorial enrichment.

---

**Begin by reading the three calibration chapters listed above, then start chapter 1 (arrays intro). Stop after that one for the user's read.**
