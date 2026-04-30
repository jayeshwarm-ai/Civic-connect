import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getRequestsByOfficer } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function OfficerDashboardPage() {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    (async () => {
      try { const r = await getRequestsByOfficer(user.userId); setRequests(r.data); }
      catch (e) { toast.error('Failed to load assignments.'); }
      finally { setLoading(false); }
    })();
  }, [user.userId]);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  const filtered = filter === 'ALL' ? requests : requests.filter(r => r.status === filter);
  const assigned = requests.filter(r=>r.status==='ASSIGNED').length;
  const inProg = requests.filter(r=>r.status==='IN_PROGRESS').length;
  const resolved = requests.filter(r=>r.status==='RESOLVED').length;

  return (
    <>
      <div className="welcome-banner">
        <h2>👮 Officer Dashboard</h2>
        <p>Welcome, {user?.name}. View and update your assigned service requests.</p>
      </div>

      <div className="stats-row">
        <div className="stat-card"><div className="stat-icon icon-blue">📋</div><div><div className="stat-value">{requests.length}</div><div className="stat-label">Total Assigned</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-orange">📝</div><div><div className="stat-value">{assigned}</div><div className="stat-label">Assigned</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-blue">🔄</div><div><div className="stat-value">{inProg}</div><div className="stat-label">In Progress</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-green">✅</div><div><div className="stat-value">{resolved}</div><div className="stat-label">Resolved</div></div></div>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📋</span> My Assignments ({filtered.length})</div>
        <div style={{display:'flex',gap:8,marginBottom:20,flexWrap:'wrap'}}>
          {['ALL','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].map(s=>(
            <button key={s} className={`btn btn-small ${filter===s?'btn-primary':'btn-outline'}`} onClick={()=>setFilter(s)}>{s.replace('_',' ')}</button>
          ))}
        </div>

        {filtered.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No requests found</h3></div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Citizen</th><th>Type</th><th>Location</th><th>Status</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {filtered.map(r=>(
              <tr key={r.requestId}>
                <td style={{fontWeight:700}}>#{r.requestId}</td>
                <td>{r.citizenName}</td>
                <td>{r.type==='ROAD'?'🛣️':r.type==='WATER'?'💧':'⚡'} {r.type}</td>
                <td style={{maxWidth:150,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{r.location}</td>
                <td>{badge(r.status)}</td>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <Link to={`/requests/${r.requestId}`} className="btn btn-small btn-secondary">👁️</Link>
                    {(r.status==='ASSIGNED'||r.status==='IN_PROGRESS')&&
                      <Link to={`/officer/update/${r.requestId}`} className="btn btn-small btn-primary">✏️ Update</Link>
                    }
                  </div>
                </td>
              </tr>
            ))}
          </tbody></table></div>
        )}
      </div>
    </>
  );
}

