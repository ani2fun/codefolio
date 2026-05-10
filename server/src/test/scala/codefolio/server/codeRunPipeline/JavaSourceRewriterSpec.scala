package codefolio.server.codeRunPipeline

import zio.test.*

object JavaSourceRewriterSpec extends ZIOSpecDefault:

  override def spec: Spec[Any, Any] = suite("JavaSourceRewriter.normalizeEntrypoint")(
    test("rewrites `public class Solution` and its self-references to Main") {
      val src =
        """public class Solution {
          |  public static void main(String[] args) {
          |    Solution s = new Solution();
          |    System.out.println(Solution.class.getName());
          |  }
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(
        out.contains("public class Main"),
        !out.contains("public class Solution"),
        out.contains("Main s = new Main()"),
        out.contains("Main.class.getName()")
      )
    },
    test("passes through when the public class is already Main") {
      val src =
        """public class Main {
          |  public static void main(String[] args) {
          |    System.out.println("hi");
          |  }
          |}""".stripMargin
      assertTrue(JavaSourceRewriter.normalizeEntrypoint(src) == src)
    },
    test("renames a non-public top-level class too — Judge0 runs `java Main` regardless") {
      val src =
        """class Helper {
          |  public static void main(String[] args) {
          |    System.out.println(1);
          |  }
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(out.contains("class Main"), !out.contains("class Helper"))
    },
    test("only renames word-boundary occurrences (does not match substrings)") {
      val src =
        """public class Sol {
          |  public static void main(String[] args) {
          |    String SolHelper = "do not touch substring SolHelper";
          |    Sol x = new Sol();
          |  }
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(
        out.contains("public class Main"),
        out.contains("Main x = new Main()"),
        out.contains("\"do not touch substring SolHelper\""),
        out.contains("String SolHelper =")
      )
    },
    test("handles modifier ordering — `public final class Foo`") {
      val src =
        """public final class Foo {
          |  public static void main(String[] args) {}
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(out.contains("public final class Main"), !out.contains("class Foo"))
    },
    test("handles `public abstract class Bar`") {
      val src =
        """public abstract class Bar {
          |  public static void main(String[] args) {}
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(out.contains("public abstract class Main"))
    },
    test("rewrites a non-public top-level `class Solution` (Judge0 fails to find Main otherwise)") {
      val src =
        """import java.util.*;
          |
          |class Solution {
          |  static class Node { int val; Node next; }
          |  public static void main(String[] args) { new Solution(); }
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(
        out.contains("class Main {"),
        !out.contains("class Solution"),
        out.contains("new Main()"),
        // The nested `static class Node` must NOT be touched.
        out.contains("static class Node {")
      )
    },
    test("does not touch indented (nested) class declarations") {
      val src =
        """class Outer {
          |  static class Inner {}
          |  public static void main(String[] args) {}
          |}""".stripMargin
      val out = JavaSourceRewriter.normalizeEntrypoint(src)
      assertTrue(
        out.contains("class Main"),
        // Inner stays Inner — we only renamed the outermost (top-level) class.
        out.contains("static class Inner {}")
      )
    },
    test("passes through when an existing `Main` class is present (avoid collision)") {
      val src =
        """class Helper {
          |  void aux() {}
          |}
          |public class Main {
          |  public static void main(String[] args) { new Helper(); }
          |}""".stripMargin
      assertTrue(JavaSourceRewriter.normalizeEntrypoint(src) == src)
    }
  )
