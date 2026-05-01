import { useState } from 'react';
import type { HttpMethod, Header, ApiRequestBody } from '../types';
import { HeaderEditor } from './HeaderEditor';
import './RequestPanel.css';

interface RequestPanelProps {
  onExecute: (request: ApiRequestBody) => Promise<void>;
  isLoading: boolean;
  error: string | null;
}

const HTTP_METHODS: HttpMethod[] = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];

export const RequestPanel = ({ onExecute, isLoading, error }: RequestPanelProps) => {
  const [url, setUrl] = useState('');
  const [method, setMethod] = useState<HttpMethod>('GET');
  const [body, setBody] = useState('');
  const [headers, setHeaders] = useState<Header[]>([
    { id: crypto.randomUUID(), key: 'Content-Type', value: 'application/json', enabled: true },
  ]);

  const handleSubmit = async () => {
    if (!url.trim()) return;

    const enabledHeaders = headers.filter((h) => h.enabled && h.key.trim());
    
    const headersObject: Record<string, string> = {};
    enabledHeaders.forEach(h => {
    if (h.key.trim()) {
        headersObject[h.key] = h.value.trim();
    }
    });

    console.log('headersObject', headersObject);

    await onExecute({
      url: url.trim(),
      method,
      body: ['POST', 'PUT', 'PATCH'].includes(method) ? body : undefined,
      headers: Object.keys(headersObject).length > 0 ? headersObject : undefined,
    });
  };

  return (
    <div className="request-panel">
      <div className="request-panel__title">Request</div>
      <div className="request-panel__form">
        <div className="request-panel__url-row">
          <select
            value={method}
            onChange={(e) => setMethod(e.target.value as HttpMethod)}
            className={`request-panel__method request-panel__method--${method.toLowerCase()}`}
            disabled={isLoading}
          >
            {HTTP_METHODS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="Enter request URL"
            className="request-panel__url"
            disabled={isLoading}
          />
          <button 
            type="button" 
            className="request-panel__execute" 
            disabled={isLoading || !url.trim()}
            onClick={handleSubmit}
          >
            {isLoading ? 'Executing...' : 'Execute'}
          </button>
        </div>

        <div className="request-panel__body-section">
          <label className="request-panel__label">Request Body (JSON)</label>
          <textarea
            value={body}
            onChange={(e) => setBody(e.target.value)}
            placeholder='{"key": "value"}'
            className="request-panel__body"
            disabled={isLoading}
          />
        </div>

        <HeaderEditor headers={headers} onChange={setHeaders} />

        {error && <div className="request-panel__error">{error}</div>}
      </div>
    </div>
  );
};