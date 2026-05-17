# Widget Spec — `string-matcher`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise the **four canonical substring-matching algorithms** —
**naive**, **KMP**, **Z**, and **Rabin-Karp** — on a single composable
widget. The author picks an algorithm via `algorithm`, supplies a text
strip + pattern strip, and the widget renders both rows with an
algorithm-specific overlay that exposes the precomputed structure
driving each algorithm:

| `algorithm` | Overlay |
|---|---|
| `"naive"` | Pattern slides over text by 1 per outer step; inner mismatch counter; no precomputed table |
| `"kmp"` | Failure-function table `pi[]` above the pattern; on mismatch, the pattern slides by `j - pi[j-1]` and the active pointer falls back to `pi[j-1]` |
| `"z"` | Z-array bar chart above the concatenated `P + "$" + T` strip; current Z-box `[l, r]` shaded |
| `"rabin-karp"` | Rolling-hash window value + pattern-hash for collision comparison; verify-on-collision branch highlighted |

A unified step model drives all four — `{ textPtr, patPtr, mode, msg,
overlay }` — so the chapter reads four near-identical instances and the
reader can directly compare what each algorithm "knows" at any moment.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

The strings phase has no source counterpart in the Lesson Source. All
payloads are authored from destination chapter prose at
`content/cortex/data-structures-and-algorithms/07-strings/`:

- `01-string-matching-naive.md` (pseudocode lines 14-25, example lines 37-57)
- `02-kmp.md` (failure-fn table line 44-48, build pseudo lines 62-73,
  match pseudo lines 85-98)
- `03-z-algorithm.md` (Z-array table line 44-48, build pseudo lines 62-74)
- `04-rabin-karp-and-rolling-hash.md` (rolling-hash trick lines 54-69,
  algorithm lines 75-95)

## 3. Destination chapter usage

| Chapter | Instances |
|---|---|
| `07-strings/01-string-matching-naive.md` | 1 — `algorithm: "naive"` on `T="ababcababcabc"`, `P="abc"` |
| `07-strings/02-kmp.md` | 2 — first instance on `P="ABABC"` for failure-function build; second on `T="ABABDABACDABABCABAB"`, `P="ABABCABAB"` for matching |
| `07-strings/03-z-algorithm.md` | 2 — first on `S="aabcaabxaaaz"` for Z-array build; second on concat `S=P+"$"+T` for matching |
| `07-strings/04-rabin-karp-and-rolling-hash.md` | 2 — first showing single rolling-hash window slide; second on multi-match scan with one false-positive verify |

Total at landing: **7 instances** across 4 chapters. The unified schema
lets a single widget module own all four algorithms — no per-algorithm
fork. Adding `boyer-moore` or `aho-corasick` later is a new
`algorithm` value, not a new widget.

## 4. Payload schema sketch

```ts
{
  title:     string,
  algorithm: "naive" | "kmp" | "z" | "rabin-karp",
  text:      string,                // T
  pattern:   string,                // P
  steps: Array<{
    textPtr:   number,              // index into T currently aligned with patPtr=0 of the pattern
                                    //   (i.e. the pattern's left edge sits over T[textPtr])
    patPtr:    number,              // j — current pattern index being compared
    status:    "compare" | "match" | "mismatch" | "shift" | "fallback" | "found" | "done",
    matches:   number[],            // discovered match start indices so far

    // Overlay payload — exactly one of the following depending on algorithm:
    pi?:        number[],           // failure-function array (kmp only); length = pattern.length
    zArray?:    number[],           // Z-array values (z only); length = concat.length
    zBox?:      { l: number, r: number },  // current Z-box (z only)
    rollingHash?: {                 // rolling-hash state (rabin-karp only)
      windowHash: number,
      patternHash: number,
      window: string,               // T.substring(textPtr, textPtr + m)
      hit: boolean,                 // does windowHash == patternHash?
      verified: boolean | null,     // null until verify runs; true/false after
    },

    msg: string,
  }>,
}
```

