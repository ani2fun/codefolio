---
title: "Linux Red-Black Tree in the CFS Scheduler"
summary: "A tour of `lib/rbtree.c` and `kernel/sched/fair.c` — how the Linux Completely Fair Scheduler picks which thread to run next, in O(log n), via a red-black tree keyed by virtual runtime."
prereqs:
  - trees-red-black-tree-introduction-to-red-black-trees
---

# 2. Linux Red-Black Tree in the CFS Scheduler

## The Hook

Every time the Linux kernel decides which thread to run next, it queries a red-black tree.

The **Completely Fair Scheduler** (CFS), Linux's default scheduler since 2.6.23, treats every runnable task as a node in an RB-tree keyed by the task's `vruntime` ("virtual runtime"). To schedule the next task, CFS picks the **leftmost** node — the task that has run least so far. To account for the task that just ran, CFS removes it, updates its `vruntime`, and re-inserts. Both operations are `O(log n)`.

This chapter is the tour: from the kernel's generic-typed RB-tree (`lib/rbtree.c`) to the per-CPU run queue (`kernel/sched/fair.c`), to the choices that make CFS run on machines from a Raspberry Pi (5-10 tasks) to a 256-CPU server (thousands of tasks per run queue).

By the end you'll know why the kernel chose RB-trees over AVL, why per-CPU run queues exist, and how the leftmost-task pointer is maintained without scanning the tree.

---

## Table of contents

