import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { submitFeedback, getFeedbackByRequestId, getServiceRequestById } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, FieldError } from '../../components/ui';

export default function SubmitFeedbackPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comments, setComments] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [ratingError, setRatingError] = useState('');
  const [commentsError, setCommentsError] = useState('');
  const [request, setRequest] = useState(null);
  const [existing, setExisting] = useState(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [reqRes, fbRes] = await Promise.all([
          getServiceRequestById(requestId).catch(() => null),
          getFeedbackByRequestId(requestId).catch(() => null),
        ]);
        if (reqRes) setRequest(reqRes.data);
        if (fbRes && fbRes.data) setExisting(fbRes.data);
      } catch {}
      finally { setChecking(false); }
    })();
  }, [requestId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    let hasError = false;
    if (rating === 0) { setRatingError('Please select a star rating before submitting.'); hasError = true; }
    else setRatingError('');
    if (comments && comments.length > 500) { setCommentsError('Comments cannot exceed 500 characters.'); hasError = true; }
    else setCommentsError('');
    if (hasError) return;

    setError(''); setLoading(true);
    try {
      await submitFeedback({ requestId: Number(requestId), rating, comments: comments || null });
      toast.success('⭐ Feedback submitted! Thank you!');
      navigate('/feedback');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit feedback.');
    } finally { setLoading(false); }
  };

  if (checking) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;

  const ratingLabels = ['', 'Very Poor', 'Poor', 'Average', 'Good', 'Excellent'];

  return (
    <>
      <PageHeader icon="⭐" title="Submit Feedback" subtitle={<>Rate the service for Request #{requestId}</>} />

      {existing ? (
        <div className="card">
          <div className="card-title"><span className="icon icon-green">✅</span> Feedback Already Submitted</div>
          <div className="profile-grid">
            <div className="profile-item"><label>⭐ Rating</label><div className="value">{'⭐'.repeat(existing.rating)}{'☆'.repeat(5 - existing.rating)} ({existing.rating}/5)</div></div>
            <div className="profile-item"><label>📅 Date</label><div className="value">{new Date(existing.createdAt).toLocaleString()}</div></div>
          </div>
          {existing.comments && (
            <div style={{ marginTop: 16, padding: 20, background: 'var(--gray-50)', borderRadius: 14, border: '1px solid var(--gray-100)' }}>
              <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>💬 Comments</label>
              <p style={{ color: 'var(--gray-700)' }}>{existing.comments}</p>
            </div>
          )}
          <div className="actions-row">
            <button className="btn btn-secondary" onClick={() => navigate('/feedback')}>← Back to Feedback</button>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="card-title"><span className="icon icon-orange">⭐</span> Rate Service Request #{requestId}</div>

          {request && request.status !== 'CLOSED' && (
            <div className="error-msg">⚠️ This service request is not CLOSED yet (status: {request.status}). You can only submit feedback for closed requests.</div>
          )}

          {error && <div className="error-msg">⚠️ {error}</div>}

          <form onSubmit={handleSubmit}>
            {/* Star Rating */}
            <div className="form-group">
              <label>Rating</label>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 }}>
                {[1, 2, 3, 4, 5].map(n => (
                  <button key={n} type="button"
                    onMouseEnter={() => setHoverRating(n)}
                    onMouseLeave={() => setHoverRating(0)}
                    onClick={() => setRating(n)}
                    style={{
                      background: 'none', border: 'none', cursor: 'pointer', fontSize: '2.5rem',
                      transition: 'transform 0.15s ease',
                      transform: (hoverRating || rating) >= n ? 'scale(1.2)' : 'scale(1)',
                      filter: (hoverRating || rating) >= n ? 'none' : 'grayscale(1) opacity(0.4)',
                    }}>
                    ⭐
                  </button>
                ))}
                {(hoverRating || rating) > 0 && (
                  <span style={{ marginLeft: 12, fontWeight: 700, fontSize: '1rem', color: 'var(--dark)' }}>
                    {ratingLabels[hoverRating || rating]} ({hoverRating || rating}/5)
                  </span>
                )}
              </div>
              <FieldError message={ratingError} />
            </div>

            <div className="form-group">
              <label>Comments (Optional)</label>
              <textarea value={comments} onChange={e => { setComments(e.target.value); if (commentsError) setCommentsError(''); }}
                rows={4} placeholder="Share your experience... What went well? What could be improved?"
                className={commentsError ? 'input-error' : ''} />
              <FieldError message={commentsError} />
            </div>

            <div className="actions-row">
              <button className="btn btn-primary" disabled={loading || rating === 0}>
                {loading ? '⏳ Submitting...' : '⭐ Submit Feedback'}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}

