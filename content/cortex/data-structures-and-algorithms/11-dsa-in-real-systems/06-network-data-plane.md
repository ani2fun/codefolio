---
title: "Network Data Plane: Radix Tries in Routing Tables"
summary: "Every packet sent on the internet hits a routing table. The Linux kernel's `fib_trie.c` is a level-compressed (LC) radix trie that resolves longest-prefix matches in tens of nanoseconds. The data structure between you and every byte you send."
prereqs:
  - trees-trie-introduction-to-tries
  - concurrency-and-systems-rcu-and-hazard-pointers
---

# 6. Network Data Plane: Radix Tries in Routing Tables

## The Hook

Every packet leaving your machine — every IP header, every TCP SYN, every DNS query — passes through a routing decision: *which interface should this packet go out on?* The decision involves matching the destination IP address against the routing table, picking the entry with the **longest matching prefix**.

For a routing table with thousands of entries (or hundreds of thousands on backbone routers), this lookup happens *millions of times per second*. The data structure underneath is a heavily-optimised **radix trie**.

This chapter is a tour of the Linux kernel's IP routing trie, in `net/ipv4/fib_trie.c`. By the end you'll understand:

- Why a binary trie of 32-bit IP addresses isn't enough.
- The level-compression (LC-trie) optimisation that flattens the trie.
- How RCU lets readers traverse the trie without locks.
- Why `route` lookups don't allocate memory in the fast path.

It's the canonical "data structure performance under hard real-time constraints" story.

---

## Table of contents

