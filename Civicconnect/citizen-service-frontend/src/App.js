import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProfilePage from './pages/ProfilePage';
import ProfileEditPage from './pages/ProfileEditPage';
import DocumentsPage from './pages/DocumentsPage';
import AdminDashboard from './pages/AdminDashboard';
import CitizenDetailPage from './pages/CitizenDetailPage';
import PendingDocumentsPage from './pages/PendingDocumentsPage';
import HelpPage from './pages/HelpPage';
import StaffManagementPage from './pages/StaffManagementPage';
// Service Request pages
import MyServiceRequestsPage from './pages/MyServiceRequestsPage';
import SubmitServiceRequestPage from './pages/SubmitServiceRequestPage';
import ServiceRequestDetailPage from './pages/ServiceRequestDetailPage';
import AllServiceRequestsPage from './pages/AllServiceRequestsPage';
import AssignOfficerPage from './pages/AssignOfficerPage';
import OfficerDashboardPage from './pages/OfficerDashboardPage';
import UpdateServiceRequestPage from './pages/UpdateServiceRequestPage';
import LandingPage from './pages/LandingPage';
import EditServiceRequestPage from './pages/EditServiceRequestPage';
// Resolution pages
import MyResolutionsPage from './pages/MyResolutionsPage';
import CreateResolutionPage from './pages/CreateResolutionPage';
import ResolutionDetailPage from './pages/ResolutionDetailPage';
// Compliance pages
import ComplianceDashboardPage from './pages/ComplianceDashboardPage';
import CreateComplianceRecordPage from './pages/CreateComplianceRecordPage';
import ComplianceRecordDetailPage from './pages/ComplianceRecordDetailPage';
import CreateAuditPage from './pages/CreateAuditPage';
import AuditDetailPage from './pages/AuditDetailPage';
// Feedback pages
import MyFeedbackPage from './pages/MyFeedbackPage';
import SubmitFeedbackPage from './pages/SubmitFeedbackPage';
import LeaderboardPage from './pages/LeaderboardPage';
// Reporting pages
import ReportsDashboardPage from './pages/ReportsDashboardPage';
// Notification pages
import NotificationsPage from './pages/NotificationsPage';

function PrivateRoute({ children, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" />;
  return children;
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

          {/* Citizen routes */}
          <Route path="/profile" element={
            <PrivateRoute roles={['CITIZEN']}><ProfilePage /></PrivateRoute>
          } />
          <Route path="/profile/edit" element={
            <PrivateRoute roles={['CITIZEN']}><ProfileEditPage /></PrivateRoute>
          } />
          <Route path="/documents" element={
            <PrivateRoute roles={['CITIZEN']}><DocumentsPage /></PrivateRoute>
          } />
          <Route path="/help" element={
            <PrivateRoute roles={['CITIZEN']}><HelpPage /></PrivateRoute>
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

