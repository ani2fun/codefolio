#!/usr/bin/env python3
"""Import the note-book DSA tree into cortex as a single book.

Reads:  ~/Development/homelab/note-book/src/computer-science/DSA/
        {data-structures,algorithms}/<N.subtopic>/<NN.chapter>.md
Writes: <repo>/content/cortex/data-structures-and-algorithms/<topic>/<NN-subtopic>/<NN-chapter>.md
        <repo>/content/cortex/data-structures-and-algorithms/book.json
        <repo>/content/cortex/data-structures-and-algorithms/<topic>/_section.json

Behaviour:
- Strips numeric+separator prefixes (e.g. "1.arrays") and re-prefixes with
  zero-padded "01-arrays" form for stable lex ordering on disk.
- Mirrors the server's chapter-slug derivation so the import-time uniqueness
  check matches what CortexHandler will compute at runtime.
- Best-effort rewrites internal `[text](relative.md)` links to
  `/cortex/data-structures-and-algorithms/<derived-slug>`. Unresolved links
  become `[text](#)` with a `<!-- TODO unresolved-link: ... -->` marker.
- Tags every fenced code block whose info string starts with a
  Piston-supported language alias (read from Languages.scala) with ` run`
  so cortex's runnable-code component picks it up.
- Idempotent: wipes the destination directory at the start of each run.
"""

from __future__ import annotations

import json
import re
import shutil
import sys
from dataclasses import dataclass
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SOURCE_ROOT = Path.home() / "Development/homelab/note-book/src/computer-science/DSA"
DEST_BOOK_SLUG = "data-structures-and-algorithms"
DEST_ROOT = REPO_ROOT / "content/cortex" / DEST_BOOK_SLUG
LANGUAGES_FILE = REPO_ROOT / "server/src/main/scala/codefolio/server/runner/Languages.scala"

# Source dir name -> destination dir name (numeric-prefixed so the topic
# ordering on the cortex sidebar matches the book's title: Data Structures
# first, Algorithms second).
TOP_LEVEL_DIRS = [
    ("data-structures", "01-data-structures"),
    ("algorithms", "02-algorithms"),
]
TOP_LEVEL_TITLES = {
    "01-data-structures": "Data Structures",
    "02-algorithms": "Algorithms",
}

PREFIX_RE = re.compile(r"^\d+[._-]?")
LEADING_NUMBER_RE = re.compile(r"^(\d+)")
H1_RE = re.compile(r"^#\s+(.+?)\s*$", re.MULTILINE)
LINK_RE = re.compile(r"\[([^\]]*)\]\(([^)\s]+?)(?:\s+\"[^\"]*\")?\)")
FENCE_RE = re.compile(r"^(`{3,})([^\n]*)$")


# ---------- Slug + title derivation (mirrors CortexHandler) ----------------

def strip_order_prefix(name: str) -> str:
    return PREFIX_RE.sub("", name, count=1)


def humanise(name: str) -> str:
    cleaned = strip_order_prefix(name).removesuffix(".md")
    parts = re.split(r"[-_.]+", cleaned)
    return " ".join(p[:1].upper() + p[1:].lower() for p in parts if p)


def slugify_segment(seg: str) -> str:
    out: list[str] = []
    last_dash = False
    for c in seg:
        if c.isalnum():
            out.append(c.lower())
            last_dash = False
        elif c == "_":
            out.append("_")
            last_dash = False
        elif not last_dash and out:
            out.append("-")
            last_dash = True
    s = "".join(out)
    return s.rstrip("-")


def chapter_slug_from_path(rel_path_in_book: str) -> str:
    """rel_path_in_book like 'data-structures/01-arrays/02-traversal.md'"""
    no_ext = rel_path_in_book.removesuffix(".md")
    segs = [slugify_segment(strip_order_prefix(p)) for p in no_ext.split("/") if p]
    return "-".join(s for s in segs if s)


# ---------- Piston language list -----------------------------------------

def load_runnable_aliases() -> set[str]:
    text = LANGUAGES_FILE.read_text()
    aliases: set[str] = set()
    for m in re.finditer(r'aliases\s*=\s*Seq\(([^)]+)\)', text):
        for sub in re.finditer(r'"([^"]+)"', m.group(1)):
            aliases.add(sub.group(1).lower())
    if not aliases:
        sys.exit(f"failed to parse aliases from {LANGUAGES_FILE}")
    return aliases


# ---------- Source walk ---------------------------------------------------

@dataclass
class Chapter:
    src_path: Path           # absolute path to the source .md
    rel_in_book: str         # destination path relative to the book root
    slug: str                # derived chapter slug
    title: str               # derived display title


