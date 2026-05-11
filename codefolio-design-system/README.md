# Codefolio — Design System

This is the design system for **Codefolio** (kakde.eu) — the editorial personal portfolio of Aniket Kakde, a backend-leaning software engineer based in Paris. The system is extracted from the live codebase, normalised, and re-presented here for use by design agents producing slides, mocks, prototypes, and production work in the same visual language.

## Brand & product context

- **Product**: One single-page editorial portfolio at **kakde.eu**. Sections: Hero · About · Experience · Projects · Selected Work · Cortex (reading shelf) · Blog · Certifications · Footer.
- **Author / subject**: Aniket Kakde — ~10 years on production systems at Europcar, Audi, Disneyland Paris, Dassault Systèmes, UPS, Bell-Labs.
- **Tagline**: *Backend-leaning Software Engineer.* JVM-centric (Kotlin · Java · Scala · ZIO), with the occasional foray into infra (K3s, AWS/GCP) and frontends (Scala.js + scalajs-react).
- **Stack**: Scala 3 + Scala.js + scalajs-react on the frontend (yes — really), Tailwind v4 with shadcn-style HSL tokens, Vite. Self-hosted on the author's K3s homelab.
- **Voice**: Quiet confidence. *"You're looking at it."* Plain spoken, lightly self-deprecating, technically specific.

## Sources

| Source                | Path                                                              |
|-----------------------|-------------------------------------------------------------------|
| Local codebase (live) | `client/` — Scala 3 + Vite SPA, mounted via File System Access    |
| Token table           | `client/tailwind.css`                                             |
| Section CSS           | `client/src/styles/sections/*.css` (BEM-style, in `@layer components`) |
| Component CSS         | `client/src/styles/components/*.css`                              |
| Page data             | `client/src/data/{projectsData,experienceData,certificationsData}.json` |
| Section components    | `client/src/main/scala/codefolio/client/components/sections/*.scala` |
| Icon bindings         | `client/src/main/scala/codefolio/client/components/icons/{LucideIcons,BrandIcons}.scala` |
| Public assets         | `client/public/img/portfolio/`, `client/public/certificates/`     |
| Live site             | https://kakde.eu                                                  |

Treat the codebase as the source of truth; this design system normalises and explains it, but never replaces it.

---

## Content fundamentals

**Voice.** First-person, conversational, technical. Sentences are short and load-bearing. Plain English. Never marketing fluff. The author is the protagonist — *"I"*, *"my"*, sometimes *"you"* when addressing the reader directly (only in the hero/footer CTAs).

