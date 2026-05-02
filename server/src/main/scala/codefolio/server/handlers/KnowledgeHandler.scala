package codefolio.server.handlers

import codefolio.server.config.AppConfig
import codefolio.shared.api.Endpoints.{
  Book,
  ChapterFrontmatter,
  ChapterPayload,
  ChapterRef,
  KnowledgeIndex
}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import zio.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/** Errors the handler can raise. Mapped to HTTP status codes in HttpApp. */
sealed trait KnowledgeFailure extends Product with Serializable
object KnowledgeFailure:
  case object NotFound                              extends KnowledgeFailure
  final case class IO(detail: String)               extends KnowledgeFailure
  final case class IndexInvalid(detail: String)     extends KnowledgeFailure

trait KnowledgeHandler:
  def index: IO[KnowledgeFailure, KnowledgeIndex]
  def chapter(book: String, chapter: String): IO[KnowledgeFailure, ChapterPayload]

object KnowledgeHandler:

  val live: ZLayer[AppConfig, Nothing, KnowledgeHandler] =
    ZLayer.fromZIO(
      for
        cfg <- ZIO.service[AppConfig]
        // Cache the parsed index in a Ref. Content ships with the Docker
        // image — no point re-walking the disk on every request. Restart to
        // pick up changes.
        cache <- Ref.make(Option.empty[KnowledgeIndex])
      yield KnowledgeHandlerLive(cfg, cache)
    )

