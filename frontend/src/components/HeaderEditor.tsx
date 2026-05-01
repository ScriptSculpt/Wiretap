import type { Header } from '../types';
import './HeaderEditor.css';

interface HeaderEditorProps {
  headers: Header[];
  onChange: (headers: Header[]) => void;
}

const createEmptyHeader = (): Header => ({
  id: crypto.randomUUID(),
  key: '',
  value: '',
  enabled: true,
});

export const HeaderEditor = ({ headers, onChange }: HeaderEditorProps) => {
  const handleKeyChange = (id: string, key: string) => {
    const updated = headers.map((h) => (h.id === id ? { ...h, key } : h));
    onChange(updated);
  };

  const handleValueChange = (id: string, value: string) => {
    const updated = headers.map((h) => (h.id === id ? { ...h, value } : h));
    onChange(updated);
  };

  const handleToggle = (id: string) => {
    const updated = headers.map((h) => (h.id === id ? { ...h, enabled: !h.enabled } : h));
    onChange(updated);
  };

  const handleDelete = (id: string) => {
    const updated = headers.filter((h) => h.id !== id);
    onChange(updated);
  };

  const handleAdd = () => {
    onChange([...headers, createEmptyHeader()]);
  };

  return (
    <div className="header-editor">
      <div className="header-editor__header">
        <span className="header-editor__title">Headers</span>
        <button className="header-editor__add-btn" onClick={handleAdd} type="button">
          + Add
        </button>
      </div>
      <div className="header-editor__list">
        {headers.map((header) => (
          <div key={header.id} className={`header-row ${!header.enabled ? 'header-row--disabled' : ''}`}>
            <input
              type="checkbox"
              checked={header.enabled}
              onChange={() => handleToggle(header.id)}
              className="header-row__checkbox"
            />
            <input
              type="text"
              value={header.key}
              onChange={(e) => handleKeyChange(header.id, e.target.value)}
              placeholder="Key"
              className="header-row__input header-row__key"
            />
            <input
              type="text"
              value={header.value}
              onChange={(e) => handleValueChange(header.id, e.target.value)}
              placeholder="Value"
              className="header-row__input header-row__value"
            />
            <button
              className="header-row__delete"
              onClick={() => handleDelete(header.id)}
              type="button"
              aria-label="Delete header"
            >
              ×
            </button>
          </div>
        ))}
        {headers.length === 0 && (
          <div className="header-editor__empty">No headers added. Click + Add to create one.</div>
        )}
      </div>
    </div>
  );
};