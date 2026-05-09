import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  createComplianceRecord,
  getComplianceRecordsByEntity,
  getServiceRequestById,
  getResolutionById,
} from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader } from '../../components/ui';

// Eligibility rules — must mirror the backend ComplianceServiceImpl validation.
//   REQUEST    → only CLOSED service requests are eligible.
//   RESOLUTION → only COMPLETED resolutions are eligible.
const ELIGIBLE_REQUEST_STATUSES    = ['CLOSED'];
const ELIGIBLE_RESOLUTION_STATUSES = ['COMPLETED'];

export default function CreateComplianceRecordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialType = searchParams.get('type') || 'REQUEST';
  const initialEntityId = searchParams.get('entityId') || '';
  const [form, setForm] = useState({ type: initialType, entityId: initialEntityId, result: 'PASS', notes: '' });
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [checking, setChecking] = useState(false);
  const [exists, setExists]     = useState(null);

  // Eligibility tracking — populated by clicking the "🔍 Check" button.
  // `eligible === null` means "not yet checked".
  const [entityStatus, setEntityStatus] = useState(null);
  const [eligible, setEligible]         = useState(null);

  // Reset all derived state when the user changes the type or ID
  const resetCheck = () => { setExists(null); setEntityStatus(null); setEligible(null); };
  const onTypeChange     = (e) => { setForm({ ...form, type: e.target.value }); resetCheck(); };
  const onEntityIdChange = (e) => { setForm({ ...form, entityId: e.target.value }); resetCheck(); };

  const checkExisting = async () => {
    if (!form.entityId) return;
    setChecking(true);
    setEntityStatus(null);
    setEligible(null);
    try {
      // Step 1: pull the linked entity to verify its status
      let entityRes;
      try {
        entityRes = form.type === 'REQUEST'
          ? await getServiceRequestById(Number(form.entityId))
          : await getResolutionById(Number(form.entityId));
      } catch {
        setEligible(false);
        setEntityStatus('NOT_FOUND');
        toast.error(`❌ ${form.type} #${form.entityId} not found.`);
        return;
      }

      const status = (entityRes.data && entityRes.data.status) || 'UNKNOWN';
      setEntityStatus(status);

      const isEligible = form.type === 'REQUEST'
        ? ELIGIBLE_REQUEST_STATUSES.includes(status)
        : ELIGIBLE_RESOLUTION_STATUSES.includes(status);
      setEligible(isEligible);

      if (!isEligible) {
        toast.error(
          form.type === 'REQUEST'
            ? `❌ Request #${form.entityId} is in "${status}" state. Compliance records can only be created for CLOSED requests.`
            : `❌ Resolution #${form.entityId} is in "${status}" state. Compliance records can only be created for COMPLETED resolutions.`
        );
        return;
      }

      // Step 2: only after status passes, check whether a record already exists
      const res = await getComplianceRecordsByEntity(form.type, Number(form.entityId));
      if (res.data && res.data.length > 0) {
        setExists(res.data[0]);
      } else {
        setExists(null);
        toast.success('✅ Eligible — no existing compliance record. You can create one.');
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

    // Hard front-end gate (backend will also reject this; we just give a friendlier message first)
    if (eligible !== true) {
      setError(
        form.type === 'REQUEST'
          ? 'Please click "🔍 Check" first. Compliance records can only be created for CLOSED service requests.'
          : 'Please click "🔍 Check" first. Compliance records can only be created for COMPLETED resolutions.'
      );
      return;
    }
    if (exists) {
      setError(`A compliance record already exists for this ${form.type} #${form.entityId}.`);
      return;
    }

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

  // Submit button stays locked unless the entity has been checked AND found eligible AND has no record
  const submitDisabled = loading || exists !== null || eligible !== true;

  return (
    <>
      <PageHeader icon="➕" title="Create Compliance Record" subtitle="Review a closed request or completed resolution and record your compliance findings." />

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">🛡️</span> New Compliance Check</div>

        {error && <div className="error-msg">⚠️ {error}</div>}

        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span>
          Only create compliance records for <strong>CLOSED</strong> service requests or <strong>COMPLETED</strong> resolutions. One record per entity.
        </div>

        {exists && (
          <div className="error-msg" style={{ background: '#fef3c7', borderColor: '#f59e0b', color: '#92400e' }}>
            ⚠️ A compliance record already exists for this {form.type} #{form.entityId}:
            <strong> {exists.result}</strong> (ID: #{exists.complianceId})
          </div>
        )}

        {/* Eligibility banner — shown after the user clicks Check */}
        {eligible === false && entityStatus && (
          <div className="error-msg" style={{ background: '#fee2e2', borderColor: '#fecaca', color: '#991b1b' }}>
            🚫 This {form.type.toLowerCase()} is in <strong>{entityStatus}</strong> state. A compliance record can only be created when it is{' '}
            <strong>{form.type === 'REQUEST' ? 'CLOSED' : 'COMPLETED'}</strong>.
          </div>
        )}
        {eligible === true && !exists && entityStatus && (
          <div className="info-tip" style={{ width: '100%', marginBottom: 20, background: '#d1fae5', borderColor: '#a7f3d0', color: '#065f46' }}>
            <span className="tip-icon">✅</span>
            Eligible — {form.type.toLowerCase()} #{form.entityId} is in <strong>{entityStatus}</strong> state. You can create a compliance record.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Compliance Type</label>
              <select value={form.type} onChange={onTypeChange}>
                <option value="REQUEST">REQUEST — Service Request</option>
                <option value="RESOLUTION">RESOLUTION — Resolution</option>
              </select>
            </div>
            <div className="form-group">
              <label>{form.type === 'REQUEST' ? 'Service Request ID' : 'Resolution ID'}</label>
              <div style={{ display: 'flex', gap: 8 }}>
                <input type="number" value={form.entityId}
                  onChange={onEntityIdChange}
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
            <button className="btn btn-primary" disabled={submitDisabled}>
              {loading ? '⏳ Creating...' : '🛡️ Submit Compliance Record'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
          </div>

          {eligible !== true && !error && (
            <p style={{ marginTop: 12, fontSize: '0.85rem', color: 'var(--gray-500)' }}>
              ℹ️ Submit will be enabled after you click <strong>🔍 Check</strong> and the {form.type.toLowerCase()} is confirmed eligible.
            </p>
          )}
        </form>
      </div>
    </>
  );
}
