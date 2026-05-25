package codefolio.shared.viz

/**
 * Role-based colour canon for cursor pointers (ADR-0016 / ADR-0018).
 *
 * A pointer's colour is decided by its *role*, not the author: `head` / `root` / `i` are blue (an entry
 * point), `current` / `mid` emerald (the active cursor), `fast` / `right` rose (the contentious second),
 * `previous` / `j` amber, `next` / `successor` violet, `tail` / `parent` slate — so a reader who has seen one
 * chapter recognises a pointer's colour everywhere.
 *
 * The bespoke widgets (`MarkerCanon`) demand canonical names. The trace-driven Visualise reads *real* Python
 * variable names, which it cannot rename — so this canon adds an `aliases` layer mapping common real-code
 * names (`cur`, `nxt`, `prev`, `lo`, …) onto canonical roles, and a `fallback` palette of distinct hues for
 * names with no role. Same colours as `MarkerCanon`, looser matching: the trace aliases liberally where the
 * widgets hard-reject.
 *
 * Pure data — cross-compiles to the JVM (HeapToGraph tests) and Scala.js (the adapter at runtime).
 */
object MarkerColors:

  /** Canonical pointer name → role colour. Ported from `MarkerCanon.palette` (ADR-0016). */
  val canon: Map[String, String] = Map(
    "head"        -> "#3b82f6",
    "root"        -> "#3b82f6",
    "i"           -> "#3b82f6",
    "left"        -> "#3b82f6",
    "low"         -> "#3b82f6",
    "slow"        -> "#3b82f6",
    "front"       -> "#3b82f6",
    "read"        -> "#3b82f6",
    "base"        -> "#3b82f6",
    "current"     -> "#10b981",
    "mid"         -> "#10b981",
    "top"         -> "#10b981",
    "write"       -> "#10b981",
    "found"       -> "#10b981",
    "ptr"         -> "#10b981",
    "end"         -> "#10b981",
    "j"           -> "#f59e0b",
    "previous"    -> "#f59e0b",
    "p"           -> "#f59e0b",
    "start"       -> "#f59e0b",
    "next"        -> "#a855f7",
    "successor"   -> "#a855f7",
    "predecessor" -> "#a855f7",
    "kth"         -> "#a855f7",
    "tail"        -> "#64748b",
    "dummy"       -> "#64748b",
    "parent"      -> "#64748b",
    "last"        -> "#64748b",
    "fast"        -> "#ef4444",
    "right"       -> "#ef4444",
    "high"        -> "#ef4444",
    "swap"        -> "#ef4444",
    "back"        -> "#ef4444",
    "q"           -> "#06b6d4"
  )

  /** Real-code variable name → a canonical name. The trace aliases liberally; the bespoke widgets reject. */
  val aliases: Map[String, String] = Map(
    "cur"    -> "current",
    "curr"   -> "current",
    "node"   -> "current",
    "cnode"  -> "current",
    "runner" -> "current",
    "walk"   -> "current",
    "prev"   -> "previous",
    "pre"    -> "previous",
    "nxt"    -> "next",
    "tmp"    -> "next",
    "temp"   -> "next",
    "lo"     -> "low",
    "hi"     -> "high",
    "l"      -> "left",
    "r"      -> "right",
    "succ"   -> "successor",
    "pred"   -> "predecessor",
    "tree"   -> "root",
    "h"      -> "head",
    "first"  -> "head"
  )

  /** Distinct hues for names with no canonical role — assigned by order of first appearance. */
  val fallback: Vector[String] =
    Vector("#3b82f6", "#10b981", "#f59e0b", "#a855f7", "#ef4444", "#06b6d4", "#64748b")

  /** The role colour for `name`, if it has one — a direct canon hit, then an alias → canon hit. */
  def roleColor(name: String): Option[String] =
    canon.get(name).orElse(aliases.get(name).flatMap(canon.get))

  /**
   * Assign a colour to every name in `names` (given in first-appearance order). Canon / alias names get their
   * role colour; the rest draw from the `fallback` palette, counting only the unknowns so each gets a
   * distinct hue. Stable for the whole trace — a pointer keeps one colour across every step.
   */
  def assignColors(names: List[String]): Map[String, String] =
    var unknown = 0
    names.distinct.map { name =>
      val color = roleColor(name).getOrElse {
        val c = fallback(math.floorMod(unknown, fallback.size))
        unknown += 1
        c
      }
      name -> color
    }.toMap
