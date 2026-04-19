import './SiteFooter.css';

/**
 * Optional donation link from `VITE_DONATE_URL` (see `.env.example`).
 * Renders nothing if unset.
 */
export default function SiteFooter() {
  const donateUrl = (import.meta.env.VITE_DONATE_URL ?? '').trim();

  if (!donateUrl) {
    return null;
  }

  return (
    <footer className="site-footer">
      <div className="site-footer-inner">
        <nav className="site-footer-links" aria-label="Support">
          <a href={donateUrl} target="_blank" rel="noopener noreferrer">
            Support the project
          </a>
        </nav>
      </div>
    </footer>
  );
}
