import React from 'react';
import AuthPage from './AuthPage';

// Thin wrapper kept so the /register route + existing barrel export still work.
export default function RegisterPage() {
  return <AuthPage initialTab="register" />;
}
