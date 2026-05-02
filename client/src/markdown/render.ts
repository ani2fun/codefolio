// Markdown rendering pipeline.
//
// Port of `portfolio-app/src/lib/knowledgeMarkdown.tsx`. The custom remark
// plugins (D2 pre-pass, runnable-group merging, image unwrapping) and the
// custom remark-rehype code handler are preserved verbatim. The output
// difference: instead of running through `toJsxRuntime` to produce React
// nodes, this module emits an HTML string + an extracted table of contents.
// The Scala.js client injects the HTML via `dangerouslySetInnerHTML` and
// then walks the article DOM to React-portal-mount Scala.js components into
// `<div class="runnable-code|runnable-group|mermaid-block|d2-diagram">` stubs.
//
// Heavy renderers (D2 WASM engine, mermaid) are dynamic-imported so they
// only land in the chunk that loads this module — keeping the home page
// bundle small.

import { unified, type Plugin } from "unified";
import remarkParse from "remark-parse";
import remarkGfm from "remark-gfm";
import remarkMath from "remark-math";
import remarkRehype from "remark-rehype";
import rehypeSlug from "rehype-slug";
import rehypeAutolinkHeadings from "rehype-autolink-headings";
import rehypeKatex from "rehype-katex";
import rehypePrettyCode from "rehype-pretty-code";
import rehypeStringify from "rehype-stringify";
import { visit } from "unist-util-visit";
import { fromHtml } from "hast-util-from-html";
import { defaultHandlers } from "mdast-util-to-hast";

import type { Root, Code, Html, Image, Paragraph } from "mdast";
import type { Element, ElementContent, Root as HastRoot, Text } from "hast";
import type { State } from "mdast-util-to-hast";

// Same alias map as `client/src/markdown/runtime.ts` (Prism) and
// `server/.../runner/Languages.scala`. Local copy avoids importing prism
// just for the alias resolution.
interface LanguageInfo {
  id: number;
  label: string;
  aliases: string[];
}

const RUNNABLE_LANGUAGES: LanguageInfo[] = [
  { id: 71, label: "Python 3.8", aliases: ["python", "py", "python3"] },
  { id: 62, label: "Java 13 (OpenJDK)", aliases: ["java"] },
  { id: 81, label: "Scala 2.13", aliases: ["scala"] },
  { id: 50, label: "C (GCC 9.2)", aliases: ["c"] },
  { id: 54, label: "C++ (GCC 9.2)", aliases: ["cpp", "c++", "cxx"] },
  { id: 60, label: "Go 1.13", aliases: ["go", "golang"] },
  { id: 73, label: "Rust 1.40", aliases: ["rust", "rs"] },
  { id: 78, label: "Kotlin 1.9", aliases: ["kotlin", "kt"] },
  { id: 74, label: "TypeScript 3.7", aliases: ["typescript", "ts"] },
  { id: 63, label: "JavaScript (Node.js 12)", aliases: ["javascript", "js", "node"] },
  { id: 82, label: "SQL (SQLite 3.27)", aliases: ["sql", "sqlite"] },
];

const aliasIndex = new Map<string, LanguageInfo>();
for (const lang of RUNNABLE_LANGUAGES) {
  for (const a of lang.aliases) aliasIndex.set(a.toLowerCase(), lang);
}

const resolveLanguage = (lang: string | null | undefined): LanguageInfo | null =>
  lang ? aliasIndex.get(lang.toLowerCase()) ?? null : null;

// ---- D2 array traversal slideshow expansion -----------------------------
//
// Markdown authors can write one compact marker instead of duplicating the
// same array diagram across every traversal frame:
//
//   <div class="d2-array-traversal"
//        data-caption="..."
//        data-rows="2"
//        data-cols="3"
//        data-values="value1, value2, value3, value4, value5, value6"></div>
//
// This expands to a normal `.d2-slides` marker plus generated D2 code nodes.

