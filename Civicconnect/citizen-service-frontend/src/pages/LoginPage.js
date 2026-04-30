import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as loginAPI } from '../services/api';
import { toast } from 'react-toastify';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await loginAPI(email, password);
      loginUser(res.data);
      toast.success(`Welcome back, ${res.data.name}!`);
      navigate(res.data.role === 'CITY_ADMINISTRATOR' ? '/admin' : '/profile');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Login failed. Check your credentials.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 8 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16, margin: '0 auto 16px',
            background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.6rem'
          }}>🏛️</div>
        </div>
        <h2 style={{ textAlign: 'center' }}>Welcome Back</h2>
        <p className="subtitle" style={{ textAlign: 'center' }}>Sign in to your CivicConnect account</p>

        {error && <div className="error-msg">⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)}
              placeholder="you@example.com" required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)}
              placeholder="Enter your password" required />
          </div>
          <button className="btn btn-primary btn-full" disabled={loading}>
            {loading ? '⏳ Signing in...' : '🔑 Sign In'}
          </button>
        </form>

        <p className="link-text">
          Don't have an account? <Link to="/register">Create one here</Link>
        </p>
        <p className="link-text" style={{marginTop:8}}>
          <Link to="/reset-password">🔒 Forgot password?</Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;

