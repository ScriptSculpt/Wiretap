import { getStatusClass } from '../styles/utils';
import type { ApiResponseBody } from '../types';
import './ResponsePanel.css';

interface ResponsePanelProps {
  response: ApiResponseBody | null;
  isLoading: boolean;
}

const formatJson = (obj: unknown): string => {
  try {
    return JSON.stringify(obj, null, 2);
  } catch {
    return String(obj);
  }
};

export const ResponsePanel = ({ response, isLoading }: ResponsePanelProps) => {
  const handleCopy = async () => {
    if (!response) return;
    const text = formatJson(response);
    await navigator.clipboard.writeText(text);
  };

  if (isLoading) {
    return (
      <div className="response-panel">
        <div className="response-panel__title">Response</div>
        <div className="response-panel__loading">
          <div className="response-panel__spinner" />
          <span>Executing request...</span>
        </div>
      </div>
    );
  }

  if (!response) {
    return (
      <div className="response-panel">
        <div className="response-panel__title">Response</div>
        <div className="response-panel__empty">
          <span>Send a request to see the response</span>
        </div>
      </div>
    );
  }

  return (
    <div className="response-panel">
      <div className="response-panel__header">
        <div className="response-panel__title">Response</div>
        <button className="response-panel__copy" onClick={handleCopy} type="button" title="Copy response">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
          </svg>
        </button>
      </div>

      <div className="response-panel__meta">
        <div className={`response-panel__status ${getStatusClass(response.statusCode)}`}>
          {response.statusCode}
        </div>
        <div className="response-panel__time">{response.timeTaken}ms</div>
      </div>

      <div className="response-panel__body-section">
        <pre className="response-panel__body">{formatJson(response)}</pre>
      </div>
    </div>
  );
};