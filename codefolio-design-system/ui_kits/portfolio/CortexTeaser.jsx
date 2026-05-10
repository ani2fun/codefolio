// Cortex teaser: 4 chapter cards + browse-all link.
const CortexCard = ({ c }) => (
  <a className="cx-card" href="#">
    <div className="cx-card__meta">
      <svg viewBox="0 0 24 24"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2zM22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
      <span>{c.book} · {c.chapters} chapters</span>
    </div>
    <h3 className="cx-card__title">{c.title}</h3>
    <p className="cx-card__desc">{c.desc}</p>
    <div className="cx-card__footer">
      <div className="pf-tags">
        {c.tags.map(t => <span key={t} className="pf-tag">{t}</span>)}
      </div>
      <span className="cx-card__cta">
        Read
        <svg viewBox="0 0 24 24"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
      </span>
    </div>
  </a>
);

const PortfolioCortexTeaser = () => (
  <section id="cortex" className="pf-section pf-cortex">
    <h2 className="pf-section__title">Cortex</h2>
    <p className="pf-section__lede">Long-form notes from books, courses, and rabbit holes. Click any topic to start reading.</p>
    <div className="pf-cortex__grid">
      {window.PORTFOLIO_DATA.cortexChapters.map(c => <CortexCard key={c.title} c={c} />)}
    </div>
    <a className="pf-btn pf-btn--secondary pf-cortex__all" href="#">Browse all</a>
  </section>
);
window.PortfolioCortexTeaser = PortfolioCortexTeaser;
