import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

// Human-readable role labels
export const ROLE_LABELS = {
  'super_admin': 'Super Admin',
  'station-admin': 'Station Admin',
  'verifier_admin': 'Verifier Admin',
};

// Permission map — super_admin always gets true
const ROLE_PERMISSIONS = {
  'super_admin': ['*'], // wildcard = everything
  'station-admin': ['manage_trains', 'manage_stations'],
  'verifier_admin': ['verify_restaurants', 'verify_riders'],
};

export const AuthProvider = ({ children }) => {
  const [role, setRole] = useState(localStorage.getItem('role') || null);
  const [adminName, setAdminName] = useState(localStorage.getItem('adminName') || 'Admin');
  const [adminEmail, setAdminEmail] = useState(localStorage.getItem('adminEmail') || '');

  const login = (userRole, name, email) => {
    localStorage.setItem('role', userRole);
    localStorage.setItem('adminName', name || 'Admin');
    localStorage.setItem('adminEmail', email || '');
    setRole(userRole);
    setAdminName(name || 'Admin');
    setAdminEmail(email || '');
  };

  const logout = () => {
    localStorage.removeItem('role');
    localStorage.removeItem('adminName');
    localStorage.removeItem('adminEmail');
    setRole(null);
    setAdminName('Admin');
    setAdminEmail('');
  };

  /**
   * Check if the current admin has a specific permission.
   * super_admin always returns true.
   * @param {string} action - e.g. 'manage_trains', 'verify_restaurants'
   */
  const can = (action) => {
    if (!role) return false;
    if (role === 'super_admin') return true;
    return ROLE_PERMISSIONS[role]?.includes(action) || false;
  };

  return (
    <AuthContext.Provider value={{ role, adminName, adminEmail, login, logout, can }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
