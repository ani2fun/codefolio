---
title: HyperLogLog
summary: Estimate the number of distinct items in a stream using ~12 KB of memory, regardless of stream size. The trick: count leading zeros in hash values. Used by Redis, Google BigQuery, and every major analytics platform.
prereqs:
  - probabilistic-and-advanced-count-min-sketch
  - bit-tricks-pattern-kth-bit
---

# 4. HyperLogLog

## The Hook

You run a website. You want to answer: "how many *distinct* visitors did we have today?" Naive: hash set of visitor IDs. For 10 million distinct visitors with 16-byte UUIDs, that's 160 MB of memory just for one day's count. For multi-day rollups, gigabytes.

**HyperLogLog (HLL)** estimates this count using **~12 KB**, regardless of whether you've seen 10 thousand or 10 billion distinct visitors. Standard error: ~2%. The compression ratio is *millions to one*.

The trick: hash every item to a uniform 64-bit value. The number of leading zeros in the hash is a proxy for "rarity" — by chance, hashes with `k` leading zeros appear about `1/2^k` of the time, so seeing a hash with 30 leading zeros suggests roughly `2^30 ≈ 10⁹` distinct items have been seen. Multiple counter "buckets" reduce the variance.

This chapter is the algorithm. By the end you'll be able to estimate cardinality in 12 KB, recognise where HLL is used in production, and tune for the error you can tolerate.

---

## Table of contents

