import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyProfile, updateMyProfile } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, FieldError } from '../../components/ui';
import { validate, required, minLen, maxLen, exactDigits } from '../../utils/validators';

const RULES = {
  address:     [required('Address'), minLen(15, 'Address'), maxLen(255, 'Address')],
  contactInfo: [required('Contact Info'), minLen(5, 'Contact Info')],
  phone:       [required('Phone'), exactDigits(10, 'Phone')],
};

function ProfileEditPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [serverError, setServerError] = useState('');
  const [form, setForm] = useState({ address: '', contactInfo: '', phone: '' });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    (async () => {
      try {
        const res = await getMyProfile();
        setForm({
          address: res.data.address || '',
          contactInfo: res.data.contactInfo || '',
          phone: res.data.phone || '',
        });
      } catch (err) {
        setServerError('Failed to load profile.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

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

    setSaving(true);
    setServerError('');
    try {
      await updateMyProfile(form);
      toast.success('Profile updated!');
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
      <PageHeader icon="✏️" title="Edit Profile" subtitle="Update your contact and address details." />
      <div className="card">
        <div className="card-title"><span className="icon">✏️</span> Edit Profile</div>

        {serverError && <div className="error-msg">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label>Address *</label>
            <input
              name="address"
              value={form.address}
              onChange={e => setField('address', e.target.value)}
              className={errors.address ? 'input-error' : ''}
            />
            <FieldError message={errors.address} />
          </div>

          <div className="form-group">
            <label>Contact Info *</label>
            <input
              name="contactInfo"
              value={form.contactInfo}
              onChange={e => setField('contactInfo', e.target.value)}
              className={errors.contactInfo ? 'input-error' : ''}
            />
            <FieldError message={errors.contactInfo} />
          </div>

          <div className="form-group">
            <label>Phone (10 digits) *</label>
            <input
              name="phone"
              value={form.phone}
              onChange={e => setField('phone', e.target.value)}
              maxLength={10}
              className={errors.phone ? 'input-error' : ''}
            />
            <FieldError message={errors.phone} />
          </div>

          <div className="actions-row">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? 'Saving...' : '💾 Save Changes'}
            </button>
            <button className="btn btn-secondary" type="button" onClick={() => navigate('/profile')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
}

export default ProfileEditPage;
