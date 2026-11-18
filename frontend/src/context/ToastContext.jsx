import { createContext, useCallback, useMemo, useRef, useState } from 'react';
import Icon from '../components/Icon.jsx';

export const ToastContext = createContext(null);

const TOAST_DURATION_MS = 4000;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const nextId = useRef(1);

  const dismissToast = useCallback((id) => {
    setToasts((current) => current.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (message, type = 'success') => {
      const id = nextId.current++;
      setToasts((current) => [...current, { id, message, type }]);
      setTimeout(() => dismissToast(id), TOAST_DURATION_MS);
    },
    [dismissToast]
  );

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-container" role="status" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast-${toast.type}`}>
            <Icon name={toast.type === 'error' ? 'alert' : 'check'} size={18} />
            <span className="toast-message">{toast.message}</span>
            <button className="toast-close" onClick={() => dismissToast(toast.id)} aria-label="Dismiss">
              <Icon name="x" size={16} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
