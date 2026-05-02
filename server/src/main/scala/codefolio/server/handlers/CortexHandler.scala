package codefolio.server.handlers

import codefolio.server.config.AppConfig
import codefolio.shared.api.Endpoints.{Book, ChapterFrontmatter, ChapterPayload, ChapterRef, CortexIndex}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import zio.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

/** Errors the handler can raise. Mapped to HTTP status codes in HttpApp. */
sealed trait CortexFailure extends Product with Serializable

object CortexFailure:
  case object NotFound                          extends CortexFailure
  final case class IO(detail: String)           extends CortexFailure
  final case class IndexInvalid(detail: String) extends CortexFailure

trait CortexHandler:
  def index: IO[CortexFailure, CortexIndex]
  def chapter(book: String, chapter: String): IO[CortexFailure, ChapterPayload]

object CortexHandler:

  /** Hard cap on directory nesting (sections beyond this are an error). */
  val MaxSectionDepth: Int = 6

  val live: ZLayer[AppConfig, Nothing, CortexHandler] =
    ZLayer.fromZIO(
      for
        cfg   <- ZIO.service[AppConfig]
        cache <- Ref.make(Option.empty[CachedState])
      yield CortexHandlerLive(cfg, cache)
    )

/**
 * Internal cached state: the API payload, plus per-book reverse maps from `chapterSlug → relativeMdPath`
 * (relative to the book directory), plus the mtime watermark used by auto-reload.
 */
final private[handlers] case class CachedState(
    index: CortexIndex,
    reverseMaps: Map[String, Map[String, String]],
    mtimeWatermark: Long
)

