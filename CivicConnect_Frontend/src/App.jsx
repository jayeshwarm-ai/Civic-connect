import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';
import './styles/gov-theme.css';            // ← Government-style theme (loaded after App.css)

import Header from './components/Header';
import Footer from './components/Footer';

// ── Module imports (grouped by feature) ─────────────────────────────────
import { LoginPage, RegisterPage, ResetPasswordPage } from './modules/auth';
import { ProfilePage, ProfileEditPage, StaffProfilePage, StaffEditProfilePage, DocumentsPage, HelpPage } from './modules/citizen';
import { AdminDashboard, CitizenDetailPage, PendingDocumentsPage, StaffManagementPage } from './modules/admin';
import {
  MyServiceRequestsPage,
  SubmitServiceRequestPage,
  EditServiceRequestPage,
  ServiceRequestDetailPage,
  AllServiceRequestsPage,
  AssignOfficerPage,
  OfficerDashboardPage,
  UpdateServiceRequestPage,
} from './modules/service-requests';
import { MyResolutionsPage, CreateResolutionPage, ResolutionDetailPage } from './modules/resolutions';
import {
  ComplianceDashboardPage,
  CreateComplianceRecordPage,
  ComplianceRecordDetailPage,
  CreateAuditPage,
  AuditDetailPage,
} from './modules/compliance';
import { MyFeedbackPage, SubmitFeedbackPage, LeaderboardPage } from './modules/feedback';
import { ReportsDashboardPage } from './modules/reports';
import { NotificationsPage } from './modules/notifications';
import { LandingPage, AboutPage } from './modules/common';

function PrivateRoute({ children, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" />;
  return children;
}

/**
 * /profile is shared across roles. Citizens see their full profile (with
 * documents + activation steps), every staff role sees the staff profile.
 */
function ProfileSwitcher() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  return user.role === 'CITIZEN' ? <ProfilePage /> : <StaffProfilePage />;
}

