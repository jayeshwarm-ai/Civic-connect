import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createResolution } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader } from '../../components/ui';

export default function CreateResolutionPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ requestId: '', actions: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setLoading(true);
    try {
      const res = await createResolution({ requestId: Number(form.requestId), actions: form.actions });
      toast.success('✅ Resolution created!');
      navigate(`/resolutions/${res.data.resolutionId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create resolution.');
    } finally { setLoading(false); }
  };

  return (
    <>
      <PageHeader icon="➕" title="Create Resolution" subtitle="Start resolving an assigned service request by creating a resolution with action plan." />
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">🔧</span> New Resolution</div>
        {error && <div className="error-msg">⚠️ {error}</div>}
        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span> The service request must be assigned to you. Creating a resolution moves it to <strong>IN_PROGRESS</strong>.
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Service Request ID</label>
            <input type="number" value={form.requestId} onChange={e => setForm({ ...form, requestId: e.target.value })}
              placeholder="Enter the request ID (e.g., 1)" required min="1" />
          </div>
          <div className="form-group">
            <label>Resolution Actions / Plan</label>
            <textarea value={form.actions} onChange={e => setForm({ ...form, actions: e.target.value })}
              rows={5} placeholder="Describe the resolution plan and actions to be taken..." required />
          </div>
          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading}>{loading ? '⏳ Creating...' : '🔧 Create Resolution'}</button>
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </>
  );
}

