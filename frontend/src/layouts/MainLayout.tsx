import { useState } from 'react'
import { FiHome, FiShield } from 'react-icons/fi'
import { NavLink, Outlet } from 'react-router'
import Header from '../components/Header'

export default function MainLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`

  return (
    <div className="app-shell">
      {menuOpen && (
        <button
          aria-label="Close navigation"
          onClick={() => setMenuOpen(false)}
          className="fixed inset-0 z-30 bg-slate-900/30 lg:hidden"
        />
      )}
      <aside id="main-navigation" className={`app-sidebar ${menuOpen ? 'is-open' : ''}`}>
        <NavLink
          to="/"
          className="flex items-center gap-3 px-1 pb-8 pt-1"
          onClick={() => setMenuOpen(false)}
        >
          <span
            className="flex size-[34px] items-center justify-center rounded-[10px] bg-indigo-500 text-sm font-bold text-white"
            aria-hidden="true"
          >
            ...
          </span>
          <span className="text-[19px] font-bold tracking-tight">Messaging</span>
        </NavLink>
        <nav aria-label="Main navigation" className="space-y-1" onClick={() => setMenuOpen(false)}>
          <p className="sidebar-label">APP</p>
          <NavLink to="/" end className={linkClass}>
            <FiHome aria-hidden="true" />Home
          </NavLink>
          <NavLink to="/auth" className={linkClass}>
            <FiShield aria-hidden="true" />Auth layout
          </NavLink>
        </nav>
        <div className="mt-auto border-t border-slate-100 pt-5 text-xs text-slate-500">
          <p className="font-medium text-slate-700">Frontend shell</p>
          <p className="mt-1">Ready for new pages.</p>
        </div>
      </aside>
      <div className="min-w-0 lg:ml-[248px]">
        <Header menuOpen={menuOpen} onToggleMenu={() => setMenuOpen(!menuOpen)} />
        <main id="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