1. [Longest-prefix match](#longest-prefix-match)
2. [A binary trie of IP addresses](#a-binary-trie-of-ip-addresses)
3. [Path compression (Patricia trie)](#path-compression-patricia-trie)
4. [Level compression (LC-trie)](#level-compression-lc-trie)
5. [RCU for lock-free reads](#rcu-for-lock-free-reads)
6. [The fast path](#the-fast-path)
7. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# Longest-prefix match

A routing table entry has the form `(prefix, prefix_length, next_hop)`. For example:

```
0.0.0.0/0       → eth0   # default route
192.168.0.0/16  → eth1   # local network
192.168.1.0/24  → eth2   # specific subnet
```

For a packet destined for `192.168.1.42`:

- It matches `0.0.0.0/0` (everything matches).
- It matches `192.168.0.0/16` (because `192.168.x.x`).
- It matches `192.168.1.0/24` (because `192.168.1.x`).

The **longest** matching prefix wins. The packet goes out on `eth2`. The routing logic must find this longest match efficiently — in tens of nanoseconds, on every packet.

***

# A binary trie of IP addresses

Treat each IP address as a 32-bit string of bits. A binary trie has up to 32 levels; each internal node has two children (one for bit 0, one for bit 1). To insert prefix `192.168.1.0/24`, walk 24 bits into the trie and mark the node "next_hop = eth2".

To look up `192.168.1.42`:

1. Walk down the trie following the bits of the address.
2. Track the deepest "marked" node encountered along the path.
3. Return that node's next_hop.

Up to 32 pointer chases per lookup. For a million packets per second, that's 32 million pointer chases — each taking ~5 ns of L1-cache time, or ~50 ns of L3-cache time.

***

# Path compression (Patricia trie)

A binary trie wastes space on long single-child chains. If only one prefix exists in some part of the address space, the trie has a chain of single-child nodes — pure overhead.

**Patricia tries** (Practical Algorithm to Retrieve Information Coded in Alphanumeric) collapse such chains. Each edge is labelled with a *bit position* and a *bit value*. To traverse, jump directly to the labelled bit position and check.

For a routing table of 100k entries, path compression typically reduces node count by 5-10×. Lookups still take up to 32 bit-tests, but each one might skip many levels of the original trie.

***

# Level compression (LC-trie)

The Linux kernel goes further: **level compression** (LC-trie). Each internal node, instead of being binary, is *2^b-ary* for some `b ≥ 1`. The node tests `b` bits at a time, branching into one of `2^b` children.

When `b = 4`, each node has 16 children — testing 4 bits per step. Lookup depth: 32 / 4 = 8 levels max. Each level is a single memory fetch (the children are stored as a contiguous array indexed by the 4 bits).

The kernel adapts `b` per node based on the density of prefixes in that subtree. Sparse subtrees use `b = 1` (binary); dense ones use `b = 4` or higher. This minimises both depth and memory.

The kernel's structure in `net/ipv4/fib_trie.c`:

```c
struct key_vector {
    t_key key;
    unsigned char pos;       // bit position within the key
    unsigned char bits;      // number of bits this node tests (the "b")
    unsigned char slen;      // (used in the LC-trie variant)
    union {
        struct hlist_head leaf;
        struct {
            struct key_vector __rcu *tnode[0];   // child pointers, length 2^bits
        };
    };
};
```

The `tnode[0]` is a "flexible array member" — the kernel allocates the `key_vector` struct plus `2^bits` pointers contiguously, in a single allocation. Cache-line-friendly.

***

# RCU for lock-free reads

Routing table lookups happen on every packet. Lookups must be *lock-free* — taking even a spinlock per packet would be unacceptable.

The Linux kernel uses [RCU](/cortex/data-structures-and-algorithms/concurrency-and-systems-rcu-and-hazard-pointers) for the trie. Lookups are pure reads, no synchronisation. Updates (`ip route add`) are rare and synchronised via `rtnl_lock`. Old trie versions are freed after a grace period, ensuring no in-flight lookups hold stale references.

The `__rcu` annotation on `tnode[]` tells the compiler (and Linux's `sparse` static analyzer) that these pointers must be accessed via `rcu_dereference()`, ensuring memory barriers on weak-memory architectures.

***

# The fast path

The "fast path" is the lookup function called per packet. In `net/ipv4/fib_trie.c`:

```c
int fib_table_lookup(struct fib_table *tb, const struct flowi4 *flp,
                     struct fib_result *res, int fib_flags) {
    struct key_vector *cn, *n, *pn;
    // ... several local variables, all stack-allocated; no heap allocations.

    pn = tb->tb_root;
    cn = pn->tnode[0];

    while (cn) {
        // ... level-compressed descent
        cn = rcu_dereference(cn->tnode[index]);
    }
    // ... longest-prefix match wraps it up
}
```

Crucial properties:

- **No heap allocations.** All variables are on the stack; no `kmalloc` calls in the lookup.
- **No locks.** Pure RCU reads.
- **Cache-friendly.** Children are stored contiguously; one cache line per level descent.
- **Short.** Inlined and aggressively optimised by the compiler.

A tuned routing table lookup on modern hardware: 50-100 nanoseconds. At 1 Gbps line rate (1 million packets per second), 1000 ns per packet is the budget. The trie lookup uses 5-10% of that budget; the rest goes to NIC processing, packet copies, and protocol logic.

***

# Edge cases and pitfalls

- **Trie updates require RTNL lock.** Adding or removing a route holds the global routing-table lock; multiple updates serialise. This is fine because route updates are rare.
- **IPv6 (128-bit) is harder.** A 128-bit binary trie is 4× deeper than IPv4. The kernel's `fib6_tree.c` handles IPv6 with similar techniques but slightly different trade-offs.
- **VRF (virtual routing and forwarding).** Modern networking has multiple routing tables per system (per-namespace, per-VRF). Each is a separate trie.
- **Multipath ECMP.** When multiple equal-cost routes exist, the kernel hashes the flow's 5-tuple to pick a specific next-hop. The trie still resolves the prefix; the multipath choice is post-resolution.
- **TCAMs in hardware.** Switches and routers don't use software tries — they use **TCAMs** (Ternary Content-Addressable Memory), specialised hardware that does longest-prefix match in *one cycle*. The Linux trie is for software-defined networking; the data plane in real switches is in silicon.
- **The `route` command is deprecated.** Use `ip route` for kernel routing tables on modern Linux.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. Every IP packet leaving your machine hits this trie; knowing how it works is networking-engineering currency.

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> What operation is the IP routing table doing on every packet?</summary>

**A:** **Longest-prefix match**: find the routing entry whose prefix matches the destination IP, picking the *longest* such prefix.

</details>

<details>
<summary><strong>Q:</strong> Why not a binary trie of 32-bit IP addresses?</summary>

**A:** Up to 32 levels deep. Linux uses a level-compressed (LC) trie: each node tests `b` bits at a time, branching `2^b` ways. Depth `~8` for IPv4.

</details>

<details>
<summary><strong>Q:</strong> Typical lookup latency for the Linux fib_trie?</summary>

**A:** 50-100 ns per packet on modern hardware. At 1 Gbps line rate (1M packets/sec), the trie uses 5-10% of the per-packet budget.

</details>

<details>
<summary><strong>Q:</strong> Why is the trie structure read-mostly?</summary>

**A:** Routing-table updates (adding/removing routes) are rare; lookups happen per packet. The kernel uses RCU to make reads lock-free.

</details>

<details>
<summary><strong>Q:</strong> What synchronisation does the lookup path use?</summary>

**A:** **None for reads** — RCU lets readers traverse without locks. **Writers acquire `rtnl_lock`** (the global routing-table lock; rare path).

</details>

<details>
<summary><strong>Q:</strong> Why no heap allocations in the fast path?</summary>

**A:** The fib_trie lookup is per-packet hot path; allocation cost would dominate. All lookup state is on the stack.

</details>

<details>
<summary><strong>Q:</strong> What's a TCAM and why does hardware use one instead of fib_trie?</summary>

**A:** **Ternary Content-Addressable Memory** does longest-prefix match in *one cycle* via parallel bit-comparison. Used by switch/router silicon; software-defined routers use fib_trie.

</details>

<details>
<summary><strong>Q:</strong> What does "VRF" mean and how does it affect the trie?</summary>

**A:** **Virtual Routing and Forwarding** — multiple routing tables per system (per-namespace, per-tenant). Each is a separate fib_trie instance.

</details>

## Source pointers

```
net/ipv4/fib_trie.c             — IPv4 LC-trie implementation
net/ipv4/fib_lookup.h           — lookup interface
net/ipv4/fib_semantics.c        — fib_alias, fib_info: route metadata
include/net/ip_fib.h            — public API: fib_lookup, fib_table_lookup
net/ipv6/ip6_fib.c              — IPv6 routing trie (different impl)
```

Reading order:

```
1. include/net/ip_fib.h                — entry points
2. net/ipv4/fib_trie.c::fib_table_lookup — the per-packet fast path
3. net/ipv4/fib_trie.c::fib_table_insert — the update path (rtnl_lock held)
4. Documentation/RCU/                  — to understand the lock-free reads
```

User-space tooling:

```
ip route show                  — current routing table
ip route get <dest>            — what would happen for this destination
ip rule                        — policy routing (selects which trie/VRF)
ss -t                          — TCP socket state (touches the trie indirectly)
```

## Pattern triggers

- **"Lock-free read-mostly structure in a kernel"** → RCU + appropriate trie/tree
- **"Per-packet processing budget"** → 1000 ns at 1 Gbps; trie lookup must fit in tens of ns
- **"Longest-prefix match"** → radix / Patricia / LC-trie
- **"Hardware vs software routing"** → TCAM (silicon) vs fib_trie (software)
- **"Multiple routing tables per box"** → VRF / namespace; each gets its own fib_trie
- **"Routing change causing latency spike"** → trie update path takes `rtnl_lock`; coordinated rollout helps
- **"How do switches do this in microseconds?"** → TCAM in silicon; not the same algorithm
- **"Where to read the source?"** → `net/ipv4/fib_trie.c::fib_table_lookup`

***

# Cross-links

- **Prerequisites:** [Trie](/cortex/data-structures-and-algorithms/trees-trie-introduction-to-tries), [RCU and Hazard Pointers](/cortex/data-structures-and-algorithms/concurrency-and-systems-rcu-and-hazard-pointers).
- **Source reference:** [`net/ipv4/fib_trie.c`](https://github.com/torvalds/linux/blob/master/net/ipv4/fib_trie.c) in the Linux kernel.

***

# Final takeaway

Routing tables are tries under hard real-time constraints. Three patterns to internalise:

1. **Longest-prefix match is the operation.** Every IP packet hits this lookup. Microseconds matter.
2. **Level compression beats path compression for dense data.** A 16-way LC-trie has depth `log_16(2³²) = 8` for IPv4. Eight memory accesses per lookup.
3. **RCU + cache-aware layout = real-time read performance.** No locks on the fast path; allocations only on rare updates; siblings packed for cache locality. The Linux IP routing trie is one of the canonical "data structures under real-time constraints" examples in production code.

This concludes the "DSA in Real Systems" module — six chapters connecting the abstract data structures of the curriculum to the production codebases that run the internet, your laptop, and your version control. Reading the source of any one of them is a master class. Reading all six is a curriculum on its own.
