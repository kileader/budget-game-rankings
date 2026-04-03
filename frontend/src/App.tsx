import { Outlet } from 'react-router-dom'

export default function App() {
  return (
    <>
      <header>
        <nav>
          <span className="site-title">Budget Game Rankings</span>
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
    </>
  )
}
