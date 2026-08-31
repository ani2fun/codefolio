package codefolio.client.components.sections

import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import codefolio.client.components.ui.Section
import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/**
 * Projects — filterable card grid with inline ASCII architecture diagrams.
 *
 *   - Filter chips at the top: All · Infra · OSS · Backend.
 *   - Every project marked `featured: true` in the JSON gets a 2-col span; today that's the homelab and
 *     Synapse. The grid is `grid-auto-flow: dense`, so a narrow card backfills the column a wide one leaves
 *     over.
 *   - Cards whose project has a diagram (see `asciiFor`) render it as an inline `<pre>` rather than a hero
 *     image — substance over stock photography.
 *   - The rest: a real photo when the project has its own (`gradle-plugin`), otherwise a tinted-stripe
 *     placeholder showing the `metadata` mono caption.
 */
object Projects:

  private val filters: List[String] = List("All", "Infra", "OSS", "Backend")

  /**
   * A pre-block + status-pill pair for one project card. Different cards get different diagrams; the badge
   * text travels with the diagram so each project's frame reads as its own thing rather than wearing homelab
   * chrome.
   */
  final private case class AsciiPanel(badge: String, art: String)

  /**
   * Inline ASCII topology for the homelab K3s card. Mirrors the actual cluster: a Contabo edge node that
   * terminates all public traffic, joined over WireGuard to three home machines running the control plane and
   * the workloads.
   */
  private val k3sAscii: String =
    """                     internet
      |                        │
      |                   ┌─────────┐
      |                   │   DNS   │   kakde.eu · Cloudflare
      |                   └────┬────┘
      |                        │  80 / 443 only
      |                        ▼
      |              ┌─────────────────┐
      |              │   ctb-edge-1    │   cloud VPS — the only
      |              │     Traefik     │   public entrypoint
      |              │  cert-manager   │   TLS via DNS-01
      |              └────────┬────────┘
      |                       │  WireGuard mesh
      |                       │  172.27.15.0/24
      |                       ▼
      |┌────────────────────────  home LAN · k3s + Calico ─────┐
      |│                          192.168.15.0/24              │
      |│   ┌──────────┐                                        │
      |│   │   ms-1   │   k3s server (control plane)           │
      |│   └──────────┘                                        │
      |│      ▲                                                │
      |│      │ k3s api                                        │
      |│      ▼                                                │
      |│   ┌──────────┐         ┌──────────┐                   │
      |│   │   wk-1   │         │   wk-2   │   workers         │
      |│   └──────────┘         └──────────┘                   │
      |│    postgres             argo cd                       │
      |│    keycloak             gitops sync                   │
      |│                                                       │
      |└───────────────────────────────────────────────────────┘""".stripMargin

  /**
   * Runtime layout for Synapse. It is one deployable, not a split frontend and backend: axum is the only
   * listener, and the Astro SSR process sits beside it on loopback, reachable only through axum's fallback so
   * `/api` and `/media` always win. Rendering a page is therefore a loop — axum hands the request down, Astro
   * fetches back up for data. The visualiser is a WebAssembly bundle the browser loads lazily; the d2
   * renderer and the content sync are further containers in the same pod.
   */
  private val synapseAscii: String =
    """                      browser
      |       page HTML · islands · viz-wasm (lazy wasm)
      |                         │
      |                         │  https — every request
      |                         ▼
      | ┌──── one pod ────────────────────────────────────┐
      | │  ┌───────────────────────────────────────────┐  │
      | │  │  axum · tokio · Rust   the only listener  │  │
      | │  │  /api · /media · headers · compression    │  │
      | │  └───┬──────────────────────────▲────────────┘  │
      | │      │ pages (fallback)         │ SSR fetch     │
      | │      ▼                          │ 127.0.0.1     │
      | │  ┌──────────────────────┐       │               │
      | │  │  Astro 7 SSR · node  │       │               │
      | │  │  loopback :4321      ├───────┘               │
      | │  └──────────┬───────────┘                       │
      | │             │ d2 fences                         │
      | │             ▼                                   │
      | │  ┌──────────────────┐  ┌──────────────────────┐ │
      | │  │  d2-render · Go  │  │  git-sync → content  │ │
      | │  └──────────────────┘  └──────────────────────┘ │
      | └──────┬───────────────┬──────────────────┬───────┘
      |        ▼               ▼                  ▼
      |  ┌──────────┐    ┌──────────┐      ┌──────────┐
      |  │ go-judge │    │ Postgres │      │ Keycloak │
      |  │  sandbox │    │   sqlx   │      │   OIDC   │
      |  └──────────┘    └──────────┘      └──────────┘""".stripMargin

  /**
   * Layout for this site: a Scala.js SPA bundled with Vite, served as plain assets by a trivial zio-http edge
   * (just the `assets` tree plus an `/api/health` check) on the homelab K3s cluster. There are no backing
   * stores — the interactive knowledge base is Synapse, a separate application.
   */
  private val portfolioAscii: String =
    """             browser
      |                │
      |                ▼
      |       ┌──────────────────┐
      |       │   Scala.js SPA   │
      |       │  scalajs-react   │
      |       │   Tailwind v4    │
      |       └────────┬─────────┘
      |                │  vite build
      |                ▼
      |       ┌──────────────────┐
      |       │   zio-http edge  │
      |       │  serves /assets  │
      |       │  + /api/health   │
      |       └────────┬─────────┘
      |                │  container
      |                ▼
      |       ┌──────────────────┐
      |       │   K3s homelab    │
      |       └──────────────────┘""".stripMargin

  /**
   * Publish flow for the Sonatype Maven Central Publisher Gradle plugin: user's build → plugin assembles +
   * PGP-signs the bundle → POSTs it to the new Central Portal API → stages and releases to Maven Central.
   */
  private val sonatypeAscii: String =
    """      your gradle build
      |              │
      |              ▼
      |      ┌────────────────┐
      |      │   plugin DSL   │   sonatypeCentral
      |      │    (Kotlin)    │   PublishExtension
      |      └────────┬───────┘
      |               │  publish
      |               ▼
      |      ┌────────────────┐
      |      │  bundle + sign │   pom · jar
      |      │   (PGP / GPG)  │   sources · javadoc
      |      └────────┬───────┘
      |               │  POST /api/v1/upload
      |               ▼
      |      ┌────────────────┐
      |      │    Sonatype    │   validate
      |      │ Central Portal │   → stage → release
      |      └────────┬───────┘
      |               │
      |               ▼
      |      ┌────────────────┐
      |      │  Maven Central │   search.maven.org
      |      └────────────────┘""".stripMargin

  /**
   * Lookup the diagram for a project by name. Anything unmatched falls back to the photo / placeholder branch
   * in `renderCard`.
   */
  private def asciiFor(p: PortfolioData.Project): Option[AsciiPanel] =
    p.name match
      case "Self-hosted homelab on K3s" =>
        Some(AsciiPanel("live · 4 nodes · k3s", k3sAscii))
      case "Sonatype Maven Central Publisher" =>
        Some(AsciiPanel("live · plugin portal", sonatypeAscii))
      case "Synapse" =>
        Some(AsciiPanel("live · rust · astro · wasm", synapseAscii))
      case "Portfolio App" =>
        Some(AsciiPanel("live · static · scala.js", portfolioAscii))
      case _ => None

  private def iconLink(href: String, ariaLabel: String, icon: VdomNode): VdomNode =
    <.a(
      ^.href       := href,
      ^.rel        := "noopener noreferrer",
      ^.target     := "_blank",
      ^.aria.label := ariaLabel,
      ^.className  := "projects__icon-link",
      icon
    )

  /**
   * Decide which projects show up under the active filter. "All" passes everything; otherwise the project's
   * `category` field has to match (or the project is hidden).
   */
  private def matchesFilter(p: PortfolioData.Project, active: String): Boolean =
    if active == "All" then true
    else p.category.toOption.contains(active)

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState("All")
      .render { (_, activeS) =>
        val active        = activeS.value
        val liveCount     = PortfolioData.projects.count(!_.archived.getOrElse(false))
        val archivedCount = PortfolioData.projects.length - liveCount
        val visible       = PortfolioData.projects.toList.filter(matchesFilter(_, active))
        // Only explicitly-featured projects get a wide 2-column slot, and there may be more than
        // one (the homelab and Synapse both claim it). Selecting by position instead — "whichever
        // sorts first" — would drop e.g. the Sonatype plugin into the wide slot under an "OSS"
        // filter, which the layout doesn't intend. ASCII-art selection is per-project (see
        // `asciiFor`) and independent of this.
        val featured = visible.filter(_.featured.getOrElse(false))
        val rest     = visible.filterNot(_.featured.getOrElse(false))

        Section("projects", "projects")(
          <.div(
            ^.className := "projects__inner",
            <.div(
              ^.className := "projects__heading-row",
              <.div(
                ^.className := "projects__heading",
                <.div(
                  ^.className := "projects__eyebrow",
                  if archivedCount > 0 then s"SIDE PROJECTS · $liveCount LIVE · $archivedCount ARCHIVED"
                  else s"SIDE PROJECTS · $liveCount LIVE"
                ),
                <.h2(
                  ^.className := "projects__title",
                  "Things I made",
                  <.br,
                  "on weekends."
                )
              ),
              <.p(
                ^.className := "projects__intro",
                "Self-hosted on the homelab below — the meta-loop is part of the point."
              )
            ),
            <.div(
              ^.className := "projects__filters",
              filters.toTagMod { f =>
                val cls = if active == f then "projects__filter projects__filter--active"
                else "projects__filter"
                <.button(
                  ^.key       := f,
                  ^.className := cls,
                  ^.onClick --> activeS.setState(f),
                  f
                )
              }
            ),
            <.div(
              ^.className := "projects__grid",
              featured.toTagMod { p =>
                renderCard(p, featuredCard = true, asciiArt = asciiFor(p))
              },
              rest.toTagMod(p => renderCard(p, featuredCard = false, asciiArt = asciiFor(p)))
            )
          )
        )
      }

  /**
   * True iff the project's image points at a real per-project asset that we want to keep showing — anything
   * else (the duplicated macbook.webp) gets replaced by a tinted-stripe placeholder.
   */
  private def hasRealPhoto(p: PortfolioData.Project): Boolean =
    val url = Option(p.image).map(_.url).getOrElse("")
    url.nonEmpty && !url.contains("macbook.webp") && !url.endsWith("portfolio-webapp.webp")

  private def renderCard(
      p: PortfolioData.Project,
      featuredCard: Boolean,
      asciiArt: Option[AsciiPanel]
  ): VdomNode =
    val isGithubOnly = p.projectUrl == p.githubUrl
    val cardCls =
      if featuredCard then "projects__card projects__card--featured"
      else "projects__card"

    <.article(
      ^.key       := p.name,
      ^.className := cardCls,
      asciiArt match
        case Some(panel) =>
          <.div(
            ^.className := "projects__ascii-frame",
            <.span(^.className := "projects__ascii-badge", panel.badge),
            <.pre(^.className  := "projects__ascii", panel.art)
          )
        case None if hasRealPhoto(p) =>
          <.img(
            ^.src               := p.image.url,
            ^.alt               := p.image.alt,
            VdomAttr("width")   := "400",
            VdomAttr("height")  := "240",
            VdomAttr("loading") := "lazy",
            ^.className         := "projects__image"
          )
        case None =>
          <.div(
            ^.className := "projects__placeholder",
            <.span(
              ^.className := "projects__placeholder-meta",
              p.metadata.toOption.getOrElse(p.tags.toList.take(3).mkString(" · ").toUpperCase)
            )
          )
      ,
      <.div(
        ^.className := "projects__body",
        <.div(
          ^.className := "projects__icon-row",
          if isGithubOnly then EmptyVdom
          else
            iconLink(
              p.projectUrl,
              s"Open ${p.name}",
              LucideIcons.ExternalLink(LucideIcons.withClass("projects__icon-svg"))
            )
          ,
          iconLink(
            p.githubUrl,
            s"${p.name} on GitHub",
            BrandIcons.Github("projects__icon-svg")
          )
        ),
        <.h3(^.className := "projects__name", p.name),
        <.p(^.className  := "projects__description", p.description),
        <.div(
          ^.className := "projects__tag-row",
          p.tags.toList.toTagMod { tag =>
            <.span(^.key := tag, ^.className := "projects__tag", tag)
          }
        )
      )
    )
