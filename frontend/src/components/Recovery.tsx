import { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';
import type { RetryResponse } from '../types';
import { retryFailed } from '../api/retry';
import { RecordsList } from './RecordsList';
import './Recovery.css';

type RecoveryStatus = 'idle' | 'loading' | 'done';

export const Recovery = () => {
  const [status, setStatus] = useState<RecoveryStatus>('idle');
  const [response, setResponse] = useState<RetryResponse | null>(null);

  const handleRunRecovery = async () => {
    setStatus('loading');
    try {
      const result = await retryFailed();
      setResponse(result);
      setStatus('done');
    } catch (err) {
      console.error('Recovery failed:', err);
      setStatus('idle');
    }
  };

  const getSuccessPercentage = () => {
    if (!response || response.total === 0 || response.success === undefined) return 0;
    return Math.round((response.success / response.total) * 100);
  };

  const chartData = response ? [
    { name: 'Success', value: response.success, color: '#22c55e' },
    { name: 'Failed', value: response.failed, color: '#ef4444' },
    { name: 'Skipped', value: response.skipped, color: '#6b7280' },
  ] : [];

  if (status === 'idle') {
    return (
      <div className="recovery">
        <div className="recovery__idle">
          <h2 className="recovery__title">Recovery</h2>
          <p className="recovery__description">
            Retry all failed API calls to check if issues are resolved
          </p>
          <button
            className="recovery__button"
            onClick={handleRunRecovery}
            type="button"
          >
            Run Recovery
          </button>
          <p className="recovery__helper">
            This may take a few seconds depending on number of failed requests
          </p>
        </div>
      </div>
    );
  }

  if (status === 'loading') {
    return (
      <div className="recovery">
        <div className="recovery__loading">
          <div className="recovery__spinner"></div>
          <h3 className="recovery__loading-title">Running recovery...</h3>
          <p className="recovery__loading-text">
            Retrying failed requests, this may take a while
          </p>
        </div>
      </div>
    );
  }

  if (status === 'done' && response) {
    return (
      <div className="recovery">
        {
            response.total === 0 ? (
                <div className="recovery__error">No failed requests to recover.</div>
            ) : (
                <div className="recovery__results">
                    <div className="recovery__top-section">
                        <div className="recovery__summary">
                        <div className="recovery__summary-percentage">{getSuccessPercentage()}%</div>
                        <h3 className="recovery__summary-title">Success</h3>
                        <p className="recovery__summary-subtitle">
                            {response.success === undefined ? 0 : response.success} of {response.total} requests recovered
                        </p>
                        </div>

                        <div className="recovery__chart">
                        <ResponsiveContainer width="100%" height={200}>
                        <PieChart>
                            <Pie
                            data={chartData}
                            cx="50%"
                            cy="50%"
                            innerRadius={40}
                            outerRadius={80}
                            paddingAngle={2}
                            dataKey="value"
                            >
                            {chartData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                            </Pie>
                        </PieChart>
                        </ResponsiveContainer>
                        <div className="recovery__chart-legend">
                        {chartData.map((entry, index) => (
                            <div key={index} className="recovery__chart-legend-item">
                            <div
                                className="recovery__chart-legend-color"
                                style={{ backgroundColor: entry.color }}
                            ></div>
                            <span className="recovery__chart-legend-label">
                                {entry.name}: {entry.value ?? 0}
                            </span>
                            </div>
                        ))}
                        </div>
                    </div>
                    </div>

                    <div className="recovery__sections">
                        <div className="recovery__section-wrapper">
                        <RecordsList
                            ids={response.failedIds || []}
                            title={`Still Failing (${response.failed ?? 0})`}
                        />
                        </div>
                        <div className="recovery__section-wrapper">
                        <RecordsList
                            ids={response.successIds || []}
                            title={`Recovered Successfully (${response.success ?? 0})`}
                        />
                        </div>
                    </div>
                </div>
            )
        }
      </div>
    );
  }

  return null;
};