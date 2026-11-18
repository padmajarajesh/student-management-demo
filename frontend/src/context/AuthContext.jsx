import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { authService } from '../services/authService.js';
import { setSessionExpiredHandler } from '../services/api.js';
import { useToast } from '../hooks/useToast.js';

export const AuthContext = createContext(null);

/**
 * Holds the logged-in user. The user object comes from GET /api/auth/me;
 * the tokens themselves live only in HttpOnly cookies managed by the browser.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);
  const userRef = useRef(null);
  const { showToast } = useToast();

  useEffect(() => {
    userRef.current = user;
  }, [user]);

  // Called by the Axios interceptor when the refresh token is no longer valid
  useEffect(() => {
    setSessionExpiredHandler(() => {
      if (userRef.current) {
        setUser(null);
        showToast('Your session has expired. Please log in again.', 'error');
      }
    });
    return () => setSessionExpiredHandler(null);
  }, [showToast]);

  // Restore the session on page load (cookies are sent automatically)
  useEffect(() => {
    let active = true;
    authService
      .getCurrentUser()
      .then((currentUser) => active && setUser(currentUser))
      .catch(() => active && setUser(null))
      .finally(() => active && setInitializing(false));
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (credentials) => {
    const loggedIn = await authService.login(credentials);
    setUser(loggedIn);
    return loggedIn;
  }, []);

  const register = useCallback(async (payload) => {
    const registered = await authService.register(payload);
    setUser(registered);
    return registered;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
    }
  }, []);

  const value = useMemo(
    () => ({ user, initializing, login, register, logout }),
    [user, initializing, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
