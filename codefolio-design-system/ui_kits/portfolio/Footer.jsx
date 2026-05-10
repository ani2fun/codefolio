// Footer — Made with ♥ + brand icons.
const PortfolioFooter = () => (
  <footer className="pf-footer">
    <div className="pf-footer__row">
      <p className="pf-footer__made">
        Made with <svg className="pf-footer__heart" viewBox="0 0 24 24" style={{fill: "currentColor"}}><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> in Paris.
      </p>
      <div className="pf-footer__icons">
        <a href="#" aria-label="GitHub"><window.GitHubIcon size={28} /></a>
        <a href="#" aria-label="LinkedIn"><window.LinkedInIcon size={28} /></a>
      </div>
    </div>
    <p className="pf-footer__copyright">© 2026 Aniket Kakde · Self-hosted on a homelab K3s cluster.</p>
  </footer>
);
window.PortfolioFooter = PortfolioFooter;
