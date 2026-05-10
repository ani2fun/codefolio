// Sections — Header / Hero / SelectedWork / About / Experience / Projects / Cortex / Footer
const { useState, useEffect, useRef, useMemo } = React;

// ─── Icons (inline SVG, no external deps) ───────────────────────────
const Icon = {
  Sun: (p) =>
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41" />
    </svg>,

  Moon: (p) =>
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>,

  Arrow: (p) =>
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <path d="M5 12h14M13 5l7 7-7 7" />
    </svg>,

  ArrowUR: (p) =>
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <path d="M7 17 17 7M7 7h10v10" />
    </svg>,

  Chevron: (p) =>
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <path d="M6 9l6 6 6-6" />
    </svg>,

  Github: (p) =>
  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" {...p}>
      <path d="M12 .5a12 12 0 0 0-3.79 23.4c.6.11.82-.26.82-.58v-2c-3.34.73-4.04-1.6-4.04-1.6-.55-1.4-1.34-1.77-1.34-1.77-1.1-.75.08-.74.08-.74 1.21.09 1.85 1.25 1.85 1.25 1.07 1.84 2.81 1.31 3.5 1 .1-.78.42-1.31.76-1.61-2.66-.31-5.46-1.34-5.46-5.95 0-1.32.47-2.4 1.24-3.24-.13-.31-.54-1.55.12-3.23 0 0 1.01-.32 3.3 1.23a11.46 11.46 0 0 1 6 0c2.29-1.55 3.3-1.23 3.3-1.23.66 1.68.25 2.92.12 3.23.77.84 1.24 1.92 1.24 3.24 0 4.62-2.81 5.63-5.49 5.93.43.37.81 1.1.81 2.22v3.29c0 .32.22.69.83.57A12 12 0 0 0 12 .5z" />
    </svg>,

  LinkedIn: (p) =>
  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" {...p}>
      <path d="M19 0h-14a5 5 0 0 0-5 5v14a5 5 0 0 0 5 5h14a5 5 0 0 0 5-5V5a5 5 0 0 0-5-5zm-11 19h-3v-9h3v9zm-1.5-10.3a1.7 1.7 0 1 1 0-3.4 1.7 1.7 0 0 1 0 3.4zm12.5 10.3h-3v-4.5c0-1.1-.4-1.9-1.4-1.9a1.5 1.5 0 0 0-1.4 1c-.05.18-.07.4-.07.62v4.78h-3v-9h3v1.27a3 3 0 0 1 2.7-1.5c2 0 3.18 1.32 3.18 4v5.23z" />
    </svg>

};

// ─── Header ─────────────────────────────────────────────────────────
function Header({ dark, setDark }) {
  return (
    <header className="hdr">
      <div className="container hdr__inner">
        <a href="#hero" className="hdr__brand">
          <span className="hdr__brand-mark">a</span>
          <span>aniket.kakde</span>
        </a>
        <nav className="hdr__nav">
          <a href="#work" className="hdr__link">Work</a>
          <a href="#about" className="hdr__link">About</a>
          <a href="#experience" className="hdr__link">Experience</a>
          <a href="#projects" className="hdr__link">Projects</a>
          <a href="#cortex" className="hdr__link">Cortex</a>
        </nav>
        <div className="hdr__right">
          <button
            className="hdr__theme"
            aria-label="Toggle theme"
            onClick={() => {
              const next = !dark;
              setDark(next);
              try {localStorage.setItem("theme-v2", next ? "dark" : "light");} catch (_) {}
            }}>
            
            {dark ? <Icon.Sun /> : <Icon.Moon />}
          </button>
          <a className="hdr__cta" href="mailto:a.r.kakde@gmail.com?subject=Hi%20Aniket">Get in touch</a>
        </div>
      </div>
    </header>);

}

