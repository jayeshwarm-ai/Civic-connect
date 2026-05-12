import axios from 'axios';

const API_BASE = 'http://localhost:9999';

const api = axios.create({ baseURL: API_BASE });

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto-logout on 401 (expired/invalid session)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      sessionStorage.clear();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// ── Auth (identity-service via gateway) ─────────────────────────────────
export const login = (email, password) =>
  api.post('/api/v1/auth/login', { email, password });

export const resetPassword = (email, phone, newPassword) =>
  api.post('/api/v1/auth/reset-password', { email, phone, newPassword });

// ── Citizen Registration (public) ───────────────────────────────────────
export const registerCitizen = (data) =>
  api.post('/api/v1/citizens/register', data);


// ── Citizen Profile ─────────────────────────────────────────────────────
export const getMyProfile = () =>
  api.get('/api/v1/citizens/my-profile');

export const updateMyProfile = (data) =>
  api.put('/api/v1/citizens/my-profile', data);

export const getCitizenById = (citizenId) =>
  api.get(`/api/v1/citizens/${citizenId}`);

export const getAllCitizens = () =>
  api.get('/api/v1/citizens');

// ── Documents ───────────────────────────────────────────────────────────
export const uploadMyDocument = (docType, file) => {
  const formData = new FormData();
  formData.append('docType', docType);
  formData.append('file', file);
  return api.post('/api/v1/citizens/my-documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const getMyDocuments = () =>
  api.get('/api/v1/citizens/my-documents');

export const getCitizenDocuments = (citizenId) =>
  api.get(`/api/v1/citizens/${citizenId}/documents`);

export const getPendingDocuments = () =>
  api.get('/api/v1/citizens/documents/pending');

export const verifyDocument = (documentId, data) =>
  api.patch(`/api/v1/citizens/documents/${documentId}/verify`, data);

export const getDocumentDownloadUrl = (documentId) =>
  `http://localhost:9999/api/v1/citizens/documents/${documentId}/download`;

export const downloadDocument = (documentId) =>
  api.get(`/api/v1/citizens/documents/${documentId}/download`, { responseType: 'blob' });

// ── Admin ───────────────────────────────────────────────────────────────
export const deactivateCitizen = (citizenId) =>
  api.patch(`/api/v1/citizens/${citizenId}/deactivate`);

// ── Staff Management (Admin) ─────────────────────────────────────────
export const registerStaff = (data) =>
  api.post('/api/v1/users/staff', data);

export const getStaffByRole = (role) =>
  api.get(`/api/v1/users/staff?role=${role}`);

export const getStaffById = (userId) =>
  api.get(`/api/v1/users/staff/${userId}`);

export const updateStaffStatus = (userId, newStatus) =>
  api.patch(`/api/v1/users/staff/${userId}/status?newStatus=${newStatus}`);

// ── Self-service profile (any staff role) ────────────────────────────
// GET works for Officer / Dept Head / Compliance / City Admin.
// PATCH works for Officer / Dept Head / Compliance only — City Admin is blocked
// by the backend (their profiles are administered centrally).
export const getMyStaffProfile = () =>
  api.get('/api/v1/users/me');

export const updateMyStaffProfile = (data) =>
  api.patch('/api/v1/users/me', data);

// ═══════════════════════════════════════════════════════════════════════
// SERVICE REQUEST APIs
// ═══════════════════════════════════════════════════════════════════════
export const submitServiceRequest = (data) =>
  api.post('/api/v1/service-requests', data);

export const getMyServiceRequests = () =>
  api.get('/api/v1/service-requests/my-requests');

export const closeServiceRequest = (requestId) =>
  api.patch(`/api/v1/service-requests/${requestId}/close`);

export const withdrawServiceRequest = (requestId) =>
  api.delete(`/api/v1/service-requests/${requestId}`);

export const getServiceRequestById = (requestId) =>
  api.get(`/api/v1/service-requests/${requestId}`);

export const getServiceRequestUpdates = (requestId) =>
  api.get(`/api/v1/service-requests/${requestId}/updates`);

export const getServiceRequestsByOfficer = (officerId) =>
  api.get(`/api/v1/service-requests/officer/${officerId}`);

export const updateServiceRequestStatus = (requestId, data) =>
  api.patch(`/api/v1/service-requests/${requestId}/status`, data);

export const getServiceRequestsByStatus = (status) =>
  api.get(`/api/v1/service-requests?status=${status}`);

export const getServiceRequestsByCitizen = (citizenId) =>
  api.get(`/api/v1/service-requests/citizen/${citizenId}`);

export const assignOfficerToRequest = (requestId, officerId) =>
  api.patch(`/api/v1/service-requests/${requestId}/assign`, { officerId });

// ═══════════════════════════════════════════════════════════════════════
// RESOLUTION APIs
// ═══════════════════════════════════════════════════════════════════════
export const createResolution = (data) =>
  api.post('/api/v1/resolutions', data);

export const getResolutionById = (resolutionId) =>
  api.get(`/api/v1/resolutions/${resolutionId}`);

export const getResolutionByRequestId = (requestId) =>
  api.get(`/api/v1/resolutions/by-request/${requestId}`);

export const getResolutionsByOfficer = (officerId) =>
  api.get(`/api/v1/resolutions/officer/${officerId}`);

export const addWorkflowStep = (resolutionId, data) =>
  api.post(`/api/v1/resolutions/${resolutionId}/steps`, data);

export const getWorkflowSteps = (resolutionId) =>
  api.get(`/api/v1/resolutions/${resolutionId}/steps`);

export const updateWorkflowStepStatus = (stepId, status) =>
  api.patch(`/api/v1/resolutions/steps/${stepId}/status`, { status });

// ═══════════════════════════════════════════════════════════════════════
// COMPLIANCE APIs
// ═══════════════════════════════════════════════════════════════════════
export const createComplianceRecord = (data) =>
  api.post('/api/v1/compliance/records', data);

export const getComplianceRecordById = (complianceId) =>
  api.get(`/api/v1/compliance/records/${complianceId}`);

export const getComplianceRecordsByEntity = (type, entityId) =>
  api.get(`/api/v1/compliance/records?type=${type}&entityId=${entityId}`);

export const getComplianceRecordsByResult = (result) =>
  api.get(`/api/v1/compliance/records/result?result=${result}`);

// Audit Records — base path: /api/v1/audit-records
export const getAllAudits = () =>
  api.get('/api/v1/audit-records');

export const createAuditRecord = (data) =>
  api.post('/api/v1/audit-records', data);

export const getAuditRecordById = (auditId) =>
  api.get(`/api/v1/audit-records/${auditId}`);

export const updateAuditRecord = (auditId, data) =>
  api.patch(`/api/v1/audit-records/${auditId}`, data);

export const getAuditsByOfficer = (officerId) =>
  api.get(`/api/v1/audit-records/officer/${officerId}`);

export const getAuditsByStatus = (status) =>
  api.get(`/api/v1/audit-records/status?status=${status}`);

// ═══════════════════════════════════════════════════════════════════════
// FEEDBACK APIs
// ═══════════════════════════════════════════════════════════════════════
export const submitFeedback = (data) =>
  api.post('/api/v1/feedback', data);

export const getFeedbackByRequestId = (requestId) =>
  api.get(`/api/v1/feedback/request/${requestId}`);

export const getFeedbacksByCitizen = (citizenId) =>
  api.get(`/api/v1/feedback/citizen/${citizenId}`);

export const getSatisfactionMetricByOfficer = (officerId) =>
  api.get(`/api/v1/feedback/metrics/officer/${officerId}`);

export const getAllSatisfactionMetrics = () =>
  api.get('/api/v1/feedback/metrics');

// ═══════════════════════════════════════════════════════════════════════
// REPORTING APIs
// ═══════════════════════════════════════════════════════════════════════
export const generateReport = (scope) =>
  api.post('/api/v1/reports', { scope });

export const getReportById = (reportId) =>
  api.get(`/api/v1/reports/${reportId}`);

export const getAllReports = () =>
  api.get('/api/v1/reports');

export const getReportsByScope = (scope) =>
  api.get(`/api/v1/reports/scope?scope=${scope}`);

// ═══════════════════════════════════════════════════════════════════════
// NOTIFICATION APIs
// ═══════════════════════════════════════════════════════════════════════
export const getNotifications = (userId) =>
  api.get(`/api/v1/notifications?userId=${userId}`);

export const getUnreadNotifications = (userId) =>
  api.get(`/api/v1/notifications/unread?userId=${userId}`);

export const getUnreadCount = (userId) =>
  api.get(`/api/v1/notifications/unread/count?userId=${userId}`);

export const getNotificationsByCategory = (userId, category) =>
  api.get(`/api/v1/notifications/category?userId=${userId}&category=${category}`);

export const markNotificationAsRead = (notificationId) =>
  api.patch(`/api/v1/notifications/${notificationId}/read`);

export const dismissNotification = (notificationId) =>
  api.patch(`/api/v1/notifications/${notificationId}/dismiss`);

export default api;

