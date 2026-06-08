package codefolio.shared

/** Shared route and public-asset path segments used by the SPA router and the production static fallback. */
object AppRoutes:
  val Root         = "/"
  val IndexHtml    = "index.html"
  val Assets       = "assets"
  val Images       = "img"
  val Certificates = "certificates"
  val CvFile       = "Aniket-Kakde-CV-EN.pdf"

  /** One reserved top-level SPA route beyond `/`. The portfolio is a single-page site, so there are none. */
  final case class SpaRoute(segment: String, hasNestedRoutes: Boolean)

  /**
   * The portfolio's only route is `/` (which is `index.html` itself), so there are no extra top-level SPA
   * fallback routes for the production static server to serve. See ADR-0009.
   */
  val SpaRoutes: List[SpaRoute] = Nil
