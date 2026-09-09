import { useMemo, useState } from 'react'
import { FiChevronDown, FiMenu, FiPlus } from 'react-icons/fi'
import { Link, useLocation, useNavigate } from 'react-router'
import type { CompanySummary } from '../service/companyService'

export default function Header({
  menuOpen,
  onToggleMenu,
  companies,
  hasMoreCompanies,
  onLoadMoreCompanies,
}: {
  menuOpen: boolean
  onToggleMenu: () => void
  companies: CompanySummary[]
  hasMoreCompanies: boolean
  onLoadMoreCompanies: () => Promise<void>
}) {
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [open, setOpen] = useState(false)
  const title =
    pathname === '/admin'
      ? 'Platform administration'
      : pathname === '/account/profile'
        ? 'Profile'
        : pathname === '/account/security'
          ? 'Security'
          : pathname === '/change-password'
            ? 'Security'
            : pathname === '/verify-email'
              ? 'Email verification'
              : pathname.includes('compan')
                ? 'Company workspace'
                : 'Dashboard'

  const activeCompany = useMemo(() => {
    const match = pathname.match(/^\/companies\/(\d+)/)
    if (!match) return companies[0]
    return companies.find((company) => company.id === Number(match[1]))
  }, [companies, pathname])

  const companyIdFromPath = pathname.match(/^\/companies\/(\d+)/)?.[1]
  const workspaceName =
    activeCompany?.displayName || activeCompany?.name || (companyIdFromPath ? 'Company' : 'No company')

  return (
    <header className="app-header">
      <div className="flex min-w-0 items-center gap-3">
        <button
          onClick={onToggleMenu}
          aria-expanded={menuOpen}
          aria-controls="main-navigation"
          aria-label="Toggle navigation"
          className="secondary-button lg:hidden"
        >
          <FiMenu aria-hidden="true" />
        </button>
        <div>
          <p className="text-lg font-semibold tracking-tight">{title}</p>
          <p className="mt-1 hidden text-[11px] text-slate-500 sm:block">
            Messaging, broadcasts, advertising and account health
          </p>
        </div>
      </div>
      <div className="relative">
        <button
          type="button"
          className="workspace-switcher"
          aria-expanded={open}
          aria-haspopup="menu"
          onClick={() => setOpen((current) => !current)}
        >
          <span className="min-w-0 truncate">{workspaceName}</span>
          <FiChevronDown aria-hidden="true" />
        </button>
        {open && (
          <div className="workspace-menu" role="menu">
            {companies.length ? (
              companies.map((company) => (
                <button
                  key={company.id}
                  type="button"
                  role="menuitem"
                  className="workspace-menu-item"
                  onClick={() => {
                    setOpen(false)
                    navigate(`/companies/${company.id}`)
                  }}
                >
                  <span className="truncate">{company.displayName || company.name}</span>
                  <span className="text-[10px] font-semibold text-slate-400">{company.role}</span>
                </button>
              ))
            ) : (
              <p className="px-3 py-2 text-xs text-slate-500">Create a company to start.</p>
            )}
            {hasMoreCompanies && (
              <button
                type="button"
                className="workspace-menu-item text-indigo-600"
                onClick={() => onLoadMoreCompanies()}
              >
                Load more
              </button>
            )}
            <Link
              role="menuitem"
              className="workspace-menu-item border-t border-slate-100 text-indigo-600"
              to="/company/create"
              onClick={() => setOpen(false)}
            >
              <FiPlus aria-hidden="true" />
              Create company
            </Link>
          </div>
        )}
      </div>
    </header>
  )
}
