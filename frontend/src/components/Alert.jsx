import Icon from './Icon.jsx';

/** Inline message box. type: "error" | "success" | "info". */
export default function Alert({ type = 'error', children, action }) {
  return (
    <div className={`alert alert-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <Icon name={type === 'success' ? 'check' : 'alert'} size={18} />
      <div className="alert-body">{children}</div>
      {action}
    </div>
  );
}
