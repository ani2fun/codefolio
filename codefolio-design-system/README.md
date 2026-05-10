# Codefolio Design System

Aniket Kakde's personal portfolio + **Cortex** (long-form technical notes / knowledge library) — a single Scala.js + React SPA self-hosted on a homelab K3s cluster at **kakde.eu**.

The site doubles as a portfolio (Hero / About / Experience / Projects / Certifications / Footer) and a reader for Markdown books with diagrams, math, and runnable code blocks. This design system extracts the live tokens, fonts, components, and brand language out of that codebase so we can prototype against it.

---

## Sources

| What | Where |
| ---- | ----- |
| Codebase (read-only mount) | `client/` — Scala.js + React 19 + Tailwind v4 + shadcn HSL tokens |
| Live site | <https://kakde.eu> |
| Cortex live | (subdomain — also on the homelab cluster) |
| Notebook (mdBook, sibling project) | <https://notebook.kakde.eu> |
| Resume PDF | `client/public/Aniket-Kakde-CV-EN.pdf` |

The codebase uses Tailwind v4 with the standard shadcn variable set (HSL channels in `:root`/`.dark`) plus BEM-style component classes per section, all under `client/src/styles/`.

---

## Products represented

1. **Portfolio (homepage)** — single-page scroll: Hero → About → Experience → Projects → Certifications → Cortex preview → Footer. Sticky pill nav with hash anchors. Dark mode toggle in nav and footer.
2. **Cortex reader** — three-column layout (sidebar / main / right-rail TOC minimap). Renders Markdown via a Scala.js renderer (`MarkdownRenderer`), with `rehype-pretty-code` syntax highlighting, KaTeX math, Mermaid + D2 diagrams, and runnable code blocks. The right-rail minimap shows ticks for headings; hovering reveals the full TOC panel.

---

## Index

- `README.md` — this file (context, content fundamentals, visual foundations, iconography)
- `SKILL.md` — Agent Skill manifest for `codefolio-design`
- `colors_and_type.css` — base + semantic CSS variables and `.h1` / `.body` / `.tag` / `.eyebrow` roles
- `assets/` — logos, portrait collage, project thumbnails (all `.webp`)
- `preview/` — small HTML cards for the Design System tab (one per token group / component / state)
- `ui_kits/portfolio/` — pixel-recreation of the portfolio homepage as JSX components + `index.html`
- `ui_kits/cortex/` — pixel-recreation of the Cortex reader (sidebar / chapter / TOC) as JSX components + `index.html`

---

## Content fundamentals

The voice is **first-person, dry, pragmatic, technical**. No marketing puff, no emoji, no exclamation marks. It reads like a senior engineer writing their own bio.

**Pronouns.** Almost always "I" / "my" — not "we", not "you". Bio paragraphs lead with the subject, never with adjectives.

> "Backend-leaning Software Engineer with **ten years** on production systems."
> "I run a small four-node K3s cluster on commodity hardware at home and self-host my own services on it — including this site."

**Concreteness over claims.** Specifics replace adjectives:

> "Modernised the team's Gradle build setup with multi-level CI caching, cutting build times by ~50% (≈12 min → ≈5 min) — pattern adopted as the internal reference by 3+ adjacent JVM teams."

Tech stacks are listed inline with commas, never as bullet salads in prose: *"Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP."* Dedicated **Stack:** rows in the Experience pane do the chip-list version.

**Casing.** Sentence case for headings (`About`, `Experience`, `Projects`, `Cortex`). Short eyebrow labels are UPPERCASE with wide tracking (`STACK`, `RESULTS`, issuer / date metadata).

**Em dashes** (`—`) and **en dashes** (`–`) are used freely, especially for date ranges and parenthetical asides. ASCII hyphens are not substituted.

**Punctuation & accent.**
- British / European spellings — *"modernised"*, *"organisation"*, *"specialise"*. Aniket is based in Paris.
- French diacritics retained: *"Diplôme Data Engineer"*, *"Grande École"*.
- Two-space dashes around clauses, e.g. *"hands-on with the full delivery path — packaging, deployment…"*.
- "**Cortex**" is a proper noun (always capitalised, never italicised).

**Tone words / vibe.** *backend-leaning · production · pragmatic · hands-on · self-hosted · long-form · rabbit holes · day-to-day*.

**Tone words to avoid.** *passionate · journey · empower · unlock · seamless · synergy · cutting-edge*. No emoji 🚫. No 🔥. Star emoji and decorative unicode are not used.