// ─── Hero + stats + logos ──────────────────────────────────────────
function Hero() {
  return (
    <section id="hero" className="hero relative">
      <div className="container">
        <span className="hero__status">
          <span className="hero__status-dot" />
          Currently at Europcar · open to senior backend roles
        </span>
        <h1 className="hero__name" style={{ fontSize: "70px", fontWeight: "400" }}>
          Aniket Kakde<span className="hero__name-sup">EU</span>
        </h1>
        <p className="hero__lede">
          A <em>backend-leaning</em> software engineer in Paris.
          I build the unglamorous pieces — auth, identity, integration, the parts of a platform that have to keep working
          while the rest of the product changes underneath them.
        </p>
        <div className="hero__cta-row">
          <a className="btn btn--primary" href="/Aniket-Kakde-CV-EN.pdf">
            Download CV <Icon.Arrow className="btn__arrow" />
          </a>
          <a className="btn btn--ghost" href="#work">See selected work</a>
          <a className="btn btn--text" href="mailto:a.r.kakde@gmail.com">a.r.kakde@gmail.com</a>
        </div>

        <div className="hero__stats">
          <div className="hero__stat">
            <div className="hero__stat-num">10<span style={{ fontSize: '24px', color: 'var(--muted)' }}> yrs</span></div>
            <div className="hero__stat-label">on production systems</div>
          </div>
          <div className="hero__stat">
            <div className="hero__stat-num">6</div>
            <div className="hero__stat-label">Companies, four sectors</div>
          </div>
          <div className="hero__stat">
            <div className="hero__stat-num">€M</div>
            <div className="hero__stat-label">Pilot → funded programme</div>
          </div>
          <div className="hero__stat">
            <div className="hero__stat-num">JVM</div>
            <div className="hero__stat-label">Kotlin · Java · Scala</div>
          </div>
        </div>

        <div className="logos">
          <span className="logos__label">Built for</span>
          <span className="logo">Europcar</span>
          <span className="logo">Audi</span>
          <span className="logo">Disneyland Paris</span>
          <span className="logo">Dassault</span>
          <span className="logo">UPS</span>
          <span className="logo">Bell Labs</span>
        </div>
      </div>
    </section>);

}

// ─── Selected work strip ───────────────────────────────────────────
function SelectedWork() {
  const items = window.PORTFOLIO_DATA.selectedWork;
  return (
    <section id="work" className="swork relative">
      <div className="container">
        <div className="section__head">
          <div>
            <p className="section__eyebrow">Selected work · 2017 — present</p>
            <h2 className="section__title">Three rooms<br />I helped build.</h2>
          </div>
          <p className="section__sub">A 30-second skim. The full story — bullets, results, stack — lives in <a href="#experience" style={{ textDecoration: 'underline', textDecorationColor: 'var(--line-2)', textUnderlineOffset: '3px' }}>Experience</a>.</p>
        </div>
        <div className="swork__list">
          {items.map((it, i) =>
          <a key={i} className="swork__row" href={`#exp-${it.anchor}`}>
              <div className="swork__co">
                <span className="swork__co-name">{it.company}</span>
                <span className="swork__co-meta">{it.meta} · {it.role}</span>
              </div>
              <div className="swork__what">{it.what}</div>
              <div className="swork__tech">
                {it.tech.map((t) => <span key={t} className="swork__tech-tag">{t}</span>)}
              </div>
              <Icon.ArrowUR className="swork__arrow" />
            </a>
          )}
        </div>
      </div>
    </section>);

}

