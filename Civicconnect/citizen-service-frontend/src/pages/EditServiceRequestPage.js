import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById } from '../services/api';
import api from '../services/api';
import { toast } from 'react-toastify';

export default function EditServiceRequestPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ type: '', description: '', location: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const res = await getServiceRequestById(requestId);
        if (res.data.status !== 'SUBMITTED') { toast.error('Only SUBMITTED requests can be edited.'); navigate('/service-requests'); return; }
        setForm({ type: res.data.type, description: res.data.description, location: res.data.location });
      } catch { toast.error('Failed to load request.'); }
      finally { setLoading(false); }
    })();
  }, [requestId, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSaving(true);
    try {
      await api.put(`/api/v1/service-requests/${requestId}`, form);
      toast.success('✅ Request updated!'); navigate('/service-requests');
    } catch (err) { setError(err.response?.data?.message || 'Update failed.'); }
    finally { setSaving(false); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  return (
    <>
      <div className="welcome-banner" style={{background:'linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%)'}}>
        <h2>✏️ Edit Service Request #{requestId}</h2>
        <p>Update your submitted service request details.</p>
      </div>
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">✏️</span> Edit Request</div>
        {error && <div className="error-msg">⚠️ {error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Request Type</label>
              <select value={form.type} onChange={e=>setForm({...form,type:e.target.value})}>
                <option value="ROAD">🛣️ Road</option><option value="WATER">💧 Water</option><option value="ELECTRICITY">⚡ Electricity</option>
              </select>
            </div>
            <div className="form-group">
              <label>Location</label>
              <input value={form.location} onChange={e=>setForm({...form,location:e.target.value})} required />
            </div>
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea value={form.description} onChange={e=>setForm({...form,description:e.target.value})} rows={4} required />
          </div>
          <div className="actions-row">
            <button className="btn btn-primary" disabled={saving}>{saving ? '⏳ Saving...' : '💾 Save Changes'}</button>
            <button type="button" className="btn btn-outline" onClick={()=>navigate('/service-requests')}>Cancel</button>
          </div>
        </form>
      </div>
    </>
  );
}
