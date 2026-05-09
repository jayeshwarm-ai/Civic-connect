import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getComplianceRecordById, getComplianceRecordsByResult } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader } from '../../components/ui';

const resultBadge = (r) => (
  <span className={`badge ${r === 'PASS' ? 'badge-verified' : 'badge-rejected'}`} style={{ fontSize: '.85rem', padding: '8px 18px' }}>
    {r === 'PASS' ? '✅ PASS' : '❌ FAIL'}
  </span>
);

// Simple SVG Pie Chart component
function PieChart({ pass, fail }) {
  const total = pass + fail;
  if (total === 0) return <p style={{ color: 'var(--gray-400)', textAlign: 'center' }}>No data</p>;
  const passAngle = (pass / total) * 360;
  const passRad = (passAngle - 90) * (Math.PI / 180);
  const failRad = (360 - 90) * (Math.PI / 180);
  const largeArc = passAngle > 180 ? 1 : 0;
  const px = 50 + 40 * Math.cos(passRad);
  const py = 50 + 40 * Math.sin(passRad);
  return (
    <div style={{ textAlign: 'center' }}>
      <svg width="160" height="160" viewBox="0 0 100 100">
        <circle cx="50" cy="50" r="40" fill="#fee2e2" />
        {pass > 0 && (
          <path d={`M50,50 L50,10 A40,40 0 ${largeArc},1 ${px},${py} Z`} fill="#bbf7d0" />
        )}
        <circle cx="50" cy="50" r="22" fill="white" />
        <text x="50" y="48" textAnchor="middle" fontSize="9" fontWeight="800" fill="#1e293b">{total}</text>
        <text x="50" y="58" textAnchor="middle" fontSize="5" fill="#64748b">Total</text>
      </svg>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 20, marginTop: 8 }}>
        <span style={{ fontSize: '.8rem' }}><span style={{ display: 'inline-block', width: 10, height: 10, background: '#bbf7d0', borderRadius: 2, marginRight: 4 }}></span>Pass ({pass})</span>
        <span style={{ fontSize: '.8rem' }}><span style={{ display: 'inline-block', width: 10, height: 10, background: '#fee2e2', borderRadius: 2, marginRight: 4 }}></span>Fail ({fail})</span>
      </div>
    </div>
  );
}

export default function ComplianceRecordDetailPage() {
  const { complianceId } = useParams();
  const navigate = useNavigate();
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ pass: 0, fail: 0 });

  useEffect(() => {
    (async () => {
      try {
        const [res, passRes, failRes] = await Promise.all([
          getComplianceRecordById(complianceId),
          getComplianceRecordsByResult('PASS').catch(() => ({ data: [] })),
          getComplianceRecordsByResult('FAIL').catch(() => ({ data: [] })),
        ]);
        setRecord(res.data);
        setStats({ pass: (passRes.data || []).length, fail: (failRes.data || []).length });
      } catch {
        toast.error('Failed to load compliance record.');
      } finally {
        setLoading(false);
      }
    })();
  }, [complianceId]);

  const handleDownloadReport = () => {
    if (!record) return;
    const reportText = `
COMPLIANCE RECORD REPORT
========================
Compliance ID: #${record.complianceId}
Type: ${record.type}
Entity ID: #${record.entityId}
Result: ${record.result}
Reviewed By: ${record.createdByName} (ID: #${record.createdByUserId})
Created: ${new Date(record.createdAt).toLocaleString()}

FINDINGS / NOTES:
${record.notes}

---
Generated on: ${new Date().toLocaleString()}
CivicConnect Compliance System
    `.trim();
    const blob = new Blob([reportText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `compliance_record_${record.complianceId}.txt`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('📥 Report downloaded!');
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;
  if (!record) return <div className="card"><div className="error-msg">Compliance record not found.</div></div>;

  return (
    <>
      <PageHeader icon="🛡️" title={<>Compliance Record #{record.complianceId}</>} subtitle="Detailed compliance review information" />

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, alignItems: 'start' }}>
        <div className="card">
          <div className="card-title">
            <span className="icon icon-blue">📋</span> Record Details
            <span style={{ marginLeft: 'auto' }}>{resultBadge(record.result)}</span>
          </div>

          <div className="profile-grid">
            <div className="profile-item"><label>🆔 Compliance ID</label><div className="value">#{record.complianceId}</div></div>
            <div className="profile-item"><label>📂 Type</label><div className="value">{record.type}</div></div>
            <div className="profile-item"><label>🔗 Entity ID</label><div className="value">#{record.entityId}</div></div>
            <div className="profile-item"><label>👮 Reviewed By</label><div className="value">{record.createdByName} (ID: #{record.createdByUserId})</div></div>
            <div className="profile-item"><label>📅 Created</label><div className="value">{new Date(record.createdAt).toLocaleString()}</div></div>
            <div className="profile-item"><label>📊 Result</label><div className="value">{record.result}</div></div>
          </div>

          <div style={{ marginTop: 24, padding: 20, background: record.result === 'FAIL' ? '#fef2f2' : '#f0fdf4', borderRadius: 14, border: `1px solid ${record.result === 'FAIL' ? '#fecaca' : '#bbf7d0'}` }}>
            <label style={{ fontSize: '.75rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>
              📝 Findings / Notes
            </label>
            <p style={{ fontSize: '.95rem', lineHeight: 1.7, color: 'var(--gray-700)' }}>{record.notes}</p>
          </div>

          {record.result === 'FAIL' && (
            <div className="info-tip" style={{ width: '100%', marginTop: 20, background: '#fef2f2', borderColor: '#fecaca', color: '#991b1b' }}>
              <span className="tip-icon">🔔</span>
              A notification was sent to the responsible officer regarding this compliance failure.
            </div>
          )}

          <div className="actions-row">
            <button className="btn btn-secondary" onClick={() => navigate(-1)}>← Back</button>
            <button className="btn btn-primary" onClick={handleDownloadReport}>📥 Download Report</button>
            <button className="btn btn-outline" onClick={() => navigate('/compliance')}>📋 All Records</button>
          </div>
        </div>

        {/* Statistics Pie Chart */}
        <div className="card">
          <div className="card-title"><span className="icon icon-green">📊</span> Compliance Statistics</div>
          <PieChart pass={stats.pass} fail={stats.fail} />
          <div style={{ marginTop: 16, textAlign: 'center' }}>
            <div style={{ fontSize: '.85rem', color: 'var(--gray-500)' }}>
              Pass Rate: <strong style={{ color: '#059669' }}>{stats.pass + stats.fail > 0 ? ((stats.pass / (stats.pass + stats.fail)) * 100).toFixed(1) : 0}%</strong>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
