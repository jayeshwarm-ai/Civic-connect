import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById, updateServiceRequestStatus, getServiceRequestUpdates } from '../../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

export default function UpdateServiceRequestPage() {
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
        const [r, u] = await Promise.all([getServiceRequestById(requestId), getServiceRequestUpdates(requestId)]);
        setReq(r.data); setUpdates(u.data);
        if (r.data.status === 'ASSIGNED') setForm(f => ({...f, status: 'IN_PROGRESS'}));
        else if (r.data.status === 'IN_PROGRESS') setForm(f => ({...f, status: 'RESOLVED'}));
      } catch { toast.error('Failed to load.'); }
      finally { setLoading(false); }
    })();
  }, [requestId]);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSaving(true);
    try {
      await updateServiceRequestStatus(requestId, form);
      toast.success('✅ Status updated!');
      navigate('/officer/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Update failed.');
    } finally { setSaving(false); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!req) return <div className="card"><div className="error-msg">Request not found.</div></div>;

  const allowed = req.status === 'ASSIGNED' ? ['IN_PROGRESS']
    : req.status === 'IN_PROGRESS' ? ['RESOLVED']
    : req.status === 'SUBMITTED' ? ['IN_PROGRESS']
    : req.status === 'RESOLVED' ? ['CLOSED']
    : [];

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
          <div className="profile-item"><label>📍 City / State</label><div className="value">{req.city || '—'}{req.state ? `, ${req.state}` : ''}</div></div>
          <div className="profile-item" style={{gridColumn:'1 / -1'}}><label>🏠 Address</label><div className="value">{req.address || '—'}</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
        </div>
        <div style={{padding:16,background:'var(--gray-50)',borderRadius:14,border:'1px solid var(--gray-100)',marginBottom:24}}>
          <label style={{fontSize:'.75rem',fontWeight:800,color:'var(--gray-400)',textTransform:'uppercase',letterSpacing:1,display:'block',marginBottom:8}}>Description</label>
          <p style={{color:'var(--gray-700)'}}>{req.description}</p>
        </div>
        {error && <div className="error-msg">⚠️ {error}</div>}
        {allowed.length === 0 ? (
          <div className="info-tip" style={{width:'100%'}}><span className="tip-icon">ℹ️</span> This request cannot be updated further.</div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="info-tip" style={{width:'100%',marginBottom:20}}>
              <span className="tip-icon">💡</span> Transition: <strong>{req.status}</strong> → <strong>{form.status}</strong>
            </div>
            <div className="form-group">
              <label>New Status</label>
              <select value={form.status} onChange={e=>setForm({...form,status:e.target.value})}>
                {allowed.map(s => <option key={s} value={s}>{s.replace('_',' ')}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Notes / Update Details</label>
              <textarea value={form.notes} onChange={e=>setForm({...form,notes:e.target.value})} rows={4} placeholder="Describe what has been done..." required />
            </div>
            <div className="actions-row">
              <button className="btn btn-primary" disabled={saving}>{saving ? '⏳ Updating...' : '✅ Update Status'}</button>
              <button type="button" className="btn btn-outline" onClick={()=>navigate(-1)}>Cancel</button>
            </div>
          </form>
        )}
      </div>

      {updates.length > 0 && (
        <div className="card">
          <div className="card-title"><span className="icon icon-green">📜</span> Previous Updates</div>
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
        </div>
      )}
    </>
  );
}

