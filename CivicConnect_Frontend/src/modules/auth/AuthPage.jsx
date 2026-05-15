import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { login as loginAPI, registerCitizen,
         forgotPasswordRequest, verifySecurityAnswers, resetPasswordWithToken,
         getSecurityQuestions, setupSecurityAnswers } from '../../services/api';
import { toast } from 'react-toastify';
import { FieldError } from '../../components/ui';
import { validate, required, email as emailRule, minLen, exactDigits, passwordStrength, dobAtLeast } from '../../utils/validators';

/* ─── Reusable password input with show/hide eye toggle ──────────────────── */
function PasswordInput({ id, name, value, onChange, placeholder, autoComplete, hasError }) {
  const [show, setShow] = useState(false);
  return (
    <div className="gov-pw-wrap">
      <input
        id={id}
        name={name}
        type={show ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className={hasError ? 'input-error gov-pw-input' : 'gov-pw-input'}
      />
      <button
              type="button"
              className="gov-pw-toggle"
              onClick={() => setShow(s => !s)}
              aria-label={show ? 'Hide password' : 'Show password'}
              tabIndex={-1}
            >
              {show ? (
                // eye-off icon
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
                  <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
                  <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
                  <line x1="2" y1="2" x2="22" y2="22" />
                </svg>
              ) : (
                // eye icon
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              )}
            </button>
    </div>
  );
}

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
  if (score <= 2) return { label: 'Weak', color: '#dc2626', percent: 25 };
  if (score <= 3) return { label: 'Fair', color: '#ea580c', percent: 50 };
  if (score <= 4) return { label: 'Good', color: '#d97706', percent: 75 };
  return { label: 'Strong', color: '#059669', percent: 100 };
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
      >Login
      </button>
      <button
        role="tab"
        aria-selected={current === 'register'}
        onClick={() => onChange('register')}
        className={`gov-auth-tab ${current === 'register' ? 'gov-auth-tab--active' : ''}`}
      >Register
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
      if (res.data.mustChangePassword) {
        // Admin issued a temp password. Force user to set their own before
        // they can access anything else.
        toast.info('You are using a temporary password. Please set a new one.');
        navigate('/change-password?forced=1');
        return;
      }
      toast.success(`Welcome back, ${res.data.name}!`);
      const landingByRole = {
        CITY_ADMINISTRATOR: '/admin',
        DEPARTMENT_HEAD:    '/reports',
        SERVICE_OFFICER:    '/officer/dashboard',
        COMPLIANCE_OFFICER: '/compliance',
        CITIZEN:            '/service-requests',
      };
      navigate(landingByRole[res.data.role] || '/profile');
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

      {error && <div className="gov-auth-error"> {error}</div>}

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
          <PasswordInput
            id="login-password"
            value={password}
            onChange={e => { setPassword(e.target.value); if (errors.password) setErrors({ ...errors, password: undefined }); }}
            placeholder="Enter your password"
            autoComplete="current-password"
            hasError={!!errors.password}
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
        <span style={{ color: 'var(--gov-text-muted)' }}>New here?{' '}
          <button type="button" className="gov-auth-link" onClick={onSwitchToRegister}>Create an account
          </button>
        </span>
      </div>

      {showForgot && <ForgotPasswordPanel onDone={() => setShowForgot(false)} />}
    </>
  );
}

/* ───────────── Inline reset panel inside the LOGIN tab ─────────────────── */
/**
 * 3-step forgot-password flow using security questions:
 *   1. Citizen enters email → backend returns their 3 chosen questions.
 *   2. Citizen answers all 3 → backend issues a single-use reset token.
 *   3. Citizen sets a new password using the token.
 *
 * This flow is for CITIZENS ONLY. Staff (Service Officer, Department Head,
 * Compliance Officer) must contact a City Administrator to reset their password.
 */
