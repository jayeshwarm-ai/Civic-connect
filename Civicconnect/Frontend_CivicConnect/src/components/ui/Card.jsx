import React from 'react';

/**
 * Card — standard surface for grouped content.
 *
 * Usage:
 *   <Card title="Profile" icon="👤">...</Card>
 *   <Card title="Recent activity" icon="📋" actions={<button>View all</button>}>...</Card>
 *
 * Props:
 *   title    — heading shown at the top of the card.
 *   icon     — optional icon next to the title.
 *   actions  — optional ReactNode rendered on the right of the title row.
 *   variant  — 'default' | 'highlight' | 'flat'  (default 'default').
 *   children — card body content.
 */
function Card({ title, icon, actions, variant = 'default', className = '', children }) {
  return (
    <div className={`gv-card gv-card--${variant} ${className}`.trim()}>
      {(title || actions) && (
        <div className="gv-card__head">
          {title && (
            <h3 className="gv-card__title">
              {icon && <span className="gv-card__icon" aria-hidden="true">{icon}</span>}
              {title}
            </h3>
          )}
          {actions && <div className="gv-card__actions">{actions}</div>}
        </div>
      )}
      <div className="gv-card__body">{children}</div>
    </div>
  );
}

export default Card;
