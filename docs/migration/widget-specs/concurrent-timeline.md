# Widget Spec — `concurrent-timeline`

> Read [`../methodology.md`](../methodology.md) first.

## 1. Purpose

Visualise concurrent thread interactions on a shared memory location.
The widget pairs:

1. **One or more memory cells** at the top of the canvas, each labelled
   with its current value. Updates propagate visibly when a step
   declares a write.
2. **Two or more thread swim-lanes** running horizontally below the
   memory row. Each lane is a left-to-right timeline; each step in the
   payload becomes a labelled event placed at the step's `t` (relative
   tick). Events have icons by kind:
   - `read`: small eye glyph
   - `cas-success`: green checkmark
   - `cas-fail`: red X (retry edge curls back to the next read on the
     same lane)
   - `write`: arrow into a memory cell
   - `compute`: dotted "local" event, no memory interaction
3. **A timeline ruler** at the bottom shows ticks; the cursor scrubs
   across it under the `Stepper` controls.

The widget unifies three concurrency chapter visualisations:

- **CAS retry loop.** Two threads contend on the same cell; only one CAS
  per tick wins, the loser retries. The widget renders the retry edge
  as a curved arrow back to the loser's next read.
- **ABA problem.** Three-step sequence: Thread A reads X; Thread B
  changes X→Y→X; Thread A's CAS succeeds because the value matches —
  but the underlying state changed.
- **RCU / hazard-pointer reclamation.** A "reader" lane holds a pointer
  during a hazard window; a "writer" lane attempts to free; the widget
  shows the deferred-free queue and the moment the grace period closes.

## 2. Source-diagram inventory

**0 — orphan widget; payloads derived from destination chapter prose.**

No source coverage. Payload milestones come from:

- `10-concurrency-and-systems/01-cas-and-atomics.md`: the increment-loop
  pseudo-code in § The Hook, the ABA-problem step list in § The ABA
  Problem (numbered 1–3, ideal for direct payload conversion).
- `10-concurrency-and-systems/04-rcu-and-hazard-pointers.md`: the
  hazard-pointer publish/unpublish pseudo-code in § Hazard pointers, the
  RCU writer-grace-period flowchart in § RCU.

## 3. Destination chapter usage

- **Primary owner.** `10-concurrency-and-systems/01-cas-and-atomics.md`
  — two instances:
  - § What CAS does: simple two-thread increment race demonstrating
    one CAS-success and one CAS-fail-then-retry.
  - § The ABA Problem: three-step ABA sequence with explicit
    pointer-state annotations.
- **Reuse in `10-concurrency-and-systems/04-rcu-and-hazard-pointers.md`** —
  two instances:
  - § Hazard pointers: reader publishes hazard; writer scans table,
    defers free.
  - § RCU: writer copies + swaps pointer, waits for grace period, frees
    old.
- **Possible reuse in `10-concurrency-and-systems/02-lock-free-queue.md`** —
  for the producer/consumer enqueue race. Single instance.
- **Possible reuse in `10-concurrency-and-systems/03-concurrent-hash-map.md`** —
  for the striped-lock contention picture.

## 4. Payload schema sketch

