import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyStaffProfile } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { PageHeader } from '../../components/ui';

/**
 * StaffProfilePage
 * ---
 * Profile page for non-citizen roles: SERVICE_OFFICER, DEPARTMENT_HEAD,
 * COMPLIANCE_OFFICER, CITY_ADMINISTRATOR.
 *
 * Shows: Name, ID, Role, Email, Phone, account status, and the registration date.
 * (Address, DOB, and Gender are NOT in the staff data model — citizens have those.)
 *
 * Edit Profile button:
 *   • CITY_ADMINISTRATOR — hidden. Their accounts are administered centrally
 *     and cannot be self-edited.
 *   • All other staff — visible. Takes them to /profile/edit-staff.
 */
function StaffProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    (async () => {
      try {
        const res = await getMyStaffProfile();
        setProfile(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load profile.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading profile...</div>;
  if (error)   return <div className="card"><div className="error-msg">⚠️ {error}</div></div>;
  if (!profile) return null;

  const statusClass =
      profile.status === 'ACTIVE'    ? 'badge-active'
    : profile.status === 'SUSPENDED' ? 'badge-suspended'
    : 'badge-inactive';

  // City Admin can't edit their own profile (rule from the spec).
  const canEdit = user.role !== 'CITY_ADMINISTRATOR';

  return (
    <>
      <PageHeader
        icon="👤"
        title={<>My Profile</>}
        subtitle="Your account details on the CivicConnect platform."
      />

      <div className="card">
        <div className="profile-header">
          <div className="profile-avatar">
            {profile.name?.charAt(0)?.toUpperCase()}
          </div>
          <div className="profile-header-info">
            <h2>{profile.name}</h2>
            <p>{profile.email} · {profile.role.replace(/_/g, ' ')} #{profile.userId}</p>
          </div>
          <span className={`badge ${statusClass}`} style={{ marginLeft: 'auto' }}>
            {profile.status}
          </span>
        </div>

        <div className="profile-grid">
          <div className="profile-item">
            <label>👤 Name</label>
            <div className="value">{profile.name}</div>
          </div>
          <div className="profile-item">
            <label>🪪 User ID</label>
            <div className="value">#{profile.userId}</div>
          </div>
          <div className="profile-item">
            <label>🎯 Role</label>
            <div className="value">{profile.role.replace(/_/g, ' ')}</div>
          </div>
          <div className="profile-item">
            <label>📧 Email</label>
            <div className="value">{profile.email}</div>
          </div>
          <div className="profile-item">
            <label>📱 Phone</label>
            <div className="value">{profile.phone}</div>
          </div>
          <div className="profile-item">
            <label>📅 Registered On</label>
            <div className="value">
              {profile.createdAt
                ? new Date(profile.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
                : '—'}
            </div>
          </div>
        </div>

        <div className="actions-row">
          {canEdit && <Link to="/profile/edit-staff" className="btn btn-primary">✏️ Edit Profile</Link>}
          <Link to="/help" className="btn btn-outline">❓ Help</Link>
        </div>

        {!canEdit && (
          <div className="info-tip" style={{ width: '100%', marginTop: 16 }}>
            <span className="tip-icon">🔒</span>
            City Administrator profiles are managed centrally and cannot be self-edited.
          </div>
        )}
      </div>
    </>
  );
}

export default StaffProfilePage;
