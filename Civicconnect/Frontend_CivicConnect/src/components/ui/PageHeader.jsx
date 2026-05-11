import React from 'react';

/**
 * PageHeader — page-top banner used throughout the app.
 *
 * Usage:
 *   <PageHeader title="My Requests" subtitle="Track all your civic complaints" />
 *   <PageHeader title="Compliance Audit" subtitle="..." actions={<button>New</button>} variant="dark" />
 *
 * Props:
 *   title    — main heading (required).
 *   subtitle — optional smaller supporting line.
 *   icon     — optional emoji or character before the title.
 *   actions  — optional ReactNode rendered on the right (buttons, links).
 *   variant  — 'default' | 'dark' | 'subtle' (default 'default').
 */
function PageHeader({ title, subtitle, icon, actions, variant = 'default' }) {
  return (
    <div className={`page-header page-header--${variant}`}>
      <div className="page-header__text">
        <h1 className="page-header__title">
          {icon && <span className="page-header__icon" aria-hidden="true">{icon}</span>}
          {title}
        </h1>
        {subtitle && <p className="page-header__subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="page-header__actions">{actions}</div>}
    </div>
  );
}

export default PageHeader;
