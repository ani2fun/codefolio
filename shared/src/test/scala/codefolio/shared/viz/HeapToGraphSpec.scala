package codefolio.shared.viz

import zio.test.*

/**
 * JVM unit tests for [[HeapToGraph]] — tree reconstruction from hand-built [[HeapTrace]] fixtures, the
 * inserted-node highlight, edge labels, the cursor, root resolution (hint + auto-detect), step coalescing,
 * carry-forward across frames that don't reach the root, and the truncated flag. No Python, no DOM.
 */
object HeapToGraphSpec extends ZIOSpecDefault:

  private def int(n: Long): HeapValue    = HeapValue.Scalar(HeapScalar.I(n))
  private val nul: HeapValue             = HeapValue.Scalar(HeapScalar.Null)
  private def ref(id: String): HeapValue = HeapValue.Ref(id)

  private def treeNode(value: Long, left: HeapValue, right: HeapValue): HeapObject =
    HeapObject.Instance("TreeNode", List("val" -> int(value), "left" -> left, "right" -> right))

  private def step(
      line: Int,
      locals: List[(String, HeapValue)],
      heap: (String, HeapObject)*
  ): HeapStep =
    HeapStep(line, "line", "fn", locals, heap.toMap)

  // tree [1, 2, 3, 4] — A=1 (l=B, r=C), B=2 (l=D, r=null), C=3, D=4
  private val before = step(
    2,
    List("tree" -> ref("A")),
    "A" -> treeNode(1, ref("B"), ref("C")),
    "B" -> treeNode(2, ref("D"), nul),
    "C" -> treeNode(3, nul, nul),
    "D" -> treeNode(4, nul, nul)
  )

  // after — node 9 (E) attached as B's right child
  private val after = step(
    3,
    List("tree" -> ref("A"), "node" -> ref("B")),
    "A" -> treeNode(1, ref("B"), ref("C")),
    "B" -> treeNode(2, ref("D"), ref("E")),
    "C" -> treeNode(3, nul, nul),
    "D" -> treeNode(4, nul, nul),
    "E" -> treeNode(9, nul, nul)
  )

  private val src = "line one\nline two\nline three"

  override def spec: Spec[Any, Any] = suite("HeapToGraph")(
    test("reconstructs the tree, highlights the inserted node, labels edges") {
      val result = HeapToGraph.adapt(
        HeapTrace(List(before, after), truncated = false),
        src,
        "binary-tree",
        Some("tree"),
        "t"
      )
      assertTrue(
        result.isRight,
        result.exists(_.steps.size == 2),
        result.exists(_.steps.head.nodes.size == 4),
        result.exists(_.steps.last.nodes.size == 5),
        result.exists(_.steps.last.nodes.exists(_.label == "9")),
        result.exists(_.steps.last.highlight == List("E")),
        result.exists(_.steps.last.edges.contains(VizEdge("B", "E", "right"))),
        result.exists(_.steps.last.edges.contains(VizEdge("A", "B", "left"))),
        result.exists(_.steps.last.cursor.size == 2),
        result.exists(_.steps.last.cursor.exists(c => c.name == "node" && c.target == "B")),
        result.exists(_.steps.last.annotation == "line three"),
        result.exists(_.layoutHint == "binary-tree")
      )
    },
    test("auto-detects the root when no hint is given") {
      val result =
        HeapToGraph.adapt(HeapTrace(List(before), truncated = false), src, "binary-tree", None, "t")
      assertTrue(result.isRight, result.exists(_.steps.head.nodes.size == 4))
    },
    test("coalesces consecutive identical steps") {
      val result = HeapToGraph.adapt(
        HeapTrace(List(before, before), truncated = false),
        src,
        "binary-tree",
        Some("tree"),
        "t"
      )
      assertTrue(result.exists(_.steps.size == 1))
    },
    test("propagates the truncated flag") {
      val result =
        HeapToGraph.adapt(HeapTrace(List(before), truncated = true), src, "binary-tree", Some("tree"), "t")
      assertTrue(result.exists(_.truncated))
    },
    test("an empty trace is a Left") {
      val result = HeapToGraph.adapt(HeapTrace(Nil, truncated = false), src, "binary-tree", None, "t")
      assertTrue(result.isLeft)
    },
    test("surfaces non-pointer scalar fields as node meta") {
      val avl = step(
        1,
        List("tree" -> ref("A")),
        "A" -> HeapObject.Instance(
          "AvlNode",
          List("key" -> int(10), "left" -> nul, "right" -> nul, "height" -> int(2))
        )
      )
      val result =
        HeapToGraph.adapt(HeapTrace(List(avl), truncated = false), src, "binary-tree", Some("tree"), "t")
      assertTrue(
        result.isRight,
        result.exists(_.steps.head.nodes.head.label == "10"),
        result.exists(_.steps.head.nodes.head.meta == List(VizField("height", "2")))
      )
    },
    test("carries the structure forward through a step whose frame doesn't reach the root") {
      // a helper frame mid-trace — its heap holds an unrelated object, no tree node
      val helper = step(
        1,
        List("self" -> ref("X")),
        "X" -> HeapObject.Instance("TreeNode", List("val" -> int(0), "left" -> nul, "right" -> nul))
      )
      val result = HeapToGraph.adapt(
        HeapTrace(List(before, helper, after), truncated = false),
        src,
        "binary-tree",
        Some("tree"),
        "t"
      )
      assertTrue(
        result.isRight,
        result.exists(_.steps.size == 3),
        result.exists(_.steps.forall(_.nodes.nonEmpty)),
        result.exists(_.steps(1).nodes.size == 4),
        result.exists(_.steps(1).cursor.isEmpty),
        result.exists(_.steps(1).annotation == "line one"),
        result.exists(_.steps.last.nodes.size == 5),
        result.exists(_.steps.last.highlight == List("E"))
      )
    }
  )
