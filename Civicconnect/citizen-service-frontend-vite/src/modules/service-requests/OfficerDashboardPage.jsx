import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getServiceRequestsByOfficer } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader, StatCard } from '../../components/ui';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

export default function OfficerDashboardPage() {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    (async () => {
      try { const r = await getServiceRequestsByOfficer(user.userId); setRequests(r.data); }
      catch { toast.error('Failed to load assignments.'); }
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
      <PageHeader icon="👮" title="My Assigned Requests" subtitle={<>Welcome, {user?.name}. View and update your assigned service requests.</>} />

      <div className="stats-row">
        <StatCard icon="📋" label="Total"       value={requests.length} variant="primary" />
        <StatCard icon="📝" label="Assigned"    value={assigned}        variant="warning" />
        <StatCard icon="🔄" label="In Progress" value={inProg}          variant="info"    />
        <StatCard icon="✅" label="Resolved"    value={resolved}        variant="success" />
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📋</span> Assignments ({filtered.length})</div>
        <div style={{display:'flex',gap:8,marginBottom:20,flexWrap:'wrap'}}>
          {['ALL','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].map(s => (
            <button key={s} className={`btn btn-small ${filter===s?'btn-primary':'btn-outline'}`} onClick={()=>setFilter(s)}>{s.replace('_',' ')}</button>
          ))}
        </div>
        {filtered.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No requests found</h3></div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Citizen</th><th>Type</th><th>Location</th><th>Status</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {filtered.map(r => (
              <tr key={r.requestId}>
                <td style={{fontWeight:700}}>#{r.requestId}</td>
                <td>{r.citizenName}</td>
                <td>{typeIcon(r.type)} {r.type}</td>
                <td
                  style={{maxWidth:200,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}
                  title={`${r.address || ''}${r.address ? ', ' : ''}${r.city || ''}, ${r.state || ''}`.trim()}
                >
                  {r.city || '—'}{r.state ? `, ${r.state}` : ''}
                </td>
                <td>{badge(r.status)}</td>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <Link to={`/service-requests/${r.requestId}`} className="btn btn-small btn-secondary">👁️</Link>
                    {r.status==='ASSIGNED' &&
                      <Link to="/resolutions/create" className="btn btn-small btn-success">🔧 Resolve</Link>}
                    {(r.status==='ASSIGNED'||r.status==='IN_PROGRESS') &&
                      <Link to={`/service-requests/${r.requestId}/update`} className="btn btn-small btn-primary">✏️ Update</Link>}
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

