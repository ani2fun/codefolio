# Phase 5 — Queue

> Read [`docs/dsa-migration-session-prompt.md`](../dsa-migration-session-prompt.md)
> first. This file is the phase plan, not the methodology.

**Source**: `/Users/aniket/Development/others/tutorial_dsa/extracted_data/DSA/data-structure/6.queue/`
**Destination**: `/Users/aniket/Development/homelab/codefolio/content/cortex/data-structures-and-algorithms/02-linear-structures/06-queue/`

## Stats

| | Count |
|---|---:|
| Chapters | 4 |
| Source lessons | 26 |
| Interactive diagrams | 10 |
| Destination size | 3,808 lines across 4 files |
| Indicative sessions | 4-5 |

Smallest phase under the "linear structures" parent. Should be doable
in a week of focused work.

## Widget readiness

Queue visualisations use **horizontal cells with `front` and `rear`
markers**. Circular-array queue is a clean fit for `array-traversal`
with wraparound indicated in the message field.

## Per-chapter breakdown

| # | Chapter | Lessons | IDs | Notes |
|---:|---|---:|---:|---|
| 5.1 | Introduction to Queues | 4 | 7 | FIFO concept; enqueue/dequeue. |
| 5.2 | Array Implementation of Queues | 10 | 8 | Circular array; biggest chapter. |
| 5.3 | Linked-List Implementation of Queues | 9 | 4 | Queue as singly linked list with head + tail. |
| 5.4 | Design | 3 | 0 | Deque, priority queue intro. |

## Recommended session breakdown

| Session | Scope |
|---|---|
| 1 | Ch 5.1 Introduction (full) |
| 2 | Ch 5.2 Array Implementation — first half |
| 3 | Ch 5.2 Array Implementation — second half + Ch 5.3 LL impl concept |
| 4 | Ch 5.3 LL Implementation problems |
| 5 | Ch 5.4 Design + phase verification |

## Session task templates

### Session 1

```
Phase 5 — Queue, Session 1.

Ch 5.1 Introduction to Queues. Align enqueue, dequeue, peek,
isEmpty, isFull code (5 langs). Source uses `front` and `rear`
indices on a fixed-size array; destination must too. Convert
operation interactive diagrams to `array-traversal` with `front`
and `rear` markers.

One commit per operation.
```

### Sessions 2-3 (Array Implementation)

```
Phase 5 — Queue, Session <N>.

Ch 5.2 Array Implementation of Queues. Source uses CIRCULAR array
with `(rear + 1) % capacity` modular arithmetic. Destination must
match the modular indexing exactly. Pay attention to the empty-vs-
full disambiguation: source uses a separate `size` counter rather
than the `front == rear` ambiguous condition.

Convert the wraparound interactive diagram to a 2-step widget
showing the modular jump.
```

### Session 4 (LL Implementation)

```
Phase 5 — Queue, Session 4.

Ch 5.3 Linked-List Implementation of Queues. Queue as singly linked
list with `head` (front, dequeue side) and `tail` (rear, enqueue
side). Source's enqueue is O(1) using the `tail` reference; without
it would be O(n).

Defer linked-list widget conversion.
```

### Session 5 (closeout)

```
Phase 5 — Queue, Session 5 (closeout).

Ch 5.4 Design (Deque, priority queue intro). Then phase verification
— scalafmt, tests, build, browser smoke across all four chapters.
End with status note for Phase 6.
```

## Gotchas

- **Circular indexing edge cases** — when `rear` wraps past
  `front`, the queue is full but `rear == front` numerically. Source
  uses a separate `size` counter to disambiguate from empty.
- **Capacity vs size** — `capacity` is fixed at construction; `size`
  is the current item count. Don't confuse them.
- **Front vs head terminology** — array implementation uses `front`,
  LL uses `head`. Match source per chapter.