1. [The leading-zero trick](#the-leading-zero-trick)
2. [Bucketed estimation](#bucketed-estimation)
3. [The HLL algorithm](#the-hll-algorithm)
4. [Implementation](#implementation)
5. [Tuning `m`](#tuning-m)
6. [Edge cases and pitfalls](#edge-cases-and-pitfalls)
7. [Production reality](#production-reality)
8. [Cross-links](#cross-links)
9. [Final takeaway](#final-takeaway)

***

# The leading-zero trick

Hash every item to a uniform 64-bit value. For a uniformly-random hash, the probability of seeing `k` or more leading zeros is `1/2^k`.

If you've hashed `n` distinct items and the maximum number of leading zeros across all hashes is `R`, then `n ≈ 2^R`.

That's the entire idea. A single counter — "max leading zeros so far" — gives a rough estimate of the cardinality. **2 bytes** of memory.

The catch: a single counter has *enormous* variance. You might have seen 100 distinct items but happened to hash one to a value with 30 leading zeros (probability `1/10⁹` × 100 trials, very rare but possible). Estimate: `2^30 ≈ 10⁹`. Off by 7 orders of magnitude.

***

# Bucketed estimation

Split incoming items into `m` buckets based on the first few bits of the hash. Each bucket maintains its own "max leading zeros" counter. Average across buckets to reduce variance.

```
hash(x) = (b₁b₂...b_log_m) (rest of hash bits)
       └─────────────┘ └────────────────────┘
       bucket index    leading-zero count
```

Estimate `n ≈ α_m · m² / Σ 2^(-R_j)`, where the sum is over all `m` buckets and `α_m` is a bias-correction constant (~0.7).

The standard error scales as `1.04 / √m`. For 2% error, `m ≈ 2700` buckets. With 6-bit counters per bucket, that's `~2 KB` total memory.

Industry-standard "HyperLogLog++" uses `m = 16384` (2^14) buckets, 6-bit counters → 12 KB total, with ~1% error.

***

# The HLL algorithm

The Z-formula is the harmonic mean of `2^R[j]` — gives lower variance than arithmetic mean. The bias corrections are small adjustments for extreme cardinalities.

***

# Implementation

```python run viz=graph viz-root=E
import hashlib, math

class HyperLogLog:
    def __init__(self, p=14):
        self.p = p
        self.m = 1 << p                                            # 2^p buckets
        self.R = [0] * self.m
        # Bias-correction constant
        if self.m == 16: self.alpha = 0.673
        elif self.m == 32: self.alpha = 0.697
        elif self.m == 64: self.alpha = 0.709
        else: self.alpha = 0.7213 / (1 + 1.079 / self.m)

    def add(self, x):
        h = int(hashlib.sha256(str(x).encode()).hexdigest(), 16) & ((1 << 64) - 1)
        j = h >> (64 - self.p)                                     # top p bits
        w = (h << self.p) & ((1 << 64) - 1)                        # remaining bits, padded
        # Count leading zeros in w (in 64-bit space)
        if w == 0:
            rho = 64 - self.p + 1
        else:
            rho = 64 - w.bit_length() + 1
        self.R[j] = max(self.R[j], rho)

    def estimate(self):
        Z = sum(2.0 ** (-r) for r in self.R)
        E = self.alpha * self.m * self.m / Z
        # Small-cardinality correction
        if E <= 2.5 * self.m:
            zeros = self.R.count(0)
            if zeros > 0:
                E = self.m * math.log(self.m / zeros)
        return E


if __name__ == "__main__":
    import random
    random.seed(7)

    # Stress test: insert n distinct items, estimate
    for n in [100, 1_000, 10_000, 100_000, 1_000_000]:
        hll = HyperLogLog(p=14)
        for i in range(n):
            hll.add(f"item_{i}")
        est = hll.estimate()
        err = abs(est - n) / n * 100
        print(f"n={n:>10,d}  estimate={est:>12,.0f}  error={err:.2f}%   memory=12 KB")
```

```java run viz=graph viz-root=E
import java.security.MessageDigest;

public class Main {
    final int p = 14;
    final int m = 1 << p;
    final byte[] R = new byte[m];
    final double alpha = 0.7213 / (1 + 1.079 / m);

    void add(String x) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] d = sha.digest(x.getBytes());
        long h = 0;
        for (int i = 0; i < 8; i++) h = (h << 8) | (d[i] & 0xff);
        int j = (int) (h >>> (64 - p));
        long w = h << p;
        int rho = (w == 0) ? 64 - p + 1 : Long.numberOfLeadingZeros(w) + 1;
        if (rho > R[j]) R[j] = (byte) rho;
    }

    double estimate() {
        double Z = 0;
        for (byte r : R) Z += Math.pow(2, -r);
        return alpha * m * m / Z;
    }

    public static void main(String[] args) throws Exception {
        Main hll = new Main();
        for (int i = 0; i < 1_000_000; i++) hll.add("item_" + i);
        System.out.printf("estimate ~%.0f (true 1,000,000)%n", hll.estimate());
    }
}
```

***

# Tuning `m`

| `p` | `m = 2^p` | Standard error | Memory |
|---|---|---|---|
| 10 | 1024 | 3.25% | 768 bytes |
| 12 | 4096 | 1.625% | 3 KB |
| 14 | 16384 | 0.81% | 12 KB |
| 16 | 65536 | 0.41% | 48 KB |
| 18 | 262144 | 0.20% | 192 KB |

`p = 14` is the standard production setting (Redis HLL, Google's HyperLogLog++ paper). Below 1% error with 12 KB of memory is the sweet spot.

***

# Edge cases and pitfalls

- **Hash quality matters.** A bad hash function biases the leading-zero distribution and wrecks the estimate. Use SHA-256, MurmurHash3, or xxHash — never `hashCode()`.
- **Small cardinalities are inaccurate.** The basic estimator is wildly off for `n < 5m/2`. Use linear counting (count of empty buckets) for small `n`.
- **Cardinality overflow.** Beyond ~2^32 distinct items, the estimator can saturate. HyperLogLog++ extends to higher ranges with a slightly different encoding.
- **Don't add the same item twice and expect a different estimate.** HLL only tracks "max leading zeros seen". Re-adding has no effect. This is what makes HLL a *cardinality* estimator — it counts distinct.
- **Merging HLLs.** HLLs are *mergeable*: take the per-bucket max across two HLLs to get an HLL of their union. This is enormously useful for distributed systems — each shard maintains its own HLL, merge them at query time.

***

# Production reality

- **Redis `PFADD` / `PFCOUNT` / `PFMERGE`** — HyperLogLog as a first-class data type. ~12 KB per HLL. Used by major sites for unique-visitor counts.
- **Google BigQuery's `APPROX_COUNT_DISTINCT`** — under the hood, HLL with `p = 14`. The `EXACT` variant uses a hash set; `APPROX` is up to 100× faster on large inputs.
- **Apache Spark, Flink, Druid, Pinot** — all expose HyperLogLog primitives for distinct-counting in distributed analytics.
- **Apache DataSketches.** Yahoo's open-source library; the canonical HLL implementation in Java/C++.
- **Stripe, Cloudflare, Twitter** — use HLL for rolling windowed analytics where exact counts are too expensive.
- **Counting unique IPs hitting a CDN.** HLL is the only approach that scales to billions of IPs in tens of KB of memory per time-window.

***

# Memorize

The high-leverage facts to commit to long-term memory — atomic enough for an Anki card, concrete enough to recall under pressure or during production debugging. HLL is the magic of "1% error in 12 KB regardless of cardinality" — once you've internalised the leading-zero trick, every distinct-count question becomes "should I use HLL?"

## Quick recall

Click any question to reveal the answer.

<details>
<summary><strong>Q:</strong> What does HLL estimate?</summary>

**A:** Cardinality (number of distinct items) of a stream, regardless of stream size.

</details>
<details>
<summary><strong>Q:</strong> Memory at <code>p = 14</code> (industry standard)?</summary>

**A:** ~12 KB (16384 buckets × 6 bits). Standard error ~0.81%.

</details>
<details>
<summary><strong>Q:</strong> Core trick?</summary>

**A:** Hash each item to a uniform 64-bit value. Track the *maximum number of leading zeros* per bucket. By chance, max leading zeros ≈ log₂(distinct items).

</details>
<details>
<summary><strong>Q:</strong> Why bucket?</summary>

**A:** A single counter has huge variance. `m` buckets reduce standard error to `1.04 / √m`.

</details>
<details>
<summary><strong>Q:</strong> What's the harmonic mean for and why?</summary>

**A:** `n ≈ α · m² / Σ 2^(-R[j])`. Harmonic mean of `2^R[j]` reduces variance vs arithmetic mean.

</details>
<details>
<summary><strong>Q:</strong> Are HLLs mergeable?</summary>

**A:** Yes! Take per-bucket max across two HLLs to get the union's HLL. Critical for distributed analytics.

</details>
<details>
<summary><strong>Q:</strong> Tradeoff space — error vs memory at p?</summary>

**A:** `p = 12`: 1.625% error, 3 KB. `p = 14`: 0.81%, 12 KB. `p = 16`: 0.41%, 48 KB. `p = 18`: 0.20%, 192 KB.

</details>
<details>
<summary><strong>Q:</strong> Where does HLL ship in production?</summary>

**A:** **Redis** (`PFADD`/`PFCOUNT`/`PFMERGE`), **BigQuery** (`APPROX_COUNT_DISTINCT`), **Apache Druid / Pinot / Spark / Flink**.

</details>

## Code template

```python
import hashlib, math

class HyperLogLog:
    def __init__(self, p=14):
        self.p = p
        self.m = 1 << p
        self.R = [0] * self.m
        self.alpha = 0.7213 / (1 + 1.079 / self.m) if self.m > 64 else 0.709

    def add(self, x):
        h = int(hashlib.sha256(str(x).encode()).hexdigest(), 16) & ((1 << 64) - 1)
        j = h >> (64 - self.p)
        w = (h << self.p) & ((1 << 64) - 1)
        rho = (64 - self.p + 1) if w == 0 else 64 - w.bit_length() + 1
        if rho > self.R[j]: self.R[j] = rho

    def estimate(self):
        Z = sum(2.0 ** (-r) for r in self.R)
        E = self.alpha * self.m * self.m / Z
        if E <= 2.5 * self.m:                                 # small-cardinality correction
            zeros = self.R.count(0)
            if zeros > 0: E = self.m * math.log(self.m / zeros)
        return E
```

## Pattern triggers

- **"How many distinct visitors today?"** → HLL
- **"Unique IPs hitting our CDN"** → HLL
- **"Count distinct across many shards"** → HLL per shard, merge at query time
- **"Rolling 24h windowed unique counts"** → HLL per minute, merge over the window
- **"Need exact distinct count under 100k"** → hash set is fine; HLL only wins at scale
- **"Bias on small cardinalities"** → linear-counting correction (count zero buckets)
- **"Approximate set membership"** → not HLL — use Bloom filter
- **"Approximate frequency"** → not HLL — use Count-Min Sketch

***

# Cross-links

- **Sibling structures:** [Bloom Filter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-bloom-filter), [Count-Min Sketch](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-count-min-sketch).
- **Bit trick reference:** [Pattern: Kth Bit](/cortex/data-structures-and-algorithms/bit-tricks-pattern-kth-bit) — leading-zero counting is a hardware-supported bit operation.

***

# Final takeaway

HyperLogLog estimates cardinality in 12 KB. Three patterns to internalise:

1. **Leading zeros encode rarity.** The number of leading zeros in a uniform random hash is geometrically distributed; the max over `n` items grows as `log₂ n`.
2. **Bucketing reduces variance.** A single counter has enormous variance; `m` buckets with harmonic mean reduces standard error to `1.04/√m`.
3. **Mergeable.** Two HLLs of disjoint sets merge into an HLL of their union by taking per-bucket max. This is what makes HLL the right primitive for distributed analytics.

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->
