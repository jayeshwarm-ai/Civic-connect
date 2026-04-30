import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:9999' });

API.interceptors.request.use(cfg => {
  const t = localStorage.getItem('token');
  if (t) cfg.headers.Authorization = `Bearer ${t}`;
  return cfg;
});

// ── Auth ─────────────────────────────────────────────────────────────────
export const login = (email, password) =>
  API.post('/api/v1/auth/login', { email, password });

// ── Citizen endpoints ────────────────────────────────────────────────────
export const submitRequest = (data) =>
  API.post('/api/v1/service-requests', data);

export const getMyRequests = () =>
  API.get('/api/v1/service-requests/my-requests');

export const closeRequest = (requestId) =>
  API.patch(`/api/v1/service-requests/${requestId}/close`);

export const withdrawRequest = (requestId) =>
  API.delete(`/api/v1/service-requests/${requestId}`);

// ── Shared endpoints ─────────────────────────────────────────────────────
export const getRequestById = (requestId) =>
  API.get(`/api/v1/service-requests/${requestId}`);

export const getRequestUpdates = (requestId) =>
  API.get(`/api/v1/service-requests/${requestId}/updates`);

// ── Officer endpoints ────────────────────────────────────────────────────
export const getRequestsByOfficer = (officerId) =>
  API.get(`/api/v1/service-requests/officer/${officerId}`);

export const updateRequestStatus = (requestId, data) =>
  API.patch(`/api/v1/service-requests/${requestId}/status`, data);

// ── Admin / Dept Head endpoints ──────────────────────────────────────────
export const getRequestsByStatus = (status) =>
  API.get(`/api/v1/service-requests?status=${status}`);

export const getRequestsByCitizen = (citizenId) =>
  API.get(`/api/v1/service-requests/citizen/${citizenId}`);

export const assignOfficer = (requestId, officerId) =>
  API.patch(`/api/v1/service-requests/${requestId}/assign`, { officerId });

export default API;

