import { Outlet } from 'react-router-dom'
import Nav from './components/Nav'

export default function App() {
  return (
    <>
      <header>
        <Nav />
      </header>
      <main>
        <Outlet />
      </main>
    </>
  )
}
