import React, { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUnreadCount } from '../services/api';

/* ═════════════════════════════════════════════════════════════════════════
   Header — Government-of-India style.

   Layout:
     ┌──────────────────────────────────────────────────────────────────────┐
     │  [☰] [emblem] CivicConnect       [Help] [🔔] [Profile chip ▾]       │
     └──────────────────────────────────────────────────────────────────────┘
                ↓ click ☰
     ┌──────────────────────────────────────────────────────────────────────┐
     │  Role-specific links (Dashboard, Documents, Requests, Reports, ...) │
     └──────────────────────────────────────────────────────────────────────┘

   • The hamburger holds every role-specific link, keeping the top bar clean.
   • Help, Notifications, and the Profile chip live on the right.
   • Profile dropdown carries: Home / Profile / Logout (with confirm).
   • Guests see only Home / About / Login-Register — no hamburger needed.
   ═════════════════════════════════════════════════════════════════════════ */
function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? 'active' : '';

  // Notification polling — unchanged from before.
  const [unread, setUnread] = useState(0);
  useEffect(() => {
    if (!user) return;
    const fetchCount = () => getUnreadCount(user.userId).then(r => setUnread(r.data)).catch(() => {});
    fetchCount();
    const id = setInterval(fetchCount, 30000);
    return () => clearInterval(id);
  }, [user]);

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <header className="gov-header">
      <TopStrip />
      <div className="gov-tricolour" aria-hidden="true" />
      <div className="gov-header__main">
        <div className="gov-header__left">
          {user && <HamburgerMenu user={user} isActive={isActive} />}
          <Brand />
        </div>

        <nav className="gov-nav" aria-label="Primary">
          {!user ? (
            <GuestNav isActive={isActive} />
          ) : (
            <>
              <NotifBell count={unread} isActive={isActive} />
              <ProfileMenu user={user} onLogout={handleLogout} />
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

/* ─── Top strip + tricolour + brand (unchanged) ────────────────────── */

function TopStrip() {
  return (
    <div className="gov-header__top">
      <div className="gov-header__top-left">
        <span>Government of India</span>
        <span style={{ opacity: 0.6 }}>|</span>
        <span>Ministry of Housing &amp; Urban Affairs</span>
      </div>
    </div>
  );
}

function Brand() {
  return (
    <Link to="/" className="gov-brand" aria-label="CivicConnect home">
      <div className="gov-brand__emblem" aria-hidden="true">🏛️</div>
      <div>
        <div className="gov-brand__name">CivicConnect</div>
        <div className="gov-brand__tag">Citizen Services Portal</div>
      </div>
    </Link>
  );
}

function GuestNav({ isActive }) {
  return (
    <>
      <Link to="/"      className={isActive('/')}>Home</Link>
      <Link to="/about" className={isActive('/about')}>About</Link>
      <Link to="/login" className="btn-cta">Login / Register</Link>
    </>
  );
}

function NotifBell({ count, isActive }) {
  return (
    <Link to="/notifications" className={`gov-bell ${isActive('/notifications')}`} aria-label="Notifications" title="Notifications">
      🔔
      {count > 0 && <span className="gov-bell__count">{count > 9 ? '9+' : count}</span>}
    </Link>
  );
}

/* ═════════════════════════════════════════════════════════════════════════
   Hamburger menu — role-specific navigation
   ═════════════════════════════════════════════════════════════════════════ */

function HamburgerMenu({ user, isActive }) {
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef(null);

  // Close on outside click and Escape
  useEffect(() => {
    if (!open) return;
    const onDocClick = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setOpen(false);
    };
    const onEsc = (e) => { if (e.key === 'Escape') setOpen(false); };
    document.addEventListener('mousedown', onDocClick);
    document.addEventListener('keydown', onEsc);
    return () => {
      document.removeEventListener('mousedown', onDocClick);
      document.removeEventListener('keydown', onEsc);
    };
  }, [open]);

  const links = roleLinks(user.role);

  return (
    <div className="gov-hamburger" ref={wrapperRef}>
      <button
        className="gov-hamburger__btn"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label="Open navigation menu"
        onClick={() => setOpen(v => !v)}
      >
        <span className="gov-hamburger__icon" aria-hidden="true">
          <span></span><span></span><span></span>
        </span>
      </button>

      {open && (
        <div className="gov-hamburger__menu" role="menu">
          <div className="gov-hamburger__heading">{user.role.replace(/_/g, ' ')} MENU</div>
          {links.map(({ to, label, icon }) => (
            <Link
              key={to}
              to={to}
              role="menuitem"
              onClick={() => setOpen(false)}
              className={`gov-hamburger__item ${isActive(to)}`}
            >
              <span className="gov-hamburger__item-icon" aria-hidden="true">{icon}</span>
              {label}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

/**
 * Returns the role-specific navigation links shown inside the hamburger panel.
 * Each entry: { to, label, icon }. Keeping role-specific labels here keeps the
 * Header's top section clean and easy to read.
 */
function roleLinks(role) {
  switch (role) {
    case 'CITIZEN':
      return [
        { to: '/service-requests', label: 'My Requests',   icon: '📋' },
        { to: '/documents',        label: 'Documents',     icon: '📄' },
        { to: '/feedback',         label: 'Feedback',      icon: '💬' },
      ];

    case 'SERVICE_OFFICER':
      return [
        { to: '/officer/dashboard',        label: 'Assignments',  icon: '🧰' },
        { to: '/resolutions',              label: 'Resolutions',  icon: '🔧' },
        { to: '/leaderboard',              label: 'Leaderboard',  icon: '🏆' },
        { to: '/admin/pending-documents',  label: 'Documents',    icon: '📄' },
      ];

    case 'DEPARTMENT_HEAD':
      return [
        { to: '/admin/service-requests',   label: 'Requests',     icon: '📋' },
        { to: '/admin/pending-documents',  label: 'Pending Docs', icon: '📄' },
        { to: '/reports',                  label: 'Reports',      icon: '📊' },
      ];

    case 'COMPLIANCE_OFFICER':
      return [
        { to: '/compliance',              label: 'Dashboard',  icon: '🛡️' },
        { to: '/compliance/records/new',  label: 'New Check',  icon: '➕' },
        { to: '/compliance/audits/new',   label: 'New Audit',  icon: '🔍' },
        { to: '/reports',                 label: 'Reports',    icon: '📊' },
      ];

    default: // CITY_ADMINISTRATOR
      return [
        { to: '/admin',                    label: 'Dashboard',     icon: '📊' },
        { to: '/admin/pending-documents',  label: 'Documents',     icon: '📄' },
        { to: '/admin/service-requests',   label: 'Requests',      icon: '📋' },
        { to: '/admin/staff',              label: 'Staff',         icon: '👥' },
        { to: '/compliance',               label: 'Compliance',    icon: '🛡️' },
        { to: '/compliance/records/new',   label: 'New Check',     icon: '➕' },
        { to: '/compliance/audits/new',    label: 'New Audit',     icon: '🔍' },
        { to: '/leaderboard',              label: 'Leaderboard',   icon: '🏆' },
        { to: '/reports',                  label: 'Reports',       icon: '📈' },
      ];
  }
}

/* ═════════════════════════════════════════════════════════════════════════
   Profile dropdown — Home / Profile / Logout (with confirm)
   ═════════════════════════════════════════════════════════════════════════ */

function ProfileMenu({ user, onLogout }) {
  const [open, setOpen] = useState(false);
  const [confirmingLogout, setConfirmingLogout] = useState(false);
  const wrapperRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    const onDocClick = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false); setConfirmingLogout(false);
      }
    };
    const onEsc = (e) => { if (e.key === 'Escape') { setOpen(false); setConfirmingLogout(false); } };
    document.addEventListener('mousedown', onDocClick);
    document.addEventListener('keydown', onEsc);
    return () => {
      document.removeEventListener('mousedown', onDocClick);
      document.removeEventListener('keydown', onEsc);
    };
  }, [open]);

  const initial = user.name?.charAt(0)?.toUpperCase() || '?';
  // The role's home route — what the dashboard auto-redirect chooses.
  const roleHome =
      user.role === 'CITY_ADMINISTRATOR'  ? '/admin'
    : user.role === 'SERVICE_OFFICER'     ? '/officer/dashboard'
    : user.role === 'DEPARTMENT_HEAD'     ? '/admin/service-requests'
    : user.role === 'COMPLIANCE_OFFICER'  ? '/compliance'
    : '/profile';

  return (
    <div className="gov-profile" ref={wrapperRef}>
      <button
        className="gov-profile__chip"
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen(v => !v)}
      >
        <span className="gov-user__avatar">{initial}</span>
        <span className="gov-profile__chip-name">{user.name}</span>
        <span className="gov-profile__chip-caret" aria-hidden="true">▾</span>
      </button>

      {open && (
        <div className="gov-profile__menu" role="menu">
          {/* Quick links */}
          <Link
            to={roleHome}
            className="gov-profile__item"
            role="menuitem"
            onClick={() => setOpen(false)}
          >
            <span className="gov-profile__item-icon">🏠</span>
            Home
          </Link>
          <Link
            to="/profile"
            className="gov-profile__item"
            role="menuitem"
            onClick={() => setOpen(false)}
          >
            <span className="gov-profile__item-icon">👤</span>
            Profile
          </Link>
          <Link
            to="/help"
            className="gov-profile__item"
            role="menuitem"
            onClick={() => setOpen(false)}
          >
            <span className="gov-profile__item-icon">🆘</span>
            Help
          </Link>
          <div className="gov-profile__divider" />

          {/* Logout — confirmation flow */}
          {!confirmingLogout ? (
            <button
              className="gov-profile__item gov-profile__item--danger"
              role="menuitem"
              onClick={() => setConfirmingLogout(true)}
            >
              <span className="gov-profile__item-icon">🚪</span>
              Logout
            </button>
          ) : (
            <div className="gov-profile__confirm">
              <div className="gov-profile__confirm-text">Are you sure you want to logout?</div>
              <div className="gov-profile__confirm-actions">
                <button className="btn btn-small btn-outline" onClick={() => setConfirmingLogout(false)}>
                  Cancel
                </button>
                <button className="btn btn-small btn-danger" onClick={() => { setOpen(false); onLogout(); }}>
                  Yes, logout
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Header;