// ─── About ──────────────────────────────────────────────────────────
function About() {
  return (
    <section id="about" className="about relative">
      <div className="container">
        <p className="section__eyebrow">About</p>
        <div className="about__layout">
          <div className="about__prose">
            <p>
              I've spent <span className="accent">ten years on production systems</span> — long enough to have an honest opinion about
              which architectural choices age well and which look clever in slides but quietly cost you for years.
            </p>
            <p>
              At <a className="link" href="https://www.disneylandparis.com/" target="_blank" rel="noopener">Disneyland Paris</a> I co-led
              a pilot that turned into a multi-million-euro replacement of the legacy marketing platform.
              For client <a className="link" href="https://www.audi.com/" target="_blank" rel="noopener">Audi</a>, I helped set the early backend architecture for an
              autonomous-driving labeling pipeline that grew from three engineers to thirty without needing to be rewritten.
            </p>
            <p>
              Today I'm at <a className="link" href="https://www.europcar.com/" target="_blank" rel="noopener">Europcar International</a>,
              building customer authentication and the wider reservation platform — Kotlin / Spring Boot, Kafka, PostgreSQL, on AWS and GCP.
              Outside work I run a four-node K3s cluster at home, maintain a <a className="link" href="https://plugins.gradle.org/plugin/eu.kakde.gradle.sonatype-maven-central-publisher" target="_blank" rel="noopener">Gradle plugin</a> on
              the official Plugin Portal, and am wrapping up the <span className="accent">Diplôme Data Engineer</span> at Sorbonne / DataScientest.
            </p>
          </div>
          <aside className="about__side">
            <img src="assets/portrait.webp" alt="Aniket Kakde" className="about__portrait" loading="lazy" />
            <dl className="about__facts">
              <div className="about__fact"><dt className="about__fact-key">Currently</dt><dd className="about__fact-val">Europcar International · Backend</dd></div>
              <div className="about__fact"><dt className="about__fact-key">Based</dt><dd className="about__fact-val">Paris, France · open to remote (EU)</dd></div>
              <div className="about__fact"><dt className="about__fact-key">Studying</dt><dd className="about__fact-val">Diplôme Data Engineer (RNCP-7), 2026</dd></div>
              <div className="about__fact"><dt className="about__fact-key">Education</dt><dd className="about__fact-val">M.Sc. ISEP Paris · B.E. Univ. Mumbai</dd></div>
              <div className="about__fact"><dt className="about__fact-key">Languages</dt><dd className="about__fact-val">English · Hindi · Marathi · learning French</dd></div>
            </dl>
          </aside>
        </div>
      </div>
    </section>);

}

// ─── Experience accordion ──────────────────────────────────────────
function Experience() {
  const items = window.PORTFOLIO_DATA.experience;
  const [open, setOpen] = useState(0);
  return (
    <section id="experience" className="exp relative">
      <div className="container">
        <p className="section__eyebrow">Experience · {items.length} roles</p>
        <h2 className="section__title">Where the<br />hours went.</h2>
        <ul className="exp__list" style={{ marginTop: 28, listStyle: 'none', padding: 0 }}>
          {items.map((it, i) => {
            const isOpen = open === i;
            return (
              <li
                key={i}
                id={`exp-${it.short.toLowerCase().replace(/\s+/g, '-')}`}
                className={`exp__item${isOpen ? ' exp__item--open' : ''}`}>
                
                <div
                  className="exp__head"
                  role="button"
                  tabIndex={0}
                  onClick={() => setOpen(isOpen ? -1 : i)}
                  onKeyDown={(e) => {if (e.key === 'Enter' || e.key === ' ') {e.preventDefault();setOpen(isOpen ? -1 : i);}}}>
                  
                  <span className="exp__co">{it.short}</span>
                  <span className="exp__pos">{it.position}</span>
                  <span className="exp__time">{it.time}</span>
                  <Icon.Chevron className="exp__chev" />
                </div>
                <div className="exp__body">
                  <div className="exp__inner">
                    <div className="exp__inner-pad" />
                    <div>
                      <p className="exp__summary">{it.summary}</p>
                      <ul className="exp__bullets">
                        {it.bullets.map((b, j) => <li key={j} className="exp__bullet">{b}</li>)}
                      </ul>
                      {it.results && it.results.length > 0 &&
                      <div className="exp__results">
                          <p className="exp__results-label">Results</p>
                          <ul>
                            {it.results.map((r, j) => <li key={j}>{r}</li>)}
                          </ul>
                        </div>
                      }
                      <div className="exp__tech">
                        {it.primary.map((t) => <span key={t} className="exp__tech-tag exp__tech-tag--primary">{t}</span>)}
                        {it.secondary.map((t) => <span key={t} className="exp__tech-tag">{t}</span>)}
                      </div>
                    </div>
                  </div>
                </div>
              </li>);

          })}
        </ul>
      </div>
    </section>);

}

