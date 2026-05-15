import React from 'react';

/* ═════════════════════════════════════════════════════════════════════════
   Header / Navbar / Menu SVG icons.

   ⚠ These icons are intended ONLY for the Header (top bar) and the
   hamburger / profile menus. They are NOT to be used as decorative icons
   inside dashboards, cards, tables, or any page content.

   All icons inherit color from `currentColor`, so styling them is just a
   matter of setting CSS `color` on the parent. Sizes are controlled via
   the `size` prop (defaults to 18px square).
   ═════════════════════════════════════════════════════════════════════════ */

const IconSvg = ({ size = 18, children, ...rest }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    {...rest}
  >
    {children}
  </svg>
);

/* ── Brand emblem — civic building (columns + roof) ─────────────────── */
export const BrandIcon = (props) => (
  <IconSvg {...props}>
    <path d="M3 21h18" />
    <path d="M5 21V10l7-5 7 5v11" />
    <path d="M9 21V13" />
    <path d="M15 21V13" />
    <path d="M12 21V16" />
  </IconSvg>
);

/* ── Notification bell ──────────────────────────────────────────────── */
export const BellIcon = (props) => (
  <IconSvg {...props}>
    <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
    <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
  </IconSvg>
);

/* ── Hamburger (3 lines) ────────────────────────────────────────────── */
export const MenuIcon = (props) => (
  <IconSvg {...props}>
    <line x1="3"  y1="6"  x2="21" y2="6"  />
    <line x1="3"  y1="12" x2="21" y2="12" />
    <line x1="3"  y1="18" x2="21" y2="18" />
  </IconSvg>
);

/* ── Profile dropdown items ─────────────────────────────────────────── */
export const HomeIcon = (props) => (
  <IconSvg {...props}>
    <path d="M3 11l9-8 9 8" />
    <path d="M5 10v10h14V10" />
  </IconSvg>
);

export const UserIcon = (props) => (
  <IconSvg {...props}>
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </IconSvg>
);

/**
 * Help — the universal "?" inside a circle. Per design instruction we keep
 * the question-mark shape since it is the most widely recognised help symbol.
 */
export const HelpIcon = (props) => (
  <IconSvg {...props}>
    <circle cx="12" cy="12" r="10" />
    <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
    <line x1="12" y1="17" x2="12.01" y2="17" />
  </IconSvg>
);

export const LogoutIcon = (props) => (
  <IconSvg {...props}>
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <polyline points="16 17 21 12 16 7" />
    <line x1="21" y1="12" x2="9" y2="12" />
  </IconSvg>
);

/* ── Hamburger-menu link icons (role-specific navigation) ───────────── */
export const DashboardIcon = (props) => (
  <IconSvg {...props}>
    <rect x="3" y="3"  width="7" height="9" />
    <rect x="14" y="3" width="7" height="5" />
    <rect x="14" y="12" width="7" height="9" />
    <rect x="3" y="16" width="7" height="5" />
  </IconSvg>
);

export const RequestsIcon = (props) => (
  <IconSvg {...props}>
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="9" y1="13" x2="15" y2="13" />
    <line x1="9" y1="17" x2="15" y2="17" />
  </IconSvg>
);

export const DocumentsIcon = (props) => (
  <IconSvg {...props}>
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
  </IconSvg>
);

export const FeedbackIcon = (props) => (
  <IconSvg {...props}>
    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
  </IconSvg>
);

export const StaffIcon = (props) => (
  <IconSvg {...props}>
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </IconSvg>
);

export const ComplianceIcon = (props) => (
  <IconSvg {...props}>
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
  </IconSvg>
);

export const ReportsIcon = (props) => (
  <IconSvg {...props}>
    <line x1="18" y1="20" x2="18" y2="10" />
    <line x1="12" y1="20" x2="12" y2="4"  />
    <line x1="6"  y1="20" x2="6"  y2="14" />
  </IconSvg>
);

export const LeaderboardIcon = (props) => (
  <IconSvg {...props}>
    <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
    <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
    <path d="M4 22h16" />
    <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22" />
    <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22" />
    <path d="M18 2H6v7a6 6 0 0 0 12 0V2z" />
  </IconSvg>
);

export const KeyIcon = (props) => (
  <IconSvg {...props}>
    <circle cx="7.5" cy="15.5" r="5.5" />
    <path d="M21 2l-9.6 9.6" />
    <path d="M15.5 7.5l3 3L22 7l-3-3" />
  </IconSvg>
);

export const PlusCircleIcon = (props) => (
  <IconSvg {...props}>
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8"  x2="12" y2="16" />
    <line x1="8"  y1="12" x2="16" y2="12" />
  </IconSvg>
);

export const ClipboardCheckIcon = (props) => (
  <IconSvg {...props}>
    <path d="M9 2h6a2 2 0 0 1 2 2v2H7V4a2 2 0 0 1 2-2z" />
    <path d="M7 4H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-2" />
    <polyline points="9 14 11 16 15 12" />
  </IconSvg>
);