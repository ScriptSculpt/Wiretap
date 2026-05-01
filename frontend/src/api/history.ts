import type { HistoryResponse } from '../types';

interface HistoryParams {
  page?: number;
  size?: number;
  sortField?: string;
  desc?: boolean;
  status?: number;
  minThreshold?: number;
  maxThreshold?: number;
  method?: string;
  url?: string;
  search?: string;
}

export const fetchHistory = async (params: HistoryParams = {}): Promise<HistoryResponse> => {
  const searchParams = new URLSearchParams();

  if (params.page !== undefined) searchParams.append('page', String(params.page));
  if (params.size !== undefined) searchParams.append('size', String(params.size));
  if (params.sortField) searchParams.append('sortField', params.sortField);
  if (params.desc !== undefined) searchParams.append('desc', String(params.desc));
  if (params.status !== undefined) searchParams.append('status', String(params.status));
  if (params.minThreshold !== undefined) searchParams.append('minThreshold', String(params.minThreshold));
  if (params.maxThreshold !== undefined) searchParams.append('maxThreshold', String(params.maxThreshold));
  if (params.method) searchParams.append('method', params.method);
  if (params.url) searchParams.append('url', params.url);
  if (params.search) searchParams.append('search', params.search);

  const url = searchParams.toString() ? `/api/history?${searchParams.toString()}` : '/api/history'; 
  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Failed to fetch history: ${response.status}`);
  }

  return response.json();
};