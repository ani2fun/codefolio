package codefolio.client.components.cortex.widgets

import org.scalajs.dom

/**
 * Shared canonical-marker vocabulary across DSA D3 widgets. Built originally inside `LinkedList` per ADR-0016
 * and lifted here so every Arc 1 widget shares one source of truth — a pointer named `head`, `current`,
 * `slow`, `fast`, `top`, `front`, `back`, … is the same colour wherever it appears in the book. Authors
 * cannot override colours from the payload; per-marker `color` fields are silently dropped at parse time.
 *
 * Three concerns:
 *   1. The colour table (name → hex). 2. `colorFor(name)` lookup with [[WarningColor]] fallback for unknown
 *      names. 3. `warnUnknown` / `warnAuthorColor` — one-line console warnings shared by every widget's
 *      parser.
 *
 * Growing the canon: add the entry, amend ADR-0016 + ADR-0017 (shared canon), and every other code path reads
 * through this map.
 */
object MarkerCanon:

  /**
   * Warning colour rendered when a marker name is not in the canon. Same rose as LinkedList used pre-lift,
   * preserved verbatim so existing linked-list payloads render identically.
   */
  val WarningColor: String = "#ef4444"

  /**
   * Closed marker vocabulary across all DSA widgets. Entries below carry their widget-of-origin in the inline
   * comment so future readers can trace why a name lives in the canon at all.
   */
  val palette: Map[String, String] = Map(
    // ── Linked-list canon (ADR-0016, preserved verbatim from LinkedList.CanonicalMarkers) ──
    "head"     -> "#3b82f6", // blue    — list entry
    "tail"     -> "#64748b", // slate   — explicit last-node tracker
    "previous" -> "#f59e0b", // amber   — trailing pointer (reversal)
    "current"  -> "#10b981", // emerald — active pointer
    "next"     -> "#a855f7", // violet  — saved-next reference
    "slow"     -> "#3b82f6", // blue    — slow (Floyd, two-pointer)
    "fast"     -> "#ef4444", // rose    — fast (Floyd, two-pointer)
    "dummy"    -> "#64748b", // slate   — sentinel / dummy head
    "start"    -> "#f59e0b", // amber   — segment start
    "end"      -> "#10b981", // emerald — segment end
    // Multi-list operations (merge, split, round-robin, alternate-merge) — letter suffix
    // distinguishes lists; colours stay distinct so two or three simultaneously-tracked
    // heads don't blur.
    "headA" -> "#3b82f6", // blue         — list A entry
    "headB" -> "#06b6d4", // cyan         — list B entry
    "headC" -> "#a855f7", // violet       — list C entry
    "tailA" -> "#64748b", // slate        — list A end
    "tailB" -> "#475569", // slate-dark   — list B end
    "tailC" -> "#334155", // slate-darker — list C end

    // ── Stack-queue (Arc 1 — `top` of a LIFO, `front`/`back` of a FIFO) ──
    // `top` shares emerald with `current` because both name "the cursor of activity".
    // `front`/`back` mirror the linked-list two-pointer convention (blue/rose) so
    // FIFO traversal reads like Floyd / sliding-window in the rest of the book.
    "top"   -> "#10b981", // emerald — top of stack
    "front" -> "#3b82f6", // blue    — front of queue (dequeue side)
    "back"  -> "#ef4444", // rose    — back of queue (enqueue side)

    // ── Array-traversal (Arc 1) ──
    // `i`/`j` = primary / secondary loop indices.
    // `left`/`right` = two-pointer bounds — same paint as `slow`/`fast` so a two-pointer
    //   sweep reads like Floyd's algorithm in the rest of the book.
    // `mid` = binary-search midpoint — emerald, same family as `current`/`top` ("the
    //   cursor of activity").
    // `low`/`high` = binary-search bounds — kept as separate canonical names because
    //   algorithm code in the source uses `low`/`high` for search intervals; paint is
    //   aliased to `left`/`right` so the reader's "lo end is blue, hi end is rose"
    //   mental model is preserved across two-pointer and binary-search arcs.
    // `read`/`write` = in-place compaction cursors (slow/current family).
    // `found`/`ptr`/`addr` = single-cursor terminal / general / address-mapping
    //   pointers — all emerald (the `current` family).
    // `freq` = secondary-row frequency-table accumulator — violet (same family as
    //   `next`, the saved-aside value being accumulated).
    "i"     -> "#3b82f6", // blue    — primary loop index
    "j"     -> "#f59e0b", // amber   — secondary / inner loop index
    "left"  -> "#3b82f6", // blue    — two-pointer lo (alias of `slow`)
    "right" -> "#ef4444", // rose    — two-pointer hi (alias of `fast`)
    "mid"   -> "#10b981", // emerald — binary-search midpoint
    "low"   -> "#3b82f6", // blue    — binary-search lower bound (alias of `left`)
    "high"  -> "#ef4444", // rose    — binary-search upper bound (alias of `right`)
    "read"  -> "#3b82f6", // blue    — in-place compaction read cursor
    "write" -> "#10b981", // emerald — in-place compaction write cursor
    "found" -> "#10b981", // emerald — search success marker
    "ptr"   -> "#10b981", // emerald — single-pointer cursor (staircase, walks)
    "addr"  -> "#10b981", // emerald — flat-with-2d-overlay address pointer
    "freq"  -> "#a855f7", // violet  — secondary-row frequency-table accumulator

    // ── Binary-tree (Arc 1) ──
    // `parent` = trailing pointer for iterative-search / iterative-insert / iterative-delete
    //   (slate, aliased to `dummy` so "the still-known previous node" reads alike across
    //   linked-list reversal and tree insert/delete).
    // `successor`/`predecessor` = in-order successor / predecessor cursor used by BST
    //   delete (two-children case) and iterator chapters — violet (the `next` family,
    //   "the saved-aside value being walked toward").
    // `p`/`q` = the two query-node markers for LCA chapters — amber/cyan, distinct from
    //   the algorithm's working `current` so the reader keeps the two "fixed queries"
    //   visually separate from the cursor walking the tree.
    // `lca` = the resolved lowest common ancestor — emerald (the `found` family,
    //   "the answer landed here").
    "parent"      -> "#64748b", // slate   — tree trailing pointer (alias of `dummy`)
    "successor"   -> "#a855f7", // violet  — in-order successor cursor (alias of `next`)
    "predecessor" -> "#a855f7", // violet  — in-order predecessor cursor (alias of `next`)
    "p"           -> "#f59e0b", // amber   — LCA query node 1
    "q"           -> "#06b6d4", // cyan    — LCA query node 2
    "lca"         -> "#10b981", // emerald — LCA result (alias of `found`)

    // ── Heap-tree (Arc 1) ──
    // `root` = the index-0 root marker — blue, the `head`/`i` family ("the entry
    //   point of the structure"). Distinct paint from `top` (emerald) so a step
    //   that names BOTH the root index AND the to-be-extracted top reads as two
    //   separate annotations rather than one.
    // `last` = the trailing-end cell (insert target on push; swap partner on
    //   extract-top). Slate, aliased to `tail`/`parent` so "the explicit end
    //   tracker" reads alike across linked list, BST, and heap.
    // `swap` = the cell currently being swapped against the cursor (sift-up
    //   partner = parent; sift-down partner = larger child). Rose, aliased to
    //   `fast`/`right` so the reader's "rose = the contentious second slot"
    //   mental model is preserved across two-pointer and heap-sift arcs.
    // `kth` = the k-th element marker for top-k streaming patterns (e.g. the
    //   k-th largest being maintained at the min-heap root). Violet (the `next`
    //   family — "the accumulated / saved-aside value").
    "root" -> "#3b82f6", // blue    — heap root (index 0; alias of `head`)
    "last" -> "#64748b", // slate   — last-cell tracker (alias of `tail`)
    "swap" -> "#ef4444", // rose    — sift swap partner (alias of `fast`)
    "kth"  -> "#a855f7", // violet  — k-th element in top-k streams (alias of `next`)

    // ── Call-stack (Arc 1) ──
    // `caller` = the frame ONE BELOW the top — the frame that's waiting on the
    //   current call's return. Slate, aliased to `parent`/`last`/`dummy` so
    //   "the waiting / previous slot" reads alike across linked list, BST,
    //   heap, and call stack.
    // `callee` = the frame just pushed (top of stack), distinct from the
    //   computed-active highlight so an author can name a specific frame in
    //   narration. Emerald, aliased to `current`/`top` (the cursor of activity).
    // `base` = the bottom-of-stack frame (typically `main`). Blue, aliased to
    //   `head`/`root`/`i` — "the entry point of the structure". Useful for
    //   stack-overflow visualisations where the chapter wants to keep
    //   `main` named even as the stack grows past the visible limit.
    "caller" -> "#64748b", // slate   — frame below the top (alias of `parent` / `last`)
    "callee" -> "#10b981", // emerald — frame just pushed (alias of `current` / `top`)
    "base"   -> "#3b82f6"  // blue    — bottom-of-stack frame (alias of `head` / `root`)
  )

  /** Lookup with warning-colour fallback. Every widget's `colorFor` delegates here. */
  def colorFor(name: String): String =
    palette.getOrElse(name, WarningColor)

  /** Whether a name is in the canon — used by widgets that render a warning badge for unknown names. */
  def isCanonical(name: String): Boolean =
    palette.contains(name)

  /**
   * Sorted comma-separated list of canonical names. Used in console-warning messages so the author sees the
   * full vocabulary at the point of error.
   */
  def canonicalNames: String =
    palette.keys.toList.sorted.mkString(", ")

  /** Console warning for an unknown marker name (typo, or a name not yet admitted to the canon). */
  def warnUnknown(widgetName: String, name: String): Unit =
    dom.console.warn(
      s"$widgetName: marker name '$name' is not in the canonical vocabulary. Rendered as an inline warning. Canonical names: $canonicalNames."
    )

  /** Console warning for an author-supplied `color` field that gets silently dropped at parse time. */
  def warnAuthorColor(widgetName: String, name: String): Unit =
    dom.console.warn(
      s"$widgetName: marker '$name' carries a `color` field — dropping (colour is resolved from the canon, not the payload)."
    )
