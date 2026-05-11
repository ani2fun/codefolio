/* Certifications.jsx — vertical timeline of diplomas / courses.
   Mirrors the live site's `certifications.css` block. Data lives inline here
   (matches `client/src/data/certificationsData.json`); swap for `fetch()` if
   you want to bind to the JSON directly.

   - `highlight: true` lifts a row to the in-progress / featured treatment
     (terracotta-filled marker, tinted card, "IN PROGRESS" badge).
   - Markers default to a star glyph; flagship diplomas use a trophy glyph.
*/

const CERTIFICATIONS = [
  {
    name: "Diplôme Data Engineer Liora x Sorbonne University",
    issuer: "Sorbonne University · Liora (DataScientest)",
    date: "April 2026",
    length: "9-month programme",
    description: "Hands-on coursework spanning data engineering and DevOps fundamentals — Kafka, PySpark, Airflow, MongoDB, Snowflake, dbt, FastAPI, Docker/Kubernetes, and Prometheus/Grafana.",
    tags: ["Kafka", "PySpark", "Airflow", "Snowflake", "dbt", "FastAPI", "Docker", "Kubernetes"],
    url: "#",
    highlight: true,
    badge: "In progress",
    marker: "trophy",
  },
  {
    name: "The Complete SQL Bootcamp — Zero to Hero",
    issuer: "Udemy · Baraa Khatib Salkini",
    date: "December 2025",
    length: "29 hours",
    description: "End-to-end SQL — relational design, joins, aggregations, window functions, indexes, and query tuning.",
    tags: ["SQL", "PostgreSQL", "Query tuning"],
    url: "#",
    marker: "star",
  },
  {
    name: "Kubernetes for the Absolute Beginners — Hands-On",
    issuer: "Udemy · KodeKloud",
    date: "July 2024",
    length: "6.5 hours",
    description: "Hands-on introduction to Kubernetes primitives — pods, deployments, services, configmaps — and the kubectl workflow.",
    tags: ["Kubernetes", "kubectl", "DevOps"],
    url: "#",
    marker: "star",
  },
  {
    name: "Ansible for the Absolute Beginner — DevOps",
    issuer: "Udemy · KodeKloud",
    date: "July 2024",
    length: "2.5 hours",
    description: "Configuration management with Ansible — inventories, playbooks, roles, and idempotent provisioning.",
    tags: ["Ansible", "DevOps", "Configuration management"],
    url: "#",
    marker: "star",
  },
];

const MarkerGlyph = ({ kind }) => {
  if (kind === "trophy") {
    return (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
           strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"
           className="certifications__marker-icon" aria-hidden="true">
        <path d="M8 21h8M12 17v4M7 4h10v4a5 5 0 0 1-10 0z"/>
        <path d="M17 5h2a2 2 0 0 1 0 4h-2M7 5H5a2 2 0 0 0 0 4h2"/>
      </svg>
    );
  }
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"
         className="certifications__marker-icon" aria-hidden="true">
      <polygon points="12 2 15 9 22 9.5 17 14.5 18.5 22 12 18 5.5 22 7 14.5 2 9.5 9 9 12 2"/>
    </svg>
  );
};

const Certifications = () => {
  const last = CERTIFICATIONS.length - 1;
  return (
    <section id="certifications" className="section">
      <div className="section__inner section__inner--narrow">
        <p className="eyebrow" style={{textAlign:"center"}}>Certifications · {CERTIFICATIONS.length} entries</p>
        <h2 className="certifications__title">Diplomas &amp; courses.</h2>
        <p className="certifications__subtitle">
          What I learned outside of work — formal coursework and self-directed study, most recent first.
        </p>

        <div className="certifications__timeline">
          <span className="certifications__spine" aria-hidden="true" />

          {CERTIFICATIONS.map((c, i) => {
            const hl = !!c.highlight;
            return (
              <article key={c.name}
                       className={"certifications__entry" + (i === last ? " certifications__entry--last" : "")}>
                <span className={"certifications__marker" + (hl ? " certifications__marker--highlight" : "")}>
                  <MarkerGlyph kind={c.marker} />
                </span>

                <div className="certifications__meta">
                  <span className="certifications__date">{c.date}</span>
                  <span className="certifications__length">· {c.length}</span>
                </div>

                <a className={"certifications__card" + (hl ? " certifications__card--highlight" : "")}
                   href={c.url}>
                  <div className="certifications__issuer-row">
                    <span className="certifications__issuer">{c.issuer}</span>
                  </div>
                  <h3 className="certifications__name">
                    {c.name}
                    {hl && c.badge && <span className="certifications__badge">{c.badge}</span>}
                  </h3>
                  <p className="certifications__description">{c.description}</p>
                  <div className="certifications__footer">
                    {c.tags.map((t, idx) => (
                      <span key={t} className="certifications__tag">
                        {idx > 0 && <span className="certifications__tag-sep">·</span>} {t}
                      </span>
                    ))}
                    <span className="certifications__view">
                      View certificate
                      <svg className="certifications__view-arrow" viewBox="0 0 24 24" fill="none"
                           stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
                      </svg>
                    </span>
                  </div>
                </a>
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
};

window.Certifications = Certifications;
