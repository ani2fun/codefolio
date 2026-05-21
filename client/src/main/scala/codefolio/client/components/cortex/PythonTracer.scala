package codefolio.client.components.cortex

import codefolio.shared.viz.*

import scala.scalajs.js
import scala.util.Try

/**
 * The one robust Python tracer (ADR-0018). Wraps user Python in a `sys.settrace` harness that captures, per
 * step, a **heap snapshot** — the reachable object graph, not just `repr` strings — so the same trace can
 * drive both the `python trace` step-through ([[TracedCodeBlock]]) and the engine-free Visualise diagram
 * ([[VisualiseModal]]).
 *
 * The server-side `/api/run` is unchanged: the wrapped program prints its trace as JSON between
 * `__CFHEAP_BEGIN__` / `__CFHEAP_END__` markers, after the user's own stdout. [[parse]] splits the two back
 * apart and decodes the JSON into the shared [[HeapTrace]] model.
 */
object PythonTracer:

  /** A parsed trace plus the user program's own stdout (everything printed before the marker). */
  final case class TraceResult(trace: HeapTrace, programStdout: String)

  private val BeginMarker = "__CFHEAP_BEGIN__"
  private val EndMarker   = "__CFHEAP_END__"

  private def base64Encode(s: String): String =
    js.Dynamic.global
      .btoa(js.Dynamic.global.unescape(js.Dynamic.global.encodeURIComponent(s)))
      .asInstanceOf[String]

  // ---------------------------------------------------------------------------
  // Harness — wraps user source in a sys.settrace tracer that snapshots the heap
  // each step and dumps {steps, truncated} as JSON between the marker pair.
  // ---------------------------------------------------------------------------

  /** Wrap user Python in the heap-snapshot tracer harness. The result is a runnable Python program. */
  def wrap(userSource: String): String =
    val encoded = base64Encode(userSource)
    s"""import sys, json, base64, math
       |
       |_cf_source = base64.b64decode("$encoded").decode("utf-8")
       |_cf_steps = []
       |_cf_truncated = [False]
       |_cf_step_limit = 600
       |_cf_max_objects = 400
       |_cf_max_depth = 60
       |_cf_max_payload = 512 * 1024
       |
       |def _cf_scalar(v):
       |    if v is None or isinstance(v, bool) or isinstance(v, int):
       |        return (True, v)
       |    if isinstance(v, float):
       |        return (True, v if math.isfinite(v) else repr(v))
       |    if isinstance(v, str):
       |        return (True, v if len(v) <= 80 else v[:80] + "\\u2026")
       |    return (False, None)
       |
       |def _cf_snapshot(items):
       |    heap = {}
       |    def visit(v, depth):
       |        is_s, sv = _cf_scalar(v)
       |        if is_s:
       |            return sv
       |        oid = str(id(v))
       |        if oid in heap:
       |            return {"ref": oid}
       |        if len(heap) >= _cf_max_objects or depth >= _cf_max_depth:
       |            _cf_truncated[0] = True
       |            return {"ref": oid}
       |        heap[oid] = None
       |        if isinstance(v, (list, tuple)):
       |            kind = "list" if isinstance(v, list) else "tuple"
       |            heap[oid] = {"type": kind,
       |                         "items": [visit(x, depth + 1) for x in list(v)[:_cf_max_objects]]}
       |        elif isinstance(v, dict):
       |            entries = []
       |            for dk, dv in list(v.items())[:_cf_max_objects]:
       |                entries.append([visit(dk, depth + 1), visit(dv, depth + 1)])
       |            heap[oid] = {"type": "dict", "entries": entries}
       |        else:
       |            d = getattr(v, "__dict__", None)
       |            if d is None:
       |                d = {}
       |                for sl in (getattr(type(v), "__slots__", ()) or ()):
       |                    if isinstance(sl, str) and hasattr(v, sl):
       |                        d[sl] = getattr(v, sl)
       |            fields = {}
       |            for fk, fv in list(d.items()):
       |                if isinstance(fk, str) and not fk.startswith("_cf_"):
       |                    fields[fk] = visit(fv, depth + 1)
       |            heap[oid] = {"type": "object", "cls": type(v).__name__, "fields": fields}
       |        return {"ref": oid}
       |    locs = {}
       |    for k, v in items:
       |        if isinstance(k, str) and not k.startswith("_cf_") and not k.startswith("__"):
       |            locs[k] = visit(v, 0)
       |    return locs, heap
       |
       |def _cf_tracer(frame, event, arg):
       |    if event in ("line", "call", "return") and frame.f_code.co_filename == "<traced>":
       |        if frame.f_lineno <= 0:
       |            return _cf_tracer
       |        try:
       |            locs, heap = _cf_snapshot(list(frame.f_locals.items()))
       |            _cf_steps.append({
       |                "line": frame.f_lineno,
       |                "event": event,
       |                "fn": frame.f_code.co_name,
       |                "locals": locs,
       |                "heap": heap,
       |            })
       |        except Exception:
       |            pass
       |        if len(_cf_steps) >= _cf_step_limit:
       |            _cf_truncated[0] = True
       |            sys.settrace(None)
       |    return _cf_tracer
       |
       |try:
       |    _cf_compiled = compile(_cf_source, "<traced>", "exec")
       |    _cf_ns = {"__name__": "__main__"}
       |    sys.settrace(_cf_tracer)
       |    try:
       |        exec(_cf_compiled, _cf_ns)
       |    finally:
       |        sys.settrace(None)
       |finally:
       |    while True:
       |        _cf_payload = json.dumps({"steps": _cf_steps, "truncated": _cf_truncated[0]})
       |        if len(_cf_payload) <= _cf_max_payload or len(_cf_steps) <= 1:
       |            break
       |        _cf_steps = _cf_steps[len(_cf_steps) // 4 + 1:]
       |        _cf_truncated[0] = True
       |    sys.stdout.write("\\n$BeginMarker")
       |    sys.stdout.write(_cf_payload)
       |    sys.stdout.write("$EndMarker\\n")
       |""".stripMargin

  // ---------------------------------------------------------------------------
  // Parsing — split the marker-delimited JSON out of stdout and decode it.
  // ---------------------------------------------------------------------------

  /**
   * Split the harness output: everything before `__CFHEAP_BEGIN__` is the user's program stdout; the JSON
   * between the markers is the trace. Missing markers (compile error, fatal harness exception) → an empty
   * trace and the raw stdout as program output.
   */
  def parse(stdout: String): TraceResult =
    val beginIdx = stdout.lastIndexOf(BeginMarker)
    if beginIdx < 0 then TraceResult(HeapTrace(Nil, truncated = false), stdout.stripSuffix("\n"))
    else
      val afterBegin = stdout.substring(beginIdx + BeginMarker.length)
      val endIdx     = afterBegin.indexOf(EndMarker)
      val jsonRaw    = if endIdx < 0 then afterBegin else afterBegin.substring(0, endIdx)
      val programOut = stdout.substring(0, beginIdx).stripSuffix("\n")
      val trace      = Try(decodeTrace(jsonRaw.trim)).getOrElse(HeapTrace(Nil, truncated = false))
      TraceResult(trace, programOut)

  private def decodeTrace(json: String): HeapTrace =
    val root      = js.JSON.parse(json)
    val truncated = root.truncated.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
    val stepsArr  = root.steps.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
    val steps     = stepsArr.toOption.fold(List.empty[HeapStep])(_.toList.map(decodeStep))
    HeapTrace(steps, truncated)

  private def decodeStep(s: js.Dynamic): HeapStep =
    val line   = s.line.asInstanceOf[js.UndefOr[Int]].getOrElse(0)
    val event  = s.event.asInstanceOf[js.UndefOr[String]].getOrElse("line")
    val fn     = s.fn.asInstanceOf[js.UndefOr[String]].getOrElse("")
    val locals = entriesOf(s.locals).map { case (k, v) => k -> decodeValue(v) }
    val heap = entriesOf(s.heap).collect {
      case (k, v) if v != null && !js.isUndefined(v) => k -> decodeObject(v)
    }.toMap
    HeapStep(line, event, fn, locals, heap)

  /** Own-enumerable string keys of a JS object, as `(key, value)` pairs. */
  private def entriesOf(d: js.Dynamic): List[(String, js.Dynamic)] =
    if js.isUndefined(d) || d == null then Nil
    else
      val obj = d.asInstanceOf[js.Object]
      js.Object.keys(obj).toList.map(k => k -> d.selectDynamic(k))

  private def decodeValue(v: js.Dynamic): HeapValue =
    if v == null then HeapValue.Scalar(HeapScalar.Null)
    else
      js.typeOf(v) match
        case "number" =>
          val d = v.asInstanceOf[Double]
          if d.isWhole then HeapValue.Scalar(HeapScalar.I(d.toLong))
          else HeapValue.Scalar(HeapScalar.D(d))
        case "boolean" => HeapValue.Scalar(HeapScalar.B(v.asInstanceOf[Boolean]))
        case "string"  => HeapValue.Scalar(HeapScalar.S(v.asInstanceOf[String]))
        case "object" =>
          v.ref.asInstanceOf[js.UndefOr[String]].toOption match
            case Some(id) => HeapValue.Ref(id)
            case None     => HeapValue.Scalar(HeapScalar.Null)
        case _ => HeapValue.Scalar(HeapScalar.Null)

  private def decodeObject(d: js.Dynamic): HeapObject =
    d.selectDynamic("type").asInstanceOf[js.UndefOr[String]].getOrElse("object") match
      case "list" | "tuple" =>
        val kind =
          if d.selectDynamic("type").asInstanceOf[String] == "tuple" then ArrKind.Tup
          else ArrKind.Lst
        val items = d.items
          .asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]]
          .toOption
          .fold(List.empty[HeapValue])(_.toList.map(decodeValue))
        HeapObject.Arr(kind, items)
      case "dict" =>
        val entries = d.entries
          .asInstanceOf[js.UndefOr[js.Array[js.Array[js.Dynamic]]]]
          .toOption
          .fold(List.empty[(HeapValue, HeapValue)]) { arr =>
            arr.toList.collect {
              case pair if pair.length >= 2 => decodeValue(pair(0)) -> decodeValue(pair(1))
            }
          }
        HeapObject.Dict(entries)
      case _ =>
        val cls    = d.cls.asInstanceOf[js.UndefOr[String]].getOrElse("object")
        val fields = entriesOf(d.fields).map { case (k, v) => k -> decodeValue(v) }
        HeapObject.Instance(cls, fields)
