import React, { useState, useEffect } from 'react';
import { registerStaff, getStaffByRole, updateStaffStatus } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, FieldError } from '../../components/ui';
import { validate, required, email as emailRule, minLen, exactDigits, passwordStrength } from '../../utils/validators';

const ROLES = ['SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR', 'COMPLIANCE_OFFICER'];
const roleIcon = (r) => r === 'SERVICE_OFFICER' ? '👮' : r === 'DEPARTMENT_HEAD' ? '🏢' : r === 'CITY_ADMINISTRATOR' ? '🏛️' : '📋';

const STAFF_RULES = {
  name:     [required('Name'), minLen(2, 'Name')],
  email:    [required('Email'), emailRule()],
  password: [required('Password'), passwordStrength()],
  phone:    [required('Phone'), exactDigits(10, 'Phone')],
};

export default function StaffManagementPage() {
  const [tab, setTab] = useState('register');
  const [staffList, setStaffList] = useState([]);
  const [viewRole, setViewRole] = useState('SERVICE_OFFICER');
  const [loadingList, setLoadingList] = useState(false);
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'SERVICE_OFFICER' });
  const [errors, setErrors] = useState({});
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');

  const fetchStaff = async (role) => {
    setLoadingList(true);
    try { const r = await getStaffByRole(role); setStaffList(r.data); }
    catch { setStaffList([]); }
    finally { setLoadingList(false); }
  };

  useEffect(() => { if (tab === 'manage') fetchStaff(viewRole); }, [tab, viewRole]);

  const setField = (field, value) => {
    const next = { ...form, [field]: value };
    setForm(next);
    if (errors[field]) {
      const fe = validate(next, { [field]: STAFF_RULES[field] || [] });
      setErrors({ ...errors, [field]: fe[field] });
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    const formErrors = validate(form, STAFF_RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    setError(''); setCreating(true);
    try {
      await registerStaff(form);
      toast.success(`✅ ${form.role.replace('_', ' ')} created!`);
      setForm({ name: '', email: '', password: '', phone: '', role: 'SERVICE_OFFICER' });
      setErrors({});
    } catch (err) {
      setError(err.response?.data?.message || 'Creation failed.');
    } finally { setCreating(false); }
  };

  const handleStatusChange = async (userId, name, newStatus) => {
    const action = newStatus === 'SUSPENDED' ? 'suspend' : 'activate';
    if (!window.confirm(`Are you sure you want to ${action} ${name}?`)) return;
    try {
      await updateStaffStatus(userId, newStatus);
      toast.success(`${name} has been ${newStatus === 'SUSPENDED' ? 'suspended' : 'activated'}.`);
      fetchStaff(viewRole);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed.');
    }
  };

  return (
    <>
      <PageHeader icon="👥" title="Staff Management" subtitle="Register new staff members and manage their account status." />

      {/* Tab Switcher */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
        <button className={`btn ${tab === 'register' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('register')}>
          ➕ Register Staff
        </button>
        <button className={`btn ${tab === 'manage' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('manage')}>
          👥 View & Manage Staff
        </button>
      </div>

      {/* ══ REGISTER TAB ══ */}
      {tab === 'register' && (
        <div className="card">
          <div className="card-title"><span className="icon icon-blue">➕</span> Register New Staff</div>
          {error && <div className="error-msg">⚠️ {error}</div>}
          <form onSubmit={handleCreate} noValidate>
            <div className="form-group">
              <label>Role</label>
              <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
                <option value="SERVICE_OFFICER">👮 Service Officer</option>
                <option value="DEPARTMENT_HEAD">🏢 Department Head</option>
                <option value="CITY_ADMINISTRATOR">🏛️ City Administrator</option>
                <option value="COMPLIANCE_OFFICER">📋 Compliance Officer</option>
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Full Name</label>
                <input value={form.name} onChange={e => setField('name', e.target.value)} placeholder="Jane Smith"
                  className={errors.name ? 'input-error' : ''} />
                <FieldError message={errors.name} />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value={form.email} onChange={e => setField('email', e.target.value)} placeholder="jane@civicconnect.gov"
                  className={errors.email ? 'input-error' : ''} />
                <FieldError message={errors.email} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Password</label>
                <input type="password" value={form.password} onChange={e => setField('password', e.target.value)}
                  placeholder="Min 8 chars with upper, lower, digit"
                  className={errors.password ? 'input-error' : ''} />
                <FieldError message={errors.password} />
              </div>
              <div className="form-group">
                <label>Phone (10 digits)</label>
                <input value={form.phone} onChange={e => setField('phone', e.target.value)} maxLength={10}
                  placeholder="9876543210"
                  className={errors.phone ? 'input-error' : ''} />
                <FieldError message={errors.phone} />
              </div>
            </div>
            <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
              <span className="tip-icon">💡</span> Staff accounts are created with <strong>ACTIVE</strong> status immediately. They can login right away.
            </div>
            <button className="btn btn-primary" disabled={creating}>
              {creating ? '⏳ Creating...' : '➕ Create Staff Account'}
            </button>
          </form>
        </div>
      )}

      {/* ══ MANAGE TAB ══ */}
      {tab === 'manage' && (
        <div className="card">
          <div className="card-title"><span className="icon icon-green">👥</span> Staff List</div>

          <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
            {ROLES.map(r => (
              <button key={r} className={`btn btn-small ${viewRole === r ? 'btn-primary' : 'btn-outline'}`} onClick={() => setViewRole(r)}>
                {roleIcon(r)} {r.replace(/_/g, ' ')}
              </button>
            ))}
          </div>

          {loadingList ? (
            <div className="loading"><div className="spinner"></div></div>
          ) : staffList.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">👤</div>
              <h3>No {viewRole.replace(/_/g, ' ').toLowerCase()}s found</h3>
              <p>Register one using the "Register Staff" tab.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Staff</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {staffList.map(s => (
                    <tr key={s.userId}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                          <div style={{
                            width: 36, height: 36, borderRadius: '50%',
                            background: s.status === 'ACTIVE' ? 'linear-gradient(135deg, #4f46e5, #06b6d4)' : 'linear-gradient(135deg, #94a3b8, #64748b)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            color: 'white', fontWeight: 700, fontSize: '0.85rem', flexShrink: 0
                          }}>{s.name?.charAt(0)?.toUpperCase()}</div>
                          <div>
                            <div style={{ fontWeight: 700, color: '#0f172a' }}>{s.name}</div>
                            <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>ID: #{s.userId}</div>
                          </div>
                        </div>
                      </td>
                      <td>{s.email}</td>
                      <td>{s.phone}</td>
                      <td>
                        <span className={`badge ${s.status === 'ACTIVE' ? 'badge-active' : 'badge-suspended'}`}>
                          {s.status}
                        </span>
                      </td>
                      <td>{new Date(s.createdAt).toLocaleDateString()}</td>
                      <td>
                        {s.status === 'ACTIVE' ? (
                          <button className="btn btn-small btn-danger" onClick={() => handleStatusChange(s.userId, s.name, 'SUSPENDED')}>
                            🚫 Suspend
                          </button>
                        ) : (
                          <button className="btn btn-small btn-success" onClick={() => handleStatusChange(s.userId, s.name, 'ACTIVE')}>
                            ✅ Activate
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </>
  );
}

