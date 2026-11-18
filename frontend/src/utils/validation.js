// Client-side validation mirrors the backend Bean Validation rules so users
// get instant feedback. The backend still validates everything.

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^\+?[0-9][0-9 ()-]{6,19}$/;
const PASSWORD_RE = /^(?=.*[A-Za-z])(?=.*\d).+$/;

export const GENDERS = ['Male', 'Female', 'Other'];

export const DEPARTMENT_SUGGESTIONS = [
  'Computer Science',
  'Information Technology',
  'Electronics',
  'Mechanical Engineering',
  'Civil Engineering',
  'Mathematics',
  'Physics',
  'Chemistry',
  'Biology',
  'Business Administration',
  'Commerce',
  'Arts',
];

function validateName(value) {
  const v = value.trim();
  if (!v) return 'Full name is required';
  if (v.length < 2 || v.length > 100) return 'Full name must be between 2 and 100 characters';
  return null;
}

function validateEmail(value) {
  const v = value.trim();
  if (!v) return 'Email is required';
  if (!EMAIL_RE.test(v)) return 'Enter a valid email address';
  if (v.length > 150) return 'Email must be at most 150 characters';
  return null;
}

function validatePhone(value) {
  const v = value.trim();
  if (!v) return 'Phone number is required';
  if (!PHONE_RE.test(v)) return 'Phone number must be 7-20 digits and may start with +';
  return null;
}

function collect(checks) {
  const errors = {};
  Object.entries(checks).forEach(([field, message]) => {
    if (message) errors[field] = message;
  });
  return errors;
}

export function validateLogin(values) {
  return collect({
    email: validateEmail(values.email),
    password: values.password ? null : 'Password is required',
  });
}

export function validateRegistration(values) {
  let password = null;
  if (!values.password) password = 'Password is required';
  else if (values.password.length < 8 || values.password.length > 72) password = 'Password must be 8-72 characters';
  else if (!PASSWORD_RE.test(values.password)) password = 'Password must contain at least one letter and one number';

  let confirmPassword = null;
  if (!values.confirmPassword) confirmPassword = 'Please confirm your password';
  else if (values.confirmPassword !== values.password) confirmPassword = 'Passwords do not match';

  return collect({
    fullName: validateName(values.fullName),
    email: validateEmail(values.email),
    phoneNumber: validatePhone(values.phoneNumber),
    password,
    confirmPassword,
  });
}

/** Today's date as yyyy-MM-dd in the user's local time zone. */
export function todayIso() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

export function validateStudent(values) {
  let department = null;
  if (!values.department.trim()) department = 'Department is required';
  else if (values.department.trim().length > 100) department = 'Department must be at most 100 characters';

  let dateOfBirth = null;
  if (!values.dateOfBirth) dateOfBirth = 'Date of birth is required';
  else if (values.dateOfBirth >= todayIso()) dateOfBirth = 'Date of birth must be in the past';

  return collect({
    fullName: validateName(values.fullName),
    email: validateEmail(values.email),
    phoneNumber: validatePhone(values.phoneNumber),
    department,
    gender: GENDERS.includes(values.gender) ? null : 'Please select a gender',
    dateOfBirth,
    address: values.address.length > 255 ? 'Address must be at most 255 characters' : null,
  });
}
