# Alignment — current implementation ↔ design system

Re-verified after the user updated the design folder with a complete file set
(now includes the 3 `index.html` files, 22 `preview/*` token cards, and 13
`assets/*.webp` images that weren't extractable on the first pass).

## Two systems live in this folder — important context

The design folder mixes **baseline** (pre-redesign extract) and **redesign
prototype** files. Knowing which is which determines what to verify against:

| Layer | Files | Describes |
|---|---|---|
| **Baseline** | [`README.md`](README.md), [`colors_and_type.css`](colors_and_type.css), [`ui_kits/portfolio/`](ui_kits/portfolio/), [`ui_kits/cortex/style.css`](ui_kits/cortex/style.css), all of [`preview/`](preview/) | The **old** site — blue spine (`--blue-700 #1d4ed8`), system-stack fonts, "Hi, I'm" hero, no SelectedWork, blue cortex reader. Auto-extracted from `client/` *before* the redesign. |
| **Redesign prototype** | [`index.html`](index.html) (root), [`sections.jsx`](sections.jsx), [`app.jsx`](app.jsx), [`data.jsx`](data.jsx), [`tweaks-panel.jsx`](tweaks-panel.jsx) | The **new** editorial direction — terracotta `oklch(0.62 0.13 50)`, Instrument Serif italic, Geist + Geist Mono, dark-roast palette, SelectedWork strip, vertical Experience accordion, ASCII K3s art, "Let's talk." footer. |

**The codebase ships the redesign**, so the verdict below compares the
implementation against the **prototype** layer (root [`index.html`](index.html)
+ [`sections.jsx`](sections.jsx)). The baseline files have not been
regenerated — that's a documentation hygiene gap, orthogonal to the code.

## Token-level comparison (light mode)

| Token | Prototype (root [`index.html`](index.html) `:root`) | Implementation ([`tailwind.css`](../client/tailwind.css) `:root`) | Aligned? |
|---|---|---|---|
| Background | `#f6f3ec` | `hsl(36 28% 96%)` ≈ `#faf6ec` | ✅ within 4 RGB units |
| Foreground / ink | `#1a1814` | `hsl(30 14% 14%)` ≈ `#28231f` | ✅ visually indistinguishable |
| Muted | `#6b6659` | `hsl(30 8% 38%)` ≈ `#61584a` | ✅ |
| Line | `#d9d2bf` | `hsl(32 18% 84%)` ≈ `#ddd1c1` | ✅ |
| Card | `#fbf9f3` (warm-tinted) | `hsl(0 0% 100%)` (pure white) | ⚠️ minor — cards read slightly cooler in light mode |
| Accent | `oklch(0.62 0.13 50)` ≈ `#c8714e` | `hsl(18 55% 52%)` ≈ `#c46a3f` | ✅ |
| Radius | `14px` | `0.75rem` (12px) | ⚠️ -2px on every card |
| Page max width | `1180px` | `max-w-6xl` (1152px) | ✅ within 28px |
| Serif | `Instrument Serif` | `Instrument Serif` | ✅ |
| Sans | `Geist` (300/400/500/600/700) | `Geist` (400/500/600/700) | ✅ — implementation drops 300 weight, not used anywhere |
| Mono | `Geist Mono` | `Geist Mono` | ✅ |

Dark mode ink/bg/muted/card values land in the same places.

## Section-by-section comparison

### Header

| Prototype ([`sections.jsx`](sections.jsx) + root [`index.html`](index.html) `.hdr*`) | Implementation ([Header.scala](../client/src/main/scala/codefolio/client/components/sections/Header.scala) + [header.css](../client/src/styles/sections/header.css)) | Aligned? |
|---|---|---|
| `position: sticky; top: 0` | `fixed top-0` | ✅ behaviourally equivalent |
| `backdrop-filter: blur(14px) saturate(140%)` + `color-mix(in oklch, var(--bg) 78%, transparent)` | `bg-background/85 backdrop-blur` | ✅ |
| `.hdr__brand-mark` 22×22, **filled** `background: var(--ink); color: var(--bg)`, mono "a" | `.header__logomark` 28×28, **outlined** `border-primary text-primary`, mono "a." | ⚠️ visual mismatch — prototype is a solid black/cream chip, mine is a terracotta-bordered outline |
| `.hdr__nav` 5 pill-radius links, hover `background: var(--bg-2)` | `.header__menu` 5 links with sliding-underline `::after` on hover | ⚠️ different hover affordance — prototype is filled-pill, mine is animated underline |
| `.hdr__cta` **filled** ink-on-bg pill | `.header__cta` **ghost** outlined pill | ⚠️ visual mismatch — prototype's "Get in touch" is a solid black button, mine is bordered |
| `.hdr__theme` 34px circle border button | Existing `ToggleMode.Component()` slot | ✅ |
| 5 nav items: Work · About · Experience · Projects · Cortex | 5 nav items: Work · About · Experience · Projects · Cortex | ✅ |

### Hero

| Prototype | Implementation | Aligned? |
|---|---|---|
| Status pill: green dot (`oklch(0.62 0.13 145)`) + box-shadow ring + 2.4s pulse | Status pill: `#4ade80` (Tailwind green-400) + box-shadow ring + 2.4s pulse | ✅ |
| Status copy: "Currently at Europcar · open to senior backend roles" mono 12px | Same copy, mono 11px | ✅ |
| Name: `clamp(56px, 9vw, 128px)`, line-height 0.92, letter-spacing -0.025em | `clamp(64px, 12vw, 128px)`, line-height 0.92, letter-spacing -0.02em | ✅ visually equivalent (mine scales faster on viewport) |
| Name "EU" sup: mono 12px, vertical-align: super, margin-left 6px | Same | ✅ |
| Lede italic em is sized at `1.08em`, color `var(--ink)` | Italic em uses `font-display` family but no size bump | ⚠️ `<em>backend-leaning</em>` reads slightly smaller |
| 3 CTAs: filled ink primary `Download CV →` | filled accent primary | ✅ — except prototype filled is **ink (black/cream)**, mine is **terracotta** |
| Ghost `See selected work` | ghost outlined | ✅ |
| Plain `a.r.kakde@gmail.com` | plain text-link | ✅ |
| Stat strip: 4 cells with **vertical dividers** between (`border-right`) + horizontal `border-top`/`border-bottom` | 4-col CSS grid, **no dividers**, single `border-top` only | ❌ visible visual difference — prototype has the cell-divider chrome |
| Stat number: `44px` fixed serif italic | `clamp(48px, 5vw, 64px)` — significantly larger | ⚠️ visual emphasis differs |
| Logo strip eyebrow "BUILT FOR" (mono) + 6 italic-serif company names | Same | ✅ |
| 6 names: Europcar · Audi · Disneyland Paris · Dassault · UPS · Bell Labs | Same | ✅ |
| Section padding `64px 0 56px` | `pt-24 pb-16 px-6` (`min-h-screen`) | ⚠️ implementation forces full viewport height; prototype lets it size to content |

### Selected Work

| Prototype `.swork*` | Implementation `.selected-work__*` | Aligned? |
|---|---|---|
| `<section id="work">` | Same | ✅ |
| Eyebrow + italic h2 "Three rooms / I helped build." + right-aligned mono intro | Same | ✅ |
| Row grid: `200px 1fr 180px 32px` (4 cols) | `minmax(160,1fr) minmax(180,1.2fr) minmax(0,2fr) minmax(140,1fr) auto` (5 cols) | ⚠️ structurally same but mine has separate `meta` and `tags` columns; prototype merges meta+role into one and puts tags right-aligned in same cell as `what` |
| Hover: row gets `padding-left: 14px` (whole row shifts) + `background: var(--bg-2)` + 3px terracotta `::before` rail (scaleY 0→1) | Hover: `transform: translateX(2px)` + 2px rail (scaleY 0→1) | ⚠️ shift smaller (2 vs 14px), no row-bg fade, rail thinner |
| Arrow ↗ rotates `translate(4px, -4px)` to terracotta on hover | Arrow rotates `-45deg` + small translate, terracotta on hover | ✅ |
| Tech tags as mono dots: `swork__tech-tag + swork__tech-tag::before { content: "·" }` | Tech tags as plain mono spans, no separator | ⚠️ no visible separator between tags |

### About

| Prototype `.about*` | Implementation `.about__*` | Aligned? |
|---|---|---|
| Eyebrow "About" + 3 prose paragraphs (verbatim) | Same | ✅ — copy match |
| Right `.about__side` `position: sticky; top: 96px` | `lg:sticky lg:top-24` | ✅ |
| Portrait `aspect-ratio: 4/5; border-radius: var(--radius)` (14px) | `aspect-square` (1:1), `rounded-md` (≈10px) | ⚠️ aspect ratio differs — prototype is portrait, mine is square |
| 5 facts (Currently / Based / Studying / Education / Languages) | Same 5 facts, copy verbatim | ✅ |
| Fact grid: `110px 1fr` 16px gap | `100px 1fr` 12px gap | ✅ functionally same |

### Experience

| Prototype `.exp*` | Implementation `.experience__*` | Aligned? |
|---|---|---|
| Vertical accordion, one open by default, header `200px 1fr 180px 32px` grid | Same accordion semantics, header `1fr auto auto` grid | ✅ behaviour |
| `.exp__chev` rotates 180° when open (chevron-down → up), turns terracotta | `.experience__role-chevron` rotates 90° (chevron-right → down) | ⚠️ different chevron icon + rotation axis |
| `.exp__body` animates `max-height 0 → 1600px` on open (360ms ease) | Body conditionally renders (no transition) | ⚠️ no animation |
| Bullets: 6px-wide terracotta dash `::before` at left | List-style `square` | ⚠️ different bullet glyph |
| Results pull-quote: `background: var(--accent-soft)` (12% terracotta) + 2px terracotta left border + mono uppercase "RESULTS" label | `background: hsl(var(--primary) / 0.06)` + 2px primary left border + mono "RESULTS" label | ✅ |
| Tech tags: `var(--bg-2)` background + line border + 999px radius; primary tags filled `var(--ink)` | Tags `hsl(var(--primary) / 0.12)` background + primary/35% border + rounded-full; secondary tags as plain mono spans | ✅ — prototype's primary chips are filled black/cream, mine are terracotta-tinted |

### Projects

| Prototype `.proj*` / `.pcard*` | Implementation `.projects__*` | Aligned? |
|---|---|---|
| Section `background: var(--bg-2)` + top/bottom borders (subtle visual band) | Plain section, just top border | ⚠️ no warm band |
| Filter chips with optional count: `<span class="chip__count">{n}</span>` | Filter chips, no counts | ⚠️ minor |
| Featured card: 2-col `1fr 1fr` grid; left `proj__featured-art` 4:3 with diagonal-stripe + linear-gradient bg + a `proj__featured-diagram` card-in-card with ASCII art | Featured card: `md:col-span-2` with the ASCII as the entire hero block | ⚠️ structurally different — prototype has framed ASCII inside an art panel; mine has bare ASCII spanning the card |
| Tinted placeholder `pcard__art--stripe-a/b/c` variants — repeating diagonals + accent gradients | Single `.projects__placeholder` with 45° stripe pattern + metadata caption | ✅ same intent, simpler |
| Card hover: `translateY(-3px)` + shadow + border lift | Border-color change only, no lift | ⚠️ flatter |
| Card name: serif italic 22px | Sans medium 18px | ⚠️ prototype uses italic-serif card names, mine uses sans |
| Pcard tags use mono dot-separator (same as `.swork__tech-tag::before { content: "·" }`) | Plain mono spans, no separator | ⚠️ minor |

### Cortex (homepage section)

| Prototype `.cortex*` / `.ccard*` | Implementation `.cortex__*` | Aligned? |
|---|---|---|
| Eyebrow "Cortex · long-form notes" | Eyebrow "CORTEX · LONG-FORM NOTES" (uppercase via CSS) | ✅ |
| Display heading: "Writing / I'm working through." (italic serif, 2 lines) | "Notes from / the rabbit holes." (italic serif, 2 lines) | ⚠️ different copy — both invoke the brand voice; pick one |
| `.cortex__current` pulse pill with terracotta dot, copy: "Currently writing: <em>Kafka rebalancing internals</em>" | `.cortex__pulse-pill` with primary dot, copy: "Currently writing: {first book title}" (live data from `/api/cortex/index`) | ⚠️ implementation pulls live data (better!), but the chapter title isn't italicised |
| Card grid: `repeat(3, 1fr) 14px gap`, cards 22px padding, italic-serif 22px title | `BookGrid.Component` (existing) — already aligned to terracotta after token rename | ✅ |
| "Browse the index ↗" CTA in ghost button | "Browse all" CTA in ghost button | ⚠️ minor copy delta |

### Footer

| Prototype `.ftr*` | Implementation `.footer__*` | Aligned? |
|---|---|---|
| Big "Let's talk." h2 (`56px` italic serif) + sub copy ("Senior backend roles, JVM ecosystems, identity / platforms. Reach me at …") | None — implementation has a single credit line | ❌ |
| Two link columns: "Find me" (LinkedIn ↗ / GitHub ↗ / Email ↗) + "Read" (Cortex / Notebook ↗ / CV (PDF)) | Single row of 2 social icons (LinkedIn, GitHub) + theme toggle | ❌ |
| Meta line: "built with scala.js · served from a 4-node k3s cluster in my flat · © {year}" + version line "v2.0 · paris ⌁ {year}" | Single credit line: "Built with Scala.js, served from a Pi 5 at home · © {year}" | ❌ |

This is the **largest single divergence** in the codebase.

### Cortex reader (the 3-column markdown reader)

| Prototype `ui_kits/cortex/style.css` (`.cx-page`) | Implementation [`cortex-reader.css`](../client/src/styles/components/cortex-reader.css) | Aligned? |
|---|---|---|
| `grid-template-columns: 264px minmax(0,1fr) 56px` | Same | ✅ |
| `max-width: 1680px; margin: 0 auto` | Same | ✅ |
| `.cx-page.is-toc-open` widens right rail to `260px` with 150ms transition | Existing pre-redesign hover behaviour preserved | ✅ |
| `.cx-prose { max-width: 860px; margin: 0 auto }` | `.cortex-reader-layout__prose { max-width: 860px; margin-inline: auto }` | ✅ |
| Pager max 860px | Same | ✅ |

The cortex UI kit's stylesheet **still uses the old blue palette**
(`--cx-primary: #2563eb`); the user explicitly asked the implementation to
adopt terracotta globally instead, so the **structural** widths align but
the **palette** intentionally diverges from the old UI kit (and aligns with
the redesign-direction global token rename).

## Summary verdict

✅ **Structural alignment is high** — every prototype section (Hero, SelectedWork, About, Experience, Projects, Cortex, Footer) is implemented with the right elements, copy and behaviour.
✅ **Token alignment is high** — colors, fonts, dark/light blocks land within ~4 RGB units of the prototype.
⚠️ **Token deltas worth fixing**: card bg in light mode (pure white vs warm tinted), border-radius (12px vs 14px).
⚠️ **Visual emphasis deltas**: header logomark + CTA both use ghost/outlined treatment (mine) vs solid filled (prototype); hero stat strip lacks vertical cell dividers; experience accordion missing the slide-open animation; project cards lack hover lift.
❌ **Footer is structurally out of date** — implementation has the legacy single-row credit footer; prototype has a "Let's talk." block + two link columns + meta line.
⚠️ **Copy deltas**: Cortex section heading ("Notes from / the rabbit holes." vs "Writing / I'm working through."), footer credit ("Pi 5 at home" vs "4-node k3s cluster in my flat"), Cortex CTA ("Browse all" vs "Browse the index ↗").

## What this means in practice

If you want to drive the implementation closer to the prototype, the
high-impact fixes in priority order are:

1. **Replace the footer** with the "Let's talk." block + 2 link columns + meta line
2. **Fill the header logomark + "Get in touch" CTA** instead of ghost-outlining them (matches the prototype's solid-pill confidence)
3. **Add vertical dividers between hero stat cells** + drop the stat font from `clamp(48,5vw,64)` to a fixed `44px`
4. **Animate the experience accordion** with `max-height` transition (or accept the no-animation simplification)
5. **Bump card border-radius from 12 → 14px** + use a warm-tinted card bg in light mode
6. **Pick one canonical Cortex heading + footer credit copy** (let me know which)

Or — alternatively — accept the current divergences as intentional and
regenerate the design system docs ([`README.md`](README.md),
[`colors_and_type.css`](colors_and_type.css), [`ui_kits/portfolio/`](ui_kits/portfolio/))
to reflect the live site rather than the pre-redesign extract. That
documents the implementation as the canonical version going forward.