```typescript
{
  title?: string,
  // The shared memory cells visualised at the top. Most demos use 1;
  // ABA might use 1; RCU uses 2 (the pointer + the pointee node).
  cells: Array<{
    id:    string,
    label: string,           // displayed name, e.g. "counter", "head", "node*"
    initialValue: string     // displayed verbatim
  }>,
  // Each thread is a horizontal swim-lane. 2–4 threads is the practical
  // limit before the canvas crowds.
  threads: Array<{
    id:    string,
    label: string,           // displayed name, e.g. "T1", "Thread A", "Reader"
    color: string            // canon-validated; see § 8
  }>,
  // Discrete time ticks; render as a left-to-right ruler at the bottom.
  // Events position by their `t` field.
  ticks: number,             // total tick count; 8–12 typical
  steps: Array<{
    t:        number,        // tick index; 0 <= t < ticks
    thread:   string,        // thread id
    op:       "read" | "cas-success" | "cas-fail" | "write" | "compute",
    cell?:    string,        // cell id for read / cas-* / write ops
    msg:      string,        // caption shown when the stepper lands on this event
    // For read ops: the value the thread observes (renders inside the
    // read glyph, e.g. "T1 reads 0").
    observedValue?: string,
    // For write / cas-success ops: the new value to store in the cell.
    newValue?: string,
    // For cas-* ops: the expected value the thread is comparing against.
    // The widget renders this as "expected X, got Y" in the caption.
    expectedValue?: string,
    // For cas-fail ops: optional "retry to" tick index. The widget
    // draws a curved arrow back to that tick on the same lane.
    retryTo?: number,
    // For hazard-pointer / RCU demos: annotate the event with a tag
    // visible above the swim-lane.
    annotation?: string      // e.g. "hp publish", "hp release", "grace period start"
  }>,
  // Optional ABA / hazard panels — small inset boxes that surface
  // structural state that's not just a value-in-a-cell:
  panels?: Array<{
    kind:  "aba-pointer" | "hazard-table" | "rcu-grace",
    msg:   string,
    // For aba-pointer: which cell the panel watches.
    cellId?: string
  }>,
  sections?: Array<{ name: string, startIdx: number }>
}
```

Validation:

- Every `step.thread` must be a valid thread id.
- Every `step.cell` (when present) must be a valid cell id.
- `read`, `cas-success`, `cas-fail`, `write` must have a `cell`.
- `cas-*` must have both `expectedValue` and (for `cas-success`)
  `newValue`.
- `cas-fail.retryTo` (when supplied) must reference a later tick on the
  same thread.
- `t < ticks` for every step.

## 5. POC payloads

### 5a. Two-thread increment race — one CAS wins, one retries

```d3 widget=concurrent-timeline
{
  "title": "CAS retry loop — two threads racing to increment counter",
  "cells": [
    { "id": "counter", "label": "counter", "initialValue": "0" }
  ],
  "threads": [
    { "id": "T1", "label": "Thread 1", "color": "#3b82f6" },
    { "id": "T2", "label": "Thread 2", "color": "#ef4444" }
  ],
  "ticks": 8,
  "steps": [
    { "t": 0, "thread": "T1", "op": "read",         "cell": "counter", "observedValue": "0", "msg": "T1 reads counter = 0." },
    { "t": 0, "thread": "T2", "op": "read",         "cell": "counter", "observedValue": "0", "msg": "T2 reads counter = 0." },
    { "t": 1, "thread": "T1", "op": "compute",                                                "msg": "T1 computes new = 0 + 1 = 1." },
    { "t": 1, "thread": "T2", "op": "compute",                                                "msg": "T2 computes new = 0 + 1 = 1." },
    { "t": 2, "thread": "T1", "op": "cas-success",  "cell": "counter", "expectedValue": "0", "newValue": "1", "msg": "T1 CAS(counter, 0, 1) — succeeds. counter = 1." },
    { "t": 2, "thread": "T2", "op": "cas-fail",     "cell": "counter", "expectedValue": "0", "retryTo": 4, "msg": "T2 CAS(counter, 0, 1) — fails (counter is now 1, not 0). Retry." },
    { "t": 4, "thread": "T2", "op": "read",         "cell": "counter", "observedValue": "1", "msg": "T2 retries — reads counter = 1." },
    { "t": 5, "thread": "T2", "op": "compute",                                                "msg": "T2 computes new = 1 + 1 = 2." },
    { "t": 6, "thread": "T2", "op": "cas-success",  "cell": "counter", "expectedValue": "1", "newValue": "2", "msg": "T2 CAS(counter, 1, 2) — succeeds. counter = 2." }
  ]
}
```

### 5b. ABA problem — Thread A's CAS succeeds despite intermediate change

