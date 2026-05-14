package codefolio.server.codeRunPipeline

import codefolio.server.codeRunPipeline.Languages.Language
import codefolio.shared.api.Endpoints.RunResult
import io.circe.Json
import io.circe.parser.parse

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Pure wire-format adapter for the local Code Runner backend (a Judge0-API-compatible container). Despite the
 * name, speaks the Judge0 submissions API — that's the wire shape the Code Runner image exposes.
 *
 * Owns the path/query suffix, the base64-encoded JSON request body shape, and the mapping from Judge0's
 * response (also base64-encoded fields) into our canonical [[RunResult]]. The numeric language id comes from
 * [[Languages]] (`Language.id`), not a constant kept here. Exercised directly by `CodeRunnerWireSpec` against
 * golden response fixtures so wire-level mapping bugs surface without a stub HTTP server.
 */
private[codeRunPipeline] object CodeRunnerWire:

  /**
   * Path-and-query string appended to the configured base URL. Asks for base64 encoding (so we can roundtrip
   * binary stdout/stderr safely) and a synchronous wait for the result.
   */
  val PathAndQuery: String =
    "/submissions/?base64_encoded=true&wait=true" +
      "&fields=stdout,stderr,compile_output,message,status,time,memory"

  /**
   * Build the Judge0 submission request body. Source and stdin are base64-encoded; the language id is the
   * canonical Judge0 id from [[Languages]].
   */
  def buildRequestBody(
      source: String,
      stdin: Option[String],
      lang: Language
  ): String =
    val effectiveSource = Languages.effectiveSource(lang, source)
    Json
      .obj(
        "language_id" -> Json.fromInt(lang.id),
        "source_code" -> Json.fromString(b64(effectiveSource)),
        "stdin"       -> stdin.map(s => Json.fromString(b64(s))).getOrElse(Json.Null)
      )
      .noSpaces

  /**
   * Parse a Code Runner response body into our canonical [[RunResult]]. Throws `RuntimeException` on
   * malformed JSON. Empty `stderr` falls back to Judge0's protocol-level `message` field so a runtime error
   * with no streamed stderr still surfaces something actionable.
   */
  def parseRunResult(body: String): RunResult =
    val json = parse(body) match
      case Right(j) => j
      case Left(e)  => throw new RuntimeException(s"Code Runner returned invalid JSON: ${e.getMessage}")

    val cursor = json.hcursor

    val stdoutB64        = cursor.get[String]("stdout").toOption
    val stderrB64        = cursor.get[String]("stderr").toOption
    val compileOutputB64 = cursor.get[String]("compile_output").toOption
    val messageB64       = cursor.get[String]("message").toOption
    val time             = cursor.get[String]("time").toOption
    val memory           = cursor.get[Long]("memory").toOption

    val statusId = cursor.downField("status").get[Int]("id").getOrElse(13)
    val statusDescription =
      cursor.downField("status").get[String]("description").getOrElse("Unknown")

    val stderr          = decodeB64(stderrB64)
    val message         = decodeB64(messageB64)
    val effectiveStderr = if stderr.nonEmpty then stderr else message

    RunResult(
      stdout = decodeB64(stdoutB64),
      stderr = effectiveStderr,
      compileOutput = decodeB64(compileOutputB64),
      statusId = statusId,
      statusDescription = statusDescription,
      time = time,
      memory = memory
    )

  private def b64(s: String): String =
    Base64.getEncoder.encodeToString(s.getBytes(StandardCharsets.UTF_8))

  private def decodeB64(s: Option[String]): String =
    s.filter(_.nonEmpty)
      .map(b => new String(Base64.getDecoder.decode(b), StandardCharsets.UTF_8))
      .getOrElse("")
