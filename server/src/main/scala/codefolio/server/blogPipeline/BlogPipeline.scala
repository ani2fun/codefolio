package codefolio.server.blogPipeline

import codefolio.server.config.BlogConfig
import codefolio.shared.api.Endpoints.{BlogIndex, BlogPostFrontmatter, BlogPostPayload, BlogSummary}
import zio.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.time.LocalDate
import scala.jdk.CollectionConverters.*

/** Errors raised while reading or indexing blog content. Mapped to HTTP status codes in `ApiErrors`. */
sealed trait BlogFailure extends Product with Serializable

object BlogFailure:
  case object NotFound                          extends BlogFailure
  final case class IO(detail: String)           extends BlogFailure
  final case class IndexInvalid(detail: String) extends BlogFailure

/**
 * In-memory snapshot of the blog index. Bodies aren't cached — the post handler re-reads on demand via
 * `readPostSafe`, mirroring the Cortex pattern (cache the index, not chapter bodies). `slugToFile` is the
 * lookup the post handler uses to resolve a slug back to its filename.
 */
final private[blogPipeline] case class BlogState(
    index: BlogIndex,
    slugToFile: Map[String, String],
    slugToFrontmatter: Map[String, BlogPostFrontmatter],
    mtimeWatermark: Long
)

/**
 * Filesystem accessor for the blog content tree. Internal seam — package-private; `LiveBlogFs` is built by
 * `BlogPipeline.live`, fakes by tests.
 *
 *   - `currentMtime` is a cheap watermark over the whole tree (no file bodies read).
 *   - `readPostSafe` reads one post's body, rejecting any path that escapes the blog root.
 *   - `loadPosts` scans every `*.md` under the root and returns `(slug, raw)` pairs.
 */
private[blogPipeline] trait BlogFs:
  def currentMtime: IO[BlogFailure, Long]
  def readPostSafe(slug: String): IO[BlogFailure, String]
  def loadPosts: IO[BlogFailure, List[(String, String)]]

/**
 * Cached, deep facade over the blog content tree.
 *
 *   - `index` returns the global `BlogIndex`, sorted descending by `publishedAt` (newest first).
 *   - `post(slug)` resolves the file path, reads the body, parses frontmatter, computes prev/next slugs, and
 *     assembles a `BlogPostPayload` in one pass.
 *
 * Mirrors the ADR-0003 internal-seams pattern from Cortex: the [[BlogFs]] accessor trait is package-private;
 * the only public surface is `index` + `post`. Lenient parsing per ADR-0001 — malformed or missing
 * frontmatter fields fall through to humanised-slug defaults.
 *
 * Prev/next semantics: posts are sorted descending by `publishedAt`, so for a reader on post `i`, `prevSlug =
 * posts(i + 1).slug` (the older post) and `nextSlug = posts(i - 1).slug` (the newer post). Reads natural in
 * chronological order: prev = "what was posted before this", next = "what came next".
 */
trait BlogPipeline:
  def index: IO[BlogFailure, BlogIndex]
  def post(slug: String): IO[BlogFailure, BlogPostPayload]

object BlogPipeline:

  /** Direct construction from an explicit [[BlogFs]] accessor + cache reset. Used by tests. */
  def from(blogFs: BlogFs, autoReload: Boolean): UIO[BlogPipeline] =
    Ref.make(Option.empty[BlogState]).map(cache => BlogPipelineLive(blogFs, autoReload, cache))

  /** Resource-free layer: builds a filesystem-backed [[BlogFs]] and an empty cache. */
  val live: ZLayer[BlogConfig, Nothing, BlogPipeline] =
    ZLayer.fromZIO {
      for
        cfg   <- ZIO.service[BlogConfig]
        cache <- Ref.make(Option.empty[BlogState])
      yield BlogPipelineLive(LiveBlogFs(cfg.root), cfg.autoReload, cache)
    }

  // ===========================================================================
  // FS adapter — bytes ↔ values plus path-traversal containment.
  // ===========================================================================

  final private class LiveBlogFs(root: String) extends BlogFs:

    private val rootFile: File = File(root).getAbsoluteFile

    private val rootPath: Path =
      if rootFile.isDirectory then rootFile.toPath.toRealPath()
      else rootFile.toPath.toAbsolutePath.normalize

    override def currentMtime: IO[BlogFailure, Long] =
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
        .mapError(t => BlogFailure.IO(t.getMessage))

    override def readPostSafe(slug: String): IO[BlogFailure, String] =
      if !slugLike(slug) then ZIO.fail(BlogFailure.NotFound)
      else
        safeUnder(s"$slug.md") match
          case Left(e) => ZIO.fail(e)
          case Right(file) =>
            ZIO
              .attemptBlocking {
                new String(Files.readAllBytes(file.toPath), StandardCharsets.UTF_8)
              }
              .mapError(t => BlogFailure.IO(t.getMessage))

    override def loadPosts: IO[BlogFailure, List[(String, String)]] =
      ZIO
        .attemptBlocking {
          if !rootFile.isDirectory then List.empty[(String, String)]
          else
            val files = Option(rootFile.listFiles()).fold(List.empty[File])(_.toList)
            files
              .filter(f => f.isFile && f.getName.endsWith(".md") && !f.getName.startsWith("_"))
              .map { f =>
                val slug = f.getName.stripSuffix(".md")
                val raw = scala.util
                  .Try(new String(Files.readAllBytes(f.toPath), StandardCharsets.UTF_8))
                  .getOrElse("")
                slug -> raw
              }
        }
        .mapError(t => BlogFailure.IO(t.getMessage))

    // Path-traversal defense: resolve the candidate to its real on-disk path and reject
    // anything that escapes `rootPath`. Catches `..` segments, absolute-path inputs, and
    // symlinks pointing outside the blog root.
    private def safeUnder(rel: String): Either[BlogFailure, File] =
      val candidate = File(rootFile, rel)
      if !candidate.exists() || !candidate.isFile() then Left(BlogFailure.NotFound)
      else
        val real =
          try candidate.toPath.toRealPath()
          catch case _: Throwable => candidate.toPath.toAbsolutePath.normalize
        if real.startsWith(rootPath) then Right(candidate)
        else Left(BlogFailure.NotFound)

  /** Reject anything that isn't a simple slug — letters, digits, hyphens, underscores; non-empty. */
  private[blogPipeline] def slugLike(s: String): Boolean =
    s.nonEmpty && s.forall(c => c.isLetterOrDigit || c == '-' || c == '_')