```d3 widget=concurrent-timeline
{
  "title": "ABA — Thread A reads X; B does X→Y→X; A's CAS succeeds (wrongly safe)",
  "cells": [
    { "id": "ptr", "label": "shared ptr", "initialValue": "X" }
  ],
  "threads": [
    { "id": "TA", "label": "Thread A", "color": "#3b82f6" },
    { "id": "TB", "label": "Thread B", "color": "#ef4444" }
  ],
  "ticks": 10,
  "steps": [
    { "t": 0, "thread": "TA", "op": "read",        "cell": "ptr", "observedValue": "X", "msg": "A reads ptr = X. Plans to CAS(ptr, X, new)." },
    { "t": 1, "thread": "TA", "op": "compute",                                          "msg": "A pauses (context switch, slow path, …)." },
    { "t": 2, "thread": "TB", "op": "cas-success", "cell": "ptr", "expectedValue": "X", "newValue": "Y", "msg": "B CAS(ptr, X, Y) — succeeds. ptr = Y." },
    { "t": 3, "thread": "TB", "op": "compute",                                          "msg": "B does its work with Y." },
    { "t": 4, "thread": "TB", "op": "cas-success", "cell": "ptr", "expectedValue": "Y", "newValue": "X", "msg": "B CAS(ptr, Y, X) — restores ptr to X (perhaps via reuse / pool / coincidence)." },
    { "t": 5, "thread": "TA", "op": "compute",                                          "msg": "A resumes." },
    { "t": 6, "thread": "TA", "op": "cas-success", "cell": "ptr", "expectedValue": "X", "newValue": "Z", "msg": "A CAS(ptr, X, Z) — succeeds. But state changed between reads — A's logic may now be broken." }
  ],
  "panels": [
    { "kind": "aba-pointer", "cellId": "ptr", "msg": "ptr value sequence: X → Y → X → Z. A only sees first X and last X." }
  ]
}
```

### 5c. Hazard pointer publish / scan / defer

```d3 widget=concurrent-timeline
{
  "title": "Hazard pointer — Reader publishes hazard; Writer scans, defers free",
  "cells": [
    { "id": "head", "label": "head", "initialValue": "→N1" },
    { "id": "n1",   "label": "N1",   "initialValue": "live" }
  ],
  "threads": [
    { "id": "R", "label": "Reader", "color": "#10b981" },
    { "id": "W", "label": "Writer", "color": "#f59e0b" }
  ],
  "ticks": 10,
  "steps": [
    { "t": 0, "thread": "R", "op": "write",       "cell": "head",                       "newValue": "→N1",        "annotation": "hp publish",   "msg": "Reader publishes hazard pointer to N1." },
    { "t": 1, "thread": "R", "op": "read",        "cell": "head", "observedValue": "→N1",                                                      "msg": "Reader follows head pointer to N1." },
    { "t": 2, "thread": "W", "op": "cas-success", "cell": "head", "expectedValue": "→N1", "newValue": "→N2",                                    "msg": "Writer unlinks N1, swaps head to N2." },
    { "t": 3, "thread": "W", "op": "compute",                                                                      "annotation": "hp scan",      "msg": "Writer scans hazard table — sees Reader holds N1. Defers free." },
    { "t": 4, "thread": "R", "op": "read",        "cell": "n1",   "observedValue": "live",                                                      "msg": "Reader still reads N1's data safely — hazard pointer kept it alive." },
    { "t": 5, "thread": "R", "op": "write",       "cell": "head",                                                  "annotation": "hp release",  "msg": "Reader releases hazard pointer." },
    { "t": 6, "thread": "W", "op": "compute",                                                                                                    "msg": "Writer's next scan finds no holders — frees N1." },
    { "t": 7, "thread": "W", "op": "write",       "cell": "n1",                          "newValue": "freed",                                  "msg": "N1 is freed." }
  ],
  "panels": [
    { "kind": "hazard-table", "msg": "Hazard table: Reader → N1 (steps 0–4); empty (step 5+)." }
  ]
}
```

### 5d. RCU writer + grace-period reclamation

