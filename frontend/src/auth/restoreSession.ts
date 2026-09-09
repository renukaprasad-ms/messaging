import { AxiosError } from 'axios'
import { authService } from '../service/authService'
import { store } from '../store/store'
import { logout, sessionReady, setUser } from '../store/auth/authSlice'

let restoration: Promise<void> | null = null

export function restoreSession(): Promise<void> {
  restoration ??= authService
    .me()
    .then((response) => {
      if (response.data) store.dispatch(setUser(response.data))
      store.dispatch(sessionReady())
    })
    .catch((error: unknown) => {
      if (error instanceof AxiosError && error.response?.status === 401) {
        store.dispatch(logout())
        store.dispatch(sessionReady())
        return
      }
      throw error
    })
    .finally(() => {
      restoration = null
    })
  return restoration
}
