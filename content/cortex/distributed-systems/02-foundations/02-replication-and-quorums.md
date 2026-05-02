---
title: Replication and Quorums
summary: Why we replicate, the three replication strategies, and how quorum arithmetic gives us a knob between consistency and availability.
---

## Why replicate at all

There are three reasons we keep multiple copies of the same data:

1. **Durability** — a single disk dies, the data survives.
2. **Availability** — a single machine reboots, the system keeps serving.
3. **Read throughput** — clients can fan out reads across replicas.

The cost is that writes now need to propagate to multiple places, and the system has to decide *when* a write counts as "done."

## Three replication strategies

### Single-leader

One replica is the leader; all writes go through it. Followers replicate from the leader. Reads can go to any replica (with stale-read implications) or only to the leader (consistent but a bottleneck).

### Multi-leader

Multiple replicas can accept writes. Conflicts can occur and must be resolved — typically with vector clocks, last-write-wins, or CRDTs.

### Leaderless

No designated leader. Clients write to several replicas in parallel and read from several, using quorum arithmetic to ensure overlap. This is the Dynamo / Cassandra style.

## Quorum arithmetic

For a cluster of $N$ replicas, a write quorum $W$ and a read quorum $R$, the strong-consistency rule is:

$$
W + R > N
$$

The intuition: any read quorum must overlap with any write quorum by at least one node, so a read is guaranteed to hit at least one replica that has the latest write.

A few common configurations on $N = 3$:

| W | R | Semantics                                                                       |
| - | - | ------------------------------------------------------------------------------- |
| 3 | 1 | Read-optimized, expensive writes (rare today)                                   |
| 1 | 3 | Write-optimized, expensive reads                                                |
| 2 | 2 | Balanced; tolerates one node failure                                            |
| 1 | 1 | Available but eventually consistent — $W + R = 2 \le N$                          |

The last row is interesting: it satisfies neither half of the inequality, so it's "AP" in CAP terms. Conflicts will happen and must be reconciled.

## A code sketch of leaderless replication

```typescript
type Replica = { put(k: string, v: string): Promise<void>; get(k: string): Promise<string | null> };
const replicas: Replica[] = [/* N=3 replicas */];

async function quorumPut(key: string, value: string, w: number) {
    const ops = replicas.map((r) => r.put(key, value).then(() => true).catch(() => false));
    const results = await Promise.allSettled(ops);
    const ok = results.filter((r) => r.status === "fulfilled" && r.value).length;
    if (ok < w) throw new Error(`write quorum not met: ${ok}/${w}`);
}

async function quorumGet(key: string, r: number): Promise<string | null> {
    const reads = await Promise.all(
        replicas.map((replica) => replica.get(key).catch(() => null))
    );
    const values = reads.filter((v): v is string => v !== null);
    if (values.length < r) throw new Error(`read quorum not met: ${values.length}/${r}`);
    return resolveConflict(values);
}
```

The `resolveConflict` function is where the real work lives — last-write-wins, vector clocks, application-specific merge — and a topic for its own chapter.

## What quorum systems still get wrong

Even with $W + R > N$, leaderless quorum systems aren't truly linearizable. They can violate it during failure handoff, sloppy quorums (where the cluster temporarily admits non-member nodes), or read-repair races. Strict linearizability needs consensus — Paxos or Raft — which we'll cover next.

## Failure modes worth knowing

A non-exhaustive list of things that *will* go wrong:

- [ ] **Replication lag** — followers fall behind; a read after write returns stale data.
- [ ] **Split brain** — two leaders during a partition both accept writes.
- [ ] **Lost updates** — concurrent writes overwrite each other.
- [ ] **Hinted handoff anomalies** — a write is accepted by a temporary node and forgotten.
- [ ] **Read-your-writes failure** — a client's own write isn't visible on the next read.

Knowing the names is half the battle; the other half is recognizing them in production at 3am.

## Summary

Replication is what lets distributed systems be useful. Quorums are how we tune the consistency–availability dial. The papers worth reading next are:

- [Dynamo: Amazon's Highly Available Key-value Store](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf)
- [Cassandra — A Decentralized Structured Storage System](https://www.cs.cornell.edu/projects/ladis2009/papers/lakshman-ladis2009.pdf)

Read them with the inequality $W + R > N$ in mind, and the design choices will read like inevitabilities.
