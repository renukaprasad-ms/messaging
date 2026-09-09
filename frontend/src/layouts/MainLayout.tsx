import { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { FiLogOut, FiShield, FiUser } from 'react-icons/fi'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import Header from '../components/Header'
import { isPlatformAdmin } from '../auth/access'
import { authService, getApiErrorMessage } from '../service/authService'
import { companyService, type CompanySummary } from '../service/companyService'
import { logout } from '../store/auth/authSlice'
import type { RootState } from '../store/store'

export default function MainLayout() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const user = useSelector((state: RootState) => state.auth.user)
  const [menuOpen, setMenuOpen] = useState(false)
  const [signingOut, setSigningOut] = useState(false)
  const [accountError, setAccountError] = useState('')
  const [companies, setCompanies] = useState<CompanySummary[]>([])
  const [companyPage, setCompanyPage] = useState(0)
  const [hasMoreCompanies, setHasMoreCompanies] = useState(false)
  const ready = user?.status === 'ACTIVE' && !user.passwordChangeRequired
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`

  useEffect(() => {
    if (!ready) return
    let active = true
    async function loadCompanies() {
      const data = await companyService.list(0)
      if (active) {
        setCompanies(data.companies)
        setCompanyPage(0)
        setHasMoreCompanies(data.hasNext)
      }
    }
    loadCompanies().catch(() => {
      if (active) {
        setCompanies([])
        setHasMoreCompanies(false)
      }
    })
    return () => {
      active = false
    }
  }, [ready])

  async function loadMoreCompanies() {
    const nextPage = companyPage + 1
    const data = await companyService.list(nextPage)
    setCompanies((current) => [...current, ...data.companies])
    setCompanyPage(nextPage)
    setHasMoreCompanies(data.hasNext)
  }

  const activeCompany = useMemo(() => {
    const match = pathname.match(/^\/companies\/(\d+)/)
    if (!match) return companies[0]
    return companies.find((company) => company.id === Number(match[1])) ?? companies[0]
  }, [companies, pathname])
  const companyIdFromPath = pathname.match(/^\/companies\/(\d+)/)?.[1]
  const companyProfilePath =
    companyIdFromPath || activeCompany ? `/companies/${companyIdFromPath ?? activeCompany?.id}` : ''

  async function signOut() {
    setAccountError('')
    setSigningOut(true)
    try {
      await authService.logout()
      dispatch(logout())
      navigate('/login', { replace: true })
    } catch (error) {
      setAccountError(getApiErrorMessage(error, 'Unable to sign out. Please try again.'))
    } finally {
      setSigningOut(false)
    }
  }

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
          {ready && (
            <>
              <NavLink to="/" end className={linkClass}>
                <span aria-hidden="true">-</span>Dashboard
              </NavLink>
              {isPlatformAdmin(user) && (
                <>
                  <p className="sidebar-label">PLATFORM</p>
                  <NavLink to="/admin" className={linkClass}>
                    <span aria-hidden="true">-</span>User administration
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
                  <span aria-hidden="true">-</span>
                  {label}
                </div>
              ))}
              <p className="sidebar-label">MARKETING</p>
              <div className="sidebar-link opacity-50" aria-disabled="true" title="Coming soon">
                <span aria-hidden="true">-</span>Ad Center
              </div>
              {companyProfilePath && (
                <>
                  <p className="sidebar-label">COMPANY</p>
                  <NavLink to={companyProfilePath} className={linkClass}>
                    <FiUser aria-hidden="true" />Profile
                  </NavLink>
                </>
              )}
            </>
          )}
          <p className="sidebar-label">ACCOUNT</p>
          {user?.status === 'PENDING_VERIFICATION' && (
            <NavLink to="/verify-email" className={linkClass}>
              <span aria-hidden="true">-</span>Verify email
            </NavLink>
          )}
          <NavLink to="/account/profile" className={linkClass}>
            <FiUser aria-hidden="true" />Profile
          </NavLink>
          <NavLink to="/account/security" className={linkClass}>
            <FiShield aria-hidden="true" />Security
          </NavLink>
          <button
            type="button"
            onClick={signOut}
            disabled={signingOut}
            className="sidebar-link w-full"
          >
            <FiLogOut aria-hidden="true" />
            {signingOut ? 'Signing out...' : 'Logout'}
          </button>
          {accountError && (
            <p role="alert" className="px-3 pt-2 text-xs text-red-600">
              {accountError}
            </p>
          )}
        </nav>
        <div className="mt-auto border-t border-slate-100 pt-5 text-xs text-slate-500">
          <p className="font-medium text-slate-700">{user?.name}</p>
          <p className="mt-1 break-all">{user?.email}</p>
          <p className="mt-2 text-indigo-600">
            {user?.platformRoles.join(' / ') || 'Workspace account'}
          </p>
        </div>
      </aside>
      <div className="min-w-0 lg:ml-[248px]">
        <Header
          menuOpen={menuOpen}
          onToggleMenu={() => setMenuOpen(!menuOpen)}
          companies={companies}
          hasMoreCompanies={hasMoreCompanies}
          onLoadMoreCompanies={loadMoreCompanies}
        />
        <main id="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
