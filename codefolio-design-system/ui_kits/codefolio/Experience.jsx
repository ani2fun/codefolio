/* Experience.jsx — vertical accordion of roles. One open at a time.
   Eyebrow: "EXPERIENCE · N ROLES", title: "Where the / hours went." */

const ROLES = [
  {
    id: "europcar",
    company: "Europcar",
    time: "August 2022 – Present",
    roleTag: "Backend / Identity",
    position: "Software Engineer — Backend",
    employer: "Europcar International (Paris, France)",
    url: "https://www.europcar.com/",
    summary: "Customer authentication and account lifecycle for both consumer and partner channels, plus the wider reservation platform around it.",
    primary: ["Kotlin", "Spring Boot", "Keycloak"],
    bullets: [
      "Built the customer authentication and account lifecycle on Kotlin/Spring Boot with Keycloak — account creation, email verification, password reset, login, logout, recovery, profile updates, deletion.",
      "Set up Keycloak as the shared identity layer for B2B and B2C, integrating customer profiles into a single source for downstream personalisation.",
      "Rewrote slow PostgreSQL queries on the auth and account services and added composite indexes where lookups were doing full scans.",
    ],
    results: [
      "Authored a C4-model architecture POC using Structurizr DSL; adopted by individual developers as a proposal for living documentation.",
      "Modernised the team's Gradle build with multi-level CI caching — cut build times, simplified the architecture, adopted as internal reference by 3+ adjacent teams.",
    ],
    secondary: ["Java", "OAuth2 / OIDC", "JWT", "PostgreSQL", "Kafka", "AWS", "GitLab CI/CD", "Gradle", "Structurizr", "Hexagonal / DDD"],
  },
  {
    id: "audi",
    company: "Audi AG",
    time: "2018 – 2021",
    roleTag: "Backend / Data Pipeline",
    position: "Software Engineer P.II",
    employer: "Audi AG (Ingolstadt, remote)",
    url: "https://www.audi.com/",
    summary: "Set the early backend architecture for Audi's in-house labeling platform — ground-truth video data feeding autonomous-driving model training.",
    primary: ["Scala", "Akka-HTTP", "ScalaJS"],
    bullets: [
      "Designed and shipped the Scala backend for an internal video-labeling tool used by hundreds of annotators.",
      "Pipeline work across S3, Postgres and internal data stores; turned a pilot into a funded multi-million-euro programme.",
    ],
    results: [
      "Pilot scaled into a funded multi-million-euro programme inside Audi's autonomous-driving group.",
    ],
    secondary: ["Akka Streams", "S3", "Postgres", "Argo Workflows", "Kafka", "Elasticsearch", "Docker", "GitLab CI"],
  },
  {
    id: "disney",
    company: "Disneyland Paris",
    time: "2016 – 2018",
    roleTag: "Backend",
    position: "Software Engineer",
    employer: "Disneyland Paris",
    url: "https://www.disneylandparis.com/",
    summary: "Internal tools and back-office services on the JVM.",
    primary: ["Java", "Spring"],
    bullets: [],
    results: [],
    secondary: ["Hibernate", "Oracle", "REST"],
  },
];

const Experience = () => {
  const [open, setOpen] = React.useState("europcar");
  return (
    <section id="experience" className="section">
      <div className="section__inner section__inner--narrow">
        <p className="eyebrow">Experience · {ROLES.length} roles</p>
        <h2 className="section-title">Where the<br />hours went.</h2>
        <div className="experience__list">
          {ROLES.map((r) => (
            <article key={r.id} className={"experience__role" + (open === r.id ? " experience__role--open" : "")}>
              <button className="experience__role-header"
                      onClick={() => setOpen(open === r.id ? null : r.id)}>
                <span className="experience__role-company">{r.company}</span>
                <span className="experience__role-meta">{r.time} · {r.roleTag}</span>
                <span className="experience__role-chevron">
                  <Icon name="chevron-down" size={16} />
                </span>
              </button>
              <div className="experience__role-body">
                <div className="inner">
                  <p className="experience__position">
                    {r.position} · <a className="experience__position-link" href={r.url} target="_blank" rel="noreferrer">{r.employer}</a>
                  </p>
                  {r.primary.length > 0 && (
                    <div className="experience__primary-tags">
                      {r.primary.map((t) => <span key={t} className="experience__primary-tag">{t}</span>)}
                    </div>
                  )}
                  <p className="experience__summary">{r.summary}</p>
                  {r.bullets.length > 0 && (
                    <ul className="experience__bullets">
                      {r.bullets.map((b, i) => <li key={i}>{b}</li>)}
                    </ul>
                  )}
                  {r.results.length > 0 && (
                    <div className="experience__results">
                      <h5>Results</h5>
                      <ul className="experience__results-list">
                        {r.results.map((b, i) => <li key={i}>{b}</li>)}
                      </ul>
                    </div>
                  )}
                  {r.secondary.length > 0 && (
                    <div className="experience__secondary">
                      <span className="experience__secondary-label">Also</span>
                      {r.secondary.map((t) => <span key={t} className="experience__secondary-tag">{t}</span>)}
                    </div>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
};

window.Experience = Experience;
