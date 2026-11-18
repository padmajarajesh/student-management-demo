import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';
import { FullPageLoader } from './Spinner.jsx';

/**
 * Only renders child routes for logged-in users. This is a UX convenience;
 * the real protection is enforced by Spring Security on the backend.
 */
export default function ProtectedRoute() {
  const { user, initializing } = useAuth();
  const location = useLocation();

  if (initializing) return <FullPageLoader />;
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  return <Outlet />;
}
