import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { login as loginAPI, registerCitizen, resetPassword } from '../../services/api';
import { toast } from 'react-toastify';
import { FieldError } from '../../components/ui';
import { validate, required, email as emailRule, minLen, exactDigits, passwordStrength, dobAtLeast } from '../../utils/validators';

/* ─── Password strength helper (kept from original RegisterPage) ─────────── */
function getPasswordStrength(pw) {
  if (!pw) return { label: '', color: '#94a3b8', percent: 0 };
  let score = 0;
  if (pw.length >= 8)               score++;
  if (pw.length >= 12)              score++;
  if (/[A-Z]/.test(pw))             score++;
  if (/[a-z]/.test(pw))             score++;
  if (/[0-9]/.test(pw))             score++;
  if (/[^A-Za-z0-9]/.test(pw))      score++;
  if (score <= 2) return { label: '🔴 Weak',   color: '#dc2626', percent: 25 };
  if (score <= 3) return { label: '🟠 Fair',   color: '#ea580c', percent: 50 };
  if (score <= 4) return { label: '🟡 Good',   color: '#d97706', percent: 75 };
  return            { label: '🟢 Strong', color: '#059669', percent: 100 };
}

/**
 * AuthPage — combined Login / Register card with inline reset-password panel.
 *
 * Props:
 *   initialTab — 'login' | 'register' (default 'login')
 *
 * UX:
 *   • Tabs at top switch between LOGIN and REGISTER without page reload.
 *   • A "Forgot password?" link inside the LOGIN tab toggles an inline panel
 *     that performs the reset using email + phone + new password.
 *
 * Functionality is preserved exactly from the original three pages —
 * the same API calls, fields, validation, success/error toasts, and redirects.
 */
export default function AuthPage({ initialTab = 'login' }) {
  const [tab, setTab] = useState(initialTab); // 'login' | 'register'
  return (
    <div className="gov-auth-shell">
      <div className="gov-auth-card">
        <div className="gov-auth-card__strip" aria-hidden="true" />
        <AuthTabs current={tab} onChange={setTab} />
        <div className="gov-auth-body">
          {tab === 'login'    && <LoginForm    onSwitchToRegister={() => setTab('register')} />}
          {tab === 'register' && <RegisterForm onSwitchToLogin={()    => setTab('login')} />}
        </div>
      </div>
    </div>
  );
}

/* ────────────────────────────────────────────────────────────────────────── */

function AuthTabs({ current, onChange }) {
  return (
    <div className="gov-auth-tabs" role="tablist">
      <button
        role="tab"
        aria-selected={current === 'login'}
        onClick={() => onChange('login')}
        className={`gov-auth-tab ${current === 'login' ? 'gov-auth-tab--active' : ''}`}
      >
        Login
      </button>
      <button
        role="tab"
        aria-selected={current === 'register'}
        onClick={() => onChange('register')}
        className={`gov-auth-tab ${current === 'register' ? 'gov-auth-tab--active' : ''}`}
      >
        Register
      </button>
    </div>
  );
}

/* ─────────────── LOGIN form (with inline forgot-password panel) ─────────── */

function LoginForm({ onSwitchToRegister }) {
  const navigate = useNavigate();
  const { loginUser } = useAuth();

  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors]     = useState({});
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [showForgot, setShowForgot] = useState(false);

  const RULES = {
    email:    [required('Email'), emailRule()],
    password: [required('Password')],
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formErrors = validate({ email, password }, RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

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
    <>
      <h2 className="gov-auth-title">Sign in to your account</h2>
      <p className="gov-auth-subtitle">Access civic services and track your requests.</p>

      {error && <div className="gov-auth-error">⚠️ {error}</div>}

      <form onSubmit={handleSubmit} className="gov-auth-form" noValidate>
        <div className="form-group">
          <label htmlFor="login-email">Email Address</label>
          <input
            id="login-email" type="email" value={email}
            onChange={e => { setEmail(e.target.value); if (errors.email) setErrors({ ...errors, email: undefined }); }}
            placeholder="you@example.com" autoComplete="email"
            className={errors.email ? 'input-error' : ''}
          />
          <FieldError message={errors.email} />
        </div>
        <div className="form-group">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password" type="password" value={password}
            onChange={e => { setPassword(e.target.value); if (errors.password) setErrors({ ...errors, password: undefined }); }}
            placeholder="Enter your password" autoComplete="current-password"
            className={errors.password ? 'input-error' : ''}
          />
          <FieldError message={errors.password} />
        </div>
        <button type="submit" className="gov-auth-submit" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </button>
      </form>

      <div className="gov-auth-aux">
        <button type="button" className="gov-auth-link" onClick={() => setShowForgot(v => !v)}>
          {showForgot ? 'Hide' : 'Forgot password?'}
        </button>
        <span style={{ color: 'var(--gov-text-muted)' }}>
          New here?{' '}
          <button type="button" className="gov-auth-link" onClick={onSwitchToRegister}>
            Create an account
          </button>
        </span>
      </div>

      {showForgot && <ForgotPasswordPanel onDone={() => setShowForgot(false)} />}
    </>
  );
}

