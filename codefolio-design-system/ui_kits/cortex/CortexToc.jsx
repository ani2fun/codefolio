// Right-rail TOC minimap. Ticks at rest; full TOC on hover.
const CortexToc = ({ activeId, onPick }) => {
  const { sections } = window.CORTEX_DATA.chapter;
  const [open, setOpen] = React.useState(false);
  return (
    <aside
      className={"cx-toc" + (open ? " is-open" : "")}
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
    >
      <div className="cx-toc__eyebrow">Contents</div>
      <ul className="cx-toc__list">
        {sections.map(s => (
          <li key={s.id} className={"cx-toc__row cx-toc__row--l" + s.level + (s.id === activeId ? " is-active" : "")}>
            <button className="cx-toc__btn" onClick={() => onPick(s.id)}>
              <span className="cx-toc__tick"></span>
              <span className="cx-toc__label">{s.title}</span>
            </button>
          </li>
        ))}
      </ul>
    </aside>
  );
};
window.CortexToc = CortexToc;
