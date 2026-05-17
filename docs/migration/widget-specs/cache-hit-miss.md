# Widget Spec — `cache-hit-miss`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Render the **CPU → L1 → L2 → L3 → RAM** memory hierarchy as a stack
of horizontal bars (one per level), each scaled by its access latency
in nanoseconds so the reader sees that "L3 is roughly 10× L2 which is
roughly 3× L1 which is roughly 4× a register". A scriptable sequence of
memory accesses steps through one at a time: for each access, an
animated path lights up from the CPU down to the level where the data
was actually found (the first level whose simulated cache state
contains the address), then back up — coloured green at every level
that *hit*, rose at the level that *missed-then-loaded*, and slate at
intermediate untouched levels. A latency-scaled timing bar appears at
the bottom of each step, growing right to the cost of that access; a
cumulative "total ns" counter ticks up across steps.

The widget's central pedagogical job is to make the chapter's "L1 hit
takes 4 cycles, DRAM miss takes 200 cycles" claim viscerally felt by
letting the reader scrub through a sequence of accesses on a column-
major vs row-major matrix walk and watch the timing bars grow at
wildly different rates for the two patterns.

The widget covers the orphan chapter
[`01-foundations/05-memory-model-and-cache.md`](../../../content/cortex/data-structures-and-algorithms/01-foundations/05-memory-model-and-cache.md),
specifically "The memory hierarchy", "Cache lines", and "Worked
example: matrix traversal".

## 2. Source-diagram inventory

0 — orphan widget; payloads derived from destination chapter prose.

## 3. Destination chapter usage

Three natural insertion points, mapped to three payloads:

1. **In "The memory hierarchy" replacing the level-stack mermaid** —
   a static-ish payload that just shows the hierarchy with latency
   bars to scale. No access sequence yet; "step 1" highlights the
   register, "step 2" the L1, etc., walking down the stack so the
   reader sees the ratios. Step 6 shows them all together with a
   one-line latency callout.
2. **In "Cache lines"** — a four-step sequence on a small contiguous
   array, all accesses hitting after the first (the first access
   warms a 64-byte cache line; the next three are L1 hits). Drives
   home the "the next 56 bytes came along for free" point.
3. **In "Worked example: matrix traversal"** — the centrepiece.
   Two payloads side by side: row-major (16 accesses, mostly L1 hits)
   and column-major (16 accesses, mostly L1 misses bouncing to L2 or
   L3). The cumulative-ns counter at the end shows the chapter's
   "60 ms vs 600 ms" gap in microcosm.

## 4. Payload schema sketch

```typescript
{
  title?: string;
  // The hierarchy is fixed; authors don't redesign the CPU. The widget
  // hard-codes the five levels with sensible defaults and lets the
  // author override individual latencies for the chapter at hand.
  // Defaults below assume a typical mid-range x86_64 laptop.
  hierarchy?: {
    register?: { sizeLabel: string; latencyNs: number };  // default 32×8B, 0.3 ns
    l1?:       { sizeLabel: string; latencyNs: number };  // default 32 KB, 1.0 ns
    l2?:       { sizeLabel: string; latencyNs: number };  // default 512 KB, 3.0 ns
    l3?:       { sizeLabel: string; latencyNs: number };  // default 16 MB, 12.0 ns
    ram?:      { sizeLabel: string; latencyNs: number };  // default 16 GB, 80.0 ns
  };
  // Cache line size in bytes. Used to compute hit/miss per access in
  // the simulator. Defaults to 64.
  cacheLineBytes?: number;
  // Capacity in cache lines per level. Used by the LRU simulator below.
  l1Lines?: number;        // default 512  (32 KB / 64 B)
  l2Lines?: number;        // default 8192 (512 KB / 64 B)
  l3Lines?: number;        // default 262144 (16 MB / 64 B)
  // The access sequence. Each access is a single byte address. The
  // widget runs an LRU simulator over the three caches to determine
  // hit/miss per level. Authors who want to script the hits manually
  // can use the customSteps option below.
  accesses: Array<{
    address: number;       // byte address; cache lines derived as address >> 6 (for cacheLineBytes=64)
    note?: string;         // optional per-step caption
  }>;
  // Alternative to accesses: hand-authored steps for chapters where the
  // simulator's eviction decisions would distract. The widget then
  // skips the simulator and just renders what the author says.
  customSteps?: Array<{
    note: string;
    hitLevel: "register" | "l1" | "l2" | "l3" | "ram";  // where the access landed
    latencyNs: number;
  }>;
  // Default true. When false, the cumulative-ns counter is hidden.
  showCumulative?: boolean;
}
```

## 5. POC payloads

The hierarchy walk-through (static-ish, step-driven introduction):

````markdown
```d3 widget=cache-hit-miss
{
  "title": "The memory hierarchy — latency to scale",
  "accesses": [],
  "customSteps": [
    { "note": "Register access — instant", "hitLevel": "register", "latencyNs": 0.3 },
    { "note": "L1 cache hit — ~1 ns",      "hitLevel": "l1",       "latencyNs": 1.0 },
    { "note": "L2 cache hit — ~3 ns",      "hitLevel": "l2",       "latencyNs": 3.0 },
    { "note": "L3 cache hit — ~12 ns",     "hitLevel": "l3",       "latencyNs": 12.0 },
    { "note": "RAM access — ~80 ns",       "hitLevel": "ram",      "latencyNs": 80.0 }
  ],
  "showCumulative": false
}
```
````

Cache-line bonus — four accesses, three free:

````markdown
```d3 widget=cache-hit-miss
{
  "title": "One cache-line load, four reads",
  "accesses": [
    { "address": 0,  "note": "First read — L1 miss; loads 64-byte line" },
    { "address": 8,  "note": "Same cache line — L1 hit" },
    { "address": 16, "note": "Same cache line — L1 hit" },
    { "address": 24, "note": "Same cache line — L1 hit" }
  ]
}
```
````

Row-major matrix walk (sequential addresses, mostly hits):

````markdown
```d3 widget=cache-hit-miss
{
  "title": "Row-major sum — sequential access, L1-friendly",
  "accesses": [
    { "address": 0  }, { "address": 8  }, { "address": 16 }, { "address": 24 },
    { "address": 32 }, { "address": 40 }, { "address": 48 }, { "address": 56 },
    { "address": 64 }, { "address": 72 }, { "address": 80 }, { "address": 88 },
    { "address": 96 }, { "address": 104}, { "address": 112}, { "address": 120}
  ]
}
```
````

Column-major matrix walk (stride-n addresses, mostly misses) — 1024×1024
matrix of doubles, column stride is `8 * 1024 = 8192` bytes:

````markdown
```d3 widget=cache-hit-miss
{
  "title": "Column-major sum — strided access, L1-hostile",
  "accesses": [
    { "address": 0      }, { "address": 8192   }, { "address": 16384  }, { "address": 24576  },
    { "address": 32768  }, { "address": 40960  }, { "address": 49152  }, { "address": 57344  },
    { "address": 65536  }, { "address": 73728  }, { "address": 81920  }, { "address": 90112  },
    { "address": 98304  }, { "address": 106496 }, { "address": 114688 }, { "address": 122880 }
  ],
  "l1Lines": 8
}
```
````

(The artificially small `l1Lines: 8` here forces the simulator to evict
quickly so the column-major story is visible in 16 accesses; in real
hardware the eviction would come later, but the *pattern* — every access
a miss — is what the reader needs to see.)

## 6. Closest existing widget to mimic

The closest catalog widget is `array-traversal` — both step through a
sequence of states with a per-step caption and an animation between
states. The key shape differences:

- The visual is a *vertical stack of bars* sized by latency, not a
  *horizontal row of cells*.
- An animated path (CPU → level-of-hit → CPU) overlays the bars on
  every step; this is a transient `<g>` that fades in on step start
  and out on step end.
- A latency-scaled timing bar grows per-step at the bottom; the
  cumulative-ns counter is an HTML `<div>` updated on step change.

Reuse the React + D3 boundary from `ArrayTraversal.scala`: React owns
the host `<div>`, the cumulative counter, and the step controls; D3
owns the `<svg>` containing the hierarchy bars, the access path, and
the timing bar.

## 7. D3 selections plan

Four layers inside the `<svg>`:

- `<g class="cache-hit-miss__hierarchy">` — five `<rect>` bars, one
  per level, x-position fixed, width proportional to `log(latencyNs)`
  (log-scale because the linear-scale ratio between register and RAM
  would overwhelm everything else). Drawn once at mount; never
  re-drawn.
- `<g class="cache-hit-miss__cells">` — for the hit-level bar, a row of
  small cache-line cells that highlight green when the access is a
  hit and rose when it triggers a load. Eight cells per level (fits
  in the chapter column width) is a *symbolic* representation of the
  cache; not literally the simulator's full state.
- `<g class="cache-hit-miss__path">` — the per-step animated path
  from the CPU node down to the hit level and back. Implemented as a
  `<path>` with a stroke-dasharray and an animated `stroke-dashoffset`.
  Disappears on step exit.
- `<g class="cache-hit-miss__timing">` — at the bottom, a horizontal
  bar growing left-to-right to the current step's latency, with a
  number at the right end. Updated on every step.

The cumulative-ns counter is a React-rendered HTML element above the
SVG, not part of the SVG; updates via `useState`.

## 8. Shared abstractions

- **`PayloadDecoder`** — parses the optional `hierarchy` overrides, the
  `accesses` array, the optional `customSteps`, and the optional
  capacity/cache-line overrides.
- **`Stepper.scala`** — reuse for prev / next / play / reset.
- **A new internal `LruCacheSim`** inside the widget module: a
  three-level LRU simulator that ingests a sequence of byte addresses
  and returns per-step `{hitLevel, latencyNs}`. Pure function, no DOM.
  Lives in the widget module; not promoted to `shared/` until a second
  widget needs it.

## 9. Estimated build session count

**1–2 build sessions**:

- Session 1: Scala module skeleton, hierarchy bars, the LRU
  simulator, hand-authored payload rendering, three payloads working.
- Session 2 (optional): animated path overlay, timing bar polish,
  cumulative counter, browser smoke pass, the column-major
  contrived-eviction payload.

Risk factors: the LRU simulator can give surprising results for
adversarial access sequences; the demo book chapter should explicitly
note that this is an *illustrative* simulator (one level per bar with
LRU eviction), not a model of real CPU caches (inclusive vs exclusive,
write-back, multi-core coherence, prefetcher behaviour — all elided).
For chapters where the simulator's behaviour would be confusing,
authors should use `customSteps` to script the hit/miss pattern by hand.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/02-foundations/04-cache-hit-miss.md` —
a new chapter under the demo book exhibiting the four payloads with
prose contextualising the latency ratios and the row-major vs column-
major story. Created as part of the widget build session; serves as the
canonical authoring reference.
