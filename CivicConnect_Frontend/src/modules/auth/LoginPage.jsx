import React from 'react';
import AuthPage from './AuthPage';

// Thin wrapper kept so the /login route + existing barrel export still work.
// All actual logic lives in AuthPage.
export default function LoginPage() {
  return <AuthPage initialTab="login" />;
}
