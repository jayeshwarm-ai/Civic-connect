import React from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * FeedbackPromptModal
 * ---
 * A small popup that nudges the citizen to share feedback right after they
 * close a service request. The user can either:
 *   • "Share Feedback" — navigates to the feedback submission page for this
 *     specific request (the existing /feedback/submit/:requestId route).
 *   • "Maybe later"    — dismisses the popup; request stays closed.
 *
 * Props:
 *   open       — boolean. Controls visibility.
 *   requestId  — the closed request's ID. Used to build the feedback URL.
 *   onClose    — function called when the user dismisses the popup
 *                (close-X, "Maybe later", or backdrop click).
 */
export default function FeedbackPromptModal({ open, requestId, onClose }) {
  const navigate = useNavigate();

  if (!open) return null;

  const goToFeedback = () => {
    onClose();
    navigate(`/feedback/submit/${requestId}`);
  };

  return (
    // Backdrop — clicking outside the modal dismisses it
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0, zIndex: 1000,
        background: 'rgba(15, 23, 42, 0.55)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: 16,
        animation: 'fadeIn 0.2s ease-out',
      }}
    >
      {/* Stop click propagation so the modal itself doesn't dismiss */}
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: '#fff', borderRadius: 10,
          maxWidth: 460, width: '100%',
          boxShadow: '0 20px 60px rgba(11, 61, 145, 0.25)',
          overflow: 'hidden',
          animation: 'popIn 0.25s ease-out',
        }}
      >
        {/* Tricolour strip echoes the gov.in style */}
        <div
          aria-hidden="true"
          style={{
            height: 4,
            background: 'linear-gradient(to right, #FF9933 33%, #fff 33%, #fff 66%, #138808 66%)',
          }}
        />

        <div style={{ padding: '32px 28px 24px', textAlign: 'center' }}>
          <div style={{ fontSize: '3rem', marginBottom: 8 }} aria-hidden="true">🙏</div>

          <h2 style={{
            fontFamily: 'Merriweather, Georgia, serif',
            fontSize: '1.4rem', fontWeight: 800,
            color: '#0B3D91', margin: '0 0 8px',
          }}>
            Thank you for closing the request!
          </h2>

          <p style={{ color: '#5B6478', fontSize: '0.95rem', lineHeight: 1.55, margin: '0 0 20px' }}>
            Your feedback helps us understand what worked and what we can improve.
            It only takes a minute — would you like to share it now?
          </p>

          <div style={{
            display: 'flex', gap: 10, justifyContent: 'center', flexWrap: 'wrap',
          }}>
            <button
              onClick={goToFeedback}
              className="btn btn-primary"
              style={{ minWidth: 170 }}
            >
              ⭐ Share Feedback
            </button>
            <button
              onClick={onClose}
              className="btn btn-outline"
              style={{ minWidth: 130 }}
            >
              Maybe later
            </button>
          </div>

          <p style={{ marginTop: 16, fontSize: '0.78rem', color: '#94A3B8' }}>
            You can always submit feedback from the Feedback section later.
          </p>
        </div>
      </div>

      {/* Inline keyframes — keeps the component self-contained */}
      <style>{`
        @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
        @keyframes popIn  { from { transform: scale(0.92); opacity: 0 } to { transform: scale(1); opacity: 1 } }
      `}</style>
    </div>
  );
}
