import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useLocation, useNavigate } from 'react-router'
import { authService, getApiErrorMessage } from '../service/authService'
import { logout } from '../store/auth/authSlice'
import type { RootState } from '../store/store'

export default function Header({
  menuOpen,
  onToggleMenu,
}: {
  menuOpen: boolean
  onToggleMenu: () => void
}) {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const user = useSelector((state: RootState) => state.auth.user)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const title =
    pathname === '/admin'
      ? 'Platform administration'
      : pathname === '/change-password'
        ? 'Account security'
        : pathname === '/verify-email'
          ? 'Email verification'
          : pathname.includes('compan')
            ? 'Company workspace'
            : 'Dashboard'

  async function signOut() {
    setBusy(true)
    try {
      await authService.logout()
      dispatch(logout())
      navigate('/login', { replace: true })
    } catch (error) {
      setError(getApiErrorMessage(error, 'Unable to sign out. Please try again.'))
    } finally {
      setBusy(false)
    }
  }

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
          ☰
        </button>
        <div>
          <p className="text-lg font-semibold tracking-tight">{title}</p>
          <p className="mt-1 hidden text-[11px] text-slate-500 sm:block">
            Messaging, broadcasts, advertising and account health
          </p>
        </div>
      </div>
      <div className="flex items-center gap-3">
        <span className="hidden size-9 items-center justify-center rounded-lg bg-indigo-50 text-xs font-semibold text-indigo-600 sm:flex">
          {user?.name
            .split(' ')
            .map((part) => part[0])
            .slice(0, 2)
            .join('')}
        </span>
        <button onClick={signOut} disabled={busy} className="secondary-button">
          {busy ? 'Signing out…' : 'Logout'}
        </button>
      </div>
      {error && (
        <p
          role="alert"
          className="absolute right-4 top-full z-20 rounded border border-red-100 bg-white p-3 text-sm text-red-600"
        >
          {error}
        </p>
      )}
    </header>
  )
}
