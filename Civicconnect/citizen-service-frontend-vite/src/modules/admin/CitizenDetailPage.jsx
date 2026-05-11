import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCitizenById, getCitizenDocuments, deactivateCitizen, downloadDocument } from '../../services/api';
import { toast } from 'react-toastify';

function CitizenDetailPage() {
  const { citizenId } = useParams();
  const navigate = useNavigate();
  const [citizen, setCitizen] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewingDoc, setViewingDoc] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cRes, dRes] = await Promise.all([
          getCitizenById(citizenId),
          getCitizenDocuments(citizenId)
        ]);
        setCitizen(cRes.data);
        setDocuments(dRes.data);
      } catch (err) {
        toast.error('Failed to load citizen details.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [citizenId]);

  const handleDeactivate = async () => {
    if (!window.confirm('Deactivate this citizen?')) return;
    try {
      await deactivateCitizen(citizenId);
      toast.success('Citizen deactivated.');
      const res = await getCitizenById(citizenId);
      setCitizen(res.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed.');
    }
  };

  const handleViewDocument = async (documentId) => {
    try {
      const res = await downloadDocument(documentId);
      const blob = res.data;
      const url = URL.createObjectURL(blob);
      setViewingDoc({ url, type: blob.type, documentId });
    } catch (err) {
      toast.error('Failed to load document.');
    }
  };

  const closeViewer = () => {
    if (viewingDoc) URL.revokeObjectURL(viewingDoc.url);
    setViewingDoc(null);
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;
  if (!citizen) return <div className="card error-msg">Citizen not found.</div>;

  const statusClass = citizen.accountStatus === 'ACTIVE' ? 'badge-active'
    : citizen.accountStatus === 'SUSPENDED' ? 'badge-suspended' : 'badge-inactive';

  const docStatusBadge = (s) => {
    const cls = s === 'VERIFIED' ? 'badge-verified' : s === 'REJECTED' ? 'badge-rejected' : 'badge-pending';
    return <span className={`badge ${cls}`}>{s}</span>;
  };

  return (
    <>
      {/* Document Viewer Modal */}
      {viewingDoc && (
        <div className="modal-overlay" onClick={closeViewer}>
          <div className="modal" style={{ maxWidth: 800, maxHeight: '90vh' }} onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3>📄 Document #{viewingDoc.documentId}</h3>
              <button className="btn btn-small btn-secondary" onClick={closeViewer}>✕ Close</button>
            </div>
            <div style={{ textAlign: 'center', maxHeight: '70vh', overflow: 'auto' }}>
              {viewingDoc.type && viewingDoc.type.startsWith('image/') ? (
                <img src={viewingDoc.url} alt="Document" style={{ maxWidth: '100%', borderRadius: 8 }} />
              ) : viewingDoc.type === 'application/pdf' ? (
                <iframe src={viewingDoc.url} title="Document" style={{ width: '100%', height: '65vh', border: 'none', borderRadius: 8 }} />
              ) : (
                <div className="empty-state">
                  <p>Cannot preview this file type.</p>
                  <a href={viewingDoc.url} download className="btn btn-primary">⬇️ Download</a>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <div className="card">
        <div className="profile-header">
          <div className="profile-avatar">
            {citizen.name?.charAt(0)?.toUpperCase()}
          </div>
          <div className="profile-header-info">
            <h2>{citizen.name}</h2>
            <p>Citizen #{citizen.citizenId} · {citizen.email}</p>
          </div>
          <span className={`badge ${statusClass}`} style={{ marginLeft: 'auto' }}>
            {citizen.accountStatus}
          </span>
        </div>

        <div className="profile-grid">
          <div className="profile-item"><label>📧 Email</label><div className="value">{citizen.email}</div></div>
          <div className="profile-item"><label>📱 Phone</label><div className="value">{citizen.phone}</div></div>
          <div className="profile-item"><label>🎂 DOB</label><div className="value">{citizen.dob}</div></div>
          <div className="profile-item"><label>👤 Gender</label><div className="value">{citizen.gender}</div></div>
          <div className="profile-item"><label>📍 Address</label><div className="value">{citizen.address}</div></div>
          <div className="profile-item"><label>📋 Contact Info</label><div className="value">{citizen.contactInfo}</div></div>
          <div className="profile-item"><label>📅 Registered</label><div className="value">{new Date(citizen.createdAt).toLocaleString()}</div></div>
          <div className="profile-item"><label>🔄 Updated</label><div className="value">{new Date(citizen.updatedAt).toLocaleString()}</div></div>
        </div>

        <div className="actions-row">
          <button className="btn btn-secondary" onClick={() => navigate('/admin')}>← Back to Dashboard</button>
          {citizen.accountStatus !== 'SUSPENDED' && (
            <button className="btn btn-danger" onClick={handleDeactivate}>🚫 Deactivate Account</button>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-green">📄</span> Documents ({documents.length})</div>
        {documents.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📁</div>
            <h3>No documents uploaded</h3>
            <p>This citizen has not uploaded any documents yet.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>ID</th><th>Type</th><th>Status</th><th>Uploaded</th><th>Remarks</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {documents.map(d => (
                  <tr key={d.documentId}>
                    <td style={{ fontWeight: 700 }}>#{d.documentId}</td>
                    <td>{d.docType === 'ID_PROOF' ? '🪪' : '🏠'} {d.docType.replace('_', ' ')}</td>
                    <td>{docStatusBadge(d.verificationStatus)}</td>
                    <td>{new Date(d.uploadedDate).toLocaleDateString()}</td>
                    <td style={{ color: d.remarks ? '#991b1b' : '#94a3b8' }}>{d.remarks || '—'}</td>
                    <td>
                      <button className="btn btn-small btn-secondary"
                        onClick={() => handleViewDocument(d.documentId)}>
                        👁️ View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

export default CitizenDetailPage;

