export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface Header {
  id: string;
  key: string;
  value: string;
  enabled: boolean;
}

export interface ApiRequestBody {
  url: string;
  method: HttpMethod;
  body?: string;
  headers?: Record<string, string>;
}

export interface ApiResponseBody {
  responseBody: unknown;
  statusCode: number;
  timeTaken: number;
  requestId: string;
}

export interface ApiHistoryItem {
  id: number;
  url: string;
  method: string;
  statusCode: number;
  timeTaken: number;
//   timestamp: string;   // Needs to be removed
//   requestBody?: string;
//   responseBody?: string;
//   headers?: Record<string, string>;    // needs to be removed
}

// {
//     "size": 5,
//     "totalPages": 1,
//     "page": 0,
//     "content": [
//         {
//             "id": 1,
//             "url": "https://jsonplaceholder.typicode.com/posts",
//             "method": "POST",
//             "statusCode": 201,
//             "timeTaken": 889
//         }
//     ],
//     "totalElements": 1
// }

export interface HistoryResponse {
  content: ApiHistoryItem[];
  totalPages: number;
  totalElements: number;
  size: number;
  page: number;
}