import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as loginAPI } from '../services/api';
import { toast } from 'react-toastify';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setLoading(true);
    try {
      const res = await loginAPI(email, password);
      loginUser(res.data);
      toast.success(`Welcome, ${res.data.name}!`);
      const r = res.data.role;
      navigate(r === 'CITIZEN' ? '/my-requests' : r === 'SERVICE_OFFICER' ? '/officer' : '/admin');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed.');
    } finally { setLoading(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{textAlign:'center',marginBottom:8}}>
          <div style={{width:56,height:56,borderRadius:16,margin:'0 auto 16px',background:'linear-gradient(135deg,#2563eb,#0d9488)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'1.6rem'}}>📋</div>
        </div>
        <h2>Service Requests</h2>
        <p className="subtitle">Sign in to manage service requests</p>
        {error && <div className="error-msg">⚠️ {error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group"><label>Email</label><input type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com" required /></div>
          <div className="form-group"><label>Password</label><input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Enter password" required /></div>
          <button className="btn btn-primary btn-full" disabled={loading}>{loading ? '⏳ Signing in...' : '🔑 Sign In'}</button>
        </form>
      </div>
    </div>
  );
}

