# `linked-list` widget hard-rejects non-canonical marker names — closed vocabulary, fixed role colours

ADR-0014 introduced the `linked-list` widget with a loose marker schema: each marker carried `{name, nodeId, color?}` where `name` was free-form and `color` was optional with a palette-by-position fallback. The Phase 1 chapter audit (commit `97f25b0` notes, and the live console warnings the new widget emits at chapter load) showed exactly the drift that openness produces:

- The same role gets three names across three chapters: `head`, `previous`, `slow`/`fast`, `current`, `new head`, `previous=null` are all in use for *the trailing/leading/current pointer in a reversal*.
- The same role gets different colours in different chapters: `current` is sometimes emerald, sometimes amber, sometimes whatever the author typed into `color`.
- Marker names sometimes embed values (`previous=null`) or status (`new head`) — pedagogical context that belongs in the step caption, leaking into the role label.

The reader carries a mental model across the section. When the model has to bend per chapter ("oh, in this one the green pointer is `prev`, not `curr`"), the chapter section reads like five different widgets stitched together.

**Decision**: the widget's marker vocabulary is a closed set; the `color` field on payload markers is dropped at parse time; non-canonical names render as inline warning badges instead of being quietly accepted.

  - **The canon** (lives in `CanonicalMarkers` inside [`LinkedList.scala`](../../client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala)):

    | Name | Role | Colour |
    |---|---|---|
    | `head` | List entry | blue `#3b82f6` |
    | `tail` | Explicit last-node tracker | slate `#64748b` |
    | `prev` | Trailing pointer (reversal) | amber `#f59e0b` |
    | `curr` | Active pointer (reversal, traversal) | emerald `#10b981` |
    | `next` | Saved-next reference | violet `#a855f7` |
    | `slow` | Slow pointer (Floyd, two-pointer) | blue `#3b82f6` |
    | `fast` | Fast pointer (Floyd, two-pointer) | rose `#ef4444` |
    | `dummy` | Sentinel / dummy head | slate `#64748b` |
    | `start` | Segment start (reversal-of-segment) | amber `#f59e0b` |
    | `end` | Segment end (reversal-of-segment) | emerald `#10b981` |

    The node-style canon is `{new, removed, highlight}` — unknown values drop with a dev-mode `console.warn` and the node renders with its default look.

  - **Why hard-reject in the widget, not soft-warn in a linter.** A linter catches drift at PR time; the widget catches it the instant the author hits save under `bin/dev`. The badge — `⚠ name` rendered in rose with no triangle — is loud enough that the author can't ship a chapter with non-canonical markers without noticing. The dev-mode console also surfaces the canonical names so the fix is one rename away.

  - **Why role-bound colours, not author-chosen.** Same reason ADR-0006 closes the widget catalog: every choice the author makes is a choice the section's visual coherence has to absorb. A `head` pointer is always blue; a reader who's seen one chapter knows what `head` looks like in every other chapter. Author-side `color` overrides are the open-ended counterpart to free-form names — they produce the same drift, just in a different field. Drop both.

  - **Why this vocabulary, not a larger one.** Each name covers a *role* the algorithms book actually uses across the linked-list section, and every role uses one name (no aliases — `previous` is not `prev`, it's a typo for `prev`). Pedagogical context that previously crept into marker names (`previous=null`, `new head`, `stitched`) moves into the per-step `msg` caption, which is the thing the reader is meant to read for context anyway. Adding a new role name later is an ADR amendment: a real PR review pass forces the question "is this a new role or a synonym for one that already exists?"

  - **Where the values live.** All three vocabularies — pointer names, role colours, node styles — live in `CanonicalMarkers` and `CanonicalNodeStyles` constants in [`LinkedList.scala`](../../client/src/main/scala/codefolio/client/components/cortex/widgets/LinkedList.scala). Growing them means amending this ADR and adding a map entry. A build-time validator (separate workstream) re-checks every `d3 widget=linked-list` payload across `content/cortex/**/*.md` so CI catches drift authors miss locally.

The trade is **author flexibility on names and colours** for **a marker vocabulary the reader recognises everywhere** — and a permanent answer when a future chapter asks "what should I call this pointer?": the answer is in the table above or it's an ADR-amendment conversation, never an ad-hoc choice.
