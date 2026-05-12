import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createAuditRecord } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader } from '../../components/ui';

export default function CreateAuditPage() {
  const navigate = useNavigate();
  const [scope, setScope] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await createAuditRecord({ scope });
      toast.success('✅ Audit record created!');
      navigate(`/compliance/audits/${res.data.auditId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create audit.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <PageHeader icon="➕" title="Create Audit" subtitle="Start a new audit to review compliance for a specific scope." />
      <div className="card">
        <div className="card-title"><span className="icon icon-green">🔍</span> New Audit Record</div>
        {error && <div className="error-msg">⚠️ {error}</div>}
        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span>
          Audits follow the lifecycle: <strong>OPEN → IN_REVIEW → CLOSED</strong>. You can add findings during the review phase.
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Audit Scope</label>
            <textarea value={scope} onChange={e => setScope(e.target.value)}
              rows={4} placeholder='e.g. "Road maintenance requests in Jan 2026" or "All resolutions for water department"'
              required />
          </div>
          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading}>{loading ? '⏳ Creating...' : '🔍 Create Audit'}</button>
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </>
  );
}

