import { useSelector } from 'react-redux'
import { Navigate, Outlet, useLocation } from 'react-router'
import type { RootState } from '../store/store'
import { isPlatformAdmin } from './access'

export default function RequireAuth({
  platformAdmin = false,
}: {
  platformAdmin?: boolean
}) {
  const { user, initialized } = useSelector((state: RootState) => state.auth)
  const location = useLocation()
  if (!initialized)
    return (
      <p role="status" className="p-8">
        Restoring your session…
      </p>
    )
  if (!user || user.status === 'SUSPENDED' || user.status === 'DISABLED')
    return <Navigate to="/login" replace />
  if (user.passwordChangeRequired && location.pathname !== '/change-password')
    return <Navigate to="/change-password" replace />
  if (platformAdmin && !isPlatformAdmin(user)) return <Navigate to="/forbidden" replace />
  return <Outlet />
}
