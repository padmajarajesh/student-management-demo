import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import StatCard from '../components/StatCard.jsx';
import Alert from '../components/Alert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import Icon from '../components/Icon.jsx';
import { PageLoader } from '../components/Spinner.jsx';
import { dashboardService } from '../services/dashboardService.js';
import { getErrorMessage } from '../services/api.js';
import { useAuth } from '../hooks/useAuth.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { initials, timeAgo } from '../utils/format.js';

export default function DashboardPage() {
  useDocumentTitle('Dashboard');
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadStats = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setStats(await dashboardService.getStats());
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load dashboard statistics.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  const addButton = (
    <Link to="/students/new" className="btn btn-primary">
      <Icon name="plus" size={18} /> Add Student
    </Link>
  );

  const header = (
    <PageHeader
      title={`Hello, ${user?.fullName?.split(' ')[0] || 'there'}`}
      subtitle="Here is an overview of your student records."
      actions={addButton}
    />
  );

  if (loading && !stats) {
    return (
      <>
        {header}
        <PageLoader label="Loading dashboard..." />
      </>
    );
  }

  if (error && !stats) {
    return (
      <>
        {header}
        <Alert
          type="error"
          action={
            <button className="btn btn-secondary btn-sm" onClick={loadStats}>
              <Icon name="refresh" size={16} /> Retry
            </button>
          }
        >
          {error}
        </Alert>
      </>
    );
  }

  const maxDeptCount = Math.max(1, ...stats.departmentCounts.map((d) => d.count));

  return (
    <>
      {header}

      <section className="stats-grid" aria-label="Statistics">
        <StatCard icon="users" label="Total students" value={stats.totalStudents} tone="navy" />
        <StatCard icon="trendingUp" label="Added in last 7 days" value={stats.studentsLast7Days} tone="green" />
        <StatCard icon="calendar" label="Added in last 30 days" value={stats.studentsLast30Days} tone="amber" />
        <StatCard icon="layers" label="Departments" value={stats.totalDepartments} tone="purple" />
      </section>

      {stats.totalStudents === 0 ? (
        <div className="card">
          <EmptyState
            icon="userPlus"
            title="No students yet"
            message="Add your first student to see statistics here."
            action={addButton}
          />
        </div>
      ) : (
        <div className="dashboard-grid">
          <section className="card">
            <div className="card-header">
              <h3 className="card-title">Students by department</h3>
            </div>
            <ul className="dept-list">
              {stats.departmentCounts.map((d) => (
                <li key={d.department} className="dept-item">
                  <div className="dept-row">
                    <span className="dept-name">{d.department}</span>
                    <span className="dept-count">{d.count}</span>
                  </div>
                  <div className="dept-bar" aria-hidden="true">
                    <span style={{ width: `${(d.count / maxDeptCount) * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
          </section>

          <section className="card">
            <div className="card-header">
              <h3 className="card-title">Recent registrations</h3>
              <Link to="/students" className="link-sm">View all</Link>
            </div>
            <ul className="recent-list">
              {stats.recentStudents.map((s) => (
                <li key={s.id}>
                  <Link to={`/students/${s.id}`} className="recent-item">
                    <span className="avatar">{initials(s.fullName)}</span>
                    <span className="recent-info">
                      <strong>{s.fullName}</strong>
                      <small>{s.department}</small>
                    </span>
                    <span className="recent-time">{timeAgo(s.createdAt)}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        </div>
      )}
    </>
  );
}
