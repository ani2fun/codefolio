# DSA in Real Systems

The synthesis module. Every chapter here is a **deep tour into one real codebase** — Postgres, Linux, Redis, Git, RocksDB, the BGP routing table — built around the question "where does the textbook data structure I just learnt actually live, and what does the real implementation look like?". The textbook gives you the algorithm. This module shows you the production-hardened version, with cache-line considerations, NUMA awareness, and a decade of bug fixes baked in.

## Place in the curriculum

- **Prerequisites:** every prior module. This is where the curriculum's threads come together.
- **Capstone:** there's no "next module" — this is where the book ends. Each chapter here is a starting point for *that* system, not for further DSA.

## Chapters

1. [Postgres B-Tree and the Write Path](/cortex/data-structures-and-algorithms/dsa-in-real-systems-postgres-b-tree-and-the-write-path) — `nbtree/` source tour; how an `INSERT` finds its leaf, how WAL records the change, why Postgres uses B-link variant of B+-trees.
2. [Linux Red-Black Tree in the CFS Scheduler](/cortex/data-structures-and-algorithms/dsa-in-real-systems-linux-red-black-tree-in-the-cfs-scheduler) — `lib/rbtree.c` and `kernel/sched/fair.c`; why CFS picks RB over AVL; cached leftmost.
3. [Redis Internal Encodings](/cortex/data-structures-and-algorithms/dsa-in-real-systems-redis-internal-encodings) — `ziplist`, `quicklist`, `intset`, `listpack`, `skiplist`; the encoding that backs your data depends on its size.
4. [Git's Merkle DAG](/cortex/data-structures-and-algorithms/dsa-in-real-systems-git-merkle-dag) — trees, commits, blobs, and the content-addressed graph that makes `git log`, `git merge`, and `git diff` work.
5. [LSM Trees in RocksDB and Cassandra](/cortex/data-structures-and-algorithms/dsa-in-real-systems-lsm-trees-rocksdb-cassandra) — the write-optimised alternative to B-trees; memtables, SSTables, compaction strategies.
6. [Network Data Plane: Radix Tries in Routing Tables](/cortex/data-structures-and-algorithms/dsa-in-real-systems-network-data-plane) — the Linux kernel's LC-trie for IP prefix lookup; how a single `ip route` lookup happens for every packet.
