import { useEffect } from 'react';

export function useDocumentTitle(title) {
  useEffect(() => {
    document.title = title ? `${title} | Student Management` : 'Student Management System';
  }, [title]);
}
