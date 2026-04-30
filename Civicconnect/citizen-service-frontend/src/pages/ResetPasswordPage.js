import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { resetPassword } from '../services/api';
import { toast } from 'react-toastify';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', phone: '', newPassword: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    if (form.newPassword !== form.confirmPassword) {
      setError('Passwords do not match.'); return;
    }
    setLoading(true);
    try {
      await resetPassword(form.email, form.phone, form.newPassword);
      setSuccess(true);
      toast.success('🔑 Password reset successful!');
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed. Check your email and phone.');
    } finally { setLoading(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 8 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16, margin: '0 auto 16px',
            background: 'linear-gradient(135deg, #f59e0b, #ef4444)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.6rem'
          }}>🔒</div>
        </div>
        <h2 style={{ textAlign: 'center' }}>Reset Password</h2>
        <p className="subtitle" style={{ textAlign: 'center' }}>Verify your identity with email & phone</p>

        {error && <div className="error-msg">⚠️ {error}</div>}

        {success ? (
          <div style={{ textAlign: 'center' }}>
            <div className="success-msg">✅ Password has been reset successfully!</div>
            <Link to="/login" className="btn btn-primary btn-full" style={{ marginTop: 16 }}>🔑 Sign In Now</Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email Address</label>
              <input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})}
                placeholder="you@example.com" required />
            </div>
            <div className="form-group">
              <label>Phone (for verification)</label>
              <input value={form.phone} onChange={e => setForm({...form, phone: e.target.value})}
                placeholder="Your registered 10-digit phone" required pattern="^[0-9]{10}$" />
            </div>
            <div className="form-group">
              <label>New Password</label>
              <input type="password" value={form.newPassword} onChange={e => setForm({...form, newPassword: e.target.value})}
                placeholder="Min 8 characters" required minLength={8} />
            </div>
            <div className="form-group">
              <label>Confirm New Password</label>
              <input type="password" value={form.confirmPassword} onChange={e => setForm({...form, confirmPassword: e.target.value})}
                placeholder="Re-enter new password" required minLength={8} />
            </div>
            <button className="btn btn-primary btn-full" disabled={loading}>
              {loading ? '⏳ Resetting...' : '🔑 Reset Password'}
            </button>
          </form>
        )}

        <p className="link-text">
          Remember your password? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

