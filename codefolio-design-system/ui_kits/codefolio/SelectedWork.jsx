/* SelectedWork.jsx — three-row scannable strip between Hero and About.
   Heading: "Three rooms / I helped build." with intro paragraph linking to
   Experience. Rows: company-italic | dates · location · role mono | blurb
   | primary tech tags right-aligned | arrow. */

const ROWS = [
  { company: "Europcar",
    meta: "2022 – Present · Paris · Backend / Identity",
    blurb: "Customer authentication and account lifecycle on Kotlin + Spring Boot with Keycloak — for both consumer and B2B partner organisations.",
    tags: ["Kotlin", "Spring Boot", "Keycloak", "PostgreSQL"] },
  { company: "Audi AG",
    meta: "2018 – 2021 · Remote · Backend / Data Pipeline",
    blurb: "Set the early backend architecture for Audi's in-house labeling platform — ground-truth video data feeding autonomous-driving model training.",
    tags: ["Scala", "Akka-HTTP", "ScalaJS", "Argo"] },
  { company: "Disneyland Paris",
    meta: "2016 – 2018 · Marne-la-Vallée · Backend",
    blurb: "Internal tools and back-office services on the JVM. Java + Spring with Hibernate over Oracle.",
    tags: ["Java", "Spring", "Hibernate", "Oracle"] },
];

const SelectedWork = () => (
  <section id="work" className="section">
    <div className="section__inner section__inner--narrow">
      <div className="selected-work__heading-row">
        <div className="selected-work__heading">
          <p className="eyebrow">Selected work · 2017 — Present</p>
          <h2 className="section-title" style={{ margin: 0 }}>
            Three rooms<br />I helped build.
          </h2>
        </div>
        <p className="selected-work__intro">
          A 30-second skim. The full story — bullets, results, stack — lives in{" "}
          <a href="#experience" className="selected-work__intro-link">Experience</a>.
        </p>
      </div>
      <div className="work__list">
        {ROWS.map((r) => (
          <a key={r.company} className="work__row" href="#experience">
            <span className="work__rail" aria-hidden="true"></span>
            <div className="work__company">{r.company}</div>
            <div className="work__meta">{r.meta}</div>
            <div className="work__blurb">{r.blurb}</div>
            <div className="work__tags">{r.tags.map((t) => <span key={t}>{t}</span>)}</div>
            <div className="work__arrow"><Icon name="arrow-right" size={16} /></div>
          </a>
        ))}
      </div>
    </div>
  </section>
);

window.SelectedWork = SelectedWork;
