import { expect, it, vi } from 'vitest'

it('restores a session only once when startup runs concurrently', async () => {
  vi.resetModules()
  const account = {
    name: 'Test',
    email: 'test@example.com',
    phone: '',
    status: 'ACTIVE',
    accountType: 'INDIVIDUAL',
    hasCompany: true,
    platformRoles: [],
    passwordChangeRequired: false,
  }
  const me = vi.fn().mockResolvedValue({ data: account })
  vi.doMock('../service/userService', () => ({ userService: { me } }))
  const { restoreSession } = await import('./restoreSession')
  const { store } = await import('../store/store')
  await Promise.all([restoreSession(), restoreSession()])
  expect(me).toHaveBeenCalledTimes(1)
  expect(store.getState().auth.user?.email).toBe(account.email)
  expect(store.getState().auth.initialized).toBe(true)
  vi.doUnmock('../service/userService')
})