```d3 widget=concurrent-timeline
{
  "title": "RCU — Writer copies + swaps pointer; grace period closes; old version freed",
  "cells": [
    { "id": "ptr",  "label": "ptr",  "initialValue": "→V1" },
    { "id": "v1",   "label": "V1",   "initialValue": "live" }
  ],
  "threads": [
    { "id": "R1", "label": "Reader 1", "color": "#10b981" },
    { "id": "R2", "label": "Reader 2", "color": "#06b6d4" },
    { "id": "W",  "label": "Writer",   "color": "#f59e0b" }
  ],
  "ticks": 12,
  "steps": [
    { "t": 0, "thread": "R1", "op": "read",        "cell": "ptr", "observedValue": "→V1", "msg": "R1 reads ptr — holds V1." },
    { "t": 1, "thread": "R2", "op": "read",        "cell": "ptr", "observedValue": "→V1", "msg": "R2 reads ptr — also holds V1." },
    { "t": 2, "thread": "W",  "op": "compute",                                              "msg": "Writer prepares V2 (modified copy)." },
    { "t": 3, "thread": "W",  "op": "cas-success", "cell": "ptr", "expectedValue": "→V1", "newValue": "→V2", "msg": "Writer atomically swaps ptr → V2." },
    { "t": 4, "thread": "W",  "op": "compute",                                              "annotation": "grace period start", "msg": "Writer waits for grace period. V1 must not be freed yet." },
    { "t": 5, "thread": "R1", "op": "compute",                                              "annotation": "R1 quiescent", "msg": "R1 finishes its critical section — passes a quiescent point." },
    { "t": 7, "thread": "R2", "op": "compute",                                              "annotation": "R2 quiescent", "msg": "R2 also passes a quiescent point." },
    { "t": 9, "thread": "W",  "op": "compute",                                              "annotation": "grace period end",   "msg": "All readers have passed. Grace period closes." },
    { "t": 10, "thread": "W", "op": "write",       "cell": "v1",                            "newValue": "freed",                "msg": "V1 freed safely." }
  ],
  "panels": [
    { "kind": "rcu-grace", "msg": "Grace period spans ticks 4–9. Free deferred until all readers passed a quiescent point." }
  ]
}
```

### 5e. Lock-free stack push — concurrent pushers serialise on the head CAS

```d3 widget=concurrent-timeline
{
  "title": "Lock-free push — two pushers race for the head pointer",
  "cells": [
    { "id": "top", "label": "stack.top", "initialValue": "→A" }
  ],
  "threads": [
    { "id": "P1", "label": "Pusher 1", "color": "#3b82f6" },
    { "id": "P2", "label": "Pusher 2", "color": "#ef4444" }
  ],
  "ticks": 8,
  "steps": [
    { "t": 0, "thread": "P1", "op": "read",        "cell": "top", "observedValue": "→A", "msg": "P1 reads top = A. Plans to push B; B.next = A." },
    { "t": 0, "thread": "P2", "op": "read",        "cell": "top", "observedValue": "→A", "msg": "P2 reads top = A. Plans to push C; C.next = A." },
    { "t": 2, "thread": "P1", "op": "cas-success", "cell": "top", "expectedValue": "→A", "newValue": "→B", "msg": "P1 CAS(top, A, B) — succeeds." },
    { "t": 2, "thread": "P2", "op": "cas-fail",    "cell": "top", "expectedValue": "→A", "retryTo": 4, "msg": "P2 CAS(top, A, B) — fails (top is now B). Retry." },
    { "t": 4, "thread": "P2", "op": "read",        "cell": "top", "observedValue": "→B", "msg": "P2 re-reads top = B. C.next = B now." },
    { "t": 5, "thread": "P2", "op": "cas-success", "cell": "top", "expectedValue": "→B", "newValue": "→C", "msg": "P2 CAS(top, B, C) — succeeds." }
  ]
}
```

## 6. Closest existing widget to mimic

- **`RaftAnimator.scala`** is the closest existing widget conceptually:
  it pre-bakes a multi-actor scripted scenario and steps through
  snapshots. Reuse: the message-in-flight arrow pattern (for read /
  write events drawn between thread lanes and the memory cell row),
  the per-actor coloured node decoration, and the caption-driven
  step model.
