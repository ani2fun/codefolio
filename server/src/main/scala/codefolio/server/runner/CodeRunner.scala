package codefolio.server.runner

import codefolio.shared.api.Endpoints.{RunResult, RunnableLanguageInfo}
import io.circe.parser.parse
import zio.*

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.util.Base64

/** Local code-execution backend.
  *
  * Despite the name, this client speaks the **Judge0 submissions API** —
  * `POST /submissions/?base64_encoded=true&wait=true&fields=...` — because
  * that's the wire shape the local Code Runner container in
  * `portfolio-app/runner/` exposes. (The historic name `JUDGE0_URL` is a
  * misnomer: actual Judge0 is no longer used; the local runner just kept
  * the protocol so the original Next.js code didn't need to change. Hence
  * `CodeRunner` here, not `Judge0`.)
  *
  * Port of `portfolio-app/src/lib/judge0.ts`.
  */
object CodeRunner:

  def run(
      baseUrl: String,
      authToken: Option[String],
      source: String,
      stdin: Option[String],
      lang: RunnableLanguageInfo
  ): Task[RunResult] =
    ZIO.attemptBlocking {
      val cleanedBase = baseUrl.replaceAll("/+$", "")
      val url = URI.create(
        s"$cleanedBase/submissions/?base64_encoded=true&wait=true" +
          "&fields=stdout,stderr,compile_output,message,status,time,memory"
      )

      val payload =
        s"""{
           |  "language_id": ${lang.id},
           |  "source_code": ${quote(b64(source))},
           |  "stdin": ${stdin.map(s => quote(b64(s))).getOrElse("null")}
           |}""".stripMargin

      val builder = HttpRequest
        .newBuilder()
        .uri(url)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .timeout(java.time.Duration.ofSeconds(30))
        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))

      authToken.filter(_.nonEmpty).foreach(t => builder.header("X-Auth-Token", t))

      val resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if resp.statusCode() < 200 || resp.statusCode() >= 300 then
        throw new RuntimeException(s"Code Runner returned ${resp.statusCode()}: ${resp.body()}")

      parseResult(resp.body())
    }

  // ---- internals ----------------------------------------------------------

  private val httpClient: HttpClient =
    HttpClient
      .newBuilder()
      .connectTimeout(java.time.Duration.ofSeconds(10))
      .build()

  private def b64(s: String): String =
    Base64.getEncoder.encodeToString(s.getBytes(StandardCharsets.UTF_8))

  private def decodeB64(s: Option[String]): String =
    s.filter(_.nonEmpty)
      .map(b => new String(Base64.getDecoder.decode(b), StandardCharsets.UTF_8))
      .getOrElse("")

  /** JSON-string-encode a Java string. */
  private def quote(s: String): String =
    val sb = new StringBuilder("\"")
    s.foreach {
      case '"'  => sb.append("\\\"")
      case '\\' => sb.append("\\\\")
      case '\b' => sb.append("\\b")
      case '\f' => sb.append("\\f")
      case '\n' => sb.append("\\n")
      case '\r' => sb.append("\\r")
      case '\t' => sb.append("\\t")
      case c if c < 0x20 => sb.append(f"\\u${c.toInt}%04x")
      case c    => sb.append(c)
    }
    sb.append('"').toString

  private def parseResult(body: String): RunResult =
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

    val stderr = decodeB64(stderrB64)
    val message = decodeB64(messageB64)
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
