/* Header.jsx — sticky top nav. Mirrors the live site:
   brand "a · aniket.kakde", 6 menu links (Work · About · Experience ·
   Projects · Cortex · Blog), theme toggle, "Get in touch" mailto CTA, and a
   mobile burger that opens a drawer. */

const Header = ({ activeId, onNav, dark, onToggleDark }) => {
  const [open, setOpen] = React.useState(false);
  const items = [
    ["work", "Work"],
    ["about", "About"],
    ["experience", "Experience"],
    ["projects", "Projects"],
    ["cortex", "Cortex"],
    ["blog", "Blog"],
  ];
  const handleClick = (id) => (e) => {
    e.preventDefault();
    setOpen(false);
    onNav && onNav(id);
  };
  return (
    <header className="header">
      <nav className="header__nav">
        <a href="#hero" className="header__brand" onClick={handleClick("hero")}>
          <span className="header__logomark">a</span>
          <span className="header__wordmark">aniket.kakde</span>
        </a>
        <div className="header__menu">
          {items.map(([id, label]) => (
            <a
              key={id}
              href={`#${id}`}
              className={"header__link" + (activeId === id ? " header__link--active" : "")}
              onClick={handleClick(id)}
            >{label}</a>
          ))}
        </div>
        <div className="header__actions">
          <button className="header__toggle" aria-label="Toggle theme" onClick={onToggleDark}>
            <Icon name={dark ? "sun" : "moon"} size={18} />
          </button>
          <a className="header__cta" href="mailto:a.r.kakde@gmail.com">
            Get in touch
          </a>
          <button className="header__burger"
                  aria-label={open ? "Close menu" : "Open menu"}
                  onClick={() => setOpen((o) => !o)}>
            <Icon className="header__burger-icon" name={open ? "x" : "menu"} size={18} />
          </button>
        </div>
      </nav>
      {open && (
        <nav className="header__drawer">
          <ul className="header__drawer-list">
            {items.map(([id, label]) => (
              <li key={id}>
                <a href={`#${id}`} className="header__drawer-link"
                   onClick={handleClick(id)}>{label}</a>
              </li>
            ))}
          </ul>
        </nav>
      )}
    </header>
  );
};

window.Header = Header;
