import type { ApiResponseBody, RetryResponse } from '../types';
import { fetchWithAuth } from './client';

export const retryApi = async (id: number): Promise<ApiResponseBody> => {
  const response = await fetchWithAuth(`/api/history/${id}/retry`, {
    method: 'GET',
  });

  if (!response.ok) {
    throw new Error(`Failed to retry request: ${response.status}`);
  }

  return response.json();
};

export const retryFailed = async (): Promise<RetryResponse> => {
  const response = await fetchWithAuth('/api/history/retry-failed', {
    method: 'GET',
  });

  if (!response.ok) {
    throw new Error(`Failed to run recovery: ${response.status}`);
  }

  return response.json();
};
