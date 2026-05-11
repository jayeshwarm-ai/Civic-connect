import React from 'react';

/**
 * Inline validation message displayed under (or above) an input.
 *
 * Renders nothing when there's no error, so it's safe to drop in next to
 * every form field without conditional wrappers in the parent.
 *
 * Usage:
 *   <input ... />
 *   <FieldError message={errors.address} />
 */
export default function FieldError({ message }) {
  if (!message) return null;
  return (
    <div className="field-error" role="alert" aria-live="polite">
      <span className="field-error__icon" aria-hidden="true">⚠️</span>
      <span>{message}</span>
    </div>
  );
}
