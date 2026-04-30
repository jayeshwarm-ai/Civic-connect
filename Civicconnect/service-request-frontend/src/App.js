import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
// Citizen pages
import MyRequestsPage from './pages/citizen/MyRequestsPage';
import SubmitRequestPage from './pages/citizen/SubmitRequestPage';
import RequestDetailPage from './pages/RequestDetailPage';
// Admin / Dept Head pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AllRequestsPage from './pages/admin/AllRequestsPage';
import AssignOfficerPage from './pages/admin/AssignOfficerPage';
// Officer pages
import OfficerDashboardPage from './pages/officer/OfficerDashboardPage';
import UpdateStatusPage from './pages/officer/UpdateStatusPage';

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

          {/* Citizen */}
          <Route path="/my-requests" element={<PrivateRoute roles={['CITIZEN']}><MyRequestsPage /></PrivateRoute>} />
          <Route path="/submit-request" element={<PrivateRoute roles={['CITIZEN']}><SubmitRequestPage /></PrivateRoute>} />

          {/* Admin / Dept Head */}
          <Route path="/admin" element={<PrivateRoute roles={['CITY_ADMINISTRATOR','DEPARTMENT_HEAD']}><AdminDashboardPage /></PrivateRoute>} />
          <Route path="/admin/requests" element={<PrivateRoute roles={['CITY_ADMINISTRATOR','DEPARTMENT_HEAD']}><AllRequestsPage /></PrivateRoute>} />
          <Route path="/admin/assign/:requestId" element={<PrivateRoute roles={['CITY_ADMINISTRATOR','DEPARTMENT_HEAD']}><AssignOfficerPage /></PrivateRoute>} />

          {/* Officer */}
          <Route path="/officer" element={<PrivateRoute roles={['SERVICE_OFFICER']}><OfficerDashboardPage /></PrivateRoute>} />
          <Route path="/officer/update/:requestId" element={<PrivateRoute roles={['SERVICE_OFFICER']}><UpdateStatusPage /></PrivateRoute>} />

          {/* Shared */}
          <Route path="/requests/:requestId" element={<PrivateRoute roles={['CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR']}><RequestDetailPage /></PrivateRoute>} />

          {/* Home / Default */}
          <Route path="/" element={user ? <HomePage /> : <Navigate to="/login" />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
      <ToastContainer position="top-right" autoClose={3000} />
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