function ForgotPasswordPanel({ onDone }) {
  const [step, setStep]               = useState(1);
  const [email, setEmail]             = useState('');
  const [questions, setQuestions]     = useState([]);
  const [answers, setAnswers]         = useState(['', '', '']);
  const [resetToken, setResetToken]   = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPwd, setConfirmPwd]   = useState('');
  const [loading, setLoading]         = useState(false);
  const [error, setError]             = useState('');
  const [success, setSuccess]         = useState(false);

  // Step 1 → 2: fetch the user's questions
  const handleStep1 = async (e) => {
    e.preventDefault();
    setError('');
    if (!email.trim()) { setError('Please enter your email.'); return; }
    setLoading(true);
    try {
      const res = await forgotPasswordRequest(email.trim());
      setQuestions(res.data || []);
      setAnswers((res.data || []).map(() => ''));
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not find your account.');
    } finally {
      setLoading(false);
    }
  };

  // Step 2 → 3: verify answers, receive reset token
  const handleStep2 = async (e) => {
    e.preventDefault();
    setError('');
    if (answers.some(a => !a.trim())) {
      setError('Please answer all 3 questions.');
      return;
    }
    setLoading(true);
    try {
      const payload = questions.map((q, i) => ({ questionId: q.questionId, answer: answers[i] }));
      const res = await verifySecurityAnswers(email.trim(), payload);
      setResetToken(res.data.resetToken);
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || 'One or more answers are incorrect.');
    } finally {
      setLoading(false);
    }
  };

  // Step 3: set new password
  const handleStep3 = async (e) => {
    e.preventDefault();
    setError('');
    if (newPassword.length < 8) { setError('Password must be at least 8 characters.'); return; }
    if (newPassword !== confirmPwd) { setError('Passwords do not match.'); return; }
    setLoading(true);
    try {
      await resetPasswordWithToken(resetToken, newPassword);
      setSuccess(true);
      toast.success('Password reset successful!');
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="gov-forgot-panel">
        <div className="gov-auth-success">Password has been reset. You can sign in now.
          <div style={{ marginTop: 10 }}>
            <button type="button" className="gov-auth-link" onClick={onDone}>Close</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="gov-forgot-panel">
      <h4>Reset your password</h4>
      <p style={{ fontSize: '.85rem', color: 'var(--gov-text-muted)' }}>
        Citizens can reset their password by answering the 3 security questions they set during registration.
        Staff members should contact a City Administrator.
      </p>
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, fontSize: '.8rem', color: 'var(--gov-text-muted)' }}>
        <span style={{ fontWeight: step === 1 ? 800 : 400 }}>1. Email</span>
        <span>›</span>
        <span style={{ fontWeight: step === 2 ? 800 : 400 }}>2. Answers</span>
        <span>›</span>
        <span style={{ fontWeight: step === 3 ? 800 : 400 }}>3. New password</span>
      </div>

      {error && <div className="gov-auth-error">{error}</div>}

      {step === 1 && (
        <form onSubmit={handleStep1} className="gov-auth-form" noValidate>
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)}
              placeholder="you@example.com" />
          </div>
          <button type="submit" className="gov-auth-submit" disabled={loading}>
            {loading ? 'Looking up…' : 'Next'}
          </button>
        </form>
      )}

      {step === 2 && (
        <form onSubmit={handleStep2} className="gov-auth-form" noValidate>
          {questions.map((q, i) => (
            <div key={q.questionId} className="form-group">
              <label>{i + 1}. {q.questionText}</label>
              <input value={answers[i]}
                onChange={e => {
                  const next = [...answers];
                  next[i] = e.target.value;
                  setAnswers(next);
                }}
                placeholder="Your answer" />
            </div>
          ))}
          <p style={{ fontSize: '.75rem', color: 'var(--gov-text-muted)' }}>
            Answers are not case-sensitive.
          </p>
          <button type="submit" className="gov-auth-submit" disabled={loading}>
            {loading ? 'Verifying…' : 'Verify Answers'}
          </button>
          <button type="button" className="gov-auth-link"
            style={{ marginTop: 8 }} onClick={() => { setStep(1); setError(''); }}>
            ← Back
          </button>
        </form>
      )}

      {step === 3 && (
        <form onSubmit={handleStep3} className="gov-auth-form" noValidate>
          <div className="form-group">
            <label>New Password</label>
            <PasswordInput value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              placeholder="Min 8 characters" />
          </div>
          <div className="form-group">
            <label>Confirm New Password</label>
            <PasswordInput value={confirmPwd}
              onChange={e => setConfirmPwd(e.target.value)}
              placeholder="Re-enter new password" />
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
      toast.success('Registration successful! Please log in and set up security questions for password recovery.', { autoClose: 6000 });
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

      {error && <div className="gov-auth-error"> {error}</div>}

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
            <PasswordInput name="password" value={form.password} onChange={onChange}
              placeholder="Min 8 characters with upper, lower, digit"
              hasError={!!errors.password} />
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
        <span style={{ color: 'var(--gov-text-muted)' }}>Already have an account?{' '}
          <button type="button" className="gov-auth-link" onClick={onSwitchToLogin}>Sign in</button>
        </span>
      </div>
    </>
  );
}
