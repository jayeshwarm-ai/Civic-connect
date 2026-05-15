import React, { useState, useEffect, useRef } from 'react';
import { getMyDocuments, uploadMyDocument, getMyProfile } from '../../services/api';
import { toast } from 'react-toastify';

const DOC_TYPES = [
  { value: 'ID_PROOF',        label: 'ID Proof (Aadhar, Passport, Voter ID)' },
  { value: 'RESIDENCE_PROOF', label: 'Residence Proof (Utility Bill, Rent Agreement)' },
];

function DocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [docType, setDocType] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const fileRef = useRef();

  const fetchData = async () => {
    try {
      const [docsRes, profRes] = await Promise.all([
        getMyDocuments().catch(() => ({ data: [] })),
        getMyProfile().catch(() => ({ data: null })),
      ]);
      setDocuments(docsRes.data || []);
      setProfile(profRes.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  // A document "blocks" further upload of its type if it's not REJECTED.
  // REJECTED documents allow re-upload so the citizen isn't stuck after a rejection.
  const isBlocked = (type) => documents.some(
    d => d.docType === type && d.verificationStatus !== 'REJECTED'
  );

  const availableTypes = DOC_TYPES.filter(t => !isBlocked(t.value));

  // Keep docType in sync with what's actually available
  useEffect(() => {
    if (availableTypes.length === 0) {
      setDocType('');
    } else if (!availableTypes.some(t => t.value === docType)) {
      setDocType(availableTypes[0].value);
    }
  }, [documents]);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!docType) { toast.error('Please select a document type.'); return; }
    if (!selectedFile) { toast.error('Please select a file.'); return; }
    setUploading(true);
    try {
      await uploadMyDocument(docType, selectedFile);
      toast.success('Document uploaded successfully!');
      setSelectedFile(null);
      if (fileRef.current) fileRef.current.value = '';
      fetchData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Upload failed.';
      toast.error(msg);
    } finally {
      setUploading(false);
    }
  };

  const statusBadge = (status) => {
    const cls = status === 'VERIFIED' ? 'badge-verified'
      : status === 'REJECTED' ? 'badge-rejected' : 'badge-pending';
    return <span className={`badge ${cls}`}>{status}</span>;
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading documents...</div>;

  const hasIdProof = isBlocked('ID_PROOF');
  const hasResProof = isBlocked('RESIDENCE_PROOF');
  const allUploaded = hasIdProof && hasResProof;

  // Detect "address-changed → residence proof was cleared" state:
  // citizen used to be verified (has an ID_PROOF on file) but no residence proof,
  // and account is INACTIVE.
  const idDocVerified = documents.some(
    d => d.docType === 'ID_PROOF' && d.verificationStatus === 'VERIFIED'
  );
  const residenceMissingAfterAddressChange =
    profile && profile.accountStatus === 'INACTIVE'
    && idDocVerified
    && !documents.some(d => d.docType === 'RESIDENCE_PROOF');

  return (
    <>
      {/* Address-change re-verification banner */}
      {residenceMissingAfterAddressChange && (
        <div className="info-tip" style={{ width: '100%', marginBottom: 24, background: '#fef3c7', borderColor: '#fbbf24' }}>
          <span className="tip-icon"></span>
          Your address has been updated. Please upload a new <strong>Residence Proof</strong> matching your current address to re-verify your account.
        </div>
      )}

      {/* Info banner — only when something is missing */}
      {(!hasIdProof || !hasResProof) && !residenceMissingAfterAddressChange && (
        <div className="info-tip" style={{ display: 'flex', width: '100%', marginBottom: 24 }}>
          <span className="tip-icon"></span>You need to upload both <strong style={{ margin: '0 4px' }}>ID Proof</strong> and <strong style={{ margin: '0 4px' }}>Residence Proof</strong> for account activation.
          {!hasIdProof && <span style={{ marginLeft: 8 }}>ID Proof missing</span>}
          {!hasResProof && <span style={{ marginLeft: 8 }}>Residence Proof missing</span>}
        </div>
      )}

      {/* Upload Section — hidden once both docs are uploaded */}
      {availableTypes.length > 0 ? (
        <div className="card">
          <div className="card-title">
            <span className="icon icon-blue"></span>Upload Document
          </div>
          <p className="card-subtitle">Accepted formats: PDF, JPG, JPEG, PNG (max 10MB)</p>
          <form onSubmit={handleUpload}>
            <div className="form-row">
              <div className="form-group">
                <label>Document Type</label>
                <select value={docType} onChange={e => setDocType(e.target.value)}>
                  {availableTypes.map(t => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Select File</label>
                <input type="file" ref={fileRef}
                  onChange={e => setSelectedFile(e.target.files[0])}
                  accept=".pdf,.jpg,.jpeg,.png"
                  style={{ padding: 10 }} />
              </div>
            </div>
            {selectedFile && (
              <div className="info-tip" style={{ marginBottom: 16 }}>
                <span className="tip-icon"></span>Selected: <strong>{selectedFile.name}</strong> ({(selectedFile.size / 1024).toFixed(1)} KB)
              </div>
            )}
            <button className="btn btn-primary" disabled={uploading || !selectedFile}>
              {uploading ? 'Uploading...' : 'Upload Document'}
            </button>
          </form>
        </div>
      ) : (
        <div className="card">
          <div className="info-tip" style={{ width: '100%' }}>
            <span className="tip-icon"></span>
            You have uploaded both required documents. No further uploads are needed.
            {allUploaded && profile?.accountStatus !== 'ACTIVE'
              && ' Waiting for officer verification.'}
            {' '}To replace your Residence Proof, update your address from your profile.
          </div>
        </div>
      )}

      {/* Documents List */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-green"></span>My Documents ({documents.length})
        </div>
        {documents.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"></div>
            <h3>No documents uploaded yet</h3>
            <p>Upload your ID Proof and Residence Proof above to activate your account.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Document</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th>Uploaded</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {documents.map(doc => (
                  <tr key={doc.documentId}>
                    <td style={{ fontWeight: 700 }}>#{doc.documentId}</td>
                    <td>{doc.docType.replace('_', ' ')}</td>
                    <td>{statusBadge(doc.verificationStatus)}</td>
                    <td>{new Date(doc.uploadedDate).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}</td>
                    <td style={{ color: doc.remarks ? '#991b1b' : '#94a3b8', fontStyle: doc.remarks ? 'normal' : 'italic' }}>
                      {doc.remarks || 'No remarks'}
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

export default DocumentsPage;
