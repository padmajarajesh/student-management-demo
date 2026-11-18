/** "2026-11-18T09:30:00Z" -> "Nov 18, 2026" */
export function formatDate(isoDateTime) {
  if (!isoDateTime) return '-';
  return new Date(isoDateTime).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

/** "2026-11-18T09:30:00Z" -> "Nov 18, 2026, 3:00 PM" (local time) */
export function formatDateTime(isoDateTime) {
  if (!isoDateTime) return '-';
  return new Date(isoDateTime).toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

/**
 * "2003-05-14" -> "May 14, 2003". Parsed as a local date so the day never
 * shifts because of time zones.
 */
export function formatLocalDate(isoDate) {
  if (!isoDate) return '-';
  const [y, m, d] = isoDate.split('-').map(Number);
  return new Date(y, m - 1, d).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function calculateAge(isoDate) {
  if (!isoDate) return null;
  const [y, m, d] = isoDate.split('-').map(Number);
  const today = new Date();
  let age = today.getFullYear() - y;
  if (today.getMonth() + 1 < m || (today.getMonth() + 1 === m && today.getDate() < d)) age -= 1;
  return age;
}

export function timeAgo(isoDateTime) {
  if (!isoDateTime) return '';
  const seconds = Math.floor((Date.now() - new Date(isoDateTime).getTime()) / 1000);
  if (seconds < 60) return 'just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hr${hours > 1 ? 's' : ''} ago`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days} day${days > 1 ? 's' : ''} ago`;
  return formatDate(isoDateTime);
}

export function initials(name = '') {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  const first = parts[0][0];
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase();
}
