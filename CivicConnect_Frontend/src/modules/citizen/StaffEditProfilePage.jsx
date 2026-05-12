import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyStaffProfile, updateMyStaffProfile } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, FieldError } from '../../components/ui';
import { validate, required, email, exactDigits } from '../../utils/validators';
import { toast } from 'react-toastify';

const RULES = {
  email: [required('Email'), email()],
  phone: [required('Phone'), exactDigits(10, 'Phone')],
};

function StaffEditProfilePage() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [form, setForm] = useState({ email: '', phone: '' });
  const [original, setOriginal] = useState({ name: '', userId: '', role: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving]   = useState(false);
  const [serverError, setServerError] = useState('');

  useEffect(() => {
    if (user.role === 'CITIZEN') {
      navigate('/profile/edit', { replace: true });
      return;
    }
    if (user.role === 'CITY_ADMINISTRATOR') {
      toast.info('City Administrator profiles cannot be self-edited.');
      navigate('/profile', { replace: true });
      return;
    }

    (async () => {
      try {
        const res = await getMyStaffProfile();
        setForm({ email: res.data.email || '', phone: res.data.phone || '' });
        setOriginal({ name: res.data.name, userId: res.data.userId, role: res.data.role });
      } catch (err) {
        setServerError(err.response?.data?.message || 'Failed to load profile.');
      } finally {
        setLoading(false);
      }
    })();
  }, [user, navigate]);

  const setField = (field, value) => {
    const next = { ...form, [field]: value };
    setForm(next);
    if (errors[field]) {
      const fe = validate(next, { [field]: RULES[field] });
      setErrors({ ...errors, [field]: fe[field] });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formErrors = validate(form, RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    setServerError('');
    setSaving(true);
    try {
      await updateMyStaffProfile(form);
      toast.success('✅ Profile updated.');
      navigate('/profile');
    } catch (err) {
      setServerError(err.response?.data?.message || 'Update failed.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  return (
    <>
      <PageHeader
        icon="✏️"
        title="Edit My Profile"
        subtitle="Update your contact details. Name and role are managed centrally."
      />
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">✏️</span> Update Contact Details</div>

        {serverError && <div className="error-msg">⚠️ {serverError}</div>}

        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span>
          Only <strong>Email</strong> and <strong>Phone</strong> can be edited. Contact your administrator
          if your name or role needs to change.
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-row">
            <div className="form-group">
              <label>Name (read-only)</label>
              <input type="text" value={original.name} disabled />
            </div>
            <div className="form-group">
              <label>Role (read-only)</label>
              <input type="text" value={(original.role || '').replace(/_/g, ' ')} disabled />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Email *</label>
              <input
                type="email"
                value={form.email}
                onChange={e => setField('email', e.target.value)}
                className={errors.email ? 'input-error' : ''}
              />
              <FieldError message={errors.email} />
            </div>
            <div className="form-group">
              <label>Phone (10 digits) *</label>
              <input
                type="text"
                value={form.phone}
                onChange={e => setField('phone', e.target.value)}
                maxLength={10}
                className={errors.phone ? 'input-error' : ''}
              />
              <FieldError message={errors.phone} />
            </div>
          </div>

          <div className="actions-row">
            <button className="btn btn-primary" disabled={saving}>
              {saving ? '⏳ Saving...' : '💾 Save Changes'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate('/profile')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
}

export default StaffEditProfilePage;
