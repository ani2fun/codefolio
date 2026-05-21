package codefolio.shared.viz

/**
 * The raw heap-snapshot trace produced by the Python `PythonTracer` harness (ADR-0018).
 *
 * Each [[HeapStep]] is one `sys.settrace` event: the source line, the frame locals, and a snapshot of the
 * reachable object graph (the "heap"). [[HeapToGraph]] turns a `HeapTrace` into a renderable [[VizGraph]].
 *
 * Lives in `shared` so the adapter that consumes these types cross-compiles to the JVM (unit tests) and to
 * Scala.js (the Visualise modal). The client decodes the harness JSON into these types directly; there are no
 * codecs here — `HeapToGraphSpec` builds `HeapTrace` values by hand.
 */

/** A scalar leaf value inside a heap snapshot. */
enum HeapScalar:
  case I(value: Long)
  case D(value: Double)
  case B(value: Boolean)
  case S(value: String)
  case Null

/** A field / slot value: an inline scalar, or a reference to a heap object by its id. */
enum HeapValue:
  case Scalar(value: HeapScalar)
  case Ref(id: String)

/** Whether a sequence object came from a Python `list` or `tuple`. */
enum ArrKind:
  case Lst, Tup

/** A heap object: a class instance, a list/tuple, or a dict. */
enum HeapObject:
  case Instance(cls: String, fields: List[(String, HeapValue)])
  case Arr(kind: ArrKind, items: List[HeapValue])
  case Dict(entries: List[(HeapValue, HeapValue)])

/**
 * One traced step.
 *
 *   - `line` — 1-based source line about to run / just run.
 *   - `event` — the `settrace` event (`line` / `call` / `return`).
 *   - `fn` — the enclosing function name.
 *   - `locals` — the frame's local variables, name → value (scalar or ref).
 *   - `heap` — every reachable object this step, id → object.
 */
final case class HeapStep(
    line: Int,
    event: String,
    fn: String,
    locals: List[(String, HeapValue)],
    heap: Map[String, HeapObject]
)

/** A whole trace. `truncated` is set when the harness dropped steps to fit its payload budget. */
final case class HeapTrace(steps: List[HeapStep], truncated: Boolean)
