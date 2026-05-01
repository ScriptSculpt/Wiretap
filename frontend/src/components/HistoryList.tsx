import { useState, useEffect, type Dispatch, type SetStateAction } from 'react';
import type { ApiHistoryItem, ApiResponseBody } from '../types';
import { fetchHistory } from '../api/history';
import { deleteHistoryItem, deleteAllHistory } from '../api/delete';
import { retryApi } from '../api/retry';
import './HistoryList.css';
import { getMethodClass, getStatusClass } from '../styles/utils';

interface HistoryListProps {
  setResponse: Dispatch<SetStateAction<ApiResponseBody | null>>;
  setIsLoading: Dispatch<SetStateAction<boolean>>;
  setResponseError: Dispatch<SetStateAction<string | null>>;
}

export const HistoryList = ({ setResponse, setIsLoading, setResponseError }: HistoryListProps) => {
  const [items, setItems] = useState<ApiHistoryItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadHistory = async (pageNum: number) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchHistory({ page: pageNum, size: 5 });
      setItems(data.content);
      setTotalPages(data.totalPages);
      setPage(pageNum);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load history';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory(0);
  }, []);

  const handlePrev = () => {
    if (page > 0) loadHistory(page - 1);
  };

  const handleNext = () => {
    if (page < totalPages - 1) loadHistory(page + 1);
  };

  const handleRetry = async (item: ApiHistoryItem) => {
    setIsLoading(true);
    setResponseError(null);
    try {
      const result = await retryApi(item.id);
      setResponse(result);
      loadHistory(page);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Retry failed';
      setResponseError(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteItem = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await deleteHistoryItem(id);
      loadHistory(page);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to delete item';
      setError(message);
    }
  };

  const handleDeleteAll = async () => {
    if (!window.confirm('Are you sure you want to delete all the history?')) return;
    try {
      await deleteAllHistory();
      loadHistory(0);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to delete all history';
      setError(message);
    }
  };

  if (loading && items.length === 0) {
    return (
      <div className="history-list">
        <div className="history-list__title">History</div>
        <div className="history-list__loading">Loading...</div>
      </div>
    );
  }

  return (
    <div className="history-list">
      <div className="history-list__header">
        <div className="history-list__title">History</div>
        {items.length > 0 && (
          <button className="history-list__delete-all" onClick={handleDeleteAll} type="button" title="Delete all history">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              <line x1="10" y1="11" x2="10" y2="17"></line>
              <line x1="14" y1="11" x2="14" y2="17"></line>
            </svg>
            Delete All
          </button>
        )}
      </div>

      {error && <div className="history-list__error">{error}</div>}

      <div className="history-list__items">
        {items.map((item) => (
          <div key={item.id} className="history-item">
            <div className="history-item__main">
              <span className={`history-item__method ${getMethodClass(item.method)}`}>{item.method}</span>
              <span className="history-item__url" title={item.url}>
                {item.url.length > 40 ? `${item.url.slice(0, 40)}...` : item.url}
              </span>
            </div>
            <div className="history-item__meta">
              <span className={`history-item__status ${getStatusClass(item.statusCode)}`}>{item.statusCode}</span>
              <span className="history-item__time">{item.timeTaken}ms</span>
            </div>
            <div className="history-item__actions">
              <button className="history-item__retry" onClick={() => handleRetry(item)} type="button" title="Retry request">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="23 4 23 10 17 10"></polyline>
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                </svg>
              </button>
              <button className="history-item__delete" onClick={(e) => handleDeleteItem(item.id, e)} type="button" title="Delete">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        ))}
        {items.length === 0 && !error && <div className="history-list__empty">No history yet</div>}
      </div>

      <div className="history-list__pagination">
        <button onClick={handlePrev} disabled={page === 0} type="button" className="pagination-btn">
          Previous
        </button>
        <span className="pagination-info">
          {page + 1} / {totalPages || 1}
        </span>
        <button onClick={handleNext} disabled={page >= totalPages - 1} type="button" className="pagination-btn">
          Next
        </button>
      </div>
    </div>
  );
};