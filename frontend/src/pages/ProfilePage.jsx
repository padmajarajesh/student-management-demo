import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import Icon from '../components/Icon.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { dashboardService } from '../services/dashboardService.js';
import { API_BASE_URL } from '../services/api.js';
import { formatDate, initials } from '../utils/format.js';

export default function ProfilePage() {
  useDocumentTitle('Profile');
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [studentCount, setStudentCount] = useState(null);
  const [confirmLogout, setConfirmLogout] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    let active = true;
    dashboardService
      .getStats()
      .then((s) => active && setStudentCount(s.totalStudents))
      .catch(() => active && setStudentCount(null));
    return () => {
      active = false;
    };
  }, []);

  const closeDialog = useCallback(() => setConfirmLogout(false), []);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
      showToast('You have been logged out.');
    } catch {
      showToast('Signed out of the app, but the server could not be reached.', 'error');
    } finally {
      navigate('/login', { replace: true });
    }
  };

  return (
    <>
      <PageHeader title="My Profile" subtitle="Your account information." />

      <div className="card profile-card">
        <div className="profile-header">
          <span className="avatar avatar-xl">{initials(user.fullName)}</span>
          <div className="profile-heading">
            <h2 className="profile-name">{user.fullName}</h2>
            <p className="page-subtitle">Member since {formatDate(user.createdAt)}</p>
          </div>
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-icon"><Icon name="mail" size={18} /></span>
            <div>
              <p className="detail-label">Email</p>
              <p className="detail-value">{user.email}</p>
            </div>
          </div>
          <div className="detail-item">
            <span className="detail-icon"><Icon name="phone" size={18} /></span>
            <div>
              <p className="detail-label">Phone number</p>
              <p className="detail-value">{user.phoneNumber}</p>
            </div>
          </div>
          <div className="detail-item">
            <span className="detail-icon"><Icon name="users" size={18} /></span>
            <div>
              <p className="detail-label">Students managed</p>
              <p className="detail-value">{studentCount ?? '-'}</p>
            </div>
          </div>
          <div className="detail-item">
            <span className="detail-icon"><Icon name="layers" size={18} /></span>
            <div>
              <p className="detail-label">Connected API</p>
              <p className="detail-value detail-mono">{API_BASE_URL || '(not configured)'}</p>
            </div>
          </div>
        </div>

        <div className="profile-footer">
          <button className="btn btn-danger-outline" onClick={() => setConfirmLogout(true)}>
            <Icon name="logout" size={18} /> Log out
          </button>
        </div>
      </div>

      <ConfirmDialog
        open={confirmLogout}
        variant="primary"
        title="Log out?"
        message="You will need to sign in again to manage your students."
        confirmLabel="Log out"
        loading={loggingOut}
        onConfirm={handleLogout}
        onCancel={closeDialog}
      />
    </>
  );
}
