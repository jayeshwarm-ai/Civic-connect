import React from 'react';

/**
 * ConfirmModal
 * ---
 * A generic Yes/No confirmation popup, styled to match FeedbackPromptModal
 * (same backdrop, tricolour strip, popIn animation). Replaces native
 * window.confirm() calls everywhere in the app.
 *
 * Props:
 *   open         — boolean. Controls visibility.
 *   title        — short heading shown in bold. Default: "Are you sure?"
 *   message      — body text (supports plain string or a node).
 *   confirmLabel — label for the primary action. Default: "Confirm".
 *   cancelLabel  — label for the dismiss action. Default: "Cancel".
 *   variant      — 'danger' | 'primary' | 'success'. Default: 'primary'.
 *                  Controls the colour of the confirm button. Use 'danger'
 *                  for destructive/irreversible actions (deactivate, withdraw).
 *   icon         — single emoji shown above the title. Default: '⚠️'.
 *   loading      — when true, disables both buttons and shows a spinner state
 *                  on the confirm button. Useful while the async action runs.
 *   onConfirm    — called when the user clicks the confirm button.
 *   onClose      — called when the user clicks cancel, the X, or the backdrop.
 *
 * Typical use:
 *   const [pending, setPending] = useState(null);  // null | { run, message, ... }
 *   <ConfirmModal
 *     open={!!pending}
 *     message={pending?.message}
 *     variant={pending?.variant}
 *     onConfirm={async () => { await pending.run(); setPending(null); }}
 *     onClose={() => setPending(null)}
 *   />
 */
export default function ConfirmModal({
  open,
  title = 'Are you sure?',
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'primary',
  icon = '⚠️',
  loading = false,
  onConfirm,
  onClose,
}) {
  if (!open) return null;

  // Map variant → btn class so we lean on the existing gov-theme button styles
  const confirmBtnClass =
    variant === 'danger'  ? 'btn btn-danger'  :
    variant === 'success' ? 'btn btn-success' :
                            'btn btn-primary';

  // Dismiss only on a true backdrop click, never when the user clicks inside the dialog.
  // Also block dismissal while a confirm action is in flight.
  const handleBackdropClick = () => { if (!loading) onClose(); };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-modal-title"
      onClick={handleBackdropClick}
      style={{
        position: 'fixed', inset: 0, zIndex: 1000,
        background: 'rgba(15, 23, 42, 0.55)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: 16,
        animation: 'cm-fadeIn 0.2s ease-out',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: '#fff', borderRadius: 10,
          maxWidth: 460, width: '100%',
          boxShadow: '0 20px 60px rgba(11, 61, 145, 0.25)',
          overflow: 'hidden',
          animation: 'cm-popIn 0.25s ease-out',
        }}
      >
        {/* Tricolour strip (matches FeedbackPromptModal) */}
        <div
          aria-hidden="true"
          style={{
            height: 4,
            background: 'linear-gradient(to right, #FF9933 33%, #fff 33%, #fff 66%, #138808 66%)',
          }}
        />

        <div style={{ padding: '28px 28px 24px', textAlign: 'center' }}>
          <div style={{ fontSize: '2.6rem', marginBottom: 6 }} aria-hidden="true">{icon}</div>

          <h2
            id="confirm-modal-title"
            style={{
              fontFamily: 'Merriweather, Georgia, serif',
              fontSize: '1.3rem', fontWeight: 800,
              color: '#0B3D91', margin: '0 0 10px',
            }}
          >
            {title}
          </h2>

          {message && (
            <p style={{ color: '#5B6478', fontSize: '0.95rem', lineHeight: 1.55, margin: '0 0 22px' }}>
              {message}
            </p>
          )}

          <div style={{
            display: 'flex', gap: 10, justifyContent: 'center', flexWrap: 'wrap',
          }}>
            <button
              onClick={onConfirm}
              disabled={loading}
              className={confirmBtnClass}
              style={{ minWidth: 140 }}
            >
              {loading ? '⏳ Working…' : confirmLabel}
            </button>
            <button
              onClick={onClose}
              disabled={loading}
              className="btn btn-outline"
              style={{ minWidth: 110 }}
            >
              {cancelLabel}
            </button>
          </div>
        </div>
      </div>

      {/* Inline keyframes — keeps the component self-contained.
          Prefixed with cm- so they don't collide with FeedbackPromptModal's. */}
      <style>{`
        @keyframes cm-fadeIn { from { opacity: 0 } to { opacity: 1 } }
        @keyframes cm-popIn  { from { transform: scale(0.92); opacity: 0 } to { transform: scale(1); opacity: 1 } }
      `}</style>
    </div>
  );
}
