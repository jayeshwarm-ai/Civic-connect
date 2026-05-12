import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getServiceRequestsByStatus } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, EmptyState } from '../../components/ui';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

// ── Status-aware empty-state copy ─────────────────────────────────────────
// Each status has its own meaningful message. The default "No <status>
// requests" is too cryptic — "No submitted requests" sounds like nothing
// was ever submitted, when it really means "every submitted request has
// already been assigned" (which is good news, not an error).
const EMPTY_BY_STATUS = {
  SUBMITTED: {
    icon: '✅',
    title: 'Nothing waiting to be assigned',
    description: 'Every newly submitted request has already been picked up by an officer. Great work — check back later when new complaints arrive.',
  },
  ASSIGNED: {
    icon: '🚀',
    title: 'No idle assignments',
    description: 'Every assigned request is already being worked on. Officers are on the move.',
  },
  IN_PROGRESS: {
    icon: '🛠️',
    title: 'Nothing in progress right now',
    description: 'No requests are currently being actively worked on. Either everything is freshly assigned or already resolved.',
  },
  RESOLVED: {
    icon: '🎯',
    title: 'No requests awaiting citizen confirmation',
    description: 'There are no resolved requests waiting for citizens to confirm closure. Once officers mark requests as resolved, they will appear here.',
  },
  CLOSED: {
    icon: '📁',
    title: 'No closed requests yet',
    description: 'No requests have been fully resolved and closed by citizens. Closed requests show the final outcome of the service lifecycle.',
  },
};

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

  const empty = EMPTY_BY_STATUS[status];

  return (
    <>
      <PageHeader icon="📋" title="All Service Requests" subtitle="Filter and manage service requests. Assign officers to submitted requests." />
      <div className="card">
        <div style={{display:'flex',gap:8,marginBottom:24,flexWrap:'wrap'}}>
          {['SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'].map(s => (
            <button key={s} className={`btn btn-small ${status===s?'btn-primary':'btn-outline'}`} onClick={()=>setStatus(s)}>{s.replace('_',' ')}</button>
          ))}
        </div>
        {loading ? <div className="loading"><div className="spinner"></div></div> : requests.length === 0 ? (
          <EmptyState icon={empty.icon} title={empty.title} description={empty.description} />
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>ID</th><th>Citizen</th><th>Type</th><th>Location</th><th>Status</th><th>Officer</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {requests.map(r => (
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

