import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById, assignOfficerToRequest } from '../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function AssignOfficerPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [req, setReq] = useState(null);
  const [officerId, setOfficerId] = useState('');
  const [loading, setLoading] = useState(true);
  const [assigning, setAssigning] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try { const r = await getServiceRequestById(requestId); setReq(r.data); }
      catch { toast.error('Failed to load.'); }
      finally { setLoading(false); }
    })();
  }, [requestId]);

  const handleAssign = async (e) => {
    e.preventDefault(); setError(''); setAssigning(true);
    try {
      await assignOfficerToRequest(requestId, Number(officerId));
      toast.success('✅ Officer assigned!');
      navigate('/admin/service-requests');
    } catch (err) {
      setError(err.response?.data?.message || 'Assignment failed.');
    } finally { setAssigning(false); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!req) return <div className="card"><div className="error-msg">Request not found.</div></div>;

  return (
    <div className="card">
      <div className="card-title">
        <span className="icon icon-blue">👤</span> Assign Officer — Request #{req.requestId}
        <span style={{marginLeft:'auto'}}>{badge(req.status)}</span>
      </div>
      <div className="profile-grid" style={{marginBottom:24}}>
        <div className="profile-item"><label>📋 Type</label><div className="value">{req.type}</div></div>
        <div className="profile-item"><label>👤 Citizen</label><div className="value">{req.citizenName}</div></div>
        <div className="profile-item"><label>📍 Location</label><div className="value">{req.location}</div></div>
        <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
      </div>
      <div style={{padding:16,background:'var(--gray-50)',borderRadius:14,border:'1px solid var(--gray-100)',marginBottom:24}}>
        <label style={{fontSize:'.75rem',fontWeight:800,color:'var(--gray-400)',textTransform:'uppercase',letterSpacing:1,display:'block',marginBottom:8}}>Description</label>
        <p style={{color:'var(--gray-700)'}}>{req.description}</p>
      </div>
      {error && <div className="error-msg">⚠️ {error}</div>}
      <div className="info-tip" style={{width:'100%',marginBottom:20}}>
        <span className="tip-icon">💡</span> Enter the <strong>User ID</strong> of a SERVICE_OFFICER to assign this request.
      </div>
      <form onSubmit={handleAssign}>
        <div className="form-group">
          <label>Officer User ID</label>
          <input type="number" value={officerId} onChange={e=>setOfficerId(e.target.value)} placeholder="Enter officer's user ID (e.g., 5)" required min="1" />
        </div>
        <div className="actions-row">
          <button className="btn btn-primary" disabled={assigning||!officerId}>{assigning ? '⏳ Assigning...' : '👤 Assign Officer'}</button>
          <button type="button" className="btn btn-outline" onClick={()=>navigate(-1)}>Cancel</button>
        </div>
      </form>
    </div>
  );
}

