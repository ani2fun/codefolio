# codefolio-design-system — local mirror

Local snapshot of the Claude Design project at
`https://claude.ai/design/p/019dfdd7-ace0-7d23-accf-89c766d005a6`.

The download URL `https://api.anthropic.com/v1/design/h/qK6q4kBWCJksFAZOetVttg`
returned 404 unauthenticated, so files were extracted via the design tool's
in-browser editor (textarea → clipboard → disk).

## What's here

| Path | What it is |
|---|---|
| `README.md` | Brand bible — voice, palette, type, spacing, hover/press, card anatomy, iconography |
| `SKILL.md` | Agent Skill manifest (`name: codefolio-design`, `user-invocable: true`) |
| `colors_and_type.css` | Token mirror of `client/tailwind.css` (HSL channels) + Tailwind blue/rose/gray scales + semantic role classes (`.h1`, `.eyebrow`, `.tag`, `.body`) |
| `app.jsx` | Redesign prototype entry point — wires sections, theme persistence, tweaks panel |
| `sections.jsx` | **Redesign prototype** — Header / Hero / SelectedWork / About / Experience / Projects / Cortex / Footer |
| `data.jsx` | Hardcoded prototype data (selectedWork, about, experience, projects, certs, cortex picks) |
| `tweaks-panel.jsx` | Floating accent / density / theme tweaker (Terracotta · Forest · Plum · Ink) |
| `ui_kits/portfolio/` | Pixel-recreation of the **original** portfolio (blue palette, "Hi I'm" hero, no italic, no terracotta) |
| `ui_kits/cortex/` | Pixel-recreation of the **original** Cortex reader (blue palette) |

## What's NOT here (and why)

| Missing | Reason |
|---|---|
| `index.html` (root + 2 ui_kits) | The design tool renders these in **Present mode** by default — there's no source-view toggle in the UI. Source can only be reached via the underlying API or by clicking the in-app "Copy" button per file. The structural content of `index.html` is composed from `app.jsx` + `sections.jsx` + `colors_and_type.css`, which I do have. |
| `preview/*.html` (≈22 files) | Small ≤1 KB token-snippet cards used to render thumbnails in the design system's overview tab. Auto-generated; not load-bearing for verification. |
| `assets/*.webp` (13 files) | Binary image assets (portrait, project thumbnails, logos). Equivalent files already live at `client/public/img/portfolio/`. |

If you need the missing files, the cleanest path is the design tool's Share/Export
menu. The fallback I used (clipboard via `navigator.clipboard.writeText` from
the editor's textarea) only works for files that open in the source editor.

## Two systems live in this folder

This is the load-bearing thing the user should know:

- **Baseline** (`README.md`, `SKILL.md`, `colors_and_type.css`, `ui_kits/`) was
  extracted from the codebase **before** the redesign. It documents the live
  site as it was on `main`: blue spine, system fonts, "Hi, I'm Aniket Kakde"
  hero, 4-image About collage, horizontal Experience tab rail.

- **Redesign prototype** (`app.jsx`, `sections.jsx`, `data.jsx`,
  `tweaks-panel.jsx`) was built **after** the design critique. It introduces
  Instrument Serif italic display, Geist + Geist Mono, a terracotta accent,
  warm-cream-on-dark-roast palette, the SelectedWork strip, vertical
  Experience accordion, K3s ASCII art on the featured project card, and the
  homelab-cluster footer one-liner.

The codebase on `redesign/codefolio-design-system` ships the **redesign**.
See `ALIGNMENT.md` for a section-by-section comparison.
