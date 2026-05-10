// Fixed pill nav with theme toggle. Mirrors Header.scala.
const PortfolioHeader = ({ active, onNav, dark, onToggleDark }) => {
  const items = ["About", "Experience", "Projects", "Certifications", "Cortex"];
  return (
    <header className="pf-header">
      <a className="pf-header__title" href="#hero" onClick={(e) => { e.preventDefault(); onNav("hero"); }}>
        Aniket Kakde
      </a>
      <nav className="pf-header__menu">
        {items.map(it => {
          const id = it.toLowerCase();
          const cls = "pf-header__link" + (active === id ? " is-active" : "");
          return (
            <a key={id} className={cls} href={`#${id}`} onClick={(e) => { e.preventDefault(); onNav(id); }}>{it}</a>
          );
        })}
      </nav>
      <button className="pf-icon-btn" aria-label="Toggle theme" onClick={onToggleDark}>
        {dark
          ? <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/></svg>
          : <svg viewBox="0 0 24 24"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>}
      </button>
    </header>
  );
};
window.PortfolioHeader = PortfolioHeader;
