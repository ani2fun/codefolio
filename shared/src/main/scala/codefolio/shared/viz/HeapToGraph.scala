package codefolio.shared.viz

import scala.collection.mutable

/**
 * Turns a [[HeapTrace]] into a [[VizGraph]] — the one generic, data-structure-agnostic adapter (ADR-0018).
 *
 * It does no algorithm- or shape-specific work. It picks a root object, walks the object graph by reference
 * reachability, maps objects to nodes and references to edges, and derives per-step emphasis from the diff
 * between consecutive heaps. The *shape* of the drawing is entirely the layout's job — adding a data
 * structure adds a layout, never a branch here.
 */
object HeapToGraph:

  /** Instance field names treated as a node's display value, in priority order. */
  private val ValueFields = List("val", "value", "data", "key", "item")

  /**
   * @param trace
   *   the decoded heap trace
   * @param source
   *   the traced Python source — supplies per-step line annotations
   * @param layoutHint
   *   forwarded to [[VizGraph.layoutHint]]; selects the renderer's layout
   * @param rootHint
   *   optional variable name whose object is the structure root
   * @param title
   *   the modal title
   */
  def adapt(
      trace: HeapTrace,
      source: String,
      layoutHint: String,
      rootHint: Option[String],
      title: String
  ): Either[String, VizGraph] =
    if trace.steps.isEmpty then Left("The trace produced no steps.")
    else
      resolveRootId(trace, rootHint) match
        case None =>
          Left(
            "Couldn't find a structure to visualise — add a `viz-root=<variable>` hint " +
              "naming the variable that holds the data structure."
          )
        case Some(rootId) =>
          val srcLines = source.split("\n", -1).toVector
          val built    = trace.steps.map(step => buildStep(step, rootId, srcLines))
          val visible  = carryForward(built).dropWhile(_.nodes.isEmpty)
          val steps    = withHighlights(coalesce(visible))
          if steps.forall(_.nodes.isEmpty) then
            Left("The chosen root never held a structure during the trace.")
          else Right(VizGraph(steps, layoutHint, title, trace.truncated))

  /** The structure root: the `rootHint` variable's object if found, else an auto-detected root. */
  private def resolveRootId(trace: HeapTrace, rootHint: Option[String]): Option[String] =
    val byHint = rootHint.flatMap { name =>
      trace.steps.iterator.flatMap(_.locals).collectFirst {
        case (n, HeapValue.Ref(id)) if n == name => id
      }
    }
    byHint.orElse(autoDetectRoot(trace))

  /** Auto-detect: the in-degree-0 instance with the largest reachable object set, in the final heap. */
  private def autoDetectRoot(trace: HeapTrace): Option[String] =
    val heap        = trace.steps.last.heap
    val instanceIds = heap.collect { case (id, _: HeapObject.Instance) => id }.toSet
    if instanceIds.isEmpty then None
    else
      val referenced = heap.values.flatMap(outRefs).toSet
      val roots      = instanceIds.diff(referenced)
      val pool       = if roots.nonEmpty then roots else instanceIds
      pool.maxByOption(id => reachableFrom(id, heap).size)

  /** One step → the subgraph of instances reachable from `rootId`. */
  private def buildStep(step: HeapStep, rootId: String, srcLines: Vector[String]): VizGraphStep =
    val annotation =
      if step.line >= 1 && step.line <= srcLines.size then srcLines(step.line - 1).trim else ""
    if !step.heap.contains(rootId) then VizGraphStep(Nil, Nil, Nil, Nil, annotation, step.line)
    else
      val reachable = reachableFrom(rootId, step.heap)
      val nodes = reachable.flatMap { id =>
        step.heap.get(id) match
          case Some(inst: HeapObject.Instance) =>
            val (label, meta) = nodeView(inst)
            Some(VizNode(id, label, inst.cls, meta))
          case _ => None
      }
      val nodeIds = nodes.iterator.map(_.id).toSet
      val edges = reachable.flatMap { id =>
        step.heap.get(id) match
          case Some(inst: HeapObject.Instance) if nodeIds.contains(id) =>
            inst.fields.collect {
              case (field, HeapValue.Ref(to)) if nodeIds.contains(to) => VizEdge(id, to, field)
            }
          case _ => Nil
      }
      val cursor = step.locals.collect {
        case (name, HeapValue.Ref(id)) if nodeIds.contains(id) => VizCursor(name, id)
      }.distinct
      VizGraphStep(nodes, edges, cursor, Nil, annotation, step.line)

  /**
   * A node's display: the primary value label (the first present value-field) plus the object's *other*
   * scalar fields as `meta` sub-labels — so per-node state (an AVL `height`, an RBT `color`, …) shows on the
   * diagram, not just the value.
   *
   * `null`-valued fields are dropped from `meta`: an empty `left` / `right` is a pointer slot, not state, and
   * `field=null` sub-labels under every leaf would clutter rather than teach.
   */
  private def nodeView(inst: HeapObject.Instance): (String, List[VizField]) =
    val byName     = inst.fields.toMap
    val valueField = ValueFields.find(byName.contains)
    val label = valueField.flatMap(byName.get) match
      case Some(HeapValue.Scalar(s)) => scalarLabel(s)
      case Some(HeapValue.Ref(_))    => "·"
      case None                      => inst.cls
    val meta = inst.fields.collect {
      case (name, HeapValue.Scalar(s)) if !valueField.contains(name) && s != HeapScalar.Null =>
        VizField(name, scalarLabel(s))
    }
    (label, meta)

  private def scalarLabel(s: HeapScalar): String = s match
    case HeapScalar.I(v) => v.toString
    case HeapScalar.D(v) => v.toString
    case HeapScalar.B(v) => v.toString
    case HeapScalar.S(v) => v
    case HeapScalar.Null => "null"

  /** Every object id referenced directly by an object's fields / items / entries. */
  private def outRefs(obj: HeapObject): List[String] = obj match
    case HeapObject.Instance(_, fields) => fields.collect { case (_, HeapValue.Ref(id)) => id }
    case HeapObject.Arr(_, items)       => items.collect { case HeapValue.Ref(id) => id }
    case HeapObject.Dict(entries) =>
      entries.flatMap { case (k, v) => List(k, v) }.collect { case HeapValue.Ref(id) => id }

  /** Ids reachable from `start` over references, in deterministic depth-first order. */
  private def reachableFrom(start: String, heap: Map[String, HeapObject]): List[String] =
    val seen = mutable.LinkedHashSet.empty[String]
    def go(id: String): Unit =
      if heap.contains(id) && seen.add(id) then outRefs(heap(id)).foreach(go)
    go(start)
    seen.toList

  /**
   * Replace every empty-graph step with the most recent non-empty structure, so the diagram never blanks out
   * while the trace steps through a frame whose locals don't reach the root — a `TreeNode.__init__`, a helper
   * call. The carried step keeps its own source line and annotation (the code pane still advances) but shows
   * the last structure as context, with no cursor: no local in that frame points into the structure. Leading
   * steps — before the structure first exists — stay empty; `adapt` drops them.
   */
  private def carryForward(steps: List[VizGraphStep]): List[VizGraphStep] =
    val out  = mutable.ListBuffer.empty[VizGraphStep]
    var last = Option.empty[VizGraphStep]
    steps.foreach { s =>
      if s.nodes.nonEmpty then
        out += s
        last = Some(s)
      else
        out += last.fold(s)(prev => s.copy(nodes = prev.nodes, edges = prev.edges))
    }
    out.toList

  /** Drop consecutive steps whose visible state (nodes, edges, cursor) is unchanged — keep the first. */
  private def coalesce(steps: List[VizGraphStep]): List[VizGraphStep] = steps match
    case Nil => Nil
    case head :: tail =>
      val out = mutable.ListBuffer(head)
      tail.foreach { s =>
        val prev = out.last
        if s.nodes != prev.nodes || s.edges != prev.edges || s.cursor != prev.cursor then out += s
      }
      out.toList

  /** Fill each step's `highlight` with node ids absent from the previous step. */
  private def withHighlights(steps: List[VizGraphStep]): List[VizGraphStep] =
    val v = steps.toVector
    v.indices.map { i =>
      if i == 0 then v(i)
      else
        val prevIds = v(i - 1).nodes.iterator.map(_.id).toSet
        v(i).copy(highlight = v(i).nodes.iterator.map(_.id).filterNot(prevIds).toList)
    }.toList
