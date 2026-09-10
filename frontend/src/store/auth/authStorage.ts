import type { AuthUser } from '../../service/authService'

const AUTH_USER_STORAGE_KEY = 'messaging.auth.user'

export function readStoredUser(): AuthUser | null {
  if (typeof window === 'undefined') return null

  try {
    const value = window.localStorage.getItem(AUTH_USER_STORAGE_KEY)
    if (!value) return null
    const user = JSON.parse(value) as Partial<AuthUser>
    if (typeof user.email !== 'string' || typeof user.status !== 'string') return null
    return user as AuthUser
  } catch {
    return null
  }
}

export function writeStoredUser(user: AuthUser | null) {
  if (typeof window === 'undefined') return

  if (user) {
    window.localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(user))
    return
  }

  window.localStorage.removeItem(AUTH_USER_STORAGE_KEY)
}
