import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRequestById, getRequestUpdates } from '../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function RequestDetailPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [req, setReq] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [r, u] = await Promise.all([getRequestById(requestId), getRequestUpdates(requestId)]);
        setReq(r.data); setUpdates(u.data);
      } catch (e) { toast.error('Failed to load request.'); }
      finally { setLoading(false); }
    })();
  }, [requestId]);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!req) return <div className="card error-msg">Request not found.</div>;

  return (
    <>
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">{req.type==='ROAD'?'🛣️':req.type==='WATER'?'💧':'⚡'}</span>
          Request #{req.requestId}
          <span style={{marginLeft:'auto'}}>{badge(req.status)}</span>
        </div>

        <div className="profile-grid">
          <div className="profile-item"><label>📋 Type</label><div className="value">{req.type}</div></div>
          <div className="profile-item"><label>👤 Citizen</label><div className="value">{req.citizenName} (#{req.citizenId})</div></div>
          <div className="profile-item"><label>📍 Location</label><div className="value">{req.location}</div></div>
          <div className="profile-item"><label>👮 Assigned Officer</label><div className="value">{req.assignedOfficerName || <span style={{color:'#94a3b8'}}>Not assigned</span>}</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
          <div className="profile-item"><label>🔄 Last Updated</label><div className="value">{new Date(req.updatedAt).toLocaleString()}</div></div>
        </div>

        <div style={{marginTop:24,padding:20,background:'#f8fafc',borderRadius:14,border:'1px solid #e2e8f0'}}>
          <label style={{fontSize:'.75rem',fontWeight:800,color:'#94a3b8',textTransform:'uppercase',letterSpacing:1,marginBottom:8,display:'block'}}>📝 Description</label>
          <p style={{fontSize:'.95rem',lineHeight:1.7,color:'#334155'}}>{req.description}</p>
        </div>

        <div className="actions-row">
          <button className="btn btn-secondary" onClick={()=>navigate(-1)}>← Back</button>
        </div>
      </div>

      {/* Update History / Timeline */}
      <div className="card">
        <div className="card-title"><span className="icon icon-green">📜</span> Update History ({updates.length})</div>
        {updates.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📭</div><h3>No updates yet</h3><p>Updates will appear here as the request progresses.</p></div>
        ) : (
          <div className="timeline">
            {updates.map((u, i) => (
              <div className={`timeline-item ${i === 0 ? 'done' : ''}`} key={u.updateId}>
                <div className="dot"></div>
                <div className="t-title">{badge(u.status)} — by {u.officerName}</div>
                <div className="t-desc">{u.notes}</div>
                <div className="t-time">{new Date(u.createdAt).toLocaleString()}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

