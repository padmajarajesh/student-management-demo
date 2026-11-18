import { NavLink } from 'react-router-dom';
import Icon from './Icon.jsx';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: 'dashboard', end: true },
  { to: '/students', label: 'Students', icon: 'users', end: true },
  { to: '/students/new', label: 'Add Student', icon: 'userPlus', end: true },
  { to: '/profile', label: 'Profile', icon: 'user', end: true },
];

export default function Sidebar({ open, onClose, onLogout }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`} aria-label="Main navigation">
      <div className="sidebar-brand">
        <span className="brand-logo">
          <Icon name="graduation" size={22} />
        </span>
        <span className="brand-text">
          <strong>StudentHub</strong>
          <small>Management System</small>
        </span>
        <button className="icon-btn sidebar-close" onClick={onClose} aria-label="Close menu">
          <Icon name="x" />
        </button>
      </div>

      <nav className="sidebar-nav">
        <p className="sidebar-section">Menu</p>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <Icon name={item.icon} size={19} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <button className="sidebar-link sidebar-logout" onClick={onLogout}>
          <Icon name="logout" size={19} />
          <span>Log out</span>
        </button>
      </div>
    </aside>
  );
}