final private class BlogPipelineLive(
    blogFs: BlogFs,
    autoReload: Boolean,
    cache: Ref[Option[BlogState]]
) extends BlogPipeline:

  override def index: IO[BlogFailure, BlogIndex] =
    cachedState.map(_.index)

  override def post(slug: String): IO[BlogFailure, BlogPostPayload] =
    if !BlogPipeline.slugLike(slug) then ZIO.fail(BlogFailure.NotFound)
    else
      for
        state <- cachedState
        idx = state.index.posts.indexWhere(_.slug == slug)
        _ <- ZIO.unless(idx >= 0)(ZIO.fail(BlogFailure.NotFound: BlogFailure))
        summary = state.index.posts(idx)
        fm <- ZIO
          .fromOption(state.slugToFrontmatter.get(slug))
          .orElseFail(BlogFailure.NotFound: BlogFailure)
        raw <- blogFs.readPostSafe(slug)
        body     = BlogFrontmatter.stripFrontmatter(raw)
        prevSlug = if idx + 1 < state.index.posts.length then Some(state.index.posts(idx + 1).slug) else None
        nextSlug = if idx > 0 then Some(state.index.posts(idx - 1).slug) else None
      yield BlogPostPayload(
        post = summary,
        frontmatter = fm,
        raw = body,
        prevSlug = prevSlug,
        nextSlug = nextSlug
      )

  private def cachedState: IO[BlogFailure, BlogState] =
    cache.get.flatMap {
      case None => loadAndCache
      case Some(old) =>
        if !autoReload then ZIO.succeed(old)
        else
          blogFs.currentMtime.flatMap { mt =>
            if mt > old.mtimeWatermark then loadAndCache
            else ZIO.succeed(old)
          }
    }

  private def loadAndCache: IO[BlogFailure, BlogState] =
    for
      mt    <- blogFs.currentMtime
      posts <- blogFs.loadPosts
      parsed = posts.map { case (slug, raw) => slug -> BlogFrontmatter.parse(raw, fallbackSlug = slug) }
      summaries = parsed.map { case (slug, p) =>
        BlogSummary(
          slug = slug,
          title = p.frontmatter.title,
          summary = p.frontmatter.summary.getOrElse(""),
          publishedAt = p.frontmatter.publishedAt,
          tags = p.frontmatter.tags,
          readMinutes = p.frontmatter.readMinutes,
          eyebrow = p.frontmatter.eyebrow
        )
      }
      // Sort descending by `publishedAt` (newest first). Malformed or missing dates sink
      // to the bottom — lenient per ADR-0001, no IndexInvalid raised for bad date strings.
      sorted            = summaries.sortBy(s => parseDate(s.publishedAt))(Ordering[LocalDate].reverse)
      slugToFile        = sorted.map(s => s.slug -> s"${s.slug}.md").toMap
      slugToFrontmatter = parsed.map { case (slug, p) => slug -> p.frontmatter }.toMap
      state             = BlogState(BlogIndex(sorted), slugToFile, slugToFrontmatter, mt)
      _ <- cache.set(Some(state))
    yield state

  // Sentinel for unparseable dates — pushes the post to the bottom of the descending sort.
  private val DateFloor: LocalDate = LocalDate.of(1, 1, 1)

  private def parseDate(s: String): LocalDate =
    scala.util.Try(LocalDate.parse(s)).getOrElse(DateFloor)
