import React from 'react';

/**
 * EmptyState — shown when a list, table or section has no data.
 *
 * Usage:
 *   <EmptyState icon="📋" title="No requests yet" description="You have not raised any complaints." />
 *   <EmptyState icon="🔔" title="All caught up" description="No new notifications." action={<button>Refresh</button>} />
 *
 * Props:
 *   icon        — emoji or character (default '📭').
 *   title       — short headline (required).
 *   description — optional supporting text.
 *   action      — optional ReactNode (usually a button or link).
 */
function EmptyState({ icon = '📭', title, description, action }) {
  return (
    <div className="gv-empty">
      <div className="gv-empty__icon" aria-hidden="true">{icon}</div>
      <h3 className="gv-empty__title">{title}</h3>
      {description && <p className="gv-empty__description">{description}</p>}
      {action && <div className="gv-empty__action">{action}</div>}
    </div>
  );
}

export default EmptyState;
