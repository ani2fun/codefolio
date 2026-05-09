---
title: CAS and Atomics
summary: Compare-And-Swap is the single hardware primitive every lock-free data structure is built on. Memory ordering, ABA, and the load-linked / store-conditional alternative on ARM and POWER.
prereqs:
  - foundations-memory-model-and-cache
---

# 1. CAS and Atomics

## The Hook

You have two threads incrementing the same counter. With a regular `count++`, you race: both threads read the same value, both write back the same incremented value, you lose an increment. The simple fix is a mutex around the increment — but mutex acquisition costs hundreds of cycles, and in pathological cases (contention on a hot variable) the mutex becomes the bottleneck.

The hardware-level alternative is **Compare-And-Swap (CAS)** — a single atomic instruction that:

1. Reads a memory location's current value.
2. Compares it to an expected value.
3. If equal, writes a new value and returns success.
4. Otherwise leaves memory unchanged and returns failure.

Step (1)+(2)+(3) are atomic — no other thread can interleave between them. With CAS, the increment becomes:

```
do {
    old = counter;
    new = old + 1;
} while (!CAS(&counter, old, new));
```

If another thread beat you, the CAS fails, you retry. Lock-free: no thread blocks waiting for any other; failure just means "try again". Every mainstream lock-free data structure — concurrent queues, hash maps, stacks, lock-free graph algorithms — is built on CAS.

This chapter is the primitive. Memory ordering, the ABA problem, and the load-linked/store-conditional alternative on non-x86 architectures.

---

## Table of contents

