import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { store } from '../store/store'
import { logout, setUser } from '../store/auth/authSlice'
import type { ApiResponse, AuthUser } from './authService'

type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean }
const options = {
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8001',
  withCredentials: true,
  timeout: 15000,
}

export const apiClient = axios.create(options)
const sessionClient = axios.create(options)
let csrfRequest: Promise<string> | null = null
let refreshRequest: Promise<void> | null = null

async function attachCsrf(config: InternalAxiosRequestConfig) {
  if (['get', 'head', 'options'].includes(config.method ?? 'get')) return config
  // Read the server's cookie-backed token before mutations. Concurrent requests share the lookup.
  csrfRequest ??= sessionClient
    .get<{ token: string }>('/api/auth/csrf')
    .then((response) => response.data.token)
    .finally(() => {
      csrfRequest = null
    })
  config.headers.set('X-XSRF-TOKEN', await csrfRequest)
  return config
}

apiClient.interceptors.request.use(attachCsrf)
sessionClient.interceptors.request.use(attachCsrf)

export function refreshSession(): Promise<void> {
  refreshRequest ??= sessionClient
    .post<ApiResponse<AuthUser>>('/api/auth/refresh')
    .then((response) => {
      if (response.data.data) store.dispatch(setUser(response.data.data))
    })
    .catch((error: AxiosError) => {
      if (error.response?.status === 401) store.dispatch(logout())
      throw error
    })
    .finally(() => {
      refreshRequest = null
    })
  return refreshRequest
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const request = error.config as RetryableRequest | undefined
    const isPublicAuth = request?.url?.startsWith('/api/auth/') && request.url !== '/api/auth/me'
    if (!request || error.response?.status !== 401 || request._retry || isPublicAuth) {
      if (request?._retry && error.response?.status === 401) store.dispatch(logout())
      return Promise.reject(error)
    }
    request._retry = true
    await refreshSession()
    return apiClient(request)
  },
)

export default apiClient
