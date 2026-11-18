import { Link } from 'react-router-dom';
import { useDocumentTitle } from '../hooks/useDocumentTitle.js';

export default function NotFoundPage() {
  useDocumentTitle('Page not found');
  return (
    <div className="not-found">
      <p className="not-found-code">404</p>
      <h1>Page not found</h1>
      <p>The page you are looking for does not exist or has been moved.</p>
      <Link to="/dashboard" className="btn btn-primary">Go to dashboard</Link>
    </div>
  );
}