// ─── Projects (filterable, featured first) ────────────────────────
function Projects() {
  const projects = window.PORTFOLIO_DATA.projects;
  const cats = useMemo(() => {
    const counts = projects.reduce((acc, p) => {acc[p.category] = (acc[p.category] || 0) + 1;return acc;}, {});
    return [{ key: 'All', count: projects.length }, ...Object.keys(counts).map((k) => ({ key: k, count: counts[k] }))];
  }, [projects]);
  const [filter, setFilter] = useState('All');
  const featured = projects.find((p) => p.featured);
  const rest = projects.filter((p) => !p.featured && (filter === 'All' || p.category === filter));

  return (
    <section id="projects" className="proj relative">
      <div className="container">
        <div className="section__head">
          <div>
            <p className="section__eyebrow">Side projects · {projects.length} live</p>
            <h2 className="section__title">Things I made<br />on weekends.</h2>
          </div>
          <p className="section__sub">Self-hosted on the homelab below — the meta-loop is part of the point.</p>
        </div>

        <div className="proj__filters" role="tablist" aria-label="Filter projects">
          {cats.map((c) =>
          <button
            key={c.key}
            className={`chip${filter === c.key ? ' chip--active' : ''}`}
            onClick={() => setFilter(c.key)}
            role="tab"
            aria-selected={filter === c.key}>
            
              {c.key} <span className="chip__count">{c.count}</span>
            </button>
          )}
        </div>

        {(filter === 'All' || featured && filter === featured.category) && featured &&
        <div className="proj__featured">
            <div className="proj__featured-art">
              <span className="proj__featured-art-mark">live · 4 nodes · k3s</span>
              <pre className="proj__featured-diagram">
{`┌── ingress ────────────────┐
│  traefik · cert-manager   │
└──────────┬────────────────┘
           │
┌──────────▼────────────────┐
│  k3s control plane (n=1)  │
└─┬───────┬───────┬─────────┘
  │       │       │
┌─▼─┐   ┌─▼─┐   ┌─▼─┐
│n1 │   │n2 │   │n3 │
│   │   │   │   │   │
└───┘   └───┘   └───┘
        kakde.eu
       notebook.kakde.eu`}
              </pre>
            </div>
            <div className="proj__featured-body">
              <span className="proj__featured-eyebrow">Featured · {featured.category}</span>
              <h3 className="proj__featured-title">{featured.name}</h3>
              <p className="proj__featured-desc">{featured.description}</p>
              <div className="proj__featured-tags">
                {featured.tags.map((t) => <span key={t} className="exp__tech-tag">{t}</span>)}
              </div>
              <div className="proj__featured-actions">
                <a className="btn btn--primary" href={featured.projectUrl} target="_blank" rel="noopener">
                  Visit live <Icon.ArrowUR className="btn__arrow" />
                </a>
                <a className="btn btn--ghost" href={featured.githubUrl} target="_blank" rel="noopener">
                  <Icon.Github /> Source
                </a>
              </div>
            </div>
          </div>
        }

        <div className="proj__grid">
          {rest.map((p, i) => <ProjectCard key={i} p={p} />)}
        </div>
      </div>
    </section>);

}

