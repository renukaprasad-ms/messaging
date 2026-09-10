import { afterEach, expect, it, vi } from 'vitest'
import type { AxiosAdapter, InternalAxiosRequestConfig } from 'axios'

afterEach(() => {
  vi.resetModules()
})

it('shares one refresh for concurrent unauthorized requests and retries them', async () => {
  vi.resetModules()
  const axiosModule = await import('axios')
  const originalAdapter = axiosModule.default.defaults.adapter
  let refreshed = false
  let refreshCount = 0
  const adapter: AxiosAdapter = async (config) => {
    const response = { data: {}, status: 200, statusText: 'OK', headers: {}, config }
    if (config.url === '/api/auth/csrf') return { ...response, data: { token: 'csrf-test' } }
    if (config.url === '/api/auth/refresh') {
      refreshCount++
      expect(config.headers.get('X-XSRF-TOKEN')).toBe('csrf-test')
      await new Promise((resolve) => setTimeout(resolve, 5))
      refreshed = true
      return response
    }
    if (!refreshed)
      throw new axiosModule.AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, undefined, {
        ...response,
        status: 401,
      })
    return response
  }
  axiosModule.default.defaults.adapter = adapter
  try {
    const { apiClient } = await import('./apiClient')
    await Promise.all([apiClient.get('/api/companies'), apiClient.get('/api/admin/users')])
    expect(refreshCount).toBe(1)
  } finally {
    axiosModule.default.defaults.adapter = originalAdapter
  }
})

it('clears a stale user when refresh is rejected and never loops', async () => {
  vi.resetModules()
  const axiosModule = await import('axios')
  const originalAdapter = axiosModule.default.defaults.adapter
  let refreshCount = 0
  axiosModule.default.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    const response = { data: {}, status: 401, statusText: 'Unauthorized', headers: {}, config }
    if (config.url === '/api/auth/csrf')
      return { ...response, status: 200, data: { token: 'csrf-test' } }
    if (config.url === '/api/auth/refresh') refreshCount++
    throw new axiosModule.AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, undefined, response)
  }
  try {
    const { apiClient } = await import('./apiClient')
    const { store } = await import('../store/store')
    const { setUser } = await import('../store/auth/authSlice')
    store.dispatch(
      setUser({
        name: 'Test',
        email: 'test@example.com',
        phone: '',
        hasCompany: true,
        accountType: 'INDIVIDUAL',
        status: 'ACTIVE',
        platformRoles: [],
        passwordChangeRequired: false,
      }),
    )
    await expect(apiClient.get('/api/companies')).rejects.toThrow('Unauthorized')
    expect(store.getState().auth.user).toBeNull()
    expect(refreshCount).toBe(1)
  } finally {
    axiosModule.default.defaults.adapter = originalAdapter
  }
})
