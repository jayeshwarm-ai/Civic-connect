import React, { useState, useEffect, useRef } from 'react';
import { getMyDocuments, uploadMyDocument } from '../../services/api';
import { toast } from 'react-toastify';

function DocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [docType, setDocType] = useState('ID_PROOF');
  const [selectedFile, setSelectedFile] = useState(null);
  const fileRef = useRef();

  const fetchDocs = async () => {
    try {
      const res = await getMyDocuments();
      setDocuments(res.data);
    } catch (err) {
      // May fail if no profile yet
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDocs(); }, []);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) { toast.error('Please select a file'); return; }
    setUploading(true);
    try {
      await uploadMyDocument(docType, selectedFile);
      toast.success('📄 Document uploaded successfully!');
      setSelectedFile(null);
      if (fileRef.current) fileRef.current.value = '';
      fetchDocs();
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

  const hasIdProof = documents.some(d => d.docType === 'ID_PROOF');
  const hasResProof = documents.some(d => d.docType === 'RESIDENCE_PROOF');

  return (
    <>
      {/* Info Banner */}
      {(!hasIdProof || !hasResProof) && (
        <div className="info-tip" style={{ display: 'flex', width: '100%', marginBottom: 24 }}>
          <span className="tip-icon">💡</span>
          You need to upload both <strong style={{ margin: '0 4px' }}>ID Proof</strong> and <strong style={{ margin: '0 4px' }}>Residence Proof</strong> for account activation.
          {!hasIdProof && <span style={{ marginLeft: 8 }}>❌ ID Proof missing</span>}
          {!hasResProof && <span style={{ marginLeft: 8 }}>❌ Residence Proof missing</span>}
        </div>
      )}

      {/* Upload Section */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">📤</span> Upload Document
        </div>
        <p className="card-subtitle">Accepted formats: PDF, JPG, JPEG, PNG (max 10MB)</p>
        <form onSubmit={handleUpload}>
          <div className="form-row">
            <div className="form-group">
              <label>Document Type</label>
              <select value={docType} onChange={e => setDocType(e.target.value)}>
                <option value="ID_PROOF">🪪 ID Proof (Aadhar, Passport, Voter ID)</option>
                <option value="RESIDENCE_PROOF">🏠 Residence Proof (Utility Bill, Rent Agreement)</option>
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
              <span className="tip-icon">📎</span>
              Selected: <strong>{selectedFile.name}</strong> ({(selectedFile.size / 1024).toFixed(1)} KB)
            </div>
          )}
          <button className="btn btn-primary" disabled={uploading || !selectedFile}>
            {uploading ? '⏳ Uploading...' : '⬆️ Upload Document'}
          </button>
        </form>
      </div>

      {/* Documents List */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-green">📄</span> My Documents ({documents.length})
        </div>
        {documents.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📁</div>
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
                    <td>
                      <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        {doc.docType === 'ID_PROOF' ? '🪪' : '🏠'} {doc.docType.replace('_', ' ')}
                      </span>
                    </td>
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

