import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getMyServiceRequests, closeServiceRequest, withdrawServiceRequest } from '../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

export default function MyServiceRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  const fetchData = async () => {
    try { const r = await getMyServiceRequests(); setRequests(r.data); }
    catch {} finally { setLoading(false); }
  };
  useEffect(() => { fetchData(); }, []);

  const handleClose = async (id) => {
    if (!window.confirm('Confirm closing this resolved request?')) return;
    try { await closeServiceRequest(id); toast.success('Request closed!'); fetchData(); }
    catch (e) { toast.error(e.response?.data?.message || 'Failed'); }
  };
  const handleWithdraw = async (id) => {
    if (!window.confirm('Withdraw this request? This cannot be undone.')) return;
    try { await withdrawServiceRequest(id); toast.success('Request withdrawn.'); fetchData(); }
    catch (e) { toast.error(e.response?.data?.message || 'Failed'); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  const filtered = filter === 'ALL' ? requests : requests.filter(r => r.status === filter);
  const counts = {};
  ['SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].forEach(s => counts[s] = requests.filter(r=>r.status===s).length);

  return (
    <>
      <div className="welcome-banner">
        <h2>📋 My Service Requests</h2>
        <p>Track and manage all your submitted service requests.</p>
      </div>

      <div className="stats-row">
        {Object.entries(counts).map(([s, c]) => (
          <div className="stat-card" key={s} style={{cursor:'pointer', border: filter===s ? '2px solid var(--primary)' : undefined}}
            onClick={() => setFilter(filter===s ? 'ALL' : s)}>
            <div className={`stat-icon ${s==='RESOLVED'||s==='CLOSED'?'icon-green':'icon-blue'}`}>
              {s==='SUBMITTED'?'📝':s==='ASSIGNED'?'👤':s==='IN_PROGRESS'?'🔄':s==='RESOLVED'?'✅':'📁'}
            </div>
            <div><div className="stat-value">{c}</div><div className="stat-label">{s.replace('_',' ')}</div></div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📋</span> Requests ({filtered.length})</div>
        <div style={{display:'flex',gap:8,marginBottom:20,flexWrap:'wrap'}}>
          {['ALL','SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].map(s => (
            <button key={s} className={`btn btn-small ${filter===s?'btn-primary':'btn-outline'}`} onClick={()=>setFilter(s)}>{s.replace('_',' ')}</button>
          ))}
        </div>

        {filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📭</div>
            <h3>No requests found</h3>
            <p><Link to="/service-requests/new" className="btn btn-primary" style={{marginTop:16}}>➕ Submit New Request</Link></p>
          </div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Type</th><th>Description</th><th>Status</th><th>Officer</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {filtered.map(r => (
              <tr key={r.requestId}>
                <td style={{fontWeight:700}}>#{r.requestId}</td>
                <td>{typeIcon(r.type)} {r.type}</td>
                <td style={{maxWidth:200,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{r.description}</td>
                <td>{badge(r.status)}</td>
                <td>{r.assignedOfficerName || <span style={{color:'var(--gray-400)',fontStyle:'italic'}}>Unassigned</span>}</td>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <Link to={`/service-requests/${r.requestId}`} className="btn btn-small btn-secondary">👁️</Link>
                    {r.status==='SUBMITTED' && <Link to={`/service-requests/${r.requestId}/edit`} className="btn btn-small btn-primary">✏️ Edit</Link>}
                    {r.status==='RESOLVED' && <button className="btn btn-small btn-success" onClick={()=>handleClose(r.requestId)}>✓ Close</button>}
                    {r.status==='SUBMITTED' && <button className="btn btn-small btn-danger" onClick={()=>handleWithdraw(r.requestId)}>🗑️</button>}
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

