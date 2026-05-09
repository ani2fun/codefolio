# Foundations

Before any data structure or algorithm makes sense, two things have to be in place: **the language for measuring how long code runs** and **the mental picture of where data lives in memory**. This module covers both. Every later chapter in this book leans on the vocabulary built here — so if you skip it, you'll find yourself bluffing through complexity claims for the rest of your career.

## Place in the curriculum

- **Prerequisites:** none. Basic programming literacy is enough.
- **Followed by:** every other module. The Asymptotic Analysis chapter especially is cited by hundreds of complexity claims in later chapters.

## Chapters

1. [Asymptotic Analysis](/cortex/data-structures-and-algorithms/foundations-asymptotic-analysis) — O, Θ, Ω, little-o, little-omega; how to derive them from first principles, not memorise.
2. [Recurrence Relations and the Master Theorem](/cortex/data-structures-and-algorithms/foundations-recurrence-relations-and-master-theorem) — solve `T(n) = aT(n/b) + f(n)` without panic.
3. [Amortized Analysis](/cortex/data-structures-and-algorithms/foundations-amortized-analysis) — aggregate, accounting, and potential methods. Why dynamic-array push is `O(1)` amortized even though one push out of every `n` is `O(n)`.
4. [Proof Techniques](/cortex/data-structures-and-algorithms/foundations-proof-techniques) — induction, contradiction, and the loop-invariant pattern that turns "this code looks right" into "this code is right".
5. [Memory Model and the Cache Hierarchy](/cortex/data-structures-and-algorithms/foundations-memory-model-and-cache) — the picture every later chapter assumes you have: registers → L1 → L2 → L3 → RAM → disk, and what each step costs.