**Two-strip layout.** The text row is fixed at the top; the pattern row
slides horizontally under it. `textPtr` controls the pattern's `x`
offset; `patPtr` highlights the active comparison cell in both rows
simultaneously (via a connecting vertical line for visual binding).

**Algorithm-specific overlay rendering.** A `<g class="string-matcher__overlay">`
group sits above the text strip. Its contents are conditional on
`algorithm`:

- `naive` — empty (no precomputed structure).
- `kmp` — a row of cells matching the pattern, each labelled with `pi[i]`;
  current `j` and current `pi[j-1]` fallback target connected by an arc
  on mismatch.
- `z` — a bar chart with one bar per index of `S = P + "$" + T`,
  height `Z[i]`; current Z-box `[l, r]` shaded behind the bars.
- `rabin-karp` — a horizontal bar showing the current window's hash vs
  the pattern's hash; both numeric values displayed; equal-hashes
  flagged for a verify branch with a green checkmark or red cross.

## 5. POC payloads

### POC-1 — Naive matching on `T="ababcababcabc"`, `P="abc"`

````markdown
```d3 widget=string-matcher
{
  "title": "Naive substring matching — slide by 1, compare until mismatch",
  "algorithm": "naive",
  "text": "ababcababcabc",
  "pattern": "abc",
  "steps": [
    {
      "textPtr": 0, "patPtr": 0, "status": "compare", "matches": [],
      "msg": "i=0: compare T[0]='a' vs P[0]='a'. Match."
    },
    {
      "textPtr": 0, "patPtr": 1, "status": "mismatch", "matches": [],
      "msg": "i=0,j=1: T[1]='b' vs P[1]='b'. Match. j=2: T[2]='a' vs P[2]='c'. Mismatch — slide."
    },
    {
      "textPtr": 1, "patPtr": 0, "status": "compare", "matches": [],
      "msg": "i=1: T[1]='b' vs P[0]='a'. Immediate mismatch — slide."
    },
    {
      "textPtr": 2, "patPtr": 2, "status": "match", "matches": [2],
      "msg": "i=2: T[2..4]='abc' = P. Match at index 2! Slide."
    },
    {
      "textPtr": 7, "patPtr": 2, "status": "match", "matches": [2, 7],
      "msg": "i=7: T[7..9]='abc' = P. Match at index 7."
    },
    {
      "textPtr": 10, "patPtr": 2, "status": "found", "matches": [2, 7, 10],
      "msg": "i=10: T[10..12]='abc' = P. Three matches in total."
    }
  ]
}
```
````

### POC-2 — KMP build + match on `T="ABABDABACDABABCABAB"`, `P="ABABCABAB"`

(Compressed from full match scan to the high-signal failure-fn steps +
the one critical fallback-on-mismatch in the text. Failure-fn for
`ABABCABAB` is `[0,0,1,2,0,1,2,3,4]`.)

````markdown
```d3 widget=string-matcher
{
  "title": "KMP — fall back via pi[] instead of restarting",
  "algorithm": "kmp",
  "text": "ABABDABACDABABCABAB",
  "pattern": "ABABCABAB",
  "steps": [
    {
      "textPtr": 0, "patPtr": 0, "status": "compare", "matches": [],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "Failure function pre-built: pi = [0,0,1,2,0,1,2,3,4]. Begin scan."
    },
    {
      "textPtr": 0, "patPtr": 4, "status": "mismatch", "matches": [],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "i=4, j=4: T[4]='D' vs P[4]='C'. Mismatch at j=4."
    },
    {
      "textPtr": 2, "patPtr": 2, "status": "fallback", "matches": [],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "Fall back: j ← pi[3] = 2. Pattern slides so prefix 'AB' aligns with the 'AB' already matched. i unchanged."
    },
    {
      "textPtr": 2, "patPtr": 0, "status": "mismatch", "matches": [],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "T[4]='D' vs P[0]='A'. Mismatch. j=0 already, so i advances."
    },
    {
      "textPtr": 10, "patPtr": 9, "status": "found", "matches": [10],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "Full match found at i=10. Set j ← pi[8] = 4 to continue scanning for overlapping matches."
    },
    {
      "textPtr": 10, "patPtr": 4, "status": "done", "matches": [10],
      "pi": [0, 0, 1, 2, 0, 1, 2, 3, 4],
      "msg": "Scan complete. Pattern occurred once at index 10. Cost O(n+m)."
    }
  ]
}
```
````