def order_key(name: str) -> tuple[int, str]:
    m = LEADING_NUMBER_RE.match(name)
    return (int(m.group(1)) if m else 10**9, name.lower())


def renumber(name: str) -> str:
    """Strip any source-side prefix and re-add a zero-padded one based on
    the original ordering number, so disk layout sorts by lex AND matches
    server's prefix-stripping logic. Preserves the `.md` extension (only that
    one — directory names like `5.stack` have a literal dot in them, not an
    extension, and must be normalised to hyphens).
    """
    m = LEADING_NUMBER_RE.match(name)
    if not m:
        return name
    num = int(m.group(1))
    if name.endswith(".md"):
        base, ext = name[: -len(".md")], ".md"
    else:
        base, ext = name, ""
    rest = strip_order_prefix(base)
    rest = rest.lstrip(".-_ ")
    rest = rest.replace(".", "-").replace(" ", "-")
    return f"{num:02d}-{rest}{ext}"


def first_h1(body: str) -> str | None:
    m = H1_RE.search(body)
    return m.group(1).strip() if m else None


def collect_chapters() -> list[Chapter]:
    chapters: list[Chapter] = []
    for src_top, dest_top in TOP_LEVEL_DIRS:
        top_dir = SOURCE_ROOT / src_top
        if not top_dir.is_dir():
            sys.exit(f"missing source dir: {top_dir}")
        # Topic-level intro files (e.g. data-structures/index.md) become
        # chapters at the topic root with no sub-section.
        for f in sorted(top_dir.iterdir(), key=lambda p: order_key(p.name)):
            if not f.is_file() or not f.name.endswith(".md"):
                continue
            # Force these to sort first within the topic by giving them
            # a 00- prefix in the destination filename.
            base = f.name.removesuffix(".md")
            file_dest = f"00-{slugify_segment(base) or 'overview'}.md"
            rel = f"{dest_top}/{file_dest}"
            slug = chapter_slug_from_path(rel)
            body = f.read_text(encoding="utf-8", errors="replace")
            title = first_h1(body) or humanise(base)
            chapters.append(Chapter(src_path=f, rel_in_book=rel, slug=slug, title=title))
        # subtopic dirs (e.g. 1.arrays)
        for sub in sorted(top_dir.iterdir(), key=lambda p: order_key(p.name)):
            if not sub.is_dir():
                continue
            sub_dest = renumber(sub.name)  # 1.arrays -> 01-arrays
            for f in sorted(sub.iterdir(), key=lambda p: order_key(p.name)):
                if not f.is_file() or not f.name.endswith(".md"):
                    continue
                file_dest = renumber(f.name)
                if not file_dest.endswith(".md"):
                    file_dest += ".md" if not file_dest.endswith(".md") else ""
                rel = f"{dest_top}/{sub_dest}/{file_dest}"
                slug = chapter_slug_from_path(rel)
                body = f.read_text(encoding="utf-8", errors="replace")
                title = first_h1(body) or humanise(f.name)
                chapters.append(Chapter(src_path=f, rel_in_book=rel, slug=slug, title=title))
    return chapters


# ---------- Body transformation -------------------------------------------

def unwrap_d2_vars(body: str) -> str:
    """The note-book corpus uses `vars: { ... }` as the *only* content of
    many d2 fences (a holdover from a custom mdbook plugin). The standard
    D2 compiler treats `vars:` as a variable-declaration block and renders
    nothing — leaving an empty SVG on the page. Unwrap those bodies so the
    inner declarations become top-level nodes (which is clearly what the
    author meant: see the trailing caption "Using variables to store…").
    """
    out_lines: list[str] = []
    in_fence = False
    fence_marker = ""
    fence_lang = ""
    buf: list[str] = []
    for line in body.split("\n"):
        m = FENCE_RE.match(line)
        if m and not in_fence:
            in_fence = True
            fence_marker = m.group(1)
            fence_lang = m.group(2).strip().split()[0] if m.group(2).strip() else ""
            buf = []
            out_lines.append(line)
            continue
        if in_fence:
            if line.startswith(fence_marker) and line.strip() == fence_marker.rstrip():
                # Decide whether to unwrap before flushing.
                if fence_lang == "d2":
                    inner = "\n".join(buf).strip()
                    # Match `vars: {` ... matching `}` covering the whole body.
                    if inner.startswith("vars:"):
                        # Find the opening `{` and the matching closing `}`.
                        open_idx = inner.find("{")
                        if open_idx > 0 and inner.rstrip().endswith("}"):
                            depth = 0
                            close_idx = -1
                            for i in range(open_idx, len(inner)):
                                if inner[i] == "{":
                                    depth += 1
                                elif inner[i] == "}":
                                    depth -= 1
                                    if depth == 0:
                                        close_idx = i
                                        break
                            if close_idx == len(inner.rstrip()) - 1:
                                unwrapped = inner[open_idx + 1 : close_idx].strip("\n")
                                # Re-indent: original `vars:` body is indented
                                # by 2; strip that consistent prefix so the
                                # output is flush-left.
                                lines = unwrapped.split("\n")
                                stripped = [
                                    ln[2:] if ln.startswith("  ") else ln for ln in lines
                                ]
                                buf = stripped
                out_lines.extend(buf)
                out_lines.append(line)
                in_fence = False
                fence_lang = ""
                buf = []
                continue
            buf.append(line)
            continue
        out_lines.append(line)
    return "\n".join(out_lines)


