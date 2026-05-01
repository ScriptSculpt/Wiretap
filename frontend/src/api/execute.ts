import type { ApiRequestBody, ApiResponseBody } from '../types';

export const executeApi = async (request: ApiRequestBody): Promise<ApiResponseBody> => {
    try {
        const response = await fetch('/api/execute', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        const data: ApiResponseBody = await response.json();
        return data;

    } catch (error) {
        console.error('Error executing API:', error);
        return {
            responseBody: "An error occurred while executing the API request.",
            statusCode: 0,
            timeTaken: 0,
            requestId: '',
        }
    }
};