import { useState } from 'react'
import { useSelector } from 'react-redux'
import { NavLink, Outlet } from 'react-router'
import Header from '../components/Header'
import { isPlatformAdmin } from '../auth/access'
import type { RootState } from '../store/store'

export default function MainLayout() {
  const user = useSelector((state: RootState) => state.auth.user)
  const [menuOpen, setMenuOpen] = useState(false)
  const ready = user?.status === 'ACTIVE' && !user.passwordChangeRequired
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
            •••
          </span>
          <span className="text-[19px] font-bold tracking-tight">Messaging</span>
        </NavLink>
        <nav aria-label="Main navigation" className="space-y-1" onClick={() => setMenuOpen(false)}>
          {ready && (
            <>
              <NavLink to="/" end className={linkClass}>
                <span aria-hidden="true">•</span>Dashboard
              </NavLink>
              {isPlatformAdmin(user) && (
                <>
                  <p className="sidebar-label">PLATFORM</p>
                  <NavLink to="/admin" className={linkClass}>
                    <span aria-hidden="true">•</span>User administration
                  </NavLink>
                </>
              )}
              <p className="sidebar-label">MESSAGING</p>
              {['Inbox', 'Broadcasts', 'Contacts', 'Templates'].map((label) => (
                <div
                  key={label}
                  className="sidebar-link opacity-50"
                  aria-disabled="true"
                  title="Coming soon"
                >
                  <span aria-hidden="true">•</span>
                  {label}
                </div>
              ))}
              <p className="sidebar-label">MARKETING</p>
              <div className="sidebar-link opacity-50" aria-disabled="true" title="Coming soon">
                <span aria-hidden="true">•</span>Ad Center
              </div>
              <p className="sidebar-label">COMPANY</p>
              <NavLink to="/company/create" className={linkClass}>
                <span aria-hidden="true">•</span>Create company
              </NavLink>
            </>
          )}
          <p className="sidebar-label">ACCOUNT</p>
          {user?.status === 'PENDING_VERIFICATION' && (
            <NavLink to="/verify-email" className={linkClass}>
              <span aria-hidden="true">•</span>Verify email
            </NavLink>
          )}
          <NavLink to="/change-password" className={linkClass}>
            <span aria-hidden="true">•</span>Change password
          </NavLink>
        </nav>
        <div className="mt-auto border-t border-slate-100 pt-5 text-xs text-slate-500">
          <p className="font-medium text-slate-700">{user?.name}</p>
          <p className="mt-1 break-all">{user?.email}</p>
          <p className="mt-2 text-indigo-600">
            {user?.platformRoles.join(' · ') || 'Workspace account'}
          </p>
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
