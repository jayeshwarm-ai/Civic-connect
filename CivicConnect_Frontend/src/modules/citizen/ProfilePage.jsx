import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getMyProfile, getMyDocuments } from '../../services/api';
import { PageHeader } from '../../components/ui';

function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [pRes, dRes] = await Promise.all([
          getMyProfile(),
          getMyDocuments().catch(() => ({ data: [] }))
        ]);
        setProfile(pRes.data);
        setDocuments(dRes.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load profile.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading profile...</div>;
  if (error) return <div className="card"><div className="error-msg">⚠️ {error}</div></div>;

  const statusClass = profile.accountStatus === 'ACTIVE' ? 'badge-active'
    : profile.accountStatus === 'SUSPENDED' ? 'badge-suspended' : 'badge-inactive';

  // Determine account activation steps
  const hasIdProof = documents.some(d => d.docType === 'ID_PROOF');
  const hasResProof = documents.some(d => d.docType === 'RESIDENCE_PROOF');
  const idVerified = documents.some(d => d.docType === 'ID_PROOF' && d.verificationStatus === 'VERIFIED');
  const resVerified = documents.some(d => d.docType === 'RESIDENCE_PROOF' && d.verificationStatus === 'VERIFIED');
  const isActive = profile.accountStatus === 'ACTIVE';

  return (
    <>
      {/* Welcome Banner */}
      <PageHeader icon="👋" title={<>Welcome back, {profile.name}!</>} subtitle={<>{isActive
            ? 'Your account is active and verified. You have full access to all CivicConnect services.'
            : 'Complete the steps below to activate your account and access all services.'}</>} />

      {/* Account Activation Steps (only for non-active) */}
      {!isActive && (
        <div className="card">
          <div className="card-title">
            <span className="icon icon-blue">🚀</span> Account Activation Progress
          </div>
          <div className="steps">
            <div className={`step completed`}>
              <div className="step-dot">✓</div>
              <div className="step-label">Register</div>
            </div>
            <div className={`step ${hasIdProof ? 'completed' : hasResProof ? 'active' : 'active'}`}>
              <div className="step-dot">{hasIdProof && hasResProof ? '✓' : '2'}</div>
              <div className="step-label">Upload Docs</div>
            </div>
            <div className={`step ${idVerified && resVerified ? 'completed' : (hasIdProof && hasResProof) ? 'active' : ''}`}>
              <div className="step-dot">{idVerified && resVerified ? '✓' : '3'}</div>
              <div className="step-label">Verification</div>
            </div>
            <div className={`step ${isActive ? 'completed' : ''}`}>
              <div className="step-dot">4</div>
              <div className="step-label">Active</div>
            </div>
          </div>
          {!hasIdProof || !hasResProof ? (
            <div className="info-tip">
              <span className="tip-icon">💡</span>
              Please upload your <strong>ID Proof</strong> and <strong>Residence Proof</strong> to proceed with verification.
            </div>
          ) : !idVerified || !resVerified ? (
            <div className="info-tip">
              <span className="tip-icon">⏳</span>
              Your documents are under review. You'll be notified once verified.
            </div>
          ) : null}
        </div>
      )}

      {/* Profile Card */}
      <div className="card">
        <div className="profile-header">
          <div className="profile-avatar">
            {profile.name?.charAt(0)?.toUpperCase()}
          </div>
          <div className="profile-header-info">
            <h2>{profile.name}</h2>
            <p>{profile.email} · Citizen #{profile.citizenId}</p>
          </div>
          <span className={`badge ${statusClass}`} style={{ marginLeft: 'auto' }}>
            {profile.accountStatus}
          </span>
        </div>

        <div className="profile-grid">
          <div className="profile-item">
            <label>📧 Email</label>
            <div className="value">{profile.email}</div>
          </div>
          <div className="profile-item">
            <label>📱 Phone</label>
            <div className="value">{profile.phone}</div>
          </div>
          <div className="profile-item">
            <label>🎂 Date of Birth</label>
            <div className="value">{profile.dob}</div>
          </div>
          <div className="profile-item">
            <label>👤 Gender</label>
            <div className="value">{profile.gender}</div>
          </div>
          <div className="profile-item">
            <label>📍 Address</label>
            <div className="value">{profile.address}</div>
          </div>
          <div className="profile-item">
            <label>📋 Contact Info</label>
            <div className="value">{profile.contactInfo}</div>
          </div>
          <div className="profile-item">
            <label>📅 Registered On</label>
            <div className="value">{new Date(profile.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</div>
          </div>
          <div className="profile-item">
            <label>🔄 Last Updated</label>
            <div className="value">{new Date(profile.updatedAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</div>
          </div>
        </div>

        <div className="actions-row">
          <Link to="/profile/edit" className="btn btn-primary">✏️ Edit Profile</Link>
          <Link to="/documents" className="btn btn-secondary">📄 My Documents</Link>
          <Link to="/help" className="btn btn-outline">❓ Help & FAQ</Link>
        </div>
      </div>
    </>
  );
}

export default ProfilePage;