### POC-3 — Z-array build on `S="aabcaabxaaaz"`

(Demonstrates the Z-box `[l, r]` mechanic. Z-array is
`[0,1,0,0,3,1,0,0,2,2,1,0]`.)

````markdown
```d3 widget=string-matcher
{
  "title": "Z-algorithm — maintain a Z-box to skip work",
  "algorithm": "z",
  "text": "aabcaabxaaaz",
  "pattern": "aabcaabxaaaz",
  "steps": [
    {
      "textPtr": 1, "patPtr": 0, "status": "compare", "matches": [],
      "zArray": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      "zBox": { "l": 0, "r": 0 },
      "msg": "i=1: outside any Z-box. Scan from scratch."
    },
    {
      "textPtr": 1, "patPtr": 1, "status": "mismatch", "matches": [],
      "zArray": [0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      "zBox": { "l": 1, "r": 2 },
      "msg": "Z[1]=1: S[1]='a' matches S[0]. S[2]='b' ≠ S[1]='a'. Set Z-box [1, 2]."
    },
    {
      "textPtr": 4, "patPtr": 0, "status": "compare", "matches": [],
      "zArray": [0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      "zBox": { "l": 1, "r": 2 },
      "msg": "i=4: outside Z-box [1, 2]. Scan from scratch."
    },
    {
      "textPtr": 4, "patPtr": 3, "status": "mismatch", "matches": [],
      "zArray": [0, 1, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0],
      "zBox": { "l": 4, "r": 7 },
      "msg": "Z[4]=3: 'aab' matches prefix. S[7]='x' ≠ S[3]='c'. Update Z-box to [4, 7]."
    },
    {
      "textPtr": 5, "patPtr": 0, "status": "compare", "matches": [],
      "zArray": [0, 1, 0, 0, 3, 1, 0, 0, 0, 0, 0, 0],
      "zBox": { "l": 4, "r": 7 },
      "msg": "i=5 inside box [4, 7]. Z[5−4]=Z[1]=1, and r−i=2 → Z[5]=min(2,1)=1. Copy, no scan."
    },
    {
      "textPtr": 8, "patPtr": 0, "status": "compare", "matches": [],
      "zArray": [0, 1, 0, 0, 3, 1, 0, 0, 2, 2, 1, 0],
      "zBox": { "l": 8, "r": 10 },
      "msg": "i=8: scan 'aa' matches; Z[8]=2. Box now [8, 10]. Subsequent i=9,10 mostly copied — O(n) overall."
    },
    {
      "textPtr": 11, "patPtr": 0, "status": "done", "matches": [],
      "zArray": [0, 1, 0, 0, 3, 1, 0, 0, 2, 2, 1, 0],
      "zBox": { "l": 8, "r": 10 },
      "msg": "Z-array complete. Each character examined O(1) amortised — total O(n)."
    }
  ]
}
```
````

### POC-4 — Rabin-Karp rolling-hash slide on small text with one false positive

(Compact scan of `T="cabbad"`, `P="bba"`, hash base 31, modulus prime
997 — chosen small so the false positive on a synthetic collision can be
demonstrated cleanly.)

