import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import Alert from '../components/Alert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import Icon from '../components/Icon.jsx';
import { PageLoader, Spinner } from '../components/Spinner.jsx';
import { studentService } from '../services/studentService.js';
import { getErrorMessage } from '../services/api.js';
import { useDebounce } from '../hooks/useDebounce.js';
import { useToast } from '../hooks/useToast.js';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';
import { formatDate, initials } from '../utils/format.js';

export default function StudentListPage() {
  useDocumentTitle('Students');
  const { showToast } = useToast();
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword.trim(), 350);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const requestId = useRef(0);

  const loadStudents = useCallback(async (search) => {
    const id = ++requestId.current; // ignore responses from outdated searches
    setLoading(true);
    setError('');
    try {
      const data = search ? await studentService.search(search) : await studentService.getAll();
      if (id === requestId.current) setStudents(data);
    } catch (err) {
      if (id === requestId.current) setError(getErrorMessage(err, 'Could not load students.'));
    } finally {
      if (id === requestId.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStudents(debouncedKeyword);
  }, [debouncedKeyword, loadStudents]);

  const closeDeleteDialog = useCallback(() => setToDelete(null), []);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await studentService.remove(toDelete.id);
      setStudents((list) => list.filter((s) => s.id !== toDelete.id));
      showToast(`${toDelete.fullName} was deleted.`);
      setToDelete(null);
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not delete the student.'), 'error');
    } finally {
      setDeleting(false);
    }
  };

  const isSearching = debouncedKeyword.length > 0;

  return (
    <>
      <PageHeader
        title="Students"
        subtitle="View, search and manage your student records."
        actions={
          <Link to="/students/new" className="btn btn-primary">
            <Icon name="plus" size={18} /> Add Student
          </Link>
        }
      />

      <div className="card">
        <div className="toolbar">
          <div className="search-box">
            <Icon name="search" size={18} />
            <input
              type="search"
              placeholder="Search by name or email..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              aria-label="Search students by name or email"
            />
            {loading && students.length > 0 ? <Spinner size="sm" /> : null}
          </div>
          <span className="result-count">
            {students.length} {students.length === 1 ? 'student' : 'students'}
            {isSearching ? ' found' : ''}
          </span>
        </div>

        {error ? (
          <div className="card-body">
            <Alert
              type="error"
              action={
                <button className="btn btn-secondary btn-sm" onClick={() => loadStudents(debouncedKeyword)}>
                  <Icon name="refresh" size={16} /> Retry
                </button>
              }
            >
              {error}
            </Alert>
          </div>
        ) : loading && students.length === 0 ? (
          <PageLoader label="Loading students..." />
        ) : students.length === 0 ? (
          isSearching ? (
            <EmptyState icon="search" title="No matching students" message={`Nothing matches "${debouncedKeyword}".`} />
          ) : (
            <EmptyState
              icon="userPlus"
              title="No students yet"
              message="Students you add will appear here."
              action={<Link to="/students/new" className="btn btn-primary">Add your first student</Link>}
            />
          )
        ) : (
          <div className="table-wrap">
            <table className="data-table responsive-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Phone</th>
                  <th>Department</th>
                  <th>Gender</th>
                  <th>Added</th>
                  <th className="actions-col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {students.map((s) => (
                  <tr key={s.id}>
                    <td data-label="Student">
                      <Link to={`/students/${s.id}`} className="student-cell">
                        <span className="avatar">{initials(s.fullName)}</span>
                        <span>
                          <strong>{s.fullName}</strong>
                          <small>{s.email}</small>
                        </span>
                      </Link>
                    </td>
                    <td data-label="Phone">{s.phoneNumber}</td>
                    <td data-label="Department"><span className="badge">{s.department}</span></td>
                    <td data-label="Gender">{s.gender}</td>
                    <td data-label="Added">{formatDate(s.createdAt)}</td>
                    <td data-label="Actions" className="actions-col">
                      <div className="row-actions">
                        <Link to={`/students/${s.id}`} className="icon-btn" title="View" aria-label={`View ${s.fullName}`}>
                          <Icon name="eye" size={18} />
                        </Link>
                        <Link to={`/students/${s.id}/edit`} className="icon-btn" title="Edit" aria-label={`Edit ${s.fullName}`}>
                          <Icon name="edit" size={18} />
                        </Link>
                        <button
                          className="icon-btn icon-btn-danger"
                          title="Delete"
                          aria-label={`Delete ${s.fullName}`}
                          onClick={() => setToDelete(s)}
                        >
                          <Icon name="trash" size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete student?"
        message={toDelete ? `This will permanently delete ${toDelete.fullName}. This action cannot be undone.` : ''}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={handleDelete}
        onCancel={closeDeleteDialog}
      />
    </>
  );
}
