import { useEffect, useState } from 'react';
import { listConfigs, deleteConfig } from '../api/rankingConfigs';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { MetadataItem, RankingConfig } from '../types';
import './SavedConfigs.css';

type Props = {
  token: string;
  suggestedSaveName: string;
  platforms: MetadataItem[] | null;
  genres: MetadataItem[] | null;
  onLoad: (config: RankingConfig) => void;
  onSave: (name: string) => Promise<void>;
};

function formatConfigSummary(
  config: RankingConfig,
  platforms: MetadataItem[] | null,
  genres: MetadataItem[] | null,
): string {
  const bits: string[] = [];
  if (config.releaseYearMin != null || config.releaseYearMax != null) {
    bits.push(`${config.releaseYearMin ?? '…'}–${config.releaseYearMax ?? '…'}`);
  }
  const pids = config.platformIds ?? [];
  if (pids.length > 0) {
    const names = pids.slice(0, 2).map(id => platforms?.find(p => p.id === id)?.name ?? `#${id}`);
    let s = names.join(', ');
    if (pids.length > 2) s += ` +${pids.length - 2}`;
    bits.push(s);
  }
  const gids = config.genreIds ?? [];
  if (gids.length > 0) {
    const names = gids.slice(0, 2).map(id => genres?.find(g => g.id === id)?.name ?? `#${id}`);
    let s = names.join(', ');
    if (gids.length > 2) s += ` +${gids.length - 2}`;
    bits.push(s);
  }
  if (config.minPriceCents != null || config.maxPriceCents != null) {
    const lo = config.minPriceCents != null ? `$${(config.minPriceCents / 100).toFixed(0)}` : '…';
    const hi = config.maxPriceCents != null ? `$${(config.maxPriceCents / 100).toFixed(0)}` : '…';
    bits.push(`${lo}–${hi}`);
  }
  if (config.minPlaytimeHours != null || config.maxPlaytimeHours != null) {
    bits.push(
      `${config.minPlaytimeHours ?? '…'}–${config.maxPlaytimeHours ?? '…'}h playtime`,
    );
  }
  if (config.ratingWeight !== 1 || config.playtimeWeight !== 1 || config.priceWeight !== 1) {
    bits.push(`score R${config.ratingWeight}·P${config.playtimeWeight}·$${config.priceWeight}`);
  }
  if (config.excludeAdultRated === true) {
    bits.push('hide M/18+');
  }
  return bits.length > 0 ? bits.join(' · ') : 'No extra filters (defaults)';
}

export default function SavedConfigs({
  token,
  suggestedSaveName,
  platforms,
  genres,
  onLoad,
  onSave,
}: Props) {
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
      if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
        logout();
      }
    }
  }

  useEffect(() => { void fetchConfigs(); }, [token]);

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    const effective = (saveName.trim() || suggestedSaveName).slice(0, 100);
    if (!effective) return;
    setError(null);
    setSaving(true);
    try {
      await onSave(effective);
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
      if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
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
          {configs.map(config => {
            const summary = formatConfigSummary(config, platforms, genres);
            return (
              <li key={config.id} className="saved-config-item">
                <div className="saved-config-text">
                  <span className="saved-config-name">{config.name}</span>
                  <span className="saved-config-summary" title={summary}>
                    {summary}
                  </span>
                </div>
                <div className="saved-config-actions">
                  <button type="button" onClick={() => onLoad(config)}>Load</button>
                  <button
                    type="button"
                    className="delete"
                    onClick={() => void handleDelete(config.id)}
                    aria-label={`Delete config "${config.name}"`}
                  >
                    Delete
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}

      <form className="saved-configs-save" onSubmit={handleSave} aria-label="Save current filters">
        {error && <p className="saved-configs-error" role="alert">{error}</p>}
        <div className="saved-configs-save-row">
          <input
            type="text"
            placeholder={suggestedSaveName}
            value={saveName}
            onChange={e => setSaveName(e.target.value)}
            maxLength={100}
            aria-label="Config name (optional; uses suggested name if empty)"
          />
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : saved ? 'Saved ✓' : 'Save current filters'}
          </button>
        </div>
        <p className="saved-configs-save-hint">
          Name is optional — we suggest one from your filters and timestamp. Truncates at 100 characters.
        </p>
      </form>
    </section>
  );
}
