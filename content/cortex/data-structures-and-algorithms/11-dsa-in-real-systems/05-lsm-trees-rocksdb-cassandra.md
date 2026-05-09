---
title: "LSM Trees in RocksDB and Cassandra"
summary: "The Log-Structured Merge tree — the write-optimised alternative to B-trees that powers RocksDB, Cassandra, ScyllaDB, LevelDB, and CockroachDB. Sequential writes, periodic compaction, and the 100x write-throughput advantage."
prereqs:
  - trees-b-tree-introduction-to-b-trees
  - probabilistic-and-advanced-skip-list
  - probabilistic-and-advanced-bloom-filter
---

# 5. LSM Trees in RocksDB and Cassandra

## The Hook

A B+-tree (covered in the [B-tree chapter](/cortex/data-structures-and-algorithms/trees-b-tree-introduction-to-b-trees)) is *read-optimised*: hierarchical index, point lookups in `O(log n)` disk seeks, range queries via leaf-chain traversal. Writes are also `O(log n)` — but every write involves a *random* page modification, which on traditional spinning disks is dramatically slower than sequential writes.

The **LSM Tree (Log-Structured Merge tree)** flips the trade-off. Writes go into a small in-memory buffer (the **memtable**, usually a skip list) and a sequential **WAL** for durability. When the memtable fills, it's flushed to disk as an immutable, sorted file (an **SSTable**). Subsequent flushes create more SSTables. Periodically, a background **compaction** merges SSTables into fewer, larger ones — essentially a streaming merge sort over the entire dataset.

The result: write throughput is *sequential disk write speed* (hundreds of MB/s) instead of random write speed (a few hundred ops/s on spinning disk). The cost: reads now have to check multiple SSTables, and the compaction job consumes CPU and bandwidth.

This chapter is the tour. RocksDB, Cassandra, ScyllaDB, CockroachDB, LevelDB, HBase, InfluxDB — all built on this idea, with different compaction strategies and tuning knobs.

---

## Table of contents

