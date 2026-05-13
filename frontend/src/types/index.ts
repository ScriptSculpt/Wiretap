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

export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface ApiHistoryItem {
  id: number;
  url: string;
  method: string;
  statusCode: number;
  timeTaken: number;
}

export interface RetryResponse {
  total: number;
  success: number;
  failed: number;
  skipped: number;
  successIds: number[];
  failedIds: number[];
}

export interface HistoryResponse {
  content: ApiHistoryItem[];
  totalPages: number;
  totalElements: number;
  size: number;
  page: number;
}