final private class CortexHandlerLive(
    cfg: AppConfig,
    cache: Ref[Option[CachedState]]
) extends CortexHandler:

  import CortexHandlerLive.*
  import CortexHandlerCodecs.given

  // ---- Path resolution + traversal guard ----------------------------------

  private val rootFile: File = File(cfg.cortex.root).getAbsoluteFile

  private val rootPath: Path =
    if rootFile.isDirectory then rootFile.toPath.toRealPath()
    else rootFile.toPath.toAbsolutePath.normalize

  private def safeUnder(rel: String): Either[CortexFailure, File] =
    val candidate = File(rootFile, rel)
    if !candidate.exists() || !candidate.isFile() then Left(CortexFailure.NotFound)
    else
      val real =
        try candidate.toPath.toRealPath()
        catch case _: Throwable => candidate.toPath.toAbsolutePath.normalize
      if real.startsWith(rootPath) then Right(candidate)
      else Left(CortexFailure.NotFound)

  // ---- Public API ---------------------------------------------------------

  override def index: IO[CortexFailure, CortexIndex] =
    cachedState.map(_.index)

  override def chapter(book: String, chapter: String): IO[CortexFailure, ChapterPayload] =
    if !slugLike(book) || !slugLike(chapter) then ZIO.fail(CortexFailure.NotFound)
    else
      for
        st <- cachedState
        bk <- ZIO
          .fromOption(st.index.books.find(_.slug == book))
          .orElseFail(CortexFailure.NotFound: CortexFailure)
        relPath <- ZIO
          .fromOption(st.reverseMaps.get(book).flatMap(_.get(chapter)))
          .orElseFail(CortexFailure.NotFound: CortexFailure)
        chIndex = bk.chapters.indexWhere(_.slug == chapter)
        ch      = bk.chapters(chIndex)
        raw <- readFileSafe(s"$book/$relPath")
        (frontmatter, body) = parseFrontmatter(raw, ch)
        prevSlug            = if chIndex > 0 then Some(bk.chapters(chIndex - 1).slug) else None
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

  // ---- Caching with optional mtime-based auto-reload ---------------------

  private def cachedState: IO[CortexFailure, CachedState] =
    cache.get.flatMap {
      case None => loadAndCache
      case Some(old) =>
        if !cfg.cortex.autoReload then ZIO.succeed(old)
        else
          currentMtime.flatMap { mt =>
            if mt > old.mtimeWatermark then loadAndCache
            else ZIO.succeed(old)
          }
    }

  private def loadAndCache: IO[CortexFailure, CachedState] =
    for
      mt <- currentMtime
      st <- buildState(mt)
      _  <- cache.set(Some(st))
    yield st

  /** Cheap mtime-watermark over the whole tree. Reads no file bodies. */
  private def currentMtime: IO[CortexFailure, Long] =
    ZIO
      .attemptBlocking {
        if !rootFile.isDirectory then 0L
        else
          val stream = Files.walk(rootPath)
          try
            stream
              .iterator()
              .asScala
              .filter(p => Files.isRegularFile(p))
              .map(p => Files.getLastModifiedTime(p).toMillis)
              .foldLeft(0L)(math.max)
          finally stream.close()
      }
      .mapError(t => CortexFailure.IO(t.getMessage))

  private def buildState(mt: Long): IO[CortexFailure, CachedState] =
    ZIO
      .attemptBlocking(walkIndex())
      .mapError(t => CortexFailure.IO(t.getMessage): CortexFailure)
      .flatMap(ZIO.fromEither(_))
      .map { case (idx, rev) => CachedState(idx, rev, mt) }

  // ---- Index build (filesystem-driven) ------------------------------------

  /**
   * Walk the cortex root, producing (CortexIndex, per-book reverse maps). Validation failures (depth
   * overflow, slug collision, bad slug) come back via the Either left.
   */
  private def walkIndex(): Either[CortexFailure, (CortexIndex, Map[String, Map[String, String]])] =
    if !rootFile.isDirectory then Right(CortexIndex(Nil) -> Map.empty)
    else
      val bookDirs = orderedChildren(rootFile).filter(_.isDirectory)
      // Skip dirs whose name isn't slug-like silently (lets you keep e.g.
      // `_drafts/` or `.git/` next to real books without failing the index).
      val results = bookDirs.flatMap { d =>
        val slug = d.getName
        if !slugLike(slug) then None
        else Some(buildBook(d, slug))
      }
      results.collectFirst { case Left(e) => e } match
        case Some(e) => Left(e)
        case None =>
          val books = results.collect { case Right((b, _)) => b }
          val maps  = results.collect { case Right((b, m)) => b.slug -> m }.toMap
          Right(CortexIndex(books) -> maps)

  private def buildBook(
      bookDir: File,
      slug: String
  ): Either[CortexFailure, (Book, Map[String, String])] =
    val bookMeta = readJson[BookMetaJson](File(bookDir, "book.json"))
    val acc      = scala.collection.mutable.ListBuffer.empty[(ChapterRef, String)]
    walkSection(bookDir, bookDir, Vector.empty, acc) match
      case Left(e) => Left(e)
      case Right(()) =>
        val chRefs = acc.iterator.map(_._1).toList
        val dups   = chRefs.groupBy(_.slug).filter(_._2.size > 1).keys.toList.sorted
        if dups.nonEmpty then
          Left(
            CortexFailure.IndexInvalid(s"$slug: duplicate chapter slugs: ${dups.mkString(", ")}")
          )
        else
          val rev         = acc.iterator.map { case (ch, p) => ch.slug -> p }.toMap
          val title       = bookMeta.flatMap(_.title).getOrElse(humanise(slug))
          val description = bookMeta.flatMap(_.description).getOrElse("")
          val tags        = bookMeta.flatMap(_.tags)
          val estMins     = bookMeta.flatMap(_.estimatedReadingMinutes)
          val book = Book(
            slug = slug,
            title = title,
            description = description,
            tags = tags,
            estimatedReadingMinutes = estMins,
            chapters = chRefs
          )
          Right((book, rev))

  /**
   * Recurse a section directory, accumulating chapters with their groupPath. Top-level chapters (groupPath
   * empty) are emitted first, then sub-sections are visited in numeric-prefix order — so the resulting
   * chapter list reads top-down through the tree.
   */
  private def walkSection(
      bookDir: File,
      dir: File,
      groupPath: Vector[String],
      acc: scala.collection.mutable.ListBuffer[(ChapterRef, String)]
  ): Either[CortexFailure, Unit] =
    if groupPath.length > CortexHandler.MaxSectionDepth then
      Left(
        CortexFailure.IndexInvalid(
          s"section nesting exceeds max depth ${CortexHandler.MaxSectionDepth} at " +
            relativise(bookDir, dir)
        )
      )
    else
      val children = orderedChildren(dir)
      val mdFiles = children.filter(c =>
        c.isFile && c.getName.endsWith(".md") && !c.getName.startsWith("_")
          && !c.getName.startsWith(".")
      )
      val sectionDirs = children.filter(c =>
        c.isDirectory && !c.getName.startsWith("_") && !c.getName.startsWith(".")
      )
      val groupOpt = if groupPath.isEmpty then None else Some(groupPath)
      val chErr = mdFiles.iterator
        .map { f =>
          val title = chapterTitle(f)
          val slug  = chapterSlugFromPath(bookDir, f)
          if !slugLike(slug) then
            Left(
              CortexFailure.IndexInvalid(
                s"chapter ${relativise(bookDir, f)} produces invalid slug '$slug'"
              )
            )
          else
            acc += ((ChapterRef(slug = slug, title = title, groupPath = groupOpt), relativise(bookDir, f)))
            Right(())
        }
        .collectFirst { case Left(e) => e }
      chErr match
        case Some(e) => Left(e)
        case None =>
          sectionDirs.foldLeft[Either[CortexFailure, Unit]](Right(())) {
            case (Left(e), _) => Left(e)
            case (Right(()), d) =>
              val secMeta  = readJson[SectionMetaJson](File(d, "_section.json"))
              val secTitle = secMeta.flatMap(_.title).getOrElse(humanise(d.getName))
              walkSection(bookDir, d, groupPath :+ secTitle, acc)
          }

  // ---- Frontmatter / title extraction ------------------------------------

  private def chapterTitle(f: File): String =
    val raw =
      scala.util.Try(new String(Files.readAllBytes(f.toPath), StandardCharsets.UTF_8)).getOrElse("")
    parseFrontmatterLines(raw)
      .get("title")
      .orElse(firstH1(stripFrontmatterText(raw)))
      .getOrElse(humanise(f.getName.stripSuffix(".md")))

  private def stripFrontmatterText(content: String): String =
    val lines = content.split("\\R", -1).toIndexedSeq
    if lines.headOption.contains("---") then
      val end = lines.indexOf("---", 1)
      if end > 0 then lines.drop(end + 1).mkString("\n") else content
    else content

  private def parseFrontmatterLines(content: String): Map[String, String] =
    val lines = content.split("\\R", -1).toIndexedSeq
    if !lines.headOption.contains("---") then Map.empty
    else
      val end = lines.indexOf("---", 1)
      if end <= 0 then Map.empty
      else lines.slice(1, end).flatMap(parseFrontmatterLine).toMap

  private def firstH1(body: String): Option[String] =
    body.linesIterator
      .collectFirst { case l if l.startsWith("# ") => l.stripPrefix("# ").trim }
      .filter(_.nonEmpty)

  private def parseFrontmatter(
      content: String,
      ch: ChapterRef
  ): (ChapterFrontmatter, String) =
    val lines = content.split("\\R", -1).toIndexedSeq
    if lines.headOption.contains("---") then
      val end = lines.indexOf("---", 1)
      if end > 0 then
        val fmLines = lines.slice(1, end).toList
        val rest    = lines.drop(end + 1).mkString("\n")
        val map     = fmLines.flatMap(parseFrontmatterLine).toMap
        val fm = ChapterFrontmatter(
          title = map.getOrElse("title", ch.title),
          summary = map.get("summary")
        )
        (fm, rest)
      else (ChapterFrontmatter(title = ch.title, summary = None), content)
    else (ChapterFrontmatter(title = ch.title, summary = None), content)

  private def parseFrontmatterLine(line: String): Option[(String, String)] =
    val idx = line.indexOf(':')
    if idx <= 0 then None
    else
      val key      = line.substring(0, idx).trim
      val rawValue = line.substring(idx + 1).trim
      val value    = stripQuotes(rawValue)
      if value.nonEmpty then Some(key -> value) else None

  private def stripQuotes(s: String): String =
    if (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")) then
      if s.length >= 2 then s.substring(1, s.length - 1) else s
    else s

  // ---- JSON readers ------------------------------------------------------

  private def readJson[A: Decoder](f: File): Option[A] =
    if !f.isFile then None
    else
      val raw = scala.util
        .Try(new String(Files.readAllBytes(f.toPath), StandardCharsets.UTF_8))
        .toOption
      raw.flatMap(decode[A](_).toOption)

  // ---- Filesystem read helper -------------------------------------------

  private def readFileSafe(rel: String): IO[CortexFailure, String] =
    safeUnder(rel) match
      case Left(e) => ZIO.fail(e)
      case Right(file) =>
        ZIO
          .attemptBlocking {
            new String(Files.readAllBytes(file.toPath), StandardCharsets.UTF_8)
          }
          .mapError(t => CortexFailure.IO(t.getMessage))

private[handlers] object CortexHandlerLive:

  /**
   * Reject anything that isn't a simple slug. Belt-and-braces with the realpath check.
   */
  def slugLike(s: String): Boolean =
    s.nonEmpty && s.forall(c => c.isLetterOrDigit || c == '-' || c == '_')

  /** Strip a leading numeric ordering prefix: `01-foo` → `foo`, `1.bar` → `bar`. */
  def stripOrderPrefix(name: String): String =
    val m = "^\\d+[._-]?".r.findPrefixOf(name).getOrElse("")
    name.drop(m.length)

  /**
   * Humanise a slug-like string into a display title: `singly-linked-list` → `Singly Linked List`
   * `01-binary-search-tree` → `Binary Search Tree`
   */
  def humanise(name: String): String =
    val cleaned = stripOrderPrefix(name).stripSuffix(".md")
    cleaned
      .split("[-_.]")
      .iterator
      .filter(_.nonEmpty)
      .map(w => w.head.toUpper.toString + w.tail.toLowerCase)
      .mkString(" ")

  /**
   * Build a chapter slug from its filesystem path within the book. data-structures/01-arrays/02-traversal.md
   * → data-structures-arrays-traversal
   */
  def chapterSlugFromPath(bookDir: File, file: File): String =
    val rel = relativise(bookDir, file).stripSuffix(".md")
    rel
      .split('/')
      .iterator
      .filter(_.nonEmpty)
      .map(seg => slugify(stripOrderPrefix(seg)))
      .filter(_.nonEmpty)
      .mkString("-")

  /**
   * Slugify a single path segment. Letters/digits kept lowercase, underscore preserved, everything else
   * collapsed to a single hyphen. Leading/trailing hyphens trimmed.
   */
  def slugify(seg: String): String =
    val sb       = new StringBuilder
    var lastDash = false
    seg.foreach { c =>
      if c.isLetterOrDigit then
        sb.append(c.toLower)
        lastDash = false
      else if c == '_' then
        sb.append('_')
        lastDash = false
      else if !lastDash && sb.nonEmpty then
        sb.append('-')
        lastDash = true
    }
    val s = sb.toString
    if s.endsWith("-") then s.dropRight(1) else s

  def relativise(bookDir: File, f: File): String =
    val basePath = bookDir.toPath.toAbsolutePath.normalize
    val target   = f.toPath.toAbsolutePath.normalize
    basePath.relativize(target).toString.replace(File.separatorChar, '/')

  def orderedChildren(dir: File): List[File] =
    val arr = Option(dir.listFiles()).getOrElse(Array.empty[File])
    arr.sortBy(c => (extractOrder(c.getName), c.getName.toLowerCase)).toList

  def extractOrder(name: String): Int =
    "^(\\d+)".r.findPrefixMatchOf(name).map(_.group(1).toInt).getOrElse(Int.MaxValue)

// ---- JSON shapes for the optional on-disk meta files ---------------------

final private[handlers] case class BookMetaJson(
    title: Option[String],
    description: Option[String],
    tags: Option[Seq[String]],
    estimatedReadingMinutes: Option[Int]
)

final private[handlers] case class SectionMetaJson(
    title: Option[String],
    summary: Option[String]
)

private[handlers] object CortexHandlerCodecs:
  given Decoder[BookMetaJson]    = deriveDecoder
  given Decoder[SectionMetaJson] = deriveDecoder