1. [What CAS does](#what-cas-does)
2. [Building larger primitives on CAS](#building-larger-primitives-on-cas)
3. [Memory ordering](#memory-ordering)
4. [The ABA problem](#the-aba-problem)
5. [Load-linked / store-conditional](#load-linked-store-conditional)
6. [Implementation](#implementation)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Production reality](#production-reality)
9. [Cross-links](#cross-links)
10. [Final takeaway](#final-takeaway)

***

# What CAS does

Pseudo-code (atomic):

```
function CAS(addr, expected, new_value):
    if *addr = expected:
        *addr ← new_value
        return TRUE
    return FALSE
```

In hardware: x86-64 has `LOCK CMPXCHG`. ARM has `LDAXR`/`STLXR` (load-linked / store-conditional). POWER has `lwarx`/`stwcx`. C/C++ standard libraries expose `std::atomic::compare_exchange_strong`/`weak`. Java has `AtomicReference.compareAndSet`. Rust has `AtomicUsize::compare_exchange`.

CAS only works on word-sized values: 64-bit pointers or integers on 64-bit machines. Some architectures (x86 since Pentium) also have *double-CAS* (`LOCK CMPXCHG16B`) for 128-bit atomic operations.

***

# Building larger primitives on CAS

**Atomic increment:** the loop above. **Lock-free push to a stack:**

```
push(stack, item):
    do:
        old_top = stack.top
        item.next = old_top
    while (!CAS(&stack.top, old_top, item))
```

**Lock-free dequeue from a queue:** more involved (see [Lock-Free Queue](/cortex/data-structures-and-algorithms/concurrency-and-systems-lock-free-queue)).

**Atomic counter incrementing in steps of N:**

```
add_n(counter, n):
    do:
        old = counter
        new = old + n
    while (!CAS(&counter, old, new))
```

The pattern is universal: read, compute, attempt CAS, retry on failure.

***

# Memory ordering

Modern CPUs reorder memory operations for performance. A naive CAS without memory barriers might let later reads "see" the new value before earlier reads have completed — breaking the lock-free protocol.

The C++/Rust standard libraries expose **memory-ordering** options:

- **`relaxed`** — no inter-thread ordering. Single counters or independent variables.
- **`acquire`** (on reads) — pairs with `release` (on writes) — establishes a happens-before edge.
- **`release`** (on writes) — pairs with `acquire`.
- **`acq_rel`** — both directions on a read-modify-write (typical for CAS).
- **`seq_cst`** — sequentially consistent; the strongest, most expensive option.

CAS for lock-free data structures usually uses `acq_rel` (read with acquire semantics, write with release semantics). x86 hardware enforces strong ordering by default, so the cost is mostly compiler-inserted; ARM and POWER need explicit barriers.

```cpp
// C++ idiom
std::atomic<int> counter{0};
int expected = counter.load(std::memory_order_relaxed);
while (!counter.compare_exchange_weak(expected, expected + 1,
                                       std::memory_order_acq_rel,
                                       std::memory_order_relaxed)) { }
```

***

# The ABA Problem

A subtle bug specific to CAS-based code:

1. Thread A reads value `X`.
2. Thread B changes `X` to `Y` and back to `X`.
3. Thread A's CAS succeeds (sees `X`, writes new value), even though the underlying state has changed.

For *integer* CAS, this often doesn't matter. For *pointer* CAS, it can be catastrophic — the pointer value `X` might have been freed and a different node allocated at the same address. The old pointer's contents (which thread A is about to follow) are no longer the same.

Two standard fixes:

1. **Tagged pointers / version counters.** Pair each pointer with a counter; CAS on the (pointer, counter) tuple via 128-bit double-CAS. Each modification bumps the counter, so even if the pointer recycles, the counter differs.
2. **Hazard pointers / RCU** (covered in [RCU and Hazard Pointers](/cortex/data-structures-and-algorithms/concurrency-and-systems-rcu-and-hazard-pointers)). Defer freeing of nodes that might still be referenced.

Java's garbage collector eliminates ABA for pointer CAS automatically — a freed node can't be reallocated until *every* reader has dropped its reference.

***

# Load-linked / store-conditional

ARM, POWER, and RISC-V don't expose CAS directly. Instead they expose **LL/SC**:

- **LL (load-linked):** read a value and "register" the address.
- **SC (store-conditional):** write to that address *only if* nothing else has written to it since the LL.

LL/SC is more flexible than CAS — you can do *any* operation between LL and SC, not just compare-and-swap. The CPU tracks "exclusivity" of the cache line; if another core writes, the SC fails.

CAS can be implemented atop LL/SC:

```
CAS(addr, expected, new):
    do:
        cur = LL(addr)
        if cur ≠ expected: return FALSE
    while (!SC(addr, new))
    return TRUE
```

LL/SC also avoids ABA inherently — the SC fails if the cache line was touched at all, regardless of whether the value is the same.

***

# Implementation

Lock-free counter increment:

```python run
import threading

class AtomicCounter:
    """Python doesn't expose CAS in the language; using a lock as a stand-in."""
    def __init__(self):
        self.value = 0
        self.lock = threading.Lock()

    def increment(self):
        with self.lock:
            self.value += 1
            return self.value


# Demonstrate threaded counting
def worker(counter, n):
    for _ in range(n):
        counter.increment()

if __name__ == "__main__":
    c = AtomicCounter()
    threads = [threading.Thread(target=worker, args=(c, 10000)) for _ in range(4)]
    for t in threads: t.start()
    for t in threads: t.join()
    print(f"final count: {c.value}    (expected {4 * 10000})")
```

```java run
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

class Solution {
    static final AtomicInteger counter = new AtomicInteger(0);

    static int incrementCAS() {
        int oldVal, newVal;
        do {
            oldVal = counter.get();
            newVal = oldVal + 1;
        } while (!counter.compareAndSet(oldVal, newVal));
        return newVal;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int t = 0; t < 4; t++) {
            pool.submit(() -> { for (int i = 0; i < 10000; i++) incrementCAS(); });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("final count: " + counter.get());
    }
}
```

```c run
#include <stdio.h>
#include <pthread.h>
#include <stdatomic.h>

atomic_int counter = 0;

void *worker(void *arg) {
    for (int i = 0; i < 10000; i++) {
        int old, new;
        do {
            old = atomic_load(&counter);
            new = old + 1;
        } while (!atomic_compare_exchange_weak(&counter, &old, new));
    }
    return NULL;
}

int main(void) {
    pthread_t threads[4];
    for (int i = 0; i < 4; i++) pthread_create(&threads[i], NULL, worker, NULL);
    for (int i = 0; i < 4; i++) pthread_join(threads[i], NULL);
    printf("final count: %d (expected %d)\n", atomic_load(&counter), 4 * 10000);
    return 0;
}
```

```scala run
import java.util.concurrent.atomic.AtomicInteger

object Solution {
  val counter = new AtomicInteger(0)

  def incrementCAS(): Int = {
    var oldVal = 0; var newVal = 0
    while ({ oldVal = counter.get(); newVal = oldVal + 1; !counter.compareAndSet(oldVal, newVal) }) {}
    newVal
  }

  def main(args: Array[String]): Unit = {
    val threads = (0 until 4).map { _ =>
      new Thread(() => for (_ <- 0 until 10000) incrementCAS())
    }
    threads.foreach(_.start())
    threads.foreach(_.join())
    println(s"final count: ${counter.get()}")
  }
}
```

***

# Edge cases and pitfalls

- **CAS-loop livelock.** Two threads contending on the same CAS keep failing for each other. In practice, this resolves due to randomisation in CPU scheduling, but pathological cases exist. Add backoff or yield if you observe livelock.
- **CAS on misaligned addresses.** Most architectures require word-aligned CAS targets. Crashes or incorrect behaviour otherwise.
- **`compare_exchange_strong` vs `weak`.** Strong guarantees no spurious failures; weak may fail spuriously even when the comparison would succeed. Weak is faster on ARM/POWER (LL/SC); use it inside loops.
- **CAS doesn't make algorithms correct on its own.** A naive lock-free implementation can have data races on parts unprotected by CAS. Reason carefully about every shared variable.
- **The ABA problem strikes when you least expect it.** Pointer-based lock-free structures *will* have ABA bugs unless you address them explicitly.
- **Memory ordering bugs are non-deterministic.** They show up under high contention or specific CPU architectures (ARM more than x86). Test on the target hardware, not just on a development laptop.

***

# Production reality

- **Java's `java.util.concurrent.atomic`** package — `AtomicInteger`, `AtomicReference`, `AtomicLongArray`. The JVM compiles `compareAndSet` to platform-specific atomic instructions.
- **C++ `std::atomic`** in `<atomic>`. Used in Folly (Facebook), Google Abseil, Boost.Lockfree.
- **Rust `std::sync::atomic`** with explicit memory ordering. Used in Tokio, async-std, the Rust standard library's `Arc` and `Mutex`.
- **The Linux kernel** uses CAS extensively in lock-free data structures: futexes, RCU, per-CPU counters, work-stealing queues in the scheduler.
- **Database engines.** PostgreSQL's `LWLock`, MySQL's InnoDB transaction system, RocksDB's lock-free memtable — all use CAS for low-contention paths.
- **High-frequency trading.** Lock-free queues built on CAS are standard in HFT systems where every microsecond matters.
- **JCTools and Disruptor.** Two well-known Java libraries for high-performance lock-free queues, both built on CAS.

***

# Cross-links

- **Prerequisite:** [Memory Model and Cache](/cortex/data-structures-and-algorithms/foundations-memory-model-and-cache) — the cache-coherence behaviour CAS depends on.
- **Used by:** every other chapter in this module — [Lock-Free Queue](/cortex/data-structures-and-algorithms/concurrency-and-systems-lock-free-queue), [Concurrent Hash Map](/cortex/data-structures-and-algorithms/concurrency-and-systems-concurrent-hash-map), [RCU and Hazard Pointers](/cortex/data-structures-and-algorithms/concurrency-and-systems-rcu-and-hazard-pointers).

***

# Final takeaway

CAS is the foundation of lock-free programming. Three patterns to internalise:

1. **Read, compute, retry on failure.** The CAS-loop pattern is the universal idiom for lock-free updates.
2. **Memory ordering is real.** `relaxed` for independent counters, `acq_rel` for synchronisation, `seq_cst` when you need a global order. The wrong choice is a bug that surfaces only on weak-memory hardware.
3. **ABA is the lock-free programmer's tax.** Tagged pointers, hazard pointers, RCU — pick a strategy, document it, test it. Pointer CAS without ABA mitigation is almost always a bug.
