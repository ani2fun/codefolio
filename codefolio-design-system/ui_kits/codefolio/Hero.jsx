/* Hero.jsx — landing block (status pill, italic display name, lede, 4 CTAs, stat strip).

   CTAs (mirror live site):
     1. Download CV   — primary, downloads /Aniket-Kakde-CV-EN.pdf
     2. See selected work — ghost, anchors to #work
     3. Cortex        — ghost, navigates to /cortex (slow-burn reading shelf)
     4. Blog          — ghost, navigates to /blogs
*/

const Hero = ({ onNav, onDownloadCV, onOpenCortex, onOpenBlog }) => (
  <section id="hero" className="hero">
    <div className="hero__inner">
      <div className="hero__status">
        <span className="hero__status-dot" aria-hidden="true"></span>
        <span className="hero__status-text">
          Currently at Europcar · open to senior backend roles
        </span>
      </div>
      <h1 className="hero__name">
        Aniket Kakde<sup className="hero__name-sup">EU</sup>
      </h1>
      <p className="hero__lede">
        <em>Backend-leaning</em> Software Engineer. Currently at Europcar,
        building the unified B2B/B2C customer identity platform.
        <br />
        Previously: Helped build Audi's in-house video annotation platform and
        data pipeline used to generate ground-truth training data for autonomous
        driving models, and helped lead a Disney pilot that became a
        multi-million-euro replacement of their legacy marketing platform.
      </p>
      <div className="hero__cta-row">
        <a className="hero__cta hero__cta--primary"
           href="../../assets/Aniket-Kakde-CV-EN.pdf"
           onClick={(e) => { if (onDownloadCV) { e.preventDefault(); onDownloadCV(); } }}>
          <Icon className="hero__cta-icon" name="download" size={16} />
          Download CV
        </a>
        <a className="hero__cta hero__cta--ghost" href="#work"
           onClick={(e) => { e.preventDefault(); onNav && onNav("work"); }}>
          <Icon className="hero__cta-icon" name="arrow-right" size={16} />
          See selected work
        </a>
        <a className="hero__cta hero__cta--ghost" href="/cortex"
           onClick={(e) => { if (onOpenCortex) { e.preventDefault(); onOpenCortex(); } }}>
          <Icon className="hero__cta-icon" name="book-open" size={16} />
          Cortex
        </a>
        <a className="hero__cta hero__cta--ghost" href="/blogs"
           onClick={(e) => { if (onOpenBlog) { e.preventDefault(); onOpenBlog(); } }}>
          <Icon className="hero__cta-icon" name="pencil" size={16} />
          Blog
        </a>
      </div>
      <div className="hero__stats">
        <div className="hero__stat">
          <div className="hero__stat-num">10<span className="hero__stat-sup">yrs</span></div>
          <span className="hero__stat-label">on production systems</span>
        </div>
        <div className="hero__stat">
          <div className="hero__stat-num">4<span className="hero__stat-sup">sectors</span></div>
          <span className="hero__stat-label">mobility · automotive · media · logistics</span>
        </div>
        <div className="hero__stat">
          <div className="hero__stat-num">€M</div>
          <span className="hero__stat-label">pilot → funded programme</span>
        </div>
        <div className="hero__stat">
          <div className="hero__stat-num">6<span className="hero__stat-sup">companies</span></div>
          <span className="hero__stat-label">Europcar Audi Disneyland-Paris Dassault UPS Bell-Labs</span>
        </div>
      </div>
    </div>
  </section>
);

window.Hero = Hero;
