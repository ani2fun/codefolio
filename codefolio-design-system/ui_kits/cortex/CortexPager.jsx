const CortexPager = ({ prev, next, onPick }) => (
  <nav className="cx-pager" aria-label="Chapter pager">
    {prev ? (
      <button className="cx-pager__card" onClick={() => onPick(prev.id)}>
        <div className="cx-pager__dir">
          <svg viewBox="0 0 24 24"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
          Previous
        </div>
        <div className="cx-pager__title">Chapter {prev.num} · {prev.title}</div>
      </button>
    ) : <span />}
    {next ? (
      <button className="cx-pager__card cx-pager__card--right" onClick={() => onPick(next.id)}>
        <div className="cx-pager__dir">
          Next
          <svg viewBox="0 0 24 24"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </div>
        <div className="cx-pager__title">Chapter {next.num} · {next.title}</div>
      </button>
    ) : <span />}
  </nav>
);
window.CortexPager = CortexPager;
