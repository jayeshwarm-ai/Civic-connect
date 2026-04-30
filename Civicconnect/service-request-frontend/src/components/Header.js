import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const loc = useLocation();
  const is = (p) => loc.pathname === p ? 'active' : '';

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <header className="header">
      <Link to="/" className="header-logo">
        <div className="logo-icon">📋</div>
        Service<span className="accent">Requests</span>
      </Link>
      <nav className="header-nav">
        {!user ? (
          <Link to="/login" className={is('/login')}>🔑 Login</Link>
        ) : user.role === 'CITIZEN' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.[0]?.toUpperCase()}</span>{user.name}</span>
            <Link to="/my-requests" className={is('/my-requests')}>📋 My Requests</Link>
            <Link to="/submit-request" className={is('/submit-request')}>➕ New Request</Link>
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : user.role === 'SERVICE_OFFICER' ? (
          <>
            <span className="header-user"><span className="avatar">{user.name?.[0]?.toUpperCase()}</span>{user.name}</span>
            <Link to="/officer" className={is('/officer')}>📋 My Assignments</Link>
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        ) : (
          <>
            <span className="header-user"><span className="avatar">{user.name?.[0]?.toUpperCase()}</span>{user.name} ({user.role?.replace('_',' ')})</span>
            <Link to="/admin" className={is('/admin')}>📊 Dashboard</Link>
            <Link to="/admin/requests" className={is('/admin/requests')}>📋 All Requests</Link>
            <button className="btn-logout" onClick={handleLogout}>🚪 Logout</button>
          </>
        )}
      </nav>
    </header>
  );
}

