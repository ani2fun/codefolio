# Part 1 — Foundations

> **The mental model.** Five lessons that the rest of this book leans on. Skip them and the later parts will not stick.

By the time you finish this part you will:

- Know what "system design" actually is and how it differs from coding.
- Have a calibrated intuition for **how slow each computer operation actually is** — turning "memory is fast, network is slow" into specific numbers you can quote on demand.
- Be able to estimate the QPS, storage, and bandwidth of a system like Twitter or YouTube from first principles in under 5 minutes.
- Understand the **CAP theorem** the way the author of CAP intended (and the modern PACELC reformulation that fixes its biggest blind spot).
- Know **Little's Law** and the **Universal Scalability Law** — the two equations that explain why doubling servers does not double throughput.

## Lessons

1. [What "system design" actually means](/cortex/system-design/foundations-what-system-design-means) — the difference between coding and engineering systems; the job description of a senior engineer.
2. [Numbers every engineer should know](/cortex/system-design/foundations-numbers-every-engineer-should-know) — the latency hierarchy, time-scaled into seconds-and-years so it sticks.
3. [Back-of-envelope estimation](/cortex/system-design/foundations-back-of-envelope-estimation) — drilled until reflexive on Twitter, YouTube, and WhatsApp scale.
4. [The CAP theorem and PACELC, honestly](/cortex/system-design/foundations-cap-and-pacelc) — with a Python simulator that lets you *feel* a partition.
5. [Latency, throughput, and the Universal Scalability Law](/cortex/system-design/foundations-latency-throughput-usl) — Little's Law and the queueing simulation that makes it concrete.

## Runnable examples

- [`examples/04-cap-pacelc-simulator/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/01-foundations/examples/04-cap-pacelc-simulator) — three-node KV store with injectable partitions.
- [`examples/05-littles-law-queueing/`](https://github.com/ani2fun/codefolio/tree/main/content/cortex/system-design/01-foundations/examples/05-littles-law-queueing) — an M/M/1 queue simulator that lets you watch latency blow up as utilisation crosses 0.7.

## Prerequisites

- You can write a `for` loop, an `if/else`, and a function in any language.
- You have heard the words "client" and "server" before, even if you cannot fully define them.

That is it. Everything else is built up from scratch.