function AppRoutes() {
  const { user } = useAuth();

  return (
    <>
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/" /> : <LoginPage />} />
          <Route path="/register" element={user ? <Navigate to="/" /> : <RegisterPage />} />
          <Route path="/reset-password" element={user ? <Navigate to="/" /> : <ResetPasswordPage />} />
          <Route path="/about" element={<AboutPage />} />

          {/* Profile (any signed-in role) — picks citizen vs staff variant */}
          <Route path="/profile" element={<ProfileSwitcher />} />
          <Route path="/profile/edit" element={
            <PrivateRoute roles={['CITIZEN']}><ProfileEditPage /></PrivateRoute>
          } />
          <Route path="/profile/edit-staff" element={
            <PrivateRoute roles={['SERVICE_OFFICER','DEPARTMENT_HEAD','COMPLIANCE_OFFICER']}>
              <StaffEditProfilePage />
            </PrivateRoute>
          } />
          <Route path="/documents" element={
            <PrivateRoute roles={['CITIZEN']}><DocumentsPage /></PrivateRoute>
          } />
          <Route path="/help" element={
            <PrivateRoute roles={['CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER']}>
              <HelpPage />
            </PrivateRoute>
          } />
          <Route path="/service-requests" element={
            <PrivateRoute roles={['CITIZEN']}><MyServiceRequestsPage /></PrivateRoute>
          } />
          <Route path="/service-requests/new" element={
            <PrivateRoute roles={['CITIZEN']}><SubmitServiceRequestPage /></PrivateRoute>
          } />
          <Route path="/service-requests/:requestId/edit" element={
            <PrivateRoute roles={['CITIZEN']}><EditServiceRequestPage /></PrivateRoute>
          } />

          {/* Admin / Dept Head routes */}
          <Route path="/admin" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR']}><AdminDashboard /></PrivateRoute>
          } />
          <Route path="/admin/citizens/:citizenId" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR']}><CitizenDetailPage /></PrivateRoute>
          } />
          <Route path="/admin/pending-documents" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR', 'SERVICE_OFFICER', 'DEPARTMENT_HEAD']}><PendingDocumentsPage /></PrivateRoute>
          } />
          <Route path="/admin/staff" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR']}><StaffManagementPage /></PrivateRoute>
          } />
          <Route path="/admin/service-requests" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR', 'DEPARTMENT_HEAD']}><AllServiceRequestsPage /></PrivateRoute>
          } />
          <Route path="/service-requests/:requestId/assign" element={
            <PrivateRoute roles={['CITY_ADMINISTRATOR', 'DEPARTMENT_HEAD']}><AssignOfficerPage /></PrivateRoute>
          } />

          {/* Officer routes */}
          <Route path="/officer/dashboard" element={
            <PrivateRoute roles={['SERVICE_OFFICER']}><OfficerDashboardPage /></PrivateRoute>
          } />
          <Route path="/service-requests/:requestId/update" element={
            <PrivateRoute roles={['SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR']}><UpdateServiceRequestPage /></PrivateRoute>
          } />

          {/* Shared */}
          <Route path="/service-requests/:requestId" element={
            <PrivateRoute roles={['CITIZEN', 'SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR']}><ServiceRequestDetailPage /></PrivateRoute>
          } />

          {/* Resolution routes */}
          <Route path="/resolutions" element={
            <PrivateRoute roles={['SERVICE_OFFICER']}><MyResolutionsPage /></PrivateRoute>
          } />
          <Route path="/resolutions/create" element={
            <PrivateRoute roles={['SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR']}><CreateResolutionPage /></PrivateRoute>
          } />
          <Route path="/resolutions/:resolutionId" element={
            <PrivateRoute roles={['SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR']}><ResolutionDetailPage /></PrivateRoute>
          } />

          {/* Compliance routes */}
          <Route path="/compliance" element={
            <PrivateRoute roles={['COMPLIANCE_OFFICER', 'CITY_ADMINISTRATOR']}><ComplianceDashboardPage /></PrivateRoute>
          } />
          <Route path="/compliance/records/new" element={
            <PrivateRoute roles={['COMPLIANCE_OFFICER', 'CITY_ADMINISTRATOR']}><CreateComplianceRecordPage /></PrivateRoute>
          } />
          <Route path="/compliance/records/:complianceId" element={
            <PrivateRoute roles={['COMPLIANCE_OFFICER', 'CITY_ADMINISTRATOR']}><ComplianceRecordDetailPage /></PrivateRoute>
          } />
          <Route path="/compliance/audits/new" element={
            <PrivateRoute roles={['COMPLIANCE_OFFICER', 'CITY_ADMINISTRATOR']}><CreateAuditPage /></PrivateRoute>
          } />
          <Route path="/compliance/audits/:auditId" element={
            <PrivateRoute roles={['COMPLIANCE_OFFICER', 'CITY_ADMINISTRATOR']}><AuditDetailPage /></PrivateRoute>
          } />

          {/* Feedback routes */}
          <Route path="/feedback" element={
            <PrivateRoute roles={['CITIZEN']}><MyFeedbackPage /></PrivateRoute>
          } />
          <Route path="/feedback/submit/:requestId" element={
            <PrivateRoute roles={['CITIZEN']}><SubmitFeedbackPage /></PrivateRoute>
          } />
          <Route path="/leaderboard" element={
            <PrivateRoute roles={['SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR', 'COMPLIANCE_OFFICER']}><LeaderboardPage /></PrivateRoute>
          } />

          {/* Reporting routes */}
          <Route path="/reports" element={
            <PrivateRoute roles={['DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR', 'COMPLIANCE_OFFICER']}><ReportsDashboardPage /></PrivateRoute>
          } />

          {/* Notification routes */}
          <Route path="/notifications" element={
            <PrivateRoute roles={['CITIZEN', 'SERVICE_OFFICER', 'DEPARTMENT_HEAD', 'CITY_ADMINISTRATOR', 'COMPLIANCE_OFFICER']}><NotificationsPage /></PrivateRoute>
          } />

          {/* Default */}
          <Route path="/" element={
            user
              ? user.role === 'CITY_ADMINISTRATOR' ? <Navigate to="/admin" />
              : user.role === 'SERVICE_OFFICER' ? <Navigate to="/officer/dashboard" />
              : user.role === 'DEPARTMENT_HEAD' ? <Navigate to="/admin/service-requests" />
              : user.role === 'COMPLIANCE_OFFICER' ? <Navigate to="/compliance" />
              : <Navigate to="/profile" />
              : <LandingPage />
          } />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
      <Footer />
      <ToastContainer position="top-right" autoClose={3000} />
    </>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

export default App;