1. [Memtable + WAL + SSTables](#memtable-wal-sstables)
2. [The compaction problem](#the-compaction-problem)
3. [Compaction strategies: leveled vs tiered](#compaction-strategies-leveled-vs-tiered)
4. [Reads with multiple SSTables](#reads-with-multiple-sstables)
5. [The write-amplification trade-off](#the-write-amplification-trade-off)
6. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
7. [Production reality](#production-reality)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# Memtable + WAL + SSTables

A write to an LSM tree:

1. Append to the **WAL** (a sequential file) for durability. Cheap — sequential write.
2. Insert into the **memtable** — an in-memory sorted structure (skip list or RB-tree). Fast — pure-memory.
3. When the memtable reaches a threshold (typically 64 MB), it's marked **immutable** and a new memtable starts accepting writes.
4. The immutable memtable is flushed to disk as an **SSTable** — a sorted, compressed file with an index of keys.
5. The WAL covering the flushed memtable is truncated.

A read consults: the memtable, then each SSTable, in newest-to-oldest order. The first hit wins.

The memtable's choice of structure (skip list in RocksDB / LevelDB; RB-tree in some forks) trades insertion speed for memory overhead. Skip lists offer easy concurrent inserts, important since writes hit the memtable at full rate.

***

# The compaction problem

Without compaction, the number of SSTables grows linearly with writes. Reads slow proportionally — checking 1000 SSTables for a single key is unacceptable.

**Compaction** merges SSTables. It reads multiple SSTables (in sorted-key order — they're already sorted, so this is a streaming merge), produces a new larger sorted SSTable, and deletes the old ones. Tombstones (deletion markers) are also resolved during compaction.

The compaction job runs continuously in the background, consuming CPU and bandwidth proportional to the write rate. *Compaction must keep up with writes*, otherwise the SSTable count grows unboundedly.

***

# Compaction strategies: leveled vs tiered

Two main strategies; choice depends on read/write balance.

### Leveled compaction (LevelDB, RocksDB default)

SSTables are organised into *levels* `L0, L1, L2, …`. Each level is `T` times the size of the previous (`T = 10` typical).

- `L0` accepts flushes from the memtable; SSTables in `L0` may have overlapping key ranges.
- `L_i` (for `i ≥ 1`) maintains *non-overlapping* key ranges within the level. To insert a new SSTable into `L_i`, merge it with overlapping `L_(i+1)` SSTables.

Read amplification: at most one SSTable per level needs checking (because levels are non-overlapping). With 7 levels, ~7 SSTable checks per read.

Write amplification: every key is rewritten ~10× as it migrates through levels (each level transition is a merge with the next).

### Tiered compaction (Cassandra default)

SSTables are grouped into *tiers* by size. When a tier accumulates `K` SSTables (often `K = 4`), they're merged into one SSTable in the next tier.

Read amplification: must check every tier — typically 5-10 tiers, each with up to `K-1` overlapping SSTables. Worse than leveled.

Write amplification: each key written `~log_K n` times. Better than leveled.

The trade-off: leveled optimises reads, tiered optimises writes. RocksDB lets you choose; Cassandra used tiered by default until 4.0 and now offers both.

***

# Reads with multiple SSTables

A read for key `K`:

1. Check the memtable. Hit → return.
2. Check the immutable memtable (if any). Hit → return.
3. For each SSTable, newest first:
   a. Check the SSTable's **Bloom filter**. If "definitely not", skip the SSTable.
   b. Otherwise, binary-search the SSTable's index. Read the data block from disk if found.

The Bloom filter (covered in the [Bloom Filter chapter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-bloom-filter)) is critical. Without it, every SSTable would need a disk seek per read; with it, only the SSTables actually containing the key are read.

***

# The write-amplification trade-off

The disk writes are sequential (good), but the *total* bytes written per logical write is more than 1× — the compaction overhead.

For leveled compaction with `T = 10` and 7 levels: write amplification is ~70×. For every 1 GB of logical writes, the disk sees ~70 GB of writes after all compactions complete.

This is the LSM tree's hidden cost. SSDs have finite write endurance; high write amplification translates to early SSD failure. Modern systems have flexible compaction policies (RocksDB has 8+ knobs) to tune the read/write/space-amplification triangle.

The "**RUM conjecture**" (Athanassoulis et al., 2016) formalises this: any storage system trades off **R**ead, **U**pdate, and **M**emory amplifications. LSM trees minimise update amplification (sequential writes) at the cost of read and memory.

***

# Edge cases and pitfalls

- **Tombstones don't free space immediately.** A `DELETE` writes a tombstone; the tombstone persists until a compaction merges it with the original key (which then drops both). Heavy delete workloads bloat SSTables.
- **TTL expiration relies on compaction.** Cassandra's TTL feature marks entries with an expiry; entries are dropped during compaction, not at the moment they expire. A row with TTL = 1 day might still be readable for hours after expiration.
- **Read-after-write may need to check the memtable.** The most recent write isn't yet in any SSTable. Reads must always check the memtable first.
- **Write stalls under heavy load.** If compaction can't keep up, RocksDB stalls writes (slows them down) to give compaction breathing room. Tuning compaction throughput vs. write throughput is an art.
- **Bloom filter false positives.** A read that hits a Bloom filter false positive incurs a wasted disk read. The default 1% FPR is fine for most workloads; lower it (1 KB extra per SSTable) for read-heavy workloads.
- **Level-0 files can overlap in keys.** L0 SSTables haven't been compacted yet; multiple L0 files might contain the same key. Reads check all L0 files.

***

# Production reality

- **RocksDB** (Facebook) — the canonical embedded LSM tree. Used by Apache Kafka (state stores), CockroachDB (SQL), Apache Flink (state), MongoDB (WiredTiger), Cassandra-fork ScyllaDB. Source at [github.com/facebook/rocksdb](https://github.com/facebook/rocksdb).
- **LevelDB** (Google) — the original LSM tree implementation; predecessor to RocksDB. Smaller, fewer features, still maintained.
- **Cassandra** — the canonical distributed LSM database. Per-node storage is LSM; replication and consistency are layered on top.
- **HBase** — the Hadoop ecosystem's LSM database, similar architecture to Cassandra.
- **InfluxDB**, **TimescaleDB** — time-series databases using LSM-like storage for write-heavy ingestion.
- **Bigtable** (Google) — the original LSM-inspired distributed key-value store; described in the 2006 paper that spawned this entire family.
- **DynamoDB** (Amazon) — internally uses an LSM-tree-like structure for some storage tiers.

***

# Cross-links

- **Prerequisites:** [B-Tree](/cortex/data-structures-and-algorithms/trees-b-tree-introduction-to-b-trees) (the read-optimised alternative), [Skip List](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-skip-list) (memtable choice), [Bloom Filter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-bloom-filter) (read optimisation).
- **Sibling production deep-dive:** [Postgres B-Tree](/cortex/data-structures-and-algorithms/dsa-in-real-systems-postgres-b-tree-and-the-write-path).

***

# Final takeaway

LSM trees are the write-optimised storage primitive of the modern era. Three patterns to internalise:

1. **Sequential writes, periodic merge.** All writes go into a memtable and a WAL; flushes produce immutable SSTables; compaction merges them. Disk I/O is sequential, never random.
2. **Read amplification is the cost.** Reads check the memtable plus all SSTables. Bloom filters mitigate; compaction strategy controls how aggressively SSTables are merged.
3. **The trade-off triangle.** Read, update, and memory amplifications are constrained by the RUM conjecture. LSM minimises update amplification at the cost of the others. B-trees do the opposite. Pick by workload.
