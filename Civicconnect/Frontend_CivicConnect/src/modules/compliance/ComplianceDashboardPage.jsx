import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getComplianceRecordsByResult, getAuditsByOfficer, getAllAudits, getServiceRequestsByStatus, getComplianceRecordsByEntity } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, StatCard } from '../../components/ui';
import { toast } from 'react-toastify';

const statusBadge = (s) => <span className={`badge badge-${s.toLowerCase()}`}>{s.replace('_', ' ')}</span>;
const resultBadge = (r) => (
  <span className={`badge ${r === 'PASS' ? 'badge-verified' : 'badge-rejected'}`}>
    {r === 'PASS' ? '✅ PASS' : '❌ FAIL'}
  </span>
);

export default function ComplianceDashboardPage() {
  const { user } = useAuth();
  const [passRecords, setPassRecords] = useState([]);
  const [failRecords, setFailRecords] = useState([]);
  const [audits, setAudits] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('pending');

  const isAdmin = user.role === 'CITY_ADMINISTRATOR';

  useEffect(() => {
    (async () => {
      try {
        const [passRes, failRes, auditsRes, closedRes] = await Promise.all([
          getComplianceRecordsByResult('PASS').catch(() => ({ data: [] })),
          getComplianceRecordsByResult('FAIL').catch(() => ({ data: [] })),
          (isAdmin ? getAllAudits() : getAuditsByOfficer(user.userId)).catch(() => ({ data: [] })),
          getServiceRequestsByStatus('CLOSED').catch(() => ({ data: [] })),
        ]);
        setPassRecords(passRes.data);
        setFailRecords(failRes.data);
        setAudits(auditsRes.data);

        // Filter out requests that already have compliance records
        const closedRequests = closedRes.data || [];
        const pending = [];
        for (const req of closedRequests) {
          try {
            const existing = await getComplianceRecordsByEntity('REQUEST', req.requestId);
            if (!existing.data || existing.data.length === 0) {
              pending.push(req);
            }
          } catch {
            pending.push(req); // If check fails, show it anyway
          }
        }
        setPendingRequests(pending);
      } catch {
        toast.error('Failed to load compliance data.');
      } finally {
        setLoading(false);
      }
    })();
  }, [user.userId, isAdmin]);

  const allRecords = [...passRecords, ...failRecords].sort(
    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
  );

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;

  return (
    <>
      <PageHeader
        icon="🛡️"
        title="Compliance Dashboard"
        subtitle="Review compliance records, manage audits, and ensure regulatory standards are met."
      />

      <div className="stats-row">
        <StatCard icon="⏳" label="Pending Review" value={pendingRequests.length} variant="warning" />
        <StatCard icon="📋" label="Total Records"  value={allRecords.length}     variant="primary" />
        <StatCard icon="✅" label="Passed"         value={passRecords.length}    variant="success" />
        <StatCard icon="❌" label="Failed"         value={failRecords.length}    variant="danger"  />
      </div>

      {/* Tab buttons */}
      <div className="actions-row" style={{ marginTop: 0, marginBottom: 24 }}>
        <button className={`btn ${tab === 'pending' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('pending')}>
          ⏳ Pending Review ({pendingRequests.length})
        </button>
        <button className={`btn ${tab === 'records' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('records')}>
          📋 Compliance Records
        </button>
        <button className={`btn ${tab === 'audits' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('audits')}>
          🔍 {isAdmin ? 'All Audits' : 'My Audits'}
        </button>
        <Link to="/compliance/audits/new" className="btn btn-secondary">➕ New Audit</Link>
      </div>

      {/* Pending compliance review - closed requests without compliance records */}
      {tab === 'pending' && (
        <div className="card">
          <div className="card-title"><span className="icon icon-orange">⏳</span> Closed Requests — Pending Compliance Review ({pendingRequests.length})</div>
          {pendingRequests.length > 0 && (
            <div className="info-tip" style={{ width: '100%', marginBottom: 16 }}>
              <span className="tip-icon">💡</span>
              These service requests have been <strong>CLOSED</strong> by citizens but have no compliance record yet. Review and create a PASS/FAIL record for each.
            </div>
          )}
          {pendingRequests.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">✅</div>
              <h3>All caught up!</h3>
              <p>No closed service requests pending compliance review.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Request #</th><th>Citizen</th><th>Type</th><th>Location</th><th>Officer</th><th>Closed Date</th><th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingRequests.map(r => (
                    <tr key={r.requestId}>
                      <td style={{ fontWeight: 700 }}>#{r.requestId}</td>
                      <td>{r.citizenName}</td>
                      <td><span className="badge badge-submitted">{r.type}</span></td>
                      <td
                        style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                        title={`${r.address || ''}${r.address ? ', ' : ''}${r.city || ''}, ${r.state || ''}`.trim()}
                      >
                        {r.city || '—'}{r.state ? `, ${r.state}` : ''}
                      </td>
                      <td>{r.assignedOfficerName || '—'}</td>
                      <td>{new Date(r.updatedAt).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/compliance/records/new?type=REQUEST&entityId=${r.requestId}`} className="btn btn-small btn-primary">
                          🛡️ Create Record
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {tab === 'records' && (
        <div className="card">
          <div className="card-title"><span className="icon icon-blue">📋</span> Compliance Records ({allRecords.length})</div>
          {allRecords.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <h3>No compliance records yet</h3>
              <p>Create a compliance check for a completed/closed service request or resolution.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Type</th><th>Entity ID</th><th>Result</th><th>Officer</th><th>Created</th><th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {allRecords.map(r => (
                    <tr key={r.complianceId}>
                      <td style={{ fontWeight: 700 }}>#{r.complianceId}</td>
                      <td><span className={`badge ${r.type === 'REQUEST' ? 'badge-submitted' : 'badge-in_progress'}`}>{r.type}</span></td>
                      <td style={{ fontWeight: 600 }}>#{r.entityId}</td>
                      <td>{resultBadge(r.result)}</td>
                      <td>{r.createdByName}</td>
                      <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/compliance/records/${r.complianceId}`} className="btn btn-small btn-secondary">👁️ View</Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {tab === 'audits' && (
        <div className="card">
          <div className="card-title"><span className="icon icon-green">🔍</span> {isAdmin ? 'All Audits' : 'My Audits'} ({audits.length})</div>
          {audits.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3>No audits yet</h3>
              <p>{isAdmin ? 'No audit records have been created yet.' : 'Create an audit to review compliance for a scope of requests or resolutions.'}</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    {isAdmin && <th>Officer</th>}
                    <th>Scope</th><th>Status</th><th>Findings</th><th>Created</th><th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {audits.map(a => (
                    <tr key={a.auditId}>
                      <td style={{ fontWeight: 700 }}>#{a.auditId}</td>
                      {isAdmin && <td>{a.officerName || `#${a.officerUserId}`}</td>}
                      <td style={{ maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{a.scope}</td>
                      <td>{statusBadge(a.status)}</td>
                      <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {a.findings || <span style={{ color: 'var(--gray-400)' }}>—</span>}
                      </td>
                      <td>{new Date(a.createdAt).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/compliance/audits/${a.auditId}`} className="btn btn-small btn-secondary">👁️ Manage</Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </>
  );
}

