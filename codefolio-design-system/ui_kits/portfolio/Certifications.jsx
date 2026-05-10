// Vertical timeline. Star marker + diploma badge for highlighted entries.
const TrophyIcon = () => (
  <svg viewBox="0 0 24 24"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6m12 0h1.5a2.5 2.5 0 0 1 0 5H18M4 22h16M10 14.66V17a2 2 0 0 1-2 2H7m10-4.34V17a2 2 0 0 0 2 2h1m-7-15h6v8a3 3 0 0 1-3 3 3 3 0 0 1-3-3z"/></svg>
);
const StarIcon = () => (
  <svg viewBox="0 0 24 24" style={{fill: "currentColor"}}><polygon points="12 2 15 9 22 9 17 14 19 21 12 17 5 21 7 14 2 9 9 9"/></svg>
);

const PortfolioCertifications = () => {
  const list = window.PORTFOLIO_DATA.certifications;
  return (
    <section id="certifications" className="pf-section pf-certs">
      <h2 className="pf-section__title">Certifications</h2>
      <div className="pf-certs__timeline">
        <div className="pf-certs__spine"></div>
        {list.map(c => (
          <div key={c.name} className="pf-cert">
            <div className={"pf-cert__marker" + (c.highlight ? " is-highlight" : "")}>
              {c.highlight ? <StarIcon /> : <TrophyIcon />}
            </div>
            <div className="pf-cert__meta">
              <span className="pf-cert__date">{c.date}</span>
              <span className="pf-cert__len">· {c.length}</span>
            </div>
            <div className={"pf-cert__card" + (c.highlight ? " is-highlight" : "")}>
              <div className="pf-cert__issuer">{c.issuer}</div>
              <div className="pf-cert__name">
                {c.name}
                {c.badge && <span className="pf-cert__badge">{c.badge}</span>}
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
window.PortfolioCertifications = PortfolioCertifications;