1. [The choice of RB-tree](#the-choice-of-rb-tree)
2. [`lib/rbtree.c` — the generic tree](#lib-rbtree-c-the-generic-tree)
3. [Per-CPU run queues](#per-cpu-run-queues)
4. [`vruntime` and the leftmost task](#vruntime-and-the-leftmost-task)
5. [Picking the next task](#picking-the-next-task)
6. [Load balancing across CPUs](#load-balancing-across-cpus)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The choice of RB-tree

Why RB-tree and not AVL, treap, or skip list?

| Concern | RB-tree | AVL | Treap | Skip list |
|---|---|---|---|---|
| Worst-case insert/delete | `O(log n)` (≤3 rotations) | `O(log n)` (≤2 rotations) | `O(log n)` expected | `O(log n)` expected |
| Worst-case search | `O(log n)` | `O(log n)` | `O(log n)` expected | `O(log n)` expected |
| Per-node overhead | 1 colour bit | 4 bytes (height) | 4 bytes (priority) | array of pointers |
| Mutation work | constant rotations | up to log n rotations | rotations + RNG | linked-list splices |
| Real-time predictability | yes (deterministic) | yes (deterministic) | no (RNG variance) | no (RNG variance) |

CFS needs *deterministic* worst-case bounds (a real-time-adjacent kernel can't accept "expected" performance), so probabilistic structures are out. Between RB and AVL, RB-tree's *fewer rotations on update* matters more than AVL's slightly tighter height — the kernel scheduler is write-heavy (every context switch involves an insert + delete), so write performance dominates.

The colour bit is also stored cleverly: the kernel packs it into the *low bit* of the parent pointer (which is always 0 due to alignment), so the per-node memory overhead is *zero*. Reading `rbtree.h`:

```c
struct rb_node {
    unsigned long  __rb_parent_color;   // parent pointer + colour bit
    struct rb_node *rb_right;
    struct rb_node *rb_left;
};
```

Three pointers per node, no extra bytes. The price: every parent-or-colour read is a bit-mask away.

***

# `lib/rbtree.c` — the generic tree

`lib/rbtree.c` is the kernel's generic RB-tree. It's *type-erased* — you embed an `rb_node` field in your struct, then the RB-tree functions operate on `rb_node` pointers without knowing your type. Lookup-by-key is delegated to a per-user comparison function (or, more often, inlined at the call site for performance).

```c
// kernel/sched/fair.c
struct sched_entity {
    struct rb_node run_node;
    u64 vruntime;
    // ... lots more
};
```

The kernel's tree uses macros (`rb_entry`, `container_of`) to translate between `rb_node*` and the containing `sched_entity*`. This is the C idiom for generic data structures pre-C11.

The five RB invariants (covered in the [Red-Black Tree chapter](/cortex/data-structures-and-algorithms/trees-red-black-tree-introduction-to-red-black-trees)) are maintained by `rb_insert_color` and `rb_erase_color`. Both are ~50 lines, identical in structure to CLRS's pseudocode but with the kernel's pointer-packing tricks.

***

# Per-CPU run queues

A multi-CPU system doesn't have *one* RB-tree; it has *one tree per CPU*, called the **CFS run queue** (`struct cfs_rq`). Each CPU's scheduler operates on its own tree, in its own cache.

```c
// kernel/sched/sched.h
struct cfs_rq {
    struct load_weight load;
    unsigned int nr_running;
    u64 min_vruntime;
    struct rb_root_cached tasks_timeline;   // ← the RB-tree of runnable tasks
    struct sched_entity *curr;              // ← currently running task
    // ... more
};
```

`tasks_timeline` is the per-CPU RB-tree. `min_vruntime` tracks the smallest `vruntime` across the tree (effectively, the leftmost task's vruntime). New tasks added to a CPU adjust their `vruntime` based on `min_vruntime` so they can compete fairly.

The `_cached` in `rb_root_cached` is the trick we'll see next.

***

# `vruntime` and the leftmost task

`vruntime` is the cumulative weighted time the task has run. CFS rewards CPU-bound tasks with high `vruntime`; sleeping tasks (blocked on I/O) accrue no vruntime. To enforce fairness, CFS picks the task with the smallest vruntime — the leftmost in the RB-tree.

A naive implementation walks the tree to find the leftmost — `O(log n)` per scheduling decision. Fast, but per-tick overhead matters.

The kernel uses **`rb_root_cached`**, a small wrapper:

```c
struct rb_root_cached {
    struct rb_root rb_root;
    struct rb_node *rb_leftmost;       // cached leftmost pointer
};
```

Every insert/erase updates `rb_leftmost` (`O(1)` extra work to track it). Picking the next task is now a *pointer dereference*: `rq->tasks_timeline.rb_leftmost`. **`O(1)` to find the next task, regardless of run queue size.**

***

# Picking the next task

`pick_next_task_fair` (the entry point):

```c
static struct task_struct *
pick_next_task_fair(struct rq *rq, struct task_struct *prev, struct rq_flags *rf) {
    struct cfs_rq *cfs_rq = &rq->cfs;
    struct sched_entity *se;
    // ... bookkeeping
    se = rb_entry(rb_first_cached(&cfs_rq->tasks_timeline),
                  struct sched_entity, run_node);
    return task_of(se);
}
```

`rb_first_cached` is the `O(1)` leftmost lookup. `rb_entry` translates `rb_node*` to `sched_entity*`. `task_of` extracts the `task_struct`.

Once the task is selected, CFS removes it from the tree (`rb_erase_cached`), runs it, then on its preemption inserts it back with an updated `vruntime`. Each schedule cycle does `O(log n)` work for the insert+erase pair.

***

# Load balancing across CPUs

If one CPU's run queue has 100 tasks and another has 5, the system isn't fair. CFS periodically (and on certain events like task wakeup) **migrates** tasks between CPUs to balance load. The migration involves removing the task from one CPU's RB-tree and inserting into another's — `O(log n)` work per migrated task.

The migration policy considers cache locality (don't migrate too often) and NUMA topology (don't migrate across NUMA nodes). The full algorithm is in `kernel/sched/fair.c`'s `load_balance` function — about 1000 lines of nuanced code that's the difference between linear-scaling and falling-off-a-cliff scaling at high core counts.

***

# Edge cases and pitfalls

- **Real-time tasks bypass CFS.** `SCHED_FIFO` and `SCHED_RR` use a different scheduler class (`rt_sched_class`); they're not in the RB-tree. CFS only handles `SCHED_NORMAL`, `SCHED_BATCH`, `SCHED_IDLE`.
- **Cgroups complicate the picture.** Linux cgroups (used by Docker, Kubernetes) introduce *hierarchical* CFS: each cgroup has its own `cfs_rq`, and tasks are scheduled across nested run queues. The RB-tree is still the primitive but the topology is more complex.
- **CPU pinning bypasses load balancing.** `taskset` pins a task to specific CPUs; CFS won't migrate it.
- **`vruntime` precision.** The kernel uses unsigned 64-bit `vruntime`, which wraps after ~292 years of CPU time. Comparing `vruntime` uses signed-difference arithmetic to handle wrap correctly.
- **Latency vs throughput.** CFS optimises for latency-fairness (every task gets a share). For pure throughput, you might want `SCHED_BATCH` (CPU-bound, slightly de-prioritised).

***

# Cross-links

- **Prerequisite:** [Red-Black Tree](/cortex/data-structures-and-algorithms/trees-red-black-tree-introduction-to-red-black-trees).
- **Sibling production deep-dives:** [Postgres B-Tree](/cortex/data-structures-and-algorithms/dsa-in-real-systems-postgres-b-tree-and-the-write-path), [LSM Trees](/cortex/data-structures-and-algorithms/dsa-in-real-systems-lsm-trees-rocksdb-cassandra).
- **Source reference:** [`lib/rbtree.c`](https://github.com/torvalds/linux/blob/master/lib/rbtree.c), [`kernel/sched/fair.c`](https://github.com/torvalds/linux/blob/master/kernel/sched/fair.c).

***

# Final takeaway

The CFS scheduler is the canonical RB-tree-in-production. Three patterns to internalise:

1. **RB-tree wins on write-heavy workloads.** Constant-bounded rotation count beats AVL's log-bounded for schedulers, where every context switch is an insert+delete.
2. **Cached leftmost is the trick.** `rb_root_cached` makes `pick_next_task` `O(1)`, not `O(log n)`. A small structural addition with outsized impact.
3. **Per-CPU trees + load balancing scales linearly.** One RB-tree per CPU avoids contention. The load-balancing algorithm migrates tasks across trees to enforce fairness across cores. The combined system runs from 1 CPU to 256 CPUs without retuning.
