import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { user } = useAuth();
  if (user.role === 'CITIZEN') return <Navigate to="/my-requests" replace />;
  if (user.role === 'SERVICE_OFFICER') return <Navigate to="/officer" replace />;
  return <Navigate to="/admin" replace />;
}
