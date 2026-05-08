package codefolio.server.codeRunPipeline

import codefolio.shared.api.Endpoints.{RunResult, RunnableLanguageInfo}
import io.circe.Json
import io.circe.parser.parse

/**
 * Pure wire-format adapter for the Piston public-service API (`/api/v2/execute`).
 *
 * Owns the protocol details: which Judge0 language IDs Piston supports, the JSON request body shape, and the
 * mapping from Piston's response into our canonical [[RunResult]]. Lives behind `LivePistonBackend` in
 * production; exercised directly by `PistonWireSpec` against golden response fixtures so wire-level mapping
 * bugs surface without a stub HTTP server.
 *
 * Status IDs in the returned `RunResult` follow the Judge0 convention so the same UI maps both backends
 * without a switch. Piston doesn't expose `time` / `memory`.
 */
private[codeRunPipeline] object PistonWire:

  /** Map our canonical (Judge0) language ID to the string Piston wants. */
  private val pistonLanguage: Map[Int, String] = Map(
    71 -> "python",
    62 -> "java",
    81 -> "scala",
    50 -> "c",
    54 -> "c++",
    60 -> "go",
    73 -> "rust",
    78 -> "kotlin",
    74 -> "typescript",
    63 -> "javascript",
    82 -> "sqlite3"
  )

  /**
   * Whether Piston knows how to run a given language. False → orchestration should pick a different backend.
   */
  def supports(lang: RunnableLanguageInfo): Boolean =
    pistonLanguage.contains(lang.id)

  /**
   * Build the Piston request JSON. Caller must check [[supports]] first; an unsupported language panics with
   * an `IllegalArgumentException` (defense-in-depth — the orchestration ensures this can't happen).
   */
  def buildRequestBody(
      source: String,
      stdin: Option[String],
      lang: RunnableLanguageInfo
  ): String =
    val pistonName = pistonLanguage.getOrElse(
      lang.id,
      throw new IllegalArgumentException(s"Piston does not support ${lang.label} (id=${lang.id})")
    )
    Json
      .obj(
        "language"        -> Json.fromString(pistonName),
        "version"         -> Json.fromString("*"),
        "files"           -> Json.arr(Json.obj("content" -> Json.fromString(source))),
        "stdin"           -> Json.fromString(stdin.getOrElse("")),
        "compile_timeout" -> Json.fromInt(10000),
        "run_timeout"     -> Json.fromInt(10000)
      )
      .noSpaces

  /**
   * Parse a Piston response body into our canonical [[RunResult]]. Throws `RuntimeException` if the body
   * isn't valid JSON or carries a top-level `message` (Piston's protocol-error envelope). The caller (live
   * backend or test) absorbs the throw — `LivePistonBackend.run` does so via `ZIO.attemptBlocking`.
   */
  def parseRunResult(body: String): RunResult =
    val json = parse(body) match
      case Right(j) => j
      case Left(e)  => throw new RuntimeException(s"Piston returned invalid JSON: ${e.getMessage}")

    val cursor = json.hcursor
    cursor.get[String]("message").toOption.foreach { msg =>
      throw new RuntimeException(s"Piston error: $msg")
    }

    val compileObj = cursor.downField("compile")
    val runObj     = cursor.downField("run")

    val compileExitCode = compileObj.get[Int]("code").toOption
    val runExitCode     = runObj.get[Int]("code").toOption

    val (statusId, statusDescription) =
      if compileExitCode.exists(_ != 0) then (6, "Compilation Error")
      else if runExitCode.contains(0) then (3, "Accepted")
      else (11, "Runtime Error (NZEC)")

    val stdout = runObj.get[String]("stdout").toOption.getOrElse("")
    val stderr = runObj.get[String]("stderr").toOption.getOrElse("")
    val compileOutput =
      if compileObj.succeeded then
        val cStderr = compileObj.get[String]("stderr").toOption.getOrElse("")
        val cStdout = compileObj.get[String]("stdout").toOption.getOrElse("")
        if cStderr.nonEmpty then cStderr else cStdout
      else ""

    RunResult(
      stdout = stdout,
      stderr = stderr,
      compileOutput = compileOutput,
      statusId = statusId,
      statusDescription = statusDescription,
      time = None,
      memory = None
    )
