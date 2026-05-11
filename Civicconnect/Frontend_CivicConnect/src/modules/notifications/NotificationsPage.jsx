import React, { useState, useEffect, useCallback } from 'react';
import { getNotifications, getUnreadNotifications, getNotificationsByCategory, markNotificationAsRead, dismissNotification } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { PageHeader, StatCard } from '../../components/ui';

const catIcon = (c) => c === 'REQUEST' ? '📋' : c === 'RESOLUTION' ? '🔧' : c === 'FEEDBACK' ? '⭐' : c === 'COMPLIANCE' ? '🛡️' : c === 'REPORT' ? '📊' : '🔔';
const catBadge = (c) => (
  <span className={`badge ${c === 'REQUEST' ? 'badge-submitted' : c === 'RESOLUTION' ? 'badge-in_progress' : c === 'FEEDBACK' ? 'badge-assigned' : c === 'COMPLIANCE' ? 'badge-pending' : 'badge-resolved'}`}>
    {catIcon(c)} {c}
  </span>
);

const categories = ['ALL', 'REQUEST', 'RESOLUTION', 'FEEDBACK', 'COMPLIANCE', 'REPORT'];

export default function NotificationsPage() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [showFilter, setShowFilter] = useState('ALL_STATUS'); // ALL_STATUS, UNREAD, READ, DISMISSED

  const fetchNotifications = useCallback(async () => {
    try {
      let res;
      if (filter === 'ALL') {
        if (showFilter === 'UNREAD') {
          res = await getUnreadNotifications(user.userId);
        } else {
          res = await getNotifications(user.userId);
        }
      } else {
        res = await getNotificationsByCategory(user.userId, filter);
      }
      let data = res.data;
      if (showFilter === 'READ') data = data.filter(n => n.status === 'READ');
      if (showFilter === 'DISMISSED') data = data.filter(n => n.status === 'DISMISSED');
      if (showFilter === 'UNREAD' && filter !== 'ALL') data = data.filter(n => n.status === 'UNREAD');
      setNotifications(data);
    } catch {
      toast.error('Failed to load notifications.');
    } finally {
      setLoading(false);
    }
  }, [user.userId, filter, showFilter]);

  useEffect(() => { setLoading(true); fetchNotifications(); }, [fetchNotifications]);

  const handleMarkRead = async (id) => {
    try {
      await markNotificationAsRead(id);
      setNotifications(prev => prev.map(n => n.notificationId === id ? { ...n, status: 'READ' } : n));
      toast.success('Marked as read');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed');
    }
  };

  const handleDismiss = async (id) => {
    try {
      await dismissNotification(id);
      setNotifications(prev => prev.map(n => n.notificationId === id ? { ...n, status: 'DISMISSED' } : n));
      toast.success('Dismissed');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed');
    }
  };

  const handleMarkAllRead = async () => {
    const unread = notifications.filter(n => n.status === 'UNREAD');
    for (const n of unread) {
      try { await markNotificationAsRead(n.notificationId); } catch {}
    }
    setNotifications(prev => prev.map(n => n.status === 'UNREAD' ? { ...n, status: 'READ' } : n));
    toast.success(`${unread.length} notifications marked as read`);
  };

  const unreadCount = notifications.filter(n => n.status === 'UNREAD').length;

  if (loading) return <div className="loading"><div className="spinner"></div><br />Loading...</div>;

  return (
    <>
      <PageHeader icon="🔔" title="Notifications" subtitle="Stay updated with all activities across CivicConnect services." />

      <div className="stats-row">
        <StatCard icon="🔔" label="Total"  value={notifications.length} variant="primary" />
        <StatCard icon="🔴" label="Unread" value={unreadCount}           variant="danger"  />
      </div>

      {/* Filters */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 20 }}>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            {categories.map(c => (
              <button key={c} className={`btn btn-small ${filter === c ? 'btn-primary' : 'btn-outline'}`} onClick={() => setFilter(c)}>
                {c === 'ALL' ? '📬 All' : `${catIcon(c)} ${c}`}
              </button>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 6 }}>
            {['ALL_STATUS', 'UNREAD', 'READ', 'DISMISSED'].map(s => (
              <button key={s} className={`btn btn-small ${showFilter === s ? 'btn-secondary' : 'btn-outline'}`}
                onClick={() => setShowFilter(s)} style={{ fontSize: '.75rem' }}>
                {s === 'ALL_STATUS' ? 'All' : s === 'UNREAD' ? '🔴 Unread' : s === 'READ' ? '✅ Read' : '🗑️ Dismissed'}
              </button>
            ))}
          </div>
        </div>

        {unreadCount > 0 && (
          <div style={{ marginBottom: 16 }}>
            <button className="btn btn-small btn-success" onClick={handleMarkAllRead}>✅ Mark All as Read ({unreadCount})</button>
          </div>
        )}

        {notifications.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔔</div>
            <h3>No notifications</h3>
            <p>You're all caught up! Notifications from all services will appear here.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {notifications.map(n => (
              <div key={n.notificationId} style={{
                display: 'flex', alignItems: 'flex-start', gap: 16, padding: '18px 20px',
                background: n.status === 'UNREAD' ? '#eff6ff' : n.status === 'DISMISSED' ? 'var(--gray-50)' : 'var(--white)',
                borderRadius: 'var(--radius-sm)', border: `1px solid ${n.status === 'UNREAD' ? '#bfdbfe' : 'var(--gray-100)'}`,
                opacity: n.status === 'DISMISSED' ? 0.6 : 1, transition: 'var(--transition)',
              }}>
                <div style={{
                  width: 44, height: 44, borderRadius: 12, display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '1.3rem', flexShrink: 0,
                  background: n.status === 'UNREAD' ? '#dbeafe' : 'var(--gray-100)',
                }}>
                  {catIcon(n.category)}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    {catBadge(n.category)}
                    {n.status === 'UNREAD' && <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#3b82f6', flexShrink: 0 }}></span>}
                    <span style={{ fontSize: '.8rem', color: 'var(--gray-400)', marginLeft: 'auto', flexShrink: 0 }}>
                      {new Date(n.createdDate).toLocaleString()}
                    </span>
                  </div>
                  <p style={{ fontSize: '.92rem', color: 'var(--gray-700)', lineHeight: 1.5, margin: 0 }}>{n.message}</p>
                  {n.requestId && (
                    <span style={{ fontSize: '.8rem', color: 'var(--gray-400)', marginTop: 4, display: 'inline-block' }}>
                      📋 Request #{n.requestId}
                    </span>
                  )}
                </div>
                <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                  {n.status === 'UNREAD' && (
                    <button className="btn btn-small btn-secondary" onClick={() => handleMarkRead(n.notificationId)} title="Mark as read">✅</button>
                  )}
                  {n.status !== 'DISMISSED' && (
                    <button className="btn btn-small btn-outline" onClick={() => handleDismiss(n.notificationId)} title="Dismiss" style={{ padding: '6px 10px' }}>🗑️</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