def tag_runnable_fences(body: str, runnable: set[str]) -> str:
    """Normalise fence info strings so cortex's runnable-code path picks them up.

    The source mdbook uses ``` `python,editable` `` (a custom mdbook plugin
    convention). Cortex expects ``` `python run` ``. Normalisation rules:
      1. If info is `<lang>,editable` (or `<lang>,...`) and `<lang>` is
         Piston-supported, rewrite to `<lang> run`.
      2. If info is just `<lang>` and `<lang>` is supported, append ` run`.
      3. Otherwise leave the fence alone (so mermaid, d2, plain code remain).
    """
    out: list[str] = []
    in_fence = False
    fence_marker = ""
    for line in body.split("\n"):
        m = FENCE_RE.match(line)
        if m and not in_fence:
            fence_marker = m.group(1)
            info = m.group(2).strip()
            # First token, splitting on whitespace OR `,` so `python,editable`
            # parses as language=python, rest=["editable"].
            head = re.split(r"[,\s]+", info, maxsplit=1)
            lang = head[0].lower()
            if lang in runnable:
                line = f"{fence_marker}{lang} run"
            in_fence = True
        elif m and in_fence and line.startswith(fence_marker):
            in_fence = False
        out.append(line)
    return "\n".join(out)


def strip_lang_tabs_wrappers(body: str) -> str:
    """The source markdown wraps groups of fences in `<div class="lang-tabs">…</div>`
    (a custom mdbook plugin), which our markdown pipeline doesn't need —
    consecutive runnable fences merge into a tab group automatically. Worse,
    the raw HTML can confuse remark's fence parser when blank lines aren't
    consistent. Strip the wrappers; keep the fences.
    """
    body = re.sub(r"<div class=\"lang-tabs\">\s*\n", "", body)
    # Remove the closing </div> only when it's on its own line and immediately
    # follows a runnable code-fence block (matched by a closing ``` on the
    # previous line). To stay simple, drop any standalone </div> line.
    body = re.sub(r"\n</div>\s*(?=\n)", "\n", body)
    return body


def rewrite_links(body: str, src_path: Path, src_to_slug: dict[Path, str]) -> tuple[str, int, int]:
    """Best-effort rewrite of `[text](relative.md)` links to cortex URLs.

    CRITICAL: skip lines that live inside a fenced code block — Scala's
    `Array[Int](5)` and friends look exactly like markdown links and would
    otherwise get rewritten in place. Inline backtick spans are also masked
    so a line like `Use `vec[0]` to access` doesn't trip the same trap.
    """
    resolved = 0
    unresolved = 0

    def repl(m: re.Match[str]) -> str:
        nonlocal resolved, unresolved
        text, href = m.group(1), m.group(2)
        if href.startswith(("http://", "https://", "mailto:", "#", "/")):
            return m.group(0)
        anchor = ""
        link = href
        if "#" in link:
            link, _, anchor_part = link.partition("#")
            anchor = f"#{anchor_part}"
        if not link or link.endswith(("/", ".md")):
            try:
                target = (src_path.parent / link).resolve()
            except (OSError, ValueError):
                unresolved += 1
                return f"[{text}](#)<!-- TODO unresolved-link: {href} -->"
            if link.endswith("/"):
                target = target / "index.md"
            elif target.is_dir():
                target = target / "index.md"
            if target in src_to_slug:
                resolved += 1
                return f"[{text}](/cortex/{DEST_BOOK_SLUG}/{src_to_slug[target]}{anchor})"
        unresolved += 1
        return f"[{text}](#)<!-- TODO unresolved-link: {href} -->"

    out: list[str] = []
    in_fence = False
    fence_marker = ""
    for line in body.split("\n"):
        m = FENCE_RE.match(line)
        if m and not in_fence:
            in_fence = True
            fence_marker = m.group(1)
            out.append(line)
            continue
        if in_fence:
            if line.startswith(fence_marker) and line.strip() == fence_marker.rstrip():
                in_fence = False
            out.append(line)
            continue
        # Mask inline `code` spans before running the link regex so backticked
        # snippets like `vec[0]` aren't misread as links. Restore them after.
        spans = [mm.group(0) for mm in re.finditer(r"`[^`\n]*`", line)]
        masked = re.sub(r"`[^`\n]*`", lambda mm: "\x00" * len(mm.group(0)), line)
        rewritten = LINK_RE.sub(repl, masked)
        if spans:
            idx = 0

            def restore(_mm: re.Match[str], _spans: list[str] = spans) -> str:
                nonlocal idx
                s = _spans[idx]
                idx += 1
                return s

            rewritten = re.sub(r"\x00+", restore, rewritten)
        out.append(rewritten)
    return "\n".join(out), resolved, unresolved


