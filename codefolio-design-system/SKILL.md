---
name: codefolio-design
description: Use this skill to generate well-branded interfaces and assets for Codefolio (kakde.eu, the editorial personal portfolio of Aniket Kakde), either for production or throwaway prototypes / mocks / slides. Contains essential design guidelines, colors, type, fonts, assets, and UI kit components for prototyping in this brand.
user-invocable: true
---

Read the `README.md` file within this skill first — it covers brand context, content tone, the colour palette, typography rules, iconography, and an index of every other file available. Then explore as needed:

- `colors_and_type.css` — design tokens (CSS vars) + semantic typographic classes ready to drop into any HTML file.
- `assets/` — real visual assets: portrait, favicon, brand-mark SVGs (GitHub / LinkedIn / Scala), an offline subset of Lucide icons, the CV PDF, certificate scans.
- `preview/*.html` — small specimen cards for every token and component group (colours light/dark/semantic, type scale, radii & shadows, buttons, badges, hero stats, selected-work rows, project cards, experience accordion). Each is a working HTML snippet you can copy from.
- `ui_kits/codefolio/` — high-fidelity click-thru recreation of the live site (`index.html` + small React/JSX components for header, hero, experience, projects, footer, etc).

If you're creating visual artifacts (slides, mocks, throwaway prototypes), copy the relevant assets out of `assets/` and write static HTML files that import `colors_and_type.css` and the Google-Fonts stylesheet (Instrument Serif + Geist + Geist Mono — see `README.md`). Use Lucide icons from CDN for general UI, the brand-mark SVGs from `assets/icons/` for GitHub / LinkedIn / Scala.

If you're working on the production Scala 3 + Scala.js codebase at `kakde.eu`, you can copy assets and read the rules here to become an expert in designing inside that brand. The Tailwind tokens in `colors_and_type.css` mirror the live `client/tailwind.css` one-to-one.

If the user invokes this skill without further guidance, ask them what they want to build or design, ask 4–10 focused questions (audience, fidelity, variations, the surface they're targeting), and act as an expert designer who outputs HTML artifacts — or production code, depending on the need. Match the brand voice precisely: quiet confidence, plain English, mono captions in `UPPERCASE WITH WIDE TRACKING`, italic serif display headings, terracotta as the only accent, no emoji.
