# Cortex content errors are lenient by default

Cortex content is filesystem-driven and edited like a knowledge book — typos in a chapter's frontmatter or in a `book.json` file are routine and shouldn't block the whole index from building.

**Decision**: malformed JSON metadata files (`book.json`, `_section.json`) and malformed YAML frontmatter (e.g. unterminated `---` fences) fall through to defaults — the index keeps building with humanised-slug titles and the chapter renders with no frontmatter. Pinned by `CortexHandlerSpec`: `falls back to humanised slug when book.json is malformed`, and `treats unterminated frontmatter as plain body`.

The trade is early detection of metadata typos for build resilience. New content types added under the Cortex tree should follow the same convention.
