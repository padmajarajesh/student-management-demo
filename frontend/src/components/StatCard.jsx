import Icon from './Icon.jsx';

export default function StatCard({ icon, label, value, hint, tone = 'navy' }) {
  return (
    <div className="card stat-card">
      <div className={`stat-icon stat-icon-${tone}`}>
        <Icon name={icon} size={22} />
      </div>
      <div className="stat-content">
        <p className="stat-label">{label}</p>
        <p className="stat-value">{value}</p>
        {hint ? <p className="stat-hint">{hint}</p> : null}
      </div>
    </div>
  );
}
