import { useState, useEffect } from 'react';
import { getPlatforms } from '../api/metadata';
import MultiSelect from './MultiSelect';
import type { MetadataItem, OnboardingPrefs } from '../types';
import './OnboardingModal.css';

const STORAGE_KEY = 'bgr_onboarding';

const PLATFORM_GROUPS: { label: string; ids: number[] }[] = [
  { label: 'Current Gen', ids: [6, 167, 169, 612, 130] },
  { label: 'Mobile', ids: [39, 34] },
  { label: 'VR', ids: [385, 390, 163, 165] },
  { label: 'Last Gen', ids: [48, 49, 41, 37, 14, 3] },
  { label: 'Retro', ids: [7, 12, 11, 9, 8, 5, 18, 20, 21, 22, 24, 33, 46, 38, 4, 19, 32, 29] },
];

export function loadOnboardingPrefs(): OnboardingPrefs | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveOnboardingPrefs(prefs: OnboardingPrefs) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
}

const DEFAULTS: OnboardingPrefs = {
  platformIds: [],
  yearPreset: 'modern',
  includeFreeToPlay: false,
  includeMultiplayerOnly: false,
};

type Step = 1 | 2 | 3 | 4;

export default function OnboardingModal({
  onComplete,
  onClose,
  initial,
}: {
  onComplete: (prefs: OnboardingPrefs) => void;
  onClose: () => void;
  initial?: OnboardingPrefs | null;
}) {
  const [step, setStep] = useState<Step>(1);
  const [prefs, setPrefs] = useState<OnboardingPrefs>(initial ?? DEFAULTS);
  const [platforms, setPlatforms] = useState<MetadataItem[] | null>(null);

  useEffect(() => {
    void getPlatforms().then(setPlatforms).catch(() => setPlatforms([]));
  }, []);

  function next() {
    if (step < 4) setStep((step + 1) as Step);
    else finish();
  }

  function back() {
    if (step > 1) setStep((step - 1) as Step);
  }

  function finish() {
    saveOnboardingPrefs(prefs);
    onComplete(prefs);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Escape') onClose();
  }

  const YEAR_PRESETS: { value: OnboardingPrefs['yearPreset']; label: string; sub: string }[] = [
    { value: 'modern', label: 'Modern', sub: '2015 and newer' },
    { value: 'classic', label: 'Classic', sub: '2005 and newer' },
    { value: 'all', label: 'All Time', sub: 'Every era' },
  ];

  return (
    <div className="onboarding-overlay" onKeyDown={handleKeyDown}>
      <div className="onboarding-modal" role="dialog" aria-modal aria-label="Set up your preferences">
        <button className="onboarding-close" onClick={onClose} aria-label="Close">&times;</button>

        <div className="onboarding-progress">
          {[1, 2, 3, 4].map(s => (
            <div key={s} className={`onboarding-dot${s === step ? ' active' : ''}${s < step ? ' done' : ''}`} />
          ))}
        </div>

        {step === 1 && (
          <div className="onboarding-step">
            <h2>What do you play on?</h2>
            <p className="onboarding-hint">Pick your platforms so we can show the most relevant games.</p>
            <div className="onboarding-platform-picker">
              <MultiSelect
                label="Platforms"
                options={platforms}
                selected={prefs.platformIds}
                onChange={ids => setPrefs(p => ({ ...p, platformIds: ids }))}
                searchable
                grouped={PLATFORM_GROUPS}
              />
            </div>
            {prefs.platformIds.length > 0 && platforms && (
              <div className="onboarding-tags">
                {prefs.platformIds.map(id => {
                  const p = platforms.find(x => x.id === id);
                  return p ? (
                    <span key={id} className="onboarding-tag">
                      {p.name}
                      <button type="button" onClick={() => setPrefs(prev => ({ ...prev, platformIds: prev.platformIds.filter(x => x !== id) }))} aria-label={`Remove ${p.name}`}>&times;</button>
                    </span>
                  ) : null;
                })}
              </div>
            )}
          </div>
        )}

        {step === 2 && (
          <div className="onboarding-step">
            <h2>How far back?</h2>
            <p className="onboarding-hint">Choose a release year range for the rankings.</p>
            <div className="onboarding-year-buttons">
              {YEAR_PRESETS.map(p => (
                <button
                  key={p.value}
                  type="button"
                  className={`onboarding-year-btn${prefs.yearPreset === p.value ? ' selected' : ''}`}
                  onClick={() => setPrefs(prev => ({ ...prev, yearPreset: p.value }))}
                >
                  <strong>{p.label}</strong>
                  <span>{p.sub}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="onboarding-step">
            <h2>Include free-to-play games?</h2>
            <p className="onboarding-hint">Free games use a different scoring formula (coming soon). For now this filters them in or out.</p>
            <div className="onboarding-toggle-group">
              <button
                type="button"
                className={`onboarding-toggle-btn${prefs.includeFreeToPlay ? ' selected' : ''}`}
                onClick={() => setPrefs(p => ({ ...p, includeFreeToPlay: true }))}
              >
                Yes, include them
              </button>
              <button
                type="button"
                className={`onboarding-toggle-btn${!prefs.includeFreeToPlay ? ' selected' : ''}`}
                onClick={() => setPrefs(p => ({ ...p, includeFreeToPlay: false }))}
              >
                No, skip them
              </button>
            </div>
          </div>
        )}

        {step === 4 && (
          <div className="onboarding-step">
            <h2>Include multiplayer-only games?</h2>
            <p className="onboarding-hint">Multiplayer-only titles don't have a single-player hours-to-beat, so scoring works differently (coming soon).</p>
            <div className="onboarding-toggle-group">
              <button
                type="button"
                className={`onboarding-toggle-btn${prefs.includeMultiplayerOnly ? ' selected' : ''}`}
                onClick={() => setPrefs(p => ({ ...p, includeMultiplayerOnly: true }))}
              >
                Yes, include them
              </button>
              <button
                type="button"
                className={`onboarding-toggle-btn${!prefs.includeMultiplayerOnly ? ' selected' : ''}`}
                onClick={() => setPrefs(p => ({ ...p, includeMultiplayerOnly: false }))}
              >
                No, skip them
              </button>
            </div>
          </div>
        )}

        <div className="onboarding-actions">
          {step > 1 && (
            <button type="button" className="onboarding-btn secondary" onClick={back}>Back</button>
          )}
          <button type="button" className="onboarding-btn primary" onClick={next}>
            {step === 4 ? 'Done' : 'Next'}
          </button>
          {step === 1 && (
            <button type="button" className="onboarding-btn skip" onClick={() => { saveOnboardingPrefs(DEFAULTS); onComplete(DEFAULTS); }}>
              Skip setup
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
