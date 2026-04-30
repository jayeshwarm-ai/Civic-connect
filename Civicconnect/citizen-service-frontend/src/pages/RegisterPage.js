import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerCitizen } from '../services/api';
import { toast } from 'react-toastify';

function getPasswordStrength(pw) {
  if (!pw) return { label: '', color: '#ccc', percent: 0 };
  let score = 0;
  if (pw.length >= 8) score++;
  if (pw.length >= 12) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[a-z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  if (score <= 2) return { label: '🔴 Weak', color: '#dc2626', percent: 25 };
  if (score <= 3) return { label: '🟠 Fair', color: '#ea580c', percent: 50 };
  if (score <= 4) return { label: '🟡 Good', color: '#d97706', percent: 75 };
  return { label: '🟢 Strong', color: '#059669', percent: 100 };
}

function RegisterPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    name: '', email: '', password: '', phone: '',
    dob: '', gender: 'MALE', address: '', contactInfo: ''
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await registerCitizen(form);
      toast.success('🎉 Registration successful! Please login to continue.');
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Registration failed.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 580 }}>
        <div style={{ textAlign: 'center', marginBottom: 8 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16, margin: '0 auto 16px',
            background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.6rem'
          }}>📝</div>
        </div>
        <h2 style={{ textAlign: 'center' }}>Citizen Registration</h2>
        <p className="subtitle" style={{ textAlign: 'center' }}>Create your CivicConnect citizen account</p>

        {error && <div className="error-msg">⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Full Name</label>
              <input name="name" value={form.name} onChange={handleChange} placeholder="John Doe" required />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input name="email" type="email" value={form.email} onChange={handleChange} placeholder="you@example.com" required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Password</label>
              <input name="password" type="password" value={form.password} onChange={handleChange} placeholder="Min 8 characters" required minLength={8} />
              {form.password && (() => {
                const s = getPasswordStrength(form.password);
                return (
                  <div style={{ marginTop: 6 }}>
                    <div style={{ height: 6, borderRadius: 3, background: '#e5e7eb', overflow: 'hidden' }}>
                      <div style={{ width: `${s.percent}%`, height: '100%', background: s.color, borderRadius: 3, transition: 'all .3s' }} />
                    </div>
                    <span style={{ fontSize: '.75rem', fontWeight: 700, color: s.color }}>{s.label}</span>
                    {s.percent < 75 && (
                      <div style={{ fontSize: '.7rem', color: '#6b7280', marginTop: 2 }}>
                        Tip: Use uppercase, lowercase, numbers &amp; special characters
                      </div>
                    )}
                  </div>
                );
              })()}
            </div>
            <div className="form-group">
              <label>Phone (10 digits)</label>
              <input name="phone" value={form.phone} onChange={handleChange} placeholder="9876543210" required pattern="^[0-9]{10}$" />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Date of Birth</label>
              <input name="dob" type="date" value={form.dob} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Gender</label>
              <select name="gender" value={form.gender} onChange={handleChange}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Address</label>
            <input name="address" value={form.address} onChange={handleChange} placeholder="123 Main Street, City, State" required />
          </div>
          <div className="form-group">
            <label>Contact Info</label>
            <input name="contactInfo" value={form.contactInfo} onChange={handleChange} placeholder="Alternate phone or emergency contact" required />
          </div>
          <button className="btn btn-primary btn-full" disabled={loading}>
            {loading ? '⏳ Registering...' : '🚀 Create Account'}
          </button>
        </form>

        <p className="link-text">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;