final private class KnowledgeHandlerLive(
    cfg: AppConfig,
    cache: Ref[Option[KnowledgeIndex]]
) extends KnowledgeHandler:

  import KnowledgeHandlerCodecs.given

  // ---- Path resolution + traversal guard -----------------------------------

  private val rootFile: File = File(cfg.knowledge.root).getAbsoluteFile
  private val rootPath: Path =
    if rootFile.isDirectory then rootFile.toPath.toRealPath()
    else rootFile.toPath.toAbsolutePath.normalize

  private def safeUnder(rel: String): Either[KnowledgeFailure, File] =
    val candidate = File(rootFile, rel)
    if !candidate.exists() || !candidate.isFile() then Left(KnowledgeFailure.NotFound)
    else
      val real =
        try candidate.toPath.toRealPath()
        catch case _: Throwable => candidate.toPath.toAbsolutePath.normalize
      if real.startsWith(rootPath) then Right(candidate)
      else Left(KnowledgeFailure.NotFound)

  // ---- Index ---------------------------------------------------------------

  override def index: IO[KnowledgeFailure, KnowledgeIndex] =
    cache.get.flatMap {
      case Some(idx) => ZIO.succeed(idx)
      case None      =>
        loadIndex.tap(idx => cache.set(Some(idx)))
    }

  private def loadIndex: IO[KnowledgeFailure, KnowledgeIndex] =
    for
      rootRaw <- readFileSafe("meta.json")
      rootIdx <- ZIO
        .fromEither(decode[RootIndex](rootRaw))
        .mapError(e => KnowledgeFailure.IndexInvalid(s"root meta.json: ${e.getMessage}"))
      books <- ZIO.foreach(rootIdx.books) { ref =>
        loadBook(ref)
      }
    yield KnowledgeIndex(books)

  private def loadBook(ref: RootBookRef): IO[KnowledgeFailure, Book] =
    for
      raw <- readFileSafe(s"${ref.slug}/meta.json")
      idx <- ZIO
        .fromEither(decode[BookMetaJson](raw))
        .mapError(e => KnowledgeFailure.IndexInvalid(s"${ref.slug}/meta.json: ${e.getMessage}"))
    yield Book(
      slug = ref.slug,
      title = idx.title.getOrElse(ref.title),
      description = idx.description.getOrElse(ref.description),
      tags = idx.tags.orElse(ref.tags),
      estimatedReadingMinutes = idx.estimatedReadingMinutes.orElse(ref.estimatedReadingMinutes),
      chapters = idx.chapters.map(c => ChapterRef(slug = c.slug, title = c.title, group = c.group))
    )

  // ---- Chapter -------------------------------------------------------------

  override def chapter(book: String, chapter: String): IO[KnowledgeFailure, ChapterPayload] =
    if !slugLike(book) || !slugLike(chapter) then ZIO.fail(KnowledgeFailure.NotFound)
    else
      for
        idx <- index
        bk <- ZIO
          .fromOption(idx.books.find(_.slug == book))
          .orElseFail(KnowledgeFailure.NotFound)
        chIndex = bk.chapters.indexWhere(_.slug == chapter)
        _ <- ZIO.when(chIndex < 0)(ZIO.fail(KnowledgeFailure.NotFound))
        ch = bk.chapters(chIndex)
        raw <- readFileSafe(s"$book/$chapter.md")
        (frontmatter, body) = parseFrontmatter(raw, ch)
        prevSlug = if chIndex > 0 then Some(bk.chapters(chIndex - 1).slug) else None
        nextSlug =
          if chIndex < bk.chapters.length - 1 then Some(bk.chapters(chIndex + 1).slug) else None
      yield ChapterPayload(
        book = bk,
        chapter = ch,
        frontmatter = frontmatter,
        raw = body,
        prevSlug = prevSlug,
        nextSlug = nextSlug
      )

  /** Reject anything that isn't a simple slug. Belt-and-braces with the
    * realpath check.
    */
  private def slugLike(s: String): Boolean =
    s.nonEmpty && s.forall(c => c.isLetterOrDigit || c == '-' || c == '_')

  // ---- Frontmatter parser --------------------------------------------------
  //
  // Hand-rolled: looks for `---` … `---` at the very top of the file and
  // pulls out `key: value` pairs (only `title`, `summary`, `group`). Avoids
  // pulling in a YAML library — frontmatter in this corpus is always a
  // handful of plain string fields.

  private def parseFrontmatter(
      content: String,
      ch: ChapterRef
  ): (ChapterFrontmatter, String) =
    val lines = content.split("\\R", -1).toIndexedSeq
    if lines.headOption.contains("---") then
      val end = lines.indexOf("---", 1)
      if end > 0 then
        val fmLines = lines.slice(1, end).toList
        val rest = lines.drop(end + 1).mkString("\n")
        val map = fmLines.flatMap(parseFrontmatterLine).toMap
        val fm = ChapterFrontmatter(
          title = map.getOrElse("title", ch.title),
          summary = map.get("summary"),
          group = map.get("group").orElse(ch.group)
        )
        (fm, rest)
      else (ChapterFrontmatter(title = ch.title, summary = None, group = ch.group), content)
    else (ChapterFrontmatter(title = ch.title, summary = None, group = ch.group), content)

  private def parseFrontmatterLine(line: String): Option[(String, String)] =
    val idx = line.indexOf(':')
    if idx <= 0 then None
    else
      val key = line.substring(0, idx).trim
      val rawValue = line.substring(idx + 1).trim
      val value = stripQuotes(rawValue)
      if value.nonEmpty then Some(key -> value) else None

  private def stripQuotes(s: String): String =
    if (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")) then
      if s.length >= 2 then s.substring(1, s.length - 1) else s
    else s

  // ---- Filesystem helper ---------------------------------------------------

  private def readFileSafe(rel: String): IO[KnowledgeFailure, String] =
    safeUnder(rel) match
      case Left(e) => ZIO.fail(e)
      case Right(file) =>
        ZIO
          .attemptBlocking {
            new String(Files.readAllBytes(file.toPath), StandardCharsets.UTF_8)
          }
          .mapError(t => KnowledgeFailure.IO(t.getMessage))

// ---- JSON shapes for parsing the on-disk meta files (private to handler) -

private[handlers] final case class RootBookRef(
    slug: String,
    title: String,
    description: String,
    tags: Option[Seq[String]],
    estimatedReadingMinutes: Option[Int]
)
private[handlers] final case class RootIndex(books: Seq[RootBookRef])

private[handlers] final case class BookMetaChapter(slug: String, title: String, group: Option[String])
private[handlers] final case class BookMetaJson(
    title: Option[String],
    description: Option[String],
    tags: Option[Seq[String]],
    estimatedReadingMinutes: Option[Int],
    chapters: Seq[BookMetaChapter]
)

private[handlers] object KnowledgeHandlerCodecs:
  given Decoder[RootBookRef]     = deriveDecoder
  given Decoder[RootIndex]       = deriveDecoder
  given Decoder[BookMetaChapter] = deriveDecoder
  given Decoder[BookMetaJson]    = deriveDecoder
