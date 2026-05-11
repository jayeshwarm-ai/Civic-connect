import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllCitizens, deactivateCitizen } from '../../services/api';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, StatCard } from '../../components/ui';

function AdminDashboard() {
  const { user } = useAuth();
  const [citizens, setCitizens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchCitizens = async () => {
    try {
      const res = await getAllCitizens();
      setCitizens(res.data);
    } catch (err) {
      toast.error('Failed to load citizens.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCitizens(); }, []);

  const handleDeactivate = async (citizenId, name) => {
    if (!window.confirm(`Are you sure you want to deactivate ${name}'s account?`)) return;
    try {
      await deactivateCitizen(citizenId);
      toast.success(`${name}'s account has been deactivated.`);
      fetchCitizens();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Deactivation failed.');
    }
  };

  const statusBadge = (status) => {
    const cls = status === 'ACTIVE' ? 'badge-active'
      : status === 'SUSPENDED' ? 'badge-suspended' : 'badge-inactive';
    return <span className={`badge ${cls}`}>{status}</span>;
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading dashboard...</div>;

  const active = citizens.filter(c => c.accountStatus === 'ACTIVE').length;
  const inactive = citizens.filter(c => c.accountStatus === 'INACTIVE').length;
  const suspended = citizens.filter(c => c.accountStatus === 'SUSPENDED').length;

  const filtered = citizens.filter(c =>
    c.name.toLowerCase().includes(search.toLowerCase()) ||
    c.email.toLowerCase().includes(search.toLowerCase()) ||
    c.phone.includes(search)
  );

  return (
    <>
      {/* Welcome Banner */}
      <PageHeader icon="📊" title="Admin Dashboard" subtitle={<>Welcome back, {user?.name}. Manage citizens, verify documents, and oversee the CivicConnect platform.</>} />

      {/* Stats */}
      <div className="stats-row">
        <StatCard icon="👥" label="Total Citizens"      value={citizens.length} variant="primary" />
        <StatCard icon="✅" label="Active"              value={active}          variant="success" />
        <StatCard icon="⏳" label="Pending Verification" value={inactive}        variant="warning" />
        <StatCard icon="🚫" label="Suspended"           value={suspended}       variant="danger"  />
      </div>

      {/* Citizens Table */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-blue">👥</span> All Citizens
        </div>

        <div className="search-bar">
          <input
            type="text"
            placeholder="🔍 Search by name, email, or phone..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        {filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👤</div>
            <h3>{search ? 'No citizens match your search' : 'No citizens registered yet'}</h3>
            <p>{search ? 'Try a different search term.' : 'Citizens will appear here once they register.'}</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Citizen</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Registered</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(c => (
                  <tr key={c.citizenId}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <div style={{
                          width: 36, height: 36, borderRadius: '50%',
                          background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          color: 'white', fontWeight: 700, fontSize: '0.85rem', flexShrink: 0
                        }}>
                          {c.name?.charAt(0)?.toUpperCase()}
                        </div>
                        <div>
                          <div style={{ fontWeight: 700, color: '#0f172a' }}>{c.name}</div>
                          <div style={{ fontSize: '0.78rem', color: '#94a3b8' }}>ID: #{c.citizenId}</div>
                        </div>
                      </div>
                    </td>
                    <td>{c.email}</td>
                    <td>{c.phone}</td>
                    <td>{statusBadge(c.accountStatus)}</td>
                    <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <Link to={`/admin/citizens/${c.citizenId}`} className="btn btn-small btn-secondary">
                          👁️ View
                        </Link>
                        {c.accountStatus !== 'SUSPENDED' && (
                          <button className="btn btn-small btn-danger"
                            onClick={() => handleDeactivate(c.citizenId, c.name)}>
                            🚫
                          </button>
                        )}
                      </div>
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

export default AdminDashboard;

