import React, { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById, assignOfficerToRequest, getStaffByRole } from '../../services/api';
import { toast } from 'react-toastify';

const badge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_',' ')}</span>;

/**
 * AssignOfficerPage
 * ---
 * Department Heads / City Administrators land here to assign a Service Officer
 * to a specific service request.
 *
 * Functionality (unchanged):
 *   • Loads the request by its ID.
 *   • Calls assignOfficerToRequest(requestId, officerId) on submit.
 *   • Redirects to /admin/service-requests on success.
 *
 * What changed:
 *   • Officer selection is now a DROPDOWN listing every ACTIVE Service Officer,
 *     instead of a free-text "type the officer's user ID" field.
 *   • The dropdown shows: #ID — Name (phone).
 *   • A small "Open Staff Management" link is shown so admins can still go to
 *     the Staff Management page if they need to add a new officer first.
 */
export default function AssignOfficerPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();

  // Service-request state
  const [req, setReq] = useState(null);
  const [loadingReq, setLoadingReq] = useState(true);

  // Officer-list state (drives the dropdown)
  const [officers, setOfficers] = useState([]);
  const [loadingOfficers, setLoadingOfficers] = useState(true);

  // Form state
  const [officerId, setOfficerId] = useState('');
  const [assigning, setAssigning] = useState(false);
  const [error, setError] = useState('');

  // Load the request details
  useEffect(() => {
    (async () => {
      try {
        const r = await getServiceRequestById(requestId);
        setReq(r.data);
      } catch {
        toast.error('Failed to load request.');
      } finally {
        setLoadingReq(false);
      }
    })();
  }, [requestId]);

  // Load every Service Officer and keep only the ACTIVE ones — a suspended
  // officer should never be a valid assignment target.
  useEffect(() => {
    (async () => {
      try {
        const r = await getStaffByRole('SERVICE_OFFICER');
        const activeOnly = (r.data || []).filter(o => o.status === 'ACTIVE');
        setOfficers(activeOnly);
      } catch {
        setOfficers([]);
        toast.error('Could not load officer list.');
      } finally {
        setLoadingOfficers(false);
      }
    })();
  }, []);

  const handleAssign = async (e) => {
    e.preventDefault();
    setError('');
    setAssigning(true);
    try {
      await assignOfficerToRequest(requestId, Number(officerId));
      toast.success('✅ Officer assigned!');
      navigate('/admin/service-requests');
    } catch (err) {
      setError(err.response?.data?.message || 'Assignment failed.');
    } finally {
      setAssigning(false);
    }
  };

  if (loadingReq) {
    return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  }
  if (!req) {
    return <div className="card"><div className="error-msg">Request not found.</div></div>;
  }

  // Look up the currently selected officer (for the helper panel below the dropdown)
  const selected = officers.find(o => String(o.userId) === String(officerId));

  return (
    <div className="card">
      <div className="card-title">
        <span className="icon icon-blue">👤</span> Assign Officer — Request #{req.requestId}
        <span style={{ marginLeft: 'auto' }}>{badge(req.status)}</span>
      </div>

      {/* ── Request summary (unchanged) ─────────────────────────── */}
      <div className="profile-grid" style={{ marginBottom: 24 }}>
        <div className="profile-item"><label>📋 Type</label><div className="value">{req.type}</div></div>
        <div className="profile-item"><label>👤 Citizen</label><div className="value">{req.citizenName}</div></div>
        <div className="profile-item"><label>📍 City / State</label><div className="value">{req.city || '—'}{req.state ? `, ${req.state}` : ''}</div></div>
        <div className="profile-item" style={{ gridColumn: '1 / -1' }}><label>🏠 Address</label><div className="value">{req.address || '—'}</div></div>
        <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(req.createdAt).toLocaleString()}</div></div>
      </div>
      <div style={{ padding: 16, background: 'var(--gray-50)', borderRadius: 14, border: '1px solid var(--gray-100)', marginBottom: 24 }}>
        <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>Description</label>
        <p style={{ color: 'var(--gray-700)' }}>{req.description}</p>
      </div>

      {error && <div className="error-msg">⚠️ {error}</div>}

      <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
        <span className="tip-icon">💡</span>
        Pick a <strong>Service Officer</strong> from the list. Only active officers are shown.
        {' '}Need to register a new one? <Link to="/admin/staff">Open Staff Management</Link>.
      </div>

      {/* ── Assignment form ────────────────────────────────────── */}
      <form onSubmit={handleAssign}>
        <div className="form-group">
          <label>Service Officer</label>
          {loadingOfficers ? (
            <div className="loading" style={{ padding: 12 }}>
              <div className="spinner" style={{ width: 22, height: 22 }}></div>
              &nbsp;Loading officers…
            </div>
          ) : officers.length === 0 ? (
            <div className="error-msg" style={{ background: '#fef3c7', borderColor: '#f59e0b', color: '#92400e' }}>
              ⚠️ No active Service Officers exist yet.
              {' '}<Link to="/admin/staff">Register one in Staff Management</Link> and come back.
            </div>
          ) : (
            <select
              value={officerId}
              onChange={e => setOfficerId(e.target.value)}
              required
            >
              <option value="" disabled>— Select an officer —</option>
              {officers.map(o => (
                <option key={o.userId} value={o.userId}>
                  #{o.userId} — {o.name} ({o.phone})
                </option>
              ))}
            </select>
          )}
        </div>

        {/* Selected-officer preview card */}
        {selected && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 14,
            padding: 14, marginBottom: 20,
            background: '#eef2ff', border: '1px solid #c7d2fe',
            borderRadius: 12,
          }}>
            <div style={{
              width: 42, height: 42, borderRadius: '50%',
              background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: '#fff', fontWeight: 800, fontSize: '1rem',
            }}>
              {selected.name?.charAt(0)?.toUpperCase()}
            </div>
            <div>
              <div style={{ fontWeight: 700, color: '#1e1b4b' }}>{selected.name}</div>
              <div style={{ fontSize: '0.82rem', color: '#4338ca' }}>
                ID #{selected.userId} • SERVICE_OFFICER • {selected.email}
              </div>
            </div>
          </div>
        )}

        <div className="actions-row">
          <button className="btn btn-primary" disabled={assigning || !officerId || officers.length === 0}>
            {assigning ? '⏳ Assigning...' : '👤 Assign Officer'}
          </button>
          <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
