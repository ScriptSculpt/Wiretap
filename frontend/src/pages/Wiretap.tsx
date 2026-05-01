import { useState, useEffect } from 'react';
import type { ApiResponseBody, ApiRequestBody } from '../types';
import { RequestPanel } from '../components/RequestPanel';
import { ResponsePanel } from '../components/ResponsePanel';
import { HistoryList } from '../components/HistoryList';
import './Wiretap.css';
import { executeApi } from '../api/execute';

export const Wiretap = () => {
  const [response, setResponse] = useState<ApiResponseBody | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'request' | 'history'>('request');

  // Clear response when switching tabs
  useEffect(() => {
    setResponse(null);
    setError(null);
  }, [activeTab]);

  const handleExecute = async (request: ApiRequestBody) => {
    setIsLoading(true);
    setError(null);
    setResponse(null);

    try {
      const result = await executeApi(request);
      setResponse(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="console">
      <header className="console__header">
        <h1 className="console__logo">Wiretap</h1>
        <p className="console__tagline">API Testing Made Simple</p>
      </header>

      <div className="console__tabs">
        <button
          className={`console__tab ${activeTab === 'request' ? 'console__tab--active' : ''}`}
          onClick={() => {
            setResponse(null);
            setError(null);
            setActiveTab('request');
          }}
        >
          Request
        </button>
        <button
          className={`console__tab ${activeTab === 'history' ? 'console__tab--active' : ''}`}
          onClick={() => {
            setResponse(null);
            setError(null);
            setActiveTab('history');
          }}
        >
          History
        </button>
      </div>

      <div className="console__content">
        {activeTab === 'request' ? (
          <div className="console__split">
            <div className="console__panel">
              <RequestPanel onExecute={handleExecute} isLoading={isLoading} error={error} />
            </div>
            <div className="console__panel">
              <ResponsePanel response={response} isLoading={isLoading} />
            </div>
          </div>
        ) : (
          <div className="console__split">
            <div className="console__panel">
              <HistoryList 
                setResponse={setResponse}
                setIsLoading={setIsLoading}
                setResponseError={setError}
              />
            </div>
            <div className="console__panel">
              <ResponsePanel response={response} isLoading={isLoading} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};