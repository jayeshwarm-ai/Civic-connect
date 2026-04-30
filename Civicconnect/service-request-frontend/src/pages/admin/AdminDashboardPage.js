import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getRequestsByStatus } from '../../services/api';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';

export default function AdminDashboardPage() {
  const { user } = useAuth();
  const [counts, setCounts] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const statuses = ['SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'];
        const results = await Promise.all(statuses.map(s => getRequestsByStatus(s).catch(() => ({data:[]}))));
        const c = {};
        statuses.forEach((s, i) => c[s] = results[i].data.length);
        setCounts(c);
      } catch {} finally { setLoading(false); }
    })();
  }, []);

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  const total = Object.values(counts).reduce((a,b)=>a+b, 0);

  return (
    <>
      <div className="welcome-banner">
        <h2>📊 Admin Dashboard</h2>
        <p>Welcome, {user?.name}. Manage and assign service requests across the platform.</p>
      </div>

      <div className="stats-row">
        <div className="stat-card"><div className="stat-icon icon-blue">📋</div><div><div className="stat-value">{total}</div><div className="stat-label">Total</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-blue">📝</div><div><div className="stat-value">{counts.SUBMITTED||0}</div><div className="stat-label">Submitted</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-orange">👤</div><div><div className="stat-value">{counts.ASSIGNED||0}</div><div className="stat-label">Assigned</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-blue">🔄</div><div><div className="stat-value">{counts.IN_PROGRESS||0}</div><div className="stat-label">In Progress</div></div></div>
        <div className="stat-card"><div className="stat-icon icon-green">✅</div><div><div className="stat-value">{counts.RESOLVED||0}</div><div className="stat-label">Resolved</div></div></div>
      </div>

      <div className="card">
        <div className="card-title"><span className="icon icon-blue">⚡</span> Quick Actions</div>
        <div className="actions-row">
          <Link to="/admin/requests" className="btn btn-primary">📋 View All Requests</Link>
          <Link to="/admin/requests?filter=SUBMITTED" className="btn btn-secondary">📝 Unassigned Requests ({counts.SUBMITTED||0})</Link>
        </div>
      </div>
    </>
  );
}

