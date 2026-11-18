import { useState } from 'react';
import { Link } from 'react-router-dom';
import FormField from './FormField.jsx';
import Alert from './Alert.jsx';
import { Spinner } from './Spinner.jsx';
import { DEPARTMENT_SUGGESTIONS, GENDERS, todayIso, validateStudent } from '../utils/validation.js';
import { getErrorMessage, getFieldErrors } from '../services/api.js';

export const EMPTY_STUDENT = {
  fullName: '',
  email: '',
  phoneNumber: '',
  department: '',
  gender: '',
  dateOfBirth: '',
  address: '',
};

/**
 * Shared form for "Add Student" and "Edit Student".
 * `onSubmit(values)` must return a promise; backend validation errors are
 * shown next to the matching fields.
 */
export default function StudentForm({ initialValues = EMPTY_STUDENT, submitLabel, cancelTo, onSubmit }) {
  // Keep only the editable fields (drops id, createdAt, ...) and replace null with ''
  const [values, setValues] = useState(() =>
    Object.fromEntries(Object.keys(EMPTY_STUDENT).map((key) => [key, initialValues[key] ?? '']))
  );
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    if (errors[name]) setErrors((errs) => ({ ...errs, [name]: undefined }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    const clientErrors = validateStudent(values);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length > 0) return;

    setSubmitting(true);
    try {
      await onSubmit({
        ...values,
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        phoneNumber: values.phoneNumber.trim(),
        department: values.department.trim(),
        address: values.address.trim() || null,
      });
    } catch (error) {
      setErrors(getFieldErrors(error));
      setFormError(getErrorMessage(error, 'Could not save the student.'));
      setSubmitting(false);
    }
  };

  const inputProps = (name) => ({
    id: name,
    name,
    value: values[name],
    onChange: handleChange,
    disabled: submitting,
    'aria-invalid': errors[name] ? 'true' : 'false',
    'aria-describedby': errors[name] ? `${name}-error` : undefined,
    className: 'form-input',
  });

  return (
    <form className="card form-card" onSubmit={handleSubmit} noValidate>
      {formError ? <Alert type="error">{formError}</Alert> : null}

      <div className="form-grid">
        <FormField id="fullName" label="Full name" required error={errors.fullName}>
          <input type="text" autoComplete="off" placeholder="e.g. Priya Sharma" {...inputProps('fullName')} />
        </FormField>

        <FormField id="email" label="Email" required error={errors.email}>
          <input type="email" autoComplete="off" placeholder="student@example.com" {...inputProps('email')} />
        </FormField>

        <FormField id="phoneNumber" label="Phone number" required error={errors.phoneNumber}>
          <input type="tel" autoComplete="off" placeholder="+91 98765 43210" {...inputProps('phoneNumber')} />
        </FormField>

        <FormField id="department" label="Department" required error={errors.department}
                   hint="Pick a suggestion or type your own">
          <input type="text" list="department-options" placeholder="e.g. Computer Science" {...inputProps('department')} />
          <datalist id="department-options">
            {DEPARTMENT_SUGGESTIONS.map((d) => (
              <option key={d} value={d} />
            ))}
          </datalist>
        </FormField>

        <FormField id="gender" label="Gender" required error={errors.gender}>
          <select {...inputProps('gender')}>
            <option value="">Select gender</option>
            {GENDERS.map((g) => (
              <option key={g} value={g}>{g}</option>
            ))}
          </select>
        </FormField>

        <FormField id="dateOfBirth" label="Date of birth" required error={errors.dateOfBirth}>
          <input type="date" max={todayIso()} {...inputProps('dateOfBirth')} />
        </FormField>

        <FormField id="address" label="Address" error={errors.address} className="form-field-full"
                   hint={`${values.address.length}/255 characters`}>
          <textarea rows={3} placeholder="Street, city, state" {...inputProps('address')} />
        </FormField>
      </div>

      <div className="form-actions">
        <Link to={cancelTo} className={`btn btn-secondary ${submitting ? 'disabled' : ''}`}>
          Cancel
        </Link>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? <Spinner size="sm" /> : null}
          {submitting ? 'Saving...' : submitLabel}
        </button>
      </div>
    </form>
  );
}
