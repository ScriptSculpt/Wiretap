import { clearToken, getValidToken, isTokenExpired, getToken } from '../auth';

export interface FetchOptions extends Omit<RequestInit, 'headers' | 'body'> {
  headers?: Record<string, string>;
  body?: unknown;
}

export const fetchWithAuth = async (url: string, options: FetchOptions = {}): Promise<Response> => {
  const token = getValidToken();
  const headers: Record<string, string> = {
    ...(options.headers ?? {}),
  };

  if (options.body !== undefined && !(options.body instanceof FormData) && typeof options.body !== 'string') {
    headers['Content-Type'] = headers['Content-Type'] ?? 'application/json';
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  if (!token) {
    const expiredToken = getToken();
    if (expiredToken && isTokenExpired(expiredToken)) {
      clearToken();
      throw new Error('Session expired. Please login again.');
    }
  }

  const body = options.body instanceof FormData
    ? options.body
    : typeof options.body === 'string'
      ? options.body
      : options.body !== undefined
        ? JSON.stringify(options.body)
        : undefined;

  console.log('Making API request to:', `${import.meta.env.VITE_API_BASE_URL}${url}`);
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}${url}`, {
    ...options,
    headers,
    body,
  });

  if (response.status === 401) {
    clearToken();
    throw new Error('Unauthorized. Please login again.');
  }

  return response;
};
