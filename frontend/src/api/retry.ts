import type { ApiResponseBody } from '../types';

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