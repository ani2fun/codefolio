package codefolio.server.codeRunPipeline

import codefolio.shared.api.Endpoints.RunnableLanguageInfo

/**
 * The set of languages we expose as runnable code blocks.
 *
 * The numeric `id` field is the canonical identifier and matches Judge0's submission language IDs (kept as
 * the canonical key so the same enum works for both Piston and the local Code Runner container, which speaks
 * the Judge0 submissions API protocol). Aliases let the markdown layer (and the user) write `python` or `py`
 * without caring about the IDs.
 *
 * Mirror of `portfolio-app/src/lib/judge0.ts#RUNNABLE_LANGUAGES`. The Piston wire layer
 * ([[PistonWire.pistonLanguage]]) currently maps every entry here onto a Piston runtime; if a future language
 * is added that Piston doesn't support, the orchestration falls back to the Code Runner backend.
 */
object Languages:

  /** Hard limit on `source` size, in UTF-8 bytes. Mirrors route.ts. */
  val MaxSourceBytes: Int = 64 * 1024

  /** Hard limit on `stdin` size, in UTF-8 bytes. */
  val MaxStdinBytes: Int = 16 * 1024

  val all: List[RunnableLanguageInfo] = List(
    RunnableLanguageInfo(id = 71, label = "Python 3.8", aliases = Seq("python", "py", "python3")),
    RunnableLanguageInfo(id = 62, label = "Java 13 (OpenJDK)", aliases = Seq("java")),
    RunnableLanguageInfo(id = 81, label = "Scala 2.13", aliases = Seq("scala")),
    RunnableLanguageInfo(id = 50, label = "C (GCC 9.2)", aliases = Seq("c")),
    RunnableLanguageInfo(id = 54, label = "C++ (GCC 9.2)", aliases = Seq("cpp", "c++", "cxx")),
    RunnableLanguageInfo(id = 60, label = "Go 1.13", aliases = Seq("go", "golang")),
    RunnableLanguageInfo(id = 73, label = "Rust 1.40", aliases = Seq("rust", "rs")),
    RunnableLanguageInfo(id = 78, label = "Kotlin 1.9", aliases = Seq("kotlin", "kt")),
    RunnableLanguageInfo(id = 74, label = "TypeScript 3.7", aliases = Seq("typescript", "ts")),
    RunnableLanguageInfo(
      id = 63,
      label = "JavaScript (Node.js 12)",
      aliases = Seq("javascript", "js", "node")
    ),
    RunnableLanguageInfo(id = 82, label = "SQL (SQLite 3.27)", aliases = Seq("sql", "sqlite"))
  )

  /** Lowercased alias → language. Built once at class-load. */
  private val aliasIndex: Map[String, RunnableLanguageInfo] =
    all.flatMap(lang => lang.aliases.map(a => a.toLowerCase -> lang)).toMap

  /**
   * Resolve an alias (case-insensitive). Returns `None` for unknown languages so the pipeline can return a
   * 400.
   */
  def resolve(alias: String): Option[RunnableLanguageInfo] =
    Option(alias).map(_.trim.toLowerCase).filter(_.nonEmpty).flatMap(aliasIndex.get)
