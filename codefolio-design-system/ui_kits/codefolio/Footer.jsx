/* Footer.jsx — "Let's talk." block + two link columns (FIND ME / READ) +
   meta strip with homelab one-liner and version stamp. */

const Footer = () => {
  const year = new Date().getFullYear();
  return (
    <footer id="contact" className="footer">
      <div className="footer__inner">
        <div className="footer__lede">
          <h2 className="footer__name">Let's talk.</h2>
          <p className="footer__sub">
            Senior backend roles, JVM ecosystems, identity and platforms. Reach me at{" "}
            <a className="footer__sub-link" href="mailto:a.r.kakde@gmail.com">a.r.kakde@gmail.com</a>.
          </p>
        </div>
        <div className="footer__cols">
          <div className="footer__col">
            <div className="footer__col-label">Find me</div>
            <div className="footer__col-list">
              <a className="footer__col-link" href="https://www.linkedin.com/in/aniketkakde/"
                 rel="noopener noreferrer" target="_blank">LinkedIn<span className="footer__col-link-arrow"> ↗</span></a>
              <a className="footer__col-link" href="https://github.com/ani2fun"
                 rel="noopener noreferrer" target="_blank">GitHub<span className="footer__col-link-arrow"> ↗</span></a>
              <a className="footer__col-link" href="mailto:a.r.kakde@gmail.com">Email</a>
            </div>
          </div>
          <div className="footer__col">
            <div className="footer__col-label">Read</div>
            <div className="footer__col-list">
              <a className="footer__col-link" href="/cortex">Cortex</a>
              <a className="footer__col-link" href="/blogs">Blog</a>
              <a className="footer__col-link" href="../../assets/Aniket-Kakde-CV-EN.pdf">CV (PDF)</a>
            </div>
          </div>
        </div>
      </div>
      <div className="footer__meta">
        <span className="footer__meta-credit">
          built with scala.js · served from a 4-node k3s cluster in my flat · © {year}
        </span>
        <div className="footer__meta-right">
          <span className="footer__meta-version">v2.0 · paris · {year}</span>
        </div>
      </div>
    </footer>
  );
};

window.Footer = Footer;