const isD2ArrayTraversalMarker = (node: unknown): node is Html =>
  !!node &&
  typeof node === "object" &&
  (node as { type?: unknown }).type === "html" &&
  /<div\b[^>]*class=(["'])[^"']*\bd2-array-traversal\b[^"']*\1/i.test((node as Html).value);

const parsePositiveInt = (raw: string | null, fallback: number): number => {
  const n = raw ? parseInt(raw, 10) : Number.NaN;
  return Number.isFinite(n) && n > 0 ? n : fallback;
};

const d2MdText = (s: string): string => s.replace(/\|/g, "\\|").replace(/`/g, "\\`");

interface ArrayTraversalConfig {
  caption: string | null;
  rows: number;
  cols: number;
  values: string[];
}

const parseArrayTraversalConfig = (marker: Html): ArrayTraversalConfig => {
  const rows = parsePositiveInt(htmlAttr(marker.value, "data-rows"), 2);
  const cols = parsePositiveInt(htmlAttr(marker.value, "data-cols"), 3);
  const total = rows * cols;
  const rawValues = htmlAttr(marker.value, "data-values") ?? "";
  const values = rawValues
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  return {
    caption: htmlAttr(marker.value, "data-caption"),
    rows,
    cols,
    values:
      values.length === total ? values : Array.from({ length: total }, (_, i) => `value${i + 1}`),
  };
};

const cellLabel = (index: number, cols: number): string => {
  const row = Math.floor(index / cols);
  const col = index % cols;
  return `[${row},${col}]`;
};

const cellStyle = (index: number, active: number | null, done: boolean): string => {
  if (done || (active !== null && index < active)) {
    return ' {style.fill: "#dcfce7"; style.stroke: "#16a34a"}';
  }
  if (active === index) {
    return ' {style.fill: "#fde68a"; style.stroke: "#d97706"}';
  }
  return "";
};

const arrayD2 = (
  title: string,
  cfg: ArrayTraversalConfig,
  active: number | null,
  done = false
): string => {
  const cells = cfg.values
    .map((value, i) => {
      const id = `cell${i}`;
      return [
        `  ${id}: |md`,
        `    \`${cellLabel(i, cfg.cols)}\` ${d2MdText(value)}`,
        `  |${cellStyle(i, active, done)}`,
      ].join("\n");
    })
    .join("\n");

  return [
    `arr: "${title}" {`,
    `  grid-rows: ${cfg.rows}`,
    "  grid-gap: 0",
    cells,
    "}",
  ].join("\n");
};

const stateD2 = (cfg: ArrayTraversalConfig, active: number, done = false): string => {
  const row = Math.floor(active / cfg.cols);
  const col = active % cfg.cols;
  const reset = row > 0 && col === 0 && !done ? "\n\n  inner loop reset" : "";
  const doneLine = done ? "\n\n  ✓ done" : "";
  const title = done
    ? `Traversal complete — visited all ${cfg.values.length} cells`
    : `Visit arr[${row}][${col}]`;

  return [
    "direction: right",
    "",
    "state: |md",
    `  **row** = ${row}`,
    "",
    `  **column** = ${col}${reset}${doneLine}`,
    "|",
    "",
    arrayD2(title, cfg, done ? null : active, done),
  ].join("\n");
};

const d2ArrayTraversalSlides = (marker: Html): Array<Html | Code> => {
  const cfg = parseArrayTraversalConfig(marker);
  const captionAttr = cfg.caption ? ` data-caption="${escapeHtmlAttr(cfg.caption)}"` : "";
  const slideMarker: Html = { type: "html", value: `<div class="d2-slides"${captionAttr}>` };
  const ready: Code = {
    type: "code",
    lang: "d2",
    meta: null,
    value: arrayD2(`Ready to traverse the ${cfg.rows} × ${cfg.cols} array`, cfg, null),
  };
  const visits: Code[] = cfg.values.map((_, i) => ({
    type: "code",
    lang: "d2",
    meta: null,
    value: stateD2(cfg, i),
  }));
  const complete: Code = {
    type: "code",
    lang: "d2",
    meta: null,
    value: stateD2(cfg, cfg.values.length - 1, true),
  };

  return [slideMarker, ready, ...visits, complete];
};

const remarkExpandD2ArrayTraversal: Plugin<[], Root> = () => (tree) => {
  const walk = (parent: { children?: unknown[] } | null) => {
    if (!parent || !Array.isArray(parent.children)) return;

    const out: unknown[] = [];
    for (const node of parent.children) {
      if (isD2ArrayTraversalMarker(node)) out.push(...d2ArrayTraversalSlides(node));
      else out.push(node);
    }

    parent.children = out;
    for (const child of parent.children) {
      if (child && typeof child === "object" && "children" in (child as object)) {
        walk(child as { children?: unknown[] });
      }
    }
  };

  walk(tree as { children?: unknown[] });
};

// ---- D2 pre-pass --------------------------------------------------------
//
// Renders every ```d2 fence to SVG via the WASM engine and stashes the
// result on node.data so the custom code handler can pick it up. Done in
// remark land because the D2 API is async and remark-rehype handlers must
// be sync.

const remarkRenderD2: Plugin<[], Root> = () => async (tree) => {
  const targets: Code[] = [];
  visit(tree, "code", (node: Code) => {
    if (node.lang === "d2") targets.push(node);
  });
  if (targets.length === 0) return;

  // Dynamic import — D2 ships a multi-MB WASM blob. Only load it when at
  // least one chapter actually has a d2 fence.
  const { D2 } = await import("@terrastruct/d2");
  const d2 = new D2();
  for (const node of targets) {
    const data = (node.data ??= {}) as Record<string, unknown>;
    try {
      const result = await d2.compile(node.value);
      const svg = await d2.render(result.diagram, result.renderOptions);
      data.d2Svg = svg;
    } catch (err) {
      data.d2Error = err instanceof Error ? err.message : "D2 render failed";
    }
  }
};

const isD2SlidesMarker = (node: unknown): node is Html =>
  !!node &&
  typeof node === "object" &&
  (node as { type?: unknown }).type === "html" &&
  /<div\b[^>]*class=(["'])[^"']*\bd2-slides\b[^"']*\1/i.test((node as Html).value);

const isClosingDiv = (node: unknown): node is Html =>
  !!node &&
  typeof node === "object" &&
  (node as { type?: unknown }).type === "html" &&
  /^<\/div>\s*$/i.test((node as Html).value.trim());

const htmlAttr = (html: string, name: string): string | null => {
  const re = new RegExp(`\\b${name}\\s*=\\s*(["'])(.*?)\\1`, "i");
  return html.match(re)?.[2] ?? null;
};

const escapeHtmlAttr = (s: string): string =>
  s
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");

const escapeHtmlText = (s: string): string =>
  s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

const d2SlideHtml = (node: Code): string => {
  const data = node.data as { d2Svg?: string; d2Error?: string } | undefined;
  if (data?.d2Svg) return data.d2Svg;
  if (data?.d2Error) {
    return [
      '<div class="d2-error">',
      '<p class="d2-error-title">D2 render error</p>',
      `<pre>${escapeHtmlText(data.d2Error)}</pre>`,
      "</div>",
    ].join("");
  }
  return "";
};

const buildD2SlidesHtml = (marker: Html, slides: Code[]): string => {
  const caption = htmlAttr(marker.value, "data-caption");
  const captionAttr = caption ? ` data-caption="${escapeHtmlAttr(caption)}"` : "";
  const body = slides
    .map((slide, i) => {
      const html = d2SlideHtml(slide);
      return `<div class="d2-slide" data-slide-index="${i}">${html}</div>`;
    })
    .join("");
  return `<div class="d2-slides not-prose"${captionAttr}>${body}</div>`;
};

// Groups:
//   <div class="d2-slides" ...>
//
//   ```d2
//   ...
//   ```
//   ```d2
//   ...
//   ```
//
// into one slideshow placeholder. The imported source currently lacks the
// closing </div> because the legacy import script stripped standalone closing
// divs, so this pass intentionally stops at the first non-D2 node too.
const remarkGroupD2Slides: Plugin<[], Root> = () => (tree) => {
  const walk = (parent: { children?: unknown[] } | null) => {
    if (!parent || !Array.isArray(parent.children)) return;
    const out: unknown[] = [];
    let i = 0;

    while (i < parent.children.length) {
      const node = parent.children[i];

      if (isD2SlidesMarker(node)) {
        const slides: Code[] = [];
        let j = i + 1;

        while (j < parent.children.length) {
          const child = parent.children[j] as Code | Html;
          if (child.type === "code" && child.lang === "d2") {
            slides.push(child);
            j++;
          } else {
            break;
          }
        }

        if (slides.length > 0) {
          const next = parent.children[j];
          out.push({ type: "html", value: buildD2SlidesHtml(node, slides) } satisfies Html);
          i = isClosingDiv(next) ? j + 1 : j;
          continue;
        }
      }

      out.push(node);
      i++;
    }

    parent.children = out;
    for (const child of parent.children) {
      if (child && typeof child === "object" && "children" in (child as object)) {
        walk(child as { children?: unknown[] });
      }
    }
  };

  walk(tree as { children?: unknown[] });
};

// ---- Runnable-group merge -----------------------------------------------
//
// Adjacent ```<lang> run``` fences merge into a single tabbed group. Walks
// each parent and replaces consecutive runnable code siblings with one
// merged node carrying the full tab list on data.runnableTabs. The custom
// code handler emits a `runnable-group` placeholder for the merged node.

interface RunnableTabNode {
  language: string;
  languageLabel: string;
  source: string;
}

const isRunnableCode = (node: { type: string; lang?: string | null; meta?: string | null }) => {
  if (node.type !== "code") return false;
  const meta = typeof node.meta === "string" ? node.meta : "";
  if (!/\brun\b/.test(meta)) return false;
  return resolveLanguage(node.lang ?? null) !== null;
};

const remarkGroupRunnable: Plugin<[], Root> = () => (tree) => {
  const walk = (parent: { children?: unknown[] } | null) => {
    if (!parent || !Array.isArray(parent.children)) return;
    const out: unknown[] = [];
    let i = 0;
    while (i < parent.children.length) {
      const node = parent.children[i] as Code & { data?: Record<string, unknown> };
      if (isRunnableCode(node)) {
        let j = i;
        const tabs: RunnableTabNode[] = [];
        while (j < parent.children.length) {
          const sibling = parent.children[j] as Code;
          if (!isRunnableCode(sibling)) break;
          const lang = resolveLanguage(sibling.lang ?? null)!;
          tabs.push({
            language: sibling.lang ?? "",
            languageLabel: lang.label,
            source: sibling.value,
          });
          j++;
        }
        if (tabs.length > 1) {
          const first = parent.children[i] as Code & { data?: Record<string, unknown> };
          first.data = { ...(first.data ?? {}), runnableTabs: tabs };
          out.push(first);
          i = j;
          continue;
        }
      }
      out.push(parent.children[i]);
      i++;
    }
    parent.children = out;
    for (const child of parent.children) {
      if (child && typeof child === "object" && "children" in (child as object)) {
        walk(child as { children?: unknown[] });
      }
    }
  };
  walk(tree as { children?: unknown[] });
};

// ---- Unwrap images ------------------------------------------------------
//
// `![alt](url)` is parsed as a paragraph wrapping an image node. Unwrap it
// so the image isn't rendered inside a <p> (which would prevent the figure
// post-processing in the custom handler).

const remarkUnwrapImages: Plugin<[], Root> = () => (tree) => {
  visit(tree, "paragraph", (node: Paragraph, index, parent) => {
    if (
      parent &&
      typeof index === "number" &&
      node.children.length === 1 &&
      node.children[0].type === "image"
    ) {
      parent.children.splice(index, 1, node.children[0] as Image);
    }
  });
};

// ---- Custom code handler ------------------------------------------------
//
// Routes:
//   ```d2     → <div class="d2-diagram">{rendered SVG}</div>
//                (or .d2-error for compile failures)
//   <div class="d2-slides"> + D2 fences
//              → <div class="d2-slides"><div class="d2-slide">...</div>...</div>
//   ```mermaid → <div class="mermaid-block" data-mermaid-source="...">  (placeholder)
//   ```<lang> run → <div class="runnable-code" data-...>                (placeholder)
//   merged group → <div class="runnable-group" data-tabs="...">         (placeholder)
//   anything else → default <pre><code class="language-x">…</code></pre>
//
// The placeholders are React-portal-mounted by Scala.js after innerHTML
// injection.

const codeHandler = (state: State, node: Code): Element | undefined => {
  const data = node.data as
    | {
        d2Svg?: string;
        d2Error?: string;
        runnableTabs?: RunnableTabNode[];
      }
    | undefined;

  if (node.lang === "d2" && data?.d2Svg) {
    const fragment = fromHtml(data.d2Svg, { fragment: true });
    return {
      type: "element",
      tagName: "div",
      properties: { className: ["d2-diagram", "not-prose"] },
      children: fragment.children as ElementContent[],
    };
  }
  if (node.lang === "d2" && data?.d2Error) {
    return {
      type: "element",
      tagName: "div",
      properties: { className: ["d2-error", "not-prose"] },
      children: [
        {
          type: "element",
          tagName: "p",
          properties: { className: ["d2-error-title"] },
          children: [{ type: "text", value: "D2 render error" }],
        },
        {
          type: "element",
          tagName: "pre",
          properties: {},
          children: [{ type: "text", value: data.d2Error }],
        },
      ],
    };
  }
  if (node.lang === "mermaid") {
    return {
      type: "element",
      tagName: "div",
      properties: {
        className: ["mermaid-block"],
        "data-mermaid-source": encodeURIComponent(node.value),
      },
      children: [],
    };
  }

  const meta = typeof node.meta === "string" ? node.meta : "";
  const runRequested = /\brun\b/.test(meta);

  if (data?.runnableTabs) {
    return {
      type: "element",
      tagName: "div",
      properties: {
        className: ["runnable-group"],
        "data-tabs": encodeURIComponent(JSON.stringify(data.runnableTabs)),
      },
      children: [],
    };
  }

  if (runRequested && node.lang) {
    const lang = resolveLanguage(node.lang);
    if (lang) {
      return {
        type: "element",
        tagName: "div",
        properties: {
          className: ["runnable-code"],
          "data-lang": node.lang,
          "data-language-label": lang.label,
          "data-source": encodeURIComponent(node.value),
        },
        children: [],
      };
    }
  }

  return defaultHandlers.code(state, node) as Element;
};

// ---- TOC extraction -----------------------------------------------------
//
// Runs after rehype-slug so each <h*> has an id. Collects {depth, slug,
// text}; the Scala.js sidebar renders these directly.

export interface TocEntry {
  depth: number;
  slug: string;
  text: string;
}

const collectText = (node: Element | Text | ElementContent | undefined): string => {
  if (!node) return "";
  if (node.type === "text") return (node as Text).value;
  if (node.type === "element") {
    const el = node as Element;
    // Skip rehype-autolink-headings appended <span class="heading-anchor-icon">.
    const cls = el.properties?.className;
    const list = Array.isArray(cls) ? cls : typeof cls === "string" ? cls.split(" ") : [];
    if (list.includes("heading-anchor") || list.includes("heading-anchor-icon")) return "";
    return (el.children ?? []).map(collectText).join("");
  }
  return "";
};

const rehypeCollectToc = (collector: TocEntry[]): Plugin<[], HastRoot> => () => (tree) => {
  visit(tree, "element", (node: Element) => {
    if (!/^h[1-6]$/.test(node.tagName)) return;
    const depth = parseInt(node.tagName[1], 10);
    const id = node.properties?.id;
    const slug = typeof id === "string" ? id : "";
    if (!slug) return;
    collector.push({ depth, slug, text: collectText(node) });
  });
};

// ---- Public API ---------------------------------------------------------

export interface RenderResult {
  /** Article HTML, ready for `dangerouslySetInnerHTML` / `innerHTML`. */
  html: string;
  /** Headings + slugs in document order. */
  toc: TocEntry[];
}

/** Render a chapter's raw markdown source. */
export async function renderChapter(source: string): Promise<RenderResult> {
  const toc: TocEntry[] = [];

  const processor = unified()
    .use(remarkParse)
    .use(remarkGfm)
    .use(remarkMath)
    .use(remarkExpandD2ArrayTraversal)
    .use(remarkRenderD2)
    .use(remarkGroupD2Slides)
    .use(remarkGroupRunnable)
    .use(remarkUnwrapImages)
    .use(remarkRehype, {
      handlers: { code: codeHandler },
      // Allow our custom div placeholders + d2's inline SVG markup to pass
      // through unmodified.
      allowDangerousHtml: true,
    })
    .use(rehypeSlug)
    // Strip the leading h1 — ChapterPage already renders the title as a
    // page-level <h1>. Chapters that have no h1 are unaffected.
    .use((() => (tree: HastRoot) => {
      const idx = tree.children.findIndex(
        (c) => c.type === "element" && (c as Element).tagName === "h1"
      );
      if (idx !== -1) tree.children.splice(idx, 1);
    }) as Plugin<[], HastRoot>)
    .use(rehypeAutolinkHeadings, {
      behavior: "append",
      properties: {
        className: ["heading-anchor"],
        ariaLabel: "Link to section",
      },
      content: {
        type: "element",
        tagName: "span",
        properties: { className: ["heading-anchor-icon"] },
        children: [{ type: "text", value: "#" }],
      },
    })
    .use(rehypeCollectToc(toc))
    .use(rehypeKatex)
    .use(rehypePrettyCode, {
      theme: "github-dark",
      keepBackground: true,
      defaultLang: "plaintext",
      bypassInlineCode: true,
    })
    .use(rehypeStringify, { allowDangerousHtml: true });

  const file = await processor.process(source);
  return { html: String(file), toc };
}
