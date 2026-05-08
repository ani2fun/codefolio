package codefolio.server.codeRunPipeline

import codefolio.shared.api.Endpoints.RunnableLanguageInfo
import zio.test.*

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Golden-fixture tests for the pure Code Runner wire parser. Mirrors `PistonWireSpec` but for the Judge0
 * submissions API: base64-encoded streams, optional `time` / `memory`, and a `message`-fallback for when
 * stderr is missing.
 */
object CodeRunnerWireSpec extends ZIOSpecDefault:

  private val python: RunnableLanguageInfo =
    Languages.resolve("python").getOrElse(throw new IllegalStateException("python missing"))

  private def b64(s: String): String =
    Base64.getEncoder.encodeToString(s.getBytes(StandardCharsets.UTF_8))

  /**
   * Build a Judge0-shape response body. Empty strings round-trip as `null` so the parser exercises both the
   * "field present" and "field absent" branches via the same helper.
   */
  private def respWithStatus(
      stdout: String = "",
      stderr: String = "",
      compileOutput: String = "",
      message: String = "",
      statusId: Int = 3,
      statusDescription: String = "Accepted",
      time: Option[String] = None,
      memory: Option[Long] = None
  ): String =
    val maybeQuoted = (s: String) => if s.isEmpty then "null" else "\"" + b64(s) + "\""
    val timeJson    = time.map(t => s"\"$t\"").getOrElse("null")
    val memoryJson  = memory.map(m => m.toString).getOrElse("null")
    s"""{
       |  "stdout": ${maybeQuoted(stdout)},
       |  "stderr": ${maybeQuoted(stderr)},
       |  "compile_output": ${maybeQuoted(compileOutput)},
       |  "message": ${maybeQuoted(message)},
       |  "status": {"id": $statusId, "description": "$statusDescription"},
       |  "time": $timeJson,
       |  "memory": $memoryJson
       |}""".stripMargin

  override def spec: Spec[Any, Any] = suite("CodeRunnerWire")(
    suite("PathAndQuery")(
      test("asks Code Runner for base64-encoded streams and a synchronous wait") {
        assertTrue(
          CodeRunnerWire.PathAndQuery.contains("base64_encoded=true"),
          CodeRunnerWire.PathAndQuery.contains("wait=true")
        )
      }
    ),
    suite("buildRequestBody")(
      test("base64-encodes source and stdin and uses the Judge0 language id") {
        val body = CodeRunnerWire.buildRequestBody(
          source = "print('hi')",
          stdin = Some("input"),
          lang = python
        )
        assertTrue(
          body.contains("\"language_id\":71"),
          body.contains("\"source_code\":\"" + b64("print('hi')") + "\""),
          body.contains("\"stdin\":\"" + b64("input") + "\"")
        )
      },
      test("absent stdin serialises as JSON null") {
        val body = CodeRunnerWire.buildRequestBody("x", None, python)
        assertTrue(body.contains("\"stdin\":null"))
      }
    ),
    suite("parseRunResult")(
      test("Decodes base64 stdout/stderr and propagates time/memory") {
        val body = respWithStatus(
          stdout = "hello\n",
          stderr = "",
          time = Some("0.012"),
          memory = Some(2048L)
        )
        val result = CodeRunnerWire.parseRunResult(body)
        assertTrue(
          result.stdout == "hello\n",
          result.stderr == "",
          result.statusId == 3,
          result.statusDescription == "Accepted",
          result.time == Some("0.012"),
          result.memory == Some(2048L)
        )
      },
      test("Empty stderr falls back to the protocol-level message field") {
        val body = respWithStatus(
          stdout = "",
          stderr = "",
          message = "Time limit exceeded",
          statusId = 5,
          statusDescription = "Time Limit Exceeded"
        )
        val result = CodeRunnerWire.parseRunResult(body)
        assertTrue(
          result.stderr == "Time limit exceeded",
          result.statusId == 5,
          result.statusDescription == "Time Limit Exceeded"
        )
      },
      test("Streamed stderr beats the message fallback when both are present") {
        val body = respWithStatus(
          stdout = "",
          stderr = "actual stderr",
          message = "envelope error"
        )
        val result = CodeRunnerWire.parseRunResult(body)
        assertTrue(result.stderr == "actual stderr")
      },
      test("Defaults to Unknown status when the status object is missing") {
        val body =
          s"""{
             |  "stdout": "${b64("hi")}",
             |  "stderr": null,
             |  "compile_output": null,
             |  "message": null,
             |  "time": null,
             |  "memory": null
             |}""".stripMargin
        val result = CodeRunnerWire.parseRunResult(body)
        assertTrue(
          result.statusId == 13,
          result.statusDescription == "Unknown"
        )
      },
      test("Malformed JSON throws a RuntimeException") {
        val ex = scala.util.Try(CodeRunnerWire.parseRunResult("not json")).failed.toOption
        assertTrue(
          ex.exists(_.isInstanceOf[RuntimeException]),
          ex.exists(_.getMessage.contains("invalid JSON"))
        )
      }
    )
  )
