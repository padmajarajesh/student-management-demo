import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import StudentForm from '../components/StudentForm.jsx';
import Alert from '../components/Alert.jsx';
import Icon from '../components/Icon.jsx';
import { PageLoader } from '../components/Spinner.jsx';
import { studentService } from '../services/studentService.js';
import { getErrorMessage } from '../services/api.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';

export default function EditStudentPage() {
  useDocumentTitle('Edit Student');
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [student, setStudent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    studentService
      .getById(id)
      .then((data) => active && setStudent(data))
      .catch((err) => active && setError(getErrorMessage(err, 'Could not load the student.')))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [id]);

  const handleSubmit = async (values) => {
    const updated = await studentService.update(id, values);
    showToast(`${updated.fullName} was updated successfully.`);
    navigate(`/students/${id}`);
  };

  if (loading) return <PageLoader label="Loading student..." />;

  if (error) {
    return (
      <>
        <PageHeader title="Edit Student" />
        <Alert type="error">{error}</Alert>
        <Link to="/students" className="btn btn-secondary mt-16">
          <Icon name="arrowLeft" size={18} /> Back to students
        </Link>
      </>
    );
  }

  return (
    <>
      <PageHeader title="Edit Student" subtitle={`Update the details for ${student.fullName}.`} />
      <StudentForm
        initialValues={student}
        submitLabel="Save changes"
        cancelTo={`/students/${id}`}
        onSubmit={handleSubmit}
      />
    </>
  );
}
