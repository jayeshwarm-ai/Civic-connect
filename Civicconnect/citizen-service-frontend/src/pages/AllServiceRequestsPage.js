import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getServiceRequestsByStatus } from '../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

export default function AllServiceRequestsPage() {
  const [status, setStatus] = useState('SUBMITTED');
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async (s) => {
    setLoading(true);
    try { const r = await getServiceRequestsByStatus(s); setRequests(r.data); }
    catch { toast.error('Failed to load.'); setRequests([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(status); }, [status]);

  return (
    <>
      <div className="welcome-banner" style={{background:'linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%)'}}>
        <h2>📋 All Service Requests</h2>
        <p>Filter and manage service requests. Assign officers to submitted requests.</p>
      </div>
      <div className="card">
        <div style={{display:'flex',gap:8,marginBottom:24,flexWrap:'wrap'}}>
          {['SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].map(s => (
            <button key={s} className={`btn btn-small ${status===s?'btn-primary':'btn-outline'}`} onClick={()=>setStatus(s)}>{s.replace('_',' ')}</button>
          ))}
        </div>
        {loading ? <div className="loading"><div className="spinner"></div></div> : requests.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No {status.replace('_',' ').toLowerCase()} requests</h3></div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Citizen</th><th>Type</th><th>Location</th><th>Status</th><th>Officer</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {requests.map(r => (
              <tr key={r.requestId}>
                <td style={{fontWeight:700}}>#{r.requestId}</td>
                <td>{r.citizenName}</td>
                <td>{typeIcon(r.type)} {r.type}</td>
                <td style={{maxWidth:150,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{r.location}</td>
                <td>{badge(r.status)}</td>
                <td>{r.assignedOfficerName || <span style={{color:'var(--gray-400)'}}>—</span>}</td>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <Link to={`/service-requests/${r.requestId}`} className="btn btn-small btn-secondary">👁️</Link>
                    {r.status==='SUBMITTED' && <Link to={`/service-requests/${r.requestId}/assign`} className="btn btn-small btn-primary">👤 Assign</Link>}
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

