import { useEffect, useRef } from 'react';
import Icon from './Icon.jsx';
import { Spinner } from './Spinner.jsx';

/** Modal confirmation used before deleting a student and before logging out. */
export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'danger',
  loading = false,
  onConfirm,
  onCancel,
}) {
  const cancelRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    cancelRef.current?.focus();
    const onKey = (e) => {
      if (e.key === 'Escape' && !loading) onCancel();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, loading, onCancel]);

  if (!open) return null;

  return (
    <div className="modal-backdrop" onMouseDown={() => !loading && onCancel()}>
      <div
        className="modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-title"
        aria-describedby="confirm-message"
        onMouseDown={(e) => e.stopPropagation()}
      >
        <div className={`modal-icon modal-icon-${variant}`}>
          <Icon name={variant === 'danger' ? 'trash' : 'logout'} size={22} />
        </div>
        <h2 id="confirm-title" className="modal-title">{title}</h2>
        <p id="confirm-message" className="modal-message">{message}</p>
        <div className="modal-actions">
          <button ref={cancelRef} className="btn btn-secondary" onClick={onCancel} disabled={loading}>
            {cancelLabel}
          </button>
          <button
            className={`btn ${variant === 'danger' ? 'btn-danger' : 'btn-primary'}`}
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? <Spinner size="sm" /> : null}
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
