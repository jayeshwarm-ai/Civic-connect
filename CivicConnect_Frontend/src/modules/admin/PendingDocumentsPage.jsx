import React, { useState, useEffect } from 'react';
import { getPendingDocuments, verifyDocument, downloadDocument } from '../../services/api';
import { toast } from 'react-toastify';

function PendingDocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // { documentId, action }
  const [remarks, setRemarks] = useState('');
  const [processing, setProcessing] = useState(false);
  const [viewingDoc, setViewingDoc] = useState(null); // { url, type, documentId }

  const fetchDocs = async () => {
    try {
      const res = await getPendingDocuments();
      setDocuments(res.data);
    } catch (err) {
      toast.error('Failed to load pending documents.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDocs(); }, []);

  const handleVerify = async (documentId) => {
    try {
      await verifyDocument(documentId, { verificationStatus: 'VERIFIED', remarks: '' });
      toast.success('Document verified!');
      fetchDocs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Verification failed.');
    }
  };

  const openRejectModal = (documentId) => {
    setModal({ documentId });
    setRemarks('');
  };

  const handleReject = async () => {
    if (!remarks.trim()) { toast.error('Remarks required for rejection.'); return; }
    setProcessing(true);
    try {
      await verifyDocument(modal.documentId, { verificationStatus: 'REJECTED', remarks });
      toast.success('Document rejected.');
      setModal(null);
      fetchDocs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Rejection failed.');
    } finally {
      setProcessing(false);
    }
  };

  const handleViewDocument = async (documentId) => {
    try {
      const res = await downloadDocument(documentId);
      const blob = res.data;
      const url = URL.createObjectURL(blob);
      const type = blob.type;
      setViewingDoc({ url, type, documentId });
    } catch (err) {
      toast.error('Failed to load document.');
    }
  };

  const closeViewer = () => {
    if (viewingDoc) URL.revokeObjectURL(viewingDoc.url);
    setViewingDoc(null);
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

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
              {viewingDoc.type.startsWith('image/') ? (
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
        <div className="card-title"><span className="icon">⏳</span> Pending Documents ({documents.length})</div>

        {documents.length === 0 ? (
          <div className="empty-state">
            <div className="icon">✅</div>
            <h3>No pending documents</h3>
            <p>All documents have been reviewed.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Doc ID</th>
                  <th>Citizen ID</th>
                  <th>Type</th>
                  <th>Uploaded</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {documents.map(doc => (
                  <tr key={doc.documentId}>
                    <td>#{doc.documentId}</td>
                    <td>#{doc.citizenId}</td>
                    <td>{doc.docType}</td>
                    <td>{new Date(doc.uploadedDate).toLocaleDateString()}</td>
                    <td>
                      <button className="btn btn-small btn-secondary" style={{ marginRight: 6 }}
                        onClick={() => handleViewDocument(doc.documentId)}>
                        👁️ View
                      </button>
                      <button className="btn btn-small btn-success" style={{ marginRight: 6 }}
                        onClick={() => handleVerify(doc.documentId)}>
                        ✓ Verify
                      </button>
                      <button className="btn btn-small btn-danger"
                        onClick={() => openRejectModal(doc.documentId)}>
                        ✗ Reject
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Reject Modal */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Reject Document #{modal.documentId}</h3>
            <div className="form-group">
              <label>Reason for Rejection</label>
              <textarea value={remarks} onChange={e => setRemarks(e.target.value)}
                rows={3} placeholder="Enter rejection reason..." />
            </div>
            <div className="actions-row">
              <button className="btn btn-danger" onClick={handleReject} disabled={processing}>
                {processing ? 'Rejecting...' : 'Reject Document'}
              </button>
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default PendingDocumentsPage;

