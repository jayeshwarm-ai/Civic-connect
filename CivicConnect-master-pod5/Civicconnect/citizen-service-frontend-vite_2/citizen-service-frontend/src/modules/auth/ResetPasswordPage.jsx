import React, { useEffect, useState } from 'react';
import AuthPage from './AuthPage';

// Thin wrapper kept so the /reset-password route still works.
// Reset password is now an inline panel inside the LOGIN tab (single combined card).
// We default to the login tab so the user can sign in OR reset from the same card.
export default function ResetPasswordPage() {
  return <AuthPage initialTab="login" />;
}
