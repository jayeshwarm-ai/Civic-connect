import React from 'react';

/**
 * StatCard — reusable dashboard stat tile.
 *
 * Usage:
 *   <StatCard icon="📋" label="Total Requests" value={42} variant="primary" />
 *   <StatCard icon="✅" label="Resolved" value="12 / 18" variant="success" hint="last 30 days" />
 *
 * Props:
 *   icon       — emoji or single character displayed on the left.
 *   label      — short caption for the metric.
 *   value      — main number (string or number).
 *   variant    — colour theme: 'primary' (default) | 'success' | 'warning' | 'danger' | 'info' | 'neutral'.
 *   hint       — optional small helper text under the value.
 *   onClick    — optional click handler; renders the card as a button.
 *   className  — optional extra class names.
 */
function StatCard({ icon, label, value, variant = 'primary', hint, onClick, className = '' }) {
  const isClickable = typeof onClick === 'function';
  const Tag = isClickable ? 'button' : 'div';
  return (
    <Tag
      type={isClickable ? 'button' : undefined}
      onClick={onClick}
      className={`stat-card stat-card--${variant} ${isClickable ? 'stat-card--clickable' : ''} ${className}`.trim()}
    >
      <div className="stat-card__icon" aria-hidden="true">{icon}</div>
      <div className="stat-card__body">
        <div className="stat-card__label">{label}</div>
        <div className="stat-card__value">{value}</div>
        {hint && <div className="stat-card__hint">{hint}</div>}
      </div>
    </Tag>
  );
}

export default StatCard;
