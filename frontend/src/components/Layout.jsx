import { useCallback, useEffect, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar.jsx';
import Icon from './Icon.jsx';
import ConfirmDialog from './ConfirmDialog.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useToast } from '../hooks/useToast.js';
import { initials } from '../utils/format.js';

function titleFor(pathname) {
  if (pathname === '/dashboard') return 'Dashboard';
  if (pathname === '/students') return 'Students';
  if (pathname === '/students/new') return 'Add Student';
  if (pathname.endsWith('/edit')) return 'Edit Student';
  if (pathname.startsWith('/students/')) return 'Student Details';
  if (pathname === '/profile') return 'Profile';
  return '';
}

/** App shell for logged-in pages: sidebar + top bar + page content. */
export default function Layout() {
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [confirmLogout, setConfirmLogout] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  // Close the mobile menu after navigating
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const closeLogoutDialog = useCallback(() => setConfirmLogout(false), []);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
      showToast('You have been logged out.');
    } catch {
      // The app state is cleared regardless; the HttpOnly cookies can only be
      // cleared by the server, so tell the user the server call failed.
      showToast('Signed out of the app, but the server could not be reached.', 'error');
    } finally {
      setLoggingOut(false);
      setConfirmLogout(false);
      navigate('/login', { replace: true });
    }
  };

  return (
    <div className="app-shell">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} onLogout={() => setConfirmLogout(true)} />
      {sidebarOpen ? <div className="sidebar-backdrop" onClick={() => setSidebarOpen(false)} /> : null}

      <div className="main-area">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setSidebarOpen(true)} aria-label="Open menu">
            <Icon name="menu" />
          </button>
          <h2 className="topbar-title">{titleFor(location.pathname)}</h2>
          <Link to="/profile" className="topbar-user" title="View profile">
            <span className="avatar avatar-sm">{initials(user?.fullName)}</span>
            <span className="topbar-user-name">{user?.fullName}</span>
          </Link>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>

      <ConfirmDialog
        open={confirmLogout}
        variant="primary"
        title="Log out?"
        message="You will need to sign in again to manage your students."
        confirmLabel="Log out"
        loading={loggingOut}
        onConfirm={handleLogout}
        onCancel={closeLogoutDialog}
      />
    </div>
  );
}
