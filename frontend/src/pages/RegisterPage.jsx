import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from './AuthLayout.jsx';
import FormField from '../components/FormField.jsx';
import Alert from '../components/Alert.jsx';
import { Spinner } from '../components/Spinner.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { validateRegistration } from '../utils/validation.js';
import { getErrorMessage, getFieldErrors } from '../services/api.js';

const INITIAL = { fullName: '', email: '', phoneNumber: '', password: '', confirmPassword: '' };

export default function RegisterPage() {
  useDocumentTitle('Create account');
  const { register } = useAuth();
  const { showToast } = useToast();
  const [values, setValues] = useState(INITIAL);
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
    const clientErrors = validateRegistration(values);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length > 0) return;

    setSubmitting(true);
    try {
      await register({
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        phoneNumber: values.phoneNumber.trim(),
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      showToast('Account created successfully. Welcome!');
    } catch (error) {
      setErrors(getFieldErrors(error));
      setFormError(getErrorMessage(error, 'Registration failed.'));
      setSubmitting(false);
    }
  };

  const field = (name, type, autoComplete, placeholder) => (
    <input
      id={name}
      name={name}
      type={type}
      autoComplete={autoComplete}
      placeholder={placeholder}
      className="form-input"
      value={values[name]}
      onChange={handleChange}
      disabled={submitting}
      aria-invalid={errors[name] ? 'true' : 'false'}
    />
  );

  return (
    <AuthLayout title="Create your account" subtitle="Start managing your students in minutes">
      <form onSubmit={handleSubmit} noValidate className="auth-form">
        {formError ? <Alert type="error">{formError}</Alert> : null}

        <FormField id="fullName" label="Full name" required error={errors.fullName}>
          {field('fullName', 'text', 'name', 'Your full name')}
        </FormField>
        <FormField id="email" label="Email" required error={errors.email}>
          {field('email', 'email', 'email', 'you@example.com')}
        </FormField>
        <FormField id="phoneNumber" label="Phone number" required error={errors.phoneNumber}>
          {field('phoneNumber', 'tel', 'tel', '+91 98765 43210')}
        </FormField>
        <div className="form-row">
          <FormField id="password" label="Password" required error={errors.password}
                     hint="At least 8 characters with a letter and a number">
            {field('password', 'password', 'new-password', 'Create a password')}
          </FormField>
          <FormField id="confirmPassword" label="Confirm password" required error={errors.confirmPassword}>
            {field('confirmPassword', 'password', 'new-password', 'Repeat password')}
          </FormField>
        </div>

        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? <Spinner size="sm" /> : null}
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </AuthLayout>
  );
}
