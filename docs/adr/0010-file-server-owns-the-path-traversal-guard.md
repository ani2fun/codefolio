# Static file serving — one module owns the path-traversal guard

The server has two static-file surfaces: `StaticRoutes` (the Vite frontend bundle + SPA fallback) and
`CortexAssetRoutes` (chapter-relative binary assets). Until this ADR, each carried its own near-identical
copy of "resolve a path under a root, reject anything that escapes it, read the bytes, attach a
Content-Type, 404 on a miss" — including the security-critical path-traversal guard (`toRealPath()` +
`startsWith(root)`). A hardening fix to one copy would not reach the other, and neither copy was tested for
symlink escapes or `..` traversal. The two content-type tables had also already drifted.

**Decision**: the file-serving behaviour is a single module, `server.http.FileServer`, constructed once per
root directory. It owns the path-traversal containment, the read-or-404, and the resolved-root snapshot;
the content-type table is `server.http.ContentTypes` — one table covering the union of both surfaces' file
types, so they can't drift. `StaticRoutes` and `CortexAssetRoutes` become thin route definitions over a
`FileServer` instance. `FileServerSpec` pins the security behaviour — `..` escape, absolute-path input,
symlink escape, directory, missing file, absent root — once, against the module both callers route through.

The trade is two small shared modules for **the path-traversal guard having one home and one test surface**.
This is the kind of code the Server Stack deep-dive calls out as "looks defensive and unnecessary until it
isn't" — exactly the code that should not be living in two hand-maintained copies.