````markdown
```d3 widget=string-matcher
{
  "title": "Rabin-Karp — compare hashes; verify on collision",
  "algorithm": "rabin-karp",
  "text": "cabbad",
  "pattern": "bba",
  "steps": [
    {
      "textPtr": 0, "patPtr": 0, "status": "compare", "matches": [],
      "rollingHash": {
        "windowHash": 311, "patternHash": 705,
        "window": "cab", "hit": false, "verified": null
      },
      "msg": "Window 'cab' hash=311. Pattern 'bba' hash=705. No collision — slide."
    },
    {
      "textPtr": 1, "patPtr": 0, "status": "compare", "matches": [],
      "rollingHash": {
        "windowHash": 152, "patternHash": 705,
        "window": "abb", "hit": false, "verified": null
      },
      "msg": "Rolling-hash update: drop 'c'·31², shift left, add 'b'. Window 'abb' hash=152. No collision."
    },
    {
      "textPtr": 2, "patPtr": 0, "status": "compare", "matches": [],
      "rollingHash": {
        "windowHash": 705, "patternHash": 705,
        "window": "bba", "hit": true, "verified": null
      },
      "msg": "Window 'bba' hash=705. COLLISION with pattern hash. Verify character-by-character."
    },
    {
      "textPtr": 2, "patPtr": 2, "status": "match", "matches": [2],
      "rollingHash": {
        "windowHash": 705, "patternHash": 705,
        "window": "bba", "hit": true, "verified": true
      },
      "msg": "Verified: 'bba' = 'bba'. True match at index 2."
    },
    {
      "textPtr": 3, "patPtr": 0, "status": "compare", "matches": [2],
      "rollingHash": {
        "windowHash": 705, "patternHash": 705,
        "window": "bad", "hit": true, "verified": false
      },
      "msg": "Synthetic collision example: window 'bad' hashes equal but verify fails. False positive — discard."
    },
    {
      "textPtr": 4, "patPtr": 0, "status": "done", "matches": [2],
      "rollingHash": {
        "windowHash": 0, "patternHash": 705,
        "window": "ad", "hit": false, "verified": null
      },
      "msg": "Window shorter than pattern — scan complete. 1 match found. Average cost O(n+m); the verify branch handled the one collision."
    }
  ]
}
```
````

## 6. Closest existing widget to mimic

- **`array-traversal`** for the **two parallel rows** (text strip + pattern
  strip — analogous to `array-traversal`'s primary + secondary row
  feature). The fixed-width-cell rendering, the per-cell highlight
  classes, the `keys` based join, and the per-row marker layer all map
  1:1.
- **`linked-list`** for the **per-step arc** drawn between pattern's `j`
  and the fallback target `pi[j-1]` (KMP overlay). The dashed back-edge
  bezier used for Floyd-cycle visualisations is the right primitive for
  the fallback arrow.

The overlay layer above the text strip is novel — none of the existing
widgets render an "above-row" auxiliary chart. The Z-array bar chart in
particular needs a `d3.scaleLinear()` from `Z[i]` to bar height; modest
new code.

## 7. D3 selections plan

| Layer | Selector | Bound data | Update on step |
|---|---|---|---|
| Text cells | `g.string-matcher__text-row` ← `g.string-matcher__cell--text` keyed by index | `text.split('')` | static after mount; per-step class toggle `--active` for `textPtr + patPtr`, `--matched` for confirmed-match cells |
| Pattern cells | `g.string-matcher__pattern-row` ← `g.string-matcher__cell--pat` keyed by index | `pattern.split('')` | transform-translate transition for `x` offset when `textPtr` advances |
| Pattern row container | `g.string-matcher__pattern-row` | (parent of above) | `transform: translate(textPtr * cellWidth, 0)` with `.duration(450)` |
| Active comparator line | `line.string-matcher__binder` (single) | `{ textPtr, patPtr }` | endpoints transition between cells |
| KMP overlay (pi cells) | `g.string-matcher__overlay--kmp` ← `g.string-matcher__pi-cell` keyed by index | `pi` | enter on first kmp step; per-step `--active-j` and `--fallback-target` class toggles |
| KMP fallback arc | `path.string-matcher__pi-arc` (single) | `{ j, pi[j-1] }` | enter on mismatch; fade out on next step |
| Z overlay (bars) | `g.string-matcher__overlay--z` ← `rect.string-matcher__z-bar` keyed by index | `zArray` | height tween from prev to next value |
| Z-box shading | `rect.string-matcher__z-box` (single) | `step.zBox` | x/width transition |
| Rabin-Karp overlay | `g.string-matcher__overlay--rk` ← three labelled values | `step.rollingHash` | text update; collision flash on `hit && verified === true`; red-X on `verified === false` |
| Caption | `div.string-matcher__msg` | `step.msg` | reactive React render |

