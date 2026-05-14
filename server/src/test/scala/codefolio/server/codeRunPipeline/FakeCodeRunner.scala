package codefolio.server.codeRunPipeline

import codefolio.server.codeRunPipeline.Languages.Language
import codefolio.shared.api.Endpoints.RunResult
import zio.*

import java.util.concurrent.atomic.AtomicReference

/**
 * Test-only Code-Runner-shaped backend. Code Runner is the universal fallback in production — every language
 * is supported, so this fake claims `supports = true` unconditionally.
 */
final class FakeCodeRunner(
    response: Task[RunResult]
) extends CodeExecutionBackend:

  private val recorded =
    AtomicReference(List.empty[FakeCodeRunner.Call])

  override def supports(lang: Language): Boolean = true

  override def run(
      source: String,
      stdin: Option[String],
      lang: Language
  ): Task[RunResult] =
    ZIO.succeed(recorded.updateAndGet(_ :+ FakeCodeRunner.Call(source, stdin, lang))) *> response

  def calls: List[FakeCodeRunner.Call] = recorded.get()

object FakeCodeRunner:

  final case class Call(source: String, stdin: Option[String], lang: Language)

  def succeeding(result: RunResult): FakeCodeRunner =
    FakeCodeRunner(ZIO.succeed(result))

  def failing(error: Throwable): FakeCodeRunner =
    FakeCodeRunner(ZIO.fail(error))
