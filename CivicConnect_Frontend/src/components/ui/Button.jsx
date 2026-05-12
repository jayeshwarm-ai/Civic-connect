import React from 'react';

/**
 * Button — small wrapper around the existing .btn classes for clarity in JSX.
 *
 * Usage:
 *   <Button variant="primary" onClick={save}>Save</Button>
 *   <Button variant="outline" size="small">Cancel</Button>
 *
 * Props:
 *   variant  — 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost'  (default 'primary').
 *   size     — 'small' | undefined  (default normal).
 *   block    — full-width.
 *   disabled — passes through to <button>.
 *   type     — passes through to <button> (default 'button').
 *   icon     — optional emoji/icon shown before children.
 *   onClick  — passes through.
 *   children — button label.
 */
function Button({
  variant = 'primary', size, block = false, disabled = false,
  type = 'button', icon, onClick, className = '', children, ...rest
}) {
  const classes = [
    'btn',
    `btn-${variant}`,
    size === 'small' ? 'btn-small' : '',
    block ? 'btn-block' : '',
    className,
  ].filter(Boolean).join(' ');

  return (
    <button type={type} className={classes} onClick={onClick} disabled={disabled} {...rest}>
      {icon && <span className="btn__icon" aria-hidden="true">{icon}</span>}
      {children}
    </button>
  );
}

export default Button;
