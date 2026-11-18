import Icon from '../components/Icon.jsx';

/** Split-screen layout shared by the Login and Registration pages. */
export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="auth-page">
      <section className="auth-hero" aria-hidden="true">
        <div className="auth-hero-inner">
          <div className="brand-logo brand-logo-lg">
            <Icon name="graduation" size={30} />
          </div>
          <h2>StudentHub</h2>
          <p>A simple, secure student management system built with React, Spring Boot and PostgreSQL.</p>
          <ul className="auth-hero-list">
            <li><Icon name="check" size={16} /> Manage student records in one place</li>
            <li><Icon name="check" size={16} /> Live dashboard from your database</li>
            <li><Icon name="check" size={16} /> Secure cookie-based authentication</li>
          </ul>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-card">
          <div className="auth-mobile-brand">
            <span className="brand-logo"><Icon name="graduation" size={22} /></span>
            <strong>StudentHub</strong>
          </div>
          <h1 className="auth-title">{title}</h1>
          <p className="auth-subtitle">{subtitle}</p>
          {children}
        </div>
      </section>
    </div>
  );
}
