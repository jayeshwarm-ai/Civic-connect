import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRequestById, updateRequestStatus, getRequestUpdates } from '../../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function UpdateStatusPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [req, setReq] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ status: 'IN_PROGRESS', notes: '' });

  useEffect(() => {
    (async () => {
      try {
        const [r, u] = await Promise.all([getRequestById(requestId), getRequestUpdates(requestId)]);
        setReq(r.data); setUpdates(u.data);
        // Set default next status
        if (r.data.status === 'ASSIGNED') setForm(f => ({...f, status: 'IN_PROGRESS'}));
        else if (r.data.status === 'IN_PROGRESS') setForm(f => ({...f, status: 'RESOLVED'}));
      } catch { toast.error('Failed to load.'); }
      finally { setLoading(false); }
    })();
  }, [requestId]);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSaving(true);
    try {
      await updateRequestStatus(requestId, form);
      toast.success('✅ Status updated!');
      navigate('/officer');
    } catch (err) {
      setError(err.response?.data?.message || 'Update failed.');
    } finally { setSaving(false); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!req) return <div className="card error-msg">Request not found.</div>;

  // Allowed transitions
  const allowedStatuses = req.status === 'ASSIGNED' ? ['IN_PROGRESS'] : req.status === 'IN_PROGRESS' ? ['RESOLVED'] : [];

  return (
    <>
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">✏️</span> Update Request #{req.requestId}
          <span style={{marginLeft:'auto'}}>{badge(req.status)}</span>
        </div>

        <div className="profile-grid" style={{marginBottom:24}}>
          <div className="profile-item"><label>📋 Type</label><div className="value">{req.type}</div></div>
          <div className="profile-item"><label>👤 Citizen</label><div className="value">{req.citizenName}</div></div>
          <div className="profile-item"><label>📍 Location</label><div className="value">{req.location}</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
        </div>

        <div style={{padding:16,background:'#f8fafc',borderRadius:14,border:'1px solid #e2e8f0',marginBottom:24}}>
          <label style={{fontSize:'.75rem',fontWeight:800,color:'#94a3b8',textTransform:'uppercase',letterSpacing:1,display:'block',marginBottom:8}}>Description</label>
          <p style={{color:'#334155'}}>{req.description}</p>
        </div>

        {error && <div className="error-msg">⚠️ {error}</div>}

        {allowedStatuses.length === 0 ? (
          <div className="info-tip" style={{width:'100%'}}>
            <span>ℹ️</span> This request is in <strong>{req.status}</strong> state and cannot be updated further by you.
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="info-tip" style={{width:'100%',marginBottom:20}}>
              <span>💡</span> Transition: <strong>{req.status}</strong> → <strong>{form.status}</strong>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>New Status</label>
                <select value={form.status} onChange={e=>setForm({...form,status:e.target.value})}>
                  {allowedStatuses.map(s=><option key={s} value={s}>{s.replace('_',' ')}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Notes / Update Details</label>
              <textarea value={form.notes} onChange={e=>setForm({...form,notes:e.target.value})} rows={4}
                placeholder="Describe what has been done or findings..." required />
            </div>
            <div className="actions-row">
              <button className="btn btn-primary" disabled={saving}>{saving ? '⏳ Updating...' : '✅ Update Status'}</button>
              <button type="button" className="btn btn-outline" onClick={()=>navigate('/officer')}>Cancel</button>
            </div>
          </form>
        )}
      </div>

      {/* Previous Updates */}
      {updates.length > 0 && (
        <div className="card">
          <div className="card-title"><span className="icon icon-green">📜</span> Previous Updates</div>
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
        </div>
      )}
    </>
  );
}

