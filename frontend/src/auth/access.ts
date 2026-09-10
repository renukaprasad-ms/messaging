import type { AuthUser } from '../service/authService'

export function isPlatformAdmin(user: AuthUser | null): boolean {
  return (
    user?.status === 'ACTIVE' &&
    user.platformRoles.some((role) => role === 'ADMIN' || role === 'SUPERADMIN') === true
  )
}

export function homePath(user: AuthUser): string {
  if (user.passwordChangeRequired) return '/change-password'
  if (user.status === 'PENDING_VERIFICATION') return '/'
  if (user.status !== 'ACTIVE') return '/login'
  if (isPlatformAdmin(user)) return '/admin'
  if (user.accountType === 'ORGANIZATION' && !user.hasCompany) return '/company/create'
  return '/'
}

export function canManageCompany(role: string): boolean {
  return role === 'OWNER' || role === 'ADMIN'
}
