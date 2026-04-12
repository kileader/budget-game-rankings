import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { OnboardingPrefs } from '../types';
import { loadOnboardingPrefs } from '../components/OnboardingModal';
import { listConfigs, createConfig, updateConfig } from '../api/rankingConfigs';

const MY_SETUP_NAME = 'My Setup';

const YEAR_PRESET_MIN: Record<OnboardingPrefs['yearPreset'], number | undefined> = {
  modern: 2015,
  classic: 2005,
  all: undefined,
};

async function upsertMySetup(prefs: OnboardingPrefs, token: string): Promise<void> {
  const configs = await listConfigs(token);
  const existing = configs.find(c => c.name === MY_SETUP_NAME);
  const body = {
    name: MY_SETUP_NAME,
    ...(prefs.platformIds.length ? { platformIds: prefs.platformIds } : {}),
    ...(YEAR_PRESET_MIN[prefs.yearPreset] !== undefined
      ? { releaseYearMin: YEAR_PRESET_MIN[prefs.yearPreset] }
      : {}),
  };
  if (existing) {
    await updateConfig(existing.id, body, token);
  } else {
    await createConfig(body, token);
  }
}

type OnboardingCtx = {
  prefs: OnboardingPrefs | null;
  showModal: boolean;
  openModal: () => void;
  closeModal: () => void;
  setPrefs: (prefs: OnboardingPrefs, token?: string) => void;
};

const Ctx = createContext<OnboardingCtx | null>(null);

export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [prefs, setPrefs] = useState<OnboardingPrefs | null>(loadOnboardingPrefs);
  const [showModal, setShowModal] = useState(() => loadOnboardingPrefs() === null);

  const openModal = useCallback(() => setShowModal(true), []);
  const closeModal = useCallback(() => setShowModal(false), []);

  const handleSetPrefs = useCallback((p: OnboardingPrefs, token?: string) => {
    setPrefs(p);
    setShowModal(false);
    if (token) {
      void upsertMySetup(p, token).catch(() => {
        // silent — not critical if save fails
      });
    }
  }, []);

  return (
    <Ctx.Provider value={{ prefs, showModal, openModal, closeModal, setPrefs: handleSetPrefs }}>
      {children}
    </Ctx.Provider>
  );
}

export function useOnboarding() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useOnboarding must be inside OnboardingProvider');
  return ctx;
}
