import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

function decodeToken(token) {
  try { return JSON.parse(atob(token.split('.')[1])); } catch { return null; }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('sr_user');
    if (token && userData) {
      const decoded = decodeToken(token);
      if (decoded && decoded.exp * 1000 > Date.now()) {
        setUser(JSON.parse(userData));
      } else {
        localStorage.removeItem('token');
        localStorage.removeItem('sr_user');
      }
    }
    setLoading(false);
  }, []);

  const loginUser = (res) => {
    const { token, userId, name, email, role } = res;
    localStorage.setItem('token', token);
    const u = { userId, name, email, role };
    localStorage.setItem('sr_user', JSON.stringify(u));
    setUser(u);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('sr_user');
    setUser(null);
  };

  if (loading) return <div style={{textAlign:'center',padding:80}}>Loading...</div>;

  return (
    <AuthContext.Provider value={{ user, loginUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