function ProjectCard({ p }) {
  return (
    <a href={p.projectUrl || p.githubUrl} target="_blank" rel="noopener" className="pcard">
      <div className={`pcard__art pcard__art--${p.art || 'stripe-a'}`}>
        {p.art === 'photo' && p.photo ?
        <img src={p.photo} alt="" loading="lazy" /> :
        <span className="pcard__placeholder">{p.category.toUpperCase()}</span>}
      </div>
      <div className="pcard__body">
        <h3 className="pcard__name">{p.name}</h3>
        <p className="pcard__desc">{p.description}</p>
        <div className="pcard__row">
          <div className="pcard__tags">
            {p.tags.slice(0, 4).map((t) => <span key={t} className="pcard__tag">{t}</span>)}
          </div>
          <div className="pcard__links">
            {p.githubUrl &&
            <span className="pcard__icon-link" onClick={(e) => {e.stopPropagation();e.preventDefault();window.open(p.githubUrl, '_blank');}}>
                <Icon.Github />
              </span>
            }
            {p.projectUrl && p.projectUrl !== p.githubUrl &&
            <span className="pcard__icon-link" onClick={(e) => {e.stopPropagation();e.preventDefault();window.open(p.projectUrl, '_blank');}}>
                <Icon.ArrowUR />
              </span>
            }
          </div>
        </div>
      </div>
    </a>);

}

// ─── Cortex preview ───────────────────────────────────────────────
function Cortex() {
  const items = window.PORTFOLIO_DATA.cortex;
  return (
    <section id="cortex" className="cortex relative">
      <div className="container">
        <p className="section__eyebrow">Cortex — long-form notes</p>
        <h2 className="section__title">Writing<br />I'm working through.</h2>
        <span className="cortex__current">
          <span className="cortex__current-pulse" />
          Currently writing: <em style={{ fontFamily: 'var(--serif)', fontStyle: 'italic', marginLeft: 6 }}>Kafka rebalancing internals</em>
        </span>
        <div className="cortex__grid">
          {items.map((c, i) =>
          <a key={i} className="ccard" href="#cortex">
              <span className="ccard__chap">{c.chap}</span>
              <h3 className="ccard__title">{c.title}</h3>
              <p className="ccard__excerpt">{c.excerpt}</p>
              <div className="ccard__meta">{c.meta.map((m, j) => <span key={j}>{m}</span>)}</div>
            </a>
          )}
        </div>
        <div style={{ marginTop: 32 }}>
          <a className="btn btn--ghost" href="/cortex">Browse the index <Icon.Arrow className="btn__arrow" /></a>
        </div>
      </div>
    </section>);

}

// ─── Footer ────────────────────────────────────────────────────────
function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="ftr relative">
      <div className="container">
        <div className="ftr__inner">
          <div>
            <h2 className="ftr__name">Let's talk.</h2>
            <p className="ftr__sub">
              Senior backend roles, JVM ecosystems, identity / platforms.
              Reach me at <a href="mailto:a.r.kakde@gmail.com">a.r.kakde@gmail.com</a>.
            </p>
          </div>
          <div className="ftr__cols">
            <div>
              <p className="ftr__col-label">Find me</p>
              <div className="ftr__col">
                <a href="https://www.linkedin.com/in/aniketkakde/" target="_blank" rel="noopener">LinkedIn ↗</a>
                <a href="https://github.com/ani2fun" target="_blank" rel="noopener">GitHub ↗</a>
                <a href="mailto:a.r.kakde@gmail.com">Email ↗</a>
              </div>
            </div>
            <div>
              <p className="ftr__col-label">Read</p>
              <div className="ftr__col">
                <a href="#cortex">Cortex</a>
                <a href="https://notebook.kakde.eu" target="_blank" rel="noopener">Notebook ↗</a>
                <a href="/Aniket-Kakde-CV-EN.pdf">CV (PDF)</a>
              </div>
            </div>
          </div>
        </div>
        <div className="ftr__meta">
          <span>built with scala.js · served from a 4-node k3s cluster in my flat · © {year}</span>
          <span>v2.0 · paris ⌁ {year}</span>
        </div>
      </div>
    </footer>);

}

Object.assign(window, { Header, Hero, SelectedWork, About, Experience, Projects, Cortex, Footer });