export const getStatusClass = (status: number): string => {
  if (status >= 200 && status < 300) return 'status--success';
  if (status >= 300 && status < 400) return 'status--redirect';
  if (status >= 400 && status < 500) return 'status--client-error';
  if (status >= 500) return 'status--server-error';
  return '';
};

export const getMethodClass = (method: string): string => {
    const m = method.toUpperCase();
    if (m === 'GET') return 'method--get';
    if (m === 'POST') return 'method--post';
    if (m === 'PUT') return 'method--put';
    if (m === 'PATCH') return 'method--patch';
    if (m === 'DELETE') return 'method--delete';
    return '';
  };