- **`linked-list`**'s sectioned `Stepper` (multi-phase progress bar)
  fits the multi-phase nature of CAS retry + ABA + RCU.
- **`array-traversal`** is not a direct fit — there are no cells in a
  row to scrub through; the visual is a 2D matrix of (thread × tick).
- **`CacheStampedeSimulator`** is not a fit — it's a slider over a
  static formula, not a scripted scenario.

## 7. D3 selections plan

- Host `<svg>` with four logical regions stacked vertically:
  - **Memory cells row** (top, `~60px` high). One labelled box per
    cell, value rendered inside. On write, the new value pops in with
    a 350 ms tween; cell flash highlights the change.
  - **Swim-lane grid** (centre, `threadCount * 56px` high). One
    horizontal lane per thread. Lane label on the left; ticks across
    the top. Events placed at `(t, thread)` coordinates with kind-icon
    glyphs.
  - **Panels strip** (right side, vertical stack of ~150×80 boxes).
    Rendered only when payload supplies `panels`. Each panel has its
    own renderer:
    - `aba-pointer`: value-sequence list with arrows.
    - `hazard-table`: per-thread "currently holding" rows.
    - `rcu-grace`: a horizontal bar spanning the grace-period ticks.
  - **Caption lane** (bottom). Standard step msg lane.
- **Event selection** keyed by `${stepIdx}` so retries / re-reads don't
  collapse. Each event renders as a small group: icon + tiny value
  label.
- **CAS-fail retry edge** drawn as a quadratic-bezier curve from the
  failing event back to `retryTo` on the same lane.
- **Read / write arrows to memory cells** drawn as dashed lines from
  the event glyph up to the cell, styled by op kind.
- **Tick ruler** at the bottom; cursor (a thin vertical line spanning
  all lanes) tracks `Stepper`'s current step. Active step's event
  pulses (radius tween).
- Transitions: 300 ms for event entry; 200 ms for cell-value tween;
  150 ms cursor sweep.

## 8. Shared abstractions

- **`Stepper.scala`** for prev/next/play/reset + section dividers.
- **`PayloadDecoder.scala`** for parsing.
- **Canon thread colours** (parse-time validated; up to 4 threads
  supported with distinct colours):
  - thread 1 → `#3b82f6` (blue)
  - thread 2 → `#ef4444` (rose)
  - thread 3 → `#10b981` (emerald) — used for Reader role in RCU/HP
  - thread 4 → `#f59e0b` (amber) — used for Writer role in RCU/HP
  - additional → fall back to slate `#64748b` with a warning
- **Canon op-icon glyphs** — single source of truth for the
  read/cas/write/compute icons:
  - read → eye (Lucide)
  - cas-success → check (Lucide)
  - cas-fail → x (Lucide)
  - write → arrow-up (Lucide, draws toward memory row)
  - compute → ellipsis (Lucide)
- **`LucideIcons.scala`** for the glyphs above.
- **`Stepper` section markers** to label macro-phases (e.g., "Reader
  read", "Writer swap", "Grace period", "Free" in RCU demos).

## 9. Estimated build session count

**2 sessions.**

- Session 1: schema + parsing + render of memory row + swim lanes +
  events covering payloads 5a + 5b. Lift `RaftAnimator`'s
  scripted-step pattern + `linked-list`'s `Stepper` machinery.
- Session 2: panels (aba-pointer, hazard-table, rcu-grace), retry-edge
  curve, payloads 5c + 5d + 5e. Demo book chapter, dispatcher wiring,
  scalafmt.

Risk: the panels are heterogeneous; each kind needs its own renderer.
If session 2 runs over, defer `rcu-grace` panel polish to a third
session.

## 10. POC chapter

`content/cortex/dsa-widget-catalog/concurrent-timeline.md` — exhibits
all 5 POC payloads (CAS retry race, ABA, hazard pointer, RCU,
lock-free stack push). Each payload has a 1-sentence caption naming
its destination home.
