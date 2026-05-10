// Outlined hero card with two CTAs.
const PortfolioHero = ({ onNav }) => (
  <section id="hero" className="pf-section pf-hero">
    <div className="pf-hero__card">
      <p className="pf-hero__greeting">Hi, I'm</p>
      <h1 className="pf-hero__name">Aniket Kakde</h1>
      <p className="pf-hero__subtitle">
        Backend-leaning Software Engineer with ten years on production systems.
        Day-to-day in Java, Kotlin, Scala, Kafka, PostgreSQL, and AWS/GCP.
      </p>
      <div className="pf-hero__ctas">
        <a className="pf-btn pf-btn--primary" href="#cv">Download CV</a>
        <a className="pf-btn pf-btn--secondary" onClick={(e) => { e.preventDefault(); onNav("cortex"); }} href="#contact">Get in touch</a>
      </div>
    </div>
  </section>
);
window.PortfolioHero = PortfolioHero;
