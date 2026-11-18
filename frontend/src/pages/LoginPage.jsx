import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from './AuthLayout.jsx';
import FormField from '../components/FormField.jsx';
import Alert from '../components/Alert.jsx';
import Icon from '../components/Icon.jsx';
import { Spinner } from '../components/Spinner.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { validateLogin } from '../utils/validation.js';
import { getErrorMessage, getFieldErrors } from '../services/api.js';

export default function LoginPage() {
  useDocumentTitle('Log in');
  const { login } = useAuth();
  const { showToast } = useToast();
  const [values, setValues] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [slowServer, setSlowServer] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  // Free-tier servers sleep when idle; tell the user if the first request is slow
  useEffect(() => {
    if (!submitting) {
      setSlowServer(false);
      return undefined;
    }
    const timer = setTimeout(() => setSlowServer(true), 5000);
    return () => clearTimeout(timer);
  }, [submitting]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    if (errors[name]) setErrors((errs) => ({ ...errs, [name]: undefined }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    const clientErrors = validateLogin(values);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length > 0) return;

    setSubmitting(true);
    try {
      const user = await login({ email: values.email.trim(), password: values.password });
      showToast(`Welcome back, ${user.fullName.split(' ')[0]}!`);
      // GuestRoute redirects to the dashboard (or the page the user came from)
    } catch (error) {
      setErrors(getFieldErrors(error));
      setFormError(getErrorMessage(error, 'Login failed.'));
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to manage your students">
      <form onSubmit={handleSubmit} noValidate className="auth-form">
        {formError ? <Alert type="error">{formError}</Alert> : null}
        {slowServer ? (
          <Alert type="info">The server is waking up - this can take up to a minute on the first request.</Alert>
        ) : null}

        <FormField id="email" label="Email" required error={errors.email}>
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            className="form-input"
            placeholder="you@example.com"
            value={values.email}
            onChange={handleChange}
            disabled={submitting}
            aria-invalid={errors.email ? 'true' : 'false'}
          />
        </FormField>

        <FormField id="password" label="Password" required error={errors.password}>
          <div className="input-with-action">
            <input
              id="password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              className="form-input"
              placeholder="Your password"
              value={values.password}
              onChange={handleChange}
              disabled={submitting}
              aria-invalid={errors.password ? 'true' : 'false'}
            />
            <button
              type="button"
              className="input-action"
              onClick={() => setShowPassword((s) => !s)}
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            >
              <Icon name={showPassword ? 'eyeOff' : 'eye'} size={18} />
            </button>
          </div>
        </FormField>

        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? <Spinner size="sm" /> : null}
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <p className="auth-switch">
        Don&apos;t have an account? <Link to="/register">Create one</Link>
      </p>
    </AuthLayout>
  );
}
