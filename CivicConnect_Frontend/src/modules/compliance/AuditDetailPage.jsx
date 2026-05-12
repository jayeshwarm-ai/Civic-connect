import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAuditRecordById, updateAuditRecord } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader } from '../../components/ui';

const statusBadge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_', ' ')}</span>;

const statusSteps = ['OPEN', 'IN_REVIEW', 'CLOSED'];

export default function AuditDetailPage() {
  const { auditId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [audit, setAudit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showUpdate, setShowUpdate] = useState(false);
  const [updateForm, setUpdateForm] = useState({ status: '', findings: '' });
  const [updating, setUpdating] = useState(false);

  const fetchAudit = async () => {
    try {
      const res = await getAuditRecordById(auditId);
      setAudit(res.data);
    } catch {
      toast.error('Failed to load audit.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAudit(); }, [auditId]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setUpdating(true);
    try {
      await updateAuditRecord(auditId, {
        status: updateForm.status,
        findings: updateForm.findings || null,
      });
      toast.success(`Audit updated to ${updateForm.status.replace('_', ' ')}!`);
      setShowUpdate(false);
      fetchAudit();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update audit.');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;
  if (!audit) return <div className="card"><div className="error-msg">Audit not found.</div></div>;

  const currentIdx = statusSteps.indexOf(audit.status);
  const nextStatus = currentIdx < statusSteps.length - 1 ? statusSteps[currentIdx + 1] : null;
  const isOwner = audit.officerUserId === user.userId;
  const isAdmin = user.role === 'CITY_ADMINISTRATOR';
  const canUpdate = (isOwner || isAdmin) && audit.status !== 'CLOSED';

  return (
    <>
      <PageHeader icon="🔍" title={<>Audit #{audit.auditId}</>} subtitle="Audit review and management" />

      {/* Status timeline */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-green">📊</span> Audit Status
          <span style={{ marginLeft: 'auto' }}>{statusBadge(audit.status)}</span>
        </div>
        <div className="steps">
          {statusSteps.map((step, i) => (
            <div key={step} className={`step ${i < currentIdx ? 'completed' : i === currentIdx ? 'active' : ''}`}>
              <div className="step-dot">{i < currentIdx ? '✓' : i + 1}</div>
              <div className="step-label">{step.replace('_', ' ')}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Details */}
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📋</span> Audit Details</div>
        <div className="profile-grid">
          <div className="profile-item"><label>🆔 Audit ID</label><div className="value">#{audit.auditId}</div></div>
          <div className="profile-item"><label>👮 Officer</label><div className="value">{audit.officerName} (ID: #{audit.officerUserId})</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(audit.createdAt).toLocaleString()}</div></div>
          <div className="profile-item"><label>🔄 Updated</label><div className="value">{new Date(audit.updatedAt).toLocaleString()}</div></div>
        </div>

        <div style={{ marginTop: 24, padding: 20, background: 'var(--gray-50)', borderRadius: 14, border: '1px solid var(--gray-100)' }}>
          <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>
            🎯 Scope
          </label>
          <p style={{ fontSize: '.95rem', lineHeight: 1.7, color: 'var(--gray-700)' }}>{audit.scope}</p>
        </div>

        {audit.findings && (
          <div style={{ marginTop: 16, padding: 20, background: '#eff6ff', borderRadius: 14, border: '1px solid #bfdbfe' }}>
            <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>
              📝 Findings
            </label>
            <p style={{ fontSize: '.95rem', lineHeight: 1.7, color: 'var(--gray-700)' }}>{audit.findings}</p>
          </div>
        )}

        <div className="actions-row">
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>← Back</button>
          {canUpdate && nextStatus && (
            <button className="btn btn-primary" onClick={() => { setUpdateForm({ status: nextStatus, findings: audit.findings || '' }); setShowUpdate(true); }}>
              ➡️ Move to {nextStatus.replace('_', ' ')}
            </button>
          )}
        </div>
      </div>

      {/* Update Modal */}
      {showUpdate && (
        <div className="modal-overlay" onClick={() => setShowUpdate(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>➡️ Update Audit to {updateForm.status.replace('_', ' ')}</h3>
            <form onSubmit={handleUpdate}>
              <div className="form-group">
                <label>New Status</label>
                <input type="text" value={updateForm.status.replace('_', ' ')} disabled />
              </div>
              <div className="form-group">
                <label>Findings {updateForm.status === 'CLOSED' && <span style={{ color: 'var(--danger)' }}>(Recommended before closing)</span>}</label>
                <textarea value={updateForm.findings} onChange={e => setUpdateForm({ ...updateForm, findings: e.target.value })}
                  rows={5} placeholder="Enter your audit findings, observations, and conclusions..." />
              </div>
              <div className="actions-row">
                <button className="btn btn-primary" disabled={updating}>{updating ? '⏳ Updating...' : `➡️ Confirm ${updateForm.status.replace('_', ' ')}`}</button>
                <button type="button" className="btn btn-outline" onClick={() => setShowUpdate(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

