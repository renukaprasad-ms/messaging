import { configureStore } from '@reduxjs/toolkit'
import { render, screen } from '@testing-library/react'
import { Provider } from 'react-redux'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import authReducer, { sessionReady, setUser } from '../store/auth/authSlice'
import type { AuthUser } from '../service/authService'
import RequireAuth from './RequireAuth'
import { homePath } from './access'

const user: AuthUser = {
  name: 'Test',
  email: 'test@example.com',
  phone: '',
  status: 'ACTIVE',
  hasCompany: true,
  platformRoles: [],
  passwordChangeRequired: false,
}

function visit(account: AuthUser | null, initialized = true) {
  const store = configureStore({ reducer: { auth: authReducer } })
  if (initialized) store.dispatch(sessionReady())
  if (account) store.dispatch(setUser(account))
  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<RequireAuth platformAdmin />}>
            <Route path="/admin" element={<p>Admin content</p>} />
          </Route>
          <Route path="/login" element={<p>Login page</p>} />
          <Route path="/verify-email" element={<p>Verification page</p>} />
          <Route path="/change-password" element={<p>Change password page</p>} />
          <Route path="/forbidden" element={<p>Forbidden page</p>} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  )
}

describe('role entry', () => {
  it('waits for session restoration', () => {
    visit(null, false)
    expect(screen.getByRole('status')).toBeInTheDocument()
  })
  it('redirects guests to login', () => {
    visit(null)
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })
  it('does not grant platform access to company users', () => {
    visit(user)
    expect(screen.getByText('Forbidden page')).toBeInTheDocument()
  })
  it('allows superadmins', () => {
    visit({ ...user, platformRoles: ['SUPERADMIN'] })
    expect(screen.getByText('Admin content')).toBeInTheDocument()
  })
  it('requires verification before entry', () => {
    visit({ ...user, status: 'PENDING_VERIFICATION' })
    expect(screen.getByText('Verification page')).toBeInTheDocument()
  })
  it('rejects suspended users', () => {
    visit({ ...user, status: 'SUSPENDED' })
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })
  it('requires bootstrap password rotation', () => {
    visit({ ...user, platformRoles: ['SUPERADMIN'], passwordChangeRequired: true })
    expect(screen.getByText('Change password page')).toBeInTheDocument()
  })
  it('selects the landing page by account state', () => {
    expect(homePath({ ...user, platformRoles: ['SUPERADMIN'] })).toBe('/admin')
    expect(homePath({ ...user, hasCompany: false })).toBe('/company/create')
    expect(homePath(user)).toBe('/')
  })
})
