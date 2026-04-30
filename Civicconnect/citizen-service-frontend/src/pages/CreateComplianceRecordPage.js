import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { createComplianceRecord, getComplianceRecordsByEntity } from '../services/api';
import { toast } from 'react-toastify';

export default function CreateComplianceRecordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialType = searchParams.get('type') || 'REQUEST';
  const initialEntityId = searchParams.get('entityId') || '';
  const [form, setForm] = useState({ type: initialType, entityId: initialEntityId, result: 'PASS', notes: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [checking, setChecking] = useState(false);
  const [exists, setExists] = useState(null);

  const checkExisting = async () => {
    if (!form.entityId) return;
    setChecking(true);
    try {
      const res = await getComplianceRecordsByEntity(form.type, Number(form.entityId));
      if (res.data && res.data.length > 0) {
        setExists(res.data[0]);
      } else {
        setExists(null);
        toast.info('✅ No existing compliance record found. You can create one.');
      }
    } catch {
      setExists(null);
    } finally {
      setChecking(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await createComplianceRecord({
        type: form.type,
        entityId: Number(form.entityId),
        result: form.result,
        notes: form.notes,
      });
      toast.success('✅ Compliance record created!');
      navigate(`/compliance/records/${res.data.complianceId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create compliance record.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="welcome-banner" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0e7490 100%)' }}>
        <h2>➕ Create Compliance Record</h2>
        <p>Review a completed request or resolution and record your compliance findings.</p>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">🛡️</span> New Compliance Check</div>

        {error && <div className="error-msg">⚠️ {error}</div>}

        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span>
          Only create compliance records for <strong>CLOSED / RESOLVED</strong> service requests or <strong>COMPLETED</strong> resolutions. One record per entity.
        </div>

        {exists && (
          <div className="error-msg" style={{ background: '#fef3c7', borderColor: '#f59e0b', color: '#92400e' }}>
            ⚠️ A compliance record already exists for this {form.type} #{form.entityId}:
            <strong> {exists.result}</strong> (ID: #{exists.complianceId})
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Compliance Type</label>
              <select value={form.type} onChange={e => { setForm({ ...form, type: e.target.value }); setExists(null); }}>
                <option value="REQUEST">REQUEST — Service Request</option>
                <option value="RESOLUTION">RESOLUTION — Resolution</option>
              </select>
            </div>
            <div className="form-group">
              <label>{form.type === 'REQUEST' ? 'Service Request ID' : 'Resolution ID'}</label>
              <div style={{ display: 'flex', gap: 8 }}>
                <input type="number" value={form.entityId}
                  onChange={e => { setForm({ ...form, entityId: e.target.value }); setExists(null); }}
                  placeholder={`Enter ${form.type.toLowerCase()} ID`} required min="1" />
                <button type="button" className="btn btn-outline btn-small" onClick={checkExisting} disabled={checking || !form.entityId}>
                  {checking ? '⏳' : '🔍 Check'}
                </button>
              </div>
            </div>
          </div>

          <div className="form-group">
            <label>Compliance Result</label>
            <div style={{ display: 'flex', gap: 16 }}>
              <label style={{
                display: 'flex', alignItems: 'center', gap: 10, padding: '16px 28px',
                background: form.result === 'PASS' ? '#d1fae5' : 'var(--gray-50)',
                border: `2px solid ${form.result === 'PASS' ? '#10b981' : 'var(--gray-200)'}`,
                borderRadius: 'var(--radius-sm)', cursor: 'pointer', flex: 1, fontWeight: 700,
                fontSize: '1rem', textTransform: 'none', letterSpacing: 0, transition: 'var(--transition)'
              }}>
                <input type="radio" name="result" value="PASS" checked={form.result === 'PASS'}
                  onChange={e => setForm({ ...form, result: e.target.value })} style={{ width: 'auto' }} />
                ✅ PASS — Meets compliance standards
              </label>
              <label style={{
                display: 'flex', alignItems: 'center', gap: 10, padding: '16px 28px',
                background: form.result === 'FAIL' ? '#fee2e2' : 'var(--gray-50)',
                border: `2px solid ${form.result === 'FAIL' ? '#ef4444' : 'var(--gray-200)'}`,
                borderRadius: 'var(--radius-sm)', cursor: 'pointer', flex: 1, fontWeight: 700,
                fontSize: '1rem', textTransform: 'none', letterSpacing: 0, transition: 'var(--transition)'
              }}>
                <input type="radio" name="result" value="FAIL" checked={form.result === 'FAIL'}
                  onChange={e => setForm({ ...form, result: e.target.value })} style={{ width: 'auto' }} />
                ❌ FAIL — Does not meet standards
              </label>
            </div>
          </div>

          <div className="form-group">
            <label>Findings / Notes {form.result === 'FAIL' && <span style={{ color: 'var(--danger)' }}>(Required — explain why it failed)</span>}</label>
            <textarea value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })}
              rows={5} placeholder={form.result === 'FAIL'
                ? 'Explain the compliance violation, what standards were not met, and recommended actions...'
                : 'Document your compliance review findings, observations, and conclusions...'
              } required />
          </div>

          {form.result === 'FAIL' && (
            <div className="info-tip" style={{ width: '100%', marginBottom: 20, background: '#fef2f2', borderColor: '#fecaca', color: '#991b1b' }}>
              <span className="tip-icon">🔔</span>
              A <strong>FAIL</strong> result will automatically generate a notification to the responsible officer.
            </div>
          )}

          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading || (exists !== null)}>
              {loading ? '⏳ Creating...' : '🛡️ Submit Compliance Record'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </>
  );
}

