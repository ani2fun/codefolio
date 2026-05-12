# Part 2 — Building blocks

> **The Lego pieces.** Once you know how each one behaves under load, you can assemble almost any system.

By the time you finish this part you will:

- Read a protocol-stack choice and **count the RTTs** it costs at 50 ms and at 150 ms RTT.
- Know the **L4 vs L7 distinction** cold, and reach for the right load-balancing algorithm by symptom.
- Build a four-tier cache (client / CDN / app L1 / distributed L2) and know what each tier wins and loses.
- Read a **Postgres `EXPLAIN ANALYZE` plan** end to end and tell stale stats apart from a missing index.
- Recognise which **NoSQL family** fits which access shape — and accept that Postgres still wins most arguments.
- Know the three **replication topologies**, the lag that lives inside each one, and the GitHub-October-2018 split-brain failure mode.
- Pick a **partition key** that won't strand you in a rewrite two years later, and feel the hot-shard math in your gut.
- Read the **consistency-model lattice** (linearisable → eventual) and pick the weakest model your specific query tolerates.
- Walk a **Raft cluster** through leader election + log replication + failover, and operate one without flinching.

## Lessons

6. [Networking primer for system designers](/cortex/system-design/building-blocks-networking-primer) — TCP / UDP / HTTP/1.1/2/3 / TLS / DNS / CDNs and what each one *costs*.
7. [Load balancing](/cortex/system-design/building-blocks-load-balancing) — L4 vs L7, the five algorithms, the senior moment of consistent hashing.
8. [Caching](/cortex/system-design/building-blocks-caching) — four tiers, four strategies, and the two hard problems (invalidation and stampedes).
9. [Relational databases](/cortex/system-design/building-blocks-relational-databases) — Postgres internals, indexes, transactions, isolation levels, and EXPLAIN ANALYZE.
10. [NoSQL families](/cortex/system-design/building-blocks-nosql-families) — KV, document, wide-column, graph. When and why.
11. [Replication](/cortex/system-design/building-blocks-replication) — single-leader, multi-leader, leaderless. Replication lag, read-your-writes, failover, split-brain.
12. [Sharding and partitioning](/cortex/system-design/building-blocks-sharding-and-partitioning) — range, hash, directory; hot shards; the resharding migration cost.
13. [Consistency models](/cortex/system-design/building-blocks-consistency-models) — the six-level lattice and the anomalies each level allows.
14. [Consensus — Paxos and Raft from scratch](/cortex/system-design/building-blocks-consensus-paxos-and-raft) — the algorithm under etcd, Consul, CockroachDB, and every Kubernetes control plane.

## Interactive widgets

Seven new widgets joined the Wave-1 catalog in this Part; drag, click, and step through each to feel the underlying math instead of memorising it.

| Widget | Where it lives | What it teaches |
|---|---|---|
| `handshake-timeline` | Lesson 6 | The handshake tax — 4–6 RTTs cold vs 1 RTT warm, sized by an RTT slider |
| `consistent-hash-ring` | Lesson 7 | Ring positions of physical + virtual nodes; the modulo-vs-consistent remap-count gap |
| `cache-stampede` | Lesson 8 | Origin pressure with vs without request coalescing; the N× rectangle |
| `btree-walker` | Lesson 9 | B-tree depth vs sequential scan cost as you drag table size from 100 to 100 M rows |
| `replication-lag` | Lesson 11 | Leader vs replica timelines with a draggable read-time cursor — the read-your-writes hazard made visceral |
| `hot-shard` | Lesson 12 | Per-shard load under range / hash / hash-virtual partitioning, with a Zipf-skew slider |
| `raft-animator` | Lesson 14 | Step through leader election, log replication, and leader failover on a 3-node Raft cluster |

## Runnable examples

Every lesson with hands-on infrastructure ships with a `docker compose`-able example. `cd` in, run the README's recipe, and break it on purpose.

- [`examples/07-load-balancing-nginx/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/02-building-blocks/examples/07-load-balancing-nginx) — NGINX in front of three FastAPI replicas; induce backend failure and watch the LB react.
- [`examples/08-caching-redis-stampede/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/02-building-blocks/examples/08-caching-redis-stampede) — `/no-coalesce` vs `/coalesced` endpoints in front of Redis; reads off the origin-hit counter under concurrent load.
- [`examples/09-relational-explain-analyze/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/02-building-blocks/examples/09-relational-explain-analyze) — Postgres + a 100k-row seed; same query before and after `CREATE INDEX`, read the plans line by line.
- [`examples/11-replication-postgres-streaming/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/02-building-blocks/examples/11-replication-postgres-streaming) — Postgres primary + streaming replica via `pg_basebackup`; measure the lag distribution and the stale-read fraction.
- [`examples/12-sharding-strategies/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/02-building-blocks/examples/12-sharding-strategies) — pure-Python demo of range / hash / directory / hash+virtual partitioning under a Zipfian workload.

## Prerequisites

- You're comfortable with the [foundations](/cortex/system-design/foundations-index) — the numbers, BOTE estimation, CAP / PACELC, and Little's Law.
- You can read short Python and short Scala. You don't need to write them.
- You have `docker compose` working locally for the runnable examples (optional but recommended).

That's it. Each lesson stands on its own, but they were written to be read in order — later lessons reference earlier ones (Lesson 11's replication lag is what Lesson 8's read-your-writes mitigation actually does, Lesson 12's sharding leans on Lesson 7's consistent hashing) and the cross-links are everywhere.
