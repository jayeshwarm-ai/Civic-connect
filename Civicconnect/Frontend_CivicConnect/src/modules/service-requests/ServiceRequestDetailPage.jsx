import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById, getServiceRequestUpdates } from '../../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;
const typeIcon = (t) => t==='ROAD'?'🛣️':t==='WATER'?'💧':'⚡';

export default function ServiceRequestDetailPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [req, setReq] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [r, u] = await Promise.all([getServiceRequestById(requestId), getServiceRequestUpdates(requestId)]);
        setReq(r.data); setUpdates(u.data);
      } catch { toast.error('Failed to load request.'); }
      finally { setLoading(false); }
    })();
  }, [requestId]);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!req) return <div className="card"><div className="error-msg">Request not found.</div></div>;

  return (
    <>
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">{typeIcon(req.type)}</span>
          Request #{req.requestId}
          <span style={{marginLeft:'auto'}}>{badge(req.status)}</span>
        </div>
        <div className="profile-grid">
          <div className="profile-item"><label>📋 Type</label><div className="value">{req.type}</div></div>
          <div className="profile-item"><label>👤 Citizen</label><div className="value">{req.citizenName} (#{req.citizenId})</div></div>
          <div className="profile-item"><label>📍 City / State</label><div className="value">{req.city || '—'}{req.state ? `, ${req.state}` : ''}</div></div>
          <div className="profile-item" style={{gridColumn:'1 / -1'}}><label>🏠 Address</label><div className="value">{req.address || '—'}</div></div>
          <div className="profile-item"><label>👮 Officer</label><div className="value">{req.assignedOfficerName || <span style={{color:'var(--gray-400)'}}>Not assigned</span>}</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
          <div className="profile-item"><label>🔄 Updated</label><div className="value">{new Date(req.updatedAt).toLocaleString()}</div></div>
        </div>
        <div style={{marginTop:24,padding:20,background:'var(--gray-50)',borderRadius:14,border:'1px solid var(--gray-100)'}}>
          <label style={{fontSize:'.75rem',fontWeight:800,color:'var(--gray-400)',textTransform:'uppercase',letterSpacing:1,marginBottom:8,display:'block'}}>📝 Description</label>
          <p style={{fontSize:'.95rem',lineHeight:1.7,color:'var(--gray-700)'}}>{req.description}</p>
        </div>
        <div className="actions-row">
          <button className="btn btn-secondary" onClick={()=>navigate(-1)}>← Back</button>
        </div>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-green">📜</span> Update History ({updates.length})</div>
        {updates.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No updates yet</h3><p>Updates will appear here as the request progresses.</p></div>
        ) : (
          <div className="timeline">
            {updates.map((u, i) => (
              <div className={`timeline-item ${i===0?'completed':'active'}`} key={u.updateId}>
                <div className="timeline-dot"></div>
                <div className="timeline-title">{badge(u.status)} — by {u.officerName}</div>
                <div className="timeline-desc">{u.notes}</div>
                <div style={{fontSize:'.75rem',color:'var(--gray-400)',marginTop:2}}>{new Date(u.createdAt).toLocaleString()}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

