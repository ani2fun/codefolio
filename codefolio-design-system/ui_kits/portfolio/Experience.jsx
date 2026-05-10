// Tabbed experience pane — left rail picks the role, right pane shows details.
const PortfolioExperience = () => {
  const data = window.PORTFOLIO_DATA.experiences;
  const [idx, setIdx] = React.useState(0);
  const cur = data[idx];
  return (
    <section id="experience" className="pf-section pf-exp">
      <h2 className="pf-section__title">Experience</h2>
      <div className="pf-exp__layout">
        <div className="pf-exp__rail">
          <ul>
            {data.map((d, i) => (
              <li key={d.company} className={i === idx ? "is-active" : ""} onClick={() => setIdx(i)}>
                {d.company.split(" ")[0]}
              </li>
            ))}
          </ul>
        </div>
        <div className="pf-exp__pane">
          <div className="pf-exp__pos">{cur.position}</div>
          <div className="pf-exp__co">{cur.company} <span className="pf-exp__loc">({cur.location})</span></div>
          <div className="pf-exp__time">{cur.time}</div>
          <p className="pf-exp__summary">{cur.summary}</p>
          <div className="pf-exp__stack">
            <div className="pf-eyebrow">Stack</div>
            <div className="pf-tags">
              {cur.stack.map(t => <span key={t} className="pf-tag">{t}</span>)}
            </div>
          </div>
          <div className="pf-exp__results">
            <div className="pf-results__label">Results</div>
            <p>{cur.results}</p>
          </div>
        </div>
      </div>
    </section>
  );
};
window.PortfolioExperience = PortfolioExperience;
