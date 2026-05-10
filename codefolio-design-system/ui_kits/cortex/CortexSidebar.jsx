const CortexSidebar = ({ activeId, onPick }) => {
  const { book } = window.CORTEX_DATA;
  return (
    <aside className="cx-sidebar">
      <div className="cx-sidebar__header">
        <div className="cx-sidebar__eyebrow">Book</div>
        <div className="cx-sidebar__title">{book.title}</div>
        <div className="cx-sidebar__author">{book.author}</div>
      </div>
      <nav className="cx-sidebar__nav">
        {book.chapters.map(c => {
          const cls = "cx-sidebar__item" + (c.id === activeId ? " is-active" : "");
          return (
            <button key={c.id} className={cls} onClick={() => onPick(c.id)}>
              <span className="cx-sidebar__num">{String(c.num).padStart(2, "0")}</span>
              <span className="cx-sidebar__name">{c.title}</span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
};
window.CortexSidebar = CortexSidebar;
