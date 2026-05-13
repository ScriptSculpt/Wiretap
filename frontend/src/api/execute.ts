import type { ApiRequestBody, ApiResponseBody } from '../types';
import { fetchWithAuth } from './client';

export const executeApi = async (request: ApiRequestBody): Promise<ApiResponseBody> => {
  try {
    const response = await fetchWithAuth('/api/execute', {
      method: 'POST',
      body: request,
    });

    const data: ApiResponseBody = await response.json();
    return data;
    
  } catch (error) {
    console.error('Error executing API:', error);
    return {
      responseBody: 'An error occurred while executing the API request.',
      statusCode: 0,
      timeTaken: 0,
      requestId: '',
    };
  }
};
