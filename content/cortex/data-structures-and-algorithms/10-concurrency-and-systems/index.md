# Concurrency and Systems

Every data structure in this book so far assumed **one thread**. The moment you have two — and that's every server, every database, every browser tab — the rules change. A regular linked-list insert is a few instructions; a *concurrent* linked-list insert that doesn't lose updates is a research paper. This module is the bridge between the textbook DSA you've learnt and the data structures real systems run.

## Place in the curriculum

- **Prerequisites:** every prior module. You need to know how the regular structures work before the concurrent versions make sense.
- **Followed by:** [DSA in Real Systems](/cortex/data-structures-and-algorithms/dsa-in-real-systems-index) (where the structures here show up in JVM internals, the Linux kernel, and distributed databases).

## Chapters

1. [CAS and Atomics](/cortex/data-structures-and-algorithms/concurrency-and-systems-cas-and-atomics) — the compare-and-swap primitive that all lock-free code is built on; ABA problem; memory ordering.
2. [Lock-Free Queue](/cortex/data-structures-and-algorithms/concurrency-and-systems-lock-free-queue) — the Michael-Scott queue every JVM and .NET runtime uses; producer/consumer at scale.
3. [Concurrent Hash Map](/cortex/data-structures-and-algorithms/concurrency-and-systems-concurrent-hash-map) — Java's `ConcurrentHashMap`; striping, lock-free reads, the linearizability guarantee.
4. [RCU and Hazard Pointers](/cortex/data-structures-and-algorithms/concurrency-and-systems-rcu-and-hazard-pointers) — the Linux kernel's preferred lock-free reclamation pattern.
5. [Distributed Data Structures (Teaser)](/cortex/data-structures-and-algorithms/concurrency-and-systems-distributed-data-structures-teaser) — CRDTs, Merkle trees, vector clocks; the move from "concurrent" to "distributed".
