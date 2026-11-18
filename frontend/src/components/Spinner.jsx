export function Spinner({ size = 'md', label }) {
  return (
    <span className="spinner-wrap" role="status">
      <span className={`spinner spinner-${size}`} aria-hidden="true" />
      {label ? <span className="spinner-label">{label}</span> : <span className="sr-only">Loading</span>}
    </span>
  );
}

export function PageLoader({ label = 'Loading...' }) {
  return (
    <div className="page-loader">
      <Spinner size="lg" label={label} />
    </div>
  );
}

export function FullPageLoader() {
  return (
    <div className="full-page-loader">
      <Spinner size="lg" label="Loading your workspace..." />
    </div>
  );
}