**Casing.**
- Section titles, hero name, role companies → display **italic serif**, sentence case (e.g. *About*, *Experience*, *Let's talk.*).
- Eyebrows, dates, role tags, metadata, tags → **UPPERCASE MONO** with wide tracking (`0.16em`).
- Body, paragraphs, bullets → **sentence case**, no capitalisation games.
- Tech names rendered as they are conventionally typeset: `Kotlin`, `Spring Boot`, `OAuth2 / OIDC`, `Scala 3`, `Scala.js`, `ZIO`, `K3s`, `JVM` (not `Jvm`).

**Punctuation.**
- En-dash (`–`) for date ranges: *2022 – Present*.
- Em-dash (`—`) for parenthetical asides: *Backend-leaning Software Engineer — based in Paris.*
- Middle dot (`·`) as a separator in mono meta strings: `2022 – Present · Paris · Backend / Identity`.
- The Oxford comma is fine. Curly quotes always: *"Let's talk."* (`'` not `'`).

**Editorial italic.** A single word or phrase mid-sentence can switch from Geist sans to Instrument Serif italic for a one-word lift — used sparingly, never twice in a row. CSS class: `.about__emphasis` / `.hero__lede em`.

**Vibe.** Editorial magazine, not landing page. The site reads like a designer's CV in print: lots of hairlines, room to breathe, italicised display headings, mono captions like a column-rule. *"Living architecture documentation"* and *"on production systems"* are kinds of phrases that fit here — quietly proud, never breathless.

**No emoji.** None. The codebase has zero. Don't introduce them.

**Examples (taken verbatim from the live site).**

> *"You're looking at it. kakde.eu runs on this — same architecture I used at Audi: Scala 3 end-to-end, Scala.js + scalajs-react on the frontend, ZIO 2 + zio-http on the backend over Postgres, Redis, and Mongo."*

> *"Four-node K3s cluster on commodity hardware sitting at home. Hands-on with the full delivery path — packaging, deployment, networking, observability — for the services I actually use, including this site."*

> *"Backend-leaning Software Engineer based in Paris. Ten years on production systems at Europcar, Audi, Dassault, and Disneyland Paris — Java, Kotlin, Scala, Kafka, PostgreSQL, AWS/GCP."*

> Eyebrow: `PROJECTS · SELECTED` / Meta: `K3S · 2023 · LINUX · GO` / Status: `Currently at Europcar · open to senior backend roles`.

---

## Visual foundations

**Palette.** Warm editorial neutrals with a single saturated accent — *terracotta*.

| Token             | Light       | Dark        | Use                                            |
|-------------------|-------------|-------------|------------------------------------------------|
| `--c-bg`          | `#f7f3ec`   | `#110f0d`   | Page background (warm off-white / dark roast)  |
| `--c-bg-card`     | `#fbf9f3`   | `#1a1715`   | Cards, popovers                                |
| `--c-bg-band`     | `#e8e1d5`   | `#25211f`   | Section bands (Projects section background)    |
| `--c-fg`          | `#292421`   | `#ece2d0`   | Primary ink                                    |
| `--c-fg-muted`    | `#696057`   | `#a59b8c`   | Mono captions, dates, eyebrows                 |
| `--c-fg-soft`     | 85% fg      | 85% fg      | Body copy                                      |
| `--c-border`      | `#dcd4c5`   | `#2e2a26`   | Hairline rules                                 |
| `--c-primary`     | `#c8693e`   | `#db7e54`   | Terracotta accent, hover, single rails         |

There is **one** accent, deliberately. No secondary colour. No gradient backgrounds (the only gradient in the codebase is a `repeating-linear-gradient` of 1px stripes on project-card placeholders).

**Typography.** Three families, strict roles:

- **Instrument Serif** — display headings, hero name, section titles, company names in Experience, *editorial italic* mid-sentence accents. **Always italic, 400 only. Never roman, never bold.**
- **Geist** — sans body, CTAs, nav, fact-list values. Weights 400/500/600/700.
- **Geist Mono** — eyebrows, dates, tags, metadata, status pills, card icons. Weights 400/500.

Display scale is `clamp()`-driven for fluid sizes. Mono is set at `11px / 0.16em` tracking for caption use, `13px / 0.04em` for inline meta.

**Spacing & rhythm.** Tailwind v4 spacing scale (4px base). Section padding is `py-24 px-6`. Max widths: `max-w-6xl` for hero/projects/footer, `max-w-5xl` for experience, `max-w-prose` for body copy.

**Radii.** Restrained.
- `--r-sm = 10px` — chips, small cards
- `--r-md = 12px` — buttons, inputs
- `--r-lg = 14px` — cards (`rounded-lg` in shadcn)
- `--r-pill = 9999px` — status pills, CTA buttons, filter chips, badges

**Borders & rules.** This system *runs on hairlines.* `1px solid hsl(var(--border))` everywhere — at section tops/bottoms, between stat-strip cells, between accordion rows, between fact-list rows. Often softened to `border-border/60` for whisper-quiet rules. Borders do more work than backgrounds here.

**Shadows.** Two only:
- **Resting** — `0 1px 0 rgba(0,0,0,.05), 0 1px 2px rgba(0,0,0,.04)` — a 1px ledge.
- **Hover-lift** — `0 1px 0 rgba(0,0,0,.05), 0 8px 24px -12px rgba(0,0,0,.18)` — long soft drop, only on interactive cards.

No inner shadows. No coloured shadows. No "glow".

**Backgrounds.**
- Plain warm bg, no images, no patterns.
- One full-bleed warm band (`--c-bg-band`) on the Projects section.
- A *single* texture: SVG fractal noise at 4% opacity, `mix-blend-mode: multiply`, applied as a `body::before` overlay. Reads as paper grain, not visible as noise.
- Project-card placeholders are a soft 135° linear-gradient over a `repeating-linear-gradient` of 1px stripes — for when no photo exists.

**Animation.** Quiet and physical.
- Status-pill dot pulses on `cubic-bezier(0.4, 0, 0.2, 1)` over 2.4s. Respects `prefers-reduced-motion`.
- Hover transitions are 200–220ms, easing `cubic-bezier(0.2, 0.7, 0.2, 1)` (gentle deceleration).
- Underlines slide in `scaleX(0) → scaleX(1)` from `transform-origin: left` on nav hover.
- Cards lift `-3px` on hover with the long shadow.
- Buttons translate `-1px` on hover. No bounce. No spring.
- Accordion chevrons rotate 90° on open with the same easing.

**Hover states.** Slight darken or terracotta tint. *Never* a colour shift on the whole element — usually only the underline / border / icon picks up `--primary`. Outlined buttons get `border-foreground` on hover (the existing border darkens, doesn't change colour).

**Press states.** No explicit press style — focus rings are the default Tailwind `ring`, terracotta-tinted via `--ring`. Buttons don't scale on press.

**Transparency & blur.** Used only twice:
- Header chrome: `bg-background/85 backdrop-blur` so the top nav sits softly over scrolling content.
- Card surfaces: occasional `bg-card/40` for very subtle layering (status pill).

**Cards.** `border border-border/70 bg-card rounded-lg`. Hover adds `border-primary/50` + the long shadow + `translateY(-3px)`. They do **not** have heavy fills; the warm cream surface against the warm bg is enough.

**Layout rules.**
- Header is fixed top with a hairline bottom border. Sections push `py-24` and use `scroll-mt-24` so anchor scrolls don't hide behind the header.
- Content is centred in `max-w-6xl mx-auto` containers with `px-6` gutters.
- The Hero stat-strip is a 4-cell grid with internal vertical rules — collapses to 2×2 on mobile with a mid-row horizontal rule.
- The About section is a 12-col grid: 7-col prose / 5-col sticky sidecard.
- The Selected-Work row is a 5-col flex with a left rail that scales in vertically on hover.

**Imagery vibe.** Warm, slightly desaturated. The portrait gets `filter: saturate(0.92) contrast(1.02)`. Photos sit in `rounded-lg` frames with a hairline border. There are very few photos — the system is mostly typographic.

---

## Iconography

**Two icon systems.** Both live in `client/src/main/scala/codefolio/client/components/icons/`.

### 1. Lucide (general UI)
The codebase uses **lucide-react** for all general UI icons — typed bindings in `LucideIcons.scala`. Stroke icons, **1.5px stroke** by default (Tailwind's `[&_svg]:stroke-[1.5]`), 16–20px sizing.

Used icons (verbatim from the codebase):
`Sun · Moon · Menu · X · ArrowRight · ArrowLeft · ArrowUp · BookOpen · Loader2 · Play · Pause · RotateCcw · Square · Maximize2 · ZoomIn · ZoomOut · Check · Copy · ChevronRight · ChevronDown · ListTree · Heart · Star · Trophy · ExternalLink · Search · Download · Pencil`

When designing in HTML outside the Scala app, link Lucide from CDN:

```html
<script src="https://unpkg.com/lucide@latest"></script>
<i data-lucide="arrow-right" class="h-4 w-4"></i>
<script>lucide.createIcons();</script>
```

### 2. Brand marks
Hand-curated Simple-Icons paths in `BrandIcons.scala`. Currently exported: **GitHub**, **LinkedIn**, **Scala**. Single-path SVGs at `viewBox="0 0 24 24"`, `fill="currentColor"`. Inline SVG, no CDN.

Copied as standalone SVGs into `assets/icons/`:
- `assets/icons/github.svg`
- `assets/icons/linkedin.svg`
- `assets/icons/scala.svg`
- `assets/icons/sun.svg`, `moon.svg`, `arrow-right.svg`, `external-link.svg`, `chevron-down.svg`, `download.svg`, `book-open.svg`, `check.svg`, `menu.svg`, `x.svg` (Lucide subset for offline use)

### Rules
- **No emoji.** Anywhere. Ever.
- **No unicode glyphs as icons** (no `↗`, no `→`, no `★`). The single exception is the footer-link arrow `↗` which is rendered as `font-mono text-muted-foreground`-styled text — and Lucide `ArrowUpRight` is preferred for new work.
- Stroke icons match the brand's hairline aesthetic — **never use fill-style icons** for UI.
- Brand marks (GitHub, LinkedIn, Scala) are the **only** filled SVGs allowed, and only for their own brand.
- If an icon isn't in Lucide and isn't a brand, look for the nearest Lucide match before reaching for another set.

---

## Index

Root of this design system:

| Path                                 | What's there                                            |
|--------------------------------------|---------------------------------------------------------|
| `README.md`                          | You are here.                                           |
| `SKILL.md`                           | Agent skill manifest (cross-compatible with Claude Code).|
| `colors_and_type.css`                | Tokens + semantic typographic classes (`.ds-display-xl`, `.ds-eyebrow`, `.ds-em`, …). |
| `assets/`                            | Logos, portrait, certificates, downloadable CV, icon SVGs. |
| `assets/portrait.webp`               | Portrait photo (4:5).                                    |
| `assets/favicon.webp`                | Site favicon — the brand glyph.                          |
| `assets/icons/*.svg`                 | Brand marks + offline Lucide subset.                     |
| `assets/Aniket-Kakde-CV-EN.pdf`      | The CV PDF served from the live site.                    |
| `assets/certificates/*.jpg`          | Certificate scans (Udemy etc).                           |
| `preview/`                           | Design-system preview cards (registered for the Design System tab). |
| `ui_kits/codefolio/`                 | Click-thru recreation of the live site.                  |

### Font substitution (caveat)
The codebase loads **Instrument Serif**, **Geist**, and **Geist Mono** from **Google Fonts** at runtime — no local TTFs are checked in. The preview cards and UI kit do the same. If you need offline / PPTX-embedded fonts, please drop the TTFs into `fonts/` and update `colors_and_type.css`. Otherwise the system substitutes from Google Fonts.

