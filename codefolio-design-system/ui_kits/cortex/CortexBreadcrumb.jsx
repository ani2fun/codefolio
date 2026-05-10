const CortexBreadcrumb = ({ chapter }) => (
  <nav className="cx-crumb" aria-label="Breadcrumb">
    <a href="#" className="cx-crumb__link">Cortex</a>
    <svg viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
    <a href="#" className="cx-crumb__link">Designing data-intensive applications</a>
    <svg viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
    <span className="cx-crumb__current">Chapter {chapter.num} · {chapter.title}</span>
  </nav>
);
window.CortexBreadcrumb = CortexBreadcrumb;
