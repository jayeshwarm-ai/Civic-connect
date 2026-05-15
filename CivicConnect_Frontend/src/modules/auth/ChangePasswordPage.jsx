import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { changeOwnPassword } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader, FieldError } from '../../components/ui';

/**
 * Lets the signed-in user change their own password.
 *
 * Two modes:
 *   • Voluntary  — accessed from a profile/menu link.
 *   • Forced     — auto-routed here after login when the user has
 *                  mustChangePassword = true (admin issued a temp password).
 *                  Detected by the query string ?forced=1.
 *
 * Forced mode hides the back/cancel buttons so the user can't navigate
 * away without setting a real password.
 */
export default function ChangePasswordPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const forced = new URLSearchParams(location.search).get('forced') === '1';

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const next = {};
    if (!currentPassword) next.currentPassword = forced ? 'Enter the temporary password.' : 'Enter your current password.';
    if (!newPassword) next.newPassword = 'New password is required.';
    else if (newPassword.length < 8) next.newPassword = 'New password must be at least 8 characters.';
    if (newPassword === currentPassword && newPassword) {
      next.newPassword = 'New password must be different from the current password.';
    }
    if (newPassword !== confirmPwd) next.confirmPwd = 'Passwords do not match.';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validateForm()) return;

    setLoading(true);
    try {
      await changeOwnPassword(currentPassword, newPassword);
      toast.success('Password changed successfully. Please sign in with your new password.');
      // Best practice after a password change: force re-login so any cached
      // session uses fresh credentials.
      logout();
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not change password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Change Password"
        subtitle={forced
          ? 'You are signed in with a temporary password. Please set a new password to continue.'
          : 'Update your account password.'}
      />

      <div className="card" style={{ maxWidth: 560 }}>
        {forced && (
          <div className="info-tip" style={{ width: '100%', marginBottom: 20, background: '#fef3c7', borderColor: '#fbbf24' }}>
            <span className="tip-icon"></span>
            A City Administrator reset your password. You must change it now to continue using your account.
          </div>
        )}

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label>{forced ? 'Temporary Password' : 'Current Password'}</label>
            <input
              type="password"
              value={currentPassword}
              onChange={e => { setCurrentPassword(e.target.value); if (errors.currentPassword) setErrors({ ...errors, currentPassword: undefined }); }}
              placeholder={forced ? 'Enter the temporary password you received' : 'Enter your current password'}
              className={errors.currentPassword ? 'input-error' : ''}
              autoComplete="current-password"
            />
            <FieldError message={errors.currentPassword} />
          </div>

          <div className="form-group">
            <label>New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={e => { setNewPassword(e.target.value); if (errors.newPassword) setErrors({ ...errors, newPassword: undefined }); }}
              placeholder="At least 8 characters"
              className={errors.newPassword ? 'input-error' : ''}
              autoComplete="new-password"
            />
            <FieldError message={errors.newPassword} />
          </div>

          <div className="form-group">
            <label>Confirm New Password</label>
            <input
              type="password"
              value={confirmPwd}
              onChange={e => { setConfirmPwd(e.target.value); if (errors.confirmPwd) setErrors({ ...errors, confirmPwd: undefined }); }}
              placeholder="Re-enter the new password"
              className={errors.confirmPwd ? 'input-error' : ''}
              autoComplete="new-password"
            />
            <FieldError message={errors.confirmPwd} />
          </div>

          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Updating...' : 'Change Password'}
            </button>
            {!forced && (
              <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>
                Cancel
              </button>
            )}
            {forced && (
              <button type="button" className="btn btn-outline" onClick={() => { logout(); navigate('/login'); }}>
                Sign out
              </button>
            )}
          </div>
        </form>
      </div>
    </>
  );
}
