# The SPA route topology is declared once in `AppRoutes`

The SPA's top-level routes exist in two runtimes: the client `Router` declares them as routing rules, and
the production server's `StaticRoutes` must serve `index.html` as a fallback for each one (a hard reload of
`/cortex/foo/bar` 404s otherwise). Until this ADR, `shared.AppRoutes` carried only path *string constants*;
the server's fallback list was a hand-maintained mirror of the client's route set, kept in sync by a code
comment. "Forgot to add the route to the server fallback list" is a bug the project has shipped before.

**Decision**: `AppRoutes` declares the SPA route *topology*, not just strings — `AppRoutes.SpaRoutes:
List[SpaRoute]`, where each `SpaRoute(segment, hasNestedRoutes)` names a top-level SPA path and whether the
SPA also owns deeper paths under it. The server's `StaticRoutes` derives its `index.html` fallback list by
iterating `SpaRoutes` (a leaf route for each, plus a `/segment/trailing` route when `hasNestedRoutes`); the
client `Router` uses the same segment constants. Adding an SPA route is one entry in `SpaRoutes` — the
server picks it up with no separate mirror to forget. `StaticRoutesSpec` runs real requests through the
derived routes to prove every `SpaRoute` is covered (and that a leaf-only route does *not* serve deep paths).

The trade is a small shared data structure in `shared` for **a seam with one home**: the question "what are
the SPA's top-level routes?" has a single answer both runtimes read. The catch-all-wildcard alternative is
still rejected for the reason documented in `StaticRoutes` — zio-http's combined routing makes a wildcard
greedy enough to shadow `/api/*` and `/docs`.
