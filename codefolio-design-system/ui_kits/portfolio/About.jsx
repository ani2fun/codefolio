// About — copy + 4-portrait collage with blue tint overlay.
const PortfolioAbout = () => (
  <section id="about" className="pf-section pf-about">
    <h2 className="pf-section__title">About</h2>
    <div className="pf-about__grid">
      <div className="pf-about__copy">
        <p>
          Currently at <strong className="pf-emph-green">Europcar International</strong> in Paris,
          working on customer authentication and the wider reservation platform.
        </p>
        <p>
          I run a small four-node <strong>K3s</strong> cluster on commodity hardware at home and self-host
          my own services on it — including this site. Hands-on with the full delivery path —
          packaging, deployment, networking, observability.
        </p>
        <p>
          Outside of work I keep long-form notes in <strong>Cortex</strong> — books I'm reading, courses I'm
          taking, and the rabbit holes that distract me from them.
        </p>
      </div>
      <div className="pf-about__collage">
        {[1,2,3,4].map(i => (
          <div key={i} className="pf-about__tile">
            <img src={`../../assets/portrait-${i}.webp`} alt="" />
          </div>
        ))}
      </div>
    </div>
  </section>
);
window.PortfolioAbout = PortfolioAbout;
