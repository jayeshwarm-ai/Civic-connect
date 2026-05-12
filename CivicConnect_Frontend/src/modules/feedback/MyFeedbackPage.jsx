import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFeedbacksByCitizen, getMyServiceRequests, getFeedbackByRequestId } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader, StatCard } from '../../components/ui';

const stars = (n) => '⭐'.repeat(n) + '☆'.repeat(5 - n);

export default function MyFeedbackPage() {
  const { user } = useAuth();
  const [feedbacks, setFeedbacks] = useState([]);
  const [closedRequests, setClosedRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const reqRes = await getMyServiceRequests().catch(() => ({ data: [] }));
        const allRequests = reqRes.data || [];

        // Try to get feedbacks by citizen userId first
        let allFeedbacks = [];
        try {
          const fbRes = await getFeedbacksByCitizen(user.userId);
          allFeedbacks = fbRes.data || [];
        } catch {
          // Fallback: fetch feedback for each request individually
          for (const r of allRequests.filter(r => r.status === 'CLOSED' || r.status === 'RESOLVED')) {
            try {
              const fb = await getFeedbackByRequestId(r.requestId);
              if (fb.data) {
                const fbArr = Array.isArray(fb.data) ? fb.data : [fb.data];
                allFeedbacks.push(...fbArr);
              }
            } catch { /* no feedback for this request */ }
          }
        }

        setFeedbacks(allFeedbacks);
        const feedbackRequestIds = new Set(allFeedbacks.map(f => f.requestId));
        setClosedRequests(
          allRequests.filter(r => r.status === 'CLOSED' && !feedbackRequestIds.has(r.requestId))
        );
      } catch {
        toast.error('Failed to load data.');
      } finally {
        setLoading(false);
      }
    })();
  }, [user.userId]);

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;

  return (
    <>
      <PageHeader icon="⭐" title="My Feedback" subtitle="Rate your experience with resolved service requests and help us improve." />

      <div className="stats-row">
        <StatCard icon="⭐" label="Submitted"          value={feedbacks.length}      variant="warning" />
        <StatCard icon="📋" label="Awaiting Feedback" value={closedRequests.length} variant="primary" />
        <StatCard
          icon="📊"
          label="Avg Rating"
          value={feedbacks.length > 0
            ? (feedbacks.reduce((s, f) => s + f.rating, 0) / feedbacks.length).toFixed(1)
            : '—'}
          variant="success"
        />
      </div>

      {/* Pending feedback */}
      {closedRequests.length > 0 && (
        <div className="card">
          <div className="card-title"><span className="icon icon-orange">📝</span> Requests Awaiting Feedback ({closedRequests.length})</div>
          <div className="info-tip" style={{ width: '100%', marginBottom: 16 }}>
            <span className="tip-icon">💡</span>
            These closed requests don't have feedback yet. Share your experience!
          </div>
          <div className="table-container">
            <table>
              <thead><tr><th>Request #</th><th>Category</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {closedRequests.map(r => (
                  <tr key={r.requestId}>
                    <td style={{ fontWeight: 700 }}>#{r.requestId}</td>
                    <td>{r.category || '—'}</td>
                    <td><span className="badge badge-closed">CLOSED</span></td>
                    <td><Link to={`/feedback/submit/${r.requestId}`} className="btn btn-small btn-primary">⭐ Give Feedback</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Past feedback */}
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">⭐</span> My Submitted Feedback ({feedbacks.length})</div>
        {feedbacks.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">⭐</div>
            <h3>No feedback submitted yet</h3>
            <p>Once your service requests are closed, you can rate the service you received.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead><tr><th>ID</th><th>Request #</th><th>Rating</th><th>Comments</th><th>Date</th></tr></thead>
              <tbody>
                {feedbacks.map(f => (
                  <tr key={f.feedbackId}>
                    <td style={{ fontWeight: 700 }}>#{f.feedbackId}</td>
                    <td><Link to={`/service-requests/${f.requestId}`}>#{f.requestId}</Link></td>
                    <td style={{ fontSize: '1.1rem' }}>{stars(f.rating)}</td>
                    <td style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {f.comments || <span style={{ color: 'var(--gray-400)' }}>—</span>}
                    </td>
                    <td>{new Date(f.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

