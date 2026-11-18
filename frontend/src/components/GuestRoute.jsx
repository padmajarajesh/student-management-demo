import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';
import { FullPageLoader } from './Spinner.jsx';

/** Login/registration pages: logged-in users are sent to where they came from (or the dashboard). */
export default function GuestRoute() {
  const { user, initializing } = useAuth();
  const location = useLocation();

  if (initializing) return <FullPageLoader />;
  if (user) {
    const from = location.state?.from?.pathname || '/dashboard';
    return <Navigate to={from} replace />;
  }
  return <Outlet />;
}
