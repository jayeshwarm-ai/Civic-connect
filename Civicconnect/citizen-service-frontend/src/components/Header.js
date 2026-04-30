import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUnreadCount } from '../services/api';

function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const handleLogout = () => { logout(); navigate('/login'); };
  const isActive = (path) => location.pathname === path ? 'active' : '';
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!user) return;
    const fetch = () => getUnreadCount(user.userId).then(r => setUnread(r.data)).catch(() => {});
    fetch();
    const interval = setInterval(fetch, 30000);
    return () => clearInterval(interval);
  }, [user]);

  const bellLink = (
    <Link to="/notifications" className={isActive('/notifications')} style={{ position: 'relative' }}>
      🔔{unread > 0 && (
        <span style={{
          position: 'absolute', top: 2, right: 2, background: '#ef4444', color: '#fff',
          borderRadius: '50%', width: 18, height: 18, fontSize: '.65rem', fontWeight: 800,
          display: 'flex', alignItems: 'center', justifyContent: 'center', lineHeight: 1,
        }}>{unread > 9 ? '9+' : unread}</span>
      )}
    </Link>
  );

  return (
    <header className="header">
      <Link to="/" className="header-logo">
        <div className="logo-icon">🏛️</div>
        Civic<span className="text-accent">Connect</span>
      </Link>
      <nav className="header-nav">
        {!user ? (
          <>
            <Link to="/" className={isActive('/')}>🏠 Home</Link>
            <Link to="/login" className={isActive('/login')}>🔑 Login</Link>
            <Link to="/register" className={isActive('/register')}>📝 Register</Link>
            <Link to="/reset-password" className={isActive('/reset-password')}>🔒 Reset Password</Link>
          </>
        ) : user.role === 'CITIZEN' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.charAt(0)?.toUpperCase()}</span>{user.name}</span>
            <Link to="/profile" className={isActive('/profile')}>👤 Profile</Link>
            <Link to="/documents" className={isActive('/documents')}>📄 Docs</Link>
            <Link to="/service-requests" className={isActive('/service-requests')}>📋 Requests</Link>
            <Link to="/feedback" className={isActive('/feedback')}>⭐ Feedback</Link>
            <Link to="/help" className={isActive('/help')}>❓ Help</Link>
            {bellLink}
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : user.role === 'SERVICE_OFFICER' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.charAt(0)?.toUpperCase()}</span>{user.name}</span>
            <Link to="/officer/dashboard" className={isActive('/officer/dashboard')}>📋 Assignments</Link>
            <Link to="/resolutions" className={isActive('/resolutions')}>🔧 Resolutions</Link>
            <Link to="/leaderboard" className={isActive('/leaderboard')}>🏆 Leaderboard</Link>
            <Link to="/admin/pending-documents" className={isActive('/admin/pending-documents')}>⏳ Docs</Link>
            {bellLink}
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : user.role === 'DEPARTMENT_HEAD' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.charAt(0)?.toUpperCase()}</span>{user.name}</span>
            <Link to="/admin/service-requests" className={isActive('/admin/service-requests')}>📋 Requests</Link>
            <Link to="/admin/pending-documents" className={isActive('/admin/pending-documents')}>⏳ Pending Docs</Link>
            <Link to="/reports" className={isActive('/reports')}>📊 Reports</Link>
            {bellLink}
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : user.role === 'COMPLIANCE_OFFICER' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.charAt(0)?.toUpperCase()}</span>{user.name}</span>
            <Link to="/compliance" className={isActive('/compliance')}>🛡️ Dashboard</Link>
            <Link to="/compliance/records/new" className={isActive('/compliance/records/new')}>➕ New Check</Link>
            <Link to="/compliance/audits/new" className={isActive('/compliance/audits/new')}>🔍 New Audit</Link>
            <Link to="/reports" className={isActive('/reports')}>📊 Reports</Link>
            {bellLink}
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : (
          <>
            <span className="header-user"><span className="avatar">{user.name?.charAt(0)?.toUpperCase()}</span>{user.name}</span>
            <Link to="/admin" className={isActive('/admin')}>📊 Dashboard</Link>
            <Link to="/admin/pending-documents" className={isActive('/admin/pending-documents')}>⏳ Docs</Link>
            <Link to="/admin/service-requests" className={isActive('/admin/service-requests')}>📋 Requests</Link>
            <Link to="/admin/staff" className={isActive('/admin/staff')}>👥 Staff</Link>
            <Link to="/leaderboard" className={isActive('/leaderboard')}>🏆 Leaderboard</Link>
            <Link to="/reports" className={isActive('/reports')}>📊 Reports</Link>
            {bellLink}
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;

