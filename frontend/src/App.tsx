import { Outlet } from 'react-router-dom'
import Nav from './components/Nav'
import SiteFooter from './components/SiteFooter'
import OnboardingModal from './components/OnboardingModal'
import { useOnboarding } from './context/OnboardingContext'
import { useAuth } from './context/AuthContext'
import type { OnboardingPrefs } from './types'

export default function App() {
  const { showModal, prefs, closeModal, setPrefs } = useOnboarding();
  const { token } = useAuth();

  function handleOnboardingComplete(p: OnboardingPrefs) {
    setPrefs(p, token ?? undefined);
  }

  return (
    <>
      <header>
        <Nav />
      </header>
      <main>
        <Outlet />
      </main>
      <SiteFooter />
      {showModal && (
        <OnboardingModal
          initial={prefs}
          onComplete={handleOnboardingComplete}
          onClose={closeModal}
        />
      )}
    </>
  )
}
