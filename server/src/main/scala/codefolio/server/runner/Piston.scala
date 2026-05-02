package codefolio.server.runner

import codefolio.shared.api.Endpoints.{RunResult, RunnableLanguageInfo}
import io.circe.parser.parse
import zio.*

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets

/**
 * Port of `portfolio-app/src/lib/piston.ts`. Talks the Piston API (`/api/v2/execute`); used in production
 * where Piston-the-public-service runs the snippet. Status IDs in the returned `RunResult` follow the Judge0
 * convention (3 = Accepted, 6 = Compilation Error, 11 = Runtime Error) so the same UI maps both backends
 * without a switch.
 *
 * Piston doesn't expose `time` / `memory`, so they're left as `None`.
 */
object Piston:

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
   * Whether Piston supports a given language at all. SQL is special: route lib called it sqlite3 here but
   * Piston historically lacks a sqlite runtime — keep the entry, let the handler discover the 400 from
   * upstream, and prefer Code Runner when both are configured.
   */
  def supports(langId: Int): Boolean = pistonLanguage.contains(langId)

  /** Execute via Piston; returns `RunResult` mirroring Judge0's shape. */
  def run(
      baseUrl: String,
      source: String,
      stdin: Option[String],
      lang: RunnableLanguageInfo
  ): Task[RunResult] =
    pistonLanguage.get(lang.id) match
      case None =>
        ZIO.fail(new RuntimeException(s"Language ${lang.label} (id=${lang.id}) not supported by Piston"))
      case Some(pistonName) =>
        ZIO.attemptBlocking {
          val payload =
            s"""{
               |  "language": ${quote(pistonName)},
               |  "version": "*",
               |  "files": [{"content": ${quote(source)}}],
               |  "stdin": ${quote(stdin.getOrElse(""))},
               |  "compile_timeout": 10000,
               |  "run_timeout": 10000
               |}""".stripMargin

          val cleanedBase = baseUrl.replaceAll("/+$", "")
          val req = HttpRequest
            .newBuilder()
            .uri(URI.create(s"$cleanedBase/api/v2/execute"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .timeout(java.time.Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build()

          val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
          if resp.statusCode() < 200 || resp.statusCode() >= 300 then
            throw new RuntimeException(s"Piston returned ${resp.statusCode()}: ${resp.body()}")

          parseResult(resp.body())
        }

  // ---- internals ----------------------------------------------------------

  private val httpClient: HttpClient =
    HttpClient
      .newBuilder()
      .connectTimeout(java.time.Duration.ofSeconds(10))
      .build()

  /** JSON-string-encode a Java string. */
  private def quote(s: String): String =
    val sb = new StringBuilder("\"")
    s.foreach {
      case '"'           => sb.append("\\\"")
      case '\\'          => sb.append("\\\\")
      case '\b'          => sb.append("\\b")
      case '\f'          => sb.append("\\f")
      case '\n'          => sb.append("\\n")
      case '\r'          => sb.append("\\r")
      case '\t'          => sb.append("\\t")
      case c if c < 0x20 => sb.append(f"\\u${c.toInt}%04x")
      case c             => sb.append(c)
    }
    sb.append('"').toString

  /**
   * Convert a Piston response body to our RunResult. Mirrors the status mapping in piston.ts.
   */
  private def parseResult(body: String): RunResult =
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