**CTA copy is bare.** "Get in touch", "Download CV", "Browse all", "View". No verbs like *"Discover"*, no exclamation marks.

**Cortex blurb sets the reader's expectation:**

> "Long-form notes from books, courses, and rabbit holes. Click any topic to start reading."

That's the brand voice in one sentence: long-form, plural sources, casual ("rabbit holes"), and immediately gives the reader an action.

---

## Visual foundations

### Palette

- **Primary blue** is the spine of the brand. Light mode lands on `--blue-700` (#1d4ed8) for headings and `--blue-600` for buttons; dark mode lifts to `--blue-400`. Backgrounds for selected/hovered states use `--blue-50` (light) or `--gray-900` (dark).
- **Rose accent** (`--rose-500`) is used **sparingly** — dates, the experience `time` line, the Diploma badge, the footer "Made with ♥" heart, and the highlighted certification marker. Never as a primary surface colour.
- **Greens** appear once (`Europcar International` emphasis in About). Otherwise neutral.
- Cards in light mode are pure white with a 1px `--blue-500` border. In dark mode they switch to `--slate-900` with a near-white border.

### Type

- **Single family.** The codebase ships zero webfonts. It uses Tailwind defaults: `font-sans` (system stack) and `font-mono` (ui-monospace). No display / serif. We mirror this in `colors_and_type.css`.
  - **Substitution flagged:** if a prototype renders on a device whose system stack is ugly, fall back to **Inter** for sans and **JetBrains Mono** for mono — both close in metric to the macOS / Windows defaults the live site renders against.
- **Weights actually used.** 400 (regular), 600 (semibold), 700 (bold). Nothing lighter, nothing heavier.
- **Scale is large at the top.** Hero name hits `text-6xl` (60px) at md+. Section h2s are `text-5xl` (48px). Body is `text-lg`–`text-2xl` (18–24px) — much larger than typical SaaS sites — which is part of the "long-form readable" feel.
- **Eyebrow pattern.** 11–12px, semibold, uppercase, `tracking-wider`. Used for issuer / metadata rows under titles.

### Spacing & rhythm

- 4px base unit (Tailwind). `gap-2`, `gap-4`, `gap-8` carry most layouts.
- Sections use `min-h-screen` + generous `pt-32` (128px) so each lands cleanly when hash-jumped to.
- Cards use `p-5`/`p-6` consistently. Tag chips use `px-2 py-1`.

### Radii

- Cards / buttons / sections: `rounded-md` (10px) and `rounded-lg` (12px). Tags get `rounded` (4px). Pills are full-rounded only on the scroll-to-top FAB.
- The `--radius` token is `0.75rem`; `--radius-md` and `--radius-sm` derive from it.

### Shadows

Three tiers:

- `shadow-md` — every section block and most cards. Soft, generic.
- `shadow-lg` — only on the floating buttons (scroll-to-top, mobile sidebar opener).
- A single coloured shadow exists: the highlighted certification marker, with a `shadow-rose-500/30` glow.

### Borders

- 1px `border-blue-500` is the default card outline in light mode; in dark, `border-gray-50`.
- The Experience rail uses a thicker `border-2 border-blue-600` to register as a tab strip.
- The certifications timeline spine is a vertical 2px gradient (`from-blue-600 via-blue-500 to-blue-300`).
- Default body border colour comes from the shadcn `--border` token.

### Backgrounds

- **No gradient hero.** The hero is plain background with an outlined card.
- **No full-bleed photography.** Photos live inside the About collage at small/medium sizes, and as project thumbnails (`/img/...`) inside cards. Each collage tile carries a translucent `bg-blue-200/50` overlay for a unified tint.
- **No texture / grain / repeating pattern.** Surfaces are flat colour.
- **One coloured spine** (the certifications timeline), one coloured chip cluster (tags). That's the extent of decoration.

### Hover & press

- Links and chips flip text colour to `--primary` (`hover:text-primary`) — never just opacity-down.
- Project social-icon hovers use `hover:scale-125 transition-transform duration-100` — a sharp, snappy 100ms zoom.
- Buttons swap fill: a primary button goes from `bg-blue-600 text-gray-50` to `bg-gray-50 text-slate-900` on hover (an inversion, not a tint).
- Cards flip background to `--blue-50` (light) / `--gray-900` (dark). No translation, no scale on cards.
- The "next chapter" hover in the Cortex reader changes border colour, not background.
- Pressed states are not custom-styled beyond the browser default — focus rings come from `focus-visible:ring-2 ring-ring`.

### Animation

- `transition-colors` and `transition-transform` are everywhere; `transition-all` is rare.
- Durations: `duration-100` (icon zoom), `duration-150` (rail TOC), `duration-200` (scroll-to-top fade), `duration-300` (CTA buttons).
- No bounce easings, no spring physics. Default `ease`. **No keyframe animations** beyond Tailwind's built-in `animate-pulse` on the footer heart.

### Layout rules

- Sticky top header (`fixed top-0`, `z-50`, 95% opacity white / dark background).
- One sticky bottom-right scroll-to-top FAB (`fixed bottom-5 right-5`).
- Content max-width via Tailwind's `container` (and per-section caps like `max-w-6xl` on the Cortex grid, `max-w-3xl` on certifications, `max-w-2xl` on subtitles).
- Three-column reader (sidebar / main / right-rail TOC) with `[280px_minmax(0,1fr)_64px]` grid template.

### Transparency & blur

- The header sits at `opacity-95` on solid white — light blur effect via the layered z-index stack, not via `backdrop-filter`. **No backdrop blur is used.**
- The drawer overlay is `bg-black/40` (40% black, no blur).
- The about photo overlay is `bg-blue-200/50` light / `bg-blue-600/50` dark.

### Card anatomy (canonical)

```
┌──────────────────────────────────────┐  ← 1px blue-500 border, rounded-lg
│  [optional 192px-tall image]         │     no shadow on internal cards;
│                                      │     section blocks add shadow-md
│  [icon-row]              ← w-7 h-7   │
│                                      │
│  Card Title                          │  ← text-xl semibold blue-700
│  description body, gray-800          │  ← text-base, flex-1 to fill
│                                      │
│  [tag][tag][tag]            View →   │  ← bg-blue-200 chips,
└──────────────────────────────────────┘     rose-500 right-aligned cta
```

---

## Iconography

The codebase has **two** icon sources:

1. **Lucide React** — pulled directly via `import { Sun } from 'lucide-react'`. Used for every functional UI icon. The Scala.js wrapper (`LucideIcons.scala`) explicitly enumerates the subset in use:
   - `Sun`, `Moon`, `Menu`, `X`, `ArrowRight`, `ArrowLeft`, `ArrowUp`, `BookOpen`, `Loader2`, `Play`, `Pause`, `RotateCcw`, `Square`, `Maximize2`, `ZoomIn`, `ZoomOut`, `Check`, `Copy`, `ChevronRight`, `ChevronDown`, `ListTree`, `Heart`, `Star`, `Trophy`, `ExternalLink`.
2. **Brand SVGs** — only **GitHub** and **LinkedIn** glyphs (Simple-Icons paths), drawn inline with `fill="currentColor"`. Defined in `BrandIcons.scala`. These are the only non-Lucide icons in the codebase.

**Defaults.** Lucide stroke icons inherit `currentColor` and `stroke-width: 2` (Lucide's default). Sizes come from the surrounding className (`h-4 w-4` for inline meta, `h-5 w-5` for buttons, `h-7 w-7` for project icons, `h-8 w-8` for footer socials). The codebase **never** sets `size` numerically — always via Tailwind classes.

**No emoji.** Anywhere. Not in copy, not in icons, not in tag labels. The footer "Made with ♥" uses the Lucide `Heart` glyph filled with `currentColor` (`[fill:currentColor]` modifier), not a unicode emoji. The certification highlight uses the Lucide `Star` (also `[fill:currentColor]`); non-highlighted certs use `Trophy` outlined.

**No icon font.** Lucide is shipped per-component via tree-shaking; there's no consolidated SVG sprite or font file in `client/public/`.

**For prototypes in this design system,** load Lucide via CDN: `<script src="https://unpkg.com/lucide@latest"></script>` then `lucide.createIcons()`. Keep the same restricted vocabulary listed above; if a new icon is needed, **prefer adding it from Lucide** before reaching for any other set.

**Substitutions flagged.** None — Lucide is freely CDN-available, and the GitHub / LinkedIn marks are inlined in `ui_kits/portfolio/BrandIcons.jsx`.

---

## Iterating

- The system inherits Tailwind's defaults wholesale. If a prototype needs a token that's not here, look first in Tailwind v3/v4 docs, then the project's own `tailwind.css`.
- When in doubt about how a section should look in dark mode, the rule is: text → near-white, surfaces → `slate-900` / `gray-900`, primary blue lifts one shade lighter (`blue-400`).
- Want a non-blue brand variant? Override `--primary` and `--color-text-accent` only — the rest of the system follows.