def transform_body(body: str, src_path: Path, runnable: set[str], src_to_slug: dict[Path, str]):
    body = unwrap_d2_vars(body)
    body = strip_lang_tabs_wrappers(body)
    body = tag_runnable_fences(body, runnable)
    body, resolved, unresolved = rewrite_links(body, src_path, src_to_slug)
    return body, resolved, unresolved


# ---------- Write -----------------------------------------------

def write_chapter(ch: Chapter, body: str) -> None:
    dest = DEST_ROOT / ch.rel_in_book
    dest.parent.mkdir(parents=True, exist_ok=True)
    # Write a frontmatter title block so the chapter title is stable
    # regardless of any later H1 edits.
    safe_title = ch.title.replace('"', '\\"')
    out = f'---\ntitle: "{safe_title}"\n---\n\n{body.lstrip()}'
    dest.write_text(out, encoding="utf-8")


def write_book_json() -> None:
    payload = {
        "title": "Data Structure and Algorithms",
        "description": (
            "A guided tour of core data structures and algorithms — adapted from "
            "the homelab note-book project. Includes Mermaid and D2 diagrams plus "
            "runnable code samples in multiple languages."
        ),
        "tags": ["computer-science", "data-structures", "algorithms", "dsa"],
        "estimatedReadingMinutes": 600,
    }
    (DEST_ROOT / "book.json").write_text(
        json.dumps(payload, indent=4, ensure_ascii=False) + "\n", encoding="utf-8"
    )


def write_section_jsons() -> None:
    for slug, title in TOP_LEVEL_TITLES.items():
        section_dir = DEST_ROOT / slug
        section_dir.mkdir(parents=True, exist_ok=True)
        (section_dir / "_section.json").write_text(
            json.dumps({"title": title}, ensure_ascii=False) + "\n", encoding="utf-8"
        )


# ---------- Main --------------------------------------------------

def main() -> int:
    if not SOURCE_ROOT.is_dir():
        sys.exit(f"source not found: {SOURCE_ROOT}")
    runnable = load_runnable_aliases()
    print(f"runnable language aliases: {sorted(runnable)}")

    if DEST_ROOT.exists():
        shutil.rmtree(DEST_ROOT)
    DEST_ROOT.mkdir(parents=True)
    write_book_json()
    write_section_jsons()

    chapters = collect_chapters()
    # Build the resolution map BEFORE writing, so cross-links can resolve
    # in any order. Key by resolved (canonicalised) source path.
    src_to_slug = {ch.src_path.resolve(): ch.slug for ch in chapters}

    # Slug-uniqueness sanity check (matches server-side check).
    seen: dict[str, Chapter] = {}
    dups: list[tuple[Chapter, Chapter]] = []
    for ch in chapters:
        if ch.slug in seen:
            dups.append((seen[ch.slug], ch))
        else:
            seen[ch.slug] = ch
    if dups:
        for a, b in dups:
            print(f"DUP slug '{a.slug}': {a.src_path}  ←→  {b.src_path}", file=sys.stderr)
        sys.exit(f"{len(dups)} duplicate slug(s); aborting")

    total_resolved = 0
    total_unresolved = 0
    for ch in chapters:
        body = ch.src_path.read_text(encoding="utf-8", errors="replace")
        body, resolved, unresolved = transform_body(body, ch.src_path, runnable, src_to_slug)
        write_chapter(ch, body)
        total_resolved += resolved
        total_unresolved += unresolved

    print(
        f"wrote {len(chapters)} chapters; "
        f"resolved {total_resolved} internal links; "
        f"{total_unresolved} unresolved (TODOs)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
