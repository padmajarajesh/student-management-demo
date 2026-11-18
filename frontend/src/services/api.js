import axios from 'axios';

/**
 * Centralized Axios instance used by every service.
 *
 * - baseURL comes from VITE_API_URL (the deployed Render URL in production).
 * - withCredentials: true makes the browser send/receive the HttpOnly auth
 *   cookies. The JWT is never visible to JavaScript and never stored in
 *   localStorage/sessionStorage.
 * - X-Requested-With is required by the backend's CSRF protection.
 */
const configuredUrl = import.meta.env.VITE_API_URL;
const fallbackUrl = import.meta.env.DEV ? 'http://localhost:8080' : '';

if (!configuredUrl && !import.meta.env.DEV) {
  // eslint-disable-next-line no-console
  console.error('VITE_API_URL is not set. Configure it in your Vercel project settings and redeploy.');
}

export const API_BASE_URL = (configuredUrl || fallbackUrl).replace(/\/+$/, '');

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true,
  // Render's free tier can take ~1 minute to wake up after being idle
  timeout: 70000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
  },
});

// ---------------------------------------------------------------------------
// Automatic token refresh
// When a request fails with 401 (access token expired), call /auth/refresh
// once and retry the original request. Concurrent 401s share one refresh call.
// ---------------------------------------------------------------------------
const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/logout'];
let refreshPromise = null;
let sessionExpiredHandler = null;

export function setSessionExpiredHandler(handler) {
  sessionExpiredHandler = handler;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const isAuthEndpoint = AUTH_ENDPOINTS.some((path) => original?.url?.startsWith(path));

    if (status === 401 && original && !original._retry && !isAuthEndpoint) {
      original._retry = true;
      try {
        if (!refreshPromise) {
          refreshPromise = api.post('/auth/refresh').finally(() => {
            refreshPromise = null;
          });
        }
        await refreshPromise;
        return api(original);
      } catch (refreshError) {
        if (sessionExpiredHandler) sessionExpiredHandler();
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

/** Turns any Axios error into a human-readable message. */
export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (error?.code === 'ECONNABORTED') {
    return 'The server took too long to respond. It may be starting up - please try again.';
  }
  if (!error?.response) {
    return 'Cannot reach the server. Check your connection or try again in a moment.';
  }
  return error.response.data?.message || fallback;
}

/** Field-level validation errors from the backend, e.g. { email: "..." }. */
export function getFieldErrors(error) {
  return error?.response?.data?.errors || {};
}

export default api;
