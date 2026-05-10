// Project card grid. Each card: image · icon row · title · desc · tags.
const ProjectCard = ({ p }) => (
  <article className="pf-proj">
    <img className="pf-proj__img" src={p.image} alt="" />
    <div className="pf-proj__body">
      <div className="pf-proj__icons">
        <a href={p.links.external} aria-label="External link" className="pf-icon-link">
          <svg viewBox="0 0 24 24"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
        </a>
        <a href={p.links.github} aria-label="GitHub" className="pf-icon-link">
          <window.GitHubIcon size={22} />
        </a>
      </div>
      <h3 className="pf-proj__title">{p.title}</h3>
      <p className="pf-proj__desc">{p.description}</p>
      <div className="pf-tags">
        {p.tags.map(t => <span key={t} className="pf-tag">{t}</span>)}
      </div>
    </div>
  </article>
);

const PortfolioProjects = () => (
  <section id="projects" className="pf-section pf-projects">
    <h2 className="pf-section__title">Projects</h2>
    <div className="pf-projects__grid">
      {window.PORTFOLIO_DATA.projects.map(p => <ProjectCard key={p.title} p={p} />)}
    </div>
  </section>
);
window.PortfolioProjects = PortfolioProjects;
