import type { ApiResponseBody, RetryResponse } from '../types';

export const retryApi = async (id: number): Promise<ApiResponseBody> => {
    try {
        const response = await fetch(`/api/history/${id}/retry`, {
            method: 'GET',
        });
        const data: ApiResponseBody = await response.json();
        return data;

    } catch (error) {
        console.error('Error retrying API for id', id, ':', error);
        return {
            responseBody: "An error occurred while retrying for id " + id,
            statusCode: 0,
            timeTaken: 0,
            requestId: '',
        }
    }
};

export const retryFailed = async (): Promise<RetryResponse> => {
  const response = await fetch('/api/history/retry-failed', {
    method: 'GET',
  });

  if (!response.ok) {
    throw new Error(`Failed to run recovery: ${response.status}`);
  }

  return response.json();
};