import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getResolutionById, getWorkflowSteps, addWorkflowStep, updateWorkflowStepStatus } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';

const statusBadge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_', ' ')}</span>;
const stepIcon = (s) => s === 'COMPLETED' ? '✅' : s === 'IN_PROGRESS' ? '🔄' : '⏳';

export default function ResolutionDetailPage() {
  const { resolutionId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [resolution, setResolution] = useState(null);
  const [steps, setSteps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddStep, setShowAddStep] = useState(false);
  const [stepForm, setStepForm] = useState({ description: '' });
  const [addingStep, setAddingStep] = useState(false);
  const [updatingStep, setUpdatingStep] = useState(null);

  const fetchData = async () => {
    try {
      const [r, s] = await Promise.all([getResolutionById(resolutionId), getWorkflowSteps(resolutionId)]);
      setResolution(r.data); setSteps(s.data);
    } catch { toast.error('Failed to load resolution.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, [resolutionId]);

  const handleAddStep = async (e) => {
    e.preventDefault(); setAddingStep(true);
    try {
      await addWorkflowStep(resolutionId, { description: stepForm.description, assignedToUserId: user.userId });
      toast.success('✅ Workflow step added!');
      setStepForm({ description: '' });
      setShowAddStep(false);
      fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add step.');
    } finally { setAddingStep(false); }
  };

  const handleUpdateStepStatus = async (stepId, newStatus) => {
    setUpdatingStep(stepId);
    try {
      await updateWorkflowStepStatus(stepId, newStatus);
      toast.success(`Step updated to ${newStatus.replace('_', ' ')}!`);
      fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update step.');
    } finally { setUpdatingStep(null); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!resolution) return <div className="card"><div className="error-msg">Resolution not found.</div></div>;

  const isOfficer = user.role === 'SERVICE_OFFICER';
  const isOwner = resolution.officerUserId === user.userId;
  const canManage = isOwner && resolution.status !== 'COMPLETED';

  return (
    <>
      {/* Resolution Info */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">🔧</span> Resolution #{resolution.resolutionId}
          <span style={{ marginLeft: 'auto' }}>{statusBadge(resolution.status)}</span>
        </div>
        <div className="profile-grid">
          <div className="profile-item"><label>📋 Request ID</label><div className="value"><Link to={`/service-requests/${resolution.requestId}`}>#{resolution.requestId}</Link></div></div>
          <div className="profile-item"><label>👮 Officer</label><div className="value">{resolution.officerName} (ID: #{resolution.officerUserId})</div></div>
          <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(resolution.createdAt).toLocaleString()}</div></div>
          <div className="profile-item"><label>🔄 Updated</label><div className="value">{new Date(resolution.updatedAt).toLocaleString()}</div></div>
        </div>
        <div style={{ marginTop: 24, padding: 20, background: 'var(--gray-50)', borderRadius: 14, border: '1px solid var(--gray-100)' }}>
          <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>📝 Actions / Plan</label>
          <p style={{ fontSize: '.95rem', lineHeight: 1.7, color: 'var(--gray-700)' }}>{resolution.actions}</p>
        </div>
        <div className="actions-row">
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>← Back</button>
          {canManage && <button className="btn btn-primary" onClick={() => setShowAddStep(true)}>➕ Add Workflow Step</button>}
        </div>
      </div>

      {/* Add Step Modal */}
      {showAddStep && (
        <div className="modal-overlay" onClick={() => setShowAddStep(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>➕ Add Workflow Step</h3>
            <form onSubmit={handleAddStep}>
              <div className="form-group">
                <label>Step Description / Action Plan</label>
                <textarea value={stepForm.description} onChange={e => setStepForm({ ...stepForm, description: e.target.value })}
                  rows={3} placeholder="Describe the task or action step..." required />
              </div>
              <div className="actions-row">
                <button className="btn btn-primary" disabled={addingStep}>{addingStep ? '⏳ Adding...' : '➕ Add Step'}</button>
                <button type="button" className="btn btn-outline" onClick={() => setShowAddStep(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Workflow Steps */}
      <div className="card">
        <div className="card-title"><span className="icon icon-green">📋</span> Workflow Steps ({steps.length})</div>
        {steps.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📋</div>
            <h3>No workflow steps yet</h3>
            <p>{canManage ? 'Add workflow steps to break down the resolution into tasks.' : 'No steps have been added to this resolution.'}</p>
          </div>
        ) : (
          <div className="table-container"><table><thead><tr>
            <th>Step</th><th>Description</th><th>Assigned To</th><th>Status</th><th>Created</th><th>Actions</th>
          </tr></thead><tbody>
            {steps.map(s => {
              const canUpdate = s.assignedToUserId === user.userId && s.status !== 'COMPLETED';
              const nextStatus = s.status === 'PENDING' ? 'IN_PROGRESS' : s.status === 'IN_PROGRESS' ? 'COMPLETED' : null;
              return (
                <tr key={s.stepId}>
                  <td style={{ fontWeight: 700 }}>{stepIcon(s.status)} #{s.stepId}</td>
                  <td style={{ maxWidth: 250 }}>{s.description}</td>
                  <td>{s.assignedToUserName} (#{s.assignedToUserId})</td>
                  <td>{statusBadge(s.status)}</td>
                  <td>{new Date(s.createdAt).toLocaleDateString()}</td>
                  <td>
                    {canUpdate && nextStatus && (
                      <button className={`btn btn-small ${nextStatus === 'COMPLETED' ? 'btn-success' : 'btn-primary'}`}
                        disabled={updatingStep === s.stepId}
                        onClick={() => handleUpdateStepStatus(s.stepId, nextStatus)}>
                        {updatingStep === s.stepId ? '⏳' : nextStatus === 'IN_PROGRESS' ? '▶️ Start' : '✅ Complete'}
                      </button>
                    )}
                    {!canUpdate && <span style={{ color: 'var(--gray-400)', fontSize: '.85rem' }}>—</span>}
                  </td>
                </tr>
              );
            })}
          </tbody></table></div>
        )}

        {resolution.status === 'IN_PROGRESS' && steps.length > 0 && (
          <div className="info-tip" style={{ width: '100%', marginTop: 20 }}>
            <span className="tip-icon">💡</span>
            When <strong>all workflow steps</strong> are completed, the resolution will auto-complete and the service request will be marked as <strong>RESOLVED</strong>.
          </div>
        )}
      </div>
    </>
  );
}

