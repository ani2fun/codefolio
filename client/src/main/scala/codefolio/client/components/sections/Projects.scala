package codefolio.client.components.sections

import codefolio.client.components.icons.{BrandIcons, LucideIcons}
import codefolio.client.components.ui.Section
import codefolio.client.data.PortfolioData
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/**
 * Projects — featured K3s card with ASCII topology + filterable card grid.
 *
 *   - Filter chips at the top: All · Infra · OSS · Backend.
 *   - First entry (where `featured: true` or first in JSON) gets a 2-col span and renders the K3s topology as
 *     an inline `<pre>` instead of a hero image. Replaces the duplicated macbook.webp reuse — substance over
 *     stock photography.
 *   - Other cards: real photo when present (`food-ordering-system`, `gradle-plugin`); otherwise a
 *     tinted-stripe placeholder showing the `metadata` mono caption.
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
   * Inline ASCII topology for the homelab K3s card. Mirrors the actual 4-node cluster: one control-plane +
   * three workers, all on a single flat home network behind a Pi-hosted ingress.
   */
  private val k3sAscii: String =
    """                     internet
      |                        │
      |                   ┌─────────┐
      |                   │   DNS   │   example.com
      |                   └────┬────┘
      |                        │  80 / 443
      |                        ▼
      |              ┌─────────────────┐
      |              │    cloud-vm     │   cloud VPS
      |              │     Traefik     │   edge worker
      |              └────────┬────────┘
      |                       │  WireGuard mesh
      |                       │  172.27.15.0/24
      |                       ▼
      |┌────────────── home LAN  192.168.15.0/24 ─────────────┐
      |│                                                      │
      |│   ┌──────────┐                                       │
      |│   │ server-1 │   k3s server (control plane)          │
      |│   └──────────┘                                       │
      |│      ▲                                               │
      |│      │ k3s api                                       │
      |│      ▼                                               │
      |│   ┌──────────┐         ┌──────────┐                  │
      |│   │ worker-1 │         │ worker-2 │  workers         │
      |│   └──────────┘         └──────────┘                  │
      |│    postgres             argo cd                      │
      |│                                                      │
      |└──────────────────────────────────────────────────────┘""".stripMargin

  /**
   * Layout for Synapse, the ground-up all-Scala rebuild of Cortex: a React-free Scala.js + Laminar SPA (with
   * a Scala visualisation engine) calling a ZIO 2 + tapir + zio-http API, serving markdown books that live in
   * their own `synapse-content` repo.
   */
  private val synapseAscii: String =
    """             browser
      |                │
      |                ▼
      |       ┌──────────────────┐
      |       │     Scala.js     │
      |       │     Laminar      │
      |       │  viz engine · md │
      |       └────────┬─────────┘
      |                │  /api/*
      |                ▼
      |       ┌──────────────────┐
      |       │     zio-http     │
      |       │   ZIO 2 · tapir  │
      |       │  OpenAPI codegen │
      |       └────────┬─────────┘
      |                │
      |                ▼
      |       ┌──────────────────┐
      |       │ synapse-content  │
      |       │  markdown books  │
      |       └──────────────────┘""".stripMargin

  /**
   * Static-portfolio layout for this site after the codefolio/cortex split: a Scala.js SPA bundled with Vite,
   * served as plain assets by a trivial zio-http edge (just the `assets` tree plus an `/api/health` check) on
   * the homelab K3s cluster. No stores — the interactive knowledge base now lives in Synapse.
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
        Some(AsciiPanel("live · scala 3 · laminar", synapseAscii))
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
        // Only an explicitly-featured project gets the wide 2-column slot. Falling back to
        // `headOption` would put whichever project is first in the filtered list (e.g. the
        // Sonatype plugin under an "OSS" filter) into the wide slot, which the layout doesn't
        // intend. ASCII-art selection is per-project (see `asciiFor`) and independent of this.
        val featured = visible.find(_.featured.getOrElse(false))
        val rest     = visible.filterNot(p => featured.contains(p))

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
