# Widget Spec — `suffix-array-builder`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise the **construction of a suffix array** for a small string,
together with the derived **LCP (Longest Common Prefix) array**. The
widget renders a stacked, multi-row sorted-suffix view — one row per
suffix — and steps through the **prefix-doubling** construction (the
algorithm the destination chapter implements at education-friendly
complexity). Each step shows:

1. The current **rank-pair** under each suffix (the `(rank, rank-of-half-shifted)`
   tuple used for the doubling round's sort key).
2. The **sort permutation** changing per round, with rows reordering
   smoothly so the reader sees suffixes "bubble into place".
3. The final **LCP overlay** — a horizontal bar between adjacent sorted
   suffixes showing the shared-prefix length, computed via Kasai once
   `SA` stabilises.

The destination chapter's primary algorithmic content is prefix-doubling
(it mentions SA-IS / DC3 but uses Python's built-in sort for the worked
example). The widget mirrors that pedagogical choice: prefix-doubling
shown explicitly across 6-8 compressed steps, with naive-sort and SA-IS
deferred to non-default modes.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

Strings phase has no source counterpart. Payloads are authored from
destination prose at
`content/cortex/data-structures-and-algorithms/07-strings/06-suffix-array.md`
(suffix-list table lines 13-26, LCP table lines 71-80, Kasai
implementation lines 108-126).

## 3. Destination chapter usage

| Chapter | Instances |
|---|---|
| `07-strings/06-suffix-array.md` | 3 — (1) naive sort-the-suffixes for `"banana"`; (2) prefix-doubling construction for `"banana"`; (3) LCP overlay |
| `07-strings/07-suffix-automaton.md` (future, when authored) | 1 — comparison anchor |
| `07-strings/08-aho-corasick.md` (future) | reuse `trie` not this widget |
| `09-probabilistic-and-advanced/06-persistent-data-structures.md` (orphan) | possible reuse for "persistent suffix array" mention |

Total at landing: **3 instances** in the suffix-array chapter.

## 4. Payload schema sketch

```ts
{
  title:    string,
  text:     string,                 // the source string S; widget appends "$" sentinel internally if showSentinel=true
  showSentinel?: boolean,           // default true — render trailing $
  mode:     "naive" | "prefix-doubling" | "kasai-lcp",
                                    // selects which animation plays
  steps: Array<{
    round?:   number,               // 0, 1, 2, … in prefix-doubling (compare-by-1, by-2, by-4, …)
    order:    number[],             // current permutation: order[i] is suffix start index at sorted position i
    rankPair: Array<[number, number]>,
                                    // for each suffix (indexed by start), its current (rank, rank-of-half-shifted) tuple
                                    // — drives the next round's sort
    lcp?:     number[],             // populated only after construction finishes (Kasai output)
    highlight?: {                   // optional per-step focus
      compareA?: number,            // suffix start index A being compared
      compareB?: number,            // suffix start index B being compared
      lcpBetween?: { i: number },   // index into order for which LCP bar to highlight
    },
    msg:      string,
  }>,
}
```

**Row layout.** Each suffix is rendered as a fixed-width strip showing
the suffix's characters, prefixed by its start index. Rows reorder
between steps via D3-join transitions keyed by start index (so a suffix
moving from row 2 to row 5 slides downward smoothly, not flicker-rebuilt).

**Rank-pair gutters.** A small gutter to the right of each row shows the
current `(r_i, r_{i + k})` tuple — the sort key that round. As the round
advances (`k` doubles), the gutter updates and the rows reorder.

**LCP bars.** Between adjacent sorted suffix rows, a horizontal blue bar
of length proportional to `LCP[i]` (in number of chars) is drawn. The
bar visually overlaps the matched-prefix characters of both rows so the
reader sees exactly which characters are shared.

## 5. POC payloads — `"banana"` worked example

The destination chapter uses `"banana"` as the canonical worked example
(`SA = [5, 3, 1, 0, 4, 2]`, `LCP = [_, 1, 3, 0, 0, 2]`). The
prefix-doubling construction reaches the final order in `⌈log₂ 6⌉ = 3`
rounds, so the 6-8 step budget cleanly fits an init + 3 rounds + LCP
phase.

### POC-1 — Naive sort: all suffixes listed, then sorted

````markdown
```d3 widget=suffix-array-builder
{
  "title": "Naive: list all suffixes, sort lexicographically",
  "text": "banana",
  "showSentinel": false,
  "mode": "naive",
  "steps": [
    {
      "order": [0, 1, 2, 3, 4, 5],
      "rankPair": [[0,0], [0,0], [0,0], [0,0], [0,0], [0,0]],
      "msg": "Step 0: list suffixes in start-index order. 'banana', 'anana', 'nana', 'ana', 'na', 'a'."
    },
    {
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[0,0], [0,0], [0,0], [0,0], [0,0], [0,0]],
      "msg": "After sort: 'a' < 'ana' < 'anana' < 'banana' < 'na' < 'nana'. SA = [5, 3, 1, 0, 4, 2]."
    }
  ]
}
```
````

### POC-2 — Prefix-doubling construction (8 steps: init + 3 rounds compressed + finalise)

````markdown
```d3 widget=suffix-array-builder
{
  "title": "Prefix-doubling construction — sort by 1, then 2, then 4 chars",
  "text": "banana",
  "showSentinel": false,
  "mode": "prefix-doubling",
  "steps": [
    {
      "round": 0,
      "order": [0, 1, 2, 3, 4, 5],
      "rankPair": [[1,0], [0,0], [2,0], [0,0], [2,0], [0,0]],
      "msg": "Round 0: rank by 1st char only. 'a'=0, 'b'=1, 'n'=2. Three distinct ranks."
    },
    {
      "round": 0,
      "order": [1, 3, 5, 0, 2, 4],
      "rankPair": [[1,0], [0,0], [2,0], [0,0], [2,0], [0,0]],
      "msg": "Sort by 1st-char rank. Ties: {1, 3, 5} all 'a'; {2, 4} both 'n'; {0} 'b'. Internal order arbitrary for now."
    },
    {
      "round": 1,
      "order": [1, 3, 5, 0, 2, 4],
      "rankPair": [[1,2], [0,2], [2,0], [0,2], [2,0], [0,-1]],
      "msg": "Round 1: extend key by 2nd char. Pair = (rank_i, rank_{i+1}). Suffix 5 has no i+1 → rank -1 (sentinel)."
    },
    {
      "round": 1,
      "order": [5, 1, 3, 0, 2, 4],
      "rankPair": [[3,4], [1,4], [4,0], [1,3], [4,0], [0,-1]],
      "msg": "Sort by pair, derive new ranks. Suffix 5 ('a' alone) → rank 0; suffix 3 ('ana') → 1; etc. Five distinct ranks already."
    },
    {
      "round": 2,
      "order": [5, 1, 3, 0, 2, 4],
      "rankPair": [[3,2], [1,4], [4,-1], [1,-1], [4,-1], [0,-1]],
      "msg": "Round 2: extend by 4 chars. Pair = (rank_i, rank_{i+2}). Most i+2 land past 6 → -1 sentinel."
    },
    {
      "round": 2,
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "msg": "Sort by pair. All ranks distinct now → SA stabilises. Final order: [5, 3, 1, 0, 4, 2]."
    },
    {
      "round": 3,
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "msg": "All n ranks distinct — algorithm terminates. SA = [5, 3, 1, 0, 4, 2]. Total rounds: ⌈log₂ 6⌉ = 3."
    },
    {
      "round": 3,
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "lcp": [0, 1, 3, 0, 0, 2],
      "highlight": { "lcpBetween": { "i": 2 } },
      "msg": "Kasai: LCP = [_, 1, 3, 0, 0, 2]. Highlight: LCP[2]=3 means 'ana' and 'anana' share prefix 'ana' (3 chars)."
    }
  ]
}
```
````

### POC-3 — Kasai LCP build animation over a stabilised SA

(Smaller-scope demo: SA already known; step through Kasai's k-pointer
descent on each suffix. Reuses the post-build state of POC-2 but
focuses the overlay on LCP computation.)

````markdown
```d3 widget=suffix-array-builder
{
  "title": "Kasai's O(n) LCP — walk suffixes in original order, reuse k",
  "text": "banana",
  "showSentinel": false,
  "mode": "kasai-lcp",
  "steps": [
    {
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "lcp": [0, 0, 0, 0, 0, 0],
      "highlight": { "compareA": 0, "compareB": 3 },
      "msg": "i=0 ('banana'). rank[0]=3. Prev suffix in SA is 1 ('anana'). Compare 'banana' vs 'anana' — 0 chars match. LCP[3]=0."
    },
    {
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "lcp": [0, 0, 0, 0, 0, 0],
      "highlight": { "compareA": 1, "compareB": 3 },
      "msg": "i=1 ('anana'). rank[1]=2. Prev suffix is 3 ('ana'). Compare — 3 chars match ('ana'). LCP[2]=3."
    },
    {
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "lcp": [0, 1, 3, 0, 0, 0],
      "highlight": { "compareA": 3, "compareB": 5 },
      "msg": "i=3 ('ana'). rank[3]=1. Prev is 5 ('a'). k reused — start at k-1=2 already. Compare 'ana'[2..] vs 'a'[2..] — both end. LCP[1]=2? No, 'a' length 1 → LCP[1]=1."
    },
    {
      "order": [5, 3, 1, 0, 4, 2],
      "rankPair": [[3,-1], [2,-1], [5,-1], [1,-1], [4,-1], [0,-1]],
      "lcp": [0, 1, 3, 0, 0, 2],
      "msg": "Final LCP = [_, 1, 3, 0, 0, 2]. Longest repeated substring = 'ana' (max LCP = 3)."
    }
  ]
}
```
````

## 6. Closest existing widget to mimic

- **`array-traversal`** for the **per-row cell rendering** of each suffix.
  Each row is conceptually an `ArrayTraversal` strip; multiple rows
  stacked. The cell rect, character label, and per-cell highlight class
  scheme transfers verbatim.
- **`linked-list`** for the **per-row D3 join keyed by suffix start index**
  so row reorderings transition smoothly (analogous to a node moving
  between slots).

The new abstraction is the **multi-row vertical stack with cross-row
LCP bars** — no existing widget renders this. It's modest new code:
~80 LOC for the layout + bar rendering.

## 7. D3 selections plan

| Layer | Selector | Bound data | Update on step |
|---|---|---|---|
| Row container | `g.suffix-array-builder__row` keyed by `suffix start index` | one per suffix | transform-translate y transition between rows when `order` permutes |
| Row cells | `g.suffix-array-builder__row` ← `g.suffix-array-builder__cell` keyed by `char-position` within suffix | suffix characters | enter once at mount; class toggle for `--shared-prefix` based on LCP highlight |
| Start-index label | `text.suffix-array-builder__start-idx` per row | suffix start index | static |
| Rank-pair gutter | `text.suffix-array-builder__rank-pair` per row | `step.rankPair[startIdx]` | text update with tween |
| LCP bars | `g.suffix-array-builder__lcp-layer` ← `rect.suffix-array-builder__lcp-bar` keyed by `between-rows index` | `step.lcp` (when present) | width tween; opacity 0 → 0.7 fade-in on first appearance |
| Compare-overlay (kasai mode) | `g.suffix-array-builder__compare` (single) | `step.highlight.{compareA, compareB}` | enter: pair of brackets framing the two suffix rows being compared |
| Round indicator | `text.suffix-array-builder__round-label` | `step.round` | text update; flash on round change |
| Caption | `div.suffix-array-builder__msg` | `step.msg` | reactive React render |

D3 transitions:
- Row reorder: `transform: translate(0, rowY(orderIdx))` with
  `.duration(600)` (slightly slower than the standard 450 ms so the
  reader can track which row went where).
- LCP bar enter: `attr('width', 0)` then `.transition().duration(400).attr('width', barWidth)`.
- Rank-pair text: `interpolateString` tween between the prev and next
  pair-string for a brief number-roll effect.

## 8. Shared abstractions

| Lift | From / To |
|---|---|
| `Stepper.scala` | Reuse as-is |
| `PayloadDecoder.scala` | Reuse `d.string`, `d.int`, `d.dynList`, `d.stringList`; add `d.intList` (if not already from string-matcher); add `d.intPairList` for `rankPair` (new helper — also useful for graph-edge specs) |
| Per-cell rendering | Copy verbatim from `ArrayTraversal` |
| Row layout helpers | New — `rowY(orderIdx)`, `rowHeight`, `gutterX`; modelled on `ArrayTraversal`'s `cellX` |
| D3-join key strategy | Lift the "key by stable identity, not slot" pattern from `LinkedList` (id-keyed) — apply per-row keyed by `suffix start index` |
| Canon: marker colours | Compare brackets = blue `#3b82f6`; shared-prefix cells = emerald `#10b981`; LCP bar = blue with 0.7 opacity; round-label flash = amber `#f59e0b` |
| BEM block | New file `client/src/styles/components/suffix-array-builder.css`; modifiers `__row`, `__cell`, `__rank-pair`, `__lcp-bar`, `__compare`, `__start-idx`, `--shared-prefix`, `--reordering` |

## 9. Estimated build session count

**2 sessions.**

Session 1 (≈ 5-6 hours): Scaffold the Scala module; port per-cell
rendering from `ArrayTraversal`; build the multi-row stack with
row-keyed D3 join for reorder transitions; wire POC-1 (naive sort)
end-to-end with smooth row transitions.

Session 2 (≈ 4-5 hours): Add rank-pair gutter, round indicator, LCP
overlay bars (POC-2 + POC-3). Add Kasai compare-bracket overlay. Write
BEM CSS, register in `D3WidgetBlock.scala`, build demo book chapter
housing all three POCs. Run the four-gate verification (scalafmt,
compile, test, vite build) + browser smoke.

Lower than `string-matcher` (only one algorithm mode in the visual
sense; no overlay forks) but new ground for the multi-row stack
abstraction. Estimate matches the `trie` budget in `diagram-gap-audit.md`.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/07-strings/02-suffix-array-builder.md`
(NEW — created alongside the widget build session).

Houses all three POC payloads above, each preceded by the analogous
destination prose (naive O(n²log n) sort, prefix-doubling rounds,
Kasai's LCP). Acts as the living authoring reference. Future schema
changes re-render these three payloads as a smoke test before landing —
particularly important because the row-reorder transition is the
widget's most visible feature and a regression would be immediately
obvious.
