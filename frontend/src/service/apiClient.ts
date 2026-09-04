import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
}

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8001'

export const apiClient = axios.create({
  baseURL,
  withCredentials: true,
})

const refreshClient = axios.create({
  baseURL,
  withCredentials: true,
})

let refreshRequest: Promise<unknown> | null = null

const authEndpoints = [
  '/api/auth/register',
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/logout',
  '/api/auth/forgot-password',
  '/api/auth/verify-reset-otp',
  '/api/auth/reset-password',
]

const isAuthEndpoint = (url?: string) => {
  if (!url) {
    return false
  }

  return authEndpoints.some((endpoint) => url === endpoint || url.endsWith(endpoint))
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined

    if (
      !originalRequest ||
      error.response?.status !== 401 ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      refreshRequest ??= refreshClient.post('/api/auth/refresh')
      await refreshRequest
      return apiClient(originalRequest)
    } catch (refreshError) {
      return Promise.reject(refreshError)
    } finally {
      refreshRequest = null
    }
  },
)

export default apiClient
