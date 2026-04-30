import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyProfile, updateMyProfile } from '../services/api';
import { toast } from 'react-toastify';

function ProfileEditPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ address: '', contactInfo: '', phone: '' });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await getMyProfile();
        setForm({
          address: res.data.address,
          contactInfo: res.data.contactInfo,
          phone: res.data.phone,
        });
      } catch (err) {
        setError('Failed to load profile.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await updateMyProfile(form);
      toast.success('Profile updated!');
      navigate('/profile');
    } catch (err) {
      const msg = err.response?.data?.message || 'Update failed.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  return (
    <div className="card">
      <div className="card-title"><span className="icon">✏️</span> Edit Profile</div>

      {error && <div className="error-msg">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Address</label>
          <input name="address" value={form.address} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Contact Info</label>
          <input name="contactInfo" value={form.contactInfo} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Phone (10 digits)</label>
          <input name="phone" value={form.phone} onChange={handleChange} required pattern="^[0-9]{10}$" />
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
  );
}

export default ProfileEditPage;

