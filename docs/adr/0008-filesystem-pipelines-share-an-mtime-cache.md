# Filesystem-driven pipelines share one mtime-keyed cache

The Cortex and Blog pipelines both serve a derived in-memory index over a filesystem content tree, rebuilt
when the tree changes on disk. Until this ADR, each pipeline carried its own copy of the cache machinery: a
`Ref[Option[*State]]`, an mtime watermark stored inside the state, a `cachedState` read path, and a
`loadAndCache` rebuild path — ~100 structurally identical lines, differing only in the payload type. A
cache-invalidation bug would have to be found and fixed twice, and a third filesystem-driven pipeline would
clone the dance again.

**Decision**: the cache-and-invalidate behaviour is a single module, `server.content.MtimeCachedIndex[E, S]`.
It owns the `Ref`, the watermark comparison, the `autoReload` gate, and the "read mtime → rebuild → store"
composition. Each pipeline supplies only two effects at construction — a cheap whole-tree `currentMtime` and
a `rebuild` that scans the tree and produces the derived state — through its filesystem seam (`CortexFs` /
`BlogFs`) and its pure walker. The watermark lives in `MtimeCachedIndex`, not in `CortexState` / `BlogState`,
so the pipelines' state types carry no cache bookkeeping.

The trade is one shared module (one extra import per pipeline) for **locality**: the invalidation logic is
written and tested once (`MtimeCachedIndexSpec`) instead of being re-verified end-to-end through each
pipeline's fake. New filesystem-driven pipelines reuse it and only write their walker. The concurrency
posture is unchanged from the old per-pipeline code — a race during a content edit can rebuild twice, which
is harmless because `rebuild` is idempotent and the stale-serve window is bounded by one rebuild's work.
