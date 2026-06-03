package codefolio.server.codeRunPipeline

import codefolio.shared.api.Endpoints.RunnableLanguageInfo

/**
 * The single source of truth for every runnable language and everything the pipeline needs to dispatch it to
 * a backend.
 *
 * Each [[Language]] bundles:
 *   - `info` — the public, codegen'd API shape ([[RunnableLanguageInfo]]: a Judge0 submission id, a display
 *     label, and the aliases the markdown layer / user can type). The Judge0 id is the canonical key; the
 *     local Code Runner backend speaks the Judge0 submissions protocol and uses it directly.
 *   - `pistonName` — the Piston-protocol runtime name, or `None` when Piston can't run this language. The
 *     orchestration treats `None` as "Piston doesn't support this" and falls through to the Code Runner
 *     backend.
 *
 * The wire adapters ([[PistonWire]], [[CodeRunnerWire]]) read backend ids from here rather than each carrying
 * its own map — adding a language is one entry in [[all]], not a change spread across three files.
 *
 * Mirror of `portfolio-app/src/lib/judge0.ts#RUNNABLE_LANGUAGES`.
 */
object Languages:

  /** Hard limit on `source` size, in UTF-8 bytes. Mirrors route.ts. */
  val MaxSourceBytes: Int = 64 * 1024

  /** Hard limit on `stdin` size, in UTF-8 bytes. */
  val MaxStdinBytes: Int = 16 * 1024

  /** One runnable language plus its per-backend dispatch knowledge. See the object docstring. */
  final case class Language(info: RunnableLanguageInfo, pistonName: Option[String]):
    def id: Int              = info.id
    def label: String        = info.label
    def aliases: Seq[String] = info.aliases

  // Judge0 id of Java — the one language whose source needs entrypoint normalisation before either backend
  // (the `public class` name must match the on-disk file, which both backends write as Main.java).
  private val JavaId: Int = 62

  val all: List[Language] = List(
    Language(
      RunnableLanguageInfo(id = 71, label = "Python 3.8", aliases = Seq("python", "py", "python3")),
      pistonName = Some("python")
    ),
    Language(
      RunnableLanguageInfo(id = JavaId, label = "Java 13 (OpenJDK)", aliases = Seq("java")),
      pistonName = Some("java")
    ),
    Language(
      RunnableLanguageInfo(id = 81, label = "Scala 3", aliases = Seq("scala")),
      pistonName = Some("scala")
    ),
    Language(
      RunnableLanguageInfo(id = 50, label = "C (GCC 9.2)", aliases = Seq("c")),
      pistonName = Some("c")
    ),
    Language(
      RunnableLanguageInfo(id = 54, label = "C++ (GCC 9.2)", aliases = Seq("cpp", "c++", "cxx")),
      pistonName = Some("c++")
    ),
    Language(
      RunnableLanguageInfo(id = 60, label = "Go 1.13", aliases = Seq("go", "golang")),
      pistonName = Some("go")
    ),
    Language(
      RunnableLanguageInfo(id = 73, label = "Rust 1.40", aliases = Seq("rust", "rs")),
      pistonName = Some("rust")
    ),
    Language(
      RunnableLanguageInfo(id = 78, label = "Kotlin 1.9", aliases = Seq("kotlin", "kt")),
      pistonName = Some("kotlin")
    ),
    Language(
      RunnableLanguageInfo(id = 74, label = "TypeScript 3.7", aliases = Seq("typescript", "ts")),
      pistonName = Some("typescript")
    ),
    Language(
      RunnableLanguageInfo(
        id = 63,
        label = "JavaScript (Node.js 12)",
        aliases = Seq("javascript", "js", "node")
      ),
      pistonName = Some("javascript")
    ),
    Language(
      RunnableLanguageInfo(id = 82, label = "SQL (SQLite 3.27)", aliases = Seq("sql", "sqlite")),
      pistonName = Some("sqlite3")
    )
  )

  /** Lowercased alias → language. Built once at class-load. */
  private val aliasIndex: Map[String, Language] =
    all.flatMap(lang => lang.aliases.map(a => a.toLowerCase -> lang)).toMap

  /**
   * Resolve an alias (case-insensitive). Returns `None` for unknown languages so the pipeline can return a
   * 400.
   */
  def resolve(alias: String): Option[Language] =
    Option(alias).map(_.trim.toLowerCase).filter(_.nonEmpty).flatMap(aliasIndex.get)

  /**
   * Sentinel that tracer-wrapped sources carry as their very first line.
   *
   * `Languages.effectiveSource` skips [[JavaSourceRewriter.normalizeEntrypoint]] when this sentinel is
   * present: the harness already emits a valid `public class Main` and re-running normalisation would be a
   * no-op at best and a destructive word-boundary rename at worst if the user's inner class happens to share
   * the first top-level name. This mitigates plan risk R3.
   *
   * The constant is defined here (rather than in the client) so both the server bypass and the client harness
   * can share the same literal without duplication.
   */
  val TracerSentinel: String = "// __CF_TRACER__"

  /**
   * The source to actually send to a backend: Java gets its entrypoint class renamed to `Main` (see
   * [[JavaSourceRewriter]]); every other language passes through untouched. Both wire adapters call this so
   * neither has to know which language is special.
   *
   * Exception: Java sources that begin with [[TracerSentinel]] skip normalisation — the JVM harness already
   * emits a well-formed `public class Main` wrapper and must not be altered.
   */
  def effectiveSource(lang: Language, source: String): String =
    if lang.id == JavaId && !source.trim.startsWith(TracerSentinel) then
      JavaSourceRewriter.normalizeEntrypoint(source)
    else source
