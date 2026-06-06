export const formatDate = (timestamp) => {
  if (!timestamp) return 'N/A';
  const date = typeof timestamp.toDate === 'function' ? timestamp.toDate() : new Date(timestamp);
  return date.toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const getStatusClass = (status) => {
  switch ((status || '').toLowerCase()) {
    case 'approved':
    case 'active':
      return 'status-active';
    case 'rejected':
    case 'inactive':
      return 'status-inactive';
    case 'pending':
    default:
      return 'status-pending';
  }
};

export const getStatusBadgeLabel = (status) => {
  if (!status) return 'Unknown';
  switch (status.toLowerCase()) {
    case 'approved':
    case 'active':
      return 'Active';
    case 'rejected':
      return 'Rejected';
    case 'inactive':
      return 'Inactive';
    case 'pending':
    default:
      return 'Pending';
  }
};

export const getPendingCount = (items) => items.filter((item) => item.status === 'Pending').length;
