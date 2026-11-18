/** Label + input slot + error message, with accessible wiring. */
export default function FormField({ id, label, error, required, hint, children, className = '' }) {
  return (
    <div className={`form-field ${error ? 'has-error' : ''} ${className}`}>
      <label htmlFor={id} className="form-label">
        {label}
        {required ? <span className="required-mark" aria-hidden="true"> *</span> : null}
      </label>
      {children}
      {error ? (
        <p className="field-error" id={`${id}-error`} role="alert">{error}</p>
      ) : hint ? (
        <p className="field-hint">{hint}</p>
      ) : null}
    </div>
  );
}
