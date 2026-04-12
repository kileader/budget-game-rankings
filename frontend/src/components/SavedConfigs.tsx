import { useEffect, useState } from 'react';
import { listConfigs, deleteConfig } from '../api/rankingConfigs';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { RankingConfig } from '../types';
import './SavedConfigs.css';

type Props = {
  token: string;
  onLoad: (config: RankingConfig) => void;
  onSave: (name: string) => Promise<void>;
};

export default function SavedConfigs({ token, onLoad, onSave }: Props) {
  const { logout } = useAuth();
  const [configs, setConfigs] = useState<RankingConfig[]>([]);
  const [saveName, setSaveName] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  async function fetchConfigs() {
    try {
      const list = await listConfigs(token);
      setConfigs(list);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        logout();
      }
      // Otherwise non-critical — user can still use the page without saved configs
    }
  }

  useEffect(() => { void fetchConfigs(); }, [token]);

  async function handleSave(e: React.SubmitEvent) {
    e.preventDefault();
    if (!saveName.trim()) return;
    setError(null);
    setSaving(true);
    try {
      await onSave(saveName.trim());
      setSaveName('');
      setSaved(true);
      setTimeout(() => setSaved(false), 2500);
      await fetchConfigs();
    } catch (err) {
      const msg = err instanceof ApiError
        ? `Save failed: ${err.message} (${err.status})`
        : 'Save failed. Check your connection and try again.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: number) {
    try {
      await deleteConfig(id, token);
      setConfigs(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        logout();
      } else {
        setError('Delete failed.');
      }
    }
  }

  return (
    <section className="saved-configs" aria-label="Saved filter configurations">
      <h2>Saved Configs</h2>

      {configs.length === 0 && (
        <p className="saved-configs-empty">No saved configs yet.</p>
      )}

      {configs.length > 0 && (
        <ul className="saved-configs-list">
          {configs.map(config => (
            <li key={config.id} className="saved-config-item">
              <span className="saved-config-name">{config.name}</span>
              <div className="saved-config-actions">
                <button onClick={() => onLoad(config)}>Load</button>
                <button
                  className="delete"
                  onClick={() => void handleDelete(config.id)}
                  aria-label={`Delete config "${config.name}"`}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      <form className="saved-configs-save" onSubmit={handleSave} aria-label="Save current filters">
        {error && <p className="saved-configs-error" role="alert">{error}</p>}
        <input
          type="text"
          placeholder="Config name"
          value={saveName}
          onChange={e => setSaveName(e.target.value)}
          maxLength={100}
          aria-label="Config name"
        />
        <button type="submit" disabled={saving || !saveName.trim()}>
          {saving ? 'Saving...' : saved ? 'Saved ✓' : 'Save current filters'}
        </button>
      </form>
    </section>
  );
}
