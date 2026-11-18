import { useNavigate } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import StudentForm from '../components/StudentForm.jsx';
import { studentService } from '../services/studentService.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';

export default function AddStudentPage() {
  useDocumentTitle('Add Student');
  const navigate = useNavigate();
  const { showToast } = useToast();

  const handleSubmit = async (values) => {
    const created = await studentService.create(values);
    showToast(`${created.fullName} was added successfully.`);
    navigate(`/students/${created.id}`);
  };

  return (
    <>
      <PageHeader title="Add Student" subtitle="Fill in the details to create a new student record." />
      <StudentForm submitLabel="Create student" cancelTo="/students" onSubmit={handleSubmit} />
    </>
  );
}
