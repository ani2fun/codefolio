# D3 widgets are a closed catalog, not author-written JS

The new `Block.D3Widget` variant lets chapters embed interactive D3 visualisations alongside the existing
Mermaid / D2 blocks. Authors *name* a widget in the markdown fence — `` ```d3 widget=array-traversal `` — and
pass a JSON payload; the widget itself is a Scala.js + D3 component built once and reused across many
chapters. Raw author-written JS in chapter content was rejected: it would be an `eval` surface, escape the
typed Block pipeline, and shift authoring from prose to JS programming.

The payload is kept structurally loose at the `shared` layer (`Block.D3Widget(name: String, payload: String)`
— payload is the raw JSON string) so growing the catalog never touches `shared` or regenerates the OpenAPI
contract. Each widget owns its schema on the client. This mirrors the `D2Slides(slides: List[String])`
precedent: shared keeps the structure; the renderer interprets.

Unknown widget names render an inline error rather than failing the chapter — the catalog is a closed match
in `D3WidgetBlock`, and a typo in markdown should not break the page.
