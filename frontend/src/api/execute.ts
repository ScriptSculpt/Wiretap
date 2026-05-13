import type { ApiRequestBody, ApiResponseBody } from '../types';
import { fetchWithAuth } from './client';

export const executeApi = async (request: ApiRequestBody): Promise<ApiResponseBody> => {
  const response = await fetchWithAuth('/api/execute', {
    method: 'POST',
    body: request,
  });

  if (!response.ok) {
    throw new Error(`Failed to execute request: ${response.status}`);
  }

  return response.json();
};
