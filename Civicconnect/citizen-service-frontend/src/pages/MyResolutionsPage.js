import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getResolutionsByOfficer } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';

const statusBadge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function MyResolutionsPage() {
  const { user } = useAuth();
  const [resolutions, setResolutions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { const r = await getResolutionsByOfficer(user.userId); setResolutions(r.data); }
      catch { toast.error('Failed to load resolutions.'); }
      finally { setLoading(false); }
    })();
  }, [user.userId]);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  const inProg = resolutions.filter(r => r.status === 'IN_PROGRESS').length;
  const completed = resolutions.filter(r => r.status === 'COMPLETED').length;

  return (
    <>
      <div className="welcome-banner" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #312e81 50%, #4f46e5 100%)' }}>
        <h2>🔧 My Resolutions</h2>
        <p>Manage resolutions and workflow steps for your assigned service requests.</p>
      </div>

      <div className="stats-row">
        <div className="stat-card"><div className="stat-icon icon-blue">📋</div><div><div className="stat-value">{resolutions.length}</div><div className="stat-label">Total</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-orange">🔄</div><div><div className="stat-value">{inProg}</div><div className="stat-label">In Progress</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-green">✅</div><div><div className="stat-value">{completed}</div><div className="stat-label">Completed</div></div></div>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">🔧</span> Resolutions ({resolutions.length})</div>
        {resolutions.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No resolutions yet</h3><p>Create a resolution from an assigned service request.</p></div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Request #</th><th>Actions</th><th>Status</th><th>Created</th><th>Details</th>
          </tr></thead><tbody>
            {resolutions.map(r => (
              <tr key={r.resolutionId}>
                <td style={{fontWeight:700}}>#{r.resolutionId}</td>
                <td><Link to={`/service-requests/${r.requestId}`}>#{r.requestId}</Link></td>
                <td style={{maxWidth:250,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{r.actions}</td>
                <td>{statusBadge(r.status)}</td>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td><Link to={`/resolutions/${r.resolutionId}`} className="btn btn-small btn-secondary">👁️ View</Link></td>
              </tr>
            ))}
          </tbody></table></div>
        )}
      </div>
    </>
  );
}