/* ───────────── Inline reset panel inside the LOGIN tab ─────────────────── */

function ForgotPasswordPanel({ onDone }) {
  const [form, setForm]       = useState({ email: '', phone: '', newPassword: '', confirmPassword: '' });
  const [errors, setErrors]   = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState(false);

  const RULES = {
    email:           [required('Email'), emailRule()],
    phone:           [required('Phone'), exactDigits(10, 'Phone')],
    newPassword:     [required('New Password'), passwordStrength()],
    confirmPassword: [required('Confirm Password')],
  };

  const onChange = (e) => {
    const next = { ...form, [e.target.name]: e.target.value };
    setForm(next);
    if (errors[e.target.name]) {
      const fe = validate(next, { [e.target.name]: RULES[e.target.name] });
      setErrors({ ...errors, [e.target.name]: fe[e.target.name] });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formErrors = validate(form, RULES);
    if (!formErrors.confirmPassword && form.newPassword !== form.confirmPassword) {
      formErrors.confirmPassword = 'Passwords do not match.';
    }
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    setError('');
    setLoading(true);
    try {
      await resetPassword(form.email, form.phone, form.newPassword);
      setSuccess(true);
      toast.success('🔑 Password reset successful!');
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed. Check your email and phone.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-forgot-panel">
      <h4>Reset your password</h4>
      <p>Verify your identity using your registered email and phone number.</p>

      {error && <div className="gov-auth-error">⚠️ {error}</div>}

      {success ? (
        <div className="gov-auth-success">
          ✅ Password has been reset. You can sign in now.
          <div style={{ marginTop: 10 }}>
            <button type="button" className="gov-auth-link" onClick={onDone}>Close</button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="gov-auth-form" noValidate>
          <div className="gov-auth-row">
            <div className="form-group">
              <label>Email Address</label>
              <input name="email" type="email" value={form.email} onChange={onChange}
                placeholder="you@example.com"
                className={errors.email ? 'input-error' : ''} />
              <FieldError message={errors.email} />
            </div>
            <div className="form-group">
              <label>Phone (10 digits)</label>
              <input name="phone" value={form.phone} onChange={onChange} maxLength={10}
                placeholder="Registered phone"
                className={errors.phone ? 'input-error' : ''} />
              <FieldError message={errors.phone} />
            </div>
          </div>
          <div className="gov-auth-row">
            <div className="form-group">
              <label>New Password</label>
              <input name="newPassword" type="password" value={form.newPassword} onChange={onChange}
                placeholder="Min 8 characters with upper, lower, digit"
                className={errors.newPassword ? 'input-error' : ''} />
              <FieldError message={errors.newPassword} />
            </div>
            <div className="form-group">
              <label>Confirm New Password</label>
              <input name="confirmPassword" type="password" value={form.confirmPassword} onChange={onChange}
                placeholder="Re-enter new password"
                className={errors.confirmPassword ? 'input-error' : ''} />
              <FieldError message={errors.confirmPassword} />
            </div>
          </div>
          <button type="submit" className="gov-auth-submit" disabled={loading}>
            {loading ? 'Resetting…' : 'Reset Password'}
          </button>
        </form>
      )}
    </div>
  );
}

/* ───────────────────────────── REGISTER form ────────────────────────────── */

function RegisterForm({ onSwitchToLogin }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [errors, setErrors]   = useState({});
  const [form, setForm]       = useState({
    name: '', email: '', password: '', phone: '',
    dob: '', gender: 'MALE', address: '', contactInfo: '',
  });

  const RULES = {
    name:        [required('Name'), minLen(2, 'Name')],
    email:       [required('Email'), emailRule()],
    password:    [required('Password'), passwordStrength()],
    phone:       [required('Phone'), exactDigits(10, 'Phone')],
    dob:         [required('Date of Birth'), dobAtLeast(13)],
    address:     [required('Address'), minLen(15, 'Address')],
    contactInfo: [required('Contact Info'), minLen(5, 'Contact Info')],
  };

  const onChange = (e) => {
    const next = { ...form, [e.target.name]: e.target.value };
    setForm(next);
    if (errors[e.target.name]) {
      const fe = validate(next, { [e.target.name]: RULES[e.target.name] || [] });
      setErrors({ ...errors, [e.target.name]: fe[e.target.name] });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formErrors = validate(form, RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    setError('');
    setLoading(true);
    try {
      await registerCitizen(form);
      toast.success('🎉 Registration successful! Please login to continue.');
      onSwitchToLogin();
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Registration failed.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const strength = getPasswordStrength(form.password);

  return (
    <>
      <h2 className="gov-auth-title">Citizen registration</h2>
      <p className="gov-auth-subtitle">Create your CivicConnect citizen account.</p>

      {error && <div className="gov-auth-error">⚠️ {error}</div>}

      <form onSubmit={handleSubmit} className="gov-auth-form" noValidate>
        <div className="gov-auth-row">
          <div className="form-group">
            <label>Full Name</label>
            <input name="name" value={form.name} onChange={onChange} placeholder="John Doe"
              className={errors.name ? 'input-error' : ''} />
            <FieldError message={errors.name} />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input name="email" type="email" value={form.email} onChange={onChange} placeholder="you@example.com"
              className={errors.email ? 'input-error' : ''} />
            <FieldError message={errors.email} />
          </div>
        </div>

        <div className="gov-auth-row">
          <div className="form-group">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={onChange}
              placeholder="Min 8 characters with upper, lower, digit"
              className={errors.password ? 'input-error' : ''} />
            <FieldError message={errors.password} />
            {form.password && !errors.password && (
              <>
                <div className="gov-pw-bar">
                  <div className="gov-pw-bar__fill" style={{ width: `${strength.percent}%`, background: strength.color }} />
                </div>
                <div className="gov-pw-label" style={{ color: strength.color }}>{strength.label}</div>
              </>
            )}
          </div>
          <div className="form-group">
            <label>Phone (10 digits)</label>
            <input name="phone" value={form.phone} onChange={onChange} maxLength={10}
              placeholder="9876543210"
              className={errors.phone ? 'input-error' : ''} />
            <FieldError message={errors.phone} />
          </div>
        </div>

        <div className="gov-auth-row">
          <div className="form-group">
            <label>Date of Birth</label>
            <input name="dob" type="date" value={form.dob} onChange={onChange}
              className={errors.dob ? 'input-error' : ''} />
            <FieldError message={errors.dob} />
          </div>
          <div className="form-group">
            <label>Gender</label>
            <select name="gender" value={form.gender} onChange={onChange}>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>Address</label>
          <input name="address" value={form.address} onChange={onChange}
            placeholder="123 Main Street, City, State"
            className={errors.address ? 'input-error' : ''} />
          <FieldError message={errors.address} />
        </div>
        <div className="form-group">
          <label>Contact Info</label>
          <input name="contactInfo" value={form.contactInfo} onChange={onChange}
            placeholder="Alternate phone or emergency contact"
            className={errors.contactInfo ? 'input-error' : ''} />
          <FieldError message={errors.contactInfo} />
        </div>

        <button type="submit" className="gov-auth-submit" disabled={loading}>
          {loading ? 'Registering…' : 'Create Account'}
        </button>
      </form>

      <div className="gov-auth-aux" style={{ justifyContent: 'flex-end' }}>
        <span style={{ color: 'var(--gov-text-muted)' }}>
          Already have an account?{' '}
          <button type="button" className="gov-auth-link" onClick={onSwitchToLogin}>Sign in</button>
        </span>
      </div>
    </>
  );
}
