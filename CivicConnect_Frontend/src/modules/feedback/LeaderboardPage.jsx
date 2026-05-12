import React, { useState, useEffect } from 'react';
import { getAllSatisfactionMetrics, getSatisfactionMetricByOfficer } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader, StatCard } from '../../components/ui';

const renderStars = (avg) => {
  const full = Math.floor(avg);
  const half = avg - full >= 0.5;
  return '⭐'.repeat(full) + (half ? '⭐' : '') + '☆'.repeat(5 - full - (half ? 1 : 0));
};

const medalIcon = (i) => i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `#${i + 1}`;

export default function LeaderboardPage() {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [myMetric, setMyMetric] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await getAllSatisfactionMetrics().catch(() => ({ data: [] }));
        const data = Array.isArray(res.data) ? res.data : [];
        setMetrics(data.sort((a, b) => (b.averageScore || 0) - (a.averageScore || 0)));
        if (user.role === 'SERVICE_OFFICER') {
          const m = await getSatisfactionMetricByOfficer(user.userId).catch(() => null);
          if (m && m.data) setMyMetric(m.data);
        }
      } catch {
        // silently handle - empty leaderboard
      } finally {
        setLoading(false);
      }
    })();
  }, [user]);

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;

  return (
    <>
      <PageHeader icon="🏆" title="Officer Satisfaction Leaderboard" subtitle="Ranked by average citizen feedback scores across all service requests." />

      {myMetric && (
        <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
          <div className="card-title"><span className="icon icon-blue">👤</span> Your Score</div>
          <div className="stats-row">
            <StatCard icon="⭐" label="Avg Score"     value={myMetric.averageScore?.toFixed(1) || '—'} variant="warning" />
            <StatCard icon="📊" label="Total Reviews" value={myMetric.totalFeedbackCount}              variant="primary" />
            <StatCard
              icon="🏆"
              label="Rank"
              value={`#${metrics.findIndex(m => m.officerUserId === user.userId) + 1 || '—'}`}
              variant="success"
            />
          </div>
        </div>
      )}

      <div className="card">
        <div className="card-title"><span className="icon icon-orange">🏆</span> Leaderboard ({metrics.length} Officers)</div>
        {metrics.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏆</div>
            <h3>No feedback data yet</h3>
            <p>Once citizens submit feedback, officer satisfaction scores will appear here.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead><tr><th>Rank</th><th>Officer</th><th>Avg Score</th><th>Rating</th><th>Reviews</th><th>Last Updated</th></tr></thead>
              <tbody>
                {metrics.map((m, i) => (
                  <tr key={m.metricId} style={m.officerUserId === user.userId ? { background: 'var(--primary-bg)' } : {}}>
                    <td style={{ fontWeight: 800, fontSize: '1.2rem' }}>{medalIcon(i)}</td>
                    <td style={{ fontWeight: 700 }}>{m.officerName} <span style={{ color: 'var(--gray-400)', fontSize: '.85rem' }}>(#{m.officerUserId})</span></td>
                    <td>
                      <span style={{
                        fontWeight: 800, fontSize: '1.1rem',
                        color: m.averageScore >= 4 ? '#059669' : m.averageScore >= 3 ? '#d97706' : '#dc2626'
                      }}>
                        {m.averageScore?.toFixed(1)}
                      </span>
                      <span style={{ color: 'var(--gray-400)', fontSize: '.8rem' }}> / 5</span>
                    </td>
                    <td>{renderStars(m.averageScore)}</td>
                    <td>{m.totalFeedbackCount}</td>
                    <td>{new Date(m.lastUpdatedAt).toLocaleDateString()}</td>
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

