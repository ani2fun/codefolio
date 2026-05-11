/* About.jsx — long-form prose column + sticky portrait/fact-list sidecard.
   Mirrors the live site: Clarke epigraph, three context paragraphs, an
   italic-emphasis bullet list, a "what makes me different" paragraph, the
   Liora diploma + Gradle plugin note, an "outside work" line, and a closing
   "happy to talk about" CTA. Fact list (5 rows) lives in the sidecard. */

const FACT_LIST = [
  ["CURRENTLY", "Europcar International · Backend"],
  ["BASED",     "Paris, France · open to remote (EU) or hybrid"],
  ["COMPLETED", "Diplôme Data Engineer · Liora × Sorbonne (Apr 2026)"],
  ["EDUCATION", "M.Sc. ISEP Paris · B.E. Univ. Mumbai"],
  ["LANGUAGES", "English · Hindi · Marathi · learning French"],
];

const About = () => (
  <section id="about" className="section">
    <div className="section__inner about__layout">
      <div className="about__prose">
        <p className="eyebrow">About</p>
        <h2 className="section-title">A decade in production systems.</h2>
        <blockquote className="about__quote">
          <p className="about__quote-text">
            The only way to discover the limits of the possible is to go beyond
            them into the impossible.
          </p>
          <cite className="about__quote-author">— Arthur C. Clarke</cite>
        </blockquote>
        <p className="about__paragraph">
          I build the backend systems that power large applications: the part
          users don't see, but everything depends on.
        </p>
        <p className="about__paragraph">
          As a Software Engineer / Data Engineer with{" "}
          <span className="about__emphasis">10+ years of experience</span>,
          I've worked on services on the JVM (Kotlin, Java, Scala) with Spring
          Boot, Kafka, PostgreSQL, and cloud delivery on AWS and GCP.
        </p>
        <p className="about__paragraph">
          Some of the companies and clients I've worked with:{" "}
          <span className="about__emphasis">
            Europcar, Disneyland Paris, Audi, Dassault Systèmes, UPS, Nokia
            Bell Labs
          </span>.
        </p>
        <p className="about__paragraph">A few things I keep coming back to:</p>
        <ul className="about__list">
          <li className="about__list-item">
            <span className="about__emphasis">Microservices on the JVM</span>{" "}
            — with a soft spot for hexagonal architecture and Domain-Driven
            Design; currently shipping Kotlin services on this pattern at
            Europcar.
          </li>
          <li className="about__list-item">
            <span className="about__emphasis">Data pipelines and infrastructure</span>{" "}
            — at Audi, helped deliver the labeling pipeline behind their
            autonomous-driving model training (Argo Workflows, Kafka,
            Elasticsearch).
          </li>
          <li className="about__list-item">
            <span className="about__emphasis">Self-hosted infrastructure</span>{" "}
            — I run a small Kubernetes homelab and care about the craft of
            operating real services.
          </li>
        </ul>
        <p className="about__paragraph">
          What makes me different: I like getting my hands dirty on hard
          problems whether they're in code or not. I renovated my own home with
          the same instinct that makes me run my own k8s cluster instead of
          just using someone else's.
        </p>
        <p className="about__paragraph">
          I recently earned the{" "}
          <span className="about__emphasis">
            Data Engineer diploma at Liora (formerly DataScientest)
          </span>{" "}
          backed by <span className="about__emphasis">Sorbonne University</span>.
          I also maintain a Gradle plugin on Maven Central
          (<code className="about__inline-code">
            eu.kakde.gradle.sonatype-maven-central-publisher
          </code>) and write technical guides at kakde.eu.
        </p>
        <p className="about__paragraph">
          <span className="about__emphasis">Outside work:</span>{" "}
          yoga and meditation daily. Recently lost 10 kg over four months by
          treating it like a system to debug. Reading self-help and the
          Bhagavad Gita. Action and role-playing games on PS5 when the day is
          done.
        </p>
        <p className="about__paragraph about__paragraph--cta">
          <span className="about__emphasis">Happy to talk about: </span>
          backend and data-engineering roles in Paris (hybrid / on-site) or
          fully remote in the EU.
        </p>
      </div>
      <aside className="about__sidecard">
        <img className="about__portrait"
             src="../../assets/portrait.webp"
             alt="Aniket Kakde — portrait" loading="lazy" />
        <dl className="about__facts">
          {FACT_LIST.map(([label, value]) => (
            <div key={label} className="about__fact">
              <dt className="about__fact-label">{label}</dt>
              <dd className="about__fact-value">{value}</dd>
            </div>
          ))}
        </dl>
      </aside>
    </div>
  </section>
);

window.About = About;
