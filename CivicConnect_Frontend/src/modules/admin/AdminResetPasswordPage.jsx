import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStaffByRole, adminResetStaffPassword } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, ConfirmModal } from '../../components/ui';

// Roles that an admin can reset. Note: city admins cannot reset other city
// admins from the dashboard — the backend rejects it. Citizens use the
// self-service forgot-password flow with security questions.
const ROLES = ['SERVICE_OFFICER', 'COMPLIANCE_OFFICER', 'DEPARTMENT_HEAD'];

const roleLabel = (r) => r.replace(/_/g, ' ');

const statusBadge = (s) => {
  const cls = s === 'ACTIVE' ? 'badge-resolved' : s === 'SUSPENDED' ? 'badge-rejected' : 'badge-pending';
  return <span className={`badge ${cls}`}>{s}</span>;
};

/**
 * Admin-only page that lets a City Administrator reset a staff member's
 * password. The admin selects a role, picks a staff member, confirms in a
 * modal, and the backend returns a one-time temporary password that the
 * admin must share with the staff member through a trusted channel.
 *
 * The staff member is forced (via mustChangePassword on the User entity)
 * to change the temp password on their next login.
 */
export default function AdminResetPasswordPage() {
  const navigate = useNavigate();
  const [role, setRole] = useState('SERVICE_OFFICER');
  const [staffList, setStaffList] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [pending, setPending] = useState(null); // staff row pending confirmation
  const [resetting, setResetting] = useState(false);
  const [result, setResult] = useState(null);   // { name, email, temporaryPassword, message }

  const fetchStaff = async () => {
    setLoading(true);
    try {
      const res = await getStaffByRole(role);
      setStaffList(res.data || []);
    } catch {
      setStaffList([]);
      toast.error('Failed to load staff list.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchStaff(); }, [role]);

  const filtered = staffList.filter(s => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (s.name || '').toLowerCase().includes(q)
        || (s.email || '').toLowerCase().includes(q);
  });

  const handleConfirmReset = async () => {
    if (!pending) return;
    setResetting(true);
    try {
      const res = await adminResetStaffPassword(pending.userId);
      setResult(res.data);
      setPending(null);
      toast.success(`Password reset for ${pending.name}.`);
      // Refresh the list in case the staff status or "must change" flag is shown
      fetchStaff();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Reset failed.');
      setPending(null);
    } finally {
      setResetting(false);
    }
  };

  const copyToClipboard = (text) => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text)
        .then(() => toast.success('Copied to clipboard'))
        .catch(() => toast.error('Copy failed — please select the text manually.'));
    }
  };

  return (
    <>
      <PageHeader
        title="Reset Staff Password"
        subtitle="Select a staff member to issue a one-time temporary password. They will be required to change it on their next login."
      />

      <div className="card">
        <div className="card-title">Select Staff Member</div>

        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon"></span>
          This action issues a <strong>temporary password</strong> shown <strong>once</strong> on this page.
          You must share it with the staff member through a trusted channel (in person, secure messaging).
          On their next login, the system will force them to set a new password.
        </div>

        <div className="form-row" style={{ marginBottom: 16 }}>
          <div className="form-group">
            <label>Role</label>
            <select value={role} onChange={e => setRole(e.target.value)}>
              {ROLES.map(r => <option key={r} value={r}>{roleLabel(r)}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Search</label>
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Filter by name or email..."
            />
          </div>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner"></div><br/>Loading staff...</div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <h3>No {roleLabel(role)} accounts found</h3>
            {search && <p>Try clearing the search filter.</p>}
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(s => (
                  <tr key={s.userId}>
                    <td style={{ fontWeight: 700 }}>#{s.userId}</td>
                    <td>{s.name}</td>
                    <td>{s.email}</td>
                    <td>{s.phone || '—'}</td>
                    <td>{statusBadge(s.status)}</td>
                    <td>
                      {s.status === 'SUSPENDED' ? (
                        <span style={{ color: 'var(--gray-400)', fontSize: '.85rem' }}>Activate account first</span>
                      ) : (
                        <button
                          className="btn btn-small btn-danger"
                          onClick={() => setPending(s)}
                        >
                          Reset Password
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="actions-row" style={{ marginTop: 20 }}>
          <button className="btn btn-outline" onClick={() => navigate(-1)}>Back</button>
        </div>
      </div>

      {/* Confirmation modal */}
      {pending && (
        <ConfirmModal
          open={true}
          title="Reset Password?"
          message={
            <>
              Reset password for <strong>{pending.name}</strong> ({roleLabel(pending.role)})?
              <br/><br/>
              This will:
              <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                <li>Issue a one-time temporary password (shown to you next).</li>
                <li>Force them to choose a new password on next login.</li>
              </ul>
            </>
          }
          variant="danger"
          confirmLabel="Yes, reset password"
          loading={resetting}
          onConfirm={handleConfirmReset}
          onClose={() => setPending(null)}
        />
      )}

      {/* Result modal — temp password shown ONCE */}
      {result && (
        <div className="modal-overlay" onClick={() => setResult(null)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 520 }}>
            <h3>Temporary Password Issued</h3>
            <p>Share this password with <strong>{result.name}</strong> ({result.email}) through a trusted channel.</p>
            <div className="info-tip" style={{ width: '100%', marginTop: 12, background: '#fef3c7', borderColor: '#fbbf24' }}>
              <span className="tip-icon"></span>
              This is the <strong>only time</strong> the temporary password will be shown. Copy it now.
            </div>

            <div style={{
              marginTop: 16,
              padding: 16,
              border: '2px dashed var(--gray-300)',
              borderRadius: 10,
              background: 'var(--gray-50)',
              textAlign: 'center',
            }}>
              <div style={{
                fontFamily: 'ui-monospace, "Courier New", monospace',
                fontSize: '1.4rem',
                fontWeight: 700,
                letterSpacing: '0.05em',
                wordBreak: 'break-all',
              }}>
                {result.temporaryPassword}
              </div>
            </div>

            <p style={{ fontSize: '.85rem', color: 'var(--gray-500)', marginTop: 12 }}>
              {result.message}
            </p>

            <div className="actions-row">
              <button className="btn btn-primary" onClick={() => copyToClipboard(result.temporaryPassword)}>
                Copy Password
              </button>
              <button className="btn btn-outline" onClick={() => setResult(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
