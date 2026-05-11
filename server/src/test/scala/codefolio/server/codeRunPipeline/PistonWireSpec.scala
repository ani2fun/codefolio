package codefolio.server.codeRunPipeline

import codefolio.shared.api.Endpoints.RunnableLanguageInfo
import zio.test.*

/**
 * Golden-fixture tests for the pure Piston wire parser. No HTTP, no `Task` — just (JSON string → RunResult)
 * and (lang, source, stdin) → JSON string. Bugs in status mapping / compile-vs-run prioritisation surface
 * here without needing a stub HTTP server.
 */
object PistonWireSpec extends ZIOSpecDefault:

  private val python: RunnableLanguageInfo =
    Languages.resolve("python").getOrElse(throw new IllegalStateException("python missing"))

  /**
   * Synthetic language whose id is deliberately NOT in `PistonWire.pistonLanguage`. Keeps the test decoupled
   * from which real `Languages` entries happen to be in Piston's protocol map at any moment.
   */
  private val unknown: RunnableLanguageInfo =
    RunnableLanguageInfo(id = 999, label = "Unknown 999", aliases = Seq("unknown"))

  override def spec: Spec[Any, Any] = suite("PistonWire")(
    suite("supports")(
      test("supports a language whose id is in the Piston map") {
        assertTrue(PistonWire.supports(python))
      },
      test("rejects a language whose id is not in the Piston map") {
        assertTrue(!PistonWire.supports(unknown))
      }
    ),
    suite("buildRequestBody")(
      test("emits the Piston language name (not the Judge0 id) plus stdin and source") {
        val body = PistonWire.buildRequestBody(
          source = "print('hi')",
          stdin = Some("input data"),
          lang = python
        )
        assertTrue(
          body.contains("\"language\":\"python\""),
          body.contains("\"version\":\"*\""),
          body.contains("\"content\":\"print('hi')\""),
          body.contains("\"stdin\":\"input data\"")
        )
      },
      test("absent stdin defaults to an empty string") {
        val body = PistonWire.buildRequestBody("x", None, python)
        assertTrue(body.contains("\"stdin\":\"\""))
      },
      test("rejects an unsupported language with an IllegalArgumentException") {
        val ex = scala.util.Try(PistonWire.buildRequestBody("SELECT 1", None, unknown)).failed.toOption
        assertTrue(ex.exists(_.isInstanceOf[IllegalArgumentException]))
      }
    ),
    suite("parseRunResult")(
      test("Accepted run: stdout populated, statusId = 3") {
        val body =
          """{"compile":{},"run":{"stdout":"hi\n","stderr":"","code":0,"signal":null}}"""
        val result = PistonWire.parseRunResult(body)
        assertTrue(
          result.stdout == "hi\n",
          result.stderr == "",
          result.statusId == 3,
          result.statusDescription == "Accepted",
          result.time.isEmpty,
          result.memory.isEmpty
        )
      },
      test("Compilation error: statusId = 6, compileOutput is the compiler stderr") {
        val body =
          """{"compile":{"stdout":"","stderr":"err: undefined symbol","code":1},"run":{"stdout":"","stderr":"","code":null}}"""
        val result = PistonWire.parseRunResult(body)
        assertTrue(
          result.statusId == 6,
          result.statusDescription == "Compilation Error",
          result.compileOutput == "err: undefined symbol"
        )
      },
      test("Runtime non-zero exit: statusId = 11 (Runtime Error NZEC)") {
        val body =
          """{"compile":{},"run":{"stdout":"","stderr":"oops","code":1}}"""
        val result = PistonWire.parseRunResult(body)
        assertTrue(
          result.statusId == 11,
          result.statusDescription == "Runtime Error (NZEC)",
          result.stderr == "oops"
        )
      },
      test("Compile-output falls back to compile.stdout when compile.stderr is empty") {
        val body =
          """{"compile":{"stdout":"warning: deprecated","stderr":"","code":0},"run":{"stdout":"ok","stderr":"","code":0}}"""
        val result = PistonWire.parseRunResult(body)
        assertTrue(
          result.compileOutput == "warning: deprecated",
          result.statusId == 3
        )
      },
      test("Top-level Piston `message` envelope throws a RuntimeException") {
        val body = """{"message":"some piston-level error"}"""
        val ex   = scala.util.Try(PistonWire.parseRunResult(body)).failed.toOption
        assertTrue(
          ex.exists(_.isInstanceOf[RuntimeException]),
          ex.exists(_.getMessage.contains("Piston error"))
        )
      },
      test("Malformed JSON throws a RuntimeException") {
        val ex = scala.util.Try(PistonWire.parseRunResult("not json")).failed.toOption
        assertTrue(
          ex.exists(_.isInstanceOf[RuntimeException]),
          ex.exists(_.getMessage.contains("invalid JSON"))
        )
      }
    )
  )
