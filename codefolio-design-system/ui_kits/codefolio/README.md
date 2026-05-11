# Codefolio UI Kit

A hi-fi click-thru recreation of [kakde.eu](https://kakde.eu) — the live portfolio — rebuilt as a single static HTML page with small React/JSX components, no build step. Mirrors the typography, palette, hairlines, and interactions of the production Scala 3 + Scala.js codebase.

## What's here

- `index.html` — open this. Mounts the whole click-thru.
- `styles.css` — section/component CSS rolled up from the live `client/src/styles/sections/*.css` files.
- `Header.jsx` — fixed top nav (logomark + wordmark + menu + theme toggle + CTA pill).
- `Hero.jsx` — landing block: live-status pill, italic display name, lede with editorial italic, primary/ghost CTAs, four-cell stat strip.
- `About.jsx` — italic-quote prose column + sticky portrait/fact-list sidecard.
- `Experience.jsx` — vertical accordion of roles (Europcar, Audi, Disneyland Paris) with primary-tag chips, bullets, "Results" pull-quote, and demoted "Also" tags.
- `SelectedWork.jsx` — large italic-company rows with a terracotta rail that scales in on hover.
- `Projects.jsx` — filterable 3-up card grid. Featured card uses inline ASCII art for the K3s homelab; others use the tinted-stripe placeholder.
- `Footer.jsx` — "Let's talk." display heading + two link columns + mono meta strip.
- `Icon.jsx` — Lucide stroke-icon subset + GitHub/LinkedIn/Scala brand marks.

## What it covers

- Section transitions: hairline rules at every section boundary, scroll-snap top offset for the fixed header.
- Theme toggle: light/dark via `.dark` class on `<html>` (toggled by the header button) — token table in `colors_and_type.css` flips the whole palette.
- Hover/press: nav-underline slide, project-card lift, work-row rail scale-in, CTA-icon translate.

## What's intentionally omitted

- The **Cortex** reader (markdown-driven reading shelf), Mermaid/D2 diagrams, runnable code blocks, and the blog. These are content surfaces, not UI kit specimens — the live codebase remains the source of truth.
- Certifications grid (just three cards from real Udemy certificates — not needed for a design kit).
- Mobile drawer / burger menu. Desktop nav only.
- Real text content beyond what's visible in `client/src/data/*.json`. Nothing invented.
