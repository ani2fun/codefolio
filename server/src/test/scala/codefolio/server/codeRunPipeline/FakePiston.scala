package codefolio.server.codeRunPipeline

import codefolio.server.codeRunPipeline.Languages.Language
import codefolio.shared.api.Endpoints.RunResult
import zio.*

import java.util.concurrent.atomic.AtomicReference

/**
 * Test-only Piston-shaped backend. Records every invocation and returns a canned response. `languageIds` is
 * the set of Judge0 IDs this fake claims to support — mirrors the real Piston's protocol-specific whitelist.
 */
final class FakePiston(
    languageIds: Set[Int],
    response: Task[RunResult]
) extends CodeExecutionBackend:

  private val recorded =
    AtomicReference(List.empty[FakePiston.Call])

  override def supports(lang: Language): Boolean =
    languageIds.contains(lang.id)

  override def run(
      source: String,
      stdin: Option[String],
      lang: Language
  ): Task[RunResult] =
    ZIO.succeed(recorded.updateAndGet(_ :+ FakePiston.Call(source, stdin, lang))) *> response

  def calls: List[FakePiston.Call] = recorded.get()

object FakePiston:

  final case class Call(source: String, stdin: Option[String], lang: Language)

  def succeeding(languageIds: Set[Int], result: RunResult): FakePiston =
    FakePiston(languageIds, ZIO.succeed(result))

  def failing(languageIds: Set[Int], error: Throwable): FakePiston =
    FakePiston(languageIds, ZIO.fail(error))
