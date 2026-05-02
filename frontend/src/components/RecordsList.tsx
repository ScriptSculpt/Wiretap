import { useState, useEffect } from 'react';
import type { ApiHistoryItem } from '../types';
import { fetchHistory } from '../api/history';
import { getMethodClass, getStatusClass } from '../styles/utils';
import './RecordsList.css';

interface RecordsListProps {
  ids: number[];
  title: string;
}

export const RecordsList = ({ ids, title }: RecordsListProps) => {
  const [items, setItems] = useState<ApiHistoryItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRecords = async (pageNum: number) => {
    if (ids.length === 0) {
      setItems([]);
      setTotalPages(0);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await fetchHistory({ ids, page: pageNum, size: 5 });
      setItems(data.content);
      setTotalPages(data.totalPages);
      setPage(pageNum);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load records';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRecords(0);
  }, [ids]);

  const handlePrev = () => {
    if (page > 0) loadRecords(page - 1);
  };

  const handleNext = () => {
    if (page < totalPages - 1) loadRecords(page + 1);
  };

  if (loading) {
    return (
      <div className="records-list">
        <div className="records-list__title">{title}</div>
        <div className="records-list__loading">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="records-list">
        <div className="records-list__title">{title}</div>
        <div className="records-list__error">{error}</div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="records-list">
        <div className="records-list__title">{title}</div>
        <div className="records-list__empty">No records found</div>
      </div>
    );
  }

  return (
    <div className="records-list">
      <div className="records-list__title">{title}</div>
      <div className="records-list__items">
        {items.map((item) => (
          <div key={item.id} className="records-list__item">
            <div className="records-list__item-header">
              <span className={`records-list__method ${getMethodClass(item.method)}`}>
                {item.method}
              </span>
              <span className={`records-list__status ${getStatusClass(item.statusCode)}`}>
                {item.statusCode}
              </span>
              <span className="records-list__time">{item.timeTaken}ms</span>
            </div>
            <div className="records-list__url">{item.url}</div>
          </div>
        ))}
      </div>
      {totalPages >= 1 && (
        <div className="records-list__pagination">
          <button
            className="records-list__page-btn"
            onClick={handlePrev}
            disabled={page === 0 || totalPages === 0}
            type="button"
          >
            Previous
          </button>
          <span className="records-list__page-info">
            {`${page + 1} / ${totalPages}`}
          </span>
          <button
            className="records-list__page-btn"
            onClick={handleNext}
            disabled={page >= totalPages - 1 || totalPages === 0}
            type="button"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};