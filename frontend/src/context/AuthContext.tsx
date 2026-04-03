import { createContext, useContext, useState } from 'react';
import type { AuthResponse } from '../types';

const TOKEN_KEY = 'bgr_token';
const USERNAME_KEY = 'bgr_username';
const ROLE_KEY = 'bgr_role';

type AuthState = {
  token: string | null;
  username: string | null;
  role: string | null;
};

type AuthContextValue = AuthState & {
  login: (response: AuthResponse) => void;
  logout: () => void;
  isLoggedIn: boolean;
};

function readStorage(): AuthState {
  return {
    token: localStorage.getItem(TOKEN_KEY),
    username: localStorage.getItem(USERNAME_KEY),
    role: localStorage.getItem(ROLE_KEY),
  };
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = useState<AuthState>(readStorage);

  function login(response: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USERNAME_KEY, response.username);
    localStorage.setItem(ROLE_KEY, response.role);
    setAuth({ token: response.token, username: response.username, role: response.role });
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(ROLE_KEY);
    setAuth({ token: null, username: null, role: null });
  }

  return (
    <AuthContext.Provider value={{ ...auth, login, logout, isLoggedIn: auth.token !== null }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