D3 transitions:
- Pattern slide: `transform: translate(textPtr * (cellSize + cellGap), 0)`
  with `.duration(StepDelayMs)`.
- KMP fallback arc: bezier path enter with `stroke-dasharray` animation
  to convey "snap back".
- Z bars: vertical `rect` height tween via `attr('y', …)` and
  `attr('height', …)` interpolation.
- Rabin-Karp hash: flash background of the hash cell amber on collision.

## 8. Shared abstractions

| Lift | From / To |
|---|---|
| `Stepper.scala` | Reuse as-is |
| `PayloadDecoder.scala` | Reuse `d.int`, `d.string`, `d.dynList`; add `d.intList` (likely already exists for `markers`); add `d.optObj` for the nested overlay objects |
| Two-row cell layout | Lift from `ArrayTraversal`'s `secondaryItems` machinery (`secondaryRowY`, `secondaryItemsFor`, etc.) — the text strip is the "primary", pattern strip is the "secondary" but renders below with offset-translate |
| Per-cell rect + label | Copy verbatim from `ArrayTraversal`'s cell rendering |
| Per-row marker triangle | Copy from `ArrayTraversal`'s marker layer (for `textPtr` / `patPtr` indicators) |
| Bezier arc primitive | Lift from `LinkedList`'s cycle-back-edge (dashed variant) — re-use for KMP fallback arrow |
| Canon: marker colours | Active comparator = blue `#3b82f6`; match cell = emerald `#10b981`; mismatch = rose `#ef4444`; fallback = amber `#f59e0b`; verify = emerald on success, rose on collision |
| BEM block | New file `client/src/styles/components/string-matcher.css`; modifiers `__cell`, `__binder`, `__pi-cell`, `__pi-arc`, `__z-bar`, `__z-box`, `__rk-hash`, `--active`, `--matched`, `--fallback`, `--collision` |

## 9. Estimated build session count

**2-3 sessions.**

Session 1 (≈ 4-5 hours): Scaffold the Scala module; port the two-row
layout from `ArrayTraversal`; wire `algorithm: "naive"` end-to-end with
POC-1. Get text+pattern strips rendering with pattern slide animation
and per-cell colour classes.

Session 2 (≈ 4-6 hours): Add KMP overlay (pi-cells row + fallback
bezier arc); wire POC-2; tune the fallback animation. Then add Z-array
overlay (bar chart + Z-box shading); wire POC-3.

Session 3 (≈ 3-4 hours): Add Rabin-Karp overlay (hash display + verify
branch); wire POC-4. Write the BEM CSS, register in
`D3WidgetBlock.scala`, build demo book chapter housing all four POCs.
Run the four-gate verification (scalafmt, compile, test, vite build) +
browser smoke.

This is the most complex of the five specs in this batch because of
four distinct overlay modes; budget aligns with `graph-explorer` in
`diagram-gap-audit.md`'s Wave-B Tier 1.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/07-strings/01-string-matcher.md` (NEW —
created alongside the widget build session).

Houses all four POC payloads above (one per algorithm mode), each
preceded by the analogous destination prose. Acts as the living
authoring reference. Future schema changes re-render these four payloads
as a smoke test before landing. Particularly important here since the
overlay rendering forks four ways — a regression in one mode would be
caught immediately.
