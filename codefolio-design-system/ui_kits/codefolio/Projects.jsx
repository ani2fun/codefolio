/* Projects.jsx — featured K3s card with ASCII topology + filterable card grid.
   Filter order matches live site: All · Infra · OSS · Backend.
   Title: "Things I made / on weekends." Three projects with their own ASCII
   diagrams. */

const K3S_ASCII = `                     internet
                        │
                   ┌─────────┐
                   │   DNS   │   example.com
                   └────┬────┘
                        │  80 / 443
                        ▼
              ┌─────────────────┐
              │    cloud-vm     │   cloud VPS
              │     Traefik     │   edge worker
              └────────┬────────┘
                       │  WireGuard mesh
                       │  172.27.15.0/24
                       ▼
┌────────────── home LAN  192.168.15.0/24 ─────────────┐
│                                                      │
│   ┌──────────┐                                       │
│   │ server-1 │   k3s server (control plane)          │
│   └──────────┘                                       │
│      ▲                                               │
│      │ k3s api                                       │
│      ▼                                               │
│   ┌──────────┐         ┌──────────┐                  │
│   │ worker-1 │         │ worker-2 │  workers         │
│   └──────────┘         └──────────┘                  │
│    postgres             argo cd                      │
│                                                      │
└──────────────────────────────────────────────────────┘`;

const CODEFOLIO_ASCII = `             browser
                │
                ▼
       ┌──────────────────┐
       │     Scala.js     │
       │  scalajs-react   │
       │   Tailwind v4    │
       └────────┬─────────┘
                │  /api/*
                ▼
       ┌──────────────────┐
       │     zio-http     │
       │   ZIO 2 · tapir  │
       │  OpenAPI codegen │
       └──┬──────┬──────┬─┘
          │      │      │
          ▼      ▼      ▼
       ┌────┐┌─────┐┌─────┐
       │ PG ││Redis││Mongo│
       └────┘└─────┘└─────┘
       counter cache events`;

const SONATYPE_ASCII = `      your gradle build
              │
              ▼
      ┌────────────────┐
      │   plugin DSL   │   sonatypeCentral
      │    (Kotlin)    │   PublishExtension
      └────────┬───────┘
               │  publish
               ▼
      ┌────────────────┐
      │  bundle + sign │   pom · jar
      │   (PGP / GPG)  │   sources · javadoc
      └────────┬───────┘
               │  POST /api/v1/upload
               ▼
      ┌────────────────┐
      │    Sonatype    │   validate
      │ Central Portal │   → stage → release
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │  Maven Central │   search.maven.org
      └────────────────┘`;

const PROJECTS = [
  {
    name: "Self-hosted homelab on K3s", category: "Infra", featured: true,
    badge: "live · 4 nodes · k3s",
    metadata: "K3S · 2023 · LINUX · GO",
    description: "Four-node K3s cluster on commodity hardware sitting at home. Hands-on with the full delivery path — packaging, deployment, networking, observability — for the services I actually use, including this site.",
    tags: ["Kubernetes", "K3s", "Linux", "Networking", "Observability", "Self-hosted"],
    icons: ["github"], ascii: K3S_ASCII,
  },
  {
    name: "Codefolio App", category: "Backend",
    badge: "live · scala 3 · 3 stores",
    metadata: "SCALA.JS · 2024 · ZIO + TAILWIND",
    description: "You're looking at it. kakde.eu runs on this — Scala 3 end-to-end, Scala.js + scalajs-react on the frontend, ZIO 2 + zio-http on the backend over Postgres, Redis, and Mongo. Packaged as a container and deployed to my homelab cluster.",
    tags: ["Scala 3", "Scala.js", "ZIO", "Tailwind", "Self-hosted"],
    icons: ["external-link", "github"], ascii: CODEFOLIO_ASCII,
  },
  {
    name: "Sonatype Maven Central Publisher", category: "OSS",
    badge: "live · plugin portal",
    metadata: "GRADLE PLUGIN · 2023 · KOTLIN",
    description: "Open-source Gradle plugin published on the official Plugin Portal. Publishes JVM artefacts to Maven Central through Sonatype's Central Portal API.",
    tags: ["Kotlin", "Gradle", "Maven Central", "Open source"],
    icons: ["external-link", "github"], ascii: SONATYPE_ASCII,
  },
];

const FILTERS = ["All", "Infra", "OSS", "Backend"];

const Projects = () => {
  const [filter, setFilter] = React.useState("All");
  const visible = PROJECTS.filter((p) => filter === "All" || p.category === filter);
  return (
    <section id="projects" className="section section--projects">
      <div className="section__inner">
        <div className="projects__heading-row">
          <div>
            <p className="eyebrow">Side projects · {PROJECTS.length} live</p>
            <h2 className="section-title" style={{margin: 0}}>Things I made<br />on weekends.</h2>
          </div>
          <p className="projects__intro">
            Self-hosted on the homelab below — the meta-loop is part of the point.
          </p>
        </div>
        <div className="projects__filters">
          {FILTERS.map((f) => (
            <button key={f}
              className={"projects__filter" + (filter === f ? " projects__filter--active" : "")}
              onClick={() => setFilter(f)}>
              {f}
            </button>
          ))}
        </div>
        <div className="projects__grid">
          {visible.map((p) => (
            <article key={p.name} className={"projects__card" + (p.featured ? " projects__card--featured" : "")}>
              <div className="projects__frame">
                <span className="projects__badge">{p.badge || p.metadata}</span>
                {p.ascii
                  ? <pre className="projects__ascii">{p.ascii}</pre>
                  : <div className="projects__placeholder">
                      <span className="projects__placeholder-meta">{p.metadata}</span>
                    </div>}
              </div>
              <div className="projects__body">
                <div className="projects__icon-row">
                  {p.icons.map((n) =>
                    n === "external-link"
                      ? <Icon key={n} name="external-link" size={14} />
                      : <Brand key={n} name={n} size={14} />)}
                </div>
                <h3 className="projects__name">{p.name}</h3>
                <p className="projects__description">{p.description}</p>
                <div className="projects__tag-row">
                  {p.tags.map((t) => <span key={t}>{t}</span>)}
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
};

window.Projects = Projects;
