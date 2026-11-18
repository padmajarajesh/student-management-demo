import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Alert from '../components/Alert.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import Icon from '../components/Icon.jsx';
import { PageLoader } from '../components/Spinner.jsx';
import { studentService } from '../services/studentService.js';
import { getErrorMessage } from '../services/api.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { calculateAge, formatDateTime, formatLocalDate, initials } from '../utils/format.js';

function DetailItem({ icon, label, children }) {
  return (
    <div className="detail-item">
      <span className="detail-icon"><Icon name={icon} size={18} /></span>
      <div>
        <p className="detail-label">{label}</p>
        <p className="detail-value">{children || '-'}</p>
      </div>
    </div>
  );
}

export default function StudentDetailsPage() {
  useDocumentTitle('Student Details');
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [student, setStudent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    studentService
      .getById(id)
      .then((data) => active && setStudent(data))
      .catch((err) => active && setError(getErrorMessage(err, 'Could not load the student.')))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [id]);

  const closeDialog = useCallback(() => setConfirmDelete(false), []);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await studentService.remove(id);
      showToast(`${student.fullName} was deleted.`);
      navigate('/students', { replace: true });
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not delete the student.'), 'error');
      setDeleting(false);
      setConfirmDelete(false);
    }
  };

  if (loading) return <PageLoader label="Loading student..." />;

  if (error) {
    return (
      <>
        <Alert type="error">{error}</Alert>
        <Link to="/students" className="btn btn-secondary mt-16">
          <Icon name="arrowLeft" size={18} /> Back to students
        </Link>
      </>
    );
  }

  const age = calculateAge(student.dateOfBirth);

  return (
    <>
      <Link to="/students" className="back-link">
        <Icon name="arrowLeft" size={18} /> Back to students
      </Link>

      <div className="card profile-card">
        <div className="profile-header">
          <span className="avatar avatar-xl">{initials(student.fullName)}</span>
          <div className="profile-heading">
            <h1 className="page-title">{student.fullName}</h1>
            <p className="page-subtitle">
              <span className="badge">{student.department}</span>
            </p>
          </div>
          <div className="page-actions">
            <Link to={`/students/${id}/edit`} className="btn btn-secondary">
              <Icon name="edit" size={18} /> Edit
            </Link>
            <button className="btn btn-danger-outline" onClick={() => setConfirmDelete(true)}>
              <Icon name="trash" size={18} /> Delete
            </button>
          </div>
        </div>

        <div className="detail-grid">
          <DetailItem icon="mail" label="Email">{student.email}</DetailItem>
          <DetailItem icon="phone" label="Phone number">{student.phoneNumber}</DetailItem>
          <DetailItem icon="layers" label="Department">{student.department}</DetailItem>
          <DetailItem icon="user" label="Gender">{student.gender}</DetailItem>
          <DetailItem icon="calendar" label="Date of birth">
            {formatLocalDate(student.dateOfBirth)}
            {age !== null ? ` (${age} years)` : ''}
          </DetailItem>
          <DetailItem icon="trendingUp" label="Registered on">{formatDateTime(student.createdAt)}</DetailItem>
          <DetailItem icon="mapPin" label="Address">{student.address}</DetailItem>
        </div>
      </div>

      <ConfirmDialog
        open={confirmDelete}
        title="Delete student?"
        message={`This will permanently delete ${student.fullName}. This action cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={handleDelete}
        onCancel={closeDialog}
      />
    </>
  );
}
