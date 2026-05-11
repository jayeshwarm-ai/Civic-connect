import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getMyServiceRequests, closeServiceRequest, withdrawServiceRequest } from '../../services/api';
import { PageHeader, StatCard, FeedbackPromptModal } from '../../components/ui';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

const STATUS_META = {
  SUBMITTED:   { icon: '📝', variant: 'primary' },
  ASSIGNED:    { icon: '👤', variant: 'info'    },
  IN_PROGRESS: { icon: '🔄', variant: 'warning' },
  RESOLVED:    { icon: '✅', variant: 'success' },
  CLOSED:      { icon: '📁', variant: 'neutral' },
};

export default function MyServiceRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [filter, setFilter]     = useState('ALL');

  // Tracks which request just got closed so we can show the feedback prompt.
  // null = modal hidden; a numeric requestId = show prompt for that request.
  const [closedRequestId, setClosedRequestId] = useState(null);

  const fetchData = async () => {
    try { const r = await getMyServiceRequests(); setRequests(r.data); }
    catch {} finally { setLoading(false); }
  };
  useEffect(() => { fetchData(); }, []);

  const handleClose = async (id) => {
    if (!window.confirm('Confirm closing this resolved request?')) return;
    try {
      await closeServiceRequest(id);
      toast.success('Request closed!');
      // Open the feedback prompt for this specific request.
      // The user can either share feedback now or dismiss the prompt.
      setClosedRequestId(id);
      fetchData();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed');
    }
  };
  const handleWithdraw = async (id) => {
    if (!window.confirm('Withdraw this request? This cannot be undone.')) return;
    try { await withdrawServiceRequest(id); toast.success('Request withdrawn.'); fetchData(); }
    catch (e) { toast.error(e.response?.data?.message || 'Failed'); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  const filtered = filter === 'ALL' ? requests : requests.filter(r => r.status === filter);
  const counts = {};
  Object.keys(STATUS_META).forEach(s => counts[s] = requests.filter(r => r.status === s).length);

  // Toggle filter on click — clicking the active stat clears the filter back to ALL.
  const toggleFilter = (status) => setFilter(filter === status ? 'ALL' : status);

  return (
    <>
      <PageHeader icon="📋" title="My Service Requests" subtitle="Track and manage all your submitted service requests." />

      <div style={{display:'flex',justifyContent:'flex-end',marginBottom:16}}>
        <Link to="/service-requests/new" className="btn btn-primary">➕ Submit New Request</Link>
      </div>

      <div className="stats-row">
        {Object.entries(counts).map(([status, count]) => {
          const meta = STATUS_META[status];
          return (
            <StatCard
              key={status}
              icon={meta.icon}
              label={status.replace('_', ' ')}
              value={count}
              variant={meta.variant}
              onClick={() => toggleFilter(status)}
              className={filter === status ? 'stat-card--active' : ''}
            />
          );
        })}
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
            <h3>{requests.length === 0 ? 'No requests yet' : `No ${filter === 'ALL' ? '' : filter.replace('_', ' ').toLowerCase() + ' '}requests`}</h3>
            <p>{requests.length === 0
              ? 'You haven\'t submitted any service requests yet. Use the button above to get started.'
              : 'No requests match this filter. Try a different status or click "ALL" to see everything.'
            }</p>
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

      {/* Feedback prompt — shown right after a citizen successfully closes a request.
          The modal manages its own visibility via the open prop; we just clear
          closedRequestId on dismiss to hide it. */}
      <FeedbackPromptModal
        open={closedRequestId !== null}
        requestId={closedRequestId}
        onClose={() => setClosedRequestId(null)}
      />
    </>
  );
}

